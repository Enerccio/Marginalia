---
label: 音声サーバーのセットアップ
order: 28
---

# Audiobook 用の音声サーバーのセットアップ

[Audiobook](audiobook.md) 拡張機能は、HTTP で接続できる **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)、FastAPI、MIT ライセンス）を必要とします。このページはサーバー側の説明です。ここに記載の構成は 1 台のマシン（32 GB の AMD Radeon AI PRO R9700、すべての前段に LiteLLM、ルーターモードの `llama-server`）で試したものです。お使いのハードウェアやプロキシでは別の値が必要になる場合があります。

<a id="which-setup-is-yours"></a>

## お使いの構成はどれですか？

| お使いの状況 | 対応 |
|---|---|
| 音声サーバーが**別のマシン**、CPU 上、または**言語モデルと並べて入る**余裕のあるカードで動く | サーバーを起動し、そのアドレスをバックエンドに設定し、**バッチの前後のリクエストは空のままにします**。他に必要なものはありません。 |
| 音声サーバーと言語モデルが**同じカードを共有し、両方は収まらない** | バッチの前後のリクエストが必要です。**前**でカードを空けて音声サーバーを起動し、**後**で停止します。このページの残りの内容です。 |
| 言語モデルがクラウドモデルである | 空けるものはありません。音声サーバーは好きな場所で動かし、リクエストは空のままにします。 |

つまり、**前後のリクエストが必要になるのは、2 つのモデルが 1 枚の GPU を交互に使う場合だけです。** これが交代を実現します。プラグインは、*バッチ*（ワンクリックで開始したすべて）の前後で 1 回ずつこれを実行し、各パートごとには実行しません。

<a id="1-the-speech-server"></a>

## 1. 音声サーバー

`GET /api/model-info` が応答するように、Chatterbox-TTS-Server をコンテナ（またはソースから）で起動します。重要な点は以下のとおりです:

- **モデル。** Turbo（`chatterbox-turbo`）は拡張機能の既定値で、テストでは最も良い音声でした。多言語モデルは、本の言語とは別の言語のテキストの場合にだけ必要です。モデルはサーバーの `config.yaml`（または `POST /save_settings` と `POST /restart_server`）で設定します。バックエンドの *Speech model* は、バッチ前のリクエストのあとにサーバーが読み込んでいるモデルと一致している必要があります。
- **日本語には、イメージに `pykakasi` が必要です。** これがないと、サーバーは *pykakasi not available - Japanese text processing skipped* というログを出し、漢字を誤って読みます。その Dockerfile に `RUN python3 -m pip install --no-cache-dir pykakasi` を（`COPY . .` の前に）追加してください。ヘブライ語、ロシア語、中国語のパッケージでも同じ黙示的なスキップが起きます。
- **声**は、サーバーの `reference_audio/` フォルダー内のファイルです（残るようにマウントしてください）。拡張機能は、バッチの前に、登場人物のクリップを（`POST /upload_reference`）自分でアップロードします。名前はクリップのハッシュから作られます。サーバーには削除の呼び出しがないため、クリップはそこに溜まっていきます。
- サーバーには**認証がありません**。そのポートを公開しないでください。プライベートネットワーク上にない場合は、前段にプロキシを置いてください。
- オンデマンドで別の何かが起動するコンテナには `restart: "no"` を使います。`restart: unless-stopped` は起動時に起動し、何もしていないのにカードを占有します。

バックエンド（*Settings → Extension Settings → Audiobook Settings*）で、そのアドレスを入力します。サーバーが本機またはネットワーク内にある場合は *The server is on this machine or on my network* にチェックを入れ、**Test connection** を押します。目指す結果は *the server answers, the model is loaded* です。

拡張機能は `/tts`（パラメーターの全体: voice、language、exaggeration、CFG weight、temperature、speed、seed）と、`GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` を使って通信します。あなたとサーバーの間にプロキシがある場合は、これらすべてを通す必要があります。

<a id="one-gpu-for-the-model-and-the-speech"></a>

## モデルと音声で 1 枚の GPU を使う場合

