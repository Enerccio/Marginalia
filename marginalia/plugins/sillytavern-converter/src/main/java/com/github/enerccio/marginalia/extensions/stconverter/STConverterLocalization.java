package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Converter (formerly SillyTavern Converter) in the languages besides English; the English text is the key.
 */
public class STConverterLocalization extends PluginLocalizationBundle {

    public static final STConverterLocalization T = new STConverterLocalization();

    public static final String CONVERTER = "Converter";
    public static final String FROM = "From";
    public static final String TO = "To";
    public static final String ALL = "All";
    public static final String CONVERT_RANGE = "Convert range";
    public static final String ASK_THE_MODEL = "Ask the model for the turn details of the messages in the range";
    public static final String EDIT_THE_PROMPT = "Edit the prompt of the wand";
    public static final String ALIGN_TO_TURNS = "Align to turns";
    public static final String THE_RANGE_STARTS = "The range starts at the beginning of a turn and ends with a message of the character (the end of the range moves back to the last message of the character before it)";
    public static final String AUTO_MODE = "Auto mode";
    public static final String CONVERT_THE_WHOLE_RANGE = "Convert the whole range in batches of messages, one request after another";
    public static final String MESSAGES_PER_BATCH = "Messages per batch";
    public static final String CONVERT_IN_BATCHES = "Convert in batches";
    public static final String ASK_THE_MODEL_FOR_THE_TURN = "Ask the model for the turn details of the range, one batch of messages after another";
    public static final String CLICK_A_MESSAGE_TO_START = "Click a message to start a range, Shift+click to extend it.";
    public static final String TAP_A_MESSAGE_TO_START = "Tap a message to start a range, tap another to end it.";
    public static final String CONVERT_TO_TURN_DETAILS = "Convert to turn details";
    public static final String CHAT = "Chat";
    public static final String TURN_DETAILS = "Turn details";
    public static final String MESSAGE = "Message";
    public static final String MSGS = "Msgs";
    public static final String TURN = "Turn";
    public static final String DELETE_THE_TURN_DETAILS = "Delete the turn details";
    public static final String THE_NEW_TURN_FORM = "The new turn form of the story is not available";
    public static final String CLICK_TO_PUT_THE_TURN = "Click to put the turn details into the new turn form";
    public static final String SCENE_SETTING = "Scene setting";
    public static final String POV_CHARACTER = "POV character";
    public static final String PRESENT_CHARACTERS = "Present characters";
    public static final String INSTRUCTIONS = "Instructions";
    public static final String REMOVE_THE_CHAT_AND_THE_TURN = "Remove the chat and the turn details made of it from the book?";
    public static final String REMOVE_THE_CHAT = "Remove the chat";
    public static final String CHAT_LOWERCASE = "chat";
    public static final String MESSAGES_CHECKED = "%d messages, %d checked";
    public static final String MESSAGES_LOADED = "%d messages loaded";
    public static final String NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT = "Not a SillyTavern or LM Studio chat";
    public static final String THE_CHAT_HAS_NO_MESSAGES = "The chat has no messages";
    public static final String GO_TO_THE_NEXT_UNCHECKED = "Go to the next unchecked row";
    public static final String ALL_MESSAGES_ARE_CHECKED = "All messages are checked";
    public static final String THERE_ARE_NO_TURN_DETAILS = "There are no turn details";
    public static final String ALL_TURNS_ARE_CHECKED = "All turns are checked";
    public static final String NO_TURN_DETAILS_YET_CHOOSE = "No turn details yet: choose a range and use the wand.";
    public static final String TURNS_CHECKED_CLICK_ONE = "%d turns, %d checked. Click one to open it in the new turn form.";
    public static final String TURN_DETAILS_D = "Turn details (%d)";
    public static final String MESSAGES_ABOUT_TOKENS_CHAT = "%d messages: about %s tokens (chat %s + prompt %s)";
    public static final String REQUEST_ABOUT_TOKENS_IN_ALL = "%d request, about %s tokens in all";
    public static final String REQUESTS_ABOUT_TOKENS_IN_ALL = "%d requests, about %s tokens in all";
    public static final String LOAD_A_CHAT_FIRST = "Load a chat first";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String WAITING_FOR_THE_MODEL = "Waiting for the model...";
    public static final String CANT_CONVERT = "Can't convert: %s";
    public static final String BATCH = "Batch %d of %d: %s";
    public static final String BATCH_OF_TRY = "Batch %d of %d (try %d of %d): %s";
    public static final String WRITING_THE_TURN_DETAILS_SO = "Writing the turn details... %d so far";
    public static final String THE_MODEL_IS_THINKING = "The model is thinking...";
    public static final String CANT_GO = "Can't go on: %s";
    public static final String STOPPED_NOTHING_WAS_CHANGED = "Stopped, nothing was changed";
    public static final String THE_MODEL_DID_NOT_ANSWER = "The model did not answer.";
    public static final String TURNS_FOR_THE_MESSAGES = "%1$d turns for the messages %2$d-%3$d";
    public static final String EARLIER_ONES_REPLACED = " (%d earlier ones replaced)";
    public static final String CONVERT_THE_MESSAGES_IN_REQUESTS = "Convert the messages %d-%d in %d requests to the model, one after another? The earlier turn details of the range that you have not ticked are replaced.";
    public static final String TURNS_MADE_IN_REQUESTS = "%1$d turns made in %2$d requests";
    public static final String STOPPED_AFTER_OF_REQUESTS_TURNS = "Stopped after %d of %d requests (%d turns made)";
    public static final String THE_RANGE_IS_WHAT = ". The range is what is left.";
    public static final String PROMPT_OF_THE_WAND = "Prompt of the wand";
    public static final String VARIABLES_BACKGROUNDLORE_CHAT = "Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}";
    public static final String DEFAULT = "Default";
    public static final String CANCEL = "Cancel";
    public static final String SAVE = "Save";
    public static final String STOP = "Stop";
    public static final String THE_MODELS_ANSWER_DOES = "The model's answer does not match what was asked for: %s";
    public static final String THE_MODELS_ANSWER_HAS_NO = "The model's answer has no turn for the messages %d to %d";
    public static final String UPLOAD_A_CHAT = "Upload a chat";
    public static final String LOAD_A_BACKUP = "Load a backup";
    public static final String PICK_A_BOOK = "Pick a book";
    public static final String A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO = "A SillyTavern chat (.jsonl) or an LM Studio conversation (.conversation.json), without branches, to rewrite as a story with the model.";
    public static final String OR_THE_PARTS = "Or the parts of a book of yours, from a backup file or the book itself: their instructions, and the text to redraft.";
    public static final String ONLY_THE_INSTRUCTIONS = "Only the instructions and the text are kept with this book. Remove the source to load another.";
    public static final String PARTS_LOADED = "%d parts loaded";
    public static final String NOT_A_BACKUP = "Not a backup of a book";
    public static final String BOOK = "Book";
    public static final String LOAD = "Load";
    public static final String THE_PARTS_OF_THE_BRANCH = "The parts of the branch the book is on are loaded.";
    public static final String PARTS_CHECKED = "%d parts, %d checked";
    public static final String PARTS = "Parts";
    public static final String REMOVE_THE_SOURCE = "Remove the source";
    public static final String REMOVE_THE_SOURCE_AND_WHAT = "Remove the source and what you checked of it from the book?";
    public static final String THE_BOOK_HAS_NO_PARTS = "The book has no parts to load";
    public static final String THE_BACKUP_IS_DAMAGED = "The backup is damaged: the parts do not lead to the first part";
    public static final String THE_STORED_SOURCE = "The stored source of the book can't be read";
    public static final String THE_STORED_SOURCE_OF_THE_BOOK = "The stored source of the book is from a newer version of the plugin";

