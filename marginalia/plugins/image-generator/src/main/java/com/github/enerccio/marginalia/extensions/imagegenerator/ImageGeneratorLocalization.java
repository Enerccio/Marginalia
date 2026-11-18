package com.github.enerccio.marginalia.extensions.imagegenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.imagegenerator.defaults.*;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.L;

import java.util.Map;

/**
 * Texts of the Image Generator in the languages besides English; the English text is the key.
 */
public class ImageGeneratorLocalization extends PluginLocalizationBundle {

    public static final ImageGeneratorLocalization T = new ImageGeneratorLocalization();

    // the built-in prompt texts, one object per prompt language
    private final ImageGeneratorDefaults englishDefaults = new ImageGeneratorDefaults();
    private final Map<Language, ImageGeneratorDefaults> translatedDefaults = Map.of(JA, new ImageGeneratorDefaultsJA(), ZH,
            new ImageGeneratorDefaultsZH(), FR, new ImageGeneratorDefaultsFR(), ES, new ImageGeneratorDefaultsES(), DE,
            new ImageGeneratorDefaultsDE(), IT, new ImageGeneratorDefaultsIT(), KO, new ImageGeneratorDefaultsKO());

    /** The built-in prompt texts in the prompt language of the user. */
    public ImageGeneratorDefaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

    public static final String IMAGE_GENERATOR = "Image Generator";
    public static final String GENERATE_IMAGE = "Generate image";
    public static final String IMAGE_GENERATOR_SETTINGS = "Image Generator Settings";
    public static final String BACKEND = "Backend";
    public static final String NO_BACKEND_YET = "No backend yet. Add one in Settings → Extension Settings → Image Generator Settings.";
    public static final String PROMPT = "Prompt";
    public static final String WRITE_THE_PROMPT = "Write the prompt with the model";
    public static final String MODEL_FOR_THE_PROMPT = "The model that writes the prompt";
    public static final String ABORT = "Abort";
    public static final String NEGATIVE_PROMPT = "Negative prompt";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String GENERATE = "Generate";
    public static final String REROLL = "Reroll";
    public static final String ACCEPT = "Accept";
    public static final String DISCARD = "Discard";
    public static final String CLOSE = "Close";
    public static final String WAITING_FOR_THE_MODEL = "Waiting for the model...";
    public static final String GENERATING_THE_IMAGE = "Generating the image...";
    public static final String THE_PROMPT_IS_EMPTY = "The prompt is empty";
    public static final String THE_IMAGE_WAS_ADDED = "The image was added to the part";
    public static final String THE_IMAGE_IS_NOT_VALID = "The server did not return a valid image";
    public static final String GENERATING_FAILED = "Generating the image failed: %s";
    public static final String THE_MODEL_FAILED = "The model failed: %s";
    public static final String STYLE = "Style";
    public static final String PUT_IN_FRONT_OF_EVERY_PROMPT = "Put in front of every prompt, for the look of the book (for example: watercolor, soft light)";
    public static final String INSTRUCTION_FOR_THE_PROMPT_WRITER = "Instruction for the prompt writer";
    public static final String BACKENDS = "Backends";
    public static final String ADD_A_BACKEND = "Add a backend";
    public static final String DELETE_THE_BACKEND = "Delete the backend";
    public static final String NEW_BACKEND = "New backend";
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String COMFYUI = "ComfyUI";
    public static final String OPENAI_IMAGES_API = "OpenAI images API";
    public static final String ADDRESS = "Address";
    public static final String BASE_ADDRESS_OF_THE_SERVER = "Base address of the server, for example http://localhost:8188";
    public static final String API_KEY = "Api key";
    public static final String STORED_TYPE_TO_REPLACE = "Stored. Type to replace it.";
    public static final String REMOVE_THE_KEY = "Remove the key";
    public static final String MODEL = "Model";
    public static final String WORKFLOW = "Workflow";
    public static final String API_FORMAT_WORKFLOW = "Workflow of ComfyUI saved with Save (API Format), with {{prompt}}, {{negative}}, {{seed}}, {{width}} and {{height}} where they go; {{lora}} and {{lora_strength}} in a LoRA loader let you choose the LoRA in the window";
    public static final String THE_SERVER_IS_LOCAL = "The server is on this machine or on my network";
    public static final String ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK = "Allows the addresses of the local network; leave it off for a server in the cloud";
    public static final String TIMEOUT_SECONDS = "Timeout (s)";
    public static final String ADD_A_REQUEST = "Add a request";
    public static final String REMOVE_THE_REQUEST = "Remove the request";
    public static final String REQUEST = "Request";
    public static final String WAIT_AFTERWARDS_SECONDS = "Wait afterwards (s)";
    public static final String METHOD = "Method";
    public static final String HEADERS_NAME_VALUE_ONE_PER_LINE = "Headers (Name: value, one per line)";
    public static final String BODY = "Body";
    public static final String TEST_CONNECTION = "Test connection";
    public static final String CONNECTED = "Connected";
    public static final String CONNECTION_FAILED = "Connection failed: %s";
    public static final String SEND_LOREBOOK_ENTRIES = "Send lorebook entries";
    public static final String THE_ENTRIES_THE_PART_ACTIVATES = "The entries whose filters are found in the part (its text, instructions, scene setting, point of view and characters present) and in your details go to the model, as in a generation";
    public static final String DETAILS = "Details";
    public static final String WHAT_THE_PICTURE_MUST_SHOW = "What the picture must show or how it must look; the prompt writer follows it (for example: at night, close up on her face, the red dress)";
    public static final String LORE_ENTRIES_DID_NOT_FIT = "%d lorebook entries did not fit into the context of the model and were left out";
    public static final String LORE_ENTRIES_SENT = "%d lorebook entries sent";
    public static final String IMAGE_GENERATOR_WRITE_THE_PROMPT = "Image Generator: write the prompt";
    public static final String IMAGE_GENERATOR_GENERATE_THE_IMAGE = "Image Generator: generate the image";

