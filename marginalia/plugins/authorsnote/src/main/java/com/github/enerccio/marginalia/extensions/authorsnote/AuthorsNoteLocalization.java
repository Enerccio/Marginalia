package com.github.enerccio.marginalia.extensions.authorsnote;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Author's Note in the languages besides English; the English text is the key.
 */
public class AuthorsNoteLocalization extends PluginLocalizationBundle {

    public static final AuthorsNoteLocalization T = new AuthorsNoteLocalization();

    @Override
    protected void loadKeys() {
        ja("Author's Note", "作者メモ");
        zh("Author's Note", "作者备注");

        ja("Insert into prompt", "プロンプトに挿入");
        zh("Insert into prompt", "插入提示词");

        ja("Insertion depth", "挿入の深さ");
        zh("Insertion depth", "插入深度");

        ja("Messages after the note: 0 = after the instructions, 1 = right before them", "メモの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh("Messages after the note: 0 = after the instructions, 1 = right before them", "备注之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");

        ja("Role", "ロール");
        zh("Role", "角色");

        ja("System", "システム");
        zh("System", "系统");

        ja("Assistant", "アシスタント");
        zh("Assistant", "助手");

        ja("User", "ユーザー");
        zh("User", "用户");

        ja("Author's Note (private)", "作者メモ（非公開）");
        zh("Author's Note (private)", "作者备注（私密）");

        ja("For you only, never sent to the model", "あなた専用で、モデルには送信されません");
        zh("For you only, never sent to the model", "仅供您自己查看，不会发送给模型");

        ja("Inserted into the prompt of every generation", "すべての生成のプロンプトに挿入されます");
        zh("Inserted into the prompt of every generation", "会插入每次生成的提示词");

        ja("~%d tokens, taken from the room for the story", "約%dトークン、物語用の枠から差し引かれます");
        zh("~%d tokens, taken from the room for the story", "约 %d 个词元，从故事可用的空间中扣除");

        ja("~%d tokens, not inserted", "約%dトークン、挿入されません");
        zh("~%d tokens, not inserted", "约 %d 个词元，未插入");
    }
}
