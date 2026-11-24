---
label: 数据库备份
order: 90
---

# 数据库备份

数据库备份是 Marginalia 数据库的完整副本，包括所有用户及其全部书籍、设定集、设置和扩展数据。用它可以防范误操作和故障，也可以把整个安装迁移到另一台计算机。要备份单本书，用户可以使用[书籍备份](../books/backups.md)。

![数据库备份标签页](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## 创建备份

在 **Database Backups** 标签页中点击 **Create Backup**。备份在 Marginalia 运行时进行，即使有人正在写作，副本也是一致的。备份会出现在列表中，显示其 **Name**、**Created** 日期、**Type**（*Manual* 或 *Scheduled*）和 **Size**。工具栏显示数据库的当前大小。

每个备份都有以下按钮：

| 按钮 | |
|---|---|
| **Download** | 下载备份文件（`.sqlite`）。请在服务器之外保留一份副本。 |
| **Restore on restart** | 恢复该备份，见[下文](#restoring)。 |
| 垃圾桶 | 删除该备份。 |

**Upload Backup** 可以把一个备份文件（`.sqlite` 或 `.db`）添加到列表中，例如从另一个安装下载的文件。上传的文件会先被检查。如果它不是 SQLite 数据库、已损坏、不是 Marginalia 数据库（而是其他应用程序的数据库），或者来自比当前运行版本更新的 Marginalia，系统会拒绝该文件，并显示 *The database can't be restored: ...* 以及原因。

!!!warning 备份包含敏感信息
数据库备份包含全部内容，包括所有用户的密码哈希值和 API 密钥。API 密钥使用[数据文件夹](index.md#the-data-folder)中的 `secret.key` 文件加密，而该文件不属于备份的一部分。但是，任何同时拥有备份和 `secret.key` 的人都可以读取这些密钥。请像保护服务器本身一样保护下载的备份。
!!!

备份文件存放在[数据文件夹](index.md#the-data-folder)的 `db-backups` 文件夹中。Marginalia 自行创建的副本也会出现在列表中，显示为 *Manual*，包括[恢复](#restoring)前创建的 `pre-restore-...`，以及[升级](#copy-before-an-upgrade)前创建的 `pre-migration-...`。因此，它们不会被定时备份的轮替机制删除。书籍备份和扩展 JAR 是单独的文件，不属于数据库备份。

**上传的文件（即[部件的图片](../books/story-editor.md#images)）同样不属于数据库备份。** 数据库只记录它们作为 [Resources](../resources.md) 标签页中的条目，而文件本身保存在数据文件夹的 `data/<login>/images` 文件夹中。这样设计没有问题：恢复后，条目与备份时的一致，它们使用的图片仍然保留在文件夹中。备份之后添加的图片文件会成为孤立文件（没有任何内容引用它们），并占用磁盘空间。要确保图片安全，请在自己的备份中包含整个数据文件夹，或将其打包进[书籍备份](../books/backups.md#pictures-in-a-backup)。

在**另一个安装**上恢复备份时，还需要从原安装复制 `secret.key`（先停止 Marginalia）。否则，已保存的 API 密钥无法解密，每个用户都必须重新输入。

<a id="scheduled-backups"></a>

## 定时备份

**Scheduled Backups** 部分可以自动创建备份：

| 字段 | |
|---|---|
| **Create backups automatically** | 开启定时备份。 |
| **Schedule (cron)** | 设定创建备份的时间，见[下文](#schedule-syntax)。字段下方显示接下来的三个时间点。 |
| **Keep scheduled backups** | 保留的定时备份数量；更早的备份会被删除。`0` 表示保留全部。手动备份和上传的备份永远不会被删除。 |

点击 **Save Schedule** 应用设置。状态行显示上一次定时备份的时间，以及下一次备份的预定时间。

如果 Marginalia 在定时时间没有运行（桌面应用的情况很典型，它只在您写作时运行），它会在下次启动后约 30 秒补做错过的备份。在这 30 秒内关闭定时功能或修改计划，会取消该次备份。

<a id="schedule-syntax"></a>

### 计划语法

计划是一个由五个字段组成的 cron 表达式，`minute hour day-of-month month day-of-week`：

| 表达式 | 运行时间 |
|---|---|
| `0 3 * * *` | 每天 3:00 |
| `0 */6 * * *` | 每 6 小时 |
| `30 2 * * 1` | 每周一 2:30（星期几：`0` 或 `7` 为星期日，`1` 为星期一……） |
| `0 12 1 * *` | 每月 1 日中午 |
| `@daily`、`@weekly`、`@monthly` | 每天 / 每周 / 每月午夜各一次（`@hourly`、`@yearly` 也可用） |

时间以服务器所在的时区为准（在 Docker 中，使用 `TZ` 环境变量设置时区）。

!!!
要关闭定时备份，取消勾选 *Create backups automatically*，然后点击 *Save Schedule*。表达式会保留下来，之后可以再次开启备份。关闭期间表达式仍会被检查，无效的表达式永远不会被保存，但关闭备份时该字段也可能为空。
!!!

<a id="restoring"></a>

## 恢复

Marginalia 使用数据库时无法替换它，因此恢复会在**下一次启动**时进行：

1. 点击备份旁边的 **Restore on restart** 并确认。标签页会显示 *Database restore is scheduled. Restart the application to apply it.*，点击 **Cancel Restore** 可以撤销此操作。
2. 重启 Marginalia：退出并启动[桌面应用](../getting-started/desktop-app.md)，或在[服务器](../getting-started/docker-server.md)上执行 `docker compose restart`。

启动时，当前数据库会被移动到 `db-backups/pre-restore-<time>-marginalia.sqlite`，它会出现在列表中，因此可以通过恢复该文件来撤销恢复操作。随后备份会取代它的位置。来自较旧 Marginalia 版本的备份会被自动升级。

在替换数据库之前，系统会再次检查备份（检查项目与 *Upload Backup* 相同，*Restore on restart* 也会拒绝未通过检查的备份）。如果备份未通过检查，或升级失败，Marginalia 会保留（或恢复）当前数据库并正常启动；被拒绝的文件会被移动到 `db-backups/rejected-restore-<time>-marginalia.sqlite`，日志会说明原因。

备份之后的所有内容都会丢失：备份之后创建的书籍、部件和设置。已登录的用户如果账户有变化，需要重新登录。

<a id="copy-before-an-upgrade"></a>

## 升级前的副本

当新版本的 Marginalia 要改变现有数据库的结构时，它会先保存一份副本，名为 `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`，其中版本号是数据库当时所处的版本。该副本会出现在备份列表中，因此可以通过安装旧版 Marginalia 并[恢复](#restoring)该副本来撤销升级。系统会保留最近三份副本。新安装没有需要复制的内容；如果无法创建副本（例如磁盘已满），日志会说明情况，升级仍会继续。

如果迁移在中途失败，它会自行回滚，Marginalia 也不会启动。该副本用于应对升级已完成、但结果不是您想要的情况。

<a id="moving-to-another-computer"></a>

## 迁移到另一台计算机

1. 创建备份并**下载**。
2. 在新计算机上安装 Marginalia，并在首次启动时创建一个临时管理员。
3. 依次执行 **Upload Backup**、**Restore on restart**，然后重启。
4. 使用备份中的账户登录。

同时，从旧的[数据文件夹](index.md#the-data-folder)复制 `data` 文件夹（书籍备份和部件图片）以及 `extensions` 文件夹。或者，直接停止 Marginalia，然后复制整个 `.marginalia` 文件夹。
