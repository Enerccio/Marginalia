---
label: 命令
order: 945
icon: terminal
---

# 命令

命令是通过输入来执行的小型操作，而不必逐级点击菜单：在故事或设定集中查找内容、搜索与替换，或者创建备份。[扩展](../extensions/index.md)会添加它们自己的命令，例如生成一条设定条目或为角色取名。[智能体](../agents.md)使用的也是同样的命令，因此每条命令只做一件明确的事，并说明它做什么：你在输入时就能读懂。

**命令行**是[故事编辑器](../books/story-editor.md)中故事下方菜单旁边的输入框。

<a id="running-a-command"></a>

## 运行命令

1. 在命令行中点击并输入 `/`。列表会显示所有命令，每条都带有名称和简短说明。继续输入可以缩小范围：筛选会匹配命令的名称、标题和说明。
2. 用鼠标选择命令，或使用方向键和 **Enter** 选择。不需要参数的命令会立即运行；需要参数的命令会在输入框中写入 `/名称 `，然后你继续输入参数。
![输入斜杠后打开的命令列表](../../images/command-list.png)

3. 输入参数后按 **Enter**，或点击输入框旁边的播放按钮来运行命令。命令一开始运行，输入框就会清空；不是命令的文本会保留下来，方便你修改。

不以 `/` 开头的文本不是命令，不会运行任何东西。点击输入框以外的地方或按 **Esc**，也绝不会运行命令，因此你不会误启动命令。

<a id="the-help"></a>

## 帮助

输入时，输入框上方会弹出一个小窗口。它会遮住部分故事内容，但不会移动任何东西；离开输入框或运行命令后，它就会关闭。它显示：

