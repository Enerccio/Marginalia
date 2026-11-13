package com.github.enerccio.marginalia.extensions.instructionexporter;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Instruction Exporter in the languages besides English; the English text is the key.
 */
public class InstructionExporterLocalization extends PluginLocalizationBundle {

    public static final InstructionExporterLocalization T = new InstructionExporterLocalization();

    public static final String EXPORT_INSTRUCTIONS = "Export instructions";
    public static final String THERE_IS_NO_ACTIVE_BRANCH = "There is no active branch to export";
    public static final String THERE_IS_NOTHING_TO_EXPORT = "There is nothing to export in the active branch";
    public static final String COPIED_TO_CLIPBOARD = "Copied to clipboard";
    public static final String FAILED_TO_EXPORT = "Failed to export: %s";

    @Override
    protected void loadKeys() {
        ja(EXPORT_INSTRUCTIONS, "指示をエクスポート");
        zh(EXPORT_INSTRUCTIONS, "导出指令");
        fr(EXPORT_INSTRUCTIONS, "Exporter les instructions");
        es(EXPORT_INSTRUCTIONS, "Exportar instrucciones");
        de(EXPORT_INSTRUCTIONS, "Anweisungen exportieren");
        ko(EXPORT_INSTRUCTIONS, "지시문 내보내기");
        it(EXPORT_INSTRUCTIONS, "Esporta le istruzioni");

        ja(THERE_IS_NO_ACTIVE_BRANCH, "エクスポートするアクティブなブランチがありません");
        zh(THERE_IS_NO_ACTIVE_BRANCH, "没有可导出的活动分支");
        fr(THERE_IS_NO_ACTIVE_BRANCH, "Il n'y a aucune branche active à exporter");
        es(THERE_IS_NO_ACTIVE_BRANCH, "No hay ninguna rama activa que exportar");
        de(THERE_IS_NO_ACTIVE_BRANCH, "Es gibt keinen aktiven Zweig zum Exportieren");
        ko(THERE_IS_NO_ACTIVE_BRANCH, "내보낼 활성 분기가 없습니다");
        it(THERE_IS_NO_ACTIVE_BRANCH, "Non c'è un ramo attivo da esportare");

        ja(THERE_IS_NOTHING_TO_EXPORT, "アクティブなブランチにエクスポートする内容がありません");
        zh(THERE_IS_NOTHING_TO_EXPORT, "活动分支中没有可导出的内容");
        fr(THERE_IS_NOTHING_TO_EXPORT, "Il n'y a rien à exporter dans la branche active");
        es(THERE_IS_NOTHING_TO_EXPORT, "No hay nada que exportar en la rama activa");
        de(THERE_IS_NOTHING_TO_EXPORT, "Im aktiven Zweig gibt es nichts zu exportieren");
        ko(THERE_IS_NOTHING_TO_EXPORT, "활성 분기에 내보낼 것이 없습니다");
        it(THERE_IS_NOTHING_TO_EXPORT, "Non c'è nulla da esportare nel ramo attivo");

        ja(COPIED_TO_CLIPBOARD, "クリップボードにコピーしました");
        zh(COPIED_TO_CLIPBOARD, "已复制到剪贴板");
        fr(COPIED_TO_CLIPBOARD, "Copié dans le presse-papiers");
        es(COPIED_TO_CLIPBOARD, "Copiado al portapapeles");
        de(COPIED_TO_CLIPBOARD, "In die Zwischenablage kopiert");
        ko(COPIED_TO_CLIPBOARD, "클립보드에 복사했습니다");
        it(COPIED_TO_CLIPBOARD, "Copiato negli appunti");

        ja(FAILED_TO_EXPORT, "エクスポートに失敗しました: %s");
        zh(FAILED_TO_EXPORT, "导出失败：%s");
        fr(FAILED_TO_EXPORT, "Échec de l'export : %s");
        es(FAILED_TO_EXPORT, "No se pudo exportar: %s");
        de(FAILED_TO_EXPORT, "Export fehlgeschlagen: %s");
        ko(FAILED_TO_EXPORT, "내보내기에 실패했습니다: %s");
        it(FAILED_TO_EXPORT, "Esportazione non riuscita: %s");
    }
}
