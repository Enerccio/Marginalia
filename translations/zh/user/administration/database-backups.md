---
label: 数据库备份
order: 90
---

# 数据库备份

数据库备份是 Marginalia 数据库的完整副本：包含所有用户及其全部书籍、设定集、设置和扩展数据。用它可以防范误操作和故障，也可以把整个安装迁移到另一台电脑上。若只想备份单本书，用户可以使用[书籍备份](../books/backups.md)。

![“数据库备份”标签页](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## 创建备份

在 **Database Backups** 标签页中点击 **Create Backup**。备份是在 Marginalia 运行期间完成的，即使有人正在写作，副本也是一致的。备份会出现在列表中，列出 **Name**、**Created**、**Type**（*Manual* 或 *Scheduled*）和 **Size**。工具栏显示数据库当前的大小。

每个备份都有以下操作：

| 按钮 | |
|---|---|
| **Download** | 下载备份文件（`.sqlite`），请在服务器之外保留一份副本。 |
| **Restore on restart** | 还原该备份，参见[下文](#restoring)。 |
| 垃圾桶图标 | 删除该备份。 |

**Upload Backup** 会把一个备份文件（`.sqlite` 或 `.db`）添加到列表中，例如从另一个安装中下载的文件。文件会先经过检查；如果它不是 SQLite 数据库、已损坏、不是 Marginalia 数据库（而是其他应用的数据库），或者来自比当前运行版本更新的 Marginalia，则会被拒绝，并显示 *The database can't be restored: ...* 及原因。

!!!warning 备份包含敏感信息
数据库备份包含全部内容，包括所有用户的密码哈希以及他们的 API 密钥。API 密钥由[数据文件夹](index.md#the-data-folder)中的 `secret.key` 文件加密，而该文件不属于备份的一部分——但同时拥有备份和 `secret.key` 的人就能读取这些密钥。请像保护服务器本身一样保护下载下来的备份。
!!!

备份文件保存在[数据文件夹](index.md#the-data-folder)的 `db-backups` 文件夹中。Marginalia 自己创建的副本——[还原](#restoring)前生成的 `pre-restore-...`，以及[升级](#copy-before-an-upgrade)前生成的 `pre-migration-...`——也会出现在列表中，并显示为 *Manual*，因此定时备份的轮换删除不会删掉它们。书籍备份和扩展 JAR 是独立的文件，不属于数据库备份。

**上传的文件（[段落插图](../books/story-editor.md#images)）同样不属于数据库备份。** 数据库只把它们记录为 [Resources](../resources.md) 标签页中的条目；文件本身位于数据文件夹的 `data/<login>/images` 中。这没有问题：还原后，条目与备份时一致，它们所用的插图仍在文件夹中。备份之后新添加的插图文件会残留下来（已无任何引用），并占用磁盘空间。要确保插图安全，请在你自己的备份中包含整个数据文件夹，或将它们打包进[书籍备份](../books/backups.md#pictures-in-a-backup)。

在**另一个安装**上还原备份时，请同时（在 Marginalia 停止运行的情况下）从原安装复制 `secret.key`，否则已保存的 API 密钥无法解密，每位用户都必须重新输入。

<a id="scheduled-backups"></a>

## 定时备份

**Scheduled Backups** 区域会自动创建备份：

| 字段 | |
|---|---|
| **Create backups automatically** | 开启定时备份。 |
| **Schedule (cron)** | 创建备份的时间，参见[下文](#schedule-syntax)。字段下方会显示接下来三次的执行时间。 |
| **Keep scheduled backups** | 保留的定时备份数量，更早的备份会被删除。`0` 表示全部保留。手动备份和上传的备份永远不会被删除。 |

点击 **Save Schedule** 使其生效。状态行会显示上一次定时备份的时间，以及下一次备份的预定时间。

如果到达预定时间时 Marginalia 没有运行（桌面应用常见的情况，它只在你写作时运行），它会在下次启动后约 30 秒补做错过的备份。

<a id="schedule-syntax"></a>

### 计划语法

计划是一个包含五个字段的 cron 表达式，`minute hour day-of-month month day-of-week`：

| 表达式 | 执行时间 |
|---|---|
| `0 3 * * *` | 每天 3:00 |
| `0 */6 * * *` | 每 6 小时 |
| `30 2 * * 1` | 每周一 2:30（星期几：`0` 或 `7` 为周日，`1` 为周一……） |
| `0 12 1 * *` | 每月 1 日中午 12:00 |
| `@daily`、`@weekly`、`@monthly` | 每天 / 每周 / 每月午夜执行一次（`@hourly`、`@yearly` 同样可用） |

时间以服务器的时区为准（在 Docker 中，通过 `TZ` 环境变量设置）。

!!!
要关闭定时备份，取消勾选 *Create backups automatically*，然后点击 *Save Schedule*。表达式会被保留，因此以后可以重新开启备份。表达式仍会被检查——无效的表达式永远不会被保存——但在备份关闭期间，该字段也可以为空。
!!!

<a id="restoring"></a>

## 还原

数据库在 Marginalia 使用期间无法被替换，因此还原会在**下次启动**时进行：

1. 点击备份旁边的 **Restore on restart** 并确认。标签页会显示 *Database restore is scheduled. Restart the application to apply it.* —— 点击 **Cancel Restore** 可撤销。
2. 重启 Marginalia：退出并重新启动[桌面应用](../getting-started/desktop-app.md)，或在[服务器](../getting-started/docker-server.md)上执行 `docker compose restart`。

启动时，当前数据库会被移动到 `db-backups/pre-restore-<time>-marginalia.sqlite`——它会出现在列表中，因此还原可以通过还原该文件来撤销——备份则取而代之。来自较旧 Marginalia 版本的备份会被自动升级。

备份在被替换进来之前会再次检查（与 *Upload Backup* 相同的检查；*Restore on restart* 同样会拒绝未通过检查的备份）。如果检查未通过，或升级失败，Marginalia 会保留（或恢复）当前数据库并正常启动；被拒绝的文件会被移动到 `db-backups/rejected-restore-<time>-marginalia.sqlite`，日志中会说明原因。

备份之后的所有内容都会丢失：此后创建的书籍、段落和设置。已登录的用户如果账户有变动，需要重新登录。

<a id="copy-before-an-upgrade"></a>

## 升级前的副本

当新版本的 Marginalia 将要改变现有数据库的结构时，它会先保存一份副本，命名为 `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`，其中的版本号是该数据库升级前所处的版本。该副本会出现在备份列表中，因此可以通过安装旧版 Marginalia 并[还原](#restoring)该副本来撤销升级。系统会保留最近三份副本。全新安装没有需要复制的内容；如果无法生成副本（例如磁盘已满），日志会说明情况，升级仍会继续。

迁移如果中途失败，会自动回滚，Marginalia 也不会启动。这份副本用于应对已顺利完成、但效果不符合你预期的升级。

<a id="moving-to-another-computer"></a>

## 迁移到另一台电脑

1. 创建一个备份并**下载**它。
2. 在新电脑上安装 Marginalia，首次启动时创建一个临时管理员。
3. **Upload Backup**，**Restore on restart**，然后重启。
4. 使用备份中的账户登录。

同时也要从旧的[数据文件夹](index.md#the-data-folder)中复制 `data` 文件夹（书籍备份和段落插图）以及 `extensions` 文件夹。或者，直接停止 Marginalia，然后复制整个 `.marginalia` 文件夹。
