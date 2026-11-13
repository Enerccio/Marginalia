---
label: 故障排除与常见问题
order: 900
icon: question
---

# 故障排除与常见问题

<a id="where-are-the-logs"></a>

## 日志在哪里？

大多数问题都会在日志中留下记录：

| 安装方式 | 日志 |
|---|---|
| 桌面应用 | `~/.marginalia/desktop/logs/marginalia.log`（Windows 上为 `%USERPROFILE%\.marginalia\desktop\logs`）；上一次运行的日志在 `marginalia.log.1` 中 |
| Docker | `docker compose logs -f marginalia` |

应用中的错误对话框（*Internal Server Error*）也会显示错误信息，完整细节请查看日志。

报告问题时，请附上页面左下角的版本文字（例如
`Marginalia 1.5.0 (93ec118)`：版本号以及构建所对应的 Git 提交）。

<a id="installing-and-starting"></a>

## 安装与启动

**macOS 提示应用"已损坏"或"无法打开"。**
该应用尚未签名。右键点击它并选择 *Open*，或者运行
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`。参见[桌面应用](getting-started/desktop-app.md)。

**Windows SmartScreen 阻止了该应用。**
点击 *More info* → *Run anyway*。

**浏览器打开的端口不是 8765。**
8765 端口已被占用，因此桌面应用改用了一个空闲端口。可以使用托盘菜单中的 **Open Marginalia**，或者加上
`--port` 参数启动，以固定端口。

**"Marginalia did not start in time"。**
桌面应用无法启动其服务器。日志中会说明原因，通常是另一个程序占用了文件，或者某个扩展损坏（见[下文](#extensions)）。

**Docker：8080 端口已被占用。**
修改 `docker-compose.yml` 中端口映射的左侧数字，例如 `"8081:8080"`，然后访问 8081 端口。

**Marginalia 运行缓慢，或出现 "OutOfMemoryError" 而停止。**
提高内存：在 `docker-compose.yml` 的 `JAVA_TOOL_OPTIONS` 中设置 `-Xmx2g`（或更大的值）。

<a id="logging-in"></a>

## 登录

**"Save login" 没有让我保持登录状态。**
保存登录状态需要服务器使用 HTTPS。请将 Marginalia 放在带 HTTPS 的反向代理后面，参见[服务器（Docker）](getting-started/docker-server.md#https-and-reverse-proxy)。

**忘记了密码。**
管理员可以为你设置新密码，也可以清空密码，让你使用空密码登录后自行设置新密码。操作位置是 *Admin → Users*（参见[重置忘记的密码](administration/users.md#resetting-a-forgotten-password)）。系统不支持通过电子邮件重置密码。

**再也没有人能以管理员身份登录。**
如果没有管理员，只能在数据库中重置密码：

1. 停止 Marginalia，并将[数据文件夹](administration/index.md#the-data-folder)中的 `marginalia.sqlite` 复制一份作为备份。
2. 使用 `sqlite3` 工具清除管理员的密码：
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   （请替换为正确的用户名）。
3. 启动 Marginalia，使用该用户名和**空**密码登录，然后立即通过 **Change Password** 设置新密码。

**"Server connection lost" 或页面不停重新加载。**
在反向代理后面时，代理必须转发 WebSocket 并允许较长的请求，参见[服务器（Docker）](getting-started/docker-server.md#https-and-reverse-proxy)中的 nginx 示例。使用桌面应用时，很可能是应用已退出，重新启动即可。

**打开工作区时浏览器卡死。**
Marginalia 会重新打开你离开时打开的内容，因此一本非常长的书（例如从归档文件导入的书）可能会在你还没来得及做任何操作之前就让页面卡死。请在 Marginalia 地址后加上 `/reset` 打开（例如 `https://marginalia.example.com/reset`）并登录：工作区保存的状态（打开的书籍、选中的标签页、筛选器）会被清除，你会进入一个空的工作区。然后在再次打开该书之前，请在你的[账户](account.md)中设置 **Parts loaded at once in the story**。

