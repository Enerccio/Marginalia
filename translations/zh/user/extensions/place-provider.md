---
label: 地点提供者
order: 5
---

# 地点提供者

让模型查询地球上真实存在的地点，而不是凭空编造：城市、国家、海域和海湾、河流、湖泊、岛屿、山脉、沙漠、港口和机场，并附上坐标以及所在国家。它还能说明某地周边有什么，以及两个地点相距多远，这样以真实地图为背景的故事就能得到正确的距离和邻近关系。

该扩展没有自己的界面，也无需任何配置。[Lore Generator](lore-generator.md#tools) 和 [Arc Outline](arc-outline.md) 通过 **Tools** 按钮（[模型可用的工具](tools.md)）中的 *Place provider* 套件把它提供给模型，可开可关；它也可以作为 [智能体](../agents.md) 的命令和命令行中的命令使用。

<a id="what-it-knows"></a>

## 数据范围

数据来自 [Natural Earth](https://www.naturalearthdata.com/)（公有领域），这是一张包含**主要**地点的地图：约 18 000 个，其中 4 900 座城市、250 个国家、4 500 个省和州、1 100 条河流、500 个湖泊、400 座岛屿、350 座山峰和山脉、1 000 个港口和 900 个机场。它不是地名词典：小溪或村庄不在其中。Natural Earth 还把大多数海湾画成 bays，因此查询加拿大的 inlets 只会得到它收录的四个（Hamilton Inlet、Bathurst Inlet、Minto Inlet 和 Richard Collinson Inlet），而不是不列颠哥伦比亚海岸的数百个。

<a id="commands"></a>

## 命令

它们都只读取数据。

| 命令 | 作用 |
|---|---|
| `/list-place-types` | 列出地点的类型（`city`、`capital`、`bay`、`inlet`、`river`、`mountain range`、`island`……）以及每种类型的数量。 |
| `/search-places query [type [country [limit]]]` | 按名称、类型和国家搜索，最匹配的排在最前。查询词为 `*` 时匹配任意名称：`/search-places * inlets Canada` 会列出加拿大的所有 inlets。 |
| `/places-near place radius_km [type [limit]]` | 列出某个地点或某个点一定半径内的地点，最近的排在最前，并给出以千米计的距离和方向。 |
| `/distance-between from to` | 地球表面的直线距离（不是道路距离），以千米和英里计，并给出方向。 |

*地点*可以是名称（`Ottawa`；若有多个同名地点，用 `Paris, France` 这样加上逗号和国家或省份来指定），也可以是坐标（`45.42, -75.69`）。*类型*可以用复数形式，也可以用人们日常的说法：`inlets`、`mountains`、`towns`、`states`。*国家*可以是名称或代码：`Canada`、`Czechia`、`CA`、`CAN`。比较名称时不区分大小写和重音符号。最多返回 100 个地点（不限数量时为 25 个），回答中会说明总共有多少个符合条件。

按类型搜索时，还会找到名称中含有该词的、性质相近的水域地点：`inlet` 会找到 Hamilton Inlet，尽管 Natural Earth 把它归为 bay。

<a id="using-it-from-an-agent"></a>

## 在智能体中使用

获准使用这些命令的智能体（它们包含在 *All non-destructive* 选项和 *Auditor* 角色中）可以把它们当作函数调用。它们是为这样的提示词设计的：“把第一章设定在挪威一个位于峡湾边的渔村，离首都乘船两天”：智能体会搜索 `fjords of Norway`，挑选一个，然后查询它到奥斯陆的距离。

<a id="usage"></a>

## 用法

在 *Admin → Extensions* 中加载该扩展。在 Lore Generator 和 Arc Outline 的 **Tools** 中，勾选 Place provider 的 *Use this suite*；其提示会告诉模型查询真实地点，而不是凭猜测判断它在哪里或有多远。
