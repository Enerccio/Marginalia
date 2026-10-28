package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Google Gemini provider in the languages besides English; the English text is the key.
 */
public class GoogleGenAiLocalization extends PluginLocalizationBundle {

    public static final GoogleGenAiLocalization T = new GoogleGenAiLocalization();

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

        ja("The key of Google AI Studio (aistudio.google.com/apikey)", "Google AI Studioのキー（aistudio.google.com/apikey）");
        zh("The key of Google AI Studio (aistudio.google.com/apikey)", "Google AI Studio 的密钥（aistudio.google.com/apikey）");

        ja("Optional. Empty uses Google's API, set it to go through a proxy.", "任意。空欄の場合はGoogleのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh("Optional. Empty uses Google's API, set it to go through a proxy.", "可选。留空则使用 Google 的 API，设置后可通过代理访问。");

        ja("Turn off the safety filters", "安全フィルターをオフにする");
        zh("Turn off the safety filters", "关闭安全过滤器");

        ja("Sends BLOCK_NONE for the adjustable categories (harassment, hate speech, sexually explicit, dangerous content). Google still blocks some content whatever is sent.", "調整可能なカテゴリ（ハラスメント、ヘイトスピーチ、性的に露骨な内容、危険なコンテンツ）にBLOCK_NONEを送信します。送信内容に関わらず、Googleは一部のコンテンツを引き続きブロックします。");
        zh("Sends BLOCK_NONE for the adjustable categories (harassment, hate speech, sexually explicit, dangerous content). Google still blocks some content whatever is sent.", "对可调整的类别（骚扰、仇恨言论、色情内容、危险内容）发送 BLOCK_NONE。无论发送什么，Google 仍会拦截部分内容。");
    }
}
