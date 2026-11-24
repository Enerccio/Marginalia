---
label: アカウントと設定
order: 920
icon: person
---

# アカウントと設定

Marginalia で作成するものはすべて、あなたのアカウントに属します。本、ロアブック、タグ、推論プロバイダー、プロトコル、設定などが対象です。他のユーザーはこれらを見ることができません（あなたが[公開](books/publishing.md)した本を除く）。また、あなたも他のユーザーのものは見られません。

<a id="your-account"></a>

## あなたのアカウント

ワークスペース下部のボタンです:

| ボタン | |
|---|---|
| **Language** | ユーザーインターフェースの言語。[Language](#language)を参照。 |
| 太陽／月のボタン | 言語の隣にあり、ダークテーマとライトテーマを切り替えます。[Theme](#theme)を参照。 |
| **Change Password** | パスワード、ユーザー名、氏名を変更します。下記を参照。 |
| **Logout** | このデバイスからログアウトします。 |
| **Admin** | 管理者のみ使用できます。[Administration](administration/index.md)を参照。 |

<a id="changing-the-password"></a>

### パスワードの変更

**Change Password** を押すと、**Username**、**Full Name**、**Current Password**、**Password**、**Repeat password** の入力欄があるダイアログが開きます:

- 変更のたびに **現在のパスワード** を入力します（アカウントにパスワードがない場合は空欄のままにします）。
- パスワードを変更するには、新しいパスワードを両方のパスワード欄に入力します。
- 両方のパスワード欄を空のままにすると、名前だけを変更し、パスワードはそのまま残ります。
- **Log out everywhere else (other browsers and all saved logins)** にチェックを入れると、パスワードを変えずに、他のすべてのデバイスからアカウントをログアウトさせられます。下記を参照。
- ユーザー名は、他のアカウントで使われているものであってはなりません。

**OK** を押して保存します。現在のパスワードが間違っている場合は、何も保存されません。

パスワードを変更すると、保存されたすべてのログイン（*Save login*）が終了します。現在のデバイスを含め、すべてのデバイスで新しいパスワードを使って再度ログインする必要があります。今開いている他のセッション（ほかのデバイスで作業画面を表示しているブラウザーなど）も終了します。作業中のセッションだけが続きます。デバイスを紛失した場合やパスワードが漏れた場合は、パスワードを変更することでそのデバイスをログアウトさせられます。

**Log out everywhere else** は、新しいパスワードを設定せずに同じことをします。アカウントの保存済みログインがすべて忘れられ、開いている他のセッションもすべて終了します。たとえば自分のものではないコンピューターなど、どこかでログインしたままにしてしまった場合に使います。このダイアログの他の変更と同様に、現在のパスワードが必要です。今使っているセッションは継続しますが、その保存済みログインも忘れられるため、このブラウザーでも次回はパスワードを求められます。

<a id="language"></a>

### 言語

ボタンの上にある **Language** セレクターで、ユーザーインターフェースの言語を切り替えます。選択内容はアカウントに保存されます。ページは新しい言語で再読み込みされ、開いている他のページ（別のデバイスのものも含む）も同様に再読み込みされます。次回ログインするときにもこの言語が使われます。ログイン画面自体は常に既定の言語で表示されます。

一覧に出るのは、インストールされている言語だけです。英語、日本語、フランス語、スペイン語、ドイツ語、韓国語、イタリア語は Marginalia に付属しています。管理者は、[拡張機能](extensions/index.md)を読み込むことで言語を追加できます。例えば [中国語ローカライズ](extensions/chinese-localization.md) などです。ある言語で翻訳されていない文章は英語で表示されます。同梱の拡張機能は、日本語、中国語、フランス語、スペイン語、ドイツ語、韓国語、イタリア語の文章を含みます（中国語のものは、中国語ローカライズが読み込まれているときに表示されます）。日付はその言語の書式で表示されます。本とその本文は翻訳されず、インターフェースだけが翻訳されます。

![言語セレクター](../images/language-selector.png)

![日本語の作業画面](../images/ui-japanese.png)

<a id="theme"></a>

### テーマ

**Language** セレクターの隣にあるボタンで、ユーザーインターフェースを、ダークテーマ（既定）とライトテーマの間で切り替えます。選択内容はアカウントに保存され、次回ログイン時に、どのデバイスでも再び使われます。別のタブで既に開いているページは、再読み込みすると変わります。グラフと関係マップは、開かれたときにテーマの色を使います。

![ライトテーマの作業画面](../images/theme-light.png)

<a id="logging-in"></a>

### ログイン

ログイン画面の **Save login** を選ぶと、このデバイスでのログイン状態が30日間維持されます（サーバーで使う場合は HTTPS が必要です。[サーバー（Docker）](getting-started/docker-server.md#https-and-reverse-proxy)を参照）。**Logout** を押すと、この状態は終了します。

アカウントにはパスワードを空にしておくこともできます。その場合は、ユーザー名だけでログインします。管理者が忘れられたパスワードを消去した場合は、この方法でログインし、すぐに **Change Password** で新しいパスワードを設定してください。これは自分のコンピューター上のデスクトップアプリでは便利ですが、他の人がアクセスできるサーバーでは絶対に行わないでください。

<a id="settings"></a>

## 設定

**Settings** タブには、すべての本に対するあなたの既定値が入っています。

![設定タブ](../images/settings-defaults.png)

上部には3つのボタンがあります:

- **Import Backup as New Book** - バックアップファイルから本を作成します。[本のバックアップ](books/backups.md#importing-a-backup-as-a-new-book)を参照;
- **Discard Changes** - 保存されていない変更を破棄し、保存済みの設定を読み込み直します;
- **Save** - 設定を保存します。

!!!info 未保存の変更
設定は自動的には保存されません。項目を変更するとすぐ（拡張機能の設定も含みます）、上部に *Unsaved changes* が表示され、他のタブ、**Admin**、**Logout** がロックされます。**Save** または **Discard Changes** を押すと、ロックが解除されます。
!!!

<a id="general-settings"></a>

### 一般設定

| 項目 | |
|---|---|
| **Default Model** | 新しい本が最初に使う[推論プロバイダー](inference-providers.md)。 |
| **Default Protocol** | 新しい本が最初に使う[プロトコル](protocols.md)。 |
| **Default model for annotating**、**Default protocol for annotating** | 実行時に別のものを選ばなかった場合に、[パートに注釈を付ける](books/annotations.md#letting-a-model-annotate)モデル。空欄の場合は、本のモデルになります。 |
| **Backup Strategy** | 独自の戦略を持たない本のための自動[本のバックアップ](books/backups.md#automatic-backups): *No Backups*、*After N Messages*、または *After N Minutes*（隣の数値と組み合わせ）。 |
| **Versions kept per part** | フォルダー、本、または管理者がより厳しく制限していない限り、すべての本について、パートの本文の過去バージョンを何件[履歴](books/version-history.md#how-many-versions-are-kept)に残すか。空欄ではここでは制限を設けません。**0** の場合は履歴を残しません。 |
| **Parts loaded at once in the story** | [ストーリー](books/story-editor.md)と[ビューアー](books/publishing.md)が一度に読み込んでおく本のパートの数。ストーリーは最後のパートから始まり、ビューアーは読むのを止めた場所（または最初のパート）から始まります。前後のパートはスクロールに合わせて読み込まれ、あなたから遠いものは再び破棄されます。空欄の場合はすべてのパートを一度に読み込みます。 |
| **Daily goal: words written**、**Daily goal: minutes of editing** | [執筆統計](books/statistics.md#the-daily-goal)のカレンダーに表示される1日の目標：すべての本にわたって、自分で書いた単語数と編集した分数。空欄は目標なし。 |
| **Record writing statistics**、**Keep statistics for (months)** | [執筆統計](books/statistics.md#privacy-clearing-not-recording-keeping-less)を集計するかどうか、そして何か月分を保存するか（空欄または0：すべて）。管理者は全員について記録をオフにしたり、より短い期間を設定したりできます。 |

*Default Model* と *Default Protocol* は、本を作成するときにだけ使われます。これらを変更しても、既存の本は変わりません。[Lore Generator](extensions/lore-generator.md) 拡張機能は本を持たず、これらを使って生成します。バックアップ戦略は、*Per manuscript override* を持たないすべての本に適用され、次に生成されるパートから有効になります。
<a id="templates"></a>

### テンプレート

[書籍のプロンプト](books/prompts.md)の既定値:

| 項目 | |
|---|---|
| **Prompt language** | 組み込みプロンプトの言語。英語(既定)、日本語、フランス語、スペイン語、ドイツ語、イタリア語、韓国語、または[中国語の現地化](extensions/chinese-localization.md)が読み込まれている場合は中国語。詳しくは下記の[プロンプトの言語](#prompt-language)を参照。 |
| **Master Template** | プロンプトの枠組み。[マスターテンプレート](books/prompts.md#master-template)を参照。 |
| **Point of View (POV)**, **Tense** | 語りの視点と時制。 |
| **Style** | 文体とトーンのガイドライン。 |
| **Default User Prompt** | 各パートの依頼。[ユーザープロンプト](books/prompts.md#user-prompt)を参照。 |
| **Default Summary Prompt** | [要約](books/summaries.md#the-summary-prompt)の依頼。 |
| **Default Meta Summary Prompt** | [メタ要約](books/summaries.md#the-meta-summary-prompt)の依頼。要約を統合する。 |
| **Redraft System Prompt**, **Redraft Prompt** | パートの[再下書き](books/story-editor.md#redrafting-a-part)のシステムメッセージと最後のメッセージ。[再下書きプロンプト](books/prompts.md#redraft-prompt)を参照。 |
| **Annotate System Prompt**, **Annotate Prompt** | モデルにパートを[注釈付け](books/annotations.md#letting-a-model-annotate)させる依頼のシステムメッセージと最後のメッセージ。[注釈プロンプト](books/prompts.md#annotate-prompt)を参照。 |
| **Annotate Instructions** | 注釈時にモデルが探すもの。何も設定していない書籍とフォルダー向け。[注釈プロンプト](books/prompts.md#annotate-prompt)を参照。 |
| **Impersonate Prompt** | あなたの代わりに次のターンの指示を[下書きする](books/story-editor.md#drafting-the-instructions)依頼。[代理プロンプト](books/prompts.md#impersonate-prompt)を参照。 |
| **Compaction prompt of agents** | [エージェント](agents.md)を使えるユーザーのみ。[`/compact`](agents.md#running-a-command-yourself)のときにエージェントへ与える依頼で、エージェント自身の圧縮プロンプトが空の場合に使われる。ここが空なら、管理者が設定したもの、なければ組み込みのものになる。 |
| **Instructions of agents (agents.md)** | [エージェント](agents.md)を使えるユーザーのみ。独自の指示を持たないエージェント向けの指示で、実行のたびにそのプロンプトへ追加される。空なら追加なし。 |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | [*Notifications*](administration/permissions.md)の権限を持つユーザーのみ(エージェントを使うユーザーだけではない)。*General* タブにあり、管理者が許可している場合のみ。エージェントが質問したり、あなたを待ったり、完了または失敗したりしたとき、アプリケーションを閉じている場合に短いメッセージを送る先は、[エージェントを使う](agents.md)場合も含めて[不在のとき](agents.md#when-you-are-not-there)を参照。 |
| **System prompt of agents** | [エージェント](agents.md)を使えるユーザーのみ。自身のシステムプロンプトが空のとき、エージェントに与える役割の指示。空なら管理者が設定したもの、なければ組み込みのもの。プレーンテキストで、テンプレートではない。 |

![テンプレートタブ](../images/settings-templates.png)

さらに下に、エージェントを使えるユーザーの場合はエージェントのプロンプトがあります:

![テンプレートタブのエージェントのプロンプト](../images/settings-templates-agents.png)

各フィールドは空のとき組み込みの既定値を灰色で表示し、**Insert Default Template** と利用可能な[変数とマクロ](templates/index.md)を含むヒントのポップオーバーを持ちます。

<a id="prompt-language"></a>

#### プロンプトの言語

**Prompt language** は組み込みプロンプトの言語を選びます。これは空のフィールドが何を意味するかを決めます(別の言語を選ぶとすぐに灰色のテキストが変わります)。そのため、生成、要約、再下書き、注釈、代理のためにモデルへ送られる内容も決まります。独自のプロンプトを持つプラグイン(Arc Outline、Beta Readers、Book Description Generator、Lore Generator、Manga Generator、SillyTavern Converter、Knowledge Tracker、Audiobook)とエージェントのプロンプトも、この設定に従います。これはインターフェースの言語とは独立しています。たとえばアプリケーションを英語で使いながら、日本語のプロンプトを送ることができます。モデルは物語の言語で書かれた指示に最もよく従うので、書籍が書かれている言語を選んでください。変わるのは組み込みプロンプトだけです。ここでも、書籍やフォルダーでも、自分で書いたプロンプトはそのまま残ります。[エージェント](agents.md)は、この設定ではなく、エージェントの設定の *Language* で指定された自分の言語でプロンプトと注意事項を読みます。

これらの既定値は、**自分のフィールドが空であるすべての書籍**に、新しい書籍だけでなく、すぐに適用されます。*Prompts* タブで独自の値を設定した書籍は、その値を保ちます。したがって:

- すべての書籍に共通のものはここで設定する;
- 1冊だけの特別なものは、その書籍の *Prompts* タブで設定する;
- 書籍のフィールドを空にすると、*Settings* の既定値に戻る。

<a id="styles"></a>

### スタイル

書籍の文章スタイル: パートの文章が物語の中や読者の画面でどう見えるかを決めます。このタブには、アプリケーションが用意し変更できない **Normal** と **Book** のスタイル(*built in* と表示されます)と、あなたのスタイルが並びます。スタイルを選ぶと右側で編集できます。一覧の下のボタン:

- **Duplicate** - 選択したスタイルから自分のスタイルを作成する(組み込みのスタイルからも可能。これが始め方です)。名前は *... (copy)* となり、後で名前と内容を変えられます;
- **Delete** - 自分のスタイルを削除する。組み込みのスタイルは削除できません;
- **Set as default** - 書籍が、ストーリーの設定メニューで[別のものを選ぶ](books/story-editor.md#text-styles)のでなければ使うスタイルにする。既定のスタイルは一覧で *default* と表示されます。
- **Export** and **Import** - 選択したスタイルを `.style.json` ファイルとして保存し、そのファイルのスタイルを自分のスタイルに追加する(設定を保存するまで保存されません)。[スタイルの共有](books/styling-guide.md#share-a-style-export-and-import)を参照。

[スタイリングガイド](books/styling-guide.md)では、すべてのオプションを順に説明し、ダウンロードできるスタイルの例も用意しています。

![スタイルタブ。スタイリングガイドのホラースタイルを選択した状態](../images/settings-styles.png)

自分のスタイルの場合、右側のフォームには **Name**、**For exports (not offered for books)** のチェックボックス、2つのタブがあります。スタイルは書籍用かエクスポート用のどちらかです。エクスポート用のスタイルは[エクスポートダイアログ](books/export.md#print-styles)に表示されます(アプリケーションは HTML、EPUB、PDF、Word のエクスポートの見た目として4つを用意しています)。このスタイルは書籍の既定スタイルにはできません。書籍用のスタイルはエクスポートダイアログには表示されません。一覧ではエクスポート用のものに *(export)* と表示されます。

- **Options** - グループごとに分かれています: *Text* (フォント、サイズ、行の高さ、文字と単語の間隔、色、配置、ハイフネーション、段落の間隔、段落の **first line indent**、見出しの後の **drop cap**)、*Page* (本文欄の幅、中央揃え、テキストの後ろ全体を塗る **background color**)、*Emphasis, links and code* (斜体と太字の色、リンクの色、コードのフォント)、*Quotes*、*Headings* (フォント、第1レベルのサイズ、色、配置、スモールキャピタル)、そして *Scene break* の記号と色 (`---`)。欄には任意の CSS 値を入力できます (`1.1rem`、`#336699`、`var(--lumo-body-text-color)`)。空の欄ではアプリケーションの見た目のままになります。フォントの欄には一般的なフォントの候補があり、独自のフォントも指定できます。
- **Custom CSS** - 自分で書いた CSS。オプションの後に追加され、スタイル内にのみ適用されるので、書いたルールはそのスタイルの文章にだけ効きます: `.chat-message-markdown p { text-indent: 1.5em !important; }` は段落をインデントし、単純な宣言(`font-size: 1.2rem;`)は文章全体に適用されます。その下の **Generated CSS** には、オプションから作られるスタイルシートが表示されます(読み取り専用)。波括弧 `{ }` は対応していなければならず、そうでなければスタイルは保存されません。

![スタイルの Custom CSS タブ](../images/settings-styles-css.png)

フォームの下の **Preview** には、そのスタイルで書かれたサンプル文章が表示され、入力するたびにすべての変更に追従します。

他のタブと同様に、スタイルタブも設定の **Save** で保存され、**Discard Changes** でその変更も破棄されます。複製、編集、削除、既定の設定は作業用のコピーを変えるだけで、そのいずれかを行うとすぐに *Unsaved changes* (およびロック)が表示されます。名前のないスタイル、または波括弧が対応していない Custom CSS を持つスタイルは、保存時に拒否されます。
<a id="exports"></a>

### エクスポート

各ファイル形式をどのように書き出すかを設定します。タブには、設定できる項目を持つ形式ごとのセクションがあります（**Plain text**、**Markdown**、**HTML**、**Word**、**PDF**、**EPUB**、**Markdown with front matter**、**Obsidian vault**。名前をクリックすると開きます）。

- **Print style**（HTML、Word、PDF、EPUB。フォントと色を持つ形式）- [エクスポートダイアログ](books/export.md)で、その形式に対して初期選択されるスタイルです。
  初期値は、アプリケーションがその形式に用意している見た目（**HTML page**、**PDF book**など）です。一覧には[エクスポート用スタイル](#styles)が並び、組み込みのものと自分で作ったものがあります。別のものを選ぶと、ダイアログで1回ごとに変更するまで、その形式は毎回そのスタイルで書き出されます。
- **Front matter**（Markdown with front matter と Obsidian vault）- ファイルの先頭に置かれる YAML ブロックです。`{{title}}`、`{{author}}`、`{{language}}`、`{{exportDate}}` を使います。空欄がデフォルトのブロックになります。
- **Title page**（他のツール向けの形式を除くすべての形式。Obsidian vault では、本のノートに持ちます）- タイトルページのテンプレートです。Markdown を書き出す Handlebars テンプレートで、`{{title}}` と `{{author}}` を使います（欄の情報アイコンに一覧があります）。空欄の場合はデフォルトになります。タイトルを見出しとして書き、その下に著者名を斜体で載せます。
  情報ポップオーバーの **Fill default** を押すと、デフォルトのテンプレートから始められます。
- **Authorship statement**（SillyTavern chat を除くすべての形式）- エクスポートのチェックボックス *Include authorship statement* をオンにしたとき、末尾に追加される文章です。`{{title}}`、`{{author}}`、`{{totalWords}}`、`{{writtenWords}}`、`{{generatedWords}}`、`{{writtenPercent}}`、`{{generatedPercent}}`、`{{models}}` が使えます（モデルはカンマで区切ります。ない場合は空欄です）。空欄の場合はデフォルトの文章になります。

これらはあなた自身の設定です。本やフォルダーは、それぞれ独自の設定を持つこともできます。詳しくは[タイトルページのテンプレート](books/export.md#title-page-template)を参照してください。このタブは、設定の **Save** で保存され、**Discard Changes** で変更を破棄します。他のタブと同じです。有効でないテンプレートは、保存時に拒否されます。Styles タブで作成したスタイルは、保存後にここで選べます。

![HTML セクションを開いた「エクスポート」タブ](../images/settings-exports.png)

<a id="tags"></a>

### タグ

本、ロアブック、ロアブックのエントリーに付けたすべてのタグと、各タグが関連付けられているオブジェクトの数を示す表です。列見出しをクリックすると、その列で並べ替えられます。他のタブと違い、操作はすぐに反映されます。未保存の変更には含まれず、保存するものもありません。

![タグタブ](../images/settings-tags.png)

| ボタン | |
|---|---|
| **Rename**（鉛筆） | 新しいタグ名を尋ねます。すべてのオブジェクトは、新しい名前でそのタグを持ちます。 |
| **Delete**（ゴミ箱） | 確認を求めたあと、タグを削除し、それを持つすべてのオブジェクトからも取り除きます。 |

新しい名前が別のタグの名前と同じ場合（大文字と小文字は区別されません）、名前変更によって2つのタグが同じになってしまいます。そのため、それらを **merge**（統合）するかどうかを尋ねられます。統合すると、名前を変更したタグが付いていたものはすべて既存のタグに移され、名前を変更したほうのタグは削除されます。両方のタグが付いていたオブジェクトには、既存のタグが1回だけ残ります。Cancel を選ぶと何も変わらず、タグは元の名前のままです。

![2つのタグを統合するかを尋ねる画面](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### キーボードショートカット

[ストーリーエディターのショートカット](books/story-editor.md#keyboard-shortcuts)のキーです（パソコンのストーリーエディターで動作します。[スマートフォン用のレイアウト](phone.md)にはありません）。この表には、[拡張機能](extensions/index.md)が追加するものを含むすべての操作と、その割り当てキーが並びます。また、本の一覧の [Organise](books/folders.md#organise) ウィンドウにあるボタンのキーも含まれます。ストーリーの設定メニュー、パートのメニュー、本文の右クリックメニューの各項目は、すべてここで操作として扱われます（多くは、キーを割り当てるまで何も設定されていません）。メニューには、各項目の横にキーが表示されます。

| ボタン | |
|---|---|
| **Record** | 新しいキーの組み合わせを押します（修飾キーを先に、または同時に押します。例：`Ctrl+Shift+K`）。それ以外の場所をクリックするとキャンセルされます。 |
| **Reset to default** | 操作の初期キーに戻します。 |
| **Clear** | 操作にキーを割り当てません。 |

赤い注記は、2つの操作が共有しているキー、ブラウザーが自分用に予約しているキー（`Ctrl+N`、`Ctrl+T`、`Ctrl+W`など。ページには届きません）、そして文字を伴うキーを示します（`Alt` と文字の組み合わせは、一部のキーボード、たとえば macOS では文字が入力されます）。`Ctrl`、`Alt`、`Meta` のいずれも伴わないキーは、テキスト欄にフォーカスがないときにだけ動作します。キーは他の設定と同じく **Save** で保存され、開いているストーリーにはすぐに反映されます。

![設定、キーボードショートカット](../images/settings-shortcuts.png)

<a id="notifications"></a>

### 通知

モデルの処理が終わったときに、ブラウザーのデスクトップ通知を出します。処理中にタブを離れていても気づけます。このタブには、チェックボックス **Use desktop notifications**、下のボタン、そして通知の対象の一覧があります。

- **Story generation**、**Annotate with AI**、**Annotate with AI (automatic)**、**Redraft**、**Redraft (automatic)**、**Draft Instructions**、**Summary**、**Resummarize all summaries**、**Help assistant**、そして[拡張機能](extensions/index.md)が追加するものです。拡張機能の項目は拡張機能の名前が付きます（*Reviewer: generate a review*、*Lore Generator: generate lore*、*Image Generator: generate the image*、*Arc Outline: generate chapters*など）。*Manga Generator* の項目のうち *(automatic)* と記されたものは、その連続モードです。
- *(automatic)* と記された項目は、本が自動的に開始する実行です。つまり、生成のあとの[自動注釈](books/story-editor.md)と、それに続くリドラフトです。これらは、自分で開くダイアログとは別に選択されます。**Annotate with AI** にチェックが入っていて **Annotate with AI (automatic)** にチェックが入っていない場合、自動注釈は通知されません。自動の項目だけにチェックが入っている場合は、自動注釈だけが通知されます。
- エラーで終わった実行も通知されます（*… failed*）。自分で停止した実行は通知されません。

このチェックボックスは、あなたのアカウントの通知をオンにし、2つのボタンを有効にします。**Register this browser** を押すと、（そのクリックをきっかけに）ブラウザーに許可を求めます。ボタンの下の行には、このブラウザーの状態が表示されます。許可されている、ブロックされている（ブラウザーのサイト設定で許可してください）、またはまだ尋ねられていない、のいずれかです。許可はアカウントではなく、ブラウザーに属します。通知を受け取りたいブラウザーやデバイスごとに、一度登録する必要があります。チェックボックスと一覧は、どこでも同じです。**Send a test notification** を押すと、ページが前面にあるときでもすぐに通知を表示します。

**Filter** 欄は名前で一覧を絞り込みます。**Check shown** と **Uncheck shown** は、表示されている項目にだけ作用し、それ以外はそのままにします。通知が表示されるのは、ページを見ていないとき（別のタブを開いているとき、またはウィンドウが前面にないとき）だけです。通知をクリックすると、ページが前面に戻ります。

通知には `https://` のアドレスか `localhost` が必要です。ネットワーク内のサーバーの通常の `http://` アドレスでは、ブラウザーは通知を提供せず、タブにもその旨が表示されます。ブラウザーがスリープさせたタブ（多くはスマートフォン）でも、通知は表示できません。このタブは他のタブと同じく、設定の **Save** で保存されます。

<a id="phrase-watch-list"></a>

### フレーズ監視リスト

すべての本が使う、あなたの[フレーズ監視リスト](books/annotations.md#watching-for-phrases)です（フォルダーや本は独自のものを追加できます）。また、その項目や、フォルダーと本の項目が選ぶ **annotation templates** も含まれます。見つかったフレーズのメモに何を書くか、その種類、そしてモデルに渡すかどうかを決めます。他のタブと同じく、設定の **Save** で保存されます。

![設定のフレーズ監視リスト](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### APIキー

Marginalia の HTTP API（`/api/v1`）を使うプログラムやスクリプトのためのキーです。プログラムはヘッダー `Authorization: Bearer <key>` にこのキーを送り、あなたが自分の本に対してできることを実行できます。管理者と、[*API access*](administration/permissions.md) の権限を持つユーザーだけが対象です。権限がない場合、タブにその旨が表示されます。

![APIキータブ](../images/settings-api-keys.png)

![新しいキー（一度だけ表示）](../images/api-key-created.png)

**New API key** では、**Name**（キーを使うもの。どのキーを取り消すかわかるように）、**Access**、そして **Expires**（日数の経過後。空欄の場合は無期限）を指定します。**Access** は次の組み合わせから選べます:

| アクセス | |
|---|---|
| **Read** | 本と、読み取りだけを行うコマンド。 |
| **Write** | すべてのコマンド（変更や削除を行うものを含む）。*Read* を含む。 |
| **Update the installation** | 管理者のみ: インストールの[アップデート](administration/updates.md)（その状態、ログ、開始）。 |
| **Manage the extensions** | 管理者のみ: [拡張機能](administration/extensions.md)のインストール、アップデート、停止、削除。 |

どのキーでも Marginalia のバージョンを読み取れます。キーには、そのプログラムが必要とするものだけを与えてください。プラグインをインストールするスクリプトに必要なのは *Manage the extensions* だけで、そのようなキーでは本に触れることはできません。キーの所有者が管理者でなくなると、そのキーではアップデートと拡張機能の操作が停止します。それ以外のアクセスは保持されます。

キーは作成直後に一度だけ表示されます。その時にコピーしてください。Marginalia はキーの指紋しか保存しないため、後から再表示することはできません。一覧には、あなたのキーの先頭部分、作成日時、有効期限、最終使用日時が表示されます。ゴミ箱ボタンでキーを**取り消し**ます。取り消されたキーを使うプログラムは直ちにアクセスできなくなります。キーは有効期限が切れたとき、また管理者が権限を取り上げたときにも動作しなくなります。キーの保存は作成時や取り消し時に行われ、設定の **Save** では保存されません。

**APIページ**: ブラウザで Marginalia の `/api/v1/` を開きます（例: `https://marginalia.example.org/api/v1/`）。すべての呼び出し、その引数、応答を確認できます。上部の **API key** にキーを貼り付けると、そのページには、そのキーで実行できるすべてのコマンドも引数付きで表示されます。**Try it out** → **Execute** を押すと、そのキーで呼び出しを送信し、応答が表示されます。呼び出しは実際に実行されます。コマンドは本を変更し、アップデートはサーバーを再起動します。ページがキーを保持するのはタブが開いている間だけで、ディスクには決して保存されません。

開発者向けには、呼び出しの詳細が[開発者マニュアル](../developer/api.md)に記載されています。

<a id="extension-settings"></a>

### 拡張機能の設定

インストールされている[拡張機能](extensions/index.md)の設定です。拡張機能ごとに1つのセクションがあります（例: *Reviewer Settings* や *SideQuery Settings*）。同じ **Save** ボタンで保存されます。設定を持つ拡張機能がインストールされていない場合、タブは空になります。

![Lore Generator の設定を表示した拡張機能の設定タブ](../images/settings-extensions.png)
