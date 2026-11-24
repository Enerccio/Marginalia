---
label: 章节大纲
order: 24
---

# 章节大纲

侧边栏中的书籍规划：卷（arc）、每卷下的章节，以及每个条目的状态；另外还有一个可以为你编写卷和章节的模型。大纲不属于故事提示词的一部分，生成器以及[旁问（Side Query）](side-query.md)在你要求时可以使用它。

<a id="the-arcs-tab"></a>

## 「卷」标签页

[故事编辑器](../books/story-editor.md)的左侧边栏在 *Outline* 旁边有一个 **Arcs** 标签页。点击 **Add new arc** 会打开卷的对话框：

| 字段 | |
|---|---|
| 标题 | 必填。 |
| 描述 | 这一卷中发生的事。 |
| 备注（私密） | 写给自己的笔记。 |
| 状态 | **Planned**（计划中）、**Current**（进行中）或 **Finished**（已完成）。 |
| 关联部分 | 卷或章节所属的故事部分，见[下文](#linking-to-the-story)。 |

章节具有相同的字段。列表将每一卷显示为一张卡片，其章节缩进排列在卡片下方。卡片的颜色随状态变化：计划中为灰色，进行中的卷为蓝色，已完成的为绿色且标题变暗。卡片上的按钮：

| 按钮 | |
|---|---|
| 日历 / 播放 / 勾选 | 状态；点击后打开菜单以更改状态。 |
| 箭头符号（卷） | 隐藏或显示该卷的章节。 |
| 箭头符号（带备注的章节） | 在描述下方显示或隐藏章节的备注，无需打开对话框即可阅读。 |
| 箭头 | 上移或下移卷或章节。 |
| 加号（卷） | 在卷的末尾添加一个章节。 |
| 魔杖（卷） | 请求模型为该卷编写章节，见[下文](#generating-chapters)。 |
| 图钉 | 将卷或章节关联（固定）到故事中最显眼的部分，见[下文](#linking-to-the-story)。 |
| 靶心（已关联部分的卷和章节） | 将故事滚动到其关联的部分。 |
| 笔 | 在对话框中打开。 |
| 垃圾桶 | 删除（卷连同其所有章节一并删除），删除前需确认。 |
| 机器人（生成的卷和章节） | 显示模型所写的内容，见[下文](#generating-an-arc)。 |

顶部魔杖旁边有一个带羽毛的笔：它会根据当前章节起草下一轮的指令，见[下文](#drafting-the-instructions-for-the-current-chapter)。

![「卷」标签页：一个已完成、一个进行中和一个计划中的卷](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### 关联到故事

卷或章节可以**关联到故事的某个部分**，这样你就可以从规划直接跳转到正文中的对应位置。对话框中有 **Linked part** 字段：即该部分在当前分支中的位置，从 1 开始编号，与 *Outline* 标签页中显示的编号一致（`#12`）。你可以直接输入，也可以点击旁边的 **link** 按钮，取当时故事中**最显眼**的部分（先将故事滚动到目标位置，再打开对话框）；**target** 按钮会将故事滚动到该字段中所指的部分。已关联的卷或章节卡片上也有靶心按钮。卡片上的**图钉**与链接按钮作用相同，只是无需打开对话框：将故事滚动到章节开头处，然后按下即可。链接记录的是分支中的位置，因此如果其前面的部分被删除或插入，链接会指向另一个部分；届时请手动更改。从故事导入时会为你自动关联章节，见[下文](#importing-an-arc-from-the-story)。

<a id="chapter-headings-in-the-text"></a>

### 正文中的章节标题

顶部魔杖菜单中的 **Chapter headings in the text**（正文中的章节标题）会在每个章节[关联](#linking-to-the-story)的部分开头写入一个标题，例如 `# Chapter 4: The Storm`，使故事的正文包含其章节结构（Chapter Marker 扩展和导出功能会识别这些标题）。对话框会随着你的更改列出将要写入的标题及其所在部分：

| 选项 | |
|---|---|
| Arcs get headings too（卷也加标题） | 关闭：只有章节获得标题（`#`），卷不写入。开启：每一卷都获得标题（`#`），其章节则低一级（`##`）。未直接关联的卷会放到其第一个已关联章节所在的部分。 |
| Prefix of the arcs / of the chapters（卷 / 章节的前缀） | 数字前的词语，初始为 *Part* 和 *Chapter*。留空则只显示 `4. The Storm`。 |
| Numbers（编号） | 阿拉伯数字（1、2、3）、罗马数字（I、II、III）或无编号。章节按其在大纲中的位置编号，无论是否已关联，因此尚未写作的章节会保留其编号。 |
| Number the chapters from 1 in every arc（每卷内的章节从 1 开始编号） | 否则章节在全书范围内连续编号。 |
| Put the titles into the headings（将标题文字放入标题） | 关闭时仅显示 `Chapter 4`。 |
| Remove every other heading of the story first（先删除故事中的其他所有标题） | 写入新标题前，删除活动分支中的每一行标题，适用于上次运行后章节被关联到其他位置的情况。 |
| Keep the summaries valid（保持摘要有效） | 初始为开启。标题不会改变发生的事件，因此在更改之前描述故事的摘要会重新标记为有效（已过时的摘要仍保持过时）。 |

**Write the headings**（写入标题）会将标题放入正文；部分原有的标题会被替换，因此在大纲更改后再次运行不会重复添加。**Remove all headings**（删除所有标题）在确认后会删除活动分支文本中的每一行标题。每个被改动的部分都会在其历史记录中保留旧文本。已完成或进行中但未关联的章节，以及超出活动分支末尾的链接，都不会获得标题，并会被列出报告。这些选项会被记住。

<a id="the-note-of-a-chapter"></a>

### 章节的备注

带有备注的章节在右侧显示一个箭头符号。点击它会在列表中展开描述下方的备注，这样你无需打开对话框即可阅读章节的详细信息（例如模型为其编写的内容）；再次点击则隐藏。哪些备注处于展开状态不会被保存。

![显示备注的章节](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### 在卷之间移动章节

将章节从其所在卷的第一位上移，它会移到前一卷的末尾；从最后一位下移，则会移到后一卷的开头。按住箭头可以将章节移动到你需要的位置。

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### 为当前章节起草指令

*Add new arc* 旁边带羽毛的笔会打开一个窗口，让模型[起草指令](../books/story-editor.md#drafting-the-instructions)用于下一轮，并以故事所处的章节数据作为**故事规划（Plan of the Story）**。当前章节是第一个标记为 **Current** 的章节；若没有，则为第一个未标记为 **Finished** 的章节；尚无章节但标记为当前的卷则单独使用。模型会获得该卷和章节的描述与备注，以及下一章的标题和描述（不含备注），并附带故事尚未抵达该章的提示。窗口会以你在新一轮面板的 *Instructions* 中输入的内容作为指导；**Use as Instructions**（用作指令）会将草稿放入该面板并将其打开。若没有当前章节，按钮会说明这一点。

同样的数据也是[旁问（Side Query）](side-query.md)的来源之一，见[下文](#the-outline-as-a-source-for-other-extensions)。

<a id="finishing-an-arc"></a>

### 完成一卷

将一卷标记为 **Finished** 会同时将其所有章节标记为 **Finished**。将卷改为其他状态时，其章节保持不变。

大纲会在你更改后立即随书籍一起保存，因此也包含在书籍的备份中。

<a id="the-outline-as-a-source-for-other-extensions"></a>

## 大纲作为其他扩展的数据来源

大纲作为有关本书的数据来源提供给其他扩展：在[旁问（Side Query）](side-query.md)中，于插件弹出框里勾选 **Arc outline**，即可将卷、其状态、描述和章节随你的问题一起发送。共有两个版本：**Arc outline** 为精简版（仅描述），**Arc outline (with notes)** 会在每个卷和章节的描述下方添加其备注，生成卷和章节的详细内容就保存在这里。只有勾选第二项时，你的私密备注才会被发送。第三个来源 **Arc outline: current chapter** 仅发送故事所处的卷和章节（参见[为当前章节起草指令](#drafting-the-instructions-for-the-current-chapter)）。
<a id="generating-an-arc"></a>

## 生成弧线

紧挨 **Add new arc** 的魔杖按钮会打开一个包含三项的菜单：**Generate an arc** 让模型写出一条新弧线；**Import from story** 会总结你已经写好的内容，参见[下文](#importing-an-arc-from-the-story)；**Develop the plot** 是与模型就故事走向进行的一次对话，参见[下文](#developing-the-plot-in-a-conversation)。

![魔杖菜单](../../images/plugin-arcoutline-menu.png)

**Generate an arc** 的对话框分为两部分。

左侧：

| | |
|---|---|
| Prompt | 提供给模型的指令，包含三个变量：`{{backgroundLore}}`（本书设定集及其子设定集的条目）、`{{existingArcs}}`（你的弧线和章节，包括它们的状态和描述，但不包括你的私人笔记）和 `{{specification}}`。它的初始内容来自 *Settings → Extension Settings → Arc Outline* 中的提示词（如果你没有修改过，即为默认提示词）；你在这里所做的编辑只对本次生成有效。 |
| Specification | 你希望弧线具备什么：主题、角色、故事应导向何处。可以留空。 |
| Tools | 仅当安装了[模型可用的工具套件](tools.md)时才出现（人名、地名）：模型可以使用哪些套件（及其中的哪些类别）来代替凭空编造，与 [Lore Generator](lore-generator.md) 中的设置相同。 |
| Model, Protocols | 起始值为本书的模型和协议（若本书没有设置，则为你在 *Settings* 中的默认设置）。更改它们可仅为本次生成使用其他模型；此选择不会被保存。 |
| Generate | 开始运行模型。运行期间，该按钮变为红色的 **Stop**，对话框中的其他内容全部禁用。 |

右侧会显示模型写出的**推理过程**；一旦答案开始输出，还会显示模型提议的**弧线**，包括名称、简短描述和较长的备注。弧线以大纲中的样子呈现（备注位于描述下方），并随着答案的流式输出不断增长。大纲显示的是描述，因此模型被要求将描述控制在几句话之内，并把细节放入备注中，备注会成为保存后弧线的 **Note**。模型完成后，其答案会被检查；如果它不是有效的弧线（例如缺少备注），对话框会提示，且该弧线无法保存。

![正在生成弧线](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** 会将弧线添加到大纲末尾。若想重试，再次按 **Generate** 即可，提示词或说明可以保持不变，也可以修改；之前的答案会被替换。

<a id="importing-an-arc-from-the-story"></a>

## 从故事中导入弧线

魔杖菜单中的 **Import from story** 会让模型阅读你故事的一部分，并将其连同章节整理为一条弧线，这样你就能把已经写好的书导入大纲。模型总是读取本书的[当前分支](../books/branches-and-story-tree.md)，即你打开对话框时的状态。该对话框与弧线生成对话框相似，左侧有以下选项：

| | |
|---|---|
| From message, To message | 要发送的故事范围。分支中有文本的消息从 1 开始编号；初始时选择整个分支。每个字段下方显示其所指消息的开头。 |
| Number of chapters (optional) | 选择一个数字即可获得恰好这么多章节（数量不符的答案会被拒绝）；留空则让模型按故事自然的划分方式分章。 |
| Link the chapters to the parts of the story | 初始为开启。发送范围内的消息从 1 开始编号，模型会说明每个章节从哪条消息开始；章节会[链接](#linking-to-the-story)到故事中的对应部分（编号依据整个分支，因此无论发送范围如何都是正确的），弧线则链接到其第一章开始所在的部分。如果答案提到了一条未被发送的消息，该章节将不会被链接。 |
| State of the imported arc and its chapters | 初始为 **Finished**，因为故事已经写完；如果需要，可选择 **Current** 或 **Planned**。 |
| Token counter | 该范围包含多少条消息及其大约的 token 数，以及整个提示词（指令、设定、大纲和故事）的大约 token 数，并与模型的承载上限相比较。当提示词超出上限时会变为警告，此时请选择更少的消息。它会随范围、章节数、提示词和模型而变化。 |
| Prompt | 模型的指令，包含变量 `{{backgroundLore}}`、`{{existingArcs}}`、`{{story}}`（发送范围内的消息）和 `{{numberOfChapters}}`。初始内容来自 *Settings → Extension Settings → Arc Outline* 中的 **Story import prompt**。 |

![从故事导入弧线](../../images/plugin-arcoutline-import.png)

这里没有工具，人名都来自故事本身。右侧依次显示推理过程，以及模型逐步写出的弧线和章节。**Save as arc** 会将弧线连同其章节添加到大纲末尾；每个对象都保留了模型的推理过程和答案，并各自带有一个机器人按钮。篇幅较长的故事最好分几次导入，每个范围导入一条弧线。
<a id="developing-the-plot-in-a-conversation"></a>

## 在对话中发展情节

魔杖菜单中的 **Develop the plot** 会打开一个与模型讨论情节的对话。聊天本身不会改变书中的任何内容：模型只能看到你选择发送的内容，并进行回复。当你们就某事达成一致后，只需点击一下，即可将其交给弧线生成器。

左侧可以选择模型能看到什么以及它如何提供帮助（这些选择会被记住，下次仍然生效），右侧是聊天区：

![剧情向导：左侧是选项，右侧是对话](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Model, Protocols | 初始使用本书的模型和协议。更改它们仅对本次对话生效。 |
| How the model helps | **Free talk**（由你主导）、**Explore directions**（几种彼此不同的故事走向）、**Find the problems**（从未解决的线索、无人兑现的承诺、与设定的矛盾、节奏问题）或 **Expand an idea**（将你的想法扩展为故事前提、转折点、结局，以及接续你已有弧线的章节）。 |
| Send the lorebook | 本书的设定集及其子设定集中的条目。 |
| Send the outline | **Not sent**、**Descriptions only** 或 **With the notes**（你的私人笔记也一并发送）。 |
| Send the notes | 仅当安装了 [Story Notes](story-notes.md) 时可用：**Not sent**、**Titles and first lines**（默认）或你的笔记**全文**，但不包括你对助手隐藏的页面。 |
| Send the summaries of the story | 摘要会代替故事中截至最新一条摘要为止的内容，与生成内容时的发送方式相同。 |
| Send the latest parts of the story | 最新摘要之后的各部分（关闭摘要时为全部部分），取能放入 **Tokens of the story** 限制内的最新部分。每个部分都会以其在故事大纲中的编号展示给模型。 |
| Tokens of the story | 故事各部分可使用的最大 token 数。留空时：为模型可接收量的三分之一。 |
| Prompt | 给模型的指令，可使用变量 `{{backgroundLore}}`、`{{outline}}`、`{{notes}}`、`{{summaries}}`、`{{story}}` 和 `{{mode}}`。修改后会被记住。 |

选项下方有一行文字，说明模型将获得多少条摘要和故事部分，以及该请求中占用了模型可接收容量的多少 token。如果超出限制，这行文字会变为警告；此时请减少发送内容，或[总结对话](#chats-branches-and-summaries)。每条消息发送时都会重新读取全书，因此大纲或故事的任何更改都会在下一次回复时生效。

右侧是聊天区。输入消息后按 **Send** 发送（模型输出时按钮变为 **Stop**，已收到的内容会保留）。每条消息都有若干按钮：复制、**branch from here**、删除；对于模型的回复，还有一个魔杖按钮 **Generate an arc from this message**，点击后会以该消息作为规格说明打开弧线生成器。如果讨论较长，请先让模型把你已定下的内容总结成一份规格说明。

<a id="letting-the-model-change-the-book"></a>

### 让模型修改书稿

选项下方是 **What the model may do**。如果这些分组都未勾选，聊天就只是单纯的对话。勾选某个分组后，模型就会获得该分组的工具，并可在你要求或同意时使用它们；模型会被告知：添加之前先查看，优先追加而非替换，并说明它做了什么：

| 分组 | 模型可以 |
|---|---|
| **Planner** | 列出[规划器](../books/story-editor.md#the-planner-tab)中的场景，添加、修改、移动、删除场景，并将其标记为已完成。 |
| **Lorebook** | 搜索并阅读设定，创建条目并修改已有条目（不能删除）。 |
| **Outline** | 阅读大纲，添加弧线和章节，修改并移动它们（不能删除）。 |
| **Notes**（安装了 [Story Notes](story-notes.md) 时） | 搜索并阅读你的笔记，顺着链接查看相关内容；当你要求它保存某些内容时，可以新增页面或在已有页面中追加内容；也可以修改某页中的文字（不能删除页面）。你对助手隐藏的页面无法访问。 |
| **Characters**（安装了 [Character Tracker](character-tracker.md) 时） | 读取书中的角色及其关系，添加角色和关系，修改它们（名称、标语、备注、颜色）并删除它们。只有删除操作会在执行前征求你的同意，修改则不会：你可以在聊天和 Characters 标签页中看到修改内容。模型可以用角色在列表中的编号来指代角色；确认提问和聊天中会在编号旁边显示角色名称。 |
| **Generators** | 调用弧线和章节生成器；安装了 [Lore Generator](lore-generator.md) 时，还可以生成设定条目。生成器只返回草稿，除非模型被告知，否则不会保存任何内容；模型会向你展示草稿，并在你同意后保存。 |
| **Tools**（按钮，安装了[工具套件](tools.md)时出现） | 命名工具（人名、地名），与生成器中的一样，以便它能为你提供可直接使用的名字。 |

- **Ask me before it changes or deletes something**（默认开启）：修改现有内容的文字（设定条目、场景、弧线）和删除操作，会先展示给你，并附上模型想要传入的内容；模型会等待你点击 **Allow** 或 **Refuse**。添加场景、条目或弧线则无需确认：你可以在聊天、规划器、设定集或大纲中看到它们，并且可以将其删除。
- 模型调用过的每个工具都会列在其回复下方（一行，包含名称和标记：✓ 表示成功，✗ 表示失败；展开可查看传入的内容和返回的结果）。之后的消息会携带模型此前的调用及其返回结果（过长的结果会被截断），因此它知道自己已经做过什么，不会重复创建同一条目；这些内容计入聊天的 token 上限。
- 工具的开销：工具的描述会包含在每个请求中，因此上下文较短的模型可用空间更少。只有支持函数调用的模型才能使用工具；如果模型不支持，对话框会提示。

![模型在规划器中添加了两个场景；其调用记录列在回复下方](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### 对话、分支与摘要

一本书可以有任意数量的对话。在消息上方的列表中选择一个对话；**+** 新建对话，铅笔图标用于重命名（在重命名之前，对话以你的第一条消息命名），垃圾桶图标用于删除。对话与书一同保存。

- **Branch** 会将当前对话原样复制为一个新对话；消息上的分支按钮则复制到该消息为止（包含该消息），以便从那里换一种方式继续讨论。复制之后，副本与原对话互不影响。
- **Summarize the chat** 首先会询问摘要应保留或强调什么（可选，例如"保留关于叛徒的转折以及我们否决的结局"；留空则由模型决定），然后请模型对对话进行总结（你已确定的内容、你否决的内容、仍未解决的问题、已确定的名称），并将其作为一条消息添加。此后，模型只会获得该摘要以及摘要之后的消息，**不会**获得摘要之前的消息；那些消息仍保留在屏幕上，但会变暗。当对话过长、模型难以处理，或想在不丢失已做决定的前提下从干净的状态重新开始时，可以使用此功能。之后再次总结时，新摘要会涵盖旧摘要。如果你希望模型保留整个对话，请先使用分支。

![询问摘要应保留哪些内容](../../images/plugin-arcoutline-wizard-summarize.png)

![摘要之后的对话：摘要之前的消息仍显示在屏幕上，但已变暗](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## 生成章节

弧线上的魔杖按钮会请求模型将该弧线划分为若干章节。对话框与弧线的对话框类似，左侧有以下选项：

| | |
|---|---|
| 模型对故事的了解范围 | **截至目前的整个故事**：包括此弧线之前的所有弧线及其全部章节。**仅弧线摘要**：同样的弧线，但不含章节。**弧线摘要和已完成的章节**：同样的弧线，只包含你标记为 *Finished* 的章节。此弧线之后的弧线永远不会发送。 |
| 你对章节的总体要求 | 节奏、基调、必须发生的事情。可以留空。 |
| 章节数量 | 可以用文字填写（如"大约八章"），也可以选择一个确切数字。填写数字后，每章都会出现一个文本框，可在其中说明该章应包含的内容；所有文本框都是可选的。两种方式只能用一种，使用其中一种会清除另一种。若指定确切数字，章节数不符的回答会被拒绝。 |
| 提示词 | 给模型的指令，其中包含变量 `{{backgroundLore}}`、`{{outline}}`、`{{numberOfChapters}}`、`{{generalSpecification}}` 和 `{{chapterSpecification}}`。初始内容来自 *Settings → Extension Settings → Arc Outline* 中的 **Chapter generation prompt**。 |

右侧会先显示模型的推理过程，然后是该弧线及其已有的章节，以及模型生成时逐步显示的新章节。**Save as chapters** 会将这些新章节添加到弧线末尾，每章都带有简短描述，详细内容写在其备注中（与弧线相同）。每个新章节都保留模型的推理过程和回答，并有自己的机器人按钮。

![生成弧线的章节](../../images/plugin-arcoutline-generate-chapters.png)

由模型编写的弧线或章节会保留其**推理过程**和生成它的**回答**。其上的机器人按钮会在一个小型弹出框中打开它们，不占用版面。

![模型为弧线写的内容](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## 命令

大纲及其生成器也可以作为[命令](../commands/index.md)使用，适用于命令行以及[代理](../agents.md)。它们作用于当前打开的书籍（代理则作用于其工作副本），打开的 Arcs 标签页会立即显示变更。弧线和章节通过**编号**指定，与大纲中显示的一致：第一条弧线为 `1`，弧线的第一个章节为 `1`（当前面的内容被添加、删除或移动时，编号会随之改变）。修改弧线或章节的命令只会处理你提供的字段；空文本会清除描述或备注。

| 命令 | 功能 |
|---|---|
| `/get-arc-outline [arc [notes]]` | 以 JSON 格式输出弧线及其章节、状态和描述。私人备注只有使用 `notes` `true` 时才会输出；`arc` 只返回该弧线。仅读取。 |
| `/add-arc title [description [note [state [position]]]]` | 添加一条弧线，放在末尾或 `position` 处。 |
| `/update-arc arc [title [description [note [state]]]]` | 修改一条弧线。`state` 可为 `PLANNED`、`CURRENT` 或 `FINISHED`（完成弧线会同时完成其章节）。 |
| `/move-arc arc position` | 将弧线移动到位置 `position`（1 为第一个）。 |
| `/delete-arc arc` | 删除弧线及其章节，删除前需确认。 |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | 为弧线添加一个章节。 |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | 修改一个章节。 |
| `/move-outline-chapter arc chapter position [to_arc]` | 在所属弧线内移动章节，或将其移入弧线 `to_arc`。 |
| `/delete-outline-chapter arc chapter` | 删除一个章节，删除前需确认。 |
| `/generate-arc [specification [save]]` | 请求模型生成一条新弧线，与 *Generate an arc* 的功能相同。 |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | 请求模型为某条弧线生成章节，与该弧线的魔杖按钮功能相同。`count` 为确切的章节数；`context` 为 `EVERYTHING`（默认）、`ARCS_ONLY` 或 `ARCS_AND_FINISHED`，对应 *What the model knows of the story* 中的选项。 |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | 请求模型将当前分支中第 `from` 至 `to` 条消息（按有文本的消息编号，与 *Import from story* 中相同）总结为一条带章节的弧线。`state`（默认为 `FINISHED`）表示已保存的弧线及其章节的状态。若加上 `mapParts` `true`，每个章节都会链接到其开始的消息（即其 *Linked part*，与对话框中复选框 *Map parts* 的作用相同），返回结果中会包含每个章节的对应部分。 |

这三个生成命令使用与对话框相同的提示词（即你在 *Extension Settings → Arc Outline* 中的设置，或默认值）、书籍所用的模型和协议（若书籍未指定模型，则使用你的默认模型），且不使用任何工具。需要名称的调用方，可通过 [名称生成器](name-generator.md) 或 [名称提供者](name-provider.md) 的命令获取名称，并将其写入规格说明。与对话框一样，这些命令只在被要求时才保存：使用 `save` `false`（默认）时，它们将弧线或章节以 JSON 形式返回，不做任何存储；使用 `save` `true` 时，它们会将结果添加到大纲中（弧线添加到末尾，章节添加到弧线末尾），回答中会包含它们的编号。若故事对模型而言过长，`/import-arc-from-story` 会拒绝该请求：请选择较少的消息。生成命令需要一定时间，运行被停止时也会随之停止。

添加和修改操作不具破坏性；只有两个 `delete` 命令具有破坏性，可以设置让代理在执行前向你征求确认。
