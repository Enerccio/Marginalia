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

    @Override
    protected void loadKeys() {
        ja(IMAGE_GENERATOR, "画像ジェネレーター");
        zh(IMAGE_GENERATOR, "图像生成器");

        ja(GENERATE_IMAGE, "画像を生成");
        zh(GENERATE_IMAGE, "生成图像");

        ja(IMAGE_GENERATOR_SETTINGS, "画像ジェネレーターの設定");
        zh(IMAGE_GENERATOR_SETTINGS, "图像生成器设置");

        ja(BACKEND, "バックエンド");
        zh(BACKEND, "后端");

        ja(NO_BACKEND_YET, "バックエンドがありません。設定 → 拡張機能の設定 → 画像ジェネレーターの設定で追加してください。");
        zh(NO_BACKEND_YET, "还没有后端。请在“设置 → 扩展设置 → 图像生成器设置”中添加。");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");

        ja(WRITE_THE_PROMPT, "モデルでプロンプトを書く");
        zh(WRITE_THE_PROMPT, "用模型撰写提示词");

        ja(MODEL_FOR_THE_PROMPT, "プロンプトを書くモデル");
        zh(MODEL_FOR_THE_PROMPT, "撰写提示词的模型");

        ja(ABORT, "中止");
        zh(ABORT, "中止");

        ja(NEGATIVE_PROMPT, "ネガティブプロンプト");
        zh(NEGATIVE_PROMPT, "负面提示词");

        ja(WIDTH, "幅");
        zh(WIDTH, "宽度");

        ja(HEIGHT, "高さ");
        zh(HEIGHT, "高度");

        ja(SIZE_AND_NEGATIVE_PROMPT, "サイズとネガティブプロンプト");
        zh(SIZE_AND_NEGATIVE_PROMPT, "尺寸和负面提示词");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");

        ja(REROLL, "再生成");
        zh(REROLL, "重新生成");

        ja(ACCEPT, "採用");
        zh(ACCEPT, "采用");

        ja(DISCARD, "破棄");
        zh(DISCARD, "丢弃");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(WAITING_FOR_THE_MODEL, "モデルを待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");

        ja(GENERATING_THE_IMAGE, "画像を生成しています...");
        zh(GENERATING_THE_IMAGE, "正在生成图像...");

        ja(THE_PROMPT_IS_EMPTY, "プロンプトが空です");
        zh(THE_PROMPT_IS_EMPTY, "提示词为空");

        ja(THE_IMAGE_WAS_ADDED, "画像をパートに追加しました");
        zh(THE_IMAGE_WAS_ADDED, "图像已添加到该部分");

        ja(THE_IMAGE_IS_NOT_VALID, "サーバーが有効な画像を返しませんでした");
        zh(THE_IMAGE_IS_NOT_VALID, "服务器没有返回有效的图像");

        ja(THE_BACKEND_WAS_RELEASED, "待機時間が過ぎたためバックエンドを解放しました");
        zh(THE_BACKEND_WAS_RELEASED, "空闲时间已过，已释放后端");

        ja(THE_AFTER_HOOK_FAILED, "画像の後のリクエストが失敗しました: %s");
        zh(THE_AFTER_HOOK_FAILED, "图像之后的请求失败：%s");

        ja(GENERATING_FAILED, "画像の生成に失敗しました: %s");
        zh(GENERATING_FAILED, "生成图像失败：%s");

        ja(THE_MODEL_FAILED, "モデルが失敗しました: %s");
        zh(THE_MODEL_FAILED, "模型失败：%s");

        ja(STYLE, "スタイル");
        zh(STYLE, "风格");

        ja(PUT_IN_FRONT_OF_EVERY_PROMPT, "すべてのプロンプトの前に付けます。本の雰囲気に合わせて指定します (例: 水彩画、柔らかい光)");
        zh(PUT_IN_FRONT_OF_EVERY_PROMPT, "加在每个提示词之前，用于统一书的画风（例如：水彩，柔和的光线）");

        ja(INSTRUCTION_FOR_THE_PROMPT_WRITER, "プロンプト作成の指示");
        zh(INSTRUCTION_FOR_THE_PROMPT_WRITER, "提示词撰写指令");

        ja(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "空または未変更の場合は組み込みのテキストを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT_IN_TEXT, "留空或未更改：使用内置文本");

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

        ja(TYPE, "種類");
        zh(TYPE, "类型");

        ja(COMFYUI, "ComfyUI");
        zh(COMFYUI, "ComfyUI");

        ja(OPENAI_IMAGES_API, "OpenAI 画像 API");
        zh(OPENAI_IMAGES_API, "OpenAI 图像 API");

        ja(ADDRESS, "アドレス");
        zh(ADDRESS, "地址");

        ja(BASE_ADDRESS_OF_THE_SERVER, "サーバーのベースアドレス (例: http://localhost:8188)");
        zh(BASE_ADDRESS_OF_THE_SERVER, "服务器的基础地址，例如 http://localhost:8188");

        ja(API_KEY, "API キー");
        zh(API_KEY, "API 密钥");

        ja(STORED_TYPE_TO_REPLACE, "保存済み。置き換えるには入力してください。");
        zh(STORED_TYPE_TO_REPLACE, "已保存。输入新密钥以替换。");

        ja(REMOVE_THE_KEY, "キーを削除");
        zh(REMOVE_THE_KEY, "删除密钥");

        ja(MODEL, "モデル");
        zh(MODEL, "模型");

        ja(WORKFLOW, "ワークフロー");
        zh(WORKFLOW, "工作流");

        ja(API_FORMAT_WORKFLOW, "ComfyUI の Save (API Format) で保存したワークフロー。{{prompt}}、{{negative}}、{{seed}}、{{width}}、{{height}} を入れる場所に書きます");
        zh(API_FORMAT_WORKFLOW, "用 ComfyUI 的 Save (API Format) 保存的工作流，在相应位置写入 {{prompt}}、{{negative}}、{{seed}}、{{width}} 和 {{height}}");

        ja(THE_SERVER_IS_LOCAL, "サーバーはこのマシンまたはローカルネットワークにあります");
        zh(THE_SERVER_IS_LOCAL, "服务器在本机或我的网络中");

        ja(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "ローカルネットワークのアドレスを許可します。クラウドのサーバーではオフのままにします");
        zh(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK, "允许本地网络地址；云端服务器请保持关闭");

        ja(TIMEOUT_SECONDS, "タイムアウト (秒)");
        zh(TIMEOUT_SECONDS, "超时（秒）");

        ja(RELEASE_AFTER_IDLE_SECONDS, "待機後に解放 (秒)");
        zh(RELEASE_AFTER_IDLE_SECONDS, "空闲后释放（秒）");

        ja(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "最後の画像からこの秒数後に画像後のリクエストを実行します。0 で実行しません");
        zh(THE_REQUEST_AFTER_THE_IMAGES_RUNS, "最后一张图像之后经过这么多秒执行“之后”的请求；0 表示不执行");

        ja(BEFORE_THE_FIRST_IMAGE, "最初の画像の前");
        zh(BEFORE_THE_FIRST_IMAGE, "第一张图像之前");

        ja(AFTER_THE_LAST_IMAGE, "最後の画像の後");
        zh(AFTER_THE_LAST_IMAGE, "最后一张图像之后");

        ja(A_REQUEST_MADE_AROUND_THE_IMAGES, "画像の前後に順番に行うリクエスト (例: GPU のメモリを解放する)。なければ何もしません。");
        zh(A_REQUEST_MADE_AROUND_THE_IMAGES, "在图像前后按顺序发出的请求（例如释放 GPU 显存）。没有则不执行任何操作。");

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

        ja(CONNECTED, "接続しました");
        zh(CONNECTED, "已连接");

        ja(CONNECTION_FAILED, "接続に失敗しました: %s");
        zh(CONNECTION_FAILED, "连接失败：%s");

        ja(SEND_LOREBOOK_ENTRIES, "ロアブックのエントリを送る");
        zh(SEND_LOREBOOK_ENTRIES, "发送设定集条目");
        ja(THE_ENTRIES_THE_PART_ACTIVATES, "パート (本文、指示、シーン設定、視点、登場人物) とあなたの詳細にフィルターが一致するエントリが、生成と同様にモデルへ送られます");
        zh(THE_ENTRIES_THE_PART_ACTIVATES, "其过滤条件出现在该部分（正文、指示、场景设定、视角和在场角色）及你的详细说明中的条目，会像生成时一样发送给模型");
        ja(DETAILS, "詳細");
        zh(DETAILS, "详细说明");
        ja(WHAT_THE_PICTURE_MUST_SHOW, "絵に描くものや見た目の指定。プロンプトの作成時に従います (例: 夜、彼女の顔のアップ、赤いドレス)");
        zh(WHAT_THE_PICTURE_MUST_SHOW, "画面必须呈现的内容或样式；提示词撰写时会遵循（例如：夜晚、她的脸部特写、红色连衣裙）");
        ja(LORE_ENTRIES_DID_NOT_FIT, "%d 件のロアブックのエントリがモデルのコンテキストに収まらず省略されました");
        zh(LORE_ENTRIES_DID_NOT_FIT, "有 %d 个设定集条目放不进模型的上下文，已被省略");
        ja(LORE_ENTRIES_SENT, "ロアブックのエントリを %d 件送信しました");
        zh(LORE_ENTRIES_SENT, "已发送 %d 个设定集条目");

        ja(IMAGE_GENERATOR_WRITE_THE_PROMPT, "画像ジェネレーター: プロンプトを作成");
        zh(IMAGE_GENERATOR_WRITE_THE_PROMPT, "图像生成器：撰写提示词");

        ja(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "画像ジェネレーター: 画像を生成");
        zh(IMAGE_GENERATOR_GENERATE_THE_IMAGE, "图像生成器：生成图像");
    }
}
