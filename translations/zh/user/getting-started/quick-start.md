---
label: 快速入门
order: 70
---

# 快速入门

从全新安装到生成故事的第一部分。本文假定你已经[安装](index.md)了 Marginalia，并以[管理员](first-start.md)身份登录。

<a id="1-connect-a-model"></a>

## 1. 连接模型

1. 打开 **Inference Providers** 标签页，点击 **Add Inference Provider**。
2. 输入 **Name**，例如 `OpenRouter - Claude`。
3. 在 **General Settings** 中，将 **Max Context Size** 设为模型的上下文大小，例如 `32000`；将 **Max Response Tokens** 设为你希望的最长输出，例如 `2000`。
4. 在 **Per Type Settings** 中，填写 **OpenAI URL**（以 `/v1` 结尾）和 **Api Key**（对于没有密钥的本地服务器，留空即可），点击 **Refresh**，然后选择 **Model**。
5. 点击 **OK**。

各字段的说明见 [推理提供方](../inference-providers.md)，其中列出了常见服务的 URL。

<a id="2-create-a-protocol"></a>

## 2. 创建协议

1. 打开 **Protocols** 标签页，点击 **Add Protocol**。
2. 输入 **Name**，例如 `Default`。
3. 将 **Max Context Tokens** 和 **Max Reply Tokens** 留空，此时使用推理提供方的限制。
4. 点击 **OK**。

参见 [协议](../protocols.md)。

<a id="3-set-the-defaults"></a>

## 3. 设置默认值

打开 **Settings** 标签页，将新建的提供方选为 **Default Model**，将新建的协议选为 **Default Protocol**。点击 **Save**。之后新建的书籍会自动使用这两项，你无需每次都重新选择。

![Settings 中的默认模型和协议](../../images/settings-defaults.png)

<a id="4-create-a-book"></a>

## 4. 创建书籍

1. 打开 **Books** 标签页，点击 **Add Book**。
2. 输入书籍名称，书籍随即打开。
3. 在 **About** 标签页中，确认 **Model** 和 **Protocols** 已设置（它们来自你的默认值）。如果需要，可以为故事添加 **Description**。

<a id="5-write-the-first-part"></a>

## 5. 写出第一部分

1. 打开 **Story** 标签页。
2. 点击右下角的 **+**，*New Turn Instructions* 面板随即打开。
3. 在 **Instructions** 中描述接下来应发生的情节，例如：
   > 一位住在荒岛上的灯塔看守人在一个漂流瓶里发现了一封写给她的信，信上的日期是五十年后。请介绍她和这座小岛。约 800 字。

   *Scene Setting*、*POV Character* 和 *Present Characters* 是可选的；填写它们可以为模型提供更具体的指引。
4. 点击 **Generate**。模型在写作时，文本会逐步出现。红色的 **Stop** 按钮（位于 **+** 的位置）可以中断生成。

![生成的第一部分](../../images/quick-start-first-part.png)

再次点击 **+** 即可生成下一部分。每一部分都可以通过其菜单（☰）进行编辑、重新生成，或左右滑动以切换到另一个备选版本；参见 [故事编辑器](../books/story-editor.md) 和 [分支与故事树](../books/branches-and-story-tree.md)。

<a id="next-steps"></a>

## 后续步骤

- 在 [设定集](../lorebooks/index.md) 中描述你的世界和角色。
- 在书籍的 [提示词](../books/prompts.md) 中调整文风、视角和时态。
- 随着书籍篇幅增长，[总结](../books/summaries.md) 较早的部分。
- 设置 [书籍备份](../books/backups.md)。