    public static final String LORA = "LoRA";
    public static final String LORA_STRENGTH = "LoRA strength";
    public static final String NO_LORA = "None";
    public static final String THE_LORAS_COULD_NOT_BE_READ = "The LoRAs of the server could not be read: %s";
    public static final String STYLE_FOR_THIS_WINDOW = "The style of the book (its Prompts tab, a folder or your settings); a change here is for this window only";
    public static final String NEGATIVE_PROMPT_HELP = "What the pictures must not show. A turbo model that runs without guidance (CFG 1) ignores it";
    public static final String A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE = "These are your defaults: a book or a folder can change the style and the negative prompt in its Prompts tab, under Extension Settings.";
    public static final String PROMPT_FORM = "Prompt form";
    public static final String TAGS_FORM = "Tags (SDXL, Pony, Illustrious)";
    public static final String SENTENCES_FORM = "Descriptive sentences (Flux, Krea, Qwen-Image, Z-Image)";
    public static final String FREE_TEXT = "Free text";
    public static final String PROMPT_FORM_HELP = "How the language model writes the prompt: what the image model of this backend understands best";
    public static final String KEY_OF_THE_REQUESTS = "Key of the requests around the images";
    public static final String BEFORE_EACH_IMAGE = "Before each image";
    public static final String AFTER_EACH_IMAGE = "After each image";
    public static final String REQUESTS_MADE_AROUND_EACH_IMAGE = "Requests made before and after each image, one after the other, for example to start or stop a container. Freeing the GPU for the language model is the job of the GPU lock. None does nothing.";
    public static final String GPU_LOCK_HELP = "The GPU lock the administrator set up for the GPU of this server: every image waits for its turn on the GPU, and the lock's steps load the server first (freeing the language model or another server when needed). Empty: no lock.";
    public static final String KEY_OF_THE_REQUESTS_HELP = "Sent as Authorization: Bearer with every request before and after the images that has no Authorization header of its own (a proxy such as LiteLLM wants it), stored encrypted and never shown again. Empty: the api key above is used.";

