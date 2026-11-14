---
label: Anthropic Claude
order: 25
---

# Anthropic Claude

将 Anthropic 的 Claude 模型添加为[推理提供方](../inference-providers.md)，通过 Anthropic 自己的 API 调用，而不是兼容 OpenAI 的接口。

也可以通过 OpenRouter 等聚合服务，使用常规的 *OpenAI Compatible* 提供方访问 Claude。本扩展直接连接 Claude API（使用 Anthropic SDK），因此能提供兼容接口无法提供的功能：以推理过程显示的**模型思考摘要**、Claude 4.6 及更新模型的推理力度设置，以及 Anthropic 报告和统计的精确 token 数。

!!!warning 你的故事文本会发送给 Anthropic
使用此提供方的 AI 发出的每个请求都会发送到 Anthropic 的服务器（`api.anthropic.com`，或你设置的代理），包括带有故事的提示词、词条集中的条目和摘要、模型的回答，以及智能体或命令发送的文件和图片。Anthropic 针对 API 的条款和隐私政策决定这些数据在其服务器上如何保留（包括保留期限，以及可能拒绝请求的安全机制）。其他提供方会发送到你选择的服务器，该服务器也可能是你自己的。请勿使用此提供方处理你不能发送给 Anthropic 的文本。在多人共用的服务器上，如果管理员分享了使用此提供方的 AI，那么所有使用者的故事文本都会被发送给 Anthropic。
!!!

<a id="installing"></a>

## 安装

按照[管理扩展](../administration/extensions.md)中的说明，在 *Admin → Extensions* 中加载扩展。该 JAR 文件较大（约 36 MB），其中包含 Anthropic 的 SDK 以及仅 SDK 需要的库。Marginalia 的其他部分不使用这些库。

加载后，推理提供方对话框的 **Type** 列表中会出现新选项 **Extension**，*Per Type Settings* 标签页顶部的 **Provider** 列表中会提供 **Anthropic Claude**（当它是唯一选项时会自动选中）。如果没有加载提供 AI 的扩展，则不会显示 **Extension**。

<a id="adding-a-claude-provider"></a>

## 添加 Claude 提供方

1. 在 [Claude Console](https://platform.claude.com) 中获取 API 密钥。
2. 在 **Inference Providers** 标签页点击 **Add Inference Provider**，输入 **Name**，选择类型 **Extension**，并将提供方选为 **Anthropic Claude**。
3. 填写 *General Settings*（上下文大小、响应大小、[推理](#reasoning)），并在 *Per Type Settings* 标签页填写该提供方的设置，然后点击 **Test Connection**。

| 字段 | |
|---|---|
| **Api Key** | Claude Console 中的密钥。它会加密存储，之后不再显示；编辑提供方时，点击 **Reset** 可以输入新的密钥。 |
| **Model** | 要使用的模型，例如 `claude-opus-5-5` 或 `claude-sonnet-5-5`。点击 **Refresh** 可列出你的密钥可用的模型；你也可以输入列表中没有的名称。 |
| **URL** | 可选。留空则使用 Anthropic 的 API（`api.anthropic.com`）；如需通过代理发送请求，请在此设置。 |
| **Timeout (seconds)** | 等待 Anthropic 响应的时长，以及模型开始输出后等待更多文本的时长。默认值为 300。 |
| **Retries** | 当 Anthropic 返回速率限制（429）或服务器错误（5xx，包括 529 *overloaded*），或无法连接时，请求重试的次数。默认值为 2，设为 0 则关闭重试。 |

**Test Connection** 会列出模型，并使用对话框中的当前值（保存之前）让所选模型生成几个 token。

![Anthropic Claude 提供方的 Per Type Settings](../../images/plugin-anthropic.png)

![包含 Claude 提供方的 Inference Providers 列表](../../images/plugin-anthropic-list.png)

<a id="reasoning"></a>

## 推理

推理设置位于提供方的通用设置中，与其他任何提供方相同：

- 关闭 **Enabled Reasoning** 时，不会发送任何与思考相关的设置。默认会思考的模型（Opus 5.5 无法被设置为不思考）仍会思考，但其思考过程不会显示；其他模型不会思考。
- 开启时，模型会发送其思考过程的摘要，并作为故事段落的推理内容显示。对于 Claude 4.6 及更新的模型，**Reasoning Effort** 用于设置模型的推理力度（low、medium、high），并采用自适应思考：由模型自行决定思考的深度。对于更早的模型（Claude 4.5 及之前的版本，包括 Haiku 4.5），则使用思考 token 预算，分别为 1 024、8 192 或 24 576。**None** 与关闭相同。

思考 token 会计入生成的 token，并占用你设置的响应大小：如果模型把全部额度都用于思考，故事段落就会返回空白，并提示响应大小已用完。此时请提高 **Max Response Tokens**，或降低推理力度。Claude 的每个请求都需要设置响应大小：如果提供方和协议都没有设置，则使用 16 000。

<a id="what-works"></a>

## 支持的功能

故事所需的一切功能与兼容 OpenAI 的提供方相同：流式输出、停止生成、摘要、[调用模型的扩展](index.md)、模型可以调用的函数（[名称生成器](name-generator.md)提供了一些），以及附加在消息中的图片（JPEG、PNG、GIF、WebP）、PDF 和文本文件。用户使用的 token 会根据 Anthropic 报告的数字计入 **Tokens sent** 和 **Tokens generated** 两列（从缓存读取和写入缓存的 token 计入发送量），并且适用共享提供方的[限制](../inference-providers.md#limits-of-shared-providers)。

故事中显示的 token 数（段落大小、提示词）由 Anthropic 统计（即模型的 `count_tokens` 请求），每段文本统计一次，结果会被记住。当无法向 Anthropic 发起请求时（没有密钥、没有网络或出现错误），Marginalia 会在一分钟内自行估算数量，然后再次尝试；在另一次保存仍在进行时保存或编辑的文本，也会使用估算值。

与其他提供方相比，有两点不同：

- **协议中的 temperature、top p 和惩罚参数不会发送。** 当前的 Claude API 会拒绝这些参数；模型会使用自己的采样设置。协议中的响应大小仍然有效。
- 对话必须以用户消息开始，并以用户消息结束，Claude 也不会续写回答。书籍的提示词始终符合这一要求；扩展发出的不符合要求的请求会被拒绝，并返回“bad request”错误。

<a id="when-the-extension-is-not-loaded"></a>

## 扩展未加载时

扩展卸载后，你创建的提供方仍会保留在列表中，类型显示为 *Extension: anthropic*，并会在扩展重新加载后恢复工作。扩展未加载期间，使用此类提供方的书籍无法生成内容；错误提示会告诉你加载扩展或选择其他提供方。此时仍可打开该提供方并修改其通用设置；Claude 相关设置只在扩展加载时显示，并且不会被修改。

<a id="not-supported"></a>

## 不支持的功能

- **Amazon Bedrock、Google Vertex AI 和 Microsoft Foundry。** 仅支持 Claude API 的 API 密钥。
- 提示词缓存，以及在 Anthropic 服务器上运行的工具（网络搜索、代码执行）。