- 命令的作用，
- 与你目前输入的内容相符的命令形式（见[形式](#forms-of-a-command)），并高亮显示你正在输入的参数及其说明，
- 命令的返回结果，
- 当你输入的参数多于命令所需，或者没有任何命令使用该名称时的提示。

![在输入 /search-replace-lore-entries 的第二个参数时显示的帮助](../../images/command-help.png)

在你还在输入名称时，命令列表本身就是帮助。

<a id="arguments"></a>

## 参数

参数之间用空格分隔，连续的空格算作一个。若要在参数*内部*放入空格，有三种方法：

| 你输入的内容                   | 命令得到的内容                | 原因                                        |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | 一个参数，`two words`       | `\ `（反斜杠加空格）表示一个空格         |
| `/search-parts "two words"`  | 一个参数，`two words`       | 双引号会把其中的所有内容保持在一起       |
| `/search-parts say\ "hi"`    | 一个参数，`say hi`          | 引号可以从参数的中间开始                 |

其他特殊字符：

| 你输入的内容                  | 你得到的结果                |
|-------------------------------|----------------------------|
| `\\`                          | 一个反斜杠                  |
| `\"`                          | 一个双引号，外面有没有引号都可以：`"say \"hi\""` 得到 `say "hi"` |
| `""`                          | 一个空参数                  |

反斜杠放在其他任何字符前面时保持原样，因此 `C:\books\story` 会原样传递。如果你打开了引号却没有关闭，那么从引号到行尾的所有内容都属于该参数。

参数都是文本。需要数字、是/否或几个选项之一的命令会自行转换文本；如果文本不符合要求，命令就不会运行，并会弹出通知说明原因。

<a id="forms-of-a-command"></a>

## 命令的形式

同一条命令可以接受不同的参数：不带参数的 `/list-parts` 和带一个参数的 `/list-parts 2` 是同一条命令的两种**形式**。Marginalia 会根据你输入的参数数量及其内容来选择形式。如果没有形式符合，会弹出通知，说明该命令不接受这些参数，帮助窗口则会列出它确实支持的形式。

<a id="the-result"></a>

## 结果

命令返回内容时，结果会显示在命令行上方的窗口中。可以点击叉号、按 **Esc** 关闭它，或者直接继续操作：当你输入新内容或运行另一条命令时，它会消失。不返回任何内容的命令不会显示窗口。

![命令行上方显示的 /take-backup 结果](../../images/command-result.png)

如果命令失败，或者命令不存在，会弹出通知说明原因。

<a id="commands-that-take-time"></a>

## 耗时的命令

大多数命令会立即返回结果。需要一定时间的命令会显示一个带动画进度条和**停止**按钮的窗口。该窗口位于一侧，页面仍可继续使用；点击其中的 ⌄ 可以把它收起到[运行面板](../books/story-editor.md#the-panel-of-the-runs)中，该命令会与模型正在生成的内容一起列在所有页面上。进度条下方，命令可以说明它当前正在做什么，以及步骤的细节（字号较小）。**停止**会取消命令，并应立即返回。像 `/generate-annotation` 这类让模型撰写摘要或笔记的命令，就属于这一类。

在故事的命令栏中输入的 `/next`、`/swipe` 和 `/regenerate` 没有这样的窗口：新的部分会像按下圆形按钮时一样流式写入故事，圆形按钮可以停止生成，并且故事像任何生成时一样被暂时锁定（你仍可以在设定集、其他部分和侧边标签页中继续工作）。部分生成完成后，命令的结果会出现在命令栏上方。在此之前，命令栏会拒绝再次运行这类命令（你的文本会保留，之后可以再运行）；其他命令不受影响。

![正在运行的命令窗口](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## 修改数据的命令

每条命令都会说明它对已有数据做了什么，命令栏会在命令旁边、列表和帮助中用一个小标签显示这一点：

- **破坏性**（红色）——命令会修改或删除已有数据，例如会改写整本书的搜索与替换命令。将鼠标指向该标签可查看说明。
- **非破坏性**（绿色）——命令只读取数据或添加新数据，例如 `/take-backup`，它只添加一份备份，不改变其他任何内容。

该标签仅作提示，运行时不会阻止命令执行。[智能体](../agents.md)会依据它行事：根据为该智能体设定的选项，它不会获得破坏性命令，或者必须先询问你，或者直接运行。命令自身会要求确认的情况，见下文。

<a id="commands-that-ask-first"></a>

## 先询问的命令

会大幅改动内容的命令可能会在执行前先询问。搜索与替换命令没有预览功能，因此搜索完成后，它们会显示找到了多少处匹配、分布在多少个条目中，以及将被替换成什么内容；只有点击**是**才会修改书籍，点击**否**则一切保持不变。问题会一直保留，直到你作出回答。

![/search-replace-parts 的确认提问](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## 不随 Marginalia 提供的命令

| 命令 | 作用 |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 在当前打开的书的故事中搜索并替换，与故事标签页的**搜索和替换**窗口相同。选项为 `true`/`false`，按此顺序排列；省略的选项使用该窗口的默认值（[正则表达式](../regular-expressions.md)：否；区分大小写：是；摘要：是；所有分支：否；重新验证摘要：是）。要设置其中一个选项，必须同时给出前面的所有选项。`maxHits` 会拒绝找到的匹配数超过该值的替换操作；当 `dryRun` 为 `true` 时，不会更改任何内容：结果会统计匹配数并显示部分匹配项，智能体在替换前通常会这样查看。没有预览功能：搜索之后，系统会询问是否替换所找到的匹配数，只有点击**是**才会修改书籍。替换操作无法中止。 |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 与上一条相同，但针对当前打开的书的设定集条目（名称、正文和注释）。 |
| `/take-backup` | 为当前打开的书做一次备份；备份会出现在该书的**备份**标签页中。若未打开任何书，则提示没有打开的书。 |
| `/get-book-info` | 当前打开的书的名称、设置、模型、总计（段落、字数、词元）以及标签。 |
| `/list-parts [branch] [offset] [limit]` | 某个分支的段落：句柄、位置、大小、候选版本数、摘要状态以及简短预览。 |
| `/get-part <part> [branch] [offset] [length] [field]` | 某个段落的正文（分段显示），或其 `instructions`、`scene`、`pov`、`present` 或 `reasoning` 字段，以及它的生成依据。 |
| `/list-swipes <part> [branch]` | 某个段落的备选版本（候选版本），以及显示每个版本所用的分支编号。 |
| `/list-branches [offset] [limit]` | 书的分支，带编号，并说明每个分支与其他分支从哪里分开。 |
| `/get-part-metrics <part> [branch]` | 某个段落的词元、耗时、模型和协议。 |
| `/show-prompt <part> [branch] [offset] [length]` | 生成某个段落时所用的提示词，按其存储时的原样显示。 |
| `/search-parts <query> [limit]` | 在所有分支的段落中进行全文搜索，并附带摘录。 |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 名称或词语出现的次数及位置（默认值：不使用正则、不区分大小写、仅搜索段落、当前分支）。 |
| `/compare-parts <a> <b> [branchA] [branchB]` | 两个段落之间不同的句子；将 `b` 设为 `0` 时，会把段落 `a` 与模型最初生成的文本进行比较。 |
| `/count-tokens <text>`、`/count-tokens <part> <branch>` | 一段文本的词元（使用该书所用模型的分词器）、字数和字符数；若文本是某个段落的句柄，则统计该段落。 |
| `/list-summaries [branch]` | 某个分支的摘要、它们所涵盖的段落，以及摘要是否仍然有效。 |
| `/get-summary <part> [branch]` | 某个段落的摘要。 |
| `/check-summaries [branch]` | 已过时的摘要，以及使其过时的已编辑段落。 |
| `/get-template-variables <scope> [part] [branch]` | 书的模板变量（`global`），或某个段落的模板变量（`part`）。 |
| `/list-lorebooks` | 当前打开的书的设定集（根设定集及其子集），附带编号、条目数、标签和子集。 |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | 某个设定集的条目，附带其设置和预览；筛选条件会保留名称或标签包含该内容的条目。 |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 单个条目：设置、关键词、激活规则（递归、上下文、段落范围、条件）、标签、注释，以及分段显示的正文。 |
| `/search-lore <query> [regex] [limit]` | 书的所有设定集中，名称、正文或注释包含该查询内容的条目，附带摘录。 |
| `/preview-lore-activation <text> [limit]`、`/preview-lore-activation <part> <branch> <limit>` | 对于某段文本，或对于与生成某个段落时相似的一轮对话，说明生成时会使用哪些条目以及原因（无筛选；在文本中找到了筛选内容；或在另一条目的内容中找到了筛选内容，前提是该设定集设有[扫描深度](../lorebooks/entries.md#entries-brought-in-by-other-entries)；或因为已关闭而未使用；或由书的某个标签决定；或筛选内容不在文本中），并给出设定文本及其词元。文本也可以是某个段落的句柄。 |
| `/list-annotations [status [part [offset [limit]]]]` | 书或某个段落的[批注](../books/annotations.md)：除非 `status` 为 `resolved`、`dismissed`、`orphaned` 或 `all`，否则默认列出未解决的批注。 |
| `/get-annotation <annotation>` | 单条批注的完整内容，以及其所在段落的周围文本。 |
| `/get-book-statistics [range] [side]` | 当前打开的书每天所做的工作（词元、字数、段落、编辑、时间、大小）：参见[写作统计](../books/statistics.md#commands)。 |
| `/get-statistics [range] [by] [side]` | 与上一条相同，但针对你所有的书，可按天或按书查看。不需要打开任何书。 |

<a id="reading-the-story"></a>

### 阅读故事

上面读取书籍的命令（从 `/get-book-info` 到 `/get-template-variables`）不会更改任何内容；它们以 JSON 文本形式返回结果，
因此智能体也可以使用它们。结果中的每一行都有一个 **`handle`**：段落（或设定集、条目）的标识符，
在添加、移动或删除段落时不会改变。段落可以通过其句柄指定，也可以通过它在分支上的**位置**指定，故事的第一个
段落为 `1`；分支可以通过其中任意段落的句柄指定，也可以通过 `/list-branches` 给出的**编号**指定（编号按分支末端的创建顺序排列，
因此添加或删除分支时编号会变化）。位置和编号更便于输入，句柄则是智能体应当使用的标识；会更改故事的命令只接受句柄。不指定分支时，命令作用于当前活动分支。列表用 `offset` 和 `limit` 分页，长文本用 `offset`、`length` 分段，结果会说明总长度。与其他命令一样，选项按位置排列：要设置其中一个选项，必须同时给出前面的所有选项。

读取设定集的命令（从 `/list-lorebooks` 到 `/preview-lore-activation`）工作方式相同，并且只访问当前打开的书的设定集：根设定集及其子集。设定集可以通过其**句柄**或 `/list-lorebooks` 给出的**编号**指定（根设定集为 `1`），条目可以通过其**句柄**或其在设定集中的**序号**指定（按设定集顺序，第一个条目为 `1`）；添加或删除条目或子集时，序号会变化。未指定设定集时，使用根设定集。
<a id="changing-the-story-and-the-lore"></a>

### 修改故事和设定

这些命令会修改当前打开的书（或智能体的工作副本，智能体的修改就写在那里）。部分、世界书或条目总是通过其**句柄**指定，而不是通过位置或编号：添加或删除内容时它们的位置会变动，读取时正确的位置，写入时可能已经指向另一个部分。选项仍然是按位置传入的，与之前相同。删除文本或数据的命令标记为 `DESTRUCTIVE`，会要求你确认（智能体则由其破坏性模式负责）；添加和修改不需要确认。对故事的编辑可能使摘要过时（摘要带有它所描述的故事的哈希值）：回答中会列出这些摘要，并说明该怎么做。如果仍然正确，使用 `/revalidate-summaries`；如果不正确，使用 `/regenerate-summary`。

| 命令 | 作用 |
|------|------|
| `/edit-part <part> <old> <new> [field]` | 在某个部分的正文中把 `old` 替换为 `new`；`old` 必须恰好出现一次，否则不做任何改动，错误信息会说明它出现了几次。`field` 则改为编辑本轮的 `instructions`、`scene`、`pov` 或 `present` 字段（视角与在场角色）。 |
| `/replace-part-text <part> <text> [field]` | 替换某个部分的全部正文。会先询问。 |
| `/append-to-part <part> <text> [separator]` | 在某个部分的末尾添加文本，默认在一个空行之后；若 `separator` 另有指定则按其设置。 |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | 修改某个部分的生成依据，重新生成时会沿用这些设置；空文本会清除对应字段，未填写的字段保持不变。 |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 在当前分支的最后一个部分之后写入一个新部分，或在 `parent` 之后写入（即开启一个新分支）；它将成为当前分支的末尾。 |
| `/insert-part <text> <part> [before]` | 在 `part` 之后插入一个新部分到故事中（当 `before` 为 `true` 时则插入到它之前）。该位置之后的所有内容（包括所有分支）现在都排在新部分之后；没有任何内容被删除或重新生成，因此其他部分保留各自的摘要和数据，只是位置向后移动一位。覆盖该位置的摘要会变为过时（回答中会说明是哪些）。若位于分支末尾，则与 `/create-part` 相同。 |
| `/branch-part <part>` | 在某个部分旁边复制一份（连同其摘要），作为开启新分支的一个新的备选版本，并将该副本设为当前分支的末尾。 |
| `/delete-part <part>` | 删除某个部分及其摘要；其后的部分保留。会先询问。 |
| `/switch-branch <part>` | 将经过某个部分的分支设为当前分支。 |
| `/set-summary <part> <text>` | 替换某个部分上摘要的文本。 |
| `/remove-summary <part> [unwind]` | 删除某个部分的摘要（`unwind` 会恢复被元摘要替代的内容）。会先询问。 |
| `/revalidate-summaries [part]` | 确认过时的摘要仍然符合故事内容（可针对当前分支的全部摘要，或仅针对该部分的摘要）。只有在你已核对之后才使用。 |
| `/regenerate-summary <part>` | 让模型重新生成某个部分的摘要。速度较慢，消耗令牌。 |
| `/create-meta-summary <from> <to>` | 让模型把从部分 `from`（最新）回溯到 `to` 之间的摘要合并为一条。速度较慢，消耗令牌。 |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | 让模型撰写下一个部分，效果与 **New turn** 按钮相同。速度较慢，消耗令牌；同一本书一次只运行一个生成任务，其他任务排队等待。`profile` 指定智能体的某个模型配置。 |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | 让模型为分支的最后一个部分撰写另一个版本。省略的参数取自该部分。 |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | 让模型重新撰写分支的最后一个部分，并替换其正文。会先询问。 |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | 根据你的指令让模型重新撰写任意部分（参见 [重写一个部分](../books/story-editor.md#redrafting-a-part)），并替换其正文。`prompt` 为用于代替本书设定的重写提示词，`profile` 为智能体的某个模型配置，`apply` 为 `false` 时只返回文本。`DESTRUCTIVE`；会先询问。速度较慢，消耗令牌。 |
| `/impersonate [instructions] [outline] [prompt]` | 由模型代你起草下一轮的指令（参见 [起草指令](../books/story-editor.md#drafting-the-instructions)），并返回文本；不做任何修改。`instructions` 为该部分应当涉及的内容（留空则顺着故事走向继续），`outline` 为供模型参考的故事大纲，`prompt` 为用于代替本书设定的代拟提示词。仅供你在命令栏中使用：[智能体](../agents.md) 不提供此命令。速度较慢，消耗令牌。 |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 修改本书的描述性设置；只修改你给出的值。 |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 替换本书的提示词模板。每个模板都会先经过检查。`DESTRUCTIVE`：有问题的模板会破坏之后的每一次生成。 |
| `/set-book-tags [add] [remove]` | 为本书添加或移除标签（用逗号分隔）。 |
| `/set-book-history-limit [maxVersions]` | 设置本书的[历史记录](../books/version-history.md#how-many-versions-are-kept)为每个部分保留的版本数量（`0` 表示从现在起不保留任何版本）；不填数字则本书不设上限。文件夹、用户和管理员设定的上限仍然有效。不会删除任何版本。 |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | 向世界书添加一个条目（未指定时添加到根世界书；没有世界书的书会自动创建一个，与书同名）。`keys` 为以逗号分隔的关键词列表；`filteringMode` 可为 `KEYWORDS_OR`（默认）、`KEYWORDS_AND`、`TEXT` 或 `REGEX`。模板或正则表达式的问题会被保存，并一并报告。 |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 修改你给出的字段。 |
| `/delete-lore-entry <entry>` | 删除一个条目。会先询问。 |
| `/move-lore-entry <entry> <lorebook>` | 将一个条目移动到本书的另一个世界书。 |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | 修改条目的标签和排除标签（用逗号分隔）。 |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | 修改条目的启用时机：`preventRecursion`、`recursionOnly` 和 `activationSummaries`（`true` / `false`），`activationContext`（`INSTRUCTIONS` 或 `INCLUDE_PARTS`）及其 `activationParts`，部分的范围 `fromMessage` / `toMessage`（0 表示去除限制），以及 `condition` 模板（空文本则将其删除）。给定空文本的值保持不变（`condition` 除外）。 |
| `/create-lorebook <name> [parent]` | 创建一个世界书，作为 `parent` 的子世界书（未指定时以根世界书为父级；没有世界书的书会先自动创建一个）。 |
| `/set-lorebook <lorebook> [name] [enabled]` | 重命名世界书，或将其启用或停用。 |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | 修改一个世界书所包含的世界书；世界书不能包含它自身。对于[智能体](../agents.md)，`/add-subbook` 还可以恢复它此前删除或从父世界书中移出的世界书（需通过它原来的句柄指定），连同其条目一起恢复。 |
| `/delete-lorebook <lorebook>` | 连同其条目删除一个世界书（根世界书除外）。会先询问。 |
<a id="annotations"></a>

### 批注

读取[批注](../books/annotations.md)的命令（`/list-annotations`、`/get-annotation`）与其他读取类命令一样，以 JSON 形式返回结果。
批注通过其**句柄**指定，段落则通过用于修改操作的命令所用的句柄指定。这些命令只改动批注，不改动故事，因此只有删除操作会先询问。智能体可以使用全部命令；它写下的批注会在你[审阅结果](../books/annotations.md#annotations-and-agents)时进入你的书中。

| 命令 | 作用 |
|------|------|
| `/create-annotation <part> <text> [quote [type]]` | 为某个段落添加一条批注。`quote` 是段落中原文的词句，需从段落中复制：批注会附着在该段落上，并在文本被编辑时随之移动。若不提供 quote，或其内容不在文本中，批注针对整个段落（回复中会说明这一点）。 |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 修改批注的内容、类型，或模型是否会接收该批注；文本或类型留空则保持不变。 |
| `/resolve-annotation <annotation> [status]` | 设置状态：`resolved`（默认）、`dismissed` 或 `open`。 |
| `/reattach-annotation <annotation> [quote]` | 将批注移动到其所在段落中 quote 所匹配的位置；若不提供 quote，批注针对整个段落。 |
| `/delete-annotation <annotation>` | 删除批注（可从回收站恢复）。会先询问。 |
| `/generate-annotation <part> [profile]` | 让模型像 [用 AI 批注](../books/annotations.md#letting-a-model-annotate)那样审阅该段落，并将其发现的所有问题添加为批注（来源为 *AI*；内容不在文本中的 quote 会使批注针对整个段落）。此操作不会询问：批注是唯一的改动，且可以被标记为已解决、编辑或删除。它使用本书的模型进行审阅；若智能体开启了 *Write the book with the model of the agent*（与 Lore Generator 命令的做法相同），则使用该智能体自己的模型，`profile` 是智能体的一个模型配置。此命令单独运行，耗时较长，并消耗令牌。 |

<a id="the-planner"></a>

### 规划器

用于本书[规划器](../books/story-editor.md#the-planner-tab)的命令。场景通过其在列表中的**编号**指定（从 1 开始，`/get-planned-scenes` 会显示编号）；移动或删除场景后，编号会发生变化，因此在修改后应重新读取列表。这些命令只改动规划器，不改动故事。添加、移动和标记场景为已完成不会询问；修改场景文本和删除场景属于破坏性操作（[智能体](../agents.md)与其他破坏性命令一样会先询问）。

| 命令 | 作用 |
|------|------|
| `/get-planned-scenes` | 列出各场景的编号、标题、场景设定、视角角色、在场角色、写作指令，以及是否已完成。 |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 在列表末尾添加一个场景。 |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 修改标题及所给出的细节；未提供的内容保持不变，留空的文本则会清除该项。 |
| `/set-planned-scene-done <scene> [done]` | 将场景标记为已写作（`done` 默认为 `true`）或未写作。 |
| `/move-planned-scene <scene> <to>` | 将场景移动到位置 `to`（1 为第一位；若数值过大，则移到最后一位）。 |
| `/delete-planned-scene <scene>` | 删除该场景。 |

让模型写作或审阅的命令耗时较长：`/generate-annotation` 以及两个摘要命令会显示 **Stop** 窗口；`/next`、`/swipe` 和 `/regenerate` 会将内容流式写入故事中（参见[耗时命令](#commands-that-take-time)）。

<a id="commands-that-come-with-extensions"></a>

## 随扩展提供的命令

只要相应扩展已加载，你就能在列表中看到这些命令；每个命令的说明见其扩展的文档。

| 命令 | 扩展 | 作用 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 你保存的生成提示词，或其中某一个的文本。 |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | 结合设定集的内容生成一条设定条目的文本，将其压缩（除非 `compact` 为 `false`），然后返回。不保存任何内容。 |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 将生成的文本压缩为精炼的设定条目文本并返回。不保存任何内容。 |
| `/list-name-categories [<filter>]`、`/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 虚构人名的类别，以及某一类别下的人名。 |
| `/list-real-name-lists`、`/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 真实人名列表，以及从某一列表中挑选的人名。 |
| `/list-place-types`、`/search-places <query> [...]`、`/places-near <place> <radius_km> [...]`、`/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 真实地点的类型、按名称、类型和国家搜索、某一点周围的地点，以及两个地点之间的距离。 |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | 大纲中的篇章和章节。 |
| `/add-arc`、`/update-arc`、`/move-arc`、`/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | 添加、修改、移动和删除一个篇章（`<arc>` 为其编号）。 |
| `/add-outline-chapter`、`/update-outline-chapter`、`/move-outline-chapter`、`/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | 对篇章中的章节执行同样的操作。 |
| `/generate-arc`、`/generate-outline-chapters <arc>`、`/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | 篇章标签页中的生成器。它们返回结果，仅在使用 `save` 时才将其添加到大纲中。 |
| `/list-characters`、`/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 本书的角色，以及某个角色的备注和关系。 |
| `/create-character`、`/update-character`、`/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 添加、修改和删除角色。 |
| `/list-character-relations`、`/add-character-relation`、`/update-character-relation`、`/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 角色之间的关系。 |
| `/list-notes`、`/get-note <note>`、`/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 本书笔记的页面（不包括对助手隐藏的页面）、某一页的正文和链接，以及搜索。 |
| `/create-note`、`/append-to-note`、`/update-note`、`/edit-note`、`/delete-note`、`/note-text <note>` | [Story Notes](../extensions/story-notes.md) | 添加、追加、修改、编辑和删除页面；`note-text` 专为 {%{`{{command::note-text::Title}}`}%} 宏设计。 |

Lore Generator、Name generator、Name provider 和 Place provider 的命令只读取数据：其中的生成类命令返回文本，由你或调用它的智能体决定是否将其放入条目中。Arc Outline 和 Character Tracker 的命令会修改当前打开的书的大纲和角色；只有它们的 `delete` 命令具有破坏性。Story Notes 也会修改当前打开的书的页面；其中 `update-note` 和 `edit-note` 同样具有破坏性，因为它们会替换已有文本。

<a id="limits"></a>

## 限制

- 命令行位于桌面布局的故事标签页中。[手机视图](../phone.md)暂时还没有命令行。
- 命令以你的身份在你的会话中运行，并作用于你当前打开的书。
