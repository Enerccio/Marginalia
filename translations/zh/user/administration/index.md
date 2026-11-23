---
label: 管理
order: 910
icon: tools
---

# 管理

管理员负责管理 Marginalia 安装实例：用户账户、数据库备份、已删除数据的清理、更新和扩展。在[首次启动](../getting-started/first-start.md)时创建的第一个账户是管理员，它还可以把其他用户设为管理员。

管理员同时也是普通用户，拥有自己的书籍。他们看不到其他用户的书籍、lorebook 或设置，只能看到那些用户与所有人共享的 lorebook 和协议。

<a id="the-admin-page"></a>

## 管理页面

管理员在工作区底部有一个 **Admin** 按钮。在[手机布局](../phone.md#administration)（窗口宽度小于 768 像素）中，它是抽屉中的 **Admin** 标签页。点击后会打开管理页面，其中包含以下标签页：

| 标签页 | |
|---|---|
| **Users** | 创建、编辑和删除账户，并授予用户[权限](permissions.md)。参见 [Users](users.md)。 |
| **Database Backups** | 整个数据库的备份，可随时手动创建或按计划创建，并可从备份还原。参见 [Database backups](database-backups.md)。 |
| **Cleanup** | 永久删除已删除的数据。参见 [Cleanup](cleanup.md)。 |
| **Resource Files** | 查找不再被任何内容使用的资源文件，将其下载为 ZIP 压缩包，或从磁盘中删除。参见 [Resource Files](resource-files.md)。 |
| **Help assistant** | 根据手册回答问题的聊天功能：使用的模型、提示词以及由谁承担费用。参见 [Help assistant](help-assistant.md)。 |
| **Agents** | 智能体在自身和用户都没有各自的提示词时遵循的系统提示词和压缩提示词。留空则使用内置提示词；信息图标处的 **Insert Default Template** 会把默认模板填入，方便你修改副本。用户可以对智能体做什么，由其[权限](permissions.md)决定。另外还有 **Agents may send messages to the webhooks of users**：当智能体需要联系用户，而用户已关闭应用时，服务器会调用用户在设置中填写的地址（[你不在时](../agents.md#when-you-are-not-there)）；如果不能接受这一点，请将其关闭。 |
| **Statistics** | 是否记录[写作统计](../books/statistics.md)、保存多久，以及清空某个用户或所有人的统计数据。参见 [Statistics](statistics.md)。 |
| **Jobs** | 记录所有向外部 AI 发出的请求的日志（谁、何时、进行了哪些调用、如何结束）、手动删除旧条目的过滤器，以及显示当前正在运行的任务的页面 `/jobs`。参见 [Jobs](jobs.md)。 |
| **GPUs** | 一块显卡由语言模型以及图像和语音服务器共享：显卡本身、GPU 锁（每个模型如何加载、如何释放显卡）及其执行的步骤，每块 GPU 当前占用的内容，以及 **Unload now**。参见 [GPUs](gpus.md)。 |
| **Update** | 在 Docker 服务器上：当前运行的版本、服务器所跟随分支的最新版本，以及用于更新并重启 Marginalia 的按钮。参见 [Updates](updates.md)。 |
| **Extensions** | 安装和移除扩展。参见 [Managing extensions](extensions.md)。 |

![管理页面的 Agents 标签](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## 数据文件夹

Marginalia 保存的所有内容都位于运行它的用户主目录下的 `.marginalia` 文件夹中（使用 [Docker 部署](../getting-started/docker-server.md)时位于宿主机上的 `./data`）：

| 路径 | |
|---|---|
| `marginalia.sqlite`（以及 `-wal`、`-shm`） | 数据库：用户、书籍、lorebook、设置，所有内容。 |
| `secret.key` | 用于加密数据库中 API 密钥的密钥。在首次启动时创建，仅其所有者可读（Linux 和 macOS 上）；没有它，已保存的 API 密钥将无法读取。如果计算机上的其他用户也可以读取它，日志会对此给出提示：`chmod 600 secret.key`。 |
| `db-backups/` | [数据库备份](database-backups.md)。 |
| `data/<login>/backups/manuscripts/<book id>/` | 每个用户的[书籍备份](../books/backups.md)。 |
| `extensions/` | 已安装的[扩展](extensions.md) JAR 文件。 |
| `update/` | Marginalia 与 Docker 服务器上的[更新监视器](updates.md#setting-up-the-watcher)之间通信所用的文件：状态、上次更新的日志、更新请求。 |
| `agent-workspaces/<agent id>/` | [智能体](../agents.md#the-working-copy)的文件（其笔记和报告，在两次运行之间保留）。 |
| `agent-runs/<run id>/` | 智能体每次运行时保留的内容：书籍的起始状态（用于向你展示改动，随工作副本一同删除）、检查点和发现结果（运行被清理功能清除时删除）。 |
| `desktop/logs/` | [桌面应用](../getting-started/desktop-app.md)的日志。 |

要备份整个安装，请停止 Marginalia 并复制此文件夹（包括 `secret.key`）。在其运行期间，请使用[数据库备份](database-backups.md)来备份数据库；书籍备份和扩展都是普通文件，可随时复制。
