package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Anthropic provider in the languages besides English; the English text is the key.
 */
public class AnthropicLocalization extends PluginLocalizationBundle {

    public static final AnthropicLocalization T = new AnthropicLocalization();

    @Override
    protected void loadKeys() {
        ja("URL", "URL");
        zh("URL", "URL");

        ja("No model is set", "モデルが設定されていません");
        zh("No model is set", "未设置模型");

        ja("No API key is set", "APIキーが設定されていません");
        zh("No API key is set", "未设置 API 密钥");

        ja("The model gave no answer (%s)", "モデルから回答がありませんでした（%s）");
        zh("The model gave no answer (%s)", "模型没有给出回答（%s）");

        ja("The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort", "モデルから回答がありませんでした（%s）: 最初の単語の前に応答サイズを使い切りました。応答サイズを増やすか、推論の強度を下げてください");
        zh("The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort", "模型没有给出回答（%s）：在输出第一个词之前，回复长度已用完，请调高回复长度或降低推理强度");

        ja("The model gave no answer (%s): %s", "モデルから回答がありませんでした（%s）: %s");
        zh("The model gave no answer (%s): %s", "模型没有给出回答（%s）：%s");

        ja("The key of the Claude Console (platform.claude.com)", "Claude Consoleのキー（platform.claude.com）");
        zh("The key of the Claude Console (platform.claude.com)", "Claude 控制台的密钥（platform.claude.com）");

        ja("Optional. Empty uses Anthropic's API, set it to go through a proxy.", "任意。空欄の場合はAnthropicのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh("Optional. Empty uses Anthropic's API, set it to go through a proxy.", "可选。留空则使用 Anthropic 的 API，设置后可通过代理访问。");

        ja("The connection to Anthropic ended before the answer was complete", "回答が完了する前にAnthropicとの接続が終了しました");
        zh("The connection to Anthropic ended before the answer was complete", "在回答完成之前，与 Anthropic 的连接已结束");

        ja("The context window of the model was used up", "モデルのコンテキストウィンドウを使い切りました");
        zh("The context window of the model was used up", "模型的上下文窗口已用完");

        ja("The payload has to start and end with a message of the user, Claude does not continue an answer", "ペイロードはユーザーのメッセージで始まり、ユーザーのメッセージで終わる必要があります。Claudeは回答の続きを書きません");
        zh("The payload has to start and end with a message of the user, Claude does not continue an answer", "载荷必须以用户的消息开头并以用户的消息结尾，Claude 不会续写回答");
    }
}
