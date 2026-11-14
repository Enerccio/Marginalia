---
label: レビュー機能
order: 2
---

# レビュー機能

モデルに文章を読んでもらい、読者の反応、編集者の講評、設定の整合性チェックなど、プロンプトで指定した内容のレビューを作らせます。レビューはパートごとに、本文の隣に保存され、本そのものには含まれません。

![パートのレビュー](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## パートをレビューする

[ストーリーエディタ](../books/story-editor.md)の各パートのメニュー（☰）には**Review**のサブメニューがあります。

| 項目 | |
|---|---|
| **View / Generate Review** | レビューのダイアログを開きます。パートにまだレビューがない場合は、すぐに生成されます。 |
| **Advanced Options** | [詳細オプション](#advanced-options)を開き、そのオプションでレビューを生成します。 |
| **Delete Review** | そのパートのレビューをすべて削除します。 |

レビューのダイアログでは次の操作ができます。

- **‹** / **›** でパートのレビューを切り替えます。最後のレビューで **›** を押すと、新しいレビューを生成します。
- **Stop generating** で、書き込み中のレビューを中断します。
- レビューの本文をクリックすると編集できます。入力欄の外をクリックすると保存されます。
- *Thinking Process* には、[推論モデル](../inference-providers.md#reasoning-models)の思考過程が表示されます。
- **Close** でダイアログを閉じます。

<a id="what-the-model-gets"></a>

## モデルに渡される内容

既定では、レビューはパートを書いたときと**同じプロンプト**を使います（*Use standard prompt info*）。つまり、ロアと要約を含むシステムプロンプト、それより前のパート、対象のパート、最後にレビュー用のプロンプトの順です。モデルは物語全体の文脈を踏まえてパートを読みます。

プロファイルに**Pre-prompt**が設定されている場合は、それが最初にシステムメッセージとして送られます。

<a id="advanced-options"></a>

## 詳細オプション

| オプション | |
|---|---|
| **Prompt Override** | このレビューにだけ使う別のレビュー用プロンプトです。初期値はプロファイルのプロンプトです。 |
| **Use standard prompt info** | オン：パートを書いたときのプロンプトを使います（上記のとおり）。オフ：下記のオプションを使い、ストーリーのパートだけを送ります。 |
| **Include Lorebook** | （オフのときのみ）パートを書いたときに有効だったロアを追加します。 |
| **With summaries** | （オフのときのみ）生成と同じように、まず物語の[要約](../books/summaries.md)を送り、その後に最新の要約より後のパートを送ります。最新の要約を含むパートとそれより前のパートは、要約で代表させます。要約は先にトークン上限の枠を使い、残りの枠をパートが新しい順に受け取ります。要約を作った後に物語が変更された要約は送られません（レビューのために削除されることはありません）。オフ：要約は送らず、入る分だけ最新のパートを送ります。 |
| **Send original text** | 翻訳機能（[Real Language Translator](real-language-translator.md)、[Conlang Translator](conlang-translator.md)）が読み込まれているときだけ表示されます。オフ：レビューでは、ほかのモデルと同じく、翻訳された台詞の背後にある英語を読みます。オン：レビュー対象のパート（*Use standard prompt info*がオフのときは、それより前のパートも）を保存された形のまま、台詞は翻訳後の文で送ります。*Use standard prompt info*がオンのときにパートを書いたプロンプトは英語から作られているので、そのまま変わりません。 |
| **Token Limit** | （オフのときのみ）送るストーリーのトークン数です。レビュー対象のパートまでを新しい順に、入る分だけ送ります。モデルのプロンプト分の余裕を差し引いた値が初期値です。 |

*Use standard prompt info*がオフのときは、ダイアログにリクエストの大きさ（*Total tokens*）が表示されます。**Generate**を押すと、これらのオプションを指定したレビューのダイアログが開きます。そのダイアログで続けて生成したレビューも、同じオプションを使います。

![レビューの詳細オプション](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## プロファイル

レビュー機能の設定は**設定 → Extension Settings → Reviewer Settings**にあります。設定は**プロファイル**単位で管理します。

![Reviewerの設定](../../images/plugin-reviewer-settings.png)

| 項目 | |
|---|---|
| **Profile** | 編集するプロファイルです。すべてのレビューにも、このプロファイルが使われます。 |
| **New**、**Rename**、**Delete** | プロファイルを管理します。*_Default*プロファイルは名前を変更することも削除することもできません。 |
| **Reviewer Model** | レビューを書く[推論プロバイダ](../inference-providers.md)です。空欄：その本のモデルを使います。 |
| **Reviewer Protocol** | [プロトコル](../protocols.md)です。空欄：その本のプロトコルを使います。 |
| **Review Pre-Prompt (System)** | 最初に送られるシステムメッセージです。例：*You are a demanding fiction editor.* |
| **Review Post-Prompt (User)** | レビューの依頼文です。最後に送られます。 |

変更を保存するには、*Settings*ページ上部の**Save**をクリックします。

既定の後置プロンプトは、物語の掲示板での議論を模したものです。架空のスレッドで、さまざまな読者から肯定的・否定的な30件の返信がつきます。ほかにも次のような使い方があります。

- *List continuity errors in the last part compared to the earlier story. Quote the passages.*
- *As a line editor, point out clichés, repetitions and weak verbs in the last part, with suggestions.*
- *Describe how a first-time reader would feel at the end of this part, and what they expect next.*

レビューの種類ごとにプロファイルを作り、レビューの前に使うものを選んでください。レビュー用に安価なモデルを使うと費用を抑えられます。そのモデルを、プロファイルの*Reviewer Model*に設定してください。

<a id="storage"></a>

## 保存

レビューはストーリーのパートとともに保存されるため、[本のバックアップ](../books/backups.md)や複製にも含まれます。[Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part)ではコピーされません。プロファイルはユーザー設定に保存されます。
