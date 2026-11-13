---
label: 导入与导出
order: 80
---

# 导入与导出

<a id="exporting"></a>

## 导出

在编辑器中选择世界书，然后点击**导出**按钮（文件图标，*Export Lorebook*）。浏览器会下载 `<世界书名称>.json` 文件，其中包含：

- 该世界书及**所有子世界书**（每一层），包括它们的名称、描述、启用状态和标签；
- 每个条目的内容、备注、顺序、过滤器、插入模式、标签和负标签。

导出可用于保留副本、与其他用户分享世界观，或迁移到另一个安装实例。[扩展](../extensions/index.md)保存在世界书或条目中的数据也会包含在文件中，导入时会一并恢复。[Lorebook VCS](../extensions/lorebook-vcs.md) 扩展的条目修订记录属于旧条目，不会出现在导入生成的新条目上；该扩展有自己的导出功能来处理这些记录。

<a id="exporting-for-sillytavern"></a>

### 导出为 SillyTavern 格式

**导出**按钮会打开一个菜单，包含两项：**Export Lorebook**（即上述文件）和 **Export to SillyTavern**。后者会将世界书下载为 SillyTavern 的世界信息文件，可供 SillyTavern 导入。它与[导入](#importing-sillytavern-world-info)互为逆操作：

| Marginalia | SillyTavern |
|---|---|
| *Entry Name* | 标题 / 备忘（`comment`） |
| *Content*、*Order*、*Enabled* | 内容、顺序、禁用 |
| 无过滤器 | 常驻条目（🔵） |
| *Text* 过滤器 | 一个主关键词 |
| *Keywords (any)* | 每个关键词各一个主关键词 |
| *Keywords (all)* | 第一个关键词为主关键词，其余为次关键词，逻辑为 *AND ALL* |
| *Regex* | 一个写作 `/pattern/flags` 形式的关键词 |
| *In Lore Block* | 位置 *before character definitions* |
| *Before User Prompt* | 位置 *at depth* 1 |
| 标签 | 角色过滤（按名称）；仅当条目没有标签时才使用负标签（*exclude*） |

**子世界书会被合并**：文件中先是该世界书的条目，然后是其子世界书的条目，因为 SillyTavern 不支持世界书嵌套。SillyTavern 中没有对应项的内容会被省略，包括条目的*备注*、同时存在标签时的负标签，以及扩展的数据。SillyTavern 会在近期聊天中匹配关键词；反向的对应关系见[与其他方式的差异](#what-works-differently)。

!!! warning
正则表达式使用 Java 语法编写。SillyTavern 使用 JavaScript，因此简单的表达式可以正常工作，但仅 Java 支持的写法（如占有量词、`\p{...}` 字符类）在那里无法匹配。
!!!

<a id="importing-a-marginalia-lorebook"></a>

## 导入 Marginalia 世界书

1. 点击**导入**按钮（文件图标），选择**Import Marginalia Lorebook**。
2. 上传从 Marginalia 导出的文件。

导入总是创建**新的**世界书——包括该世界书及其子世界书，结构保持不变——即使你已有同名的世界书也是如此。导入的世界书会在编辑器中被选中。标签会按名称与你现有的标签匹配，不存在的则会新建。

旧版 Marginalia 导出的文件（单个世界书，没有子世界书）也可以导入。

!!!
世界书也会包含在[书籍备份](../books/backups.md)中。恢复或克隆备份时，可能会关联到你已有的世界书，而不是创建副本；参见[恢复备份](../books/backups.md#restoring-a-backup)。
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## 导入其他用户分享的世界书

导入菜单中的 **Import from other user** 会列出其他用户[分享](lorebooks.md#sharing-a-lorebook)的世界书，显示其名称、作者和描述；打开某一行即可阅读其中的条目，点击 **Import** 即可获得一份私人副本。只会包含作者所分享的子世界书。与文件导入不同，这种方式无需导出和发送文件：作者只需勾选 **Share with other users**。

<a id="importing-an-archive-of-text-files"></a>

## 导入文本文件压缩包

1. 点击**导入**按钮，选择 **Import from archive**。
2. 上传包含 `.md`（Markdown）文件的 `.zip` 文件。
3. 检查表格中的**条目名称**，如有需要可修改，然后点击 **Import**。

![上传压缩包的窗口](../../images/archive-lorebook-upload.png)

![导入前的条目名称](../../images/archive-entry-names.png)

Marginalia 会创建一个新世界书，名称取自文件名（不含 `.zip`），并为**每个 `.md` 文件创建一个条目**。文件按名称顺序排列，名称中的数字按数值比较（`2.md` 排在 `10.md` 之前），压缩包中的文件夹内的文件同样适用；条目的*顺序*依次设为 1、2、3……整个文件内容即为*内容*。其他文件、文件夹、隐藏文件（`.name`）以及在 Mac 上创建的压缩包中的 `__MACOSX` 文件夹都会被跳过。

**条目名称**按以下顺序确定：

1. 压缩包根目录中的 `names.txt` 文件：每行一个名称，第一行对应按上述顺序排列的第一个 `.md` 文件，依此类推（空行则该文件交由下一条规则处理；多余的行会被忽略）。如需为大量文件批量命名，例如通过脚本生成，可使用此方式；
2. 文件中的第一个标题（`# Chapter 1` 得到 *Chapter 1*）；
3. 去掉 `.md` 后的文件名。

对话框中的表格会显示结果，你可以直接覆盖任意名称。

导入过程中会显示进度。**Stop** 会在当前正在保存的条目完成后结束导入：世界书会保留已导入的条目（导入不会被撤销），并显示一条消息说明已导入了多少个文件。

条目会以**禁用**状态导入：它们没有过滤器，如果启用，每次请求都会发送，对于一整本章节文件来说通常并不需要。请按需启用其中一部分，可以使用过滤器或[标签](entries.md#tags)——对于大量条目，**Mass edit** 是更快捷的方式。

如果希望将这些文件作为章节导入故事中，参见[导入到书籍](../books/story-editor.md#importing-into-the-book)。

<a id="importing-sillytavern-world-info"></a>

## 导入 SillyTavern 世界信息

1. 点击**导入**按钮，选择 **Import from SillyTavern**。
2. 上传从 SillyTavern 导出的世界信息文件（`.json`）。

Marginalia 会创建一个新世界书，名称取自世界信息（或文件名），并转换每个条目：

| SillyTavern | Marginalia |
|---|---|
| 标题 / 备忘（`comment`） | *Entry Name*（没有标题时使用第一个关键词） |
| 内容 | *Content*——形如 `{%{{{user}}}%}` 和 `{%{{{char}}}%}` 的宏仍然有效，参见[宏](../templates/macros.md) |
| 顺序 | *Order* |
| 禁用 | 取消勾选 *Enabled* |
| 常驻（🔵），或没有关键词 | 始终激活（无过滤器） |
| 主关键词，可选的次关键词及其逻辑 | 过滤器，见下文 |
| 区分大小写、全词匹配 | 内置于过滤器中 |
| 位置 *before / after character definitions*、*example messages* | *In Lore Block* |
| 位置 *author's note*（顶部 / 底部）、*at depth* | *Before User Prompt* |
| 角色过滤 | 标签（*exclude* 时为负标签），按角色名称和标签匹配 |

**关键词会转为过滤器。** 单个普通关键词会变为 *Text* 过滤器（`dragon`）。其他情况——多个关键词、次关键词（*AND ANY*、*AND ALL*、*NOT ANY*、*NOT ALL*）、写作 `/regex/` 的关键词、区分大小写或全词匹配——都会合并为一个 *Regex* 过滤器，实现相同的效果。例如，关键词 `dragon`、`wyrm` 加上次关键词 `fire`（*AND ANY*）会变成：

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

你不需要理解这些表达式；只有在熟悉正则表达式的情况下才需要修改它们。

<a id="what-works-differently"></a>

### 与其他方式的差异

SillyTavern 中在 Marginalia 没有对应项的设置会列在条目的 **Note** 中，例如 *Imported from SillyTavern, not supported: probability 50%, inclusion group "weather".*。这些包括：

- 概率、包含组、sticky、cooldown 和 delay——条目只要匹配即会激活；
- 按*深度*插入——条目会改为放在用户提示之前。

最大的差异在于**关键词与什么内容匹配**。SillyTavern 扫描的是近期聊天记录。Marginalia 则会匹配[正在编写的部分的请求](entries.md#filters)——其场景、角色和指令——而导入的条目默认就是这样（其 **Activation Context** 为 *Instructions*）。因此，SillyTavern 中因为名字出现在最近几条消息里而被激活的条目，在 Marginalia 中只有当你在该轮次中提到这个名字时才会激活。若要获得与 SillyTavern 相同的效果，请将条目的 **Activation Context** 设为 *Instructions and parts of the story*，并将 **Parts** 设为需要查看的最新章节数，参见[查看故事内容](entries.md#looking-into-the-story)。导入也不会设置递归激活（条目激活其他条目）：请通过世界书的 **scan depth** 开启，参见[由其他条目引入的条目](entries.md#entries-brought-in-by-other-entries)。

导入后，请检查各个条目，决定哪些应保持始终激活，哪些保留其过滤器，哪些更适合用[标签](entries.md#tags)来处理。
