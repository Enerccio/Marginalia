---
label: 音声サーバーのセットアップ
order: 28
---

# オーディオブック用の音声サーバーのセットアップ

[オーディオブック](audiobook.md)拡張機能は、HTTP で接続できる **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)、FastAPI、MITライセンス）を必要とします。このページはサーバー側について説明します。ここで示す構成は1台のマシン（32 GB の AMD Radeon AI PRO R9700、すべての前段にある LiteLLM、ルーターモードの `llama-server`）で試したものです。お使いのハードウェアやプロキシによっては、別の値が必要になる場合があります。

<a id="which-setup-is-yours"></a>

## お使いの構成はどれですか？

| 状況 | 行うこと |
|---|---|
| 音声サーバーが**別のマシン**で動いている、CPU で動かしている、または**言語モデルと並べて**入るカードで動かしている | サーバーを起動し、そのアドレスをバックエンドに設定して、**バッチの前後のリクエストは空のままにします**。ほかに必要なことはありません。 |
| 音声サーバーと言語モデルが**1枚のカードを共有していて、両方は収まらない** | バッチの前後のリクエストが必要です。**前**はカードを空けて音声サーバーを起動し、**後**はそれを停止します。以降はこのページの内容です。 |
| 言語モデルがクラウドモデルである | 空ける必要はありません。音声サーバーは好きな場所で動かし、リクエストは空のままにします。 |

つまり、**バッチの前後のリクエストが必要になるのは、2つのモデルが1つの GPU を交互に使う場合だけです。** これがその交代を実現します。プラグインは、一回のクリックで開始したすべて（*バッチ*）の前後で一度ずつ、それらのリクエストを送信します。個々のパートの前後ではありません。

<a id="1-the-speech-server"></a>

## 1. 音声サーバー

`GET /api/model-info` が応答するように、Chatterbox-TTS-Server をコンテナ（またはソースから）で起動してください。重要な点は次のとおりです:

- **モデル。** Turbo（`chatterbox-turbo`）は拡張機能の既定値で、テストでは最も良い音質でした。多言語モデルは、本のテキストとは別の言語のテキストにだけ必要です。モデルはサーバーの `config.yaml` で設定します（または `POST /save_settings` と `POST /restart_server`）。バックエンドの *Speech model* は、バッチの前のリクエストの後にサーバーが読み込んでいるモデルと一致している必要があります。
- **日本語には、イメージに `pykakasi` が必要です。** これがないと、サーバーはログに *pykakasi not available - Japanese text processing skipped* と出力し、漢字を誤って読みます。Dockerfile に `RUN python3 -m pip install --no-cache-dir pykakasi` を（`COPY . .` の前に）追加してください。同じ無音のスキップは、ヘブライ語、ロシア語、中国語のパッケージにも存在します。
- **声**は、サーバーの `reference_audio/` フォルダー内のファイルです（消えないようにマウントしてください）。拡張機能は、バッチの前に登場人物のクリップを自分で（`POST /upload_reference`）アップロードします。名前はクリップのハッシュから作られます。サーバーには削除の呼び出しがないため、クリップは溜まっていきます。
- サーバーには**認証がありません**。ポートを公開しないでください。プライベートネットワーク上にない場合は、前段にプロキシを置いてください。
- 何か別のものがオンデマンドで起動するコンテナには `restart: "no"` を使います。`restart: unless-stopped` にすると、起動時に自動的に立ち上がり、カードを無駄に占有します。

バックエンド（*Settings → Extension Settings → Audiobook Settings*）で、サーバーのアドレスを入力します。サーバーが同じマシンまたは自分のネットワーク上にある場合は *The server is on this machine or on my network* にチェックを入れ、**Test connection** を押します。目指すのは *the server answers, the model is loaded* の表示です。

拡張機能は `/tts`（パラメーター一式：voice、language、exaggeration、CFG weight、temperature、speed、seed）と、`GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` に接続します。プロキシがあなたとサーバーの間にある場合は、これらすべてを通す必要があります。

<a id="one-gpu-for-the-model-and-the-speech"></a>

## モデルと音声のための1つの GPU

