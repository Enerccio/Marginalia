---
label: 样式指南
order: 54
---

# 样式指南：让你的书看起来就是你的书

**文本样式**决定你的章节文字的外观：字体、颜色、栏宽、标题，以及场景之间的分隔线。本指南从第一处修改讲到完成的外观，最后给出一个可下载的完整示例：一个恐怖小说的样式，用到了所有选项，并附带一些自定义 CSS。

样式的使用位置：

| 位置 | 发生的事 |
|---|---|
| **故事编辑器** | 全书的样式（或你的默认样式）决定每个章节的外观。参见 [文本样式](story-editor.md#text-styles)。 |
| **阅读器** | [已发布的书](publishing.md) 会以*读者*从自己的样式中选择的样式阅读（在读者选择之前，使用“书籍样式”）。 |
| **导出** | 标记为 **用于导出** 的样式在[导出对话框](export.md#print-styles)中选择。HTML 和 EPUB 使用样式表，PDF 和 Word 使用数值设置（参见[各导出格式采用的内容](#what-the-exports-take)）。 |

<a id="make-your-first-style"></a>

## 制作你的第一个样式

1. 打开 **设置 → 样式**（[样式标签页](../account.md#styles)）。
2. 在列表中选择 **Book**，然后按 **Duplicate**。内置样式无法修改，因此你总是从名为 *Book (copy)* 的副本开始。给它起个名字。
3. 在 **Options** 标签页中修改几个选项。表单下方的 **Preview** 会显示一段示例文字（章节标题、强调、引文、场景分隔线和一个较小的标题），并随你的每次输入即时更新。
4. 按下设置的 **Save**。在此之前不会保存任何内容，**Discard Changes** 会放弃所做的修改。
5. 在故事编辑器中打开设置菜单（⚙）→ **Appearance** → **Change Styles For Text**，并勾选你的样式。它会保存在这本书中。若想让所有书都使用该样式，请在列表中选中它，然后按 **Set as default**。

![故事菜单中的样式](../../images/story-style-menu.png)

留空的选项会保留应用程序的外观，因此样式只需说明你想改变的部分。所有选项都留空的样式就是普通外观。

<a id="the-options"></a>

## 选项

每个输入框都接受一个 CSS 值。你会用到的单位：`rem`（页面文字的大小，1rem 通常为 16px）、`em`（当前文字的大小）、`px`、`%`，颜色可以写成 `#336699`、`rgb(51, 102, 153)` 或 `ivory` 之类的名称。

<a id="text"></a>

### 文本

| 选项 | 作用 | 示例 |
|---|---|---|
| **Font family** | 文字的字体。可以写一个列表：电脑上第一个存在的字体会被使用，最后一个最好是通用字体族（`serif`、`sans-serif`、`monospace`）。 | `"Garamond", "Georgia", serif` |
| **Font size** | 文字大小。 | `1.1rem` |
| **Line height** | 行距。裸数字表示字体大小的倍数。 | `1.7` |
| **Letter spacing**、**Word spacing** | 字母之间、单词之间的额外间距。取值较小：少量的字母间距会让文字显得疏朗。 | `0.01em`、`0.05em` |
| **Text color** | 文字颜色。 | `#cfc6bb` |
| **Text align** | `left`、`right`、`center` 或 `justify`。 | `justify` |
| **Hyphenation** | 在行尾断开较长的单词。它使用书籍[语言](managing-books.md#the-about-tab)的词典，因此请先设置语言。与 *justify* 搭配效果最佳。 | 开启 |
| **Paragraph spacing** | 段落下方的间距。书籍通常间距很小，若使用首行缩进则通常没有段间距。 | `0.3em` |
| **First line indent** | 缩进段落的首行，类似印刷书籍，但标题或场景分隔线之后除外。 | `1.5em` |
| **Drop cap** | 标题之后第一段的首字母放大。 | 开启 |

<a id="page"></a>

### 页面

| 选项 | 作用 | 示例 |
|---|---|---|
| **Text width** | 居中的、具有自身宽度的一栏。每行 60 到 75 个字符最易阅读，约为 `34em` 到 `40em`。 | `38em` |
| **Background color** | 文字背后的颜色；它会填满整个区域，而不仅仅是文字块。请选择在其上清晰可读的文字颜色。 | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### 强调、链接与代码

| 选项 | 作用 |
|---|---|
| **Emphasis color** | *斜体*文字的颜色。 |
| **Strong color** | **粗体**文字的颜色。 |
| **Link color** | 链接的颜色。 |
| **Code font family** | `代码` 和代码块的字体。 |

<a id="quotes"></a>

### 引文

| 选项 | 作用 |
|---|---|
| **Quote color** | 引文（`> 一行文字`）的文字颜色。 |
| **Quote border color** | 引文一侧竖线的颜色。 |

<a id="headings"></a>

### 标题

| 选项 | 作用 |
|---|---|
| **Heading font family** | `#`、`##` 和 `###` 标题的字体。 |
| **Heading size** | 第一级标题的大小；较低级别的标题按比例缩小。 |
| **Heading color** | 标题的颜色。 |
| **Heading align** | `left`、`center` 或 `right`。 |
| **Heading small caps** | 以小型大写字母书写标题。 |

<a id="scene-break"></a>

### 场景分隔线

文本中的一行 `---` 表示场景之间的分隔。默认显示为三个点。**Scene break text** 是用来代替它显示的内容（`* * *`、`~`、`† † †`），**Scene break color** 是它的颜色。

<a id="custom-css"></a>

## 自定义 CSS

选项涵盖了常见的需求。其余部分可使用 **Custom CSS** 标签页输入你自己的 CSS，它会添加在所有内容之后，且只在该样式内部生效。有两种写法：

- **纯声明**应用于容纳文字的整个区域：

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **带选择器的规则**。可以在前面加上 `&`（代表样式区域），也可以不加：两种写法都只作用于带有此样式的文字。

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

章节的文字位于带有 `chat-message-markdown` 类的元素中，因此针对文字中某个元素的选择器都从这里开始。CSS 框下方的 **Generated CSS** 字段显示选项生成的样式表：复制其中的选择器，即可了解你能覆盖哪些内容。

| 你想修改 | 选择器（位于 `& .chat-message-markdown` 之后） |
|---|---|
| 段落 | `p` |
| 标题 | `h1`、`h2`、`h3` |
| 斜体、粗体 | `em`、`strong` |
| 链接 | `a`、`a:hover` |
| 引文 | `blockquote` |
| 列表 | `ul`、`ol`、`li` |
| 代码 | `code`（行内）、`pre`（代码块） |
| 场景分隔线 | `hr`、`hr::after`（符号） |
| 段落的首行 | `p::first-line` |
| 选中的文字 | `::selection` |

需要注意的事项：

- **花括号 `{ }` 必须配对**，否则保存时样式会被拒绝。注释 `/* ... */` 可以使用。
- 应用程序所需的一些规则带有 `!important`（文字永远不会超出其区域宽度，长单词会换行，段落保留其换行）。你的 CSS 无法覆盖这些规则；除此之外的一切都可以覆盖，因为你的 CSS 排在最后。
- CSS 只能改变内容的外观，不能改变内容本身。它无法作用于文字周围的工具栏和标签页。
- 自定义 CSS 用于故事、阅读器和 HTML 导出。EPUB、PDF 和 Word 不支持它。

<a id="what-the-exports-take"></a>

## 各导出格式采用的内容

用于导出的样式在[导出对话框](export.md#print-styles)中选择；**Duplicate** 一个样式并勾选 **For exports**，或从应用程序自带的四个样式之一开始。依赖应用程序主题的颜色（深色主题的浅色文字）在导出时会变成适用于白色页面的深色。

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| 字体、字号、行高、间距、对齐、首行缩进 | ✓ | ✓ | ✓ | ✓ |
| 文字、强调、粗体、引文、标题、场景分隔线的颜色 | ✓ | ✓ | ✓ | ✓ |
| 背景颜色 | ✓ | ✓ | ✓ | ✓（页面颜色） |
| 栏宽、首字母放大、连字符、标题的小型大写字母 | ✓ | ✓ | | |
| 自定义 CSS | ✓ | | | |

电子书阅读器通常会使用它们自己的字体、字号和颜色，而不理会样式表的设置。PDF 和 Word 只能使用该格式支持的字体：PDF 会从你写的字体列表中选择衬线、无衬线或等宽字体；Word 会使用列表中的第一个字体名，若电脑上没有该字体，则显示它自己的字体。
<a id="share-a-style-export-and-import"></a>

## 分享样式：导出与导入

在样式列表下方，**导出**会将所选样式保存为文件（`name.style.json`；内置样式也可以导出），**导入**则读取此类文件。导入的样式会作为新样式添加到你自己的样式中，在你点击**保存**之前不会被存储，并保留其名称，因此如果你已有相似名称的样式，请先重命名。

该文件为 JSON 格式：

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

文件可以包含多个样式，你也可以手动编写。选项名称使用大写字母（`FONT_SIZE`、`TEXT_COLOR`、`SCENE_BREAK_TEXT` 等）；[恐怖风格示例](#an-example-a-horror-book)包含了所有选项。当前版本的 Marginalia 不认识的选项会被忽略（并会告知你数量），来自更新格式版本的文件会被拒绝，自定义 CSS 中花括号不匹配的样式同样会被拒绝。

<a id="an-example-a-horror-book"></a>

## 示例：恐怖小说

[下载恐怖风格](../../files/horror-book.style.json)（`horror-book.style.json`），在*设置 → 样式*中导入并选中，即可在预览中看到效果。它设置了**每一个选项**，并用自定义 CSS 处理选项无法覆盖的部分：

| | |
|---|---|
| **页面** | 近乎纯黑的背景 `#0c0a0a`，宽度为 `38em` 的版心，以及向边缘收紧的暗色晕影（自定义 CSS）。 |
| **正文** | Garamond 字体，字号 `1.12rem`，行高 `1.8`，两端对齐并启用连字符，首行缩进 `1.6em`，段落之间几乎没有间距，宛如印刷的小说。正文为旧纸般的灰色 `#cfc6bb`，每个标题之后带有首字下沉。 |
| **强调** | 斜体为暗淡的玫瑰色 `#b99a94`，粗体为血红色 `#b3261e`。 |
| **链接** | 病态的绿色 `#8fa07e`，鼠标悬停时变为红色并带波浪下划线（自定义 CSS）。 |
| **引文** | 灰色文字，配以深红色竖线 `#5c1010`，背后有淡淡的红色污渍（自定义 CSS）。 |
| **标题** | Palatino 小型大写字母，居中，深红色 `#8e1b17`，带有淡淡的光晕（自定义 CSS）。 |
| **场景分隔** | 深红色的 `† † †`；列表项用十字代替圆点（自定义 CSS）。 |
| **代码** | Courier New 字体，背景近乎纯黑。 |

![恐怖风格的故事](../../images/story-style-horror.png)

要在真实文本上试用，请将下面的内容粘贴到某个部分中（或者预览会显示类似的示例）：

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

然后可以修改它：将背景改为 `#14100c` 以获得烛光下的棕色，或将**正文宽度**设为 `32em` 以获得更窄的页面，又或者删除自定义 CSS，看看单独的选项会带来什么效果。找到满意的效果后，点击**导出**，并将文件与你的书稿放在一起保存。
