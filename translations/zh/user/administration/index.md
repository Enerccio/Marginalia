---
label: 管理
order: 910
icon: tools
---

# 管理

管理员负责管理 Marginalia 的安装：用户账户、数据库备份、已删除数据的清理、更新和扩展。在[首次启动](../getting-started/first-start.md)时创建的第一个账户是管理员，它也可以把其他用户设为管理员。

管理员同时也是普通用户，拥有自己的书籍。他们看不到其他用户的书籍、设定集或设置，只能看到那些用户与所有人共享的设定集和协议。

<a id="the-admin-page"></a>

## Admin 页面

管理员在工作区底部有一个 **Admin** 按钮。在[手机布局](../phone.md#administration)下（窗口宽度小于 768 像素），它是抽屉中的 **Admin** 标签页。点击后会打开管理页面，其中包含以下标签页：

| 标签页 | |
|---|---|
| **Users** | 创建、编辑和删除账户，以及为用户授予[权限](permissions.md)。参见[用户](users.md)。 |
| **Database Backups** | 整个数据库的备份，可按需或按计划进行，并可从备份还原。参见[数据库备份](database-backups.md)。 |
| **Cleanup** | 永久删除已删除的数据。参见[清理](cleanup.md)。 |
| **Resource Files** | 查找不再被任何内容使用的资源文件，将其下载为 ZIP 压缩包，并从磁盘上删除。参见[资源文件](resource-files.md)。 |
| **Help assistant** | 根据手册回答问题的聊天：使用的模型、提示词以及由谁付费。参见[帮助助手](help-assistant.md)。 |
| **Agents** | 智能体在自身及其用户都没有自定义提示词时所遵循的系统提示词和压缩提示词。留空则使用内置提示词；信息图标旁有 **Insert Default Template**，点击后会填入默认模板，方便你编辑副本。用户可以对智能体做什么，由其[权限](permissions.md)决定。另外还有 **Agents may send messages to the webhooks of users**：当智能体需要联系某用户、而该用户已关闭应用时，服务器会调用该用户在设置中填写的地址（[不在时](../agents.md#when-you-are-not-there)）；若不能接受这一点，请将其关闭。 |
| **Statistics** | 是否记录[写作统计](../books/statistics.md)，保留多长时间，以及清空某个用户或所有人的统计数据。参见[统计](statistics.md)。 |
| **Jobs** | 记录所有向外部 AI 发出的请求（谁、何时、调用了哪些接口、以何种方式结束）的日志，一个用于手动删除旧条目的过滤器，以及显示当前正在运行的任务的页面 `/jobs`。参见[任务](jobs.md)。 |
| **Update** | 在 Docker 服务器上：当前运行的版本、服务器所跟踪分支的最新版本，以及用于更新并重启 Marginalia 的按钮。参见[更新](updates.md)。 |
| **Extensions** | 安装和移除扩展。参见[管理扩展](extensions.md)。 |

![Admin 页面的 Agents 标签页](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## 数据文件夹

Marginalia 保存的所有内容都位于运行它的用户主目录下的 `.marginalia` 文件夹中（在使用 [Docker 部署](../getting-started/docker-server.md)时，位于宿主机上的 `./data` 中）：

| 路径 | |
|---|---|
| `marginalia.sqlite`（以及 `-wal`、`-shm`） | 数据库：用户、书籍、设定集、设置，全部内容。 |
| `secret.key` | 用于加密数据库中 API 密钥的密钥。在首次启动时创建，仅其所有者可读（在 Linux 和 macOS 上）；没有它，已保存的 API 密钥将无法读取。如果计算机上的其他用户也能读取该文件，日志中会提示：`chmod 600 secret.key`。 |
| `db-backups/` | [数据库备份](database-backups.md)。 |
| `data/<login>/backups/manuscripts/<book id>/` | 每位用户的[书籍备份](../books/backups.md)。 |
| `extensions/` | 已安装的[扩展](extensions.md) JAR 文件。 |
| `update/` | Marginalia 与 Docker 服务器上的[更新监视器](updates.md#setting-up-the-watcher)之间用于通信的文件：状态、上次更新的日志以及请求。 |
| `agent-workspaces/<agent id>/` | [智能体](../agents.md#the-working-copy)的文件（其笔记和报告，在多次运行之间保留）。 |
| `agent-runs/<run id>/` | 智能体一次运行所保留的内容：书籍的起始版本（用于向你展示改动，随工作副本一起删除）、检查点和发现（当运行被清理功能清除时删除）。 |
| `desktop/logs/` | [桌面应用](../getting-started/desktop-app.md)的日志。 |

要备份整个安装，请先停止 Marginalia，然后复制此文件夹（包括 `secret.key`）。在其运行期间，数据库请使用[数据库备份](database-backups.md)；书籍备份和扩展都是普通文件，可随时复制。
