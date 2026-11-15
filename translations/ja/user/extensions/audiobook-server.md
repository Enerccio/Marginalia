---
label: 音声サーバーのセットアップ
order: 28
---

# Audiobook のための音声サーバーのセットアップ

[Audiobook](audiobook.md) 拡張機能には、HTTP で到達できる **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)、
FastAPI、MIT ライセンス）が必要です。このページはサーバー側を扱います。ここに書かれている内容は、1台のマシン（AMD Radeon AI PRO R9700、32 GB、前段に LiteLLM、`llama-server` はルーターモード）で試したものです。お使いのハードウェアやプロキシでは、別の値が必要になる場合があります。

<a id="which-setup-is-yours"></a>

## お使いの構成はどれですか？

| 状況 | やること |
|---|---|
| 音声サーバーが **別のマシン** 上、CPU 上、または **言語モデルと一緒に** 入る余裕のあるカード上で動く | サーバーを起動し、そのアドレスをバックエンドに設定し、**GPU ロックは選ばない**。ほかには何も必要ありません。 |
| 音声サーバーと言語モデルが **1枚のカードを共有** しており、両方は収まらない | [GPU ロック](../administration/gpus.md)が必要です。その **acquire** 手順がカードを空け、音声サーバーを起動します。**release** 手順がそれを停止します。以下がこのページの内容です。 |
| 言語モデルがクラウドモデルである | 空けるものはありません。音声サーバーは好きな場所で動かし、GPU ロックは選ばないでください。 |

つまり、**はい、pre と post のリクエストが必要なのは、2つのモデルが1つの GPU を交互に使う場合だけです。** これが交代を実現するもので、プラグインはそれらを *バッチ*（ワンクリックで開始したすべて）の前後で一度だけ送り、部品ごとには送りません。

<a id="1-the-speech-server"></a>

## 1. 音声サーバー

`GET /api/model-info` が応答するように、Chatterbox-TTS-Server をコンテナ内（またはソースから）で動かしてください。重要な点は次のとおりです。

- **モデル。** Turbo（`chatterbox-turbo`）は拡張機能の既定値で、テストでは最も良い音でした。多言語モデルは、本の言語とは別の言語のテキストにだけ必要です。モデルはサーバーの `config.yaml`（または `POST /save_settings` と `POST /restart_server`）で設定します。バックエンドの *Speech model* は、GPU ロックの acquire 手順の後にサーバーが読み込んだモデルと一致している必要があります。
- **日本語にはイメージ内の `pykakasi` が必要です。** これがないと、サーバーは *pykakasi not available - Japanese text processing skipped* とログに出力し、漢字を誤って読みます。その Dockerfile に `RUN python3 -m pip install --no-cache-dir pykakasi` を追加してください（`COPY . .` の前に）。ヘブライ語、ロシア語、中国語のパッケージでも、同じように黙ってスキップされる現象があります。
- **声** は、サーバーの `reference_audio/` フォルダー内のファイルです（消えないようにマウントしてください）。拡張機能は、バッチの前にキャラクターのクリップを（`POST /upload_reference`）自分でアップロードします。その際の名前はクリップのハッシュから作られます。サーバーには削除の呼び出しがないため、クリップはそこに溜まっていきます。
- サーバーには **認証がありません**。そのポートを公開しないでください。プライベートネットワーク上にない場合は、前段にプロキシを置いてください。
- `restart: "no"` は、ほかの何かがオンデマンドで起動するコンテナ向けです。`restart: unless-stopped` はブート時に起動し、カードを常に占有します。

バックエンド（*Settings → Extension Settings → Audiobook Settings*）でそのアドレスを入力し、サーバーが同じマシンまたは自分のネットワーク上にある場合は *The server is on this machine or on my network* にチェックを入れ、**Test connection** を押します。目指すのは *the server answers, the model is loaded* の表示です。

拡張機能は `/tts`（パラメーターの全セット：声、言語、exaggeration、CFG weight、temperature、速度、seed）と、`GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` に話しかけます。あなたとサーバーの間にプロキシがある場合、それらすべてを通す必要があります。

<a id="higgs-audio-v3-in-comfyui"></a>

## ComfyUI の Higgs Audio v3

