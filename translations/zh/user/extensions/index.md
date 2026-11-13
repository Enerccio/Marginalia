---
label: 扩展
order: 930
icon: plug
---

# 扩展

扩展可以为 Marginalia 添加功能，而不会改动应用本身。Marginalia 随附二十三个扩展：

| 扩展 | 添加的内容 |
|---|---|
| [Arc Outline](arc-outline.md) | 侧边栏中书籍的篇章及其章节和状态；模型可以为你撰写篇章。 |
| [Anthropic Claude](anthropic.md) | 通过 Anthropic 自有 API 提供的 Anthropic Claude 模型，并将思考摘要显示为推理过程。 |
| [Audiobook](audiobook.md) | 借助语音服务器将书籍制作成有声书：为旁白和每个角色配置声音，提供用于最终版本的录音室，并可导出带章节标记的 mp3。[设置语音服务器](audiobook-server.md)。 |
| [Author's Note](authors-note.md) | 一条发给模型的备注，会插入到本书的每次生成中。 |
| [Beta Readers](beta-readers.md) | 你虚构的读者（角色设定），以及与他们就书籍进行的对话，可单独或以小组形式进行。 |
| [Book Description Generator](book-description-generator.md) | 借助模型，根据故事的某一范围撰写书名和简介，使用“关于”标签页中的按钮。 |
| [Chapter Marker](chapter-marker.md) | 根据正文中的 Markdown 标题，在故事大纲中添加章节标题。 |
| [Character Tracker](character-tracker.md) | 书中角色及其关系，以及角色关系图。 |
| [Conlang Translator](conlang-translator.md) | 用你描述的人造语言来呈现角色的对话，背后保留英文供模型使用，读者点击即可查看。 |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | 使 Gemma 4 模型能够进行函数调用；这些模型的 API 要求工具结果使用另一种角色。 |
| [Google Gemini](google-genai.md) | 通过 Google 自有 API 提供的 Google Gemini 模型，并将思考摘要显示为推理过程。 |
| [Image Generator](image-generator.md) | 为故事的某一部分生成图片：模型撰写提示词，由 ComfyUI 或兼容 OpenAI 风格的图像 API 绘制，你可以重新生成并接受结果。 |
| [Prompt History](prompt-history.md) | 在你的会话中（以及每次智能体运行中）保留发送给模型的最近 20 次请求，包括回答和推理过程，用于调试扩展和检查提示词；可从“设置 → 扩展设置”或智能体的控制台打开。 |
| [Prompt Inspector](prompt-inspector.md) | 显示一次生成所用的提示词，并在发送给模型前询问你；在故事的设置菜单中开启或关闭。 |
| [Instruction Exporter](instruction-exporter.md) | 将故事的指令、场景设定和角色复制到剪贴板。 |
| [Knowledge Tracker](knowledge-tracker.md) | 记录每个被追踪角色知道和不知道的内容，并将其放入提示词，使模型不再让角色掌握他们不可能知道的信息。 |
| [Lore Generator](lore-generator.md) | 根据你已有的设定，借助模型撰写一条设定集条目的内容。 |
| [Lorebook VCS](lorebook-vcs.md) | 设定集条目的修订历史。 |
| [Manga Generator](manga-generator.md) | 将故事的部分内容转换为由页和分镜组成的漫画脚本，以及图像生成标签。 |
| [Tools for the model](tools.md) | 生成器的 **Tools** 按钮：模型可以使用哪些工具套件（名称、地点），以及其中的哪些类别。 |
| [Name generator](name-generator.md) | 供模型使用的名字，而不必让它凭空编造：约 1270 份名字列表，以马尔可夫链的形式提供。 |
| [Name provider](name-provider.md) | 供模型挑选的真实名字：约 100 个国家的名和姓，保留原始拼写。 |
| [Place provider](place-provider.md) | 供模型查询的真实地点：城市、海洋、海湾、河流、山脉等，附带坐标和所在国家，可查询某地周边情况以及两地之间的距离。 |
| [Real Language Translator](real-language-translator.md) | 用真实语言呈现角色的对话（例如以日本为背景的故事使用日语），背后保留英文供模型使用，读者点击即可查看。 |
| [Reviewer](reviewer.md) | 由模型撰写的故事部分评论，包括读者反应和批评。 |
| [Side Query](side-query.md) | 在故事旁边与模型就你的书籍进行对话。 |
| [Converter](sillytavern-converter.md) | 在故事旁边显示 SillyTavern 聊天记录，以及模型根据它准备的转换细节，用于将聊天重写为故事；或者将旧书籍的部分内容（来自备份或书籍本身）及其指令载入，以便重新改写。 |
| [Story Notes](story-notes.md) | 侧边栏中一个由你自己的页面组成的小型维基（页面可添加标签并相互链接），用于记录情节、想法和世界规则；情节向导和智能体可以读取并修改你允许的页面。 |
| [Read Aloud](tts-reader.md) | 在阅读器中使用你设备的声音朗读已发布的书籍。 |
| [Chinese localization](chinese-localization.md) | 将简体中文作为用户界面的语言。 |

<a id="installing"></a>

## 安装

扩展由**管理员**为整个 Marginalia 安装统一安装，位置在 *Admin → Extensions*：
它们是 JAR 文件，通过 **Load Extension (.jar)** 上传。详情参见[管理扩展](../administration/extensions.md)。

安装后，扩展对每位用户都可用。在 Marginalia 运行期间加载的扩展，会出现在之后打开的窗口中：关闭并重新打开书籍（或刷新页面）即可看到。

!!!warning 使用版本匹配的扩展
扩展依赖于某个 Marginalia 版本的用户界面。为其他版本构建的扩展可能无法正常工作，或破坏应用的部分功能。请使用与你的 Marginalia 版本随附的扩展。
!!!

以上扩展都是 Marginalia 源代码（`marginalia/plugins`）的一部分，并单独构建；参见[插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)。

<a id="extensions-on-a-phone"></a>

## 在手机上使用扩展

[手机布局](../phone.md)有自己的样式类，因此扩展必须专门支持它。Arc Outline、Author's Note、Beta Readers、Chapter Marker、Character Tracker、Instruction Exporter 和 Reviewer 支持手机布局；Side Query、Lore Generator 和 Lorebook VCS 在手机上不添加任何内容（参见[在手机上使用 Marginalia](../phone.md#extensions)）。Name generator、Name provider、Place provider、Gemma 4 tools fix 和 Google Gemini 没有自己的界面（Google Gemini 只在模型对话框中添加一个提供方，手机同样有此对话框），Chinese localization 则位于手机菜单的语言选择器中。扩展需要在 Admin 页面安装，而手机上没有该页面。

<a id="extension-settings"></a>

## 扩展设置

带有设置的扩展会在 **Settings → Extension Settings**（*Settings* 页面的第三个标签页）中添加一个分区。这些设置与 *Settings* 的其他内容一样属于你的账户，并通过其 **Save** 按钮保存。

<a id="where-extension-data-is-kept"></a>

## 扩展数据存放位置

扩展将数据与其所属的数据存放在一起：评论与故事部分存放在一起，对话与书籍存放在一起，修订与设定集存放在一起，因此它们都包含在[数据库备份](../administration/database-backups.md)中。它们是否也包含在[书籍备份](../books/backups.md)和导出中，在每个扩展的页面上有说明。

卸载扩展不会删除其数据：重新安装后数据会随之恢复。