言語モデルを載せたカードには、音声モデルを置く余地がありません。手順はどのプロキシでも同じです:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**この順序は変更できません。** 言語モデルがカードを占有している状態で音声コンテナを起動すると、テストではホストのメモリが埋まりました（RAM 30 GB、スワップ、その後 OOM killer が他のサービスを停止させました）。アンロードは**必ず最初に**行う必要があり、最初に失敗したリクエストでリストは終了するため、満杯のカードで何かが起動することはありません。*バッチの後の*リクエストは、そのうちの一つに何が起ころうとも、すべて実行されます。

サーバー内でモデルをアンロードするのではなく、コンテナを*停止*する理由：サーバー内のアンロードでは、多数のモデル切り替えの後にもカードの約 1 GB が使われたままになりました。コンテナを停止すればすべて解放されます。

<a id="the-requests"></a>

### リクエスト

リストの各リクエストは、メソッド、アドレス、ヘッダー、ボディ、そしてその後の待機時間（秒）からなります。キーはヘッダーに書かないでください（フォームはヘッダーを入力どおりに表示します）。代わりに **Key of the requests around a batch** に入力するか、空のままにすると、バックエンドの *Api key* が送られます。自分専用のヘッダーを持たない以下のすべてのリクエストには、`Authorization: Bearer` として付けて送られます。すべての前段に LiteLLM があり、キーが1つだけの場合、リストは次のとおりです:

| | メソッドとアドレス | ヘッダー | ボディ | 動作 |
|---|---|---|---|---|
| 前 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 言語モデルをアンロードします。フック自体が応答し、1〜2 秒かかります。 |
| 前 2 | `POST http://litellm:4000/gpu/tts/start` | | | 音声コンテナを起動し、サーバーの準備ができるまで待って応答します（約 13 秒）。 |
| 前 3（サーバーがモデルなしで起動する場合のみ） | `POST http://litellm:4000/gpu/tts/load` | | | モデルを読み込みます（8〜14 秒、初回のダウンロード時は 50 秒）。 |
| 後 1 | `POST http://litellm:4000/gpu/tts/stop` | | | コンテナを停止します（1 秒未満）。 |

言語モデルは自動的に戻ります。次の通常のリクエストでルーターがそれを読み込みます（大きなモデルでは約 1 分。[プロンプトキャッシュフック](../prompt-cache.md)を使うと、セッションのキャッシュが復元されます）。そのため、バッチの後に言語モデルのためのリクエストは必要ありません。

LiteLLM を使わない場合も、同じリクエストを、コンテナを起動・停止する仕組みに送ってください。守るべきルールは上記の順序だけです。

<a id="what-answers-those-addresses"></a>

### これらのアドレスに応答するもの

