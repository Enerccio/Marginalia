package com.github.enerccio.marginalia.extensions.loregenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Lore Generator in the languages besides English; the English text is the key.
 */
public class LoreGeneratorLocalization extends PluginLocalizationBundle {

    public static final LoreGeneratorLocalization T = new LoreGeneratorLocalization();

    @Override
    protected void loadKeys() {
        ja("Lore Generator", "ロアジェネレーター");
        zh("Lore Generator", "世界书生成器");

        ja("Generate lore entry", "ロアエントリを生成");
        zh("Generate lore entry", "生成世界书条目");

        ja("Save prompt as", "プロンプトを名前を付けて保存");
        zh("Save prompt as", "提示词另存为");

        ja("Name", "名前");
        zh("Name", "名称");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Name can't be empty", "名前は空にできません");
        zh("Name can't be empty", "名称不能为空");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Generate lore entry: %s", "ロアエントリを生成: %s");
        zh("Generate lore entry: %s", "生成世界书条目：%s");

        ja("This lore entry already has content. Copying the result to it replaces the content and the note.", "このロアエントリには既に内容があります。結果をコピーすると、内容とメモが置き換えられます。");
        zh("This lore entry already has content. Copying the result to it replaces the content and the note.", "此世界书条目已有内容。复制结果将替换其内容和备注。");

        ja("All Lore", "すべてのロア");
        zh("All Lore", "所有世界书");

        ja("Current Lorebook", "現在のロアブック");
        zh("Current Lorebook", "当前世界书");

        ja("All entries", "すべてのエントリ");
        zh("All entries", "所有条目");

        ja("Only up to position (%d)", "位置（%d）まで");
        zh("Only up to position (%d)", "仅到位置（%d）");

        ja("Generation prompt", "生成プロンプト");
        zh("Generation prompt", "生成提示词");

        ja("Save as", "名前を付けて保存");
        zh("Save as", "另存为");

        ja("Delete", "削除");
        zh("Delete", "删除");

        ja("Prompt", "プロンプト");
        zh("Prompt", "提示词");

        ja("Run Generation", "生成を実行");
        zh("Run Generation", "运行生成");

        ja("Run Compaction", "圧縮を実行");
        zh("Run Compaction", "运行压缩");

        ja("Generation", "生成");
        zh("Generation", "生成");

        ja("Compaction", "圧縮");
        zh("Compaction", "压缩");

        ja("Exit", "終了");
        zh("Exit", "退出");

        ja("Copy to Lore Entry", "ロアエントリにコピー");
        zh("Copy to Lore Entry", "复制到世界书条目");

        ja("Prompt saved", "プロンプトを保存しました");
        zh("Prompt saved", "提示词已保存");

        ja("Prompt '%s' already exists, overwrite it?", "プロンプト '%s' は既に存在します。上書きしますか？");
        zh("Prompt '%s' already exists, overwrite it?", "提示词“%s”已存在，要覆盖吗？");

        ja("Delete prompt '%s'?", "プロンプト '%s' を削除しますか？");
        zh("Delete prompt '%s'?", "要删除提示词“%s”吗？");

        ja("The prompt is empty", "プロンプトが空です");
        zh("The prompt is empty", "提示词为空");

        ja("Choose a model", "モデルを選択してください");
        zh("Choose a model", "请选择模型");

        ja("Copied to the lore entry", "ロアエントリにコピーしました");
        zh("Copied to the lore entry", "已复制到世界书条目");

        ja("Attachments (images and files sent with the generation)", "添付ファイル（生成と一緒に送信される画像とファイル）");
        zh("Attachments (images and files sent with the generation)", "附件（随生成一起发送的图片和文件）");

        ja("attachment", "添付ファイル");
        zh("attachment", "附件");

        ja("unknown type", "不明な形式");
        zh("unknown type", "未知类型");

        ja("link", "リンク");
        zh("link", "链接");

        ja("%s (%s, %s)", "%s（%s、%s）");
        zh("%s (%s, %s)", "%s（%s，%s）");

        ja("%d kB", "%d kB");
        zh("%d kB", "%d kB");

        ja("Delete attachment", "添付ファイルを削除");
        zh("Delete attachment", "删除附件");

        ja("Thinking Process", "思考プロセス");
        zh("Thinking Process", "思考过程");

        ja("Stop", "停止");
        zh("Stop", "停止");

        ja("Compaction prompt", "圧縮プロンプト");
        zh("Compaction prompt", "压缩提示词");

        ja("Generating the text of a lore entry", "ロアエントリのテキストを生成しています");
        zh("Generating the text of a lore entry", "正在生成世界书条目的文本");

        ja("Compacting the text of a lore entry", "ロアエントリのテキストを圧縮しています");
        zh("Compacting the text of a lore entry", "正在压缩世界书条目的文本");
    }
}