音声モデルが **Higgs Audio v3 in ComfyUI** のバックエンドは、Chatterbox-TTS-Server ではなく、[TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) のノードを備えた [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) と通信します。そのアドレスは ComfyUI のアドレスです（例：`http://gpu-server:8188`）。同じ AMD R9700 上の `yanwk/comfyui-boot:rocm` イメージで設定しました。

1. **suite をインストールします。** ComfyUI の `custom_nodes` フォルダー（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）に入れてください。その Python パッケージはコンテナ内に入ります。コンテナを作り直しても保持するため、pip にボリューム上のフォルダーを与えてください。コンテナの環境変数に `PIP_USER=true` を追加し、`./storage/local:/root/.local` をマウントします。
2. **pip にイメージの torch を置き換えさせないでください**（CUDA ビルドは ROCm 版を置き換えてしまいます）。インストール済みのバージョンをファイルに書き出し、それを制約（constraint）として指定してインストールしてください：
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   suite の `install.py` を丸ごと実行しないでください。torch を再インストールする可能性があります。`librosa` はその `requirements.txt` に含まれておらず、これがないと suite は読み込まれません。
3. **モデル**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（約 8 GB）を使うか、初回に suite にダウンロードさせてください。グラフィックカードの約 8 GB を必要とします。
4. バックエンドの **Test connection** で、ComfyUI とノードがあれば、ノードのモデルとともに *the server answers* と表示されます。

