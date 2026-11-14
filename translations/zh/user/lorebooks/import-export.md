---
label: 导入与导出
order: 80
---

# 导入与导出

<a id="exporting"></a>

## 导出

在编辑器中选中设定集，点击 **export** 按钮（文件图标，*Export Lorebook*）。浏览器会下载 `<lorebook name>.json`，其中包含：

- 设定集及其**所有子设定集**（每一层），包括名称、描述、启用状态和标签；
- 每个条目的内容、备注、顺序、过滤器、插入模式、标签和反向标签。

可用于保存副本、与其他用户分享世界观，或迁移到另一个安装实例。[扩展](../extensions/index.md)在设定集或条目中保存的数据也包含在文件里，导入时会一并恢复。[Lorebook VCS](../extensions/lorebook-vcs.md) 扩展的条目修订记录属于旧条目，不会出现在导入所创建的条目上；该扩展有自己的导出功能来处理这些记录。

<a id="exporting-for-sillytavern"></a>

### 导出为 SillyTavern 格式

**export** 按钮会打开一个包含两项的菜单：**Export Lorebook**（即上述文件）和 **Export to SillyTavern**。后者会把设定集下载为 SillyTavern 世界信息文件，SillyTavern 可以直接导入。它与[导入](#importing-sillytavern-world-info)互为逆操作：

| Marginalia | SillyTavern |
|---|---|
| *Entry Name* | 标题 / 备忘（`comment`） |
| *Content*, *Order*, *Enabled* | 内容、顺序、disabled |
| 无过滤器 | 常驻条目（🔵） |
| *Text* 过滤器 | 一个主关键词 |
| *Keywords (any)* | 每个关键词各一个主关键词 |
| *Keywords (all)* | 第一个关键词为主关键词，其余为次关键词，逻辑为 *AND ALL* |
| *Regex* | 一个写成 `/pattern/flags` 的关键词 |
| *In Lore Block* | 位置为 *before character definitions*（角色定义之前） |
| *Before User Prompt* | 位置为 *at depth* 1 |
| 标签 | 角色过滤器（按名称）；仅当条目没有标签时，反向标签才会作为 *exclude* |

**子设定集会被合并**：文件中先是设定集本身的条目，然后是其子设定集的条目，因为 SillyTavern 没有设定集嵌套设定集的结构。SillyTavern 中没有对应项的内容会被略去：条目的*备注*、与标签同时存在的反向标签、扩展的数据。SillyTavern 会在最近的聊天内容中匹配关键词，反方向的说明见[与 SillyTavern 的差异](#what-works-differently)。

!!! warning
正则表达式按 Java 语法书写。SillyTavern 使用 JavaScript，因此简单的表达式可以工作，但仅 Java 支持的写法（如占有量词、`\p{...}` 类）在那里不会匹配。
!!!

<a id="importing-a-marginalia-lorebook"></a>

## 导入 Marginalia 设定集

1. 点击 **import** 按钮（文件图标），选择 **Import Marginalia Lorebook**。
2. 上传从 Marginalia 导出的文件。

导入总是创建**新的**设定集（包括设定集及其子设定集，结构保持不变），即使已有同名设定集。导入后的设定集会在编辑器中被选中。标签会按名称与已有标签匹配，不存在的则会新建。

来自较早 Marginalia 版本的文件（单个设定集、没有子设定集）也可以导入。

!!!
设定集也会包含在[书籍备份](../books/backups.md)中。恢复或克隆备份时，可能会关联到你已有的设定集，而不是创建副本；参见[恢复备份](../books/backups.md#restoring-a-backup)。
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## 导入其他用户分享的设定集

导入菜单中的 **Import from other user** 会列出其他用户[分享](lorebooks.md#sharing-a-lorebook)的设定集，显示名称、作者和描述；打开某一行可以阅读其中的条目，点击 **Import** 即可获得一份私人副本。只会包含作者分享的子设定集。与文件导入不同，这种方式无需导出和发送文件：作者只需勾选 **Share with other users**。

<a id="importing-an-archive-of-text-files"></a>

## 导入文本文件压缩包

1. 点击 **import** 按钮，选择 **Import from archive**。
2. 上传一个 `.zip` 文件，其中含有 `.md`（Markdown）文件。
3. 检查表格中的**条目名称**，如需要可修改，然后点击 **Import**。

![上传压缩包的窗口](../../images/archive-lorebook-upload.png)

![导入前的条目名称](../../images/archive-entry-names.png)

Marginalia 会新建一个设定集，以文件名命名（去掉 `.zip` 后缀），并为**每个 `.md` 文件**创建一个条目。文件按名称顺序排列，名称中的数字按数值比较（`2.md` 排在 `10.md` 之前），压缩包中子文件夹里的文件也一样；条目的*顺序*依次为 1、2、3……。整个文件作为*内容*。其他文件、文件夹、隐藏文件（`.name`）以及 Mac 上生成的压缩包中的 `__MACOSX` 文件夹都会被跳过。

**条目名称**按以下顺序确定：

1. 压缩包根目录中的一个 `names.txt` 文件：每行一个名称，第一行对应按上述顺序排列的第一个 `.md` 文件，依此类推（空行则该文件交由下一条规则处理；多出的行会被忽略）。可用于一次性命名大量文件，例如由脚本生成；
2. 文件中的第一个标题（`# Chapter 1` 会得到 *Chapter 1*）；
3. 去掉 `.md` 后缀的文件名。

对话框中的表格显示最终结果，你可以直接改写任何名称。

导入随后显示进度。**Stop** 会在当前正在保存的条目之后结束导入：设定集保留已导入的条目（导入不会被撤销），并显示一条消息说明导入了多少个文件。

这些条目会以**禁用**状态导入：它们没有过滤器，若启用，每次请求都会发送它们，对一整本章节来说通常并不需要。根据需要为它们开启，可以使用过滤器或[标签](entries.md#tags)；对于大量条目，**Mass edit** 是快速的做法。

若想把同样的文件作为部分导入故事，参见[导入到书籍中](../books/story-editor.md#importing-into-the-book)。

<a id="importing-sillytavern-world-info"></a>

## 导入 SillyTavern 世界信息

1. 点击 **import** 按钮，选择 **Import from SillyTavern**。
2. 上传从 SillyTavern 导出的世界信息文件（`.json`）。

Marginalia 会新建一个设定集，以世界信息（或文件）命名，并转换每个条目：

| SillyTavern | Marginalia |
|---|---|
| 标题 / 备忘（`comment`） | *Entry Name*（没有标题时使用第一个关键词） |
| 内容 | *Content*——`{%{{{user}}}%}` 和 `{%{{{char}}}%}` 等宏仍然有效，参见[宏](../templates/macros.md) |
| 顺序 | *Order* |
| 禁用 | 取消勾选 *Enabled* |
| 常驻（🔵），或没有关键词 | 始终激活（无过滤器） |
| 主关键词，以及可选的次关键词及其逻辑 | 过滤器，见下文 |
| 区分大小写、全词匹配 | 内置于过滤器中 |
| 位置 *before / after character definitions*、*example messages* | *In Lore Block* |
| 位置 *author's note*（顶部 / 底部）、*at depth* | *Before User Prompt* |
| 角色过滤器 | 标签（*exclude* 时为反向标签），按角色名称和标签匹配 |

**关键词会变成过滤器。** 单个普通关键词会变成 *Text* 过滤器（`dragon`）。其他情况——多个关键词、次关键词（*AND ANY*、*AND ALL*、*NOT ANY*、*NOT ALL*）、写成 `/regex/` 的关键词、区分大小写或全词匹配——都会变成一个功能相同的 *Regex* 过滤器。例如，关键词 `dragon`、`wyrm` 加上次关键词 `fire`（*AND ANY*）会变成：

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

你不需要理解这些表达式；只有熟悉正则表达式时才需要修改。

<a id="what-works-differently"></a>

### 与 SillyTavern 的差异

SillyTavern 中没有 Marginalia 对应项的设置会被记录在条目的**备注**中，例如 *Imported from SillyTavern, not supported: probability 50%, inclusion group "weather".* 这些包括：

- 概率、包含组、sticky、cooldown 和 delay——条目只要匹配即会激活；
- *at depth* 插入——条目改为放在用户提示之前。

最大的区别在于**关键词与什么内容进行匹配**。SillyTavern 扫描最近的聊天记录。Marginalia 则匹配[当前正在写的部分的请求](entries.md#filters)——其场景、角色和指令——导入的条目也正是如此（其**激活上下文**为 *Instructions*）。因此，SillyTavern 中因为某个名字出现在最近消息里而激活的条目，在 Marginalia 中只有当你在本轮中提到该名字时才会激活。要达到 SillyTavern 的效果，请把条目的**激活上下文**设为 *Instructions and parts of the story*，并把 **Parts** 设为要查看的最新部分数量，参见[查看故事内容](entries.md#looking-into-the-story)。递归激活（条目激活其他条目）同样不会由导入设置：可通过设定集的 **scan depth** 开启，参见[由其他条目引入的条目](entries.md#entries-brought-in-by-other-entries)。

导入后，请检查这些条目，决定哪些应始终激活，哪些保留过滤器，哪些更适合用[标签](entries.md#tags)处理。