- **`X-Llama-Unload`** には、LiteLLM のフック `gpu_unload.py` が応答します。このフックは、そのコードとともに [別のモデルのためにGPUを空ける](../prompt-cache.md#freeing-the-gpu-for-another-model) で説明しています（キャッシュフックがなくても動作します）。ルート `openai/*` を使う場合、ボディのモデル名は `openai/` で始まる必要があります。別の名前だと、モデルのアンロード*後*に 400 が返されます。
- **`/gpu/tts/start`、`/stop`、`/load`** は、LiteLLM の **パススルーエンドポイント**で、リポジトリの小さなコントローラー `tools/gpu-control` に転送されます。このコントローラーは名前でコンテナを起動・停止します。その README には、**Docker ソケットプロキシ**を使った compose ファイル（コントローラーはコンテナの起動と停止だけができ、それ以外は何もできないようにするため。ソケット自体はホスト上の root 権限です）と、間違えやすい設定が記載されています：`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。LiteLLM では次のように設定します:

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

  （コンテナ名は `chatterbox` で、`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` によって、コントローラーが待つべき対象を指定します。コントローラーのトークンは、その `.env` と LiteLLM の環境変数に置かれ、Marginalia が持つのは LiteLLM のキーだけです。）
- **すべてのコンテナと LiteLLM は、1つの Docker ネットワークを共有します**（`docker network create ai` を一度実行）。`networks: [default, ai]` と外部ネットワーク `ai` を各 compose ファイルに記述してください。手作業の `docker network connect` は、コンテナを作り直すと失われます。

<a id="checking-it"></a>

## 動作確認

1. `curl http://<音声サーバー>/api/model-info` が応答すること（起動後は `loaded` が true）。
2. バックエンドの *Test connection* で、モデルが読み込まれていると表示されること。バッチの前のリクエストだけがサーバーを起動する場合は、リストを設定した状態でテストしてください。*the server answers, no model is loaded* は、停止して起動したがまだ読み込みが必要なサーバーからの応答です。
3. **Convert to audio** で短いパートを一つ変換します。ウィンドウには作成中の断片が表示されます。その後、音声コンテナは停止している必要があり、`nvidia-smi` / `nvtop` / `rocm-smi` でカードが再び空いていることが確認できるはずです。
4. モデル読み込み後の最初の呼び出しに時間がかかる場合は、エンジンのウォームアップです（一度だけで、最初の実行では 28 秒かかりましたが、その後は見られませんでした）。
<a id="troubleshooting"></a>

## トラブルシューティング

| 症状 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | サーバーが空の状態で起動しています（起動時に一度だけモデルをアンロードするラッパーがあります）。リクエスト **Before 3** を追加してください。 |
| 最初のいくつかのピースは作られるが、その後サーバーが *Model not loaded* と返し、ログに `Request received for /api/unload` が表示される | Marginalia 以外の何かがモデルをアンロードしています（拡張機能は `/api/unload` を一切呼び出しません）。典型的には、コンテナ内の起動ラッパーが、サーバーの応答を待ってから「起動を軽くするため」にモデルをアンロードするものです。その待機は、シングルスレッドのサーバーが処理中で忙しかったために最初のピースの後まで終わらず、アンロードがバッチの途中に入ってしまいます（コンテナログの `urllib` のタイムアウトのトレースバックは、そのラッパーが待機している証拠です）。compose ファイルで探すパターンは `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"` です。これを `command: ["python3", "server.py"]` に置き換えてください。起動だけで Turbo はロードされた状態になりますし、アンロードは起動後にカードを空けておくためだけのものなので、コントローラーが起動と停止を行うコンテナには不要です。誰がリクエストを送ったかは、拡張機能のログ（*Admin → Extensions → Log*）で確認できます。このログには、バッチの前後のすべてのリクエストが、そのアドレスと応答された時刻とともに記録されており、ピースの開始と終了の時刻も分かります。ログがバッチの実行中を示している時に来たアンロード、またはリストにないアドレスからのアンロードは、拡張機能のものではありません。 |
| バッチが途中で止まる（拡張機能のログに *Cannot reach* と出る、またはサーバーのログがそこで途切れている）が、手動で再起動すると動く | サーバーが死んでいます。典型的には、メモリを使い切ったためにシステムに殺されたものです（ホストで `dmesg \| grep -i oom` を実行すると、数GBの `python3` プロセスが見つかります）。拡張機能は自分でサーバーを再起動し（バッチの後のリクエスト、次に前のリクエスト）、ピースを一度だけ作り直します。サーバーを戻せない場合、または直後にまた消えた場合は、バッチの残りが直ちに失敗し、ログにその理由が出ます。メモリリークするサーバーがそこまで到達しないようにするには、バックエンドで **Restart the server after (pieces)**（例：100）を設定し、コンテナに `mem_limit` を設定して、システムが殺すのはそのコンテナだけになるようにしてください。 |
| 変換がプロキシの応答ですぐに失敗する | バッチの前の最初のリクエストが失敗しています（キーが誤っている、アンロードのルートが見つからない）。リストはそこで意図的に止まっています。 |
| バッチの開始時にホストのメモリが不足する | 言語モデルがまだカードに載っています。アンロードのリクエストが無い、または最初ではありません。 |
| 日本語が意味不明に読まれる | サーバーのイメージに `pykakasi` がありません。そのピースには、バックエンドも多言語版でなければなりません。 |
| プロキシの背後でクリップのアップロードが失敗する | プロキシは multipart の本文（`/upload_reference`）を変更せずに通す必要があります。LiteLLM のパススルーではテストしていません。動くようになるまでは、サーバーの音声ファイル（`predefined:Name.wav`）を使ってください。 |
| 別のユーザーのバッチがあなたのモデルをアンロードしてしまう | 同じサーバーアドレスへのバッチは Marginalia 内のゲートを共有し、別の音声モデルのバッチは、他が終わるまで待機します。同じカードに音声サーバーと、例えば画像サーバーがある場合は、両方のバックエンドの **GPU (name)** に同じ名前を設定すると、互いを待つようになります（画像生成も同様です）。同じカードに Marginalia を二つインストールしている場合は、ゲートを共有しません。 |
