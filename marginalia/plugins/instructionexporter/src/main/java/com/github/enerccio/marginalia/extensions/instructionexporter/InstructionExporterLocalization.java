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

        ja(THERE_IS_NO_ACTIVE_BRANCH, "エクスポートするアクティブなブランチがありません");
        zh(THERE_IS_NO_ACTIVE_BRANCH, "没有可导出的活动分支");

        ja(THERE_IS_NOTHING_TO_EXPORT, "アクティブなブランチにエクスポートする内容がありません");
        zh(THERE_IS_NOTHING_TO_EXPORT, "活动分支中没有可导出的内容");

        ja(COPIED_TO_CLIPBOARD, "クリップボードにコピーしました");
        zh(COPIED_TO_CLIPBOARD, "已复制到剪贴板");

        ja(FAILED_TO_EXPORT, "エクスポートに失敗しました: %s");
        zh(FAILED_TO_EXPORT, "导出失败：%s");
    }
}
