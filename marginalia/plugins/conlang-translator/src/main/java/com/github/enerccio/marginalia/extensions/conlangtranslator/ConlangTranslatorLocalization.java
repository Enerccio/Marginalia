package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Conlang Translator in the languages besides English; the English text is the key.
 */
public class ConlangTranslatorLocalization extends PluginLocalizationBundle {

    public static final ConlangTranslatorLocalization T = new ConlangTranslatorLocalization();

    public static final String TRANSLATE_THE_SPEECH = "Translate the speech";
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
    public static final String TRANSLATE_PARTS = "Translate parts";
    public static final String FROM_PART = "From part";
    public static final String TO_PART = "To part";
    public static final String START = "Start";
    public static final String STOP = "Stop";
    public static final String TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED = "Translate the parts that are not translated";
    public static final String TRANSLATE_THE_TRANSLATED_PARTS_AGAIN = "Translate the translated parts again";
    public static final String PUT_THE_ENGLISH_BACK = "Put the English back";
    public static final String PARTS_IN_THE_RANGE_D_TO_DO_D = "%d part(s) in the range, %d to do";
    public static final String NOTHING_TO_DO_IN_THE_RANGE = "Nothing to do in the range";
    public static final String PUT_THE_ENGLISH_BACK_INTO_D_PARTS = "Put the English back into %d part(s)? The conlang stays in the history of each part, and speeches you edited stay as they are.";
    public static final String A_RANGE_IS_BEING_TRANSLATED_ALREADY = "A range of this book is being processed already";
    public static final String STOPPING_AFTER_THIS_PART = "Stopping after this part...";
    public static final String PART_D_OF_D = "Part %d of %d";
    public static final String D_PARTS_CHANGED_D_SPEECHES_D_NEW_WORDS = "%d part(s) changed, %d speech(es), %d new word(s).";
    public static final String D_EDITED_SPEECHES_STAY = "%d edited speech(es) stay as they are.";
    public static final String D_PARTS_FAILED_S = "%d part(s) failed: %s";
    public static final String STOPPED_AFTER_D_OF_D_PARTS = "Stopped after %d of %d part(s).";
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
    public static final String TRANSLATE_THE_SELECTION = "Translate the selection";
    public static final String TRANSLATING_THE_SELECTION = "Translating the selected text...";
    public static final String THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH = "The selection holds a translated speech; put the English back first";
    public static final String THE_PART_WAS_CHANGED_MEANWHILE = "The part was changed meanwhile; select the text again";

    @Override
    protected void loadKeys() {
        ja(TRANSLATE_THE_SPEECH, "台詞を翻訳");
        zh(TRANSLATE_THE_SPEECH, "翻译对白");

        ja(TRANSLATING_THE_SPEECH, "パートの台詞を翻訳中...");
        zh(TRANSLATING_THE_SPEECH, "正在翻译本段对白...");

        ja(TRANSLATED_SPEECHES, "台詞を%d件翻訳し、語彙に新しい単語を%d個追加しました");
        zh(TRANSLATED_SPEECHES, "已翻译 %d 句对白,词汇表新增 %d 个词");

        ja(SOME_WORDS_NOT_IN_THE_LEXICON, "%d件の翻訳に、語彙になく報告もされていない単語があります");
        zh(SOME_WORDS_NOT_IN_THE_LEXICON, "%d 句翻译使用了词汇表中没有且未报告的词");

        ja(NO_SPEECH_IN_THE_LANGUAGE, "このパートにこの言語の台詞はありません");
        zh(NO_SPEECH_IN_THE_LANGUAGE, "本段没有使用该语言的对白");

        ja(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "エージェントの実行が言語「%s」を変更しました。変更はまだライブラリに入っていません。");
        zh(AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S, "智能体运行修改了语言“%s”。这些修改尚未进入你的语言库。");
        ja(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "エージェントの実行が言語「%s」を作成しました。まだライブラリにはありません。");
        zh(AN_AGENT_RUN_MADE_THE_LANGUAGE_S, "智能体运行创建了语言“%s”。它尚未进入你的语言库。");
        ja(REVIEW_THE_RUN, "確認");
        zh(REVIEW_THE_RUN, "审阅");
        ja(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "言語「%s」をライブラリに追加しますか?");
        zh(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY, "将语言“%s”添加到你的语言库吗?");
        ja(CHANGES_OF_THE_RUN_TO_S, "エージェントの実行による「%s」への変更");
        zh(CHANGES_OF_THE_RUN_TO_S, "智能体运行对“%s”的修改");
        ja(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "エージェントの実行による「%s」への変更を破棄しますか? ライブラリには取り込まれません。");
        zh(DROP_THE_CHANGES_OF_THE_RUN_TO_S, "放弃智能体运行对“%s”的修改吗?它们不会进入你的语言库。");
        ja(DROP, "破棄");
        zh(DROP, "放弃");
        ja(THE_BOOK_HAS_NO_LANGUAGE, "この本には翻訳先の言語がありません");
        zh(THE_BOOK_HAS_NO_LANGUAGE, "这本书没有可翻译成的语言");

        ja(THE_BOOK_HAS_NO_MODEL, "翻訳用のモデルがありません。本にもデフォルトにも設定されていません");
        zh(THE_BOOK_HAS_NO_MODEL, "没有可用于翻译的模型:书和默认设置都没有指定");

        ja(THE_PART_HAS_NO_TEXT, "パートにテキストがありません");
        zh(THE_PART_HAS_NO_TEXT, "本段没有文本");

        ja(THE_MODEL_ANSWERED_WRONG, "モデルが想定外の形式で回答しました");
        zh(THE_MODEL_ANSWERED_WRONG, "模型没有按预期格式回答");

        ja(THE_TRANSLATION_FAILED, "翻訳に失敗しました: %s");
        zh(THE_TRANSLATION_FAILED, "翻译失败:%s");

        ja(THE_PART_IS_BEING_TRANSLATED, "このパートはすでに翻訳中です");
        zh(THE_PART_IS_BEING_TRANSLATED, "本段正在翻译");

        ja(CONLANG_TRANSLATOR, "コンラング翻訳");
        zh(CONLANG_TRANSLATOR, "构造语翻译");

        ja(LANGUAGES, "言語");
        zh(LANGUAGES, "语言");

        ja(LANGUAGES_D, "言語 %d 件");
        zh(LANGUAGES_D, "%d 种语言");

        ja(EDITED, "(編集済み)");
        zh(EDITED, "(已编辑)");

        ja(THE_LANGUAGES_YOU_TRANSLATE_INTO, "本の台詞を翻訳する人工言語です。言語は本またはフォルダーごとに選びます。モデルが作った単語は語彙に追加されます。");
        zh(THE_LANGUAGES_YOU_TRANSLATE_INTO, "你的书的对白所翻译成的构造语。语言按书或文件夹选择;模型创造的词会加入其词汇表。");

        ja(THE_LANGUAGES_CAN_NOT_BE_LOADED, "言語を読み込めませんでした: %s");
        zh(THE_LANGUAGES_CAN_NOT_BE_LOADED, "无法加载语言:%s");

        ja(NEW_LANGUAGE, "新しい言語");
        zh(NEW_LANGUAGE, "新建语言");

        ja(LANGUAGE_S, "言語: %s");
        zh(LANGUAGE_S, "语言:%s");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(WORDS, "単語");
        zh(WORDS, "词数");

        ja(NEW, "新規");
        zh(NEW, "新增");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "描述");

        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(EDIT, "編集");
        zh(EDIT, "编辑");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(DELETE_S, "%s を削除しますか?");
        zh(DELETE_S, "删除 %s?");

