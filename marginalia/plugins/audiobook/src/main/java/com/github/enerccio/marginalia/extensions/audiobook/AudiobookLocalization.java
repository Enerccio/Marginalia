package com.github.enerccio.marginalia.extensions.audiobook;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.audiobook.label.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Audiobook in the languages besides English; the English text is the key.
 */
public class AudiobookLocalization extends PluginLocalizationBundle {

    public static final AudiobookLocalization T = new AudiobookLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final AudiobookDefaults englishDefaults = new AudiobookDefaults();
    private final Map<Language, AudiobookDefaults> translatedDefaults = Map.of(JA, new AudiobookDefaultsJA(), ZH, new AudiobookDefaultsZH(), FR, new AudiobookDefaultsFR(), ES, new AudiobookDefaultsES(), DE, new AudiobookDefaultsDE(),
            IT, new AudiobookDefaultsIT(), KO, new AudiobookDefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public AudiobookDefaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

    public static final String AUDIOBOOK = "Audiobook";
    public static final String AUDIOBOOK_SETTINGS = "Audiobook Settings";
    public static final String LABELLING_PROMPT = "Labelling prompt";
    public static final String EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS = "Even out the loudness of the parts";
    public static final String TARGET_LOUDNESS_LUFS = "Target loudness (LUFS)";
    public static final String THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED = "The joined audio of each part is levelled to this loudness with ffmpeg (EBU R128), so the parts sit at the same level. A part made before is levelled the next time you convert it; its pieces are kept, so the speech server is not asked.";
    public static final String FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED = "ffmpeg was not found: the audio is not levelled.";
    public static final String THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE = "The instruction that tells the language model who speaks each piece of a part and how it is read (a template). Empty: the built-in one.";
    public static final String SPEECH_SERVERS_HELP = "The speech servers the audiobook is made by. The requests around a batch make room for the server on the GPU and stop it again.";
    public static final String BACKENDS = "Backends";
    public static final String ADD_A_BACKEND = "Add a backend";
    public static final String DELETE_THE_BACKEND = "Delete the backend";
    public static final String NEW_BACKEND = "New backend";
    public static final String NAME = "Name";
    public static final String ADDRESS = "Address";
    public static final String BASE_ADDRESS_OF_THE_SPEECH_SERVER = "Base address of the speech server (Chatterbox-TTS-Server), for example http://localhost:8004, or the address your proxy forwards to it";
    public static final String API_KEY = "Api key";
    public static final String STORED_TYPE_TO_REPLACE = "Stored. Type to replace it.";
    public static final String REMOVE_THE_KEY = "Remove the key";
    public static final String SPEECH_MODEL = "Speech model";
    public static final String MODEL_TURBO = "Chatterbox Turbo (English, sound tags)";
    public static final String MODEL_ORIGINAL = "Chatterbox (English, exaggeration)";
    public static final String MODEL_MULTILINGUAL = "Chatterbox Multilingual (23 languages)";
    public static final String THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD = "The model the requests before a batch have to leave loaded; a book that needs another one picks another backend";
    public static final String THE_SERVER_IS_LOCAL = "The server is on this machine or on my network";
    public static final String ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK = "Allows the addresses of the local network; leave it off for a server in the cloud";
    public static final String TIMEOUT_SECONDS = "Timeout of a request (s)";
    public static final String RESTART_THE_SERVER_AFTER_PIECES = "Restart the server after (pieces)";
    public static final String RESTART_THE_SERVER_HELP = "0 = never. The requests after and before a batch are made again in the middle of a batch every so many pieces, for a server that leaks memory. A server that dies is restarted whatever this says.";
    public static final String DEFAULTS_OF_A_SEGMENT = "Defaults of a segment";
    public static final String TEMPERATURE = "Temperature";
    public static final String CFG_WEIGHT = "CFG weight";
    public static final String SPEED = "Speed";
    public static final String CHUNK_SIZE = "Chunk size (characters)";
    public static final String BEFORE_A_BATCH = "Before a batch";
    public static final String AFTER_A_BATCH = "After a batch";
    public static final String REQUESTS_MADE_AROUND_A_BATCH = "Requests made around a batch of parts, one after the other. Before: free the GPU and start the speech server (unload the language model, start the container, load the model). After: stop it. None does nothing.";
    public static final String ADD_A_REQUEST = "Add a request";
    public static final String REMOVE_THE_REQUEST = "Remove the request";
    public static final String REQUEST = "Request";
    public static final String WAIT_AFTERWARDS_SECONDS = "Wait afterwards (s)";
    public static final String METHOD = "Method";
    public static final String HEADERS_NAME_VALUE_ONE_PER_LINE = "Headers (Name: value, one per line)";
    public static final String BODY = "Body";
    public static final String TEST_CONNECTION = "Test connection";
    public static final String THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED = "The server answers, the model is loaded (%s)";
    public static final String THE_SERVER_ANSWERS_NO_MODEL = "The server answers, no model is loaded (the requests before a batch load it)";
    public static final String CONNECTION_FAILED = "Connection failed: %s";
    public static final String NARRATOR_VOICE = "Narrator voice";
    public static final String NARRATOR_VOICE_HELP = "The narrator when a book has no cast, or its cast has no narrator voice.";
    public static final String CONVERT_TO_AUDIO = "Convert to audio";
    public static final String BACKEND = "Backend";
    public static final String LOAD = "Load";
    public static final String LOAD_THE_PARTS_HINT = "The book has %s parts with text. Load them to see what each has, tick the parts, plan what to do with them and press Start.";
    public static final String PART_NOT_GENERATED = "Not generated";
    public static final String PART_LABELLED = "Labelled";
    public static final String PART_GENERATED = "Generated";
    public static final String PART_GENERATED_TEXT_CHANGED = "Generated (the text changed)";
    public static final String PART_FINAL = "final";
    public static final String PLAN = "Plan";
    public static final String GENERATE = "Generate";
    public static final String REGENERATE = "Regenerate";
    public static final String DELETE_AUDIO = "Delete audio";
    public static final String REMOVE_LABELS = "Remove labels";
    public static final String CLEAR_THE_PLAN = "Clear the plan";
    public static final String PLAY = "Play";
    public static final String SELECT_THE_PARTS_FIRST = "Tick the parts first";
    public static final String NOTHING_IS_PLANNED = "Nothing is planned: tick parts and choose Generate, Regenerate, Delete audio or Remove labels";
    public static final String APPLY_THE_PLAN_QUESTION = "The audio of %s parts will be deleted and the labels of %s parts removed. This cannot be undone. Carry out the plan?";
    public static final String THE_PLAN_WAS_CARRIED_OUT = "Audio deleted for %s parts, labels removed for %s parts";
    public static final String PARTS_IN_THE_QUEUE_ARE_LEFT_OUT = "%s parts are waiting or running in the queue and were left out";
    public static final String REASONING = "Reasoning";
    public static final String THE_ANSWER_OF_THE_MODEL = "Answer of the model";
    public static final String WHAT_THE_MODEL_SAID = "What the model said: %s";
    public static final String CLICK_TO_SEE_WHAT_THE_MODEL_SAID = "Click to read what the model said while it labelled this part";
    public static final String START = "Start";
    public static final String STOP_ALL = "Stop all";
    public static final String CLEAR_FINISHED = "Clear finished";
    public static final String CLOSE = "Close";
    public static final String PART = "Part";
    public static final String STATE = "State";
    public static final String MESSAGE = "Message";
    public static final String STATE_WAITING = "Waiting";
    public static final String STATE_RUNNING = "Running";
    public static final String STATE_DONE = "Done";
    public static final String STATE_FAILED = "Failed";
    public static final String STATE_CANCELLED = "Cancelled";
    public static final String DOWNLOAD = "Download";
    public static final String RETRY = "Retry";
    public static final String CANCEL = "Cancel";
    public static final String NO_BACKEND_YET = "No backend yet. Add one in Settings → Extension Settings → Audiobook Settings.";
    public static final String THERE_IS_NO_NARRATOR_VOICE = "There is no narrator voice. Pick it in the cast, or in Settings → Extension Settings → Audiobook Settings.";
    public static final String THE_BOOK_HAS_NO_TEXT = "The book has no parts with text";
    public static final String THE_PARTS_ARE_IN_THE_QUEUE = "%s parts are in the queue";
    public static final String THE_CONVERSION_COULD_NOT_BE_STARTED = "The conversion could not be started: %s";
    public static final String LABEL_WITH_A_LANGUAGE_MODEL = "Label the parts with a language model (who speaks, how, sound tags)";
    public static final String THE_MODEL_THAT_LABELS = "The model that labels";
    public static final String THE_PARTS_ARE_LABELLED_FIRST = "The parts are labelled first (the model must be loaded), then spoken: the requests before the batch unload the model for the speech.";
    public static final String LISTEN_TO_THE_AUDIOBOOK = "Listen to the audiobook";
    public static final String PAUSE_LISTENING = "Pause";
    public static final String STOP = "Stop";
    public static final String THIS_PART_HAS_NO_AUDIO = "This part has no audio (yet)";
    public static final String THE_AUDIO_COULD_NOT_BE_LOADED = "The audio could not be loaded";

    public static final String DELETE = "Delete";
    public static final String SAVE = "Save";
    public static final String REMOVE = "Remove";
    public static final String CAST = "Cast";
    public static final String CHARACTERS = "Characters";
    public static final String NARRATOR = "Narrator";
    public static final String OTHER_SPEAKERS = "Other speakers";
    public static final String VOICE = "Voice";
    public static final String WHEN = "When";
    public static final String MOOD_NAME = "Mood: %s";
    public static final String LANGUAGE_NAME = "Language: %s";
    public static final String A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE = "A voice for a mood or a language";
    public static final String A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER = "A clip, or a voice file (Henry.wav)";
    public static final String ADD_A_CLIP = "Add a clip";
    public static final String THE_CLIP_COULD_NOT_BE_ADDED = "The clip could not be added: %s";
    public static final String ADD_A_CHARACTER = "Add a character";
    public static final String ADD_THE_CHARACTERS_OF_THE_BOOK = "Add the characters of the book";
    public static final String CHARACTERS_ADDED = "Characters added: %s";
    public static final String NEW_CHARACTER = "New character";
    public static final String OTHER_NAMES = "Other names";
    public static final String SEPARATED_BY_COMMAS = "Separated by commas";
    public static final String THE_CAST_COULD_NOT_BE_STORED = "The cast could not be stored: %s";
    public static final String THE_CAST_IS_STORED = "The cast is stored";

    public static final String ACTOR = "Actor";
    public static final String ACTORS = "Actors";
    public static final String NEW_ACTOR = "New actor";
    public static final String EDIT_THE_ACTOR = "Edit the actor";
    public static final String DELETE_THE_ACTOR = "Delete the actor";
    public static final String THE_ACTOR_IS_DELETED = "The actor \"%s\" is deleted. A cast that names it has no voice for it then, and the voice of the other speakers reads. The clips stay among your resources.";
    public static final String THE_ACTOR_COULD_NOT_BE_STORED = "The actor could not be stored: %s";
    public static final String NOT_SET = "Not set";
    public static final String ACTORS_HELP = "An actor is a voice of yours: a reference clip or a voice file of the speech server, with voices of its own for a mood or a language. Any book can give an actor to its narrator and its characters (Convert to audio → Cast).";
    public static final String THE_CAST_HELP = "Who reads in this book: an actor for the narrator and one for each character. The actors are your voices (Settings → Extension Settings → Audiobook Settings); a new one can be made here. The cast is kept with the book.";
    public static final String THE_FALLBACK_HELP = "The actor that reads when the model names a speaker the cast does not have. Not set: the narrator reads. A character without an actor is read the same way.";

    public static final String THE_ACTOR_HELP = "The voice is a reference clip (one of your audio resources, or a new one) or the file of a voice of the speech server. A mood or a language can have a voice of its own: a whisper, a shout, a clip that speaks Japanese; the language wins over the mood, and without a voice for them the voice above reads.";

    public static final String AUDIOBOOK_STUDIO = "Audiobook studio";
    public static final String ANNOTATE = "Annotate";
    public static final String ANNOTATING = "Annotating the part ...";
    public static final String A_FINAL_PART_IS_NOT_ANNOTATED = "A final part is left alone: set its status to reviewed or draft first.";
    public static final String MAKE_THE_PART = "Make the part from this script";
    public static final String MAKING_THE_PART = "Making the part ...";
    public static final String HEAR_THE_PART = "Hear the part";
    public static final String THE_PART_HAS_NO_AUDIO_YET = "The part has no audio yet";
    public static final String THIS_PART_HAS_NO_SCRIPT_YET = "This part has no script yet: annotate it.";
    public static final String THE_TEXT_CHANGED_ANNOTATE_AGAIN = "The text of the part changed since this script was made: annotate it again (the segments you changed are kept).";
    public static final String SPEAKER = "Speaker";
    public static final String SPOKEN_TEXT = "Spoken text";
    public static final String DELIVERY = "Delivery";
    public static final String INTENSITY = "Intensity";
    public static final String EXAGGERATION = "Exaggeration";
    public static final String LANGUAGE = "Language";
    public static final String SEED = "Seed";
    public static final String SEED_X = "Seed %s";
    public static final String PAUSE_AFTER_SECONDS = "Pause after (s)";
    public static final String STATUS = "Status";
    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_REVIEWED = "Reviewed";
    public static final String STATUS_FINAL = "Final";
    public static final String LOCKED = "Locked";
    public static final String TAKES = "Takes";
    public static final String TAKE = "Take";
    public static final String MADE = "Made";
    public static final String HEAR = "Hear";
    public static final String ANOTHER_TAKE = "Another take";
    public static final String LOWER_THE_INTENSITY = "Lower the intensity";
    public static final String MORE_PAUSE = "More pause";
    public static final String UNLOCK = "Unlock";
    public static final String APPLY = "Apply";
    public static final String CHOOSE_AND_HEAR_THE_TAKE = "Use this take and hear it";
    public static final String CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION = "Changed by hand: a new annotation keeps it as it is.";
    public static final String AS_THE_ANNOTATION_MADE_IT = "As the annotation made it: a new annotation replaces it.";
    public static final String THE_REQUEST_TO_THE_SERVER = "The request the speech server gets";
    public static final String MAKING_THE_SEGMENT = "Making the segment (the speech server is started for it) ...";
    public static final String THE_JOB_FAILED = "It failed: %s";

    public static final String FINAL_PARTS_ARE_LEFT_ALONE = "Parts left alone because their status is final: %s";

    public static final String PRONUNCIATION = "Pronunciation";
    public static final String NUMBERS_AND_ABBREVIATIONS = "Numbers and abbreviations";
    public static final String RULES_ENGLISH = "English: numbers, abbreviations and numbered headings are read as words";
    public static final String RULES_NONE = "Off: only the dictionary (for a book in another language)";
    public static final String WORD_BEFORE_A_CHAPTER_NUMBER = "Word before a chapter number";
    public static final String FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER = "For a heading that is only a number: \"IV. The Arch\" is read \"Chapter four. The Arch\". Empty: just the number.";
    public static final String PRONUNCIATION_HELP = "The book is never changed: this is how the text is spelled for the voice. The dictionary replaces whole words (case matters) with what is said for them: a respelling for a name or a made-up word, or a reading in kana for Japanese. Pieces whose spoken text changes are made again, the others are kept.";
    public static final String DICTIONARY = "Dictionary";
    public static final String ADD_A_WORD = "Add a word";
    public static final String WORD_IN_THE_BOOK = "Word in the book";
    public static final String SAID_AS = "Said as";
    public static final String TRY_A_TEXT = "Try a text";
    public static final String AS_A_HEADING = "As a heading";
    public static final String IT_IS_SAID_AS = "It is said as";
    public static final String NOTHING_SCENE_BREAK = "(nothing: a scene break, a pause)";
    public static final String THE_PRONUNCIATION_IS_STORED = "The pronunciation is stored";
    public static final String THE_PRONUNCIATION_COULD_NOT_BE_STORED = "The pronunciation could not be stored: %s";
    public static final String IN_THE_BOOK = "In the book: %s";

    public static final String AUDIOBOOK_ZIP = "Audiobook (one mp3 per chapter, in a zip)";
    public static final String AUDIOBOOK_ONE_MP3 = "Audiobook (one mp3 with chapter marks)";
    public static final String AUDIOBOOK_M4B = "Audiobook (one m4b with chapters)";
    public static final String STORAGE_OF_THE_AUDIO = "How the audio is stored (for everyone on this installation)";
    public static final String STORAGE_MP3 = "mp3 (small; an m4b is encoded a second time)";
    public static final String STORAGE_WAV = "wav (lossless, about ten times the size; needs ffmpeg)";
    public static final String STORAGE_OF_THE_AUDIO_HELP = "Only administrators see this. It applies to the parts that are made from now on; the parts that were made before keep their audio and are understood as they are. A wav is encoded by ffmpeg for the mp3 and zip exports and once for the m4b, so the m4b is not encoded twice.";
    public static final String FFMPEG_IS_NOT_INSTALLED = "ffmpeg is not installed on the server: only mp3 can be chosen, and the m4b export is not offered.";
    public static final String THE_STORAGE_OF_THE_AUDIO_IS_SAVED = "Saved. New parts are stored this way.";
    public static final String ONLY_THE_PARTS_MARKED_FINAL = "Audiobook: only the parts marked final";
    public static final String PART_NUMBER = "Part %s";
    public static final String NO_AUDIO = "no audio";
    public static final String AUDIO_OLDER_THAN_THE_TEXT = "audio older than the text";
    public static final String AUDIO_OLDER_THAN_THE_SCRIPT = "audio older than the script";
    public static final String PARTS_LEFT_OUT_OF_THE_AUDIOBOOK = "Parts left out of the audiobook: %s. Convert them (or make them again in the studio) first.";
    public static final String NO_PART_HAS_UP_TO_DATE_AUDIO = "No part has up to date audio: %s. Convert the parts first (Convert to audio), or choose parts that are final.";

    public static final String NO_PART_TO_EXPORT = "There is no part to export: none is marked final (or the range of parts is empty).";

    public static final String CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF = "Convert new parts to audio by itself";
    public static final String AUTO_NO = "No";
    public static final String AUTO_PLAIN = "Yes: the narrator reads the part";
    public static final String AUTO_LABELLED = "Yes: labelled by the model of the book, the cast gives the voices";
    public static final String A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL = "When a generated part is final (the automatic annotation and redraft of the book are done, if it has them) it goes into the queue of the audiobook. The speech waits while a generation of yours runs. Uses the speech server picked in Settings → Extension Settings → Audiobook Settings.";
    public static final String THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO = "The new part is in the audiobook queue";
    public static final String THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART = "The book has no model for the labelling: the narrator reads the new part";
    public static final String THE_AUDIO_OF_THE_NEW_PART_IS_READY = "The audio of the new part is ready";
    public static final String THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE = "The audio of the new part could not be made: %s";
    public static final String AUTOMATIC_CONVERSION_OF_NEW_PARTS = "Convert new parts by itself, for the books that say nothing";

    public static final String PREVIEW = "Preview";
    public static final String MAKING_THE_SAMPLE = "Making the sample ...";
    public static final String PICK_A_VOICE_FIRST = "Pick a voice first";
    public static final String THE_VOICE_COULD_NOT_BE_HEARD = "The voice could not be heard: %s";
    public static final String KEY_OF_THE_REQUESTS = "Key of the requests around a batch";
    public static final String KEY_OF_THE_REQUESTS_HELP = "Sent as Authorization: Bearer with every request before and after a batch, stored encrypted and never shown again, so it does not have to be written into their headers. Empty: the api key above is used.";
    public static final String THE_REQUEST_AFTER_THE_TEST_FAILED = "The server answers, but the request after the test failed: %s";
    public static final String THE_REQUESTS_AROUND_THE_TEST_RAN = "The requests before and after the test were made. ";
    public static final String TEST_HELP = "The test makes the requests before a batch (they start a server that is stopped), asks the server, and makes the requests after it.";

    public static final String DOWNLOAD_THE_AUDIOBOOK = "The whole audiobook";
    public static final String DOWNLOAD_THE_AUDIOBOOK_HELP = "Joins all the parts that have up to date audio into one mp3 with a chapter for each part, and offers it for download";
    public static final String THE_AUDIOBOOK_IS_BEING_JOINED = "The audiobook is being joined ...";
    public static final String DOWNLOAD_THE_AUDIOBOOK_FILE = "Download the audiobook (about %s MB)";

    public static final String BACKEND_IN_USE = "Backend in use";
    public static final String BACKEND_IN_USE_HELP = "The speech server your conversions use. A shared one belongs to the administrator; its address and keys are not shown to you.";
    public static final String SHARED_MARK = " (shared)";
    public static final String OWN_SPEECH_SERVERS_ARE_OFF = "The administrator does not let users use speech servers of their own. The ones you added before are kept and are not used.";
    public static final String SHARED_SPEECH_SERVERS = "Shared speech servers";
    public static final String SHARED_SPEECH_SERVERS_HELP = "Only administrators see this. Speech servers that users may use, and whether they may add their own.";
    public static final String SHARED_SPEECH_SERVERS_DIALOG_HELP = "A user who may use a shared server picks it as the backend in use. They see its name, never its address, its keys or its requests. The narrator voice should be a voice of the server (a clip belongs to one user).";
    public static final String ALL_USERS_MAY_USE_IT = "All users may use it";
    public static final String THESE_USERS_MAY_USE_IT = "These users may use it";
    public static final String USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS = "Users may add speech servers of their own";
    public static final String USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP = "When this is off, users can only use the shared servers; the servers they added before are kept and are not used.";
    public static final String A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS = "The shared speech server \"%s\" has no address";
    public static final String THE_SHARED_SPEECH_SERVERS_ARE_SAVED = "The shared speech servers are saved";

    public static final String READ_THIS_PART_WITH_THE_BROWSER_VOICE = "Read this part (and the next ones without audio) with the voice of the browser";

    public static final String GPU_NAME = "GPU (name)";
    public static final String GPU_NAME_HELP = "Optional. Name the GPU when this server shares it with other servers (an image server, another speech server), and give the same name there: they then wait for each other instead of unloading each other's model. Empty: only requests to this address wait for each other.";

    @Override
    protected void loadKeys() {
        ja(AUDIOBOOK, "オーディオブック");
        zh(AUDIOBOOK, "有声书");
        fr(AUDIOBOOK, "Livre audio");
        es(AUDIOBOOK, "Audiolibro");
        de(AUDIOBOOK, "Hörbuch");

        ja(AUDIOBOOK_SETTINGS, "オーディオブックの設定");
        zh(AUDIOBOOK_SETTINGS, "有声书设置");
        fr(AUDIOBOOK_SETTINGS, "Paramètres du livre audio");
        es(AUDIOBOOK_SETTINGS, "Ajustes del audiolibro");
        de(AUDIOBOOK_SETTINGS, "Hörbuch-Einstellungen");

        ja(SPEECH_SERVERS_HELP, "オーディオブックを作る音声合成サーバー。バッチの前後のリクエストで GPU にサーバーの場所を空け、終わったら停止します。");
        zh(SPEECH_SERVERS_HELP, "制作有声书所用的语音合成服务器。批处理前后的请求会为服务器腾出 GPU，并在结束后将其停止。");
        fr(SPEECH_SERVERS_HELP, "Les serveurs de synthèse vocale qui fabriquent le livre audio. Les requêtes autour d'un lot font de la place pour le serveur sur le GPU puis l'arrêtent.");
        es(SPEECH_SERVERS_HELP, "Los servidores de voz con los que se crea el audiolibro. Las solicitudes alrededor de un lote hacen sitio al servidor en la GPU y lo detienen de nuevo.");
        de(SPEECH_SERVERS_HELP, "Die Sprachserver, mit denen das Hörbuch erstellt wird. Die Anfragen rund um einen Stapel schaffen Platz für den Server auf der GPU und stoppen ihn wieder.");

        ja(BACKENDS, "バックエンド");
        zh(BACKENDS, "后端");
        fr(BACKENDS, "Moteurs");
        es(BACKENDS, "Backends");
        de(BACKENDS, "Backends");

        ja(ADD_A_BACKEND, "バックエンドを追加");
        zh(ADD_A_BACKEND, "添加后端");
        fr(ADD_A_BACKEND, "Ajouter un moteur");
        es(ADD_A_BACKEND, "Añadir un backend");
        de(ADD_A_BACKEND, "Ein Backend hinzufügen");

        ja(DELETE_THE_BACKEND, "バックエンドを削除");
        zh(DELETE_THE_BACKEND, "删除后端");
        fr(DELETE_THE_BACKEND, "Supprimer le moteur");
        es(DELETE_THE_BACKEND, "Eliminar el backend");
        de(DELETE_THE_BACKEND, "Das Backend löschen");

        ja(NEW_BACKEND, "新しいバックエンド");
        zh(NEW_BACKEND, "新后端");
        fr(NEW_BACKEND, "Nouveau moteur");
        es(NEW_BACKEND, "Nuevo backend");
        de(NEW_BACKEND, "Neues Backend");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");

        ja(ADDRESS, "アドレス");
        zh(ADDRESS, "地址");
        fr(ADDRESS, "Adresse");
        es(ADDRESS, "Dirección");
        de(ADDRESS, "Adresse");

        ja(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "音声合成サーバー (Chatterbox-TTS-Server) のベースアドレス (例: http://localhost:8004)、またはプロキシが転送するアドレス");
        zh(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "语音合成服务器（Chatterbox-TTS-Server）的基础地址，例如 http://localhost:8004，或代理转发到它的地址");
        fr(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Adresse de base du serveur de synthèse vocale (Chatterbox-TTS-Server), par exemple http://localhost:8004, ou l'adresse vers laquelle votre proxy le redirige");
        es(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Dirección base del servidor de voz (Chatterbox-TTS-Server), por ejemplo http://localhost:8004, o la dirección a la que lo reenvía tu proxy");
        de(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Basisadresse des Sprachservers (Chatterbox-TTS-Server), zum Beispiel http://localhost:8004, oder die Adresse, an die dein Proxy weiterleitet");

        ja(API_KEY, "API キー");
        zh(API_KEY, "API 密钥");
        fr(API_KEY, "Clé d'API");
        es(API_KEY, "Clave de API");
        de(API_KEY, "API-Schlüssel");

        ja(STORED_TYPE_TO_REPLACE, "保存済み。置き換えるには入力してください。");
        zh(STORED_TYPE_TO_REPLACE, "已保存。输入新密钥以替换。");
        fr(STORED_TYPE_TO_REPLACE, "Enregistrée. Tapez pour la remplacer.");
        es(STORED_TYPE_TO_REPLACE, "Guardada. Escribe para reemplazarla.");
        de(STORED_TYPE_TO_REPLACE, "Gespeichert. Tippe, um ihn zu ersetzen.");

        ja(REMOVE_THE_KEY, "キーを削除");
        zh(REMOVE_THE_KEY, "删除密钥");
        fr(REMOVE_THE_KEY, "Retirer la clé");
        es(REMOVE_THE_KEY, "Quitar la clave");
        de(REMOVE_THE_KEY, "Den Schlüssel entfernen");

        ja(SPEECH_MODEL, "音声モデル");
        zh(SPEECH_MODEL, "语音模型");
        fr(SPEECH_MODEL, "Modèle de synthèse vocale");
        es(SPEECH_MODEL, "Modelo de voz");
        de(SPEECH_MODEL, "Sprachmodell");

        ja(MODEL_TURBO, "Chatterbox Turbo (英語、音のタグ)");
        zh(MODEL_TURBO, "Chatterbox Turbo（英语，声音标签）");
        fr(MODEL_TURBO, "Chatterbox Turbo (anglais, balises sonores)");
        es(MODEL_TURBO, "Chatterbox Turbo (inglés, etiquetas de sonido)");
        de(MODEL_TURBO, "Chatterbox Turbo (Englisch, Sound-Tags)");

        ja(MODEL_ORIGINAL, "Chatterbox (英語、感情の強さ)");
        zh(MODEL_ORIGINAL, "Chatterbox（英语，情感强度）");
        fr(MODEL_ORIGINAL, "Chatterbox (anglais, exagération)");
        es(MODEL_ORIGINAL, "Chatterbox (inglés, exageración)");
        de(MODEL_ORIGINAL, "Chatterbox (Englisch, Exaggeration)");

        ja(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 言語)");
        zh(MODEL_MULTILINGUAL, "Chatterbox Multilingual（23 种语言）");
        fr(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 langues)");
        es(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 idiomas)");
        de(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 Sprachen)");

        ja(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "バッチの前のリクエストが読み込んだ状態にしておくモデル。別のモデルが必要な本は別のバックエンドを選びます");
        zh(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "批处理前的请求应让服务器加载的模型；需要其他模型的书请选用另一个后端");
        fr(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "Le modèle que les requêtes avant un lot doivent laisser chargé ; un livre qui en a besoin d'un autre choisit un autre moteur");
        es(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "El modelo que las solicitudes anteriores a un lote deben dejar cargado; un libro que necesite otro elige otro backend");
        de(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "Das Modell, das die Anfragen vor einem Stapel geladen lassen müssen; ein Buch, das ein anderes braucht, wählt ein anderes Backend");

        ja(THE_SERVER_IS_LOCAL, "サーバーはこのマシンまたはローカルネットワークにあります");
        zh(THE_SERVER_IS_LOCAL, "服务器在本机或我的网络中");
        fr(THE_SERVER_IS_LOCAL, "Le serveur est sur cette machine ou sur mon réseau");
        es(THE_SERVER_IS_LOCAL, "El servidor está en esta máquina o en mi red");
        de(THE_SERVER_IS_LOCAL, "Der Server steht auf diesem Rechner oder in meinem Netzwerk");

        ja(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "ローカルネットワークのアドレスを許可します。クラウドのサーバーではオフのままにします");
        zh(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "允许本地网络地址；云端服务器请保持关闭");
        fr(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Autorise les adresses du réseau local ; laissez-le désactivé pour un serveur dans le cloud");
        es(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Permite las direcciones de la red local; déjalo desactivado para un servidor en la nube");
        de(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Erlaubt die Adressen des lokalen Netzwerks; lass es für einen Server in der Cloud aus");

        ja(TIMEOUT_SECONDS, "リクエストのタイムアウト (秒)");
        zh(TIMEOUT_SECONDS, "请求超时（秒）");
        fr(TIMEOUT_SECONDS, "Délai d'attente d'une requête (s)");
        es(TIMEOUT_SECONDS, "Tiempo de espera de una solicitud (s)");
        de(TIMEOUT_SECONDS, "Zeitlimit einer Anfrage (s)");
        ja(RESTART_THE_SERVER_AFTER_PIECES, "サーバーを再起動するまでのピース数");
        zh(RESTART_THE_SERVER_AFTER_PIECES, "每隔多少片段重启服务器");
        fr(RESTART_THE_SERVER_AFTER_PIECES, "Redémarrer le serveur après (morceaux)");
        es(RESTART_THE_SERVER_AFTER_PIECES, "Reiniciar el servidor tras (fragmentos)");
        de(RESTART_THE_SERVER_AFTER_PIECES, "Server neu starten nach (Stücke)");
        ja(RESTART_THE_SERVER_HELP, "0 = 再起動しない。メモリリークするサーバー向けに、指定した数のピースごとにバッチの途中でバッチ後・バッチ前のリクエストをもう一度実行します。停止したサーバーはこの設定に関係なく再起動されます。");
        zh(RESTART_THE_SERVER_HELP, "0 = 从不。针对内存泄漏的服务器：每处理这么多片段，就在批处理中途重新执行批处理之后和之前的请求。服务器崩溃时无论如何设置都会重启。");
        fr(RESTART_THE_SERVER_HELP, "0 = jamais. Les requêtes d'après et d'avant un lot sont refaites au milieu d'un lot tous les tant de morceaux, pour un serveur qui fuit de la mémoire. Un serveur qui meurt est redémarré quel que soit ce réglage.");
        es(RESTART_THE_SERVER_HELP, "0 = nunca. Las solicitudes posteriores y anteriores a un lote se vuelven a hacer en medio de un lote cada cierto número de fragmentos, para un servidor que pierde memoria. Un servidor que se cae se reinicia digas lo que digas aquí.");
        de(RESTART_THE_SERVER_HELP, "0 = nie. Die Anfragen nach und vor einem Stapel werden mitten in einem Stapel alle so viele Stücke erneut ausgeführt, für einen Server, der Speicher verliert. Ein Server, der abstürzt, wird unabhängig davon neu gestartet.");

        ja(DEFAULTS_OF_A_SEGMENT, "セグメントの初期値");
        zh(DEFAULTS_OF_A_SEGMENT, "片段的默认值");
        fr(DEFAULTS_OF_A_SEGMENT, "Valeurs par défaut d'un segment");
        es(DEFAULTS_OF_A_SEGMENT, "Valores predeterminados de un segmento");
        de(DEFAULTS_OF_A_SEGMENT, "Standardwerte eines Segments");

        ja(TEMPERATURE, "温度");
        zh(TEMPERATURE, "温度");
        fr(TEMPERATURE, "Température");
        es(TEMPERATURE, "Temperatura");
        de(TEMPERATURE, "Temperatur");

        ja(CFG_WEIGHT, "CFG ウェイト");
        zh(CFG_WEIGHT, "CFG 权重");
        fr(CFG_WEIGHT, "Poids CFG");
        es(CFG_WEIGHT, "Peso CFG");
        de(CFG_WEIGHT, "CFG-Gewicht");

        ja(SPEED, "速度");
        zh(SPEED, "语速");
        fr(SPEED, "Vitesse");
        es(SPEED, "Velocidad");
        de(SPEED, "Geschwindigkeit");

        ja(CHUNK_SIZE, "チャンクのサイズ (文字数)");
        zh(CHUNK_SIZE, "分块大小（字符数）");
        fr(CHUNK_SIZE, "Taille des morceaux (caractères)");
        es(CHUNK_SIZE, "Tamaño del fragmento (caracteres)");
        de(CHUNK_SIZE, "Chunk-Größe (Zeichen)");

        ja(BEFORE_A_BATCH, "バッチの前");
        zh(BEFORE_A_BATCH, "批处理之前");
        fr(BEFORE_A_BATCH, "Avant un lot");
        es(BEFORE_A_BATCH, "Antes de un lote");
        de(BEFORE_A_BATCH, "Vor einem Stapel");

        ja(AFTER_A_BATCH, "バッチの後");
        zh(AFTER_A_BATCH, "批处理之后");
        fr(AFTER_A_BATCH, "Après un lot");
        es(AFTER_A_BATCH, "Después de un lote");
        de(AFTER_A_BATCH, "Nach einem Stapel");

        ja(REQUESTS_MADE_AROUND_A_BATCH, "パートのバッチの前後に順番に行うリクエスト。前: GPU を空けて音声サーバーを起動 (言語モデルのアンロード、コンテナの起動、モデルの読み込み)。後: 停止。なければ何もしません。");
        zh(REQUESTS_MADE_AROUND_A_BATCH, "在一批章节前后按顺序发出的请求。之前：释放 GPU 并启动语音服务器（卸载语言模型、启动容器、加载模型）。之后：将其停止。没有则不执行任何操作。");
        fr(REQUESTS_MADE_AROUND_A_BATCH, "Requêtes faites autour d'un lot de parties, l'une après l'autre. Avant : libérer le GPU et démarrer le serveur de synthèse vocale (décharger le modèle de langage, démarrer le conteneur, charger le modèle). Après : l'arrêter. Aucune : ne fait rien.");
        es(REQUESTS_MADE_AROUND_A_BATCH, "Solicitudes que se hacen alrededor de un lote de partes, una tras otra. Antes: liberar la GPU e iniciar el servidor de voz (descargar el modelo de lenguaje, iniciar el contenedor, cargar el modelo). Después: detenerlo. Ninguna no hace nada.");
        de(REQUESTS_MADE_AROUND_A_BATCH, "Anfragen rund um einen Stapel von Abschnitten, nacheinander. Davor: die GPU freigeben und den Sprachserver starten (das Sprachmodell entladen, den Container starten, das Modell laden). Danach: ihn stoppen. „Keine“ tut nichts.");

        ja(ADD_A_REQUEST, "リクエストを追加");
        zh(ADD_A_REQUEST, "添加请求");
        fr(ADD_A_REQUEST, "Ajouter une requête");
        es(ADD_A_REQUEST, "Añadir una solicitud");
        de(ADD_A_REQUEST, "Eine Anfrage hinzufügen");

        ja(REMOVE_THE_REQUEST, "リクエストを削除");
        zh(REMOVE_THE_REQUEST, "删除请求");
        fr(REMOVE_THE_REQUEST, "Retirer la requête");
        es(REMOVE_THE_REQUEST, "Quitar la solicitud");
        de(REMOVE_THE_REQUEST, "Die Anfrage entfernen");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");
        fr(REQUEST, "Requête");
        es(REQUEST, "Solicitud");
        de(REQUEST, "Anfrage");

        ja(WAIT_AFTERWARDS_SECONDS, "その後の待機 (秒)");
        zh(WAIT_AFTERWARDS_SECONDS, "之后等待（秒）");
        fr(WAIT_AFTERWARDS_SECONDS, "Attendre ensuite (s)");
        es(WAIT_AFTERWARDS_SECONDS, "Esperar después (s)");
        de(WAIT_AFTERWARDS_SECONDS, "Danach warten (s)");

        ja(METHOD, "メソッド");
        zh(METHOD, "方法");
        fr(METHOD, "Méthode");
        es(METHOD, "Método");
        de(METHOD, "Methode");

        ja(HEADERS_NAME_VALUE_ONE_PER_LINE, "ヘッダー (名前: 値、1 行に 1 つ)");
        zh(HEADERS_NAME_VALUE_ONE_PER_LINE, "请求头（名称: 值，每行一个）");
        fr(HEADERS_NAME_VALUE_ONE_PER_LINE, "En-têtes (Nom: valeur, un par ligne)");
        es(HEADERS_NAME_VALUE_ONE_PER_LINE, "Cabeceras (Nombre: valor, una por línea)");
        de(HEADERS_NAME_VALUE_ONE_PER_LINE, "Header (Name: Wert, einer pro Zeile)");

        ja(BODY, "本文");
        zh(BODY, "正文");
        fr(BODY, "Corps");
        es(BODY, "Cuerpo");
        de(BODY, "Body");

        ja(TEST_CONNECTION, "接続をテスト");
        zh(TEST_CONNECTION, "测试连接");
        fr(TEST_CONNECTION, "Tester la connexion");
        es(TEST_CONNECTION, "Probar la conexión");
        de(TEST_CONNECTION, "Verbindung testen");

        ja(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "サーバーは応答しました。モデルは読み込み済みです (%s)");
        zh(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "服务器已响应，模型已加载（%s）");
        fr(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "Le serveur répond, le modèle est chargé (%s)");
        es(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "El servidor responde, el modelo está cargado (%s)");
        de(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "Der Server antwortet, das Modell ist geladen (%s)");

        ja(THE_SERVER_ANSWERS_NO_MODEL, "サーバーは応答しました。モデルは読み込まれていません (バッチの前のリクエストが読み込みます)");
        zh(THE_SERVER_ANSWERS_NO_MODEL, "服务器已响应，尚未加载模型（批处理前的请求会加载）");
        fr(THE_SERVER_ANSWERS_NO_MODEL, "Le serveur répond, aucun modèle n'est chargé (les requêtes avant un lot le chargent)");
        es(THE_SERVER_ANSWERS_NO_MODEL, "El servidor responde, no hay ningún modelo cargado (las solicitudes anteriores a un lote lo cargan)");
        de(THE_SERVER_ANSWERS_NO_MODEL, "Der Server antwortet, kein Modell ist geladen (die Anfragen vor einem Stapel laden es)");

        ja(CONNECTION_FAILED, "接続に失敗しました: %s");
        zh(CONNECTION_FAILED, "连接失败：%s");
        fr(CONNECTION_FAILED, "Échec de la connexion : %s");
        es(CONNECTION_FAILED, "Falló la conexión: %s");
        de(CONNECTION_FAILED, "Verbindung fehlgeschlagen: %s");

        ja(NARRATOR_VOICE, "ナレーターの声");
        zh(NARRATOR_VOICE, "旁白声音");
        fr(NARRATOR_VOICE, "Voix du narrateur");
        es(NARRATOR_VOICE, "Voz del narrador");
        de(NARRATOR_VOICE, "Erzählerstimme");

        ja(NARRATOR_VOICE_HELP, "本にキャストがない、またはキャストにナレーターの声がない場合に使うナレーター。");
        zh(NARRATOR_VOICE_HELP, "书没有角色分配、或其中没有旁白声音时使用的旁白。");
        fr(NARRATOR_VOICE_HELP, "Le narrateur quand un livre n'a pas de distribution, ou que sa distribution n'a pas de voix de narrateur.");
        es(NARRATOR_VOICE_HELP, "El narrador cuando un libro no tiene reparto, o su reparto no tiene voz de narrador.");
        de(NARRATOR_VOICE_HELP, "Der Erzähler, wenn ein Buch keine Besetzung hat oder seine Besetzung keine Erzählerstimme hat.");

        ja(CONVERT_TO_AUDIO, "音声に変換");
        zh(CONVERT_TO_AUDIO, "转换为音频");
        fr(CONVERT_TO_AUDIO, "Convertir en audio");
        es(CONVERT_TO_AUDIO, "Convertir en audio");
        de(CONVERT_TO_AUDIO, "In Audio umwandeln");

        ja(BACKEND, "バックエンド");
        zh(BACKEND, "后端");
        fr(BACKEND, "Moteur");
        es(BACKEND, "Backend");
        de(BACKEND, "Backend");

        ja(LOAD, "読み込む");
        zh(LOAD, "加载");
        fr(LOAD, "Charger");
        es(LOAD, "Cargar");
        de(LOAD, "Laden");

        ja(LOAD_THE_PARTS_HINT, "この本には本文のあるパートが %s 個あります。読み込んで各パートの状態を確認し、パートにチェックを入れて処理を計画し、開始を押してください。");
        zh(LOAD_THE_PARTS_HINT, "本书有 %s 个含文本的章节。加载后可查看各章节的状态，勾选章节并规划操作，然后点击开始。");
        fr(LOAD_THE_PARTS_HINT, "Le livre a %s parties avec du texte. Chargez-les pour voir ce que chacune a, cochez les parties, planifiez ce qu'il faut en faire et appuyez sur Démarrer.");
        es(LOAD_THE_PARTS_HINT, "El libro tiene %s partes con texto. Cárgalas para ver qué tiene cada una, marca las partes, planifica qué hacer con ellas y pulsa Iniciar.");
        de(LOAD_THE_PARTS_HINT, "Das Buch hat %s Abschnitte mit Text. Lade sie, um zu sehen, was jeder hat, hake die Abschnitte an, plane, was mit ihnen geschehen soll, und klicke auf „Starten“.");

        ja(PART_NOT_GENERATED, "未生成");
        zh(PART_NOT_GENERATED, "未生成");
        fr(PART_NOT_GENERATED, "Non générée");
        es(PART_NOT_GENERATED, "No generada");
        de(PART_NOT_GENERATED, "Nicht generiert");

        ja(PART_LABELLED, "ラベル付き");
        zh(PART_LABELLED, "已标注");
        fr(PART_LABELLED, "Étiquetée");
        es(PART_LABELLED, "Etiquetada");
        de(PART_LABELLED, "Etikettiert");

        ja(PART_GENERATED, "生成済み");
        zh(PART_GENERATED, "已生成");
        fr(PART_GENERATED, "Générée");
        es(PART_GENERATED, "Generada");
        de(PART_GENERATED, "Generiert");

        ja(PART_GENERATED_TEXT_CHANGED, "生成済み(本文が変更されました)");
        zh(PART_GENERATED_TEXT_CHANGED, "已生成（文本已更改）");
        fr(PART_GENERATED_TEXT_CHANGED, "Générée (le texte a changé)");
        es(PART_GENERATED_TEXT_CHANGED, "Generada (el texto cambió)");
        de(PART_GENERATED_TEXT_CHANGED, "Generiert (der Text hat sich geändert)");

        ja(PART_FINAL, "確定");
        zh(PART_FINAL, "最终");
        fr(PART_FINAL, "définitive");
        es(PART_FINAL, "definitiva");
        de(PART_FINAL, "endgültig");

        ja(PLAN, "計画");
        zh(PLAN, "计划");
        fr(PLAN, "Plan");
        es(PLAN, "Plan");
        de(PLAN, "Plan");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");
        fr(GENERATE, "Générer");
        es(GENERATE, "Generar");
        de(GENERATE, "Generieren");

        ja(REGENERATE, "再生成");
        zh(REGENERATE, "重新生成");
        fr(REGENERATE, "Régénérer");
        es(REGENERATE, "Regenerar");
        de(REGENERATE, "Neu generieren");

        ja(DELETE_AUDIO, "音声を削除");
        zh(DELETE_AUDIO, "删除音频");
        fr(DELETE_AUDIO, "Supprimer l'audio");
        es(DELETE_AUDIO, "Eliminar audio");
        de(DELETE_AUDIO, "Audio löschen");

        ja(REMOVE_LABELS, "ラベルを削除");
        zh(REMOVE_LABELS, "移除标注");
        fr(REMOVE_LABELS, "Retirer les étiquettes");
        es(REMOVE_LABELS, "Quitar etiquetas");
        de(REMOVE_LABELS, "Etiketten entfernen");

        ja(CLEAR_THE_PLAN, "計画を消去");
        zh(CLEAR_THE_PLAN, "清除计划");
        fr(CLEAR_THE_PLAN, "Vider le plan");
        es(CLEAR_THE_PLAN, "Borrar el plan");
        de(CLEAR_THE_PLAN, "Den Plan leeren");

        ja(PLAY, "再生");
        zh(PLAY, "播放");
        fr(PLAY, "Lire");
        es(PLAY, "Reproducir");
        de(PLAY, "Abspielen");

        ja(SELECT_THE_PARTS_FIRST, "先にパートにチェックを入れてください");
        zh(SELECT_THE_PARTS_FIRST, "请先勾选章节");
        fr(SELECT_THE_PARTS_FIRST, "Cochez d'abord les parties");
        es(SELECT_THE_PARTS_FIRST, "Marca primero las partes");
        de(SELECT_THE_PARTS_FIRST, "Hake zuerst die Abschnitte an");

        ja(NOTHING_IS_PLANNED, "計画がありません。パートにチェックを入れ、生成・再生成・音声を削除・ラベルを削除のいずれかを選んでください");
        zh(NOTHING_IS_PLANNED, "没有计划：请勾选章节并选择生成、重新生成、删除音频或移除标注");
        fr(NOTHING_IS_PLANNED, "Rien n'est planifié : cochez des parties et choisissez Générer, Régénérer, Supprimer l'audio ou Retirer les étiquettes");
        es(NOTHING_IS_PLANNED, "No hay nada planificado: marca partes y elige Generar, Regenerar, Eliminar audio o Quitar etiquetas");
        de(NOTHING_IS_PLANNED, "Nichts ist geplant: Hake Abschnitte an und wähle „Generieren“, „Neu generieren“, „Audio löschen“ oder „Etiketten entfernen“");

        ja(APPLY_THE_PLAN_QUESTION, "%s パートの音声を削除し、%s パートのラベルを削除します。元に戻せません。計画を実行しますか?");
        zh(APPLY_THE_PLAN_QUESTION, "将删除 %s 个章节的音频并移除 %s 个章节的标注，此操作无法撤销。执行计划吗？");
        fr(APPLY_THE_PLAN_QUESTION, "L'audio de %s parties sera supprimé et les étiquettes de %s parties retirées. C'est irréversible. Exécuter le plan ?");
        es(APPLY_THE_PLAN_QUESTION, "Se eliminará el audio de %s partes y se quitarán las etiquetas de %s partes. No se puede deshacer. ¿Ejecutar el plan?");
        de(APPLY_THE_PLAN_QUESTION, "Das Audio von %s Abschnitten wird gelöscht und die Etiketten von %s Abschnitten werden entfernt. Das kann nicht rückgängig gemacht werden. Den Plan ausführen?");

        ja(THE_PLAN_WAS_CARRIED_OUT, "%s パートの音声を削除し、%s パートのラベルを削除しました");
        zh(THE_PLAN_WAS_CARRIED_OUT, "已删除 %s 个章节的音频，已移除 %s 个章节的标注");
        fr(THE_PLAN_WAS_CARRIED_OUT, "Audio supprimé pour %s parties, étiquettes retirées pour %s parties");
        es(THE_PLAN_WAS_CARRIED_OUT, "Audio eliminado en %s partes, etiquetas quitadas en %s partes");
        de(THE_PLAN_WAS_CARRIED_OUT, "Audio gelöscht für %s Abschnitte, Etiketten entfernt für %s Abschnitte");

        ja(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s パートはキューで待機中または実行中のため対象外です");
        zh(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s 个章节正在队列中等待或运行，已被跳过");
        fr(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s parties sont en attente ou en cours dans la file et ont été omises");
        es(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s partes están esperando o en ejecución en la cola y se omitieron");
        de(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s Abschnitte warten oder laufen in der Warteschlange und wurden ausgelassen");

        ja(REASONING, "思考");
        zh(REASONING, "推理");
        fr(REASONING, "Raisonnement");
        es(REASONING, "Razonamiento");
        de(REASONING, "Reasoning");

        ja(THE_ANSWER_OF_THE_MODEL, "モデルの回答");
        zh(THE_ANSWER_OF_THE_MODEL, "模型的回答");
        fr(THE_ANSWER_OF_THE_MODEL, "Réponse du modèle");
        es(THE_ANSWER_OF_THE_MODEL, "Respuesta del modelo");
        de(THE_ANSWER_OF_THE_MODEL, "Antwort des Modells");

        ja(WHAT_THE_MODEL_SAID, "モデルの出力: %s");
        zh(WHAT_THE_MODEL_SAID, "模型的输出：%s");
        fr(WHAT_THE_MODEL_SAID, "Ce que le modèle a dit : %s");
        es(WHAT_THE_MODEL_SAID, "Lo que dijo el modelo: %s");
        de(WHAT_THE_MODEL_SAID, "Was das Modell gesagt hat: %s");

        ja(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "クリックして、このパートのラベル付け中にモデルが出力した内容を表示");
        zh(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "点击查看模型在标注此章节时输出的内容");
        fr(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Cliquez pour lire ce que le modèle a dit pendant l'étiquetage de cette partie");
        es(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Haz clic para leer lo que dijo el modelo mientras etiquetaba esta parte");
        de(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Klicken, um zu lesen, was das Modell beim Etikettieren dieses Abschnitts gesagt hat");

        ja(START, "開始");
        zh(START, "开始");
        fr(START, "Démarrer");
        es(START, "Iniciar");
        de(START, "Starten");

        ja(STOP_ALL, "すべて停止");
        zh(STOP_ALL, "全部停止");
        fr(STOP_ALL, "Tout arrêter");
        es(STOP_ALL, "Detener todo");
        de(STOP_ALL, "Alle stoppen");

        ja(CLEAR_FINISHED, "完了したものを消去");
        zh(CLEAR_FINISHED, "清除已完成");
        fr(CLEAR_FINISHED, "Effacer les terminées");
        es(CLEAR_FINISHED, "Quitar las terminadas");
        de(CLEAR_FINISHED, "Abgeschlossene entfernen");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");

        ja(PART, "パート");
        zh(PART, "章节");
        fr(PART, "Partie");
        es(PART, "Parte");
        de(PART, "Abschnitt");

        ja(STATE, "状態");
        zh(STATE, "状态");
        fr(STATE, "État");
        es(STATE, "Estado");
        de(STATE, "Zustand");

        ja(MESSAGE, "メッセージ");
        zh(MESSAGE, "消息");
        fr(MESSAGE, "Message");
        es(MESSAGE, "Mensaje");
        de(MESSAGE, "Nachricht");

        ja(STATE_WAITING, "待機中");
        zh(STATE_WAITING, "等待中");
        fr(STATE_WAITING, "En attente");
        es(STATE_WAITING, "En espera");
        de(STATE_WAITING, "Wartet");

        ja(STATE_RUNNING, "実行中");
        zh(STATE_RUNNING, "进行中");
        fr(STATE_RUNNING, "En cours");
        es(STATE_RUNNING, "En ejecución");
        de(STATE_RUNNING, "Läuft");

        ja(STATE_DONE, "完了");
        zh(STATE_DONE, "完成");
        fr(STATE_DONE, "Terminée");
        es(STATE_DONE, "Hecha");
        de(STATE_DONE, "Fertig");

        ja(STATE_FAILED, "失敗");
        zh(STATE_FAILED, "失败");
        fr(STATE_FAILED, "Échouée");
        es(STATE_FAILED, "Fallida");
        de(STATE_FAILED, "Fehlgeschlagen");

        ja(STATE_CANCELLED, "中止");
        zh(STATE_CANCELLED, "已取消");
        fr(STATE_CANCELLED, "Annulée");
        es(STATE_CANCELLED, "Cancelada");
        de(STATE_CANCELLED, "Abgebrochen");

        ja(DOWNLOAD, "ダウンロード");
        zh(DOWNLOAD, "下载");
        fr(DOWNLOAD, "Télécharger");
        es(DOWNLOAD, "Descargar");
        de(DOWNLOAD, "Herunterladen");

        ja(RETRY, "再試行");
        zh(RETRY, "重试");
        fr(RETRY, "Réessayer");
        es(RETRY, "Reintentar");
        de(RETRY, "Wiederholen");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(NO_BACKEND_YET, "バックエンドがありません。設定 → 拡張機能の設定 → オーディオブックの設定で追加してください。");
        zh(NO_BACKEND_YET, "尚无后端。请在 设置 → 扩展设置 → 有声书设置 中添加。");
        fr(NO_BACKEND_YET, "Pas encore de moteur. Ajoutez-en un dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(NO_BACKEND_YET, "Todavía no hay ningún backend. Añade uno en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(NO_BACKEND_YET, "Noch kein Backend. Füge eines unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen hinzu.");

        ja(THERE_IS_NO_NARRATOR_VOICE, "ナレーターの声がありません。キャスト、または 設定 → 拡張機能の設定 → オーディオブックの設定 で指定してください。");
        zh(THERE_IS_NO_NARRATOR_VOICE, "没有旁白声音。请在角色分配中，或在 设置 → 扩展设置 → 有声书设置 中选择。");
        fr(THERE_IS_NO_NARRATOR_VOICE, "Il n'y a pas de voix de narrateur. Choisissez-la dans la distribution, ou dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(THERE_IS_NO_NARRATOR_VOICE, "No hay voz de narrador. Elígela en el reparto, o en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(THERE_IS_NO_NARRATOR_VOICE, "Es gibt keine Erzählerstimme. Wähle sie in der Besetzung oder unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen.");

        ja(THE_BOOK_HAS_NO_TEXT, "本文のあるパートがありません");
        zh(THE_BOOK_HAS_NO_TEXT, "书中没有含文本的章节");
        fr(THE_BOOK_HAS_NO_TEXT, "Le livre n'a pas de parties avec du texte");
        es(THE_BOOK_HAS_NO_TEXT, "El libro no tiene partes con texto");
        de(THE_BOOK_HAS_NO_TEXT, "Das Buch hat keine Abschnitte mit Text");

        ja(THE_PARTS_ARE_IN_THE_QUEUE, "%s 個のパートをキューに入れました");
        zh(THE_PARTS_ARE_IN_THE_QUEUE, "已将 %s 个章节加入队列");
        fr(THE_PARTS_ARE_IN_THE_QUEUE, "%s parties sont dans la file");
        es(THE_PARTS_ARE_IN_THE_QUEUE, "%s partes están en la cola");
        de(THE_PARTS_ARE_IN_THE_QUEUE, "%s Abschnitte sind in der Warteschlange");

        ja(THE_CONVERSION_COULD_NOT_BE_STARTED, "変換を開始できませんでした: %s");
        zh(THE_CONVERSION_COULD_NOT_BE_STARTED, "无法开始转换：%s");
        fr(THE_CONVERSION_COULD_NOT_BE_STARTED, "La conversion n'a pas pu démarrer : %s");
        es(THE_CONVERSION_COULD_NOT_BE_STARTED, "No se pudo iniciar la conversión: %s");
        de(THE_CONVERSION_COULD_NOT_BE_STARTED, "Die Umwandlung konnte nicht gestartet werden: %s");

        ja(LISTEN_TO_THE_AUDIOBOOK, "オーディオブックを聴く");
        zh(LISTEN_TO_THE_AUDIOBOOK, "收听有声书");
        fr(LISTEN_TO_THE_AUDIOBOOK, "Écouter le livre audio");
        es(LISTEN_TO_THE_AUDIOBOOK, "Escuchar el audiolibro");
        de(LISTEN_TO_THE_AUDIOBOOK, "Das Hörbuch anhören");

        ja(PAUSE_LISTENING, "一時停止");
        zh(PAUSE_LISTENING, "暂停");
        fr(PAUSE_LISTENING, "Pause");
        es(PAUSE_LISTENING, "Pausar");
        de(PAUSE_LISTENING, "Pause");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");

        ja(THIS_PART_HAS_NO_AUDIO, "このパートにはまだ音声がありません");
        zh(THIS_PART_HAS_NO_AUDIO, "此章节还没有音频");
        fr(THIS_PART_HAS_NO_AUDIO, "Cette partie n'a pas (encore) d'audio");
        es(THIS_PART_HAS_NO_AUDIO, "Esta parte (todavía) no tiene audio");
        de(THIS_PART_HAS_NO_AUDIO, "Dieser Abschnitt hat (noch) kein Audio");

        ja(THE_AUDIO_COULD_NOT_BE_LOADED, "音声を読み込めませんでした");
        zh(THE_AUDIO_COULD_NOT_BE_LOADED, "无法加载音频");
        fr(THE_AUDIO_COULD_NOT_BE_LOADED, "L'audio n'a pas pu être chargé");
        es(THE_AUDIO_COULD_NOT_BE_LOADED, "No se pudo cargar el audio");
        de(THE_AUDIO_COULD_NOT_BE_LOADED, "Das Audio konnte nicht geladen werden");

        ja(LABEL_WITH_A_LANGUAGE_MODEL, "言語モデルでパートにラベルを付ける (誰が話すか、話し方、効果音タグ)");
        zh(LABEL_WITH_A_LANGUAGE_MODEL, "用语言模型为章节加标注（谁在说话、语气、声音标签）");
        fr(LABEL_WITH_A_LANGUAGE_MODEL, "Étiqueter les parties avec un modèle de langage (qui parle, comment, balises sonores)");
        es(LABEL_WITH_A_LANGUAGE_MODEL, "Etiquetar las partes con un modelo de lenguaje (quién habla, cómo, etiquetas de sonido)");
        de(LABEL_WITH_A_LANGUAGE_MODEL, "Die Abschnitte mit einem Sprachmodell etikettieren (wer spricht, wie, Sound-Tags)");

        ja(THE_MODEL_THAT_LABELS, "ラベルを付けるモデル");
        zh(THE_MODEL_THAT_LABELS, "负责标注的模型");
        fr(THE_MODEL_THAT_LABELS, "Le modèle qui étiquette");
        es(THE_MODEL_THAT_LABELS, "El modelo que etiqueta");
        de(THE_MODEL_THAT_LABELS, "Das Modell, das etikettiert");

        ja(THE_PARTS_ARE_LABELLED_FIRST, "先にパートにラベルを付け (モデルが読み込まれている必要があります)、その後で読み上げます。バッチ前のリクエストが音声のためにモデルをアンロードします。");
        zh(THE_PARTS_ARE_LABELLED_FIRST, "先为章节加标注（模型必须已加载），再朗读：批处理前的请求会为语音卸载该模型。");
        fr(THE_PARTS_ARE_LABELLED_FIRST, "Les parties sont d'abord étiquetées (le modèle doit être chargé), puis lues : les requêtes avant le lot déchargent le modèle pour la synthèse vocale.");
        es(THE_PARTS_ARE_LABELLED_FIRST, "Las partes se etiquetan primero (el modelo debe estar cargado) y luego se leen en voz alta: las solicitudes anteriores al lote descargan el modelo para la voz.");
        de(THE_PARTS_ARE_LABELLED_FIRST, "Die Abschnitte werden zuerst etikettiert (das Modell muss geladen sein), dann gesprochen: Die Anfragen vor dem Stapel entladen das Modell für die Sprache.");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");

        ja(REMOVE, "削除");
        zh(REMOVE, "移除");
        fr(REMOVE, "Retirer");
        es(REMOVE, "Quitar");
        de(REMOVE, "Entfernen");

        ja(CAST, "キャスト");
        zh(CAST, "角色分配");
        fr(CAST, "Distribution");
        es(CAST, "Reparto");
        de(CAST, "Besetzung");





        ja(CHARACTERS, "登場人物");
        zh(CHARACTERS, "人物");
        fr(CHARACTERS, "Personnages");
        es(CHARACTERS, "Personajes");
        de(CHARACTERS, "Figuren");

        ja(NARRATOR, "ナレーター");
        zh(NARRATOR, "旁白");
        fr(NARRATOR, "Narrateur");
        es(NARRATOR, "Narrador");
        de(NARRATOR, "Erzähler");

        ja(OTHER_SPEAKERS, "その他の話者");
        zh(OTHER_SPEAKERS, "其他说话者");
        fr(OTHER_SPEAKERS, "Autres locuteurs");
        es(OTHER_SPEAKERS, "Otros hablantes");
        de(OTHER_SPEAKERS, "Andere Sprecher");

        ja(VOICE, "声");
        zh(VOICE, "声音");
        fr(VOICE, "Voix");
        es(VOICE, "Voz");
        de(VOICE, "Stimme");

        ja(WHEN, "場合");
        zh(WHEN, "场合");
        fr(WHEN, "Quand");
        es(WHEN, "Cuándo");
        de(WHEN, "Wann");

        ja(MOOD_NAME, "感情: %s");
        zh(MOOD_NAME, "语气：%s");
        fr(MOOD_NAME, "Humeur : %s");
        es(MOOD_NAME, "Estado de ánimo: %s");
        de(MOOD_NAME, "Stimmung: %s");

        ja(LANGUAGE_NAME, "言語: %s");
        zh(LANGUAGE_NAME, "语言：%s");
        fr(LANGUAGE_NAME, "Langue : %s");
        es(LANGUAGE_NAME, "Idioma: %s");
        de(LANGUAGE_NAME, "Sprache: %s");

        ja(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "感情や言語ごとの声を追加");
        zh(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "为语气或语言添加声音");
        fr(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Une voix pour une humeur ou une langue");
        es(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Una voz para un estado de ánimo o un idioma");
        de(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Eine Stimme für eine Stimmung oder eine Sprache");

        ja(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "クリップ、または声のファイル (Henry.wav)");
        zh(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "参考片段，或声音文件 (Henry.wav)");
        fr(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Un extrait, ou un fichier de voix (Henry.wav)");
        es(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Un clip, o un archivo de voz (Henry.wav)");
        de(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Ein Clip oder eine Stimmdatei (Henry.wav)");

        ja(ADD_A_CLIP, "クリップを追加");
        zh(ADD_A_CLIP, "添加片段");
        fr(ADD_A_CLIP, "Ajouter un extrait");
        es(ADD_A_CLIP, "Añadir un clip");
        de(ADD_A_CLIP, "Einen Clip hinzufügen");

        ja(THE_CLIP_COULD_NOT_BE_ADDED, "クリップを追加できませんでした: %s");
        zh(THE_CLIP_COULD_NOT_BE_ADDED, "无法添加片段：%s");
        fr(THE_CLIP_COULD_NOT_BE_ADDED, "L'extrait n'a pas pu être ajouté : %s");
        es(THE_CLIP_COULD_NOT_BE_ADDED, "No se pudo añadir el clip: %s");
        de(THE_CLIP_COULD_NOT_BE_ADDED, "Der Clip konnte nicht hinzugefügt werden: %s");

        ja(ADD_A_CHARACTER, "登場人物を追加");
        zh(ADD_A_CHARACTER, "添加人物");
        fr(ADD_A_CHARACTER, "Ajouter un personnage");
        es(ADD_A_CHARACTER, "Añadir un personaje");
        de(ADD_A_CHARACTER, "Eine Figur hinzufügen");

        ja(ADD_THE_CHARACTERS_OF_THE_BOOK, "本の登場人物を追加");
        zh(ADD_THE_CHARACTERS_OF_THE_BOOK, "添加本书的人物");
        fr(ADD_THE_CHARACTERS_OF_THE_BOOK, "Ajouter les personnages du livre");
        es(ADD_THE_CHARACTERS_OF_THE_BOOK, "Añadir los personajes del libro");
        de(ADD_THE_CHARACTERS_OF_THE_BOOK, "Die Figuren des Buchs hinzufügen");

        ja(CHARACTERS_ADDED, "追加した登場人物: %s");
        zh(CHARACTERS_ADDED, "已添加人物：%s");
        fr(CHARACTERS_ADDED, "Personnages ajoutés : %s");
        es(CHARACTERS_ADDED, "Personajes añadidos: %s");
        de(CHARACTERS_ADDED, "Figuren hinzugefügt: %s");

        ja(NEW_CHARACTER, "新しい登場人物");
        zh(NEW_CHARACTER, "新人物");
        fr(NEW_CHARACTER, "Nouveau personnage");
        es(NEW_CHARACTER, "Personaje nuevo");
        de(NEW_CHARACTER, "Neue Figur");

        ja(OTHER_NAMES, "別名");
        zh(OTHER_NAMES, "别名");
        fr(OTHER_NAMES, "Autres noms");
        es(OTHER_NAMES, "Otros nombres");
        de(OTHER_NAMES, "Andere Namen");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");
        fr(SEPARATED_BY_COMMAS, "Séparés par des virgules");
        es(SEPARATED_BY_COMMAS, "Separados por comas");
        de(SEPARATED_BY_COMMAS, "Durch Kommas getrennt");



        ja(THE_CAST_COULD_NOT_BE_STORED, "キャストを保存できませんでした: %s");
        zh(THE_CAST_COULD_NOT_BE_STORED, "无法保存角色分配：%s");
        fr(THE_CAST_COULD_NOT_BE_STORED, "La distribution n'a pas pu être enregistrée : %s");
        es(THE_CAST_COULD_NOT_BE_STORED, "No se pudo guardar el reparto: %s");
        de(THE_CAST_COULD_NOT_BE_STORED, "Die Besetzung konnte nicht gespeichert werden: %s");

        ja(THE_CAST_IS_STORED, "キャストを保存しました");
        zh(THE_CAST_IS_STORED, "角色分配已保存");
        fr(THE_CAST_IS_STORED, "La distribution est enregistrée");
        es(THE_CAST_IS_STORED, "El reparto se ha guardado");
        de(THE_CAST_IS_STORED, "Die Besetzung ist gespeichert");







        ja(ACTOR, "声優");
        zh(ACTOR, "配役の声");
        fr(ACTOR, "Comédien");
        es(ACTOR, "Actor");
        de(ACTOR, "Sprecher");

        ja(ACTORS, "声優");
        zh(ACTORS, "配役声音");
        fr(ACTORS, "Comédiens");
        es(ACTORS, "Actores");
        de(ACTORS, "Sprecher");

        ja(NEW_ACTOR, "新しい声優");
        zh(NEW_ACTOR, "新建声音");
        fr(NEW_ACTOR, "Nouveau comédien");
        es(NEW_ACTOR, "Actor nuevo");
        de(NEW_ACTOR, "Neuer Sprecher");

        ja(EDIT_THE_ACTOR, "声優を編集");
        zh(EDIT_THE_ACTOR, "编辑声音");
        fr(EDIT_THE_ACTOR, "Modifier le comédien");
        es(EDIT_THE_ACTOR, "Editar el actor");
        de(EDIT_THE_ACTOR, "Den Sprecher bearbeiten");

        ja(DELETE_THE_ACTOR, "声優を削除");
        zh(DELETE_THE_ACTOR, "删除声音");
        fr(DELETE_THE_ACTOR, "Supprimer le comédien");
        es(DELETE_THE_ACTOR, "Eliminar el actor");
        de(DELETE_THE_ACTOR, "Den Sprecher löschen");

        ja(THE_ACTOR_IS_DELETED, "声優「%s」を削除します。これを指定しているキャストでは声がなくなり、その他の話者の声で読まれます。クリップはリソースに残ります。");
        zh(THE_ACTOR_IS_DELETED, "将删除声音“%s”。指定它的角色分配将不再有对应声音，改由其他说话者的声音朗读。片段仍保留在你的资源中。");
        fr(THE_ACTOR_IS_DELETED, "Le comédien « %s » est supprimé. Une distribution qui le nomme n'a alors plus de voix pour lui, et c'est la voix des autres locuteurs qui lit. Les extraits restent parmi vos ressources.");
        es(THE_ACTOR_IS_DELETED, "El actor \"%s\" se ha eliminado. Un reparto que lo nombre se queda entonces sin voz para él, y lee la voz de los demás hablantes. Los clips se quedan entre tus recursos.");
        de(THE_ACTOR_IS_DELETED, "Der Sprecher „%s“ ist gelöscht. Eine Besetzung, die ihn nennt, hat dann keine Stimme für ihn, und die Stimme der anderen Sprecher liest. Die Clips bleiben unter deinen Ressourcen.");

        ja(THE_ACTOR_COULD_NOT_BE_STORED, "声優を保存できませんでした: %s");
        zh(THE_ACTOR_COULD_NOT_BE_STORED, "无法保存声音：%s");
        fr(THE_ACTOR_COULD_NOT_BE_STORED, "Le comédien n'a pas pu être enregistré : %s");
        es(THE_ACTOR_COULD_NOT_BE_STORED, "No se pudo guardar el actor: %s");
        de(THE_ACTOR_COULD_NOT_BE_STORED, "Der Sprecher konnte nicht gespeichert werden: %s");

        ja(NOT_SET, "未設定");
        zh(NOT_SET, "未设置");
        fr(NOT_SET, "Non défini");
        es(NOT_SET, "Sin definir");
        de(NOT_SET, "Nicht festgelegt");

        ja(ACTORS_HELP, "声優はあなたの声です。参照クリップまたは音声サーバーの声のファイルで、感情や言語ごとの声も持てます。どの本でも、ナレーターと登場人物に声優を割り当てられます (オーディオに変換 → キャスト)。");
        zh(ACTORS_HELP, "声音（演员）属于你：参考片段或语音服务器的声音文件，还可为语气或语言设置各自的声音。任何一本书都可以为旁白和人物指定声音（转为音频 → 角色分配）。");
        fr(ACTORS_HELP, "Un comédien est l'une de vos voix : un extrait de référence ou un fichier de voix du serveur de synthèse vocale, avec ses propres voix pour une humeur ou une langue. N'importe quel livre peut attribuer un comédien à son narrateur et à ses personnages (Convertir en audio → Distribution).");
        es(ACTORS_HELP, "Un actor es una voz tuya: un clip de referencia o un archivo de voz del servidor de voz, con voces propias para un estado de ánimo o un idioma. Cualquier libro puede dar un actor a su narrador y a sus personajes (Convertir en audio → Reparto).");
        de(ACTORS_HELP, "Ein Sprecher ist eine Stimme von dir: ein Referenzclip oder eine Stimmdatei des Sprachservers, mit eigenen Stimmen für eine Stimmung oder eine Sprache. Jedes Buch kann seinem Erzähler und seinen Figuren einen Sprecher geben (In Audio umwandeln → Besetzung).");

        ja(THE_CAST_HELP, "この本で誰が読むか: ナレーターと各登場人物に声優を割り当てます。声優はあなたの声です (設定 → 拡張機能の設定 → オーディオブックの設定)。ここで新しく作ることもできます。キャストは本と一緒に保存されます。");
        zh(THE_CAST_HELP, "这本书由谁朗读：为旁白和每个人物指定一个声音。声音是你自己的（设置 → 扩展设置 → 有声书设置），也可在此新建。角色分配随书保存。");
        fr(THE_CAST_HELP, "Qui lit dans ce livre : un comédien pour le narrateur et un pour chaque personnage. Les comédiens sont vos voix (Paramètres → Paramètres des extensions → Paramètres du livre audio) ; un nouveau peut être créé ici. La distribution est gardée avec le livre.");
        es(THE_CAST_HELP, "Quién lee en este libro: un actor para el narrador y uno para cada personaje. Los actores son tus voces (Ajustes → Ajustes de extensiones → Ajustes del audiolibro); aquí se puede crear uno nuevo. El reparto se guarda con el libro.");
        de(THE_CAST_HELP, "Wer in diesem Buch liest: ein Sprecher für den Erzähler und einer für jede Figur. Die Sprecher sind deine Stimmen (Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen); hier kann ein neuer erstellt werden. Die Besetzung wird mit dem Buch aufbewahrt.");

        ja(THE_FALLBACK_HELP, "モデルがキャストにない話者を示した場合に読む声優。未設定ならナレーターが読みます。声優のない登場人物も同様です。");
        zh(THE_FALLBACK_HELP, "当模型给出角色分配中没有的说话者时朗读的声音。未设置则由旁白朗读。没有指定声音的人物也一样。");
        fr(THE_FALLBACK_HELP, "Le comédien qui lit quand le modèle nomme un locuteur absent de la distribution. Non défini : le narrateur lit. Un personnage sans comédien est lu de la même façon.");
        es(THE_FALLBACK_HELP, "El actor que lee cuando el modelo nombra a un hablante que el reparto no tiene. Sin definir: lee el narrador. Un personaje sin actor se lee de la misma manera.");
        de(THE_FALLBACK_HELP, "Der Sprecher, der liest, wenn das Modell einen Sprecher nennt, den die Besetzung nicht hat. Nicht festgelegt: Der Erzähler liest. Eine Figur ohne Sprecher wird genauso gelesen.");

        ja(THE_ACTOR_HELP, "声は参照クリップ (音声リソース、または新規)、または音声サーバーの声のファイルです。感情や言語ごとに別の声を指定できます (ささやき、叫び、日本語を話すクリップ)。言語は感情より優先され、指定がなければ上の声で読みます。");
        zh(THE_ACTOR_HELP, "声音是参考片段（你的音频资源之一，或新上传的）或语音服务器自带声音的文件。语气或语言可以有各自的声音：耳语、喊叫、会说日语的片段；语言优先于语气，没有对应声音时用上面的声音朗读。");
        fr(THE_ACTOR_HELP, "La voix est un extrait de référence (l'une de vos ressources audio, ou un nouvel extrait) ou le fichier d'une voix du serveur de synthèse vocale. Une humeur ou une langue peut avoir sa propre voix : un chuchotement, un cri, un extrait qui parle japonais ; la langue l'emporte sur l'humeur, et sans voix pour elles, c'est la voix ci-dessus qui lit.");
        es(THE_ACTOR_HELP, "La voz es un clip de referencia (uno de tus recursos de audio, o uno nuevo) o el archivo de una voz del servidor de voz. Un estado de ánimo o un idioma puede tener una voz propia: un susurro, un grito, un clip que habla japonés; el idioma tiene prioridad sobre el estado de ánimo, y sin una voz para ellos lee la voz de arriba.");
        de(THE_ACTOR_HELP, "Die Stimme ist ein Referenzclip (eine deiner Audio-Ressourcen oder ein neuer) oder die Datei einer Stimme des Sprachservers. Eine Stimmung oder eine Sprache kann eine eigene Stimme haben: ein Flüstern, ein Schrei, ein Clip, der Japanisch spricht; die Sprache hat Vorrang vor der Stimmung, und ohne eigene Stimme für sie liest die obige Stimme.");

        ja(AUDIOBOOK_STUDIO, "オーディオブックスタジオ");
        zh(AUDIOBOOK_STUDIO, "有声书工作室");
        fr(AUDIOBOOK_STUDIO, "Studio du livre audio");
        es(AUDIOBOOK_STUDIO, "Estudio de audiolibros");
        de(AUDIOBOOK_STUDIO, "Hörbuch-Studio");

        ja(ANNOTATE, "注釈を付ける");
        zh(ANNOTATE, "标注");
        fr(ANNOTATE, "Annoter");
        es(ANNOTATE, "Anotar");
        de(ANNOTATE, "Annotieren");

        ja(ANNOTATING, "パートに注釈を付けています ...");
        zh(ANNOTATING, "正在标注该章节 ...");
        fr(ANNOTATING, "Annotation de la partie ...");
        es(ANNOTATING, "Anotando la parte ...");
        de(ANNOTATING, "Der Abschnitt wird annotiert ...");

        ja(A_FINAL_PART_IS_NOT_ANNOTATED, "最終版のパートは変更されません。先に状態を「確認済み」または「下書き」にしてください。");
        zh(A_FINAL_PART_IS_NOT_ANNOTATED, "定稿的章节不会被改动：请先把状态改为已审阅或草稿。");
        fr(A_FINAL_PART_IS_NOT_ANNOTATED, "Une partie définitive n'est pas touchée : mettez d'abord son statut à relue ou brouillon.");
        es(A_FINAL_PART_IS_NOT_ANNOTATED, "Una parte definitiva se deja como está: cambia primero su estado a revisada o borrador.");
        de(A_FINAL_PART_IS_NOT_ANNOTATED, "Ein endgültiger Abschnitt bleibt unangetastet: Setze zuerst seinen Status auf „Geprüft“ oder „Entwurf“.");

        ja(MAKE_THE_PART, "このスクリプトでパートを作成");
        zh(MAKE_THE_PART, "按此脚本生成该章节");
        fr(MAKE_THE_PART, "Fabriquer la partie à partir de ce script");
        es(MAKE_THE_PART, "Crear la parte a partir de este guion");
        de(MAKE_THE_PART, "Den Abschnitt aus diesem Skript erstellen");

        ja(MAKING_THE_PART, "パートを作成しています ...");
        zh(MAKING_THE_PART, "正在生成该章节 ...");
        fr(MAKING_THE_PART, "Fabrication de la partie ...");
        es(MAKING_THE_PART, "Creando la parte ...");
        de(MAKING_THE_PART, "Der Abschnitt wird erstellt ...");

        ja(HEAR_THE_PART, "パートを聞く");
        zh(HEAR_THE_PART, "试听该章节");
        fr(HEAR_THE_PART, "Écouter la partie");
        es(HEAR_THE_PART, "Escuchar la parte");
        de(HEAR_THE_PART, "Den Abschnitt anhören");

        ja(THE_PART_HAS_NO_AUDIO_YET, "このパートにはまだ音声がありません");
        zh(THE_PART_HAS_NO_AUDIO_YET, "该章节还没有音频");
        fr(THE_PART_HAS_NO_AUDIO_YET, "La partie n'a pas encore d'audio");
        es(THE_PART_HAS_NO_AUDIO_YET, "La parte todavía no tiene audio");
        de(THE_PART_HAS_NO_AUDIO_YET, "Der Abschnitt hat noch kein Audio");

        ja(THIS_PART_HAS_NO_SCRIPT_YET, "このパートにはまだスクリプトがありません。注釈を付けてください。");
        zh(THIS_PART_HAS_NO_SCRIPT_YET, "该章节还没有脚本：请先标注。");
        fr(THIS_PART_HAS_NO_SCRIPT_YET, "Cette partie n'a pas encore de script : annotez-la.");
        es(THIS_PART_HAS_NO_SCRIPT_YET, "Esta parte todavía no tiene guion: anótala.");
        de(THIS_PART_HAS_NO_SCRIPT_YET, "Dieser Abschnitt hat noch kein Skript: Annotiere ihn.");

        ja(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "このスクリプトの作成後にパートの本文が変わりました。再度注釈を付けてください (変更したセグメントは保持されます)。");
        zh(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "自该脚本生成后章节文本已更改：请重新标注（你改过的片段会保留）。");
        fr(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "Le texte de la partie a changé depuis la création de ce script : annotez-la à nouveau (les segments que vous avez modifiés sont gardés).");
        es(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "El texto de la parte cambió desde que se hizo este guion: vuelve a anotarla (los segmentos que cambiaste se conservan).");
        de(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "Der Text des Abschnitts hat sich geändert, seit dieses Skript erstellt wurde: Annotiere ihn erneut (die Segmente, die du geändert hast, bleiben erhalten).");

        ja(SPEAKER, "話者");
        zh(SPEAKER, "说话者");
        fr(SPEAKER, "Locuteur");
        es(SPEAKER, "Hablante");
        de(SPEAKER, "Sprecher");

        ja(SPOKEN_TEXT, "読み上げるテキスト");
        zh(SPOKEN_TEXT, "朗读文本");
        fr(SPOKEN_TEXT, "Texte lu");
        es(SPOKEN_TEXT, "Texto hablado");
        de(SPOKEN_TEXT, "Gesprochener Text");

        ja(DELIVERY, "話し方");
        zh(DELIVERY, "语气");
        fr(DELIVERY, "Diction");
        es(DELIVERY, "Interpretación");
        de(DELIVERY, "Vortrag");

        ja(INTENSITY, "強さ");
        zh(INTENSITY, "强度");
        fr(INTENSITY, "Intensité");
        es(INTENSITY, "Intensidad");
        de(INTENSITY, "Intensität");

        ja(EXAGGERATION, "誇張");
        zh(EXAGGERATION, "夸张度");
        fr(EXAGGERATION, "Exagération");
        es(EXAGGERATION, "Exageración");
        de(EXAGGERATION, "Exaggeration");

        ja(LANGUAGE, "言語");
        zh(LANGUAGE, "语言");
        fr(LANGUAGE, "Langue");
        es(LANGUAGE, "Idioma");
        de(LANGUAGE, "Sprache");

        ja(SEED, "シード");
        zh(SEED, "种子");
        fr(SEED, "Graine");
        es(SEED, "Semilla");
        de(SEED, "Seed");

        ja(SEED_X, "シード %s");
        zh(SEED_X, "种子 %s");
        fr(SEED_X, "Graine %s");
        es(SEED_X, "Semilla %s");
        de(SEED_X, "Seed %s");

        ja(PAUSE_AFTER_SECONDS, "後の間 (秒)");
        zh(PAUSE_AFTER_SECONDS, "之后停顿（秒）");
        fr(PAUSE_AFTER_SECONDS, "Pause après (s)");
        es(PAUSE_AFTER_SECONDS, "Pausa después (s)");
        de(PAUSE_AFTER_SECONDS, "Pause danach (s)");

        ja(STATUS, "状態");
        zh(STATUS, "状态");
        fr(STATUS, "Statut");
        es(STATUS, "Estado");
        de(STATUS, "Status");

        ja(STATUS_DRAFT, "下書き");
        zh(STATUS_DRAFT, "草稿");
        fr(STATUS_DRAFT, "Brouillon");
        es(STATUS_DRAFT, "Borrador");
        de(STATUS_DRAFT, "Entwurf");

        ja(STATUS_REVIEWED, "確認済み");
        zh(STATUS_REVIEWED, "已审阅");
        fr(STATUS_REVIEWED, "Relue");
        es(STATUS_REVIEWED, "Revisada");
        de(STATUS_REVIEWED, "Geprüft");

        ja(STATUS_FINAL, "最終版");
        zh(STATUS_FINAL, "定稿");
        fr(STATUS_FINAL, "Définitive");
        es(STATUS_FINAL, "Definitiva");
        de(STATUS_FINAL, "Endgültig");

        ja(LOCKED, "固定");
        zh(LOCKED, "已锁定");
        fr(LOCKED, "Verrouillé");
        es(LOCKED, "Bloqueado");
        de(LOCKED, "Gesperrt");

        ja(TAKES, "テイク");
        zh(TAKES, "录音");
        fr(TAKES, "Prises");
        es(TAKES, "Tomas");
        de(TAKES, "Takes");

        ja(TAKE, "テイク");
        zh(TAKE, "录音");
        fr(TAKE, "Prise");
        es(TAKE, "Toma");
        de(TAKE, "Take");

        ja(MADE, "作成済み");
        zh(MADE, "已生成");
        fr(MADE, "Fabriquée");
        es(MADE, "Hecha");
        de(MADE, "Erstellt");

        ja(HEAR, "聞く");
        zh(HEAR, "试听");
        fr(HEAR, "Écouter");
        es(HEAR, "Escuchar");
        de(HEAR, "Anhören");

        ja(ANOTHER_TAKE, "別のテイク");
        zh(ANOTHER_TAKE, "再来一遍");
        fr(ANOTHER_TAKE, "Une autre prise");
        es(ANOTHER_TAKE, "Otra toma");
        de(ANOTHER_TAKE, "Ein anderer Take");

        ja(LOWER_THE_INTENSITY, "強さを下げる");
        zh(LOWER_THE_INTENSITY, "降低强度");
        fr(LOWER_THE_INTENSITY, "Baisser l'intensité");
        es(LOWER_THE_INTENSITY, "Reducir la intensidad");
        de(LOWER_THE_INTENSITY, "Die Intensität verringern");

        ja(MORE_PAUSE, "間を長く");
        zh(MORE_PAUSE, "停顿更久");
        fr(MORE_PAUSE, "Plus de pause");
        es(MORE_PAUSE, "Más pausa");
        de(MORE_PAUSE, "Mehr Pause");

        ja(UNLOCK, "固定を解除");
        zh(UNLOCK, "解除锁定");
        fr(UNLOCK, "Déverrouiller");
        es(UNLOCK, "Desbloquear");
        de(UNLOCK, "Entsperren");

        ja(APPLY, "適用");
        zh(APPLY, "应用");
        fr(APPLY, "Appliquer");
        es(APPLY, "Aplicar");
        de(APPLY, "Anwenden");

        ja(CHOOSE_AND_HEAR_THE_TAKE, "このテイクを使って聞く");
        zh(CHOOSE_AND_HEAR_THE_TAKE, "采用此录音并试听");
        fr(CHOOSE_AND_HEAR_THE_TAKE, "Utiliser cette prise et l'écouter");
        es(CHOOSE_AND_HEAR_THE_TAKE, "Usar esta toma y escucharla");
        de(CHOOSE_AND_HEAR_THE_TAKE, "Diesen Take verwenden und anhören");

        ja(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "手動で変更済み: 新しい注釈でもそのまま保持されます。");
        zh(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "已手动更改：重新标注时保持原样。");
        fr(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Modifié à la main : une nouvelle annotation le garde tel quel.");
        es(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Cambiado a mano: una anotación nueva lo conserva tal como está.");
        de(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Von Hand geändert: Eine neue Annotation behält es, wie es ist.");

        ja(AS_THE_ANNOTATION_MADE_IT, "注釈で作られたまま: 新しい注釈で置き換えられます。");
        zh(AS_THE_ANNOTATION_MADE_IT, "由标注生成：重新标注时会被替换。");
        fr(AS_THE_ANNOTATION_MADE_IT, "Tel que l'annotation l'a fait : une nouvelle annotation le remplace.");
        es(AS_THE_ANNOTATION_MADE_IT, "Tal como lo hizo la anotación: una anotación nueva lo reemplaza.");
        de(AS_THE_ANNOTATION_MADE_IT, "So, wie die Annotation es erstellt hat: Eine neue Annotation ersetzt es.");

        ja(THE_REQUEST_TO_THE_SERVER, "音声サーバーに送るリクエスト");
        zh(THE_REQUEST_TO_THE_SERVER, "发给语音服务器的请求");
        fr(THE_REQUEST_TO_THE_SERVER, "La requête que reçoit le serveur de synthèse vocale");
        es(THE_REQUEST_TO_THE_SERVER, "La solicitud que recibe el servidor de voz");
        de(THE_REQUEST_TO_THE_SERVER, "Die Anfrage, die der Sprachserver erhält");

        ja(MAKING_THE_SEGMENT, "セグメントを作成しています (音声サーバーを起動します) ...");
        zh(MAKING_THE_SEGMENT, "正在生成该片段（会启动语音服务器）...");
        fr(MAKING_THE_SEGMENT, "Fabrication du segment (le serveur de synthèse vocale est démarré pour lui) ...");
        es(MAKING_THE_SEGMENT, "Creando el segmento (se inicia el servidor de voz para ello) ...");
        de(MAKING_THE_SEGMENT, "Das Segment wird erstellt (der Sprachserver wird dafür gestartet) ...");

        ja(THE_JOB_FAILED, "失敗しました: %s");
        zh(THE_JOB_FAILED, "失败：%s");
        fr(THE_JOB_FAILED, "Échec : %s");
        es(THE_JOB_FAILED, "Ha fallado: %s");
        de(THE_JOB_FAILED, "Fehlgeschlagen: %s");

        ja(FINAL_PARTS_ARE_LEFT_ALONE, "状態が最終版のため変更しなかったパート: %s");
        zh(FINAL_PARTS_ARE_LEFT_ALONE, "因状态为定稿而未改动的章节：%s");
        fr(FINAL_PARTS_ARE_LEFT_ALONE, "Parties non touchées parce que leur statut est définitif : %s");
        es(FINAL_PARTS_ARE_LEFT_ALONE, "Partes que se dejaron como están porque su estado es definitivo: %s");
        de(FINAL_PARTS_ARE_LEFT_ALONE, "Abschnitte, die unangetastet bleiben, weil ihr Status „Endgültig“ ist: %s");

        ja(PRONUNCIATION, "発音");
        zh(PRONUNCIATION, "发音");
        fr(PRONUNCIATION, "Prononciation");
        es(PRONUNCIATION, "Pronunciación");
        de(PRONUNCIATION, "Aussprache");

        ja(NUMBERS_AND_ABBREVIATIONS, "数字と略語");
        zh(NUMBERS_AND_ABBREVIATIONS, "数字和缩写");
        fr(NUMBERS_AND_ABBREVIATIONS, "Nombres et abréviations");
        es(NUMBERS_AND_ABBREVIATIONS, "Números y abreviaturas");
        de(NUMBERS_AND_ABBREVIATIONS, "Zahlen und Abkürzungen");

        ja(RULES_ENGLISH, "英語: 数字、略語、番号付きの見出しを単語として読む");
        zh(RULES_ENGLISH, "英语：数字、缩写和带编号的标题按单词朗读");
        fr(RULES_ENGLISH, "Anglais : les nombres, abréviations et titres numérotés sont lus en toutes lettres");
        es(RULES_ENGLISH, "Inglés: los números, las abreviaturas y los títulos numerados se leen como palabras");
        de(RULES_ENGLISH, "Englisch: Zahlen, Abkürzungen und nummerierte Überschriften werden als Wörter gelesen");

        ja(RULES_NONE, "オフ: 辞書のみ (他の言語の本用)");
        zh(RULES_NONE, "关闭：只用词典（用于其他语言的书）");
        fr(RULES_NONE, "Désactivé : seulement le dictionnaire (pour un livre dans une autre langue)");
        es(RULES_NONE, "Desactivado: solo el diccionario (para un libro en otro idioma)");
        de(RULES_NONE, "Aus: nur das Wörterbuch (für ein Buch in einer anderen Sprache)");

        ja(WORD_BEFORE_A_CHAPTER_NUMBER, "章番号の前の語");
        zh(WORD_BEFORE_A_CHAPTER_NUMBER, "章节编号前的词");
        fr(WORD_BEFORE_A_CHAPTER_NUMBER, "Mot avant un numéro de chapitre");
        es(WORD_BEFORE_A_CHAPTER_NUMBER, "Palabra antes de un número de capítulo");
        de(WORD_BEFORE_A_CHAPTER_NUMBER, "Wort vor einer Kapitelnummer");

        ja(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "番号だけの見出し用:「IV. The Arch」は「Chapter four. The Arch」と読まれます。空欄なら番号のみ。");
        zh(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "用于只有编号的标题：“IV. The Arch”读作“Chapter four. The Arch”。留空则只读编号。");
        fr(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Pour un titre qui n'est qu'un numéro : \"IV. L'Arche\" est lu \"Chapitre quatre. L'Arche\". Vide : juste le numéro.");
        es(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Para un título que es solo un número: \"IV. El Arco\" se lee \"Capítulo cuatro. El Arco\". Vacío: solo el número.");
        de(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Für eine Überschrift, die nur aus einer Zahl besteht: „IV. Der Bogen“ wird als „Kapitel vier. Der Bogen“ gelesen. Leer: nur die Zahl.");

        ja(PRONUNCIATION_HELP, "本は変更されません。これは音声のための綴りです。辞書は単語全体 (大文字小文字は区別) を読み方に置き換えます: 名前や造語の綴り直し、日本語ならかな読み。読み上げが変わった部分だけが作り直されます。");
        zh(PRONUNCIATION_HELP, "书不会被改动：这是为朗读准备的拼写。词典按整词（区分大小写）替换成要读的内容：人名或自造词的改拼，日语的假名读法。读法改变的片段会重新生成，其余保留。");
        fr(PRONUNCIATION_HELP, "Le livre n'est jamais modifié : c'est ainsi que le texte est épelé pour la voix. Le dictionnaire remplace des mots entiers (la casse compte) par ce qui est dit à leur place : une transcription pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Les morceaux dont le texte lu change sont refaits, les autres sont gardés.");
        es(PRONUNCIATION_HELP, "El libro nunca se cambia: así se escribe el texto para la voz. El diccionario reemplaza palabras completas (importan las mayúsculas y minúsculas) por lo que se dice en su lugar: una reescritura fonética para un nombre o una palabra inventada, o una lectura en kana para el japonés. Los fragmentos cuyo texto hablado cambia se rehacen, los demás se conservan.");
        de(PRONUNCIATION_HELP, "Das Buch wird nie verändert: So wird der Text für die Stimme buchstabiert. Das Wörterbuch ersetzt ganze Wörter (Groß-/Kleinschreibung zählt) durch das, was dafür gesagt wird: eine Umschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Stücke, deren gesprochener Text sich ändert, werden neu erstellt, die anderen bleiben erhalten.");

        ja(DICTIONARY, "辞書");
        zh(DICTIONARY, "词典");
        fr(DICTIONARY, "Dictionnaire");
        es(DICTIONARY, "Diccionario");
        de(DICTIONARY, "Wörterbuch");

        ja(ADD_A_WORD, "単語を追加");
        zh(ADD_A_WORD, "添加词");
        fr(ADD_A_WORD, "Ajouter un mot");
        es(ADD_A_WORD, "Añadir una palabra");
        de(ADD_A_WORD, "Ein Wort hinzufügen");

        ja(WORD_IN_THE_BOOK, "本の中の単語");
        zh(WORD_IN_THE_BOOK, "书中的词");
        fr(WORD_IN_THE_BOOK, "Mot dans le livre");
        es(WORD_IN_THE_BOOK, "Palabra en el libro");
        de(WORD_IN_THE_BOOK, "Wort im Buch");

        ja(SAID_AS, "読み方");
        zh(SAID_AS, "读作");
        fr(SAID_AS, "Prononcé");
        es(SAID_AS, "Se dice como");
        de(SAID_AS, "Gesprochen als");

        ja(TRY_A_TEXT, "テキストを試す");
        zh(TRY_A_TEXT, "试一段文字");
        fr(TRY_A_TEXT, "Essayer un texte");
        es(TRY_A_TEXT, "Probar un texto");
        de(TRY_A_TEXT, "Einen Text ausprobieren");

        ja(AS_A_HEADING, "見出しとして");
        zh(AS_A_HEADING, "作为标题");
        fr(AS_A_HEADING, "Comme titre");
        es(AS_A_HEADING, "Como un título");
        de(AS_A_HEADING, "Als Überschrift");

        ja(IT_IS_SAID_AS, "読まれ方");
        zh(IT_IS_SAID_AS, "读出来是");
        fr(IT_IS_SAID_AS, "Il se prononce");
        es(IT_IS_SAID_AS, "Se dice como");
        de(IT_IS_SAID_AS, "Es wird gesprochen als");

        ja(NOTHING_SCENE_BREAK, "(なし: 場面転換、間)");
        zh(NOTHING_SCENE_BREAK, "（无：场景分隔，停顿）");
        fr(NOTHING_SCENE_BREAK, "(rien : un changement de scène, une pause)");
        es(NOTHING_SCENE_BREAK, "(nada: un cambio de escena, una pausa)");
        de(NOTHING_SCENE_BREAK, "(nichts: ein Szenenwechsel, eine Pause)");

        ja(THE_PRONUNCIATION_IS_STORED, "発音を保存しました");
        zh(THE_PRONUNCIATION_IS_STORED, "发音已保存");
        fr(THE_PRONUNCIATION_IS_STORED, "La prononciation est enregistrée");
        es(THE_PRONUNCIATION_IS_STORED, "La pronunciación se ha guardado");
        de(THE_PRONUNCIATION_IS_STORED, "Die Aussprache ist gespeichert");

        ja(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "発音を保存できませんでした: %s");
        zh(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "无法保存发音：%s");
        fr(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "La prononciation n'a pas pu être enregistrée : %s");
        es(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "No se pudo guardar la pronunciación: %s");
        de(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "Die Aussprache konnte nicht gespeichert werden: %s");

        ja(IN_THE_BOOK, "本の中: %s");
        zh(IN_THE_BOOK, "书中：%s");
        fr(IN_THE_BOOK, "Dans le livre : %s");
        es(IN_THE_BOOK, "En el libro: %s");
        de(IN_THE_BOOK, "Im Buch: %s");

        ja(AUDIOBOOK_ZIP, "オーディオブック (章ごとに mp3、zip)");
        zh(AUDIOBOOK_ZIP, "有声书（每章一个 mp3，打包为 zip）");
        fr(AUDIOBOOK_ZIP, "Livre audio (un mp3 par chapitre, dans un zip)");
        es(AUDIOBOOK_ZIP, "Audiolibro (un mp3 por capítulo, en un zip)");
        de(AUDIOBOOK_ZIP, "Hörbuch (eine mp3 je Kapitel, in einer ZIP-Datei)");

        ja(AUDIOBOOK_ONE_MP3, "オーディオブック (章マーク付きの mp3 1 本)");
        zh(AUDIOBOOK_ONE_MP3, "有声书（带章节标记的单个 mp3）");
        fr(AUDIOBOOK_ONE_MP3, "Livre audio (un seul mp3 avec des marques de chapitre)");
        es(AUDIOBOOK_ONE_MP3, "Audiolibro (un mp3 con marcas de capítulo)");
        de(AUDIOBOOK_ONE_MP3, "Hörbuch (eine mp3 mit Kapitelmarken)");
        ja(AUDIOBOOK_M4B, "オーディオブック (章付きの m4b 1 本)");
        zh(AUDIOBOOK_M4B, "有声书（带章节的单个 m4b）");
        fr(AUDIOBOOK_M4B, "Livre audio (un seul m4b avec des chapitres)");
        es(AUDIOBOOK_M4B, "Audiolibro (un m4b con capítulos)");
        de(AUDIOBOOK_M4B, "Hörbuch (eine m4b mit Kapiteln)");
        ja(STORAGE_OF_THE_AUDIO, "音声の保存形式 (このインストール全体)");
        zh(STORAGE_OF_THE_AUDIO, "音频的存储格式（对整个安装生效）");
        fr(STORAGE_OF_THE_AUDIO, "Comment l'audio est stocké (pour tout le monde sur cette installation)");
        es(STORAGE_OF_THE_AUDIO, "Cómo se guarda el audio (para todos en esta instalación)");
        de(STORAGE_OF_THE_AUDIO, "Wie das Audio gespeichert wird (für alle auf dieser Installation)");
        ja(STORAGE_MP3, "mp3 (小さい。m4b は二重にエンコードされます)");
        zh(STORAGE_MP3, "mp3（体积小；m4b 会被二次编码）");
        fr(STORAGE_MP3, "mp3 (léger ; un m4b est encodé une seconde fois)");
        es(STORAGE_MP3, "mp3 (pequeño; un m4b se codifica por segunda vez)");
        de(STORAGE_MP3, "mp3 (klein; eine m4b wird ein zweites Mal kodiert)");
        ja(STORAGE_WAV, "wav (ロスレス、約10倍のサイズ。ffmpeg が必要)");
        zh(STORAGE_WAV, "wav（无损，约为十倍大小；需要 ffmpeg）");
        fr(STORAGE_WAV, "wav (sans perte, environ dix fois plus gros ; nécessite ffmpeg)");
        es(STORAGE_WAV, "wav (sin pérdida, unas diez veces el tamaño; necesita ffmpeg)");
        de(STORAGE_WAV, "wav (verlustfrei, etwa zehnmal so groß; benötigt ffmpeg)");
        ja(STORAGE_OF_THE_AUDIO_HELP, "管理者にのみ表示されます。これから作成するパートに適用され、以前のパートはそのまま使われます。wav は mp3 と zip の書き出し時に ffmpeg でエンコードされ、m4b では一度だけエンコードされます。");
        zh(STORAGE_OF_THE_AUDIO_HELP, "仅管理员可见。仅对之后生成的章节生效，之前的章节保持原样。导出 mp3 和 zip 时由 ffmpeg 编码 wav，导出 m4b 时只编码一次。");
        fr(STORAGE_OF_THE_AUDIO_HELP, "Seuls les administrateurs voient ceci. S'applique aux parties fabriquées à partir de maintenant ; les parties fabriquées avant gardent leur audio et sont comprises telles quelles. Un wav est encodé par ffmpeg pour les exports mp3 et zip et une fois pour le m4b, le m4b n'est donc pas encodé deux fois.");
        es(STORAGE_OF_THE_AUDIO_HELP, "Solo lo ven los administradores. Se aplica a las partes que se creen a partir de ahora; las partes creadas antes conservan su audio y se entienden tal como están. ffmpeg codifica un wav para las exportaciones en mp3 y zip y una vez para el m4b, así que el m4b no se codifica dos veces.");
        de(STORAGE_OF_THE_AUDIO_HELP, "Nur Administratoren sehen dies. Es gilt für die Abschnitte, die ab jetzt erstellt werden; die früher erstellten behalten ihr Audio und werden so verstanden, wie sie sind. Eine wav wird von ffmpeg für die mp3- und ZIP-Exporte und einmal für die m4b kodiert, sodass die m4b nicht zweimal kodiert wird.");
        ja(FFMPEG_IS_NOT_INSTALLED, "サーバーに ffmpeg がインストールされていません。mp3 のみ選択でき、m4b の書き出しは利用できません。");
        zh(FFMPEG_IS_NOT_INSTALLED, "服务器未安装 ffmpeg：只能选择 mp3，且不提供 m4b 导出。");
        fr(FFMPEG_IS_NOT_INSTALLED, "ffmpeg n'est pas installé sur le serveur : seul le mp3 peut être choisi, et l'export m4b n'est pas proposé.");
        es(FFMPEG_IS_NOT_INSTALLED, "ffmpeg no está instalado en el servidor: solo se puede elegir mp3 y no se ofrece la exportación en m4b.");
        de(FFMPEG_IS_NOT_INSTALLED, "ffmpeg ist auf dem Server nicht installiert: Es kann nur mp3 gewählt werden, und der m4b-Export wird nicht angeboten.");
        ja(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "保存しました。新しいパートはこの形式で保存されます。");
        zh(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "已保存。新章节将按此方式存储。");
        fr(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Enregistré. Les nouvelles parties sont stockées ainsi.");
        es(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Guardado. Las partes nuevas se guardan así.");
        de(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Gespeichert. Neue Abschnitte werden so gespeichert.");

        ja(ONLY_THE_PARTS_MARKED_FINAL, "オーディオブック: 最終版のパートのみ");
        zh(ONLY_THE_PARTS_MARKED_FINAL, "有声书：只含已定稿的章节");
        fr(ONLY_THE_PARTS_MARKED_FINAL, "Livre audio : seulement les parties marquées définitives");
        es(ONLY_THE_PARTS_MARKED_FINAL, "Audiolibro: solo las partes marcadas como definitivas");
        de(ONLY_THE_PARTS_MARKED_FINAL, "Hörbuch: nur die als endgültig markierten Abschnitte");

        ja(PART_NUMBER, "パート %s");
        zh(PART_NUMBER, "第 %s 部分");
        fr(PART_NUMBER, "Partie %s");
        es(PART_NUMBER, "Parte %s");
        de(PART_NUMBER, "Abschnitt %s");

        ja(NO_AUDIO, "音声なし");
        zh(NO_AUDIO, "没有音频");
        fr(NO_AUDIO, "pas d'audio");
        es(NO_AUDIO, "sin audio");
        de(NO_AUDIO, "kein Audio");

        ja(AUDIO_OLDER_THAN_THE_TEXT, "音声が本文より古い");
        zh(AUDIO_OLDER_THAN_THE_TEXT, "音频比文本旧");
        fr(AUDIO_OLDER_THAN_THE_TEXT, "audio plus ancien que le texte");
        es(AUDIO_OLDER_THAN_THE_TEXT, "audio más antiguo que el texto");
        de(AUDIO_OLDER_THAN_THE_TEXT, "Audio älter als der Text");

        ja(AUDIO_OLDER_THAN_THE_SCRIPT, "音声がスクリプトより古い");
        zh(AUDIO_OLDER_THAN_THE_SCRIPT, "音频比脚本旧");
        fr(AUDIO_OLDER_THAN_THE_SCRIPT, "audio plus ancien que le script");
        es(AUDIO_OLDER_THAN_THE_SCRIPT, "audio más antiguo que el guion");
        de(AUDIO_OLDER_THAN_THE_SCRIPT, "Audio älter als das Skript");

        ja(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "オーディオブックから除外したパート: %s。先に変換 (またはスタジオで作り直し) してください。");
        zh(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "有声书中省略的章节：%s。请先转换（或在工作室中重新生成）。");
        fr(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Parties omises du livre audio : %s. Convertissez-les d'abord (ou refaites-les dans le studio).");
        es(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Partes omitidas del audiolibro: %s. Conviértelas (o créalas de nuevo en el estudio) primero.");
        de(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Aus dem Hörbuch ausgelassene Abschnitte: %s. Wandle sie zuerst um (oder erstelle sie im Studio neu).");

        ja(NO_PART_HAS_UP_TO_DATE_AUDIO, "最新の音声を持つパートがありません: %s。先にパートを変換してください (音声に変換)、または最終版のパートを選んでください。");
        zh(NO_PART_HAS_UP_TO_DATE_AUDIO, "没有章节拥有最新的音频：%s。请先转换章节（转为音频），或选择已定稿的章节。");
        fr(NO_PART_HAS_UP_TO_DATE_AUDIO, "Aucune partie n'a d'audio à jour : %s. Convertissez d'abord les parties (Convertir en audio), ou choisissez des parties définitives.");
        es(NO_PART_HAS_UP_TO_DATE_AUDIO, "Ninguna parte tiene audio actualizado: %s. Convierte primero las partes (Convertir en audio), o elige partes definitivas.");
        de(NO_PART_HAS_UP_TO_DATE_AUDIO, "Kein Abschnitt hat aktuelles Audio: %s. Wandle zuerst die Abschnitte um (In Audio umwandeln) oder wähle Abschnitte, die endgültig sind.");

        ja(NO_PART_TO_EXPORT, "書き出すパートがありません: 最終版のパートがない (または範囲が空) です。");
        zh(NO_PART_TO_EXPORT, "没有可导出的章节：没有已定稿的章节（或范围为空）。");
        fr(NO_PART_TO_EXPORT, "Il n'y a aucune partie à exporter : aucune n'est marquée définitive (ou la plage de parties est vide).");
        es(NO_PART_TO_EXPORT, "No hay ninguna parte que exportar: ninguna está marcada como definitiva (o el intervalo de partes está vacío).");
        de(NO_PART_TO_EXPORT, "Es gibt keinen Abschnitt zum Exportieren: Keiner ist als endgültig markiert (oder der Bereich der Abschnitte ist leer).");

        ja(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "新しいパートを自動で音声に変換");
        zh(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "自动把新章节转为音频");
        fr(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Convertir automatiquement les nouvelles parties en audio");
        es(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Convertir las partes nuevas en audio automáticamente");
        de(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Neue Abschnitte von selbst in Audio umwandeln");

        ja(AUTO_NO, "しない");
        zh(AUTO_NO, "否");
        fr(AUTO_NO, "Non");
        es(AUTO_NO, "No");
        de(AUTO_NO, "Nein");

        ja(AUTO_PLAIN, "する: ナレーターが読む");
        zh(AUTO_PLAIN, "是：由旁白朗读");
        fr(AUTO_PLAIN, "Oui : le narrateur lit la partie");
        es(AUTO_PLAIN, "Sí: el narrador lee la parte");
        de(AUTO_PLAIN, "Ja: Der Erzähler liest den Abschnitt");

        ja(AUTO_LABELLED, "する: 本のモデルでラベルを付け、キャストが声を当てる");
        zh(AUTO_LABELLED, "是：用书的模型标注，由角色分配提供声音");
        fr(AUTO_LABELLED, "Oui : étiquetée par le modèle du livre, la distribution donne les voix");
        es(AUTO_LABELLED, "Sí: etiquetada por el modelo del libro, el reparto da las voces");
        de(AUTO_LABELLED, "Ja: vom Modell des Buchs etikettiert, die Besetzung liefert die Stimmen");

        ja(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成されたパートが最終版になると (本に自動注釈と再執筆がある場合はその後)、オーディオブックのキューに入ります。生成が動いている間、音声は待機します。設定 → 拡張機能の設定 → オーディオブックの設定 で選んだ音声サーバーを使います。");
        zh(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成的章节定稿后（若书设有自动注释和重写，则在其完成后）会进入有声书队列。你有生成任务运行时，语音会等待。使用在 设置 → 扩展设置 → 有声书设置 中选定的语音服务器。");
        fr(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Quand une partie générée est définitive (l'annotation et la réécriture automatiques du livre sont faites, s'il en a), elle entre dans la file du livre audio. La synthèse vocale attend pendant qu'une de vos générations tourne. Utilise le serveur de synthèse vocale choisi dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Cuando una parte generada es definitiva (la anotación automática y la reescritura del libro están hechas, si las tiene) pasa a la cola del audiolibro. La voz espera mientras se ejecuta una generación tuya. Usa el servidor de voz elegido en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Wenn ein generierter Abschnitt endgültig ist (die automatische Annotation und Überarbeitung des Buchs sind erledigt, falls es welche hat), kommt er in die Warteschlange des Hörbuchs. Die Sprache wartet, solange eine Generierung von dir läuft. Verwendet den Sprachserver, der unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen gewählt ist.");

        ja(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新しいパートをオーディオブックのキューに入れました");
        zh(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新章节已进入有声书队列");
        fr(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "La nouvelle partie est dans la file du livre audio");
        es(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "La parte nueva está en la cola del audiolibro");
        de(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "Der neue Abschnitt ist in der Warteschlange des Hörbuchs");

        ja(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "本にラベル付け用のモデルがないため、ナレーターが新しいパートを読みます");
        zh(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "书没有用于标注的模型：由旁白朗读新章节");
        fr(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "Le livre n'a pas de modèle pour l'étiquetage : le narrateur lit la nouvelle partie");
        es(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "El libro no tiene modelo para el etiquetado: el narrador lee la parte nueva");
        de(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "Das Buch hat kein Modell für das Etikettieren: Der Erzähler liest den neuen Abschnitt");

        ja(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新しいパートの音声ができました");
        zh(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新章节的音频已生成");
        fr(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "L'audio de la nouvelle partie est prêt");
        es(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "El audio de la parte nueva está listo");
        de(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "Das Audio des neuen Abschnitts ist fertig");

        ja(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "新しいパートの音声を作成できませんでした: %s");
        zh(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "无法生成新章节的音频：%s");
        fr(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "L'audio de la nouvelle partie n'a pas pu être fabriqué : %s");
        es(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "No se pudo crear el audio de la parte nueva: %s");
        de(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "Das Audio des neuen Abschnitts konnte nicht erstellt werden: %s");

        ja(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "指定のない本の新しいパートを自動変換");
        zh(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "未指定的书自动转换新章节");
        fr(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Convertir automatiquement les nouvelles parties, pour les livres qui ne disent rien");
        es(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Convertir las partes nuevas automáticamente, para los libros que no indican nada");
        de(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Neue Abschnitte von selbst umwandeln, für die Bücher, die nichts dazu sagen");

        ja(PREVIEW, "試聴");
        zh(PREVIEW, "试听");
        fr(PREVIEW, "Aperçu");
        es(PREVIEW, "Vista previa");
        de(PREVIEW, "Vorschau");

        ja(MAKING_THE_SAMPLE, "サンプルを作成中 ...");
        zh(MAKING_THE_SAMPLE, "正在生成样本 ...");
        fr(MAKING_THE_SAMPLE, "Fabrication de l'échantillon ...");
        es(MAKING_THE_SAMPLE, "Creando la muestra ...");
        de(MAKING_THE_SAMPLE, "Das Beispiel wird erstellt ...");

        ja(PICK_A_VOICE_FIRST, "先に声を選んでください");
        zh(PICK_A_VOICE_FIRST, "请先选择声音");
        fr(PICK_A_VOICE_FIRST, "Choisissez d'abord une voix");
        es(PICK_A_VOICE_FIRST, "Elige primero una voz");
        de(PICK_A_VOICE_FIRST, "Wähle zuerst eine Stimme");

        ja(THE_VOICE_COULD_NOT_BE_HEARD, "声を再生できませんでした: %s");
        zh(THE_VOICE_COULD_NOT_BE_HEARD, "无法试听该声音：%s");
        fr(THE_VOICE_COULD_NOT_BE_HEARD, "La voix n'a pas pu être écoutée : %s");
        es(THE_VOICE_COULD_NOT_BE_HEARD, "No se pudo escuchar la voz: %s");
        de(THE_VOICE_COULD_NOT_BE_HEARD, "Die Stimme konnte nicht angehört werden: %s");

        ja(KEY_OF_THE_REQUESTS, "バッチ前後のリクエストのキー");
        zh(KEY_OF_THE_REQUESTS, "批处理前后请求的密钥");
        fr(KEY_OF_THE_REQUESTS, "Clé des requêtes autour d'un lot");
        es(KEY_OF_THE_REQUESTS, "Clave de las solicitudes alrededor de un lote");
        de(KEY_OF_THE_REQUESTS, "Schlüssel der Anfragen rund um einen Stapel");

        ja(KEY_OF_THE_REQUESTS_HELP, "バッチの前後のすべてのリクエストに Authorization: Bearer として付けられます。暗号化して保存され、再表示されないので、ヘッダーに書く必要はありません。空: 上の API キーを使います。");
        zh(KEY_OF_THE_REQUESTS_HELP, "作为 Authorization: Bearer 附加到批处理前后的每个请求，加密保存且不再显示，因此无需写入请求头。留空：使用上面的 API 密钥。");
        fr(KEY_OF_THE_REQUESTS_HELP, "Envoyée comme Authorization: Bearer avec chaque requête avant et après un lot, stockée chiffrée et jamais réaffichée, pour ne pas avoir à l'écrire dans leurs en-têtes. Vide : la clé d'API ci-dessus est utilisée.");
        es(KEY_OF_THE_REQUESTS_HELP, "Se envía como Authorization: Bearer con cada solicitud anterior y posterior a un lote, se guarda cifrada y no se vuelve a mostrar, para que no haya que escribirla en sus cabeceras. Vacío: se usa la clave de API de arriba.");
        de(KEY_OF_THE_REQUESTS_HELP, "Wird als Authorization: Bearer bei jeder Anfrage vor und nach einem Stapel gesendet, verschlüsselt gespeichert und nie wieder angezeigt, damit er nicht in ihre Header geschrieben werden muss. Leer: Der API-Schlüssel oben wird verwendet.");

        ja(THE_REQUEST_AFTER_THE_TEST_FAILED, "サーバーは応答しましたが、テスト後のリクエストが失敗しました: %s");
        zh(THE_REQUEST_AFTER_THE_TEST_FAILED, "服务器有响应，但测试后的请求失败：%s");
        fr(THE_REQUEST_AFTER_THE_TEST_FAILED, "Le serveur répond, mais la requête après le test a échoué : %s");
        es(THE_REQUEST_AFTER_THE_TEST_FAILED, "El servidor responde, pero falló la solicitud posterior a la prueba: %s");
        de(THE_REQUEST_AFTER_THE_TEST_FAILED, "Der Server antwortet, aber die Anfrage nach dem Test ist fehlgeschlagen: %s");

        ja(THE_REQUESTS_AROUND_THE_TEST_RAN, "テストの前後のリクエストを実行しました。");
        zh(THE_REQUESTS_AROUND_THE_TEST_RAN, "已执行测试前后的请求。");
        fr(THE_REQUESTS_AROUND_THE_TEST_RAN, "Les requêtes avant et après le test ont été faites. ");
        es(THE_REQUESTS_AROUND_THE_TEST_RAN, "Se hicieron las solicitudes anteriores y posteriores a la prueba. ");
        de(THE_REQUESTS_AROUND_THE_TEST_RAN, "Die Anfragen vor und nach dem Test wurden ausgeführt. ");

        ja(TEST_HELP, "テストはバッチ前のリクエスト（停止中のサーバーを起動）を実行し、サーバーに問い合わせ、バッチ後のリクエストを実行します。");
        zh(TEST_HELP, "测试会先执行批处理前的请求（启动已停止的服务器），再询问服务器，最后执行批处理后的请求。");
        fr(TEST_HELP, "Le test fait les requêtes d'avant un lot (elles démarrent un serveur arrêté), interroge le serveur, puis fait les requêtes d'après.");
        es(TEST_HELP, "La prueba hace las solicitudes anteriores a un lote (inician un servidor que está detenido), consulta al servidor y hace las solicitudes posteriores.");
        de(TEST_HELP, "Der Test führt die Anfragen vor einem Stapel aus (sie starten einen Server, der gestoppt ist), fragt den Server und führt die Anfragen danach aus.");

        ja(DOWNLOAD_THE_AUDIOBOOK, "オーディオブック全体");
        zh(DOWNLOAD_THE_AUDIOBOOK, "整本有声书");
        fr(DOWNLOAD_THE_AUDIOBOOK, "Tout le livre audio");
        es(DOWNLOAD_THE_AUDIOBOOK, "El audiolibro completo");
        de(DOWNLOAD_THE_AUDIOBOOK, "Das gesamte Hörbuch");

        ja(DOWNLOAD_THE_AUDIOBOOK_HELP, "最新の音声があるパートをすべて、パートごとの章を付けた1つのmp3にまとめ、ダウンロードできるようにします");
        zh(DOWNLOAD_THE_AUDIOBOOK_HELP, "把所有音频最新的章节合并为一个 mp3（每个章节一个章节标记），并提供下载");
        fr(DOWNLOAD_THE_AUDIOBOOK_HELP, "Joint toutes les parties ayant un audio à jour en un seul mp3 avec un chapitre par partie, et le propose au téléchargement");
        es(DOWNLOAD_THE_AUDIOBOOK_HELP, "Une todas las partes que tienen audio actualizado en un mp3 con un capítulo por cada parte y lo ofrece para descargar");
        de(DOWNLOAD_THE_AUDIOBOOK_HELP, "Fügt alle Abschnitte mit aktuellem Audio zu einer mp3 mit einem Kapitel je Abschnitt zusammen und bietet sie zum Herunterladen an");

        ja(THE_AUDIOBOOK_IS_BEING_JOINED, "オーディオブックを結合しています ...");
        zh(THE_AUDIOBOOK_IS_BEING_JOINED, "正在合并有声书 ...");
        fr(THE_AUDIOBOOK_IS_BEING_JOINED, "Assemblage du livre audio ...");
        es(THE_AUDIOBOOK_IS_BEING_JOINED, "Uniendo el audiolibro ...");
        de(THE_AUDIOBOOK_IS_BEING_JOINED, "Das Hörbuch wird zusammengefügt ...");

        ja(DOWNLOAD_THE_AUDIOBOOK_FILE, "オーディオブックをダウンロード（約 %s MB）");
        zh(DOWNLOAD_THE_AUDIOBOOK_FILE, "下载有声书（约 %s MB）");
        fr(DOWNLOAD_THE_AUDIOBOOK_FILE, "Télécharger le livre audio (environ %s Mo)");
        es(DOWNLOAD_THE_AUDIOBOOK_FILE, "Descargar el audiolibro (unos %s MB)");
        de(DOWNLOAD_THE_AUDIOBOOK_FILE, "Das Hörbuch herunterladen (etwa %s MB)");

        ja(BACKEND_IN_USE, "使用するバックエンド");
        zh(BACKEND_IN_USE, "使用的后端");
        fr(BACKEND_IN_USE, "Moteur utilisé");
        es(BACKEND_IN_USE, "Backend en uso");
        de(BACKEND_IN_USE, "Verwendetes Backend");

        ja(BACKEND_IN_USE_HELP, "変換に使う音声サーバー。共有のものは管理者のもので、アドレスやキーは表示されません。");
        zh(BACKEND_IN_USE_HELP, "转换所用的语音服务器。共享的服务器属于管理员，其地址和密钥不会显示给你。");
        fr(BACKEND_IN_USE_HELP, "Le serveur de synthèse vocale qu'utilisent vos conversions. Un serveur partagé appartient à l'administrateur ; son adresse et ses clés ne vous sont pas montrées.");
        es(BACKEND_IN_USE_HELP, "El servidor de voz que usan tus conversiones. Uno compartido pertenece al administrador; no se te muestran su dirección ni sus claves.");
        de(BACKEND_IN_USE_HELP, "Der Sprachserver, den deine Umwandlungen verwenden. Ein geteilter gehört dem Administrator; seine Adresse und Schlüssel werden dir nicht angezeigt.");

        ja(SHARED_MARK, "（共有）");
        zh(SHARED_MARK, "（共享）");
        fr(SHARED_MARK, " (partagé)");
        es(SHARED_MARK, " (compartido)");
        de(SHARED_MARK, " (geteilt)");

        ja(OWN_SPEECH_SERVERS_ARE_OFF, "管理者はユーザー自身の音声サーバーの使用を許可していません。以前に追加したものは保持されますが、使われません。");
        zh(OWN_SPEECH_SERVERS_ARE_OFF, "管理员不允许用户使用自己的语音服务器。你之前添加的会保留，但不会被使用。");
        fr(OWN_SPEECH_SERVERS_ARE_OFF, "L'administrateur ne permet pas aux utilisateurs d'utiliser leurs propres serveurs de synthèse vocale. Ceux que vous avez ajoutés avant sont gardés et ne sont pas utilisés.");
        es(OWN_SPEECH_SERVERS_ARE_OFF, "El administrador no permite que los usuarios usen servidores de voz propios. Los que añadiste antes se conservan y no se usan.");
        de(OWN_SPEECH_SERVERS_ARE_OFF, "Der Administrator lässt Benutzer keine eigenen Sprachserver verwenden. Die, die du früher hinzugefügt hast, bleiben erhalten und werden nicht verwendet.");

        ja(SHARED_SPEECH_SERVERS, "共有の音声サーバー");
        zh(SHARED_SPEECH_SERVERS, "共享语音服务器");
        fr(SHARED_SPEECH_SERVERS, "Serveurs de synthèse vocale partagés");
        es(SHARED_SPEECH_SERVERS, "Servidores de voz compartidos");
        de(SHARED_SPEECH_SERVERS, "Geteilte Sprachserver");

        ja(SHARED_SPEECH_SERVERS_HELP, "管理者のみに表示されます。ユーザーが使える音声サーバーと、ユーザー自身のサーバーを追加できるかどうか。");
        zh(SHARED_SPEECH_SERVERS_HELP, "仅管理员可见。用户可以使用的语音服务器，以及用户是否可以添加自己的服务器。");
        fr(SHARED_SPEECH_SERVERS_HELP, "Seuls les administrateurs voient ceci. Les serveurs de synthèse vocale que les utilisateurs peuvent utiliser, et s'ils peuvent ajouter les leurs.");
        es(SHARED_SPEECH_SERVERS_HELP, "Solo lo ven los administradores. Servidores de voz que pueden usar los usuarios, y si pueden añadir los suyos.");
        de(SHARED_SPEECH_SERVERS_HELP, "Nur Administratoren sehen dies. Sprachserver, die Benutzer verwenden dürfen, und ob sie eigene hinzufügen dürfen.");

        ja(SHARED_SPEECH_SERVERS_DIALOG_HELP, "共有サーバーを使えるユーザーは、使用するバックエンドとしてそれを選べます。見えるのは名前だけで、アドレス、キー、リクエストは見えません。ナレーターの声はサーバーの声にしてください (クリップは 1 人のユーザーのものです)。");
        zh(SHARED_SPEECH_SERVERS_DIALOG_HELP, "可以使用共享服务器的用户把它选为所用后端。他们只能看到名称，看不到地址、密钥和请求。旁白声音应使用服务器自带的声音（参考音频属于某一个用户）。");
        fr(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Un utilisateur qui peut utiliser un serveur partagé le choisit comme moteur utilisé. Il voit son nom, jamais son adresse, ses clés ni ses requêtes. La voix du narrateur devrait être une voix du serveur (un extrait appartient à un seul utilisateur).");
        es(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Un usuario que puede usar un servidor compartido lo elige como backend en uso. Ve su nombre, nunca su dirección, sus claves ni sus solicitudes. La voz del narrador debería ser una voz del servidor (un clip pertenece a un usuario).");
        de(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Ein Benutzer, der einen geteilten Server verwenden darf, wählt ihn als verwendetes Backend. Er sieht seinen Namen, nie seine Adresse, seine Schlüssel oder seine Anfragen. Die Erzählerstimme sollte eine Stimme des Servers sein (ein Clip gehört einem Benutzer).");

        ja(ALL_USERS_MAY_USE_IT, "すべてのユーザーが使用可能");
        zh(ALL_USERS_MAY_USE_IT, "所有用户都可以使用");
        fr(ALL_USERS_MAY_USE_IT, "Tous les utilisateurs peuvent l'utiliser");
        es(ALL_USERS_MAY_USE_IT, "Todos los usuarios pueden usarlo");
        de(ALL_USERS_MAY_USE_IT, "Alle Benutzer dürfen ihn verwenden");

        ja(THESE_USERS_MAY_USE_IT, "使用できるユーザー");
        zh(THESE_USERS_MAY_USE_IT, "可以使用的用户");
        fr(THESE_USERS_MAY_USE_IT, "Ces utilisateurs peuvent l'utiliser");
        es(THESE_USERS_MAY_USE_IT, "Estos usuarios pueden usarlo");
        de(THESE_USERS_MAY_USE_IT, "Diese Benutzer dürfen ihn verwenden");

        ja(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "ユーザーが自分の音声サーバーを追加可能");
        zh(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "用户可以添加自己的语音服务器");
        fr(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Les utilisateurs peuvent ajouter leurs propres serveurs de synthèse vocale");
        es(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Los usuarios pueden añadir servidores de voz propios");
        de(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Benutzer dürfen eigene Sprachserver hinzufügen");

        ja(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "オフの場合、ユーザーは共有サーバーのみ使用できます。以前に追加したサーバーは保持されますが、使われません。");
        zh(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "关闭后，用户只能使用共享服务器；他们之前添加的服务器会保留，但不会被使用。");
        fr(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Quand c'est désactivé, les utilisateurs ne peuvent utiliser que les serveurs partagés ; les serveurs qu'ils ont ajoutés avant sont gardés et ne sont pas utilisés.");
        es(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Cuando está desactivado, los usuarios solo pueden usar los servidores compartidos; los servidores que añadieron antes se conservan y no se usan.");
        de(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Wenn dies aus ist, können Benutzer nur die geteilten Server verwenden; die Server, die sie früher hinzugefügt haben, bleiben erhalten und werden nicht verwendet.");

        ja(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "共有の音声サーバー「%s」にアドレスがありません");
        zh(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "共享语音服务器“%s”没有地址");
        fr(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "Le serveur de synthèse vocale partagé « %s » n'a pas d'adresse");
        es(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "El servidor de voz compartido \"%s\" no tiene dirección");
        de(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "Der geteilte Sprachserver „%s“ hat keine Adresse");

        ja(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "共有の音声サーバーを保存しました");
        zh(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "共享语音服务器已保存");
        fr(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Les serveurs de synthèse vocale partagés sont enregistrés");
        es(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Los servidores de voz compartidos se han guardado");
        de(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Die geteilten Sprachserver sind gespeichert");

        ja(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "このパート (と音声のない次のパート) をブラウザの音声で読み上げる");
        zh(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "用浏览器的语音朗读此章节（以及之后没有音频的章节）");
        fr(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Lire cette partie (et les suivantes sans audio) avec la voix du navigateur");
        es(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Leer esta parte (y las siguientes sin audio) con la voz del navegador");
        de(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Diesen Abschnitt (und die folgenden ohne Audio) mit der Stimme des Browsers lesen");

        ja(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "パートの音量をそろえる");
        zh(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "统一各章节的响度");
        fr(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Égaliser le volume des parties");
        es(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Igualar el volumen de las partes");
        de(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Die Lautstärke der Abschnitte angleichen");

        ja(TARGET_LOUDNESS_LUFS, "目標音量 (LUFS)");
        zh(TARGET_LOUDNESS_LUFS, "目标响度 (LUFS)");
        fr(TARGET_LOUDNESS_LUFS, "Volume cible (LUFS)");
        es(TARGET_LOUDNESS_LUFS, "Volumen objetivo (LUFS)");
        de(TARGET_LOUDNESS_LUFS, "Ziellautstärke (LUFS)");

        ja(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "各パートの結合した音声を ffmpeg (EBU R128) でこの音量にそろえ、パート間の音量差をなくします。以前に作ったパートは、次に変換するときにそろえられます。部分ごとの音声は残っているため、音声サーバーには問い合わせません。");
        zh(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "用 ffmpeg (EBU R128) 把每个章节合并后的音频调到这一响度，使各章节音量一致。之前制作的章节会在下次转换时调整；各段音频已保存，因此不会请求语音服务器。");
        fr(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "L'audio assemblé de chaque partie est mis à ce volume avec ffmpeg (EBU R128), pour que les parties soient au même niveau. Une partie fabriquée avant est nivelée la prochaine fois que vous la convertissez ; ses morceaux sont gardés, le serveur de synthèse vocale n'est donc pas sollicité.");
        es(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "El audio unido de cada parte se nivela a este volumen con ffmpeg (EBU R128), de modo que las partes quedan al mismo nivel. Una parte creada antes se nivela la próxima vez que la conviertas; sus fragmentos se conservan, así que no se consulta al servidor de voz.");
        de(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "Das zusammengefügte Audio jedes Abschnitts wird mit ffmpeg (EBU R128) auf diese Lautstärke angeglichen, damit die Abschnitte auf demselben Pegel liegen. Ein früher erstellter Abschnitt wird beim nächsten Umwandeln angeglichen; seine Stücke bleiben erhalten, der Sprachserver wird also nicht gefragt.");

        ja(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg が見つかりません。音量はそろえられません。");
        zh(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "未找到 ffmpeg：不会统一音频响度。");
        fr(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg est introuvable : l'audio n'est pas nivelé.");
        es(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "No se encontró ffmpeg: el audio no se nivela.");
        de(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg wurde nicht gefunden: Das Audio wird nicht angeglichen.");

        ja(LABELLING_PROMPT, "ラベル付けのプロンプト");
        zh(LABELLING_PROMPT, "标注提示词");
        fr(LABELLING_PROMPT, "Prompt d'étiquetage");
        es(LABELLING_PROMPT, "Prompt de etiquetado");
        de(LABELLING_PROMPT, "Etikettierungsprompt");

        ja(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "パートの各部分を誰が話し、どう読むかを言語モデルに伝える指示 (テンプレート)。空の場合は組み込みのものを使います。");
        zh(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "告诉语言模型章节的每一段由谁说、如何朗读的指令（模板）。留空：使用内置指令。");
        fr(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "L'instruction qui indique au modèle de langage qui dit chaque morceau d'une partie et comment il est lu (un modèle). Vide : celle intégrée.");
        es(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "La instrucción que le dice al modelo de lenguaje quién habla cada fragmento de una parte y cómo se lee (una plantilla). Vacío: la integrada.");
        de(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "Die Anweisung, die dem Sprachmodell sagt, wer jedes Stück eines Abschnitts spricht und wie es gelesen wird (eine Vorlage). Leer: die eingebaute.");

        ja(GPU_NAME, "GPU（名前）");
        zh(GPU_NAME, "GPU（名称）");
        fr(GPU_NAME, "GPU (nom)");
        es(GPU_NAME, "GPU (nombre)");
        de(GPU_NAME, "GPU (Name)");

        ja(GPU_NAME_HELP, "任意。このサーバーが他のサーバー (画像サーバーなど) とGPUを共有している場合に名前を付け、他にも同じ名前を指定します。互いのモデルを解放せず、順番に待ちます。空の場合は、このアドレスへのリクエスト同士だけが待ち合います。");
        zh(GPU_NAME_HELP, "可选。如果此服务器与其他服务器（如图像服务器）共用一块 GPU，请给它起个名字，并在其他服务器上填写相同的名字：它们会互相等待，而不是卸载对方的模型。留空：只有发往此地址的请求会互相等待。");
        fr(GPU_NAME_HELP, "Facultatif. Nommez le GPU quand ce serveur le partage avec d'autres serveurs (un serveur d'images, un autre serveur de synthèse vocale), et donnez-y le même nom : ils s'attendent alors au lieu de décharger le modèle des autres. Vide : seules les requêtes vers cette adresse s'attendent.");
        es(GPU_NAME_HELP, "Opcional. Indica el nombre de la GPU cuando este servidor la comparte con otros servidores (un servidor de imágenes, otro servidor de voz) y da allí el mismo nombre: entonces esperan unos a otros en lugar de descargarse mutuamente el modelo. Vacío: solo esperan entre sí las solicitudes a esta dirección.");
        de(GPU_NAME_HELP, "Optional. Gib der GPU einen Namen, wenn dieser Server sie mit anderen Servern teilt (einem Bildserver, einem anderen Sprachserver), und gib dort denselben Namen an: Sie warten dann aufeinander, statt gegenseitig ihr Modell zu entladen. Leer: Nur Anfragen an diese Adresse warten aufeinander.");
    }
}
