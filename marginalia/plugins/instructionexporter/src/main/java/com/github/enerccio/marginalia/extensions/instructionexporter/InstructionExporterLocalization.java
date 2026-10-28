package com.github.enerccio.marginalia.extensions.instructionexporter;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Instruction Exporter in the languages besides English; the English text is the key.
 */
public class InstructionExporterLocalization extends PluginLocalizationBundle {

    public static final InstructionExporterLocalization T = new InstructionExporterLocalization();

    @Override
    protected void loadKeys() {
        ja("Export instructions", "指示をエクスポート");
        zh("Export instructions", "导出指令");

        ja("There is no active branch to export", "エクスポートするアクティブなブランチがありません");
        zh("There is no active branch to export", "没有可导出的活动分支");

        ja("There is nothing to export in the active branch", "アクティブなブランチにエクスポートする内容がありません");
        zh("There is nothing to export in the active branch", "活动分支中没有可导出的内容");

        ja("Copied to clipboard", "クリップボードにコピーしました");
        zh("Copied to clipboard", "已复制到剪贴板");

        ja("Failed to export: %s", "エクスポートに失敗しました: %s");
        zh("Failed to export: %s", "导出失败：%s");
    }
}
