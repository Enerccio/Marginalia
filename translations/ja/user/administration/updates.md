---
label: アップデート
order: 65
---

# アップデート

[Docker サーバー](../getting-started/docker-server.md)上で Git のチェックアウトからビルドされた Marginalia は、サーバーにログインしなくても、管理者が Admin ページの **Update** タブからアップデートできます。サーバーはチェックアウトしているブランチの最新バージョンを取得し、アプリケーションと[同梱プラグイン](../getting-started/docker-server.md#plugins)をビルドして再起動します。

サーバーには小さなプログラムである**アップデートウォッチャー**が必要で、これは[一度だけセットアップ](#setting-up-the-watcher)します。ウォッチャーがなければ、このタブは実行中のバージョンを表示し、ウォッチャーが動いていないと伝えるだけです。[デスクトップアプリ](../getting-started/desktop-app.md)や[ビルド済みイメージ](../getting-started/docker-server.md#prebuilt-image)を使うサーバーも同様です。これらは新しいバージョンをインストールして更新します。

<a id="the-update-tab"></a>

## Update タブ

| | |
|---|---|
| **Running version** | 実行中の Marginalia のバージョンとコミット。 |
| **Branch** | サーバー上のチェックアウトが使っているブランチと、それが追跡するリモートブランチ（例: `dev`、追跡先 `origin/dev`）、そのリモートブランチの最新コミット、およびサーバーが最後に確認した時刻。サーバーは 15 分ごとに確認します。 |
| Status | 最新コミットが実行中のものと異なる場合は *A new version is available*、そうでなければ *Marginalia is up to date*。更新の実行中は、タブにその旨が表示されます。 |
| **Last update** | ここから開始した最後の更新が終了した時刻と、完了したか失敗したか。 |

サーバーは、そこでチェックアウトされているブランチが何であれ、それに従います。インストールを別のブランチに移すには、サーバー上のチェックアウトで `git switch` を実行してブランチを切り替えてください。タブには次回の確認後に新しいブランチが表示されます。

| Button | |
|---|---|
| **Update and restart** | 確認の後、サーバーにアップデートを依頼します。新しいものがなくても、ビルドして再起動します（例えば `docker-compose.yml` を変更した後に使います）。 |
| **Refresh** | 状態を再読み込みします。タブは自動では更新されません。 |
| **Log** | 最後の更新の出力。Git とビルドが書き出した内容です。 |

同じことはスクリプトからも行えます。アクセス権 *Update the installation* を持つ [API キー](../account.md#api-keys)を使えば、状態の読み取り、アップデートの依頼、ログの読み取りができます（[開発者マニュアル](../../developer/api.md)を参照）。

<a id="what-an-update-does"></a>

## アップデートの動作

1. 新しいバージョンをビルドします: `git pull`、プラグイン、イメージ。これには数分かかり、その間 **Marginalia は実行され続けます**。タブには *The update is running* と表示され、**Log** で進行状況がわかります。
2. Marginalia が停止し、新しいプラグインが `data/extensions` にコピーされ（インストール済みのものは置き換えられます）、新しいバージョンが起動します。これには 1〜2 分かかります（[ヘルスチェック](../getting-started/docker-server.md#health)を参照）。
   作業中の全員が中断され、生成中のものはすべて停止します。確認画面には、その時点で実行中の[ジョブ](jobs.md)数が表示されるので、静かな時間帯を選んでください。
3. 新しいバージョンの起動時に、データベースがアップグレードされます。その前に[コピー](database-backups.md#copy-before-an-upgrade)が保存されています。自分でも先に[データベースのバックアップ](database-backups.md)を取っておいてください。

ビルドが失敗した場合、何も停止しません。以前のバージョンが実行され続け、**Last update** には *failed* と表示され、ログに理由が書かれます。次回の更新は、新しいものを取得しなくても再びビルドします。

<a id="setting-up-the-watcher"></a>

## ウォッチャーのセットアップ

Marginalia はコンテナ内で動作し、Git のチェックアウトにも Docker にもアクセスできません。そのアクセスを与えると、アプリケーションに入ってきた誰もがサーバーの管理者になってしまうためです。そこで Marginalia はただ要求するだけです。[データフォルダ](index.md#the-data-folder)にファイルを書き込み、コンテナの外でサーバー上で動くウォッチャーが作業を行います。ウォッチャーはチェックアウト内のスクリプト `tools/update-watcher/watch.sh` で、これが実行するのは常に同じチェックアウトの `rebuild.sh` だけです（[自動アップデート](../getting-started/docker-server.md#automatic-updates)を参照）。

<a id="linux-with-systemd"></a>

### systemd を使う Linux

サーバー上のチェックアウト（`docker-compose.yml` があるフォルダ）で:

```sh
sudo tools/update-watcher/install.sh
```

3 つのユニットをインストールして起動します:

| ユニット | |
|---|---|
| `marginalia-update.service` | ウォッチャーを一度実行します。 |
| `marginalia-update.path` | アプリケーションが更新を要求した瞬間に起動します。 |
| `marginalia-update.timer` | 起動の 2 分後に開始し、その後 15 分ごとに起動して、新しいコミットを確認します。 |

**Admin → Update** を開き、**Refresh** をクリックします。タブにブランチと最新コミットが表示されます。以上です。

| オプション | |
|---|---|
| `--name NAME` | ユニットの名前（デフォルトは `marginalia-update`）。同じコンピュータ上の 2 つのチェックアウト（テスト環境と本番環境）は、それぞれ別の名前が必要です。 |
| `--user USER` | ウォッチャーを root ではなく、このユーザーとして実行します。[下記](#who-the-watcher-runs-as)を参照。 |
| `--uninstall` | ユニットを停止して削除します（同じ `--name` と共に）。 |

`systemctl status marginalia-update.service` は最後の実行を、`journalctl -u marginalia-update.service` はその出力を表示します（**Log** と同じ内容、およびウォッチャー自体が動作できなかった場合はその理由）。ユニットにはチェックアウトのパスが含まれるため、チェックアウトを移動した後は `install.sh` を再度実行してください。

<a id="without-systemd-cron"></a>

### systemd を使わない場合（cron）

ウォッチャーを毎分実行してください。誰もアップデートを要求していない場合や、前回リモートを確認してから 15 分経っていない場合は何もしないので、負荷はかかりません:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

この行を root の crontab（`sudo crontab -e`）、または下記の別のユーザーの crontab に追加してください。そうすると、クリックから 1 分以内に更新が始まります。

<a id="settings"></a>

### 設定

ウォッチャーは次の環境変数を読みます（systemd の場合は `[Service]` 以下の `sudo systemctl edit marginalia-update.service` と `Environment=NAME=value` の行で指定）:

| 変数 | |
|---|---|
| `MARGINALIA_DATA_DIR` | サーバー上のデータフォルダ。`data` がチェックアウト内にない場合に指定します（`docker-compose.yml` のボリュームの左側）。 |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh` の引数。デフォルトは `--force` です。アプリケーションとビルドを合わせたメモリがないサーバーでは `--force --stop-first` を使ってください。その場合、ビルドの前に Marginalia が停止し、ビルドの全期間中ずっと停止したままになります。 |
| `MARGINALIA_CHECK_MINUTES` | 要求のない実行が、リモートを再度確認するまでに状態がどれくらい古くてよいか（分）。デフォルトは 15。systemd の場合は、タイマーがウォッチャーの実行頻度を決めます。 |

<a id="who-the-watcher-runs-as"></a>

### ウォッチャーの実行ユーザー

デフォルトでは、ウォッチャーは **root** として実行されます。Docker を使い、コンテナのユーザーが所有するデータフォルダに書き込む必要があるためです。Git 自体はチェックアウトの所有者として実行されるので、Git のファイルは所有者のものとして保たれます。

ウォッチャーは、ブランチに含まれるもの（`rebuild.sh`、`Dockerfile`）を実行します。これは手動のアップデートと同じです。信頼できるリポジトリだけを追跡し、チェックアウトへの書き込みはその所有者だけに限定してください。そこで `rebuild.sh` や `watch.sh` を変更できる者は、ウォッチャーのユーザーとしてコマンドを実行できてしまうからです。

`--user USER` を使う場合（またはそのユーザーの crontab）、ウォッチャーは別のユーザーとして実行されます。そのユーザーは Docker を使える必要があり（多くのシステムではそれは root とほぼ同等です）、チェックアウトを所有し、`data/update` と `data/extensions` に書き込めなければなりません。これらは通常、コンテナのユーザーが所有しています。

<a id="windows-and-macos"></a>

### Windows と macOS

Docker Desktop 用の既成のウォッチャーはありません。`rebuild.bat` / `rebuild.sh` を使って手動で、またはスケジュールされたタスクから更新してください（[自動アップデート](../getting-started/docker-server.md#automatic-updates)を参照）。

<a id="troubleshooting"></a>

## トラブルシューティング

| | |
|---|---|
| *The update watcher does not run on the server* | ウォッチャーが 1 時間以上、または一度も状態を書き込んでいません。`systemctl status marginalia-update.timer marginalia-update.path` と `journalctl -u marginalia-update.service` を確認してください。データフォルダがチェックアウト内の `data` でない場合は、`MARGINALIA_DATA_DIR` を設定してください。 |
| *Branch ... follows no remote branch* | チェックアウトが上流のないブランチ（またはデタッチされたコミット）上にあるため、取得するものがありません。サーバーで `git switch <branch>` または `git branch --set-upstream-to=origin/<branch>` を実行してください。 |
| *The server could not reach the Git repository* | 前回の確認で `git fetch` が失敗しました（ネットワークがない、チェックアウトの所有者に認証情報がないプライベートリポジトリ、など）。表示されている最新コミットは古い可能性があります。 |
| The update was requested and nothing happens | cron の場合は 1 分以内に開始します。1 時間の間誰も処理しなかった要求は破棄されます。ウォッチャーを修復したときに、突然開始しないようにするためです。 |
| *Last update: ..., failed* | **Log** を読んでください。失敗した `git pull` は、通常チェックアウトに独自の変更（編集されたファイル、ローカルコミット）があることを意味します。サーバー上で解決してください。ビルドが失敗した場合は、古いバージョンが実行され続けます。 |

ウォッチャーのファイルはデータフォルダの `update` にあります: `status.properties`（タブに表示される内容）、`update.log`（最後の更新）、そして一時的に `request`。