        ja(DELETE_THE_LANGUAGE_S, "言語 %s を、規則と語彙ごと削除しますか?この言語を使う本は言語なしになります。");
        zh(DELETE_THE_LANGUAGE_S, "删除语言 %s 及其规则和词汇表?使用它的书将没有语言。");

        ja(DUPLICATE, "複製");
        zh(DUPLICATE, "复制");

        ja(EXPORT, "エクスポート");
        zh(EXPORT, "导出");

        ja(IMPORT, "インポート");
        zh(IMPORT, "导入");

        ja(EXPORT_THE_LANGUAGE_S, "言語 %s をエクスポート");
        zh(EXPORT_THE_LANGUAGE_S, "导出语言 %s");

        ja(IMPORT_A_LANGUAGE, "言語をインポート");
        zh(IMPORT_A_LANGUAGE, "导入语言");

        ja(IMPORTED_LANGUAGE, "インポートした言語");
        zh(IMPORTED_LANGUAGE, "导入的语言");

        ja(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "ここにドキュメントを貼り付けるか、上でファイルを選択します");
        zh(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE, "在此粘贴文档,或在上方选择文件");

        ja(THE_DOCUMENT_HAS_THE_RULES, "ドキュメントには規則・カテゴリ・語域・語彙・話者が含まれます。例の本とロアブックへのリンクは含まれません。");
        zh(THE_DOCUMENT_HAS_THE_RULES, "文档包含规则、类别、语域、词汇表和说话者;不含示例书和设定集的链接。");

        ja(DOWNLOAD, "ダウンロード");
        zh(DOWNLOAD, "下载");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(OK, "OK");
        zh(OK, "确定");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(MAXIMIZE, "最大化");
        zh(MAXIMIZE, "最大化");

        ja(RESTORE_SIZE, "元のサイズに戻す");
        zh(RESTORE_SIZE, "还原大小");

        ja(LANGUAGE_OF_THE_SPEECH, "台詞の言語");
        zh(LANGUAGE_OF_THE_SPEECH, "对白的语言");

        ja(THE_SPEECH_IS_TRANSLATED_INTO, "登場人物の台詞を翻訳する言語です。空欄の場合、この本は翻訳されません。");
        zh(THE_SPEECH_IS_TRANSLATED_INTO, "角色对白翻译成的语言。留空:这本书不翻译。");

        ja(TRANSLATE_BY_ITSELF, "新しいパートを自動で翻訳");
        zh(TRANSLATE_BY_ITSELF, "自动翻译新的段落");

        ja(A_NEW_PART_IS_TRANSLATED, "新しいパートは、本に自動注釈がある場合はそれが終わった後、確定した時点で翻訳されます。");
        zh(A_NEW_PART_IS_TRANSLATED, "新段落在定稿后翻译;若书有自动批注,则在其之后。");

        ja(YES_TEXT, "はい");
        zh(YES_TEXT, "是");

        ja(NO_TEXT, "いいえ");
        zh(NO_TEXT, "否");

        ja(MODEL_OF_THE_TRANSLATION, "翻訳に使うモデル");
        zh(MODEL_OF_THE_TRANSLATION, "翻译所用的模型");

        ja(PROTOCOL_OF_THE_TRANSLATION, "翻訳に使うプロトコル");
        zh(PROTOCOL_OF_THE_TRANSLATION, "翻译所用的协议");

        ja(EMPTY_THE_MODEL_OF_THE_BOOK, "空欄の場合: 本のモデル、なければ既定のモデル。");
        zh(EMPTY_THE_MODEL_OF_THE_BOOK, "留空:使用书的模型,否则使用你的默认模型。");

        ja(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "空欄の場合: 本のプロトコル、なければ既定のプロトコル。");
        zh(EMPTY_THE_PROTOCOL_OF_THE_BOOK, "留空:使用书的协议,否则使用你的默认协议。");

        ja(WHAT_BOOKS_AND_FOLDERS_INHERIT, "本とフォルダーが引き継ぐ設定です。本やフォルダーは、各ダイアログの「拡張機能設定」で独自に選べます。");
        zh(WHAT_BOOKS_AND_FOLDERS_INHERIT, "书和文件夹继承的设置。书或文件夹可在其对话框的“扩展设置”中自行选择。");

        ja(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "空欄の場合: 本またはフォルダーが選ばない限り言語なし。");
        zh(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK, "留空:没有语言,除非书或文件夹另行选择。");

        ja(OVERVIEW, "概要");
        zh(OVERVIEW, "概览");

        ja(RULES, "規則");
        zh(RULES, "规则");

        ja(CATEGORIES, "カテゴリ");
        zh(CATEGORIES, "类别");

        ja(REGISTERS, "語域");
        zh(REGISTERS, "语域");

        ja(LEXICON, "語彙");
        zh(LEXICON, "词汇表");

        ja(SPEAKERS, "話者");
        zh(SPEAKERS, "说话者");

        ja(A_HYPHEN_IS_PART_OF_A_WORD, "ハイフンは単語の一部");
        zh(A_HYPHEN_IS_PART_OF_A_WORD, "连字符是词的一部分");

