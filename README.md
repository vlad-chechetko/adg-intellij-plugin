# Плагин IntelliJ IDEA для авто-генерации текстового описания к архитектурной схеме

## О плагине
<!-- Plugin description -->

Плагин позволяет создавать и редактировать диаграммы в формате drawio и автоматически формировать документ с текстовым описанием к диаграмме. 

На данный момент поддреживается диаграмма типа System Context Diagram в нотации C4 https://c4model.com/. Документация формируется в формате AsciiDoc https://docs.asciidoctor.org/asciidoc/latest/. Для формирования текстового описания используется сервис YandexGPT https://yandex.ru/project/alice/yagpt.

<!-- Plugin description end -->

## Текущая стадия разработки

На данный момент плагин на стадии proof of concept. Т.е. не претендует на полноту или универальность. Реализован супер-минимум функционала, только чтобы попробовать - возможно / не возможно.

## Постановка задачи

Реализация делалась по мотивам этого типа ТЗ "src/docs/adg/adg_plugin_requirements.docx". В качестве ожидаемого результата представлялось это https://github.com/Hemiun/craftsman_Design/.

## Как запустить

В IDEA:
- Клонируем git репозиторий
- Импортируем проект в IDEA
- Кнопочкой в окошке Gradle в группе intellij запускаем задачу runIde
- Должно открыться новое окно с "IDE песочнией" и установленным плагином

С командной строки:
- Клонируем git репозиторий
- Запускаем команду `./gradlew runIde`
- Должно открыться новое окно с "IDE песочнией" и установленным плагином

## Предполагаемый сценарий работы пользователя

Пользователь:

- Запускаем IDEA с adg-плагином
- Создаём новый проект, выбираем тип модуля Autodoc Generator
- После того как проект создан переходим в папку diagrams и редактируем файл c4context.drawio
- Далее на панели инструментов справа нажимает молоточек, запускается сборка.
- В процессе сборки в консоль внизу выводится информация о том, что происходит и результат сборки
- Открываем файл architecture.adoc
- IDEA предлагает установить плагин для adoc файлов. Соглашаемся.
- Смотрим сгенерённое по диаграмме текстовое описание.
- Если не нравится, можно нажать молоточек ещё раз и получить новую версию.

## Как устроен плагин

### Редактор drawio диаграм

За основу взят этот https://github.com/docToolchain/diagrams.net-intellij-plugin плагин, который позволяет редактировать .drawio диаграммы. 

В плагин скопированы исходники open source редактора drawio https://github.com/jgraph/drawio. Редактор drawio написан на javascript. Это всё добро лежит в папке src/webview. Надо заметить, что исходники старые и чуть-чуть подправленные в паре мест.

Чтобы сделать из javascript редактора плагин IDEA, в проект добавлена соответвующая "InelliJ IDEA обёртка". Обёртка написана на Kotlin и лежит в src/main/kotlin в пакете de.docs_as_co.intellij.plugin.drawio. Kotlin-обёртка регистрирует в IDEA редактор и ассоциирует его с файлами ".drawio". При открытии .drawio файла в IDEA запускается встроенный браузер и в нём открывается вебстраничка src/webview/index.html, в которую подгужадется javascript-овый ад и собственно открываемый .drawio файл.

### Внесение изменений в редактор

#### Плагины

Редктор предлагает механизм расширения через javascript drawio плагины. Для целей проверки наших возможностей в редактор добавлен новый плагин "src/webview/drawio/src/main/webapp/plugins/test123.js". Плагин регистрирует новый пункт меню, при выборе которого выводится диалоговое окошко. 

В процессе сборки весь javascript собирается в файл src/webview/drawio/src/main/webapp/js/app.min.js. Чтобы зарегистрирвать плагин, в app.min.js пришлось внести маленькую правку (ищи строку 
test123:"plugins/test123.js").

#### Изменение без плагинов

Чтобы внести изменения в основную логику работы редактора (нельзя поменять плагинами), используется дополнительный javascript код в src/webview/index.html. В основном код переопределяет уже существующие функции и добавляет в них то, что требуется поменять (см комментарии вида "//Vlad: хххх" для примера).

### Элементы UI для генерации документа по схеме

Функционал, связанный с элементами UI для запуска процесса генерации текстового описание по схеме, лежит в src/main/kotlin в пакете org.adg. Эта часть регистрирует новый тип модуля org.adg.AdgModuleType, который должен быть выбран при создании нового проекта для работы с документацией. Модуль добавляет в IDE панельку с инструментами справа и консольное окошко для вывода сообщений внизу. Всё, что связано с UI, можно найти в пакете kotlin/org/adg/ui.

### Генерация иллюстрации

Kotlin-обёртка для javascript редактора умеет перехватывать некоторые события javascript-редактора. В частности, сохранение отредактированной drawio диаграммы. В сохранение drawio файла добавлено автоматическое создание png картинки со схемой и её запись в /.work/architecture.png. (см функцию exportPng() в de.docs_as_co.intellij.plugin.drawio.editor.DiagramsWebView). Картинка формируется и сохраняется каждый раз при любых изменения на диаграмме. Далее эта кертинка будет добавлена в формируемый текстовый документ, как иллюстрация.

### Ограничения для C4 диаграммы

Предполагается, что на диаграмме будут один или более "Пользователей", одна "Система", ноль и более "Внешних систем". Между элементами должны быть просталены связи (стрелочки) с соответствующим описанием. Для каждого элемента нужно задать внятное name и description (см св-ва элемента в редакторе).

Если что-то сделать не так - в процессе сборки будет выдаваться ошибка.

### Использование GPT

Для формирования текстового описания используентся нейросеть YandexGPT. Общение с YandexGPT делается по вебсокету. Реализацию ws клиента см в kotlin/org/adg/ws/WebSocketClient.kt. Логика формирования сообщений для взаимодействия с YandexGPT в пакете kotlin/org/adg/gpt. Шаблоны json сообщений в resources/yagpt.

### Формирование adoc файла

Итоговый adoc документ формруется на основе шаблона. Шаблон лежит в resources/templates/template_short.adoc. В процессе создания нового проекта с ADG модулем, этот шаблон копируется в папку .work создаваемого проекта.

## Полезные ссылки

### GPT

YandexGPT https://yandex.ru/project/alice/yagpt

Доступ к другим GPT сервисам https://poe.com/Claude-instant

Исходники Kandinsky https://github.com/ai-forever/Kandinsky-2

## IDEA plugin

Документация по SDK
https://plugins.jetbrains.com/docs/intellij/project-wizard.html#implementing-module-builder-listener

Официальные примеры по SDK
https://github.com/JetBrains/intellij-sdk-code-samples

Иконки платформы
https://jetbrains.design/intellij/resources/icons_list/

Пример реализации ModuleBuilder для java
https://github.com/JetBrains/intellij-community/blob/idea/231.8109.175/java/openapi/src/com/intellij/ide/util/projectWizard/JavaModuleBuilder.java

Пример реализации ToolWindow для Ant
https://github.com/JetBrains/intellij-community/blob/idea/231.8109.175/plugins/ant/src/com/intellij/lang/ant/config/explorer/AntExplorer.java

Пример полноценного модуля
https://github.com/bulenkov/RedlineSmalltalk/blob/master/resources/META-INF/plugin.xml

Ещё пример модуля
https://stackoverflow.com/questions/25951117/how-do-i-register-a-new-module-type-in-an-intellij-plugin

Пример Console
https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window

## C4

https://c4model.com/

## Drawio

https://app.diagrams.net/
