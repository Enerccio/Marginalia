---
label: 书籍提示词
order: 70
---

# 书籍提示词

书籍的 **Prompts** 标签页保存着 Marginalia 用来构建提示词的文本：两个模板、叙事设定，以及摘要和重写提示词。它们对书籍的文风影响远大于模型设置。

![Prompts 标签页](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## 默认值与书籍值

每个字段都是可选的。空字段会继承其值：先看书籍上方最近的[文件夹](folders.md)是否设置了该字段，若没有，则继承 **Settings → Templates** 中的默认值（参见[账户与设置](../account.md)）；若这里也为空，则使用内置默认值，其语言由你的设置中的[提示词语言](../account.md#prompt-language)决定。空字段中会以灰色显示它所继承的值。

因此，请在 *Settings* 中设置所有书籍共用的内容，在系列所在的文件夹中设置该系列共用的内容，只在书籍自己的 *Prompts* 标签页中填写该书特有的内容。修改会在你输入时自动保存。

<a id="inherited-settings"></a>

### 继承的设置

空字段下方的一行文字会说明其值的来源，便于你理解书籍为何是这样的写作风格：

- *Inherited from the folder "Sci-fi"*：由某个[文件夹](folders.md#where-the-settings-of-a-book-come-from)设置（会注明书籍上方最近的那个文件夹的名称），
- *Inherited from your settings*：来自 *Settings → Templates* 中的值，
- *Built-in default*：没有任何地方设置它，使用内置默认值。

![位于文件夹中的书籍的 Prompts 标签页](../../images/book-prompts-inherited.png)

文件夹的设置中也有相同的字段，并带有相同的说明行。空白文本视为未设置。由于查找是在构建提示词时进行的，因此把书籍移到另一个文件夹，或更改其所在文件夹，都会影响你接下来生成的部分。

<a id="settings-of-extensions"></a>

### 扩展的设置

[扩展](../extensions/index.md)可以添加一些设置，书籍和文件夹同样以继承的方式使用它们（例如，关于该书的聊天应以何种媒介来对待它）。这些设置位于 Prompts 下方的 **Extension Settings** 区域中，书籍的 *Prompts* 标签页和文件夹的设置中都有。每个扩展对应一个**可折叠面板**（默认全部折叠，但第一个已设置了值的面板会展开，以免你填写的值被隐藏）。它们的工作方式与上述字段相同：空字段先从书籍上方的文件夹继承，然后从 *Settings → Extension Settings* 中该扩展自身的设置继承，最后才使用其默认值，字段下方的说明行会指明具体来源。[导出格式](export.md#title-page-template)的模板（标题页、前置内容、著作权声明）也位于同一区域，每种格式各占一个面板。你为某个扩展设置的值，即使该扩展未被加载，也会保留在书籍或文件夹中。

每个字段都有一个提示弹出框，其中的 **Insert Default Template** 可以把默认模板复制到字段中，作为你自定义版本的起点。模板字段还会列出 **Available Template Variables** 和 **Available Macros**。

![模板字段的提示弹出框](../../images/template-hints.png)

<a id="the-fields"></a>

## 各字段

| 字段 | 用于 | |
|---|---|---|
| **Master Template** | 系统消息 | 提示词的框架：模型的角色，以及设定、叙事设定和摘要放置的位置。模板。 |
| **Point of View (POV)** | 主模板 | 叙事视角，例如 *First person*、*Third-Person Limited*。纯文本。 |
| **Tense** | 主模板 | 例如 *Past Tense*、*Present Tense*。纯文本。 |
| **Style** | 主模板 | 正文的文风与语气指南。纯文本。 |
| **Default User Prompt** | 最后一条消息 | 根据[回合指令](story-editor.md#writing-the-next-part)生成的、对下一部分的请求。模板。 |
| **Default Summary Prompt** | 摘要 | 对[摘要](summaries.md)的请求。模板。 |
| **Default Meta Summary Prompt** | 元摘要 | 对[元摘要](summaries.md#meta-summaries)的请求，元摘要用于合并多份摘要。模板。 |
| **Redraft System Prompt** | 重写 | [重写](story-editor.md#redrafting-a-part)的系统消息：指令、叙述和摘要。模板。 |
| **Redraft Prompt** | 重写 | 重写的最后一条消息，即请求本身：词条书给出的当前设定（因此对词条的编辑会影响重写；只有当书籍没有词条书时，才使用写作该部分时所用的设定）、该部分之后的故事、该部分以及指令。模板。 |
| **Redraft Shorter Prompt**、**Redraft Longer Prompt** | 重写 | 当[重写](story-editor.md#redrafting-a-part)窗口设为 **Shorter** 或 **Longer** 时，附加在你的指令之后的句子。纯文本，不是模板。留空则使用内置文本。 |
| **Annotate System Prompt** | 批注 | 请求模型对某个部分进行[批注](annotations.md#letting-a-model-annotate)时的系统消息。模板。 |
| **Annotate Prompt** | 批注 | 批注请求的最后一条消息。模板。 |
| **Annotate Instructions**（*What to look for*） | 批注 | 模型在批注时要查找的内容（例如 *flag continuity errors*、*weak dialogue*），即批注提示词中 `{%{{{annotateInstructions}}}%}` 的文本。纯文本。可在此处、文件夹中或 *Settings → Templates* 中设置，设置后 *Annotate with AI* 窗口会以它作为初始内容。 |
| **Impersonate Prompt** | 草拟指令 | 请求模型代替你[草拟](story-editor.md#drafting-the-instructions)下一回合指令的请求。模板。 |

*Point of View*、*Tense* 和 *Style* 会原样插入主模板中；宏在这些字段中不起作用。

带有语法错误的模板不会被保存，系统会显示一条警告，指出错误所在。

<a id="templates"></a>

## 模板

模板是带有占位符的文本，占位符写在双花括号中。每个模板都有各自的变量：

| 模板 | 变量 |
|---|---|
| 主模板 | `{%{{{backgroundLore}}}%}`（已激活的词条书条目）、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`、`{%{{{summaries}}}%}`、`{%{{{annotations}}}%}`（你给模型的备注） |
| 用户提示词 | `{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}`、`{%{{{instructions}}}%}` |
| 摘要提示词 | `{%{{{backgroundLore}}}%}`（词条书当前为该部分给出的设定）、`{%{{{text}}}%}`（待摘要的部分） |
| 元摘要提示词 | `{%{{{backgroundLore}}}%}`（同上）、`{%{{{summaryBlocks}}}%}`（待合并的摘要） |
| 重写系统提示词、重写提示词 | `{%{{{text}}}%}`（该部分）、`{%{{{redraftInstructions}}}%}`（你想修改的内容）、`{%{{{previousStory}}}%}`、`{%{{{summaries}}}%}`、`{%{{{followingStory}}}%}`、`{%{{{backgroundLore}}}%}`、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`，以及写作该部分时所用的回合：`{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}`、`{%{{{instructions}}}%}`，还有 `{%{{{annotations}}}%}`（你对该部分的未结备注，除非你在重写窗口中要求，否则为空）；对于[选段重写](story-editor.md#redrafting-only-a-passage)，还有 `{%{{{selection}}}%}`（所选文本；重写整个部分时为空）、`{%{{{selectionBefore}}}%}` 和 `{%{{{selectionAfter}}}%}`（该选段前后的部分文本） |
| 草拟指令提示词 | `{%{{{impersonateInstructions}}}%}`（你的指导）、`{%{{{outline}}}%}`（调用方提供的故事规划，例如 Arc Outline 中的当前章节）、`{%{{{previousStory}}}%}`、`{%{{{summaries}}}%}`、`{%{{{backgroundLore}}}%}`（上一部分的设定）、`{%{{{instructionExamples}}}%}`（最近三个部分的指令，最早的在前）、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`，以及这些指令所针对的回合：`{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}` |
| 批注系统提示词、批注提示词 | 重写提示词的变量，但以 `{%{{{annotateInstructions}}}%}`（你希望检查的内容）代替 `{%{{{redraftInstructions}}}%}`，另外还有 `{%{{{annotations}}}%}`（该部分已有的备注）、`{%{{{facts}}}%}`（扩展所了解的该部分故事情况，例如角色所知的信息；没有此类扩展时为空）以及 `{%{{{outputFormat}}}%}`（告诉模型应如何作答） |

只有当变量不为空时，某个段落才会输出其内容，因此提示词中不会出现空的标题：

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

段落内的 `{%{{{.}}}%}` 即该变量的值。若 *Scene Setting* 为空，则整行都会被省略。

所有模板还可以使用与 SillyTavern 兼容的**宏**，例如 `{%{{{user}}}%}`（视角角色）、`{%{{{description}}}%}`（书籍的简介）、`{%{{{random::a::b}}}%}`、`{%{{{getvar::name}}}%}` 和 `{%{{{setvar::name::value}}}%}`。完整语法和宏参考请见[模板与宏](../templates/index.md)。

<a id="master-template"></a>

## 主模板

主模板会成为**系统消息**，即提示词的第一条消息。内置默认值包含以下几个块：

1. **System role**：模型是一位续写故事的作者。
2. **World & lore context**：已激活的[词条书](../lorebooks/index.md)条目（`backgroundLore`）。
3. **Writing style & mechanics**：视角、时态、文风以及一般的行文规则。
4. **Summary of story so far**：[摘要](summaries.md)。
5. **Author's notes on the story**：仅当你向模型提供了[备注](annotations.md#giving-a-note-to-the-model)时才出现。

如果[推理服务提供方](../inference-providers.md#general-settings)启用了越狱提示词，它会被放在主模板之前。

<a id="user-prompt"></a>

## 用户提示词

用户提示词是提示词的**最后一条消息**，即要求模型撰写该部分的请求。它由 *New Turn Instructions* 填充（若是 *Regenerate* 和 *Swipe*，则使用 *Turn Details*）。内置默认值会请求下一节，包括视角角色、地点、在场角色以及情节指令，并附带以下规则：除非有要求，不得引入新角色；遵循指令；保持连贯性；只输出故事正文。

插入模式为 *Before User Prompt* 的词条书条目会被放在它之前；参见[条目与激活](../lorebooks/entries.md)。
<a id="redraft-prompt"></a>

## 重绘提示词

一个部分的[重绘](story-editor.md#redrafting-a-part)是一次由两个提示词组成的请求：**重绘系统提示词**是系统消息，该部分之前的故事作为对话中较早的消息（各个部分作为模型的回答，与生成时发送的方式相同），而**重绘提示词**是用户的最后一条消息。内置的默认提示词要求模型执行你的指令、保留你未要求修改的内容、与该部分前后的故事保持一致、保持文风，并且只回答重绘后的部分。两个提示词使用相同的变量；`{%{{{previousStory}}}%}` 只是一个说明，表示故事以消息的形式跟随在后。在系统提示词中保留每次重绘都相同的内容，在最后一条提示词中放入会变化的内容（设定集，它对每个部分都不同；该部分之后的故事；该部分本身；指令）：这样，如果服务器会记住它读过的提示词，就能复用其中的大部分内容。你自己编写的最后一条提示词应保留它所需的变量：没有 `{%{{{text}}}%}`，模型就看不到该部分。每个提示词都遵循所有提示词的规则：先取本书的字段，其次取*设置 → 模板*中的字段，最后取内置的提示词。

当你只重绘[选中的段落](story-editor.md#redrafting-only-a-passage)时，同样的提示词会在 `{%{{{selection}}}%}` 被填充的情况下使用：内置的默认提示词会显示该部分，段落位于 `[[PASSAGE TO REDRAFT]]` 与 `[[/PASSAGE TO REDRAFT]]` 之间，并要求模型只回答替换该段落的文字。如果你自己的提示词不认识 `{%{{{selection}}}%}`，模型就会回答整个部分，而整个回答将替换选中的内容；请添加一个 `{%{{#selection}}%}...{%{{{/selection}}}%}` 块（另一种情况则使用 `{%{{{^selection}}}%}`）来处理这种情况。

<a id="redraft-shorter-and-longer"></a>

## 重绘得更短或更长

[重绘](story-editor.md#redrafting-a-part)窗口中的**长度**选项（**保持长度**、**更短**、**更长**）不会改变重绘提示词。选择**更短**时，**更短重绘提示词**的文本（选择**更长**时则是**更长重绘提示词**的文本）会被添加到你在*重绘指令*中所写内容之后，位于 `{%{{{redraftInstructions}}}%}` 的位置，因此它也适用于你自己的重绘提示词。如果指令为空，它就是唯一的指令。这两段文字遵循所有提示词的规则：先取本书的字段，其次取其上方最近的、设置了该字段的文件夹，再取*设置 → 模板*中的文本，最后取内置的文本。内置文本要求大约缩短三分之一到一半（加长则是再增加一半到两倍），且不改变情节走向。

<a id="annotate-prompt"></a>

## 标注提示词

当模型[标注一个部分](annotations.md#letting-a-model-annotate)时，它收到的请求与重绘的类型相同：**标注系统提示词**作为系统消息，该部分之前的故事作为较早的消息，而**标注提示词**作为最后一条消息。内置的默认提示词让模型扮演编辑：它根据你的指令找出存在问题的段落，逐字引用每一处，并为你写一段简短的批注，不加赞美或废话，也不重复该部分已有的批注。当扩展提供[关于故事的事实](annotations.md#letting-a-model-annotate)时（知识追踪器：角色知道什么），默认提示词会将其显示为*已确立的事实*，编辑会标出与之相矛盾的内容。提示词中的变量 `{%{{{outputFormat}}}%}` 位于描述回答方式的位置（JSON、文本块或自由评论，取决于你选择的回答格式）；如果你要自己编写提示词，请保留它，否则模型不会知道如何作答。两个标注提示词都遵循所有提示词的规则：先取本书的字段，其次取*设置 → 模板*中的字段，最后取内置的提示词。

**关注什么**（*标注指令*）遵循同样的规则，因此你可以只设置一次：在*设置 → 模板*中设置则适用于你所有的书；在某个文件夹中设置则适用于其中的书；在某本书的*提示词*标签页中设置则只适用于这一本。*使用 AI 标注*窗口会在其字段中以灰色文字显示当前生效的内容，并说明其来源：留空即可使用它；输入内容则会在本次运行中替换它；或点击该字段的信息图标并选择**填充默认值**，将当前生效的内容复制到字段中，以便为本次运行进行编辑。若各处均未设置，则使用提示词的默认任务。自动标注和 `/generate-annotation` 命令同样如此。

<a id="impersonate-prompt"></a>

## 续写指令提示词

当模型[起草下一轮的指令](story-editor.md#drafting-the-instructions)时，它会收到一个请求，作为唯一消息发送，与重绘的方式相同。内置的默认提示词要求模型撰写你会输入到*指令*中的内容：即下一部分的情节说明（发生什么、谁做什么、该部分在哪里结束），它发展你的引导并遵循计划，从故事停下的地方继续，写法与你自己早先的指令相似，而不是正文本身。没有引导时，它会顺着故事的走向继续。你的引导是提示词放置 `{%{{{impersonateInstructions}}}%}` 的位置；如果你自己编写提示词，请保留它。续写指令提示词遵循所有提示词的规则：先取本书的字段，其次取*设置 → 模板*中的**续写指令提示词**，最后取内置的提示词。

<a id="how-the-prompt-is-put-together"></a>

## 提示词是如何组装的

```
system     jailbreak (if enabled) + master template
user       [ Generate story. ]
assistant  part #1
user       [ Generate more story. ]
assistant  part #2
...        (as many earlier parts as fit)
user       lore "Before User Prompt" + user prompt
```

较早的部分会作为模型自己的回答发送，因此它会以相同的文风继续写作。当故事长于[上下文限制](../protocols.md#limits)时，最古老的部分会首先被省略；[摘要](summaries.md)会以浓缩的形式将它们保留在故事中。

任何部分菜单中的**显示提示词**会显示该部分是依据哪一份确切的提示词写成的。**比较提示词**会将它与较早某个部分的提示词并排显示，并指出它们第一次出现差异的位置，而那里正是服务器的[提示词缓存](../prompt-cache.md#compare-prompt)结束的地方。

<a id="tips"></a>

## 小贴士

- 把适用于每个部分的规则（不引入新角色、长度、格式）放在用户提示词中：它是最后一条消息，影响力最强。
- 把关于世界与角色的长期事实放入[设定集](../lorebooks/index.md)，而不是*文风*中。
- 在指令中要求长度（“约 800 字”），而不要依赖回答的长度上限，因为它会把文本截断。
- 修改模板后，生成一个部分并用**显示提示词**检查它。
