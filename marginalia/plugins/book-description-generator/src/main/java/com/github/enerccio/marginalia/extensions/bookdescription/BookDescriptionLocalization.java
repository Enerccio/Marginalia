package com.github.enerccio.marginalia.extensions.bookdescription;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.bookdescription.service.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Book Description Generator in the languages besides English; the English text is the key.
 */
public class BookDescriptionLocalization extends PluginLocalizationBundle {

    public static final BookDescriptionLocalization T = new BookDescriptionLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final Defaults englishDefaults = new Defaults();
    private final Map<Language, Defaults> translatedDefaults = Map.of(JA, new DefaultsJA(), ZH, new DefaultsZH(), FR, new DefaultsFR(), ES, new DefaultsES(), DE, new DefaultsDE(),
            IT, new DefaultsIT(), KO, new DefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public Defaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

    public static final String BOOK_DESCRIPTION_GENERATOR = "Book Description Generator";
    public static final String GENERATE_NAME_AND_DESCRIPTION = "Generate name and description";
    public static final String BOOK_DESCRIPTION_PROMPT = "Description prompt";
    public static final String BOOK_NOTES_PROMPT = "Notes prompt";
    public static final String THE_PROMPT_THAT_WRITES_THE_NAME = "Writes the name and the description of the book from the story. Variables: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}";
    public static final String THE_PROMPT_THAT_WRITES_NOTES = "Writes notes on one part of a story that is too long for the model; the notes are what the description prompt gets. Variables: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}";
    public static final String VARIABLES = "Variables: %s";
    public static final String FROM_MESSAGE = "From message";
    public static final String TO_MESSAGE = "To message";
    public static final String NO_SUCH_MESSAGE = "No such message";
    public static final String THE_MESSAGES_WITH_TEXT = "The book has %d messages with text";
    public static final String THE_MESSAGES_ARE_NUMBERED_1 = "The messages are numbered from 1 to %d";
    public static final String THE_FIRST_MESSAGE_IS_AFTER = "The first message is after the last one";
    public static final String USE_SUMMARIES = "Use summaries";
    public static final String A_SUMMARY_STANDS_IN = "A summary stands in for the messages it covers when all of them are in the range; a summary that does not describe the story any more is not used";
    public static final String ANSWER_FORMAT = "Answer format";
    public static final String AUTOMATIC = "Automatic";
    public static final String JSON = "JSON";
    public static final String JSON_WITHOUT_A_SCHEMA = "JSON without a schema";
    public static final String MARKDOWN = "Markdown";
    public static final String AUTOMATIC_ASKS_FOR_JSON = "Automatic asks for JSON in a schema when the model can follow one, Markdown otherwise";
    public static final String JSON_WITHOUT_A_SCHEMA_HELP = "JSON without a schema is for a server that can not take a schema together with the thinking of the model";
    public static final String THE_PROMPTS_ARE_SET_IN = "The prompts are inherited: set them in the Prompts tab of the book or of its folder, or in Settings → Extension Settings → Book Description Generator";
    public static final String THE_RANGE_HAS = "%d messages in the range, %d parts are sent (%d of them summaries)";
    public static final String TOKENS_SENT_AS_IT_IS = "About %s tokens with the prompt, the model takes %s: the story is sent as it is.";
    public static final String TOKENS_SENT_AS_NOTES = "About %s tokens with the prompt, the model takes %s: the story is too long, notes are made of it first (about %d requests).";
    public static final String TOKENS_CONTEXT_UNKNOWN = "About %s tokens with the prompt. The context size of the model is not known, the story is sent as it is.";
    public static final String THE_PROMPT_CANT_BE_BUILT = "The prompt can't be built: %s";
    public static final String GENERATE = "Generate";
    public static final String STOP = "Stop";
    public static final String CLOSE = "Close";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String THE_BOOK_HAS_NO_STORY = "The book has no story to describe";
    public static final String CANT_GENERATE = "Can't generate: %s";
    public static final String NOTES_ROUND = "Notes, round %d: the story is divided into %d parts";
    public static final String NOTES_PART_DONE = "Notes: part %d of %d done";
    public static final String WRITING_THE_DESCRIPTION = "Writing the name and the description...";
    public static final String WRITING_THE_DESCRIPTION_FROM_NOTES = "Writing the name and the description from the notes...";
    public static final String DONE = "Done";
    public static final String STOPPED = "Stopped";
    public static final String NOTES = "Notes";
    public static final String NOTES_OF_A_PART = "Part %d of %d";
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String COPY_TO_BOOK = "Copy to book";
    public static final String COPIED_TO_THE_BOOK = "Copied to the book";
    public static final String CANT_COPY_TO_THE_BOOK = "Can't copy to the book: %s";
    public static final String THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE = "The name and the description the model writes appear here";
    public static final String THE_ANSWER_CANT_BE_READ = "The answer has no name and description that could be read. This is what the model wrote:";
    public static final String BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION = "Book Description Generator: write the description";

    @Override
    protected void loadKeys() {
        ja(BOOK_DESCRIPTION_GENERATOR, "書籍説明ジェネレーター");
        zh(BOOK_DESCRIPTION_GENERATOR, "书籍简介生成器");
        fr(BOOK_DESCRIPTION_GENERATOR, "Générateur de description de livre");
        es(BOOK_DESCRIPTION_GENERATOR, "Generador de descripciones de libros");
        de(BOOK_DESCRIPTION_GENERATOR, "Buchbeschreibungs-Generator");
        ko(BOOK_DESCRIPTION_GENERATOR, "책 설명 생성기");
        it(BOOK_DESCRIPTION_GENERATOR, "Generatore di descrizioni del libro");

        ja(GENERATE_NAME_AND_DESCRIPTION, "名前と説明を生成");
        zh(GENERATE_NAME_AND_DESCRIPTION, "生成名称和简介");
        fr(GENERATE_NAME_AND_DESCRIPTION, "Générer le nom et la description");
        es(GENERATE_NAME_AND_DESCRIPTION, "Generar nombre y descripción");
        de(GENERATE_NAME_AND_DESCRIPTION, "Namen und Beschreibung generieren");
        ko(GENERATE_NAME_AND_DESCRIPTION, "이름과 설명 생성");
        it(GENERATE_NAME_AND_DESCRIPTION, "Genera nome e descrizione");

        ja(BOOK_DESCRIPTION_PROMPT, "説明プロンプト");
        zh(BOOK_DESCRIPTION_PROMPT, "简介提示词");
        fr(BOOK_DESCRIPTION_PROMPT, "Prompt de description");
        es(BOOK_DESCRIPTION_PROMPT, "Prompt de la descripción");
        de(BOOK_DESCRIPTION_PROMPT, "Beschreibungsprompt");
        ko(BOOK_DESCRIPTION_PROMPT, "설명 프롬프트");
        it(BOOK_DESCRIPTION_PROMPT, "Prompt della descrizione");

        ja(BOOK_NOTES_PROMPT, "ノートプロンプト");
        zh(BOOK_NOTES_PROMPT, "笔记提示词");
        fr(BOOK_NOTES_PROMPT, "Prompt de notes");
        es(BOOK_NOTES_PROMPT, "Prompt de las notas");
        de(BOOK_NOTES_PROMPT, "Notizprompt");
        ko(BOOK_NOTES_PROMPT, "메모 프롬프트");
        it(BOOK_NOTES_PROMPT, "Prompt delle note");

        ja(THE_PROMPT_THAT_WRITES_THE_NAME, "物語から本の名前と説明を書きます。変数: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        zh(THE_PROMPT_THAT_WRITES_THE_NAME, "根据故事撰写书名和简介。变量：{{story}}、{{fromNotes}}、{{bookName}}、{{bookDescription}}");
        fr(THE_PROMPT_THAT_WRITES_THE_NAME, "Écrit le nom et la description du livre à partir de l'histoire. Variables : {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        es(THE_PROMPT_THAT_WRITES_THE_NAME, "Escribe el nombre y la descripción del libro a partir de la historia. Variables: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        de(THE_PROMPT_THAT_WRITES_THE_NAME, "Schreibt den Namen und die Beschreibung des Buchs aus der Geschichte. Variablen: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        ko(THE_PROMPT_THAT_WRITES_THE_NAME, "이야기를 바탕으로 책의 이름과 설명을 씁니다. 변수: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");
        it(THE_PROMPT_THAT_WRITES_THE_NAME, "Scrive il nome e la descrizione del libro a partire dalla storia. Variabili: {{story}}, {{fromNotes}}, {{bookName}}, {{bookDescription}}");

        ja(THE_PROMPT_THAT_WRITES_NOTES, "モデルに長すぎる物語の一部についてノートを書きます。このノートが説明プロンプトに渡されます。変数: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        zh(THE_PROMPT_THAT_WRITES_NOTES, "为对模型来说过长的故事的某一部分撰写笔记；笔记会交给简介提示词。变量：{{story}}、{{part}}、{{parts}}、{{bookName}}、{{bookDescription}}");
        fr(THE_PROMPT_THAT_WRITES_NOTES, "Écrit des notes sur une partie d'une histoire trop longue pour le modèle ; ces notes sont ce que reçoit le prompt de description. Variables : {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        es(THE_PROMPT_THAT_WRITES_NOTES, "Escribe notas sobre una parte de una historia que es demasiado larga para el modelo; las notas son lo que recibe el prompt de la descripción. Variables: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        de(THE_PROMPT_THAT_WRITES_NOTES, "Schreibt Notizen zu einem Teil einer Geschichte, die für das Modell zu lang ist; die Notizen erhält der Beschreibungsprompt. Variablen: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        ko(THE_PROMPT_THAT_WRITES_NOTES, "모델이 한 번에 읽기에 너무 긴 이야기의 한 부분에 대한 메모를 씁니다. 메모는 설명 프롬프트가 받는 내용입니다. 변수: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");
        it(THE_PROMPT_THAT_WRITES_NOTES, "Scrive note su una parte di una storia troppo lunga per il modello; le note sono ciò che riceve il prompt della descrizione. Variabili: {{story}}, {{part}}, {{parts}}, {{bookName}}, {{bookDescription}}");

        ja(VARIABLES, "変数: %s");
        zh(VARIABLES, "变量：%s");
        fr(VARIABLES, "Variables : %s");
        es(VARIABLES, "Variables: %s");
        de(VARIABLES, "Variablen: %s");
        ko(VARIABLES, "변수: %s");
        it(VARIABLES, "Variabili: %s");

        ja(FROM_MESSAGE, "開始メッセージ");
        zh(FROM_MESSAGE, "起始消息");
        fr(FROM_MESSAGE, "Depuis le message");
        es(FROM_MESSAGE, "Desde el mensaje");
        de(FROM_MESSAGE, "Ab Nachricht");
        ko(FROM_MESSAGE, "시작 메시지");
        it(FROM_MESSAGE, "Dal messaggio");

        ja(TO_MESSAGE, "終了メッセージ");
        zh(TO_MESSAGE, "结束消息");
        fr(TO_MESSAGE, "Jusqu'au message");
        es(TO_MESSAGE, "Hasta el mensaje");
        de(TO_MESSAGE, "Bis Nachricht");
        ko(TO_MESSAGE, "끝 메시지");
        it(TO_MESSAGE, "Al messaggio");

        ja(NO_SUCH_MESSAGE, "そのメッセージはありません");
        zh(NO_SUCH_MESSAGE, "没有这条消息");
        fr(NO_SUCH_MESSAGE, "Ce message n'existe pas");
        es(NO_SUCH_MESSAGE, "No existe ese mensaje");
        de(NO_SUCH_MESSAGE, "Keine solche Nachricht");
        ko(NO_SUCH_MESSAGE, "해당 메시지가 없습니다");
        it(NO_SUCH_MESSAGE, "Nessun messaggio simile");

        ja(THE_MESSAGES_WITH_TEXT, "本にはテキストのあるメッセージが %d 件あります");
        zh(THE_MESSAGES_WITH_TEXT, "本书有 %d 条含文本的消息");
        fr(THE_MESSAGES_WITH_TEXT, "Le livre a %d messages avec du texte");
        es(THE_MESSAGES_WITH_TEXT, "El libro tiene %d mensajes con texto");
        de(THE_MESSAGES_WITH_TEXT, "Das Buch hat %d Nachrichten mit Text");
        ko(THE_MESSAGES_WITH_TEXT, "책에 텍스트가 있는 메시지가 %d개 있습니다");
        it(THE_MESSAGES_WITH_TEXT, "Il libro ha %d messaggi con testo");

        ja(THE_MESSAGES_ARE_NUMBERED_1, "メッセージの番号は 1 から %d までです");
        zh(THE_MESSAGES_ARE_NUMBERED_1, "消息编号从 1 到 %d");
        fr(THE_MESSAGES_ARE_NUMBERED_1, "Les messages sont numérotés de 1 à %d");
        es(THE_MESSAGES_ARE_NUMBERED_1, "Los mensajes están numerados del 1 al %d");
        de(THE_MESSAGES_ARE_NUMBERED_1, "Die Nachrichten sind von 1 bis %d nummeriert");
        ko(THE_MESSAGES_ARE_NUMBERED_1, "메시지는 1부터 %d까지 번호가 매겨져 있습니다");
        it(THE_MESSAGES_ARE_NUMBERED_1, "I messaggi sono numerati da 1 a %d");

        ja(THE_FIRST_MESSAGE_IS_AFTER, "開始メッセージが終了メッセージより後ろにあります");
        zh(THE_FIRST_MESSAGE_IS_AFTER, "起始消息在结束消息之后");
        fr(THE_FIRST_MESSAGE_IS_AFTER, "Le premier message est après le dernier");
        es(THE_FIRST_MESSAGE_IS_AFTER, "El primer mensaje está después del último");
        de(THE_FIRST_MESSAGE_IS_AFTER, "Die erste Nachricht liegt nach der letzten");
        ko(THE_FIRST_MESSAGE_IS_AFTER, "첫 메시지가 마지막 메시지 뒤에 있습니다");
        it(THE_FIRST_MESSAGE_IS_AFTER, "Il primo messaggio è dopo l'ultimo");

        ja(USE_SUMMARIES, "要約を使う");
        zh(USE_SUMMARIES, "使用摘要");
        fr(USE_SUMMARIES, "Utiliser les résumés");
        es(USE_SUMMARIES, "Usar resúmenes");
        de(USE_SUMMARIES, "Zusammenfassungen verwenden");
        ko(USE_SUMMARIES, "요약 사용");
        it(USE_SUMMARIES, "Usa i riassunti");

        ja(A_SUMMARY_STANDS_IN, "要約が対象とするメッセージがすべて範囲内にある場合、要約がそれらのメッセージの代わりになります。物語と一致しなくなった要約は使われません");
        zh(A_SUMMARY_STANDS_IN, "当摘要所涵盖的消息全部在范围内时，摘要会代替这些消息；不再符合故事的摘要不会被使用");
        fr(A_SUMMARY_STANDS_IN, "Un résumé remplace les messages qu'il couvre quand ils sont tous dans la plage ; un résumé qui ne décrit plus l'histoire n'est pas utilisé");
        es(A_SUMMARY_STANDS_IN, "Un resumen sustituye a los mensajes que cubre cuando todos están en el intervalo; un resumen que ya no describe la historia no se usa");
        de(A_SUMMARY_STANDS_IN, "Eine Zusammenfassung ersetzt die Nachrichten, die sie abdeckt, wenn alle im Bereich liegen; eine Zusammenfassung, die die Geschichte nicht mehr beschreibt, wird nicht verwendet");
        ko(A_SUMMARY_STANDS_IN, "요약은 범위에 모두 들어 있을 때 그것이 다루는 메시지를 대신합니다. 더 이상 이야기를 설명하지 않는 요약은 사용하지 않습니다");
        it(A_SUMMARY_STANDS_IN, "Un riassunto sostituisce i messaggi che copre quando tutti sono nell'intervallo; un riassunto che non descrive più la storia non viene usato");

        ja(ANSWER_FORMAT, "回答の形式");
        zh(ANSWER_FORMAT, "回答格式");
        fr(ANSWER_FORMAT, "Format de la réponse");
        es(ANSWER_FORMAT, "Formato de la respuesta");
        de(ANSWER_FORMAT, "Antwortformat");
        ko(ANSWER_FORMAT, "응답 형식");
        it(ANSWER_FORMAT, "Formato della risposta");

        ja(AUTOMATIC, "自動");
        zh(AUTOMATIC, "自动");
        fr(AUTOMATIC, "Automatique");
        es(AUTOMATIC, "Automático");
        de(AUTOMATIC, "Automatisch");
        ko(AUTOMATIC, "자동");
        it(AUTOMATIC, "Automatico");

        ja(JSON, "JSON");
        zh(JSON, "JSON");
        fr(JSON, "JSON");
        es(JSON, "JSON");
        de(JSON, "JSON");
        ko(JSON, "JSON");
        it(JSON, "JSON");

        ja(JSON_WITHOUT_A_SCHEMA, "スキーマなしの JSON");
        zh(JSON_WITHOUT_A_SCHEMA, "不带模式的 JSON");
        fr(JSON_WITHOUT_A_SCHEMA, "JSON sans schéma");
        es(JSON_WITHOUT_A_SCHEMA, "JSON sin esquema");
        de(JSON_WITHOUT_A_SCHEMA, "JSON ohne Schema");
        ko(JSON_WITHOUT_A_SCHEMA, "스키마 없는 JSON");
        it(JSON_WITHOUT_A_SCHEMA, "JSON senza schema");

        ja(MARKDOWN, "Markdown");
        zh(MARKDOWN, "Markdown");
        fr(MARKDOWN, "Markdown");
        es(MARKDOWN, "Markdown");
        de(MARKDOWN, "Markdown");
        ko(MARKDOWN, "Markdown");
        it(MARKDOWN, "Markdown");

        ja(AUTOMATIC_ASKS_FOR_JSON, "自動: モデルがスキーマに従える場合はスキーマ付きの JSON、それ以外は Markdown を要求します");
        zh(AUTOMATIC_ASKS_FOR_JSON, "自动：模型能遵循模式时要求带模式的 JSON，否则使用 Markdown");
        fr(AUTOMATIC_ASKS_FOR_JSON, "Automatique demande du JSON selon un schéma quand le modèle peut en suivre un, du Markdown sinon");
        es(AUTOMATIC_ASKS_FOR_JSON, "Automático pide JSON con un esquema cuando el modelo puede seguir uno, y Markdown si no");
        de(AUTOMATIC_ASKS_FOR_JSON, "„Automatisch“ verlangt JSON in einem Schema, wenn das Modell einem folgen kann, sonst Markdown");
        ko(AUTOMATIC_ASKS_FOR_JSON, "자동은 모델이 스키마를 따를 수 있으면 스키마가 있는 JSON을, 그렇지 않으면 Markdown을 요청합니다");
        it(AUTOMATIC_ASKS_FOR_JSON, "Automatico chiede JSON in uno schema quando il modello può seguirne uno, Markdown altrimenti");

        ja(JSON_WITHOUT_A_SCHEMA_HELP, "スキーマなしの JSON は、モデルの思考とスキーマを同時に受け付けないサーバー向けです");
        zh(JSON_WITHOUT_A_SCHEMA_HELP, "不带模式的 JSON 适用于无法同时接受模型思考和模式的服务器");
        fr(JSON_WITHOUT_A_SCHEMA_HELP, "JSON sans schéma est pour un serveur qui ne peut pas accepter un schéma en même temps que la réflexion du modèle");
        es(JSON_WITHOUT_A_SCHEMA_HELP, "JSON sin esquema es para un servidor que no puede aceptar un esquema junto con el razonamiento del modelo");
        de(JSON_WITHOUT_A_SCHEMA_HELP, "JSON ohne Schema ist für einen Server, der ein Schema nicht zusammen mit dem Denken des Modells annehmen kann");
        ko(JSON_WITHOUT_A_SCHEMA_HELP, "스키마 없는 JSON은 모델의 추론과 스키마를 함께 받을 수 없는 서버용입니다");
        it(JSON_WITHOUT_A_SCHEMA_HELP, "JSON senza schema è per un server che non può accettare uno schema insieme al ragionamento del modello");

        ja(THE_PROMPTS_ARE_SET_IN, "プロンプトは継承されます。本またはフォルダーの「プロンプト」タブ、または 設定 → 拡張機能の設定 → 書籍説明ジェネレーター で設定してください");
        zh(THE_PROMPTS_ARE_SET_IN, "提示词是继承的：请在本书或其文件夹的“提示词”标签页，或在 设置 → 扩展设置 → 书籍简介生成器 中设置");
        fr(THE_PROMPTS_ARE_SET_IN, "Les prompts sont hérités : définissez-les dans l'onglet Prompts du livre ou de son dossier, ou dans Paramètres → Paramètres des extensions → Générateur de description de livre");
        es(THE_PROMPTS_ARE_SET_IN, "Los prompts se heredan: defínelos en la pestaña Prompts del libro o de su carpeta, o en Ajustes → Ajustes de extensiones → Generador de descripciones de libros");
        de(THE_PROMPTS_ARE_SET_IN, "Die Prompts werden geerbt: Lege sie im Tab „Prompts“ des Buchs oder seines Ordners fest oder unter Einstellungen → Erweiterungseinstellungen → Buchbeschreibungs-Generator");
        ko(THE_PROMPTS_ARE_SET_IN, "프롬프트는 상속됩니다: 책이나 폴더의 프롬프트 탭, 또는 설정 → 확장 기능 설정 → 책 설명 생성기에서 설정하세요");
        it(THE_PROMPTS_ARE_SET_IN, "I prompt sono ereditati: impostali nella scheda Prompt del libro o della sua cartella, oppure in Impostazioni → Impostazioni delle estensioni → Generatore di descrizioni del libro");

        ja(THE_RANGE_HAS, "範囲内のメッセージ: %d 件、送信する部分: %d（うち要約 %d）");
        zh(THE_RANGE_HAS, "范围内有 %d 条消息，发送 %d 个部分（其中 %d 个是摘要）");
        fr(THE_RANGE_HAS, "%d messages dans la plage, %d parties sont envoyées (dont %d résumés)");
        es(THE_RANGE_HAS, "%d mensajes en el intervalo, se envían %d partes (%d de ellas resúmenes)");
        de(THE_RANGE_HAS, "%d Nachrichten im Bereich, %d Abschnitte werden gesendet (%d davon Zusammenfassungen)");
        ko(THE_RANGE_HAS, "범위에 메시지 %d개, 파트 %d개가 전송됩니다 (그중 요약 %d개)");
        it(THE_RANGE_HAS, "%d messaggi nell'intervallo, %d parti vengono inviate (%d di esse riassunti)");

        ja(TOKENS_SENT_AS_IT_IS, "プロンプトを含めて約 %s トークン、モデルが受け付けるのは %s: 物語はそのまま送信されます。");
        zh(TOKENS_SENT_AS_IT_IS, "包含提示词约 %s 个 token，模型可接受 %s：故事将原样发送。");
        fr(TOKENS_SENT_AS_IT_IS, "Environ %s jetons avec le prompt, le modèle en accepte %s : l'histoire est envoyée telle quelle.");
        es(TOKENS_SENT_AS_IT_IS, "Unos %s tokens con el prompt, el modelo admite %s: la historia se envía tal cual.");
        de(TOKENS_SENT_AS_IT_IS, "Etwa %s Tokens mit dem Prompt, das Modell nimmt %s: Die Geschichte wird unverändert gesendet.");
        ko(TOKENS_SENT_AS_IT_IS, "프롬프트 포함 약 %s 토큰, 모델이 받는 양은 %s: 이야기가 그대로 전송됩니다.");
        it(TOKENS_SENT_AS_IT_IS, "Circa %s token con il prompt, il modello ne accetta %s: la storia viene inviata così com'è.");

        ja(TOKENS_SENT_AS_NOTES, "プロンプトを含めて約 %s トークン、モデルが受け付けるのは %s: 物語が長すぎるため、先にノートを作成します（約 %d 回のリクエスト）。");
        zh(TOKENS_SENT_AS_NOTES, "包含提示词约 %s 个 token，模型可接受 %s：故事过长，将先生成笔记（约 %d 次请求）。");
        fr(TOKENS_SENT_AS_NOTES, "Environ %s jetons avec le prompt, le modèle en accepte %s : l'histoire est trop longue, des notes en sont d'abord tirées (environ %d requêtes).");
        es(TOKENS_SENT_AS_NOTES, "Unos %s tokens con el prompt, el modelo admite %s: la historia es demasiado larga, primero se hacen notas de ella (unas %d solicitudes).");
        de(TOKENS_SENT_AS_NOTES, "Etwa %s Tokens mit dem Prompt, das Modell nimmt %s: Die Geschichte ist zu lang, daher werden zuerst Notizen daraus erstellt (etwa %d Anfragen).");
        ko(TOKENS_SENT_AS_NOTES, "프롬프트 포함 약 %s 토큰, 모델이 받는 양은 %s: 이야기가 너무 길어 먼저 메모로 만듭니다 (약 %d회 요청).");
        it(TOKENS_SENT_AS_NOTES, "Circa %s token con il prompt, il modello ne accetta %s: la storia è troppo lunga, ne vengono prima ricavate delle note (circa %d richieste).");

        ja(TOKENS_CONTEXT_UNKNOWN, "プロンプトを含めて約 %s トークン。モデルのコンテキストサイズが不明なため、物語はそのまま送信されます。");
        zh(TOKENS_CONTEXT_UNKNOWN, "包含提示词约 %s 个 token。模型的上下文大小未知，故事将原样发送。");
        fr(TOKENS_CONTEXT_UNKNOWN, "Environ %s jetons avec le prompt. La taille de contexte du modèle est inconnue, l'histoire est envoyée telle quelle.");
        es(TOKENS_CONTEXT_UNKNOWN, "Unos %s tokens con el prompt. Se desconoce el tamaño del contexto del modelo, la historia se envía tal cual.");
        de(TOKENS_CONTEXT_UNKNOWN, "Etwa %s Tokens mit dem Prompt. Die Kontextgröße des Modells ist nicht bekannt, die Geschichte wird unverändert gesendet.");
        ko(TOKENS_CONTEXT_UNKNOWN, "프롬프트 포함 약 %s 토큰. 모델의 컨텍스트 크기를 알 수 없으므로 이야기가 그대로 전송됩니다.");
        it(TOKENS_CONTEXT_UNKNOWN, "Circa %s token con il prompt. La dimensione del contesto del modello non è nota, la storia viene inviata così com'è.");

        ja(THE_PROMPT_CANT_BE_BUILT, "プロンプトを作成できません: %s");
        zh(THE_PROMPT_CANT_BE_BUILT, "无法构建提示词：%s");
        fr(THE_PROMPT_CANT_BE_BUILT, "Le prompt ne peut pas être construit : %s");
        es(THE_PROMPT_CANT_BE_BUILT, "No se puede construir el prompt: %s");
        de(THE_PROMPT_CANT_BE_BUILT, "Der Prompt kann nicht aufgebaut werden: %s");
        ko(THE_PROMPT_CANT_BE_BUILT, "프롬프트를 만들 수 없습니다: %s");
        it(THE_PROMPT_CANT_BE_BUILT, "Impossibile costruire il prompt: %s");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");
        fr(GENERATE, "Générer");
        es(GENERATE, "Generar");
        de(GENERATE, "Generieren");
        ko(GENERATE, "생성");
        it(GENERATE, "Genera");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");
        ko(STOP, "중지");
        it(STOP, "Interrompi");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");
        fr(CHOOSE_A_MODEL, "Choisissez un modèle");
        es(CHOOSE_A_MODEL, "Elige un modelo");
        de(CHOOSE_A_MODEL, "Wähle ein Modell");
        ko(CHOOSE_A_MODEL, "모델 선택");
        it(CHOOSE_A_MODEL, "Scegli un modello");

        ja(THE_BOOK_HAS_NO_STORY, "この本には説明する物語がありません");
        zh(THE_BOOK_HAS_NO_STORY, "这本书还没有可供描述的故事");
        fr(THE_BOOK_HAS_NO_STORY, "Le livre n'a pas d'histoire à décrire");
        es(THE_BOOK_HAS_NO_STORY, "El libro no tiene historia que describir");
        de(THE_BOOK_HAS_NO_STORY, "Das Buch hat keine Geschichte zum Beschreiben");
        ko(THE_BOOK_HAS_NO_STORY, "책에 설명할 이야기가 없습니다");
        it(THE_BOOK_HAS_NO_STORY, "Il libro non ha una storia da descrivere");

        ja(CANT_GENERATE, "生成できません: %s");
        zh(CANT_GENERATE, "无法生成：%s");
        fr(CANT_GENERATE, "Impossible de générer : %s");
        es(CANT_GENERATE, "No se puede generar: %s");
        de(CANT_GENERATE, "Generierung nicht möglich: %s");
        ko(CANT_GENERATE, "생성할 수 없습니다: %s");
        it(CANT_GENERATE, "Impossibile generare: %s");

        ja(NOTES_ROUND, "ノート、ラウンド %d: 物語を %d 個の部分に分割しました");
        zh(NOTES_ROUND, "笔记，第 %d 轮：故事被分成 %d 个部分");
        fr(NOTES_ROUND, "Notes, tour %d : l'histoire est divisée en %d parties");
        es(NOTES_ROUND, "Notas, ronda %d: la historia se divide en %d partes");
        de(NOTES_ROUND, "Notizen, Runde %d: Die Geschichte wird in %d Teile geteilt");
        ko(NOTES_ROUND, "메모, %d라운드: 이야기를 %d개 부분으로 나눕니다");
        it(NOTES_ROUND, "Note, turno %d: la storia è divisa in %d parti");

        ja(NOTES_PART_DONE, "ノート: %d / %d 個目が完了しました");
        zh(NOTES_PART_DONE, "笔记：第 %d / %d 部分已完成");
        fr(NOTES_PART_DONE, "Notes : partie %d sur %d terminée");
        es(NOTES_PART_DONE, "Notas: parte %d de %d hecha");
        de(NOTES_PART_DONE, "Notizen: Teil %d von %d fertig");
        ko(NOTES_PART_DONE, "메모: %d / %d 부분 완료");
        it(NOTES_PART_DONE, "Note: parte %d di %d fatta");

        ja(WRITING_THE_DESCRIPTION, "名前と説明を書いています...");
        zh(WRITING_THE_DESCRIPTION, "正在撰写名称和简介……");
        fr(WRITING_THE_DESCRIPTION, "Écriture du nom et de la description...");
        es(WRITING_THE_DESCRIPTION, "Escribiendo el nombre y la descripción...");
        de(WRITING_THE_DESCRIPTION, "Der Name und die Beschreibung werden geschrieben ...");
        ko(WRITING_THE_DESCRIPTION, "이름과 설명을 쓰는 중...");
        it(WRITING_THE_DESCRIPTION, "Scrittura del nome e della descrizione...");

        ja(WRITING_THE_DESCRIPTION_FROM_NOTES, "ノートから名前と説明を書いています...");
        zh(WRITING_THE_DESCRIPTION_FROM_NOTES, "正在根据笔记撰写名称和简介……");
        fr(WRITING_THE_DESCRIPTION_FROM_NOTES, "Écriture du nom et de la description à partir des notes...");
        es(WRITING_THE_DESCRIPTION_FROM_NOTES, "Escribiendo el nombre y la descripción a partir de las notas...");
        de(WRITING_THE_DESCRIPTION_FROM_NOTES, "Der Name und die Beschreibung werden aus den Notizen geschrieben ...");
        ko(WRITING_THE_DESCRIPTION_FROM_NOTES, "메모로 이름과 설명을 쓰는 중...");
        it(WRITING_THE_DESCRIPTION_FROM_NOTES, "Scrittura del nome e della descrizione dalle note...");

        ja(DONE, "完了");
        zh(DONE, "完成");
        fr(DONE, "Terminé");
        es(DONE, "Hecho");
        de(DONE, "Fertig");
        ko(DONE, "완료");
        it(DONE, "Fatto");

        ja(STOPPED, "停止しました");
        zh(STOPPED, "已停止");
        fr(STOPPED, "Arrêté");
        es(STOPPED, "Detenido");
        de(STOPPED, "Gestoppt");
        ko(STOPPED, "중지됨");
        it(STOPPED, "Interrotto");

        ja(NOTES, "ノート");
        zh(NOTES, "笔记");
        fr(NOTES, "Notes");
        es(NOTES, "Notas");
        de(NOTES, "Notizen");
        ko(NOTES, "메모");
        it(NOTES, "Note");

        ja(NOTES_OF_A_PART, "%d / %d 個目");
        zh(NOTES_OF_A_PART, "第 %d / %d 部分");
        fr(NOTES_OF_A_PART, "Partie %d sur %d");
        es(NOTES_OF_A_PART, "Parte %d de %d");
        de(NOTES_OF_A_PART, "Teil %d von %d");
        ko(NOTES_OF_A_PART, "파트 %d / %d");
        it(NOTES_OF_A_PART, "Parte %d di %d");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");

        ja(DESCRIPTION, "説明");
        zh(DESCRIPTION, "简介");
        fr(DESCRIPTION, "Description");
        es(DESCRIPTION, "Descripción");
        de(DESCRIPTION, "Beschreibung");
        ko(DESCRIPTION, "설명");
        it(DESCRIPTION, "Descrizione");

        ja(COPY_TO_BOOK, "本にコピー");
        zh(COPY_TO_BOOK, "复制到书中");
        fr(COPY_TO_BOOK, "Copier dans le livre");
        es(COPY_TO_BOOK, "Copiar al libro");
        de(COPY_TO_BOOK, "In das Buch kopieren");
        ko(COPY_TO_BOOK, "책에 복사");
        it(COPY_TO_BOOK, "Copia nel libro");

        ja(COPIED_TO_THE_BOOK, "本にコピーしました");
        zh(COPIED_TO_THE_BOOK, "已复制到书中");
        fr(COPIED_TO_THE_BOOK, "Copié dans le livre");
        es(COPIED_TO_THE_BOOK, "Copiado al libro");
        de(COPIED_TO_THE_BOOK, "In das Buch kopiert");
        ko(COPIED_TO_THE_BOOK, "책에 복사했습니다");
        it(COPIED_TO_THE_BOOK, "Copiato nel libro");

        ja(CANT_COPY_TO_THE_BOOK, "本にコピーできません: %s");
        zh(CANT_COPY_TO_THE_BOOK, "无法复制到书中：%s");
        fr(CANT_COPY_TO_THE_BOOK, "Impossible de copier dans le livre : %s");
        es(CANT_COPY_TO_THE_BOOK, "No se puede copiar al libro: %s");
        de(CANT_COPY_TO_THE_BOOK, "Kopieren in das Buch nicht möglich: %s");
        ko(CANT_COPY_TO_THE_BOOK, "책에 복사할 수 없습니다: %s");
        it(CANT_COPY_TO_THE_BOOK, "Impossibile copiare nel libro: %s");

        ja(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "モデルが書いた名前と説明がここに表示されます");
        zh(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "模型撰写的名称和简介会显示在这里");
        fr(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "Le nom et la description écrits par le modèle apparaissent ici");
        es(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "Aquí aparecen el nombre y la descripción que escribe el modelo");
        de(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "Der Name und die Beschreibung, die das Modell schreibt, erscheinen hier");
        ko(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "모델이 쓴 이름과 설명이 여기에 표시됩니다");
        it(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE, "Il nome e la descrizione che il modello scrive compaiono qui");

        ja(THE_ANSWER_CANT_BE_READ, "回答から名前と説明を読み取れませんでした。モデルの回答:");
        zh(THE_ANSWER_CANT_BE_READ, "无法从回答中读取名称和简介。模型的回答如下：");
        fr(THE_ANSWER_CANT_BE_READ, "La réponse n'a ni nom ni description lisibles. Voici ce que le modèle a écrit :");
        es(THE_ANSWER_CANT_BE_READ, "La respuesta no tiene un nombre y una descripción que se puedan leer. Esto es lo que escribió el modelo:");
        de(THE_ANSWER_CANT_BE_READ, "Die Antwort enthält keinen lesbaren Namen und keine lesbare Beschreibung. Das hat das Modell geschrieben:");
        ko(THE_ANSWER_CANT_BE_READ, "응답에서 읽을 수 있는 이름과 설명이 없습니다. 모델이 쓴 내용은 다음과 같습니다:");
        it(THE_ANSWER_CANT_BE_READ, "La risposta non ha un nome e una descrizione leggibili. Questo è ciò che ha scritto il modello:");

        ja(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "ブック説明ジェネレーター: 説明を作成");
        zh(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "书籍简介生成器：撰写简介");
        fr(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "Générateur de description de livre : écrire la description");
        es(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "Generador de descripciones de libros: escribir la descripción");
        de(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "Buchbeschreibungs-Generator: die Beschreibung schreiben");
        ko(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "책 설명 생성기: 설명 쓰기");
        it(BOOK_DESCRIPTION_GENERATOR_WRITE_THE_DESCRIPTION, "Generatore di descrizioni del libro: scrivi la descrizione");
    }
}
