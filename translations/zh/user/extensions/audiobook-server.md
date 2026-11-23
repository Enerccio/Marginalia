---
label: 设置语音服务器
order: 28
---

# 为有声书设置语音服务器

[有声书](audiobook.md) 扩展需要一个 **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)，基于 FastAPI，MIT 许可证），并且它必须能通过 HTTP 访问该服务器。本页介绍服务器端。下文所述内容在一台机器上测试过（一块 32 GB 的 AMD Radeon AI PRO R9700，前面是 LiteLLM，`llama-server` 处于路由模式）；你的硬件和代理可能需要其他的设置值。

<a id="which-setup-is-yours"></a>

## 你的是哪种配置？

| 你的情况 | 需要做什么 |
|---|---|
| 语音服务器运行在**另一台机器**上、运行在 CPU 上，或运行在一块与语言模型**能够同时容纳**的显卡上 | 运行服务器，将其地址填入一个后端，**不选择 GPU 锁**。无需其他步骤。 |
| 语音服务器与语言模型**共用一块显卡**，且两者无法同时容纳 | 你需要一个 [GPU 锁](../administration/gpus.md)：它的 **acquire** 步骤会释放显卡并启动语音服务器，它的 **release** 步骤则会停止语音服务器。本页其余内容适用于这种情况。 |
| 语言模型是云端模型 | 无需释放任何资源：语音服务器放在你方便的任何地方，不选择 GPU 锁。 |

因此：**只有当两个模型在同一块 GPU 上轮流工作时，才需要 pre 和 post 请求。** 正是这些请求实现了轮流工作：插件围绕一个*批次*（即你一次点击启动的全部内容）发送这些请求，而不是围绕每个部分分别发送。

<a id="1-the-speech-server"></a>

## 1. 语音服务器

在容器中（或从源码）运行 Chatterbox-TTS-Server，使 `GET /api/model-info` 能够正常响应。以下几点很重要：

- **模型。** Turbo（`chatterbox-turbo`）是扩展的默认模型，在测试中音质最好；只有当文本的语言与书籍的语言不同时，才需要多语言模型。模型在服务器的 `config.yaml` 中设置（或 `POST /save_settings` 和 `POST /restart_server`）。后端的 *Speech model* 必须与服务器在其 GPU 锁的 acquire 步骤之后加载的模型一致。
- **日语需要镜像中包含 `pykakasi`。** 否则服务器会记录 *pykakasi not available - Japanese text processing skipped*，并错误地读取汉字。请在其 Dockerfile 中添加 `RUN python3 -m pip install --no-cache-dir pykakasi`（位于 `COPY . .` 之前）。希伯来语、俄语和中文的软件包也存在同样的静默跳过问题。
- **声音**是服务器 `reference_audio/` 文件夹中的文件（请挂载该文件夹，使其在容器重建后保留）。扩展会在批次运行前自行上传你角色的音频片段（`POST /upload_reference`），文件名由片段的哈希值生成。服务器没有删除接口，因此片段会不断堆积。
- 服务器**没有身份验证**。不要公开其端口；如果它不在私有网络中，请在其前面放置一个代理。
- `restart: "no"` 适用于由其他程序按需启动的容器。`restart: unless-stopped` 则会在开机时启动它，并一直占用显卡，却不做任何事情。

在后端（*Settings → Extension Settings → Audiobook Settings*）中填入服务器地址；如果服务器在本机或你的局域网中，勾选 *The server is on this machine or on my network*，然后点击 **Test connection**。你期望看到的结果是 *the server answers, the model is loaded*。
扩展通过 `/tts` 与服务器通信（完整参数包括：voice、language、exaggeration、CFG weight、temperature、speed、seed），并使用 `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference`。如果你与服务器之间有代理，它必须转发所有这些请求。

<a id="higgs-audio-v3-in-comfyui"></a>

## 在 ComfyUI 中使用 Higgs Audio v3

将语音模型设为 **Higgs Audio v3 in ComfyUI** 的后端，并不与 Chatterbox-TTS-Server 通信，而是与安装了 [TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) 节点的 [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) 通信。其地址即 ComfyUI 的地址（例如 `http://gpu-server:8188`）。它是在同一块 AMD R9700 上，于 `yanwk/comfyui-boot:rocm` 镜像中配置的。

