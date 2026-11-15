---
label: 设置语音服务器
order: 28
---

# 为有声书设置语音服务器

[有声书](audiobook.md)扩展需要一个 **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)，基于 FastAPI，MIT 许可证），并且扩展必须能够通过 HTTP 访问它。本页介绍服务器端。以下设置在一台机器上测试过（一块 32 GB 的 AMD Radeon AI PRO R9700，前面是 LiteLLM 代理，`llama-server` 以路由模式运行）；你的硬件和代理可能需要其他取值。

<a id="which-setup-is-yours"></a>

## 你属于哪种情况？

| 你的情况 | 要做什么 |
|---|---|
| 语音服务器运行在**另一台机器**上、使用 CPU，或运行在一块显存足以与**语言模型**并存的显卡上 | 运行服务器，把它的地址填入一个后端，**不要选择 GPU 锁**。无需其他操作。 |
| 语音服务器与语言模型**共用一块显卡**，且两者放不下 | 你需要一个 [GPU 锁](../administration/gpus.md)：它的**获取**步骤会释放显卡并启动语音服务器，它的**释放**步骤会将其停止。请阅读本页其余部分。 |
| 语言模型是云端模型 | 无需释放任何资源：把语音服务器放在任何你想要的地方，不要选择 GPU 锁。 |

所以：**只有当两个模型在同一块 GPU 上轮流使用时，才需要前置和后置请求。** 正是它们实现了轮流使用：插件会围绕一个*批次*（即你一键启动的全部内容）发出这些请求一次，而不是围绕每个部分各发一次。

<a id="1-the-speech-server"></a>

## 1. 语音服务器

以容器方式（或从源代码）运行 Chatterbox-TTS-Server，使 `GET /api/model-info` 能够响应。以下事项很重要：

- **模型。** Turbo（`chatterbox-turbo`）是扩展的默认模型，在测试中音质最好；只有当文本语言与书籍语言不同时，才需要多语言模型。模型在服务器的 `config.yaml` 中设置（或 `POST /save_settings` 和 `POST /restart_server`）。后端的*Speech model*必须与服务器在其 GPU 锁的获取步骤之后加载的模型一致。
- **日语需要镜像中有 `pykakasi`。** 没有它，服务器会记录 *pykakasi not available - Japanese text processing skipped*，并且会错误地读出汉字。请把 `RUN python3 -m pip install --no-cache-dir pykakasi` 添加到它的 Dockerfile 中（放在 `COPY . .` 之前）。希伯来语、俄语和中文的软件包也存在同样的无声跳过问题。
- **声音**是服务器 `reference_audio/` 文件夹中的文件（挂载该文件夹，以便数据得以保留）。扩展会在每个批次之前自行上传你角色的音频片段（`POST /upload_reference`），文件名由片段的哈希值生成。服务器没有删除接口，因此片段会在那里不断累积。
- 服务器**没有身份验证**。不要公开其端口；如果它不在私有网络中，请在它前面放置一个代理。
- `restart: "no"` 适用于由其他程序按需启动的容器。`restart: unless-stopped` 会在开机时启动它，并且会白白占用显卡。

在后端（*Settings → Extension Settings → Audiobook Settings*）中填入其地址；如果服务器位于本机或你的局域网中，勾选 *The server is on this machine or on my network*，然后点击 **Test connection**。你期望看到的结果是：*the server answers, the model is loaded*。
扩展会与 `/tts` 通信（完整参数包括：声音、语言、exaggeration、CFG weight、temperature、speed、seed），以及 `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference`。如果你与服务器之间有代理，代理必须全部转发这些请求。

<a id="higgs-audio-v3-in-comfyui"></a>

## ComfyUI 中的 Higgs Audio v3

