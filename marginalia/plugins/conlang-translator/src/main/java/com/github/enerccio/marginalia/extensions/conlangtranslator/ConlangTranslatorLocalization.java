package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Conlang Translator in the languages besides English; the English text is the key.
 */
public class ConlangTranslatorLocalization extends PluginLocalizationBundle {

    public static final ConlangTranslatorLocalization T = new ConlangTranslatorLocalization();

    public static final String TRANSLATE_DIALOGS_CONSTRUCTED = "Translate dialogs (constructed language)";
    public static final String TRANSLATING_THE_SPEECH = "Translating the speech of the part...";
    public static final String TRANSLATED_SPEECHES = "Translated %d speech(es), %d new word(s) in the lexicon";
    public static final String SOME_WORDS_NOT_IN_THE_LEXICON = "%d translation(s) use words that are not in the lexicon and were not reported";
    public static final String NO_SPEECH_IN_THE_LANGUAGE = "No speech of the part is spoken in the language";
    public static final String AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S = "An agent run changed the language %s. The changes are not in your library yet.";
    public static final String AN_AGENT_RUN_MADE_THE_LANGUAGE_S = "An agent run made the language %s. It is not in your library yet.";
    public static final String REVIEW_THE_RUN = "Review";
    public static final String ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY = "Add the language %s to your library?";
    public static final String CHANGES_OF_THE_RUN_TO_S = "Changes of the agent run to %s";
    public static final String DROP_THE_CHANGES_OF_THE_RUN_TO_S = "Drop the changes of the agent run to %s? They are not taken into your library.";
    public static final String DROP = "Drop";
    public static final String THE_BOOK_HAS_NO_LANGUAGE = "The book has no language to translate into";
    public static final String THE_BOOK_HAS_NO_MODEL = "There is no model for the translation: the book has none and neither have your defaults";
    public static final String THE_PART_HAS_NO_TEXT = "The part has no text";
    public static final String THE_MODEL_ANSWERED_WRONG = "The model did not answer in the expected form";
    public static final String THE_TRANSLATION_FAILED = "The translation failed: %s";
    public static final String THE_PART_IS_BEING_TRANSLATED = "The part is being translated already";
    public static final String CONLANG_TRANSLATOR = "Conlang Translator";
    public static final String LANGUAGES = "Languages";
    public static final String LANGUAGES_D = "%d language(s)";
    public static final String EDITED = "(edited)";
    public static final String THE_LANGUAGES_YOU_TRANSLATE_INTO = "The constructed languages you translate the speech of your books into. A language is chosen per book or per folder; the words the model invents are added to its lexicon.";
    public static final String THE_LANGUAGES_CAN_NOT_BE_LOADED = "The languages could not be loaded: %s";
    public static final String NEW_LANGUAGE = "New language";
    public static final String LANGUAGE_S = "Language: %s";
    public static final String NAME = "Name";
    public static final String WORDS = "Words";
    public static final String NEW = "New";
    public static final String DESCRIPTION = "Description";
    public static final String ADD = "Add";
    public static final String EDIT = "Edit";
    public static final String DELETE = "Delete";
    public static final String DELETE_S = "Delete %s?";
    public static final String DELETE_THE_LANGUAGE_S = "Delete the language %s with its rules and lexicon? Books that use it will have no language.";
    public static final String DUPLICATE = "Duplicate";
    public static final String EXPORT = "Export";
    public static final String IMPORT = "Import";
    public static final String EXPORT_THE_LANGUAGE_S = "Export the language %s";
    public static final String IMPORT_A_LANGUAGE = "Import a language";
    public static final String IMPORTED_LANGUAGE = "Imported language";
    public static final String PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE = "Paste the document here or choose a file above";
    public static final String THE_DOCUMENT_HAS_THE_RULES = "The document has the rules, categories, registers, lexicon and speakers; the links to the example book and lorebook are left out.";
    public static final String DOWNLOAD = "Download";
    public static final String CLOSE = "Close";
    public static final String CANCEL = "Cancel";
    public static final String OK = "OK";
    public static final String SAVE = "Save";
    public static final String MAXIMIZE = "Maximize";
    public static final String RESTORE_SIZE = "Restore the size";
    public static final String LANGUAGE_OF_THE_SPEECH = "Language of the speech";
    public static final String THE_SPEECH_IS_TRANSLATED_INTO = "The language the speech of the characters is translated into. Empty: the book is not translated.";
    public static final String TRANSLATE_BY_ITSELF = "Translate a new part by itself";
    public static final String A_NEW_PART_IS_TRANSLATED = "A new part is translated when it is final, after the automatic annotation of the book if it has one.";
    public static final String YES_TEXT = "Yes";
    public static final String NO_TEXT = "No";
    public static final String MODEL_OF_THE_TRANSLATION = "Model of the translation";
    public static final String PROTOCOL_OF_THE_TRANSLATION = "Protocol of the translation";
    public static final String EMPTY_THE_MODEL_OF_THE_BOOK = "Empty: the model of the book, else your default model.";
    public static final String EMPTY_THE_PROTOCOL_OF_THE_BOOK = "Empty: the protocol of the book, else your default protocol.";
    public static final String WHAT_BOOKS_AND_FOLDERS_INHERIT = "What books and folders inherit. A book or a folder can choose its own in its dialog, under Extension Settings.";
    public static final String EMPTY_NO_LANGUAGE_UNLESS_A_BOOK = "Empty: no language, unless a book or a folder chooses one.";
    public static final String OVERVIEW = "Overview";
    public static final String RULES = "Rules";
    public static final String CATEGORIES = "Categories";
    public static final String REGISTERS = "Registers";
    public static final String LEXICON = "Lexicon";
    public static final String SPEAKERS = "Speakers";
    public static final String A_HYPHEN_IS_PART_OF_A_WORD = "A hyphen is a part of a word";
    public static final String OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT = "Off: the same compound written with and without hyphens is the same word. On: the hyphen belongs to the form.";
    public static final String WHAT_THE_LANGUAGE_IS_AND_WHO = "What the language is and who speaks it (for you; the translator reads the rules, not this)";
    public static final String EXAMPLE_BOOK = "Example book";
    public static final String EXAMPLE_LOREBOOK = "Example lorebook";
    public static final String THE_DESIGNER_TESTS_ON_ITS_SPEECH = "The designer tries the language on the speech of this book.";
    public static final String THE_DESIGNER_READS_THE_WORLD = "The designer reads the people and places of this lorebook.";
    public static final String IT_DOES_NOT_EXIST_ANY_MORE = "It does not exist any more; the designer works without it.";
    public static final String THE_RULES_ARE_TEXT_THE_PLUGIN = "The rules are text in sections: the plugin does not parse them. Choose who reads each section: the translator, only the designer, or nobody but you.";
    public static final String TITLE = "Title";
    public static final String TEXT = "Text";
    public static final String READ_BY = "Read by";
    public static final String AUDIENCE_TRANSLATOR = "The translator";
    public static final String AUDIENCE_DESIGNER = "The designer only";
    public static final String AUDIENCE_NOBODY = "Nobody (my notes)";
    public static final String THE_TRANSLATOR_GETS_MODEL_SECTIONS = "The translator gets the sections for the translator in every request.";
    public static final String NEW_SECTION = "New section";
    public static final String EDIT_SECTION = "Edit the section";
    public static final String THE_TITLE_IS_EMPTY = "The title is empty";
    public static final String THE_LEXICON_IS_SHOWN_AND_SENT = "The lexicon is shown and sent to the model under these headings. A category that is always sent goes into every request even when the lexicon is too big to be sent whole.";
    public static final String CATEGORY = "Category";
    public static final String ALWAYS_SENT = "Always sent";
    public static final String ALWAYS_SEND_THE_WHOLE_CATEGORY = "Always send the whole category";
    public static final String PARTICLES_NUMBERS_KINSHIP = "For particles, numbers, kinship: the words the translator needs in every part.";
    public static final String NEW_CATEGORY = "New category";
    public static final String EDIT_CATEGORY = "Edit the category";
    public static final String THE_NAME_IS_EMPTY = "The name is empty";
    public static final String ANOTHER_LANGUAGE_HAS_THE_NAME = "Another language has this name";
    public static final String THE_MODEL_CHOOSES_THE_FORM = "The model chooses the form of a word by the register the speaker and the situation ask for (formal, soft, vulgar, archaic...).";
    public static final String WHEN_IT_IS_USED = "When it is used";
    public static final String NEW_REGISTER = "New register";
    public static final String EDIT_REGISTER = "Edit the register";
    public static final String SEARCH_BY_WORD_OR_MEANING = "Search by word or meaning";
    public static final String ALL_CATEGORIES = "All categories";
    public static final String ALL_STATUSES = "All statuses";
    public static final String WORDS_D_OF_D_D_NEW = "%d of %d words, %d new";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_ALL_NEW = "Accept all new";
    public static final String ACCEPT_D_NEW_WORDS = "Accept %d new word(s) as they are?";
    public static final String LOCK = "Lock";
    public static final String UNLOCK = "Unlock";
    public static final String RETIRE = "Retire";
    public static final String RESTORE = "Restore";
    public static final String WORD = "Word";
    public static final String IN_THE_SCRIPT = "In the script";
    public static final String MEANING = "Meaning";
    public static final String MEANINGS = "Meanings";
    public static final String PART_OF_SPEECH = "Part of speech";
    public static final String NOTE = "Note";
    public static final String NOTES = "Notes";
    public static final String KIND = "Kind";
    public static final String REGISTER = "Register";
    public static final String NEUTRAL = "Neutral";
    public static final String STATUS = "Status";
    public static final String KIND_ROOT = "Root";
    public static final String KIND_COMPOUND = "Compound";
    public static final String KIND_AFFIX = "Affix";
    public static final String KIND_PARTICLE = "Particle";
    public static final String KIND_NAME = "Name";
    public static final String KIND_NUMBER = "Number";
    public static final String KIND_PHRASE = "Phrase";
    public static final String KIND_OTHER = "Other";
    public static final String STATUS_NEW = "New";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_LOCKED = "Locked";
    public static final String STATUS_RETIRED = "Retired";
    public static final String NEW_WORD = "New word";
    public static final String EDIT_WORD = "Edit the word";
    public static final String NEW_MEANING = "New meaning";
    public static final String EDIT_MEANING = "Edit the meaning";
    public static final String EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT = "Empty when the language has no script of its own";
    public static final String OTHER_FORMS_OF_THE_SAME_CONCEPT = "Other forms of the same concept";
    public static final String THE_FORMS_FOR_OTHER_REGISTERS = "The forms the concept has in other registers (formal, soft, vulgar...)";
    public static final String WHY_THE_WORD_IS_AS_IT_IS = "Why the word is as it is";
    public static final String RULE_IT_FOLLOWS = "Rule it follows";
    public static final String MADE_BY_THE_AUTHOR = "Made by the author";
    public static final String MADE_BY_THE_MODEL = "Made by the model %s; saving makes it the author's";
    public static final String THE_WORD_IS_EMPTY = "The word is empty";
    public static final String A_WORD_NEEDS_A_MEANING = "A word needs at least one meaning";
    public static final String THE_MEANING_IS_EMPTY = "The meaning is empty";
    public static final String D_WORDS_D_NEW = "%d words, %d new";
    public static final String SELECT_ALL = "Select all";
    public static final String SELECT_NONE = "Select none";
    public static final String SELECT_WITHOUT_CONFLICTS = "Select without conflicts";
    public static final String EVERYTHING = "Everything";
    public static final String TARGET_LANGUAGE = "Language";
    public static final String TARGET_RULE = "Rule section";
    public static final String TARGET_CATEGORY = "Category";
    public static final String TARGET_REGISTER = "Register";
    public static final String TARGET_WORD = "Word";
    public static final String KIND_ADD = "Add";
    public static final String KIND_CHANGE = "Change";
    public static final String KIND_DELETE = "Delete";
    public static final String KIND_MOVE = "Move";
    public static final String CHANGE = "Change";
    public static final String ABOUT = "About";
    public static final String FIELDS = "Fields";
    public static final String CONFLICT_TEXT = "Conflict";
    public static final String D_CHANGES_D_IN_CONFLICT = "%d change(s), %d in conflict";
    public static final String APPLY_D_PICKED = "Apply %d picked";
    public static final String SELECT_A_CHANGE_TO_SEE_ITS_FIELDS = "Select a change to see its fields.";
    public static final String NEEDS_THE_CHANGE_OF_ANOTHER_THING = "This change needs another change (a new category or register that it uses): picking one picks the other.";
    public static final String THE_THING_IS_DELETED = "Picking this deletes it here.";
    public static final String THE_THING_IS_MOVED = "Picking this moves it to the place it has in the other language.";
    public static final String BOTH_CHANGED_THIS = "both changed it";
    public static final String NOTHING = "Nothing";
    public static final String HERE_NOW = "Here, now";
    public static final String IN_THE_OTHER_LANGUAGE = "In the other language";
    public static final String WILL_BE_WRITTEN = "Will be written";
    public static final String KEEP_MINE = "Keep mine";
    public static final String TAKE_THEIRS = "Take theirs";
    public static final String SOME_FIELDS_CHANGED_ON_BOTH_SIDES = "Some fields were changed on both sides";
    public static final String CHANGED_HERE_DELETED_THERE_TEXT = "Changed here, deleted in the other language";
    public static final String DELETED_HERE_CHANGED_THERE_TEXT = "Deleted here, changed in the other language: picking it brings it back";
    public static final String THE_SAME_WORD_IS_HERE = "The same word with a shared meaning is here already";
    public static final String MOVED_ON_BOTH_SIDES = "Moved on both sides";
    public static final String COMPARE = "Compare";
    public static final String BRING_CHANGES_FROM = "Bring the changes from";
    public static final String COMPARE_S = "Compare %s with another language";
    public static final String NO_OTHER_LANGUAGE = "There is no other language to compare with.";
    public static final String THE_LANGUAGES_ARE_THE_SAME = "The two languages are the same.";
    public static final String BRING_THE_CHANGES_OF_S_INTO_S = "Bring the changes of %s into %s";
    public static final String APPLIED_D_CHANGES = "Applied %d change(s)";
    public static final String SKIPPED_D_CHANGED_MEANWHILE = "%d change(s) were skipped: the language is not what the review saw";
    public static final String SKIPPED_D_NEED_ANOTHER = "%d change(s) were skipped: what they need was not picked";
    public static final String A_LANGUAGE_NAMED_S_EXISTS = "A language named %s is in your library already. Review its differences and take over what you want, or import this one as a new language?";
    public static final String REVIEW_THE_DIFFERENCES = "Review the differences";
    public static final String IMPORT_AS_NEW = "Import as new";
    public static final String ACCEPTED_IN_THE_RULES = "Accepted: the section is in the rules.";
    public static final String ADD_A_SECTION_WITH_AI = "Add a section with AI...";
    public static final String ADD_THE_BASIC_CATEGORIES = "Add the basic categories";
    public static final String ASK_ABOUT_THE_LANGUAGE = "Ask about the language, or ask for a change...";
    public static final String ASK_THE_DESIGNER = "Ask the designer...";
    public static final String A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE = "A name that is not a category yet makes a new one.";
    public static final String BACK = "Back";
    public static final String BRIEF = "Brief";
    public static final String CHOOSE_A_SECTION = "Choose a section";
    public static final String CONSTRAINTS_AND_WHAT_TO_AVOID = "Constraints and what to avoid";
    public static final String DESIGN_A_LANGUAGE = "Design a language";
    public static final String DESIGN_WITH_AI = "Design with AI";
    public static final String DISCARD_THE_CHANGES = "Discard the changes to the language?";
    public static final String DISCARD_THE_NEW_LANGUAGE = "Discard the new language and what was made so far?";
    public static final String EDIT_THE_PROPOSAL_YOURSELF = "Edit the text yourself, or write a note above and propose again to refine it.";
    public static final String EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF = "Empty: the first proposal comes from the brief. With a proposal below, the note says what to change.";
    public static final String EMPTY_THE_MODEL_OF_THE_TRANSLATION = "Empty: the model of the translation, else your default model.";
    public static final String EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION = "Empty: the protocol of the translation, else your default protocol.";
    public static final String FINISH = "Finish";
    public static final String FIX = "Fix";
    public static final String GENERATE_WORDS = "Generate words...";
    public static final String HARSH_LIKE_OLD_NORSE = "For example: harsh, like Old Norse; sing-song, Polynesian-like.";
    public static final String HOW_MANY_WORDS = "How many words";
    public static final String INSPIRATIONS = "Inspirations";
    public static final String IN_THE_LANGUAGE = "In the language";
    public static final String LEFT_OUT_OF_THE_PROPOSAL = "Left out of the proposal:";
    public static final String MODEL_OF_THE_DESIGNER = "Model of the designer";
    public static final String NEW_WORDS_OF_THE_SAMPLES = "New words of the samples";
    public static final String NEXT = "Next";
    public static final String NOTHING_FOUND = "Nothing to report.";
    public static final String NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY = "For example: no sounds an English reader cannot say, short words, how the language should look on the page.";
    public static final String PROBLEM = "Problem";
    public static final String PROPOSE = "Propose";
    public static final String PROTOCOL_OF_THE_DESIGNER = "Protocol of the designer";
    public static final String REVIEW_OF_S = "Review of %s";
    public static final String REVIEW_THE_LANGUAGE = "Review the language";
    public static final String REVIEW_THE_PROPOSED_CHANGES = "Review the proposed changes";
    public static final String REWRITE_A_SECTION = "Rewrite a section...";
    public static final String REWRITE_WITH_AI = "Rewrite with AI...";
    public static final String SAY_WHAT_YOU_WANT = "Say what you want first.";
    public static final String SECTION = "Section";
    public static final String SEED_LEXICON = "Seed lexicon";
    public static final String SEND = "Send";
    public static final String SENTENCE = "Sentence";
    public static final String SENTENCES_ONE_PER_LINE = "Sentences, one per line";
    public static final String SEVERITY = "Severity";
    public static final String STAGE_GRAMMAR = "Grammar";
    public static final String STAGE_SOUNDS = "Sounds";
    public static final String STAGE_WORD_FORMATION = "Word formation";
    public static final String STAGE_WRITING = "Writing";
    public static final String STEP_D_OF_D_S = "Step %d of %d: %s";
    public static final String STOP_WAITING = "Stop waiting";
    public static final String TELL_THE_DESIGNER_WHAT_YOU_WANT = "Tell the designer what you want";
    public static final String TEST = "Test";
    public static final String THE_DESIGNER_FAILED = "The designer failed: %s";
    public static final String THE_DESIGNER_IS_REVIEWING = "The designer is reviewing the language...";
    public static final String THE_DESIGNER_IS_THINKING = "The designer is thinking...";
    public static final String THE_DESIGNER_IS_WORKING = "The designer is working, this can take a minute or more...";
    public static final String THE_DESIGNER_PROPOSES_NOTHING = "The designer proposes nothing to write.";
    public static final String THE_DESIGNER_PROPOSES_THE_SECTION = "The designer proposes the section \"%s\" of the rules from the brief and what is accepted so far.";
    public static final String THE_DESIGNER_TRANSLATES_SAMPLES = "The designer translates your sentences with the language as it is, so you can read it and see what looks wrong. Words the language lacks are proposed as new words.";
    public static final String THE_PEOPLE_THEIR_WORLD = "The people who speak it, their world and their culture.";
    public static final String THE_SECTION_IS_EMPTY = "The section is empty";
    public static final String THE_SEED_LEXICON_IS_MADE_BY_CATEGORY = "The first words are made category by category, and each batch is reviewed before it is written. Start with the basic categories, then the names of your book.";
    public static final String TRANSLATE_THE_SENTENCES = "Translate the sentences";
    public static final String WHAT_DO_YOU_WANT = "What do you want?";
    public static final String WHAT_THE_LANGUAGE_IS_FOR = "What the language is for";
    public static final String WHAT_THE_WORDS_ARE_FOR = "What the words are for (optional)";
    public static final String THE_DESIGNER_INVENTS_WORDS = "The designer invents new words by the rules of the language and the lexicon it has. You review them before anything is written.";
    public static final String THE_DESIGNER_WRITES_A_SECTION = "The designer writes a new section of the rules for what the rules miss (a numbering system, colors, politeness...) and the words it needs. You review them before anything is written.";
    public static final String THE_DESIGNER_REWRITES = "The designer rewrites a section as you ask. You see what changed before it is written.";
    public static final String READERS_SEE = "Readers of the published book see";
    public static final String CONLANG_WITH_THE_ENGLISH_ON_CLICK = "The conlang, the English on a click";
    public static final String CONLANG_ONLY = "The conlang only";
    public static final String ENGLISH_ONLY = "The English only";
    public static final String WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE = "How the speech that was translated is shown in the viewer. With the conlang only, the English is never sent to the reader. Your own views are not affected.";
    public static final String TRANSLATED_SPEECH = "Translated speech";
    public static final String THE_CONLANG_AS_IT_IS = "The conlang, as it is";
    public static final String THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES = "The conlang, the English in footnotes";
    public static final String THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES = "The English, the conlang in footnotes";
    public static final String TRANSLATE_PARTS_CONSTRUCTED = "Translate parts (constructed language)";
    public static final String THE_DESIGNER_REVIEWS = "The designer reads the rules and the lexicon and lists contradictions, gaps, and words that break the sound system. Nothing is changed.";
    public static final String CONLANG = "Conlang";
    public static final String QUICK_TRANSLATE = "Quick translate";
    public static final String REFRESH = "Refresh";
    public static final String TEXT_TO_TRANSLATE = "Text to translate (English)";
    public static final String WHO_SAYS_IT_TO_WHOM = "Who says it, to whom (optional)";
    public static final String THE_NOTES_OF_THE_SPEAKERS_ARE_USED = "A character named in the text or here is described to the model with the notes of the Speakers tab.";
    public static final String THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN = "The text is translated into the language of the book. Nothing is written into the story; you use the translation, and import the new words you want into the lexicon.";
    public static final String TRANSLATE = "Translate";
    public static final String TRANSLATION = "Translation";
    public static final String COPY = "Copy";
    public static final String COPIED = "Copied";
    public static final String THERE_IS_NO_TEXT_TO_TRANSLATE = "There is no text to translate";
    public static final String THE_WORDS_THE_TRANSLATION_NEEDED = "Words the translation needed and the lexicon lacked";
    public static final String IMPORT_THE_SELECTED_WORDS = "Import the selected words";
    public static final String IMPORTED_D_WORDS_AS_NEW = "Imported %d word(s) as new";
    public static final String WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS = "Words that are in neither the lexicon nor the new words: %s";
    public static final String TRANSLATE_SELECTED_TEXT_CONSTRUCTED = "Translate selected text (constructed language)";
    public static final String TRANSLATE_MISSING_DIALOGS_CONSTRUCTED = "Translate missing dialogs (constructed language)";
    public static final String TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED = "Translate missing dialogs in selection (constructed language)";
    public static final String TRANSLATING_THE_SELECTION = "Translating the selected text...";
    public static final String THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH = "The selection holds a translated speech; put the English back first";
    public static final String THE_PART_WAS_CHANGED_MEANWHILE = "The part was changed meanwhile; select the text again";

