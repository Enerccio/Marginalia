---
label: 账户与设置
order: 920
icon: person
---

# 账户与设置

你在 Marginalia 中创建的所有内容都归属于你的账户：书籍、设定集、标签、推理提供方、协议和设置。其他用户看不到这些内容（你[发布](books/publishing.md)的书籍除外），你也看不到他们的内容。

<a id="your-account"></a>

## 你的账户

工作区底部的按钮：

| 按钮 | |
|---|---|
| **Language** | 用户界面的语言，参见 [语言](#language)。 |
| 太阳 / 月亮按钮 | 位于语言按钮旁边：在深色和浅色主题之间切换，参见 [主题](#theme)。 |
| **Change Password** | 更改你的密码、用户名和全名，见下文。 |
| **Logout** | 在此设备上退出登录。 |
| **Admin** | 仅限管理员，参见[管理](administration/index.md)。 |

<a id="changing-the-password"></a>

### 更改密码

点击 **Change Password** 会打开一个对话框，其中包含 **Username**、**Full Name**、**Current Password**、**Password** 和 **Repeat password**：

- 每次更改都要输入你的**当前密码**（如果你的账户没有密码，则留空）；
- 要更改密码，请在两个密码栏中输入新密码；
- 如果只想更改名称并保留密码，请将两个密码栏都留空；
- 勾选 **Log out everywhere else (other browsers and all saved logins)**，可以让你的账户在其他所有设备上退出登录，而不更改密码，见下文；
- 用户名不得与其他账户重复。

点击 **OK** 保存。如果当前密码输入错误，则不会保存任何内容。

更改密码会结束所有已保存的登录（*Save login*），包括在所有设备上，也包括当前这台设备：他们必须使用新密码重新登录。你当前打开的其他会话（在其他设备上仍显示工作区的浏览器）也会被结束；只有你正在使用的那个会话会保留。如果某台设备丢失或你的密码泄露，更改密码即可让该设备退出登录。

**Log out everywhere else** 的效果相同，但不需要设置新密码：你账户的每一个已保存登录都会被遗忘，每个其他打开的会话也都会结束。如果你在某处（例如一台不属于你的电脑）忘记了退出登录，就可以使用它。与此对话框中的其他更改一样，它需要你的当前密码。你当前的会话会保留，但它的已保存登录也会被遗忘，因此下次此浏览器同样会要求输入密码。

<a id="language"></a>

### 语言

**Language** 选择器位于按钮上方，用于将用户界面切换为其他语言。该选择会保存在你的账户中：页面会以新语言重新加载，你打开的其他每个页面（包括其他设备上的页面）也会如此，并且下次登录时会继续使用该语言。登录界面本身始终使用默认语言。

列表中只包含已安装的语言。英语、日语、法语、西班牙语、德语、韩语和意大利语随 Marginalia 一起提供；管理员可以通过加载[扩展](extensions/index.md)来添加更多语言，例如[中文本地化](extensions/chinese-localization.md)。某种语言未翻译的文本会以英文显示。随附的扩展提供日语、中文、法语、西班牙语、德语、韩语和意大利语的文本（加载中文本地化后会显示中文）。日期会按该语言的习惯格式书写。你的书籍及其文本不会被翻译，只翻译界面。

![Language 选择器](../images/language-selector.png)

![日语界面中的工作区](../images/ui-japanese.png)

<a id="theme"></a>

### 主题

**Language** 选择器旁边的按钮可在深色主题（默认）和浅色主题之间切换用户界面。该选择会保存在你的账户中，下次在任何设备上登录时都会使用。已经在其他标签页中打开的页面，在重新加载后才会切换主题。图表和关系图在打开时会采用当前主题的颜色。

![浅色主题下的工作区](../images/theme-light.png)

<a id="logging-in"></a>

### 登录

登录界面上的 **Save login** 可使你在此设备上保持登录 30 天（在服务器上需要 HTTPS，参见 [服务器（Docker）](getting-started/docker-server.md#https-and-reverse-proxy)）。点击 **Logout** 即可结束登录。

账户可以没有密码；此时你只需输入用户名即可登录。如果管理员清除了你忘记的密码，请用这种方式登录，并立即使用 **Change Password** 设置新密码。这种方式适合在自己的电脑上使用桌面应用，但绝不要在他人可以访问的服务器上这样做。

<a id="settings"></a>

## 设置

**Settings** 标签页保存你所有书籍的默认值。

![Settings 标签页](../images/settings-defaults.png)

顶部有三个按钮：

- **Import Backup as New Book**：从备份文件创建一本书，参见[书籍备份](books/backups.md#importing-a-backup-as-a-new-book)；
- **Discard Changes**：放弃未保存的更改，并重新加载已保存的设置；
- **Save**：保存设置。

!!!info 未保存的更改
设置不会自动保存。一旦你更改任何字段（包括扩展设置），顶部就会出现 *Unsaved changes*，其他标签页、**Admin** 和 **Logout** 都会被锁定。点击 **Save** 或 **Discard Changes** 即可解除锁定。
!!!

<a id="general-settings"></a>

### 通用设置

| 字段 | |
|---|---|
| **Default Model** | 新书默认使用的[推理提供方](inference-providers.md)。 |
| **Default Protocol** | 新书默认使用的[协议](protocols.md)。 |
| **Default model for annotating**、**Default protocol for annotating** | 当你在运行时没有选择其他模型时，[为某个部分添加批注](books/annotations.md#letting-a-model-annotate)所使用的模型。留空则使用书籍的模型。 |
| **Backup Strategy** | 对于没有自身策略的书籍，自动进行[书籍备份](books/backups.md#automatic-backups)：*No Backups*、*After N Messages* 或 *After N Minutes*，旁边填写数字。 |
| **Versions kept per part** | [历史记录](books/version-history.md#how-many-versions-are-kept)为你所有书籍中每个部分的文本保留多少个早期版本，除非文件夹、书籍或管理员设置了更严格的限制。留空表示此处不设限制；**0** 表示不保留历史。 |
| **Parts loaded at once in the story** | [故事](books/story-editor.md)和[阅读器](books/publishing.md)同时加载一本书的多少个部分。故事从最后几个部分开始加载，阅读器则从你上次停止阅读的位置（或从前几个部分）开始；前后的部分会随着你滚动而加载，远离你的部分则会被再次卸载。留空则一次性加载所有部分。 |
| **Daily goal: words written**、**Daily goal: minutes of editing** | [写作统计](books/statistics.md#the-daily-goal)日历中每天的目标：你亲自写下的字数和编辑所花的分钟数，统计所有书籍。留空表示没有目标。 |
| **Record writing statistics**、**Keep statistics for (months)** | 是否统计你的[写作统计](books/statistics.md#privacy-clearing-not-recording-keeping-less)，以及保留多少个月的统计数据（留空或为 0：全部保留）。管理员可以为所有人关闭记录功能，并设置更短的保留时间。 |

*Default Model* 和 *Default Protocol* 仅在创建书籍时使用；更改它们不会影响已有的书籍。[Lore Generator](extensions/lore-generator.md) 扩展没有对应的书籍，它会使用这两项设置来生成内容。备份策略适用于所有没有*按稿件覆盖（Per manuscript override）*设置的书籍，从其下一个生成的部分开始生效。
<a id="templates"></a>

### 模板

[书籍提示词](books/prompts.md)的默认值:

| 字段 | |
|---|---|
| **Prompt language** | 内置提示词的语言:英语(默认)、日语、法语、西班牙语、德语、意大利语、韩语,以及加载了[中文本地化](extensions/chinese-localization.md)时的中文。参见下文的[提示词语言](#prompt-language)。 |
| **Master Template** | 提示词的框架,参见[主模板](books/prompts.md#master-template)。 |
| **Point of View (POV)**、**Tense** | 叙述视角和时态。 |
| **Style** | 文风和语气指导。 |
| **Default User Prompt** | 每个部分的请求,参见[用户提示词](books/prompts.md#user-prompt)。 |
| **Default Summary Prompt** | [摘要](books/summaries.md#the-summary-prompt)的请求。 |
| **Default Meta Summary Prompt** | [元摘要](books/summaries.md#the-meta-summary-prompt)的请求,用于合并摘要。 |
| **Redraft System Prompt**、**Redraft Prompt** | 部分[重写](books/story-editor.md#redrafting-a-part)的系统消息和最后一条消息,参见[重写提示词](books/prompts.md#redraft-prompt)。 |
| **Annotate System Prompt**、**Annotate Prompt** | 请求模型为某个部分[添加批注](books/annotations.md#letting-a-model-annotate)时的系统消息和最后一条消息,参见[批注提示词](books/prompts.md#annotate-prompt)。 |
| **Annotate Instructions** | 模型添加批注时关注的内容,用于未设置此项的书籍和文件夹,参见[批注提示词](books/prompts.md#annotate-prompt)。 |
| **Impersonate Prompt** | 请求模型代你[草拟下一轮的指令](books/story-editor.md#drafting-the-instructions)的提示词,参见[代入提示词](books/prompts.md#impersonate-prompt)。 |
| **Compaction prompt of agents** | 仅适用于可以使用[智能体](agents.md)的用户。当你对智能体执行 [`/compact`](agents.md#running-a-command-yourself) 时,向它们提出的要求,前提是其自身的压缩提示词为空;此处为空时,使用管理员设置的提示词,若管理员也未设置,则使用内置的提示词。 |
| **Instructions of agents (agents.md)** | 仅适用于可以使用[智能体](agents.md)的用户。为没有自身指令的智能体添加的指令,每次运行时都会加入其提示词。为空则不添加。 |
| **Message to a URL when an agent needs you**、**Format of the message**、**Also when the application is open**、**Send a test message** | 仅适用于拥有权限 [*Notifications*](administration/permissions.md) 的用户(不限于使用[智能体](agents.md)的用户),位于 *General* 标签页中,且仅当管理员允许时可用。当智能体提出问题、等待你、完成或失败,而你已关闭应用时,用于发送简短消息的地址:参见[你不在时](agents.md#when-you-are-not-there)。 |
| **System prompt of agents** | 仅适用于可以使用[智能体](agents.md)的用户。当智能体自身的系统提示词为空时,告诉它们应扮演什么角色;此处为空时,使用管理员设置的提示词,若管理员也未设置,则使用内置的提示词。纯文本,不是模板。 |

![模板标签页](../images/settings-templates.png)

再往下,对于可以使用智能体的用户,还有智能体的提示词:

![模板标签页中的智能体提示词](../images/settings-templates-agents.png)

每个字段在为空时以灰色显示内置默认值,并带有提示弹出框,其中有 **Insert Default Template** 以及可用的[变量和宏](templates/index.md)。

<a id="prompt-language"></a>

#### 提示词语言

**Prompt language** 用于选择内置提示词的语言:即空字段所代表的内容(选择其他语言后,灰色文字会立即变化),因此也决定了发送给模型用于生成、摘要、重写、批注和代入的内容。插件中自带提示词的部分(Arc Outline、Beta Readers、Book Description Generator、Lore Generator、Manga Generator、SillyTavern Converter、Knowledge Tracker、Audiobook)以及智能体的提示词也遵循此设置。它与界面语言互不影响:你可以用英文界面发送日语提示词。模型在故事所用的语言中最能理解指令,因此请选择你的书籍所用的语言。只有内置提示词会改变:你自己编写的提示词,无论是在此处还是在某本书或某个文件夹中,都保持不变。[智能体](agents.md)读取其提示词和提示时使用的是它自己的语言(智能体设置中的 *Language*),而不是此处的语言。

这些默认值会立即应用于**每一本自身字段为空的书籍**,而不仅仅是新建的书籍。在书籍的 *Prompts* 标签页中设置了自己的值的书籍会保留该值。因此:

- 把所有书籍共用的设置放在这里;
- 把某本书特有的设置放在它自己的 *Prompts* 标签页中;
- 清空某本书的字段,即可使其恢复为 *Settings* 中的默认值。

<a id="styles"></a>

### 文风

你的书籍的文字样式:段落文字在故事和阅读器中的呈现方式。此标签页列出了应用自带且不可更改的 **Normal** 和 **Book** 样式(标有 *built in*),以及你自己的样式。选择一个样式即可在右侧编辑。列表下方的按钮如下:

- **Duplicate**:基于所选样式创建你自己的样式(对内置样式同样可用,这是开始自定义的方式),名称为 *...(copy)*,之后可以重命名和修改;
- **Delete**:删除你自己的样式;内置样式不能删除;
- **Set as default**:将该样式设为书籍的默认样式,除非书籍在故事设置菜单中[选择了其他样式](books/story-editor.md#text-styles)。默认样式在列表中标有 *default*。
- **Export** 和 **Import**:将所选样式保存为 `.style.json` 文件,或将此类文件中的样式添加到你自己的样式中(在保存设置之前不会存储)。参见[分享样式](books/styling-guide.md#share-a-style-export-and-import)。

[文风指南](books/styling-guide.md)逐项介绍了每个选项,并提供可下载的示例样式。

![文风标签页,已选中文风指南中的恐怖样式](../images/settings-styles.png)

对于你自己的样式,右侧的表单包含 **Name**、复选框 **For exports (not offered for books)** 以及两个标签页。样式要么用于书籍,要么用于导出:用于导出的样式会出现在[导出对话框](books/export.md#print-styles)中(应用自带四种,分别对应 HTML、EPUB、PDF 和 Word 导出的外观),且不能设为书籍的默认样式;用于书籍的样式则不会出现在导出对话框中。列表中会以 *(export)* 标注用于导出的样式。

- **Options**:按组排列。*Text*(字体、字号、行高、字母和单词间距、颜色、对齐、连字符、段落间距、段落的**首行缩进**,以及标题后的**首字下沉**);*Page*(文字栏宽度、居中,以及填满文字背后整个区域的**背景颜色**);*Emphasis, links and code*(斜体、粗体文字和链接的颜色,以及代码字体);*Quotes*;*Headings*(字体、一级标题字号、颜色、对齐、小型大写字母);以及*Scene break*(`---`)的符号和颜色。每个输入框可填写任意 CSS 值(如 `1.1rem`、`#336699`、`var(--lumo-body-text-color)`);留空则保持应用本身的外观。字体输入框提供常用字体组合,也可以填写你自己的字体。
- **Custom CSS**:你自己的 CSS,添加在选项之后,并仅在该样式内生效,因此你编写的规则只作用于使用此样式的文字。例如 `.chat-message-markdown p { text-indent: 1.5em !important; }` 会为段落加上缩进;普通声明(如 `font-size: 1.2rem;`)则作用于整个文字。其下方的 **Generated CSS** 显示由选项生成的样式表(只读)。花括号 `{ }` 必须成对,否则样式无法保存。

![样式的 Custom CSS 标签页](../images/settings-styles-css.png)

表单下方的 **Preview** 会以该样式显示示例文字,并在你输入时实时更新。

与其他标签页一样,文风标签页随设置的 **Save** 一同保存,**Discard Changes** 也会一并放弃其中的更改:复制、编辑、删除和设为默认都只改变工作副本,只要进行了其中任何一项操作,*Unsaved changes*(以及锁定标记)就会出现。保存时,若样式没有名称,或其自定义 CSS 的花括号不成对,则会被拒绝。
<a id="exports"></a>

### 导出

各文件格式的导出设置。该标签页中，每种有可设置项的格式都有一个区块（**Plain text**、**Markdown**、**HTML**、**Word**、**PDF**、**EPUB**、**Markdown with front matter**、**Obsidian vault**，点击名称即可展开）。

- **Print style**（HTML、Word、PDF 和 EPUB，即带字体和颜色的格式）：[导出对话框](books/export.md)为该格式预先选中的样式。初始值是应用为该格式提供的外观（**HTML page**、**PDF book**……）。列表中包含[导出样式](#styles)，既有内置样式，也有您自己的样式。选择另一种样式后，该格式每次导出都会使用它，直到您在对话框中为某次导出更改选择为止。
- **Front matter**（Markdown with front matter 和 Obsidian vault）：文件开头的 YAML 块，可使用 `{{title}}`、`{{author}}`、`{{language}}` 和 `{{exportDate}}`。留空即为默认块。
- **Title page**（除用于其他工具的格式外的所有格式；Obsidian vault 在书籍的笔记中设置标题页）：标题页的模板，是一个写 Markdown 的 Handlebars 模板，可使用 `{{title}}` 和 `{{author}}`（字段旁的信息图标会列出它们）。留空即为默认模板：标题作为一级标题，作者以斜体显示在其下方。在信息弹出框中点击 **Fill default**，可从默认模板开始编辑。
- **Authorship statement**（除 SillyTavern chat 外的所有格式）：导出时勾选 *Include authorship statement* 复选框，会在文末添加这段文字。可使用 `{{title}}`、`{{author}}`、`{{totalWords}}`、`{{writtenWords}}`、`{{generatedWords}}`、`{{writtenPercent}}`、`{{generatedPercent}}` 和 `{{models}}`（模型之间以逗号分隔，没有模型时为空）。留空即为默认文字。

这些都是您自己的版本。某本书或某个文件夹可以有自己的版本，参见[标题页模板](books/export.md#title-page-template)。该标签页与其他标签页一样，通过设置中的 **Save** 保存，**Discard Changes** 会放弃其中的更改。无效的模板在保存时会被拒绝。您在样式标签页中创建的样式，保存后即可在此处选用。

![导出标签页，已展开 HTML 区块](../images/settings-exports.png)

<a id="tags"></a>

### 标签

您所有标签（即您赋予书籍、lorebook 和 lorebook 条目的标签）的表格，显示每个标签所关联的对象数量。点击列标题可按该列排序。与其他标签页不同，它立即生效：不属于未保存的更改，也无需保存。

![标签标签页](../images/settings-tags.png)

| 按钮 | |
|---|---|
| **Rename**（铅笔） | 询问标签的新名称；每个对象都会以新名称保留该标签。 |
| **Delete**（垃圾桶） | 询问确认，然后删除该标签，并将其从所有带有它的对象上移除。 |

如果新名称与另一个标签的名称相同（比较名称时不区分大小写），重命名会让两个标签变成同一个，因此会询问您是否要**merge**它们。合并会把所有带有被重命名标签的内容移到已有标签上，并删除被重命名的标签；同时拥有两个标签的对象只会保留一次已有标签。选择取消则什么都不会改变，标签保留原名。

![询问是否合并两个标签](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### 键盘快捷键

[故事编辑器快捷键](books/story-editor.md#keyboard-shortcuts)的按键（在电脑上的故事编辑器中生效，[手机布局](phone.md)没有快捷键）。该表列出每个操作（包括[扩展](extensions/index.md)添加的操作）及其按键，并列出书籍列表的 [Organise](books/folders.md#organise) 窗口中按钮的按键。故事设置菜单、段落菜单以及段落正文的右键菜单中的每一项，在这里都是一个操作（大多数在您设置之前没有按键），菜单中会在该项旁显示按键。

| 按钮 | |
|---|---|
| **Record** | 按下新的组合键（修饰键可以先按，也可以与其他键同时按，例如 `Ctrl+Shift+K`）；点击其他任何地方则取消。 |
| **Reset to default** | 将该操作恢复为默认按键。 |
| **Clear** | 该操作没有按键。 |

红色提示会标出两个操作共用的按键、浏览器自己保留的按键（`Ctrl+N`、`Ctrl+T`、`Ctrl+W` 等，页面永远收不到它们），以及带字母的 `Alt`（在某些键盘上，例如 macOS，它会输入字符）。不带 `Ctrl`、`Alt` 或 `Meta` 的按键，仅在没有文本框获得焦点时才生效。按键与其他设置一样通过 **Save** 保存，并立即应用于已打开的故事。

![设置，键盘快捷键](../images/settings-shortcuts.png)

<a id="notifications"></a>

### 通知

模型完成工作时，浏览器会发出桌面通知，让您在它工作时可以离开该标签页。该标签页有一个复选框 **Use desktop notifications**、下方的按钮，以及一个通知项目列表：

- **Story generation**、**Annotate with AI**、**Annotate with AI (automatic)**、**Redraft**、**Redraft (automatic)**、**Draft Instructions**、**Summary**、**Resummarize all summaries** 和 **Help assistant**，以及[扩展](extensions/index.md)添加的项目，这些项目以扩展的名称命名（*Reviewer: generate a review*、*Lore Generator: generate lore*、*Image Generator: generate the image*、*Arc Outline: generate chapters*……）。*Manga Generator* 中标有 *(automatic)* 的项目是它的连续模式。
- 标有 *(automatic)* 的项目是书籍自行启动的运行，即一次生成之后的[自动批注](books/story-editor.md)，以及随之而来的重写。它们与您自己打开的对话框分开选择：勾选 **Annotate with AI** 而不勾选 **Annotate with AI (automatic)** 时，自动批注不发出通知；只勾选自动项时，只有自动批注会发出通知。
- 以错误结束的运行也会发出通知（*… failed*），您自己停止的运行则不会。

复选框为您的账户开启通知，并启用两个按钮。**Register this browser** 会向浏览器请求权限（从这次点击开始）；按钮下方的一行文字说明本浏览器的状态：已允许、已阻止（请在浏览器的网站设置中允许），或尚未询问。该权限属于浏览器，而不属于您的账户：您希望接收通知的每个浏览器或设备都需要注册一次，而复选框和列表在所有地方都相同。**Send a test notification** 会立即显示一条测试通知，即使页面在前台也会显示。

**Filter** 输入框按名称缩小列表范围，**Check shown** 和 **Uncheck shown** 只作用于它显示出来的项目，其他项目保持不变。只有当您没有在看该页面时（在另一个标签页中，或窗口不在前台），才会显示通知；点击通知即可把页面调回前台。

通知需要 `https://` 地址或 `localhost`：在您网络中某台服务器的普通 `http://` 地址上，浏览器不会提供通知，标签页会说明这一点。被浏览器休眠的标签页（通常在手机上）也无法显示通知。该标签页与其他标签页一样，通过设置中的 **Save** 保存。

<a id="extension-settings"></a>

### 扩展设置

已安装[扩展](extensions/index.md)的设置，每个扩展一个区块（例如 *Reviewer Settings* 和 *SideQuery Settings*）。它们使用同一个 **Save** 按钮保存。没有安装带设置项的扩展时，该标签页为空。

![扩展设置标签页，显示 Lore Generator 的设置](../images/settings-extensions.png)
