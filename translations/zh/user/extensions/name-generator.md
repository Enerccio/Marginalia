---
label: 名称生成器
order: 7
---

# 名称生成器

让模型可以请求名字，而不必自己编造。这些名字取自真实名字的列表，包括多个国家的名和姓、地名、神祇、怪物、药物、公司、食物等等，再由马尔可夫链生成新名字。因此生成的名字听起来属于某一类别，却不是原样照抄。

该扩展没有自己的界面，也无需配置。其他扩展会把它的功能提供给模型，目前有[Lore Generator](lore-generator.md#tools)和[Arc Outline](arc-outline.md)。你可以在[为模型提供的工具](tools.md)中选择模型可以使用哪些类别。它的套件名为*Name generator*，与[Name provider](name-provider.md)并列。后者提供的是真实名字，而不是虚构的名字。

![在 Lore Generator 的 Tools 窗口中选择名字类别](../../images/tools-dialog.png)

<a id="what-the-model-can-do"></a>

## 模型能做什么

| 函数 | 作用 |
|---|---|
| `list_name_categories` | 列出名字类别；若给出 `filter`，则只列出名称中包含其中所有词的类别。 |
| `generate_names` | 生成某一类别的新名字，最多 100 个。可选择指定最短和最长长度。 |

若不指定长度，生成的名字会与该类别通常的名字长度相当（长公司名类别生成长名字，短名类别生成短名字）。模型可以要求 2 到 30 个字母之间的其他长度。小类别返回的名字可能少于要求的数量，因为马尔可夫链会用尽不同的结果。模型可以自由选用其中一个名字、修改它，或把两个名字组合起来。参见[工具窗口](tools.md#choosing-the-tools)中 *Name generator* 套件的提示。

<a id="commands"></a>

## 命令

同样的名字也可以通过[命令](../commands/index.md)使用，适用于命令行以及[代理](../agents.md)。代理可以在自己的提示中使用这些命令（例如 [Lore Generator](lore-generator.md) 的 `/generate-lore-entry`）：

| 命令 | 作用 |
|---|---|
| `/list-name-categories [<filter>]` | 以 JSON 数组的形式列出类别；给出过滤词时，只列出名称中包含其所有词的类别（每个词须位于某个单词的开头，顺序不限，不区分大小写：`chad male` 能找到 *Africa - Central Africa - Chad - Male*，但找不到 *…Female*）。 |
| `/generate-names category count [min_length [max_length]]` | 生成该类别的最多 100 个名字，并以 JSON 数组返回。每次结果都不同。 |

两者都只读取数据，不会修改任何内容。

<a id="categories"></a>

## 类别

目前约有 1270 个类别。类别名称本身就说明了内容：

- **世界各地的名和姓**，来自 Kate Monk 的 Onomastikon，例如 `Europe Western - France - Surnames`、`Ancient World - Egypt - Male`、`India - Hindu Gods - Goddesses`。这类约有数百个。
- **75 个国家的姓氏**，来自[按国家划分的热门名字数据集](https://github.com/sigpwned/popular-names-by-country-dataset)（CC0），已转为罗马字母拼写，例如 `Armenia - Surnames`、`Japan - Surnames`、`Kosovo - Surnames`。这些列表都很小（从 6 个到约 150 个不等），因此链条较短，生成的新名字与真实名字很接近。如需真实名字本身，请使用 [Name provider](name-provider.md)。
- **来自其他集合的列表**：神祇、天使、恶魔、古代城市与国家、皇帝、行星（fantasygen），以及 Corpora（动物、塔罗牌、托尔金的名字、《权力的游戏》的家族、药物、医院、织物、宝石、编程语言等）和 markov-namegen-lib（宝可梦、龙、鸡尾酒、银行、罗马地名等）。

<a id="groups"></a>

### 组

约 170 个条目是类别的**组**。它们把某种语言中许多零散的小列表合并在一起，因此你可以直接请求 `German - Male`，而无需从 *Germany - Germanic - Male*、*Germany - Biblical - Male*、*Austria - First names - Male* 等中逐一挑选。组和普通类别一样列出，每个组在[工具窗口](tools.md)中都是一个条目。各组如下：

- **按语言划分**，每种语言在有相应列表的情况下分别提供 `- Male`、`- Female` 和 `- Surnames`：德语、法语、西班牙语（姓氏包括拉丁美洲）、意大利语、葡萄牙语、荷兰语、斯堪的纳维亚语、芬兰语族、波罗的语族、爱尔兰语、苏格兰语、威尔士语、凯尔特语（全部）、西斯拉夫语、东斯拉夫语和南斯拉夫语、希腊语、匈牙利语、罗马尼亚语、阿尔巴尼亚语、高加索语、突厥语、波斯语、阿拉伯语、犹太语、印地语（把四个按字母排列的男性名字列表合并为一组）、南亚语、东南亚语、马来-印尼语、波利尼西亚语，以及非洲各地区（西部、东部、南部、北部、中部）。`Chinese`、`Japanese`、`Korean` 和 `Vietnamese` 带有 `- Surnames`。
- **任意性别**：`<Language> - Forenames`（如 `German - Forenames`、`Spanish - Forenames`、`Celtic - Forenames`……）会合并该语言的男性和女性列表，以及同时适用于两种性别的名字列表，因此一次调用返回的男性和女性名字是混合的（比例随列表的大小而定，通常男性占一半到四分之三）。中文、日文、韩文和越南文同样有这类组。对于混合后会严重失衡的语言（突厥语、南亚语、西非和南部非洲语、马来-印尼语），只提供男性和女性两组。
- **古代**：盎格鲁-撒克逊、中古日耳曼、中古英语、古代近东。
- **美洲原住民**：`Native American - Male`、`- Female` 和 `- Surnames`（Onomastikon 中的北美类别：阿帕奇、苏族、易洛魁、切罗基、因纽特等），以及中美洲和南美洲的对应组（阿兹特克、玛雅、印加、亚马孙）。
- **其他**：`English - Surnames`（父名、职业、地名等）、`Saints`、`Deities`、`Fantasy Creatures`、`Game of Thrones Houses`。

对于每个名字，扩展先从组中随机选取一个类别，然后用该类别的链条（以及其通常的长度）生成名字。这样，一个名字听起来属于某一个民族和语言，而不是所有民族的混合，同时一次调用又能返回来自多个民族的名字。按出处或字母划分的国家列表，按其大小来选择（一个含 600 个名字的列表比含 10 个名字的被选中的机会更多）。在按整个语言或国家划分的组中（突厥语、高加索语、波罗的语、波利尼西亚语、非洲各地区、美洲原住民等），每个成员的被选中机会相同。某些组的来源较短，部分是英文译本（如 *Calf-Woman*），因此生成的名字比较粗糙。组在 `name-groups.json` 中占一行，写明成员及选取方式，参见[插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md#groups)。

每个类别都有一条最适合它的长度的链条（短列表使用短链条，这样能减少重复原始名字的情况），并附有其名字通常具有的长度。这些列表本身不属于 Marginalia，只有概率数据属于它，因此无法从扩展中还原出原始名字。部分来源有各自的许可证，参见[插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md)。

<a id="usage"></a>

## 用法

在 *Admin → Extensions* 中加载该扩展。它在下一次生成时生效。使用它的扩展会在加载时找到它，即使它是在这些扩展之后安装的，或者被卸载后重新安装，也同样有效，无需重启 Lore Generator。若未安装它以及其他套件，Lore Generator 上就不会出现 *Tools* 按钮。
