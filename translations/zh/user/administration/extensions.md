---
label: 管理扩展
order: 70
---

# 管理扩展

管理页面的 **Extensions** 标签页为整个安装实例安装和移除[扩展](../extensions/index.md)。

![Extensions 标签页](../../images/admin-extensions.png)

<a id="installing"></a>

## 安装

点击 **Load Extension (.jar)**（或将文件拖到该按钮上），然后选择扩展的 JAR 文件。文件会被复制到[数据文件夹](index.md#the-data-folder)中的 `extensions` 文件夹，并立即启动；*Extension loaded successfully.* 表示加载成功。扩展随后会出现在列表中：

| 列 | |
|---|---|
| **ID** | 本次运行中该扩展的内部编号。 |
| **Extension Name** | 扩展名称。 |
| **Version** | 扩展版本。其旁边的 **#** 按钮在鼠标悬停时显示该扩展 JAR 的 SHA-256 哈希值；点击即可复制该哈希，例如用于与作者公布的哈希进行比对。 |
| **State** | 运行中显示 *Active*；你手动停止时显示 *Stopped*（见[停止和启动](#stopping-and-starting)）；启动失败时显示 *Installed* 或 *Resolved*（见[扩展失败时](#when-an-extension-fails)）。 |
| **Verification** | *Valid* 或 *Invalid*；点击可查看 Marginalia 在启动扩展前所做检查的报告。 |
| **Actions** | **Stop** / **Start**、**Log** 和 **Unload**。 |

此后用户打开的窗口中就会看到该扩展。已经打开的书籍需要关闭后重新打开，或刷新页面。

也可以直接将 JAR 文件复制到 `extensions` 文件夹中，例如在无法访问界面的服务器上；这些文件会在下次启动时加载。Docker 部署也可以通过这种方式预先准备：将 JAR 放入 `./data/extensions`。JAR 同样可以在 Docker 中构建，参见服务器指南中的[插件](../getting-started/docker-server.md#plugins)。

!!!warning 请使用为当前版本构建的扩展
扩展会修改某个 Marginalia 版本的用户界面部分内容。请使用与应用程序基于同一源代码构建的扩展（参见[插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)）。
不兼容的扩展可能无法加载，或导致应用程序的部分功能出错。1.0 版本的扩展 API 尚未冻结，因此请预期需要与 Marginalia 一同更新扩展。
!!!

<a id="stopping-and-starting"></a>

## 停止和启动

**Stop** 会立即卸载扩展，但不会删除它：扩展会从其添加功能的界面中消失，但其 JAR 和数据保留，并在列表中显示为 *Stopped* 状态。此时按钮变为 **Start**，点击即可重新加载扩展（加载前会先进行检查，与应用程序启动时相同；如果检查失败，扩展将保持停止状态，并显示报告）。Marginalia 重启后，已停止的扩展仍保持停止。上传已停止扩展的新版本会将其启动。

停止状态由 `extensions` 文件夹中一个与 JAR 同名、并在末尾加上 `.STOPPED` 的空文件记录（例如 `sidequery.jar.STOPPED`）。在无法访问界面的服务器上，若要阻止扩展启动，可创建该文件；若要让扩展在下次启动时运行，则删除该文件。

<a id="the-extension-log"></a>

## 扩展日志

**Log** 会打开扩展的日志：即扩展通过 Marginalia 提供给扩展的日志记录器所写入的内容（[日志记录](../../developer/plugins/overview.md#logging)）。它显示文件的最后一部分；**Refresh** 重新读取文件，**Clear** 删除文件。日志文件为 `extensions/<name>.jar.log`（例如 `sidequery.jar.log`）；当其超过 2 MB 时，会被移动为 `.log.old`，并开始一个新的日志文件。扩展被卸载时，日志会被删除。不记录任何内容的扩展，其日志为空。扩展写入的日志也会输出到应用程序日志中（名称为 `extension.<name>`，级别与该行相同），因此也会出现在服务器日志中；Marginalia 自身的问题则只记录在应用程序日志中。

![扩展的日志](../../images/extension-log.png)

<a id="removing"></a>

## 移除

**Unload** 会先要求确认，然后立即停止扩展，并从 `extensions` 文件夹中删除其 JAR（以及日志）。
其数据会保留（见[扩展数据的存放位置](../extensions/index.md#where-extension-data-is-kept)）；重新加载该扩展即可恢复其数据。

<a id="updating"></a>

## 更新

要安装扩展的较新版本，请使用新文件通过 **Load Extension (.jar)** 上传——文件名无关紧要。已安装的版本会被卸载，其 JAR 被删除，新版本随即替代它启动。如果新版本加载失败，上传会显示错误，旧版本继续运行。

<a id="when-an-extension-fails"></a>

## 扩展失败时

如果上传的扩展无法加载，上传会显示 *Failed to load extension: ...*，且其 JAR 不会被保留。

Marginalia 在启动扩展前，会先将其与当前运行的版本进行比对。不匹配的扩展——通常是为其他版本的 Marginalia 构建的扩展——不会被加载。上传时会显示检查报告，且该扩展不会被保留；若扩展位于 `extensions` 文件夹中，则仍会留在列表里，但不会启动，*Verification* 列显示 **Invalid**，报告则记录在日志中。Marginalia 更新后会再次进行检查，因此之前能正常工作的扩展之后可能被拒绝：请获取为新版本 Marginalia 构建的扩展版本，并加载它。扩展无效期间，**工作区顶部会显示一条红色横条**，提醒所有人：管理员将鼠标悬停其上即可看到哪些扩展无效，点击横条即可打开本页面；其他用户则会被提示告知管理员（你自己停止的扩展不计入其中）。每个扩展的 JAR 旁边都有一个名为 `.valid` 或 `.invalid` 的小文件，内含报告；它会随扩展一同被删除，也可以保留不管。

![扩展无效时工作区上方的红色横条](../../images/invalid-extensions-warning.png)

![不匹配扩展的验证报告，以及一个已停止的扩展（Start 按钮）](../../images/extension-verification.png)

`extensions` 文件夹中如果有扩展启动失败（例如为其他版本构建的扩展），不会妨碍其他扩展的加载。它在列表中会显示为 *Installed* 或 *Resolved*，而非 *Active*，可以通过 **Unload** 移除。如果某个文件根本不是扩展，则会被跳过且不会列出——请从 `extensions` 文件夹中删除它。错误详情可在日志中查看（`docker compose logs`，或桌面应用的 `desktop/logs/marginalia.log`）。
