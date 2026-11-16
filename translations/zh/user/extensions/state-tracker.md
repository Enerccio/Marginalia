---
label: 状态追踪器
order: 29
---

# 状态追踪器

记录角色当前的**状态**：他们穿着什么、带着什么、手里拿着什么武器、有哪些伤势、情绪如何，以及你定义的任何其他内容。它会把场景中角色的状态放进提示词。模型很容易忘记剑在两章之前已经丢失，或者斗篷已经被淋湿。有了状态追踪器，提示词会说明场景中每个被追踪角色现在是什么样子。

!!!
你决定追踪谁，以及追踪他们的哪些方面。只有你添加到标签页中的角色会被描述给模型，而且只在他们属于正在撰写的部分的*在场角色*时才会描述。在这个版本中，状态由你自己维护（或由代理按下面的命令维护）；让模型跟随故事自动更新，将在以后推出。
!!!

<a id="categories"></a>

## 类别

**类别**是可以对角色进行追踪的一项内容。每个类别包括：

- **名称**，提示词也会使用这个名称（*Wearing*、*Weapon at hand*、*Carrying*）；
- **类型**：
  - **Single value**：一个简短的值，新值会替换旧值（手中的武器、所在位置）。
  - **List**：逐条添加和删除的条目（携带的物品、伤势）。
  - **Description**：一段整体重写的文字（衣着、情绪）。
- **给模型的说明**：该类别包含什么、不包含什么。模型在跟随故事时会读取它。
- **Only what the text says**：开启时，只有文本明确说明时才改变该值；关闭时，也会根据明显的后果改变（游泳后变湿）。模型在跟随故事时会使用这一设置。
- **Say "none" when empty**：角色没有某个值时（武器丢失），提示词会写明 *none*，而不是省略该类别。

<a id="my-categories"></a>

### 我的类别

你自己的类别位于 **Settings → Extension Settings → State Tracker**。书籍从这里复制类别。在你修改之前，系统内置了五个类别：*Wearing*、*Weapon at hand*、*Carrying*、*Injuries* 和 *Mood*。你可以在那里添加、编辑和删除类别，并在设置页面按下 **Save**。**Reset to the built-in categories** 会恢复这五个内置类别。

<a id="the-categories-of-a-book"></a>

### 书籍的类别

每本书都会为它使用的每个类别保存**自己的副本**，因此备份或导出书籍时会包含全部内容，删除你的某个类别也绝不会破坏书籍。状态标签页中的 **Categories of this book** 按钮会列出这些类别：

- **Add from my categories** 将你的某个类别复制到书中。
- **New category for this book** 创建仅属于本书的类别（例如船上的货物、魔法契合度）。这类类别会标记为 *Only in this book*。**Save to my categories** 会把它加入你的类别，以便其他书籍也能使用。
- 与你的类别不再一致的副本会标记为 *Differs from your category*。**Update from my categories** 会采用你的版本。除非你亲自操作，否则不会自动更新，因为修改后的说明会改变模型的行为。
- 编辑书中的类别只影响这本书。当角色已经拥有该类别的值时，它的类型无法更改。
- 从书中删除某个类别，会删除所有角色在该类别中的值。

<a id="the-state-tab"></a>

## State 标签页

[故事编辑器](../books/story-editor.md)左侧边栏新增了 **State** 标签页。所做的任何更改都会随时保存到书籍中。

**Track a character** 会要求填写**名称**、可选的**别名**（即在某个部分的 *Present Characters* 字段中，该角色所使用的其他名称，以逗号分隔），以及**被追踪的类别**。列表中提供书中的类别和你自己的类别；选择你自己的类别时，会将其复制到书中。马只需追踪 *Injuries*，而女主角则可以追踪全部内容。

每个被追踪的角色都会显示其类别，以及每个类别当前的一个或多个值：

- **+** 为单一值或描述类类别设置值（当前值会进入历史记录），或为列表类别添加一个条目。
- 笔图标用于编辑某个值，叉号用于**删除**它：它不再成立，因此会进入历史记录，并且不会发送给模型。
- 锁图标会将某个值标记为**锁定**（例如伤疤、家族之剑）。之后将推出的整理功能会保留锁定的值不变。
- **History** 显示该类别的过往值，并说明它们结束的原因（*replaced* 或 *removed*）。**Restore** 会让某个过往值重新成为当前值（对于单一值或描述类类别，当前值会被移入历史记录以取代它）。Delete 会永久删除某个值。

角色旁边的笔图标用于编辑其名称、别名和类别。取消勾选某个类别会将其隐藏，但保留其中的值；重新勾选即可恢复。垃圾桶图标会停止追踪该角色，并删除其全部状态。

<a id="in-the-prompt"></a>

## 在提示词中

齿轮按钮保存书籍的设置：

- **In prompt** 会在每次生成时，将场景中被追踪角色的状态插入提示词（开启时齿轮变为绿色）。
- **Depth** 和 **Role** 决定它的位置，与作者注释的设置相同：*Depth* 是它之后的消息数（0 表示位于本轮指令之后，1 表示紧接在指令之前）。
- **Name separators** 决定 *Present Characters* 字段如何拆分（默认为 `,` `;` `/` `|` 以及换行）。方括号中的注释会被忽略，因此 `Mara (asleep), Fiora` 能同时匹配两者。

提示词会包含场景中有内容需要说明的角色：

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

你可以在模板的任何位置使用宏 {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} 插入同样的文本（参数可以是任意名称列表）。无论 **In prompt** 是否开启，该宏都会输出状态，因此使用宏时请关闭 **In prompt**，否则模型会收到两次状态。

<a id="commands"></a>

## 命令

以下命令用于命令栏和代理：

| 命令 | 作用 |
|---|---|
| `state-status` | 设置、书籍的类别，以及书中没有副本的你的类别。 |
| `list-state-characters` | 被追踪的角色及其类别。 |
| `track-state-character` | 开始追踪一个角色：名称、别名、按名称指定类别。 |
| `stop-tracking-state-character` | 停止追踪角色并删除其状态（会先询问）。 |
| `list-character-state` | 角色的值，包括当前值和过往值，并带编号。 |
| `set-character-state` | 设置一个值：角色、类别、文本。 |
| `remove-character-state` | 按编号删除一个值：它会进入历史记录。 |
| `state-tracker-block` | 一组角色的提示词文本（宏）。 |