语音模型为 **Higgs Audio v3 in ComfyUI** 的后端会与一个带有 [TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI) 节点的 [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite) 通信，而不是与 Chatterbox-TTS-Server 通信。其地址为 ComfyUI 的地址（例如 `http://gpu-server:8188`）。它是在同一块 AMD R9700 上、在 `yanwk/comfyui-boot:rocm` 镜像中设置的。

1. **安装该套件**到 ComfyUI 的 `custom_nodes` 文件夹中（`git clone https://github.com/diodiogod/TTS-Audio-Suite`）。其 Python 软件包会安装到容器内。为了在容器重建后保留它们，请为 pip 指定一个位于数据卷上的文件夹：在容器的环境变量中添加 `PIP_USER=true`，并挂载 `./storage/local:/root/.local`。
2. **绝不要让 pip 替换镜像中的 torch**（CUDA 版本会替换 ROCm 版本）。将已安装的版本写入一个文件，并以约束文件的方式安装：
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   不要整体运行该套件的 `install.py`：它可能会重新安装 torch。`librosa` 不在其 `requirements.txt` 中，而没有它套件将无法加载。
3. **模型**：`hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`（约 8 GB），或者让套件在首次运行时自行下载。它需要约 8 GB 的显卡显存。
4. 当 ComfyUI 和该节点都已就绪时，后端中的 **Test connection** 会显示 *the server answers*，并附带该节点的模型。

