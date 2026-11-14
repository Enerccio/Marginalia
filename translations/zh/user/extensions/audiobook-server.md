---
label: 设置语音服务器
order: 28
---

# 为有声书设置语音服务器

[有声书](audiobook.md) 扩展需要一个 **Chatterbox-TTS-Server**（[devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server)，FastAPI，MIT 许可证），并且必须能够通过 HTTP 访问它。本页讲的是服务器端。这里的做法是在一台机器上试验的（AMD Radeon AI PRO R9700，32 GB 显存，所有请求前面挂一个 LiteLLM，`llama-server` 以路由模式运行）；你的硬件和代理可能需要不同的值。

<a id="which-setup-is-yours"></a>

## 你的是哪种配置？

| 你的情况 | 该怎么做 |
|---|---|
| 语音服务器运行在**另一台机器**上、使用 CPU，或运行在一块有空间与**语言模型**并存的显卡上 | 运行服务器，把它的地址填入后端，**将批次前后的请求留空**。无需其他设置。 |
| 语音服务器和语言模型**共用一块显卡**，且两者无法同时容纳 | 你需要批次前后的请求：**前置**请求释放显卡并启动语音服务器，**后置**请求则将其停止。本页其余内容即是说明。 |
| 语言模型是云端模型 | 无需释放资源：语音服务器放在任何你喜欢的位置运行，请求留空即可。 |

因此：**只有当两个模型在同一块 GPU 上轮流使用时，才需要前置和后置请求。** 正是它们实现了这种轮换：插件会在一个*批次*（即你一次点击启动的全部内容）的前后各发出一次请求，而不是在每个部分前后都发。

<a id="1-the-speech-server"></a>

## 1. 语音服务器

以容器方式（或从源代码）运行 Chatterbox-TTS-Server，使 `GET /api/model-info` 能够响应。以下事项很重要：

- **模型。** Turbo（`chatterbox-turbo`）是本扩展的默认模型，在测试中音质最佳；只有当文本的语言与书籍语言不同时才需要多语言模型。模型在服务器的 `config.yaml` 中设置（或使用 `POST /save_settings` 和 `POST /restart_server`）。后端的*Speech model*必须与批次前的请求加载完成后服务器所加载的模型一致。
- **日语需要镜像中包含 `pykakasi`。** 没有它时，服务器会记录 *pykakasi not available - Japanese text processing skipped*，并且会错误地读出汉字。请在其 Dockerfile 中（`COPY . .` 之前）添加 `RUN python3 -m pip install --no-cache-dir pykakasi`。希伯来语、俄语和中文所需的包也存在同样的静默跳过问题。
- **声音**是服务器 `reference_audio/` 文件夹中的文件（请挂载该文件夹，以便数据保留）。扩展会在批次前自行上传你的角色的音频片段（`POST /upload_reference`），文件名由音频片段的哈希值生成。服务器没有删除接口，因此音频片段会不断堆积在那里。
- 服务器**没有身份验证**。不要公开其端口；如果它不在私有网络中，请在其前面放置代理。
- `restart: "no"` 适用于由其他程序按需启动的容器。`restart: unless-stopped` 会在开机时启动它，白白占用显卡。

在后端（*Settings → Extension Settings → Audiobook Settings*）中填入其地址；如果服务器在本机或你的局域网中，请勾选 *The server is on this machine or on my network*，然后点击 **Test connection**：你希望看到的是 *the server answers, the model is loaded*。

扩展通过 `/tts`（完整参数集：声音、语言、exaggeration、CFG weight、temperature、speed、seed）以及 `GET /api/model-info`、`/get_predefined_voices`、`/get_reference_files`、`/upload_reference` 与服务器通信。如果你和服务器之间有代理，它必须转发以上所有接口。

<a id="one-gpu-for-the-model-and-the-speech"></a>

## 模型与语音共用一块 GPU

容纳语言模型的显卡没有空间再容纳语音模型。无论使用哪种代理，方案都相同：

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**顺序不可更改。** 在测试中，当语言模型仍占用显卡时启动语音容器，耗尽了主机内存（30 GB 内存，然后是交换分区，随后 OOM killer 结束了其他服务）：卸载**必须先执行**，并且第一个失败的请求会结束整个列表，这样就不会在显卡已满时启动任何东西。*批次之后*的请求会全部执行，无论其中某个请求发生什么。

为什么要*停止*容器，而不是在服务器内部卸载模型：在服务器内部卸载后，经过多次模型切换，显卡上仍有约 1 GB 被占用；停止容器则能释放全部资源。

<a id="the-requests"></a>

### 请求

列表中的每个请求都包括方法、地址、请求头、请求体，以及之后的等待时间（秒）。不要把密钥写进请求头（表单会按原样显示请求头）：请将其填入 **Key of the requests around a batch**；若该项留空，则会发送后端的 *Api key*。在没有自身密钥的每个请求中，它都会以 `Authorization: Bearer` 的形式发出。在所有请求前面部署 LiteLLM 且只使用一个密钥的情况下，列表如下：

| | 方法与地址 | 请求头 | 请求体 | 作用 |
|---|---|---|---|---|
| 前置 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 卸载语言模型；由钩子本身应答，耗时 1 到 2 秒。 |
| 前置 2 | `POST http://litellm:4000/gpu/tts/start` | | | 启动语音容器，并在服务器就绪后返回响应（约 13 秒）。 |
| 前置 3（仅当服务器启动时没有模型） | `POST http://litellm:4000/gpu/tts/load` | | | 加载模型（8 到 14 秒，首次下载时需 50 秒）。 |
| 后置 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 停止容器（不到一秒）。 |

