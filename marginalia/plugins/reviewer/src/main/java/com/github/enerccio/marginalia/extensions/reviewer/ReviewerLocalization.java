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

        ja(VIEW_GENERATE_REVIEW, "レビューの表示 / 生成");
        zh(VIEW_GENERATE_REVIEW, "查看 / 生成评论");

        ja(REVIEW_ADVANCED_OPTIONS, "レビュー: 詳細オプション");
        zh(REVIEW_ADVANCED_OPTIONS, "评论：高级选项");

        ja(ADVANCED_OPTIONS, "詳細オプション");
        zh(ADVANCED_OPTIONS, "高级选项");

        ja(DELETE_REVIEW, "レビューを削除");
        zh(DELETE_REVIEW, "删除评论");

        ja(REVIEWER_SETTINGS, "レビュアー設定");
        zh(REVIEWER_SETTINGS, "评论者设置");

        ja(REVIEWER_PROFILE_DEFAULT_SELECTED, "レビュアープロファイル（デフォルト: 設定で選択したもの）");
        zh(REVIEWER_PROFILE_DEFAULT_SELECTED, "评论者配置文件（默认：在设置中选择的）");

        ja(MESSAGE_REVIEW, "メッセージのレビュー");
        zh(MESSAGE_REVIEW, "消息评论");

        ja(STOP_GENERATING, "生成を停止");
        zh(STOP_GENERATING, "停止生成");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(GENERATING_REVIEW_PLEASE_WAIT, "レビューを生成中です。お待ちください...");
        zh(GENERATING_REVIEW_PLEASE_WAIT, "正在生成评论，请稍候……");

        ja(ERROR_GENERATING_REVIEW, "レビューの生成中にエラーが発生しました: %s");
        zh(ERROR_GENERATING_REVIEW, "生成评论时出错：%s");

        ja(FAILED_TO_START_INFERENCE, "推論を開始できませんでした: %s");
        zh(FAILED_TO_START_INFERENCE, "无法启动推理：%s");

        ja(PROFILE, "プロファイル");
        zh(PROFILE, "配置文件");

        ja(NEW, "新規");
        zh(NEW, "新建");

        ja(RENAME, "名前を変更");
        zh(RENAME, "重命名");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "レビュアーモデル（デフォルト: 原稿のAI）");
        zh(REVIEWER_MODEL_DEFAULT_MANUSCRIPT, "评论者模型（默认：稿件的 AI）");

        ja(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "レビュアープロトコル（デフォルト: 原稿のプロトコル）");
        zh(REVIEWER_PROTOCOL_DEFAULT_MANUSCRIPT, "评论者协议（默认：稿件的协议）");

        ja(REVIEW_PRE_PROMPT_SYSTEM, "レビューの前置プロンプト（システム）");
        zh(REVIEW_PRE_PROMPT_SYSTEM, "评论前置提示词（系统）");

        ja(REVIEW_POST_PROMPT_USER, "レビューの後置プロンプト（ユーザー）");
        zh(REVIEW_POST_PROMPT_USER, "评论后置提示词（用户）");

        ja(NEW_PROFILE, "新しいプロファイル");
        zh(NEW_PROFILE, "新建配置文件");

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

        ja(RENAME_PROFILE, "プロファイル名を変更");
        zh(RENAME_PROFILE, "重命名配置文件");

        ja(NEW_PROFILE_NAME, "新しいプロファイル名");
        zh(NEW_PROFILE_NAME, "新配置文件名称");

        ja(CANNOT_DELETE_PROFILE, "%s プロファイルは削除できません");
        zh(CANNOT_DELETE_PROFILE, "无法删除 %s 配置文件");

        ja(ADVANCED_REVIEW_OPTIONS, "レビューの詳細オプション");
        zh(ADVANCED_REVIEW_OPTIONS, "高级评论选项");

        ja(PROMPT_FROM_PROFILE, "プロファイルからのプロンプト");
        zh(PROMPT_FROM_PROFILE, "来自配置文件的提示词");

        ja(PROMPT_OVERRIDE, "プロンプトの上書き");
        zh(PROMPT_OVERRIDE, "提示词覆盖");

        ja(USE_STANDARD_PROMPT_INFO, "標準のプロンプト情報を使用");
        zh(USE_STANDARD_PROMPT_INFO, "使用标准提示词信息");

        ja(INCLUDE_LOREBOOK, "ロアブックを含める");
        zh(INCLUDE_LOREBOOK, "包含世界书");

        ja(WITH_SUMMARIES, "要約を含める");
        zh(WITH_SUMMARIES, "包含摘要");

        ja(SEND_THE_SUMMARIES, "生成と同様に、パートより前の物語の要約を送り、その後に最新の要約より後のパートを送ります（収まる限りの最新のパートを送る代わりに）");
        zh(SEND_THE_SUMMARIES, "与生成时一样，先发送该段落之前故事的摘要，再发送最新摘要之后的段落（而不是发送尽可能多的最新段落）");

        ja(SEND_ORIGINAL_TEXT, "原文を送る");
        zh(SEND_ORIGINAL_TEXT, "发送原文");

        ja(SEND_THE_TEXT_AS_STORED, "モデルが読む英語の代わりに、翻訳後の言語または人工言語の台詞を含む、保存されているままの物語のテキストを送ります");
        zh(SEND_THE_TEXT_AS_STORED, "发送故事的存储原样文本（对白为翻译后的语言或构造语言），而不是模型所读取的英文");

        ja(TOKEN_LIMIT, "トークン上限");
        zh(TOKEN_LIMIT, "词元上限");

        ja(GENERATE, "生成");
        zh(GENERATE, "生成");

        ja(TOTAL_TOKENS, "合計トークン数: %s");
        zh(TOTAL_TOKENS, "总词元数：%s");

        ja(REVIEWER_GENERATE_A_REVIEW, "レビュアー: レビューを生成");
        zh(REVIEWER_GENERATE_A_REVIEW, "审阅器：生成审阅");
    }
}
