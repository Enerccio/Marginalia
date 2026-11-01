package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of Beta Readers in the languages besides English; the English text is the key.
 */
public class BetaReadersLocalization extends PluginLocalizationBundle {

    public static final BetaReadersLocalization T = new BetaReadersLocalization();

    public static final String BETA_READERS = "Beta Readers";
    public static final String PERSONA_LIBRARY = "Persona library";
    public static final String PERSONAS_D = "Personas: %d";
    public static final String MEDIUMS_D = "Mediums: %d";
    public static final String EDITED = "(edited)";
    public static final String EDIT_PERSONAS = "Edit personas";
    public static final String EDIT_MEDIUMS = "Edit mediums";
    public static final String MEDIUMS = "Mediums";
    public static final String SCENE = "Scene";
    public static final String WHERE_AND_HOW_THE_CONVERSATION = "Where and how the conversation takes place; it is sent in the system prompt of every chat";
    public static final String EMPTY_THE_BUILT_IN_SCENE = "Empty: the built-in scene, an online chat. It is sent in the system prompt of every chat, after the prompt, or where the prompt has {{scene}}.";
    public static final String EMPTY_THE_BUILT_IN_PROMPT = "Empty: the built-in prompt. {{medium}} is the text of the medium, {{scene}} the scene.";
    public static final String EMPTY_INHERITED_WHERE_AND_HOW = "Empty: inherited. Where and how the conversation takes place; it goes into the system prompt.";
    public static final String THE_SCENE_IS_NOW = "The scene is now: %s";
    public static final String WHAT_THE_PERSONA_IS_LIKE = "What the persona is like";
    public static final String A_GRUMPY_EDITOR_WHO_HATES = "A grumpy editor who hates adverbs, a teenager who only reads fantasy...";
    public static final String DESCRIBE_THE_PERSONA = "Describe the persona in a few words and let the model write the name and the description; you can change them before you save.";
    public static final String WRITE_WITH_THE_MODEL = "Write with the model";
    public static final String DESCRIBE_THE_PERSONA_FIRST = "Describe the persona first";
    public static final String MEDIUM = "Medium";
    public static final String MODEL_OF_THE_CHATS = "Model of the chats";
    public static final String PROTOCOL_OF_THE_CHATS = "Protocol of the chats";
    public static final String PROMPT_OF_A_CHAT = "Prompt of a chat with one persona";
    public static final String PROMPT_OF_A_GROUP_CHAT = "Prompt of a group chat";
    public static final String PROMPT = "Prompt";
    public static final String THE_SYSTEM_PROMPT = "The system prompt of a 1v1 chat";
    public static final String THE_SYSTEM_PROMPT_OF_A_CHAT = "The system prompt of a chat with a group of personas";
    public static final String WHAT_THE_TEXT_STANDS = "What the text stands for: the personas react to it as the audience of that medium";
    public static final String WHAT_BOOKS_AND_FOLDERS_INHERIT = "What books and folders inherit: a book or a folder can set its own in its settings. The personas and the mediums are yours, a book can add personas of its own.";
    public static final String EMPTY_THE_BUILT_IN_DEFAULT = "Empty: the built-in default";
    public static final String EMPTY_THE_MODEL = "Empty: the model of the book";
    public static final String EMPTY_THE_PROTOCOL = "Empty: the protocol of the book";
    public static final String ADD = "Add";
    public static final String EDIT = "Edit";
    public static final String COPY = "Copy";
    public static final String DELETE = "Delete";
    public static final String DELETE_S = "Delete \"%s\"?";
    public static final String MOVE_UP = "Move up";
    public static final String MOVE_DOWN = "Move down";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String ICON = "Icon";
    public static final String UPLOAD_ICON = "Upload icon";
    public static final String REMOVE_ICON = "Remove icon";
    public static final String ICON_SCALED_DOWN = "Icon (scaled down to at most %1$d x %1$d pixels; without one, a circle with the first letters of the name is shown)";
    public static final String THE_FILE = "The file is not an image that can be read";
    public static final String THE_FILE_IS_NOT_AN_IMAGE = "The file is not an image or it is too big";
    public static final String THE_NAME_IS_REQUIRED = "The name is required";
    public static final String ANOTHER_PERSONA_HAS_THIS_NAME = "Another persona has this name";
    public static final String ANOTHER_MEDIUM_HAS_THIS_NAME = "Another medium has this name";
    public static final String NEW_PERSONA = "New persona";
    public static final String EDIT_PERSONA = "Edit persona";
    public static final String NEW_MEDIUM = "New medium";
    public static final String EDIT_MEDIUM = "Edit medium";
    public static final String HOW_THE_PERSONA_READS = "How the persona reads and what it cares about; it is what the model plays";
    public static final String GOES_INTO_THE_PROMPT = "Goes into the prompt of the chats: what the text stands for and how the personas react to it";
    public static final String ADD_THE_STARTER_PERSONAS = "Add the starter personas";
    public static final String ADDS_THE_PERSONAS_THE_PLUGIN = "Adds the personas the plugin comes with that are not in the list";
    public static final String ALL_THE_STARTER_PERSONAS = "All the starter personas are in the list";
    public static final String RESTORE_THE_BUILT_IN_MEDIUMS = "Restore the built-in mediums";
    public static final String ADDS_THE_MEDIUMS_THE_PLUGIN = "Adds the mediums the plugin comes with that are not in the list";
    public static final String ALL_THE_BUILT_IN_MEDIUMS = "All the built-in mediums are in the list";
    public static final String BUILT = "built-in";
    public static final String TITLE = "Title";
    public static final String PERSONA = "Persona";
    public static final String PERSONAS = "Personas";
    public static final String MODEL = "Model";
    public static final String PROTOCOL = "Protocol";
    public static final String NEW_CHAT = "New chat";
    public static final String KIND = "Kind";
    public static final String ONE_PERSONA = "One persona";
    public static final String A_GROUP = "A group";
    public static final String A_CHAT_WITH_ONE_PERSONA_STAYS = "A chat with one persona stays that way. A group can start with one persona, and others can join later.";
    public static final String EMPTY_THE_NAMES = "Empty: the names of the personas";
    public static final String REPLY_FORMAT = "Reply format";
    public static final String AUTOMATIC_ASKS_FOR_JSON = "Automatic asks for JSON in a schema when the model can follow one, free text otherwise";
    public static final String JSON_WITHOUT_A_SCHEMA = "JSON without a schema";
    public static final String JSON_WITHOUT_A_SCHEMA_HELP = "JSON without a schema is for a server that can not take a schema together with the thinking of the model";
    public static final String CREATE = "Create";
    public static final String INHERITED = "Inherited: %s";
    public static final String NONE = "none";
    public static final String THIS_BOOK = "this book";
    public static final String AUTOMATIC = "Automatic";
    public static final String JSON = "JSON";
    public static final String FREE_TEXT = "Free text";
    public static final String CHOOSE_A_PERSONA = "Choose a persona";
    public static final String CHOOSE_AT_LEAST_ONE_PERSONA = "Choose at least one persona";
    public static final String A_CHAT_WITH_ONE_PERSONA = "A chat with one persona";
    public static final String A_GROUP_CHAT = "A group chat";
    public static final String MEDIUM_S = "Medium: %s";
    public static final String MODEL_S = "Model: %s";
    public static final String LEFT = "left";
    public static final String CHATS = "Chats";
    public static final String RENAME_CHAT = "Rename chat";
    public static final String CLOSE_CHAT = "Close chat";
    public static final String DELETE_CHAT = "Delete chat";
    public static final String PERSONAS_OF_THIS_BOOK = "Personas of this book";
    public static final String CLOSED_CHATS = "Closed chats";
    public static final String NO_CHATS_YET_START_ONE = "No chats yet. Start one with the + button.";
    public static final String THERE_IS_NO_PERSONA_TO_TALK = "There is no persona to talk to. Add some to the library in the settings, or to this book.";
    public static final String JOINED_THE_CONVERSATION = "%s joined the conversation.";
    public static final String LEFT_THE_CONVERSATION = "%s left the conversation.";
    public static final String YOU = "You";
    public static final String PARTS_SENT = "Parts %s sent";
    public static final String REASONING = "Reasoning";
    public static final String NARRATOR = "Narrator";
    public static final String PERSONAS_COUNT = "Personas (%d)";
    public static final String WRITE_TO_THE_BETA_READERS = "Write to the beta readers...";
    public static final String CHECK_FOR_NEW_PARTS = "Check for new parts";
    public static final String INCLUDE_THE_LATEST_PARTS = "Include the latest parts";
    public static final String SEND = "Send";
    public static final String STOP = "Stop";
    public static final String THERE_IS_NO_MODEL = "There is no model to ask. Choose one for the chat, the book or in the settings.";
    public static final String WRITE_A_MESSAGE_OR_INCLUDE = "Write a message or include the latest parts.";
    public static final String CANT_ASK_THE_MODEL = "Can't ask the model: %s";
    public static final String THE_MODEL_DID_NOT_ANSWER = "The model did not answer.";
    public static final String OLDER_MESSAGES_WERE_LEFT_OUT = "%d older messages were left out of the request to fit the context.";
    public static final String INCLUDE_THE_STORY_PARTS = "Include the story (%d parts)";
    public static final String INCLUDE_THE_LATEST_PARTS_NEW = "Include the latest parts (%d new)";
    public static final String INCLUDE_THE_LATEST_PARTS_NONE = "Include the latest parts (none new)";
    public static final String CONTEXT_OF_TOKENS = "Context: %1$s of %2$s tokens";
    public static final String CONTEXT_TOKENS = "Context: %s tokens";
    public static final String REGENERATE = "Regenerate";
    public static final String THERE_IS_NO_REPLY_YET = "There is no reply yet.";
    public static final String ASK_AGAIN = "Ask again";
    public static final String EDIT_MESSAGE = "Edit message";
    public static final String DELETE_MESSAGE = "Delete message";
    public static final String DELETE_THIS_MESSAGE = "Delete this message?";
    public static final String BRING_IN_A_PERSONA = "Bring in a persona";
    public static final String BRING = "Bring in";
    public static final String SEND_AWAY = "Send away";
    public static final String SEND_AWAY_S = "Send away %s";
    public static final String UPDATE_FROM_THE_LIBRARY = "Update from the library";
    public static final String IS_UPDATED_FROM_THE_LIBRARY = "%s is updated from the library.";
    public static final String IS_NOT_IN_THE_LIBRARY = "%s is not in the library or in this book any more.";
    public static final String THERE_IS_NO_PERSONA = "There is no persona to bring in: they are all here.";
    public static final String THE_LAST_PERSONA_CANT_LEAVE = "The last persona can't leave.";
    public static final String WHAT_THE_GROUP_IS_TOLD = "What the group is told (optional)";
    public static final String CHAT_SETTINGS = "Chat settings";
    public static final String FROM_NOW_ON_THE_TEXT = "From now on the text is read as: %s";
    public static final String PERSONAS_READ_UP_TO_PART = "Personas read up to part";
    public static final String START_FROM_PART = "Start from part";
    public static final String USE_THE_SUMMARIES = "Use the summaries of the story";
    public static final String EMPTY_THE_WHOLE_STORY = "Empty: the whole story. The personas never get a part after this one.";
    public static final String EMPTY_FROM_THE_FIRST_PART = "Empty: from the first part. Earlier parts are not sent, not even as summaries.";
    public static final String THE_FIRST_PART_CANT_COME = "The first part can't come after the last one.";
    public static final String STORY_MEDIUM_MODEL_AND_FORMAT = "Story, medium, model and format";
    public static final String READS_UP_TO_PART = "Reads up to part %d";
    public static final String FROM_PART = "from part %d";
    public static final String THE_PERSONAS_HAVE_READ_PARTS = "The personas have read: parts %s";
    public static final String SEND_AS_A_SCENE = "Send as scene";
    public static final String SEND_THE_TEXT_AS_A_SCENE = "Send the text as a scene: a change of scene or what you do, not something you say. The personas react to it. Parts of the story are not included.";
    public static final String WRITE_THE_SCENE_FIRST = "Write the scene first.";
    public static final String MOST_MESSAGES_IN_A_TURN = "Most messages in a turn";
    public static final String EMPTY_INHERITED_THE_MOST_MESSAGES = "Empty: inherited. The most messages the personas write in answer to one message of yours.";
    public static final String EMPTY_AUTOMATIC_THE_MOST_MESSAGES = "Empty: automatic (3 for one persona, about two for each persona of a group, at most 8). The most messages the personas write in answer to one message; a chat can set its own.";
    public static final String THE_MOST_MESSAGES_MUST_BE = "The most messages in a turn must be between 1 and %d.";

