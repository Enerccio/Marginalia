---
label: 命令
order: 945
icon: terminal
---

# 命令

命令是你通过输入来执行的小型操作，而不必逐级点击菜单：在故事或设定集中查找内容、搜索和替换、创建备份。[扩展](../extensions/index.md)可以添加自己的命令，例如生成一条设定条目或随机取名。[智能体](../agents.md)使用的也是这些命令，因此每个命令只做一件明确的事，并且会说明它做什么，你在输入时就能看懂。

**命令行**是[故事编辑器](../books/story-editor.md)中故事下方菜单旁边的输入框。

<a id="running-a-command"></a>

## 运行命令

1. 点击命令行并输入 `/`。列表会显示所有命令，每个命令都带有名称和简短说明。继续输入可以缩小列表范围：过滤器会匹配命令的名称、标题和说明。
2. 用鼠标选择命令，或使用方向键和 **Enter** 选择。不需要参数的命令会立即运行；需要参数的命令则会把 `/name ` 写入输入框，然后你继续输入参数。
![通过输入斜杠打开的命令列表](../../images/command-list.png)

3. 输入参数后按 **Enter**，或点击输入框旁边的播放按钮来运行。命令一开始运行，输入框就会清空；如果输入的文本不是命令，它会保留下来，方便你修改。

不以 `/` 开头的文本不是命令，不会执行任何操作。点击输入框外部或按 **Esc** 都不会运行命令，因此你不会意外启动命令。

<a id="the-help"></a>

## 帮助

输入时，输入框上方会弹出一个小窗口。它会遮住故事的一部分，但不会移动任何内容，并且在你离开输入框或运行命令时关闭。窗口显示：

