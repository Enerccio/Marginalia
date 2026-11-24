---
label: 推理提供商
order: 980
icon: server
---

# 推理提供商

推理提供商是通往某个模型的连接，包括 API 地址、API 密钥、模型及其限制。每本书使用一个提供商。你可以设置任意多个提供商，例如用大型云端模型写故事，用小型本地模型做实验，并随时在不同提供商之间切换某本书。

提供商属于你的账户。其他用户看不到它们，也看不到你的 API 密钥。API 密钥以加密方式存储（参见 [数据文件夹](administration/index.md#the-data-folder)）。

添加提供商需要权限 **Create own models**，由管理员授予（管理员始终拥有该权限，参见 [权限](administration/permissions.md)）。没有该权限时，你只能使用管理员与你[共享](#shared-providers)的提供商，并且不会显示 **Add Inference Provider** 按钮。

![推理提供商选项卡](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## 支持的 API

Marginalia 支持任何实现了 **OpenAI Chat Completions** 接口（`/v1/chat/completions`，支持流式输出）的 API。大多数服务和本地服务器都支持该接口：

| 服务 | OpenAI 地址 |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

端口为各服务器的默认值；请使用你的服务器实际运行的端口。

!!!warning 端口 8080
Docker 版 Marginalia 同样使用端口 8080。如果你在同一台机器上运行 llama.cpp，请将它启动在其他端口（`llama-server --port 8081`）。
!!!

!!!
使用 [Docker 版本](getting-started/docker-server.md) 时，`localhost` 指的是 Marginalia 容器。若要连接同一台机器上的模型服务器，请使用 `http://host.docker.internal:<port>/v1`，参见[同一台机器上的模型](getting-started/docker-server.md#models-on-the-same-machine)。
!!!
<a id="adding-a-provider"></a>

## 添加提供方

打开 **Inference Providers** 标签页，点击 **Add Inference Provider**。编辑已有提供方用铅笔按钮，删除用垃圾桶按钮。复制按钮（两张叠放的纸）会复制一个提供方，并让你确认新名称（默认为原名称加 *(Copy)*）。副本与原提供方的地址、模型、**API key**（或扩展的设置）、推理设置、token 上限和参数相同，因此在同一服务上添加第二个模型只需取个名字并点击一次。副本不会共享，也没有自己的限额和用量。你只能复制自己的提供方（不能复制管理员共享的提供方，因为你看不到它的密钥），而且只有在你有权创建提供方时才能复制。

对话框顶部的字段：

| 字段 | |
|---|---|
| **Name** | 你为该提供方取的名称，在为书籍选择模型时显示。请包含模型名，例如 `OpenRouter - Claude Sonnet`。 |
| **Type** | API 的类型：**OpenAI Compatible**，或由[扩展](extensions/index.md)提供的 **Extension** 提供方（仅当此类扩展已加载时可选，例如 [Google Gemini](extensions/google-genai.md) 或 [Anthropic Claude](extensions/anthropic.md)）。提供方创建后无法更改类型。 |
| **Provider** | 仅用于类型 **Extension**：选择由哪个扩展提供此提供方。该扩展自己的设置会填入 *Per Type Settings* 标签页。 |

### Per Type Settings

![载入模型列表后的 Per Type Settings 标签页](../images/inference-provider.png)

该标签页比对话框高，可以滚动。下方的字段如下：

![超时、重试、附加参数和 Test Connection](../images/inference-provider-2.png)

| 字段 | |
|---|---|
| **OpenAI URL (ends with /v1)** | API 的基础地址，参见[上面的表格](#supported-apis)。不要包含 `/chat/completions`。 |
| **Api Key** | 服务的密钥。本地服务器通常不检查密钥：将此字段留空，Marginalia 会发送占位符 `none`。 |
| **Model** | 要使用的模型。点击 **Refresh** 从 API 载入模型列表，或自行输入模型 ID（例如 OpenRouter 上的 `anthropic/claude-sonnet-4.5`）。 |
| **Timeout (seconds)** | 等待 API 回应的时间，以及模型开始输出后等待更多文本的时间。默认为 300。推理模型可能需要数分钟才会输出第一段文本；若在此时间内没有收到任何内容，请求会以超时信息失败（模型的思考过程也算作文本，因此持续思考的模型不会被中断）。修改后会在下一次请求时生效，包括正在工作的智能体的请求。 |
| **Retries** | 当 API 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求重试的次数。重试间隔会逐次增加，并且会遵循 API 的 `Retry-After` 响应头。默认为 2，设为 0 即关闭重试。只重试请求的开始部分，已经收到的文本永远不会被重复写入。 |
| **Tokenizer** | Marginalia 如何为该提供方计算文本的 token 数。**Automatic**（默认）会依次尝试服务器的分词端点（OpenAI、llama.cpp、LiteLLM），使用第一个有回应的端点；若都没有回应，则使用本地估算。选择某一项则只使用该项；选择 **LOCAL_TOKKIT** 则使用从不调用服务器的本地估算（当计数会导致服务器加载另一个模型时很有用）。插件提供的分词器也会列在这里；它只会获得地址、模型和文本，不会获得 API 密钥，并且只有在你选择它时才会使用。**Test tokenizer** 会用该提供方将尝试的各个分词器计算一段示例文本，并显示每个分词器的回应。创建提供方时，与地址匹配的插件分词器会被预先选中，直到你自行选择为止。 |
| **Send the thinking back** | 模型自己的思考内容是否在下一次请求中回传给它，参见 [Send the thinking back](#send-the-thinking-back)。仅适用于 OpenAI 兼容的提供方：Anthropic 和 Google 提供方会自行发送其 API 所需的内容。 |
| **Additional Parameters** | 一个 JSON 对象，包含发送给该提供方的每个请求都附带的额外字段，例如用于本地服务器的 `{"top_k": 40, "min_p": 0.05}`，或用于 OpenRouter 的 `{"provider": {"order": ["anthropic"]}}`。在此设置的字段优先于[协议](protocols.md)中的同名设置（例如 `temperature`）或回应长度上限（`max_completion_tokens`）。必须是有效的 JSON 对象或留空。 |

编辑已有提供方时，API 密钥不会再次显示。除非点击 **Reset Api Key** 并输入新密钥，否则它会保持原样。

**Refresh** 会使用表单中的地址和密钥向 API 请求模型列表（`/v1/models`）。如果失败并显示 *Failed to download model list*，请检查地址（必须以 `/v1` 结尾）、密钥，以及服务器是否正在运行。有些服务不提供模型列表，此时请直接输入模型 ID。

**Test Connection** 会检查表单中的值，但不保存：它先向 API 请求模型列表，然后用所选 **Model** 请求一个 token 的简短回答，且不重试。显示成功信息即表示地址、密钥和模型可以正常协同工作。失败时会指出原因（参见[错误信息](#error-messages)）。有些服务没有模型列表，此时测试只需完成那一个 token 的回答即可。

### General Settings

![推理提供方的 General Settings](../images/inference-provider-general.png)

| 字段 | |
|---|---|
| **Max Context Size** | 必填。模型总共能接受的 token 数，包括提示词和回应。 |
| **Max Response Tokens** | 必填。模型可以写出的最长回应。会以 `max_completion_tokens` 的形式发送给 API，并且上下文中会为回应保留同样多的空间。必须小于 *Max Context Size*。 |
| **Connections** | 同时可以有多少个请求发往该模型：所有用户的、所有类型的请求（你的生成、辅助查询、翻译、标注、[智能体](agents.md#one-model-several-agents)、插件）都计算在内。其余请求按到达顺序排队等待；需要等待的生成会显示 *Waiting for the model: N requests before this one*，点击 **Stop** 可将其移出队列。默认为 **1**，适用于只有一个插槽（一块 GPU）的本地服务器；能同时处理多个请求的云端模型可以设置更大的值，使辅助查询可以与生成同时进行。 |
| **GPU lock** | 仅当管理员设置了 [GPU](administration/gpus.md) 时可用：即该模型所用 GPU 的锁，适用于显卡与其他模型或图像、语音服务器共享的情况。此时每个请求都会按顺序等待其轮次（与其前面的请求一起计数），并且当 GPU 上运行着其他内容时，会先释放它，再通过锁定步骤加载此模型。留空表示不使用锁。 |
| **Multimodal (accepts images)** | 模型能理解图像，因此一个回合所用的[lorebook 条目图像](lorebooks/lorebooks.md#editing-in-full-screen)会与其文本一起发送。默认关闭：只有在模型支持图像时才应开启，因为不支持视觉的模型会拒绝请求，导致该回合失败。Lore 块中条目的图像会放在系统提示词之后的一条单独消息中（并附带模型的一段简短回答），其他图像则随你的提示词发送。 |
| **Image placement** | （与 *Multimodal* 一同使用）图像的放置位置。*In the system message*（仅限 OpenAI 兼容）：Lore 块的图像会放入系统消息本身，与 lore 文本放在一起；只有服务器和聊天模板支持在此处接收图像时才适用，大多数都不支持，请求会失败。*Before the text of the user message*：每张图像放在其所属消息的文本之前，适用于 Gemma 4 的要求。*After the text of the user message*（默认）：放在文本之后。音频和文件始终放在文本之后。 |
| **Function definitions** | 模型在哪里读取它可以调用的函数（适用于[智能体](agents.md)、辅助查询和提供函数的插件）。*In the system message*（默认）：仅作为请求的 tools 发送，由模型的聊天模板放入系统回合。*Before* / *After the text of the user message*：同时也会在最新一条用户消息的开头或结尾完整写出。tools 始终会一并发送，因为服务器需要它们来识别调用。 |
| **Function call rule**、**Rule placement** | 添加到每个提供函数的请求中的一条规则，适用于那些以文本形式在回应中写出调用、而不是实际发起调用，或者会中途停止调用的模型。例如：*You act only through function calls. Never write a call as text in your reply; to use a function, call it.*。放置位置决定它放在哪里：系统消息，或最新一条用户消息的开头或结尾（默认）；Gemma 4 在用户消息中遵循得好得多。留空则不添加规则。 |
| **Needs Jailbreak**、**Jailbreak Prompt** | 为拒绝编写某些内容的模型准备的附加提示词。勾选后，jailbreak 提示词会被放在整个提示词的最前面。 |
| **Enable Reasoning**、**Reasoning Effort** | 适用于推理（“思考”）模型，参见[推理模型](#reasoning-models)。 |

Marginalia 会在上下文中为回应保留 **Max Response Tokens** 的空间，并用提示词填满其余部分：模板、[lorebook](lorebooks/index.md) 条目和[摘要](books/summaries.md)都会计入其中，而放不下的最早的故事部分会被省略。例如，上下文为 32 000 个 token、回应最多 2 000 个 token 时，提示词可使用 30 000 个 token。

**Max Context Size** 请参考服务的文档，并将其设置得略低一些：token 计数并不总是精确的，超出真实上限的请求会失败。[协议](protocols.md)可以为某些书籍降低这两个上限，但不能提高它们。
<a id="error-messages"></a>

## 错误信息

当 API 拒绝请求时，Marginalia 会说明应当如何修正，而不是显示内部错误，随后显示 API 本身返回的内容：

![向用户显示的提供方错误](../images/error-dialog.png)

| 信息 | 原因 |
|---|---|
| *The provider rejected the API key.* | **Api Key** 错误、已过期或缺失。 |
| *The API key is not allowed to use this model or endpoint.* | 密钥有效，但无权使用该模型。 |
| *The provider does not know the configured model.* | **Model** 错误；使用 **Refresh** 查看 API 认可的模型名称。 |
| *Nothing was found at the configured URL.* | **OpenAI URL** 错误；它通常以 `/v1` 结尾。 |
| *The prompt does not fit into the context of the model.* | **Max Context Size** 高于模型实际接受的上限。请降低该值。 |
| *The provider is limiting requests.* | 触发速率限制或配额已用尽，即使在重试之后仍是如此。 |
| *The provider failed to process the request.* | 服务出现问题（5xx），即使在重试之后仍是如此。 |
| *The provider did not answer in time.* | 提高 **Timeout (seconds)**，或检查服务器。 |
| *Cannot connect to the provider.* | 服务器未运行或 Marginalia 无法访问，或者 URL 有拼写错误。 |
| *The provider rejected the request.* | 其他拒绝原因，通常是 **Additional Parameters** 中包含不受支持的字段，或模型不支持所启用的推理功能。 |

<a id="warning-before-sending"></a>

## 发送前的警告

Marginalia 只会把上下文填充到 **Max Context Size** 减去 **Max Response Tokens** 的量。如果最终提示词仍然超过这个值（例如扩展添加了文本，或 token 计数有偏差），则会在发送请求前显示一条警告，列出估计的大小。请求仍会照常发送；API 可能会拒绝它，或截断提示词。

![提示词可能超出上限的警告](../images/context-warning.png)

<a id="reasoning-models"></a>

## 推理模型

推理模型在回答之前会先进行思考。勾选 **Enable Reasoning** 后，Marginalia 会把所选的 **Reasoning Effort**（*None*、*Low*、*Medium* 或 *High*）以 `reasoning_effort` 的形式发送给 API。投入的力度越大，通常文本规划得越好，但响应也越慢、成本越高。并非所有 API 都支持该参数；如果在启用推理的情况下请求失败，请将其关闭。

当模型返回其推理内容时（以 `reasoning_content` 或 `reasoning` 的形式，取决于服务器），它会显示在生成部分的上方，位于 **View Reasoning** 之下。推理内容不属于故事的一部分，也不会回传给模型，除非你设置了 **Send the thinking back**（见下文）。

### Send the thinking back

**Send the thinking back**（位于 OpenAI 兼容提供方的 *Per Type Settings* 中）决定模型的思考内容是否在下一次请求中回传给它，作为其先前消息的 `reasoning_content`：

| 值 | |
|---|---|
| **None**（默认） | 从不回传。适用于大多数模型和服务器，也适用于拒绝未知字段的 API。 |
| **Between tool calls** | 仅在同一回答的工具调用持续进行时回传：调用工具的模型会在工具结果中一并收到自己的思考内容。你发送的新消息会从不含旧思考内容开始。这是 Google 对 Gemma 4 的要求（*thoughts must not be removed between the function calls*，但在回合之间可以移除）。 |
| **Always** | 对于保留了思考内容的更早回答也回传（Arc Outline 的剧情向导就是这样）。如果服务器的模板会自行丢弃旧的思考内容（例如 Gemma 4 的模板），则忽略此设置。 |

它适用于工具循环（剧情向导、使用工具的生成器）以及代理：只要同一轮的工具调用仍在继续，代理就会收到其早先轮次的思考内容（思考内容保存在控制台中的消息里，而不是存储的消息里）。服务器必须接受该字段：llama.cpp 支持（它读取的是 `reasoning_content`，而不是 `reasoning`），通过 LiteLLM 使用时同样支持；如果提供方拒绝未知的消息字段而导致失败，则将其保持为 *None*。

![已生成部分的推理内容](../images/reasoning-view.png)

推理内容计入响应长度：对于推理模型，请设置更大的 **Max Response Tokens**，否则在长篇推理之后，文本可能会被截断。

<a id="token-counting"></a>

## Token 计数

Marginalia 会计算 token，以决定有多少故事内容可以放入提示词。它会在可能的情况下向提供方请求计数，并按以下顺序尝试各种方法：

1. OpenAI 的 token 计数端点（`/v1/responses/input_tokens`），
2. llama.cpp 的 `/tokenize` 端点，
3. LiteLLM 的 `/tokenize` 端点，
4. LiteLLM 的 `/v1/messages/count_tokens` 端点（适用于 Anthropic 模型），
5. 使用 OpenAI 分词器进行的本地估算。

第一个成功的方法会被此后一直使用。本地估算对大多数模型来说已较为接近，但并不精确，这也是为什么应当将 **Max Context Size** 设置得略低于实际上限的另一个原因。*Extension* 类型的提供方按其扩展所定义的方式计数；[Google Gemini](extensions/google-genai.md) 和 [Anthropic Claude](extensions/anthropic.md) 的提供方则会请求提供方进行计数。

<a id="prompt-cache"></a>

## 提示词缓存

Marginalia 为每一类工作（新的部分、批注、重写、摘要……）发出的每个请求都会带有 `X-Session-ID` 请求头，这是一个哈希值，对于同一用户和同一类工作来说是相同的。保留提示词缓存的服务器或代理可以借助它找到同类上一个请求的缓存。不认识该请求头的服务器会忽略它。参见 [Prompt cache and session id](prompt-cache.md)，其中有一个将 llama.cpp 的单个槽位保存到磁盘的示例。

<a id="shared-providers"></a>

## 共享提供方

在多用户的安装环境中，管理员可以**共享**一个提供方：在提供方的对话框中勾选 **Shared with all users**（该复选框仅对管理员可见）。之后，每位用户都会在其书籍的 **Model** 列表以及 **Inference Providers** 选项卡中找到该提供方，并在 *Shared* 列中标记为 **Yes**。他们可以使用它，但不能打开它、查看其地址或 API 密钥、修改或删除它；只有其所有者可以。引用共享提供方的书籍、备份和恢复对所有能看到它的人都保持有效。

![普通用户的 Inference Providers 选项卡中的共享提供方](../images/provider-shared-user.png)

共享提供方意味着其他用户会消耗所有者在该服务上的配额。若要阻止用户添加自己的提供方，使其只能使用共享的提供方，请参见 [Users and providers](administration/users.md#users-and-providers)。
## Limits of shared providers

An administrator can limit what each user may do with a shared provider. In the **Inference Providers** tab, the
administrator who owns a shared provider has a **Limits** button (a user with a lock) in its row, which opens a table with
the limits that were set. **Add limit** creates a new one: pick the user and a limit type.

![The limits of a shared provider](../images/provider-limits.png)

| Limit type | Meaning |
|---|---|
| Unlimited | No limit (the same as having none). |
| Token limit | The user can use up to the given number of tokens per period. |
| Not available | The user can not use the provider; anything that would send a request to it fails. |

A **token limit** has a **reset** (daily, weekly from Monday, or monthly; calendar periods in the time zone of the
server) and three limits that can each be left empty for no limit: the tokens **sent**, the tokens **generated**, and
both together (**in total**). Whichever is reached first stops the user until the next reset. If all three are set, the
sent plus the generated limit must not be below the total (the total could then never be reached), otherwise the dialog
refuses to save. For example a total of 100,000 with 100,000 sent and 20,000 generated is fine, a total of 100,000 with
20,000 sent and 10,000 generated is not.

![Adding a limit](../images/provider-limit-edit.png)

Nothing is reset on a timer: the tokens used start from zero when the user uses the provider (or looks at their limits)
for the first time in a new period. The size of a request is known before it is sent, so a request whose prompt would
take the user over the **sent** or **in total** limit is refused (the prompt is measured with Marginalia's own estimate,
which can differ a little from the service's count). The tokens **generated** are not known in advance: they are counted
when the request ends, so the user is stopped only once the limit is reached and the last response can go over it. Users
without a limit in the table are not limited.

To keep the generated tokens from going over, check **Hard limit** (only available with a limit of the generated tokens).
A request reserves room for the largest response it allows (the **Max Response Size** of the provider, or the lower one of
the protocol), and with a hard limit it is refused when that reserve does not fit into what is left of the generated
limit. So a hard limit lower than the response size of the provider refuses every request; the table and the user's
info show *hard* next to such a limit. The hard limit only covers the generated tokens, the **in total** limit can still be
gone over by the last response.

Every user sees the shared providers in their tab as read-only rows. The **info** button (the circle with an *i*) of a
shared provider shows their own limit: what they used in this period, the limits and when they reset. When a limit is
reached, the story editor, summaries and extensions show an error instead of sending the request.

![The limits a user sees, with what was used in this period](../images/provider-limit-info.png)

## Usage

The **Inference Providers** tab shows, per provider, the **Tokens sent** (the prompt, "prefill") and **Tokens generated** (the
response, reasoning included) you have used with it, added up over all your books, summaries and extensions. A shared
provider has a count of its own for each user, so you see only your part.

Administrators also get a **Usage per user** button on the tab, which opens a table with what every user has used of every
provider (tokens sent, tokens generated, requests and the time of the last one).

![Usage per user](../images/provider-usage.png)

The numbers are what the service reports. A service that doesn't report them (or a request you stop in the middle)
is counted with the local estimate described in [Token counting](#token-counting), so those numbers are approximate.
A request that fails before any text arrived isn't counted. Unless an administrator has set a
[limit](#limits-of-shared-providers) for you on a shared provider, the counts are only informational.

!!!
Marginalia asks the service to report the token counts (`stream_options` with `include_usage`). To change that for a
provider, put your own `stream_options` into its **Additional Parameters**, for example
`{"stream_options": {"include_usage": false}}`: it replaces what Marginalia sends, and the local estimate is used.
!!!

## Using a provider in a book

Each book has its model in the **Model** field of its **About** tab. To use a provider for all new books, select it
as **Default Model** in the **Settings** tab. Changing the provider of a book takes effect with the next generated
part; parts written before stay as they are.

## Troubleshooting

| Problem | What to check |
|---|---|
| *Failed to download model list* | URL ends with `/v1`, API key, the server is running and reachable from Marginalia. Type the model ID if the service has no model list. |
| *The provider did not answer in time.* | Raise **Timeout (seconds)** of the provider; reasoning models may need minutes. |
| *The provider is limiting requests.* | Wait, or raise **Retries**. |
| *Book is missing model.* | The book has no provider. Select one in **About → Model**. |
| *Contextual limit not sufficient.* | The prompt without any story doesn't fit into the context minus the response tokens. Raise **Max Context Size** (or the protocol's **Max Context Tokens**), lower **Max Response Tokens**, or shorten the templates and lorebook entries. |
| Text stops in the middle | **Max Response Tokens** (or the protocol's **Max Reply Tokens**) is too low, especially for reasoning models. |
| Errors from the API with reasoning on | The API doesn't support `reasoning_effort`. Turn off **Enable Reasoning**. |
