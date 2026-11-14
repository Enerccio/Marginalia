---
label: ホーム
icon: home
order: 1000
---

# Marginalia

Marginaliaは、大規模言語モデルと共同で長編小説を書くためのセルフホスト型ライティングツールです。物語の各パートについての指示はあなたが書き、本文はモデルが生成します。Marginaliaはその周囲のすべてを整理しておきます。分岐する物語、世界設定のロア、モデルのコンテキストに長い作品を収めるための要約、文体を決めるプロンプト設定などです。

ブラウザで動作します。自分のコンピュータ上のデスクトップアプリとしても、自分や友人のための小さなサーバーとしても使えます。OpenAI互換のAPIであればどれとでも連携でき、OpenAI、OpenRouter、LiteLLM、llama.cpp、LM Studio、Ollama、vLLMなどに対応しています。

![ストーリーエディタ](images/hero-story-editor.png)

[!button variant="primary" text="Get started"](user/getting-started/index.md)
[!button text="Download"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Source on GitHub"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## できること

- **分岐する物語を持つブック** – パートを再生成またはスワイプし、任意の地点から分岐させ、ストーリーツリー内を移動し、生成された本文を編集できます。詳しくは [ブック](user/books/index.md) を参照してください。
- **ロアブック** – 世界観やキャラクターのメモを、必要なときだけプロンプトに入れます。ブックのタグ、テキストや正規表現によるフィルター、入れ子のロアブックで制御できます。SillyTavernのワールドインフォをインポートできます。詳しくは [ロアブック](user/lorebooks/index.md) を参照してください。
- **テンプレートとマクロ** – プロンプトはSillyTavern互換のマクロを使ったHandlebarsテンプレートです。そのため、インポートしたロアもそのまま動作します。詳しくは [テンプレートとマクロ](user/templates/index.md) を参照してください。
- **要約** – 古いパートは圧縮され、長いブックもコンテキストウィンドウに収まります。詳しくは [要約](user/books/summaries.md) を参照してください。
- **任意のOpenAI互換モデル** – 推論モデルを含み、モデルごとの上限や切り替え可能な生成設定に対応します。詳しくは [推論プロバイダー](user/inference-providers.md) と [プロトコル](user/protocols.md) を参照してください。
- **バックアップ** – ブック単位のバックアップで復元やコピーができ、データベース全体のバックアップをスケジュールで取得できます。詳しくは [ブックのバックアップ](user/books/backups.md) と [データベースのバックアップ](user/administration/database-backups.md) を参照してください。
- **複数ユーザー** – ユーザーごとに、ブック、ロア、モデルを個別に持てます。管理者はユーザー、バックアップ、クリーンアップを管理します。詳しくは [管理](user/administration/index.md) を参照してください。
- **公開されたブック** – 共有できる読み取り専用の閲覧画面です。詳しくは [公開とビューア](user/books/publishing.md) を参照してください。
- **検索と置換、コマンド** – ストーリーとロアブックを検索して置換できます。ストーリーメニューの横にはコマンドラインがあり、ブックを読み取ったり変更したりするコマンドを実行できます。詳しくは [コマンド](user/commands/index.md) と [正規表現](user/regular-expressions.md) を参照してください。
- **エージェント** – コマンドを通じて、コピーしたブックに対して単独で作業するモデルです。作業を見守り、コンソールで指示を出し、変更がブックに反映される前に内容を確認できます。詳しくは [エージェント](user/agents.md) を参照してください。
- **拡張機能** – プラグインを実行中に機能追加できます。チャプターマーカー、ロアブックの履歴、AIによるレビュー、モデルへの補足的な質問などがあります。詳しくは [拡張機能](user/extensions/index.md) を参照してください。

<a id="which-guide-do-you-need"></a>

## どのガイドが必要ですか

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[ユーザーガイド](user/index.md)**           | Marginaliaをインストールしたい、他の人のために実行したい、または執筆に使いたい場合。[はじめに](user/getting-started/index.md) から始めてください。 |
| **[開発者ガイド](developer/index.md)** | Marginaliaをソースからビルドしたい、変更したい、拡張機能を書きたい、または内部の仕組みを理解したい場合。                       |

<a id="quick-links"></a>

## クイックリンク

| やりたいこと                   |                                                          |
|--------------------------------|----------------------------------------------------------|
| 自分のコンピュータにインストールする      | [デスクトップアプリ](user/getting-started/desktop-app.md)       |
| サーバーで実行する             | [サーバー（Docker）](user/getting-started/docker-server.md) |
| モデルを接続する               | [推論プロバイダー](user/inference-providers.md)       |
| ブックの最初のパートを書く     | [クイックスタート](user/getting-started/quick-start.md)       |
| SillyTavernのロアブックを使う  | [インポートとエクスポート](user/lorebooks/import-export.md)       |
| 問題を解決する                 | [トラブルシューティングとFAQ](user/troubleshooting.md)         |
| 拡張機能を書く                 | [プラグイン開発](developer/plugins/index.md)       |
| バグを報告する、または貢献する | [コントリビューション](developer/contributing.md)                |

<a id="about"></a>

## このプロジェクトについて

Marginaliaは [MITライセンス](https://github.com/Enerccio/Marginalia/blob/master/LICENSE) のもとで公開されているオープンソースです。
このマニュアルのスクリーンショットで使用している画像のクレジットは [クレジット](credits.md) ページに記載しています。
リリースは [GitHub](https://github.com/Enerccio/Marginalia/releases) にあります。バグや提案は [issueトラッカー](https://github.com/Enerccio/Marginalia/issues) へお寄せください。
