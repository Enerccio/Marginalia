---
label: プロンプトテンプレート
order: 100
---

# プロンプトテンプレート

テンプレートは [Handlebars](https://handlebarsjs.com/guide/) の構文を使い、SillyTavern の
[マクロ](macros.md)で拡張されています。このページでは、各テンプレートが受け取る変数と、構文のうち Handlebars の部分を扱います。

{%{

<a id="variables"></a>

## 変数

二重の波括弧で囲んだ変数は、その値に置き換えられます。`{{instructions}}` は、そのパートについて書いた指示に置き換わります。値が空の場合は何も表示されません。

<a id="variables-of-each-template"></a>

### 各テンプレートの変数

| テンプレート | 変数 |
|---|---|
| **マスターテンプレート** | `{{backgroundLore}}` 有効になったロアブックのエントリ（*ロアブロック内*）、`{{narrativePov}}`、`{{narrativeTense}}`、`{{style}}`、`{{summaries}}` ブランチのすべての要約、`{{annotations}}` モデルに渡すようにマークした[メモ](../books/annotations.md#giving-a-note-to-the-model)（プロンプトに含まれるパート用） |
| **User Prompt** | `{{sceneSetting}}`、`{{povCharacter}}`、`{{presentCharacters}}`、`{{instructions}}` - ターンの指示 |
| **Summary Prompt** | `{{text}}` 要約するパート、`{{backgroundLore}}` 要約対象のパートを書いたときに有効だったロア |
| **Meta Summary Prompt** | `{{summaryBlocks}}` 統合する要約（古いものから順）、`{{backgroundLore}}` メタ要約のそのパートを書いたときに有効だったロア |
| **Lorebook entry** | `{{sceneSetting}}`、`{{povCharacter}}`、`{{presentCharacters}}`、`{{instructions}}`、`{{narrativePov}}`、`{{narrativeTense}}`、`{{style}}` |

<a id="variables-available-everywhere"></a>

### どこでも使える変数

各テンプレートは、独自の変数に加えて次の変数も使えます。

| 変数 | |
|---|---|
| `{{povCharacter}}`、`{{sceneSetting}}`、`{{presentCharacters}}`、`{{instructions}}` | 書いている途中のパートのターン指示（要約の場合は、要約対象のパートのもの）。 |
| `{{narrativePov}}`、`{{narrativeTense}}`、`{{style}}` | 本の語りの設定。 |
| `{{manuscriptName}}`、`{{manuscriptDescription}}` | 本の名前と説明。 |

どのテンプレートのフィールドのヒントのポップオーバー（ⓘ）にも、これらが一覧で表示されます。

<a id="typos"></a>

### タイプミス

存在しない変数は、単語 **`Error`** として表示されます。`{{instruction}}`（*s* が抜けている）は、プロンプトに *Error* を入れてしまいます。テンプレートの各フィールド（本の *プロンプト*、*設定*、ロアブックのエントリ）は、そのような名前をフィールドの下に *Unknown names (sent to the model as "Error")* として一覧表示します。これは警告にすぎず、テンプレートは保存されるので、一覧に出た名前を修正してください。変数とセクションはどこでも検査され、表示されない `{{#if}}` の分岐の中も対象です。条件としてだけ使われている名前は報告されません。*Error* を表示しないからです。`{{#if name}}` では、未知の名前は偽になります。SillyTavern の `{{if name}}` では、ただのテキストとして扱われ、真になります。

<a id="sections"></a>

## セクション

セクションは、変数に値があるときだけ中身を表示します。中では `{{.}}` が値を表します。

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

*場面設定* が空のときは、行全体が消えます。既定のテンプレートはどこでもこの仕組みを使っているので、プロンプトに中身の空の見出しは現れません。

**反転セクション**は、変数が空のときだけ中身を表示します。

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## 条件

省略可能な `{{else}}` を伴う `{{#if}}`：

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` は `{{#if x}}` の逆です。SillyTavern の書式 `{{if x}}...{{/if}}` も使えて、否定、変数、比較などの選択肢が増えます。詳しくは [条件分岐](macros.md#conditionals) を参照してください。

引数のないマクロは、セクションや条件の中で変数と同じように使えます。

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## コメントとエスケープ

| 構文 | 結果 |
|---|---|
| `{{!-- a comment --}}`、`{{! short comment }}` | 出力から取り除かれます。テンプレート内のメモに使ってください。 |
| `\{{instructions}}` | 値の代わりに `{{instructions}}` をそのまま表示します。 |
| `{{{instructions}}}` | `{{instructions}}` と同じです。Marginalia では HTML エスケープは一切行われないため、`<`、`&`、引用符はそのまま表示されます。 |

SillyTavern のコメント `{{// ...}}` とエスケープ `\{\{...\}\}` も使えます。詳しくは [ユーティリティ](macros.md#utility) を参照してください。

<a id="order-of-evaluation"></a>

## 評価の順序

生成されたパート1つについて、テンプレートは次の順序で処理されます。

1. **ユーザープロンプト**;
2. 有効になった**ロアブックのエントリ**を、その順序で;
3. **マスターテンプレート**。

これらはすべて、一組の[変数](macros.md#variables)を共有します。ユーザープロンプトで `{{setvar}}` に設定した値は、ロアブックのエントリとマスターテンプレートから読めます。ロアブックのエントリで設定した値は、後のエントリとマスターテンプレートからは見えますが、すでに処理済みのユーザープロンプトからは見えません。

ロアブックのフィルターは、処理済みのユーザープロンプトを参照します。そのため、ユーザープロンプト内のマクロが、エントリを有効にするテキストを生成することがあります。

<a id="what-is-kept-between-parts"></a>

## パートの間で保持されるもの

| | |
|---|---|
| **ローカル変数**（`{{setvar}}`、`{{.name}}`） | 生成されたパートと一緒に保存されます。次のパートは、有効なブランチの最後のパートの値から始まるので、ブランチごとに独自の値を持ちます。*再生成* は、その前のパートの値から改めて始まります。 |
| **グローバル変数**（`{{setglobalvar}}`、`{{$name}}`） | 本と一緒に保存され、すべてのブランチで共有されます。*再生成* やパートの削除では元に戻りません。 |
| **要約** | 変数を読めますが、要約による変更は破棄されます。 |

`{{pick}}` は同じ本では毎回同じ結果を返し、`{{random}}` は毎回新しい結果を返します。

<a id="writing-your-own-templates"></a>

## 独自のテンプレートを書く

まず既定のテンプレートから始めてください。ヒントのポップオーバーを開き、**既定のテンプレートを挿入**をクリックしてから、内容を変更します。残しておくべきものは次のとおりです。

- マスターテンプレート内の `{{#backgroundLore}}` ブロック。これがないと、ロアブックは使われません（*ユーザープロンプトの前* を指定したエントリだけがモデルに届きます）；
- マスターテンプレート内の `{{#summaries}}` ブロック。これがないと、要約は失われます；
- ユーザープロンプト内の `{{instructions}}`。これがないと、モデルは何が起こるべきかを見られません；
- 要約プロンプト内の `{{text}}`。これがないと、要約する内容がなくなります。

}%}
