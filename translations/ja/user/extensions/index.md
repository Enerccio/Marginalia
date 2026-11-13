---
label: 拡張機能
order: 930
icon: plug
---

# 拡張機能

拡張機能は、Marginalia本体を変更せずに機能を追加します。Marginaliaには23の拡張機能が同梱されています。

| 拡張機能 | 追加される内容 |
|---|---|
| [Arc Outline](arc-outline.md) | 本のアークとその章を、状態とともにサイドバーに表示します。モデルにアークを書いてもらうこともできます。 |
| [Anthropic Claude](anthropic.md) | Anthropic自身のAPIを通じて、推論プロバイダーとしてAnthropicのClaudeモデルを使用します。思考の要約は推論として表示されます。 |
| [Audiobook](audiobook.md) | 音声サーバーを使って本をオーディオブックに変換します。語り手と各キャラクターに声を割り当て、最終版はスタジオで仕上げ、章マーク付きのmp3として書き出せます。[音声サーバーのセットアップ](audiobook-server.md)。 |
| [Author's Note](authors-note.md) | 本のすべての生成に挿入される、モデルへのメモです。 |
| [Beta Readers](beta-readers.md) | 架空の読者（ペルソナ）を作成し、その読者と本について、一人でも複数人のグループでもチャットできます。 |
| [Book Description Generator](book-description-generator.md) | Aboutタブのボタンから、物語の範囲をもとにモデルで本の名前と説明文を書きます。 |
| [Chapter Marker](chapter-marker.md) | パート内のMarkdownの見出しから、ストーリーのアウトラインに章タイトルを付けます。 |
| [Character Tracker](character-tracker.md) | 本に登場するキャラクターとその関係性を、関係図とともに表示します。 |
| [Conlang Translator](conlang-translator.md) | あなたが定義した人工言語で登場人物の台詞を表示します。モデル用に英語の原文を保持し、読者は クリックして原文を見られます。 |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | ツールの結果に別のロールを求めるGemma 4モデルでも、関数呼び出しが動作するようにします。 |
| [Google Gemini](google-genai.md) | Google自身のAPIを通じて、推論プロバイダーとしてGoogleのGeminiモデルを使用します。思考の要約は推論として表示されます。 |
| [Image Generator](image-generator.md) | 物語の一部の画像を作ります。モデルがプロンプトを書き、ComfyUIまたはOpenAI形式の画像APIが描画します。再生成して、気に入ったものを採用できます。 |
| [Prompt History](prompt-history.md) | セッション内（およびエージェントの実行ごと）にモデルへ送られた直近20件のリクエストを、回答と推論とともに保存します。プラグインのデバッグやプロンプトの確認に使います。設定 → Extension Settingsから、またはエージェントのコンソールから開きます。 |
| [Prompt Inspector](prompt-inspector.md) | 生成のプロンプトを表示し、モデルに送信される前に確認を求めます。物語の設定メニューでオン・オフを切り替えます。 |
| [Instruction Exporter](instruction-exporter.md) | 物語の指示、シーン設定、キャラクターをクリップボードにコピーします。 |
| [Knowledge Tracker](knowledge-tracker.md) | 追跡対象の各キャラクターが知っていること・知らないことを記録し、プロンプトに組み込みます。キャラクターが持ちえない知識を与えることを、モデルに防がせます。 |
| [Lore Generator](lore-generator.md) | 既存の設定資料をもとに、モデルがロアブックのエントリーの内容を書きます。 |
| [Lorebook VCS](lorebook-vcs.md) | ロアブックのエントリーの改訂履歴を管理します。 |
| [Manga Generator](manga-generator.md) | 物語の一部を、ページとコマからなるマンガの台本に変換し、画像生成用のタグも作ります。 |
| [Tools for the model](tools.md) | 生成ツールの**ツール**ボタン。モデルが使えるツールスイート（名前や場所など）と、そのカテゴリーを選択します。 |
| [Name generator](name-generator.md) | モデルが名前を発明する代わりに使える名前を提供します。約1270の名前リストをマルコフ連鎖として収録しています。 |
| [Name provider](name-provider.md) | モデルが選べる実在の名前を提供します。約100か国の名（ファーストネーム）と姓を、原綴りのまま収録しています。 |
| [Place provider](place-provider.md) | モデルが参照できる実在の場所を提供します。都市、海、湾、川、山などを座標と国とともに収録し、ある場所の周辺や二地点間の距離も調べられます。 |
| [Real Language Translator](real-language-translator.md) | 実在の言語（舞台が日本なら日本語など）で登場人物の台詞を表示します。モデル用に英語の原文を保持し、読者はクリックして原文を見られます。 |
| [Reviewer](reviewer.md) | モデルが書いた物語のパートのレビューを提供します。読者の反応や批評などです。 |
| [Side Query](side-query.md) | 物語の隣に表示され、本についてモデルと対話できるチャットです。 |
| [Converter](sillytavern-converter.md) | 物語の隣にSillyTavernのチャットと、モデルがそこから用意したターンの詳細を表示し、チャットを物語として書き直せます。また、古い本（バックアップまたは本そのもの）のパートを指示とともに読み込み、書き直すこともできます。 |
| [Story Notes](story-notes.md) | サイドバーに表示される、自分専用の小さなwikiです。ページ同士はタグやリンクで結べます。プロット、アイデア、世界のルールなどを整理できます。プロットウィザードやエージェントは、許可したページを読み書きできます。 |
| [Read Aloud](tts-reader.md) | 公開された本を、ビューアーでデバイスの声を使って読み上げます。 |
| [Chinese localization](chinese-localization.md) | ユーザーインターフェースの言語として、簡体字中国語を提供します。 |

<a id="installing"></a>

## インストール

拡張機能は**管理者**が、Marginaliaのインストール全体に対して、*Admin → Extensions*でインストールします。
拡張機能はJARファイルで、**拡張機能を読み込む（.jar）**からアップロードします。詳細は[拡張機能の管理](../administration/extensions.md)を参照してください。

インストールされた拡張機能は、すべてのユーザーが利用できます。Marginaliaの実行中に読み込まれた拡張機能は、その後に開かれたウィンドウに表示されます。表示するには、本を閉じて開き直すか、ページを再読み込みしてください。

!!!warning バージョンを合わせる
拡張機能は、特定のバージョンのMarginaliaのユーザーインターフェースにフックします。別のバージョン向けに作られた拡張機能は、動作しなくなったり、アプリケーションの一部を壊したりすることがあります。使用しているMarginaliaのバージョンに同梱された拡張機能を使用してください。
!!!

上記の拡張機能はMarginaliaのソースコード（`marginalia/plugins`）に含まれており、個別にビルドされます。
詳しくは[プラグインのREADME](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)を参照してください。

<a id="extensions-on-a-phone"></a>

## 電話での拡張機能

[電話向けレイアウト](../phone.md)には独自のクラスがあるため、拡張機能は電話に対応している必要があります。Arc Outline、Author's Note、Beta Readers、Chapter Marker、Character Tracker、Instruction Exporter、Reviewerは対応しています。Side Query、Lore Generator、Lorebook VCSは電話では何も追加しません（[電話でのMarginaliaの使用](../phone.md#extensions)を参照）。Name generator、Name provider、Place provider、Gemma 4 tools fix、Google Geminiは独自の画面を持ちません（Google Geminiはモデルのダイアログにプロバイダーを追加するだけで、これは電話にもあります）。Chinese localizationは電話メニューの言語選択にあります。拡張機能のインストールは、電話にはないAdminページから行います。

<a id="extension-settings"></a>

## 拡張機能の設定

設定を持つ拡張機能は、**設定 → Extension Settings**（*設定*ページの3番目のタブ）にセクションを追加します。
これらの設定は、*設定*の他の項目と同じくあなたのアカウントに属し、その**保存**ボタンで保存されます。

<a id="where-extension-data-is-kept"></a>

## 拡張機能のデータの保存場所

拡張機能は、データを、そのデータが属するものと一緒に保存します。レビューはストーリーのパートと、会話は本と、改訂履歴はロアブックと一緒に保存されるため、[データベースのバックアップ](../administration/database-backups.md)に含まれます。[本のバックアップ](../books/backups.md)やエクスポートにも含まれるかどうかは、各拡張機能のページに記載されています。

拡張機能をアンインストールしても、そのデータは削除されません。再びインストールすると元に戻ります。
