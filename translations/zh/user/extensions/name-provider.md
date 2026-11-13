---
label: 名称提供者
order: 6
---

# 名称提供者

为模型提供真实的姓名供其选择，而不是让它自行编造：收录了许多国家最常见的名和姓。[名称生成器](name-generator.md)会编造只听起来像某一类别的新名字，而本扩展提供的名字确实存在，每个名字都附有原始拼写和罗马化拼写，例如 `Satō` 和 `佐藤`。

本扩展没有自己的界面，也无需任何配置。其他扩展会向模型提供它的功能，例如 [Lore Generator](lore-generator.md#tools) 和 [Arc Outline](arc-outline.md)，你可以在那里选择模型可以使用的名字列表。它的套件名为 *Name provider*，位于[工具窗口](tools.md)中，与 *Name generator* 套件并列。你可以检查两者的列表，只检查其中之一，或两者都不检查。

<a id="what-the-model-can-do"></a>

## 模型能做什么

| 功能 | 作用 |
|---|---|
| `list_real_name_lists` | 列出名字列表；若给出 `filter`，则只列出名称中包含其中词语的列表。 |
| `pick_real_names` | 从某个列表中随机挑选最多 100 个不重复的名字。每个名字都是一对拼写：罗马化名字和原始名字。 |

若列表中的名字少于所要求的数量，则返回全部名字。模型会被告知（参见[工具窗口](tools.md#choosing-the-tools)中 *Name provider* 套件的提示）从所获得的名字中选取，并按返回时的样子书写；在合适于设定时，可以在旁边附上原始拼写；它也可以把同一国家的名与姓组合起来。

<a id="commands"></a>

## 命令

同样的名字也可以通过[命令](../commands/index.md)使用，适用于命令行以及[智能体](../agents.md)。智能体可以在自己的提示词中使用它们（例如 [Lore Generator](lore-generator.md) 的 `/generate-lore-entry`）：

| 命令 | 作用 |
|---|---|
| `/list-real-name-lists [<filter>]` | 以 JSON 数组形式列出各个列表；若给出过滤词，则只列出名称包含全部过滤词的列表（每个词须位于单词开头，顺序不限，不区分大小写）。 |
| `/pick-real-names list count` | 从指定列表中随机挑选最多 100 个名字，并以 JSON 数组返回，每项为一对拼写：罗马化名字和原始名字。 |

两者都只读取数据，不作修改。

<a id="lists"></a>

## 列表

共有约 180 个列表，名称格式为 `<Country> - Surnames` 和 `<Country> - Forenames`：75 个国家的姓氏列表，以及 106 个国家的名字列表（例如 `Japan - Surnames`、`Czechia - Forenames`、`Kosovo - Surnames`）。男性和女性名字都放在同一个名字列表中。每个列表包含 5 到约 150 个名字，均为该国最常见的名字。这些名字并非凭空生成，你得到的每个名字都是真实存在的人所用的名字。

另有几十个条目是按语言划分的**分组**，即 `<Language> - Forenames` 和 `<Language> - Surnames`，涵盖西班牙语（18 个国家）、葡萄牙语、法语、德语、英语、阿拉伯语、西斯拉夫语、东斯拉夫语、南斯拉夫语、北日耳曼语、北欧语、乌拉尔语、波罗的语、突厥语、波斯语、中文、南亚语、东南亚语、高加索语、阿尔巴尼亚语和罗马尼亚语（若存在此类列表）。它们与其他列表一样被列出，在[工具窗口](tools.md)中也是一个条目。分组中的每个名字都从其所含列表中随机选出一个列表来获取，每个国家的概率相同，因此请求 20 个名字时，得到的会是多个国家的名字，且不会出现同一个名字两次。同时使用多种语言的国家（比利时、卢森堡、马耳他、安道尔）不属于任何分组。分组定义是 `name-groups.json` 中的一行，参见[插件说明（README）](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups)。

数据来自[按国家的热门名字数据集](https://github.com/sigpwned/popular-names-by-country-dataset)（CC0 协议），取自维基百科中关于最常见姓氏和最受欢迎名字的列表。以非拉丁字母书写的名字（中文、日文、韩文、阿拉伯文、西里尔字母等）都有罗马化拼写，这些拼写要么来自源数据，要么由机器翻译生成，因此在拼写重要时请自行核对。

<a id="usage"></a>

## 用法

在 *Admin → Extensions* 中加载该扩展。它从下一次生成开始生效，即使它是在使用它的扩展之后安装，或是卸载后重新安装，同样如此。若没有它和其他套件，就不会出现 *Tools* 按钮。
