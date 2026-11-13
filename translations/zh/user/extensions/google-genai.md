---
label: Google Gemini
order: 14
---

# Google Gemini

为 Google 的 Gemini 模型添加一个[推理提供方](../inference-providers.md)，通过 Google 自有的 API 而不是 OpenAI 兼容接口进行调用。

Google 也提供兼容 OpenAI 的接口，可与普通的 *OpenAI Compatible* 提供方一起使用。本扩展直接调用 Gemini API（Google Gen AI SDK），因此能提供兼容接口所没有的功能：以推理形式显示的**模型思考摘要**、Gemini 3 的思考级别和早期模型的思考预算、安全过滤器，以及 Google 报告的精确 token 计数。

!!!warning 你的故事文本会发送给 Google
使用此提供方的 AI 的每一个请求，包括带有故事的提示词、Lorebook 条目和摘要、模型的回答，以及智能体或命令发送的文件和图片，都会发送到 Google 的服务器（`generativelanguage.googleapis.com`，或你设置的代理）。这些内容在那里如何处理，取决于 Google 的 Gemini API 服务条款和隐私政策（包括保留期限、是否用于改进模型；这取决于你的 API 密钥所属的套餐）。其他提供方会发送到你选择的服务器，该服务器可能是你自己的。请勿将此提供方用于你不能发送给 Google 的文本。在与其他用户共享的服务器上，共享此提供方 AI 的管理员会将所有使用该 AI 的用户的故事文本发送给 Google。
!!!

<a id="installing"></a>

## 安装

按照[管理扩展](../administration/extensions.md)中的说明，在 *Admin → Extensions* 中加载该扩展。JAR 文件较大（约 14 MB），其中包含 Google 的 SDK 以及只有该 SDK 需要的库。Marginalia 中的其他部分都不会使用它们。

加载后，推理提供方对话框中的 **Type** 列表会新增一项 **Extension**，*Per Type Settings* 选项卡顶部的 **Provider** 列表会提供 **Google Gemini**（当它是唯一选项时会自动为你选中）。如果没有加载提供 AI 的扩展，则不会显示 **Extension**。

<a id="adding-a-gemini-provider"></a>

## 添加 Gemini 提供方

1. 在 [Google AI Studio](https://aistudio.google.com/apikey) 获取 API 密钥。
2. 在 **Inference Providers** 选项卡上点击 **Add Inference Provider**，输入 **Name**，类型选择 **Extension**，提供方选择 **Google Gemini**。
3. 填写 *General Settings*（上下文大小、响应大小、[推理](#reasoning)），并在 *Per Type Settings* 选项卡中填写提供方的设置，然后点击 **Test Connection**。

![类型为 Extension 的提供方的通用设置](../../images/plugin-googlegenai-general.png)

![Google Gemini 提供方的 Per Type Settings](../../images/plugin-googlegenai.png)

此后，该提供方会像其他提供方一样出现在列表中，提供方名称显示在 *Type* 列中：

![包含 Google Gemini 提供方的 Inference Providers 列表](../../images/plugin-googlegenai-list.png)

| 字段 | |
|---|---|
| **Api Key** | Google AI Studio 的密钥。密钥会加密存储且不再显示；编辑提供方时，点击 **Reset** 即可输入新的密钥。 |
| **Model** | 要使用的模型，例如 `gemini-2.5-flash` 或 `gemini-3-pro-preview`。**Refresh** 会列出你的密钥可用于生成文本的模型；你也可以输入列表中没有的名称。 |
| **URL** | 可选。留空则使用 Google 的 API（`generativelanguage.googleapis.com`）；如需通过代理发送请求，请在此设置。 |
| **Timeout (seconds)** | 等待 Google 响应的时长，以及开始输出后等待更多文本的时长。默认值为 300。 |
| **Retries** | 当 Google 返回速率限制（429）或服务器错误（5xx），或无法连接时，请求重试的次数。默认值为 2，设为 0 则关闭重试。 |
| **Turn off the safety filters** | 对可调节的安全类别（骚扰、仇恨言论、色情内容、危险内容）发送 `BLOCK_NONE`。小说创作常会触发过滤器。无论发送何种设置，Google 仍会拦截部分内容。 |

**Test Connection** 会列出模型，并使用对话框中的当前值向所选模型请求少量 token（无需先保存）。

<a id="reasoning"></a>

## 推理

推理设置与其他提供方一样，位于提供方的通用设置中：

- 关闭 **Enabled Reasoning** 时，模型按默认方式思考，其思考过程不显示。
- 开启后，模型会发送其思考摘要，并将其作为故事部分的推理内容显示。**Reasoning Effort** 用于设置模型的思考量：对于 Gemini 3 模型，它是思考级别（low、medium、high）；对于早期模型，则是 1 024、8 192 或 24 576 个思考 token 的预算。**None** 与关闭相同。

思考 token 计入生成的 token，并从你设置的响应大小中扣除：如果模型把它全部用于思考，故事部分就会返回空白，并且错误信息会提示响应大小已用尽。请提高 **Max Response Tokens**，或降低思考强度。

<a id="what-works"></a>

## 可用功能

故事所需的一切功能都与 OpenAI 兼容提供方相同：流式输出、停止生成、摘要、[调用模型的扩展](index.md)、模型可调用的函数（[名称生成器](name-generator.md)提供了部分函数），以及附加到消息中的图片和文件。用户使用的 token 会根据 Google 报告的数字计入 **Tokens sent** 和 **Tokens generated** 列，并且适用共享提供方的[限制](../inference-providers.md#limits-of-shared-providers)。

故事中显示的 token 数（某部分的大小、提示词）由 Google 计算（即模型的 `countTokens` 请求），每段文本计算一次，结果会被记住。当无法向 Google 请求时（没有密钥、无网络或出错），Marginalia 会在一分钟内自行估算该数字，之后再次尝试 Google；在另一次保存进行期间保存或编辑的文本同样会被估算。估算结果与 Google 的计费会略有不同。

<a id="when-the-extension-is-not-loaded"></a>

## 扩展未加载时

扩展卸载后，你创建的提供方仍保留在列表中，类型为 *Extension: google-genai*，并在扩展重新加载后恢复工作。在扩展未加载期间，使用此类提供方的书籍无法生成内容：错误信息会提示你加载扩展或选择其他提供方。该提供方仍可打开并修改其通用设置；Gemini 设置仅在扩展加载时显示，且不会被改动。

<a id="not-supported"></a>

## 不支持的功能

- **Vertex AI**（需要项目和凭据的 Google Cloud）。仅支持 Gemini API 的 API 密钥。
- 图像生成以及 Gemini 的其他输出类型：仅生成文本。
