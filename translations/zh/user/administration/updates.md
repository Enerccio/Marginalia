---
label: 更新
order: 65
---

# 更新

在从 Git 检出目录构建的 [Docker 服务器](../getting-started/docker-server.md)上，管理员无需登录服务器，即可在管理页面的 **Update** 标签页中更新 Marginalia：服务器会拉取检出目录所在分支的最新版本，构建应用和[内置插件](../getting-started/docker-server.md#plugins)，然后重启。

这需要服务器上的一个小程序，即**更新监视器**（update watcher），它需要[先设置一次](#setting-up-the-watcher)。没有它，该标签页只会显示正在运行的版本，并说明监视器未运行。[桌面应用](../getting-started/desktop-app.md)以及使用[预构建镜像](../getting-started/docker-server.md#prebuilt-image)的服务器也是如此：它们通过安装新版本来更新。

<a id="the-update-tab"></a>

## Update 标签页

| | |
|---|---|
| **Running version** | 正在运行的 Marginalia 的版本和提交。 |
| **Branch** | 服务器上检出目录所在的分支，以及它跟踪的远程分支（例如 `dev`，跟踪 `origin/dev`），该远程分支的最新提交，以及服务器上次检查的时间。服务器每 15 分钟检查一次。 |
| Status | 当最新提交不是正在运行的提交时显示 *A new version is available*，否则显示 *Marginalia is up to date*。更新进行时，标签页会显示相应状态。 |
| **Last update** | 从此处发起的上一次更新何时结束，以及它是成功还是失败。 |

服务器会跟踪其中检出的分支，无论是哪个分支：要将安装切换到另一个分支，请在服务器上的检出目录中执行 `git switch`；下一次检查后，标签页会显示新分支。

| 按钮 | |
|---|---|
| **Update and restart** | 在确认后请求服务器进行更新。即使没有新内容，它也会构建并重启（例如修改 `docker-compose.yml` 之后使用）。 |
| **Refresh** | 重新读取状态。标签页不会自动刷新。 |
| **Log** | 上一次更新的输出：Git 和构建写入的内容。 |

<a id="what-an-update-does"></a>

## 更新会做什么

1. 构建新版本：`git pull`、插件、镜像。这需要几分钟，期间 **Marginalia 保持运行**。标签页会显示 *The update is running*；**Log** 会显示进度。
2. Marginalia 停止，新插件被复制到 `data/extensions`（替换已安装的插件），然后新版本启动。这需要一两分钟（参见[健康检查](../getting-started/docker-server.md#health)）。所有正在工作的用户都会被打断，正在生成的内容也会被停止：确认对话框会告诉你此时有多少[任务](jobs.md)在运行，因此请选择一个空闲时段。
3. 新版本启动时会升级数据库，升级前会先保存[数据库的副本](database-backups.md#copy-before-an-upgrade)。你也应先自行创建一份[数据库备份](database-backups.md)。

如果构建失败，不会停止任何东西：之前的版本继续运行，**Last update** 显示 *failed*，日志会说明原因。下一次更新会重新构建，即使没有新内容可拉取。

<a id="setting-up-the-watcher"></a>

## 设置监视器

Marginalia 运行在容器中，既无法访问 Git 检出目录，也无法访问 Docker；若给它这种权限，每个进入应用的人都将成为服务器的管理员。因此它只负责提出请求：它会在其[数据文件夹](index.md#the-data-folder)中写入一个文件，而运行在容器外、服务器上的监视器则负责实际工作。监视器是检出目录中的脚本 `tools/update-watcher/watch.sh`；它只会运行同一检出目录中的 `rebuild.sh`（参见[自动更新](../getting-started/docker-server.md#automatic-updates)）。

<a id="linux-with-systemd"></a>

### 使用 systemd 的 Linux

在服务器上的检出目录中（即包含 `docker-compose.yml` 的文件夹）执行：

```sh
sudo tools/update-watcher/install.sh
```

它会安装并启动三个单元：

| 单元 | |
|---|---|
| `marginalia-update.service` | 运行一次监视器。 |
| `marginalia-update.path` | 应用请求更新的那一刻启动它。 |
| `marginalia-update.timer` | 开机两分钟后启动，之后每 15 分钟启动一次，以检查远程是否有新提交。 |

打开 **Admin → Update** 并点击 **Refresh**：标签页会显示分支和最新提交。仅此而已。

| 选项 | |
|---|---|
| `--name NAME` | 单元的名称（默认为 `marginalia-update`）。同一台计算机上的两个检出目录（测试和生产安装）各自需要不同的名称。 |
| `--user USER` | 以该用户而非 root 运行监视器，参见[下文](#who-the-watcher-runs-as)。 |
| `--uninstall` | 停止并删除这些单元（需使用相同的 `--name`）。 |

`systemctl status marginalia-update.service` 显示上一次运行情况，`journalctl -u marginalia-update.service` 显示其输出（与 **Log** 内容相同，以及监视器自身无法工作时的原因）。这些单元中包含检出目录的路径：移动检出目录后，请重新运行 `install.sh`。

<a id="without-systemd-cron"></a>

### 不使用 systemd（cron）

每分钟运行一次监视器；如果没有人请求更新，且它在 15 分钟内已检查过远程，它就什么也不做，因此不会产生额外开销：

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

将该行写入 root 的 crontab（`sudo crontab -e`），或写入其他用户的 crontab，参见下文。这样，更新会在点击后一分钟内开始。

<a id="settings"></a>

### 设置

监视器读取以下环境变量（使用 systemd 时：执行 `sudo systemctl edit marginalia-update.service`，并在 `[Service]` 下添加 `Environment=NAME=value` 行）：

| 变量 | |
|---|---|
| `MARGINALIA_DATA_DIR` | 服务器上的数据文件夹，当它不是检出目录中的 `data` 时使用（即 `docker-compose.yml` 中卷的左侧）。 |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh` 的参数，默认为 `--force`。如果服务器的内存不足以同时运行应用和构建，请使用 `--force --stop-first`：这样 Marginalia 会在构建前停止，并在整个构建期间不可用。 |
| `MARGINALIA_CHECK_MINUTES` | 在没有请求的情况下运行时，状态可以陈旧到何种程度才再次检查远程，默认为 15。使用 systemd 时，由定时器决定监视器的运行频率。 |

<a id="who-the-watcher-runs-as"></a>

### 监视器以谁的身份运行

默认情况下，监视器以 **root** 身份运行：它必须使用 Docker，并写入数据文件夹，而该文件夹属于容器的用户。Git 本身以检出目录所有者的身份运行，因此 Git 的文件仍归其所有者所有。

监视器会运行分支中包含的内容（`rebuild.sh`、`Dockerfile`），与每次手动更新时一样：只跟踪你信任的仓库，并且只让检出目录的所有者拥有写权限，因为任何能够修改其中 `rebuild.sh` 或 `watch.sh` 的人，都将以监视器用户的身份运行命令。

使用 `--user USER`（或该用户的 crontab）时，它会以其他用户的身份运行。该用户必须被允许使用 Docker（在大多数系统上，这等同于拥有 root 权限），必须拥有检出目录，并且必须能够写入 `data/update` 和 `data/extensions`，这两个目录通常属于容器的用户。

<a id="windows-and-macos"></a>

### Windows 和 macOS

Docker Desktop 没有现成的监视器。请手动使用 `rebuild.bat` / `rebuild.sh` 更新，或通过计划任务更新，参见[自动更新](../getting-started/docker-server.md#automatic-updates)。

<a id="troubleshooting"></a>

## 故障排除

| | |
|---|---|
| *The update watcher does not run on the server* | 监视器一小时内未写入状态，或从未写入。请检查 `systemctl status marginalia-update.timer marginalia-update.path` 和 `journalctl -u marginalia-update.service`。如果数据文件夹不是检出目录中的 `data`，请设置 `MARGINALIA_DATA_DIR`。 |
| *Branch ... follows no remote branch* | 检出目录位于没有上游的分支上（或处于分离的提交上），因此没有可拉取的内容。在服务器上执行：`git switch <branch>` 或 `git branch --set-upstream-to=origin/<branch>`。 |
| *The server could not reach the Git repository* | 上次检查时 `git fetch` 失败（没有网络，或私有仓库没有为检出目录所有者提供凭据）。显示的最新提交可能已过时。 |
| 已请求更新，但什么也没有发生 | 使用 cron 时，更新会在一分钟内开始。若一个请求在一小时内无人处理，则会被丢弃，以免监视器修复后意外启动。 |
| *Last update: ..., failed* | 请阅读 **Log**。`git pull` 失败通常意味着检出目录有自己的更改（已编辑的文件、本地提交）：请在服务器上解决。构建失败则会保留旧版本继续运行。 |

监视器的文件位于数据文件夹的 `update` 目录中：`status.properties`（标签页显示的内容）、`update.log`（上一次更新）以及短暂存在的 `request`。