        ja(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "オフ: ハイフンの有無が違う同じ複合語は同じ単語として扱います。オン: ハイフンも語形の一部です。");
        zh(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT, "关闭:带连字符与不带连字符的同一复合词视为同一个词。开启:连字符属于词形。");

        ja(WHAT_THE_LANGUAGE_IS_AND_WHO, "この言語が何で、誰が話すか(自分用のメモです。翻訳モデルが読むのは規則で、これではありません)");
        zh(WHAT_THE_LANGUAGE_IS_AND_WHO, "这门语言是什么、谁在说(给你自己看;翻译模型读的是规则,不是这里)");

        ja(EXAMPLE_BOOK, "例の本");
        zh(EXAMPLE_BOOK, "示例书");

        ja(EXAMPLE_LOREBOOK, "例のロアブック");
        zh(EXAMPLE_LOREBOOK, "示例设定集");

        ja(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "デザイナーはこの本の台詞で言語を試します。");
        zh(THE_DESIGNER_TESTS_ON_ITS_SPEECH, "设计器用这本书的对白试验语言。");

        ja(THE_DESIGNER_READS_THE_WORLD, "デザイナーはこのロアブックの人物と場所を読みます。");
        zh(THE_DESIGNER_READS_THE_WORLD, "设计器读取这本设定集中的人物和地点。");

        ja(IT_DOES_NOT_EXIST_ANY_MORE, "既に存在しません。デザイナーはなしで動作します。");
        zh(IT_DOES_NOT_EXIST_ANY_MORE, "它已不存在;设计器将在没有它的情况下工作。");

        ja(THE_RULES_ARE_TEXT_THE_PLUGIN, "規則はセクションに分けたテキストで、プラグインは解析しません。各セクションの読み手を選びます: 翻訳モデル、デザイナーのみ、または自分だけ。");
        zh(THE_RULES_ARE_TEXT_THE_PLUGIN, "规则是分节的文本,插件不会解析。为每一节选择读者:翻译模型、仅设计器,或只有你自己。");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");

        ja(TEXT, "テキスト");
        zh(TEXT, "文本");

        ja(READ_BY, "読み手");
        zh(READ_BY, "读者");

        ja(AUDIENCE_TRANSLATOR, "翻訳モデル");
        zh(AUDIENCE_TRANSLATOR, "翻译模型");

        ja(AUDIENCE_DESIGNER, "デザイナーのみ");
        zh(AUDIENCE_DESIGNER, "仅设计器");

        ja(AUDIENCE_NOBODY, "なし(自分用メモ)");
        zh(AUDIENCE_NOBODY, "无人(我的笔记)");

        ja(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "翻訳モデルは、翻訳モデル向けのセクションをすべてのリクエストで受け取ります。");
        zh(THE_TRANSLATOR_GETS_MODEL_SECTIONS, "翻译模型在每次请求中都会收到面向翻译模型的小节。");

        ja(NEW_SECTION, "新しいセクション");
        zh(NEW_SECTION, "新建小节");

        ja(EDIT_SECTION, "セクションを編集");
        zh(EDIT_SECTION, "编辑小节");

        ja(THE_TITLE_IS_EMPTY, "タイトルが空です");
        zh(THE_TITLE_IS_EMPTY, "标题为空");

        ja(THE_LEXICON_IS_SHOWN_AND_SENT, "語彙はこの見出しの下で表示され、モデルにも送られます。常に送るカテゴリは、語彙が大きすぎて全部送れない場合でも毎回のリクエストに含まれます。");
        zh(THE_LEXICON_IS_SHOWN_AND_SENT, "词汇表按这些标题显示并发送给模型。始终发送的类别即使词汇表过大无法全部发送,也会包含在每次请求中。");

        ja(CATEGORY, "カテゴリ");
        zh(CATEGORY, "类别");

        ja(ALWAYS_SENT, "常に送信");
        zh(ALWAYS_SENT, "始终发送");

        ja(ALWAYS_SEND_THE_WHOLE_CATEGORY, "カテゴリ全体を常に送る");
        zh(ALWAYS_SEND_THE_WHOLE_CATEGORY, "始终发送整个类别");

        ja(PARTICLES_NUMBERS_KINSHIP, "助詞・数詞・親族名称など、どのパートでも翻訳に必要な単語向けです。");
        zh(PARTICLES_NUMBERS_KINSHIP, "适用于助词、数词、亲属称谓:翻译每个段落都需要的词。");

        ja(NEW_CATEGORY, "新しいカテゴリ");
        zh(NEW_CATEGORY, "新建类别");

        ja(EDIT_CATEGORY, "カテゴリを編集");
        zh(EDIT_CATEGORY, "编辑类别");

        ja(THE_NAME_IS_EMPTY, "名前が空です");
        zh(THE_NAME_IS_EMPTY, "名称为空");

        ja(ANOTHER_LANGUAGE_HAS_THE_NAME, "同じ名前の言語が既にあります");
        zh(ANOTHER_LANGUAGE_HAS_THE_NAME, "已有另一种语言使用此名称");

        ja(THE_MODEL_CHOOSES_THE_FORM, "モデルは、話し手と状況に合わせた語域(丁寧、柔らかい、粗野、古風など)で単語の形を選びます。");
        zh(THE_MODEL_CHOOSES_THE_FORM, "模型根据说话者和情境所需的语域(正式、柔和、粗俗、古语等)选择词形。");

        ja(WHEN_IT_IS_USED, "使われる場面");
        zh(WHEN_IT_IS_USED, "使用场合");

        ja(NEW_REGISTER, "新しい語域");
        zh(NEW_REGISTER, "新建语域");

        ja(EDIT_REGISTER, "語域を編集");
        zh(EDIT_REGISTER, "编辑语域");

        ja(SEARCH_BY_WORD_OR_MEANING, "単語または意味で検索");
        zh(SEARCH_BY_WORD_OR_MEANING, "按词或词义搜索");

        ja(ALL_CATEGORIES, "すべてのカテゴリ");
        zh(ALL_CATEGORIES, "所有类别");

        ja(ALL_STATUSES, "すべての状態");
        zh(ALL_STATUSES, "所有状态");

        ja(WORDS_D_OF_D_D_NEW, "%d / %d 語、新規 %d");
        zh(WORDS_D_OF_D_D_NEW, "%d / %d 个词,新增 %d");

