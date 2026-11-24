---
label: 管理扩展
order: 70
---

# 管理扩展

管理页面中的 **Extensions** 标签页用于为整个安装实例安装和移除[扩展](../extensions/index.md)。

![Extensions 标签页](../../images/admin-extensions.png)

<a id="installing"></a>

## 安装

点击 **Load Extension (.jar)**（或将文件拖到该按钮上），然后选择扩展的 JAR 文件。文件会被复制到[数据文件夹](index.md#the-data-folder)中的 `extensions` 文件夹，并立即启动；*Extension loaded successfully.* 会确认加载成功。扩展随后会出现在列表中：

| 列 | |
|---|---|
| **ID** | 扩展在本次运行中的内部编号。 |
| **Extension Name** | 扩展的名称。 |
| **Version** | 扩展的版本。旁边的 **#** 按钮在鼠标悬停时会显示扩展 JAR 的 SHA-256 哈希值；点击即可复制该哈希，例如用于与作者发布的哈希值进行比对。 |
| **State** | *Active* 表示正在运行；*Stopped* 表示你已将其停止（参见[停止与启动](#stopping-and-starting)）；*Installed* 或 *Resolved* 表示启动失败（参见[扩展加载失败时](#when-an-extension-fails)）。 |
| **Verification** | *Valid* 或 *Invalid*；点击可查看 Marginalia 在启动扩展前所做检查的报告。 |
| **Actions** | **Stop** / **Start**、**Log** 和 **Unload**。 |

用户从此之后打开的窗口中就能看到该扩展。已经打开的书籍需要关闭后重新打开，或刷新页面。

也可以直接将 JAR 文件复制到 `extensions` 文件夹中，例如在无法访问界面的服务器上操作；这些文件会在下次启动时加载。这也是准备 Docker 部署的方法：将 JAR 文件放入 `./data/extensions`。JAR 文件也可以在 Docker 中构建，参见服务器指南中的[插件](../getting-started/docker-server.md#plugins)。

!!!warning 使用为本版本构建的扩展
扩展会改变 Marginalia 某一版本用户界面的部分内容。请使用与应用程序出自同一源代码构建的扩展（参见[插件 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)）。
不兼容的扩展可能无法加载，或破坏应用程序的部分功能。扩展 API 在 1.0 中尚未冻结，因此请预期需要与 Marginalia 一同更新扩展。
!!!

<a id="stopping-and-starting"></a>

## 停止与启动

**Stop** 会立即卸载扩展，但不会将其删除：它会从其添加到的界面中消失，但其 JAR 文件和数据会保留，并在列表中显示为 *Stopped* 状态。此时按钮变为 **Start**，点击即可重新加载。加载前会先进行检查，与应用程序启动时相同；如果检查失败，扩展将保持停止状态，并显示报告。已停止的扩展在 Marginalia 重启后仍保持停止。上传已停止扩展的新版本会将其启动。

停止状态通过 `extensions` 文件夹中的一个空文件来记录。该文件与 JAR 同名，并附加 `.STOPPED`（即 `sidequery.jar.STOPPED`）。在无法访问界面的服务器上，创建该文件可以阻止扩展启动；删除该文件则可使扩展在下次启动时加载。

<a id="the-extension-log"></a>

## 扩展日志

**Log** 会打开扩展的日志，其中包含扩展通过 Marginalia 提供给扩展的日志记录器（[日志](../../developer/plugins/overview.md#logging)）所写入的内容。它显示文件的最后一部分；**Refresh** 会重新读取文件，**Clear** 则会删除日志。日志文件为 `extensions/<name>.jar.log`（例如 `sidequery.jar.log`）。当其超过 2 MB 时，会被移动到 `.log.old`，并开始一个新的日志文件。扩展卸载时，日志会被删除。不记录任何内容的扩展，其日志为空。扩展所记录的内容也会写入应用程序日志（名为 `extension.<name>`，按行记录级别），因此服务器的日志中同样包含这些内容；Marginalia 本身的问题则仅出现在该日志中。

![扩展的日志](../../images/extension-log.png)

<a id="removing"></a>

## 移除

**Unload** 会先要求确认，然后立即停止扩展，并从 `extensions` 文件夹中删除其 JAR 文件（以及其日志）。扩展的数据会保留（参见[扩展数据的存放位置](../extensions/index.md#where-extension-data-is-kept)）；重新加载该扩展时，数据会恢复。

<a id="updating"></a>

## 更新

要安装扩展的新版本，请使用新文件执行 **Load Extension (.jar)**，文件名无关紧要。已安装的版本会被卸载，其 JAR 文件被删除，新版本随即在原位置启动。如果新版本加载失败，上传时会显示错误，而旧版本则继续运行。

也可以通过脚本安装、更新、停止和移除扩展，但需要使用具有 *Manage the extensions* 权限的 [API 密钥](../account.md#api-keys)。脚本可以先比较服务器运行的提交与插件构建所用源代码的提交，以判断在不更新 Marginalia 本身的情况下，插件能否直接部署（参见[开发者手册](../../developer/api.md#plugins-without-a-full-update)）。

<a id="when-an-extension-fails"></a>

## 扩展加载失败时

如果上传的扩展无法加载，上传时会显示 *Failed to load extension: ...*，且其 JAR 文件不会被保留。

Marginalia 在启动扩展前，会先将其与当前运行的版本进行检查。不匹配的扩展（通常是为其他版本的 Marginalia 构建的）不会被加载。上传时会显示检查报告，且该扩展不会被保留。位于 `extensions` 文件夹中的扩展会留在列表中但不会启动，其 *Verification* 列显示 **Invalid**，报告则记录在日志中。Marginalia 更新后会重新进行检查，因此之前能正常工作的扩展之后可能会被拒绝：请获取为新版本 Marginalia 构建的扩展版本，并重新加载。扩展处于无效状态时，工作区顶部会出现一个**红色提示条**，提醒所有用户。管理员将鼠标悬停其上即可看到哪些扩展无效，点击提示条即可打开本页面；其他用户则会被提示告知管理员（你自己停止的扩展不计入其中）。每个扩展的 JAR 文件旁边都有一个小的 `.valid` 或 `.invalid` 文件，其中包含报告。它会随扩展一同被删除，也可以保留不动。

![扩展无效时覆盖在工作区上方的红色提示条](../../images/invalid-extensions-warning.png)

![不匹配扩展的检查报告，以及已停止的扩展（Start 按钮）](../../images/extension-verification.png)

位于 `extensions` 文件夹中、启动失败的扩展（例如为其他版本构建的扩展）不会妨碍其他扩展的加载。它会以 *Installed* 或 *Resolved* 状态（而非 *Active*）显示在列表中，可以使用 **Unload** 移除。不是扩展的文件会被跳过且不会列出，请从 `extensions` 文件夹中删除该文件。错误详情记录在日志中（`docker compose logs`，桌面应用则为 `desktop/logs/marginalia.log`）。
