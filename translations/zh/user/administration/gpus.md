---
label: GPU
order: 84
---

# GPU

显卡通常一次只能装载一个模型：语言模型、图像模型或语音模型之一。管理员页面的 **GPUs** 标签页让同一块显卡上的模型和服务器轮流使用显卡，这样当语言模型占满显卡时，图像或有声书不会启动；当需要语言模型时，它会重新装载回来。

这项设置只需由管理员做一次，之后每位用户都可以为自己的 AI 或后端选择一个锁。

<a id="what-it-is-made-of"></a>

## 组成部分

| | |
|---|---|
| **GPU** | 一块显卡（或任何一次只能装载一个模型的机器）。只有它的名称。 |
| **GPU lock** | 使用某块 GPU 的一种方式：它上面装载了什么。例如，同一块 GPU 上的 *Language model*、*ComfyUI* 和 *Speech*。锁包含两个有序的步骤列表：**acquire steps** 负责装载它（启动容器、装载模型、卸载必须先退出的内容），**release steps** 负责把显卡从它那里释放出来（停止容器、卸载模型）。**Jobs at once** 限制同一个锁下能同时运行的任务数（一次只生成一张图的图像服务器设为 1；设为 0 表示不限制）。 |
| **GPU step** | 一次 HTTP 请求：地址、方法、请求头（`Name: value`，每行一个，加密保存，因此可以在这里写入 bearer 密钥）、请求体、超时时间（留空为 60 秒），以及收到应答后的等待时间。一个步骤可以被多个锁共用，例如图像锁和语音锁都先运行的同一个 *unload the language model* 步骤。**Test** 会立即发出该请求。 |

在以下位置选择锁：

- [AI 的设置](../inference-providers.md)（通用设置中的 **GPU lock**），用于语言模型；
- [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images) 的后端；
- [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech) 的后端。

只有当至少存在一个锁时，才会显示 **GPU lock** 选择框。

<a id="how-the-turns-work"></a>

## 轮流使用的方式

- 每个发往模型的请求，以及每个图像和有声书的批次或样本，都会在其运行期间占用所属 AI 或后端的锁。
- 如果 GPU 已经用该锁装载好，任务会立即开始，不运行任何步骤。
- 如果 GPU 当前用另一个锁装载，任务会等待，直到该锁下的任务全部完成。随后运行当前锁的 **release steps**（全部运行，即使其中某一步失败），再运行新锁的 **acquire steps**（按顺序；第一个失败的步骤会停止后续步骤，任务以该步骤的消息失败）。
- 如果某个 release 步骤失败，新锁根本不会被装载（显卡可能仍然被占满）：任务以错误失败，GPU 保持原来的锁。另一个锁的下一个任务会再次尝试释放显卡。
- 任务完成后，GPU 会**保持装载**其锁。系统不会按计时器自动卸载：图像窗口即使被打开后保留、折叠或关闭，也不会释放 GPU，图像模型会一直装载，直到语言模型（或其他锁）需要显卡。
- 任务按到达顺序等待。一旦某个任务开始等待另一个锁，已装载锁的新任务就会排在它后面，因此繁忙的语言模型不会让图像无限期等待。
- 没有锁的 AI 或后端，或者锁已被删除的 AI 或后端，不需要等待任何东西。

应用程序只在运行期间记录 GPU 上装载了什么：重启之后，系统不知道任何东西被装载，第一个任务只会运行 acquire 步骤。

<a id="the-tab"></a>

## 标签页

该标签页列出各个 GPU，显示它当前装载的锁（如果尚未运行过或已被卸载，则显示 *Unknown*），以及正在运行和等待它的任务数量；随后列出锁和步骤，每项都带有添加、编辑和删除按钮。**Refresh** 会重新读取各 GPU 当前装载的内容。

**Unload now** 用于手动释放 GPU，例如想把显卡用于 Marginalia 之外的任务：它会等待正在运行的任务结束，运行已装载锁的 release 步骤，并把 GPU 的状态设为未知（即使某个步骤失败也是如此；消息会说明是哪一步）。下一个任务会重新装载它的锁。

删除 GPU 会同时删除它的锁；删除某个步骤会把它从所有使用它的锁中移除。引用了已删除锁的 AI 和后端会在没有锁的情况下运行。

<a id="an-example-one-card-three-models"></a>

## 示例：一块显卡，三个模型

假设 `llama-server` 放在 LiteLLM 后面，并且有一个 [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) 风格的代理，用来启动和停止容器：

| 步骤 | 请求 |
|---|---|
| Unload the language model | 带有请求头 `X-Llama-Unload: all` 的 `POST http://litellm:4000/v1/chat/completions`（参见 [为其他模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)） |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Start speech / Stop speech | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| 锁 | Acquire 步骤 | Release 步骤 | Jobs at once |
|---|---|---|---|
| Language model | （无：路由器会在下一个请求时装载它） | （无） | 0 |
| ComfyUI | Unload the language model, Start ComfyUI | Stop ComfyUI | 1 |
| Speech | Unload the language model, Start speech | Stop speech | 1 |
| Clip cleanup | Unload the language model, Start ComfyUI | Stop ComfyUI | 1 |

语言模型的 AI 选择 *Language model*，图像后端选择 *ComfyUI*，语音后端选择 *Speech*。有声书的 [剪辑清理](../extensions/audiobook.md#cleanup-of-clips) 选择 *Clip cleanup*：它运行在与图像相同的 ComfyUI 上，但它有自己的锁，会在两者之间重启 ComfyUI，因此图像模型和清理模型绝不会同时驻留在显存中。聊天之后的一批语音会运行 *Unload the language model* 和 *Start speech*；下一条聊天消息则会先运行 *Stop speech*。
