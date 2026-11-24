---
label: 设置语音服务器
order: 28
---

# 为有声书设置语音服务器

[有声书](audiobook.md)扩展需要一个 **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)，基于 FastAPI，MIT 许可证），并且它必须能通过 HTTP 访问该服务器。本页介绍服务器端。这里的内容是在一台机器上试验得出的（配备 32 GB 显存的 AMD Radeon AI PRO R9700，所有请求前面都有 LiteLLM，`llama-server` 处于路由模式）。你的硬件和代理可能需要其他取值。

<a id="which-setup-is-yours"></a>

## 哪种设置适合你？

| 你的情况 | 该怎么做 |
|---|---|
| 语音服务器运行在**另一台机器**上、在 CPU 上运行，或者运行在一块**与语言模型并存**还有余量的显卡上 | 运行服务器，把它的地址填入后端，**不选择 GPU 锁**。无需其他步骤。 |
| 语音服务器和语言模型**共用一块显卡**，两者放不下 | 你需要一个 [GPU 锁](../administration/gpus.md)：它的 **acquire** 步骤释放显卡并启动语音服务器，它的 **release** 步骤则停止语音服务器。本页其余部分适用于这种情况。 |
| 语言模型是云端模型 | 无需释放任何资源：把语音服务器放在任何你想要的地方，并且不选择 GPU 锁。 |

所以：**只有当两个模型在同一块 GPU 上轮流工作时，才需要 pre 和 post 请求。** 正是这些请求实现了轮流工作：插件围绕一个*批次*（你一次点击所启动的全部内容）发出这些请求一次，而不是围绕每个部分各发一次。

<a id="1-the-speech-server"></a>

## 1. 语音服务器

在容器中（或从源码）运行 Chatterbox-TTS-Server，使 `GET /api/model-info` 能够响应。以下事项很重要：

- **模型。** Turbo（`chatterbox-turbo`）是扩展的默认模型，并且在测试中音质最好；只有当文本的语言与书籍语言不同时，才需要多语言模型。模型在服务器的 `config.yaml` 中设置（或者 `POST /save_settings` 和 `POST /restart_server`）。后端的 *Speech model* 必须与服务器在其 GPU 锁的获取步骤之后加载的模型一致。
- **日语需要镜像中有 `pykakasi`。** 没有它，服务器会记录 *pykakasi not available - Japanese text processing skipped*，并且会错误地读出汉字。请将 `RUN python3 -m pip install --no-cache-dir pykakasi` 加入其 Dockerfile（在 `COPY . .` 之前）。希伯来语、俄语和中文的相应包同样存在这种静默跳过的问题。
- **声音**是服务器 `reference_audio/` 文件夹中的文件（挂载该文件夹，使其在重建后保留）。扩展会在批次开始前自行上传你的角色的音频片段（`POST /upload_reference`），文件名由片段的哈希值生成。服务器没有删除接口：片段会不断堆积在那里。
- 服务器**没有身份验证**。不要公开其端口；如果它不在私有网络中，请在其前面放置代理。
- `restart: "no"` 适用于由其他程序按需启动的容器。`restart: unless-stopped` 会在开机时启动它，并白白占用显卡。

在后端（*Settings → Extension Settings → Audiobook Settings*）中填入其地址；如果服务器位于本机或你的网络中，勾选 *The server is on this machine or on my network*，然后按下 **Test connection**。你希望看到的结果是 *the server answers, the model is loaded*。

扩展通过 `/tts` 与服务器通信（完整参数集：声音、语言、exaggeration、CFG weight、temperature、speed、seed），并使用 `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference`。如果你和服务器之间有代理，它必须转发所有这些请求。

<a id="higgs-audio-v3-in-comfyui"></a>

## 在 ComfyUI 中使用 Higgs Audio v3

使用语音模型 **Higgs Audio v3 in ComfyUI** 的后端，是与安装了 [TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) 节点的 [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) 通信，而不是与 Chatterbox-TTS-Server 通信。它的地址就是 ComfyUI 的地址（例如 `http://gpu-server:8188`）。它是在同一台 AMD R9700 上，在 `yanwk/comfyui-boot:rocm` 镜像中设置的。

