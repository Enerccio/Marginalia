---
label: オーディオブック
order: 27
---

# オーディオブック

音声合成サーバーを使って、本をパートごとにオーディオブックに変換します。ナレーター用の声と、登場人物ごとの声を設定し、それらは本のパートと一緒に保存され、リーダーで再生できます。ラフ版を作るなら自動で走らせられます（モデルが誰がどのように話すかをラベル付けし、音声サーバーが読み上げます）。パートを一つずつ **オーディオブック・スタジオ** で仕上げることもできます。スタジオでは音声サーバーに送られる内容を正確に確認し、手で変更でき、結果をチャプター付きの mp3 ファイルとして書き出せます。

この拡張機能は音声の生成そのものは行いません。あなたが用意する **Chatterbox-TTS-Server** と通信します（[音声サーバーのセットアップ](audiobook-server.md)を参照）。本の本文は変更されず、指定したサーバー以外には送信されません。音声はあなた自身のインストール環境に、パートに紐づくリソースとして保存されます。

<a id="what-you-need"></a>

## 必要なもの

- 音声サーバー。言語モデルと同じグラフィックカードを使う場合は、音声サーバーのために場所を空け、終わったら停止するリクエストも必要です（[音声サーバーのセットアップ](audiobook-server.md)）。
- 声：クローンしたい声ごとに、短い **参照クリップ**（一人の人物の 12〜30 秒の音声、音楽なし）、またはサーバーが持っている声ファイルのどれか。ナレーターには落ち着いた、安定した朗読の声が必要です。
- 複数の声がある本では、テキストにラベルを付けられる言語モデル。既定では本のモデルを使います。
- **ffmpeg** は任意です。なくても mp3 で全て動作します。サーバーにあれば（Docker イメージには含まれています。デスクトップでは `PATH` や `/opt/homebrew/bin` などの一般的なフォルダで見つかります。別の場所にある場合は環境変数 `MARGINALIA_FFMPEG` で指定します）、書き出しで **m4b** 形式が使えるようになり、管理者は音声を wav で保存することもできます。

<a id="the-settings"></a>

## 設定

*設定 → 拡張機能の設定 → オーディオブックの設定* がアカウントごとのパネルです。変更したら *設定* ページを保存してください。