    @Override
    protected void loadKeys() {
        ja(BETA_READERS, "ベータリーダー");
        zh(BETA_READERS, "试读者");

        ja(PERSONA_LIBRARY, "ペルソナライブラリ");
        zh(PERSONA_LIBRARY, "读者角色库");

        ja(PERSONAS_D, "ペルソナ: %d");
        zh(PERSONAS_D, "读者角色：%d");

        ja(MEDIUMS_D, "メディア: %d");
        zh(MEDIUMS_D, "媒介：%d");

        ja(EDITED, "（編集済み）");
        zh(EDITED, "（已编辑）");

        ja(EDIT_PERSONAS, "ペルソナ一覧を編集");
        zh(EDIT_PERSONAS, "编辑读者角色列表");

        ja(EDIT_MEDIUMS, "メディア一覧を編集");
        zh(EDIT_MEDIUMS, "编辑媒介列表");

        ja(MEDIUMS, "メディア");
        zh(MEDIUMS, "媒介");

        ja(SCENE, "シーン");
        zh(SCENE, "场景");

        ja(WHERE_AND_HOW_THE_CONVERSATION, "会話がどこで、どのように行われるか。すべてのチャットのシステムプロンプトに送られます");
        zh(WHERE_AND_HOW_THE_CONVERSATION, "对话发生的地点和方式；它会随每个聊天的系统提示词一起发送");

        ja(EMPTY_THE_BUILT_IN_SCENE, "空の場合: 組み込みのシーン（オンラインチャット）。すべてのチャットのシステムプロンプトの、プロンプトの後（プロンプトに {{scene}} がある場合はその位置）に送られます。");
        zh(EMPTY_THE_BUILT_IN_SCENE, "留空：使用内置场景（在线聊天）。它会放在每个聊天的系统提示词中提示词之后（提示词里有 {{scene}} 时放在该位置）。");

        ja(EMPTY_THE_BUILT_IN_PROMPT, "空の場合: 組み込みのプロンプト。{{medium}} はメディアのテキスト、{{scene}} はシーンです。");
        zh(EMPTY_THE_BUILT_IN_PROMPT, "留空：使用内置提示词。{{medium}} 是媒介的文字，{{scene}} 是场景。");

        ja(EMPTY_INHERITED_WHERE_AND_HOW, "空の場合: 継承。会話がどこで、どのように行われるか。システムプロンプトに入ります。");
        zh(EMPTY_INHERITED_WHERE_AND_HOW, "留空：继承。对话发生的地点和方式；它会进入系统提示词。");

        ja(THE_SCENE_IS_NOW, "今のシーン: %s");
        zh(THE_SCENE_IS_NOW, "现在的场景：%s");

        ja(WHAT_THE_PERSONA_IS_LIKE, "ペルソナの人物像");
        zh(WHAT_THE_PERSONA_IS_LIKE, "这个读者角色是什么样的");

        ja(A_GRUMPY_EDITOR_WHO_HATES, "副詞が大嫌いな気難しい編集者、ファンタジーしか読まない十代の若者…");
        zh(A_GRUMPY_EDITOR_WHO_HATES, "讨厌副词的暴躁编辑，只读奇幻小说的少年……");

        ja(DESCRIBE_THE_PERSONA, "ペルソナを数語で説明すると、モデルが名前と説明を書きます。保存する前に変更できます。");
        zh(DESCRIBE_THE_PERSONA, "用几个词描述这个读者角色，让模型写出名字和描述；保存前你还可以修改。");

        ja(WRITE_WITH_THE_MODEL, "モデルで書く");
        zh(WRITE_WITH_THE_MODEL, "让模型来写");

        ja(DESCRIBE_THE_PERSONA_FIRST, "先にペルソナを説明してください");
        zh(DESCRIBE_THE_PERSONA_FIRST, "请先描述这个读者角色");

        ja(MEDIUM, "メディア");
        zh(MEDIUM, "媒介");

        ja(MODEL_OF_THE_CHATS, "チャットのモデル");
        zh(MODEL_OF_THE_CHATS, "聊天使用的模型");

        ja(PROTOCOL_OF_THE_CHATS, "チャットのプロトコル");
        zh(PROTOCOL_OF_THE_CHATS, "聊天使用的协议");

        ja(PROMPT_OF_A_CHAT, "ペルソナ1人とのチャットのプロンプト");
        zh(PROMPT_OF_A_CHAT, "与单个读者角色聊天的提示词");

        ja(PROMPT_OF_A_GROUP_CHAT, "グループチャットのプロンプト");
        zh(PROMPT_OF_A_GROUP_CHAT, "群聊的提示词");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");

        ja(THE_SYSTEM_PROMPT, "1対1チャットのシステムプロンプト");
        zh(THE_SYSTEM_PROMPT, "一对一聊天的系统提示词");

        ja(THE_SYSTEM_PROMPT_OF_A_CHAT, "ペルソナのグループとのチャットのシステムプロンプト");
        zh(THE_SYSTEM_PROMPT_OF_A_CHAT, "与一组读者角色聊天的系统提示词");

        ja(WHAT_THE_TEXT_STANDS,
                "テキストが何を表すか: ペルソナはそのメディアの受け手として反応します");
        zh(WHAT_THE_TEXT_STANDS,
                "文本代表什么：读者角色会以该媒介的观众身份做出反应");

        ja(WHAT_BOOKS_AND_FOLDERS_INHERIT,
                "本とフォルダーが継承する設定です。本やフォルダーは、それぞれの設定で独自の値を指定できます。ペルソナとメディアはあなた専用で、本には独自のペルソナを追加できます。");
        zh(WHAT_BOOKS_AND_FOLDERS_INHERIT,
                "书籍和文件夹继承的设置：书籍或文件夹可以在各自的设置中指定自己的值。读者角色和媒介属于你，书籍还可以添加自己的读者角色。");

        ja(EMPTY_THE_BUILT_IN_DEFAULT, "空の場合: 組み込みの既定値");
        zh(EMPTY_THE_BUILT_IN_DEFAULT, "留空：使用内置默认值");

        ja(EMPTY_THE_MODEL, "空の場合: 本のモデル");
        zh(EMPTY_THE_MODEL, "留空：使用书籍的模型");

        ja(EMPTY_THE_PROTOCOL, "空の場合: 本のプロトコル");
        zh(EMPTY_THE_PROTOCOL, "留空：使用书籍的协议");

        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");

        ja(COPY, "コピー");
        zh(COPY, "复制");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(DELETE_S, "「%s」を削除しますか？");
        zh(DELETE_S, "删除“%s”？");

        ja(MOVE_UP, "上へ移動");
        zh(MOVE_UP, "上移");

        ja(MOVE_DOWN, "下へ移動");
        zh(MOVE_DOWN, "下移");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "描述");

