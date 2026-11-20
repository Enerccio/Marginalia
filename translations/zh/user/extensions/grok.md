---
label: xAI Grok
order: 26
---

# xAI Grok

为 [推理服务提供商](../inference-providers.md) 添加 xAI 的 Grok 模型，通过 xAI 自己的 API 调用。

Grok 也可以通过普通的 *OpenAI Compatible* 提供商和地址 `https://api.x.ai/v1` 访问。此扩展直接与 xAI API 通信，因此能够将推理过程显示为推理内容，获得 xAI 报告的精确 token 计数（包括推理 token）和计数，并提供专为该服务设计的模型列表和连接测试。

!!!warning 你的故事文本会发送给 xAI
使用此提供商的每个 AI 所发出的每个请求都会发送到 xAI 的服务器（`api.x.ai`，或你设置的代理）。这些请求包括：带有故事的提示词、Lorebook 条目和摘要、模型的回复，以及智能体或命令发送的图片。数据在 xAI 那里如何处理，取决于其 API 的服务条款和隐私政策。其他提供商会发送到你选择的服务器，该服务器也可能是你自己的。请勿将此提供商用于你不能发送给 xAI 的文本。在与其他用户共享的服务器上，如果管理员共享了使用此提供商的 AI，那么所有使用该 AI 的人的故事文本都会被发送给 xAI。
!!!

<a id="installing"></a>

## 安装

在 *管理 → 扩展* 中加载扩展，具体步骤见 [管理扩展](../administration/extensions.md)。加载后，推理服务提供商对话框的 **类型** 列表中会出现 **Extension** 选项，并且 *按类型设置* 标签页顶部的 **提供商** 列表中会提供 **xAI Grok**。

<a id="adding-a-grok-provider"></a>

## 添加 Grok 提供商

1. 在 [xAI 控制台](https://console.x.ai) 获取 API 密钥。
2. 在 **Inference Providers** 标签页点击 **Add Inference Provider**，输入 **Name**，选择类型 **Extension** 和提供商 **xAI Grok**。
3. 填写 *通用设置*（上下文大小、响应大小、[推理](#reasoning)）以及 *按类型设置* 标签页中该提供商的设置，然后点击 **Test Connection**。

| 字段 | |
|---|---|
| **Api Key** | xAI 控制台的密钥。它会加密存储，且不会再次显示；编辑提供商时，点击 **Reset** 以输入新的密钥。 |
| **Model** | 要使用的模型，例如 `grok-4` 或 `grok-3-mini`。**Refresh** 会列出你的密钥可以使用的模型；你也可以输入列表中没有的名称。 |
| **URL** | 可选。留空则使用 xAI 的 API（`https://api.x.ai/v1`）；如需通过代理发送请求，请在此设置。 |
| **Timeout (seconds)** | 等待 xAI 回复的时长，以及模型开始输出后等待后续文本的时长。默认为 300。 |
| **Retries** | 当 xAI 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求被重复的次数。默认为 2，设为 0 则关闭重试。 |
| **Send the reasoning effort** | *Automatic* 仅对 `grok-3-mini` 发送推理强度，*Always* 对所有模型发送，*Never* 都不发送。 |

**Test Connection** 会列出模型，并使用对话框中的值（保存之前的值）让所选模型生成少量 token。

<a id="reasoning"></a>

## 推理

推理设置与其他提供商一样，位于提供商的通用设置中。开启推理后，模型显示的推理内容会以故事部分的推理形式呈现。推理强度只会发送给支持该参数的模型（`grok-3-mini`：*low*，对 medium 和 high 则使用 *high*）；其他 Grok 模型保持默认的推理方式，并会拒绝该参数。

<a id="what-works"></a>

## 支持的功能

流式输出、停止生成、JSON Schema、模型可调用的函数、作为附件的图片（JPEG、PNG）和文本文件、共享 AI 的限制以及用量统计，都与 OpenAI 兼容提供商的表现一致。用量数据即 xAI 报告的数据；推理 token 计入生成的 token。启用时，温度、top p 以及[协议](../protocols.md)中的惩罚参数都会被发送；部分推理模型会拒绝惩罚参数。不支持 xAI 的服务端工具（实时搜索、代码执行）以及图片或视频生成。
