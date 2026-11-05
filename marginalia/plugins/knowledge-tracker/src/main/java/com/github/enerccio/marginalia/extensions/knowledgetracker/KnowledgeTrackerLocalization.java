package com.github.enerccio.marginalia.extensions.knowledgetracker;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Knowledge Tracker in the languages besides English; the English text is the key.
 */
public class KnowledgeTrackerLocalization extends PluginLocalizationBundle {

    public static final KnowledgeTrackerLocalization T = new KnowledgeTrackerLocalization();

    public static final String KNOWLEDGE = "Knowledge";
    public static final String TRACK_A_CHARACTER = "Track a character";
    public static final String NEW_TRACKED_CHARACTER = "New tracked character";
    public static final String EDIT_TRACKED_CHARACTER = "Edit tracked character";
    public static final String NAME = "Name";
    public static final String ALIASES = "Aliases";
    public static final String OTHER_NAMES_THE_CHARACTER_IS_WRITTEN = "Other names the character is written as in the present characters field, separated by commas";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String EDIT = "Edit";
    public static final String DELETE = "Delete";
    public static final String COPY = "Copy";
    public static final String COPIED = "Copied";
    public static final String GIVE_THE_CHARACTER_A_NAME = "Give the character a name";
    public static final String THE_NAME_OR_AN_ALIAS_IS_USED_BY = "The name or an alias is already used by %s";
    public static final String DELETE_THE_TRACKED_CHARACTER_AND_ALL = "Delete the tracked character %s with everything it knows?";
    public static final String DELETE_THIS_ITEM_FOR_GOOD = "Delete this item for good? Retract keeps it as old knowledge.";
    public static final String NO_CHARACTER_IS_TRACKED_YET = "No character is tracked yet. Only the characters you add here are described to the model, and only while they are among the present characters of a part.";
    public static final String NOTHING_IS_KNOWN_YET = "Nothing is written yet. Add what the character knows and does not know.";
    public static final String ITEMS_ACTIVE_OLD = "%d active, %d old";
    public static final String SHOW_WHAT_THE_CHARACTER_KNOWS = "Show what the character knows";
    public static final String ADD_KNOWLEDGE = "Add knowledge";
    public static final String NEW_ITEM = "New item";
    public static final String EDIT_ITEM = "Edit item";
    public static final String KIND = "Kind";
    public static final String KNOWS = "Knows";
    public static final String DOES_NOT_KNOW = "Does not know";
    public static final String TEXT = "Text";
    public static final String WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW = "About anything: a person, a place, a secret, an event. One short sentence; write what it does not know explicitly.";
    public static final String WRITE_SOMETHING = "Write something";
    public static final String RETRACT = "Retract";
    public static final String NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD = "Not true any more: keep it as old knowledge and do not send it to the model";
    public static final String RESTORE = "Restore";
    public static final String MAKE_IT_ACTIVE_AGAIN = "Make it active again, from now on";
    public static final String OLD_KNOWLEDGE = "Old knowledge (%d)";
    public static final String HAS_MET = "Has met: %s";
    public static final String SETTINGS = "Settings";
    public static final String REFRESH = "Refresh";
    public static final String LET_THE_MODEL_READ_EARLIER_PARTS = "Let the model read earlier parts";
    public static final String WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART = "The model may read the part an item was learned in when it is unsure (hearsay that is now confirmed, for instance). Each read is one more round to the model.";
    public static final String MET_PEOPLE = "Has met";
    public static final String PEOPLE_THE_CHARACTER_HAS_MET = "People the character has met, separated by commas. Processing adds the ones it sees in the story; the ones you remove come back only if the story is read again.";
    public static final String IN_PROMPT = "In prompt";
    public static final String INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW = "Insert what the tracked characters of the scene know into the prompt of every generation";
    public static final String DEPTH = "Depth";
    public static final String MESSAGES_AFTER_THE_KNOWLEDGE = "Messages after the knowledge: 0 = after the instructions, 1 = right before them";
    public static final String ROLE = "Role";
    public static final String SYSTEM = "System";
    public static final String ASSISTANT = "Assistant";
    public static final String USER = "User";
    public static final String SAY_THAT_NOTHING_ELSE_IS_KNOWN = "Say that nothing else is known";
    public static final String THE_CHARACTER_HAS_NEVER_MET_OR_HEARD = "Add a line saying that the character has never met or heard of anyone else and knows nothing else of the story's secrets";
    public static final String SEPARATORS_OF_THIS_BOOK = "Separators of this book";
    public static final String MY_SEPARATORS = "My separators (all books)";
    public static final String SEPARATORS_HELP = "How the present characters field is split into names. One per line; empty = the book uses mine, mine empty = the built-in ones (, ; new line / | 、 ， ；). A word such as and splits between spaces, \\n is a new line.";
    public static final String PROCESSING = "Processing";
    public static final String PROCESSED_UP_TO_PART = "Processed up to part %d of %d";
    public static final String NOTHING_IS_PROCESSED_YET = "Nothing is processed yet (%d parts)";
    public static final String PROCESS_NOW = "Process now";
    public static final String THE_MODEL_IS_WRITING = "What the model is writing";
    public static final String REQUEST_N_CHANGES = "Request %d: %d change(s) proposed";
    public static final String TIDY_REQUEST_N_CHANGES = "Tidy request %d: %d change(s) proposed";
    public static final String THE_MODEL_SENT_NO_REASONING = "The model sent no reasoning";
    public static final String WHAT_THE_MODEL_ANSWERED = "What the model answered";
    public static final String LAST_RUN_WHAT_THE_MODEL_THOUGHT = "Last run: what the model thought";
    public static final String TIDY_AUTOMATICALLY = "Tidy automatically";
    public static final String AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES = "At the end of a run that applies its changes without asking, when enough parts were read since the last tidy";
    public static final String TIDY_AFTER_N_PARTS = "Tidy after this many parts";
    public static final String AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS = "A change you uncheck leaves its items as they are; an unchecked merged item is not added.";
    public static final String TIDY_PARTS_OF_CONTEXT = "Tidy: parts of context";
    public static final String THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS = "The last processed parts the tidy pass reads for context";
    public static final String TIDY_FROM_N_ITEMS = "Tidy: from this many items";
    public static final String A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED = "A character with fewer items that the model learned is not tidied";
    public static final String LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE = "Locked: the tidy pass leaves it alone. Click to unlock";
    public static final String LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE = "Lock it so that the tidy pass leaves it alone (the story can still contradict it)";
    public static final String TIDY_KNOWLEDGE = "Tidy knowledge";
    public static final String DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING = "The model drops or merges items that are not worth keeping; locked items and what you wrote are not touched";
    public static final String REVIEW_THE_TIDY = "Review the tidy";
    public static final String WHY = "Why: %s";
    public static final String NOTHING_TO_TIDY = "Tidy: nothing to drop or merge";
    public static final String TIDY_DONE = "Tidy: %d item(s) became old knowledge, %d merged item(s) added";
    public static final String WHAT_THE_MODEL_THOUGHT = "What the model thought";
    public static final String CHANGES_FROM_A_COMMAND_TO_REVIEW = "%d changes found by a command, review them";
    public static final String THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN = "The story changed while the changes were waiting; run the command again";
    public static final String PROCESS_NEXT_PART = "Process next part";
    public static final String READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED = "Read only the first part that is not processed yet, for one step at a time";
    public static final String READ_N_PARTS_IN_N_REQUESTS = "Read %d parts with the model in %d requests? The story text alone is about %d tokens, the instructions, the lorebook and the knowledge of the characters are sent with every request on top of it. You can cancel the series at any time; what was applied stays.";
    public static final String READ_THE_PARTS_THAT_ARE_NOT_PROCESSED = "Read the parts that are not processed yet and note who has met whom";
    public static final String START_POINT = "Start point";
    public static final String START_OF_THE_BOOK = "Start of the book";
    public static final String PARTS_BEFORE_THE_START_POINT = "Parts before it are never processed";
    public static final String PROCESSED_PARTS = "Processed parts (%d)";
    public static final String CHANGED_SINCE_PROCESSED = "Changed since processed";
    public static final String REPROCESS_FROM_HERE = "Reprocess from here";
    public static final String REPROCESS_FROM_PART = "Reprocess from part %d? What was learned from it and from the later parts is dropped and read again.";
    public static final String PROCESS_NEW_PARTS_AUTOMATICALLY = "Process new parts automatically";
    public static final String BEFORE_A_GENERATION_THE_NEW_PARTS = "Before a generation, process the parts written since the last time, so the prompt knows who has met whom";
    public static final String PART_N = "Part %d";
    public static final String ADDED_TO = "Added to %s";
    public static final String RETRACTED_FROM = "Retracted from %s";
    public static final String REVERTED = "Reverted";
    public static final String REVERT_THIS_CHANGE = "Revert this change";
    public static final String APPLIED_AUTOMATICALLY = "Applied automatically (%d)";
    public static final String THE_ITEM_HAS_BEEN_CHANGED_SINCE = "The item has been changed since, it was not touched";
    public static final String PROCESSED_N_PARTS = "Processed %d parts";
    public static final String NOTHING_NEW_TO_PROCESS = "Nothing new to process";
    public static final String THE_BOOK_HAS_NO_PARTS_YET = "The book has no parts yet";
    public static final String PROCESSING_THE_STORY = "Processing the story";
    public static final String PREPARING = "Preparing…";
    public static final String ASKING_THE_MODEL_RUN = "Asking the model, run %d";
    public static final String WAITING_FOR_YOUR_REVIEW = "%d changes are waiting for your review";
    public static final String APPLYING_THE_CHANGES = "Applying the changes";
    public static final String DONE_PARTS_ADDED_RETRACTED = "Done: %d parts read, %d added, %d retracted";
    public static final String IT_FAILED = "It failed: %s";
    public static final String STOPPED_PARTS_ADDED_RETRACTED = "Stopped: %d parts read, %d added, %d retracted";
    public static final String REVIEW_CHANGES = "Review changes";
    public static final String CLOSE = "Close";
    public static final String LOG = "Log";
    public static final String REVIEW_THE_CHANGES_RUN = "Review the changes (run %d)";
    public static final String APPLY = "Apply";
    public static final String CHECK_ALL = "Check all";
    public static final String UNCHECK_ALL = "Uncheck all";
    public static final String ADD = "Add";
    public static final String CHANGE = "Change";
    public static final String ITEM = "Item";
    public static final String EVIDENCE = "Evidence";
    public static final String IN_PART_N_QUOTE = "In part %d: \"%s\"";
    public static final String THE_QUOTE_IS_NOT_IN_THE_PART = "This quote is not in that part, check it before you accept";
    public static final String LEFT_OUT_N = "Left out (%d)";
    public static final String UNCHECKED_ADDITIONS_ARE_KEPT = "An addition you uncheck is kept as old knowledge; an unchecked retraction changes nothing.";
    public static final String THE_STORY_CHANGED_WHILE_YOU_REVIEWED = "The story changed while you reviewed the changes; process again";
    public static final String CHANGES_TO_REVIEW = "%d changes to review";
    public static final String READ_WHAT_THE_CHARACTERS_LEARN = "Read what the characters learn with the model";
    public static final String ASK_THE_MODEL_WHAT_EACH_TRACKED = "Ask the model what each tracked character learned in the parts and propose it as changes. Off: processing only notes who has met whom.";
    public static final String ASK_BEFORE_APPLYING = "Ask before applying";
    public static final String SHOW_WHAT_THE_MODEL_FOUND = "Show what the model found for review before anything is applied. Off: it is applied at once and logged.";
    public static final String PARTS_PER_RUN = "Parts per run";
    public static final String LOREBOOK_FOR_THE_MODEL = "Lorebook for the model";
    public static final String ENTRIES_THE_PARTS_ACTIVATE = "Entries the parts activate";
    public static final String THE_WHOLE_LOREBOOK = "The whole lorebook";
    public static final String NO_LOREBOOK = "No lorebook";
    public static final String MODEL_FOR_THE_EXTRACTION = "Model for the extraction";
    public static final String PROTOCOL_FOR_THE_EXTRACTION = "Protocol for the extraction";
    public static final String THE_MODEL_OF_THE_BOOK = "The model of the book";
    public static final String SHOW_PART_IN_THE_STORY = "Show part %d in the story";