        ja(ICON, "アイコン");
        zh(ICON, "图标");

        ja(UPLOAD_ICON, "アイコンをアップロード");
        zh(UPLOAD_ICON, "上传图标");

        ja(REMOVE_ICON, "アイコンを削除");
        zh(REMOVE_ICON, "移除图标");

        ja(ICON_SCALED_DOWN,
                "アイコン（最大 %1$d x %1$d ピクセルに縮小されます。ない場合は名前の最初の文字を入れた円が表示されます）");
        zh(ICON_SCALED_DOWN,
                "图标（缩小到最多 %1$d x %1$d 像素；没有图标时显示带有名称首字母的圆形）");

        ja(THE_FILE, "ファイルは読み取れる画像ではありません");
        zh(THE_FILE, "该文件不是可读取的图片");

        ja(THE_FILE_IS_NOT_AN_IMAGE, "ファイルが画像ではないか、大きすぎます");
        zh(THE_FILE_IS_NOT_AN_IMAGE, "该文件不是图片或太大");

        ja(THE_NAME_IS_REQUIRED, "名前は必須です");
        zh(THE_NAME_IS_REQUIRED, "名称为必填项");

        ja(ANOTHER_PERSONA_HAS_THIS_NAME, "この名前のペルソナが既にあります");
        zh(ANOTHER_PERSONA_HAS_THIS_NAME, "已有同名的读者角色");

