---
label: 世界书
order: 950
icon: archive
---

# 世界书

!!!
[手机版布局](../phone.md#lorebooks)使用同样的编辑器，但采用单列布局：世界书的按钮位于菜单中，世界书的设置位于一个可展开的区域中，条目则打开为表单。只有少数功能仍保留在桌面版中，详见那里的列表。
!!!

世界书存放模型必须了解的关于你的世界的内容：角色、地点、历史、规则、术语。每一条知识都是一个**条目**。在每个生成的部分之前，Marginalia 会判断哪些条目相关，并将它们放入提示词中，因此当卡车·瓦罗船长走进场景时，模型就知道他是谁，而无需让每个条目都占用每次提示词的空间。

![世界书编辑器](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## 整体结构

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- 一本**书**使用一个世界书（在其*世界书*标签页中设置）。
- 世界书可以将其他世界书作为**子世界书**包含进来。它们的条目同样参与其中，因此你可以把共享的素材（被多本书使用的世界观、一组角色）放在独立的世界书中，并在需要的地方引入。
- 每个**条目**都包含放入提示词的文本，以及决定何时使用它的条件。

<a id="when-an-entry-is-used"></a>

## 条目何时被使用

对于每个生成的部分，当以下条件全部满足时，条目会被**激活**：

1. 条目及其所属世界书均已启用；
2. 条目的**标签**与书的标签相符（如果条目有标签）；
3. 条目的**过滤器**与该部分的指令相符（如果条目有过滤器），或者，当条目会检视故事内容时，也与故事的最新部分相符。

被激活的条目会按其**顺序**插入——插入系统提示词的世界观块中，或紧挨在该部分的请求之前。参见[条目与激活](entries.md)。

既没有标签也没有过滤器的条目始终处于激活状态：将它们用于模型必须始终知道的内容。

<a id="lorebooks-and-other-features"></a>

## 世界书与其他功能

- 世界书条目可以使用[模板和宏](../templates/index.md)——SillyTavern 的世界信息仍可正常工作。
- 可以导入[SillyTavern 世界信息](import-export.md#importing-sillytavern-world-info)。
- [书籍备份](../books/backups.md)包含该书的世界书及其子世界书。
- [Lore Generator](../extensions/lore-generator.md) 扩展可以根据你已有的设定，借助模型撰写条目内容。
- [Lorebook VCS](../extensions/lorebook-vcs.md) 扩展会保存条目的修订历史。

<a id="in-this-section"></a>

## 本节内容

- [创建世界书](lorebooks.md)——世界书编辑器、子世界书、在书中使用世界书
- [条目与激活](entries.md)——条目字段、标签、过滤器、顺序与插入
- [导入与导出](import-export.md)——Marginalia 世界书文件与 SillyTavern 世界信息