拡張機能はこれに対して次のことを行います。キャストのクリップを ComfyUI の入力フォルダー（`POST /upload/image`）にアップロードし（クリップから作られた名前で）、`GET /object_info` で suite の声とクリップを一覧表示し、部分ごとに自前のワークフローを `/prompt` へ送信し、`/history` で待機して、`/view` から音声を取得します。Marginalia 側に ffmpeg があれば flac で取得し、最後の単語の後でトリムしてフェードアウトしたうえで mp3 または wav として保存します。ffmpeg がなければ ComfyUI の mp3 をトリムせずにそのまま使います（この場合、wav での保存は機能しません）。**部分ごとに、`{"free_memory": true}` を付けて `POST /free` を呼び出します**。R9700 では、読み込み済みのモデルで連続して3つ目の部分を処理すると、グラフィックカードがクラッシュしました（`HSA_STATUS_ERROR_EXCEPTION`、ComfyUI が終了します）。部分の間でメモリを解放すると、モデルは読み込まれたままになり、このクラッシュを避けられます。ただし1部分あたり約3秒かかります。ComfyUI は、音声サーバーと同種のリクエストによって、バッチの前後で起動と停止が行われます（[リクエスト](#the-requests)）。

**AMD カードでは fp32 を使ってください。** 精度を `auto` にすると（ROCm 7.2 の R9700 では bf16）、多くの部分が意味不明な音声になったり、効果音になったり（「ドンドン叩く」という一文が、11回のノック音になった）、誤った声になったりしました。シードを変えても、その運が変わるだけでした。同じ部分を fp32 で処理すると、言葉は完璧でした。バックエンドの **Precision** は既定で fp32 で、カードの約 16 GB を必要とします。suite の Granite 音声認識も bf16 で同じ問題があり（2回目以降の認識が意味不明になる）、そのため拡張機能はこれも float32 で実行します。

suite の **Fish Audio S2 Pro** エンジンは、そのカードでは動作しませんでした。音声が無効な数値（NaN）として出力されたためです。Higgs Audio v3 の重みは、研究用および非商用のライセンスの下にあります。
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 1つのGPUでモデルと音声を共有する

カードに言語モデルが載っていると、音声モデルを載せる余地がありません。管理者は一度だけ [Admin → GPUs](../administration/gpus.md) で入れ替えを設定します。GPUと、その上に読み込まれうるものごとのロックを用意し、言語モデルのAIと音声バックエンドでそのロックを選びます。計画はどのプロキシでも同じです:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

音声バックエンドのバッチ（またはサンプル）は、GPUに言語モデルが載っているとき、音声ロックの取得ステップを実行し、その後すべての断片を処理します。GPUには音声サーバーが起動したまま残ります。言語モデルへの次のリクエストは、バッチの完了を待ってから音声ロックの解放ステップ（コンテナの停止）を実行し、そのあと処理を続けます。連続する複数のバッチやサンプルは、入れ替えの費用を一度だけ払います。

**順序は変更できません。** カードが満杯の状態で音声コンテナを起動すると、テストではホストのメモリが埋まりました（RAM 30 GB、スワップ、その後OOMキラーが他のサービスを停止）。そのため、アンロードが**必ず先**です。最初に失敗した取得ステップでリストは終了するので、満杯のカードの上では何も起動しません。解放ステップが失敗した場合、次のロックは読み込まれず（GPUがまだ満杯かもしれないため）、ジョブはエラーで失敗します。解放ステップは、どれか一つが失敗してもすべて実行されます。

モデルをサーバー内でアンロードする代わりにコンテナを*停止*する理由：サーバー内のアンロードでは、多数のモデル切り替えの後にカードの約 1 GB が占有されたままになりました。コンテナを停止すると、すべて解放されます。

<a id="the-steps"></a>

### ステップ

各ステップは、メソッド、アドレス、ヘッダー、ボディ、タイムアウト、そしてその後の待機時間（秒）で構成されます。ヘッダーは暗号化して保存されるため、プロキシのキーをそこに書き込めます（`Authorization: Bearer ...`）。前段にLiteLLMを置く場合、音声ロックのステップは次のとおりです（*Before 1* から *3* が取得ステップ、*After 1* が解放ステップです）:

| | メソッドとアドレス | ヘッダー | ボディ | 役割 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 言語モデルをアンロードします。フック自身が応答し、1〜2秒かかります。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 音声コンテナを起動し、サーバーの準備ができたら応答します（約13秒）。 |
| Before 3（サーバーがモデルなしで起動する場合のみ） | `POST http://litellm:4000/gpu/tts/load` | | | モデルを読み込みます（8〜14秒、初回ダウンロード時は50秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | コンテナを停止します（1秒未満）。 |

**Higgs** バックエンドの場合、*Before 2* は `POST http://litellm:4000/gpu/comfy/start` です（ComfyUIが起動すると応答します。約15秒。モデルは最初の断片と一緒に読み込まれます）。*Before 3* はなく、*After 1* は `POST http://litellm:4000/gpu/comfy/stop` です。2つのパススルーは `tools/gpu-control` のREADMEにあります。

言語モデルは自動的に戻ります。次の通常のリクエストでルーターがモデルを読み込みます（大きなモデルで約1分。[prompt cache hook](../prompt-cache.md) を使っていれば、セッションのキャッシュが復元されます）。そのため、バッチの後にこのための追加リクエストは不要です。

LiteLLMを使わない場合、同じリクエストはコンテナを起動・停止する仕組みへ送られます。守るべきルールは上記の順序だけです。

<a id="what-answers-those-addresses"></a>

### アドレスに応答するもの

- **`X-Llama-Unload`** は、LiteLLMのフック `gpu_unload.py` が応答します。コードとともに [別のモデルのためにGPUを空ける](../prompt-cache.md#freeing-the-gpu-for-another-model) で説明しています（キャッシュフックなしでも動作します）。ルート `openai/*` を使う場合、ボディのモデル名は `openai/` で始まる必要があります。別の名前だと、モデルがアンロードされた*後で* 400 が返ります。
- **`/gpu/tts/start`, `/stop`, `/load`** は、リポジトリの小さなコントローラー `tools/gpu-control` へのLiteLLMの**パススルーエンドポイント**です。コントローラーはコンテナを名前で起動・停止します。そのREADMEには、**Docker socket proxy** を使うcomposeファイル（コントローラーがコンテナの起動と停止だけをできるようにする。ソケット自体はホスト上でrootになる点に注意）と、間違えやすい設定があります：`POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**。LiteLLMでは次のように設定します:

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

  （コンテナ名は `chatterbox`、`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` はコントローラーが待つ対象を示します。コントローラーのトークンは `.env` とLiteLLMの環境変数にのみ置かれ、Marginalia が持つのはLiteLLMのキーだけです。）
- **すべてのコンテナとLiteLLMはDockerネットワークを共有します**（`docker network create ai`、初回のみ）。`networks: [default, ai]` と外部ネットワーク `ai` を各composeファイルに追加してください。手作業で行った`docker network connect`は、コンテナを再作成すると失われます。

<a id="checking-it"></a>

## 動作確認

1. `curl http://<speech server>/api/model-info` が応答します（起動後は `loaded` が true になります）。
2. バックエンドの *Test connection* で、モデルが読み込まれていると表示されることを確認します。GPUロックだけがサーバーを起動する場合は、ロックを選んだ状態でテストしてください。*the server answers, no model is loaded* という応答は、停止して再起動したサーバーがまだモデルを読み込む必要がある状態を示します。
3. **Convert to audio** で短い部分を1つ変換します。ウィンドウに断片の作成状況が表示されます。その後、音声コンテナは停止しているはずで、`nvidia-smi` / `nvtop` / `rocm-smi` でカードが空いたことを確認できます。
4. モデル読み込み後の最初の呼び出しが遅い場合、それはエンジンのウォームアップです（一度だけ起こり、初回の実行では28秒かかりましたが、その後は見られません）。
<a id="troubleshooting"></a>

## Dépannage

| Symptôme | Cause |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Le serveur a démarré vide (un script de démarrage qui décharge le modèle une fois) : ajoutez l'étape **Before 3** aux étapes d'acquisition du verrou. |
| Les premiers morceaux sont produits, puis le serveur répond *Model not loaded* et son journal affiche `Request received for /api/unload` | Quelque chose d'autre que Marginalia décharge le modèle (l'extension n'appelle jamais `/api/unload`) : typiquement un script de démarrage dans le conteneur qui attend que le serveur réponde puis décharge le modèle « pour que le démarrage reste rapide ». Son attente ne peut se terminer qu'après le premier morceau, car le serveur mono-thread était occupé, et le déchargement tombe alors au milieu du lot (une trace de délai `urllib` dans le journal du conteneur indique que ce script attend). Le motif à chercher dans le fichier compose est un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Remplacez-le par `command: ["python3", "server.py"]` : un simple démarrage donne un Turbo chargé, et le déchargement ne servait qu'à libérer la carte après un démarrage, ce dont un conteneur que le contrôleur démarre et arrête n'a pas besoin. Pour savoir qui l'a envoyé, lisez le journal de l'extension (*Admin → Extensions → Log*) : il liste chaque requête avant et après un lot, avec son adresse et l'heure de sa réponse, ainsi que le début et la fin des morceaux ; les étapes du verrou GPU figurent dans le journal de l'application. Un déchargement survenant pendant que le journal indique que le lot est en cours, ou provenant d'une adresse absente de la liste, ne vient pas de l'extension. |
| Le lot s'interrompt au milieu (le journal de l'extension indique *Cannot reach*, ou le journal du serveur s'arrête simplement) et un redémarrage manuel fonctionne | Le serveur est mort, généralement tué par le système parce qu'il a pris toute la mémoire (`dmesg \| grep -i oom` sur l'hôte : un processus `python3` de plusieurs Go). L'extension le redémarre alors elle-même (les requêtes après le lot, les étapes de libération et d'acquisition du verrou GPU, puis les requêtes avant celui-ci) et refait le morceau une seule fois ; si le serveur ne peut pas être relancé, ou s'il tombe de nouveau aussitôt, le reste du lot échoue immédiatement et le journal indique la raison. Pour empêcher un serveur qui fuit d'en arriver là, réglez **Restart the server after (pieces)** dans le backend (par exemple 100), et `mem_limit` sur le conteneur afin que le système ne tue que lui. |
| La conversion échoue immédiatement avec la réponse du proxy | La première étape d'acquisition du verrou GPU a échoué (une mauvaise clé, la route de déchargement introuvable) ; la liste s'arrête volontairement à cet endroit. |
| L'hôte manque de mémoire au démarrage du lot | Le modèle de langage était encore sur la carte : l'étape de déchargement est absente du verrou de la parole ou n'est pas en première position, ou l'IA du modèle de langage n'a pas de verrou sur le même GPU. |
| Le japonais est lu comme du charabia | `pykakasi` est absent de l'image du serveur ; le backend doit aussi être la version multilingue pour ce morceau. |
| L'envoi d'un clip échoue derrière un proxy | Le proxy doit transmettre un corps multipart (`/upload_reference`) sans modification. Non testé via le pass-through de LiteLLM ; utilisez un fichier vocal du serveur (`predefined:Name.wav`) en attendant que cela fonctionne. |
| Le lot d'un autre utilisateur décharge le modèle sous le vôtre | Chaque backend et chaque IA sur une même carte doit nommer un verrou du même [GPU](../administration/gpus.md) : c'est seulement dans ce cas qu'ils se relaient. Un backend sans verrou n'attend personne. Deux installations de Marginalia sur une même carte ne partagent pas de GPU. |