        ja(ANOTHER_MEDIUM_HAS_THIS_NAME, "この名前のメディアが既にあります");
        zh(ANOTHER_MEDIUM_HAS_THIS_NAME, "已有同名的媒介");

        ja(NEW_PERSONA, "新しいペルソナ");
        zh(NEW_PERSONA, "新建读者角色");

        ja(EDIT_PERSONA, "ペルソナを編集");
        zh(EDIT_PERSONA, "编辑读者角色");

        ja(NEW_MEDIUM, "新しいメディア");
        zh(NEW_MEDIUM, "新建媒介");

        ja(EDIT_MEDIUM, "メディアを編集");
        zh(EDIT_MEDIUM, "编辑媒介");

        ja(HOW_THE_PERSONA_READS,
                "ペルソナがどう読み、何を重視するか。モデルが演じる内容です");
        zh(HOW_THE_PERSONA_READS,
                "该读者角色如何阅读、关注什么；模型将扮演它");

        ja(GOES_INTO_THE_PROMPT,
                "チャットのプロンプトに入ります: テキストが何を表すか、ペルソナがそれにどう反応するか");
        zh(GOES_INTO_THE_PROMPT,
                "会加入聊天的提示词：文本代表什么，以及读者角色如何对其做出反应");

        ja(ADD_THE_STARTER_PERSONAS, "初期ペルソナを追加");
        zh(ADD_THE_STARTER_PERSONAS, "添加预设读者角色");

