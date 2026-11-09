package com.github.enerccio.marginalia.extensions.loregenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Lore Generator in the languages besides English; the English text is the key.
 */
public class LoreGeneratorLocalization extends PluginLocalizationBundle {

    public static final LoreGeneratorLocalization T = new LoreGeneratorLocalization();

    public static final String LORE_GENERATOR = "Lore Generator";
    public static final String GENERATE_LORE_ENTRY = "Generate lore entry";
    public static final String SAVE_PROMPT_AS = "Save prompt as";
    public static final String NAME = "Name";
    public static final String SAVE = "Save";
    public static final String NAME_CANT_BE_EMPTY = "Name can't be empty";
    public static final String CANCEL = "Cancel";
    public static final String GENERATE_LORE_ENTRY_S = "Generate lore entry: %s";
    public static final String THIS_LORE_ENTRY_ALREADY_HAS = "This lore entry already has content. Copying the result to it replaces the content and the note.";
    public static final String ALL_LORE = "All Lore";
    public static final String CURRENT_LOREBOOK = "Current Lorebook";
    public static final String ALL_ENTRIES = "All entries";
    public static final String ONLY_UP_TO_POSITION = "Only up to position (%d)";
    public static final String GENERATION_PROMPT = "Generation prompt";
    public static final String SAVE_AS = "Save as";
    public static final String DELETE = "Delete";
    public static final String PROMPT = "Prompt";
    public static final String RUN_GENERATION = "Run Generation";
    public static final String RUN_COMPACTION = "Run Compaction";
    public static final String GENERATION = "Generation";
    public static final String COMPACTION = "Compaction";
    public static final String EXIT = "Exit";
    public static final String COPY_TO_LORE_ENTRY = "Copy to Lore Entry";
    public static final String PROMPT_SAVED = "Prompt saved";
    public static final String PROMPT_ALREADY_EXISTS_OVERWRITE = "Prompt '%s' already exists, overwrite it?";
    public static final String DELETE_PROMPT = "Delete prompt '%s'?";
    public static final String THE_PROMPT_IS_EMPTY = "The prompt is empty";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String COPIED_TO_THE_LORE_ENTRY = "Copied to the lore entry";
    public static final String ATTACHMENTS_IMAGES_AND_FILES = "Attachments (images and files sent with the generation)";
    public static final String ATTACHMENT = "attachment";
    public static final String UNKNOWN_TYPE = "unknown type";
    public static final String LINK = "link";
    public static final String FORMAT = "%s (%s, %s)";
    public static final String KB = "%d kB";
    public static final String DELETE_ATTACHMENT = "Delete attachment";
    public static final String THINKING_PROCESS = "Thinking Process";
    public static final String STOP = "Stop";
    public static final String COMPACTION_PROMPT = "Compaction prompt";
    public static final String GENERATING_THE_TEXT = "Generating the text of a lore entry";
    public static final String COMPACTING_THE_TEXT = "Compacting the text of a lore entry";
    public static final String LORE_GENERATOR_GENERATE_LORE = "Lore Generator: generate lore";
    public static final String LORE_GENERATOR_COMPACT_LORE = "Lore Generator: compact lore";

