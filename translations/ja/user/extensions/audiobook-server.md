---
label: 音声サーバーの設定
order: 28
---

# Audiobook 用の音声サーバーの設定

[Audiobook](audiobook.md) 拡張機能には、HTTP で到達できる **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)、FastAPI、MIT ライセンス）が必要です。このページではサーバー側を扱います。ここに書かれている内容は、1台のマシン（AMD Radeon AI PRO R9700 32 GB、前段にすべて LiteLLM、`llama-server` はルーターモード）で試したものです。お使いのハードウェアやプロキシによっては、別の値が必要になるかもしれません。

<a id="which-setup-is-yours"></a>

## お使いの構成はどれか？

| お使いの状況 | 行うこと |
|---|---|
| 音声サーバーが**別のマシン**で動く、CPU で動く、または**言語モデルと並べて入る**余裕のあるカードで動く | サーバーを起動し、そのアドレスをバックエンドに入力し、**GPU ロックは選ばない**。ほかには何も必要ありません。 |
| 音声サーバーと言語モデルが**1枚のカードを共有**し、両方は収まらない | [GPU ロック](../administration/gpus.md)が必要です。その **acquire** の手順でカードを空け音声サーバーを起動し、**release** の手順で停止します。このページの残りの部分がそれを扱います。 |
| 言語モデルがクラウドモデルである | 空けるものはありません。音声サーバーは好きな場所で動かし、GPU ロックは選ばないでください。 |

つまり、**事前および事後のリクエストが必要なのは、2つのモデルが1枚の GPU を交互に使う場合だけです。** それが順番の切り替えを生み出します。プラグインは、個々のパートごとではなく、*バッチ*（1クリックで開始したすべて）の前後に一度ずつ、それを送ります。

<a id="1-the-speech-server"></a>

## 1. 音声サーバー

`GET /api/model-info` に応答するように、Chatterbox-TTS-Server をコンテナ（またはソースから）で起動します。重要な点は次のとおりです。

- **モデル。** Turbo（`chatterbox-turbo`）は拡張機能の既定値で、テストでは最も自然に聞こえました。多言語モデルが必要なのは、本の言語とは別の言語のテキストを読む場合だけです。モデルはサーバーの `config.yaml`（または `POST /save_settings` と `POST /restart_server`）で設定します。バックエンドの *Speech model* は、GPU ロックの acquire の手順のあとにサーバーが読み込んでいるモデルと同じでなければなりません。
- **日本語には、イメージに `pykakasi` が必要です。** これがないと、サーバーは *pykakasi not available - Japanese text processing skipped* とログに出力し、漢字を誤って読みます。Dockerfile の（`COPY . .` の前に）`RUN python3 -m pip install --no-cache-dir pykakasi` を追加してください。ヘブライ語、ロシア語、中国語のパッケージでも、同じように黙ってスキップされる問題があります。
- **声**は、サーバーの `reference_audio/` フォルダー内のファイルです（残るようにマウントしてください）。拡張機能は、バッチの前に、俳優のクリップを（`POST /upload_reference` によって）自分でアップロードします。名前はクリップのハッシュから作られます。サーバーには削除の呼び出しがないため、クリップはそこに溜まっていきます。
- サーバーには**認証がありません**。ポートを公開しないでください。プライベートネットワーク上にない場合は、前段にプロキシを置いてください。
- 別のものがオンデマンドで起動するコンテナには `restart: "no"` を使います。`restart: unless-stopped` は起動時に起動し、そのためカードを常時占有します（何も解放されません）。

バックエンド（*Settings → Extension Settings → Audiobook Settings*）で、そのアドレスを入力し、サーバーが同じマシンまたは自分のネットワーク上にある場合は *The server is on this machine or on my network* にチェックを入れ、**Test connection** を押します。望ましい結果は *the server answers, the model is loaded* です。
拡張機能は `/tts`（パラメーターの全セット：声、言語、exaggeration、CFG weight、temperature、速度、seed）と `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` を使って通信します。あなたとサーバーの間にプロキシがある場合、プロキシはこれらすべてを通す必要があります。

## Higgs Audio v3 in ComfyUI

音声モデルに **Higgs Audio v3 in ComfyUI** を指定したバックエンドは、Chatterbox-TTS-Server ではなく、[TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) のノードを備えた [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) と通信します。そのアドレスは ComfyUI のアドレスです（例：`http://gpu-server:8188`）。これは同じ AMD R9700 上の `yanwk/comfyui-boot:rocm` イメージで設定しました。

