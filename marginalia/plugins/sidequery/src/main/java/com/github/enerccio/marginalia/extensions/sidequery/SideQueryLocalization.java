package com.github.enerccio.marginalia.extensions.sidequery;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Side Query in the languages besides English; the English text is the key.
 */
public class SideQueryLocalization extends PluginLocalizationBundle {

    public static final SideQueryLocalization T = new SideQueryLocalization();

    public static final String SIDE_QUERY = "Side Query";
    public static final String SIDEQUERY_SETTINGS = "SideQuery Settings";
    public static final String PROFILE = "Profile";
    public static final String NEW = "New";
    public static final String RENAME = "Rename";
    public static final String DELETE = "Delete";
    public static final String SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT = "SideQuery Model (Default: Manuscript AI)";
    public static final String SIDEQUERY_PROTOCOL_DEFAULT = "SideQuery Protocol (Default: Manuscript Protocol)";
    public static final String INITIAL_SYSTEM_QUERY = "Initial System Query";
    public static final String INSTRUCTIONS_BEFORE_USER_INPUT = "Instructions Before User Input";
    public static final String ENABLE_AI_TAB_NAMING = "Enable AI Tab Naming";
    public static final String NEW_SIDEQUERY_PROFILE = "New SideQuery Profile";
    public static final String PROFILE_NAME = "Profile Name";
    public static final String CREATE = "Create";
    public static final String PROFILE_NAME_CANNOT_BE_EMPTY = "Profile name cannot be empty";
    public static final String PROFILE_ALREADY_EXISTS = "Profile already exists";
    public static final String CANCEL = "Cancel";
    public static final String CANNOT_RENAME_PROFILE = "Cannot rename %s profile";
    public static final String RENAME_SIDEQUERY_PROFILE = "Rename SideQuery Profile";
    public static final String NEW_PROFILE_NAME = "New Profile Name";
    public static final String CANNOT_DELETE_PROFILE = "Cannot delete %s profile";
    public static final String MOVE_MESSAGE_UP = "Move message up";
    public static final String MOVE_MESSAGE_DOWN = "Move message down";
    public static final String EDIT_MESSAGE = "Edit message";
    public static final String COPIED_TO_CLIPBOARD = "Copied to clipboard";
    public static final String COPY_MESSAGE = "Copy message";
    public static final String THINKING_PROCESS = "Thinking Process";
    public static final String USER = "User";
    public static final String AI_LABEL = "AI";
    public static final String EDITING = "%s (Editing)";
    public static final String SAVE = "Save";
    public static final String EXCLUDE_FROM_CONTEXT = "Exclude from context";
    public static final String INCLUDE_IN_CONTEXT = "Include in context";
    public static final String LOREBOOK = "Lorebook";
    public static final String CHAT_LOGS = "Chat Logs";
    public static final String STORY_PARTS_FROM_TO_NUMBERED = "Story parts from - to, numbered from 1 as in the outline";
    public static final String FIRST_STORY_PART_FROM_1 = "First story part (from 1)";
    public static final String LAST_STORY_PART = "Last story part";
    public static final String TO = "to";
    public static final String TOKENS_0 = "Tokens: 0";
    public static final String TOKENS = "Tokens: ~%d";
    public static final String SELECT_SAVED_QUERY = "-- Select Saved Query --";
    public static final String SAVE_PROMPT = "Save prompt";
    public static final String SAVE_PROMPT_AS = "Save prompt as...";
    public static final String DELETE_SAVED_PROMPT = "Delete saved prompt";
    public static final String ENTER_QUERY = "Enter query...";
    public static final String CANNOT_SAVE_AN_EMPTY_QUERY = "Cannot save an empty query";
    public static final String SAVED_QUERY = "Saved query: %s";
    public static final String SAVE_QUERY_AS = "Save Query As";
    public static final String QUERY_NAME = "Query Name";
    public static final String NAME_CANNOT_BE_EMPTY = "Name cannot be empty";
    public static final String SELECT_A_SAVED_QUERY = "Select a saved query to delete";
    public static final String DELETED_QUERY = "Deleted query: %s";
    public static final String GENERATING_RESPONSE = "Generating response...";
    public static final String ERROR = "Error: %s";
    public static final String FAILED_TO_START_INFERENCE = "Failed to start inference: %s";
    public static final String ADDITIONAL_SOURCES = "Additional sources";
    public static final String ADDITIONAL_SOURCES_DATA = "Additional sources: data of other extensions";
    public static final String NO_OTHER_EXTENSION_OFFERS_DATA = "No other extension offers data.";
    public static final String ABOUT_THE_BOOK = "About the book";
    public static final String ABOUT_STORY_PARTS_ADDED = "About story parts (added to each included part, needs Chat Logs)";
    public static final String STOP = "STOP";
    public static final String SEND = "SEND";
    public static final String RENAME_TAB = "Rename Tab";
    public static final String TAB_NAME = "Tab Name";
    public static final String CANNOT_CLOSE_THE_LAST_TAB = "Cannot close the last tab";
    public static final String SIDEQUERY_ANSWER_A_QUESTION = "SideQuery: answer a question";