    @Override
    protected void loadKeys() {
        ja(CONVERTER, "コンバーター");
        zh(CONVERTER, "转换器");

        ja(FROM, "開始");
        zh(FROM, "起始");

        ja(TO, "終了");
        zh(TO, "结束");

        ja(ALL, "すべて");
        zh(ALL, "全部");

        ja(CONVERT_RANGE, "範囲を変換");
        zh(CONVERT_RANGE, "转换范围");

        ja(ASK_THE_MODEL, "範囲内のメッセージのターン詳細をモデルに依頼します");
        zh(ASK_THE_MODEL, "让模型为范围内的消息生成回合详情");

        ja(EDIT_THE_PROMPT, "ワンドのプロンプトを編集");
        zh(EDIT_THE_PROMPT, "编辑魔杖的提示词");

        ja(ALIGN_TO_TURNS, "ターンに揃える");
        zh(ALIGN_TO_TURNS, "对齐到回合");

        ja(THE_RANGE_STARTS, "範囲はターンの先頭から始まり、キャラクターのメッセージで終わります（範囲の終わりは、その手前にあるキャラクターの最後のメッセージまで戻ります）");
        zh(THE_RANGE_STARTS, "范围从回合的开头开始，以角色的消息结束（范围的结尾会回退到其之前角色的最后一条消息）");

        ja(AUTO_MODE, "自動モード");
        zh(AUTO_MODE, "自动模式");

        ja(CONVERT_THE_WHOLE_RANGE, "範囲全体をメッセージのバッチに分けて、リクエストを1件ずつ順に送って変換します");
        zh(CONVERT_THE_WHOLE_RANGE, "将整个范围分成若干批消息，逐个请求依次转换");

        ja(MESSAGES_PER_BATCH, "バッチあたりのメッセージ数");
        zh(MESSAGES_PER_BATCH, "每批消息数");

        ja(CONVERT_IN_BATCHES, "バッチで変換");
        zh(CONVERT_IN_BATCHES, "分批转换");

        ja(ASK_THE_MODEL_FOR_THE_TURN, "範囲のターン詳細をモデルに、メッセージのバッチごとに順に依頼します");
        zh(ASK_THE_MODEL_FOR_THE_TURN, "依次按每批消息让模型生成该范围的回合详情");

        ja(CLICK_A_MESSAGE_TO_START, "メッセージをクリックすると範囲を開始し、Shiftを押しながらクリックすると範囲を広げます。");
        zh(CLICK_A_MESSAGE_TO_START, "点击一条消息开始选择范围，按住 Shift 点击可扩展范围。");

        ja(TAP_A_MESSAGE_TO_START, "メッセージをタップすると範囲を開始し、別のメッセージをタップすると範囲を終了します。");
        zh(TAP_A_MESSAGE_TO_START, "点按一条消息开始选择范围，再点按另一条消息结束范围。");

        ja(CONVERT_TO_TURN_DETAILS, "ターン詳細に変換");
        zh(CONVERT_TO_TURN_DETAILS, "转换为回合详情");

        ja(CHAT, "チャット");
        zh(CHAT, "聊天");

        ja(TURN_DETAILS, "ターン詳細");
        zh(TURN_DETAILS, "回合详情");

        ja(MESSAGE, "メッセージ");
        zh(MESSAGE, "消息");

        ja(MSGS, "件数");
        zh(MSGS, "消息数");

        ja(TURN, "ターン");
        zh(TURN, "回合");

        ja(DELETE_THE_TURN_DETAILS, "ターン詳細を削除");
        zh(DELETE_THE_TURN_DETAILS, "删除回合详情");

        ja(THE_NEW_TURN_FORM, "ストーリーの新規ターンフォームは利用できません");
        zh(THE_NEW_TURN_FORM, "故事的新回合表单不可用");

        ja(CLICK_TO_PUT_THE_TURN, "クリックしてターン詳細を新規ターンフォームに入れます");
        zh(CLICK_TO_PUT_THE_TURN, "点击将回合详情放入新回合表单");

        ja(SCENE_SETTING, "シーン設定");
        zh(SCENE_SETTING, "场景设定");

        ja(POV_CHARACTER, "視点キャラクター");
        zh(POV_CHARACTER, "视角角色");

        ja(PRESENT_CHARACTERS, "登場キャラクター");
        zh(PRESENT_CHARACTERS, "在场角色");

        ja(INSTRUCTIONS, "指示");
        zh(INSTRUCTIONS, "指令");

        ja(REMOVE_THE_CHAT_AND_THE_TURN, "チャットと、そこから作成したターン詳細をブックから削除しますか？");
        zh(REMOVE_THE_CHAT_AND_THE_TURN, "要从书籍中移除该聊天记录及据此生成的回合详情吗？");

        ja(REMOVE_THE_CHAT, "チャットを削除");
        zh(REMOVE_THE_CHAT, "移除聊天记录");

        ja(CHAT_LOWERCASE, "チャット");
        zh(CHAT_LOWERCASE, "聊天");

        ja(MESSAGES_CHECKED, "%d件のメッセージ、%d件確認済み");
        zh(MESSAGES_CHECKED, "%d 条消息，已检查 %d 条");

        ja(MESSAGES_LOADED, "%d件のメッセージを読み込みました");
        zh(MESSAGES_LOADED, "已加载 %d 条消息");

        ja(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "SillyTavernまたはLM Studioのチャットではありません");
        zh(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "不是 SillyTavern 或 LM Studio 聊天记录");

        ja(THE_CHAT_HAS_NO_MESSAGES, "チャットにメッセージがありません");
        zh(THE_CHAT_HAS_NO_MESSAGES, "聊天记录中没有消息");

        ja(GO_TO_THE_NEXT_UNCHECKED, "次の未確認の行へ移動");
        zh(GO_TO_THE_NEXT_UNCHECKED, "跳到下一个未检查的行");

        ja(ALL_MESSAGES_ARE_CHECKED, "すべてのメッセージを確認済みです");
        zh(ALL_MESSAGES_ARE_CHECKED, "所有消息均已检查");

        ja(THERE_ARE_NO_TURN_DETAILS, "ターン詳細がありません");
        zh(THERE_ARE_NO_TURN_DETAILS, "没有回合详情");

        ja(ALL_TURNS_ARE_CHECKED, "すべてのターンを確認済みです");
        zh(ALL_TURNS_ARE_CHECKED, "所有回合均已检查");

        ja(NO_TURN_DETAILS_YET_CHOOSE, "ターン詳細はまだありません。範囲を選んでワンドを使ってください。");
        zh(NO_TURN_DETAILS_YET_CHOOSE, "还没有回合详情：请选择范围并使用魔杖。");

        ja(TURNS_CHECKED_CLICK_ONE, "%d個のターン、%d個確認済み。クリックすると新規ターンフォームで開きます。");
        zh(TURNS_CHECKED_CLICK_ONE, "%d 个回合，已检查 %d 个。点击其中一个可在新回合表单中打开。");

        ja(TURN_DETAILS_D, "ターン詳細（%d）");
        zh(TURN_DETAILS_D, "回合详情（%d）");

        ja(MESSAGES_ABOUT_TOKENS_CHAT, "%d件のメッセージ: 約%sトークン（チャット %s + プロンプト %s）");
        zh(MESSAGES_ABOUT_TOKENS_CHAT, "%d 条消息：约 %s 个词元（聊天 %s + 提示词 %s）");

        ja(REQUEST_ABOUT_TOKENS_IN_ALL, "%d件のリクエスト、合計約%sトークン");
        zh(REQUEST_ABOUT_TOKENS_IN_ALL, "%d 个请求，共约 %s 个词元");

        ja(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d件のリクエスト、合計約%sトークン");
        zh(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d 个请求，共约 %s 个词元");

        ja(LOAD_A_CHAT_FIRST, "先にチャットを読み込んでください");
        zh(LOAD_A_CHAT_FIRST, "请先加载聊天记录");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");

        ja(WAITING_FOR_THE_MODEL, "モデルの応答を待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");

        ja(CANT_CONVERT, "変換できません: %s");
        zh(CANT_CONVERT, "无法转换：%s");

        ja(BATCH, "バッチ %d/%d: %s");
        zh(BATCH, "第 %d/%d 批：%s");

        ja(BATCH_OF_TRY, "バッチ %d/%d（試行 %d/%d）: %s");
        zh(BATCH_OF_TRY, "第 %d/%d 批（第 %d/%d 次尝试）：%s");

        ja(WRITING_THE_TURN_DETAILS_SO, "ターン詳細を書き込み中... 現在%d件");
        zh(WRITING_THE_TURN_DETAILS_SO, "正在写入回合详情……目前 %d 个");

        ja(THE_MODEL_IS_THINKING, "モデルが思考中です...");
        zh(THE_MODEL_IS_THINKING, "模型正在思考……");

        ja(CANT_GO, "続行できません: %s");
        zh(CANT_GO, "无法继续：%s");

        ja(STOPPED_NOTHING_WAS_CHANGED, "停止しました。何も変更されていません");
        zh(STOPPED_NOTHING_WAS_CHANGED, "已停止，未做任何更改");

        ja(THE_MODEL_DID_NOT_ANSWER, "モデルから応答がありませんでした。");
        zh(THE_MODEL_DID_NOT_ANSWER, "模型没有回答。");

        ja(TURNS_FOR_THE_MESSAGES, "メッセージ%2$d〜%3$dに対する%1$d個のターン");
        zh(TURNS_FOR_THE_MESSAGES, "针对第 %2$d-%3$d 条消息生成了 %1$d 个回合");

        ja(EARLIER_ONES_REPLACED, "（以前の%d件を置き換えました）");
        zh(EARLIER_ONES_REPLACED, "（替换了之前的 %d 个）");

        ja(CONVERT_THE_MESSAGES_IN_REQUESTS, "メッセージ%d〜%dを%d件のリクエストでモデルに順番に送って変換しますか？範囲内の以前のターン詳細のうち、チェックを付けていないものは置き換えられます。");
        zh(CONVERT_THE_MESSAGES_IN_REQUESTS, "要将第 %d-%d 条消息分 %d 个请求依次发送给模型进行转换吗？该范围内之前未勾选的回合详情将被替换。");

        ja(TURNS_MADE_IN_REQUESTS, "%2$d件のリクエストで%1$d個のターンを作成しました");
        zh(TURNS_MADE_IN_REQUESTS, "通过 %2$d 个请求生成了 %1$d 个回合");

        ja(STOPPED_AFTER_OF_REQUESTS_TURNS, "%d/%d件のリクエストの後で停止しました（%d個のターンを作成）");
        zh(STOPPED_AFTER_OF_REQUESTS_TURNS, "在 %d/%d 个请求后停止（已生成 %d 个回合）");

        ja(THE_RANGE_IS_WHAT, "。範囲は残りの部分になっています。");
        zh(THE_RANGE_IS_WHAT, "。范围为剩余部分。");

        ja(PROMPT_OF_THE_WAND, "ワンドのプロンプト");
        zh(PROMPT_OF_THE_WAND, "魔杖的提示词");

        ja(VARIABLES_BACKGROUNDLORE_CHAT, "変数: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        zh(VARIABLES_BACKGROUNDLORE_CHAT, "变量：{{backgroundLore}}、{{chat}}、{{firstMessage}}、{{lastMessage}}、{{previousTurn}}");

        ja(DEFAULT, "デフォルト");
        zh(DEFAULT, "默认");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(THE_MODELS_ANSWER_DOES, "モデルの回答が依頼内容と一致しません: %s");
        zh(THE_MODELS_ANSWER_DOES, "模型的回答与要求不符：%s");

        ja(THE_MODELS_ANSWER_HAS_NO, "モデルの回答に、メッセージ%d〜%dのターンがありません");
        zh(THE_MODELS_ANSWER_HAS_NO, "模型的回答中没有第 %d 至 %d 条消息对应的回合");

        ja(UPLOAD_A_CHAT, "チャットをアップロード");
        zh(UPLOAD_A_CHAT, "上传聊天记录");

        ja(LOAD_A_BACKUP, "バックアップを読み込む");
        zh(LOAD_A_BACKUP, "载入备份");

        ja(PICK_A_BOOK, "ブックを選ぶ");
        zh(PICK_A_BOOK, "选择书籍");

        ja(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "SillyTavernのチャット（.jsonl）またはLM Studioの会話（.conversation.json）。分岐なし。モデルとともにストーリーとして書き直します。");
        zh(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "SillyTavern 聊天记录（.jsonl）或 LM Studio 对话（.conversation.json），不含分支，与模型一起改写为故事。");

        ja(OR_THE_PARTS, "または、バックアップファイルまたはブックそのものから、あなたのブックのパート（その指示と、書き直す本文）。");
        zh(OR_THE_PARTS, "或者你的某本书的各个部分，来自备份文件或书籍本身：它们的指令，以及要重写的文本。");

        ja(ONLY_THE_INSTRUCTIONS, "このブックには指示と本文だけが保存されます。別のものを読み込むには、まずソースを削除してください。");
        zh(ONLY_THE_INSTRUCTIONS, "本书只保存指令和文本。要载入其他来源，请先移除当前来源。");

        ja(PARTS_LOADED, "%d個のパートを読み込みました");
        zh(PARTS_LOADED, "已载入 %d 个部分");

        ja(NOT_A_BACKUP, "ブックのバックアップではありません");
        zh(NOT_A_BACKUP, "不是书籍备份");

        ja(BOOK, "ブック");
        zh(BOOK, "书籍");

        ja(LOAD, "読み込む");
        zh(LOAD, "载入");

        ja(THE_PARTS_OF_THE_BRANCH, "ブックが現在いるブランチのパートが読み込まれます。");
        zh(THE_PARTS_OF_THE_BRANCH, "将载入书籍当前所在分支的各个部分。");

        ja(PARTS_CHECKED, "%d個のパート、%d個チェック済み");
        zh(PARTS_CHECKED, "%d 个部分，已勾选 %d 个");

        ja(PARTS, "パート");
        zh(PARTS, "部分");

        ja(REMOVE_THE_SOURCE, "ソースを削除");
        zh(REMOVE_THE_SOURCE, "移除来源");

        ja(REMOVE_THE_SOURCE_AND_WHAT, "ソースと、そのチェック内容をこのブックから削除しますか？");
        zh(REMOVE_THE_SOURCE_AND_WHAT, "要从本书中移除该来源及其勾选记录吗？");

        ja(THE_BOOK_HAS_NO_PARTS, "読み込めるパートがありません");
        zh(THE_BOOK_HAS_NO_PARTS, "该书没有可载入的部分");

        ja(THE_BACKUP_IS_DAMAGED, "バックアップが壊れています: パートが最初のパートにつながっていません");
        zh(THE_BACKUP_IS_DAMAGED, "备份已损坏：各部分无法追溯到第一个部分");

        ja(THE_STORED_SOURCE, "保存されているブックのソースを読み取れません");
        zh(THE_STORED_SOURCE, "无法读取已保存的书籍来源");

        ja(THE_STORED_SOURCE_OF_THE_BOOK, "保存されているブックのソースは、より新しいバージョンのプラグインのものです");
        zh(THE_STORED_SOURCE_OF_THE_BOOK, "已保存的书籍来源来自更新版本的插件");
    }
}
