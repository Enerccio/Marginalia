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
        fr(AUTHORS_NOTE, "Note de l'auteur");
        es(AUTHORS_NOTE, "Nota del autor");
        de(AUTHORS_NOTE, "Autorennotiz");
        ko(AUTHORS_NOTE, "작가의 메모");
        it(AUTHORS_NOTE, "Nota dell'autore");

        ja(INSERT_INTO_PROMPT, "プロンプトに挿入");
        zh(INSERT_INTO_PROMPT, "插入提示词");
        fr(INSERT_INTO_PROMPT, "Insérer dans le prompt");
        es(INSERT_INTO_PROMPT, "Insertar en el prompt");
        de(INSERT_INTO_PROMPT, "In den Prompt einfügen");
        ko(INSERT_INTO_PROMPT, "프롬프트에 삽입");
        it(INSERT_INTO_PROMPT, "Inserisci nel prompt");

        ja(INSERTION_DEPTH, "挿入の深さ");
        zh(INSERTION_DEPTH, "插入深度");
        fr(INSERTION_DEPTH, "Profondeur d'insertion");
        es(INSERTION_DEPTH, "Profundidad de inserción");
        de(INSERTION_DEPTH, "Einfügetiefe");
        ko(INSERTION_DEPTH, "삽입 깊이");
        it(INSERTION_DEPTH, "Profondità di inserimento");

        ja(MESSAGES_AFTER_THE_NOTE_0, "メモの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_NOTE_0, "备注之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");
        fr(MESSAGES_AFTER_THE_NOTE_0, "Messages après la note : 0 = après les instructions, 1 = juste avant elles");
        es(MESSAGES_AFTER_THE_NOTE_0, "Mensajes después de la nota: 0 = después de las instrucciones, 1 = justo antes de ellas");
        de(MESSAGES_AFTER_THE_NOTE_0, "Nachrichten nach der Notiz: 0 = nach den Anweisungen, 1 = direkt davor");
        ko(MESSAGES_AFTER_THE_NOTE_0, "메모 뒤의 메시지 수: 0 = 지시문 뒤, 1 = 지시문 바로 앞");
        it(MESSAGES_AFTER_THE_NOTE_0, "Messaggi dopo la nota: 0 = dopo le istruzioni, 1 = subito prima di esse");

        ja(ROLE, "ロール");
        zh(ROLE, "角色");
        fr(ROLE, "Rôle");
        es(ROLE, "Rol");
        de(ROLE, "Rolle");
        ko(ROLE, "역할");
        it(ROLE, "Ruolo");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");
        fr(SYSTEM, "Système");
        es(SYSTEM, "Sistema");
        de(SYSTEM, "System");
        ko(SYSTEM, "시스템");
        it(SYSTEM, "Sistema");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");
        fr(ASSISTANT, "Assistant");
        es(ASSISTANT, "Asistente");
        de(ASSISTANT, "Assistent");
        ko(ASSISTANT, "어시스턴트");
        it(ASSISTANT, "Assistente");

        ja(USER, "ユーザー");
        zh(USER, "用户");
        fr(USER, "Utilisateur");
        es(USER, "Usuario");
        de(USER, "Benutzer");
        ko(USER, "사용자");
        it(USER, "Utente");

        ja(AUTHORS_NOTE_PRIVATE, "作者メモ（非公開）");
        zh(AUTHORS_NOTE_PRIVATE, "作者备注（私密）");
        fr(AUTHORS_NOTE_PRIVATE, "Note de l'auteur (privée)");
        es(AUTHORS_NOTE_PRIVATE, "Nota del autor (privada)");
        de(AUTHORS_NOTE_PRIVATE, "Autorennotiz (privat)");
        ko(AUTHORS_NOTE_PRIVATE, "작가의 메모 (비공개)");
        it(AUTHORS_NOTE_PRIVATE, "Nota dell'autore (privata)");

        ja(FOR_YOU_ONLY_NEVER_SENT, "あなた専用で、モデルには送信されません");
        zh(FOR_YOU_ONLY_NEVER_SENT, "仅供您自己查看，不会发送给模型");
        fr(FOR_YOU_ONLY_NEVER_SENT, "Pour vous seul, jamais envoyée au modèle");
        es(FOR_YOU_ONLY_NEVER_SENT, "Solo para ti, nunca se envía al modelo");
        de(FOR_YOU_ONLY_NEVER_SENT, "Nur für dich, wird nie an das Modell gesendet");
        ko(FOR_YOU_ONLY_NEVER_SENT, "나만 볼 수 있으며 모델에 전송되지 않습니다");
        it(FOR_YOU_ONLY_NEVER_SENT, "Solo per te, mai inviata al modello");

        ja(INSERTED_INTO_THE_PROMPT, "すべての生成のプロンプトに挿入されます");
        zh(INSERTED_INTO_THE_PROMPT, "会插入每次生成的提示词");
        fr(INSERTED_INTO_THE_PROMPT, "Insérée dans le prompt de chaque génération");
        es(INSERTED_INTO_THE_PROMPT, "Se inserta en el prompt de cada generación");
        de(INSERTED_INTO_THE_PROMPT, "Wird in den Prompt jeder Generierung eingefügt");
        ko(INSERTED_INTO_THE_PROMPT, "모든 생성의 프롬프트에 삽입됩니다");
        it(INSERTED_INTO_THE_PROMPT, "Inserita nel prompt di ogni generazione");

        ja(TOKENS_TAKEN_FROM_THE_ROOM, "約%dトークン、物語用の枠から差し引かれます");
        zh(TOKENS_TAKEN_FROM_THE_ROOM, "约 %d 个词元，从故事可用的空间中扣除");
        fr(TOKENS_TAKEN_FROM_THE_ROOM, "~%d jetons, pris sur la place réservée à l'histoire");
        es(TOKENS_TAKEN_FROM_THE_ROOM, "~%d tokens, tomados del espacio para la historia");
        de(TOKENS_TAKEN_FROM_THE_ROOM, "~%d Tokens, vom Platz für die Geschichte abgezogen");
        ko(TOKENS_TAKEN_FROM_THE_ROOM, "~%d 토큰, 이야기를 위한 공간에서 차지함");
        it(TOKENS_TAKEN_FROM_THE_ROOM, "~%d token, presi dallo spazio per la storia");

        ja(TOKENS_NOT_INSERTED, "約%dトークン、挿入されません");
        zh(TOKENS_NOT_INSERTED, "约 %d 个词元，未插入");
        fr(TOKENS_NOT_INSERTED, "~%d jetons, non insérée");
        es(TOKENS_NOT_INSERTED, "~%d tokens, no insertados");
        de(TOKENS_NOT_INSERTED, "~%d Tokens, nicht eingefügt");
        ko(TOKENS_NOT_INSERTED, "~%d 토큰, 삽입되지 않음");
        it(TOKENS_NOT_INSERTED, "~%d token, non inseriti");
    }
}