    public static final String NAMES = "Names";
    public static final String THE_NAME_GENERATOR_MAKES_ITS_NAMES = "Check the words the Name generator makes its names of: it trains a Markov chain on them, so the names it gives sound like the language. A word that is deleted or retired later is left out. The categories are offered as \"Conlang - language - category\" after you save the settings.";
    public static final String NAME_CATEGORY = "Name category";
    public static final String NEW_NAME_CATEGORY = "New name category";
    public static final String RENAME = "Rename";
    public static final String RENAME_THE_NAME_CATEGORY = "Rename the name category";
    public static final String THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME = "The language has a name category of that name";
    public static final String ADD_A_NAME_CATEGORY_FIRST = "Add a name category first";
    public static final String D_WORDS_CHOSEN_D_SHOWN = "%d words chosen, %d shown";
    public static final String CHECK_ALL_SHOWN = "Check all shown";
    public static final String UNCHECK_ALL_SHOWN = "Uncheck all shown";
    public static final String PREVIEW_NAMES = "Preview names";
    public static final String CHECK_SOME_WORDS_FIRST = "Check some words first";
    public static final String THE_NAME_GENERATOR_IS_NOT_INSTALLED = "The Name generator is not installed";
    public static final String TOO_FEW_WORDS_TO_MAKE_NAMES = "Too few words to make names of";

    public static final String TARGET_NAME_CATEGORY = "Name category";
    public static final String WORDS_CHOSEN = "Words chosen";

