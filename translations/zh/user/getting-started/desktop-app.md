---
label: 桌面应用
order: 100
---

# 桌面应用

桌面应用是供单台计算机使用的独立版 Marginalia。它自带 Java 运行时和服务器，只监听您的计算机，并在默认浏览器中打开。

<a id="download"></a>

## 下载

从[发布页面](https://github.com/Enerccio/Marginalia/releases)下载适用于您系统的安装包或压缩包。每个系统都发布四种版本：

| 文件 | 内容 |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | 安装包：Linux 上为 `.deb`（适用于 Debian、Ubuntu 及其衍生版），macOS 上为 `.dmg`，Windows 上为 `.msi` |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | 相同的安装包，并且**已预装所有内置[插件](../extensions/index.md)**（见下文） |
| `marginalia-<version>-<os>-<arch>-app` | 应用程序：macOS 上为 `Marginalia.app`，Windows 上为 `Marginalia.exe`，Linux 上为 `bin/Marginalia` |
| `marginalia-<version>-<os>-<arch>` | 便携文件夹，通过脚本启动，可使用命令行[选项](#options) |

提供以下构建版本：Windows x64、macOS（Apple Silicon 与 Intel）以及 Linux（x64 与 arm64）。文件名中的 `<arch>` 对于 64 位 Intel 和 AMD 处理器（Windows、macOS Intel、Linux）为 `x86_64`，对于 ARM（Apple Silicon、Linux arm64）为 `aarch64`。

<a id="install-and-start"></a>

## 安装与启动

+++ macOS
使用安装包：打开 `.dmg`，将 *Marginalia* 拖到 *Applications*。使用压缩包：

1. 解压压缩包，并将 `Marginalia.app` 移至 *Applications*。
2. 打开它。该应用尚未签名，因此 macOS 第一次会拒绝打开。右键点击应用并选择 *Open*，或运行：

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
使用安装包：运行 `.msi`。它会为当前用户安装（无需管理员权限），在开始菜单和桌面添加 Marginalia，较新的 `.msi` 会升级已安装的版本（您的数据保存在 `.marginalia` 中）。使用压缩包：

1. 解压压缩包。
2. 运行 `Marginalia\Marginalia.exe`。
3. SmartScreen 可能会提示未知发布者。点击 *More info* → *Run anyway*。
+++ Linux
使用 `.deb`（适用于 Debian、Ubuntu 及其衍生版）：

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

它会安装到 `/opt/marginalia`，并在应用菜单中添加 *Marginalia*。`sudo apt remove marginalia` 可将其删除（您在 `~/.marginalia` 中的数据会保留）。使用压缩包：

1. 解压压缩包。
2. 运行 `Marginalia/bin/Marginalia`。
+++ 便携文件夹
1. 解压压缩包。
2. 运行 `bin/marginalia`（Windows 上为 `bin\marginalia.bat`）。
+++

启动需要几秒钟。随后 Marginalia 会在浏览器中打开 `http://127.0.0.1:8765`，并显示[首次启动](first-start.md)对话框。

<a id="installer-with-the-plugins"></a>

### 带插件的安装包

**完整版**安装包（文件名中含 `-full`）与普通版是同一个应用，并包含 Marginalia 的所有插件（Anthropic 和 Google 提供商、Reviewer、Beta Readers、Side Query、Arc Outline、各类生成器等）。Marginalia 首次启动时，插件会被复制到 `~/.marginalia/extensions`，并出现在 *Admin → Extensions* 中。使用普通安装包时，您需要自己将这些插件逐个安装到该目录。

- 插件只安装一次。如果您在 *Admin → Extensions* 中将其移除，它将保持移除状态，即使更新到较新版本的 Marginalia 之后也是如此（新版本会带来更新的插件，并将其与旧版本放在一起；Marginalia 使用最新版本，并将旧版 JAR 保留在 `extensions/old-versions` 中）。更新中新增的插件会被安装。
- 用完整版安装包覆盖普通版（或反之），会保留您的数据和设置，每次更新同样如此。
- 普通安装包不会删除插件。您自己安装的插件永远不会被改动。

<a id="tray-icon"></a>

## 托盘图标

运行期间，Marginalia 会在系统托盘（macOS 上为菜单栏）中显示其图标（一张手稿页面，页边带有一棵小型分支树）。其菜单有两项：

- **Open Marginalia** - 再次在浏览器中打开 Marginalia，例如在您关闭标签页之后。
- **Quit Marginalia** - 停止服务器。关闭浏览器标签页不会停止 Marginalia。

应用已在运行时再次启动，只会在浏览器中打开它。

![托盘图标及其菜单](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## 数据存放位置

| 内容 | 位置 |
|---|---|
| 数据库、备份、扩展 | `~/.marginalia`（Windows 上为 `%USERPROFILE%\.marginalia`） |
| 日志 | `~/.marginalia/desktop/logs/marginalia.log`（上一次运行的日志保存为 `marginalia.log.1`） |

要将 Marginalia 迁移到另一台计算机，请退出它，然后复制 `.marginalia` 文件夹（包括 `secret.key`，它用于解密已保存的 API 密钥）。在进行较大改动之前，请先进行[数据库备份](../administration/database-backups.md)。

<a id="options"></a>

## 选项

便携式启动器接受以下选项：

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning 其他地址
使用 `--host 0.0.0.0`（或其他地址）时，Marginalia 将可通过普通 HTTP 从您的网络访问。若要从其他设备访问，请改用经 HTTPS 保护的[服务器](docker-server.md)。
!!!

<a id="updating"></a>

## 更新

退出 Marginalia，用新版本替换应用（或便携文件夹），然后重新启动。您的数据保留在 `.marginalia` 中，并会在首次启动时自动升级。在数据库结构发生变化之前，Marginalia 会将其[副本](../administration/database-backups.md#copy-before-an-upgrade)保存到 `db-backups` 中。扩展是为特定 Marginalia 版本构建的，因此请同时更新它们（参见[管理扩展](../administration/extensions.md)）。

<a id="uninstalling"></a>

## 卸载

删除应用或文件夹。若要同时删除您的数据，请删除 `.marginalia` 文件夹。
