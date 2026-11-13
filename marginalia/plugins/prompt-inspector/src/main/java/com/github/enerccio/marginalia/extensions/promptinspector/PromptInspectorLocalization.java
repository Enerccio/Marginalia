package com.github.enerccio.marginalia.extensions.promptinspector;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Prompt Inspector in the languages besides English; the English text is the key.
 */
public class PromptInspectorLocalization extends PluginLocalizationBundle {

    public static final PromptInspectorLocalization T = new PromptInspectorLocalization();

    public static final String INSPECT_PROMPT_BEFORE_SENDING = "Inspect prompt before sending";
    public static final String SEND_THIS_PROMPT_TO_THE_MODEL = "Send this prompt to the model?";
    public static final String SEND = "Send";
    public static final String CANCEL = "Cancel";
    public static final String ATTACHMENTS = "[%d attachment(s)]";
    public static final String FAILED_TO_SAVE_THE_SETTING = "Failed to save the setting: %s";

    @Override
    protected void loadKeys() {
        ja(INSPECT_PROMPT_BEFORE_SENDING, "送信前にプロンプトを確認");
        zh(INSPECT_PROMPT_BEFORE_SENDING, "发送前检查提示词");
        fr(INSPECT_PROMPT_BEFORE_SENDING, "Inspecter le prompt avant l'envoi");
        es(INSPECT_PROMPT_BEFORE_SENDING, "Inspeccionar el prompt antes de enviarlo");
        de(INSPECT_PROMPT_BEFORE_SENDING, "Prompt vor dem Senden prüfen");

        ja(SEND_THIS_PROMPT_TO_THE_MODEL, "このプロンプトをモデルに送信しますか？");
        zh(SEND_THIS_PROMPT_TO_THE_MODEL, "要将此提示词发送给模型吗？");
        fr(SEND_THIS_PROMPT_TO_THE_MODEL, "Envoyer ce prompt au modèle ?");
        es(SEND_THIS_PROMPT_TO_THE_MODEL, "¿Enviar este prompt al modelo?");
        de(SEND_THIS_PROMPT_TO_THE_MODEL, "Diesen Prompt an das Modell senden?");

        ja(SEND, "送信");
        zh(SEND, "发送");
        fr(SEND, "Envoyer");
        es(SEND, "Enviar");
        de(SEND, "Senden");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(ATTACHMENTS, "[添付 %d 件]");
        zh(ATTACHMENTS, "[%d 个附件]");
        fr(ATTACHMENTS, "[%d pièce(s) jointe(s)]");
        es(ATTACHMENTS, "[%d adjunto(s)]");
        de(ATTACHMENTS, "[%d Anhang/Anhänge]");

        ja(FAILED_TO_SAVE_THE_SETTING, "設定を保存できませんでした: %s");
        zh(FAILED_TO_SAVE_THE_SETTING, "无法保存设置：%s");
        fr(FAILED_TO_SAVE_THE_SETTING, "Échec de l'enregistrement du paramètre : %s");
        es(FAILED_TO_SAVE_THE_SETTING, "No se pudo guardar el ajuste: %s");
        de(FAILED_TO_SAVE_THE_SETTING, "Die Einstellung konnte nicht gespeichert werden: %s");
    }
}