    @Override
    protected void loadKeys() {
        ja(TRANSLATE_DIALOGS_CONSTRUCTED, "台詞を翻訳(人工言語)");
        zh(TRANSLATE_DIALOGS_CONSTRUCTED, "翻译对白(人造语言)");
        fr(TRANSLATE_DIALOGS_CONSTRUCTED, "Traduire les dialogues (langue construite)");
        es(TRANSLATE_DIALOGS_CONSTRUCTED, "Traducir diálogos (lengua construida)");
        de(TRANSLATE_DIALOGS_CONSTRUCTED, "Dialoge übersetzen (Plansprache)");
        ko(TRANSLATE_DIALOGS_CONSTRUCTED, "대화 번역 (인공어)");

        ja(TRANSLATING_THE_SPEECH, "パートの台詞を翻訳中...");
        zh(TRANSLATING_THE_SPEECH, "正在翻译本段对白...");
        fr(TRANSLATING_THE_SPEECH, "Traduction des répliques de la partie...");
        es(TRANSLATING_THE_SPEECH, "Traduciendo el diálogo de la parte...");
        de(TRANSLATING_THE_SPEECH, "Die Rede des Abschnitts wird übersetzt ...");
        ko(TRANSLATING_THE_SPEECH, "파트의 대사를 번역하는 중...");

        ja(TRANSLATED_SPEECHES, "台詞を%d件翻訳し、語彙に新しい単語を%d個追加しました");
        zh(TRANSLATED_SPEECHES, "已翻译 %d 句对白,词汇表新增 %d 个词");
        fr(TRANSLATED_SPEECHES, "%d réplique(s) traduite(s), %d nouveau(x) mot(s) dans le lexique");
        es(TRANSLATED_SPEECHES, "Se han traducido %d diálogo(s), %d palabra(s) nueva(s) en el léxico");
        de(TRANSLATED_SPEECHES, "%d Rede(n) übersetzt, %d neue(s) Wort/Wörter im Lexikon");
        ko(TRANSLATED_SPEECHES, "대사 %d개를 번역했으며 사전에 새 단어 %d개가 추가되었습니다");

        ja(SOME_WORDS_NOT_IN_THE_LEXICON, "%d件の翻訳に、語彙になく報告もされていない単語があります");
        zh(SOME_WORDS_NOT_IN_THE_LEXICON, "%d 句翻译使用了词汇表中没有且未报告的词");
        fr(SOME_WORDS_NOT_IN_THE_LEXICON, "%d traduction(s) utilisent des mots qui ne sont pas dans le lexique et n'ont pas été signalés");
        es(SOME_WORDS_NOT_IN_THE_LEXICON, "%d traducción(es) usan palabras que no están en el léxico y no se informó de ellas");
        de(SOME_WORDS_NOT_IN_THE_LEXICON, "%d Übersetzung(en) verwenden Wörter, die nicht im Lexikon stehen und nicht gemeldet wurden");
        ko(SOME_WORDS_NOT_IN_THE_LEXICON, "번역 %d개가 사전에 없는 단어를 사용했으며 보고되지 않았습니다");

        ja(NO_SPEECH_IN_THE_LANGUAGE, "このパートにこの言語の台詞はありません");
        zh(NO_SPEECH_IN_THE_LANGUAGE, "本段没有使用该语言的对白");
        fr(NO_SPEECH_IN_THE_LANGUAGE, "Aucune réplique de la partie n'est dans cette langue");
        es(NO_SPEECH_IN_THE_LANGUAGE, "Ningún diálogo de la parte se habla en el idioma");
        de(NO_SPEECH_IN_THE_LANGUAGE, "Keine Rede des Abschnitts wird in der Sprache gesprochen");
        ko(NO_SPEECH_IN_THE_LANGUAGE, "파트에 해당 언어로 말하는 대사가 없습니다");

        ja(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "エージェントの実行が言語「%s」を変更しました。変更はまだライブラリに入っていません。");
        zh(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "智能体运行修改了语言“%s”。这些修改尚未进入你的语言库。");
        fr(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "Une exécution d'agent a modifié la langue %s. Les modifications ne sont pas encore dans votre bibliothèque.");
        es(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "Una ejecución de un agente cambió el idioma %s. Los cambios todavía no están en tu biblioteca.");
        de(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "Ein Agentenlauf hat die Sprache %s geändert. Die Änderungen sind noch nicht in deiner Bibliothek.");
        ko(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "에이전트 실행이 언어 %s을(를) 변경했습니다. 변경 사항은 아직 내 라이브러리에 없습니다.");
        ja(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "エージェントの実行が言語「%s」を作成しました。まだライブラリにはありません。");
        zh(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "智能体运行创建了语言“%s”。它尚未进入你的语言库。");
        fr(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "Une exécution d'agent a créé la langue %s. Elle n'est pas encore dans votre bibliothèque.");
        es(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "Una ejecución de un agente creó el idioma %s. Todavía no está en tu biblioteca.");
        de(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "Ein Agentenlauf hat die Sprache %s erstellt. Sie ist noch nicht in deiner Bibliothek.");
        ko(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "에이전트 실행이 언어 %s을(를) 만들었습니다. 아직 내 라이브러리에 없습니다.");
        ja(REVIEW_THE_RUN, "確認");
        zh(REVIEW_THE_RUN, "审阅");
        fr(REVIEW_THE_RUN, "Examiner");
        es(REVIEW_THE_RUN, "Revisar");
        de(REVIEW_THE_RUN, "Prüfen");
        ko(REVIEW_THE_RUN, "검토");
        ja(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "言語「%s」をライブラリに追加しますか?");
        zh(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "将语言“%s”添加到你的语言库吗?");
        fr(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "Ajouter la langue %s à votre bibliothèque ?");
        es(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "¿Añadir el idioma %s a tu biblioteca?");
        de(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "Die Sprache %s zu deiner Bibliothek hinzufügen?");
        ko(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "언어 %s을(를) 내 라이브러리에 추가하시겠습니까?");
        ja(CHANGES_OF_THE_RUN_TO_S, "エージェントの実行による「%s」への変更");
        zh(CHANGES_OF_THE_RUN_TO_S, "智能体运行对“%s”的修改");
        fr(CHANGES_OF_THE_RUN_TO_S, "Modifications de l'exécution d'agent sur %s");
        es(CHANGES_OF_THE_RUN_TO_S, "Cambios de la ejecución del agente en %s");
        de(CHANGES_OF_THE_RUN_TO_S, "Änderungen des Agentenlaufs an %s");
        ko(CHANGES_OF_THE_RUN_TO_S, "%s에 대한 에이전트 실행의 변경 사항");
        ja(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "エージェントの実行による「%s」への変更を破棄しますか? ライブラリには取り込まれません。");
        zh(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "放弃智能体运行对“%s”的修改吗?它们不会进入你的语言库。");
        fr(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "Abandonner les modifications de l'exécution d'agent sur %s ? Elles ne sont pas reprises dans votre bibliothèque.");
        es(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "¿Descartar los cambios de la ejecución del agente en %s? No se incorporan a tu biblioteca.");
        de(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "Die Änderungen des Agentenlaufs an %s verwerfen? Sie werden nicht in deine Bibliothek übernommen.");
        ko(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "%s에 대한 에이전트 실행의 변경 사항을 버리시겠습니까? 내 라이브러리에는 반영되지 않습니다.");
        ja(DROP, "破棄");
        zh(DROP, "放弃");
        fr(DROP, "Abandonner");
        es(DROP, "Descartar");
        de(DROP, "Verwerfen");
        ko(DROP, "버리기");
        ja(THE_BOOK_HAS_NO_LANGUAGE, "この本には翻訳先の言語がありません");
        zh(THE_BOOK_HAS_NO_LANGUAGE, "这本书没有可翻译成的语言");
        fr(THE_BOOK_HAS_NO_LANGUAGE, "Le livre n'a pas de langue vers laquelle traduire");
        es(THE_BOOK_HAS_NO_LANGUAGE, "El libro no tiene idioma al que traducir");
        de(THE_BOOK_HAS_NO_LANGUAGE, "Das Buch hat keine Sprache, in die übersetzt werden kann");
        ko(THE_BOOK_HAS_NO_LANGUAGE, "책에 번역할 언어가 없습니다");

        ja(THE_BOOK_HAS_NO_MODEL, "翻訳用のモデルがありません。本にもデフォルトにも設定されていません");
        zh(THE_BOOK_HAS_NO_MODEL, "没有可用于翻译的模型:书和默认设置都没有指定");
        fr(THE_BOOK_HAS_NO_MODEL, "Il n'y a pas de modèle pour la traduction : le livre n'en a pas, ni vos paramètres par défaut");
        es(THE_BOOK_HAS_NO_MODEL, "No hay modelo para la traducción: el libro no tiene ninguno y tus valores predeterminados tampoco");
        de(THE_BOOK_HAS_NO_MODEL, "Es gibt kein Modell für die Übersetzung: Das Buch hat keines, und deine Standardwerte auch nicht");
        ko(THE_BOOK_HAS_NO_MODEL, "번역용 모델이 없습니다: 책에도 내 기본값에도 없습니다");

        ja(THE_PART_HAS_NO_TEXT, "パートにテキストがありません");
        zh(THE_PART_HAS_NO_TEXT, "本段没有文本");
        fr(THE_PART_HAS_NO_TEXT, "La partie n'a pas de texte");
        es(THE_PART_HAS_NO_TEXT, "La parte no tiene texto");
        de(THE_PART_HAS_NO_TEXT, "Der Abschnitt hat keinen Text");
        ko(THE_PART_HAS_NO_TEXT, "파트에 텍스트가 없습니다");

        ja(THE_MODEL_ANSWERED_WRONG, "モデルが想定外の形式で回答しました");
        zh(THE_MODEL_ANSWERED_WRONG, "模型没有按预期格式回答");
        fr(THE_MODEL_ANSWERED_WRONG, "Le modèle n'a pas répondu sous la forme attendue");
        es(THE_MODEL_ANSWERED_WRONG, "El modelo no respondió con la forma esperada");
        de(THE_MODEL_ANSWERED_WRONG, "Das Modell hat nicht in der erwarteten Form geantwortet");
        ko(THE_MODEL_ANSWERED_WRONG, "모델이 예상한 형식으로 응답하지 않았습니다");

        ja(THE_TRANSLATION_FAILED, "翻訳に失敗しました: %s");
        zh(THE_TRANSLATION_FAILED, "翻译失败:%s");
        fr(THE_TRANSLATION_FAILED, "La traduction a échoué : %s");
        es(THE_TRANSLATION_FAILED, "La traducción ha fallado: %s");
        de(THE_TRANSLATION_FAILED, "Die Übersetzung ist fehlgeschlagen: %s");
        ko(THE_TRANSLATION_FAILED, "번역에 실패했습니다: %s");

        ja(THE_PART_IS_BEING_TRANSLATED, "このパートはすでに翻訳中です");
        zh(THE_PART_IS_BEING_TRANSLATED, "本段正在翻译");
        fr(THE_PART_IS_BEING_TRANSLATED, "La partie est déjà en cours de traduction");
        es(THE_PART_IS_BEING_TRANSLATED, "La parte ya se está traduciendo");
        de(THE_PART_IS_BEING_TRANSLATED, "Der Abschnitt wird bereits übersetzt");
        ko(THE_PART_IS_BEING_TRANSLATED, "파트가 이미 번역 중입니다");

        ja(CONLANG_TRANSLATOR, "コンラング翻訳");
        zh(CONLANG_TRANSLATOR, "构造语翻译");
        fr(CONLANG_TRANSLATOR, "Traducteur de langues construites");
        es(CONLANG_TRANSLATOR, "Traductor de lenguas construidas");
        de(CONLANG_TRANSLATOR, "Conlang-Übersetzer");
        ko(CONLANG_TRANSLATOR, "인공어 번역기");

        ja(LANGUAGES, "言語");
        zh(LANGUAGES, "语言");
        fr(LANGUAGES, "Langues");
        es(LANGUAGES, "Idiomas");
        de(LANGUAGES, "Sprachen");
        ko(LANGUAGES, "언어");

        ja(LANGUAGES_D, "言語 %d 件");
        zh(LANGUAGES_D, "%d 种语言");
        fr(LANGUAGES_D, "%d langue(s)");
        es(LANGUAGES_D, "%d idioma(s)");
        de(LANGUAGES_D, "%d Sprache(n)");
        ko(LANGUAGES_D, "언어 %d개");

        ja(EDITED, "(編集済み)");
        zh(EDITED, "(已编辑)");
        fr(EDITED, "(modifié)");
        es(EDITED, "(editado)");
        de(EDITED, "(bearbeitet)");
        ko(EDITED, "(편집됨)");

        ja(THE_LANGUAGES_YOU_TRANSLATE_INTO, "本の台詞を翻訳する人工言語です。言語は本またはフォルダーごとに選びます。モデルが作った単語は語彙に追加されます。");
        zh(THE_LANGUAGES_YOU_TRANSLATE_INTO, "你的书的对白所翻译成的构造语。语言按书或文件夹选择;模型创造的词会加入其词汇表。");
        fr(THE_LANGUAGES_YOU_TRANSLATE_INTO, "Les langues construites dans lesquelles vous traduisez les répliques de vos livres. Une langue se choisit par livre ou par dossier ; les mots inventés par le modèle sont ajoutés à son lexique.");
        es(THE_LANGUAGES_YOU_TRANSLATE_INTO, "Las lenguas construidas a las que traduces el diálogo de tus libros. Un idioma se elige por libro o por carpeta; las palabras que inventa el modelo se añaden a su léxico.");
        de(THE_LANGUAGES_YOU_TRANSLATE_INTO, "Die Plansprachen, in die du die Rede deiner Bücher übersetzt. Eine Sprache wird je Buch oder je Ordner gewählt; die Wörter, die das Modell erfindet, werden ihrem Lexikon hinzugefügt.");
        ko(THE_LANGUAGES_YOU_TRANSLATE_INTO, "책의 대사를 번역할 인공어. 언어는 책이나 폴더마다 선택하며, 모델이 만든 단어는 해당 언어의 사전에 추가됩니다.");

        ja(THE_LANGUAGES_CAN_NOT_BE_LOADED, "言語を読み込めませんでした: %s");
        zh(THE_LANGUAGES_CAN_NOT_BE_LOADED, "无法加载语言:%s");
        fr(THE_LANGUAGES_CAN_NOT_BE_LOADED, "Les langues n'ont pas pu être chargées : %s");
        es(THE_LANGUAGES_CAN_NOT_BE_LOADED, "No se pudieron cargar los idiomas: %s");
        de(THE_LANGUAGES_CAN_NOT_BE_LOADED, "Die Sprachen konnten nicht geladen werden: %s");
        ko(THE_LANGUAGES_CAN_NOT_BE_LOADED, "언어를 불러올 수 없었습니다: %s");

        ja(NEW_LANGUAGE, "新しい言語");
        zh(NEW_LANGUAGE, "新建语言");
        fr(NEW_LANGUAGE, "Nouvelle langue");
        es(NEW_LANGUAGE, "Idioma nuevo");
        de(NEW_LANGUAGE, "Neue Sprache");
        ko(NEW_LANGUAGE, "새 언어");

        ja(LANGUAGE_S, "言語: %s");
        zh(LANGUAGE_S, "语言:%s");
        fr(LANGUAGE_S, "Langue : %s");
        es(LANGUAGE_S, "Idioma: %s");
        de(LANGUAGE_S, "Sprache: %s");
        ko(LANGUAGE_S, "언어: %s");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");

        ja(WORDS, "単語");
        zh(WORDS, "词数");
        fr(WORDS, "Mots");
        es(WORDS, "Palabras");
        de(WORDS, "Wörter");
        ko(WORDS, "단어");

        ja(NEW, "新規");
        zh(NEW, "新增");
        fr(NEW, "Nouveau");
        es(NEW, "Nuevo");
        de(NEW, "Neu");
        ko(NEW, "새로 만들기");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "描述");
        fr(DESCRIPTION, "Description");
        es(DESCRIPTION, "Descripción");
        de(DESCRIPTION, "Beschreibung");
        ko(DESCRIPTION, "설명");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajouter");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");

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

        ja(DELETE_S, "%s を削除しますか?");
        zh(DELETE_S, "删除 %s?");
        fr(DELETE_S, "Supprimer %s ?");
        es(DELETE_S, "¿Eliminar %s?");
        de(DELETE_S, "%s löschen?");
        ko(DELETE_S, "%s을(를) 삭제하시겠습니까?");

        ja(DELETE_THE_LANGUAGE_S, "言語 %s を、規則と語彙ごと削除しますか?この言語を使う本は言語なしになります。");
        zh(DELETE_THE_LANGUAGE_S, "删除语言 %s 及其规则和词汇表?使用它的书将没有语言。");
        fr(DELETE_THE_LANGUAGE_S, "Supprimer la langue %s avec ses règles et son lexique ? Les livres qui l'utilisent n'auront plus de langue.");
        es(DELETE_THE_LANGUAGE_S, "¿Eliminar el idioma %s con sus reglas y su léxico? Los libros que lo usan se quedarán sin idioma.");
        de(DELETE_THE_LANGUAGE_S, "Die Sprache %s mit ihren Regeln und ihrem Lexikon löschen? Bücher, die sie verwenden, haben dann keine Sprache.");
        ko(DELETE_THE_LANGUAGE_S, "언어 %s과(와) 그 규칙 및 사전을 삭제하시겠습니까? 이 언어를 사용하는 책은 언어가 없어집니다.");

        ja(DUPLICATE, "複製");
        zh(DUPLICATE, "复制");
        fr(DUPLICATE, "Dupliquer");
        es(DUPLICATE, "Duplicar");
        de(DUPLICATE, "Duplizieren");
        ko(DUPLICATE, "복제");

        ja(EXPORT, "エクスポート");
        zh(EXPORT, "导出");
        fr(EXPORT, "Exporter");
        es(EXPORT, "Exportar");
        de(EXPORT, "Exportieren");
        ko(EXPORT, "내보내기");

        ja(IMPORT, "インポート");
        zh(IMPORT, "导入");
        fr(IMPORT, "Importer");
        es(IMPORT, "Importar");
        de(IMPORT, "Importieren");
        ko(IMPORT, "가져오기");

        ja(EXPORT_THE_LANGUAGE_S, "言語 %s をエクスポート");
        zh(EXPORT_THE_LANGUAGE_S, "导出语言 %s");
        fr(EXPORT_THE_LANGUAGE_S, "Exporter la langue %s");
        es(EXPORT_THE_LANGUAGE_S, "Exportar el idioma %s");
        de(EXPORT_THE_LANGUAGE_S, "Die Sprache %s exportieren");
        ko(EXPORT_THE_LANGUAGE_S, "언어 %s 내보내기");

        ja(IMPORT_A_LANGUAGE, "言語をインポート");
        zh(IMPORT_A_LANGUAGE, "导入语言");
        fr(IMPORT_A_LANGUAGE, "Importer une langue");
        es(IMPORT_A_LANGUAGE, "Importar un idioma");
        de(IMPORT_A_LANGUAGE, "Eine Sprache importieren");
        ko(IMPORT_A_LANGUAGE, "언어 가져오기");

        ja(IMPORTED_LANGUAGE, "インポートした言語");
        zh(IMPORTED_LANGUAGE, "导入的语言");
        fr(IMPORTED_LANGUAGE, "Langue importée");
        es(IMPORTED_LANGUAGE, "Idioma importado");
        de(IMPORTED_LANGUAGE, "Importierte Sprache");
        ko(IMPORTED_LANGUAGE, "가져온 언어");

        ja(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "ここにドキュメントを貼り付けるか、上でファイルを選択します");
        zh(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "在此粘贴文档,或在上方选择文件");
        fr(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "Collez le document ici ou choisissez un fichier ci-dessus");
        es(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "Pega aquí el documento o elige un archivo arriba");
        de(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "Füge das Dokument hier ein oder wähle oben eine Datei");
        ko(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "문서를 여기에 붙여 넣거나 위에서 파일을 선택하세요");

        ja(THE_DOCUMENT_HAS_THE_RULES, "ドキュメントには規則・カテゴリ・語域・語彙・話者が含まれます。例の本とロアブックへのリンクは含まれません。");
        zh(THE_DOCUMENT_HAS_THE_RULES, "文档包含规则、类别、语域、词汇表和说话者;不含示例书和设定集的链接。");
        fr(THE_DOCUMENT_HAS_THE_RULES, "Le document contient les règles, catégories, registres, lexique et locuteurs ; les liens vers le livre et le lorebook d'exemple sont omis.");
        es(THE_DOCUMENT_HAS_THE_RULES, "El documento tiene las reglas, las categorías, los registros, el léxico y los hablantes; los vínculos al libro y al lorebook de ejemplo se omiten.");
        de(THE_DOCUMENT_HAS_THE_RULES, "Das Dokument enthält die Regeln, Kategorien, Register, das Lexikon und die Sprecher; die Verknüpfungen mit dem Beispielbuch und dem Beispiel-Lorebook werden weggelassen.");
        ko(THE_DOCUMENT_HAS_THE_RULES, "문서에는 규칙, 범주, 어체, 사전, 화자가 들어 있으며, 예시 책과 로어북에 대한 링크는 제외됩니다.");

        ja(DOWNLOAD, "ダウンロード");
        zh(DOWNLOAD, "下载");
        fr(DOWNLOAD, "Télécharger");
        es(DOWNLOAD, "Descargar");
        de(DOWNLOAD, "Herunterladen");
        ko(DOWNLOAD, "다운로드");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

        ja(OK, "OK");
        zh(OK, "确定");
        fr(OK, "OK");
        es(OK, "Aceptar");
        de(OK, "OK");
        ko(OK, "확인");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");

        ja(MAXIMIZE, "最大化");
        zh(MAXIMIZE, "最大化");
        fr(MAXIMIZE, "Agrandir");
        es(MAXIMIZE, "Maximizar");
        de(MAXIMIZE, "Maximieren");
        ko(MAXIMIZE, "최대화");

        ja(RESTORE_SIZE, "元のサイズに戻す");
        zh(RESTORE_SIZE, "还原大小");
        fr(RESTORE_SIZE, "Rétablir la taille");
        es(RESTORE_SIZE, "Restaurar el tamaño");
        de(RESTORE_SIZE, "Größe wiederherstellen");
        ko(RESTORE_SIZE, "크기 복원");

        ja(LANGUAGE_OF_THE_SPEECH, "台詞の言語");
        zh(LANGUAGE_OF_THE_SPEECH, "对白的语言");
        fr(LANGUAGE_OF_THE_SPEECH, "Langue des répliques");
        es(LANGUAGE_OF_THE_SPEECH, "Idioma del diálogo");
        de(LANGUAGE_OF_THE_SPEECH, "Sprache der Rede");
        ko(LANGUAGE_OF_THE_SPEECH, "대사의 언어");

        ja(THE_SPEECH_IS_TRANSLATED_INTO, "登場人物の台詞を翻訳する言語です。空欄の場合、この本は翻訳されません。");
        zh(THE_SPEECH_IS_TRANSLATED_INTO, "角色对白翻译成的语言。留空:这本书不翻译。");
        fr(THE_SPEECH_IS_TRANSLATED_INTO, "La langue dans laquelle les répliques des personnages sont traduites. Vide : le livre n'est pas traduit.");
        es(THE_SPEECH_IS_TRANSLATED_INTO, "El idioma al que se traduce el diálogo de los personajes. Vacío: el libro no se traduce.");
        de(THE_SPEECH_IS_TRANSLATED_INTO, "Die Sprache, in die die Rede der Figuren übersetzt wird. Leer: Das Buch wird nicht übersetzt.");
        ko(THE_SPEECH_IS_TRANSLATED_INTO, "캐릭터의 대사를 번역할 언어. 비워 두면 책은 번역되지 않습니다.");

        ja(TRANSLATE_BY_ITSELF, "新しいパートを自動で翻訳");
        zh(TRANSLATE_BY_ITSELF, "自动翻译新的段落");
        fr(TRANSLATE_BY_ITSELF, "Traduire une nouvelle partie automatiquement");
        es(TRANSLATE_BY_ITSELF, "Traducir una parte nueva automáticamente");
        de(TRANSLATE_BY_ITSELF, "Einen neuen Abschnitt von selbst übersetzen");
        ko(TRANSLATE_BY_ITSELF, "새 파트를 자동으로 번역");

        ja(A_NEW_PART_IS_TRANSLATED, "新しいパートは、本に自動注釈がある場合はそれが終わった後、確定した時点で翻訳されます。");
        zh(A_NEW_PART_IS_TRANSLATED, "新段落在定稿后翻译;若书有自动批注,则在其之后。");
        fr(A_NEW_PART_IS_TRANSLATED, "Une nouvelle partie est traduite quand elle est définitive, après l'annotation automatique du livre s'il en a une.");
        es(A_NEW_PART_IS_TRANSLATED, "Una parte nueva se traduce cuando es definitiva, después de la anotación automática del libro si la tiene.");
        de(A_NEW_PART_IS_TRANSLATED, "Ein neuer Abschnitt wird übersetzt, wenn er endgültig ist, nach der automatischen Annotation des Buchs, falls es eine hat.");
        ko(A_NEW_PART_IS_TRANSLATED, "새 파트는 최종이 되면 (책에 자동 주석이 있으면 그 후에) 번역됩니다.");

        ja(YES_TEXT, "はい");
        zh(YES_TEXT, "是");
        fr(YES_TEXT, "Oui");
        es(YES_TEXT, "Sí");
        de(YES_TEXT, "Ja");
        ko(YES_TEXT, "예");

        ja(NO_TEXT, "いいえ");
        zh(NO_TEXT, "否");
        fr(NO_TEXT, "Non");
        es(NO_TEXT, "No");
        de(NO_TEXT, "Nein");
        ko(NO_TEXT, "아니요");

        ja(MODEL_OF_THE_TRANSLATION, "翻訳に使うモデル");
        zh(MODEL_OF_THE_TRANSLATION, "翻译所用的模型");
        fr(MODEL_OF_THE_TRANSLATION, "Modèle de la traduction");
        es(MODEL_OF_THE_TRANSLATION, "Modelo de la traducción");
        de(MODEL_OF_THE_TRANSLATION, "Modell der Übersetzung");
        ko(MODEL_OF_THE_TRANSLATION, "번역용 모델");

        ja(PROTOCOL_OF_THE_TRANSLATION, "翻訳に使うプロトコル");
        zh(PROTOCOL_OF_THE_TRANSLATION, "翻译所用的协议");
        fr(PROTOCOL_OF_THE_TRANSLATION, "Protocole de la traduction");
        es(PROTOCOL_OF_THE_TRANSLATION, "Protocolo de la traducción");
        de(PROTOCOL_OF_THE_TRANSLATION, "Protokoll der Übersetzung");
        ko(PROTOCOL_OF_THE_TRANSLATION, "번역용 프로토콜");

        ja(EMPTY_THE_MODEL_OF_THE_BOOK, "空欄の場合: 本のモデル、なければ既定のモデル。");
        zh(EMPTY_THE_MODEL_OF_THE_BOOK, "留空:使用书的模型,否则使用你的默认模型。");
        fr(EMPTY_THE_MODEL_OF_THE_BOOK, "Vide : le modèle du livre, sinon votre modèle par défaut.");
        es(EMPTY_THE_MODEL_OF_THE_BOOK, "Vacío: el modelo del libro o, si no, tu modelo predeterminado.");
        de(EMPTY_THE_MODEL_OF_THE_BOOK, "Leer: das Modell des Buchs, sonst dein Standardmodell.");
        ko(EMPTY_THE_MODEL_OF_THE_BOOK, "비워 두면: 책의 모델, 없으면 내 기본 모델.");

        ja(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "空欄の場合: 本のプロトコル、なければ既定のプロトコル。");
        zh(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "留空:使用书的协议,否则使用你的默认协议。");
        fr(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "Vide : le protocole du livre, sinon votre protocole par défaut.");
        es(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "Vacío: el protocolo del libro o, si no, tu protocolo predeterminado.");
        de(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "Leer: das Protokoll des Buchs, sonst dein Standardprotokoll.");
        ko(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "비워 두면: 책의 프로토콜, 없으면 내 기본 프로토콜.");

        ja(WHAT_BOOKS_AND_FOLDERS_INHERIT, "本とフォルダーが引き継ぐ設定です。本やフォルダーは、各ダイアログの「拡張機能設定」で独自に選べます。");
        zh(WHAT_BOOKS_AND_FOLDERS_INHERIT, "书和文件夹继承的设置。书或文件夹可在其对话框的“扩展设置”中自行选择。");
        fr(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Ce dont héritent les livres et les dossiers. Un livre ou un dossier peut choisir le sien dans sa fenêtre, sous Paramètres des extensions.");
        es(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Lo que heredan los libros y las carpetas. Un libro o una carpeta puede elegir lo suyo en su cuadro de diálogo, en Ajustes de extensiones.");
        de(WHAT_BOOKS_AND_FOLDERS_INHERIT, "Was Bücher und Ordner erben. Ein Buch oder ein Ordner kann in seinem Dialog unter „Erweiterungseinstellungen“ eigene Werte wählen.");
        ko(WHAT_BOOKS_AND_FOLDERS_INHERIT, "책과 폴더가 상속하는 것. 책이나 폴더는 대화 상자의 확장 기능 설정에서 자체 값을 선택할 수 있습니다.");

        ja(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "空欄の場合: 本またはフォルダーが選ばない限り言語なし。");
        zh(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "留空:没有语言,除非书或文件夹另行选择。");
        fr(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "Vide : aucune langue, sauf si un livre ou un dossier en choisit une.");
        es(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "Vacío: ningún idioma, a menos que lo elija un libro o una carpeta.");
        de(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "Leer: keine Sprache, es sei denn, ein Buch oder ein Ordner wählt eine.");
        ko(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "비워 두면: 책이나 폴더가 선택하지 않는 한 언어 없음.");

        ja(OVERVIEW, "概要");
        zh(OVERVIEW, "概览");
        fr(OVERVIEW, "Vue d'ensemble");
        es(OVERVIEW, "Resumen general");
        de(OVERVIEW, "Übersicht");
        ko(OVERVIEW, "개요");

        ja(RULES, "規則");
        zh(RULES, "规则");
        fr(RULES, "Règles");
        es(RULES, "Reglas");
        de(RULES, "Regeln");
        ko(RULES, "규칙");

        ja(CATEGORIES, "カテゴリ");
        zh(CATEGORIES, "类别");
        fr(CATEGORIES, "Catégories");
        es(CATEGORIES, "Categorías");
        de(CATEGORIES, "Kategorien");
        ko(CATEGORIES, "범주");

        ja(REGISTERS, "語域");
        zh(REGISTERS, "语域");
        fr(REGISTERS, "Registres");
        es(REGISTERS, "Registros");
        de(REGISTERS, "Register");
        ko(REGISTERS, "어체");

        ja(LEXICON, "語彙");
        zh(LEXICON, "词汇表");
        fr(LEXICON, "Lexique");
        es(LEXICON, "Léxico");
        de(LEXICON, "Lexikon");
        ko(LEXICON, "사전");

        ja(SPEAKERS, "話者");
        zh(SPEAKERS, "说话者");
        fr(SPEAKERS, "Locuteurs");
        es(SPEAKERS, "Hablantes");
        de(SPEAKERS, "Sprecher");
        ko(SPEAKERS, "화자");

        ja(A_HYPHEN_IS_PART_OF_A_WORD, "ハイフンは単語の一部");
        zh(A_HYPHEN_IS_PART_OF_A_WORD, "连字符是词的一部分");
        fr(A_HYPHEN_IS_PART_OF_A_WORD, "Un trait d'union fait partie du mot");
        es(A_HYPHEN_IS_PART_OF_A_WORD, "Un guion forma parte de una palabra");
        de(A_HYPHEN_IS_PART_OF_A_WORD, "Ein Bindestrich ist Teil eines Wortes");
        ko(A_HYPHEN_IS_PART_OF_A_WORD, "하이픈은 단어의 일부");

        ja(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "オフ: ハイフンの有無が違う同じ複合語は同じ単語として扱います。オン: ハイフンも語形の一部です。");
        zh(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "关闭:带连字符与不带连字符的同一复合词视为同一个词。开启:连字符属于词形。");
        fr(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "Désactivé : le même composé écrit avec et sans traits d'union est le même mot. Activé : le trait d'union appartient à la forme.");
        es(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "Desactivado: el mismo compuesto escrito con y sin guiones es la misma palabra. Activado: el guion forma parte de la forma.");
        de(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "Aus: Dasselbe Kompositum mit und ohne Bindestrich geschrieben ist dasselbe Wort. An: Der Bindestrich gehört zur Form.");
        ko(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "끄면: 하이픈이 있거나 없는 같은 복합어는 같은 단어입니다. 켜면: 하이픈이 형태의 일부입니다.");

        ja(WHAT_THE_LANGUAGE_IS_AND_WHO, "この言語が何で、誰が話すか(自分用のメモです。翻訳モデルが読むのは規則で、これではありません)");
        zh(WHAT_THE_LANGUAGE_IS_AND_WHO, "这门语言是什么、谁在说(给你自己看;翻译模型读的是规则,不是这里)");
        fr(WHAT_THE_LANGUAGE_IS_AND_WHO, "Ce qu'est la langue et qui la parle (pour vous ; le traducteur lit les règles, pas ceci)");
        es(WHAT_THE_LANGUAGE_IS_AND_WHO, "Qué es el idioma y quién lo habla (para ti; el traductor lee las reglas, no esto)");
        de(WHAT_THE_LANGUAGE_IS_AND_WHO, "Was die Sprache ist und wer sie spricht (für dich; der Übersetzer liest die Regeln, nicht dies)");
        ko(WHAT_THE_LANGUAGE_IS_AND_WHO, "언어가 무엇이고 누가 사용하는지 (나를 위한 것이며, 번역기는 이것이 아니라 규칙을 읽습니다)");

        ja(EXAMPLE_BOOK, "例の本");
        zh(EXAMPLE_BOOK, "示例书");
        fr(EXAMPLE_BOOK, "Livre d'exemple");
        es(EXAMPLE_BOOK, "Libro de ejemplo");
        de(EXAMPLE_BOOK, "Beispielbuch");
        ko(EXAMPLE_BOOK, "예시 책");

        ja(EXAMPLE_LOREBOOK, "例のロアブック");
        zh(EXAMPLE_LOREBOOK, "示例设定集");
        fr(EXAMPLE_LOREBOOK, "Lorebook d'exemple");
        es(EXAMPLE_LOREBOOK, "Lorebook de ejemplo");
        de(EXAMPLE_LOREBOOK, "Beispiel-Lorebook");
        ko(EXAMPLE_LOREBOOK, "예시 로어북");

        ja(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "デザイナーはこの本の台詞で言語を試します。");
        zh(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "设计器用这本书的对白试验语言。");
        fr(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "Le concepteur essaie la langue sur les répliques de ce livre.");
        es(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "El diseñador prueba el idioma con el diálogo de este libro.");
        de(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "Der Designer probiert die Sprache an der Rede dieses Buchs aus.");
        ko(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "디자이너가 이 책의 대사로 언어를 시험합니다.");

        ja(THE_DESIGNER_READS_THE_WORLD, "デザイナーはこのロアブックの人物と場所を読みます。");
        zh(THE_DESIGNER_READS_THE_WORLD, "设计器读取这本设定集中的人物和地点。");
        fr(THE_DESIGNER_READS_THE_WORLD, "Le concepteur lit les personnes et les lieux de ce lorebook.");
        es(THE_DESIGNER_READS_THE_WORLD, "El diseñador lee las personas y los lugares de este lorebook.");
        de(THE_DESIGNER_READS_THE_WORLD, "Der Designer liest die Personen und Orte dieses Lorebooks.");
        ko(THE_DESIGNER_READS_THE_WORLD, "디자이너가 이 로어북의 인물과 장소를 읽습니다.");

        ja(IT_DOES_NOT_EXIST_ANY_MORE, "既に存在しません。デザイナーはなしで動作します。");
        zh(IT_DOES_NOT_EXIST_ANY_MORE, "它已不存在;设计器将在没有它的情况下工作。");
        fr(IT_DOES_NOT_EXIST_ANY_MORE, "Il n'existe plus ; le concepteur travaille sans lui.");
        es(IT_DOES_NOT_EXIST_ANY_MORE, "Ya no existe; el diseñador trabaja sin él.");
        de(IT_DOES_NOT_EXIST_ANY_MORE, "Es existiert nicht mehr; der Designer arbeitet ohne es.");
        ko(IT_DOES_NOT_EXIST_ANY_MORE, "더 이상 존재하지 않습니다. 디자이너는 이것 없이 작업합니다.");

        ja(THE_RULES_ARE_TEXT_THE_PLUGIN, "規則はセクションに分けたテキストで、プラグインは解析しません。各セクションの読み手を選びます: 翻訳モデル、デザイナーのみ、または自分だけ。");
        zh(THE_RULES_ARE_TEXT_THE_PLUGIN, "规则是分节的文本,插件不会解析。为每一节选择读者:翻译模型、仅设计器,或只有你自己。");
        fr(THE_RULES_ARE_TEXT_THE_PLUGIN, "Les règles sont du texte en sections : le plugin ne les analyse pas. Choisissez qui lit chaque section : le traducteur, seulement le concepteur, ou personne d'autre que vous.");
        es(THE_RULES_ARE_TEXT_THE_PLUGIN, "Las reglas son texto en secciones: el plugin no las analiza. Elige quién lee cada sección: el traductor, solo el diseñador, o nadie salvo tú.");
        de(THE_RULES_ARE_TEXT_THE_PLUGIN, "Die Regeln sind Text in Abschnitten: Das Plugin wertet sie nicht aus. Wähle, wer jeden Abschnitt liest: der Übersetzer, nur der Designer oder niemand außer dir.");
        ko(THE_RULES_ARE_TEXT_THE_PLUGIN, "규칙은 섹션으로 나뉜 텍스트이며 플러그인이 구문 분석하지 않습니다. 각 섹션을 누가 읽을지 선택하세요: 번역기, 디자이너만, 또는 나만.");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");
        fr(TITLE, "Titre");
        es(TITLE, "Título");
        de(TITLE, "Titel");
        ko(TITLE, "제목");

        ja(TEXT, "テキスト");
        zh(TEXT, "文本");
        fr(TEXT, "Texte");
        es(TEXT, "Texto");
        de(TEXT, "Text");
        ko(TEXT, "텍스트");

        ja(READ_BY, "読み手");
        zh(READ_BY, "读者");
        fr(READ_BY, "Lu par");
        es(READ_BY, "Lo lee");
        de(READ_BY, "Gelesen von");
        ko(READ_BY, "읽는 대상");

        ja(AUDIENCE_TRANSLATOR, "翻訳モデル");
        zh(AUDIENCE_TRANSLATOR, "翻译模型");
        fr(AUDIENCE_TRANSLATOR, "Le traducteur");
        es(AUDIENCE_TRANSLATOR, "El traductor");
        de(AUDIENCE_TRANSLATOR, "Der Übersetzer");
        ko(AUDIENCE_TRANSLATOR, "번역기");

        ja(AUDIENCE_DESIGNER, "デザイナーのみ");
        zh(AUDIENCE_DESIGNER, "仅设计器");
        fr(AUDIENCE_DESIGNER, "Le concepteur seulement");
        es(AUDIENCE_DESIGNER, "Solo el diseñador");
        de(AUDIENCE_DESIGNER, "Nur der Designer");
        ko(AUDIENCE_DESIGNER, "디자이너만");

        ja(AUDIENCE_NOBODY, "なし(自分用メモ)");
        zh(AUDIENCE_NOBODY, "无人(我的笔记)");
        fr(AUDIENCE_NOBODY, "Personne (mes notes)");
        es(AUDIENCE_NOBODY, "Nadie (mis notas)");
        de(AUDIENCE_NOBODY, "Niemand (meine Notizen)");
        ko(AUDIENCE_NOBODY, "아무도 (내 메모)");

        ja(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "翻訳モデルは、翻訳モデル向けのセクションをすべてのリクエストで受け取ります。");
        zh(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "翻译模型在每次请求中都会收到面向翻译模型的小节。");
        fr(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "Le traducteur reçoit les sections destinées au traducteur dans chaque requête.");
        es(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "El traductor recibe las secciones para el traductor en cada solicitud.");
        de(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "Der Übersetzer erhält die Abschnitte für den Übersetzer bei jeder Anfrage.");
        ko(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "번역기는 모든 요청에서 번역기용 섹션을 받습니다.");

        ja(NEW_SECTION, "新しいセクション");
        zh(NEW_SECTION, "新建小节");
        fr(NEW_SECTION, "Nouvelle section");
        es(NEW_SECTION, "Sección nueva");
        de(NEW_SECTION, "Neuer Abschnitt");
        ko(NEW_SECTION, "새 섹션");

        ja(EDIT_SECTION, "セクションを編集");
        zh(EDIT_SECTION, "编辑小节");
        fr(EDIT_SECTION, "Modifier la section");
        es(EDIT_SECTION, "Editar la sección");
        de(EDIT_SECTION, "Den Abschnitt bearbeiten");
        ko(EDIT_SECTION, "섹션 편집");

        ja(THE_TITLE_IS_EMPTY, "タイトルが空です");
        zh(THE_TITLE_IS_EMPTY, "标题为空");
        fr(THE_TITLE_IS_EMPTY, "Le titre est vide");
        es(THE_TITLE_IS_EMPTY, "El título está vacío");
        de(THE_TITLE_IS_EMPTY, "Der Titel ist leer");
        ko(THE_TITLE_IS_EMPTY, "제목이 비어 있습니다");

        ja(THE_LEXICON_IS_SHOWN_AND_SENT, "語彙はこの見出しの下で表示され、モデルにも送られます。常に送るカテゴリは、語彙が大きすぎて全部送れない場合でも毎回のリクエストに含まれます。");
        zh(THE_LEXICON_IS_SHOWN_AND_SENT, "词汇表按这些标题显示并发送给模型。始终发送的类别即使词汇表过大无法全部发送,也会包含在每次请求中。");
        fr(THE_LEXICON_IS_SHOWN_AND_SENT, "Le lexique est affiché et envoyé au modèle sous ces rubriques. Une catégorie toujours envoyée va dans chaque requête, même quand le lexique est trop gros pour être envoyé en entier.");
        es(THE_LEXICON_IS_SHOWN_AND_SENT, "El léxico se muestra y se envía al modelo bajo estos encabezados. Una categoría que se envía siempre va en cada solicitud incluso cuando el léxico es demasiado grande para enviarlo entero.");
        de(THE_LEXICON_IS_SHOWN_AND_SENT, "Das Lexikon wird unter diesen Überschriften angezeigt und an das Modell gesendet. Eine Kategorie, die immer gesendet wird, geht in jede Anfrage, auch wenn das Lexikon zu groß ist, um ganz gesendet zu werden.");
        ko(THE_LEXICON_IS_SHOWN_AND_SENT, "사전은 이 제목 아래에 표시되고 모델에 전송됩니다. 항상 전송되는 범주는 사전이 너무 커서 통째로 보낼 수 없을 때도 모든 요청에 포함됩니다.");

        ja(CATEGORY, "カテゴリ");
        zh(CATEGORY, "类别");
        fr(CATEGORY, "Catégorie");
        es(CATEGORY, "Categoría");
        de(CATEGORY, "Kategorie");
        ko(CATEGORY, "범주");

        ja(ALWAYS_SENT, "常に送信");
        zh(ALWAYS_SENT, "始终发送");
        fr(ALWAYS_SENT, "Toujours envoyée");
        es(ALWAYS_SENT, "Se envía siempre");
        de(ALWAYS_SENT, "Immer gesendet");
        ko(ALWAYS_SENT, "항상 전송");

        ja(ALWAYS_SEND_THE_WHOLE_CATEGORY, "カテゴリ全体を常に送る");
        zh(ALWAYS_SEND_THE_WHOLE_CATEGORY, "始终发送整个类别");
        fr(ALWAYS_SEND_THE_WHOLE_CATEGORY, "Toujours envoyer toute la catégorie");
        es(ALWAYS_SEND_THE_WHOLE_CATEGORY, "Enviar siempre la categoría entera");
        de(ALWAYS_SEND_THE_WHOLE_CATEGORY, "Die ganze Kategorie immer senden");
        ko(ALWAYS_SEND_THE_WHOLE_CATEGORY, "범주 전체를 항상 전송");

        ja(PARTICLES_NUMBERS_KINSHIP, "助詞・数詞・親族名称など、どのパートでも翻訳に必要な単語向けです。");
        zh(PARTICLES_NUMBERS_KINSHIP, "适用于助词、数词、亲属称谓:翻译每个段落都需要的词。");
        fr(PARTICLES_NUMBERS_KINSHIP, "Pour les particules, nombres, liens de parenté : les mots dont le traducteur a besoin dans chaque partie.");
        es(PARTICLES_NUMBERS_KINSHIP, "Para partículas, números, parentesco: las palabras que el traductor necesita en cada parte.");
        de(PARTICLES_NUMBERS_KINSHIP, "Für Partikeln, Zahlen, Verwandtschaft: die Wörter, die der Übersetzer in jedem Abschnitt braucht.");
        ko(PARTICLES_NUMBERS_KINSHIP, "조사, 숫자, 친족어용: 번역기가 모든 파트에서 필요로 하는 단어.");

        ja(NEW_CATEGORY, "新しいカテゴリ");
        zh(NEW_CATEGORY, "新建类别");
        fr(NEW_CATEGORY, "Nouvelle catégorie");
        es(NEW_CATEGORY, "Categoría nueva");
        de(NEW_CATEGORY, "Neue Kategorie");
        ko(NEW_CATEGORY, "새 범주");

        ja(EDIT_CATEGORY, "カテゴリを編集");
        zh(EDIT_CATEGORY, "编辑类别");
        fr(EDIT_CATEGORY, "Modifier la catégorie");
        es(EDIT_CATEGORY, "Editar la categoría");
        de(EDIT_CATEGORY, "Die Kategorie bearbeiten");
        ko(EDIT_CATEGORY, "범주 편집");

        ja(THE_NAME_IS_EMPTY, "名前が空です");
        zh(THE_NAME_IS_EMPTY, "名称为空");
        fr(THE_NAME_IS_EMPTY, "Le nom est vide");
        es(THE_NAME_IS_EMPTY, "El nombre está vacío");
        de(THE_NAME_IS_EMPTY, "Der Name ist leer");
        ko(THE_NAME_IS_EMPTY, "이름이 비어 있습니다");

        ja(ANOTHER_LANGUAGE_HAS_THE_NAME, "同じ名前の言語が既にあります");
        zh(ANOTHER_LANGUAGE_HAS_THE_NAME, "已有另一种语言使用此名称");
        fr(ANOTHER_LANGUAGE_HAS_THE_NAME, "Une autre langue porte ce nom");
        es(ANOTHER_LANGUAGE_HAS_THE_NAME, "Otro idioma tiene este nombre");
        de(ANOTHER_LANGUAGE_HAS_THE_NAME, "Eine andere Sprache hat diesen Namen");
        ko(ANOTHER_LANGUAGE_HAS_THE_NAME, "다른 언어가 이 이름을 사용합니다");

        ja(THE_MODEL_CHOOSES_THE_FORM, "モデルは、話し手と状況に合わせた語域(丁寧、柔らかい、粗野、古風など)で単語の形を選びます。");
        zh(THE_MODEL_CHOOSES_THE_FORM, "模型根据说话者和情境所需的语域(正式、柔和、粗俗、古语等)选择词形。");
        fr(THE_MODEL_CHOOSES_THE_FORM, "Le modèle choisit la forme d'un mot selon le registre que demandent le locuteur et la situation (soutenu, doux, vulgaire, archaïque...).");
        es(THE_MODEL_CHOOSES_THE_FORM, "El modelo elige la forma de una palabra según el registro que piden el hablante y la situación (formal, suave, vulgar, arcaico...).");
        de(THE_MODEL_CHOOSES_THE_FORM, "Das Modell wählt die Form eines Wortes nach dem Register, das der Sprecher und die Situation verlangen (förmlich, sanft, vulgär, archaisch ...).");
        ko(THE_MODEL_CHOOSES_THE_FORM, "모델은 화자와 상황이 요구하는 어체(격식, 부드러움, 비속어, 고어...)에 따라 단어의 형태를 선택합니다.");

        ja(WHEN_IT_IS_USED, "使われる場面");
        zh(WHEN_IT_IS_USED, "使用场合");
        fr(WHEN_IT_IS_USED, "Quand il est utilisé");
        es(WHEN_IT_IS_USED, "Cuándo se usa");
        de(WHEN_IT_IS_USED, "Wann es verwendet wird");
        ko(WHEN_IT_IS_USED, "사용되는 때");

        ja(NEW_REGISTER, "新しい語域");
        zh(NEW_REGISTER, "新建语域");
        fr(NEW_REGISTER, "Nouveau registre");
        es(NEW_REGISTER, "Registro nuevo");
        de(NEW_REGISTER, "Neues Register");
        ko(NEW_REGISTER, "새 어체");

        ja(EDIT_REGISTER, "語域を編集");
        zh(EDIT_REGISTER, "编辑语域");
        fr(EDIT_REGISTER, "Modifier le registre");
        es(EDIT_REGISTER, "Editar el registro");
        de(EDIT_REGISTER, "Das Register bearbeiten");
        ko(EDIT_REGISTER, "어체 편집");

        ja(SEARCH_BY_WORD_OR_MEANING, "単語または意味で検索");
        zh(SEARCH_BY_WORD_OR_MEANING, "按词或词义搜索");
        fr(SEARCH_BY_WORD_OR_MEANING, "Rechercher par mot ou par sens");
        es(SEARCH_BY_WORD_OR_MEANING, "Buscar por palabra o significado");
        de(SEARCH_BY_WORD_OR_MEANING, "Nach Wort oder Bedeutung suchen");
        ko(SEARCH_BY_WORD_OR_MEANING, "단어나 뜻으로 검색");

        ja(ALL_CATEGORIES, "すべてのカテゴリ");
        zh(ALL_CATEGORIES, "所有类别");
        fr(ALL_CATEGORIES, "Toutes les catégories");
        es(ALL_CATEGORIES, "Todas las categorías");
        de(ALL_CATEGORIES, "Alle Kategorien");
        ko(ALL_CATEGORIES, "모든 범주");

        ja(ALL_STATUSES, "すべての状態");
        zh(ALL_STATUSES, "所有状态");
        fr(ALL_STATUSES, "Tous les statuts");
        es(ALL_STATUSES, "Todos los estados");
        de(ALL_STATUSES, "Alle Status");
        ko(ALL_STATUSES, "모든 상태");

        ja(WORDS_D_OF_D_D_NEW, "%d / %d 語、新規 %d");
        zh(WORDS_D_OF_D_D_NEW, "%d / %d 个词,新增 %d");
        fr(WORDS_D_OF_D_D_NEW, "%d mots sur %d, %d nouveaux");
        es(WORDS_D_OF_D_D_NEW, "%d de %d palabras, %d nuevas");
        de(WORDS_D_OF_D_D_NEW, "%d von %d Wörtern, %d neu");
        ko(WORDS_D_OF_D_D_NEW, "단어 %d / %d개, 새 단어 %d개");

        ja(ACCEPT, "承認");
        zh(ACCEPT, "接受");
        fr(ACCEPT, "Accepter");
        es(ACCEPT, "Aceptar");
        de(ACCEPT, "Übernehmen");
        ko(ACCEPT, "수락");

        ja(ACCEPT_ALL_NEW, "新規をすべて承認");
        zh(ACCEPT_ALL_NEW, "接受所有新增");
        fr(ACCEPT_ALL_NEW, "Accepter tous les nouveaux");
        es(ACCEPT_ALL_NEW, "Aceptar todas las nuevas");
        de(ACCEPT_ALL_NEW, "Alle neuen übernehmen");
        ko(ACCEPT_ALL_NEW, "새 단어 모두 수락");

        ja(ACCEPT_D_NEW_WORDS, "新規の単語 %d 個をそのまま承認しますか?");
        zh(ACCEPT_D_NEW_WORDS, "按原样接受 %d 个新词?");
        fr(ACCEPT_D_NEW_WORDS, "Accepter %d nouveau(x) mot(s) tels quels ?");
        es(ACCEPT_D_NEW_WORDS, "¿Aceptar %d palabra(s) nueva(s) tal como están?");
        de(ACCEPT_D_NEW_WORDS, "%d neue(s) Wort/Wörter unverändert übernehmen?");
        ko(ACCEPT_D_NEW_WORDS, "새 단어 %d개를 그대로 수락하시겠습니까?");

        ja(LOCK, "ロック");
        zh(LOCK, "锁定");
        fr(LOCK, "Verrouiller");
        es(LOCK, "Bloquear");
        de(LOCK, "Sperren");
        ko(LOCK, "잠금");

        ja(UNLOCK, "ロック解除");
        zh(UNLOCK, "解锁");
        fr(UNLOCK, "Déverrouiller");
        es(UNLOCK, "Desbloquear");
        de(UNLOCK, "Entsperren");
        ko(UNLOCK, "잠금 해제");

        ja(RETIRE, "廃止");
        zh(RETIRE, "停用");
        fr(RETIRE, "Retirer");
        es(RETIRE, "Retirar");
        de(RETIRE, "Ausmustern");
        ko(RETIRE, "폐기");

        ja(RESTORE, "復元");
        zh(RESTORE, "恢复");
        fr(RESTORE, "Restaurer");
        es(RESTORE, "Restaurar");
        de(RESTORE, "Wiederherstellen");
        ko(RESTORE, "복원");

        ja(WORD, "単語");
        zh(WORD, "词");
        fr(WORD, "Mot");
        es(WORD, "Palabra");
        de(WORD, "Wort");
        ko(WORD, "단어");

        ja(IN_THE_SCRIPT, "文字体系での表記");
        zh(IN_THE_SCRIPT, "文字写法");
        fr(IN_THE_SCRIPT, "Dans l'écriture");
        es(IN_THE_SCRIPT, "En la escritura");
        de(IN_THE_SCRIPT, "In der Schrift");
        ko(IN_THE_SCRIPT, "문자 표기");

        ja(MEANING, "意味");
        zh(MEANING, "词义");
        fr(MEANING, "Sens");
        es(MEANING, "Significado");
        de(MEANING, "Bedeutung");
        ko(MEANING, "뜻");

        ja(MEANINGS, "意味");
        zh(MEANINGS, "词义");
        fr(MEANINGS, "Sens");
        es(MEANINGS, "Significados");
        de(MEANINGS, "Bedeutungen");
        ko(MEANINGS, "뜻");

        ja(PART_OF_SPEECH, "品詞");
        zh(PART_OF_SPEECH, "词性");
        fr(PART_OF_SPEECH, "Nature grammaticale");
        es(PART_OF_SPEECH, "Categoría gramatical");
        de(PART_OF_SPEECH, "Wortart");
        ko(PART_OF_SPEECH, "품사");

        ja(NOTE, "メモ");
        zh(NOTE, "备注");
        fr(NOTE, "Note");
        es(NOTE, "Nota");
        de(NOTE, "Notiz");
        ko(NOTE, "메모");

        ja(NOTES, "メモ");
        zh(NOTES, "备注");
        fr(NOTES, "Notes");
        es(NOTES, "Notas");
        de(NOTES, "Notizen");
        ko(NOTES, "메모");

        ja(KIND, "種類");
        zh(KIND, "种类");
        fr(KIND, "Genre");
        es(KIND, "Tipo");
        de(KIND, "Art");
        ko(KIND, "종류");

        ja(REGISTER, "語域");
        zh(REGISTER, "语域");
        fr(REGISTER, "Registre");
        es(REGISTER, "Registro");
        de(REGISTER, "Register");
        ko(REGISTER, "어체");

        ja(NEUTRAL, "中立");
        zh(NEUTRAL, "中性");
        fr(NEUTRAL, "Neutre");
        es(NEUTRAL, "Neutro");
        de(NEUTRAL, "Neutral");
        ko(NEUTRAL, "중립");

        ja(STATUS, "状態");
        zh(STATUS, "状态");
        fr(STATUS, "Statut");
        es(STATUS, "Estado");
        de(STATUS, "Status");
        ko(STATUS, "상태");

        ja(KIND_ROOT, "語根");
        zh(KIND_ROOT, "词根");
        fr(KIND_ROOT, "Racine");
        es(KIND_ROOT, "Raíz");
        de(KIND_ROOT, "Wurzel");
        ko(KIND_ROOT, "어근");

        ja(KIND_COMPOUND, "複合語");
        zh(KIND_COMPOUND, "复合词");
        fr(KIND_COMPOUND, "Composé");
        es(KIND_COMPOUND, "Compuesto");
        de(KIND_COMPOUND, "Kompositum");
        ko(KIND_COMPOUND, "복합어");

        ja(KIND_AFFIX, "接辞");
        zh(KIND_AFFIX, "词缀");
        fr(KIND_AFFIX, "Affixe");
        es(KIND_AFFIX, "Afijo");
        de(KIND_AFFIX, "Affix");
        ko(KIND_AFFIX, "접사");

        ja(KIND_PARTICLE, "助詞");
        zh(KIND_PARTICLE, "助词");
        fr(KIND_PARTICLE, "Particule");
        es(KIND_PARTICLE, "Partícula");
        de(KIND_PARTICLE, "Partikel");
        ko(KIND_PARTICLE, "조사");

        ja(KIND_NAME, "固有名詞");
        zh(KIND_NAME, "名称");
        fr(KIND_NAME, "Nom propre");
        es(KIND_NAME, "Nombre");
        de(KIND_NAME, "Name");
        ko(KIND_NAME, "이름");

        ja(KIND_NUMBER, "数詞");
        zh(KIND_NUMBER, "数词");
        fr(KIND_NUMBER, "Nombre");
        es(KIND_NUMBER, "Número");
        de(KIND_NUMBER, "Zahl");
        ko(KIND_NUMBER, "수사");

        ja(KIND_PHRASE, "句");
        zh(KIND_PHRASE, "短语");
        fr(KIND_PHRASE, "Locution");
        es(KIND_PHRASE, "Frase");
        de(KIND_PHRASE, "Wendung");
        ko(KIND_PHRASE, "구");

        ja(KIND_OTHER, "その他");
        zh(KIND_OTHER, "其他");
        fr(KIND_OTHER, "Autre");
        es(KIND_OTHER, "Otro");
        de(KIND_OTHER, "Sonstiges");
        ko(KIND_OTHER, "기타");

        ja(STATUS_NEW, "新規");
        zh(STATUS_NEW, "新增");
        fr(STATUS_NEW, "Nouveau");
        es(STATUS_NEW, "Nueva");
        de(STATUS_NEW, "Neu");
        ko(STATUS_NEW, "새로 만들기");

        ja(STATUS_ACCEPTED, "承認済み");
        zh(STATUS_ACCEPTED, "已接受");
        fr(STATUS_ACCEPTED, "Accepté");
        es(STATUS_ACCEPTED, "Aceptada");
        de(STATUS_ACCEPTED, "Übernommen");
        ko(STATUS_ACCEPTED, "수락됨");

        ja(STATUS_LOCKED, "ロック済み");
        zh(STATUS_LOCKED, "已锁定");
        fr(STATUS_LOCKED, "Verrouillé");
        es(STATUS_LOCKED, "Bloqueada");
        de(STATUS_LOCKED, "Gesperrt");
        ko(STATUS_LOCKED, "잠김");

        ja(STATUS_RETIRED, "廃止");
        zh(STATUS_RETIRED, "已停用");
        fr(STATUS_RETIRED, "Retiré");
        es(STATUS_RETIRED, "Retirada");
        de(STATUS_RETIRED, "Ausgemustert");
        ko(STATUS_RETIRED, "폐기됨");

        ja(NEW_WORD, "新しい単語");
        zh(NEW_WORD, "新建词");
        fr(NEW_WORD, "Nouveau mot");
        es(NEW_WORD, "Palabra nueva");
        de(NEW_WORD, "Neues Wort");
        ko(NEW_WORD, "새 단어");

        ja(EDIT_WORD, "単語を編集");
        zh(EDIT_WORD, "编辑词");
        fr(EDIT_WORD, "Modifier le mot");
        es(EDIT_WORD, "Editar la palabra");
        de(EDIT_WORD, "Das Wort bearbeiten");
        ko(EDIT_WORD, "단어 편집");

        ja(NEW_MEANING, "新しい意味");
        zh(NEW_MEANING, "新建词义");
        fr(NEW_MEANING, "Nouveau sens");
        es(NEW_MEANING, "Significado nuevo");
        de(NEW_MEANING, "Neue Bedeutung");
        ko(NEW_MEANING, "새 뜻");

        ja(EDIT_MEANING, "意味を編集");
        zh(EDIT_MEANING, "编辑词义");
        fr(EDIT_MEANING, "Modifier le sens");
        es(EDIT_MEANING, "Editar el significado");
        de(EDIT_MEANING, "Die Bedeutung bearbeiten");
        ko(EDIT_MEANING, "뜻 편집");

        ja(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "独自の文字体系がない場合は空欄");
        zh(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "语言没有自己的文字时留空");
        fr(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "Vide quand la langue n'a pas d'écriture propre");
        es(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "Vacío cuando el idioma no tiene una escritura propia");
        de(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "Leer, wenn die Sprache keine eigene Schrift hat");
        ko(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "언어에 고유한 문자가 없으면 비워 둡니다");

        ja(OTHER_FORMS_OF_THE_SAME_CONCEPT, "同じ概念の他の形");
        zh(OTHER_FORMS_OF_THE_SAME_CONCEPT, "同一概念的其他词形");
        fr(OTHER_FORMS_OF_THE_SAME_CONCEPT, "Autres formes du même concept");
        es(OTHER_FORMS_OF_THE_SAME_CONCEPT, "Otras formas del mismo concepto");
        de(OTHER_FORMS_OF_THE_SAME_CONCEPT, "Andere Formen desselben Konzepts");
        ko(OTHER_FORMS_OF_THE_SAME_CONCEPT, "같은 개념의 다른 형태");

        ja(THE_FORMS_FOR_OTHER_REGISTERS, "この概念が他の語域(丁寧、柔らかい、粗野など)で持つ形");
        zh(THE_FORMS_FOR_OTHER_REGISTERS, "该概念在其他语域(正式、柔和、粗俗等)中的词形");
        fr(THE_FORMS_FOR_OTHER_REGISTERS, "Les formes du concept dans d'autres registres (soutenu, doux, vulgaire...)");
        es(THE_FORMS_FOR_OTHER_REGISTERS, "Las formas que tiene el concepto en otros registros (formal, suave, vulgar...)");
        de(THE_FORMS_FOR_OTHER_REGISTERS, "Die Formen, die das Konzept in anderen Registern hat (förmlich, sanft, vulgär ...)");
        ko(THE_FORMS_FOR_OTHER_REGISTERS, "개념이 다른 어체(격식, 부드러움, 비속어...)에서 가지는 형태");

        ja(WHY_THE_WORD_IS_AS_IT_IS, "この単語がこの形である理由");
        zh(WHY_THE_WORD_IS_AS_IT_IS, "这个词为何如此");
        fr(WHY_THE_WORD_IS_AS_IT_IS, "Pourquoi le mot est tel qu'il est");
        es(WHY_THE_WORD_IS_AS_IT_IS, "Por qué la palabra es como es");
        de(WHY_THE_WORD_IS_AS_IT_IS, "Warum das Wort so ist, wie es ist");
        ko(WHY_THE_WORD_IS_AS_IT_IS, "단어가 이런 형태인 이유");

        ja(RULE_IT_FOLLOWS, "従う規則");
        zh(RULE_IT_FOLLOWS, "遵循的规则");
        fr(RULE_IT_FOLLOWS, "Règle qu'il suit");
        es(RULE_IT_FOLLOWS, "Regla que sigue");
        de(RULE_IT_FOLLOWS, "Regel, der es folgt");
        ko(RULE_IT_FOLLOWS, "따르는 규칙");

        ja(MADE_BY_THE_AUTHOR, "作者が作成");
        zh(MADE_BY_THE_AUTHOR, "作者创造");
        fr(MADE_BY_THE_AUTHOR, "Créé par l'auteur");
        es(MADE_BY_THE_AUTHOR, "Creada por el autor");
        de(MADE_BY_THE_AUTHOR, "Vom Autor erstellt");
        ko(MADE_BY_THE_AUTHOR, "작성자가 만듦");

        ja(MADE_BY_THE_MODEL, "モデル %s が作成。保存すると作者のものになります");
        zh(MADE_BY_THE_MODEL, "由模型 %s 创造;保存后归作者所有");
        fr(MADE_BY_THE_MODEL, "Créé par le modèle %s ; l'enregistrer en fait celui de l'auteur");
        es(MADE_BY_THE_MODEL, "Creada por el modelo %s; al guardar pasa a ser del autor");
        de(MADE_BY_THE_MODEL, "Vom Modell %s erstellt; durch Speichern wird es zu einem Wort des Autors");
        ko(MADE_BY_THE_MODEL, "모델 %s이(가) 만듦. 저장하면 작성자의 것이 됩니다");

        ja(THE_WORD_IS_EMPTY, "単語が空です");
        zh(THE_WORD_IS_EMPTY, "词为空");
        fr(THE_WORD_IS_EMPTY, "Le mot est vide");
        es(THE_WORD_IS_EMPTY, "La palabra está vacía");
        de(THE_WORD_IS_EMPTY, "Das Wort ist leer");
        ko(THE_WORD_IS_EMPTY, "단어가 비어 있습니다");

        ja(A_WORD_NEEDS_A_MEANING, "単語には少なくとも1つの意味が必要です");
        zh(A_WORD_NEEDS_A_MEANING, "一个词至少需要一个词义");
        fr(A_WORD_NEEDS_A_MEANING, "Un mot a besoin d'au moins un sens");
        es(A_WORD_NEEDS_A_MEANING, "Una palabra necesita al menos un significado");
        de(A_WORD_NEEDS_A_MEANING, "Ein Wort braucht mindestens eine Bedeutung");
        ko(A_WORD_NEEDS_A_MEANING, "단어에는 뜻이 하나 이상 필요합니다");

        ja(THE_MEANING_IS_EMPTY, "意味が空です");
        zh(THE_MEANING_IS_EMPTY, "词义为空");
        fr(THE_MEANING_IS_EMPTY, "Le sens est vide");
        es(THE_MEANING_IS_EMPTY, "El significado está vacío");
        de(THE_MEANING_IS_EMPTY, "Die Bedeutung ist leer");
        ko(THE_MEANING_IS_EMPTY, "뜻이 비어 있습니다");

        ja(D_WORDS_D_NEW, "%d 語、新規 %d");
        zh(D_WORDS_D_NEW, "%d 个词,新增 %d");
        fr(D_WORDS_D_NEW, "%d mots, %d nouveaux");
        es(D_WORDS_D_NEW, "%d palabras, %d nuevas");
        de(D_WORDS_D_NEW, "%d Wörter, %d neu");
        ko(D_WORDS_D_NEW, "단어 %d개, 새 단어 %d개");

        ja(SELECT_ALL, "すべて選択");
        zh(SELECT_ALL, "全选");
        fr(SELECT_ALL, "Tout sélectionner");
        es(SELECT_ALL, "Seleccionar todo");
        de(SELECT_ALL, "Alle auswählen");
        ko(SELECT_ALL, "모두 선택");

        ja(SELECT_NONE, "選択解除");
        zh(SELECT_NONE, "全不选");
        fr(SELECT_NONE, "Ne rien sélectionner");
        es(SELECT_NONE, "No seleccionar nada");
        de(SELECT_NONE, "Keine auswählen");
        ko(SELECT_NONE, "선택 해제");

        ja(SELECT_WITHOUT_CONFLICTS, "競合のないものを選択");
        zh(SELECT_WITHOUT_CONFLICTS, "选择无冲突项");
        fr(SELECT_WITHOUT_CONFLICTS, "Sélectionner sans conflits");
        es(SELECT_WITHOUT_CONFLICTS, "Seleccionar sin conflictos");
        de(SELECT_WITHOUT_CONFLICTS, "Ohne Konflikte auswählen");
        ko(SELECT_WITHOUT_CONFLICTS, "충돌 없는 것 선택");

        ja(EVERYTHING, "すべて");
        zh(EVERYTHING, "全部");
        fr(EVERYTHING, "Tout");
        es(EVERYTHING, "Todo");
        de(EVERYTHING, "Alles");
        ko(EVERYTHING, "전체");

        ja(TARGET_LANGUAGE, "言語");
        zh(TARGET_LANGUAGE, "语言");
        fr(TARGET_LANGUAGE, "Langue");
        es(TARGET_LANGUAGE, "Idioma");
        de(TARGET_LANGUAGE, "Sprache");
        ko(TARGET_LANGUAGE, "언어");

        ja(TARGET_RULE, "規則セクション");
        zh(TARGET_RULE, "规则小节");
        fr(TARGET_RULE, "Section de règles");
        es(TARGET_RULE, "Sección de reglas");
        de(TARGET_RULE, "Regelabschnitt");
        ko(TARGET_RULE, "규칙 섹션");

        ja(TARGET_CATEGORY, "カテゴリ");
        zh(TARGET_CATEGORY, "类别");
        fr(TARGET_CATEGORY, "Catégorie");
        es(TARGET_CATEGORY, "Categoría");
        de(TARGET_CATEGORY, "Kategorie");
        ko(TARGET_CATEGORY, "범주");

        ja(TARGET_REGISTER, "語域");
        zh(TARGET_REGISTER, "语域");
        fr(TARGET_REGISTER, "Registre");
        es(TARGET_REGISTER, "Registro");
        de(TARGET_REGISTER, "Register");
        ko(TARGET_REGISTER, "어체");

        ja(TARGET_WORD, "単語");
        zh(TARGET_WORD, "词");
        fr(TARGET_WORD, "Mot");
        es(TARGET_WORD, "Palabra");
        de(TARGET_WORD, "Wort");
        ko(TARGET_WORD, "단어");

        ja(KIND_ADD, "追加");
        zh(KIND_ADD, "新增");
        fr(KIND_ADD, "Ajout");
        es(KIND_ADD, "Añadir");
        de(KIND_ADD, "Hinzufügen");
        ko(KIND_ADD, "추가");

        ja(KIND_CHANGE, "変更");
        zh(KIND_CHANGE, "修改");
        fr(KIND_CHANGE, "Modification");
        es(KIND_CHANGE, "Cambiar");
        de(KIND_CHANGE, "Ändern");
        ko(KIND_CHANGE, "변경");

        ja(KIND_DELETE, "削除");
        zh(KIND_DELETE, "删除");
        fr(KIND_DELETE, "Suppression");
        es(KIND_DELETE, "Eliminar");
        de(KIND_DELETE, "Löschen");
        ko(KIND_DELETE, "삭제");

        ja(KIND_MOVE, "移動");
        zh(KIND_MOVE, "移动");
        fr(KIND_MOVE, "Déplacement");
        es(KIND_MOVE, "Mover");
        de(KIND_MOVE, "Verschieben");
        ko(KIND_MOVE, "이동");

        ja(CHANGE, "変更");
        zh(CHANGE, "变更");
        fr(CHANGE, "Modification");
        es(CHANGE, "Cambio");
        de(CHANGE, "Änderung");
        ko(CHANGE, "변경");

        ja(ABOUT, "対象");
        zh(ABOUT, "对象");
        fr(ABOUT, "Concerne");
        es(ABOUT, "Sobre");
        de(ABOUT, "Über");
        ko(ABOUT, "정보");

        ja(FIELDS, "項目");
        zh(FIELDS, "字段");
        fr(FIELDS, "Champs");
        es(FIELDS, "Campos");
        de(FIELDS, "Felder");
        ko(FIELDS, "필드");

        ja(CONFLICT_TEXT, "競合");
        zh(CONFLICT_TEXT, "冲突");
        fr(CONFLICT_TEXT, "Conflit");
        es(CONFLICT_TEXT, "Conflicto");
        de(CONFLICT_TEXT, "Konflikt");
        ko(CONFLICT_TEXT, "충돌");

        ja(D_CHANGES_D_IN_CONFLICT, "変更 %d 件、競合 %d 件");
        zh(D_CHANGES_D_IN_CONFLICT, "%d 项变更,%d 项冲突");
        fr(D_CHANGES_D_IN_CONFLICT, "%d modification(s), %d en conflit");
        es(D_CHANGES_D_IN_CONFLICT, "%d cambio(s), %d en conflicto");
        de(D_CHANGES_D_IN_CONFLICT, "%d Änderung(en), %d im Konflikt");
        ko(D_CHANGES_D_IN_CONFLICT, "변경 %d건, 충돌 %d건");

        ja(APPLY_D_PICKED, "選択した %d 件を適用");
        zh(APPLY_D_PICKED, "应用所选的 %d 项");
        fr(APPLY_D_PICKED, "Appliquer les %d choisies");
        es(APPLY_D_PICKED, "Aplicar los %d elegidos");
        de(APPLY_D_PICKED, "%d gewählte anwenden");
        ko(APPLY_D_PICKED, "선택한 %d건 적용");

        ja(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "変更を選択すると項目が表示されます。");
        zh(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "选择一项变更以查看其字段。");
        fr(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "Sélectionnez une modification pour voir ses champs.");
        es(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "Selecciona un cambio para ver sus campos.");
        de(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "Wähle eine Änderung, um ihre Felder zu sehen.");
        ko(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "변경을 선택하면 필드가 표시됩니다.");

        ja(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "この変更は別の変更(使用する新しいカテゴリや語域)を必要とします。片方を選ぶともう片方も選ばれます。");
        zh(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "此变更需要另一项变更(它使用的新类别或语域):选择其一会同时选择另一项。");
        fr(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "Cette modification a besoin d'une autre modification (une nouvelle catégorie ou un nouveau registre qu'elle utilise) : choisir l'une choisit l'autre.");
        es(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "Este cambio necesita otro cambio (una categoría o un registro nuevo que usa): elegir uno elige el otro.");
        de(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "Diese Änderung braucht eine weitere Änderung (eine neue Kategorie oder ein neues Register, das sie verwendet): Wer eine wählt, wählt die andere mit.");
        ko(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "이 변경에는 다른 변경(사용하는 새 범주나 어체)이 필요합니다: 하나를 선택하면 다른 하나도 선택됩니다.");

        ja(THE_THING_IS_DELETED, "これを選ぶと、こちらからも削除されます。");
        zh(THE_THING_IS_DELETED, "选择此项会在这里删除它。");
        fr(THE_THING_IS_DELETED, "Choisir ceci le supprime ici.");
        es(THE_THING_IS_DELETED, "Elegir esto lo elimina aquí.");
        de(THE_THING_IS_DELETED, "Wer dies wählt, löscht es hier.");
        ko(THE_THING_IS_DELETED, "이것을 선택하면 여기서 삭제됩니다.");

        ja(THE_THING_IS_MOVED, "これを選ぶと、もう一方の言語での位置に移動します。");
        zh(THE_THING_IS_MOVED, "选择此项会把它移到在另一种语言中的位置。");
        fr(THE_THING_IS_MOVED, "Choisir ceci le déplace à la place qu'il occupe dans l'autre langue.");
        es(THE_THING_IS_MOVED, "Elegir esto lo mueve al lugar que tiene en el otro idioma.");
        de(THE_THING_IS_MOVED, "Wer dies wählt, verschiebt es an die Stelle, die es in der anderen Sprache hat.");
        ko(THE_THING_IS_MOVED, "이것을 선택하면 다른 언어에서 있던 위치로 이동합니다.");

        ja(BOTH_CHANGED_THIS, "両方が変更");
        zh(BOTH_CHANGED_THIS, "双方都修改了");
        fr(BOTH_CHANGED_THIS, "modifié des deux côtés");
        es(BOTH_CHANGED_THIS, "ambos lo cambiaron");
        de(BOTH_CHANGED_THIS, "beide haben es geändert");
        ko(BOTH_CHANGED_THIS, "양쪽 모두 변경함");

        ja(NOTHING, "なし");
        zh(NOTHING, "无");
        fr(NOTHING, "Rien");
        es(NOTHING, "Nada");
        de(NOTHING, "Nichts");
        ko(NOTHING, "없음");

        ja(HERE_NOW, "こちら(現在)");
        zh(HERE_NOW, "这里(当前)");
        fr(HERE_NOW, "Ici, maintenant");
        es(HERE_NOW, "Aquí, ahora");
        de(HERE_NOW, "Hier, jetzt");
        ko(HERE_NOW, "여기, 현재");

        ja(IN_THE_OTHER_LANGUAGE, "もう一方の言語");
        zh(IN_THE_OTHER_LANGUAGE, "另一种语言中");
        fr(IN_THE_OTHER_LANGUAGE, "Dans l'autre langue");
        es(IN_THE_OTHER_LANGUAGE, "En el otro idioma");
        de(IN_THE_OTHER_LANGUAGE, "In der anderen Sprache");
        ko(IN_THE_OTHER_LANGUAGE, "다른 언어");

        ja(WILL_BE_WRITTEN, "書き込まれる内容");
        zh(WILL_BE_WRITTEN, "将写入的内容");
        fr(WILL_BE_WRITTEN, "Sera écrit");
        es(WILL_BE_WRITTEN, "Se escribirá");
        de(WILL_BE_WRITTEN, "Wird geschrieben");
        ko(WILL_BE_WRITTEN, "기록될 내용");

        ja(KEEP_MINE, "こちらを残す");
        zh(KEEP_MINE, "保留我的");
        fr(KEEP_MINE, "Garder le mien");
        es(KEEP_MINE, "Conservar el mío");
        de(KEEP_MINE, "Meins behalten");
        ko(KEEP_MINE, "내 것 유지");

        ja(TAKE_THEIRS, "相手を採用");
        zh(TAKE_THEIRS, "采用对方的");
        fr(TAKE_THEIRS, "Prendre l'autre");
        es(TAKE_THEIRS, "Tomar el suyo");
        de(TAKE_THEIRS, "Das andere übernehmen");
        ko(TAKE_THEIRS, "상대 것 사용");

        ja(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "一部の項目が両側で変更されています");
        zh(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "部分字段在双方都被修改");
        fr(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "Certains champs ont été modifiés des deux côtés");
        es(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "Algunos campos cambiaron en ambos lados");
        de(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "Einige Felder wurden auf beiden Seiten geändert");
        ko(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "일부 필드가 양쪽에서 변경되었습니다");

        ja(CHANGED_HERE_DELETED_THERE_TEXT, "こちらで変更、もう一方で削除");
        zh(CHANGED_HERE_DELETED_THERE_TEXT, "这里修改了,另一种语言中删除了");
        fr(CHANGED_HERE_DELETED_THERE_TEXT, "Modifié ici, supprimé dans l'autre langue");
        es(CHANGED_HERE_DELETED_THERE_TEXT, "Cambiado aquí, eliminado en el otro idioma");
        de(CHANGED_HERE_DELETED_THERE_TEXT, "Hier geändert, in der anderen Sprache gelöscht");
        ko(CHANGED_HERE_DELETED_THERE_TEXT, "여기서 변경되고 다른 언어에서 삭제됨");

        ja(DELETED_HERE_CHANGED_THERE_TEXT, "こちらで削除、もう一方で変更。選ぶと復元されます");
        zh(DELETED_HERE_CHANGED_THERE_TEXT, "这里删除了,另一种语言中修改了:选择它会恢复");
        fr(DELETED_HERE_CHANGED_THERE_TEXT, "Supprimé ici, modifié dans l'autre langue : le choisir le ramène");
        es(DELETED_HERE_CHANGED_THERE_TEXT, "Eliminado aquí, cambiado en el otro idioma: elegirlo lo recupera");
        de(DELETED_HERE_CHANGED_THERE_TEXT, "Hier gelöscht, in der anderen Sprache geändert: Wer es wählt, bringt es zurück");
        ko(DELETED_HERE_CHANGED_THERE_TEXT, "여기서 삭제되고 다른 언어에서 변경됨: 선택하면 되살아납니다");

        ja(THE_SAME_WORD_IS_HERE, "同じ語形で共通の意味を持つ単語が既にあります");
        zh(THE_SAME_WORD_IS_HERE, "这里已有形式相同且词义相通的词");
        fr(THE_SAME_WORD_IS_HERE, "Le même mot avec un sens commun est déjà ici");
        es(THE_SAME_WORD_IS_HERE, "Aquí ya está la misma palabra con un significado compartido");
        de(THE_SAME_WORD_IS_HERE, "Dasselbe Wort mit einer gemeinsamen Bedeutung ist hier bereits vorhanden");
        ko(THE_SAME_WORD_IS_HERE, "뜻을 공유하는 같은 단어가 이미 여기 있습니다");

        ja(MOVED_ON_BOTH_SIDES, "両側で移動されています");
        zh(MOVED_ON_BOTH_SIDES, "双方都移动了");
        fr(MOVED_ON_BOTH_SIDES, "Déplacé des deux côtés");
        es(MOVED_ON_BOTH_SIDES, "Movido en ambos lados");
        de(MOVED_ON_BOTH_SIDES, "Auf beiden Seiten verschoben");
        ko(MOVED_ON_BOTH_SIDES, "양쪽에서 이동됨");

        ja(COMPARE, "比較");
        zh(COMPARE, "比较");
        fr(COMPARE, "Comparer");
        es(COMPARE, "Comparar");
        de(COMPARE, "Vergleichen");
        ko(COMPARE, "비교");

        ja(BRING_CHANGES_FROM, "変更の取り込み元");
        zh(BRING_CHANGES_FROM, "从以下语言带入变更");
        fr(BRING_CHANGES_FROM, "Reprendre les modifications de");
        es(BRING_CHANGES_FROM, "Traer los cambios de");
        de(BRING_CHANGES_FROM, "Die Änderungen übernehmen aus");
        ko(BRING_CHANGES_FROM, "변경 사항을 가져올 곳");

        ja(COMPARE_S, "%s を別の言語と比較");
        zh(COMPARE_S, "将 %s 与另一种语言比较");
        fr(COMPARE_S, "Comparer %s avec une autre langue");
        es(COMPARE_S, "Comparar %s con otro idioma");
        de(COMPARE_S, "%s mit einer anderen Sprache vergleichen");
        ko(COMPARE_S, "%s을(를) 다른 언어와 비교");

        ja(NO_OTHER_LANGUAGE, "比較できる他の言語がありません。");
        zh(NO_OTHER_LANGUAGE, "没有可比较的其他语言。");
        fr(NO_OTHER_LANGUAGE, "Il n'y a pas d'autre langue avec laquelle comparer.");
        es(NO_OTHER_LANGUAGE, "No hay otro idioma con el que comparar.");
        de(NO_OTHER_LANGUAGE, "Es gibt keine andere Sprache zum Vergleichen.");
        ko(NO_OTHER_LANGUAGE, "비교할 다른 언어가 없습니다.");

        ja(THE_LANGUAGES_ARE_THE_SAME, "2つの言語は同じです。");
        zh(THE_LANGUAGES_ARE_THE_SAME, "两种语言相同。");
        fr(THE_LANGUAGES_ARE_THE_SAME, "Les deux langues sont identiques.");
        es(THE_LANGUAGES_ARE_THE_SAME, "Los dos idiomas son iguales.");
        de(THE_LANGUAGES_ARE_THE_SAME, "Die beiden Sprachen sind gleich.");
        ko(THE_LANGUAGES_ARE_THE_SAME, "두 언어가 같습니다.");

        ja(BRING_THE_CHANGES_OF_S_INTO_S, "%s の変更を %s に取り込む");
        zh(BRING_THE_CHANGES_OF_S_INTO_S, "把 %s 的变更带入 %s");
        fr(BRING_THE_CHANGES_OF_S_INTO_S, "Reprendre les modifications de %s dans %s");
        es(BRING_THE_CHANGES_OF_S_INTO_S, "Traer los cambios de %s a %s");
        de(BRING_THE_CHANGES_OF_S_INTO_S, "Die Änderungen von %s in %s übernehmen");
        ko(BRING_THE_CHANGES_OF_S_INTO_S, "%s의 변경 사항을 %s(으)로 가져오기");

        ja(APPLIED_D_CHANGES, "変更を %d 件適用しました");
        zh(APPLIED_D_CHANGES, "已应用 %d 项变更");
        fr(APPLIED_D_CHANGES, "%d modification(s) appliquée(s)");
        es(APPLIED_D_CHANGES, "Se han aplicado %d cambio(s)");
        de(APPLIED_D_CHANGES, "%d Änderung(en) angewendet");
        ko(APPLIED_D_CHANGES, "변경 %d건을 적용했습니다");

        ja(SKIPPED_D_CHANGED_MEANWHILE, "%d 件の変更はスキップされました。言語がレビュー時と異なります");
        zh(SKIPPED_D_CHANGED_MEANWHILE, "已跳过 %d 项变更:语言与审阅时不同");
        fr(SKIPPED_D_CHANGED_MEANWHILE, "%d modification(s) ignorée(s) : la langue n'est plus ce que l'examen a vu");
        es(SKIPPED_D_CHANGED_MEANWHILE, "Se omitieron %d cambio(s): el idioma no es lo que vio la revisión");
        de(SKIPPED_D_CHANGED_MEANWHILE, "%d Änderung(en) wurden übersprungen: Die Sprache ist nicht das, was die Prüfung gesehen hat");
        ko(SKIPPED_D_CHANGED_MEANWHILE, "변경 %d건을 건너뛰었습니다: 언어가 검토 시점과 다릅니다");

        ja(SKIPPED_D_NEED_ANOTHER, "%d 件の変更はスキップされました。必要な変更が選ばれていません");
        zh(SKIPPED_D_NEED_ANOTHER, "已跳过 %d 项变更:它们所需的变更未被选择");
        fr(SKIPPED_D_NEED_ANOTHER, "%d modification(s) ignorée(s) : ce dont elles ont besoin n'a pas été choisi");
        es(SKIPPED_D_NEED_ANOTHER, "Se omitieron %d cambio(s): no se eligió lo que necesitan");
        de(SKIPPED_D_NEED_ANOTHER, "%d Änderung(en) wurden übersprungen: Was sie brauchen, wurde nicht gewählt");
        ko(SKIPPED_D_NEED_ANOTHER, "변경 %d건을 건너뛰었습니다: 필요한 것이 선택되지 않았습니다");

        ja(A_LANGUAGE_NAMED_S_EXISTS, "%s という言語は既にライブラリにあります。差分を確認して必要なものだけ取り込みますか?それとも新しい言語としてインポートしますか?");
        zh(A_LANGUAGE_NAMED_S_EXISTS, "你的库中已有名为 %s 的语言。要审阅差异并采纳需要的部分,还是作为新语言导入?");
        fr(A_LANGUAGE_NAMED_S_EXISTS, "Une langue nommée %s est déjà dans votre bibliothèque. Examiner ses différences et reprendre ce que vous voulez, ou importer celle-ci comme nouvelle langue ?");
        es(A_LANGUAGE_NAMED_S_EXISTS, "Ya hay un idioma llamado %s en tu biblioteca. ¿Revisar sus diferencias y tomar lo que quieras, o importar este como un idioma nuevo?");
        de(A_LANGUAGE_NAMED_S_EXISTS, "Eine Sprache namens %s ist bereits in deiner Bibliothek. Die Unterschiede prüfen und übernehmen, was du möchtest, oder diese als neue Sprache importieren?");
        ko(A_LANGUAGE_NAMED_S_EXISTS, "%s이라는 언어가 이미 내 라이브러리에 있습니다. 차이점을 검토하여 원하는 것을 가져오시겠습니까, 아니면 이것을 새 언어로 가져오시겠습니까?");

        ja(REVIEW_THE_DIFFERENCES, "差分を確認");
        zh(REVIEW_THE_DIFFERENCES, "审阅差异");
        fr(REVIEW_THE_DIFFERENCES, "Examiner les différences");
        es(REVIEW_THE_DIFFERENCES, "Revisar las diferencias");
        de(REVIEW_THE_DIFFERENCES, "Die Unterschiede prüfen");
        ko(REVIEW_THE_DIFFERENCES, "차이점 검토");

        ja(IMPORT_AS_NEW, "新規としてインポート");
        zh(IMPORT_AS_NEW, "作为新语言导入");
        fr(IMPORT_AS_NEW, "Importer comme nouvelle");
        es(IMPORT_AS_NEW, "Importar como nuevo");
        de(IMPORT_AS_NEW, "Als neu importieren");
        ko(IMPORT_AS_NEW, "새로 가져오기");

        ja(ACCEPTED_IN_THE_RULES, "承認しました。セクションが規則に追加されました。");
        zh(ACCEPTED_IN_THE_RULES, "已接受:该小节已加入规则。");
        fr(ACCEPTED_IN_THE_RULES, "Acceptée : la section est dans les règles.");
        es(ACCEPTED_IN_THE_RULES, "Aceptada: la sección está en las reglas.");
        de(ACCEPTED_IN_THE_RULES, "Übernommen: Der Abschnitt steht in den Regeln.");
        ko(ACCEPTED_IN_THE_RULES, "수락됨: 섹션이 규칙에 있습니다.");

        ja(ADD_A_SECTION_WITH_AI, "AI でセクションを追加...");
        zh(ADD_A_SECTION_WITH_AI, "用 AI 添加小节...");
        fr(ADD_A_SECTION_WITH_AI, "Ajouter une section avec l'IA...");
        es(ADD_A_SECTION_WITH_AI, "Añadir una sección con IA...");
        de(ADD_A_SECTION_WITH_AI, "Einen Abschnitt mit KI hinzufügen ...");
        ko(ADD_A_SECTION_WITH_AI, "AI로 섹션 추가...");

        ja(ADD_THE_BASIC_CATEGORIES, "基本カテゴリを追加");
        zh(ADD_THE_BASIC_CATEGORIES, "添加基本类别");
        fr(ADD_THE_BASIC_CATEGORIES, "Ajouter les catégories de base");
        es(ADD_THE_BASIC_CATEGORIES, "Añadir las categorías básicas");
        de(ADD_THE_BASIC_CATEGORIES, "Die Grundkategorien hinzufügen");
        ko(ADD_THE_BASIC_CATEGORIES, "기본 범주 추가");

        ja(ASK_ABOUT_THE_LANGUAGE, "言語について質問する、または変更を依頼...");
        zh(ASK_ABOUT_THE_LANGUAGE, "询问这门语言,或请求修改...");
        fr(ASK_ABOUT_THE_LANGUAGE, "Posez une question sur la langue, ou demandez une modification...");
        es(ASK_ABOUT_THE_LANGUAGE, "Pregunta sobre el idioma, o pide un cambio...");
        de(ASK_ABOUT_THE_LANGUAGE, "Frag etwas zur Sprache oder bitte um eine Änderung ...");
        ko(ASK_ABOUT_THE_LANGUAGE, "언어에 대해 질문하거나 변경을 요청하세요...");

        ja(ASK_THE_DESIGNER, "デザイナーに質問...");
        zh(ASK_THE_DESIGNER, "询问设计器...");
        fr(ASK_THE_DESIGNER, "Demander au concepteur...");
        es(ASK_THE_DESIGNER, "Pregunta al diseñador...");
        de(ASK_THE_DESIGNER, "Den Designer fragen ...");
        ko(ASK_THE_DESIGNER, "디자이너에게 질문...");

        ja(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "まだ存在しない名前を入力すると新しいカテゴリが作られます。");
        zh(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "输入尚不存在的名称会新建类别。");
        fr(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "Un nom qui n'est pas encore une catégorie en crée une nouvelle.");
        es(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "Un nombre que todavía no es una categoría crea una nueva.");
        de(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "Ein Name, der noch keine Kategorie ist, erzeugt eine neue.");
        ko(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "아직 범주가 아닌 이름은 새 범주를 만듭니다.");

        ja(BACK, "戻る");
        zh(BACK, "上一步");
        fr(BACK, "Retour");
        es(BACK, "Atrás");
        de(BACK, "Zurück");
        ko(BACK, "뒤로");

        ja(BRIEF, "概要書き");
        zh(BRIEF, "简述");
        fr(BRIEF, "Cahier des charges");
        es(BRIEF, "Descripción del encargo");
        de(BRIEF, "Auftrag");
        ko(BRIEF, "브리프");

        ja(CHOOSE_A_SECTION, "セクションを選択してください");
        zh(CHOOSE_A_SECTION, "请选择小节");
        fr(CHOOSE_A_SECTION, "Choisissez une section");
        es(CHOOSE_A_SECTION, "Elige una sección");
        de(CHOOSE_A_SECTION, "Wähle einen Abschnitt");
        ko(CHOOSE_A_SECTION, "섹션 선택");

        ja(CONSTRAINTS_AND_WHAT_TO_AVOID, "制約と避けたいこと");
        zh(CONSTRAINTS_AND_WHAT_TO_AVOID, "限制与要避免的内容");
        fr(CONSTRAINTS_AND_WHAT_TO_AVOID, "Contraintes et ce qu'il faut éviter");
        es(CONSTRAINTS_AND_WHAT_TO_AVOID, "Restricciones y lo que se debe evitar");
        de(CONSTRAINTS_AND_WHAT_TO_AVOID, "Einschränkungen und was zu vermeiden ist");
        ko(CONSTRAINTS_AND_WHAT_TO_AVOID, "제약 조건과 피할 것");

        ja(DESIGN_A_LANGUAGE, "言語をデザイン");
        zh(DESIGN_A_LANGUAGE, "设计语言");
        fr(DESIGN_A_LANGUAGE, "Concevoir une langue");
        es(DESIGN_A_LANGUAGE, "Diseñar un idioma");
        de(DESIGN_A_LANGUAGE, "Eine Sprache entwerfen");
        ko(DESIGN_A_LANGUAGE, "언어 설계");

        ja(DESIGN_WITH_AI, "AI でデザイン");
        zh(DESIGN_WITH_AI, "用 AI 设计");
        fr(DESIGN_WITH_AI, "Concevoir avec l'IA");
        es(DESIGN_WITH_AI, "Diseñar con IA");
        de(DESIGN_WITH_AI, "Mit KI entwerfen");
        ko(DESIGN_WITH_AI, "AI로 설계");

        ja(DISCARD_THE_CHANGES, "言語への変更を破棄しますか?");
        zh(DISCARD_THE_CHANGES, "放弃对语言的修改?");
        fr(DISCARD_THE_CHANGES, "Abandonner les modifications de la langue ?");
        es(DISCARD_THE_CHANGES, "¿Descartar los cambios del idioma?");
        de(DISCARD_THE_CHANGES, "Die Änderungen an der Sprache verwerfen?");
        ko(DISCARD_THE_CHANGES, "언어에 대한 변경 사항을 버리시겠습니까?");
        ja(DISCARD_THE_NEW_LANGUAGE, "新しい言語とここまでの作業を破棄しますか?");
        zh(DISCARD_THE_NEW_LANGUAGE, "放弃新语言及目前的成果?");
        fr(DISCARD_THE_NEW_LANGUAGE, "Abandonner la nouvelle langue et ce qui a été fait jusqu'ici ?");
        es(DISCARD_THE_NEW_LANGUAGE, "¿Descartar el idioma nuevo y lo que se ha hecho hasta ahora?");
        de(DISCARD_THE_NEW_LANGUAGE, "Die neue Sprache und das bisher Erstellte verwerfen?");
        ko(DISCARD_THE_NEW_LANGUAGE, "새 언어와 지금까지 만든 것을 버리시겠습니까?");

        ja(EDIT_THE_PROPOSAL_YOURSELF, "テキストを自分で編集するか、上にメモを書いて再提案させて練り直せます。");
        zh(EDIT_THE_PROPOSAL_YOURSELF, "可自行编辑文本,或在上方写备注后再次提议以改进。");
        fr(EDIT_THE_PROPOSAL_YOURSELF, "Modifiez le texte vous-même, ou écrivez une note ci-dessus et proposez à nouveau pour l'affiner.");
        es(EDIT_THE_PROPOSAL_YOURSELF, "Edita tú mismo el texto, o escribe una nota arriba y vuelve a proponer para refinarlo.");
        de(EDIT_THE_PROPOSAL_YOURSELF, "Bearbeite den Text selbst oder schreibe oben eine Notiz und schlage erneut vor, um ihn zu verfeinern.");
        ko(EDIT_THE_PROPOSAL_YOURSELF, "텍스트를 직접 편집하거나, 위에 메모를 쓰고 다시 제안받아 다듬으세요.");

        ja(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "空欄の場合: 最初の提案は概要書きから作られます。下に提案がある場合、メモは変更点を伝えます。");
        zh(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "留空:首次提议来自简述。下方已有提议时,备注说明要改什么。");
        fr(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "Vide : la première proposition vient du cahier des charges. Avec une proposition ci-dessous, la note dit quoi changer.");
        es(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "Vacío: la primera propuesta sale de la descripción del encargo. Con una propuesta abajo, la nota indica qué cambiar.");
        de(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "Leer: Der erste Vorschlag kommt aus dem Auftrag. Bei einem Vorschlag unten sagt die Notiz, was geändert werden soll.");
        ko(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "비워 두면: 첫 제안은 브리프에서 나옵니다. 아래에 제안이 있으면 메모가 바꿀 내용을 알려 줍니다.");

        ja(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "空欄の場合: 翻訳用のモデル、なければ既定のモデル。");
        zh(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "留空:使用翻译所用的模型,否则使用你的默认模型。");
        fr(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "Vide : le modèle de la traduction, sinon votre modèle par défaut.");
        es(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "Vacío: el modelo de la traducción o, si no, tu modelo predeterminado.");
        de(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "Leer: das Modell der Übersetzung, sonst dein Standardmodell.");
        ko(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "비워 두면: 번역용 모델, 없으면 내 기본 모델.");

        ja(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "空欄の場合: 翻訳用のプロトコル、なければ既定のプロトコル。");
        zh(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "留空:使用翻译所用的协议,否则使用你的默认协议。");
        fr(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "Vide : le protocole de la traduction, sinon votre protocole par défaut.");
        es(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "Vacío: el protocolo de la traducción o, si no, tu protocolo predeterminado.");
        de(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "Leer: das Protokoll der Übersetzung, sonst dein Standardprotokoll.");
        ko(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "비워 두면: 번역용 프로토콜, 없으면 내 기본 프로토콜.");

        ja(FINISH, "完了");
        zh(FINISH, "完成");
        fr(FINISH, "Terminer");
        es(FINISH, "Finalizar");
        de(FINISH, "Abschließen");
        ko(FINISH, "완료");

        ja(FIX, "修正案");
        zh(FIX, "修复建议");
        fr(FIX, "Corriger");
        es(FIX, "Corregir");
        de(FIX, "Beheben");
        ko(FIX, "수정");

        ja(GENERATE_WORDS, "単語を生成...");
        zh(GENERATE_WORDS, "生成词汇...");
        fr(GENERATE_WORDS, "Générer des mots...");
        es(GENERATE_WORDS, "Generar palabras...");
        de(GENERATE_WORDS, "Wörter generieren ...");
        ko(GENERATE_WORDS, "단어 생성...");

        ja(HARSH_LIKE_OLD_NORSE, "例: 古ノルド語のように荒々しい、ポリネシア語のように歌うような。");
        zh(HARSH_LIKE_OLD_NORSE, "例如:像古诺尔斯语一样粗犷;像波利尼西亚语一样如歌。");
        fr(HARSH_LIKE_OLD_NORSE, "Par exemple : rude, comme le vieux norrois ; chantante, à la polynésienne.");
        es(HARSH_LIKE_OLD_NORSE, "Por ejemplo: áspero, como el nórdico antiguo; cantarín, como el polinesio.");
        de(HARSH_LIKE_OLD_NORSE, "Zum Beispiel: hart, wie Altnordisch; singend, wie polynesische Sprachen.");
        ko(HARSH_LIKE_OLD_NORSE, "예: 고대 노르드어처럼 거친 느낌, 폴리네시아어 같은 노래하는 듯한 느낌.");

        ja(HOW_MANY_WORDS, "単語数");
        zh(HOW_MANY_WORDS, "词数");
        fr(HOW_MANY_WORDS, "Combien de mots");
        es(HOW_MANY_WORDS, "Cuántas palabras");
        de(HOW_MANY_WORDS, "Wie viele Wörter");
        ko(HOW_MANY_WORDS, "단어 수");

        ja(INSPIRATIONS, "着想元");
        zh(INSPIRATIONS, "灵感来源");
        fr(INSPIRATIONS, "Inspirations");
        es(INSPIRATIONS, "Inspiraciones");
        de(INSPIRATIONS, "Inspirationen");
        ko(INSPIRATIONS, "영감");

        ja(IN_THE_LANGUAGE, "言語での表記");
        zh(IN_THE_LANGUAGE, "该语言中的写法");
        fr(IN_THE_LANGUAGE, "Dans la langue");
        es(IN_THE_LANGUAGE, "En el idioma");
        de(IN_THE_LANGUAGE, "In der Sprache");
        ko(IN_THE_LANGUAGE, "언어 내");

        ja(LEFT_OUT_OF_THE_PROPOSAL, "提案から除外されたもの:");
        zh(LEFT_OUT_OF_THE_PROPOSAL, "未纳入提议的内容:");
        fr(LEFT_OUT_OF_THE_PROPOSAL, "Omis de la proposition :");
        es(LEFT_OUT_OF_THE_PROPOSAL, "Omitido de la propuesta:");
        de(LEFT_OUT_OF_THE_PROPOSAL, "Aus dem Vorschlag weggelassen:");
        ko(LEFT_OUT_OF_THE_PROPOSAL, "제안에서 제외됨:");

        ja(MODEL_OF_THE_DESIGNER, "デザイナーに使うモデル");
        zh(MODEL_OF_THE_DESIGNER, "设计器所用的模型");
        fr(MODEL_OF_THE_DESIGNER, "Modèle du concepteur");
        es(MODEL_OF_THE_DESIGNER, "Modelo del diseñador");
        de(MODEL_OF_THE_DESIGNER, "Modell des Designers");
        ko(MODEL_OF_THE_DESIGNER, "디자이너의 모델");

        ja(NEW_WORDS_OF_THE_SAMPLES, "サンプルの新しい単語");
        zh(NEW_WORDS_OF_THE_SAMPLES, "示例中的新词");
        fr(NEW_WORDS_OF_THE_SAMPLES, "Nouveaux mots des exemples");
        es(NEW_WORDS_OF_THE_SAMPLES, "Palabras nuevas de las muestras");
        de(NEW_WORDS_OF_THE_SAMPLES, "Neue Wörter der Beispiele");
        ko(NEW_WORDS_OF_THE_SAMPLES, "샘플의 새 단어");

        ja(NEXT, "次へ");
        zh(NEXT, "下一步");
        fr(NEXT, "Suivant");
        es(NEXT, "Siguiente");
        de(NEXT, "Weiter");
        ko(NEXT, "다음");

        ja(NOTHING_FOUND, "報告することはありません。");
        zh(NOTHING_FOUND, "没有要报告的内容。");
        fr(NOTHING_FOUND, "Rien à signaler.");
        es(NOTHING_FOUND, "Nada que señalar.");
        de(NOTHING_FOUND, "Nichts zu melden.");
        ko(NOTHING_FOUND, "보고할 내용이 없습니다.");

        ja(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "例: 英語の読者が発音できない音は使わない、短い単語、紙面での見た目。");
        zh(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "例如:不使用英语读者无法发出的音、词要短、语言在页面上应有的样子。");
        fr(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "Par exemple : pas de sons qu'un lecteur anglophone ne peut pas prononcer, des mots courts, l'aspect que la langue doit avoir sur la page.");
        es(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "Por ejemplo: sin sonidos que un lector de inglés no pueda decir, palabras cortas, cómo debe verse el idioma en la página.");
        de(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "Zum Beispiel: keine Laute, die ein englischer Leser nicht aussprechen kann, kurze Wörter, wie die Sprache auf der Seite aussehen soll.");
        ko(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "예: 영어권 독자가 발음할 수 없는 소리 금지, 짧은 단어, 지면에서 언어가 보여야 하는 모습.");

        ja(PROBLEM, "問題点");
        zh(PROBLEM, "问题");
        fr(PROBLEM, "Problème");
        es(PROBLEM, "Problema");
        de(PROBLEM, "Problem");
        ko(PROBLEM, "문제");

        ja(PROPOSE, "提案");
        zh(PROPOSE, "提议");
        fr(PROPOSE, "Proposer");
        es(PROPOSE, "Proponer");
        de(PROPOSE, "Vorschlagen");
        ko(PROPOSE, "제안");

        ja(PROTOCOL_OF_THE_DESIGNER, "デザイナーに使うプロトコル");
        zh(PROTOCOL_OF_THE_DESIGNER, "设计器所用的协议");
        fr(PROTOCOL_OF_THE_DESIGNER, "Protocole du concepteur");
        es(PROTOCOL_OF_THE_DESIGNER, "Protocolo del diseñador");
        de(PROTOCOL_OF_THE_DESIGNER, "Protokoll des Designers");
        ko(PROTOCOL_OF_THE_DESIGNER, "디자이너의 프로토콜");

        ja(REVIEW_OF_S, "%s のレビュー");
        zh(REVIEW_OF_S, "%s 的审阅");
        fr(REVIEW_OF_S, "Examen de %s");
        es(REVIEW_OF_S, "Revisión de %s");
        de(REVIEW_OF_S, "Prüfung von %s");
        ko(REVIEW_OF_S, "%s 검토");

        ja(REVIEW_THE_LANGUAGE, "言語をレビュー");
        zh(REVIEW_THE_LANGUAGE, "审阅语言");
        fr(REVIEW_THE_LANGUAGE, "Examiner la langue");
        es(REVIEW_THE_LANGUAGE, "Revisar el idioma");
        de(REVIEW_THE_LANGUAGE, "Die Sprache prüfen");
        ko(REVIEW_THE_LANGUAGE, "언어 검토");

        ja(REVIEW_THE_PROPOSED_CHANGES, "提案された変更を確認");
        zh(REVIEW_THE_PROPOSED_CHANGES, "审阅提议的变更");
        fr(REVIEW_THE_PROPOSED_CHANGES, "Examiner les modifications proposées");
        es(REVIEW_THE_PROPOSED_CHANGES, "Revisar los cambios propuestos");
        de(REVIEW_THE_PROPOSED_CHANGES, "Die vorgeschlagenen Änderungen prüfen");
        ko(REVIEW_THE_PROPOSED_CHANGES, "제안된 변경 사항 검토");

        ja(REWRITE_A_SECTION, "セクションを書き直す...");
        zh(REWRITE_A_SECTION, "重写小节...");
        fr(REWRITE_A_SECTION, "Réécrire une section...");
        es(REWRITE_A_SECTION, "Reescribir una sección...");
        de(REWRITE_A_SECTION, "Einen Abschnitt neu schreiben ...");
        ko(REWRITE_A_SECTION, "섹션 다시 쓰기...");

        ja(REWRITE_WITH_AI, "AI で書き直す...");
        zh(REWRITE_WITH_AI, "用 AI 重写...");
        fr(REWRITE_WITH_AI, "Réécrire avec l'IA...");
        es(REWRITE_WITH_AI, "Reescribir con IA...");
        de(REWRITE_WITH_AI, "Mit KI neu schreiben ...");
        ko(REWRITE_WITH_AI, "AI로 다시 쓰기...");

        ja(SAY_WHAT_YOU_WANT, "まず希望を書いてください。");
        zh(SAY_WHAT_YOU_WANT, "请先说明你想要什么。");
        fr(SAY_WHAT_YOU_WANT, "Dites d'abord ce que vous voulez.");
        es(SAY_WHAT_YOU_WANT, "Di primero lo que quieres.");
        de(SAY_WHAT_YOU_WANT, "Sage zuerst, was du möchtest.");
        ko(SAY_WHAT_YOU_WANT, "먼저 원하는 것을 말해 주세요.");

        ja(SECTION, "セクション");
        zh(SECTION, "小节");
        fr(SECTION, "Section");
        es(SECTION, "Sección");
        de(SECTION, "Abschnitt");
        ko(SECTION, "섹션");

        ja(SEED_LEXICON, "基礎語彙");
        zh(SEED_LEXICON, "基础词汇");
        fr(SEED_LEXICON, "Lexique de départ");
        es(SEED_LEXICON, "Léxico inicial");
        de(SEED_LEXICON, "Grundlexikon");
        ko(SEED_LEXICON, "초기 사전");

        ja(SEND, "送信");
        zh(SEND, "发送");
        fr(SEND, "Envoyer");
        es(SEND, "Enviar");
        de(SEND, "Senden");
        ko(SEND, "보내기");

        ja(SENTENCE, "文");
        zh(SENTENCE, "句子");
        fr(SENTENCE, "Phrase");
        es(SENTENCE, "Frase");
        de(SENTENCE, "Satz");
        ko(SENTENCE, "문장");

        ja(SENTENCES_ONE_PER_LINE, "文(1行に1文)");
        zh(SENTENCES_ONE_PER_LINE, "句子(每行一句)");
        fr(SENTENCES_ONE_PER_LINE, "Phrases, une par ligne");
        es(SENTENCES_ONE_PER_LINE, "Frases, una por línea");
        de(SENTENCES_ONE_PER_LINE, "Sätze, einer pro Zeile");
        ko(SENTENCES_ONE_PER_LINE, "문장, 한 줄에 하나");

        ja(SEVERITY, "重大度");
        zh(SEVERITY, "严重程度");
        fr(SEVERITY, "Gravité");
        es(SEVERITY, "Gravedad");
        de(SEVERITY, "Schweregrad");
        ko(SEVERITY, "심각도");

        ja(STAGE_GRAMMAR, "文法");
        zh(STAGE_GRAMMAR, "语法");
        fr(STAGE_GRAMMAR, "Grammaire");
        es(STAGE_GRAMMAR, "Gramática");
        de(STAGE_GRAMMAR, "Grammatik");
        ko(STAGE_GRAMMAR, "문법");

        ja(STAGE_SOUNDS, "音");
        zh(STAGE_SOUNDS, "语音");
        fr(STAGE_SOUNDS, "Sons");
        es(STAGE_SOUNDS, "Sonidos");
        de(STAGE_SOUNDS, "Laute");
        ko(STAGE_SOUNDS, "소리");

        ja(STAGE_WORD_FORMATION, "語形成");
        zh(STAGE_WORD_FORMATION, "构词");
        fr(STAGE_WORD_FORMATION, "Formation des mots");
        es(STAGE_WORD_FORMATION, "Formación de palabras");
        de(STAGE_WORD_FORMATION, "Wortbildung");
        ko(STAGE_WORD_FORMATION, "단어 형성");

        ja(STAGE_WRITING, "表記");
        zh(STAGE_WRITING, "文字");
        fr(STAGE_WRITING, "Écriture");
        es(STAGE_WRITING, "Escritura");
        de(STAGE_WRITING, "Schrift");
        ko(STAGE_WRITING, "표기");

        ja(STEP_D_OF_D_S, "ステップ %d / %d: %s");
        zh(STEP_D_OF_D_S, "第 %d 步,共 %d 步:%s");
        fr(STEP_D_OF_D_S, "Étape %d sur %d : %s");
        es(STEP_D_OF_D_S, "Paso %d de %d: %s");
        de(STEP_D_OF_D_S, "Schritt %d von %d: %s");
        ko(STEP_D_OF_D_S, "단계 %d / %d: %s");

        ja(STOP_WAITING, "待機をやめる");
        zh(STOP_WAITING, "不再等待");
        fr(STOP_WAITING, "Ne plus attendre");
        es(STOP_WAITING, "Dejar de esperar");
        de(STOP_WAITING, "Nicht mehr warten");
        ko(STOP_WAITING, "기다리기 중지");

        ja(TELL_THE_DESIGNER_WHAT_YOU_WANT, "デザイナーへの要望");
        zh(TELL_THE_DESIGNER_WHAT_YOU_WANT, "告诉设计器你想要什么");
        fr(TELL_THE_DESIGNER_WHAT_YOU_WANT, "Dites au concepteur ce que vous voulez");
        es(TELL_THE_DESIGNER_WHAT_YOU_WANT, "Dile al diseñador lo que quieres");
        de(TELL_THE_DESIGNER_WHAT_YOU_WANT, "Sage dem Designer, was du möchtest");
        ko(TELL_THE_DESIGNER_WHAT_YOU_WANT, "디자이너에게 원하는 것을 알려 주세요");

        ja(TEST, "テスト");
        zh(TEST, "测试");
        fr(TEST, "Tester");
        es(TEST, "Probar");
        de(TEST, "Testen");
        ko(TEST, "테스트");

        ja(THE_DESIGNER_FAILED, "デザイナーが失敗しました: %s");
        zh(THE_DESIGNER_FAILED, "设计器失败:%s");
        fr(THE_DESIGNER_FAILED, "Le concepteur a échoué : %s");
        es(THE_DESIGNER_FAILED, "El diseñador ha fallado: %s");
        de(THE_DESIGNER_FAILED, "Der Designer ist fehlgeschlagen: %s");
        ko(THE_DESIGNER_FAILED, "디자이너가 실패했습니다: %s");

        ja(THE_DESIGNER_IS_REVIEWING, "デザイナーが言語をレビュー中...");
        zh(THE_DESIGNER_IS_REVIEWING, "设计器正在审阅语言...");
        fr(THE_DESIGNER_IS_REVIEWING, "Le concepteur examine la langue...");
        es(THE_DESIGNER_IS_REVIEWING, "El diseñador está revisando el idioma...");
        de(THE_DESIGNER_IS_REVIEWING, "Der Designer prüft die Sprache ...");
        ko(THE_DESIGNER_IS_REVIEWING, "디자이너가 언어를 검토하는 중...");

        ja(THE_DESIGNER_IS_THINKING, "デザイナーが考え中...");
        zh(THE_DESIGNER_IS_THINKING, "设计器正在思考...");
        fr(THE_DESIGNER_IS_THINKING, "Le concepteur réfléchit...");
        es(THE_DESIGNER_IS_THINKING, "El diseñador está pensando...");
        de(THE_DESIGNER_IS_THINKING, "Der Designer denkt nach ...");
        ko(THE_DESIGNER_IS_THINKING, "디자이너가 생각하는 중...");

        ja(THE_DESIGNER_IS_WORKING, "デザイナーが作業中です。1分以上かかることがあります...");
        zh(THE_DESIGNER_IS_WORKING, "设计器正在工作,可能需要一分钟或更久...");
        fr(THE_DESIGNER_IS_WORKING, "Le concepteur travaille, cela peut prendre une minute ou plus...");
        es(THE_DESIGNER_IS_WORKING, "El diseñador está trabajando, puede tardar un minuto o más...");
        de(THE_DESIGNER_IS_WORKING, "Der Designer arbeitet, das kann eine Minute oder länger dauern ...");
        ko(THE_DESIGNER_IS_WORKING, "디자이너가 작업 중입니다. 1분 이상 걸릴 수 있습니다...");

        ja(THE_DESIGNER_PROPOSES_NOTHING, "デザイナーは書き込む提案をしていません。");
        zh(THE_DESIGNER_PROPOSES_NOTHING, "设计器没有提出可写入的内容。");
        fr(THE_DESIGNER_PROPOSES_NOTHING, "Le concepteur ne propose rien à écrire.");
        es(THE_DESIGNER_PROPOSES_NOTHING, "El diseñador no propone nada que escribir.");
        de(THE_DESIGNER_PROPOSES_NOTHING, "Der Designer schlägt nichts zum Schreiben vor.");
        ko(THE_DESIGNER_PROPOSES_NOTHING, "디자이너가 쓸 것을 제안하지 않았습니다.");

        ja(THE_DESIGNER_PROPOSES_THE_SECTION, "デザイナーが概要書きとここまでの承認内容から、規則のセクション「%s」を提案します。");
        zh(THE_DESIGNER_PROPOSES_THE_SECTION, "设计器根据简述和目前已接受的内容提议规则的“%s”小节。");
        fr(THE_DESIGNER_PROPOSES_THE_SECTION, "Le concepteur propose la section « %s » des règles à partir du cahier des charges et de ce qui est accepté jusqu'ici.");
        es(THE_DESIGNER_PROPOSES_THE_SECTION, "El diseñador propone la sección \"%s\" de las reglas a partir de la descripción del encargo y de lo aceptado hasta ahora.");
        de(THE_DESIGNER_PROPOSES_THE_SECTION, "Der Designer schlägt den Abschnitt „%s“ der Regeln aus dem Auftrag und dem bisher Übernommenen vor.");
        ko(THE_DESIGNER_PROPOSES_THE_SECTION, "디자이너가 브리프와 지금까지 수락된 내용을 바탕으로 규칙의 섹션 \"%s\"을(를) 제안합니다.");

        ja(THE_DESIGNER_TRANSLATES_SAMPLES, "デザイナーが現在の言語であなたの文を翻訳します。読んでおかしな点を確認できます。足りない単語は新しい単語として提案されます。");
        zh(THE_DESIGNER_TRANSLATES_SAMPLES, "设计器用当前的语言翻译你的句子,方便你阅读并发现不对劲之处。语言缺少的词会作为新词提议。");
        fr(THE_DESIGNER_TRANSLATES_SAMPLES, "Le concepteur traduit vos phrases avec la langue telle qu'elle est, pour que vous puissiez la lire et voir ce qui cloche. Les mots qui manquent à la langue sont proposés comme nouveaux mots.");
        es(THE_DESIGNER_TRANSLATES_SAMPLES, "El diseñador traduce tus frases con el idioma tal como está, para que puedas leerlo y ver qué parece estar mal. Las palabras que le faltan al idioma se proponen como palabras nuevas.");
        de(THE_DESIGNER_TRANSLATES_SAMPLES, "Der Designer übersetzt deine Sätze mit der Sprache, wie sie ist, damit du sie lesen und sehen kannst, was falsch aussieht. Wörter, die der Sprache fehlen, werden als neue Wörter vorgeschlagen.");
        ko(THE_DESIGNER_TRANSLATES_SAMPLES, "디자이너가 현재 언어 그대로 문장을 번역하므로 읽어 보고 이상한 부분을 확인할 수 있습니다. 언어에 없는 단어는 새 단어로 제안됩니다.");

        ja(THE_PEOPLE_THEIR_WORLD, "話す人々、その世界と文化。");
        zh(THE_PEOPLE_THEIR_WORLD, "说这种语言的人、他们的世界和文化。");
        fr(THE_PEOPLE_THEIR_WORLD, "Les gens qui la parlent, leur monde et leur culture.");
        es(THE_PEOPLE_THEIR_WORLD, "Las personas que lo hablan, su mundo y su cultura.");
        de(THE_PEOPLE_THEIR_WORLD, "Die Menschen, die sie sprechen, ihre Welt und ihre Kultur.");
        ko(THE_PEOPLE_THEIR_WORLD, "언어를 사용하는 사람들, 그들의 세계와 문화.");

        ja(THE_SECTION_IS_EMPTY, "セクションが空です");
        zh(THE_SECTION_IS_EMPTY, "小节为空");
        fr(THE_SECTION_IS_EMPTY, "La section est vide");
        es(THE_SECTION_IS_EMPTY, "La sección está vacía");
        de(THE_SECTION_IS_EMPTY, "Der Abschnitt ist leer");
        ko(THE_SECTION_IS_EMPTY, "섹션이 비어 있습니다");

        ja(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "最初の単語はカテゴリごとに作られ、書き込む前に毎回確認します。まず基本カテゴリ、その後に本の固有名詞を作りましょう。");
        zh(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "最初的词按类别生成,每一批写入前都要审阅。先从基本类别开始,再做你书中的名称。");
        fr(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "Les premiers mots sont créés catégorie par catégorie, et chaque lot est examiné avant d'être écrit. Commencez par les catégories de base, puis les noms de votre livre.");
        es(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "Las primeras palabras se crean categoría por categoría, y cada lote se revisa antes de escribirse. Empieza con las categorías básicas y sigue con los nombres de tu libro.");
        de(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "Die ersten Wörter werden Kategorie für Kategorie erstellt, und jeder Stapel wird vor dem Schreiben geprüft. Beginne mit den Grundkategorien, dann den Namen deines Buchs.");
        ko(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "처음 단어는 범주별로 만들어지며 각 묶음은 기록되기 전에 검토됩니다. 기본 범주부터 시작한 다음 책의 이름을 추가하세요.");

        ja(TRANSLATE_THE_SENTENCES, "文を翻訳");
        zh(TRANSLATE_THE_SENTENCES, "翻译句子");
        fr(TRANSLATE_THE_SENTENCES, "Traduire les phrases");
        es(TRANSLATE_THE_SENTENCES, "Traducir las frases");
        de(TRANSLATE_THE_SENTENCES, "Die Sätze übersetzen");
        ko(TRANSLATE_THE_SENTENCES, "문장 번역");

        ja(WHAT_DO_YOU_WANT, "何をしたいですか?");
        zh(WHAT_DO_YOU_WANT, "你想要什么?");
        fr(WHAT_DO_YOU_WANT, "Que voulez-vous faire ?");
        es(WHAT_DO_YOU_WANT, "¿Qué quieres?");
        de(WHAT_DO_YOU_WANT, "Was möchtest du?");
        ko(WHAT_DO_YOU_WANT, "무엇을 하시겠습니까?");

        ja(WHAT_THE_LANGUAGE_IS_FOR, "この言語の目的");
        zh(WHAT_THE_LANGUAGE_IS_FOR, "这门语言的用途");
        fr(WHAT_THE_LANGUAGE_IS_FOR, "À quoi sert la langue");
        es(WHAT_THE_LANGUAGE_IS_FOR, "Para qué sirve el idioma");
        de(WHAT_THE_LANGUAGE_IS_FOR, "Wofür die Sprache da ist");
        ko(WHAT_THE_LANGUAGE_IS_FOR, "언어의 용도");

        ja(WHAT_THE_WORDS_ARE_FOR, "単語の用途(任意)");
        zh(WHAT_THE_WORDS_ARE_FOR, "词的用途(可选)");
        fr(WHAT_THE_WORDS_ARE_FOR, "À quoi servent les mots (facultatif)");
        es(WHAT_THE_WORDS_ARE_FOR, "Para qué son las palabras (opcional)");
        de(WHAT_THE_WORDS_ARE_FOR, "Wofür die Wörter da sind (optional)");
        ko(WHAT_THE_WORDS_ARE_FOR, "단어의 용도 (선택 사항)");

        ja(THE_DESIGNER_INVENTS_WORDS, "デザイナーが言語の規則と既存の語彙に従って新しい単語を作ります。何かが書き込まれる前に確認できます。");
        zh(THE_DESIGNER_INVENTS_WORDS, "设计器按语言的规则和现有词汇创造新词。写入之前你可以审阅。");
        fr(THE_DESIGNER_INVENTS_WORDS, "Le concepteur invente de nouveaux mots selon les règles de la langue et le lexique existant. Vous les examinez avant que quoi que ce soit soit écrit.");
        es(THE_DESIGNER_INVENTS_WORDS, "El diseñador inventa palabras nuevas según las reglas del idioma y el léxico que tiene. Las revisas antes de que se escriba nada.");
        de(THE_DESIGNER_INVENTS_WORDS, "Der Designer erfindet neue Wörter nach den Regeln der Sprache und dem Lexikon, das sie hat. Du prüfst sie, bevor etwas geschrieben wird.");
        ko(THE_DESIGNER_INVENTS_WORDS, "디자이너가 언어의 규칙과 사전에 맞게 새 단어를 만듭니다. 아무것도 기록되기 전에 검토합니다.");

        ja(THE_DESIGNER_WRITES_A_SECTION, "デザイナーが規則に欠けているもの(数の体系、色、敬語など)の新しいセクションと必要な単語を書きます。書き込む前に確認できます。");
        zh(THE_DESIGNER_WRITES_A_SECTION, "设计器为规则中缺少的内容(计数系统、颜色、敬语等)撰写新小节及所需词汇。写入之前你可以审阅。");
        fr(THE_DESIGNER_WRITES_A_SECTION, "Le concepteur écrit une nouvelle section des règles pour ce qui manque aux règles (un système de numération, les couleurs, la politesse...) et les mots nécessaires. Vous les examinez avant que quoi que ce soit soit écrit.");
        es(THE_DESIGNER_WRITES_A_SECTION, "El diseñador escribe una sección nueva de las reglas para lo que a las reglas les falta (un sistema de numeración, colores, cortesía...) y las palabras que necesita. Las revisas antes de que se escriba nada.");
        de(THE_DESIGNER_WRITES_A_SECTION, "Der Designer schreibt einen neuen Abschnitt der Regeln für das, was den Regeln fehlt (ein Zahlensystem, Farben, Höflichkeit ...), und die Wörter, die er braucht. Du prüfst sie, bevor etwas geschrieben wird.");
        ko(THE_DESIGNER_WRITES_A_SECTION, "디자이너가 규칙에 빠진 것(수 체계, 색상, 공손함...)과 필요한 단어에 대한 새 규칙 섹션을 씁니다. 아무것도 기록되기 전에 검토합니다.");

        ja(THE_DESIGNER_REWRITES, "デザイナーが依頼どおりにセクションを書き直します。書き込む前に変更点を確認できます。");
        zh(THE_DESIGNER_REWRITES, "设计器按你的要求重写小节。写入之前你可以看到改动。");
        fr(THE_DESIGNER_REWRITES, "Le concepteur réécrit une section comme vous le demandez. Vous voyez ce qui a changé avant que ce soit écrit.");
        es(THE_DESIGNER_REWRITES, "El diseñador reescribe una sección como se lo pides. Ves lo que cambió antes de que se escriba.");
        de(THE_DESIGNER_REWRITES, "Der Designer schreibt einen Abschnitt so um, wie du es verlangst. Du siehst, was sich geändert hat, bevor es geschrieben wird.");
        ko(THE_DESIGNER_REWRITES, "디자이너가 요청한 대로 섹션을 다시 씁니다. 기록되기 전에 바뀐 내용을 볼 수 있습니다.");

        ja(THE_DESIGNER_REVIEWS, "デザイナーが規則と語彙を読み、矛盾・不足・音体系に反する単語を一覧にします。何も変更されません。");
        zh(THE_DESIGNER_REVIEWS, "设计器阅读规则和词汇,列出矛盾、缺漏和违反语音系统的词。不会修改任何内容。");
        fr(THE_DESIGNER_REVIEWS, "Le concepteur lit les règles et le lexique et liste les contradictions, les lacunes et les mots qui violent le système phonétique. Rien n'est modifié.");
        es(THE_DESIGNER_REVIEWS, "El diseñador lee las reglas y el léxico y lista las contradicciones, las lagunas y las palabras que rompen el sistema de sonidos. No se cambia nada.");
        de(THE_DESIGNER_REVIEWS, "Der Designer liest die Regeln und das Lexikon und listet Widersprüche, Lücken und Wörter auf, die das Lautsystem verletzen. Nichts wird geändert.");
        ko(THE_DESIGNER_REVIEWS, "디자이너가 규칙과 사전을 읽고 모순, 빈틈, 소리 체계에 어긋나는 단어를 나열합니다. 아무것도 변경되지 않습니다.");

        ja(READERS_SEE, "公開された本の読者に見えるもの");
        zh(READERS_SEE, "已发布图书的读者看到的内容");
        fr(READERS_SEE, "Les lecteurs du livre publié voient");
        es(READERS_SEE, "Los lectores del libro publicado ven");
        de(READERS_SEE, "Leser des veröffentlichten Buchs sehen");
        ko(READERS_SEE, "게시된 책의 독자에게 보이는 것");

        ja(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "人工言語。クリックすると英語");
        zh(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "构造语言,点击显示英文");
        fr(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "La langue construite, l'anglais au clic");
        es(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "La lengua construida, el inglés al hacer clic");
        de(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "Die Plansprache, das Englische per Klick");
        ko(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "인공어, 클릭하면 영어");

        ja(CONLANG_ONLY, "人工言語のみ");
        zh(CONLANG_ONLY, "仅构造语言");
        fr(CONLANG_ONLY, "La langue construite seulement");
        es(CONLANG_ONLY, "Solo la lengua construida");
        de(CONLANG_ONLY, "Nur die Plansprache");
        ko(CONLANG_ONLY, "인공어만");

        ja(ENGLISH_ONLY, "英語のみ");
        zh(ENGLISH_ONLY, "仅英文");
        fr(ENGLISH_ONLY, "L'anglais seulement");
        es(ENGLISH_ONLY, "Solo el inglés");
        de(ENGLISH_ONLY, "Nur das Englische");
        ko(ENGLISH_ONLY, "영어만");

        ja(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "翻訳された台詞をビューアでどう表示するか。人工言語のみの場合、英語は読者に送信されません。ご自身の画面には影響しません。");
        zh(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "翻译后的对白在阅读器中的显示方式。选择仅构造语言时,英文不会发送给读者。不影响你自己的视图。");
        fr(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "Comment les répliques traduites sont affichées dans le lecteur. Avec la langue construite seulement, l'anglais n'est jamais envoyé au lecteur. Vos propres vues ne sont pas affectées.");
        es(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "Cómo se muestra en el visor el diálogo que se tradujo. Con solo la lengua construida, el inglés nunca se envía al lector. Tus propias vistas no se ven afectadas.");
        de(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "Wie die übersetzte Rede im Viewer angezeigt wird. Bei „nur die Plansprache“ wird das Englische nie an den Leser gesendet. Deine eigenen Ansichten sind nicht betroffen.");
        ko(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "뷰어에서 번역된 대사가 표시되는 방식. 인공어만 선택하면 영어는 독자에게 전송되지 않습니다. 내 보기에는 영향이 없습니다.");

        ja(TRANSLATED_SPEECH, "翻訳された台詞");
        zh(TRANSLATED_SPEECH, "翻译后的对白");
        fr(TRANSLATED_SPEECH, "Réplique traduite");
        es(TRANSLATED_SPEECH, "Diálogo traducido");
        de(TRANSLATED_SPEECH, "Übersetzte Rede");
        ko(TRANSLATED_SPEECH, "번역된 대사");

        ja(THE_CONLANG_AS_IT_IS, "人工言語のまま");
        zh(THE_CONLANG_AS_IT_IS, "保持构造语言原样");
        fr(THE_CONLANG_AS_IT_IS, "La langue construite, telle quelle");
        es(THE_CONLANG_AS_IT_IS, "La lengua construida, tal cual");
        de(THE_CONLANG_AS_IT_IS, "Die Plansprache, wie sie ist");
        ko(THE_CONLANG_AS_IT_IS, "인공어 그대로");

        ja(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "人工言語。英語は脚注");
        zh(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "构造语言,英文放脚注");
        fr(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "La langue construite, l'anglais en notes de bas de page");
        es(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "La lengua construida, el inglés en notas al pie");
        de(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "Die Plansprache, das Englische in Fußnoten");
        ko(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "인공어, 영어는 각주로");

        ja(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "英語。人工言語は脚注");
        zh(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "英文,构造语言放脚注");
        fr(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "L'anglais, la langue construite en notes de bas de page");
        es(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "El inglés, la lengua construida en notas al pie");
        de(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "Das Englische, die Plansprache in Fußnoten");
        ko(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "영어, 인공어는 각주로");

        ja(TRANSLATE_PARTS_CONSTRUCTED, "パートを翻訳(人工言語)");
        zh(TRANSLATE_PARTS_CONSTRUCTED, "翻译段落(人造语言)");
        fr(TRANSLATE_PARTS_CONSTRUCTED, "Traduire des parties (langue construite)");
        es(TRANSLATE_PARTS_CONSTRUCTED, "Traducir partes (lengua construida)");
        de(TRANSLATE_PARTS_CONSTRUCTED, "Abschnitte übersetzen (Plansprache)");
        ko(TRANSLATE_PARTS_CONSTRUCTED, "파트 번역 (인공어)");

        ja(CONLANG, "コンラング");
        zh(CONLANG, "人造语");
        fr(CONLANG, "Langue construite");
        es(CONLANG, "Lengua construida");
        de(CONLANG, "Plansprache");
        ko(CONLANG, "인공어");

        ja(QUICK_TRANSLATE, "クイック翻訳");
        zh(QUICK_TRANSLATE, "快速翻译");
        fr(QUICK_TRANSLATE, "Traduction rapide");
        es(QUICK_TRANSLATE, "Traducción rápida");
        de(QUICK_TRANSLATE, "Schnellübersetzung");
        ko(QUICK_TRANSLATE, "빠른 번역");

        ja(REFRESH, "再読み込み");
        zh(REFRESH, "刷新");
        fr(REFRESH, "Actualiser");
        es(REFRESH, "Actualizar");
        de(REFRESH, "Aktualisieren");
        ko(REFRESH, "새로 고침");

        ja(TEXT_TO_TRANSLATE, "翻訳するテキスト(英語)");
        zh(TEXT_TO_TRANSLATE, "要翻译的文本(英文)");
        fr(TEXT_TO_TRANSLATE, "Texte à traduire (anglais)");
        es(TEXT_TO_TRANSLATE, "Texto que traducir (inglés)");
        de(TEXT_TO_TRANSLATE, "Zu übersetzender Text (Englisch)");
        ko(TEXT_TO_TRANSLATE, "번역할 텍스트 (영어)");

        ja(WHO_SAYS_IT_TO_WHOM, "誰が誰に言うか(任意)");
        zh(WHO_SAYS_IT_TO_WHOM, "谁对谁说(可选)");
        fr(WHO_SAYS_IT_TO_WHOM, "Qui le dit, à qui (facultatif)");
        es(WHO_SAYS_IT_TO_WHOM, "Quién lo dice, a quién (opcional)");
        de(WHO_SAYS_IT_TO_WHOM, "Wer es zu wem sagt (optional)");
        ko(WHO_SAYS_IT_TO_WHOM, "누가 누구에게 말하는지 (선택 사항)");

        ja(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "テキストまたはここに名前がある人物は、「話者」タブのメモとともにモデルに伝えられます。");
        zh(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "文本或此处提到的角色,会连同“说话者”标签中的备注一起告诉模型。");
        fr(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "Un personnage nommé dans le texte ou ici est décrit au modèle avec les notes de l'onglet Locuteurs.");
        es(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "Un personaje nombrado en el texto o aquí se describe al modelo con las notas de la pestaña Hablantes.");
        de(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "Eine im Text oder hier genannte Figur wird dem Modell mit den Notizen des Tabs „Sprecher“ beschrieben.");
        ko(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "텍스트나 여기에서 이름이 언급된 캐릭터는 화자 탭의 메모와 함께 모델에 설명됩니다.");

        ja(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "テキストは本の言語に翻訳されます。物語には何も書き込まれません。翻訳を使い、必要な新語を語彙に取り込めます。");
        zh(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "文本会被翻译成本书的语言。不会写入故事;你自行使用译文,并可把需要的新词导入词汇表。");
        fr(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "Le texte est traduit dans la langue du livre. Rien n'est écrit dans l'histoire ; c'est vous qui utilisez la traduction, et importez dans le lexique les nouveaux mots que vous voulez.");
        es(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "El texto se traduce al idioma del libro. No se escribe nada en la historia; usas la traducción e importas al léxico las palabras nuevas que quieras.");
        de(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "Der Text wird in die Sprache des Buchs übersetzt. Nichts wird in die Geschichte geschrieben; du verwendest die Übersetzung und importierst die neuen Wörter, die du möchtest, in das Lexikon.");
        ko(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "텍스트는 책의 언어로 번역됩니다. 이야기에는 아무것도 기록되지 않으며, 번역을 사용하고 원하는 새 단어를 사전에 가져옵니다.");

        ja(TRANSLATE, "翻訳");
        zh(TRANSLATE, "翻译");
        fr(TRANSLATE, "Traduire");
        es(TRANSLATE, "Traducir");
        de(TRANSLATE, "Übersetzen");
        ko(TRANSLATE, "번역");

        ja(TRANSLATION, "翻訳結果");
        zh(TRANSLATION, "译文");
        fr(TRANSLATION, "Traduction");
        es(TRANSLATION, "Traducción");
        de(TRANSLATION, "Übersetzung");
        ko(TRANSLATION, "번역");

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

        ja(THERE_IS_NO_TEXT_TO_TRANSLATE, "翻訳するテキストがありません");
        zh(THERE_IS_NO_TEXT_TO_TRANSLATE, "没有要翻译的文本");
        fr(THERE_IS_NO_TEXT_TO_TRANSLATE, "Il n'y a pas de texte à traduire");
        es(THERE_IS_NO_TEXT_TO_TRANSLATE, "No hay texto que traducir");
        de(THERE_IS_NO_TEXT_TO_TRANSLATE, "Es gibt keinen Text zum Übersetzen");
        ko(THERE_IS_NO_TEXT_TO_TRANSLATE, "번역할 텍스트가 없습니다");

        ja(THE_WORDS_THE_TRANSLATION_NEEDED, "翻訳に必要で語彙になかった単語");
        zh(THE_WORDS_THE_TRANSLATION_NEEDED, "译文需要而词汇表中没有的词");
        fr(THE_WORDS_THE_TRANSLATION_NEEDED, "Mots dont la traduction avait besoin et qui manquaient au lexique");
        es(THE_WORDS_THE_TRANSLATION_NEEDED, "Palabras que necesitó la traducción y que faltaban en el léxico");
        de(THE_WORDS_THE_TRANSLATION_NEEDED, "Wörter, die die Übersetzung brauchte und die dem Lexikon fehlten");
        ko(THE_WORDS_THE_TRANSLATION_NEEDED, "번역에 필요했지만 사전에 없던 단어");

        ja(IMPORT_THE_SELECTED_WORDS, "選択した単語を取り込む");
        zh(IMPORT_THE_SELECTED_WORDS, "导入所选的词");
        fr(IMPORT_THE_SELECTED_WORDS, "Importer les mots sélectionnés");
        es(IMPORT_THE_SELECTED_WORDS, "Importar las palabras seleccionadas");
        de(IMPORT_THE_SELECTED_WORDS, "Die ausgewählten Wörter importieren");
        ko(IMPORT_THE_SELECTED_WORDS, "선택한 단어 가져오기");

        ja(IMPORTED_D_WORDS_AS_NEW, "%d個の単語を新規として取り込みました");
        zh(IMPORTED_D_WORDS_AS_NEW, "已将 %d 个词作为新词导入");
        fr(IMPORTED_D_WORDS_AS_NEW, "%d mot(s) importé(s) comme nouveaux");
        es(IMPORTED_D_WORDS_AS_NEW, "Se han importado %d palabra(s) como nuevas");
        de(IMPORTED_D_WORDS_AS_NEW, "%d Wort/Wörter als neu importiert");
        ko(IMPORTED_D_WORDS_AS_NEW, "단어 %d개를 새 단어로 가져왔습니다");

        ja(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "語彙にも新語にもない単語: %s");
        zh(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "既不在词汇表也不在新词中的词:%s");
        fr(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "Mots qui ne sont ni dans le lexique ni parmi les nouveaux mots : %s");
        es(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "Palabras que no están ni en el léxico ni en las palabras nuevas: %s");
        de(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "Wörter, die weder im Lexikon noch unter den neuen Wörtern sind: %s");
        ko(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "사전에도 새 단어에도 없는 단어: %s");

        ja(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "選択範囲を翻訳(人工言語)");
        zh(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "翻译所选内容(人造语言)");
        fr(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "Traduire le texte sélectionné (langue construite)");
        es(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "Traducir el texto seleccionado (lengua construida)");
        de(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "Ausgewählten Text übersetzen (Plansprache)");
        ko(TRANSLATE_SELECTED_TEXT_CONSTRUCTED, "선택한 텍스트 번역 (인공어)");
        ja(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "未翻訳の台詞を翻訳(人工言語)");
        zh(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "翻译未翻译的对白(人造语言)");
        fr(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "Traduire les dialogues manquants (langue construite)");
        es(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "Traducir los diálogos que faltan (lengua construida)");
        de(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "Fehlende Dialoge übersetzen (Plansprache)");
        ko(TRANSLATE_MISSING_DIALOGS_CONSTRUCTED, "누락된 대사 번역 (인공어)");
        ja(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "選択範囲の未翻訳の台詞を翻訳(人工言語)");
        zh(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "翻译所选内容中未翻译的对白(人造语言)");
        fr(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "Traduire les dialogues manquants de la sélection (langue construite)");
        es(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "Traducir los diálogos que faltan en la selección (lengua construida)");
        de(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "Fehlende Dialoge in der Auswahl übersetzen (Plansprache)");
        ko(TRANSLATE_MISSING_DIALOGS_IN_SELECTION_CONSTRUCTED, "선택 영역의 누락된 대사 번역 (인공어)");

        ja(TRANSLATING_THE_SELECTION, "選択したテキストを翻訳中...");
        zh(TRANSLATING_THE_SELECTION, "正在翻译所选文本...");
        fr(TRANSLATING_THE_SELECTION, "Traduction du texte sélectionné...");
        es(TRANSLATING_THE_SELECTION, "Traduciendo el texto seleccionado...");
        de(TRANSLATING_THE_SELECTION, "Der ausgewählte Text wird übersetzt ...");
        ko(TRANSLATING_THE_SELECTION, "선택한 텍스트를 번역하는 중...");

        ja(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "選択範囲に翻訳済みの台詞があります。先に英語に戻してください");
        zh(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "所选内容包含已翻译的对白,请先恢复为英文");
        fr(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "La sélection contient une réplique traduite ; remettez d'abord l'anglais");
        es(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "La selección contiene un diálogo traducido; restaura primero el inglés");
        de(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "Die Auswahl enthält eine übersetzte Rede; setze zuerst das Englische zurück");
        ko(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "선택 영역에 번역된 대사가 있습니다. 먼저 영어로 되돌리세요");

        ja(THE_PART_WAS_CHANGED_MEANWHILE, "その間にパートが変更されました。もう一度選択してください");
        zh(THE_PART_WAS_CHANGED_MEANWHILE, "该段在此期间已被修改,请重新选择文本");
        fr(THE_PART_WAS_CHANGED_MEANWHILE, "La partie a été modifiée entre-temps ; sélectionnez à nouveau le texte");
        es(THE_PART_WAS_CHANGED_MEANWHILE, "La parte cambió entretanto; vuelve a seleccionar el texto");
        de(THE_PART_WAS_CHANGED_MEANWHILE, "Der Abschnitt wurde inzwischen geändert; wähle den Text erneut aus");
        ko(THE_PART_WAS_CHANGED_MEANWHILE, "그 사이 파트가 변경되었습니다. 텍스트를 다시 선택하세요");

        ja(NAMES, "名前");
        zh(NAMES, "名称");
        fr(NAMES, "Noms");
        es(NAMES, "Nombres");
        de(NAMES, "Namen");
        ko(NAMES, "이름");

        ja(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "名前ジェネレーターが名前を作る元になる単語にチェックを入れてください。ジェネレーターはそれらでマルコフ連鎖を学習するので、言語らしい響きの名前になります。後で削除または廃止された単語は除外されます。設定を保存すると、カテゴリは「Conlang - 言語 - カテゴリ」として提供されます。");
        zh(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "勾选名称生成器用来生成名称的词:它用这些词训练马尔可夫链,生成的名称会带有该语言的音感。之后被删除或弃用的词会被忽略。保存设置后,这些类别会以\"Conlang - 语言 - 类别\"的形式提供。");
        fr(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "Cochez les mots à partir desquels le générateur de noms fabrique ses noms : il entraîne une chaîne de Markov sur eux, pour que les noms qu'il donne sonnent comme la langue. Un mot supprimé ou retiré plus tard est omis. Les catégories sont proposées sous la forme \"Conlang - langue - catégorie\" après l'enregistrement des paramètres.");
        es(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "Marca las palabras con las que el Generador de nombres crea sus nombres: entrena una cadena de Markov con ellas, así que los nombres que da suenan como el idioma. Una palabra que se elimine o se retire después se omite. Las categorías se ofrecen como \"Lengua construida - idioma - categoría\" después de guardar los ajustes.");
        de(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "Prüfe die Wörter, aus denen der Namensgenerator seine Namen bildet: Er trainiert eine Markov-Kette darauf, sodass die Namen, die er liefert, wie die Sprache klingen. Ein Wort, das später gelöscht oder ausgemustert wird, bleibt außen vor. Die Kategorien werden nach dem Speichern der Einstellungen als „Conlang - Sprache - Kategorie“ angeboten.");
        ko(THE_NAME_GENERATOR_MAKES_ITS_NAMES, "이름 생성기가 이름을 만드는 데 사용할 단어를 체크하세요: 마르코프 체인을 학습하므로 생성되는 이름이 언어처럼 들립니다. 나중에 삭제되거나 폐기된 단어는 제외됩니다. 범주는 설정을 저장한 후 \"인공어 - 언어 - 범주\"로 제공됩니다.");

        ja(NAME_CATEGORY, "名前カテゴリ");
        zh(NAME_CATEGORY, "名称类别");
        fr(NAME_CATEGORY, "Catégorie de noms");
        es(NAME_CATEGORY, "Categoría de nombres");
        de(NAME_CATEGORY, "Namenskategorie");
        ko(NAME_CATEGORY, "이름 범주");

        ja(NEW_NAME_CATEGORY, "新しい名前カテゴリ");
        zh(NEW_NAME_CATEGORY, "新建名称类别");
        fr(NEW_NAME_CATEGORY, "Nouvelle catégorie de noms");
        es(NEW_NAME_CATEGORY, "Nueva categoría de nombres");
        de(NEW_NAME_CATEGORY, "Neue Namenskategorie");
        ko(NEW_NAME_CATEGORY, "새 이름 범주");

        ja(RENAME, "名前を変更");
        zh(RENAME, "重命名");
        fr(RENAME, "Renommer");
        es(RENAME, "Renombrar");
        de(RENAME, "Umbenennen");
        ko(RENAME, "이름 바꾸기");

        ja(RENAME_THE_NAME_CATEGORY, "名前カテゴリの名前を変更");
        zh(RENAME_THE_NAME_CATEGORY, "重命名名称类别");
        fr(RENAME_THE_NAME_CATEGORY, "Renommer la catégorie de noms");
        es(RENAME_THE_NAME_CATEGORY, "Renombrar la categoría de nombres");
        de(RENAME_THE_NAME_CATEGORY, "Die Namenskategorie umbenennen");
        ko(RENAME_THE_NAME_CATEGORY, "이름 범주 이름 바꾸기");

        ja(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "その名前の名前カテゴリがすでにあります");
        zh(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "该语言已有同名的名称类别");
        fr(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "La langue a déjà une catégorie de noms portant ce nom");
        es(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "El idioma tiene una categoría de nombres con ese nombre");
        de(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "Die Sprache hat eine Namenskategorie mit diesem Namen");
        ko(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME, "언어에 그 이름의 이름 범주가 있습니다");

        ja(ADD_A_NAME_CATEGORY_FIRST, "先に名前カテゴリを追加してください");
        zh(ADD_A_NAME_CATEGORY_FIRST, "请先添加名称类别");
        fr(ADD_A_NAME_CATEGORY_FIRST, "Ajoutez d'abord une catégorie de noms");
        es(ADD_A_NAME_CATEGORY_FIRST, "Añade primero una categoría de nombres");
        de(ADD_A_NAME_CATEGORY_FIRST, "Füge zuerst eine Namenskategorie hinzu");
        ko(ADD_A_NAME_CATEGORY_FIRST, "먼저 이름 범주를 추가하세요");

        ja(D_WORDS_CHOSEN_D_SHOWN, "%d語を選択、%d語を表示");
        zh(D_WORDS_CHOSEN_D_SHOWN, "已选 %d 个词,显示 %d 个");
        fr(D_WORDS_CHOSEN_D_SHOWN, "%d mots choisis, %d affichés");
        es(D_WORDS_CHOSEN_D_SHOWN, "%d palabras elegidas, %d mostradas");
        de(D_WORDS_CHOSEN_D_SHOWN, "%d Wörter gewählt, %d angezeigt");
        ko(D_WORDS_CHOSEN_D_SHOWN, "단어 %d개 선택됨, %d개 표시됨");

        ja(CHECK_ALL_SHOWN, "表示中をすべて選択");
        zh(CHECK_ALL_SHOWN, "全选显示的词");
        fr(CHECK_ALL_SHOWN, "Cocher tous ceux affichés");
        es(CHECK_ALL_SHOWN, "Marcar todas las mostradas");
        de(CHECK_ALL_SHOWN, "Alle angezeigten markieren");
        ko(CHECK_ALL_SHOWN, "표시된 항목 모두 체크");

        ja(UNCHECK_ALL_SHOWN, "表示中をすべて解除");
        zh(UNCHECK_ALL_SHOWN, "取消选择显示的词");
        fr(UNCHECK_ALL_SHOWN, "Décocher tous ceux affichés");
        es(UNCHECK_ALL_SHOWN, "Desmarcar todas las mostradas");
        de(UNCHECK_ALL_SHOWN, "Markierung aller angezeigten aufheben");
        ko(UNCHECK_ALL_SHOWN, "표시된 항목 모두 체크 해제");

        ja(PREVIEW_NAMES, "名前をプレビュー");
        zh(PREVIEW_NAMES, "预览名称");
        fr(PREVIEW_NAMES, "Aperçu des noms");
        es(PREVIEW_NAMES, "Vista previa de los nombres");
        de(PREVIEW_NAMES, "Namen in der Vorschau");
        ko(PREVIEW_NAMES, "이름 미리 보기");

        ja(CHECK_SOME_WORDS_FIRST, "先に単語を選択してください");
        zh(CHECK_SOME_WORDS_FIRST, "请先勾选一些词");
        fr(CHECK_SOME_WORDS_FIRST, "Cochez d'abord quelques mots");
        es(CHECK_SOME_WORDS_FIRST, "Marca primero algunas palabras");
        de(CHECK_SOME_WORDS_FIRST, "Markiere zuerst einige Wörter");
        ko(CHECK_SOME_WORDS_FIRST, "먼저 단어를 체크하세요");

        ja(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "名前ジェネレーターがインストールされていません");
        zh(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "未安装名称生成器");
        fr(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "Le générateur de noms n'est pas installé");
        es(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "El Generador de nombres no está instalado");
        de(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "Der Namensgenerator ist nicht installiert");
        ko(THE_NAME_GENERATOR_IS_NOT_INSTALLED, "이름 생성기가 설치되어 있지 않습니다");

        ja(TOO_FEW_WORDS_TO_MAKE_NAMES, "名前を作るには単語が少なすぎます");
        zh(TOO_FEW_WORDS_TO_MAKE_NAMES, "词太少,无法生成名称");
        fr(TOO_FEW_WORDS_TO_MAKE_NAMES, "Trop peu de mots pour fabriquer des noms");
        es(TOO_FEW_WORDS_TO_MAKE_NAMES, "Hay muy pocas palabras para crear nombres");
        de(TOO_FEW_WORDS_TO_MAKE_NAMES, "Zu wenige Wörter, um daraus Namen zu bilden");
        ko(TOO_FEW_WORDS_TO_MAKE_NAMES, "이름을 만들기에는 단어가 너무 적습니다");

        ja(TARGET_NAME_CATEGORY, "名前カテゴリ");
        zh(TARGET_NAME_CATEGORY, "名称类别");
        fr(TARGET_NAME_CATEGORY, "Catégorie de noms");
        es(TARGET_NAME_CATEGORY, "Categoría de nombres");
        de(TARGET_NAME_CATEGORY, "Namenskategorie");
        ko(TARGET_NAME_CATEGORY, "이름 범주");

        ja(WORDS_CHOSEN, "選択した単語");
        zh(WORDS_CHOSEN, "已选的词");
        fr(WORDS_CHOSEN, "Mots choisis");
        es(WORDS_CHOSEN, "Palabras elegidas");
        de(WORDS_CHOSEN, "Gewählte Wörter");
        ko(WORDS_CHOSEN, "선택된 단어");
    }
}
