package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.stconverter.service.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Converter (formerly SillyTavern Converter) in the languages besides English; the English text is the key.
 */
public class STConverterLocalization extends PluginLocalizationBundle {

    public static final STConverterLocalization T = new STConverterLocalization();

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

    public static final String CONVERTER = "Converter";
    public static final String FROM = "From";
    public static final String TO = "To";
    public static final String ALL = "All";
    public static final String CONVERT_RANGE = "Convert range";
    public static final String ASK_THE_MODEL = "Ask the model for the turn details of the messages in the range";
    public static final String EDIT_THE_PROMPT = "Edit the prompt of the wand";
    public static final String ALIGN_TO_TURNS = "Align to turns";
    public static final String THE_RANGE_STARTS = "The range starts at the beginning of a turn and ends with a message of the character (the end of the range moves back to the last message of the character before it)";
    public static final String AUTO_MODE = "Auto mode";
    public static final String CONVERT_THE_WHOLE_RANGE = "Convert the whole range in batches of messages, one request after another";
    public static final String MESSAGES_PER_BATCH = "Messages per batch";
    public static final String CONVERT_IN_BATCHES = "Convert in batches";
    public static final String ASK_THE_MODEL_FOR_THE_TURN = "Ask the model for the turn details of the range, one batch of messages after another";
    public static final String CLICK_A_MESSAGE_TO_START = "Click a message to start a range, Shift+click to extend it.";
    public static final String TAP_A_MESSAGE_TO_START = "Tap a message to start a range, tap another to end it.";
    public static final String CONVERT_TO_TURN_DETAILS = "Convert to turn details";
    public static final String CHAT = "Chat";
    public static final String TURN_DETAILS = "Turn details";
    public static final String MESSAGE = "Message";
    public static final String MSGS = "Msgs";
    public static final String TURN = "Turn";
    public static final String DELETE_THE_TURN_DETAILS = "Delete the turn details";
    public static final String MERGE_WITH_THE_NEXT_TURN = "Merge with the next turn";
    public static final String THE_TURN_DETAILS_HAVE_CHANGED = "The turn details have changed, nothing was merged";
    public static final String THE_NEW_TURN_FORM = "The new turn form of the story is not available";
    public static final String CLICK_TO_PUT_THE_TURN = "Click to put the turn details into the new turn form";
    public static final String SCENE_SETTING = "Scene setting";
    public static final String POV_CHARACTER = "POV character";
    public static final String PRESENT_CHARACTERS = "Present characters";
    public static final String INSTRUCTIONS = "Instructions";
    public static final String REMOVE_THE_CHAT_AND_THE_TURN = "Remove the chat and the turn details made of it from the book?";
    public static final String REMOVE_THE_CHAT = "Remove the chat";
    public static final String CHAT_LOWERCASE = "chat";
    public static final String MESSAGES_CHECKED = "%d messages, %d checked";
    public static final String MESSAGES_LOADED = "%d messages loaded";
    public static final String NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT = "Not a SillyTavern or LM Studio chat";
    public static final String THE_CHAT_HAS_NO_MESSAGES = "The chat has no messages";
    public static final String GO_TO_THE_NEXT_UNCHECKED = "Go to the next unchecked row";
    public static final String ALL_MESSAGES_ARE_CHECKED = "All messages are checked";
    public static final String THERE_ARE_NO_TURN_DETAILS = "There are no turn details";
    public static final String ALL_TURNS_ARE_CHECKED = "All turns are checked";
    public static final String NO_TURN_DETAILS_YET_CHOOSE = "No turn details yet: choose a range and use the wand.";
    public static final String TURNS_CHECKED_CLICK_ONE = "%d turns, %d checked. Click one to open it in the new turn form.";
    public static final String TURN_DETAILS_D = "Turn details (%d)";
    public static final String MESSAGES_ABOUT_TOKENS_CHAT = "%d messages: about %s tokens (chat %s + prompt %s)";
    public static final String REQUEST_ABOUT_TOKENS_IN_ALL = "%d request, about %s tokens in all";
    public static final String REQUESTS_ABOUT_TOKENS_IN_ALL = "%d requests, about %s tokens in all";
    public static final String LOAD_A_CHAT_FIRST = "Load a chat first";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String WAITING_FOR_THE_MODEL = "Waiting for the model...";
    public static final String CANT_CONVERT = "Can't convert: %s";
    public static final String BATCH = "Batch %d of %d: %s";
    public static final String BATCH_OF_TRY = "Batch %d of %d (try %d of %d): %s";
    public static final String WRITING_THE_TURN_DETAILS_SO = "Writing the turn details... %d so far";
    public static final String THE_MODEL_IS_THINKING = "The model is thinking...";
    public static final String CANT_GO = "Can't go on: %s";
    public static final String STOPPED_NOTHING_WAS_CHANGED = "Stopped, nothing was changed";
    public static final String THE_MODEL_DID_NOT_ANSWER = "The model did not answer.";
    public static final String TURNS_FOR_THE_MESSAGES = "%1$d turns for the messages %2$d-%3$d";
    public static final String EARLIER_ONES_REPLACED = " (%d earlier ones replaced)";
    public static final String CONVERT_THE_MESSAGES_IN_REQUESTS = "Convert the messages %d-%d in %d requests to the model, one after another? The earlier turn details of the range that you have not ticked are replaced.";
    public static final String TURNS_MADE_IN_REQUESTS = "%1$d turns made in %2$d requests";
    public static final String STOPPED_AFTER_OF_REQUESTS_TURNS = "Stopped after %d of %d requests (%d turns made)";
    public static final String THE_RANGE_IS_WHAT = ". The range is what is left.";
    public static final String PROMPT_OF_THE_WAND = "Prompt of the wand";
    public static final String VARIABLES_BACKGROUNDLORE_CHAT = "Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}";
    public static final String DEFAULT = "Default";
    public static final String CANCEL = "Cancel";
    public static final String SAVE = "Save";
    public static final String STOP = "Stop";
    public static final String THE_MODELS_ANSWER_DOES = "The model's answer does not match what was asked for: %s";
    public static final String THE_MODELS_ANSWER_HAS_NO = "The model's answer has no turn for the messages %d to %d";
    public static final String UPLOAD_A_CHAT = "Upload a chat";
    public static final String LOAD_A_BACKUP = "Load a backup";
    public static final String PICK_A_BOOK = "Pick a book";
    public static final String A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO = "A SillyTavern chat (.jsonl) or an LM Studio conversation (.conversation.json), without branches, to rewrite as a story with the model.";
    public static final String OR_THE_PARTS = "Or the parts of a book of yours, from a backup file or the book itself: their instructions, and the text to redraft.";
    public static final String ONLY_THE_INSTRUCTIONS = "Only the instructions and the text are kept with this book. Remove the source to load another.";
    public static final String PARTS_LOADED = "%d parts loaded";
    public static final String NOT_A_BACKUP = "Not a backup of a book";
    public static final String BOOK = "Book";
    public static final String LOAD = "Load";
    public static final String THE_PARTS_OF_THE_BRANCH = "The parts of the branch the book is on are loaded.";
    public static final String PARTS_CHECKED = "%d parts, %d checked";
    public static final String PARTS = "Parts";
    public static final String REMOVE_THE_SOURCE = "Remove the source";
    public static final String REMOVE_THE_SOURCE_AND_WHAT = "Remove the source and what you checked of it from the book?";
    public static final String THE_BOOK_HAS_NO_PARTS = "The book has no parts to load";
    public static final String THE_BACKUP_IS_DAMAGED = "The backup is damaged: the parts do not lead to the first part";
    public static final String THE_STORED_SOURCE = "The stored source of the book can't be read";
    public static final String THE_STORED_SOURCE_OF_THE_BOOK = "The stored source of the book is from a newer version of the plugin";
    public static final String CONVERTER_CONVERT_THE_CHAT = "Converter: convert the chat into turn details";