        ja(ACCEPT, "承認");
        zh(ACCEPT, "接受");

        ja(ACCEPT_ALL_NEW, "新規をすべて承認");
        zh(ACCEPT_ALL_NEW, "接受所有新增");

        ja(ACCEPT_D_NEW_WORDS, "新規の単語 %d 個をそのまま承認しますか?");
        zh(ACCEPT_D_NEW_WORDS, "按原样接受 %d 个新词?");

        ja(LOCK, "ロック");
        zh(LOCK, "锁定");

        ja(UNLOCK, "ロック解除");
        zh(UNLOCK, "解锁");

        ja(RETIRE, "廃止");
        zh(RETIRE, "停用");

        ja(RESTORE, "復元");
        zh(RESTORE, "恢复");

        ja(WORD, "単語");
        zh(WORD, "词");

        ja(IN_THE_SCRIPT, "文字体系での表記");
        zh(IN_THE_SCRIPT, "文字写法");

        ja(MEANING, "意味");
        zh(MEANING, "词义");

        ja(MEANINGS, "意味");
        zh(MEANINGS, "词义");

        ja(PART_OF_SPEECH, "品詞");
        zh(PART_OF_SPEECH, "词性");

        ja(NOTE, "メモ");
        zh(NOTE, "备注");

        ja(NOTES, "メモ");
        zh(NOTES, "备注");

        ja(KIND, "種類");
        zh(KIND, "种类");

        ja(REGISTER, "語域");
        zh(REGISTER, "语域");

        ja(NEUTRAL, "中立");
        zh(NEUTRAL, "中性");

        ja(STATUS, "状態");
        zh(STATUS, "状态");

        ja(KIND_ROOT, "語根");
        zh(KIND_ROOT, "词根");

        ja(KIND_COMPOUND, "複合語");
        zh(KIND_COMPOUND, "复合词");

        ja(KIND_AFFIX, "接辞");
        zh(KIND_AFFIX, "词缀");

        ja(KIND_PARTICLE, "助詞");
        zh(KIND_PARTICLE, "助词");

        ja(KIND_NAME, "固有名詞");
        zh(KIND_NAME, "名称");

        ja(KIND_NUMBER, "数詞");
        zh(KIND_NUMBER, "数词");

        ja(KIND_PHRASE, "句");
        zh(KIND_PHRASE, "短语");

        ja(KIND_OTHER, "その他");
        zh(KIND_OTHER, "其他");

        ja(STATUS_NEW, "新規");
        zh(STATUS_NEW, "新增");

        ja(STATUS_ACCEPTED, "承認済み");
        zh(STATUS_ACCEPTED, "已接受");

        ja(STATUS_LOCKED, "ロック済み");
        zh(STATUS_LOCKED, "已锁定");

        ja(STATUS_RETIRED, "廃止");
        zh(STATUS_RETIRED, "已停用");

        ja(NEW_WORD, "新しい単語");
        zh(NEW_WORD, "新建词");

        ja(EDIT_WORD, "単語を編集");
        zh(EDIT_WORD, "编辑词");

        ja(NEW_MEANING, "新しい意味");
        zh(NEW_MEANING, "新建词义");

        ja(EDIT_MEANING, "意味を編集");
        zh(EDIT_MEANING, "编辑词义");

        ja(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "独自の文字体系がない場合は空欄");
        zh(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT, "语言没有自己的文字时留空");

        ja(OTHER_FORMS_OF_THE_SAME_CONCEPT, "同じ概念の他の形");
        zh(OTHER_FORMS_OF_THE_SAME_CONCEPT, "同一概念的其他词形");

        ja(THE_FORMS_FOR_OTHER_REGISTERS, "この概念が他の語域(丁寧、柔らかい、粗野など)で持つ形");
        zh(THE_FORMS_FOR_OTHER_REGISTERS, "该概念在其他语域(正式、柔和、粗俗等)中的词形");

        ja(WHY_THE_WORD_IS_AS_IT_IS, "この単語がこの形である理由");
        zh(WHY_THE_WORD_IS_AS_IT_IS, "这个词为何如此");

        ja(RULE_IT_FOLLOWS, "従う規則");
        zh(RULE_IT_FOLLOWS, "遵循的规则");

        ja(MADE_BY_THE_AUTHOR, "作者が作成");
        zh(MADE_BY_THE_AUTHOR, "作者创造");

        ja(MADE_BY_THE_MODEL, "モデル %s が作成。保存すると作者のものになります");
        zh(MADE_BY_THE_MODEL, "由模型 %s 创造;保存后归作者所有");

        ja(THE_WORD_IS_EMPTY, "単語が空です");
        zh(THE_WORD_IS_EMPTY, "词为空");

        ja(A_WORD_NEEDS_A_MEANING, "単語には少なくとも1つの意味が必要です");
        zh(A_WORD_NEEDS_A_MEANING, "一个词至少需要一个词义");

        ja(THE_MEANING_IS_EMPTY, "意味が空です");
        zh(THE_MEANING_IS_EMPTY, "词义为空");

        ja(D_WORDS_D_NEW, "%d 語、新規 %d");
        zh(D_WORDS_D_NEW, "%d 个词,新增 %d");

        ja(SELECT_ALL, "すべて選択");
        zh(SELECT_ALL, "全选");

        ja(SELECT_NONE, "選択解除");
        zh(SELECT_NONE, "全不选");

        ja(SELECT_WITHOUT_CONFLICTS, "競合のないものを選択");
        zh(SELECT_WITHOUT_CONFLICTS, "选择无冲突项");

        ja(EVERYTHING, "すべて");
        zh(EVERYTHING, "全部");

        ja(TARGET_LANGUAGE, "言語");
        zh(TARGET_LANGUAGE, "语言");

        ja(TARGET_RULE, "規則セクション");
        zh(TARGET_RULE, "规则小节");

        ja(TARGET_CATEGORY, "カテゴリ");
        zh(TARGET_CATEGORY, "类别");

        ja(TARGET_REGISTER, "語域");
        zh(TARGET_REGISTER, "语域");

        ja(TARGET_WORD, "単語");
        zh(TARGET_WORD, "词");

        ja(KIND_ADD, "追加");
        zh(KIND_ADD, "新增");

        ja(KIND_CHANGE, "変更");
        zh(KIND_CHANGE, "修改");