        ja(ADDS_THE_PERSONAS_THE_PLUGIN,
                "拡張機能に付属するペルソナのうち、リストにないものを追加します");
        zh(ADDS_THE_PERSONAS_THE_PLUGIN,
                "添加扩展自带但不在列表中的读者角色");

        ja(ALL_THE_STARTER_PERSONAS, "初期ペルソナはすべてリストにあります");
        zh(ALL_THE_STARTER_PERSONAS, "所有预设读者角色都已在列表中");

        ja(RESTORE_THE_BUILT_IN_MEDIUMS, "組み込みのメディアを復元");
        zh(RESTORE_THE_BUILT_IN_MEDIUMS, "恢复内置媒介");

        ja(ADDS_THE_MEDIUMS_THE_PLUGIN,
                "拡張機能に付属するメディアのうち、リストにないものを追加します");
        zh(ADDS_THE_MEDIUMS_THE_PLUGIN,
                "添加扩展自带但不在列表中的媒介");

        ja(ALL_THE_BUILT_IN_MEDIUMS, "組み込みのメディアはすべてリストにあります");
        zh(ALL_THE_BUILT_IN_MEDIUMS, "所有内置媒介都已在列表中");

        ja(BUILT, "組み込み");
        zh(BUILT, "内置");

        ja(TITLE,
                "タイトル");
        zh(TITLE,
                "标题");

        ja(PERSONA,
                "ペルソナ");
        zh(PERSONA,
                "读者角色");

        ja(PERSONAS,
                "ペルソナ");
        zh(PERSONAS,
                "读者角色");

        ja(MODEL,
                "モデル");
        zh(MODEL,
                "模型");

        ja(PROTOCOL,
                "プロトコル");
        zh(PROTOCOL,
                "协议");

        ja(NEW_CHAT,
                "新しいチャット");
        zh(NEW_CHAT,
                "新建聊天");

        ja(KIND,
                "種類");
        zh(KIND,
                "类型");

        ja(ONE_PERSONA,
                "1人のペルソナ");
        zh(ONE_PERSONA,
                "单个读者角色");

        ja(A_GROUP,
                "グループ");
        zh(A_GROUP,
                "群组");

        ja(A_CHAT_WITH_ONE_PERSONA_STAYS,
                "ペルソナ1人とのチャットはそのままです。グループは1人のペルソナで始めることができ、後から他のペルソナが参加できます。");
        zh(A_CHAT_WITH_ONE_PERSONA_STAYS,
                "与单个读者角色的聊天始终如此。群组可以从一个读者角色开始，之后其他角色可以加入。");

        ja(EMPTY_THE_NAMES,
                "空の場合: ペルソナの名前");
        zh(EMPTY_THE_NAMES,
                "留空：使用读者角色的名称");

