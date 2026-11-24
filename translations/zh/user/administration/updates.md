---
label: 更新
order: 65
---

# 更新

在从 Git 检出目录构建的 [Docker 服务器](../getting-started/docker-server.md)上，管理员可以在管理页面的 **Update** 标签页中更新 Marginalia，无需登录服务器：服务器会拉取其检出目录所在分支的最新版本，构建应用和[随附插件](../getting-started/docker-server.md#plugins)，然后重新启动。

这需要在服务器上运行一个小程序，即 **更新监视器**（update watcher），它只需[设置一次](#setting-up-the-watcher)。没有它时，该标签页只显示正在运行的版本，并提示监视器未运行。[桌面应用](../getting-started/desktop-app.md)以及使用[预构建镜像](../getting-started/docker-server.md#prebuilt-image)的服务器也是如此：它们通过安装新版本来更新。

<a id="the-update-tab"></a>

## Update 标签页

| | |
|---|---|
| **Running version** | 正在运行的 Marginalia 的版本及提交。 |
| **Branch** | 服务器上检出目录所在的分支，以及它跟踪的远程分支（例如 `dev`，跟踪 `origin/dev`）、该远程分支的最新提交，以及服务器上次检查的时间。服务器每 15 分钟检查一次。 |
| 状态 | 当最新提交不是正在运行的提交时显示 *A new version is available*，否则显示 *Marginalia is up to date*。更新进行期间，标签页会显示相应提示。 |
| **Last update** | 从此处启动的上一次更新结束的时间，以及它是成功完成还是失败。 |

服务器跟踪的是那里检出的分支，无论是哪个分支：要将安装切换到另一分支，请在服务器上的检出目录中使用 `git switch` 切换；标签页会在下一次检查后显示新分支。

| 按钮 | |
|---|---|
| **Update and restart** | 在确认后请求服务器进行更新。即使没有新内容，也会构建并重新启动（例如在修改 `docker-compose.yml` 之后使用）。 |
| **Refresh** | 重新读取状态。标签页不会自动刷新。 |
| **Log** | 上一次更新的输出：Git 和构建过程写入的内容。 |

也可以通过脚本完成相同操作，使用具有 *Update the installation* 权限的 [API 密钥](../account.md#api-keys)：读取状态、请求更新并读取日志（参见[开发者手册](../../developer/api.md)）。

<a id="what-an-update-does"></a>

## 更新会做什么

1. 构建新版本：`git pull`、插件和镜像。这需要几分钟，期间 **Marginalia 保持运行**。标签页显示 *The update is running*；**Log** 显示进度。
2. 停止 Marginalia，将新插件复制到 `data/extensions`（替换已安装的插件），然后启动新版本。这需要一两分钟（参见[健康检查](../getting-started/docker-server.md#health)）。所有正在工作的用户都会被打断，正在生成的内容也会被停止：确认对话框会告诉你此时有多少[任务](jobs.md)正在运行，因此请选择空闲时段。
3. 新版本启动时会升级数据库，升级前会先保存一份[数据库副本](database-backups.md#copy-before-an-upgrade)。你也应先自行创建一份[数据库备份](database-backups.md)。

如果构建失败，不会停止任何服务：之前的版本继续运行，**Last update** 显示 *failed*，日志会说明原因。下一次更新会重新构建，即使没有新内容可拉取也是如此。

<a id="setting-up-the-watcher"></a>

## 设置监视器

Marginalia 运行在容器内，既无法访问 Git 检出目录，也无法访问 Docker；若赋予其该权限，则任何能进入应用的人都将成为服务器管理员。因此它只负责请求：它会向其[数据文件夹](index.md#the-data-folder)写入一个文件，而在容器外、运行于服务器上的监视器负责实际工作。监视器是检出目录中的脚本 `tools/update-watcher/watch.sh`；它只会运行同一检出目录中的 `rebuild.sh`（参见[自动更新](../getting-started/docker-server.md#automatic-updates)）。

<a id="linux-with-systemd"></a>

### 使用 systemd 的 Linux

在服务器上的检出目录中（即包含 `docker-compose.yml` 的文件夹）：

```sh
sudo tools/update-watcher/install.sh
```

它会安装并启动三个单元：

| 单元 | |
|---|---|
| `marginalia-update.service` | 只运行一次监视器。 |
| `marginalia-update.path` | 应用请求更新的那一刻启动它。 |
| `marginalia-update.timer` | 开机两分钟后启动，之后每 15 分钟启动一次，以检查新提交。 |

打开 **Admin → Update**，点击 **Refresh**：标签页会显示分支和最新提交。就这些。

| 选项 | |
|---|---|
| `--name NAME` | 单元的名称（默认为 `marginalia-update`）。同一台计算机上的两个检出目录（测试环境和生产环境）各需要自己的名称。 |
| `--user USER` | 以此用户而非 root 身份运行监视器，参见[下文](#who-the-watcher-runs-as)。 |
| `--uninstall` | 停止并删除单元（需带相同的 `--name`）。 |

`systemctl status marginalia-update.service` 显示上一次运行情况，`journalctl -u marginalia-update.service` 显示其输出（与 **Log** 中的文本相同，以及监视器本身无法工作时的原因）。单元中包含检出目录的路径：移动检出目录后，请重新运行 `install.sh`。

<a id="without-systemd-cron"></a>

### 不使用 systemd（cron）

每分钟运行一次监视器；如果没有人请求更新，并且它在 15 分钟内已检查过远程仓库，则不会执行任何操作，因此几乎没有开销：

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

将该行加入 root 的 crontab（`sudo crontab -e`），或其他用户的 crontab，参见下文。之后，更新会在点击后一分钟内开始。

<a id="settings"></a>

### 设置

监视器读取以下环境变量（使用 systemd 时：位于 `[Service]` 下的 `sudo systemctl edit marginalia-update.service` 和 `Environment=NAME=value` 行）：

| 变量 | |
|---|---|
| `MARGINALIA_DATA_DIR` | 服务器上的数据文件夹，当它不是检出目录中的 `data` 时使用（即 `docker-compose.yml` 中卷映射的左侧）。 |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh` 的参数，默认为 `--force`。若服务器内存不足以同时运行应用和构建，请使用 `--force --stop-first`：此时 Marginalia 会在构建前停止，并在整个构建期间处于停机状态。 |
| `MARGINALIA_CHECK_MINUTES` | 在没有请求的情况下运行时，状态可以陈旧到何种程度才会再次检查远程仓库，默认为 15。使用 systemd 时，由定时器决定监视器的运行频率。 |

<a id="who-the-watcher-runs-as"></a>

### 监视器以谁的身份运行

默认情况下，监视器以 **root** 身份运行：它必须使用 Docker，并写入数据文件夹，而该文件夹属于容器的用户。Git 本身以检出目录的所有者身份运行，因此 Git 的文件仍归其所有。

监视器运行分支中包含的内容（`rebuild.sh`、`Dockerfile`），手动更新时也是如此：只跟踪你信任的仓库，并且只允许检出目录的所有者写入，因为任何能在那里修改 `rebuild.sh` 或 `watch.sh` 的人，都会以监视器用户的身份运行命令。

使用 `--user USER`（或该用户的 crontab）时，它以其他用户身份运行。该用户必须有权使用 Docker（在大多数系统上，这等同于拥有 root 权限），必须拥有检出目录，并且必须能够写入 `data/update` 和 `data/extensions`，这两者通常属于容器的用户。

<a id="windows-and-macos"></a>

### Windows 和 macOS

对于 Docker Desktop，没有现成的监视器。请手动使用 `rebuild.bat` / `rebuild.sh` 更新，或通过计划任务更新，参见[自动更新](../getting-started/docker-server.md#automatic-updates)。

<a id="troubleshooting"></a>

## 故障排除

| | |
|---|---|
| *The update watcher does not run on the server* | 监视器一小时内没有写入状态，或从未写入。请检查 `systemctl status marginalia-update.timer marginalia-update.path` 和 `journalctl -u marginalia-update.service`。如果数据文件夹不是检出目录中的 `data`，请设置 `MARGINALIA_DATA_DIR`。 |
| *Branch ... follows no remote branch* | 检出目录位于没有上游分支的分支上（或处于分离的提交上），因此没有可拉取的内容。在服务器上执行：`git switch <branch>` 或 `git branch --set-upstream-to=origin/<branch>`。 |
| 服务器无法访问 Git 仓库 | `git fetch` 在上次检查时失败（没有网络，或私有仓库没有检出目录所有者的凭据）。所显示的最新提交可能已过时。 |
| 已请求更新，但没有任何反应 | 使用 cron 时，它会在一分钟内开始。一小时内无人处理的请求会被丢弃，以免监视器修复后意外启动。 |
| *Last update: ..., failed* | 查看 **Log**。`git pull` 失败通常意味着检出目录有自己的改动（已编辑的文件、本地提交）：请在服务器上解决。构建失败时，旧版本会继续运行。 |

监视器的文件位于数据文件夹中的 `update` 内：`status.properties`（标签页显示的内容）、`update.log`（上一次更新）以及短暂存在的 `request`。