        ja(KIND_DELETE, "削除");
        zh(KIND_DELETE, "删除");

        ja(KIND_MOVE, "移動");
        zh(KIND_MOVE, "移动");

        ja(CHANGE, "変更");
        zh(CHANGE, "变更");

        ja(ABOUT, "対象");
        zh(ABOUT, "对象");

        ja(FIELDS, "項目");
        zh(FIELDS, "字段");

        ja(CONFLICT_TEXT, "競合");
        zh(CONFLICT_TEXT, "冲突");

        ja(D_CHANGES_D_IN_CONFLICT, "変更 %d 件、競合 %d 件");
        zh(D_CHANGES_D_IN_CONFLICT, "%d 项变更,%d 项冲突");

        ja(APPLY_D_PICKED, "選択した %d 件を適用");
        zh(APPLY_D_PICKED, "应用所选的 %d 项");

        ja(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "変更を選択すると項目が表示されます。");
        zh(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS, "选择一项变更以查看其字段。");

        ja(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "この変更は別の変更(使用する新しいカテゴリや語域)を必要とします。片方を選ぶともう片方も選ばれます。");
        zh(NEEDS_THE_CHANGE_OF_ANOTHER_THING, "此变更需要另一项变更(它使用的新类别或语域):选择其一会同时选择另一项。");

        ja(THE_THING_IS_DELETED, "これを選ぶと、こちらからも削除されます。");
        zh(THE_THING_IS_DELETED, "选择此项会在这里删除它。");

        ja(THE_THING_IS_MOVED, "これを選ぶと、もう一方の言語での位置に移動します。");
        zh(THE_THING_IS_MOVED, "选择此项会把它移到在另一种语言中的位置。");

        ja(BOTH_CHANGED_THIS, "両方が変更");
        zh(BOTH_CHANGED_THIS, "双方都修改了");

        ja(NOTHING, "なし");
        zh(NOTHING, "无");

        ja(HERE_NOW, "こちら(現在)");
        zh(HERE_NOW, "这里(当前)");

        ja(IN_THE_OTHER_LANGUAGE, "もう一方の言語");
        zh(IN_THE_OTHER_LANGUAGE, "另一种语言中");

        ja(WILL_BE_WRITTEN, "書き込まれる内容");
        zh(WILL_BE_WRITTEN, "将写入的内容");

        ja(KEEP_MINE, "こちらを残す");
        zh(KEEP_MINE, "保留我的");

        ja(TAKE_THEIRS, "相手を採用");
        zh(TAKE_THEIRS, "采用对方的");

        ja(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "一部の項目が両側で変更されています");
        zh(SOME_FIELDS_CHANGED_ON_BOTH_SIDES, "部分字段在双方都被修改");

        ja(CHANGED_HERE_DELETED_THERE_TEXT, "こちらで変更、もう一方で削除");
        zh(CHANGED_HERE_DELETED_THERE_TEXT, "这里修改了,另一种语言中删除了");

        ja(DELETED_HERE_CHANGED_THERE_TEXT, "こちらで削除、もう一方で変更。選ぶと復元されます");
        zh(DELETED_HERE_CHANGED_THERE_TEXT, "这里删除了,另一种语言中修改了:选择它会恢复");

        ja(THE_SAME_WORD_IS_HERE, "同じ語形で共通の意味を持つ単語が既にあります");
        zh(THE_SAME_WORD_IS_HERE, "这里已有形式相同且词义相通的词");

        ja(MOVED_ON_BOTH_SIDES, "両側で移動されています");
        zh(MOVED_ON_BOTH_SIDES, "双方都移动了");

        ja(COMPARE, "比較");
        zh(COMPARE, "比较");

        ja(BRING_CHANGES_FROM, "変更の取り込み元");
        zh(BRING_CHANGES_FROM, "从以下语言带入变更");

        ja(COMPARE_S, "%s を別の言語と比較");
        zh(COMPARE_S, "将 %s 与另一种语言比较");

        ja(NO_OTHER_LANGUAGE, "比較できる他の言語がありません。");
        zh(NO_OTHER_LANGUAGE, "没有可比较的其他语言。");

        ja(THE_LANGUAGES_ARE_THE_SAME, "2つの言語は同じです。");
        zh(THE_LANGUAGES_ARE_THE_SAME, "两种语言相同。");

        ja(BRING_THE_CHANGES_OF_S_INTO_S, "%s の変更を %s に取り込む");
        zh(BRING_THE_CHANGES_OF_S_INTO_S, "把 %s 的变更带入 %s");

        ja(APPLIED_D_CHANGES, "変更を %d 件適用しました");
        zh(APPLIED_D_CHANGES, "已应用 %d 项变更");

        ja(SKIPPED_D_CHANGED_MEANWHILE, "%d 件の変更はスキップされました。言語がレビュー時と異なります");
        zh(SKIPPED_D_CHANGED_MEANWHILE, "已跳过 %d 项变更:语言与审阅时不同");

        ja(SKIPPED_D_NEED_ANOTHER, "%d 件の変更はスキップされました。必要な変更が選ばれていません");
        zh(SKIPPED_D_NEED_ANOTHER, "已跳过 %d 项变更:它们所需的变更未被选择");

        ja(A_LANGUAGE_NAMED_S_EXISTS, "%s という言語は既にライブラリにあります。差分を確認して必要なものだけ取り込みますか?それとも新しい言語としてインポートしますか?");
        zh(A_LANGUAGE_NAMED_S_EXISTS, "你的库中已有名为 %s 的语言。要审阅差异并采纳需要的部分,还是作为新语言导入?");

        ja(REVIEW_THE_DIFFERENCES, "差分を確認");
        zh(REVIEW_THE_DIFFERENCES, "审阅差异");

        ja(IMPORT_AS_NEW, "新規としてインポート");
        zh(IMPORT_AS_NEW, "作为新语言导入");

        ja(ACCEPTED_IN_THE_RULES, "承認しました。セクションが規則に追加されました。");
        zh(ACCEPTED_IN_THE_RULES, "已接受:该小节已加入规则。");

        ja(ADD_A_SECTION_WITH_AI, "AI でセクションを追加...");
        zh(ADD_A_SECTION_WITH_AI, "用 AI 添加小节...");

        ja(ADD_THE_BASIC_CATEGORIES, "基本カテゴリを追加");
        zh(ADD_THE_BASIC_CATEGORIES, "添加基本类别");