    @Override
    protected void loadKeys() {
        ja(SIDE_QUERY, "サイドクエリ");
        zh(SIDE_QUERY, "侧边查询");
        fr(SIDE_QUERY, "Requête annexe");
        es(SIDE_QUERY, "Consulta lateral");
        de(SIDE_QUERY, "Nebenabfrage");

        ja(SIDEQUERY_SETTINGS, "サイドクエリ設定");
        zh(SIDEQUERY_SETTINGS, "侧边查询设置");
        fr(SIDEQUERY_SETTINGS, "Paramètres de SideQuery");
        es(SIDEQUERY_SETTINGS, "Ajustes de SideQuery");
        de(SIDEQUERY_SETTINGS, "Einstellungen der Nebenabfrage");

        ja(PROFILE, "プロファイル");
        zh(PROFILE, "配置文件");
        fr(PROFILE, "Profil");
        es(PROFILE, "Perfil");
        de(PROFILE, "Profil");

        ja(NEW, "新規");
        zh(NEW, "新建");
        fr(NEW, "Nouveau");
        es(NEW, "Nuevo");
        de(NEW, "Neu");

        ja(RENAME, "名前を変更");
        zh(RENAME, "重命名");
        fr(RENAME, "Renommer");
        es(RENAME, "Renombrar");
        de(RENAME, "Umbenennen");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");

        ja(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "サイドクエリのモデル（デフォルト: 原稿のAI）");
        zh(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "侧边查询模型（默认：稿件的 AI）");
        fr(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "Modèle de SideQuery (par défaut : celui du manuscrit)");
        es(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "Modelo de SideQuery (predeterminado: la IA del manuscrito)");
        de(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "Modell der Nebenabfrage (Standard: KI des Manuskripts)");

        ja(SIDEQUERY_PROTOCOL_DEFAULT, "サイドクエリのプロトコル（デフォルト: 原稿のプロトコル）");
        zh(SIDEQUERY_PROTOCOL_DEFAULT, "侧边查询协议（默认：稿件的协议）");
        fr(SIDEQUERY_PROTOCOL_DEFAULT, "Protocole de SideQuery (par défaut : celui du manuscrit)");
        es(SIDEQUERY_PROTOCOL_DEFAULT, "Protocolo de SideQuery (predeterminado: el protocolo del manuscrito)");
        de(SIDEQUERY_PROTOCOL_DEFAULT, "Protokoll der Nebenabfrage (Standard: Protokoll des Manuskripts)");

        ja(INITIAL_SYSTEM_QUERY, "最初のシステムクエリ");
        zh(INITIAL_SYSTEM_QUERY, "初始系统查询");
        fr(INITIAL_SYSTEM_QUERY, "Requête système initiale");
        es(INITIAL_SYSTEM_QUERY, "Consulta inicial del sistema");
        de(INITIAL_SYSTEM_QUERY, "Anfängliche Systemabfrage");

        ja(INSTRUCTIONS_BEFORE_USER_INPUT, "ユーザー入力の前の指示");
        zh(INSTRUCTIONS_BEFORE_USER_INPUT, "用户输入之前的指令");
        fr(INSTRUCTIONS_BEFORE_USER_INPUT, "Instructions avant la saisie de l'utilisateur");
        es(INSTRUCTIONS_BEFORE_USER_INPUT, "Instrucciones antes de la entrada del usuario");
        de(INSTRUCTIONS_BEFORE_USER_INPUT, "Anweisungen vor der Benutzereingabe");

        ja(ENABLE_AI_TAB_NAMING, "AIによるタブ名の自動設定を有効にする");
        zh(ENABLE_AI_TAB_NAMING, "启用 AI 自动命名标签页");
        fr(ENABLE_AI_TAB_NAMING, "Nommer les onglets par l'IA");
        es(ENABLE_AI_TAB_NAMING, "Activar el nombre de las pestañas por IA");
        de(ENABLE_AI_TAB_NAMING, "KI-Benennung der Tabs aktivieren");

        ja(NEW_SIDEQUERY_PROFILE, "新しいサイドクエリのプロファイル");
        zh(NEW_SIDEQUERY_PROFILE, "新建侧边查询配置文件");
        fr(NEW_SIDEQUERY_PROFILE, "Nouveau profil SideQuery");
        es(NEW_SIDEQUERY_PROFILE, "Nuevo perfil de SideQuery");
        de(NEW_SIDEQUERY_PROFILE, "Neues Profil für die Nebenabfrage");

        ja(PROFILE_NAME, "プロファイル名");
        zh(PROFILE_NAME, "配置文件名称");
        fr(PROFILE_NAME, "Nom du profil");
        es(PROFILE_NAME, "Nombre del perfil");
        de(PROFILE_NAME, "Profilname");

        ja(CREATE, "作成");
        zh(CREATE, "创建");
        fr(CREATE, "Créer");
        es(CREATE, "Crear");
        de(CREATE, "Erstellen");

        ja(PROFILE_NAME_CANNOT_BE_EMPTY, "プロファイル名は空にできません");
        zh(PROFILE_NAME_CANNOT_BE_EMPTY, "配置文件名称不能为空");
        fr(PROFILE_NAME_CANNOT_BE_EMPTY, "Le nom du profil ne peut pas être vide");
        es(PROFILE_NAME_CANNOT_BE_EMPTY, "El nombre del perfil no puede estar vacío");
        de(PROFILE_NAME_CANNOT_BE_EMPTY, "Der Profilname darf nicht leer sein");

        ja(PROFILE_ALREADY_EXISTS, "プロファイルは既に存在します");
        zh(PROFILE_ALREADY_EXISTS, "配置文件已存在");
        fr(PROFILE_ALREADY_EXISTS, "Le profil existe déjà");
        es(PROFILE_ALREADY_EXISTS, "El perfil ya existe");
        de(PROFILE_ALREADY_EXISTS, "Das Profil existiert bereits");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(CANNOT_RENAME_PROFILE, "%s プロファイルの名前は変更できません");
        zh(CANNOT_RENAME_PROFILE, "无法重命名 %s 配置文件");
        fr(CANNOT_RENAME_PROFILE, "Impossible de renommer le profil %s");
        es(CANNOT_RENAME_PROFILE, "No se puede renombrar el perfil %s");
        de(CANNOT_RENAME_PROFILE, "Das Profil %s kann nicht umbenannt werden");

        ja(RENAME_SIDEQUERY_PROFILE, "サイドクエリのプロファイル名を変更");
        zh(RENAME_SIDEQUERY_PROFILE, "重命名侧边查询配置文件");
        fr(RENAME_SIDEQUERY_PROFILE, "Renommer le profil SideQuery");
        es(RENAME_SIDEQUERY_PROFILE, "Renombrar perfil de SideQuery");
        de(RENAME_SIDEQUERY_PROFILE, "Profil der Nebenabfrage umbenennen");

        ja(NEW_PROFILE_NAME, "新しいプロファイル名");
        zh(NEW_PROFILE_NAME, "新配置文件名称");
        fr(NEW_PROFILE_NAME, "Nouveau nom du profil");
        es(NEW_PROFILE_NAME, "Nombre nuevo del perfil");
        de(NEW_PROFILE_NAME, "Neuer Profilname");

        ja(CANNOT_DELETE_PROFILE, "%s プロファイルは削除できません");
        zh(CANNOT_DELETE_PROFILE, "无法删除 %s 配置文件");
        fr(CANNOT_DELETE_PROFILE, "Impossible de supprimer le profil %s");
        es(CANNOT_DELETE_PROFILE, "No se puede eliminar el perfil %s");
        de(CANNOT_DELETE_PROFILE, "Das Profil %s kann nicht gelöscht werden");

        ja(MOVE_MESSAGE_UP, "メッセージを上へ移動");
        zh(MOVE_MESSAGE_UP, "上移消息");
        fr(MOVE_MESSAGE_UP, "Monter le message");
        es(MOVE_MESSAGE_UP, "Subir el mensaje");
        de(MOVE_MESSAGE_UP, "Nachricht nach oben verschieben");

        ja(MOVE_MESSAGE_DOWN, "メッセージを下へ移動");
        zh(MOVE_MESSAGE_DOWN, "下移消息");
        fr(MOVE_MESSAGE_DOWN, "Descendre le message");
        es(MOVE_MESSAGE_DOWN, "Bajar el mensaje");
        de(MOVE_MESSAGE_DOWN, "Nachricht nach unten verschieben");

        ja(EDIT_MESSAGE, "メッセージを編集");
        zh(EDIT_MESSAGE, "编辑消息");
        fr(EDIT_MESSAGE, "Modifier le message");
        es(EDIT_MESSAGE, "Editar mensaje");
        de(EDIT_MESSAGE, "Nachricht bearbeiten");

        ja(COPIED_TO_CLIPBOARD, "クリップボードにコピーしました");
        zh(COPIED_TO_CLIPBOARD, "已复制到剪贴板");
        fr(COPIED_TO_CLIPBOARD, "Copié dans le presse-papiers");
        es(COPIED_TO_CLIPBOARD, "Copiado al portapapeles");
        de(COPIED_TO_CLIPBOARD, "In die Zwischenablage kopiert");

        ja(COPY_MESSAGE, "メッセージをコピー");
        zh(COPY_MESSAGE, "复制消息");
        fr(COPY_MESSAGE, "Copier le message");
        es(COPY_MESSAGE, "Copiar mensaje");
        de(COPY_MESSAGE, "Nachricht kopieren");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");
        fr(THINKING_PROCESS, "Réflexion");
        es(THINKING_PROCESS, "Proceso de razonamiento");
        de(THINKING_PROCESS, "Denkprozess");

        ja(USER, "ユーザー");
        zh(USER, "用户");
        fr(USER, "Utilisateur");
        es(USER, "Usuario");
        de(USER, "Benutzer");

        ja(AI_LABEL, "AI");
        zh(AI_LABEL, "AI");
        fr(AI_LABEL, "IA");
        es(AI_LABEL, "IA");
        de(AI_LABEL, "KI");

        ja(EDITING, "%s（編集中）");
        zh(EDITING, "%s（编辑中）");
        fr(EDITING, "%s (en cours de modification)");
        es(EDITING, "%s (editando)");
        de(EDITING, "%s (wird bearbeitet)");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");

        ja(EXCLUDE_FROM_CONTEXT, "コンテキストから除外");
        zh(EXCLUDE_FROM_CONTEXT, "从上下文中排除");
        fr(EXCLUDE_FROM_CONTEXT, "Exclure du contexte");
        es(EXCLUDE_FROM_CONTEXT, "Excluir del contexto");
        de(EXCLUDE_FROM_CONTEXT, "Aus dem Kontext ausschließen");

        ja(INCLUDE_IN_CONTEXT, "コンテキストに含める");
        zh(INCLUDE_IN_CONTEXT, "包含在上下文中");
        fr(INCLUDE_IN_CONTEXT, "Inclure dans le contexte");
        es(INCLUDE_IN_CONTEXT, "Incluir en el contexto");
        de(INCLUDE_IN_CONTEXT, "In den Kontext aufnehmen");

        ja(LOREBOOK, "ロアブック");
        zh(LOREBOOK, "世界书");
        fr(LOREBOOK, "Lorebook");
        es(LOREBOOK, "Lorebook");
        de(LOREBOOK, "Lorebook");

        ja(CHAT_LOGS, "チャットログ");
        zh(CHAT_LOGS, "聊天记录");
        fr(CHAT_LOGS, "Historique de l'histoire");
        es(CHAT_LOGS, "Registros del chat");
        de(CHAT_LOGS, "Chat-Protokolle");

        ja(STORY_PARTS_FROM_TO_NUMBERED, "開始〜終了のストーリーパート。アウトラインと同様に1から番号が付きます");
        zh(STORY_PARTS_FROM_TO_NUMBERED, "从 - 到的故事部分，编号从 1 开始，与大纲一致");
        fr(STORY_PARTS_FROM_TO_NUMBERED, "Parties de l'histoire de - à, numérotées à partir de 1 comme dans le plan");
        es(STORY_PARTS_FROM_TO_NUMBERED, "Partes de la historia de la ... a la ..., numeradas desde 1 como en el esquema");
        de(STORY_PARTS_FROM_TO_NUMBERED, "Abschnitte der Geschichte von – bis, ab 1 nummeriert wie in der Gliederung");

        ja(FIRST_STORY_PART_FROM_1, "最初のストーリーパート（1から）");
        zh(FIRST_STORY_PART_FROM_1, "第一个故事部分（从 1 开始）");
        fr(FIRST_STORY_PART_FROM_1, "Première partie de l'histoire (à partir de 1)");
        es(FIRST_STORY_PART_FROM_1, "Primera parte de la historia (desde 1)");
        de(FIRST_STORY_PART_FROM_1, "Erster Abschnitt der Geschichte (ab 1)");

        ja(LAST_STORY_PART, "最後のストーリーパート");
        zh(LAST_STORY_PART, "最后一个故事部分");
        fr(LAST_STORY_PART, "Dernière partie de l'histoire");
        es(LAST_STORY_PART, "Última parte de la historia");
        de(LAST_STORY_PART, "Letzter Abschnitt der Geschichte");

        ja(TO, "〜");
        zh(TO, "至");
        fr(TO, "à");
        es(TO, "hasta");
        de(TO, "bis");

        ja(TOKENS_0, "トークン: 0");
        zh(TOKENS_0, "词元：0");
        fr(TOKENS_0, "Jetons : 0");
        es(TOKENS_0, "Tokens: 0");
        de(TOKENS_0, "Tokens: 0");

        ja(TOKENS, "トークン: 約%d");
        zh(TOKENS, "词元：约 %d");
        fr(TOKENS, "Jetons : ~%d");
        es(TOKENS, "Tokens: ~%d");
        de(TOKENS, "Tokens: ~%d");

        ja(SELECT_SAVED_QUERY, "-- 保存済みクエリを選択 --");
        zh(SELECT_SAVED_QUERY, "-- 选择已保存的查询 --");
        fr(SELECT_SAVED_QUERY, "-- Choisir une requête enregistrée --");
        es(SELECT_SAVED_QUERY, "-- Seleccionar consulta guardada --");
        de(SELECT_SAVED_QUERY, "-- Gespeicherte Abfrage wählen --");

        ja(SAVE_PROMPT, "プロンプトを保存");
        zh(SAVE_PROMPT, "保存提示词");
        fr(SAVE_PROMPT, "Enregistrer le prompt");
        es(SAVE_PROMPT, "Guardar prompt");
        de(SAVE_PROMPT, "Prompt speichern");

        ja(SAVE_PROMPT_AS, "プロンプトを名前を付けて保存...");
        zh(SAVE_PROMPT_AS, "提示词另存为...");
        fr(SAVE_PROMPT_AS, "Enregistrer le prompt sous...");
        es(SAVE_PROMPT_AS, "Guardar prompt como...");
        de(SAVE_PROMPT_AS, "Prompt speichern unter ...");

        ja(DELETE_SAVED_PROMPT, "保存済みプロンプトを削除");
        zh(DELETE_SAVED_PROMPT, "删除已保存的提示词");
        fr(DELETE_SAVED_PROMPT, "Supprimer le prompt enregistré");
        es(DELETE_SAVED_PROMPT, "Eliminar prompt guardado");
        de(DELETE_SAVED_PROMPT, "Gespeicherten Prompt löschen");

        ja(ENTER_QUERY, "クエリを入力...");
        zh(ENTER_QUERY, "输入查询……");
        fr(ENTER_QUERY, "Saisissez une requête...");
        es(ENTER_QUERY, "Escribe la consulta...");
        de(ENTER_QUERY, "Abfrage eingeben ...");

        ja(CANNOT_SAVE_AN_EMPTY_QUERY, "空のクエリは保存できません");
        zh(CANNOT_SAVE_AN_EMPTY_QUERY, "无法保存空查询");
        fr(CANNOT_SAVE_AN_EMPTY_QUERY, "Impossible d'enregistrer une requête vide");
        es(CANNOT_SAVE_AN_EMPTY_QUERY, "No se puede guardar una consulta vacía");
        de(CANNOT_SAVE_AN_EMPTY_QUERY, "Eine leere Abfrage kann nicht gespeichert werden");

        ja(SAVED_QUERY, "クエリを保存しました: %s");
        zh(SAVED_QUERY, "已保存查询：%s");
        fr(SAVED_QUERY, "Requête enregistrée : %s");
        es(SAVED_QUERY, "Consulta guardada: %s");
        de(SAVED_QUERY, "Abfrage gespeichert: %s");

        ja(SAVE_QUERY_AS, "クエリを名前を付けて保存");
        zh(SAVE_QUERY_AS, "查询另存为");
        fr(SAVE_QUERY_AS, "Enregistrer la requête sous");
        es(SAVE_QUERY_AS, "Guardar consulta como");
        de(SAVE_QUERY_AS, "Abfrage speichern unter");

        ja(QUERY_NAME, "クエリ名");
        zh(QUERY_NAME, "查询名称");
        fr(QUERY_NAME, "Nom de la requête");
        es(QUERY_NAME, "Nombre de la consulta");
        de(QUERY_NAME, "Name der Abfrage");

        ja(NAME_CANNOT_BE_EMPTY, "名前は空にできません");
        zh(NAME_CANNOT_BE_EMPTY, "名称不能为空");
        fr(NAME_CANNOT_BE_EMPTY, "Le nom ne peut pas être vide");
        es(NAME_CANNOT_BE_EMPTY, "El nombre no puede estar vacío");
        de(NAME_CANNOT_BE_EMPTY, "Der Name darf nicht leer sein");

        ja(SELECT_A_SAVED_QUERY, "削除する保存済みクエリを選択してください");
        zh(SELECT_A_SAVED_QUERY, "请选择要删除的已保存查询");
        fr(SELECT_A_SAVED_QUERY, "Choisissez une requête enregistrée à supprimer");
        es(SELECT_A_SAVED_QUERY, "Selecciona una consulta guardada para eliminarla");
        de(SELECT_A_SAVED_QUERY, "Wähle eine gespeicherte Abfrage zum Löschen");

        ja(DELETED_QUERY, "クエリを削除しました: %s");
        zh(DELETED_QUERY, "已删除查询：%s");
        fr(DELETED_QUERY, "Requête supprimée : %s");
        es(DELETED_QUERY, "Consulta eliminada: %s");
        de(DELETED_QUERY, "Abfrage gelöscht: %s");

        ja(GENERATING_RESPONSE, "応答を生成中...");
        zh(GENERATING_RESPONSE, "正在生成回复……");
        fr(GENERATING_RESPONSE, "Génération de la réponse...");
        es(GENERATING_RESPONSE, "Generando la respuesta...");
        de(GENERATING_RESPONSE, "Antwort wird generiert ...");

        ja(ERROR, "エラー: %s");
        zh(ERROR, "错误：%s");
        fr(ERROR, "Erreur : %s");
        es(ERROR, "Error: %s");
        de(ERROR, "Fehler: %s");

        ja(FAILED_TO_START_INFERENCE, "推論を開始できませんでした: %s");
        zh(FAILED_TO_START_INFERENCE, "无法启动推理：%s");
        fr(FAILED_TO_START_INFERENCE, "Échec du lancement de l'inférence : %s");
        es(FAILED_TO_START_INFERENCE, "No se pudo iniciar la inferencia: %s");
        de(FAILED_TO_START_INFERENCE, "Die Inferenz konnte nicht gestartet werden: %s");

        ja(ADDITIONAL_SOURCES, "追加ソース");
        zh(ADDITIONAL_SOURCES, "其他来源");
        fr(ADDITIONAL_SOURCES, "Sources supplémentaires");
        es(ADDITIONAL_SOURCES, "Fuentes adicionales");
        de(ADDITIONAL_SOURCES, "Zusätzliche Quellen");

        ja(ADDITIONAL_SOURCES_DATA, "追加ソース: 他の拡張機能のデータ");
        zh(ADDITIONAL_SOURCES_DATA, "其他来源：其他扩展的数据");
        fr(ADDITIONAL_SOURCES_DATA, "Sources supplémentaires : données d'autres extensions");
        es(ADDITIONAL_SOURCES_DATA, "Fuentes adicionales: datos de otras extensiones");
        de(ADDITIONAL_SOURCES_DATA, "Zusätzliche Quellen: Daten anderer Erweiterungen");

        ja(NO_OTHER_EXTENSION_OFFERS_DATA, "データを提供している他の拡張機能はありません。");
        zh(NO_OTHER_EXTENSION_OFFERS_DATA, "没有其他扩展提供数据。");
        fr(NO_OTHER_EXTENSION_OFFERS_DATA, "Aucune autre extension ne propose de données.");
        es(NO_OTHER_EXTENSION_OFFERS_DATA, "Ninguna otra extensión ofrece datos.");
        de(NO_OTHER_EXTENSION_OFFERS_DATA, "Keine andere Erweiterung bietet Daten an.");

        ja(ABOUT_THE_BOOK, "ブックについて");
        zh(ABOUT_THE_BOOK, "关于本书");
        fr(ABOUT_THE_BOOK, "À propos du livre");
        es(ABOUT_THE_BOOK, "Sobre el libro");
        de(ABOUT_THE_BOOK, "Über das Buch");

        ja(ABOUT_STORY_PARTS_ADDED, "ストーリーパートについて（含まれる各パートに追加されます。チャットログが必要です）");
        zh(ABOUT_STORY_PARTS_ADDED, "关于故事部分（添加到每个包含的部分，需要聊天记录）");
        fr(ABOUT_STORY_PARTS_ADDED, "À propos des parties de l'histoire (ajouté à chaque partie incluse, nécessite l'historique de l'histoire)");
        es(ABOUT_STORY_PARTS_ADDED, "Sobre las partes de la historia (se añaden a cada parte incluida, requiere Registros del chat)");
        de(ABOUT_STORY_PARTS_ADDED, "Über Abschnitte der Geschichte (zu jedem einbezogenen Abschnitt hinzugefügt, erfordert Chat-Protokolle)");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "ARRÊTER");
        es(STOP, "DETENER");
        de(STOP, "STOPP");

