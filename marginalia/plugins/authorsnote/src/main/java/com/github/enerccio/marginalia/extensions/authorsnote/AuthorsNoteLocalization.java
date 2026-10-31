package com.github.enerccio.marginalia.extensions.authorsnote;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Author's Note in the languages besides English; the English text is the key.
 */
public class AuthorsNoteLocalization extends PluginLocalizationBundle {

    public static final AuthorsNoteLocalization T = new AuthorsNoteLocalization();

    public static final String AUTHORS_NOTE = "Author's Note";
    public static final String INSERT_INTO_PROMPT = "Insert into prompt";
    public static final String INSERTION_DEPTH = "Insertion depth";
    public static final String MESSAGES_AFTER_THE_NOTE_0 = "Messages after the note: 0 = after the instructions, 1 = right before them";
    public static final String ROLE = "Role";
    public static final String SYSTEM = "System";
    public static final String ASSISTANT = "Assistant";
    public static final String USER = "User";
    public static final String AUTHORS_NOTE_PRIVATE = "Author's Note (private)";
    public static final String FOR_YOU_ONLY_NEVER_SENT = "For you only, never sent to the model";
    public static final String INSERTED_INTO_THE_PROMPT = "Inserted into the prompt of every generation";
    public static final String TOKENS_TAKEN_FROM_THE_ROOM = "~%d tokens, taken from the room for the story";
    public static final String TOKENS_NOT_INSERTED = "~%d tokens, not inserted";

    @Override
    protected void loadKeys() {
        ja(AUTHORS_NOTE, "作者メモ");
        zh(AUTHORS_NOTE, "作者备注");

        ja(INSERT_INTO_PROMPT, "プロンプトに挿入");
        zh(INSERT_INTO_PROMPT, "插入提示词");

        ja(INSERTION_DEPTH, "挿入の深さ");
        zh(INSERTION_DEPTH, "插入深度");

        ja(MESSAGES_AFTER_THE_NOTE_0, "メモの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_NOTE_0, "备注之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");

        ja(ROLE, "ロール");
        zh(ROLE, "角色");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");

        ja(USER, "ユーザー");
        zh(USER, "用户");

        ja(AUTHORS_NOTE_PRIVATE, "作者メモ（非公開）");
        zh(AUTHORS_NOTE_PRIVATE, "作者备注（私密）");

        ja(FOR_YOU_ONLY_NEVER_SENT, "あなた専用で、モデルには送信されません");
        zh(FOR_YOU_ONLY_NEVER_SENT, "仅供您自己查看，不会发送给模型");

        ja(INSERTED_INTO_THE_PROMPT, "すべての生成のプロンプトに挿入されます");
        zh(INSERTED_INTO_THE_PROMPT, "会插入每次生成的提示词");

        ja(TOKENS_TAKEN_FROM_THE_ROOM, "約%dトークン、物語用の枠から差し引かれます");
        zh(TOKENS_TAKEN_FROM_THE_ROOM, "约 %d 个词元，从故事可用的空间中扣除");

        ja(TOKENS_NOT_INSERTED, "約%dトークン、挿入されません");
        zh(TOKENS_NOT_INSERTED, "约 %d 个词元，未插入");
    }
}