        ja(ASK_ABOUT_THE_LANGUAGE, "言語について質問する、または変更を依頼...");
        zh(ASK_ABOUT_THE_LANGUAGE, "询问这门语言,或请求修改...");

        ja(ASK_THE_DESIGNER, "デザイナーに質問...");
        zh(ASK_THE_DESIGNER, "询问设计器...");

        ja(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "まだ存在しない名前を入力すると新しいカテゴリが作られます。");
        zh(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE, "输入尚不存在的名称会新建类别。");

        ja(BACK, "戻る");
        zh(BACK, "上一步");

        ja(BRIEF, "概要書き");
        zh(BRIEF, "简述");

        ja(CHOOSE_A_SECTION, "セクションを選択してください");
        zh(CHOOSE_A_SECTION, "请选择小节");

        ja(CONSTRAINTS_AND_WHAT_TO_AVOID, "制約と避けたいこと");
        zh(CONSTRAINTS_AND_WHAT_TO_AVOID, "限制与要避免的内容");

        ja(DESIGN_A_LANGUAGE, "言語をデザイン");
        zh(DESIGN_A_LANGUAGE, "设计语言");

        ja(DESIGN_WITH_AI, "AI でデザイン");
        zh(DESIGN_WITH_AI, "用 AI 设计");

        ja(DISCARD_THE_NEW_LANGUAGE, "新しい言語とここまでの作業を破棄しますか?");
        zh(DISCARD_THE_NEW_LANGUAGE, "放弃新语言及目前的成果?");

        ja(EDIT_THE_PROPOSAL_YOURSELF, "テキストを自分で編集するか、上にメモを書いて再提案させて練り直せます。");
        zh(EDIT_THE_PROPOSAL_YOURSELF, "可自行编辑文本,或在上方写备注后再次提议以改进。");

        ja(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "空欄の場合: 最初の提案は概要書きから作られます。下に提案がある場合、メモは変更点を伝えます。");
        zh(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF, "留空:首次提议来自简述。下方已有提议时,备注说明要改什么。");

        ja(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "空欄の場合: 翻訳用のモデル、なければ既定のモデル。");
        zh(EMPTY_THE_MODEL_OF_THE_TRANSLATION, "留空:使用翻译所用的模型,否则使用你的默认模型。");

        ja(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "空欄の場合: 翻訳用のプロトコル、なければ既定のプロトコル。");
        zh(EMPTY_THE_PROTOCOL_OF_THE_TRANSLATION, "留空:使用翻译所用的协议,否则使用你的默认协议。");

        ja(FINISH, "完了");
        zh(FINISH, "完成");

        ja(FIX, "修正案");
        zh(FIX, "修复建议");

        ja(GENERATE_WORDS, "単語を生成...");
        zh(GENERATE_WORDS, "生成词汇...");

        ja(HARSH_LIKE_OLD_NORSE, "例: 古ノルド語のように荒々しい、ポリネシア語のように歌うような。");
        zh(HARSH_LIKE_OLD_NORSE, "例如:像古诺尔斯语一样粗犷;像波利尼西亚语一样如歌。");

        ja(HOW_MANY_WORDS, "単語数");
        zh(HOW_MANY_WORDS, "词数");

        ja(INSPIRATIONS, "着想元");
        zh(INSPIRATIONS, "灵感来源");

        ja(IN_THE_LANGUAGE, "言語での表記");
        zh(IN_THE_LANGUAGE, "该语言中的写法");

        ja(LEFT_OUT_OF_THE_PROPOSAL, "提案から除外されたもの:");
        zh(LEFT_OUT_OF_THE_PROPOSAL, "未纳入提议的内容:");

        ja(MODEL_OF_THE_DESIGNER, "デザイナーに使うモデル");
        zh(MODEL_OF_THE_DESIGNER, "设计器所用的模型");

        ja(NEW_WORDS_OF_THE_SAMPLES, "サンプルの新しい単語");
        zh(NEW_WORDS_OF_THE_SAMPLES, "示例中的新词");

        ja(NEXT, "次へ");
        zh(NEXT, "下一步");

        ja(NOTHING_FOUND, "報告することはありません。");
        zh(NOTHING_FOUND, "没有要报告的内容。");

        ja(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "例: 英語の読者が発音できない音は使わない、短い単語、紙面での見た目。");
        zh(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY, "例如:不使用英语读者无法发出的音、词要短、语言在页面上应有的样子。");

        ja(PROBLEM, "問題点");
        zh(PROBLEM, "问题");

        ja(PROPOSE, "提案");
        zh(PROPOSE, "提议");

        ja(PROTOCOL_OF_THE_DESIGNER, "デザイナーに使うプロトコル");
        zh(PROTOCOL_OF_THE_DESIGNER, "设计器所用的协议");

        ja(REVIEW_OF_S, "%s のレビュー");
        zh(REVIEW_OF_S, "%s 的审阅");

        ja(REVIEW_THE_LANGUAGE, "言語をレビュー");
        zh(REVIEW_THE_LANGUAGE, "审阅语言");

        ja(REVIEW_THE_PROPOSED_CHANGES, "提案された変更を確認");
        zh(REVIEW_THE_PROPOSED_CHANGES, "审阅提议的变更");

        ja(REWRITE_A_SECTION, "セクションを書き直す...");
        zh(REWRITE_A_SECTION, "重写小节...");

        ja(REWRITE_WITH_AI, "AI で書き直す...");
        zh(REWRITE_WITH_AI, "用 AI 重写...");

        ja(SAY_WHAT_YOU_WANT, "まず希望を書いてください。");
        zh(SAY_WHAT_YOU_WANT, "请先说明你想要什么。");

        ja(SECTION, "セクション");
        zh(SECTION, "小节");

        ja(SEED_LEXICON, "基礎語彙");
        zh(SEED_LEXICON, "基础词汇");

        ja(SEND, "送信");
        zh(SEND, "发送");

        ja(SENTENCE, "文");
        zh(SENTENCE, "句子");

        ja(SENTENCES_ONE_PER_LINE, "文(1行に1文)");
        zh(SENTENCES_ONE_PER_LINE, "句子(每行一句)");

        ja(SEVERITY, "重大度");
        zh(SEVERITY, "严重程度");

        ja(STAGE_GRAMMAR, "文法");
        zh(STAGE_GRAMMAR, "语法");

