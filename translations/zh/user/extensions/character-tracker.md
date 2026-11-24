---
label: 角色追踪器
order: 19
---

# 角色追踪器

故事书侧边栏中的人物列表：每个角色都有一句标语、一段备注、一个图标和一种颜色，此外还有角色之间的关系，以及一张关系图。

<a id="the-characters-tab"></a>

## 角色标签页

[故事编辑器](../books/story-editor.md)的左侧边栏在 *Outline* 旁边增加了一个 **Characters** 标签页。列表上方是一排按钮：**Add**（新建角色）、一个用于导入设定条目的菜单、**Relationship map**，以及一个对话气泡按钮，用于设置提示词（鼠标悬停可查看名称）。

对话气泡按钮会打开一个弹出面板，其中有 **In prompt**、**Depth** 和一个角色（role），与[作者注释](authors-note.md)类似。角色出现在提示词中时，该按钮为绿色。勾选 *In prompt* 后，每次生成内容时，角色都会以一条消息的形式加入：每个角色占一行，包含姓名、括号中的标语和备注；另有一个关系列表（若关系为双向，则显示 `Mara -> Tomas: employs`、`<->`）。*Depth* 表示插入位置之后的消息条数（`0` 为最末尾，即你的指令之后；`1` 为紧挨指令之前）。角色（role）可选 *System*、*Assistant* 或 *User*。这段文字占用故事的 token 空间。书中没有角色时，不会插入任何内容。设置随书一起保存。

