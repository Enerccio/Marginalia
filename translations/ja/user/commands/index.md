---
label: コマンド
order: 945
icon: terminal
---

# コマンド

コマンドは、メニューを順にクリックする代わりに入力して実行する小さな操作です。story内やlorebookの検索、検索と置換、バックアップの作成などができます。[拡張機能](../extensions/index.md)は独自のコマンドを追加します。たとえばlorebookのエントリを生成したり、名前を考えたりするものがあります。[エージェント](../agents.md)も同じコマンドを使って作業するため、各コマンドは一つの明確な操作だけを行い、何をするのかが入力しながら読めるようになっています。

**コマンドライン**は、[ストーリーエディタ](../books/story-editor.md)でメニューの下、ストーリーの近くにある入力欄です。

<a id="running-a-command"></a>

## コマンドの実行

1. コマンドラインをクリックして `/` を入力します。すべてのコマンドが一覧で表示され、それぞれに名前と短い説明が付いています。さらに入力すると一覧が絞り込まれます。絞り込みはコマンドの名前、タイトル、説明を対象に行われます。
2. マウスで選ぶか、矢印キーと **Enter** でコマンドを選びます。引数を必要としないコマンドはすぐに実行されます。引数が必要なコマンドでは、`/name ` が入力欄に書き込まれるので、続けて引数を入力します。
![Slash を入力して開いたコマンドの一覧](../../images/command-list.png)

3. 引数を入力して **Enter** を押すか、入力欄の横の再生ボタンをクリックすると実行されます。コマンドが始まると入力欄は空になります。コマンドではないテキストは残るので、修正できます。

`/` で始まらないテキストはコマンドではないため、何も実行されません。入力欄からフォーカスを外したり **Esc** を押したりしても、何も実行されません。そのため、うっかりコマンドを始めてしまうことはありません。

<a id="the-help"></a>

## ヘルプ

入力中は、入力欄の上に小さなウィンドウが開きます。ストーリーの一部を覆いますが、ほかの表示は動かしません。入力欄から離れるか、コマンドを実行すると閉じます。ヘルプには次の内容が表示されます。

