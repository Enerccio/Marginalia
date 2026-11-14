---
label: 首次启动
order: 80
---

# 首次启动

<a id="create-the-administrator"></a>

## 创建管理员

当 Marginalia 以空数据库启动时，它会要求创建第一个账户，而不是显示登录界面。
该账户即为**管理员**：可以管理用户、数据库备份、清理和扩展。

![创建管理员账户](../../images/first-start.png)

请填写：

| 字段 | |
|---|---|
| **Username** | 登录名。 |
| **Full Name** | 在应用中显示的名称。 |
| **Password**, **Repeat password** | 两次输入必须一致。Marginalia 也接受空密码——在服务器上切勿留空，否则任何人只需输入用户名就能以管理员身份登录。 |

*Administrator* 选项已勾选，首个账户无法更改。点击 **OK**。页面会重新加载并显示登录界面。

!!!
请牢记密码。系统不支持通过电子邮件重置密码。其他管理员可以在 *Admin → Users* 中为用户设置新密码。
!!!

<a id="log-in"></a>

## 登录

使用新账户登录。勾选 **Save login** 后，浏览器将保持登录 30 天。仅在通过 HTTPS 或使用桌面应用在 `127.0.0.1` 上运行时才有效（参见[服务器](docker-server.md#https-and-reverse-proxy)）。

![登录界面](../../images/login.png)

Marginalia 的版本号以及构建所基于的 Git 提交短 ID（例如 `Marginalia 1.5.0 (93ec118)`）显示在登录表单下方。

<a id="the-workspace"></a>

## 工作区

登录后您将看到工作区。左侧的标签页包括：

| 标签页 | |
|---|---|
| **Books** | 您的图书。打开一本书即可看到其故事编辑器。参见[书籍](../books/index.md)。 |
| **Lorebooks** | 用于提示词的世界观和角色笔记。参见[设定集](../lorebooks/index.md)。 |
| **Settings** | 您的个人设置：新图书的默认模型和协议、提示词模板、备份。参见[账户与设置](../account.md)。 |
| **Protocols** | 生成设置。参见[协议](../protocols.md)。 |
| **Inference Providers** | Marginalia 所连接的模型。参见[推理服务提供方](../inference-providers.md)。 |
| **Resources** | 您上传的文件。参见[资源](../resources.md)。 |
| **Trash** | 您删除的内容，直到管理员清理为止。参见[回收站](../trash.md)。 |

底部有 **Admin**（仅限管理员）、**Language** 选择器、**Change Password** 和 **Logout**（关于 *Admin* 页面，参见[管理](../administration/index.md)）。

![首次登录后的工作区](../../images/workspace.png)

同样的版本文字以小字显示在页面左下角；将鼠标悬停其上可查看完整的提交 ID 以及提交和构建的时间。报告问题时请提供该信息。从开发检出目录运行的构建（`mvn jetty:run`）会显示 `dev` 而非版本号。

Marginalia 会记住您的操作位置。下次登录时（在任何设备上），它会打开同一个标签页，保留图书列表的相同筛选条件、相同的设定集以及已打开的设定集条目；如果您当时打开了某本书，则会在同一标签页中打开该书，并保留您已输入的场景、视角角色、在场角色以及下一部分的指令。已不存在的内容（已删除的图书、设定集或标签）将被跳过；已被删除的图书不会被打开。

继续阅读[快速入门](quick-start.md)。
