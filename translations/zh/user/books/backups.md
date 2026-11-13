---
label: 书籍备份
order: 50
---

# 书籍备份

书籍备份是对一本书的快照，包括它的设置和提示词、所有分支中的全部段落、摘要、标签以及设定集（lorebook）。你可以用备份回到故事的早先状态、复制一本书，或者把一本书迁移到另一个 Marginalia 安装实例或另一位用户那里。

书籍备份与管理员为整个安装实例所做的[数据库备份](../administration/database-backups.md)是分开的。

![书籍的“备份”标签页](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## 创建备份

在书籍的 **Backups** 标签页中，点击 **Take Backup**。新备份会出现在列表顶部，显示其 **Date**（日期）、所含的 **Messages**（段落）数量，以及其段落使用的 **Images**（图片）数量（即[段落图片](story-editor.md#images)）。

备份也可以自动创建，参见[下文](#automatic-backups)。

备份包含整个故事树、摘要以及段落的[批注](annotations.md)。恢复备份、复制书籍和导入备份时，批注也会一并恢复。

<a id="the-history-of-the-parts"></a>

## 段落的历史

除非你特别要求，备份默认不包含段落的[版本历史](version-history.md)。如需包含，请勾选 **Take Backup** 旁边的 **Include version history**（默认未勾选，因为历史记录的体积可能是故事本身的数倍）。恢复这类备份（无论是恢复原书还是恢复为副本）时，版本会连同名称、生成版本的推理过程以及历史中的回收站内容一并恢复；版本数量会被裁剪到它们所进入的那本书的上限之内。自动备份从不包含历史。恢复不含历史的备份时，每个段落只会得到一个初始版本，与早于版本历史功能的书籍相同。

<a id="backups-and-folders"></a>

## 备份与文件夹

备份会记录书籍原先所在的[文件夹](folders.md)，从顶层文件夹一直到所在文件夹，每个文件夹都包含其名称、描述、语言以及它设置的文本选项。**Restore as new book** 和 **Clone Backup** 会把新书放入备份中记录的文件夹（如果你仍然拥有该文件夹）；否则，会放入同名的文件夹（你已有的同名文件夹直接使用，缺少的文件夹则按原有设置新建）。没有文件夹的备份，或者无法建立文件夹的备份，会恢复到顶层。对书籍执行 **Restore**（完整恢复或仅恢复段落）时，书籍会保留在原来的文件夹中。文件夹的标签不包含在备份中。旧版备份的恢复方式保持不变。

<a id="pictures-in-a-backup"></a>

## 备份中的图片

备份会记录段落使用了**哪些图片**（以及图片说明），但**不包含图片文件本身**。这样可以让备份（包括自动备份）保持较小的体积。图片仍保存在你的[资源](../resources.md)中。如果希望备份中包含图片，请在[导出](#exporting-and-importing)时将其打包。

<a id="restoring-a-backup"></a>

## 恢复备份

点击备份旁边的 **Restore**，然后选择：

| 选项 | 恢复内容 |
|---|---|
| **Full Restore** | 全部内容：名称、描述、发布设置、提示词及其他书籍设置、标签、模型、协议、设定集，以及所有段落。 |
| **Messages Only Restore** | 仅恢复段落（所有分支）和摘要。书籍当前的设置、提示词和设定集保持不变。 |

!!!warning
恢复会用备份中的内容替换书籍的**全部段落**。备份之后写的段落都会丢失。如果以后可能还需要当前的内容，请先为当前状态创建一份备份。
!!!

书籍窗口会关闭，然后以恢复后的内容重新打开。

已恢复段落的图片，以及段落所指向的其他文件（例如[有声书](../extensions/audiobook.md)的音频），都是备份中列出的那些，前提是你仍在[资源](../resources.md)中保留它们。如果你之后删除了某个资源，它就会从段落中消失。恢复操作不会找回已删除的文件，除非该备份来自带有资源的导出文件，并且你已经[导入](#importing-a-backup)了它。

**模型和协议**会在你的推理服务提供方和协议中查找：先找备份中完全相同的那一个，再找同名的那一个。如果两者都不存在，书籍会保留当前的模型或协议。

备份中的**设定集**会与你现有的设定集进行比较。如果存在同名但身份不同的设定集，Marginalia 会逐一询问你，对于备份中的每个设定集，选择 **Link to existing**（关联到现有设定集）或 **Create new**（新建）。你完全没有的设定集可以选择 **Import**（导入），也可以选择 **Don't import**（不导入）。

![选择如何处理设定集](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## 复制书籍

**Clone Backup** 会根据备份创建一本新书，名称由你输入。原书不会被修改。你可以用这个功能在副本上尝试一些大胆的改动，或者从同一个开头另起一本新书。

如果备份指向了资源（图片、有声书的音频或其他文件），Marginalia 会询问你选择 **With resources**（包含资源）还是 **Without resources**（不含资源）：

- *With resources*：新书会获得这些资源各自的**独立副本**（文件相同，但在[资源](../resources.md)中有新的条目）。这样，在一本书中删除或替换图片，不会影响另一本书。备份创建之后你已删除的资源会被排除在外。
- *Without resources*：仅复制文本。

![复制时选择是否包含资源](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## 导出与导入

**导出**会把备份下载为 JSON 文件（`<book name>_backup.json`）。可以留作额外副本，或带到另一个安装实例中使用。

如果备份引用了资源，Marginalia 会询问是否将其打包（包括各部分的图片，以及其数据中提到的其他文件，例如有声书的音频）：

![导出包含图片的备份](../../images/backup-export-images.png)

- **不含资源**：立即下载上述 JSON。资源在文件中只是被引用。
- **含资源**：把备份及其全部资源打包成一个 **ZIP 文件**（`<book name>_backup.zip`）。进度条会显示已完成的数量；文件是逐个写入磁盘的，因此即使书中有许多大文件，也不会占用太多内存。长篇有声书可达数百兆字节，所以只在需要时才打包。准备就绪后，点击 **Download**。该文件只保留到您关闭该对话框为止。

如果某个资源因为已被您删除或其文件缺失而无法打包，对话框会告知被略去的数量。

*Settings* 标签页中导出按钮旁边的复选框 **Include inherited settings in the export**（默认开启），决定了文件中关于本书从[文件夹](folders.md#where-the-settings-of-a-book-come-from)所继承设置的内容。开启时，本书未自行设置的语言、母版模板、人称、时态、风格和提示词，会作为其自身的值写入文件中。这些值取自备份时本书所在的文件夹（由近到远），之后再取自您的设置，因此本书在没有这些文件夹和设置的安装实例或账户中，显示效果也相同；内置默认值不会写入。关闭时，文件中只包含本书自行设置的内容。文件从不记录本书位于哪个文件夹：书籍归档到哪里是本地的选择。（故事导出，即[导出书籍](export.md)，只写入正文，没有设置。）

ZIP 包含 `backup.json`（即上述 JSON）、`resources.json`（资源列表）以及各个文件。您无需解压：两种恢复备份的方式都接受 `.zip` 和 `.json`。

有两种方式可以把备份文件重新导入：

- *Backups* 标签页上的 **Import Backup** 会把文件加入**本书**的备份列表。在您对其执行 *Restore* 之前，不会有任何改变。
- *Settings* 标签页上的 **Import Backup as New Book** 会从该文件创建一本**新书**。

两者都接受 `.json` 或 `.zip` 文件。文件会上传到服务器并在那里读取，因此较大的备份也没有问题。

<a id="importing-a-backup"></a>

### 导入备份

当文件是包含图片的 ZIP 时，图片会先被添加到您的[资源](../resources.md)中，备份随后使用这些图片。您已有的图片（同一文件）不会被重复存储：会使用已有的那一份，因此导入您自己的导出文件不会增加任何内容。不含图片的 JSON 文件则按原样导入。

以**新书**方式导入时，即使您已经拥有相同的图片，新书也会在资源中获得**自己的条目**：这些条目指向相同的文件，因此不会重复占用磁盘空间。如果文件没有图片（普通 JSON），新书会获得文件所列出且您仍然拥有的那些图片的副本；您没有的则被略去。文件中任何不是可用图片的内容（不是 PNG、JPEG、GIF 或 WebP，或者过大）都会被忽略。

<a id="importing-a-backup-as-a-new-book"></a>

### 将备份导入为新书

1. 打开 **Settings** 标签页，点击 **Import Backup as New Book**。
2. 输入 **New Book Name**——名称填写后，上传功能才会启用。
3. 上传备份文件。
4. 如有询问，决定如何处理长篇设定集（lorebooks）。

新书会出现在 *Books* 标签页中。

<a id="deleting-backups"></a>

## 删除备份

垃圾桶按钮会永久删除一个备份。自动备份不会被轮换：它们会一直保留，直到您将其删除。

<a id="automatic-backups"></a>

## 自动备份

Marginalia 可以在您写作时自行生成备份。请在 **Settings → General Settings → Backup Strategy** 中设置所有书籍的默认值：

| 策略 | |
|---|---|
| **No Backups** | 不做自动备份。 |
| **After N Messages** | 每生成 *N* 个部分后备份一次。 |
| **After N Minutes** | 生成一个部分后，若距离上次自动备份已过去至少 *N* 分钟，则备份一次。 |

若要为某本书使用不同的策略，请在其 *Backups* 标签页中勾选 **Per manuscript override**，并在此选择策略和数值。取消勾选则使该书恢复为 *Settings* 中的默认值。

自动备份只在生成部分后才会进行，因此只阅读而不写作的书不会产生新的备份。

<a id="where-backups-are-stored"></a>

## 备份的存储位置

每个备份都是 Marginalia 数据文件夹中的一个 JSON 文件：

```
~/.marginalia/data/<your login>/backups/manuscripts/<book id>/
```

在 [Docker 版本](../getting-started/docker-server.md)中，对应宿主机上的 `./data/data/<login>/...`。这些文件不在数据库中，因此[数据库备份](../administration/database-backups.md)不包含它们。请将整个 `.marginalia` 文件夹纳入您自己的备份范围。

图片文件也不在备份中，它们位于与 `backups` 并列的 `data/<your login>/images` 文件夹中。它们同样不在[数据库备份](../administration/database-backups.md)中，详见该页说明。
