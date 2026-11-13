package com.github.enerccio.marginalia.extensions.reviewer;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Reviewer in the languages besides English; the English text is the key.
 */
public class ReviewerLocalization extends PluginLocalizationBundle {

    public static final ReviewerLocalization T = new ReviewerLocalization();

    public static final String REVIEW = "Review";
    public static final String VIEW_GENERATE_REVIEW = "View / Generate Review";
    public static final String REVIEW_ADVANCED_OPTIONS = "Review: Advanced Options";
    public static final String ADVANCED_OPTIONS = "Advanced Options";
    public static final String DELETE_REVIEW = "Delete Review";
    public static final String REVIEWER_SETTINGS = "Reviewer Settings";
    public static final String REVIEWER_PROFILE_DEFAULT_SELECTED = "Reviewer Profile (Default: selected in settings)";
    public static final String MESSAGE_REVIEW = "Message Review";
    public static final String STOP_GENERATING = "Stop generating";
    public static final String THINKING_PROCESS = "Thinking Process";
    public static final String CLOSE = "Close";
    public static final String GENERATING_REVIEW_PLEASE_WAIT = "Generating review, please wait...";
    public static final String ERROR_GENERATING_REVIEW = "Error generating review: %s";
    public static final String FAILED_TO_START_INFERENCE = "Failed to start inference: %s";
    public static final String PROFILE = "Profile";
    public static final String NEW = "New";
    public static final String RENAME = "Rename";
    public static final String DELETE = "Delete";
    public static final String REVIEWER_MODEL_DEFAULT_MANUSCRIPT = "Reviewer Model (Default: Manuscript AI)";
    public static final String REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT = "Reviewer Protocol (Default: Manuscript Protocol)";
    public static final String REVIEW_PRE_PROMPT_SYSTEM = "Review Pre-Prompt (System)";
    public static final String REVIEW_POST_PROMPT_USER = "Review Post-Prompt (User)";
    public static final String NEW_PROFILE = "New Profile";
    public static final String PROFILE_NAME = "Profile Name";
    public static final String CREATE = "Create";
    public static final String PROFILE_NAME_CANNOT_BE_EMPTY = "Profile name cannot be empty";
    public static final String PROFILE_ALREADY_EXISTS = "Profile already exists";
    public static final String CANCEL = "Cancel";
    public static final String CANNOT_RENAME_PROFILE = "Cannot rename %s profile";
    public static final String RENAME_PROFILE = "Rename Profile";
    public static final String NEW_PROFILE_NAME = "New Profile Name";
    public static final String CANNOT_DELETE_PROFILE = "Cannot delete %s profile";
    public static final String ADVANCED_REVIEW_OPTIONS = "Advanced Review Options";
    public static final String PROMPT_FROM_PROFILE = "Prompt from Profile";
    public static final String PROMPT_OVERRIDE = "Prompt Override";
    public static final String USE_STANDARD_PROMPT_INFO = "Use standard prompt info";
    public static final String INCLUDE_LOREBOOK = "Include Lorebook";
    public static final String WITH_SUMMARIES = "With summaries";
    public static final String SEND_THE_SUMMARIES = "Send the summaries of the story before the part, then the parts after the newest summary, as a generation does, instead of as many of the latest parts as fit";
    public static final String SEND_ORIGINAL_TEXT = "Send original text";
    public static final String SEND_THE_TEXT_AS_STORED = "Send the text of the story as it is stored, with the speech in the translated or constructed language, instead of the English a model reads";
    public static final String TOKEN_LIMIT = "Token Limit";
    public static final String GENERATE = "Generate";
    public static final String TOTAL_TOKENS = "Total tokens: %s";
    public static final String REVIEWER_GENERATE_A_REVIEW = "Reviewer: generate a review";

