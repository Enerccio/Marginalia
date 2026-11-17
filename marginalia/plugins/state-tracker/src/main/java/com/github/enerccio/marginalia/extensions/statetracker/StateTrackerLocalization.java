package com.github.enerccio.marginalia.extensions.statetracker;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.statetracker.extract.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the State Tracker in the languages besides English; the English text is the key. The texts that the Knowledge
 * Tracker has too are translated the same way.
 */
public class StateTrackerLocalization extends PluginLocalizationBundle {

    public static final StateTrackerLocalization T = new StateTrackerLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final StateTrackerDefaults englishDefaults = new StateTrackerDefaults();
    private final Map<Language, StateTrackerDefaults> translatedDefaults = Map.of(JA, new StateTrackerDefaultsJA(), ZH, new StateTrackerDefaultsZH(), FR, new StateTrackerDefaultsFR(), ES, new StateTrackerDefaultsES(), DE, new StateTrackerDefaultsDE(),
            IT, new StateTrackerDefaultsIT(), KO, new StateTrackerDefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public StateTrackerDefaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

    public static final String STATE = "State";
    public static final String STATE_TRACKER = "State Tracker";
    public static final String TRACK_A_CHARACTER = "Track a character";
    public static final String NEW_TRACKED_CHARACTER = "New tracked character";
    public static final String EDIT_TRACKED_CHARACTER = "Edit tracked character";
    public static final String NAME = "Name";
    public static final String ALIASES = "Aliases";
    public static final String OTHER_NAMES_THE_CHARACTER_IS_WRITTEN = "Other names the character is written as in the present characters field, separated by commas";
    public static final String TRACKED_CATEGORIES = "Tracked categories";
    public static final String WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER = "What the plugin keeps for this character. Your own categories are copied into the book when you pick them.";
    public static final String FROM_MY_CATEGORIES = "%s (from my categories)";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String EDIT = "Edit";
    public static final String DELETE = "Delete";
    public static final String CLOSE = "Close";
    public static final String GIVE_THE_CHARACTER_A_NAME = "Give the character a name";
    public static final String THE_NAME_OR_AN_ALIAS_IS_USED_BY = "The name or an alias is already used by %s";
    public static final String STOP_TRACKING_AND_DELETE_ITS_STATE = "Stop tracking %s and delete its whole state?";
    public static final String NO_CHARACTER_IS_TRACKED_YET = "No character is tracked yet. Only the characters you add here are described to the model, and only while they are among the present characters of a part.";
    public static final String NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER = "No category is tracked for this character. Edit it to pick some.";
    public static final String NOTHING_YET = "Nothing yet";
    public static final String SET_THE_VALUE = "Set the value";
    public static final String ADD_AN_ENTRY = "Add an entry";
    public static final String NEW_VALUE = "New value";
    public static final String EDIT_VALUE = "Edit value";
    public static final String CATEGORY = "Category";
    public static final String VALUE = "Value";
    public static final String WRITE_SOMETHING = "Write something";
    public static final String THE_CURRENT_VALUE_GOES_TO_THE_HISTORY = "The current value is kept in the history.";
    public static final String LOCKED = "Locked";
    public static final String THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE = "The tidy pass leaves a locked value alone (a scar, the family sword)";
    public static final String LOCK = "Lock";
    public static final String UNLOCK = "Unlock";
    public static final String REMOVE = "Remove";
    public static final String NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY = "Not true any more: keep it in the history and do not send it to the model";
    public static final String RESTORE = "Restore";
    public static final String MAKE_IT_CURRENT_AGAIN = "Make it current again";
    public static final String HISTORY_N = "History (%d)";
    public static final String DELETE_THIS_VALUE_FOR_GOOD = "Delete this value for good? Remove keeps it in the history.";
    public static final String REPLACED = "replaced";
    public static final String REMOVED = "removed";
    public static final String CATEGORIES_OF_THIS_BOOK = "Categories of this book";
    public static final String NEW_CATEGORY_FOR_THIS_BOOK = "New category for this book";
    public static final String ADD_FROM_MY_CATEGORIES = "Add from my categories";
    public static final String ONLY_IN_THIS_BOOK = "Only in this book";
    public static final String DIFFERS_FROM_YOUR_CATEGORY = "Differs from your category";
    public static final String UPDATE_FROM_MY_CATEGORIES = "Update from my categories";
    public static final String SAVE_TO_MY_CATEGORIES = "Save to my categories";
    public static final String SAVED_TO_YOUR_CATEGORIES = "Saved to your categories";
    public static final String DELETE_THE_CATEGORY_FROM_THIS_BOOK = "Delete the category %s from this book? %d character(s) track it; all their values of it are deleted.";
    public static final String THE_BOOK_USES_NO_CATEGORY_YET = "The book uses no category yet. Add one here, or pick categories when you track a character.";
    public static final String NEW_CATEGORY = "New category";
    public static final String EDIT_CATEGORY = "Edit category";
    public static final String KIND = "Kind";
    public static final String SINGLE_VALUE = "Single value";
    public static final String LIST = "List";
    public static final String DESCRIPTION = "Description";
    public static final String KIND_HELP = "Single value: a new value replaces the old one. List: entries are added and removed. Description: one text, rewritten as a whole.";
    public static final String THE_KIND_CANT_CHANGE = "The kind can't change while characters have values of this category";
    public static final String INSTRUCTION_FOR_THE_MODEL = "Instruction for the model";
    public static final String WHAT_BELONGS_IN_THE_CATEGORY = "What belongs in the category and what does not; the model reads it when it follows the story";
    public static final String ONLY_WHAT_THE_TEXT_SAYS = "Only what the text says";
    public static final String ON_THE_VALUE_CHANGES_ONLY_WHEN = "On: the value changes only when the text says so. Off: also on obvious consequences (wet after a swim).";
    public static final String SAY_NONE_WHEN_EMPTY = "Say \"none\" when empty";
    public static final String THE_PROMPT_SAYS_NONE = "The prompt says none when the character has no value (a lost weapon) instead of leaving the category out";
    public static final String GIVE_THE_CATEGORY_A_NAME = "Give the category a name";
    public static final String A_CATEGORY_IS_CALLED_SO_ALREADY = "A category is called %s already";
    public static final String MY_CATEGORIES = "My categories";
    public static final String THE_CATEGORIES_YOUR_BOOKS_COPY_FROM = "The categories your books copy from. A book keeps its own copy: a change here reaches a book when you update it there.";
    public static final String RESET_TO_THE_BUILT_IN_CATEGORIES = "Reset to the built-in categories";
    public static final String DELETE_YOUR_CATEGORY = "Delete your category %s? Books that use it keep their copy.";
    public static final String SETTINGS = "Settings";
    public static final String REFRESH = "Refresh";
    public static final String IN_PROMPT = "In prompt";
    public static final String INSERT_THE_STATE_INTO_THE_PROMPT = "Insert the state of the tracked characters of the scene into the prompt of every generation";
    public static final String DEPTH = "Depth";
    public static final String MESSAGES_AFTER_THE_STATE = "Messages after the state: 0 = after the instructions, 1 = right before them";
    public static final String ROLE = "Role";
    public static final String SYSTEM = "System";
    public static final String ASSISTANT = "Assistant";
    public static final String USER = "User";
    public static final String NAME_SEPARATORS = "Name separators";
    public static final String HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT = "How the present characters field of this book is split, one per line (\\n for a new line); empty = the built-in ones";
    public static final String CATEGORIES_N = "Categories (%d)";
    public static final String WEARING = "Wearing";
    public static final String WEARING_INSTRUCTION = "What the character wears right now, with its state (wet, torn, bloodied). Not what is carried in bags.";
    public static final String WEAPON_AT_HAND = "Weapon at hand";
    public static final String WEAPON_INSTRUCTION = "The weapon the character has in hand or ready (on the belt, on the back). Nothing else.";
    public static final String CARRYING = "Carrying";
    public static final String CARRYING_INSTRUCTION = "Things the character carries on the body or in bags, one entry each. Not clothes, not the weapon at hand, not small change.";
    public static final String INJURIES = "Injuries";
    public static final String INJURIES_INSTRUCTION = "Wounds, illnesses and lasting physical effects, one entry each, until they heal.";
    public static final String MOOD = "Mood";
    public static final String MOOD_INSTRUCTION = "How the character feels right now, in a few words. It fades: change it when the scene shows another mood.";
    public static final String PROCESSING = "Processing";
    public static final String PROCESSED_UP_TO_PART = "Processed up to part %d of %d";
    public static final String NOTHING_IS_PROCESSED_YET = "Nothing is processed yet (%d parts)";
    public static final String THE_BOOK_HAS_NO_PARTS_YET = "The book has no parts yet";
    public static final String PROCESS_NOW = "Process now";
    public static final String PROCESS_NEXT_PART = "Process next part";
    public static final String READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED = "Read only the first part that is not processed yet, for one step at a time";
    public static final String START_POINT = "Start point";
    public static final String START_OF_THE_BOOK = "Start of the book";
    public static final String PARTS_BEFORE_THE_START_POINT = "Parts before it are never processed";
    public static final String REPROCESS_FROM_HERE = "Reprocess from here";
    public static final String PART_N = "Part %d";
    public static final String PROCESSING_THE_STORY = "Processing the story";
    public static final String CHANGES_TO_REVIEW = "%d changes to review";
    public static final String THE_STORY_CHANGED_WHILE_YOU_REVIEWED = "The story changed while you reviewed the changes; process again";
    public static final String LAST_RUN_WHAT_THE_MODEL_THOUGHT = "Last run: what the model thought";
    public static final String PROCESS_NEW_PARTS_AUTOMATICALLY = "Process new parts automatically";
    public static final String ASK_BEFORE_APPLYING = "Ask before applying";
    public static final String PARTS_PER_RUN = "Parts per run";
    public static final String LOREBOOK_FOR_THE_MODEL = "Lorebook for the model";
    public static final String ENTRIES_THE_PARTS_ACTIVATE = "Entries the parts activate";
    public static final String THE_WHOLE_LOREBOOK = "The whole lorebook";
    public static final String NO_LOREBOOK = "No lorebook";
    public static final String MODEL_FOR_THE_EXTRACTION = "Model for the extraction";
    public static final String PROTOCOL_FOR_THE_EXTRACTION = "Protocol for the extraction";
    public static final String THE_MODEL_OF_THE_BOOK = "The model of the book";
    public static final String REVIEW_THE_CHANGES_OF_RUN = "Review the changes (run %d)";
    public static final String ADD = "Add";
    public static final String READ_THE_STORY_FOR_THE_STATE = "State Tracker: read the story";
    public static final String DONE_PARTS_CHANGES_APPLIED = "Done: %d parts read, %d changes applied";
    public static final String STOPPED_PARTS_CHANGES_APPLIED = "Stopped: %d parts read, %d changes applied";
    public static final String AN_UNCHECKED_CHANGE_IS_NOT_APPLIED = "A change you uncheck is not applied.";
    public static final String SET = "Set";
    public static final String CLEAR = "Clear";
    public static final String ASK_THE_MODEL_WHAT_CHANGED = "Ask the model what changed in the state of the tracked characters in the parts that are not processed yet";
    public static final String WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT = "When a part is final, the model reads it for changes of the state (at the latest before the next generation)";
    public static final String SHOW_THE_CHANGES_FOR_REVIEW_FIRST = "Show what the model found for review before anything is applied. Off: it is applied at once; the history of a value shows the part it came from.";
    public static final String READ_N_PARTS_WITH_THE_MODEL = "Read %d parts with the model in %d requests? The story text alone is about %d tokens; the instructions, the lorebook and the state of the characters are sent with every request on top of it.";
    public static final String REPROCESS_FROM_PART_N = "Reprocess from part %d? What was read from it and from the later parts is taken back and read again.";
    public static final String THE_STORY_IS_BEING_PROCESSED_ALREADY = "The story is being processed already";
    public static final String READ_FROM_THE_STORY_IN_PART_N = "Read from the story in part %d; a reprocess from that part takes it back";
    public static final String PROCESSED_PARTS = "Processed parts (%d)";
    public static final String CHANGED_SINCE_PROCESSED = "Changed since processed";
    public static final String NOTHING_NEW_TO_PROCESS = "Nothing new to process";
    public static final String ENDED_BY_THE_STORY_IN_PART_N = "Ended by the story in part %d; a reprocess from that part brings it back";
    public static final String TIDY_THE_STATE = "Tidy the state";
    public static final String ASK_THE_MODEL_WHICH_VALUES_FADED = "Ask the model which current values faded, moved on or are listed twice, from the last parts; locked values stay";
    public static final String TIDY_AUTOMATICALLY = "Tidy automatically";
    public static final String AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES = "At the end of a run that applies its changes without asking, when enough parts were read since the last tidy";
    public static final String TIDY_AFTER_N_PARTS = "Tidy after this many parts";
    public static final String THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS = "The tidy also reads this many of the last processed parts";
    public static final String REVIEW_THE_TIDY = "Review the tidy";
    public static final String AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE = "An unchecked change keeps the value as it is";
    public static final String REPLACE = "Replace";
    public static final String TIDIED = "tidied";
    public static final String TIDIED_AFTER_PART_N = "Tidied after part %d; a reprocess from that part brings it back";
    public static final String TIDY_DONE = "Tidy: %d value(s) changed";
    public static final String THE_VALUE_HAS_BEEN_CHANGED_SINCE = "The value has been changed since, it was not touched";
    public static final String BY_THE_TIDY_PART_N = "by the tidy, part %d";
    public static final String WRITTEN_BY_THE_TIDY_AFTER_PART_N = "Written by the tidy after part %d, not stated by the story; a reprocess from that part takes it back";
    public static final String STORY_CLOCK = "Story clock";
    public static final String FOLLOW_THE_TIME_OF_THE_STORY = "The model also follows the time of the story (days, the time of day, dates) and the prompt gets the story time";
    public static final String DAY_ONE_IS_THE_DAY_OF = "Day 1 is the day of";
    public static final String AN_EVENT_FOR_EXAMPLE_THE_AWAKENING = "An event, for example \"the awakening\": the story time then reads \"day 14 after the awakening\"";
    public static final String DAY_AT_THE_START_POINT = "Day at the start point";
    public static final String TIME_OF_DAY = "Time of day";
    public static final String DATE_AT_THE_START_POINT = "Date at the start point";
    public static final String OPTIONAL_A_CALENDAR_DATE = "Optional: with a date the clock also counts dates";
    public static final String STORY_TIME = "Story time";
    public static final String STORY_TIME_S = "Story time: %s";
    public static final String DAY_N = "Day %d";
    public static final String DAY_N_AFTER_S = "Day %d after %s";
    public static final String ABOUT_S = "about: %s";
    public static final String SET_THE_STORY_TIME = "Set the story time";
    public static final String STORY_TIME_AFTER_PART_N = "Story time after part %d";
    public static final String A_TIME_SET_HERE_IS_KEPT = "A time set here is a fixed point: reading the part again does not replace it, and the clock counts on from it";
    public static final String DAY = "Day";
    public static final String DATE = "Date";
    public static final String REMOVE_THE_FIXED_POINT = "Remove the fixed point";
    public static final String SET_BY_HAND = "set by hand";
    public static final String FLASHBACK = "Flashback";
    public static final String TIME_PASSES = "Time passes";
    public static final String PLUS_N_DAYS = "+%d day(s)";
    public static final String THE_SAME_DAY = "the same day";
    public static final String THE_CLOCK_DOES_NOT_MOVE = "the clock does not move";
    public static final String APPROXIMATE = "approximate";
    public static final String SET_ON_DAY_N = "day %d";
    public static final String SET_ON_DAY_N_OF_THE_STORY = "Set on day %d of the story";
    public static final String DAWN = "dawn";
    public static final String MORNING = "morning";
    public static final String NOON = "noon";
    public static final String AFTERNOON = "afternoon";
    public static final String EVENING = "evening";
    public static final String NIGHT = "night";
    public static final String IMPORT_FROM_THE_CHARACTER_TRACKER = "Import from the Character Tracker";
    public static final String EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES = "Every imported character tracks the checked categories. Your own categories are copied into the book when you pick them.";
    public static final String N_CHARACTERS_NOW_TRACKED = "%d character(s) now tracked";
    public static final String STATE_EXTRACTION_PROMPT = "State extraction prompt";
    public static final String STATE_TIDY_PROMPT = "State tidy prompt";
    public static final String THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL = "The system message that tells the model how to read the story for what changed in the state of the tracked characters. Keep the JSON answer format, the plugin reads it. When the book follows the time of the story, the rules of the clock are added after it. Empty = the inherited one, else the built-in text.";
    public static final String THE_SYSTEM_MESSAGE_OF_THE_TIDY = "The system message of the tidy, which drops, updates and merges values that faded. Keep the JSON answer format, the plugin reads it. Empty = the inherited one, else the built-in text.";

    @Override
    protected void loadKeys() {
        ja(STATE, "状態");
        zh(STATE, "状态");
        fr(STATE, "État");
        es(STATE, "Estado");
        de(STATE, "Zustand");
        ko(STATE, "상태");
        it(STATE, "Stato");

        ja(STATE_TRACKER, "状態トラッカー");
        zh(STATE_TRACKER, "状态追踪器");
        fr(STATE_TRACKER, "Suivi d'état");
        es(STATE_TRACKER, "Seguimiento de estado");
        de(STATE_TRACKER, "Zustandsverfolgung");
        ko(STATE_TRACKER, "상태 추적기");
        it(STATE_TRACKER, "Tracciamento dello stato");

        ja(TRACK_A_CHARACTER, "キャラクターを追跡");
        zh(TRACK_A_CHARACTER, "追踪角色");
        fr(TRACK_A_CHARACTER, "Suivre un personnage");
        es(TRACK_A_CHARACTER, "Seguir a un personaje");
        de(TRACK_A_CHARACTER, "Eine Figur verfolgen");
        ko(TRACK_A_CHARACTER, "캐릭터 추적");
        it(TRACK_A_CHARACTER, "Traccia un personaggio");

        ja(NEW_TRACKED_CHARACTER, "新しい追跡キャラクター");
        zh(NEW_TRACKED_CHARACTER, "新的被追踪角色");
        fr(NEW_TRACKED_CHARACTER, "Nouveau personnage suivi");
        es(NEW_TRACKED_CHARACTER, "Nuevo personaje seguido");
        de(NEW_TRACKED_CHARACTER, "Neue verfolgte Figur");
        ko(NEW_TRACKED_CHARACTER, "새 추적 캐릭터");
        it(NEW_TRACKED_CHARACTER, "Nuovo personaggio tracciato");

        ja(EDIT_TRACKED_CHARACTER, "追跡キャラクターを編集");
        zh(EDIT_TRACKED_CHARACTER, "编辑被追踪的角色");
        fr(EDIT_TRACKED_CHARACTER, "Modifier le personnage suivi");
        es(EDIT_TRACKED_CHARACTER, "Editar personaje seguido");
        de(EDIT_TRACKED_CHARACTER, "Verfolgte Figur bearbeiten");
        ko(EDIT_TRACKED_CHARACTER, "추적 캐릭터 편집");
        it(EDIT_TRACKED_CHARACTER, "Modifica il personaggio tracciato");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");

        ja(ALIASES, "別名");
        zh(ALIASES, "别名");
        fr(ALIASES, "Autres noms");
        es(ALIASES, "Alias");
        de(ALIASES, "Aliasnamen");
        ko(ALIASES, "별칭");
        it(ALIASES, "Alias");

        ja(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "「登場人物」欄でこのキャラクターが書かれる他の名前（カンマ区切り）");
        zh(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "该角色在“在场角色”栏中的其他写法，用逗号分隔");
        fr(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Autres noms sous lesquels le personnage est écrit dans le champ des personnages présents, séparés par des virgules");
        es(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Otros nombres con los que se escribe el personaje en el campo de personajes presentes, separados por comas");
        de(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Andere Namen, unter denen die Figur im Feld „Anwesende Figuren“ geschrieben wird, durch Kommas getrennt");
        ko(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "등장인물 필드에서 캐릭터가 표기되는 다른 이름, 쉼표로 구분");
        it(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN, "Altri nomi con cui il personaggio è scritto nel campo dei personaggi presenti, separati da virgole");

        ja(TRACKED_CATEGORIES, "追跡するカテゴリー");
        zh(TRACKED_CATEGORIES, "追踪的类别");
        fr(TRACKED_CATEGORIES, "Catégories suivies");
        es(TRACKED_CATEGORIES, "Categorías seguidas");
        de(TRACKED_CATEGORIES, "Verfolgte Kategorien");
        ko(TRACKED_CATEGORIES, "추적할 카테고리");
        it(TRACKED_CATEGORIES, "Categorie tracciate");

        ja(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "このキャラクターについて記録する内容です。自分のカテゴリーを選ぶと、本にコピーされます。");
        zh(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "插件为该角色记录的内容。选择你自己的类别时，会把它复制到本书中。");
        fr(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "Ce que le plugin garde pour ce personnage. Vos propres catégories sont copiées dans le livre quand vous les choisissez.");
        es(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "Lo que el plugin guarda de este personaje. Tus propias categorías se copian en el libro cuando las eliges.");
        de(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "Was das Plugin für diese Figur festhält. Deine eigenen Kategorien werden ins Buch kopiert, wenn du sie auswählst.");
        ko(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "이 캐릭터에 대해 플러그인이 기록하는 내용입니다. 내 카테고리를 고르면 책에 복사됩니다.");
        it(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER, "Ciò che il plugin conserva per questo personaggio. Le tue categorie vengono copiate nel libro quando le scegli.");

        ja(FROM_MY_CATEGORIES, "%s（マイカテゴリーから）");
        zh(FROM_MY_CATEGORIES, "%s（来自我的类别）");
        fr(FROM_MY_CATEGORIES, "%s (de mes catégories)");
        es(FROM_MY_CATEGORIES, "%s (de mis categorías)");
        de(FROM_MY_CATEGORIES, "%s (aus meinen Kategorien)");
        ko(FROM_MY_CATEGORIES, "%s (내 카테고리에서)");
        it(FROM_MY_CATEGORIES, "%s (dalle mie categorie)");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");
        it(SAVE, "Salva");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");
        it(CANCEL, "Annulla");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");
        fr(EDIT, "Modifier");
        es(EDIT, "Editar");
        de(EDIT, "Bearbeiten");
        ko(EDIT, "편집");
        it(EDIT, "Modifica");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");
        ko(DELETE, "삭제");
        it(DELETE, "Elimina");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(GIVE_THE_CHARACTER_A_NAME, "キャラクターに名前を付けてください");
        zh(GIVE_THE_CHARACTER_A_NAME, "请给角色起个名字");
        fr(GIVE_THE_CHARACTER_A_NAME, "Donnez un nom au personnage");
        es(GIVE_THE_CHARACTER_A_NAME, "Ponle un nombre al personaje");
        de(GIVE_THE_CHARACTER_A_NAME, "Gib der Figur einen Namen");
        ko(GIVE_THE_CHARACTER_A_NAME, "캐릭터에 이름을 지정하세요");
        it(GIVE_THE_CHARACTER_A_NAME, "Dai un nome al personaggio");

        ja(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名前または別名は「%s」で既に使われています");
        zh(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "名称或别名已被“%s”使用");
        fr(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "Le nom ou un autre nom est déjà utilisé par %s");
        es(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "El nombre o un alias ya lo usa %s");
        de(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "Der Name oder ein Aliasname wird bereits von %s verwendet");
        ko(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "이름이나 별칭이 이미 %s에서 사용 중입니다");
        it(THE_NAME_OR_AN_ALIAS_IS_USED_BY, "Il nome o un alias è già usato da %s");

        ja(STOP_TRACKING_AND_DELETE_ITS_STATE, "%s の追跡をやめ、状態をすべて削除しますか？");
        zh(STOP_TRACKING_AND_DELETE_ITS_STATE, "停止追踪 %s 并删除其全部状态？");
        fr(STOP_TRACKING_AND_DELETE_ITS_STATE, "Arrêter de suivre %s et supprimer tout son état ?");
        es(STOP_TRACKING_AND_DELETE_ITS_STATE, "¿Dejar de seguir a %s y borrar todo su estado?");
        de(STOP_TRACKING_AND_DELETE_ITS_STATE, "%s nicht mehr verfolgen und den ganzen Zustand löschen?");
        ko(STOP_TRACKING_AND_DELETE_ITS_STATE, "%s 추적을 멈추고 상태를 모두 삭제할까요?");
        it(STOP_TRACKING_AND_DELETE_ITS_STATE, "Smettere di tracciare %s ed eliminare tutto il suo stato?");

        ja(NO_CHARACTER_IS_TRACKED_YET, "追跡中のキャラクターはまだありません。ここで追加したキャラクターだけが、パートの登場人物に含まれている間だけモデルに説明されます。");
        zh(NO_CHARACTER_IS_TRACKED_YET, "尚未追踪任何角色。只有在此添加的角色，并且处于某一部分的在场角色之中时，才会向模型说明。");
        fr(NO_CHARACTER_IS_TRACKED_YET, "Aucun personnage n'est encore suivi. Seuls les personnages ajoutés ici sont décrits au modèle, et seulement lorsqu'ils font partie des personnages présents d'une partie.");
        es(NO_CHARACTER_IS_TRACKED_YET, "Todavía no se sigue a ningún personaje. Solo se describe al modelo a los personajes que añades aquí, y solo mientras estén entre los personajes presentes de una parte.");
        de(NO_CHARACTER_IS_TRACKED_YET, "Noch wird keine Figur verfolgt. Nur die Figuren, die du hier hinzufügst, werden dem Modell beschrieben, und nur solange sie zu den anwesenden Figuren eines Abschnitts gehören.");
        ko(NO_CHARACTER_IS_TRACKED_YET, "아직 추적하는 캐릭터가 없습니다. 여기에 추가한 캐릭터만 모델에 설명되며, 파트의 등장인물에 포함되어 있을 때만 해당됩니다.");
        it(NO_CHARACTER_IS_TRACKED_YET, "Nessun personaggio è ancora tracciato. Solo i personaggi che aggiungi qui vengono descritti al modello, e solo mentre sono tra i personaggi presenti di una parte.");

        ja(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "このキャラクターには追跡するカテゴリーがありません。編集して選んでください。");
        zh(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "该角色没有追踪的类别。编辑角色来选择一些。");
        fr(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "Aucune catégorie n'est suivie pour ce personnage. Modifiez-le pour en choisir.");
        es(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "No se sigue ninguna categoría de este personaje. Edítalo para elegir algunas.");
        de(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "Für diese Figur wird keine Kategorie verfolgt. Bearbeite sie, um welche auszuwählen.");
        ko(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "이 캐릭터에는 추적하는 카테고리가 없습니다. 편집해서 고르세요.");
        it(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER, "Nessuna categoria è tracciata per questo personaggio. Modificalo per sceglierne qualcuna.");

        ja(NOTHING_YET, "まだなし");
        zh(NOTHING_YET, "暂无");
        fr(NOTHING_YET, "Rien pour l'instant");
        es(NOTHING_YET, "Nada todavía");
        de(NOTHING_YET, "Noch nichts");
        ko(NOTHING_YET, "아직 없음");
        it(NOTHING_YET, "Ancora niente");

        ja(SET_THE_VALUE, "値を設定");
        zh(SET_THE_VALUE, "设置值");
        fr(SET_THE_VALUE, "Définir la valeur");
        es(SET_THE_VALUE, "Fijar el valor");
        de(SET_THE_VALUE, "Wert setzen");
        ko(SET_THE_VALUE, "값 설정");
        it(SET_THE_VALUE, "Imposta il valore");

        ja(ADD_AN_ENTRY, "項目を追加");
        zh(ADD_AN_ENTRY, "添加条目");
        fr(ADD_AN_ENTRY, "Ajouter une entrée");
        es(ADD_AN_ENTRY, "Añadir una entrada");
        de(ADD_AN_ENTRY, "Eintrag hinzufügen");
        ko(ADD_AN_ENTRY, "항목 추가");
        it(ADD_AN_ENTRY, "Aggiungi una voce");

        ja(NEW_VALUE, "新しい値");
        zh(NEW_VALUE, "新值");
        fr(NEW_VALUE, "Nouvelle valeur");
        es(NEW_VALUE, "Nuevo valor");
        de(NEW_VALUE, "Neuer Wert");
        ko(NEW_VALUE, "새 값");
        it(NEW_VALUE, "Nuovo valore");

        ja(EDIT_VALUE, "値を編集");
        zh(EDIT_VALUE, "编辑值");
        fr(EDIT_VALUE, "Modifier la valeur");
        es(EDIT_VALUE, "Editar valor");
        de(EDIT_VALUE, "Wert bearbeiten");
        ko(EDIT_VALUE, "값 편집");
        it(EDIT_VALUE, "Modifica il valore");

        ja(CATEGORY, "カテゴリー");
        zh(CATEGORY, "类别");
        fr(CATEGORY, "Catégorie");
        es(CATEGORY, "Categoría");
        de(CATEGORY, "Kategorie");
        ko(CATEGORY, "카테고리");
        it(CATEGORY, "Categoria");

        ja(VALUE, "値");
        zh(VALUE, "值");
        fr(VALUE, "Valeur");
        es(VALUE, "Valor");
        de(VALUE, "Wert");
        ko(VALUE, "값");
        it(VALUE, "Valore");

        ja(WRITE_SOMETHING, "内容を入力してください");
        zh(WRITE_SOMETHING, "请输入内容");
        fr(WRITE_SOMETHING, "Écrivez quelque chose");
        es(WRITE_SOMETHING, "Escribe algo");
        de(WRITE_SOMETHING, "Schreibe etwas");
        ko(WRITE_SOMETHING, "무언가 쓰세요");
        it(WRITE_SOMETHING, "Scrivi qualcosa");

        ja(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "現在の値は履歴に残ります。");
        zh(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "当前值会保留在历史中。");
        fr(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "La valeur actuelle est gardée dans l'historique.");
        es(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "El valor actual se guarda en el historial.");
        de(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "Der aktuelle Wert bleibt im Verlauf erhalten.");
        ko(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "현재 값은 기록에 남습니다.");
        it(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY, "Il valore attuale resta nella cronologia.");

        ja(LOCKED, "ロック");
        zh(LOCKED, "已锁定");
        fr(LOCKED, "Verrouillé");
        es(LOCKED, "Bloqueado");
        de(LOCKED, "Gesperrt");
        ko(LOCKED, "잠김");
        it(LOCKED, "Bloccato");

        ja(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "整理ではロックされた値に触れません（傷跡、家宝の剣など）");
        zh(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "整理时不会动已锁定的值（伤疤、家传之剑）");
        fr(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "Le rangement ne touche pas une valeur verrouillée (une cicatrice, l'épée de famille)");
        es(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "La ordenación no toca un valor bloqueado (una cicatriz, la espada de la familia)");
        de(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "Das Aufräumen lässt einen gesperrten Wert in Ruhe (eine Narbe, das Familienschwert)");
        ko(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "정리는 잠긴 값을 건드리지 않습니다(흉터, 가문의 검)");
        it(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE, "Il riordino non tocca un valore bloccato (una cicatrice, la spada di famiglia)");

        ja(LOCK, "ロック");
        zh(LOCK, "锁定");
        fr(LOCK, "Verrouiller");
        es(LOCK, "Bloquear");
        de(LOCK, "Sperren");
        ko(LOCK, "잠금");
        it(LOCK, "Blocca");

        ja(UNLOCK, "ロック解除");
        zh(UNLOCK, "解锁");
        fr(UNLOCK, "Déverrouiller");
        es(UNLOCK, "Desbloquear");
        de(UNLOCK, "Entsperren");
        ko(UNLOCK, "잠금 해제");
        it(UNLOCK, "Sblocca");

        ja(REMOVE, "外す");
        zh(REMOVE, "移除");
        fr(REMOVE, "Retirer");
        es(REMOVE, "Quitar");
        de(REMOVE, "Entfernen");
        ko(REMOVE, "제거");
        it(REMOVE, "Rimuovi");

        ja(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "もう当てはまらない：履歴に残し、モデルには送らない");
        zh(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "不再成立：保留在历史中，不发送给模型");
        fr(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "N'est plus vrai : le garder dans l'historique et ne plus l'envoyer au modèle");
        es(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "Ya no es cierto: guardarlo en el historial y no enviarlo al modelo");
        de(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "Stimmt nicht mehr: im Verlauf behalten und nicht an das Modell senden");
        ko(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "더 이상 사실이 아님: 기록에 남기고 모델에는 보내지 않음");
        it(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY, "Non è più vero: tenerlo nella cronologia e non inviarlo al modello");

        ja(RESTORE, "復元");
        zh(RESTORE, "恢复");
        fr(RESTORE, "Restaurer");
        es(RESTORE, "Restaurar");
        de(RESTORE, "Wiederherstellen");
        ko(RESTORE, "복원");
        it(RESTORE, "Ripristina");

        ja(MAKE_IT_CURRENT_AGAIN, "再び現在の値にする");
        zh(MAKE_IT_CURRENT_AGAIN, "重新设为当前值");
        fr(MAKE_IT_CURRENT_AGAIN, "La rendre actuelle à nouveau");
        es(MAKE_IT_CURRENT_AGAIN, "Volver a hacerlo actual");
        de(MAKE_IT_CURRENT_AGAIN, "Wieder aktuell machen");
        ko(MAKE_IT_CURRENT_AGAIN, "다시 현재 값으로");
        it(MAKE_IT_CURRENT_AGAIN, "Rendilo di nuovo attuale");

        ja(HISTORY_N, "履歴（%d）");
        zh(HISTORY_N, "历史（%d）");
        fr(HISTORY_N, "Historique (%d)");
        es(HISTORY_N, "Historial (%d)");
        de(HISTORY_N, "Verlauf (%d)");
        ko(HISTORY_N, "기록 (%d)");
        it(HISTORY_N, "Cronologia (%d)");

        ja(DELETE_THIS_VALUE_FOR_GOOD, "この値を完全に削除しますか？「外す」なら履歴に残ります。");
        zh(DELETE_THIS_VALUE_FOR_GOOD, "要永久删除这个值吗？“移除”会把它保留在历史中。");
        fr(DELETE_THIS_VALUE_FOR_GOOD, "Supprimer cette valeur définitivement ? Retirer la garde dans l'historique.");
        es(DELETE_THIS_VALUE_FOR_GOOD, "¿Borrar este valor para siempre? Quitar lo guarda en el historial.");
        de(DELETE_THIS_VALUE_FOR_GOOD, "Diesen Wert endgültig löschen? Entfernen behält ihn im Verlauf.");
        ko(DELETE_THIS_VALUE_FOR_GOOD, "이 값을 완전히 삭제할까요? 제거하면 기록에 남습니다.");
        it(DELETE_THIS_VALUE_FOR_GOOD, "Eliminare questo valore per sempre? Rimuovi lo tiene nella cronologia.");

        ja(REPLACED, "置き換え");
        zh(REPLACED, "已替换");
        fr(REPLACED, "remplacé");
        es(REPLACED, "reemplazado");
        de(REPLACED, "ersetzt");
        ko(REPLACED, "교체됨");
        it(REPLACED, "sostituito");

        ja(REMOVED, "外した");
        zh(REMOVED, "已移除");
        fr(REMOVED, "retiré");
        es(REMOVED, "quitado");
        de(REMOVED, "entfernt");
        ko(REMOVED, "제거됨");
        it(REMOVED, "rimosso");

        ja(CATEGORIES_OF_THIS_BOOK, "この本のカテゴリー");
        zh(CATEGORIES_OF_THIS_BOOK, "本书的类别");
        fr(CATEGORIES_OF_THIS_BOOK, "Catégories de ce livre");
        es(CATEGORIES_OF_THIS_BOOK, "Categorías de este libro");
        de(CATEGORIES_OF_THIS_BOOK, "Kategorien dieses Buchs");
        ko(CATEGORIES_OF_THIS_BOOK, "이 책의 카테고리");
        it(CATEGORIES_OF_THIS_BOOK, "Categorie di questo libro");

        ja(NEW_CATEGORY_FOR_THIS_BOOK, "この本の新しいカテゴリー");
        zh(NEW_CATEGORY_FOR_THIS_BOOK, "本书的新类别");
        fr(NEW_CATEGORY_FOR_THIS_BOOK, "Nouvelle catégorie pour ce livre");
        es(NEW_CATEGORY_FOR_THIS_BOOK, "Nueva categoría para este libro");
        de(NEW_CATEGORY_FOR_THIS_BOOK, "Neue Kategorie für dieses Buch");
        ko(NEW_CATEGORY_FOR_THIS_BOOK, "이 책의 새 카테고리");
        it(NEW_CATEGORY_FOR_THIS_BOOK, "Nuova categoria per questo libro");

        ja(ADD_FROM_MY_CATEGORIES, "マイカテゴリーから追加");
        zh(ADD_FROM_MY_CATEGORIES, "从我的类别添加");
        fr(ADD_FROM_MY_CATEGORIES, "Ajouter depuis mes catégories");
        es(ADD_FROM_MY_CATEGORIES, "Añadir de mis categorías");
        de(ADD_FROM_MY_CATEGORIES, "Aus meinen Kategorien hinzufügen");
        ko(ADD_FROM_MY_CATEGORIES, "내 카테고리에서 추가");
        it(ADD_FROM_MY_CATEGORIES, "Aggiungi dalle mie categorie");

        ja(ONLY_IN_THIS_BOOK, "この本のみ");
        zh(ONLY_IN_THIS_BOOK, "仅限本书");
        fr(ONLY_IN_THIS_BOOK, "Seulement dans ce livre");
        es(ONLY_IN_THIS_BOOK, "Solo en este libro");
        de(ONLY_IN_THIS_BOOK, "Nur in diesem Buch");
        ko(ONLY_IN_THIS_BOOK, "이 책에만");
        it(ONLY_IN_THIS_BOOK, "Solo in questo libro");

        ja(DIFFERS_FROM_YOUR_CATEGORY, "マイカテゴリーと異なります");
        zh(DIFFERS_FROM_YOUR_CATEGORY, "与你的类别不同");
        fr(DIFFERS_FROM_YOUR_CATEGORY, "Diffère de votre catégorie");
        es(DIFFERS_FROM_YOUR_CATEGORY, "Difiere de tu categoría");
        de(DIFFERS_FROM_YOUR_CATEGORY, "Weicht von deiner Kategorie ab");
        ko(DIFFERS_FROM_YOUR_CATEGORY, "내 카테고리와 다름");
        it(DIFFERS_FROM_YOUR_CATEGORY, "Diversa dalla tua categoria");

        ja(UPDATE_FROM_MY_CATEGORIES, "マイカテゴリーから更新");
        zh(UPDATE_FROM_MY_CATEGORIES, "从我的类别更新");
        fr(UPDATE_FROM_MY_CATEGORIES, "Mettre à jour depuis mes catégories");
        es(UPDATE_FROM_MY_CATEGORIES, "Actualizar desde mis categorías");
        de(UPDATE_FROM_MY_CATEGORIES, "Aus meinen Kategorien aktualisieren");
        ko(UPDATE_FROM_MY_CATEGORIES, "내 카테고리에서 업데이트");
        it(UPDATE_FROM_MY_CATEGORIES, "Aggiorna dalle mie categorie");

        ja(SAVE_TO_MY_CATEGORIES, "マイカテゴリーに保存");
        zh(SAVE_TO_MY_CATEGORIES, "保存到我的类别");
        fr(SAVE_TO_MY_CATEGORIES, "Enregistrer dans mes catégories");
        es(SAVE_TO_MY_CATEGORIES, "Guardar en mis categorías");
        de(SAVE_TO_MY_CATEGORIES, "In meinen Kategorien speichern");
        ko(SAVE_TO_MY_CATEGORIES, "내 카테고리에 저장");
        it(SAVE_TO_MY_CATEGORIES, "Salva nelle mie categorie");

        ja(SAVED_TO_YOUR_CATEGORIES, "マイカテゴリーに保存しました");
        zh(SAVED_TO_YOUR_CATEGORIES, "已保存到你的类别");
        fr(SAVED_TO_YOUR_CATEGORIES, "Enregistrée dans vos catégories");
        es(SAVED_TO_YOUR_CATEGORIES, "Guardada en tus categorías");
        de(SAVED_TO_YOUR_CATEGORIES, "In deinen Kategorien gespeichert");
        ko(SAVED_TO_YOUR_CATEGORIES, "내 카테고리에 저장했습니다");
        it(SAVED_TO_YOUR_CATEGORIES, "Salvata nelle tue categorie");

        ja(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "カテゴリー %s をこの本から削除しますか？ %d 人のキャラクターが追跡しており、その値はすべて削除されます。");
        zh(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "要从本书删除类别 %s 吗？有 %d 个角色在追踪它，它们在该类别下的值都会被删除。");
        fr(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "Supprimer la catégorie %s de ce livre ? %d personnage(s) la suivent ; toutes leurs valeurs de cette catégorie sont supprimées.");
        es(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "¿Borrar la categoría %s de este libro? La siguen %d personaje(s); se borran todos sus valores de ella.");
        de(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "Die Kategorie %s aus diesem Buch löschen? %d Figur(en) verfolgen sie; alle ihre Werte darin werden gelöscht.");
        ko(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "이 책에서 카테고리 %s 을(를) 삭제할까요? %d명의 캐릭터가 추적 중이며, 그 값은 모두 삭제됩니다.");
        it(DELETE_THE_CATEGORY_FROM_THIS_BOOK, "Eliminare la categoria %s da questo libro? La tracciano %d personaggi; tutti i loro valori di essa vengono eliminati.");

        ja(THE_BOOK_USES_NO_CATEGORY_YET, "この本はまだカテゴリーを使っていません。ここで追加するか、キャラクターを追跡するときに選んでください。");
        zh(THE_BOOK_USES_NO_CATEGORY_YET, "本书还没有使用任何类别。在这里添加，或在追踪角色时选择。");
        fr(THE_BOOK_USES_NO_CATEGORY_YET, "Le livre n'utilise encore aucune catégorie. Ajoutez-en une ici, ou choisissez-en quand vous suivez un personnage.");
        es(THE_BOOK_USES_NO_CATEGORY_YET, "El libro aún no usa ninguna categoría. Añade una aquí, o elige categorías al seguir a un personaje.");
        de(THE_BOOK_USES_NO_CATEGORY_YET, "Das Buch verwendet noch keine Kategorie. Füge hier eine hinzu oder wähle Kategorien, wenn du eine Figur verfolgst.");
        ko(THE_BOOK_USES_NO_CATEGORY_YET, "이 책은 아직 카테고리를 쓰지 않습니다. 여기서 추가하거나, 캐릭터를 추적할 때 고르세요.");
        it(THE_BOOK_USES_NO_CATEGORY_YET, "Il libro non usa ancora nessuna categoria. Aggiungine una qui, o scegli le categorie quando tracci un personaggio.");

        ja(NEW_CATEGORY, "新しいカテゴリー");
        zh(NEW_CATEGORY, "新类别");
        fr(NEW_CATEGORY, "Nouvelle catégorie");
        es(NEW_CATEGORY, "Nueva categoría");
        de(NEW_CATEGORY, "Neue Kategorie");
        ko(NEW_CATEGORY, "새 카테고리");
        it(NEW_CATEGORY, "Nuova categoria");

        ja(EDIT_CATEGORY, "カテゴリーを編集");
        zh(EDIT_CATEGORY, "编辑类别");
        fr(EDIT_CATEGORY, "Modifier la catégorie");
        es(EDIT_CATEGORY, "Editar categoría");
        de(EDIT_CATEGORY, "Kategorie bearbeiten");
        ko(EDIT_CATEGORY, "카테고리 편집");
        it(EDIT_CATEGORY, "Modifica la categoria");

        ja(KIND, "種類");
        zh(KIND, "类型");
        fr(KIND, "Genre");
        es(KIND, "Tipo");
        de(KIND, "Art");
        ko(KIND, "종류");
        it(KIND, "Tipo");

        ja(SINGLE_VALUE, "単一の値");
        zh(SINGLE_VALUE, "单个值");
        fr(SINGLE_VALUE, "Valeur unique");
        es(SINGLE_VALUE, "Valor único");
        de(SINGLE_VALUE, "Einzelner Wert");
        ko(SINGLE_VALUE, "단일 값");
        it(SINGLE_VALUE, "Valore singolo");

        ja(LIST, "リスト");
        zh(LIST, "列表");
        fr(LIST, "Liste");
        es(LIST, "Lista");
        de(LIST, "Liste");
        ko(LIST, "목록");
        it(LIST, "Elenco");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "描述");
        fr(DESCRIPTION, "Description");
        es(DESCRIPTION, "Descripción");
        de(DESCRIPTION, "Beschreibung");
        ko(DESCRIPTION, "설명");
        it(DESCRIPTION, "Descrizione");

        ja(KIND_HELP, "単一の値：新しい値が古い値を置き換えます。リスト：項目を追加・削除します。説明：一つの文章を丸ごと書き直します。");
        zh(KIND_HELP, "单个值：新值替换旧值。列表：逐条添加和移除。描述：一段文字，整体改写。");
        fr(KIND_HELP, "Valeur unique : une nouvelle valeur remplace l'ancienne. Liste : on ajoute et retire des entrées. Description : un texte, réécrit en entier.");
        es(KIND_HELP, "Valor único: un valor nuevo reemplaza al anterior. Lista: se añaden y quitan entradas. Descripción: un texto, reescrito entero.");
        de(KIND_HELP, "Einzelner Wert: Ein neuer Wert ersetzt den alten. Liste: Einträge werden hinzugefügt und entfernt. Beschreibung: ein Text, als Ganzes neu geschrieben.");
        ko(KIND_HELP, "단일 값: 새 값이 이전 값을 대신합니다. 목록: 항목을 추가하고 제거합니다. 설명: 하나의 글을 통째로 다시 씁니다.");
        it(KIND_HELP, "Valore singolo: un nuovo valore sostituisce il vecchio. Elenco: le voci si aggiungono e si tolgono. Descrizione: un testo, riscritto per intero.");

        ja(THE_KIND_CANT_CHANGE, "キャラクターがこのカテゴリーの値を持っている間は種類を変えられません");
        zh(THE_KIND_CANT_CHANGE, "角色拥有该类别的值时不能更改种类");
        fr(THE_KIND_CANT_CHANGE, "Le type ne peut pas changer tant que des personnages ont des valeurs de cette catégorie");
        es(THE_KIND_CANT_CHANGE, "El tipo no puede cambiar mientras haya personajes con valores de esta categoría");
        de(THE_KIND_CANT_CHANGE, "Die Art kann sich nicht ändern, solange Figuren Werte dieser Kategorie haben");
        ko(THE_KIND_CANT_CHANGE, "캐릭터가 이 카테고리의 값을 가지고 있는 동안에는 종류를 바꿀 수 없습니다");
        it(THE_KIND_CANT_CHANGE, "Il tipo non può cambiare finché dei personaggi hanno valori di questa categoria");

        ja(INSTRUCTION_FOR_THE_MODEL, "モデルへの指示");
        zh(INSTRUCTION_FOR_THE_MODEL, "给模型的说明");
        fr(INSTRUCTION_FOR_THE_MODEL, "Consigne pour le modèle");
        es(INSTRUCTION_FOR_THE_MODEL, "Instrucción para el modelo");
        de(INSTRUCTION_FOR_THE_MODEL, "Anweisung für das Modell");
        ko(INSTRUCTION_FOR_THE_MODEL, "모델을 위한 지침");
        it(INSTRUCTION_FOR_THE_MODEL, "Istruzione per il modello");

        ja(WHAT_BELONGS_IN_THE_CATEGORY, "このカテゴリーに入るもの・入らないもの。モデルが物語を追うときに読みます");
        zh(WHAT_BELONGS_IN_THE_CATEGORY, "哪些属于该类别、哪些不属于；模型跟进故事时会读取");
        fr(WHAT_BELONGS_IN_THE_CATEGORY, "Ce qui appartient à la catégorie et ce qui n'y appartient pas ; le modèle le lit quand il suit l'histoire");
        es(WHAT_BELONGS_IN_THE_CATEGORY, "Qué pertenece a la categoría y qué no; el modelo lo lee cuando sigue la historia");
        de(WHAT_BELONGS_IN_THE_CATEGORY, "Was in die Kategorie gehört und was nicht; das Modell liest es, wenn es der Geschichte folgt");
        ko(WHAT_BELONGS_IN_THE_CATEGORY, "이 카테고리에 들어가는 것과 들어가지 않는 것. 모델이 이야기를 따라갈 때 읽습니다");
        it(WHAT_BELONGS_IN_THE_CATEGORY, "Cosa appartiene alla categoria e cosa no; il modello lo legge quando segue la storia");

        ja(ONLY_WHAT_THE_TEXT_SAYS, "本文に書かれたことだけ");
        zh(ONLY_WHAT_THE_TEXT_SAYS, "只按文中所写");
        fr(ONLY_WHAT_THE_TEXT_SAYS, "Seulement ce que dit le texte");
        es(ONLY_WHAT_THE_TEXT_SAYS, "Solo lo que dice el texto");
        de(ONLY_WHAT_THE_TEXT_SAYS, "Nur, was der Text sagt");
        ko(ONLY_WHAT_THE_TEXT_SAYS, "본문에 쓰인 것만");
        it(ONLY_WHAT_THE_TEXT_SAYS, "Solo ciò che dice il testo");

        ja(ON_THE_VALUE_CHANGES_ONLY_WHEN, "オン：本文がそう書いたときだけ値が変わります。オフ：明らかな結果でも変わります（泳いだ後は濡れている）。");
        zh(ON_THE_VALUE_CHANGES_ONLY_WHEN, "开：只有文中写明时才改变值。关：明显的后果也会改变（游泳后衣服是湿的）。");
        fr(ON_THE_VALUE_CHANGES_ONLY_WHEN, "Activé : la valeur ne change que si le texte le dit. Désactivé : aussi sur les conséquences évidentes (mouillé après une baignade).");
        es(ON_THE_VALUE_CHANGES_ONLY_WHEN, "Activado: el valor cambia solo cuando el texto lo dice. Desactivado: también por consecuencias obvias (mojado tras nadar).");
        de(ON_THE_VALUE_CHANGES_ONLY_WHEN, "An: Der Wert ändert sich nur, wenn der Text es sagt. Aus: auch bei offensichtlichen Folgen (nass nach dem Schwimmen).");
        ko(ON_THE_VALUE_CHANGES_ONLY_WHEN, "켜짐: 본문에 그렇게 쓰였을 때만 값이 바뀝니다. 꺼짐: 명백한 결과에도 바뀝니다(수영 후에는 젖어 있음).");
        it(ON_THE_VALUE_CHANGES_ONLY_WHEN, "Attivo: il valore cambia solo quando il testo lo dice. Disattivo: anche per conseguenze ovvie (bagnato dopo una nuotata).");

        ja(SAY_NONE_WHEN_EMPTY, "空なら「なし」と書く");
        zh(SAY_NONE_WHEN_EMPTY, "为空时写“无”");
        fr(SAY_NONE_WHEN_EMPTY, "Dire « aucun » quand c'est vide");
        es(SAY_NONE_WHEN_EMPTY, "Decir «ninguno» cuando esté vacío");
        de(SAY_NONE_WHEN_EMPTY, "„keine“ sagen, wenn leer");
        ko(SAY_NONE_WHEN_EMPTY, "비어 있으면 \"없음\"이라고 쓰기");
        it(SAY_NONE_WHEN_EMPTY, "Dire «nessuno» quando è vuoto");

        ja(THE_PROMPT_SAYS_NONE, "キャラクターに値がないとき（武器を失った）、カテゴリーを省かずに none と書きます");
        zh(THE_PROMPT_SAYS_NONE, "角色没有值时（武器丢了），提示中写 none，而不是省略该类别");
        fr(THE_PROMPT_SAYS_NONE, "Le prompt dit none quand le personnage n'a pas de valeur (une arme perdue) au lieu d'omettre la catégorie");
        es(THE_PROMPT_SAYS_NONE, "El prompt dice none cuando el personaje no tiene valor (un arma perdida) en vez de omitir la categoría");
        de(THE_PROMPT_SAYS_NONE, "Der Prompt sagt none, wenn die Figur keinen Wert hat (eine verlorene Waffe), statt die Kategorie wegzulassen");
        ko(THE_PROMPT_SAYS_NONE, "캐릭터에 값이 없을 때(무기를 잃음) 카테고리를 빼지 않고 none이라고 씁니다");
        it(THE_PROMPT_SAYS_NONE, "Il prompt dice none quando il personaggio non ha un valore (un'arma persa) invece di omettere la categoria");

        ja(GIVE_THE_CATEGORY_A_NAME, "カテゴリーに名前を付けてください");
        zh(GIVE_THE_CATEGORY_A_NAME, "请给类别起个名字");
        fr(GIVE_THE_CATEGORY_A_NAME, "Donnez un nom à la catégorie");
        es(GIVE_THE_CATEGORY_A_NAME, "Dale un nombre a la categoría");
        de(GIVE_THE_CATEGORY_A_NAME, "Gib der Kategorie einen Namen");
        ko(GIVE_THE_CATEGORY_A_NAME, "카테고리에 이름을 지어 주세요");
        it(GIVE_THE_CATEGORY_A_NAME, "Dai un nome alla categoria");

        ja(A_CATEGORY_IS_CALLED_SO_ALREADY, "%s という名前のカテゴリーは既にあります");
        zh(A_CATEGORY_IS_CALLED_SO_ALREADY, "已有名为 %s 的类别");
        fr(A_CATEGORY_IS_CALLED_SO_ALREADY, "Une catégorie s'appelle déjà %s");
        es(A_CATEGORY_IS_CALLED_SO_ALREADY, "Ya hay una categoría llamada %s");
        de(A_CATEGORY_IS_CALLED_SO_ALREADY, "Eine Kategorie heißt bereits %s");
        ko(A_CATEGORY_IS_CALLED_SO_ALREADY, "%s(이)라는 카테고리가 이미 있습니다");
        it(A_CATEGORY_IS_CALLED_SO_ALREADY, "Una categoria si chiama già %s");

        ja(MY_CATEGORIES, "マイカテゴリー");
        zh(MY_CATEGORIES, "我的类别");
        fr(MY_CATEGORIES, "Mes catégories");
        es(MY_CATEGORIES, "Mis categorías");
        de(MY_CATEGORIES, "Meine Kategorien");
        ko(MY_CATEGORIES, "내 카테고리");
        it(MY_CATEGORIES, "Le mie categorie");

        ja(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "本がコピーする元のカテゴリーです。本は自分のコピーを持つので、ここでの変更は本の側で更新したときに反映されます。");
        zh(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "书籍从这里复制类别。每本书保留自己的副本：这里的修改要在书中更新后才会生效。");
        fr(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "Les catégories que vos livres copient. Un livre garde sa propre copie : un changement ici l'atteint quand vous le mettez à jour là-bas.");
        es(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "Las categorías que copian tus libros. Cada libro guarda su propia copia: un cambio aquí llega a un libro cuando lo actualizas allí.");
        de(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "Die Kategorien, aus denen deine Bücher kopieren. Ein Buch behält seine eigene Kopie: Eine Änderung hier erreicht ein Buch, wenn du es dort aktualisierst.");
        ko(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "책이 복사해 가는 카테고리입니다. 책은 자기 사본을 가지므로, 여기서 바꾼 내용은 책에서 업데이트할 때 반영됩니다.");
        it(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM, "Le categorie da cui copiano i tuoi libri. Ogni libro tiene la sua copia: una modifica qui arriva a un libro quando lo aggiorni lì.");

        ja(RESET_TO_THE_BUILT_IN_CATEGORIES, "組み込みのカテゴリーに戻す");
        zh(RESET_TO_THE_BUILT_IN_CATEGORIES, "恢复为内置类别");
        fr(RESET_TO_THE_BUILT_IN_CATEGORIES, "Revenir aux catégories intégrées");
        es(RESET_TO_THE_BUILT_IN_CATEGORIES, "Volver a las categorías integradas");
        de(RESET_TO_THE_BUILT_IN_CATEGORIES, "Auf die eingebauten Kategorien zurücksetzen");
        ko(RESET_TO_THE_BUILT_IN_CATEGORIES, "기본 카테고리로 되돌리기");
        it(RESET_TO_THE_BUILT_IN_CATEGORIES, "Ripristina le categorie predefinite");

        ja(DELETE_YOUR_CATEGORY, "カテゴリー %s を削除しますか？使っている本は自分のコピーを保ちます。");
        zh(DELETE_YOUR_CATEGORY, "要删除你的类别 %s 吗？使用它的书会保留各自的副本。");
        fr(DELETE_YOUR_CATEGORY, "Supprimer votre catégorie %s ? Les livres qui l'utilisent gardent leur copie.");
        es(DELETE_YOUR_CATEGORY, "¿Borrar tu categoría %s? Los libros que la usan conservan su copia.");
        de(DELETE_YOUR_CATEGORY, "Deine Kategorie %s löschen? Bücher, die sie verwenden, behalten ihre Kopie.");
        ko(DELETE_YOUR_CATEGORY, "내 카테고리 %s 을(를) 삭제할까요? 사용하는 책은 사본을 유지합니다.");
        it(DELETE_YOUR_CATEGORY, "Eliminare la tua categoria %s? I libri che la usano conservano la loro copia.");

        ja(SETTINGS, "設定");
        zh(SETTINGS, "设置");
        fr(SETTINGS, "Paramètres");
        es(SETTINGS, "Ajustes");
        de(SETTINGS, "Einstellungen");
        ko(SETTINGS, "설정");
        it(SETTINGS, "Impostazioni");

        ja(REFRESH, "更新");
        zh(REFRESH, "刷新");
        fr(REFRESH, "Actualiser");
        es(REFRESH, "Actualizar");
        de(REFRESH, "Aktualisieren");
        ko(REFRESH, "새로 고침");
        it(REFRESH, "Aggiorna");

        ja(IN_PROMPT, "プロンプトに入れる");
        zh(IN_PROMPT, "加入提示词");
        fr(IN_PROMPT, "Dans le prompt");
        es(IN_PROMPT, "En el prompt");
        de(IN_PROMPT, "Im Prompt");
        ko(IN_PROMPT, "프롬프트에 포함");
        it(IN_PROMPT, "Nel prompt");

        ja(INSERT_THE_STATE_INTO_THE_PROMPT, "場面にいる追跡キャラクターの状態を、毎回の生成のプロンプトに入れる");
        zh(INSERT_THE_STATE_INTO_THE_PROMPT, "把场景中被追踪角色的状态插入每次生成的提示中");
        fr(INSERT_THE_STATE_INTO_THE_PROMPT, "Insérer l'état des personnages suivis de la scène dans le prompt de chaque génération");
        es(INSERT_THE_STATE_INTO_THE_PROMPT, "Insertar el estado de los personajes seguidos de la escena en el prompt de cada generación");
        de(INSERT_THE_STATE_INTO_THE_PROMPT, "Den Zustand der verfolgten Figuren der Szene in den Prompt jeder Generierung einfügen");
        ko(INSERT_THE_STATE_INTO_THE_PROMPT, "장면에 있는 추적 캐릭터의 상태를 매 생성의 프롬프트에 넣기");
        it(INSERT_THE_STATE_INTO_THE_PROMPT, "Inserire lo stato dei personaggi tracciati della scena nel prompt di ogni generazione");

        ja(DEPTH, "深さ");
        zh(DEPTH, "深度");
        fr(DEPTH, "Profondeur");
        es(DEPTH, "Profundidad");
        de(DEPTH, "Tiefe");
        ko(DEPTH, "깊이");
        it(DEPTH, "Profondità");

        ja(MESSAGES_AFTER_THE_STATE, "状態の後のメッセージ数：0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_STATE, "状态之后的消息数：0 = 在指令之后，1 = 紧挨指令之前");
        fr(MESSAGES_AFTER_THE_STATE, "Messages après l'état : 0 = après les instructions, 1 = juste avant elles");
        es(MESSAGES_AFTER_THE_STATE, "Mensajes después del estado: 0 = tras las instrucciones, 1 = justo antes de ellas");
        de(MESSAGES_AFTER_THE_STATE, "Nachrichten nach dem Zustand: 0 = nach den Anweisungen, 1 = direkt davor");
        ko(MESSAGES_AFTER_THE_STATE, "상태 뒤의 메시지 수: 0 = 지시 뒤, 1 = 지시 바로 앞");
        it(MESSAGES_AFTER_THE_STATE, "Messaggi dopo lo stato: 0 = dopo le istruzioni, 1 = subito prima");

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

        ja(NAME_SEPARATORS, "名前の区切り");
        zh(NAME_SEPARATORS, "名字分隔符");
        fr(NAME_SEPARATORS, "Séparateurs de noms");
        es(NAME_SEPARATORS, "Separadores de nombres");
        de(NAME_SEPARATORS, "Namenstrenner");
        ko(NAME_SEPARATORS, "이름 구분자");
        it(NAME_SEPARATORS, "Separatori dei nomi");

        ja(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "この本の登場キャラクター欄の区切り方。1行に1つ（改行は \\n）。空なら組み込みの区切り");
        zh(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "本书在场角色栏的拆分方式，每行一个（换行写 \\n）；留空 = 使用内置分隔符");
        fr(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "Comment le champ des personnages présents de ce livre est découpé, un par ligne (\\n pour un retour à la ligne) ; vide = ceux intégrés");
        es(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "Cómo se divide el campo de personajes presentes de este libro, uno por línea (\\n para un salto de línea); vacío = los integrados");
        de(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "Wie das Feld der anwesenden Figuren dieses Buchs geteilt wird, einer pro Zeile (\\n für einen Zeilenumbruch); leer = die eingebauten");
        ko(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "이 책의 등장인물 칸을 나누는 방법, 한 줄에 하나(줄바꿈은 \\n). 비우면 기본 구분자");
        it(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT, "Come si divide il campo dei personaggi presenti di questo libro, uno per riga (\\n per un a capo); vuoto = quelli predefiniti");

        ja(CATEGORIES_N, "カテゴリー（%d）");
        zh(CATEGORIES_N, "类别（%d）");
        fr(CATEGORIES_N, "Catégories (%d)");
        es(CATEGORIES_N, "Categorías (%d)");
        de(CATEGORIES_N, "Kategorien (%d)");
        ko(CATEGORIES_N, "카테고리 (%d)");
        it(CATEGORIES_N, "Categorie (%d)");

        ja(WEARING, "服装");
        zh(WEARING, "穿着");
        fr(WEARING, "Tenue");
        es(WEARING, "Ropa");
        de(WEARING, "Kleidung");
        ko(WEARING, "복장");
        it(WEARING, "Abbigliamento");

        ja(WEARING_INSTRUCTION, "キャラクターが今身に着けているもの。状態も含む（濡れている、破れている、血まみれ）。鞄の中の物は含まない。");
        zh(WEARING_INSTRUCTION, "角色此刻穿戴的东西，包括状态（湿的、破的、沾血的）。不包括包里带的东西。");
        fr(WEARING_INSTRUCTION, "Ce que le personnage porte en ce moment, avec son état (mouillé, déchiré, ensanglanté). Pas ce qu'il transporte dans des sacs.");
        es(WEARING_INSTRUCTION, "Lo que lleva puesto el personaje ahora, con su estado (mojado, roto, ensangrentado). No lo que lleva en bolsas.");
        de(WEARING_INSTRUCTION, "Was die Figur gerade trägt, mit Zustand (nass, zerrissen, blutig). Nicht, was sie in Taschen mitführt.");
        ko(WEARING_INSTRUCTION, "캐릭터가 지금 입고 있는 것과 그 상태(젖음, 찢어짐, 피투성이). 가방에 넣어 다니는 물건은 아님.");
        it(WEARING_INSTRUCTION, "Ciò che il personaggio indossa adesso, con il suo stato (bagnato, strappato, insanguinato). Non ciò che porta nelle borse.");

        ja(WEAPON_AT_HAND, "手元の武器");
        zh(WEAPON_AT_HAND, "随手武器");
        fr(WEAPON_AT_HAND, "Arme à portée de main");
        es(WEAPON_AT_HAND, "Arma a mano");
        de(WEAPON_AT_HAND, "Waffe zur Hand");
        ko(WEAPON_AT_HAND, "손에 든 무기");
        it(WEAPON_AT_HAND, "Arma a portata di mano");

        ja(WEAPON_INSTRUCTION, "キャラクターが手にしている、またはすぐ使える武器（腰、背中）。それ以外は含まない。");
        zh(WEAPON_INSTRUCTION, "角色手中或随时可用的武器（腰间、背上）。不包括其他东西。");
        fr(WEAPON_INSTRUCTION, "L'arme que le personnage a en main ou prête (à la ceinture, dans le dos). Rien d'autre.");
        es(WEAPON_INSTRUCTION, "El arma que el personaje tiene en la mano o lista (al cinto, a la espalda). Nada más.");
        de(WEAPON_INSTRUCTION, "Die Waffe, die die Figur in der Hand oder griffbereit hat (am Gürtel, auf dem Rücken). Sonst nichts.");
        ko(WEAPON_INSTRUCTION, "캐릭터가 손에 들었거나 바로 쓸 수 있는 무기(허리, 등). 그 밖의 것은 아님.");
        it(WEAPON_INSTRUCTION, "L'arma che il personaggio ha in mano o pronta (alla cintura, sulla schiena). Nient'altro.");

        ja(CARRYING, "持ち物");
        zh(CARRYING, "随身物品");
        fr(CARRYING, "Transporte");
        es(CARRYING, "Lleva");
        de(CARRYING, "Dabei");
        ko(CARRYING, "소지품");
        it(CARRYING, "Porta con sé");

        ja(CARRYING_INSTRUCTION, "キャラクターが身に付けて、または鞄に入れて持っている物。1件ずつ。服、手元の武器、小銭は含まない。");
        zh(CARRYING_INSTRUCTION, "角色带在身上或包里的东西，每样一条。不包括衣服、随手武器和零钱。");
        fr(CARRYING_INSTRUCTION, "Ce que le personnage transporte sur lui ou dans des sacs, une entrée par chose. Pas les vêtements, pas l'arme à portée de main, pas la menue monnaie.");
        es(CARRYING_INSTRUCTION, "Lo que el personaje lleva encima o en bolsas, una entrada por cosa. Ni la ropa, ni el arma a mano, ni la calderilla.");
        de(CARRYING_INSTRUCTION, "Dinge, die die Figur am Körper oder in Taschen dabeihat, je ein Eintrag. Keine Kleidung, nicht die Waffe zur Hand, kein Kleingeld.");
        ko(CARRYING_INSTRUCTION, "캐릭터가 몸에 지니거나 가방에 넣어 다니는 물건, 하나씩. 옷, 손에 든 무기, 잔돈은 아님.");
        it(CARRYING_INSTRUCTION, "Le cose che il personaggio porta addosso o nelle borse, una voce ciascuna. Non i vestiti, non l'arma a portata di mano, non gli spiccioli.");

        ja(INJURIES, "負傷");
        zh(INJURIES, "伤势");
        fr(INJURIES, "Blessures");
        es(INJURIES, "Heridas");
        de(INJURIES, "Verletzungen");
        ko(INJURIES, "부상");
        it(INJURIES, "Ferite");

        ja(INJURIES_INSTRUCTION, "傷、病気、続いている身体的な影響。治るまで1件ずつ。");
        zh(INJURIES_INSTRUCTION, "伤口、疾病和持续的身体影响，每项一条，直到痊愈。");
        fr(INJURIES_INSTRUCTION, "Blessures, maladies et effets physiques durables, une entrée chacun, jusqu'à guérison.");
        es(INJURIES_INSTRUCTION, "Heridas, enfermedades y efectos físicos duraderos, una entrada cada uno, hasta que se curen.");
        de(INJURIES_INSTRUCTION, "Wunden, Krankheiten und anhaltende körperliche Folgen, je ein Eintrag, bis sie heilen.");
        ko(INJURIES_INSTRUCTION, "상처, 질병, 지속되는 신체적 영향. 나을 때까지 하나씩.");
        it(INJURIES_INSTRUCTION, "Ferite, malattie ed effetti fisici duraturi, una voce ciascuno, finché non guariscono.");

        ja(MOOD, "気分");
        zh(MOOD, "情绪");
        fr(MOOD, "Humeur");
        es(MOOD, "Ánimo");
        de(MOOD, "Stimmung");
        ko(MOOD, "기분");
        it(MOOD, "Umore");

        ja(MOOD_INSTRUCTION, "キャラクターが今どう感じているか、短く。移ろうものなので、場面が別の気分を見せたら変える。");
        zh(MOOD_INSTRUCTION, "角色此刻的感受，用几个词。它会消退：场景显示出别的情绪时就更改。");
        fr(MOOD_INSTRUCTION, "Ce que ressent le personnage en ce moment, en quelques mots. Cela passe : changez-la quand la scène montre une autre humeur.");
        es(MOOD_INSTRUCTION, "Cómo se siente el personaje ahora, en pocas palabras. Se pasa: cámbialo cuando la escena muestre otro ánimo.");
        de(MOOD_INSTRUCTION, "Wie sich die Figur gerade fühlt, in wenigen Worten. Sie vergeht: ändere sie, wenn die Szene eine andere Stimmung zeigt.");
        ko(MOOD_INSTRUCTION, "캐릭터가 지금 느끼는 감정, 몇 단어로. 사라지는 것이니 장면이 다른 기분을 보이면 바꾸세요.");
        it(MOOD_INSTRUCTION, "Come si sente il personaggio adesso, in poche parole. Passa: cambialo quando la scena mostra un altro umore.");

        ja(PROCESSING, "処理");
        zh(PROCESSING, "处理");
        fr(PROCESSING, "Traitement");
        es(PROCESSING, "Procesamiento");
        de(PROCESSING, "Verarbeitung");
        ko(PROCESSING, "처리");
        it(PROCESSING, "Elaborazione");

        ja(PROCESSED_UP_TO_PART, "%d / %d パートまで処理済み");
        zh(PROCESSED_UP_TO_PART, "已处理到第 %d 部分（共 %d 部分）");
        fr(PROCESSED_UP_TO_PART, "Traité jusqu'à la partie %d sur %d");
        es(PROCESSED_UP_TO_PART, "Procesado hasta la parte %d de %d");
        de(PROCESSED_UP_TO_PART, "Verarbeitet bis Abschnitt %d von %d");
        ko(PROCESSED_UP_TO_PART, "파트 %d / %d까지 처리됨");
        it(PROCESSED_UP_TO_PART, "Elaborato fino alla parte %d di %d");

        ja(NOTHING_IS_PROCESSED_YET, "まだ何も処理されていません（%d パート）");
        zh(NOTHING_IS_PROCESSED_YET, "尚未处理任何内容（共 %d 部分）");
        fr(NOTHING_IS_PROCESSED_YET, "Rien n'est encore traité (%d parties)");
        es(NOTHING_IS_PROCESSED_YET, "Todavía no se ha procesado nada (%d partes)");
        de(NOTHING_IS_PROCESSED_YET, "Noch nichts verarbeitet (%d Abschnitte)");
        ko(NOTHING_IS_PROCESSED_YET, "아직 처리된 것이 없습니다 (파트 %d개)");
        it(NOTHING_IS_PROCESSED_YET, "Non è ancora elaborato nulla (%d parti)");

        ja(THE_BOOK_HAS_NO_PARTS_YET, "この本にはまだパートがありません");
        zh(THE_BOOK_HAS_NO_PARTS_YET, "这本书还没有任何部分");
        fr(THE_BOOK_HAS_NO_PARTS_YET, "Le livre n'a pas encore de parties");
        es(THE_BOOK_HAS_NO_PARTS_YET, "El libro todavía no tiene partes");
        de(THE_BOOK_HAS_NO_PARTS_YET, "Das Buch hat noch keine Abschnitte");
        ko(THE_BOOK_HAS_NO_PARTS_YET, "책에 아직 파트가 없습니다");
        it(THE_BOOK_HAS_NO_PARTS_YET, "Il libro non ha ancora parti");

        ja(PROCESS_NOW, "今すぐ処理");
        zh(PROCESS_NOW, "立即处理");
        fr(PROCESS_NOW, "Traiter maintenant");
        es(PROCESS_NOW, "Procesar ahora");
        de(PROCESS_NOW, "Jetzt verarbeiten");
        ko(PROCESS_NOW, "지금 처리");
        it(PROCESS_NOW, "Elabora ora");

        ja(PROCESS_NEXT_PART, "次のパートを処理");
        zh(PROCESS_NEXT_PART, "处理下一部分");
        fr(PROCESS_NEXT_PART, "Traiter la partie suivante");
        es(PROCESS_NEXT_PART, "Procesar la parte siguiente");
        de(PROCESS_NEXT_PART, "Nächsten Abschnitt verarbeiten");
        ko(PROCESS_NEXT_PART, "다음 파트 처리");
        it(PROCESS_NEXT_PART, "Elabora la parte successiva");

        ja(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "まだ処理されていない最初のパートだけを読みます。一度に一歩ずつ進めたいときに使います");
        zh(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "只读取第一个尚未处理的部分，便于一步一步来");
        fr(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Lire seulement la première partie non encore traitée, pour avancer pas à pas");
        es(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Lee solo la primera parte que todavía no está procesada, para ir paso a paso");
        de(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Nur den ersten noch nicht verarbeiteten Abschnitt lesen, für einen Schritt nach dem anderen");
        ko(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "한 번에 한 단계씩, 아직 처리되지 않은 첫 파트만 읽습니다");
        it(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED, "Legge solo la prima parte non ancora elaborata, un passo alla volta");

        ja(START_POINT, "開始位置");
        zh(START_POINT, "起点");
        fr(START_POINT, "Point de départ");
        es(START_POINT, "Punto de inicio");
        de(START_POINT, "Startpunkt");
        ko(START_POINT, "시작 지점");
        it(START_POINT, "Punto di partenza");

        ja(START_OF_THE_BOOK, "本の最初");
        zh(START_OF_THE_BOOK, "书的开头");
        fr(START_OF_THE_BOOK, "Début du livre");
        es(START_OF_THE_BOOK, "Inicio del libro");
        de(START_OF_THE_BOOK, "Anfang des Buchs");
        ko(START_OF_THE_BOOK, "책의 시작");
        it(START_OF_THE_BOOK, "Inizio del libro");

        ja(PARTS_BEFORE_THE_START_POINT, "これより前のパートは処理されません");
        zh(PARTS_BEFORE_THE_START_POINT, "此前的部分永远不会被处理");
        fr(PARTS_BEFORE_THE_START_POINT, "Les parties d'avant ne sont jamais traitées");
        es(PARTS_BEFORE_THE_START_POINT, "Las partes anteriores nunca se procesan");
        de(PARTS_BEFORE_THE_START_POINT, "Abschnitte davor werden nie verarbeitet");
        ko(PARTS_BEFORE_THE_START_POINT, "그 앞의 파트는 처리되지 않습니다");
        it(PARTS_BEFORE_THE_START_POINT, "Le parti prima di esso non vengono mai elaborate");

        ja(REPROCESS_FROM_HERE, "ここから再処理");
        zh(REPROCESS_FROM_HERE, "从此处重新处理");
        fr(REPROCESS_FROM_HERE, "Retraiter à partir d'ici");
        es(REPROCESS_FROM_HERE, "Volver a procesar desde aquí");
        de(REPROCESS_FROM_HERE, "Ab hier neu verarbeiten");
        ko(REPROCESS_FROM_HERE, "여기서부터 다시 처리");
        it(REPROCESS_FROM_HERE, "Rielabora da qui");

        ja(PART_N, "パート %d");
        zh(PART_N, "第 %d 部分");
        fr(PART_N, "Partie %d");
        es(PART_N, "Parte %d");
        de(PART_N, "Abschnitt %d");
        ko(PART_N, "파트 %d");
        it(PART_N, "Parte %d");

        ja(PROCESSING_THE_STORY, "物語を処理中");
        zh(PROCESSING_THE_STORY, "正在处理故事");
        fr(PROCESSING_THE_STORY, "Traitement de l'histoire");
        es(PROCESSING_THE_STORY, "Procesando la historia");
        de(PROCESSING_THE_STORY, "Die Geschichte wird verarbeitet");
        ko(PROCESSING_THE_STORY, "이야기를 처리하는 중");
        it(PROCESSING_THE_STORY, "Elaborazione della storia");

        ja(CHANGES_TO_REVIEW, "確認待ちの変更 %d 件");
        zh(CHANGES_TO_REVIEW, "%d 项更改待审阅");
        fr(CHANGES_TO_REVIEW, "%d modifications à examiner");
        es(CHANGES_TO_REVIEW, "%d cambios que revisar");
        de(CHANGES_TO_REVIEW, "%d Änderungen zu prüfen");
        ko(CHANGES_TO_REVIEW, "검토할 변경 %d건");
        it(CHANGES_TO_REVIEW, "%d modifiche da rivedere");

        ja(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "確認中に物語が変更されました。もう一度処理してください");
        zh(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "您审阅期间故事发生了变化，请重新处理");
        fr(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "L'histoire a changé pendant votre examen des modifications ; traitez de nouveau");
        es(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "La historia cambió mientras revisabas los cambios; procesa de nuevo");
        de(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "Die Geschichte hat sich geändert, während du die Änderungen geprüft hast; verarbeite erneut");
        ko(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "변경 사항을 검토하는 동안 이야기가 변경되었습니다. 다시 처리하세요");
        it(THE_STORY_CHANGED_WHILE_YOU_REVIEWED, "La storia è cambiata mentre rivedevi le modifiche; elabora di nuovo");

        ja(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "前回の実行：モデルの考え");
        zh(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "上次运行：模型的思考");
        fr(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Dernier traitement : ce que le modèle a pensé");
        es(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Última ejecución: lo que pensó el modelo");
        de(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Letzter Lauf: was das Modell gedacht hat");
        ko(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "마지막 실행: 모델의 생각");
        it(LAST_RUN_WHAT_THE_MODEL_THOUGHT, "Ultima esecuzione: cosa ha pensato il modello");

        ja(PROCESS_NEW_PARTS_AUTOMATICALLY, "新しいパートを自動で処理");
        zh(PROCESS_NEW_PARTS_AUTOMATICALLY, "自动处理新的部分");
        fr(PROCESS_NEW_PARTS_AUTOMATICALLY, "Traiter automatiquement les nouvelles parties");
        es(PROCESS_NEW_PARTS_AUTOMATICALLY, "Procesar las partes nuevas automáticamente");
        de(PROCESS_NEW_PARTS_AUTOMATICALLY, "Neue Abschnitte automatisch verarbeiten");
        ko(PROCESS_NEW_PARTS_AUTOMATICALLY, "새 파트를 자동으로 처리");
        it(PROCESS_NEW_PARTS_AUTOMATICALLY, "Elabora automaticamente le nuove parti");

        ja(ASK_BEFORE_APPLYING, "適用前に確認する");
        zh(ASK_BEFORE_APPLYING, "应用前先确认");
        fr(ASK_BEFORE_APPLYING, "Demander avant d'appliquer");
        es(ASK_BEFORE_APPLYING, "Preguntar antes de aplicar");
        de(ASK_BEFORE_APPLYING, "Vor dem Anwenden fragen");
        ko(ASK_BEFORE_APPLYING, "적용 전에 묻기");
        it(ASK_BEFORE_APPLYING, "Chiedi prima di applicare");

        ja(PARTS_PER_RUN, "1回あたりのパート数");
        zh(PARTS_PER_RUN, "每轮处理的部分数");
        fr(PARTS_PER_RUN, "Parties par passe");
        es(PARTS_PER_RUN, "Partes por ejecución");
        de(PARTS_PER_RUN, "Abschnitte je Lauf");
        ko(PARTS_PER_RUN, "실행당 파트 수");
        it(PARTS_PER_RUN, "Parti per esecuzione");

        ja(LOREBOOK_FOR_THE_MODEL, "モデルに渡すロアブック");
        zh(LOREBOOK_FOR_THE_MODEL, "提供给模型的设定集");
        fr(LOREBOOK_FOR_THE_MODEL, "Lorebook pour le modèle");
        es(LOREBOOK_FOR_THE_MODEL, "Lorebook para el modelo");
        de(LOREBOOK_FOR_THE_MODEL, "Lorebook für das Modell");
        ko(LOREBOOK_FOR_THE_MODEL, "모델용 로어북");
        it(LOREBOOK_FOR_THE_MODEL, "Lorebook per il modello");

        ja(ENTRIES_THE_PARTS_ACTIVATE, "パートが有効にするエントリ");
        zh(ENTRIES_THE_PARTS_ACTIVATE, "这些部分触发的条目");
        fr(ENTRIES_THE_PARTS_ACTIVATE, "Entrées activées par les parties");
        es(ENTRIES_THE_PARTS_ACTIVATE, "Entradas que activan las partes");
        de(ENTRIES_THE_PARTS_ACTIVATE, "Einträge, die die Abschnitte aktivieren");
        ko(ENTRIES_THE_PARTS_ACTIVATE, "파트가 활성화하는 항목");
        it(ENTRIES_THE_PARTS_ACTIVATE, "Voci che le parti attivano");

        ja(THE_WHOLE_LOREBOOK, "ロアブック全体");
        zh(THE_WHOLE_LOREBOOK, "整个设定集");
        fr(THE_WHOLE_LOREBOOK, "Tout le lorebook");
        es(THE_WHOLE_LOREBOOK, "Todo el lorebook");
        de(THE_WHOLE_LOREBOOK, "Das gesamte Lorebook");
        ko(THE_WHOLE_LOREBOOK, "로어북 전체");
        it(THE_WHOLE_LOREBOOK, "L'intero lorebook");

        ja(NO_LOREBOOK, "ロアブックなし");
        zh(NO_LOREBOOK, "不使用设定集");
        fr(NO_LOREBOOK, "Pas de lorebook");
        es(NO_LOREBOOK, "Sin lorebook");
        de(NO_LOREBOOK, "Kein Lorebook");
        ko(NO_LOREBOOK, "로어북 없음");
        it(NO_LOREBOOK, "Nessun lorebook");

        ja(MODEL_FOR_THE_EXTRACTION, "読み取りに使うモデル");
        zh(MODEL_FOR_THE_EXTRACTION, "用于读取的模型");
        fr(MODEL_FOR_THE_EXTRACTION, "Modèle pour l'extraction");
        es(MODEL_FOR_THE_EXTRACTION, "Modelo para la extracción");
        de(MODEL_FOR_THE_EXTRACTION, "Modell für die Extraktion");
        ko(MODEL_FOR_THE_EXTRACTION, "추출용 모델");
        it(MODEL_FOR_THE_EXTRACTION, "Modello per l'estrazione");

        ja(PROTOCOL_FOR_THE_EXTRACTION, "読み取りに使うプロトコル");
        zh(PROTOCOL_FOR_THE_EXTRACTION, "用于读取的协议");
        fr(PROTOCOL_FOR_THE_EXTRACTION, "Protocole pour l'extraction");
        es(PROTOCOL_FOR_THE_EXTRACTION, "Protocolo para la extracción");
        de(PROTOCOL_FOR_THE_EXTRACTION, "Protokoll für die Extraktion");
        ko(PROTOCOL_FOR_THE_EXTRACTION, "추출용 프로토콜");
        it(PROTOCOL_FOR_THE_EXTRACTION, "Protocollo per l'estrazione");

        ja(THE_MODEL_OF_THE_BOOK, "本のモデル");
        zh(THE_MODEL_OF_THE_BOOK, "本书的模型");
        fr(THE_MODEL_OF_THE_BOOK, "Le modèle du livre");
        es(THE_MODEL_OF_THE_BOOK, "El modelo del libro");
        de(THE_MODEL_OF_THE_BOOK, "Das Modell des Buchs");
        ko(THE_MODEL_OF_THE_BOOK, "책의 모델");
        it(THE_MODEL_OF_THE_BOOK, "Il modello del libro");

        ja(REVIEW_THE_CHANGES_OF_RUN, "変更の確認（%d 回目）");
        zh(REVIEW_THE_CHANGES_OF_RUN, "审阅更改（第 %d 轮）");
        fr(REVIEW_THE_CHANGES_OF_RUN, "Examiner les modifications (passe %d)");
        es(REVIEW_THE_CHANGES_OF_RUN, "Revisar los cambios (ejecución %d)");
        de(REVIEW_THE_CHANGES_OF_RUN, "Die Änderungen prüfen (Lauf %d)");
        ko(REVIEW_THE_CHANGES_OF_RUN, "변경 사항 검토 (실행 %d)");
        it(REVIEW_THE_CHANGES_OF_RUN, "Rivedi le modifiche (esecuzione %d)");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajout");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");
        it(ADD, "Aggiungi");

        ja(READ_THE_STORY_FOR_THE_STATE, "状態トラッカー：物語を読む");
        zh(READ_THE_STORY_FOR_THE_STATE, "状态追踪器：读取故事");
        fr(READ_THE_STORY_FOR_THE_STATE, "Suivi d'état : lire l'histoire");
        es(READ_THE_STORY_FOR_THE_STATE, "Seguimiento de estado: leer la historia");
        de(READ_THE_STORY_FOR_THE_STATE, "Zustandsverfolgung: die Geschichte lesen");
        ko(READ_THE_STORY_FOR_THE_STATE, "상태 추적기: 이야기 읽기");
        it(READ_THE_STORY_FOR_THE_STATE, "Tracciamento dello stato: leggere la storia");

        ja(DONE_PARTS_CHANGES_APPLIED, "完了：%d パートを読み、%d 件の変更を適用");
        zh(DONE_PARTS_CHANGES_APPLIED, "完成：读取了 %d 个部分，应用了 %d 项更改");
        fr(DONE_PARTS_CHANGES_APPLIED, "Terminé : %d parties lues, %d changements appliqués");
        es(DONE_PARTS_CHANGES_APPLIED, "Hecho: %d partes leídas, %d cambios aplicados");
        de(DONE_PARTS_CHANGES_APPLIED, "Fertig: %d Teile gelesen, %d Änderungen übernommen");
        ko(DONE_PARTS_CHANGES_APPLIED, "완료: 파트 %d개를 읽고 변경 %d개를 적용함");
        it(DONE_PARTS_CHANGES_APPLIED, "Fatto: %d parti lette, %d modifiche applicate");

        ja(STOPPED_PARTS_CHANGES_APPLIED, "停止：%d パートを読み、%d 件の変更を適用");
        zh(STOPPED_PARTS_CHANGES_APPLIED, "已停止：读取了 %d 个部分，应用了 %d 项更改");
        fr(STOPPED_PARTS_CHANGES_APPLIED, "Arrêté : %d parties lues, %d changements appliqués");
        es(STOPPED_PARTS_CHANGES_APPLIED, "Detenido: %d partes leídas, %d cambios aplicados");
        de(STOPPED_PARTS_CHANGES_APPLIED, "Angehalten: %d Teile gelesen, %d Änderungen übernommen");
        ko(STOPPED_PARTS_CHANGES_APPLIED, "중지됨: 파트 %d개를 읽고 변경 %d개를 적용함");
        it(STOPPED_PARTS_CHANGES_APPLIED, "Fermato: %d parti lette, %d modifiche applicate");

        ja(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "チェックを外した変更は適用されません。");
        zh(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "取消勾选的更改不会被应用。");
        fr(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "Un changement que vous décochez n'est pas appliqué.");
        es(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "Un cambio que desmarques no se aplica.");
        de(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "Eine Änderung, deren Haken du entfernst, wird nicht übernommen.");
        ko(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "체크를 해제한 변경은 적용되지 않습니다.");
        it(AN_UNCHECKED_CHANGE_IS_NOT_APPLIED, "Una modifica che deselezioni non viene applicata.");

        ja(SET, "設定");
        zh(SET, "设置");
        fr(SET, "Définir");
        es(SET, "Fijar");
        de(SET, "Setzen");
        ko(SET, "설정");
        it(SET, "Imposta");

        ja(CLEAR, "なしに");
        zh(CLEAR, "清空");
        fr(CLEAR, "Vider");
        es(CLEAR, "Vaciar");
        de(CLEAR, "Leeren");
        ko(CLEAR, "비우기");
        it(CLEAR, "Svuota");

        ja(ASK_THE_MODEL_WHAT_CHANGED, "まだ処理していないパートで、追跡キャラクターの状態の何が変わったかをモデルに尋ねる");
        zh(ASK_THE_MODEL_WHAT_CHANGED, "询问模型：在尚未处理的部分中，被追踪角色的状态有哪些变化");
        fr(ASK_THE_MODEL_WHAT_CHANGED, "Demander au modèle ce qui a changé dans l'état des personnages suivis dans les parties pas encore traitées");
        es(ASK_THE_MODEL_WHAT_CHANGED, "Preguntar al modelo qué cambió en el estado de los personajes seguidos en las partes aún no procesadas");
        de(ASK_THE_MODEL_WHAT_CHANGED, "Das Modell fragen, was sich am Zustand der verfolgten Figuren in den noch nicht verarbeiteten Teilen geändert hat");
        ko(ASK_THE_MODEL_WHAT_CHANGED, "아직 처리하지 않은 파트에서 추적 캐릭터의 상태가 무엇이 바뀌었는지 모델에게 묻기");
        it(ASK_THE_MODEL_WHAT_CHANGED, "Chiedere al modello cosa è cambiato nello stato dei personaggi tracciati nelle parti non ancora elaborate");

        ja(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "パートが確定すると、モデルが状態の変化を読み取ります（遅くとも次の生成の前に）");
        zh(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "部分定稿后，模型会读取其中的状态变化（最迟在下一次生成之前）");
        fr(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "Quand une partie est définitive, le modèle y lit les changements d'état (au plus tard avant la génération suivante)");
        es(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "Cuando una parte es definitiva, el modelo lee en ella los cambios de estado (a más tardar antes de la siguiente generación)");
        de(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "Wenn ein Teil feststeht, liest das Modell darin die Änderungen des Zustands (spätestens vor der nächsten Generierung)");
        ko(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "파트가 확정되면 모델이 상태 변화를 읽습니다(늦어도 다음 생성 전에)");
        it(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT, "Quando una parte è definitiva, il modello vi legge i cambiamenti di stato (al più tardi prima della generazione successiva)");

        ja(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "適用する前に、モデルが見つけた内容をレビューのために表示します。オフ：すぐに適用され、値の履歴にどのパートから来たかが表示されます。");
        zh(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "在应用之前先显示模型发现的内容供审阅。关：立即应用；值的历史会显示它来自哪个部分。");
        fr(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "Montrer ce que le modèle a trouvé pour relecture avant d'appliquer quoi que ce soit. Désactivé : appliqué tout de suite ; l'historique d'une valeur montre la partie d'où elle vient.");
        es(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "Mostrar lo que encontró el modelo para revisarlo antes de aplicar nada. Desactivado: se aplica en el acto; el historial de un valor muestra la parte de la que viene.");
        de(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "Zeigen, was das Modell gefunden hat, und erst nach der Prüfung übernehmen. Aus: sofort übernommen; der Verlauf eines Werts zeigt den Teil, aus dem er stammt.");
        ko(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "적용하기 전에 모델이 찾은 내용을 검토용으로 보여 줍니다. 꺼짐: 바로 적용되며, 값의 기록에 어느 파트에서 왔는지 나옵니다.");
        it(SHOW_THE_CHANGES_FOR_REVIEW_FIRST, "Mostrare ciò che il modello ha trovato per la revisione prima di applicare qualcosa. Disattivo: viene applicato subito; la cronologia di un valore mostra la parte da cui viene.");

        ja(READ_N_PARTS_WITH_THE_MODEL, "%d パートを %d 回のリクエストでモデルに読ませますか？物語の本文だけで約 %d トークンあり、指示・ロアブック・キャラクターの状態は毎回のリクエストに加えて送られます。");
        zh(READ_N_PARTS_WITH_THE_MODEL, "要用 %d 次请求让模型读取 %d 个部分吗？仅故事正文就约有 %d 个令牌，指令、设定集和角色状态会在每次请求中额外发送。");
        fr(READ_N_PARTS_WITH_THE_MODEL, "Lire %d parties avec le modèle en %d requêtes ? Le texte de l'histoire seul fait environ %d jetons ; les instructions, le lorebook et l'état des personnages sont envoyés en plus à chaque requête.");
        es(READ_N_PARTS_WITH_THE_MODEL, "¿Leer %d partes con el modelo en %d peticiones? Solo el texto de la historia son unos %d tokens; las instrucciones, el lorebook y el estado de los personajes se envían además con cada petición.");
        de(READ_N_PARTS_WITH_THE_MODEL, "%d Teile mit dem Modell in %d Anfragen lesen? Allein der Text der Geschichte sind etwa %d Tokens; die Anweisungen, das Lorebook und der Zustand der Figuren werden bei jeder Anfrage zusätzlich gesendet.");
        ko(READ_N_PARTS_WITH_THE_MODEL, "모델이 %d개 파트를 %d번의 요청으로 읽게 할까요? 이야기 본문만 약 %d 토큰이며, 지시·로어북·캐릭터 상태는 요청마다 추가로 보내집니다.");
        it(READ_N_PARTS_WITH_THE_MODEL, "Leggere %d parti con il modello in %d richieste? Il solo testo della storia è di circa %d token; le istruzioni, il lorebook e lo stato dei personaggi vengono inviati in più a ogni richiesta.");

        ja(REPROCESS_FROM_PART_N, "パート %d から再処理しますか？そのパート以降から読み取った内容は取り消され、もう一度読まれます。");
        zh(REPROCESS_FROM_PART_N, "要从第 %d 部分重新处理吗？从该部分及之后读取的内容会被撤回并重新读取。");
        fr(REPROCESS_FROM_PART_N, "Retraiter à partir de la partie %d ? Ce qui a été lu dans celle-ci et dans les suivantes est retiré et relu.");
        es(REPROCESS_FROM_PART_N, "¿Volver a procesar desde la parte %d? Lo leído en ella y en las siguientes se deshace y se vuelve a leer.");
        de(REPROCESS_FROM_PART_N, "Ab Teil %d neu verarbeiten? Was in ihm und den späteren Teilen gelesen wurde, wird zurückgenommen und neu gelesen.");
        ko(REPROCESS_FROM_PART_N, "파트 %d부터 다시 처리할까요? 그 파트와 이후 파트에서 읽은 내용은 취소되고 다시 읽힙니다.");
        it(REPROCESS_FROM_PART_N, "Rielaborare dalla parte %d? Ciò che è stato letto in essa e nelle parti successive viene annullato e riletto.");

        ja(THE_STORY_IS_BEING_PROCESSED_ALREADY, "物語はすでに処理中です");
        zh(THE_STORY_IS_BEING_PROCESSED_ALREADY, "故事已在处理中");
        fr(THE_STORY_IS_BEING_PROCESSED_ALREADY, "L'histoire est déjà en cours de traitement");
        es(THE_STORY_IS_BEING_PROCESSED_ALREADY, "La historia ya se está procesando");
        de(THE_STORY_IS_BEING_PROCESSED_ALREADY, "Die Geschichte wird bereits verarbeitet");
        ko(THE_STORY_IS_BEING_PROCESSED_ALREADY, "이야기를 이미 처리하고 있습니다");
        it(THE_STORY_IS_BEING_PROCESSED_ALREADY, "La storia è già in elaborazione");

        ja(READ_FROM_THE_STORY_IN_PART_N, "パート %d で物語から読み取った値。そのパートからの再処理で取り消されます");
        zh(READ_FROM_THE_STORY_IN_PART_N, "在第 %d 部分从故事中读取；从该部分重新处理会撤回它");
        fr(READ_FROM_THE_STORY_IN_PART_N, "Lu dans l'histoire, partie %d ; un retraitement depuis cette partie le retire");
        es(READ_FROM_THE_STORY_IN_PART_N, "Leído de la historia en la parte %d; volver a procesar desde esa parte lo deshace");
        de(READ_FROM_THE_STORY_IN_PART_N, "Aus der Geschichte gelesen in Teil %d; eine Neuverarbeitung ab diesem Teil nimmt es zurück");
        ko(READ_FROM_THE_STORY_IN_PART_N, "파트 %d에서 이야기로부터 읽은 값. 그 파트부터 다시 처리하면 취소됩니다");
        it(READ_FROM_THE_STORY_IN_PART_N, "Letto dalla storia nella parte %d; una rielaborazione da quella parte lo annulla");

        ja(PROCESSED_PARTS, "処理済みのパート（%d）");
        zh(PROCESSED_PARTS, "已处理的部分（%d）");
        fr(PROCESSED_PARTS, "Parties traitées (%d)");
        es(PROCESSED_PARTS, "Partes procesadas (%d)");
        de(PROCESSED_PARTS, "Verarbeitete Abschnitte (%d)");
        ko(PROCESSED_PARTS, "처리된 파트 (%d)");
        it(PROCESSED_PARTS, "Parti elaborate (%d)");

        ja(CHANGED_SINCE_PROCESSED, "処理後に変更されました");
        zh(CHANGED_SINCE_PROCESSED, "处理后已更改");
        fr(CHANGED_SINCE_PROCESSED, "Modifiée depuis le traitement");
        es(CHANGED_SINCE_PROCESSED, "Modificada desde que se procesó");
        de(CHANGED_SINCE_PROCESSED, "Seit der Verarbeitung geändert");
        ko(CHANGED_SINCE_PROCESSED, "처리 후 변경됨");
        it(CHANGED_SINCE_PROCESSED, "Modificato dopo l'elaborazione");

        ja(NOTHING_NEW_TO_PROCESS, "処理するものはありません");
        zh(NOTHING_NEW_TO_PROCESS, "没有需要处理的内容");
        fr(NOTHING_NEW_TO_PROCESS, "Rien de nouveau à traiter");
        es(NOTHING_NEW_TO_PROCESS, "No hay nada nuevo que procesar");
        de(NOTHING_NEW_TO_PROCESS, "Nichts Neues zu verarbeiten");
        ko(NOTHING_NEW_TO_PROCESS, "처리할 새 파트가 없습니다");
        it(NOTHING_NEW_TO_PROCESS, "Niente di nuovo da elaborare");

        ja(ENDED_BY_THE_STORY_IN_PART_N, "パート %d で物語により終了。そのパートから再処理すると元に戻ります");
        zh(ENDED_BY_THE_STORY_IN_PART_N, "在第 %d 部分被故事结束；从该部分重新处理会将其恢复");
        fr(ENDED_BY_THE_STORY_IN_PART_N, "Terminé par l'histoire dans la partie %d ; un retraitement depuis cette partie le rétablit");
        es(ENDED_BY_THE_STORY_IN_PART_N, "Terminado por la historia en la parte %d; reprocesar desde esa parte lo restablece");
        de(ENDED_BY_THE_STORY_IN_PART_N, "In Teil %d von der Geschichte beendet; eine Neuverarbeitung ab diesem Teil stellt es wieder her");
        ko(ENDED_BY_THE_STORY_IN_PART_N, "파트 %d에서 이야기에 의해 끝남. 그 파트부터 다시 처리하면 되돌아옵니다");
        it(ENDED_BY_THE_STORY_IN_PART_N, "Terminato dalla storia nella parte %d; una rielaborazione da quella parte lo ripristina");

        ja(TIDY_THE_STATE, "状態を整理");
        zh(TIDY_THE_STATE, "整理状态");
        fr(TIDY_THE_STATE, "Ranger l'état");
        es(TIDY_THE_STATE, "Ordenar el estado");
        de(TIDY_THE_STATE, "Zustand aufräumen");
        ko(TIDY_THE_STATE, "상태 정리");
        it(TIDY_THE_STATE, "Riordina lo stato");

        ja(ASK_THE_MODEL_WHICH_VALUES_FADED, "直近のパートをもとに、薄れた値・変化した値・重複した値をモデルに尋ねます。ロックされた値はそのままです");
        zh(ASK_THE_MODEL_WHICH_VALUES_FADED, "根据最近的部分，询问模型哪些当前值已消退、已变化或重复列出；已锁定的值保持不变");
        fr(ASK_THE_MODEL_WHICH_VALUES_FADED, "Demander au modèle, d'après les dernières parties, quelles valeurs actuelles se sont estompées, ont évolué ou figurent deux fois ; les valeurs verrouillées restent");
        es(ASK_THE_MODEL_WHICH_VALUES_FADED, "Preguntar al modelo, a partir de las últimas partes, qué valores actuales se desvanecieron, cambiaron o aparecen dos veces; los valores bloqueados se quedan");
        de(ASK_THE_MODEL_WHICH_VALUES_FADED, "Das Modell anhand der letzten Teile fragen, welche aktuellen Werte verblasst sind, sich weiterentwickelt haben oder doppelt stehen; gesperrte Werte bleiben");
        ko(ASK_THE_MODEL_WHICH_VALUES_FADED, "최근 파트를 바탕으로 사라졌거나 바뀌었거나 두 번 적힌 현재 값을 모델에게 묻습니다. 잠긴 값은 그대로 둡니다");
        it(ASK_THE_MODEL_WHICH_VALUES_FADED, "Chiedere al modello, in base alle ultime parti, quali valori attuali sono svaniti, sono cambiati o compaiono due volte; i valori bloccati restano");

        ja(TIDY_AUTOMATICALLY, "自動で整理する");
        zh(TIDY_AUTOMATICALLY, "自动整理");
        fr(TIDY_AUTOMATICALLY, "Ranger automatiquement");
        es(TIDY_AUTOMATICALLY, "Ordenar automáticamente");
        de(TIDY_AUTOMATICALLY, "Automatisch aufräumen");
        ko(TIDY_AUTOMATICALLY, "자동으로 정리");
        it(TIDY_AUTOMATICALLY, "Riordina automaticamente");

        ja(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "確認なしで変更を適用する実行の最後に、前回の整理から十分なパートを読んだ場合に整理します");
        zh(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "在不经确认就应用更改的运行结束时，若自上次整理后读取了足够多的部分，则自动整理");
        fr(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "À la fin d'un traitement qui applique ses modifications sans demander, quand assez de parties ont été lues depuis le dernier rangement");
        es(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "Al final de una ejecución que aplica sus cambios sin preguntar, cuando se han leído suficientes partes desde la última ordenación");
        de(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "Am Ende eines Laufs, der seine Änderungen ohne Rückfrage anwendet, wenn seit dem letzten Aufräumen genug Teile gelesen wurden");
        ko(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "묻지 않고 변경을 적용하는 실행이 끝날 때, 마지막 정리 이후 충분한 파트를 읽었으면 실행합니다");
        it(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES, "Alla fine di un'esecuzione che applica le sue modifiche senza chiedere, quando sono state lette abbastanza parti dall'ultimo riordino");

        ja(TIDY_AFTER_N_PARTS, "整理するまでのパート数");
        zh(TIDY_AFTER_N_PARTS, "每隔多少部分整理一次");
        fr(TIDY_AFTER_N_PARTS, "Ranger après ce nombre de parties");
        es(TIDY_AFTER_N_PARTS, "Ordenar tras esta cantidad de partes");
        de(TIDY_AFTER_N_PARTS, "Aufräumen nach so vielen Teilen");
        ko(TIDY_AFTER_N_PARTS, "이 파트 수 후에 정리");
        it(TIDY_AFTER_N_PARTS, "Riordina dopo questo numero di parti");

        ja(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "整理では、処理済みの直近のパートもこの数だけ読みます");
        zh(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "整理时也会读取这么多最近处理过的部分");
        fr(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "Le rangement lit aussi ce nombre de dernières parties traitées");
        es(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "La ordenación también lee esta cantidad de las últimas partes procesadas");
        de(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "Das Aufräumen liest auch so viele der zuletzt verarbeiteten Teile");
        ko(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "정리는 마지막으로 처리된 파트도 이만큼 읽습니다");
        it(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS, "Il riordino legge anche questo numero di ultime parti elaborate");

        ja(REVIEW_THE_TIDY, "整理を確認");
        zh(REVIEW_THE_TIDY, "审阅整理");
        fr(REVIEW_THE_TIDY, "Vérifier le rangement");
        es(REVIEW_THE_TIDY, "Revisar la ordenación");
        de(REVIEW_THE_TIDY, "Aufräumen prüfen");
        ko(REVIEW_THE_TIDY, "정리 검토");
        it(REVIEW_THE_TIDY, "Rivedi il riordino");

        ja(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "チェックを外した変更では値はそのまま残ります");
        zh(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "未勾选的更改会让该值保持原样");
        fr(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "Une modification décochée laisse la valeur telle quelle");
        es(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "Un cambio sin marcar deja el valor como está");
        de(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "Eine nicht angehakte Änderung lässt den Wert, wie er ist");
        ko(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "체크하지 않은 변경은 값을 그대로 둡니다");
        it(AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE, "Una modifica non selezionata lascia il valore com'è");

        ja(REPLACE, "置き換え");
        zh(REPLACE, "替换");
        fr(REPLACE, "Remplacer");
        es(REPLACE, "Reemplazar");
        de(REPLACE, "Ersetzen");
        ko(REPLACE, "교체");
        it(REPLACE, "Sostituisci");

        ja(TIDIED, "整理済み");
        zh(TIDIED, "已整理");
        fr(TIDIED, "rangé");
        es(TIDIED, "ordenado");
        de(TIDIED, "aufgeräumt");
        ko(TIDIED, "정리됨");
        it(TIDIED, "riordinato");

        ja(TIDIED_AFTER_PART_N, "パート %d の後に整理されました。そのパートから再処理すると元に戻ります");
        zh(TIDIED_AFTER_PART_N, "在第 %d 部分之后被整理；从该部分重新处理会将其恢复");
        fr(TIDIED_AFTER_PART_N, "Rangé après la partie %d ; un retraitement depuis cette partie le rétablit");
        es(TIDIED_AFTER_PART_N, "Ordenado después de la parte %d; reprocesar desde esa parte lo restablece");
        de(TIDIED_AFTER_PART_N, "Nach Teil %d aufgeräumt; eine Neuverarbeitung ab diesem Teil stellt es wieder her");
        ko(TIDIED_AFTER_PART_N, "파트 %d 이후에 정리됨. 그 파트부터 다시 처리하면 되돌아옵니다");
        it(TIDIED_AFTER_PART_N, "Riordinato dopo la parte %d; una rielaborazione da quella parte lo ripristina");

        ja(TIDY_DONE, "整理: %d 件の値を変更しました");
        zh(TIDY_DONE, "整理：已更改 %d 个值");
        fr(TIDY_DONE, "Rangement : %d valeur(s) modifiée(s)");
        es(TIDY_DONE, "Ordenación: %d valor(es) cambiado(s)");
        de(TIDY_DONE, "Aufräumen: %d Wert(e) geändert");
        ko(TIDY_DONE, "정리: 값 %d개 변경");
        it(TIDY_DONE, "Riordino: %d valore/i modificato/i");

        ja(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "値はその後変更されたため、そのままにしました");
        zh(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "该值此后已被更改，未做任何改动");
        fr(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "La valeur a été modifiée depuis, elle n'a pas été touchée");
        es(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "El valor cambió después, no se tocó");
        de(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "Der Wert wurde seitdem geändert, er wurde nicht angetastet");
        ko(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "값이 그 후에 변경되어 건드리지 않았습니다");
        it(THE_VALUE_HAS_BEEN_CHANGED_SINCE, "Il valore è stato modificato nel frattempo, non è stato toccato");

        ja(BY_THE_TIDY_PART_N, "整理による・パート %d");
        zh(BY_THE_TIDY_PART_N, "由整理写入，第 %d 部分");
        fr(BY_THE_TIDY_PART_N, "par le rangement, partie %d");
        es(BY_THE_TIDY_PART_N, "por la ordenación, parte %d");
        de(BY_THE_TIDY_PART_N, "vom Aufräumen, Teil %d");
        ko(BY_THE_TIDY_PART_N, "정리에 의해, 파트 %d");
        it(BY_THE_TIDY_PART_N, "dal riordino, parte %d");

        ja(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "パート %d の後に整理が書いた値で、物語に書かれたものではありません。そのパートから再処理すると元に戻ります");
        zh(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "由整理在第 %d 部分之后写入，并非故事中所述；从该部分重新处理会将其撤回");
        fr(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "Écrit par le rangement après la partie %d, l'histoire ne le dit pas ; un retraitement depuis cette partie le retire");
        es(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "Escrito por la ordenación después de la parte %d, la historia no lo dice; reprocesar desde esa parte lo retira");
        de(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "Vom Aufräumen nach Teil %d geschrieben, nicht von der Geschichte genannt; eine Neuverarbeitung ab diesem Teil nimmt es zurück");
        ko(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "파트 %d 이후 정리가 쓴 값으로, 이야기에 나온 것이 아닙니다. 그 파트부터 다시 처리하면 되돌려집니다");
        it(WRITTEN_BY_THE_TIDY_AFTER_PART_N, "Scritto dal riordino dopo la parte %d, non detto dalla storia; una rielaborazione da quella parte lo ritira");

        ja(STORY_CLOCK, "ストーリー時計");
        zh(STORY_CLOCK, "故事时钟");
        fr(STORY_CLOCK, "Horloge de l'histoire");
        es(STORY_CLOCK, "Reloj de la historia");
        de(STORY_CLOCK, "Geschichtsuhr");
        ko(STORY_CLOCK, "이야기 시계");
        it(STORY_CLOCK, "Orologio della storia");

        ja(FOLLOW_THE_TIME_OF_THE_STORY, "モデルが物語の時間（日数、時間帯、日付）も追い、プロンプトに物語の時刻が入ります");
        zh(FOLLOW_THE_TIME_OF_THE_STORY, "模型也会跟踪故事的时间（天数、时段、日期），提示中会加入故事时间");
        fr(FOLLOW_THE_TIME_OF_THE_STORY, "Le modèle suit aussi le temps de l'histoire (jours, moment de la journée, dates) et le prompt reçoit l'heure de l'histoire");
        es(FOLLOW_THE_TIME_OF_THE_STORY, "El modelo también sigue el tiempo de la historia (días, momento del día, fechas) y el prompt recibe la hora de la historia");
        de(FOLLOW_THE_TIME_OF_THE_STORY, "Das Modell verfolgt auch die Zeit der Geschichte (Tage, Tageszeit, Daten), und der Prompt erhält die Geschichtszeit");
        ko(FOLLOW_THE_TIME_OF_THE_STORY, "모델이 이야기의 시간(날짜 수, 하루 중 때, 날짜)도 추적하고 프롬프트에 이야기 시간이 들어갑니다");
        it(FOLLOW_THE_TIME_OF_THE_STORY, "Il modello segue anche il tempo della storia (giorni, momento della giornata, date) e il prompt riceve l'ora della storia");

        ja(DAY_ONE_IS_THE_DAY_OF, "1日目の出来事");
        zh(DAY_ONE_IS_THE_DAY_OF, "第 1 天是哪件事的那天");
        fr(DAY_ONE_IS_THE_DAY_OF, "Le jour 1 est le jour de");
        es(DAY_ONE_IS_THE_DAY_OF, "El día 1 es el día de");
        de(DAY_ONE_IS_THE_DAY_OF, "Tag 1 ist der Tag von");
        ko(DAY_ONE_IS_THE_DAY_OF, "1일째는 이 사건의 날");
        it(DAY_ONE_IS_THE_DAY_OF, "Il giorno 1 è il giorno di");

        ja(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "出来事。例：「目覚め」なら物語の時刻は「目覚めから14日目」となります");
        zh(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "一个事件，例如「苏醒」：故事时间会显示为「苏醒后第 14 天」");
        fr(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "Un événement, par exemple « l'éveil » : l'heure de l'histoire se lit alors « jour 14 après l'éveil »");
        es(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "Un suceso, por ejemplo «el despertar»: la hora de la historia se lee entonces «día 14 después del despertar»");
        de(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "Ein Ereignis, zum Beispiel „das Erwachen“: Die Geschichtszeit lautet dann „Tag 14 nach dem Erwachen“");
        ko(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "사건. 예: \"깨어남\"이면 이야기 시간은 \"깨어남 후 14일째\"로 표시됩니다");
        it(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING, "Un evento, per esempio «il risveglio»: l'ora della storia diventa «giorno 14 dopo il risveglio»");

        ja(DAY_AT_THE_START_POINT, "開始位置の日");
        zh(DAY_AT_THE_START_POINT, "起点时的天数");
        fr(DAY_AT_THE_START_POINT, "Jour au point de départ");
        es(DAY_AT_THE_START_POINT, "Día en el punto de inicio");
        de(DAY_AT_THE_START_POINT, "Tag am Startpunkt");
        ko(DAY_AT_THE_START_POINT, "시작 지점의 날");
        it(DAY_AT_THE_START_POINT, "Giorno al punto di partenza");

        ja(TIME_OF_DAY, "時間帯");
        zh(TIME_OF_DAY, "时段");
        fr(TIME_OF_DAY, "Moment de la journée");
        es(TIME_OF_DAY, "Momento del día");
        de(TIME_OF_DAY, "Tageszeit");
        ko(TIME_OF_DAY, "하루 중 때");
        it(TIME_OF_DAY, "Momento della giornata");

        ja(DATE_AT_THE_START_POINT, "開始位置の日付");
        zh(DATE_AT_THE_START_POINT, "起点时的日期");
        fr(DATE_AT_THE_START_POINT, "Date au point de départ");
        es(DATE_AT_THE_START_POINT, "Fecha en el punto de inicio");
        de(DATE_AT_THE_START_POINT, "Datum am Startpunkt");
        ko(DATE_AT_THE_START_POINT, "시작 지점의 날짜");
        it(DATE_AT_THE_START_POINT, "Data al punto di partenza");

        ja(OPTIONAL_A_CALENDAR_DATE, "任意：日付を入れると時計は日付も数えます");
        zh(OPTIONAL_A_CALENDAR_DATE, "可选：填写日期后时钟也会计算日期");
        fr(OPTIONAL_A_CALENDAR_DATE, "Facultatif : avec une date, l'horloge compte aussi les dates");
        es(OPTIONAL_A_CALENDAR_DATE, "Opcional: con una fecha el reloj también cuenta fechas");
        de(OPTIONAL_A_CALENDAR_DATE, "Optional: Mit einem Datum zählt die Uhr auch Daten");
        ko(OPTIONAL_A_CALENDAR_DATE, "선택: 날짜를 넣으면 시계가 날짜도 셉니다");
        it(OPTIONAL_A_CALENDAR_DATE, "Facoltativo: con una data l'orologio conta anche le date");

        ja(STORY_TIME, "物語の時刻");
        zh(STORY_TIME, "故事时间");
        fr(STORY_TIME, "Heure de l'histoire");
        es(STORY_TIME, "Hora de la historia");
        de(STORY_TIME, "Geschichtszeit");
        ko(STORY_TIME, "이야기 시간");
        it(STORY_TIME, "Ora della storia");

        ja(STORY_TIME_S, "物語の時刻：%s");
        zh(STORY_TIME_S, "故事时间：%s");
        fr(STORY_TIME_S, "Heure de l'histoire : %s");
        es(STORY_TIME_S, "Hora de la historia: %s");
        de(STORY_TIME_S, "Geschichtszeit: %s");
        ko(STORY_TIME_S, "이야기 시간: %s");
        it(STORY_TIME_S, "Ora della storia: %s");

        ja(DAY_N, "%d日目");
        zh(DAY_N, "第 %d 天");
        fr(DAY_N, "Jour %d");
        es(DAY_N, "Día %d");
        de(DAY_N, "Tag %d");
        ko(DAY_N, "%d일째");
        it(DAY_N, "Giorno %d");

        ja(DAY_N_AFTER_S, "%d日目（%sから）");
        zh(DAY_N_AFTER_S, "第 %d 天（自%s起）");
        fr(DAY_N_AFTER_S, "Jour %d après %s");
        es(DAY_N_AFTER_S, "Día %d después de %s");
        de(DAY_N_AFTER_S, "Tag %d nach %s");
        ko(DAY_N_AFTER_S, "%d일째(%s 이후)");
        it(DAY_N_AFTER_S, "Giorno %d dopo %s");

        ja(ABOUT_S, "おおよそ：%s");
        zh(ABOUT_S, "大约：%s");
        fr(ABOUT_S, "environ : %s");
        es(ABOUT_S, "aproximadamente: %s");
        de(ABOUT_S, "ungefähr: %s");
        ko(ABOUT_S, "대략: %s");
        it(ABOUT_S, "circa: %s");

        ja(SET_THE_STORY_TIME, "物語の時刻を設定");
        zh(SET_THE_STORY_TIME, "设置故事时间");
        fr(SET_THE_STORY_TIME, "Régler l'heure de l'histoire");
        es(SET_THE_STORY_TIME, "Ajustar la hora de la historia");
        de(SET_THE_STORY_TIME, "Geschichtszeit festlegen");
        ko(SET_THE_STORY_TIME, "이야기 시간 설정");
        it(SET_THE_STORY_TIME, "Imposta l'ora della storia");

        ja(STORY_TIME_AFTER_PART_N, "パート %d の後の物語の時刻");
        zh(STORY_TIME_AFTER_PART_N, "第 %d 部分之后的故事时间");
        fr(STORY_TIME_AFTER_PART_N, "Heure de l'histoire après la partie %d");
        es(STORY_TIME_AFTER_PART_N, "Hora de la historia después de la parte %d");
        de(STORY_TIME_AFTER_PART_N, "Geschichtszeit nach Teil %d");
        ko(STORY_TIME_AFTER_PART_N, "파트 %d 이후의 이야기 시간");
        it(STORY_TIME_AFTER_PART_N, "Ora della storia dopo la parte %d");

        ja(A_TIME_SET_HERE_IS_KEPT, "ここで設定した時刻は固定点です。パートを読み直しても置き換えられず、時計はそこから数え続けます");
        zh(A_TIME_SET_HERE_IS_KEPT, "在此设置的时间是固定点：重新读取该部分不会替换它，时钟会从这里继续计算");
        fr(A_TIME_SET_HERE_IS_KEPT, "Une heure réglée ici est un point fixe : relire la partie ne la remplace pas, et l'horloge compte à partir d'elle");
        es(A_TIME_SET_HERE_IS_KEPT, "Una hora ajustada aquí es un punto fijo: volver a leer la parte no la reemplaza y el reloj sigue contando desde ella");
        de(A_TIME_SET_HERE_IS_KEPT, "Eine hier festgelegte Zeit ist ein Fixpunkt: Erneutes Lesen des Teils ersetzt sie nicht, und die Uhr zählt von ihr aus weiter");
        ko(A_TIME_SET_HERE_IS_KEPT, "여기서 설정한 시간은 고정점입니다. 파트를 다시 읽어도 바뀌지 않고 시계는 거기서부터 계속 셉니다");
        it(A_TIME_SET_HERE_IS_KEPT, "Un'ora impostata qui è un punto fisso: rileggere la parte non la sostituisce e l'orologio conta a partire da essa");

        ja(DAY, "日");
        zh(DAY, "天");
        fr(DAY, "Jour");
        es(DAY, "Día");
        de(DAY, "Tag");
        ko(DAY, "날");
        it(DAY, "Giorno");

        ja(DATE, "日付");
        zh(DATE, "日期");
        fr(DATE, "Date");
        es(DATE, "Fecha");
        de(DATE, "Datum");
        ko(DATE, "날짜");
        it(DATE, "Data");

        ja(REMOVE_THE_FIXED_POINT, "固定点を削除");
        zh(REMOVE_THE_FIXED_POINT, "移除固定点");
        fr(REMOVE_THE_FIXED_POINT, "Retirer le point fixe");
        es(REMOVE_THE_FIXED_POINT, "Quitar el punto fijo");
        de(REMOVE_THE_FIXED_POINT, "Fixpunkt entfernen");
        ko(REMOVE_THE_FIXED_POINT, "고정점 제거");
        it(REMOVE_THE_FIXED_POINT, "Rimuovi il punto fisso");

        ja(SET_BY_HAND, "手動設定");
        zh(SET_BY_HAND, "手动设置");
        fr(SET_BY_HAND, "réglé à la main");
        es(SET_BY_HAND, "ajustado a mano");
        de(SET_BY_HAND, "von Hand festgelegt");
        ko(SET_BY_HAND, "직접 설정");
        it(SET_BY_HAND, "impostato a mano");

        ja(FLASHBACK, "回想");
        zh(FLASHBACK, "闪回");
        fr(FLASHBACK, "Retour en arrière");
        es(FLASHBACK, "Flashback");
        de(FLASHBACK, "Rückblende");
        ko(FLASHBACK, "회상");
        it(FLASHBACK, "Flashback");

        ja(TIME_PASSES, "時間が経過");
        zh(TIME_PASSES, "时间流逝");
        fr(TIME_PASSES, "Le temps passe");
        es(TIME_PASSES, "Pasa el tiempo");
        de(TIME_PASSES, "Zeit vergeht");
        ko(TIME_PASSES, "시간 경과");
        it(TIME_PASSES, "Il tempo passa");

        ja(PLUS_N_DAYS, "+%d日");
        zh(PLUS_N_DAYS, "+%d 天");
        fr(PLUS_N_DAYS, "+%d jour(s)");
        es(PLUS_N_DAYS, "+%d día(s)");
        de(PLUS_N_DAYS, "+%d Tag(e)");
        ko(PLUS_N_DAYS, "+%d일");
        it(PLUS_N_DAYS, "+%d giorno/i");

        ja(THE_SAME_DAY, "同じ日");
        zh(THE_SAME_DAY, "同一天");
        fr(THE_SAME_DAY, "le même jour");
        es(THE_SAME_DAY, "el mismo día");
        de(THE_SAME_DAY, "derselbe Tag");
        ko(THE_SAME_DAY, "같은 날");
        it(THE_SAME_DAY, "lo stesso giorno");

        ja(THE_CLOCK_DOES_NOT_MOVE, "時計は進みません");
        zh(THE_CLOCK_DOES_NOT_MOVE, "时钟不动");
        fr(THE_CLOCK_DOES_NOT_MOVE, "l'horloge ne bouge pas");
        es(THE_CLOCK_DOES_NOT_MOVE, "el reloj no se mueve");
        de(THE_CLOCK_DOES_NOT_MOVE, "die Uhr bleibt stehen");
        ko(THE_CLOCK_DOES_NOT_MOVE, "시계가 움직이지 않습니다");
        it(THE_CLOCK_DOES_NOT_MOVE, "l'orologio non si muove");

        ja(APPROXIMATE, "概算");
        zh(APPROXIMATE, "大约");
        fr(APPROXIMATE, "approximatif");
        es(APPROXIMATE, "aproximado");
        de(APPROXIMATE, "ungefähr");
        ko(APPROXIMATE, "대략");
        it(APPROXIMATE, "approssimativo");

        ja(SET_ON_DAY_N, "%d日目");
        zh(SET_ON_DAY_N, "第 %d 天");
        fr(SET_ON_DAY_N, "jour %d");
        es(SET_ON_DAY_N, "día %d");
        de(SET_ON_DAY_N, "Tag %d");
        ko(SET_ON_DAY_N, "%d일째");
        it(SET_ON_DAY_N, "giorno %d");

        ja(SET_ON_DAY_N_OF_THE_STORY, "物語の%d日目に設定");
        zh(SET_ON_DAY_N_OF_THE_STORY, "设置于故事第 %d 天");
        fr(SET_ON_DAY_N_OF_THE_STORY, "Réglé le jour %d de l'histoire");
        es(SET_ON_DAY_N_OF_THE_STORY, "Fijado el día %d de la historia");
        de(SET_ON_DAY_N_OF_THE_STORY, "Am Tag %d der Geschichte gesetzt");
        ko(SET_ON_DAY_N_OF_THE_STORY, "이야기 %d일째에 설정됨");
        it(SET_ON_DAY_N_OF_THE_STORY, "Impostato il giorno %d della storia");

        ja(DAWN, "夜明け");
        zh(DAWN, "黎明");
        fr(DAWN, "aube");
        es(DAWN, "alba");
        de(DAWN, "Morgendämmerung");
        ko(DAWN, "새벽");
        it(DAWN, "alba");

        ja(MORNING, "朝");
        zh(MORNING, "早上");
        fr(MORNING, "matin");
        es(MORNING, "mañana");
        de(MORNING, "Morgen");
        ko(MORNING, "아침");
        it(MORNING, "mattina");

        ja(NOON, "正午");
        zh(NOON, "中午");
        fr(NOON, "midi");
        es(NOON, "mediodía");
        de(NOON, "Mittag");
        ko(NOON, "정오");
        it(NOON, "mezzogiorno");

        ja(AFTERNOON, "午後");
        zh(AFTERNOON, "下午");
        fr(AFTERNOON, "après-midi");
        es(AFTERNOON, "tarde");
        de(AFTERNOON, "Nachmittag");
        ko(AFTERNOON, "오후");
        it(AFTERNOON, "pomeriggio");

        ja(EVENING, "夕方");
        zh(EVENING, "傍晚");
        fr(EVENING, "soir");
        es(EVENING, "anochecer");
        de(EVENING, "Abend");
        ko(EVENING, "저녁");
        it(EVENING, "sera");

        ja(NIGHT, "夜");
        zh(NIGHT, "夜晚");
        fr(NIGHT, "nuit");
        es(NIGHT, "noche");
        de(NIGHT, "Nacht");
        ko(NIGHT, "밤");
        it(NIGHT, "notte");

        ja(IMPORT_FROM_THE_CHARACTER_TRACKER, "キャラクタートラッカーからインポート");
        zh(IMPORT_FROM_THE_CHARACTER_TRACKER, "从角色追踪器导入");
        fr(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importer depuis le suivi des personnages");
        es(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importar desde el Seguimiento de personajes");
        de(IMPORT_FROM_THE_CHARACTER_TRACKER, "Aus dem Figuren-Tracker importieren");
        ko(IMPORT_FROM_THE_CHARACTER_TRACKER, "캐릭터 추적기에서 가져오기");
        it(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importa dal Tracciatore dei personaggi");

        ja(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "インポートしたキャラクターはすべて、チェックしたカテゴリーを追跡します。マイカテゴリーは選ぶと本にコピーされます。");
        zh(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "每个导入的角色都会追踪勾选的类别。选择我的类别时，会将其复制到本书中。");
        fr(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "Chaque personnage importé suit les catégories cochées. Vos propres catégories sont copiées dans le livre quand vous les choisissez.");
        es(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "Cada personaje importado sigue las categorías marcadas. Tus propias categorías se copian en el libro cuando las eliges.");
        de(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "Jede importierte Figur verfolgt die angehakten Kategorien. Deine eigenen Kategorien werden ins Buch kopiert, wenn du sie wählst.");
        ko(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "가져온 모든 캐릭터가 체크한 카테고리를 추적합니다. 내 카테고리는 고르면 책에 복사됩니다.");
        it(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES, "Ogni personaggio importato traccia le categorie selezionate. Le tue categorie vengono copiate nel libro quando le scegli.");

        ja(N_CHARACTERS_NOW_TRACKED, "%d 人のキャラクターを追跡開始");
        zh(N_CHARACTERS_NOW_TRACKED, "已开始追踪 %d 个角色");
        fr(N_CHARACTERS_NOW_TRACKED, "%d personnage(s) désormais suivi(s)");
        es(N_CHARACTERS_NOW_TRACKED, "%d personaje(s) ahora seguido(s)");
        de(N_CHARACTERS_NOW_TRACKED, "%d Figur(en) werden jetzt verfolgt");
        ko(N_CHARACTERS_NOW_TRACKED, "%d명의 캐릭터를 추적합니다");
        it(N_CHARACTERS_NOW_TRACKED, "%d personaggio/i ora tracciato/i");

        ja(STATE_EXTRACTION_PROMPT, "状態の読み取りプロンプト");
        zh(STATE_EXTRACTION_PROMPT, "状态读取提示词");
        fr(STATE_EXTRACTION_PROMPT, "Prompt d'extraction de l'état");
        es(STATE_EXTRACTION_PROMPT, "Prompt de extracción del estado");
        de(STATE_EXTRACTION_PROMPT, "Prompt zur Zustandsextraktion");
        ko(STATE_EXTRACTION_PROMPT, "상태 추출 프롬프트");
        it(STATE_EXTRACTION_PROMPT, "Prompt di estrazione dello stato");

        ja(STATE_TIDY_PROMPT, "状態の整理プロンプト");
        zh(STATE_TIDY_PROMPT, "状态整理提示词");
        fr(STATE_TIDY_PROMPT, "Prompt de rangement de l'état");
        es(STATE_TIDY_PROMPT, "Prompt de ordenación del estado");
        de(STATE_TIDY_PROMPT, "Prompt zum Aufräumen des Zustands");
        ko(STATE_TIDY_PROMPT, "상태 정리 프롬프트");
        it(STATE_TIDY_PROMPT, "Prompt di riordino dello stato");

        ja(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "追跡中のキャラクターの状態で何が変わったかを物語から読み取る方法をモデルに伝えるシステムメッセージ。プラグインが読み取るため、JSON の回答形式は維持してください。本が物語の時間を追う場合、時計のルールがその後に追加されます。空 = 継承されたもの、なければ組み込みのテキスト。");
        zh(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "告诉模型如何从故事中读取被追踪角色状态变化的系统消息。请保留 JSON 回答格式，插件会读取它。如果本书追踪故事时间，时钟规则会附加在其后。留空 = 继承的设置，否则使用内置文本。");
        fr(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "Le message système qui indique au modèle comment lire l'histoire pour ce qui a changé dans l'état des personnages suivis. Gardez le format de réponse JSON, le plugin le lit. Quand le livre suit le temps de l'histoire, les règles de l'horloge sont ajoutées après lui. Vide = celui hérité, sinon le texte intégré.");
        es(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "El mensaje del sistema que le dice al modelo cómo leer la historia para ver qué cambió en el estado de los personajes seguidos. Conserva el formato de respuesta JSON, el plugin lo lee. Cuando el libro sigue el tiempo de la historia, las reglas del reloj se añaden después. Vacío = el heredado, si no el texto integrado.");
        de(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "Die Systemnachricht, die dem Modell sagt, wie es die Geschichte danach lesen soll, was sich am Zustand der verfolgten Figuren geändert hat. Behalte das JSON-Antwortformat bei, das Plugin liest es. Wenn das Buch der Zeit der Geschichte folgt, werden die Regeln der Uhr danach angefügt. Leer = der geerbte, sonst der eingebaute Text.");
        ko(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "추적 캐릭터의 상태에서 무엇이 바뀌었는지 모델이 이야기를 읽는 방법을 알려 주는 시스템 메시지. 플러그인이 읽으므로 JSON 응답 형식은 유지하세요. 책이 이야기의 시간을 따라가면 시계 규칙이 그 뒤에 덧붙습니다. 비워 두면 상속된 것, 없으면 내장 텍스트.");
        it(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL, "Il messaggio di sistema che dice al modello come leggere la storia per ciò che è cambiato nello stato dei personaggi tracciati. Mantieni il formato di risposta JSON, il plugin lo legge. Quando il libro segue il tempo della storia, le regole dell'orologio vengono aggiunte dopo. Vuoto = quello ereditato, altrimenti il testo integrato.");

        ja(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "薄れた値を削除・更新・統合する整理のシステムメッセージ。プラグインが読み取るため、JSON の回答形式は維持してください。空 = 継承されたもの、なければ組み込みのテキスト。");
        zh(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "整理的系统消息，用于删除、更新和合并已经淡去的值。请保留 JSON 回答格式，插件会读取它。留空 = 继承的设置，否则使用内置文本。");
        fr(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "Le message système du rangement, qui abandonne, met à jour et fusionne les valeurs qui se sont estompées. Gardez le format de réponse JSON, le plugin le lit. Vide = celui hérité, sinon le texte intégré.");
        es(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "El mensaje del sistema de la ordenación, que descarta, actualiza y combina los valores que se han desvanecido. Conserva el formato de respuesta JSON, el plugin lo lee. Vacío = el heredado, si no el texto integrado.");
        de(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "Die Systemnachricht des Aufräumens, das verblasste Werte verwirft, aktualisiert und zusammenführt. Behalte das JSON-Antwortformat bei, das Plugin liest es. Leer = der geerbte, sonst der eingebaute Text.");
        ko(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "희미해진 값을 버리고, 갱신하고, 병합하는 정리의 시스템 메시지. 플러그인이 읽으므로 JSON 응답 형식은 유지하세요. 비워 두면 상속된 것, 없으면 내장 텍스트.");
        it(THE_SYSTEM_MESSAGE_OF_THE_TIDY, "Il messaggio di sistema del riordino, che scarta, aggiorna e unisce i valori che sono sbiaditi. Mantieni il formato di risposta JSON, il plugin lo legge. Vuoto = quello ereditato, altrimenti il testo integrato.");
    }
}
