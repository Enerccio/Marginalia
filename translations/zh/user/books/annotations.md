---
label: 注释
order: 95
---

# 注释

**注释**是你为某段章节文字留下的备忘，例如*场景较弱*、*时间线有误*、*需要重写*。它的作用类似文字处理软件中的批注：故事正文保持干净，段落仅被标记，备注则附在旁边。注释也可以由模型或检查故事的[扩展](../extensions/index.md)生成。

注释**仅供作者查看**。除非你为某条注释作出设定，否则它不会发送给模型，参见[把注释交给模型](#giving-a-note-to-the-model)。

<a id="what-an-annotation-is"></a>

## 注释包含什么

| | |
|---|---|
| **段落** | 注释所针对的文字，或**整个章节**。 |
| **备注** | 你想记住的内容。 |
| **类型** | 可选，由你自定的词语（*连贯性*、*文风*、*节奏*……）。 |
| **来源** | 注释的作者：你、模型（*AI*）、*代理*或*扩展*。正文中的标记以不同颜色区分。 |
| **状态** | *待处理*、*已解决*、*已忽略*或*已失去段落*。只有待处理的注释会在正文中标记。 |

一个章节可以有任意数量的注释，段落也可以重叠。

<a id="making-an-annotation"></a>

## 添加注释

**针对某段文字。** 在章节正文中选中文字（可以带有格式，选区不必精确），然后**右键单击**（手机上为长按），选择**Annotate selection**，或按下其[快捷键](story-editor.md#keyboard-shortcuts)（`a`）。窗口会显示该段落；填写备注、可选的类型，以及模型是否接收该备注，然后点击**Save**。章节的菜单（☰）中没有此项：打开菜单会取消选区。右键菜单包含**Annotate selection**、[**Redraft selection**](story-editor.md#redrafting-only-a-passage)和**Copy**，它在章节正文上取代了浏览器自带的菜单，因此也提供了**Copy**。章节正在编辑时，使用编辑器中的选区，编辑内容会先被保存。

**针对整个章节。** 在章节的菜单中选择**Annotate part**。

**借助模型。** 选择**Annotate with AI**，参见[下文](#letting-a-model-annotate)。

**通过命令行**（以及供扩展使用）：[`/create-annotation`](../commands/index.md#annotations)。

![章节文字的右键菜单](../../images/story-text-menu.png)

![新建注释的窗口](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## 阅读注释

带有待处理备注的段落会以**波浪线**标出，颜色表示备注的作者：蓝色为你，橙色为模型，绿色为代理，红色为扩展。一条注释位于另一条之内时，下划线位置会更低，两者都能看到。

![带下划线段落的备注，在标记处打开](../../images/annotation-popover.png)

- **点击带下划线的文字**，查看覆盖该位置的备注（段落重叠时显示全部）。它们会在文字旁边打开；在手机上请点按该段落。
- 章节标题中带数字的**气泡**，表示该章节中需要查看的备注数量。点击即可查看全部，包括针对整个章节的备注，以及失去段落的备注。
- 在**页边**（故事的齿轮菜单中的**Annotations in the margin**；仅限桌面端，并会记住你的设置）中，每个章节的备注会按其段落在文中的顺序排成一列，位于正文左侧，而不再显示在弹出框中。鼠标指向的备注与其下划线段落会同时高亮，点击段落即可将对应备注滚动到视野中。这些仍是同一批备注（可解决、编辑等）；已解决和已忽略的备注不会在此显示，因为它们也不再带下划线。
- **Annotations**标签页按故事顺序，依章节分组列出整本书的备注，并提供筛选（*Need a look*、*Closed*、*All*）。点击**Show in story**（或章节编号），即可跳转到该章节。

![章节页边的备注](../../images/annotations-margin.png)

![Annotations标签页](../../images/annotations-tab.png)

每条备注都会显示其作者、类型、状态、段落、备注内容，以及以下操作：

| | |
|---|---|
| ✓ **Resolve** | 问题已处理。备注离开正文，但仍保留在列表中。 |
| ✗ **Dismiss** | 备注不值得处理。效果同上。 |
| ↶ **Reopen** | 将已关闭的备注重新打开。 |
| ✎ **Edit** | 修改备注内容和类型。 |
| 🗑 **Delete** | 删除备注；可以从[回收站](../trash.md)中恢复。 |
| **Give to the model with the part** | 见下文。 |
| **Attach to selection** | 用于没有段落的备注：先在章节中选中文字，再点击它。 |

<a id="annotations-follow-the-text"></a>

## 注释跟随文字

编辑时无需在意备注的位置。Marginalia 会让每条备注的段落跟随它所指向的文字：

- 在段落**之前**添加或删除文字，会使段落随之移动；
- 在段落**内部**的编辑会使段落变长或变短；
- 你在段落**最开头或最末尾**输入的文字，不属于该段落；
- 修正只有一个词的段落中的错别字，备注仍会保留在该词上。

当段落被**删除**，或被改写到无法再找到时，备注会变为**已失去段落**。它会被保留：你可以将其附加到另一段文字（**Attach to selection**）、忽略它，或将其删除。如果你恢复了这些文字（撤销），备注会重新找到它的段落。

当某个章节**被重新生成**时，文字已是新的，因此备注会依据它所引用的文字重新寻找段落；引用的文字已不存在的备注会失去段落。合并两个章节（[合并](story-editor.md#merging-a-part-into-the-previous-one)）时，较后那个章节的备注会移到合并后的章节中。章节的**分支**会复制其备注。备注属于[备份](backups.md)的一部分，也包含在书籍的副本以及备份的导出文件中。
<a id="letting-a-model-annotate"></a>

## 让模型做批注

част 菜单中的 **Annotate with AI** 会请模型审阅该部分。你说明要查找什么（*flag continuity errors*、*weak dialogue*、*contradicts the lore*；留空则使用书籍、其文件夹或你的设置中[设定一次](prompts.md#annotate-prompt)的内容，否则使用提示词的任务），选择模型，然后点击 **Annotate**。

模型读取该部分的方式与[重写](story-editor.md#redrafting-a-part)相同：读取该部分、它之前的故事（较早的故事以[摘要](summaries.md)形式提供）、它之后尽可能多的故事（以能放入为限）、书籍的设定和风格，以及该部分已有的批注，以免重复。模型从不给出位置，而是**引用**段落原文，由 Marginalia 在正文中找到该引文。找不到的引文并不会丢失：该批注会作为针对整个部分的批注提出，并标记为 *not found in the text*。

**在你接受之前，不会向书籍添加任何内容。** 模型提出的批注会逐条列出，每条前面都有一个复选框（默认全部勾选）。取消勾选你不需要的批注，然后选择 **Add the selected annotations**。

在电脑上，该窗口不会阻止你阅读故事；在模型工作时，窗口还可以折叠到页面角落，参见[在窗口旁边工作](story-editor.md#working-beside-the-redraft-and-annotation-windows)。

| 选项 | |
|---|---|
| **Model** | 进行审阅的模型。留空时使用你[设置](../account.md#general-settings)中的 **Default model for annotating**，若未设置则使用书籍的模型。审阅属于分析工作，因此通常不必与撰写正文的模型相同。 |
| **Answer format** | *Automatic*（当模型能以 JSON 回答时使用结构化格式）、*Structured (JSON)*、*Text blocks*（适用于不严格遵守格式的模型），或 *Free critique, then extraction*（模型先写出自由评论，再由第二个模型（通常是更便宜的模型）将其整理为批注；请在 **Model that extracts the notes** 中选择该模型）。 |

![模型为某一部分提出的批注](../../images/annotate-ai-proposals.png)

**一段范围内的部分（批量）。** 某部分菜单中的 **Annotate with AI** 只审阅该部分。若要审阅多个部分，请打开故事的设置菜单（⚙），选择 **Annotate a range of parts with AI**（手机上也可用）。这是同一个窗口，可选择 **From part** 和 **To part**（两者都必填，否则 **Annotate** 会等待；哪个在前无关紧要；只列出有正文的部分）。随后 **Annotate** 会依次审阅范围内的各个部分，使用相同的指令、回答格式和模型，每个部分都连同其周围的故事一起读取，如同单独审阅一样。提出的批注会列在其所属部分的编号之下，全部默认勾选；**Add the selected annotations** 会把每条批注添加到各自的部分中。**Stop** 会停止当前部分，并不再开始新的部分；此前找到的内容仍保留在列表中。若某部分的回答无法读取，或内容超出模型的上下文，该部分会被跳过（窗口会说明原因），运行继续；若模型本身出错，则运行结束。每个部分都是一次对模型的请求，因此范围越长，请求（以及令牌）消耗越多。

![批量审阅一段范围内的部分：提出的批注列在其所属部分的编号之下](../../images/annotate-ai-batch.png)

**来自扩展的事实。** 某个[扩展](../extensions/index.md)若掌握的故事信息多于其正文所写，可以将这些信息提供给审阅者；审阅者随后会把与之矛盾的段落标为连贯性错误。[Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know) 就是这样做的：它告诉模型，该部分中每个被追踪的角色在该部分开始时知道什么，因此一个从未听说过某个名字却使用了该名字的角色会被批注出来。这些事实写在内置的[批注提示词](prompts.md#annotate-prompt)中（你自己编写的提示词必须使用 `{%{{{facts}}}%}`）；若没有此类扩展，提示词保持原样。

如果模型的回答无法读取，Marginalia 会再请求一次；若仍无法读取，则会显示模型的原始回答，以免工作丢失。提示词即[批注提示词](prompts.md#annotate-prompt)。

<a id="annotating-every-new-part-automatically"></a>

### 自动为每个新部分做批注

故事的设置菜单（⚙）中有 **Automatic annotation**（手机上也可用；打勾表示已开启）。开启后，书籍会在每个部分生成后立即审阅该部分，并可选择是否进行重写。这些设置随书籍一起保存：

| 选项 | |
|---|---|
| **Model**、**Protocol** | 进行审阅的模型。留空时使用书籍的模型（而不是你设置中的 **Default model for annotating**，后者是你自己打开的窗口默认使用的模型）。 |
| **What to look for** | 指令，与 **Annotate with AI** 中的相同。留空时使用书籍所继承的内容（参见 [Annotate prompt](prompts.md#annotate-prompt)），若无则使用提示词的任务；当你在此字段留空开始时，该字段会显示所使用的文本。 |
| **Annotate every generated part automatically** | 开启此项。某部分生成后（新部分、换一版或重新生成；被停止或失败的不计在内），模型会**立即**审阅它，无需窗口；[运行面板](story-editor.md#the-panel-of-the-runs)会将其显示为[该部分之后步骤](story-editor.md#what-follows-a-generated-part)中的一步。 |
| **Add the annotations without asking** | 模型完成后，自动添加它找到的批注。未开启时，批注窗口会打开并显示提议的批注（在你生成时所在的页面中；在任何页面中可通过面板中的 **Review** 打开），并等待你选择，与你自己启动时一样。 |
| **Then redraft the part to address the annotations** | 仅在上一项开启时可用。添加批注后，模型会[重写](story-editor.md#redrafting-a-part)该部分，使用该部分仍未解决的批注，但不附加自己的指令。它使用书籍的模型撰写，且不改动摘要：该部分是新的，没有摘要描述它。若模型未发现任何问题，则无需重写。 |
| **Apply the redraft without asking** | 仅在重写开启时可用。模型完成后，重写稿会替换该部分的正文，所处理的批注会被标记为已解决。未开启此项时，或当模型撰写期间你改动了该部分，重写窗口会打开并显示结果，等待你处理。 |

等待你处理的重写窗口有两个标签页：**Redrafted text**（重写后的正文、其推理过程，以及与原部分的差异）和 **Original text with annotations**（原始部分，其中仍未解决的批注所对应的段落以故事中的方式标出，批注列在其下方；点击标记可跳到对应批注，或将鼠标指向任一处，使两者同时高亮）。查看原文可以了解模型被要求处理什么，然后你可以修改正文或应用它。

![自动批注的设置](../../images/annotate-auto.png)

**关闭等待你处理的窗口，或在面板中点击 Stop，会结束该部分之后的步骤。** 不会再添加或应用任何内容，后续步骤（翻译、音频）也不会运行；面板的消息会说明这些步骤的名称，以便你手动完成。已经添加或应用的内容保持不变。重写进行期间，无法生成新的部分（它会基于旧正文继续写）；批注制作期间则可以生成。

每个生成的部分至少会额外产生一次对模型的请求（若开启重写则为两次）。如果你使用的本地服务器只在缓存中保留一个提示词，那么审阅会取代缓存中的故事，下一次生成就必须重新读取故事。请为审阅选择另一个 **Model**，或使用第二台服务器。
<a id="watching-for-phrases"></a>

## 监视短语

**短语监视列表**记录你不希望出现在正文中的词语：陈词滥调（*a shiver ran down her spine*、*tapestry*）、模型的口癖，或是你漏掉的某个名字。章节在检查时**不使用模型**，每一处命中的短语都会在该段落上生成一条批注（来源为 *Phrase watch list*，并注明命中的条目）。此后它和其他批注没有区别：可以交给[重写](#redrafting-to-address-the-notes)处理，或者对没问题的命中点击 **Dismiss**（例如在对话中）。

![一本书的短语监视列表](../../images/phrase-watch-dialog.png)

这些短语并不会被禁止出现在模型的输出中。禁止只会在短语已经开始输出之后才起作用，并把模型推向最接近的同义词；而对成稿添加批注，可以让整段文字被整体重写。

**列表位于何处。** 你的设置中有一个标签页 **Phrase watch list**，其中是你自己的列表，每本书都会使用它；[文件夹](folders.md#folder-settings)可以为其中的书籍以及其下的子文件夹添加列表，书籍则可以在故事的设置菜单（⚙）中的 **Phrase watch list** 里添加自己的列表。各个列表会叠加。如果某条目与继承来的条目短语相同，则替换它（例如为其指定另一个模板）；标记为 **Off here** 的条目则会在该文件夹或该书中将其去除。被继承的短语会列在你自己的列表下方，每条都带有 **Switch off here**。

**短语如何被找到。** 不区分大小写，且只匹配完整的单词（*tapestry* 不会匹配 *tapestries*）；任何空格或换行都能匹配其他任何空白，直引号与弯引号、直撇号与弯撇号也互相匹配。若要改为编写[正则表达式](../regular-expressions.md)，请勾选 **Regular expression**，同样不区分大小写。无法使用的表达式在保存时会被拒绝。单个章节中最多生成 100 条批注。

**批注模板。** 批注的内容由你设置中（同一标签页）的**批注模板**决定：包括名称、**Note**（其中 `{%{{{match}}}%}` 为找到的词语，`{%{{{phrase}}}%}` 为列表中的条目）、**Type**（标记的颜色）以及 **Give to the model with the part**（参见[把批注交给模型](#giving-a-note-to-the-model)）。列表中的每个条目都选择一个模板；未选择模板的条目会使用内置批注（*Watched phrase: "…"*），没有类型，也不会交给模型。

**何时检查章节。** 在书籍的 **Phrase watch list** 中：

| 选项 | |
|---|---|
| **Annotate the watched phrases in every generated part** | 生成章节后，会在[自动批注](#annotating-every-new-part-automatically)之后立即检查该章节（这是[章节之后的操作](story-editor.md#what-follows-a-generated-part)中的一个步骤，无论自动批注是否开启）。若书籍同时启用了自动重写（自动批注中的 **Add the annotations without asking** 和 **Then redraft the part…**），重写也会处理这些批注；未经询问即应用的重写会再次检查，若其中又写回了某个短语，则会再次重写，最多三次（最后一次的命中项会作为批注留给你）。[智能体](../agents.md)写入的章节同样会被检查。 |
| **Save and scan the story** | 保存列表，并立即检查当前分支上的章节（适用于已经写好的书）：可以检查全部章节，也可以只检查从 **From part** 到 **To part** 之间的章节（即分支上章节的编号）。命令 `/scan-phrases [from to]` 的作用相同。 |

若某处已经有针对同一短语的该列表批注，则不会再添加第二条，即使你已对其执行 **Dismiss** 也是如此，这样故事在改动之后仍可再次扫描。已解决的批注不计算在内：若该短语仍然存在（例如重写又将其写回），则会生成新的批注。批注模板属于你的设置：导出时若包含所继承的内容，书籍会连同这些短语一并导出；而在其他安装环境中，这些批注将使用内置的批注内容。

<a id="giving-a-note-to-the-model"></a>

## 把批注交给模型

在批注上勾选 **Give to the model with the part**，它也会成为给模型的批注：提示词中所包含章节的批注会被放入主模板的 `{%{{{annotations}}}%}` 中（内置模板有一个名为 *Author's notes on the story* 的区块；你自己的模板则必须使用该变量）。只有勾选了此项的**未解决**批注才会被交给模型，并且仅限于能放入提示词的章节。请将其用于模型在续写时应当牢记的内容（*this character is hiding the letter*），而不要用于编辑性的备注。

<a id="redrafting-to-address-the-notes"></a>

## 通过重写处理批注

当某个章节有未解决的批注时，[重写](story-editor.md#redrafting-a-part)窗口中会有一个复选框 **Deal with the open annotations of the part**。勾选后，模型会连同你的指示一起获得这些批注（包括段落与批注内容），并据此重写章节以处理它们；当你点击 **Apply** 应用重写后，这些批注会被标记为已解决。在[对选区的重写](story-editor.md#redrafting-only-a-passage)中，只会提供其段落与选区有重叠的批注。

<a id="commands"></a>

## 命令

[`/list-annotations`](../commands/index.md#annotations)、`/get-annotation`、`/create-annotation`、`/update-annotation`、`/resolve-annotation`、`/reattach-annotation` 和 `/delete-annotation` 可以通过命令行操作批注。扩展也可以使用相同的命令。[`/generate-annotation <part>`](../commands/index.md#annotations) 是作为命令使用的 **Annotate with AI**：模型使用书籍的模型审阅章节（当 *Write the book with the model of the agent* 开启时，智能体使用其自身的模型；`profile` 用于选择智能体的另一个模型配置），并依据书籍的指示、回答格式和提示词进行审阅；它找到的每条批注都会立即添加，因为没有人来逐一挑选。这是一次对模型的请求，因此它单独运行，并会消耗 token。

<a id="annotations-and-agents"></a>

## 批注与智能体

[智能体](../agents.md)使用相同的命令读写批注：它可以为整本书添加批注（*continuity*、*style*……），将其处理过的批注标记为已解决，也可以编辑或删除批注。它在书籍的副本上工作，因此在你[审阅结果](../agents.md#reviewing-the-result)之前，任何内容都不会进入你的批注：

- 智能体添加的批注显示为一行 **Annotation - Added**；它已解决、被 Dismiss 或改写的批注显示为 **Changed**（其字段包括批注内容、类型、状态以及 *give to the model*，每个字段都有各自的 **Take this change** 复选框）；它删除的批注显示为 **Deleted**。若智能体删除了某个章节，该章节的批注会随之一并删除，不单独列出。
- 该行会注明批注所在的章节（显示 `3: ` 以及批注的开头部分）；**Show in the copy** 会在智能体的副本中打开该章节。对于智能体所写章节上的批注，需要该章节本身：勾选批注时，章节也会被一并勾选。
- 列表上方的 **Only annotations** 只勾选批注本身。当章节文本相同时，被采纳批注的段落也相同；若你采纳了智能体对章节的新文本，或你自己的文本有所不同，批注会在你书籍的文本中重新查找对应的词语；如果找不到，则会变成针对整个章节的批注。
- 你在智能体暂停期间写下的批注，会在其恢复运行时进入它的副本（你已解决的批注也会在那里被标记为已解决），因此它能够知晓；若你与智能体都修改了同一批注，则与其他情况一样视为冲突。