    @Override
    protected void loadKeys() {
        ja(LORE_GENERATOR, "ロアジェネレーター");
        zh(LORE_GENERATOR, "世界书生成器");

        ja(GENERATE_LORE_ENTRY, "ロアエントリを生成");
        zh(GENERATE_LORE_ENTRY, "生成世界书条目");

        ja(SAVE_PROMPT_AS, "プロンプトを名前を付けて保存");
        zh(SAVE_PROMPT_AS, "提示词另存为");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(NAME_CANT_BE_EMPTY, "名前は空にできません");
        zh(NAME_CANT_BE_EMPTY, "名称不能为空");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(GENERATE_LORE_ENTRY_S, "ロアエントリを生成: %s");
        zh(GENERATE_LORE_ENTRY_S, "生成世界书条目：%s");

        ja(THIS_LORE_ENTRY_ALREADY_HAS, "このロアエントリには既に内容があります。結果をコピーすると、内容とメモが置き換えられます。");
        zh(THIS_LORE_ENTRY_ALREADY_HAS, "此世界书条目已有内容。复制结果将替换其内容和备注。");

        ja(ALL_LORE, "すべてのロア");
        zh(ALL_LORE, "所有世界书");

        ja(CURRENT_LOREBOOK, "現在のロアブック");
        zh(CURRENT_LOREBOOK, "当前世界书");

        ja(ALL_ENTRIES, "すべてのエントリ");
        zh(ALL_ENTRIES, "所有条目");

        ja(ONLY_UP_TO_POSITION, "位置（%d）まで");
        zh(ONLY_UP_TO_POSITION, "仅到位置（%d）");

        ja(GENERATION_PROMPT, "生成プロンプト");
        zh(GENERATION_PROMPT, "生成提示词");

        ja(SAVE_AS, "名前を付けて保存");
        zh(SAVE_AS, "另存为");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");

        ja(RUN_GENERATION, "生成を実行");
        zh(RUN_GENERATION, "运行生成");

        ja(RUN_COMPACTION, "圧縮を実行");
        zh(RUN_COMPACTION, "运行压缩");

        ja(GENERATION, "生成");
        zh(GENERATION, "生成");

        ja(COMPACTION, "圧縮");
        zh(COMPACTION, "压缩");

        ja(EXIT, "終了");
        zh(EXIT, "退出");

        ja(COPY_TO_LORE_ENTRY, "ロアエントリにコピー");
        zh(COPY_TO_LORE_ENTRY, "复制到世界书条目");

        ja(PROMPT_SAVED, "プロンプトを保存しました");
        zh(PROMPT_SAVED, "提示词已保存");

        ja(PROMPT_ALREADY_EXISTS_OVERWRITE, "プロンプト '%s' は既に存在します。上書きしますか？");
        zh(PROMPT_ALREADY_EXISTS_OVERWRITE, "提示词“%s”已存在，要覆盖吗？");

        ja(DELETE_PROMPT, "プロンプト '%s' を削除しますか？");
        zh(DELETE_PROMPT, "要删除提示词“%s”吗？");

        ja(THE_PROMPT_IS_EMPTY, "プロンプトが空です");
        zh(THE_PROMPT_IS_EMPTY, "提示词为空");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");

        ja(COPIED_TO_THE_LORE_ENTRY, "ロアエントリにコピーしました");
        zh(COPIED_TO_THE_LORE_ENTRY, "已复制到世界书条目");

        ja(ATTACHMENTS_IMAGES_AND_FILES, "添付ファイル（生成と一緒に送信される画像とファイル）");
        zh(ATTACHMENTS_IMAGES_AND_FILES, "附件（随生成一起发送的图片和文件）");

        ja(ATTACHMENT, "添付ファイル");
        zh(ATTACHMENT, "附件");

        ja(UNKNOWN_TYPE, "不明な形式");
        zh(UNKNOWN_TYPE, "未知类型");

        ja(LINK, "リンク");
        zh(LINK, "链接");

        ja(FORMAT, "%s（%s、%s）");
        zh(FORMAT, "%s（%s，%s）");

        ja(KB, "%d kB");
        zh(KB, "%d kB");

        ja(DELETE_ATTACHMENT, "添付ファイルを削除");
        zh(DELETE_ATTACHMENT, "删除附件");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(COMPACTION_PROMPT, "圧縮プロンプト");
        zh(COMPACTION_PROMPT, "压缩提示词");

        ja(GENERATING_THE_TEXT, "ロアエントリのテキストを生成しています");
        zh(GENERATING_THE_TEXT, "正在生成世界书条目的文本");

        ja(COMPACTING_THE_TEXT, "ロアエントリのテキストを圧縮しています");
        zh(COMPACTING_THE_TEXT, "正在压缩世界书条目的文本");

        ja(LORE_GENERATOR_GENERATE_LORE, "ロアジェネレーター: ロアを生成");
        zh(LORE_GENERATOR_GENERATE_LORE, "设定生成器：生成设定");

        ja(LORE_GENERATOR_COMPACT_LORE, "ロアジェネレーター: ロアを圧縮");
        zh(LORE_GENERATOR_COMPACT_LORE, "设定生成器：压缩设定");
    }
}