两个按钮之间的魔杖按钮带有菜单项 **Import lore entries as characters**，详见[下文](#importing-characters-from-the-lorebook)。

列表按名称排序。每个角色都有自己的颜色，名称下方以较小的字体显示**标语**（最多两行）。角色的按钮如下：

| 按钮 | |
|---|---|
| 箭头 | 展开角色，显示其**图标**和**备注**。 |
| 铅笔 | 在对话框中打开角色。 |
| 垃圾桶 | 删除角色及其所有关系，删除前需确认。 |

角色一经修改即随书保存，因此会包含在备份中。

![角色标签页](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## 为一轮对话选择角色

[新一轮](../books/story-editor.md)表单中有 **POV character** 和 **Present characters** 两个字段。它们仍是自由文本，但末尾各有一个带人物图标的按钮，点击后会按名称顺序列出书中的角色。在 **POV character** 的列表中选择的名称会替换字段中的内容；在 **Present characters** 的列表中选择的名称会添加到末尾，以逗号分隔，若已存在则不再重复添加。列表在打开时读取，因此你刚添加的角色也会出现在其中。你仍可以输入任何名称，字段中的名称也不必是书中的角色。

<a id="the-character-dialog"></a>

## 角色对话框

**Add**（带加号的按钮，提示文字为 *Add new character*）和铅笔按钮打开的是同一个对话框：

| 字段 | |
|---|---|
| **Name** | 必填。 |
| **Tagline** | 关于角色的几个词，显示在列表和关系图中。 |
| **Note** | 任何你想记住的内容。 |
| **Icon** | 上传 PNG、JPEG 或 GIF 图片。图片会被缩小至最大 200 x 200 像素，并保持比例。 |
| **Color** | 角色在列表和关系图中的背景色。 |

点击 **Save** 应用这些字段。

![带有关系的角色](../../images/plugin-charactertracker-dialog.png)

如果在有未保存的更改时按下 **Cancel**（或 Esc），包括你已填写但尚未添加的关系，系统会询问你是否放弃这些更改。

![取消带有未保存更改的角色时的提示](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### 关系

打开已有角色时，对话框还会列出它的关系：该角色指向其他角色的关系（`→`）、指向该角色的关系（`←`），以及单向关系（`↔`）。每项都显示名称和标语。垃圾桶按钮可删除一段关系。

要添加关系，先搜索另一个角色，选择 **Direction**（*this character → other*、*other → this character* 或 *both ways*），输入 **Name**（例如 *employs*、*friends*），可选填 **Tagline**，然后点击 **Add relationship**（该按钮位于这些字段上方）。关系会立即保存。新角色需先保存，之后才能添加关系。

<a id="importing-characters-from-the-lorebook"></a>

## 从设定集导入角色

如果你的角色已经写在设定集（lorebook）中，魔杖菜单可以为你自动创建。**Import lore entries as characters** 会打开一个对话框，列出本书设定集及其所有子设定集中的条目，并按其 *order* 排序。每个条目显示名称和一个复选框，还有一个 **i** 图标，点击后会在弹出面板中显示条目内容，便于你在决定前快速查看。列表标题中的复选框可以全选或全不选。点击 **Import** 会为每个勾选的条目创建一个同名角色（标语、备注、图标和颜色留空，由你之后填写）；点击 **Close** 则直接离开，不做导入。

条目不会与你已有的角色比较：同一条目导入两次，会创建两个角色。

![从设定集导入角色](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## 关系图

**Relationship map** 以图形方式显示书中的所有关系。每个有关系的角色都是一个圆圈（没有图标时为方框），以其颜色为底，并显示名称和标语；每段关系则是两个角色之间的一条箭头，箭头上显示名称和标语。箭头指向目标；单向关系两端都有箭头。没有关系的角色不会出现在图中。

![关系图](../../images/plugin-charactertracker-map.png)

角色会围绕某一个角色排成环状，起初是关系最多的那个：与它相关的角色位于第一环，与它们相关的角色位于第二环，依此类推。**双击一个角色**可以围绕它重新排列关系图。你也可以拖动角色、移动关系图，并用鼠标滚轮缩放。

<a id="commands"></a>

## 命令

角色同时也是[命令](../commands/index.md)，可用于命令行以及[代理](../agents.md)。它们作用于当前打开的书（代理则作用于其工作副本），打开的角色标签页会立即显示变化。角色通过其**名称**（不区分大小写）指定；当两个角色同名时，也可以通过 `/list-characters` 中的**编号**（即名称的排序序号）指定。关系则通过 `/list-character-relations` 中的编号指定。修改角色的命令只会更新你提供的字段；空文本会清除标语或备注。图标只能在对话框中设置。

| 命令 | 作用 |
|---|---|
| `/list-characters` | 按名称排序的角色：标语、颜色、是否有备注，以及关系数量。只读。 |
| `/get-character character` | 一个角色及其备注和关系。只读。 |
| `/create-character name [tagline [note [color]]]` | 添加角色。`color` 为 `#rrggbb`。 |
| `/update-character character [name [tagline [note [color]]]]` | 修改角色。 |
| `/delete-character character` | 删除角色及其关系，删除前需确认。 |
| `/list-character-relations [character]` | 全部关系，或某个角色的关系。只读。 |
| `/add-character-relation source target name [tagline [bidirectional]]` | 添加从 `source` 到 `target` 的关系（*employs*），或在带有 `bidirectional` `true` 时添加双方对等的关系（*friends*）。 |
| `/update-character-relation relationship [name [tagline]]` | 修改关系的名称或标语。 |
| `/delete-character-relation relationship` | 删除关系，删除前需确认。 |
| `/character-tracker-map` | 以提示词所接收的文本形式输出角色及其关系。只读。 |

`character-tracker-map` 是为[{%{`{{command::character-tracker-map}}`}%} 宏](../templates/macros.md#commands)准备的：将它放入主模板、用户提示词或设定条目中，即可把角色放在提示词中你想要的位置，而不是（或在之外）使用角色标签页的自动插入功能。无论自动插入是否开启，它始终反映角色的当前状态。

添加和修改都不具破坏性；只有 `delete` 类命令具有破坏性，代理可以设置为在执行这些命令前询问你。

[情节向导](arc-outline.md)中的 Arc Outline 在你勾选其 **Characters** 工具时，会使用相同的命令，因此你可以在讨论情节的同时完善角色阵容。

<a id="phone"></a>

## 手机版

该标签页位于故事的抽屉中，与大纲相邻，就像[作者注释](authors-note.md)一样。手机版的新一轮表单中同样有 **POV character** 和 **Present characters** 的按钮。