扩展对此的处理方式如下：它会将角色的音频片段上传到 ComfyUI 的输入文件夹（`POST /upload/image`，文件名由片段生成）；使用 `GET /object_info` 列出套件的声音和音频片段；为每个片段向 `/prompt` 提交一个它自己的工作流；通过 `/history` 等待结果，并从 `/view` 获取音频：若 Marginalia 一侧安装了 ffmpeg，则以 flac 格式获取，裁掉最后一个词之后的部分，淡出，并存储为 mp3 或 wav；若没有 ffmpeg，则获取 ComfyUI 输出的 mp3，不做裁剪（此时 wav 存储无法使用）。**每个片段之后，它都会调用 `POST /free`，并附带 `{"free_memory": true}`**：在 R9700 上，已加载模型连续处理第三个片段时会导致显卡崩溃（`HSA_STATUS_ERROR_EXCEPTION`，ComfyUI 退出）；在片段之间清空内存可以使模型保持加载状态并避免该问题，每个片段大约增加 3 秒。ComfyUI 会在一个批次前后被启动和停止，所用的请求与语音服务器的同类请求相同（[相关请求](#the-requests)）。

**在 AMD 显卡上使用 fp32。** 使用精度 `auto`（在 ROCm 7.2 的 R9700 上为 bf16）时，许多片段输出为乱码，或变成音效（一句关于敲打的台词变成了十一声敲击），或使用了错误的声音，而改变种子只是改变了运气。相同的片段使用 fp32 时则逐字无误。后端的 **Precision** 默认为 fp32；它需要约 16 GB 的显卡显存。套件中的 Granite 语音识别在 bf16 下存在同样的问题（从第二次识别起输出乱码），因此扩展也以 float32 运行它。

套件中的 **Fish Audio S2 Pro** 引擎在该显卡上无法工作：其音频输出为无效数值（NaN）。Higgs Audio v3 的权重采用仅限研究和非商业用途的许可证。
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 一块 GPU 同时承载语言模型和语音

显卡装下了语言模型，就没有空间再放语音模型。管理员需要一次性在 [Admin → GPUs](../administration/gpus.md) 中完成切换设置：为每个可以加载到该 GPU 上的对象各配置一个 GPU 和一把锁，然后在语言模型的 AI 设置和语音后端中选择对应的锁。该方案适用于任何代理：

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

语音后端的一个批次（或一个样本）在显卡上装载的是语言模型时，会先执行语音锁的获取步骤，然后生成所有片段；之后显卡保持加载语音服务器的状态。下一次对语言模型的请求会等待该批次结束，执行语音锁的释放步骤（停止服务器），然后继续。连续的多个批次和样本只需付出一次切换的代价。

**顺序不可更改。** 在语言模型仍占用显卡时启动语音容器，在测试中把主机内存占满了（30 GB 内存，然后是交换区，随后 OOM killer 杀掉了其他服务）：必须先卸载。第一个失败的获取步骤会结束整个列表，因此不会在已满的显卡上启动任何东西；当某个释放步骤失败时，下一把锁根本不会被加载（显卡可能仍然是满的），任务会以错误结束。无论其中某个释放步骤是否失败，所有释放步骤都会执行。

为什么要*停止*容器，而不是在服务器内部卸载模型：在服务器内部卸载后，经过多次模型切换，显卡上仍会残留约 1 GB 的占用；停止容器则会释放全部显存。

<a id="the-steps"></a>

### 各个步骤

每个步骤包含一个方法、一个地址、请求头、请求体、超时时间，以及之后的等待时间（秒）。请求头以加密形式保存，因此可以把代理的密钥写入其中（`Authorization: Bearer ...`）。在 LiteLLM 前面部署所有服务时，语音锁的步骤如下（*Before 1* 到 *3* 是其获取步骤，*After 1* 是其释放步骤）：

| | 方法和地址 | 请求头 | 请求体 | 作用 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 卸载语言模型；由钩子自身应答，耗时 1 到 2 秒。 |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 启动语音容器，并在服务器就绪后应答（约 13 秒）。 |
| Before 3（仅当服务器启动时不带模型） | `POST http://litellm:4000/gpu/tts/load` | | | 加载模型（8 到 14 秒；首次需下载时为 50 秒）。 |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 停止容器（不到一秒）。 |

对于 **Higgs** 后端，*Before 2* 为 `POST http://litellm:4000/gpu/comfy/start`（在 ComfyUI 就绪后应答，约 15 秒；模型随第一个片段加载），没有 *Before 3*，*After 1* 为 `POST http://litellm:4000/gpu/comfy/stop`。这两个透传端点的说明见 `tools/gpu-control` 的 README。

语言模型会自动恢复：下一次普通请求会让路由器加载它（大模型约需一分钟；若使用[提示缓存钩子](../prompt-cache.md)，则会恢复你会话的缓存），因此批次结束后无需再发送请求来恢复它。

不使用 LiteLLM 时，同样的请求会发送给启动和停止你容器的程序；唯一需要遵守的规则就是上述顺序。

<a id="what-answers-those-addresses"></a>

### 这些地址由谁应答

- **`X-Llama-Unload`** 由 LiteLLM 的钩子 `gpu_unload.py` 应答，其代码在 [为另一个模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model) 中有说明（不带缓存钩子也可运行）。若使用路由 `openai/*`，请求体中的模型名称必须以 `openai/` 开头：其他名称会在模型已被卸载*之后*返回 400。
- **`/gpu/tts/start`、`/stop`、`/load`** 是 LiteLLM 的**透传端点**，指向仓库中的小型控制器 `tools/gpu-control`，它按名称启动和停止容器。其 README 中提供了带有 **Docker 套接字代理**的 compose 文件（控制器只能启动和停止容器，别无其他权限；套接字本身在主机上具有 root 权限），以及容易出错的设置：`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。在 LiteLLM 中：

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

  （容器名为 `chatterbox`，`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` 告诉控制器需要等待什么；控制器的令牌保存在它自己的 `.env` 以及 LiteLLM 的环境变量中，Marginalia 只持有 LiteLLM 的密钥。）
- **所有容器和 LiteLLM 共享一个 Docker 网络**（`docker network create ai`，只需一次）。将 `networks: [default, ai]` 和外部网络 `ai` 写入每个 compose 文件：手动完成的 `docker network connect` 会在容器重建时丢失。

<a id="checking-it"></a>

## 检查是否生效

1. `curl http://<speech server>/api/model-info` 能正常应答（启动后 `loaded` 为 true）。
2. 后端中的 *Test connection* 会显示模型已加载。如果 GPU 锁是唯一启动服务器的方式，请在选中该锁的情况下进行测试：*服务器有应答，但未加载模型* 是刚停止后又重新启动、但仍需加载模型的服务器给出的应答。
3. 在 **Convert to audio** 中转换一段较短的文本。窗口会显示各个片段的生成进度；完成后，语音容器应已停止，`nvidia-smi` / `nvtop` / `rocm-smi` 会显示显卡已重新空闲。
4. 如果模型加载后的第一次调用耗时较长，那是引擎在预热（仅一次，首次运行时为 28 秒，之后不再出现）。
<a id="troubleshooting"></a>

## 故障排除

| 症状 | 原因 |
|---|---|
| *没有加载任何模型* / 503 "TTS engine model is not currently loaded" | 服务器以空模型启动（一个启动包装器只会卸载一次模型）。请在锁的获取步骤中加入步骤 **Before 3**。 |
| 最初几段已生成，随后服务器报 *Model not loaded*，其日志中出现 `Request received for /api/unload` | 除 Marginalia 之外的其他程序卸载了模型（扩展从不调用 `/api/unload`）。通常是容器中的启动包装器：它等待服务器响应，然后以"让启动更快"为由卸载模型。由于单线程服务器当时正忙，它的等待可能要到第一段生成之后才结束，于是卸载恰好落在批次中间（容器日志中的 `urllib` 超时回溯就是该包装器在等待）。在 compose 文件中应查找的写法是 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`。请将其替换为 `command: ["python3", "server.py"]`：仅启动就能得到已加载的 Turbo，而卸载只是为了在启动后保持显卡空闲，由控制器启动和停止的容器并不需要这一步。要判断是谁发出的请求，请查看扩展的日志（*Admin → Extensions → Log*）：它列出批次前后的每个请求，包括其地址和应答时间，以及各段开始和结束的时间；GPU 锁的各步骤则记录在应用日志中。如果卸载发生在日志显示批次正在运行时，或来自不在列表中的地址，则不是扩展发出的。 |
| 批次中途停止（扩展日志显示 *Cannot reach*，或服务器日志直接中断），手动重启后可以正常工作 | 服务器已崩溃，通常是被系统因占用过多内存而杀掉（主机上的 `dmesg \| grep -i oom`，一个占用数 GB 内存的 `python3` 进程）。随后扩展会自行重启服务器（批次之后的请求、GPU 锁的释放和获取步骤，然后是批次之前的请求），并重新生成该段一次。如果服务器无法恢复，或再次立即消失，批次的其余部分会立即失败，日志会说明原因。为防止泄漏内存的服务器走到这一步，请在后端中设置 **Restart the server after (pieces)**（例如 100），并在容器上设置 `mem_limit`，使系统只杀掉它。 |
| 转换立即以代理的应答失败 | GPU 锁的第一个获取步骤失败（密钥错误，或找不到卸载路由）。列表在此处有意停止。 |
| 批次开始时主机内存耗尽 | 语言模型仍在显卡上：语音锁中缺少卸载步骤，或它不在第一位；或者语言模型的 AI 没有与同一 GPU 对应的锁。 |
| 日语被读成乱码 | 服务器镜像中缺少 `pykakasi`；该段的后端还必须是多语言版本。 |
| 通过代理上传音频片段失败 | 代理必须原样传递 multipart 请求体（`/upload_reference`）。尚未通过 LiteLLM 的直通模式测试；在其可用之前，请使用服务器的语音文件（`predefined:Name.wav`）。 |
| 另一用户的批次卸载了你的模型 | 同一块显卡上的每个后端和 AI 都必须指定同一 [GPU](../administration/gpus.md) 的锁，只有这样它们才会轮流使用。没有锁的后端不会等待任何人。同一块显卡上的两个 Marginalia 安装实例不共享 GPU。 |
