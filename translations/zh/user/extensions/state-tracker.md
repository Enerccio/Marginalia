---
label: 状态追踪器
order: 29
---

# 状态追踪器

记录角色当前的**状态**：他们穿着什么、携带什么、手中拿着什么武器、有什么伤势、情绪如何，或你定义的任何其他信息。它会把场景中角色的状态放入提示词。模型很容易忘记剑在两章前已经丢失，或者斗篷已被雨水浸透。使用状态追踪器后，提示词会针对场景中每个被追踪的角色说明其当前的真实状况。

!!!
你决定追踪哪些角色，以及追踪他们的哪些信息。只有你添加到该标签页中的角色才会被描述给模型，并且只有在他们是正在撰写部分的*在场角色*时才会如此。你可以自己维护状态，也可以让模型阅读故事并提出变更建议（参见[跟随故事](#following-the-story)）。
!!!

<a id="categories"></a>

## 类别

**类别**是可以为角色追踪的一项信息。每个类别包含：

- 一个**名称**，也是提示词中对它的称呼（*Wearing*、*Weapon at hand*、*Carrying*）；
- 一个**种类**：
  - **Single value**：一个简短的值，新值会替换旧值（手中的武器、位置）。
  - **List**：逐条添加和移除的条目（随身携带的物品、伤势）。
  - **Description**：整段文字，每次整体重写（衣着、情绪）。
- 一条**给模型的说明**：哪些内容属于该类别，哪些不属于。模型在跟随故事时会读取它。
- **仅依据原文**：开启时，只有原文明确说明时值才会改变；关闭时，也会根据明显的后果改变（游泳后变湿）。
- **空时写“none”**：角色没有某个值时（武器丢失），提示词会写 *none*，而不是省略该类别。

<a id="my-categories"></a>

### 我的类别

你自己的类别位于 **Settings → Extension Settings → State Tracker**。书籍从这里复制其类别。在你修改之前，系统内置了五个类别：*Wearing*、*Weapon at hand*、*Carrying*、*Injuries* 和 *Mood*。你可以在那里添加、编辑和删除类别，并在设置页面中按 **Save** 保存。**Reset to the built-in categories** 会恢复内置的五个类别。

<a id="the-categories-of-a-book"></a>

### 书籍的类别

书籍会为其使用的每个类别保存一份**自己的副本**，因此书籍的备份或导出包含所有内容，删除你的某个类别也不会影响书籍。状态标签页中的 **Categories of this book** 按钮会列出这些类别：

- **Add from my categories** 将你的某个类别复制到书籍中。
- **New category for this book** 创建仅存在于本书中的类别（如船只货物、魔法契合度）。此类类别会标记为 *Only in this book*。**Save to my categories** 会将其添加到你的类别中，以便其他书籍也能使用。
- 与你的类别不再相同的副本会标记为 *Differs from your category*。**Update from my categories** 会采用你的版本。任何更新都需要你确认，因为指令的改变会改变模型的行为。
- 编辑类别只会改变本书中的该类别。当角色已有该类别的值时，其种类无法更改。
- 从书籍中删除某个类别，会删除所有角色在该类别中的值。

<a id="the-state-tab"></a>

## 状态标签页

[故事编辑器](../books/story-editor.md)的左侧边栏会出现 **State** 标签页。你所做的每项更改都会即时保存到书籍中。

![带有已追踪角色的 State 标签页](../../images/plugin-statetracker.png)

![某个值的历史记录](../../images/plugin-statetracker-history.png)

**Track a character** 会要求填写 **name**，可选填 **aliases**（角色在某一部分的 *Present Characters* 字段中所用的其他名称，用逗号分隔），以及 **tracked categories**。列表中会显示书籍中的类别和你自己的类别；选择你的类别会将其复制到书籍中。一匹马可以只追踪 *Injuries*，而女主角则可以追踪全部类别。

**Import from the Character Tracker**（**Track a character** 旁边的人形图标）会开始追踪同一本书的[角色追踪器](character-tracker.md)中的角色。它会列出书籍尚未追踪的角色（以名称或别名计）及其标语，默认全部勾选；取消勾选不需要的角色，检查每个导入角色将获得的 **tracked categories**（书籍的和你自己的，如上所述），然后按 **Import**。只会导入名称；之后可以用笔形按钮为角色添加其他类别。

每个被追踪的角色都有一张卡片，初始为折叠状态：点击卡片上的箭头即可展开，查看其各类别及当前值：

- **+** 为单值或描述类别设置值（当前值会移入历史记录），或为列表添加一个条目。
- 笔形按钮用于编辑某个值，叉号用于**移除**它：该值不再成立，因此会移入历史记录，且不会发送给模型。
- 锁形按钮会将某个值标记为 **locked**（如伤疤、家传之剑）。[整理](#tidying-the-state)功能不会改动已锁定的值；但故事仍可以改变它们。
- **History** 显示该类别过去的值及其结束原因（*replaced*、*removed* 或 *tidied*），若结束是由模型完成的，还会显示其读取到该结果的部分。**Restore** 会让某个过去的值重新生效（对于单值或描述类别，当前值会移入历史记录，取代其位置）。Delete 会永久删除某个值。

角色的笔形按钮用于编辑其名称、别名和类别。取消勾选某个类别会将其隐藏，但保留其值；重新勾选即可恢复。垃圾桶图标会停止追踪该角色，并删除其全部状态。
<a id="following-the-story"></a>

## 跟进故事

State 标签页顶部的 **Processing** 面板可以让模型阅读故事，找出发生了哪些变化：斗篷被河水浸湿、提灯交到了别人手里、长剑出鞘、一封信遗失了。它按顺序阅读当前分支中的各个部分，从 **Start point** 开始（默认是全书开头，起点之前的部分从不会被阅读），并记住已经读到哪里。

![处理设置](../../images/plugin-statetracker-settings.png)

![对拟议变更的审阅](../../images/plugin-statetracker-review.png)

- **Process now** 阅读尚未处理的所有部分，**Process next part** 只阅读下一个部分。如果要阅读的部分很多，程序会先询问需要发出多少次请求。当你向下滚动角色列表，越过这些按钮时，顶部栏中也会提供 **Process now**、**Process next part** 和 **Tidy the state** 按钮。
- 模型会获得这些部分中出现的被追踪角色，以及每个角色的类别、说明和当前值，然后返回需要做出的变更。每项变更都必须附上来自该部分的引文。不符合要求的变更会被略去，例如：角色并不追踪的类别、角色已经具有的值，或者引用了一个不存在的部分。
- 面板会显示进度，以及模型在写作时输出的内容。你可以在此期间继续阅读和写作。如果开启了 **Ask before applying**（默认开启），运行会等待你的确认：**Review changes** 会按角色列出变更，结束值会以删除线标出，并显示新值（你可以修改）以及引文。取消勾选有误的变更，然后按下 **Apply**；按下 **Cancel** 则保持一切不变，下次还会重新提出同样的部分。
- 模型读取的每个值都会标明它来自哪个部分。**Processed parts** 列出已经读取的部分；**Reprocess from here** 会撤销从该部分及之后所有部分读取到的内容（你写入或修改过的值会保留），然后重新阅读这些部分。在你改写了较早的某个部分之后，应当使用它。重新生成某个部分、切换候选版本或切换到另一分支时，会自动撤销那些已不存在的部分所带来的内容。

<a id="the-story-clock"></a>

### 故事时钟

开启 **Story clock**（在设置中，默认关闭）后，模型还会追踪故事的时间。它与状态在同一次请求中，为每个部分报告该部分所述的时间，例如：*第二天早上*、*三天后*、*那天夜里*、*你醒来后的第四十天*、*四月二十日*，或者含糊的*几周混在一起了*（会标明为大致时间），也可能是闪回、回忆或对过去的梦境。这类内容不会推进时钟，也不会改变状态。模型本身不做计算，由插件把天数累加起来。

![处理设置中的故事时钟](../../images/plugin-statetracker-clock.png)

![手动修正某个部分的时间](../../images/plugin-statetracker-clock-dialog.png)

- **Day 1 is the day of** 指定计算天数所从的事件（例如*觉醒之日*），于是时间会显示为*觉醒后第 14 天，傍晚*。**Day at the start point**、**Time of day** 和 **Date at the start point** 用来设定第一个被阅读的部分之前的时间；如果设置了日期，时钟还会累计日期（例如*1453 年 4 月 20 日*）。
- Processing 面板现在会显示 **story time**，每个已处理的部分也会显示其之后的时间。明确说明的天数或日期会取代先前的推算。
- 某个部分的时钟按钮（若是最后一个部分，则是故事时间那一行上的按钮）可以**手动设定时间**：设定一个固定点（天数、时段、日期），之后重新阅读该部分时不会被取代，时钟将从这个固定点继续计算。同一对话框中的 **Remove the fixed point** 会让模型的阅读重新开始计算。
- 在审阅中，时间排在最前面，标题为 **Story time**，并附上每个部分的引文。如果有误，请取消勾选。
- 提示词块以时间开头（*Story time: day 14 after the awakening, evening.*），即使场景中没有被追踪的角色，也是如此。
- 一个值会记住设定时所处的故事天数（*第 4 天*）。整理状态时，程序会看出它已经过去多久（*一杯茶，9 天前设定*），并删除已有足够时间淡化的内容。
- 开启时钟后，所有部分都会发送给模型，包括没有被追踪角色的部分，因为任何部分都可能推进时间。
- 重新处理、重新生成或切换到另一分支，会撤销从已不存在的部分读取到的内容，与状态的处理方式相同；你设定的固定点则会保留。

<a id="the-log-of-automatic-changes"></a>

### 自动变更日志

未经审阅直接应用的变更（在 **Ask before applying** 关闭时的运行、自动整理，或不带 `ask` 的命令）会被记录到 Processing 面板中的 **Applied automatically**：包括由谁做出、属于哪个类别、来自哪个部分（或 *tidied*），以及具体改变了什么（*+ lantern*、*− rope*、*short sword → axe*）。日志中的内容都不能编辑，但每一行都有一个 **Revert** 按钮。它的作用相当于你在审阅中取消勾选了该变更：该变更写入的值会被删除，变更之前的值重新成为当前值。如果你之后手动修改过该值（编辑、删除、恢复或删除了它，或为某个单独类别设定了另一个值），该行将保持不变。日志只保留最近 200 条变更。

![自动变更日志](../../images/plugin-statetracker-log.png)

<a id="returning-to-an-earlier-point"></a>

### 回到较早的节点

每次会应用变更的运行之前（无论是否经过审阅的标签页运行、新部分的自动阅读、命令，还是整理），追踪器都会创建一个**检查点**：记录运行开始时其角色的完整状态、故事时钟，以及它已读到的位置（首次运行前为 *Start of the book*）。Processing 面板中的 **Checkpoints** 会按从新到旧的顺序列出这些检查点：包括追踪器读到的部分、时间、下一次运行的类型，以及它应用了多少项变更。只有其所在部分位于当前分支上的检查点才会被列出。

![撤销的变更](../../images/plugin-statetracker-reverted.png)

点击某一行上的 **Return to this point** 会撤销该次运行及其之后的所有运行：角色的值（当前值和过去的值）、故事时钟的标记、已处理的部分，以及自动变更日志，都会恢复为检查点时的状态。下一次运行会重新阅读该检查点所在部分之后的部分，包括被撤销的那次运行所涉及的部分。追踪器原有的状态会被保存为一个检查点（*before a return to a checkpoint*），因此你可以再回到它。你的设置、起点、时钟设置以及本书的类别保持现状（如果你之后删除了某个类别，而检查点中的值使用了它，该类别会重新出现）。由于检查点之后读取的所有内容都会被撤销，包括那之后你手动做出的变更，系统会先询问确认。当一系列运行出了问题，或者你恢复了某个较旧的书籍备份，并希望追踪器从你信任的某个节点重新开始追赶时，就可以使用它。

追踪器会为每本书保留最近 20 个检查点。它们保存在你的文件中（*Resources*，按书籍分组），而不在书籍内部，因此书籍备份和副本不会包含它们。

<a id="tidying-the-state"></a>

### 整理状态

有些状态会在故事中没有明确交代的情况下逐渐消退，例如情绪、被打湿的衣服、气喘吁吁，或吃完饭后很久仍握在手中的杯子。Processing 面板中的 **Tidy the state** 会请模型处理这些内容。它会读取最近处理过的部分，以及出现在这些部分中的被追踪角色的当前值，然后对每个值可以执行以下操作：

![整理的审阅](../../images/plugin-statetracker-tidy-review.png)

- **drop**（删除）：它已经不再成立，或者不值得保留；
- **update**（更新）：部分内容显示它已经发生变化（愤怒已经转为释然）；
- **merge**（合并）：将指代同一事物的条目合并（*rope* 和 *coil of rope*）。

模型未提及的值保持不变，被锁定的值也保持不变。每项变更都附有理由。整理写入的值（更新或合并）会标记为 *by the tidy*，并附上它所读取的最后一个部分，因为故事本身并未说明该内容；如果你编辑它，它就会变成你的内容。整理与其他运行一样需要审阅（或者在 **Ask before applying** 关闭时立即应用）。整理结束的值会以 *tidied* 的形式进入历史记录；在整理之前最后读取的那个部分上使用 **Reprocess from here**，即可撤销该整理。那些不在最近部分中出现的角色不会受影响，因此一个由某位角色携带、而该角色已经二十章未出场的绳子，仍会保留。

开启 **Tidy automatically**（默认开启）后，当一次不经询问直接应用变更的运行结束时，整理也会自动运行，前提是自上次整理以来已经读取了 **Tidy after this many parts** 个部分（默认 10 个）。你需要审阅的运行永远不会启动整理，因此不会强迫你进行第二次审阅。

在设置（齿轮图标）中：

- **Process new parts automatically**：某个部分定稿后，模型会阅读它（最迟在下一次生成之前）。若 **Ask before applying** 开启，则只有在你按下 **Process now** 时才会阅读，因此写作过程中绝不会强迫你进行审阅。
- **Ask before applying**：关闭时，变更立即应用并写入日志（见下文）。
- **Parts per run**：一次请求阅读多少个部分。
- **Tidy automatically** 和 **Tidy after this many parts**：参见[整理状态](#tidying-the-state)。整理还会阅读最近已处理的这么多个部分。
- **Lorebook for the model**：提供给模型的设定集条目，可以是部分激活的条目（默认）、整个设定集，或无。
- **Model for the extraction** 和 **Protocol for the extraction**：留空时使用本书的模型。
- **Use these settings for new books** 会把本书的设置（包括模型和协议，分隔符仍归本书所有）保存为从未使用过状态追踪器的新书的初始设置。**Forget the settings for new books** 则恢复为内置设置。已经使用追踪器的书籍保留它们自己的设置。
<a id="prompts"></a>

## 提示词

读取的系统消息（**State extraction prompt**）和整理的系统消息（**State tidy prompt**）是[继承设置](../books/prompts.md#inherited-settings)：书籍优先使用自己的设置；若没有，则使用其上方最近一个设置了该项的文件夹的设置；若仍没有，则使用你的全局设置；再没有，则使用你所用提示语言的内置文本（英文版本经过了真实模型的测试）。

在书籍的 *Prompts* 标签页中、*Extension Settings* 下设置它们，也可以在文件夹的对话框中设置，或在 *Settings → Extension Settings → State Tracker* 中为你所有的书籍统一设置（留空时会以灰色显示内置文本）。请保留内置文本的 JSON 回答格式：插件依此格式读取回答。当书籍跟随时间线时，插件会在提取提示词之后添加[故事时钟](#the-story-clock)的规则，因此你的提示词无需包含这些规则。

<a id="in-the-prompt"></a>

## 在提示词中

齿轮按钮保存着本书的设置：

- **In prompt** 会在每次生成的提示词中插入本场景中被追踪角色的状态（开启时齿轮会变绿）。
- **Depth** 和 **Role** 决定它放置的位置，与作者注释相同：*Depth* 是其之后的消息数（0 表示位于本轮指令之后，1 表示恰好位于指令之前）。
- **Name separators** 决定 *Present Characters* 字段如何拆分（默认为 `,` `;` `/` `|` 以及换行）。方括号中的备注会被忽略，因此 `Mara (asleep), Fiora` 能同时找到两者。

提示词会包含场景中有话要说的角色：

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

你可以使用宏 {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} 在模板的任意位置插入相同的文本（参数可以是任意名字列表）。无论 **In prompt** 是否开启，该宏都会输出状态，因此使用宏时请将其关闭，否则模型会收到两次状态。

<a id="commands"></a>

## 命令

适用于命令栏和代理：

| 命令 | 作用 |
|---|---|
| `state-status` | 设置、本书的分类，以及你的、本书没有副本的分类。 |
| `list-state-characters` | 被追踪的角色及其分类。 |
| `track-state-character` | 开始追踪一个角色：名称、别名、按名称指定的分类。 |
| `stop-tracking-state-character` | 停止追踪角色并删除其状态（会先询问）。 |
| `list-character-state` | 角色的值，包括当前值和过去的值，并编号。 |
| `set-character-state` | 设置一个值：角色、分类、文本。 |
| `remove-character-state` | 按编号删除一个值：它会进入历史记录。 |
| `state-tracker-block` | 针对一组角色的提示词文本（即宏）。 |
| `state-process` | 让模型读取接下来的部分并应用变更；若使用 `ask true`，则改为返回带编号的变更。 |
| `apply-state-changes` | 应用 `state-process` 或 `state-tidy` 返回的变更，可全部应用，也可仅应用给定的编号。 |
| `set-story-time` | 在已处理的部分之后手动设置故事时间（天、时段、日期）；若三者都不给出，则移除固定点。 |
| `state-tidy` | 让模型删除、更新或合并已淡化的值；若使用 `ask true`，则改为返回编号后的变更。 |
| `state-reprocess` | 撤回从某一部分及其之后各部分读取的内容（会先询问）。 |
| `state-set-start` | 设置开始读取的部分。 |