    @Override
    protected void loadKeys() {
        ja(KNOWLEDGE, "知識");
        zh(KNOWLEDGE, "认知");

        ja(TRACK_A_CHARACTER, "キャラクターを追跡");
        zh(TRACK_A_CHARACTER, "追踪角色");

        ja(NEW_TRACKED_CHARACTER, "新しい追跡キャラクター");
        zh(NEW_TRACKED_CHARACTER, "新的被追踪角色");

        ja(EDIT_TRACKED_CHARACTER, "追跡キャラクターを編集");
        zh(EDIT_TRACKED_CHARACTER, "编辑被追踪的角色");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(ALIASES, "別名");
        zh(ALIASES, "别名");

        ja(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "「登場人物」欄でこのキャラクターが書かれる他の名前（カンマ区切り）");
        zh(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "该角色在“在场角色”栏中的其他写法，用逗号分隔");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(COPY, "コピー");
        zh(COPY, "复制");

        ja(COPIED, "コピーしました");
        zh(COPIED, "已复制");

        ja(GIVE_THE_CHARACTER_A_NAME, "キャラクターに名前を付けてください");
        zh(GIVE_THE_CHARACTER_A_NAME, "请给角色起个名字");

        ja(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名前または別名は「%s」で既に使われています");
        zh(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名称或别名已被“%s”使用");

        ja(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "追跡中のキャラクター「%s」と、その知識をすべて削除しますか？");
        zh(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "要删除被追踪的角色“%s”及其所有已知信息吗？");

        ja(DELETE_THIS_ITEM_FOR_GOOD, "この項目を完全に削除しますか？「撤回」なら古い知識として残ります。");
        zh(DELETE_THIS_ITEM_FOR_GOOD, "要永久删除此条目吗？“撤回”会将其保留为旧认知。");

        ja(NO_CHARACTER_IS_TRACKED_YET, "追跡中のキャラクターはまだありません。ここで追加したキャラクターだけが、パートの登場人物に含まれている間だけモデルに説明されます。");
        zh(NO_CHARACTER_IS_TRACKED_YET, "尚未追踪任何角色。只有在此添加的角色，并且处于某一部分的在场角色之中时，才会向模型说明。");

        ja(NOTHING_IS_KNOWN_YET, "まだ何も書かれていません。このキャラクターが知っていること、知らないことを追加してください。");
        zh(NOTHING_IS_KNOWN_YET, "尚无内容。请添加该角色知道和不知道的事情。");

        ja(ITEMS_ACTIVE_OLD, "有効 %d 件、古い %d 件");
        zh(ITEMS_ACTIVE_OLD, "有效 %d 条，旧的 %d 条");

        ja(SHOW_WHAT_THE_CHARACTER_KNOWS, "キャラクターの知識を表示");
        zh(SHOW_WHAT_THE_CHARACTER_KNOWS, "显示该角色的认知");

        ja(ADD_KNOWLEDGE, "知識を追加");
        zh(ADD_KNOWLEDGE, "添加认知");

        ja(NEW_ITEM, "新しい項目");
        zh(NEW_ITEM, "新条目");

        ja(EDIT_ITEM, "項目を編集");
        zh(EDIT_ITEM, "编辑条目");

        ja(KIND, "種類");
        zh(KIND, "类型");

        ja(KNOWS, "知っている");
        zh(KNOWS, "知道");

        ja(DOES_NOT_KNOW, "知らない");
        zh(DOES_NOT_KNOW, "不知道");

        ja(TEXT, "内容");
        zh(TEXT, "内容");

        ja(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "人物、場所、秘密、出来事など何についてでも構いません。短い一文で、知らないことははっきり書いてください。");
        zh(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "可以是任何事：人物、地点、秘密、事件。一句简短的话；不知道的事请明确写出。");

        ja(WRITE_SOMETHING, "内容を入力してください");
        zh(WRITE_SOMETHING, "请输入内容");

        ja(RETRACT, "撤回");
        zh(RETRACT, "撤回");

        ja(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "もう正しくない：古い知識として残し、モデルには送りません");
        zh(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "已不再成立：保留为旧认知，不发送给模型");

        ja(RESTORE, "復元");
        zh(RESTORE, "恢复");

        ja(MAKE_IT_ACTIVE_AGAIN, "今後はまた有効にします");
        zh(MAKE_IT_ACTIVE_AGAIN, "从现在起重新生效");

        ja(OLD_KNOWLEDGE, "古い知識（%d）");
        zh(OLD_KNOWLEDGE, "旧认知（%d）");

        ja(HAS_MET, "会ったことがある：%s");
        zh(HAS_MET, "见过：%s");

        ja(SETTINGS, "設定");
        zh(SETTINGS, "设置");

        ja(REFRESH, "更新");
        zh(REFRESH, "刷新");

        ja(LET_THE_MODEL_READ_EARLIER_PARTS, "モデルが以前のパートを読めるようにする");
        zh(LET_THE_MODEL_READ_EARLIER_PARTS, "允许模型阅读之前的部分");

        ja(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "モデルは、確信が持てないとき（たとえば噂で聞いたことが確認された場合）、項目を得たパートを読み返せます。読み返すたびにモデルへのやり取りが1回増えます。");
        zh(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "模型不确定时（例如传闻现在得到证实），可以阅读某条内容得知的那一部分。每次阅读都会让模型多一轮请求。");

        ja(MET_PEOPLE, "会ったことがある人");
        zh(MET_PEOPLE, "见过的人");

        ja(PEOPLE_THE_CHARACTER_HAS_MET, "キャラクターが会ったことのある人をコンマで区切って入力します。処理はストーリーで見つけた人を追加します。削除した人は、ストーリーを再度読み込んだ場合にのみ戻ります。");
        zh(PEOPLE_THE_CHARACTER_HAS_MET, "角色见过的人，用逗号分隔。处理时会加入故事中出现的人；删除的人只有在重新读取故事时才会再出现。");

        ja(IN_PROMPT, "プロンプトに入れる");
        zh(IN_PROMPT, "加入提示词");

        ja(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "場面にいる追跡キャラクターの知識を、すべての生成のプロンプトに挿入します");
        zh(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "将场景中被追踪角色的认知插入每次生成的提示词");

        ja(DEPTH, "深さ");
        zh(DEPTH, "深度");

        ja(MESSAGES_AFTER_THE_KNOWLEDGE, "知識の後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_KNOWLEDGE, "认知之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");

        ja(ROLE, "ロール");
        zh(ROLE, "角色");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");

        ja(USER, "ユーザー");
        zh(USER, "用户");

        ja(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "それ以外は知らないと明記する");
        zh(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "注明不知道其他任何事");

        ja(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "キャラクターは他の誰にも会ったことも聞いたこともなく、物語の秘密や計画や出来事を他には知らない、という一行を加えます");
        zh(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "加一行说明：该角色没有见过也没有听说过其他任何人，也不知道故事中的其他秘密、计划或事件");

        ja(SEPARATORS_OF_THIS_BOOK, "この本の区切り文字");
        zh(SEPARATORS_OF_THIS_BOOK, "本书的分隔符");

        ja(MY_SEPARATORS, "自分の区切り文字（すべての本）");
        zh(MY_SEPARATORS, "我的分隔符（所有书）");

        ja(SEPARATORS_HELP, "「登場人物」欄を名前に分ける方法。1行に1つ。空 = 自分の設定を使用、自分の設定も空 = 組み込み（, ; 改行 / | 、 ， ；）。and のような単語は前後の空白で区切り、\\n は改行です。");
        zh(SEPARATORS_HELP, "如何把“在场角色”栏拆分成名字。每行一个；留空 = 使用我的设置，我的也留空 = 使用内置的（, ; 换行 / | 、 ， ；）。像 and 这样的单词按前后空格拆分，\\n 表示换行。");

        ja(PROCESSING, "処理");
        zh(PROCESSING, "处理");

        ja(PROCESSED_UP_TO_PART, "%d / %d パートまで処理済み");
        zh(PROCESSED_UP_TO_PART, "已处理到第 %d 部分（共 %d 部分）");

        ja(NOTHING_IS_PROCESSED_YET, "まだ何も処理されていません（%d パート）");
        zh(NOTHING_IS_PROCESSED_YET, "尚未处理任何内容（共 %d 部分）");

        ja(PROCESS_NOW, "今すぐ処理");
        zh(PROCESS_NOW, "立即处理");

        ja(READ_N_PARTS_IN_N_REQUESTS, "%d 個のパートをモデルで %d 回のリクエストに分けて読みますか？物語の本文だけで約 %d トークンです。指示、ロアブック、キャラクターの知識が毎回のリクエストにさらに付きます。シリーズはいつでもキャンセルでき、適用済みの内容は残ります。");
        zh(READ_N_PARTS_IN_N_REQUESTS, "用模型分 %d 个部分、%d 次请求读取吗？仅故事正文就约 %d 个 token，每次请求还会附带指令、设定集和角色的知识。可随时取消该系列；已应用的内容会保留。");
        ja(ADDED_TO, "%s に追加");
        zh(ADDED_TO, "添加给 %s");
        ja(RETRACTED_FROM, "%s から撤回");
        zh(RETRACTED_FROM, "从 %s 撤回");
        ja(REVERTED, "元に戻しました");
        zh(REVERTED, "已撤销");
        ja(REVERT_THIS_CHANGE, "この変更を元に戻す");
        zh(REVERT_THIS_CHANGE, "撤销此更改");
        ja(APPLIED_AUTOMATICALLY, "自動で適用された変更 (%d)");
        zh(APPLIED_AUTOMATICALLY, "自动应用的更改 (%d)");
        ja(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "項目はその後変更されたため、そのままにしました");
        zh(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "该项此后已被更改，未做任何改动");
        ja(PROCESS_NEXT_PART, "次のパートを処理");
        zh(PROCESS_NEXT_PART, "处理下一部分");
        ja(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "まだ処理されていない最初のパートだけを読みます。一度に一歩ずつ進めたいときに使います");
        zh(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "只读取第一个尚未处理的部分，便于一步一步来");
        ja(CHANGES_FROM_A_COMMAND_TO_REVIEW, "コマンドが見つけた変更 %d 件を確認する");
        zh(CHANGES_FROM_A_COMMAND_TO_REVIEW, "命令找到了 %d 项更改，请查看");
        ja(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "変更の待機中に物語が変わりました。コマンドをもう一度実行してください");
        zh(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "等待期间故事已更改，请重新运行命令");
        ja(WHAT_THE_MODEL_THOUGHT, "モデルの考え");
        zh(WHAT_THE_MODEL_THOUGHT, "模型的思考");
        ja(TIDY_PARTS_OF_CONTEXT, "整理：文脈のパート数");
        zh(TIDY_PARTS_OF_CONTEXT, "整理：上下文部分数");
        ja(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "整理の際に文脈として読む、最後に処理されたパートの数");
        zh(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "整理时作为上下文读取的最后已处理部分数");
        ja(TIDY_FROM_N_ITEMS, "整理：この数以上の項目から");
        zh(TIDY_FROM_N_ITEMS, "整理：至少这么多项时");
        ja(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "モデルが学習した項目がこれより少ないキャラクターは整理しません");
        zh(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "模型学到的项数少于此值的角色不会被整理");
        ja(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "ロック中：整理の対象外です。クリックで解除");
        zh(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "已锁定：整理时不会处理。点击解锁");
        ja(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "整理の対象外にするためロックします（物語が矛盾すれば撤回はされます）");
        zh(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "锁定后整理时不会处理（故事若与之矛盾仍会撤回）");
        ja(TIDY_KNOWLEDGE, "知識を整理");
        zh(TIDY_KNOWLEDGE, "整理知识");
        ja(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "残す価値のない項目をモデルが削除または統合します。ロックした項目と自分で書いた項目は対象外です");
        zh(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "模型会删除或合并不值得保留的项；已锁定的项和你写的项不会被处理");
        ja(REVIEW_THE_TIDY, "整理の確認");
        zh(REVIEW_THE_TIDY, "查看整理结果");
        ja(WHY, "理由：%s");
        zh(WHY, "原因：%s");
        ja(NOTHING_TO_TIDY, "整理：削除や統合するものはありません");
        zh(NOTHING_TO_TIDY, "整理：没有要删除或合并的内容");
        ja(TIDY_DONE, "整理：%d 件が古い知識になり、統合した項目が %d 件追加されました");
        zh(TIDY_DONE, "整理：%d 项变为旧知识，新增 %d 项合并项");
        ja(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "チェックを外した変更では項目はそのままです。チェックを外した統合項目は追加されません。");
        zh(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "取消勾选的更改会保持原有项不变；取消勾选的合并项不会被添加。");
        ja(TIDY_AUTOMATICALLY, "自動で整理する");
        zh(TIDY_AUTOMATICALLY, "自动整理");
        ja(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "確認なしで変更を適用する実行の最後に、前回の整理から十分なパートを読んだ場合に整理します");
        zh(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "在不经确认就应用更改的运行结束时，若自上次整理后读取了足够多的部分，则自动整理");
        ja(TIDY_AFTER_N_PARTS, "整理するまでのパート数");
        zh(TIDY_AFTER_N_PARTS, "每隔多少部分整理一次");
        ja(REQUEST_N_CHANGES, "リクエスト %d：提案された変更 %d 件");
        zh(REQUEST_N_CHANGES, "请求 %d：提出 %d 项更改");
        ja(TIDY_REQUEST_N_CHANGES, "整理リクエスト %d：提案された変更 %d 件");
        zh(TIDY_REQUEST_N_CHANGES, "整理请求 %d：提出 %d 项更改");
        ja(THE_MODEL_SENT_NO_REASONING, "モデルは推論を送っていません");
        zh(THE_MODEL_SENT_NO_REASONING, "模型没有发送推理内容");
        ja(WHAT_THE_MODEL_ANSWERED, "モデルの回答");
        zh(WHAT_THE_MODEL_ANSWERED, "模型的回答");
        ja(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "前回の実行：モデルの考え");
        zh(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "上次运行：模型的思考");
        ja(THE_MODEL_IS_WRITING, "モデルが書いている内容");
        zh(THE_MODEL_IS_WRITING, "模型正在写的内容");
        ja(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "まだ処理されていないパートを読み、誰が誰に会ったかを記録します");
        zh(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "读取尚未处理的部分，并记录谁见过谁");

        ja(START_POINT, "開始位置");
        zh(START_POINT, "起点");

        ja(START_OF_THE_BOOK, "本の最初");
        zh(START_OF_THE_BOOK, "书的开头");

        ja(PARTS_BEFORE_THE_START_POINT, "これより前のパートは処理されません");
        zh(PARTS_BEFORE_THE_START_POINT, "此前的部分永远不会被处理");

        ja(PROCESSED_PARTS, "処理済みのパート（%d）");
        zh(PROCESSED_PARTS, "已处理的部分（%d）");

        ja(CHANGED_SINCE_PROCESSED, "処理後に変更されました");
        zh(CHANGED_SINCE_PROCESSED, "处理后已更改");

        ja(REPROCESS_FROM_HERE, "ここから再処理");
        zh(REPROCESS_FROM_HERE, "从此处重新处理");

        ja(REPROCESS_FROM_PART, "パート %d から再処理しますか？そのパートと以降のパートから得られた内容は破棄され、読み直されます。");
        zh(REPROCESS_FROM_PART, "要从第 %d 部分重新处理吗？由该部分及之后部分得到的内容会被丢弃并重新读取。");

        ja(PROCESS_NEW_PARTS_AUTOMATICALLY, "新しいパートを自動で処理");
        zh(PROCESS_NEW_PARTS_AUTOMATICALLY, "自动处理新的部分");

        ja(BEFORE_A_GENERATION_THE_NEW_PARTS, "生成の前に、前回以降に書かれたパートを処理し、プロンプトが誰が誰に会ったかを把握できるようにします");
        zh(BEFORE_A_GENERATION_THE_NEW_PARTS, "生成之前先处理上次以来写下的部分，使提示词知道谁见过谁");

        ja(PART_N, "パート %d");
        zh(PART_N, "第 %d 部分");

        ja(PROCESSED_N_PARTS, "%d パートを処理しました");
        zh(PROCESSED_N_PARTS, "已处理 %d 个部分");

        ja(NOTHING_NEW_TO_PROCESS, "処理するものはありません");
        zh(NOTHING_NEW_TO_PROCESS, "没有需要处理的内容");

        ja(THE_BOOK_HAS_NO_PARTS_YET, "この本にはまだパートがありません");
        zh(THE_BOOK_HAS_NO_PARTS_YET, "这本书还没有任何部分");

        ja(PROCESSING_THE_STORY, "物語を処理中");
        zh(PROCESSING_THE_STORY, "正在处理故事");

        ja(PREPARING, "準備中…");
        zh(PREPARING, "准备中…");

        ja(ASKING_THE_MODEL_RUN, "モデルに質問中、%d 回目");
        zh(ASKING_THE_MODEL_RUN, "正在询问模型，第 %d 轮");

        ja(WAITING_FOR_YOUR_REVIEW, "%d 件の変更が確認を待っています");
        zh(WAITING_FOR_YOUR_REVIEW, "有 %d 项更改等待您审阅");

        ja(APPLYING_THE_CHANGES, "変更を適用中");
        zh(APPLYING_THE_CHANGES, "正在应用更改");

        ja(DONE_PARTS_ADDED_RETRACTED, "完了：%d パートを読み、%d 件追加、%d 件撤回");
        zh(DONE_PARTS_ADDED_RETRACTED, "完成：读取 %d 个部分，添加 %d 条，撤回 %d 条");

        ja(IT_FAILED, "失敗しました：%s");
        zh(IT_FAILED, "失败：%s");

        ja(STOPPED_PARTS_ADDED_RETRACTED, "停止：%d パートを読み、%d 件追加、%d 件撤回");
        zh(STOPPED_PARTS_ADDED_RETRACTED, "已停止：读取 %d 个部分，添加 %d 条，撤回 %d 条");

        ja(REVIEW_CHANGES, "変更を確認");
        zh(REVIEW_CHANGES, "审阅更改");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(LOG, "ログ");
        zh(LOG, "日志");

        ja(REVIEW_THE_CHANGES_RUN, "変更の確認（%d 回目）");
        zh(REVIEW_THE_CHANGES_RUN, "审阅更改（第 %d 轮）");

        ja(APPLY, "適用");
        zh(APPLY, "应用");

        ja(CHECK_ALL, "すべて選択");
        zh(CHECK_ALL, "全选");

        ja(UNCHECK_ALL, "すべて解除");
        zh(UNCHECK_ALL, "全部取消");

        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(CHANGE, "変更");
        zh(CHANGE, "更改");

        ja(ITEM, "項目");
        zh(ITEM, "条目");

        ja(EVIDENCE, "根拠");
        zh(EVIDENCE, "依据");

        ja(IN_PART_N_QUOTE, "パート %d：「%s」");
        zh(IN_PART_N_QUOTE, "第 %d 部分：“%s”");

        ja(THE_QUOTE_IS_NOT_IN_THE_PART, "この引用はそのパートにありません。受け入れる前に確認してください");
        zh(THE_QUOTE_IS_NOT_IN_THE_PART, "该引文不在此部分中，接受前请先核对");

        ja(LEFT_OUT_N, "除外（%d）");
        zh(LEFT_OUT_N, "已排除（%d）");

        ja(UNCHECKED_ADDITIONS_ARE_KEPT, "チェックを外した追加は古い知識として残ります。チェックを外した撤回は何も変更しません。");
        zh(UNCHECKED_ADDITIONS_ARE_KEPT, "取消勾选的添加会保留为旧认知；取消勾选的撤回不会改变任何内容。");

        ja(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "確認中に物語が変更されました。もう一度処理してください");
        zh(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "您审阅期间故事发生了变化，请重新处理");

        ja(CHANGES_TO_REVIEW, "確認待ちの変更 %d 件");
        zh(CHANGES_TO_REVIEW, "%d 项更改待审阅");

        ja(READ_WHAT_THE_CHARACTERS_LEARN, "モデルでキャラクターが知ったことを読み取る");
        zh(READ_WHAT_THE_CHARACTERS_LEARN, "用模型读取角色得知的内容");

        ja(ASK_THE_MODEL_WHAT_EACH_TRACKED, "追跡中の各キャラクターがパートで知ったことをモデルに尋ね、変更として提案します。オフの場合、処理は誰が誰に会ったかを記録するだけです。");
        zh(ASK_THE_MODEL_WHAT_EACH_TRACKED, "询问模型每个被追踪的角色在这些部分中得知了什么，并作为更改提出。关闭时，处理只记录谁见过谁。");

        ja(ASK_BEFORE_APPLYING, "適用前に確認する");
        zh(ASK_BEFORE_APPLYING, "应用前先确认");

        ja(SHOW_WHAT_THE_MODEL_FOUND, "何かを適用する前に、モデルが見つけた内容を確認のために表示します。オフの場合は直ちに適用され、ログに記録されます。");
        zh(SHOW_WHAT_THE_MODEL_FOUND, "在应用任何内容之前，先显示模型找到的内容供审阅。关闭时会立即应用并记入日志。");

        ja(PARTS_PER_RUN, "1回あたりのパート数");
        zh(PARTS_PER_RUN, "每轮处理的部分数");

        ja(LOREBOOK_FOR_THE_MODEL, "モデルに渡すロアブック");
        zh(LOREBOOK_FOR_THE_MODEL, "提供给模型的设定集");

        ja(ENTRIES_THE_PARTS_ACTIVATE, "パートが有効にするエントリ");
        zh(ENTRIES_THE_PARTS_ACTIVATE, "这些部分触发的条目");

        ja(THE_WHOLE_LOREBOOK, "ロアブック全体");
        zh(THE_WHOLE_LOREBOOK, "整个设定集");

        ja(NO_LOREBOOK, "ロアブックなし");
        zh(NO_LOREBOOK, "不使用设定集");

        ja(MODEL_FOR_THE_EXTRACTION, "読み取りに使うモデル");
        zh(MODEL_FOR_THE_EXTRACTION, "用于读取的模型");

        ja(PROTOCOL_FOR_THE_EXTRACTION, "読み取りに使うプロトコル");
        zh(PROTOCOL_FOR_THE_EXTRACTION, "用于读取的协议");

        ja(THE_MODEL_OF_THE_BOOK, "本のモデル");
        zh(THE_MODEL_OF_THE_BOOK, "本书的模型");

        ja(SHOW_PART_IN_THE_STORY, "パート %d をストーリーで表示");
        zh(SHOW_PART_IN_THE_STORY, "在故事中显示第 %d 部分");
    }
}
