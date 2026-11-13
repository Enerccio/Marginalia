---
label: 协议
order: 970
icon: sliders
---

# 协议

协议是一组生成设置：采样参数（temperature、top P 和惩罚项），以及可选的自有上下文和回复长度上限。[推理服务商](inference-providers.md)决定*由哪个模型*写作；协议决定*如何写*。

协议与服务商是分开的，因此你可以保存几个协议，例如一个谨慎的和一个富有创意的，并在不改动连接的情况下让某本书在它们之间切换。与服务商一样，协议属于你的账户；你可以[分享](#sharing-a-protocol)一个协议，让其他用户导入一份副本。

![协议标签页](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## 创建协议

打开 **Protocols** 标签页，点击 **Add Protocol**。编辑现有协议请点击铅笔按钮，删除请点击垃圾桶按钮。克隆按钮（两张叠放的纸）会复制一个协议及其所有设置，并让你确认新名称（默认为原名称加上 *(Copy)*）。当你想尝试其他采样设置又不想丢失已经调好的设置时，这很方便。

**Import from other user**（列表上方）会显示其他用户[分享](#sharing-a-protocol)的协议，并允许你导入其中一个的副本。

![协议对话框](../images/protocol-dialog.png)

| 字段 | |
|---|---|
| **Name** | 你为协议取的名称，例如 `Creative` 或 `Long context`。 |
| **Type** | 请求格式。目前只有 **Chat Completion**。 |
| **Max Context Tokens** | 可选。使用此协议的书籍，提示词与回复的 token 总数。留空则使用服务商的 **Max Context Size**。 |
| **Max Reply Tokens** | 可选。回复的最大长度。留空则使用服务商的 **Max Response Tokens**。 |
| **Temperature** | 文本的随机程度。较低（约 `0.7`）更集中、更可预测；较高（`1.0` 及以上）更多变、更出人意料。 |
| **Top P** | 核采样：模型只从概率之和达到该值（`0`-`1`）的最可能词中挑选。`0.9`-`0.95` 是常见选择。 |
| **Frequency Penalty** | 按词语已出现的次数进行惩罚，从而减少重复。通常为 `0`-`1`。 |
| **Presence Penalty** | 对已经出现过的词语进行惩罚，促使模型转向新的话题。通常为 `0`-`1`。 |
| **Share with other users** | 勾选后，其他用户可以查看该协议并导入副本，参见[下文](#sharing-a-protocol)。新建协议时默认不勾选。 |

每个采样参数都有一个 **Enable** 复选框。未启用的参数不会由 Marginalia 设置，模型会使用它自己的默认值。只启用你想修改的参数即可。

<a id="sharing-a-protocol"></a>

## 分享协议

在协议对话框中勾选 **Share with other users**，本安装实例的其他所有用户都会在 **Import from other user**（位于 **Add Protocol** 旁边的按钮）中看到该协议，并显示其名称和作者（你的全名，若没有则显示登录名）。他们可以打开某一行查看其设置，并点击 **Import**，从而生成一个拥有相同设置和名称的协议副本。分享仅供阅读：其他人无法更改、删除或使用你的协议，而且副本是私有的，与原协议没有关联——双方之后各自的修改都不会影响对方。取消勾选后，该协议会从他们的列表中消失；他们已经生成的副本仍归他们所有。

<a id="limits"></a>

## 上限

[推理服务商](inference-providers.md#general-settings)的上限始终有效。协议可以分别降低这些上限，但不能将其提高到服务商设定值以上：

| | 设置了协议值 | 协议值留空 |
|---|---|---|
| 上下文 | **Max Context Tokens**，若其小于服务商的 **Max Context Size** 则取前者 | 服务商的 **Max Context Size** |
| 回复 | **Max Reply Tokens**，若其小于服务商的 **Max Response Tokens** 则取前者 | 服务商的 **Max Response Tokens** |

例如，协议的 **Max Context Tokens** 设为 128 000，而服务商为 32 000，则实际取 32 000：服务商的设置即是服务（或分享该服务的管理员）所允许的上限。

Marginalia 会为回复预留空间，并用提示词填满其余的上下文：模板、lorebook 条目、摘要，以及能放得下的尽可能多的故事内容。若上下文为 32 000 token，回复最多 2 000 token，则提示词可使用 30 000 token。回复上限同时会发送给 API（`max_completion_tokens`）。

当你希望提示词比模型允许的更短时，可以使用协议的上限，例如在使用大上下文云端模型时让长篇书籍更便宜、更快，或让某些书籍的回复更简短。协议的上限也用于使用该协议的书籍的[摘要](books/summaries.md)。

除非确有需要，否则两项都保持留空：这样会沿用服务商的数值，并且在你更换书籍的服务商时，其上限会随之变化。

<a id="using-a-protocol-in-a-book"></a>

## 在书籍中使用协议

每本书的协议在其 **About** 标签页的 **Protocols** 字段中设置。若要让所有新书都使用某个协议，请在 **Settings** 标签页中将其选为 **Default Protocol**。没有协议的书籍无法生成内容，并会显示 *Book is missing protocol.*。

<a id="suggested-settings"></a>

## 建议设置

以下是起始值，请根据你的模型进行调整：

| 用途 | Temperature | Top P | Frequency Penalty | Presence Penalty |
|---|---|---|---|---|
| 均衡的文笔 | `0.8` | `0.95` | 关 | 关 |
| 富有创意、变化丰富 | `1.0` | `0.95` | `0.3` | `0.3` |
| 稳定、贴近指令 | `0.6` | `0.9` | 关 | 关 |

推理模型通常会忽略或拒绝采样参数；对这类模型请保持它们处于禁用状态。
