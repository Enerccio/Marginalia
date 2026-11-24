---
label: 创建 lorebook
order: 100
---

# 创建 lorebook

<a id="the-lorebook-editor"></a>

## lorebook 编辑器

打开 **Lorebooks** 标签页。编辑器一次只显示一个 lorebook；在顶部的 **Lorebook** 字段中选择要编辑的那个。
lorebook 属于你的账户：其他用户看不到它们，除非你[共享](#sharing-a-lorebook)了某个 lorebook。

工具栏分为两行，在较窄的窗口中会换到更多行：第一行用于 lorebook（从 **Lorebook** 到 **Tag lorebooks**），第二行用于所选 lorebook 的条目（从 **Add Entry** 到 **Refresh**）。

| 按钮 | |
|---|---|
| **Lorebook** | 选择要编辑的 lorebook。 |
| **Only root lorebooks** | 只列出不是其他 lorebook 子 lorebook 的 lorebook（所选的 lorebook 仍保留在列表中）。 |
| **Add Lorebook** | 创建一个名为 *New Lorebook* 的新 lorebook 并将其选中。 |
| Clone（两张纸图标） | 复制所选的 lorebook：询问名称（原名称加上 *(Copy)*），然后选中副本。副本包含相同的条目（及其设置、标签和负面标签）、标签、描述和扩展数据。其子 lorebook 不会被复制，副本使用相同的子 lorebook。原 lorebook 不会被更改。 |
| **Delete Lorebook** | 删除所选的 lorebook（确认后）。 |
| Import（文件图标） | **Import Marginalia Lorebook**、**Import from SillyTavern**、**Import from archive**（`.md` 文件的 zip 压缩包）或 **Import from other user**（其他用户[共享](#sharing-a-lorebook)的 lorebook），参见[导入与导出](import-export.md)。 |
| Export（文件图标） | 下载该 lorebook 及其子 lorebook，参见[导入与导出](import-export.md#exporting)。 |
| Tags（标签图标） | **Tag lorebooks**：一次为多个 lorebook 添加标签，参见[下文](#tagging-many-at-once)。在书中打开的 lorebook 不显示此按钮。 |
| Description（横线图标，第一行最后一个） | **Edit description**：打开一个窗口，用于填写该 lorebook 的内容说明，参见[下文](#description)。 |
| **Add Entry** | 向 lorebook 添加条目，参见[条目与激活](entries.md)。 |
| **Full-screen editor** | 在全屏窗口中以标签页的形式打开该 lorebook 的条目，参见[全屏编辑](#editing-in-full-screen)。仅限桌面端。 |
| **Search and replace** | 替换该 lorebook 各条目的名称、正文和注释中的文本，参见[查找和替换](entries.md#search-and-replace)。 |
| **Mass edit** | 在一张表格中打开该 lorebook 及其所有子 lorebook 的全部条目，参见[批量编辑](#mass-edit)。 |
| Tags（标签图标，位于 **Search and replace** 右侧，第二行） | **Tag entries**：一次为所选 lorebook 的多个条目添加标签，参见[下文](#tagging-many-at-once)。 |
| **Preview activation** | 显示使用你输入的文本进行一轮生成时会用到哪些条目以及原因，参见[预览激活](entries.md#preview-activation)。 |
| **Refresh** | 重新加载条目。 |
| 搜索框 | 只显示包含所输入文本的条目，参见[搜索并批量删除条目](entries.md#searching-and-deleting-many-entries)。 |
| **Delete selected** | 在列表中选中条目时出现；确认后删除这些条目。 |

工具栏下方是所选 lorebook 的设置：

| 字段 | |
|---|---|
| **Name** | lorebook 的名称，在为书选择它或将其作为子 lorebook 选择时显示。 |
| **Sub Lorebooks** | 其条目会与本 lorebook 一起被包含的其他 lorebook，参见[下文](#sub-lorebooks)。点击已选子 lorebook 的名称可打开它。 |
| **Tags** | 整个 lorebook 的标签；它们被视为其每个条目的标签，参见[标签](entries.md#tags)。 |
| **Scan depth** | `0` 至 `5`：已被激活的条目的内容被用于搜索其他条目过滤器的次数，参见[被其他条目引入的条目](entries.md#entries-brought-in-by-other-entries)。它对书所使用的 lorebook 的条目及其子 lorebook 的条目均有效，除非某个子 lorebook 有自己的扫描深度（下一行）。 |
| **Scan depth as a sub lorebook** | 留空，或 `0` 至 `5`：当本 lorebook 作为书所使用的 lorebook 的子 lorebook 时，其条目的扫描深度。留空表示使用该 lorebook 的扫描深度，`0` 表示本 lorebook 的条目永远不会被其他内容触发。 |
| **Enabled** | 取消勾选时，该 lorebook 不提供任何内容——既不提供其条目，也不提供其子 lorebook 的条目。 |
| **Share with other users** | 勾选后，其他用户可以查看该 lorebook 并导入一份副本，参见[下文](#sharing-a-lorebook)。新建的 lorebook 默认关闭。 |

设置下方是该 lorebook 的[描述](#description)（如果有的话）。

编辑器的其余部分是[条目](entries.md)列表。所有更改在你进行时即自动保存。

<a id="description"></a>

### 描述

描述是关于该 lorebook 的几个词（或几行）说明：它描述的是哪个世界、用途是什么、哪本书在使用它。点击工具栏第一行的横线图标，在窗口中填写后确认；文本为空则删除描述。有描述的 lorebook 会在其设置下方以一行显示，放不下时以省略号截断；将鼠标指针停留在该行上即可阅读全部内容。描述会随 lorebook 一起复制（克隆、导出、导入、备份），并且是你共享该 lorebook 时其他用户在其名称旁看到的文字。

![lorebook 描述窗口](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### 共享 lorebook

勾选 **Share with other users**后，本安装实例的其他每位用户都能在 **Import from other user**（工具栏第一行的导入按钮）中看到该 lorebook，并显示其名称、作者（你的全名，若没有则为你的登录名）和描述。他们可以展开一行阅读条目（以及子 lorebook 的条目），并导入一份副本。取消勾选后，该 lorebook 会从他们的列表中消失；他们已经制作的副本仍归他们所有。

* 共享仅供阅读。其他人不能更改、删除或在书中使用你的 lorebook；它仍归你所有，你看到的情况与之前相同。
* 导入会为**其他用户**创建**新的 lorebook**：包括该 lorebook 及其**已共享**的子 lorebook（无论嵌套多深），连同其条目、标签和描述，并以与你的 lorebook 相同的方式相互链接。这些副本是私有的（未共享），你之后对自己的 lorebook 做的任何操作都不会影响它们，它们的更改也不会影响你的 lorebook。
* **只有本身已共享的子 lorebook 才会被显示和复制。** 未共享的子 lorebook 连同其下的所有内容都会被排除在外，即使其下方有已共享的 lorebook 也是如此。当你在一个含有未共享子 lorebook 的 lorebook 上勾选 **Share with other users** 时，会弹出一个询问是否也共享它们的提示：**Yes** 表示共享该 lorebook 及其所有子 lorebook（无论嵌套多深），**No** 表示只共享该 lorebook，**Cancel** 表示什么都不共享并保持复选框未勾选。之后添加的子 lorebook 在你共享之前不会被共享。
* 取消勾选复选框只会停止共享该 lorebook；其子 lorebook 仍保持共享（并各自独立出现在其他人的列表中），直到你也取消它们的勾选。
* 已删除的 lorebook、智能体的工作副本以及你自己的 lorebook 不会被列出；已被删除的用户也不会有任何内容被列出。

设置下方会显示已启用条目的 token 估算值。其旁边的 **Count with subbooks** 链接会汇总该 lorebook 及其所有子 lorebook（无论嵌套多深）中已启用条目的 token，并在旁边显示总数。与故事写作时一样，被禁用的子 lorebook 连同其下的 lorebook 都会被排除在外；若某 lorebook 在多个位置是子 lorebook，则只计算一次。总数是你点击那一刻的计数：更改某个条目后该数字会消失，再次点击即可重新计数。

智能体正在处理的书所使用的 lorebook 以只读方式显示（参见[智能体](../agents.md)），不显示 **Add Entry**、**Search and replace**、**Mass edit**、**Delete Lorebook** 以及设置项；**Add Lorebook**、导入、导出和 **Refresh** 仍然可用，因此你随时可以开始另一个 lorebook。
<a id="mass-edit"></a>

## 批量编辑

**批量编辑**会打开一个窗口，将所选世界书**及其子世界书**（无论嵌套多深）中的每个条目放在同一张表格中，按条目的**顺序**排列（顺序相同的条目先按世界书排列，再按名称排列）。它用于整理大型世界书：

- 可以直接修改条目的*启用*复选框、**条目名称**、**顺序**、**标签**、**过滤模式**、**过滤**、**插入模式**、**负面标签**、**激活上下文**及其**部分**、消息范围（**起始消息**和**结束消息**，留空表示不限制）以及条目的**备注**，修改方式与编辑器中相同。每个条目占一行，表格可向下滚动（窗口较窄时也可横向滚动）。修改**顺序**后，该行会移动到新的位置。表格中的条目文本会被缩短显示；如需阅读或编辑完整内容，请在世界书中打开该条目。
- 每行的**世界书**下拉框显示该条目所属的世界书。选择另一个世界书（该世界书本身或其任一子世界书），即可将条目移动到那里。
- 若要移动多个条目，请勾选它们（表头的复选框可全选），然后在底部选择**移动到世界书**，并点击**移动所选**。
- 若要删除多个条目，请勾选它们并点击**删除所选**；系统会询问一次，然后将它们移入[回收站](../trash.md)。
- 智能体正在使用的世界书会被锁定：其中的条目显示为灰色，无法修改或移动，也无法向其中移入任何条目。系统会逐个检查每个世界书，因此某个世界书可能处于可用状态，而其子世界书却被锁定（因为它正被另一本书使用）。点击**刷新**可重新读取窗口内容，以确认锁定是否已解除。

关闭窗口时若有过修改，编辑器中的世界书会随之刷新。

![Mass edit of a lorebook and its sub lorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## 全屏编辑

**全屏编辑器**（工具栏的第二行）会在一个铺满屏幕的窗口中打开所选设定集，便于编辑较长的条目。条目所在行中的展开按钮会直接打开该条目；书籍窗口的 *Lorebook* 标签页也有同样的按钮，用于打开该页显示的设定集。窗口标题为设定集的名称。它由一组标签页组成，与[故事编辑器](../books/story-editor.md)的相同：拖动标签页可将其放在另一个标签页的旁边或下方，拖动它们之间的分隔条可调整大小。

![全屏设定集编辑器：条目列表和两个以标签页打开的条目](../../images/lorebook-fullscreen.png)

- **Entries** 标签页列出条目，并提供设定集页面上的搜索框以及标签和状态筛选器，还有 **Add Entry**、**Mass edit**、**Tag entries**、**Preview activation** 和 **Refresh**（批量编辑和标签操作也会在已打开的标签页中显示变化）。勾选 **Include sub lorebooks** 后，列表还会包括[子设定集](#sub-lorebooks)中的条目，每个条目都标有其所属设定集的名称；此时 **Add Entry** 会将条目添加到其旁边所选的设定集中，且此类条目的标签页会标明其所属设定集。该标签页无法关闭。每一行有三个按钮：
  - **Open**（铅笔图标，双击该行也可）会在你最近使用的条目标签页中显示该条目；若没有这样的标签页，则会在列表右侧的一栏中打开一个新的条目标签页；
  - **Open in new tab**（箭头图标）会在你最近使用的条目标签页旁边为该条目新增一个标签页；
  - **Delete**（垃圾桶图标）会在确认后删除该条目，并关闭其标签页。

  同一条目最多只在一个标签页中打开：再次打开时，会显示它已有的那个标签页。
- **条目标签页**从上到下依次包含：**Name**、**Order** 和 **Enabled**；**Settings** 是一个可折叠的面板，包含条目的其余设置（筛选模式和筛选器、插入模式、激活上下文和部分、标签和负面标签、部分的范围、递归的复选框和条件，参见[条目与激活](entries.md)）；然后是 Markdown 编辑器中的**内容**，以及在勾选 **Show note** 时位于其旁边的**备注**。每个标签页会记住它显示的是文本、文本与预览，还是仅预览，重新打开窗口时也保持不变。眼睛图标会为该条目打开[激活预览](entries.md#preview-activation)。底部一行显示内容的估计词元数；如果模板有问题，会以红色显示问题所在（仍会照常保存）。关闭标签页请点击其 **×**。
- **条目中的图片。** 将图片粘贴到内容中、拖放到内容上，或用工具栏的图片按钮选择（支持 PNG、JPEG、GIF 或 WebP，最大 10 MB）。图片会与你的文件一起保存（*Resources* 标签页），内容中会在光标所在位置生成一行类似 `![Map](resource:…)` 的文本；预览中会显示图片，方括号中的文字是它的图注。删除该行即可从条目中移除图片。模型读取条目时会忽略这些行。只有当模型设置为 *Multimodal (accepts images)* 时，才会发送图片本身（参见[推理提供方](../inference-providers.md)）。图片会随设定集一同转移：克隆、共享设定集的副本、导出的设定集文件以及包含文件的书籍备份都会携带图片，而副本会获得自己的图片。SillyTavern 导出和书籍导出则不包含图片。故事的 *Lorebook* 标签页会在条目文本下方显示图片。图片目前不计入词元估计。
- **在条目之间跳转。** 在内容的正文或预览中，按住 Ctrl 单击（Mac 上为 Cmd 单击）另一个条目的名称或关键词，即可在你最近使用的条目标签页中打开该条目；再加按住 Shift，则在新标签页中打开。底部一行还会在 **Mentioned in:** 之后列出内容提及本条目的其他条目，单击即可打开，按住 Shift 单击则在新标签页中打开。条目通过其名称和筛选器中的词来识别（每个关键词、*Text* 筛选器的完整文本，以及正则表达式匹配的内容），以整词匹配，且不区分大小写。这与[递归](entries.md)所遵循的规则相近但并不相同：递归还会找到更长单词中的关键词，要求 *AND* 筛选器的所有关键词都出现，并检查标签和条件。
- Markdown 编辑器有一个工具栏（粗体、斜体、删除线、分隔线、标题、引用、代码、代码块、表格以及三种列表），右侧是 *edit*、*edit with preview*（默认）和 *preview* 三种视图。常用快捷键均可使用（Ctrl+B、Ctrl+I 等）；按 Tab 会跳到下一个字段，而不是缩进。你输入的内容会在停止输入片刻后以及离开编辑器时保存。
- 窗口顶部显示已启用条目的估计词元数，以及 **Count with subbooks** 和 **Search and replace**；后者也会在已打开的标签页中显示替换后的文本。

所有更改都会在你编辑时即时保存，因此点击 **Close** 不会丢失任何内容。窗口会为每个设定集记住你离开时的状态：下次打开时，相同的条目会以相同的标签页和相同的排列方式再次出现（已被删除的条目则不再显示）。关闭窗口后，设定集页面会重新显示条目。

**在别处所做的更改。** 标签页中的更改会以该条目当前保存的状态写入条目，因此在别处（设定集页面、批量编辑、另一个窗口）对条目*其他*字段所做的修改会保留，并在标签页中显示。如果自标签页显示以来，**内容**或**备注**在别处被修改，你的文本不会覆盖已保存的内容，底部一行会提示此情况，并提供 **Keep my text**（仍保存你的文本）和 **Load the saved text**（放弃你的文本）两个选项。在别处被删除的条目，其标签页会随之关闭。

窗口还会跟随打开期间保存的内容：当某个代理或其他窗口（另一个浏览器标签页、故事的设定集标签页）保存或删除条目时，列表会重新加载，该条目的标签页也会显示其当前状态（被删除的条目，其标签页会关闭）。等待 **Keep my text** 或 **Load the saved text** 选择的标签页会保持原样，直到你作出选择。若你输入时恰好同一秒内保存了其他更改，两者仍可能相互覆盖，此时底部一行会像上面那样提示你选择。

**代理。** 当代理正在使用包含该设定集的书籍时，此窗口不会提供。如果窗口打开期间代理开始运行，窗口会在你下一次修改或切换标签页时变为只读，并显示一条提示栏，指出代理的名称（若你最近一次修改未能保存，也会说明）。请暂停或停止运行，然后点击 **Check again**：条目会按运行结束时的状态重新加载，你即可再次编辑。

[Lore Generator](../extensions/lore-generator.md) 和 [Lorebook VCS](../extensions/lorebook-vcs.md) 扩展在此同样可用：魔杖图标位于列表的各行中以及条目标签页的名称行中，修订面板位于列表上方，条目的修订栏位于其标签页中。

<a id="tagging-many-at-once"></a>

## 一次为多项添加标签

工具栏上的两个标签按钮会打开同一个窗口，用于为多个项目添加或移除标签：**Tag lorebooks** 列出你的设定集，**Tag entries** 列出所选设定集中的条目。选择 **Tag**（或输入新标签并按 Enter）；列表会显示每个项目及其标签，带有所选标签的项目会排在最前面，并突出显示该标签。每行的 **+** 和 **-** 按钮可为该项目添加或移除标签；勾选多个项目（标题中的复选框可全选），然后使用底部的 **Apply the tag to the checked** 或 **Remove the tag from the checked**，即可对所有勾选项目执行操作。代理正在处理的项目（例如运行中代理所在书籍的设定集，因此也包括其所有条目）会显示为灰色，在运行暂停或停止之前无法更改。该窗口也可为[书籍](../books/managing-books.md#tagging-many-books-at-once)添加标签。

此处为条目添加的标签是普通标签，参见[标签](entries.md#tags)；负面标签则直接设置在条目本身上。

![为设定集中的条目添加标签](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## 在书中使用设定集

打开书籍及其 **Lorebook** 标签页，然后在 **Lorebook** 字段中选择设定集。下方的编辑器会显示该设定集，因此你可以在写作时添加和修改条目。清空该字段即可在不使用设定集的情况下写作。

一本书只使用一个设定集。若要同时使用多个设定集，请新建一个将它们作为子设定集包含在内的设定集，并使用该设定集。

同一个设定集可被任意数量的书籍使用。对它的更改会从下一次生成的部分起影响所有使用它的书籍；已经写好的部分则保留写作时所用的设定。
<a id="sub-lorebooks"></a>

## 子设定集

**Sub Lorebooks** 会把其他设定集中的条目纳入本设定集。典型的设置如下：

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- 子设定集本身也可以拥有子设定集，所有层级都会被纳入。
- 一个设定集可以同时是多个设定集的子设定集。如果同一设定集被多次引用，它的条目仍只会使用一次。
- 设定集不能直接或通过其他设定集包含自身。此类选择会被拒绝，并显示 *Cannot select sub lorebook: cycle detected.*
- 被停用的子设定集会连同它所包含的全部内容一起被排除。

![一个设定集包含两个子设定集：带与不带子设定集时的词元](../../images/lorebook-sub-lorebooks.png)

所有被纳入的设定集的条目会合并在一起，并按各自的[顺序](entries.md#order-and-insertion)排列，与它们来自哪个设定集无关。

<a id="disabling-and-deleting"></a>

## 停用与删除

取消勾选 **Enabled** 可以暂时停用某个设定集，例如想看看作品在没有它的情况下读起来如何。关联它的作品仍会保留该引用，重新勾选后它会再次生效。

**Delete Lorebook** 会将该设定集从你的列表中移除。曾使用它的作品不再获取其条目，将其作为子设定集包含的设定集也会跳过它。数据会保留在数据库中，直到管理员运行[清理](../administration/cleanup.md)；在此之前，你可以从[回收站](../trash.md)中恢复该设定集。[作品备份](../books/backups.md)和导出的文件中的副本不受影响。
