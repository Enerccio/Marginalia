package com.github.enerccio.marginalia.extensions.imagegenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Image Generator in the languages besides English; the English text is the key.
 */
public class ImageGeneratorLocalization extends PluginLocalizationBundle {

    public static final ImageGeneratorLocalization T = new ImageGeneratorLocalization();

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
    public static final String SIZE_AND_NEGATIVE_PROMPT = "Size and negative prompt";
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
    public static final String THE_BACKEND_WAS_RELEASED = "The backend was released after the idle time";
    public static final String THE_AFTER_HOOK_FAILED = "The request after the images failed: %s";
    public static final String GENERATING_FAILED = "Generating the image failed: %s";
    public static final String THE_MODEL_FAILED = "The model failed: %s";
    public static final String STYLE = "Style";
    public static final String PUT_IN_FRONT_OF_EVERY_PROMPT = "Put in front of every prompt, for the look of the book (for example: watercolor, soft light)";
    public static final String INSTRUCTION_FOR_THE_PROMPT_WRITER = "Instruction for the prompt writer";
    public static final String EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT = "Empty or unchanged: the built-in text is used";
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
    public static final String API_FORMAT_WORKFLOW = "Workflow of ComfyUI saved with Save (API Format), with {{prompt}}, {{negative}}, {{seed}}, {{width}} and {{height}} where they go";
    public static final String THE_SERVER_IS_LOCAL = "The server is on this machine or on my network";
    public static final String ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK = "Allows the addresses of the local network; leave it off for a server in the cloud";
    public static final String TIMEOUT_SECONDS = "Timeout (s)";
    public static final String RELEASE_AFTER_IDLE_SECONDS = "Release after idle (s)";
    public static final String THE_REQUEST_AFTER_THE_IMAGES_RUNS = "The request after the images runs this many seconds after the last image; 0 never";
    public static final String BEFORE_THE_FIRST_IMAGE = "Before the first image";
    public static final String AFTER_THE_LAST_IMAGE = "After the last image";
    public static final String A_REQUEST_MADE_AROUND_THE_IMAGES = "Requests made around the images, one after the other, for example to free the memory of the GPU. None does nothing.";
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

    public static final String GPU_NAME = "GPU (name)";
    public static final String GPU_NAME_HELP = "Optional. Name the GPU when this server shares it with other servers (a speech server, another image server), and give the same name there: they then wait for each other instead of unloading each other's model. Empty: only requests to this address wait for each other.";

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

        ja(SIZE_AND_NEGATIVE_PROMPT, "サイズとネガティブプロンプト");
        zh(SIZE_AND_NEGATIVE_PROMPT, "尺寸和负面提示词");
        fr(SIZE_AND_NEGATIVE_PROMPT, "Taille et prompt négatif");
        es(SIZE_AND_NEGATIVE_PROMPT, "Tamaño y prompt negativo");
        de(SIZE_AND_NEGATIVE_PROMPT, "Größe und negativer Prompt");
        ko(SIZE_AND_NEGATIVE_PROMPT, "크기와 네거티브 프롬프트");
        it(SIZE_AND_NEGATIVE_PROMPT, "Dimensione e prompt negativo");

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

        ja(THE_BACKEND_WAS_RELEASED, "待機時間が過ぎたためバックエンドを解放しました");
        zh(THE_BACKEND_WAS_RELEASED, "空闲时间已过，已释放后端");
        fr(THE_BACKEND_WAS_RELEASED, "Le moteur a été libéré après le délai d'inactivité");
        es(THE_BACKEND_WAS_RELEASED, "El backend se liberó tras el tiempo de inactividad");
        de(THE_BACKEND_WAS_RELEASED, "Das Backend wurde nach der Leerlaufzeit freigegeben");
        ko(THE_BACKEND_WAS_RELEASED, "유휴 시간 후 백엔드가 해제되었습니다");
        it(THE_BACKEND_WAS_RELEASED, "Il backend è stato rilasciato dopo il tempo di inattività");

        ja(THE_AFTER_HOOK_FAILED, "画像の後のリクエストが失敗しました: %s");
        zh(THE_AFTER_HOOK_FAILED, "图像之后的请求失败：%s");
        fr(THE_AFTER_HOOK_FAILED, "La requête après les images a échoué : %s");
        es(THE_AFTER_HOOK_FAILED, "Falló la solicitud posterior a las imágenes: %s");
        de(THE_AFTER_HOOK_FAILED, "Die Anfrage nach den Bildern ist fehlgeschlagen: %s");
        ko(THE_AFTER_HOOK_FAILED, "이미지 후의 요청이 실패했습니다: %s");
        it(THE_AFTER_HOOK_FAILED, "La richiesta dopo le immagini non è riuscita: %s");

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