<a id="models-and-generation"></a>

## 模型与生成

**"Failed to download model list"。**
请检查 URL 是否以 `/v1` 结尾、API 密钥是否正确，以及模型服务是否正在运行。有些服务不列出模型，此时请直接输入模型 ID。参见[推理服务提供方](inference-providers.md#per-type-settings)。

**Docker：无法访问同一台机器上的模型服务。**
在容器内部，`localhost` 指的是容器本身。请使用 `http://host.docker.internal:<port>/v1`，并让模型服务监听所有地址，参见[同一台机器上的模型](getting-started/docker-server.md#models-on-the-same-machine)。

**"Book is missing model。" / "Book is missing protocol。"**
在书籍的 *About* 标签页中选择它们。在 *Settings* 中设置 *Default Model* 和 *Default Protocol*，这样新书就会自动使用这些设置。

**"Contextual limit not sufficient。"**
提示词（不含任何故事内容）本身已经超过了回复之后上下文所剩余的空间。请提高提供方的 *Max Context Size*（或协议的 *Max Context Tokens*），降低 *Max Response Tokens*，或缩短模板、Lorebook 条目和摘要。参见[限制](protocols.md#limits)。

**模型报告上下文过长。**
对于某些模型，Marginalia 的 token 计数会有一定偏差。请将 *Max Context Size* 降低几百个 token。当提示词看起来已经过大时，发送请求前会出现警告，并列出估算的大小。

**"The provider did not answer in time"、"The provider is limiting requests"、"The provider rejected the API key"……**
Marginalia 会用通俗的语言解释模型 API 的失败原因，参见[错误信息](inference-providers.md#error-messages)。提供方对话框中的 *Test Connection* 可以在不选择书籍的情况下检查 URL、密钥和模型。超时与重试次数是按提供方单独设置的。

**文本在句子中间停止。**
已达到回复长度上限。请提高 *Max Response Tokens*（或协议中的 *Max Reply Tokens*）。推理模型需要的数量要多得多，因为推理过程也会计入其中。

**启用推理后请求失败。**
该 API 不支持 `reasoning_effort`。请在提供方中关闭 *Enable Reasoning*。

**"The summaries of the messages with IDs ... are outdated ... Continue generation and delete them?"**
摘要所覆盖的部分已被编辑或删除。选择 *Yes* 将删除过时的摘要并继续，之后再生成新的摘要。选择 *No* 则停止并保留这些摘要：它们会在 Summaries 概览中被标记出来，你可以在那里重新验证仍然准确的摘要。参见[摘要何时过时](books/summaries.md#when-summaries-become-outdated)。

**提示词中包含单词 "Error"。**
某个模板使用了不存在的变量或宏，通常是拼写错误，例如用了 `instruction` 而不是 `instructions`。模板字段下方会列出这些名称。参见[拼写错误](templates/templates.md#typos)。

<a id="lorebooks"></a>

## Lorebook

**某个条目没有被使用。** 请逐项检查[激活](lorebooks/entries.md#activation)的条件：

- 条目是否已启用？它所在的 Lorebook 是否已启用，通往它的每一层子 Lorebook 是否也都已启用？
- 条目是否有标签？如果有，书籍必须带有其中一个标签（书籍标签位于 *About* 标签页）。否定标签会将其排除。
- Lorebook 本身是否设置了 **Tags**？它们会应用于其所有条目。
- 条目是否设置了过滤器？过滤器只能看到**正在撰写的部分的指令**，看不到前面的故事。请在 *Present Characters* 或指令中提到该角色。
- 过滤器是正则表达式吗？无效的正则表达式永远不会匹配。

已生成部分上的 **Show Prompt** 会显示发送了哪些设定内容。

<a id="prompts-and-settings"></a>

## 提示词与设置

**无法切换到其他标签页。**
*Settings* 标签页中有未保存的更改。请点击 **Save** 或 **Discard Changes**。

**我的摘要提示词没有被使用。**
书籍自己的摘要提示词（*Prompts* 标签页）优先于 *Settings* 中的 *Default Summary Prompt*；清空书籍中的该字段即可使用默认值。元摘要（Meta summary）有自己的提示词，即 *Default Meta Summary Prompt*。

**Style、Point of View 或 Tense 中的宏不起作用。**
这些字段是纯文本，只有模板会处理宏。请把宏放到主模板或用户提示词中。

<a id="backups"></a>

## 备份

**我恢复了数据库备份，但没有任何变化。**
恢复操作会在下次启动时生效。请重新启动 Marginalia。参见[恢复](administration/database-backups.md#restoring)。

**迁移到另一台电脑后，书籍备份不见了。**
书籍备份是文件，不属于数据库的一部分。请同时复制 `data` 文件夹，参见[数据文件夹](administration/index.md#the-data-folder)。

<a id="extensions"></a>

## 扩展

**已安装的扩展没有显示出来。**
关闭并重新打开书籍，或刷新页面。

**某个扩展加载失败，且未列在 Admin → Extensions 中。**
停止 Marginalia，从 `extensions` 文件夹中删除该扩展的 JAR 文件，然后重新启动。参见[扩展加载失败时](administration/extensions.md#when-an-extension-fails)。

**Marginalia 更新后，某个扩展被拒绝加载或停止工作。**
扩展是针对某一版本的 Marginalia 构建的，并会在加载时进行检查。请打开报告（Admin → Extensions → *Verification*），并加载与你的 Marginalia 版本相匹配的扩展版本。参见[扩展加载失败时](administration/extensions.md#when-an-extension-fails)。

**更新扩展后，旧版本仍在运行。**
请先卸载旧版本，再加载新版本；或者重新启动 Marginalia。
<a id="faq"></a>

## 常见问题

**我的写作内容会被发送到任何地方吗？**
只会发送到你配置的推理服务提供商：每个部分的提示（以及词元计数请求）都会发送到该提供商的 API。如果使用在你自己电脑上运行的模型，则不会有任何内容离开本机。Marginalia 没有遥测功能。

**应该使用哪种模型？**
任何能够写出优质小说且上下文足够长的模型都可以；对于长篇书籍，32 000 个词元或更大的上下文比较理想。可以用同一本书试用几种模型——在*关于*选项卡上点击一下即可切换提供商。

**多人能否共同创作同一本书？**
不能。每本书只属于一个用户。其他用户可以在[发布](books/publishing.md)之后阅读。

**可以离线使用 Marginalia 吗？**
可以，只要在本地运行模型（llama.cpp、LM Studio、Ollama 等）。Marginalia 本身不需要网络连接。

**如何将我的书导出到 Marginalia 之外？**
在*故事*选项卡的设置菜单（⚙）中，可以将故事[导出](books/export.md)为文本、HTML、Word、PDF 或 EPUB 格式。若要保留包括所有分支在内的全部内容，请[导出书籍备份](books/backups.md#exporting-and-importing)（JSON）。你也可以通过[阅读器](books/publishing.md)阅读它。

**可以在手机上使用吗？**
[阅读器](books/publishing.md)专为手机设计，并且可以像应用一样添加到主屏幕（在浏览器菜单中选择*安装应用* / *添加到主屏幕*）。当浏览器窗口宽度小于 768 像素时，编辑器会切换到[手机布局](phone.md)。该布局没有管理页面，词条库编辑器功能简化，也不支持全部扩展，因此最适合写作和阅读；设置和管理在较大的屏幕上会更方便。

**有浅色主题或其他语言吗？**
两者都有：语言选择器旁边的太阳/月亮按钮可以切换主题（默认为深色），语言选择器提供日语以及扩展带来的其他语言（参见[账户](account.md#theme)）。
