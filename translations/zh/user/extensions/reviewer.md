---
label: 审稿
order: 2
---

# 审稿

让模型审阅你写的内容：读者反应、编辑点评、连贯性检查，任何你让它做的审阅都可以。审稿结果保存在对应的章节旁边，
不会成为书稿的一部分。

![对故事章节的审稿](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## 审阅章节

[故事编辑器](../books/story-editor.md)中每个章节的菜单（☰）都有一个 **Review** 子菜单：

| 项目 | |
|---|---|
| **View / Generate Review** | 打开审稿对话框。如果该章节还没有审稿结果，会立即生成一份。 |
| **Advanced Options** | 打开[高级选项](#advanced-options)，然后使用这些选项生成审稿结果。 |
| **Delete Review** | 删除该章节的所有审稿结果。 |

在审稿对话框中：

- **‹** / **›** 翻阅该章节的审稿结果；在最后一份上点 **›** 会生成新的审稿结果；
- **Stop generating** 中断正在生成的审稿结果；
- 点击审稿文本可以编辑它；点击输入框之外时保存；
- *Thinking Process* 显示[推理模型](../inference-providers.md#reasoning-models)的思考过程；
- **Close** 关闭对话框。

<a id="what-the-model-gets"></a>

## 模型收到什么

默认情况下，审稿使用与该章节写作时**相同的提示**（*Use standard prompt info*）：系统提示（包括设定集和摘要）、
前面的章节，然后是该章节本身，最后是审稿提示。模型会结合整个故事的完整上下文来审阅该章节。

如果设置了配置档的**前置提示**，它会作为系统消息首先发送。

<a id="advanced-options"></a>

## 高级选项

| 选项 | |
|---|---|
| **Prompt Override** | 本次审稿使用的另一个审稿提示。初始内容为配置档中的提示。 |
| **Use standard prompt info** | 开启：使用该章节写作时的提示，如上所述。关闭：只发送故事章节，并应用下面的选项。 |
| **Include Lorebook** | （仅在关闭时）加入该章节写作时生效的设定。 |
| **With summaries** | （仅在关闭时）先发送故事的[摘要](../books/summaries.md)，然后发送最新摘要之后的章节，方式与生成相同：包含最新摘要的章节及其之前的内容由摘要代表。摘要优先占用令牌上限，剩余空间留给章节，从最新的开始。如果某个摘要所对应的故事在其生成之后被修改过，则不会发送该摘要（审稿过程不会删除它）。关闭：不发送摘要，发送尽可能多的最新章节。 |
| **Send original text** | 仅在加载了翻译器（[Real Language Translator](real-language-translator.md)、[Conlang Translator](conlang-translator.md)）时显示。关闭：与所有模型一样，审稿读取的是translated语音背后的英文。开启：被审阅的章节（以及在 *Use standard prompt info* 关闭时的前面章节）按其存储的原样发送，语音保留为译文。从英文构建的、写作该章节时的提示（*Use standard prompt info* 开启）保持不变。 |
| **Token Limit** | （仅在关闭时）要发送多少令牌的故事：截至并包括被审阅章节的章节，从最新的开始，尽可能多地装入。初始值为模型提示所需空间之外的余量。 |

*Use standard prompt info* 关闭时，对话框会显示请求的大小（*Total tokens*）。点击 **Generate** 会以这些选项打开审稿对话框；
在该对话框中继续生成的审稿结果也会使用这些选项。

![高级审稿选项](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## 配置档

审稿设置位于 **Settings → Extension Settings → Reviewer Settings**。它们按**配置档**组织：

![审稿设置](../../images/plugin-reviewer-settings.png)

| 字段 | |
|---|---|
| **Profile** | 要编辑的配置档，也是所有审稿使用的配置档。 |
| **New**、**Rename**、**Delete** | 管理配置档。*_Default* 配置档不能重命名或删除。 |
| **Reviewer Model** | 负责撰写审稿的[推理提供方](../inference-providers.md)。留空：使用书籍的模型。 |
| **Reviewer Protocol** | [协议](../protocols.md)。留空：使用书籍的协议。 |
| **Review Pre-Prompt (System)** | 首先发送的系统消息，例如 *You are a demanding fiction editor.*（你是一位严格的小说编辑。） |
| **Review Post-Prompt (User)** | 审稿请求，最后发送。 |

点击 *Settings* 页面顶部的 **Save** 以保存更改。

默认的后置提示模拟一个故事论坛上的讨论：一个虚构的帖子，有来自不同读者的三十条回复，既有正面的也有负面的。其他想法：

- *对照前面的故事，列出最后一章中的连贯性错误。引用相关段落。*
- *以文字编辑的身份，指出最后一章中的陈词滥调、重复和无力的动词，并给出建议。*
- *描述一位初次阅读的读者在该章结束时会有什么感受，以及他们期待接下来发生什么。*

为每种审稿创建一个配置档，并在审阅前选择所需的配置档。用于审稿的模型可以更便宜，省钱的办法是把它设为该配置档的 *Reviewer Model*。

<a id="storage"></a>

## 存储

审稿结果与故事章节一起保存，因此它们会包含在[书籍备份](../books/backups.md)和副本中。
[Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) 不会复制它们。配置档保存在你的用户设置中。
