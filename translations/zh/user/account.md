---
label: 账户与设置
order: 920
icon: person
---

# 账户与设置

你在 Marginalia 中创建的所有内容都属于你的账户：书籍、lorebook（设定集）、标签、推理服务提供方、协议和设置。其他用户看不到这些内容（你[发布](books/publishing.md)的书籍除外），你也看不到他们的内容。

<a id="your-account"></a>

## 你的账户

工作区底部的按钮：

| 按钮 | |
|---|---|
| **Language** | 用户界面的语言，参见 [语言](#language)。 |
| 太阳 / 月亮按钮 | 位于语言按钮旁边：在深色和浅色主题之间切换，参见 [主题](#theme)。 |
| **Change Password** | 修改密码、用户名和全名，见下文。 |
| **Logout** | 在此设备上退出登录。 |
| **Admin** | 仅管理员可用，参见 [管理功能](administration/index.md)。 |

<a id="changing-the-password"></a>

### 修改密码

**Change Password** 会打开一个对话框，其中包含 **Username**、**Full Name**、**Current Password**、**Password** 和 **Repeat password** 字段：

- 每次修改都必须输入**当前密码**（如果你的账户没有密码，则留空）；
- 要修改密码，请在两个密码栏中输入新密码；
- 若两个密码栏都留空，则只修改名称，保留原密码；
- 勾选 **Log out everywhere else (other browsers and all saved logins)**，可在不修改密码的情况下，让你的账户在所有其他设备上退出登录，见下文；
- 用户名不得与其他账户重复。

点击 **OK** 保存。如果当前密码输入错误，则不会保存任何内容。

修改密码会结束所有已保存的登录（*Save login*），包括本设备在内的每台设备都需要使用新密码重新登录。当前打开的其他会话（例如另一台设备上仍显示工作区的浏览器）也会一并结束，只有你正在使用的这一个会话会继续。如果设备丢失或密码泄露，修改密码即可让该设备退出登录。

**Log out everywhere else** 的效果与此相同，但不更改密码：你账户的所有已保存登录都会被遗忘，其他所有打开的会话都会结束。如果你曾在某处（例如别人的电脑上）忘记退出登录，可以使用它。与此对话框中的其他修改一样，它需要输入当前密码。你当前的会话会保留，但其已保存的登录也会被遗忘，因此本浏览器下次同样会要求输入密码。

<a id="language"></a>

### 语言

按钮上方的 **Language** 选择器可将用户界面切换为其他语言。该选择会保存在你的账户中：页面会以新语言重新加载，你打开的其他所有页面（包括其他设备上的页面）也会如此，并且下次登录时仍会使用该语言。登录界面本身始终使用默认语言。

列表中只包含已安装的语言。Marginalia 自带英语、日语、法语、西班牙语、德语、韩语和意大利语；管理员可以通过加载[扩展](extensions/index.md)添加更多语言，例如[中文本地化](extensions/chinese-localization.md)。某种语言未翻译的文本会以英文显示。随附的扩展自带日语、中文、法语、西班牙语、德语、韩语和意大利语的文本（加载中文本地化后会显示中文内容）。日期按该语言的习惯格式显示。你的书籍及其文本不会被翻译，只翻译界面。

![语言选择器](../images/language-selector.png)

![日语界面的工作区](../images/ui-japanese.png)

<a id="theme"></a>

### 主题

**Language** 选择器旁边的按钮可在深色主题（默认）和浅色主题之间切换用户界面。该选择会保存在你的账户中，下次在任何设备上登录时仍会使用；已在其他标签页中打开的页面需要重新加载后才会变化。图表和关系图在打开时会采用当前主题的配色。

![浅色主题下的工作区](../images/theme-light.png)

<a id="logging-in"></a>

### 登录

登录界面上的 **Save login** 会让你在本设备上保持登录 30 天（在服务器上使用时需要 HTTPS，参见 [服务器（Docker）](getting-started/docker-server.md#https-and-reverse-proxy)）。**Logout** 则会结束这一登录状态。

账户可以没有密码，此时只需输入用户名即可登录。如果管理员清除了你忘记的密码，请以这种方式登录，并立即使用 **Change Password** 设置新密码。这对于你自己电脑上的桌面应用很方便，但切勿在他人可以访问的服务器上这样做。

<a id="settings"></a>

## 设置

**Settings** 标签页保存你所有书籍的默认设置。

![设置标签页](../images/settings-defaults.png)

顶部有三个按钮：

- **Import Backup as New Book** - 从备份文件创建一本书，参见 [书籍备份](books/backups.md#importing-a-backup-as-a-new-book)；
- **Discard Changes** - 放弃未保存的更改，并重新加载已保存的设置；
- **Save** - 保存设置。

!!!info 未保存的更改
设置不会自动保存。一旦你修改任何字段（包括扩展设置），顶部就会出现 *Unsaved changes*，其他标签页、**Admin** 和 **Logout** 也会被锁定。点击 **Save** 或 **Discard Changes** 即可解锁。
!!!

<a id="general-settings"></a>

### 常规设置

| 字段 | |
|---|---|
| **Default Model** | 新书默认使用的[推理服务提供方](inference-providers.md)。 |
| **Default Protocol** | 新书默认使用的[协议](protocols.md)。 |
| **Default model for annotating**, **Default protocol for annotating** | [为某一部分添加批注](books/annotations.md#letting-a-model-annotate)时，本次运行中未选择其他模型时所用的模型。留空则使用该书的模型。 |
| **Backup Strategy** | 对于未设置自有策略的书籍，自动[书籍备份](books/backups.md#automatic-backups)的策略：*No Backups*、*After N Messages* 或 *After N Minutes*，并在其旁边填写数字。 |
| **Versions kept per part** | [历史记录](books/version-history.md#how-many-versions-are-kept)为你所有书籍保留每个部分文本的早期版本数量，除非文件夹、书籍或管理员设置了更严格的限制。留空表示此处不设上限；**0** 表示不保留历史版本。 |
| **Parts loaded at once in the story** | [故事](books/story-editor.md)和[阅读器](books/publishing.md)同时加载的书籍部分数量。故事从最后几个部分开始加载，阅读器则从你上次停止阅读的位置（或从前几个部分）开始；前后的部分会随滚动加载，远离你的部分会被再次卸载。留空则一次性加载所有部分。 |
| **Daily goal: words written**, **Daily goal: minutes of editing** | [写作统计](books/statistics.md#the-daily-goal)日历中的每日目标：你亲自撰写的字数和编辑分钟数，合计所有书籍。留空表示没有目标。 |
| **Record writing statistics**, **Keep statistics for (months)** | 是否统计你的[写作统计](books/statistics.md#privacy-clearing-not-recording-keeping-less)，以及保留多少个月的统计数据（留空或为 0 表示全部保留）。管理员可以为所有用户关闭记录，并设置更短的保留时间。 |

*Default Model* 和 *Default Protocol* 仅在创建书籍时使用；修改它们不会影响已有书籍。[Lore Generator](extensions/lore-generator.md) 扩展没有对应的书籍，它会使用这两项设置进行生成。备份策略适用于所有未设置 *Per manuscript override* 的书籍，从其下一个生成的部分开始生效。
<a id="templates"></a>

### 模板

[书籍提示词](books/prompts.md)的默认值：

| 字段 | |
|---|---|
| **Prompt language** | 内置提示词的语言：英语（默认）、日语、法语、西班牙语、德语、意大利语、韩语，或在加载[中文本地化](extensions/chinese-localization.md)后可用的中文。参见下文的[提示词语言](#prompt-language)。 |
| **Master Template** | 提示词的框架，参见[主模板](books/prompts.md#master-template)。 |
| **Point of View (POV)**, **Tense** | 叙述视角和时态。 |
| **Style** | 风格与语气指南。 |
| **Default User Prompt** | 每个部分的请求，参见[用户提示词](books/prompts.md#user-prompt)。 |
| **Default Summary Prompt** | 请求[摘要](books/summaries.md#the-summary-prompt)的提示词。 |
| **Default Meta Summary Prompt** | 请求[元摘要](books/summaries.md#the-meta-summary-prompt)的提示词，用于合并摘要。 |
| **Redraft System Prompt**, **Redraft Prompt** | 部分内容[重写](books/story-editor.md#redrafting-a-part)时的系统消息和最后一条消息，参见[重写提示词](books/prompts.md#redraft-prompt)。 |
| **Annotate System Prompt**, **Annotate Prompt** | 请求模型为某部分[添加批注](books/annotations.md#letting-a-model-annotate)时的系统消息和最后一条消息，参见[批注提示词](books/prompts.md#annotate-prompt)。 |
| **Annotate Instructions** | 模型添加批注时查找的内容，适用于未设置该项的书籍和文件夹，参见[批注提示词](books/prompts.md#annotate-prompt)。 |
| **Impersonate Prompt** | 请求模型代你[起草下一轮指令](books/story-editor.md#drafting-the-instructions)的提示词，参见[代拟提示词](books/prompts.md#impersonate-prompt)。 |
| **Compaction prompt of agents** | 仅适用于可以使用[智能体](agents.md)的用户。当你[`/compact`](agents.md#running-a-command-yourself)智能体时，若其自身的压缩提示词为空，则向它们发出的请求；此处为空时，使用管理员设置的提示词，否则使用内置提示词。 |
| **Instructions of agents (agents.md)** | 仅适用于可以使用[智能体](agents.md)的用户。为没有自身指令的智能体提供的指令，每次运行时都会加入其提示词。留空表示没有指令。 |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | 仅适用于拥有权限 [*Notifications*](administration/permissions.md) 的用户（不限于使用[智能体](agents.md)的用户），位于 *General* 标签页，且仅当管理员允许时可用。当智能体提出问题、等待你、完成或失败，而应用已关闭时，在此处设置发送简短消息的地址：参见[你不在时](agents.md#when-you-are-not-there)。 |
| **System prompt of agents** | 仅适用于可以使用[智能体](agents.md)的用户。当智能体自身的系统提示词为空时，告诉它们应扮演的角色；此处为空时，使用管理员设置的提示词，否则使用内置提示词。纯文本，不是模板。 |

![模板标签页](../images/settings-templates.png)

对于可以使用智能体的用户，下方还有智能体的提示词：

![模板标签页中的智能体提示词](../images/settings-templates-agents.png)

每个字段为空时会以灰色显示内置默认值，并带有提示弹出框，其中包含 **Insert Default Template** 以及可用的[变量和宏](templates/index.md)。

<a id="prompt-language"></a>

#### 提示词语言

**Prompt language** 用于选择内置提示词的语言：它决定空字段代表的内容（选择另一种语言后，灰色文字会立即改变），因此也决定发送给模型用于生成、摘要、重写、批注和代拟的提示词。拥有自身提示词的插件（Arc Outline、Beta Readers、Book Description Generator、Lore Generator、Manga Generator、SillyTavern Converter、Knowledge Tracker、Audiobook）以及智能体的提示词也遵循此设置。它与界面语言无关：你可以用英文界面，同时发送日语提示词。模型在故事所用语言中遵循指令的效果最好，因此请选择书籍所用的语言。只有内置提示词会改变：你自己编写的提示词（无论在此处，还是在某本书或文件夹中）保持不变。[智能体](agents.md)读取其提示词和通知时使用的是它自己的语言（智能体设置中的 *Language*），而不是此处的语言。

这些默认值会立即应用于**每一本自身对应字段为空的书籍**，而不仅限于新书。在书籍的 *Prompts* 标签页中设置了自身值的书籍会保留该值。因此：

- 在此处设置所有书籍共有的内容；
- 在某本书的 *Prompts* 标签页中设置该书特有的内容；
- 清空书籍的字段，使其恢复为 *Settings* 中的默认值。

<a id="styles"></a>

### 样式

这是书籍的文本样式：各部分的文字在故事和阅读器中的呈现方式。该标签页列出应用自带且无法修改的 **Normal** 和 **Book** 样式（标有 *built in*），以及你自己的样式。选择一种样式即可在右侧编辑；列表下方的按钮如下：

- **Duplicate**：以所选样式创建一个你自己的样式（也可以基于内置样式创建，这是开始定制的方式），名称为 *... (copy)*，之后你可以重命名并修改它；
- **Delete**：删除你自己的样式；内置样式无法删除；
- **Set as default**：将该样式设为书籍使用的默认样式，除非书籍在故事的设置菜单中[选择了其他样式](books/story-editor.md#text-styles)。默认样式在列表中标有 *default*。
- **Export** 和 **Import**：将所选样式保存为 `.style.json` 文件，或将此类文件中的样式添加到你的样式中（在保存设置之前不会存储）。参见[分享样式](books/styling-guide.md#share-a-style-export-and-import)。

[样式指南](books/styling-guide.md)逐一介绍了每个选项，并提供了可下载的示例样式。

![样式标签页，已选中样式指南中的 horror 样式](../images/settings-styles.png)

对于你自己的样式，右侧表单包含 **Name**、复选框 **For exports (not offered for books)** 以及两个标签页。样式要么用于书籍，要么用于导出：用于导出的样式会出现在[导出对话框](books/export.md#print-styles)中（应用自带四种，分别对应 HTML、EPUB、PDF 和 Word 导出的外观），且不能设为书籍的默认样式；用于书籍的样式不会出现在导出对话框中。列表中会以 *(export)* 标记它们。

- **Options**：按组排列：*Text*（字体、字号、行高、字母和词间距、颜色、对齐、连字符、段落之间的间距、段落的**首行缩进**，以及标题后的**首字下沉**）、*Page*（文本栏宽度、居中，以及填充文本后整个区域的**背景颜色**）、*Emphasis, links and code*（斜体和粗体文字以及链接的颜色、代码字体）、*Quotes*、*Headings*（字体、一级标题字号、颜色、对齐、小型大写字母），以及 *Scene break* 的符号和颜色（`---`）。每个输入框接受任意 CSS 值（`1.1rem`、`#336699`、`var(--lumo-body-text-color)`）；留空的输入框保持应用的外观。字体输入框提供常见的字体栈，也可以填写你自己的字体。
- **Custom CSS**：你自己的 CSS，添加在选项之后并应用于样式内部，因此你编写的规则仅作用于使用此样式的文本：`.chat-message-markdown p { text-indent: 1.5em !important; }` 用于缩进段落，普通声明（`font-size: 1.2rem;`）作用于整个文本。其下方的 **Generated CSS** 显示选项生成的样式表（只读）。花括号 `{ }` 必须成对匹配，否则样式无法保存。

![样式的 Custom CSS 标签页](../images/settings-styles-css.png)

表单下方的 **Preview** 以该样式显示示例文本，并在你输入时实时更新。

与其他标签页一样，样式标签页随设置的 **Save** 一同保存，**Discard Changes** 同样会放弃其中的更改：复制、编辑、删除以及设为默认，都只会改变工作副本，并且一旦进行其中任何操作，就会出现 *Unsaved changes*（以及锁定状态）。没有名称的样式，或花括号不匹配的自定义 CSS，在保存时会被拒绝。
<a id="exports"></a>

### 导出

各文件格式的导出设置。标签页为每个有可设置项的格式提供一个小节（**Plain text**、**Markdown**、**HTML**、**Word**、**PDF**、**EPUB**、**Markdown with front matter**、**Obsidian vault**；点击名称即可展开）。

- **Print style**（HTML、Word、PDF 和 EPUB，即带字体和颜色的格式）：[导出对话框](books/export.md)为该格式预先选中的样式。初始值为应用为该格式提供的外观（**HTML page**、**PDF book**……）。列表中包含[导出样式](#styles)，既有内置样式，也有您自己创建的样式。选择其他样式后，该格式每次导出都将使用此样式，直到您在某次导出的对话框中更改该选择为止。
- **Front matter**（Markdown with front matter 和 Obsidian vault）：位于文件开头的 YAML 块，其中包含 `{{title}}`、`{{author}}`、`{{language}}` 和 `{{exportDate}}`。留空即使用默认块。
- **Title page**（除面向其他工具的格式外的所有格式；Obsidian vault 则在书籍的笔记中设置）：标题页模板，这是一个输出 Markdown 的 Handlebars 模板，可使用 `{{title}}` 和 `{{author}}`（字段旁的信息图标会列出它们）。留空即使用默认模板：标题作为标题显示，作者以斜体显示在其下方。信息弹窗中的 **Fill default** 会以默认模板为起点开始编辑。
- **Authorship statement**（除 SillyTavern chat 外的所有格式）：导出时勾选 *Include authorship statement* 复选框后，在文末添加的文本，可使用 `{{title}}`、`{{author}}`、`{{totalWords}}`、`{{writtenWords}}`、`{{generatedWords}}`、`{{writtenPercent}}`、`{{generatedPercent}}` 和 `{{models}}`（模型之间以逗号分隔，若无模型则为空）。留空即使用默认文本。

这些是您自己的版本：书籍或文件夹可以拥有各自的版本，参见[标题页模板](books/export.md#title-page-template)。本标签页与其他标签页一样，通过设置中的 **Save** 保存，**Discard Changes** 则会放弃其中的更改。无效的模板在保存时会被拒绝。在 Styles 标签页中创建的样式，保存后即可在此处选用。

![导出标签页，已展开 HTML 小节](../images/settings-exports.png)

<a id="tags"></a>

### 标签

列出您所有的标签（即您为书籍、设定集和设定集条目添加的标签），并显示每个标签关联的对象数量。点击列标题可按该列排序。与其他标签页不同，此页的操作会立即生效：它不属于未保存的更改，也无需保存。

![标签标签页](../images/settings-tags.png)

| 按钮 | |
|---|---|
| **Rename**（铅笔） | 询问标签的新名称；所有对象都将以新名称保留该标签。 |
| **Delete**（垃圾桶） | 要求确认，然后删除该标签，并将其从所有带有该标签的对象中移除。 |

如果新名称与另一个标签的名称相同（比较时忽略大小写），重命名会使两个标签变得相同，因此系统会询问您是否要**合并**它们。合并会将所有带有被重命名标签的对象转移到已有标签上，并删除被重命名的标签；同时拥有两者的对象最终只保留一次已有标签。点击 Cancel 则不做任何更改，该标签保留原名。

![询问是否合并两个标签](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### 键盘快捷键

[故事编辑器快捷键](books/story-editor.md#keyboard-shortcuts)的按键（它们在电脑上的故事编辑器中生效，[手机布局](phone.md)没有快捷键）。该表列出每个操作（包括[扩展](extensions/index.md)添加的操作）及其按键，以及书籍列表中 [Organise](books/folders.md#organise) 窗口按钮的按键。故事设置菜单、某个部分的菜单以及其文本的右键菜单中的每一项，在此都是一个操作（大多数在您为其设置按键之前没有按键），菜单中会在该项旁显示其按键。

| 按钮 | |
|---|---|
| **Record** | 按下新的组合键（修饰键可以先按或与其他键同时按，例如 `Ctrl+Shift+K`）；在其他任何位置点击即取消。 |
| **Reset to default** | 将该操作恢复为默认按键。 |
| **Clear** | 该操作不设按键。 |

红色提示会指出两个操作共用的按键、浏览器自行保留的按键（`Ctrl+N`、`Ctrl+T`、`Ctrl+W` 等，页面永远无法接收这些按键），以及带字母的 `Alt`（在某些键盘上会输入字符，例如 macOS 上）。不带 `Ctrl`、`Alt` 或 `Meta` 的按键仅在没有任何文本框获得焦点时生效。按键与其他设置一样通过 **Save** 保存，并立即应用于已打开的故事。

![设置，键盘快捷键](../images/settings-shortcuts.png)

<a id="notifications"></a>

### 通知

模型完成工作时，浏览器会发出桌面通知，因此您可以在它工作时离开该标签页。该标签页有一个复选框 **Use desktop notifications**、下方的按钮，以及一份会发出通知的项目列表：

- **Story generation**、**Annotate with AI**、**Annotate with AI (automatic)**、**Redraft**、**Redraft (automatic)**、**Draft Instructions**、**Summary**、**Resummarize all summaries** 和 **Help assistant**，以及[扩展](extensions/index.md)添加的项目，这些项目以扩展名称命名（*Reviewer: generate a review*、*Lore Generator: generate lore*、*Image Generator: generate the image*、*Arc Outline: generate chapters*……）。标记为 *(automatic)* 的 *Manga Generator* 条目是其连续模式。
- 标记为 *(automatic)* 的条目是书籍自行启动的运行，即生成后的[自动批注](books/story-editor.md)以及随之而来的重新起草。它们与您自己打开的对话框分别设置：若勾选 **Annotate with AI** 而未勾选 **Annotate with AI (automatic)**，则自动批注不发出通知；若只勾选自动条目，则只有自动批注会发出通知。
- 以错误结束的运行同样会发出通知（*… failed*），而您自己中止的运行则不会。

该复选框为您的账户开启通知，并启用两个按钮。**Register this browser** 会向浏览器请求权限（由该次点击触发）；按钮下方的一行会显示此浏览器的状态：allowed、blocked（已阻止，请在浏览器的网站设置中允许）或 not asked yet。权限属于浏览器，而非您的账户：每个您希望接收通知的浏览器或设备都需注册一次，而复选框与列表在所有设备上都是一致的。**Send a test notification** 会立即显示一条测试通知，即使页面处于前台也一样。

**Filter** 字段按名称筛选列表，**Check shown** 和 **Uncheck shown** 作用于其中显示的条目，并保持其他条目不变。仅当您没有在看该页面时（例如在另一个标签页中，或窗口不在前台）才会显示通知；点击通知可将页面切换回来。

通知需要 `https://` 地址或 `localhost`：若使用网络中服务器的普通 `http://` 地址，浏览器不会提供通知，标签页会对此予以说明。浏览器休眠的标签页（通常在手机上）同样无法显示通知。本标签页与其他标签页一样，通过设置中的 **Save** 保存。

<a id="phrase-watch-list"></a>

### 短语监视列表

您的[短语监视列表](books/annotations.md#watching-for-phrases)，每本书都会使用它（文件夹和书籍可以添加各自的条目），以及其条目和文件夹、书籍条目所选用的**批注模板**：找到某个短语时，其批注所显示的内容、其类型，以及是否将其提供给模型。通过设置中的 **Save** 保存，与其他标签页相同。

![设置中的短语监视列表](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### API 密钥

供程序和脚本通过 HTTP API（`/api/v1`）使用 Marginalia 时所用的密钥。程序在请求头 `Authorization: Bearer <key>` 中发送密钥，并可以执行你在自己的书中能做的操作。仅限管理员和拥有 [*API access*](administration/permissions.md) 权限的用户使用；没有该权限时，标签页会提示说明。

![API 密钥标签页](../images/settings-api-keys.png)

![新密钥，仅显示一次](../images/api-key-created.png)

**New API key** 会要求填写 **Name**（使用该密钥的程序，便于之后知道该撤销哪一个）、**Access**，以及 **Expires**（有效天数，留空则永不过期）。访问权限可以是以下任意组合：

| Access | |
|---|---|
| **Read** | 你的书籍，以及只读取数据的命令。 |
| **Write** | 所有命令，包括修改或删除内容的命令；包含 *Read*。 |
| **Update the installation** | 仅限管理员：Marginalia 的[更新](administration/updates.md)（查看状态、查看日志、启动更新）。 |
| **Manage the extensions** | 仅限管理员：安装、更新、停止和删除[扩展](administration/extensions.md)。 |

每个密钥都可以读取 Marginalia 的版本号。只授予程序所需的权限：例如，安装插件的脚本只需要 *Manage the extensions*，而且这样的密钥无法访问你的书籍。当密钥的所有者不再是管理员时，该密钥的更新和扩展管理功能会停止工作；其他权限保持不变。

密钥只在创建后立即显示一次：请当场复制保存，因为 Marginalia 只保存其指纹，之后无法再次显示。列表会显示每个密钥的开头部分、创建时间、过期时间和最后使用时间。点击垃圾桶按钮即可**撤销**密钥，使用该密钥的程序会立即失去访问权限。密钥过期或管理员收回权限时，密钥也会失效。密钥在创建或撤销时即时保存，不随设置中的 **Save** 保存。

**API 页面**：在浏览器中打开你的 Marginalia 的 `/api/v1/`（例如 `https://marginalia.example.org/api/v1/`），即可查看每个调用、其参数和返回结果。在顶部的 **API key** 中粘贴密钥后，页面还会列出该密钥可以运行的每个命令及其参数；点击 **Try it out** → **Execute** 即可使用该密钥发送调用并显示结果。这些调用是真实的：命令会修改你的书籍，更新会重启服务器。页面只在标签页打开期间保留密钥，绝不会写入磁盘。

面向开发者的调用说明见[开发者手册](../developer/api.md)。

<a id="extension-settings"></a>

### 扩展设置

已安装[扩展](extensions/index.md)的设置，每个扩展一个分节（例如 *Reviewer Settings* 和 *SideQuery Settings*）。它们使用同一个 **Save** 按钮保存。未安装带有设置的扩展时，该标签页为空。

![带有 Lore Generator 设置的扩展设置标签页](../images/settings-extensions.png)
