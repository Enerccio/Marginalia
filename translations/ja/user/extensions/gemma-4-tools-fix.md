---
label: Gemma 4 ツール修正
order: 15
---

# Gemma 4 ツール修正

API が呼び出し結果に別のロールを期待する Gemma 4 モデルで、関数呼び出しが動作するようにします。

!!!warning 現在のチャットテンプレートでは不要
古い Gemma 4 モデルでのみ読み込んでください。2026 年 4 月以降、Google の Gemma 4 のチャットテンプレートは、Marginalia が送信するロール `tool` で結果を読み取ります（それ以降の GGUF はすべてこれを含みます）。このようなテンプレートでは、この拡張機能はプロンプトを悪化させます。結果が、テンプレートが認識しない余分なターンとして届くためです。llama.cpp も古い形式を無視するため、そこではロール `tool` だけが機能します。

古い GGUF は、作成されたときのテンプレートをそのまま保持しています。お使いのモデルを確認するには、llama.cpp の `/apply-template` で関数呼び出しとその結果をレンダリングし、モデルのターンに `<|tool_response>` があるか調べてください。ある場合は、この拡張機能を読み込まないでください。呼び出しを行わず、テキストとして書いてしまうモデルには、代わりに[モデル設定](../inference-providers.md)の *Function call rule* が役立ちます。
!!!

<a id="how-it-works"></a>

## 仕組み

モデルが関数を呼び出すと、Marginalia は結果をロール `tool` のメッセージでモデルに返します。Gemma 4 を実行する一部のサーバーはその代わりにロール `tool_responses` を要求し、そうでない場合はエラーを返すか結果を無視します。

この拡張機能は、そのようなモデルに対してロールを変更します。[推論プロバイダー](../inference-providers.md)の *Model* に `gemma4` または `gemma-4` が含まれる場合（英字の大文字・小文字は問いません）、そのモデルへの関数呼び出しの結果は `tool_responses` として送信されます。他のモデルへのリクエストは変更されません。これは SillyTavern 拡張機能 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix) を移植したものです。

<a id="usage"></a>

## 使い方

設定する項目はありません。*Admin → Extensions* で拡張機能を読み込んでください。次のリクエストから適用されます。
