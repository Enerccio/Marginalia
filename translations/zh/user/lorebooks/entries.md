---
label: 条目与激活
order: 90
---

# 条目与激活

<a id="the-entry-list"></a>

## 条目列表

世界书编辑器的每一行都是一个条目：

| 列 | |
|---|---|
| 复选框 | **Enabled**。已禁用的条目永远不会被使用。 |
| **Entry Name** | 条目的名称，由你自己决定。不会发送给模型。 |
| **Order** | 条目在提示词中的位置，参见[顺序与插入](#order-and-insertion)。新条目的值为 `100`。 |
| **Tags** | 用来激活该条目的标签，参见[标签](#tags)。 |
| ⌄ | 显示条目的详细信息。 |
| expand | 在[全屏编辑器](lorebooks.md#editing-in-full-screen)中打开该条目（桌面版）。 |
| trash | 删除该条目（确认后）。 |

工具栏中的 **Add Entry** 会添加一个顺序为 `100` 的条目。修改会在你输入时自动保存。

<a id="searching-and-deleting-many-entries"></a>

### 搜索和批量删除条目

按钮旁边带放大镜的搜索框只显示名称、内容、备注或过滤器中含有所输入文字的条目（不区分大小写）；清空它即可再次看到所有条目。它只查找当前所选世界书中的条目，不包括子世界书。手机布局中，搜索框位于列表上方。

搜索框旁边还有两个用来限制列表的框。**Tags** 显示带有该标签的条目（列出的是世界书中至少一个条目拥有的标签；*排除*标签不在此列）。**Status** 显示 **All**、**Enabled**、**Disabled** 或 **Constant (no keys)**——后者指已启用且没有过滤器的条目，因此会用于每个部分。搜索、标签和状态可以组合使用：条目必须同时符合所有条件。手机布局中，这两个框位于搜索框下方。

左侧的复选框列用于选择条目（表头中的复选框会选中所有显示的条目，因此先搜索再使用表头复选框，即可选中所有匹配项）。选中条目后，工具栏中会出现 **Delete selected** 按钮并显示所选条目的数量；它会先询问一次，然后把条目移到[回收站](../trash.md)。[批量编辑](lorebooks.md#mass-edit)中也有一个 **Delete checked** 按钮。在手机上，条目的 **⋮** 菜单中有 **Select entries**：条目前会出现复选框，并且出现 **Delete selected** 按钮，显示已勾选的数量（那里每个条目也有自己的 **Delete entry** 按钮）。


<a id="search-and-replace"></a>

## 搜索和替换

**Search and replace**（位于 **Add Entry** 右侧）可一次性替换世界书中所有条目的名称、正文和注释中的某个词、名字或短语。它与[故事的搜索和替换](../books/story-editor.md#search-and-replace)类似，但没有摘要和分支的选项：**Search** 会列出含有匹配内容的条目（表格显示其名称），以及条目中的匹配内容（当前内容和替换后的内容）；**Replace** 则执行替换。它有以下选项：**Search by regex**（参见[正则表达式](../regular-expressions.md)）和 **Case sensitive**（默认开启；开启正则时该选项会被锁定，此时可以使用 `(?i)` 忽略大小写）。已禁用的条目同样会被搜索。在手机上，该按钮（位于 *Enabled* 复选框旁边的图标）会以手机布局打开同一窗口。

![世界书的搜索和替换窗口，展开了一个条目中的匹配内容](../../images/lorebook-search-replace.png)

故事编辑器的命令行中有 `/search-replace-lore-entries`，用于当前打开的书的世界书，参见[命令](../commands/index.md#commands-that-come-with-marginalia)。

<a id="entry-details"></a>

## 条目详情

⌄ 按钮会打开详细信息：

![带有详细信息的条目](../../images/lorebook-entry.png)

| 字段 | |
|---|---|
| **Content** | 写入提示词的文本。它是一个[模板](#templates-in-entries)：可以使用变量和宏。 |
| **Filtering Mode** | **Keywords (any)**、**Keywords (all)**、**Text** 或 **Regex**，决定 *Filtering Text/Pattern* 如何匹配。 |
| **Filtering Text/Pattern** | 过滤器，参见[过滤器](#filters)。留空表示没有过滤器。 |
| **Insertion Mode** | **In Lore Block** 或 **Before User Prompt**，参见[顺序与插入](#order-and-insertion)。 |
| **Activation Context** | 过滤器在哪里查找：**Instructions**（该部分的请求，默认值）或 **Instructions and parts of the story**，参见[查看故事内容](#looking-into-the-story)。 |
| **Parts** | 当选择 *Instructions and parts of the story* 时：与请求一起查找的故事最新部分的数量，至少为 1。 |
| **From message** | 条目仅从该编号的部分开始使用，参见[将条目限制在部分内容中](#limiting-an-entry-to-some-parts)。留空：从头开始。 |
| **To message** | 条目仅使用到该编号的部分为止。留空：直到结尾。 |
| **Condition** | 一个模板，其结果必须为真，条目才会被使用，参见[条目的条件](#a-condition-for-an-entry)。留空：没有条件。 |
| **Negative Tags (Exclusion)** | 用来关闭条目的标签，参见[标签](#tags)。 |
| **Also search the summaries** | 勾选后，过滤器也会在故事迄今为止的[摘要](../books/summaries.md)中查找，即下一次请求会发送的那些摘要（与故事内容不再匹配的摘要会被排除）。需要设置过滤器。 |
| **Do not search its content for other entries** | 勾选后，该条目内容中的词语不会引入其他条目，参见[被其他条目引入的条目](#entries-brought-in-by-other-entries)。 |
| **Only through other entries** | 勾选后，该条目永远不会因请求或故事本身而被使用；只有当它的过滤器在另一个已被使用的条目的内容中被找到时才会使用，参见[被其他条目引入的条目](#entries-brought-in-by-other-entries)。 |
| **Note** | 你的备注，不会发送给模型。从 SillyTavern 导入的条目会在此列出无法导入的设置。 |

按照你希望模型阅读的方式撰写内容——简短、客观的描述效果最好：

> **Captain Varro** —— 奥斯特拉港口守卫队的指挥官，五十多岁，左手有伤疤。效忠公爵，不信任法师公会。说话简短利落。
<a id="activation"></a>

## 激活

在生成每个部分之前，Marginalia 会遍历该书的世界书条目以及[子世界书](lorebooks.md#sub-lorebooks)中的条目。当满足以下条件时，条目会被**激活**：

1. 该条目已启用，且其所属世界书也已启用；
2. 如果条目设有范围，则正在编写的部分位于其部分范围内（**From message**、**To message**）；
3. 它的**负标签**都不是本书的标签；
4. 它没有**标签**，或其中至少一个标签是本书的标签；
5. 如果它有**条件**，则该条件为真，参见[条目的条件](#a-condition-for-an-entry)；
6. 它没有**过滤器**，或过滤器与该部分的指令相匹配（或者在 **Activation Context** 设为*指令和故事部分*时，与指令以及故事的最新部分相匹配）。

所有条件都必须成立。没有标签和过滤器的条目始终处于激活状态。

<a id="tags"></a>

### 标签

标签通过**书籍标签**（在书籍的 [About](../books/managing-books.md#the-about-tab) 选项卡中设置）将条目与书籍关联起来。
利用标签，可以让多本需要世界书不同部分的书籍共用同一本世界书：

| 条目标签 | 负标签 | 在标有以下标签的书籍中激活 |
|---|---|---|
| - | - | 始终 |
| `winter` | - | `winter` |
| `winter`、`book-2` | - | `winter` **或** `book-2` |
| - | `book-1` | 除 `book-1` 以外的所有书籍 |
| `winter` | `book-1` | `winter`，但如果书籍同时带有 `book-1` 则不激活 |

标签必须完全匹配。负标签始终优先于标签。

标签也可以赋予整本世界书（[世界书](lorebooks.md)的 **Tags** 字段；从文件导入的世界书可能自带标签）。这些标签会被视为其中每个条目的标签。

<a id="filters"></a>

### 过滤器

过滤器只有在**该部分的请求**中提到了条目时才会激活它。过滤器匹配的对象是该部分发送时的[用户提示词](../books/prompts.md#user-prompt)——也就是你输入的 *Scene Setting*、*POV Character*、*Present Characters* 和 *Instructions*，它们位于用户提示词模板的措辞之中。

| 模式 | 匹配条件 |
|---|---|
| **Keywords (any)** | 过滤器是以逗号分隔的关键词列表（如 `Varro, Ostra`）；请求中至少包含其中一个关键词，忽略大小写。关键词两侧的空格会被忽略。 |
| **Keywords (all)** | 同样的列表，但请求必须包含全部关键词。 |
| **Text** | 该文本出现在请求的任何位置，忽略大小写。`varro` 可以匹配 *Captain Varro*。 |
| **Regex** | [正则表达式](../regular-expressions.md#in-a-lorebook-entry-filter)在请求的任何位置匹配，忽略大小写，并可跨行匹配。 |

例如，正则 `\b(varro|captain)\b` 可以匹配完整单词 *Varro* 或 *Captain*；而 `varro.*(harbor|docks)` 只有在 Varro 与 harbor 或 docks 都被提及，且顺序如此时才会匹配。

!!!warning 默认情况下，过滤器看的是请求，而不是故事
除非条目会查看故事（参见[在故事中查找](#looking-into-the-story)），过滤器只检查新部分的指令，而不检查之前部分的文本。关于 Varro 的条目，会在你正在编写的部分的场景、角色或指令中提到 Varro 时被激活——当他在场景中出现时，请把他写进 *Present Characters*。

用户提示词模板本身也包含在请求中。如果过滤器的关键词与模板使用的词相同（默认模板中提到了 *characters*、*plot*、*tone* 等），该条目将始终处于激活状态。
!!!

无效的正则表达式永远不会匹配，条目保持未激活状态。

当某个条目只应在部分书籍中使用，并且只在相关时才使用时，可以结合标签和过滤器。

<a id="looking-into-the-story"></a>

### 在故事中查找

默认情况下，过滤器只在该部分的请求中查找，因此关于龙的条目只会在*你*提到龙时被使用。当故事应当让该条目持续生效时——龙出现在上一部分，而你的新指令只写了"它飞走了"——可以将该条目的 **Activation Context** 设为 **Instructions and parts of the story**，并将 **Parts** 设为用于与请求一同搜索的最新部分数（`1` 表示正在编写部分之前的那一部分，`2` 表示之前的两部分，以此类推）。

- 过滤器会在这些部分的文本和请求中一起查找，因此 **Keywords (all)** 的关键词可以分散在它们之间。请求始终计入。
- 只搜索部分的文本，不搜索它们的摘要；并且只搜索你正在编写的分支中的部分（重新生成或 swipe 时，不会搜索被替换的那个部分）。
- 如果故事比 **Parts** 短，则整个故事都会被搜索。Marginalia 只读取已启用的条目所需数量的部分，因此没有此类条目的世界书根本不会读取故事。
- 通过[其他条目](#entries-brought-in-by-other-entries)引入的条目不会在故事中查找，只会在已使用的条目内容中查找。

<a id="limiting-an-entry-to-some-parts"></a>

### 将条目限制在部分范围内

有些设定只在故事的某一段中成立：例如某个角色的伪装在第 5 章被揭穿，或战争开始前的谣言。请在 **From message** 和/或 **To message** 中填写部分的编号，编号与故事大纲中的一致（第一部分为 `1`；两端的编号都包含在内）。此时，只有当正在编写的部分的编号落在该范围内时，条目才会被使用，无论其过滤器如何设置——没有过滤器的条目在范围内即为常驻条目。

- 空字段表示没有限制：只填 **From message** 为 `10` 表示"从第 10 部分起"，只填 **To message** 为 `4` 表示"到第 4 部分为止"。
- 正在编写的部分是分支中最后一个部分之后的那一个（一个 9 部分的故事会编写第 `10` 部分）。swipe 或重新生成时，编写的是它所替换的部分。
- 没有文本的部分同样计入，与大纲中的计数方式一致。删除或插入部分会改变编号。
- 如果 **From message** 大于 **To message**，该条目永远不会被使用。
- 超出范围的条目也不会被[其他条目](#entries-brought-in-by-other-entries)引入。
- [Lorebook VCS](../extensions/index.md) 扩展会在条目的各个版本中保留该范围；世界书的导入、导出和克隆同样会保留该范围。

<a id="a-condition-for-an-entry"></a>

### 条目的条件

**Condition** 是一个模板，书写方式与[条目内容](#templates-in-entries)相同，Marginalia 会在每个部分之前渲染它。只有当结果为**真**时，条目才会被使用：除空结果、`0`、`false`、`no` 和 `off`（不论大小写）之外的任何结果都为真。它看到的内容与条目内容相同：视角角色、在场角色、场景、本轮指令、迄今为止的故事以及变量。

![一个带条件的条目：只有当 Tomáš 是视角角色且 Mizuki 在场时，该条目才会被使用](../../images/lorebook-condition.png)

| 条件 | 条目被使用的时机 |
|---|---|
| `{%{{{#if presentCharacters}}yes{{/if}}}%}` | 本轮有在场角色 |
| `{%{{{eval::povCharacter == "Fiora"}}}%}` | 视角角色是 Fiora（参见[表达式](../templates/macros.md#expressions)） |
| `{%{{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}}%}` | Fiora 是视角角色，且 Mara 在场 |
| `{%{{{getvar::revealed}}}%}` | 变量 `revealed` 有值（由更早的部分设置，参见[宏参考](../templates/macros.md#variables)） |
| `{%{{{command::authors-note}}}%}` | [命令](../commands/index.md)返回了内容（只能使用为宏设计的命令） |

工作方式如下：

- 它在检查启用开关、部分范围和标签之后进行检查，并且**早于过滤器**：条件为假的条目即使过滤器匹配也不会被使用，并且也不会被[其他条目](#entries-brought-in-by-other-entries)引入。
- 无法判定的条件视为假，并且[预览](#preview-activation)会说明原因：模板无效、使用了不存在的名称（拼写错误）、使用了不存在或不能用于宏的命令，或者时间已耗尽。在你输入时，前两种情况的字段会变红。
- 条件只读取，不写入。条件中的 `{%{{{setvar}}}%}` 不会改变任何内容，无论是对提示词还是对其他条目而言；并且只能使用只读的命令。
- 每个条目和每个部分只渲染一次，因此 `{%{{{random::yes::no}}}%}` 不会在第一遍与扫描深度的各遍之间改变结果。
- [预览](#preview-activation)会列出条件为假的条目：*Not used: condition is false*。

  ![激活预览：Mizuki 的条目未被使用，因为其条件为假](../../images/lorebook-condition-preview.png)
- 同一部分的所有条件共享 **60 秒**。时间用完后，剩余的条件视为假（*condition failed*，超时）。正在运行的条件不会被中断，时间只在下一个条件开始前检查。
- 世界书的导入和导出、克隆以及 Lorebook VCS 扩展都会保留条件。

<a id="entries-brought-in-by-other-entries"></a>

### 由其他条目引入的条目

通常只有该部分的请求才会激活条目。当设置了**扫描深度**（[世界书](lorebooks.md#the-lorebook-editor)中的字段；默认值 `0` 表示关闭此功能）时，已激活条目的内容会被搜索，查找未激活条目的过滤器，就像它是请求的一部分一样。过滤器被找到的条目也会被激活。这就是**第 1 遍**；在第 2 遍中，又会搜索第 1 遍所激活条目的内容，依此类推，遍数等于扫描深度（最多 5 遍）。

例如，扫描深度为 `1` 时，由请求激活了条目 *Captain Varro*（过滤器为 `varro`），其内容写着"……效忠于 Ostra 公爵"。条目 *Ostra*（过滤器为 `ostra`）随之也会被激活，尽管请求中从未提到这座城市。若扫描深度为 `2`，条目 *Ostra* 又会引入它所提到的港口条目。

- 只有**过滤器**会计入：因为没有过滤器而已经被使用的条目无需任何引入；被关闭或被标签排除的条目不会被引入。
- 被搜索的是条目按原样书写的文本，其中的模板不会被执行。
- 书籍所使用的世界书的**扫描深度**，同时作用于它自己的条目以及其子世界书的条目。子世界书可以有自己的深度（其设置中的 **Scan depth as a sub lorebook**；留空则使用书籍所用世界书的深度）：其条目只有在不超过该深度的遍数中才会被其他条目的内容找到，`0` 则从不找到。实际进行的遍数等于最大深度。子世界书的字段只在它作为子世界书时生效；当书籍直接使用它时，则采用其普通的 **Scan depth**。
- 勾选了 **Do not search its content for other entries** 的条目可以被激活，但不会引入其他任何条目。
- 勾选了 **Only through other entries** 的条目永远不会被请求找到：只有当某个已使用条目的内容中包含其过滤器时，它才会被使用。若没有扫描深度，或没有过滤器，它永远不会被使用；[预览](#preview-activation)会显示 *Not used: used only through the content of other entries*。
- 提示词中的顺序不取决于条目是如何被找到的，参见[顺序与插入](#order-and-insertion)。
- 改变被使用条目的扩展（参见[扩展](../extensions/index.md)）同样会被询问关于这些被引入条目的问题。
<a id="preview-activation"></a>

## 预览激活

**预览激活**（生成按钮工具栏中 lorebook 编辑器里的眼睛按钮，以及手机上搜索和替换按钮旁边的同一按钮）用于回答"这段文字会用到哪些条目？"，而无需生成任何内容。在 **Text of the turn** 中输入请求的文字（场景、角色和指令，与实际输入时一致），表格会按使用顺序列出 lorebook 及其子 lorebook 的每一个条目：

- 绿色对勾表示会被使用的条目，灰色标记表示其他条目；
- 条目所属的 lorebook、它的 **Order**、**Pass**（由文字激活的条目为空，由[其他条目带入](#entries-brought-in-by-other-entries)的条目为 `1`、`2`……）；
- **Reason**：无过滤器、在文字中找到过滤器、在其他条目的内容中找到过滤器（并附上过滤器和该条目），或未被使用的原因：已关闭、所在部分超出其范围、该书的否定标签、该书缺少该条目的标签、条件为假或无法判定、未找到过滤器。

标签同样会影响结果，因此可以在 **Tags of the book** 中选择一本书以使用其标签（不选则不计入任何标签）。该书还提供[查看故事](#looking-into-the-story)的条目所需的故事部分（不选则只搜索文字），以及[限定到某些部分](#limiting-an-entry-to-some-parts)的条目所需的下一部分编号（不选则不检查其范围）。你输入的文字即为[条件](#a-condition-for-an-entry)所用的本轮指令；若选择了书，则同时使用其变量、视角、当前出场角色以及最后一部分的场景。条件中的命令在预览中不会运行。文字下方的一行会说明有多少条目会被使用，以及大约多少 token 的设定内容。预览使用该 lorebook 的扫描深度。

在[全屏编辑器](lorebooks.md#editing-in-full-screen)中，每个条目标签页的名称行中也有眼睛按钮：点击后预览会针对该条目打开，其所在行被选中，表格上方的一行会说明它是否会被使用以及原因，并随你的输入实时变化。修改标签页中的过滤器后，在预览中按 **Refresh** 即可查看效果。

![lorebook 的预览激活](../../images/lorebook-preview-activation.png)

预览只应用 Marginalia 的规则：所用文字是你输入的内容（不包括其外围的用户提示模板），而[扩展](../extensions/index.md)在生成某部分时所做的改动不包含在内。

<a id="order-and-insertion"></a>

## 顺序与插入

被激活的条目按 **Order** 放入提示中，数值最小的排在最前。Order 相同的条目保持创建的先后顺序。所有被包含的 lorebook 中的条目会被一起排序。

**Insertion Mode** 决定条目的位置：

| 模式 | 位置 |
|---|---|
| **In Lore Block** | 放入[主模板](../books/prompts.md#master-template)中的 `{%{{{backgroundLore}}}%}` 块——默认模板中为 *World & lore context*，位于系统消息里。用于背景知识。 |
| **Before User Prompt** | 紧接在该部分的请求之前，位于提示的末尾。模型最关注这一位置。请谨慎使用，只用于必须影响这一部分的内容（例如角色的当前状态，或场景的规则）。 |

条目之间用空行分隔。设定内容计入[上下文上限](../protocols.md#limits)；如果条目很多且很长，能放入提示的故事就会变少。

<a id="templates-in-entries"></a>

## 条目中的模板

条目的内容是一个模板，与[书籍提示](../books/prompts.md#templates)相同。它可以使用：

| 变量 | |
|---|---|
| `{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}`、`{%{{{instructions}}}%}` | 该部分的指令。 |
| `{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}` | 书籍的叙事设置。 |

以及所有[宏](../templates/macros.md)。例如，一个会根据视角角色而变化的条目：

{%{
```handlebars
Varro {{#povCharacter}}is seen through the eyes of {{.}}: {{/povCharacter}}a stern, scarred officer.
```
}%}

同一部分中所有被激活的条目按激活顺序共享其变量：某个条目可以用 `setvar` 设置一个值，排在后面的条目（以及主模板）则用 `getvar` 读取它。

内容中的语法错误会显示在字段下方。条目仍会保存；在修正之前，其内容会作为纯文本使用，不处理模板。

<a id="tips"></a>

## 提示

- 条目要简短、以事实为准；模型不需要散文，它需要的是事实。
- 每个角色、地点或概念一个条目，使它们可以各自被单独激活。
- 对世界的核心内容使用始终激活的条目，对角色群体和地点使用过滤器，并用标签让多本书共享同一个 lorebook。
- 在已生成的部分上使用 **Show Prompt** 检查哪些内容被激活，或在写作之前用[预览激活](#preview-activation)试用一段文字。
- 谨慎使用扫描深度：大多数书一轮就够了，而每多一轮都可能带入你不想要的设定。
