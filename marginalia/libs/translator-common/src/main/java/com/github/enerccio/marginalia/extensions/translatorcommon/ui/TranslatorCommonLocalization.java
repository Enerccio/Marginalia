package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * The texts of the screens that the translator plugins share (the English text is the key). Each plugin embeds its own copy of this class, so the
 * texts are looked up in the language of the session like those of the plugin itself.
 */
public class TranslatorCommonLocalization extends PluginLocalizationBundle {

    public static final TranslatorCommonLocalization TC = new TranslatorCommonLocalization();

    public static final String ALSO_CALLED = "Also called";
    public static final String EDITED_AFTER_TRANSLATION = "The text was edited after the translation; this may not be what it says now.";
    public static final String EDIT_CHARACTER = "Edit the character";
    public static final String EVERYBODY_SPEAKS_IT_EXCEPT = "Everybody speaks it except the listed characters";
    public static final String GENDER = "Gender";
    public static final String HOW_THE_CHARACTER_SPEAKS_IT = "How the character speaks it: formal, clipped, coarse, learned...";
    public static final String LISTED = "Listed";
    public static final String LISTED_IN_THE_RULE_ABOVE = "Listed: counted by the rule above (an exception, or one of the only speakers)";
    public static final String NAME = "Name";
    public static final String NEW_CHARACTER = "New character";
    public static final String NOT_UNDERSTOOD = "(not understood by the listener)";
    public static final String ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART = "This is only the default: the text of the part overrides it (\"she said in the common tongue\", \"the guard could not understand a word\"). The notes help the translator keep a voice.";
    public static final String ONLY_THE_LISTED_SPEAK_IT = "Only the listed characters speak it";
    public static final String SAID_BY_S = "Said by %s";
    public static final String SEPARATED_BY_COMMAS = "Separated by commas";
    public static final String THE_NAME_IS_EMPTY = "The name is empty";
    public static final String VOICE = "Voice";
    public static final String YES_TEXT = "Yes";
    public static final String UNDERSTANDING = "Understands it";
    public static final String UNDERSTANDING_NOT_SAID = "Not said";
    public static final String UNDERSTANDS_NOTHING = "Understands nothing";
    public static final String KNOWS_A_FEW_WORDS = "Knows a few words";
    public static final String FOLLOWS_MOST_OF_IT = "Follows most of it";
    public static final String FOLLOWS_EVERYTHING = "Follows everything";
    public static final String UNDERSTANDING_HELP = "How much of the language the character follows when it is spoken around them. The story model and the reviewer of the annotation are told (\"not said\" and \"follows everything\" tell them nothing); what to do with it is up to the model, and you can say more in the instructions. A group can be written as a name (\"Other villagers\").";
    public static final String TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT = "Insert who does not follow the language into the prompt of the story (off: only where the template has the macro)";
    public static final String MESSAGES_FROM_THE_END = "Messages from the end where it is inserted";
    public static final String ADD = "Add";
    public static final String CANCEL = "Cancel";
    public static final String DELETE = "Delete";
    public static final String DELETE_S = "Delete %s?";
    public static final String EDIT = "Edit";
    public static final String MOVE_DOWN = "Move down";
    public static final String MOVE_UP = "Move up";
    public static final String OK = "OK";
    public static final String A_RANGE_IS_BEING_TRANSLATED_ALREADY = "A range of this book is being processed already";
    public static final String D_EDITED_SPEECHES_STAY = "%d edited speech(es) stay as they are.";
    public static final String D_NEW_WORDS = "%d new word(s).";
    public static final String D_PARTS_CHANGED_D_SPEECHES = "%d part(s) changed, %d speech(es).";
    public static final String D_PARTS_FAILED_S = "%d part(s) failed: %s";
    public static final String FROM_PART = "From part";
    public static final String NOTHING_TO_DO_IN_THE_RANGE = "Nothing to do in the range";
    public static final String PARTS_IN_THE_RANGE_D_TO_DO_D = "%d part(s) in the range, %d to do";
    public static final String PART_D_OF_D = "Part %d of %d";
    public static final String PUT_THE_ENGLISH_BACK = "Put the English back";
    public static final String PUT_THE_ENGLISH_BACK_INTO_D_PARTS = "Put the English back into %d part(s)? The translation stays in the history of each part, and speeches you edited stay as they are.";
    public static final String START = "Start";
    public static final String STOP = "Stop";
    public static final String STOPPED_AFTER_D_OF_D_PARTS = "Stopped after %d of %d part(s).";
    public static final String STOPPING_AFTER_THIS_PART = "Stopping after this part...";
    public static final String THE_TRANSLATION_FAILED = "The translation failed: %s";
    public static final String TO_PART = "To part";
    public static final String TRANSLATE_PARTS = "Translate parts";
    public static final String TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED = "Translate the parts that are not translated";
    public static final String TRANSLATE_THE_TRANSLATED_PARTS_AGAIN = "Translate the translated parts again";
    public static final String WHAT_DO_YOU_WANT = "What do you want?";
    public static final String CLOSE = "Close";
    public static final String ALL_LANGUAGES = "All languages";
    public static final String ANOTHER_LANGUAGE = "Another language";
    public static final String A_LANGUAGE_THAT_IS_GONE = "(a language that is not in your library any more)";
    public static final String LANGUAGE = "Language";
    public static final String REMOVE = "Remove";
    public static final String THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO = "The book is translated into these languages besides the one it names. Each has its own speakers. A request is for one language: a part with speech in two languages takes two requests, one after the other.";
    public static final String TRANSLATE = "Translate";
    public static final String WHICH_LANGUAGE = "Which language?";
    public static final String EDIT_THE_SOURCE = "Edit the source";
    public static final String PART_D_SPEECH_D = "Part %d, speech %d";
    public static final String SEARCH_AND_REPLACE_IN_THE_SOURCES = "Search and replace in the sources";
    public static final String SPEECH = "Speech";
    public static final String THE_ENGLISH_SOURCE = "The English source";
    public static final String THE_SOURCE_IS_EMPTY = "The source is empty";
    public static final String THE_SOURCE_WAS_SAVED = "The source was saved";
    public static final String THE_TRANSLATION = "The translation";
    public static final String THE_TRANSLATION_COUNTS_AS_UP_TO_DATE = "Saving also takes the translation as it is now for the one this source belongs to, so the models read this source in its place.";
    public static final String TRANSLATED_SPEECH = "Translated speech";

