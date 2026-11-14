---
label: 权限
order: 95
---

# 权限

权限让某个用户可以做一些并非所有人都能做的事。管理员逐个把权限授予用户；没有任何权限的用户仍可以像往常一样使用 Marginalia 的所有其他功能（编写书籍、构建设定集、使用管理员[共享](../inference-providers.md#shared-providers)的模型）。

| 权限 | 允许的操作 | 设置 |
|---|---|---|
| **Create own models** | 添加[推理提供方](../inference-providers.md)：没有此权限时，用户只能使用管理员共享的提供方；用户已有的提供方继续工作，并且仍可编辑。 | |
| **Help assistant** | 使用[帮助助手](help-assistant.md)，即根据手册回答问题的聊天功能。 | **Apply limits** 和 **Allow navigating the interface**，见[权限的设置](#settings-of-a-permission)。 |
| **Agents** | （实验性功能，见 [Agents](../agents.md)）[Agents](../agents.md) 标签页：创建智能体、克隆智能体，并让它们在某本书上运行。 | **Most agents**、**Concurrent runs**、**Requests to the model at once**、**Token budget of a run**，以及副本和文件的配额，见[权限的设置](#settings-of-a-permission)。 |
| **Version history** | 用户书籍的历史记录为每个部分保留的最多[版本数](../books/version-history.md#how-many-versions-are-kept)。每个用户默认为 50 个版本（没有此行的用户也是如此）；未授予该权限则表示该用户没有历史记录。 | **Versions kept per part**，这是文件夹、书籍和用户都无法超过的上限。留空则此处不设限制。新添加的权限默认为 50。 |
| **Notifications** | 当智能体需要用户而没有打开任何界面时，服务器发送到某个 URL 的[消息](../agents.md#when-you-are-not-there)。服务器会调用用户输入的地址，因此它是独立的权限：没有它，用户设置中就不会出现相关字段，也不会发送任何消息。 | **Allow addresses on this machine and in private networks**，见[权限的设置](#settings-of-a-permission)。 |

管理员始终拥有所有权限，因此无需授予（管理员的按钮和版块从不依赖权限）。新用户没有任何权限：在你[添加用户](users.md#adding-a-user)之后，应立即授予他们所需的权限。

<a id="giving-and-taking-permissions"></a>

## 授予和收回权限

**Admin → Users** 中用户所在行的钥匙按钮会打开该用户的权限窗口：

![用户的权限](../../images/permissions-dialog.png)

该窗口是一个表格，列出用户拥有的权限，共三列：

| 列 | |
|---|---|
| **Name** | 权限名称。带有设置的权限，其名称前有一个箭头，见[权限的设置](#settings-of-a-permission)。 |
| **Granted** | 权限是否生效。未授予的权限等同于没有该权限，但会保留其设置，因此你可以暂时关闭再重新开启。 |
| 垃圾桶按钮 | 永久删除该权限（及其设置）。 |

要授予权限，请点击表格上方的 **Add permission**，然后选择用户尚未拥有的一项权限；它会以已授予的状态添加到表格中。在你点击 **Save** 之前，任何内容都不会被保存：

![选择要添加的权限](../../images/permissions-add.png)

- 没有可保存的内容时，**Save** 处于禁用状态。
- **Exit** 关闭窗口；如果有未保存的更改，它会先询问，以免意外丢弃更改。
- 你删除的权限会保留在表格中，直到你保存；保存时才会永久删除。在此之前，只需用 **Exit** 关闭窗口而不保存，即可恢复该权限。

保存后，如果用户已登录，其页面会重新加载，因此依赖权限的按钮和版块会立即出现或消失。即使你修改的是自己的权限，你自己的页面也不会重新加载。

<a id="settings-of-a-permission"></a>

## 权限的设置

有些权限带有属于用户个人的设置，而不是属于整个安装实例的设置。这类权限所在行以箭头开头：点击它可展开设置（打开窗口时设置是折叠的，刚添加的权限则是展开的），再次点击即可折叠。折叠只是为了让长列表保持简短；它不会改变任何内容，也不算作未保存的更改。

![帮助助手权限的设置](../../images/permissions-settings.png)

**Help assistant**、**Agents** 和 **Notifications** 都有设置项。帮助助手的设置如下：

| 设置 | |
|---|---|
| **Apply limits** | 对于共享模型，用户的提问是否与其他任何请求一样计入其限额和用量。默认关闭，见[谁来付费](help-assistant.md#who-pays)。 |
| **Allow navigating the interface** | 当用户要求助手带他们找到某处时，允许助手打开某个版块、选中某个标签页，或打开用于添加内容的对话框，见[指引路径](help-assistant.md#showing-the-way)。默认关闭。 |

智能体的设置如下；留空字段即为默认值：

| 设置 | |
|---|---|
| **Most agents** | 用户最多可以拥有多少个智能体，包括智能体的克隆。留空时为五个。设为零则用户可以保留和运行已有的智能体，但不能创建或克隆新的智能体。删除一个智能体会释放其名额。 |
| **Concurrent runs** | 用户的运行任务中，最多有多少个可以同时进行。留空时为一个。 |
| **Requests to the model at once (per run)** | 一次运行中，最多有多少个模型请求可以同时在途，包括智能体及其[子智能体](../agents.md#sub-agents)的请求。留空时为一个：子智能体向模型提问时，智能体会等待，反之亦然。设为更大的值可以让子智能体与智能体并行工作（智能体所用的模型也可能有自己的[请求上限](../inference-providers.md#general-settings)，同样会生效）。 |
| **Token budget of a run** | 用户的一次运行最多可以消耗多少个令牌（包括发送和生成的）。留空或设为零则不设限制。 |
| **Working copies** | 用户的运行同时保留多少个书籍的工作副本。未完成的运行会保留其副本，暂停的运行也是如此；运行完成、停止或删除后会释放副本。留空或设为零则除智能体数量外不设其他限制。 |
| **Largest file (KB)** | 用户的智能体可以写入其工作区的最大文件。留空时为 1024（1 MB）。 |
| **Workspace size (MB)** | 用户的某个智能体工作区中所有文件合计可以占用的空间。留空时为 20。 |
| **Files in a workspace** | 用户的某个智能体工作区最多可以容纳多少个文件；文件夹不计入。留空时为 500。 |
| **Checkpoints of a run (MB)** | 一次运行的所有[检查点](../agents.md#checkpoints)（工作副本及其工作区的还原点）合计可以占用的空间；其中一部分保留给自动检查点。留空时为 100。 |

![Agents 权限的设置](../../images/permissions-settings-agents.png)

通知的设置如下：

| 设置 | |
|---|---|
| **Allow addresses on this machine and in private networks** | 一项子权限：只有在已授予 **Notifications** 时才能勾选（否则该选项呈灰色不可用，收回该权限时会自动清除此勾选）。未勾选时，只会调用互联网上的地址：不包括 `localhost`、私有网络（`10.x.x.x`、`172.16-31.x.x`、`192.168.x.x`）、链路本地或共享地址（`169.254.x.x`，云主机通常在此提供元数据服务；`100.64-127.x.x`），也不包括 IPv6 的唯一本地地址。如果用户的 ntfy 或 Gotify 运行在同一台机器上或你自己的网络中，请为该用户勾选此项。默认关闭。域名会根据其解析出的地址进行检查，因此解析到私有地址的域名同样会被拒绝，无法解析的域名也会被拒绝。 |

管理员既不需要该权限，也不需要该子权限：他们能收到消息，并且可以使用本地地址。在与你不完全信任的人共用的服务器上，请保持此子权限关闭：否则服务器会向用户输入的任何地址发送请求，包括你网络内部的地址。

<a id="when-permissions-were-introduced"></a>

## 权限是何时引入的

在引入权限之前，**Users can create their own models** 以及帮助助手的 **Enabled** 开关（连同 **Apply limits** 和 **Allow navigating the interface**）都是整个安装实例的设置。Marginalia 在更新时将它们改为权限：如果当时所有人都可以创建模型（这是默认值），那么已存在的每个用户都会获得 **Create own models**；如果助手已启用，则每个用户都会获得 **Help assistant**，并带有相同的两项设置。在你做出更改之前，他们的情况不会有任何变化；从现在起添加的用户则以没有任何权限开始。
