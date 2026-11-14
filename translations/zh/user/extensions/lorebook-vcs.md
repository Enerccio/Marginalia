---
label: Lorebook 版本控制
order: 9
---

# Lorebook 版本控制

[Lorebook 条目](../lorebooks/entries.md)的修订历史：保留设定的早期版本，并在它们之间切换，例如战争前后的世界状态，或角色在故事不同时间点的样子。

!!!
Lorebook 版本控制在[手机布局](../phone.md#extensions)中同样可用：面板位于 lorebook 的 *Enabled* 行下方，条目的修订位于其表单顶部。
!!!

!!!
在[全屏编辑器](../lorebooks/lorebooks.md#editing-in-full-screen)中，**Lorebook Revisions** 行位于条目列表上方，条目的修订栏位于其标签页名称行下方。切换修订会立即在标签页中显示。
!!!

修订是整个条目的快照：名称、内容、备注、启用状态、顺序、过滤器和过滤模式、插入模式、*Do not search its content for other entries*、标签和负面标签。（在该标志出现之前制作的修订，读取时视为关闭。）

![条目的修订栏](../../images/plugin-lorebookvcs.png)

<a id="entry-revisions"></a>

## 条目修订

安装扩展后，每个 lorebook 条目的详情（⌄）都以修订栏开头：

```
Revision: 2 / 3            [−]  [‹]  [Rev 2 (2026-10-08 14:30:12) ▾]  [›]  [+]
Modified: 2026-10-08 14:30:12
```

修订的工作方式类似插槽。条目始终显示其中一个修订，即**当前修订**，你在条目中所做的更改都属于该修订。生成内容时始终使用条目的现有状态，因此模型看到的是当前修订。

| 控件 | |
|---|---|
| **+** | 将条目当前的状态保存为新修订，并将其设为当前修订。 |
| **‹** / **›**，或列表 | 切换到上一个、下一个或所选修订。你在离开的修订中所做的编辑会保留；另一个修订则被加载到条目中。 |
| **−** | 删除当前修订，并切换到上一个修订。最后剩下的修订无法删除。 |
| *n / total*、*Modified* | 当前是哪个修订，以及它最后一次被修改的时间。 |

首次打开条目详情时，其当前状态会成为修订 1。

典型用法：

1. 打开某个角色的条目并点击 **+**，修订 2 即为修订 1 的副本。
2. 将修订 2 改为第 10 章事件之后的角色。
3. 继续写作。当你回到第 5 章编辑时，用 **‹** 切换到修订 1；之后再用 **›** 切换回来。

<a id="lorebook-revisions"></a>

## Lorebook 修订

lorebook 编辑器中会出现一行 **Lorebook Revisions**，其中有两个按钮。它们作用于编辑器中所选的 lorebook：

| 按钮 | |
|---|---|
| export | 将 lorebook 所有条目的历史记录下载为 `<lorebook>_vcs_history.json`。 |
| import | **Import Marginalia VCS JSON**：由本扩展导出的历史记录；**Import SillyTavern VCS Extension JSON**：来自 SillyTavern lorebook 版本历史扩展的历史记录。 |

导入会**替换**该 lorebook 的历史记录。如果 lorebook 已有历史记录，或文件是从其他 lorebook 导出的，导入前会先要求确认。

Marginalia 历史记录首先通过条目本身与条目匹配。找不到的条目（例如历史记录来自 lorebook 的另一份副本）则通过其当前修订的名称和顺序匹配；如果只有一个条目具有该名称，则仅通过名称匹配。与任何条目都不匹配的历史记录会被跳过，确认对话框中会显示被跳过的数量。

SillyTavern 历史记录通过条目在 lorebook 中的位置与条目匹配，因此请在添加或删除条目之前，将其导入到[从相同世界信息导入](../lorebooks/import-export.md#importing-sillytavern-world-info)的 lorebook 中。其修订的触发键会成为条目的过滤器，转换方式与[世界信息导入](../lorebooks/import-export.md#importing-sillytavern-world-info)相同。

<a id="storage"></a>

## 存储

历史记录与 lorebook 一同保存在数据库中，因此[数据库备份](../administration/database-backups.md)会包含它。扩展保存在 lorebook 中的数据，也会写入 [lorebook 导出](../lorebooks/import-export.md#exporting)文件、[书籍备份](../books/backups.md)中的 lorebook，以及[智能体](../agents.md)所处理的书籍 lorebook 副本中。但是，导入 lorebook 会创建新条目，而历史记录属于旧条目的身份，因此不会显示在新条目上。要将历史记录转移到导入的副本中，请从原始 lorebook 导出，再导入到副本中。此时条目将按名称和顺序匹配（参见[Lorebook 修订](#lorebook-revisions)）。