    @Override
    protected void loadKeys() {
        ja(ALSO_CALLED, "別名");
        zh(ALSO_CALLED, "别名");
        fr(ALSO_CALLED, "Aussi appelé");
        es(ALSO_CALLED, "También llamado");
        de(ALSO_CALLED, "Auch genannt");
        ko(ALSO_CALLED, "별칭");

        ja(EDITED_AFTER_TRANSLATION, "翻訳後にテキストが編集されました。現在の内容と一致しない可能性があります。");
        zh(EDITED_AFTER_TRANSLATION, "翻译之后文本被编辑过,这里的原文可能与现在的内容不符。");
        fr(EDITED_AFTER_TRANSLATION, "Le texte a été modifié après la traduction ; elle peut ne plus correspondre à ce qu'il dit maintenant.");
        es(EDITED_AFTER_TRANSLATION, "El texto se editó después de la traducción; puede que ya no diga esto.");
        de(EDITED_AFTER_TRANSLATION, "Der Text wurde nach der Übersetzung bearbeitet; das ist möglicherweise nicht mehr das, was er jetzt sagt.");
        ko(EDITED_AFTER_TRANSLATION, "번역 후 텍스트가 편집되어, 지금은 내용과 다를 수 있습니다.");

        ja(EDIT_CHARACTER, "人物を編集");
        zh(EDIT_CHARACTER, "编辑角色");
        fr(EDIT_CHARACTER, "Modifier le personnage");
        es(EDIT_CHARACTER, "Editar el personaje");
        de(EDIT_CHARACTER, "Die Figur bearbeiten");
        ko(EDIT_CHARACTER, "캐릭터 편집");

        ja(EVERYBODY_SPEAKS_IT_EXCEPT, "一覧の人物以外は全員が話す");
        zh(EVERYBODY_SPEAKS_IT_EXCEPT, "除列出的角色外,所有人都说");
        fr(EVERYBODY_SPEAKS_IT_EXCEPT, "Tout le monde la parle sauf les personnages listés");
        es(EVERYBODY_SPEAKS_IT_EXCEPT, "Todos lo hablan salvo los personajes de la lista");
        de(EVERYBODY_SPEAKS_IT_EXCEPT, "Alle sprechen sie außer den aufgeführten Figuren");
        ko(EVERYBODY_SPEAKS_IT_EXCEPT, "나열된 캐릭터를 제외한 모두가 사용함");

        ja(GENDER, "性別");
        zh(GENDER, "性别");
        fr(GENDER, "Genre");
        es(GENDER, "Género");
        de(GENDER, "Geschlecht");
        ko(GENDER, "성별");

        ja(HOW_THE_CHARACTER_SPEAKS_IT, "その人物の話し方: 丁寧、簡潔、粗野、学のある話し方など");
        zh(HOW_THE_CHARACTER_SPEAKS_IT, "该角色怎么说:正式、简短、粗鲁、有学识……");
        fr(HOW_THE_CHARACTER_SPEAKS_IT, "Comment le personnage la parle : soutenu, sec, grossier, savant...");
        es(HOW_THE_CHARACTER_SPEAKS_IT, "Cómo lo habla el personaje: formal, cortante, grosero, culto...");
        de(HOW_THE_CHARACTER_SPEAKS_IT, "Wie die Figur sie spricht: förmlich, knapp, derb, gebildet ...");
        ko(HOW_THE_CHARACTER_SPEAKS_IT, "캐릭터가 말하는 방식: 격식체, 간결체, 거친 말투, 학식 있는 말투...");

        ja(LISTED, "一覧に含む");
        zh(LISTED, "已列出");
        fr(LISTED, "Listé");
        es(LISTED, "En la lista");
        de(LISTED, "Aufgeführt");
        ko(LISTED, "나열됨");

        ja(LISTED_IN_THE_RULE_ABOVE, "一覧に含める: 上の規則の対象(例外、または唯一の話者の一人)");
        zh(LISTED_IN_THE_RULE_ABOVE, "已列出:适用上面的规则(例外,或唯一的说话者之一)");
        fr(LISTED_IN_THE_RULE_ABOVE, "Listé : compté par la règle ci-dessus (une exception, ou l'un des seuls locuteurs)");
        es(LISTED_IN_THE_RULE_ABOVE, "En la lista: cuenta según la regla de arriba (una excepción, o uno de los únicos hablantes)");
        de(LISTED_IN_THE_RULE_ABOVE, "Aufgeführt: von der obigen Regel erfasst (eine Ausnahme oder einer der einzigen Sprecher)");
        ko(LISTED_IN_THE_RULE_ABOVE, "나열됨: 위 규칙에 따라 집계됨 (예외이거나 유일한 사용자 중 하나)");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");

        ja(NEW_CHARACTER, "新しい人物");
        zh(NEW_CHARACTER, "新建角色");
        fr(NEW_CHARACTER, "Nouveau personnage");
        es(NEW_CHARACTER, "Personaje nuevo");
        de(NEW_CHARACTER, "Neue Figur");
        ko(NEW_CHARACTER, "새 캐릭터");

        ja(NOT_UNDERSTOOD, "(聞き手には理解できない)");
        zh(NOT_UNDERSTOOD, "(听者听不懂)");
        fr(NOT_UNDERSTOOD, "(non compris par l'auditeur)");
        es(NOT_UNDERSTOOD, "(el oyente no lo entiende)");
        de(NOT_UNDERSTOOD, "(vom Zuhörer nicht verstanden)");
        ko(NOT_UNDERSTOOD, "(듣는 사람은 이해하지 못함)");

        ja(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "これは既定にすぎません。パートの本文が優先されます(「彼女は共通語で言った」「衛兵は一言も理解できなかった」など)。メモは翻訳モデルが話し方を保つのに役立ちます。");
        zh(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "这只是默认设置:段落文本优先(“她用通用语说”“守卫一个字也听不懂”)。备注帮助翻译模型保持口吻。");
        fr(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "Ce n'est que la valeur par défaut : le texte de la partie l'emporte (\"dit-elle dans la langue commune\", \"le garde ne comprenait pas un mot\"). Les notes aident le traducteur à garder une voix.");
        es(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "Esto es solo el valor predeterminado: el texto de la parte lo anula (\"dijo ella en la lengua común\", \"el guardia no entendió ni una palabra\"). Las notas ayudan al traductor a mantener una voz.");
        de(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "Dies ist nur der Standard: Der Text des Abschnitts hat Vorrang (\"sie sagte in der Gemeinsprache\", \"der Wächter verstand kein Wort\"). Die Notizen helfen dem Übersetzer, eine Stimme beizubehalten.");
        ko(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART, "이것은 기본값일 뿐이며 파트의 텍스트가 우선합니다 (\"그녀는 공용어로 말했다\", \"경비병은 한 마디도 알아듣지 못했다\"). 메모는 번역기가 목소리를 유지하는 데 도움이 됩니다.");

        ja(ONLY_THE_LISTED_SPEAK_IT, "一覧の人物だけが話す");
        zh(ONLY_THE_LISTED_SPEAK_IT, "只有列出的角色说");
        fr(ONLY_THE_LISTED_SPEAK_IT, "Seuls les personnages listés la parlent");
        es(ONLY_THE_LISTED_SPEAK_IT, "Solo lo hablan los personajes de la lista");
        de(ONLY_THE_LISTED_SPEAK_IT, "Nur die aufgeführten Figuren sprechen sie");
        ko(ONLY_THE_LISTED_SPEAK_IT, "나열된 캐릭터만 사용함");

        ja(SAID_BY_S, "話者:%s");
        zh(SAID_BY_S, "说话者:%s");
        fr(SAID_BY_S, "Dit par %s");
        es(SAID_BY_S, "Dicho por %s");
        de(SAID_BY_S, "Gesagt von %s");
        ko(SAID_BY_S, "%s이(가) 말함");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");
        fr(SEPARATED_BY_COMMAS, "Séparés par des virgules");
        es(SEPARATED_BY_COMMAS, "Separados por comas");
        de(SEPARATED_BY_COMMAS, "Durch Kommas getrennt");
        ko(SEPARATED_BY_COMMAS, "쉼표로 구분");

        ja(THE_NAME_IS_EMPTY, "名前が空です");
        zh(THE_NAME_IS_EMPTY, "名称为空");
        fr(THE_NAME_IS_EMPTY, "Le nom est vide");
        es(THE_NAME_IS_EMPTY, "El nombre está vacío");
        de(THE_NAME_IS_EMPTY, "Der Name ist leer");
        ko(THE_NAME_IS_EMPTY, "이름이 비어 있습니다");

        ja(VOICE, "話し方");
        zh(VOICE, "口吻");
        fr(VOICE, "Voix");
        es(VOICE, "Voz");
        de(VOICE, "Stimme");
        ko(VOICE, "목소리");

        ja(YES_TEXT, "はい");
        zh(YES_TEXT, "是");
        fr(YES_TEXT, "Oui");
        es(YES_TEXT, "Sí");
        de(YES_TEXT, "Ja");
        ko(YES_TEXT, "예");
        ja(UNDERSTANDING, "理解の程度");
        zh(UNDERSTANDING, "理解程度");
        fr(UNDERSTANDING, "La comprend");
        es(UNDERSTANDING, "Lo entiende");
        de(UNDERSTANDING, "Versteht sie");
        ko(UNDERSTANDING, "이해함");
        ja(UNDERSTANDING_NOT_SAID, "未指定");
        zh(UNDERSTANDING_NOT_SAID, "未说明");
        fr(UNDERSTANDING_NOT_SAID, "Non précisé");
        es(UNDERSTANDING_NOT_SAID, "No se dice");
        de(UNDERSTANDING_NOT_SAID, "Nicht angegeben");
        ko(UNDERSTANDING_NOT_SAID, "말하지 않음");
        ja(UNDERSTANDS_NOTHING, "まったく理解しない");
        zh(UNDERSTANDS_NOTHING, "完全听不懂");
        fr(UNDERSTANDS_NOTHING, "Ne comprend rien");
        es(UNDERSTANDS_NOTHING, "No entiende nada");
        de(UNDERSTANDS_NOTHING, "Versteht nichts");
        ko(UNDERSTANDS_NOTHING, "아무것도 이해하지 못함");
        ja(KNOWS_A_FEW_WORDS, "数語だけ知っている");
        zh(KNOWS_A_FEW_WORDS, "只懂几个词");
        fr(KNOWS_A_FEW_WORDS, "Connaît quelques mots");
        es(KNOWS_A_FEW_WORDS, "Conoce unas pocas palabras");
        de(KNOWS_A_FEW_WORDS, "Kennt ein paar Wörter");
        ko(KNOWS_A_FEW_WORDS, "몇 단어를 앎");
        ja(FOLLOWS_MOST_OF_IT, "ほとんど理解できる");
        zh(FOLLOWS_MOST_OF_IT, "大部分能听懂");
        fr(FOLLOWS_MOST_OF_IT, "En suit l'essentiel");
        es(FOLLOWS_MOST_OF_IT, "Entiende la mayor parte");
        de(FOLLOWS_MOST_OF_IT, "Versteht das meiste");
        ko(FOLLOWS_MOST_OF_IT, "대부분 따라감");
        ja(FOLLOWS_EVERYTHING, "すべて理解できる");
        zh(FOLLOWS_EVERYTHING, "完全听得懂");
        fr(FOLLOWS_EVERYTHING, "Suit tout");
        es(FOLLOWS_EVERYTHING, "Lo entiende todo");
        de(FOLLOWS_EVERYTHING, "Versteht alles");
        ko(FOLLOWS_EVERYTHING, "모두 따라감");
        ja(UNDERSTANDING_HELP, "その言語が周囲で話されたとき、人物がどの程度理解できるか。物語のモデルと注釈のレビュー担当モデルに伝えられます(「未指定」と「すべて理解できる」は何も伝えません)。どう扱うかはモデル次第で、指示でさらに補足できます。グループも名前として書けます(「その他の村人」)。");
        zh(UNDERSTANDING_HELP, "该语言在角色周围被说出时,角色能听懂多少。会告诉故事模型和批注审阅模型(“未说明”和“完全听得懂”不告诉它们任何内容);怎么处理由模型自行决定,你也可以在指令中补充。也可以把一个群体写成名字(“其他村民”)。");
        fr(UNDERSTANDING_HELP, "Quelle part de la langue le personnage suit quand on la parle autour de lui. Le modèle d'histoire et le relecteur de l'annotation en sont informés (« non précisé » et « suit tout » ne leur disent rien) ; ce qu'il en fait dépend du modèle, et vous pouvez en dire plus dans les instructions. Un groupe peut s'écrire comme un nom (« Autres villageois »).");
        es(UNDERSTANDING_HELP, "Cuánto entiende el personaje del idioma cuando se habla a su alrededor. Se informa de ello al modelo de la historia y al revisor de la anotación (\"no se dice\" y \"lo entiende todo\" no les dicen nada); qué hacer con ello depende del modelo, y puedes decir más en las instrucciones. Un grupo se puede escribir como un nombre (\"Los demás aldeanos\").");
        de(UNDERSTANDING_HELP, "Wie viel von der Sprache die Figur versteht, wenn sie in ihrer Umgebung gesprochen wird. Dem Geschichtsmodell und dem Prüfer der Anmerkung wird es mitgeteilt („Nicht angegeben“ und „Versteht alles“ sagen ihnen nichts); was damit geschieht, liegt beim Modell, und in den Anweisungen kannst du mehr sagen. Eine Gruppe kann als Name geschrieben werden (\"Andere Dorfbewohner\").");
        ko(UNDERSTANDING_HELP, "캐릭터가 주변에서 말하는 언어를 얼마나 따라가는지. 이야기 모델과 주석 검토자에게 알려 줍니다 (\"말하지 않음\"과 \"모두 따라감\"은 아무것도 알려 주지 않습니다). 이를 어떻게 다룰지는 모델에 달려 있으며, 지시문에서 더 설명할 수 있습니다. 그룹은 이름(\"다른 마을 사람들\")으로 쓸 수 있습니다.");
        ja(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "言語を理解しない人物を物語のプロンプトに挿入する(オフ: テンプレートにマクロがある場合のみ)");
        zh(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "把听不懂该语言的人物插入故事提示词(关闭:仅在模板中有宏的地方)");
        fr(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "Insérer dans le prompt de l'histoire qui ne suit pas la langue (désactivé : seulement là où le modèle a la macro)");
        es(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "Insertar en el prompt de la historia quién no entiende el idioma (desactivado: solo donde la plantilla tiene la macro)");
        de(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "Wer die Sprache nicht versteht, in den Prompt der Geschichte einfügen (aus: nur dort, wo die Vorlage das Makro hat)");
        ko(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT, "언어를 따라가지 못하는 사람을 이야기의 프롬프트에 삽입 (끄면: 템플릿에 매크로가 있는 곳에서만)");
        ja(MESSAGES_FROM_THE_END, "末尾から数えて挿入するメッセージ位置");
        zh(MESSAGES_FROM_THE_END, "从末尾数起的插入位置(消息数)");
        fr(MESSAGES_FROM_THE_END, "Messages depuis la fin où il est inséré");
        es(MESSAGES_FROM_THE_END, "Mensajes desde el final en los que se inserta");
        de(MESSAGES_FROM_THE_END, "Nachrichten vom Ende, wo es eingefügt wird");
        ko(MESSAGES_FROM_THE_END, "끝에서부터 삽입되는 위치의 메시지 수");
        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajouter");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

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

        ja(EDIT, "編集");
        zh(EDIT, "编辑");
        fr(EDIT, "Modifier");
        es(EDIT, "Editar");
        de(EDIT, "Bearbeiten");
        ko(EDIT, "편집");

        ja(MOVE_DOWN, "下へ移動");
        zh(MOVE_DOWN, "下移");
        fr(MOVE_DOWN, "Descendre");
        es(MOVE_DOWN, "Bajar");
        de(MOVE_DOWN, "Nach unten");
        ko(MOVE_DOWN, "아래로 이동");

        ja(MOVE_UP, "上へ移動");
        zh(MOVE_UP, "上移");
        fr(MOVE_UP, "Monter");
        es(MOVE_UP, "Subir");
        de(MOVE_UP, "Nach oben");
        ko(MOVE_UP, "위로 이동");

        ja(OK, "OK");
        zh(OK, "确定");
        fr(OK, "OK");
        es(OK, "Aceptar");
        de(OK, "OK");
        ko(OK, "확인");

        ja(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "この本の範囲を処理中です");
        zh(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "本书已有一个范围正在处理");
        fr(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "Une plage de ce livre est déjà en cours de traitement");
        es(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "Ya se está procesando un intervalo de este libro");
        de(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "Ein Bereich dieses Buchs wird bereits verarbeitet");
        ko(A_RANGE_IS_BEING_TRANSLATED_ALREADY, "이 책의 범위가 이미 처리 중입니다");

        ja(D_EDITED_SPEECHES_STAY, "編集された台詞%d件はそのままです。");
        zh(D_EDITED_SPEECHES_STAY, "%d 句编辑过的对白保持不变。");
        fr(D_EDITED_SPEECHES_STAY, "%d réplique(s) modifiée(s) restent telles quelles.");
        es(D_EDITED_SPEECHES_STAY, "%d diálogo(s) editado(s) se quedan como están.");
        de(D_EDITED_SPEECHES_STAY, "%d bearbeitete Rede(n) bleiben, wie sie sind.");
        ko(D_EDITED_SPEECHES_STAY, "편집된 대사 %d개는 그대로 유지됩니다.");

        ja(D_NEW_WORDS, "新語%d個。");
        zh(D_NEW_WORDS, "%d 个新词。");
        fr(D_NEW_WORDS, "%d nouveau(x) mot(s).");
        es(D_NEW_WORDS, "%d palabra(s) nueva(s).");
        de(D_NEW_WORDS, "%d neue(s) Wort/Wörter.");
        ko(D_NEW_WORDS, "새 단어 %d개.");

        ja(D_PARTS_CHANGED_D_SPEECHES, "%d個のパートを変更、台詞%d件。");
        zh(D_PARTS_CHANGED_D_SPEECHES, "已修改 %d 段,%d 句对白。");
        fr(D_PARTS_CHANGED_D_SPEECHES, "%d partie(s) modifiée(s), %d réplique(s).");
        es(D_PARTS_CHANGED_D_SPEECHES, "%d parte(s) cambiada(s), %d diálogo(s).");
        de(D_PARTS_CHANGED_D_SPEECHES, "%d Abschnitt(e) geändert, %d Rede(n).");
        ko(D_PARTS_CHANGED_D_SPEECHES, "파트 %d개, 대사 %d개가 변경되었습니다.");

        ja(D_PARTS_FAILED_S, "%d個のパートが失敗しました:%s");
        zh(D_PARTS_FAILED_S, "%d 段失败:%s");
        fr(D_PARTS_FAILED_S, "%d partie(s) en échec : %s");
        es(D_PARTS_FAILED_S, "%d parte(s) fallida(s): %s");
        de(D_PARTS_FAILED_S, "%d Abschnitt(e) fehlgeschlagen: %s");
        ko(D_PARTS_FAILED_S, "파트 %d개가 실패했습니다: %s");

        ja(FROM_PART, "開始パート");
        zh(FROM_PART, "起始段落");
        fr(FROM_PART, "Depuis la partie");
        es(FROM_PART, "Desde la parte");
        de(FROM_PART, "Ab Abschnitt");
        ko(FROM_PART, "시작 파트");

        ja(NOTHING_TO_DO_IN_THE_RANGE, "範囲内に対象がありません");
        zh(NOTHING_TO_DO_IN_THE_RANGE, "范围内没有需要处理的段落");
        fr(NOTHING_TO_DO_IN_THE_RANGE, "Rien à faire dans la plage");
        es(NOTHING_TO_DO_IN_THE_RANGE, "No hay nada que hacer en el intervalo");
        de(NOTHING_TO_DO_IN_THE_RANGE, "Im Bereich ist nichts zu tun");
        ko(NOTHING_TO_DO_IN_THE_RANGE, "범위에서 할 일이 없습니다");

        ja(PARTS_IN_THE_RANGE_D_TO_DO_D, "範囲内のパート:%d、対象:%d");
        zh(PARTS_IN_THE_RANGE_D_TO_DO_D, "范围内 %d 段,待处理 %d 段");
        fr(PARTS_IN_THE_RANGE_D_TO_DO_D, "%d partie(s) dans la plage, %d à faire");
        es(PARTS_IN_THE_RANGE_D_TO_DO_D, "%d parte(s) en el intervalo, %d por hacer");
        de(PARTS_IN_THE_RANGE_D_TO_DO_D, "%d Abschnitt(e) im Bereich, %d zu erledigen");
        ko(PARTS_IN_THE_RANGE_D_TO_DO_D, "범위에 파트 %d개, 그중 %d개가 대상");

        ja(PART_D_OF_D, "パート %d / %d");
        zh(PART_D_OF_D, "第 %d / %d 段");
        fr(PART_D_OF_D, "Partie %d sur %d");
        es(PART_D_OF_D, "Parte %d de %d");
        de(PART_D_OF_D, "Abschnitt %d von %d");
        ko(PART_D_OF_D, "파트 %d / %d");

        ja(PUT_THE_ENGLISH_BACK, "英語に戻す");
        zh(PUT_THE_ENGLISH_BACK, "恢复为英文");
        fr(PUT_THE_ENGLISH_BACK, "Remettre l'anglais");
        es(PUT_THE_ENGLISH_BACK, "Restaurar el inglés");
        de(PUT_THE_ENGLISH_BACK, "Das Englische zurücksetzen");
        ko(PUT_THE_ENGLISH_BACK, "영어로 되돌리기");

        ja(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "%d個のパートを英語に戻しますか?翻訳は各パートの履歴に残り、編集した台詞はそのままです。");
        zh(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "要将 %d 段恢复为英文吗?译文保留在每段的历史中,你编辑过的对白保持不变。");
        fr(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "Remettre l'anglais dans %d partie(s) ? La traduction reste dans l'historique de chaque partie, et les répliques que vous avez modifiées restent telles quelles.");
        es(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "¿Restaurar el inglés en %d parte(s)? La traducción se queda en el historial de cada parte, y los diálogos que editaste se quedan como están.");
        de(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "Das Englische in %d Abschnitt(e) zurücksetzen? Die Übersetzung bleibt im Verlauf jedes Abschnitts, und von dir bearbeitete Reden bleiben, wie sie sind.");
        ko(PUT_THE_ENGLISH_BACK_INTO_D_PARTS, "파트 %d개를 영어로 되돌리시겠습니까? 번역은 각 파트의 기록에 남으며, 편집한 대사는 그대로 유지됩니다.");

        ja(START, "開始");
        zh(START, "开始");
        fr(START, "Démarrer");
        es(START, "Iniciar");
        de(START, "Starten");
        ko(START, "시작");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");
        ko(STOP, "중지");

        ja(STOPPED_AFTER_D_OF_D_PARTS, "%d / %d個のパートの後に停止しました。");
        zh(STOPPED_AFTER_D_OF_D_PARTS, "在 %d / %d 段后停止。");
        fr(STOPPED_AFTER_D_OF_D_PARTS, "Arrêté après %d partie(s) sur %d.");
        es(STOPPED_AFTER_D_OF_D_PARTS, "Detenido tras %d de %d parte(s).");
        de(STOPPED_AFTER_D_OF_D_PARTS, "Gestoppt nach %d von %d Abschnitt(en).");
        ko(STOPPED_AFTER_D_OF_D_PARTS, "파트 %d / %d개 후에 중지되었습니다.");

        ja(STOPPING_AFTER_THIS_PART, "このパートの後に停止します...");
        zh(STOPPING_AFTER_THIS_PART, "处理完这一段后停止...");
        fr(STOPPING_AFTER_THIS_PART, "Arrêt après cette partie...");
        es(STOPPING_AFTER_THIS_PART, "Deteniéndose tras esta parte...");
        de(STOPPING_AFTER_THIS_PART, "Stoppt nach diesem Abschnitt ...");
        ko(STOPPING_AFTER_THIS_PART, "이 파트 후에 중지하는 중...");

        ja(THE_TRANSLATION_FAILED, "翻訳に失敗しました: %s");
        zh(THE_TRANSLATION_FAILED, "翻译失败:%s");
        fr(THE_TRANSLATION_FAILED, "La traduction a échoué : %s");
        es(THE_TRANSLATION_FAILED, "La traducción ha fallado: %s");
        de(THE_TRANSLATION_FAILED, "Die Übersetzung ist fehlgeschlagen: %s");
        ko(THE_TRANSLATION_FAILED, "번역에 실패했습니다: %s");

        ja(TO_PART, "終了パート");
        zh(TO_PART, "结束段落");
        fr(TO_PART, "Jusqu'à la partie");
        es(TO_PART, "Hasta la parte");
        de(TO_PART, "Bis Abschnitt");
        ko(TO_PART, "끝 파트");

        ja(TRANSLATE_PARTS, "パートを翻訳");
        zh(TRANSLATE_PARTS, "翻译段落");
        fr(TRANSLATE_PARTS, "Traduire des parties");
        es(TRANSLATE_PARTS, "Traducir partes");
        de(TRANSLATE_PARTS, "Abschnitte übersetzen");
        ko(TRANSLATE_PARTS, "파트 번역");

        ja(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "未翻訳のパートを翻訳");
        zh(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "翻译尚未翻译的段落");
        fr(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "Traduire les parties qui ne sont pas traduites");
        es(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "Traducir las partes que no están traducidas");
        de(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "Die Abschnitte übersetzen, die nicht übersetzt sind");
        ko(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED, "번역되지 않은 파트 번역");

        ja(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "翻訳済みのパートを再翻訳");
        zh(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "重新翻译已翻译的段落");
        fr(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "Retraduire les parties traduites");
        es(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "Volver a traducir las partes traducidas");
        de(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "Die übersetzten Abschnitte erneut übersetzen");
        ko(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN, "번역된 파트 다시 번역");

        ja(WHAT_DO_YOU_WANT, "何をしたいですか?");
        zh(WHAT_DO_YOU_WANT, "你想要什么?");
        fr(WHAT_DO_YOU_WANT, "Que voulez-vous faire ?");
        es(WHAT_DO_YOU_WANT, "¿Qué quieres hacer?");
        de(WHAT_DO_YOU_WANT, "Was möchtest du?");
        ko(WHAT_DO_YOU_WANT, "무엇을 하시겠습니까?");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");

        ja(ALL_LANGUAGES, "すべての言語");
        zh(ALL_LANGUAGES, "所有语言");
        fr(ALL_LANGUAGES, "Toutes les langues");
        es(ALL_LANGUAGES, "Todos los idiomas");
        de(ALL_LANGUAGES, "Alle Sprachen");
        ko(ALL_LANGUAGES, "모든 언어");

        ja(ANOTHER_LANGUAGE, "別の言語");
        zh(ANOTHER_LANGUAGE, "另一种语言");
        fr(ANOTHER_LANGUAGE, "Une autre langue");
        es(ANOTHER_LANGUAGE, "Otro idioma");
        de(ANOTHER_LANGUAGE, "Eine andere Sprache");
        ko(ANOTHER_LANGUAGE, "다른 언어");

        ja(A_LANGUAGE_THAT_IS_GONE, "(ライブラリにない言語)");
        zh(A_LANGUAGE_THAT_IS_GONE, "(已不在你的语言库中)");
        fr(A_LANGUAGE_THAT_IS_GONE, "(une langue qui n'est plus dans votre bibliothèque)");
        es(A_LANGUAGE_THAT_IS_GONE, "(un idioma que ya no está en tu biblioteca)");
        de(A_LANGUAGE_THAT_IS_GONE, "(eine Sprache, die nicht mehr in deiner Bibliothek ist)");
        ko(A_LANGUAGE_THAT_IS_GONE, "(라이브러리에 더 이상 없는 언어)");

        ja(LANGUAGE, "言語");
        zh(LANGUAGE, "语言");
        fr(LANGUAGE, "Langue");
        es(LANGUAGE, "Idioma");
        de(LANGUAGE, "Sprache");
        ko(LANGUAGE, "언어");

        ja(REMOVE, "削除");
        zh(REMOVE, "移除");
        fr(REMOVE, "Retirer");
        es(REMOVE, "Quitar");
        de(REMOVE, "Entfernen");
        ko(REMOVE, "제거");

        ja(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "この本は、最初に指定した言語のほかに、これらの言語にも翻訳されます。各言語にはそれぞれの話者がいます。リクエストは1言語ごとです。2つの言語の台詞があるパートは、順に2回リクエストします。");
        zh(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "除书中指定的第一种语言外,本书还会翻译成这些语言。每种语言有各自的说话者。一次请求针对一种语言:同时含有两种语言对白的段落需要依次请求两次。");
        fr(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "Le livre est traduit dans ces langues en plus de celle qu'il nomme. Chacune a ses propres locuteurs. Une requête porte sur une langue : une partie avec des répliques en deux langues demande deux requêtes, l'une après l'autre.");
        es(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "El libro se traduce a estos idiomas además del que nombra. Cada uno tiene sus propios hablantes. Una solicitud es para un idioma: una parte con diálogo en dos idiomas necesita dos solicitudes, una tras otra.");
        de(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "Das Buch wird außer in die Sprache, die es nennt, in diese Sprachen übersetzt. Jede hat ihre eigenen Sprecher. Eine Anfrage gilt für eine Sprache: Ein Abschnitt mit Rede in zwei Sprachen braucht zwei Anfragen, eine nach der anderen.");
        ko(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO, "책은 책에 명시된 언어 외에 이 언어들로도 번역됩니다. 각 언어에는 고유한 화자가 있습니다. 요청은 한 언어에 대한 것입니다: 두 언어의 대사가 있는 파트는 요청 두 번을 차례로 보냅니다.");

        ja(TRANSLATE, "翻訳");
        zh(TRANSLATE, "翻译");
        fr(TRANSLATE, "Traduire");
        es(TRANSLATE, "Traducir");
        de(TRANSLATE, "Übersetzen");
        ko(TRANSLATE, "번역");

        ja(WHICH_LANGUAGE, "どの言語ですか?");
        zh(WHICH_LANGUAGE, "哪种语言?");
        fr(WHICH_LANGUAGE, "Quelle langue ?");
        es(WHICH_LANGUAGE, "¿Qué idioma?");
        de(WHICH_LANGUAGE, "Welche Sprache?");
        ko(WHICH_LANGUAGE, "어떤 언어로?");

        ja(EDIT_THE_SOURCE, "原文を編集");
        zh(EDIT_THE_SOURCE, "编辑原文");
        fr(EDIT_THE_SOURCE, "Modifier la source");
        es(EDIT_THE_SOURCE, "Editar el original");
        de(EDIT_THE_SOURCE, "Die Quelle bearbeiten");
        ko(EDIT_THE_SOURCE, "원문 편집");

        ja(PART_D_SPEECH_D, "パート%d、台詞%d");
        zh(PART_D_SPEECH_D, "第%d段,第%d句对白");
        fr(PART_D_SPEECH_D, "Partie %d, réplique %d");
        es(PART_D_SPEECH_D, "Parte %d, diálogo %d");
        de(PART_D_SPEECH_D, "Abschnitt %d, Rede %d");
        ko(PART_D_SPEECH_D, "파트 %d, 대사 %d");

        ja(SEARCH_AND_REPLACE_IN_THE_SOURCES, "原文の検索と置換");
        zh(SEARCH_AND_REPLACE_IN_THE_SOURCES, "在原文中查找和替换");
        fr(SEARCH_AND_REPLACE_IN_THE_SOURCES, "Rechercher et remplacer dans les sources");
        es(SEARCH_AND_REPLACE_IN_THE_SOURCES, "Buscar y reemplazar en los originales");
        de(SEARCH_AND_REPLACE_IN_THE_SOURCES, "In den Quellen suchen und ersetzen");
        ko(SEARCH_AND_REPLACE_IN_THE_SOURCES, "원문에서 찾기 및 바꾸기");

        ja(SPEECH, "台詞");
        zh(SPEECH, "对白");
        fr(SPEECH, "Réplique");
        es(SPEECH, "Diálogo");
        de(SPEECH, "Rede");
        ko(SPEECH, "대사");

        ja(THE_ENGLISH_SOURCE, "英語の原文");
        zh(THE_ENGLISH_SOURCE, "英文原文");
        fr(THE_ENGLISH_SOURCE, "La source anglaise");
        es(THE_ENGLISH_SOURCE, "El original en inglés");
        de(THE_ENGLISH_SOURCE, "Die englische Quelle");
        ko(THE_ENGLISH_SOURCE, "영어 원문");

        ja(THE_SOURCE_IS_EMPTY, "原文が空です");
        zh(THE_SOURCE_IS_EMPTY, "原文为空");
        fr(THE_SOURCE_IS_EMPTY, "La source est vide");
        es(THE_SOURCE_IS_EMPTY, "El original está vacío");
        de(THE_SOURCE_IS_EMPTY, "Die Quelle ist leer");
        ko(THE_SOURCE_IS_EMPTY, "원문이 비어 있습니다");

        ja(THE_SOURCE_WAS_SAVED, "原文を保存しました");
        zh(THE_SOURCE_WAS_SAVED, "原文已保存");
        fr(THE_SOURCE_WAS_SAVED, "La source a été enregistrée");
        es(THE_SOURCE_WAS_SAVED, "El original se ha guardado");
        de(THE_SOURCE_WAS_SAVED, "Die Quelle wurde gespeichert");
        ko(THE_SOURCE_WAS_SAVED, "원문을 저장했습니다");

        ja(THE_TRANSLATION, "翻訳");
        zh(THE_TRANSLATION, "译文");
        fr(THE_TRANSLATION, "La traduction");
        es(THE_TRANSLATION, "La traducción");
        de(THE_TRANSLATION, "Die Übersetzung");
        ko(THE_TRANSLATION, "번역");

        ja(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "保存すると、現在の翻訳がこの原文に対応するものとして扱われ、モデルは翻訳の代わりにこの原文を読みます。");
        zh(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "保存后,当前译文会被视为与这段原文对应,模型将读取这段原文来代替译文。");
        fr(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "L'enregistrement prend aussi la traduction actuelle pour celle à laquelle appartient cette source, les modèles lisent donc cette source à sa place.");
        es(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "Al guardar, también se toma la traducción tal como está ahora para aquella a la que pertenece este original, de modo que los modelos lean este original en su lugar.");
        de(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "Beim Speichern wird auch die Übersetzung in ihrem jetzigen Zustand für die übernommen, zu der diese Quelle gehört, sodass die Modelle diese Quelle an ihrer Stelle lesen.");
        ko(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE, "저장하면 이 원문이 속한 대사의 현재 번역도 그대로 가져가므로, 모델은 그 대신 이 원문을 읽습니다.");

        ja(TRANSLATED_SPEECH, "翻訳された台詞");
        zh(TRANSLATED_SPEECH, "已翻译的对白");
        fr(TRANSLATED_SPEECH, "Réplique traduite");
        es(TRANSLATED_SPEECH, "Diálogo traducido");
        de(TRANSLATED_SPEECH, "Übersetzte Rede");
        ko(TRANSLATED_SPEECH, "번역된 대사");
    }
}
