---
label: 书籍提示词
order: 70
---

# 书籍提示词

书籍的 **Prompts** 标签页保存 Marginalia 用来构建提示词的文本：两个模板、叙述设置，以及摘要和重写提示词。它们对书籍的语气影响远大于模型设置。

![Prompts 标签页](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## 默认值与书籍值

每个字段都是可选的。空字段会继承其值：先看书籍上方最近的[文件夹](folders.md)是否设置了该字段，若没有，则使用 **Settings → Templates** 中的默认值（参见[账户与设置](../account.md)）；若那里也为空，则使用内置默认值，其语言与你设置中的[提示词语言](../account.md#prompt-language)一致。被继承的值会以灰色显示在空字段中。

因此，可以在 *Settings* 中设置所有书籍共享的内容，在系列所在的文件夹中设置该系列共享的内容，只在书籍的 *Prompts* 标签页中设置该书特有的内容。修改会在你输入时自动保存。

<a id="inherited-settings"></a>

### 继承的设置

空字段下方的一行文字会说明其值的来源，便于理解这本书为什么会这样写作：

- *Inherited from the folder "Sci-fi"*：由某个[文件夹](folders.md#where-the-settings-of-a-book-come-from)设置（会注明书籍上方最近的那个文件夹）；
- *Inherited from your settings*：来自 *Settings → Templates* 中的值；
- *Built-in default*：没有任何地方设置该值。

![位于文件夹中的书籍的 Prompts 标签页](../../images/book-prompts-inherited.png)

文件夹的设置中也有同样的字段，并带有同样的说明行。空白文本视为未设置。由于查找是在构建提示词时进行的，将书籍移动到另一个文件夹，或更改其所在文件夹，都会影响你接下来生成的片段。

<a id="settings-of-extensions"></a>

### 扩展的设置

[扩展](../extensions/index.md)可以添加设置，书籍和文件夹同样会继承这些设置（例如，关于本书的聊天将其视为何种媒介）。这些设置位于提示词下方的 **Extension Settings** 区域中，包括书籍的 *Prompts* 标签页和文件夹的设置，每个扩展对应一个 **可折叠面板**（默认全部收起，但第一个已设置了值的面板除外，以免你填写的值被隐藏）。它们的工作方式与上面的字段相同：空字段先从书籍上方的文件夹继承，然后从 *Settings → Extension Settings* 中该扩展自己的设置继承，最后才是其默认值，字段下方的说明行会注明具体来源。[导出格式](export.md#title-page-template)的模板（扉页、前置内容、著作权声明）也位于同一区域，每种格式各占一个面板。为某个扩展设置的值会保留在书籍或文件夹中，即使该扩展未被加载。

每个字段都有一个提示气泡，其中的 **Insert Default Template** 会把默认模板复制到字段中，作为你自定义版本的起点。模板字段还会列出 **Available Template Variables** 和 **Available Macros**。

![模板字段的提示气泡](../../images/template-hints.png)

<a id="the-fields"></a>

## 各个字段

| 字段 | 用于 | |
|---|---|---|
| **Master Template** | 系统消息 | 提示词的框架：模型的角色，以及设定集条目、叙述设置和摘要的放置位置。模板。 |
| **Point of View (POV)** | 主模板 | 叙述视角，例如 *First person*、*Third-Person Limited*。纯文本。 |
| **Tense** | 主模板 | 例如 *Past Tense*、*Present Tense*。纯文本。 |
| **Style** | 主模板 | 散文的风格与语气指引。纯文本。 |
| **Default User Prompt** | 最后一条消息 | 下一段的请求，由[回合指令](story-editor.md#writing-the-next-part)构建。模板。 |
| **Default Summary Prompt** | 摘要 | [摘要](summaries.md)的请求。模板。 |
| **Default Meta Summary Prompt** | 元摘要 | [元摘要](summaries.md#meta-summaries)的请求，用于合并多个摘要。模板。 |
| **Redraft System Prompt** | 重写 | [重写](story-editor.md#redrafting-a-part)的系统消息：指令、叙述和摘要。模板。 |
| **Redraft Prompt** | 重写 | 重写的最后一条消息，即请求本身：设定集现在为该片段提供的设定（因此修改条目会影响重写；只有当书籍没有设定集时，才会使用写作该片段时的设定）、该片段之后的故事、该片段以及指令。模板。 |
| **Redraft Shorter Prompt**、**Redraft Longer Prompt** | 重写 | 当[重写](story-editor.md#redrafting-a-part)窗口设为 **Shorter** 或 **Longer** 时，添加在你的指令之后的句子。纯文本，不是模板。留空则使用内置文本。 |
| **Annotate System Prompt** | 批注 | 请求模型[批注](annotations.md#letting-a-model-annotate)某个片段时的系统消息。模板。 |
| **Annotate Prompt** | 批注 | 批注请求的最后一条消息。模板。 |
| **Annotate Instructions**（*What to look for*） | 批注 | 模型批注时要查找的内容（例如 *flag continuity errors*、*weak dialogue*），即批注提示词中 `{%{{{annotateInstructions}}}%}` 的文本。纯文本。在这里、在文件夹中或在 *Settings → Templates* 中设置，*Annotate with AI* 窗口会以它作为起始内容。 |
| **Impersonate Prompt** | 指令草拟 | 请求模型代你[草拟](story-editor.md#drafting-the-instructions)下一回合指令的请求。模板。 |

*Point of View*、*Tense* 和 *Style* 会原样插入主模板；宏在其中不起作用。

包含语法错误的模板不会被保存，警告会显示错误信息。

<a id="templates"></a>

## 模板

模板是带有占位符的文本，占位符用双花括号括起。每个模板都有各自的变量：

| 模板 | 变量 |
|---|---|
| 主模板 | `{%{{{backgroundLore}}}%}`（已激活的设定集条目）、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`、`{%{{{summaries}}}%}`、`{%{{{annotations}}}%}`（你提供给模型的备注） |
| 用户提示词 | `{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}`、`{%{{{instructions}}}%}` |
| 摘要提示词 | `{%{{{backgroundLore}}}%}`（设定集现在为该片段提供的设定）、`{%{{{text}}}%}`（要摘要的片段） |
| 元摘要提示词 | `{%{{{backgroundLore}}}%}`（同上）、`{%{{{summaryBlocks}}}%}`（要合并的摘要） |
| 重写系统提示词、重写提示词 | `{%{{{text}}}%}`（片段）、`{%{{{redraftInstructions}}}%}`（你希望修改的内容）、`{%{{{previousStory}}}%}`、`{%{{{summaries}}}%}`、`{%{{{followingStory}}}%}`、`{%{{{backgroundLore}}}%}`、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`，以及写作该片段时所用的回合：`{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}`、`{%{{{instructions}}}%}`，还有 `{%{{{annotations}}}%}`（你对该片段未处理的批注，除非你在重写窗口中要求，否则为空）；对于[选段重写](story-editor.md#redrafting-only-a-passage)，还有 `{%{{{selection}}}%}`（所选文本；重写整个片段时为空）、`{%{{{selectionBefore}}}%}` 和 `{%{{{selectionAfter}}}%}`（所选部分前后的片段文本） |
| 代入提示词 | `{%{{{impersonateInstructions}}}%}`（你的指导）、`{%{{{outline}}}%}`（调用方提供的故事大纲，例如“篇章大纲”中的当前章节）、`{%{{{previousStory}}}%}`、`{%{{{summaries}}}%}`、`{%{{{backgroundLore}}}%}`（最后一段的设定）、`{%{{{instructionExamples}}}%}`（最近三段的指令，最早的在前）、`{%{{{narrativePov}}}%}`、`{%{{{narrativeTense}}}%}`、`{%{{{style}}}%}`，以及这些指令所针对的回合：`{%{{{sceneSetting}}}%}`、`{%{{{povCharacter}}}%}`、`{%{{{presentCharacters}}}%}` |
| 批注系统提示词、批注提示词 | 重写提示词的变量，但以 `{%{{{annotateInstructions}}}%}`（你希望审视的内容）代替 `{%{{{redraftInstructions}}}%}`，并增加 `{%{{{annotations}}}%}`（该片段已有的批注）、`{%{{{facts}}}%}`（扩展所知的、该片段所处时间点的故事情况，例如角色知道什么；没有此类扩展时为空）以及 `{%{{{outputFormat}}}%}`（告诉模型如何作答） |

只有当变量不为空时，区段才会输出其内容，因此提示词中不会出现空的标题：

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

区段内的 `{%{{{.}}}%}` 即该变量的值。当 *Scene Setting* 为空时，整行都会被省略。

所有模板还可以使用与 SillyTavern 兼容的**宏**，例如 `{%{{{user}}}%}`（视角角色）、`{%{{{description}}}%}`（书籍的描述）、`{%{{{random::a::b}}}%}`、`{%{{{getvar::name}}}%}` 和 `{%{{{setvar::name::value}}}%}`。完整语法和宏参考见[模板与宏](../templates/index.md)。

<a id="master-template"></a>

## 主模板

主模板会成为**系统消息**，即提示词的第一条消息。内置默认值包含以下几个部分：

1. **系统角色**：模型是一位续写故事的作者。
2. **世界与设定背景**：已激活的[设定集](../lorebooks/index.md)条目（`backgroundLore`）。
3. **写作风格与技法**：视角、时态、风格以及一般的散文规则。
4. **截至目前的故事摘要**：[摘要](summaries.md)。
5. **作者对故事的备注**：仅当你向[模型提供了备注](annotations.md#giving-a-note-to-the-model)时出现。

如果[推理提供商](../inference-providers.md#general-settings)启用了越狱提示词，它会被放在主模板之前。

<a id="user-prompt"></a>

## 用户提示词

用户提示词是提示词的**最后一条消息**，即请求编写下一段的内容。它由*新回合指令*（若是 *Regenerate* 和 *Swipe*，则由*回合详情*）填充。内置默认值会请求下一节的内容，并给出视角角色、地点、在场角色和情节指令，之后是若干规则：除非有要求，不引入新角色；遵循指令；保持连贯性；只输出故事正文。

设定集条目若设置了插入模式 *Before User Prompt*，就会被放在它之前；参见[条目与激活](../lorebooks/entries.md)。
<a id="redraft-prompt"></a>

## 重写提示词

[重写](story-editor.md#redrafting-a-part)一个部分是一次由两条提示词组成的请求：**重写系统提示词**是系统消息，该部分之前的故事作为对话中较早的消息跟随其后（各部分作为模型的回答，与生成时的发送方式相同），**重写提示词**是用户的最后一条消息。内置默认版本会让模型应用你的指示、保留你没有要求修改的内容、与该部分前后的故事保持一致、保持文风，并且只回复重写后的部分。两条提示词使用相同的变量；`{%{{{previousStory}}}%}` 只是一个说明，表示故事以消息的形式跟随。请在系统提示词中保留每次重写都相同的内容，在最后一条提示词中放置每次都会变化的内容（设定，因为它并非对每个部分都相同；该部分之后的故事；该部分本身；指示）。这样，一个会记住它读过的提示词的服务器便能复用其中的大部分内容。你自己编写的最后一条提示词应保留它所需的变量：没有 `{%{{{text}}}%}`，模型就看不到该部分。每条提示词都遵循所有提示词的规则：书籍的字段，否则是*设置 → 模板*中的那一条，否则是内置的那一条。

当你只重写[选中的段落](story-editor.md#redrafting-only-a-passage)时，使用的是同一条提示词，并且 `{%{{{selection}}}%}` 会被填充：此时内置默认版本会显示该部分，并在 `[[PASSAGE TO REDRAFT]]` 与 `[[/PASSAGE TO REDRAFT]]` 之间标出该段落，同时告诉模型只回复用来替换该段落的文本。如果你自己的提示词不认识 `{%{{{selection}}}%}`，模型就会回复整个部分，而整个回复会替换选区；此时请添加一个 `{%{{#selection}}%}...{%{{{/selection}}}%}` 块（以及用于另一种情况的 `{%{{{^selection}}}%}`）来处理。

<a id="redraft-shorter-and-longer"></a>

## 缩短与加长重写

[重写](story-editor.md#redrafting-a-part)窗口中的**长度**选项（**保持原长**、**缩短**、**加长**）不会改动重写提示词。选择**缩短**时，**缩短重写提示词**的文本（选择**加长**时则是**加长重写提示词**的文本）会被添加到你在*重写指示*中填写的内容之后，位于 `{%{{{redraftInstructions}}}%}` 所在的位置，因此即使使用你自己的重写提示词，它也同样有效。如果重写指示为空，它就是唯一的指示。这两段文本遵循所有提示词的规则：书籍的字段，否则是其上方最近的、设置了该字段的文件夹，否则是*设置 → 模板*中的那一条，否则是内置文本。内置文本要求大约缩短三分之一到一半（加长则是多出一半到两倍的篇幅），且不改变情节发展。

<a id="annotate-prompt"></a>

## 批注提示词

当模型[为某个部分添加批注](annotations.md#letting-a-model-annotate)时，它收到的请求与重写类似：**批注系统提示词**作为系统消息，该部分之前的故事作为较早的消息，**批注提示词**作为最后一条消息。内置默认版本让模型扮演一名编辑：它依据你的指示找出存在问题的段落，逐字引用每一处，并为你写一条简短的批注，不写赞美或套话，也不重复该部分已有的批注。当某个扩展提供[关于故事的事实](annotations.md#letting-a-model-annotate)（例如知识追踪器：角色知道什么）时，默认提示词会以*已确立的事实*的形式显示它们，编辑会标出与之相矛盾的内容。提示词中有变量 `{%{{{outputFormat}}}%}`，它描述了回答的方式（JSON、文本块或自由评论，取决于你选择的回答格式）；如果你自己编写提示词，请保留它，否则模型不会知道如何回答。两条批注提示词都遵循所有提示词的规则：书籍的字段，否则是*设置 → 模板*中的那一条，否则是内置的那一条。

**查找什么**（*批注指示*）遵循同样的规则，因此你只需设置一次：对于你的所有书籍，在*设置 → 模板*中设置；对于其中的书籍，在文件夹中设置；对于某一本书，在该书的*提示词*标签页中设置。*用 AI 添加批注*窗口会在其字段的灰色文字中显示当前生效的内容，并说明它来自何处：将字段留空即可使用它，输入内容则会在本次运行中替换它，或者点击该字段的信息图标并选择**填入默认值**，把当前生效的内容复制到字段中，以便为本次运行进行编辑。若在任何地方都未设置，则使用该提示词的默认任务。自动批注和 `/generate-annotation` 命令也是如此。

<a id="impersonate-prompt"></a>

## 续写指示提示词

当模型[起草下一轮的指示](story-editor.md#drafting-the-instructions)时，它会收到一次请求，并作为唯一的消息发送，与重写类似。内置默认版本会要求模型撰写你本会输入到*指示*中的内容：一份关于下一部分的情节说明（发生什么、谁做什么、该部分在哪里结束），它要发展你的引导并遵循既定计划，从故事停止的地方继续，写法与你自己先前的指示相似，而不是正文本身。若没有引导，它就顺着故事的走向继续。你的引导位于提示词中 `{%{{{impersonateInstructions}}}%}` 所在的位置；如果你自己编写提示词，请保留它。续写指示提示词遵循所有提示词的规则：书籍的字段，否则是*设置 → 模板*中的**续写指示提示词**，否则是内置的那一条。

<a id="how-the-prompt-is-put-together"></a>

## 提示词的组成方式

```
system     越狱提示（若已启用）+ 主模板
user       [ 生成故事。 ]
assistant  第 1 部分
user       [ 继续生成故事。 ]
assistant  第 2 部分
...        （尽可能多地放入较早的部分）
user       设定的“用户提示之前”+ 用户提示
```

较早的部分会作为模型自己的回答发送，因此它会以相同的文风继续写下去。当故事长于[上下文限制](../protocols.md#limits)时，最早的部分会优先被省略；[摘要](summaries.md)会以压缩的形式将它们保留在故事中。

任何部分菜单中的**显示提示词**会显示写出该部分时所用的确切提示词。**比较提示词**会将它与较早某个部分的提示词并列放置，并显示它们首次不同之处，即服务器的[提示词缓存](../prompt-cache.md#compare-prompt)在何处结束。

<a id="tips"></a>

## 提示

- 把适用于每个部分的规则（不得引入新角色、长度、格式）放入用户提示词：它是最后一条消息，因此效果最强。
- 把关于世界和角色的长期事实放入[设定集](../lorebooks/index.md)，而不要放入*风格*。
- 在指示中要求篇幅（例如“约 800 字”），而不要依赖回复长度上限，因为上限会把文本截断。
- 更改模板后，生成一个部分并用**显示提示词**检查它。
