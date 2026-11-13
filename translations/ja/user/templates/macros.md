---
label: マクロリファレンス
order: 90
---

# マクロリファレンス

Marginalia は [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/) のマクロを理解するため、
SillyTavern 向けに書かれたプロンプトやワールド情報は変更なしで動作します。マクロは、すべての
[テンプレート](templates.md) で機能します。マスターテンプレート、ユーザープロンプト、要約プロンプト、ロアブックのエントリーがそれにあたります。

{%{

<a id="syntax"></a>

## 構文

| 形式 | 例 |
|---|---|
| 引数なし | `{{user}}` |
| `::` で区切った引数 | `{{setvar::mood::nervous}}` |
| スペースの後の最初の引数 | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| 旧形式の単一 `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| マクロの入れ子 | `{{getvar::{{user}}_mood}}` |
| スコープ付き（最後の引数として内容を置く） | `{{setvar backstory}}Long text...{{/setvar}}` |

- マクロ名は大文字と小文字を区別しません。`{{User}}` と `{{USER}}` はどちらも動作します。
- 名前や引数の前後のスペースは無視されます。例: `{{ getvar :: mood }}`。
- 余分な `::` は最後の引数に含まれます。`{{setvar::x::a::b}}` は `x` に `a::b` を設定します。
- スコープ付きの内容は先に評価され、その後インデントが除去されて前後の空白が取り除かれます。波かっこの後に `#` を付けると、`{{#setvar name}}...{{/setvar}}` のように、空白がそのまま保持されます。

<a id="names-and-participants"></a>

## 名前と参加者

Marginalia には、ペルソナやキャラクターカードはありません。名前は、書いている部分のターン指示から取得されます。

| マクロ | 結果 |
|---|---|
| `{{user}}` | 視点キャラクター。 |
| `{{char}}` | 視点キャラクター。別名 `{{charIfNotGroup}}`。 |
| `{{group}}` | 登場するキャラクター。指定がない場合は視点キャラクター。別名 `{{groupNotMuted}}`。 |
| `{{notChar}}` | 登場するキャラクターから視点キャラクターを除いたもの（カンマまたは改行区切り、`, ` で連結）。 |
| `{{description}}` | 本の説明。 |
| `{{scenario}}` | シーンの設定。 |
| `{{input}}` | そのパートへの指示。 |

SillyTavern のマーカー `<USER>`、`<BOT>`、`<CHAR>`、`<GROUP>`、`<CHARIFNOTGROUP>`（大文字小文字は問わない）も動作します。

視点キャラクターが *Alice*、登場キャラクターが *Alice, Bob, Carol* の場合:
`{{user}}` → *Alice*、`{{group}}` → *Alice, Bob, Carol*、`{{notChar}}` → *Bob, Carol*。

<a id="story-history"></a>

## 物語の履歴

| マクロ | 結果 |
|---|---|
| `{{lastMessage}}` | 有効な分岐の最後のパートの本文。別名 `{{lastCharMessage}}`。 |
| `{{lastMessageId}}` | 最後のパートの位置（0 から数える）。 |
| `{{lastUserMessage}}` | 最後のパートの指示。 |
| `{{summary}}` | 有効な分岐の最新の [要約](../books/summaries.md)。 |
| `{{closest-message}}`, `{{closest-message::n}}` | 書いているパートに最も近いパート: 最後のもの、またはそこから逆に数えて *n* 番目（`::2` は最後の一つ前）。該当するパートがない場合は空。別名 `{{closestMessage}}`。 |
| `{{closest-summary}}`, `{{closest-summary::n}}` | 分岐の要約について同じ。最新のもの、またはそこから逆に数えて *n* 番目。別名 `{{closestSummary}}`。 |
| `{{current-message}}` | *再生成* またはスワイプによって置き換えられるパートの本文。新しいパートでは空。別名 `{{currentMessage}}`。 |

本の最初のパートでは、これらはすべて空です。*再生成* の場合、「最後のパート」は再生成されるパートの一つ前のパートを指します。

`closest-message` と `closest-summary` の数値はマクロにすることもできるため、変数から取得できます: `{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` は末尾から3番目のパートです。正の整数でない数値は何も返しません。`closest-summary` は、分岐のパートの要約を数えます。[メタ要約](../books/summaries.md) と、それが代わりになっている要約も含まれます。`{{summary}}` は `{{closest-summary}}` と同じです。

<a id="date-and-time"></a>

## 日付と時刻

| マクロ | 出力例 |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - UTCオフセットでの時刻（`UTC-5:30` も動作） |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js の書式トークン](https://momentjs.com/docs/#/displaying/format/)。`[角かっこ]` 内のテキストはそのまま出力されます: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - 最後のパートを書いてからの経過時間。最初のパートでは `just now`。別名 `{{idle_duration}}`。 |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - 2つの時刻の差。`now`、日付（`2026-01-08`）、ISO 形式の時刻が使えます。不明な値は何も返しません。 |

時刻は Marginalia が動作しているコンピューターまたはサーバーのタイムゾーンで表示され、常に英語です。[Docker版](../getting-started/docker-server.md) では、環境変数 `TZ` でタイムゾーンを設定します（例: `TZ=Europe/Prague`）。

<a id="variables"></a>

## 変数

変数は、パート間で値を保存します。カウンター、キャラクターの気分、物語で確定した事柄などです。変数には2種類あります:

- **ローカル**変数は物語の分岐に属します。各パートがその値を記憶し、新しいパートは一つ前のパートの値から始まります。
- **グローバル**変数は本に属し、すべての分岐で共有されます。

[パート間に保持される内容](templates.md#what-is-kept-between-parts) を参照してください。

| ローカル | グローバル | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | 値。未設定の場合は空。 |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | 値を設定します。何も出力しません。 |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | 数値を加算します。どちらかが数値でない場合はテキストを追加します。何も出力しません。 |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | 1を加算し、新しい値を出力します。数値でない値は 0 から始まります。 |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | 1を減算し、新しい値を出力します。 |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` または `false`。 |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | 変数を削除します。別名 `flushvar` / `flushglobalvar`。 |

数値は末尾のゼロを省いて出力されます: `1.50` に `1.5` を加えると `3`。

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### 省略形

`{{.name}}` はローカル変数、`{{$name}}` はグローバル変数を表します。単独で使うと、省略形は値を出力します。演算子を付けると:

| 省略形 | 効果 | 出力 |
|---|---|---|
| `{{.hp = 20}}` | 値を設定 | なし |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | 加算 / 減算（`+=` はテキストにテキストを追加） | なし |
| `{{.turn++}}`, `{{.turn--}}` | 1を加算 / 減算 | 新しい値 |
| `{{.hp == 17}}`, `{{.hp != 17}}` | 比較 | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | 数値を比較（テキストの場合は `false`） | `true` / `false` |

フォールバックと既定値:

- 変数が空、または偽とみなされる値（`0`、`false`、`no`、`off`）の場合に *Guest* を出力します;
- 変数が全く設定されていない場合にのみ *Guest* を出力します;
- 変数が空または偽とみなされる値の場合に *Wanderer* を設定し、その後出力します;
- 変数が全く設定されていない場合にのみ設定し、その後出力します。

値にはマクロを含めることができます: `{{.log += -> {{user}}}}`。

例えば、ユーザープロンプトの先頭に置くパートカウンター（`+=` は何も出力せず、`{{trim}}` はそこに残る空行を取り除きます）:

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## 条件分岐

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

条件には次を使えます:

| 条件 | 真になるとき |
|---|---|
| マクロ名 - `{{if user}}` | マクロが値を返すとき |
| 変数 - `{{if .mood}}`, `{{if $chapter}}` | 変数に値があるとき |
| テンプレート変数 - `{{if sceneSetting}}` | 変数に値があるとき |
| 比較 - `{{if .hp > 20}}` | 比較が真のとき |
| 任意のテキスト - `{{if yes}}` | 偽とみなされる値を除き、常に真 |
| 否定 - `{{if !user}}`, `{{if !.mood}}` | その逆のとき |

偽とみなされるのは、空の値と `0`、`false`、`no`、`off`（大文字小文字は問わない）です。それ以外はすべて真です。

- 短い形式 `{{if user::text}}` は、条件が真のときに *text* を出力します。
- 実行される分岐だけが評価されます。もう一方の分岐にある `{{setvar}}` は実行されません。
- `{{if}}` ブロックは、スコープ付きマクロと同様にインデントが除去され前後の空白が取り除かれるため、読みやすさのためにインデントできます。`{{#if}}`（[Handlebars の if](templates.md#conditions)）は空白を保持します。
- `{{/if}}` のない `{{if}}` は、テキストとして出力されます。
<a id="expressions"></a>

## 式

`{{eval::expression}}` は `true` または `false` を返します。`{{if}}` ではできない比較が必要なときに使います。テンプレートの値（変数だけでなく）に対して使え、`and`、`or`、`not` で結合できます:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| 式の中で | 意味 |
|---|---|
| `"text"`、`'text'` | テキスト。**テキストは必ず引用符で囲みます**。引用符のない単語は名前とみなされます。 |
| `12`、`-3.5`、`true`、`false` | 数値または真偽値。 |
| `povCharacter`、`presentCharacters`、`sceneSetting`... | テンプレートの値で、`{{povCharacter}}` と同じです。引数のないマクロも使えます（`user`、`summary`、`lastMessage`）。 |
| `.mood`、`$chapter` | ローカルまたはグローバルの[変数](#variables)。設定されていない場合は空になります。 |
| `( )` | グループ化。 |

比較:

| 演算子 | 真になるとき |
|---|---|
| `==`、`!=` | 等しい、等しくない。数値は値で比較されます（`5 == 5.0`）。それ以外は完全一致で比較されるため、`"Fiora" == "fiora"` は偽です。 |
| `~=` | 大文字小文字を無視して等しい。 |
| `<`、`<=`、`>`、`>=` | 数値がその順序にある。どちらかが数値でない場合は偽です。 |
| `contains` | 左側のテキストの中に右側のテキストが含まれる（大文字小文字を無視）。`presentCharacters contains "ma"` は *Mara* でも真になります。 |
| `has` | 左側のリスト（カンマ、セミコロン、または改行で区切られたもの）に右側の項目がある（大文字小文字と項目の前後の空白を無視）。`presentCharacters has "Mara"` は *Marati* では真になりません。 |

結合: `and`（`&&`）、`or`（`||`）、`not`（`!`）。`not` が最も強く結合し、次に `and`、最後に `or` です。値だけを書いた場合、空、`0`、`false`、`no`、`off` でなければ真になります。テキストの中ではマクロを使えます（`"{{user}}"`）。これらは先に展開されます。

無効な式は `Error` を表示します。構文エラー、名前でない引用符のない単語（`povCharacter == Fiora`）、未知の名前などが原因です。すべての名前は、到達しない `and` / `or` の部分も含めてチェックされるため、入力ミスが隠れることはありません。[ロアブックの条件](../lorebooks/entries.md#a-condition-for-an-entry)で `Error` が出ると、そのエントリは使われず、プレビューにもその旨が表示されます。

<a id="randomness"></a>

## ランダム

| マクロ | 結果 |
|---|---|
| `{{random::red::green::blue}}` | 選択肢のどれか一つ。毎回異なります。`{{random:red,green,blue}}` や `{{random}}red, green{{/random}}` も使えます。 |
| `{{pick::sword::axe::bow}}` | 選択肢のどれか一つですが、同じブックで、テンプレート内の同じ位置では**常に同じもの**になります。ランダムに選ばれた名前のように、一貫性を保つ必要があるものに使います。 |
| `{{roll::2d6+3}}` | サイコロを振った結果: `1d20`、`3d4-10`、`d100`。また `{{roll d100}}`、`{{roll:6}}`（6面体のサイコロ一つ）も使えます。無効なロールは何も出力しません。 |

<a id="generation-info"></a>

## 生成情報

| マクロ | 結果 |
|---|---|
| `{{model}}` | ブックの[推論プロバイダ](../inference-providers.md)の名前。 |
| `{{maxContextTokens}}` | トークンで表したコンテキストの上限（プロトコルまたはプロバイダによる。[上限](../protocols.md#limits)を参照）。別名 `{{maxContext}}`。 |
| `{{maxResponseTokens}}` | トークンで表した応答の上限。別名 `{{maxResponse}}`。 |
| `{{maxPrompt}}` | コンテキストから応答を引いた値。プロンプトに使える余裕です。 |
| `{{lastGenerationType}}` | 新しいパートの場合は `normal`、`regenerate`、`swipe`、または要約の場合は `quiet`。 |

<a id="commands"></a>

## コマンド

| マクロ | 結果 |
|---|---|
| `{{command::name}}`、`{{command::name::argument::argument}}` | [コマンド](../commands/index.md)の答えをテキストとして返します。マクロ用に作られたコマンドのみ使えます（読み取りのみで、拡張機能がそのように明記します。例: [Character Tracker](../extensions/character-tracker.md) の `{{command::character-tracker-map}}`）。それ以外の名前は `Error` を表示します。失敗したコマンドは何も表示しません。 |

コマンドは、生成がそのマクロに到達したときに実行されます（`{{if}}` の選ばれなかった分岐にある場合は実行されません）。生成されるブックに対して実行され、生成のプロンプト（ユーザープロンプト、ロアブックのエントリ、マスターテンプレート）の中でのみ動作します。それ以外の場所では、マクロは何も表示しません。回答は、新しいパートが続く元のパートとともに保存されます。同じパートからの次の生成や、同じ生成の他のテンプレートは、コマンドを再実行する代わりにその回答を使います。**再生成**と**スワイプ**は、コマンドに再度問い合わせます。新しいパートも、新しい最後のパートから続くため、同様に再度問い合わせます。

<a id="utility"></a>

## ユーティリティ

| マクロ | 結果 |
|---|---|
| `{{newline}}`、`{{newline::2}}` | 一つ以上の改行。 |
| `{{space}}`、`{{space::4}}` | 一つ以上の空白。 |
| `{{trim}}` | その前後の改行を削除します。変数を設定するだけの行の後で役立ちます。 |
| `{{noop}}` | 何もしない。 |
| `{{join::separator::a::b::c}}` | 最初の引数の後のテキストを、最初の引数を間に挟んで結合します。数はいくつでも構いません（マクロも可）: `{{join::-::{{user}}::{{scenario}}}}`。空のテキストは除外されるため、省略可能なマクロが余分な区切り文字を残すことはありません。書かれた引数の前後の空白は取り除かれるので、空白を含む区切り文字にはマクロを使います: `{{join::{{space}}::a::b}}`、`{{join::{{newline}}::a::b}}`。 |
| `{{reverse::text}}` | テキストを逆順にします。 |
| `{{// comment}}` | コメント。削除されます。`{{//}}` ... `{{///}}` は、その間にあるマクロを含むすべてをコメントアウトします。 |
| `\{\{user\}\}` | `{{user}}` をそのまま表示します。 |

<a id="sillytavern-only-macros"></a>

## SillyTavern 専用のマクロ

SillyTavern のチャームでのみ意味を持つマクロ（ペルソナやキャラクターカードのフィールド（`{{persona}}`、`{{personality}}`、`{{charPrompt}}`、`{{mesExamples}}`...）、インストラクトモードのシーケンス（`{{instructUserPrefix}}`...）、作者のノート、スワイプID、`{{outlet}}` など）は認識され、**何も表示しません**。これにより、インポートされたワールドインフォが、そうしたマクロをプロンプトに漏らすことはありません。`{{isMobile}}` と `{{hasExtension}}` は `false` を表示します。

Marginalia が知らないマクロ:

- 引数がある場合（`{{fooBar::x}}`）、SillyTavern と同様にそのまま表示されます。
- 引数がない場合（`{{fooBar}}`）、未知の[テンプレート変数](templates.md#typos)と同様に **`Error`** と表示されます。