        ja(REPLY_FORMAT,
                "返信の形式");
        zh(REPLY_FORMAT,
                "回复格式");

        ja(AUTOMATIC_ASKS_FOR_JSON,
                "自動: モデルがスキーマに従える場合はスキーマ付きの JSON、それ以外は自由テキストを要求します");
        zh(AUTOMATIC_ASKS_FOR_JSON,
                "自动：模型能遵循模式时要求带模式的 JSON，否则使用自由文本");

        ja(JSON_WITHOUT_A_SCHEMA,
                "スキーマなしの JSON");
        zh(JSON_WITHOUT_A_SCHEMA,
                "不带模式的 JSON");

        ja(JSON_WITHOUT_A_SCHEMA_HELP,
                "スキーマなしの JSON は、スキーマとモデルの思考を同時に扱えないサーバー向けです");
        zh(JSON_WITHOUT_A_SCHEMA_HELP,
                "不带模式的 JSON 适用于无法同时处理模式和模型思考的服务器");

        ja(CREATE,
                "作成");
        zh(CREATE,
                "创建");

        ja(INHERITED,
                "継承: %s");
        zh(INHERITED,
                "继承：%s");

        ja(NONE,
                "なし");
        zh(NONE,
                "无");

        ja(THIS_BOOK,
                "この本");
        zh(THIS_BOOK,
                "本书");

        ja(AUTOMATIC,
                "自動");
        zh(AUTOMATIC,
                "自动");

        ja(JSON,
                "JSON");
        zh(JSON,
                "JSON");

        ja(FREE_TEXT,
                "自由テキスト");
        zh(FREE_TEXT,
                "自由文本");

        ja(CHOOSE_A_PERSONA,
                "ペルソナを選択してください");
        zh(CHOOSE_A_PERSONA,
                "请选择一个读者角色");

        ja(CHOOSE_AT_LEAST_ONE_PERSONA,
                "ペルソナを1人以上選択してください");
        zh(CHOOSE_AT_LEAST_ONE_PERSONA,
                "请至少选择一个读者角色");

        ja(A_CHAT_WITH_ONE_PERSONA,
                "ペルソナ1人とのチャット");
        zh(A_CHAT_WITH_ONE_PERSONA,
                "与单个读者角色的聊天");

        ja(A_GROUP_CHAT,
                "グループチャット");
        zh(A_GROUP_CHAT,
                "群聊");

        ja(MEDIUM_S,
                "メディア: %s");
        zh(MEDIUM_S,
                "媒介：%s");

        ja(MODEL_S,
                "モデル: %s");
        zh(MODEL_S,
                "模型：%s");

        ja(LEFT,
                "退出済み");
        zh(LEFT,
                "已离开");

        ja(CHATS,
                "チャット");
        zh(CHATS,
                "聊天");

        ja(RENAME_CHAT,
                "チャット名を変更");
        zh(RENAME_CHAT,
                "重命名聊天");

        ja(CLOSE_CHAT,
                "チャットを閉じる");
        zh(CLOSE_CHAT,
                "关闭聊天");

        ja(DELETE_CHAT,
                "チャットを削除");
        zh(DELETE_CHAT,
                "删除聊天");

        ja(PERSONAS_OF_THIS_BOOK,
                "この本のペルソナ");
        zh(PERSONAS_OF_THIS_BOOK,
                "本书的读者角色");

        ja(CLOSED_CHATS,
                "閉じたチャット");
        zh(CLOSED_CHATS,
                "已关闭的聊天");

        ja(NO_CHATS_YET_START_ONE,
                "チャットはまだありません。+ ボタンで開始してください。");
        zh(NO_CHATS_YET_START_ONE,
                "还没有聊天。点击 + 按钮开始。");

        ja(THERE_IS_NO_PERSONA_TO_TALK,
                "話し相手のペルソナがありません。設定のライブラリか、この本に追加してください。");
        zh(THERE_IS_NO_PERSONA_TO_TALK,
                "没有可交谈的读者角色。请在设置的角色库或本书中添加。");


        ja(JOINED_THE_CONVERSATION,
                "%s が会話に参加しました。");
        zh(JOINED_THE_CONVERSATION,
                "%s 加入了对话。");

        ja(LEFT_THE_CONVERSATION,
                "%s が会話から退出しました。");
        zh(LEFT_THE_CONVERSATION,
                "%s 离开了对话。");

        ja(YOU,
                "あなた");
        zh(YOU,
                "你");

        ja(PARTS_SENT,
                "送信したパート: %s");
        zh(PARTS_SENT,
                "已发送的部分：%s");

        ja(REASONING,
                "推論");
        zh(REASONING,
                "推理");

        ja(NARRATOR,
                "ナレーター");
        zh(NARRATOR,
                "旁白");

        ja(PERSONAS_COUNT,
                "ペルソナ (%d)");
        zh(PERSONAS_COUNT,
                "读者角色 (%d)");

