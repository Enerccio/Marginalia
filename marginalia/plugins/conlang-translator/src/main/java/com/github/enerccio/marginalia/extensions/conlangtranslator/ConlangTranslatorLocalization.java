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
    public static final String MOVE_UP = "Move up";
    public static final String MOVE_DOWN = "Move down";
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
    public static final String EVERYBODY_SPEAKS_IT_EXCEPT = "Everybody speaks it except the listed characters";
    public static final String ONLY_THE_LISTED_SPEAK_IT = "Only the listed characters speak it";
    public static final String ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART = "This is only the default: the text of the part overrides it (\"she said in the common tongue\", \"the guard could not understand a word\"). The notes help the translator keep a voice.";
    public static final String ALSO_CALLED = "Also called";
    public static final String SEPARATED_BY_COMMAS = "Separated by commas";
    public static final String GENDER = "Gender";
    public static final String VOICE = "Voice";
    public static final String HOW_THE_CHARACTER_SPEAKS_IT = "How the character speaks it: formal, clipped, coarse, learned...";
    public static final String LISTED = "Listed";
    public static final String LISTED_IN_THE_RULE_ABOVE = "Listed: counted by the rule above (an exception, or one of the only speakers)";
    public static final String NEW_CHARACTER = "New character";
    public static final String EDIT_CHARACTER = "Edit the character";
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
    public static final String LOOK_UP_A_WORD = "Look up a word";
    public static final String WORDS_OF_S = "Words of %s";
    public static final String D_WORDS_D_NEW = "%d words, %d new";

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

        ja(MOVE_UP, "上へ移動");
        zh(MOVE_UP, "上移");

        ja(MOVE_DOWN, "下へ移動");
        zh(MOVE_DOWN, "下移");

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

        ja(EVERYBODY_SPEAKS_IT_EXCEPT, "一覧の人物以外は全員が話す");
        zh(EVERYBODY_SPEAKS_IT_EXCEPT, "除列出的角色外,所有人都说");

        ja(ONLY_THE_LISTED_SPEAK_IT, "一覧の人物だけが話す");
        zh(ONLY_THE_LISTED_SPEAK_IT, "只有列出的角色说");

        ja(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "これは既定にすぎません。パートの本文が優先されます(「彼女は共通語で言った」「衛兵は一言も理解できなかった」など)。メモは翻訳モデルが話し方を保つのに役立ちます。");
        zh(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "这只是默认设置:段落文本优先(“她用通用语说”“守卫一个字也听不懂”)。备注帮助翻译模型保持口吻。");

        ja(ALSO_CALLED, "別名");
        zh(ALSO_CALLED, "别名");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");

        ja(GENDER, "性別");
        zh(GENDER, "性别");

        ja(VOICE, "話し方");
        zh(VOICE, "口吻");

        ja(HOW_THE_CHARACTER_SPEAKS_IT, "その人物の話し方: 丁寧、簡潔、粗野、学のある話し方など");
        zh(HOW_THE_CHARACTER_SPEAKS_IT, "该角色怎么说:正式、简短、粗鲁、有学识……");

        ja(LISTED, "一覧に含む");
        zh(LISTED, "已列出");

        ja(LISTED_IN_THE_RULE_ABOVE, "一覧に含める: 上の規則の対象(例外、または唯一の話者の一人)");
        zh(LISTED_IN_THE_RULE_ABOVE, "已列出:适用上面的规则(例外,或唯一的说话者之一)");

        ja(NEW_CHARACTER, "新しい人物");
        zh(NEW_CHARACTER, "新建角色");

        ja(EDIT_CHARACTER, "人物を編集");
        zh(EDIT_CHARACTER, "编辑角色");

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

        ja(LOOK_UP_A_WORD, "単語を調べる");
        zh(LOOK_UP_A_WORD, "查词");

        ja(WORDS_OF_S, "%s の単語");
        zh(WORDS_OF_S, "%s 的词汇");

        ja(D_WORDS_D_NEW, "%d 語、新規 %d");
        zh(D_WORDS_D_NEW, "%d 个词,新增 %d");
    }
}
