package com.github.enerccio.marginalia.extensions.bookdescription;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Book Description Generator in the languages besides English; the English text is the key.
 */
public class BookDescriptionLocalization extends PluginLocalizationBundle {

    public static final BookDescriptionLocalization T = new BookDescriptionLocalization();

    public static final String BOOK_DESCRIPTION_GENERATOR = "Book Description Generator";
    public static final String GENERATE_NAME_AND_DESCRIPTION = "Generate name and description";
    public static final String BOOK_DESCRIPTION_PROMPT = "Description prompt";
    public static final String BOOK_NOTES_PROMPT = "Notes prompt";
    public static final String THE_PROMPT_THAT_WRITES_THE_NAME = "Writes the name and the description of the book from the story. Variables: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}";
    public static final String THE_PROMPT_THAT_WRITES_NOTES = "Writes notes on one part of a story that is too long for the model; the notes are what the description prompt gets. Variables: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}";
    public static final String VARIABLES = "Variables: %s";
    public static final String FROM_MESSAGE = "From message";
    public static final String TO_MESSAGE = "To message";
    public static final String NO_SUCH_MESSAGE = "No such message";
    public static final String THE_MESSAGES_WITH_TEXT = "The book has %d messages with text";
    public static final String THE_MESSAGES_ARE_NUMBERED_1 = "The messages are numbered from 1 to %d";
    public static final String THE_FIRST_MESSAGE_IS_AFTER = "The first message is after the last one";
    public static final String USE_SUMMARIES = "Use summaries";
    public static final String A_SUMMARY_STANDS_IN = "A summary stands in for the messages it covers when all of them are in the range; a summary that does not describe the story any more is not used";
    public static final String ANSWER_FORMAT = "Answer format";
    public static final String AUTOMATIC = "Automatic";
    public static final String JSON = "JSON";
    public static final String JSON_WITHOUT_A_SCHEMA = "JSON without a schema";
    public static final String MARKDOWN = "Markdown";
    public static final String AUTOMATIC_ASKS_FOR_JSON = "Automatic asks for JSON in a schema when the model can follow one, Markdown otherwise";
    public static final String JSON_WITHOUT_A_SCHEMA_HELP = "JSON without a schema is for a server that can not take a schema together with the thinking of the model";
    public static final String THE_PROMPTS_ARE_SET_IN = "The prompts are inherited: set them in the Prompts tab of the book or of its folder, or in Settings → Extension Settings → Book Description Generator";
    public static final String THE_RANGE_HAS = "%d messages in the range, %d parts are sent (%d of them summaries)";
    public static final String TOKENS_SENT_AS_IT_IS = "About %s tokens with the prompt, the model takes %s: the story is sent as it is.";
    public static final String TOKENS_SENT_AS_NOTES = "About %s tokens with the prompt, the model takes %s: the story is too long, notes are made of it first (about %d requests).";
    public static final String TOKENS_CONTEXT_UNKNOWN = "About %s tokens with the prompt. The context size of the model is not known, the story is sent as it is.";
    public static final String THE_PROMPT_CANT_BE_BUILT = "The prompt can't be built: %s";
    public static final String GENERATE = "Generate";
    public static final String STOP = "Stop";
    public static final String CLOSE = "Close";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String THE_BOOK_HAS_NO_STORY = "The book has no story to describe";
    public static final String CANT_GENERATE = "Can't generate: %s";
    public static final String NOTES_ROUND = "Notes, round %d: the story is divided into %d parts";
    public static final String NOTES_PART_DONE = "Notes: part %d of %d done";
    public static final String WRITING_THE_DESCRIPTION = "Writing the name and the description...";
    public static final String WRITING_THE_DESCRIPTION_FROM_NOTES = "Writing the name and the description from the notes...";
    public static final String DONE = "Done";
    public static final String STOPPED = "Stopped";
    public static final String NOTES = "Notes";
    public static final String NOTES_OF_A_PART = "Part %d of %d";
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String COPY_TO_BOOK = "Copy to book";
    public static final String COPIED_TO_THE_BOOK = "Copied to the book";
    public static final String CANT_COPY_TO_THE_BOOK = "Can't copy to the book: %s";
    public static final String THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE = "The name and the description the model writes appear here";
    public static final String THE_ANSWER_CANT_BE_READ = "The answer has no name and description that could be read. This is what the model wrote:";
    public static final String BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION = "Book Description Generator: write the description";

