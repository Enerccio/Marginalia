---
label: 正则表达式
order: 935
icon: search
---

# 正则表达式

正则表达式（*regex*）是一种简短的模式，用来描述文本，而不是把文本逐字写出来：比如"单词 Harry，但不是 Harrys"、"任意两个带引号的词"、"连续三个或更多空格"。Marginalia 在两处使用正则表达式：

- [故事](books/story-editor.md#search-and-replace)和[设定集](lorebooks/entries.md#search-and-replace)的**搜索与替换**，以及对应的[命令](commands/index.md)，前提是开启了**使用正则搜索**。模式用来查找文本，替换内容可以复用其中的片段。
- [设定条目](lorebooks/entries.md#activation)的**正则**过滤器，它决定条目何时被放入提示词。

本页使用的是 Java 的方言。这里介绍你最常用的部分；完整参考放在页面末尾的链接中。

!!!
普通的词不需要正则表达式。关闭**使用正则搜索**时，文本会按照你输入的原样搜索，其中的 `.` 或 `(` 只是普通的点号或括号。只有当单一的固定文本不够用时，才需要开启正则。
!!!

<a id="the-building-blocks"></a>

## 基本构件

| 写法 | 匹配 | 示例 |
|---|---|---|
| 一个字母或数字 | 该字符本身 | `cat` 在 *concatenate* 中找到 *cat* |
| `.` | 任意一个字符（换行符除外） | `c.t` 找到 *cat*、*cot*、*c7t* |
| `\d` `\w` `\s` | 一个数字、一个字母/数字/`_`、一个空格或换行 | `\d\d` 找到 *42* |
| `[abc]` `[a-z]` | 列出的字符之一，或某个范围内的字符 | `gr[ae]y` 找到 *grey* 和 *gray* |
| `[^abc]` | 一个**不在**列表中的字符 | `[^"]` 是除引号以外的任意字符 |
| `\b` | 单词的边界 | `\bcat\b` 找到 *cat*，但找不到 *concatenate* |
| `^` `$` | 一行的开头和结尾 | `^Chapter` 找到位于行首的 *Chapter* |
| `x?` `x*` `x+` | `x` 出现零次或一次、任意次、至少一次 | `colou?r` 找到 *color* 和 *colour* |
| `x{2,4}` | `x` 出现两到四次 | `\.{3}` 是连续三个点 |
| `\` | 紧随其后的字符按字面含义处理 | `\.` 是点号，`\?` 是问号，`\(` 是括号 |

括号可以构成一个**分组**，分组内的竖线表示"或"：`(Mr|Mrs) Dursley` 找到 *Mr Dursley* 和 *Mrs Dursley*，`\b(Voldemort|You-Know-Who)\b` 找到这两个名字中的任意一个。分组后面的 `?`、`*` 或 `+` 作用于整个分组：`(very )+good` 找到 *very good* 和 *very very good*。

字符 `. ? * + ^ $ | \ ( ) [ ] { }` 有其特殊含义。若要把其中某个字符作为普通字符来搜索，请在它前面加上 `\`：要找到 *Really?!*，就写 `Really\?!`。

默认情况下，正则不忽略大小写：*harry* 找不到 *Harry*。在开头加上 `(?i)` 即可忽略大小写：`(?i)harry` 找到 *Harry*、*HARRY* 和 *harry*。（搜索与替换窗口在正则开启时会锁定**区分大小写**，因此在那里要通过这种方式关闭它；设定条目的**正则**过滤器始终忽略大小写。）

不可能匹配出任何长度的匹配（例如 `a*` 这类可以什么都不匹配的模式）会被搜索与替换忽略。

<a id="typical-uses-in-a-story"></a>

## 故事中的典型用法

下表每一行都是你可能想对某本书做的操作。**搜索**是你在*查找内容*中输入的内容，**替换**是你在*替换为*中输入的内容。

| 你想要的效果 | 搜索 | 替换 |
|---|---|---|
| 改名某个角色，但不改动更长单词的一部分（改 *Harry*，不改 *Harrys* 或 *Harrison*） | `\bHarry\b` | `Henry` |
| 改名，不论大小写 | `(?i)\bharry\b` | `Henry` |
| 一次性改掉名字及其所有格 | `\bHarry('s)?\b` | `Henry$1` |
| 把直引号换成弯引号 | `"([^"]*)"` | `“$1”` |
| 把三个点换成一个省略号字符 | `\.{3}` | `…` |
| 把两个或更多空格合并成一个 | ` {2,}` | ` `（一个空格） |
| 删除行尾的空格 | ` +$` | （留空） |
| 把两个连字符换成长破折号 | ` ?-- ?` | ` — ` |
| 把 *姓, 名* 调换为 *名 姓* | `(\w+), (\w+)` | `$2 $1` |
| 查找某个词在哪里出现，但不做任何修改 | 该模式 | （留空，只按**搜索**） |

若要把几种写法统一为同一个名字，搜索 `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b`，并替换为 `Tom Riddle`。

<a id="taking-the-match-into-the-replacement"></a>

### 把匹配内容带入替换

正则中圆括号内的所有内容都是一个**分组**，按左括号的位置从左往右编号：替换内容中的 `$1` 是第一个分组的文本，`$2` 是第二个分组的文本，`$0` 是整个匹配。在 `(\w+), (\w+)` 中，第一个分组是逗号前的词，第二个分组是逗号后的词，因此 `$2 $1` 会将它们互换：*Potter, Harry* 变成 *Harry Potter*。

你可以为分组命名，如 `(?<last>\w+), (?<first>\w+)`，并在替换内容中使用该名称：`${first} ${last}`。

如果替换内容中的 `$` 或 `\` 需要保持其字面含义，就必须对其转义：若要在文本中写入 *$5*，请写 `\$5`。不使用正则时，替换内容始终按你输入的原样处理。

如果模式有误（例如有一个括号未闭合），或者替换内容引用了模式中不存在的分组，会弹出提示说明原因，且什么也不会发生；搜索本身永远不会修改任何内容。

<a id="match-only-in-a-context"></a>

### 只在特定上下文中匹配

*前瞻*（lookahead）`(?=...)` 和*后顾*（lookbehind）`(?<=...)` 会查看匹配项周围的文本，但不会将其纳入匹配，因此只有它们之外的部分会被替换：

| 搜索 | 查找结果 |
|---|---|
| `Harry(?= Potter)` | 仅当后面紧跟 *Potter* 时的 *Harry*（且保留 *Potter* 不变） |
| `(?<=Professor )McGonagall` | 仅位于 *Professor* 之后的 *McGonagall* |
| `Harry(?! Potter)` | 后面不跟 *Potter* 的 *Harry* |

<a id="lazy-and-greedy"></a>

### 惰性与贪婪

`*` 和 `+` 会尽可能多地匹配：在 `He said "yes" and "no"` 中，模式 `".*"` 会匹配整个 `"yes" and "no"`。加上 `?`（`".*?"`），或者明确排除不允许的字符（`"[^"]*"`），即可在第一个闭合引号处停止。

搜索会逐行处理：`.` 不会跨越换行符，`^` 和 `$` 会匹配每一行的开头和结尾，而不只是整段文本的开头和结尾。若要匹配换行符，请写 `\n`。

<a id="in-a-lorebook-entry-filter"></a>

## 在设定条目过滤器中

设置为**正则**的设定条目过滤器会与该部分请求的内容进行匹配，匹配范围是其中的任意位置。它忽略大小写，并且在这里 `.` 确实可以匹配换行符，因此 `varro.*harbor` 即使 Varro 与港口位于不同的行，也能找到它们。常见的过滤器写法：

- `\bvarro\b` - 作为完整单词的 Varro。
- `\b(varro|captain)\b` - Varro 或 captain。
- `varro.*(harbor|docks)` - Varro，之后某处出现 harbor 或 docks。
- `dragons?` - *dragon* 或 *dragons*。
- `(?<!no )magic` - *magic*，但紧跟在 *no* 之后的除外。

无效的正则永远不会激活条目，原因会写入日志。关于条目如何被选中的其余规则，请参阅[条目与激活](lorebooks/entries.md#activation)。

<a id="in-the-commands"></a>

## 在命令中

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) 和 `/search-replace-lore-entries` 将正则作为第三个参数（`true`），放在搜索内容和替换内容之后。命令行以空格分隔参数，因此包含空格的模式或替换内容需要用双引号括起来。反斜杠后接字母时会原样传递（`\b`、`\d`），但 `\\` 会变成单个反斜杠，`\"` 会变成引号，因此模式中的字面反斜杠应写作 `\\\\`。

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

这些命令不会列出匹配项，而是询问你是否替换找到的全部匹配项。如果你对某个模式没有把握，请先在**搜索与替换**窗口中试运行，该窗口会逐一显示每个匹配项的原文和替换后的结果。

<a id="tips"></a>

## 小贴士

- 先用**搜索**试运行模式，并打开其中几行查看：窗口会显示每个匹配项及其上下文，包括原文和替换后的样子。确认结果符合预期后再替换。
- 从简单的模式开始，逐步添加内容；如果模式匹配的内容太多，通常需要在单词两侧加上 `\b`，或用 `?` 来约束 `*`。
- 在进行大规模替换前先做一次[备份](books/backups.md)非常简单（`/take-backup`），而且替换一旦开始就无法中途停止。
- 摘要变化：如果替换涉及某个[摘要](books/summaries.md)所依据的文本，窗口会提示该摘要将会过时；参见[搜索与替换](books/story-editor.md#search-and-replace)。

<a id="further-reading"></a>

## 延伸阅读

本页只涵盖基础内容。Java 的完整语法，包括所有结构、标志和字符类，请参阅
[`java.util.regex.Pattern` 文档](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html)；替换语法（`$1`、`${name}`、`\$`）则见
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String))。
