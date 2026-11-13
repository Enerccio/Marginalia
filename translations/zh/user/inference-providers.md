---
label: 推理服务提供方
order: 980
icon: server
---

# 推理服务提供方

推理服务提供方（Inference provider）是与模型的一条连接，包含 API 地址、API 密钥、模型及其各项限制。每本书使用一个提供方。你可以添加任意数量的提供方，例如用大型云端模型写故事，用小型本地模型做实验，并随时在不同提供方之间为某本书切换。

提供方属于你的账户。其他用户看不到它们，也看不到其 API 密钥。API 密钥以加密形式存储（见 [数据文件夹](administration/index.md#the-data-folder)）。

添加提供方需要 **Create own models** 权限，该权限由管理员授予（管理员始终拥有此权限，见 [权限](administration/permissions.md)）。没有此权限时，你只能使用管理员[共享](#shared-providers)给你的提供方，此时不会显示 **Add Inference Provider** 按钮。

![Inference Providers 标签页](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## 支持的 API

Marginalia 支持任何实现了 **OpenAI Chat Completions** 接口（`/v1/chat/completions`，支持流式输出）的 API。大多数服务和本地服务器都支持该接口：

| 服务 | OpenAI 地址 |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM 代理 | `http://localhost:4000/v1` |
| llama.cpp（`llama-server`） | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

表中端口为各服务器的默认值，请使用你的服务器实际运行的端口。

!!!warning 端口 8080
Docker 版本的 Marginalia 同样使用 8080 端口。如果你在同一台机器上运行 llama.cpp，请让它使用其他端口启动（`llama-server --port 8081`）。
!!!

!!!
使用 [Docker 版本](getting-started/docker-server.md)时，`localhost` 指的是 Marginalia 容器本身。若模型服务器运行在同一台机器上，请使用 `http://host.docker.internal:<端口>/v1`，详见[同一台机器上的模型](getting-started/docker-server.md#models-on-the-same-machine)。
!!!
<a id="adding-a-provider"></a>

## 添加推理提供方

打开 **Inference Providers** 标签页，点击 **Add Inference Provider**。编辑已有的提供方请点击铅笔按钮，删除请点击垃圾桶按钮。克隆按钮（两张叠放的纸）会复制一个提供方，并让你确认新名称（原名称后加 *(Copy)*）：副本与原提供方拥有相同的地址、模型、**API key**（或扩展的设置）、推理设置、令牌上限和参数，因此同一服务上的第二个模型只需取个名字并点击一次。副本不与原提供方共享，也没有自己的限额和用量。你只能克隆自己创建的提供方（不能克隆管理员共享的提供方，因为你看不到其密钥），并且只有在你有权创建提供方时才能克隆。

在对话框顶部：

| 字段 | |
|---|---|
| **Name** | 你为该提供方取的名称，在为某本书选择模型时显示。请包含模型名，例如 `OpenRouter - Claude Sonnet`。 |
| **Type** | API 的类型：**OpenAI Compatible**，或者由某个[扩展](extensions/index.md)提供的 **Extension** 提供方（只有在加载了此类扩展时才提供，例如 [Google Gemini](extensions/google-genai.md) 或 [Anthropic Claude](extensions/anthropic.md)）。提供方创建后无法更改类型。 |
| **Provider** | 仅适用于 **Extension** 类型：此提供方属于哪个扩展。其自身的设置会填充到 *Per Type Settings* 标签页中。 |

### Per Type Settings

![Per Type Settings with the model list loaded](../images/inference-provider.png)

该标签页比对话框更高，可以滚动。下方的字段如下：

![Timeout, retries, additional parameters and Test Connection](../images/inference-provider-2.png)

| 字段 | |
|---|---|
| **OpenAI URL (ends with /v1)** | API 的基础地址，参见[上方的表格](#supported-apis)。不要包含 `/chat/completions`。 |
| **Api Key** | 服务的密钥。本地服务器通常不检查密钥：将此字段留空，Marginalia 会发送占位符 `none`。 |
| **Model** | 要使用的模型。点击 **Refresh** 从 API 加载模型列表，或者自己输入模型 ID（例如 OpenRouter 上的 `anthropic/claude-sonnet-4.5`）。 |
| **Timeout (seconds)** | 等待 API 响应的时长，以及在它开始输出后等待更多文本的时长。默认为 300。推理模型在输出第一段文字之前可能需要几分钟；如果在此时长内没有任何内容返回，请求会以超时信息失败（模型的思考过程也算作内容，因此持续思考的模型不会被中断）。修改后的值由下一个请求使用，包括正在工作的智能体的请求。 |
| **Retries** | 当 API 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求重试的次数。两次尝试之间的等待时间会逐渐增加，并且会遵守 API 的 `Retry-After` 响应头。默认为 2，设为 0 则关闭重试。只重试请求的开始部分，已经收到的文本绝不会被重复写入。 |
| **Tokenizer** | Marginalia 如何为该提供方统计文本的令牌数。**Automatic**（默认）会依次尝试服务器的分词端点（OpenAI、llama.cpp、LiteLLM），使用第一个响应的端点；如果都没有响应，则使用本地估算。选择某一项则只使用该端点；选择 **LOCAL_TOKKIT** 则使用从不调用服务器的本地估算（当统计令牌会导致服务器加载另一个模型时很有用）。插件提供的分词器也会列在这里；它只会获得地址、模型和文本，绝不会获得 API 密钥，而且只有在你选择它时才会使用。 **Test tokenizer** 会用该提供方将尝试使用的分词器统计一段示例文本，并显示每个分词器的响应结果。创建提供方时，与该地址匹配的插件分词器会被预先选中，直到你自己做出选择为止。 |
| **Send the thinking back** | 模型是否在下一个请求中收到它自己的思考内容，参见[把思考内容发回](#send-the-thinking-back)。仅适用于 OpenAI 兼容的提供方：Anthropic 和 Google 提供方会自行发送其 API 所需的内容。 |
| **Additional Parameters** | 一个 JSON 对象，包含发送给该提供方的每个请求的额外字段，例如本地服务器的 `{"top_k": 40, "min_p": 0.05}`，或 OpenRouter 的 `{"provider": {"order": ["anthropic"]}}`。这里设置的字段优先于[协议](protocols.md)中的同名设置（例如 `temperature`），也优先于响应长度上限（`max_completion_tokens`）。必须是有效的 JSON 对象，或留空。 |

编辑已有的提供方时，API 密钥不会再次显示。除非你点击 **Reset Api Key** 并输入新的密钥，否则它将保持原样。

**Refresh** 会使用表单中的地址和密钥向 API 请求其模型列表（`/v1/models`）。如果失败并提示 *Failed to download model list*，请检查地址（必须以 `/v1` 结尾）、密钥，以及服务器是否正在运行。部分服务不提供模型列表；此时请直接输入模型 ID。

**Test Connection** 会检查表单中的值，但不保存：它先向 API 请求模型列表，然后用所选的 **Model** 请求一个单令牌的回答，且不进行重试。显示成功消息，说明地址、密钥和模型可以协同工作。失败时会指出原因（参见[错误信息](#error-messages)）。有些服务没有模型列表；此时测试只需完成单令牌回答即可。

### General Settings

![General Settings of an inference provider](../images/inference-provider-general.png)

| 字段 | |
|---|---|
| **Max Context Size** | 必填。模型总共能接受的令牌数，包括提示词和回复。 |
| **Max Response Tokens** | 必填。模型可以写出的最长回复。以 `max_completion_tokens` 发送给 API，并且会从上下文中预留同样多的空间用于回复。必须小于 *Max Context Size*。 |
| **Connections** | 同时可以有多少个请求发往该模型：所有用户、所有类型的请求（你的生成、辅助查询、翻译、注释、[智能体](agents.md#one-model-several-agents)、插件）都计算在内。其余请求按到达顺序排队等待；需要等待的生成会显示 *Waiting for the model: N requests before this one*，**Stop** 可以将其移出队列。默认值 **1** 适用于只有一个插槽（一块 GPU）的本地服务器；能同时处理多个请求的云端模型可以设置更大的值，这样辅助查询就能与生成同时进行。 |
| **Send lore images** | 将一轮生成所用的[lorebook 条目图片](lorebooks/lorebooks.md#editing-in-full-screen)连同其文本一起发送。默认关闭：仅在模型支持图片时开启，不支持视觉的模型会拒绝请求，导致该轮失败。位于 lore 块中的条目图片会放在系统提示词之后的一条独立消息中（附带模型的简短回应），其余图片则随你的提示词一起发送。 |
| **Images in the system message** | （OpenAI 兼容，需同时开启 *Send lore images*）将 lore 块的图片直接放入系统消息中，与 lore 文本放在一起。仅适用于在那里接受图片的服务器和聊天模板；大多数不支持，请求会失败。 |
| **Needs Jailbreak**, **Jailbreak Prompt** | 为那些会拒绝撰写某些内容的模型提供的额外提示词。勾选后，越狱提示词会被放在提示词的最开头。 |
| **Enable Reasoning**, **Reasoning Effort** | 用于推理（“思考”）模型，参见[推理模型](#reasoning-models)。 |

Marginalia 会从上下文中为 **Max Response Tokens** 预留空间用于回复，并用提示词填充其余部分：模板、[lorebook](lorebooks/index.md) 条目和[摘要](books/summaries.md)都会计入其中，装不下的最早故事内容会被省略。若上下文为 32 000 令牌、回复最多 2 000 令牌，则提示词可使用 30 000 令牌。

**Max Context Size** 请取自服务的文档，并将其设得略低一些：令牌计数并不总是精确的，超出真实上限的请求会失败。[协议](protocols.md)可以为某些书籍降低这两项上限，但不能提高它们。
<a id="error-messages"></a>

## 错误信息

当 API 拒绝请求时，Marginalia 会告诉你应该修复什么，而不是显示内部错误，随后显示 API 本身返回的内容：

![向用户显示的服务商错误](../images/error-dialog.png)

| 信息 | 原因 |
|---|---|
| *服务商拒绝了 API 密钥。* | **Api Key** 错误、已过期或缺失。 |
| *该 API 密钥无权使用此模型或端点。* | 密钥有效，但无权访问该模型。 |
| *服务商不认识已配置的模型。* | **Model** 错误；使用 **Refresh** 查看 API 支持的模型名称。 |
| *在已配置的 URL 上没有找到任何内容。* | **OpenAI URL** 错误；通常应以 `/v1` 结尾。 |
| *提示词超出了模型的上下文容量。* | **Max Context Size** 高于模型实际支持的值。请将其调低。 |
| *服务商正在限制请求。* | 达到速率限制或配额已用尽，即使经过重试。 |
| *服务商未能处理该请求。* | 服务出现问题（5xx），即使经过重试。 |
| *服务商未能在规定时间内响应。* | 增大 **Timeout (seconds)**，或检查服务器。 |
| *无法连接到服务商。* | 服务器未运行或 Marginalia 无法访问，或 URL 有拼写错误。 |
| *服务商拒绝了该请求。* | 其他拒绝原因，常见于 **Additional Parameters** 中包含不受支持的字段，或模型不支持所请求的推理功能。 |

<a id="warning-before-sending"></a>

## 发送前的警告

Marginalia 只会把上下文填充到 **Max Context Size** 减去 **Max Response Tokens** 的部分。如果最终提示词仍超过该值（例如扩展添加了文本，或 token 计数有偏差），则在发送请求前会显示一条警告，列出估算的大小。请求仍会照常发送；API 可能会拒绝它，或将提示词截断。

![关于提示词可能超出容量的警告](../images/context-warning.png)

<a id="reasoning-models"></a>

## 推理模型

推理模型会在作答前先进行思考。勾选 **Enable Reasoning** 后，Marginalia 会把所选的 **Reasoning Effort**（*None*、*Low*、*Medium* 或 *High*）以 `reasoning_effort` 的形式发送给 API。投入的推理力度越大，通常生成的文本规划越好，但响应也越慢、成本越高。并非所有 API 都支持该参数；如果开启推理后请求失败，请将其关闭。

当模型返回其推理过程时（以 `reasoning_content` 或 `reasoning` 的形式，取决于服务器），它会显示在生成的部分上方的 **View Reasoning** 下。推理内容不属于故事正文，也不会回传给模型，除非你设置了 **Send the thinking back**（见下文）。

<a id="send-the-thinking-back"></a>

### 回传思考内容

**Send the thinking back**（位于 OpenAI 兼容服务商的 *Per Type Settings* 中）决定模型的思考内容是否在下一次请求中回传给它，作为其较早消息的 `reasoning_content`：

| 值 | |
|---|---|
| **None**（默认） | 从不回传。适用于大多数模型和服务器，以及拒绝未知字段的 API。 |
| **Between tool calls** | 仅在同一回答的工具调用持续进行期间回传：调用工具的模型会在返回结果时收回它自己的思考内容。你发送新消息后，旧的思考内容不再保留。这是 Google 对 Gemma 4 的要求（*在函数调用之间不得删除思考内容*，但在轮次之间可以删除）。 |
| **Always** | 对于聊天中保存了思考内容的较早回答也一并回传（情节向导 Arc Outline 即是如此）。若服务器的模板会自行丢弃旧的思考内容（如 Gemma 4 的模板），则会忽略此设置。 |

该设置适用于工具循环（情节向导、使用工具的生成器）以及智能体：只要同一轮的工具调用仍在进行，智能体就会取回其较早轮次的思考内容（思考内容随控制台中的消息保存，而不是保存在存储的消息中）。服务器必须支持该字段：llama.cpp 支持（它读取 `reasoning_content`，而非 `reasoning`），通过 LiteLLM 使用时同样支持；如果服务商拒绝未知的消息字段而导致失败，请将其设为 *None*。

![已生成部分的推理内容](../images/reasoning-view.png)

推理内容也计入响应长度：对于推理模型，请设置更大的 **Max Response Tokens**，否则长篇推理之后文本可能会被截断。

<a id="token-counting"></a>

## Token 计数

Marginalia 通过计算 token 来决定有多少故事内容能放入提示词。它会在可能时向服务商询问，并按以下顺序尝试各种方法：

1. OpenAI 的 token 计数端点（`/v1/responses/input_tokens`），
2. llama.cpp 的 `/tokenize` 端点，
3. LiteLLM 的 `/tokenize` 端点，
4. LiteLLM 的 `/v1/messages/count_tokens` 端点（适用于 Anthropic 模型），
5. 使用 OpenAI 分词器进行的本地估算。

第一个成功的方法会从此开始一直被使用。本地估算对大多数模型来说已较为接近，但并不精确，这也是为什么要让 **Max Context Size** 略低于实际上限的另一个原因。类型为 *Extension* 的服务商按其扩展的方式计数；[Google Gemini](extensions/google-genai.md) 和 [Anthropic Claude](extensions/anthropic.md) 的扩展则会请求服务商进行计数。

<a id="prompt-cache"></a>

## 提示词缓存

Marginalia 为每一类工作（新的部分、批注、重写、摘要……）发出的每个请求都会带上请求头 `X-Session-ID`，这是一个哈希值，对同一用户和同一类工作保持不变。如果服务器或代理保留了提示词缓存，就可以利用它找到同类上一次请求的缓存。不认识该请求头的服务器会忽略它。参见 [提示词缓存与会话 ID](prompt-cache.md)，其中有一个将 llama.cpp 的一个槽位保存到磁盘上的示例。

<a id="shared-providers"></a>

## 共享服务商

在多用户的安装环境中，管理员可以**共享**一个服务商：在该服务商的对话框中勾选 **Shared with all users**（此复选框仅对管理员可见）。此后，每位用户都能在其书籍的 **Model** 列表中以及其 **Inference Providers** 标签页中找到它，并在 *Shared* 一栏显示为 **Yes**。他们可以使用它，但无法打开它、查看其地址或 API 密钥、修改或删除它；只有其所有者可以这样做。引用共享服务商的书籍、备份和恢复对所有能看到它的用户都将继续有效。

![普通用户的 Inference Providers 标签页中的共享服务商](../images/provider-shared-user.png)

共享服务商意味着其他用户会消耗所有者在该服务上的配额。若要阻止用户添加自己的服务商，使其只能使用共享的服务商，请参见 [用户与服务商](administration/users.md#users-and-providers)。
<a id="limits-of-shared-providers"></a>

## 共享服务商的限额

管理员可以限制每个用户通过共享服务商能做的事情。在 **Inference Providers** 标签页中，拥有共享服务商的管理员可以在其所在行点击 **Limits** 按钮（带锁的用户图标），这会打开一个表格，列出已设置的限额。点击 **Add limit** 可新建一条限额：选择用户和限额类型。

![共享服务商的限额](../images/provider-limits.png)

| 限额类型 | 含义 |
|---|---|
| Unlimited | 无限额（等同于没有设置限额）。 |
| Token limit | 用户在每个周期内最多可使用指定数量的令牌。 |
| Not available | 用户无法使用该服务商；任何会向其发送请求的操作都会失败。 |

**token limit**（令牌限额）有一个 **reset**（重置周期：每天、从周一开始的每周或每月；按服务器时区的日历周期计算），以及三个限额，每个都可以留空表示不限：令牌 **sent**（发送）、令牌 **generated**（生成），以及两者合计（**in total**）。以先达到者为准：一旦达到任一限额，用户将被停止，直到下一次重置。若三个都设置，则 sent 与 generated 限额之和不得小于 total（否则 total 将永远无法达到），否则对话框会拒绝保存。例如，total 为 100,000、sent 为 100,000、generated 为 20,000 是可以的；total 为 100,000、sent 为 20,000、generated 为 10,000 则不行。

![添加限额](../images/provider-limit-edit.png)

限额并非按计时器重置：当用户在新周期内首次使用该服务商（或查看其限额）时，已用令牌数从零开始计算。请求的大小在发送前即可知道，因此若某个请求的提示词会使用户超出 **sent** 或 **in total** 限额，该请求会被拒绝（提示词按 Marginalia 自身的估算计量，可能与服务商的计数略有出入）。生成的令牌（**generated**）无法提前得知：它们在请求结束时才被计数，因此只有在达到限额后用户才会被停止，且最后一次响应可能超出限额。表格中没有限额的用户不受限制。

要防止生成的令牌超出限额，请勾选 **Hard limit**（仅在设置了生成令牌限额时可用）。请求会预留其允许的最大响应所需的空间（即服务商的 **Max Response Size**，或协议中较小的那个）。启用硬限额后，若该预留空间放不进生成限额的剩余额度，请求将被拒绝。因此，若硬限额低于服务商的响应大小，则所有请求都会被拒绝；表格和用户信息会在此类限额旁边显示 *hard*。硬限额只针对生成的令牌，**in total** 限额仍可能被最后一次响应超出。

每个用户都会在其标签页中以只读行的形式看到共享服务商。共享服务商的 **info** 按钮（带有字母 *i* 的圆圈）会显示该用户自己的限额：本周期内已用的量、限额以及重置时间。当达到限额时，故事编辑器、摘要和扩展会显示错误，而不是发送请求。

![用户看到的限额，以及本周期的用量](../images/provider-limit-info.png)

<a id="usage"></a>

## 用量

**Inference Providers** 标签页会按服务商显示你在其中使用过的 **Tokens sent**（提示词，即 "prefill"）和 **Tokens generated**（响应，包括推理内容），并汇总你所有书籍、摘要和扩展中的用量。共享服务商会为每个用户单独计数，因此你只能看到自己的部分。

管理员在该标签页上还会看到 **Usage per user** 按钮，点击后会打开一个表格，列出每位用户对每个服务商的用量（发送的令牌、生成的令牌、请求数以及最近一次请求的时间）。

![按用户统计的用量](../images/provider-usage.png)

这些数字是服务商报告的结果。若服务商不报告这些数字（或你中途停止的请求），则会按 [令牌计数](#token-counting) 中描述的本地估算进行计数，因此这些数字是近似值。在收到任何文本之前就失败的请求不会被计数。除非管理员已在共享服务商上为你设置了[限额](#limits-of-shared-providers)，否则这些计数仅供参考。

!!!
Marginalia 会要求服务商报告令牌计数（即带有 `include_usage` 的 `stream_options`）。若要为某个服务商更改此设置，请在其 **Additional Parameters** 中填入你自己的 `stream_options`，例如 `{"stream_options": {"include_usage": false}}`：它会替换 Marginalia 发送的内容，并使用本地估算。
!!!

<a id="using-a-provider-in-a-book"></a>

## 在书中使用服务商

每本书的模型位于其 **About** 标签页的 **Model** 字段中。若要让所有新书都使用某个服务商，请在 **Settings** 标签页中将其选为 **Default Model**。更改书籍的服务商会从下一次生成的部分开始生效；之前已写好的部分保持不变。

<a id="troubleshooting"></a>

## 故障排除

| 问题 | 检查什么 |
|---|---|
| *Failed to download model list* | URL 是否以 `/v1` 结尾、API 密钥、服务器是否正在运行且 Marginalia 能够访问。若服务没有模型列表，请手动输入模型 ID。 |
| *The provider did not answer in time.* | 提高服务商的 **Timeout (seconds)**；推理模型可能需要几分钟。 |
| *The provider is limiting requests.* | 等待，或提高 **Retries**。 |
| *Book is missing model.* | 该书没有服务商。请在 **About → Model** 中选择一个。 |
| *Contextual limit not sufficient.* | 不含任何故事的提示词，放不进扣除响应令牌后的上下文。请提高 **Max Context Size**（或协议的 **Max Context Tokens**），降低 **Max Response Tokens**，或缩短模板和设定集条目。 |
| 文本在中途停止 | **Max Response Tokens**（或协议的 **Max Reply Tokens**）设置过低，推理模型尤其如此。 |
| 开启推理时 API 报错 | 该 API 不支持 `reasoning_effort`。请关闭 **Enable Reasoning**。 |
