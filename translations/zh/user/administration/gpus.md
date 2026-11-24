---
label: GPU
order: 84
---

# GPU

显卡通常一次只能装载一个模型：语言模型、图像模型或语音模型之一。管理员页面的 **GPUs** 标签页让同一块显卡上的模型和服务轮流使用它，这样图像或有声书不会在语言模型占满显卡时启动，而语言模型在需要时会重新加载。

由管理员一次性设置；之后每位用户都可以为自己的 AI 或后端选择一个锁。

<a id="what-it-is-made-of"></a>

## 组成

| | |
|---|---|
| **GPU** | 一块显卡（或任何一次只能装载一个模型的机器）：它的名称，以及 **Also used outside Marginalia**（见[下文](#a-gpu-that-others-use-too)）。 |
| **GPU lock** | 使用 GPU 的一种方式：显卡上装载了什么。例如同一块 GPU 上的 *Language model*、*ComfyUI* 和 *Speech*。一个锁有两个有序的步骤列表：**acquire steps** 用于装载它（启动容器、加载模型、卸载必须先卸下的东西），**release steps** 用于从显卡上释放它（停止容器、卸载模型）。第三个列表 **steps before every job** 在该锁的每个任务之前运行，无论 GPU 当前是否持有该锁（当 GPU 未持有该锁时，它排在 acquire steps 之前运行）。它用于必须每次都确认的事项，因为其他程序可能已经占用过显卡。**Jobs at once** 限制同一个锁下可同时运行的任务数（一次只生成一张图的图像服务器：1；0 表示不限制）。 |
| **GPU step** | 一个 HTTP 请求：地址、方法、请求头（`Name: value`，每行一个，加密存储，因此可以在此写入 bearer 密钥）、请求体、超时时间（留空：60 秒）以及收到回应后的等待时间。一个步骤可以被多个锁使用，例如图像锁和语音锁都先运行的同一个 *unload the language model* 步骤。**Test** 会立即发出该请求。 |

![锁的窗口](../../images/gpu-lock-dialog.png)

在以下位置选择锁：

- [AI 的设置](../inference-providers.md)（通用设置中的 **GPU lock**），用于语言模型；
- [图像生成器](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images) 的某个后端；
- [有声书](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech) 的某个后端。

只有至少存在一个锁时，才会显示 **GPU lock** 框。

<a id="variables-in-the-steps"></a>

## 步骤中的变量

步骤可以指明任务的用途，这样一个步骤即可服务于多个模型，尝试其他模型时也无需修改锁。以下变量可用于地址、请求头和请求体：

| 变量 | 含义 |
|---|---|
| `{{model}}` | 任务所用的模型：AI 的模型 ID（与 AI 设置中的一致，例如通过 LiteLLM 的 `openai/big-32b`）、有声书后端的模型（`higgs-audio-v3`、`chatterbox-turbo`）、语音识别或图像后端的模型；对于没有模型的任务为空（例如清理片段） |
| `{{lock}}` | 锁的名称 |
| `{{text_model}}` | 仅限文本生成任务：其 AI 的模型 ID（与 `{{model}}` 相同）；语音和图像任务则为空 |
| `{{last_text_model}}` | 每个任务：此锁上最近一次文本生成所用的模型，在该任务开始时记录（因此文本任务能看到自己的模型）；首次文本生成之前以及 Marginalia 重启之后为空 |

过滤器会改变值：`{{model | strip:openai/}}` 在值以某个前缀开头时删除该前缀（LiteLLM 的路由名称，其后端服务器并不识别），`{{model | last}}` 保留最后一个 `/` 之后的内容，`{{model | json}}` 为 JSON 字符串进行转义，`{{model | url}}` 为地址进行编码；它们可以串联使用（`{{model | last | json}}`）。acquire steps 和每个任务前的步骤获得该任务的值；release steps 获得当前已装载的锁上最后一个任务的值（即将被卸载的模型）。**Test** 会在变量为空的情况下运行一个步骤。

<a id="how-the-turns-work"></a>

## 轮流使用的方式

- 对模型的每个请求、每张图像以及每一批或每个有声书样本，都会占用其 AI 或后端的锁，**持续该任务自身的时长**。
- 如果 GPU 已经装载了该锁，任务立即开始，不运行任何步骤，但该锁的 **steps before every job** 除外。
- 如果它装载的是另一个锁，任务会等待该锁的任务全部完成。然后运行该锁的 **release steps**（全部运行，即使其中某个失败），接着运行新锁的 steps before every job 和 **acquire steps**（按顺序；第一个失败会使其停止，任务随之失败，并显示该步骤的消息）。
- 如果某个 release step 失败，新锁根本不会被装载（显卡可能仍然是满的）：任务以该错误失败，GPU 保留原来的锁。下一个使用其他锁的任务会再次尝试释放它。
- 任务结束后，GPU 会**保持装载**其锁。不会按计时器卸载任何东西：无论图像窗口保持打开、被折叠还是已关闭，都不会占用 GPU，图像模型会一直保持装载，直到语言模型（或其他锁）需要这块显卡。
- 任务按到达顺序等待。一旦某个任务开始等待另一个锁，已装载的锁的新任务就会排在它后面，因此繁忙的语言模型不会让图像任务永远等待。
- 没有锁的 AI 或后端，或者所用的锁已被删除，不需要等待任何东西。

GPU 上装载了什么，只有在应用运行期间才是已知的：重启之后，系统不知道任何东西已被装载，第一个任务只运行其 acquire steps。

<a id="a-gpu-that-others-use-too"></a>

## 同时有其他程序使用的 GPU

锁只为 Marginalia 的任务排序。它所知道的显卡装载情况仅来自其自身的任务：Marginalia 重启后，或者其他程序、脚本或人员在显卡上加载了模型（例如唤醒语言模型的路由器请求，或在 ComfyUI 自己的页面中排队的工作流）时，Marginalia 认为已装载的内容就是错误的，任务可能会在已满的显卡上启动。

- 在 GPU 上勾选 **Also used outside Marginalia**，会让 Marginalia 不再信任它所知道的情况：**acquire steps 在每个任务前都会运行**，而不仅仅是在 GPU 切换锁时（release steps 仍然只在切换时运行）。此时，每个锁的 acquire steps 必须自行腾出空间（卸载其他模型、停止其他服务），并且可以安全地重复执行：启动已在运行的服务器、卸载未装载的模型以及停止已停止的容器，都必须返回成功，否则每个任务都会失败。这会让每个任务都额外产生这些请求。
- **steps before every job** 则只针对单个锁执行同样的做法，而不影响其他锁：只有必须每次检查的步骤（卸载其他程序的模型、确认服务器有响应）才会在每个任务前运行，其余步骤仅在切换时运行。
- 对此，最稳妥的位置是 GPU 所在的机器本身，因为每个用户都要经过它：例如，代理的 *start ComfyUI* 先卸载语言模型，路由器钩子则在加载语言模型之前先停止 ComfyUI。这样，无论哪个程序发出请求，都不会有人把显卡重复占满。

<a id="the-tab"></a>

## 标签页

该标签页列出各 GPU 当前装载的锁（尚未运行任何任务或已被卸载时显示 *Unknown*），以及正在运行和等待它的任务数，然后列出锁和步骤，每项都带有添加、编辑和删除按钮。**Refresh** 会重新读取各 GPU 的装载情况。

![带有 GPU、锁和步骤的 GPUs 标签页](../../images/admin-gpus.png)

**Unload now** 手动释放 GPU，例如为了将显卡用于 Marginalia 之外的用途：它会等待正在运行的任务，运行已装载锁的 release steps，并使 GPU 的状态变为未知（即使某个步骤失败也是如此；消息会指明是哪一个）。下一个任务会重新装载其锁。

删除 GPU 会同时删除其锁；删除步骤会将其从运行它的锁中移除。指定了已删除锁的 AI 和后端将在没有锁的情况下运行。
<a id="an-example-one-card-several-models"></a>

## 一个显卡，多个模型的示例

在 `llama-server` 后面接 LiteLLM，并配合一个启动和停止容器的 [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) 风格代理：

| 步骤 | 请求 |
|---|---|
| 卸载语言模型 | 带有请求头 `X-Llama-Unload: all` 的 `POST http://litellm:4000/v1/chat/completions`（参见 [为另一个模型释放 GPU](../prompt-cache.md#freeing-the-gpu-for-another-model)） |
| 启动 ComfyUI / 停止 ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |

如果只由 Marginalia 使用，每个锁只释放它自己的显卡，不会影响其他部分：

| 锁 | 获取步骤 | 释放步骤 | 同时运行的任务数 |
|---|---|---|---|
| 语言模型 | （无：路由器在下一个请求时加载模型） | 卸载语言模型 | 1 |
| ComfyUI 图像 | 启动 ComfyUI | 停止 ComfyUI | 1 |
| ComfyUI 语音 | 启动 ComfyUI | 停止 ComfyUI | 1 |
| ComfyUI 语音识别 | 启动 ComfyUI | 停止 ComfyUI | 1 |
| 片段清理 | 启动 ComfyUI | 停止 ComfyUI | 1 |

聊天之后的一批语音任务会先执行*卸载语言模型*（即释放语言模型的锁），然后执行*启动 ComfyUI*；下一条聊天消息会先执行*停止 ComfyUI*。ComfyUI 的几个锁刻意分开：从一个锁切换到另一个锁时会停止并重新启动 ComfyUI，这正是释放模型在卸载后仍占用内存的办法（fp32 的 Higgs 在服务器内存中仍保留约 16 GB）。因此图像模型、Higgs、语音识别模型和清理模型绝不会同时驻留。

如果使用**同时也在 Marginalia 之外使用**的设置，每个获取操作都必须自行腾出空间：*语言模型*的获取使用*停止 ComfyUI*，每个 ComfyUI 锁的获取使用*卸载语言模型，启动 ComfyUI*。

当另一个程序使用**同一个 `llama-server` 路由器**，并带有它自己的模型（例如在语音模型旁边运行的一个小模型）时，*卸载语言模型*也会把它的模型卸载掉，而它自己的卸载操作也会把 Marginalia 的模型卸载掉。`tools/gpu-control` 提供一个调用，可以卸载路由器中**除指定模型之外**的所有模型：使用请求体 `{"keep": ["{{model | strip:openai/}}"]}` 的步骤 `POST http://litellm:4000/gpu/llama/unload`（其 README 中有透传说明）。请在*语言模型*锁的**每个任务之前**都设置这一步：该锁上的每个 AI 都会保留自己的模型，无论选择哪一个；另一个程序也会在它的每个任务之前对自己的模型执行同样的操作。当被保留的模型是唯一加载的模型时，该调用什么也不做，并立即返回，因此两个程序都无需了解对方的存在。

当**一个锁同时服务语言模型和语音模型**（两者在显卡上并行运行）时，该步骤中的 `{{model}}` 在语音任务期间会是语音模型，而没有路由器模型与之匹配，结果语言模型会在每次回复后立即被卸载。此时应改用 `{{last_text_model}}`，即 `{"keep": ["{{last_text_model | strip:openai/}}"]}`：该锁的每个任务都会保留最近一次文本生成所用的模型，无论任务是什么类型。在第一次文本生成之前不保留任何模型，第一次文本生成会加载它的模型。

对于 ComfyUI，同类步骤是 `POST http://litellm:4000/gpu/comfy/ensure`：当 ComfyUI 有响应时它什么也不做；当容器已停止时它会启动容器；当它在运行但 ComfyUI 没有响应（内部崩溃、挂起）时它会重启容器，然后等待其就绪。作为 ComfyUI 各锁每个任务之前的步骤，它确保 ComfyUI 处于运行状态，无论是谁停止了它。

语言模型的 AI 选择*语言模型*锁，图像后端选择*ComfyUI 图像*锁，语音后端选择*ComfyUI 语音*锁，有声书的[语音识别](../extensions/audiobook.md#speech-recognition)和[片段清理](../extensions/audiobook.md#cleanup-of-clips)各自使用自己的锁。