1. **安装该套件**到 ComfyUI 的 `custom_nodes` 文件夹中（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）。其 Python 软件包会安装到容器中。若要在容器重建后保留它们，请为 pip 指定一个位于数据卷上的文件夹：在容器的环境变量中添加 `PIP_USER=true`，并挂载 `./storage/local:/root/.local`。
2. **绝不能让 pip 替换镜像中的 torch**（CUDA 版本会替换 ROCm 版本）。将已安装的版本写入一个文件，并以该文件作为约束进行安装：
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   不要整体运行该套件的 `install.py`，它可能会重新安装 torch。`librosa` 不在其 `requirements.txt` 中，而没有它，该套件将无法加载。
3. **模型**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（约 8 GB），或让套件在首次使用时自行下载。它需要显卡约 8 GB 的显存。
4. 当 ComfyUI 和节点都已就绪时，后端中的 **Test connection** 会显示 *the server answers*，并附带节点的模型信息。

扩展对此的处理方式如下：它将角色的音频片段上传到 ComfyUI 的输入文件夹（`POST /upload/image`，文件名由片段生成），使用 `GET /object_info` 列出套件的声音和片段，为每个片段向 `/prompt` 提交一个它自己的工作流，在 `/history` 上等待，并从 `/view` 获取音频。若 Marginalia 一侧安装了 ffmpeg，则以 flac 格式获取音频，在最后一个词之后裁剪，淡出，并存储为 mp3 或 wav；若没有 ffmpeg，则获取 ComfyUI 生成的 mp3，且不做裁剪（此时 wav 存储无法使用）。**每个片段之后，它都会以 `{"free_memory": true}` 调用 `POST /free`**：在 R9700 上，模型已加载的情况下连续处理第三个片段会导致显卡崩溃（`HSA_STATUS_ERROR_EXCEPTION`，ComfyUI 退出）；在片段之间清空内存可使模型保持加载状态，并避免崩溃，代价是每个片段约多耗时 3 秒。ComfyUI 在一个批次前后的启动和停止，使用与语音服务器相同类型的请求（[这些请求](#the-requests)）。

**在 AMD 显卡上使用 fp32。** 将精度设为 `auto`（在搭载 ROCm 7.2 的 R9700 上为 bf16）时，许多片段输出为乱码，或变成音效（一句关于砸门的台词变成了十一声敲击），或变成了错误的声音，而更改种子只是改变了运气。同样的片段在 fp32 下则逐字准确。后端的 **Precision** 默认为 fp32，它需要显卡约 16 GB 的显存。该套件的 Granite 语音识别在 bf16 下存在同样的问题（从第二次识别起输出乱码），因此扩展也以 float32 运行它。

**片段清理**（[片段清理](audiobook.md#cleanup-of-clips)）还使用该套件中的另外两个节点 `VocalRemovalNode`（分离模型）和 `VoiceFixerNode`，以及 ComfyUI 的 `LoadAudio` 和 `SaveAudio`；无需另外安装任何东西。套件会在首次使用时下载模型（每个 MelBand Roformer 模型约 900 MB）。在 R9700 上，MelBand 模型（vocals、denoise）和 voice fixer 运行正常，处理一段 16 秒的片段约需 25 秒。UVR 模型则不能用：`UVR-DeNoise` 下载失败（套件的地址返回 404），`UVR-DeEcho-DeReverb` 因 `module 'samplerate' has no attribute 'resample'` 而失败（容器中的 Python 包 `samplerate` 与该套件所需的版本不同）。默认的清理只使用已验证可用的模型。

该套件的 **Fish Audio S2 Pro** 引擎在该显卡上无法工作：其音频输出为无效数值（NaN）。Higgs Audio v3 的权重采用仅限研究和非商业用途的许可证。
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 一块 GPU 同时承载模型与语音

一块能放下语言模型的显卡，装不下语音模型。管理员只需设置一次，入口是 [Admin → GPUs](../administration/gpus.md)：为每个可以加载到该 GPU 上的对象各配置一个 GPU 和一把锁，然后在语言模型的 AI 设置和语音后端中选择对应的锁。任何代理的方案都一样：

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

当 GPU 上加载着语言模型时，语音后端的一个批次（或一个样本）会先执行语音锁的获取步骤，然后处理所有片段；之后 GPU 保持加载语音服务器。下一个发给语言模型的请求会等待该批次结束，执行语音锁的释放步骤（停止服务器），然后继续。连续的多个批次和样本只需付出一次切换的代价。

**顺序不能调换。** 在显卡仍被语言模型占用时启动语音容器，测试中耗尽了主机内存（30 GB 内存、交换空间，然后 OOM killer 结束了其他服务）：必须**先卸载**。第一个失败的获取步骤会结束整个列表，因此不会在显卡已满时启动任何东西；当某个释放步骤失败时，后续的锁根本不会加载（显卡可能仍然被占满），任务随即以该错误失败。无论其中某个释放步骤是否失败，所有释放步骤都会执行。

**Higgs、语音识别以及片段清理共用同一个 ComfyUI 时**，每个各有一把锁，三把锁的步骤相同（获取：卸载语言模型，启动 ComfyUI；释放：停止 ComfyUI）。ComfyUI 会在服务器内存中保留已卸载的模型（fp32 的 Higgs 约 16 GB）；测试中，若在它旁边再加载语音识别，服务器会内存不足。使用三把锁时，每次切换都会重启 ComfyUI，因此任何时候只会加载其中一个。

为什么要*停止*容器，而不是在服务器内部卸载模型：在服务器内部卸载，经过多次模型切换后显卡上仍会残留约 1 GB 被占用的显存；停止容器则会释放全部显存。

<a id="the-steps"></a>

### 步骤

每个步骤包括一个方法、一个地址、请求头、请求体、超时时间以及之后的等待时间（秒）；请求头以加密形式保存，因此可以把代理的密钥写入其中（`Authorization: Bearer ...`）。如果前面有 LiteLLM 统管所有服务，语音锁的步骤如下（*Before 1* 至 *3* 为其获取步骤，*After 1* 为其释放步骤）：

| | 方法与地址 | 请求头 | 请求体 | 作用 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 卸载语言模型；由钩子自己应答，耗时 1 至 2 秒。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 启动语音容器，并在服务器就绪后应答（约 13 秒）。 |
| Before 3（仅当服务器启动时不带模型） | `POST http://litellm:4000/gpu/tts/load` | | | 加载模型（8 至 14 秒；首次需下载时为 50 秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 停止容器（不到一秒）。 |

对于 **Higgs** 后端，*Before 2* 为 `POST http://litellm:4000/gpu/comfy/start`（ComfyUI 就绪时应答，约 15 秒；模型随第一个片段加载），没有 *Before 3*，*After 1* 为 `POST http://litellm:4000/gpu/comfy/stop`。这两个直通端点写在 `tools/gpu-control` 的 README 中。

语言模型会自行恢复：下一个普通请求会让路由器加载它（大模型约需一分钟；若启用了 [prompt cache hook](../prompt-cache.md)，则会恢复你会话的缓存），因此批次结束后无需为它再发任何请求。

不使用 LiteLLM 时，同样的请求发往负责启停容器的程序；唯一的规则就是上述顺序。

<a id="what-answers-those-addresses"></a>

### 这些地址由谁应答

- **`X-Llama-Unload`** 由 LiteLLM 的钩子 `gpu_unload.py` 应答，其代码见 [为其他模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)（不使用缓存钩子也可以运行）。若路由为 `openai/*`，请求体中的模型名称必须以 `openai/` 开头：其他名称会在模型已被卸载*之后*返回 400。
- **`/gpu/tts/start`、`/stop`、`/load`** 是 LiteLLM 的**直通端点**，转发到仓库中的小型控制器 `tools/gpu-control`，它通过名称启动和停止容器。其 README 包含使用 **Docker socket proxy** 的 compose 文件（使控制器只能启动和停止容器，别无其他权限；socket 本身等同于主机 root 权限），以及容易配错的设置：`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。在 LiteLLM 中：

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

  （容器名为 `chatterbox`，`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` 告诉控制器需要等待什么；控制器的令牌保存在它自己的 `.env` 中以及 LiteLLM 的环境变量里，Marginalia 只持有 LiteLLM 的密钥。）
- **所有容器与 LiteLLM 共用一个 Docker 网络**（`docker network create ai`，只需一次）。将 `networks: [default, ai]` 以及外部网络 `ai` 写入每个 compose 文件；手动做的 `docker network connect` 在容器重建时会丢失。

<a id="checking-it"></a>

## 检查是否生效

1. `curl http://<speech server>/api/model-info` 能应答（启动后 `loaded` 为 true）。
2. 后端中的 *Test connection* 应显示模型已加载。如果 GPU 锁是唯一启动服务器的方式，请在选中该锁的状态下测试：*服务器能应答，但未加载模型* 表示服务器刚被停止后又启动，仍需加载模型。
3. 在 **Convert to audio** 中转换一段较短的内容。窗口会显示片段的生成进度；之后语音容器应当已停止，`nvidia-smi` / `nvtop` / `rocm-smi` 显示显卡已空闲。
4. 若模型加载后的首次调用耗时较长，那是引擎在预热（只发生一次，首次运行为 28 秒，之后不再出现）。
<a id="troubleshooting"></a>

## 故障排除

| 症状 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 服务器以空模型状态启动（启动包装脚本只会卸载一次模型）：请在锁的获取步骤中添加步骤 **Before 3**。 |
| 前几段已生成，然后服务器报告 *Model not loaded*，其日志中出现 `Request received for /api/unload` | 模型被 Marginalia 以外的程序卸载（扩展从不调用 `/api/unload`）。通常是容器中的启动包装脚本：它等待服务器响应，然后以“为了让启动更快”为由卸载模型。它的等待可能要到第一段生成之后才结束，因为单线程服务器当时正忙，于是卸载恰好落在批次中间（容器日志中的 `urllib` 超时堆栈即为该包装脚本在等待）。在 compose 文件中要找的是 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`。请将其替换为 `command: ["python3", "server.py"]`：仅启动即可得到已加载的 Turbo 模型，而卸载只是为了在启动后释放显卡，由控制器启动和停止的容器并不需要这一步。要判断是谁发出的请求，请查看扩展的日志（*Admin → Extensions → Log*）：它列出批次前后的每个请求，包括其地址和响应时间，以及段落开始和结束的时间；GPU 锁的步骤记录在应用日志中。如果卸载请求发生在日志显示批次仍在运行时，或来自不在列表中的地址，那就不是扩展发出的。 |
| 批次在中途停止（扩展日志显示 *Cannot reach*，或服务器日志直接中断），而手动重启可以正常工作 | 服务器已终止，通常是被系统因占用过多内存而杀掉（主机上的 `dmesg \| grep -i oom`：一个占用数 GB 的 `python3` 进程）。随后扩展会自行重启它（依次执行批次之后的请求、GPU 锁的释放与获取步骤，然后是批次之前的请求），并重新生成该段一次。如果服务器无法恢复，或马上又消失，批次剩余部分会立即失败，日志会说明原因。为防止泄漏的服务器走到这一步，请在后端中设置 **Restart the server after (pieces)**（例如 100），并在容器上设置 `mem_limit`，使系统只杀掉它。 |
| 转换立即因代理的响应而失败 | GPU 锁的第一个获取步骤失败（密钥错误，或找不到卸载路由）；列表在那里故意停止。 |
| 批次开始时主机内存耗尽 | 语言模型仍占用着显卡：语音锁中缺少卸载步骤，或该步骤不在首位；或者语言模型的 AI 没有使用同一 GPU 的锁。 |
| 日语被读成乱码 | 服务器镜像中缺少 `pykakasi`；该片段的后端还必须是多语言版本。 |
| 通过代理上传音频片段失败 | 代理必须原样传递 multipart 请求体（`/upload_reference`）。尚未通过 LiteLLM pass-through 测试；在其可用之前，请使用服务器的语音文件（`predefined:Name.wav`）。 |
| 另一用户的批次卸载了你的模型 | 同一显卡上的每个后端和 AI 都必须指定同一 [GPU](../administration/gpus.md) 的锁：只有这样它们才会轮流使用。没有锁的后端不会等待任何人。同一显卡上的两个 Marginalia 安装不共享 GPU。 |
