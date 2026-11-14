---
label: 提示模板
order: 100
---

# 提示模板

模板使用 [Handlebars](https://handlebarsjs.com/guide/) 语法，并扩展了 SillyTavern 的[宏](macros.md)。本页介绍每个模板可获得的变量，以及语法中的 Handlebars 部分。

{%{

<a id="variables"></a>

## 变量

用双花括号括起来的变量会被替换为它的值：`{{instructions}}` 会变成你为该部分写下的指令。值为空时不输出任何内容。

<a id="variables-of-each-template"></a>

### 各模板的变量

| 模板 | 变量 |
|---|---|
| **Master Template** | `{{backgroundLore}}` 已激活的 lorebook 条目（*In Lore Block*），`{{narrativePov}}`，`{{narrativeTense}}`，`{{style}}`，`{{summaries}}` 该分支的所有摘要，`{{annotations}}` 你标记为要交给模型的[备注](../books/annotations.md#giving-a-note-to-the-model)，用于提示中的相应部分 |
| **User Prompt** | `{{sceneSetting}}`，`{{povCharacter}}`，`{{presentCharacters}}`，`{{instructions}}`：本轮指令 |
| **Summary Prompt** | `{{text}}` 要摘要的部分，`{{backgroundLore}}` 被摘要部分写作时生效的 lorebook |
| **Meta Summary Prompt** | `{{summaryBlocks}}` 要合并的摘要，最早的在前，`{{backgroundLore}}` 该元摘要部分写作时生效的 lorebook |
| **Lorebook entry** | `{{sceneSetting}}`，`{{povCharacter}}`，`{{presentCharacters}}`，`{{instructions}}`，`{{narrativePov}}`，`{{narrativeTense}}`，`{{style}}` |

<a id="variables-available-everywhere"></a>

### 所有模板都可用的变量

除了各自的变量，每个模板还可以使用：

| 变量 | |
|---|---|
| `{{povCharacter}}`，`{{sceneSetting}}`，`{{presentCharacters}}`，`{{instructions}}` | 正在写作部分的本轮指令（对于摘要：被摘要部分的指令）。 |
| `{{narrativePov}}`，`{{narrativeTense}}`，`{{style}}` | 书籍的叙事设置。 |
| `{{manuscriptName}}`，`{{manuscriptDescription}}` | 书籍的名称和描述。 |

每个模板字段的提示弹窗（ⓘ）也会列出这些变量。

<a id="typos"></a>

### 拼写错误

不存在的变量会被输出为单词 **`Error`**：`{{instruction}}`（缺少 *s*）会把 *Error* 写进提示。模板字段（书籍的 *Prompts*、*Settings*，以及 lorebook 条目）会在字段下方以 *Unknown names (sent to the model as "Error")* 的形式列出此类名称。这只是警告，模板仍会保存，请修正其中列出的名称。变量和区段在所有地方都会检查，包括未输出的 `{{#if}}` 分支内部。仅用作条件的名称不会被报告，因为它们不会输出 *Error*：在 `{{#if name}}` 中，未知名称为假；在 SillyTavern 的 `{{if name}}` 中，它被当作普通文本，因此为真。

<a id="sections"></a>

## 区段

只有当变量有值时，区段才会输出其内容。在区段内，`{{.}}` 即为该值：

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

当 *Scene Setting* 为空时，整行都会消失。默认模板到处都这样使用，因此提示中不会出现空的标题。

**反向区段**只有在变量为空时才输出其内容：

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## 条件

带可选 `{{else}}` 的 `{{#if}}`：

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` 与 `{{#if x}}` 相反。SillyTavern 的写法 `{{if x}}...{{/if}}` 同样可用，并且选项更多（取反、变量、比较），参见[条件](macros.md#conditionals)。

不带参数的宏可以像变量一样用于区段和条件中：

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## 注释与转义

| 语法 | 结果 |
|---|---|
| `{{!-- a comment --}}`，`{{! short comment }}` | 从输出中删除。可用来在模板中添加备注。 |
| `\{{instructions}}` | 原样输出 `{{instructions}}`，而不是它的值。 |
| `{{{instructions}}}` | 与 `{{instructions}}` 相同。Marginalia 不对任何内容做 HTML 转义，因此 `<`、`&` 和引号会按原样输出。 |

SillyTavern 的注释 `{{// ...}}` 和转义 `\{\{...\}\}` 同样可用，参见[实用工具](macros.md#utility)。

<a id="order-of-evaluation"></a>

## 求值顺序

对于每个生成的部分，模板按以下顺序处理：

1. **User Prompt**；
2. 已激活的 **lorebook 条目**，按其顺序；
3. **Master Template**。

所有模板共享同一组[变量](macros.md#variables)：在 **user prompt** 中用 `{{setvar}}` 设置的值，可以在 lorebook 条目和 master template 中读取；在某个 lorebook 条目中设置的值，对之后的条目和 master template 可见，但对已经处理过的 user prompt 不可见。

lorebook 过滤器看到的是处理后的 user prompt，因此 user prompt 中的宏可以生成激活条目的文本。

<a id="what-is-kept-between-parts"></a>

## 各部分之间保留的内容

| | |
|---|---|
| **Local variables**（`{{setvar}}`，`{{.name}}`） | 随生成的部分一同保存。下一部分从当前激活分支最后一部分的值开始，因此每个分支都有自己的值。*Regenerate* 会从前一部分的值重新开始。 |
| **Global variables**（`{{setglobalvar}}`，`{{$name}}`） | 随书籍一同保存，所有分支共享。*Regenerate* 或删除某部分都不会撤销它们。 |
| **Summaries** | 可以读取变量，但其中的修改会被丢弃。 |

对于同一本书，`{{pick}}` 每次给出相同的结果，`{{random}}` 则每次给出新的结果。

<a id="writing-your-own-templates"></a>

## 编写自己的模板

从默认模板开始：打开提示弹窗并点击 **Insert Default Template**，然后再修改。需要保留的内容：

- master template 中的 `{{#backgroundLore}}` 块。没有它，lorebook 就不会被使用（只有带 *Before User Prompt* 的条目仍会送达模型）；
- master template 中的 `{{#summaries}}` 块。没有它，摘要就会丢失；
- user prompt 中的 `{{instructions}}`。没有它，模型就看不到应该发生什么；
- Summary Prompt 中的 `{{text}}`。没有它，就没有可摘要的内容。

}%}
