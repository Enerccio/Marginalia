---
label: 推理提供方
order: 980
icon: server
---

# 推理提供方

推理提供方是到某个模型的连接,包括 API 地址、API 密钥、模型及其限制。每本书使用一个提供方。你可以添加任意多个提供方,例如用云端的大模型写故事,用本地的小模型做实验,并随时把某本书切换到不同的提供方。

提供方属于你的账户。其他用户看不到它们,也看不到你的 API 密钥。API 密钥以加密方式存储(参见[数据文件夹](administration/index.md#the-data-folder))。

添加提供方需要 **Create own models** 权限,由管理员授予(管理员始终拥有该权限,参见[权限](administration/permissions.md))。没有该权限时,你只能使用管理员与你[共享](#shared-providers)的提供方,界面中不会显示 **Add Inference Provider** 按钮。

![推理提供方标签页](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## 支持的 API

Marginalia 可以与任何实现了 **OpenAI Chat Completions** 接口(`/v1/chat/completions`,支持流式输出)的 API 配合使用。大多数服务和本地服务器都支持该接口:

| 服务 | OpenAI URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM 代理 | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

表中的端口是各服务器的默认端口;请使用你的服务器实际运行的端口。

!!!warning 端口 8080
Docker 版本的 Marginalia 同样使用端口 8080。如果你在同一台机器上运行 llama.cpp,请让它使用其他端口(`llama-server --port 8081`)。
!!!

!!!
使用 [Docker 版本](getting-started/docker-server.md)时,`localhost` 指的是 Marginalia 容器。若要连接同一台机器上的模型服务器,请使用 `http://host.docker.internal:<port>/v1`,参见[同一台机器上的模型](getting-started/docker-server.md#models-on-the-same-machine)。
!!!
<a id="adding-a-provider"></a>

## 添加推理提供商

打开 **Inference Providers** 标签页，点击 **Add Inference Provider**。编辑现有提供商请点击铅笔按钮，删除请点击垃圾桶按钮。克隆按钮（两张叠放的纸）会复制一个提供商，你需要确认新名称（原名称加上 *(Copy)*）。副本拥有相同的地址、模型、**API key**（或扩展的设置）、推理设置、令牌上限和参数，因此在同一服务上使用第二个模型时，只需取一个名字并点击一下。副本不与原提供商共享，也没有自己的限额和用量。你只能克隆自己创建的提供商（不能克隆管理员共享的提供商，因为你不应看到其密钥），并且只有在你被允许创建提供商时才能克隆。

对话框顶部的字段：

| 字段 | |
|---|---|
| **Name** | 你为该提供商取的名称，在为一本书选择模型时显示。请包含模型名称，例如 `OpenRouter - Claude Sonnet`。 |
| **Type** | API 的类型：**OpenAI Compatible**，或为某个[扩展](extensions/index.md)提供的提供商所用的 **Extension**（仅当此类扩展已加载时才可选，例如 [Google Gemini](extensions/google-genai.md) 或 [Anthropic Claude](extensions/anthropic.md)）。提供商创建后，类型无法更改。 |
| **Provider** | 仅适用于 **Extension** 类型：该提供商属于哪个扩展。其自身设置会填入 *Per Type Settings* 标签页。 |

### Per Type Settings

![已加载模型列表的 Per Type Settings](../images/inference-provider.png)

此标签页比对话框更高，可以滚动。下方的字段如下：

![Timeout、Retries、附加参数和 Test Connection](../images/inference-provider-2.png)

| 字段 | |
|---|---|
| **OpenAI URL (ends with /v1)** | API 的基础地址，参见[上表](#supported-apis)。不要包含 `/chat/completions`。 |
| **Api Key** | 服务的密钥。本地服务器通常不检查密钥：将此字段留空，Marginalia 会发送占位符 `none`。 |
| **Model** | 要使用的模型。点击 **Refresh** 从 API 加载模型列表，或自行输入模型 ID（例如 OpenRouter 上的 `anthropic/claude-sonnet-4.5`）。 |
| **Timeout (seconds)** | 等待 API 回应的时长，以及模型开始输出后等待更多文本的时长。默认值为 300。推理模型可能需要几分钟才会输出第一段文字；在此时间内没有收到任何内容的请求会以超时信息失败（模型的思考过程也算作文本，因此持续思考的模型不会被中断）。修改后，下一次请求即生效，包括正在工作的智能体的请求。 |
| **Retries** | 当 API 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求被重试的次数。重试间隔会逐渐增加，并遵守 API 的 `Retry-After` 响应头。默认值为 2，设为 0 则关闭重试。只重试请求的开始阶段，已经收到的文本不会被重复写入。 |
| **Tokenizer** | Marginalia 如何为该提供商计算文本的令牌数。**Automatic**（默认）会依次尝试服务器的分词端点（OpenAI、llama.cpp、LiteLLM），采用第一个有回应的结果；若都没有回应，则使用本地估算。选择其中一个则只使用该方式；选择 **LOCAL_TOKKIT** 则使用从不调用服务器的本地估算（当计数会导致服务器加载另一个模型时很有用）。插件提供的分词器也会列在此处；它只会获得地址、模型和文本，永远不会获得 API 密钥，并且只有在你选择它时才会使用。**Test tokenizer** 会用该提供商将尝试的方式统计一段示例文本，并显示每种方式的回应结果。创建提供商时，若有与该地址相符的插件分词器，它会被预先选中，直到你自己做出选择为止。 |
| **Send the thinking back** | 模型在下一次请求中是否会收到它自己的思考内容，参见[回传思考内容](#send-the-thinking-back)。仅适用于 OpenAI 兼容的提供商：Anthropic 和 Google 提供商会自行发送其 API 所需的内容。 |
| **Additional Parameters** | 一个 JSON 对象，包含发送给该提供商的每个请求的额外字段，例如本地服务器的 `{"top_k": 40, "min_p": 0.05}`，或 OpenRouter 的 `{"provider": {"order": ["anthropic"]}}`。此处设置的字段优先于[协议](protocols.md)中的同名设置（例如 `temperature`）或响应上限（`max_completion_tokens`）。必须是有效的 JSON 对象，或留空。 |

编辑现有提供商时，API 密钥不会再次显示。除非你点击 **Reset Api Key** 并输入新密钥，否则它保持不变。

**Refresh** 会使用表单中的地址和密钥向 API 请求模型列表（`/v1/models`）。如果它以 *Failed to download model list* 失败，请检查地址（必须以 `/v1` 结尾）、密钥，以及服务器是否正在运行。部分服务不提供模型列表，此时请直接输入模型 ID。

**Test Connection** 会检查表单中的值，但不保存：它先向 API 请求模型列表，然后用所选 **Model** 请求一个单令牌的回答，且不重试。显示成功信息，表示地址、密钥和模型可以协同工作。失败时会说明原因（参见[错误信息](#error-messages)）。部分服务没有模型列表，此时测试只需完成单令牌回答即可。

### General Settings

![推理提供商的 General Settings](../images/inference-provider-general.png)

| 字段 | |
|---|---|
| **Max Context Size** | 必填。模型总共能接受的令牌数，包括提示词和回应。 |
| **Max Response Tokens** | 必填。模型可以写出的最长回应。会以 `max_completion_tokens` 的形式发送给 API，并为回应保留同样多的上下文空间。必须小于 *Max Context Size*。 |
| **Connections** | 同时发往该模型的请求数量上限：所有用户、所有类型的请求都计算在内（你的生成、旁路查询、翻译、标注、[智能体](agents.md#one-model-several-agents)、插件）。其余请求按到达顺序等待；需要等待的生成会显示 *Waiting for the model: N requests before this one*，**Stop** 可将其移出队列。默认值为 **1**，适用于只有一个槽位（一块 GPU）的本地服务器；能同时处理多个请求的云端模型可以设置更大的值，以便旁路查询与生成同时进行。 |
| **Send lore images** | 将一轮使用的[知识库条目图片](lorebooks/lorebooks.md#editing-in-full-screen)连同其文本一并发送。默认关闭：只有在模型支持图片时才应开启，不支持视觉的模型会拒绝请求，导致该轮失败。位于知识块中的条目图片会放在系统提示词之后紧接着的一条单独消息中（并附带模型的简短回应），其他图片则随你的提示词发送。 |
| **Images in the system message** | （OpenAI 兼容，需同时开启 *Send lore images*）将知识块的图片放入系统消息本身，与知识文本并列。仅适用于在那里接受图片的服务器和聊天模板；大多数不支持，请求会失败。 |
| **Needs Jailbreak**、**Jailbreak Prompt** | 为拒绝编写某些内容的模型提供的额外提示词。勾选后，越狱提示词会被放在提示词的最前面。 |
| **Enable Reasoning**、**Reasoning Effort** | 用于推理（"思考"）模型，参见[推理模型](#reasoning-models)。 |

Marginalia 会为回应保留 **Max Response Tokens** 的上下文空间，并用提示词填满其余部分：模板、[知识库](lorebooks/index.md)条目和[摘要](books/summaries.md)都计入其中，装不下的最早故事内容会被省略。例如，上下文为 32 000 个令牌、回应最多 2 000 个令牌时，提示词可用 30 000 个令牌。

请从服务的文档中获取 **Max Context Size**，并将其设置得略低一些：令牌计数不一定完全精确，超过真实上限的请求会失败。某个[协议](protocols.md)可以为部分书籍降低这两个上限，但不能提高它们。
<a id="error-messages"></a>

## 错误信息

当 API 拒绝请求时，Marginalia 会告诉你该修改什么，而不是显示内部错误，随后显示 API 本身返回的内容：

![向用户显示的提供方错误](../images/error-dialog.png)

| 信息 | 原因 |
|---|---|
| *The provider rejected the API key.* | **Api Key** 错误、已过期或缺失。 |
| *The API key is not allowed to use this model or endpoint.* | 密钥有效，但无权使用该模型。 |
| *The provider does not know the configured model.* | **Model** 错误；使用 **Refresh** 查看 API 已知的模型名称。 |
| *Nothing was found at the configured URL.* | **OpenAI URL** 错误；它通常以 `/v1` 结尾。 |
| *The prompt does not fit into the context of the model.* | **Max Context Size** 高于模型实际接受的值。请降低它。 |
| *The provider is limiting requests.* | 即使经过重试，仍触发速率限制或配额已用尽。 |
| *The provider failed to process the request.* | 服务出现问题（5xx），即使经过重试。 |
| *The provider did not answer in time.* | 提高 **Timeout (seconds)**，或检查服务器。 |
| *Cannot connect to the provider.* | 服务器未运行或 Marginalia 无法访问，或者 URL 拼写有误。 |
| *The provider rejected the request.* | 其他拒绝原因，通常是 **Additional Parameters** 中有不受支持的字段，或模型不支持所请求的推理功能。 |

<a id="warning-before-sending"></a>

## 发送前的警告

Marginalia 只会将上下文填充到 **Max Context Size** 减去 **Max Response Tokens** 的部分。如果最终提示词仍然超过该值（例如某个扩展添加了文本，或 token 计数有偏差），请求发送前会显示一个警告，列出估算的大小。请求仍会发送；API 可能会拒绝它，或截断提示词。

![关于提示词可能超出容量的警告](../images/context-warning.png)

<a id="reasoning-models"></a>

## 推理模型

推理模型在回答之前会先进行思考。勾选 **Enable Reasoning** 后，Marginalia 会将所选的 **Reasoning Effort**（*None*、*Low*、*Medium* 或 *High*）以 `reasoning_effort` 的形式发送给 API。投入的推理越多，通常文本规划越好，但响应也越慢、越昂贵。并非所有 API 都支持该参数；如果启用推理后请求失败，请将其关闭。

当模型返回其推理内容时（以 `reasoning_content` 或 `reasoning` 的形式，取决于服务器），它会显示在生成内容上方的 **View Reasoning** 下。推理内容不属于故事的一部分，也不会回传给模型，除非你设置了 **Send the thinking back**（见下文）。

### Send the thinking back

**Send the thinking back**（位于 OpenAI 兼容提供方的 *Per Type Settings* 中）决定模型自己的思考内容是否在下一次请求中，作为其先前消息的 `reasoning_content` 回传给它：

| 值 | |
|---|---|
| **None**（默认） | 从不回传。适用于大多数模型和服务器，也适用于拒绝未知字段的 API。 |
| **Between tool calls** | 仅在同一回答的工具调用过程中回传：调用过工具的模型会在工具结果中取回它自己的思考内容。你的新消息会从没有旧思考内容的状态开始。这是 Google 对 Gemma 4 的要求（*thoughts must not be removed between the function calls*，但在轮次之间可以移除）。 |
| **Always** | 对于聊天中保留了思考内容的更早回答也回传（例如 Arc Outline 的剧情向导）。如果服务器的模板会自行丢弃旧的思考内容（如 Gemma 4 的模板），则忽略此设置。 |

它适用于工具循环（剧情向导、使用工具的生成器）以及智能体：只要同一轮的工具调用仍在继续，智能体就会取回其更早轮次的思考内容（思考内容随控制台中的消息保存，而不是存入已保存的消息）。服务器必须接受该字段：llama.cpp 支持（它读取的是 `reasoning_content`，而非 `reasoning`），通过 LiteLLM 使用时也支持；如果提供方拒绝未知的消息字段而导致失败，请保持为 *None*。

![生成部分的推理内容](../images/reasoning-view.png)

推理内容会计入响应长度：请为推理模型设置较大的 **Max Response Tokens**，否则长时间推理后文本可能会被截断。

<a id="token-counting"></a>

## Token 计数

Marginalia 通过计算 token 来决定有多少故事内容能放入提示词。它会在可能的情况下向提供方询问，并按以下顺序尝试各种方法：

1. OpenAI 的 token 计数端点（`/v1/responses/input_tokens`），
2. llama.cpp 的 `/tokenize` 端点，
3. LiteLLM 的 `/tokenize` 端点，
4. LiteLLM 的 `/v1/messages/count_tokens` 端点（适用于 Anthropic 模型），
5. 使用 OpenAI 分词器进行本地估算。

第一个成功的方法会被沿用。本地估算对大多数模型来说已经很接近，但并不精确，这也是请将 **Max Context Size** 设置得略低于实际上限的另一个原因。*Extension* 类型的提供方按其扩展的方式计数；[Google Gemini](extensions/google-genai.md) 和 [Anthropic Claude](extensions/anthropic.md) 的提供方则请求提供方进行计数。

<a id="prompt-cache"></a>

## 提示词缓存

Marginalia 为每一类工作（新的部分、批注、重写、摘要……）发出的每个请求都会带上请求头 `X-Session-ID`，这是一个哈希值，对于同一用户和同一类工作保持不变。维护提示词缓存的服务器或代理可以借助它找到同类上一次请求的缓存。不认识该请求头的服务器会忽略它。参见 [提示词缓存与会话 id](prompt-cache.md)，其中有一个将 llama.cpp 的一个槽位保存到磁盘的示例。

<a id="shared-providers"></a>

## 共享提供方

在有多个用户的安装环境中，管理员可以**共享**一个提供方：在该提供方的对话框中勾选 **Shared with all users**（此复选框仅对管理员可见）。之后，每位用户都会在其书籍的 **Model** 列表和 **Inference Providers** 标签页中找到它，并在 *Shared* 列标记为 **Yes**。他们可以使用它，但无法打开它、查看其地址或 API 密钥、修改或删除它；只有其所有者可以这样做。引用共享提供方的书籍、备份和恢复对所有能看到它的用户都会继续有效。

![普通用户的 Inference Providers 标签页中的共享提供方](../images/provider-shared-user.png)

共享提供方会让其他用户消耗所有者在该服务上的配额。若要阻止用户添加自己的提供方，使其只能使用共享的提供方，请参见 [用户与提供方](administration/users.md#users-and-providers)。
<a id="limits-of-shared-providers"></a>

## 共享提供方的限制

管理员可以限制每位用户在共享提供方上能做什么。在 **Inference Providers** 标签页中，拥有共享提供方的管理员，在其所在行有一个 **Limits** 按钮（带锁的用户图标），点击后会打开一个表格，列出已设置的限制。**Add limit** 用于新建限制：选择用户和限制类型。

![共享提供方的限制](../images/provider-limits.png)

| 限制类型 | 含义 |
|---|---|
| Unlimited | 无限制（与没有限制相同）。 |
| Token limit | 用户在每个周期内最多可使用给定数量的 token。 |
| Not available | 用户不能使用该提供方；任何会向其发送请求的操作都会失败。 |

**Token limit** 包含一个 **reset**（每日、每周一起算的每周，或每月；按服务器所在时区的日历周期计算），以及三个限制项，每项都可以留空表示不设限制：**sent**（发送的 token）、**generated**（生成的 token），以及两者合计（**in total**）。先达到的那个限制会使用户被阻止，直到下一次重置。如果三项都设置了，那么 sent 加 generated 的限制不能低于 total（否则 total 永远无法达到），否则对话框会拒绝保存。例如，total 为 100,000、sent 为 100,000、generated 为 20,000 是可以的；total 为 100,000、sent 为 20,000、generated 为 10,000 则不行。

![添加限制](../images/provider-limit-edit.png)

没有计时器自动重置：用户在新周期中第一次使用该提供方（或查看自己的限制）时，已用 token 从零开始计算。请求的大小在发送前即可知道，因此如果某个请求的提示词会使用户超过 **sent** 或 **in total** 限制，该请求会被拒绝（提示词使用 Marginalia 自己的估算方法计算，与服务的计数可能略有差异）。生成的 token 事先无法得知：它们在请求结束时才被计数，因此只有在达到限制后才会阻止用户，最后一次回复可能会超出限制。表格中没有限制的用户不受限制。

为了防止生成的 token 超出限制，可以勾选 **Hard limit**（仅在设置了生成 token 限制时可用）。请求会预留出它允许的最大回复空间（提供方的 **Max Response Size**，或协议中较小的那个值）。启用硬限制后，如果该预留空间放不进生成限制的剩余额度，请求就会被拒绝。因此，硬限制若低于提供方的回复大小，将拒绝每一个请求；表格和用户的信息中会在此类限制旁显示 *hard*。硬限制只针对生成的 token，**in total** 限制仍可能因最后一次回复而被超出。

每位用户都会在其标签页中以只读行的形式看到共享提供方。共享提供方的 **info** 按钮（带字母 *i* 的圆圈）会显示该用户自己的限制：本周期已使用的量、限制内容以及重置时间。当某项限制被达到时，故事编辑器、摘要和扩展会显示错误，而不是发送请求。

![用户看到的限制，以及本周期已使用的量](../images/provider-limit-info.png)

<a id="usage"></a>

## 用量

**Inference Providers** 标签页按提供方显示您已使用的 **Tokens sent**（提示词，即“prefill”）和 **Tokens generated**（回复，包括推理内容），并汇总您所有书籍、摘要和扩展的用量。共享提供方会为每位用户单独计数，因此您只会看到自己的部分。

管理员还可以在该标签页上使用 **Usage per user** 按钮，打开一个表格，显示每位用户在每个提供方上的用量（发送的 token、生成的 token、请求数以及最近一次请求的时间）。

![按用户统计的用量](../images/provider-usage.png)

这些数字是服务报告的结果。如果服务不报告用量（或您中途停止了某个请求），则会使用[Token 计数](#token-counting)中描述的本地估算进行计数，因此这些数字是近似值。在请求开始返回任何文本之前就失败的请求不会被计数。除非管理员已为您在某个共享提供方上设置了[限制](#limits-of-shared-providers)，否则这些计数仅供参考。

!!!
Marginalia 会要求服务报告 token 计数（`stream_options` 与 `include_usage`）。若要为某个提供方更改此设置，请在其 **Additional Parameters** 中填入您自己的 `stream_options`，例如
`{"stream_options": {"include_usage": false}}`：它会替换 Marginalia 发送的内容，此时将使用本地估算。
!!!

<a id="using-a-provider-in-a-book"></a>

## 在书籍中使用提供方

每本书的模型都在其 **About** 标签页的 **Model** 字段中设置。若要让所有新书都使用某个提供方，请在 **Settings** 标签页中将其选为 **Default Model**。更改书籍的提供方会在下一次生成的部分中生效；之前已写好的部分保持不变。

<a id="troubleshooting"></a>

## 故障排除

| 问题 | 需要检查的内容 |
|---|---|
| *Failed to download model list* | URL 是否以 `/v1` 结尾、API 密钥、服务器是否正在运行且 Marginalia 能够访问。如果服务没有模型列表，请输入模型 ID。 |
| *The provider did not answer in time.* | 提高该提供方的 **Timeout (seconds)**；推理模型可能需要几分钟。 |
| *The provider is limiting requests.* | 稍等片刻，或提高 **Retries**。 |
| *Book is missing model.* | 这本书没有提供方。请在 **About → Model** 中选择一个。 |
| *Contextual limit not sufficient.* | 不含任何故事内容的提示词无法放入上下文减去回复 token 后的空间。请提高 **Max Context Size**（或协议的 **Max Context Tokens**），降低 **Max Response Tokens**，或缩短模板和 lorebook 条目。 |
| 文本在中途停止 | **Max Response Tokens**（或协议的 **Max Reply Tokens**）过低，推理模型尤其如此。 |
| 开启推理时 API 报错 | 该 API 不支持 `reasoning_effort`。请关闭 **Enable Reasoning**。 |
