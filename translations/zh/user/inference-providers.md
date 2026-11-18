---
label: 推理提供方
order: 980
icon: server
---

# 推理提供方

推理提供方是与模型的一个连接：包括 API 地址、API 密钥、模型及其限制。每本书使用一个提供方。你可以添加任意多个提供方，例如用于故事的大型云端模型，以及用于试验的小型本地模型，并且可以随时在不同提供方之间切换某本书所用的模型。

提供方属于你的账户。其他用户看不到你的提供方及其 API 密钥。API 密钥以加密形式存储（参见[数据文件夹](administration/index.md#the-data-folder)）。

添加提供方需要权限 **Create own models**，该权限由管理员授予（管理员始终拥有此权限，参见[权限](administration/permissions.md)）。没有该权限时，你只能使用管理员[共享](#shared-providers)给你的提供方；此时不会显示 **Add Inference Provider** 按钮。

![“Inference Providers”标签页](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## 支持的 API

Marginalia 支持任何实现 **OpenAI Chat Completions** 接口（`/v1/chat/completions`，支持流式输出）的 API。大多数服务和本地服务器都支持该接口：

| 服务 | OpenAI URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

上表中的端口是各服务器的默认端口；请使用你的服务器实际运行所用的端口。

!!!warning 端口 8080
Docker 版本的 Marginalia 同样使用端口 8080。如果你在同一台机器上运行 llama.cpp，请让它使用其他端口（`llama-server --port 8081`）。
!!!

!!!
对于[Docker 版本](getting-started/docker-server.md)，`localhost` 指的是 Marginalia 容器。若要访问同一台机器上的模型服务器，请使用 `http://host.docker.internal:<port>/v1`，参见[同一台机器上的模型](getting-started/docker-server.md#models-on-the-same-machine)。
!!!
<a id="adding-a-provider"></a>

## 添加推理提供方

打开 **Inference Providers** 标签页，点击 **Add Inference Provider**。现有提供方通过铅笔按钮编辑，通过垃圾桶按钮删除。复制按钮（两张叠放的纸）会以你确认的名称复制一个提供方，默认名称为原名称加 *(Copy)*：副本与原提供方的地址、模型、**API key**（或扩展的设置）、推理设置、token 上限和参数完全相同，因此在同一服务上使用第二个模型只需取个名字并点击一次。副本不共享，也没有自己的限额和用量。你只能复制自己创建的提供方（不能复制管理员共享的提供方，因为你无法看到其密钥），并且只有在你有权创建提供方时才可以复制。

在对话框顶部：

| 字段 | |
|---|---|
| **Name** | 你为该提供方取的名称，在为某本书选择模型时显示。请包含模型名，例如 `OpenRouter - Claude Sonnet`。 |
| **Type** | API 的类型：**OpenAI Compatible**，或由某个[扩展](extensions/index.md)提供的 **Extension**（仅当此类扩展已加载时才会提供，例如 [Google Gemini](extensions/google-genai.md) 或 [Anthropic Claude](extensions/anthropic.md)）。提供方创建后无法更改类型。 |
| **Provider** | 仅用于 **Extension** 类型：该提供方属于哪个扩展。其自身的设置会填充 *Per Type Settings* 标签页。 |

### Per Type Settings

![已加载模型列表的 Per Type Settings](../images/inference-provider.png)

该标签页比对话框更高，可以滚动。下面是较靠下的字段：

![超时、重试、附加参数和 Test Connection](../images/inference-provider-2.png)

| 字段 | |
|---|---|
| **OpenAI URL (ends with /v1)** | API 的基础地址，参见[上表](#supported-apis)。不要包含 `/chat/completions`。 |
| **Api Key** | 服务的密钥。本地服务器通常不检查密钥：将此字段留空，Marginalia 会发送占位符 `none`。 |
| **Model** | 要使用的模型。点击 **Refresh** 从 API 加载模型列表，或自行输入模型 ID（例如 OpenRouter 上的 `anthropic/claude-sonnet-4.5`）。 |
| **Timeout (seconds)** | 等待 API 响应的时间，以及模型开始输出后等待更多文本的时间。默认值为 300。推理模型可能需要数分钟才能输出第一段文本；若在此时间内没有收到任何内容，请求会以超时信息失败（模型的思考过程也算作文本，因此持续思考的模型不会被中断）。修改后，下一个请求会使用新值，包括正在工作的智能体的请求。 |
| **Retries** | 当 API 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求被重复的次数。两次尝试之间的等待时间会逐渐增加，并且会遵守 API 的 `Retry-After` 响应头。默认值为 2，设为 0 则关闭重试。只重试请求的开始部分，已收到的文本永远不会被重复写入。 |
| **Tokenizer** | Marginalia 如何为该提供方统计文本的 token。**Automatic**（默认）会依次尝试服务器的分词器端点（OpenAI、llama.cpp、LiteLLM），使用第一个有响应的端点；若都没有响应，则使用本地估算。选择其中一个则只使用它；选择 **LOCAL_TOKKIT** 则使用从不调用服务器的本地估算（当计数会让服务器加载另一个模型时很有用）。插件提供的分词器也会列在这里；它只会获得地址、模型和文本，永远不会获得 API 密钥，并且只在你选择它时才会使用。**Test tokenizer** 会用该提供方将尝试的方法统计一段示例文本，并显示每种方法的应答结果。创建提供方时，与地址匹配的插件分词器会被预先选中，直到你自行选择为止。 |
| **Send the thinking back** | 是否在下一个请求中把模型自己的思考内容发回给它，参见[将思考内容发回](#send-the-thinking-back)。仅适用于 OpenAI 兼容的提供方：Anthropic 和 Google 提供方会自行发送其 API 所需的内容。 |
| **Additional Parameters** | 一个 JSON 对象，包含发送给该提供方的每个请求的额外字段，例如本地服务器的 `{"top_k": 40, "min_p": 0.05}`，或 OpenRouter 的 `{"provider": {"order": ["anthropic"]}}`。这里设置的字段优先于[协议](protocols.md)中的同名设置（例如 `temperature`）或响应上限（`max_completion_tokens`）。必须是有效的 JSON 对象，或留空。 |

编辑现有提供方时，API 密钥不会再次显示。除非你点击 **Reset Api Key** 并输入新密钥，否则它将保持不变。

**Refresh** 会使用表单中的地址和密钥向 API 请求模型列表（`/v1/models`）。如果它以 *Failed to download model list* 失败，请检查地址（必须以 `/v1` 结尾）、密钥，以及服务器是否正在运行。有些服务不提供模型列表；此时请直接输入模型 ID。

**Test Connection** 会检查表单中的值但不保存：它会先向 API 请求模型列表，然后用所选 **Model** 请求一个单 token 的回答，且不进行重试。成功消息表示地址、密钥和模型可以协同工作。失败时会说明原因（参见[错误信息](#error-messages)）。有些服务没有模型列表；此时测试只需要单 token 的回答即可。

### General Settings

![推理提供方的 General Settings](../images/inference-provider-general.png)

| 字段 | |
|---|---|
| **Max Context Size** | 必填。模型总共接受的 token 数，包括提示和响应。 |
| **Max Response Tokens** | 必填。模型可以写出的最长响应。它会以 `max_completion_tokens` 发送给 API，并且会从上下文中预留同样多的空间给响应。必须小于 *Max Context Size*。 |
| **Connections** | 同时有多少个请求可以发往该模型：所有用户、所有类型的请求（你的生成、辅助查询、翻译、注释、[智能体](agents.md#one-model-several-agents)、插件）都计算在内。其余请求按到达顺序等待；需要等待的生成会显示 *Waiting for the model: N requests before this one*，**Stop** 可以将其移出队列。默认值为 **1**，适用于只有一个插槽（一块 GPU）的本地服务器；能同时处理多个请求的云端模型可以设置更大的值，以便辅助查询与生成同时运行。 |
| **GPU lock** | 仅当管理员设置了 [GPU](administration/gpus.md) 时可用：当该模型运行的显卡与其他模型或图像、语音服务器共享时，此处为该显卡的锁。此后每个请求都会排队等待其在 GPU 上的轮次（与其前面的请求一同计算），并且，当 GPU 上运行着其他内容时，会先释放它，然后按锁的步骤加载此模型。留空则不使用锁。 |
| **Multimodal (accepts images)** | 将一个轮次使用的[lorebook 条目图片](lorebooks/lorebooks.md#editing-in-full-screen)连同文本一起发送。默认关闭：只有在模型支持图像时才应开启，不支持视觉的模型会拒绝请求，导致该轮次失败。位于 lore 块中的条目图片会放在一条独立消息中，紧接在系统提示之后（模型会给出简短回答），其他图片则随你的提示一起发送。 |
| **Images in the system message** | （OpenAI 兼容，且已启用 *Multimodal*）将 lore 块的图片放入系统消息本身，与 lore 文本放在一起。仅适用于在那里接受图片的服务器和聊天模板；大多数不支持，请求会失败。 |
| **Needs Jailbreak**、**Jailbreak Prompt** | 用于会拒绝编写某些内容的模型的额外提示。勾选后，越狱提示会被放在整个提示的最前面。 |
| **Enable Reasoning**、**Reasoning Effort** | 用于推理（“思考”）模型，参见[推理模型](#reasoning-models)。 |

Marginalia 会在上下文中为 **Max Response Tokens** 预留空间供响应使用，并用提示填满其余部分：模板、[lorebook](lorebooks/index.md) 条目和[摘要](books/summaries.md)都计入其中，放不下的最早故事内容会被省略。例如，上下文为 32 000 token、响应最多 2 000 token 时，提示可获得 30 000 token。

请从服务的文档中获取 **Max Context Size**，并将其设置得略低一些：token 计数并不总是精确的，超出真实上限的请求会失败。[协议](protocols.md)可以为某些书籍降低这两个上限，但不能提高它们。
<a id="error-messages"></a>

## 错误信息

当 API 拒绝请求时，Marginalia 会说明应该修改什么，而不是显示内部错误，随后显示 API 本身返回的内容：

![显示给用户的提供方错误](../images/error-dialog.png)

| 信息 | 原因 |
|---|---|
| *提供方拒绝了 API 密钥。* | **Api Key** 错误、已过期或缺失。 |
| *该 API 密钥无权使用此模型或端点。* | 密钥有效，但无权访问该模型。 |
| *提供方不认识所配置的模型。* | **Model** 错误；使用 **Refresh** 查看 API 所知道的模型名称。 |
| *在所配置的 URL 上没有找到任何内容。* | **OpenAI URL** 错误；它通常以 `/v1` 结尾。 |
| *提示词无法放入模型的上下文。* | **Max Context Size** 高于模型实际接受的值。请将其调低。 |
| *提供方正在限制请求。* | 达到速率限制或配额已用完，即使经过了重试。 |
| *提供方无法处理该请求。* | 服务出现问题（5xx），即使经过了重试。 |
| *提供方未能及时响应。* | 提高 **Timeout (seconds)**，或检查服务器。 |
| *无法连接到提供方。* | 服务器未运行，或 Marginalia 无法访问，或 URL 有拼写错误。 |
| *提供方拒绝了该请求。* | 其他拒绝情况，通常是 **Additional Parameters** 中有不受支持的字段，或模型不支持所使用的推理功能。 |

<a id="warning-before-sending"></a>

## 发送前的警告

Marginalia 只会将上下文填充到 **Max Context Size** 减去 **Max Response Tokens** 的部分。如果完成后的提示词仍然超过该值（例如因为某个扩展添加了文本，或令牌计数有误），则在发送请求之前会显示一条警告，列出估算的大小。请求仍会照常发送；API 可能会拒绝它，或截断提示词。

![关于提示词可能无法放入的警告](../images/context-warning.png)

<a id="reasoning-models"></a>

## 推理模型

推理模型会先思考再作答。勾选 **Enable Reasoning** 后，Marginalia 会将所选的 **Reasoning Effort**（*None*、*Low*、*Medium* 或 *High*）作为 `reasoning_effort` 发送给 API。并非所有 API 都支持该参数；如果启用推理后请求失败，请将其关闭。

当模型返回其推理内容时（根据服务器不同，以 `reasoning_content` 或 `reasoning` 的形式），它会显示在生成部分的上方，位于 **View Reasoning** 之下。推理内容不属于故事的一部分，也不会回传给模型，除非你设置了 **Send the thinking back**（见下文）。

### Send the thinking back

**Send the thinking back**（位于 OpenAI 兼容提供方的 *Per Type Settings* 中）用于设定模型的思考内容是否在下一次请求中回传给它，作为其更早消息中的 `reasoning_content`：

| 值 | |
|---|---|
| **None**（默认） | 从不回传。适用于大多数模型和服务器，以及拒绝未知字段的 API。 |
| **Between tool calls** | 仅在同一次回答的工具调用持续进行时回传：调用过工具的模型会在工具结果中获得其自身的思考内容。你发出的新消息会从不带旧思考内容开始。这是 Google 对 Gemma 4 的要求（*thoughts must not be removed between the function calls*，但在轮次之间可以移除）。 |
| **Always** | 对于聊天中保留了思考内容的更早回答也会回传（Arc Outline 的剧情向导即是如此）。若服务器的模板会自行丢弃旧的思考内容，例如 Gemma 4 的模板，则忽略此设置。 |

该设置适用于工具循环（剧情向导、使用工具的生成器）以及代理：只要同一轮的工具调用仍在继续，代理就会取回其更早轮次的思考内容（思考内容与控制台中的消息一同保存，而不是保存在已存储的消息中）。服务器必须接受该字段：llama.cpp 支持（它读取的是 `reasoning_content`，而不是 `reasoning`），通过 LiteLLM 使用时也是如此；若提供方拒绝未知的消息字段而导致失败，则请保持为 *None*。

![已生成部分的推理内容](../images/reasoning-view.png)

推理内容也计入响应长度：对于推理模型，请设置更大的 **Max Response Tokens**，否则长篇推理之后文本可能会被截断。

<a id="token-counting"></a>

## 令牌计数

Marginalia 会计算令牌数，以决定有多少故事内容能放入提示词。它会在可能时向提供方请求计数，并按以下顺序尝试各种方法：

1. OpenAI 令牌计数端点（`/v1/responses/input_tokens`），
2. llama.cpp 的 `/tokenize` 端点，
3. LiteLLM 的 `/tokenize` 端点，
4. LiteLLM 的 `/v1/messages/count_tokens` 端点（针对 Anthropic 模型），
5. 使用 OpenAI 分词器的本地估算。

第一个可用的方法会从此被沿用。本地估算对大多数模型来说较为接近，但并不精确，这也是为什么应将 **Max Context Size** 设得略低于实际上限的另一个原因。类型为 *Extension* 的提供方按其扩展所述方式计数；[Google Gemini](extensions/google-genai.md) 和 [Anthropic Claude](extensions/anthropic.md) 的提供方则由提供方负责计数。

<a id="prompt-cache"></a>

## 提示词缓存

Marginalia 为每一类工作（新的部分、批注、重写、摘要……）发出的每个请求都会携带请求头 `X-Session-ID`，这是一个哈希值，对同一用户和同一类工作保持不变。保存提示词缓存的服务器或代理可以利用它，找到该类上一次请求的缓存。不认识该请求头的服务器会忽略它。另见 [Prompt cache and session id](prompt-cache.md)，其中有一个将 llama.cpp 的单个插槽保存到磁盘的示例。

<a id="shared-providers"></a>

## 共享提供方

在多用户的安装环境中，管理员可以**共享**提供方：在提供方的对话框中勾选 **Shared with all users**（该复选框仅对管理员可见）。此后，每位用户都能在其书籍的 **Model** 列表和 **Inference Providers** 选项卡中找到该提供方，并标记为 *Shared* **Yes**。他们可以使用它，但无法打开它、查看其地址或 API 密钥、修改或删除它；只有其所有者可以这样做。引用共享提供方的书籍、备份和恢复，对所有能看到它的人都将继续有效。

![普通用户的 Inference Providers 选项卡中的共享提供方](../images/provider-shared-user.png)

共享提供方会让其他用户消耗所有者在该服务上的配额。若要阻止用户添加自己的提供方，使其只能使用共享的提供方，请参阅 [Users and providers](administration/users.md#users-and-providers)。
<a id="limits-of-shared-providers"></a>

## 共享提供方的限制

管理员可以限制每位用户对共享提供方的可用范围。在 **Inference Providers** 标签页中，拥有共享提供方的管理员可以在其行中看到一个 **Limits** 按钮（一个带锁的用户图标），点击后会打开一个表格，显示已设置的限制。点击 **Add limit** 可以新建一条限制：选择用户和限制类型。

![共享提供方的限制](../images/provider-limits.png)

| 限制类型 | 含义 |
|---|---|
| Unlimited | 无限制（与没有限制相同）。 |
| Token limit | 用户在每个周期内最多可使用给定数量的令牌。 |
| Not available | 用户无法使用该提供方；任何会向其发送请求的操作都会失败。 |

**Token limit** 带有一个 **reset**（每日、每周（从周一开始）或每月；按服务器时区计算的日历周期），以及三个上限，每个都可以留空表示不限：**sent**（发送的令牌）、**generated**（生成的令牌），以及两者合计的 **in total**。无论哪个先达到，用户都会被停止使用，直到下一次重置。若三个上限都已设置，则 sent 与 generated 的上限之和不得低于 total（否则 total 将永远无法达到），否则对话框会拒绝保存。例如，total 为 100,000、sent 为 100,000、generated 为 20,000 是可以的；total 为 100,000、sent 为 20,000、generated 为 10,000 则不可以。

![添加限制](../images/provider-limit-edit.png)

没有任何计时器会重置数据：已用令牌从用户在新周期内第一次使用该提供方（或查看其限制）时开始从零计算。请求的大小在发送前即可得知，因此若某个请求的提示会使用户超过 **sent** 或 **in total** 上限，该请求会被拒绝（提示长度使用 Marginalia 自身的估算，可能与服务的计数略有出入）。**generated** 令牌事先无法得知：它们在请求结束时才计数，因此只有在达到上限后用户才会被停止，最后一条回复可能会超出上限。表格中没有限制的用户不受限制。

要防止生成的令牌超出上限，请勾选 **Hard limit**（仅在设置了生成令牌上限时可用）。请求会预留其允许的最大回复长度的空间（提供方的 **Max Response Size**，或协议中较小的那个值）；启用硬限制后，若该预留空间放不进剩余的生成上限，请求会被拒绝。因此，若硬限制低于提供方的回复长度，则每个请求都会被拒绝；表格和用户的信息中会在此类限制旁显示 *hard*。硬限制只针对生成的令牌，**in total** 上限仍可能被最后一条回复超出。

每位用户都会在其标签页中以只读行看到共享提供方。共享提供方的 **info** 按钮（带字母 *i* 的圆圈）会显示该用户自己的限制：本周期内已使用的量、限制以及重置时间。当某项限制被达到时，故事编辑器、摘要和扩展会显示错误，而不是发送请求。

![用户看到的限制，以及本周期内的用量](../images/provider-limit-info.png)

<a id="usage"></a>

## 用量

**Inference Providers** 标签页按提供方显示您在所有书籍、摘要和扩展中已使用的 **Tokens sent**（提示，即 "prefill"）与 **Tokens generated**（回复，包括推理过程）。共享提供方会为每位用户单独计数，因此您只能看到自己的部分。

管理员还会在该标签页上看到 **Usage per user** 按钮，点击后会打开一个表格，显示每位用户对每个提供方的用量（发送的令牌、生成的令牌、请求数，以及最近一次请求的时间）。

![每位用户的用量](../images/provider-usage.png)

数字以服务报告的为准。若服务未报告（或您中途停止的请求），则使用 [令牌计数](#token-counting) 中描述的本地估算进行计数，因此这些数字是近似值。在任何文本到达之前就失败的请求不会被计数。除非管理员在某个共享提供方上为您设置了[限制](#limits-of-shared-providers)，否则这些计数仅供参考。

!!!
Marginalia 要求服务报告令牌计数（`stream_options` 与 `include_usage`）。要更改某个提供方的这一设置，请在其 **Additional Parameters** 中填入您自己的 `stream_options`，例如 `{"stream_options": {"include_usage": false}}`：它会替换 Marginalia 发送的内容，此时将使用本地估算。
!!!

<a id="using-a-provider-in-a-book"></a>

## 在书籍中使用提供方

每本书的模型在其 **About** 标签页的 **Model** 字段中设置。若要让所有新书默认使用某个提供方，请在 **Settings** 标签页中将其选为 **Default Model**。更改书籍的提供方会在下一次生成的部分中生效；之前已写好的部分保持不变。

<a id="troubleshooting"></a>

## 故障排查

| 问题 | 检查什么 |
|---|---|
| *Failed to download model list* | URL 是否以 `/v1` 结尾、API 密钥是否正确、服务器是否在运行且 Marginalia 能否访问。若服务没有模型列表，请手动输入模型 ID。 |
| *The provider did not answer in time.* | 提高该提供方的 **Timeout (seconds)**；推理模型可能需要数分钟。 |
| *The provider is limiting requests.* | 等待，或提高 **Retries**。 |
| *Book is missing model.* | 书籍没有提供方。请在 **About → Model** 中选择一个。 |
| *Contextual limit not sufficient.* | 不含任何故事的提示无法放入上下文减去回复令牌后的空间。请提高 **Max Context Size**（或协议的 **Max Context Tokens**），降低 **Max Response Tokens**，或缩短模板和 lorebook 条目。 |
| 文本在中途停止 | **Max Response Tokens**（或协议的 **Max Reply Tokens**）过低，推理模型尤其如此。 |
| 开启推理时 API 报错 | 该 API 不支持 `reasoning_effort`。请关闭 **Enable Reasoning**。 |