        ja(WRITE_TO_THE_BETA_READERS,
                "ベータリーダーに書く...");
        zh(WRITE_TO_THE_BETA_READERS,
                "写给试读者……");

        ja(CHECK_FOR_NEW_PARTS,
                "新しいパートを確認");
        zh(CHECK_FOR_NEW_PARTS,
                "检查新的部分");

        ja(INCLUDE_THE_LATEST_PARTS,
                "最新のパートを含める");
        zh(INCLUDE_THE_LATEST_PARTS,
                "包含最新的部分");

        ja(SEND,
                "送信");
        zh(SEND,
                "发送");

        ja(STOP,
                "停止");
        zh(STOP,
                "停止");

        ja(THERE_IS_NO_MODEL,
                "問い合わせるモデルがありません。チャット、本、または設定でモデルを選択してください。");
        zh(THERE_IS_NO_MODEL,
                "没有可询问的模型。请在聊天、书籍或设置中选择一个。");

        ja(WRITE_A_MESSAGE_OR_INCLUDE,
                "メッセージを書くか、最新のパートを含めてください。");
        zh(WRITE_A_MESSAGE_OR_INCLUDE,
                "请写一条消息，或包含最新的部分。");

        ja(CANT_ASK_THE_MODEL,
                "モデルに問い合わせできません: %s");
        zh(CANT_ASK_THE_MODEL,
                "无法询问模型：%s");

        ja(THE_MODEL_DID_NOT_ANSWER,
                "モデルは応答しませんでした。");
        zh(THE_MODEL_DID_NOT_ANSWER,
                "模型没有回答。");

        ja(OLDER_MESSAGES_WERE_LEFT_OUT,
                "コンテキストに収めるため、古いメッセージ %d 件をリクエストから省きました。");
        zh(OLDER_MESSAGES_WERE_LEFT_OUT,
                "为适应上下文，请求中省略了 %d 条较早的消息。");

        ja(INCLUDE_THE_STORY_PARTS,
                "ストーリーを含める (%d パート)");
        zh(INCLUDE_THE_STORY_PARTS,
                "包含故事（%d 个部分）");

        ja(INCLUDE_THE_LATEST_PARTS_NEW,
                "最新のパートを含める (新規 %d)");
        zh(INCLUDE_THE_LATEST_PARTS_NEW,
                "包含最新的部分（新增 %d）");

        ja(INCLUDE_THE_LATEST_PARTS_NONE,
                "最新のパートを含める (新しいものなし)");
        zh(INCLUDE_THE_LATEST_PARTS_NONE,
                "包含最新的部分（没有新的）");

        ja(CONTEXT_OF_TOKENS,
                "コンテキスト: %1$s / %2$s トークン");
        zh(CONTEXT_OF_TOKENS,
                "上下文：%1$s / %2$s 个词元");

        ja(CONTEXT_TOKENS,
                "コンテキスト: %s トークン");
        zh(CONTEXT_TOKENS,
                "上下文：%s 个词元");

        ja(REGENERATE,
                "再生成");
        zh(REGENERATE,
                "重新生成");

        ja(THERE_IS_NO_REPLY_YET,
                "まだ返信がありません。");
        zh(THERE_IS_NO_REPLY_YET,
                "还没有回复。");

        ja(ASK_AGAIN,
                "もう一度尋ねる");
        zh(ASK_AGAIN,
                "再次询问");

        ja(EDIT_MESSAGE,
                "メッセージを編集");
        zh(EDIT_MESSAGE,
                "编辑消息");

        ja(DELETE_MESSAGE,
                "メッセージを削除");
        zh(DELETE_MESSAGE,
                "删除消息");

        ja(DELETE_THIS_MESSAGE,
                "このメッセージを削除しますか？");
        zh(DELETE_THIS_MESSAGE,
                "删除这条消息？");

        ja(BRING_IN_A_PERSONA,
                "ペルソナを呼ぶ");
        zh(BRING_IN_A_PERSONA,
                "邀请读者角色加入");

        ja(BRING,
                "呼ぶ");
        zh(BRING,
                "邀请");

        ja(SEND_AWAY,
                "退出させる");
        zh(SEND_AWAY,
                "请其离开");

        ja(SEND_AWAY_S,
                "%s を退出させる");
        zh(SEND_AWAY_S,
                "请 %s 离开");

        ja(UPDATE_FROM_THE_LIBRARY,
                "ライブラリから更新");
        zh(UPDATE_FROM_THE_LIBRARY,
                "从角色库更新");

        ja(IS_UPDATED_FROM_THE_LIBRARY,
                "%s をライブラリから更新しました。");
        zh(IS_UPDATED_FROM_THE_LIBRARY,
                "已从角色库更新 %s。");

        ja(IS_NOT_IN_THE_LIBRARY,
                "%s はライブラリにもこの本にもありません。");
        zh(IS_NOT_IN_THE_LIBRARY,
                "角色库和本书中都没有 %s 了。");

        ja(THERE_IS_NO_PERSONA,
                "呼べるペルソナがありません。全員がここにいます。");
        zh(THERE_IS_NO_PERSONA,
                "没有可邀请的读者角色：他们都在这里。");

