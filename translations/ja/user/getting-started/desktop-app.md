---
label: デスクトップアプリ
order: 100
---

# デスクトップアプリ

デスクトップアプリは、1台のコンピューター向けの自己完結型Marginaliaです。独自のJavaランタイムとサーバーを同梱し、お使いのコンピューター上でのみ待ち受け、既定のブラウザで開きます。

<a id="download"></a>

## ダウンロード

お使いのシステム向けのインストーラーまたはアーカイブを[リリースページ](https://github.com/Enerccio/Marginalia/releases)からダウンロードしてください。すべてのシステムについて4種類のバリアントが公開されています:

| ファイル | 内容 |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | インストーラー: Linux（Debian、Ubuntuおよびその派生）では`.deb`、macOSでは`.dmg`、Windowsでは`.msi` |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | 同じインストーラーで、**組み込みの[プラグイン](../extensions/index.md)をすべて**最初からインストール済みのもの（後述） |
| `marginalia-<version>-<os>-<arch>-app` | アプリ本体: macOSでは`Marginalia.app`、Windowsでは`Marginalia.exe`、Linuxでは`bin/Marginalia` |
| `marginalia-<version>-<os>-<arch>` | スクリプトで起動するポータブルフォルダー。コマンドラインの[オプション](#options)を指定できます |

ビルドはWindows x64、macOS（Apple SiliconおよびIntel）、Linux（x64およびarm64）向けに用意されています。ファイル名の`<arch>`は、64ビットのIntel・AMDプロセッサー（Windows、macOS Intel、Linux）向けは`x86_64`、ARM（Apple Silicon、Linux arm64）向けは`aarch64`を意味します。

<a id="install-and-start"></a>

## インストールと起動

+++ macOS
インストーラーを使う場合: `.dmg`を開き、*Marginalia*を*アプリケーション*にドラッグします。アーカイブを使う場合:

1. アーカイブを展開し、`Marginalia.app`を*アプリケーション*に移動します。
2. 開きます。このアプリはまだ署名されていないため、macOSは初回起動時に開くのを拒否することがあります。アプリを右クリックして*開く*を選ぶか、次を実行してください:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
インストーラーを使う場合: `.msi`を実行します。インストールはお使いのユーザー向けに行われ（管理者権限は不要）、スタートメニューとデスクトップにMarginaliaを追加します。新しい`.msi`は、インストール済みのバージョンをアップグレードします（データは`.marginalia`に残ります）。アーカイブを使う場合:

1. アーカイブを展開します。
2. `Marginalia\Marginalia.exe`を実行します。
3. SmartScreenが不明な発行元について警告することがあります。*詳細情報*→*実行*をクリックしてください。
+++ Linux
`.deb`を使う場合（Debian、Ubuntuおよびその派生）:

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

インストール先は`/opt/marginalia`で、アプリケーションメニューに*Marginalia*が追加されます。`sudo apt remove marginalia`で削除できます（`~/.marginalia`にあるデータは残ります）。アーカイブを使う場合:

1. アーカイブを展開します。
2. `Marginalia/bin/Marginalia`を実行します。
+++ ポータブルフォルダー
1. アーカイブを展開します。
2. `bin/marginalia`を実行します（Windowsでは`bin\marginalia.bat`）。
+++

起動には数秒かかります。その後、Marginaliaがブラウザの`http://127.0.0.1:8765`で開き、[初回起動](first-start.md)のダイアログが表示されます。

<a id="installer-with-the-plugins"></a>

### プラグイン同梱のインストーラー

**フル**インストーラー（ファイル名の`-full`）は同じアプリで、Marginaliaに含まれるすべてのプラグイン（AnthropicおよびGoogleのプロバイダー、Reviewer、Beta Readers、Side Query、Arc Outline、各種ジェネレーターなど）が同梱されています。Marginaliaを初めて起動すると、プラグインが`~/.marginalia/extensions`にコピーされ、*管理 → 拡張機能*に表示されます。通常のインストーラーの場合は、そこから1つずつ自分でインストールします。

- プラグインはインストールが1回で済みます。*管理 → 拡張機能*で削除すると、削除した状態のままになります。新しいバージョンのMarginaliaに更新した後も同様です（更新では新しいプラグインが古いものと並んで配置され、Marginaliaは最新のものを使い、古いJARは`extensions/old-versions`に残します）。更新で新しく追加されたプラグインはインストールされます。
- フルインストーラーを通常のものの上から（またはその逆に）インストールしても、データと設定は残ります。どの更新でも同じです。
- 通常のインストーラーはプラグインを削除しません。自分でインストールしたプラグインには一切触れません。

<a id="tray-icon"></a>

## トレイアイコン

実行中、Marginaliaはそのアイコン（余白に小さな分岐する木が描かれた原稿用紙）をシステムトレイに表示します（macOSではメニューバーに表示されます）。そのメニューには2つの項目があります:

- **Marginaliaを開く** - タブを閉じた後などに、ブラウザでMarginaliaを開き直します。
- **Marginaliaを終了** - サーバーを停止します。ブラウザのタブを閉じても、Marginaliaは停止しません。

すでに実行中のときにアプリをもう一度起動すると、ブラウザで開くだけです。

![トレイアイコンとそのメニュー](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## データの保存場所

| 内容 | 場所 |
|---|---|
| データベース、バックアップ、拡張機能 | `~/.marginalia`（Windowsでは`%USERPROFILE%\.marginalia`） |
| ログ | `~/.marginalia/desktop/logs/marginalia.log`（前回の実行分は`marginalia.log.1`として保存されます） |

Marginaliaを別のコンピューターへ移すには、終了してから`.marginalia`フォルダーをコピーします（保存されたAPIキーを復号する`secret.key`も一緒に）。大きな変更を加える前に、[データベースのバックアップ](../administration/database-backups.md)を取ってください。

<a id="options"></a>

## オプション

ポータブル用の起動スクリプトは、次のオプションを受け付けます:

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning 他のアドレス
`--host 0.0.0.0`（または他のアドレス）を指定すると、Marginaliaはネットワーク上からプレーンHTTPで到達可能になります。他の機器からアクセスする場合は、代わりにHTTPSの背後に置いた[サーバー](docker-server.md)を使ってください。
!!!

<a id="updating"></a>

## 更新

Marginaliaを終了し、アプリ（またはポータブルフォルダー）を新しいバージョンに置き換えてから、再び起動します。データは`.marginalia`に残り、初回起動時に自動でアップグレードされます。データベースの構造が変わる前に、Marginaliaは[そのコピー](../administration/database-backups.md#copy-before-an-upgrade)を`db-backups`に保存します。拡張機能は特定のMarginaliaバージョン向けにビルドされているため、同時に更新してください（[拡張機能の管理](../administration/extensions.md)を参照）。

<a id="uninstalling"></a>

## アンインストール

アプリまたはフォルダーを削除します。データも削除する場合は、`.marginalia`フォルダーも削除してください。
