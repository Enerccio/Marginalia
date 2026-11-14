---
label: Gemma 4ツールの修正
order: 15
---

# Gemma 4ツールの修正

結果のロールに別の名前を求めるAPIを持つGemma 4モデルでも、関数呼び出しが動作するようにします。

<a id="how-it-works"></a>

## 仕組み

モデルが関数を呼び出すと、Marginaliaは結果をロール `tool` のメッセージでモデルに返します。Gemma 4を動かす一部のサーバーはロール `tool_responses` を期待しており、そうしないとエラーを返すか、結果を無視します。

この拡張機能は、そのようなモデル向けにロールを変更します。[推論プロバイダー](../inference-providers.md)の**モデル**に `gemma4` または `gemma-4` が含まれる場合（英字の大文字・小文字は問いません）、そのモデルへの関数呼び出しの結果は `tool_responses` として送信されます。ほかのモデルへのリクエストは変更されません。これはSillyTavern拡張機能 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix) を移植したものです。

<a id="usage"></a>

## 使い方

設定は必要ありません。*Admin → Extensions* で拡張機能を読み込むと、次のリクエストから適用されます。