        ja(STAGE_SOUNDS, "音");
        zh(STAGE_SOUNDS, "语音");

        ja(STAGE_WORD_FORMATION, "語形成");
        zh(STAGE_WORD_FORMATION, "构词");

        ja(STAGE_WRITING, "表記");
        zh(STAGE_WRITING, "文字");

        ja(STEP_D_OF_D_S, "ステップ %d / %d: %s");
        zh(STEP_D_OF_D_S, "第 %d 步,共 %d 步:%s");

        ja(STOP_WAITING, "待機をやめる");
        zh(STOP_WAITING, "不再等待");

        ja(TELL_THE_DESIGNER_WHAT_YOU_WANT, "デザイナーへの要望");
        zh(TELL_THE_DESIGNER_WHAT_YOU_WANT, "告诉设计器你想要什么");

        ja(TEST, "テスト");
        zh(TEST, "测试");

        ja(THE_DESIGNER_FAILED, "デザイナーが失敗しました: %s");
        zh(THE_DESIGNER_FAILED, "设计器失败:%s");

        ja(THE_DESIGNER_IS_REVIEWING, "デザイナーが言語をレビュー中...");
        zh(THE_DESIGNER_IS_REVIEWING, "设计器正在审阅语言...");

        ja(THE_DESIGNER_IS_THINKING, "デザイナーが考え中...");
        zh(THE_DESIGNER_IS_THINKING, "设计器正在思考...");

        ja(THE_DESIGNER_IS_WORKING, "デザイナーが作業中です。1分以上かかることがあります...");
        zh(THE_DESIGNER_IS_WORKING, "设计器正在工作,可能需要一分钟或更久...");

        ja(THE_DESIGNER_PROPOSES_NOTHING, "デザイナーは書き込む提案をしていません。");
        zh(THE_DESIGNER_PROPOSES_NOTHING, "设计器没有提出可写入的内容。");

        ja(THE_DESIGNER_PROPOSES_THE_SECTION, "デザイナーが概要書きとここまでの承認内容から、規則のセクション「%s」を提案します。");
        zh(THE_DESIGNER_PROPOSES_THE_SECTION, "设计器根据简述和目前已接受的内容提议规则的“%s”小节。");

        ja(THE_DESIGNER_TRANSLATES_SAMPLES, "デザイナーが現在の言語であなたの文を翻訳します。読んでおかしな点を確認できます。足りない単語は新しい単語として提案されます。");
        zh(THE_DESIGNER_TRANSLATES_SAMPLES, "设计器用当前的语言翻译你的句子,方便你阅读并发现不对劲之处。语言缺少的词会作为新词提议。");

        ja(THE_PEOPLE_THEIR_WORLD, "話す人々、その世界と文化。");
        zh(THE_PEOPLE_THEIR_WORLD, "说这种语言的人、他们的世界和文化。");

        ja(THE_SECTION_IS_EMPTY, "セクションが空です");
        zh(THE_SECTION_IS_EMPTY, "小节为空");

        ja(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "最初の単語はカテゴリごとに作られ、書き込む前に毎回確認します。まず基本カテゴリ、その後に本の固有名詞を作りましょう。");
        zh(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY, "最初的词按类别生成,每一批写入前都要审阅。先从基本类别开始,再做你书中的名称。");

        ja(TRANSLATE_THE_SENTENCES, "文を翻訳");
        zh(TRANSLATE_THE_SENTENCES, "翻译句子");

        ja(WHAT_DO_YOU_WANT, "何をしたいですか?");
        zh(WHAT_DO_YOU_WANT, "你想要什么?");

        ja(WHAT_THE_LANGUAGE_IS_FOR, "この言語の目的");
        zh(WHAT_THE_LANGUAGE_IS_FOR, "这门语言的用途");

        ja(WHAT_THE_WORDS_ARE_FOR, "単語の用途(任意)");
        zh(WHAT_THE_WORDS_ARE_FOR, "词的用途(可选)");

        ja(THE_DESIGNER_INVENTS_WORDS, "デザイナーが言語の規則と既存の語彙に従って新しい単語を作ります。何かが書き込まれる前に確認できます。");
        zh(THE_DESIGNER_INVENTS_WORDS, "设计器按语言的规则和现有词汇创造新词。写入之前你可以审阅。");

        ja(THE_DESIGNER_WRITES_A_SECTION, "デザイナーが規則に欠けているもの(数の体系、色、敬語など)の新しいセクションと必要な単語を書きます。書き込む前に確認できます。");
        zh(THE_DESIGNER_WRITES_A_SECTION, "设计器为规则中缺少的内容(计数系统、颜色、敬语等)撰写新小节及所需词汇。写入之前你可以审阅。");

        ja(THE_DESIGNER_REWRITES, "デザイナーが依頼どおりにセクションを書き直します。書き込む前に変更点を確認できます。");
        zh(THE_DESIGNER_REWRITES, "设计器按你的要求重写小节。写入之前你可以看到改动。");

        ja(THE_DESIGNER_REVIEWS, "デザイナーが規則と語彙を読み、矛盾・不足・音体系に反する単語を一覧にします。何も変更されません。");
        zh(THE_DESIGNER_REVIEWS, "设计器阅读规则和词汇,列出矛盾、缺漏和违反语音系统的词。不会修改任何内容。");

        ja(READERS_SEE, "公開された本の読者に見えるもの");
        zh(READERS_SEE, "已发布图书的读者看到的内容");

        ja(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "人工言語。クリックすると英語");
        zh(CONLANG_WITH_THE_ENGLISH_ON_CLICK, "构造语言,点击显示英文");

        ja(CONLANG_ONLY, "人工言語のみ");
        zh(CONLANG_ONLY, "仅构造语言");

        ja(ENGLISH_ONLY, "英語のみ");
        zh(ENGLISH_ONLY, "仅英文");

        ja(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "翻訳された台詞をビューアでどう表示するか。人工言語のみの場合、英語は読者に送信されません。ご自身の画面には影響しません。");
        zh(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE, "翻译后的对白在阅读器中的显示方式。选择仅构造语言时,英文不会发送给读者。不影响你自己的视图。");

        ja(TRANSLATED_SPEECH, "翻訳された台詞");
        zh(TRANSLATED_SPEECH, "翻译后的对白");

