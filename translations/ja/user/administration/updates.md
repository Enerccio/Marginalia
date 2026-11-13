---
label: アップデート
order: 65
---

# アップデート

Git のチェックアウトから作られた [Docker サーバー](../getting-started/docker-server.md)では、管理者はサーバーにログインしなくても、管理画面の **アップデート** タブから Marginalia を更新できます。サーバーはチェックアウトしているブランチの最新バージョンを取得し、アプリケーションと[同梱プラグイン](../getting-started/docker-server.md#plugins)をビルドして、再起動します。

サーバー上には **アップデート監視プログラム（update watcher）** という小さなプログラムが必要です。これは[一度だけセットアップ](#setting-up-the-watcher)します。監視プログラムがない場合、タブには実行中のバージョンが表示されるだけで、監視プログラムが動いていないことが示されます。[デスクトップアプリ](../getting-started/desktop-app.md)や[ビルド済みイメージ](../getting-started/docker-server.md#prebuilt-image)のサーバーでも同じ表示になります。これらは新しいバージョンをインストールして更新します。

<a id="the-update-tab"></a>

## アップデートタブ

| | |
|---|---|
| **Running version** | 動作している Marginalia のバージョンとコミット。 |
| **ブランチ** | サーバー上のチェックアウトが使っているブランチと、それが追従するリモートブランチ（例: `dev`、`origin/dev` に追従）。そのリモートブランチの最新コミットと、サーバーが最後に確認した時刻。サーバーは15分ごとに確認します。 |
| Status | 最新コミットが動作中のものと異なるときは *A new version is available*、そうでなければ *Marginalia is up to date*。更新の実行中は、タブにその旨が表示されます。 |
| **Last update** | 直前にここから開始された更新が終了した時刻と、完了したか失敗したか。 |

サーバーは、そこでチェックアウトされているブランチが何であっても、そのブランチに追従します。インストールを別のブランチに移すには、サーバー上のチェックアウトで `git switch` を実行してください。次回の確認の後に、タブに新しいブランチが表示されます。

| ボタン | |
|---|---|
| **アップデートして再起動** | 確認の後、サーバーに更新を依頼します。新しいものがなくても、ビルドして再起動します（たとえば `docker-compose.yml` を変更した後に使います）。 |
| **更新** | 状態を読み直します。タブは自動では更新されません。 |
| **ログ** | 直近の更新の出力、つまり Git とビルドが書き出した内容。 |

<a id="what-an-update-does"></a>

## アップデートの処理内容

1. 新しいバージョンをビルドします: `git pull`、プラグイン、イメージ。数分かかり、その間 **Marginalia は動作を続けます**。タブには *The update is running* と表示され、**ログ** で進行状況を確認できます。
2. Marginalia を停止し、新しいプラグインを `data/extensions` にコピーして（インストール済みのものを置き換えて）、新しいバージョンを起動します。1〜2分かかります（[ヘルスチェック](../getting-started/docker-server.md#health)を参照）。作業中の人は全員中断され、生成中のものはすべて停止します。確認画面には、その時点で実行中の[ジョブ](jobs.md)の数が表示されるので、静かな時間帯を選んでください。
3. 新しいバージョンの起動時にデータベースがアップグレードされますが、その前に[コピー](database-backups.md#copy-before-an-upgrade)が保存されます。念のため、先に自分でも[データベースのバックアップ](database-backups.md)を取ってください。

ビルドが失敗した場合は、何も停止されません。以前のバージョンが動き続け、**Last update** に *failed* と表示され、ログに理由が書かれます。次の更新では、取得すべき新しいものがなくても、再びビルドします。

<a id="setting-up-the-watcher"></a>

## 監視プログラムのセットアップ

Marginalia はコンテナの中で動くため、Git のチェックアウトにも Docker にも手が届きません。そこにアクセスを与えると、アプリケーションに入り込めた人が全員、サーバーの管理者になってしまいます。そのため、アプリケーションは依頼を出すだけです。依頼は[データフォルダ](index.md#the-data-folder)にファイルとして書き込まれ、コンテナの外、サーバー上で動く監視プログラムが実際の作業を行います。監視プログラムは、チェックアウトの `tools/update-watcher/watch.sh` というスクリプトです。実行するのは、同じチェックアウトの `rebuild.sh` だけです（[自動アップデート](../getting-started/docker-server.md#automatic-updates)を参照）。

<a id="linux-with-systemd"></a>

### systemd を使う Linux

サーバー上のチェックアウト（`docker-compose.yml` のあるフォルダ）で、次のコマンドを実行します:

```sh
sudo tools/update-watcher/install.sh
```

3つのユニットをインストールして起動します:

| ユニット | |
|---|---|
| `marginalia-update.service` | 監視プログラムを一度実行します。 |
| `marginalia-update.path` | アプリケーションが更新を依頼するとすぐに、監視プログラムを起動します。 |
| `marginalia-update.timer` | 起動の2分後に、その後は15分ごとに監視プログラムを起動し、新しいコミットがないか確認します。 |

**管理画面 → アップデート** を開き、**更新** をクリックしてください。タブにブランチと最新コミットが表示されます。以上で完了です。

| オプション | |
|---|---|
| `--name NAME` | ユニットの名前（既定値は `marginalia-update`）。1台のコンピュータに2つのチェックアウト（テスト用と本番用）がある場合は、それぞれ別の名前が必要です。 |
| `--user USER` | 監視プログラムを root ではなくこのユーザーとして実行します。[下記](#who-the-watcher-runs-as)を参照。 |
| `--uninstall` | ユニットを停止して削除します（同じ `--name` を指定します）。 |

`systemctl status marginalia-update.service` では最後の実行を、`journalctl -u marginalia-update.service` ではその出力を確認できます（**ログ** と同じ内容と、監視プログラム自体が動作できなかった場合の理由）。ユニットにはチェックアウトのパスが含まれているため、チェックアウトを移動したあとは `install.sh` を再実行してください。

<a id="without-systemd-cron"></a>

### systemd を使わない場合（cron）

監視プログラムを毎分実行します。誰も更新を依頼していないときや、前回リモートを確認してから15分経っていないときは何もしないので、負荷はかかりません。

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

この行を root の crontab（`sudo crontab -e`）に書くか、下記のように別のユーザーの crontab に書きます。そうすると、ボタンを押してから1分以内に更新が始まります。

<a id="settings"></a>

### 設定

監視プログラムは次の環境変数を読み取ります（systemd の場合は、`sudo systemctl edit marginalia-update.service` を実行し、`[Service]` の下に `Environment=NAME=value` の行を書きます）:

| 変数 | |
|---|---|
| `MARGINALIA_DATA_DIR` | サーバー上のデータフォルダ。チェックアウト内の `data` でない場合に指定します（`docker-compose.yml` のボリュームの左側）。 |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh` の引数。既定値は `--force` です。アプリケーションとビルドを同時に動かすメモリがないサーバーでは、`--force --stop-first` を使います。この場合、Marginalia はビルド前に停止し、ビルドの間ずっと停止したままになります。 |
| `MARGINALIA_CHECK_MINUTES` | 依頼のない実行で、状態がどれくらい古くなったらリモートを再確認するか。既定値は15です。systemd の場合は、監視プログラムの実行頻度をタイマーが決めます。 |

<a id="who-the-watcher-runs-as"></a>

### 監視プログラムの実行ユーザー

既定では、監視プログラムは **root** として動きます。Docker を使い、コンテナのユーザーが所有するデータフォルダに書き込む必要があるためです。Git 自体はチェックアウトの所有者として実行されるので、Git のファイルは所有者のもののままです。

監視プログラムは、ブランチに含まれるもの（`rebuild.sh`、`Dockerfile`）を実行します。これは手動のアップデートと同じです。信頼できるリポジトリだけを使い、チェックアウトへの書き込み権限は所有者だけに限定してください。`rebuild.sh` や `watch.sh` を変更できる人は、監視プログラムのユーザーとしてコマンドを実行できてしまうからです。

`--user USER`（またはそのユーザーの crontab）を指定すると、監視プログラムは別のユーザーとして実行されます。そのユーザーは Docker を使用できる必要があります（多くのシステムでは、これは root とほぼ同等の権限です）。さらに、チェックアウトを所有し、`data/update` と `data/extensions` に書き込める必要があります。これらは通常、コンテナのユーザーが所有しています。

<a id="windows-and-macos"></a>

### Windows と macOS

Docker Desktop 用の既成の監視プログラムはありません。`rebuild.bat` / `rebuild.sh` を手で実行するか、スケジュールされたタスクから実行して更新してください。[自動アップデート](../getting-started/docker-server.md#automatic-updates)を参照。

<a id="troubleshooting"></a>

## トラブルシューティング

| | |
|---|---|
| *監視プログラムがサーバー上で動いていない* | 監視プログラムが1時間以上、状態を書き出していない（または一度も書き出していない）。`systemctl status marginalia-update.timer marginalia-update.path` と `journalctl -u marginalia-update.service` を確認してください。データフォルダがチェックアウト内の `data` でない場合は、`MARGINALIA_DATA_DIR` を設定してください。 |
| *ブランチ ... はリモートブランチに追従していません* | チェックアウトが上流のないブランチ（または detached コミット）にあるため、取得するものがありません。サーバー上で `git switch <branch>` または `git branch --set-upstream-to=origin/<branch>` を実行してください。 |
| *サーバーが Git リポジトリに到達できませんでした* | 直近の確認で `git fetch` が失敗しました（ネットワークがない、またはチェックアウトの所有者に認証情報がないプライベートリポジトリ）。表示されている最新コミットは古いかもしれません。 |
| 更新を依頼したのに何も起きない | cron の場合、1分以内に開始します。1時間以内に誰も取り上げなかった依頼は破棄されます。監視プログラムを修復したときに、突然開始しないようにするためです。 |
| *Last update: ..., failed* | **ログ** を読んでください。`git pull` の失敗は、通常、チェックアウトに独自の変更（編集済みのファイルやローカルコミット）があることを意味します。サーバー上で解決してください。ビルドが失敗した場合は、古いバージョンが動き続けます。 |

監視プログラムのファイルはデータフォルダの `update` にあります: `status.properties`（タブに表示される内容）、`update.log`（直近の更新）、そして一時的に `request`。
