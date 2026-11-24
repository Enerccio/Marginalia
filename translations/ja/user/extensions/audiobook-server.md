---
label: 音声サーバーの設定
order: 28
---

# Audiobook のための音声サーバーの設定

[Audiobook](audiobook.md) 拡張機能は、HTTP で到達できる **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)、FastAPI、MIT ライセンス）を必要とします。このページはサーバー側について説明します。ここに記載の構成は 1 台のマシン（AMD Radeon AI PRO R9700、32 GB、すべての前段に LiteLLM、`llama-server` はルーターモード）で試したものです。お使いのハードウェアやプロキシでは別の値が必要になるかもしれません。

<a id="which-setup-is-yours"></a>

## お使いの構成はどれですか？

| 状況 | 行うこと |
|---|---|
| 音声サーバーが **別のマシン** 上、CPU 上、または **言語モデルと並べて入る余裕のあるカード** 上で動く | サーバーを起動し、そのアドレスをバックエンドに設定し、**GPU ロックは選ばない**。ほかに必要なものはありません。 |
| 音声サーバーと言語モデルが **1 枚のカードを共有し、両方は収まらない** | [GPU ロック](../administration/gpus.md) が必要です。その **acquire** の手順でカードを空け音声サーバーを起動し、**release** の手順でそれを停止します。このページの残りの部分です。 |
| 言語モデルがクラウドモデルである | 空けるものはありません。音声サーバーはお好きな場所で動かし、GPU ロックは選ばないでください。 |

要約すると、**前処理（pre）と後処理（post）のリクエストが必要なのは、2 つのモデルが 1 枚の GPU を交互に使う場合だけです。** これがその交互の切り替えを行います。プラグインは、一括処理（ワンクリックで開始したものすべて）である *バッチ* の前後に一度ずつこれを送り、パーツごとには送りません。

<a id="1-the-speech-server"></a>

## 1. 音声サーバー

Chatterbox-TTS-Server をコンテナ内（またはソースから）で起動し、`GET /api/model-info` に応答するようにします。重要な点は次のとおりです。

- **モデル。** Turbo（`chatterbox-turbo`）が拡張機能の既定で、テストでは最も良い音声でした。多言語モデルは、本とは別の言語のテキストにのみ必要です。モデルはサーバーの `config.yaml`（または `POST /save_settings` と `POST /restart_server`）で設定します。バックエンドの *Speech model* は、GPU ロックの acquire の手順の後にサーバーが読み込んだモデルと一致している必要があります。
- **日本語には、イメージに `pykakasi` が必要です。** これがないと、サーバーは *pykakasi not available - Japanese text processing skipped* とログに出し、漢字を誤って読みます。`COPY . .` より前に、その Dockerfile に `RUN python3 -m pip install --no-cache-dir pykakasi` を追加してください。ヘブライ語、ロシア語、中国語のパッケージにも同じように黙ってスキップされる問題があります。
- **声** は、サーバーの `reference_audio/` フォルダー内のファイルです（残るようにマウントしてください）。拡張機能は、バッチの前にあなたのキャストのクリップを自分でアップロードします（`POST /upload_reference`）。その名前はクリップのハッシュから作られます。サーバーには削除の呼び出しがないため、クリップは溜まっていきます。
- サーバーには**認証がありません**。ポートを公開しないでください。プライベートネットワーク上にない場合は、前段にプロキシを置いてください。
- 別のものが必要に応じて起動するコンテナには `restart: "no"` を使います。`restart: unless-stopped` は起動時にコンテナを立ち上げ、カードを占有し続けます。

バックエンド（*Settings → Extension Settings → Audiobook Settings*）でそのアドレスを入力し、サーバーが同じマシンまたは自分のネットワーク上にある場合は *The server is on this machine or on my network* にチェックを入れ、**Test connection** を押します。目的の表示は *the server answers, the model is loaded* です。

拡張機能は `/tts`（パラメーター一式：voice、language、exaggeration、CFG weight、temperature、speed、seed）と `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` に話しかけます。あなたとサーバーの間にプロキシがある場合、そのプロキシはこれらすべてを通す必要があります。

<a id="higgs-audio-v3-in-comfyui"></a>

## ComfyUI の Higgs Audio v3