        ja(THE_CONLANG_AS_IT_IS, "人工言語のまま");
        zh(THE_CONLANG_AS_IT_IS, "保持构造语言原样");

        ja(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "人工言語。英語は脚注");
        zh(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES, "构造语言,英文放脚注");

        ja(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "英語。人工言語は脚注");
        zh(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES, "英文,构造语言放脚注");

        ja(TRANSLATE_PARTS, "パートを翻訳");
        zh(TRANSLATE_PARTS, "翻译段落");

        ja(FROM_PART, "開始パート");
        zh(FROM_PART, "起始段落");

        ja(TO_PART, "終了パート");
        zh(TO_PART, "结束段落");

        ja(START, "開始");
        zh(START, "开始");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "未翻訳のパートを翻訳");
        zh(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "翻译尚未翻译的段落");

        ja(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "翻訳済みのパートを再翻訳");
        zh(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "重新翻译已翻译的段落");

        ja(PUT_THE_ENGLISH_BACK, "英語に戻す");
        zh(PUT_THE_ENGLISH_BACK, "恢复为英文");

        ja(PARTS_IN_THE_RANGE_D_TO_DO_D, "範囲内のパート:%d、対象:%d");
        zh(PARTS_IN_THE_RANGE_D_TO_DO_D, "范围内 %d 段,待处理 %d 段");

        ja(NOTHING_TO_DO_IN_THE_RANGE, "範囲内に対象がありません");
        zh(NOTHING_TO_DO_IN_THE_RANGE, "范围内没有需要处理的段落");

        ja(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "%d個のパートを英語に戻しますか?人工言語は各パートの履歴に残り、編集した台詞はそのままです。");
        zh(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "要将 %d 段恢复为英文吗?构造语言保留在每段的历史中,你编辑过的对白保持不变。");

        ja(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "この本の範囲を処理中です");
        zh(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "本书已有一个范围正在处理");

        ja(STOPPING_AFTER_THIS_PART, "このパートの後に停止します...");
        zh(STOPPING_AFTER_THIS_PART, "处理完这一段后停止...");

        ja(PART_D_OF_D, "パート %d / %d");
        zh(PART_D_OF_D, "第 %d / %d 段");

        ja(D_PARTS_CHANGED_D_SPEECHES_D_NEW_WORDS, "%d個のパートを変更、台詞%d件、新語%d個。");
        zh(D_PARTS_CHANGED_D_SPEECHES_D_NEW_WORDS, "已修改 %d 段,%d 句对白,%d 个新词。");

        ja(D_EDITED_SPEECHES_STAY, "編集された台詞%d件はそのままです。");
        zh(D_EDITED_SPEECHES_STAY, "%d 句编辑过的对白保持不变。");

        ja(D_PARTS_FAILED_S, "%d個のパートが失敗しました:%s");
        zh(D_PARTS_FAILED_S, "%d 段失败:%s");

        ja(STOPPED_AFTER_D_OF_D_PARTS, "%d / %d個のパートの後に停止しました。");
        zh(STOPPED_AFTER_D_OF_D_PARTS, "在 %d / %d 段后停止。");

        ja(CONLANG, "コンラング");
        zh(CONLANG, "人造语");

        ja(QUICK_TRANSLATE, "クイック翻訳");
        zh(QUICK_TRANSLATE, "快速翻译");

        ja(REFRESH, "再読み込み");
        zh(REFRESH, "刷新");

        ja(TEXT_TO_TRANSLATE, "翻訳するテキスト(英語)");
        zh(TEXT_TO_TRANSLATE, "要翻译的文本(英文)");

        ja(WHO_SAYS_IT_TO_WHOM, "誰が誰に言うか(任意)");
        zh(WHO_SAYS_IT_TO_WHOM, "谁对谁说(可选)");

        ja(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "テキストまたはここに名前がある人物は、「話者」タブのメモとともにモデルに伝えられます。");
        zh(THE_NOTES_OF_THE_SPEAKERS_ARE_USED, "文本或此处提到的角色,会连同“说话者”标签中的备注一起告诉模型。");

        ja(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "テキストは本の言語に翻訳されます。物語には何も書き込まれません。翻訳を使い、必要な新語を語彙に取り込めます。");
        zh(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN, "文本会被翻译成本书的语言。不会写入故事;你自行使用译文,并可把需要的新词导入词汇表。");

        ja(TRANSLATE, "翻訳");
        zh(TRANSLATE, "翻译");

        ja(TRANSLATION, "翻訳結果");
        zh(TRANSLATION, "译文");

        ja(COPY, "コピー");
        zh(COPY, "复制");

        ja(COPIED, "コピーしました");
        zh(COPIED, "已复制");

        ja(THERE_IS_NO_TEXT_TO_TRANSLATE, "翻訳するテキストがありません");
        zh(THERE_IS_NO_TEXT_TO_TRANSLATE, "没有要翻译的文本");

        ja(THE_WORDS_THE_TRANSLATION_NEEDED, "翻訳に必要で語彙になかった単語");
        zh(THE_WORDS_THE_TRANSLATION_NEEDED, "译文需要而词汇表中没有的词");

        ja(IMPORT_THE_SELECTED_WORDS, "選択した単語を取り込む");
        zh(IMPORT_THE_SELECTED_WORDS, "导入所选的词");

        ja(IMPORTED_D_WORDS_AS_NEW, "%d個の単語を新規として取り込みました");
        zh(IMPORTED_D_WORDS_AS_NEW, "已将 %d 个词作为新词导入");

        ja(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "語彙にも新語にもない単語: %s");
        zh(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS, "既不在词汇表也不在新词中的词:%s");

        ja(TRANSLATE_THE_SELECTION, "選択範囲を翻訳");
        zh(TRANSLATE_THE_SELECTION, "翻译所选内容");

        ja(TRANSLATING_THE_SELECTION, "選択したテキストを翻訳中...");
        zh(TRANSLATING_THE_SELECTION, "正在翻译所选文本...");

        ja(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "選択範囲に翻訳済みの台詞があります。先に英語に戻してください");
        zh(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH, "所选内容包含已翻译的对白,请先恢复为英文");

        ja(THE_PART_WAS_CHANGED_MEANWHILE, "その間にパートが変更されました。もう一度選択してください");
        zh(THE_PART_WAS_CHANGED_MEANWHILE, "该段在此期间已被修改,请重新选择文本");
    }
}