1. **スイートをインストールします。** ComfyUI の `custom_nodes` フォルダー（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）に入れます。その Python パッケージはコンテナ内に入ります。コンテナを作り直しても残すには、pip にボリューム上のフォルダーを与えます。コンテナの環境変数に `PIP_USER=true` を追加し、`./storage/local:/root/.local` をマウントしてください。
2. **pip にイメージの torch を置き換えさせないでください**（CUDA ビルドは ROCm 版を置き換えてしまいます）。インストール済みのバージョンをファイルに書き出し、それを制約として指定してインストールしてください。
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   スイートの `install.py` を丸ごと実行しないでください。torch を再インストールする可能性があります。`librosa` はその `requirements.txt` に含まれておらず、これがないとスイートは読み込まれません。
3. **モデル**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（約 8 GB）を入れるか、初回にスイートにダウンロードさせてください。グラフィックカードの約 8 GB を必要とします。
4. バックエンドの **Test connection** は、ComfyUI とノードがあれば、ノードのモデルとともに *the server answers* と表示します。

拡張機能はこれを次のように使います。キャストのクリップを ComfyUI の入力フォルダー（`POST /upload/image`、クリップから作った名前で）にアップロードし、`GET /object_info` でスイートの声とクリップの一覧を取得し、パートごとに独自のワークフローを `/prompt` へ投稿し、`/history` で待機し、`/view` から音声を取得します。Marginalia 側に ffmpeg があれば flac で受け取り、最後の単語のあとをトリムし、フェードアウトして mp3 または wav で保存します。ffmpeg がなければ、ComfyUI の mp3 をトリムせずにそのまま使います（この場合 wav での保存は機能しません）。**パートごとに、読み込み済みのモデルで `POST /free` を `{"free_memory": true}` とともに呼び出します。** R9700 では、読み込み済みのモデルで連続して3つ目のパートを処理するとグラフィックカードがクラッシュしました（`HSA_STATUS_ERROR_EXCEPTION`、ComfyUI が終了します）。パートの間にメモリを空にすると、モデルは読み込まれたままになり、この問題を避けられます。そのかわりパート1つにつき約3秒かかります。ComfyUI は、音声サーバーと同じ種類のリクエスト（[リクエスト](#the-requests)）を使って、バッチの前後に起動・停止されます。

**AMD カードでは fp32 を使ってください。** 精度 `auto`（ROCm 7.2 の R9700 では bf16）では、多くのパートが意味不明な音声になったり、効果音になったり（「叩く」という一文が11回のノックになった）、違う声になったりしました。シードを変えても、うまくいくかどうかが変わるだけでした。同じパートを fp32 で行うと、一語一句正確でした。バックエンドの **Precision** は既定で fp32 で、カードの約 16 GB を必要とします。スイートの Granite 音声認識も bf16 で同じ問題があり（2回目の認識から意味不明になる）、そのため拡張機能はこれも float32 で実行します。

**クリップのクリーンアップ**（[Cleanup of clips](audiobook.md#cleanup-of-clips)）は、同じスイートのさらに2つのノード、`VocalRemovalNode`（分離モデル）と `VoiceFixerNode` を、ComfyUI の `LoadAudio` と `SaveAudio` とともに使います。ほかにインストールするものはありません。スイートはモデルを初回使用時にダウンロードします（MelBand Roformer のモデルはそれぞれ約 900 MB です）。R9700 では、MelBand モデル（vocals、denoise）と voice fixer が動作し、16秒のクリップに約25秒かかりました。UVR モデルは動作しませんでした。`UVR-DeNoise` はダウンロードに失敗し（スイートのアドレスが 404 を返しました）、`UVR-DeEcho-DeReverb` は `module 'samplerate' has no attribute 'resample'` で失敗しました（コンテナ内の Python パッケージ `samplerate` が、スイートが期待するものではありません）。既定のクリーンアップは、動作したモデルのみを使用します。

スイートの **Fish Audio S2 Pro** エンジンは、そのカードでは動作しませんでした。音声が無効な数値（NaN）として出力されました。Higgs Audio v3 の重みは、研究用および非商用ライセンスの下にあります。
<a id="one-gpu-for-the-model-and-the-speech"></a>

## モデルと音声で1枚のGPUを共有する

言語モデルを載せたカードには、音声モデルを載せる余地がありません。管理者は一度だけ [Admin → GPUs](../administration/gpus.md) で入れ替えの設定をします。GPUと、そこに読み込めるもの一つにつきロックを一つ用意し、言語モデルのAI設定と音声バックエンドでそのロックを選びます。計画はどのプロキシでも同じです:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

音声バックエンドのバッチ（またはサンプル）は、GPUに言語モデルが載っている場合に音声ロックの取得ステップを実行し、その後すべての断片を処理します。GPUは処理後も音声サーバーを載せたままにします。言語モデルへの次のリクエストは、バッチの完了を待ち、音声ロックの解放ステップ（サーバーの停止）を実行してから続行します。連続する複数のバッチとサンプルは、入れ替えのコストを一度だけ払います。

**順序は変えられません。** 音声コンテナを起動したとき、言語モデルがカードを占有したままだと、テストではホストのメモリが埋まりました（RAM 30 GB、スワップ、その後 OOM killer が他のサービスを停止させました）。そのため、アンロードが**必ず最初**でなければなりません。最初に失敗した取得ステップがリストを終了させるので、空いていないカードに対して何も起動されません。解放ステップが失敗した場合、次のロックは読み込まれません（GPUがまだ満杯の可能性があります）し、ジョブはエラーで失敗します。解放ステップは、そのうちのどれか一つに何が起きても、すべて実行されます。

**1つのComfyUI上のHiggs、音声認識、クリップのクリーンアップ**は、それぞれ専用のロックを持ちます。3つとも同じステップです（言語モデルのアンロード、ComfyUIの起動。解放: ComfyUIの停止）。ComfyUIはアンロードしたモデルをサーバーのメモリに保持します（fp32のHiggsは約16 GB）。その隣で音声認識を読み込むと、テストではサーバーのメモリが不足しました。3つのロックを使うと、切り替えのたびにComfyUIが再起動するため、読み込まれるのは常に1つだけになります。

なぜサーバー内でモデルをアンロードせず、*コンテナを停止する*のか：サーバー内でのアンロードでは、多数のモデル入れ替えの後にカードの約1 GBが占有されたままになりました。コンテナを停止すれば、すべて解放されます。

<a id="the-steps"></a>

### ステップ

各ステップは、メソッド、アドレス、ヘッダー、ボディ、タイムアウト、そして後の待機時間（秒）で構成されます。ヘッダーは暗号化して保存されるため、プロキシのキーをそこに書き込めます（`Authorization: Bearer ...`）。LiteLLMを前段に置く場合、音声ロックのステップは次のとおりです（*Before 1* から *3* は取得ステップ、*After 1* は解放ステップ）:

| | メソッドとアドレス | ヘッダー | ボディ | 役割 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 言語モデルをアンロードします。フック自体が応答し、1〜2秒かかります。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 音声コンテナを起動し、サーバーの準備ができたら応答します（約13秒）。 |
| Before 3 (サーバーがモデルなしで起動する場合のみ) | `POST http://litellm:4000/gpu/tts/load` | | | モデルを読み込みます（8〜14秒、初回ダウンロード時は50秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | コンテナを停止します（1秒未満）。 |

**Higgs** バックエンドでは、*Before 2* は `POST http://litellm:4000/gpu/comfy/start` です（ComfyUIの起動後に応答し、約15秒かかります。モデルは最初の断片とともに読み込まれます）。*Before 3* はなく、*After 1* は `POST http://litellm:4000/gpu/comfy/stop` です。2つのパススルーは `tools/gpu-control` のREADMEにあります。

言語モデルは自動的に戻ります。次の通常のリクエストでルーターがモデルを読み込みます（大きなモデルでは約1分。[プロンプトキャッシュフック](../prompt-cache.md)を使うと、セッションのキャッシュが復元されます）。そのため、バッチ後に言語モデル用の追加リクエストは必要ありません。

LiteLLMを使わない場合、同じリクエストはコンテナを起動・停止する仕組みに直接送られます。守るべき規則は上記の順序だけです。

<a id="what-answers-those-addresses"></a>

### これらのアドレスに応答するもの

- **`X-Llama-Unload`** はLiteLLMのフック `gpu_unload.py` が応答します。そのコードは [別のモデルのためにGPUを解放する](../prompt-cache.md#freeing-the-gpu-for-another-model) で説明されています（キャッシュフックがなくても動作します）。ルート `openai/*` を使う場合、ボディ内のモデル名は `openai/` で始まる必要があります。別の名前だと、モデルがアンロードされた*後に* 400 が返されます。
- **`/gpu/tts/start`、`/stop`、`/load`** は、リポジトリの小さなコントローラー `tools/gpu-control` へのLiteLLMの**パススルーエンドポイント**です。コントローラーは名前でコンテナを起動・停止します。そのREADMEには、**Docker socket proxy** を含むcomposeファイルと、間違えやすい設定（`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**）が記載されています（コントローラーがコンテナの起動・停止だけを行い、それ以外は何もできないようにするためのものです。ソケット自体はホスト上のrootです）。LiteLLMでは:

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

  （コンテナ名は `chatterbox` で、`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` はコントローラーが待つべき対象を指定します。コントローラーのトークンは `.env` とLiteLLMの環境変数にのみ置かれ、Marginaliaが持つのはLiteLLMのキーだけです）。
- **すべてのコンテナとLiteLLMは同じDockerネットワークを共有します**（`docker network create ai`、一度だけ実行）。`networks: [default, ai]` と外部ネットワーク `ai` を各composeファイルに記入してください。手作業で行った `docker network connect` は、コンテナが再作成されると失われます。

<a id="checking-it"></a>

## 動作確認

1. `curl http://<speech server>/api/model-info` が応答します（起動後に `loaded` が true になります）。
2. バックエンドの *Test connection* で、モデルが読み込まれていると表示されること。GPUロックだけがサーバーを起動する場合は、ロックを選択した状態でテストしてください。*サーバーは応答するが、モデルは読み込まれていない* は、停止後に再起動され、まだ読み込みが必要なサーバーの応答です。
3. **Convert to audio** で短い部分を一つ変換します。ウィンドウに断片の生成状況が表示されます。その後、音声コンテナは停止しているはずで、`nvidia-smi` / `nvtop` / `rocm-smi` でカードが再び空いていることが確認できます。
4. モデル読み込み後の最初の呼び出しが長い場合、それはエンジンのウォームアップです（一度きりで、初回実行時は28秒。以後は見られません）。
<a id="troubleshooting"></a>

## トラブルシューティング

| 症状 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | サーバーはモデルを読み込まない状態で起動しています（起動時に一度だけモデルをアンロードするスタートラッパーが原因です）。ロックの取得ステップに **Before 3** を追加してください。 |
| 最初の断片は作られるが、その後サーバーが *Model not loaded* と応答し、ログに `Request received for /api/unload` が出る | Marginalia 以外のものがモデルをアンロードしています（拡張機能が `/api/unload` を呼び出すことはありません）。多くの場合、コンテナ内の起動ラッパーが「起動を軽くするため」にサーバーの応答を待ってからモデルをアンロードしています。シングルスレッドのサーバーが最初の断片の処理で忙しかったため、この待機が終わるのは最初の断片の後になり、アンロードがバッチの途中で入ってしまいます。コンテナのログにある `urllib` のタイムアウトのトレースバックは、このラッパーが待機している様子です。確認すべきパターンは Compose ファイル内の `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"` です。これを `command: ["python3", "server.py"]` に置き換えてください。起動だけであれば Turbo は読み込まれた状態になります。アンロードは起動後に GPU のカードを空けておくためだけのものであり、コントローラーが起動と停止を行うコンテナには不要です。送信元を特定するには拡張機能のログ（*Admin → Extensions → Log*）を開いてください。このログには、バッチの前後に届いたすべてのリクエストが、そのアドレスと応答された時刻とともに記録されています。また、断片の処理の開始と終了も記録されています。GPU ロックの各ステップはアプリケーションのログにあります。バッチの実行中のログが示している時点で届いたアンロードや、リストにないアドレスからのアンロードは、拡張機能によるものではありません。 |
| バッチが途中で止まる（拡張機能のログに *Cannot reach* と出る、またはサーバーのログが途中で終わっている）が、手動で再起動すると動く | サーバーが停止しています。多くの場合、メモリを使い切ったためシステムに強制終了されています（ホストで `dmesg \| grep -i oom` を確認すると、数 GB を使う `python3` プロセスが見つかります）。その場合、拡張機能が自分でサーバーを再起動します（バッチ後のリクエスト、GPU ロックの解放と取得のステップ、その後にバッチ前のリクエストを行います）。そして断片の作成を一度だけやり直します。サーバーを復帰させられない場合、またはすぐにまた停止した場合は、バッチの残りが直ちに失敗し、ログに理由が出ます。リークするサーバーがそこまで至らないようにするには、バックエンドで **Restart the server after (pieces)** を設定してください（例：100）。また、コンテナに `mem_limit` を設定すると、システムが強制終了するのはそのサーバーだけになります。 |
| プロキシの応答により変換が直ちに失敗する | GPU ロックの最初の取得ステップが失敗しています（キーが誤っている、またはアンロードのルートが見つからない）。リストはそこで意図的に止まります。 |
| バッチの開始時にホストのメモリが不足する | 言語モデルがまだカードに載っています。音声のロックにアンロードのステップがないか、先頭にないか、または言語モデルの AI に同じ GPU のロックがありません。 |
| 日本語が意味不明な文字として読まれる | サーバーのイメージに `pykakasi` がありません。その断片に対しては、バックエンドも多言語版でなければなりません。 |
| プロキシの背後で音声クリップのアップロードが失敗する | プロキシは multipart の本文（`/upload_reference`）を変更せずに通す必要があります。LiteLLM のパススルーでは未検証です。動作するようになるまでは、サーバーの音声ファイル（`predefined:Name.wav`）を使ってください。 |
| 別のユーザーのバッチが自分のモデルをアンロードしてしまう | 同じカード上のすべてのバックエンドと AI は、同じ [GPU](../administration/gpus.md) の名前のロックを指定しなければなりません。そうして初めて順番に使われます。ロックのないバックエンドは、他の誰も待ちません。同じカード上の二つの Marginalia のインストールは GPU を共有しません。 |
