---
label: 权限
order: 95
---

# 权限

权限允许某个用户做一些并非所有人都能做的事。管理员逐个用户授予权限；
没有任何权限的用户仍可照常在 Marginalia 中做其他所有事情（写书、建立设定集、使用管理员[共享](../inference-providers.md#shared-providers)的模型）。

| 权限 | 允许做什么 | 设置 |
|---|---|---|
| **Create own models** | 添加[推理提供方](../inference-providers.md)：没有此权限时，用户只能使用管理员共享的提供方；已有的提供方仍可继续工作，也可以编辑。 | |
| **Help assistant** | 使用[帮助助手](help-assistant.md)，即根据手册回答问题的聊天功能。 | **Apply limits** 和 **Allow navigating the interface**，参见[权限的设置](#settings-of-a-permission)。 |
| **Agents** | （实验性功能，参见[智能体](../agents.md)）[Agents](../agents.md) 标签页：创建智能体、克隆智能体，并在一本书上运行它们。 | **Most agents**、**Concurrent runs**、**Requests to the model at once**、**Token budget of a run**，以及副本和文件的配额，参见[权限的设置](#settings-of-a-permission)。 |
| **Version history** | 用户的书籍历史为每个部分保留的最多[版本](../books/version-history.md#how-many-versions-are-kept)数。每位用户默认为 50（没有此行的用户也是如此）；未授予此权限则该用户没有历史记录。 | **Versions kept per part**，这是一个上限，文件夹、书籍和用户都无法超过它。留空表示此处不设限制。新添加的权限从 50 开始。 |
| **API access** | [HTTP API](../account.md#api-keys) 的密钥：以用户身份在其书籍中操作的程序。收回此权限会使该用户的密钥失效。管理员无需此权限，并且只有管理员才能让密钥访问更新或扩展功能。 | |
| **Notifications** | 当智能体需要用户而没有打开任何界面时，服务器发送到某个 URL 的[消息](../agents.md#when-you-are-not-there)。服务器会调用用户输入的地址，因此这是一项独立的权限：没有它，用户的设置中就不会出现这些字段，也不会发送任何内容。 | **Allow addresses on this machine and in private networks**，参见[权限的设置](#settings-of-a-permission)。 |

管理员始终拥有所有权限，因此无需授予，也没有可授予的对象（管理员的按钮和界面部分从不依赖权限）。新用户没有任何权限：在你[添加用户](users.md#adding-a-user)后，应立即授予他们所需的权限。

<a id="giving-and-taking-permissions"></a>

## 授予和收回权限

**Admin → Users** 中，用户所在行的钥匙按钮会打开该用户的权限窗口：

![用户的权限](../../images/permissions-dialog.png)

该窗口是用户拥有的权限表格，包含三列：

| 列 | |
|---|---|
| **Name** | 权限名称。带有设置的权限，其名称前有一个箭头，参见[权限的设置](#settings-of-a-permission)。 |
| **Granted** | 权限是否生效。未授予的权限等同于没有权限，但其设置会被保留，因此你可以暂时关闭再重新开启。 |
| 垃圾桶按钮 | 永久删除该权限及其设置。 |

要授予权限，点击表格上方的 **Add permission**，然后选择一项该用户尚未拥有的权限；它会被添加到表格中，并处于已授予状态。在你点击 **Save** 之前，任何内容都不会被保存：

![选择要添加的权限](../../images/permissions-add.png)

- 没有需要保存的内容时，**Save** 为禁用状态。
- **Exit** 会关闭窗口；如果有未保存的更改，它会先询问，以免不小心丢弃更改。
- 你删除的权限会保留在表格中，直到你保存；保存后才会被永久删除。在此之前，关闭窗口时点击 **Exit** 且不保存，即可恢复该权限。

保存后，如果该用户已登录，其页面会重新加载，因此依赖权限的按钮和界面部分会立即出现或消失。你自己的页面不会重新加载，即使你修改的是自己的权限。

<a id="settings-of-a-permission"></a>

## 权限的设置

有些权限的设置属于用户本人，而不是整个安装实例。这类权限所在的行以箭头开头：点击它可展开设置（打开窗口时设置处于折叠状态，刚添加的权限则为展开状态），再次点击即可折叠。折叠只是为了让较长的列表保持简洁；它不会改变任何内容，也不算作未保存的更改。

![帮助助手权限的设置](../../images/permissions-settings.png)

**Help assistant**、**Agents** 和 **Notifications** 都有设置。帮助助手的设置如下：

| 设置 | |
|---|---|
| **Apply limits** | 对于共享模型，用户的提问是否与其他请求一样计入其限额和用量，参见[谁付费](help-assistant.md#who-pays)。默认关闭。 |
| **Allow navigating the interface** | 当用户要求助手指示操作方法时，允许它打开某个部分、选择某个标签页，或打开用于添加内容的对话框，参见[指引方式](help-assistant.md#showing-the-way)。默认关闭。 |

智能体的设置如下；留空即为默认值：

| 设置 | |
|---|---|
| **Most agents** | 用户最多可拥有多少个智能体，包括克隆出的智能体。留空时为五个。设为零时，用户可以保留并运行已有的智能体，但不能创建或克隆新的智能体。删除一个智能体会释放其名额。 |
| **Concurrent runs** | 用户可同时进行多少次运行。留空时为一次。 |
| **Requests to the model at once (per run)** | 一次运行中，智能体及其[子智能体](../agents.md#sub-agents)可同时发出多少个模型请求。留空时为一个：子智能体向模型提问时，智能体需等待，反之亦然。设置更大的值可让子智能体与智能体并行工作（智能体所用模型可能还有自己的[请求上限](../inference-providers.md#general-settings)，同样适用）。 |
| **Token budget of a run** | 用户的一次运行最多可消耗多少 token（发送和生成的总和）。留空或设为零表示不设限制。 |
| **Working copies** | 用户的运行同时保留多少份书籍工作副本。未完成的运行会保留其副本，暂停的运行也是如此；运行完成、停止或删除后，副本会被释放。留空或设为零时，除智能体数量外不设其他限制。 |
| **Largest file (KB)** | 用户的智能体可写入其工作区的最大文件。留空时为 1024（1 MB）。 |
| **Workspace size (MB)** | 用户的一个智能体的工作区中所有文件的总大小上限。留空时为 20。 |
| **Files in a workspace** | 用户的一个智能体的工作区最多可容纳多少个文件（文件夹不计入）。留空时为 500。 |
| **Checkpoints of a run (MB)** | 一次运行的所有[检查点](../agents.md#checkpoints)（工作副本及其工作区的还原点）总共可占用多少空间；其中一部分保留给自动检查点。留空时为 100。 |

![智能体权限的设置](../../images/permissions-settings-agents.png)

通知的设置如下：

| 设置 | |
|---|---|
| **Allow addresses on this machine and in private networks** | 一项子权限：只有在已授予 **Notifications** 时才能勾选该选项（否则该选项为灰色不可用，收回 **Notifications** 权限也会清除此选项）。未勾选时，只会调用互联网上的地址：不包括 `localhost`、私有网络（`10.x.x.x`、`172.16-31.x.x`、`192.168.x.x`）、链路本地或共享地址（`169.254.x.x`，云主机的元数据服务位于此处，即 `100.64-127.x.x`），也不包括 IPv6 的唯一本地地址。如果某用户的 ntfy 或 Gotify 运行在同一台机器上或你自己的网络中，则为该用户勾选此项。默认关闭。域名会按其解析到的地址进行检查，因此指向私有地址的域名同样会被拒绝，无法解析的域名也会被拒绝。 |

管理员既不需要该权限，也不需要该子权限：他们可以接收消息，并且可以使用本地地址。在与你并不完全信任的人共享的服务器上，请保持此子权限关闭：否则服务器会向用户输入的任何地址发出请求，而这些请求会发生在你的网络内部。

<a id="when-permissions-were-introduced"></a>

## 权限是何时引入的

在引入权限之前，**Users can create their own models** 以及帮助助手的 **Enabled** 开关（连同 **Apply limits** 和 **Allow navigating the interface**）是整个安装实例的设置。Marginalia 在更新时将它们改为权限：如果当时每个人都可以创建模型（这是默认值），那么所有已存在的用户都会获得 **Create own models**；如果助手已启用，则获得 **Help assistant** 及相同的两项设置。在你做出更改之前，他们的权限不会有任何变化；从现在起你添加的用户则从没有任何权限开始。