语言模型会自行恢复：下一个普通请求会让路由器重新加载它（大模型约需一分钟；若启用了 [prompt cache hook](../prompt-cache.md)，则会恢复你会话的缓存），因此批次之后无需为它发送任何请求。

不使用 LiteLLM 时，同样的请求会发送给负责启动和停止你容器的任何程序；唯一的规则就是上述顺序。

<a id="what-answers-those-addresses"></a>

### 这些地址由什么响应

- **`X-Llama-Unload`** 由 LiteLLM 的钩子 `gpu_unload.py` 响应，其代码说明见 [为其他模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)（不依赖缓存钩子也可工作）。若路由为 `openai/*`，请求体中的模型名必须以 `openai/` 开头：使用其他名称时，会在模型已被卸载*之后*返回 400。
- **`/gpu/tts/start`、`/stop`、`/load`** 是 LiteLLM 的**直通端点（pass-through endpoints）**，指向仓库中的小型控制器 `tools/gpu-control`，该控制器按名称启动和停止容器。其 README 中包含带有 **Docker socket proxy** 的 compose 文件（使控制器只能启动和停止容器而不能做其他操作；套接字本身在主机上具有 root 权限），以及容易配错的设置：`POST=1`、`ALLOW_START=1`、`ALLOW_STOP=1`、**`CONTAINERS=0`**。在 LiteLLM 中：

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

  （容器名为 `chatterbox`，`GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` 告诉控制器需要等待哪个地址；控制器的令牌保存在其 `.env` 文件和 LiteLLM 的环境变量中，Marginalia 只持有 LiteLLM 的密钥。）
- **所有容器与 LiteLLM 共用同一个 Docker 网络**（执行一次 `docker network create ai`）。请在每个 compose 文件中加入 `networks: [default, ai]` 以及外部网络 `ai`：手动执行的 `docker network connect` 会在容器重建时丢失。

<a id="checking-it"></a>

## 检查设置

1. `curl http://<speech server>/api/model-info` 有响应（启动后 `loaded` 为 true）。
2. 后端中的*Test connection*显示模型已加载。如果启动服务器的唯一方式是批次前的请求，则应在列表就位的情况下进行测试：*the server answers, no model is loaded* 是一个已停止并刚启动、但仍需加载模型的服务器的应答。
3. 在 **Convert to audio** 中转换一个较短的部分。窗口会显示正在生成的片段；之后语音容器应已停止，并且 `nvidia-smi` / `nvtop` / `rocm-smi` 显示显卡已再次空闲。
4. 如果模型加载后的第一次调用耗时较长，那是引擎在预热（只发生一次，首次运行时为 28 秒，之后不再出现）。
<a id="troubleshooting"></a>

## 故障排除

| 现象 | 原因 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 服务器以空载状态启动（一个启动包装脚本会把模型卸载一次）：添加请求 **Before 3**。 |
| 前几段已经生成，然后服务器报 *Model not loaded*，其日志中出现 `Request received for /api/unload` | 除 Marginalia 之外的其他程序卸载了模型（扩展从不调用 `/api/unload`）：通常是容器中的启动包装脚本，它等待服务器应答，然后以"让启动更快"为由卸载模型。它的等待只可能在第一段之后才结束，因为单线程服务器当时正忙，于是卸载恰好落在批处理中间（容器日志中的 `urllib` 超时回溯即是该包装脚本在等待）。在 compose 文件中要找的模式是 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`。将其替换为 `command: ["python3", "server.py"]`：仅启动即可得到已加载的 Turbo，而卸载只是为了在启动后释放显卡，对于由控制器启动和停止的容器而言并不需要。要判断是谁发出的请求，请查看扩展的日志（*Admin → Extensions → Log*）：它列出批处理前后的每个请求，包括其地址和应答时间，以及段落开始和结束的时间。若卸载发生在日志显示批处理仍在运行期间，或来自列表中没有的地址，则不是扩展发出的。 |
| 批处理在中途停止（扩展日志显示 *Cannot reach*，或服务器日志直接中断），而手动重启后可以正常工作 | 服务器已死亡，通常是被系统因占用全部内存而杀掉（宿主机上执行 `dmesg \| grep -i oom`：会看到一个占用数 GB 的 `python3` 进程）。随后扩展会自行重启它（先发批处理之后的请求，再发之前的请求），并对该段重新生成一次；如果服务器无法恢复，或刚恢复又再次消失，批处理的其余部分会立即失败，日志会说明原因。为防止泄漏内存的服务器走到这一步，请在后端中设置 **Restart the server after (pieces)**（例如 100），并为容器设置 `mem_limit`，使系统只杀掉该容器。 |
| 转换立即失败，并返回代理的应答 | 批处理前的第一个请求失败（密钥错误，或找不到卸载路由）；列表在此处有意停止。 |
| 批处理开始时宿主机内存耗尽 | 语言模型仍在显卡上：卸载请求缺失，或没有排在首位。 |
| 日文被读成乱码 | 服务器镜像中缺少 `pykakasi`；对于该段，后端也必须是多语言版本。 |
| 通过代理上传音频片段失败 | 代理必须原样转发 multipart 请求体（`/upload_reference`）。尚未通过 LiteLLM 透传测试；在问题解决之前，请使用服务器上的语音文件（`predefined:Name.wav`）。 |
| 另一用户的批处理卸载了你的模型 | 同一服务器地址上的批处理在 Marginalia 内部共用一个闸门，用于其他语音模型的批处理会等待其他任务完成。若语音服务器与例如图像服务器共用一块显卡，则两个后端的 **GPU (name)** 都需填写相同的名称，它们才会互相等待（图像生成器的后端也是如此）。同一块显卡上的两个 Marginalia 安装实例不共用闸门。 |
