---
label: マクロリファレンス
order: 90
---

# マクロリファレンス

Marginalia は [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/) のマクロを理解するため、SillyTavern 向けに書かれたプロンプトやワールド情報はそのまま動作します。マクロは、すべての[テンプレート](templates.md)で使えます。マスターテンプレート、ユーザープロンプト、要約プロンプト、ロアブックのエントリーです。

{%{

<a id="syntax"></a>

## 構文

| 形式 | 例 |
|---|---|
| 引数なし | `{{user}}` |
| `::` で区切った引数 | `{{setvar::mood::nervous}}` |
| 最初の引数は空白の後に | `{{getvar mood}}`、`{{setvar greeting::Hello}}` |
| 旧形式の単一の `:` | `{{getvar:mood}}`、`{{random:north,south}}` |
| マクロの入れ子 | `{{getvar::{{user}}_mood}}` |
| スコープ付き（最後の引数として内容を渡す） | `{{setvar backstory}}Long text...{{/setvar}}` |

- マクロ名は大文字と小文字を区別しません。`{{User}}` と `{{USER}}` はどちらも動作します。
- 名前や引数の前後の空白は無視されます: `{{ getvar :: mood }}`。
- 余分な `::` は最後の引数に含まれます。`{{setvar::x::a::b}}` は `x` を `a::b` に設定します。
- スコープ内の内容は先に評価され、そのあとインデントが除かれ、前後の空白が取り除かれます。波かっこの後に `#` を付けた場合（`{{#setvar name}}...{{/setvar}}`）は、空白がそのまま保たれます。

<a id="names-and-participants"></a>

## 名前と参加者

Marginalia にはペルソナやキャラクターカードはありません。名前は、書いている部分の ターン指示 から取得されます。

| マクロ | 結果 |
|---|---|
| `{{user}}` | 視点キャラクター。 |
| `{{char}}` | 視点キャラクター。別名 `{{charIfNotGroup}}`。 |
| `{{group}}` | 登場キャラクター。指定がない場合は視点キャラクター。別名 `{{groupNotMuted}}`。 |
| `{{notChar}}` | 視点キャラクターを除く登場キャラクター（カンマまたは改行で区切られたもの。`, ` で結合）。 |
| `{{description}}` | 本の説明。 |
| `{{scenario}}` | シーンの設定。 |
| `{{input}}` | その部分の指示。 |

SillyTavern のマーカー `<USER>`、`<BOT>`、`<CHAR>`、`<GROUP>`、`<CHARIFNOTGROUP>` も（大文字小文字を問わず）動作します。

視点キャラクターが *Alice*、登場キャラクターが *Alice, Bob, Carol* の場合:
`{{user}}` → *Alice*、`{{group}}` → *Alice, Bob, Carol*、`{{notChar}}` → *Bob, Carol*。

<a id="story-history"></a>

## 物語の履歴

| マクロ | 結果 |
|---|---|
| `{{lastMessage}}` | 現在のブランチの最後の部分の本文。別名 `{{lastCharMessage}}`。 |
| `{{lastMessageId}}` | 最後の部分の位置（0から数える）。 |
| `{{lastUserMessage}}` | 最後の部分の指示。 |
| `{{summary}}` | 現在のブランチの最新の[要約](../books/summaries.md)。 |
| `{{closest-message}}`、`{{closest-message::n}}` | 書いている部分に最も近い部分: 最後の部分、またはそこから数えて *n* 番目の部分（`::2` は最後の一つ前の部分）。該当する部分がない場合は空。別名 `{{closestMessage}}`。 |
| `{{closest-summary}}`、`{{closest-summary::n}}` | ブランチの要約についても同様: 最新のもの、または *n* 番目に遡ったもの。別名 `{{closestSummary}}`。 |
| `{{current-message}}` | *再生成* またはスワイプで置き換えられる部分の本文。新しい部分の場合は空。別名 `{{currentMessage}}`。 |

本の最初の部分では、これらは空になります。*再生成* の場合、「最後の部分」は再生成される部分の一つ前の部分です。

`closest-message` と `closest-summary` の数値はマクロにすることができるため、変数から取得できます: `{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` は末尾から3番目の部分です。正の整数でない数値は何も返しません。`closest-summary` は、ブランチの部分の要約を数えます。ここには、[メタ要約](../books/summaries.md)と、それが代替している要約も含まれます。`{{summary}}` は `{{closest-summary}}` と同じです。

<a id="date-and-time"></a>

## 日付と時刻

| マクロ | 例の結果 |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - UTC オフセットでの時刻（`UTC-5:30` も使えます） |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js の書式トークン](https://momentjs.com/docs/#/displaying/format/)。`[brackets]` 内のテキストはそのまま出力されます: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - 最後の部分が書かれてからの経過時間。最初の部分の場合は `just now`。別名 `{{idle_duration}}`。 |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - 2つの時刻の差。`now`、日付（`2026-01-08`）、ISO 時刻が使えます。不明な値は何も返しません。 |

時刻は、Marginalia が動作するコンピューターまたはサーバーのタイムゾーンで表示され、常に英語です。[Docker 版](../getting-started/docker-server.md)では、環境変数 `TZ` でタイムゾーンを設定してください（例: `TZ=Europe/Prague`）。

<a id="variables"></a>

## 変数

変数は、部分をまたいで値を保存します。カウンター、キャラクターの気分、物語で確定した事柄などです。変数には2種類あります:

- **ローカル**変数は物語のブランチに属します。各部分は値を記憶し、新しい部分は一つ前の部分の値から始まります。
- **グローバル**変数は本に属し、すべてのブランチで共有されます。

[部分の間に保持されるもの](templates.md#what-is-kept-between-parts)を参照してください。

| ローカル | グローバル | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | 値。未設定の場合は空。 |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | 値を設定します。何も出力しません。 |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | 数値を加算します。どちらかが数値でない場合はテキストを追加します。何も出力しません。 |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | 1を加算し、新しい値を出力します。数値でない値は0から始まります。 |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | 1を減算し、新しい値を出力します。 |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` または `false`。 |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | 変数を削除します。別名 `flushvar` / `flushglobalvar`。 |

数値は末尾のゼロを省いて出力されます: `1.50` に `1.5` を足すと `3` になります。

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### 省略形

`{{.name}}` はローカル変数、`{{$name}}` はグローバル変数です。省略形は単独で使うと値を出力します。演算子を付けると:

| 省略形 | 効果 | 出力 |
|---|---|---|
| `{{.hp = 20}}` | 値を設定 | なし |
| `{{.hp += 5}}`、`{{.hp -= 5}}` | 加算 / 減算（`+=` はテキストにテキストを追加） | なし |
| `{{.turn++}}`、`{{.turn--}}` | 1を加算 / 減算 | 新しい値 |
| `{{.hp == 17}}`、`{{.hp != 17}}` | 比較 | `true` / `false` |
| `{{.hp > 10}}`、`>=`、`<`、`<=` | 数値を比較（テキストの場合は `false`） | `true` / `false` |

フォールバックとデフォルト:

- `{{.name || Guest}}` は、変数が空または偽の値（`0`、`false`、`no`、`off`）の場合に *Guest* を出力します。
- `{{.name ?? Guest}}` は、変数が全く設定されていない場合にのみ *Guest* を出力します。
- `{{.title ||= Wanderer}}` は、変数が空または偽の値の場合に *Wanderer* を設定し、それを出力します。
- `{{.title ??= Wanderer}}` は、全く設定されていない場合にのみ設定し、それを出力します。

値にはマクロを含めることができます: `{{.log += -> {{user}}}}`。

たとえば、ユーザープロンプトの先頭に置く部分カウンター（`+=` は何も出力せず、`{{trim}}` は残った空行を削除します）:

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

条件には次のものを指定できます:

| 条件 | 真になるとき |
|---|---|
| マクロ名 - `{{if user}}` | マクロが値を返すとき |
| 変数 - `{{if .mood}}`、`{{if $chapter}}` | 変数に値があるとき |
| テンプレート変数 - `{{if sceneSetting}}` | 変数に値があるとき |
| 比較 - `{{if .hp > 20}}` | 比較が真のとき |
| 任意のテキスト - `{{if yes}}` | 偽の値を除き、常に真 |
| 否定 - `{{if !user}}`、`{{if !.mood}}` | 反対の場合 |

偽となるのは、空の値と `0`、`false`、`no`、`off`（大文字小文字を問わず）です。それ以外はすべて真です。

- 短い形式 `{{if user::text}}` は、条件が真のとき *text* を出力します。
- 実行される分岐のみが評価されます。もう一方の分岐の `{{setvar}}` は実行されません。
- `{{if}}` ブロックは、スコープ付きマクロと同様にインデントが除かれ、前後の空白が取り除かれるため、読みやすいようにインデントできます。`{{#if}}`（[Handlebars の if](templates.md#conditions)）は空白を保持します。
- `{{/if}}` のない `{{if}}` は、テキストとして出力されます。
<a id="expressions"></a>

## 式

`{{eval::expression}}` は `true` または `false` を返します。`{{if}}` ではできない比較が必要なときに使います。変数だけでなくテンプレートの値に対しても比較でき、`and`、`or`、`not` で組み合わせられます:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| 式での書き方 | 意味 |
|---|---|
| `"text"`, `'text'` | テキスト。**テキストは必ず引用符で囲みます**。引用符のない単語は名前とみなされます。 |
| `12`, `-3.5`, `true`, `false` | 数値または真偽値。 |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | テンプレートの値。`{{povCharacter}}` のように書きます。引数のないマクロも使えます（`user`、`summary`、`lastMessage`）。 |
| `.mood`, `$chapter` | ローカルまたはグローバルの[変数](#variables)。未設定のときは空です。 |
| `( )` | グループ化。 |

比較:

| 演算子 | 真になるとき |
|---|---|
| `==`, `!=` | 等しい、等しくない。数値は値で比較されます（`5 == 5.0`）。それ以外は完全一致で比較されるため、`"Fiora" == "fiora"` は偽になります。 |
| `~=` | 大文字と小文字を区別せず等しい。 |
| `<`, `<=`, `>`, `>=` | 数値がその順序に並んでいる。どちらかの辺が数値でない場合は偽。 |
| `contains` | 左側のテキストに右側のテキストが含まれている（大文字と小文字を区別しない）。`presentCharacters contains "ma"` は *Mara* に対しても真になります。 |
| `has` | 左側のリスト（カンマ、セミコロン、または改行で区切られたもの）に右側の項目がある（大文字と小文字、項目前後の空白を無視）。`presentCharacters has "Mara"` は *Marati* に対しては真になりません。 |

結合: `and` (`&&`)、`or` (`||`)、`not` (`!`)。`not` が最も強く結合し、次に `and`、その次に `or` の順です。単独の値は、空、`0`、`false`、`no`、`off` でない限り真になります。
テキストの中ではマクロも使えます（`"{{user}}"`）。マクロは先に展開されます。

無効な式は `Error` と表示されます。構文エラー、名前ではない引用符なしの単語（`povCharacter == Fiora`）、未知の名前などが該当します。すべての名前は、到達しない `and` / `or` の部分も含めてチェックされるため、入力ミスが隠れることはありません。[ロアブックの条件](../lorebooks/entries.md#a-condition-for-an-entry)で `Error` が出ると、そのエントリは使われず、プレビューにその旨が表示されます。

<a id="randomness"></a>

## 乱数

| マクロ | 結果 |
|---|---|
| `{{random::red::green::blue}}` | 選択肢の一つを、毎回異なるように選びます。`{{random:red,green,blue}}` と `{{random}}red, green{{/random}}` も同様です。 |
| `{{pick::sword::axe::bow}}` | 選択肢の一つを選びますが、同じ本とテンプレート内の同じ位置では、常に**同じもの**を選びます。ランダムに選んだ名前のように、一貫性を保つ必要があるものに使います。 |
| `{{roll::2d6+3}}` | サイコロを振ります: `1d20`、`3d4-10`、`d100`。`{{roll d100}}`、`{{roll:6}}` も使えます（六面のサイコロ一つ）。無効な振り方の場合は何も出力されません。 |

<a id="generation-info"></a>

## 生成情報

| マクロ | 結果 |
|---|---|
| `{{model}}` | 本の[推論プロバイダー](../inference-providers.md)の名前。 |
| `{{maxContextTokens}}` | コンテキストの上限（トークン数。プロトコルまたはプロバイダーによる。[上限](../protocols.md#limits)を参照）。別名は `{{maxContext}}`。 |
| `{{maxResponseTokens}}` | 応答の上限（トークン数）。別名は `{{maxResponse}}`。 |
| `{{maxPrompt}}` | コンテキストから応答の上限を引いた値。プロンプトに使える余地です。 |
| `{{lastGenerationType}}` | 新しいパートでは `normal`、`regenerate`、`swipe`、要約では `quiet`。 |

<a id="commands"></a>

## コマンド

| マクロ | 結果 |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | [コマンド](../commands/index.md)の応答をテキストとして返します。マクロ用に作られたコマンドのみ使えます（読み取りのみで、拡張機能がその旨を明記します。例: [Character Tracker](../extensions/character-tracker.md) の `{{command::character-tracker-map}}`）。それ以外の名前は `Error` と表示されます。失敗したコマンドは何も表示しません。 |

コマンドは、生成がマクロに到達したときに実行されます（`{{if}}` の、選ばれなかった分岐の中にある場合は実行されません）。対象は生成される本で、生成のプロンプト（ユーザープロンプト、ロアブックのエントリ、マスターテンプレート）の中でのみ実行されます。それ以外の場所では、マクロは何も表示しません。応答は、新しいパートが続く元のパートと一緒に保存されます。同じパートからの次の生成と、同じ生成内の他のテンプレートは、コマンドを再実行せず、保存された応答を使います。**再生成**と **swipe** では、コマンドは再度実行されます。新しいパートは新しい最後のパートから続くため、どれも同様に再度実行されます。

<a id="utility"></a>

## ユーティリティ

| マクロ | 結果 |
|---|---|
| `{{newline}}`, `{{newline::2}}` | 一つまたは複数の改行。 |
| `{{space}}`, `{{space::4}}` | 一つまたは複数の空白。 |
| `{{trim}}` | その前後の改行を削除します。変数を設定するだけの行の後に便利です。 |
| `{{noop}}` | なし。 |
| `{{join::separator::a::b::c}}` | 最初のテキスト以降のテキストを、最初のテキストを間に挟んで結合します（個数は任意、マクロも可）: `{{join::-::{{user}}::{{scenario}}}}`。空のテキストは除外されるため、省略可能なマクロが余分な区切り文字を残すことはありません。書かれた引数の前後の空白は削除されるので、空白を含む区切り文字にはマクロを使ってください: `{{join::{{space}}::a::b}}`、`{{join::{{newline}}::a::b}}`。 |
| `{{reverse::text}}` | テキストを逆順にします。 |
| `{{// comment}}` | コメント。削除されます。`{{//}}` ... `{{///}}` は、その間のすべて（マクロを含む）をコメントアウトします。 |
| `\{\{user\}\}` | `{{user}}` をそのまま表示します。 |

<a id="sillytavern-only-macros"></a>

## SillyTavern専用マクロ

ペルソナやキャラクターカードのフィールド（`{{persona}}`、`{{personality}}`、`{{charPrompt}}`、`{{mesExamples}}`...）、インストラクトモードのシーケンス（`{{instructUserPrefix}}`...）、作者のメモ、スワイプID、`{{outlet}}` など、SillyTavernのチャットでしか意味を持たないマクロは認識され、**何も表示しません**。これにより、インポートされたワールドインフォがプロンプトにそれらを漏らすことはありません。`{{isMobile}}` と `{{hasExtension}}` は `false` を表示します。

Marginalia が知らないマクロは、次のように扱われます:

- 引数付き（`{{fooBar::x}}`）の場合は、SillyTavern と同様にそのまま表示されます;
- 引数なし（`{{fooBar}}`）の場合は、未知の[テンプレート変数](templates.md#typos)と同様に **`Error`** と表示されます。

}%}
