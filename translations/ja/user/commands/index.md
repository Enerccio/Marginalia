---
label: コマンド
order: 945
icon: terminal
---

# コマンド

コマンドは、メニューをクリックして回る代わりに入力して実行する小さな操作です。たとえば、物語やロアブックの中を検索する、検索と置換を行う、バックアップを取る、といったことができます。[拡張機能](../extensions/index.md)は、ロアエントリーを生成したり名前を考えたりするための独自のコマンドを追加します。[エージェント](../agents.md)が使うのも同じコマンドなので、各コマンドは明確にひとつのことだけを行い、何をするかを説明しています。入力しながら読めます。

**command line**は、[ストーリーエディター](../books/story-editor.md)で、物語の下にあるメニューの隣にある入力欄です。

<a id="running-a-command"></a>

## コマンドの実行

1. コマンドラインをクリックして `/` と入力します。一覧にはすべてのコマンドが、それぞれの名前と短い説明とともに表示されます。入力を続けると一覧が絞り込まれます。フィルターは、コマンドの名前、タイトル、説明を対象にします。
2. マウスでコマンドを選ぶか、矢印キーと **Enter** で選びます。引数を必要としないコマンドはすぐに実行されます。引数を取るコマンドの場合は、`/name ` が入力欄に書き込まれるので、引数の入力に進みます。
![スラッシュを入力して開いたコマンドの一覧](../../images/command-list.png)

3. 引数を入力して **Enter** を押すか、入力欄の横の再生ボタンをクリックして実行します。コマンドが開始されると入力欄は空になります。ただし、コマンドではないテキストはそのまま残るので、修正できます。

`/` で始まらないテキストはコマンドではないため、何も実行されません。入力欄の外をクリックしたり **Esc** を押したりしても、何も実行されません。そのため、うっかりコマンドを開始してしまうことはありません。

<a id="the-help"></a>

## ヘルプ

入力中、入力欄の上に小さなウィンドウが開きます。ウィンドウは物語の一部を覆いますが、何も動かしません。入力欄から離れるか、コマンドを実行すると閉じます。次の内容が表示されます:

- コマンドが何をするか、
- ここまでに入力した内容に合うコマンドのフォーム（[フォーム](#forms-of-a-command)を参照）。いま入力している引数はハイライトされ、その説明が表示されます、
- コマンドが返すもの、
- 引数をコマンドの必要数より多く入力した場合、またはその名前のコマンドが存在しない場合のヒント。

![2番目の引数を入力中の /search-replace-lore-entries のヘルプ](../../images/command-help.png)

名前をまだ入力している間は、コマンドの一覧がヘルプになります。

<a id="arguments"></a>

## 引数

引数は空白で区切られ、連続する空白は1つとして扱われます。引数の *内部* に空白を入れるには、3つの方法があります:

| 入力 | コマンドが受け取るもの | 理由 |
|------|------------------------|------|
| `/search-parts two\ words` | 1つの引数、`two words` | `\ `（バックスラッシュと空白）は空白になります |
| `/search-parts "two words"` | 1つの引数、`two words` | ダブルクォートで囲むと、中身がまとめて1つになります |
| `/search-parts say\ "hi"` | 1つの引数、`say hi` | クォートは引数の途中から始めることもできます |

そのほかの特殊文字:

| 入力 | 得られるもの |
|------|--------------|
| `\\` | バックスラッシュ |
| `\"` | ダブルクォート。前後をクォートで囲んでも囲まなくてもかまいません: `"say \"hi\""` は `say "hi"` になります |
| `""` | 空の引数 |

そのほかの文字の前にあるバックスラッシュはそのまま残るので、`C:\books\story` は変更されずに渡されます。クォートを開いたまま閉じないと、行の末尾までがその引数に含まれます。

引数はテキストです。数値、はい/いいえ、またはいくつかの選択肢のいずれかを求めるコマンドは、テキストを自分で変換します。テキストが合わない場合、コマンドは実行されず、その旨が通知で知らされます。

<a id="forms-of-a-command"></a>

## コマンドのフォーム

ひとつのコマンドは、異なる引数を取ることができます。引数なしの `/list-parts` と、引数ひとつの `/list-parts 2` は、同じコマンドの2つの**フォーム**です。Marginalia は、入力した引数の数とその形式によってフォームを選びます。どのフォームも合わない場合は、そのコマンドはこれらの引数を取らないという通知が表示され、ヘルプには存在するフォームが示されます。

<a id="the-result"></a>

## 結果

コマンドが何かを返すと、コマンドラインの上のウィンドウに表示されます。×ボタンで閉じるか、**Esc** を押すか、そのまま作業を続けてください。新しく何かを入力するか、別のコマンドを実行すると消えます。何も返さないコマンドはウィンドウを表示しません。

![コマンドラインの上に表示された /take-backup の結果](../../images/command-result.png)

コマンドが失敗した場合、またはそのようなコマンドがない場合は、理由を示す通知が表示されます。

<a id="commands-that-take-time"></a>

## 時間のかかるコマンド

ほとんどのコマンドはすぐに応答します。時間が必要なコマンドは、アニメーションするバーと **Stop** ボタンのあるウィンドウを表示します。ウィンドウは画面の横に置かれ、ページは引き続き操作できます。ウィンドウの ⌄ を押すと、[実行パネル](../books/story-editor.md#the-panel-of-the-runs)に折りたたまれます。そこには、すべてのページにわたって、コマンドがモデルの書いている内容とともに一覧表示されます。バーの下には、コマンドがいま何をしているかと、小さな文字でその手順の詳細が示されることがあります。**Stop** はコマンドを取り消します。すぐに戻るはずです。モデルに要約やメモを書かせる、`/generate-annotation` のようなコマンドがこの種類です。

物語のコマンドバーに入力された `/next`、`/swipe`、`/regenerate` にはそのようなウィンドウはありません。新しい部分は、丸いボタンを押したときと同じように物語へ流れ込み、丸いボタンで止められます。物語はほかのあらゆる生成と同じように「保留」の状態になります（ロア、ほかの部分、サイドタブでの作業は続けられます）。部分が完成すると、コマンドの結果がバーの上に表示されます。それまでは、バーはこれらのコマンドをもう一つ受け付けません（入力した文は残るので、あとで実行できます）。ほかのコマンドは動作します。

![実行中のコマンドのウィンドウ](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## データを変更するコマンド

どのコマンドも、既存のデータに対して何をするかを示しており、コマンドバーはその内容を、一覧とヘルプの中でコマンドの横に小さなラベルとして表示します:

- **Destructive**（赤）- 既存のデータを変更または削除するコマンドです。本全体を書き換える検索と置換のコマンドなどが該当します。ラベルにポインターを合わせると説明が表示されます。
- **Non-destructive**（緑）- コマンドはデータを読み取るか、新しいデータを追加するだけです。`/take-backup` がこれに当たり、バックアップを追加するだけで、ほかは何も変更しません。

ラベルはあなたへの情報であり、コマンドの実行を止めるものではありません。[エージェント](../agents.md)はこのラベルに従って動作します。つまり、破壊的なコマンドを渡されないか、先に確認を求めるか、そのエージェントに設定された通りに実行します。自分で確認を求めるコマンドについては、次に説明します。

<a id="commands-that-ask-first"></a>

## 先に確認するコマンド

大きな変更を行うコマンドは、実行する前に確認を求めることがあります。検索と置換のコマンドにはプレビューがないため、検索の後に、いくつの項目で何件の一致が見つかったか、何がどれに置き換えられるかを表示します。**Yes** を選んだときだけ本が変更され、**No** を選ぶとすべてがそのまま残ります。質問は、答えるまで表示されたままです。

![/search-replace-parts の確認](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Marginaliaに付属するコマンド

| コマンド                              | 内容                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 開いている本のストーリーを検索して置換します。ストーリータブの **Search and replace** ウィンドウと同じです。オプションは `true`/`false` の順に指定します。省略したオプションには、そのウィンドウの既定値が使われます（[正規表現](../regular-expressions.md)：いいえ、大文字小文字を区別：はい、要約：はい、すべての分岐：いいえ、再検証：はい）。あるオプションを指定するには、その前のものもすべて指定してください。`maxHits` は、それより多くの一致が見つかった場合に置換を拒否します。`dryRun` `true` を指定すると何も変更されません。回答では一致数が数えられ、いくつかの例が表示されます。エージェントは置換前にこの方法で確認します。プレビューはありません。検索後、見つかった一致数を置換するかどうか確認されます。**Yes** を選んだ場合にのみ本が変更されます。置換は途中で止めることはできません。 |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 開いている本のロアブックのエントリ（名前、本文、コメント）に対して同じ操作を行います。 |
| `/take-backup`                       | 開いている本のバックアップを作成します。バックアップは本の **Backups** タブに表示されます。開いている本がない場合は、本が開かれていないと表示されます。 |
| `/get-book-info` | 開いている本の名前、設定、モデル、合計（パート、単語、トークン）、タグ。 |
| `/list-parts [branch] [offset] [limit]` | 分岐のパート：ハンドル、位置、サイズ、スワイプ、要約の状態、短いプレビュー。 |
| `/get-part <part> [branch] [offset] [length] [field]` | パートの本文を分割して表示（または `instructions`、`scene`、`pov`、`present`、`reasoning`）し、そのパートの元になったものを表示します。 |
| `/list-swipes <part> [branch]` | パートの代替案（スワイプ）を、それぞれを表示する分岐番号とともに表示します。 |
| `/list-branches [offset] [limit]` | 本の分岐を番号付きで表示し、各分岐がどこから分かれているかを示します。 |
| `/get-part-metrics <part> [branch]` | パートのトークン、所要時間、モデル、プロトコル。 |
| `/show-prompt <part> [branch] [offset] [length]` | 生成されたパートの元になったプロンプトを、保存された形で表示します。 |
| `/search-parts <query> [limit]` | すべての分岐のパートを対象に全文検索し、抜粋を表示します。 |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 名前や単語が出現する回数と場所（既定値：正規表現なし、大文字小文字を区別しない、パートのみ、アクティブな分岐）。 |
| `/compare-parts <a> <b> [branchA] [branchB]` | 2つのパートの間で異なる文。`0` を `b` として指定すると、パート `a` をモデルが最初に生成した本文と比較します。 |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | テキストのトークン数（本のモデルのトークナイザーを使用）、単語数、文字数。テキストの代わりにパートのハンドルを指定した場合は、そのパートの値を表示します。 |
| `/list-summaries [branch]` | 分岐の要約、要約が対象とするパート、要約が有効かどうか。 |
| `/get-summary <part> [branch]` | パートの要約。 |
| `/check-summaries [branch]` | 古くなった要約と、それらを古くした編集済みのパート。 |
| `/get-template-variables <scope> [part] [branch]` | 本（`global`）またはパート（`part`）のテンプレート変数。 |
| `/list-lorebooks` | 開いている本のロアブック（ルートとそのサブブック）を、番号、エントリ数、タグ、サブブックとともに表示。 |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | ロアブックのエントリを設定と短いプレビューとともに表示します。フィルターは、名前またはタグにその文字列を含むエントリだけを残します。 |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 1つのエントリ：設定、キー、有効化（再帰、コンテキスト、パートの範囲、条件）、タグ、コメント、本文の分割表示。 |
| `/search-lore <query> [regex] [limit]` | 本のすべてのロアブックのうち、名前、本文、コメントに検索語を含むエントリを、抜粋とともに表示。 |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | テキストに対して生成がどのエントリを使うか、またはパートを作ったターンと同様のターンに対してどのエントリを使うかと、その理由を表示します。理由には、フィルターなし、フィルターが見つかった（テキスト内、またはロアブックに[スキャン深度](../lorebooks/entries.md#entries-brought-in-by-other-entries)がある場合は別のエントリの内容内）、エントリがオフになっているため使われていない、本のタグで決まる、フィルターがテキストにない、が含まれます。ロア本文とそのトークンも表示されます。テキストにはパートのハンドルを指定できます。 |
| `/list-annotations [status [part [offset [limit]]]]` | 本またはパートの[注釈](../books/annotations.md)。`status` で `resolved`、`dismissed`、`orphaned`、`all` のいずれかを指定しない限り、未解決のものを表示します。 |
| `/get-annotation <annotation>` | 1つの注釈を、その箇所の前後の本文とともに全文表示。 |
| `/get-book-statistics [range] [side]` | 開いている本に対して日ごとに行われた作業（トークン、単語、パート、編集、時間、サイズ）。[執筆統計](../books/statistics.md#commands)を参照。 |
| `/get-statistics [range] [by] [side]` | すべての本について、日ごと、または本ごとに同じ情報を表示。開いている本は必要ありません。 |

<a id="reading-the-story"></a>

### ストーリーを読む

本を読むだけで変更しない上記のコマンド（`/get-book-info` から `/get-template-variables`）は、回答をJSONテキストで返すので、エージェントも利用できます。
回答のすべての行には **`handle`** があります。これはパート（またはロアブック、エントリ）の識別子で、パートを追加、移動、削除しても変わりません。
パートは、そのハンドル、または分岐上の **位置** で指定します。ストーリーの最初のパートの位置は `1` です。
分岐は、その中のいずれかのパートのハンドル、または `/list-branches` から始まる **番号** で指定します（番号は分岐の末端が作られた順に付けられるため、分岐を追加または削除すると変わります）。
位置と番号は入力しやすく、エージェントはハンドルを使うべきです。ストーリーを変更するコマンドはハンドルのみを受け付けます。分岐を指定しない場合、コマンドはアクティブな分岐に対して動作します。
一覧は `offset` と `limit` でページ分けされ、長いテキストは分割されます（`offset`、`length`）。回答には全体の長さが示されます。
他のコマンドと同様に、オプションは位置で決まります。あるオプションを指定するには、その前のものもすべて指定してください。

ロアブックを読むコマンド（`/list-lorebooks` から `/preview-lore-activation`）も同様に動作し、開いている本のロアブック（ルートロアブックとそのサブブック）だけを対象にします。
ロアブックは、その **ハンドル**、または `/list-lorebooks` から始まる **番号** で指定します（ルートは `1` です）。エントリは、その **ハンドル**、またはロアブック内の **エントリ番号** で指定します。エントリ番号はロアブックの順序に従い、最初のエントリは `1` です。エントリやサブブックを追加または削除すると番号が変わります。
ロアブックを指定しない場合は、ルートロアブックが使われます。
<a id="changing-the-story-and-the-lore"></a>

### 物語とロアの変更

これらのコマンドは、開いている本（またはエージェントの作業用コピー。エージェントの変更はそこに入ります）を変更します。パート、ロアブック、エントリは、必ず**ハンドル**で指定します。位置や番号では指定しません。追加や削除があると位置や番号は動くため、読んだときに正しかった位置が、書き込むときには別のパートを指しているかもしれないからです。オプションは従来どおり位置で指定します。テキストやデータを削除するコマンドは`DESTRUCTIVE`で、確認を求めます（エージェントの場合は破壊的モードが適用されます）。追加と変更では確認を求めません。物語を編集すると要約が古くなることがあります（要約は、それが説明している物語のハッシュを持っています）。回答では古くなった要約を一覧にし、どうすべきかを示します。まだ正しければ`/revalidate-summaries`、正しくなければ`/regenerate-summary`を使ってください。

| コマンド | 働き |
|---------|------|
| `/edit-part <part> <old> <new> [field]` | パートの本文内で`old`を`new`に置き換えます。`old`はちょうど1回だけ見つかる必要があります。そうでなければ何も変わらず、見つかった回数がエラーで示されます。`field`は代わりに、ターンの`instructions`、`scene`、`pov` または `present` の文字を編集します。 |
| `/replace-part-text <part> <text> [field]` | パートの本文全体を置き換えます。先に確認を求めます。 |
| `/append-to-part <part> <text> [separator]` | パートの末尾にテキストを追加します。`separator`で別の指定がない限り、空行の後に追加されます。 |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | パートの生成元を変更します。新しい生成ではこれが再利用されます。空のテキストを渡すとそのフィールドは消去され、省略したフィールドはそのまま残ります。 |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 有効なブランチの最後のパートの後、または`parent`の後（新しいブランチ）に新しいパートを書きます。それが有効なブランチの末尾になります。 |
| `/insert-part <text> <part> [before]` | `part`の直後に新しいパートを物語へ挿入します（`before`が`true`の場合は直前）。その場所より後にあったものは、すべてのブランチを含めて、新しいパートの後ろに続くようになります。何も削除されず、作り直されもしないため、他のパートは要約とデータを保持し、位置が1つ後ろにずれます。その場所を対象としていた要約は古くなります（どれかは回答に示されます）。ブランチの末尾では`/create-part`と同じです。 |
| `/branch-part <part>` | パートを（その要約とともに）隣にコピーします。ブランチを始める新しいスワイプになり、そのコピーが有効な末尾になります。 |
| `/delete-part <part>` | パートとその要約を削除します。その後に続いていたパートは残ります。先に確認を求めます。 |
| `/switch-branch <part>` | パートを通るブランチを、有効なブランチにします。 |
| `/set-summary <part> <text>` | パートの要約の本文を置き換えます。 |
| `/remove-summary <part> [unwind]` | パートの要約を削除します（`unwind`を指定すると、メタ要約が置き換えていたものが戻ります）。先に確認を求めます。 |
| `/revalidate-summaries [part]` | 古くなった要約が今もなお物語を説明していると宣言します（有効なブランチ全体、またはそのパートの要約のみ）。確認した場合にのみ使ってください。 |
| `/regenerate-summary <part>` | モデルにパートの要約を作り直させます。時間がかかり、トークンを消費します。 |
| `/create-meta-summary <from> <to>` | パート`from`（最新）から`to`までの要約を、モデルに1つにまとめさせます。時間がかかり、トークンを消費します。 |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | **New turn**ボタンと同じように、モデルに次のパートを書かせます。時間がかかり、トークンを消費します。本の生成は一度に1つだけ実行され、他は待機します。`profile` はエージェントのモデルプロファイルの1つを指定します。 |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | モデルに、ブランチの最後のパートの別バージョンを書かせます。省略した項目は、そのパートから取られます。 |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | モデルに、ブランチの最後のパートを書き直させ、その本文を置き換えます。先に確認を求めます。 |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | あなたの指示に基づいて、任意のパートをモデルに書き直させ（[パートの書き直し](../books/story-editor.md#redrafting-a-part)を参照）、その本文を置き換えます。`prompt` は、本の代わりに使う書き直し用プロンプトです。`profile` はエージェントのモデルプロファイルです。`apply` `false` はテキストを返すだけです。`DESTRUCTIVE`。先に確認を求めます。時間がかかり、トークンを消費します。 |
| `/impersonate [instructions] [outline] [prompt]` | あなたに代わって、モデルに次のターンの指示を下書きさせ（[指示の下書き](../books/story-editor.md#drafting-the-instructions)を参照）、そのテキストを返します。何も変更されません。`instructions` はパートが何についてのものかを指定します（空の場合は、物語が向かう方向に続きます）。`outline` はモデルに渡す物語の計画です。`prompt` は、本の代わりに使うなりきり用プロンプトです。コマンドバーでの利用者のみが使えます。[エージェント](../agents.md)には提供されません。時間がかかり、トークンを消費します。 |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 本の説明用の設定を変更します。指定した値のみ変更されます。 |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 本のプロンプトテンプレートを置き換えます。それぞれ先に検証されます。`DESTRUCTIVE`：不正なテンプレートは、以降のすべての生成を損ないます。 |
| `/set-book-tags [add] [remove]` | 本のタグを追加・削除します（カンマ区切り）。 |
| `/set-book-history-limit [maxVersions]` | 本の[履歴](../books/version-history.md#how-many-versions-are-kept)が、パートのバージョンをいくつ保持するかを設定します（`0` では今後は1つも保持しません）。数値を指定しない場合、本には上限が設定されません。フォルダー、ユーザー、管理者の上限は引き続き適用されます。バージョンは削除されません。 |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | ロアブックにエントリを追加します（指定がない場合はルート。ロアブックを持たない本には、本と同じ名前のものが作られます）。`keys` はカンマ区切りのキーワードのリストです。`filteringMode` は`KEYWORDS_OR`（既定）、`KEYWORDS_AND`、`TEXT` または `REGEX` です。テンプレートや正規表現に問題があっても保存され、その問題が報告されます。 |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 指定したフィールドのみ変更します。 |
| `/delete-lore-entry <entry>` | エントリを削除します。先に確認を求めます。 |
| `/move-lore-entry <entry> <lorebook>` | エントリを、本の別のロアブックへ移動します。 |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | エントリのタグと除外タグを変更します（カンマ区切り）。 |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | エントリが使われるタイミングを変更します。`preventRecursion`、`recursionOnly`、`activationSummaries`（`true` / `false`）、`activationContext`（`INSTRUCTIONS` または `INCLUDE_PARTS`）と`activationParts`、パートの範囲`fromMessage` / `toMessage`（0 は制限を取り除きます）、`condition`テンプレート（空のテキストで削除されます）を指定できます。空のテキストで与えた値は変更されません（`condition`を除く）。 |
| `/create-lorebook <name> [parent]` | `parent` のサブブックであるロアブックを作成します（指定がない場合はルート。ロアブックを持たない本には、まず1つ作られます）。 |
| `/set-lorebook <lorebook> [name] [enabled]` | ロアブックの名前を変更するか、有効・無効を切り替えます。 |
| `/add-subbook <lorebook> <subbook>`、`/remove-subbook <lorebook> <subbook>` | ロアブックが含むロアブックを変更します。ロアブックは自分自身を含めることはできません。[エージェント](../agents.md)の場合、`/add-subbook` は、以前に削除した、または親から外したロアブックも、そのエントリとともに戻します（以前のハンドルで指定します）。 |
| `/delete-lorebook <lorebook>` | エントリとともにロアブックを削除します（ルートは除く）。先に確認を求めます。 |
<a id="annotations"></a>

### 注釈

[注釈](../books/annotations.md)を読むコマンド(`/list-annotations`、`/get-annotation`)は、他の読み取り系コマンドと同様にJSONで応答します。
注釈は**ハンドル**で指定します。パートも、内容を変更するコマンドではハンドルで指定します。これらのコマンドが変更するのはメモだけで、物語は変わりません。そのため確認を求めるのは削除だけです。エージェントはすべてのコマンドを使えます。メモは、[結果を確認する](../books/annotations.md#annotations-and-agents)ときに本へ反映されます。

| コマンド | 内容 |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | パートに注釈を追加します。`quote`は一節の語句で、パートからコピーされます。注釈はその一節に付き、本文を編集してもそれに従って動きます。引用がない場合、または本文にない引用の場合、注釈はパート全体に関するものになります(回答にその旨が示されます)。 |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 注釈の内容、種類、またはモデルが注釈を受け取るかどうかを変更します。空の本文や種類は変更されません。 |
| `/resolve-annotation <annotation> [status]` | 状態を設定します: `resolved`(既定)、`dismissed`、または`open`。 |
| `/reattach-annotation <annotation> [quote]` | 注釈を、引用がパート内で見つかる一節へ移動します。引用がない場合、注釈はパート全体に関するものになります。 |
| `/delete-annotation <annotation>` | 注釈を削除します(ゴミ箱から復元できます)。確認を求めます。 |
| `/scan-phrases [from to]` | 有効なブランチのパートを、本の[フレーズ監視リスト](../books/annotations.md)と照合し、新たな一致ごとに注釈を追加します(モデルは使いません)。引数がなければブランチ全体を、2つのパートを指定すると、その間のパートを対象にします。作成した注釈の数を答えます。 |
| `/generate-annotation <part> [profile]` | [Annotate with AI](../books/annotations.md#letting-a-model-annotate)と同様に、モデルにパートを確認させ、見つけたものすべてを注釈として追加します(ソースは*AI*。本文にない引用の場合は、パート全体に関する注釈になります)。確認は求められません。変更は注釈だけで、注釈は解決、編集、削除できます。本の既定のモデルで確認します。エージェントは*Write the book with the model of the agent*がオンの場合は、自身のモデルを使います(Lore Generatorのコマンドと同様)。`profile`はエージェントのモデルプロファイルです。単独で実行されるため時間がかかり、トークンを消費します。 |

<a id="the-planner"></a>

### プランナー

本の[プランナー](../books/story-editor.md#the-planner-tab)のコマンドです。シーンは、リスト内の**番号**(1から始まります。`/get-planned-scenes`で一覧を表示できます)で指定します。シーンを移動または削除すると番号が変わるため、変更後はリストを読み直してください。これらのコマンドが変更するのはプランナーだけで、物語は変わりません。
追加、移動、執筆済みのマークは確認を求めません。シーンの本文を変更することと削除することは破壊的であり、([エージェント](../agents.md)の場合は他の破壊的コマンドと同様に)確認を求められます。

| コマンド | 内容 |
|---------|--------------|
| `/get-planned-scenes` | シーンを、番号、タイトル、シーンの設定、視点人物、登場人物、指示、完了しているかどうかとともに表示します。 |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | リストの末尾にシーンを追加します。 |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | タイトルと、指定された項目を変更します。省略された項目はそのまま残り、空の本文はその項目を消去します。 |
| `/set-planned-scene-done <scene> [done]` | シーンを執筆済みとしてマークします(`done`の既定値は`true`)。または未執筆に戻します。 |
| `/move-planned-scene <scene> <to>` | シーンを`to`の位置へ移動します(1が最初で、大きすぎる場合は最後の位置)。 |
| `/delete-planned-scene <scene>` | シーンを削除します。 |

モデルに書かせたり確認させたりするコマンドは時間がかかります。`/generate-annotation`と2つの要約は**Stop**ウィンドウを表示し、`/next`、`/swipe`、`/regenerate`は物語へ流れ込みます([時間がかかるコマンド](#commands-that-take-time)を参照)。

<a id="commands-that-come-with-extensions"></a>

## 拡張機能に付属するコマンド

拡張機能が読み込まれている限り、これらのコマンドはリストに表示されます。各コマンドは、その拡張機能のドキュメントで説明されています。

| コマンド | 拡張機能 | 内容 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 保存した生成プロンプトの一覧、またはそのうち1つの本文。 |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | ロアブックのロアを使ってロア項目の本文を生成し、(`compact`が`false`でない限り)それを圧縮して返します。何も保存しません。 |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 生成されたテキストを、密度の高いロア項目の本文に圧縮して返します。何も保存しません。 |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 架空の名前のカテゴリ、およびそのいずれかの名前。 |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 実在の名前のリスト、およびそのいずれかから選んだ名前。 |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 実在の場所の種類、名前・種類・国による検索、ある地点の周辺の場所、2つの場所の間の距離。 |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | アウトラインのアークとチャプターの一覧。 |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | アークを追加、変更、移動、削除します(`<arc>`はそのアークの番号)。 |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | アークのチャプターについても同様です。 |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Arcsタブの生成機能です。結果を返し、`save`を指定したときだけアウトラインに追加します。 |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 本の登場人物の一覧、および1人分のメモと関係性。 |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 登場人物を追加、変更、削除します。 |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 登場人物同士の関係性。 |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 本のノートのページ(アシスタントから隠されているものを除く)、1ページの本文とリンク、および検索。 |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | ページを追加、追記、変更、編集、削除します。`note-text`は{%{`{{command::note-text::Title}}`}%}マクロ用に作られています。 |

Lore Generator、Name generator、Name provider、Place providerのコマンドは読み取りのみです。生成するコマンドはテキストを返し、それを項目に入れるかどうかは、あなた、またはそのコマンドを呼び出したエージェントに委ねられます。Arc OutlineとCharacter Trackerのコマンドも、開いている本のアウトラインと登場人物を変更します。破壊的なのは`delete`コマンドだけです。Story Notesも、開いている本のページを変更します。そこでは`update-note`と`edit-note`も、既存のテキストを置き換えるため破壊的です。

<a id="limits"></a>

## 制限

- コマンドラインは、デスクトップレイアウトのストーリータブにあります。[スマートフォン表示](../phone.md)にはまだありません。
- コマンドはあなたとして、あなたのセッション内で実行され、開いている本に対して動作します。