- 命令的作用，
- 与你目前输入内容相符的命令形式（参见[形式](#forms-of-a-command)），其中你正在输入的参数会高亮显示，并附有说明，
- 命令的返回结果，
- 当你输入的参数多于命令所需，或者没有任何命令使用该名称时，给出提示。

![在输入第二个参数时显示的 /search-replace-lore-entries 帮助](../../images/command-help.png)

当你还在输入命令名称时，命令列表本身就是帮助。

<a id="arguments"></a>

## 参数

参数之间用空格分隔，连续的空格算作一个。如果要在参数*内部*包含空格，有三种方法：

| 你输入的内容                 | 命令得到的内容                  | 原因                                       |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | 一个参数，`two words`           | `\ `（反斜杠加空格）表示一个空格           |
| `/search-parts "two words"`  | 一个参数，`two words`           | 双引号会把其中的所有内容保持在一起         |
| `/search-parts say\ "hi"`    | 一个参数，`say hi`              | 引号可以出现在参数的中间                   |

其他特殊字符：

| 你输入的内容                   | 你得到的内容               |
|-------------------------------|----------------------------|
| `\\`                          | 一个反斜杠                 |
| `\"`                          | 一个双引号，无论是否带引号：`"say \"hi\""` 得到 `say "hi"` |
| `""`                          | 一个空参数                 |

反斜杠放在其他字符前面时保持原样，因此 `C:\books\story` 会原样传递。如果你打开了引号却没有关闭，那么从引号开始到这一行末尾的所有内容都属于该参数。

参数都是文本。需要数字、是/否或几个选项之一的命令会自行转换文本；如果文本不符合要求，命令不会运行，并会弹出通知说明原因。

<a id="forms-of-a-command"></a>

## 命令的形式

同一个命令可以接受不同的参数：不带参数的 `/list-parts` 和带一个参数的 `/list-parts 2` 是同一个命令的两种**形式**。Marginalia 会根据你输入的参数个数和参数的样子来选择形式。如果没有任何形式匹配，会弹出通知说明该命令不接受这些参数，帮助窗口则会列出它实际支持的形式。

<a id="the-result"></a>

## 结果

命令返回内容时，结果会显示在命令行上方的窗口中。你可以点击叉号、按 **Esc** 关闭它，或者直接继续操作：当你输入新内容或运行其他命令时，它会自动消失。不返回任何内容的命令不会显示窗口。

![命令行上方显示的 /take-backup 结果](../../images/command-result.png)

如果命令失败，或者命令不存在，你会收到一条说明原因的通知。

<a id="commands-that-take-time"></a>

## 耗时的命令

大多数命令会立即返回结果。需要较长时间的命令会显示一个带动画进度条和 **Stop** 按钮的窗口。该窗口位于一侧，页面仍可正常使用；点击它的 ⌄ 可以将其收起到[运行面板](../books/story-editor.md#the-panel-of-the-runs)中，该命令会在那里列出，并显示模型正在写入的内容，在所有页面上都可见。进度条下方，命令可以说明当前正在做什么，并以较小的字体显示该步骤的详情。**Stop** 用于取消命令，取消应当立即生效。让模型撰写摘要或笔记的命令（如 `/generate-annotation`）就属于这一类。

在故事的命令栏中输入的 `/next`、`/swipe` 和 `/regenerate` 没有这样的窗口：新生成的部分会像按下圆形按钮时一样直接流入故事，再次按下圆形按钮即可停止，并且故事会像任何生成过程一样被锁定（你仍可以在设定集、其他部分和侧边标签页中继续工作）。该部分完成后，命令的结果会显示在命令栏上方。在此之前，命令栏会拒绝第二个此类命令（你的文本会保留，之后可以再运行）；其他命令仍可使用。

![正在运行的命令窗口](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## 修改数据的命令

每个命令都会说明它对已有数据做了什么，命令栏会在命令旁边、列表中和帮助里用一个小标签显示这一点：

- **破坏性**（红色）——命令会修改或删除已有数据，例如重写整本书的搜索和替换命令。将鼠标指向标签可查看说明。
- **非破坏性**（绿色）——命令只读取数据或添加新数据，例如 `/take-backup`，它只添加备份，不改动其他任何内容。

该标签仅供你参考，运行命令时不会阻止它执行。[智能体](../agents.md)会依据这个标签行事：根据为该智能体的设置，它不会被赋予破坏性命令，或者必须先询问你，或者直接运行。命令自行请求确认的情况见下文。

<a id="commands-that-ask-first"></a>

## 先询问的命令

修改范围较大的命令可能会在执行前先询问你。搜索和替换命令没有预览功能，因此在搜索之后，它们会显示找到了多少处匹配、分布在多少个条目中，以及将被替换成什么内容。只有点击 **Yes** 才会修改书籍；点击 **No** 则所有内容保持原样。问题会一直保留，直到你作出回答。

![/search-replace-parts 的确认提问](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Marginalia 自带的命令

| 命令                                 | 作用                                                                               |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 在打开的书的故事中搜索并替换，类似故事标签页的 **Search and replace** 窗口。选项为 `true`/`false`，按此顺序给出；省略的选项使用该窗口的默认值（[正则表达式](../regular-expressions.md)：否，区分大小写：是，摘要：是，所有分支：否，重新验证：是）。要设置其中一个选项，前面的选项也需一并给出。`maxHits` 会拒绝匹配数超过该值的替换；配合 `dryRun` `true` 使用时不会更改任何内容：回答会统计匹配数并显示部分结果，智能体通常先这样查看，再决定是否替换。没有预览：搜索后系统会询问是否替换找到的全部匹配项，只有点击 **Yes** 才会修改书籍。替换过程无法中止。 |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 对打开的书的词条库中的条目（名称、正文和注释）执行相同操作。 |
| `/take-backup`                       | 备份打开的书；备份会出现在书籍的 **Backups** 标签页中。没有打开的书时会提示没有打开书籍。 |
| `/get-book-info` | 打开的书的名称、设置、模型、总计（段落数、字数、词元数）和标签。 |
| `/list-parts [branch] [offset] [limit]` | 分支中段落的句柄、位置、长度、备选版本数、摘要状态和简短预览。 |
| `/get-part <part> [branch] [offset] [length] [field]` | 段落的正文（分段显示），或其 `instructions`、`scene`、`pov`、`present` 或 `reasoning`，以及它是由什么生成的。 |
| `/list-swipes <part> [branch]` | 段落的备选版本（swipes），并标明各自对应的分支编号。 |
| `/list-branches [offset] [limit]` | 书的分支，按编号列出，并标明各分支从其他分支的何处分出。 |
| `/get-part-metrics <part> [branch]` | 段落的词元数、耗时、模型和协议。 |
| `/show-prompt <part> [branch] [offset] [length]` | 生成段落时所用的提示词，按存储时的原样显示。 |
| `/search-parts <query> [limit]` | 在所有分支的段落中进行全文搜索，附带上下文片段。 |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 名称或词语出现的频率和位置（默认值：不使用正则、不区分大小写、仅段落、当前分支）。 |
| `/compare-parts <a> <b> [branchA] [branchB]` | 两个段落之间不同的句子；`0` 与 `b` 相同，但会将段落 `a` 与模型最初生成的文本进行比较。 |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | 一段文本的词元数（使用书籍模型的分词器）、字数和字符数；当文本为段落句柄时，统计该段落。 |
| `/list-summaries [branch]` | 分支的摘要、摘要所涵盖的段落，以及摘要是否仍然有效。 |
| `/get-summary <part> [branch]` | 段落的摘要。 |
| `/check-summaries [branch]` | 已过期的摘要，以及使其过期的已编辑段落。 |
| `/get-template-variables <scope> [part] [branch]` | 书籍（`global`）或段落（`part`）的模板变量。 |
| `/list-lorebooks` | 打开的书的词条库（根词条库及其子词条库），附编号、条目数、标签和子词条库。 |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | 某个词条库中的条目，附设置和预览；过滤器会保留名称或标签包含该过滤词的条目。 |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 单个条目：设置、关键词、激活条件（递归、上下文、段落范围、条件）、标签、注释，以及分段显示的正文。 |
| `/search-lore <query> [regex] [limit]` | 书中所有词条库里名称、正文或注释包含查询词的条目，附上下文片段。 |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | 对于某段文本，或对于与生成某段落时类似的一轮对话，生成时会使用哪些条目以及原因（无过滤条件；找到过滤词——在文本中，或在词条库设有[扫描深度](../lorebooks/entries.md#entries-brought-in-by-other-entries)时在其他条目的内容中；因已关闭而未使用；由书籍的某个标签决定；或过滤词不在文本中），并附带世界设定文本及其词元数。该文本可以是段落的句柄。 |
| `/list-annotations [status [part [offset [limit]]]]` | 书或段落的[批注](../books/annotations.md)：默认只列出未解决的批注，除非 `status` 指定了 `resolved`、`dismissed`、`orphaned` 或 `all`。 |
| `/get-annotation <annotation>` | 查看单条批注的完整内容，附带其所标注段落周围的文本。 |
| `/get-book-statistics [range] [side]` | 打开的书每天的操作记录（词元、字数、段落、编辑、时间、大小）：参见[写作统计](../books/statistics.md#commands)。 |
| `/get-statistics [range] [by] [side]` | 与上一条相同，但针对你的所有书籍，可按天或按书籍查看。无需打开书籍。 |

<a id="reading-the-story"></a>

### 阅读故事

上面列出的读取书籍的命令（`/get-book-info` 至 `/get-template-variables`）不会更改任何内容；它们以 JSON 文本形式返回结果，
因此智能体同样可以使用。回答中的每一行都有一个 **`handle`**：段落（或词条库、条目）的标识符，
在段落被添加、移动或删除时不会改变。段落可以通过其句柄指定，也可以通过其在分支上的 **位置** 指定，故事的第一个段落的位置为 `1`；分支可以通过其中任一段落的句柄指定，也可以通过从 `/list-branches` 开始的 **编号** 指定（编号按分支末端的创建顺序排列，因此添加或删除分支时编号会改变）。位置和编号更便于手动输入，而智能体应使用句柄，会修改故事的命令也只接受句柄。若未指定分支，命令作用于当前活动分支。列表使用 `offset` 和 `limit` 分页，长文本会被切分为片段（`offset`、`length`），回答中会注明总长度。与其他命令一样，选项是按位置给出的：要设置其中一个选项，前面的选项也需一并给出。

读取词条库的命令（`/list-lorebooks` 至 `/preview-lore-activation`）用法相同，且仅作用于打开的书的词条库：根词条库及其子词条库。词条库可通过其 **句柄** 或从 `/list-lorebooks` 开始的 **编号** 指定（根词条库为 `1`）；条目可通过其 **句柄** 或其在词条库中的 **编号** 指定，按词条库中的顺序排列（第一个条目为 `1`）。添加或删除条目或子词条库时，编号会改变。未指定词条库时，使用根词条库。
<a id="changing-the-story-and-the-lore"></a>

### 修改故事和设定集

这些命令修改当前打开的书（或某个智能体的工作副本，智能体的修改都在那里进行）。部分、设定集或条目始终通过其**句柄**指定，而不是通过位置或编号：添加或删除内容时，位置会变化，读取时正确的位置，写入时可能已经指向另一个部分。选项仍然是按位置给出的。删除文本或数据的命令是 `DESTRUCTIVE`，执行前会要求确认（智能体则由其破坏性模式处理）；添加和修改不需要确认。对故事的编辑可能使摘要过时（摘要带有它所描述的故事的哈希值）：回答会列出这些摘要并说明该怎么做，如果它们仍然正确用 `/revalidate-summaries`，如果不正确用 `/regenerate-summary`。

| 命令 | 作用 |
|------|------|
| `/edit-part <part> <old> <new> [field]` | 替换部分文本中的 `old` 为 `new`；`old` 必须恰好出现一次，否则不做任何改动，并且错误信息会说明它出现了几次。`field` 则改为编辑该轮次的 `instructions`、`scene`、`pov` 或 `present` 个字符。 |
| `/replace-part-text <part> <text> [field]` | 替换部分的全部文本。执行前会先询问。 |
| `/append-to-part <part> <text> [separator]` | 在部分末尾追加文本，默认在空行之后；除非 `separator` 另有指定。 |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | 修改部分的生成依据，新一次生成时会重用这些依据；空文本会清空某个字段，未提供的字段保持不变。 |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 在活动分支的最后一个部分之后，或在 `parent` 之后（即开辟新分支）写入一个新部分；它将成为活动分支的末端。 |
| `/insert-part <text> <part> [before]` | 紧接在 `part` 之后（当 `before` 为 `true` 时则紧接在其之前）把新部分插入故事中。该位置之后的所有内容（包括所有分支）现在都会排在新部分之后；不删除、也不重新生成任何内容，因此其他部分保留各自的摘要和数据，只是位置都后移一位。覆盖该位置的摘要会变为过时（回答会说明是哪些）。在分支末端使用时，等同于 `/create-part`。 |
| `/branch-part <part>` | 在某部分旁边复制该部分（及其摘要），作为一个新的候选版本，开辟一个新分支，并将副本设为活动的末端。 |
| `/delete-part <part>` | 删除一个部分及其摘要；之后的部分保留。执行前会先询问。 |
| `/switch-branch <part>` | 将通过某部分的分支设为活动分支。 |
| `/set-summary <part> <text>` | 替换某部分上摘要的文本。 |
| `/remove-summary <part> [unwind]` | 删除某部分的摘要（`unwind` 可恢复被元摘要替换掉的内容）。执行前会先询问。 |
| `/revalidate-summaries [part]` | 声明过时的摘要仍然准确描述故事（可针对活动分支的全部摘要，或仅针对该部分的摘要）。只有在你确认过之后才使用。 |
| `/regenerate-summary <part>` | 让模型重新为某部分生成摘要。速度慢，会消耗令牌。 |
| `/create-meta-summary <from> <to>` | 让模型把从部分 `from`（最新）到 `to` 之间的摘要合并为一个。速度慢，会消耗令牌。 |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | 让模型写出下一个部分，效果同 **新回合** 按钮。速度慢，会消耗令牌；同一本书一次只运行一个生成任务，其他任务等待。`profile` 指定智能体的某个模型配置。 |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | 让模型为活动分支的最后一个部分再写一个版本。未指定的选项从该部分中取值。 |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | 让模型重新写出活动分支的最后一个部分，替换其文本。执行前会先询问。 |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | 让模型根据你的指令重新撰写任意部分（参见[重写部分](../books/story-editor.md#redrafting-a-part)），并替换其文本。`prompt` 是用来代替本书提示模板的重写提示，`profile` 是智能体的某个模型配置，`apply` `false` 仅返回文本。`DESTRUCTIVE`；执行前会先询问。速度慢，会消耗令牌。 |
| `/impersonate [instructions] [outline] [prompt]` | 让模型代替你起草下一回合的指令（参见[起草指令](../books/story-editor.md#drafting-the-instructions)），并返回文本；不改动任何内容。`instructions` 是该部分应当写些什么（留空则顺着故事的走向继续），`outline` 是提供给模型的故事规划，`prompt` 是用来代替本书的代入提示。仅供你在命令栏中使用：[智能体](../agents.md)不提供此命令。速度慢，会消耗令牌。 |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 修改本书的描述性设置；只更新你给出的值。 |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 替换本书的提示模板。每个模板都会先经过检查。`DESTRUCTIVE`：有问题的模板会破坏之后的每一次生成。 |
| `/set-book-tags [add] [remove]` | 添加和移除本书的标签（以逗号分隔）。 |
| `/set-book-history-limit [maxVersions]` | 设定本书的[历史记录](../books/version-history.md#how-many-versions-are-kept)为每个部分保留多少个版本（`0` 表示从现在起不保留任何版本）；不给出数字则本书不设上限。文件夹、用户和管理员设置的上限仍然生效。不删除任何版本。 |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | 向设定集添加一个条目（未指定时添加到根设定集；没有设定集的书会获得一个，名称与该书相同）。`keys` 是以逗号分隔的关键词列表；`filteringMode` 为 `KEYWORDS_OR`（默认）、`KEYWORDS_AND`、`TEXT` 或 `REGEX`。模板或正则表达式的问题会被保存并报告。 |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 修改你给出的字段。 |
| `/delete-lore-entry <entry>` | 删除一个条目。执行前会先询问。 |
| `/move-lore-entry <entry> <lorebook>` | 将条目移动到本书的另一个设定集。 |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | 修改条目的标签和负面标签（以逗号分隔）。 |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | 修改条目的使用时机：`preventRecursion`、`recursionOnly` 和 `activationSummaries`（`true` / `false`），`activationContext`（`INSTRUCTIONS` 或 `INCLUDE_PARTS`）加上 `activationParts`，部分的范围 `fromMessage` / `toMessage`（0 表示取消限制），以及 `condition` 模板（空文本则移除它）。以空文本给出的值保持不变（`condition` 除外）。 |
| `/create-lorebook <name> [parent]` | 创建一个作为 `parent` 子书的设定集（未指定时以根设定集为父级；没有设定集的书会先获得一个）。 |
| `/set-lorebook <lorebook> [name] [enabled]` | 重命名设定集，或将其启用或停用。 |
| `/add-subbook <lorebook> <subbook>`、`/remove-subbook <lorebook> <subbook>` | 修改某个设定集所包含的设定集；设定集不能包含它自己。对于[智能体](../agents.md)，`/add-subbook` 还可以恢复之前被它删除或从其父级中移出的设定集（需使用它原来的句柄指定），连同其条目一起恢复。 |
| `/delete-lorebook <lorebook>` | 删除一个设定集及其条目（根设定集除外）。执行前会先询问。 |
<a id="annotations"></a>

### 批注

读取[批注](../books/annotations.md)的命令（`/list-annotations`、`/get-annotation`）与其他读取命令一样，以 JSON 形式返回结果。批注通过其**句柄**指定，用于修改内容的命令中，部分同样通过其句柄指定。这些命令只改变批注，从不改变故事，因此只有删除操作会先询问。智能体可以使用所有这些命令；当你[审阅结果](../books/annotations.md#annotations-and-agents)时，它的批注会进入你的书中。

| 命令 | 作用 |
|------|------|
| `/create-annotation <part> <text> [quote [type]]` | 为某个部分添加批注。`quote` 是段落的原文词句，从该部分复制而来：批注附着在该段落上，并在编辑文本时随之移动。若未提供引文，或引文不在文本中，则批注针对整个部分（回答中会说明）。 |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 更改批注的内容、类型或是否将其提供给模型；空的文本或类型保持不变。 |
| `/resolve-annotation <annotation> [status]` | 设置状态：`resolved`（默认）、`dismissed` 或 `open`。 |
| `/reattach-annotation <annotation> [quote]` | 将批注移动到引文在其所属部分中所对应的段落；若未提供引文，则批注针对整个部分。 |
| `/delete-annotation <annotation>` | 删除批注（可从回收站恢复）。会先询问。 |
| `/generate-annotation <part> [profile]` | 让模型像[用 AI 批注](../books/annotations.md#letting-a-model-annotate)那样审阅该部分，并将其发现的所有内容添加为批注（来源为 *AI*；不在文本中的引文会生成针对整个部分的批注）。不会询问：批注是唯一的改动，可以被标记为已解决、编辑或删除。它使用书籍所用的模型进行审阅；当 *Write the book with the model of the agent* 开启时，智能体则使用自己的模型（Lore Generator 的命令即如此）。`profile` 是智能体的一个模型配置。运行时间较长，并消耗令牌。 |

<a id="the-planner"></a>

### 规划器

这些是书籍[规划器](../books/story-editor.md#the-planner-tab)的命令。场景通过其在列表中的**编号**指定（从 1 开始，`/get-planned-scenes` 会列出它们）；场景被移动或删除后编号会随之变化，因此修改后请重新读取列表。它们只改变规划器，从不改变故事。添加、移动以及将场景标记为已完成不会询问；修改场景文本和删除场景则具有破坏性（[智能体](../agents.md)与其他破坏性命令一样会先询问）。

| 命令 | 作用 |
|------|------|
| `/get-planned-scenes` | 列出场景及其编号、标题、场景设定、视角角色、在场角色、指示，以及是否已完成。 |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 在列表末尾添加一个场景。 |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 更改标题及已给出的细节；未提供的内容保持不变，空文本则会将其清除。 |
| `/set-planned-scene-done <scene> [done]` | 将场景标记为已写（默认为 `done`，即 `true`）或未写。 |
| `/move-planned-scene <scene> <to>` | 将场景移动到位置 `to`（1 为第一位；位置过大时移到最后）。 |
| `/delete-planned-scene <scene>` | 删除场景。 |

让模型写作或审阅的命令耗时较长：`/generate-annotation` 以及两个摘要命令会显示 **Stop** 窗口，`/next`、`/swipe` 和 `/regenerate` 会将内容流式输出到故事中（参见[耗时命令](#commands-that-take-time)）。

<a id="commands-that-come-with-extensions"></a>

## 随扩展提供的命令

只要扩展已加载，你就会在列表中看到这些命令；每个命令的说明见其扩展的文档。

| 命令 | 扩展 | 作用 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 你保存的生成提示词，或其中某一个的文本。 |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | 利用书籍设定集的内容生成一条设定条目的文本；除非 `compact` 为 `false`，否则会将其压缩，然后返回。不保存任何内容。 |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 将生成的文本压缩为精炼的设定条目文本并返回。不保存任何内容。 |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 虚构姓名的类别，以及其中某一类的姓名。 |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 真实姓名列表，以及从其中某一列表选取的姓名。 |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 真实地点的类型；按名称、类型和国家搜索；某点周围的地点；以及两地之间的距离。 |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | 大纲中的篇章和章节。 |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | 添加、更改、移动和删除篇章（`<arc>` 为其编号）。 |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | 对篇章中的章节执行同样的操作。 |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | 篇章标签页中的生成器。它们返回结果，只有使用 `save` 时才会将其添加到大纲中。 |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 书中的角色，以及其中某一角色的备注和关系。 |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 添加、更改和删除角色。 |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 角色之间的关系。 |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 书籍笔记的页面（不包括对助手隐藏的页面）、某一页及其文本和链接，以及搜索。 |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | 添加、追加、更改、编辑和删除页面；`note-text` 是为 {%{`{{command::note-text::Title}}`}%} 宏而设的。 |

Lore Generator、Name generator、Name provider 和 Place provider 的命令只做读取：其中用于生成的命令返回文本，由你或调用它的智能体决定是否将其放入条目中。Arc Outline 和 Character Tracker 的命令还会改变当前打开书籍的大纲和角色；只有它们的 `delete` 命令具有破坏性。Story Notes 同样会改变当前打开书籍的页面；其中 `update-note` 和 `edit-note` 也具有破坏性，因为它们会替换已有的文本。

<a id="limits"></a>

## 限制

- 命令行位于桌面布局的故事标签页中。[手机视图](../phone.md)暂时还没有命令行。
- 命令以你的身份、在你的会话中运行，并作用于你当前打开的书籍。
