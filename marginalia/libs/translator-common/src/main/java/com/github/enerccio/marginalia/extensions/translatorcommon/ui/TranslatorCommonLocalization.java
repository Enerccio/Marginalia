package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * The texts of the screens that the translator plugins share (the English text is the key). Each plugin embeds its own copy of this class, so the
 * texts are looked up in the language of the session like those of the plugin itself.
 */
public class TranslatorCommonLocalization extends PluginLocalizationBundle {

    public static final TranslatorCommonLocalization TC = new TranslatorCommonLocalization();

    public static final String ALSO_CALLED = "Also called";
    public static final String EDITED_AFTER_TRANSLATION = "The text was edited after the translation; this may not be what it says now.";
    public static final String EDIT_CHARACTER = "Edit the character";
    public static final String EVERYBODY_SPEAKS_IT_EXCEPT = "Everybody speaks it except the listed characters";
    public static final String GENDER = "Gender";
    public static final String HOW_THE_CHARACTER_SPEAKS_IT = "How the character speaks it: formal, clipped, coarse, learned...";
    public static final String LISTED = "Listed";
    public static final String LISTED_IN_THE_RULE_ABOVE = "Listed: counted by the rule above (an exception, or one of the only speakers)";
    public static final String NAME = "Name";
    public static final String NEW_CHARACTER = "New character";
    public static final String NOT_UNDERSTOOD = "(not understood by the listener)";
    public static final String ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART = "This is only the default: the text of the part overrides it (\"she said in the common tongue\", \"the guard could not understand a word\"). The notes help the translator keep a voice.";
    public static final String ONLY_THE_LISTED_SPEAK_IT = "Only the listed characters speak it";
    public static final String SAID_BY_S = "Said by %s";
    public static final String SEPARATED_BY_COMMAS = "Separated by commas";
    public static final String THE_NAME_IS_EMPTY = "The name is empty";
    public static final String VOICE = "Voice";
    public static final String YES_TEXT = "Yes";
    public static final String ADD = "Add";
    public static final String CANCEL = "Cancel";
    public static final String DELETE = "Delete";
    public static final String DELETE_S = "Delete %s?";
    public static final String EDIT = "Edit";
    public static final String MOVE_DOWN = "Move down";
    public static final String MOVE_UP = "Move up";
    public static final String OK = "OK";

    @Override
    protected void loadKeys() {
        ja(ALSO_CALLED, "別名");
        zh(ALSO_CALLED, "别名");

        ja(EDITED_AFTER_TRANSLATION, "翻訳後にテキストが編集されました。現在の内容と一致しない可能性があります。");
        zh(EDITED_AFTER_TRANSLATION, "翻译之后文本被编辑过,这里的原文可能与现在的内容不符。");

        ja(EDIT_CHARACTER, "人物を編集");
        zh(EDIT_CHARACTER, "编辑角色");

        ja(EVERYBODY_SPEAKS_IT_EXCEPT, "一覧の人物以外は全員が話す");
        zh(EVERYBODY_SPEAKS_IT_EXCEPT, "除列出的角色外,所有人都说");

        ja(GENDER, "性別");
        zh(GENDER, "性别");

        ja(HOW_THE_CHARACTER_SPEAKS_IT, "その人物の話し方: 丁寧、簡潔、粗野、学のある話し方など");
        zh(HOW_THE_CHARACTER_SPEAKS_IT, "该角色怎么说:正式、简短、粗鲁、有学识……");

        ja(LISTED, "一覧に含む");
        zh(LISTED, "已列出");

        ja(LISTED_IN_THE_RULE_ABOVE, "一覧に含める: 上の規則の対象(例外、または唯一の話者の一人)");
        zh(LISTED_IN_THE_RULE_ABOVE, "已列出:适用上面的规则(例外,或唯一的说话者之一)");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(NEW_CHARACTER, "新しい人物");
        zh(NEW_CHARACTER, "新建角色");

        ja(NOT_UNDERSTOOD, "(聞き手には理解できない)");
        zh(NOT_UNDERSTOOD, "(听者听不懂)");

        ja(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "これは既定にすぎません。パートの本文が優先されます(「彼女は共通語で言った」「衛兵は一言も理解できなかった」など)。メモは翻訳モデルが話し方を保つのに役立ちます。");
        zh(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "这只是默认设置:段落文本优先(“她用通用语说”“守卫一个字也听不懂”)。备注帮助翻译模型保持口吻。");

        ja(ONLY_THE_LISTED_SPEAK_IT, "一覧の人物だけが話す");
        zh(ONLY_THE_LISTED_SPEAK_IT, "只有列出的角色说");

        ja(SAID_BY_S, "話者:%s");
        zh(SAID_BY_S, "说话者:%s");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");

        ja(THE_NAME_IS_EMPTY, "名前が空です");
        zh(THE_NAME_IS_EMPTY, "名称为空");

        ja(VOICE, "話し方");
        zh(VOICE, "口吻");

        ja(YES_TEXT, "はい");
        zh(YES_TEXT, "是");
        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(DELETE_S, "%s を削除しますか?");
        zh(DELETE_S, "删除 %s?");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");

        ja(MOVE_DOWN, "下へ移動");
        zh(MOVE_DOWN, "下移");

        ja(MOVE_UP, "上へ移動");
        zh(MOVE_UP, "上移");

        ja(OK, "OK");
        zh(OK, "确定");
    }
}