言語モデルを載せているカードには、音声モデルを置く余裕がありません。どのプロキシでも手順は同じです:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**順序は変更できません。** テストでは、言語モデルがカードを保持している状態で音声コンテナを起動したところ、ホストのメモリが埋まりました（RAM 30 GB、スワップ、その後 OOM killer が他のサービスを停止）。そのため、アンロードを**必ず先に**行う必要があります。最初に失敗したリクエストでリストは終了するので、満杯のカードの上では何も起動しません。バッチの*後*のリクエストは、そのうちのどれに何が起きても、すべて実行されます。

サーバー内でモデルをアンロードする代わりにコンテナを*停止*する理由: サーバー内のアンロードでは、多数のモデル切り替えのあとでもカードの約 1 GB が使われたままでした。コンテナを停止すると、すべて解放されます。

<a id="the-requests"></a>

### リクエスト

リストの各リクエストは、メソッド、アドレス、ヘッダー、ボディ、そのあとの待機時間（秒）で構成されます。ヘッダーにキーを書き込まないでください（フォームはヘッダーを入力どおりに表示します）。代わりに **Key of the requests around a batch** に入力してください。空のままにすると、バックエンドの *Api key* が送られます。これは、独自のキーを持たない下記のすべてのリクエストに `Authorization: Bearer` として送られます。すべての前段に LiteLLM があり、キーが 1 つの場合、リストは次のようになります:

| | メソッドとアドレス | ヘッダー | ボディ | 役割 |
|---|---|---|---|---|
| 前 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 言語モデルをアンロードします。フック自体が応答し、1〜2 秒かかります。 |
| 前 2 | `POST http://litellm:4000/gpu/tts/start` | | | 音声コンテナを起動し、サーバーの準備ができたら応答します（約 13 秒）。 |
| 前 3（サーバーがモデルなしで起動する場合のみ） | `POST http://litellm:4000/gpu/tts/load` | | | モデルを読み込みます（8〜14 秒、初回ダウンロード時は 50 秒）。 |
| 後 1 | `POST http://litellm:4000/gpu/tts/stop` | | | コンテナを停止します（1 秒未満）。 |

言語モデルは自動的に戻ります。次の通常のリクエストで、ルーターがモデルを読み込みます（大きなモデルでは約 1 分。[prompt cache hook](../prompt-cache.md) を使うと、セッションのキャッシュが復元されます）。そのため、バッチ後にモデルのためのリクエストは必要ありません。

LiteLLM を使わない場合も、同じリクエストを、コンテナを起動・停止する仕組みに送ります。守るべき規則は上記の順序だけです。

<a id="what-answers-those-addresses"></a>

### これらのアドレスに応答するもの

