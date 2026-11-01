package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Anthropic provider in the languages besides English; the English text is the key.
 */
public class AnthropicLocalization extends PluginLocalizationBundle {

    public static final AnthropicLocalization T = new AnthropicLocalization();

    public static final String URL = "URL";
    public static final String NO_MODEL_IS_SET = "No model is set";
    public static final String NO_API_KEY_IS_SET = "No API key is set";
    public static final String THE_MODEL_GAVE_NO_ANSWER = "The model gave no answer (%s)";
    public static final String THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE = "The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort";
    public static final String THE_MODEL_GAVE_NO_ANSWER_S_S = "The model gave no answer (%s): %s";
    public static final String THE_KEY_OF_THE_CLAUDE = "The key of the Claude Console (platform.claude.com)";
    public static final String OPTIONAL_EMPTY_USES_ANTHROPICS = "Optional. Empty uses Anthropic's API, set it to go through a proxy.";
    public static final String THE_CONNECTION_TO_ANTHROPIC = "The connection to Anthropic ended before the answer was complete";
    public static final String THE_CONTEXT_WINDOW = "The context window of the model was used up";
    public static final String THE_PAYLOAD_HAS_TO_START = "The payload has to start and end with a message of the user, Claude does not continue an answer";

    @Override
    protected void loadKeys() {
        ja(URL, "URL");
        zh(URL, "URL");

        ja(NO_MODEL_IS_SET, "モデルが設定されていません");
        zh(NO_MODEL_IS_SET, "未设置模型");

        ja(NO_API_KEY_IS_SET, "APIキーが設定されていません");
        zh(NO_API_KEY_IS_SET, "未设置 API 密钥");

        ja(THE_MODEL_GAVE_NO_ANSWER, "モデルから回答がありませんでした（%s）");
        zh(THE_MODEL_GAVE_NO_ANSWER, "模型没有给出回答（%s）");

        ja(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "モデルから回答がありませんでした（%s）: 最初の単語の前に応答サイズを使い切りました。応答サイズを増やすか、推論の強度を下げてください");
        zh(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "模型没有给出回答（%s）：在输出第一个词之前，回复长度已用完，请调高回复长度或降低推理强度");

        ja(THE_MODEL_GAVE_NO_ANSWER_S_S, "モデルから回答がありませんでした（%s）: %s");
        zh(THE_MODEL_GAVE_NO_ANSWER_S_S, "模型没有给出回答（%s）：%s");

        ja(THE_KEY_OF_THE_CLAUDE, "Claude Consoleのキー（platform.claude.com）");
        zh(THE_KEY_OF_THE_CLAUDE, "Claude 控制台的密钥（platform.claude.com）");

        ja(OPTIONAL_EMPTY_USES_ANTHROPICS, "任意。空欄の場合はAnthropicのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh(OPTIONAL_EMPTY_USES_ANTHROPICS, "可选。留空则使用 Anthropic 的 API，设置后可通过代理访问。");

        ja(THE_CONNECTION_TO_ANTHROPIC, "回答が完了する前にAnthropicとの接続が終了しました");
        zh(THE_CONNECTION_TO_ANTHROPIC, "在回答完成之前，与 Anthropic 的连接已结束");

        ja(THE_CONTEXT_WINDOW, "モデルのコンテキストウィンドウを使い切りました");
        zh(THE_CONTEXT_WINDOW, "模型的上下文窗口已用完");

        ja(THE_PAYLOAD_HAS_TO_START, "ペイロードはユーザーのメッセージで始まり、ユーザーのメッセージで終わる必要があります。Claudeは回答の続きを書きません");
        zh(THE_PAYLOAD_HAS_TO_START, "载荷必须以用户的消息开头并以用户的消息结尾，Claude 不会续写回答");
    }
}
