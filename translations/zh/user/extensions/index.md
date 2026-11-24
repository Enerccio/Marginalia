---
label: 扩展
order: 930
icon: plug
---

# 扩展

扩展可以为 Marginalia 添加功能，而无需改动应用本身。Marginalia 附带三十个扩展：

| 扩展 | 添加的功能 |
|---|---|
| [xAI Grok](grok.md) | 将 xAI 的 Grok 模型作为推理提供方，通过 xAI 自己的 API 使用，推理过程与答案分开显示。 |
| [Arc Outline](arc-outline.md) | 在侧边栏中显示书中的弧线及其章节，以及它们的状态；模型可以为你撰写弧线。 |
| [Anthropic Claude](anthropic.md) | 将 Anthropic 的 Claude 模型作为推理提供方，通过 Anthropic 自己的 API 使用，思考摘要显示为推理过程。 |
| [Audiobook](audiobook.md) | 借助语音服务器把书变成有声书：为叙述者和每个角色分配声音，有用于最终版本的录音室，并可导出带章节标记的 mp3。[设置语音服务器](audiobook-server.md)。 |
| [Author's Note](authors-note.md) | 一条写给模型的备注，会插入到本书的每一次生成中。 |
| [Beta Readers](beta-readers.md) | 你虚构的读者（人设），以及与他们就本书展开的聊天，可单独进行，也可成组进行。 |
| [Book Description Generator](book-description-generator.md) | 借助模型，根据故事的某段内容撰写书名和简介，使用“关于”标签页中的按钮。 |
| [Chapter Marker](chapter-marker.md) | 根据部分中的 Markdown 标题，在故事大纲中生成章节标题。 |
| [Character Tracker](character-tracker.md) | 书中角色及其关系，并附带角色关系图。 |
| [Conlang Translator](conlang-translator.md) | 用你描述的人造语言呈现角色的话语，并保留英文原文供模型使用；读者点击即可查看原文。 |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | 让旧版 Gemma 4 模型的函数调用正常工作，这些模型的 API 要求工具结果使用另一种角色；若使用当前的聊天模板则无需此扩展。 |
| [Google Gemini](google-genai.md) | 将 Google 的 Gemini 模型作为推理提供方，通过 Google 自己的 API 使用，思考摘要显示为推理过程。 |
| [Image Generator](image-generator.md) | 为故事的某一部分生成图片：模型撰写提示词，由 ComfyUI 或兼容 OpenAI 风格的图像 API 绘制，你可以重新生成并接受结果。 |
| [Prompt History](prompt-history.md) | 在你的会话中保留发送给模型的最近 20 条请求（以及每次代理运行的记录），包括答案和推理过程，用于调试扩展和检查提示词；可从 Settings → Extension Settings 或代理的控制台打开。 |
| [Prompt Inspector](prompt-inspector.md) | 显示一次生成的提示词，并在发送给模型之前询问确认；在故事的设置菜单中开启或关闭。 |
| [Instruction Exporter](instruction-exporter.md) | 将故事的指令、场景设置和角色复制到剪贴板。 |
| [Knowledge Tracker](knowledge-tracker.md) | 记录每个被追踪角色知道什么、不知道什么，并将其放入提示词中，使模型不再让角色拥有他们不可能具有的知识。 |
| [Lore Generator](lore-generator.md) | 根据你已有的设定，借助模型撰写世界观词条的内容。 |
| [Lorebook VCS](lorebook-vcs.md) | 世界观词条的修订历史。 |
| [Manga Generator](manga-generator.md) | 将故事的部分转换为由页面和分镜组成的漫画脚本，并转换为图像生成标签。 |
| [Tools for the model](tools.md) | 生成器的 **Tools** 按钮：模型可以使用哪些工具套件（名称、地点）以及其中的哪些类别。 |
| [Name generator](name-generator.md) | 供模型使用、而非由其自行编造的名字：约 1270 份名单，以马尔可夫链的形式提供。 |
| [Name provider](name-provider.md) | 供模型挑选的真实名字：约 100 个国家的名和姓，保留原始拼写。 |
| [Place provider](place-provider.md) | 供模型查询的真实地点：城市、海洋、海湾、河流、山脉等，附带坐标和国家；可查询某地周边的情况以及两地之间的距离。 |
| [Real Language Translator](real-language-translator.md) | 用真实语言呈现角色的话语（例如以日本为背景的故事使用日语），并保留英文原文供模型使用；读者点击即可查看原文。 |
| [Reviewer](reviewer.md) | 由模型撰写的故事部分评论：读者反应、评析。 |
| [Side Query](side-query.md) | 在故事旁边与模型就你的书进行聊天。 |
| [Converter](sillytavern-converter.md) | 在故事旁边显示一段 SillyTavern 聊天以及模型据此准备的细节，用以将聊天改写为故事；或者将旧书的部分内容（来自备份或书本本身）连同其指令一起提供，用以重新起草。 |
| [State Tracker](state-tracker.md) | 被追踪角色的当前状态（衣着、随身物品、手边的武器、伤势、情绪，或你自定义的类别），并将其放入提示词中，使模型遵循这些状态。 |
| [Story Notes](story-notes.md) | 你自己的一个小型维基，由若干页面组成（带标签，页面之间可以互相链接），位于侧边栏中，用于记录情节、想法和世界规则；剧情向导和代理可以读取并修改你允许的页面。 |
| [Read Aloud](tts-reader.md) | 在阅读器中用你设备的声音朗读已发布的书。 |
| [Chinese localization](chinese-localization.md) | 简体中文作为用户界面的语言。 |

<a id="installing"></a>

## 安装

扩展由**管理员**为整个 Marginalia 安装统一安装，操作位置在 *Admin → Extensions*：
它们是 JAR 文件，通过 **Load Extension (.jar)** 上传。详情请参阅 [管理扩展](../administration/extensions.md)。

扩展安装后，每位用户都可以使用。在 Marginalia 运行期间加载的扩展，会出现在之后打开的窗口中：关闭并重新打开书籍（或刷新页面）即可看到。

!!!warning 使用匹配的版本
扩展与某一版本的 Marginalia 用户界面相连接。为其他版本构建的扩展可能无法正常工作，或会破坏应用的部分功能。请使用与你的 Marginalia 版本配套的扩展。
!!!

以上扩展是 Marginalia 源代码（`marginalia/plugins`）的一部分，但需单独构建；
参见 [插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)。

<a id="extensions-on-a-phone"></a>

## 在手机上使用扩展

[手机布局](../phone.md)有自己的类，因此扩展必须支持它。Arc Outline、Author's Note、Beta Readers、Chapter Marker、Character Tracker、Instruction Exporter 和 Reviewer 支持手机布局；Side Query、Lore Generator 和 Lorebook VCS 在手机上不提供任何内容（参见 [在手机上使用 Marginalia](../phone.md#extensions)）。Name generator、Name provider、Place provider、Gemma 4 tools fix 和 Google Gemini 没有自己的界面（Google Gemini 只是在模型对话框中添加一个提供方，手机同样有此对话框），Chinese localization 位于手机菜单的语言选择器中。扩展从管理页面安装，而手机没有该页面。

<a id="extension-settings"></a>

## 扩展设置

带有设置的扩展会在 **Settings → Extension Settings**（*Settings* 页面的第三个标签页）中添加一个部分。
这些设置属于你的账户，与 *Settings* 的其他设置一样，通过其 **Save** 按钮保存。

<a id="where-extension-data-is-kept"></a>

## 扩展数据的存放位置

扩展将其数据与所属的数据存放在一起：评论与故事部分存放在一起，对话与书籍存放在一起，修订记录与世界观词条存放在一起。因此这些数据会包含在[数据库备份](../administration/database-backups.md)中。
它是否也包含在[书籍备份](../books/backups.md)和导出中，在每个扩展的页面上有说明。

卸载扩展不会删除其数据：重新安装后，数据会恢复。
