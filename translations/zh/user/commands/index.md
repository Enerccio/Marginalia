---
label: 命令
order: 945
icon: terminal
---

# 命令

命令是你通过输入来执行的小操作，无需点击菜单。你可以用它们查找故事或设定集中的内容、搜索和替换，或者创建备份。[扩展](../extensions/index.md)还会添加它们自己的命令，例如生成设定条目或取名。[智能体](../agents.md)使用的也是同样的命令，因此每条命令只做一件明确的事，并且名称和说明都让你在输入时就能看懂。

**命令行**是[故事编辑器](../books/story-editor.md)中故事下方菜单旁边的输入框。

<a id="running-a-command"></a>

## 运行命令

1. 点击命令行并输入 `/`。列表会显示所有命令，每条都带有名称和简短说明。继续输入可以缩小范围：筛选会匹配命令的名称、标题和说明。
2. 用鼠标选择命令，或用方向键加 **Enter** 选择。不需要参数的命令会立即运行；需要参数的命令则会把 `/name ` 写入输入框，然后你继续输入参数。
![通过输入斜杠打开的命令列表](../../images/command-list.png)

3. 输入参数，然后按 **Enter**，或点击输入框旁的播放按钮来运行。命令一开始运行，输入框就会清空；不是命令的文字会保留，方便你修改。

不以 `/` 开头的文字不是命令，因此不会运行任何东西。点击输入框以外的地方或按 **Esc** 都不会运行命令，所以你不会误触发命令。

<a id="the-help"></a>

## 帮助

输入时，输入框上方会弹出一个小窗口。它会遮住部分故事内容，但不会移动任何东西；当你离开输入框或运行命令时，它会关闭。它显示：

