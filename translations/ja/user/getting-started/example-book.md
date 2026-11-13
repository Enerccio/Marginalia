---
label: サンプルの本
order: 80
---

# サンプルの本

*Unregistered* は、開いて読み、仕組みを確かめるための完成した本です。1 つの物語が約 100 パート（約 99,000 語）あり、Marginalia の機能が残したものがすべて入っています。長い本が [ストーリーエディタ](../books/story-editor.md)、[要約](../books/summaries.md)、[注釈](../books/annotations.md)、[統計](../books/statistics.md) の中でどう見えるかを知るのに適しており、また、すでにある内容で拡張機能を試すこともできます。

> チェコの地下室のガイドは小説から日本語を学びました。休暇中の日本人看護師とともに、プラハの地下室にある塞がれたアーチをくぐり抜けると、ヴラフト語を話す帝国が治める川の谷で目を覚まします。
> どちらも現地の言葉は分からず、生命税の査定官は、自分の台帳に載っていない二人の人物を見つけます。

<a id="what-is-in-it"></a>

## 中身

| | |
|---|---|
| **物語** | 本線 100 パートと、3 パートの枝分かれした支線があり、全 32 章からなります（見出しは本文の中にあります）。 |
| **要約** | 各章につき 1 つ（長さはさまざま。25 パートごとの最後の章は、モデルが生の状態で読むためそのまま残してあります）。古いものをまとめるメタ要約も含み、既定の[要約プロンプト](../books/summaries.md)の簡潔な形式で書かれています。 |
| **注釈** | 約 450 件の査読メモ（整合性、文体、ロア、視点、テンポ）。本の *AI で注釈を付ける* が提案し、書き直しで反映されたものです。各パートには版もあり、合計 360 版です（[版の履歴](../books/version-history.md)を参照）。 |
| **ロアブック** | 31 件のエントリ：世界、場所、人物、言語。 |
| **プロンプト** | 各パートには、それを書いたときのプロンプトと、有効になったロアが残っています。パートのメニューにある **プロンプトを表示**、**プロンプトを比較**、**ロアの使用状況** で確認できます（[本のプロンプト](../books/prompts.md)）。 |
| **Knowledge Tracker** | 追跡対象の人物 7 人について、パートごとに、それぞれが知っていることと知らないことを記録しています（[Knowledge Tracker](../extensions/knowledge-tracker.md)）。 |
| **翻訳された台詞** | 約 1,250 の翻訳された一節。ドミニオンの **Vrakht** の台詞と、谷の **Aethel-Vael** の台詞（[Conlang Translator](../extensions/conlang-translator.md)）、そして **日本語** と **チェコ語**（[Real Language Translator](../extensions/real-language-translator.md)）です。 |
| **章** | 本文から [Arc Outline](../extensions/arc-outline.md) が作成した、物語の概要です。 |
| **挿絵** | 第 31 パートにある挿絵 1 点です。 |

4 つの拡張機能のデータは本の中にありますが、拡張機能を読み込んだときにだけ見えます。拡張機能がなければ、データは保持されたまま無視されます。この本にはモデルもプロトコルも含まれていません。インポートの後で、本の設定から使うものを選んでください。

<a id="download"></a>

## ダウンロード

ファイルはリリース [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1) に添付されています：

| ファイル | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip)（5.7 MB） | 本そのもの：挿絵を含むバックアップです。 |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | この本のロアブックを単体で入れたものです。本のファイルにも含まれています。本を読み込まずにロアブックだけを見たい場合にのみ必要です。 |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | ドミニオンの人工言語で、Conlang Translator 用です。 |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | 谷の人工言語で、Conlang Translator 用です。 |

<a id="importing-it"></a>

## インポートの方法

1. **言語**（Conlang Translator を使う場合）：**設定 → 拡張機能の設定 → Conlang Translator** で **インポート** をクリックし、`vrakht.json` を選びます。続けて `aethel-vael.json` でも同じ操作をします（[Conlang Translator](../extensions/conlang-translator.md#languages)）。本をインポートする前に行ってください。そうすると、本の言語設定が参照できる先ができます。
2. **本：** 本のウィンドウの *設定* タブで **バックアップを新しい本としてインポート** をクリックし、名前を入力して `unregistered-book.zip` をアップロードします（[バックアップのインポート方法](../books/backups.md#importing-a-backup-as-a-new-book)）。Marginalia がロアブックについて尋ねてきたら、**インポート** を選んでください。ファイルに含まれています。
3. 新しい本を開き、その設定でモデルとプロトコルを選びます。

翻訳された台詞は本の中に保存されているため、ヴラフト語、アエテル＝ヴァエル語、日本語、チェコ語の台詞は翻訳されたとおりに表示されます。Real Language Translator でさらに翻訳するには、そこで言語 *日本語* と *チェコ語* を作成してください（プラグインがプリセットとして提示します）。プラグインは本と一緒に言語を書き出さないためです。

<a id="where-it-comes-from-and-the-licence"></a>

## 出典とライセンス

物語、ロアブック、そして 2 つの言語はオリジナルです。概要、ロアブック、言語、パートの構成は Marginalia の作者が書きました。パートの本文は **AI モデル**（Claude Haiku）が書いたもので、Marginalia 自身の生成ループを通じて作られています。生成、注釈、書き直し、知識の追跡、翻訳、要約という流れです。これは本プログラムのデモであり、出版された小説ではありません。モデルの文章特有の癖もあります。

- 本文、ロアブック、2 つの言語は [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) のもとでパブリックドメインに捧げられています。どのような用途にも使えます。
- 挿絵は、Willem Adriaan Alexander Liernur が 1887 年に描いた *Interior of a Cottage in Scheveningen* で、パブリックドメインです。出典は [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art) です。
