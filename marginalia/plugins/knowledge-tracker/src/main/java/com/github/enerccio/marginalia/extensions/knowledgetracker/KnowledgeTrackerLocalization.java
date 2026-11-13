package com.github.enerccio.marginalia.extensions.knowledgetracker;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Knowledge Tracker in the languages besides English; the English text is the key.
 */
public class KnowledgeTrackerLocalization extends PluginLocalizationBundle {

    public static final KnowledgeTrackerLocalization T = new KnowledgeTrackerLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final KnowledgeTrackerDefaults englishDefaults = new KnowledgeTrackerDefaults();
    private final Map<Language, KnowledgeTrackerDefaults> translatedDefaults = Map.of(JA, new KnowledgeTrackerDefaultsJA(), ZH, new KnowledgeTrackerDefaultsZH(), FR, new KnowledgeTrackerDefaultsFR(), ES, new KnowledgeTrackerDefaultsES(), DE, new KnowledgeTrackerDefaultsDE(),
            IT, new KnowledgeTrackerDefaultsIT(), KO, new KnowledgeTrackerDefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public KnowledgeTrackerDefaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

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
    public static final String SELECT_ITEMS = "Select items";
    public static final String SELECT_ALL = "Select all";
    public static final String DESELECT_ALL = "Deselect all";
    public static final String DELETE_SELECTED = "Delete selected (%d)";
    public static final String DELETE_N_ITEMS_FOR_GOOD = "Delete %d items for good? Retract keeps them as old knowledge.";
    public static final String SELECT_THIS_ITEM = "Select this item";
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
    public static final String GIVE_THE_FACTS_TO_THE_ANNOTATION = "Give what the characters know to the annotation";
    public static final String WHEN_A_MODEL_ANNOTATES_A_PART = "When a model annotates a part (Annotate with AI), it is told what the tracked characters of the part knew when it begins, and flags the ones who act on what they cannot know";
    public static final String SEPARATORS_OF_THIS_BOOK = "Separators of this book";
    public static final String MY_SEPARATORS = "My separators (all books)";
    public static final String SEPARATORS_HELP = "How the present characters field is split into names. One per line; empty = the book uses mine, mine empty = the built-in ones (, ; new line / | 、 ， ；). A word such as and splits between spaces, \\n is a new line.";
    public static final String KNOWLEDGE_TRACKER = "Knowledge Tracker";
    public static final String KNOWLEDGE_EXTRACTION_PROMPT = "Knowledge extraction prompt";
    public static final String KNOWLEDGE_TIDY_PROMPT = "Knowledge tidy prompt";
    public static final String THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL = "The system message that tells the model how to read the story for what the tracked characters learn. Keep the JSON answer format, the plugin reads it. Empty = the inherited one, else the built-in text.";
    public static final String THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS = "The system message of the tidy pass, which drops and merges items that are not worth keeping. Keep the JSON answer format, the plugin reads it. Empty = the inherited one, else the built-in text.";
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
    public static final String KNOWLEDGE_TRACKER_READ_THE_STORY = "Knowledge Tracker: read the story";
    public static final String MASS_CHANGES = "Mass changes";
    public static final String CHANGE_MANY_ITEMS_AT_ONCE = "Delete, retract or restore many items of the tracked characters at once; export and import them as JSON";
    public static final String STATE = "State";
    public static final String ACTIVE = "Active";
    public static final String OLD_RETRACTED = "Old: retracted";
    public static final String OLD_DECLINED = "Old: declined in a review";
    public static final String OLD_TIDIED = "Old: tidied";
    public static final String ORIGIN = "Origin";
    public static final String INITIAL_STATE = "Initial state";
    public static final String WRITTEN_BY_HAND = "Written by hand";
    public static final String LEARNED_FROM_THE_STORY = "Learned from the story";
    public static final String PART = "Part";
    public static final String NO_ITEMS = "No items";
    public static final String N_OF_N_SELECTED = "%d of %d selected";
    public static final String RETRACT_SELECTED = "Retract selected (%d)";
    public static final String RESTORE_SELECTED = "Restore selected (%d)";
    public static final String N_ITEMS_RETRACTED = "%d item(s) retracted";
    public static final String N_ITEMS_RESTORED = "%d item(s) restored";
    public static final String N_ITEMS_DELETED = "%d item(s) deleted";
    public static final String EXPORT_TO_JSON = "Export to JSON";
    public static final String IMPORT_FROM_JSON = "Import from JSON";
    public static final String IMPORTED_SUMMARY = "Imported: %d new character(s), %d updated, %d item(s) added, %d already there";
    public static final String THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT = "The file is not a Knowledge Tracker export: %s";

    @Override
    protected void loadKeys() {
        ja(KNOWLEDGE, "知識");
        zh(KNOWLEDGE, "认知");
        fr(KNOWLEDGE, "Connaissances");
        es(KNOWLEDGE, "Conocimiento");
        de(KNOWLEDGE, "Wissen");
        ko(KNOWLEDGE, "지식");

        ja(TRACK_A_CHARACTER, "キャラクターを追跡");
        zh(TRACK_A_CHARACTER, "追踪角色");
        fr(TRACK_A_CHARACTER, "Suivre un personnage");
        es(TRACK_A_CHARACTER, "Seguir a un personaje");
        de(TRACK_A_CHARACTER, "Eine Figur verfolgen");
        ko(TRACK_A_CHARACTER, "캐릭터 추적");

        ja(NEW_TRACKED_CHARACTER, "新しい追跡キャラクター");
        zh(NEW_TRACKED_CHARACTER, "新的被追踪角色");
        fr(NEW_TRACKED_CHARACTER, "Nouveau personnage suivi");
        es(NEW_TRACKED_CHARACTER, "Nuevo personaje seguido");
        de(NEW_TRACKED_CHARACTER, "Neue verfolgte Figur");
        ko(NEW_TRACKED_CHARACTER, "새 추적 캐릭터");

        ja(EDIT_TRACKED_CHARACTER, "追跡キャラクターを編集");
        zh(EDIT_TRACKED_CHARACTER, "编辑被追踪的角色");
        fr(EDIT_TRACKED_CHARACTER, "Modifier le personnage suivi");
        es(EDIT_TRACKED_CHARACTER, "Editar personaje seguido");
        de(EDIT_TRACKED_CHARACTER, "Verfolgte Figur bearbeiten");
        ko(EDIT_TRACKED_CHARACTER, "추적 캐릭터 편집");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");

        ja(ALIASES, "別名");
        zh(ALIASES, "别名");
        fr(ALIASES, "Autres noms");
        es(ALIASES, "Alias");
        de(ALIASES, "Aliasnamen");
        ko(ALIASES, "별칭");

        ja(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "「登場人物」欄でこのキャラクターが書かれる他の名前（カンマ区切り）");
        zh(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "该角色在“在场角色”栏中的其他写法，用逗号分隔");
        fr(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Autres noms sous lesquels le personnage est écrit dans le champ des personnages présents, séparés par des virgules");
        es(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Otros nombres con los que se escribe el personaje en el campo de personajes presentes, separados por comas");
        de(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Andere Namen, unter denen die Figur im Feld „Anwesende Figuren“ geschrieben wird, durch Kommas getrennt");
        ko(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "등장인물 필드에서 캐릭터가 표기되는 다른 이름, 쉼표로 구분");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");
        fr(EDIT, "Modifier");
        es(EDIT, "Editar");
        de(EDIT, "Bearbeiten");
        ko(EDIT, "편집");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");
        ko(DELETE, "삭제");

        ja(COPY, "コピー");
        zh(COPY, "复制");
        fr(COPY, "Copier");
        es(COPY, "Copiar");
        de(COPY, "Kopieren");
        ko(COPY, "복사");

        ja(COPIED, "コピーしました");
        zh(COPIED, "已复制");
        fr(COPIED, "Copié");
        es(COPIED, "Copiado");
        de(COPIED, "Kopiert");
        ko(COPIED, "복사됨");

        ja(GIVE_THE_CHARACTER_A_NAME, "キャラクターに名前を付けてください");
        zh(GIVE_THE_CHARACTER_A_NAME, "请给角色起个名字");
        fr(GIVE_THE_CHARACTER_A_NAME, "Donnez un nom au personnage");
        es(GIVE_THE_CHARACTER_A_NAME, "Ponle un nombre al personaje");
        de(GIVE_THE_CHARACTER_A_NAME, "Gib der Figur einen Namen");
        ko(GIVE_THE_CHARACTER_A_NAME, "캐릭터에 이름을 지정하세요");

        ja(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名前または別名は「%s」で既に使われています");
        zh(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名称或别名已被“%s”使用");
        fr(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "Le nom ou un autre nom est déjà utilisé par %s");
        es(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "El nombre o un alias ya lo usa %s");
        de(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "Der Name oder ein Aliasname wird bereits von %s verwendet");
        ko(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "이름이나 별칭이 이미 %s에서 사용 중입니다");

        ja(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "追跡中のキャラクター「%s」と、その知識をすべて削除しますか？");
        zh(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "要删除被追踪的角色“%s”及其所有已知信息吗？");
        fr(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "Supprimer le personnage suivi %s avec tout ce qu'il sait ?");
        es(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "¿Eliminar al personaje seguido %s con todo lo que sabe?");
        de(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "Die verfolgte Figur %s mit allem, was sie weiß, löschen?");
        ko(DELETE_THE_TRACKED_CHARACTER_AND_ALL, "추적 캐릭터 %s과(와) 그가 아는 모든 것을 삭제하시겠습니까?");

        ja(DELETE_THIS_ITEM_FOR_GOOD, "この項目を完全に削除しますか？「撤回」なら古い知識として残ります。");
        zh(DELETE_THIS_ITEM_FOR_GOOD, "要永久删除此条目吗？“撤回”会将其保留为旧认知。");
        fr(DELETE_THIS_ITEM_FOR_GOOD, "Supprimer définitivement cet élément ? Retirer le garde comme ancienne connaissance.");
        es(DELETE_THIS_ITEM_FOR_GOOD, "¿Eliminar este elemento definitivamente? Retirar lo conserva como conocimiento antiguo.");
        de(DELETE_THIS_ITEM_FOR_GOOD, "Dieses Element endgültig löschen? „Zurückziehen“ behält es als altes Wissen.");
        ko(DELETE_THIS_ITEM_FOR_GOOD, "이 항목을 영구 삭제하시겠습니까? 철회하면 오래된 지식으로 유지됩니다.");

        ja(NO_CHARACTER_IS_TRACKED_YET, "追跡中のキャラクターはまだありません。ここで追加したキャラクターだけが、パートの登場人物に含まれている間だけモデルに説明されます。");
        zh(NO_CHARACTER_IS_TRACKED_YET, "尚未追踪任何角色。只有在此添加的角色，并且处于某一部分的在场角色之中时，才会向模型说明。");
        fr(NO_CHARACTER_IS_TRACKED_YET, "Aucun personnage n'est encore suivi. Seuls les personnages ajoutés ici sont décrits au modèle, et seulement lorsqu'ils font partie des personnages présents d'une partie.");
        es(NO_CHARACTER_IS_TRACKED_YET, "Todavía no se sigue a ningún personaje. Solo se describe al modelo a los personajes que añades aquí, y solo mientras estén entre los personajes presentes de una parte.");
        de(NO_CHARACTER_IS_TRACKED_YET, "Noch wird keine Figur verfolgt. Nur die Figuren, die du hier hinzufügst, werden dem Modell beschrieben, und nur solange sie zu den anwesenden Figuren eines Abschnitts gehören.");
        ko(NO_CHARACTER_IS_TRACKED_YET, "아직 추적하는 캐릭터가 없습니다. 여기에 추가한 캐릭터만 모델에 설명되며, 파트의 등장인물에 포함되어 있을 때만 해당됩니다.");

        ja(NOTHING_IS_KNOWN_YET, "まだ何も書かれていません。このキャラクターが知っていること、知らないことを追加してください。");
        zh(NOTHING_IS_KNOWN_YET, "尚无内容。请添加该角色知道和不知道的事情。");
        fr(NOTHING_IS_KNOWN_YET, "Rien n'est encore écrit. Ajoutez ce que le personnage sait et ne sait pas.");
        es(NOTHING_IS_KNOWN_YET, "Todavía no hay nada escrito. Añade lo que el personaje sabe y lo que no sabe.");
        de(NOTHING_IS_KNOWN_YET, "Noch ist nichts geschrieben. Füge hinzu, was die Figur weiß und was nicht.");
        ko(NOTHING_IS_KNOWN_YET, "아직 작성된 것이 없습니다. 캐릭터가 아는 것과 모르는 것을 추가하세요.");

        ja(ITEMS_ACTIVE_OLD, "有効 %d 件、古い %d 件");
        zh(ITEMS_ACTIVE_OLD, "有效 %d 条，旧的 %d 条");
        fr(ITEMS_ACTIVE_OLD, "%d actifs, %d anciens");
        es(ITEMS_ACTIVE_OLD, "%d activos, %d antiguos");
        de(ITEMS_ACTIVE_OLD, "%d aktiv, %d alt");
        ko(ITEMS_ACTIVE_OLD, "활성 %d개, 오래됨 %d개");

        ja(SHOW_WHAT_THE_CHARACTER_KNOWS, "キャラクターの知識を表示");
        zh(SHOW_WHAT_THE_CHARACTER_KNOWS, "显示该角色的认知");
        fr(SHOW_WHAT_THE_CHARACTER_KNOWS, "Afficher ce que sait le personnage");
        es(SHOW_WHAT_THE_CHARACTER_KNOWS, "Mostrar lo que sabe el personaje");
        de(SHOW_WHAT_THE_CHARACTER_KNOWS, "Anzeigen, was die Figur weiß");
        ko(SHOW_WHAT_THE_CHARACTER_KNOWS, "캐릭터가 아는 것 보기");

        ja(SELECT_ITEMS, "項目を選択");
        zh(SELECT_ITEMS, "选择条目");
        fr(SELECT_ITEMS, "Sélectionner des éléments");
        es(SELECT_ITEMS, "Seleccionar elementos");
        de(SELECT_ITEMS, "Elemente auswählen");
        ko(SELECT_ITEMS, "항목 선택");

        ja(SELECT_ALL, "すべて選択");
        zh(SELECT_ALL, "全选");
        fr(SELECT_ALL, "Tout sélectionner");
        es(SELECT_ALL, "Seleccionar todo");
        de(SELECT_ALL, "Alle auswählen");
        ko(SELECT_ALL, "모두 선택");

        ja(DESELECT_ALL, "選択を解除");
        zh(DESELECT_ALL, "取消全选");
        fr(DESELECT_ALL, "Tout désélectionner");
        es(DESELECT_ALL, "Deseleccionar todo");
        de(DESELECT_ALL, "Auswahl aufheben");
        ko(DESELECT_ALL, "모두 선택 해제");

        ja(DELETE_SELECTED, "選択した項目を削除（%d）");
        zh(DELETE_SELECTED, "删除所选（%d）");
        fr(DELETE_SELECTED, "Supprimer la sélection (%d)");
        es(DELETE_SELECTED, "Eliminar la selección (%d)");
        de(DELETE_SELECTED, "Ausgewählte löschen (%d)");
        ko(DELETE_SELECTED, "선택 항목 삭제 (%d)");

        ja(DELETE_N_ITEMS_FOR_GOOD, "%d 件の項目を完全に削除しますか？「撤回」なら古い知識として残ります。");
        zh(DELETE_N_ITEMS_FOR_GOOD, "要永久删除这 %d 个条目吗？“撤回”会将其保留为旧认知。");
        fr(DELETE_N_ITEMS_FOR_GOOD, "Supprimer définitivement %d éléments ? Retirer les garde comme anciennes connaissances.");
        es(DELETE_N_ITEMS_FOR_GOOD, "¿Eliminar %d elementos definitivamente? Retirar los conserva como conocimiento antiguo.");
        de(DELETE_N_ITEMS_FOR_GOOD, "%d Elemente endgültig löschen? „Zurückziehen“ behält sie als altes Wissen.");
        ko(DELETE_N_ITEMS_FOR_GOOD, "항목 %d개를 영구 삭제하시겠습니까? 철회하면 오래된 지식으로 유지됩니다.");

        ja(SELECT_THIS_ITEM, "この項目を選択");
        zh(SELECT_THIS_ITEM, "选择此条目");
        fr(SELECT_THIS_ITEM, "Sélectionner cet élément");
        es(SELECT_THIS_ITEM, "Seleccionar este elemento");
        de(SELECT_THIS_ITEM, "Dieses Element auswählen");
        ko(SELECT_THIS_ITEM, "이 항목 선택");

        ja(ADD_KNOWLEDGE, "知識を追加");
        zh(ADD_KNOWLEDGE, "添加认知");
        fr(ADD_KNOWLEDGE, "Ajouter une connaissance");
        es(ADD_KNOWLEDGE, "Añadir conocimiento");
        de(ADD_KNOWLEDGE, "Wissen hinzufügen");
        ko(ADD_KNOWLEDGE, "지식 추가");

        ja(NEW_ITEM, "新しい項目");
        zh(NEW_ITEM, "新条目");
        fr(NEW_ITEM, "Nouvel élément");
        es(NEW_ITEM, "Elemento nuevo");
        de(NEW_ITEM, "Neues Element");
        ko(NEW_ITEM, "새 항목");

        ja(EDIT_ITEM, "項目を編集");
        zh(EDIT_ITEM, "编辑条目");
        fr(EDIT_ITEM, "Modifier l'élément");
        es(EDIT_ITEM, "Editar elemento");
        de(EDIT_ITEM, "Element bearbeiten");
        ko(EDIT_ITEM, "항목 편집");

        ja(KIND, "種類");
        zh(KIND, "类型");
        fr(KIND, "Genre");
        es(KIND, "Tipo");
        de(KIND, "Art");
        ko(KIND, "종류");

        ja(KNOWS, "知っている");
        zh(KNOWS, "知道");
        fr(KNOWS, "Sait");
        es(KNOWS, "Sabe");
        de(KNOWS, "Weiß");
        ko(KNOWS, "아는 것");

        ja(DOES_NOT_KNOW, "知らない");
        zh(DOES_NOT_KNOW, "不知道");
        fr(DOES_NOT_KNOW, "Ne sait pas");
        es(DOES_NOT_KNOW, "No sabe");
        de(DOES_NOT_KNOW, "Weiß nicht");
        ko(DOES_NOT_KNOW, "모르는 것");

        ja(TEXT, "内容");
        zh(TEXT, "内容");
        fr(TEXT, "Texte");
        es(TEXT, "Texto");
        de(TEXT, "Text");
        ko(TEXT, "텍스트");

        ja(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "人物、場所、秘密、出来事など何についてでも構いません。短い一文で、知らないことははっきり書いてください。");
        zh(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "可以是任何事：人物、地点、秘密、事件。一句简短的话；不知道的事请明确写出。");
        fr(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "Sur n'importe quoi : une personne, un lieu, un secret, un événement. Une phrase courte ; écrivez explicitement ce qu'il ne sait pas.");
        es(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "Sobre cualquier cosa: una persona, un lugar, un secreto, un suceso. Una frase corta; escribe explícitamente lo que no sabe.");
        de(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "Über alles Mögliche: eine Person, einen Ort, ein Geheimnis, ein Ereignis. Ein kurzer Satz; schreibe ausdrücklich, was sie nicht weiß.");
        ko(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW, "인물, 장소, 비밀, 사건 등 무엇이든. 짧은 문장 하나로: 모르는 것은 명시적으로 쓰세요.");

        ja(WRITE_SOMETHING, "内容を入力してください");
        zh(WRITE_SOMETHING, "请输入内容");
        fr(WRITE_SOMETHING, "Écrivez quelque chose");
        es(WRITE_SOMETHING, "Escribe algo");
        de(WRITE_SOMETHING, "Schreibe etwas");
        ko(WRITE_SOMETHING, "무언가 쓰세요");

        ja(RETRACT, "撤回");
        zh(RETRACT, "撤回");
        fr(RETRACT, "Retirer");
        es(RETRACT, "Retirar");
        de(RETRACT, "Zurückziehen");
        ko(RETRACT, "철회");

        ja(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "もう正しくない：古い知識として残し、モデルには送りません");
        zh(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "已不再成立：保留为旧认知，不发送给模型");
        fr(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "N'est plus vrai : le garder comme ancienne connaissance et ne pas l'envoyer au modèle");
        es(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "Ya no es cierto: conservarlo como conocimiento antiguo y no enviarlo al modelo");
        de(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "Stimmt nicht mehr: als altes Wissen behalten und nicht an das Modell senden");
        ko(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD, "더 이상 사실이 아님: 오래된 지식으로 유지하고 모델에 전송하지 않음");

        ja(RESTORE, "復元");
        zh(RESTORE, "恢复");
        fr(RESTORE, "Restaurer");
        es(RESTORE, "Restaurar");
        de(RESTORE, "Wiederherstellen");
        ko(RESTORE, "복원");

        ja(MAKE_IT_ACTIVE_AGAIN, "今後はまた有効にします");
        zh(MAKE_IT_ACTIVE_AGAIN, "从现在起重新生效");
        fr(MAKE_IT_ACTIVE_AGAIN, "Le rendre de nouveau actif, à partir de maintenant");
        es(MAKE_IT_ACTIVE_AGAIN, "Volver a activarlo, a partir de ahora");
        de(MAKE_IT_ACTIVE_AGAIN, "Wieder aktiv machen, ab jetzt");
        ko(MAKE_IT_ACTIVE_AGAIN, "지금부터 다시 활성화");

        ja(OLD_KNOWLEDGE, "古い知識（%d）");
        zh(OLD_KNOWLEDGE, "旧认知（%d）");
        fr(OLD_KNOWLEDGE, "Anciennes connaissances (%d)");
        es(OLD_KNOWLEDGE, "Conocimiento antiguo (%d)");
        de(OLD_KNOWLEDGE, "Altes Wissen (%d)");
        ko(OLD_KNOWLEDGE, "오래된 지식 (%d)");

        ja(HAS_MET, "会ったことがある：%s");
        zh(HAS_MET, "见过：%s");
        fr(HAS_MET, "A rencontré : %s");
        es(HAS_MET, "Ha conocido a: %s");
        de(HAS_MET, "Ist begegnet: %s");
        ko(HAS_MET, "만난 사람: %s");

        ja(SETTINGS, "設定");
        zh(SETTINGS, "设置");
        fr(SETTINGS, "Paramètres");
        es(SETTINGS, "Ajustes");
        de(SETTINGS, "Einstellungen");
        ko(SETTINGS, "설정");

        ja(REFRESH, "更新");
        zh(REFRESH, "刷新");
        fr(REFRESH, "Actualiser");
        es(REFRESH, "Actualizar");
        de(REFRESH, "Aktualisieren");
        ko(REFRESH, "새로 고침");

        ja(LET_THE_MODEL_READ_EARLIER_PARTS, "モデルが以前のパートを読めるようにする");
        zh(LET_THE_MODEL_READ_EARLIER_PARTS, "允许模型阅读之前的部分");
        fr(LET_THE_MODEL_READ_EARLIER_PARTS, "Laisser le modèle lire les parties précédentes");
        es(LET_THE_MODEL_READ_EARLIER_PARTS, "Dejar que el modelo lea partes anteriores");
        de(LET_THE_MODEL_READ_EARLIER_PARTS, "Das Modell frühere Abschnitte lesen lassen");
        ko(LET_THE_MODEL_READ_EARLIER_PARTS, "모델이 이전 파트를 읽도록 허용");

        ja(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "モデルは、確信が持てないとき（たとえば噂で聞いたことが確認された場合）、項目を得たパートを読み返せます。読み返すたびにモデルへのやり取りが1回増えます。");
        zh(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "模型不确定时（例如传闻现在得到证实），可以阅读某条内容得知的那一部分。每次阅读都会让模型多一轮请求。");
        fr(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "Le modèle peut lire la partie où un élément a été appris quand il a un doute (une rumeur désormais confirmée, par exemple). Chaque lecture est un tour de plus auprès du modèle.");
        es(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "El modelo puede leer la parte en la que se aprendió un elemento cuando no está seguro (por ejemplo, un rumor que ahora se confirma). Cada lectura es un turno más para el modelo.");
        de(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "Das Modell darf den Abschnitt lesen, in dem ein Element erfahren wurde, wenn es unsicher ist (zum Beispiel Gerüchte, die jetzt bestätigt sind). Jedes Lesen ist eine weitere Runde zum Modell.");
        ko(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART, "모델은 확신이 없을 때 항목을 알게 된 파트를 읽을 수 있습니다 (예: 지금은 확인된 소문). 읽을 때마다 모델에 라운드가 하나 더 추가됩니다.");

        ja(MET_PEOPLE, "会ったことがある人");
        zh(MET_PEOPLE, "见过的人");
        fr(MET_PEOPLE, "A rencontré");
        es(MET_PEOPLE, "Ha conocido a");
        de(MET_PEOPLE, "Ist begegnet");
        ko(MET_PEOPLE, "만난 사람");

        ja(PEOPLE_THE_CHARACTER_HAS_MET, "キャラクターが会ったことのある人をコンマで区切って入力します。処理はストーリーで見つけた人を追加します。削除した人は、ストーリーを再度読み込んだ場合にのみ戻ります。");
        zh(PEOPLE_THE_CHARACTER_HAS_MET, "角色见过的人，用逗号分隔。处理时会加入故事中出现的人；删除的人只有在重新读取故事时才会再出现。");
        fr(PEOPLE_THE_CHARACTER_HAS_MET, "Les personnes que le personnage a rencontrées, séparées par des virgules. Le traitement ajoute celles qu'il voit dans l'histoire ; celles que vous retirez ne reviennent que si l'histoire est relue.");
        es(PEOPLE_THE_CHARACTER_HAS_MET, "Personas a las que ha conocido el personaje, separadas por comas. El procesamiento añade las que ve en la historia; las que quites solo vuelven si se vuelve a leer la historia.");
        de(PEOPLE_THE_CHARACTER_HAS_MET, "Personen, denen die Figur begegnet ist, durch Kommas getrennt. Die Verarbeitung fügt die hinzu, die sie in der Geschichte sieht; die, die du entfernst, kommen nur wieder, wenn die Geschichte erneut gelesen wird.");
        ko(PEOPLE_THE_CHARACTER_HAS_MET, "캐릭터가 만난 사람들, 쉼표로 구분. 처리 시 이야기에서 보이는 사람이 추가되며, 제거한 사람은 이야기를 다시 읽을 때만 돌아옵니다.");

        ja(IN_PROMPT, "プロンプトに入れる");
        zh(IN_PROMPT, "加入提示词");
        fr(IN_PROMPT, "Dans le prompt");
        es(IN_PROMPT, "En el prompt");
        de(IN_PROMPT, "Im Prompt");
        ko(IN_PROMPT, "프롬프트에 포함");

        ja(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "場面にいる追跡キャラクターの知識を、すべての生成のプロンプトに挿入します");
        zh(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "将场景中被追踪角色的认知插入每次生成的提示词");
        fr(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "Insérer ce que savent les personnages suivis de la scène dans le prompt de chaque génération");
        es(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "Insertar en el prompt de cada generación lo que saben los personajes seguidos de la escena");
        de(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "Das, was die verfolgten Figuren der Szene wissen, in den Prompt jeder Generierung einfügen");
        ko(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW, "장면의 추적 캐릭터가 아는 것을 모든 생성의 프롬프트에 삽입");

        ja(DEPTH, "深さ");
        zh(DEPTH, "深度");
        fr(DEPTH, "Profondeur");
        es(DEPTH, "Profundidad");
        de(DEPTH, "Tiefe");
        ko(DEPTH, "깊이");

        ja(MESSAGES_AFTER_THE_KNOWLEDGE, "知識の後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_KNOWLEDGE, "认知之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");
        fr(MESSAGES_AFTER_THE_KNOWLEDGE, "Messages après les connaissances : 0 = après les instructions, 1 = juste avant elles");
        es(MESSAGES_AFTER_THE_KNOWLEDGE, "Mensajes después del conocimiento: 0 = después de las instrucciones, 1 = justo antes de ellas");
        de(MESSAGES_AFTER_THE_KNOWLEDGE, "Nachrichten nach dem Wissen: 0 = nach den Anweisungen, 1 = direkt davor");
        ko(MESSAGES_AFTER_THE_KNOWLEDGE, "지식 뒤의 메시지 수: 0 = 지시문 뒤, 1 = 지시문 바로 앞");

        ja(ROLE, "ロール");
        zh(ROLE, "角色");
        fr(ROLE, "Rôle");
        es(ROLE, "Rol");
        de(ROLE, "Rolle");
        ko(ROLE, "역할");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");
        fr(SYSTEM, "Système");
        es(SYSTEM, "Sistema");
        de(SYSTEM, "System");
        ko(SYSTEM, "시스템");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");
        fr(ASSISTANT, "Assistant");
        es(ASSISTANT, "Asistente");
        de(ASSISTANT, "Assistent");
        ko(ASSISTANT, "어시스턴트");

        ja(USER, "ユーザー");
        zh(USER, "用户");
        fr(USER, "Utilisateur");
        es(USER, "Usuario");
        de(USER, "Benutzer");
        ko(USER, "사용자");

        ja(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "それ以外は知らないと明記する");
        zh(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "注明不知道其他任何事");
        fr(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "Dire que rien d'autre n'est connu");
        es(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "Indicar que no se sabe nada más");
        de(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "Sagen, dass sonst nichts bekannt ist");
        ko(SAY_THAT_NOTHING_ELSE_IS_KNOWN, "그 외에는 아무것도 모른다고 명시");

        ja(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "キャラクターは他の誰にも会ったことも聞いたこともなく、物語の秘密や計画や出来事を他には知らない、という一行を加えます");
        zh(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "加一行说明：该角色没有见过也没有听说过其他任何人，也不知道故事中的其他秘密、计划或事件");
        fr(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "Ajouter une ligne disant que le personnage n'a jamais rencontré personne d'autre ni entendu parler de personne d'autre et ne sait rien d'autre des secrets de l'histoire");
        es(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "Añade una línea que dice que el personaje nunca ha conocido ni oído hablar de nadie más y no sabe nada más de los secretos de la historia");
        de(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "Eine Zeile hinzufügen, die besagt, dass die Figur niemand anderem begegnet ist oder von niemand anderem gehört hat und nichts sonst von den Geheimnissen der Geschichte weiß");
        ko(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD, "캐릭터가 다른 누구도 만나거나 들은 적이 없고 이야기의 다른 비밀을 알지 못한다는 한 줄을 추가합니다");

        ja(GIVE_THE_FACTS_TO_THE_ANNOTATION, "キャラクターの知識を注釈に渡す");
        zh(GIVE_THE_FACTS_TO_THE_ANNOTATION, "把角色所知内容提供给批注");
        fr(GIVE_THE_FACTS_TO_THE_ANNOTATION, "Transmettre à l'annotation ce que savent les personnages");
        es(GIVE_THE_FACTS_TO_THE_ANNOTATION, "Dar a la anotación lo que saben los personajes");
        de(GIVE_THE_FACTS_TO_THE_ANNOTATION, "Das, was die Figuren wissen, an die Anmerkung geben");
        ko(GIVE_THE_FACTS_TO_THE_ANNOTATION, "캐릭터가 아는 것을 주석에 전달");

        ja(WHEN_A_MODEL_ANNOTATES_A_PART, "モデルがパートに注釈を付けるとき（AIで注釈）、パートの始まりの時点で追跡中のキャラクターが知っていたことを伝え、知り得ないことに基づいて行動するキャラクターを指摘させます");
        zh(WHEN_A_MODEL_ANNOTATES_A_PART, "模型为某一段添加批注时（用 AI 批注），会被告知该段开始时被追踪角色所知道的内容，并指出依据其不可能知道的事情行事的角色");
        fr(WHEN_A_MODEL_ANNOTATES_A_PART, "Quand un modèle annote une partie (Annoter avec l'IA), il est informé de ce que savaient les personnages suivis au début de la partie, et signale ceux qui agissent selon ce qu'ils ne peuvent pas savoir");
        es(WHEN_A_MODEL_ANNOTATES_A_PART, "Cuando un modelo anota una parte (Anotar con IA), se le dice lo que sabían los personajes seguidos de la parte al comenzar, y señala a los que actúan según lo que no pueden saber");
        de(WHEN_A_MODEL_ANNOTATES_A_PART, "Wenn ein Modell einen Abschnitt kommentiert („Mit KI kommentieren“), wird ihm gesagt, was die verfolgten Figuren des Abschnitts zu Beginn wussten, und es markiert die, die nach etwas handeln, das sie nicht wissen können");
        ko(WHEN_A_MODEL_ANNOTATES_A_PART, "모델이 파트에 주석을 달 때(AI로 주석 달기), 파트가 시작될 때 추적 캐릭터가 알고 있던 것을 알려 주고, 알 수 없는 것에 따라 행동하는 캐릭터를 표시합니다");

        ja(SEPARATORS_OF_THIS_BOOK, "この本の区切り文字");
        zh(SEPARATORS_OF_THIS_BOOK, "本书的分隔符");
        fr(SEPARATORS_OF_THIS_BOOK, "Séparateurs de ce livre");
        es(SEPARATORS_OF_THIS_BOOK, "Separadores de este libro");
        de(SEPARATORS_OF_THIS_BOOK, "Trennzeichen dieses Buchs");
        ko(SEPARATORS_OF_THIS_BOOK, "이 책의 구분자");

        ja(MY_SEPARATORS, "自分の区切り文字（すべての本）");
        zh(MY_SEPARATORS, "我的分隔符（所有书）");
        fr(MY_SEPARATORS, "Mes séparateurs (tous les livres)");
        es(MY_SEPARATORS, "Mis separadores (todos los libros)");
        de(MY_SEPARATORS, "Meine Trennzeichen (alle Bücher)");
        ko(MY_SEPARATORS, "내 구분자 (모든 책)");

        ja(SEPARATORS_HELP, "「登場人物」欄を名前に分ける方法。1行に1つ。空 = 自分の設定を使用、自分の設定も空 = 組み込み（, ; 改行 / | 、 ， ；）。and のような単語は前後の空白で区切り、\\n は改行です。");
        zh(SEPARATORS_HELP, "如何把“在场角色”栏拆分成名字。每行一个；留空 = 使用我的设置，我的也留空 = 使用内置的（, ; 换行 / | 、 ， ；）。像 and 这样的单词按前后空格拆分，\\n 表示换行。");
        fr(SEPARATORS_HELP, "Comment le champ des personnages présents est découpé en noms. Un par ligne ; vide = le livre utilise les miens, les miens vides = ceux intégrés (, ; saut de ligne / | 、 ， ；). Un mot comme and découpe entre des espaces, \\n est un saut de ligne.");
        es(SEPARATORS_HELP, "Cómo se divide en nombres el campo de personajes presentes. Uno por línea; vacío = el libro usa los míos, los míos vacíos = los integrados (, ; salto de línea / | 、 ， ；). Una palabra como y divide entre espacios, \\n es un salto de línea.");
        de(SEPARATORS_HELP, "Wie das Feld „Anwesende Figuren“ in Namen aufgeteilt wird. Eines pro Zeile; leer = das Buch verwendet meine, meine leer = die eingebauten (, ; Zeilenumbruch / | 、 ， ；). Ein Wort wie „und“ trennt zwischen Leerzeichen, \\n ist ein Zeilenumbruch.");
        ko(SEPARATORS_HELP, "등장인물 필드를 이름으로 나누는 방법. 한 줄에 하나. 비워 두면 책은 내 것을 사용하고, 내 것도 비어 있으면 내장된 것(, ; 줄 바꿈 / | 、 ， ；)을 사용합니다. and 같은 단어는 공백 사이에서 나누며, \\n은 줄 바꿈입니다.");

        ja(PROCESSING, "処理");
        zh(PROCESSING, "处理");
        fr(PROCESSING, "Traitement");
        es(PROCESSING, "Procesamiento");
        de(PROCESSING, "Verarbeitung");
        ko(PROCESSING, "처리");

        ja(PROCESSED_UP_TO_PART, "%d / %d パートまで処理済み");
        zh(PROCESSED_UP_TO_PART, "已处理到第 %d 部分（共 %d 部分）");
        fr(PROCESSED_UP_TO_PART, "Traité jusqu'à la partie %d sur %d");
        es(PROCESSED_UP_TO_PART, "Procesado hasta la parte %d de %d");
        de(PROCESSED_UP_TO_PART, "Verarbeitet bis Abschnitt %d von %d");
        ko(PROCESSED_UP_TO_PART, "파트 %d / %d까지 처리됨");

        ja(NOTHING_IS_PROCESSED_YET, "まだ何も処理されていません（%d パート）");
        zh(NOTHING_IS_PROCESSED_YET, "尚未处理任何内容（共 %d 部分）");
        fr(NOTHING_IS_PROCESSED_YET, "Rien n'est encore traité (%d parties)");
        es(NOTHING_IS_PROCESSED_YET, "Todavía no se ha procesado nada (%d partes)");
        de(NOTHING_IS_PROCESSED_YET, "Noch nichts verarbeitet (%d Abschnitte)");
        ko(NOTHING_IS_PROCESSED_YET, "아직 처리된 것이 없습니다 (파트 %d개)");

        ja(PROCESS_NOW, "今すぐ処理");
        zh(PROCESS_NOW, "立即处理");
        fr(PROCESS_NOW, "Traiter maintenant");
        es(PROCESS_NOW, "Procesar ahora");
        de(PROCESS_NOW, "Jetzt verarbeiten");
        ko(PROCESS_NOW, "지금 처리");

        ja(READ_N_PARTS_IN_N_REQUESTS, "%d 個のパートをモデルで %d 回のリクエストに分けて読みますか？物語の本文だけで約 %d トークンです。指示、ロアブック、キャラクターの知識が毎回のリクエストにさらに付きます。シリーズはいつでもキャンセルでき、適用済みの内容は残ります。");
        zh(READ_N_PARTS_IN_N_REQUESTS, "用模型分 %d 个部分、%d 次请求读取吗？仅故事正文就约 %d 个 token，每次请求还会附带指令、设定集和角色的知识。可随时取消该系列；已应用的内容会保留。");
        fr(READ_N_PARTS_IN_N_REQUESTS, "Lire %d parties avec le modèle en %d requêtes ? Le texte de l'histoire seul fait environ %d jetons, les instructions, le lorebook et les connaissances des personnages sont envoyés en plus avec chaque requête. Vous pouvez annuler la série à tout moment ; ce qui a été appliqué reste.");
        es(READ_N_PARTS_IN_N_REQUESTS, "¿Leer %d partes con el modelo en %d solicitudes? El texto de la historia por sí solo son unos %d tokens; las instrucciones, el lorebook y el conocimiento de los personajes se envían con cada solicitud además de eso. Puedes cancelar la serie en cualquier momento; lo que se aplicó se queda.");
        de(READ_N_PARTS_IN_N_REQUESTS, "%d Abschnitte mit dem Modell in %d Anfragen lesen? Der Text der Geschichte allein umfasst etwa %d Tokens, die Anweisungen, das Lorebook und das Wissen der Figuren werden bei jeder Anfrage zusätzlich gesendet. Du kannst die Serie jederzeit abbrechen; was angewendet wurde, bleibt.");
        ko(READ_N_PARTS_IN_N_REQUESTS, "모델로 요청 %d회에 걸쳐 파트 %d개를 읽으시겠습니까? 이야기 텍스트만 약 %d 토큰이며, 지시문, 로어북, 캐릭터가 아는 것은 모든 요청에 추가로 전송됩니다. 시리즈는 언제든지 취소할 수 있으며 이미 적용된 것은 유지됩니다.");
        ja(ADDED_TO, "%s に追加");
        zh(ADDED_TO, "添加给 %s");
        fr(ADDED_TO, "Ajouté à %s");
        es(ADDED_TO, "Añadido a %s");
        de(ADDED_TO, "Hinzugefügt zu %s");
        ko(ADDED_TO, "%s에 추가됨");
        ja(RETRACTED_FROM, "%s から撤回");
        zh(RETRACTED_FROM, "从 %s 撤回");
        fr(RETRACTED_FROM, "Retiré de %s");
        es(RETRACTED_FROM, "Retirado de %s");
        de(RETRACTED_FROM, "Zurückgezogen von %s");
        ko(RETRACTED_FROM, "%s에서 철회됨");
        ja(REVERTED, "元に戻しました");
        zh(REVERTED, "已撤销");
        fr(REVERTED, "Annulé");
        es(REVERTED, "Revertido");
        de(REVERTED, "Rückgängig gemacht");
        ko(REVERTED, "되돌려짐");
        ja(REVERT_THIS_CHANGE, "この変更を元に戻す");
        zh(REVERT_THIS_CHANGE, "撤销此更改");
        fr(REVERT_THIS_CHANGE, "Annuler cette modification");
        es(REVERT_THIS_CHANGE, "Revertir este cambio");
        de(REVERT_THIS_CHANGE, "Diese Änderung rückgängig machen");
        ko(REVERT_THIS_CHANGE, "이 변경 되돌리기");
        ja(APPLIED_AUTOMATICALLY, "自動で適用された変更 (%d)");
        zh(APPLIED_AUTOMATICALLY, "自动应用的更改 (%d)");
        fr(APPLIED_AUTOMATICALLY, "Appliqué automatiquement (%d)");
        es(APPLIED_AUTOMATICALLY, "Aplicados automáticamente (%d)");
        de(APPLIED_AUTOMATICALLY, "Automatisch angewendet (%d)");
        ko(APPLIED_AUTOMATICALLY, "자동 적용됨 (%d)");
        ja(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "項目はその後変更されたため、そのままにしました");
        zh(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "该项此后已被更改，未做任何改动");
        fr(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "L'élément a été modifié depuis, il n'a pas été touché");
        es(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "El elemento cambió después, no se tocó");
        de(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "Das Element wurde seitdem geändert, es wurde nicht angetastet");
        ko(THE_ITEM_HAS_BEEN_CHANGED_SINCE, "항목이 그 후에 변경되어 건드리지 않았습니다");
        ja(PROCESS_NEXT_PART, "次のパートを処理");
        zh(PROCESS_NEXT_PART, "处理下一部分");
        fr(PROCESS_NEXT_PART, "Traiter la partie suivante");
        es(PROCESS_NEXT_PART, "Procesar la parte siguiente");
        de(PROCESS_NEXT_PART, "Nächsten Abschnitt verarbeiten");
        ko(PROCESS_NEXT_PART, "다음 파트 처리");
        ja(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "まだ処理されていない最初のパートだけを読みます。一度に一歩ずつ進めたいときに使います");
        zh(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "只读取第一个尚未处理的部分，便于一步一步来");
        fr(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Lire seulement la première partie non encore traitée, pour avancer pas à pas");
        es(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Lee solo la primera parte que todavía no está procesada, para ir paso a paso");
        de(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Nur den ersten noch nicht verarbeiteten Abschnitt lesen, für einen Schritt nach dem anderen");
        ko(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "한 번에 한 단계씩, 아직 처리되지 않은 첫 파트만 읽습니다");
        ja(CHANGES_FROM_A_COMMAND_TO_REVIEW, "コマンドが見つけた変更 %d 件を確認する");
        zh(CHANGES_FROM_A_COMMAND_TO_REVIEW, "命令找到了 %d 项更改，请查看");
        fr(CHANGES_FROM_A_COMMAND_TO_REVIEW, "%d modifications trouvées par une commande, examinez-les");
        es(CHANGES_FROM_A_COMMAND_TO_REVIEW, "%d cambios encontrados por un comando, revísalos");
        de(CHANGES_FROM_A_COMMAND_TO_REVIEW, "%d von einem Befehl gefundene Änderungen, prüfe sie");
        ko(CHANGES_FROM_A_COMMAND_TO_REVIEW, "명령이 찾은 변경 %d건, 검토하세요");
        ja(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "変更の待機中に物語が変わりました。コマンドをもう一度実行してください");
        zh(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "等待期间故事已更改，请重新运行命令");
        fr(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "L'histoire a changé pendant que les modifications attendaient ; relancez la commande");
        es(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "La historia cambió mientras los cambios esperaban; ejecuta el comando de nuevo");
        de(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "Die Geschichte hat sich geändert, während die Änderungen warteten; führe den Befehl erneut aus");
        ko(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN, "변경 사항이 대기하는 동안 이야기가 변경되었습니다. 명령을 다시 실행하세요");
        ja(WHAT_THE_MODEL_THOUGHT, "モデルの考え");
        zh(WHAT_THE_MODEL_THOUGHT, "模型的思考");
        fr(WHAT_THE_MODEL_THOUGHT, "Ce que le modèle a pensé");
        es(WHAT_THE_MODEL_THOUGHT, "Lo que pensó el modelo");
        de(WHAT_THE_MODEL_THOUGHT, "Was das Modell gedacht hat");
        ko(WHAT_THE_MODEL_THOUGHT, "모델의 생각");
        ja(TIDY_PARTS_OF_CONTEXT, "整理：文脈のパート数");
        zh(TIDY_PARTS_OF_CONTEXT, "整理：上下文部分数");
        fr(TIDY_PARTS_OF_CONTEXT, "Rangement : parties de contexte");
        es(TIDY_PARTS_OF_CONTEXT, "Ordenar: partes de contexto");
        de(TIDY_PARTS_OF_CONTEXT, "Aufräumen: Kontextabschnitte");
        ko(TIDY_PARTS_OF_CONTEXT, "정리: 컨텍스트 파트 수");
        ja(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "整理の際に文脈として読む、最後に処理されたパートの数");
        zh(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "整理时作为上下文读取的最后已处理部分数");
        fr(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "Les dernières parties traitées que le rangement lit comme contexte");
        es(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "Las últimas partes procesadas que lee la pasada de ordenación como contexto");
        de(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "Die letzten verarbeiteten Abschnitte, die der Aufräumdurchgang als Kontext liest");
        ko(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS, "정리 단계가 컨텍스트로 읽는 마지막 처리된 파트");
        ja(TIDY_FROM_N_ITEMS, "整理：この数以上の項目から");
        zh(TIDY_FROM_N_ITEMS, "整理：至少这么多项时");
        fr(TIDY_FROM_N_ITEMS, "Rangement : à partir de ce nombre d'éléments");
        es(TIDY_FROM_N_ITEMS, "Ordenar: desde esta cantidad de elementos");
        de(TIDY_FROM_N_ITEMS, "Aufräumen: ab so vielen Elementen");
        ko(TIDY_FROM_N_ITEMS, "정리: 이 항목 수부터");
        ja(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "モデルが学習した項目がこれより少ないキャラクターは整理しません");
        zh(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "模型学到的项数少于此值的角色不会被整理");
        fr(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "Un personnage ayant moins d'éléments appris par le modèle n'est pas rangé");
        es(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "Un personaje con menos elementos aprendidos por el modelo no se ordena");
        de(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "Eine Figur mit weniger vom Modell erfahrenen Elementen wird nicht aufgeräumt");
        ko(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED, "모델이 알게 된 항목이 이보다 적은 캐릭터는 정리하지 않습니다");
        ja(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "ロック中：整理の対象外です。クリックで解除");
        zh(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "已锁定：整理时不会处理。点击解锁");
        fr(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "Verrouillé : le rangement n'y touche pas. Cliquez pour déverrouiller");
        es(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "Bloqueado: la pasada de ordenación no lo toca. Haz clic para desbloquear");
        de(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "Gesperrt: Der Aufräumdurchgang lässt es in Ruhe. Klicken zum Entsperren");
        ko(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE, "잠김: 정리 단계가 건드리지 않습니다. 클릭하여 잠금 해제");
        ja(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "整理の対象外にするためロックします（物語が矛盾すれば撤回はされます）");
        zh(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "锁定后整理时不会处理（故事若与之矛盾仍会撤回）");
        fr(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "Le verrouiller pour que le rangement n'y touche pas (l'histoire peut toujours le contredire)");
        es(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "Bloquéalo para que la pasada de ordenación no lo toque (la historia aún puede contradecirlo)");
        de(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "Sperren, damit der Aufräumdurchgang es in Ruhe lässt (die Geschichte kann ihm trotzdem widersprechen)");
        ko(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE, "정리 단계가 건드리지 않도록 잠급니다 (이야기가 이와 모순될 수는 있음)");
        ja(TIDY_KNOWLEDGE, "知識を整理");
        zh(TIDY_KNOWLEDGE, "整理知识");
        fr(TIDY_KNOWLEDGE, "Ranger les connaissances");
        es(TIDY_KNOWLEDGE, "Ordenar el conocimiento");
        de(TIDY_KNOWLEDGE, "Wissen aufräumen");
        ko(TIDY_KNOWLEDGE, "지식 정리");
        ja(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "残す価値のない項目をモデルが削除または統合します。ロックした項目と自分で書いた項目は対象外です");
        zh(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "模型会删除或合并不值得保留的项；已锁定的项和你写的项不会被处理");
        fr(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "Le modèle abandonne ou fusionne les éléments qui ne valent pas d'être gardés ; les éléments verrouillés et ce que vous avez écrit ne sont pas touchés");
        es(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "El modelo descarta o combina los elementos que no merece la pena conservar; los elementos bloqueados y lo que escribiste tú no se tocan");
        de(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "Das Modell verwirft oder führt Elemente zusammen, die nicht aufbewahrenswert sind; gesperrte Elemente und das, was du geschrieben hast, werden nicht angetastet");
        ko(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING, "모델이 유지할 가치가 없는 항목을 버리거나 병합합니다. 잠긴 항목과 직접 쓴 것은 건드리지 않습니다");
        ja(REVIEW_THE_TIDY, "整理の確認");
        zh(REVIEW_THE_TIDY, "查看整理结果");
        fr(REVIEW_THE_TIDY, "Examiner le rangement");
        es(REVIEW_THE_TIDY, "Revisar la ordenación");
        de(REVIEW_THE_TIDY, "Das Aufräumen prüfen");
        ko(REVIEW_THE_TIDY, "정리 검토");
        ja(WHY, "理由：%s");
        zh(WHY, "原因：%s");
        fr(WHY, "Pourquoi : %s");
        es(WHY, "Motivo: %s");
        de(WHY, "Warum: %s");
        ko(WHY, "이유: %s");
        ja(NOTHING_TO_TIDY, "整理：削除や統合するものはありません");
        zh(NOTHING_TO_TIDY, "整理：没有要删除或合并的内容");
        fr(NOTHING_TO_TIDY, "Rangement : rien à abandonner ni à fusionner");
        es(NOTHING_TO_TIDY, "Ordenar: nada que descartar ni combinar");
        de(NOTHING_TO_TIDY, "Aufräumen: nichts zu verwerfen oder zusammenzuführen");
        ko(NOTHING_TO_TIDY, "정리: 버리거나 병합할 것이 없습니다");
        ja(TIDY_DONE, "整理：%d 件が古い知識になり、統合した項目が %d 件追加されました");
        zh(TIDY_DONE, "整理：%d 项变为旧知识，新增 %d 项合并项");
        fr(TIDY_DONE, "Rangement : %d élément(s) devenu(s) anciennes connaissances, %d élément(s) fusionné(s) ajouté(s)");
        es(TIDY_DONE, "Ordenar: %d elemento(s) pasaron a ser conocimiento antiguo, %d elemento(s) combinado(s) añadido(s)");
        de(TIDY_DONE, "Aufräumen: %d Element(e) wurden zu altem Wissen, %d zusammengeführte(s) Element(e) hinzugefügt");
        ko(TIDY_DONE, "정리: 항목 %d개가 오래된 지식이 되었고 병합된 항목 %d개가 추가되었습니다");
        ja(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "チェックを外した変更では項目はそのままです。チェックを外した統合項目は追加されません。");
        zh(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "取消勾选的更改会保持原有项不变；取消勾选的合并项不会被添加。");
        fr(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "Une modification décochée laisse ses éléments tels quels ; un élément fusionné décoché n'est pas ajouté.");
        es(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "Un cambio que desmarques deja sus elementos como están; un elemento combinado sin marcar no se añade.");
        de(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "Eine Änderung, die du abwählst, lässt ihre Elemente, wie sie sind; ein abgewähltes zusammengeführtes Element wird nicht hinzugefügt.");
        ko(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS, "체크 해제한 변경은 해당 항목을 그대로 둡니다. 체크 해제한 병합 항목은 추가되지 않습니다.");
        ja(TIDY_AUTOMATICALLY, "自動で整理する");
        zh(TIDY_AUTOMATICALLY, "自动整理");
        fr(TIDY_AUTOMATICALLY, "Ranger automatiquement");
        es(TIDY_AUTOMATICALLY, "Ordenar automáticamente");
        de(TIDY_AUTOMATICALLY, "Automatisch aufräumen");
        ko(TIDY_AUTOMATICALLY, "자동으로 정리");
        ja(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "確認なしで変更を適用する実行の最後に、前回の整理から十分なパートを読んだ場合に整理します");
        zh(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "在不经确认就应用更改的运行结束时，若自上次整理后读取了足够多的部分，则自动整理");
        fr(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "À la fin d'un traitement qui applique ses modifications sans demander, quand assez de parties ont été lues depuis le dernier rangement");
        es(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "Al final de una ejecución que aplica sus cambios sin preguntar, cuando se han leído suficientes partes desde la última ordenación");
        de(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "Am Ende eines Laufs, der seine Änderungen ohne Rückfrage anwendet, wenn seit dem letzten Aufräumen genug Abschnitte gelesen wurden");
        ko(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "묻지 않고 변경을 적용하는 실행이 끝날 때, 마지막 정리 이후 충분한 파트를 읽었으면 실행합니다");
        ja(TIDY_AFTER_N_PARTS, "整理するまでのパート数");
        zh(TIDY_AFTER_N_PARTS, "每隔多少部分整理一次");
        fr(TIDY_AFTER_N_PARTS, "Ranger après ce nombre de parties");
        es(TIDY_AFTER_N_PARTS, "Ordenar tras esta cantidad de partes");
        de(TIDY_AFTER_N_PARTS, "Aufräumen nach so vielen Abschnitten");
        ko(TIDY_AFTER_N_PARTS, "이 파트 수 후에 정리");
        ja(REQUEST_N_CHANGES, "リクエスト %d：提案された変更 %d 件");
        zh(REQUEST_N_CHANGES, "请求 %d：提出 %d 项更改");
        fr(REQUEST_N_CHANGES, "Requête %d : %d modification(s) proposée(s)");
        es(REQUEST_N_CHANGES, "Solicitud %d: %d cambio(s) propuesto(s)");
        de(REQUEST_N_CHANGES, "Anfrage %d: %d Änderung(en) vorgeschlagen");
        ko(REQUEST_N_CHANGES, "요청 %d: 변경 %d건 제안됨");
        ja(TIDY_REQUEST_N_CHANGES, "整理リクエスト %d：提案された変更 %d 件");
        zh(TIDY_REQUEST_N_CHANGES, "整理请求 %d：提出 %d 项更改");
        fr(TIDY_REQUEST_N_CHANGES, "Requête de rangement %d : %d modification(s) proposée(s)");
        es(TIDY_REQUEST_N_CHANGES, "Solicitud de ordenación %d: %d cambio(s) propuesto(s)");
        de(TIDY_REQUEST_N_CHANGES, "Aufräum-Anfrage %d: %d Änderung(en) vorgeschlagen");
        ko(TIDY_REQUEST_N_CHANGES, "정리 요청 %d: 변경 %d건 제안됨");
        ja(THE_MODEL_SENT_NO_REASONING, "モデルは推論を送っていません");
        zh(THE_MODEL_SENT_NO_REASONING, "模型没有发送推理内容");
        fr(THE_MODEL_SENT_NO_REASONING, "Le modèle n'a envoyé aucun raisonnement");
        es(THE_MODEL_SENT_NO_REASONING, "El modelo no envió razonamiento");
        de(THE_MODEL_SENT_NO_REASONING, "Das Modell hat kein Reasoning gesendet");
        ko(THE_MODEL_SENT_NO_REASONING, "모델이 추론을 보내지 않았습니다");
        ja(WHAT_THE_MODEL_ANSWERED, "モデルの回答");
        zh(WHAT_THE_MODEL_ANSWERED, "模型的回答");
        fr(WHAT_THE_MODEL_ANSWERED, "Ce que le modèle a répondu");
        es(WHAT_THE_MODEL_ANSWERED, "Lo que respondió el modelo");
        de(WHAT_THE_MODEL_ANSWERED, "Was das Modell geantwortet hat");
        ko(WHAT_THE_MODEL_ANSWERED, "모델이 응답한 내용");
        ja(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "前回の実行：モデルの考え");
        zh(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "上次运行：模型的思考");
        fr(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Dernier traitement : ce que le modèle a pensé");
        es(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Última ejecución: lo que pensó el modelo");
        de(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Letzter Lauf: was das Modell gedacht hat");
        ko(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "마지막 실행: 모델의 생각");
        ja(THE_MODEL_IS_WRITING, "モデルが書いている内容");
        zh(THE_MODEL_IS_WRITING, "模型正在写的内容");
        fr(THE_MODEL_IS_WRITING, "Ce que le modèle écrit");
        es(THE_MODEL_IS_WRITING, "Lo que está escribiendo el modelo");
        de(THE_MODEL_IS_WRITING, "Was das Modell schreibt");
        ko(THE_MODEL_IS_WRITING, "모델이 쓰는 내용");
        ja(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "まだ処理されていないパートを読み、誰が誰に会ったかを記録します");
        zh(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "读取尚未处理的部分，并记录谁见过谁");
        fr(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "Lire les parties pas encore traitées et noter qui a rencontré qui");
        es(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "Lee las partes que todavía no están procesadas y anota quién ha conocido a quién");
        de(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "Die noch nicht verarbeiteten Abschnitte lesen und festhalten, wer wem begegnet ist");
        ko(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED, "아직 처리되지 않은 파트를 읽고 누가 누구를 만났는지 기록합니다");

        ja(START_POINT, "開始位置");
        zh(START_POINT, "起点");
        fr(START_POINT, "Point de départ");
        es(START_POINT, "Punto de inicio");
        de(START_POINT, "Startpunkt");
        ko(START_POINT, "시작 지점");

        ja(START_OF_THE_BOOK, "本の最初");
        zh(START_OF_THE_BOOK, "书的开头");
        fr(START_OF_THE_BOOK, "Début du livre");
        es(START_OF_THE_BOOK, "Inicio del libro");
        de(START_OF_THE_BOOK, "Anfang des Buchs");
        ko(START_OF_THE_BOOK, "책의 시작");

        ja(PARTS_BEFORE_THE_START_POINT, "これより前のパートは処理されません");
        zh(PARTS_BEFORE_THE_START_POINT, "此前的部分永远不会被处理");
        fr(PARTS_BEFORE_THE_START_POINT, "Les parties d'avant ne sont jamais traitées");
        es(PARTS_BEFORE_THE_START_POINT, "Las partes anteriores nunca se procesan");
        de(PARTS_BEFORE_THE_START_POINT, "Abschnitte davor werden nie verarbeitet");
        ko(PARTS_BEFORE_THE_START_POINT, "그 앞의 파트는 처리되지 않습니다");

        ja(PROCESSED_PARTS, "処理済みのパート（%d）");
        zh(PROCESSED_PARTS, "已处理的部分（%d）");
        fr(PROCESSED_PARTS, "Parties traitées (%d)");
        es(PROCESSED_PARTS, "Partes procesadas (%d)");
        de(PROCESSED_PARTS, "Verarbeitete Abschnitte (%d)");
        ko(PROCESSED_PARTS, "처리된 파트 (%d)");

        ja(CHANGED_SINCE_PROCESSED, "処理後に変更されました");
        zh(CHANGED_SINCE_PROCESSED, "处理后已更改");
        fr(CHANGED_SINCE_PROCESSED, "Modifiée depuis le traitement");
        es(CHANGED_SINCE_PROCESSED, "Modificada desde que se procesó");
        de(CHANGED_SINCE_PROCESSED, "Seit der Verarbeitung geändert");
        ko(CHANGED_SINCE_PROCESSED, "처리 후 변경됨");

        ja(REPROCESS_FROM_HERE, "ここから再処理");
        zh(REPROCESS_FROM_HERE, "从此处重新处理");
        fr(REPROCESS_FROM_HERE, "Retraiter à partir d'ici");
        es(REPROCESS_FROM_HERE, "Volver a procesar desde aquí");
        de(REPROCESS_FROM_HERE, "Ab hier neu verarbeiten");
        ko(REPROCESS_FROM_HERE, "여기서부터 다시 처리");

        ja(REPROCESS_FROM_PART, "パート %d から再処理しますか？そのパートと以降のパートから得られた内容は破棄され、読み直されます。");
        zh(REPROCESS_FROM_PART, "要从第 %d 部分重新处理吗？由该部分及之后部分得到的内容会被丢弃并重新读取。");
        fr(REPROCESS_FROM_PART, "Retraiter à partir de la partie %d ? Ce qui a été appris d'elle et des parties suivantes est abandonné et relu.");
        es(REPROCESS_FROM_PART, "¿Volver a procesar desde la parte %d? Lo aprendido de ella y de las partes posteriores se descarta y se vuelve a leer.");
        de(REPROCESS_FROM_PART, "Ab Abschnitt %d neu verarbeiten? Was daraus und aus den späteren Abschnitten erfahren wurde, wird verworfen und erneut gelesen.");
        ko(REPROCESS_FROM_PART, "파트 %d부터 다시 처리하시겠습니까? 그 파트와 이후 파트에서 알게 된 것은 버려지고 다시 읽습니다.");

        ja(PROCESS_NEW_PARTS_AUTOMATICALLY, "新しいパートを自動で処理");
        zh(PROCESS_NEW_PARTS_AUTOMATICALLY, "自动处理新的部分");
        fr(PROCESS_NEW_PARTS_AUTOMATICALLY, "Traiter automatiquement les nouvelles parties");
        es(PROCESS_NEW_PARTS_AUTOMATICALLY, "Procesar las partes nuevas automáticamente");
        de(PROCESS_NEW_PARTS_AUTOMATICALLY, "Neue Abschnitte automatisch verarbeiten");
        ko(PROCESS_NEW_PARTS_AUTOMATICALLY, "새 파트를 자동으로 처리");

        ja(BEFORE_A_GENERATION_THE_NEW_PARTS, "生成の前に、前回以降に書かれたパートを処理し、プロンプトが誰が誰に会ったかを把握できるようにします");
        zh(BEFORE_A_GENERATION_THE_NEW_PARTS, "生成之前先处理上次以来写下的部分，使提示词知道谁见过谁");
        fr(BEFORE_A_GENERATION_THE_NEW_PARTS, "Avant une génération, traiter les parties écrites depuis la dernière fois, pour que le prompt sache qui a rencontré qui");
        es(BEFORE_A_GENERATION_THE_NEW_PARTS, "Antes de una generación, procesa las partes escritas desde la última vez, para que el prompt sepa quién ha conocido a quién");
        de(BEFORE_A_GENERATION_THE_NEW_PARTS, "Vor einer Generierung die seit dem letzten Mal geschriebenen Abschnitte verarbeiten, damit der Prompt weiß, wer wem begegnet ist");
        ko(BEFORE_A_GENERATION_THE_NEW_PARTS, "생성 전에 마지막 이후에 쓰인 파트를 처리하여 프롬프트가 누가 누구를 만났는지 알게 합니다");

        ja(PART_N, "パート %d");
        zh(PART_N, "第 %d 部分");
        fr(PART_N, "Partie %d");
        es(PART_N, "Parte %d");
        de(PART_N, "Abschnitt %d");
        ko(PART_N, "파트 %d");

        ja(PROCESSED_N_PARTS, "%d パートを処理しました");
        zh(PROCESSED_N_PARTS, "已处理 %d 个部分");
        fr(PROCESSED_N_PARTS, "%d parties traitées");
        es(PROCESSED_N_PARTS, "Se han procesado %d partes");
        de(PROCESSED_N_PARTS, "%d Abschnitte verarbeitet");
        ko(PROCESSED_N_PARTS, "파트 %d개를 처리했습니다");

        ja(NOTHING_NEW_TO_PROCESS, "処理するものはありません");
        zh(NOTHING_NEW_TO_PROCESS, "没有需要处理的内容");
        fr(NOTHING_NEW_TO_PROCESS, "Rien de nouveau à traiter");
        es(NOTHING_NEW_TO_PROCESS, "No hay nada nuevo que procesar");
        de(NOTHING_NEW_TO_PROCESS, "Nichts Neues zu verarbeiten");
        ko(NOTHING_NEW_TO_PROCESS, "처리할 새 파트가 없습니다");

        ja(THE_BOOK_HAS_NO_PARTS_YET, "この本にはまだパートがありません");
        zh(THE_BOOK_HAS_NO_PARTS_YET, "这本书还没有任何部分");
        fr(THE_BOOK_HAS_NO_PARTS_YET, "Le livre n'a pas encore de parties");
        es(THE_BOOK_HAS_NO_PARTS_YET, "El libro todavía no tiene partes");
        de(THE_BOOK_HAS_NO_PARTS_YET, "Das Buch hat noch keine Abschnitte");
        ko(THE_BOOK_HAS_NO_PARTS_YET, "책에 아직 파트가 없습니다");

        ja(PROCESSING_THE_STORY, "物語を処理中");
        zh(PROCESSING_THE_STORY, "正在处理故事");
        fr(PROCESSING_THE_STORY, "Traitement de l'histoire");
        es(PROCESSING_THE_STORY, "Procesando la historia");
        de(PROCESSING_THE_STORY, "Die Geschichte wird verarbeitet");
        ko(PROCESSING_THE_STORY, "이야기를 처리하는 중");

        ja(PREPARING, "準備中…");
        zh(PREPARING, "准备中…");
        fr(PREPARING, "Préparation…");
        es(PREPARING, "Preparando…");
        de(PREPARING, "Wird vorbereitet …");
        ko(PREPARING, "준비 중…");

        ja(ASKING_THE_MODEL_RUN, "モデルに質問中、%d 回目");
        zh(ASKING_THE_MODEL_RUN, "正在询问模型，第 %d 轮");
        fr(ASKING_THE_MODEL_RUN, "Interrogation du modèle, passe %d");
        es(ASKING_THE_MODEL_RUN, "Consultando al modelo, ejecución %d");
        de(ASKING_THE_MODEL_RUN, "Das Modell wird gefragt, Lauf %d");
        ko(ASKING_THE_MODEL_RUN, "모델에 요청하는 중, 실행 %d");

        ja(WAITING_FOR_YOUR_REVIEW, "%d 件の変更が確認を待っています");
        zh(WAITING_FOR_YOUR_REVIEW, "有 %d 项更改等待您审阅");
        fr(WAITING_FOR_YOUR_REVIEW, "%d modifications attendent votre examen");
        es(WAITING_FOR_YOUR_REVIEW, "%d cambios esperan tu revisión");
        de(WAITING_FOR_YOUR_REVIEW, "%d Änderungen warten auf deine Prüfung");
        ko(WAITING_FOR_YOUR_REVIEW, "변경 %d건이 검토를 기다리고 있습니다");

        ja(APPLYING_THE_CHANGES, "変更を適用中");
        zh(APPLYING_THE_CHANGES, "正在应用更改");
        fr(APPLYING_THE_CHANGES, "Application des modifications");
        es(APPLYING_THE_CHANGES, "Aplicando los cambios");
        de(APPLYING_THE_CHANGES, "Die Änderungen werden angewendet");
        ko(APPLYING_THE_CHANGES, "변경 사항을 적용하는 중");

        ja(DONE_PARTS_ADDED_RETRACTED, "完了：%d パートを読み、%d 件追加、%d 件撤回");
        zh(DONE_PARTS_ADDED_RETRACTED, "完成：读取 %d 个部分，添加 %d 条，撤回 %d 条");
        fr(DONE_PARTS_ADDED_RETRACTED, "Terminé : %d parties lues, %d ajoutés, %d retirés");
        es(DONE_PARTS_ADDED_RETRACTED, "Hecho: %d partes leídas, %d añadidos, %d retirados");
        de(DONE_PARTS_ADDED_RETRACTED, "Fertig: %d Abschnitte gelesen, %d hinzugefügt, %d zurückgezogen");
        ko(DONE_PARTS_ADDED_RETRACTED, "완료: 파트 %d개 읽음, %d개 추가, %d개 철회");

        ja(IT_FAILED, "失敗しました：%s");
        zh(IT_FAILED, "失败：%s");
        fr(IT_FAILED, "Échec : %s");
        es(IT_FAILED, "Ha fallado: %s");
        de(IT_FAILED, "Fehlgeschlagen: %s");
        ko(IT_FAILED, "실패했습니다: %s");

        ja(STOPPED_PARTS_ADDED_RETRACTED, "停止：%d パートを読み、%d 件追加、%d 件撤回");
        zh(STOPPED_PARTS_ADDED_RETRACTED, "已停止：读取 %d 个部分，添加 %d 条，撤回 %d 条");
        fr(STOPPED_PARTS_ADDED_RETRACTED, "Arrêté : %d parties lues, %d ajoutés, %d retirés");
        es(STOPPED_PARTS_ADDED_RETRACTED, "Detenido: %d partes leídas, %d añadidos, %d retirados");
        de(STOPPED_PARTS_ADDED_RETRACTED, "Gestoppt: %d Abschnitte gelesen, %d hinzugefügt, %d zurückgezogen");
        ko(STOPPED_PARTS_ADDED_RETRACTED, "중지됨: 파트 %d개 읽음, %d개 추가, %d개 철회");

        ja(REVIEW_CHANGES, "変更を確認");
        zh(REVIEW_CHANGES, "审阅更改");
        fr(REVIEW_CHANGES, "Examiner les modifications");
        es(REVIEW_CHANGES, "Revisar los cambios");
        de(REVIEW_CHANGES, "Änderungen prüfen");
        ko(REVIEW_CHANGES, "변경 사항 검토");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");

        ja(LOG, "ログ");
        zh(LOG, "日志");
        fr(LOG, "Journal");
        es(LOG, "Registro");
        de(LOG, "Protokoll");
        ko(LOG, "로그");

        ja(REVIEW_THE_CHANGES_RUN, "変更の確認（%d 回目）");
        zh(REVIEW_THE_CHANGES_RUN, "审阅更改（第 %d 轮）");
        fr(REVIEW_THE_CHANGES_RUN, "Examiner les modifications (passe %d)");
        es(REVIEW_THE_CHANGES_RUN, "Revisar los cambios (ejecución %d)");
        de(REVIEW_THE_CHANGES_RUN, "Die Änderungen prüfen (Lauf %d)");
        ko(REVIEW_THE_CHANGES_RUN, "변경 사항 검토 (실행 %d)");

        ja(APPLY, "適用");
        zh(APPLY, "应用");
        fr(APPLY, "Appliquer");
        es(APPLY, "Aplicar");
        de(APPLY, "Anwenden");
        ko(APPLY, "적용");

        ja(CHECK_ALL, "すべて選択");
        zh(CHECK_ALL, "全选");
        fr(CHECK_ALL, "Tout cocher");
        es(CHECK_ALL, "Marcar todo");
        de(CHECK_ALL, "Alle markieren");
        ko(CHECK_ALL, "모두 체크");

        ja(UNCHECK_ALL, "すべて解除");
        zh(UNCHECK_ALL, "全部取消");
        fr(UNCHECK_ALL, "Tout décocher");
        es(UNCHECK_ALL, "Desmarcar todo");
        de(UNCHECK_ALL, "Markierung aller aufheben");
        ko(UNCHECK_ALL, "모두 체크 해제");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajout");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");

        ja(CHANGE, "変更");
        zh(CHANGE, "更改");
        fr(CHANGE, "Modification");
        es(CHANGE, "Cambiar");
        de(CHANGE, "Änderung");
        ko(CHANGE, "변경");

        ja(ITEM, "項目");
        zh(ITEM, "条目");
        fr(ITEM, "Élément");
        es(ITEM, "Elemento");
        de(ITEM, "Element");
        ko(ITEM, "항목");

        ja(EVIDENCE, "根拠");
        zh(EVIDENCE, "依据");
        fr(EVIDENCE, "Preuve");
        es(EVIDENCE, "Evidencia");
        de(EVIDENCE, "Beleg");
        ko(EVIDENCE, "근거");

        ja(IN_PART_N_QUOTE, "パート %d：「%s」");
        zh(IN_PART_N_QUOTE, "第 %d 部分：“%s”");
        fr(IN_PART_N_QUOTE, "Dans la partie %d : « %s »");
        es(IN_PART_N_QUOTE, "En la parte %d: \"%s\"");
        de(IN_PART_N_QUOTE, "In Abschnitt %d: \"%s\"");
        ko(IN_PART_N_QUOTE, "파트 %d: \"%s\"");

        ja(THE_QUOTE_IS_NOT_IN_THE_PART, "この引用はそのパートにありません。受け入れる前に確認してください");
        zh(THE_QUOTE_IS_NOT_IN_THE_PART, "该引文不在此部分中，接受前请先核对");
        fr(THE_QUOTE_IS_NOT_IN_THE_PART, "Cette citation n'est pas dans cette partie, vérifiez-la avant d'accepter");
        es(THE_QUOTE_IS_NOT_IN_THE_PART, "Esta cita no está en esa parte, compruébala antes de aceptar");
        de(THE_QUOTE_IS_NOT_IN_THE_PART, "Dieses Zitat steht nicht in diesem Abschnitt, prüfe es, bevor du es übernimmst");
        ko(THE_QUOTE_IS_NOT_IN_THE_PART, "이 인용문은 해당 파트에 없습니다. 수락하기 전에 확인하세요");

        ja(LEFT_OUT_N, "除外（%d）");
        zh(LEFT_OUT_N, "已排除（%d）");
        fr(LEFT_OUT_N, "Omis (%d)");
        es(LEFT_OUT_N, "Omitidos (%d)");
        de(LEFT_OUT_N, "Weggelassen (%d)");
        ko(LEFT_OUT_N, "제외됨 (%d)");

        ja(UNCHECKED_ADDITIONS_ARE_KEPT, "チェックを外した追加は古い知識として残ります。チェックを外した撤回は何も変更しません。");
        zh(UNCHECKED_ADDITIONS_ARE_KEPT, "取消勾选的添加会保留为旧认知；取消勾选的撤回不会改变任何内容。");
        fr(UNCHECKED_ADDITIONS_ARE_KEPT, "Un ajout décoché est gardé comme ancienne connaissance ; un retrait décoché ne change rien.");
        es(UNCHECKED_ADDITIONS_ARE_KEPT, "Una adición que desmarques se conserva como conocimiento antiguo; una retirada sin marcar no cambia nada.");
        de(UNCHECKED_ADDITIONS_ARE_KEPT, "Eine Ergänzung, die du abwählst, wird als altes Wissen behalten; ein abgewähltes Zurückziehen ändert nichts.");
        ko(UNCHECKED_ADDITIONS_ARE_KEPT, "체크 해제한 추가는 오래된 지식으로 유지되며, 체크 해제한 철회는 아무것도 바꾸지 않습니다.");

        ja(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "確認中に物語が変更されました。もう一度処理してください");
        zh(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "您审阅期间故事发生了变化，请重新处理");
        fr(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "L'histoire a changé pendant votre examen des modifications ; traitez de nouveau");
        es(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "La historia cambió mientras revisabas los cambios; procesa de nuevo");
        de(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "Die Geschichte hat sich geändert, während du die Änderungen geprüft hast; verarbeite erneut");
        ko(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "변경 사항을 검토하는 동안 이야기가 변경되었습니다. 다시 처리하세요");

        ja(CHANGES_TO_REVIEW, "確認待ちの変更 %d 件");
        zh(CHANGES_TO_REVIEW, "%d 项更改待审阅");
        fr(CHANGES_TO_REVIEW, "%d modifications à examiner");
        es(CHANGES_TO_REVIEW, "%d cambios que revisar");
        de(CHANGES_TO_REVIEW, "%d Änderungen zu prüfen");
        ko(CHANGES_TO_REVIEW, "검토할 변경 %d건");

        ja(READ_WHAT_THE_CHARACTERS_LEARN, "モデルでキャラクターが知ったことを読み取る");
        zh(READ_WHAT_THE_CHARACTERS_LEARN, "用模型读取角色得知的内容");
        fr(READ_WHAT_THE_CHARACTERS_LEARN, "Lire avec le modèle ce qu'apprennent les personnages");
        es(READ_WHAT_THE_CHARACTERS_LEARN, "Leer lo que aprenden los personajes con el modelo");
        de(READ_WHAT_THE_CHARACTERS_LEARN, "Mit dem Modell lesen, was die Figuren erfahren");
        ko(READ_WHAT_THE_CHARACTERS_LEARN, "모델로 캐릭터가 알게 되는 것 읽기");

        ja(ASK_THE_MODEL_WHAT_EACH_TRACKED, "追跡中の各キャラクターがパートで知ったことをモデルに尋ね、変更として提案します。オフの場合、処理は誰が誰に会ったかを記録するだけです。");
        zh(ASK_THE_MODEL_WHAT_EACH_TRACKED, "询问模型每个被追踪的角色在这些部分中得知了什么，并作为更改提出。关闭时，处理只记录谁见过谁。");
        fr(ASK_THE_MODEL_WHAT_EACH_TRACKED, "Demander au modèle ce que chaque personnage suivi a appris dans les parties et le proposer comme modifications. Désactivé : le traitement note seulement qui a rencontré qui.");
        es(ASK_THE_MODEL_WHAT_EACH_TRACKED, "Pregunta al modelo qué aprendió cada personaje seguido en las partes y lo propone como cambios. Desactivado: el procesamiento solo anota quién ha conocido a quién.");
        de(ASK_THE_MODEL_WHAT_EACH_TRACKED, "Das Modell fragen, was jede verfolgte Figur in den Abschnitten erfahren hat, und es als Änderungen vorschlagen. Aus: Die Verarbeitung hält nur fest, wer wem begegnet ist.");
        ko(ASK_THE_MODEL_WHAT_EACH_TRACKED, "추적 캐릭터가 파트에서 각각 무엇을 알게 되었는지 모델에게 묻고 변경으로 제안합니다. 끄면: 처리는 누가 누구를 만났는지만 기록합니다.");

        ja(ASK_BEFORE_APPLYING, "適用前に確認する");
        zh(ASK_BEFORE_APPLYING, "应用前先确认");
        fr(ASK_BEFORE_APPLYING, "Demander avant d'appliquer");
        es(ASK_BEFORE_APPLYING, "Preguntar antes de aplicar");
        de(ASK_BEFORE_APPLYING, "Vor dem Anwenden fragen");
        ko(ASK_BEFORE_APPLYING, "적용 전에 묻기");

        ja(SHOW_WHAT_THE_MODEL_FOUND, "何かを適用する前に、モデルが見つけた内容を確認のために表示します。オフの場合は直ちに適用され、ログに記録されます。");
        zh(SHOW_WHAT_THE_MODEL_FOUND, "在应用任何内容之前，先显示模型找到的内容供审阅。关闭时会立即应用并记入日志。");
        fr(SHOW_WHAT_THE_MODEL_FOUND, "Montrer ce que le modèle a trouvé pour examen avant toute application. Désactivé : c'est appliqué aussitôt et consigné.");
        es(SHOW_WHAT_THE_MODEL_FOUND, "Muestra lo que encontró el modelo para revisarlo antes de aplicar nada. Desactivado: se aplica de inmediato y se registra.");
        de(SHOW_WHAT_THE_MODEL_FOUND, "Zeigen, was das Modell gefunden hat, zur Prüfung, bevor etwas angewendet wird. Aus: Es wird sofort angewendet und protokolliert.");
        ko(SHOW_WHAT_THE_MODEL_FOUND, "아무것도 적용하기 전에 모델이 찾은 것을 검토용으로 표시합니다. 끄면: 즉시 적용하고 기록합니다.");

        ja(PARTS_PER_RUN, "1回あたりのパート数");
        zh(PARTS_PER_RUN, "每轮处理的部分数");
        fr(PARTS_PER_RUN, "Parties par passe");
        es(PARTS_PER_RUN, "Partes por ejecución");
        de(PARTS_PER_RUN, "Abschnitte je Lauf");
        ko(PARTS_PER_RUN, "실행당 파트 수");

        ja(LOREBOOK_FOR_THE_MODEL, "モデルに渡すロアブック");
        zh(LOREBOOK_FOR_THE_MODEL, "提供给模型的设定集");
        fr(LOREBOOK_FOR_THE_MODEL, "Lorebook pour le modèle");
        es(LOREBOOK_FOR_THE_MODEL, "Lorebook para el modelo");
        de(LOREBOOK_FOR_THE_MODEL, "Lorebook für das Modell");
        ko(LOREBOOK_FOR_THE_MODEL, "모델용 로어북");

        ja(ENTRIES_THE_PARTS_ACTIVATE, "パートが有効にするエントリ");
        zh(ENTRIES_THE_PARTS_ACTIVATE, "这些部分触发的条目");
        fr(ENTRIES_THE_PARTS_ACTIVATE, "Entrées activées par les parties");
        es(ENTRIES_THE_PARTS_ACTIVATE, "Entradas que activan las partes");
        de(ENTRIES_THE_PARTS_ACTIVATE, "Einträge, die die Abschnitte aktivieren");
        ko(ENTRIES_THE_PARTS_ACTIVATE, "파트가 활성화하는 항목");

        ja(THE_WHOLE_LOREBOOK, "ロアブック全体");
        zh(THE_WHOLE_LOREBOOK, "整个设定集");
        fr(THE_WHOLE_LOREBOOK, "Tout le lorebook");
        es(THE_WHOLE_LOREBOOK, "Todo el lorebook");
        de(THE_WHOLE_LOREBOOK, "Das gesamte Lorebook");
        ko(THE_WHOLE_LOREBOOK, "로어북 전체");

        ja(NO_LOREBOOK, "ロアブックなし");
        zh(NO_LOREBOOK, "不使用设定集");
        fr(NO_LOREBOOK, "Pas de lorebook");
        es(NO_LOREBOOK, "Sin lorebook");
        de(NO_LOREBOOK, "Kein Lorebook");
        ko(NO_LOREBOOK, "로어북 없음");

        ja(MODEL_FOR_THE_EXTRACTION, "読み取りに使うモデル");
        zh(MODEL_FOR_THE_EXTRACTION, "用于读取的模型");
        fr(MODEL_FOR_THE_EXTRACTION, "Modèle pour l'extraction");
        es(MODEL_FOR_THE_EXTRACTION, "Modelo para la extracción");
        de(MODEL_FOR_THE_EXTRACTION, "Modell für die Extraktion");
        ko(MODEL_FOR_THE_EXTRACTION, "추출용 모델");

        ja(PROTOCOL_FOR_THE_EXTRACTION, "読み取りに使うプロトコル");
        zh(PROTOCOL_FOR_THE_EXTRACTION, "用于读取的协议");
        fr(PROTOCOL_FOR_THE_EXTRACTION, "Protocole pour l'extraction");
        es(PROTOCOL_FOR_THE_EXTRACTION, "Protocolo para la extracción");
        de(PROTOCOL_FOR_THE_EXTRACTION, "Protokoll für die Extraktion");
        ko(PROTOCOL_FOR_THE_EXTRACTION, "추출용 프로토콜");

        ja(THE_MODEL_OF_THE_BOOK, "本のモデル");
        zh(THE_MODEL_OF_THE_BOOK, "本书的模型");
        fr(THE_MODEL_OF_THE_BOOK, "Le modèle du livre");
        es(THE_MODEL_OF_THE_BOOK, "El modelo del libro");
        de(THE_MODEL_OF_THE_BOOK, "Das Modell des Buchs");
        ko(THE_MODEL_OF_THE_BOOK, "책의 모델");

        ja(SHOW_PART_IN_THE_STORY, "パート %d をストーリーで表示");
        zh(SHOW_PART_IN_THE_STORY, "在故事中显示第 %d 部分");
        fr(SHOW_PART_IN_THE_STORY, "Afficher la partie %d dans l'histoire");
        es(SHOW_PART_IN_THE_STORY, "Mostrar la parte %d en la historia");
        de(SHOW_PART_IN_THE_STORY, "Abschnitt %d in der Geschichte anzeigen");
        ko(SHOW_PART_IN_THE_STORY, "이야기에서 파트 %d 보기");

        ja(KNOWLEDGE_TRACKER_READ_THE_STORY, "ナレッジトラッカー: 物語を読み取り");
        zh(KNOWLEDGE_TRACKER_READ_THE_STORY, "知识追踪器：读取故事");
        fr(KNOWLEDGE_TRACKER_READ_THE_STORY, "Suivi des connaissances : lire l'histoire");
        es(KNOWLEDGE_TRACKER_READ_THE_STORY, "Seguimiento del conocimiento: leer la historia");
        de(KNOWLEDGE_TRACKER_READ_THE_STORY, "Knowledge Tracker: die Geschichte lesen");
        ko(KNOWLEDGE_TRACKER_READ_THE_STORY, "지식 추적기: 이야기 읽기");

        ja(KNOWLEDGE_TRACKER, "ナレッジトラッカー");
        zh(KNOWLEDGE_TRACKER, "知识追踪器");
        fr(KNOWLEDGE_TRACKER, "Suivi des connaissances");
        es(KNOWLEDGE_TRACKER, "Seguimiento del conocimiento");
        de(KNOWLEDGE_TRACKER, "Knowledge Tracker");
        ko(KNOWLEDGE_TRACKER, "지식 추적기");

        ja(KNOWLEDGE_EXTRACTION_PROMPT, "知識の読み取りプロンプト");
        zh(KNOWLEDGE_EXTRACTION_PROMPT, "知识读取提示词");
        fr(KNOWLEDGE_EXTRACTION_PROMPT, "Prompt d'extraction des connaissances");
        es(KNOWLEDGE_EXTRACTION_PROMPT, "Prompt de extracción del conocimiento");
        de(KNOWLEDGE_EXTRACTION_PROMPT, "Prompt zur Wissensextraktion");
        ko(KNOWLEDGE_EXTRACTION_PROMPT, "지식 추출 프롬프트");

        ja(KNOWLEDGE_TIDY_PROMPT, "知識の整理プロンプト");
        zh(KNOWLEDGE_TIDY_PROMPT, "知识整理提示词");
        fr(KNOWLEDGE_TIDY_PROMPT, "Prompt de rangement des connaissances");
        es(KNOWLEDGE_TIDY_PROMPT, "Prompt de ordenación del conocimiento");
        de(KNOWLEDGE_TIDY_PROMPT, "Prompt zum Aufräumen des Wissens");
        ko(KNOWLEDGE_TIDY_PROMPT, "지식 정리 프롬프트");

        ja(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "登場人物が学ぶことを物語から読み取る方法をモデルに伝えるシステムメッセージ。プラグインが読み取るため、JSON の回答形式は維持してください。空 = 継承されたもの、なければ組み込みのテキスト。");
        zh(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "告诉模型如何从故事中读取被追踪角色所得知内容的系统消息。请保留 JSON 回答格式，插件会读取它。留空 = 继承的设置，否则使用内置文本。");
        fr(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "Le message système qui indique au modèle comment lire l'histoire pour ce qu'apprennent les personnages suivis. Gardez le format de réponse JSON, le plugin le lit. Vide = celui hérité, sinon le texte intégré.");
        es(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "El mensaje del sistema que le dice al modelo cómo leer la historia para ver lo que aprenden los personajes seguidos. Conserva el formato de respuesta JSON, el plugin lo lee. Vacío = el heredado, si no el texto integrado.");
        de(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "Die Systemnachricht, die dem Modell sagt, wie es die Geschichte danach lesen soll, was die verfolgten Figuren erfahren. Behalte das JSON-Antwortformat bei, das Plugin liest es. Leer = der geerbte, sonst der eingebaute Text.");
        ko(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "추적 캐릭터가 알게 되는 것을 위해 모델이 이야기를 읽는 방법을 알려 주는 시스템 메시지. 플러그인이 읽으므로 JSON 응답 형식은 유지하세요. 비워 두면 상속된 것, 없으면 내장 텍스트.");

        ja(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "残す価値のない項目を削除・統合する整理パスのシステムメッセージ。プラグインが読み取るため、JSON の回答形式は維持してください。空 = 継承されたもの、なければ組み込みのテキスト。");
        zh(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "整理阶段的系统消息，用于删除和合并不值得保留的条目。请保留 JSON 回答格式，插件会读取它。留空 = 继承的设置，否则使用内置文本。");
        fr(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "Le message système du rangement, qui abandonne et fusionne les éléments qui ne valent pas d'être gardés. Gardez le format de réponse JSON, le plugin le lit. Vide = celui hérité, sinon le texte intégré.");
        es(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "El mensaje del sistema de la pasada de ordenación, que descarta y combina los elementos que no merece la pena conservar. Conserva el formato de respuesta JSON, el plugin lo lee. Vacío = el heredado, si no el texto integrado.");
        de(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "Die Systemnachricht des Aufräumdurchgangs, der Elemente verwirft und zusammenführt, die nicht aufbewahrenswert sind. Behalte das JSON-Antwortformat bei, das Plugin liest es. Leer = der geerbte, sonst der eingebaute Text.");
        ko(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS, "유지할 가치가 없는 항목을 버리고 병합하는 정리 단계의 시스템 메시지. 플러그인이 읽으므로 JSON 응답 형식은 유지하세요. 비워 두면 상속된 것, 없으면 내장 텍스트.");

        ja(MASS_CHANGES, "一括変更");
        zh(MASS_CHANGES, "批量修改");
        fr(MASS_CHANGES, "Modifications en masse");
        es(MASS_CHANGES, "Cambios masivos");
        de(MASS_CHANGES, "Massenänderungen");
        ko(MASS_CHANGES, "일괄 변경");

        ja(CHANGE_MANY_ITEMS_AT_ONCE, "追跡中の登場人物の多数の項目を一度に削除・撤回・復元し、JSON でエクスポート・インポートします");
        zh(CHANGE_MANY_ITEMS_AT_ONCE, "一次删除、撤回或恢复被追踪角色的多个条目；以 JSON 导出和导入");
        fr(CHANGE_MANY_ITEMS_AT_ONCE, "Supprimer, retirer ou restaurer de nombreux éléments des personnages suivis d'un coup ; les exporter et les importer en JSON");
        es(CHANGE_MANY_ITEMS_AT_ONCE, "Eliminar, retirar o restaurar muchos elementos de los personajes seguidos a la vez; exportarlos e importarlos como JSON");
        de(CHANGE_MANY_ITEMS_AT_ONCE, "Viele Elemente der verfolgten Figuren auf einmal löschen, zurückziehen oder wiederherstellen; sie als JSON exportieren und importieren");
        ko(CHANGE_MANY_ITEMS_AT_ONCE, "추적 캐릭터의 여러 항목을 한꺼번에 삭제, 철회 또는 복원하고 JSON으로 내보내거나 가져옵니다");

        ja(STATE, "状態");
        zh(STATE, "状态");
        fr(STATE, "État");
        es(STATE, "Estado");
        de(STATE, "Zustand");
        ko(STATE, "상태");

        ja(ACTIVE, "有効");
        zh(ACTIVE, "有效");
        fr(ACTIVE, "Actif");
        es(ACTIVE, "Activo");
        de(ACTIVE, "Aktiv");
        ko(ACTIVE, "활성");

        ja(OLD_RETRACTED, "古い: 撤回");
        zh(OLD_RETRACTED, "旧：已撤回");
        fr(OLD_RETRACTED, "Ancien : retiré");
        es(OLD_RETRACTED, "Antiguo: retirado");
        de(OLD_RETRACTED, "Alt: zurückgezogen");
        ko(OLD_RETRACTED, "오래됨: 철회됨");

        ja(OLD_DECLINED, "古い: レビューで却下");
        zh(OLD_DECLINED, "旧：审阅时被拒绝");
        fr(OLD_DECLINED, "Ancien : refusé lors d'un examen");
        es(OLD_DECLINED, "Antiguo: rechazado en una revisión");
        de(OLD_DECLINED, "Alt: in einer Prüfung abgelehnt");
        ko(OLD_DECLINED, "오래됨: 검토에서 거부됨");

        ja(OLD_TIDIED, "古い: 整理済み");
        zh(OLD_TIDIED, "旧：已整理");
        fr(OLD_TIDIED, "Ancien : rangé");
        es(OLD_TIDIED, "Antiguo: ordenado");
        de(OLD_TIDIED, "Alt: aufgeräumt");
        ko(OLD_TIDIED, "오래됨: 정리됨");

        ja(ORIGIN, "出所");
        zh(ORIGIN, "来源");
        fr(ORIGIN, "Origine");
        es(ORIGIN, "Origen");
        de(ORIGIN, "Herkunft");
        ko(ORIGIN, "출처");

        ja(INITIAL_STATE, "初期状態");
        zh(INITIAL_STATE, "初始状态");
        fr(INITIAL_STATE, "État initial");
        es(INITIAL_STATE, "Estado inicial");
        de(INITIAL_STATE, "Anfangszustand");
        ko(INITIAL_STATE, "초기 상태");

        ja(WRITTEN_BY_HAND, "手書き");
        zh(WRITTEN_BY_HAND, "手动编写");
        fr(WRITTEN_BY_HAND, "Écrit à la main");
        es(WRITTEN_BY_HAND, "Escrito a mano");
        de(WRITTEN_BY_HAND, "Von Hand geschrieben");
        ko(WRITTEN_BY_HAND, "직접 작성");

        ja(LEARNED_FROM_THE_STORY, "物語から学んだ");
        zh(LEARNED_FROM_THE_STORY, "从故事中得知");
        fr(LEARNED_FROM_THE_STORY, "Appris de l'histoire");
        es(LEARNED_FROM_THE_STORY, "Aprendido de la historia");
        de(LEARNED_FROM_THE_STORY, "Aus der Geschichte erfahren");
        ko(LEARNED_FROM_THE_STORY, "이야기에서 알게 됨");

        ja(PART, "パート");
        zh(PART, "部分");
        fr(PART, "Partie");
        es(PART, "Parte");
        de(PART, "Abschnitt");
        ko(PART, "파트");

        ja(NO_ITEMS, "項目なし");
        zh(NO_ITEMS, "没有条目");
        fr(NO_ITEMS, "Aucun élément");
        es(NO_ITEMS, "Sin elementos");
        de(NO_ITEMS, "Keine Elemente");
        ko(NO_ITEMS, "항목 없음");

        ja(N_OF_N_SELECTED, "%d 件を選択（全 %d 件）");
        zh(N_OF_N_SELECTED, "已选 %d / %d");
        fr(N_OF_N_SELECTED, "%d sur %d sélectionnés");
        es(N_OF_N_SELECTED, "%d de %d seleccionados");
        de(N_OF_N_SELECTED, "%d von %d ausgewählt");
        ko(N_OF_N_SELECTED, "%d / %d개 선택됨");

        ja(RETRACT_SELECTED, "選択した項目を撤回（%d）");
        zh(RETRACT_SELECTED, "撤回所选（%d）");
        fr(RETRACT_SELECTED, "Retirer la sélection (%d)");
        es(RETRACT_SELECTED, "Retirar la selección (%d)");
        de(RETRACT_SELECTED, "Ausgewählte zurückziehen (%d)");
        ko(RETRACT_SELECTED, "선택 항목 철회 (%d)");

        ja(RESTORE_SELECTED, "選択した項目を復元（%d）");
        zh(RESTORE_SELECTED, "恢复所选（%d）");
        fr(RESTORE_SELECTED, "Restaurer la sélection (%d)");
        es(RESTORE_SELECTED, "Restaurar la selección (%d)");
        de(RESTORE_SELECTED, "Ausgewählte wiederherstellen (%d)");
        ko(RESTORE_SELECTED, "선택 항목 복원 (%d)");

        ja(N_ITEMS_RETRACTED, "%d 件の項目を撤回しました");
        zh(N_ITEMS_RETRACTED, "已撤回 %d 个条目");
        fr(N_ITEMS_RETRACTED, "%d élément(s) retiré(s)");
        es(N_ITEMS_RETRACTED, "%d elemento(s) retirado(s)");
        de(N_ITEMS_RETRACTED, "%d Element(e) zurückgezogen");
        ko(N_ITEMS_RETRACTED, "항목 %d개를 철회했습니다");

        ja(N_ITEMS_RESTORED, "%d 件の項目を復元しました");
        zh(N_ITEMS_RESTORED, "已恢复 %d 个条目");
        fr(N_ITEMS_RESTORED, "%d élément(s) restauré(s)");
        es(N_ITEMS_RESTORED, "%d elemento(s) restaurado(s)");
        de(N_ITEMS_RESTORED, "%d Element(e) wiederhergestellt");
        ko(N_ITEMS_RESTORED, "항목 %d개를 복원했습니다");

        ja(N_ITEMS_DELETED, "%d 件の項目を削除しました");
        zh(N_ITEMS_DELETED, "已删除 %d 个条目");
        fr(N_ITEMS_DELETED, "%d élément(s) supprimé(s)");
        es(N_ITEMS_DELETED, "%d elemento(s) eliminado(s)");
        de(N_ITEMS_DELETED, "%d Element(e) gelöscht");
        ko(N_ITEMS_DELETED, "항목 %d개를 삭제했습니다");

        ja(EXPORT_TO_JSON, "JSON にエクスポート");
        zh(EXPORT_TO_JSON, "导出为 JSON");
        fr(EXPORT_TO_JSON, "Exporter en JSON");
        es(EXPORT_TO_JSON, "Exportar a JSON");
        de(EXPORT_TO_JSON, "Als JSON exportieren");
        ko(EXPORT_TO_JSON, "JSON으로 내보내기");

        ja(IMPORT_FROM_JSON, "JSON からインポート");
        zh(IMPORT_FROM_JSON, "从 JSON 导入");
        fr(IMPORT_FROM_JSON, "Importer depuis un JSON");
        es(IMPORT_FROM_JSON, "Importar desde JSON");
        de(IMPORT_FROM_JSON, "Aus JSON importieren");
        ko(IMPORT_FROM_JSON, "JSON에서 가져오기");

        ja(IMPORTED_SUMMARY, "インポート完了: 新しい登場人物 %d、更新 %d、追加した項目 %d、既存 %d");
        zh(IMPORTED_SUMMARY, "已导入：新角色 %d 个，更新 %d 个，新增条目 %d 个，已存在 %d 个");
        fr(IMPORTED_SUMMARY, "Importé : %d nouveau(x) personnage(s), %d mis à jour, %d élément(s) ajouté(s), %d déjà présents");
        es(IMPORTED_SUMMARY, "Importado: %d personaje(s) nuevo(s), %d actualizado(s), %d elemento(s) añadido(s), %d ya estaban");
        de(IMPORTED_SUMMARY, "Importiert: %d neue Figur(en), %d aktualisiert, %d Element(e) hinzugefügt, %d bereits vorhanden");
        ko(IMPORTED_SUMMARY, "가져옴: 새 캐릭터 %d명, 업데이트 %d명, 추가된 항목 %d개, 이미 있던 항목 %d개");

        ja(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "ファイルはナレッジトラッカーのエクスポートではありません: %s");
        zh(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "该文件不是知识追踪器的导出文件：%s");
        fr(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "Le fichier n'est pas un export du suivi des connaissances : %s");
        es(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "El archivo no es una exportación del Seguimiento del conocimiento: %s");
        de(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "Die Datei ist kein Knowledge-Tracker-Export: %s");
        ko(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT, "지식 추적기 내보내기 파일이 아닙니다: %s");

    }
}
