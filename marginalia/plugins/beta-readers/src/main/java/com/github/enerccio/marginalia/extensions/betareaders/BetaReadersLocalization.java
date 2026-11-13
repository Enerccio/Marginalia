package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of Beta Readers in the languages besides English; the English text is the key.
 */
public class BetaReadersLocalization extends PluginLocalizationBundle {

    public static final BetaReadersLocalization T = new BetaReadersLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final BetaReadersDefaults englishDefaults = new BetaReadersDefaults();
    private final Map<Language, BetaReadersDefaults> translatedDefaults = Map.of(JA, new BetaReadersDefaultsJA(), ZH, new BetaReadersDefaultsZH(), FR, new BetaReadersDefaultsFR(), ES, new BetaReadersDefaultsES(), DE, new BetaReadersDefaultsDE(),
            IT, new BetaReadersDefaultsIT(), KO, new BetaReadersDefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public BetaReadersDefaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

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
    public static final String YOUR_NAME = "Your name";
    public static final String EMPTY_NOT_NAMED = "Empty: not named. The personas call you by this name and your messages show it.";
    public static final String LAST_PART = "Last part";
    public static final String FIRST_PART = "First part";
    public static final String BETA_READERS_ANSWER_IN_A_CHAT = "Beta Readers: answer in a chat";
    public static final String BETA_READERS_WRITE_A_PERSONA = "Beta Readers: write a persona";

    @Override
    protected void loadKeys() {
        ja(BETA_READERS, "ベータリーダー");
        zh(BETA_READERS, "试读者");
        fr(BETA_READERS, "Bêta-lecteurs");
        es(BETA_READERS, "Lectores beta");
        de(BETA_READERS, "Betaleser");

        ja(PERSONA_LIBRARY, "ペルソナライブラリ");
        zh(PERSONA_LIBRARY, "读者角色库");
        fr(PERSONA_LIBRARY, "Bibliothèque de personas");
        es(PERSONA_LIBRARY, "Biblioteca de personajes lectores");
        de(PERSONA_LIBRARY, "Personabibliothek");

        ja(PERSONAS_D, "ペルソナ: %d");
        zh(PERSONAS_D, "读者角色：%d");
        fr(PERSONAS_D, "Personas : %d");
        es(PERSONAS_D, "Lectores: %d");
        de(PERSONAS_D, "Personas: %d");

        ja(MEDIUMS_D, "メディア: %d");
        zh(MEDIUMS_D, "媒介：%d");
        fr(MEDIUMS_D, "Supports : %d");
        es(MEDIUMS_D, "Medios: %d");
        de(MEDIUMS_D, "Medien: %d");

        ja(EDITED, "（編集済み）");
        zh(EDITED, "（已编辑）");
        fr(EDITED, "(modifié)");
        es(EDITED, "(editado)");
        de(EDITED, "(bearbeitet)");

        ja(EDIT_PERSONAS, "ペルソナ一覧を編集");
        zh(EDIT_PERSONAS, "编辑读者角色列表");
        fr(EDIT_PERSONAS, "Modifier les personas");
        es(EDIT_PERSONAS, "Editar lectores");
        de(EDIT_PERSONAS, "Personas bearbeiten");

        ja(EDIT_MEDIUMS, "メディア一覧を編集");
        zh(EDIT_MEDIUMS, "编辑媒介列表");
        fr(EDIT_MEDIUMS, "Modifier les supports");
        es(EDIT_MEDIUMS, "Editar medios");
        de(EDIT_MEDIUMS, "Medien bearbeiten");

        ja(MEDIUMS, "メディア");
        zh(MEDIUMS, "媒介");
        fr(MEDIUMS, "Supports");
        es(MEDIUMS, "Medios");
        de(MEDIUMS, "Medien");

        ja(SCENE, "シーン");
        zh(SCENE, "场景");
        fr(SCENE, "Scène");
        es(SCENE, "Escena");
        de(SCENE, "Szene");

        ja(WHERE_AND_HOW_THE_CONVERSATION, "会話がどこで、どのように行われるか。すべてのチャットのシステムプロンプトに送られます");
        zh(WHERE_AND_HOW_THE_CONVERSATION, "对话发生的地点和方式；它会随每个聊天的系统提示词一起发送");
        fr(WHERE_AND_HOW_THE_CONVERSATION, "Où et comment se déroule la conversation ; c'est envoyé dans le prompt système de chaque discussion");
        es(WHERE_AND_HOW_THE_CONVERSATION, "Dónde y cómo tiene lugar la conversación; se envía en el prompt del sistema de cada chat");
        de(WHERE_AND_HOW_THE_CONVERSATION, "Wo und wie das Gespräch stattfindet; es wird im Systemprompt jedes Chats gesendet");

        ja(EMPTY_THE_BUILT_IN_SCENE, "空の場合: 組み込みのシーン（オンラインチャット）。すべてのチャットのシステムプロンプトの、プロンプトの後（プロンプトに {{scene}} がある場合はその位置）に送られます。");
        zh(EMPTY_THE_BUILT_IN_SCENE, "留空：使用内置场景（在线聊天）。它会放在每个聊天的系统提示词中提示词之后（提示词里有 {{scene}} 时放在该位置）。");
        fr(EMPTY_THE_BUILT_IN_SCENE, "Vide : la scène intégrée, une discussion en ligne. Elle est envoyée dans le prompt système de chaque discussion, après le prompt, ou là où le prompt contient {{scene}}.");
        es(EMPTY_THE_BUILT_IN_SCENE, "Vacío: la escena integrada, un chat en línea. Se envía en el prompt del sistema de cada chat, después del prompt, o donde el prompt tiene {{scene}}.");
        de(EMPTY_THE_BUILT_IN_SCENE, "Leer: die eingebaute Szene, ein Online-Chat. Sie wird im Systemprompt jedes Chats gesendet, nach dem Prompt oder dort, wo der Prompt {{scene}} enthält.");

        ja(EMPTY_THE_BUILT_IN_PROMPT, "空の場合: 組み込みのプロンプト。{{medium}} はメディアのテキスト、{{scene}} はシーンです。");
        zh(EMPTY_THE_BUILT_IN_PROMPT, "留空：使用内置提示词。{{medium}} 是媒介的文字，{{scene}} 是场景。");
        fr(EMPTY_THE_BUILT_IN_PROMPT, "Vide : le prompt intégré. {{medium}} est le texte du support, {{scene}} la scène.");
        es(EMPTY_THE_BUILT_IN_PROMPT, "Vacío: el prompt integrado. {{medium}} es el texto del medio, {{scene}} la escena.");
        de(EMPTY_THE_BUILT_IN_PROMPT, "Leer: der eingebaute Prompt. {{medium}} ist der Text des Mediums, {{scene}} die Szene.");

        ja(EMPTY_INHERITED_WHERE_AND_HOW, "空の場合: 継承。会話がどこで、どのように行われるか。システムプロンプトに入ります。");
        zh(EMPTY_INHERITED_WHERE_AND_HOW, "留空：继承。对话发生的地点和方式；它会进入系统提示词。");
        fr(EMPTY_INHERITED_WHERE_AND_HOW, "Vide : hérité. Où et comment se déroule la conversation ; cela va dans le prompt système.");
        es(EMPTY_INHERITED_WHERE_AND_HOW, "Vacío: heredado. Dónde y cómo tiene lugar la conversación; va al prompt del sistema.");
        de(EMPTY_INHERITED_WHERE_AND_HOW, "Leer: geerbt. Wo und wie das Gespräch stattfindet; es geht in den Systemprompt.");

        ja(THE_SCENE_IS_NOW, "今のシーン: %s");
        zh(THE_SCENE_IS_NOW, "现在的场景：%s");
        fr(THE_SCENE_IS_NOW, "La scène est désormais : %s");
        es(THE_SCENE_IS_NOW, "La escena es ahora: %s");
        de(THE_SCENE_IS_NOW, "Die Szene ist jetzt: %s");

        ja(WHAT_THE_PERSONA_IS_LIKE, "ペルソナの人物像");
        zh(WHAT_THE_PERSONA_IS_LIKE, "这个读者角色是什么样的");
        fr(WHAT_THE_PERSONA_IS_LIKE, "Comment est le persona");
        es(WHAT_THE_PERSONA_IS_LIKE, "Cómo es el lector");
        de(WHAT_THE_PERSONA_IS_LIKE, "Wie die Persona ist");

        ja(A_GRUMPY_EDITOR_WHO_HATES, "副詞が大嫌いな気難しい編集者、ファンタジーしか読まない十代の若者…");
        zh(A_GRUMPY_EDITOR_WHO_HATES, "讨厌副词的暴躁编辑，只读奇幻小说的少年……");
        fr(A_GRUMPY_EDITOR_WHO_HATES, "Un éditeur grincheux qui déteste les adverbes, un adolescent qui ne lit que de la fantasy...");
        es(A_GRUMPY_EDITOR_WHO_HATES, "Un editor gruñón que odia los adverbios, un adolescente que solo lee fantasía...");
        de(A_GRUMPY_EDITOR_WHO_HATES, "Ein mürrischer Lektor, der Adverbien hasst, ein Teenager, der nur Fantasy liest ...");

        ja(DESCRIBE_THE_PERSONA, "ペルソナを数語で説明すると、モデルが名前と説明を書きます。保存する前に変更できます。");
        zh(DESCRIBE_THE_PERSONA, "用几个词描述这个读者角色，让模型写出名字和描述；保存前你还可以修改。");
        fr(DESCRIBE_THE_PERSONA, "Décrivez le persona en quelques mots et laissez le modèle écrire le nom et la description ; vous pouvez les changer avant d'enregistrer.");
        es(DESCRIBE_THE_PERSONA, "Describe al lector en pocas palabras y deja que el modelo escriba el nombre y la descripción; puedes cambiarlos antes de guardar.");
        de(DESCRIBE_THE_PERSONA, "Beschreibe die Persona in wenigen Worten und lass das Modell den Namen und die Beschreibung schreiben; du kannst sie vor dem Speichern ändern.");

        ja(WRITE_WITH_THE_MODEL, "モデルで書く");
        zh(WRITE_WITH_THE_MODEL, "让模型来写");
        fr(WRITE_WITH_THE_MODEL, "Écrire avec le modèle");
        es(WRITE_WITH_THE_MODEL, "Escribir con el modelo");
        de(WRITE_WITH_THE_MODEL, "Mit dem Modell schreiben");

        ja(DESCRIBE_THE_PERSONA_FIRST, "先にペルソナを説明してください");
        zh(DESCRIBE_THE_PERSONA_FIRST, "请先描述这个读者角色");
        fr(DESCRIBE_THE_PERSONA_FIRST, "Décrivez d'abord le persona");
        es(DESCRIBE_THE_PERSONA_FIRST, "Describe primero al lector");
        de(DESCRIBE_THE_PERSONA_FIRST, "Beschreibe zuerst die Persona");

        ja(MEDIUM, "メディア");
        zh(MEDIUM, "媒介");
        fr(MEDIUM, "Support");
        es(MEDIUM, "Medio");
        de(MEDIUM, "Medium");

        ja(MODEL_OF_THE_CHATS, "チャットのモデル");
        zh(MODEL_OF_THE_CHATS, "聊天使用的模型");
        fr(MODEL_OF_THE_CHATS, "Modèle des discussions");
        es(MODEL_OF_THE_CHATS, "Modelo de los chats");
        de(MODEL_OF_THE_CHATS, "Modell der Chats");

        ja(PROTOCOL_OF_THE_CHATS, "チャットのプロトコル");
        zh(PROTOCOL_OF_THE_CHATS, "聊天使用的协议");
        fr(PROTOCOL_OF_THE_CHATS, "Protocole des discussions");
        es(PROTOCOL_OF_THE_CHATS, "Protocolo de los chats");
        de(PROTOCOL_OF_THE_CHATS, "Protokoll der Chats");

        ja(PROMPT_OF_A_CHAT, "ペルソナ1人とのチャットのプロンプト");
        zh(PROMPT_OF_A_CHAT, "与单个读者角色聊天的提示词");
        fr(PROMPT_OF_A_CHAT, "Prompt d'une discussion avec un persona");
        es(PROMPT_OF_A_CHAT, "Prompt de un chat con un lector");
        de(PROMPT_OF_A_CHAT, "Prompt eines Chats mit einer Persona");

        ja(PROMPT_OF_A_GROUP_CHAT, "グループチャットのプロンプト");
        zh(PROMPT_OF_A_GROUP_CHAT, "群聊的提示词");
        fr(PROMPT_OF_A_GROUP_CHAT, "Prompt d'une discussion de groupe");
        es(PROMPT_OF_A_GROUP_CHAT, "Prompt de un chat de grupo");
        de(PROMPT_OF_A_GROUP_CHAT, "Prompt eines Gruppenchats");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");
        fr(PROMPT, "Prompt");
        es(PROMPT, "Prompt");
        de(PROMPT, "Prompt");

        ja(THE_SYSTEM_PROMPT, "1対1チャットのシステムプロンプト");
        zh(THE_SYSTEM_PROMPT, "一对一聊天的系统提示词");
        fr(THE_SYSTEM_PROMPT, "Le prompt système d'une discussion en tête-à-tête");
        es(THE_SYSTEM_PROMPT, "El prompt del sistema de un chat individual");
        de(THE_SYSTEM_PROMPT, "Der Systemprompt eines Einzelchats");

        ja(THE_SYSTEM_PROMPT_OF_A_CHAT, "ペルソナのグループとのチャットのシステムプロンプト");
        zh(THE_SYSTEM_PROMPT_OF_A_CHAT, "与一组读者角色聊天的系统提示词");
        fr(THE_SYSTEM_PROMPT_OF_A_CHAT, "Le prompt système d'une discussion avec un groupe de personas");
        es(THE_SYSTEM_PROMPT_OF_A_CHAT, "El prompt del sistema de un chat con un grupo de lectores");
        de(THE_SYSTEM_PROMPT_OF_A_CHAT, "Der Systemprompt eines Chats mit einer Gruppe von Personas");

        ja(WHAT_THE_TEXT_STANDS,
                "テキストが何を表すか: ペルソナはそのメディアの受け手として反応します");
        zh(WHAT_THE_TEXT_STANDS,
                "文本代表什么：读者角色会以该媒介的观众身份做出反应");
        fr(WHAT_THE_TEXT_STANDS, "Ce que représente le texte : les personas y réagissent comme le public de ce support");
        es(WHAT_THE_TEXT_STANDS, "Qué representa el texto: los lectores reaccionan a él como el público de ese medio");
        de(WHAT_THE_TEXT_STANDS, "Wofür der Text steht: Die Personas reagieren darauf wie das Publikum dieses Mediums");

        ja(WHAT_BOOKS_AND_FOLDERS_INHERIT,
                "本とフォルダーが継承する設定です。本やフォルダーは、それぞれの設定で独自の値を指定できます。ペルソナとメディアはあなた専用で、本には独自のペルソナを追加できます。");
        zh(WHAT_BOOKS_AND_FOLDERS_INHERIT,
                "书籍和文件夹继承的设置：书籍或文件夹可以在各自的设置中指定自己的值。读者角色和媒介属于你，书籍还可以添加自己的读者角色。");
        fr(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Ce dont héritent les livres et les dossiers : un livre ou un dossier peut définir le sien dans ses paramètres. Les personas et les supports sont à vous, un livre peut ajouter ses propres personas.");
        es(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Lo que heredan los libros y las carpetas: un libro o una carpeta puede definir lo suyo en sus ajustes. Los lectores y los medios son tuyos, un libro puede añadir lectores propios.");
        de(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Was Bücher und Ordner erben: Ein Buch oder ein Ordner kann in seinen Einstellungen eigene festlegen. Die Personas und die Medien gehören dir, ein Buch kann eigene Personas hinzufügen.");

        ja(EMPTY_THE_BUILT_IN_DEFAULT, "空の場合: 組み込みの既定値");
        zh(EMPTY_THE_BUILT_IN_DEFAULT, "留空：使用内置默认值");
        fr(EMPTY_THE_BUILT_IN_DEFAULT, "Vide : la valeur intégrée par défaut");
        es(EMPTY_THE_BUILT_IN_DEFAULT, "Vacío: el valor predeterminado integrado");
        de(EMPTY_THE_BUILT_IN_DEFAULT, "Leer: der eingebaute Standard");

        ja(EMPTY_THE_MODEL, "空の場合: 本のモデル");
        zh(EMPTY_THE_MODEL, "留空：使用书籍的模型");
        fr(EMPTY_THE_MODEL, "Vide : le modèle du livre");
        es(EMPTY_THE_MODEL, "Vacío: el modelo del libro");
        de(EMPTY_THE_MODEL, "Leer: das Modell des Buchs");

        ja(EMPTY_THE_PROTOCOL, "空の場合: 本のプロトコル");
        zh(EMPTY_THE_PROTOCOL, "留空：使用书籍的协议");
        fr(EMPTY_THE_PROTOCOL, "Vide : le protocole du livre");
        es(EMPTY_THE_PROTOCOL, "Vacío: el protocolo del libro");
        de(EMPTY_THE_PROTOCOL, "Leer: das Protokoll des Buchs");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajouter");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");
        fr(EDIT, "Modifier");
        es(EDIT, "Editar");
        de(EDIT, "Bearbeiten");

        ja(COPY, "コピー");
        zh(COPY, "复制");
        fr(COPY, "Copier");
        es(COPY, "Copiar");
        de(COPY, "Kopieren");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");

        ja(DELETE_S, "「%s」を削除しますか？");
        zh(DELETE_S, "删除“%s”？");
        fr(DELETE_S, "Supprimer « %s » ?");
        es(DELETE_S, "¿Eliminar \"%s\"?");
        de(DELETE_S, "„%s“ löschen?");

        ja(MOVE_UP, "上へ移動");
        zh(MOVE_UP, "上移");
        fr(MOVE_UP, "Monter");
        es(MOVE_UP, "Subir");
        de(MOVE_UP, "Nach oben");

        ja(MOVE_DOWN, "下へ移動");
        zh(MOVE_DOWN, "下移");
        fr(MOVE_DOWN, "Descendre");
        es(MOVE_DOWN, "Bajar");
        de(MOVE_DOWN, "Nach unten");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "描述");
        fr(DESCRIPTION, "Description");
        es(DESCRIPTION, "Descripción");
        de(DESCRIPTION, "Beschreibung");

        ja(ICON, "アイコン");
        zh(ICON, "图标");
        fr(ICON, "Icône");
        es(ICON, "Icono");
        de(ICON, "Symbol");

        ja(UPLOAD_ICON, "アイコンをアップロード");
        zh(UPLOAD_ICON, "上传图标");
        fr(UPLOAD_ICON, "Téléverser une icône");
        es(UPLOAD_ICON, "Subir icono");
        de(UPLOAD_ICON, "Symbol hochladen");

        ja(REMOVE_ICON, "アイコンを削除");
        zh(REMOVE_ICON, "移除图标");
        fr(REMOVE_ICON, "Retirer l'icône");
        es(REMOVE_ICON, "Quitar icono");
        de(REMOVE_ICON, "Symbol entfernen");

        ja(ICON_SCALED_DOWN,
                "アイコン（最大 %1$d x %1$d ピクセルに縮小されます。ない場合は名前の最初の文字を入れた円が表示されます）");
        zh(ICON_SCALED_DOWN,
                "图标（缩小到最多 %1$d x %1$d 像素；没有图标时显示带有名称首字母的圆形）");
        fr(ICON_SCALED_DOWN, "Icône (réduite à %1$d x %1$d pixels au plus ; sans icône, un cercle avec les premières lettres du nom est affiché)");
        es(ICON_SCALED_DOWN, "Icono (reducido a un máximo de %1$d x %1$d píxeles; sin icono, se muestra un círculo con las primeras letras del nombre)");
        de(ICON_SCALED_DOWN, "Symbol (auf höchstens %1$d x %1$d Pixel verkleinert; ohne eines wird ein Kreis mit den ersten Buchstaben des Namens angezeigt)");

        ja(THE_FILE, "ファイルは読み取れる画像ではありません");
        zh(THE_FILE, "该文件不是可读取的图片");
        fr(THE_FILE, "Le fichier n'est pas une image lisible");
        es(THE_FILE, "El archivo no es una imagen que se pueda leer");
        de(THE_FILE, "Die Datei ist kein lesbares Bild");

        ja(THE_FILE_IS_NOT_AN_IMAGE, "ファイルが画像ではないか、大きすぎます");
        zh(THE_FILE_IS_NOT_AN_IMAGE, "该文件不是图片或太大");
        fr(THE_FILE_IS_NOT_AN_IMAGE, "Le fichier n'est pas une image ou il est trop gros");
        es(THE_FILE_IS_NOT_AN_IMAGE, "El archivo no es una imagen o es demasiado grande");
        de(THE_FILE_IS_NOT_AN_IMAGE, "Die Datei ist kein Bild oder sie ist zu groß");

        ja(THE_NAME_IS_REQUIRED, "名前は必須です");
        zh(THE_NAME_IS_REQUIRED, "名称为必填项");
        fr(THE_NAME_IS_REQUIRED, "Le nom est obligatoire");
        es(THE_NAME_IS_REQUIRED, "El nombre es obligatorio");
        de(THE_NAME_IS_REQUIRED, "Der Name ist erforderlich");

        ja(ANOTHER_PERSONA_HAS_THIS_NAME, "この名前のペルソナが既にあります");
        zh(ANOTHER_PERSONA_HAS_THIS_NAME, "已有同名的读者角色");
        fr(ANOTHER_PERSONA_HAS_THIS_NAME, "Un autre persona porte ce nom");
        es(ANOTHER_PERSONA_HAS_THIS_NAME, "Otro lector tiene este nombre");
        de(ANOTHER_PERSONA_HAS_THIS_NAME, "Eine andere Persona hat diesen Namen");

        ja(ANOTHER_MEDIUM_HAS_THIS_NAME, "この名前のメディアが既にあります");
        zh(ANOTHER_MEDIUM_HAS_THIS_NAME, "已有同名的媒介");
        fr(ANOTHER_MEDIUM_HAS_THIS_NAME, "Un autre support porte ce nom");
        es(ANOTHER_MEDIUM_HAS_THIS_NAME, "Otro medio tiene este nombre");
        de(ANOTHER_MEDIUM_HAS_THIS_NAME, "Ein anderes Medium hat diesen Namen");

        ja(NEW_PERSONA, "新しいペルソナ");
        zh(NEW_PERSONA, "新建读者角色");
        fr(NEW_PERSONA, "Nouveau persona");
        es(NEW_PERSONA, "Lector nuevo");
        de(NEW_PERSONA, "Neue Persona");

        ja(EDIT_PERSONA, "ペルソナを編集");
        zh(EDIT_PERSONA, "编辑读者角色");
        fr(EDIT_PERSONA, "Modifier le persona");
        es(EDIT_PERSONA, "Editar lector");
        de(EDIT_PERSONA, "Persona bearbeiten");

        ja(NEW_MEDIUM, "新しいメディア");
        zh(NEW_MEDIUM, "新建媒介");
        fr(NEW_MEDIUM, "Nouveau support");
        es(NEW_MEDIUM, "Medio nuevo");
        de(NEW_MEDIUM, "Neues Medium");

        ja(EDIT_MEDIUM, "メディアを編集");
        zh(EDIT_MEDIUM, "编辑媒介");
        fr(EDIT_MEDIUM, "Modifier le support");
        es(EDIT_MEDIUM, "Editar medio");
        de(EDIT_MEDIUM, "Medium bearbeiten");

        ja(HOW_THE_PERSONA_READS,
                "ペルソナがどう読み、何を重視するか。モデルが演じる内容です");
        zh(HOW_THE_PERSONA_READS,
                "该读者角色如何阅读、关注什么；模型将扮演它");
        fr(HOW_THE_PERSONA_READS, "Comment le persona lit et ce qui lui importe ; c'est ce que le modèle joue");
        es(HOW_THE_PERSONA_READS, "Cómo lee el lector y qué le importa; es lo que interpreta el modelo");
        de(HOW_THE_PERSONA_READS, "Wie die Persona liest und was ihr wichtig ist; das spielt das Modell");

        ja(GOES_INTO_THE_PROMPT,
                "チャットのプロンプトに入ります: テキストが何を表すか、ペルソナがそれにどう反応するか");
        zh(GOES_INTO_THE_PROMPT,
                "会加入聊天的提示词：文本代表什么，以及读者角色如何对其做出反应");
        fr(GOES_INTO_THE_PROMPT, "Va dans le prompt des discussions : ce que représente le texte et comment les personas y réagissent");
        es(GOES_INTO_THE_PROMPT, "Va al prompt de los chats: qué representa el texto y cómo reaccionan a él los lectores");
        de(GOES_INTO_THE_PROMPT, "Geht in den Prompt der Chats: wofür der Text steht und wie die Personas darauf reagieren");

        ja(ADD_THE_STARTER_PERSONAS, "初期ペルソナを追加");
        zh(ADD_THE_STARTER_PERSONAS, "添加预设读者角色");
        fr(ADD_THE_STARTER_PERSONAS, "Ajouter les personas de départ");
        es(ADD_THE_STARTER_PERSONAS, "Añadir los lectores iniciales");
        de(ADD_THE_STARTER_PERSONAS, "Die Starter-Personas hinzufügen");

        ja(ADDS_THE_PERSONAS_THE_PLUGIN,
                "拡張機能に付属するペルソナのうち、リストにないものを追加します");
        zh(ADDS_THE_PERSONAS_THE_PLUGIN,
                "添加扩展自带但不在列表中的读者角色");
        fr(ADDS_THE_PERSONAS_THE_PLUGIN, "Ajoute les personas fournis avec le plugin qui ne sont pas dans la liste");
        es(ADDS_THE_PERSONAS_THE_PLUGIN, "Añade los lectores que trae el plugin y que no están en la lista");
        de(ADDS_THE_PERSONAS_THE_PLUGIN, "Fügt die Personas hinzu, die das Plugin mitbringt und die nicht in der Liste sind");

        ja(ALL_THE_STARTER_PERSONAS, "初期ペルソナはすべてリストにあります");
        zh(ALL_THE_STARTER_PERSONAS, "所有预设读者角色都已在列表中");
        fr(ALL_THE_STARTER_PERSONAS, "Tous les personas de départ sont dans la liste");
        es(ALL_THE_STARTER_PERSONAS, "Todos los lectores iniciales están en la lista");
        de(ALL_THE_STARTER_PERSONAS, "Alle Starter-Personas sind in der Liste");

        ja(RESTORE_THE_BUILT_IN_MEDIUMS, "組み込みのメディアを復元");
        zh(RESTORE_THE_BUILT_IN_MEDIUMS, "恢复内置媒介");
        fr(RESTORE_THE_BUILT_IN_MEDIUMS, "Rétablir les supports intégrés");
        es(RESTORE_THE_BUILT_IN_MEDIUMS, "Restaurar los medios integrados");
        de(RESTORE_THE_BUILT_IN_MEDIUMS, "Die eingebauten Medien wiederherstellen");

        ja(ADDS_THE_MEDIUMS_THE_PLUGIN,
                "拡張機能に付属するメディアのうち、リストにないものを追加します");
        zh(ADDS_THE_MEDIUMS_THE_PLUGIN,
                "添加扩展自带但不在列表中的媒介");
        fr(ADDS_THE_MEDIUMS_THE_PLUGIN, "Ajoute les supports fournis avec le plugin qui ne sont pas dans la liste");
        es(ADDS_THE_MEDIUMS_THE_PLUGIN, "Añade los medios que trae el plugin y que no están en la lista");
        de(ADDS_THE_MEDIUMS_THE_PLUGIN, "Fügt die Medien hinzu, die das Plugin mitbringt und die nicht in der Liste sind");

        ja(ALL_THE_BUILT_IN_MEDIUMS, "組み込みのメディアはすべてリストにあります");
        zh(ALL_THE_BUILT_IN_MEDIUMS, "所有内置媒介都已在列表中");
        fr(ALL_THE_BUILT_IN_MEDIUMS, "Tous les supports intégrés sont dans la liste");
        es(ALL_THE_BUILT_IN_MEDIUMS, "Todos los medios integrados están en la lista");
        de(ALL_THE_BUILT_IN_MEDIUMS, "Alle eingebauten Medien sind in der Liste");

        ja(BUILT, "組み込み");
        zh(BUILT, "内置");
        fr(BUILT, "intégré");
        es(BUILT, "integrado");
        de(BUILT, "eingebaut");

        ja(TITLE,
                "タイトル");
        zh(TITLE,
                "标题");
        fr(TITLE, "Titre");
        es(TITLE, "Título");
        de(TITLE, "Titel");

        ja(PERSONA,
                "ペルソナ");
        zh(PERSONA,
                "读者角色");
        fr(PERSONA, "Persona");
        es(PERSONA, "Lector");
        de(PERSONA, "Persona");

        ja(PERSONAS,
                "ペルソナ");
        zh(PERSONAS,
                "读者角色");
        fr(PERSONAS, "Personas");
        es(PERSONAS, "Lectores");
        de(PERSONAS, "Personas");

        ja(MODEL,
                "モデル");
        zh(MODEL,
                "模型");
        fr(MODEL, "Modèle");
        es(MODEL, "Modelo");
        de(MODEL, "Modell");

        ja(PROTOCOL,
                "プロトコル");
        zh(PROTOCOL,
                "协议");
        fr(PROTOCOL, "Protocole");
        es(PROTOCOL, "Protocolo");
        de(PROTOCOL, "Protokoll");

        ja(NEW_CHAT,
                "新しいチャット");
        zh(NEW_CHAT,
                "新建聊天");
        fr(NEW_CHAT, "Nouvelle discussion");
        es(NEW_CHAT, "Chat nuevo");
        de(NEW_CHAT, "Neuer Chat");

        ja(KIND,
                "種類");
        zh(KIND,
                "类型");
        fr(KIND, "Genre");
        es(KIND, "Tipo");
        de(KIND, "Art");

        ja(ONE_PERSONA,
                "1人のペルソナ");
        zh(ONE_PERSONA,
                "单个读者角色");
        fr(ONE_PERSONA, "Un persona");
        es(ONE_PERSONA, "Un lector");
        de(ONE_PERSONA, "Eine Persona");

        ja(A_GROUP,
                "グループ");
        zh(A_GROUP,
                "群组");
        fr(A_GROUP, "Un groupe");
        es(A_GROUP, "Un grupo");
        de(A_GROUP, "Eine Gruppe");

        ja(A_CHAT_WITH_ONE_PERSONA_STAYS,
                "ペルソナ1人とのチャットはそのままです。グループは1人のペルソナで始めることができ、後から他のペルソナが参加できます。");
        zh(A_CHAT_WITH_ONE_PERSONA_STAYS,
                "与单个读者角色的聊天始终如此。群组可以从一个读者角色开始，之后其他角色可以加入。");
        fr(A_CHAT_WITH_ONE_PERSONA_STAYS, "Une discussion avec un persona le reste. Un groupe peut commencer avec un persona, et d'autres peuvent le rejoindre plus tard.");
        es(A_CHAT_WITH_ONE_PERSONA_STAYS, "Un chat con un lector se queda así. Un grupo puede empezar con un lector, y otros pueden unirse después.");
        de(A_CHAT_WITH_ONE_PERSONA_STAYS, "Ein Chat mit einer Persona bleibt so. Eine Gruppe kann mit einer Persona beginnen, und andere können später dazukommen.");

        ja(EMPTY_THE_NAMES,
                "空の場合: ペルソナの名前");
        zh(EMPTY_THE_NAMES,
                "留空：使用读者角色的名称");
        fr(EMPTY_THE_NAMES, "Vide : les noms des personas");
        es(EMPTY_THE_NAMES, "Vacío: los nombres de los lectores");
        de(EMPTY_THE_NAMES, "Leer: die Namen der Personas");

        ja(REPLY_FORMAT,
                "返信の形式");
        zh(REPLY_FORMAT,
                "回复格式");
        fr(REPLY_FORMAT, "Format des réponses");
        es(REPLY_FORMAT, "Formato de la respuesta");
        de(REPLY_FORMAT, "Antwortformat");

        ja(AUTOMATIC_ASKS_FOR_JSON,
                "自動: モデルがスキーマに従える場合はスキーマ付きの JSON、それ以外は自由テキストを要求します");
        zh(AUTOMATIC_ASKS_FOR_JSON,
                "自动：模型能遵循模式时要求带模式的 JSON，否则使用自由文本");
        fr(AUTOMATIC_ASKS_FOR_JSON, "Automatique demande du JSON selon un schéma quand le modèle peut en suivre un, du texte libre sinon");
        es(AUTOMATIC_ASKS_FOR_JSON, "Automático pide JSON con un esquema cuando el modelo puede seguir uno, y texto libre si no");
        de(AUTOMATIC_ASKS_FOR_JSON, "„Automatisch“ verlangt JSON in einem Schema, wenn das Modell einem folgen kann, sonst freien Text");

        ja(JSON_WITHOUT_A_SCHEMA,
                "スキーマなしの JSON");
        zh(JSON_WITHOUT_A_SCHEMA,
                "不带模式的 JSON");
        fr(JSON_WITHOUT_A_SCHEMA, "JSON sans schéma");
        es(JSON_WITHOUT_A_SCHEMA, "JSON sin esquema");
        de(JSON_WITHOUT_A_SCHEMA, "JSON ohne Schema");

        ja(JSON_WITHOUT_A_SCHEMA_HELP,
                "スキーマなしの JSON は、スキーマとモデルの思考を同時に扱えないサーバー向けです");
        zh(JSON_WITHOUT_A_SCHEMA_HELP,
                "不带模式的 JSON 适用于无法同时处理模式和模型思考的服务器");
        fr(JSON_WITHOUT_A_SCHEMA_HELP, "JSON sans schéma est pour un serveur qui ne peut pas accepter un schéma en même temps que la réflexion du modèle");
        es(JSON_WITHOUT_A_SCHEMA_HELP, "JSON sin esquema es para un servidor que no puede aceptar un esquema junto con el razonamiento del modelo");
        de(JSON_WITHOUT_A_SCHEMA_HELP, "JSON ohne Schema ist für einen Server, der ein Schema nicht zusammen mit dem Denken des Modells annehmen kann");

        ja(CREATE,
                "作成");
        zh(CREATE,
                "创建");
        fr(CREATE, "Créer");
        es(CREATE, "Crear");
        de(CREATE, "Erstellen");

        ja(INHERITED,
                "継承: %s");
        zh(INHERITED,
                "继承：%s");
        fr(INHERITED, "Hérité : %s");
        es(INHERITED, "Heredado: %s");
        de(INHERITED, "Geerbt: %s");

        ja(NONE,
                "なし");
        zh(NONE,
                "无");
        fr(NONE, "aucun");
        es(NONE, "ninguno");
        de(NONE, "keine");

        ja(THIS_BOOK,
                "この本");
        zh(THIS_BOOK,
                "本书");
        fr(THIS_BOOK, "ce livre");
        es(THIS_BOOK, "este libro");
        de(THIS_BOOK, "dieses Buch");

        ja(AUTOMATIC,
                "自動");
        zh(AUTOMATIC,
                "自动");
        fr(AUTOMATIC, "Automatique");
        es(AUTOMATIC, "Automático");
        de(AUTOMATIC, "Automatisch");

        ja(JSON,
                "JSON");
        zh(JSON,
                "JSON");
        fr(JSON, "JSON");
        es(JSON, "JSON");
        de(JSON, "JSON");

        ja(FREE_TEXT,
                "自由テキスト");
        zh(FREE_TEXT,
                "自由文本");
        fr(FREE_TEXT, "Texte libre");
        es(FREE_TEXT, "Texto libre");
        de(FREE_TEXT, "Freier Text");

        ja(CHOOSE_A_PERSONA,
                "ペルソナを選択してください");
        zh(CHOOSE_A_PERSONA,
                "请选择一个读者角色");
        fr(CHOOSE_A_PERSONA, "Choisissez un persona");
        es(CHOOSE_A_PERSONA, "Elige un lector");
        de(CHOOSE_A_PERSONA, "Wähle eine Persona");

        ja(CHOOSE_AT_LEAST_ONE_PERSONA,
                "ペルソナを1人以上選択してください");
        zh(CHOOSE_AT_LEAST_ONE_PERSONA,
                "请至少选择一个读者角色");
        fr(CHOOSE_AT_LEAST_ONE_PERSONA, "Choisissez au moins un persona");
        es(CHOOSE_AT_LEAST_ONE_PERSONA, "Elige al menos un lector");
        de(CHOOSE_AT_LEAST_ONE_PERSONA, "Wähle mindestens eine Persona");

        ja(A_CHAT_WITH_ONE_PERSONA,
                "ペルソナ1人とのチャット");
        zh(A_CHAT_WITH_ONE_PERSONA,
                "与单个读者角色的聊天");
        fr(A_CHAT_WITH_ONE_PERSONA, "Une discussion avec un persona");
        es(A_CHAT_WITH_ONE_PERSONA, "Un chat con un lector");
        de(A_CHAT_WITH_ONE_PERSONA, "Ein Chat mit einer Persona");

        ja(A_GROUP_CHAT,
                "グループチャット");
        zh(A_GROUP_CHAT,
                "群聊");
        fr(A_GROUP_CHAT, "Une discussion de groupe");
        es(A_GROUP_CHAT, "Un chat de grupo");
        de(A_GROUP_CHAT, "Ein Gruppenchat");

        ja(MEDIUM_S,
                "メディア: %s");
        zh(MEDIUM_S,
                "媒介：%s");
        fr(MEDIUM_S, "Support : %s");
        es(MEDIUM_S, "Medio: %s");
        de(MEDIUM_S, "Medium: %s");

        ja(MODEL_S,
                "モデル: %s");
        zh(MODEL_S,
                "模型：%s");
        fr(MODEL_S, "Modèle : %s");
        es(MODEL_S, "Modelo: %s");
        de(MODEL_S, "Modell: %s");

        ja(LEFT,
                "退出済み");
        zh(LEFT,
                "已离开");
        fr(LEFT, "parti");
        es(LEFT, "se fue");
        de(LEFT, "gegangen");

        ja(CHATS,
                "チャット");
        zh(CHATS,
                "聊天");
        fr(CHATS, "Discussions");
        es(CHATS, "Chats");
        de(CHATS, "Chats");

        ja(RENAME_CHAT,
                "チャット名を変更");
        zh(RENAME_CHAT,
                "重命名聊天");
        fr(RENAME_CHAT, "Renommer la discussion");
        es(RENAME_CHAT, "Renombrar chat");
        de(RENAME_CHAT, "Chat umbenennen");

        ja(CLOSE_CHAT,
                "チャットを閉じる");
        zh(CLOSE_CHAT,
                "关闭聊天");
        fr(CLOSE_CHAT, "Fermer la discussion");
        es(CLOSE_CHAT, "Cerrar chat");
        de(CLOSE_CHAT, "Chat schließen");

        ja(DELETE_CHAT,
                "チャットを削除");
        zh(DELETE_CHAT,
                "删除聊天");
        fr(DELETE_CHAT, "Supprimer la discussion");
        es(DELETE_CHAT, "Eliminar chat");
        de(DELETE_CHAT, "Chat löschen");

        ja(PERSONAS_OF_THIS_BOOK,
                "この本のペルソナ");
        zh(PERSONAS_OF_THIS_BOOK,
                "本书的读者角色");
        fr(PERSONAS_OF_THIS_BOOK, "Personas de ce livre");
        es(PERSONAS_OF_THIS_BOOK, "Lectores de este libro");
        de(PERSONAS_OF_THIS_BOOK, "Personas dieses Buchs");

        ja(CLOSED_CHATS,
                "閉じたチャット");
        zh(CLOSED_CHATS,
                "已关闭的聊天");
        fr(CLOSED_CHATS, "Discussions fermées");
        es(CLOSED_CHATS, "Chats cerrados");
        de(CLOSED_CHATS, "Geschlossene Chats");

        ja(NO_CHATS_YET_START_ONE,
                "チャットはまだありません。+ ボタンで開始してください。");
        zh(NO_CHATS_YET_START_ONE,
                "还没有聊天。点击 + 按钮开始。");
        fr(NO_CHATS_YET_START_ONE, "Pas encore de discussions. Commencez-en une avec le bouton +.");
        es(NO_CHATS_YET_START_ONE, "Todavía no hay chats. Inicia uno con el botón +.");
        de(NO_CHATS_YET_START_ONE, "Noch keine Chats. Starte einen mit der +-Schaltfläche.");

        ja(THERE_IS_NO_PERSONA_TO_TALK,
                "話し相手のペルソナがありません。設定のライブラリか、この本に追加してください。");
        zh(THERE_IS_NO_PERSONA_TO_TALK,
                "没有可交谈的读者角色。请在设置的角色库或本书中添加。");
        fr(THERE_IS_NO_PERSONA_TO_TALK, "Il n'y a aucun persona avec qui parler. Ajoutez-en à la bibliothèque dans les paramètres, ou à ce livre.");
        es(THERE_IS_NO_PERSONA_TO_TALK, "No hay ningún lector con quien hablar. Añade algunos a la biblioteca en los ajustes, o a este libro.");
        de(THERE_IS_NO_PERSONA_TO_TALK, "Es gibt keine Persona, mit der man sprechen kann. Füge der Bibliothek in den Einstellungen oder diesem Buch welche hinzu.");


        ja(JOINED_THE_CONVERSATION,
                "%s が会話に参加しました。");
        zh(JOINED_THE_CONVERSATION,
                "%s 加入了对话。");
        fr(JOINED_THE_CONVERSATION, "%s a rejoint la conversation.");
        es(JOINED_THE_CONVERSATION, "%s se ha unido a la conversación.");
        de(JOINED_THE_CONVERSATION, "%s ist dem Gespräch beigetreten.");

        ja(LEFT_THE_CONVERSATION,
                "%s が会話から退出しました。");
        zh(LEFT_THE_CONVERSATION,
                "%s 离开了对话。");
        fr(LEFT_THE_CONVERSATION, "%s a quitté la conversation.");
        es(LEFT_THE_CONVERSATION, "%s ha salido de la conversación.");
        de(LEFT_THE_CONVERSATION, "%s hat das Gespräch verlassen.");

        ja(YOU,
                "あなた");
        zh(YOU,
                "你");
        fr(YOU, "Vous");
        es(YOU, "Tú");
        de(YOU, "Du");

        ja(PARTS_SENT,
                "送信したパート: %s");
        zh(PARTS_SENT,
                "已发送的部分：%s");
        fr(PARTS_SENT, "Parties %s envoyées");
        es(PARTS_SENT, "Partes %s enviadas");
        de(PARTS_SENT, "Abschnitte %s gesendet");

        ja(REASONING,
                "推論");
        zh(REASONING,
                "推理");
        fr(REASONING, "Raisonnement");
        es(REASONING, "Razonamiento");
        de(REASONING, "Reasoning");

        ja(NARRATOR,
                "ナレーター");
        zh(NARRATOR,
                "旁白");
        fr(NARRATOR, "Narrateur");
        es(NARRATOR, "Narrador");
        de(NARRATOR, "Erzähler");

        ja(PERSONAS_COUNT,
                "ペルソナ (%d)");
        zh(PERSONAS_COUNT,
                "读者角色 (%d)");
        fr(PERSONAS_COUNT, "Personas (%d)");
        es(PERSONAS_COUNT, "Lectores (%d)");
        de(PERSONAS_COUNT, "Personas (%d)");

        ja(WRITE_TO_THE_BETA_READERS,
                "ベータリーダーに書く...");
        zh(WRITE_TO_THE_BETA_READERS,
                "写给试读者……");
        fr(WRITE_TO_THE_BETA_READERS, "Écrivez aux bêta-lecteurs...");
        es(WRITE_TO_THE_BETA_READERS, "Escribe a los lectores beta...");
        de(WRITE_TO_THE_BETA_READERS, "An die Betaleser schreiben ...");

        ja(INCLUDE_THE_LATEST_PARTS,
                "最新のパートを含める");
        zh(INCLUDE_THE_LATEST_PARTS,
                "包含最新的部分");
        fr(INCLUDE_THE_LATEST_PARTS, "Inclure les dernières parties");
        es(INCLUDE_THE_LATEST_PARTS, "Incluir las últimas partes");
        de(INCLUDE_THE_LATEST_PARTS, "Die letzten Abschnitte einbeziehen");

        ja(SEND,
                "送信");
        zh(SEND,
                "发送");
        fr(SEND, "Envoyer");
        es(SEND, "Enviar");
        de(SEND, "Senden");

        ja(STOP,
                "停止");
        zh(STOP,
                "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");

        ja(THERE_IS_NO_MODEL,
                "問い合わせるモデルがありません。チャット、本、または設定でモデルを選択してください。");
        zh(THERE_IS_NO_MODEL,
                "没有可询问的模型。请在聊天、书籍或设置中选择一个。");
        fr(THERE_IS_NO_MODEL, "Il n'y a pas de modèle à interroger. Choisissez-en un pour la discussion, le livre ou dans les paramètres.");
        es(THERE_IS_NO_MODEL, "No hay modelo al que preguntar. Elige uno para el chat, el libro o en los ajustes.");
        de(THERE_IS_NO_MODEL, "Es gibt kein Modell, das gefragt werden kann. Wähle eines für den Chat, das Buch oder in den Einstellungen.");

        ja(WRITE_A_MESSAGE_OR_INCLUDE,
                "メッセージを書くか、最新のパートを含めてください。");
        zh(WRITE_A_MESSAGE_OR_INCLUDE,
                "请写一条消息，或包含最新的部分。");
        fr(WRITE_A_MESSAGE_OR_INCLUDE, "Écrivez un message ou incluez les dernières parties.");
        es(WRITE_A_MESSAGE_OR_INCLUDE, "Escribe un mensaje o incluye las últimas partes.");
        de(WRITE_A_MESSAGE_OR_INCLUDE, "Schreibe eine Nachricht oder beziehe die letzten Abschnitte ein.");

        ja(CANT_ASK_THE_MODEL,
                "モデルに問い合わせできません: %s");
        zh(CANT_ASK_THE_MODEL,
                "无法询问模型：%s");
        fr(CANT_ASK_THE_MODEL, "Impossible d'interroger le modèle : %s");
        es(CANT_ASK_THE_MODEL, "No se puede consultar al modelo: %s");
        de(CANT_ASK_THE_MODEL, "Das Modell kann nicht gefragt werden: %s");

        ja(THE_MODEL_DID_NOT_ANSWER,
                "モデルは応答しませんでした。");
        zh(THE_MODEL_DID_NOT_ANSWER,
                "模型没有回答。");
        fr(THE_MODEL_DID_NOT_ANSWER, "Le modèle n'a pas répondu.");
        es(THE_MODEL_DID_NOT_ANSWER, "El modelo no ha respondido.");
        de(THE_MODEL_DID_NOT_ANSWER, "Das Modell hat nicht geantwortet.");

        ja(OLDER_MESSAGES_WERE_LEFT_OUT,
                "コンテキストに収めるため、古いメッセージ %d 件をリクエストから省きました。");
        zh(OLDER_MESSAGES_WERE_LEFT_OUT,
                "为适应上下文，请求中省略了 %d 条较早的消息。");
        fr(OLDER_MESSAGES_WERE_LEFT_OUT, "%d messages plus anciens ont été omis de la requête pour tenir dans le contexte.");
        es(OLDER_MESSAGES_WERE_LEFT_OUT, "Se omitieron %d mensajes más antiguos de la solicitud para ajustarse al contexto.");
        de(OLDER_MESSAGES_WERE_LEFT_OUT, "%d ältere Nachrichten wurden aus der Anfrage weggelassen, damit sie in den Kontext passt.");

        ja(INCLUDE_THE_STORY_PARTS,
                "ストーリーを含める (%d パート)");
        zh(INCLUDE_THE_STORY_PARTS,
                "包含故事（%d 个部分）");
        fr(INCLUDE_THE_STORY_PARTS, "Inclure l'histoire (%d parties)");
        es(INCLUDE_THE_STORY_PARTS, "Incluir la historia (%d partes)");
        de(INCLUDE_THE_STORY_PARTS, "Die Geschichte einbeziehen (%d Abschnitte)");

        ja(INCLUDE_THE_LATEST_PARTS_NEW,
                "最新のパートを含める (新規 %d)");
        zh(INCLUDE_THE_LATEST_PARTS_NEW,
                "包含最新的部分（新增 %d）");
        fr(INCLUDE_THE_LATEST_PARTS_NEW, "Inclure les dernières parties (%d nouvelles)");
        es(INCLUDE_THE_LATEST_PARTS_NEW, "Incluir las últimas partes (%d nuevas)");
        de(INCLUDE_THE_LATEST_PARTS_NEW, "Die letzten Abschnitte einbeziehen (%d neu)");

        ja(INCLUDE_THE_LATEST_PARTS_NONE,
                "最新のパートを含める (新しいものなし)");
        zh(INCLUDE_THE_LATEST_PARTS_NONE,
                "包含最新的部分（没有新的）");
        fr(INCLUDE_THE_LATEST_PARTS_NONE, "Inclure les dernières parties (aucune nouvelle)");
        es(INCLUDE_THE_LATEST_PARTS_NONE, "Incluir las últimas partes (ninguna nueva)");
        de(INCLUDE_THE_LATEST_PARTS_NONE, "Die letzten Abschnitte einbeziehen (keine neuen)");

        ja(CONTEXT_OF_TOKENS,
                "コンテキスト: %1$s / %2$s トークン");
        zh(CONTEXT_OF_TOKENS,
                "上下文：%1$s / %2$s 个词元");
        fr(CONTEXT_OF_TOKENS, "Contexte : %1$s jetons sur %2$s");
        es(CONTEXT_OF_TOKENS, "Contexto: %1$s de %2$s tokens");
        de(CONTEXT_OF_TOKENS, "Kontext: %1$s von %2$s Tokens");

        ja(CONTEXT_TOKENS,
                "コンテキスト: %s トークン");
        zh(CONTEXT_TOKENS,
                "上下文：%s 个词元");
        fr(CONTEXT_TOKENS, "Contexte : %s jetons");
        es(CONTEXT_TOKENS, "Contexto: %s tokens");
        de(CONTEXT_TOKENS, "Kontext: %s Tokens");

        ja(REGENERATE,
                "再生成");
        zh(REGENERATE,
                "重新生成");
        fr(REGENERATE, "Régénérer");
        es(REGENERATE, "Regenerar");
        de(REGENERATE, "Neu generieren");

        ja(THERE_IS_NO_REPLY_YET,
                "まだ返信がありません。");
        zh(THERE_IS_NO_REPLY_YET,
                "还没有回复。");
        fr(THERE_IS_NO_REPLY_YET, "Il n'y a pas encore de réponse.");
        es(THERE_IS_NO_REPLY_YET, "Todavía no hay respuesta.");
        de(THERE_IS_NO_REPLY_YET, "Es gibt noch keine Antwort.");

        ja(ASK_AGAIN,
                "もう一度尋ねる");
        zh(ASK_AGAIN,
                "再次询问");
        fr(ASK_AGAIN, "Redemander");
        es(ASK_AGAIN, "Preguntar de nuevo");
        de(ASK_AGAIN, "Erneut fragen");

        ja(EDIT_MESSAGE,
                "メッセージを編集");
        zh(EDIT_MESSAGE,
                "编辑消息");
        fr(EDIT_MESSAGE, "Modifier le message");
        es(EDIT_MESSAGE, "Editar mensaje");
        de(EDIT_MESSAGE, "Nachricht bearbeiten");

        ja(DELETE_MESSAGE,
                "メッセージを削除");
        zh(DELETE_MESSAGE,
                "删除消息");
        fr(DELETE_MESSAGE, "Supprimer le message");
        es(DELETE_MESSAGE, "Eliminar mensaje");
        de(DELETE_MESSAGE, "Nachricht löschen");

        ja(DELETE_THIS_MESSAGE,
                "このメッセージを削除しますか？");
        zh(DELETE_THIS_MESSAGE,
                "删除这条消息？");
        fr(DELETE_THIS_MESSAGE, "Supprimer ce message ?");
        es(DELETE_THIS_MESSAGE, "¿Eliminar este mensaje?");
        de(DELETE_THIS_MESSAGE, "Diese Nachricht löschen?");

        ja(BRING_IN_A_PERSONA,
                "ペルソナを呼ぶ");
        zh(BRING_IN_A_PERSONA,
                "邀请读者角色加入");
        fr(BRING_IN_A_PERSONA, "Faire entrer un persona");
        es(BRING_IN_A_PERSONA, "Incorporar un lector");
        de(BRING_IN_A_PERSONA, "Eine Persona hinzuholen");

        ja(BRING,
                "呼ぶ");
        zh(BRING,
                "邀请");
        fr(BRING, "Faire entrer");
        es(BRING, "Incorporar");
        de(BRING, "Hinzuholen");

        ja(SEND_AWAY,
                "退出させる");
        zh(SEND_AWAY,
                "请其离开");
        fr(SEND_AWAY, "Renvoyer");
        es(SEND_AWAY, "Despedir");
        de(SEND_AWAY, "Wegschicken");

        ja(SEND_AWAY_S,
                "%s を退出させる");
        zh(SEND_AWAY_S,
                "请 %s 离开");
        fr(SEND_AWAY_S, "Renvoyer %s");
        es(SEND_AWAY_S, "Despedir a %s");
        de(SEND_AWAY_S, "%s wegschicken");

        ja(UPDATE_FROM_THE_LIBRARY,
                "ライブラリから更新");
        zh(UPDATE_FROM_THE_LIBRARY,
                "从角色库更新");
        fr(UPDATE_FROM_THE_LIBRARY, "Mettre à jour depuis la bibliothèque");
        es(UPDATE_FROM_THE_LIBRARY, "Actualizar desde la biblioteca");
        de(UPDATE_FROM_THE_LIBRARY, "Aus der Bibliothek aktualisieren");

        ja(IS_UPDATED_FROM_THE_LIBRARY,
                "%s をライブラリから更新しました。");
        zh(IS_UPDATED_FROM_THE_LIBRARY,
                "已从角色库更新 %s。");
        fr(IS_UPDATED_FROM_THE_LIBRARY, "%s est mis à jour depuis la bibliothèque.");
        es(IS_UPDATED_FROM_THE_LIBRARY, "%s se ha actualizado desde la biblioteca.");
        de(IS_UPDATED_FROM_THE_LIBRARY, "%s wird aus der Bibliothek aktualisiert.");

        ja(IS_NOT_IN_THE_LIBRARY,
                "%s はライブラリにもこの本にもありません。");
        zh(IS_NOT_IN_THE_LIBRARY,
                "角色库和本书中都没有 %s 了。");
        fr(IS_NOT_IN_THE_LIBRARY, "%s n'est plus dans la bibliothèque ni dans ce livre.");
        es(IS_NOT_IN_THE_LIBRARY, "%s ya no está en la biblioteca ni en este libro.");
        de(IS_NOT_IN_THE_LIBRARY, "%s ist nicht mehr in der Bibliothek oder in diesem Buch.");

        ja(THERE_IS_NO_PERSONA,
                "呼べるペルソナがありません。全員がここにいます。");
        zh(THERE_IS_NO_PERSONA,
                "没有可邀请的读者角色：他们都在这里。");
        fr(THERE_IS_NO_PERSONA, "Il n'y a aucun persona à faire entrer : ils sont tous là.");
        es(THERE_IS_NO_PERSONA, "No hay ningún lector que incorporar: ya están todos aquí.");
        de(THERE_IS_NO_PERSONA, "Es gibt keine Persona zum Hinzuholen: Alle sind schon hier.");

        ja(THE_LAST_PERSONA_CANT_LEAVE,
                "最後のペルソナは退出できません。");
        zh(THE_LAST_PERSONA_CANT_LEAVE,
                "最后一个读者角色不能离开。");
        fr(THE_LAST_PERSONA_CANT_LEAVE, "Le dernier persona ne peut pas partir.");
        es(THE_LAST_PERSONA_CANT_LEAVE, "El último lector no puede irse.");
        de(THE_LAST_PERSONA_CANT_LEAVE, "Die letzte Persona kann nicht gehen.");

        ja(WHAT_THE_GROUP_IS_TOLD,
                "グループに伝えること (任意)");
        zh(WHAT_THE_GROUP_IS_TOLD,
                "告诉群组的话（可选）");
        fr(WHAT_THE_GROUP_IS_TOLD, "Ce qu'on dit au groupe (facultatif)");
        es(WHAT_THE_GROUP_IS_TOLD, "Lo que se dice al grupo (opcional)");
        de(WHAT_THE_GROUP_IS_TOLD, "Was der Gruppe gesagt wird (optional)");

        ja(CHAT_SETTINGS,
                "チャット設定");
        zh(CHAT_SETTINGS,
                "聊天设置");
        fr(CHAT_SETTINGS, "Paramètres de la discussion");
        es(CHAT_SETTINGS, "Ajustes del chat");
        de(CHAT_SETTINGS, "Chat-Einstellungen");

        ja(FROM_NOW_ON_THE_TEXT,
                "これ以降、テキストは次のメディアとして読まれます: %s");
        zh(FROM_NOW_ON_THE_TEXT,
                "从现在起，文本将作为以下媒介阅读：%s");
        fr(FROM_NOW_ON_THE_TEXT, "Désormais le texte est lu comme : %s");
        es(FROM_NOW_ON_THE_TEXT, "A partir de ahora el texto se lee como: %s");
        de(FROM_NOW_ON_THE_TEXT, "Ab jetzt wird der Text gelesen als: %s");

        ja(PERSONAS_READ_UP_TO_PART,
                "ペルソナが読むパートの上限");
        zh(PERSONAS_READ_UP_TO_PART,
                "读者角色最多读到的部分");
        fr(PERSONAS_READ_UP_TO_PART, "Les personas lisent jusqu'à la partie");
        es(PERSONAS_READ_UP_TO_PART, "Los lectores leen hasta la parte");
        de(PERSONAS_READ_UP_TO_PART, "Die Personas lesen bis Abschnitt");

        ja(START_FROM_PART,
                "開始パート");
        zh(START_FROM_PART,
                "起始部分");
        fr(START_FROM_PART, "Commencer à la partie");
        es(START_FROM_PART, "Empezar desde la parte");
        de(START_FROM_PART, "Beginnen ab Abschnitt");

        ja(USE_THE_SUMMARIES,
                "ストーリーの要約を使う");
        zh(USE_THE_SUMMARIES,
                "使用故事的摘要");
        fr(USE_THE_SUMMARIES, "Utiliser les résumés de l'histoire");
        es(USE_THE_SUMMARIES, "Usar los resúmenes de la historia");
        de(USE_THE_SUMMARIES, "Die Zusammenfassungen der Geschichte verwenden");

        ja(EMPTY_THE_WHOLE_STORY,
                "空の場合: ストーリー全体。ペルソナはこのパートより後のパートを受け取りません。");
        zh(EMPTY_THE_WHOLE_STORY,
                "留空：整个故事。读者角色不会收到此部分之后的任何部分。");
        fr(EMPTY_THE_WHOLE_STORY, "Vide : toute l'histoire. Les personas ne reçoivent jamais de partie après celle-ci.");
        es(EMPTY_THE_WHOLE_STORY, "Vacío: toda la historia. Los lectores nunca reciben una parte posterior a esta.");
        de(EMPTY_THE_WHOLE_STORY, "Leer: die ganze Geschichte. Die Personas erhalten nie einen Abschnitt nach diesem.");

        ja(EMPTY_FROM_THE_FIRST_PART,
                "空の場合: 最初のパートから。それ以前のパートは要約としても送信されません。");
        zh(EMPTY_FROM_THE_FIRST_PART,
                "留空：从第一个部分开始。更早的部分不会发送，摘要也不会。");
        fr(EMPTY_FROM_THE_FIRST_PART, "Vide : depuis la première partie. Les parties précédentes ne sont pas envoyées, même pas en résumés.");
        es(EMPTY_FROM_THE_FIRST_PART, "Vacío: desde la primera parte. Las partes anteriores no se envían, ni siquiera como resúmenes.");
        de(EMPTY_FROM_THE_FIRST_PART, "Leer: ab dem ersten Abschnitt. Frühere Abschnitte werden nicht gesendet, nicht einmal als Zusammenfassungen.");

        ja(THE_FIRST_PART_CANT_COME,
                "最初のパートを最後のパートより後にすることはできません。");
        zh(THE_FIRST_PART_CANT_COME,
                "起始部分不能在最后一个部分之后。");
        fr(THE_FIRST_PART_CANT_COME, "La première partie ne peut pas venir après la dernière.");
        es(THE_FIRST_PART_CANT_COME, "La primera parte no puede ir después de la última.");
        de(THE_FIRST_PART_CANT_COME, "Der erste Abschnitt kann nicht nach dem letzten kommen.");

        ja(STORY_MEDIUM_MODEL_AND_FORMAT,
                "ストーリー、メディア、モデル、形式");
        zh(STORY_MEDIUM_MODEL_AND_FORMAT,
                "故事、媒介、模型和格式");
        fr(STORY_MEDIUM_MODEL_AND_FORMAT, "Histoire, support, modèle et format");
        es(STORY_MEDIUM_MODEL_AND_FORMAT, "Historia, medio, modelo y formato");
        de(STORY_MEDIUM_MODEL_AND_FORMAT, "Geschichte, Medium, Modell und Format");

        ja(READS_UP_TO_PART,
                "パート %d まで読む");
        zh(READS_UP_TO_PART,
                "最多读到第 %d 部分");
        fr(READS_UP_TO_PART, "Lit jusqu'à la partie %d");
        es(READS_UP_TO_PART, "Lee hasta la parte %d");
        de(READS_UP_TO_PART, "Liest bis Abschnitt %d");

        ja(FROM_PART,
                "パート %d から");
        zh(FROM_PART,
                "从第 %d 部分起");
        fr(FROM_PART, "depuis la partie %d");
        es(FROM_PART, "desde la parte %d");
        de(FROM_PART, "ab Abschnitt %d");

        ja(THE_PERSONAS_HAVE_READ_PARTS,
                "ペルソナが読んだパート: %s");
        zh(THE_PERSONAS_HAVE_READ_PARTS,
                "读者角色已读：部分 %s");
        fr(THE_PERSONAS_HAVE_READ_PARTS, "Les personas ont lu : parties %s");
        es(THE_PERSONAS_HAVE_READ_PARTS, "Los lectores han leído: partes %s");
        de(THE_PERSONAS_HAVE_READ_PARTS, "Die Personas haben gelesen: Abschnitte %s");

        ja(SEND_AS_A_SCENE,
                "シーンとして送信");
        zh(SEND_AS_A_SCENE,
                "作为场景发送");
        fr(SEND_AS_A_SCENE, "Envoyer comme scène");
        es(SEND_AS_A_SCENE, "Enviar como escena");
        de(SEND_AS_A_SCENE, "Als Szene senden");

        ja(SEND_THE_TEXT_AS_A_SCENE,
                "テキストをシーンとして送信します。シーンの転換や自分の行動であり、自分の発言ではありません。ペルソナはそれに反応します。ストーリーのパートは含まれません。");
        zh(SEND_THE_TEXT_AS_A_SCENE,
                "将文本作为场景发送：场景转换或你的行动，而不是你说的话。读者角色会对此作出反应。不包含故事的部分。");
        fr(SEND_THE_TEXT_AS_A_SCENE, "Envoyer le texte comme une scène : un changement de scène ou ce que vous faites, pas quelque chose que vous dites. Les personas y réagissent. Les parties de l'histoire ne sont pas incluses.");
        es(SEND_THE_TEXT_AS_A_SCENE, "Envía el texto como una escena: un cambio de escena o lo que haces, no algo que dices. Los lectores reaccionan a ello. No se incluyen partes de la historia.");
        de(SEND_THE_TEXT_AS_A_SCENE, "Den Text als Szene senden: ein Szenenwechsel oder das, was du tust, nicht etwas, das du sagst. Die Personas reagieren darauf. Abschnitte der Geschichte werden nicht einbezogen.");

        ja(WRITE_THE_SCENE_FIRST,
                "先にシーンを書いてください。");
        zh(WRITE_THE_SCENE_FIRST,
                "请先写下场景。");
        fr(WRITE_THE_SCENE_FIRST, "Écrivez d'abord la scène.");
        es(WRITE_THE_SCENE_FIRST, "Escribe primero la escena.");
        de(WRITE_THE_SCENE_FIRST, "Schreibe zuerst die Szene.");

        ja(MOST_MESSAGES_IN_A_TURN,
                "1ターンの最大メッセージ数");
        zh(MOST_MESSAGES_IN_A_TURN,
                "每轮最多消息数");
        fr(MOST_MESSAGES_IN_A_TURN, "Nombre maximal de messages par tour");
        es(MOST_MESSAGES_IN_A_TURN, "Máximo de mensajes en un turno");
        de(MOST_MESSAGES_IN_A_TURN, "Höchstzahl der Nachrichten in einem Zug");

        ja(EMPTY_INHERITED_THE_MOST_MESSAGES,
                "空の場合: 継承。あなたの1通のメッセージに対してペルソナが書くメッセージの最大数。");
        zh(EMPTY_INHERITED_THE_MOST_MESSAGES,
                "留空：继承。读者角色回应你的一条消息时最多写的消息数。");
        fr(EMPTY_INHERITED_THE_MOST_MESSAGES, "Vide : hérité. Le nombre maximal de messages que les personas écrivent en réponse à un de vos messages.");
        es(EMPTY_INHERITED_THE_MOST_MESSAGES, "Vacío: heredado. El máximo de mensajes que escriben los lectores en respuesta a un mensaje tuyo.");
        de(EMPTY_INHERITED_THE_MOST_MESSAGES, "Leer: geerbt. Die meisten Nachrichten, die die Personas als Antwort auf eine Nachricht von dir schreiben.");

        ja(EMPTY_AUTOMATIC_THE_MOST_MESSAGES,
                "空の場合: 自動 (1人のペルソナなら3、グループなら1人につき約2、最大8)。1通のメッセージに対してペルソナが書くメッセージの最大数。チャットごとに設定することもできます。");
        zh(EMPTY_AUTOMATIC_THE_MOST_MESSAGES,
                "留空：自动（单个读者角色为 3，群组中每个角色约 2，最多 8）。读者角色回应一条消息时最多写的消息数；每个聊天可单独设置。");
        fr(EMPTY_AUTOMATIC_THE_MOST_MESSAGES, "Vide : automatique (3 pour un persona, environ deux par persona d'un groupe, 8 au plus). Le nombre maximal de messages que les personas écrivent en réponse à un message ; une discussion peut définir le sien.");
        es(EMPTY_AUTOMATIC_THE_MOST_MESSAGES, "Vacío: automático (3 para un lector, unos dos por cada lector de un grupo, como máximo 8). El máximo de mensajes que escriben los lectores en respuesta a un mensaje; un chat puede definir el suyo.");
        de(EMPTY_AUTOMATIC_THE_MOST_MESSAGES, "Leer: automatisch (3 bei einer Persona, etwa zwei je Persona einer Gruppe, höchstens 8). Die meisten Nachrichten, die die Personas als Antwort auf eine Nachricht schreiben; ein Chat kann eigene festlegen.");

        ja(THE_MOST_MESSAGES_MUST_BE,
                "1ターンの最大メッセージ数は1から%dの間でなければなりません。");
        zh(THE_MOST_MESSAGES_MUST_BE,
                "每轮最多消息数必须在 1 到 %d 之间。");
        fr(THE_MOST_MESSAGES_MUST_BE, "Le nombre maximal de messages par tour doit être entre 1 et %d.");
        es(THE_MOST_MESSAGES_MUST_BE, "El máximo de mensajes en un turno debe estar entre 1 y %d.");
        de(THE_MOST_MESSAGES_MUST_BE, "Die Höchstzahl der Nachrichten in einem Zug muss zwischen 1 und %d liegen.");

        ja(YOUR_NAME,
                "あなたの名前");
        zh(YOUR_NAME,
                "你的名字");
        fr(YOUR_NAME, "Votre nom");
        es(YOUR_NAME, "Tu nombre");
        de(YOUR_NAME, "Dein Name");

        ja(EMPTY_NOT_NAMED,
                "空の場合: 名前なし。ペルソナはこの名前であなたを呼び、あなたのメッセージにも表示されます。");
        zh(EMPTY_NOT_NAMED,
                "留空：不设名字。读者角色会用这个名字称呼你，你的消息上也会显示它。");
        fr(EMPTY_NOT_NAMED, "Vide : sans nom. Les personas vous appellent par ce nom et vos messages l'affichent.");
        es(EMPTY_NOT_NAMED, "Vacío: sin nombre. Los lectores te llaman por este nombre y tus mensajes lo muestran.");
        de(EMPTY_NOT_NAMED, "Leer: ohne Namen. Die Personas nennen dich bei diesem Namen und deine Nachrichten zeigen ihn.");

        ja(LAST_PART,
                "最後のパート");
        zh(LAST_PART,
                "最后一个部分");
        fr(LAST_PART, "Dernière partie");
        es(LAST_PART, "Última parte");
        de(LAST_PART, "Letzter Abschnitt");

        ja(FIRST_PART,
                "最初のパート");
        zh(FIRST_PART,
                "第一个部分");
        fr(FIRST_PART, "Première partie");
        es(FIRST_PART, "Primera parte");
        de(FIRST_PART, "Erster Abschnitt");

        ja(BETA_READERS_ANSWER_IN_A_CHAT, "ベータリーダー: チャットで回答");
        zh(BETA_READERS_ANSWER_IN_A_CHAT, "试读者：在聊天中回答");
        fr(BETA_READERS_ANSWER_IN_A_CHAT, "Bêta-lecteurs : répondre dans une discussion");
        es(BETA_READERS_ANSWER_IN_A_CHAT, "Lectores beta: responder en un chat");
        de(BETA_READERS_ANSWER_IN_A_CHAT, "Betaleser: in einem Chat antworten");

        ja(BETA_READERS_WRITE_A_PERSONA, "ベータリーダー: ペルソナを作成");
        zh(BETA_READERS_WRITE_A_PERSONA, "试读者：撰写角色设定");
        fr(BETA_READERS_WRITE_A_PERSONA, "Bêta-lecteurs : écrire un persona");
        es(BETA_READERS_WRITE_A_PERSONA, "Lectores beta: escribir un lector");
        de(BETA_READERS_WRITE_A_PERSONA, "Betaleser: eine Persona schreiben");
    }
}
