---
label: サーバー（Docker）
order: 90
---

# サーバー（Docker）

複数の人が使う場合や、他の端末から開きたい場合は、Marginaliaをサーバーとして実行してください。利用者ごとに、それぞれ独自の本、ロアブック、推論プロバイダーとプロトコルを持てます。

<a id="requirements"></a>

## 要件

- [Docker](https://docs.docker.com/get-docker/)（Docker Composeを含む）
- Marginalia用に約1 GBのメモリ
- インターネット経由でアクセスする場合: ドメインと、HTTPSに対応したリバースプロキシ（[下記](#https-and-reverse-proxy)を参照）

<a id="install"></a>

## インストール

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

最初の`docker compose up`ではソースからイメージをビルドするため、数分かかります。その後、Marginaliaはポート**8080**で待ち受けます。`http://<server>:8080`を開き、管理者アカウントを作成してください（[初回起動](first-start.md)を参照）。

<a id="prebuilt-image"></a>

### ビルド済みイメージ

ソースからビルドする代わりに、各リリースで公開されているイメージを使うこともできます（`linux/amd64`と`linux/arm64`）。
`docker-compose.yml`で、`build:`ブロックを`image:`に置き換えてください:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest`は最新のリリース、`edge`は開発版、`1.1.0`（または`1.1`）は特定のリリースに固定します。ファイルのそれ以外の部分は変更しません。

<a id="configuration"></a>

## 設定

すべての設定は`docker-compose.yml`で行います:

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
| `ports` | `"8080:8080"`はMarginaliaをホストのポート8080で公開します。同じホスト上のリバースプロキシだけがアクセスする場合は、`"127.0.0.1:8080:8080"`を使ってください。 |
| `volumes` | `./data`にデータベース、バックアップ、拡張機能が保存されます。残しておき、サーバーのバックアップに含めてください。存在しない場合は最初の起動時に作成されます。起動時にコンテナは、このフォルダの所有者をコンテナ内でMarginaliaが実行されるユーザー（`jetty`）に変更するため、フォルダ内のファイルはホスト上でそのユーザーのIDに属します。 |
| `-Xmx1g` | 最大メモリ。多くの利用者がいる場合や、非常に大きな本を扱う場合は（例えば`-Xmx2g`に）増やしてください。 |

ファイルを変更したら、`docker compose up -d`で適用してください。

<a id="environment-variables"></a>

### 環境変数

Marginaliaの独自の設定は、`JAVA_TOOL_OPTIONS`と並んでサービスの環境変数として指定します:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 変数 | 既定値 | 役割 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`（イメージ内では`/var/marginalia/.marginalia`） | データベース、バックアップ、拡張機能、APIキーを暗号化する鍵のフォルダ。対応するボリューム（`./data:/your/folder`）がある場合のみ変更してください。コンテナは起動時にこのフォルダの所有者を修正します。 |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | ログインページと、言語を選んでいない利用者に使われる言語（`en`または`ja`。拡張機能の言語はここでは使えません）。 |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false`にすると*ログイン情報を保存*が非表示になります。 |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 保存されたログイン情報の有効期間（秒単位、30日）。 |
| `MARGINALIA_TRUSTED_PROXIES` | 空 | `X-Forwarded-For`ヘッダーを信頼するリバースプロキシ。[下記](#https-and-reverse-proxy)を参照。`-DtrustedProxies`を置き換えます。 |

同じ設定は Java オプションの`-Dmarginalia.default-language=ja`としても指定でき、これらは変数より優先されます。値が不正な場合（秒数に文字列を指定した場合や、未知の言語など）、Marginaliaは起動しません。理由はログに記録されます。

<a id="https-and-reverse-proxy"></a>

## HTTPSとリバースプロキシ

!!!danger インターネット上では平文のHTTPを使わないでください
パスワードとログインCookieは、すべてのリクエストで送信されます。平文のHTTPでは、経路上の誰でもこれらを読み取れてしまいます。
Marginaliaの前段にHTTPS対応のリバースプロキシを置いてください。
!!!

ログイン画面の*ログイン情報を保存*オプションにもHTTPSが必要です。このCookieはsecure属性が付けられているため、ブラウザはHTTPS接続でのみ保持します。

Marginaliaはページの更新にWebSocket接続を使うため、プロキシはWebSocketのアップグレードを転送する必要があります。`marginalia.example.com`の例は以下のとおりです。

+++ Caddy
Caddyは証明書を取得し、WebSocketを自動的に転送します。

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

ログインの失敗は、利用者ごと、およびクライアントアドレスごとに制限されます（1つのアドレスから30回失敗すると、そのアドレスからのログインは、最長15分まで次第に長くなる時間の間拒否されます）。Marginaliaは、指定したプロキシの`X-Forwarded-For`ヘッダーだけを信頼します。これがないと、すべての利用者がプロキシのアドレスから来たように見え、誰かの30回の失敗によって全員が一定時間ブロックされてしまいます。ヘッダーは（上記のように）プロキシの設定に残し、`MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`を`environment`の`docker-compose.yml`に設定してください。Dockerホスト上のプロキシは、Dockerネットワークからコンテナに接続します。通常は`172.16.0.0/12`です。複数のエントリはカンマで区切ります。この制限の状態はメモリ上に保持されるため、Marginaliaを再起動するとリセットされます。

<a id="models-on-the-same-machine"></a>

## 同じマシン上のモデル

コンテナの内部では、`localhost`はサーバーではなくコンテナ自身を指します。Dockerホスト上で動作するモデルサーバー（llama.cpp、Ollama、LM Studioなど）を使うには、[推論プロバイダー](../inference-providers.md)でアドレス`http://host.docker.internal:<port>/v1`を指定してください。Linuxでは、`docker-compose.yml`のサービスに次を追加します:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

モデルサーバーも、`127.0.0.1`だけでなく、コンテナから到達できるアドレスでも待ち受ける必要があります。

<a id="updating"></a>

## 更新

```sh
git pull
docker compose up -d --build
```

[ビルド済みイメージ](#prebuilt-image)を使う場合は、代わりに`docker compose pull`と`docker compose up -d`を使ってください。

新しいバージョンが起動すると、データベースは自動的にアップグレードされます。Marginaliaは構造を変更する前に[データベースのコピー](../administration/database-backups.md#copy-before-an-upgrade)を保存しますが、先に自分で[データベースのバックアップ](../administration/database-backups.md)も取っておいてください。

<a id="automatic-updates"></a>

### 自動更新

リポジトリのルートにある`rebuild.sh`（Linux、macOS）と`rebuild.bat`（Windows）は、更新を自動で行います。手動で実行するか、cronジョブやタスクスケジューラから実行してください。チェックアウトしているブランチ（どのブランチであっても）のリモートを取得し、新しいものがなければ何もしません（`--force`は、例えば`docker-compose.yml`を変更した後などに、いずれにせよ再ビルドして再起動します）。新しいものがあれば、`git pull`を行い、Marginaliaが動作し続けている**間に**、[同梱のプラグイン](#plugins)とイメージをビルドします。その後コンテナを停止し、プラグインのJARを`data/extensions`にコピーし（インストール済みのものを置き換えます）、新しいバージョンを起動します。いずれかの手順が失敗すると、スクリプトはメッセージを表示して停止します。失敗したのが取得またはビルドの場合は、古いバージョンがまだ動作しており、次の実行で再度ビルドされます。

`--stop-first`は、アプリケーションとビルドを同時に行うだけのメモリがないサーバー向けに、ビルドの前にコンテナを停止します。この場合、ビルドの間ずっとMarginaliaは停止します。

Linuxでは、更新ウォッチャーをインストールしておけば、スクリプトを手動で実行する必要すらありません。管理者はボタンでサーバーを更新できます。[更新](../administration/updates.md)を参照してください。

<a id="plugins"></a>

## プラグイン

Dockerイメージには[拡張機能](../extensions/index.md)は含まれておらず、アプリケーションと同じソースからビルドする必要があります。そのためにJavaやMavenは必要ありません。`marginalia/plugins/docker-compose.yml`は、すべてのプラグインを、まずアプリケーションをビルドしてから、コンテナ内でまとめてビルドし、JARを`marginalia/plugins/build`にコピーします:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

最初のビルドには数分かかります（アプリケーションの依存関係をダウンロードするため）。その後、JARを`data/extensions`にコピーしてMarginaliaを再起動するか、*管理 → 拡張機能*で読み込んでください（[拡張機能](../administration/extensions.md)を参照）。`rebuild.sh`／`rebuild.bat`がコピーを代わりに行うため、これらを使用しているサーバーでは、プラグインは常にアプリケーションと一緒に更新されます。

<a id="health"></a>

## ヘルスチェック

イメージにはDockerのヘルスチェックが含まれています。Marginaliaの起動中（最長2分間）は`docker ps`が`(health: starting)`と表示され、その後は`(healthy)`になります。`http://localhost:8080/health`が`200`で応答しなくなると`unhealthy`になります。Dockerは不健全なコンテナを自動で再起動しません。監視にはこの状態を利用するか、自動修復ツールを使ってください。

`/health`はログインを必要としないため、監視ツールやロードバランサーからも利用できます（プロキシ経由の場合: `https://marginalia.example.com/health`）。Marginaliaが動作し、データベースも応答している場合は`200`と`{"status":"UP"}`を返します。起動中または何らかの問題がある場合は、`{"status":"DOWN",...}`とともに`503`を返します。それ以外の情報（バージョンや詳細など）は表示しません。理由はログに記録されています。

<a id="logs"></a>

## ログ

```sh
docker compose logs -f marginalia
```