        ja(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "空または未変更の場合は組み込みのテキストを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "留空或未更改：使用内置文本");
        fr(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "Vide ou inchangé : le texte intégré est utilisé");
        es(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "Vacío o sin cambios: se usa el texto integrado");
        de(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "Leer oder unverändert: Der eingebaute Text wird verwendet");
        ko(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "비어 있거나 변경하지 않으면: 내장 텍스트를 사용합니다");
        it(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "Vuoto o invariato: viene usato il testo integrato");

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

        ja(API_FORMAT_WORKFLOW, "ComfyUI の Save (API Format) で保存したワークフロー。{{prompt}}、{{negative}}、{{seed}}、{{width}}、{{height}} を入れる場所に書きます");
        zh(API_FORMAT_WORKFLOW, "用 ComfyUI 的 Save (API Format) 保存的工作流，在相应位置写入 {{prompt}}、{{negative}}、{{seed}}、{{width}} 和 {{height}}");
        fr(API_FORMAT_WORKFLOW, "Workflow ComfyUI enregistré avec Save (API Format), avec {{prompt}}, {{negative}}, {{seed}}, {{width}} et {{height}} aux bons endroits");
        es(API_FORMAT_WORKFLOW, "Flujo de trabajo de ComfyUI guardado con Save (API Format), con {{prompt}}, {{negative}}, {{seed}}, {{width}} y {{height}} donde corresponda");
        de(API_FORMAT_WORKFLOW, "Workflow von ComfyUI, gespeichert mit „Save (API Format)“, mit {{prompt}}, {{negative}}, {{seed}}, {{width}} und {{height}} an den passenden Stellen");
        ko(API_FORMAT_WORKFLOW, "저장(API 형식)으로 저장한 ComfyUI 워크플로로, {{prompt}}, {{negative}}, {{seed}}, {{width}}, {{height}}가 들어갈 위치에 지정합니다");
        it(API_FORMAT_WORKFLOW, "Workflow di ComfyUI salvato con Save (API Format), con {{prompt}}, {{negative}}, {{seed}}, {{width}} e {{height}} dove vanno");

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

        ja(RELEASE_AFTER_IDLE_SECONDS, "待機後に解放 (秒)");
        zh(RELEASE_AFTER_IDLE_SECONDS, "空闲后释放（秒）");
        fr(RELEASE_AFTER_IDLE_SECONDS, "Libérer après inactivité (s)");
        es(RELEASE_AFTER_IDLE_SECONDS, "Liberar tras inactividad (s)");
        de(RELEASE_AFTER_IDLE_SECONDS, "Freigeben nach Leerlauf (s)");
        ko(RELEASE_AFTER_IDLE_SECONDS, "유휴 후 해제 (초)");
        it(RELEASE_AFTER_IDLE_SECONDS, "Rilascia dopo l'inattività (s)");

        ja(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "最後の画像からこの秒数後に画像後のリクエストを実行します。0 で実行しません");
        zh(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "最后一张图像之后经过这么多秒执行“之后”的请求；0 表示不执行");
        fr(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "La requête après les images s'exécute ce nombre de secondes après la dernière image ; 0 jamais");
        es(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "La solicitud posterior a las imágenes se ejecuta tantos segundos después de la última imagen; 0 nunca");
        de(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "Die Anfrage nach den Bildern läuft so viele Sekunden nach dem letzten Bild; 0 nie");
        ko(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "이미지 후의 요청은 마지막 이미지로부터 이 시간(초) 후에 실행됩니다. 0은 실행하지 않음");
        it(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "La richiesta dopo le immagini viene eseguita questi secondi dopo l'ultima immagine; 0 mai");

        ja(BEFORE_THE_FIRST_IMAGE, "最初の画像の前");
        zh(BEFORE_THE_FIRST_IMAGE, "第一张图像之前");
        fr(BEFORE_THE_FIRST_IMAGE, "Avant la première image");
        es(BEFORE_THE_FIRST_IMAGE, "Antes de la primera imagen");
        de(BEFORE_THE_FIRST_IMAGE, "Vor dem ersten Bild");
        ko(BEFORE_THE_FIRST_IMAGE, "첫 이미지 전");
        it(BEFORE_THE_FIRST_IMAGE, "Prima della prima immagine");

        ja(AFTER_THE_LAST_IMAGE, "最後の画像の後");
        zh(AFTER_THE_LAST_IMAGE, "最后一张图像之后");
        fr(AFTER_THE_LAST_IMAGE, "Après la dernière image");
        es(AFTER_THE_LAST_IMAGE, "Después de la última imagen");
        de(AFTER_THE_LAST_IMAGE, "Nach dem letzten Bild");
        ko(AFTER_THE_LAST_IMAGE, "마지막 이미지 후");
        it(AFTER_THE_LAST_IMAGE, "Dopo l'ultima immagine");

        ja(A_REQUEST_MADE_AROUND_THE_IMAGES, "画像の前後に順番に行うリクエスト (例: GPU のメモリを解放する)。なければ何もしません。");
        zh(A_REQUEST_MADE_AROUND_THE_IMAGES, "在图像前后按顺序发出的请求（例如释放 GPU 显存）。没有则不执行任何操作。");
        fr(A_REQUEST_MADE_AROUND_THE_IMAGES, "Requêtes faites autour des images, l'une après l'autre, par exemple pour libérer la mémoire du GPU. Aucune : ne fait rien.");
        es(A_REQUEST_MADE_AROUND_THE_IMAGES, "Solicitudes que se hacen alrededor de las imágenes, una tras otra, por ejemplo para liberar la memoria de la GPU. Ninguna no hace nada.");
        de(A_REQUEST_MADE_AROUND_THE_IMAGES, "Anfragen, die um die Bilder herum gemacht werden, nacheinander, zum Beispiel um den Speicher der GPU freizugeben. „Keine“ tut nichts.");
        ko(A_REQUEST_MADE_AROUND_THE_IMAGES, "이미지 전후에 차례로 실행하는 요청, 예를 들어 GPU 메모리를 비우기 위한 것. 없음은 아무것도 하지 않습니다.");
        it(A_REQUEST_MADE_AROUND_THE_IMAGES, "Richieste fatte attorno alle immagini, una dopo l'altra, ad esempio per liberare la memoria della GPU. Nessuna non fa nulla.");

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

        ja(GPU_NAME, "GPU（名前）");
        zh(GPU_NAME, "GPU（名称）");
        fr(GPU_NAME, "GPU (nom)");
        es(GPU_NAME, "GPU (nombre)");
        de(GPU_NAME, "GPU (Name)");
        ko(GPU_NAME, "GPU (이름)");
        it(GPU_NAME, "GPU (nome)");

        ja(GPU_NAME_HELP, "任意。このサーバーが他のサーバー (音声サーバーなど) とGPUを共有している場合に名前を付け、他にも同じ名前を指定します。互いのモデルを解放せず、順番に待ちます。空の場合は、このアドレスへのリクエスト同士だけが待ち合います。");
        zh(GPU_NAME_HELP, "可选。如果此服务器与其他服务器（如语音服务器）共用一块 GPU，请给它起个名字，并在其他服务器上填写相同的名字：它们会互相等待，而不是卸载对方的模型。留空：只有发往此地址的请求会互相等待。");
        fr(GPU_NAME_HELP, "Facultatif. Nommez le GPU quand ce serveur le partage avec d'autres serveurs (un serveur de synthèse vocale, un autre serveur d'images), et donnez-y le même nom : ils s'attendent alors au lieu de décharger le modèle des autres. Vide : seules les requêtes vers cette adresse s'attendent.");
        es(GPU_NAME_HELP, "Opcional. Indica el nombre de la GPU cuando este servidor la comparte con otros servidores (un servidor de voz, otro servidor de imágenes) y da allí el mismo nombre: entonces esperan unos a otros en lugar de descargarse mutuamente el modelo. Vacío: solo esperan entre sí las solicitudes a esta dirección.");
        de(GPU_NAME_HELP, "Optional. Gib der GPU einen Namen, wenn dieser Server sie mit anderen Servern teilt (einem Sprachserver, einem anderen Bildserver), und gib dort denselben Namen an: Sie warten dann aufeinander, statt gegenseitig ihr Modell zu entladen. Leer: Nur Anfragen an diese Adresse warten aufeinander.");
        ko(GPU_NAME_HELP, "선택 사항. 이 서버가 GPU를 다른 서버(음성 서버, 다른 이미지 서버)와 공유할 때 GPU 이름을 지정하고 그쪽에도 같은 이름을 지정하세요: 그러면 서로의 모델을 언로드하는 대신 서로를 기다립니다. 비워 두면 이 주소로의 요청끼리만 서로 기다립니다.");
        it(GPU_NAME_HELP, "Facoltativo. Indica il nome della GPU quando questo server la condivide con altri server (un server vocale, un altro server di immagini), e indica lì lo stesso nome: si aspettano allora a vicenda invece di scaricare il modello l'uno dell'altro. Vuoto: si aspettano solo le richieste a questo indirizzo.");
    }
}
