package com.github.enerccio.marginalia.extensions.audiobook;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Audiobook in the languages besides English; the English text is the key.
 */
public class AudiobookLocalization extends PluginLocalizationBundle {

    public static final AudiobookLocalization T = new AudiobookLocalization();

    public static final String AUDIOBOOK = "Audiobook";
    public static final String AUDIOBOOK_SETTINGS = "Audiobook Settings";
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
    public static final String FIRST_PART = "First part";
    public static final String LAST_PART = "Last part";
    public static final String THE_BOOK_HAS_PARTS_WITH_TEXT = "The book has %s parts with text. The parts that are up to date are not made again.";
    public static final String START = "Start";
    public static final String STOP_ALL = "Stop all";
    public static final String CLEAR_FINISHED = "Clear finished";
    public static final String CLOSE = "Close";
    public static final String PART = "Part";
    public static final String STATE = "State";
    public static final String PROGRESS = "Progress";
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
    public static final String THE_RANGE_IS_NOT_IN_THE_BOOK = "The range is not in the book: it has %s parts with text";
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

    @Override
    protected void loadKeys() {
        ja(AUDIOBOOK, "オーディオブック");
        zh(AUDIOBOOK, "有声书");

        ja(AUDIOBOOK_SETTINGS, "オーディオブックの設定");
        zh(AUDIOBOOK_SETTINGS, "有声书设置");

        ja(SPEECH_SERVERS_HELP, "オーディオブックを作る音声合成サーバー。バッチの前後のリクエストで GPU にサーバーの場所を空け、終わったら停止します。");
        zh(SPEECH_SERVERS_HELP, "制作有声书所用的语音合成服务器。批处理前后的请求会为服务器腾出 GPU，并在结束后将其停止。");

        ja(BACKENDS, "バックエンド");
        zh(BACKENDS, "后端");

        ja(ADD_A_BACKEND, "バックエンドを追加");
        zh(ADD_A_BACKEND, "添加后端");

        ja(DELETE_THE_BACKEND, "バックエンドを削除");
        zh(DELETE_THE_BACKEND, "删除后端");

        ja(NEW_BACKEND, "新しいバックエンド");
        zh(NEW_BACKEND, "新后端");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(ADDRESS, "アドレス");
        zh(ADDRESS, "地址");

        ja(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "音声合成サーバー (Chatterbox-TTS-Server) のベースアドレス (例: http://localhost:8004)、またはプロキシが転送するアドレス");
        zh(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "语音合成服务器（Chatterbox-TTS-Server）的基础地址，例如 http://localhost:8004，或代理转发到它的地址");

        ja(API_KEY, "API キー");
        zh(API_KEY, "API 密钥");

        ja(STORED_TYPE_TO_REPLACE, "保存済み。置き換えるには入力してください。");
        zh(STORED_TYPE_TO_REPLACE, "已保存。输入新密钥以替换。");

        ja(REMOVE_THE_KEY, "キーを削除");
        zh(REMOVE_THE_KEY, "删除密钥");

        ja(SPEECH_MODEL, "音声モデル");
        zh(SPEECH_MODEL, "语音模型");

        ja(MODEL_TURBO, "Chatterbox Turbo (英語、音のタグ)");
        zh(MODEL_TURBO, "Chatterbox Turbo（英语，声音标签）");

        ja(MODEL_ORIGINAL, "Chatterbox (英語、感情の強さ)");
        zh(MODEL_ORIGINAL, "Chatterbox（英语，情感强度）");

        ja(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 言語)");
        zh(MODEL_MULTILINGUAL, "Chatterbox Multilingual（23 种语言）");

        ja(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "バッチの前のリクエストが読み込んだ状態にしておくモデル。別のモデルが必要な本は別のバックエンドを選びます");
        zh(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "批处理前的请求应让服务器加载的模型；需要其他模型的书请选用另一个后端");

        ja(THE_SERVER_IS_LOCAL, "サーバーはこのマシンまたはローカルネットワークにあります");
        zh(THE_SERVER_IS_LOCAL, "服务器在本机或我的网络中");

        ja(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "ローカルネットワークのアドレスを許可します。クラウドのサーバーではオフのままにします");
        zh(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "允许本地网络地址；云端服务器请保持关闭");

        ja(TIMEOUT_SECONDS, "リクエストのタイムアウト (秒)");
        zh(TIMEOUT_SECONDS, "请求超时（秒）");

        ja(DEFAULTS_OF_A_SEGMENT, "セグメントの初期値");
        zh(DEFAULTS_OF_A_SEGMENT, "片段的默认值");

        ja(TEMPERATURE, "温度");
        zh(TEMPERATURE, "温度");

        ja(CFG_WEIGHT, "CFG ウェイト");
        zh(CFG_WEIGHT, "CFG 权重");

        ja(SPEED, "速度");
        zh(SPEED, "语速");

        ja(CHUNK_SIZE, "チャンクのサイズ (文字数)");
        zh(CHUNK_SIZE, "分块大小（字符数）");

        ja(BEFORE_A_BATCH, "バッチの前");
        zh(BEFORE_A_BATCH, "批处理之前");

        ja(AFTER_A_BATCH, "バッチの後");
        zh(AFTER_A_BATCH, "批处理之后");

        ja(REQUESTS_MADE_AROUND_A_BATCH, "パートのバッチの前後に順番に行うリクエスト。前: GPU を空けて音声サーバーを起動 (言語モデルのアンロード、コンテナの起動、モデルの読み込み)。後: 停止。なければ何もしません。");
        zh(REQUESTS_MADE_AROUND_A_BATCH, "在一批章节前后按顺序发出的请求。之前：释放 GPU 并启动语音服务器（卸载语言模型、启动容器、加载模型）。之后：将其停止。没有则不执行任何操作。");

        ja(ADD_A_REQUEST, "リクエストを追加");
        zh(ADD_A_REQUEST, "添加请求");

        ja(REMOVE_THE_REQUEST, "リクエストを削除");
        zh(REMOVE_THE_REQUEST, "删除请求");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");

        ja(WAIT_AFTERWARDS_SECONDS, "その後の待機 (秒)");
        zh(WAIT_AFTERWARDS_SECONDS, "之后等待（秒）");

        ja(METHOD, "メソッド");
        zh(METHOD, "方法");

        ja(HEADERS_NAME_VALUE_ONE_PER_LINE, "ヘッダー (名前: 値、1 行に 1 つ)");
        zh(HEADERS_NAME_VALUE_ONE_PER_LINE, "请求头（名称: 值，每行一个）");

        ja(BODY, "本文");
        zh(BODY, "正文");

        ja(TEST_CONNECTION, "接続をテスト");
        zh(TEST_CONNECTION, "测试连接");

        ja(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "サーバーは応答しました。モデルは読み込み済みです (%s)");
        zh(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "服务器已响应，模型已加载（%s）");

        ja(THE_SERVER_ANSWERS_NO_MODEL, "サーバーは応答しました。モデルは読み込まれていません (バッチの前のリクエストが読み込みます)");
        zh(THE_SERVER_ANSWERS_NO_MODEL, "服务器已响应，尚未加载模型（批处理前的请求会加载）");

        ja(CONNECTION_FAILED, "接続に失敗しました: %s");
        zh(CONNECTION_FAILED, "连接失败：%s");

        ja(NARRATOR_VOICE, "ナレーターの声");
        zh(NARRATOR_VOICE, "旁白声音");

        ja(NARRATOR_VOICE_HELP, "本にキャストがない、またはキャストにナレーターの声がない場合に使うナレーター。");
        zh(NARRATOR_VOICE_HELP, "书没有角色分配、或其中没有旁白声音时使用的旁白。");

        ja(CONVERT_TO_AUDIO, "音声に変換");
        zh(CONVERT_TO_AUDIO, "转换为音频");

        ja(BACKEND, "バックエンド");
        zh(BACKEND, "后端");

        ja(FIRST_PART, "最初のパート");
        zh(FIRST_PART, "起始章节");

        ja(LAST_PART, "最後のパート");
        zh(LAST_PART, "结束章节");

        ja(THE_BOOK_HAS_PARTS_WITH_TEXT, "この本には本文のあるパートが %s 個あります。最新のパートは作り直しません。");
        zh(THE_BOOK_HAS_PARTS_WITH_TEXT, "本书有 %s 个含文本的章节。已是最新的章节不会重新制作。");

        ja(START, "開始");
        zh(START, "开始");

        ja(STOP_ALL, "すべて停止");
        zh(STOP_ALL, "全部停止");

        ja(CLEAR_FINISHED, "完了したものを消去");
        zh(CLEAR_FINISHED, "清除已完成");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(PART, "パート");
        zh(PART, "章节");

        ja(STATE, "状態");
        zh(STATE, "状态");

        ja(PROGRESS, "進行状況");
        zh(PROGRESS, "进度");

        ja(MESSAGE, "メッセージ");
        zh(MESSAGE, "消息");

        ja(STATE_WAITING, "待機中");
        zh(STATE_WAITING, "等待中");

        ja(STATE_RUNNING, "実行中");
        zh(STATE_RUNNING, "进行中");

        ja(STATE_DONE, "完了");
        zh(STATE_DONE, "完成");

        ja(STATE_FAILED, "失敗");
        zh(STATE_FAILED, "失败");

        ja(STATE_CANCELLED, "中止");
        zh(STATE_CANCELLED, "已取消");

        ja(DOWNLOAD, "ダウンロード");
        zh(DOWNLOAD, "下载");

        ja(RETRY, "再試行");
        zh(RETRY, "重试");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(NO_BACKEND_YET, "バックエンドがありません。設定 → 拡張機能の設定 → オーディオブックの設定で追加してください。");
        zh(NO_BACKEND_YET, "尚无后端。请在 设置 → 扩展设置 → 有声书设置 中添加。");

        ja(THERE_IS_NO_NARRATOR_VOICE, "ナレーターの声がありません。キャスト、または 設定 → 拡張機能の設定 → オーディオブックの設定 で指定してください。");
        zh(THERE_IS_NO_NARRATOR_VOICE, "没有旁白声音。请在角色分配中，或在 设置 → 扩展设置 → 有声书设置 中选择。");

        ja(THE_BOOK_HAS_NO_TEXT, "本文のあるパートがありません");
        zh(THE_BOOK_HAS_NO_TEXT, "书中没有含文本的章节");

        ja(THE_PARTS_ARE_IN_THE_QUEUE, "%s 個のパートをキューに入れました");
        zh(THE_PARTS_ARE_IN_THE_QUEUE, "已将 %s 个章节加入队列");

        ja(THE_RANGE_IS_NOT_IN_THE_BOOK, "範囲が本にありません。本文のあるパートは %s 個です");
        zh(THE_RANGE_IS_NOT_IN_THE_BOOK, "范围超出本书：共有 %s 个含文本的章节");

        ja(THE_CONVERSION_COULD_NOT_BE_STARTED, "変換を開始できませんでした: %s");
        zh(THE_CONVERSION_COULD_NOT_BE_STARTED, "无法开始转换：%s");

        ja(LISTEN_TO_THE_AUDIOBOOK, "オーディオブックを聴く");
        zh(LISTEN_TO_THE_AUDIOBOOK, "收听有声书");

        ja(PAUSE_LISTENING, "一時停止");
        zh(PAUSE_LISTENING, "暂停");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(THIS_PART_HAS_NO_AUDIO, "このパートにはまだ音声がありません");
        zh(THIS_PART_HAS_NO_AUDIO, "此章节还没有音频");

        ja(THE_AUDIO_COULD_NOT_BE_LOADED, "音声を読み込めませんでした");
        zh(THE_AUDIO_COULD_NOT_BE_LOADED, "无法加载音频");

        ja(LABEL_WITH_A_LANGUAGE_MODEL, "言語モデルでパートにラベルを付ける (誰が話すか、話し方、効果音タグ)");
        zh(LABEL_WITH_A_LANGUAGE_MODEL, "用语言模型为章节加标注（谁在说话、语气、声音标签）");

        ja(THE_MODEL_THAT_LABELS, "ラベルを付けるモデル");
        zh(THE_MODEL_THAT_LABELS, "负责标注的模型");

        ja(THE_PARTS_ARE_LABELLED_FIRST, "先にパートにラベルを付け (モデルが読み込まれている必要があります)、その後で読み上げます。バッチ前のリクエストが音声のためにモデルをアンロードします。");
        zh(THE_PARTS_ARE_LABELLED_FIRST, "先为章节加标注（模型必须已加载），再朗读：批处理前的请求会为语音卸载该模型。");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(REMOVE, "削除");
        zh(REMOVE, "移除");

        ja(CAST, "キャスト");
        zh(CAST, "角色分配");





        ja(CHARACTERS, "登場人物");
        zh(CHARACTERS, "人物");

        ja(NARRATOR, "ナレーター");
        zh(NARRATOR, "旁白");

        ja(OTHER_SPEAKERS, "その他の話者");
        zh(OTHER_SPEAKERS, "其他说话者");

        ja(VOICE, "声");
        zh(VOICE, "声音");

        ja(WHEN, "場合");
        zh(WHEN, "场合");

        ja(MOOD_NAME, "感情: %s");
        zh(MOOD_NAME, "语气：%s");

        ja(LANGUAGE_NAME, "言語: %s");
        zh(LANGUAGE_NAME, "语言：%s");

        ja(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "感情や言語ごとの声を追加");
        zh(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "为语气或语言添加声音");

        ja(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "クリップ、または声のファイル (Henry.wav)");
        zh(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "参考片段，或声音文件 (Henry.wav)");

        ja(ADD_A_CLIP, "クリップを追加");
        zh(ADD_A_CLIP, "添加片段");

        ja(THE_CLIP_COULD_NOT_BE_ADDED, "クリップを追加できませんでした: %s");
        zh(THE_CLIP_COULD_NOT_BE_ADDED, "无法添加片段：%s");

        ja(ADD_A_CHARACTER, "登場人物を追加");
        zh(ADD_A_CHARACTER, "添加人物");

        ja(ADD_THE_CHARACTERS_OF_THE_BOOK, "本の登場人物を追加");
        zh(ADD_THE_CHARACTERS_OF_THE_BOOK, "添加本书的人物");

        ja(CHARACTERS_ADDED, "追加した登場人物: %s");
        zh(CHARACTERS_ADDED, "已添加人物：%s");

        ja(NEW_CHARACTER, "新しい登場人物");
        zh(NEW_CHARACTER, "新人物");

        ja(OTHER_NAMES, "別名");
        zh(OTHER_NAMES, "别名");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");



        ja(THE_CAST_COULD_NOT_BE_STORED, "キャストを保存できませんでした: %s");
        zh(THE_CAST_COULD_NOT_BE_STORED, "无法保存角色分配：%s");

        ja(THE_CAST_IS_STORED, "キャストを保存しました");
        zh(THE_CAST_IS_STORED, "角色分配已保存");







        ja(ACTOR, "声優");
        zh(ACTOR, "配役の声");

        ja(ACTORS, "声優");
        zh(ACTORS, "配役声音");

        ja(NEW_ACTOR, "新しい声優");
        zh(NEW_ACTOR, "新建声音");

        ja(EDIT_THE_ACTOR, "声優を編集");
        zh(EDIT_THE_ACTOR, "编辑声音");

        ja(DELETE_THE_ACTOR, "声優を削除");
        zh(DELETE_THE_ACTOR, "删除声音");

        ja(THE_ACTOR_IS_DELETED, "声優「%s」を削除します。これを指定しているキャストでは声がなくなり、その他の話者の声で読まれます。クリップはリソースに残ります。");
        zh(THE_ACTOR_IS_DELETED, "将删除声音“%s”。指定它的角色分配将不再有对应声音，改由其他说话者的声音朗读。片段仍保留在你的资源中。");

        ja(THE_ACTOR_COULD_NOT_BE_STORED, "声優を保存できませんでした: %s");
        zh(THE_ACTOR_COULD_NOT_BE_STORED, "无法保存声音：%s");

        ja(NOT_SET, "未設定");
        zh(NOT_SET, "未设置");

        ja(ACTORS_HELP, "声優はあなたの声です。参照クリップまたは音声サーバーの声のファイルで、感情や言語ごとの声も持てます。どの本でも、ナレーターと登場人物に声優を割り当てられます (オーディオに変換 → キャスト)。");
        zh(ACTORS_HELP, "声音（演员）属于你：参考片段或语音服务器的声音文件，还可为语气或语言设置各自的声音。任何一本书都可以为旁白和人物指定声音（转为音频 → 角色分配）。");

        ja(THE_CAST_HELP, "この本で誰が読むか: ナレーターと各登場人物に声優を割り当てます。声優はあなたの声です (設定 → 拡張機能の設定 → オーディオブックの設定)。ここで新しく作ることもできます。キャストは本と一緒に保存されます。");
        zh(THE_CAST_HELP, "这本书由谁朗读：为旁白和每个人物指定一个声音。声音是你自己的（设置 → 扩展设置 → 有声书设置），也可在此新建。角色分配随书保存。");

        ja(THE_FALLBACK_HELP, "モデルがキャストにない話者を示した場合に読む声優。未設定ならナレーターが読みます。声優のない登場人物も同様です。");
        zh(THE_FALLBACK_HELP, "当模型给出角色分配中没有的说话者时朗读的声音。未设置则由旁白朗读。没有指定声音的人物也一样。");

        ja(THE_ACTOR_HELP, "声は参照クリップ (音声リソース、または新規)、または音声サーバーの声のファイルです。感情や言語ごとに別の声を指定できます (ささやき、叫び、日本語を話すクリップ)。言語は感情より優先され、指定がなければ上の声で読みます。");
        zh(THE_ACTOR_HELP, "声音是参考片段（你的音频资源之一，或新上传的）或语音服务器自带声音的文件。语气或语言可以有各自的声音：耳语、喊叫、会说日语的片段；语言优先于语气，没有对应声音时用上面的声音朗读。");

        ja(AUDIOBOOK_STUDIO, "オーディオブックスタジオ");
        zh(AUDIOBOOK_STUDIO, "有声书工作室");

        ja(ANNOTATE, "注釈を付ける");
        zh(ANNOTATE, "标注");

        ja(ANNOTATING, "パートに注釈を付けています ...");
        zh(ANNOTATING, "正在标注该章节 ...");

        ja(A_FINAL_PART_IS_NOT_ANNOTATED, "最終版のパートは変更されません。先に状態を「確認済み」または「下書き」にしてください。");
        zh(A_FINAL_PART_IS_NOT_ANNOTATED, "定稿的章节不会被改动：请先把状态改为已审阅或草稿。");

        ja(MAKE_THE_PART, "このスクリプトでパートを作成");
        zh(MAKE_THE_PART, "按此脚本生成该章节");

        ja(MAKING_THE_PART, "パートを作成しています ...");
        zh(MAKING_THE_PART, "正在生成该章节 ...");

        ja(HEAR_THE_PART, "パートを聞く");
        zh(HEAR_THE_PART, "试听该章节");

        ja(THE_PART_HAS_NO_AUDIO_YET, "このパートにはまだ音声がありません");
        zh(THE_PART_HAS_NO_AUDIO_YET, "该章节还没有音频");

        ja(THIS_PART_HAS_NO_SCRIPT_YET, "このパートにはまだスクリプトがありません。注釈を付けてください。");
        zh(THIS_PART_HAS_NO_SCRIPT_YET, "该章节还没有脚本：请先标注。");

        ja(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "このスクリプトの作成後にパートの本文が変わりました。再度注釈を付けてください (変更したセグメントは保持されます)。");
        zh(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "自该脚本生成后章节文本已更改：请重新标注（你改过的片段会保留）。");

        ja(SPEAKER, "話者");
        zh(SPEAKER, "说话者");

        ja(SPOKEN_TEXT, "読み上げるテキスト");
        zh(SPOKEN_TEXT, "朗读文本");

        ja(DELIVERY, "話し方");
        zh(DELIVERY, "语气");

        ja(INTENSITY, "強さ");
        zh(INTENSITY, "强度");

        ja(EXAGGERATION, "誇張");
        zh(EXAGGERATION, "夸张度");

        ja(LANGUAGE, "言語");
        zh(LANGUAGE, "语言");

        ja(SEED, "シード");
        zh(SEED, "种子");

        ja(SEED_X, "シード %s");
        zh(SEED_X, "种子 %s");

        ja(PAUSE_AFTER_SECONDS, "後の間 (秒)");
        zh(PAUSE_AFTER_SECONDS, "之后停顿（秒）");

        ja(STATUS, "状態");
        zh(STATUS, "状态");

        ja(STATUS_DRAFT, "下書き");
        zh(STATUS_DRAFT, "草稿");

        ja(STATUS_REVIEWED, "確認済み");
        zh(STATUS_REVIEWED, "已审阅");

        ja(STATUS_FINAL, "最終版");
        zh(STATUS_FINAL, "定稿");

        ja(LOCKED, "固定");
        zh(LOCKED, "已锁定");

        ja(TAKES, "テイク");
        zh(TAKES, "录音");

        ja(TAKE, "テイク");
        zh(TAKE, "录音");

        ja(MADE, "作成済み");
        zh(MADE, "已生成");

        ja(HEAR, "聞く");
        zh(HEAR, "试听");

        ja(ANOTHER_TAKE, "別のテイク");
        zh(ANOTHER_TAKE, "再来一遍");

        ja(LOWER_THE_INTENSITY, "強さを下げる");
        zh(LOWER_THE_INTENSITY, "降低强度");

        ja(MORE_PAUSE, "間を長く");
        zh(MORE_PAUSE, "停顿更久");

        ja(UNLOCK, "固定を解除");
        zh(UNLOCK, "解除锁定");

        ja(APPLY, "適用");
        zh(APPLY, "应用");

        ja(CHOOSE_AND_HEAR_THE_TAKE, "このテイクを使って聞く");
        zh(CHOOSE_AND_HEAR_THE_TAKE, "采用此录音并试听");

        ja(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "手動で変更済み: 新しい注釈でもそのまま保持されます。");
        zh(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "已手动更改：重新标注时保持原样。");

        ja(AS_THE_ANNOTATION_MADE_IT, "注釈で作られたまま: 新しい注釈で置き換えられます。");
        zh(AS_THE_ANNOTATION_MADE_IT, "由标注生成：重新标注时会被替换。");

        ja(THE_REQUEST_TO_THE_SERVER, "音声サーバーに送るリクエスト");
        zh(THE_REQUEST_TO_THE_SERVER, "发给语音服务器的请求");

        ja(MAKING_THE_SEGMENT, "セグメントを作成しています (音声サーバーを起動します) ...");
        zh(MAKING_THE_SEGMENT, "正在生成该片段（会启动语音服务器）...");

        ja(THE_JOB_FAILED, "失敗しました: %s");
        zh(THE_JOB_FAILED, "失败：%s");

        ja(FINAL_PARTS_ARE_LEFT_ALONE, "状態が最終版のため変更しなかったパート: %s");
        zh(FINAL_PARTS_ARE_LEFT_ALONE, "因状态为定稿而未改动的章节：%s");

        ja(PRONUNCIATION, "発音");
        zh(PRONUNCIATION, "发音");

        ja(NUMBERS_AND_ABBREVIATIONS, "数字と略語");
        zh(NUMBERS_AND_ABBREVIATIONS, "数字和缩写");

        ja(RULES_ENGLISH, "英語: 数字、略語、番号付きの見出しを単語として読む");
        zh(RULES_ENGLISH, "英语：数字、缩写和带编号的标题按单词朗读");

        ja(RULES_NONE, "オフ: 辞書のみ (他の言語の本用)");
        zh(RULES_NONE, "关闭：只用词典（用于其他语言的书）");

        ja(WORD_BEFORE_A_CHAPTER_NUMBER, "章番号の前の語");
        zh(WORD_BEFORE_A_CHAPTER_NUMBER, "章节编号前的词");

        ja(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "番号だけの見出し用:「IV. The Arch」は「Chapter four. The Arch」と読まれます。空欄なら番号のみ。");
        zh(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "用于只有编号的标题：“IV. The Arch”读作“Chapter four. The Arch”。留空则只读编号。");

        ja(PRONUNCIATION_HELP, "本は変更されません。これは音声のための綴りです。辞書は単語全体 (大文字小文字は区別) を読み方に置き換えます: 名前や造語の綴り直し、日本語ならかな読み。読み上げが変わった部分だけが作り直されます。");
        zh(PRONUNCIATION_HELP, "书不会被改动：这是为朗读准备的拼写。词典按整词（区分大小写）替换成要读的内容：人名或自造词的改拼，日语的假名读法。读法改变的片段会重新生成，其余保留。");

        ja(DICTIONARY, "辞書");
        zh(DICTIONARY, "词典");

        ja(ADD_A_WORD, "単語を追加");
        zh(ADD_A_WORD, "添加词");

        ja(WORD_IN_THE_BOOK, "本の中の単語");
        zh(WORD_IN_THE_BOOK, "书中的词");

        ja(SAID_AS, "読み方");
        zh(SAID_AS, "读作");

        ja(TRY_A_TEXT, "テキストを試す");
        zh(TRY_A_TEXT, "试一段文字");

        ja(AS_A_HEADING, "見出しとして");
        zh(AS_A_HEADING, "作为标题");

        ja(IT_IS_SAID_AS, "読まれ方");
        zh(IT_IS_SAID_AS, "读出来是");

        ja(NOTHING_SCENE_BREAK, "(なし: 場面転換、間)");
        zh(NOTHING_SCENE_BREAK, "（无：场景分隔，停顿）");

        ja(THE_PRONUNCIATION_IS_STORED, "発音を保存しました");
        zh(THE_PRONUNCIATION_IS_STORED, "发音已保存");

        ja(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "発音を保存できませんでした: %s");
        zh(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "无法保存发音：%s");

        ja(IN_THE_BOOK, "本の中: %s");
        zh(IN_THE_BOOK, "书中：%s");

        ja(AUDIOBOOK_ZIP, "オーディオブック (章ごとに mp3、zip)");
        zh(AUDIOBOOK_ZIP, "有声书（每章一个 mp3，打包为 zip）");

        ja(AUDIOBOOK_ONE_MP3, "オーディオブック (章マーク付きの mp3 1 本)");
        zh(AUDIOBOOK_ONE_MP3, "有声书（带章节标记的单个 mp3）");
        ja(AUDIOBOOK_M4B, "オーディオブック (章付きの m4b 1 本)");
        zh(AUDIOBOOK_M4B, "有声书（带章节的单个 m4b）");
        ja(STORAGE_OF_THE_AUDIO, "音声の保存形式 (このインストール全体)");
        zh(STORAGE_OF_THE_AUDIO, "音频的存储格式（对整个安装生效）");
        ja(STORAGE_MP3, "mp3 (小さい。m4b は二重にエンコードされます)");
        zh(STORAGE_MP3, "mp3（体积小；m4b 会被二次编码）");
        ja(STORAGE_WAV, "wav (ロスレス、約10倍のサイズ。ffmpeg が必要)");
        zh(STORAGE_WAV, "wav（无损，约为十倍大小；需要 ffmpeg）");
        ja(STORAGE_OF_THE_AUDIO_HELP, "管理者にのみ表示されます。これから作成するパートに適用され、以前のパートはそのまま使われます。wav は mp3 と zip の書き出し時に ffmpeg でエンコードされ、m4b では一度だけエンコードされます。");
        zh(STORAGE_OF_THE_AUDIO_HELP, "仅管理员可见。仅对之后生成的章节生效，之前的章节保持原样。导出 mp3 和 zip 时由 ffmpeg 编码 wav，导出 m4b 时只编码一次。");
        ja(FFMPEG_IS_NOT_INSTALLED, "サーバーに ffmpeg がインストールされていません。mp3 のみ選択でき、m4b の書き出しは利用できません。");
        zh(FFMPEG_IS_NOT_INSTALLED, "服务器未安装 ffmpeg：只能选择 mp3，且不提供 m4b 导出。");
        ja(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "保存しました。新しいパートはこの形式で保存されます。");
        zh(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "已保存。新章节将按此方式存储。");

        ja(ONLY_THE_PARTS_MARKED_FINAL, "オーディオブック: 最終版のパートのみ");
        zh(ONLY_THE_PARTS_MARKED_FINAL, "有声书：只含已定稿的章节");

        ja(PART_NUMBER, "パート %s");
        zh(PART_NUMBER, "第 %s 部分");

        ja(NO_AUDIO, "音声なし");
        zh(NO_AUDIO, "没有音频");

        ja(AUDIO_OLDER_THAN_THE_TEXT, "音声が本文より古い");
        zh(AUDIO_OLDER_THAN_THE_TEXT, "音频比文本旧");

        ja(AUDIO_OLDER_THAN_THE_SCRIPT, "音声がスクリプトより古い");
        zh(AUDIO_OLDER_THAN_THE_SCRIPT, "音频比脚本旧");

        ja(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "オーディオブックから除外したパート: %s。先に変換 (またはスタジオで作り直し) してください。");
        zh(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "有声书中省略的章节：%s。请先转换（或在工作室中重新生成）。");

        ja(NO_PART_HAS_UP_TO_DATE_AUDIO, "最新の音声を持つパートがありません: %s。先にパートを変換してください (音声に変換)、または最終版のパートを選んでください。");
        zh(NO_PART_HAS_UP_TO_DATE_AUDIO, "没有章节拥有最新的音频：%s。请先转换章节（转为音频），或选择已定稿的章节。");

        ja(NO_PART_TO_EXPORT, "書き出すパートがありません: 最終版のパートがない (または範囲が空) です。");
        zh(NO_PART_TO_EXPORT, "没有可导出的章节：没有已定稿的章节（或范围为空）。");

        ja(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "新しいパートを自動で音声に変換");
        zh(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "自动把新章节转为音频");

        ja(AUTO_NO, "しない");
        zh(AUTO_NO, "否");

        ja(AUTO_PLAIN, "する: ナレーターが読む");
        zh(AUTO_PLAIN, "是：由旁白朗读");

        ja(AUTO_LABELLED, "する: 本のモデルでラベルを付け、キャストが声を当てる");
        zh(AUTO_LABELLED, "是：用书的模型标注，由角色分配提供声音");

        ja(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成されたパートが最終版になると (本に自動注釈と再執筆がある場合はその後)、オーディオブックのキューに入ります。生成が動いている間、音声は待機します。設定 → 拡張機能の設定 → オーディオブックの設定 で選んだ音声サーバーを使います。");
        zh(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成的章节定稿后（若书设有自动注释和重写，则在其完成后）会进入有声书队列。你有生成任务运行时，语音会等待。使用在 设置 → 扩展设置 → 有声书设置 中选定的语音服务器。");

        ja(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新しいパートをオーディオブックのキューに入れました");
        zh(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新章节已进入有声书队列");

        ja(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "本にラベル付け用のモデルがないため、ナレーターが新しいパートを読みます");
        zh(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "书没有用于标注的模型：由旁白朗读新章节");

        ja(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新しいパートの音声ができました");
        zh(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新章节的音频已生成");

        ja(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "新しいパートの音声を作成できませんでした: %s");
        zh(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "无法生成新章节的音频：%s");

        ja(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "指定のない本の新しいパートを自動変換");
        zh(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "未指定的书自动转换新章节");

        ja(PREVIEW, "試聴");
        zh(PREVIEW, "试听");

        ja(MAKING_THE_SAMPLE, "サンプルを作成中 ...");
        zh(MAKING_THE_SAMPLE, "正在生成样本 ...");

        ja(PICK_A_VOICE_FIRST, "先に声を選んでください");
        zh(PICK_A_VOICE_FIRST, "请先选择声音");

        ja(THE_VOICE_COULD_NOT_BE_HEARD, "声を再生できませんでした: %s");
        zh(THE_VOICE_COULD_NOT_BE_HEARD, "无法试听该声音：%s");

        ja(KEY_OF_THE_REQUESTS, "バッチ前後のリクエストのキー");
        zh(KEY_OF_THE_REQUESTS, "批处理前后请求的密钥");

        ja(KEY_OF_THE_REQUESTS_HELP, "バッチの前後のすべてのリクエストに Authorization: Bearer として付けられます。暗号化して保存され、再表示されないので、ヘッダーに書く必要はありません。空: 上の API キーを使います。");
        zh(KEY_OF_THE_REQUESTS_HELP, "作为 Authorization: Bearer 附加到批处理前后的每个请求，加密保存且不再显示，因此无需写入请求头。留空：使用上面的 API 密钥。");

        ja(THE_REQUEST_AFTER_THE_TEST_FAILED, "サーバーは応答しましたが、テスト後のリクエストが失敗しました: %s");
        zh(THE_REQUEST_AFTER_THE_TEST_FAILED, "服务器有响应，但测试后的请求失败：%s");

        ja(THE_REQUESTS_AROUND_THE_TEST_RAN, "テストの前後のリクエストを実行しました。");
        zh(THE_REQUESTS_AROUND_THE_TEST_RAN, "已执行测试前后的请求。");

        ja(TEST_HELP, "テストはバッチ前のリクエスト（停止中のサーバーを起動）を実行し、サーバーに問い合わせ、バッチ後のリクエストを実行します。");
        zh(TEST_HELP, "测试会先执行批处理前的请求（启动已停止的服务器），再询问服务器，最后执行批处理后的请求。");

        ja(DOWNLOAD_THE_AUDIOBOOK, "オーディオブック全体");
        zh(DOWNLOAD_THE_AUDIOBOOK, "整本有声书");

        ja(DOWNLOAD_THE_AUDIOBOOK_HELP, "最新の音声があるパートをすべて、パートごとの章を付けた1つのmp3にまとめ、ダウンロードできるようにします");
        zh(DOWNLOAD_THE_AUDIOBOOK_HELP, "把所有音频最新的章节合并为一个 mp3（每个章节一个章节标记），并提供下载");

        ja(THE_AUDIOBOOK_IS_BEING_JOINED, "オーディオブックを結合しています ...");
        zh(THE_AUDIOBOOK_IS_BEING_JOINED, "正在合并有声书 ...");

        ja(DOWNLOAD_THE_AUDIOBOOK_FILE, "オーディオブックをダウンロード（約 %s MB）");
        zh(DOWNLOAD_THE_AUDIOBOOK_FILE, "下载有声书（约 %s MB）");
    }
}
