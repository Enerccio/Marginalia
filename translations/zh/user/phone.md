---
label: 在手机上使用 Marginalia
order: 985
icon: device-mobile
---

# 在手机上使用 Marginalia

Marginalia 为手机准备了专门的布局。你可以在手机浏览器中写作、编辑和重新生成段落、编辑 lorebook、管理模型，以及（作为管理员）管理整个安装实例。手机布局**不是桌面布局的复制**：同样的功能被排成单列，部分窗口更简单，并且并非所有扩展都能在其中使用。本页列出这些差异。

若要在手机上阅读书籍，还可以使用专为此设计的 [viewer](books/publishing.md)。

<a id="when-the-phone-layout-is-used"></a>

## 何时使用手机布局

Marginalia 在**页面加载时**查看浏览器窗口的宽度。窄于 768 像素时使用手机布局，更宽则使用常规的 [workspace](index.md#the-workspace)。

- 横屏的平板和手机通常宽于 768 像素，会使用桌面布局。
- 布局**不会在你工作时切换**。如果你把手机或平板侧放，或调整窗口大小，布局会保持原样，直到你**重新加载页面**。因此，若想在平板或转向的手机上获得较大的桌面布局，请转向后重新加载；若想回到手机布局，请再转回并重新加载。
- 较窄的桌面浏览器窗口也会使用手机布局，这便于预览效果。

与桌面版一样，手机布局可以像应用一样添加到主屏幕（浏览器菜单中的 *Install app* / *Add to Home Screen*）。

<a id="the-workspace"></a>

## 工作区

左侧的标签页被**抽屉**取代。点击顶栏中的菜单按钮即可打开抽屉，点击某个标签页即可切换。当前标签页会占满整个屏幕，其名称显示在顶栏中。

![打开抽屉的手机工作区](../images/phone-workspace-drawer.png)

抽屉中的标签页与桌面版相同：*Books*、*Lorebooks*、*Settings*、*Protocols*、*Inference Providers*、*Agents*（仅限可以使用 [agents](agents.md#on-a-phone) 的用户）、*Resources* 和 *Trash*。对于管理员，还有 *Admin*（桌面版中的按钮在此处是一个标签页）。底部是 **Language** 选择器、**Change Password** 和 **Logout**。扩展可以添加按钮，但只能放在它们在手机上已知的位置，参见 [Extensions](#extensions)。

当 *Settings* 标签页有未保存的更改时，抽屉会像桌面版一样被锁定：请先保存或放弃更改。

### Books

书籍列表为单列。列表上方是名称过滤器、标签过滤器和排序按钮（*Last Modified*、*Created*、*Name*；再次点击当前激活的按钮可反转顺序），然后是 **Add Book**、放大镜（[高级搜索](books/managing-books.md#advanced-search)：包含和排除的标签、日期、部分数量和文本），以及刷新按钮。每本书都有按钮：星标将其设为收藏，**Move to folder** 将其放入[文件夹](books/folders.md)，铅笔打开它，垃圾桶将其删除（移入[回收站](trash.md)）。

![手机上的书籍列表](../images/phone-book-list.png)

与桌面版一样，列表每次只显示一个[文件夹](books/folders.md)：当前所在文件夹的路径以按钮形式显示，用于返回；然后是复选框 **Include subfolders**；接着是书籍上方的文件夹（点击进入，会显示其下的书籍数量），以及顶层的虚拟文件夹 **Favorites**。名称和标签过滤器搜索当前文件夹，若勾选 *Include subfolders*，则搜索其下的所有内容。**Add Book** 和各种导入会把新书放入当前所在的文件夹。当前所在的文件夹会被记住。

![手机上的文件夹](../images/phone-folders.png)

![书籍列表的菜单](../images/phone-books-menu.png)

文件夹通过标题栏的 **New folder** 按钮创建，并可通过其所在行的 **⋮** 按钮重命名、设置（描述、标签、语言，以及其书籍使用的模型和协议）或删除。标题栏中的 **⋮** 按钮包含可对多本书操作的窗口：**Organise**（在手机上，两个文件夹窗格上下排列，且没有键盘提示）、**Tag books**、**Tag folders`，以及[文件夹](books/folders.md)的批量更改（模型和协议、语言、重命名）。书籍的语言也可以在此从文件夹继承，*Prompts* 标签页会显示每个值的来源。

### Lorebooks

手机上拥有桌面版的**完整 lorebook 编辑器**，以单列排布。Lorebook 在一个选择框中选取；其旁边的 **⋮** 按钮会打开 lorebook 菜单：**Add Lorebook**、**Clone**、**Delete**、**Import**（lorebook 文件、SillyTavern 文件、Markdown 文件的 zip 压缩包，或其他用户分享的 lorebook）、**Export**（lorebook 或 SillyTavern 格式）、**Tag lorebooks** 和 **Edit description**。在 *Enabled* 复选框下方，有 token 计数，以及 **Search and replace** 和 **activation preview** 按钮；然后是 **Settings** 部分，展开后可设置 lorebook 的名称、子 lorebook、标签、扫描深度，以及 *Share with other users*。参见 [Lorebooks](lorebooks/index.md)。

下方是 **Add Entry**（以及带有批量编辑和多条目标签功能的 **⋮**）、搜索框和条目列表。条目展开前，在一行中显示启用复选框和名称；展开后会出现一个表单：名称、顺序、标签、过滤模式与文本、插入模式、否定标签、激活上下文及其范围、*Also search the summaries*、*Prevent recursion*、*Only through other entries*、内容、备注和 **Delete entry**。所有更改都会立即保存，与桌面版一样。搜索框下方是用于限制列表的标签和状态选择框；**⋮** 菜单中的 **Select entries** 会在每个条目前放置复选框，并为已勾选的条目显示 **Delete selected**。此处没有的功能是复选框 *Only root lorebooks*。

抽屉中故事的 **Lorebook** 标签页显示书籍的设定，与桌面版的标签页一样：搜索框、可展开查看内容的条目，以及用于编辑条目文本的笔形按钮。

同一编辑器也用于书籍的 *Lorebook* 标签页。当书籍的 lorebook 包含子 lorebook 时，其下方的 **Shown lorebook** 选择框可将该标签页切换到任一子 lorebook，也可切换回来，而不会改变书籍所使用的 lorebook。当某个 [agent](agents.md) 正在处理使用该 lorebook 的书籍时，该 lorebook 以文本形式显示，其中的内容都无法更改。

![手机上的 lorebook](../images/phone-lorebook.png)

![lorebook 的菜单](../images/phone-lorebook-menu.png)

![lorebook 的设置](../images/phone-lorebook-settings.png)

![展开为表单的条目，上方是 Lorebook VCS 的修订记录](../images/phone-lorebook-entry.png)

*Enabled* 复选框旁边的按钮会打开 lorebook 条目的 **Search and replace**，参见 [Entries](lorebooks/entries.md#search-and-replace)。

![手机上 lorebook 的搜索和替换](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### 设置、协议、提供方、资源、回收站

- **Settings** 与桌面版相同，排成较窄的一列；**Import Backup as New Book** 是位于 *Discard Changes* 上方的按钮。
- **Protocols** 和 **Inference Providers** 以文本形式列出每一项，并在旁边配有按钮（对于提供方，按钮位于其下方，因为每项的按钮更多）。编辑对话框与桌面版相同，因此可以在手机上[分享](protocols.md#sharing-a-protocol)协议；列表上方的 **Import from other user** 按钮可导入其他用户分享的协议。
- **Resources** 按使用它的对象对文件分组（点击箭头展开分组）。每个文件显示为一张卡片，包括图片、名称、类型、大小、日期，以及下载、替换和删除按钮；卡片旁边有用于 *Delete selected* 的复选框。勾选某个分组会删除其所有文件。
- **Trash** 将每个已删除的对象显示为一张卡片（名称、类型、删除时间）。回收站的说明位于 *i* 按钮之后；过滤器按钮会换行到第二行。恢复操作的方式见 [Trash](trash.md)。

![手机上的资源](../images/phone-resources.png)

![手机上的推理提供方，带有已分享提供方的信息按钮](../images/phone-providers.png)

![手机上的回收站](../images/phone-trash.png)
<a id="the-book-window"></a>

## 书籍窗口

打开一本书时，它会以**全屏**显示。关闭按钮是顶部角落的 **✕**，各个标签页——*About*、*Prompts*、*Lorebook*、*Story*、*Branch view*、*Backups*——排成一行，可以左右滚动。它们的功能与桌面版相同（参见 [书籍](books/index.md)），字段排成一列。

<a id="the-story"></a>

### 故事

故事标签页是一列片段。与[故事编辑器](books/story-editor.md)的区别如下：

![手机上的一本书的故事](../images/phone-book-story.png)

- **大纲**（以及扩展在其旁边添加的标签页，例如 [作者注记](extensions/authors-note.md)）位于**抽屉**中。点击左上角的按钮即可打开。
- **Summaries** 按钮和设置菜单（⚙，包含 *Change Styles For Text*、*Search and replace*、*Import*、*Mass delete* 和 *Export story*）位于顶部栏，而不是底部。
- 用于新建片段的 **+** 按钮位于底部。
- **Summaries** 窗口的菜单栏（*Search and replace*、*Create summaries...*、*Resummarize all summaries*、*Export Summaries*、*Import Summaries*）单独占一行；在窄屏上，各项位于 **⋯** 按钮之后。[导入摘要](books/summaries.md#export-and-import-of-the-summaries)与桌面版是同一个窗口，只是宽度为屏幕宽度。

![手机上的 Import Summaries](../images/phone-summaries-import.png)

- [批注](books/annotations.md)：**Annotations** 标签页位于大纲旁边的抽屉中；点击带下划线的段落即可打开其注释；**Annotate part** 和 **Annotate with AI** 位于片段的菜单中，**Annotate selection** 位于对选中文本长按后的菜单中。
- **Search and replace** 打开的窗口与桌面版相同，字段和按钮上下排列：

  ![手机上的故事搜索和替换](../images/phone-search-replace.png)

- 片段只显示其文本，下方有一行小字，带两个图标。第一个是片段的**菜单**（编辑、重新生成、滑动、分支、历史、显示提示词、图片、摘要、删除）；第二个会打开一个**弹出窗口**，其中显示桌面版在片段右侧一栏中显示的内容：ID、模型、协议、token、字数以及轮次详情。
- 片段菜单中的 **History** 会以全屏页面打开[版本历史](books/version-history.md)：各个版本以卡片形式排列在所选版本上方，操作与桌面版相同。

  ![手机上片段的版本历史](../images/phone-history.png)

- **Statistics** 位于故事的设置菜单中（本书的统计），也是书籍列表标题中的一个按钮（所有书籍）。该窗口为全屏页面；其日历显示最近 16 周（桌面版为一年），参见[写作统计](books/statistics.md)。

  ![手机上所有书籍的统计](../images/phone-statistics.png)

- [摘要](books/summaries.md)概览保持固定宽度，并可左右滚动。

<a id="administration"></a>

## 管理

管理员在抽屉中有 **Admin** 标签页，其中的标签与[桌面页面](administration/index.md)相同，排成一行并可左右滚动：*Users*、*Database Backups*、*Cleanup*、*Resource Files*、*Help assistant*、*Agents*、*Update* 和 *Extensions*。列表（用户、备份、扩展）是卡片形式，其按钮位于文字下方，工具栏会换行到第二行。安装扩展（*Load Extension*）和上传数据库备份会打开手机的文件选择器。其他用户看不到该标签页。

![手机上 Admin 标签页中的用户](../images/phone-admin-users.png)

![手机上的数据库备份](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## 缺少的功能

手机版可以完成桌面版的所有操作，但有以下差异：

| 手机上缺少或不同的功能 | 在哪里操作 |
|---|---|
| 复选框 *Only root lorebooks* 以及一次选择多个 lorebook 条目进行删除的功能 | 桌面版界面。 |
| 命令栏、键盘快捷键、重新生成片段的差异视图以及页边注释 | 桌面版界面，这些功能需要键盘或宽屏。 |
| 列数很多或分为两个窗格的窗口（[摘要](books/summaries.md)概览、Beta Readers 对话、Manga Generator）显示较小 | 它们可以使用：横屏更便于操作。 |

本列表之外的所有功能都与桌面版相同：生成、编辑、重新生成、滑动和分支片段，摘要和元摘要，书籍提示词，分支树，书籍备份，导出，资源，回收站，lorebook 以及管理功能。

<a id="extensions"></a>

## 扩展

手机版布局有自己的类，因此扩展必须明确支持它。Marginalia 自带的扩展如下：

| 扩展 | 在手机上 |
|---|---|
| [Arc Outline](extensions/arc-outline.md) | 可用。Arcs 标签页位于大纲抽屉中。 |
| [Author's Note](extensions/authors-note.md) | 可用。注记位于故事的大纲抽屉中。 |
| [Beta Readers](extensions/beta-readers.md) | 可用。Beta Readers 标签页位于大纲抽屉中；对话区域较小，横屏更便于操作。 |
| [Book Description Generator](extensions/book-description-generator.md) | 可用。按钮位于 About 标签页的末尾；对话框将选项显示在结果上方。 |
| [Chapter Marker](extensions/chapter-marker.md) | 可用。章节标题位于大纲抽屉中。 |
| [Character Tracker](extensions/character-tracker.md) | 可用。Characters 标签页位于大纲抽屉中。 |
| [Story Notes](extensions/story-notes.md) | 可用。Notes 标签页位于大纲抽屉中；页面对话框适应屏幕，编写较长页面时横屏更便于操作。 |
| [Conlang Translator](extensions/conlang-translator.md) | 部分可用。*Settings → Extension Settings* 中的面板、编辑器、审阅和向导可以使用（词典网格可左右滚动），在查看器中点击译文对话也可以；故事部分（**Translate dialogs**、**Translate parts (constructed language)**、故事中的点击）未在手机上测试，参见[限制](extensions/conlang-translator.md#limits)。 |
| [Converter](extensions/sillytavern-converter.md) | 可用。Converter 标签页位于大纲抽屉中；聊天的范围用两次点击选择，参见[在手机上](extensions/sillytavern-converter.md#on-a-phone)。 |
| [Instruction Exporter](extensions/instruction-exporter.md) | 可用。**Export instructions** 位于故事的设置菜单中。 |
| [Manga Generator](extensions/manga-generator.md) | 可用。**Manga Generator** 位于故事的设置菜单中；其窗口较大，横屏更便于操作。 |
| [Reviewer](extensions/reviewer.md) | 可用。其项目位于片段的菜单中，其设置位于 *Settings → Extension Settings* 中。 |
| [Side Query](extensions/side-query.md) | 可用。Side Query 标签页位于大纲抽屉中。 |
| [Lore Generator](extensions/lore-generator.md) | 可用。魔杖按钮位于 lorebook 每个条目的行中；其窗口在结果上方显示提示词。 |
| [Lorebook VCS](extensions/lorebook-vcs.md) | 可用。lorebook 的面板位于其 *Enabled* 行下方，条目的修订版本位于其表单顶部。 |
| [Image Generator](extensions/image-generator.md) | 可用。**Generate image** 位于片段的菜单中。 |

来自其他来源的扩展在手机上的表现可能不同，或者完全没有内容；它们会在作者使其可用的地方出现。如果某个扩展似乎缺失，请在桌面版界面中查看。扩展如何支持手机版在[开发者手册](../developer/plugins/extendable.md#the-phone-twins)中有说明。

<a id="tips"></a>

## 提示

- 在手机上写长文本时，横屏更方便。转向后布局仍保持为手机版，直到你重新加载页面。
- 如果需要手机上缺少的功能，请将手机横放并重新加载页面，或在更宽的窗口中打开 Marginalia；你的书籍和设置在各处都是相同的。
- 要从手机访问 Marginalia，它必须以[服务器](getting-started/docker-server.md)形式运行：[桌面应用](getting-started/desktop-app.md)只在你的电脑上监听。
