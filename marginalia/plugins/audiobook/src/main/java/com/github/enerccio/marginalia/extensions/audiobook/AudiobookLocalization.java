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
    public static final String BACKENDS = "Backends";
    public static final String ADD_A_BACKEND = "Add a backend";
    public static final String DELETE_THE_BACKEND = "Delete the backend";
    public static final String NEW_BACKEND = "New backend";
    public static final String DUPLICATE_THE_BACKEND = "Duplicate the backend: the same address, keys, voice and requests before and after a batch, to change what differs (another model)";
    public static final String COPY_OF = "%s (copy)";
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
    public static final String MODEL_HIGGS = "Higgs Audio v3 in ComfyUI (emotions, sounds; non-commercial license)";
    public static final String COMFYUI_ADDRESS_HELP = "The address of ComfyUI with the TTS Audio Suite nodes, for example http://gpu-server:8188";
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
    public static final String VOICES_AND_CLIPS = "Voices and clips";
    public static final String VOICES_AND_CLIPS_HELP = "Your actors and the reference clips their voices are cloned from, in folders, with the versions made from a clip under it. A voice model copies the recording, not only the voice: a clip with background sound (a film, a room, music) gives every line a whoosh or a room. Use a clean recording with real pauses, or clean a clip here: with ffmpeg, or with AI on ComfyUI when a cleanup is set up in the settings.";
    public static final String CLIPS = "Clips";
    public static final String LENGTH = "Length";
    public static final String QUALITY = "Quality";
    public static final String USED_BY = "Used by";
    public static final String OTHER_VOICES = "Other voices";
    public static final String REPLACE = "Replace...";
    public static final String CLEAN_WITH_FFMPEG = "Clean with ffmpeg";
    public static final String CLEAN_WITH_FFMPEG_HELP = "Makes a cleaned copy (less steady hiss and hum) to hear and compare; music, effects and other voices of a film stay";
    public static final String DELETE_THE_CLIP = "Delete the clip";
    public static final String THE_CLIP_IS_USED = "The clip is used and can't be deleted: %s";
    public static final String THE_CLIP_IS_DELETED = "The clip %s is deleted.";
    public static final String GRADE_CLEAN = "Clean";
    public static final String GRADE_FAIR = "Fair";
    public static final String GRADE_POOR = "Poor";
    public static final String NOT_MEASURED = "Not measured (an mp3 needs ffmpeg)";
    public static final String PROBLEM_BACKGROUND = "background sound under the voice (its quietest parts are only %.0f dB under the speech): the generated lines will carry it";
    public static final String PROBLEM_SOME_BACKGROUND = "some background sound (its quietest parts are %.0f dB under the speech)";
    public static final String PROBLEM_CLIPPED = "distorted: the loudest parts are clipped";
    public static final String PROBLEM_TOO_SHORT = "short (%.0f s): 12 to 30 seconds clone better";
    public static final String PROBLEM_TOO_LONG = "long (%.0f s): Chatterbox uses only its first 30 seconds";
    public static final String REPLACE_THE_CLIP = "Replace the clip";
    public static final String REPLACE_WITH = "Replace it with";
    public static final String REPLACE_HELP = "Every voice of your actors and every narrator of your backends that uses the clip gets the new one; the parts are made again with it. The old clip stays until you delete it.";
    public static final String THE_CLIP_WAS_REPLACED = "%d voices now use %s.";
    public static final String THE_CLEANED_CLIP_IS_READY = "The cleaned version %s is under its clip (%s). Play it, and use it in place of the clip if it sounds better.";
    public static final String THE_CLIP_COULD_NOT_BE_CLEANED = "The clip could not be cleaned: %s";
    public static final String A_CLIP_WITH_PROBLEMS = "%s: %s. For the best result clean the recording by hand, or clean it with ffmpeg in Voices and clips (Audiobook Settings).";
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

    public static final String OTHER_SAMPLES = "Other samples: with sounds, in a mood";
    public static final String READ_PLAINLY = "Read plainly";
    public static final String WITH_SOUNDS = "With sounds (sigh, laugh, gasp)";
    public static final String SPEECH_SERVERS_HELP = "The speech servers the audiobook is made by. A server on a GPU shared with the language model names the GPU lock that loads it.";
    public static final String REQUESTS_MADE_AROUND_EACH_BATCH = "Requests made before and after each batch of parts (and each sample), one after the other, inside the GPU lock. Freeing the GPU and loading the speech server is the job of the GPU lock; use these for anything else. None does nothing.";
    public static final String PRECISION = "Precision";
    public static final String PRECISION_HELP = "The precision Higgs computes in. fp32 is the safe choice: bf16 gave gibberish and wrong voices on an AMD card. bf16 needs about half the memory.";
    public static final String CHECK_THE_LENGTH_OF_PIECES = "Check the length of the pieces";
    public static final String CHECK_THE_LENGTH_HELP = "A piece far longer than its text (the model went on talking, or made a sound instead of the words) is made again with another seed.";
    public static final String CHECK_THE_WORDS = "Check the words with speech recognition";
    public static final String CHECK_THE_WORDS_HELP = "After each batch every piece is transcribed and compared with its text; the pieces that differ are made again with another seed. Needs the speech recognition of the settings.";
    public static final String MAX_WORD_ERRORS = "Max. word errors (%)";
    public static final String RETRIES = "Retries";
    public static final String SEND_THE_CLIP_TEXT = "Send the text of the clip to Higgs";
    public static final String SEND_THE_CLIP_TEXT_HELP = "Experimental, keep it off: Higgs gets the words spoken in the voice clip with it. In tests it changed the voice.";
    public static final String SPEECH_RECOGNITION = "Speech recognition";
    public static final String SPEECH_RECOGNITION_HELP = "Writes down what a piece of speech says: it checks the pieces of a batch and recognises the text of a voice clip. ComfyUI with the TTS Audio Suite (Granite ASR), or an OpenAI-compatible transcription server such as a whisper server.";
    public static final String RECOGNIZER_COMFYUI = "ComfyUI (Granite ASR)";
    public static final String RECOGNIZER_OPENAI = "OpenAI-compatible (whisper)";
    public static final String RECOGNITION_MODEL = "Model of the recognition";
    public static final String TEXT_OF_THE_CLIP = "Text of the clip";
    public static final String TEXT_OF_THE_CLIP_HELP = "The words spoken in the clip, typed or recognised.";
    public static final String RECOGNIZE = "Recognize";
    public static final String RECOGNIZING = "Recognizing ...";
    public static final String NO_SPEECH_RECOGNITION = "Set up the speech recognition in the Audiobook settings first.";
    public static final String THE_SPEECH_RECOGNITION_WORKS = "The speech recognition works.";
    public static final String RECOGNITION_FAILED = "Recognizing failed: %s";
    public static final String THE_TEXT_OF_THE_CLIP_IS_SAVED = "The text of the clip is saved.";
    public static final String CLIP = "Clip";
    public static final String PICK_A_CLIP = "Pick a clip in Clips.";
    public static final String FOLDER = "Folder";
    public static final String NO_FOLDER = "No folder";
    public static final String A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL = "A version is in the folder of the clip it was made from.";
    public static final String MADE_FROM = "Made from %s with %s";
    public static final String NOT_USED = "Not used";
    public static final String USE_THIS_VERSION = "Use this version";
    public static final String USE_THIS_VERSION_HELP = "Puts this version wherever the clip it was made from is used";
    public static final String USE_THIS_VERSION_CONFIRM = "Use %s wherever %s is used? The lines made with the old clip are made again.";
    public static final String CLEAN_WITH_AI = "Clean with AI";
    public static final String CLEAN_WITH_AI_HELP = "Makes a new version of the clip with the chosen cleanup on ComfyUI (separation models: voice from music, noise, echo)";
    public static final String CLEANING = "Cleaning…";
    public static final String NO_CLEANUP = "No cleanup of clips is set up (Audiobook settings).";
    public static final String CLEANUP_OF_CLIPS = "Cleanup of clips";
    public static final String CLEANUP_OF_CLIPS_HELP = "A ComfyUI with the TTS Audio Suite cleans reference clips: a preset runs its steps one after the other (a separation model keeps the voice and drops music, noise or echo; the voice fixer restores a degraded voice; ffmpeg lowers a steady hiss). The result is a new version of the clip, the clip itself stays. The models are downloaded by ComfyUI on first use.";
    public static final String CLEANUP_GPU_LOCK_HELP = "Give the cleanup a lock of its own, not the one of the speech or the speech recognition: its steps should restart ComfyUI, so that its models are not loaded next to theirs.";
    public static final String CLEANUP_PRESET = "Cleanup";
    public static final String NEW_PRESET = "New cleanup";
    public static final String DELETE_THE_PRESET = "Delete the cleanup";
    public static final String DEFAULT_PRESETS = "Default cleanups";
    public static final String ADD_A_STEP = "Add a step";
    public static final String REMOVE_THE_STEP = "Remove the step";
    public static final String STEP_SEPARATE = "Separation model";
    public static final String STEP_VOICE_FIXER = "Voice fixer";
    public static final String STEP_FFMPEG = "ffmpeg (here)";
    public static final String SEPARATION_MODEL = "Model";
    public static final String KEEP_THE_EXTRACTED = "Keep the extracted";
    public static final String KEEP_THE_REMAINING = "Keep the remaining";
    public static final String AGGRESSIVENESS = "Aggressiveness (0–20)";
    public static final String FIXER_MODE_ORIGINAL = "Restore (default)";
    public static final String FIXER_MODE_HIGH_FREQUENCIES = "Restore, high frequencies removed";
    public static final String FIXER_MODE_DEGRADED = "Seriously degraded voice";
    public static final String THE_CLEANUP_WORKS = "The cleanup works: ComfyUI offers %d separation models.";
    public static final String KEEP_HELP = "Which output holds the voice depends on the model: a vocals model extracts the voice, a denoise model extracts the noise (keep the remaining). Listen to the result once.";
    public static final String FROM_SECONDS = "From (s)";
    public static final String TO_SECONDS = "To (s)";
    public static final String TAKE_THE_PLACE_OF_THE_PLAYER = "Take the place of the player";
    public static final String KEEP_THIS_PART = "Keep this part";
    public static final String CUT_THIS_PART_OUT = "Cut this part out";
    public static final String TRIM = "Trim";
    public static final String TRIM_HELP = "Makes a version of the clip with only this part, or without it (a sound no cleanup removes: an animal, a cough, a door). Find the place with the player.";
    public static final String GIVE_A_RANGE = "Give where the part starts and ends (From before To).";
    public static final String THE_CLIP_COULD_NOT_BE_TRIMMED = "The clip could not be trimmed: %s";
    public static final String VERSION_PART = "part %s";
    public static final String VERSION_WITHOUT = "without %s";
    public static final String GPU_LOCK_HELP = "The GPU lock the administrator set up for the GPU of this server: every batch and sample waits for its turn on the GPU, and the lock's steps load the server first (freeing the language model or another server when needed). Empty: no lock.";

    @Override
    protected void loadKeys() {
        ja(AUDIOBOOK, "オーディオブック");
        zh(AUDIOBOOK, "有声书");
        fr(AUDIOBOOK, "Livre audio");
        es(AUDIOBOOK, "Audiolibro");
        de(AUDIOBOOK, "Hörbuch");
        ko(AUDIOBOOK, "오디오북");
        it(AUDIOBOOK, "Audiolibro");

        ja(AUDIOBOOK_SETTINGS, "オーディオブックの設定");
        zh(AUDIOBOOK_SETTINGS, "有声书设置");
        fr(AUDIOBOOK_SETTINGS, "Paramètres du livre audio");
        es(AUDIOBOOK_SETTINGS, "Ajustes del audiolibro");
        de(AUDIOBOOK_SETTINGS, "Hörbuch-Einstellungen");
        ko(AUDIOBOOK_SETTINGS, "오디오북 설정");
        it(AUDIOBOOK_SETTINGS, "Impostazioni dell'audiolibro");

        ja(BACKENDS, "バックエンド");
        zh(BACKENDS, "后端");
        fr(BACKENDS, "Moteurs");
        es(BACKENDS, "Backends");
        de(BACKENDS, "Backends");
        ko(BACKENDS, "백엔드");
        it(BACKENDS, "Backend");

        ja(ADD_A_BACKEND, "バックエンドを追加");
        zh(ADD_A_BACKEND, "添加后端");
        fr(ADD_A_BACKEND, "Ajouter un moteur");
        es(ADD_A_BACKEND, "Añadir un backend");
        de(ADD_A_BACKEND, "Ein Backend hinzufügen");
        ko(ADD_A_BACKEND, "백엔드 추가");
        it(ADD_A_BACKEND, "Aggiungi un backend");

        ja(DELETE_THE_BACKEND, "バックエンドを削除");
        zh(DELETE_THE_BACKEND, "删除后端");
        fr(DELETE_THE_BACKEND, "Supprimer le moteur");
        es(DELETE_THE_BACKEND, "Eliminar el backend");
        de(DELETE_THE_BACKEND, "Das Backend löschen");
        ko(DELETE_THE_BACKEND, "백엔드 삭제");
        it(DELETE_THE_BACKEND, "Elimina il backend");

        ja(NEW_BACKEND, "新しいバックエンド");
        zh(NEW_BACKEND, "新后端");
        fr(NEW_BACKEND, "Nouveau moteur");
        es(NEW_BACKEND, "Nuevo backend");
        de(NEW_BACKEND, "Neues Backend");
        ko(NEW_BACKEND, "새 백엔드");
        it(NEW_BACKEND, "Nuovo backend");

        ja(DUPLICATE_THE_BACKEND, "バックエンドを複製: 同じアドレス、キー、声、バッチ前後のリクエストで、違う部分 (別のモデル) だけを変更します");
        zh(DUPLICATE_THE_BACKEND, "复制后端：相同的地址、密钥、声音以及批处理前后的请求，只需修改不同之处（另一个模型）");
        fr(DUPLICATE_THE_BACKEND, "Dupliquer le moteur : même adresse, mêmes clés, même voix et mêmes requêtes avant et après un lot, pour ne changer que ce qui diffère (un autre modèle)");
        es(DUPLICATE_THE_BACKEND, "Duplicar el backend: la misma dirección, claves, voz y solicitudes antes y después de un lote, para cambiar solo lo que difiere (otro modelo)");
        de(DUPLICATE_THE_BACKEND, "Backend duplizieren: dieselbe Adresse, dieselben Schlüssel, dieselbe Stimme und dieselben Anfragen vor und nach einem Stapel, um nur das Abweichende zu ändern (ein anderes Modell)");
        ko(DUPLICATE_THE_BACKEND, "백엔드 복제: 같은 주소, 키, 목소리, 배치 전후 요청으로 다른 부분(다른 모델)만 바꿉니다");
        it(DUPLICATE_THE_BACKEND, "Duplica il backend: stesso indirizzo, chiavi, voce e richieste prima e dopo un batch, per cambiare solo ciò che differisce (un altro modello)");

        ja(COPY_OF, "%s (コピー)");
        zh(COPY_OF, "%s（副本）");
        fr(COPY_OF, "%s (copie)");
        es(COPY_OF, "%s (copia)");
        de(COPY_OF, "%s (Kopie)");
        ko(COPY_OF, "%s (사본)");
        it(COPY_OF, "%s (copia)");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");

        ja(ADDRESS, "アドレス");
        zh(ADDRESS, "地址");
        fr(ADDRESS, "Adresse");
        es(ADDRESS, "Dirección");
        de(ADDRESS, "Adresse");
        ko(ADDRESS, "주소");
        it(ADDRESS, "Indirizzo");

        ja(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "音声合成サーバー (Chatterbox-TTS-Server) のベースアドレス (例: http://localhost:8004)、またはプロキシが転送するアドレス");
        zh(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "语音合成服务器（Chatterbox-TTS-Server）的基础地址，例如 http://localhost:8004，或代理转发到它的地址");
        fr(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Adresse de base du serveur de synthèse vocale (Chatterbox-TTS-Server), par exemple http://localhost:8004, ou l'adresse vers laquelle votre proxy le redirige");
        es(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Dirección base del servidor de voz (Chatterbox-TTS-Server), por ejemplo http://localhost:8004, o la dirección a la que lo reenvía tu proxy");
        de(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Basisadresse des Sprachservers (Chatterbox-TTS-Server), zum Beispiel http://localhost:8004, oder die Adresse, an die dein Proxy weiterleitet");
        ko(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "음성 서버(Chatterbox-TTS-Server)의 기본 주소, 예: http://localhost:8004 또는 프록시가 이를 전달하는 주소");
        it(BASE_ADDRESS_OF_THE_SPEECH_SERVER, "Indirizzo di base del server vocale (Chatterbox-TTS-Server), ad esempio http://localhost:8004, oppure l'indirizzo a cui il tuo proxy lo inoltra");

        ja(API_KEY, "API キー");
        zh(API_KEY, "API 密钥");
        fr(API_KEY, "Clé d'API");
        es(API_KEY, "Clave de API");
        de(API_KEY, "API-Schlüssel");
        ko(API_KEY, "API 키");
        it(API_KEY, "Chiave API");

        ja(STORED_TYPE_TO_REPLACE, "保存済み。置き換えるには入力してください。");
        zh(STORED_TYPE_TO_REPLACE, "已保存。输入新密钥以替换。");
        fr(STORED_TYPE_TO_REPLACE, "Enregistrée. Tapez pour la remplacer.");
        es(STORED_TYPE_TO_REPLACE, "Guardada. Escribe para reemplazarla.");
        de(STORED_TYPE_TO_REPLACE, "Gespeichert. Tippe, um ihn zu ersetzen.");
        ko(STORED_TYPE_TO_REPLACE, "저장됨. 입력하면 교체됩니다.");
        it(STORED_TYPE_TO_REPLACE, "Memorizzata. Digita per sostituirla.");

        ja(REMOVE_THE_KEY, "キーを削除");
        zh(REMOVE_THE_KEY, "删除密钥");
        fr(REMOVE_THE_KEY, "Retirer la clé");
        es(REMOVE_THE_KEY, "Quitar la clave");
        de(REMOVE_THE_KEY, "Den Schlüssel entfernen");
        ko(REMOVE_THE_KEY, "키 제거");
        it(REMOVE_THE_KEY, "Rimuovi la chiave");

        ja(SPEECH_MODEL, "音声モデル");
        zh(SPEECH_MODEL, "语音模型");
        fr(SPEECH_MODEL, "Modèle de synthèse vocale");
        es(SPEECH_MODEL, "Modelo de voz");
        de(SPEECH_MODEL, "Sprachmodell");
        ko(SPEECH_MODEL, "음성 모델");
        it(SPEECH_MODEL, "Modello vocale");

        ja(MODEL_TURBO, "Chatterbox Turbo (英語、音のタグ)");
        zh(MODEL_TURBO, "Chatterbox Turbo（英语，声音标签）");
        fr(MODEL_TURBO, "Chatterbox Turbo (anglais, balises sonores)");
        es(MODEL_TURBO, "Chatterbox Turbo (inglés, etiquetas de sonido)");
        de(MODEL_TURBO, "Chatterbox Turbo (Englisch, Sound-Tags)");
        ko(MODEL_TURBO, "Chatterbox Turbo (영어, 사운드 태그)");
        it(MODEL_TURBO, "Chatterbox Turbo (inglese, tag sonori)");

        ja(MODEL_ORIGINAL, "Chatterbox (英語、感情の強さ)");
        zh(MODEL_ORIGINAL, "Chatterbox（英语，情感强度）");
        fr(MODEL_ORIGINAL, "Chatterbox (anglais, exagération)");
        es(MODEL_ORIGINAL, "Chatterbox (inglés, exageración)");
        de(MODEL_ORIGINAL, "Chatterbox (Englisch, Exaggeration)");
        ko(MODEL_ORIGINAL, "Chatterbox (영어, 과장도)");
        it(MODEL_ORIGINAL, "Chatterbox (inglese, esagerazione)");

        ja(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 言語)");
        zh(MODEL_MULTILINGUAL, "Chatterbox Multilingual（23 种语言）");
        fr(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 langues)");
        es(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 idiomas)");
        de(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 Sprachen)");
        ko(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23개 언어)");
        it(MODEL_MULTILINGUAL, "Chatterbox Multilingual (23 lingue)");

        ja(MODEL_HIGGS, "ComfyUI の Higgs Audio v3 (感情、音。非商用ライセンス)");
        zh(MODEL_HIGGS, "ComfyUI 中的 Higgs Audio v3（情感、声音；非商业许可）");
        fr(MODEL_HIGGS, "Higgs Audio v3 dans ComfyUI (émotions, sons ; licence non commerciale)");
        es(MODEL_HIGGS, "Higgs Audio v3 en ComfyUI (emociones, sonidos; licencia no comercial)");
        de(MODEL_HIGGS, "Higgs Audio v3 in ComfyUI (Emotionen, Geräusche; nicht-kommerzielle Lizenz)");
        ko(MODEL_HIGGS, "ComfyUI의 Higgs Audio v3 (감정, 소리; 비상업용 라이선스)");
        it(MODEL_HIGGS, "Higgs Audio v3 in ComfyUI (emozioni, suoni; licenza non commerciale)");

        ja(COMFYUI_ADDRESS_HELP, "TTS Audio Suite のノードがある ComfyUI のアドレス。例: http://gpu-server:8188");
        zh(COMFYUI_ADDRESS_HELP, "装有 TTS Audio Suite 节点的 ComfyUI 地址，例如 http://gpu-server:8188");
        fr(COMFYUI_ADDRESS_HELP, "L'adresse de ComfyUI avec les nœuds de TTS Audio Suite, par exemple http://gpu-server:8188");
        es(COMFYUI_ADDRESS_HELP, "La dirección de ComfyUI con los nodos de TTS Audio Suite, por ejemplo http://gpu-server:8188");
        de(COMFYUI_ADDRESS_HELP, "Die Adresse von ComfyUI mit den Nodes der TTS Audio Suite, zum Beispiel http://gpu-server:8188");
        ko(COMFYUI_ADDRESS_HELP, "TTS Audio Suite 노드가 있는 ComfyUI 주소, 예: http://gpu-server:8188");
        it(COMFYUI_ADDRESS_HELP, "L'indirizzo di ComfyUI con i nodi di TTS Audio Suite, per esempio http://gpu-server:8188");

        ja(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "バッチの前のリクエストが読み込んだ状態にしておくモデル。別のモデルが必要な本は別のバックエンドを選びます");
        zh(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "批处理前的请求应让服务器加载的模型；需要其他模型的书请选用另一个后端");
        fr(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "Le modèle que les requêtes avant un lot doivent laisser chargé ; un livre qui en a besoin d'un autre choisit un autre moteur");
        es(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "El modelo que las solicitudes anteriores a un lote deben dejar cargado; un libro que necesite otro elige otro backend");
        de(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "Das Modell, das die Anfragen vor einem Stapel geladen lassen müssen; ein Buch, das ein anderes braucht, wählt ein anderes Backend");
        ko(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "배치 전의 요청이 로드된 상태로 두어야 하는 모델. 다른 모델이 필요한 책은 다른 백엔드를 선택합니다");
        it(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD, "Il modello che le richieste prima di un batch devono lasciare caricato; un libro che ne richiede un altro sceglie un altro backend");

        ja(THE_SERVER_IS_LOCAL, "サーバーはこのマシンまたはローカルネットワークにあります");
        zh(THE_SERVER_IS_LOCAL, "服务器在本机或我的网络中");
        fr(THE_SERVER_IS_LOCAL, "Le serveur est sur cette machine ou sur mon réseau");
        es(THE_SERVER_IS_LOCAL, "El servidor está en esta máquina o en mi red");
        de(THE_SERVER_IS_LOCAL, "Der Server steht auf diesem Rechner oder in meinem Netzwerk");
        ko(THE_SERVER_IS_LOCAL, "서버가 이 컴퓨터나 내 네트워크에 있습니다");
        it(THE_SERVER_IS_LOCAL, "Il server è su questa macchina o nella mia rete");

        ja(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "ローカルネットワークのアドレスを許可します。クラウドのサーバーではオフのままにします");
        zh(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "允许本地网络地址；云端服务器请保持关闭");
        fr(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Autorise les adresses du réseau local ; laissez-le désactivé pour un serveur dans le cloud");
        es(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Permite las direcciones de la red local; déjalo desactivado para un servidor en la nube");
        de(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Erlaubt die Adressen des lokalen Netzwerks; lass es für einen Server in der Cloud aus");
        ko(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "로컬 네트워크 주소를 허용합니다. 클라우드 서버라면 꺼 두세요");
        it(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "Consente gli indirizzi della rete locale; lascia disattivato per un server nel cloud");

        ja(TIMEOUT_SECONDS, "リクエストのタイムアウト (秒)");
        zh(TIMEOUT_SECONDS, "请求超时（秒）");
        fr(TIMEOUT_SECONDS, "Délai d'attente d'une requête (s)");
        es(TIMEOUT_SECONDS, "Tiempo de espera de una solicitud (s)");
        de(TIMEOUT_SECONDS, "Zeitlimit einer Anfrage (s)");
        ko(TIMEOUT_SECONDS, "요청 제한 시간 (초)");
        it(TIMEOUT_SECONDS, "Timeout di una richiesta (s)");
        ja(RESTART_THE_SERVER_AFTER_PIECES, "サーバーを再起動するまでのピース数");
        zh(RESTART_THE_SERVER_AFTER_PIECES, "每隔多少片段重启服务器");
        fr(RESTART_THE_SERVER_AFTER_PIECES, "Redémarrer le serveur après (morceaux)");
        es(RESTART_THE_SERVER_AFTER_PIECES, "Reiniciar el servidor tras (fragmentos)");
        de(RESTART_THE_SERVER_AFTER_PIECES, "Server neu starten nach (Stücke)");
        ko(RESTART_THE_SERVER_AFTER_PIECES, "다음 조각 수 후 서버 다시 시작");
        it(RESTART_THE_SERVER_AFTER_PIECES, "Riavvia il server dopo (pezzi)");
        ja(RESTART_THE_SERVER_HELP, "0 = 再起動しない。メモリリークするサーバー向けに、指定した数のピースごとにバッチの途中でバッチ後・バッチ前のリクエストをもう一度実行します。停止したサーバーはこの設定に関係なく再起動されます。");
        zh(RESTART_THE_SERVER_HELP, "0 = 从不。针对内存泄漏的服务器：每处理这么多片段，就在批处理中途重新执行批处理之后和之前的请求。服务器崩溃时无论如何设置都会重启。");
        fr(RESTART_THE_SERVER_HELP, "0 = jamais. Les requêtes d'après et d'avant un lot sont refaites au milieu d'un lot tous les tant de morceaux, pour un serveur qui fuit de la mémoire. Un serveur qui meurt est redémarré quel que soit ce réglage.");
        es(RESTART_THE_SERVER_HELP, "0 = nunca. Las solicitudes posteriores y anteriores a un lote se vuelven a hacer en medio de un lote cada cierto número de fragmentos, para un servidor que pierde memoria. Un servidor que se cae se reinicia digas lo que digas aquí.");
        de(RESTART_THE_SERVER_HELP, "0 = nie. Die Anfragen nach und vor einem Stapel werden mitten in einem Stapel alle so viele Stücke erneut ausgeführt, für einen Server, der Speicher verliert. Ein Server, der abstürzt, wird unabhängig davon neu gestartet.");
        ko(RESTART_THE_SERVER_HELP, "0 = 다시 시작하지 않음. 메모리가 누수되는 서버를 위해 배치 도중 이 조각 수마다 배치 전후의 요청을 다시 실행합니다. 죽은 서버는 이 설정과 관계없이 다시 시작됩니다.");
        it(RESTART_THE_SERVER_HELP, "0 = mai. Le richieste dopo e prima di un batch vengono rifatte a metà di un batch ogni tot pezzi, per un server che perde memoria. Un server che muore viene riavviato qualunque cosa dica questa opzione.");

        ja(DEFAULTS_OF_A_SEGMENT, "セグメントの初期値");
        zh(DEFAULTS_OF_A_SEGMENT, "片段的默认值");
        fr(DEFAULTS_OF_A_SEGMENT, "Valeurs par défaut d'un segment");
        es(DEFAULTS_OF_A_SEGMENT, "Valores predeterminados de un segmento");
        de(DEFAULTS_OF_A_SEGMENT, "Standardwerte eines Segments");
        ko(DEFAULTS_OF_A_SEGMENT, "세그먼트 기본값");
        it(DEFAULTS_OF_A_SEGMENT, "Valori predefiniti di un segmento");

        ja(TEMPERATURE, "温度");
        zh(TEMPERATURE, "温度");
        fr(TEMPERATURE, "Température");
        es(TEMPERATURE, "Temperatura");
        de(TEMPERATURE, "Temperatur");
        ko(TEMPERATURE, "온도");
        it(TEMPERATURE, "Temperatura");

        ja(CFG_WEIGHT, "CFG ウェイト");
        zh(CFG_WEIGHT, "CFG 权重");
        fr(CFG_WEIGHT, "Poids CFG");
        es(CFG_WEIGHT, "Peso CFG");
        de(CFG_WEIGHT, "CFG-Gewicht");
        ko(CFG_WEIGHT, "CFG 가중치");
        it(CFG_WEIGHT, "Peso CFG");

        ja(SPEED, "速度");
        zh(SPEED, "语速");
        fr(SPEED, "Vitesse");
        es(SPEED, "Velocidad");
        de(SPEED, "Geschwindigkeit");
        ko(SPEED, "속도");
        it(SPEED, "Velocità");

        ja(CHUNK_SIZE, "チャンクのサイズ (文字数)");
        zh(CHUNK_SIZE, "分块大小（字符数）");
        fr(CHUNK_SIZE, "Taille des morceaux (caractères)");
        es(CHUNK_SIZE, "Tamaño del fragmento (caracteres)");
        de(CHUNK_SIZE, "Chunk-Größe (Zeichen)");
        ko(CHUNK_SIZE, "청크 크기 (글자)");
        it(CHUNK_SIZE, "Dimensione dei blocchi (caratteri)");

        ja(BEFORE_A_BATCH, "バッチの前");
        zh(BEFORE_A_BATCH, "批处理之前");
        fr(BEFORE_A_BATCH, "Avant un lot");
        es(BEFORE_A_BATCH, "Antes de un lote");
        de(BEFORE_A_BATCH, "Vor einem Stapel");
        ko(BEFORE_A_BATCH, "배치 전");
        it(BEFORE_A_BATCH, "Prima di un batch");

        ja(AFTER_A_BATCH, "バッチの後");
        zh(AFTER_A_BATCH, "批处理之后");
        fr(AFTER_A_BATCH, "Après un lot");
        es(AFTER_A_BATCH, "Después de un lote");
        de(AFTER_A_BATCH, "Nach einem Stapel");
        ko(AFTER_A_BATCH, "배치 후");
        it(AFTER_A_BATCH, "Dopo un batch");

        ja(ADD_A_REQUEST, "リクエストを追加");
        zh(ADD_A_REQUEST, "添加请求");
        fr(ADD_A_REQUEST, "Ajouter une requête");
        es(ADD_A_REQUEST, "Añadir una solicitud");
        de(ADD_A_REQUEST, "Eine Anfrage hinzufügen");
        ko(ADD_A_REQUEST, "요청 추가");
        it(ADD_A_REQUEST, "Aggiungi una richiesta");

        ja(REMOVE_THE_REQUEST, "リクエストを削除");
        zh(REMOVE_THE_REQUEST, "删除请求");
        fr(REMOVE_THE_REQUEST, "Retirer la requête");
        es(REMOVE_THE_REQUEST, "Quitar la solicitud");
        de(REMOVE_THE_REQUEST, "Die Anfrage entfernen");
        ko(REMOVE_THE_REQUEST, "요청 제거");
        it(REMOVE_THE_REQUEST, "Rimuovi la richiesta");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");
        fr(REQUEST, "Requête");
        es(REQUEST, "Solicitud");
        de(REQUEST, "Anfrage");
        ko(REQUEST, "요청");
        it(REQUEST, "Richiesta");

        ja(WAIT_AFTERWARDS_SECONDS, "その後の待機 (秒)");
        zh(WAIT_AFTERWARDS_SECONDS, "之后等待（秒）");
        fr(WAIT_AFTERWARDS_SECONDS, "Attendre ensuite (s)");
        es(WAIT_AFTERWARDS_SECONDS, "Esperar después (s)");
        de(WAIT_AFTERWARDS_SECONDS, "Danach warten (s)");
        ko(WAIT_AFTERWARDS_SECONDS, "이후 대기 (초)");
        it(WAIT_AFTERWARDS_SECONDS, "Attendi dopo (s)");

        ja(METHOD, "メソッド");
        zh(METHOD, "方法");
        fr(METHOD, "Méthode");
        es(METHOD, "Método");
        de(METHOD, "Methode");
        ko(METHOD, "메서드");
        it(METHOD, "Metodo");

        ja(HEADERS_NAME_VALUE_ONE_PER_LINE, "ヘッダー (名前: 値、1 行に 1 つ)");
        zh(HEADERS_NAME_VALUE_ONE_PER_LINE, "请求头（名称: 值，每行一个）");
        fr(HEADERS_NAME_VALUE_ONE_PER_LINE, "En-têtes (Nom: valeur, un par ligne)");
        es(HEADERS_NAME_VALUE_ONE_PER_LINE, "Cabeceras (Nombre: valor, una por línea)");
        de(HEADERS_NAME_VALUE_ONE_PER_LINE, "Header (Name: Wert, einer pro Zeile)");
        ko(HEADERS_NAME_VALUE_ONE_PER_LINE, "헤더 (이름: 값, 한 줄에 하나)");
        it(HEADERS_NAME_VALUE_ONE_PER_LINE, "Intestazioni (Nome: valore, una per riga)");

        ja(BODY, "本文");
        zh(BODY, "正文");
        fr(BODY, "Corps");
        es(BODY, "Cuerpo");
        de(BODY, "Body");
        ko(BODY, "본문");
        it(BODY, "Corpo");

        ja(TEST_CONNECTION, "接続をテスト");
        zh(TEST_CONNECTION, "测试连接");
        fr(TEST_CONNECTION, "Tester la connexion");
        es(TEST_CONNECTION, "Probar la conexión");
        de(TEST_CONNECTION, "Verbindung testen");
        ko(TEST_CONNECTION, "연결 테스트");
        it(TEST_CONNECTION, "Prova la connessione");

        ja(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "サーバーは応答しました。モデルは読み込み済みです (%s)");
        zh(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "服务器已响应，模型已加载（%s）");
        fr(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "Le serveur répond, le modèle est chargé (%s)");
        es(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "El servidor responde, el modelo está cargado (%s)");
        de(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "Der Server antwortet, das Modell ist geladen (%s)");
        ko(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "서버가 응답하며 모델이 로드되어 있습니다 (%s)");
        it(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED, "Il server risponde, il modello è caricato (%s)");

        ja(THE_SERVER_ANSWERS_NO_MODEL, "サーバーは応答しました。モデルは読み込まれていません (バッチの前のリクエストが読み込みます)");
        zh(THE_SERVER_ANSWERS_NO_MODEL, "服务器已响应，尚未加载模型（批处理前的请求会加载）");
        fr(THE_SERVER_ANSWERS_NO_MODEL, "Le serveur répond, aucun modèle n'est chargé (les requêtes avant un lot le chargent)");
        es(THE_SERVER_ANSWERS_NO_MODEL, "El servidor responde, no hay ningún modelo cargado (las solicitudes anteriores a un lote lo cargan)");
        de(THE_SERVER_ANSWERS_NO_MODEL, "Der Server antwortet, kein Modell ist geladen (die Anfragen vor einem Stapel laden es)");
        ko(THE_SERVER_ANSWERS_NO_MODEL, "서버가 응답하며 로드된 모델이 없습니다 (배치 전의 요청이 모델을 로드합니다)");
        it(THE_SERVER_ANSWERS_NO_MODEL, "Il server risponde, nessun modello è caricato (le richieste prima di un batch lo caricano)");

        ja(CONNECTION_FAILED, "接続に失敗しました: %s");
        zh(CONNECTION_FAILED, "连接失败：%s");
        fr(CONNECTION_FAILED, "Échec de la connexion : %s");
        es(CONNECTION_FAILED, "Falló la conexión: %s");
        de(CONNECTION_FAILED, "Verbindung fehlgeschlagen: %s");
        ko(CONNECTION_FAILED, "연결에 실패했습니다: %s");
        it(CONNECTION_FAILED, "Connessione non riuscita: %s");

        ja(NARRATOR_VOICE, "ナレーターの声");
        zh(NARRATOR_VOICE, "旁白声音");
        fr(NARRATOR_VOICE, "Voix du narrateur");
        es(NARRATOR_VOICE, "Voz del narrador");
        de(NARRATOR_VOICE, "Erzählerstimme");
        ko(NARRATOR_VOICE, "내레이터 음성");
        it(NARRATOR_VOICE, "Voce narrante");

        ja(NARRATOR_VOICE_HELP, "本にキャストがない、またはキャストにナレーターの声がない場合に使うナレーター。");
        zh(NARRATOR_VOICE_HELP, "书没有角色分配、或其中没有旁白声音时使用的旁白。");
        fr(NARRATOR_VOICE_HELP, "Le narrateur quand un livre n'a pas de distribution, ou que sa distribution n'a pas de voix de narrateur.");
        es(NARRATOR_VOICE_HELP, "El narrador cuando un libro no tiene reparto, o su reparto no tiene voz de narrador.");
        de(NARRATOR_VOICE_HELP, "Der Erzähler, wenn ein Buch keine Besetzung hat oder seine Besetzung keine Erzählerstimme hat.");
        ko(NARRATOR_VOICE_HELP, "책에 배역이 없거나 배역에 내레이터 음성이 없을 때의 내레이터.");
        it(NARRATOR_VOICE_HELP, "Il narratore quando un libro non ha un cast, o il suo cast non ha una voce narrante.");

        ja(CONVERT_TO_AUDIO, "音声に変換");
        zh(CONVERT_TO_AUDIO, "转换为音频");
        fr(CONVERT_TO_AUDIO, "Convertir en audio");
        es(CONVERT_TO_AUDIO, "Convertir en audio");
        de(CONVERT_TO_AUDIO, "In Audio umwandeln");
        ko(CONVERT_TO_AUDIO, "오디오로 변환");
        it(CONVERT_TO_AUDIO, "Converti in audio");

        ja(BACKEND, "バックエンド");
        zh(BACKEND, "后端");
        fr(BACKEND, "Moteur");
        es(BACKEND, "Backend");
        de(BACKEND, "Backend");
        ko(BACKEND, "백엔드");
        it(BACKEND, "Backend");

        ja(LOAD, "読み込む");
        zh(LOAD, "加载");
        fr(LOAD, "Charger");
        es(LOAD, "Cargar");
        de(LOAD, "Laden");
        ko(LOAD, "불러오기");
        it(LOAD, "Carica");

        ja(LOAD_THE_PARTS_HINT, "この本には本文のあるパートが %s 個あります。読み込んで各パートの状態を確認し、パートにチェックを入れて処理を計画し、開始を押してください。");
        zh(LOAD_THE_PARTS_HINT, "本书有 %s 个含文本的章节。加载后可查看各章节的状态，勾选章节并规划操作，然后点击开始。");
        fr(LOAD_THE_PARTS_HINT, "Le livre a %s parties avec du texte. Chargez-les pour voir ce que chacune a, cochez les parties, planifiez ce qu'il faut en faire et appuyez sur Démarrer.");
        es(LOAD_THE_PARTS_HINT, "El libro tiene %s partes con texto. Cárgalas para ver qué tiene cada una, marca las partes, planifica qué hacer con ellas y pulsa Iniciar.");
        de(LOAD_THE_PARTS_HINT, "Das Buch hat %s Abschnitte mit Text. Lade sie, um zu sehen, was jeder hat, hake die Abschnitte an, plane, was mit ihnen geschehen soll, und klicke auf „Starten“.");
        ko(LOAD_THE_PARTS_HINT, "책에 텍스트가 있는 파트가 %s개 있습니다. 불러와서 각 파트의 상태를 확인하고, 파트를 체크하고, 처리할 작업을 계획한 다음 시작을 누르세요.");
        it(LOAD_THE_PARTS_HINT, "Il libro ha %s parti con testo. Caricale per vedere cosa ha ciascuna, seleziona le parti, pianifica cosa farne e premi Avvia.");

        ja(PART_NOT_GENERATED, "未生成");
        zh(PART_NOT_GENERATED, "未生成");
        fr(PART_NOT_GENERATED, "Non générée");
        es(PART_NOT_GENERATED, "No generada");
        de(PART_NOT_GENERATED, "Nicht generiert");
        ko(PART_NOT_GENERATED, "생성되지 않음");
        it(PART_NOT_GENERATED, "Non generato");

        ja(PART_LABELLED, "ラベル付き");
        zh(PART_LABELLED, "已标注");
        fr(PART_LABELLED, "Étiquetée");
        es(PART_LABELLED, "Etiquetada");
        de(PART_LABELLED, "Etikettiert");
        ko(PART_LABELLED, "라벨 지정됨");
        it(PART_LABELLED, "Etichettato");

        ja(PART_GENERATED, "生成済み");
        zh(PART_GENERATED, "已生成");
        fr(PART_GENERATED, "Générée");
        es(PART_GENERATED, "Generada");
        de(PART_GENERATED, "Generiert");
        ko(PART_GENERATED, "생성됨");
        it(PART_GENERATED, "Generato");

        ja(PART_GENERATED_TEXT_CHANGED, "生成済み(本文が変更されました)");
        zh(PART_GENERATED_TEXT_CHANGED, "已生成（文本已更改）");
        fr(PART_GENERATED_TEXT_CHANGED, "Générée (le texte a changé)");
        es(PART_GENERATED_TEXT_CHANGED, "Generada (el texto cambió)");
        de(PART_GENERATED_TEXT_CHANGED, "Generiert (der Text hat sich geändert)");
        ko(PART_GENERATED_TEXT_CHANGED, "생성됨 (텍스트가 변경됨)");
        it(PART_GENERATED_TEXT_CHANGED, "Generato (il testo è cambiato)");

        ja(PART_FINAL, "確定");
        zh(PART_FINAL, "最终");
        fr(PART_FINAL, "définitive");
        es(PART_FINAL, "definitiva");
        de(PART_FINAL, "endgültig");
        ko(PART_FINAL, "최종");
        it(PART_FINAL, "finale");

        ja(PLAN, "計画");
        zh(PLAN, "计划");
        fr(PLAN, "Plan");
        es(PLAN, "Plan");
        de(PLAN, "Plan");
        ko(PLAN, "계획");
        it(PLAN, "Pianifica");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");
        fr(GENERATE, "Générer");
        es(GENERATE, "Generar");
        de(GENERATE, "Generieren");
        ko(GENERATE, "생성");
        it(GENERATE, "Genera");

        ja(REGENERATE, "再生成");
        zh(REGENERATE, "重新生成");
        fr(REGENERATE, "Régénérer");
        es(REGENERATE, "Regenerar");
        de(REGENERATE, "Neu generieren");
        ko(REGENERATE, "다시 생성");
        it(REGENERATE, "Rigenera");

        ja(DELETE_AUDIO, "音声を削除");
        zh(DELETE_AUDIO, "删除音频");
        fr(DELETE_AUDIO, "Supprimer l'audio");
        es(DELETE_AUDIO, "Eliminar audio");
        de(DELETE_AUDIO, "Audio löschen");
        ko(DELETE_AUDIO, "오디오 삭제");
        it(DELETE_AUDIO, "Elimina l'audio");

        ja(REMOVE_LABELS, "ラベルを削除");
        zh(REMOVE_LABELS, "移除标注");
        fr(REMOVE_LABELS, "Retirer les étiquettes");
        es(REMOVE_LABELS, "Quitar etiquetas");
        de(REMOVE_LABELS, "Etiketten entfernen");
        ko(REMOVE_LABELS, "라벨 제거");
        it(REMOVE_LABELS, "Rimuovi le etichette");

        ja(CLEAR_THE_PLAN, "計画を消去");
        zh(CLEAR_THE_PLAN, "清除计划");
        fr(CLEAR_THE_PLAN, "Vider le plan");
        es(CLEAR_THE_PLAN, "Borrar el plan");
        de(CLEAR_THE_PLAN, "Den Plan leeren");
        ko(CLEAR_THE_PLAN, "계획 지우기");
        it(CLEAR_THE_PLAN, "Cancella il piano");

        ja(PLAY, "再生");
        zh(PLAY, "播放");
        fr(PLAY, "Lire");
        es(PLAY, "Reproducir");
        de(PLAY, "Abspielen");
        ko(PLAY, "재생");
        it(PLAY, "Riproduci");

        ja(SELECT_THE_PARTS_FIRST, "先にパートにチェックを入れてください");
        zh(SELECT_THE_PARTS_FIRST, "请先勾选章节");
        fr(SELECT_THE_PARTS_FIRST, "Cochez d'abord les parties");
        es(SELECT_THE_PARTS_FIRST, "Marca primero las partes");
        de(SELECT_THE_PARTS_FIRST, "Hake zuerst die Abschnitte an");
        ko(SELECT_THE_PARTS_FIRST, "먼저 파트를 체크하세요");
        it(SELECT_THE_PARTS_FIRST, "Seleziona prima le parti");

        ja(NOTHING_IS_PLANNED, "計画がありません。パートにチェックを入れ、生成・再生成・音声を削除・ラベルを削除のいずれかを選んでください");
        zh(NOTHING_IS_PLANNED, "没有计划：请勾选章节并选择生成、重新生成、删除音频或移除标注");
        fr(NOTHING_IS_PLANNED, "Rien n'est planifié : cochez des parties et choisissez Générer, Régénérer, Supprimer l'audio ou Retirer les étiquettes");
        es(NOTHING_IS_PLANNED, "No hay nada planificado: marca partes y elige Generar, Regenerar, Eliminar audio o Quitar etiquetas");
        de(NOTHING_IS_PLANNED, "Nichts ist geplant: Hake Abschnitte an und wähle „Generieren“, „Neu generieren“, „Audio löschen“ oder „Etiketten entfernen“");
        ko(NOTHING_IS_PLANNED, "계획된 것이 없습니다: 파트를 체크하고 생성, 다시 생성, 오디오 삭제 또는 라벨 제거를 선택하세요");
        it(NOTHING_IS_PLANNED, "Nulla è pianificato: seleziona le parti e scegli Genera, Rigenera, Elimina l'audio o Rimuovi le etichette");

        ja(APPLY_THE_PLAN_QUESTION, "%s パートの音声を削除し、%s パートのラベルを削除します。元に戻せません。計画を実行しますか?");
        zh(APPLY_THE_PLAN_QUESTION, "将删除 %s 个章节的音频并移除 %s 个章节的标注，此操作无法撤销。执行计划吗？");
        fr(APPLY_THE_PLAN_QUESTION, "L'audio de %s parties sera supprimé et les étiquettes de %s parties retirées. C'est irréversible. Exécuter le plan ?");
        es(APPLY_THE_PLAN_QUESTION, "Se eliminará el audio de %s partes y se quitarán las etiquetas de %s partes. No se puede deshacer. ¿Ejecutar el plan?");
        de(APPLY_THE_PLAN_QUESTION, "Das Audio von %s Abschnitten wird gelöscht und die Etiketten von %s Abschnitten werden entfernt. Das kann nicht rückgängig gemacht werden. Den Plan ausführen?");
        ko(APPLY_THE_PLAN_QUESTION, "파트 %s개의 오디오가 삭제되고 파트 %s개의 라벨이 제거됩니다. 되돌릴 수 없습니다. 계획을 실행하시겠습니까?");
        it(APPLY_THE_PLAN_QUESTION, "L'audio di %s parti verrà eliminato e le etichette di %s parti rimosse. Non si può annullare. Eseguire il piano?");

        ja(THE_PLAN_WAS_CARRIED_OUT, "%s パートの音声を削除し、%s パートのラベルを削除しました");
        zh(THE_PLAN_WAS_CARRIED_OUT, "已删除 %s 个章节的音频，已移除 %s 个章节的标注");
        fr(THE_PLAN_WAS_CARRIED_OUT, "Audio supprimé pour %s parties, étiquettes retirées pour %s parties");
        es(THE_PLAN_WAS_CARRIED_OUT, "Audio eliminado en %s partes, etiquetas quitadas en %s partes");
        de(THE_PLAN_WAS_CARRIED_OUT, "Audio gelöscht für %s Abschnitte, Etiketten entfernt für %s Abschnitte");
        ko(THE_PLAN_WAS_CARRIED_OUT, "파트 %s개의 오디오를 삭제하고 파트 %s개의 라벨을 제거했습니다");
        it(THE_PLAN_WAS_CARRIED_OUT, "Audio eliminato per %s parti, etichette rimosse per %s parti");

        ja(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s パートはキューで待機中または実行中のため対象外です");
        zh(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s 个章节正在队列中等待或运行，已被跳过");
        fr(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s parties sont en attente ou en cours dans la file et ont été omises");
        es(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s partes están esperando o en ejecución en la cola y se omitieron");
        de(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s Abschnitte warten oder laufen in der Warteschlange und wurden ausgelassen");
        ko(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "파트 %s개가 대기열에서 대기 중이거나 실행 중이어서 제외되었습니다");
        it(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT, "%s parti sono in attesa o in esecuzione nella coda e sono state escluse");

        ja(REASONING, "思考");
        zh(REASONING, "推理");
        fr(REASONING, "Raisonnement");
        es(REASONING, "Razonamiento");
        de(REASONING, "Reasoning");
        ko(REASONING, "추론");
        it(REASONING, "Ragionamento");

        ja(THE_ANSWER_OF_THE_MODEL, "モデルの回答");
        zh(THE_ANSWER_OF_THE_MODEL, "模型的回答");
        fr(THE_ANSWER_OF_THE_MODEL, "Réponse du modèle");
        es(THE_ANSWER_OF_THE_MODEL, "Respuesta del modelo");
        de(THE_ANSWER_OF_THE_MODEL, "Antwort des Modells");
        ko(THE_ANSWER_OF_THE_MODEL, "모델의 응답");
        it(THE_ANSWER_OF_THE_MODEL, "Risposta del modello");

        ja(WHAT_THE_MODEL_SAID, "モデルの出力: %s");
        zh(WHAT_THE_MODEL_SAID, "模型的输出：%s");
        fr(WHAT_THE_MODEL_SAID, "Ce que le modèle a dit : %s");
        es(WHAT_THE_MODEL_SAID, "Lo que dijo el modelo: %s");
        de(WHAT_THE_MODEL_SAID, "Was das Modell gesagt hat: %s");
        ko(WHAT_THE_MODEL_SAID, "모델이 한 말: %s");
        it(WHAT_THE_MODEL_SAID, "Cosa ha detto il modello: %s");

        ja(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "クリックして、このパートのラベル付け中にモデルが出力した内容を表示");
        zh(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "点击查看模型在标注此章节时输出的内容");
        fr(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Cliquez pour lire ce que le modèle a dit pendant l'étiquetage de cette partie");
        es(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Haz clic para leer lo que dijo el modelo mientras etiquetaba esta parte");
        de(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Klicken, um zu lesen, was das Modell beim Etikettieren dieses Abschnitts gesagt hat");
        ko(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "클릭하면 이 파트에 라벨을 지정하는 동안 모델이 한 말을 읽을 수 있습니다");
        it(CLICK_TO_SEE_WHAT_THE_MODEL_SAID, "Clicca per leggere cosa ha detto il modello mentre etichettava questa parte");

        ja(START, "開始");
        zh(START, "开始");
        fr(START, "Démarrer");
        es(START, "Iniciar");
        de(START, "Starten");
        ko(START, "시작");
        it(START, "Avvia");

        ja(STOP_ALL, "すべて停止");
        zh(STOP_ALL, "全部停止");
        fr(STOP_ALL, "Tout arrêter");
        es(STOP_ALL, "Detener todo");
        de(STOP_ALL, "Alle stoppen");
        ko(STOP_ALL, "모두 중지");
        it(STOP_ALL, "Interrompi tutto");

        ja(CLEAR_FINISHED, "完了したものを消去");
        zh(CLEAR_FINISHED, "清除已完成");
        fr(CLEAR_FINISHED, "Effacer les terminées");
        es(CLEAR_FINISHED, "Quitar las terminadas");
        de(CLEAR_FINISHED, "Abgeschlossene entfernen");
        ko(CLEAR_FINISHED, "완료된 항목 지우기");
        it(CLEAR_FINISHED, "Cancella i terminati");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(PART, "パート");
        zh(PART, "章节");
        fr(PART, "Partie");
        es(PART, "Parte");
        de(PART, "Abschnitt");
        ko(PART, "파트");
        it(PART, "Parte");

        ja(STATE, "状態");
        zh(STATE, "状态");
        fr(STATE, "État");
        es(STATE, "Estado");
        de(STATE, "Zustand");
        ko(STATE, "상태");
        it(STATE, "Stato");

        ja(MESSAGE, "メッセージ");
        zh(MESSAGE, "消息");
        fr(MESSAGE, "Message");
        es(MESSAGE, "Mensaje");
        de(MESSAGE, "Nachricht");
        ko(MESSAGE, "메시지");
        it(MESSAGE, "Messaggio");

        ja(STATE_WAITING, "待機中");
        zh(STATE_WAITING, "等待中");
        fr(STATE_WAITING, "En attente");
        es(STATE_WAITING, "En espera");
        de(STATE_WAITING, "Wartet");
        ko(STATE_WAITING, "대기 중");
        it(STATE_WAITING, "In attesa");

        ja(STATE_RUNNING, "実行中");
        zh(STATE_RUNNING, "进行中");
        fr(STATE_RUNNING, "En cours");
        es(STATE_RUNNING, "En ejecución");
        de(STATE_RUNNING, "Läuft");
        ko(STATE_RUNNING, "실행 중");
        it(STATE_RUNNING, "In esecuzione");

        ja(STATE_DONE, "完了");
        zh(STATE_DONE, "完成");
        fr(STATE_DONE, "Terminée");
        es(STATE_DONE, "Hecha");
        de(STATE_DONE, "Fertig");
        ko(STATE_DONE, "완료");
        it(STATE_DONE, "Fatto");

        ja(STATE_FAILED, "失敗");
        zh(STATE_FAILED, "失败");
        fr(STATE_FAILED, "Échouée");
        es(STATE_FAILED, "Fallida");
        de(STATE_FAILED, "Fehlgeschlagen");
        ko(STATE_FAILED, "실패");
        it(STATE_FAILED, "Non riuscito");

        ja(STATE_CANCELLED, "中止");
        zh(STATE_CANCELLED, "已取消");
        fr(STATE_CANCELLED, "Annulée");
        es(STATE_CANCELLED, "Cancelada");
        de(STATE_CANCELLED, "Abgebrochen");
        ko(STATE_CANCELLED, "취소됨");
        it(STATE_CANCELLED, "Annullato");

        ja(DOWNLOAD, "ダウンロード");
        zh(DOWNLOAD, "下载");
        fr(DOWNLOAD, "Télécharger");
        es(DOWNLOAD, "Descargar");
        de(DOWNLOAD, "Herunterladen");
        ko(DOWNLOAD, "다운로드");
        it(DOWNLOAD, "Scarica");

        ja(RETRY, "再試行");
        zh(RETRY, "重试");
        fr(RETRY, "Réessayer");
        es(RETRY, "Reintentar");
        de(RETRY, "Wiederholen");
        ko(RETRY, "다시 시도");
        it(RETRY, "Riprova");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");
        it(CANCEL, "Annulla");

        ja(NO_BACKEND_YET, "バックエンドがありません。設定 → 拡張機能の設定 → オーディオブックの設定で追加してください。");
        zh(NO_BACKEND_YET, "尚无后端。请在 设置 → 扩展设置 → 有声书设置 中添加。");
        fr(NO_BACKEND_YET, "Pas encore de moteur. Ajoutez-en un dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(NO_BACKEND_YET, "Todavía no hay ningún backend. Añade uno en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(NO_BACKEND_YET, "Noch kein Backend. Füge eines unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen hinzu.");
        ko(NO_BACKEND_YET, "아직 백엔드가 없습니다. 설정 → 확장 기능 설정 → 오디오북 설정에서 추가하세요.");
        it(NO_BACKEND_YET, "Ancora nessun backend. Aggiungine uno in Impostazioni → Impostazioni delle estensioni → Impostazioni dell'audiolibro.");

        ja(THERE_IS_NO_NARRATOR_VOICE, "ナレーターの声がありません。キャスト、または 設定 → 拡張機能の設定 → オーディオブックの設定 で指定してください。");
        zh(THERE_IS_NO_NARRATOR_VOICE, "没有旁白声音。请在角色分配中，或在 设置 → 扩展设置 → 有声书设置 中选择。");
        fr(THERE_IS_NO_NARRATOR_VOICE, "Il n'y a pas de voix de narrateur. Choisissez-la dans la distribution, ou dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(THERE_IS_NO_NARRATOR_VOICE, "No hay voz de narrador. Elígela en el reparto, o en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(THERE_IS_NO_NARRATOR_VOICE, "Es gibt keine Erzählerstimme. Wähle sie in der Besetzung oder unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen.");
        ko(THERE_IS_NO_NARRATOR_VOICE, "내레이터 음성이 없습니다. 배역에서 선택하거나 설정 → 확장 기능 설정 → 오디오북 설정에서 선택하세요.");
        it(THERE_IS_NO_NARRATOR_VOICE, "Non c'è una voce narrante. Sceglila nel cast, o in Impostazioni → Impostazioni delle estensioni → Impostazioni dell'audiolibro.");

        ja(THE_BOOK_HAS_NO_TEXT, "本文のあるパートがありません");
        zh(THE_BOOK_HAS_NO_TEXT, "书中没有含文本的章节");
        fr(THE_BOOK_HAS_NO_TEXT, "Le livre n'a pas de parties avec du texte");
        es(THE_BOOK_HAS_NO_TEXT, "El libro no tiene partes con texto");
        de(THE_BOOK_HAS_NO_TEXT, "Das Buch hat keine Abschnitte mit Text");
        ko(THE_BOOK_HAS_NO_TEXT, "책에 텍스트가 있는 파트가 없습니다");
        it(THE_BOOK_HAS_NO_TEXT, "Il libro non ha parti con testo");

        ja(THE_PARTS_ARE_IN_THE_QUEUE, "%s 個のパートをキューに入れました");
        zh(THE_PARTS_ARE_IN_THE_QUEUE, "已将 %s 个章节加入队列");
        fr(THE_PARTS_ARE_IN_THE_QUEUE, "%s parties sont dans la file");
        es(THE_PARTS_ARE_IN_THE_QUEUE, "%s partes están en la cola");
        de(THE_PARTS_ARE_IN_THE_QUEUE, "%s Abschnitte sind in der Warteschlange");
        ko(THE_PARTS_ARE_IN_THE_QUEUE, "파트 %s개가 대기열에 있습니다");
        it(THE_PARTS_ARE_IN_THE_QUEUE, "%s parti sono in coda");

        ja(THE_CONVERSION_COULD_NOT_BE_STARTED, "変換を開始できませんでした: %s");
        zh(THE_CONVERSION_COULD_NOT_BE_STARTED, "无法开始转换：%s");
        fr(THE_CONVERSION_COULD_NOT_BE_STARTED, "La conversion n'a pas pu démarrer : %s");
        es(THE_CONVERSION_COULD_NOT_BE_STARTED, "No se pudo iniciar la conversión: %s");
        de(THE_CONVERSION_COULD_NOT_BE_STARTED, "Die Umwandlung konnte nicht gestartet werden: %s");
        ko(THE_CONVERSION_COULD_NOT_BE_STARTED, "변환을 시작할 수 없었습니다: %s");
        it(THE_CONVERSION_COULD_NOT_BE_STARTED, "Non è stato possibile avviare la conversione: %s");

        ja(LISTEN_TO_THE_AUDIOBOOK, "オーディオブックを聴く");
        zh(LISTEN_TO_THE_AUDIOBOOK, "收听有声书");
        fr(LISTEN_TO_THE_AUDIOBOOK, "Écouter le livre audio");
        es(LISTEN_TO_THE_AUDIOBOOK, "Escuchar el audiolibro");
        de(LISTEN_TO_THE_AUDIOBOOK, "Das Hörbuch anhören");
        ko(LISTEN_TO_THE_AUDIOBOOK, "오디오북 듣기");
        it(LISTEN_TO_THE_AUDIOBOOK, "Ascolta l'audiolibro");

        ja(PAUSE_LISTENING, "一時停止");
        zh(PAUSE_LISTENING, "暂停");
        fr(PAUSE_LISTENING, "Pause");
        es(PAUSE_LISTENING, "Pausar");
        de(PAUSE_LISTENING, "Pause");
        ko(PAUSE_LISTENING, "일시 정지");
        it(PAUSE_LISTENING, "Pausa");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");
        ko(STOP, "중지");
        it(STOP, "Interrompi");

        ja(THIS_PART_HAS_NO_AUDIO, "このパートにはまだ音声がありません");
        zh(THIS_PART_HAS_NO_AUDIO, "此章节还没有音频");
        fr(THIS_PART_HAS_NO_AUDIO, "Cette partie n'a pas (encore) d'audio");
        es(THIS_PART_HAS_NO_AUDIO, "Esta parte (todavía) no tiene audio");
        de(THIS_PART_HAS_NO_AUDIO, "Dieser Abschnitt hat (noch) kein Audio");
        ko(THIS_PART_HAS_NO_AUDIO, "이 파트에는 (아직) 오디오가 없습니다");
        it(THIS_PART_HAS_NO_AUDIO, "Questa parte non ha (ancora) audio");

        ja(THE_AUDIO_COULD_NOT_BE_LOADED, "音声を読み込めませんでした");
        zh(THE_AUDIO_COULD_NOT_BE_LOADED, "无法加载音频");
        fr(THE_AUDIO_COULD_NOT_BE_LOADED, "L'audio n'a pas pu être chargé");
        es(THE_AUDIO_COULD_NOT_BE_LOADED, "No se pudo cargar el audio");
        de(THE_AUDIO_COULD_NOT_BE_LOADED, "Das Audio konnte nicht geladen werden");
        ko(THE_AUDIO_COULD_NOT_BE_LOADED, "오디오를 불러올 수 없었습니다");
        it(THE_AUDIO_COULD_NOT_BE_LOADED, "Non è stato possibile caricare l'audio");

        ja(LABEL_WITH_A_LANGUAGE_MODEL, "言語モデルでパートにラベルを付ける (誰が話すか、話し方、効果音タグ)");
        zh(LABEL_WITH_A_LANGUAGE_MODEL, "用语言模型为章节加标注（谁在说话、语气、声音标签）");
        fr(LABEL_WITH_A_LANGUAGE_MODEL, "Étiqueter les parties avec un modèle de langage (qui parle, comment, balises sonores)");
        es(LABEL_WITH_A_LANGUAGE_MODEL, "Etiquetar las partes con un modelo de lenguaje (quién habla, cómo, etiquetas de sonido)");
        de(LABEL_WITH_A_LANGUAGE_MODEL, "Die Abschnitte mit einem Sprachmodell etikettieren (wer spricht, wie, Sound-Tags)");
        ko(LABEL_WITH_A_LANGUAGE_MODEL, "언어 모델로 파트에 라벨 지정 (화자, 말투, 사운드 태그)");
        it(LABEL_WITH_A_LANGUAGE_MODEL, "Etichetta le parti con un modello linguistico (chi parla, come, tag sonori)");

        ja(THE_MODEL_THAT_LABELS, "ラベルを付けるモデル");
        zh(THE_MODEL_THAT_LABELS, "负责标注的模型");
        fr(THE_MODEL_THAT_LABELS, "Le modèle qui étiquette");
        es(THE_MODEL_THAT_LABELS, "El modelo que etiqueta");
        de(THE_MODEL_THAT_LABELS, "Das Modell, das etikettiert");
        ko(THE_MODEL_THAT_LABELS, "라벨을 지정하는 모델");
        it(THE_MODEL_THAT_LABELS, "Il modello che etichetta");

        ja(THE_PARTS_ARE_LABELLED_FIRST, "先にパートにラベルを付け (モデルが読み込まれている必要があります)、その後で読み上げます。バッチ前のリクエストが音声のためにモデルをアンロードします。");
        zh(THE_PARTS_ARE_LABELLED_FIRST, "先为章节加标注（模型必须已加载），再朗读：批处理前的请求会为语音卸载该模型。");
        fr(THE_PARTS_ARE_LABELLED_FIRST, "Les parties sont d'abord étiquetées (le modèle doit être chargé), puis lues : les requêtes avant le lot déchargent le modèle pour la synthèse vocale.");
        es(THE_PARTS_ARE_LABELLED_FIRST, "Las partes se etiquetan primero (el modelo debe estar cargado) y luego se leen en voz alta: las solicitudes anteriores al lote descargan el modelo para la voz.");
        de(THE_PARTS_ARE_LABELLED_FIRST, "Die Abschnitte werden zuerst etikettiert (das Modell muss geladen sein), dann gesprochen: Die Anfragen vor dem Stapel entladen das Modell für die Sprache.");
        ko(THE_PARTS_ARE_LABELLED_FIRST, "파트에 먼저 라벨을 지정한 다음 (모델이 로드되어 있어야 함) 음성으로 만듭니다: 배치 전의 요청이 음성을 위해 모델을 언로드합니다.");
        it(THE_PARTS_ARE_LABELLED_FIRST, "Le parti vengono prima etichettate (il modello deve essere caricato), poi lette: le richieste prima del batch scaricano il modello per la voce.");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");
        ko(DELETE, "삭제");
        it(DELETE, "Elimina");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");
        it(SAVE, "Salva");

        ja(REMOVE, "削除");
        zh(REMOVE, "移除");
        fr(REMOVE, "Retirer");
        es(REMOVE, "Quitar");
        de(REMOVE, "Entfernen");
        ko(REMOVE, "제거");
        it(REMOVE, "Rimuovi");

        ja(CAST, "キャスト");
        zh(CAST, "角色分配");
        fr(CAST, "Distribution");
        es(CAST, "Reparto");
        de(CAST, "Besetzung");
        ko(CAST, "배역");
        it(CAST, "Cast");

        ja(CHARACTERS, "登場人物");
        zh(CHARACTERS, "人物");
        fr(CHARACTERS, "Personnages");
        es(CHARACTERS, "Personajes");
        de(CHARACTERS, "Figuren");
        ko(CHARACTERS, "캐릭터");
        it(CHARACTERS, "Personaggi");

        ja(NARRATOR, "ナレーター");
        zh(NARRATOR, "旁白");
        fr(NARRATOR, "Narrateur");
        es(NARRATOR, "Narrador");
        de(NARRATOR, "Erzähler");
        ko(NARRATOR, "내레이터");
        it(NARRATOR, "Narratore");

        ja(OTHER_SPEAKERS, "その他の話者");
        zh(OTHER_SPEAKERS, "其他说话者");
        fr(OTHER_SPEAKERS, "Autres locuteurs");
        es(OTHER_SPEAKERS, "Otros hablantes");
        de(OTHER_SPEAKERS, "Andere Sprecher");
        ko(OTHER_SPEAKERS, "기타 화자");
        it(OTHER_SPEAKERS, "Altri parlanti");

        ja(VOICE, "声");
        zh(VOICE, "声音");
        fr(VOICE, "Voix");
        es(VOICE, "Voz");
        de(VOICE, "Stimme");
        ko(VOICE, "목소리");
        it(VOICE, "Voce");

        ja(WHEN, "場合");
        zh(WHEN, "场合");
        fr(WHEN, "Quand");
        es(WHEN, "Cuándo");
        de(WHEN, "Wann");
        ko(WHEN, "조건");
        it(WHEN, "Quando");

        ja(MOOD_NAME, "感情: %s");
        zh(MOOD_NAME, "语气：%s");
        fr(MOOD_NAME, "Humeur : %s");
        es(MOOD_NAME, "Estado de ánimo: %s");
        de(MOOD_NAME, "Stimmung: %s");
        ko(MOOD_NAME, "분위기: %s");
        it(MOOD_NAME, "Umore: %s");

        ja(LANGUAGE_NAME, "言語: %s");
        zh(LANGUAGE_NAME, "语言：%s");
        fr(LANGUAGE_NAME, "Langue : %s");
        es(LANGUAGE_NAME, "Idioma: %s");
        de(LANGUAGE_NAME, "Sprache: %s");
        ko(LANGUAGE_NAME, "언어: %s");
        it(LANGUAGE_NAME, "Lingua: %s");

        ja(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "感情や言語ごとの声を追加");
        zh(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "为语气或语言添加声音");
        fr(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Une voix pour une humeur ou une langue");
        es(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Una voz para un estado de ánimo o un idioma");
        de(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Eine Stimme für eine Stimmung oder eine Sprache");
        ko(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "분위기나 언어에 따른 음성");
        it(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE, "Una voce per un umore o una lingua");

        ja(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "クリップ、または声のファイル (Henry.wav)");
        zh(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "参考片段，或声音文件 (Henry.wav)");
        fr(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Un extrait, ou un fichier de voix (Henry.wav)");
        es(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Un clip, o un archivo de voz (Henry.wav)");
        de(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Ein Clip oder eine Stimmdatei (Henry.wav)");
        ko(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "클립 또는 음성 파일 (Henry.wav)");
        it(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER, "Una clip, o un file di voce (Henry.wav)");

        ja(ADD_A_CLIP, "クリップを追加");
        zh(ADD_A_CLIP, "添加片段");
        fr(ADD_A_CLIP, "Ajouter un extrait");
        es(ADD_A_CLIP, "Añadir un clip");
        de(ADD_A_CLIP, "Einen Clip hinzufügen");
        ko(ADD_A_CLIP, "클립 추가");
        it(ADD_A_CLIP, "Aggiungi una clip");

        ja(THE_CLIP_COULD_NOT_BE_ADDED, "クリップを追加できませんでした: %s");
        zh(THE_CLIP_COULD_NOT_BE_ADDED, "无法添加片段：%s");
        fr(THE_CLIP_COULD_NOT_BE_ADDED, "L'extrait n'a pas pu être ajouté : %s");
        es(THE_CLIP_COULD_NOT_BE_ADDED, "No se pudo añadir el clip: %s");
        de(THE_CLIP_COULD_NOT_BE_ADDED, "Der Clip konnte nicht hinzugefügt werden: %s");
        ko(THE_CLIP_COULD_NOT_BE_ADDED, "클립을 추가할 수 없었습니다: %s");
        it(THE_CLIP_COULD_NOT_BE_ADDED, "Non è stato possibile aggiungere la clip: %s");

        ja(VOICES_AND_CLIPS, "声とクリップ");
        zh(VOICES_AND_CLIPS, "声音与片段");
        fr(VOICES_AND_CLIPS, "Voix et extraits");
        es(VOICES_AND_CLIPS, "Voces y clips");
        de(VOICES_AND_CLIPS, "Stimmen und Clips");
        ko(VOICES_AND_CLIPS, "목소리와 클립");
        it(VOICES_AND_CLIPS, "Voci e clip");

        ja(VOICES_AND_CLIPS_HELP, "アクターと、その声の元になる参照クリップです。クリップはフォルダーに分けられ、クリップから作ったバージョンはその下に表示されます。音声モデルは声だけでなく録音そのものを真似ます。背景音 (映画、部屋の響き、音楽) のあるクリップでは、どのセリフにもノイズや響きが乗ります。本当の間 (ま) がある綺麗な録音を使うか、ここでクリップを整えてください: ffmpeg で、または設定でクリーンアップを用意すれば ComfyUI の AI で。");
        zh(VOICES_AND_CLIPS_HELP, "您的演员及其声音所克隆的参考片段，按文件夹整理，由片段生成的版本显示在其下方。语音模型复制的是录音本身，而不只是声音：带有背景声（电影、房间回响、音乐）的片段会让每一句都带上杂音或回响。请使用带有真正停顿的干净录音，或在此清理片段：用 ffmpeg，或在设置中配置清理后用 ComfyUI 上的 AI。");
        fr(VOICES_AND_CLIPS_HELP, "Vos acteurs et les extraits de référence dont leurs voix sont clonées, rangés en dossiers, avec sous chaque extrait les versions qui en sont faites. Un modèle de voix copie l'enregistrement, pas seulement la voix : un extrait avec un fond sonore (un film, une pièce, de la musique) donne à chaque réplique un souffle ou une acoustique. Utilisez un enregistrement propre avec de vraies pauses, ou nettoyez un extrait ici : avec ffmpeg, ou par IA sur ComfyUI quand un nettoyage est configuré dans les réglages.");
        es(VOICES_AND_CLIPS_HELP, "Tus actores y los clips de referencia de los que se clonan sus voces, en carpetas, con las versiones hechas de un clip debajo de él. Un modelo de voz copia la grabación, no solo la voz: un clip con sonido de fondo (una película, una sala, música) da a cada frase un soplido o una acústica. Usa una grabación limpia con pausas reales, o limpia un clip aquí: con ffmpeg, o con IA en ComfyUI cuando hay una limpieza configurada en los ajustes.");
        de(VOICES_AND_CLIPS_HELP, "Deine Sprecher und die Referenzclips, aus denen ihre Stimmen geklont werden, in Ordnern, mit den aus einem Clip gemachten Versionen darunter. Ein Stimmmodell kopiert die Aufnahme, nicht nur die Stimme: Ein Clip mit Hintergrundgeräuschen (ein Film, ein Raum, Musik) gibt jedem Satz ein Rauschen oder einen Raumklang. Verwende eine saubere Aufnahme mit echten Pausen oder säubere einen Clip hier: mit ffmpeg oder mit KI auf ComfyUI, wenn in den Einstellungen eine Bereinigung eingerichtet ist.");
        ko(VOICES_AND_CLIPS_HELP, "배우와 그 목소리를 복제하는 참조 클립입니다. 클립은 폴더로 정리되고, 클립에서 만든 버전은 그 아래에 표시됩니다. 음성 모델은 목소리뿐 아니라 녹음 자체를 따라 합니다. 배경음(영화, 방의 울림, 음악)이 있는 클립은 모든 대사에 잡음이나 울림을 남깁니다. 실제 쉼이 있는 깨끗한 녹음을 쓰거나 여기서 클립을 정리하세요: ffmpeg로, 또는 설정에서 정리를 준비했다면 ComfyUI의 AI로.");
        it(VOICES_AND_CLIPS_HELP, "I tuoi attori e le clip di riferimento da cui sono clonate le loro voci, in cartelle, con sotto ogni clip le versioni ricavate da essa. Un modello vocale copia la registrazione, non solo la voce: una clip con suoni di fondo (un film, una stanza, musica) dà a ogni battuta un fruscio o un ambiente. Usa una registrazione pulita con pause vere, oppure pulisci una clip qui: con ffmpeg, o con l'IA su ComfyUI quando nelle impostazioni è configurata una pulizia.");

        ja(CLIPS, "クリップ");
        zh(CLIPS, "片段");
        fr(CLIPS, "Extraits");
        es(CLIPS, "Clips");
        de(CLIPS, "Clips");
        ko(CLIPS, "클립");
        it(CLIPS, "Clip");

        ja(LENGTH, "長さ");
        zh(LENGTH, "时长");
        fr(LENGTH, "Durée");
        es(LENGTH, "Duración");
        de(LENGTH, "Länge");
        ko(LENGTH, "길이");
        it(LENGTH, "Durata");

        ja(QUALITY, "品質");
        zh(QUALITY, "质量");
        fr(QUALITY, "Qualité");
        es(QUALITY, "Calidad");
        de(QUALITY, "Qualität");
        ko(QUALITY, "품질");
        it(QUALITY, "Qualità");

        ja(USED_BY, "使用箇所");
        zh(USED_BY, "使用者");
        fr(USED_BY, "Utilisé par");
        es(USED_BY, "Usado por");
        de(USED_BY, "Verwendet von");
        ko(USED_BY, "사용처");
        it(USED_BY, "Usata da");

        ja(OTHER_VOICES, "ほかの声");
        zh(OTHER_VOICES, "其他声音");
        fr(OTHER_VOICES, "Autres voix");
        es(OTHER_VOICES, "Otras voces");
        de(OTHER_VOICES, "Weitere Stimmen");
        ko(OTHER_VOICES, "다른 목소리");
        it(OTHER_VOICES, "Altre voci");

        ja(REPLACE, "置き換え...");
        zh(REPLACE, "替换...");
        fr(REPLACE, "Remplacer...");
        es(REPLACE, "Reemplazar...");
        de(REPLACE, "Ersetzen...");
        ko(REPLACE, "교체...");
        it(REPLACE, "Sostituisci...");

        ja(CLEAN_WITH_FFMPEG, "ffmpeg で整える");
        zh(CLEAN_WITH_FFMPEG, "用 ffmpeg 清理");
        fr(CLEAN_WITH_FFMPEG, "Nettoyer avec ffmpeg");
        es(CLEAN_WITH_FFMPEG, "Limpiar con ffmpeg");
        de(CLEAN_WITH_FFMPEG, "Mit ffmpeg bereinigen");
        ko(CLEAN_WITH_FFMPEG, "ffmpeg로 정리");
        it(CLEAN_WITH_FFMPEG, "Pulisci con ffmpeg");

        ja(CLEAN_WITH_FFMPEG_HELP, "整えたコピー (一定のヒスノイズやハムを減らしたもの) を作り、聞き比べられるようにします。映画の音楽、効果音、ほかの声は残ります");
        zh(CLEAN_WITH_FFMPEG_HELP, "生成一个清理后的副本（减少持续的嘶声和嗡声）以便试听比较；电影中的音乐、音效和其他人声会保留");
        fr(CLEAN_WITH_FFMPEG_HELP, "Crée une copie nettoyée (moins de souffle et de ronflement constants) à écouter et comparer ; la musique, les effets et les autres voix d'un film restent");
        es(CLEAN_WITH_FFMPEG_HELP, "Crea una copia limpia (menos siseo y zumbido constantes) para escuchar y comparar; la música, los efectos y otras voces de una película se quedan");
        de(CLEAN_WITH_FFMPEG_HELP, "Erzeugt eine bereinigte Kopie (weniger gleichmäßiges Rauschen und Brummen) zum Anhören und Vergleichen; Musik, Effekte und andere Stimmen eines Films bleiben");
        ko(CLEAN_WITH_FFMPEG_HELP, "정리된 사본(일정한 히스와 험을 줄인 것)을 만들어 들어 보고 비교할 수 있게 합니다. 영화의 음악, 효과음, 다른 목소리는 남습니다");
        it(CLEAN_WITH_FFMPEG_HELP, "Crea una copia pulita (meno fruscio e ronzio costanti) da ascoltare e confrontare; musica, effetti e altre voci di un film restano");

        ja(DELETE_THE_CLIP, "クリップを削除");
        zh(DELETE_THE_CLIP, "删除片段");
        fr(DELETE_THE_CLIP, "Supprimer l'extrait");
        es(DELETE_THE_CLIP, "Eliminar el clip");
        de(DELETE_THE_CLIP, "Clip löschen");
        ko(DELETE_THE_CLIP, "클립 삭제");
        it(DELETE_THE_CLIP, "Elimina la clip");

        ja(THE_CLIP_IS_USED, "このクリップは使われているため削除できません: %s");
        zh(THE_CLIP_IS_USED, "该片段正在使用，无法删除：%s");
        fr(THE_CLIP_IS_USED, "L'extrait est utilisé et ne peut pas être supprimé : %s");
        es(THE_CLIP_IS_USED, "El clip está en uso y no se puede eliminar: %s");
        de(THE_CLIP_IS_USED, "Der Clip wird verwendet und kann nicht gelöscht werden: %s");
        ko(THE_CLIP_IS_USED, "클립이 사용 중이라 삭제할 수 없습니다: %s");
        it(THE_CLIP_IS_USED, "La clip è in uso e non può essere eliminata: %s");

        ja(THE_CLIP_IS_DELETED, "クリップ %s を削除しました。");
        zh(THE_CLIP_IS_DELETED, "片段 %s 已删除。");
        fr(THE_CLIP_IS_DELETED, "L'extrait %s est supprimé.");
        es(THE_CLIP_IS_DELETED, "El clip %s se ha eliminado.");
        de(THE_CLIP_IS_DELETED, "Der Clip %s ist gelöscht.");
        ko(THE_CLIP_IS_DELETED, "클립 %s을(를) 삭제했습니다.");
        it(THE_CLIP_IS_DELETED, "La clip %s è stata eliminata.");

        ja(GRADE_CLEAN, "良好");
        zh(GRADE_CLEAN, "干净");
        fr(GRADE_CLEAN, "Propre");
        es(GRADE_CLEAN, "Limpio");
        de(GRADE_CLEAN, "Sauber");
        ko(GRADE_CLEAN, "깨끗함");
        it(GRADE_CLEAN, "Pulita");

        ja(GRADE_FAIR, "まずまず");
        zh(GRADE_FAIR, "一般");
        fr(GRADE_FAIR, "Correct");
        es(GRADE_FAIR, "Aceptable");
        de(GRADE_FAIR, "Brauchbar");
        ko(GRADE_FAIR, "보통");
        it(GRADE_FAIR, "Discreta");

        ja(GRADE_POOR, "不十分");
        zh(GRADE_POOR, "较差");
        fr(GRADE_POOR, "Médiocre");
        es(GRADE_POOR, "Deficiente");
        de(GRADE_POOR, "Mangelhaft");
        ko(GRADE_POOR, "나쁨");
        it(GRADE_POOR, "Scarsa");

        ja(NOT_MEASURED, "未測定 (mp3 には ffmpeg が必要)");
        zh(NOT_MEASURED, "未测量（mp3 需要 ffmpeg）");
        fr(NOT_MEASURED, "Non mesuré (un mp3 nécessite ffmpeg)");
        es(NOT_MEASURED, "Sin medir (un mp3 necesita ffmpeg)");
        de(NOT_MEASURED, "Nicht gemessen (eine mp3 braucht ffmpeg)");
        ko(NOT_MEASURED, "측정 안 됨(mp3는 ffmpeg 필요)");
        it(NOT_MEASURED, "Non misurata (un mp3 richiede ffmpeg)");

        ja(PROBLEM_BACKGROUND, "声の下に背景音があります (最も静かな部分が話し声より %.0f dB しか小さくありません)。生成されたセリフにも乗ります");
        zh(PROBLEM_BACKGROUND, "声音下有背景声（最安静的部分只比说话声低 %.0f dB）：生成的语句也会带上它");
        fr(PROBLEM_BACKGROUND, "fond sonore sous la voix (ses parties les plus calmes ne sont que %.0f dB sous la parole) : les répliques générées le porteront");
        es(PROBLEM_BACKGROUND, "sonido de fondo bajo la voz (sus partes más silenciosas están solo %.0f dB por debajo del habla): las frases generadas lo llevarán");
        de(PROBLEM_BACKGROUND, "Hintergrundgeräusch unter der Stimme (die leisesten Stellen liegen nur %.0f dB unter der Sprache): die erzeugten Zeilen tragen es mit");
        ko(PROBLEM_BACKGROUND, "목소리 아래에 배경음이 있습니다(가장 조용한 부분이 말소리보다 %.0f dB만 낮음). 생성된 대사에도 남습니다");
        it(PROBLEM_BACKGROUND, "suono di fondo sotto la voce (le parti più silenziose sono solo %.0f dB sotto il parlato): le battute generate lo porteranno");

        ja(PROBLEM_SOME_BACKGROUND, "いくらか背景音があります (最も静かな部分が話し声より %.0f dB 小さい)");
        zh(PROBLEM_SOME_BACKGROUND, "有一些背景声（最安静的部分比说话声低 %.0f dB）");
        fr(PROBLEM_SOME_BACKGROUND, "un peu de fond sonore (ses parties les plus calmes sont %.0f dB sous la parole)");
        es(PROBLEM_SOME_BACKGROUND, "algo de sonido de fondo (sus partes más silenciosas están %.0f dB por debajo del habla)");
        de(PROBLEM_SOME_BACKGROUND, "etwas Hintergrundgeräusch (die leisesten Stellen liegen %.0f dB unter der Sprache)");
        ko(PROBLEM_SOME_BACKGROUND, "약간의 배경음(가장 조용한 부분이 말소리보다 %.0f dB 낮음)");
        it(PROBLEM_SOME_BACKGROUND, "un po' di suono di fondo (le parti più silenziose sono %.0f dB sotto il parlato)");

        ja(PROBLEM_CLIPPED, "歪んでいます: 最も大きな部分がクリップしています");
        zh(PROBLEM_CLIPPED, "失真：最响的部分被削波");
        fr(PROBLEM_CLIPPED, "distordu : les parties les plus fortes sont écrêtées");
        es(PROBLEM_CLIPPED, "distorsionado: las partes más fuertes están saturadas");
        de(PROBLEM_CLIPPED, "verzerrt: die lautesten Stellen sind übersteuert");
        ko(PROBLEM_CLIPPED, "왜곡됨: 가장 큰 부분이 클리핑됨");
        it(PROBLEM_CLIPPED, "distorta: le parti più forti sono saturate");

        ja(PROBLEM_TOO_SHORT, "短すぎます (%.0f 秒)。12〜30 秒の方がうまく複製できます");
        zh(PROBLEM_TOO_SHORT, "太短（%.0f 秒）：12 到 30 秒克隆效果更好");
        fr(PROBLEM_TOO_SHORT, "court (%.0f s) : 12 à 30 secondes se clonent mieux");
        es(PROBLEM_TOO_SHORT, "corto (%.0f s): de 12 a 30 segundos se clonan mejor");
        de(PROBLEM_TOO_SHORT, "kurz (%.0f s): 12 bis 30 Sekunden lassen sich besser klonen");
        ko(PROBLEM_TOO_SHORT, "짧음(%.0f초): 12~30초가 더 잘 복제됩니다");
        it(PROBLEM_TOO_SHORT, "breve (%.0f s): da 12 a 30 secondi si clonano meglio");

        ja(PROBLEM_TOO_LONG, "長すぎます (%.0f 秒)。Chatterbox は最初の 30 秒だけを使います");
        zh(PROBLEM_TOO_LONG, "太长（%.0f 秒）：Chatterbox 只使用前 30 秒");
        fr(PROBLEM_TOO_LONG, "long (%.0f s) : Chatterbox n'en utilise que les 30 premières secondes");
        es(PROBLEM_TOO_LONG, "largo (%.0f s): Chatterbox solo usa sus primeros 30 segundos");
        de(PROBLEM_TOO_LONG, "lang (%.0f s): Chatterbox verwendet nur die ersten 30 Sekunden");
        ko(PROBLEM_TOO_LONG, "김(%.0f초): Chatterbox는 처음 30초만 사용합니다");
        it(PROBLEM_TOO_LONG, "lunga (%.0f s): Chatterbox ne usa solo i primi 30 secondi");

        ja(REPLACE_THE_CLIP, "クリップを置き換え");
        zh(REPLACE_THE_CLIP, "替换片段");
        fr(REPLACE_THE_CLIP, "Remplacer l'extrait");
        es(REPLACE_THE_CLIP, "Reemplazar el clip");
        de(REPLACE_THE_CLIP, "Clip ersetzen");
        ko(REPLACE_THE_CLIP, "클립 교체");
        it(REPLACE_THE_CLIP, "Sostituisci la clip");

        ja(REPLACE_WITH, "置き換え先");
        zh(REPLACE_WITH, "替换为");
        fr(REPLACE_WITH, "Le remplacer par");
        es(REPLACE_WITH, "Reemplazarlo por");
        de(REPLACE_WITH, "Ersetzen durch");
        ko(REPLACE_WITH, "교체할 클립");
        it(REPLACE_WITH, "Sostituiscila con");

        ja(REPLACE_HELP, "このクリップを使うアクターの声とバックエンドのナレーターがすべて新しいクリップになり、パートはそれで作り直されます。古いクリップは削除するまで残ります。");
        zh(REPLACE_HELP, "所有使用该片段的演员声音和后端旁白都会改用新片段，各部分会用它重新生成。旧片段会保留，直到您删除它。");
        fr(REPLACE_HELP, "Chaque voix de vos acteurs et chaque narrateur de vos moteurs qui utilise l'extrait reçoit le nouveau ; les parties sont refaites avec lui. L'ancien extrait reste jusqu'à ce que vous le supprimiez.");
        es(REPLACE_HELP, "Cada voz de tus actores y cada narrador de tus backends que usa el clip recibe el nuevo; las partes se vuelven a hacer con él. El clip antiguo se queda hasta que lo elimines.");
        de(REPLACE_HELP, "Jede Stimme deiner Schauspieler und jeder Erzähler deiner Backends, die den Clip verwenden, bekommt den neuen; die Teile werden damit neu erstellt. Der alte Clip bleibt, bis du ihn löschst.");
        ko(REPLACE_HELP, "이 클립을 쓰는 배우의 목소리와 백엔드의 내레이터가 모두 새 클립을 쓰게 되고, 파트는 그것으로 다시 만들어집니다. 이전 클립은 삭제할 때까지 남습니다.");
        it(REPLACE_HELP, "Ogni voce dei tuoi attori e ogni narratore dei tuoi backend che usa la clip riceve quella nuova; le parti vengono rifatte con essa. La vecchia clip resta finché non la elimini.");

        ja(THE_CLIP_WAS_REPLACED, "%d 個の声が %s を使うようになりました。");
        zh(THE_CLIP_WAS_REPLACED, "现在有 %d 个声音使用 %s。");
        fr(THE_CLIP_WAS_REPLACED, "%d voix utilisent maintenant %s.");
        es(THE_CLIP_WAS_REPLACED, "%d voces usan ahora %s.");
        de(THE_CLIP_WAS_REPLACED, "%d Stimmen verwenden jetzt %s.");
        ko(THE_CLIP_WAS_REPLACED, "이제 %d개의 목소리가 %s을(를) 사용합니다.");
        it(THE_CLIP_WAS_REPLACED, "%d voci ora usano %s.");

        ja(THE_CLEANED_CLIP_IS_READY, "整えたバージョン %s が元のクリップの下にあります (%s)。再生して、良ければ元のクリップの代わりに使ってください。");
        zh(THE_CLEANED_CLIP_IS_READY, "清理后的版本 %s 已位于其片段下方（%s）。播放试听，如果更好就用它替换原片段。");
        fr(THE_CLEANED_CLIP_IS_READY, "La version nettoyée %s est sous son extrait (%s). Écoutez-la, et utilisez-la à la place de l'extrait si elle sonne mieux.");
        es(THE_CLEANED_CLIP_IS_READY, "La versión limpia %s está bajo su clip (%s). Escúchala y úsala en lugar del clip si suena mejor.");
        de(THE_CLEANED_CLIP_IS_READY, "Die gesäuberte Version %s liegt unter ihrem Clip (%s). Hör sie dir an und verwende sie statt des Clips, wenn sie besser klingt.");
        ko(THE_CLEANED_CLIP_IS_READY, "정리된 버전 %s이(가) 원래 클립 아래에 있습니다(%s). 들어 보고 더 좋으면 클립 대신 사용하세요.");
        it(THE_CLEANED_CLIP_IS_READY, "La versione pulita %s è sotto la sua clip (%s). Ascoltala e usala al posto della clip se suona meglio.");

        ja(THE_CLIP_COULD_NOT_BE_CLEANED, "クリップを整えられませんでした: %s");
        zh(THE_CLIP_COULD_NOT_BE_CLEANED, "无法清理该片段：%s");
        fr(THE_CLIP_COULD_NOT_BE_CLEANED, "L'extrait n'a pas pu être nettoyé : %s");
        es(THE_CLIP_COULD_NOT_BE_CLEANED, "No se pudo limpiar el clip: %s");
        de(THE_CLIP_COULD_NOT_BE_CLEANED, "Der Clip konnte nicht bereinigt werden: %s");
        ko(THE_CLIP_COULD_NOT_BE_CLEANED, "클립을 정리할 수 없습니다: %s");
        it(THE_CLIP_COULD_NOT_BE_CLEANED, "Non è stato possibile pulire la clip: %s");

        ja(A_CLIP_WITH_PROBLEMS, "%s: %s。最良の結果のために録音を手で整えるか、「声とクリップ」(オーディオブック設定) で ffmpeg を使って整えてください。");
        zh(A_CLIP_WITH_PROBLEMS, "%s：%s。为获得最佳效果，请手动清理录音，或在“声音与片段”（有声书设置）中用 ffmpeg 清理。");
        fr(A_CLIP_WITH_PROBLEMS, "%s : %s. Pour le meilleur résultat, nettoyez l'enregistrement à la main, ou avec ffmpeg dans Voix et extraits (Paramètres du livre audio).");
        es(A_CLIP_WITH_PROBLEMS, "%s: %s. Para el mejor resultado limpia la grabación a mano, o con ffmpeg en Voces y clips (Ajustes del audiolibro).");
        de(A_CLIP_WITH_PROBLEMS, "%s: %s. Für das beste Ergebnis bereinige die Aufnahme von Hand oder mit ffmpeg unter Stimmen und Clips (Hörbuch-Einstellungen).");
        ko(A_CLIP_WITH_PROBLEMS, "%s: %s. 가장 좋은 결과를 위해 녹음을 직접 정리하거나, 목소리와 클립(오디오북 설정)에서 ffmpeg로 정리하세요.");
        it(A_CLIP_WITH_PROBLEMS, "%s: %s. Per il miglior risultato pulisci la registrazione a mano, o con ffmpeg in Voci e clip (Impostazioni dell'audiolibro).");

        ja(ADD_A_CHARACTER, "登場人物を追加");
        zh(ADD_A_CHARACTER, "添加人物");
        fr(ADD_A_CHARACTER, "Ajouter un personnage");
        es(ADD_A_CHARACTER, "Añadir un personaje");
        de(ADD_A_CHARACTER, "Eine Figur hinzufügen");
        ko(ADD_A_CHARACTER, "캐릭터 추가");
        it(ADD_A_CHARACTER, "Aggiungi un personaggio");

        ja(ADD_THE_CHARACTERS_OF_THE_BOOK, "本の登場人物を追加");
        zh(ADD_THE_CHARACTERS_OF_THE_BOOK, "添加本书的人物");
        fr(ADD_THE_CHARACTERS_OF_THE_BOOK, "Ajouter les personnages du livre");
        es(ADD_THE_CHARACTERS_OF_THE_BOOK, "Añadir los personajes del libro");
        de(ADD_THE_CHARACTERS_OF_THE_BOOK, "Die Figuren des Buchs hinzufügen");
        ko(ADD_THE_CHARACTERS_OF_THE_BOOK, "책의 캐릭터 추가");
        it(ADD_THE_CHARACTERS_OF_THE_BOOK, "Aggiungi i personaggi del libro");

        ja(CHARACTERS_ADDED, "追加した登場人物: %s");
        zh(CHARACTERS_ADDED, "已添加人物：%s");
        fr(CHARACTERS_ADDED, "Personnages ajoutés : %s");
        es(CHARACTERS_ADDED, "Personajes añadidos: %s");
        de(CHARACTERS_ADDED, "Figuren hinzugefügt: %s");
        ko(CHARACTERS_ADDED, "추가된 캐릭터: %s");
        it(CHARACTERS_ADDED, "Personaggi aggiunti: %s");

        ja(NEW_CHARACTER, "新しい登場人物");
        zh(NEW_CHARACTER, "新人物");
        fr(NEW_CHARACTER, "Nouveau personnage");
        es(NEW_CHARACTER, "Personaje nuevo");
        de(NEW_CHARACTER, "Neue Figur");
        ko(NEW_CHARACTER, "새 캐릭터");
        it(NEW_CHARACTER, "Nuovo personaggio");

        ja(OTHER_NAMES, "別名");
        zh(OTHER_NAMES, "别名");
        fr(OTHER_NAMES, "Autres noms");
        es(OTHER_NAMES, "Otros nombres");
        de(OTHER_NAMES, "Andere Namen");
        ko(OTHER_NAMES, "다른 이름");
        it(OTHER_NAMES, "Altri nomi");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");
        fr(SEPARATED_BY_COMMAS, "Séparés par des virgules");
        es(SEPARATED_BY_COMMAS, "Separados por comas");
        de(SEPARATED_BY_COMMAS, "Durch Kommas getrennt");
        ko(SEPARATED_BY_COMMAS, "쉼표로 구분");
        it(SEPARATED_BY_COMMAS, "Separati da virgole");

        ja(THE_CAST_COULD_NOT_BE_STORED, "キャストを保存できませんでした: %s");
        zh(THE_CAST_COULD_NOT_BE_STORED, "无法保存角色分配：%s");
        fr(THE_CAST_COULD_NOT_BE_STORED, "La distribution n'a pas pu être enregistrée : %s");
        es(THE_CAST_COULD_NOT_BE_STORED, "No se pudo guardar el reparto: %s");
        de(THE_CAST_COULD_NOT_BE_STORED, "Die Besetzung konnte nicht gespeichert werden: %s");
        ko(THE_CAST_COULD_NOT_BE_STORED, "배역을 저장할 수 없었습니다: %s");
        it(THE_CAST_COULD_NOT_BE_STORED, "Non è stato possibile memorizzare il cast: %s");

        ja(THE_CAST_IS_STORED, "キャストを保存しました");
        zh(THE_CAST_IS_STORED, "角色分配已保存");
        fr(THE_CAST_IS_STORED, "La distribution est enregistrée");
        es(THE_CAST_IS_STORED, "El reparto se ha guardado");
        de(THE_CAST_IS_STORED, "Die Besetzung ist gespeichert");
        ko(THE_CAST_IS_STORED, "배역을 저장했습니다");
        it(THE_CAST_IS_STORED, "Il cast è memorizzato");

        ja(ACTOR, "声優");
        zh(ACTOR, "配役の声");
        fr(ACTOR, "Comédien");
        es(ACTOR, "Actor");
        de(ACTOR, "Sprecher");
        ko(ACTOR, "배우");
        it(ACTOR, "Attore");

        ja(ACTORS, "声優");
        zh(ACTORS, "配役声音");
        fr(ACTORS, "Comédiens");
        es(ACTORS, "Actores");
        de(ACTORS, "Sprecher");
        ko(ACTORS, "배우");
        it(ACTORS, "Attori");

        ja(NEW_ACTOR, "新しい声優");
        zh(NEW_ACTOR, "新建声音");
        fr(NEW_ACTOR, "Nouveau comédien");
        es(NEW_ACTOR, "Actor nuevo");
        de(NEW_ACTOR, "Neuer Sprecher");
        ko(NEW_ACTOR, "새 배우");
        it(NEW_ACTOR, "Nuovo attore");

        ja(EDIT_THE_ACTOR, "声優を編集");
        zh(EDIT_THE_ACTOR, "编辑声音");
        fr(EDIT_THE_ACTOR, "Modifier le comédien");
        es(EDIT_THE_ACTOR, "Editar el actor");
        de(EDIT_THE_ACTOR, "Den Sprecher bearbeiten");
        ko(EDIT_THE_ACTOR, "배우 편집");
        it(EDIT_THE_ACTOR, "Modifica l'attore");

        ja(DELETE_THE_ACTOR, "声優を削除");
        zh(DELETE_THE_ACTOR, "删除声音");
        fr(DELETE_THE_ACTOR, "Supprimer le comédien");
        es(DELETE_THE_ACTOR, "Eliminar el actor");
        de(DELETE_THE_ACTOR, "Den Sprecher löschen");
        ko(DELETE_THE_ACTOR, "배우 삭제");
        it(DELETE_THE_ACTOR, "Elimina l'attore");

        ja(THE_ACTOR_IS_DELETED, "声優「%s」を削除します。これを指定しているキャストでは声がなくなり、その他の話者の声で読まれます。クリップはリソースに残ります。");
        zh(THE_ACTOR_IS_DELETED, "将删除声音“%s”。指定它的角色分配将不再有对应声音，改由其他说话者的声音朗读。片段仍保留在你的资源中。");
        fr(THE_ACTOR_IS_DELETED, "Le comédien « %s » est supprimé. Une distribution qui le nomme n'a alors plus de voix pour lui, et c'est la voix des autres locuteurs qui lit. Les extraits restent parmi vos ressources.");
        es(THE_ACTOR_IS_DELETED, "El actor \"%s\" se ha eliminado. Un reparto que lo nombre se queda entonces sin voz para él, y lee la voz de los demás hablantes. Los clips se quedan entre tus recursos.");
        de(THE_ACTOR_IS_DELETED, "Der Sprecher „%s“ ist gelöscht. Eine Besetzung, die ihn nennt, hat dann keine Stimme für ihn, und die Stimme der anderen Sprecher liest. Die Clips bleiben unter deinen Ressourcen.");
        ko(THE_ACTOR_IS_DELETED, "배우 \"%s\"을(를) 삭제합니다. 이 배우를 지정한 배역은 그 음성을 잃게 되며 다른 화자의 음성이 읽습니다. 클립은 내 리소스에 남습니다.");
        it(THE_ACTOR_IS_DELETED, "L'attore \"%s\" viene eliminato. Un cast che lo nomina non ha allora una voce per lui, e legge la voce degli altri parlanti. Le clip restano tra le tue risorse.");

        ja(THE_ACTOR_COULD_NOT_BE_STORED, "声優を保存できませんでした: %s");
        zh(THE_ACTOR_COULD_NOT_BE_STORED, "无法保存声音：%s");
        fr(THE_ACTOR_COULD_NOT_BE_STORED, "Le comédien n'a pas pu être enregistré : %s");
        es(THE_ACTOR_COULD_NOT_BE_STORED, "No se pudo guardar el actor: %s");
        de(THE_ACTOR_COULD_NOT_BE_STORED, "Der Sprecher konnte nicht gespeichert werden: %s");
        ko(THE_ACTOR_COULD_NOT_BE_STORED, "배우를 저장할 수 없었습니다: %s");
        it(THE_ACTOR_COULD_NOT_BE_STORED, "Non è stato possibile memorizzare l'attore: %s");

        ja(NOT_SET, "未設定");
        zh(NOT_SET, "未设置");
        fr(NOT_SET, "Non défini");
        es(NOT_SET, "Sin definir");
        de(NOT_SET, "Nicht festgelegt");
        ko(NOT_SET, "설정 안 됨");
        it(NOT_SET, "Non impostato");

        ja(ACTORS_HELP, "声優はあなたの声です。参照クリップまたは音声サーバーの声のファイルで、感情や言語ごとの声も持てます。どの本でも、ナレーターと登場人物に声優を割り当てられます (オーディオに変換 → キャスト)。");
        zh(ACTORS_HELP, "声音（演员）属于你：参考片段或语音服务器的声音文件，还可为语气或语言设置各自的声音。任何一本书都可以为旁白和人物指定声音（转为音频 → 角色分配）。");
        fr(ACTORS_HELP, "Un comédien est l'une de vos voix : un extrait de référence ou un fichier de voix du serveur de synthèse vocale, avec ses propres voix pour une humeur ou une langue. N'importe quel livre peut attribuer un comédien à son narrateur et à ses personnages (Convertir en audio → Distribution).");
        es(ACTORS_HELP, "Un actor es una voz tuya: un clip de referencia o un archivo de voz del servidor de voz, con voces propias para un estado de ánimo o un idioma. Cualquier libro puede dar un actor a su narrador y a sus personajes (Convertir en audio → Reparto).");
        de(ACTORS_HELP, "Ein Sprecher ist eine Stimme von dir: ein Referenzclip oder eine Stimmdatei des Sprachservers, mit eigenen Stimmen für eine Stimmung oder eine Sprache. Jedes Buch kann seinem Erzähler und seinen Figuren einen Sprecher geben (In Audio umwandeln → Besetzung).");
        ko(ACTORS_HELP, "배우는 내 음성입니다: 음성 서버의 참조 클립이나 음성 파일이며, 분위기나 언어별 고유한 음성을 가질 수 있습니다. 어떤 책이든 내레이터와 캐릭터에 배우를 지정할 수 있습니다 (오디오로 변환 → 배역).");
        it(ACTORS_HELP, "Un attore è una tua voce: una clip di riferimento o un file di voce del server vocale, con voci proprie per un umore o una lingua. Qualsiasi libro può assegnare un attore al suo narratore e ai suoi personaggi (Converti in audio → Cast).");

        ja(THE_CAST_HELP, "この本で誰が読むか: ナレーターと各登場人物に声優を割り当てます。声優はあなたの声です (設定 → 拡張機能の設定 → オーディオブックの設定)。ここで新しく作ることもできます。キャストは本と一緒に保存されます。");
        zh(THE_CAST_HELP, "这本书由谁朗读：为旁白和每个人物指定一个声音。声音是你自己的（设置 → 扩展设置 → 有声书设置），也可在此新建。角色分配随书保存。");
        fr(THE_CAST_HELP, "Qui lit dans ce livre : un comédien pour le narrateur et un pour chaque personnage. Les comédiens sont vos voix (Paramètres → Paramètres des extensions → Paramètres du livre audio) ; un nouveau peut être créé ici. La distribution est gardée avec le livre.");
        es(THE_CAST_HELP, "Quién lee en este libro: un actor para el narrador y uno para cada personaje. Los actores son tus voces (Ajustes → Ajustes de extensiones → Ajustes del audiolibro); aquí se puede crear uno nuevo. El reparto se guarda con el libro.");
        de(THE_CAST_HELP, "Wer in diesem Buch liest: ein Sprecher für den Erzähler und einer für jede Figur. Die Sprecher sind deine Stimmen (Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen); hier kann ein neuer erstellt werden. Die Besetzung wird mit dem Buch aufbewahrt.");
        ko(THE_CAST_HELP, "이 책에서 읽는 사람: 내레이터를 위한 배우 하나와 캐릭터마다 하나씩. 배우는 내 음성이며 (설정 → 확장 기능 설정 → 오디오북 설정), 여기서 새로 만들 수도 있습니다. 배역은 책과 함께 보관됩니다.");
        it(THE_CAST_HELP, "Chi legge in questo libro: un attore per il narratore e uno per ogni personaggio. Gli attori sono le tue voci (Impostazioni → Impostazioni delle estensioni → Impostazioni dell'audiolibro); se ne può creare uno nuovo qui. Il cast viene conservato con il libro.");

        ja(THE_FALLBACK_HELP, "モデルがキャストにない話者を示した場合に読む声優。未設定ならナレーターが読みます。声優のない登場人物も同様です。");
        zh(THE_FALLBACK_HELP, "当模型给出角色分配中没有的说话者时朗读的声音。未设置则由旁白朗读。没有指定声音的人物也一样。");
        fr(THE_FALLBACK_HELP, "Le comédien qui lit quand le modèle nomme un locuteur absent de la distribution. Non défini : le narrateur lit. Un personnage sans comédien est lu de la même façon.");
        es(THE_FALLBACK_HELP, "El actor que lee cuando el modelo nombra a un hablante que el reparto no tiene. Sin definir: lee el narrador. Un personaje sin actor se lee de la misma manera.");
        de(THE_FALLBACK_HELP, "Der Sprecher, der liest, wenn das Modell einen Sprecher nennt, den die Besetzung nicht hat. Nicht festgelegt: Der Erzähler liest. Eine Figur ohne Sprecher wird genauso gelesen.");
        ko(THE_FALLBACK_HELP, "모델이 배역에 없는 화자를 지정했을 때 읽는 배우. 설정하지 않으면 내레이터가 읽습니다. 배우가 없는 캐릭터도 같은 방식으로 읽습니다.");
        it(THE_FALLBACK_HELP, "L'attore che legge quando il modello nomina un parlante che il cast non ha. Non impostato: legge il narratore. Un personaggio senza attore viene letto allo stesso modo.");

        ja(THE_ACTOR_HELP, "声は参照クリップ (音声リソース、または新規)、または音声サーバーの声のファイルです。感情や言語ごとに別の声を指定できます (ささやき、叫び、日本語を話すクリップ)。言語は感情より優先され、指定がなければ上の声で読みます。");
        zh(THE_ACTOR_HELP, "声音是参考片段（你的音频资源之一，或新上传的）或语音服务器自带声音的文件。语气或语言可以有各自的声音：耳语、喊叫、会说日语的片段；语言优先于语气，没有对应声音时用上面的声音朗读。");
        fr(THE_ACTOR_HELP, "La voix est un extrait de référence (l'une de vos ressources audio, ou un nouvel extrait) ou le fichier d'une voix du serveur de synthèse vocale. Une humeur ou une langue peut avoir sa propre voix : un chuchotement, un cri, un extrait qui parle japonais ; la langue l'emporte sur l'humeur, et sans voix pour elles, c'est la voix ci-dessus qui lit.");
        es(THE_ACTOR_HELP, "La voz es un clip de referencia (uno de tus recursos de audio, o uno nuevo) o el archivo de una voz del servidor de voz. Un estado de ánimo o un idioma puede tener una voz propia: un susurro, un grito, un clip que habla japonés; el idioma tiene prioridad sobre el estado de ánimo, y sin una voz para ellos lee la voz de arriba.");
        de(THE_ACTOR_HELP, "Die Stimme ist ein Referenzclip (eine deiner Audio-Ressourcen oder ein neuer) oder die Datei einer Stimme des Sprachservers. Eine Stimmung oder eine Sprache kann eine eigene Stimme haben: ein Flüstern, ein Schrei, ein Clip, der Japanisch spricht; die Sprache hat Vorrang vor der Stimmung, und ohne eigene Stimme für sie liest die obige Stimme.");
        ko(THE_ACTOR_HELP, "음성은 참조 클립 (내 오디오 리소스 중 하나 또는 새 것) 또는 음성 서버의 음성 파일입니다. 분위기나 언어마다 고유한 음성을 둘 수 있습니다: 속삭임, 외침, 일본어를 말하는 클립. 언어가 분위기보다 우선하며, 해당하는 음성이 없으면 위의 음성이 읽습니다.");
        it(THE_ACTOR_HELP, "La voce è una clip di riferimento (una delle tue risorse audio, o una nuova) oppure il file di una voce del server vocale. Un umore o una lingua possono avere una voce propria: un sussurro, un grido, una clip che parla giapponese; la lingua prevale sull'umore, e senza una voce per essi legge la voce qui sopra.");

        ja(AUDIOBOOK_STUDIO, "オーディオブックスタジオ");
        zh(AUDIOBOOK_STUDIO, "有声书工作室");
        fr(AUDIOBOOK_STUDIO, "Studio du livre audio");
        es(AUDIOBOOK_STUDIO, "Estudio de audiolibros");
        de(AUDIOBOOK_STUDIO, "Hörbuch-Studio");
        ko(AUDIOBOOK_STUDIO, "오디오북 스튜디오");
        it(AUDIOBOOK_STUDIO, "Studio dell'audiolibro");

        ja(ANNOTATE, "注釈を付ける");
        zh(ANNOTATE, "标注");
        fr(ANNOTATE, "Annoter");
        es(ANNOTATE, "Anotar");
        de(ANNOTATE, "Annotieren");
        ko(ANNOTATE, "주석 달기");
        it(ANNOTATE, "Annota");

        ja(ANNOTATING, "パートに注釈を付けています ...");
        zh(ANNOTATING, "正在标注该章节 ...");
        fr(ANNOTATING, "Annotation de la partie ...");
        es(ANNOTATING, "Anotando la parte ...");
        de(ANNOTATING, "Der Abschnitt wird annotiert ...");
        ko(ANNOTATING, "파트에 주석 다는 중 ...");
        it(ANNOTATING, "Annotazione della parte ...");

        ja(A_FINAL_PART_IS_NOT_ANNOTATED, "最終版のパートは変更されません。先に状態を「確認済み」または「下書き」にしてください。");
        zh(A_FINAL_PART_IS_NOT_ANNOTATED, "定稿的章节不会被改动：请先把状态改为已审阅或草稿。");
        fr(A_FINAL_PART_IS_NOT_ANNOTATED, "Une partie définitive n'est pas touchée : mettez d'abord son statut à relue ou brouillon.");
        es(A_FINAL_PART_IS_NOT_ANNOTATED, "Una parte definitiva se deja como está: cambia primero su estado a revisada o borrador.");
        de(A_FINAL_PART_IS_NOT_ANNOTATED, "Ein endgültiger Abschnitt bleibt unangetastet: Setze zuerst seinen Status auf „Geprüft“ oder „Entwurf“.");
        ko(A_FINAL_PART_IS_NOT_ANNOTATED, "최종 파트는 그대로 둡니다: 먼저 상태를 검토됨이나 초안으로 설정하세요.");
        it(A_FINAL_PART_IS_NOT_ANNOTATED, "Una parte finale viene lasciata stare: imposta prima il suo stato su rivista o bozza.");

        ja(MAKE_THE_PART, "このスクリプトでパートを作成");
        zh(MAKE_THE_PART, "按此脚本生成该章节");
        fr(MAKE_THE_PART, "Fabriquer la partie à partir de ce script");
        es(MAKE_THE_PART, "Crear la parte a partir de este guion");
        de(MAKE_THE_PART, "Den Abschnitt aus diesem Skript erstellen");
        ko(MAKE_THE_PART, "이 스크립트로 파트 만들기");
        it(MAKE_THE_PART, "Crea la parte da questo copione");

        ja(MAKING_THE_PART, "パートを作成しています ...");
        zh(MAKING_THE_PART, "正在生成该章节 ...");
        fr(MAKING_THE_PART, "Fabrication de la partie ...");
        es(MAKING_THE_PART, "Creando la parte ...");
        de(MAKING_THE_PART, "Der Abschnitt wird erstellt ...");
        ko(MAKING_THE_PART, "파트를 만드는 중 ...");
        it(MAKING_THE_PART, "Creazione della parte ...");

        ja(HEAR_THE_PART, "パートを聞く");
        zh(HEAR_THE_PART, "试听该章节");
        fr(HEAR_THE_PART, "Écouter la partie");
        es(HEAR_THE_PART, "Escuchar la parte");
        de(HEAR_THE_PART, "Den Abschnitt anhören");
        ko(HEAR_THE_PART, "파트 듣기");
        it(HEAR_THE_PART, "Ascolta la parte");

        ja(THE_PART_HAS_NO_AUDIO_YET, "このパートにはまだ音声がありません");
        zh(THE_PART_HAS_NO_AUDIO_YET, "该章节还没有音频");
        fr(THE_PART_HAS_NO_AUDIO_YET, "La partie n'a pas encore d'audio");
        es(THE_PART_HAS_NO_AUDIO_YET, "La parte todavía no tiene audio");
        de(THE_PART_HAS_NO_AUDIO_YET, "Der Abschnitt hat noch kein Audio");
        ko(THE_PART_HAS_NO_AUDIO_YET, "파트에 아직 오디오가 없습니다");
        it(THE_PART_HAS_NO_AUDIO_YET, "La parte non ha ancora audio");

        ja(THIS_PART_HAS_NO_SCRIPT_YET, "このパートにはまだスクリプトがありません。注釈を付けてください。");
        zh(THIS_PART_HAS_NO_SCRIPT_YET, "该章节还没有脚本：请先标注。");
        fr(THIS_PART_HAS_NO_SCRIPT_YET, "Cette partie n'a pas encore de script : annotez-la.");
        es(THIS_PART_HAS_NO_SCRIPT_YET, "Esta parte todavía no tiene guion: anótala.");
        de(THIS_PART_HAS_NO_SCRIPT_YET, "Dieser Abschnitt hat noch kein Skript: Annotiere ihn.");
        ko(THIS_PART_HAS_NO_SCRIPT_YET, "이 파트에는 아직 스크립트가 없습니다: 주석을 다세요.");
        it(THIS_PART_HAS_NO_SCRIPT_YET, "Questa parte non ha ancora un copione: annotala.");

        ja(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "このスクリプトの作成後にパートの本文が変わりました。再度注釈を付けてください (変更したセグメントは保持されます)。");
        zh(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "自该脚本生成后章节文本已更改：请重新标注（你改过的片段会保留）。");
        fr(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "Le texte de la partie a changé depuis la création de ce script : annotez-la à nouveau (les segments que vous avez modifiés sont gardés).");
        es(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "El texto de la parte cambió desde que se hizo este guion: vuelve a anotarla (los segmentos que cambiaste se conservan).");
        de(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "Der Text des Abschnitts hat sich geändert, seit dieses Skript erstellt wurde: Annotiere ihn erneut (die Segmente, die du geändert hast, bleiben erhalten).");
        ko(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "이 스크립트를 만든 후 파트의 텍스트가 변경되었습니다: 다시 주석을 다세요 (변경한 세그먼트는 유지됩니다).");
        it(THE_TEXT_CHANGED_ANNOTATE_AGAIN, "Il testo della parte è cambiato da quando è stato creato questo copione: annotala di nuovo (i segmenti che hai modificato vengono conservati).");

        ja(SPEAKER, "話者");
        zh(SPEAKER, "说话者");
        fr(SPEAKER, "Locuteur");
        es(SPEAKER, "Hablante");
        de(SPEAKER, "Sprecher");
        ko(SPEAKER, "화자");
        it(SPEAKER, "Parlante");

        ja(SPOKEN_TEXT, "読み上げるテキスト");
        zh(SPOKEN_TEXT, "朗读文本");
        fr(SPOKEN_TEXT, "Texte lu");
        es(SPOKEN_TEXT, "Texto hablado");
        de(SPOKEN_TEXT, "Gesprochener Text");
        ko(SPOKEN_TEXT, "발화 텍스트");
        it(SPOKEN_TEXT, "Testo pronunciato");

        ja(DELIVERY, "話し方");
        zh(DELIVERY, "语气");
        fr(DELIVERY, "Diction");
        es(DELIVERY, "Interpretación");
        de(DELIVERY, "Vortrag");
        ko(DELIVERY, "전달 방식");
        it(DELIVERY, "Interpretazione");

        ja(INTENSITY, "強さ");
        zh(INTENSITY, "强度");
        fr(INTENSITY, "Intensité");
        es(INTENSITY, "Intensidad");
        de(INTENSITY, "Intensität");
        ko(INTENSITY, "강도");
        it(INTENSITY, "Intensità");

        ja(EXAGGERATION, "誇張");
        zh(EXAGGERATION, "夸张度");
        fr(EXAGGERATION, "Exagération");
        es(EXAGGERATION, "Exageración");
        de(EXAGGERATION, "Exaggeration");
        ko(EXAGGERATION, "과장도");
        it(EXAGGERATION, "Esagerazione");

        ja(LANGUAGE, "言語");
        zh(LANGUAGE, "语言");
        fr(LANGUAGE, "Langue");
        es(LANGUAGE, "Idioma");
        de(LANGUAGE, "Sprache");
        ko(LANGUAGE, "언어");
        it(LANGUAGE, "Lingua");

        ja(SEED, "シード");
        zh(SEED, "种子");
        fr(SEED, "Graine");
        es(SEED, "Semilla");
        de(SEED, "Seed");
        ko(SEED, "시드");
        it(SEED, "Seed");

        ja(SEED_X, "シード %s");
        zh(SEED_X, "种子 %s");
        fr(SEED_X, "Graine %s");
        es(SEED_X, "Semilla %s");
        de(SEED_X, "Seed %s");
        ko(SEED_X, "시드 %s");
        it(SEED_X, "Seed %s");

        ja(PAUSE_AFTER_SECONDS, "後の間 (秒)");
        zh(PAUSE_AFTER_SECONDS, "之后停顿（秒）");
        fr(PAUSE_AFTER_SECONDS, "Pause après (s)");
        es(PAUSE_AFTER_SECONDS, "Pausa después (s)");
        de(PAUSE_AFTER_SECONDS, "Pause danach (s)");
        ko(PAUSE_AFTER_SECONDS, "이후 일시 정지 (초)");
        it(PAUSE_AFTER_SECONDS, "Pausa dopo (s)");

        ja(STATUS, "状態");
        zh(STATUS, "状态");
        fr(STATUS, "Statut");
        es(STATUS, "Estado");
        de(STATUS, "Status");
        ko(STATUS, "상태");
        it(STATUS, "Stato");

        ja(STATUS_DRAFT, "下書き");
        zh(STATUS_DRAFT, "草稿");
        fr(STATUS_DRAFT, "Brouillon");
        es(STATUS_DRAFT, "Borrador");
        de(STATUS_DRAFT, "Entwurf");
        ko(STATUS_DRAFT, "초안");
        it(STATUS_DRAFT, "Bozza");

        ja(STATUS_REVIEWED, "確認済み");
        zh(STATUS_REVIEWED, "已审阅");
        fr(STATUS_REVIEWED, "Relue");
        es(STATUS_REVIEWED, "Revisada");
        de(STATUS_REVIEWED, "Geprüft");
        ko(STATUS_REVIEWED, "검토됨");
        it(STATUS_REVIEWED, "Rivista");

        ja(STATUS_FINAL, "最終版");
        zh(STATUS_FINAL, "定稿");
        fr(STATUS_FINAL, "Définitive");
        es(STATUS_FINAL, "Definitiva");
        de(STATUS_FINAL, "Endgültig");
        ko(STATUS_FINAL, "최종");
        it(STATUS_FINAL, "Finale");

        ja(LOCKED, "固定");
        zh(LOCKED, "已锁定");
        fr(LOCKED, "Verrouillé");
        es(LOCKED, "Bloqueado");
        de(LOCKED, "Gesperrt");
        ko(LOCKED, "잠김");
        it(LOCKED, "Bloccato");

        ja(TAKES, "テイク");
        zh(TAKES, "录音");
        fr(TAKES, "Prises");
        es(TAKES, "Tomas");
        de(TAKES, "Takes");
        ko(TAKES, "테이크");
        it(TAKES, "Take");

        ja(TAKE, "テイク");
        zh(TAKE, "录音");
        fr(TAKE, "Prise");
        es(TAKE, "Toma");
        de(TAKE, "Take");
        ko(TAKE, "테이크");
        it(TAKE, "Take");

        ja(MADE, "作成済み");
        zh(MADE, "已生成");
        fr(MADE, "Fabriquée");
        es(MADE, "Hecha");
        de(MADE, "Erstellt");
        ko(MADE, "생성 시각");
        it(MADE, "Creato");

        ja(HEAR, "聞く");
        zh(HEAR, "试听");
        fr(HEAR, "Écouter");
        es(HEAR, "Escuchar");
        de(HEAR, "Anhören");
        ko(HEAR, "듣기");
        it(HEAR, "Ascolta");

        ja(ANOTHER_TAKE, "別のテイク");
        zh(ANOTHER_TAKE, "再来一遍");
        fr(ANOTHER_TAKE, "Une autre prise");
        es(ANOTHER_TAKE, "Otra toma");
        de(ANOTHER_TAKE, "Ein anderer Take");
        ko(ANOTHER_TAKE, "다른 테이크");
        it(ANOTHER_TAKE, "Un altro take");

        ja(LOWER_THE_INTENSITY, "強さを下げる");
        zh(LOWER_THE_INTENSITY, "降低强度");
        fr(LOWER_THE_INTENSITY, "Baisser l'intensité");
        es(LOWER_THE_INTENSITY, "Reducir la intensidad");
        de(LOWER_THE_INTENSITY, "Die Intensität verringern");
        ko(LOWER_THE_INTENSITY, "강도 낮추기");
        it(LOWER_THE_INTENSITY, "Riduci l'intensità");

        ja(MORE_PAUSE, "間を長く");
        zh(MORE_PAUSE, "停顿更久");
        fr(MORE_PAUSE, "Plus de pause");
        es(MORE_PAUSE, "Más pausa");
        de(MORE_PAUSE, "Mehr Pause");
        ko(MORE_PAUSE, "일시 정지 늘리기");
        it(MORE_PAUSE, "Più pausa");

        ja(UNLOCK, "固定を解除");
        zh(UNLOCK, "解除锁定");
        fr(UNLOCK, "Déverrouiller");
        es(UNLOCK, "Desbloquear");
        de(UNLOCK, "Entsperren");
        ko(UNLOCK, "잠금 해제");
        it(UNLOCK, "Sblocca");

        ja(APPLY, "適用");
        zh(APPLY, "应用");
        fr(APPLY, "Appliquer");
        es(APPLY, "Aplicar");
        de(APPLY, "Anwenden");
        ko(APPLY, "적용");
        it(APPLY, "Applica");

        ja(CHOOSE_AND_HEAR_THE_TAKE, "このテイクを使って聞く");
        zh(CHOOSE_AND_HEAR_THE_TAKE, "采用此录音并试听");
        fr(CHOOSE_AND_HEAR_THE_TAKE, "Utiliser cette prise et l'écouter");
        es(CHOOSE_AND_HEAR_THE_TAKE, "Usar esta toma y escucharla");
        de(CHOOSE_AND_HEAR_THE_TAKE, "Diesen Take verwenden und anhören");
        ko(CHOOSE_AND_HEAR_THE_TAKE, "이 테이크를 사용하고 듣기");
        it(CHOOSE_AND_HEAR_THE_TAKE, "Usa questo take e ascoltalo");

        ja(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "手動で変更済み: 新しい注釈でもそのまま保持されます。");
        zh(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "已手动更改：重新标注时保持原样。");
        fr(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Modifié à la main : une nouvelle annotation le garde tel quel.");
        es(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Cambiado a mano: una anotación nueva lo conserva tal como está.");
        de(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Von Hand geändert: Eine neue Annotation behält es, wie es ist.");
        ko(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "직접 변경함: 새로 주석을 달아도 그대로 유지됩니다.");
        it(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION, "Modificato a mano: una nuova annotazione lo mantiene com'è.");

        ja(AS_THE_ANNOTATION_MADE_IT, "注釈で作られたまま: 新しい注釈で置き換えられます。");
        zh(AS_THE_ANNOTATION_MADE_IT, "由标注生成：重新标注时会被替换。");
        fr(AS_THE_ANNOTATION_MADE_IT, "Tel que l'annotation l'a fait : une nouvelle annotation le remplace.");
        es(AS_THE_ANNOTATION_MADE_IT, "Tal como lo hizo la anotación: una anotación nueva lo reemplaza.");
        de(AS_THE_ANNOTATION_MADE_IT, "So, wie die Annotation es erstellt hat: Eine neue Annotation ersetzt es.");
        ko(AS_THE_ANNOTATION_MADE_IT, "주석이 만든 그대로: 새로 주석을 달면 교체됩니다.");
        it(AS_THE_ANNOTATION_MADE_IT, "Come l'ha fatto l'annotazione: una nuova annotazione lo sostituisce.");

        ja(THE_REQUEST_TO_THE_SERVER, "音声サーバーに送るリクエスト");
        zh(THE_REQUEST_TO_THE_SERVER, "发给语音服务器的请求");
        fr(THE_REQUEST_TO_THE_SERVER, "La requête que reçoit le serveur de synthèse vocale");
        es(THE_REQUEST_TO_THE_SERVER, "La solicitud que recibe el servidor de voz");
        de(THE_REQUEST_TO_THE_SERVER, "Die Anfrage, die der Sprachserver erhält");
        ko(THE_REQUEST_TO_THE_SERVER, "음성 서버가 받는 요청");
        it(THE_REQUEST_TO_THE_SERVER, "La richiesta che riceve il server vocale");

        ja(MAKING_THE_SEGMENT, "セグメントを作成しています (音声サーバーを起動します) ...");
        zh(MAKING_THE_SEGMENT, "正在生成该片段（会启动语音服务器）...");
        fr(MAKING_THE_SEGMENT, "Fabrication du segment (le serveur de synthèse vocale est démarré pour lui) ...");
        es(MAKING_THE_SEGMENT, "Creando el segmento (se inicia el servidor de voz para ello) ...");
        de(MAKING_THE_SEGMENT, "Das Segment wird erstellt (der Sprachserver wird dafür gestartet) ...");
        ko(MAKING_THE_SEGMENT, "세그먼트를 만드는 중 (이를 위해 음성 서버가 시작됨) ...");
        it(MAKING_THE_SEGMENT, "Creazione del segmento (il server vocale viene avviato per questo) ...");

        ja(THE_JOB_FAILED, "失敗しました: %s");
        zh(THE_JOB_FAILED, "失败：%s");
        fr(THE_JOB_FAILED, "Échec : %s");
        es(THE_JOB_FAILED, "Ha fallado: %s");
        de(THE_JOB_FAILED, "Fehlgeschlagen: %s");
        ko(THE_JOB_FAILED, "실패했습니다: %s");
        it(THE_JOB_FAILED, "Non è riuscito: %s");

        ja(FINAL_PARTS_ARE_LEFT_ALONE, "状態が最終版のため変更しなかったパート: %s");
        zh(FINAL_PARTS_ARE_LEFT_ALONE, "因状态为定稿而未改动的章节：%s");
        fr(FINAL_PARTS_ARE_LEFT_ALONE, "Parties non touchées parce que leur statut est définitif : %s");
        es(FINAL_PARTS_ARE_LEFT_ALONE, "Partes que se dejaron como están porque su estado es definitivo: %s");
        de(FINAL_PARTS_ARE_LEFT_ALONE, "Abschnitte, die unangetastet bleiben, weil ihr Status „Endgültig“ ist: %s");
        ko(FINAL_PARTS_ARE_LEFT_ALONE, "상태가 최종이어서 그대로 둔 파트: %s");
        it(FINAL_PARTS_ARE_LEFT_ALONE, "Parti lasciate stare perché il loro stato è finale: %s");

        ja(PRONUNCIATION, "発音");
        zh(PRONUNCIATION, "发音");
        fr(PRONUNCIATION, "Prononciation");
        es(PRONUNCIATION, "Pronunciación");
        de(PRONUNCIATION, "Aussprache");
        ko(PRONUNCIATION, "발음");
        it(PRONUNCIATION, "Pronuncia");

        ja(NUMBERS_AND_ABBREVIATIONS, "数字と略語");
        zh(NUMBERS_AND_ABBREVIATIONS, "数字和缩写");
        fr(NUMBERS_AND_ABBREVIATIONS, "Nombres et abréviations");
        es(NUMBERS_AND_ABBREVIATIONS, "Números y abreviaturas");
        de(NUMBERS_AND_ABBREVIATIONS, "Zahlen und Abkürzungen");
        ko(NUMBERS_AND_ABBREVIATIONS, "숫자와 약어");
        it(NUMBERS_AND_ABBREVIATIONS, "Numeri e abbreviazioni");

        ja(RULES_ENGLISH, "英語: 数字、略語、番号付きの見出しを単語として読む");
        zh(RULES_ENGLISH, "英语：数字、缩写和带编号的标题按单词朗读");
        fr(RULES_ENGLISH, "Anglais : les nombres, abréviations et titres numérotés sont lus en toutes lettres");
        es(RULES_ENGLISH, "Inglés: los números, las abreviaturas y los títulos numerados se leen como palabras");
        de(RULES_ENGLISH, "Englisch: Zahlen, Abkürzungen und nummerierte Überschriften werden als Wörter gelesen");
        ko(RULES_ENGLISH, "영어: 숫자, 약어, 번호가 있는 제목을 단어로 읽습니다");
        it(RULES_ENGLISH, "Inglese: numeri, abbreviazioni e titoli numerati vengono letti come parole");

        ja(RULES_NONE, "オフ: 辞書のみ (他の言語の本用)");
        zh(RULES_NONE, "关闭：只用词典（用于其他语言的书）");
        fr(RULES_NONE, "Désactivé : seulement le dictionnaire (pour un livre dans une autre langue)");
        es(RULES_NONE, "Desactivado: solo el diccionario (para un libro en otro idioma)");
        de(RULES_NONE, "Aus: nur das Wörterbuch (für ein Buch in einer anderen Sprache)");
        ko(RULES_NONE, "끄기: 사전만 사용 (다른 언어의 책용)");
        it(RULES_NONE, "Disattivato: solo il dizionario (per un libro in un'altra lingua)");

        ja(WORD_BEFORE_A_CHAPTER_NUMBER, "章番号の前の語");
        zh(WORD_BEFORE_A_CHAPTER_NUMBER, "章节编号前的词");
        fr(WORD_BEFORE_A_CHAPTER_NUMBER, "Mot avant un numéro de chapitre");
        es(WORD_BEFORE_A_CHAPTER_NUMBER, "Palabra antes de un número de capítulo");
        de(WORD_BEFORE_A_CHAPTER_NUMBER, "Wort vor einer Kapitelnummer");
        ko(WORD_BEFORE_A_CHAPTER_NUMBER, "챕터 번호 앞의 단어");
        it(WORD_BEFORE_A_CHAPTER_NUMBER, "Parola prima di un numero di capitolo");

        ja(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "番号だけの見出し用:「IV. The Arch」は「Chapter four. The Arch」と読まれます。空欄なら番号のみ。");
        zh(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "用于只有编号的标题：“IV. The Arch”读作“Chapter four. The Arch”。留空则只读编号。");
        fr(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Pour un titre qui n'est qu'un numéro : \"IV. L'Arche\" est lu \"Chapitre quatre. L'Arche\". Vide : juste le numéro.");
        es(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Para un título que es solo un número: \"IV. El Arco\" se lee \"Capítulo cuatro. El Arco\". Vacío: solo el número.");
        de(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Für eine Überschrift, die nur aus einer Zahl besteht: „IV. Der Bogen“ wird als „Kapitel vier. Der Bogen“ gelesen. Leer: nur die Zahl.");
        ko(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "숫자만 있는 제목의 경우: \"IV. The Arch\"는 \"Chapter four. The Arch\"로 읽습니다. 비워 두면 숫자만 읽습니다.");
        it(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER, "Per un titolo che è solo un numero: \"IV. L'Arco\" viene letto \"Capitolo quattro. L'Arco\". Vuoto: solo il numero.");

        ja(PRONUNCIATION_HELP, "本は変更されません。これは音声のための綴りです。辞書は単語全体 (大文字小文字は区別) を読み方に置き換えます: 名前や造語の綴り直し、日本語ならかな読み。読み上げが変わった部分だけが作り直されます。");
        zh(PRONUNCIATION_HELP, "书不会被改动：这是为朗读准备的拼写。词典按整词（区分大小写）替换成要读的内容：人名或自造词的改拼，日语的假名读法。读法改变的片段会重新生成，其余保留。");
        fr(PRONUNCIATION_HELP, "Le livre n'est jamais modifié : c'est ainsi que le texte est épelé pour la voix. Le dictionnaire remplace des mots entiers (la casse compte) par ce qui est dit à leur place : une transcription pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Les morceaux dont le texte lu change sont refaits, les autres sont gardés.");
        es(PRONUNCIATION_HELP, "El libro nunca se cambia: así se escribe el texto para la voz. El diccionario reemplaza palabras completas (importan las mayúsculas y minúsculas) por lo que se dice en su lugar: una reescritura fonética para un nombre o una palabra inventada, o una lectura en kana para el japonés. Los fragmentos cuyo texto hablado cambia se rehacen, los demás se conservan.");
        de(PRONUNCIATION_HELP, "Das Buch wird nie verändert: So wird der Text für die Stimme buchstabiert. Das Wörterbuch ersetzt ganze Wörter (Groß-/Kleinschreibung zählt) durch das, was dafür gesagt wird: eine Umschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Stücke, deren gesprochener Text sich ändert, werden neu erstellt, die anderen bleiben erhalten.");
        ko(PRONUNCIATION_HELP, "책은 절대 변경되지 않습니다: 음성을 위해 텍스트의 철자를 이렇게 바꿉니다. 사전은 전체 단어(대소문자 구분)를 그에 대해 말하는 내용으로 바꿉니다: 이름이나 지어낸 단어의 다른 철자, 또는 일본어의 가나 읽기. 발화 텍스트가 바뀌는 조각은 다시 만들어지고 나머지는 유지됩니다.");
        it(PRONUNCIATION_HELP, "Il libro non viene mai modificato: così viene scritto il testo per la voce. Il dizionario sostituisce parole intere (le maiuscole contano) con ciò che si dice per esse: una riscrittura fonetica per un nome o una parola inventata, o una lettura in kana per il giapponese. I pezzi il cui testo pronunciato cambia vengono rifatti, gli altri vengono conservati.");

        ja(DICTIONARY, "辞書");
        zh(DICTIONARY, "词典");
        fr(DICTIONARY, "Dictionnaire");
        es(DICTIONARY, "Diccionario");
        de(DICTIONARY, "Wörterbuch");
        ko(DICTIONARY, "사전");
        it(DICTIONARY, "Dizionario");

        ja(ADD_A_WORD, "単語を追加");
        zh(ADD_A_WORD, "添加词");
        fr(ADD_A_WORD, "Ajouter un mot");
        es(ADD_A_WORD, "Añadir una palabra");
        de(ADD_A_WORD, "Ein Wort hinzufügen");
        ko(ADD_A_WORD, "단어 추가");
        it(ADD_A_WORD, "Aggiungi una parola");

        ja(WORD_IN_THE_BOOK, "本の中の単語");
        zh(WORD_IN_THE_BOOK, "书中的词");
        fr(WORD_IN_THE_BOOK, "Mot dans le livre");
        es(WORD_IN_THE_BOOK, "Palabra en el libro");
        de(WORD_IN_THE_BOOK, "Wort im Buch");
        ko(WORD_IN_THE_BOOK, "책의 단어");
        it(WORD_IN_THE_BOOK, "Parola nel libro");

        ja(SAID_AS, "読み方");
        zh(SAID_AS, "读作");
        fr(SAID_AS, "Prononcé");
        es(SAID_AS, "Se dice como");
        de(SAID_AS, "Gesprochen als");
        ko(SAID_AS, "읽는 방식");
        it(SAID_AS, "Pronunciata come");

        ja(TRY_A_TEXT, "テキストを試す");
        zh(TRY_A_TEXT, "试一段文字");
        fr(TRY_A_TEXT, "Essayer un texte");
        es(TRY_A_TEXT, "Probar un texto");
        de(TRY_A_TEXT, "Einen Text ausprobieren");
        ko(TRY_A_TEXT, "텍스트 시험");
        it(TRY_A_TEXT, "Prova un testo");

        ja(AS_A_HEADING, "見出しとして");
        zh(AS_A_HEADING, "作为标题");
        fr(AS_A_HEADING, "Comme titre");
        es(AS_A_HEADING, "Como un título");
        de(AS_A_HEADING, "Als Überschrift");
        ko(AS_A_HEADING, "제목으로");
        it(AS_A_HEADING, "Come titolo");

        ja(IT_IS_SAID_AS, "読まれ方");
        zh(IT_IS_SAID_AS, "读出来是");
        fr(IT_IS_SAID_AS, "Il se prononce");
        es(IT_IS_SAID_AS, "Se dice como");
        de(IT_IS_SAID_AS, "Es wird gesprochen als");
        ko(IT_IS_SAID_AS, "이렇게 읽습니다");
        it(IT_IS_SAID_AS, "Viene pronunciato come");

        ja(NOTHING_SCENE_BREAK, "(なし: 場面転換、間)");
        zh(NOTHING_SCENE_BREAK, "（无：场景分隔，停顿）");
        fr(NOTHING_SCENE_BREAK, "(rien : un changement de scène, une pause)");
        es(NOTHING_SCENE_BREAK, "(nada: un cambio de escena, una pausa)");
        de(NOTHING_SCENE_BREAK, "(nichts: ein Szenenwechsel, eine Pause)");
        ko(NOTHING_SCENE_BREAK, "(없음: 장면 전환, 일시 정지)");
        it(NOTHING_SCENE_BREAK, "(nulla: un cambio di scena, una pausa)");

        ja(THE_PRONUNCIATION_IS_STORED, "発音を保存しました");
        zh(THE_PRONUNCIATION_IS_STORED, "发音已保存");
        fr(THE_PRONUNCIATION_IS_STORED, "La prononciation est enregistrée");
        es(THE_PRONUNCIATION_IS_STORED, "La pronunciación se ha guardado");
        de(THE_PRONUNCIATION_IS_STORED, "Die Aussprache ist gespeichert");
        ko(THE_PRONUNCIATION_IS_STORED, "발음을 저장했습니다");
        it(THE_PRONUNCIATION_IS_STORED, "La pronuncia è memorizzata");

        ja(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "発音を保存できませんでした: %s");
        zh(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "无法保存发音：%s");
        fr(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "La prononciation n'a pas pu être enregistrée : %s");
        es(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "No se pudo guardar la pronunciación: %s");
        de(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "Die Aussprache konnte nicht gespeichert werden: %s");
        ko(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "발음을 저장할 수 없었습니다: %s");
        it(THE_PRONUNCIATION_COULD_NOT_BE_STORED, "Non è stato possibile memorizzare la pronuncia: %s");

        ja(IN_THE_BOOK, "本の中: %s");
        zh(IN_THE_BOOK, "书中：%s");
        fr(IN_THE_BOOK, "Dans le livre : %s");
        es(IN_THE_BOOK, "En el libro: %s");
        de(IN_THE_BOOK, "Im Buch: %s");
        ko(IN_THE_BOOK, "책 안: %s");
        it(IN_THE_BOOK, "Nel libro: %s");

        ja(AUDIOBOOK_ZIP, "オーディオブック (章ごとに mp3、zip)");
        zh(AUDIOBOOK_ZIP, "有声书（每章一个 mp3，打包为 zip）");
        fr(AUDIOBOOK_ZIP, "Livre audio (un mp3 par chapitre, dans un zip)");
        es(AUDIOBOOK_ZIP, "Audiolibro (un mp3 por capítulo, en un zip)");
        de(AUDIOBOOK_ZIP, "Hörbuch (eine mp3 je Kapitel, in einer ZIP-Datei)");
        ko(AUDIOBOOK_ZIP, "오디오북 (챕터당 mp3 하나, zip)");
        it(AUDIOBOOK_ZIP, "Audiolibro (un mp3 per capitolo, in uno zip)");

        ja(AUDIOBOOK_ONE_MP3, "オーディオブック (章マーク付きの mp3 1 本)");
        zh(AUDIOBOOK_ONE_MP3, "有声书（带章节标记的单个 mp3）");
        fr(AUDIOBOOK_ONE_MP3, "Livre audio (un seul mp3 avec des marques de chapitre)");
        es(AUDIOBOOK_ONE_MP3, "Audiolibro (un mp3 con marcas de capítulo)");
        de(AUDIOBOOK_ONE_MP3, "Hörbuch (eine mp3 mit Kapitelmarken)");
        ko(AUDIOBOOK_ONE_MP3, "오디오북 (챕터 표시가 있는 mp3 하나)");
        it(AUDIOBOOK_ONE_MP3, "Audiolibro (un mp3 con i segni dei capitoli)");
        ja(AUDIOBOOK_M4B, "オーディオブック (章付きの m4b 1 本)");
        zh(AUDIOBOOK_M4B, "有声书（带章节的单个 m4b）");
        fr(AUDIOBOOK_M4B, "Livre audio (un seul m4b avec des chapitres)");
        es(AUDIOBOOK_M4B, "Audiolibro (un m4b con capítulos)");
        de(AUDIOBOOK_M4B, "Hörbuch (eine m4b mit Kapiteln)");
        ko(AUDIOBOOK_M4B, "오디오북 (챕터가 있는 m4b 하나)");
        it(AUDIOBOOK_M4B, "Audiolibro (un m4b con capitoli)");
        ja(STORAGE_OF_THE_AUDIO, "音声の保存形式 (このインストール全体)");
        zh(STORAGE_OF_THE_AUDIO, "音频的存储格式（对整个安装生效）");
        fr(STORAGE_OF_THE_AUDIO, "Comment l'audio est stocké (pour tout le monde sur cette installation)");
        es(STORAGE_OF_THE_AUDIO, "Cómo se guarda el audio (para todos en esta instalación)");
        de(STORAGE_OF_THE_AUDIO, "Wie das Audio gespeichert wird (für alle auf dieser Installation)");
        ko(STORAGE_OF_THE_AUDIO, "오디오 저장 방식 (이 설치의 모든 사용자에게 적용)");
        it(STORAGE_OF_THE_AUDIO, "Come viene memorizzato l'audio (per tutti in questa installazione)");
        ja(STORAGE_MP3, "mp3 (小さい。m4b は二重にエンコードされます)");
        zh(STORAGE_MP3, "mp3（体积小；m4b 会被二次编码）");
        fr(STORAGE_MP3, "mp3 (léger ; un m4b est encodé une seconde fois)");
        es(STORAGE_MP3, "mp3 (pequeño; un m4b se codifica por segunda vez)");
        de(STORAGE_MP3, "mp3 (klein; eine m4b wird ein zweites Mal kodiert)");
        ko(STORAGE_MP3, "mp3 (작음. m4b는 두 번째로 인코딩됨)");
        it(STORAGE_MP3, "mp3 (piccolo; un m4b viene codificato una seconda volta)");
        ja(STORAGE_WAV, "wav (ロスレス、約10倍のサイズ。ffmpeg が必要)");
        zh(STORAGE_WAV, "wav（无损，约为十倍大小；需要 ffmpeg）");
        fr(STORAGE_WAV, "wav (sans perte, environ dix fois plus gros ; nécessite ffmpeg)");
        es(STORAGE_WAV, "wav (sin pérdida, unas diez veces el tamaño; necesita ffmpeg)");
        de(STORAGE_WAV, "wav (verlustfrei, etwa zehnmal so groß; benötigt ffmpeg)");
        ko(STORAGE_WAV, "wav (무손실, 크기는 약 10배, ffmpeg 필요)");
        it(STORAGE_WAV, "wav (senza perdita, circa dieci volte la dimensione; richiede ffmpeg)");
        ja(STORAGE_OF_THE_AUDIO_HELP, "管理者にのみ表示されます。これから作成するパートに適用され、以前のパートはそのまま使われます。wav は mp3 と zip の書き出し時に ffmpeg でエンコードされ、m4b では一度だけエンコードされます。");
        zh(STORAGE_OF_THE_AUDIO_HELP, "仅管理员可见。仅对之后生成的章节生效，之前的章节保持原样。导出 mp3 和 zip 时由 ffmpeg 编码 wav，导出 m4b 时只编码一次。");
        fr(STORAGE_OF_THE_AUDIO_HELP, "Seuls les administrateurs voient ceci. S'applique aux parties fabriquées à partir de maintenant ; les parties fabriquées avant gardent leur audio et sont comprises telles quelles. Un wav est encodé par ffmpeg pour les exports mp3 et zip et une fois pour le m4b, le m4b n'est donc pas encodé deux fois.");
        es(STORAGE_OF_THE_AUDIO_HELP, "Solo lo ven los administradores. Se aplica a las partes que se creen a partir de ahora; las partes creadas antes conservan su audio y se entienden tal como están. ffmpeg codifica un wav para las exportaciones en mp3 y zip y una vez para el m4b, así que el m4b no se codifica dos veces.");
        de(STORAGE_OF_THE_AUDIO_HELP, "Nur Administratoren sehen dies. Es gilt für die Abschnitte, die ab jetzt erstellt werden; die früher erstellten behalten ihr Audio und werden so verstanden, wie sie sind. Eine wav wird von ffmpeg für die mp3- und ZIP-Exporte und einmal für die m4b kodiert, sodass die m4b nicht zweimal kodiert wird.");
        ko(STORAGE_OF_THE_AUDIO_HELP, "관리자만 볼 수 있습니다. 지금부터 만들어지는 파트에 적용되며, 이전에 만들어진 파트는 오디오를 유지하고 그대로 인식됩니다. wav는 mp3 및 zip 내보내기를 위해 ffmpeg로 인코딩되고 m4b를 위해 한 번 인코딩되므로 m4b가 두 번 인코딩되지 않습니다.");
        it(STORAGE_OF_THE_AUDIO_HELP, "Solo gli amministratori lo vedono. Si applica alle parti create da ora; le parti create prima conservano il loro audio e vengono comprese così come sono. Un wav viene codificato da ffmpeg per le esportazioni mp3 e zip e una volta per l'm4b, così l'm4b non viene codificato due volte.");
        ja(FFMPEG_IS_NOT_INSTALLED, "サーバーに ffmpeg がインストールされていません。mp3 のみ選択でき、m4b の書き出しは利用できません。");
        zh(FFMPEG_IS_NOT_INSTALLED, "服务器未安装 ffmpeg：只能选择 mp3，且不提供 m4b 导出。");
        fr(FFMPEG_IS_NOT_INSTALLED, "ffmpeg n'est pas installé sur le serveur : seul le mp3 peut être choisi, et l'export m4b n'est pas proposé.");
        es(FFMPEG_IS_NOT_INSTALLED, "ffmpeg no está instalado en el servidor: solo se puede elegir mp3 y no se ofrece la exportación en m4b.");
        de(FFMPEG_IS_NOT_INSTALLED, "ffmpeg ist auf dem Server nicht installiert: Es kann nur mp3 gewählt werden, und der m4b-Export wird nicht angeboten.");
        ko(FFMPEG_IS_NOT_INSTALLED, "서버에 ffmpeg가 설치되어 있지 않습니다: mp3만 선택할 수 있으며 m4b 내보내기는 제공되지 않습니다.");
        it(FFMPEG_IS_NOT_INSTALLED, "ffmpeg non è installato sul server: si può scegliere solo mp3 e l'esportazione m4b non viene offerta.");
        ja(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "保存しました。新しいパートはこの形式で保存されます。");
        zh(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "已保存。新章节将按此方式存储。");
        fr(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Enregistré. Les nouvelles parties sont stockées ainsi.");
        es(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Guardado. Las partes nuevas se guardan así.");
        de(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Gespeichert. Neue Abschnitte werden so gespeichert.");
        ko(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "저장했습니다. 새 파트는 이 방식으로 저장됩니다.");
        it(THE_STORAGE_OF_THE_AUDIO_IS_SAVED, "Salvato. Le nuove parti vengono memorizzate così.");

        ja(ONLY_THE_PARTS_MARKED_FINAL, "オーディオブック: 最終版のパートのみ");
        zh(ONLY_THE_PARTS_MARKED_FINAL, "有声书：只含已定稿的章节");
        fr(ONLY_THE_PARTS_MARKED_FINAL, "Livre audio : seulement les parties marquées définitives");
        es(ONLY_THE_PARTS_MARKED_FINAL, "Audiolibro: solo las partes marcadas como definitivas");
        de(ONLY_THE_PARTS_MARKED_FINAL, "Hörbuch: nur die als endgültig markierten Abschnitte");
        ko(ONLY_THE_PARTS_MARKED_FINAL, "오디오북: 최종으로 표시된 파트만");
        it(ONLY_THE_PARTS_MARKED_FINAL, "Audiolibro: solo le parti contrassegnate come finali");

        ja(PART_NUMBER, "パート %s");
        zh(PART_NUMBER, "第 %s 部分");
        fr(PART_NUMBER, "Partie %s");
        es(PART_NUMBER, "Parte %s");
        de(PART_NUMBER, "Abschnitt %s");
        ko(PART_NUMBER, "파트 %s");
        it(PART_NUMBER, "Parte %s");

        ja(NO_AUDIO, "音声なし");
        zh(NO_AUDIO, "没有音频");
        fr(NO_AUDIO, "pas d'audio");
        es(NO_AUDIO, "sin audio");
        de(NO_AUDIO, "kein Audio");
        ko(NO_AUDIO, "오디오 없음");
        it(NO_AUDIO, "nessun audio");

        ja(AUDIO_OLDER_THAN_THE_TEXT, "音声が本文より古い");
        zh(AUDIO_OLDER_THAN_THE_TEXT, "音频比文本旧");
        fr(AUDIO_OLDER_THAN_THE_TEXT, "audio plus ancien que le texte");
        es(AUDIO_OLDER_THAN_THE_TEXT, "audio más antiguo que el texto");
        de(AUDIO_OLDER_THAN_THE_TEXT, "Audio älter als der Text");
        ko(AUDIO_OLDER_THAN_THE_TEXT, "텍스트보다 오래된 오디오");
        it(AUDIO_OLDER_THAN_THE_TEXT, "audio più vecchio del testo");

        ja(AUDIO_OLDER_THAN_THE_SCRIPT, "音声がスクリプトより古い");
        zh(AUDIO_OLDER_THAN_THE_SCRIPT, "音频比脚本旧");
        fr(AUDIO_OLDER_THAN_THE_SCRIPT, "audio plus ancien que le script");
        es(AUDIO_OLDER_THAN_THE_SCRIPT, "audio más antiguo que el guion");
        de(AUDIO_OLDER_THAN_THE_SCRIPT, "Audio älter als das Skript");
        ko(AUDIO_OLDER_THAN_THE_SCRIPT, "스크립트보다 오래된 오디오");
        it(AUDIO_OLDER_THAN_THE_SCRIPT, "audio più vecchio del copione");

        ja(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "オーディオブックから除外したパート: %s。先に変換 (またはスタジオで作り直し) してください。");
        zh(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "有声书中省略的章节：%s。请先转换（或在工作室中重新生成）。");
        fr(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Parties omises du livre audio : %s. Convertissez-les d'abord (ou refaites-les dans le studio).");
        es(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Partes omitidas del audiolibro: %s. Conviértelas (o créalas de nuevo en el estudio) primero.");
        de(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Aus dem Hörbuch ausgelassene Abschnitte: %s. Wandle sie zuerst um (oder erstelle sie im Studio neu).");
        ko(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "오디오북에서 제외된 파트: %s. 먼저 변환하세요 (또는 스튜디오에서 다시 만드세요).");
        it(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK, "Parti escluse dall'audiolibro: %s. Convertile (o rifalle nello studio) prima.");

        ja(NO_PART_HAS_UP_TO_DATE_AUDIO, "最新の音声を持つパートがありません: %s。先にパートを変換してください (音声に変換)、または最終版のパートを選んでください。");
        zh(NO_PART_HAS_UP_TO_DATE_AUDIO, "没有章节拥有最新的音频：%s。请先转换章节（转为音频），或选择已定稿的章节。");
        fr(NO_PART_HAS_UP_TO_DATE_AUDIO, "Aucune partie n'a d'audio à jour : %s. Convertissez d'abord les parties (Convertir en audio), ou choisissez des parties définitives.");
        es(NO_PART_HAS_UP_TO_DATE_AUDIO, "Ninguna parte tiene audio actualizado: %s. Convierte primero las partes (Convertir en audio), o elige partes definitivas.");
        de(NO_PART_HAS_UP_TO_DATE_AUDIO, "Kein Abschnitt hat aktuelles Audio: %s. Wandle zuerst die Abschnitte um (In Audio umwandeln) oder wähle Abschnitte, die endgültig sind.");
        ko(NO_PART_HAS_UP_TO_DATE_AUDIO, "최신 오디오가 있는 파트가 없습니다: %s. 먼저 파트를 변환하거나 (오디오로 변환) 최종 파트를 선택하세요.");
        it(NO_PART_HAS_UP_TO_DATE_AUDIO, "Nessuna parte ha audio aggiornato: %s. Converti prima le parti (Converti in audio), oppure scegli parti finali.");

        ja(NO_PART_TO_EXPORT, "書き出すパートがありません: 最終版のパートがない (または範囲が空) です。");
        zh(NO_PART_TO_EXPORT, "没有可导出的章节：没有已定稿的章节（或范围为空）。");
        fr(NO_PART_TO_EXPORT, "Il n'y a aucune partie à exporter : aucune n'est marquée définitive (ou la plage de parties est vide).");
        es(NO_PART_TO_EXPORT, "No hay ninguna parte que exportar: ninguna está marcada como definitiva (o el intervalo de partes está vacío).");
        de(NO_PART_TO_EXPORT, "Es gibt keinen Abschnitt zum Exportieren: Keiner ist als endgültig markiert (oder der Bereich der Abschnitte ist leer).");
        ko(NO_PART_TO_EXPORT, "내보낼 파트가 없습니다: 최종으로 표시된 파트가 없거나 파트 범위가 비어 있습니다.");
        it(NO_PART_TO_EXPORT, "Non c'è nessuna parte da esportare: nessuna è contrassegnata come finale (oppure l'intervallo di parti è vuoto).");

        ja(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "新しいパートを自動で音声に変換");
        zh(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "自动把新章节转为音频");
        fr(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Convertir automatiquement les nouvelles parties en audio");
        es(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Convertir las partes nuevas en audio automáticamente");
        de(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Neue Abschnitte von selbst in Audio umwandeln");
        ko(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "새 파트를 자동으로 오디오로 변환");
        it(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF, "Converti automaticamente le nuove parti in audio");

        ja(AUTO_NO, "しない");
        zh(AUTO_NO, "否");
        fr(AUTO_NO, "Non");
        es(AUTO_NO, "No");
        de(AUTO_NO, "Nein");
        ko(AUTO_NO, "아니요");
        it(AUTO_NO, "No");

        ja(AUTO_PLAIN, "する: ナレーターが読む");
        zh(AUTO_PLAIN, "是：由旁白朗读");
        fr(AUTO_PLAIN, "Oui : le narrateur lit la partie");
        es(AUTO_PLAIN, "Sí: el narrador lee la parte");
        de(AUTO_PLAIN, "Ja: Der Erzähler liest den Abschnitt");
        ko(AUTO_PLAIN, "예: 내레이터가 파트를 읽음");
        it(AUTO_PLAIN, "Sì: il narratore legge la parte");

        ja(AUTO_LABELLED, "する: 本のモデルでラベルを付け、キャストが声を当てる");
        zh(AUTO_LABELLED, "是：用书的模型标注，由角色分配提供声音");
        fr(AUTO_LABELLED, "Oui : étiquetée par le modèle du livre, la distribution donne les voix");
        es(AUTO_LABELLED, "Sí: etiquetada por el modelo del libro, el reparto da las voces");
        de(AUTO_LABELLED, "Ja: vom Modell des Buchs etikettiert, die Besetzung liefert die Stimmen");
        ko(AUTO_LABELLED, "예: 책의 모델이 라벨을 지정하고 배역이 음성을 제공");
        it(AUTO_LABELLED, "Sì: etichettato dal modello del libro, il cast dà le voci");

        ja(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成されたパートが最終版になると (本に自動注釈と再執筆がある場合はその後)、オーディオブックのキューに入ります。生成が動いている間、音声は待機します。設定 → 拡張機能の設定 → オーディオブックの設定 で選んだ音声サーバーを使います。");
        zh(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "生成的章节定稿后（若书设有自动注释和重写，则在其完成后）会进入有声书队列。你有生成任务运行时，语音会等待。使用在 设置 → 扩展设置 → 有声书设置 中选定的语音服务器。");
        fr(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Quand une partie générée est définitive (l'annotation et la réécriture automatiques du livre sont faites, s'il en a), elle entre dans la file du livre audio. La synthèse vocale attend pendant qu'une de vos générations tourne. Utilise le serveur de synthèse vocale choisi dans Paramètres → Paramètres des extensions → Paramètres du livre audio.");
        es(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Cuando una parte generada es definitiva (la anotación automática y la reescritura del libro están hechas, si las tiene) pasa a la cola del audiolibro. La voz espera mientras se ejecuta una generación tuya. Usa el servidor de voz elegido en Ajustes → Ajustes de extensiones → Ajustes del audiolibro.");
        de(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Wenn ein generierter Abschnitt endgültig ist (die automatische Annotation und Überarbeitung des Buchs sind erledigt, falls es welche hat), kommt er in die Warteschlange des Hörbuchs. Die Sprache wartet, solange eine Generierung von dir läuft. Verwendet den Sprachserver, der unter Einstellungen → Erweiterungseinstellungen → Hörbuch-Einstellungen gewählt ist.");
        ko(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "생성된 파트가 최종이 되면 (책에 자동 주석과 재작성이 있으면 그것이 끝난 후) 오디오북의 대기열에 들어갑니다. 내가 실행한 생성이 진행되는 동안 음성은 대기합니다. 설정 → 확장 기능 설정 → 오디오북 설정에서 선택한 음성 서버를 사용합니다.");
        it(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL, "Quando una parte generata è finale (l'annotazione automatica e la riscrittura del libro sono fatte, se le ha) entra nella coda dell'audiolibro. La voce attende mentre è in corso una tua generazione. Usa il server vocale scelto in Impostazioni → Impostazioni delle estensioni → Impostazioni dell'audiolibro.");

        ja(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新しいパートをオーディオブックのキューに入れました");
        zh(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "新章节已进入有声书队列");
        fr(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "La nouvelle partie est dans la file du livre audio");
        es(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "La parte nueva está en la cola del audiolibro");
        de(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "Der neue Abschnitt ist in der Warteschlange des Hörbuchs");
        ko(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "새 파트가 오디오북 대기열에 있습니다");
        it(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO, "La nuova parte è nella coda dell'audiolibro");

        ja(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "本にラベル付け用のモデルがないため、ナレーターが新しいパートを読みます");
        zh(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "书没有用于标注的模型：由旁白朗读新章节");
        fr(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "Le livre n'a pas de modèle pour l'étiquetage : le narrateur lit la nouvelle partie");
        es(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "El libro no tiene modelo para el etiquetado: el narrador lee la parte nueva");
        de(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "Das Buch hat kein Modell für das Etikettieren: Der Erzähler liest den neuen Abschnitt");
        ko(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "책에 라벨 지정용 모델이 없습니다: 내레이터가 새 파트를 읽습니다");
        it(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART, "Il libro non ha un modello per l'etichettatura: il narratore legge la nuova parte");

        ja(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新しいパートの音声ができました");
        zh(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "新章节的音频已生成");
        fr(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "L'audio de la nouvelle partie est prêt");
        es(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "El audio de la parte nueva está listo");
        de(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "Das Audio des neuen Abschnitts ist fertig");
        ko(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "새 파트의 오디오가 준비되었습니다");
        it(THE_AUDIO_OF_THE_NEW_PART_IS_READY, "L'audio della nuova parte è pronto");

        ja(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "新しいパートの音声を作成できませんでした: %s");
        zh(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "无法生成新章节的音频：%s");
        fr(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "L'audio de la nouvelle partie n'a pas pu être fabriqué : %s");
        es(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "No se pudo crear el audio de la parte nueva: %s");
        de(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "Das Audio des neuen Abschnitts konnte nicht erstellt werden: %s");
        ko(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "새 파트의 오디오를 만들 수 없었습니다: %s");
        it(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE, "Non è stato possibile creare l'audio della nuova parte: %s");

        ja(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "指定のない本の新しいパートを自動変換");
        zh(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "未指定的书自动转换新章节");
        fr(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Convertir automatiquement les nouvelles parties, pour les livres qui ne disent rien");
        es(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Convertir las partes nuevas automáticamente, para los libros que no indican nada");
        de(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Neue Abschnitte von selbst umwandeln, für die Bücher, die nichts dazu sagen");
        ko(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "설정이 없는 책에 대해 새 파트를 자동으로 변환");
        it(AUTOMATIC_CONVERSION_OF_NEW_PARTS, "Converti automaticamente le nuove parti, per i libri che non dicono nulla");

        ja(PREVIEW, "試聴");
        zh(PREVIEW, "试听");
        fr(PREVIEW, "Aperçu");
        es(PREVIEW, "Vista previa");
        de(PREVIEW, "Vorschau");
        ko(PREVIEW, "미리 보기");
        it(PREVIEW, "Anteprima");

        ja(MAKING_THE_SAMPLE, "サンプルを作成中 ...");
        zh(MAKING_THE_SAMPLE, "正在生成样本 ...");
        fr(MAKING_THE_SAMPLE, "Fabrication de l'échantillon ...");
        es(MAKING_THE_SAMPLE, "Creando la muestra ...");
        de(MAKING_THE_SAMPLE, "Das Beispiel wird erstellt ...");
        ko(MAKING_THE_SAMPLE, "샘플을 만드는 중 ...");
        it(MAKING_THE_SAMPLE, "Creazione dell'esempio ...");

        ja(PICK_A_VOICE_FIRST, "先に声を選んでください");
        zh(PICK_A_VOICE_FIRST, "请先选择声音");
        fr(PICK_A_VOICE_FIRST, "Choisissez d'abord une voix");
        es(PICK_A_VOICE_FIRST, "Elige primero una voz");
        de(PICK_A_VOICE_FIRST, "Wähle zuerst eine Stimme");
        ko(PICK_A_VOICE_FIRST, "먼저 음성을 선택하세요");
        it(PICK_A_VOICE_FIRST, "Scegli prima una voce");

        ja(THE_VOICE_COULD_NOT_BE_HEARD, "声を再生できませんでした: %s");
        zh(THE_VOICE_COULD_NOT_BE_HEARD, "无法试听该声音：%s");
        fr(THE_VOICE_COULD_NOT_BE_HEARD, "La voix n'a pas pu être écoutée : %s");
        es(THE_VOICE_COULD_NOT_BE_HEARD, "No se pudo escuchar la voz: %s");
        de(THE_VOICE_COULD_NOT_BE_HEARD, "Die Stimme konnte nicht angehört werden: %s");
        ko(THE_VOICE_COULD_NOT_BE_HEARD, "음성을 들을 수 없었습니다: %s");
        it(THE_VOICE_COULD_NOT_BE_HEARD, "Non è stato possibile ascoltare la voce: %s");

        ja(KEY_OF_THE_REQUESTS, "バッチ前後のリクエストのキー");
        zh(KEY_OF_THE_REQUESTS, "批处理前后请求的密钥");
        fr(KEY_OF_THE_REQUESTS, "Clé des requêtes autour d'un lot");
        es(KEY_OF_THE_REQUESTS, "Clave de las solicitudes alrededor de un lote");
        de(KEY_OF_THE_REQUESTS, "Schlüssel der Anfragen rund um einen Stapel");
        ko(KEY_OF_THE_REQUESTS, "배치 전후 요청의 키");
        it(KEY_OF_THE_REQUESTS, "Chiave delle richieste attorno a un batch");

        ja(KEY_OF_THE_REQUESTS_HELP, "バッチの前後のすべてのリクエストに Authorization: Bearer として付けられます。暗号化して保存され、再表示されないので、ヘッダーに書く必要はありません。空: 上の API キーを使います。");
        zh(KEY_OF_THE_REQUESTS_HELP, "作为 Authorization: Bearer 附加到批处理前后的每个请求，加密保存且不再显示，因此无需写入请求头。留空：使用上面的 API 密钥。");
        fr(KEY_OF_THE_REQUESTS_HELP, "Envoyée comme Authorization: Bearer avec chaque requête avant et après un lot, stockée chiffrée et jamais réaffichée, pour ne pas avoir à l'écrire dans leurs en-têtes. Vide : la clé d'API ci-dessus est utilisée.");
        es(KEY_OF_THE_REQUESTS_HELP, "Se envía como Authorization: Bearer con cada solicitud anterior y posterior a un lote, se guarda cifrada y no se vuelve a mostrar, para que no haya que escribirla en sus cabeceras. Vacío: se usa la clave de API de arriba.");
        de(KEY_OF_THE_REQUESTS_HELP, "Wird als Authorization: Bearer bei jeder Anfrage vor und nach einem Stapel gesendet, verschlüsselt gespeichert und nie wieder angezeigt, damit er nicht in ihre Header geschrieben werden muss. Leer: Der API-Schlüssel oben wird verwendet.");
        ko(KEY_OF_THE_REQUESTS_HELP, "배치 전후의 모든 요청에 Authorization: Bearer로 전송되며, 암호화되어 저장되고 다시 표시되지 않으므로 헤더에 쓸 필요가 없습니다. 비워 두면 위의 API 키를 사용합니다.");
        it(KEY_OF_THE_REQUESTS_HELP, "Inviata come Authorization: Bearer con ogni richiesta prima e dopo un batch, memorizzata cifrata e mai più mostrata, così non va scritta nelle loro intestazioni. Vuoto: viene usata la chiave API qui sopra.");

        ja(THE_REQUEST_AFTER_THE_TEST_FAILED, "サーバーは応答しましたが、テスト後のリクエストが失敗しました: %s");
        zh(THE_REQUEST_AFTER_THE_TEST_FAILED, "服务器有响应，但测试后的请求失败：%s");
        fr(THE_REQUEST_AFTER_THE_TEST_FAILED, "Le serveur répond, mais la requête après le test a échoué : %s");
        es(THE_REQUEST_AFTER_THE_TEST_FAILED, "El servidor responde, pero falló la solicitud posterior a la prueba: %s");
        de(THE_REQUEST_AFTER_THE_TEST_FAILED, "Der Server antwortet, aber die Anfrage nach dem Test ist fehlgeschlagen: %s");
        ko(THE_REQUEST_AFTER_THE_TEST_FAILED, "서버가 응답하지만 테스트 후의 요청이 실패했습니다: %s");
        it(THE_REQUEST_AFTER_THE_TEST_FAILED, "Il server risponde, ma la richiesta dopo il test non è riuscita: %s");

        ja(THE_REQUESTS_AROUND_THE_TEST_RAN, "テストの前後のリクエストを実行しました。");
        zh(THE_REQUESTS_AROUND_THE_TEST_RAN, "已执行测试前后的请求。");
        fr(THE_REQUESTS_AROUND_THE_TEST_RAN, "Les requêtes avant et après le test ont été faites. ");
        es(THE_REQUESTS_AROUND_THE_TEST_RAN, "Se hicieron las solicitudes anteriores y posteriores a la prueba. ");
        de(THE_REQUESTS_AROUND_THE_TEST_RAN, "Die Anfragen vor und nach dem Test wurden ausgeführt. ");
        ko(THE_REQUESTS_AROUND_THE_TEST_RAN, "테스트 전후의 요청을 실행했습니다.");
        it(THE_REQUESTS_AROUND_THE_TEST_RAN, "Le richieste prima e dopo il test sono state eseguite.");

        ja(TEST_HELP, "テストはバッチ前のリクエスト（停止中のサーバーを起動）を実行し、サーバーに問い合わせ、バッチ後のリクエストを実行します。");
        zh(TEST_HELP, "测试会先执行批处理前的请求（启动已停止的服务器），再询问服务器，最后执行批处理后的请求。");
        fr(TEST_HELP, "Le test fait les requêtes d'avant un lot (elles démarrent un serveur arrêté), interroge le serveur, puis fait les requêtes d'après.");
        es(TEST_HELP, "La prueba hace las solicitudes anteriores a un lote (inician un servidor que está detenido), consulta al servidor y hace las solicitudes posteriores.");
        de(TEST_HELP, "Der Test führt die Anfragen vor einem Stapel aus (sie starten einen Server, der gestoppt ist), fragt den Server und führt die Anfragen danach aus.");
        ko(TEST_HELP, "테스트는 배치 전의 요청(중지된 서버를 시작함)을 실행하고, 서버에 요청한 다음, 배치 후의 요청을 실행합니다.");
        it(TEST_HELP, "Il test esegue le richieste prima di un batch (avviano un server che viene fermato), interroga il server ed esegue le richieste dopo di esso.");

        ja(DOWNLOAD_THE_AUDIOBOOK, "オーディオブック全体");
        zh(DOWNLOAD_THE_AUDIOBOOK, "整本有声书");
        fr(DOWNLOAD_THE_AUDIOBOOK, "Tout le livre audio");
        es(DOWNLOAD_THE_AUDIOBOOK, "El audiolibro completo");
        de(DOWNLOAD_THE_AUDIOBOOK, "Das gesamte Hörbuch");
        ko(DOWNLOAD_THE_AUDIOBOOK, "오디오북 전체");
        it(DOWNLOAD_THE_AUDIOBOOK, "L'intero audiolibro");

        ja(DOWNLOAD_THE_AUDIOBOOK_HELP, "最新の音声があるパートをすべて、パートごとの章を付けた1つのmp3にまとめ、ダウンロードできるようにします");
        zh(DOWNLOAD_THE_AUDIOBOOK_HELP, "把所有音频最新的章节合并为一个 mp3（每个章节一个章节标记），并提供下载");
        fr(DOWNLOAD_THE_AUDIOBOOK_HELP, "Joint toutes les parties ayant un audio à jour en un seul mp3 avec un chapitre par partie, et le propose au téléchargement");
        es(DOWNLOAD_THE_AUDIOBOOK_HELP, "Une todas las partes que tienen audio actualizado en un mp3 con un capítulo por cada parte y lo ofrece para descargar");
        de(DOWNLOAD_THE_AUDIOBOOK_HELP, "Fügt alle Abschnitte mit aktuellem Audio zu einer mp3 mit einem Kapitel je Abschnitt zusammen und bietet sie zum Herunterladen an");
        ko(DOWNLOAD_THE_AUDIOBOOK_HELP, "최신 오디오가 있는 모든 파트를 파트마다 챕터가 있는 하나의 mp3로 합쳐 다운로드할 수 있게 합니다");
        it(DOWNLOAD_THE_AUDIOBOOK_HELP, "Unisce tutte le parti con audio aggiornato in un unico mp3 con un capitolo per ogni parte, e lo offre per il download");

        ja(THE_AUDIOBOOK_IS_BEING_JOINED, "オーディオブックを結合しています ...");
        zh(THE_AUDIOBOOK_IS_BEING_JOINED, "正在合并有声书 ...");
        fr(THE_AUDIOBOOK_IS_BEING_JOINED, "Assemblage du livre audio ...");
        es(THE_AUDIOBOOK_IS_BEING_JOINED, "Uniendo el audiolibro ...");
        de(THE_AUDIOBOOK_IS_BEING_JOINED, "Das Hörbuch wird zusammengefügt ...");
        ko(THE_AUDIOBOOK_IS_BEING_JOINED, "오디오북을 합치는 중 ...");
        it(THE_AUDIOBOOK_IS_BEING_JOINED, "L'audiolibro viene unito ...");

        ja(DOWNLOAD_THE_AUDIOBOOK_FILE, "オーディオブックをダウンロード（約 %s MB）");
        zh(DOWNLOAD_THE_AUDIOBOOK_FILE, "下载有声书（约 %s MB）");
        fr(DOWNLOAD_THE_AUDIOBOOK_FILE, "Télécharger le livre audio (environ %s Mo)");
        es(DOWNLOAD_THE_AUDIOBOOK_FILE, "Descargar el audiolibro (unos %s MB)");
        de(DOWNLOAD_THE_AUDIOBOOK_FILE, "Das Hörbuch herunterladen (etwa %s MB)");
        ko(DOWNLOAD_THE_AUDIOBOOK_FILE, "오디오북 다운로드 (약 %s MB)");
        it(DOWNLOAD_THE_AUDIOBOOK_FILE, "Scarica l'audiolibro (circa %s MB)");

        ja(BACKEND_IN_USE, "使用するバックエンド");
        zh(BACKEND_IN_USE, "使用的后端");
        fr(BACKEND_IN_USE, "Moteur utilisé");
        es(BACKEND_IN_USE, "Backend en uso");
        de(BACKEND_IN_USE, "Verwendetes Backend");
        ko(BACKEND_IN_USE, "사용 중인 백엔드");
        it(BACKEND_IN_USE, "Backend in uso");

        ja(BACKEND_IN_USE_HELP, "変換に使う音声サーバー。共有のものは管理者のもので、アドレスやキーは表示されません。");
        zh(BACKEND_IN_USE_HELP, "转换所用的语音服务器。共享的服务器属于管理员，其地址和密钥不会显示给你。");
        fr(BACKEND_IN_USE_HELP, "Le serveur de synthèse vocale qu'utilisent vos conversions. Un serveur partagé appartient à l'administrateur ; son adresse et ses clés ne vous sont pas montrées.");
        es(BACKEND_IN_USE_HELP, "El servidor de voz que usan tus conversiones. Uno compartido pertenece al administrador; no se te muestran su dirección ni sus claves.");
        de(BACKEND_IN_USE_HELP, "Der Sprachserver, den deine Umwandlungen verwenden. Ein geteilter gehört dem Administrator; seine Adresse und Schlüssel werden dir nicht angezeigt.");
        ko(BACKEND_IN_USE_HELP, "변환에 사용하는 음성 서버. 공유 서버는 관리자의 것이며, 주소와 키는 표시되지 않습니다.");
        it(BACKEND_IN_USE_HELP, "Il server vocale che usano le tue conversioni. Uno condiviso appartiene all'amministratore; il suo indirizzo e le sue chiavi non ti vengono mostrati.");

        ja(SHARED_MARK, "（共有）");
        zh(SHARED_MARK, "（共享）");
        fr(SHARED_MARK, " (partagé)");
        es(SHARED_MARK, " (compartido)");
        de(SHARED_MARK, " (geteilt)");
        ko(SHARED_MARK, " (공유)");
        it(SHARED_MARK, " (condiviso)");

        ja(OWN_SPEECH_SERVERS_ARE_OFF, "管理者はユーザー自身の音声サーバーの使用を許可していません。以前に追加したものは保持されますが、使われません。");
        zh(OWN_SPEECH_SERVERS_ARE_OFF, "管理员不允许用户使用自己的语音服务器。你之前添加的会保留，但不会被使用。");
        fr(OWN_SPEECH_SERVERS_ARE_OFF, "L'administrateur ne permet pas aux utilisateurs d'utiliser leurs propres serveurs de synthèse vocale. Ceux que vous avez ajoutés avant sont gardés et ne sont pas utilisés.");
        es(OWN_SPEECH_SERVERS_ARE_OFF, "El administrador no permite que los usuarios usen servidores de voz propios. Los que añadiste antes se conservan y no se usan.");
        de(OWN_SPEECH_SERVERS_ARE_OFF, "Der Administrator lässt Benutzer keine eigenen Sprachserver verwenden. Die, die du früher hinzugefügt hast, bleiben erhalten und werden nicht verwendet.");
        ko(OWN_SPEECH_SERVERS_ARE_OFF, "관리자가 사용자 자신의 음성 서버 사용을 허용하지 않습니다. 이전에 추가한 서버는 유지되지만 사용되지 않습니다.");
        it(OWN_SPEECH_SERVERS_ARE_OFF, "L'amministratore non permette agli utenti di usare server vocali propri. Quelli che hai aggiunto prima vengono conservati e non usati.");

        ja(SHARED_SPEECH_SERVERS, "共有の音声サーバー");
        zh(SHARED_SPEECH_SERVERS, "共享语音服务器");
        fr(SHARED_SPEECH_SERVERS, "Serveurs de synthèse vocale partagés");
        es(SHARED_SPEECH_SERVERS, "Servidores de voz compartidos");
        de(SHARED_SPEECH_SERVERS, "Geteilte Sprachserver");
        ko(SHARED_SPEECH_SERVERS, "공유 음성 서버");
        it(SHARED_SPEECH_SERVERS, "Server vocali condivisi");

        ja(SHARED_SPEECH_SERVERS_HELP, "管理者のみに表示されます。ユーザーが使える音声サーバーと、ユーザー自身のサーバーを追加できるかどうか。");
        zh(SHARED_SPEECH_SERVERS_HELP, "仅管理员可见。用户可以使用的语音服务器，以及用户是否可以添加自己的服务器。");
        fr(SHARED_SPEECH_SERVERS_HELP, "Seuls les administrateurs voient ceci. Les serveurs de synthèse vocale que les utilisateurs peuvent utiliser, et s'ils peuvent ajouter les leurs.");
        es(SHARED_SPEECH_SERVERS_HELP, "Solo lo ven los administradores. Servidores de voz que pueden usar los usuarios, y si pueden añadir los suyos.");
        de(SHARED_SPEECH_SERVERS_HELP, "Nur Administratoren sehen dies. Sprachserver, die Benutzer verwenden dürfen, und ob sie eigene hinzufügen dürfen.");
        ko(SHARED_SPEECH_SERVERS_HELP, "관리자만 볼 수 있습니다. 사용자가 사용할 수 있는 음성 서버와 자체 서버를 추가할 수 있는지 여부.");
        it(SHARED_SPEECH_SERVERS_HELP, "Solo gli amministratori lo vedono. Server vocali che gli utenti possono usare, e se possono aggiungerne di propri.");

        ja(SHARED_SPEECH_SERVERS_DIALOG_HELP, "共有サーバーを使えるユーザーは、使用するバックエンドとしてそれを選べます。見えるのは名前だけで、アドレス、キー、リクエストは見えません。ナレーターの声はサーバーの声にしてください (クリップは 1 人のユーザーのものです)。");
        zh(SHARED_SPEECH_SERVERS_DIALOG_HELP, "可以使用共享服务器的用户把它选为所用后端。他们只能看到名称，看不到地址、密钥和请求。旁白声音应使用服务器自带的声音（参考音频属于某一个用户）。");
        fr(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Un utilisateur qui peut utiliser un serveur partagé le choisit comme moteur utilisé. Il voit son nom, jamais son adresse, ses clés ni ses requêtes. La voix du narrateur devrait être une voix du serveur (un extrait appartient à un seul utilisateur).");
        es(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Un usuario que puede usar un servidor compartido lo elige como backend en uso. Ve su nombre, nunca su dirección, sus claves ni sus solicitudes. La voz del narrador debería ser una voz del servidor (un clip pertenece a un usuario).");
        de(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Ein Benutzer, der einen geteilten Server verwenden darf, wählt ihn als verwendetes Backend. Er sieht seinen Namen, nie seine Adresse, seine Schlüssel oder seine Anfragen. Die Erzählerstimme sollte eine Stimme des Servers sein (ein Clip gehört einem Benutzer).");
        ko(SHARED_SPEECH_SERVERS_DIALOG_HELP, "공유 서버를 사용할 수 있는 사용자는 이를 사용 중인 백엔드로 선택합니다. 사용자는 서버의 이름만 보며 주소, 키, 요청은 볼 수 없습니다. 내레이터 음성은 서버의 음성이어야 합니다 (클립은 한 사용자에게 속합니다).");
        it(SHARED_SPEECH_SERVERS_DIALOG_HELP, "Un utente che può usare un server condiviso lo sceglie come backend in uso. Vede il suo nome, mai il suo indirizzo, le sue chiavi o le sue richieste. La voce narrante dovrebbe essere una voce del server (una clip appartiene a un utente).");

        ja(ALL_USERS_MAY_USE_IT, "すべてのユーザーが使用可能");
        zh(ALL_USERS_MAY_USE_IT, "所有用户都可以使用");
        fr(ALL_USERS_MAY_USE_IT, "Tous les utilisateurs peuvent l'utiliser");
        es(ALL_USERS_MAY_USE_IT, "Todos los usuarios pueden usarlo");
        de(ALL_USERS_MAY_USE_IT, "Alle Benutzer dürfen ihn verwenden");
        ko(ALL_USERS_MAY_USE_IT, "모든 사용자가 사용할 수 있음");
        it(ALL_USERS_MAY_USE_IT, "Tutti gli utenti possono usarlo");

        ja(THESE_USERS_MAY_USE_IT, "使用できるユーザー");
        zh(THESE_USERS_MAY_USE_IT, "可以使用的用户");
        fr(THESE_USERS_MAY_USE_IT, "Ces utilisateurs peuvent l'utiliser");
        es(THESE_USERS_MAY_USE_IT, "Estos usuarios pueden usarlo");
        de(THESE_USERS_MAY_USE_IT, "Diese Benutzer dürfen ihn verwenden");
        ko(THESE_USERS_MAY_USE_IT, "이 사용자들이 사용할 수 있음");
        it(THESE_USERS_MAY_USE_IT, "Questi utenti possono usarlo");

        ja(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "ユーザーが自分の音声サーバーを追加可能");
        zh(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "用户可以添加自己的语音服务器");
        fr(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Les utilisateurs peuvent ajouter leurs propres serveurs de synthèse vocale");
        es(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Los usuarios pueden añadir servidores de voz propios");
        de(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Benutzer dürfen eigene Sprachserver hinzufügen");
        ko(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "사용자가 자체 음성 서버를 추가할 수 있음");
        it(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS, "Gli utenti possono aggiungere server vocali propri");

        ja(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "オフの場合、ユーザーは共有サーバーのみ使用できます。以前に追加したサーバーは保持されますが、使われません。");
        zh(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "关闭后，用户只能使用共享服务器；他们之前添加的服务器会保留，但不会被使用。");
        fr(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Quand c'est désactivé, les utilisateurs ne peuvent utiliser que les serveurs partagés ; les serveurs qu'ils ont ajoutés avant sont gardés et ne sont pas utilisés.");
        es(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Cuando está desactivado, los usuarios solo pueden usar los servidores compartidos; los servidores que añadieron antes se conservan y no se usan.");
        de(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Wenn dies aus ist, können Benutzer nur die geteilten Server verwenden; die Server, die sie früher hinzugefügt haben, bleiben erhalten und werden nicht verwendet.");
        ko(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "끄면 사용자는 공유 서버만 사용할 수 있으며, 이전에 추가한 서버는 유지되지만 사용되지 않습니다.");
        it(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP, "Quando è disattivato, gli utenti possono usare solo i server condivisi; i server che hanno aggiunto prima vengono conservati e non usati.");

        ja(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "共有の音声サーバー「%s」にアドレスがありません");
        zh(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "共享语音服务器“%s”没有地址");
        fr(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "Le serveur de synthèse vocale partagé « %s » n'a pas d'adresse");
        es(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "El servidor de voz compartido \"%s\" no tiene dirección");
        de(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "Der geteilte Sprachserver „%s“ hat keine Adresse");
        ko(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "공유 음성 서버 \"%s\"에 주소가 없습니다");
        it(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS, "Il server vocale condiviso \"%s\" non ha un indirizzo");

        ja(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "共有の音声サーバーを保存しました");
        zh(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "共享语音服务器已保存");
        fr(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Les serveurs de synthèse vocale partagés sont enregistrés");
        es(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Los servidores de voz compartidos se han guardado");
        de(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "Die geteilten Sprachserver sind gespeichert");
        ko(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "공유 음성 서버를 저장했습니다");
        it(THE_SHARED_SPEECH_SERVERS_ARE_SAVED, "I server vocali condivisi sono salvati");

        ja(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "このパート (と音声のない次のパート) をブラウザの音声で読み上げる");
        zh(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "用浏览器的语音朗读此章节（以及之后没有音频的章节）");
        fr(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Lire cette partie (et les suivantes sans audio) avec la voix du navigateur");
        es(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Leer esta parte (y las siguientes sin audio) con la voz del navegador");
        de(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Diesen Abschnitt (und die folgenden ohne Audio) mit der Stimme des Browsers lesen");
        ko(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "브라우저의 음성으로 이 파트 (그리고 오디오가 없는 다음 파트들)를 읽기");
        it(READ_THIS_PART_WITH_THE_BROWSER_VOICE, "Leggi questa parte (e le successive senza audio) con la voce del browser");

        ja(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "パートの音量をそろえる");
        zh(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "统一各章节的响度");
        fr(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Égaliser le volume des parties");
        es(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Igualar el volumen de las partes");
        de(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Die Lautstärke der Abschnitte angleichen");
        ko(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "파트의 음량 평준화");
        it(EVEN_OUT_THE_LOUDNESS_OF_THE_PARTS, "Uniforma il volume delle parti");

        ja(TARGET_LOUDNESS_LUFS, "目標音量 (LUFS)");
        zh(TARGET_LOUDNESS_LUFS, "目标响度 (LUFS)");
        fr(TARGET_LOUDNESS_LUFS, "Volume cible (LUFS)");
        es(TARGET_LOUDNESS_LUFS, "Volumen objetivo (LUFS)");
        de(TARGET_LOUDNESS_LUFS, "Ziellautstärke (LUFS)");
        ko(TARGET_LOUDNESS_LUFS, "목표 음량 (LUFS)");
        it(TARGET_LOUDNESS_LUFS, "Volume obiettivo (LUFS)");

        ja(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "各パートの結合した音声を ffmpeg (EBU R128) でこの音量にそろえ、パート間の音量差をなくします。以前に作ったパートは、次に変換するときにそろえられます。部分ごとの音声は残っているため、音声サーバーには問い合わせません。");
        zh(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "用 ffmpeg (EBU R128) 把每个章节合并后的音频调到这一响度，使各章节音量一致。之前制作的章节会在下次转换时调整；各段音频已保存，因此不会请求语音服务器。");
        fr(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "L'audio assemblé de chaque partie est mis à ce volume avec ffmpeg (EBU R128), pour que les parties soient au même niveau. Une partie fabriquée avant est nivelée la prochaine fois que vous la convertissez ; ses morceaux sont gardés, le serveur de synthèse vocale n'est donc pas sollicité.");
        es(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "El audio unido de cada parte se nivela a este volumen con ffmpeg (EBU R128), de modo que las partes quedan al mismo nivel. Una parte creada antes se nivela la próxima vez que la conviertas; sus fragmentos se conservan, así que no se consulta al servidor de voz.");
        de(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "Das zusammengefügte Audio jedes Abschnitts wird mit ffmpeg (EBU R128) auf diese Lautstärke angeglichen, damit die Abschnitte auf demselben Pegel liegen. Ein früher erstellter Abschnitt wird beim nächsten Umwandeln angeglichen; seine Stücke bleiben erhalten, der Sprachserver wird also nicht gefragt.");
        ko(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "각 파트의 합쳐진 오디오를 ffmpeg(EBU R128)로 이 음량에 맞춰 평준화하므로 파트들이 같은 수준이 됩니다. 이전에 만든 파트는 다음에 변환할 때 평준화되며, 조각은 유지되므로 음성 서버에 요청하지 않습니다.");
        it(THE_JOINED_AUDIO_OF_EACH_PART_IS_LEVELLED, "L'audio unito di ogni parte viene livellato a questo volume con ffmpeg (EBU R128), così le parti stanno allo stesso livello. Una parte creata prima viene livellata la prossima volta che la converti; i suoi pezzi vengono conservati, quindi il server vocale non viene interrogato.");

        ja(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg が見つかりません。音量はそろえられません。");
        zh(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "未找到 ffmpeg：不会统一音频响度。");
        fr(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg est introuvable : l'audio n'est pas nivelé.");
        es(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "No se encontró ffmpeg: el audio no se nivela.");
        de(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg wurde nicht gefunden: Das Audio wird nicht angeglichen.");
        ko(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg를 찾을 수 없습니다: 오디오가 평준화되지 않습니다.");
        it(FFMPEG_WAS_NOT_FOUND_THE_AUDIO_IS_NOT_LEVELLED, "ffmpeg non è stato trovato: l'audio non viene livellato.");

        ja(LABELLING_PROMPT, "ラベル付けのプロンプト");
        zh(LABELLING_PROMPT, "标注提示词");
        fr(LABELLING_PROMPT, "Prompt d'étiquetage");
        es(LABELLING_PROMPT, "Prompt de etiquetado");
        de(LABELLING_PROMPT, "Etikettierungsprompt");
        ko(LABELLING_PROMPT, "라벨 지정 프롬프트");
        it(LABELLING_PROMPT, "Prompt di etichettatura");

        ja(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "パートの各部分を誰が話し、どう読むかを言語モデルに伝える指示 (テンプレート)。空の場合は組み込みのものを使います。");
        zh(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "告诉语言模型章节的每一段由谁说、如何朗读的指令（模板）。留空：使用内置指令。");
        fr(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "L'instruction qui indique au modèle de langage qui dit chaque morceau d'une partie et comment il est lu (un modèle). Vide : celle intégrée.");
        es(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "La instrucción que le dice al modelo de lenguaje quién habla cada fragmento de una parte y cómo se lee (una plantilla). Vacío: la integrada.");
        de(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "Die Anweisung, die dem Sprachmodell sagt, wer jedes Stück eines Abschnitts spricht und wie es gelesen wird (eine Vorlage). Leer: die eingebaute.");
        ko(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "언어 모델에게 파트의 각 조각을 누가 말하는지와 어떻게 읽는지를 알려 주는 지시문 (템플릿). 비워 두면 내장된 것.");
        it(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE, "L'istruzione che dice al modello linguistico chi pronuncia ogni pezzo di una parte e come viene letto (un template). Vuoto: quello integrato.");

        ja(OTHER_SAMPLES, "他のサンプル: 効果音付き、感情付き");
        zh(OTHER_SAMPLES, "其他试听：带声音、带语气");
        fr(OTHER_SAMPLES, "Autres échantillons : avec des sons, dans une humeur");
        es(OTHER_SAMPLES, "Otras muestras: con sonidos, con un estado de ánimo");
        de(OTHER_SAMPLES, "Weitere Hörproben: mit Lauten, in einer Stimmung");
        ko(OTHER_SAMPLES, "다른 샘플: 소리 포함, 감정 포함");
        it(OTHER_SAMPLES, "Altri campioni: con suoni, in uno stato d'animo");

        ja(READ_PLAINLY, "普通に読む");
        zh(READ_PLAINLY, "平实朗读");
        fr(READ_PLAINLY, "Lecture neutre");
        es(READ_PLAINLY, "Leer sin más");
        de(READ_PLAINLY, "Neutral lesen");
        ko(READ_PLAINLY, "평이하게 읽기");
        it(READ_PLAINLY, "Lettura neutra");

        ja(WITH_SOUNDS, "効果音付き (ため息、笑い、息をのむ)");
        zh(WITH_SOUNDS, "带声音（叹气、笑、倒吸气）");
        fr(WITH_SOUNDS, "Avec des sons (soupir, rire, souffle coupé)");
        es(WITH_SOUNDS, "Con sonidos (suspiro, risa, jadeo)");
        de(WITH_SOUNDS, "Mit Lauten (Seufzen, Lachen, Keuchen)");
        ko(WITH_SOUNDS, "소리 포함 (한숨, 웃음, 헉)");
        it(WITH_SOUNDS, "Con suoni (sospiro, risata, sussulto)");

        ja(SPEECH_SERVERS_HELP, "オーディオブックを作る音声サーバーです。言語モデルと共有する GPU 上のサーバーには、それをロードする GPU ロックを指定します。");
        zh(SPEECH_SERVERS_HELP, "制作有声书的语音服务器。与语言模型共用 GPU 的服务器要指定加载它的 GPU 锁。");
        fr(SPEECH_SERVERS_HELP, "Les serveurs de synthèse vocale qui font le livre audio. Un serveur sur un GPU partagé avec le modèle de langage indique le verrou GPU qui le charge.");
        es(SPEECH_SERVERS_HELP, "Los servidores de voz que hacen el audiolibro. Un servidor en una GPU compartida con el modelo de lenguaje indica el bloqueo de GPU que lo carga.");
        de(SPEECH_SERVERS_HELP, "Die Sprachserver, die das Hörbuch machen. Ein Server auf einer GPU, die er mit dem Sprachmodell teilt, nennt die GPU-Sperre, die ihn lädt.");
        ko(SPEECH_SERVERS_HELP, "오디오북을 만드는 음성 서버입니다. 언어 모델과 GPU를 공유하는 서버는 그것을 로드하는 GPU 잠금을 지정합니다.");
        it(SPEECH_SERVERS_HELP, "I server vocali che fanno l'audiolibro. Un server su una GPU condivisa con il modello linguistico indica il blocco GPU che lo carica.");

        ja(REQUESTS_MADE_AROUND_EACH_BATCH, "各パートのバッチ (と各サンプル) の前後に、GPU ロックの中で順番に行うリクエスト。GPU を空けて音声サーバーをロードするのは GPU ロックの役目です。それ以外の用途に使います。なければ何もしません。");
        zh(REQUESTS_MADE_AROUND_EACH_BATCH, "在每批部分（以及每个试听）前后、在 GPU 锁内按顺序发出的请求。腾出 GPU 并加载语音服务器由 GPU 锁负责；这些请求用于其他用途。没有则不执行任何操作。");
        fr(REQUESTS_MADE_AROUND_EACH_BATCH, "Requêtes faites avant et après chaque lot de parties (et chaque échantillon), l'une après l'autre, dans le verrou GPU. Libérer le GPU et charger le serveur de voix est le rôle du verrou GPU ; utilisez-les pour le reste. Aucune : ne fait rien.");
        es(REQUESTS_MADE_AROUND_EACH_BATCH, "Solicitudes que se hacen antes y después de cada lote de partes (y de cada muestra), una tras otra, dentro del bloqueo de GPU. Liberar la GPU y cargar el servidor de voz es tarea del bloqueo de GPU; úsalas para lo demás. Ninguna no hace nada.");
        de(REQUESTS_MADE_AROUND_EACH_BATCH, "Anfragen vor und nach jedem Stapel von Teilen (und jeder Hörprobe), nacheinander, innerhalb der GPU-Sperre. Die GPU freizugeben und den Sprachserver zu laden ist Aufgabe der GPU-Sperre; diese Anfragen sind für alles andere. „Keine“ tut nichts.");
        ko(REQUESTS_MADE_AROUND_EACH_BATCH, "각 파트 배치(와 각 샘플) 전후에 GPU 잠금 안에서 차례로 실행하는 요청. GPU를 비우고 음성 서버를 로드하는 것은 GPU 잠금이 맡습니다. 그 밖의 용도에 쓰세요. 없음은 아무것도 하지 않습니다.");
        it(REQUESTS_MADE_AROUND_EACH_BATCH, "Richieste fatte prima e dopo ogni batch di parti (e ogni campione), una dopo l'altra, dentro il blocco GPU. Liberare la GPU e caricare il server vocale è compito del blocco GPU; usale per il resto. Nessuna non fa nulla.");

        ja(GPU_LOCK_HELP, "このサーバーの GPU 用に管理者が設定した GPU ロックです。各バッチとサンプルは GPU の順番を待ち、ロックのステップが先にサーバーをロードします (必要なら言語モデルや他のサーバーを解放します)。空: ロックなし。");
        zh(GPU_LOCK_HELP, "管理员为此服务器所在 GPU 设置的 GPU 锁：每批任务和每个试听都会等待轮到自己使用 GPU，锁的步骤会先加载服务器（需要时释放语言模型或其他服务器）。留空：无锁。");
        fr(GPU_LOCK_HELP, "Le verrou GPU que l'administrateur a configuré pour le GPU de ce serveur : chaque lot et chaque échantillon attend son tour sur le GPU, et les étapes du verrou chargent d'abord le serveur (en libérant le modèle de langage ou un autre serveur si besoin). Vide : aucun verrou.");
        es(GPU_LOCK_HELP, "El bloqueo de GPU que el administrador configuró para la GPU de este servidor: cada lote y cada muestra esperan su turno en la GPU y los pasos del bloqueo cargan primero el servidor (liberando el modelo de lenguaje u otro servidor si hace falta). Vacío: sin bloqueo.");
        de(GPU_LOCK_HELP, "Die GPU-Sperre, die der Administrator für die GPU dieses Servers eingerichtet hat: Jeder Stapel und jede Hörprobe wartet, bis sie an der Reihe ist, und die Schritte der Sperre laden zuerst den Server (und geben bei Bedarf das Sprachmodell oder einen anderen Server frei). Leer: keine Sperre.");
        ko(GPU_LOCK_HELP, "관리자가 이 서버의 GPU에 설정한 GPU 잠금입니다. 각 배치와 샘플은 GPU 차례를 기다리고, 잠금의 단계가 먼저 서버를 로드합니다(필요하면 언어 모델이나 다른 서버를 해제합니다). 비워 두면 잠금 없음.");
        it(GPU_LOCK_HELP, "Il blocco GPU che l'amministratore ha configurato per la GPU di questo server: ogni batch e ogni campione attende il suo turno sulla GPU e i passi del blocco caricano prima il server (liberando il modello linguistico o un altro server se serve). Vuoto: nessun blocco.");

        ja(PRECISION, "精度");
        zh(PRECISION, "精度");
        fr(PRECISION, "Précision");
        es(PRECISION, "Precisión");
        de(PRECISION, "Genauigkeit");
        ko(PRECISION, "정밀도");
        it(PRECISION, "Precisione");

        ja(PRECISION_HELP, "Higgs が計算に使う精度です。fp32 が安全です。AMD カードでは bf16 で意味不明な音声や別の声になりました。bf16 はメモリが約半分で済みます。");
        zh(PRECISION_HELP, "Higgs 计算时使用的精度。fp32 最稳妥：在一块 AMD 显卡上，bf16 产生了乱语和错误的声音。bf16 只需约一半的显存。");
        fr(PRECISION_HELP, "La précision de calcul de Higgs. fp32 est le choix sûr : sur une carte AMD, bf16 a donné du charabia et de mauvaises voix. bf16 demande environ moitié moins de mémoire.");
        es(PRECISION_HELP, "La precisión con la que calcula Higgs. fp32 es la opción segura: en una tarjeta AMD, bf16 dio galimatías y voces equivocadas. bf16 necesita aproximadamente la mitad de memoria.");
        de(PRECISION_HELP, "Die Genauigkeit, mit der Higgs rechnet. fp32 ist die sichere Wahl: Auf einer AMD-Karte ergab bf16 Kauderwelsch und falsche Stimmen. bf16 braucht etwa halb so viel Speicher.");
        ko(PRECISION_HELP, "Higgs가 계산하는 정밀도입니다. fp32가 안전합니다. AMD 카드에서 bf16은 알아들을 수 없는 소리와 다른 목소리를 냈습니다. bf16은 메모리를 약 절반만 씁니다.");
        it(PRECISION_HELP, "La precisione con cui calcola Higgs. fp32 è la scelta sicura: su una scheda AMD bf16 ha dato parole senza senso e voci sbagliate. bf16 richiede circa metà della memoria.");

        ja(CHECK_THE_LENGTH_OF_PIECES, "ピースの長さを確認する");
        zh(CHECK_THE_LENGTH_OF_PIECES, "检查片段长度");
        fr(CHECK_THE_LENGTH_OF_PIECES, "Vérifier la durée des morceaux");
        es(CHECK_THE_LENGTH_OF_PIECES, "Comprobar la duración de los fragmentos");
        de(CHECK_THE_LENGTH_OF_PIECES, "Länge der Stücke prüfen");
        ko(CHECK_THE_LENGTH_OF_PIECES, "조각의 길이 확인");
        it(CHECK_THE_LENGTH_OF_PIECES, "Controlla la durata dei pezzi");

        ja(CHECK_THE_LENGTH_HELP, "テキストよりずっと長いピース (モデルが話し続けた、または言葉の代わりに音を出した) は、別のシードで作り直します。");
        zh(CHECK_THE_LENGTH_HELP, "比文本长得多的片段（模型一直说下去，或者发出了声音而不是说出文字）会用另一个种子重新生成。");
        fr(CHECK_THE_LENGTH_HELP, "Un morceau bien plus long que son texte (le modèle a continué à parler, ou a fait un bruit au lieu des mots) est refait avec une autre graine.");
        es(CHECK_THE_LENGTH_HELP, "Un fragmento mucho más largo que su texto (el modelo siguió hablando o hizo un sonido en lugar de las palabras) se vuelve a hacer con otra semilla.");
        de(CHECK_THE_LENGTH_HELP, "Ein Stück, das viel länger ist als sein Text (das Modell hat weitergeredet oder ein Geräusch statt der Wörter gemacht), wird mit einem anderen Seed neu gemacht.");
        ko(CHECK_THE_LENGTH_HELP, "텍스트보다 훨씬 긴 조각(모델이 계속 말했거나 단어 대신 소리를 냄)은 다른 시드로 다시 만듭니다.");
        it(CHECK_THE_LENGTH_HELP, "Un pezzo molto più lungo del suo testo (il modello ha continuato a parlare o ha fatto un suono al posto delle parole) viene rifatto con un altro seed.");

        ja(CHECK_THE_WORDS, "音声認識で言葉を確認する");
        zh(CHECK_THE_WORDS, "用语音识别检查文字");
        fr(CHECK_THE_WORDS, "Vérifier les mots par reconnaissance vocale");
        es(CHECK_THE_WORDS, "Comprobar las palabras con reconocimiento de voz");
        de(CHECK_THE_WORDS, "Wörter per Spracherkennung prüfen");
        ko(CHECK_THE_WORDS, "음성 인식으로 단어 확인");
        it(CHECK_THE_WORDS, "Controlla le parole con il riconoscimento vocale");

        ja(CHECK_THE_WORDS_HELP, "各バッチの後、すべてのピースを文字起こししてテキストと比べ、違うものは別のシードで作り直します。設定の音声認識が必要です。");
        zh(CHECK_THE_WORDS_HELP, "每批完成后，每个片段都会被转写并与其文本比较；不一致的片段会用另一个种子重新生成。需要设置中的语音识别。");
        fr(CHECK_THE_WORDS_HELP, "Après chaque lot, chaque morceau est transcrit et comparé à son texte ; ceux qui diffèrent sont refaits avec une autre graine. Nécessite la reconnaissance vocale des réglages.");
        es(CHECK_THE_WORDS_HELP, "Tras cada lote, cada fragmento se transcribe y se compara con su texto; los que difieren se vuelven a hacer con otra semilla. Necesita el reconocimiento de voz de los ajustes.");
        de(CHECK_THE_WORDS_HELP, "Nach jedem Stapel wird jedes Stück transkribiert und mit seinem Text verglichen; abweichende Stücke werden mit einem anderen Seed neu gemacht. Braucht die Spracherkennung der Einstellungen.");
        ko(CHECK_THE_WORDS_HELP, "각 배치 후 모든 조각을 받아쓰고 텍스트와 비교하며, 다른 조각은 다른 시드로 다시 만듭니다. 설정의 음성 인식이 필요합니다.");
        it(CHECK_THE_WORDS_HELP, "Dopo ogni batch ogni pezzo viene trascritto e confrontato con il suo testo; i pezzi diversi vengono rifatti con un altro seed. Richiede il riconoscimento vocale delle impostazioni.");

        ja(MAX_WORD_ERRORS, "最大単語誤り率 (%)");
        zh(MAX_WORD_ERRORS, "最大词错误率（%）");
        fr(MAX_WORD_ERRORS, "Erreurs de mots max. (%)");
        es(MAX_WORD_ERRORS, "Errores de palabras máx. (%)");
        de(MAX_WORD_ERRORS, "Max. Wortfehler (%)");
        ko(MAX_WORD_ERRORS, "최대 단어 오류율(%)");
        it(MAX_WORD_ERRORS, "Errori di parola max. (%)");

        ja(RETRIES, "再試行回数");
        zh(RETRIES, "重试次数");
        fr(RETRIES, "Nouvelles tentatives");
        es(RETRIES, "Reintentos");
        de(RETRIES, "Wiederholungen");
        ko(RETRIES, "재시도 횟수");
        it(RETRIES, "Tentativi");

        ja(SEND_THE_CLIP_TEXT, "クリップのテキストを Higgs に送る");
        zh(SEND_THE_CLIP_TEXT, "将片段文本发送给 Higgs");
        fr(SEND_THE_CLIP_TEXT, "Envoyer le texte de l'extrait à Higgs");
        es(SEND_THE_CLIP_TEXT, "Enviar el texto del clip a Higgs");
        de(SEND_THE_CLIP_TEXT, "Text des Clips an Higgs senden");
        ko(SEND_THE_CLIP_TEXT, "클립의 텍스트를 Higgs에 보내기");
        it(SEND_THE_CLIP_TEXT, "Invia il testo della clip a Higgs");

        ja(SEND_THE_CLIP_TEXT_HELP, "試験的、オフのままにしてください: 声のクリップで話されている言葉を Higgs に一緒に渡します。テストでは声が変わりました。");
        zh(SEND_THE_CLIP_TEXT_HELP, "实验性，建议保持关闭：Higgs 会同时得到声音片段中所说的文字。在测试中它改变了声音。");
        fr(SEND_THE_CLIP_TEXT_HELP, "Expérimental, à laisser désactivé : Higgs reçoit avec l'extrait de voix les mots qui y sont dits. Lors des tests, cela a changé la voix.");
        es(SEND_THE_CLIP_TEXT_HELP, "Experimental, mejor desactivado: Higgs recibe junto con el clip de voz las palabras que se dicen en él. En las pruebas cambió la voz.");
        de(SEND_THE_CLIP_TEXT_HELP, "Experimentell, besser aus lassen: Higgs bekommt mit dem Stimmclip die darin gesprochenen Wörter. In Tests veränderte das die Stimme.");
        ko(SEND_THE_CLIP_TEXT_HELP, "실험적, 꺼 두세요: Higgs가 목소리 클립과 함께 그 안에서 말한 단어를 받습니다. 테스트에서는 목소리가 바뀌었습니다.");
        it(SEND_THE_CLIP_TEXT_HELP, "Sperimentale, meglio lasciarlo spento: Higgs riceve con la clip di voce le parole dette al suo interno. Nei test ha cambiato la voce.");

        ja(SPEECH_RECOGNITION, "音声認識");
        zh(SPEECH_RECOGNITION, "语音识别");
        fr(SPEECH_RECOGNITION, "Reconnaissance vocale");
        es(SPEECH_RECOGNITION, "Reconocimiento de voz");
        de(SPEECH_RECOGNITION, "Spracherkennung");
        ko(SPEECH_RECOGNITION, "음성 인식");
        it(SPEECH_RECOGNITION, "Riconoscimento vocale");

        ja(SPEECH_RECOGNITION_HELP, "音声が何を言っているかを書き起こします。バッチのピースの確認と、声のクリップのテキストの認識に使います。TTS Audio Suite 入りの ComfyUI (Granite ASR)、または whisper サーバーなどの OpenAI 互換の文字起こしサーバー。");
        zh(SPEECH_RECOGNITION_HELP, "写下一段语音说了什么：用于检查一批中的片段，以及识别声音片段的文本。可以是带 TTS Audio Suite 的 ComfyUI（Granite ASR），或 whisper 服务器等兼容 OpenAI 的转写服务器。");
        fr(SPEECH_RECOGNITION_HELP, "Écrit ce que dit un morceau de parole : vérifie les morceaux d'un lot et reconnaît le texte d'un extrait de voix. ComfyUI avec TTS Audio Suite (Granite ASR), ou un serveur de transcription compatible OpenAI comme un serveur whisper.");
        es(SPEECH_RECOGNITION_HELP, "Escribe lo que dice un fragmento de voz: comprueba los fragmentos de un lote y reconoce el texto de un clip de voz. ComfyUI con TTS Audio Suite (Granite ASR) o un servidor de transcripción compatible con OpenAI, como un servidor whisper.");
        de(SPEECH_RECOGNITION_HELP, "Schreibt auf, was ein Stück Sprache sagt: prüft die Stücke eines Stapels und erkennt den Text eines Stimmclips. ComfyUI mit der TTS Audio Suite (Granite ASR) oder ein OpenAI-kompatibler Transkriptionsserver wie ein whisper-Server.");
        ko(SPEECH_RECOGNITION_HELP, "음성이 무엇을 말하는지 받아씁니다. 배치의 조각을 확인하고 목소리 클립의 텍스트를 인식합니다. TTS Audio Suite가 있는 ComfyUI(Granite ASR) 또는 whisper 서버 같은 OpenAI 호환 받아쓰기 서버.");
        it(SPEECH_RECOGNITION_HELP, "Scrive ciò che dice un pezzo di parlato: controlla i pezzi di un batch e riconosce il testo di una clip di voce. ComfyUI con TTS Audio Suite (Granite ASR) o un server di trascrizione compatibile con OpenAI, come un server whisper.");

        ja(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");
        zh(RECOGNIZER_COMFYUI, "ComfyUI（Granite ASR）");
        fr(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");
        es(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");
        de(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");
        ko(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");
        it(RECOGNIZER_COMFYUI, "ComfyUI (Granite ASR)");

        ja(RECOGNIZER_OPENAI, "OpenAI 互換 (whisper)");
        zh(RECOGNIZER_OPENAI, "兼容 OpenAI（whisper）");
        fr(RECOGNIZER_OPENAI, "Compatible OpenAI (whisper)");
        es(RECOGNIZER_OPENAI, "Compatible con OpenAI (whisper)");
        de(RECOGNIZER_OPENAI, "OpenAI-kompatibel (whisper)");
        ko(RECOGNIZER_OPENAI, "OpenAI 호환(whisper)");
        it(RECOGNIZER_OPENAI, "Compatibile OpenAI (whisper)");

        ja(RECOGNITION_MODEL, "認識のモデル");
        zh(RECOGNITION_MODEL, "识别模型");
        fr(RECOGNITION_MODEL, "Modèle de reconnaissance");
        es(RECOGNITION_MODEL, "Modelo de reconocimiento");
        de(RECOGNITION_MODEL, "Modell der Erkennung");
        ko(RECOGNITION_MODEL, "인식 모델");
        it(RECOGNITION_MODEL, "Modello del riconoscimento");

        ja(TEXT_OF_THE_CLIP, "クリップのテキスト");
        zh(TEXT_OF_THE_CLIP, "片段文本");
        fr(TEXT_OF_THE_CLIP, "Texte de l'extrait");
        es(TEXT_OF_THE_CLIP, "Texto del clip");
        de(TEXT_OF_THE_CLIP, "Text des Clips");
        ko(TEXT_OF_THE_CLIP, "클립의 텍스트");
        it(TEXT_OF_THE_CLIP, "Testo della clip");

        ja(TEXT_OF_THE_CLIP_HELP, "クリップで話されている言葉。入力するか認識させます。");
        zh(TEXT_OF_THE_CLIP_HELP, "片段中所说的话，可手动输入或自动识别。");
        fr(TEXT_OF_THE_CLIP_HELP, "Les mots dits dans l'extrait, saisis ou reconnus.");
        es(TEXT_OF_THE_CLIP_HELP, "Las palabras dichas en el clip, escritas o reconocidas.");
        de(TEXT_OF_THE_CLIP_HELP, "Die im Clip gesprochenen Wörter, getippt oder erkannt.");
        ko(TEXT_OF_THE_CLIP_HELP, "클립에서 말한 단어로, 입력하거나 인식시킵니다.");
        it(TEXT_OF_THE_CLIP_HELP, "Le parole dette nella clip, digitate o riconosciute.");

        ja(RECOGNIZE, "認識");
        zh(RECOGNIZE, "识别");
        fr(RECOGNIZE, "Reconnaître");
        es(RECOGNIZE, "Reconocer");
        de(RECOGNIZE, "Erkennen");
        ko(RECOGNIZE, "인식");
        it(RECOGNIZE, "Riconosci");

        ja(RECOGNIZING, "認識中 ...");
        zh(RECOGNIZING, "识别中 ...");
        fr(RECOGNIZING, "Reconnaissance ...");
        es(RECOGNIZING, "Reconociendo ...");
        de(RECOGNIZING, "Wird erkannt ...");
        ko(RECOGNIZING, "인식 중 ...");
        it(RECOGNIZING, "Riconoscimento ...");

        ja(NO_SPEECH_RECOGNITION, "先にオーディオブックの設定で音声認識を設定してください。");
        zh(NO_SPEECH_RECOGNITION, "请先在有声书设置中设置语音识别。");
        fr(NO_SPEECH_RECOGNITION, "Configurez d'abord la reconnaissance vocale dans les réglages du livre audio.");
        es(NO_SPEECH_RECOGNITION, "Configura primero el reconocimiento de voz en los ajustes del audiolibro.");
        de(NO_SPEECH_RECOGNITION, "Richte zuerst die Spracherkennung in den Hörbuch-Einstellungen ein.");
        ko(NO_SPEECH_RECOGNITION, "먼저 오디오북 설정에서 음성 인식을 설정하세요.");
        it(NO_SPEECH_RECOGNITION, "Configura prima il riconoscimento vocale nelle impostazioni dell'audiolibro.");

        ja(THE_SPEECH_RECOGNITION_WORKS, "音声認識は動作しています。");
        zh(THE_SPEECH_RECOGNITION_WORKS, "语音识别可以使用。");
        fr(THE_SPEECH_RECOGNITION_WORKS, "La reconnaissance vocale fonctionne.");
        es(THE_SPEECH_RECOGNITION_WORKS, "El reconocimiento de voz funciona.");
        de(THE_SPEECH_RECOGNITION_WORKS, "Die Spracherkennung funktioniert.");
        ko(THE_SPEECH_RECOGNITION_WORKS, "음성 인식이 작동합니다.");
        it(THE_SPEECH_RECOGNITION_WORKS, "Il riconoscimento vocale funziona.");

        ja(RECOGNITION_FAILED, "認識に失敗しました: %s");
        zh(RECOGNITION_FAILED, "识别失败：%s");
        fr(RECOGNITION_FAILED, "La reconnaissance a échoué : %s");
        es(RECOGNITION_FAILED, "El reconocimiento falló: %s");
        de(RECOGNITION_FAILED, "Die Erkennung ist fehlgeschlagen: %s");
        ko(RECOGNITION_FAILED, "인식에 실패했습니다: %s");
        it(RECOGNITION_FAILED, "Riconoscimento non riuscito: %s");

        ja(THE_TEXT_OF_THE_CLIP_IS_SAVED, "クリップのテキストを保存しました。");
        zh(THE_TEXT_OF_THE_CLIP_IS_SAVED, "片段文本已保存。");
        fr(THE_TEXT_OF_THE_CLIP_IS_SAVED, "Le texte de l'extrait est enregistré.");
        es(THE_TEXT_OF_THE_CLIP_IS_SAVED, "El texto del clip se ha guardado.");
        de(THE_TEXT_OF_THE_CLIP_IS_SAVED, "Der Text des Clips ist gespeichert.");
        ko(THE_TEXT_OF_THE_CLIP_IS_SAVED, "클립의 텍스트를 저장했습니다.");
        it(THE_TEXT_OF_THE_CLIP_IS_SAVED, "Il testo della clip è salvato.");

        ja(CLIP, "クリップ");
        zh(CLIP, "片段");
        fr(CLIP, "Extrait");
        es(CLIP, "Clip");
        de(CLIP, "Clip");
        ko(CLIP, "클립");
        it(CLIP, "Clip");

        ja(PICK_A_CLIP, "「クリップ」でクリップを選んでください。");
        zh(PICK_A_CLIP, "请在“片段”中选择一个片段。");
        fr(PICK_A_CLIP, "Choisissez un extrait dans Extraits.");
        es(PICK_A_CLIP, "Elige un clip en Clips.");
        de(PICK_A_CLIP, "Wähle einen Clip unter Clips.");
        ko(PICK_A_CLIP, "클립에서 클립을 선택하세요.");
        it(PICK_A_CLIP, "Scegli una clip in Clip.");

        ja(FOLDER, "フォルダー");
        zh(FOLDER, "文件夹");
        fr(FOLDER, "Dossier");
        es(FOLDER, "Carpeta");
        de(FOLDER, "Ordner");
        ko(FOLDER, "폴더");
        it(FOLDER, "Cartella");

        ja(NO_FOLDER, "フォルダーなし");
        zh(NO_FOLDER, "无文件夹");
        fr(NO_FOLDER, "Sans dossier");
        es(NO_FOLDER, "Sin carpeta");
        de(NO_FOLDER, "Kein Ordner");
        ko(NO_FOLDER, "폴더 없음");
        it(NO_FOLDER, "Nessuna cartella");

        ja(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "バージョンは元のクリップと同じフォルダーに入ります。");
        zh(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "版本位于其来源片段的文件夹中。");
        fr(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "Une version est dans le dossier de l'extrait dont elle est issue.");
        es(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "Una versión está en la carpeta del clip del que se hizo.");
        de(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "Eine Version liegt im Ordner des Clips, aus dem sie gemacht wurde.");
        ko(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "버전은 원본 클립과 같은 폴더에 있습니다.");
        it(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL, "Una versione sta nella cartella della clip da cui è stata ricavata.");

        ja(MADE_FROM, "%s から %s で作成");
        zh(MADE_FROM, "由 %s 经 %s 生成");
        fr(MADE_FROM, "Faite à partir de %s avec %s");
        es(MADE_FROM, "Hecha a partir de %s con %s");
        de(MADE_FROM, "Aus %s gemacht mit %s");
        ko(MADE_FROM, "%s에서 %s(으)로 만듦");
        it(MADE_FROM, "Ricavata da %s con %s");

        ja(NOT_USED, "未使用");
        zh(NOT_USED, "未使用");
        fr(NOT_USED, "Non utilisé");
        es(NOT_USED, "Sin usar");
        de(NOT_USED, "Nicht verwendet");
        ko(NOT_USED, "사용 안 함");
        it(NOT_USED, "Non usata");

        ja(USE_THIS_VERSION, "このバージョンを使う");
        zh(USE_THIS_VERSION, "使用此版本");
        fr(USE_THIS_VERSION, "Utiliser cette version");
        es(USE_THIS_VERSION, "Usar esta versión");
        de(USE_THIS_VERSION, "Diese Version verwenden");
        ko(USE_THIS_VERSION, "이 버전 사용");
        it(USE_THIS_VERSION, "Usa questa versione");

        ja(USE_THIS_VERSION_HELP, "元のクリップが使われているすべての場所にこのバージョンを入れます");
        zh(USE_THIS_VERSION_HELP, "在来源片段被使用的所有地方换成此版本");
        fr(USE_THIS_VERSION_HELP, "Met cette version partout où l'extrait d'origine est utilisé");
        es(USE_THIS_VERSION_HELP, "Pone esta versión en todos los sitios donde se usa el clip original");
        de(USE_THIS_VERSION_HELP, "Setzt diese Version überall ein, wo der ursprüngliche Clip verwendet wird");
        ko(USE_THIS_VERSION_HELP, "원본 클립이 쓰이는 모든 곳에 이 버전을 넣습니다");
        it(USE_THIS_VERSION_HELP, "Mette questa versione ovunque sia usata la clip di origine");

        ja(USE_THIS_VERSION_CONFIRM, "%s を、%s が使われているすべての場所で使いますか? 古いクリップで作ったセリフは作り直されます。");
        zh(USE_THIS_VERSION_CONFIRM, "将 %s 用于所有使用 %s 的地方？用旧片段生成的台词会重新生成。");
        fr(USE_THIS_VERSION_CONFIRM, "Utiliser %s partout où %s est utilisé ? Les répliques faites avec l'ancien extrait seront refaites.");
        es(USE_THIS_VERSION_CONFIRM, "¿Usar %s en todos los sitios donde se usa %s? Las frases hechas con el clip anterior se volverán a hacer.");
        de(USE_THIS_VERSION_CONFIRM, "%s überall verwenden, wo %s verwendet wird? Die mit dem alten Clip gemachten Sätze werden neu gemacht.");
        ko(USE_THIS_VERSION_CONFIRM, "%s을(를) %s이(가) 쓰이는 모든 곳에 사용할까요? 이전 클립으로 만든 대사는 다시 만들어집니다.");
        it(USE_THIS_VERSION_CONFIRM, "Usare %s ovunque sia usata %s? Le battute fatte con la vecchia clip verranno rifatte.");

        ja(CLEAN_WITH_AI, "AI で整える");
        zh(CLEAN_WITH_AI, "用 AI 清理");
        fr(CLEAN_WITH_AI, "Nettoyer par IA");
        es(CLEAN_WITH_AI, "Limpiar con IA");
        de(CLEAN_WITH_AI, "Mit KI säubern");
        ko(CLEAN_WITH_AI, "AI로 정리");
        it(CLEAN_WITH_AI, "Pulisci con l'IA");

        ja(CLEAN_WITH_AI_HELP, "選んだクリーンアップを ComfyUI で実行し、クリップの新しいバージョンを作ります (分離モデル: 音楽・ノイズ・残響から声を取り出す)");
        zh(CLEAN_WITH_AI_HELP, "在 ComfyUI 上用所选清理方案生成片段的新版本（分离模型：从音乐、噪声、回声中提取人声）");
        fr(CLEAN_WITH_AI_HELP, "Crée une nouvelle version de l'extrait avec le nettoyage choisi sur ComfyUI (modèles de séparation : la voix hors de la musique, du bruit, de l'écho)");
        es(CLEAN_WITH_AI_HELP, "Crea una nueva versión del clip con la limpieza elegida en ComfyUI (modelos de separación: la voz sin música, ruido ni eco)");
        de(CLEAN_WITH_AI_HELP, "Macht mit der gewählten Bereinigung auf ComfyUI eine neue Version des Clips (Trennmodelle: Stimme aus Musik, Rauschen, Hall)");
        ko(CLEAN_WITH_AI_HELP, "선택한 정리 방식으로 ComfyUI에서 클립의 새 버전을 만듭니다(분리 모델: 음악·잡음·울림에서 목소리 추출)");
        it(CLEAN_WITH_AI_HELP, "Crea una nuova versione della clip con la pulizia scelta su ComfyUI (modelli di separazione: la voce da musica, rumore, eco)");

        ja(CLEANING, "整えています…");
        zh(CLEANING, "正在清理…");
        fr(CLEANING, "Nettoyage…");
        es(CLEANING, "Limpiando…");
        de(CLEANING, "Wird gesäubert…");
        ko(CLEANING, "정리 중…");
        it(CLEANING, "Pulizia in corso…");

        ja(NO_CLEANUP, "クリップのクリーンアップが設定されていません (オーディオブックの設定)。");
        zh(NO_CLEANUP, "尚未设置片段清理（有声书设置）。");
        fr(NO_CLEANUP, "Aucun nettoyage d'extraits n'est configuré (réglages du livre audio).");
        es(NO_CLEANUP, "No hay ninguna limpieza de clips configurada (ajustes del audiolibro).");
        de(NO_CLEANUP, "Keine Bereinigung von Clips eingerichtet (Hörbuch-Einstellungen).");
        ko(NO_CLEANUP, "클립 정리가 설정되지 않았습니다(오디오북 설정).");
        it(NO_CLEANUP, "Nessuna pulizia delle clip è configurata (impostazioni dell'audiolibro).");

        ja(CLEANUP_OF_CLIPS, "クリップのクリーンアップ");
        zh(CLEANUP_OF_CLIPS, "片段清理");
        fr(CLEANUP_OF_CLIPS, "Nettoyage des extraits");
        es(CLEANUP_OF_CLIPS, "Limpieza de clips");
        de(CLEANUP_OF_CLIPS, "Bereinigung der Clips");
        ko(CLEANUP_OF_CLIPS, "클립 정리");
        it(CLEANUP_OF_CLIPS, "Pulizia delle clip");

        ja(CLEANUP_OF_CLIPS_HELP, "TTS Audio Suite を入れた ComfyUI が参照クリップを整えます。プリセットは手順を順に実行します (分離モデルは声を残して音楽・ノイズ・残響を除き、ボイスフィクサーは劣化した声を復元し、ffmpeg は一定のヒスノイズを下げます)。結果はクリップの新しいバージョンになり、元のクリップは残ります。モデルは初回使用時に ComfyUI がダウンロードします。");
        zh(CLEANUP_OF_CLIPS_HELP, "装有 TTS Audio Suite 的 ComfyUI 可清理参考片段：方案按顺序执行各步骤（分离模型保留人声，去除音乐、噪声或回声；VoiceFixer 修复受损人声；ffmpeg 降低持续的嘶声）。结果是片段的新版本，原片段保留。模型在首次使用时由 ComfyUI 下载。");
        fr(CLEANUP_OF_CLIPS_HELP, "Un ComfyUI avec la TTS Audio Suite nettoie les extraits de référence : un préréglage exécute ses étapes l'une après l'autre (un modèle de séparation garde la voix et retire la musique, le bruit ou l'écho ; le voice fixer restaure une voix dégradée ; ffmpeg atténue un souffle constant). Le résultat est une nouvelle version de l'extrait, l'extrait lui-même reste. ComfyUI télécharge les modèles à la première utilisation.");
        es(CLEANUP_OF_CLIPS_HELP, "Un ComfyUI con TTS Audio Suite limpia los clips de referencia: un preajuste ejecuta sus pasos uno tras otro (un modelo de separación conserva la voz y quita música, ruido o eco; el voice fixer restaura una voz degradada; ffmpeg reduce un siseo constante). El resultado es una nueva versión del clip; el clip original se queda. ComfyUI descarga los modelos la primera vez que se usan.");
        de(CLEANUP_OF_CLIPS_HELP, "Ein ComfyUI mit der TTS Audio Suite säubert Referenzclips: Eine Vorlage führt ihre Schritte nacheinander aus (ein Trennmodell behält die Stimme und entfernt Musik, Rauschen oder Hall; der Voice Fixer stellt eine beschädigte Stimme wieder her; ffmpeg senkt gleichmäßiges Rauschen). Das Ergebnis ist eine neue Version des Clips, der Clip selbst bleibt. ComfyUI lädt die Modelle bei der ersten Verwendung herunter.");
        ko(CLEANUP_OF_CLIPS_HELP, "TTS Audio Suite가 설치된 ComfyUI가 참조 클립을 정리합니다. 프리셋은 단계를 차례로 실행합니다(분리 모델은 목소리를 남기고 음악·잡음·울림을 없애고, 보이스 픽서는 손상된 목소리를 복원하며, ffmpeg는 일정한 잡음을 줄입니다). 결과는 클립의 새 버전이며 원래 클립은 남습니다. 모델은 처음 사용할 때 ComfyUI가 내려받습니다.");
        it(CLEANUP_OF_CLIPS_HELP, "Un ComfyUI con la TTS Audio Suite pulisce le clip di riferimento: un preset esegue i suoi passi uno dopo l'altro (un modello di separazione tiene la voce e toglie musica, rumore o eco; il voice fixer ripristina una voce degradata; ffmpeg attenua un fruscio costante). Il risultato è una nuova versione della clip, la clip stessa resta. ComfyUI scarica i modelli al primo uso.");

        ja(CLEANUP_GPU_LOCK_HELP, "クリーンアップには音声や音声認識とは別のロックを割り当ててください。そのステップで ComfyUI を再起動し、モデルが同時に読み込まれないようにします。");
        zh(CLEANUP_GPU_LOCK_HELP, "请为清理设置单独的锁，不要与语音或语音识别共用：其步骤应重启 ComfyUI，避免模型同时加载。");
        fr(CLEANUP_GPU_LOCK_HELP, "Donnez au nettoyage un verrou à lui, pas celui de la parole ni de la reconnaissance vocale : ses étapes doivent redémarrer ComfyUI, pour que ses modèles ne soient pas chargés à côté des leurs.");
        es(CLEANUP_GPU_LOCK_HELP, "Dale a la limpieza un bloqueo propio, no el de la voz ni el del reconocimiento de voz: sus pasos deben reiniciar ComfyUI, para que sus modelos no se carguen junto a los de ellos.");
        de(CLEANUP_GPU_LOCK_HELP, "Gib der Bereinigung eine eigene Sperre, nicht die der Sprache oder der Spracherkennung: Ihre Schritte sollten ComfyUI neu starten, damit ihre Modelle nicht neben deren geladen sind.");
        ko(CLEANUP_GPU_LOCK_HELP, "정리에는 음성이나 음성 인식과 다른 별도의 잠금을 지정하세요. 그 단계에서 ComfyUI를 다시 시작해 모델이 함께 올라가지 않게 합니다.");
        it(CLEANUP_GPU_LOCK_HELP, "Dai alla pulizia un blocco suo, non quello della voce né del riconoscimento vocale: i suoi passi dovrebbero riavviare ComfyUI, così i suoi modelli non vengono caricati accanto ai loro.");

        ja(CLEANUP_PRESET, "クリーンアップ");
        zh(CLEANUP_PRESET, "清理方案");
        fr(CLEANUP_PRESET, "Nettoyage");
        es(CLEANUP_PRESET, "Limpieza");
        de(CLEANUP_PRESET, "Bereinigung");
        ko(CLEANUP_PRESET, "정리 방식");
        it(CLEANUP_PRESET, "Pulizia");

        ja(NEW_PRESET, "新しいクリーンアップ");
        zh(NEW_PRESET, "新建清理方案");
        fr(NEW_PRESET, "Nouveau nettoyage");
        es(NEW_PRESET, "Nueva limpieza");
        de(NEW_PRESET, "Neue Bereinigung");
        ko(NEW_PRESET, "새 정리 방식");
        it(NEW_PRESET, "Nuova pulizia");

        ja(DELETE_THE_PRESET, "クリーンアップを削除");
        zh(DELETE_THE_PRESET, "删除清理方案");
        fr(DELETE_THE_PRESET, "Supprimer le nettoyage");
        es(DELETE_THE_PRESET, "Eliminar la limpieza");
        de(DELETE_THE_PRESET, "Bereinigung löschen");
        ko(DELETE_THE_PRESET, "정리 방식 삭제");
        it(DELETE_THE_PRESET, "Elimina la pulizia");

        ja(DEFAULT_PRESETS, "既定のクリーンアップ");
        zh(DEFAULT_PRESETS, "默认清理方案");
        fr(DEFAULT_PRESETS, "Nettoyages par défaut");
        es(DEFAULT_PRESETS, "Limpiezas predeterminadas");
        de(DEFAULT_PRESETS, "Standard-Bereinigungen");
        ko(DEFAULT_PRESETS, "기본 정리 방식");
        it(DEFAULT_PRESETS, "Pulizie predefinite");

        ja(ADD_A_STEP, "ステップを追加");
        zh(ADD_A_STEP, "添加步骤");
        fr(ADD_A_STEP, "Ajouter une étape");
        es(ADD_A_STEP, "Añadir un paso");
        de(ADD_A_STEP, "Schritt hinzufügen");
        ko(ADD_A_STEP, "단계 추가");
        it(ADD_A_STEP, "Aggiungi un passo");

        ja(REMOVE_THE_STEP, "ステップを削除");
        zh(REMOVE_THE_STEP, "移除步骤");
        fr(REMOVE_THE_STEP, "Retirer l'étape");
        es(REMOVE_THE_STEP, "Quitar el paso");
        de(REMOVE_THE_STEP, "Schritt entfernen");
        ko(REMOVE_THE_STEP, "단계 제거");
        it(REMOVE_THE_STEP, "Rimuovi il passo");

        ja(STEP_SEPARATE, "分離モデル");
        zh(STEP_SEPARATE, "分离模型");
        fr(STEP_SEPARATE, "Modèle de séparation");
        es(STEP_SEPARATE, "Modelo de separación");
        de(STEP_SEPARATE, "Trennmodell");
        ko(STEP_SEPARATE, "분리 모델");
        it(STEP_SEPARATE, "Modello di separazione");

        ja(STEP_VOICE_FIXER, "ボイスフィクサー");
        zh(STEP_VOICE_FIXER, "VoiceFixer 修复");
        fr(STEP_VOICE_FIXER, "Voice fixer");
        es(STEP_VOICE_FIXER, "Voice fixer");
        de(STEP_VOICE_FIXER, "Voice Fixer");
        ko(STEP_VOICE_FIXER, "보이스 픽서");
        it(STEP_VOICE_FIXER, "Voice fixer");

        ja(STEP_FFMPEG, "ffmpeg (このサーバー)");
        zh(STEP_FFMPEG, "ffmpeg（本机）");
        fr(STEP_FFMPEG, "ffmpeg (ici)");
        es(STEP_FFMPEG, "ffmpeg (aquí)");
        de(STEP_FFMPEG, "ffmpeg (hier)");
        ko(STEP_FFMPEG, "ffmpeg(여기)");
        it(STEP_FFMPEG, "ffmpeg (qui)");

        ja(SEPARATION_MODEL, "モデル");
        zh(SEPARATION_MODEL, "模型");
        fr(SEPARATION_MODEL, "Modèle");
        es(SEPARATION_MODEL, "Modelo");
        de(SEPARATION_MODEL, "Modell");
        ko(SEPARATION_MODEL, "모델");
        it(SEPARATION_MODEL, "Modello");

        ja(KEEP_THE_EXTRACTED, "抽出側を残す");
        zh(KEEP_THE_EXTRACTED, "保留提取部分");
        fr(KEEP_THE_EXTRACTED, "Garder l'extrait");
        es(KEEP_THE_EXTRACTED, "Conservar lo extraído");
        de(KEEP_THE_EXTRACTED, "Extrahiertes behalten");
        ko(KEEP_THE_EXTRACTED, "추출된 쪽 유지");
        it(KEEP_THE_EXTRACTED, "Tieni l'estratto");

        ja(KEEP_THE_REMAINING, "残り側を残す");
        zh(KEEP_THE_REMAINING, "保留剩余部分");
        fr(KEEP_THE_REMAINING, "Garder le reste");
        es(KEEP_THE_REMAINING, "Conservar el resto");
        de(KEEP_THE_REMAINING, "Rest behalten");
        ko(KEEP_THE_REMAINING, "나머지 쪽 유지");
        it(KEEP_THE_REMAINING, "Tieni il resto");

        ja(AGGRESSIVENESS, "強さ (0–20)");
        zh(AGGRESSIVENESS, "强度（0–20）");
        fr(AGGRESSIVENESS, "Agressivité (0–20)");
        es(AGGRESSIVENESS, "Agresividad (0–20)");
        de(AGGRESSIVENESS, "Stärke (0–20)");
        ko(AGGRESSIVENESS, "강도(0–20)");
        it(AGGRESSIVENESS, "Aggressività (0–20)");

        ja(FIXER_MODE_ORIGINAL, "復元 (既定)");
        zh(FIXER_MODE_ORIGINAL, "修复（默认）");
        fr(FIXER_MODE_ORIGINAL, "Restaurer (par défaut)");
        es(FIXER_MODE_ORIGINAL, "Restaurar (predeterminado)");
        de(FIXER_MODE_ORIGINAL, "Wiederherstellen (Standard)");
        ko(FIXER_MODE_ORIGINAL, "복원(기본)");
        it(FIXER_MODE_ORIGINAL, "Ripristina (predefinito)");

        ja(FIXER_MODE_HIGH_FREQUENCIES, "復元、高域を除去");
        zh(FIXER_MODE_HIGH_FREQUENCIES, "修复，去除高频");
        fr(FIXER_MODE_HIGH_FREQUENCIES, "Restaurer, aigus retirés");
        es(FIXER_MODE_HIGH_FREQUENCIES, "Restaurar, sin agudos");
        de(FIXER_MODE_HIGH_FREQUENCIES, "Wiederherstellen, Höhen entfernt");
        ko(FIXER_MODE_HIGH_FREQUENCIES, "복원, 고음역 제거");
        it(FIXER_MODE_HIGH_FREQUENCIES, "Ripristina, alte frequenze rimosse");

        ja(FIXER_MODE_DEGRADED, "大きく劣化した声");
        zh(FIXER_MODE_DEGRADED, "严重受损的人声");
        fr(FIXER_MODE_DEGRADED, "Voix très dégradée");
        es(FIXER_MODE_DEGRADED, "Voz muy degradada");
        de(FIXER_MODE_DEGRADED, "Stark beschädigte Stimme");
        ko(FIXER_MODE_DEGRADED, "심하게 손상된 목소리");
        it(FIXER_MODE_DEGRADED, "Voce molto degradata");

        ja(THE_CLEANUP_WORKS, "クリーンアップは動作します: ComfyUI には分離モデルが %d 個あります。");
        zh(THE_CLEANUP_WORKS, "清理可用：ComfyUI 提供 %d 个分离模型。");
        fr(THE_CLEANUP_WORKS, "Le nettoyage fonctionne : ComfyUI propose %d modèles de séparation.");
        es(THE_CLEANUP_WORKS, "La limpieza funciona: ComfyUI ofrece %d modelos de separación.");
        de(THE_CLEANUP_WORKS, "Die Bereinigung funktioniert: ComfyUI bietet %d Trennmodelle.");
        ko(THE_CLEANUP_WORKS, "정리가 작동합니다. ComfyUI에 분리 모델이 %d개 있습니다.");
        it(THE_CLEANUP_WORKS, "La pulizia funziona: ComfyUI offre %d modelli di separazione.");

        ja(KEEP_HELP, "どちらの出力に声が入るかはモデルによります: ボーカルモデルは声を抽出し、ノイズ除去モデルはノイズを抽出します (残り側を残す)。一度結果を聞いて確かめてください。");
        zh(KEEP_HELP, "人声在哪个输出中取决于模型：人声模型提取的是人声，降噪模型提取的是噪声（保留剩余部分）。请试听一次结果。");
        fr(KEEP_HELP, "La sortie qui contient la voix dépend du modèle : un modèle de voix extrait la voix, un modèle de débruitage extrait le bruit (gardez le reste). Écoutez le résultat une fois.");
        es(KEEP_HELP, "La salida que contiene la voz depende del modelo: un modelo de voces extrae la voz, un modelo de reducción de ruido extrae el ruido (conserva el resto). Escucha el resultado una vez.");
        de(KEEP_HELP, "Welcher Ausgang die Stimme enthält, hängt vom Modell ab: Ein Gesangsmodell extrahiert die Stimme, ein Entrauschungsmodell das Rauschen (Rest behalten). Hör dir das Ergebnis einmal an.");
        ko(KEEP_HELP, "목소리가 어느 출력에 담기는지는 모델에 따라 다릅니다. 보컬 모델은 목소리를, 잡음 제거 모델은 잡음을 추출합니다(나머지 쪽 유지). 결과를 한 번 들어 보세요.");
        it(KEEP_HELP, "Quale uscita contiene la voce dipende dal modello: un modello di voce estrae la voce, un modello di riduzione del rumore estrae il rumore (tieni il resto). Ascolta il risultato una volta.");

        ja(FROM_SECONDS, "開始 (秒)");
        zh(FROM_SECONDS, "起点（秒）");
        fr(FROM_SECONDS, "De (s)");
        es(FROM_SECONDS, "Desde (s)");
        de(FROM_SECONDS, "Von (s)");
        ko(FROM_SECONDS, "시작(초)");
        it(FROM_SECONDS, "Da (s)");

        ja(TO_SECONDS, "終了 (秒)");
        zh(TO_SECONDS, "终点（秒）");
        fr(TO_SECONDS, "À (s)");
        es(TO_SECONDS, "Hasta (s)");
        de(TO_SECONDS, "Bis (s)");
        ko(TO_SECONDS, "끝(초)");
        it(TO_SECONDS, "A (s)");

        ja(TAKE_THE_PLACE_OF_THE_PLAYER, "プレーヤーの再生位置を使う");
        zh(TAKE_THE_PLACE_OF_THE_PLAYER, "使用播放器的当前位置");
        fr(TAKE_THE_PLACE_OF_THE_PLAYER, "Prendre la position du lecteur");
        es(TAKE_THE_PLACE_OF_THE_PLAYER, "Tomar la posición del reproductor");
        de(TAKE_THE_PLACE_OF_THE_PLAYER, "Position des Players übernehmen");
        ko(TAKE_THE_PLACE_OF_THE_PLAYER, "플레이어의 현재 위치 사용");
        it(TAKE_THE_PLACE_OF_THE_PLAYER, "Prendi la posizione del lettore");

        ja(KEEP_THIS_PART, "この部分を残す");
        zh(KEEP_THIS_PART, "保留这一段");
        fr(KEEP_THIS_PART, "Garder ce passage");
        es(KEEP_THIS_PART, "Conservar esta parte");
        de(KEEP_THIS_PART, "Diesen Teil behalten");
        ko(KEEP_THIS_PART, "이 부분 유지");
        it(KEEP_THIS_PART, "Tieni questa parte");

        ja(CUT_THIS_PART_OUT, "この部分を切り取る");
        zh(CUT_THIS_PART_OUT, "剪掉这一段");
        fr(CUT_THIS_PART_OUT, "Couper ce passage");
        es(CUT_THIS_PART_OUT, "Cortar esta parte");
        de(CUT_THIS_PART_OUT, "Diesen Teil herausschneiden");
        ko(CUT_THIS_PART_OUT, "이 부분 잘라내기");
        it(CUT_THIS_PART_OUT, "Taglia questa parte");

        ja(TRIM, "トリミング");
        zh(TRIM, "剪辑");
        fr(TRIM, "Couper");
        es(TRIM, "Recortar");
        de(TRIM, "Schneiden");
        ko(TRIM, "자르기");
        it(TRIM, "Taglia");

        ja(TRIM_HELP, "この部分だけ、またはこの部分を除いたクリップのバージョンを作ります (クリーンアップで消えない音: 動物、咳、ドア)。場所はプレーヤーで探してください。");
        zh(TRIM_HELP, "生成只含这一段或去掉这一段的片段版本（清理去不掉的声音：动物、咳嗽、关门声）。用播放器找到位置。");
        fr(TRIM_HELP, "Crée une version de l'extrait avec seulement ce passage, ou sans lui (un son qu'aucun nettoyage n'enlève : un animal, une toux, une porte). Trouvez l'endroit avec le lecteur.");
        es(TRIM_HELP, "Crea una versión del clip solo con esta parte, o sin ella (un sonido que ninguna limpieza quita: un animal, una tos, una puerta). Busca el lugar con el reproductor.");
        de(TRIM_HELP, "Macht eine Version des Clips nur mit diesem Teil oder ohne ihn (ein Geräusch, das keine Bereinigung entfernt: ein Tier, ein Husten, eine Tür). Finde die Stelle mit dem Player.");
        ko(TRIM_HELP, "이 부분만 남기거나 이 부분을 뺀 클립 버전을 만듭니다(정리로 지워지지 않는 소리: 동물, 기침, 문). 위치는 플레이어로 찾으세요.");
        it(TRIM_HELP, "Crea una versione della clip con solo questa parte, o senza (un suono che nessuna pulizia toglie: un animale, una tosse, una porta). Trova il punto con il lettore.");

        ja(GIVE_A_RANGE, "部分の開始と終了を指定してください (開始 < 終了)。");
        zh(GIVE_A_RANGE, "请填写这一段的起点和终点（起点小于终点）。");
        fr(GIVE_A_RANGE, "Indiquez où le passage commence et finit (De avant À).");
        es(GIVE_A_RANGE, "Indica dónde empieza y termina la parte (Desde antes de Hasta).");
        de(GIVE_A_RANGE, "Gib an, wo der Teil beginnt und endet (Von vor Bis).");
        ko(GIVE_A_RANGE, "부분의 시작과 끝을 입력하세요(시작이 끝보다 앞).");
        it(GIVE_A_RANGE, "Indica dove la parte inizia e finisce (Da prima di A).");

        ja(THE_CLIP_COULD_NOT_BE_TRIMMED, "クリップをトリミングできませんでした: %s");
        zh(THE_CLIP_COULD_NOT_BE_TRIMMED, "无法剪辑片段：%s");
        fr(THE_CLIP_COULD_NOT_BE_TRIMMED, "L'extrait n'a pas pu être coupé : %s");
        es(THE_CLIP_COULD_NOT_BE_TRIMMED, "No se pudo recortar el clip: %s");
        de(THE_CLIP_COULD_NOT_BE_TRIMMED, "Der Clip konnte nicht geschnitten werden: %s");
        ko(THE_CLIP_COULD_NOT_BE_TRIMMED, "클립을 자를 수 없습니다: %s");
        it(THE_CLIP_COULD_NOT_BE_TRIMMED, "Impossibile tagliare la clip: %s");

        ja(VERSION_PART, "部分 %s");
        zh(VERSION_PART, "片段 %s");
        fr(VERSION_PART, "passage %s");
        es(VERSION_PART, "parte %s");
        de(VERSION_PART, "Teil %s");
        ko(VERSION_PART, "부분 %s");
        it(VERSION_PART, "parte %s");

        ja(VERSION_WITHOUT, "%s を除く");
        zh(VERSION_WITHOUT, "去掉 %s");
        fr(VERSION_WITHOUT, "sans %s");
        es(VERSION_WITHOUT, "sin %s");
        de(VERSION_WITHOUT, "ohne %s");
        ko(VERSION_WITHOUT, "%s 제외");
        it(VERSION_WITHOUT, "senza %s");
    }
}
