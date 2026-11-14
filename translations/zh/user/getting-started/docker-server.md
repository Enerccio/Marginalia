---
label: 服务器（Docker）
order: 90
---

# 服务器（Docker）

当多人共同使用，或者你希望从其他设备打开 Marginalia 时，请将它作为服务器运行。每个用户都有自己的书籍、设定集、推理提供方和协议。

<a id="requirements"></a>

## 要求

- 安装了 Docker Compose 的 [Docker](https://docs.docker.com/get-docker/)
- Marginalia 约需 1 GB 内存
- 通过互联网访问：一个域名以及带 HTTPS 的反向代理（见[下文](#https-and-reverse-proxy)）

<a id="install"></a>

## 安装

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

第一次运行 `docker compose up` 会从源代码构建镜像，需要几分钟。之后 Marginalia 会在端口 **8080** 上监听。打开 `http://<server>:8080` 并创建管理员账户（见[首次启动](first-start.md)）。

<a id="prebuilt-image"></a>

### 预构建镜像

你也可以不从源代码构建，而是使用每个版本发布时附带的镜像（`linux/amd64` 和 `linux/arm64`）。在 `docker-compose.yml` 中，将 `build:` 代码块替换为 `image:`：

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` 是最新的正式版，`edge` 是开发版，`1.1.0`（或 `1.1`）固定使用某个正式版。文件的其余部分保持不变。

<a id="configuration"></a>

## 配置

所有设置都写在 `docker-compose.yml` 中：

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
| `ports` | `"8080:8080"` 将 Marginalia 发布到主机的 8080 端口。如果只希望同一主机上的反向代理访问它，请使用 `"127.0.0.1:8080:8080"`。 |
| `volumes` | `./data` 保存数据库、备份和扩展。请保留它，并将其纳入服务器备份。首次启动时若它不存在会自动创建；容器启动时会把它的所有者改为 Marginalia 在容器内运行所用的用户（`jetty`），因此其中的文件在主机上归属于该用户的 ID。 |
| `-Xmx1g` | 最大内存。用户较多或书籍非常大时可以调高（例如 `-Xmx2g`）。 |

修改文件后，使用 `docker compose up -d` 使其生效。

<a id="environment-variables"></a>

### 环境变量

Marginalia 自身的设置是服务的环境变量，位于 `JAVA_TOOL_OPTIONS` 旁边：

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 变量 | 默认值 | 作用 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`（镜像中为 `/var/marginalia/.marginalia`） | 数据库、备份、扩展以及用于加密 API 密钥的密钥所在的文件夹。只有在使用对应的卷（`./data:/your/folder`）时才应修改；容器启动时会修正该文件夹的所有者。 |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | 登录页面以及尚未选择语言的用户所使用的语言（`en` 或 `ja`；此处不能使用扩展的语言）。 |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` 会隐藏 *保存登录*。 |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 已保存的登录状态持续的时间，单位为秒（30 天）。 |
| `MARGINALIA_TRUSTED_PROXIES` | 留空 | 被信任的反向代理，其 `X-Forwarded-For` 头会被采信，见[下文](#https-and-reverse-proxy)。替代 `-DtrustedProxies`。 |

同样的设置也可以通过 Java 选项 `-Dmarginalia.default-language=ja` 给出，其优先级高于环境变量。如果某个值不合法（例如秒数不是数字，或语言不存在），Marginalia 将无法启动；原因写在日志中。

<a id="https-and-reverse-proxy"></a>

## HTTPS 与反向代理

!!!danger 不要在互联网上使用明文 HTTP
密码和登录 Cookie 会随每个请求发送。通过明文 HTTP 传输时，任何经过的人都可以读取它们。
请将 Marginalia 置于带 HTTPS 的反向代理之后。
!!!

登录界面上的 *保存登录* 选项同样需要 HTTPS：其 Cookie 被标记为安全（secure），因此浏览器只会在 HTTPS 连接上保留它们。

Marginalia 通过 WebSocket 连接更新页面，因此代理必须转发 WebSocket 升级请求。`marginalia.example.com` 的示例如下：

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

登录失败会按用户和客户端地址分别限流（同一地址失败 30 次后，来自该地址的登录会被拒绝一段逐渐变长的时间，最长 15 分钟）。Marginalia 只信任你指定的代理所发送的 `X-Forwarded-For` 头：如果没有它，所有用户看起来都来自代理的地址，于是任何人失败 30 次都会让所有人被暂时封锁。请在代理配置中保留该头（如上所示），并在 `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>` 的 `environment` 中设置 `docker-compose.yml`。位于 Docker 主机上的代理通过 Docker 网络访问容器，通常为 `172.16.0.0/12`；多个条目以逗号分隔。限流状态保存在内存中，重启 Marginalia 即会清除。

<a id="models-on-the-same-machine"></a>

## 同一台机器上的模型

在容器内部，`localhost` 指的是容器本身，而不是服务器。若要使用运行在 Docker 主机上的模型服务（llama.cpp、Ollama、LM Studio 等），请在[推理提供方](../inference-providers.md)中使用地址 `http://host.docker.internal:<port>/v1`。在 Linux 上，需要在 `docker-compose.yml` 中为服务添加以下内容：

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

模型服务还必须监听容器可以访问的地址，而不能只监听 `127.0.0.1`。

<a id="updating"></a>

## 更新

```sh
git pull
docker compose up -d --build
```

如果使用[预构建镜像](#prebuilt-image)，请改用 `docker compose pull` 和 `docker compose up -d`。

新版本启动时会自动升级数据库。Marginalia 在更改数据库结构之前会保存一份[数据库副本](../administration/database-backups.md#copy-before-an-upgrade)，但你也应先自行做一次[数据库备份](../administration/database-backups.md)。

<a id="automatic-updates"></a>

### 自动更新

仓库根目录下的 `rebuild.sh`（Linux、macOS）和 `rebuild.bat`（Windows）会为你完成更新；可以手动运行，也可以放入 cron 任务或计划任务中。它们会拉取当前检出分支所对应的远程分支（无论是哪个分支），若没有新内容则不做任何事（`--force` 即使如此也会重新构建并重启，例如在修改了 `docker-compose.yml` 之后）。否则它们会 `git pull`，在 Marginalia 继续运行的同时构建[内置扩展](#plugins)和镜像，然后停止容器，将扩展 JAR 复制到 `data/extensions`（替换已安装的版本），并启动新版本。若任何一步失败，脚本会停止并给出提示：如果失败发生在拉取或构建阶段，旧版本仍在运行，下一次运行会重新构建。

`--stop-first` 会在构建前停止容器，适用于内存不足以同时运行应用和构建的服务器；此时 Marginalia 在整个构建期间都不可用。

在 Linux 上，如果已安装更新监视器，管理员只需点击按钮即可更新服务器，无需手动运行脚本，见[更新](../administration/updates.md)。

<a id="plugins"></a>

## 扩展

Docker 镜像不包含[扩展](../extensions/index.md)，而它们必须与应用使用相同的源代码构建。为此你不需要 Java 或 Maven：`marginalia/plugins/docker-compose.yml` 会在容器中构建所有扩展，它会先构建应用，然后构建每个扩展，并将 JAR 复制到 `marginalia/plugins/build`：

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

第一次构建需要几分钟（需要下载应用的依赖）。然后将 JAR 复制到 `data/extensions` 并重启 Marginalia，或在 *管理 → 扩展* 中加载它们（见[扩展](../administration/extensions.md)）。`rebuild.sh` / `rebuild.bat` 会替你完成复制，因此在使用它们的服务器上，扩展总是会与应用一同更新。

<a id="health"></a>

## 健康检查

镜像带有 Docker 健康检查：Marginalia 启动期间（最长两分钟）`docker ps` 显示 `(health: starting)`，之后显示 `(healthy)`。当 `http://localhost:8080/health` 停止以 `200` 响应时，状态变为 `unhealthy`。Docker 不会自行重启不健康的容器；请将该状态用于你的监控，或配合自动修复工具使用。

`/health` 无需登录，因此监控工具或负载均衡器也可以使用它（通过代理访问：`https://marginalia.example.com/health`）。当 Marginalia 正在运行且其数据库可以响应时，它返回 `200` 和 `{"status":"UP"}`；在启动中或出现问题时，它返回带 `{"status":"DOWN",...}` 的 `503`。它不显示其他任何内容（没有版本，没有细节）；原因见日志。

<a id="logs"></a>

## 日志

```sh
docker compose logs -f marginalia
```
