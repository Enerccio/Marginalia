---
label: デスクトップアプリ
order: 100
---

# デスクトップアプリ

デスクトップアプリは、1台のコンピューター向けの自己完結型Marginaliaです。独自のJavaランタイムとサーバーを含み、コンピューター上でのみ待ち受け、既定のブラウザーで開きます。

<a id="download"></a>

## ダウンロード

お使いのシステム向けのインストーラーまたはアーカイブを[リリースページ](https://github.com/Enerccio/Marginalia/releases)からダウンロードしてください。すべてのシステムについて4種類のバリアントが公開されています。

| ファイル | 内容 |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | インストーラー: Linux（Debian、Ubuntuおよびその派生）では`.deb`、macOSでは`.dmg`、Windowsでは`.msi` |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | 同じインストーラーで、すべての内蔵[プラグイン](../extensions/index.md)があらかじめインストールされています（後述） |
| `marginalia-<version>-<os>-<arch>-app` | アプリ: macOSでは`Marginalia.app`、Windowsでは`Marginalia.exe`、Linuxでは`bin/Marginalia` |
| `marginalia-<version>-<os>-<arch>` | スクリプトで起動するポータブルフォルダー。コマンドラインの[オプション](#options)を指定できます |

ビルドは、Windows x64、macOS（Apple SiliconおよびIntel）、Linux（x64およびarm64）向けに提供されています。ファイル名の`<arch>`は、64ビットのIntelおよびAMDプロセッサー（Windows、macOS Intel、Linux）では`x86_64`、ARM（Apple Silicon、Linux arm64）では`aarch64`です。

<a id="install-and-start"></a>

## インストールと起動

+++ macOS
インストーラーを使う場合: `.dmg`を開き、*Marginalia*を*Applications*にドラッグします。アーカイブを使う場合:

1. アーカイブを展開し、`Marginalia.app`を*Applications*に移動します。
2. 開きます。アプリはまだ署名されていないため、macOSは初回に起動を拒否します。アプリを右クリックして*開く*を選ぶか、次のコマンドを実行してください:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
インストーラーを使う場合: `.msi`を実行します。現在のユーザー向けにインストールされ（管理者権限は不要）、スタートメニューとデスクトップにMarginaliaが追加されます。新しい`.msi`を実行すると、インストール済みのバージョンがアップグレードされます（データは`.marginalia`に残ります）。アーカイブを使う場合:

1. アーカイブを展開します。
2. `Marginalia\Marginalia.exe`を実行します。
3. SmartScreenが不明な発行元について警告することがあります。*詳細情報*→*実行*をクリックしてください。
+++ Linux
`.deb`を使う場合（Debian、Ubuntuおよびその派生）:

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

`/opt/marginalia`にインストールされ、アプリケーションメニューに*Marginalia*が追加されます。`sudo apt remove marginalia`で削除できます（データは`~/.marginalia`に残ります）。アーカイブを使う場合:

1. アーカイブを展開します。
2. `Marginalia/bin/Marginalia`を実行します。
+++ ポータブルフォルダー
1. アーカイブを展開します。
2. `bin/marginalia`を実行します（Windowsでは`bin\marginalia.bat`）。
+++

起動には数秒かかります。その後、Marginaliaがブラウザーで`http://127.0.0.1:8765`を開き、[初回起動](first-start.md)のダイアログを表示します。

<a id="installer-with-the-plugins"></a>

### プラグイン付きのインストーラー

**full**インストーラー（ファイル名に`-full`を含むもの）も同じアプリで、Marginaliaに含まれるすべてのプラグインを同梱しています（AnthropicおよびGoogleのプロバイダー、Reviewer、Beta Readers、Side Query、Arc Outline、ジェネレーターなど）。Marginaliaを初めて起動すると、プラグインは`~/.marginalia/extensions`にコピーされ、*管理 → 拡張機能*に表示されます。通常のインストーラーの場合は、そこから1つずつ自分でインストールします。

- プラグインはインストールされるのは1回だけです。*管理 → 拡張機能*で削除すると、削除された状態のままになります。新しいバージョンのMarginaliaに更新した後も同様です（新しいバージョンは新しいプラグインを同梱し、それらを古いものの隣に配置します。Marginaliaは最新のものを使用し、古いJARは`extensions/old-versions`に保存されます）。更新で新しく追加されたプラグインはインストールされます。
- fullインストーラーを通常のインストーラーの上に（またはその逆に）インストールしても、データと設定は保持されます。すべての更新についても同様です。
- 通常のインストーラーはプラグインを削除しません。自分でインストールしたプラグインには一切手を加えません。

<a id="tray-icon"></a>

## トレイアイコン

実行中、Marginaliaはそのアイコン（余白に小さな分岐した木が描かれた原稿用紙）をシステムトレイ（macOSではメニューバー）に表示します。そのメニューには2つの項目があります:

- **Open Marginalia** - ブラウザーでMarginaliaを再度開きます（例: タブを閉じた後など）。
- **Quit Marginalia** - サーバーを停止します。ブラウザーのタブを閉じても、Marginaliaは停止しません。

すでに実行中のときにアプリをもう一度起動しても、ブラウザーで開くだけです。

![トレイアイコンとそのメニュー](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## データの場所

| 内容 | 場所 |
|---|---|
| データベース、バックアップ、拡張機能 | `~/.marginalia`（Windowsでは`%USERPROFILE%\.marginalia`） |
| ログ | `~/.marginalia/desktop/logs/marginalia.log`（前回の実行分は`marginalia.log.1`として保存されます） |

Marginaliaを別のコンピューターに移すには、終了してから`.marginalia`フォルダーをコピーします（保存されたAPIキーを復号する`secret.key`も含めてください）。大きな変更を加える前には、[データベースのバックアップ](../administration/database-backups.md)を作成してください。

<a id="options"></a>

## オプション

ポータブル版のランチャーは次のオプションを受け付けます:

```
marginalia --port 8765        # 待ち受けるポート。8765が使用中の場合は空いているポートが選ばれます
marginalia --host 127.0.0.1   # 待ち受けるアドレス
marginalia --home /path       # .marginaliaフォルダーを置くディレクトリ（既定: ホームディレクトリ）
marginalia --no-browser       # ブラウザーを開かない
marginalia --no-tray          # トレイアイコンを表示しない。Ctrl+Cで停止
marginalia --help
```

!!!warning 他のアドレス
`--host 0.0.0.0`（または他のアドレス）を指定すると、Marginaliaはネットワーク上から平文のHTTPで到達可能になります。他のデバイスからアクセスする場合は、代わりにHTTPSの背後にある[サーバー](docker-server.md)を使用してください。
!!!

<a id="updating"></a>

## 更新

Marginaliaを終了し、アプリ（またはポータブルフォルダー）を新しいバージョンに置き換えて、再度起動します。データは`.marginalia`に残り、初回起動時に自動的にアップグレードされます。データベースの構造が変わる前に、Marginaliaは`db-backups`に[そのコピー](../administration/database-backups.md#copy-before-an-upgrade)を保存します。拡張機能は1つのMarginaliaバージョン向けにビルドされているため、同時に更新してください（[拡張機能の管理](../administration/extensions.md)を参照）。

<a id="uninstalling"></a>

## アンインストール

アプリまたはフォルダーを削除します。データも削除する場合は、`.marginalia`フォルダーも削除してください。
