package de.docs_as_co.intellij.plugin.drawio.editor

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.impl.file.impl.FileManager
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.reactive.IPropertyView
import com.jetbrains.rd.util.reactive.Property
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*

class DiagramsWebView(lifetime: Lifetime, uiTheme: String) : BaseDiagramsWebView(lifetime, uiTheme) {
    private var _initializedPromise = AsyncPromise<Unit>()

    // hide the internal promise type from the outside
    fun initialized(): Promise<Unit> {
        return _initializedPromise;
    }

    private val _xmlContent = Property<String?>(null)
    val xmlContent: IPropertyView<String?> = _xmlContent

    override fun handleEvent(event: IncomingMessage.Event) {
        when (event) {
            is IncomingMessage.Event.Initialized -> {
                _initializedPromise.setResult(Unit)
            }
            is IncomingMessage.Event.Configure -> {
                send(OutgoingMessage.Event.Configure(DrawioConfig(false)))
            }
            is IncomingMessage.Event.AutoSave -> {
                println("MESSAGE: "+event)
                val pic = exportPng()
                //val s = pic.blockingGet(1000*15)
                //println("s: "+s)
                //File("").writeBytes(pic.)
                _xmlContent.set(event.xml)
            }
            is IncomingMessage.Event.Save -> {
                // todo trigger save
            }
            IncomingMessage.Event.Load -> {
                // Ignore
            }
        }
    }

    fun loadXmlLike(xmlLike: String) {
        _xmlContent.set(null) // xmlLike is not xml
        send(OutgoingMessage.Event.Load(xmlLike, 1))
    }

    fun loadPng(payload: ByteArray) {
        _xmlContent.set(null) // xmlLike is not xml
        val xmlLike = "data:image/png;base64," + Base64.getEncoder().encodeToString(payload)
        send(OutgoingMessage.Event.Load(xmlLike, 1))
    }

    override fun reload(uiTheme: String, onThemeChanged: Runnable) {
        super.reload(uiTheme) {
            // promise needs to be reset, to that it can be listened to again when the reload is complete
            _initializedPromise = AsyncPromise()
            onThemeChanged.run()
        }
    }

    fun exportSvg() : Promise<String> {
        val result = AsyncPromise<String>()
        send(OutgoingMessage.Request.Export(OutgoingMessage.Request.Export.XMLSVG)).then  { response ->
            val data = (response as IncomingMessage.Response.Export).data
            val payload = data.split(",")[1]
            val decodedBytes = Base64.getDecoder().decode(payload)
            result.setResult(String(decodedBytes))
        }
        return result
    }
    fun exportPng() : Promise<ByteArray> {
        val result = AsyncPromise<ByteArray>()
        send(OutgoingMessage.Request.Export(OutgoingMessage.Request.Export.XMLPNG)).then  { response ->
            val data = (response as IncomingMessage.Response.Export).data
            val payload = data.split(",")[1]
            val decodedBytes = Base64.getDecoder().decode(payload)

            val projectPath = ProjectManager.getInstance().openProjects.get(0).basePath

            //vlad: writing diagram picture
            File(projectPath+"/.work/architecture.png").writeBytes(decodedBytes)

            result.setResult(decodedBytes)
        }
        return result
    }

}