        ja(SEND, "送信");
        zh(SEND, "发送");
        fr(SEND, "ENVOYER");
        es(SEND, "ENVIAR");
        de(SEND, "SENDEN");

        ja(RENAME_TAB, "タブ名を変更");
        zh(RENAME_TAB, "重命名标签页");
        fr(RENAME_TAB, "Renommer l'onglet");
        es(RENAME_TAB, "Renombrar pestaña");
        de(RENAME_TAB, "Tab umbenennen");

        ja(TAB_NAME, "タブ名");
        zh(TAB_NAME, "标签页名称");
        fr(TAB_NAME, "Nom de l'onglet");
        es(TAB_NAME, "Nombre de la pestaña");
        de(TAB_NAME, "Tab-Name");

        ja(CANNOT_CLOSE_THE_LAST_TAB, "最後のタブは閉じられません");
        zh(CANNOT_CLOSE_THE_LAST_TAB, "无法关闭最后一个标签页");
        fr(CANNOT_CLOSE_THE_LAST_TAB, "Impossible de fermer le dernier onglet");
        es(CANNOT_CLOSE_THE_LAST_TAB, "No se puede cerrar la última pestaña");
        de(CANNOT_CLOSE_THE_LAST_TAB, "Der letzte Tab kann nicht geschlossen werden");

        ja(SIDEQUERY_ANSWER_A_QUESTION, "SideQuery: 質問に回答");
        zh(SIDEQUERY_ANSWER_A_QUESTION, "SideQuery：回答问题");
        fr(SIDEQUERY_ANSWER_A_QUESTION, "SideQuery : répondre à une question");
        es(SIDEQUERY_ANSWER_A_QUESTION, "SideQuery: responder a una pregunta");
        de(SIDEQUERY_ANSWER_A_QUESTION, "Nebenabfrage: eine Frage beantworten");
    }
}
