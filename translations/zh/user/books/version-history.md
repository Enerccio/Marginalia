---
label: 版本历史
order: 85
---

# 部分的版本历史

每当一个部分的文本发生变化，Marginalia 都会保留它之前的文本。你可以查看该部分的较早版本，
查看它们之间的变化，恢复某个版本，为版本命名以防丢失，并搜索旧文本。

一个部分会因以下情况被修改：模型编写它（首次生成、**Regenerate**、**Swipe**、[**Redraft**](story-editor.md#redrafting-a-part)）；
你本人修改（**Edit** 和 **Save**）；批量操作（**Search and replace**、导入、命令）；
[智能体](../agents.md)；[扩展](../extensions/index.md)；[合并](story-editor.md#merging-a-part-into-the-previous-one)两个部分；
以及恢复某个版本。每一种情况都会生成一个**版本**，并记录时间和操作者：

| 来源 | |
|---|---|
| **Generated**、**Regenerated**、**Redrafted** | 由模型编写的文本。该版本会注明模型和协议，并保留随之产生的推理过程。 |
| **Author** | 由你键入。由命令、批量操作、导入或智能体所做的更改会在版本的那一行中注明（智能体会被标记）。 |
| **Extension** | 由扩展修改的文本。 |
| **Merged** | 被合并到当前部分中的另一部分的文本。 |
| **Restored** | 被重新放到最上面的版本，是它所指版本的副本。 |
| **Saved version** | 你手动保存并命名的版本。 |

历史记录并不是书的第二份副本。版本与部分分开存储，因此即使历史很长，故事也不会变慢；
导出、发布或在阅读器中打开的书，只包含当前文本。

<a id="looking-at-the-history"></a>

## 查看历史

在部分的菜单（☰）中选择 **History**。括号中的数字是该部分拥有的版本数量。（当部分的文本处于编辑状态时，或对于尚未保存的部分，不提供此项。）

![带有 History 项的部分菜单](../../images/history-part-menu.png)

![部分的历史：左侧为版本，右侧为所选版本及其变化](../../images/history-dialog.png)

左侧列表中最新的版本在最上面。每一行显示时间、来源、字数，以及版本名称（如果有）；最新的版本标记为 **Current**，即部分现在的文本。点击某一行即可在右侧查看该版本。文本上方可以选择与哪个版本进行比较：

| | |
|---|---|
| **Previous version** | 与其前一个版本相比的变化，逐词对照。 |
| **Current text** | 与部分现在的文本相比的变化。 |
| **Text only** | 原样显示的文本，不做比较。 |

由模型编写的版本会在一个可展开的区块中显示其推理过程。**Restored** 版本会注明它是哪个版本的副本。

在手机上，对话框会占满整个屏幕：版本以卡片形式排列在所选版本上方，操作无需鼠标。

<a id="putting-a-version-back"></a>

## 恢复某个版本

有两种方式，它们的区别在于之后的版本如何处理：

| | |
|---|---|
| **Restore on top** | 所选版本的文本重新成为部分的文本，并作为一个新的 **Restored** 版本出现在最上面。没有任何内容丢失：中间的版本仍保留在列表中。用于收回较早的措辞。 |
| **Roll back to here** | 部分回退到所选版本，**所有比它更新的版本都会移入历史的回收站**（系统会询问你，并告知数量）。这些版本之后仍可从回收站中取出。用于撤销整段修改。 |

两者都会像编辑一样改变部分：描述旧文本的[摘要](summaries.md)会过时，Marginalia 会告诉你有几个，并且该部分会被计为已编辑。故事会立即显示恢复后的文本。

<a id="naming-a-version"></a>

## 为版本命名

带有名称的版本称为**已保存版本**，历史记录不会自行删除它。

- 对话框中的 **Save version** 会以你给定的名称保存部分当前的文本。
- **Label…** 为已存在的版本命名，或修改、删除其名称。

在尝试有风险的操作之前使用它：*编辑喜欢的那版草稿*、*删掉决斗之前*。

<a id="deleting-versions"></a>

## 删除版本

- **Delete** 会把版本放入**历史回收站**。勾选 **Show deleted versions** 可查看回收站（这些行呈灰色）；**Take out of trash** 会把版本放回列表中原来的位置，**Delete permanently** 则永久删除某个版本。最新版本无法删除，因为它就是部分的文本。
- **Empty trash** 永久删除回收站中的全部内容。
- **Clear history** 永久删除该部分除当前版本以外的所有版本。对话框会说明将删除多少个版本以及它们占用多少空间。**Keep saved versions and the first generated version**（默认勾选）会保留你命名过的版本，以及模型为该部分首次生成的内容。

![Clear history 即将删除的内容](../../images/history-clear.png)

故事的设置菜单（⚙）中对整本书也有相应操作：**Clear history of the book** 和 **Empty trash of the history**。

![故事设置菜单中关于整本书历史的操作](../../images/history-menu.png)

<a id="how-many-versions-are-kept"></a>

## 保留多少个版本

除非有限制另行规定，一个部分最多保留 **50 个版本**。当新版本会超出上限时，历史会先删除回收站中最旧的版本，然后删除不在回收站中的最旧版本。回收站中的版本在被彻底清除之前都计入上限。有三类版本永远不会被删除：最新版本、有名称的版本，以及模型为该部分首次生成的内容（它们也不计入上限）。

该上限可以在四个层级设置，**最严格的那一级生效**：

| 层级 | 位置 |
|---|---|
| 书 | 书的 [About](managing-books.md#the-about-tab) 选项卡中的 **Versions kept per part**。 |
| 文件夹 | [文件夹设置](folders.md#folder-settings)中的 **Versions kept per part**；会查看书之上的所有文件夹。 |
| 你 | [Settings](../account.md) 选项卡中的 **Versions kept per part**。 |
| 管理员 | [权限](../administration/permissions.md)中的 **Version history**，即用户的上限。 |

字段为空表示该层级不设限制。**0** 表示完全不保留历史（已有的版本会保留，直到你清除它们，但不会再新增任何版本）。字段的提示文字会显示它所继承的上限以及由哪一层级设定，例如 *Limited to 20 by the folder "Novels"*。书不能把上限设置得高于上层已设定的值。

<a id="searching-the-old-text"></a>

## 搜索旧文本

主要的搜索只查看当前文本，因此你改掉的旧句子不会让某个部分出现在结果中。若要找到它，请勾选 **Include versions**（默认未勾选，且不会被记住，因此搜索永远不会意外地返回旧文本）：

- 在[书籍的高级搜索](managing-books.md#advanced-search)中：只要某本书的某个部分的较早版本含有这些词，该书就会被找到；
- 在[分支视图](branches-and-story-tree.md#searching-all-branches)的搜索中：仅在较早版本中含有这些词的部分会被标记，搜索结果会显示 *Found in the version of 12 Oct 2026 14:05 (Generated)*，点击 **Show in history** 可打开该部分的历史并定位到该版本。

![通过较早版本找到的部分](../../images/history-search.png)

智能体可以通过 `search-parts` 命令及其 `includeVersions` 选项完成同样的操作；每条结果都会注明它来自哪个版本。回收站中的版本不会被搜索，**Search and replace** 也绝不会修改任何版本。

<a id="backups-copies-and-the-trash"></a>

## 备份、副本与回收站

- [书籍备份](backups.md#the-history-of-the-parts)可以连同历史一起备份（**Include version history**，默认未勾选，因为可能很大）。恢复这样的备份（无论是作为整本书还是作为副本），都会带回版本，包括它们的名称和回收站内容。未包含历史的备份则会为每个部分生成一个初始版本。
- [书籍副本](managing-books.md#cloning-a-book)会开始属于它自己的历史：每个部分一个版本，内容取自复制时的文本。
- 删除一个部分或一本书，会连同其版本一起删除；只要该部分仍在[回收站](../trash.md)中，这些版本也会一并保留，并会随之被永久删除。
- 将一个部分合并到前一个部分中，会删除被合并部分的历史；其文本会以 **Merged** 版本出现在保留下来的部分的历史中。
<a id="books-from-before-the-history"></a>

## 早于历史记录的内容

Marginalia 更新时，所有已存在的部分都获得了初始版本：由模型撰写的部分会以**Generated**版本开始，即模型撰写时的文本（包括其推理过程和所用模型）；如果你之后编辑过该部分，则还会有一个**Author**版本；其他部分则以**Author**版本开始。这些部分早先的编辑记录已无从得知，因此不会出现在列表中。