        ja(THE_LAST_PERSONA_CANT_LEAVE,
                "最後のペルソナは退出できません。");
        zh(THE_LAST_PERSONA_CANT_LEAVE,
                "最后一个读者角色不能离开。");

        ja(WHAT_THE_GROUP_IS_TOLD,
                "グループに伝えること (任意)");
        zh(WHAT_THE_GROUP_IS_TOLD,
                "告诉群组的话（可选）");

        ja(CHAT_SETTINGS,
                "チャット設定");
        zh(CHAT_SETTINGS,
                "聊天设置");

        ja(FROM_NOW_ON_THE_TEXT,
                "これ以降、テキストは次のメディアとして読まれます: %s");
        zh(FROM_NOW_ON_THE_TEXT,
                "从现在起，文本将作为以下媒介阅读：%s");

        ja(PERSONAS_READ_UP_TO_PART,
                "ペルソナが読むパートの上限");
        zh(PERSONAS_READ_UP_TO_PART,
                "读者角色最多读到的部分");

        ja(START_FROM_PART,
                "開始パート");
        zh(START_FROM_PART,
                "起始部分");

        ja(USE_THE_SUMMARIES,
                "ストーリーの要約を使う");
        zh(USE_THE_SUMMARIES,
                "使用故事的摘要");

        ja(EMPTY_THE_WHOLE_STORY,
                "空の場合: ストーリー全体。ペルソナはこのパートより後のパートを受け取りません。");
        zh(EMPTY_THE_WHOLE_STORY,
                "留空：整个故事。读者角色不会收到此部分之后的任何部分。");

        ja(EMPTY_FROM_THE_FIRST_PART,
                "空の場合: 最初のパートから。それ以前のパートは要約としても送信されません。");
        zh(EMPTY_FROM_THE_FIRST_PART,
                "留空：从第一个部分开始。更早的部分不会发送，摘要也不会。");

        ja(THE_FIRST_PART_CANT_COME,
                "最初のパートを最後のパートより後にすることはできません。");
        zh(THE_FIRST_PART_CANT_COME,
                "起始部分不能在最后一个部分之后。");

        ja(STORY_MEDIUM_MODEL_AND_FORMAT,
                "ストーリー、メディア、モデル、形式");
        zh(STORY_MEDIUM_MODEL_AND_FORMAT,
                "故事、媒介、模型和格式");

        ja(READS_UP_TO_PART,
                "パート %d まで読む");
        zh(READS_UP_TO_PART,
                "最多读到第 %d 部分");

        ja(FROM_PART,
                "パート %d から");
        zh(FROM_PART,
                "从第 %d 部分起");

        ja(THE_PERSONAS_HAVE_READ_PARTS,
                "ペルソナが読んだパート: %s");
        zh(THE_PERSONAS_HAVE_READ_PARTS,
                "读者角色已读：部分 %s");

        ja(SEND_AS_A_SCENE,
                "シーンとして送信");
        zh(SEND_AS_A_SCENE,
                "作为场景发送");

        ja(SEND_THE_TEXT_AS_A_SCENE,
                "テキストをシーンとして送信します。シーンの転換や自分の行動であり、自分の発言ではありません。ペルソナはそれに反応します。ストーリーのパートは含まれません。");
        zh(SEND_THE_TEXT_AS_A_SCENE,
                "将文本作为场景发送：场景转换或你的行动，而不是你说的话。读者角色会对此作出反应。不包含故事的部分。");

        ja(WRITE_THE_SCENE_FIRST,
                "先にシーンを書いてください。");
        zh(WRITE_THE_SCENE_FIRST,
                "请先写下场景。");

        ja(MOST_MESSAGES_IN_A_TURN,
                "1ターンの最大メッセージ数");
        zh(MOST_MESSAGES_IN_A_TURN,
                "每轮最多消息数");

        ja(EMPTY_INHERITED_THE_MOST_MESSAGES,
                "空の場合: 継承。あなたの1通のメッセージに対してペルソナが書くメッセージの最大数。");
        zh(EMPTY_INHERITED_THE_MOST_MESSAGES,
                "留空：继承。读者角色回应你的一条消息时最多写的消息数。");

        ja(EMPTY_AUTOMATIC_THE_MOST_MESSAGES,
                "空の場合: 自動 (1人のペルソナなら3、グループなら1人につき約2、最大8)。1通のメッセージに対してペルソナが書くメッセージの最大数。チャットごとに設定することもできます。");
        zh(EMPTY_AUTOMATIC_THE_MOST_MESSAGES,
                "留空：自动（单个读者角色为 3，群组中每个角色约 2，最多 8）。读者角色回应一条消息时最多写的消息数；每个聊天可单独设置。");

        ja(THE_MOST_MESSAGES_MUST_BE,
                "1ターンの最大メッセージ数は1から%dの間でなければなりません。");
        zh(THE_MOST_MESSAGES_MUST_BE,
                "每轮最多消息数必须在 1 到 %d 之间。");
    }
}