    @Override
    protected void loadKeys() {
        ja(REVIEW, "レビュー");
        zh(REVIEW, "评论");
        fr(REVIEW, "Critique");
        es(REVIEW, "Revisión");
        de(REVIEW, "Rezension");
        ko(REVIEW, "검토");
        it(REVIEW, "Rivedi");

        ja(VIEW_GENERATE_REVIEW, "レビューの表示 / 生成");
        zh(VIEW_GENERATE_REVIEW, "查看 / 生成评论");
        fr(VIEW_GENERATE_REVIEW, "Voir / générer la critique");
        es(VIEW_GENERATE_REVIEW, "Ver / generar revisión");
        de(VIEW_GENERATE_REVIEW, "Rezension anzeigen / generieren");
        ko(VIEW_GENERATE_REVIEW, "리뷰 보기 / 생성");
        it(VIEW_GENERATE_REVIEW, "Visualizza / genera la recensione");

        ja(REVIEW_ADVANCED_OPTIONS, "レビュー: 詳細オプション");
        zh(REVIEW_ADVANCED_OPTIONS, "评论：高级选项");
        fr(REVIEW_ADVANCED_OPTIONS, "Critique : options avancées");
        es(REVIEW_ADVANCED_OPTIONS, "Revisión: opciones avanzadas");
        de(REVIEW_ADVANCED_OPTIONS, "Rezension: Erweiterte Optionen");
        ko(REVIEW_ADVANCED_OPTIONS, "리뷰: 고급 옵션");
        it(REVIEW_ADVANCED_OPTIONS, "Recensione: opzioni avanzate");

        ja(ADVANCED_OPTIONS, "詳細オプション");
        zh(ADVANCED_OPTIONS, "高级选项");
        fr(ADVANCED_OPTIONS, "Options avancées");
        es(ADVANCED_OPTIONS, "Opciones avanzadas");
        de(ADVANCED_OPTIONS, "Erweiterte Optionen");
        ko(ADVANCED_OPTIONS, "고급 옵션");
        it(ADVANCED_OPTIONS, "Opzioni avanzate");

        ja(DELETE_REVIEW, "レビューを削除");
        zh(DELETE_REVIEW, "删除评论");
        fr(DELETE_REVIEW, "Supprimer la critique");
        es(DELETE_REVIEW, "Eliminar revisión");
        de(DELETE_REVIEW, "Rezension löschen");
        ko(DELETE_REVIEW, "리뷰 삭제");
        it(DELETE_REVIEW, "Elimina la recensione");

        ja(REVIEWER_SETTINGS, "レビュアー設定");
        zh(REVIEWER_SETTINGS, "评论者设置");
        fr(REVIEWER_SETTINGS, "Paramètres du critique");
        es(REVIEWER_SETTINGS, "Ajustes del revisor");
        de(REVIEWER_SETTINGS, "Einstellungen des Rezensenten");
        ko(REVIEWER_SETTINGS, "리뷰어 설정");
        it(REVIEWER_SETTINGS, "Impostazioni del recensore");

        ja(REVIEWER_PROFILE_DEFAULT_SELECTED, "レビュアープロファイル（デフォルト: 設定で選択したもの）");
        zh(REVIEWER_PROFILE_DEFAULT_SELECTED, "评论者配置文件（默认：在设置中选择的）");
        fr(REVIEWER_PROFILE_DEFAULT_SELECTED, "Profil du critique (par défaut : celui choisi dans les paramètres)");
        es(REVIEWER_PROFILE_DEFAULT_SELECTED, "Perfil del revisor (predeterminado: el seleccionado en los ajustes)");
        de(REVIEWER_PROFILE_DEFAULT_SELECTED, "Rezensentenprofil (Standard: in den Einstellungen gewählt)");
        ko(REVIEWER_PROFILE_DEFAULT_SELECTED, "리뷰어 프로필 (기본값: 설정에서 선택됨)");
        it(REVIEWER_PROFILE_DEFAULT_SELECTED, "Profilo del recensore (predefinito: selezionato nelle impostazioni)");

        ja(MESSAGE_REVIEW, "メッセージのレビュー");
        zh(MESSAGE_REVIEW, "消息评论");
        fr(MESSAGE_REVIEW, "Critique du message");
        es(MESSAGE_REVIEW, "Revisión del mensaje");
        de(MESSAGE_REVIEW, "Rezension der Nachricht");
        ko(MESSAGE_REVIEW, "메시지 리뷰");
        it(MESSAGE_REVIEW, "Recensione del messaggio");

        ja(STOP_GENERATING, "生成を停止");
        zh(STOP_GENERATING, "停止生成");
        fr(STOP_GENERATING, "Arrêter la génération");
        es(STOP_GENERATING, "Detener la generación");
        de(STOP_GENERATING, "Generierung stoppen");
        ko(STOP_GENERATING, "생성 중지");
        it(STOP_GENERATING, "Interrompi la generazione");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");
        fr(THINKING_PROCESS, "Réflexion");
        es(THINKING_PROCESS, "Proceso de razonamiento");
        de(THINKING_PROCESS, "Denkprozess");
        ko(THINKING_PROCESS, "추론 과정");
        it(THINKING_PROCESS, "Processo di ragionamento");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(GENERATING_REVIEW_PLEASE_WAIT, "レビューを生成中です。お待ちください...");
        zh(GENERATING_REVIEW_PLEASE_WAIT, "正在生成评论，请稍候……");
        fr(GENERATING_REVIEW_PLEASE_WAIT, "Génération de la critique, veuillez patienter...");
        es(GENERATING_REVIEW_PLEASE_WAIT, "Generando la revisión, espera un momento...");
        de(GENERATING_REVIEW_PLEASE_WAIT, "Die Rezension wird generiert, bitte warten ...");
        ko(GENERATING_REVIEW_PLEASE_WAIT, "리뷰를 생성하는 중입니다. 잠시 기다려 주세요...");
        it(GENERATING_REVIEW_PLEASE_WAIT, "Generazione della recensione, attendi...");

        ja(ERROR_GENERATING_REVIEW, "レビューの生成中にエラーが発生しました: %s");
        zh(ERROR_GENERATING_REVIEW, "生成评论时出错：%s");
        fr(ERROR_GENERATING_REVIEW, "Erreur lors de la génération de la critique : %s");
        es(ERROR_GENERATING_REVIEW, "Error al generar la revisión: %s");
        de(ERROR_GENERATING_REVIEW, "Fehler beim Generieren der Rezension: %s");
        ko(ERROR_GENERATING_REVIEW, "리뷰 생성 오류: %s");
        it(ERROR_GENERATING_REVIEW, "Errore nella generazione della recensione: %s");

        ja(FAILED_TO_START_INFERENCE, "推論を開始できませんでした: %s");
        zh(FAILED_TO_START_INFERENCE, "无法启动推理：%s");
        fr(FAILED_TO_START_INFERENCE, "Échec du lancement de l'inférence : %s");
        es(FAILED_TO_START_INFERENCE, "No se pudo iniciar la inferencia: %s");
        de(FAILED_TO_START_INFERENCE, "Die Inferenz konnte nicht gestartet werden: %s");
        ko(FAILED_TO_START_INFERENCE, "추론을 시작하지 못했습니다: %s");
        it(FAILED_TO_START_INFERENCE, "Impossibile avviare l'inferenza: %s");

        ja(PROFILE, "プロファイル");
        zh(PROFILE, "配置文件");
        fr(PROFILE, "Profil");
        es(PROFILE, "Perfil");
        de(PROFILE, "Profil");
        ko(PROFILE, "프로필");
        it(PROFILE, "Profilo");

        ja(NEW, "新規");
        zh(NEW, "新建");
        fr(NEW, "Nouveau");
        es(NEW, "Nuevo");
        de(NEW, "Neu");
        ko(NEW, "새로 만들기");
        it(NEW, "Nuovo");

        ja(RENAME, "名前を変更");
        zh(RENAME, "重命名");
        fr(RENAME, "Renommer");
        es(RENAME, "Renombrar");
        de(RENAME, "Umbenennen");
        ko(RENAME, "이름 바꾸기");
        it(RENAME, "Rinomina");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");
        ko(DELETE, "삭제");
        it(DELETE, "Elimina");

        ja(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "レビュアーモデル（デフォルト: 原稿のAI）");
        zh(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "评论者模型（默认：稿件的 AI）");
        fr(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "Modèle du critique (par défaut : celui du manuscrit)");
        es(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "Modelo del revisor (predeterminado: la IA del manuscrito)");
        de(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "Rezensentenmodell (Standard: KI des Manuskripts)");
        ko(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "리뷰어 모델 (기본값: 원고 AI)");
        it(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "Modello del recensore (predefinito: IA del manoscritto)");

        ja(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "レビュアープロトコル（デフォルト: 原稿のプロトコル）");
        zh(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "评论者协议（默认：稿件的协议）");
        fr(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "Protocole du critique (par défaut : celui du manuscrit)");
        es(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "Protocolo del revisor (predeterminado: el protocolo del manuscrito)");
        de(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "Rezensentenprotokoll (Standard: Protokoll des Manuskripts)");
        ko(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "리뷰어 프로토콜 (기본값: 원고 프로토콜)");
        it(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "Protocollo del recensore (predefinito: protocollo del manoscritto)");

        ja(REVIEW_PRE_PROMPT_SYSTEM, "レビューの前置プロンプト（システム）");
        zh(REVIEW_PRE_PROMPT_SYSTEM, "评论前置提示词（系统）");
        fr(REVIEW_PRE_PROMPT_SYSTEM, "Pré-prompt de critique (système)");
        es(REVIEW_PRE_PROMPT_SYSTEM, "Preprompt de la revisión (sistema)");
        de(REVIEW_PRE_PROMPT_SYSTEM, "Rezensions-Vorprompt (System)");
        ko(REVIEW_PRE_PROMPT_SYSTEM, "리뷰 사전 프롬프트 (시스템)");
        it(REVIEW_PRE_PROMPT_SYSTEM, "Pre-prompt della recensione (sistema)");

        ja(REVIEW_POST_PROMPT_USER, "レビューの後置プロンプト（ユーザー）");
        zh(REVIEW_POST_PROMPT_USER, "评论后置提示词（用户）");
        fr(REVIEW_POST_PROMPT_USER, "Post-prompt de critique (utilisateur)");
        es(REVIEW_POST_PROMPT_USER, "Postprompt de la revisión (usuario)");
        de(REVIEW_POST_PROMPT_USER, "Rezensions-Nachprompt (Benutzer)");
        ko(REVIEW_POST_PROMPT_USER, "리뷰 사후 프롬프트 (사용자)");
        it(REVIEW_POST_PROMPT_USER, "Post-prompt della recensione (utente)");

        ja(NEW_PROFILE, "新しいプロファイル");
        zh(NEW_PROFILE, "新建配置文件");
        fr(NEW_PROFILE, "Nouveau profil");
        es(NEW_PROFILE, "Nuevo perfil");
        de(NEW_PROFILE, "Neues Profil");
        ko(NEW_PROFILE, "새 프로필");
        it(NEW_PROFILE, "Nuovo profilo");

        ja(PROFILE_NAME, "プロファイル名");
        zh(PROFILE_NAME, "配置文件名称");
        fr(PROFILE_NAME, "Nom du profil");
        es(PROFILE_NAME, "Nombre del perfil");
        de(PROFILE_NAME, "Profilname");
        ko(PROFILE_NAME, "프로필 이름");
        it(PROFILE_NAME, "Nome del profilo");

        ja(CREATE, "作成");
        zh(CREATE, "创建");
        fr(CREATE, "Créer");
        es(CREATE, "Crear");
        de(CREATE, "Erstellen");
        ko(CREATE, "만들기");
        it(CREATE, "Crea");

        ja(PROFILE_NAME_CANNOT_BE_EMPTY, "プロファイル名は空にできません");
        zh(PROFILE_NAME_CANNOT_BE_EMPTY, "配置文件名称不能为空");
        fr(PROFILE_NAME_CANNOT_BE_EMPTY, "Le nom du profil ne peut pas être vide");
        es(PROFILE_NAME_CANNOT_BE_EMPTY, "El nombre del perfil no puede estar vacío");
        de(PROFILE_NAME_CANNOT_BE_EMPTY, "Der Profilname darf nicht leer sein");
        ko(PROFILE_NAME_CANNOT_BE_EMPTY, "프로필 이름은 비워 둘 수 없습니다");
        it(PROFILE_NAME_CANNOT_BE_EMPTY, "Il nome del profilo non può essere vuoto");

        ja(PROFILE_ALREADY_EXISTS, "プロファイルは既に存在します");
        zh(PROFILE_ALREADY_EXISTS, "配置文件已存在");
        fr(PROFILE_ALREADY_EXISTS, "Le profil existe déjà");
        es(PROFILE_ALREADY_EXISTS, "El perfil ya existe");
        de(PROFILE_ALREADY_EXISTS, "Das Profil existiert bereits");
        ko(PROFILE_ALREADY_EXISTS, "프로필이 이미 있습니다");
        it(PROFILE_ALREADY_EXISTS, "Il profilo esiste già");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");
        it(CANCEL, "Annulla");

        ja(CANNOT_RENAME_PROFILE, "%s プロファイルの名前は変更できません");
        zh(CANNOT_RENAME_PROFILE, "无法重命名 %s 配置文件");
        fr(CANNOT_RENAME_PROFILE, "Impossible de renommer le profil %s");
        es(CANNOT_RENAME_PROFILE, "No se puede renombrar el perfil %s");
        de(CANNOT_RENAME_PROFILE, "Das Profil %s kann nicht umbenannt werden");
        ko(CANNOT_RENAME_PROFILE, "%s 프로필의 이름을 바꿀 수 없습니다");
        it(CANNOT_RENAME_PROFILE, "Impossibile rinominare il profilo %s");

        ja(RENAME_PROFILE, "プロファイル名を変更");
        zh(RENAME_PROFILE, "重命名配置文件");
        fr(RENAME_PROFILE, "Renommer le profil");
        es(RENAME_PROFILE, "Renombrar perfil");
        de(RENAME_PROFILE, "Profil umbenennen");
        ko(RENAME_PROFILE, "프로필 이름 바꾸기");
        it(RENAME_PROFILE, "Rinomina il profilo");

        ja(NEW_PROFILE_NAME, "新しいプロファイル名");
        zh(NEW_PROFILE_NAME, "新配置文件名称");
        fr(NEW_PROFILE_NAME, "Nouveau nom du profil");
        es(NEW_PROFILE_NAME, "Nombre nuevo del perfil");
        de(NEW_PROFILE_NAME, "Neuer Profilname");
        ko(NEW_PROFILE_NAME, "새 프로필 이름");
        it(NEW_PROFILE_NAME, "Nuovo nome del profilo");

        ja(CANNOT_DELETE_PROFILE, "%s プロファイルは削除できません");
        zh(CANNOT_DELETE_PROFILE, "无法删除 %s 配置文件");
        fr(CANNOT_DELETE_PROFILE, "Impossible de supprimer le profil %s");
        es(CANNOT_DELETE_PROFILE, "No se puede eliminar el perfil %s");
        de(CANNOT_DELETE_PROFILE, "Das Profil %s kann nicht gelöscht werden");
        ko(CANNOT_DELETE_PROFILE, "%s 프로필을 삭제할 수 없습니다");
        it(CANNOT_DELETE_PROFILE, "Impossibile eliminare il profilo %s");

        ja(ADVANCED_REVIEW_OPTIONS, "レビューの詳細オプション");
        zh(ADVANCED_REVIEW_OPTIONS, "高级评论选项");
        fr(ADVANCED_REVIEW_OPTIONS, "Options avancées de critique");
        es(ADVANCED_REVIEW_OPTIONS, "Opciones avanzadas de la revisión");
        de(ADVANCED_REVIEW_OPTIONS, "Erweiterte Rezensionsoptionen");
        ko(ADVANCED_REVIEW_OPTIONS, "고급 리뷰 옵션");
        it(ADVANCED_REVIEW_OPTIONS, "Opzioni avanzate di recensione");

        ja(PROMPT_FROM_PROFILE, "プロファイルからのプロンプト");
        zh(PROMPT_FROM_PROFILE, "来自配置文件的提示词");
        fr(PROMPT_FROM_PROFILE, "Prompt du profil");
        es(PROMPT_FROM_PROFILE, "Prompt del perfil");
        de(PROMPT_FROM_PROFILE, "Prompt aus dem Profil");
        ko(PROMPT_FROM_PROFILE, "프로필의 프롬프트");
        it(PROMPT_FROM_PROFILE, "Prompt dal profilo");

        ja(PROMPT_OVERRIDE, "プロンプトの上書き");
        zh(PROMPT_OVERRIDE, "提示词覆盖");
        fr(PROMPT_OVERRIDE, "Prompt de remplacement");
        es(PROMPT_OVERRIDE, "Prompt alternativo");
        de(PROMPT_OVERRIDE, "Prompt überschreiben");
        ko(PROMPT_OVERRIDE, "프롬프트 재정의");
        it(PROMPT_OVERRIDE, "Sostituzione del prompt");

        ja(USE_STANDARD_PROMPT_INFO, "標準のプロンプト情報を使用");
        zh(USE_STANDARD_PROMPT_INFO, "使用标准提示词信息");
        fr(USE_STANDARD_PROMPT_INFO, "Utiliser les informations du prompt standard");
        es(USE_STANDARD_PROMPT_INFO, "Usar la información estándar del prompt");
        de(USE_STANDARD_PROMPT_INFO, "Standard-Promptinfo verwenden");
        ko(USE_STANDARD_PROMPT_INFO, "표준 프롬프트 정보 사용");
        it(USE_STANDARD_PROMPT_INFO, "Usa le informazioni standard del prompt");

        ja(INCLUDE_LOREBOOK, "ロアブックを含める");
        zh(INCLUDE_LOREBOOK, "包含世界书");
        fr(INCLUDE_LOREBOOK, "Inclure le lorebook");
        es(INCLUDE_LOREBOOK, "Incluir el lorebook");
        de(INCLUDE_LOREBOOK, "Lorebook einbeziehen");
        ko(INCLUDE_LOREBOOK, "로어북 포함");
        it(INCLUDE_LOREBOOK, "Includi il lorebook");

        ja(WITH_SUMMARIES, "要約を含める");
        zh(WITH_SUMMARIES, "包含摘要");
        fr(WITH_SUMMARIES, "Avec les résumés");
        es(WITH_SUMMARIES, "Con resúmenes");
        de(WITH_SUMMARIES, "Mit Zusammenfassungen");
        ko(WITH_SUMMARIES, "요약 포함");
        it(WITH_SUMMARIES, "Con i riassunti");

        ja(SEND_THE_SUMMARIES, "生成と同様に、パートより前の物語の要約を送り、その後に最新の要約より後のパートを送ります（収まる限りの最新のパートを送る代わりに）");
        zh(SEND_THE_SUMMARIES, "与生成时一样，先发送该段落之前故事的摘要，再发送最新摘要之后的段落（而不是发送尽可能多的最新段落）");
        fr(SEND_THE_SUMMARIES, "Envoyer les résumés de l'histoire avant la partie, puis les parties après le résumé le plus récent, comme le fait une génération, au lieu d'autant de dernières parties que possible");
        es(SEND_THE_SUMMARIES, "Enviar los resúmenes de la historia anterior a la parte, y después las partes posteriores al resumen más reciente, como hace una generación, en lugar de tantas de las últimas partes como quepan");
        de(SEND_THE_SUMMARIES, "Die Zusammenfassungen der Geschichte vor dem Abschnitt senden, dann die Abschnitte nach der neuesten Zusammenfassung, wie es eine Generierung tut, statt so vieler der letzten Abschnitte, wie hineinpassen");
        ko(SEND_THE_SUMMARIES, "들어가는 만큼의 최신 파트 대신, 생성처럼 파트 앞 이야기의 요약과 가장 새로운 요약 이후의 파트를 전송합니다");
        it(SEND_THE_SUMMARIES, "Invia i riassunti della storia prima della parte, poi le parti dopo il riassunto più recente, come fa una generazione, invece di tante delle ultime parti quante ne entrano");

        ja(SEND_ORIGINAL_TEXT, "原文を送る");
        zh(SEND_ORIGINAL_TEXT, "发送原文");
        fr(SEND_ORIGINAL_TEXT, "Envoyer le texte original");
        es(SEND_ORIGINAL_TEXT, "Enviar el texto original");
        de(SEND_ORIGINAL_TEXT, "Originaltext senden");
        ko(SEND_ORIGINAL_TEXT, "원문 전송");
        it(SEND_ORIGINAL_TEXT, "Invia il testo originale");

        ja(SEND_THE_TEXT_AS_STORED, "モデルが読む英語の代わりに、翻訳後の言語または人工言語の台詞を含む、保存されているままの物語のテキストを送ります");
        zh(SEND_THE_TEXT_AS_STORED, "发送故事的存储原样文本（对白为翻译后的语言或构造语言），而不是模型所读取的英文");
        fr(SEND_THE_TEXT_AS_STORED, "Envoyer le texte de l'histoire tel qu'il est enregistré, avec les répliques dans la langue traduite ou construite, au lieu de l'anglais que lit un modèle");
        es(SEND_THE_TEXT_AS_STORED, "Enviar el texto de la historia tal como está guardado, con los diálogos en el idioma traducido o construido, en lugar del inglés que lee un modelo");
        de(SEND_THE_TEXT_AS_STORED, "Den Text der Geschichte so senden, wie er gespeichert ist, mit der Rede in der übersetzten oder konstruierten Sprache, statt des Englischen, das ein Modell liest");
        ko(SEND_THE_TEXT_AS_STORED, "모델이 읽는 영어 대신, 번역되거나 인공어로 된 대사를 포함해 저장된 그대로의 이야기 텍스트를 전송합니다");
        it(SEND_THE_TEXT_AS_STORED, "Invia il testo della storia così com'è memorizzato, con le battute nella lingua tradotta o inventata, invece dell'inglese che legge un modello");

        ja(TOKEN_LIMIT, "トークン上限");
        zh(TOKEN_LIMIT, "词元上限");
        fr(TOKEN_LIMIT, "Limite de jetons");
        es(TOKEN_LIMIT, "Límite de tokens");
        de(TOKEN_LIMIT, "Token-Limit");
        ko(TOKEN_LIMIT, "토큰 제한");
        it(TOKEN_LIMIT, "Limite di token");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");
        fr(GENERATE, "Générer");
        es(GENERATE, "Generar");
        de(GENERATE, "Generieren");
        ko(GENERATE, "생성");
        it(GENERATE, "Genera");

        ja(TOTAL_TOKENS, "合計トークン数: %s");
        zh(TOTAL_TOKENS, "总词元数：%s");
        fr(TOTAL_TOKENS, "Total des jetons : %s");
        es(TOTAL_TOKENS, "Total de tokens: %s");
        de(TOTAL_TOKENS, "Tokens insgesamt: %s");
        ko(TOTAL_TOKENS, "전체 토큰: %s");
        it(TOTAL_TOKENS, "Token totali: %s");

        ja(REVIEWER_GENERATE_A_REVIEW, "レビュアー: レビューを生成");
        zh(REVIEWER_GENERATE_A_REVIEW, "审阅器：生成审阅");
        fr(REVIEWER_GENERATE_A_REVIEW, "Critique : générer une critique");
        es(REVIEWER_GENERATE_A_REVIEW, "Revisor: generar una revisión");
        de(REVIEWER_GENERATE_A_REVIEW, "Rezensent: eine Rezension generieren");
        ko(REVIEWER_GENERATE_A_REVIEW, "리뷰어: 리뷰 생성");
        it(REVIEWER_GENERATE_A_REVIEW, "Recensore: genera una recensione");
    }
}