- **`X-Llama-Unload`** には、LiteLLM のフック `gpu_unload.py` が応答します。そのコードは [Freeing the GPU for another model](../prompt-cache.md#freeing-the-gpu-for-another-model) で説明しています（キャッシュフックなしでも動作します）。ルート `openai/*` の場合、ボディ内のモデル名は `openai/` で始まる必要があります。別の名前だと、モデルがアンロードされた*あと*に 400 が返されます。
- **`/gpu/tts/start`、`/stop`、`/load`** は、リポジトリの小さなコントローラー `tools/gpu-control` への LiteLLM の **パススルーエンドポイント**です。このコントローラーは、名前によってコンテナを起動・停止します。その README には、**Docker socket proxy** を含む compose ファイルと、間違えやすい設定が記載されています（コントローラーがコンテナの起動・停止だけを行い、それ以外は何もできないようにします。ソケット自体はホスト上の root 権限を持つことに注意）。間違えやすい設定は `POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`** です。LiteLLM では:

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  （コンテナの名前は `chatterbox` で、`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` はコントローラーに待機対象を伝えます。コントローラー自身の `.env` と LiteLLM の環境変数にトークンが置かれ、Marginalia が持つのは LiteLLM のキーだけです）。
- **すべてのコンテナと LiteLLM は同じ Docker ネットワークを共有します**（`docker network create ai` を一度実行）。`networks: [default, ai]` と外部ネットワーク `ai` を各 compose ファイルに記述してください。手作業で行った `docker network connect` は、コンテナを再作成すると失われます。

<a id="checking-it"></a>

## 動作確認

1. `curl http://<speech server>/api/model-info` が応答します（起動後に `loaded` が true になります）。
2. バックエンドの *Test connection* で、モデルが読み込まれていると表示されます。バッチ前のリクエストだけがサーバーを起動する場合は、リストを設定したままテストしてください。*the server answers, no model is loaded* は、停止して起動したサーバーがまだ読み込みを必要としている場合の応答です。
3. **Convert to audio** で短いパートを 1 つ変換します。ウィンドウには作成中の断片が表示されます。その後、音声コンテナは停止しているはずで、`nvidia-smi` / `nvtop` / `rocm-smi` でカードが再び空いていることが確認できます。
4. モデル読み込み後の最初の呼び出しに時間がかかる場合は、エンジンのウォームアップです（1 回だけ発生し、最初の実行では 28 秒、その後は見られませんでした）。
<a id="troubleshooting"></a>

## トラブルシューティング

| 症状 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | サーバーは空の状態で起動している(起動時に一度だけモデルをアンロードする起動ラッパーがある)。リクエスト **Before 3** を追加する。 |
| 最初の数片は作られるが、その後サーバーが *Model not loaded* と返し、そのログに `Request received for /api/unload` が出る | Marginalia以外の何かがモデルをアンロードしている(拡張機能は `/api/unload` を一切呼ばない)。典型的には、コンテナ内の起動ラッパーが、サーバーの応答を待ってから「起動を軽くするため」にモデルをアンロードするものである。このラッパーの待ち時間は最初の一片のあとにしか終わらないことがある。シングルスレッドのサーバーがそのとき処理中で応答できなかったためで、アンロードはバッチの途中に当たってしまう(コンテナログに出る `urllib` のタイムアウトのトレースバックは、そのラッパーが待っている跡である)。compose ファイルで探すべきパターンは `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"` である。これを `command: ["python3", "server.py"]` に置き換える。起動だけならTurboはロードされた状態になり、アンロードは起動後にカードを空けておくためだけに存在していたもので、コントローラーが起動・停止するコンテナにはそれは不要である。誰がリクエストを送ったかを知るには、拡張機能のログ(*Admin → Extensions → Log*)を読む。そこには、バッチの前後に送られたすべてのリクエストが、そのアドレスと応答された時刻とともに、またピースの開始と終了の時刻とともに記録されている。バッチの実行中だとログに出ている時にアンロードが届いた場合や、リストにないアドレスから届いた場合は、拡張機能によるものではない。 |
| バッチが途中で止まる(拡張機能のログには *Cannot reach* と出る、またはサーバーのログがそこで終わっている)が、手動で再起動すると動く | サーバーが死んでいる。典型的には、メモリを使い切ったためにシステムによって強制終了されたもので(ホスト上の `dmesg \| grep -i oom`、数GBを使う `python3` プロセス)、拡張機能は自分でサーバーを再起動し(バッチの後のリクエスト、次に前のリクエストを送る)、そのピースを一度だけ作り直す。サーバーを戻せない場合、またはすぐにまた消えてしまう場合は、バッチの残りが即座に失敗し、ログにその理由が出る。メモリリークするサーバーがそこまで至らないようにするには、バックエンドで **Restart the server after (pieces)** を設定する(例えば100)。また、システムがサーバーだけを強制終了するように、コンテナに `mem_limit` を設定する。 |
| 変換がプロキシの応答によってすぐに失敗する | バッチの前に送られた最初のリクエストが失敗した(キーが間違っている、またはアンロードのルートが見つからない)。リストはそこで意図的に止まる。 |
| バッチが始まるとホストのメモリが不足する | 言語モデルがまだカード上に残っている。アンロードのリクエストが欠けているか、最初に来ていない。 |
| 日本語が意味不明な文字列として読まれる | サーバーのイメージに `pykakasi` がない。そのピースのためには、バックエンドも多言語版でなければならない。 |
| プロキシの背後でクリップのアップロードが失敗する | プロキシは multipart のボディ(`/upload_reference`)を変更せずに通さなければならない。LiteLLM のパススルーでは試していない。動作するようになるまでは、サーバー側の音声ファイル(`predefined:Name.wav`)を使う。 |
| 別のユーザーのバッチがあなたのモデルをアンロードしてしまう | 同じサーバーアドレスへのバッチは、Marginalia内部の一つのゲートを共有し、別の音声モデルのバッチは他が終わるまで待つ。音声サーバーと、例えば画像サーバーを同じカードで使う場合は、両方のバックエンドの **GPU (name)** に同じ名前を設定すると、互いを待つようになる(画像ジェネレーターも同様)。同じカードに二つのMarginalia環境がある場合、それらはゲートを共有しない。 |
