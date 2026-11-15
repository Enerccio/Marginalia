---
label: GPU
order: 84
---

# GPU

显卡通常一次只能装载一个模型：语言模型、图像模型或语音模型之一。管理员页面的 **GPUs** 标签页让同一块显卡上的模型和服务轮流使用它，这样图像或有声书就不会在语言模型占满显卡时启动，而语言模型需要时又会重新载入。

由管理员一次性设置；之后每位用户为自己的 AI 或后端选择一个锁。

<a id="what-it-is-made-of"></a>

## 组成部分

| | |
|---|---|
| **GPU** | 一块显卡（或任何一次只能装载一个模型的机器）。只需填写其名称。 |
| **GPU lock** | 使用 GPU 的一种方式，即其上装载的内容。例如同一块 GPU 上的 *Language model*、*ComfyUI* 和 *Speech*。锁包含两个有序的步骤列表：**acquire steps** 负责装载（启动容器、加载模型、卸载必须先卸下的内容），**release steps** 负责从显卡上释放它（停止容器、卸载模型）。**Jobs at once** 限制同一锁下同时运行的任务数（一次只生成一张图的图像服务器填 1；填 0 表示不限制）。 |
| **GPU step** | 一个 HTTP 请求：地址、方法、请求头（`Name: value`，每行一个，加密存储，因此可以在此写入 bearer 密钥）、请求体、超时时间（留空：60 秒）以及收到应答后的等待时间。一个步骤可以被多个锁共用，例如图像锁和语音锁都先运行的一个 *unload the language model* 步骤。**Test** 立即发出该请求。 |

选择锁的位置：

- [AI 的设置](../inference-providers.md)（通用设置中的 **GPU lock**），用于语言模型；
- [图像生成器](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images)的后端；
- [有声书](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech)的后端。

只有至少存在一个锁时，才会显示 **GPU lock** 框。

<a id="how-the-turns-work"></a>

## 轮流使用的规则

- 每个发给模型的请求、每张图像以及每个有声书的批次或样本，都会在其运行期间占用所属 AI 或后端的锁。
- 如果 GPU 已经装载了该锁，任务立即开始，不运行任何步骤。
- 如果 GPU 装载的是另一个锁，任务会等待该锁下的任务全部完成。然后运行该锁的**释放步骤**（全部运行，即使其中某一步失败），再依次运行新锁的**装载步骤**（按顺序执行；第一个失败的步骤会停止后续步骤，任务以该步骤的消息失败）。
- 当某个释放步骤失败时，新锁完全不会被装载（显卡可能仍然是满的）：任务以错误失败，GPU 保持原有的锁。之后另一个锁的下一个任务会再次尝试释放它。
- 任务完成后，GPU 会**保持装载**其锁。不会按计时器自动卸载：图像窗口即使被保留打开、折叠或关闭，也不会释放 GPU，图像模型会一直装载，直到语言模型（或其他锁）需要这块显卡。
- 任务按到达顺序等待。一旦某个任务开始等待另一个锁，已装载的锁的新任务就会排在它后面，因此繁忙的语言模型不会让图像永远等下去。
- 没有锁的 AI 或后端，或者锁已被删除的 AI 或后端，不需要等待任何东西。

显卡上装载的内容只在应用运行期间有效：重启后，系统不知道任何东西已被装载，第一个任务只运行其装载步骤。

<a id="the-tab"></a>

## 标签页

该标签页列出各 GPU，显示其当前装载的锁（*Unknown* 表示尚未运行过或已被卸载），以及在其上运行和等待的任务数量；随后列出锁和步骤，每项都有添加、编辑和删除按钮。**Refresh** 重新读取各 GPU 的装载状态。

**Unload now** 手动释放 GPU，例如为了把显卡用于 Marginalia 之外的用途：它会等待正在运行的任务完成，运行当前装载锁的释放步骤，并把 GPU 状态设为未知（即使某个步骤失败也是如此；消息会说明是哪一步）。下一个任务会重新装载其锁。

删除 GPU 会删除其锁；删除步骤会将其从运行它的锁中移除。曾指定已删除锁的 AI 和后端将在没有锁的情况下运行。

<a id="an-example-one-card-three-models"></a>

## 示例：一块显卡，三个模型

当 `llama-server` 位于 LiteLLM 之后，并且使用一个 [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) 风格、可以启动和停止容器的代理时：

| 步骤 | 请求 |
|---|---|
| Unload the language model | `POST http://litellm:4000/v1/chat/completions`，请求头为 `X-Llama-Unload: all`（参见[为其他模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)） |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Start speech / Stop speech | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| 锁 | 装载步骤 | 释放步骤 | 同时运行的任务数 |
|---|---|---|---|
| Language model | （无：路由器会随下一个请求装载它） | （无） | 0 |
| ComfyUI | Unload the language model, Start ComfyUI | Stop ComfyUI | 1 |
| Speech | Unload the language model, Start speech | Stop speech | 1 |

语言模型的 AI 选择 *Language model*，图像后端选择 *ComfyUI*，语音后端选择 *Speech*。一次聊天之后的语音批次会运行 *Unload the language model* 和 *Start speech*；下一条聊天消息则会先运行 *Stop speech*。