- 命令的作用，
- 与你目前输入内容相符的命令形式（参见[形式](#forms-of-a-command)），其中正在输入的参数会被高亮显示，并附有说明，
- 命令的返回结果，
- 当你输入的参数多于命令所需，或者没有任何命令使用该名称时的提示。

![在输入第二个参数时 /search-replace-lore-entries 的帮助](../../images/command-help.png)

在你仍在输入名称时，命令列表本身就是帮助。

<a id="arguments"></a>

## 参数

参数之间用空格分隔，连续的空格算作一个。若要在参数*内部*包含空格，有三种方法：

| 你输入                     | 命令得到                | 原因                                        |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | 一个参数，`two words`       | `\ `（反斜杠加空格）表示一个空格         |
| `/search-parts "two words"`  | 一个参数，`two words`       | 双引号会把其中的全部内容保持在一起 |
| `/search-parts say\ "hi"`    | 一个参数，`say hi`          | 引号可以从参数的中间开始 |

其他特殊字符：

| 你输入                      | 你得到                    |
|-------------------------------|----------------------------|
| `\\`                          | 一个反斜杠                |
| `\"`                          | 一个双引号，无论是否用引号括起：`"say \"hi\""` 得到 `say "hi"` |
| `""`                          | 一个空参数              |

反斜杠放在其他任何字符前面时保持原样，因此 `C:\books\story` 会原样传递。如果你打开了引号却没有关闭，那么从引号开始到行尾的所有内容都属于该参数。

参数都是文本。需要数字、是/否或几个选项之一的命令会自行转换文本；如果文本不符合要求，命令不会运行，并会有通知告知你。

<a id="forms-of-a-command"></a>

## 命令的形式

同一条命令可以接受不同的参数：`/list-parts`（不带参数）与 `/list-parts 2`（带一个参数）是同一命令的两种**形式**。Marginalia 根据你输入的参数个数及其形态来选择形式。如果没有形式与之匹配，会有通知说明该命令不接受这些参数，帮助窗口会显示它实际支持的形式。

<a id="the-result"></a>

## 结果

命令返回内容时，结果会显示在命令行上方的窗口中。你可以点击叉号或按 **Esc** 关闭它，也可以直接继续操作：当你输入新内容或运行另一条命令时，它会消失。不返回任何内容的命令不会显示窗口。

![命令行上方 /take-backup 的结果](../../images/command-result.png)

如果命令失败，或者命令不存在，你会收到一条通知，说明原因。

<a id="commands-that-take-time"></a>

## 耗时的命令

大多数命令会立即返回结果。需要一定时间的命令会显示一个带动画进度条和 **Stop** 按钮的窗口。该窗口位于页面一侧，页面仍可继续使用；点击其中的 ⌄ 可以将它折叠进[运行面板](../books/story-editor.md#the-panel-of-the-runs)，在那里可以看到该命令及模型正在写的内容，并且在你所有的页面上都能查看。进度条下方，命令可以说明它当前正在做什么，并用较小的字体显示该步骤的详情。**Stop** 会取消命令，并应立即返回。让模型编写摘要或笔记的命令（如 `/generate-annotation`）就属于这一类。

在故事的命令栏中输入的 `/next`、`/swipe` 和 `/regenerate` 没有这样的窗口：新的部分会像按下圆形按钮时那样流式写入故事，圆形按钮会停止生成，并且故事会像任何生成过程一样被暂时锁定（你仍可以在设定集、其他部分和侧边标签页中继续工作）。该部分完成后，命令的结果会显示在命令栏上方。在此之前，命令栏会拒绝第二条此类命令（你的文字会保留，之后可以再运行）；其他命令仍可正常使用。

![正在运行的命令窗口](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## 修改数据的命令

每条命令都会说明它对已有数据做了什么，命令栏会在命令旁、列表和帮助中以小标签的形式显示：

- **Destructive**（红色）——命令会更改或删除已有数据，例如重写整本书的搜索和替换命令。将鼠标指向标签可查看说明。
- **Non-destructive**（绿色）——命令只读取数据或添加新数据，例如 `/take-backup`，它只添加一个备份，不改动其他任何内容。

该标签只是提供给你的信息；运行命令时它不会阻止命令执行。[智能体](../agents.md)会依据它行事：根据该智能体的设置，它不会获得具有破坏性的命令，或者必须先询问你，或者直接运行。命令自身会要求确认的情况，见下文。

<a id="commands-that-ask-first"></a>

## 先询问的命令

会大幅改动内容的命令可能会先询问你。搜索和替换命令没有预览功能，因此搜索后它们会显示找到了多少处匹配、分布在多少个条目中，以及将被替换成什么内容；只有点击 **Yes** 才会修改书籍，点击 **No** 则保持所有内容不变。问题会一直显示，直到你作出回答。

![/search-replace-parts 的确认提问](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## 不带参数的命令

| 命令                              | 作用                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 在当前打开的故事中搜索并替换，与故事标签页的**搜索和替换**窗口相同。选项为 `true`/`false`，按此顺序排列；省略的选项使用该窗口的默认值（[正则表达式](../regular-expressions.md) 否，区分大小写 是，摘要 是，所有分支 否，重新验证 是）。要设置其中一个选项，也必须在它前面给出所有前面的选项。若查找到的匹配项超过 `maxHits` 的限制，替换会被拒绝；使用 `dryRun` `true` 时不会更改任何内容：回答会统计匹配数并显示部分结果，代理通常借此在替换前先查看情况。没有预览功能：搜索后会询问是否替换所找到的全部匹配项，只有点击**是**才会更改书籍。替换操作无法中途停止。 |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 对当前打开的书籍的词条库中的条目（名称、文本和注释）执行相同操作。 |
| `/take-backup`                       | 为当前打开的书籍创建备份；备份会出现在书籍的**备份**标签页中。若没有打开的书籍，则提示没有打开书籍。 |
| `/get-book-info` | 当前打开书籍的名称、设置、模型、总计（部分、字数、词元）和标签。 |
| `/list-parts [branch] [offset] [limit]` | 分支中各部分的信息：句柄、位置、大小、备选版本、摘要状态以及简短预览。 |
| `/get-part <part> [branch] [offset] [length] [field]` | 分段显示某个部分的文本（或其 `instructions`、`scene`、`pov`、`present` 或 `reasoning`），以及它的生成来源。 |
| `/list-swipes <part> [branch]` | 某个部分的备选版本（备选项），并标明显示每个版本的分支编号。 |
| `/list-branches [offset] [limit]` | 书籍的分支，按编号列出，并说明各分支与其他分支的分叉位置。 |
| `/get-part-metrics <part> [branch]` | 某个部分的词元、耗时、模型和协议。 |
| `/show-prompt <part> [branch] [offset] [length]` | 生成某个部分时所用的提示，按保存时的原样显示。 |
| `/search-parts <query> [limit]` | 对所有分支的部分进行全文搜索，并附带片段。 |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 名称或词语出现的频率和位置（默认值：不使用正则表达式、不区分大小写、仅搜索部分、当前分支）。 |
| `/compare-parts <a> <b> [branchA] [branchB]` | 两个部分之间不同的句子；`0` 与 `b` 相同，但将部分 `a` 与模型最初生成的文本进行比较。 |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | 一段文本（若文本为句柄，则为某个部分）的词元（使用书籍模型的分词器计算）、字数和字符数。 |
| `/list-summaries [branch]` | 某个分支的摘要、摘要所涵盖的部分，以及摘要是否仍然有效。 |
| `/get-summary <part> [branch]` | 某个部分的摘要。 |
| `/check-summaries [branch]` | 已过时的摘要，以及使其过时的已编辑部分。 |
| `/get-template-variables <scope> [part] [branch]` | 书籍（`global`）或某个部分（`part`）的模板变量。 |
| `/list-lorebooks` | 当前打开书籍的词条库（根词条库及其子库），包括编号、条目数量、标签和子库。 |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | 某个词条库的条目，包括设置和预览；筛选器会保留名称或标签中包含该关键词的条目。 |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 单个条目：设置、关键词、激活方式（递归、上下文、部分范围、条件）、标签、注释，以及分段显示的文本。 |
| `/search-lore <query> [regex] [limit]` | 书籍所有词条库中，名称、文本或注释包含查询词的条目，并附带片段。 |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | 对于某段文本，或对于与生成某个部分时类似的一轮生成，说明生成时会使用哪些条目以及原因（无筛选条件；已在文本中找到筛选词；或在词条库设有[扫描深度](../lorebooks/entries.md#entries-brought-in-by-other-entries)时，在其他条目的内容中找到；或因已关闭而未使用；或书籍的某个标签决定了结果；或文本中没有筛选词），并显示词条的内容及其词元数。该文本可以是某个部分的句柄。 |
| `/list-annotations [status [part [offset [limit]]]]` | 书籍或某个部分的[批注](../books/annotations.md)：默认显示未解决的批注，除非 `status` 指定 `resolved`、`dismissed`、`orphaned` 或 `all`。 |
| `/get-annotation <annotation>` | 单条批注的完整内容，以及其所在段落的前后文。 |
| `/get-book-statistics [range] [side]` | 当前打开书籍每天的操作记录（词元、字数、部分、编辑、耗时、大小）：参见[写作统计](../books/statistics.md#commands)。 |
| `/get-statistics [range] [by] [side]` | 所有书籍的相同统计，可按天或按书籍显示。不需要打开书籍。 |

<a id="reading-the-story"></a>

### 阅读故事

上述读取书籍的命令（`/get-book-info` 至 `/get-template-variables`）不会更改任何内容；它们以 JSON 文本形式回答，因此代理也可以使用它们。回答中的每一行都有一个 **`handle`**：即部分（或词条库、条目）的标识符，在添加、移动或删除部分时不会改变。部分由其句柄或其在分支上的**位置**指定，故事的第一个部分的位置为 `1`；分支可由其中任一部分的句柄指定，或由其**编号**指定（编号范围为 `/list-branches` 起，编号按分支末端的创建顺序分配，因此添加或删除分支时编号会改变）。位置和编号更便于手动输入，而代理应使用句柄；修改故事的命令只接受句柄。若未指定分支，命令默认作用于当前活动分支。列表通过 `offset` 和 `limit` 分页显示，长文本会被切分为若干片段（`offset`、`length`），回答中会注明文本总长度。与其他命令一样，选项按位置确定：若要设置其中一个选项，也必须在它前面给出所有前面的选项。

读取词条库的命令（`/list-lorebooks` 至 `/preview-lore-activation`）工作方式相同，且仅能访问当前打开书籍的词条库：即根词条库及其子库。词条库可由其**句柄**或其在 `/list-lorebooks` 起的**编号**指定（根词条库为 `1`）；词条可由其**句柄**或其在词条库中的**条目编号**指定，条目按词条库中的顺序排列（第一个条目为 `1`）；添加或删除条目或子库时，编号会改变。若未指定词条库，则使用根词条库。
<a id="changing-the-story-and-the-lore"></a>

### 修改故事与设定

这些命令会修改当前打开的书（或智能体的工作副本，智能体的修改都写入这里）。部分、设定集或条目始终通过其**句柄**指定，而不是通过位置或编号。位置和编号会在添加或删除内容时变动，读取时正确的位置，写入时可能已指向另一个部分。选项仍像以前一样按位置给出。删除文本或数据的命令为 `DESTRUCTIVE`，会要求你确认（智能体则由其破坏性模式处理）；添加和修改的命令不需要确认。修改故事可能导致摘要过时（摘要带有其所描述故事的哈希值）：回答中会列出这些摘要，并说明如何处理，仍然准确时使用 `/revalidate-summaries`，不再准确时使用 `/regenerate-summary`。

| 命令 | 作用 |
|---------|--------------|
| `/edit-part <part> <old> <new> [field]` | 在某个部分的文本中，把 `old` 替换为 `new`；`old` 必须恰好出现一次，否则什么都不改，并且错误信息会说明它出现了几次。`field` 则改为编辑该轮次中第 `instructions`、`scene`、`pov` 或 `present` 个字符。 |
| `/replace-part-text <part> <text> [field]` | 替换某个部分的全部文本。会先要求确认。 |
| `/append-to-part <part> <text> [separator]` | 在某个部分的末尾添加文本，默认先空一行，除非 `separator` 另有指定。 |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | 修改某个部分的生成来源（新一次生成会沿用这些内容）；传入空文本会清空某个字段，未指定的字段保持不变。 |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 在当前分支的最后一个部分之后写入新部分，或在 `parent` 之后写入（即新分支）；新部分成为当前分支的末尾。 |
| `/insert-part <text> <part> [before]` | 在 `part` 之后立即把新部分插入故事（若 `before` 为 `true`，则插在它之前）。该位置之后的所有内容（包括所有分支）现在都排在新部分之后；不会删除或重新生成任何内容，因此其他部分保留各自的摘要和数据，并向后移动一个位置。覆盖该位置的摘要会变为过时（回答中会说明是哪些）。若位于分支末尾，则等同于 `/create-part`。 |
| `/branch-part <part>` | 在某个部分旁边复制该部分（连同其摘要），作为一个开启新分支的备选版本，并将副本设为当前末尾。 |
| `/delete-part <part>` | 删除某个部分及其摘要；其后的部分保留。会先要求确认。 |
| `/switch-branch <part>` | 将经过某个部分的分支设为当前分支。 |
| `/set-summary <part> <text>` | 替换某个部分上摘要的文本。 |
| `/remove-summary <part> [unwind]` | 删除某个部分的摘要（`unwind` 可恢复被元摘要取代的内容）。会先要求确认。 |
| `/revalidate-summaries [part]` | 声明过时的摘要仍然准确描述故事（可针对整个当前分支，或仅针对该部分）。仅在你已核对后使用。 |
| `/regenerate-summary <part>` | 让模型重新为某个部分生成摘要。速度慢，且消耗 token。 |
| `/create-meta-summary <from> <to>` | 让模型把从部分 `from`（最新）逆向直至 `to` 的摘要合并为一份。速度慢，且消耗 token。 |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | 让模型撰写下一个部分，效果与 **New turn** 按钮相同。速度慢，且消耗 token；同一本书一次只运行一个生成任务，其他任务排队等待。`profile` 指定智能体的某个模型配置。 |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | 让模型为分支的最后一个部分再写一个版本。未指定的项目沿用该部分的原有设置。 |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | 让模型重新撰写分支的最后一个部分，并替换其文本。会先要求确认。 |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | 根据你的指示让模型重新撰写任一部分（见[重写某个部分](../books/story-editor.md#redrafting-a-part)），并替换其文本。`prompt` 为用于替代本书设定的重写提示词，`profile` 为智能体的某个模型配置，`apply` `false` 仅返回文本而不替换。`DESTRUCTIVE`；会先要求确认。速度慢，且消耗 token。 |
| `/impersonate [instructions] [outline] [prompt]` | 让模型代你起草下一轮的指令（见[起草指令](../books/story-editor.md#drafting-the-instructions)）并返回文本；不改动任何内容。`instructions` 为该部分应围绕的内容（为空则顺着故事走向继续），`outline` 为提供给模型的故事计划，`prompt` 为用于替代本书设定的 impersonate 提示词。仅供你在命令栏中使用：[智能体](../agents.md)不提供此命令。速度慢，且消耗 token。 |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 修改本书的描述性设置；只更改你提供的值。 |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 替换本书的提示词模板。每个模板都会先经过检查。`DESTRUCTIVE`：有问题的模板会破坏之后的每一次生成。 |
| `/set-book-tags [add] [remove]` | 添加或移除本书的标签（以逗号分隔）。 |
| `/set-book-history-limit [maxVersions]` | 设置本书的[历史记录](../books/version-history.md#how-many-versions-are-kept)为每个部分保留多少个版本（`0` 表示从现在起不保留任何版本）；不指定数字则本书不设上限。文件夹、用户及管理员设定的上限仍然有效。不会删除任何版本。 |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | 向设定集中添加条目（未指定时添加到根设定集；没有设定集的书会自动创建一个，名称与本书相同）。`keys` 为以逗号分隔的关键词列表；`filteringMode` 为 `KEYWORDS_OR`（默认）、`KEYWORDS_AND`、`TEXT` 或 `REGEX`。模板或正则表达式的问题会被保存并报告。 |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 修改你提供的字段。 |
| `/delete-lore-entry <entry>` | 删除一个条目。会先要求确认。 |
| `/move-lore-entry <entry> <lorebook>` | 将条目移动到本书的另一个设定集。 |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | 修改条目的标签和否定标签（以逗号分隔）。 |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | 修改条目的使用时机：`preventRecursion`、`recursionOnly` 和 `activationSummaries`（`true` / `false`）、`activationContext`（`INSTRUCTIONS` 或 `INCLUDE_PARTS`）及 `activationParts`、部分范围 `fromMessage` / `toMessage`（0 表示取消限制）以及 `condition` 模板（空文本则删除该模板）。以空文本给出的值保持不变（`condition` 除外）。 |
| `/create-lorebook <name> [parent]` | 创建一个作为 `parent` 子设定集的设定集（未指定时以根设定集为父级；没有设定集的书会先自动创建一个）。 |
| `/set-lorebook <lorebook> [name] [enabled]` | 重命名设定集，或开启/关闭设定集。 |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | 修改一个设定集所包含的设定集；设定集不能包含它自身。对于[智能体](../agents.md)，`/add-subbook` 还可以恢复它之前删除或从父级移出的设定集（按其原有的句柄指定），连同其条目一起恢复。 |
| `/delete-lorebook <lorebook>` | 删除一个设定集及其条目（根设定集除外）。会先要求确认。 |
<a id="annotations"></a>

### 批注

读取[批注](../books/annotations.md)的命令（`/list-annotations`、`/get-annotation`）与其他读取命令一样以 JSON 形式返回结果；
批注通过其 **handle** 指定，对于会修改内容的命令，段落也通过其 handle 指定。这些命令只修改批注，
从不修改故事，因此只有删除操作会先询问。智能体可以使用所有这些命令；当你[审阅结果](../books/annotations.md#annotations-and-agents)时，它们的批注会进入你的书中。

| 命令 | 作用 |
|------|------|
| `/create-annotation <part> <text> [quote [type]]` | 为一个段落添加批注。`quote` 是段落的文字，从该段落中复制：批注附着在这段文字上，并在文本被编辑时随之移动。没有引文，或引文不在文本中时，批注针对整个段落（回答会说明这一点）。 |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 修改批注的内容、类型，或是否把批注提供给模型；空的文本或类型保持不变。 |
| `/resolve-annotation <annotation> [status]` | 设置状态：`resolved`（默认）、`dismissed`或`open`。 |
| `/reattach-annotation <annotation> [quote]` | 将批注移动到引文在其段落中所指向的位置；没有引文时，批注针对整个段落。 |
| `/delete-annotation <annotation>` | 删除批注（可以从回收站恢复）。会先询问。 |
| `/scan-phrases [from to]` | 按书籍的[短语监控列表](../books/annotations.md)检查当前分支的各段落，并为每个新命中添加一条批注（不使用模型）。不带参数时检查整个分支；给出两个段落时，检查从一个到另一个之间的段落。回答会说明新建了多少条批注。 |
| `/generate-annotation <part> [profile]` | 让模型像 [Annotate with AI](../books/annotations.md#letting-a-model-annotate) 那样审阅该段落，并将其发现的所有内容添加为批注（来源为 *AI*；不在文本中的引文会使批注针对整个段落）。不会询问：批注是唯一的改动，可以被标记为已解决、编辑或删除。它使用书籍的模型进行审阅；当 *Write the book with the model of the agent* 开启时，智能体使用其自身的模型（Lore Generator 的命令同样如此），`profile` 是智能体的一个模型配置。命令会单独运行，耗时较长，并消耗 token。 |

<a id="the-planner"></a>

### 规划器

书籍[规划器](../books/story-editor.md#the-planner-tab)的命令。

场景通过其在列表中的 **编号** 指定（从 1 开始，`/get-planned-scenes` 会列出它们）；场景被移动或删除后编号会变化，因此更改后请重新读取列表。这些命令只改变规划器，从不改变故事。添加、移动和将场景标记为完成不会询问；修改场景文本和删除场景则具有破坏性（[智能体](../agents.md)与其他破坏性命令一样会被询问）。

| 命令 | 作用 |
|------|------|
| `/get-planned-scenes` | 列出场景及其编号、标题、场景设定、视角人物、在场人物、说明，以及是否已完成。 |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 在列表末尾添加一个场景。 |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 修改标题和给出的细节；未给出的内容保持不变，空文本则清除该项。 |
| `/set-planned-scene-done <scene> [done]` | 将场景标记为已写（`done` 即 `true`，为默认值）或未写。 |
| `/move-planned-scene <scene> <to>` | 将场景移动到位置 `to`（1 为第一位，位置过大时移到最后）。 |
| `/delete-planned-scene <scene>` | 删除场景。 |

让模型写作或审阅的命令会耗时：`/generate-annotation` 和两个摘要命令会显示 **Stop** 窗口，
`/next`、`/swipe` 和 `/regenerate` 会将内容流式写入故事（参见[耗时的命令](#commands-that-take-time)）。

<a id="commands-that-come-with-extensions"></a>

## 扩展提供的命令

只要加载了扩展，你就能在列表中看到这些命令；每个命令都在其扩展的文档中有所说明。

| 命令 | 扩展 | 作用 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 你保存的生成提示词，或其中某一个的文本。 |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | 利用书籍的设定生成一条设定条目的文本，压缩它（除非 `compact` 为 `false`），然后返回。不保存任何内容。 |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 将生成的文本压缩为紧凑的设定条目文本并返回。不保存任何内容。 |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 虚构名字的类别，以及其中某一类别的名字。 |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 真实名字的列表，以及从其中某个列表挑选的名字。 |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 真实地点的类型、按名称、类型和国家搜索、某点周围的地点，以及两个地点之间的距离。 |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | 大纲中的篇章和章节。 |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | 添加、修改、移动和删除一个篇章（`<arc>` 为其编号）。 |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | 对篇章中章节的相同操作。 |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | 篇章标签页的生成器。它们返回结果，只有使用 `save` 时才会将其添加到大纲中。 |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 书中的人物列表，以及某个人物及其备注和关系。 |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 添加、修改和删除人物。 |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 人物之间的关系。 |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 书籍笔记的页面（不包括对助手隐藏的页面），某一页及其文本和链接，以及搜索。 |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | 添加、追加、修改、编辑和删除页面；`note-text` 是为 {%{`{{command::note-text::Title}}`}%} 宏所做的。 |

Lore Generator、Name generator、Name provider 和 Place provider 的命令只读：生成类命令返回文本，由你或调用它的智能体决定是否将其放入条目。Arc Outline 和 Character Tracker 的命令还会修改当前打开的书的大纲和人物；只有它们的 `delete` 命令具有破坏性。Story Notes 同样会修改当前打开的书的页面；其中 `update-note` 和 `edit-note` 同样具有破坏性，因为它们会替换已有的文本。

<a id="limits"></a>

## 限制

- 命令行位于桌面布局的故事标签页中。[手机视图](../phone.md)尚不支持它。
- 命令以你的身份、在你的会话中运行，并作用于你当前打开的书。
