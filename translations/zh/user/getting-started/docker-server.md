---
label: 服务器（Docker）
order: 90
---

# 服务器（Docker）

当多人共同使用，或者你希望从其他设备访问时，请将 Marginalia 作为服务器运行。每个用户都有自己的书籍、设定集（lorebook）、推理服务提供方和协议。

<a id="requirements"></a>

## 要求

- [Docker](https://docs.docker.com/get-docker/) 以及 Docker Compose
- Marginalia 约需 1 GB 内存
- 若要通过互联网访问：一个域名，以及一个带 HTTPS 的反向代理（见[下文](#https-and-reverse-proxy)）

<a id="install"></a>

## 安装

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

第一次执行 `docker compose up` 时会从源代码构建镜像，需要几分钟。之后 Marginalia 会在 **8080** 端口监听。打开 `http://<server>:8080` 并创建管理员账户（见[首次启动](first-start.md)）。

<a id="prebuilt-image"></a>

### 预构建镜像

除了从源代码构建，你也可以使用每个版本随附发布的镜像（`linux/amd64` 和 `linux/arm64`）。在 `docker-compose.yml` 中，将 `build:` 代码块替换为 `image:`：

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` 是最新的正式版本，`edge` 是开发版本，`1.1.0`（或 `1.1`）则固定使用某一个版本。文件的其余部分保持不变。

<a id="configuration"></a>

## 配置

所有设置都在 `docker-compose.yml` 中完成：

```yaml
services:
  marginalia:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: marginalia-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/marginalia/.marginalia
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| 设置 | 作用 |
|---|---|
| `ports` | `"8080:8080"` 将 Marginalia 发布到宿主机的 8080 端口。若只希望同一主机上的反向代理访问它，请改用 `"127.0.0.1:8080:8080"`。 |
| `volumes` | `./data` 保存数据库、备份和扩展。请保留它，并将其纳入服务器备份。若该目录不存在，会在首次启动时创建；容器启动时会把它的所有者改为 Marginalia 在容器内运行的用户（`jetty`），因此其中的文件在宿主机上归属于该用户的 ID。 |
| `-Xmx1g` | 最大内存。用户较多或书籍非常大时可以调高（例如 `-Xmx2g`）。 |

修改文件后，使用 `docker compose up -d` 使其生效。

<a id="environment-variables"></a>

### 环境变量

Marginalia 自身的设置是服务的环境变量，写在 `JAVA_TOOL_OPTIONS` 旁边：

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 变量 | 默认值 | 作用 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`（镜像中为 `/var/marginalia/.marginalia`） | 数据库、备份、扩展以及加密 API 密钥的密钥所在的文件夹。只有在使用对应的卷（`./data:/your/folder`）时才应修改；容器启动时会修正该文件夹的所有者。 |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | 登录页面以及未选择语言的用户所使用的语言（`en` 或 `ja`；此处不能使用扩展的语言）。 |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` 会隐藏 *Save login*。 |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 保存的登录有效期，单位为秒（30 天）。 |
| `MARGINALIA_TRUSTED_PROXIES` | 空 | 其 `X-Forwarded-For` 头部被信任的反向代理，见[下文](#https-and-reverse-proxy)。替代 `-DtrustedProxies`。 |
| `MARGINALIA_COOKIE_NAMESPACE` | 空 | 加在 *Save login* cookie 名称前的前缀，见[同一主机上的多个应用](#several-applications-on-one-host)。设置后会让所有保存了登录的用户退出登录。 |
| `MARGINALIA_SESSION_COOKIE` | `JSESSIONID_MARGINALIA`（镜像中；镜像外为 `JSESSIONID`） | 会话 cookie 的名称，见[同一主机上的多个应用](#several-applications-on-one-host)。 |

同样的设置也可以写成 Java 选项，即 `-Dmarginalia.default-language=ja`，其优先级高于环境变量。无法接受的值（例如不是数字的秒数、未知的语言）会导致 Marginalia 无法启动；原因写在日志中。

<a id="https-and-reverse-proxy"></a>

## HTTPS 与反向代理

!!!danger 不要在互联网上使用明文 HTTP
密码和登录 cookie 会随每个请求发送。通过明文 HTTP，途经的任何人都可以读取它们。
请将 Marginalia 置于带 HTTPS 的反向代理之后。
!!!

登录页面上的 *Save login* 选项同样需要 HTTPS：它的 cookie 被标记为 secure，因此浏览器只会在 HTTPS 连接上保留它们。

Marginalia 通过 WebSocket 连接更新页面，因此代理必须转发 WebSocket 升级请求。以下是 `marginalia.example.com` 的示例：

+++ Caddy
Caddy 会自动获取证书并转发 WebSocket。

```
marginalia.example.com {
    reverse_proxy localhost:8080
}
```
+++ nginx
```nginx
server {
    listen 443 ssl;
    server_name marginalia.example.com;

    ssl_certificate     /etc/letsencrypt/live/marginalia.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/marginalia.example.com/privkey.pem;

    client_max_body_size 100m;   # uploads of backups and extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # long generations keep the connection open
    }
}
```
+++

登录失败会按用户和客户端地址进行限流：同一地址失败 30 次后，来自该地址的登录会被拒绝一段逐渐延长的时间，最长 15 分钟。Marginalia 只信任你指定的代理所发送的 `X-Forwarded-For` 头部。若不这样做，所有用户看起来都来自代理的地址，任何人的 30 次失败登录都会让所有人在一段时间内被封锁。请如上所示在代理配置中保留该头部，并在 `docker-compose.yml` 的 `environment` 中设置 `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`。位于 Docker 宿主机上的代理通过 Docker 网络访问容器，通常为 `172.16.0.0/12`；多个条目用逗号分隔。限流状态保存在内存中，重启 Marginalia 即可清除。

<a id="several-applications-on-one-host"></a>

### 同一主机上的多个应用

浏览器按主机名保存 cookie，**而不是按端口**：在 `localhost:8080` 上运行的 Marginalia 与 `localhost:8090` 上的另一个程序（或同一域名后经由同一代理提供的两个程序）会共享 `localhost` 的 cookie。当另一个程序也是 Java Web 应用时（基于 Marginalia 平台构建的其他应用，或第二个 Marginalia），两者都使用会话 cookie `JSESSIONID`，访问其中一个会让你从另一个退出登录。因此，镜像会把它的会话 cookie 命名为 `JSESSIONID_MARGINALIA`。当你在同一主机上运行两个 Marginalia 容器时，请为第二个容器设置其他名称：分别设置 `MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA2` 和 `MARGINALIA_COOKIE_NAMESPACE=second`（即 *Save login* 的 cookie）。为每个应用使用单独的主机名（`marginalia.example.com`、`other.example.com`）也能避免此问题，且无需任何设置。

<a id="models-on-the-same-machine"></a>

## 同一台机器上的模型

在容器内部，`localhost` 指的是容器本身，而不是服务器。若要使用运行在 Docker 宿主机上的模型服务器（llama.cpp、Ollama、LM Studio 等），请在[推理服务提供方](../inference-providers.md)中使用地址 `http://host.docker.internal:<port>/v1`。在 Linux 上，请把以下内容加入 `docker-compose.yml` 中的服务：

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

模型服务器也必须监听容器可以访问的地址，而不能只监听 `127.0.0.1`。

<a id="updating"></a>

## 更新

```sh
git pull
docker compose up -d --build
```

若使用[预构建镜像](#prebuilt-image)，则改用 `docker compose pull` 和 `docker compose up -d`。

数据库会在新版本启动时自动升级。Marginalia 在修改结构之前会保存一份[数据库副本](../administration/database-backups.md#copy-before-an-upgrade)，但你也应先自行做一次[数据库备份](../administration/database-backups.md)。

<a id="automatic-updates"></a>

### 自动更新

仓库根目录中的 `rebuild.sh`（Linux、macOS）和 `rebuild.bat`（Windows）会替你完成更新。你可以手动运行它们，也可以放入 cron 任务或计划任务中。它们会获取当前检出分支（无论是哪个分支）的远程更新；若没有新内容，则不做任何操作（`--force` 则总会重新构建并重启，例如在修改了 `docker-compose.yml` 之后）。否则，它们会 `git pull`，并在 Marginalia 保持运行的同时构建[内置插件](#plugins)和镜像，然后停止容器，把插件 JAR 复制到 `data/extensions`（替换已安装的版本），再启动新版本。若任何一步失败，脚本会停止并给出提示；如果失败的是拉取或构建步骤，旧版本仍在运行，下次运行时会重新构建。

`--stop-first` 会在构建前停止容器，适用于内存不足以同时运行应用和构建的服务器；此时 Marginalia 在整个构建期间都将不可用。

在 Linux 上，甚至无需手动运行这些脚本：安装了更新监视器后，管理员可以通过一个按钮更新服务器，见[更新](../administration/updates.md)。

<a id="plugins"></a>

## 插件

Docker 镜像不包含[扩展](../extensions/index.md)，而扩展必须与应用从相同的源代码构建。为此你不需要 Java 或 Maven：`marginalia/plugins/docker-compose.yml` 会在容器中构建所有扩展，它先构建应用，再构建每个插件，并把 JAR 复制到 `marginalia/plugins/build`：

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

第一次构建需要几分钟（需要下载应用的依赖）。之后将 JAR 复制到 `data/extensions` 并重启 Marginalia，或在 *Admin → Extensions* 中加载它们（见[扩展](../administration/extensions.md)）。`rebuild.sh` / `rebuild.bat` 会替你完成复制，因此在使用它们的服务器上，插件总会与应用一同更新。
<a id="health"></a>

## 健康检查

镜像内置了 Docker 健康检查：Marginalia 启动期间（最多两分钟）`docker ps` 的状态为 `(health: starting)`，之后变为 `(healthy)`。当 `http://localhost:8080/health` 停止以 `200` 响应时，状态变为 `unhealthy`。Docker 不会自动重启不健康的容器；请将该状态用于监控，或配合自动修复工具使用。

`/health` 不需要登录，因此监控工具或负载均衡器也可以使用它（通过代理：`https://marginalia.example.com/health`）。当 Marginalia 正在运行且其数据库可以响应时，它返回 `200` 和 `{"status":"UP"}`；在启动期间或出现问题时，它返回 `503` 及 `{"status":"DOWN",...}`。除此之外不显示任何其他内容（没有版本号，没有详细信息）；原因记录在日志中。

<a id="logs"></a>

## 日志

```sh
docker compose logs -f marginalia
```
