---
label: テンプレートとマクロ
order: 940
icon: code
---

# テンプレートとマクロ

Marginaliaがモデルに送るものはすべて、**テンプレート**から組み立てられます。テンプレートとは、生成される部分ごとに値が埋め込まれるプレースホルダーを含んだテキストです。プロンプトの枠組みから個々のロアブックエントリーに至るまで、すべて変更できます。

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## テンプレートが使われる場所

| テンプレート | 設定場所 | 生成されるもの |
|---|---|---|
| **マスターテンプレート** | 本の [Prompts](../books/prompts.md)、*Settings → Templates* | システムメッセージ：役割、ロア、文体、要約 |
| **User Prompt** | 本の Prompts、*Settings → Templates* | 書かれる部分へのリクエスト |
| **Summary Prompt** | 本の Prompts、*Settings → Templates* | [要約](../books/summaries.md)へのリクエスト |
| **Meta Summary Prompt** | 本の Prompts、*Settings → Templates* | [メタ要約](../books/summaries.md#meta-summaries)へのリクエスト |
| **Lorebook entry content** | [ロアブックエントリー](../lorebooks/entries.md) | プロンプトに挿入されるロア |

*Point of View*、*Tense*、*Style* はテンプレートではなく、ただのプレーンテキストです。

<a id="two-kinds-of-placeholders"></a>

## 2種類のプレースホルダー

テンプレートでは2つの構文を組み合わせられます。

- **Handlebars** - {%{`{{instructions}}`}%} のような変数や、値が空でない場合にのみ出力される {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%} のようなセクション。詳しくは [プロンプトテンプレート](templates.md) を参照してください。
- **SillyTavernマクロ** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} など、SillyTavernのユーザーが知っているマクロ一式。SillyTavernのロアブックやプロンプトをそのまま使い続けられます。詳しくは [マクロリファレンス](macros.md) を参照してください。

どちらの構文もすべてのテンプレートで使え、自由に混在させられます。

<a id="help-in-the-editor"></a>

## エディターでのヘルプ

テンプレートの各フィールドには、ヒントのポップオーバーがあります。

- **既定のテンプレートを挿入** では、既定のテンプレートをフィールドにコピーします。
- **利用可能なテンプレート変数** では、そのテンプレートの変数を、どこでも使える変数も含めて一覧表示します。
- **利用可能なマクロ** では、すべてのマクロを短い説明付きで一覧表示します。

![テンプレートフィールドのヒントポップオーバー](../../images/template-hints.png)

構文エラーのあるテンプレート（たとえば閉じられていないセクション）は、本のプロンプトや設定では保存できません。ロアブックエントリーでは保存されますが、無効としてマークされ、修正されるまではプレーンテキストとして使われます。

未知の変数（多くは入力ミス）があっても保存はブロックされません。その場合、フィールドの下に警告として一覧表示されます。詳しくは [入力ミス](templates.md#typos) を参照してください。

生成された部分のメニューにある **プロンプトを表示** を使うと、送信されたとおりのプロンプトが表示されます。テンプレートがどのような結果を生んだかを確認する最良の方法です。

<a id="in-this-section"></a>

## このセクションの内容

- [プロンプトテンプレート](templates.md) - 各テンプレートの変数、セクション、条件、評価順序
- [マクロリファレンス](macros.md) - すべてのマクロ、変数、条件分岐、SillyTavernとの互換性