    @Override
    protected void loadKeys() {
        ja(BOOK_DESCRIPTION_GENERATOR, "書籍説明ジェネレーター");
        zh(BOOK_DESCRIPTION_GENERATOR, "书籍简介生成器");

        ja(GENERATE_NAME_AND_DESCRIPTION, "名前と説明を生成");
        zh(GENERATE_NAME_AND_DESCRIPTION, "生成名称和简介");

        ja(BOOK_DESCRIPTION_PROMPT, "説明プロンプト");
        zh(BOOK_DESCRIPTION_PROMPT, "简介提示词");

        ja(BOOK_NOTES_PROMPT, "ノートプロンプト");
        zh(BOOK_NOTES_PROMPT, "笔记提示词");

        ja(THE_PROMPT_THAT_WRITES_THE_NAME, "物語から本の名前と説明を書きます。変数: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        zh(THE_PROMPT_THAT_WRITES_THE_NAME, "根据故事撰写书名和简介。变量：{{story}}、{{fromNotes}}、{{bookName}}、{{bookDescription}}");

        ja(THE_PROMPT_THAT_WRITES_NOTES, "モデルに長すぎる物語の一部についてノートを書きます。このノートが説明プロンプトに渡されます。変数: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        zh(THE_PROMPT_THAT_WRITES_NOTES, "为对模型来说过长的故事的某一部分撰写笔记；笔记会交给简介提示词。变量：{{story}}、{{part}}、{{parts}}、{{bookName}}、{{bookDescription}}");

        ja(VARIABLES, "変数: %s");
        zh(VARIABLES, "变量：%s");

        ja(FROM_MESSAGE, "開始メッセージ");
        zh(FROM_MESSAGE, "起始消息");

        ja(TO_MESSAGE, "終了メッセージ");
        zh(TO_MESSAGE, "结束消息");

        ja(NO_SUCH_MESSAGE, "そのメッセージはありません");
        zh(NO_SUCH_MESSAGE, "没有这条消息");

        ja(THE_MESSAGES_WITH_TEXT, "本にはテキストのあるメッセージが %d 件あります");
        zh(THE_MESSAGES_WITH_TEXT, "本书有 %d 条含文本的消息");

        ja(THE_MESSAGES_ARE_NUMBERED_1, "メッセージの番号は 1 から %d までです");
        zh(THE_MESSAGES_ARE_NUMBERED_1, "消息编号从 1 到 %d");

        ja(THE_FIRST_MESSAGE_IS_AFTER, "開始メッセージが終了メッセージより後ろにあります");
        zh(THE_FIRST_MESSAGE_IS_AFTER, "起始消息在结束消息之后");

        ja(USE_SUMMARIES, "要約を使う");
        zh(USE_SUMMARIES, "使用摘要");

        ja(A_SUMMARY_STANDS_IN, "要約が対象とするメッセージがすべて範囲内にある場合、要約がそれらのメッセージの代わりになります。物語と一致しなくなった要約は使われません");
        zh(A_SUMMARY_STANDS_IN, "当摘要所涵盖的消息全部在范围内时，摘要会代替这些消息；不再符合故事的摘要不会被使用");

        ja(ANSWER_FORMAT, "回答の形式");
        zh(ANSWER_FORMAT, "回答格式");

        ja(AUTOMATIC, "自動");
        zh(AUTOMATIC, "自动");

        ja(JSON, "JSON");
        zh(JSON, "JSON");

        ja(JSON_WITHOUT_A_SCHEMA, "スキーマなしの JSON");
        zh(JSON_WITHOUT_A_SCHEMA, "不带模式的 JSON");

        ja(MARKDOWN, "Markdown");
        zh(MARKDOWN, "Markdown");

        ja(AUTOMATIC_ASKS_FOR_JSON, "自動: モデルがスキーマに従える場合はスキーマ付きの JSON、それ以外は Markdown を要求します");
        zh(AUTOMATIC_ASKS_FOR_JSON, "自动：模型能遵循模式时要求带模式的 JSON，否则使用 Markdown");

        ja(JSON_WITHOUT_A_SCHEMA_HELP, "スキーマなしの JSON は、モデルの思考とスキーマを同時に受け付けないサーバー向けです");
        zh(JSON_WITHOUT_A_SCHEMA_HELP, "不带模式的 JSON 适用于无法同时接受模型思考和模式的服务器");

        ja(THE_PROMPTS_ARE_SET_IN, "プロンプトは継承されます。本またはフォルダーの「プロンプト」タブ、または 設定 → 拡張機能の設定 → 書籍説明ジェネレーター で設定してください");
        zh(THE_PROMPTS_ARE_SET_IN, "提示词是继承的：请在本书或其文件夹的“提示词”标签页，或在 设置 → 扩展设置 → 书籍简介生成器 中设置");

        ja(THE_RANGE_HAS, "範囲内のメッセージ: %d 件、送信する部分: %d（うち要約 %d）");
        zh(THE_RANGE_HAS, "范围内有 %d 条消息，发送 %d 个部分（其中 %d 个是摘要）");

        ja(TOKENS_SENT_AS_IT_IS, "プロンプトを含めて約 %s トークン、モデルが受け付けるのは %s: 物語はそのまま送信されます。");
        zh(TOKENS_SENT_AS_IT_IS, "包含提示词约 %s 个 token，模型可接受 %s：故事将原样发送。");

        ja(TOKENS_SENT_AS_NOTES, "プロンプトを含めて約 %s トークン、モデルが受け付けるのは %s: 物語が長すぎるため、先にノートを作成します（約 %d 回のリクエスト）。");
        zh(TOKENS_SENT_AS_NOTES, "包含提示词约 %s 个 token，模型可接受 %s：故事过长，将先生成笔记（约 %d 次请求）。");

        ja(TOKENS_CONTEXT_UNKNOWN, "プロンプトを含めて約 %s トークン。モデルのコンテキストサイズが不明なため、物語はそのまま送信されます。");
        zh(TOKENS_CONTEXT_UNKNOWN, "包含提示词约 %s 个 token。模型的上下文大小未知，故事将原样发送。");

        ja(THE_PROMPT_CANT_BE_BUILT, "プロンプトを作成できません: %s");
        zh(THE_PROMPT_CANT_BE_BUILT, "无法构建提示词：%s");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");

        ja(THE_BOOK_HAS_NO_STORY, "この本には説明する物語がありません");
        zh(THE_BOOK_HAS_NO_STORY, "这本书还没有可供描述的故事");

        ja(CANT_GENERATE, "生成できません: %s");
        zh(CANT_GENERATE, "无法生成：%s");

        ja(NOTES_ROUND, "ノート、ラウンド %d: 物語を %d 個の部分に分割しました");
        zh(NOTES_ROUND, "笔记，第 %d 轮：故事被分成 %d 个部分");

        ja(NOTES_PART_DONE, "ノート: %d / %d 個目が完了しました");
        zh(NOTES_PART_DONE, "笔记：第 %d / %d 部分已完成");

        ja(WRITING_THE_DESCRIPTION, "名前と説明を書いています...");
        zh(WRITING_THE_DESCRIPTION, "正在撰写名称和简介……");

        ja(WRITING_THE_DESCRIPTION_FROM_NOTES, "ノートから名前と説明を書いています...");
        zh(WRITING_THE_DESCRIPTION_FROM_NOTES, "正在根据笔记撰写名称和简介……");

        ja(DONE, "完了");
        zh(DONE, "完成");

        ja(STOPPED, "停止しました");
        zh(STOPPED, "已停止");

        ja(NOTES, "ノート");
        zh(NOTES, "笔记");

        ja(NOTES_OF_A_PART, "%d / %d 個目");
        zh(NOTES_OF_A_PART, "第 %d / %d 部分");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "简介");

        ja(COPY_TO_BOOK, "本にコピー");
        zh(COPY_TO_BOOK, "复制到书中");

        ja(COPIED_TO_THE_BOOK, "本にコピーしました");
        zh(COPIED_TO_THE_BOOK, "已复制到书中");

        ja(CANT_COPY_TO_THE_BOOK, "本にコピーできません: %s");
        zh(CANT_COPY_TO_THE_BOOK, "无法复制到书中：%s");

        ja(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "モデルが書いた名前と説明がここに表示されます");
        zh(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "模型撰写的名称和简介会显示在这里");

        ja(THE_ANSWER_CANT_BE_READ, "回答から名前と説明を読み取れませんでした。モデルの回答:");
        zh(THE_ANSWER_CANT_BE_READ, "无法从回答中读取名称和简介。模型的回答如下：");

        ja(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "ブック説明ジェネレーター: 説明を作成");
        zh(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "书籍简介生成器：撰写简介");
    }
}
