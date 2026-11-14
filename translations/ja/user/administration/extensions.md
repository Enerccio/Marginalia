---
label: 拡張機能の管理
order: 70
---

# 拡張機能の管理

管理者ページの **Extensions** タブでは、インストール全体に対する[拡張機能](../extensions/index.md)の追加と削除を行います。

![The Extensions tab](../../images/admin-extensions.png)

<a id="installing"></a>

## インストール

**Load Extension (.jar)** をクリックする（またはファイルをその上にドロップする）と、拡張機能の JAR ファイルを選択できます。ファイルは[データフォルダー](index.md#the-data-folder)の `extensions` フォルダーにコピーされ、すぐに開始されます。*Extension loaded successfully.* と表示されれば成功です。拡張機能は一覧に表示されます:

| 列 | |
|---|---|
| **ID** | この実行における拡張機能の内部番号です。 |
| **Extension Name** | 拡張機能の名前です。 |
| **Version** | 拡張機能のバージョンです。その横の **#** ボタンにマウスを重ねると、拡張機能の JAR の SHA-256 ハッシュが表示されます。クリックするとハッシュをコピーでき、作者が公開しているハッシュとの照合に使えます。 |
| **State** | 実行中のときは *Active*、停止したときは *Stopped*（[停止と開始](#stopping-and-starting)を参照）、起動に失敗したときは *Installed* または *Resolved*（[拡張機能が失敗したとき](#when-an-extension-fails)を参照）。 |
| **Verification** | *Valid* または *Invalid*。クリックすると、拡張機能を開始する前に Marginalia が行った検査の報告を読めます。 |
| **Actions** | **Stop** / **Start**、**Log**、**Unload**。 |

ユーザーは、それ以降に開くウィンドウで拡張機能を利用できます。すでに開いていた本は、いったん閉じて開き直すか、ページを再読み込みする必要があります。

JAR ファイルは、UI にアクセスできないサーバーなどの場合に、`extensions` フォルダーへ直接コピーすることもできます。その場合は次回の起動時に読み込まれます。Docker での環境準備もこの方法で行えます。JAR を `./data/extensions` に置いてください。JAR は Docker 内でビルドすることもできます。詳しくはサーバーガイドの [Plugins](../getting-started/docker-server.md#plugins) を参照してください。

!!!warning このバージョン向けにビルドされた拡張機能を使用する
拡張機能は、特定の Marginalia バージョンのユーザーインターフェースの一部を変更します。アプリケーションと同じソースからビルドされた拡張機能を使用してください（[プラグインの README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)を参照）。
互換性のない拡張機能は、読み込みに失敗したり、アプリケーションの一部を壊したりすることがあります。拡張機能 API は 1.0 でも固定されていないため、拡張機能は Marginalia と一緒に更新する必要があると考えてください。
!!!

<a id="stopping-and-starting"></a>

## 停止と開始

**Stop** は、拡張機能を削除せずに直ちにアンロードします。拡張機能が追加していた画面からは消えますが、JAR とデータは残り、状態 *Stopped* として一覧に残ります。このとき、ボタンは **Start** に変わり、押すと再び読み込まれます（アプリケーションの起動時と同じく、先に検査が行われます。検査に失敗した場合は停止したままになり、報告が表示されます）。停止中の拡張機能は、Marginalia を再起動しても停止したままです。停止中の拡張機能に新しいバージョンをアップロードすると、そのまま開始されます。

停止の状態は、JAR と同名の後ろに `.STOPPED` を付けた空のファイル（例: `sidequery.jar.STOPPED`）を `extensions` フォルダーに置くことで記録されます。UI にアクセスできないサーバーで拡張機能を起動させないようにするには、このファイルを作成します。次回の起動時に開始させたい場合は、このファイルを削除します。

<a id="the-extension-log"></a>

## 拡張機能のログ

**Log** を押すと、拡張機能のログが開きます。ここには、Marginalia が拡張機能向けに提供するロガーを使って拡張機能自身が書いた内容が表示されます（[Logging](../../developer/plugins/overview.md#logging)を参照）。表示されるのはファイルの末尾部分です。**Refresh** でファイルを読み直し、**Clear** でファイルを削除します。ファイルは `extensions/<name>.jar.log`（例: `sidequery.jar.log`）です。サイズが 2 MB を超えると `.log.old` に移動され、新しいログが始まります。拡張機能がアンロードされると削除されます。何もログを書かない拡張機能のログは空です。拡張機能が書いたログは、アプリケーションのログ（`extension.<name>` という名前で、各行のレベルで記録されます）にも出力されるため、サーバーのログにも残ります。Marginalia 自体の問題はそちらにのみ記録されます。

![The log of an extension](../../images/extension-log.png)

<a id="removing"></a>

## 削除

**Unload** は確認を求めたあと、拡張機能を直ちに停止し、`extensions` フォルダーから JAR（とそのログ）を削除します。
拡張機能のデータは残ります（[拡張機能のデータの保存場所](../extensions/index.md#where-extension-data-is-kept)を参照）。再び読み込むと、データも戻ります。

<a id="updating"></a>

## 更新

拡張機能の新しいバージョンをインストールするには、新しいファイルで **Load Extension (.jar)** を実行します。ファイル名は問いません。インストール済みのバージョンはアンロードされ、その JAR は削除され、新しいバージョンがその場所で開始されます。新しいバージョンの読み込みに失敗した場合、アップロードはエラーを表示し、古いバージョンは動き続けます。

<a id="when-an-extension-fails"></a>

## 拡張機能が失敗したとき

アップロードした拡張機能を読み込めない場合、*Failed to load extension: ...* と表示され、その JAR は保存されません。

Marginalia は、拡張機能を開始する前に、実行中のバージョンに照らして検査します。適合しない拡張機能、典型的には別のバージョンの Marginalia 向けにビルドされたものは読み込まれません。アップロードでは検査の報告が表示され、ファイルは保存されません。`extensions` フォルダーにある拡張機能は一覧に残りますが、開始はされず、*Verification* 列に **Invalid** と表示されます。報告はログにあります。Marginalia を更新した後にも検査は繰り返されるため、以前は動いていた拡張機能が、後から拒否されることがあります。その場合は、新しい Marginalia 向けにビルドされたバージョンの拡張機能を入手して読み込んでください。拡張機能が無効な間は、**ワークスペースの上に赤いバー**が表示され、全員にそのことを知らせます。管理者はマウスを重ねると無効な拡張機能を確認でき、バーをクリックするとこのページを開けます。ほかのユーザーには、管理者へ連絡するよう求める表示が出ます（自分で停止した拡張機能は数えません）。各拡張機能の JAR の隣には、報告を含む小さな `.valid` または `.invalid` ファイルがあります。これは拡張機能と一緒に削除され、そのまま残しておいても問題ありません。

![The red bar over the workspace when an extension is invalid](../../images/invalid-extensions-warning.png)

![The verification report of an extension that does not fit, and a stopped extension (Start button)](../../images/extension-verification.png)

`extensions` フォルダーにある拡張機能の中には、開始に失敗するもの（たとえば別のバージョン向けにビルドされたもの）があります。それがあっても、ほかの拡張機能の読み込みは止まりません。失敗した拡張機能は、*Active* ではなく *Installed* または *Resolved* の状態で一覧に表示され、**Unload** で削除できます。拡張機能ではないファイルは読み飛ばされ、一覧にも表示されません。そのようなファイルは `extensions` フォルダーから削除してください。エラーの詳細はログ（`docker compose logs`、またはデスクトップアプリの場合は `desktop/logs/marginalia.log`）にあります。
