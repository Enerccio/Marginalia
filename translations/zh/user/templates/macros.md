---
label: 宏参考
order: 90
---

# 宏参考

Marginalia 支持 [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/) 的宏，因此为 SillyTavern 编写的提示词和世界信息无需修改即可使用。宏可以在每种[模板](templates.md)中使用：主模板、用户提示词、摘要提示词和 lorebook 条目。

{%{

<a id="syntax"></a>

## 语法

| 形式 | 示例 |
|---|---|
| 无参数 | `{{user}}` |
| 参数之间用`::`分隔 | `{{setvar::mood::nervous}}` |
| 第一个参数前加空格 | `{{getvar mood}}`、`{{setvar greeting::Hello}}` |
| 旧式单个`:` | `{{getvar:mood}}`、`{{random:north,south}}` |
| 嵌套宏 | `{{getvar::{{user}}_mood}}` |
| 作用域形式（内容作为最后一个参数） | `{{setvar backstory}}Long text...{{/setvar}}` |

- 宏名称不区分大小写：`{{User}}` 和 `{{USER}}` 均可使用。
- 名称和参数两侧的空格会被忽略：`{{ getvar :: mood }}`。
- 多余的`::`会并入最后一个参数：`{{setvar::x::a::b}}` 会把`x`设为`a::b`。
- 作用域内容先被求值，然后去除缩进并修剪。若在大括号后加`#`，即`{{#setvar name}}...{{/setvar}}`，则空白会被原样保留。

<a id="names-and-participants"></a>

## 名称与参与者

Marginalia 没有人设（persona）或角色卡；名称来自正在撰写的部分的轮次指令。

| 宏 | 结果 |
|---|---|
| `{{user}}` | 视角人物。 |
| `{{char}}` | 视角人物。别名`{{charIfNotGroup}}`。 |
| `{{group}}` | 在场角色；若未指定，则为视角人物。别名`{{groupNotMuted}}`。 |
| `{{notChar}}` | 除视角人物外的在场角色（以逗号或换行分隔，并用`, `连接）。 |
| `{{description}}` | 书籍的简介。 |
| `{{scenario}}` | 场景设定。 |
| `{{input}}` | 该部分的指令。 |

SillyTavern 的标记`<USER>`、`<BOT>`、`<CHAR>`、`<GROUP>`和`<CHARIFNOTGROUP>`（不区分大小写）同样可用。

假设视角人物为 *Alice*，在场角色为 *Alice, Bob, Carol*：
`{{user}}` → *Alice*，`{{group}}` → *Alice, Bob, Carol*，`{{notChar}}` → *Bob, Carol*。

<a id="story-history"></a>

## 故事历史

| 宏 | 结果 |
|---|---|
| `{{lastMessage}}` | 当前分支最后一部分的文本。别名`{{lastCharMessage}}`。 |
| `{{lastMessageId}}` | 最后一部分的位置，从 0 开始计数。 |
| `{{lastUserMessage}}` | 最后一部分的指令。 |
| `{{summary}}` | 当前分支最新的[摘要](../books/summaries.md)。 |
| `{{closest-message}}`、`{{closest-message::n}}` | 离正在撰写的部分最近的一部分：最后一部分，或从它往前数第 *n* 部分（`::2` 为倒数第二部分）。若不存在则为空。别名`{{closestMessage}}`。 |
| `{{closest-summary}}`、`{{closest-summary::n}}` | 分支摘要的对应内容：最新的一条，或从它往前数第 *n* 条。别名`{{closestSummary}}`。 |
| `{{current-message}}` | 被 *Regenerate* 或滑动切换替换的那一部分的文本；新部分则为空。别名`{{currentMessage}}`。 |

在书的第一部分，这些宏均为空。对于 *Regenerate*，“最后一部分”指被重新生成的那一部分之前的一部分。

`closest-message` 和 `closest-summary` 中的数字可以是一个宏，因此可以来自变量：`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` 表示从末尾数的第三部分。不是正整数的数字不会返回任何内容。`closest-summary` 统计分支中各部分的摘要，包括[元摘要](../books/summaries.md)及其所代表的摘要；`{{summary}}` 与 `{{closest-summary}}` 相同。

<a id="date-and-time"></a>

## 日期与时间

| 宏 | 示例结果 |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - 按 UTC 偏移量显示的时间（`UTC-5:30` 同样可用） |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js 格式标记](https://momentjs.com/docs/#/displaying/format/)，`[brackets]` 中的文本会原样输出：`{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - 距上一部分写成的时间，首部分则用`just now`。别名`{{idle_duration}}`。 |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - 两个时间之间的差值；`now`、日期（`2026-01-08`）和 ISO 时间均可使用。未知值不返回任何内容。 |

时间采用运行 Marginalia 的计算机或服务器所在的时区，并且始终使用英文。对于 [Docker 版本](../getting-started/docker-server.md)，请通过环境变量`TZ`设置时区，例如`TZ=Europe/Prague`。

<a id="variables"></a>

## 变量

变量用于在各部分之间保存数值：计数器、角色的情绪、故事已确立的事实等。变量分为两种：

- **局部**变量属于故事分支：每个部分都会记住它们的值，新部分从前一部分的值开始；
- **全局**变量属于整本书，由所有分支共享。

参见[哪些内容会在各部分之间保留](templates.md#what-is-kept-between-parts)。

| 局部 | 全局 | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | 值；未设置时为空。 |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | 设置值。不输出任何内容。 |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | 数字相加；若任一值不是数字，则追加文本。不输出任何内容。 |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | 加 1 并输出新值。非数字值从 0 开始计算。 |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | 减 1 并输出新值。 |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` 或 `false`。 |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | 删除该变量。别名`flushvar` / `flushglobalvar`。 |

数字输出时不带末尾的零：`1.50` 加 `1.5` 的结果为`3`。

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### 简写形式

`{{.name}}` 是局部变量，`{{$name}}` 是全局变量。单独使用时，简写形式输出其值。带运算符时：

| 简写 | 作用 | 输出 |
|---|---|---|
| `{{.hp = 20}}` | 设置值 | 无 |
| `{{.hp += 5}}`、`{{.hp -= 5}}` | 相加 / 相减（`+=` 向文本追加文本） | 无 |
| `{{.turn++}}`、`{{.turn--}}` | 加 1 / 减 1 | 新值 |
| `{{.hp == 17}}`、`{{.hp != 17}}` | 比较 | `true` / `false` |
| `{{.hp > 10}}`、`>=`、`<`、`<=` | 比较数字（比较文本用`false`） | `true` / `false` |

回退值与默认值：

- 当变量为空或为假值（`0`、`false`、`no`、`off`）时，`{{.name || Guest}}` 输出 *Guest*；
- 仅当变量完全未设置时，`{{.name ?? Guest}}` 输出 *Guest*；
- 若变量为空或为假值，`{{.title ||= Wanderer}}` 会将其设为 *Wanderer*，然后输出；
- 仅当变量完全未设置时，`{{.title ??= Wanderer}}` 才会设置它，然后输出。

值中可以包含宏：`{{.log += -> {{user}}}}`。

例如，在用户提示词顶部放置一个部分计数器（`+=` 不输出任何内容，`{{trim}}` 则会删除它留下的空行）：

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## 条件判断

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

条件可以是：

| 条件 | 为真的情况 |
|---|---|
| 宏名称 - `{{if user}}` | 该宏返回一个值 |
| 变量 - `{{if .mood}}`、`{{if $chapter}}` | 该变量有值 |
| 模板变量 - `{{if sceneSetting}}` | 该变量有值 |
| 比较 - `{{if .hp > 20}}` | 比较结果为真 |
| 任意文本 - `{{if yes}}` | 始终为真，假值除外 |
| 取反 - `{{if !user}}`、`{{if !.mood}}` | 与之相反 |

假值包括空值以及`0`、`false`、`no`、`off`（不区分大小写）。其他所有值均为真。

- 简写形式`{{if user::text}}`在条件为真时输出 *text*。
- 只对被选中的分支求值：另一分支中的`{{setvar}}`不会执行。
- `{{if}}` 块与作用域宏一样会去除缩进并修剪，因此可以为便于阅读而缩进。`{{#if}}`（即 [Handlebars if](templates.md#conditions)）会保留空白。
- 没有`{{/if}}`的`{{if}}`会被当作普通文本输出。
<a id="expressions"></a>

## 表达式

`{{eval::expression}}` 给出 `true` 或 `false`。在 `{{if}}` 无法完成的比较中使用它：比较的是模板中的值（不仅是变量），可用 `and`、`or` 和 `not` 连接：

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| 表达式中 | 含义 |
|---|---|
| `"text"`, `'text'` | 文本。**文本必须加引号**：不加引号的单词被视为名称。 |
| `12`, `-3.5`, `true`, `false` | 数字或真假值。 |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | 模板中的一个值，如 `{{povCharacter}}` 所示。不带参数的宏也可以使用（`user`、`summary`、`lastMessage`）。 |
| `.mood`, `$chapter` | 局部或全局[变量](#variables)；未设置时为空。 |
| `( )` | 分组。 |

比较运算：

| 运算符 | 为真的条件 |
|---|---|
| `==`, `!=` | 相等、不相等。数字按数值比较（`5 == 5.0`），其他内容严格比较，因此 `"Fiora" == "fiora"` 为假。 |
| `~=` | 相等，忽略大小写。 |
| `<`, `<=`, `>`, `>=` | 数字符合该顺序关系。若任一侧不是数字，则为假。 |
| `contains` | 左侧文本包含右侧文本，忽略大小写：`presentCharacters contains "ma"` 对 *Mara* 也为真。 |
| `has` | 左侧以逗号、分号或换行分隔的列表中包含右侧的项，忽略大小写以及各项周围的空格：`presentCharacters has "Mara"` 对 *Marati* 不为真。 |

连接：`and`（`&&`）、`or`（`||`）、`not`（`!`）；`not` 优先级最高，其次是 `and`，最后是 `or`。单独的一个值，只要不为空、`0`、`false`、`no` 或 `off`，即为真。宏可以用在文本中（`"{{user}}"`），并会先被展开。

无效的表达式会输出 `Error`：语法错误、不是名称的未加引号单词（`povCharacter == Fiora`）、未知名称。所有名称都会被检查，包括 `and` / `or` 中不会被执行到的部分，因此拼写错误不会被隐藏。在[设定集条件](../lorebooks/entries.md#a-condition-for-an-entry)中，`Error` 会使该条目不被使用，预览中也会显示这一点。

<a id="randomness"></a>

## 随机性

| 宏 | 结果 |
|---|---|
| `{{random::red::green::blue}}` | 其中一个选项，每次都不同。`{{random:red,green,blue}}` 和 `{{random}}red, green{{/random}}` 同样可用。 |
| `{{pick::sword::axe::bow}}` | 其中一个选项，但对于同一本书、模板中同一位置，结果**始终相同**。用于需要保持一致的内容，例如随机选定的名字。 |
| `{{roll::2d6+3}}` | 掷骰：`1d20`、`3d4-10`、`d100`；也可用 `{{roll d100}}`、`{{roll:6}}`（掷一枚六面骰）。无效的掷骰不产生任何结果。 |

<a id="generation-info"></a>

## 生成信息

| 宏 | 结果 |
|---|---|
| `{{model}}` | 本书所用[推理提供方](../inference-providers.md)的名称。 |
| `{{maxContextTokens}}` | 上下文上限（以 token 计，取决于协议或提供方，参见[限制](../protocols.md#limits)）。别名 `{{maxContext}}`。 |
| `{{maxResponseTokens}}` | 回复上限（以 token 计）。别名 `{{maxResponse}}`。 |
| `{{maxPrompt}}` | 上下文减去回复上限：即提示词可用的空间。 |
| `{{lastGenerationType}}` | 新章节为 `normal`；在摘要中则为 `regenerate`、`swipe` 或 `quiet`。 |

<a id="commands"></a>

## 命令

| 宏 | 结果 |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | [命令](../commands/index.md)的回答，以文本形式输出。只能使用为宏设计的命令（它只读取数据；扩展会对此作出说明，例如[角色追踪器](../extensions/character-tracker.md)的 `{{command::character-tracker-map}}`）；其他任何名称都会输出 `Error`。执行失败的命令不输出任何内容。 |

命令在生成过程执行到该宏时运行（若宏位于未被选中的 `{{if}}` 分支中，则根本不运行），且仅针对正在生成的那本书，并且只在生成的提示词中运行（用户提示词、设定集条目和主模板）；在其他地方，宏不输出任何内容。该回答会与新内容所接续的那个部分一同保存：从同一部分开始的下一次生成，以及同一次生成中的其他模板，都会直接使用它，而不会再次运行命令。**重新生成**和**切换版本**会重新请求该命令；每个新章节也会重新请求，因为它接续的是新的最后一个部分。

<a id="utility"></a>

## 实用工具

| 宏 | 结果 |
|---|---|
| `{{newline}}`, `{{newline::2}}` | 一个或多个换行。 |
| `{{space}}`, `{{space::4}}` | 一个或多个空格。 |
| `{{trim}}` | 删除其前后的换行。适用于只用来设置变量的行之后。 |
| `{{noop}}` | 无输出。 |
| `{{join::separator::a::b::c}}` | 将第一个文本之后的所有文本用第一个文本作为分隔符连接起来，数量不限（可包含宏）：`{{join::-::{{user}}::{{scenario}}}}`。空文本会被略去，因此可选的宏不会留下多余的分隔符。书写的参数两侧的空格会被去掉，所以若分隔符需要带空格，请使用宏：`{{join::{{space}}::a::b}}`、`{{join::{{newline}}::a::b}}`。 |
| `{{reverse::text}}` | 将文本反转。 |
| `{{// comment}}` | 注释，输出时删除。`{{//}}` ... `{{///}}` 将两者之间的所有内容（包括宏）注释掉。 |
| `\{\{user\}\}` | 原样输出 `{{user}}`。 |

<a id="sillytavern-only-macros"></a>

## 仅限 SillyTavern 的宏

仅在 SillyTavern 聊天中有意义的宏——用户角色（persona）和角色卡字段（`{{persona}}`、`{{personality}}`、`{{charPrompt}}`、`{{mesExamples}}` 等）、指令模式序列（`{{instructUserPrefix}}` 等）、作者注释、版本编号、`{{outlet}}` 及类似内容——会被识别，并且**不输出任何内容**，因此导入的世界信息不会把它们带入提示词。`{{isMobile}}` 和 `{{hasExtension}}` 输出 `false`。

Marginalia 不认识的宏：

- 带参数的（`{{fooBar::x}}`）会原样输出，与 SillyTavern 的做法相同；
- 不带参数的（`{{fooBar}}`）会输出为 **`Error`**，与未知的[模板变量](templates.md#typos)相同。