音声モデルに **Higgs Audio v3 in ComfyUI** を指定したバックエンドは、Chatterbox-TTS-Server ではなく、[TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) のノードを備えた [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) と通信します。そのアドレスは ComfyUI のアドレスです（例：`http://gpu-server:8188`）。これは同じ AMD R9700 上で、`yanwk/comfyui-boot:rocm` イメージに設定されました。

1. **TTS Audio Suite をインストール** します。ComfyUI の `custom_nodes` フォルダー（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）に入れます。その Python パッケージはコンテナ内に入ります。コンテナを作り直しても残すために、pip にボリューム上のフォルダーを与えます。コンテナの環境変数に `PIP_USER=true` を追加し、`./storage/local:/root/.local` をマウントします。
2. **イメージの torch を pip に置き換えさせないでください**（CUDA ビルドは ROCm のものを置き換えてしまいます）。インストール済みのバージョンをファイルに書き出し、それを制約として指定してインストールします。
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   このスイートの `install.py` を丸ごと実行しないでください。torch を入れ直すおそれがあります。`librosa` はその `requirements.txt` に含まれておらず、これがないとスイートは読み込めません。
3. **モデル**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（約 8 GB）を使うか、初回にスイートにダウンロードさせます。グラフィックカードの約 8 GB を必要とします。
4. ComfyUI とノードがあれば、バックエンドの **Test connection** は、そのノードのモデルとともに *the server answers* と表示します。