    @Override
    protected void loadKeys() {
        ja(IMAGE_GENERATOR, "画像ジェネレーター");
        zh(IMAGE_GENERATOR, "图像生成器");
        fr(IMAGE_GENERATOR, "Générateur d'images");
        es(IMAGE_GENERATOR, "Generador de imágenes");
        de(IMAGE_GENERATOR, "Bildgenerator");
        ko(IMAGE_GENERATOR, "이미지 생성기");
        it(IMAGE_GENERATOR, "Generatore di immagini");

        ja(GENERATE_IMAGE, "画像を生成");
        zh(GENERATE_IMAGE, "生成图像");
        fr(GENERATE_IMAGE, "Générer une image");
        es(GENERATE_IMAGE, "Generar imagen");
        de(GENERATE_IMAGE, "Bild generieren");
        ko(GENERATE_IMAGE, "이미지 생성");
        it(GENERATE_IMAGE, "Genera immagine");

        ja(IMAGE_GENERATOR_SETTINGS, "画像ジェネレーターの設定");
        zh(IMAGE_GENERATOR_SETTINGS, "图像生成器设置");
        fr(IMAGE_GENERATOR_SETTINGS, "Paramètres du générateur d'images");
        es(IMAGE_GENERATOR_SETTINGS, "Ajustes del generador de imágenes");
        de(IMAGE_GENERATOR_SETTINGS, "Einstellungen des Bildgenerators");
        ko(IMAGE_GENERATOR_SETTINGS, "이미지 생성기 설정");
        it(IMAGE_GENERATOR_SETTINGS, "Impostazioni del generatore di immagini");

        ja(BACKEND, "バックエンド");
        zh(BACKEND, "后端");
        fr(BACKEND, "Moteur");
        es(BACKEND, "Backend");
        de(BACKEND, "Backend");
        ko(BACKEND, "백엔드");
        it(BACKEND, "Backend");

        ja(NO_BACKEND_YET, "バックエンドがありません。設定 → 拡張機能の設定 → 画像ジェネレーターの設定で追加してください。");
        zh(NO_BACKEND_YET, "还没有后端。请在“设置 → 扩展设置 → 图像生成器设置”中添加。");
        fr(NO_BACKEND_YET, "Pas encore de moteur. Ajoutez-en un dans Paramètres → Paramètres des extensions → Paramètres du générateur d'images.");
        es(NO_BACKEND_YET, "Todavía no hay ningún backend. Añade uno en Ajustes → Ajustes de extensiones → Ajustes del generador de imágenes.");
        de(NO_BACKEND_YET, "Noch kein Backend. Füge eines unter Einstellungen → Erweiterungseinstellungen → Einstellungen des Bildgenerators hinzu.");
        ko(NO_BACKEND_YET, "아직 백엔드가 없습니다. 설정 → 확장 기능 설정 → 이미지 생성기 설정에서 추가하세요.");
        it(NO_BACKEND_YET, "Ancora nessun backend. Aggiungine uno in Impostazioni → Impostazioni delle estensioni → Impostazioni del generatore di immagini.");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");
        fr(PROMPT, "Prompt");
        es(PROMPT, "Prompt");
        de(PROMPT, "Prompt");
        ko(PROMPT, "프롬프트");
        it(PROMPT, "Prompt");

        ja(WRITE_THE_PROMPT, "モデルでプロンプトを書く");
        zh(WRITE_THE_PROMPT, "用模型撰写提示词");
        fr(WRITE_THE_PROMPT, "Écrire le prompt avec le modèle");
        es(WRITE_THE_PROMPT, "Escribir el prompt con el modelo");
        de(WRITE_THE_PROMPT, "Den Prompt mit dem Modell schreiben");
        ko(WRITE_THE_PROMPT, "모델로 프롬프트 작성");
        it(WRITE_THE_PROMPT, "Scrivi il prompt con il modello");

        ja(MODEL_FOR_THE_PROMPT, "プロンプトを書くモデル");
        zh(MODEL_FOR_THE_PROMPT, "撰写提示词的模型");
        fr(MODEL_FOR_THE_PROMPT, "Le modèle qui écrit le prompt");
        es(MODEL_FOR_THE_PROMPT, "El modelo que escribe el prompt");
        de(MODEL_FOR_THE_PROMPT, "Das Modell, das den Prompt schreibt");
        ko(MODEL_FOR_THE_PROMPT, "프롬프트를 쓰는 모델");
        it(MODEL_FOR_THE_PROMPT, "Il modello che scrive il prompt");

        ja(ABORT, "中止");
        zh(ABORT, "中止");
        fr(ABORT, "Interrompre");
        es(ABORT, "Abortar");
        de(ABORT, "Abbrechen");
        ko(ABORT, "중단");
        it(ABORT, "Interrompi");

        ja(NEGATIVE_PROMPT, "ネガティブプロンプト");
        zh(NEGATIVE_PROMPT, "负面提示词");
        fr(NEGATIVE_PROMPT, "Prompt négatif");
        es(NEGATIVE_PROMPT, "Prompt negativo");
        de(NEGATIVE_PROMPT, "Negativer Prompt");
        ko(NEGATIVE_PROMPT, "네거티브 프롬프트");
        it(NEGATIVE_PROMPT, "Prompt negativo");

        ja(WIDTH, "幅");
        zh(WIDTH, "宽度");
        fr(WIDTH, "Largeur");
        es(WIDTH, "Ancho");
        de(WIDTH, "Breite");
        ko(WIDTH, "너비");
        it(WIDTH, "Larghezza");

        ja(HEIGHT, "高さ");
        zh(HEIGHT, "高度");
        fr(HEIGHT, "Hauteur");
        es(HEIGHT, "Alto");
        de(HEIGHT, "Höhe");
        ko(HEIGHT, "높이");
        it(HEIGHT, "Altezza");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");
        fr(GENERATE, "Générer");
        es(GENERATE, "Generar");
        de(GENERATE, "Generieren");
        ko(GENERATE, "생성");
        it(GENERATE, "Genera");

        ja(REROLL, "再生成");
        zh(REROLL, "重新生成");
        fr(REROLL, "Relancer");
        es(REROLL, "Repetir");
        de(REROLL, "Neu würfeln");
        ko(REROLL, "다시 뽑기");
        it(REROLL, "Rilancia");

        ja(ACCEPT, "採用");
        zh(ACCEPT, "采用");
        fr(ACCEPT, "Accepter");
        es(ACCEPT, "Aceptar");
        de(ACCEPT, "Übernehmen");
        ko(ACCEPT, "수락");
        it(ACCEPT, "Accetta");

        ja(DISCARD, "破棄");
        zh(DISCARD, "丢弃");
        fr(DISCARD, "Abandonner");
        es(DISCARD, "Descartar");
        de(DISCARD, "Verwerfen");
        ko(DISCARD, "버리기");
        it(DISCARD, "Scarta");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(WAITING_FOR_THE_MODEL, "モデルを待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");
        fr(WAITING_FOR_THE_MODEL, "En attente du modèle...");
        es(WAITING_FOR_THE_MODEL, "Esperando al modelo...");
        de(WAITING_FOR_THE_MODEL, "Warte auf das Modell ...");
        ko(WAITING_FOR_THE_MODEL, "모델을 기다리는 중...");
        it(WAITING_FOR_THE_MODEL, "In attesa del modello...");

        ja(GENERATING_THE_IMAGE, "画像を生成しています...");
        zh(GENERATING_THE_IMAGE, "正在生成图像...");
        fr(GENERATING_THE_IMAGE, "Génération de l'image...");
        es(GENERATING_THE_IMAGE, "Generando la imagen...");
        de(GENERATING_THE_IMAGE, "Das Bild wird generiert ...");
        ko(GENERATING_THE_IMAGE, "이미지를 생성하는 중...");
        it(GENERATING_THE_IMAGE, "Generazione dell'immagine...");

        ja(THE_PROMPT_IS_EMPTY, "プロンプトが空です");
        zh(THE_PROMPT_IS_EMPTY, "提示词为空");
        fr(THE_PROMPT_IS_EMPTY, "Le prompt est vide");
        es(THE_PROMPT_IS_EMPTY, "El prompt está vacío");
        de(THE_PROMPT_IS_EMPTY, "Der Prompt ist leer");
        ko(THE_PROMPT_IS_EMPTY, "프롬프트가 비어 있습니다");
        it(THE_PROMPT_IS_EMPTY, "Il prompt è vuoto");

        ja(THE_IMAGE_WAS_ADDED, "画像をパートに追加しました");
        zh(THE_IMAGE_WAS_ADDED, "图像已添加到该部分");
        fr(THE_IMAGE_WAS_ADDED, "L'image a été ajoutée à la partie");
        es(THE_IMAGE_WAS_ADDED, "La imagen se ha añadido a la parte");
        de(THE_IMAGE_WAS_ADDED, "Das Bild wurde dem Abschnitt hinzugefügt");
        ko(THE_IMAGE_WAS_ADDED, "이미지가 파트에 추가되었습니다");
        it(THE_IMAGE_WAS_ADDED, "L'immagine è stata aggiunta alla parte");

        ja(THE_IMAGE_IS_NOT_VALID, "サーバーが有効な画像を返しませんでした");
        zh(THE_IMAGE_IS_NOT_VALID, "服务器没有返回有效的图像");
        fr(THE_IMAGE_IS_NOT_VALID, "Le serveur n'a pas renvoyé d'image valide");
        es(THE_IMAGE_IS_NOT_VALID, "El servidor no ha devuelto una imagen válida");
        de(THE_IMAGE_IS_NOT_VALID, "Der Server hat kein gültiges Bild zurückgegeben");
        ko(THE_IMAGE_IS_NOT_VALID, "서버가 올바른 이미지를 반환하지 않았습니다");
        it(THE_IMAGE_IS_NOT_VALID, "Il server non ha restituito un'immagine valida");

        ja(GENERATING_FAILED, "画像の生成に失敗しました: %s");
        zh(GENERATING_FAILED, "生成图像失败：%s");
        fr(GENERATING_FAILED, "La génération de l'image a échoué : %s");
        es(GENERATING_FAILED, "Falló la generación de la imagen: %s");
        de(GENERATING_FAILED, "Das Generieren des Bildes ist fehlgeschlagen: %s");
        ko(GENERATING_FAILED, "이미지 생성에 실패했습니다: %s");
        it(GENERATING_FAILED, "Generazione dell'immagine non riuscita: %s");

        ja(THE_MODEL_FAILED, "モデルが失敗しました: %s");
        zh(THE_MODEL_FAILED, "模型失败：%s");
        fr(THE_MODEL_FAILED, "Le modèle a échoué : %s");
        es(THE_MODEL_FAILED, "El modelo ha fallado: %s");
        de(THE_MODEL_FAILED, "Das Modell ist fehlgeschlagen: %s");
        ko(THE_MODEL_FAILED, "모델이 실패했습니다: %s");
        it(THE_MODEL_FAILED, "Il modello non è riuscito: %s");

        ja(STYLE, "スタイル");
        zh(STYLE, "风格");
        fr(STYLE, "Style");
        es(STYLE, "Estilo");
        de(STYLE, "Stil");
        ko(STYLE, "스타일");
        it(STYLE, "Stile");

        ja(PUT_IN_FRONT_OF_EVERY_PROMPT, "すべてのプロンプトの前に付けます。本の雰囲気に合わせて指定します (例: 水彩画、柔らかい光)");
        zh(PUT_IN_FRONT_OF_EVERY_PROMPT, "加在每个提示词之前，用于统一书的画风（例如：水彩，柔和的光线）");
        fr(PUT_IN_FRONT_OF_EVERY_PROMPT, "Placé devant chaque prompt, pour l'apparence du livre (par exemple : aquarelle, lumière douce)");
        es(PUT_IN_FRONT_OF_EVERY_PROMPT, "Se pone delante de cada prompt, para el aspecto del libro (por ejemplo: acuarela, luz suave)");
        de(PUT_IN_FRONT_OF_EVERY_PROMPT, "Wird jedem Prompt vorangestellt, für den Look des Buchs (zum Beispiel: Aquarell, weiches Licht)");
        ko(PUT_IN_FRONT_OF_EVERY_PROMPT, "모든 프롬프트 앞에 붙는 책의 분위기 (예: 수채화, 부드러운 빛)");
        it(PUT_IN_FRONT_OF_EVERY_PROMPT, "Messo davanti a ogni prompt, per l'aspetto del libro (ad esempio: acquerello, luce soffusa)");

        ja(INSTRUCTION_FOR_THE_PROMPT_WRITER, "プロンプト作成の指示");
        zh(INSTRUCTION_FOR_THE_PROMPT_WRITER, "提示词撰写指令");
        fr(INSTRUCTION_FOR_THE_PROMPT_WRITER, "Instruction pour le rédacteur du prompt");
        es(INSTRUCTION_FOR_THE_PROMPT_WRITER, "Instrucción para el redactor del prompt");
        de(INSTRUCTION_FOR_THE_PROMPT_WRITER, "Anweisung für den Prompt-Schreiber");
        ko(INSTRUCTION_FOR_THE_PROMPT_WRITER, "프롬프트 작성자에게 주는 지시문");
        it(INSTRUCTION_FOR_THE_PROMPT_WRITER, "Istruzione per chi scrive il prompt");

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

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");

        ja(TYPE, "種類");
        zh(TYPE, "类型");
        fr(TYPE, "Type");
        es(TYPE, "Tipo");
        de(TYPE, "Typ");
        ko(TYPE, "유형");
        it(TYPE, "Tipo");

        ja(COMFYUI, "ComfyUI");
        zh(COMFYUI, "ComfyUI");
        fr(COMFYUI, "ComfyUI");
        es(COMFYUI, "ComfyUI");
        de(COMFYUI, "ComfyUI");
        ko(COMFYUI, "ComfyUI");
        it(COMFYUI, "ComfyUI");

        ja(OPENAI_IMAGES_API, "OpenAI 画像 API");
        zh(OPENAI_IMAGES_API, "OpenAI 图像 API");
        fr(OPENAI_IMAGES_API, "API d'images OpenAI");
        es(OPENAI_IMAGES_API, "API de imágenes de OpenAI");
        de(OPENAI_IMAGES_API, "OpenAI-Images-API");
        ko(OPENAI_IMAGES_API, "OpenAI 이미지 API");
        it(OPENAI_IMAGES_API, "API di immagini di OpenAI");

        ja(ADDRESS, "アドレス");
        zh(ADDRESS, "地址");
        fr(ADDRESS, "Adresse");
        es(ADDRESS, "Dirección");
        de(ADDRESS, "Adresse");
        ko(ADDRESS, "주소");
        it(ADDRESS, "Indirizzo");

        ja(BASE_ADDRESS_OF_THE_SERVER, "サーバーのベースアドレス (例: http://localhost:8188)");
        zh(BASE_ADDRESS_OF_THE_SERVER, "服务器的基础地址，例如 http://localhost:8188");
        fr(BASE_ADDRESS_OF_THE_SERVER, "Adresse de base du serveur, par exemple http://localhost:8188");
        es(BASE_ADDRESS_OF_THE_SERVER, "Dirección base del servidor, por ejemplo http://localhost:8188");
        de(BASE_ADDRESS_OF_THE_SERVER, "Basisadresse des Servers, zum Beispiel http://localhost:8188");
        ko(BASE_ADDRESS_OF_THE_SERVER, "서버의 기본 주소, 예: http://localhost:8188");
        it(BASE_ADDRESS_OF_THE_SERVER, "Indirizzo di base del server, ad esempio http://localhost:8188");

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

        ja(MODEL, "モデル");
        zh(MODEL, "模型");
        fr(MODEL, "Modèle");
        es(MODEL, "Modelo");
        de(MODEL, "Modell");
        ko(MODEL, "모델");
        it(MODEL, "Modello");

        ja(WORKFLOW, "ワークフロー");
        zh(WORKFLOW, "工作流");
        fr(WORKFLOW, "Workflow");
        es(WORKFLOW, "Flujo de trabajo");
        de(WORKFLOW, "Workflow");
        ko(WORKFLOW, "워크플로");
        it(WORKFLOW, "Workflow");

        ja(API_FORMAT_WORKFLOW, "ComfyUI の Save (API Format) で保存したワークフロー。{{prompt}}、{{negative}}、{{seed}}、{{width}}、{{height}} を入れる場所に書きます。LoRA ローダーに {{lora}} と {{lora_strength}} を書くと、ウィンドウで LoRA を選べます");
        zh(API_FORMAT_WORKFLOW, "用 ComfyUI 的 Save (API Format) 保存的工作流，在相应位置写入 {{prompt}}、{{negative}}、{{seed}}、{{width}} 和 {{height}}；在 LoRA 加载器中写入 {{lora}} 和 {{lora_strength}}，即可在窗口中选择 LoRA");
        fr(API_FORMAT_WORKFLOW, "Workflow ComfyUI enregistré avec Save (API Format), avec {{prompt}}, {{negative}}, {{seed}}, {{width}} et {{height}} aux bons endroits ; {{lora}} et {{lora_strength}} dans un chargeur de LoRA permettent de choisir la LoRA dans la fenêtre");
        es(API_FORMAT_WORKFLOW, "Flujo de trabajo de ComfyUI guardado con Save (API Format), con {{prompt}}, {{negative}}, {{seed}}, {{width}} y {{height}} donde corresponda; {{lora}} y {{lora_strength}} en un cargador de LoRA permiten elegir la LoRA en la ventana");
        de(API_FORMAT_WORKFLOW, "Workflow von ComfyUI, gespeichert mit „Save (API Format)“, mit {{prompt}}, {{negative}}, {{seed}}, {{width}} und {{height}} an den passenden Stellen; {{lora}} und {{lora_strength}} in einem LoRA-Loader machen die LoRA im Fenster wählbar");
        ko(API_FORMAT_WORKFLOW, "저장(API 형식)으로 저장한 ComfyUI 워크플로로, {{prompt}}, {{negative}}, {{seed}}, {{width}}, {{height}}가 들어갈 위치에 지정합니다. LoRA 로더에 {{lora}}와 {{lora_strength}}를 지정하면 창에서 LoRA를 선택할 수 있습니다");
        it(API_FORMAT_WORKFLOW, "Workflow di ComfyUI salvato con Save (API Format), con {{prompt}}, {{negative}}, {{seed}}, {{width}} e {{height}} dove vanno; {{lora}} e {{lora_strength}} in un caricatore di LoRA permettono di scegliere la LoRA nella finestra");

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

        ja(TIMEOUT_SECONDS, "タイムアウト (秒)");
        zh(TIMEOUT_SECONDS, "超时（秒）");
        fr(TIMEOUT_SECONDS, "Délai d'attente (s)");
        es(TIMEOUT_SECONDS, "Tiempo de espera (s)");
        de(TIMEOUT_SECONDS, "Zeitlimit (s)");
        ko(TIMEOUT_SECONDS, "제한 시간 (초)");
        it(TIMEOUT_SECONDS, "Timeout (s)");

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

        ja(CONNECTED, "接続しました");
        zh(CONNECTED, "已连接");
        fr(CONNECTED, "Connecté");
        es(CONNECTED, "Conectado");
        de(CONNECTED, "Verbunden");
        ko(CONNECTED, "연결됨");
        it(CONNECTED, "Connesso");

        ja(CONNECTION_FAILED, "接続に失敗しました: %s");
        zh(CONNECTION_FAILED, "连接失败：%s");
        fr(CONNECTION_FAILED, "Échec de la connexion : %s");
        es(CONNECTION_FAILED, "Falló la conexión: %s");
        de(CONNECTION_FAILED, "Verbindung fehlgeschlagen: %s");
        ko(CONNECTION_FAILED, "연결에 실패했습니다: %s");
        it(CONNECTION_FAILED, "Connessione non riuscita: %s");

        ja(SEND_LOREBOOK_ENTRIES, "ロアブックのエントリを送る");
        zh(SEND_LOREBOOK_ENTRIES, "发送设定集条目");
        fr(SEND_LOREBOOK_ENTRIES, "Envoyer les entrées du lorebook");
        es(SEND_LOREBOOK_ENTRIES, "Enviar entradas del lorebook");
        de(SEND_LOREBOOK_ENTRIES, "Lorebook-Einträge senden");
        ko(SEND_LOREBOOK_ENTRIES, "로어북 항목 전송");
        it(SEND_LOREBOOK_ENTRIES, "Invia le voci del lorebook");
        ja(THE_ENTRIES_THE_PART_ACTIVATES, "パート (本文、指示、シーン設定、視点、登場人物) とあなたの詳細にフィルターが一致するエントリが、生成と同様にモデルへ送られます");
        zh(THE_ENTRIES_THE_PART_ACTIVATES, "其过滤条件出现在该部分（正文、指示、场景设定、视角和在场角色）及你的详细说明中的条目，会像生成时一样发送给模型");
        fr(THE_ENTRIES_THE_PART_ACTIVATES, "Les entrées dont les filtres sont trouvés dans la partie (son texte, ses instructions, le cadre de la scène, le point de vue et les personnages présents) et dans vos détails vont au modèle, comme lors d'une génération");
        es(THE_ENTRIES_THE_PART_ACTIVATES, "Las entradas cuyos filtros se encuentran en la parte (su texto, instrucciones, ambientación de la escena, punto de vista y personajes presentes) y en tus detalles se envían al modelo, como en una generación");
        de(THE_ENTRIES_THE_PART_ACTIVATES, "Die Einträge, deren Filter im Abschnitt (seinem Text, den Anweisungen, dem Schauplatz der Szene, der Erzählperspektive und den anwesenden Figuren) und in deinen Details gefunden werden, gehen wie bei einer Generierung an das Modell");
        ko(THE_ENTRIES_THE_PART_ACTIVATES, "필터가 파트(텍스트, 지시문, 장면 배경, 시점, 등장인물)와 내 세부 사항에서 발견된 항목이 생성에서처럼 모델에 전송됩니다");
        it(THE_ENTRIES_THE_PART_ACTIVATES, "Le voci i cui filtri si trovano nella parte (il suo testo, le istruzioni, l'ambientazione della scena, il punto di vista e i personaggi presenti) e nei tuoi dettagli vanno al modello, come in una generazione");
        ja(DETAILS, "詳細");
        zh(DETAILS, "详细说明");
        fr(DETAILS, "Détails");
        es(DETAILS, "Detalles");
        de(DETAILS, "Details");
        ko(DETAILS, "세부 사항");
        it(DETAILS, "Dettagli");
        ja(WHAT_THE_PICTURE_MUST_SHOW, "絵に描くものや見た目の指定。プロンプトの作成時に従います (例: 夜、彼女の顔のアップ、赤いドレス)");
        zh(WHAT_THE_PICTURE_MUST_SHOW, "画面必须呈现的内容或样式；提示词撰写时会遵循（例如：夜晚、她的脸部特写、红色连衣裙）");
        fr(WHAT_THE_PICTURE_MUST_SHOW, "Ce que l'image doit montrer ou à quoi elle doit ressembler ; le rédacteur du prompt le suit (par exemple : de nuit, gros plan sur son visage, la robe rouge)");
        es(WHAT_THE_PICTURE_MUST_SHOW, "Lo que debe mostrar la imagen o cómo debe verse; el redactor del prompt lo sigue (por ejemplo: de noche, primer plano de su rostro, el vestido rojo)");
        de(WHAT_THE_PICTURE_MUST_SHOW, "Was das Bild zeigen oder wie es aussehen muss; der Prompt-Schreiber folgt dem (zum Beispiel: bei Nacht, Nahaufnahme ihres Gesichts, das rote Kleid)");
        ko(WHAT_THE_PICTURE_MUST_SHOW, "그림이 보여 줘야 하는 것이나 모양. 프롬프트 작성자가 이를 따릅니다 (예: 밤, 그녀의 얼굴 클로즈업, 빨간 드레스)");
        it(WHAT_THE_PICTURE_MUST_SHOW, "Cosa deve mostrare l'immagine o come deve apparire; chi scrive il prompt la segue (ad esempio: di notte, primo piano sul suo viso, il vestito rosso)");
        ja(LORE_ENTRIES_DID_NOT_FIT, "%d 件のロアブックのエントリがモデルのコンテキストに収まらず省略されました");
        zh(LORE_ENTRIES_DID_NOT_FIT, "有 %d 个设定集条目放不进模型的上下文，已被省略");
        fr(LORE_ENTRIES_DID_NOT_FIT, "%d entrées du lorebook ne tenaient pas dans le contexte du modèle et ont été omises");
        es(LORE_ENTRIES_DID_NOT_FIT, "%d entradas del lorebook no cabían en el contexto del modelo y se omitieron");
        de(LORE_ENTRIES_DID_NOT_FIT, "%d Lorebook-Einträge passten nicht in den Kontext des Modells und wurden weggelassen");
        ko(LORE_ENTRIES_DID_NOT_FIT, "로어북 항목 %d개가 모델의 컨텍스트에 맞지 않아 제외되었습니다");
        it(LORE_ENTRIES_DID_NOT_FIT, "%d voci del lorebook non rientravano nel contesto del modello e sono state escluse");
        ja(LORE_ENTRIES_SENT, "ロアブックのエントリを %d 件送信しました");
        zh(LORE_ENTRIES_SENT, "已发送 %d 个设定集条目");
        fr(LORE_ENTRIES_SENT, "%d entrées du lorebook envoyées");
        es(LORE_ENTRIES_SENT, "%d entradas del lorebook enviadas");
        de(LORE_ENTRIES_SENT, "%d Lorebook-Einträge gesendet");
        ko(LORE_ENTRIES_SENT, "로어북 항목 %d개를 전송했습니다");
        it(LORE_ENTRIES_SENT, "%d voci del lorebook inviate");

        ja(IMAGE_GENERATOR_WRITE_THE_PROMPT, "画像ジェネレーター: プロンプトを作成");
        zh(IMAGE_GENERATOR_WRITE_THE_PROMPT, "图像生成器：撰写提示词");
        fr(IMAGE_GENERATOR_WRITE_THE_PROMPT, "Générateur d'images : écrire le prompt");
        es(IMAGE_GENERATOR_WRITE_THE_PROMPT, "Generador de imágenes: escribir el prompt");
        de(IMAGE_GENERATOR_WRITE_THE_PROMPT, "Bildgenerator: den Prompt schreiben");
        ko(IMAGE_GENERATOR_WRITE_THE_PROMPT, "이미지 생성기: 프롬프트 쓰기");
        it(IMAGE_GENERATOR_WRITE_THE_PROMPT, "Generatore di immagini: scrivi il prompt");

        ja(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "画像ジェネレーター: 画像を生成");
        zh(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "图像生成器：生成图像");
        fr(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "Générateur d'images : générer l'image");
        es(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "Generador de imágenes: generar la imagen");
        de(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "Bildgenerator: das Bild generieren");
        ko(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "이미지 생성기: 이미지 생성");
        it(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "Generatore di immagini: genera l'immagine");

        ja(LORA, "LoRA");
        zh(LORA, "LoRA");
        fr(LORA, "LoRA");
        es(LORA, "LoRA");
        de(LORA, "LoRA");
        ko(LORA, "LoRA");
        it(LORA, "LoRA");

        ja(LORA_STRENGTH, "LoRA の強さ");
        zh(LORA_STRENGTH, "LoRA 强度");
        fr(LORA_STRENGTH, "Force de la LoRA");
        es(LORA_STRENGTH, "Intensidad de la LoRA");
        de(LORA_STRENGTH, "LoRA-Stärke");
        ko(LORA_STRENGTH, "LoRA 강도");
        it(LORA_STRENGTH, "Intensità della LoRA");

        ja(NO_LORA, "なし");
        zh(NO_LORA, "无");
        fr(NO_LORA, "Aucune");
        es(NO_LORA, "Ninguna");
        de(NO_LORA, "Keine");
        ko(NO_LORA, "없음");
        it(NO_LORA, "Nessuna");

        ja(THE_LORAS_COULD_NOT_BE_READ, "サーバーの LoRA を読み込めませんでした: %s");
        zh(THE_LORAS_COULD_NOT_BE_READ, "无法读取服务器的 LoRA：%s");
        fr(THE_LORAS_COULD_NOT_BE_READ, "Impossible de lire les LoRA du serveur : %s");
        es(THE_LORAS_COULD_NOT_BE_READ, "No se pudieron leer las LoRA del servidor: %s");
        de(THE_LORAS_COULD_NOT_BE_READ, "Die LoRAs des Servers konnten nicht gelesen werden: %s");
        ko(THE_LORAS_COULD_NOT_BE_READ, "서버의 LoRA를 읽을 수 없습니다: %s");
        it(THE_LORAS_COULD_NOT_BE_READ, "Impossibile leggere le LoRA del server: %s");

        ja(STYLE_FOR_THIS_WINDOW, "本のスタイル (本の「プロンプト」タブ、フォルダー、またはあなたの設定)。ここでの変更はこのウィンドウだけに適用されます");
        zh(STYLE_FOR_THIS_WINDOW, "本书的风格（书的“提示词”选项卡、文件夹或你的设置）；在此处的更改只对本窗口有效");
        fr(STYLE_FOR_THIS_WINDOW, "Le style du livre (son onglet Prompts, un dossier ou vos paramètres) ; une modification ici ne vaut que pour cette fenêtre");
        es(STYLE_FOR_THIS_WINDOW, "El estilo del libro (su pestaña Prompts, una carpeta o tus ajustes); un cambio aquí solo vale para esta ventana");
        de(STYLE_FOR_THIS_WINDOW, "Der Stil des Buchs (sein Tab „Prompts“, ein Ordner oder deine Einstellungen); eine Änderung hier gilt nur für dieses Fenster");
        ko(STYLE_FOR_THIS_WINDOW, "책의 스타일(책의 프롬프트 탭, 폴더 또는 내 설정). 여기서 바꾼 내용은 이 창에만 적용됩니다");
        it(STYLE_FOR_THIS_WINDOW, "Lo stile del libro (la sua scheda Prompt, una cartella o le tue impostazioni); una modifica qui vale solo per questa finestra");

        ja(NEGATIVE_PROMPT_HELP, "画像に描いてはいけないもの。ガイダンスなし (CFG 1) で動くターボモデルでは無視されます");
        zh(NEGATIVE_PROMPT_HELP, "图片中不应出现的内容。不使用引导（CFG 1）运行的 turbo 模型会忽略它");
        fr(NEGATIVE_PROMPT_HELP, "Ce que les images ne doivent pas montrer. Un modèle turbo qui tourne sans guidage (CFG 1) l'ignore");
        es(NEGATIVE_PROMPT_HELP, "Lo que las imágenes no deben mostrar. Un modelo turbo que funciona sin guía (CFG 1) lo ignora");
        de(NEGATIVE_PROMPT_HELP, "Was die Bilder nicht zeigen dürfen. Ein Turbo-Modell ohne Guidance (CFG 1) ignoriert ihn");
        ko(NEGATIVE_PROMPT_HELP, "그림에 나오면 안 되는 것. 가이던스 없이(CFG 1) 동작하는 터보 모델은 이를 무시합니다");
        it(NEGATIVE_PROMPT_HELP, "Ciò che le immagini non devono mostrare. Un modello turbo che funziona senza guida (CFG 1) lo ignora");

        ja(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "これはあなたの既定値です。本やフォルダーは「プロンプト」タブの「拡張機能の設定」で、スタイルとネガティブプロンプトを変更できます。");
        zh(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "这些是你的默认值：书或文件夹可以在其“提示词”选项卡的“扩展设置”中更改风格和负面提示词。");
        fr(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "Ce sont vos valeurs par défaut : un livre ou un dossier peut changer le style et le prompt négatif dans son onglet Prompts, sous Paramètres des extensions.");
        es(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "Estos son tus valores predeterminados: un libro o una carpeta puede cambiar el estilo y el prompt negativo en su pestaña Prompts, en Ajustes de extensiones.");
        de(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "Das sind deine Standardwerte: Ein Buch oder ein Ordner kann Stil und Negativ-Prompt in seinem Tab „Prompts“ unter „Erweiterungseinstellungen“ ändern.");
        ko(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "이 값은 기본값입니다. 책이나 폴더는 프롬프트 탭의 확장 설정에서 스타일과 네거티브 프롬프트를 바꿀 수 있습니다.");
        it(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE, "Questi sono i tuoi valori predefiniti: un libro o una cartella può cambiare lo stile e il prompt negativo nella sua scheda Prompt, in Impostazioni delle estensioni.");

        ja(PROMPT_FORM, "プロンプトの形式");
        zh(PROMPT_FORM, "提示词形式");
        fr(PROMPT_FORM, "Forme du prompt");
        es(PROMPT_FORM, "Forma del prompt");
        de(PROMPT_FORM, "Form des Prompts");
        ko(PROMPT_FORM, "프롬프트 형식");
        it(PROMPT_FORM, "Forma del prompt");

        ja(TAGS_FORM, "タグ (SDXL、Pony、Illustrious)");
        zh(TAGS_FORM, "标签（SDXL、Pony、Illustrious）");
        fr(TAGS_FORM, "Tags (SDXL, Pony, Illustrious)");
        es(TAGS_FORM, "Etiquetas (SDXL, Pony, Illustrious)");
        de(TAGS_FORM, "Tags (SDXL, Pony, Illustrious)");
        ko(TAGS_FORM, "태그 (SDXL, Pony, Illustrious)");
        it(TAGS_FORM, "Tag (SDXL, Pony, Illustrious)");

        ja(SENTENCES_FORM, "説明文 (Flux、Krea、Qwen-Image、Z-Image)");
        zh(SENTENCES_FORM, "描述性句子（Flux、Krea、Qwen-Image、Z-Image）");
        fr(SENTENCES_FORM, "Phrases descriptives (Flux, Krea, Qwen-Image, Z-Image)");
        es(SENTENCES_FORM, "Frases descriptivas (Flux, Krea, Qwen-Image, Z-Image)");
        de(SENTENCES_FORM, "Beschreibende Sätze (Flux, Krea, Qwen-Image, Z-Image)");
        ko(SENTENCES_FORM, "설명 문장 (Flux, Krea, Qwen-Image, Z-Image)");
        it(SENTENCES_FORM, "Frasi descrittive (Flux, Krea, Qwen-Image, Z-Image)");

        ja(FREE_TEXT, "自由記述");
        zh(FREE_TEXT, "自定义文本");
        fr(FREE_TEXT, "Texte libre");
        es(FREE_TEXT, "Texto libre");
        de(FREE_TEXT, "Freier Text");
        ko(FREE_TEXT, "자유 텍스트");
        it(FREE_TEXT, "Testo libero");

        ja(PROMPT_FORM_HELP, "言語モデルがプロンプトをどう書くか。このバックエンドの画像モデルが最もよく理解する形式を選びます");
        zh(PROMPT_FORM_HELP, "语言模型如何撰写提示词：选择此后端的图像模型最能理解的形式");
        fr(PROMPT_FORM_HELP, "Comment le modèle de langage écrit le prompt : ce que le modèle d'images de ce backend comprend le mieux");
        es(PROMPT_FORM_HELP, "Cómo escribe el prompt el modelo de lenguaje: lo que mejor entiende el modelo de imágenes de este backend");
        de(PROMPT_FORM_HELP, "Wie das Sprachmodell den Prompt schreibt: was das Bildmodell dieses Backends am besten versteht");
        ko(PROMPT_FORM_HELP, "언어 모델이 프롬프트를 쓰는 방식: 이 백엔드의 이미지 모델이 가장 잘 이해하는 형식");
        it(PROMPT_FORM_HELP, "Come il modello linguistico scrive il prompt: ciò che il modello di immagini di questo backend capisce meglio");

        ja(KEY_OF_THE_REQUESTS, "画像の前後のリクエストのキー");
        zh(KEY_OF_THE_REQUESTS, "图片前后请求的密钥");
        fr(KEY_OF_THE_REQUESTS, "Clé des requêtes autour des images");
        es(KEY_OF_THE_REQUESTS, "Clave de las solicitudes alrededor de las imágenes");
        de(KEY_OF_THE_REQUESTS, "Schlüssel der Anfragen rund um die Bilder");
        ko(KEY_OF_THE_REQUESTS, "이미지 전후 요청의 키");
        it(KEY_OF_THE_REQUESTS, "Chiave delle richieste attorno alle immagini");

        ja(KEY_OF_THE_REQUESTS_HELP, "画像の前後のリクエストのうち、独自の Authorization ヘッダーを持たないものすべてに Authorization: Bearer として付けられます (LiteLLM などのプロキシが必要とします)。暗号化して保存され、再表示されません。空: 上の API キーを使います。");
        zh(KEY_OF_THE_REQUESTS_HELP, "作为 Authorization: Bearer 附加到图片前后每个没有自己 Authorization 请求头的请求（LiteLLM 等代理需要它），加密保存且不再显示。留空：使用上面的 API 密钥。");
        fr(KEY_OF_THE_REQUESTS_HELP, "Envoyée comme Authorization: Bearer avec chaque requête avant et après les images qui n'a pas d'en-tête Authorization à elle (un proxy comme LiteLLM la demande), stockée chiffrée et jamais réaffichée. Vide : la clé d'API ci-dessus est utilisée.");
        es(KEY_OF_THE_REQUESTS_HELP, "Se envía como Authorization: Bearer con cada solicitud anterior y posterior a las imágenes que no tenga su propia cabecera Authorization (un proxy como LiteLLM la pide), se guarda cifrada y no se vuelve a mostrar. Vacío: se usa la clave de API de arriba.");
        de(KEY_OF_THE_REQUESTS_HELP, "Wird als Authorization: Bearer bei jeder Anfrage vor und nach den Bildern gesendet, die keinen eigenen Authorization-Header hat (ein Proxy wie LiteLLM verlangt ihn), verschlüsselt gespeichert und nie wieder angezeigt. Leer: Der API-Schlüssel oben wird verwendet.");
        ko(KEY_OF_THE_REQUESTS_HELP, "이미지 전후의 요청 중 자체 Authorization 헤더가 없는 모든 요청에 Authorization: Bearer로 전송되며(LiteLLM 같은 프록시가 요구합니다), 암호화되어 저장되고 다시 표시되지 않습니다. 비워 두면 위의 API 키를 사용합니다.");
        it(KEY_OF_THE_REQUESTS_HELP, "Inviata come Authorization: Bearer con ogni richiesta prima e dopo le immagini che non ha un'intestazione Authorization propria (un proxy come LiteLLM la richiede), memorizzata cifrata e mai più mostrata. Vuoto: viene usata la chiave API qui sopra.");

        ja(BEFORE_EACH_IMAGE, "各画像の前");
        zh(BEFORE_EACH_IMAGE, "每张图像之前");
        fr(BEFORE_EACH_IMAGE, "Avant chaque image");
        es(BEFORE_EACH_IMAGE, "Antes de cada imagen");
        de(BEFORE_EACH_IMAGE, "Vor jedem Bild");
        ko(BEFORE_EACH_IMAGE, "각 이미지 전");
        it(BEFORE_EACH_IMAGE, "Prima di ogni immagine");

        ja(AFTER_EACH_IMAGE, "各画像の後");
        zh(AFTER_EACH_IMAGE, "每张图像之后");
        fr(AFTER_EACH_IMAGE, "Après chaque image");
        es(AFTER_EACH_IMAGE, "Después de cada imagen");
        de(AFTER_EACH_IMAGE, "Nach jedem Bild");
        ko(AFTER_EACH_IMAGE, "각 이미지 후");
        it(AFTER_EACH_IMAGE, "Dopo ogni immagine");

        ja(REQUESTS_MADE_AROUND_EACH_IMAGE, "各画像の前後に順番に行うリクエスト (例: コンテナの起動や停止)。言語モデルのために GPU を空けるのは GPU ロックの役目です。なければ何もしません。");
        zh(REQUESTS_MADE_AROUND_EACH_IMAGE, "在每张图像前后按顺序发出的请求（例如启动或停止容器）。为语言模型腾出 GPU 由 GPU 锁负责。没有则不执行任何操作。");
        fr(REQUESTS_MADE_AROUND_EACH_IMAGE, "Requêtes faites avant et après chaque image, l'une après l'autre, par exemple pour démarrer ou arrêter un conteneur. Libérer le GPU pour le modèle de langage est le rôle du verrou GPU. Aucune : ne fait rien.");
        es(REQUESTS_MADE_AROUND_EACH_IMAGE, "Solicitudes que se hacen antes y después de cada imagen, una tras otra, por ejemplo para arrancar o detener un contenedor. Liberar la GPU para el modelo de lenguaje es tarea del bloqueo de GPU. Ninguna no hace nada.");
        de(REQUESTS_MADE_AROUND_EACH_IMAGE, "Anfragen vor und nach jedem Bild, nacheinander, zum Beispiel um einen Container zu starten oder zu stoppen. Die GPU für das Sprachmodell freizugeben ist Aufgabe der GPU-Sperre. „Keine“ tut nichts.");
        ko(REQUESTS_MADE_AROUND_EACH_IMAGE, "각 이미지 전후에 차례로 실행하는 요청, 예를 들어 컨테이너를 시작하거나 멈추기 위한 것. 언어 모델을 위해 GPU를 비우는 것은 GPU 잠금이 맡습니다. 없음은 아무것도 하지 않습니다.");
        it(REQUESTS_MADE_AROUND_EACH_IMAGE, "Richieste fatte prima e dopo ogni immagine, una dopo l'altra, ad esempio per avviare o fermare un container. Liberare la GPU per il modello linguistico è compito del blocco GPU. Nessuna non fa nulla.");

        ja(GPU_LOCK_HELP, "このサーバーの GPU 用に管理者が設定した GPU ロックです。各画像は GPU の順番を待ち、ロックのステップが先にサーバーをロードします (必要なら言語モデルや他のサーバーを解放します)。空: ロックなし。");
        zh(GPU_LOCK_HELP, "管理员为此服务器所在 GPU 设置的 GPU 锁：每张图像都会等待轮到自己使用 GPU，锁的步骤会先加载服务器（需要时释放语言模型或其他服务器）。留空：无锁。");
        fr(GPU_LOCK_HELP, "Le verrou GPU que l'administrateur a configuré pour le GPU de ce serveur : chaque image attend son tour sur le GPU, et les étapes du verrou chargent d'abord le serveur (en libérant le modèle de langage ou un autre serveur si besoin). Vide : aucun verrou.");
        es(GPU_LOCK_HELP, "El bloqueo de GPU que el administrador configuró para la GPU de este servidor: cada imagen espera su turno en la GPU y los pasos del bloqueo cargan primero el servidor (liberando el modelo de lenguaje u otro servidor si hace falta). Vacío: sin bloqueo.");
        de(GPU_LOCK_HELP, "Die GPU-Sperre, die der Administrator für die GPU dieses Servers eingerichtet hat: Jedes Bild wartet, bis es an der Reihe ist, und die Schritte der Sperre laden zuerst den Server (und geben bei Bedarf das Sprachmodell oder einen anderen Server frei). Leer: keine Sperre.");
        ko(GPU_LOCK_HELP, "관리자가 이 서버의 GPU에 설정한 GPU 잠금입니다. 각 이미지는 GPU 차례를 기다리고, 잠금의 단계가 먼저 서버를 로드합니다(필요하면 언어 모델이나 다른 서버를 해제합니다). 비워 두면 잠금 없음.");
        it(GPU_LOCK_HELP, "Il blocco GPU che l'amministratore ha configurato per la GPU di questo server: ogni immagine attende il suo turno sulla GPU e i passi del blocco caricano prima il server (liberando il modello linguistico o un altro server se serve). Vuoto: nessun blocco.");
    }
}