    @Override
    protected void loadKeys() {
        ja(CONVERTER, "コンバーター");
        zh(CONVERTER, "转换器");
        fr(CONVERTER, "Convertisseur");
        es(CONVERTER, "Conversor");
        de(CONVERTER, "Konverter");
        ko(CONVERTER, "변환기");

        ja(FROM, "開始");
        zh(FROM, "起始");
        fr(FROM, "De");
        es(FROM, "Desde");
        de(FROM, "Von");
        ko(FROM, "시작");

        ja(TO, "終了");
        zh(TO, "结束");
        fr(TO, "À");
        es(TO, "Hasta");
        de(TO, "Bis");
        ko(TO, "끝");

        ja(ALL, "すべて");
        zh(ALL, "全部");
        fr(ALL, "Tout");
        es(ALL, "Todos");
        de(ALL, "Alle");
        ko(ALL, "전체");

        ja(CONVERT_RANGE, "範囲を変換");
        zh(CONVERT_RANGE, "转换范围");
        fr(CONVERT_RANGE, "Convertir la plage");
        es(CONVERT_RANGE, "Convertir el intervalo");
        de(CONVERT_RANGE, "Bereich konvertieren");
        ko(CONVERT_RANGE, "범위 변환");

        ja(ASK_THE_MODEL, "範囲内のメッセージのターン詳細をモデルに依頼します");
        zh(ASK_THE_MODEL, "让模型为范围内的消息生成回合详情");
        fr(ASK_THE_MODEL, "Demander au modèle les détails de tour des messages de la plage");
        es(ASK_THE_MODEL, "Pedir al modelo los detalles de turno de los mensajes del intervalo");
        de(ASK_THE_MODEL, "Das Modell nach den Zugdetails der Nachrichten im Bereich fragen");
        ko(ASK_THE_MODEL, "범위 안의 메시지에 대한 턴 상세 정보를 모델에 요청합니다");

        ja(EDIT_THE_PROMPT, "ワンドのプロンプトを編集");
        zh(EDIT_THE_PROMPT, "编辑魔杖的提示词");
        fr(EDIT_THE_PROMPT, "Modifier le prompt de la baguette");
        es(EDIT_THE_PROMPT, "Editar el prompt de la varita");
        de(EDIT_THE_PROMPT, "Den Prompt des Zauberstabs bearbeiten");
        ko(EDIT_THE_PROMPT, "마법봉의 프롬프트 편집");

        ja(ALIGN_TO_TURNS, "ターンに揃える");
        zh(ALIGN_TO_TURNS, "对齐到回合");
        fr(ALIGN_TO_TURNS, "Aligner sur les tours");
        es(ALIGN_TO_TURNS, "Ajustar a los turnos");
        de(ALIGN_TO_TURNS, "An Zügen ausrichten");
        ko(ALIGN_TO_TURNS, "턴에 맞추기");

        ja(THE_RANGE_STARTS, "範囲はターンの先頭から始まり、キャラクターのメッセージで終わります（範囲の終わりは、その手前にあるキャラクターの最後のメッセージまで戻ります）");
        zh(THE_RANGE_STARTS, "范围从回合的开头开始，以角色的消息结束（范围的结尾会回退到其之前角色的最后一条消息）");
        fr(THE_RANGE_STARTS, "La plage commence au début d'un tour et finit par un message du personnage (la fin de la plage recule jusqu'au dernier message du personnage qui la précède)");
        es(THE_RANGE_STARTS, "El intervalo empieza al comienzo de un turno y termina con un mensaje del personaje (el final del intervalo retrocede hasta el último mensaje del personaje anterior a él)");
        de(THE_RANGE_STARTS, "Der Bereich beginnt am Anfang eines Zugs und endet mit einer Nachricht der Figur (das Ende des Bereichs rückt zur letzten Nachricht der Figur davor zurück)");
        ko(THE_RANGE_STARTS, "범위는 턴의 시작에서 시작하여 캐릭터의 메시지로 끝납니다 (범위의 끝은 그 앞의 캐릭터 마지막 메시지로 물러납니다)");

        ja(AUTO_MODE, "自動モード");
        zh(AUTO_MODE, "自动模式");
        fr(AUTO_MODE, "Mode automatique");
        es(AUTO_MODE, "Modo automático");
        de(AUTO_MODE, "Automatikmodus");
        ko(AUTO_MODE, "자동 모드");

        ja(CONVERT_THE_WHOLE_RANGE, "範囲全体をメッセージのバッチに分けて、リクエストを1件ずつ順に送って変換します");
        zh(CONVERT_THE_WHOLE_RANGE, "将整个范围分成若干批消息，逐个请求依次转换");
        fr(CONVERT_THE_WHOLE_RANGE, "Convertir toute la plage par lots de messages, une requête après l'autre");
        es(CONVERT_THE_WHOLE_RANGE, "Convierte todo el intervalo en lotes de mensajes, una solicitud tras otra");
        de(CONVERT_THE_WHOLE_RANGE, "Den ganzen Bereich in Nachrichtenstapeln konvertieren, eine Anfrage nach der anderen");
        ko(CONVERT_THE_WHOLE_RANGE, "전체 범위를 메시지 묶음 단위로 차례로 요청하며 변환합니다");

        ja(MESSAGES_PER_BATCH, "バッチあたりのメッセージ数");
        zh(MESSAGES_PER_BATCH, "每批消息数");
        fr(MESSAGES_PER_BATCH, "Messages par lot");
        es(MESSAGES_PER_BATCH, "Mensajes por lote");
        de(MESSAGES_PER_BATCH, "Nachrichten je Stapel");
        ko(MESSAGES_PER_BATCH, "묶음당 메시지 수");

        ja(CONVERT_IN_BATCHES, "バッチで変換");
        zh(CONVERT_IN_BATCHES, "分批转换");
        fr(CONVERT_IN_BATCHES, "Convertir par lots");
        es(CONVERT_IN_BATCHES, "Convertir por lotes");
        de(CONVERT_IN_BATCHES, "In Stapeln konvertieren");
        ko(CONVERT_IN_BATCHES, "묶음으로 변환");

        ja(ASK_THE_MODEL_FOR_THE_TURN, "範囲のターン詳細をモデルに、メッセージのバッチごとに順に依頼します");
        zh(ASK_THE_MODEL_FOR_THE_TURN, "依次按每批消息让模型生成该范围的回合详情");
        fr(ASK_THE_MODEL_FOR_THE_TURN, "Demander au modèle les détails de tour de la plage, un lot de messages après l'autre");
        es(ASK_THE_MODEL_FOR_THE_TURN, "Pide al modelo los detalles de turno del intervalo, un lote de mensajes tras otro");
        de(ASK_THE_MODEL_FOR_THE_TURN, "Das Modell nach den Zugdetails des Bereichs fragen, einen Nachrichtenstapel nach dem anderen");
        ko(ASK_THE_MODEL_FOR_THE_TURN, "범위의 턴 상세 정보를 메시지 묶음 단위로 차례로 모델에 요청합니다");

        ja(CLICK_A_MESSAGE_TO_START, "メッセージをクリックすると範囲を開始し、Shiftを押しながらクリックすると範囲を広げます。");
        zh(CLICK_A_MESSAGE_TO_START, "点击一条消息开始选择范围，按住 Shift 点击可扩展范围。");
        fr(CLICK_A_MESSAGE_TO_START, "Cliquez sur un message pour commencer une plage, Maj+clic pour l'étendre.");
        es(CLICK_A_MESSAGE_TO_START, "Haz clic en un mensaje para empezar un intervalo, Mayús+clic para ampliarlo.");
        de(CLICK_A_MESSAGE_TO_START, "Klicke auf eine Nachricht, um einen Bereich zu beginnen, Umschalt+Klick erweitert ihn.");
        ko(CLICK_A_MESSAGE_TO_START, "메시지를 클릭하여 범위를 시작하고, Shift+클릭으로 확장합니다.");

        ja(TAP_A_MESSAGE_TO_START, "メッセージをタップすると範囲を開始し、別のメッセージをタップすると範囲を終了します。");
        zh(TAP_A_MESSAGE_TO_START, "点按一条消息开始选择范围，再点按另一条消息结束范围。");
        fr(TAP_A_MESSAGE_TO_START, "Touchez un message pour commencer une plage, touchez-en un autre pour la terminer.");
        es(TAP_A_MESSAGE_TO_START, "Toca un mensaje para empezar un intervalo, toca otro para terminarlo.");
        de(TAP_A_MESSAGE_TO_START, "Tippe auf eine Nachricht, um einen Bereich zu beginnen, tippe auf eine andere, um ihn zu beenden.");
        ko(TAP_A_MESSAGE_TO_START, "메시지를 탭하여 범위를 시작하고, 다른 메시지를 탭하여 끝냅니다.");

        ja(CONVERT_TO_TURN_DETAILS, "ターン詳細に変換");
        zh(CONVERT_TO_TURN_DETAILS, "转换为回合详情");
        fr(CONVERT_TO_TURN_DETAILS, "Convertir en détails de tour");
        es(CONVERT_TO_TURN_DETAILS, "Convertir en detalles de turno");
        de(CONVERT_TO_TURN_DETAILS, "In Zugdetails konvertieren");
        ko(CONVERT_TO_TURN_DETAILS, "턴 상세 정보로 변환");

        ja(CHAT, "チャット");
        zh(CHAT, "聊天");
        fr(CHAT, "Discussion");
        es(CHAT, "Chat");
        de(CHAT, "Chat");
        ko(CHAT, "채팅");

        ja(TURN_DETAILS, "ターン詳細");
        zh(TURN_DETAILS, "回合详情");
        fr(TURN_DETAILS, "Détails de tour");
        es(TURN_DETAILS, "Detalles del turno");
        de(TURN_DETAILS, "Zugdetails");
        ko(TURN_DETAILS, "턴 상세 정보");

        ja(MESSAGE, "メッセージ");
        zh(MESSAGE, "消息");
        fr(MESSAGE, "Message");
        es(MESSAGE, "Mensaje");
        de(MESSAGE, "Nachricht");
        ko(MESSAGE, "메시지");

        ja(MSGS, "件数");
        zh(MSGS, "消息数");
        fr(MSGS, "Msgs");
        es(MSGS, "Msj.");
        de(MSGS, "Nachr.");
        ko(MSGS, "메시지");

        ja(TURN, "ターン");
        zh(TURN, "回合");
        fr(TURN, "Tour");
        es(TURN, "Turno");
        de(TURN, "Zug");
        ko(TURN, "턴");

        ja(DELETE_THE_TURN_DETAILS, "ターン詳細を削除");
        zh(DELETE_THE_TURN_DETAILS, "删除回合详情");
        fr(DELETE_THE_TURN_DETAILS, "Supprimer les détails de tour");
        es(DELETE_THE_TURN_DETAILS, "Eliminar los detalles del turno");
        de(DELETE_THE_TURN_DETAILS, "Die Zugdetails löschen");
        ko(DELETE_THE_TURN_DETAILS, "턴 상세 정보 삭제");

        ja(MERGE_WITH_THE_NEXT_TURN, "次のターンと統合");
        zh(MERGE_WITH_THE_NEXT_TURN, "与下一个回合合并");
        fr(MERGE_WITH_THE_NEXT_TURN, "Fusionner avec le tour suivant");
        es(MERGE_WITH_THE_NEXT_TURN, "Combinar con el turno siguiente");
        de(MERGE_WITH_THE_NEXT_TURN, "Mit dem nächsten Zug zusammenführen");
        ko(MERGE_WITH_THE_NEXT_TURN, "다음 턴과 병합");

        ja(THE_TURN_DETAILS_HAVE_CHANGED, "ターン詳細が変更されたため、統合されませんでした");
        zh(THE_TURN_DETAILS_HAVE_CHANGED, "回合详情已更改，未进行合并");
        fr(THE_TURN_DETAILS_HAVE_CHANGED, "Les détails de tour ont changé, rien n'a été fusionné");
        es(THE_TURN_DETAILS_HAVE_CHANGED, "Los detalles del turno han cambiado, no se ha combinado nada");
        de(THE_TURN_DETAILS_HAVE_CHANGED, "Die Zugdetails haben sich geändert, nichts wurde zusammengeführt");
        ko(THE_TURN_DETAILS_HAVE_CHANGED, "턴 상세 정보가 변경되어 아무것도 병합되지 않았습니다");

        ja(THE_NEW_TURN_FORM, "ストーリーの新規ターンフォームは利用できません");
        zh(THE_NEW_TURN_FORM, "故事的新回合表单不可用");
        fr(THE_NEW_TURN_FORM, "Le formulaire du nouveau tour de l'histoire n'est pas disponible");
        es(THE_NEW_TURN_FORM, "El formulario del nuevo turno de la historia no está disponible");
        de(THE_NEW_TURN_FORM, "Das Formular für den neuen Zug der Geschichte ist nicht verfügbar");
        ko(THE_NEW_TURN_FORM, "이야기의 새 턴 입력 창을 사용할 수 없습니다");

        ja(CLICK_TO_PUT_THE_TURN, "クリックしてターン詳細を新規ターンフォームに入れます");
        zh(CLICK_TO_PUT_THE_TURN, "点击将回合详情放入新回合表单");
        fr(CLICK_TO_PUT_THE_TURN, "Cliquez pour mettre les détails de tour dans le formulaire du nouveau tour");
        es(CLICK_TO_PUT_THE_TURN, "Haz clic para poner los detalles del turno en el formulario del nuevo turno");
        de(CLICK_TO_PUT_THE_TURN, "Klicken, um die Zugdetails in das Formular für den neuen Zug zu übernehmen");
        ko(CLICK_TO_PUT_THE_TURN, "클릭하면 턴 상세 정보가 새 턴 입력 창에 들어갑니다");

        ja(SCENE_SETTING, "シーン設定");
        zh(SCENE_SETTING, "场景设定");
        fr(SCENE_SETTING, "Cadre de la scène");
        es(SCENE_SETTING, "Ambientación de la escena");
        de(SCENE_SETTING, "Schauplatz der Szene");
        ko(SCENE_SETTING, "장면 배경");

        ja(POV_CHARACTER, "視点キャラクター");
        zh(POV_CHARACTER, "视角角色");
        fr(POV_CHARACTER, "Personnage du point de vue");
        es(POV_CHARACTER, "Personaje del punto de vista");
        de(POV_CHARACTER, "POV-Figur");
        ko(POV_CHARACTER, "시점 인물");

        ja(PRESENT_CHARACTERS, "登場キャラクター");
        zh(PRESENT_CHARACTERS, "在场角色");
        fr(PRESENT_CHARACTERS, "Personnages présents");
        es(PRESENT_CHARACTERS, "Personajes presentes");
        de(PRESENT_CHARACTERS, "Anwesende Figuren");
        ko(PRESENT_CHARACTERS, "등장인물");

        ja(INSTRUCTIONS, "指示");
        zh(INSTRUCTIONS, "指令");
        fr(INSTRUCTIONS, "Instructions");
        es(INSTRUCTIONS, "Instrucciones");
        de(INSTRUCTIONS, "Anweisungen");
        ko(INSTRUCTIONS, "지시문");

        ja(REMOVE_THE_CHAT_AND_THE_TURN, "チャットと、そこから作成したターン詳細をブックから削除しますか？");
        zh(REMOVE_THE_CHAT_AND_THE_TURN, "要从书籍中移除该聊天记录及据此生成的回合详情吗？");
        fr(REMOVE_THE_CHAT_AND_THE_TURN, "Retirer du livre la discussion et les détails de tour qui en ont été tirés ?");
        es(REMOVE_THE_CHAT_AND_THE_TURN, "¿Quitar del libro el chat y los detalles de turno hechos a partir de él?");
        de(REMOVE_THE_CHAT_AND_THE_TURN, "Den Chat und die daraus erstellten Zugdetails aus dem Buch entfernen?");
        ko(REMOVE_THE_CHAT_AND_THE_TURN, "채팅과 그로 만든 턴 상세 정보를 책에서 제거하시겠습니까?");

        ja(REMOVE_THE_CHAT, "チャットを削除");
        zh(REMOVE_THE_CHAT, "移除聊天记录");
        fr(REMOVE_THE_CHAT, "Retirer la discussion");
        es(REMOVE_THE_CHAT, "Quitar el chat");
        de(REMOVE_THE_CHAT, "Den Chat entfernen");
        ko(REMOVE_THE_CHAT, "채팅 제거");

        ja(CHAT_LOWERCASE, "チャット");
        zh(CHAT_LOWERCASE, "聊天");
        fr(CHAT_LOWERCASE, "discussion");
        es(CHAT_LOWERCASE, "chat");
        de(CHAT_LOWERCASE, "Chat");
        ko(CHAT_LOWERCASE, "채팅");

        ja(MESSAGES_CHECKED, "%d件のメッセージ、%d件確認済み");
        zh(MESSAGES_CHECKED, "%d 条消息，已检查 %d 条");
        fr(MESSAGES_CHECKED, "%d messages, %d cochés");
        es(MESSAGES_CHECKED, "%d mensajes, %d marcados");
        de(MESSAGES_CHECKED, "%d Nachrichten, %d markiert");
        ko(MESSAGES_CHECKED, "메시지 %d개, %d개 체크됨");

        ja(MESSAGES_LOADED, "%d件のメッセージを読み込みました");
        zh(MESSAGES_LOADED, "已加载 %d 条消息");
        fr(MESSAGES_LOADED, "%d messages chargés");
        es(MESSAGES_LOADED, "%d mensajes cargados");
        de(MESSAGES_LOADED, "%d Nachrichten geladen");
        ko(MESSAGES_LOADED, "메시지 %d개를 불러왔습니다");

        ja(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "SillyTavernまたはLM Studioのチャットではありません");
        zh(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "不是 SillyTavern 或 LM Studio 聊天记录");
        fr(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "Ce n'est pas une discussion SillyTavern ou LM Studio");
        es(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "No es un chat de SillyTavern ni de LM Studio");
        de(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "Kein SillyTavern- oder LM-Studio-Chat");
        ko(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT, "SillyTavern 또는 LM Studio 채팅이 아닙니다");

        ja(THE_CHAT_HAS_NO_MESSAGES, "チャットにメッセージがありません");
        zh(THE_CHAT_HAS_NO_MESSAGES, "聊天记录中没有消息");
        fr(THE_CHAT_HAS_NO_MESSAGES, "La discussion n'a pas de messages");
        es(THE_CHAT_HAS_NO_MESSAGES, "El chat no tiene mensajes");
        de(THE_CHAT_HAS_NO_MESSAGES, "Der Chat hat keine Nachrichten");
        ko(THE_CHAT_HAS_NO_MESSAGES, "채팅에 메시지가 없습니다");

        ja(GO_TO_THE_NEXT_UNCHECKED, "次の未確認の行へ移動");
        zh(GO_TO_THE_NEXT_UNCHECKED, "跳到下一个未检查的行");
        fr(GO_TO_THE_NEXT_UNCHECKED, "Aller à la prochaine ligne non cochée");
        es(GO_TO_THE_NEXT_UNCHECKED, "Ir a la siguiente fila sin marcar");
        de(GO_TO_THE_NEXT_UNCHECKED, "Zur nächsten nicht markierten Zeile gehen");
        ko(GO_TO_THE_NEXT_UNCHECKED, "체크되지 않은 다음 행으로 이동");

        ja(ALL_MESSAGES_ARE_CHECKED, "すべてのメッセージを確認済みです");
        zh(ALL_MESSAGES_ARE_CHECKED, "所有消息均已检查");
        fr(ALL_MESSAGES_ARE_CHECKED, "Tous les messages sont cochés");
        es(ALL_MESSAGES_ARE_CHECKED, "Todos los mensajes están marcados");
        de(ALL_MESSAGES_ARE_CHECKED, "Alle Nachrichten sind markiert");
        ko(ALL_MESSAGES_ARE_CHECKED, "모든 메시지가 체크되었습니다");

        ja(THERE_ARE_NO_TURN_DETAILS, "ターン詳細がありません");
        zh(THERE_ARE_NO_TURN_DETAILS, "没有回合详情");
        fr(THERE_ARE_NO_TURN_DETAILS, "Il n'y a pas de détails de tour");
        es(THERE_ARE_NO_TURN_DETAILS, "No hay detalles de turno");
        de(THERE_ARE_NO_TURN_DETAILS, "Es gibt keine Zugdetails");
        ko(THERE_ARE_NO_TURN_DETAILS, "턴 상세 정보가 없습니다");

        ja(ALL_TURNS_ARE_CHECKED, "すべてのターンを確認済みです");
        zh(ALL_TURNS_ARE_CHECKED, "所有回合均已检查");
        fr(ALL_TURNS_ARE_CHECKED, "Tous les tours sont cochés");
        es(ALL_TURNS_ARE_CHECKED, "Todos los turnos están marcados");
        de(ALL_TURNS_ARE_CHECKED, "Alle Züge sind markiert");
        ko(ALL_TURNS_ARE_CHECKED, "모든 턴이 체크되었습니다");

        ja(NO_TURN_DETAILS_YET_CHOOSE, "ターン詳細はまだありません。範囲を選んでワンドを使ってください。");
        zh(NO_TURN_DETAILS_YET_CHOOSE, "还没有回合详情：请选择范围并使用魔杖。");
        fr(NO_TURN_DETAILS_YET_CHOOSE, "Pas encore de détails de tour : choisissez une plage et utilisez la baguette.");
        es(NO_TURN_DETAILS_YET_CHOOSE, "Todavía no hay detalles de turno: elige un intervalo y usa la varita.");
        de(NO_TURN_DETAILS_YET_CHOOSE, "Noch keine Zugdetails: Wähle einen Bereich und verwende den Zauberstab.");
        ko(NO_TURN_DETAILS_YET_CHOOSE, "아직 턴 상세 정보가 없습니다: 범위를 선택하고 마법봉을 사용하세요.");

        ja(TURNS_CHECKED_CLICK_ONE, "%d個のターン、%d個確認済み。クリックすると新規ターンフォームで開きます。");
        zh(TURNS_CHECKED_CLICK_ONE, "%d 个回合，已检查 %d 个。点击其中一个可在新回合表单中打开。");
        fr(TURNS_CHECKED_CLICK_ONE, "%d tours, %d cochés. Cliquez sur l'un d'eux pour l'ouvrir dans le formulaire du nouveau tour.");
        es(TURNS_CHECKED_CLICK_ONE, "%d turnos, %d marcados. Haz clic en uno para abrirlo en el formulario del nuevo turno.");
        de(TURNS_CHECKED_CLICK_ONE, "%d Züge, %d markiert. Klicke auf einen, um ihn im Formular für den neuen Zug zu öffnen.");
        ko(TURNS_CHECKED_CLICK_ONE, "턴 %d개, %d개 체크됨. 클릭하면 새 턴 입력 창에서 엽니다.");

        ja(TURN_DETAILS_D, "ターン詳細（%d）");
        zh(TURN_DETAILS_D, "回合详情（%d）");
        fr(TURN_DETAILS_D, "Détails de tour (%d)");
        es(TURN_DETAILS_D, "Detalles del turno (%d)");
        de(TURN_DETAILS_D, "Zugdetails (%d)");
        ko(TURN_DETAILS_D, "턴 상세 정보 (%d)");

        ja(MESSAGES_ABOUT_TOKENS_CHAT, "%d件のメッセージ: 約%sトークン（チャット %s + プロンプト %s）");
        zh(MESSAGES_ABOUT_TOKENS_CHAT, "%d 条消息：约 %s 个词元（聊天 %s + 提示词 %s）");
        fr(MESSAGES_ABOUT_TOKENS_CHAT, "%d messages : environ %s jetons (discussion %s + prompt %s)");
        es(MESSAGES_ABOUT_TOKENS_CHAT, "%d mensajes: unos %s tokens (chat %s + prompt %s)");
        de(MESSAGES_ABOUT_TOKENS_CHAT, "%d Nachrichten: etwa %s Tokens (Chat %s + Prompt %s)");
        ko(MESSAGES_ABOUT_TOKENS_CHAT, "메시지 %d개: 약 %s 토큰 (채팅 %s + 프롬프트 %s)");

        ja(REQUEST_ABOUT_TOKENS_IN_ALL, "%d件のリクエスト、合計約%sトークン");
        zh(REQUEST_ABOUT_TOKENS_IN_ALL, "%d 个请求，共约 %s 个词元");
        fr(REQUEST_ABOUT_TOKENS_IN_ALL, "%d requête, environ %s jetons au total");
        es(REQUEST_ABOUT_TOKENS_IN_ALL, "%d solicitud, unos %s tokens en total");
        de(REQUEST_ABOUT_TOKENS_IN_ALL, "%d Anfrage, insgesamt etwa %s Tokens");
        ko(REQUEST_ABOUT_TOKENS_IN_ALL, "요청 %d회, 총 약 %s 토큰");

        ja(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d件のリクエスト、合計約%sトークン");
        zh(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d 个请求，共约 %s 个词元");
        fr(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d requêtes, environ %s jetons au total");
        es(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d solicitudes, unos %s tokens en total");
        de(REQUESTS_ABOUT_TOKENS_IN_ALL, "%d Anfragen, insgesamt etwa %s Tokens");
        ko(REQUESTS_ABOUT_TOKENS_IN_ALL, "요청 %d회, 총 약 %s 토큰");

        ja(LOAD_A_CHAT_FIRST, "先にチャットを読み込んでください");
        zh(LOAD_A_CHAT_FIRST, "请先加载聊天记录");
        fr(LOAD_A_CHAT_FIRST, "Chargez d'abord une discussion");
        es(LOAD_A_CHAT_FIRST, "Carga primero un chat");
        de(LOAD_A_CHAT_FIRST, "Lade zuerst einen Chat");
        ko(LOAD_A_CHAT_FIRST, "먼저 채팅을 불러오세요");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");
        fr(CHOOSE_A_MODEL, "Choisissez un modèle");
        es(CHOOSE_A_MODEL, "Elige un modelo");
        de(CHOOSE_A_MODEL, "Wähle ein Modell");
        ko(CHOOSE_A_MODEL, "모델 선택");

        ja(WAITING_FOR_THE_MODEL, "モデルの応答を待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");
        fr(WAITING_FOR_THE_MODEL, "En attente du modèle...");
        es(WAITING_FOR_THE_MODEL, "Esperando al modelo...");
        de(WAITING_FOR_THE_MODEL, "Warte auf das Modell ...");
        ko(WAITING_FOR_THE_MODEL, "모델을 기다리는 중...");

        ja(CANT_CONVERT, "変換できません: %s");
        zh(CANT_CONVERT, "无法转换：%s");
        fr(CANT_CONVERT, "Impossible de convertir : %s");
        es(CANT_CONVERT, "No se puede convertir: %s");
        de(CANT_CONVERT, "Konvertierung nicht möglich: %s");
        ko(CANT_CONVERT, "변환할 수 없습니다: %s");

        ja(BATCH, "バッチ %d/%d: %s");
        zh(BATCH, "第 %d/%d 批：%s");
        fr(BATCH, "Lot %d sur %d : %s");
        es(BATCH, "Lote %d de %d: %s");
        de(BATCH, "Stapel %d von %d: %s");
        ko(BATCH, "묶음 %d / %d: %s");

        ja(BATCH_OF_TRY, "バッチ %d/%d（試行 %d/%d）: %s");
        zh(BATCH_OF_TRY, "第 %d/%d 批（第 %d/%d 次尝试）：%s");
        fr(BATCH_OF_TRY, "Lot %d sur %d (essai %d sur %d) : %s");
        es(BATCH_OF_TRY, "Lote %d de %d (intento %d de %d): %s");
        de(BATCH_OF_TRY, "Stapel %d von %d (Versuch %d von %d): %s");
        ko(BATCH_OF_TRY, "묶음 %d / %d (시도 %d / %d): %s");

        ja(WRITING_THE_TURN_DETAILS_SO, "ターン詳細を書き込み中... 現在%d件");
        zh(WRITING_THE_TURN_DETAILS_SO, "正在写入回合详情……目前 %d 个");
        fr(WRITING_THE_TURN_DETAILS_SO, "Écriture des détails de tour... %d jusqu'ici");
        es(WRITING_THE_TURN_DETAILS_SO, "Escribiendo los detalles de turno... %d hasta ahora");
        de(WRITING_THE_TURN_DETAILS_SO, "Die Zugdetails werden geschrieben ... bisher %d");
        ko(WRITING_THE_TURN_DETAILS_SO, "턴 상세 정보를 쓰는 중... 지금까지 %d개");

        ja(THE_MODEL_IS_THINKING, "モデルが思考中です...");
        zh(THE_MODEL_IS_THINKING, "模型正在思考……");
        fr(THE_MODEL_IS_THINKING, "Le modèle réfléchit...");
        es(THE_MODEL_IS_THINKING, "El modelo está pensando...");
        de(THE_MODEL_IS_THINKING, "Das Modell denkt nach ...");
        ko(THE_MODEL_IS_THINKING, "모델이 생각하는 중...");

        ja(CANT_GO, "続行できません: %s");
        zh(CANT_GO, "无法继续：%s");
        fr(CANT_GO, "Impossible de continuer : %s");
        es(CANT_GO, "No se puede continuar: %s");
        de(CANT_GO, "Es kann nicht weitergehen: %s");
        ko(CANT_GO, "계속할 수 없습니다: %s");

        ja(STOPPED_NOTHING_WAS_CHANGED, "停止しました。何も変更されていません");
        zh(STOPPED_NOTHING_WAS_CHANGED, "已停止，未做任何更改");
        fr(STOPPED_NOTHING_WAS_CHANGED, "Arrêté, rien n'a été modifié");
        es(STOPPED_NOTHING_WAS_CHANGED, "Detenido, no se ha cambiado nada");
        de(STOPPED_NOTHING_WAS_CHANGED, "Gestoppt, nichts wurde geändert");
        ko(STOPPED_NOTHING_WAS_CHANGED, "중지됨, 변경된 것 없음");

        ja(THE_MODEL_DID_NOT_ANSWER, "モデルから応答がありませんでした。");
        zh(THE_MODEL_DID_NOT_ANSWER, "模型没有回答。");
        fr(THE_MODEL_DID_NOT_ANSWER, "Le modèle n'a pas répondu.");
        es(THE_MODEL_DID_NOT_ANSWER, "El modelo no ha respondido.");
        de(THE_MODEL_DID_NOT_ANSWER, "Das Modell hat nicht geantwortet.");
        ko(THE_MODEL_DID_NOT_ANSWER, "모델이 응답하지 않았습니다.");

        ja(TURNS_FOR_THE_MESSAGES, "メッセージ%2$d〜%3$dに対する%1$d個のターン");
        zh(TURNS_FOR_THE_MESSAGES, "针对第 %2$d-%3$d 条消息生成了 %1$d 个回合");
        fr(TURNS_FOR_THE_MESSAGES, "%1$d tours pour les messages %2$d-%3$d");
        es(TURNS_FOR_THE_MESSAGES, "%1$d turnos para los mensajes %2$d-%3$d");
        de(TURNS_FOR_THE_MESSAGES, "%1$d Züge für die Nachrichten %2$d–%3$d");
        ko(TURNS_FOR_THE_MESSAGES, "메시지 %2$d-%3$d에 대한 턴 %1$d개");

        ja(EARLIER_ONES_REPLACED, "（以前の%d件を置き換えました）");
        zh(EARLIER_ONES_REPLACED, "（替换了之前的 %d 个）");
        fr(EARLIER_ONES_REPLACED, " (%d précédents remplacés)");
        es(EARLIER_ONES_REPLACED, " (%d anteriores reemplazados)");
        de(EARLIER_ONES_REPLACED, " (%d frühere ersetzt)");
        ko(EARLIER_ONES_REPLACED, " (이전 %d개 교체됨)");

        ja(CONVERT_THE_MESSAGES_IN_REQUESTS, "メッセージ%d〜%dを%d件のリクエストでモデルに順番に送って変換しますか？範囲内の以前のターン詳細のうち、チェックを付けていないものは置き換えられます。");
        zh(CONVERT_THE_MESSAGES_IN_REQUESTS, "要将第 %d-%d 条消息分 %d 个请求依次发送给模型进行转换吗？该范围内之前未勾选的回合详情将被替换。");
        fr(CONVERT_THE_MESSAGES_IN_REQUESTS, "Convertir les messages %d-%d en %d requêtes au modèle, l'une après l'autre ? Les détails de tour précédents de la plage que vous n'avez pas cochés sont remplacés.");
        es(CONVERT_THE_MESSAGES_IN_REQUESTS, "¿Convertir los mensajes %d-%d en %d solicitudes al modelo, una tras otra? Los detalles de turno anteriores del intervalo que no has marcado se reemplazan.");
        de(CONVERT_THE_MESSAGES_IN_REQUESTS, "Die Nachrichten %d–%d in %d Anfragen an das Modell konvertieren, eine nach der anderen? Die früheren Zugdetails des Bereichs, die du nicht markiert hast, werden ersetzt.");
        ko(CONVERT_THE_MESSAGES_IN_REQUESTS, "메시지 %d-%d를 모델에 요청 %d회로 차례로 변환하시겠습니까? 범위에 있는 이전 턴 상세 정보 중 체크하지 않은 것은 교체됩니다.");

        ja(TURNS_MADE_IN_REQUESTS, "%2$d件のリクエストで%1$d個のターンを作成しました");
        zh(TURNS_MADE_IN_REQUESTS, "通过 %2$d 个请求生成了 %1$d 个回合");
        fr(TURNS_MADE_IN_REQUESTS, "%1$d tours faits en %2$d requêtes");
        es(TURNS_MADE_IN_REQUESTS, "%1$d turnos hechos en %2$d solicitudes");
        de(TURNS_MADE_IN_REQUESTS, "%1$d Züge in %2$d Anfragen erstellt");
        ko(TURNS_MADE_IN_REQUESTS, "요청 %2$d회로 턴 %1$d개를 만들었습니다");

        ja(STOPPED_AFTER_OF_REQUESTS_TURNS, "%d/%d件のリクエストの後で停止しました（%d個のターンを作成）");
        zh(STOPPED_AFTER_OF_REQUESTS_TURNS, "在 %d/%d 个请求后停止（已生成 %d 个回合）");
        fr(STOPPED_AFTER_OF_REQUESTS_TURNS, "Arrêté après %d requêtes sur %d (%d tours faits)");
        es(STOPPED_AFTER_OF_REQUESTS_TURNS, "Detenido tras %d de %d solicitudes (%d turnos hechos)");
        de(STOPPED_AFTER_OF_REQUESTS_TURNS, "Gestoppt nach %d von %d Anfragen (%d Züge erstellt)");
        ko(STOPPED_AFTER_OF_REQUESTS_TURNS, "요청 %d / %d회 후 중지됨 (턴 %d개 생성)");

        ja(THE_RANGE_IS_WHAT, "。範囲は残りの部分になっています。");
        zh(THE_RANGE_IS_WHAT, "。范围为剩余部分。");
        fr(THE_RANGE_IS_WHAT, ". La plage est ce qui reste.");
        es(THE_RANGE_IS_WHAT, ". El intervalo es lo que queda.");
        de(THE_RANGE_IS_WHAT, ". Der Bereich ist das, was übrig ist.");
        ko(THE_RANGE_IS_WHAT, ". 범위는 남은 부분입니다.");

        ja(PROMPT_OF_THE_WAND, "ワンドのプロンプト");
        zh(PROMPT_OF_THE_WAND, "魔杖的提示词");
        fr(PROMPT_OF_THE_WAND, "Prompt de la baguette");
        es(PROMPT_OF_THE_WAND, "Prompt de la varita");
        de(PROMPT_OF_THE_WAND, "Prompt des Zauberstabs");
        ko(PROMPT_OF_THE_WAND, "마법봉의 프롬프트");

        ja(VARIABLES_BACKGROUNDLORE_CHAT, "変数: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        zh(VARIABLES_BACKGROUNDLORE_CHAT, "变量：{{backgroundLore}}、{{chat}}、{{firstMessage}}、{{lastMessage}}、{{previousTurn}}");
        fr(VARIABLES_BACKGROUNDLORE_CHAT, "Variables : {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        es(VARIABLES_BACKGROUNDLORE_CHAT, "Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        de(VARIABLES_BACKGROUNDLORE_CHAT, "Variablen: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        ko(VARIABLES_BACKGROUNDLORE_CHAT, "변수: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");

        ja(DEFAULT, "デフォルト");
        zh(DEFAULT, "默认");
        fr(DEFAULT, "Par défaut");
        es(DEFAULT, "Predeterminado");
        de(DEFAULT, "Standard");
        ko(DEFAULT, "기본값");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");
        ko(STOP, "중지");

        ja(THE_MODELS_ANSWER_DOES, "モデルの回答が依頼内容と一致しません: %s");
        zh(THE_MODELS_ANSWER_DOES, "模型的回答与要求不符：%s");
        fr(THE_MODELS_ANSWER_DOES, "La réponse du modèle ne correspond pas à ce qui était demandé : %s");
        es(THE_MODELS_ANSWER_DOES, "La respuesta del modelo no coincide con lo que se pidió: %s");
        de(THE_MODELS_ANSWER_DOES, "Die Antwort des Modells entspricht nicht dem, was verlangt wurde: %s");
        ko(THE_MODELS_ANSWER_DOES, "모델의 응답이 요청한 것과 일치하지 않습니다: %s");

        ja(THE_MODELS_ANSWER_HAS_NO, "モデルの回答に、メッセージ%d〜%dのターンがありません");
        zh(THE_MODELS_ANSWER_HAS_NO, "模型的回答中没有第 %d 至 %d 条消息对应的回合");
        fr(THE_MODELS_ANSWER_HAS_NO, "La réponse du modèle n'a pas de tour pour les messages %d à %d");
        es(THE_MODELS_ANSWER_HAS_NO, "La respuesta del modelo no tiene ningún turno para los mensajes %d a %d");
        de(THE_MODELS_ANSWER_HAS_NO, "Die Antwort des Modells enthält keinen Zug für die Nachrichten %d bis %d");
        ko(THE_MODELS_ANSWER_HAS_NO, "모델의 응답에 메시지 %d~%d에 대한 턴이 없습니다");

        ja(UPLOAD_A_CHAT, "チャットをアップロード");
        zh(UPLOAD_A_CHAT, "上传聊天记录");
        fr(UPLOAD_A_CHAT, "Téléverser une discussion");
        es(UPLOAD_A_CHAT, "Subir un chat");
        de(UPLOAD_A_CHAT, "Einen Chat hochladen");
        ko(UPLOAD_A_CHAT, "채팅 업로드");

        ja(LOAD_A_BACKUP, "バックアップを読み込む");
        zh(LOAD_A_BACKUP, "载入备份");
        fr(LOAD_A_BACKUP, "Charger une sauvegarde");
        es(LOAD_A_BACKUP, "Cargar una copia de seguridad");
        de(LOAD_A_BACKUP, "Eine Sicherung laden");
        ko(LOAD_A_BACKUP, "백업 불러오기");

        ja(PICK_A_BOOK, "ブックを選ぶ");
        zh(PICK_A_BOOK, "选择书籍");
        fr(PICK_A_BOOK, "Choisir un livre");
        es(PICK_A_BOOK, "Elegir un libro");
        de(PICK_A_BOOK, "Ein Buch wählen");
        ko(PICK_A_BOOK, "책 선택");

        ja(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "SillyTavernのチャット（.jsonl）またはLM Studioの会話（.conversation.json）。分岐なし。モデルとともにストーリーとして書き直します。");
        zh(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "SillyTavern 聊天记录（.jsonl）或 LM Studio 对话（.conversation.json），不含分支，与模型一起改写为故事。");
        fr(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "Une discussion SillyTavern (.jsonl) ou une conversation LM Studio (.conversation.json), sans branches, à réécrire en histoire avec le modèle.");
        es(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "Un chat de SillyTavern (.jsonl) o una conversación de LM Studio (.conversation.json), sin ramas, para reescribirlo como historia con el modelo.");
        de(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "Ein SillyTavern-Chat (.jsonl) oder eine LM-Studio-Unterhaltung (.conversation.json), ohne Zweige, die mit dem Modell als Geschichte umgeschrieben werden soll.");
        ko(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO, "모델이 이야기로 다시 쓸 SillyTavern 채팅(.jsonl) 또는 LM Studio 대화(.conversation.json). 분기는 없어야 합니다.");

        ja(OR_THE_PARTS, "または、バックアップファイルまたはブックそのものから、あなたのブックのパート（その指示と、書き直す本文）。");
        zh(OR_THE_PARTS, "或者你的某本书的各个部分，来自备份文件或书籍本身：它们的指令，以及要重写的文本。");
        fr(OR_THE_PARTS, "Ou les parties d'un de vos livres, depuis un fichier de sauvegarde ou le livre lui-même : leurs instructions, et le texte à réécrire.");
        es(OR_THE_PARTS, "O las partes de un libro tuyo, de un archivo de copia de seguridad o del propio libro: sus instrucciones y el texto que reescribir.");
        de(OR_THE_PARTS, "Oder die Abschnitte eines deiner Bücher, aus einer Sicherungsdatei oder dem Buch selbst: ihre Anweisungen und der zu überarbeitende Text.");
        ko(OR_THE_PARTS, "또는 내 책의 파트: 백업 파일이나 책 자체에서, 지시문과 재작성할 텍스트.");

        ja(ONLY_THE_INSTRUCTIONS, "このブックには指示と本文だけが保存されます。別のものを読み込むには、まずソースを削除してください。");
        zh(ONLY_THE_INSTRUCTIONS, "本书只保存指令和文本。要载入其他来源，请先移除当前来源。");
        fr(ONLY_THE_INSTRUCTIONS, "Seuls les instructions et le texte sont gardés avec ce livre. Retirez la source pour en charger une autre.");
        es(ONLY_THE_INSTRUCTIONS, "Con este libro solo se conservan las instrucciones y el texto. Quita el origen para cargar otro.");
        de(ONLY_THE_INSTRUCTIONS, "Nur die Anweisungen und der Text werden mit diesem Buch aufbewahrt. Entferne die Quelle, um eine andere zu laden.");
        ko(ONLY_THE_INSTRUCTIONS, "이 책에는 지시문과 텍스트만 보관됩니다. 다른 것을 불러오려면 소스를 제거하세요.");

        ja(PARTS_LOADED, "%d個のパートを読み込みました");
        zh(PARTS_LOADED, "已载入 %d 个部分");
        fr(PARTS_LOADED, "%d parties chargées");
        es(PARTS_LOADED, "%d partes cargadas");
        de(PARTS_LOADED, "%d Abschnitte geladen");
        ko(PARTS_LOADED, "파트 %d개를 불러왔습니다");

        ja(NOT_A_BACKUP, "ブックのバックアップではありません");
        zh(NOT_A_BACKUP, "不是书籍备份");
        fr(NOT_A_BACKUP, "Ce n'est pas la sauvegarde d'un livre");
        es(NOT_A_BACKUP, "No es una copia de seguridad de un libro");
        de(NOT_A_BACKUP, "Keine Sicherung eines Buchs");
        ko(NOT_A_BACKUP, "책의 백업이 아닙니다");

        ja(BOOK, "ブック");
        zh(BOOK, "书籍");
        fr(BOOK, "Livre");
        es(BOOK, "Libro");
        de(BOOK, "Buch");
        ko(BOOK, "책");

        ja(LOAD, "読み込む");
        zh(LOAD, "载入");
        fr(LOAD, "Charger");
        es(LOAD, "Cargar");
        de(LOAD, "Laden");
        ko(LOAD, "불러오기");

        ja(THE_PARTS_OF_THE_BRANCH, "ブックが現在いるブランチのパートが読み込まれます。");
        zh(THE_PARTS_OF_THE_BRANCH, "将载入书籍当前所在分支的各个部分。");
        fr(THE_PARTS_OF_THE_BRANCH, "Les parties de la branche sur laquelle est le livre sont chargées.");
        es(THE_PARTS_OF_THE_BRANCH, "Se cargan las partes de la rama en la que está el libro.");
        de(THE_PARTS_OF_THE_BRANCH, "Die Abschnitte des Zweigs, auf dem das Buch steht, werden geladen.");
        ko(THE_PARTS_OF_THE_BRANCH, "책이 있는 분기의 파트를 불러옵니다.");

        ja(PARTS_CHECKED, "%d個のパート、%d個チェック済み");
        zh(PARTS_CHECKED, "%d 个部分，已勾选 %d 个");
        fr(PARTS_CHECKED, "%d parties, %d cochées");
        es(PARTS_CHECKED, "%d partes, %d marcadas");
        de(PARTS_CHECKED, "%d Abschnitte, %d markiert");
        ko(PARTS_CHECKED, "파트 %d개, %d개 체크됨");

        ja(PARTS, "パート");
        zh(PARTS, "部分");
        fr(PARTS, "Parties");
        es(PARTS, "Partes");
        de(PARTS, "Abschnitte");
        ko(PARTS, "파트");

        ja(REMOVE_THE_SOURCE, "ソースを削除");
        zh(REMOVE_THE_SOURCE, "移除来源");
        fr(REMOVE_THE_SOURCE, "Retirer la source");
        es(REMOVE_THE_SOURCE, "Quitar el origen");
        de(REMOVE_THE_SOURCE, "Die Quelle entfernen");
        ko(REMOVE_THE_SOURCE, "소스 제거");

        ja(REMOVE_THE_SOURCE_AND_WHAT, "ソースと、そのチェック内容をこのブックから削除しますか？");
        zh(REMOVE_THE_SOURCE_AND_WHAT, "要从本书中移除该来源及其勾选记录吗？");
        fr(REMOVE_THE_SOURCE_AND_WHAT, "Retirer du livre la source et ce que vous en avez coché ?");
        es(REMOVE_THE_SOURCE_AND_WHAT, "¿Quitar del libro el origen y lo que marcaste de él?");
        de(REMOVE_THE_SOURCE_AND_WHAT, "Die Quelle und das, was du davon markiert hast, aus dem Buch entfernen?");
        ko(REMOVE_THE_SOURCE_AND_WHAT, "소스와 그중 체크한 것을 책에서 제거하시겠습니까?");

        ja(THE_BOOK_HAS_NO_PARTS, "読み込めるパートがありません");
        zh(THE_BOOK_HAS_NO_PARTS, "该书没有可载入的部分");
        fr(THE_BOOK_HAS_NO_PARTS, "Le livre n'a pas de parties à charger");
        es(THE_BOOK_HAS_NO_PARTS, "El libro no tiene partes que cargar");
        de(THE_BOOK_HAS_NO_PARTS, "Das Buch hat keine Abschnitte zum Laden");
        ko(THE_BOOK_HAS_NO_PARTS, "책에 불러올 파트가 없습니다");

        ja(THE_BACKUP_IS_DAMAGED, "バックアップが壊れています: パートが最初のパートにつながっていません");
        zh(THE_BACKUP_IS_DAMAGED, "备份已损坏：各部分无法追溯到第一个部分");
        fr(THE_BACKUP_IS_DAMAGED, "La sauvegarde est endommagée : les parties ne mènent pas à la première partie");
        es(THE_BACKUP_IS_DAMAGED, "La copia de seguridad está dañada: las partes no llevan a la primera parte");
        de(THE_BACKUP_IS_DAMAGED, "Die Sicherung ist beschädigt: Die Abschnitte führen nicht zum ersten Abschnitt");
        ko(THE_BACKUP_IS_DAMAGED, "백업이 손상되었습니다: 파트가 첫 파트로 이어지지 않습니다");

        ja(THE_STORED_SOURCE, "保存されているブックのソースを読み取れません");
        zh(THE_STORED_SOURCE, "无法读取已保存的书籍来源");
        fr(THE_STORED_SOURCE, "La source enregistrée du livre ne peut pas être lue");
        es(THE_STORED_SOURCE, "No se puede leer el origen guardado del libro");
        de(THE_STORED_SOURCE, "Die gespeicherte Quelle des Buchs kann nicht gelesen werden");
        ko(THE_STORED_SOURCE, "책에 저장된 소스를 읽을 수 없습니다");

        ja(THE_STORED_SOURCE_OF_THE_BOOK, "保存されているブックのソースは、より新しいバージョンのプラグインのものです");
        zh(THE_STORED_SOURCE_OF_THE_BOOK, "已保存的书籍来源来自更新版本的插件");
        fr(THE_STORED_SOURCE_OF_THE_BOOK, "La source enregistrée du livre provient d'une version plus récente du plugin");
        es(THE_STORED_SOURCE_OF_THE_BOOK, "El origen guardado del libro es de una versión más reciente del plugin");
        de(THE_STORED_SOURCE_OF_THE_BOOK, "Die gespeicherte Quelle des Buchs stammt aus einer neueren Version des Plugins");
        ko(THE_STORED_SOURCE_OF_THE_BOOK, "책에 저장된 소스가 더 새로운 버전의 플러그인에서 만들어졌습니다");

        ja(CONVERTER_CONVERT_THE_CHAT, "コンバーター: チャットをターン詳細に変換");
        zh(CONVERTER_CONVERT_THE_CHAT, "转换器：将聊天转换为回合详情");
        fr(CONVERTER_CONVERT_THE_CHAT, "Convertisseur : convertir la discussion en détails de tour");
        es(CONVERTER_CONVERT_THE_CHAT, "Conversor: convertir el chat en detalles de turno");
        de(CONVERTER_CONVERT_THE_CHAT, "Konverter: den Chat in Zugdetails konvertieren");
        ko(CONVERTER_CONVERT_THE_CHAT, "변환기: 채팅을 턴 상세 정보로 변환");
    }
}