拡張機能は次のことを行います。キャストのクリップを ComfyUI の入力フォルダー（`POST /upload/image`、クリップから作られた名前で）にアップロードし、`GET /object_info` でスイートの声とクリップを一覧にし、パーツごとに独自のワークフローを `/prompt` へ投稿し、`/history` を待ち、`/view` から音声を取得します（ワークフローは、TTS ノードの生成情報をコアの `PreviewAny` ノードにも渡します。スイートはエラーの際に 1 秒の無音と成功した実行を返すため、この情報だけがパーツの失敗を示し、そのパーツは保持されずに理由とともに失敗します）。Marginalia 側に ffmpeg がある場合は flac として受け取り、最後の単語の後を切り詰め、フェードアウトしてから mp3 または wav で保存します。ffmpeg がない場合は、ComfyUI の mp3 を切り詰めずにそのまま使います（その場合、wav 保存は機能しません）。**パーツごとに、拡張機能は `POST /free` を `{"free_memory": true}` とともに呼び出します。** R9700 では、読み込み済みのモデルで連続して 3 つ目のパーツを生成するとグラフィックカードがクラッシュしました（`HSA_STATUS_ERROR_EXCEPTION`、ComfyUI が終了）。パーツの間でメモリを解放すると、モデルは読み込まれたままになり、これを回避できます。コストはパーツあたり約 3 秒です。ComfyUI はバッチの前後で、音声サーバーと同じ種類のリクエスト（[リクエスト](#the-requests)）によって起動・停止されます。

**AMD カードでは fp32 を使ってください。** 精度 `auto`（ROCm 7.2 の R9700 では bf16）では、多くのパーツが意味不明な音声になったり、効果音になったり（ドアを叩く音についての台詞が 11 回のノック音になった）、違う声になったりしました。シードを変えても運が変わるだけでした。同じパーツを fp32 で生成すると、単語まで正確でした。バックエンドの **Precision** は既定で fp32 で、カードの約 16 GB を必要とします。スイートの Granite 音声認識も bf16 で同じ問題があるため（2 回目の認識から意味不明な出力）、拡張機能はこれも float32 で実行します。

**クリップのクリーンアップ**（[クリップのクリーンアップ](audiobook.md#cleanup-of-clips)）は、同じスイートのさらに 2 つのノード `VocalRemovalNode`（分離モデル）と `VoiceFixerNode` を、ComfyUI の `LoadAudio` と `SaveAudio` とともに使います。ほかのノードパックは必要ありません。スイートは初回使用時にモデルをダウンロードします（MelBand Roformer のモデルはそれぞれ約 900 MB）。R9700 では、MelBand のモデル（vocals、denoise）と voice fixer が動作し、16 秒のクリップで約 25 秒でした。UVR モデルは最初は動作しませんでした。

- `UVR-DeEcho-DeReverb`（*Remove echo* と *Film or game clip* で使用）は `module 'samplerate' has no attribute 'resample'` で失敗しました。コンテナには `samplerate` 0.1.0 が入っていましたが、これはシステムライブラリ `libsamplerate` を必要とするラッパーで、イメージにはそれがありません。現在のパッケージは独自にそれを持っているため、torch の固定バージョンとともにインストールし、ComfyUI を再起動してください：
  ```sh
  PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install --user 'samplerate>=0.2.1'
  ```
  その後、16 秒のクリップのリバーブ除去は約 5 秒かかりました。
- `UVR-DeNoise` はダウンロードに失敗しました（スイートのアドレスが 404 を返しました）。使うには、ファイルを手作業で `models/TTS/UVR/` に入れてください。MelBand の denoise モデルはこの手順を必要とせず、既定のクリーンアップに含まれるものです。

**Fish Audio S2 Pro** エンジンは、そのカードでは動作しませんでした。出力音声が無効な数値（NaN）になりました。Higgs Audio v3 の重みは、研究・非商用ライセンスの下にあります。

---
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 1枚のGPUでモデルと音声を共有する

言語モデルを載せたカードには、音声モデルを置く余地がありません。管理者は一度だけ [Admin → GPUs](../administration/gpus.md) で切り替えを設定します。読み込めるものごとにGPUとロックを1つずつ用意し、言語モデルのAIと音声バックエンドでロックを選びます。この方式はどのプロキシでも同じです。

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

音声バックエンドのバッチ（またはサンプル）は、GPUに言語モデルが載っているとき、音声ロックの取得ステップを実行してから、すべての断片を処理します。その後もGPUには音声サーバーが載ったままです。言語モデルへの次のリクエストは、バッチの完了を待ってから音声ロックの解放ステップ（サーバーの停止）を実行し、処理を続けます。連続するバッチやサンプルでは、切り替えの手間は一度だけで済みます。

**手順の順序は変更できません。** 言語モデルがカードを占有している状態で音声コンテナを起動すると、テストではホストのメモリが埋まりました（RAM 30 GB、スワップ、その後OOMキラーが他のサービスを停止させました）。そのため、アンロードは**必ず先に**行う必要があります。最初に失敗した取得ステップでリストは終わるので、空いていないカードの上で何かが起動することはありません。解放ステップが失敗した場合は、次のロックを読み込みません（GPUがまだ埋まっている可能性があるため）。そしてジョブはエラーで失敗します。解放ステップは、どれか1つに問題が起きても、すべて実行されます。

**Higgs、音声認識、クリップのクリーンアップは、1つのComfyUI上でそれぞれ専用のロック**を持ちます。3つのロックは同じステップを使います（取得: 言語モデルをアンロードし、ComfyUIを起動。解放: ComfyUIを停止）。ComfyUIは、アンロードしたモデルをサーバーのメモリに残します（fp32のHiggsで約16 GB）。テストでは、その隣に音声認識を読み込むとサーバーのメモリが不足しました。3つのロックがあれば、切り替えのたびにComfyUIが再起動するので、読み込まれるのは常に1つだけになります。

モデルをサーバー内でアンロードせず、コンテナを*停止*する理由は、次のとおりです。サーバー内でアンロードすると、モデルを何度も切り替えた後にカードの約1 GBが占有されたままになりました。コンテナを停止すれば、すべて解放されます。

<a id="the-steps"></a>

### ステップ

各ステップは、メソッド、アドレス、ヘッダー、ボディ、タイムアウト、その後の待機時間（秒）で構成されます。ヘッダーは暗号化して保存されるため、プロキシのキーをそこに書き込めます（`Authorization: Bearer ...`）。すべての前段にLiteLLMを置く場合、音声ロックのステップは次のとおりです（*Before 1*から*3*が取得ステップ、*After 1*が解放ステップです）。

| | メソッドとアドレス | ヘッダー | ボディ | 内容 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 言語モデルをアンロードする。フックが自ら応答し、1〜2秒かかる。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 音声コンテナを起動し、サーバーの準備ができたら応答する（約13秒）。 |
| Before 3（サーバーがモデルなしで起動する場合のみ） | `POST http://litellm:4000/gpu/tts/load` | | | モデルを読み込む（8〜14秒、初回のダウンロード時は50秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | コンテナを停止する（1秒未満）。 |

**Higgs**のバックエンドでは、*Before 2*は`POST http://litellm:4000/gpu/comfy/start`です（ComfyUIが起動すると応答し、約15秒かかります。モデルは最初の断片とともに読み込まれます）。*Before 3*はなく、*After 1*は`POST http://litellm:4000/gpu/comfy/stop`です。2つのパススルーについては、`tools/gpu-control`のREADMEに記載があります。

言語モデルは自動的に戻ります。次の通常のリクエストで、ルーターがモデルを読み込みます（大きなモデルでは約1分かかります。[プロンプトキャッシュフック](../prompt-cache.md)を使っていれば、セッションのキャッシュも復元されます）。そのため、バッチの後に言語モデルのためのリクエストを送る必要はありません。

LiteLLMを使わない場合は、同じリクエストがコンテナを起動・停止する仕組みに直接送られます。守るべきルールは、上記の順序だけです。

<a id="what-answers-those-addresses"></a>

### これらのアドレスに応答するもの

- **`X-Llama-Unload`** は、LiteLLMのフック`gpu_unload.py`が応答します。そのコードは[GPUを別のモデルのために解放する](../prompt-cache.md#freeing-the-gpu-for-another-model)で説明しています（キャッシュフックがなくても動作します）。ルート`openai/*`がある場合、ボディのモデル名は`openai/`で始まる必要があります。別の名前だと、モデルをアンロードした*後*に400が返されます。
- **`/gpu/tts/start`、`/stop`、`/load`** は、LiteLLMの**パススルーエンドポイント**です。リクエストは、リポジトリにある小さなコントローラー`tools/gpu-control`に転送されます。コントローラーは名前を指定してコンテナを起動・停止します。そのREADMEには、**Dockerソケットプロキシ**を使うcomposeファイルが載っています。これによりコントローラーは、コンテナの起動と停止だけができ、それ以外は何もできなくなります（ソケット自体はホスト上ではrootです）。また、間違えやすい設定も載っています: `POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。LiteLLMでは次のように設定します。

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

  （コンテナ名は`chatterbox`で、`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info`はコントローラーが待つ対象を指定します。コントローラーのトークンは`.env`とLiteLLMの環境変数に置かれ、Marginaliaが持つのはLiteLLMのキーだけです。）
- **すべてのコンテナとLiteLLMは、1つのDockerネットワークを共有します**（`docker network create ai`、初回のみ）。各composeファイルに`networks: [default, ai]`と外部ネットワーク`ai`を書き込んでください。手作業で行った`docker network connect`は、コンテナが再作成されると失われます。

<a id="checking-it"></a>

## 動作確認

1. `curl http://<speech server>/api/model-info`が応答すること（起動後は`loaded`がtrueになります）。
2. バックエンドの*Test connection*で、モデルが読み込まれていると表示されること。GPUロックだけがサーバーを起動する場合は、ロックを選択した状態でテストしてください。*the server answers, no model is loaded*は、停止して再起動したサーバーがまだモデルを読み込む必要がある状態の応答です。
3. **Convert to audio**で短い部分を1つ変換します。ウィンドウに断片が作られていく様子が表示されます。その後、音声コンテナが停止し、`nvidia-smi` / `nvtop` / `rocm-smi` でカードが再び空いていることを確認できるはずです。
4. モデル読み込み後の最初の呼び出しに時間がかかる場合は、エンジンのウォームアップです（初回のみ、最初の実行では28秒かかり、以後は発生しません）。
<a id="troubleshooting"></a>

## トラブルシューティング

| 症状 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | サーバーがモデルを読み込んでいない状態で起動した（起動ラッパーがモデルを一度アンロードしている）。ロックの取得手順に **Before 3** の手順を追加してください。 |
| 最初のピースは作られるが、その後サーバーが *Model not loaded* と返し、ログに `Request received for /api/unload` が出る | Marginalia 以外がモデルをアンロードしている（拡張機能は `/api/unload` を呼び出しません）。典型的な原因は、コンテナ内の起動ラッパーが、サーバーの応答を待った後に「起動を軽くするため」としてモデルをアンロードしていることです。シングルスレッドのサーバーが最初のピースの処理で忙しかったため、このラッパーの待機は最初のピースの後にしか終わらず、アンロードがバッチの途中で実行されてしまいます（コンテナのログに出る `urllib` のタイムアウトのトレースバックは、このラッパーが待機している証拠です）。Compose ファイルで探すべきパターンは `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"` です。これを `command: ["python3", "server.py"]` に置き換えてください。起動だけであれば Turbo は読み込まれた状態になります。アンロードは起動後にカードを空けておくためだけのものであり、コントローラーが起動と停止を行うコンテナには不要です。誰がリクエストを送ったかを確かめるには、拡張機能のログ（*Admin → Extensions → Log*）を読んでください。ここには、バッチの前後のすべてのリクエストが、アドレスと応答時刻とともに記録されています。ピースの開始と終了の時刻も記録されています。GPU ロックの手順はアプリケーションのログにあります。バッチが実行中だとログに示されているときに届いたアンロード、またはリストにないアドレスからのアンロードは、拡張機能によるものではありません。 |
| バッチが途中で止まる（拡張機能のログに *Cannot reach* と出る、またはサーバーのログが途切れる）が、手動で再起動すると動く | サーバーが停止した。典型的には、システムがメモリを使い切ったとしてプロセスを終了させたケースです（ホスト上で数 GB を使う `dmesg \| grep -i oom` の `python3` プロセス）。その場合、拡張機能は自分でサーバーを再起動します（バッチ後のリクエスト、GPU ロックの解放と取得の手順、その後にバッチ前のリクエストの順）。そのうえでピースを一度だけ作り直します。サーバーを復帰させられない場合、またはすぐにまた停止した場合は、バッチの残りが直ちに失敗し、ログにその理由が出ます。メモリリークするサーバーがそこまで至らないようにするには、バックエンドで **Restart the server after (pieces)** を設定します（例: 100）。また、システムが終了させるのがそのプロセスだけになるように、コンテナに `mem_limit` を設定してください。 |
| Higgs（ComfyUI 上）: 約 15 ピース後、すべてのピースが *Higgs failed on the server: CUDA out of memory*（`Tried to allocate 1.45 GiB`）で失敗する。ComfyUI を再起動するまで続く | Higgs の音声コーデックが解放されない。transformers 5.15.1 は読み込んだすべてのコーデックを、`HiggsAudioV2TokenizerModel` のメソッド `@lru_cache` によってキャッシュに保持する。モデルを読み込み直すたびに fp32 で約 770 MB がカードから使われ、ComfyUI がモデルをアンロードした後は、スイートがピースごとにモデルを読み込み直す。対策として、バックエンドの **Restart the server after (pieces)** を 10 に設定するか、transformers が修正されるまで、スイートがモデルを手放すときにそのキャッシュをクリアしてください（`HiggsAudioV2TokenizerModel._get_conv1d_layers.cache_clear()`）。 |
| 変換が直ちに失敗し、プロキシが応答を返す | GPU ロックの最初の取得手順が失敗した（キーが間違っている、アンロードのルートが見つからないなど）。リストはそこで意図的に止まります。 |
| バッチ開始時にホストのメモリが不足する | 言語モデルがまだカード上に残っている。音声ロックにアンロード手順がない、または先頭にない。あるいは、言語モデルの AI に同じ GPU のロックがない。 |
| 日本語が意味不明に読まれる | サーバーのイメージに `pykakasi` がない。そのピースにはバックエンドもマルチリンガル版を使う必要がある。 |
| プロキシの背後でクリップのアップロードが失敗する | プロキシは multipart のボディ（`/upload_reference`）を変更せずに通す必要がある。LiteLLM のパススルーではテストしていない。動作するまでは、サーバーの音声ファイル（`predefined:Name.wav`）を使ってください。 |
| 別のユーザーのバッチが、あなたのモデルの下でモデルをアンロードしてしまう | 1 枚のカード上のすべてのバックエンドと AI は、同じ [GPU](../administration/gpus.md) のロックを指定する必要がある。そうして初めて順番に使われる。ロックのないバックエンドは誰も待たない。同じカード上に Marginalia を 2 つインストールしても、GPU は共有されない。 |
