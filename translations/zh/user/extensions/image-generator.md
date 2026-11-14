---
label: 图像生成器
order: 13
---

# 图像生成器

为故事的某一部分生成一张图像。模型根据该部分内容撰写图像提示词，你可以对其进行编辑，然后由图像后端绘制出图片。你可以反复重新生成，直到满意后接受它——它就会成为该部分的一张图像，与你使用该部分菜单中的 *Images* 手动添加的图像一样（会显示在故事中，并包含在导出内容里）。

该扩展本身不生成图像：它与 **ComfyUI** 服务器通信，或与支持 **OpenAI images API** 的服务器通信（stable-diffusion.cpp、LiteLLM 以及许多托管服务也支持该接口）。

<a id="adding-a-backend"></a>

## 添加后端

*Settings → Extension Settings → Image Generator Settings* 列出了各个后端。点击（+）添加一个，填写内容后保存 *Settings* 页面。

| 设置 | 作用 |
|---|---|
| Name, Type | 窗口中显示的名称。**ComfyUI** 或 **OpenAI images API**。 |
| Address | 服务器的基础地址，例如 ComfyUI 的 `http://localhost:8188`。 |
| Api key | 以 bearer token 的形式发送。它会加密存储，且不会再次显示；输入新的密钥即可替换，或使用 **Remove the key**。 |
| Model | 仅限 OpenAI images API：请求中的 `model`。 |
| Workflow | 仅限 ComfyUI：使用 ComfyUI 中的 **Save (API Format)** 保存的工作流。在需要的位置写入 `{{prompt}}`、`{{negative}}`、`{{seed}}`、`{{width}}` 和 `{{height}}`。 |
| The server is on this machine or on my network | 允许访问本地网络中的地址。若服务器位于云端，请保持关闭：这样它就无法被诱导去调用你自己的机器。 |
| Width, Height, Negative prompt | 窗口打开时的初始值（你可以在窗口中针对单张图像更改它们）。 |
| Timeout | 等待一张图像生成的最长时间。 |
| Release after idle | 参见 [One GPU](#one-gpu-for-the-model-and-the-images)。 |

![设置中的一个后端](../../images/plugin-imagegenerator-settings.png)

**Test connection** 用于检查地址和密钥。

该面板还包含 **Style**（会放在每条提示词前面的风格设定，例如 *watercolor, soft light*，用于定义整本书的画风）以及 **Instruction for the prompt writer**（提供给模型、用于根据某一部分撰写提示词的指令文本）。留空或保持不变则使用内置指令。

<a id="making-an-image"></a>

## 生成图像

![部分菜单中的 Generate image](../../images/plugin-imagegenerator-menu.png)

1. 在某一部分的菜单中选择 **Generate image**。窗口中的模型（默认是本书所设的模型，上次的选择会被记住）开始为该部分撰写提示词；你可以用 **Abort** 将其中止、修改提示词，或点击 **Write the prompt with the model** 让它重新撰写。在重新撰写之前，你可以填写以下内容：
   - **Send lorebook entries** —— 本书的 lorebook（及其子 lorebook）中的条目会像生成正文时一样发送给模型，使其了解角色和地点的外观。当某个条目的过滤条件在该部分中被找到时，该条目就会被发送：过滤条件包括其文本、指令、场景设定、视角以及在场角色，或者你在 *Details* 中填写的内容。始终启用的条目同样会被发送。发送的条目数量仅限于模型上下文所能容纳的范围（会有提示说明有多少条被略去）。该选项的选择会被记住。
   - **Details** —— 图片必须展示什么或应呈现怎样的样子（*at night, close up on her face, the red dress*）；提示词撰写者会遵循这些内容。它仅适用于当前窗口。
2. 选择后端并点击 **Generate**。在 *Size and negative prompt* 下，你可以为当前窗口更改尺寸和负面提示词。
3. 图像随即出现。**Reroll** 会以新的随机种子生成另一张，**Discard** 将其丢弃，**Accept** 将其添加到该部分并关闭窗口。**Abort** 用于中止正在生成的图像（ComfyUI 会被告知中断该任务）。

![窗口中由模型撰写的提示词](../../images/plugin-imagegenerator-prompt.png)

![图像，以及 Accept、Discard 和 Reroll](../../images/plugin-imagegenerator.png)

当前打开的部分会立即在其正文下方显示新图像。

<a id="one-gpu-for-the-model-and-the-images"></a>

## 一块 GPU 同时用于模型和图像

在只有一块小型 GPU 的情况下，语言模型和图像模型无法同时加载。该扩展不管理显存；每个后端可以配置两组请求列表：

- **Before the first image**（生成第一张图像之前）—— 在窗口开始使用该后端时执行一次。例如，向你的代理发送一个请求，卸载语言模型并加载图像模型。
- **After the last image**（最后一张图像之后）—— 在你接受、丢弃或关闭窗口时执行，或者自上一张图像生成后经过了 **Release after idle** 设定的秒数时执行（这样被遗忘的窗口就不会一直占用 GPU；设为 0 则关闭此功能）。对于 ComfyUI，这可以是 `POST <address>/free`，请求体为 `{"unload_models": true, "free_memory": true}`；对于代理，则是重新加载语言模型的请求。

在 LiteLLM 之后的路由模式下使用 `llama-server` 时，*before* 请求可以通过代理卸载语言模型：参见 [Freeing the GPU for another model](../prompt-cache.md#freeing-the-gpu-for-another-model)。

这两个时机都接受一个**请求列表**，各请求依次执行（**Add a request** 添加一个，垃圾桶图标删除一个）。切换模型往往需要多个请求：先卸载语言模型，然后启动图像服务器，图像生成完毕后再将其停止。每个请求包含：地址、方法（默认为 `POST`）、请求头（`Name: value`，每行一个）、请求体、**timeout**（留空为 60 秒，最多 600 秒；对于需要加载大型模型的服务器，请设置更长的时间）以及 **Wait afterwards**（收到响应后暂停的秒数，适用于回复确认后仍处于忙碌状态的服务器）。请求头与 api key 一样加密存储（请求中常会携带 `Authorization: Bearer ...`）。在你反复重新生成期间，后端会一直被占用，因此只有第一张图像需要承担切换的开销。如果模型前面的代理是通过模型名称进行切换的，请将两者都留空。

当 *before* 列表中的某个请求失败时，其余请求不会再执行，图像生成也会停止（提示信息会指明是哪个请求失败，例如 *Request 2 of 3: ...*），以免在仍然占满的 GPU 上加载其他模型。*after* 列表则会**全部**执行，无论其中某个请求是否失败（每个服务器都必须被停止），并且第一个失败会以警告的形式显示。以旧版本单个请求保存的后端，会将其显示为列表中的第一个请求。

**一块 GPU 上的多个用户。** 所有后端服务器地址相同（`http://host:port`）的用户都会经过同一个闸门：第一个开始使用的窗口会执行 *before* 请求，最后一个结束的窗口会执行 *after* 请求。因此，关闭一个窗口不会在另一个窗口的图像生成过程中卸载模型，并且图像一次只生成一张（第二个窗口需排队等待）。请求内容以先进入和最后离开的用户为准。

后端还可以指定其所使用的 GPU（**GPU (name)**）。即使地址不同，指定相同 GPU 的后端也会相互等待，这同样适用于 [Audiobook](audiobook.md) 的后端（同一块显卡上的语音服务器和图像服务器）：当某用户的后端需要另一个模型时，它会等待其他任务完成后再执行自己的请求，而不会加入一个并非其所需的加载过程。若留空，则只有发往同一地址的请求会相互等待。

<a id="what-is-not-done"></a>

## 未包含的内容

- 后端是按用户设置的，而非按书籍设置的。