1. **安装该套件**到 ComfyUI 的 `custom_nodes` 文件夹中（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）。它的 Python 包会安装到容器内。为了在容器重建后保留它们，请为 pip 指定卷上的一个文件夹：将 `PIP_USER=true` 添加到容器的环境变量中，并挂载 `./storage/local:/root/.local`。
2. **绝不要让 pip 替换镜像中的 torch**（CUDA 版本会替换 ROCm 版本）。把已安装的版本写入一个文件，并以它作为约束条件进行安装：
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   不要整体运行该套件的 `install.py`：它可能会重新安装 torch。`librosa` 不在其 `requirements.txt` 中，而没有它套件就无法加载。
3. **模型**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（约 8 GB），或者让套件在首次使用时自动下载。它需要显卡上约 8 GB 的显存。
4. 当 ComfyUI 和该节点都已就绪时，后端的 **Test connection** 会显示 *the server answers*，并显示节点所用的模型。

扩展对它的处理如下：它把角色的音频片段上传到 ComfyUI 的输入文件夹（`POST /upload/image`，文件名由片段生成）；用 `GET /object_info` 列出套件的声音和片段；为每个片段向 `/prompt` 提交一个自己的工作流，用 `/history` 等待，并从 `/view` 获取音频。（该工作流还会把 TTS 节点的生成信息交给核心 `PreviewAny` 节点：套件遇到错误时会返回一秒静音，且运行仍显示成功，只有这些信息能表明该片段失败；失败后会报告原因，而不是保留该片段。）若 Marginalia 一侧有 ffmpeg，音频会以 flac 格式处理：在最后一个词之后裁剪，淡出，然后保存为 mp3 或 wav；若没有 ffmpeg，则直接保存 ComfyUI 生成的 mp3，不作裁剪（此时 wav 存储不可用）。**每个片段之后，它都会用 `{"free_memory": true}` 调用 `POST /free`**：在 R9700 上，已加载的模型连续处理第三个片段时会导致显卡崩溃（`HSA_STATUS_ERROR_EXCEPTION`，ComfyUI 退出）；在片段之间清空内存可以让模型保持加载状态并避免该问题，每个片段约多花 3 秒。ComfyUI 在一个批次前后启动和停止，使用的请求与语音服务器相同（[这些请求](#the-requests)）。

**在 AMD 显卡上使用 fp32。** 当精度为 `auto`（R9700 搭配 ROCm 7.2 时为 bf16）时，许多片段出现乱码、变成音效（一句关于敲打的台词变成了十一下敲击声）或使用了错误的声音，而改变种子只是碰运气。同样的片段在 fp32 下则逐字准确。后端的 **Precision** 默认为 fp32；它需要显卡约 16 GB 的显存。套件中的 Granite 语音识别在 bf16 下存在同样的问题（从第二次识别开始出现乱码），因此扩展也以 float32 运行它。

**片段清理**（[片段清理](audiobook.md#cleanup-of-clips)）还使用同一套件中的另外两个节点 `VocalRemovalNode`（分离模型）和 `VoiceFixerNode`，以及 ComfyUI 的 `LoadAudio` 和 `SaveAudio`；无需其他节点包。套件会在首次使用某个模型时下载它（MelBand Roformer 模型每个约 900 MB）。在 R9700 上，MelBand 模型（人声、降噪）和 voice fixer 工作正常，处理一段 16 秒的片段约需 25 秒。UVR 模型起初无法工作：

- `UVR-DeEcho-DeReverb`（被 *Remove echo* 和 *Film or game clip* 使用）失败，报错 `module 'samplerate' has no attribute 'resample'`。容器中的 `samplerate` 版本为 0.1.0，这是一个需要系统库 `libsamplerate` 的封装，而镜像中缺少该库。当前的包自带该库；请使用 torch 版本约束安装它，然后重启 ComfyUI：
  ```sh
  PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install --user 'samplerate>=0.2.1'
  ```
  之后，去混响处理一段 16 秒的片段约需 5 秒。
- `UVR-DeNoise` 下载失败（套件的地址返回 404）；要使用它，请手动将文件放入 `models/TTS/UVR/`。MelBand 降噪模型不需要这一步，它是默认清理方案中使用的模型。

套件中的 **Fish Audio S2 Pro** 引擎在该显卡上无法工作：其音频输出为无效数值（NaN）。Higgs Audio v3 的权重采用仅供研究与非商业用途的许可证。
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 一张 GPU 同时承载语言模型和语音

装有语言模型的显卡放不下语音模型。管理员在 [Admin → GPUs](../administration/gpus.md) 下一次性设置好切换方案：为每个可以加载到显卡上的组件配置一个 GPU 和一把锁，然后在语言模型的 AI 设置和语音后端中选择对应的锁。方案对任何代理都相同：

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

语音后端的一个批次（或一个样本）在显卡上加载着语言模型时，会先执行语音锁的获取（acquire）步骤，然后生成所有片段；此后显卡保持加载语音服务器的状态。下一次对语言模型的请求会等待该批次结束，执行语音锁的释放（release）步骤（停止服务器），然后继续。连续的多个批次和样本只需付出一次切换的代价。

**顺序不可更改。** 在语言模型占用显卡时启动语音容器，测试中会耗尽宿主机内存（30 GB 内存、交换分区，然后 OOM killer 杀掉了其他服务）：必须**先卸载**。第一个失败的获取步骤会终止整个列表，因此显卡已满时不会启动任何东西；而当某个释放步骤失败时，下一把锁根本不会加载（显卡可能仍然占满），任务随之以该错误失败。无论其中某个释放步骤出了什么问题，所有释放步骤都会执行。

**Higgs、语音识别和片段清理共用一个 ComfyUI**，各自都有一把独立的锁，三把锁的步骤相同（卸载语言模型，启动 ComfyUI；释放：停止 ComfyUI）。ComfyUI 会把已卸载的模型保留在服务器内存中（fp32 的 Higgs 约 16 GB），测试中在旁边加载语音识别导致服务器内存耗尽；有了三把锁，每次切换都会重启 ComfyUI，因此任何时候只会加载其中一个。

为什么要*停止*容器，而不是在服务器内部卸载模型：在服务器内部卸载，经过多次模型切换后，显卡仍会被占用约 1 GB；停止容器则会释放全部显存。

<a id="the-steps"></a>

### 步骤

每个步骤包括：方法、地址、请求头、请求体、超时时间，以及之后的等待时间（秒）。请求头以加密方式存储，因此可以把代理的密钥写入其中（`Authorization: Bearer ...`）。在所有服务前面有 LiteLLM 时，语音锁的步骤如下（*Before 1* 至 *3* 为其获取步骤，*After 1* 为其释放步骤）：

| | 方法和地址 | 请求头 | 请求体 | 作用 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 卸载语言模型；由钩子本身应答，耗时 1 到 2 秒。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 启动语音容器，并在服务器就绪后应答（约 13 秒）。 |
| Before 3（仅当服务器启动时不带模型） | `POST http://litellm:4000/gpu/tts/load` | | | 加载模型（8 到 14 秒，首次需下载时为 50 秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 停止容器（不到一秒）。 |

对于 **Higgs** 后端，*Before 2* 为 `POST http://litellm:4000/gpu/comfy/start`（在 ComfyUI 启动后应答，约 15 秒；模型随第一个片段加载），没有 *Before 3*，*After 1* 为 `POST http://litellm:4000/gpu/comfy/stop`。这两个直通端点的说明在 `tools/gpu-control` 的 README 中。

语言模型会自动恢复：下一次普通请求会让路由器加载它（大模型约需一分钟；启用 [prompt cache hook](../prompt-cache.md) 时会恢复你会话的缓存），因此批次结束后无需为它额外发送请求。

不使用 LiteLLM 时，同样的请求直接发往负责启动和停止你容器的程序；唯一的规则仍是上述顺序。

<a id="what-answers-those-addresses"></a>

### 响应这些地址的是谁

- **`X-Llama-Unload`** 由 LiteLLM 的钩子 `gpu_unload.py` 应答，其代码见 [为另一个模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)（不依赖 cache hook 也可工作）。若使用路由 `openai/*`，请求体中的模型名必须以 `openai/` 开头：其他名称会在模型已被卸载*之后*返回 400。
- **`/gpu/tts/start`、`/stop`、`/load`** 是 LiteLLM 的**直通端点**，指向仓库中的小型控制器 `tools/gpu-control`，它按名称启动和停止容器。其 README 中包含带有 **Docker socket 代理**的 compose 文件（使控制器只能启动和停止容器，别无其他权限；socket 本身在宿主机上具有 root 权限），以及容易配错的设置：`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。在 LiteLLM 中：

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

  （容器名为 `chatterbox`，`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` 告诉控制器需要等待什么；控制器的令牌保存在其 `.env` 中以及 LiteLLM 的环境变量里，Marginalia 只持有 LiteLLM 的密钥。）
- **所有容器和 LiteLLM 共用一个 Docker 网络**（只需创建一次 `docker network create ai`）。在每个 compose 文件中加入 `networks: [default, ai]` 以及外部网络 `ai`：手动完成的 `docker network connect` 会在容器重建时丢失。

<a id="checking-it"></a>

## 检查

1. `curl http://<speech server>/api/model-info` 有应答（启动后 `loaded` 为 true）。
2. 后端中的 *Test connection* 显示模型已加载。如果 GPU 锁是唯一启动服务器的方式，请在选中该锁的情况下测试：*服务器有应答，但未加载模型* 是一个已停止又重新启动、但仍需加载模型的服务器的应答。
3. 在 **Convert to audio** 中转换一小段。窗口会显示正在生成的片段；之后语音容器应已停止，并且 `nvidia-smi` / `nvtop` / `rocm-smi` 显示显卡重新空闲。
4. 如果模型加载后的第一次调用耗时较长，那是引擎在预热（只会发生一次，首次运行时为 28 秒，之后不再出现）。
<a id="troubleshooting"></a>

## 故障排除

| 现象 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 服务器启动时是空的（一个只卸载一次模型的启动包装脚本）：请在锁的获取步骤中添加步骤 **Before 3**。 |
| 前几个片段已生成，随后服务器报告 *Model not loaded*，其日志显示 `Request received for /api/unload` | 模型被 Marginalia 以外的程序卸载（扩展从不调用 `/api/unload`）。通常是容器中的一个启动包装脚本，它等待服务器应答，然后为了"让启动更快"而卸载模型。由于单线程服务器当时正忙，该脚本的等待可能只会在第一个片段之后才结束，此时卸载恰好落在批处理的中途（容器日志中的 `urllib` 超时回溯即是该脚本在等待）。在 compose 文件中应查找的写法是 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`。请将其替换为 `command: ["python3", "server.py"]`：仅启动即可得到已加载的 Turbo；而卸载只是为了在启动后释放显卡，对于由控制器启动和停止的容器而言并不需要。要判断是谁发出的请求，请查看扩展的日志（*Admin → Extensions → Log*）：它列出批处理前后的每个请求，包括其地址和应答时间，以及片段开始和结束的时间；GPU 锁的各个步骤则在应用日志中。若卸载发生在日志显示批处理正在运行期间，或来自不在列表中的地址，则它不是扩展发出的。 |
| 批处理中途停止（扩展日志显示 *Cannot reach*，或服务器日志就此结束），而手动重启可以恢复正常 | 服务器已崩溃，通常是因为占用了全部内存而被系统终止（宿主机上的 `dmesg \| grep -i oom` 显示一个占用数 GB 的 `python3` 进程）。随后扩展会自行重启它（依次执行批处理之后的请求、GPU 锁的释放和获取步骤，然后是批处理之前的请求），并重新生成该片段一次。如果服务器无法恢复，或再次立即消失，批处理的其余部分会立即失败，日志会说明原因。为防止泄漏的服务器发展到这一步，请在后端中将 **Restart the server after (pieces)** 设置为（例如）100，并在容器上设置 `mem_limit`，以便系统只终止它。 |
| Higgs 在 ComfyUI 上：约 15 个片段之后，每个片段都以 *Higgs failed on the server: CUDA out of memory*（`Tried to allocate 1.45 GiB`）失败，直到重启 ComfyUI | Higgs 的音频编解码器从不释放：transformers 5.15.1 会将其加载过的每个编解码器保存在缓存中（`@lru_cache`，属于 `HiggsAudioV2TokenizerModel` 的一个方法），每次重新加载模型都会以 fp32 占用约 770 MB 显存；而一旦 ComfyUI 卸载了模型，套件就会在每个片段时重新加载它。在 transformers 修复之前，请在后端中将 **Restart the server after (pieces)** 设为 10，或者在套件释放模型时清空该缓存（`HiggsAudioV2TokenizerModel._get_conv1d_layers.cache_clear()`）。 |
| 转换立即失败，并显示代理的应答 | 获取 GPU 锁的第一个步骤失败（密钥错误，或找不到卸载路由）；步骤列表在此处有意停止。 |
| 批处理开始时宿主机内存耗尽 | 语言模型仍占用着显卡：语音锁中缺少卸载步骤，或该步骤不是第一个；或者语言模型的 AI 没有与之使用同一 GPU 的锁。 |
| 日语被读成了乱码 | 服务器的镜像中缺少 `pykakasi`；对于该片段，后端也必须是多语言版本。 |
| 通过代理上传音频片段失败 | 代理必须原样传递 multipart 请求体（`/upload_reference`）。尚未通过 LiteLLM 透传测试；在此之前，请使用服务器上的语音文件（`predefined:Name.wav`）。 |
| 另一位用户的批处理卸载了你正在使用的模型 | 同一显卡上的每个后端和 AI 都必须指定同一 [GPU](../administration/gpus.md) 的锁：只有这样它们才会轮流使用。没有锁的后端不会等待任何人。同一显卡上的两个 Marginalia 安装实例不共享 GPU。 |
