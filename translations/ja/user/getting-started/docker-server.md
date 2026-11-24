---
label: サーバー (Docker)
order: 90
---

# サーバー (Docker)

複数の人が使う場合や、他の端末から開きたい場合は、Marginalia をサーバーとして動かしてください。ユーザーごとに、本、ロアブック、推論プロバイダー、プロトコルを個別に持てます。

<a id="requirements"></a>

## 要件

- Docker Compose を備えた [Docker](https://docs.docker.com/get-docker/)
- Marginalia 用に約 1 GB のメモリ
- インターネット経由でアクセスする場合: ドメインと、HTTPS に対応したリバースプロキシ（[下記](#https-and-reverse-proxy)参照）

<a id="install"></a>

## インストール

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

最初の `docker compose up` で、ソースからイメージをビルドします。数分かかります。その後 Marginalia はポート **8080** で待ち受けます。`http://<server>:8080` を開き、管理者アカウントを作成してください（[初回起動](first-start.md)を参照）。

<a id="prebuilt-image"></a>

### ビルド済みイメージ

ソースからビルドする代わりに、各リリースで公開されているイメージを使うこともできます（`linux/amd64` と `linux/arm64`）。`docker-compose.yml` の `build:` のブロックを `image:` に置き換えてください:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` は最新リリース、`edge` は開発版、`1.1.0`（または `1.1`）は特定のリリースに固定します。ファイルの他の部分はそのままです。

<a id="configuration"></a>

## 設定

すべての設定は `docker-compose.yml` で行います:

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
| `ports` | `"8080:8080"` は Marginalia をホストのポート 8080 に公開します。同じホスト上のリバースプロキシからだけアクセスさせたい場合は `"127.0.0.1:8080:8080"` を使ってください。 |
| `volumes` | `./data` にはデータベース、バックアップ、拡張機能が保存されます。保管し、サーバーのバックアップに含めてください。存在しない場合は初回起動時に作成されます。起動時にコンテナは、このフォルダーの所有者をコンテナ内で Marginalia が実行されるユーザー（`jetty`）に変更するため、中のファイルはホスト上のそのユーザーの ID が所有者になります。 |
| `-Xmx1g` | メモリの上限。多くのユーザーや非常に大きな本を扱う場合は増やしてください（例: `-Xmx2g`）。 |

ファイルを変更したら、`docker compose up -d` で反映してください。

<a id="environment-variables"></a>

### 環境変数

Marginalia 自身の設定は、`JAVA_TOOL_OPTIONS` と並べてサービスの環境変数として指定します:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 変数 | 既定値 | 役割 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`（イメージ内では `/var/marginalia/.marginalia`） | データベース、バックアップ、拡張機能、API キーを暗号化する鍵を保存するフォルダー。対応するボリューム（`./data:/your/folder`）とともにのみ変更してください。コンテナは起動時にこのフォルダーの所有者を固定します。 |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | ログイン画面の言語、および言語を選んでいないユーザーの言語（`en` または `ja`。拡張機能の言語はここでは使えません）。 |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` を指定すると *Save login* が非表示になります。 |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 保存されたログインの有効期間（秒単位。30 日）。 |
| `MARGINALIA_TRUSTED_PROXIES` | 空 | 信頼する `X-Forwarded-For` ヘッダーを送るリバースプロキシ。[下記](#https-and-reverse-proxy)を参照。`-DtrustedProxies` の代わりになります。 |
| `MARGINALIA_COOKIE_NAMESPACE` | 空 | *Save login* の Cookie 名の前に付ける名前。[1 つのホストで複数のアプリケーションを動かす](#several-applications-on-one-host)を参照。設定すると、ログインを保存していた全員がログアウトされます。 |
| `MARGINALIA_SESSION_COOKIE` | イメージ内では `JSESSIONID_MARGINALIA`（外では `JSESSIONID`） | セッション Cookie の名前。[1 つのホストで複数のアプリケーションを動かす](#several-applications-on-one-host)を参照。 |

同じ設定は Java オプション `-Dmarginalia.default-language=ja` としても指定できます。これは変数より優先されます。値が合わない場合（秒数に文字列を指定した、未知の言語を指定したなど）は、Marginalia は起動しません。理由はログに記録されます。

<a id="https-and-reverse-proxy"></a>

## HTTPS とリバースプロキシ

!!!danger インターネット越しに平文の HTTP を使わない
パスワードとログイン Cookie は毎リクエストで送信されます。平文の HTTP では、通信経路上の誰でもそれを読み取れます。Marginalia は HTTPS 対応のリバースプロキシの背後に置いてください。
!!!

ログイン画面の *Save login* オプションも HTTPS を必要とします。その Cookie は secure 属性が付いているため、ブラウザーは HTTPS 接続でのみ保持します。

Marginalia はページの更新を WebSocket 接続で行うため、プロキシは WebSocket のアップグレードを転送しなければなりません。`marginalia.example.com` の例:

+++ Caddy
Caddy は証明書を取得し、WebSocket も自動的に転送します。

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

ログインの失敗は、ユーザーごとおよびクライアントのアドレスごとに制限されます（1 つのアドレスから 30 回失敗すると、そのアドレスからのログインは、時間を延ばしながら最大 15 分間拒否されます）。Marginalia は、指定したプロキシの `X-Forwarded-For` ヘッダーのみを信頼します。このヘッダーがないと、すべてのユーザーがプロキシのアドレスから来ているように見え、誰か 1 人の 30 回の失敗がしばらく全員をブロックしてしまいます。ヘッダーは（上記のように）プロキシの設定に残し、`docker-compose.yml` の `environment` に `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>` を設定してください。Docker ホスト上のプロキシは Docker ネットワーク経由でコンテナに接続し、通常は `172.16.0.0/12` です。複数のエントリはコンマで区切ります。制限の状態はメモリに保持され、Marginalia を再起動すると消えます。

<a id="several-applications-on-one-host"></a>

### 1 つのホストで複数のアプリケーションを動かす

ブラウザーは Cookie をホスト名ごとに保持し、**ポートごとではありません**。`localhost:8080` の Marginalia と `localhost:8090` の別のプログラム（または 1 つのプロキシの背後にある同じドメインの 2 つのプログラム）は、`localhost` の Cookie を共有します。もう一方のプログラムも Java の Web アプリケーションである場合（Marginalia のプラットフォーム上に作られた別のアプリケーション、2 つ目の Marginalia など）は、両方が同じセッション Cookie `JSESSIONID` を使うため、一方にアクセスするたびに他方からログアウトされます。そのためイメージはセッション Cookie の名前を `JSESSIONID_MARGINALIA` としています。1 つのホストで 2 つの Marginalia コンテナを動かす場合は、2 つ目に別の名前を付けてください: `MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA2` と `MARGINALIA_COOKIE_NAMESPACE=second`（*Save login* の Cookie）。それぞれ別のホスト名（`marginalia.example.com`、`other.example.com`）を使うと、設定なしで回避できます。

<a id="models-on-the-same-machine"></a>

## 同じマシン上のモデル

コンテナ内では `localhost` はコンテナ自身を指し、サーバーではありません。Docker ホスト上で動くモデルサーバー（llama.cpp、Ollama、LM Studio など）を使うには、[推論プロバイダー](../inference-providers.md)でアドレス `http://host.docker.internal:<port>/v1` を指定してください。Linux では、`docker-compose.yml` のサービスに次を追加します:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

モデルサーバーも、`127.0.0.1` だけでなく、コンテナから到達できるアドレスで待ち受ける必要があります。

<a id="updating"></a>

## アップデート

```sh
git pull
docker compose up -d --build
```

[ビルド済みイメージ](#prebuilt-image)を使う場合は、代わりに `docker compose pull` と `docker compose up -d` を使ってください。

新しいバージョンが起動すると、データベースは自動的にアップグレードされます。Marginalia は構造を変更する前に[データベースのコピー](../administration/database-backups.md#copy-before-an-upgrade)を保存しますが、その前に自分で[データベースのバックアップ](../administration/database-backups.md)も作成してください。

<a id="automatic-updates"></a>

### 自動アップデート

リポジトリのルートにある `rebuild.sh`（Linux、macOS）と `rebuild.bat`（Windows）は、アップデートを自動で行います。手動で実行するか、cron ジョブやタスクスケジューラーから実行してください。チェックアウトしているブランチのリモートを取得し（どのブランチでも）、新しいものがなければ何もしません（`--force` は、例えば `docker-compose.yml` を変更した後などに、いずれにせよ再ビルドして再起動します）。新しいものがあれば、`git pull` を行い、**Marginalia が動いたまま**、[同梱プラグイン](#plugins)とイメージをビルドします。その後コンテナを停止し、プラグインの JAR を `data/extensions` にコピーして（インストール済みのものを置き換え）、新しいバージョンを起動します。どこかの手順が失敗すると、スクリプトはメッセージを表示して止まります。取得またはビルドが失敗した場合は、古いバージョンがまだ動いており、次回の実行で再度ビルドします。

`--stop-first` は、アプリケーションとビルドを同時に行うメモリがないサーバー向けに、ビルドの前にコンテナを停止します。この場合、ビルドの間ずっと Marginalia は停止します。

Linux では、スクリプトを手動で実行する必要はまったくありません。アップデートウォッチャーをインストールすると、管理者がボタンでサーバーを更新できます。[アップデート](../administration/updates.md)を参照してください。

<a id="plugins"></a>

## プラグイン

Docker イメージには[拡張機能](../extensions/index.md)は含まれておらず、アプリケーションと同じソースからビルドする必要があります。そのために Java や Maven は必要ありません。`marginalia/plugins/docker-compose.yml` がコンテナ内ですべてをビルドします。まずアプリケーションをビルドし、次にすべてのプラグインをビルドして、JAR を `marginalia/plugins/build` にコピーします:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

最初のビルドには数分かかります（アプリケーションの依存関係をダウンロードするため）。その後、JAR を `data/extensions` にコピーして Marginalia を再起動するか、*Admin → Extensions* で読み込んでください（[拡張機能](../administration/extensions.md)を参照）。`rebuild.sh` / `rebuild.bat` がコピーを代行するため、これらを使うサーバーでは、プラグインはアプリケーションと常に一緒に更新されます。
<a id="health"></a>

## ヘルスチェック

このイメージには Docker のヘルスチェックが組み込まれています。`docker ps` は、Marginalia の起動中（最大 2 分間）は `(health: starting)` を、起動後は `(healthy)` を表示します。`http://localhost:8080/health` が `200` での応答を止めると `unhealthy` になります。Docker は異常なコンテナを自動的に再起動しません。この状態は監視や自動復旧ツールで利用してください。

`/health` はログインが不要なので、監視ツールやロードバランサーからも利用できます（プロキシ経由の場合: `https://marginalia.example.com/health`）。Marginalia が動作していてデータベースも応答しているときは `200` と `{"status":"UP"}` を返します。起動中や何らかの問題があるときは `503` を `{"status":"DOWN",...}` とともに返します。それ以外の情報（バージョンや詳細など）は表示しません。理由はログに記録されています。

<a id="logs"></a>

## ログ

```sh
docker compose logs -f marginalia
```