![バックエンドとアクターが並んだパネル](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### バックエンド

バックエンドは、音声サーバーとそれが使うモデルの組み合わせです。**+** で追加します。

| 設定 | 役割 |
|---|---|
| 名前 | 変換ダイアログに表示される名前。 |
| アドレス | 音声サーバー、またはそこへ転送するものの基本アドレス。例：`http://localhost:8004`。 |
| APIキー | サーバー（またはプロキシ）が要求する場合にベアラートークンとして送られます。暗号化して保存され、再表示されません。新しいキーを入力して置き換えるか、**キーを削除** を使います。 |
| 音声モデル | **Chatterbox Turbo**（英語、`[sigh]` などの音声タグに対応。最も速く、既定）、**Chatterbox**（英語、話し方の *exaggeration* を調整可能）、**Chatterbox Multilingual**（23言語）。バッチの前に送るリクエストで、このモデルを読み込んでおく必要があります。別のモデルが必要な本は、別のバックエンドを使います。 |
| GPU（名前） | 任意。一つの GPU を共有するサーバー（[画像ジェネレーター](image-generator.md)のものも含む）には同じ名前を入れます。すると、それらの処理が互いを待ち、別のモデルのバックエンドは他の処理が終わるまで待ちます。空欄の場合は、同じアドレスへのバッチ同士だけが互いを待ちます。 |
| サーバーはこのマシンまたは自分のネットワーク上にある | ローカルネットワーク内のアドレスを許可します。クラウド上のサーバーの場合はオフのままにしてください。そうすれば、自分のマシンを呼び出させられることはありません。 |
| リクエストのタイムアウト | 音声の一片を待つ時間。 |
| 次の数のピース後にサーバーを再起動 | メモリをリークし、長いバッチの途中でシステムに強制終了される音声サーバー向けの設定です。この数のピースを処理したあと、バッチ後のリクエスト、続けてバッチ前のリクエストが、バッチの途中で再び送られます（停止、起動、読み込み）。作業は新しいサーバーで続行されます。0（既定）では再起動しません。落ちたサーバーはこの設定に関係なく再起動されます。[トラブルシューティング](audiobook-server.md#troubleshooting)を参照してください。 |
| バッチ前後のリクエストのキー | バッチの前後に送るリクエストが `Authorization: Bearer` として付けるキーです。暗号化して保存され、再表示されません（**キーを削除** で消去されます）。そのため、読める状態で各リクエストのヘッダーに書き込む必要がありません。空欄の場合は上の *APIキー* が使われます。LiteLLM のような一つのプロキシがすべてを提供している場合はこちらが適切です。自前の `Authorization` ヘッダーを持つリクエストはそのまま保持されます。 |
| ナレーターの声 | 出演者の登録がない本のナレーターの声。サーバーの声ファイルは `predefined:Henry.wav` のように書きます。 |
| セグメントの既定値 | ピースが最初に使う温度、CFG 重み、速度、チャンクサイズ（スタジオでは一つのピースごとに変更できます）。 |
| バッチ前、バッチ後 | パートのバッチの前後に行うリクエスト。*前* はグラフィックカードを空けて音声サーバーを起動し、*後* は停止します。[1 つの GPU でモデルと音声を使う](audiobook-server.md#one-gpu-for-the-model-and-the-speech)を参照してください。空欄の場合は何もしません。 |

**接続テスト** はバッチ実行時と同じ処理を行います。バッチ前のリクエストを送り（停止していた音声サーバーを起動する）、サーバーが応答するか、モデルが読み込まれているかを確認し、バッチ後のリクエストを送ります。リクエストを設定していなければ、確認だけを行います。

本に別の文字体系のピースがある場合（英語の本の中の日本語など）、対応するバックエンドがあれば、それらの言語で多言語モデルに送られます。対応するバックエンドがなければ、本のモデルのままです。

<a id="shared-speech-servers"></a>

### 共有の音声サーバー

複数のユーザーがいる環境では、管理者がすべてのユーザー向けに音声サーバーを用意できます。それにより、GPU マシンを一度設定するだけでよく、誰もアドレスやキーを用意する必要がありません。管理者は同じパネルの最後のセクション（管理者にだけ表示されます）で設定します。**共有の音声サーバー** を開くと、自分用のバックエンドと同じ項目のウィンドウが開き、利用できるユーザーを指定できます。**すべてのユーザー**、またはリストから選んだユーザーです。そこにあるスイッチ **ユーザーが自分の音声サーバーを追加できる** で、下のバックエンドを引き続き使えるかどうかを決めます。

使えるようになること：

- パネルの上部にある **使用中のバックエンド** には、利用できる共有サーバー（*(共有)* と表示）と自分のサーバーが並びます。*音声に変換* ウィンドウとスタジオは、選んだものを使います。コマンドも同じものを使います。
- 共有サーバーは名前だけが表示され、アドレス、キー、リクエストは見えません。同じサーバーアドレスへの全ユーザーのバッチは互いを待つので、二人のユーザーが同じ GPU を二重に使うことはありません。
- 管理者があなたからサーバーの利用権を取り上げると、そのサーバーを指定したままのジョブは拒否されます。別のサーバーを選び直してください。
- 管理者があなた自身のサーバーを無効にすると、パネルにその旨が表示されます。追加したサーバーは残され、スイッチを戻せば再び使われます。
- 共有サーバーの *ナレーターの声* は、サーバーの声ファイル（`predefined:Henry.wav`）にしてください。参照クリップは一人のユーザーのリソースなので、他のユーザーは読めません。あなたのアクターの声はこれまで通り使えます。

<a id="actors"></a>

### アクター

**アクター** は、あなたの声です。参照クリップ（あなたの音声 [リソース](../resources.md) のどれか、またはここでアップロードした新しいもの）か、サーバーの声ファイルのどちらかを指定します。アクターには、**ムード**（ささやき、叫び）または**言語**（日本語を話すクリップ）ごとに別の声を持たせることもできます。言語が優先され、それに対応する声がなければ、アクターの通常の声が読み上げます。アクターはあなたのアカウントに保存され、どの本でも、そのナレーターや登場人物にアクターを割り当てられます。

声の横にある **プレビュー**（アクター、設定、出演者の各画面）は、その声で話す短い文を再生します（言語の声の場合はその言語で）。バッチ前のリクエストが音声サーバーの起動を必要とする場合は、サーバーを起動します。アクターは名前順に並びます。

![ムード用の声を持つアクター](../../images/plugin-audiobook-actor.png)

<a id="converting-new-parts-by-itself"></a>

### 新しいパートを自動で変換する

*新しいパートを自動で音声に変換* は、すべての本に対する既定値です。[自動変換](#automatic-conversion)を参照してください。
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## キャストと本の発音

どちらも本とともに保存され（バックアップにも含まれます）、**音声に変換**ダイアログから編集します。

**キャスト**は誰が読むかを指定します。**ナレーター**用の声優、**その他の話者**（キャストにない人物としてモデルが指定した話者）用の声優、そして**キャラクター**ごとの声優を割り当てます。キャラクターには名前と、本文がそのキャラクターを指すために使うほかの名前（*Tomáš Novák* に対する *Tom* など）があり、カンマで区切って並べます。発話は、その名前、ほかの名前、または他のキャラクターと共有されていない名前の一語が、大文字小文字とアクセント記号を無視して一致したとき、そのキャラクターに割り当てられます。**本のキャラクターを追加**を選ぶと、パートの視点人物と登場人物からリストが埋まります。

![本のキャスト](../../images/plugin-audiobook-cast.png)

**発音**は、音声のためにテキストをどう綴るかを指定するものです。本文そのものが変更されることはありません。**辞書**は単語全体を（大文字小文字を区別して）読み上げ用の表記に置き換えます。名前や作った言葉の読み方の綴り直し、日本語の読み仮名（かな）の指定に使います。英語の本では、規則によって数字、序数、年、時刻、金額、略語も単語として読み上げられます。数字だけの見出しは *Chapter four. The Arch* のように読まれます（数字の前の語はあなたが決めます）。また、`*` などのマークダウンの記号は読み上げられず、場面転換（`***`、`---`）は読まれませんが、少し長い間になります。英語以外の言語の本では規則をオフにしてください。辞書は引き続き適用されます。**テキストを試す**では、ある文章がどのように読まれるかを確認できます。読み上げ用のテキストが変わった部分だけが作り直され、それ以外は保持されます。

![本の発音](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## 書籍を変換する

*ストーリーエディタの「ツール」メニュー（歯車ボタン）→ **音声に変換***。

![開始前のダイアログ](../../images/plugin-audiobook-convert.png)

1. **バックエンド**を選び、**読み込み**を押します。現在のブランチにあるテキストの各パートが、その状態とともに一覧に表示されます（最初のパートは1）。状態は次のとおりです。
   *未生成*、*ラベル付け済み*（モデルのラベルは保存されているが、まだ音声はない）、*生成済み*（再生ボタンとダウンロードがある）、または
   *生成済み（テキストが変更されている）*（音声は古いテキストから作られたため、作り直されます）。スタジオで確定済みにされたパートには *(final)* が付きます。
2. 単純な版にする場合は**言語モデルでパートにラベルを付ける**をオフのままにします。ナレーターがすべての段落を読みます。オンにすると、モデルが各セリフの話者と話し方（落ち着いて、ささやく、叫ぶ…）を決め、効果音タグを入れる位置を決め、キャストが声を割り当てます。書籍のモデルが選ばれていますが、別のモデルを選ぶこともできます。パートはまずラベル付けされ、その後で読み上げられます。テキストに対するラベルがすでにあるパートは、ボックスがオフのときもそのラベルを使って読まれます（モデルには問い合わせません）。そのため、ラベル付け済みの書籍をボックスをオフにして再実行しても、セリフが普通の地の文になることはありません。普通の地の文にしたい場合や新しくラベルを付けたい場合は、**ラベルを削除**を使います。
3. パートにチェックを入れ（ヘッダーのボックスですべてにチェックが入ります）、それぞれに何をするかを**計画**します。**計画**列にその内容が表示されますが、まだ何も実行されません。
   **生成**（不足しているものを作る。最新の状態のパートは作り直さない）、**再生成**（音声を削除し、ラベルを削除してから生成する）、**音声を削除**（そのパートの音声の断片と結合された音声を削除する。ラベルは残る）、**ラベルを削除**（次回そのパートを作るときにモデルに改めて問い合わせる）、**計画をクリア**。手順はパート単位で積み重なります（生成を伴わない音声の削除やラベルの削除は、後始末だけを行う）。確定済みのパートはどの計画からも外されます。
4. **開始**を押すと計画が実行されます。音声やラベルを取り除く場合は、先に確認が出ます。生成するものは**キュー**に入ります。キューはウィンドウを閉じても、ページを離れても進みます。待機中または実行中のパートは計画から外されます。各パートの状態、それまでに作られた断片、ジョブのメッセージは、そのパートの行に表示されます。
   モデルがパートにラベルを付けている間、その行には *17-32 / 120 個の断片にラベル付け中* と表示されます（モデルへの問い合わせは一度に約16個の断片ずつ）。ラベル付けが済んだパートの**状態をクリック**すると、リクエストごとのモデルの推論過程と回答を、届いた順に読めます。これらは完了したジョブをクリア（*完了したものを消去*）するか、アプリケーションを再起動するまで、ジョブとともにメモリに保持されます。

![キュー：それまでに作られた断片、パートが完了するとダウンロードができる](../../images/plugin-audiobook-queue.png)

パートがどのように作られるかは次のとおりです。

- テキストは**プログラムが分割します**。モデルが分割することはありません。分割先は地の文の断片と直接話法の断片で、直接話法は半角または全角の二重引用符、または日本語のかぎ括弧で囲まれたものです（見出しは地の文になります）。モデルがするのは、番号付きの断片に*ラベルを付ける*ことだけです。効果音タグ付きのテキストは、タグを加えただけで他に何も変わっていない場合にのみ使われるので、モデルが書籍を変えてしまうことはありません。
- **断片の話者を決めるのは、セリフの場合はモデルの判断であり、地の文の場合はナレーターです。** 手紙、メモ、メッセージ、登場人物の内面の思考には引用符がないため、プログラムはそれを地の文として扱います。そのような断片は、**斜体で始まっている**（`*...*` または `_..._`）場合にのみ、キャストの声でその登場人物が読みます。そしてモデルが文脈（署名や「筆跡は見間違えようがなかった」といった記述）から登場人物の名前を示した場合に限ります。三人称の地の文は、「パーシーが部屋を横切るのを、彼女は見つめていた」のように登場人物の視点で語られていても、常にナレーターです。斜体のない断片にモデルが登場人物を割り当てても、プログラムはそれを無視します。強調、音、外国語のための斜体も地の文として扱われます。言葉は常にあなたのものです。
- **モデルへの指示は、変更できるテンプレートです。** *設定 → 拡張機能の設定 → Audiobook Settings → Labelling prompt* があなたの既定値になります。書籍（*Prompts* タブ → *Extension Settings* → *Audiobook*）やフォルダにも独自の指示を持たせることができます。空欄にすると、上位のフォルダの指示、次にあなたの既定値、最後に組み込みの指示が使われます。書き方は他の[プロンプト](../books/prompts.md)と同じです。変数は `{{characters}}`、`{{deliveries}}`、`{{tags}}`、`{{wordsPerTag}}` で、ヒントのポップオーバーには**既定のテンプレートを挿入**があります。指示を変更すると、次に作るときに、対象パートのラベルがモデルに改めて問い合わせられます。
- **パートの音量は揃えられます。** プログラムの ffmpeg（`.m4b` を作るものと同じ）がインストールされていれば、各パートの結合音声が一つの音量に揃えられます（EBU R128、リニア。ダイナミクスは保たれます）。目標は **-18 LUFS** です。*設定 → 拡張機能の設定 → Audiobook Settings* で変更でき、オフにすることもできます。断片はサーバーが作ったままの状態で保存され、揃えられるのは結合されたパートだけです。そのため、パートごとに音量が飛ぶことはありません。**以前に作られたパート**は、もう一度変換するまで触れられません。もう一度変換すると、保存された断片から結合し直して音量を揃えますが、音声サーバーへの要求は一切行いません（言語モデルのアンロードも行いません）。音量揃えをオフにしても、すでに揃えられたパートはそのままです。パートで ffmpeg が失敗した場合は、そのパートの音声は元のまま残り、結果にその旨が表示されます。
- 一度のクリックで作られるパートはすべて、**音声モデルごとに一つのバッチ**にまとめられます。バッチの前の要求が一度実行され、次にすべての断片が読み上げられ、最後にバッチの後の要求が一度実行されます。これによって、パートごとにグラフィックカードがモデルを入れ替えることを防ぎます。
- すべての断片は**内容によって保存されます**（テキスト、声、モデル、言語、設定、シード）。最新の状態のパートは何のコストもかからず、変更された段落だけが作り直されます。失敗したパートは、**再試行**を押したときに、すでに作られた断片を保持します。
- 断片は、その位置（見出しの後、段落の間、「と彼は言った」の前など）に応じた間を置いて結合され、パートごとに一つの mp3 になり、そのパートとともに保存されます。
- あなたが行っている生成が実行されている間、音声の生成は待機するため、その下で言語モデルがアンロードされることはありません。
- スタジオで**確定**にしたパートは変更されず、ダイアログにはその数が表示されます。
- **オーディオブック全体**は、最新の音声があるすべてのパートを一つの mp3 に結合し、各パートごとにチャプターマークを付けます。一覧の下には、ダウンロードリンクが表示されます（除外されたパートは名前が示されます）。同じファイルが、エクスポート形式の*チャプターマーク付きの mp3 一つ*にもなります。
- **すべて停止**は、待機中のパートをキャンセルし、実行中のパートを直ちに停止します。言語モデルへの要求（ラベル付け）や音声サーバーへの要求（読み上げ中の断片）は、完了を待たずに打ち切られます。開始前に停止されたバッチは、その前の要求を送らないため、言語モデルは読み込まれたままになります。**ダウンロード**を押すと、完了したパートの mp3 が得られます。
<a id="the-audiobook-studio"></a>

## Audiobook studio

*Tools menu → **Audiobook studio*** (the voice of the selected piece, with a preview, is beside its speaker) is where the final audiobook is made, a part at a time: you see the script, change it, listen to a
piece and make the part.

![The studio with a speech selected](../../images/plugin-audiobook-studio.png)

- While the part is being annotated or made, a bar shows that something is going on and the buttons that would start more work wait.
- **Annotate** makes the script of the part (with the language model if you ask for it, else with the narrator) and shows the pieces in a
  table: speaker, delivery, intensity, the spoken text and a mark for a piece you changed. While it runs a **Stop** button appears beside it and ends the request to the language model.
- Select a piece to edit its **speaker**, the **spoken text**, the **delivery**, the **intensity** (the exaggeration of the voice, for the models
  that have one), the **language**, the **seed**, the **pause after** it. **Apply** keeps the change and **locks** the piece: a new annotation leaves a locked piece as it is.
  **Unlock** gives it back to the annotation. *The request the speech server gets* shows the exact request.
- **Hear** makes the audio of a piece (only that piece, the speech server is started for it); **Another take** makes it again with a new
  seed, and the **takes** are kept so you can choose the one you like (*Use this take and hear it*). A good voice is often a matter of
  the seed.
- **Make the part from this script** joins the pieces as they are, with no new annotation.
- The **status** of the part is *draft*, *reviewed* or *final*. A final part is not annotated or converted again, and the export can be
  limited to the parts that are final.

## Exporting

The audiobook is a format of the book's *Export story*: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** and, when ffmpeg is installed, **Audiobook (one m4b with chapters)**. They are made of the audio of the parts that
are up to date: a part that has no audio, or audio older than its text or than its script in the studio, is left out and the window
says which. The mp3 files carry ID3 tags (the title of the book, the chapter, the track), and the single file has
chapter marks that players such as VLC and most audiobook apps show; the zip has a playlist. The check box **Audiobook: only the parts marked final**
limits the export to the parts you finished in the studio.

The **m4b** is the format Apple Books and most phone audiobook apps want: AAC audio (64 kbit/s, enough for speech) in an MP4 container with the chapters, the title, the author and the genre. ffmpeg makes it, so the format is
offered only on a server that has ffmpeg. Audio stored as mp3 is encoded a second time for it; audio stored as wav is encoded once.

### How the audio is stored

An administrator chooses it for the whole installation in *Settings → Extension Settings → Audiobook Settings* (the last section, seen only by administrators):

- **mp3** (default): small, joined without a decoder, no ffmpeg needed. The m4b is a second lossy step.
- **wav**: lossless, about ten times the size (an hour of speech is about 170 MB), needs ffmpeg. The pieces are asked from the speech server as wav and joined as wav; the browser plays it as it is;
  the mp3 and zip exports are encoded by ffmpeg (96 kbit/s) and the m4b is encoded once.

The choice applies to what is made from then on. Parts made before keep their audio and are understood as they are; a part of both kinds (a locked piece of the studio from before) is joined as wav, and an export of
parts of both kinds works, but needs ffmpeg.

![The export dialog with the audiobook formats](../../images/plugin-audiobook-export.png)

## Automatic conversion

A book can make the audio of a new part by itself, as soon as the part is final: after the automatic annotation, redraft and
translations if the book has them, as the last step of [what follows the part](../books/story-editor.md#what-follows-a-generated-part)
(in the panel of the runs until the audio is made; its **Stop** takes the part out of the queue). It is **off** by default. Choose it in the book's *Prompts* tab under extension
settings, for a folder of books, or for all your books in the audiobook settings; the nearest one wins:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; a book without a model gets the plain narration, and you are told.

![The setting in a book](../../images/plugin-audiobook-auto.png)

The part goes into your queue, with the cast and the pronunciation of the book. A notice tells you that it is queued and another that its
audio is ready or failed; they show only in the page that started the generation, the job is in the *Convert to audio* window in any case.

## Listening

A published book (or the reader) that has parts with audio gets a player bar: play, pause, stop, the speed, the part that is heard marked in the text. It starts from the selection or
from the first part in view, plays the parts in order and fetches the next one while the current plays, and the device's media controls know the book. Audio made from an older text of a part is
not offered. The audio is served to the owner of the book and, while the book is published, to everybody who can open it; a book that is taken back from publication stops being served at once (at the next part the player loads).

![The player in the reader](../../images/plugin-audiobook-player.png)

### A part without audio

The player stops at a part that has no audio, so that a part of the story is not skipped, and says so. When the extension [Read Aloud](tts-reader.md) is installed the bar also shows a speaker button, **Read this part (and the next ones without audio) with the voice of the browser**. Nothing is read with a machine voice unless you press it. It then reads that part and the following parts that have no audio with the voice of your device, and when it reaches a part that has audio the player takes over again. Later parts without audio in the same listening are read the same way without asking again; **Stop** silences it, and starting the player anew asks again. The voice, speed and pitch are those of Read Aloud.

## Commands

The same things as [commands](../commands/index.md) (the command line of the story editor, the `command` macro of a template, an agent). Parts are numbered as the Convert dialog
numbers them, or named by their handle.

| Command | What it does |
|---|---|
| `/audiobook-status [from [to]]` | The speech server in use, the way the book converts by itself, how many parts have audio (none, ready, older than the text, older than the script), the status of each listed part in the studio, and the queue. Slow only when the book is long. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Puts the parts into the queue (the whole book without arguments, one part when only `from` is given). `labelling` `true` uses the model of the book; `what` is `audio` or `script` (only the script for the studio, nothing is spoken). With `wait` `true` the command stays until the jobs are over, shows how far they are and tells how each ended; stopping it cancels the jobs that have not started. |
| `/audiobook-stop` | Cancels the waiting parts; the running one stops after its piece. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` or `final` in the studio; the part needs a script. |
| `/audiobook-cast` | The narrator, the fallback and the characters of the cast with their actors. |

![The result of a command](../../images/plugin-audiobook-command.png)

The audio is made for **your** book, not for the hidden working copy of an [agent](../agents.md): an agent can read the cast, and gets a refusal from the other commands, saying that
you have to run them. Run them from the command line, or give an agent the task of preparing the book (annotate, review) and convert it yourself.
<a id="notifications"></a>

## 通知

一連の変換が終わると、ブラウザーで知らせることができます（[通知](../account.md#notifications)を設定すると、音声サーバーの処理中もタブを開いたままにしなくて済みます）。*設定*ページの通知一覧には二つの項目があります。**Convert to audio**（ダイアログまたはコマンドで開始した一括変換）と**Convert to audio (automatic)**（自動で音声に変換された新しいパート）です。途中で停止した一括変換は通知されず、失敗したパートを含む一括変換は失敗したと表示されます。

![通知一覧](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## まだできないこと

- mp3 と wav のみ対応（opus は非対応）。音量は音声そのものの大きさに従います。
- 効果音や環境音は入れられません。
- 音声エンジンは Chatterbox の一つだけです。他のエンジンは今後の予定です。
- セグメントの外での声の試聴。
- キャストは本の *actors* のバックアップに含まれません。復元した本はキャストを保持しますが、アカウントに既に存在している俳優の名前を指定する必要があります。
