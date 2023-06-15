package org.adg.ws

import com.intellij.openapi.diagnostic.Logger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.CharsetUtil
import java.net.URI
import java.net.URISyntaxException
import javax.net.ssl.SSLException

class WebSocketClient {
    private var group: EventLoopGroup? = null
    private var ch: Channel? = null
    private var messageHandler: WebSocketMessageHandler? = null
    fun connect(url: String?, messageHandler: WebSocketMessageHandler?) {
        this.messageHandler = messageHandler
        try {
            val uri = URI(url)
            val scheme = if (uri.scheme == null) "ws" else uri.scheme
            val host = if (uri.host == null) "127.0.0.1" else uri.host
            val port: Int
            port = if (uri.port == -1) {
                if ("ws".equals(scheme, ignoreCase = true)) {
                    80
                } else if ("wss".equals(scheme, ignoreCase = true)) {
                    443
                } else {
                    -1
                }
            } else {
                uri.port
            }
            if (!"ws".equals(scheme, ignoreCase = true) && !"wss".equals(scheme, ignoreCase = true)) {
                throw RuntimeException("Only WS(S) is supported.")
            }
            val ssl = "wss".equals(scheme, ignoreCase = true)
            val sslCtx: SslContext?
            sslCtx = if (ssl) {
                SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
            } else {
                null
            }
            group = NioEventLoopGroup()
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            val headers: HttpHeaders = DefaultHttpHeaders()
            //            headers.add("Host", "uniproxy.alice.ya.ru");
//            headers.add("Connection", "Upgrade");
//            headers.add("Pragma", "no-cache");
//            headers.add("Cache-Control", "no-cache");
//            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 YaBrowser/23.3.4.603 Yowser/2.5 Safari/537.36");
//            headers.add("Upgrade", "websocket");
//            headers.add("Origin", "https://ya.ru");
//            headers.add("Sec-WebSocket-Version", "13");
//            headers.add("Accept-Encoding", "gzip, deflate, br");
//            headers.add("Accept-Language", "ru,en;q=0.9");
//            headers.add("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
            val handler = WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, headers
                )
            )
            val b = Bootstrap()
            b.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val p = ch.pipeline()
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port))
                        }
                        p.addLast(
                            HttpClientCodec(),
                            HttpObjectAggregator(8192),
                            WebSocketClientCompressionHandler.INSTANCE,
                            handler
                        )
                    }
                })
            ch = b.connect(uri.host, port).sync().channel()
            handler.handshakeFuture()!!.sync()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: SSLException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun disconnect() {
        try {
            ch!!.writeAndFlush(CloseWebSocketFrame())
            ch!!.closeFuture().sync()
            group!!.shutdownGracefully()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun sendMessage(msg: String?) {
        val frame: WebSocketFrame = TextWebSocketFrame(msg)
        ch!!.writeAndFlush(frame)
    }

    fun ping() {
        val frame: WebSocketFrame = PingWebSocketFrame(Unpooled.wrappedBuffer(byteArrayOf(8, 1, 8, 1)))
        ch!!.writeAndFlush(frame)
    }

    inner class WebSocketClientHandler(private val handshaker: WebSocketClientHandshaker) :
        SimpleChannelInboundHandler<Any?>() {
        private var handshakeFuture: ChannelPromise? = null
        fun handshakeFuture(): ChannelFuture? {
            return handshakeFuture
        }

        override fun handlerAdded(ctx: ChannelHandlerContext) {
            handshakeFuture = ctx.newPromise()
        }

        override fun channelActive(ctx: ChannelHandlerContext) {
            handshaker.handshake(ctx.channel())
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            println("WebSocket Client disconnected!")
        }

        @Throws(Exception::class)
        public override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
            val ch = ctx.channel()
            if (!handshaker.isHandshakeComplete) {
                try {
                    handshaker.finishHandshake(ch, msg as FullHttpResponse?)
                    LOG.info("WebSocket Client connected!")
                    handshakeFuture!!.setSuccess()
                } catch (e: WebSocketHandshakeException) {
                    LOG.info("WebSocket Client failed to connect")
                    handshakeFuture!!.setFailure(e)
                }
                return
            }
            if (msg is FullHttpResponse) {
                val response = msg
                throw IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')'
                )
            }
            val frame = msg as WebSocketFrame?
            if (frame is TextWebSocketFrame) {
                val textFrame = frame
                LOG.info("WebSocket Client received message: " + textFrame.text())
                messageHandler!!.handleMessage(textFrame.text())
            } else if (frame is PongWebSocketFrame) {
                LOG.info("WebSocket Client received pong")
            } else if (frame is CloseWebSocketFrame) {
                LOG.info("WebSocket Client received closing")
                ch.close()
            }
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            if (!handshakeFuture!!.isDone) {
                handshakeFuture!!.setFailure(cause)
            }
            ctx.close()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            WebSocketClient::class.java
        )
    }
}