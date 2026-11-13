---
label: サーバー（Docker）
order: 90
---

# サーバー（Docker）

複数の人が利用する場合や、他のデバイスからアクセスしたい場合は、Marginalia をサーバーとして実行してください。ユーザーごとに、それぞれ専用の本、ロアブック、推論プロバイダー、プロトコルが用意されます。

<a id="requirements"></a>

## 必要なもの

- [Docker](https://docs.docker.com/get-docker/) と Docker Compose
- Marginalia 用に約 1 GB のメモリ
- インターネット経由でアクセスする場合：ドメインと、HTTPS に対応したリバースプロキシ（[下記](#https-and-reverse-proxy)を参照）

<a id="install"></a>

## インストール

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

最初の `docker compose up` では、ソースからイメージをビルドするため、数分かかります。その後、Marginalia はポート **8080** で待ち受けます。`http://<server>:8080` を開き、管理者アカウントを作成してください（[初回起動](first-start.md)を参照）。

<a id="prebuilt-image"></a>

### ビルド済みイメージ

ソースからビルドする代わりに、各リリースで公開されているイメージ（`linux/amd64` および `linux/arm64`）を使用できます。`docker-compose.yml` で `build:` ブロックを `image:` に置き換えてください：

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` は最新リリース、`edge` は開発版、`1.1.0`（または `1.1`）はリリースを固定します。その他のファイルの内容は変わりません。

<a id="configuration"></a>

## 設定

すべての設定は `docker-compose.yml` で行います：

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

| 設定 | 役割 |
|---|---|
| `ports` | `"8080:8080"` は、ホストのポート 8080 で Marginalia を公開します。同じホスト上のリバースプロキシからのみアクセスさせたい場合は `"127.0.0.1:8080:8080"` を使用してください。 |
| `volumes` | `./data` には、データベース、バックアップ、拡張機能が保存されます。削除せず、サーバーのバックアップにも含めてください。最初の起動時に存在しなければ作成されます。起動時にコンテナは、Marginalia がコンテナ内で実行されるユーザー（`jetty`）がこのフォルダを所有するように設定するため、フォルダ内のファイルはホスト上でそのユーザーの ID が所有者になります。 |
| `-Xmx1g` | 最大メモリ量です。多数のユーザーを扱う場合や非常に大きな本を扱う場合は、増やしてください（例：`-Xmx2g`）。 |

ファイルを変更したら、`docker compose up -d` で反映させてください。

<a id="environment-variables"></a>

### 環境変数

Marginalia 独自の設定は、`JAVA_TOOL_OPTIONS` と並んでサービスの環境変数として指定します：

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 変数 | 既定値 | 役割 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`（イメージ内では `/var/marginalia/.marginalia`） | データベース、バックアップ、拡張機能、そして API キーを暗号化する鍵が置かれるフォルダです。変更する場合は、対応するボリューム（`./data:/your/folder`）も合わせてください。コンテナは起動時にこのフォルダの所有者を設定し直します。 |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | ログインページと、言語を選択していないユーザーの言語です（`en` または `ja`。拡張機能の言語はここでは使えません）。 |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` にすると、*ログイン情報を保存* が表示されなくなります。 |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 保存されたログインの有効期間を秒単位で指定します（30 日）。 |
| `MARGINALIA_TRUSTED_PROXIES` | 空 | `X-Forwarded-For` ヘッダーを信頼するリバースプロキシを指定します。[下記](#https-and-reverse-proxy)を参照してください。`-DtrustedProxies` の代わりに使用します。 |

同じ設定は Java オプション `-Dmarginalia.default-language=ja` としても指定でき、その場合は変数より優先されます。値が不正な場合（秒数に数字以外の文字が含まれる、存在しない言語を指定したなど）、Marginalia は起動しません。理由はログに記録されます。

<a id="https-and-reverse-proxy"></a>

## HTTPS とリバースプロキシ

!!!danger インターネット経由で平文の HTTP を使わないでください
パスワードとログインクッキーは、すべてのリクエストで送信されます。平文の HTTP では、経路上の誰でもこれらを読み取ることができます。
Marginalia は、必ず HTTPS に対応したリバースプロキシの背後に置いてください。
!!!

ログイン画面の *ログイン情報を保存* も HTTPS を必要とします。そのクッキーは secure 属性が付与されているため、ブラウザは HTTPS 接続でのみ保持します。

Marginalia はページの更新に WebSocket 接続を使用するため、プロキシは WebSocket のアップグレードを転送する必要があります。`marginalia.example.com` の設定例は次のとおりです：

+++ Caddy
Caddy は証明書を取得し、WebSocket を自動的に転送します。

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

    client_max_body_size 100m;   # バックアップと拡張機能のアップロード

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # 長時間の生成中も接続を維持します
    }
}
```
+++

ログインの失敗は、ユーザーごと、およびクライアントのアドレスごとに制限されます（1 つのアドレスから 30 回失敗すると、そのアドレスからのログインは、徐々に長くなる時間（最長 15 分）の間拒否されます）。Marginalia は、指定したプロキシの `X-Forwarded-For` ヘッダーのみを信頼します。このヘッダーがないと、すべてのユーザーがプロキシのアドレスから来たように見え、誰か一人の 30 回の失敗で全員がしばらくブロックされてしまいます。プロキシの設定に上記のとおりこのヘッダーを残し、`docker-compose.yml` の `environment` に `MARGINALIA_TRUSTED_PROXIES=<プロキシのアドレスまたは範囲>` を設定してください。Docker ホスト上のプロキシは、通常 `172.16.0.0/12` である Docker ネットワーク経由でコンテナに接続します。複数の値はカンマで区切ります。制限の状態はメモリ上に保持されるため、Marginalia を再起動するとクリアされます。

<a id="models-on-the-same-machine"></a>

## 同じマシン上のモデル

コンテナ内では、`localhost` はサーバーではなくコンテナ自身を指します。Docker ホスト上で動作するモデルサーバー（llama.cpp、Ollama、LM Studio など）を使用するには、[推論プロバイダー](../inference-providers.md)でアドレス `http://host.docker.internal:<port>/v1` を指定してください。Linux では、`docker-compose.yml` のサービスに次の設定を追加します：

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

モデルサーバーは、`127.0.0.1` だけでなく、コンテナから到達できるアドレスでも待ち受ける必要があります。

<a id="updating"></a>

## 更新

```sh
git pull
docker compose up -d --build
```

[ビルド済みイメージ](#prebuilt-image)を使用している場合は、代わりに `docker compose pull` と `docker compose up -d` を実行してください。

データベースは、新しいバージョンの起動時に自動的にアップグレードされます。Marginalia は、構造を変更する前に[データベースのコピー](../administration/database-backups.md#copy-before-an-upgrade)を保存しますが、念のため事前に自分で[データベースのバックアップ](../administration/database-backups.md)も取得してください。

<a id="automatic-updates"></a>

### 自動更新

リポジトリのルートにある `rebuild.sh`（Linux、macOS）と `rebuild.bat`（Windows）が、更新を自動的に行います。手動で実行するか、cron ジョブやタスクスケジューラーから実行してください。これらは、チェックアウトされているブランチのリモートを取得し、新しい変更がない場合は何もしません（`--force` を付けると、例えば `docker-compose.yml` を変更した後などに、強制的に再ビルドして再起動します）。新しい変更がある場合は、`git pull` を行い、[同梱のプラグイン](#plugins)とイメージを **Marginalia が稼働したまま** ビルドします。その後、コンテナを停止し、プラグインの JAR を `data/extensions` にコピー（インストール済みのものを置き換え）してから、新しいバージョンを起動します。いずれかの手順が失敗すると、スクリプトはメッセージを表示して停止します。失敗が pull またはビルドの段階であった場合は、古いバージョンが引き続き稼働しており、次回の実行で再度ビルドされます。

`--stop-first` を指定すると、ビルドの前にコンテナを停止します。アプリケーションとビルドを同時に実行できるだけのメモリがないサーバー向けです。この場合、ビルドの間ずっと Marginalia は停止します。

Linux では、更新監視機能をインストールしておけば、スクリプトを手動で実行する必要はまったくありません。管理者はボタンを使ってサーバーを更新できます。[更新](../administration/updates.md)を参照してください。

<a id="plugins"></a>

## プラグイン

Docker イメージには[拡張機能](../extensions/index.md)は含まれておらず、アプリケーションと同じソースからビルドする必要があります。そのために Java や Maven は必要ありません。`marginalia/plugins/docker-compose.yml` が、コンテナ内で全てをビルドします。まずアプリケーションをビルドし、次にすべてのプラグインをビルドして、JAR を `marginalia/plugins/build` にコピーします：

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

初回のビルドには数分かかります（アプリケーションの依存関係をダウンロードするため）。その後、JAR を `data/extensions` にコピーして Marginalia を再起動するか、*Admin → Extensions* から読み込んでください（[拡張機能](../administration/extensions.md)を参照）。`rebuild.sh` / `rebuild.bat` はこのコピーを自動で行うため、これらを使用しているサーバーでは、プラグインは常にアプリケーションと一緒に更新されます。

<a id="health"></a>

## 健全性

イメージには Docker のヘルスチェックが組み込まれています。Marginalia の起動中（最大 2 分間）は `docker ps` に `(health: starting)` と表示され、その後 `(healthy)` になります。`http://localhost:8080/health` が `200` で応答しなくなると、`unhealthy` になります。Docker は、unhealthy になったコンテナを自動的に再起動しません。この状態を監視ツールや自動復旧ツールで利用してください。

`/health` はログインを必要としないため、監視ツールやロードバランサーからも利用できます（プロキシ経由の場合：`https://marginalia.example.com/health`）。Marginalia が稼働し、データベースが応答している場合は `200` と `{"status":"UP"}` を返します。起動中や何らかの問題がある場合は、`503` と `{"status":"DOWN",...}` を返します。それ以外の情報（バージョンや詳細）は表示されません。理由はログで確認してください。

<a id="logs"></a>

## ログ

```sh
docker compose logs -f marginalia
```
