package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Google Gemini provider in the languages besides English; the English text is the key.
 */
public class GoogleGenAiLocalization extends PluginLocalizationBundle {

    public static final GoogleGenAiLocalization T = new GoogleGenAiLocalization();

    public static final String URL = "URL";
    public static final String NO_MODEL_IS_SET = "No model is set";
    public static final String NO_API_KEY_IS_SET = "No API key is set";
    public static final String THE_MODEL_GAVE_NO_ANSWER = "The model gave no answer (%s)";
    public static final String THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE = "The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort";
    public static final String THE_MODEL_GAVE_NO_ANSWER_S_S = "The model gave no answer (%s): %s";
    public static final String THE_KEY_OF_GOOGLE_AI = "The key of Google AI Studio (aistudio.google.com/apikey)";
    public static final String OPTIONAL_EMPTY_USES_GOOGLES = "Optional. Empty uses Google's API, set it to go through a proxy.";
    public static final String TURN_OFF_THE_SAFETY_FILTERS = "Turn off the safety filters";
    public static final String SENDS_BLOCK_NONE = "Sends BLOCK_NONE for the adjustable categories (harassment, hate speech, sexually explicit, dangerous content). Google still blocks some content whatever is sent.";

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

        ja(THE_KEY_OF_GOOGLE_AI, "Google AI Studioのキー（aistudio.google.com/apikey）");
        zh(THE_KEY_OF_GOOGLE_AI, "Google AI Studio 的密钥（aistudio.google.com/apikey）");

        ja(OPTIONAL_EMPTY_USES_GOOGLES, "任意。空欄の場合はGoogleのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh(OPTIONAL_EMPTY_USES_GOOGLES, "可选。留空则使用 Google 的 API，设置后可通过代理访问。");

        ja(TURN_OFF_THE_SAFETY_FILTERS, "安全フィルターをオフにする");
        zh(TURN_OFF_THE_SAFETY_FILTERS, "关闭安全过滤器");

        ja(SENDS_BLOCK_NONE, "調整可能なカテゴリ（ハラスメント、ヘイトスピーチ、性的に露骨な内容、危険なコンテンツ）にBLOCK_NONEを送信します。送信内容に関わらず、Googleは一部のコンテンツを引き続きブロックします。");
        zh(SENDS_BLOCK_NONE, "对可调整的类别（骚扰、仇恨言论、色情内容、危险内容）发送 BLOCK_NONE。无论发送什么，Google 仍会拦截部分内容。");
    }
}
