---
label: xAI Grok
order: 26
---

# xAI Grok

为 xAI 的 Grok 模型添加一个[推理提供方](../inference-providers.md)，通过 xAI 自己的 API 调用。

也可以使用普通的 *OpenAI Compatible* 提供方并填写网址 `https://api.x.ai/v1` 来访问 Grok。此扩展直接与 xAI 的 API 通信，因此能够将推理过程作为推理内容显示，获得 xAI 报告的精确令牌计数（包括推理令牌），并提供专为该服务制作的模型列表和连接测试。

!!!warning 你的故事文本会发送到 xAI
使用此提供方的 AI 发出的每个请求（包括带有故事的提示词、设定集条目和摘要、模型的回答，以及智能体或命令发送的图片）都会发送到 xAI 的服务器（`api.x.ai`，或你设置的代理）。xAI 的 API 条款和隐私政策决定了这些内容在那里如何处理。其他提供方会发送到你选择的服务器，而该服务器可能是你自己的。请勿将不允许发送给 xAI 的文本用于此提供方。在与其他用户共享的服务器上，如果管理员共享了使用此提供方的 AI，那么每位使用者的故事文本都会被发送到 xAI。
!!!

<a id="installing"></a>

## 安装

在 *Admin → Extensions* 中加载扩展，方法见[管理扩展](../administration/extensions.md)。扩展加载后，推理提供方对话框的 **Type** 列表中会出现 **Extension** 选项，*Per Type Settings* 选项卡顶部的 **Provider** 列表中会提供 **xAI Grok**。

<a id="adding-a-grok-provider"></a>

## 添加 Grok 提供方

1. 在 [xAI 控制台](https://console.x.ai)获取 API 密钥。
2. 在 **Inference Providers** 选项卡上点击 **Add Inference Provider**，输入 **Name**，选择类型 **Extension** 和提供方 **xAI Grok**。
3. 填写 *General Settings*（上下文大小、响应大小、[推理](#reasoning)）以及 *Per Type Settings* 选项卡中该提供方的设置，然后点击 **Test Connection**。

![一个 Grok 提供方](../../images/plugin-grok.png)

![列表中的 Grok 提供方](../../images/plugin-grok-list.png)

| Field | |
|---|---|
| **Api Key** | xAI 控制台中的密钥。它会加密存储且不会再次显示；编辑提供方时，点击 **Reset** 可以输入另一个密钥。 |
| **Model** | 要使用的模型，例如 `grok-4` 或 `grok-3-mini`。**Refresh** 会列出你的密钥可以使用的模型；你也可以输入列表中没有的名称。 |
| **URL** | 可选。留空则使用 xAI 的 API（`https://api.x.ai/v1`）；若要通过代理发送请求，请在此设置。 |
| **Timeout (seconds)** | 等待 xAI 回答的时长，以及它开始输出后等待后续文本的时长。默认为 300。 |
| **Retries** | 当 xAI 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求重试的次数。默认为 2，设为 0 则关闭重试。 |
| **Send the reasoning effort** | *Automatic* 仅向 `grok-3-mini` 发送推理力度，*Always* 向所有模型发送，*Never* 都不发送。 |

**Test Connection** 会列出模型，并使用对话框中当前的值（保存前的值），请所选模型生成几个令牌。

<a id="reasoning"></a>

## 推理

推理设置位于提供方的通用设置中，与其他任何提供方相同。开启推理后，模型显示的推理内容会以故事部分推理的形式呈现。推理力度只会发送给支持该参数的模型（`grok-3-mini`：*low*；medium 和 high 均对应 *high*）。其他 Grok 模型按默认方式推理，并会拒绝该参数。

<a id="what-works"></a>

## 支持的功能

流式输出、停止生成、JSON 结构（schema）、模型可调用的函数、作为附件的图片（JPEG、PNG）和文本文件、共享 AI 的限制以及用量统计，均与 OpenAI 兼容提供方的功能一致。用量为 xAI 报告的数值；推理令牌计入生成的令牌。开启时，温度、top p 以及[协议](../protocols.md)的惩罚参数都会被发送；部分推理模型会拒绝惩罚参数。不支持的功能有：xAI 的服务端工具（实时搜索、代码执行），以及图像或视频生成。