- コマンドが何をするのか。
- ここまでに入力した内容に合う、コマンドの形式（[形式](#forms-of-a-command)を参照）。現在入力中の引数はハイライトされ、その説明が表示されます。
- コマンドが何を返すのか。
- 指定した引数の数が多すぎる場合や、その名前のコマンドが存在しない場合の注意。

![第二引数を入力中の /search-replace-lore-entries のヘルプ](../../images/command-help.png)

名前をまだ入力している間は、コマンドの一覧がそのままヘルプの役割を果たします。

<a id="arguments"></a>

## 引数

引数はスペースで区切られ、連続したスペースは一つとして扱われます。引数の*中に*スペースを含めるには、次の三つの方法があります。

| 入力する内容                 | コマンドが受け取る内容          | 理由                                       |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | 一つの引数 `two words`          | `\ `（バックスラッシュとスペース）はスペースになる |
| `/search-parts "two words"`  | 一つの引数 `two words`          | ダブルクォートで囲むと、中身がまとめて一つになる |
| `/search-parts say\ "hi"`    | 一つの引数 `say hi`             | クォートは引数の途中から始めることができる |

そのほかの特殊文字は次のとおりです。

| 入力する内容                  | 得られる内容               |
|-------------------------------|----------------------------|
| `\\`                          | バックスラッシュ           |
| `\"`                          | ダブルクォート。前後をクォートで囲んでも囲まなくてもよい。`"say \"hi\""` は `say "hi"` になる |
| `""`                          | 空の引数                   |

それ以外の文字の前にあるバックスラッシュはそのまま残ります。そのため `C:\books\story` はそのまま渡されます。クォートを開いたまま閉じなかった場合は、行の末尾までがその引数になります。

引数はテキストです。数値、はい/いいえ、またはいくつかの選択肢を求めるコマンドは、テキストを自分で変換します。テキストが合わない場合、コマンドは実行されず、その旨の通知が表示されます。

<a id="forms-of-a-command"></a>

## コマンドの形式

一つのコマンドが異なる引数を受け付けることがあります。たとえば `/list-parts` は引数なしで、`/list-parts 2` は一つの引数で使いますが、これらは同じコマンドの二つの**形式**です。Marginalia は、入力した引数の数と内容に応じて形式を選びます。どの形式にも合わない場合は、そのコマンドはその引数を受け付けないという通知が表示され、ヘルプにはそのコマンドが持つ形式が示されます。

<a id="the-result"></a>

## 結果

コマンドが何かを返すと、コマンドラインの上のウィンドウに表示されます。×ボタン、**Esc**、またはそのまま作業を続けることで閉じられます。新しいテキストを入力するか、別のコマンドを実行すると消えます。何も返さないコマンドでは、ウィンドウは表示されません。

![コマンドラインの上に表示された /take-backup の結果](../../images/command-result.png)

コマンドが失敗した場合や、そのコマンドが存在しない場合は、理由を示す通知が表示されます。

<a id="commands-that-take-time"></a>

## 時間のかかるコマンド

ほとんどのコマンドはすぐに応答します。時間が必要なコマンドは、アニメーションするバーと **停止** ボタンのあるウィンドウを表示します。ウィンドウは横に置かれ、ページはそのまま操作できます。ウィンドウの ⌄ を押すと、[実行中の一覧パネル](../books/story-editor.md#the-panel-of-the-runs)に折りたたまれます。そこではコマンドがモデルの書いている内容とともに一覧表示され、すべてのページから確認できます。バーの下には、コマンドが今何をしているかが表示され、さらに小さな文字で手順の詳細が表示されることがあります。**停止** を押すとコマンドはキャンセルされ、すぐに戻るはずです。`/generate-annotation` のように、モデルに要約やメモを書かせるコマンドがこの種類にあたります。

ストーリーのコマンドバーに入力した `/next`、`/swipe`、`/regenerate` には、このようなウィンドウはありません。新しいパートは、丸いボタンを押したときと同じようにストーリーへ流れ込み、丸いボタンで停止できます。ストーリーの保持は、ほかの生成と同様に行われます（lore、ほかのパート、サイドタブでの作業は続けられます）。パートが完成すると、コマンドの結果がバーの上に表示されます。それまでは、バーは同じ種類のコマンドをもう一つ受け付けません（テキストは残るので、後で実行できます）。ほかのコマンドは使えます。

![実行中のコマンドのウィンドウ](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## データを変更するコマンド

各コマンドは、既存のデータに対して何をするのかを示しています。コマンドバーでは、一覧とヘルプの中で、コマンドの横に小さなラベルとして表示されます。

- **破壊的**（赤）- 既存のデータを変更または削除するコマンドです。たとえば、本全体を書き換える検索と置換のコマンドがこれにあたります。ラベルにポインタを合わせると説明が表示されます。
- **非破壊的**（緑）- データを読み取るか、新しいデータを追加するだけのコマンドです。たとえば `/take-backup` はバックアップを追加するだけで、ほかは何も変更しません。

このラベルはあなたへの情報であり、コマンドを実行する際に止めるものではありません。[エージェント](../agents.md)はこのラベルを基準に判断します。破壊的なコマンドは渡されないか、先に確認を求めるか、そのエージェント用に設定された方法で実行されます。コマンド自身が確認を求める場合については、以下で説明します。

<a id="commands-that-ask-first"></a>

## 先に確認するコマンド

大きな変更を行うコマンドは、実行する前に確認を求めることがあります。検索と置換のコマンドにはプレビューがないため、検索後に、見つかった一致の数、何件のアイテムにあるか、何をどう置き換えるかを表示します。変更するのは **はい** を選んだ場合だけで、**いいえ** を選ぶと何も変わりません。質問には回答するまで表示され続けます。

![/search-replace-parts の確認](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Marginaliaに付属のコマンド

| コマンド                              | 機能                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 開いている本の物語内を検索と置換します。ストリータブの**検索と置換**ウィンドウと同じです。オプションは `true`/`false` で、この順に指定します。省略したものにはそのウィンドウの既定値が使われます（[regex](../regular-expressions.md) はいいえ、caseSensitive ははい、summaries ははい、allBranches はいいえ、revalidate ははい）。ある項目だけを設定するには、その前の項目も指定する必要があります。`maxHits` は、それより多くの一致が見つかった置換を拒否します。`dryRun` を `true` にすると何も変更されません。回答では一致数を数え、いくつかの例を示します。エージェントが置換の前に確認するのはこの方法です。プレビューはありません。検索後、見つかった一致数を置換してよいかを尋ねられ、**はい**を選んだときだけ本が変更されます。置換は途中で止められません。 |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 開いている本のロアブックのエントリ（名前、本文、コメント）に対して同じことを行います。 |
| `/take-backup`                       | 開いている本のバックアップを取ります。バックアップは本の**バックアップ**タブに現れます。開いている本がない場合は、本が開いていないと表示します。 |
| `/get-book-info` | 開いている本の名前、設定、モデル、合計（パート数、単語数、トークン数）、タグを表示します。 |
| `/list-parts [branch] [offset] [limit]` | ブランチのパートを一覧表示します。ハンドル、位置、サイズ、スワイプ、要約の状態、短いプレビューを含みます。 |
| `/get-part <part> [branch] [offset] [length] [field]` | パートの本文を区切って表示します（または `instructions`、`scene`、`pov`、`present`、`reasoning` を表示します）。あわせて、そのパートがどこから作られたかも表示します。 |
| `/list-swipes <part> [branch]` | パートの代替案（スワイプ）を、それぞれを表示するブランチ番号とともに表示します。 |
| `/list-branches [offset] [limit]` | 本のブランチを番号順に表示します。各ブランチが他からどこで分岐しているかも示します。 |
| `/get-part-metrics <part> [branch]` | パートのトークン、所要時間、モデル、プロトコルを表示します。 |
| `/show-prompt <part> [branch] [offset] [length]` | 生成されたパートのもとになったプロンプトを、保存されていたとおりに表示します。 |
| `/search-parts <query> [limit]` | すべてのブランチのパートを全文検索し、スニペット付きで表示します。 |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 名前や単語が何回、どこに出現するかを示します（既定値：regex なし、大文字小文字を区別しない、パートのみ、アクティブなブランチ）。 |
| `/compare-parts <a> <b> [branchA] [branchB]` | 2つのパートの間で異なる文を示します。`b` に `0` を指定すると、パート `a` とモデルが最初に生成した本文を比較します。 |
| `/count-tokens <text>`、`/count-tokens <part> <branch>` | テキストのトークン数（本のモデルのトークナイザーによる）、単語数、文字数を数えます。テキストがパートのハンドルの場合は、そのパートについて数えます。 |
| `/list-summaries [branch]` | ブランチの要約を、それがカバーするパートとともに表示し、まだ有効かどうかを示します。 |
| `/get-summary <part> [branch]` | パートの要約を表示します。 |
| `/check-summaries [branch]` | 古くなった要約と、それを古くした編集済みのパートを表示します。 |
| `/get-template-variables <scope> [part] [branch]` | 本全体（`global`）またはパート（`part`）のテンプレート変数を表示します。 |
| `/list-lorebooks` | 開いている本のロアブック（ルートとそのサブブック）を、番号、エントリ数、タグ、サブブックとともに表示します。 |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | ロアブックのエントリを、その設定とプレビューとともに表示します。filter は、名前またはタグにその文字列を含むエントリだけを残します。 |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 1つのエントリについて、設定、キー、発動（再帰、コンテキスト、パートの範囲、条件）、タグ、コメント、本文を区切って表示します。 |
| `/search-lore <query> [regex] [limit]` | 開いている本のすべてのロアブックのうち、名前、本文、コメントに query を含むエントリを、スニペット付きで表示します。 |
| `/preview-lore-activation <text> [limit]`、`/preview-lore-activation <part> <branch> <limit>` | テキスト、または、あるパートを作ったターンと同様のターンについて、生成時にどのエントリが使われるかを、その理由とともに示します（フィルターなし、フィルターが見つかった（テキスト中、または[走査の深さ](../lorebooks/entries.md#entries-brought-in-by-other-entries)があるロアブックの場合は別のエントリの内容中）、無効になっているため使われない、本のタグによって決まる、またはフィルターがテキストにない、のいずれか）。ロアの本文とそのトークンも示します。テキストにはパートのハンドルを指定できます。 |
| `/list-annotations [status [part [offset [limit]]]]` | 本またはパートの[注釈](../books/annotations.md)を表示します。`status` が `resolved`、`dismissed`、`orphaned`、`all` のいずれでもない場合は、未解決のものを表示します。 |
| `/get-annotation <annotation>` | 1つの注釈を、その箇所の前後のテキストとともに全文表示します。 |
| `/get-book-statistics [range] [side]` | 開いている本に対して、日ごとに何が行われたか（トークン、単語、パート、編集、時間、サイズ）を示します。[執筆統計](../books/statistics.md#commands)を参照してください。 |
| `/get-statistics [range] [by] [side]` | 同じ内容を、日ごとまたは本ごとに、すべての本について示します。開いている本は不要です。 |

<a id="reading-the-story"></a>

### 物語を読む

上記のコマンドのうち、本を読むもの（`/get-book-info` から `/get-template-variables` まで）は何も変更せず、JSON テキストで回答します。
そのため、エージェントも利用できます。回答の各行には **`handle`** があります。これはパート（またはロアブック、エントリ）の識別子であり、パートが追加、移動、削除されても変わりません。パートは、ハンドルで指定するか、ブランチ上の**位置**で指定します。物語の最初のパートは `1` です。ブランチは、その中のどのパートのハンドルでも、または `/list-branches` の**番号**でも指定できます。番号はブランチの末端が作られた順に付けられるため、ブランチが追加または削除されると変わります。位置と番号は入力しやすく、エージェントが使うべきものはハンドルです。物語を変更するコマンドはハンドルだけを受け付けます。ブランチを指定しなければ、コマンドはアクティブなブランチを対象にします。一覧は `offset` と `limit` でページ分けされ、長いテキストは区切られた断片（`offset`、`length`）として表示され、回答には全体の長さが示されます。他のコマンドと同様に、オプションは位置で決まります。ある項目だけを設定するには、その前の項目も指定する必要があります。

ロアブックを読むコマンド（`/list-lorebooks` から `/preview-lore-activation` まで）も同じように動作し、開いている本のロアブック、つまりルートのロアブックとそのサブブックだけを対象にします。ロアブックは、ハンドルか、`/list-lorebooks` の**番号**（ルートは `1`）で指定します。エントリは、ハンドルか、ロアブック内の**番号**で指定します。番号はロアブックの順序で付けられ、最初のエントリは `1` です。エントリやサブブックが追加または削除されると番号は変わります。ロアブックを指定しなければ、ルートのロアブックが使われます。
<a id="changing-the-story-and-the-lore"></a>

### 物語とロアの変更

これらのコマンドは、開いている本（エージェントの場合は作業用のコピー。エージェントの変更はそこに入ります）を変更します。パート、ロアブック、エントリは、必ず**ハンドル**で指定します。位置や番号では指定しません。位置や番号は、何かが追加または削除されると動いてしまい、読んだときには正しかった位置が、書き込むときには別のパートを指しているかもしれないからです。オプションは従来どおり位置で指定します。テキストやデータを削除するコマンドは `DESTRUCTIVE` で、実行前に確認を求めます（エージェントの場合は、代わりに破壊的モードで扱われます）。追加と変更は確認を求めません。物語を編集すると要約が古くなることがあります（要約は、それが説明している物語のハッシュを持っています）。回答では古くなった要約が一覧で示され、次にすべきことが書かれます。まだ正しい場合は `/revalidate-summaries`、正しくない場合は `/regenerate-summary` です。

| コマンド | 内容 |
|---------|--------------|
| `/edit-part <part> <old> <new> [field]` | パートの本文中の `old` を `new` に置き換えます。`old` はちょうど1回見つかる必要があり、そうでなければ何も変わらず、エラーに見つかった回数が示されます。代わりに `field` を指定すると、そのターンの `instructions`、`scene`、`pov`、`present`（登場人物）のいずれかを編集します。 |
| `/replace-part-text <part> <text> [field]` | パートの本文全体を置き換えます。実行前に確認を求めます。 |
| `/append-to-part <part> <text> [separator]` | パートの末尾にテキストを追加します。`separator` で指定しない限り、空行を挟んで追加されます。 |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | パートを作った元の指定を変更します。パートを新たに生成し直すときは、この指定が再利用されます。空の文字列を指定するとその項目は消去され、省略した項目はそのまま残ります。 |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 有効なブランチの最後のパートの後、または `parent` の後（新しいブランチになります）に新しいパートを書きます。新しいパートが有効なブランチの末尾になります。 |
| `/insert-part <text> <part> [before]` | `part` の直後に新しいパートを挿入します（`before` が `true` の場合は直前）。その場所より後にあったものはすべて、すべてのブランチを含めて、今すぐ新しいパートの後ろへ移動します。何も削除されず、再生成もされないため、他のパートは要約とデータを保持したまま、位置が1つ後ろにずれます。その場所を対象としていた要約は古くなります（どの要約かは回答に示されます）。ブランチの末尾に挿入する場合は `/create-part` と同じです。 |
| `/branch-part <part>` | パート（とその要約）を隣にコピーします。これは新しいスワイプで、新しいブランチの始まりになり、コピーが有効な末尾になります。 |
| `/delete-part <part>` | パートとその要約を削除します。その後に続いたパートは残ります。実行前に確認を求めます。 |
| `/switch-branch <part>` | パートを通るブランチを有効なブランチにします。 |
| `/set-summary <part> <text>` | パートの要約の本文を置き換えます。 |
| `/remove-summary <part> [unwind]` | パートの要約を削除します（`unwind` を指定すると、メタ要約が置き換えていたものを元に戻します）。実行前に確認を求めます。 |
| `/revalidate-summaries [part]` | 古くなった要約が、まだ物語を説明していると宣言します（有効なブランチ全体、またはそのパートだけ）。確認した場合にのみ使ってください。 |
| `/regenerate-summary <part>` | モデルにパートの要約を作り直させます。時間がかかり、トークンを消費します。 |
| `/create-meta-summary <from> <to>` | パート `from`（新しい方）から `to` までの要約を、モデルに1つにまとめさせます。時間がかかり、トークンを消費します。 |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | **New turn** ボタンと同様に、モデルに次のパートを書かせます。時間がかかり、トークンを消費します。1つの本の生成は一度に1つだけ実行され、他の生成は待機します。`profile` はエージェントのモデルプロファイルの1つを指定します。 |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | ブランチの最後のパートの別バージョンを、モデルに書かせます。省略した項目はそのパートから取られます。 |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | ブランチの最後のパートを書き直させ、その本文を置き換えます。実行前に確認を求めます。 |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | あらゆるパートを、あなたの指示に基づいてモデルに書き直させ（[パートの書き直し](../books/story-editor.md#redrafting-a-part)を参照）、本文を置き換えます。`prompt` は、本のものの代わりに使う書き直し用プロンプト、`profile` はエージェントのモデルプロファイル、`apply` を `false` にすると本文を返すだけです。`DESTRUCTIVE`。実行前に確認を求めます。時間がかかり、トークンを消費します。 |
| `/impersonate [instructions] [outline] [prompt]` | 次のターンの指示を、あなたの代わりにモデルに下書きさせ（[指示の下書き](../books/story-editor.md#drafting-the-instructions)を参照）、そのテキストを返します。何も変更されません。`instructions` はそのパートの内容（空の場合は、物語が向かう方向に続けます）、`outline` はモデルに示す物語の構想、`prompt` は本のものの代わりに使う impersonate 用プロンプトです。コマンドバーでのあなた専用です。[エージェント](../agents.md)には提供されません。時間がかかり、トークンを消費します。 |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 本の説明的な設定を変更します。与えた値だけを変更します。 |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 本のプロンプトテンプレートを置き換えます。それぞれ先に検証されます。`DESTRUCTIVE`：不正なテンプレートは、以降のすべての生成を台無しにします。 |
| `/set-book-tags [add] [remove]` | 本のタグを追加・削除します（カンマ区切り）。 |
| `/set-book-history-limit [maxVersions]` | 本の[履歴](../books/version-history.md#how-many-versions-are-kept)が、パートのバージョンをいくつ保持するかを設定します（`0` は今後は1つも保持しません）。数値を指定しない場合、本には上限を設定しません。フォルダ、ユーザー、管理者の上限は引き続き適用されます。バージョンは削除しません。 |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | ロアブックにエントリを追加します（指定がなければルート。ロアブックがない本には、本と同じ名前のものが作られます）。`keys` はカンマ区切りのキーワードのリストです。`filteringMode` は `KEYWORDS_OR`（既定）、`KEYWORDS_AND`、`TEXT`、`REGEX` のいずれかです。テンプレートや正規表現の問題は保存され、報告されます。 |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 指定したフィールドを変更します。 |
| `/delete-lore-entry <entry>` | エントリを削除します。実行前に確認を求めます。 |
| `/move-lore-entry <entry> <lorebook>` | エントリを、本の別のロアブックに移動します。 |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | エントリのタグと否定タグを変更します（カンマ区切り）。 |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | エントリが使われるタイミングを変更します。`preventRecursion`、`recursionOnly`、`activationSummaries`（`true` / `false`）、`activationContext`（`INSTRUCTIONS` または `INCLUDE_PARTS`）と `activationParts`、パートの範囲 `fromMessage` / `toMessage`（0 は上限を取り除きます）、`condition` テンプレート（空の文字列で削除されます）。空の文字列として与えられた値はそのまま残ります（`condition` を除く）。 |
| `/create-lorebook <name> [parent]` | `parent` のサブブックであるロアブックを作ります（指定がなければルート。ロアブックがない本は、まず1つ作られます）。 |
| `/set-lorebook <lorebook> [name] [enabled]` | ロアブックの名前を変更するか、オン・オフを切り替えます。 |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | ロアブックが含むロアブックを変更します。ロアブックは自分自身を含めることはできません。[エージェント](../agents.md)の場合、`/add-subbook` は、以前に削除された、または親から外されたロアブックを、そのエントリとともに戻すこともできます（以前持っていたハンドルで指定します）。 |
| `/delete-lorebook <lorebook>` | ロアブックをそのエントリとともに削除します（ルートは除く）。実行前に確認を求めます。 |
<a id="annotations"></a>

### アノテーション

[アノテーション](../books/annotations.md)を読み取るコマンド(`/list-annotations`、`/get-annotation`)は、他の読み取りコマンドと同じく JSON で応答します。
アノテーションは**ハンドル**で指定し、パートは内容を変更するコマンドでは**ハンドル**で指定します。これらのコマンドはメモだけを変更し、物語は変更しません。確認が必要なのは削除だけです。エージェントはすべてのコマンドを使えます。エージェントのメモは、[結果を確認する](../books/annotations.md#annotations-and-agents)ときに書籍へ反映されます。

| コマンド | 働き |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | パートにメモを追加します。`quote` は文章の言葉で、パートからそのままコピーします。メモはその一節に付き、本文を編集してもそれに従います。`quote` がない場合、または本文にない言葉を指定した場合は、メモはパート全体についてのものになります(応答でその旨が示されます)。 |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | メモの内容、種類、またはモデルにメモを渡すかどうかを変更します。テキストや種類を空にした場合は、そのまま残ります。 |
| `/resolve-annotation <annotation> [status]` | 状態を設定します。`resolved`(既定)、`dismissed`、`open` のいずれかです。 |
| `/reattach-annotation <annotation> [quote]` | メモを、引用が所属するパート内で見つかった一節へ移動します。引用がない場合、メモはパート全体についてのものになります。 |
| `/delete-annotation <annotation>` | メモを削除します(ゴミ箱から復元できます)。確認を求めます。 |
| `/generate-annotation <part> [profile]` | [AI でアノテーションを付ける](../books/annotations.md#letting-a-model-annotate)と同じように、モデルにパートを確認させ、見つかったものをすべてメモとして追加します(出典は *AI*。本文にない引用は、パート全体についてのメモになります)。確認は求めません。メモだけが変更され、解決、編集、削除できます。書籍のモデルで確認しますが、*エージェントのモデルで書籍を書く*が有効な場合はエージェント自身のモデルを使います(ロア・ジェネレーターのコマンドと同じです)。`profile` はエージェントのモデルプロファイルです。単独で実行され、時間がかかり、トークンを消費します。 |

<a id="the-planner"></a>

### プランナー

書籍の[プランナー](../books/story-editor.md#the-planner-tab)のコマンドです。シーンは一覧での**番号**で指定します(1 から始まります。`/get-planned-scenes` で表示されます)。シーンを移動または削除すると番号が変わるため、変更の後は一覧を読み直してください。これらのコマンドはプランナーだけを変更し、物語は変更しません。シーンの追加、移動、執筆済みへの変更は確認を求めません。シーンの本文の変更と削除は破壊的な操作であり、([エージェント](../agents.md)の場合は他の破壊的なコマンドと同様に)確認を求められます。

| コマンド | 働き |
|---------|--------------|
| `/get-planned-scenes` | シーンを、番号、タイトル、シーンの設定、視点キャラクター、登場キャラクター、指示、執筆済みかどうかとともに表示します。 |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 一覧の末尾にシーンを追加します。 |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | タイトルと、指定された項目を変更します。省略した項目は残り、空の値を指定するとその項目は消去されます。 |
| `/set-planned-scene-done <scene> [done]` | シーンを執筆済み(`done` の既定は `true`)または未執筆に設定します。 |
| `/move-planned-scene <scene> <to>` | シーンを位置 `to` へ移動します(1 が最初で、大きすぎる場合は最後の位置になります)。 |
| `/delete-planned-scene <scene>` | シーンを削除します。 |

書いたりレビューしたりするモデルを使うコマンドには時間がかかります。`/generate-annotation` と 2 つの要約は**停止**ウィンドウを表示し、`/next`、`/swipe`、`/regenerate` は物語へ順次流し込みます([時間のかかるコマンド](#commands-that-take-time)を参照)。

<a id="commands-that-come-with-extensions"></a>

## 拡張機能に含まれるコマンド

これらは、拡張機能が読み込まれている間は一覧に表示されます。それぞれの説明は、その拡張機能のドキュメントにあります。

| コマンド | 拡張機能 | 働き |
|---|---|---|
| `/list-lore-templates [<template>]` | [ロア・ジェネレーター](../extensions/lore-generator.md) | 保存済みの生成プロンプト、または1つのテキスト。 |
| `/generate-lore-entry [<prompt> ...]` | [ロア・ジェネレーター](../extensions/lore-generator.md) | ロアブックのロアを使ってロア項目のテキストを生成し、(`compact` が `false` でない限り)圧縮して返します。何も保存しません。 |
| `/compact-lore-entry <text> [...]` | [ロア・ジェネレーター](../extensions/lore-generator.md) | 生成されたテキストを、密度の高いロア項目のテキストに圧縮して返します。何も保存しません。 |
| `/list-name-categories [<filter>]`、`/generate-names <category> <count> [...]` | [名前ジェネレーター](../extensions/name-generator.md) | 架空の名前のカテゴリ、およびそのいずれかの名前。 |
| `/list-real-name-lists`、`/pick-real-names <list> <count>` | [名前プロバイダー](../extensions/name-provider.md) | 実在の名前のリスト、およびそのいずれかから選ばれた名前。 |
| `/list-place-types`、`/search-places <query> [...]`、`/places-near <place> <radius_km> [...]`、`/distance-between <from> <to>` | [場所プロバイダー](../extensions/place-provider.md) | 実在の場所の種類、名前・種類・国による検索、ある地点の周辺の場所、2 つの場所の距離。 |
| `/get-arc-outline [<arc> [<notes>]]` | [アークアウトライン](../extensions/arc-outline.md) | アウトラインのアークとチャプター。 |
| `/add-arc`、`/update-arc`、`/move-arc`、`/delete-arc` | [アークアウトライン](../extensions/arc-outline.md) | アークの追加、変更、移動、削除(`<arc>` は番号です)。 |
| `/add-outline-chapter`、`/update-outline-chapter`、`/move-outline-chapter`、`/delete-outline-chapter` | [アークアウトライン](../extensions/arc-outline.md) | アークのチャプターについて同じ操作を行います。 |
| `/generate-arc`、`/generate-outline-chapters <arc>`、`/import-arc-from-story` | [アークアウトライン](../extensions/arc-outline.md) | アークタブの生成機能です。結果を返し、`save` を付けた場合にのみアウトラインへ追加します。 |
| `/list-characters`、`/get-character <character>` | [キャラクタートラッカー](../extensions/character-tracker.md) | 書籍のキャラクター、および 1 人分のメモと関係性。 |
| `/create-character`、`/update-character`、`/delete-character` | [キャラクタートラッカー](../extensions/character-tracker.md) | キャラクターの追加、変更、削除。 |
| `/list-character-relations`、`/add-character-relation`、`/update-character-relation`、`/delete-character-relation` | [キャラクタートラッカー](../extensions/character-tracker.md) | キャラクター同士の関係性。 |
| `/list-notes`、`/get-note <note>`、`/search-notes <query>` | [ストーリーノート](../extensions/story-notes.md) | 書籍のノートのページ(アシスタントから隠されているものを除く)、テキストとリンク付きの 1 ページ、および検索。 |
| `/create-note`、`/append-to-note`、`/update-note`、`/edit-note`、`/delete-note`、`/note-text <note>` | [ストーリーノート](../extensions/story-notes.md) | ページの追加、追記、変更、編集、削除。`note-text` は {%{`{{command::note-text::Title}}`}%} マクロ用です。 |

ロア・ジェネレーター、名前ジェネレーター、名前プロバイダー、場所プロバイダーのコマンドは読み取りだけを行います。生成するコマンドはテキストを返すだけで、それをエントリに入れるかどうかはあなた、または呼び出したエージェントが判断します。アークアウトラインとキャラクタートラッカーのコマンドは、開いている書籍のアウトラインやキャラクターも変更します。破壊的なのは、それらの `delete` コマンドだけです。ストーリーノートも、開いている書籍のページを変更します。そこでは、既存のテキストを置き換える `update-note` と `edit-note` も破壊的です。

<a id="limits"></a>

## 制限

- コマンドラインは、デスクトップレイアウトのストーリータブにあります。[スマートフォン表示](../phone.md)にはまだありません。
- コマンドはあなたとして、あなたのセッションで実行され、開いている書籍に対して動作します。
