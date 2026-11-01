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

    @Override
    protected void loadKeys() {
        ja(SIDE_QUERY, "サイドクエリ");
        zh(SIDE_QUERY, "侧边查询");

        ja(SIDEQUERY_SETTINGS, "サイドクエリ設定");
        zh(SIDEQUERY_SETTINGS, "侧边查询设置");

        ja(PROFILE, "プロファイル");
        zh(PROFILE, "配置文件");

        ja(NEW, "新規");
        zh(NEW, "新建");

        ja(RENAME, "名前を変更");
        zh(RENAME, "重命名");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "サイドクエリのモデル（デフォルト: 原稿のAI）");
        zh(SIDEQUERY_MODEL_DEFAULT_MANUSCRIPT, "侧边查询模型（默认：稿件的 AI）");

        ja(SIDEQUERY_PROTOCOL_DEFAULT, "サイドクエリのプロトコル（デフォルト: 原稿のプロトコル）");
        zh(SIDEQUERY_PROTOCOL_DEFAULT, "侧边查询协议（默认：稿件的协议）");

        ja(INITIAL_SYSTEM_QUERY, "最初のシステムクエリ");
        zh(INITIAL_SYSTEM_QUERY, "初始系统查询");

        ja(INSTRUCTIONS_BEFORE_USER_INPUT, "ユーザー入力の前の指示");
        zh(INSTRUCTIONS_BEFORE_USER_INPUT, "用户输入之前的指令");

        ja(ENABLE_AI_TAB_NAMING, "AIによるタブ名の自動設定を有効にする");
        zh(ENABLE_AI_TAB_NAMING, "启用 AI 自动命名标签页");

        ja(NEW_SIDEQUERY_PROFILE, "新しいサイドクエリのプロファイル");
        zh(NEW_SIDEQUERY_PROFILE, "新建侧边查询配置文件");

        ja(PROFILE_NAME, "プロファイル名");
        zh(PROFILE_NAME, "配置文件名称");

        ja(CREATE, "作成");
        zh(CREATE, "创建");

        ja(PROFILE_NAME_CANNOT_BE_EMPTY, "プロファイル名は空にできません");
        zh(PROFILE_NAME_CANNOT_BE_EMPTY, "配置文件名称不能为空");

        ja(PROFILE_ALREADY_EXISTS, "プロファイルは既に存在します");
        zh(PROFILE_ALREADY_EXISTS, "配置文件已存在");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(CANNOT_RENAME_PROFILE, "%s プロファイルの名前は変更できません");
        zh(CANNOT_RENAME_PROFILE, "无法重命名 %s 配置文件");

        ja(RENAME_SIDEQUERY_PROFILE, "サイドクエリのプロファイル名を変更");
        zh(RENAME_SIDEQUERY_PROFILE, "重命名侧边查询配置文件");

        ja(NEW_PROFILE_NAME, "新しいプロファイル名");
        zh(NEW_PROFILE_NAME, "新配置文件名称");

        ja(CANNOT_DELETE_PROFILE, "%s プロファイルは削除できません");
        zh(CANNOT_DELETE_PROFILE, "无法删除 %s 配置文件");

        ja(MOVE_MESSAGE_UP, "メッセージを上へ移動");
        zh(MOVE_MESSAGE_UP, "上移消息");

        ja(MOVE_MESSAGE_DOWN, "メッセージを下へ移動");
        zh(MOVE_MESSAGE_DOWN, "下移消息");

        ja(EDIT_MESSAGE, "メッセージを編集");
        zh(EDIT_MESSAGE, "编辑消息");

        ja(COPIED_TO_CLIPBOARD, "クリップボードにコピーしました");
        zh(COPIED_TO_CLIPBOARD, "已复制到剪贴板");

        ja(COPY_MESSAGE, "メッセージをコピー");
        zh(COPY_MESSAGE, "复制消息");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");

        ja(USER, "ユーザー");
        zh(USER, "用户");

        ja(AI_LABEL, "AI");
        zh(AI_LABEL, "AI");

        ja(EDITING, "%s（編集中）");
        zh(EDITING, "%s（编辑中）");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(EXCLUDE_FROM_CONTEXT, "コンテキストから除外");
        zh(EXCLUDE_FROM_CONTEXT, "从上下文中排除");

        ja(INCLUDE_IN_CONTEXT, "コンテキストに含める");
        zh(INCLUDE_IN_CONTEXT, "包含在上下文中");

        ja(LOREBOOK, "ロアブック");
        zh(LOREBOOK, "世界书");

        ja(CHAT_LOGS, "チャットログ");
        zh(CHAT_LOGS, "聊天记录");

        ja(STORY_PARTS_FROM_TO_NUMBERED, "開始〜終了のストーリーパート。アウトラインと同様に1から番号が付きます");
        zh(STORY_PARTS_FROM_TO_NUMBERED, "从 - 到的故事部分，编号从 1 开始，与大纲一致");

        ja(FIRST_STORY_PART_FROM_1, "最初のストーリーパート（1から）");
        zh(FIRST_STORY_PART_FROM_1, "第一个故事部分（从 1 开始）");

        ja(LAST_STORY_PART, "最後のストーリーパート");
        zh(LAST_STORY_PART, "最后一个故事部分");

        ja(TO, "〜");
        zh(TO, "至");

        ja(TOKENS_0, "トークン: 0");
        zh(TOKENS_0, "词元：0");

        ja(TOKENS, "トークン: 約%d");
        zh(TOKENS, "词元：约 %d");

        ja(SELECT_SAVED_QUERY, "-- 保存済みクエリを選択 --");
        zh(SELECT_SAVED_QUERY, "-- 选择已保存的查询 --");

        ja(SAVE_PROMPT, "プロンプトを保存");
        zh(SAVE_PROMPT, "保存提示词");

        ja(SAVE_PROMPT_AS, "プロンプトを名前を付けて保存...");
        zh(SAVE_PROMPT_AS, "提示词另存为...");

        ja(DELETE_SAVED_PROMPT, "保存済みプロンプトを削除");
        zh(DELETE_SAVED_PROMPT, "删除已保存的提示词");

        ja(ENTER_QUERY, "クエリを入力...");
        zh(ENTER_QUERY, "输入查询……");

        ja(CANNOT_SAVE_AN_EMPTY_QUERY, "空のクエリは保存できません");
        zh(CANNOT_SAVE_AN_EMPTY_QUERY, "无法保存空查询");

        ja(SAVED_QUERY, "クエリを保存しました: %s");
        zh(SAVED_QUERY, "已保存查询：%s");

        ja(SAVE_QUERY_AS, "クエリを名前を付けて保存");
        zh(SAVE_QUERY_AS, "查询另存为");

        ja(QUERY_NAME, "クエリ名");
        zh(QUERY_NAME, "查询名称");

        ja(NAME_CANNOT_BE_EMPTY, "名前は空にできません");
        zh(NAME_CANNOT_BE_EMPTY, "名称不能为空");

        ja(SELECT_A_SAVED_QUERY, "削除する保存済みクエリを選択してください");
        zh(SELECT_A_SAVED_QUERY, "请选择要删除的已保存查询");

        ja(DELETED_QUERY, "クエリを削除しました: %s");
        zh(DELETED_QUERY, "已删除查询：%s");

        ja(GENERATING_RESPONSE, "応答を生成中...");
        zh(GENERATING_RESPONSE, "正在生成回复……");

        ja(ERROR, "エラー: %s");
        zh(ERROR, "错误：%s");

        ja(FAILED_TO_START_INFERENCE, "推論を開始できませんでした: %s");
        zh(FAILED_TO_START_INFERENCE, "无法启动推理：%s");

        ja(ADDITIONAL_SOURCES, "追加ソース");
        zh(ADDITIONAL_SOURCES, "其他来源");

        ja(ADDITIONAL_SOURCES_DATA, "追加ソース: 他の拡張機能のデータ");
        zh(ADDITIONAL_SOURCES_DATA, "其他来源：其他扩展的数据");

        ja(NO_OTHER_EXTENSION_OFFERS_DATA, "データを提供している他の拡張機能はありません。");
        zh(NO_OTHER_EXTENSION_OFFERS_DATA, "没有其他扩展提供数据。");

        ja(ABOUT_THE_BOOK, "ブックについて");
        zh(ABOUT_THE_BOOK, "关于本书");

        ja(ABOUT_STORY_PARTS_ADDED, "ストーリーパートについて（含まれる各パートに追加されます。チャットログが必要です）");
        zh(ABOUT_STORY_PARTS_ADDED, "关于故事部分（添加到每个包含的部分，需要聊天记录）");

        ja(STOP, "停止");
        zh(STOP, "停止");

        ja(SEND, "送信");
        zh(SEND, "发送");

        ja(RENAME_TAB, "タブ名を変更");
        zh(RENAME_TAB, "重命名标签页");

        ja(TAB_NAME, "タブ名");
        zh(TAB_NAME, "标签页名称");

        ja(CANNOT_CLOSE_THE_LAST_TAB, "最後のタブは閉じられません");
        zh(CANNOT_CLOSE_THE_LAST_TAB, "无法关闭最后一个标签页");
    }
}
