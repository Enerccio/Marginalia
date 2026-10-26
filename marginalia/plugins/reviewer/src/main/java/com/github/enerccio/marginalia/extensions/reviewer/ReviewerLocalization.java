package com.github.enerccio.marginalia.extensions.reviewer;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Reviewer in the languages besides English; the English text is the key.
 */
public class ReviewerLocalization extends PluginLocalizationBundle {

    public static final ReviewerLocalization T = new ReviewerLocalization();

    @Override
    protected void loadKeys() {
        ja("Review", "レビュー");
        zh("Review", "评论");

        ja("View / Generate Review", "レビューの表示 / 生成");
        zh("View / Generate Review", "查看 / 生成评论");

        ja("Review: Advanced Options", "レビュー: 詳細オプション");
        zh("Review: Advanced Options", "评论：高级选项");

        ja("Advanced Options", "詳細オプション");
        zh("Advanced Options", "高级选项");

        ja("Delete Review", "レビューを削除");
        zh("Delete Review", "删除评论");

        ja("Reviewer Settings", "レビュアー設定");
        zh("Reviewer Settings", "评论者设置");

        ja("Reviewer Profile (Default: selected in settings)", "レビュアープロファイル（デフォルト: 設定で選択したもの）");
        zh("Reviewer Profile (Default: selected in settings)", "评论者配置文件（默认：在设置中选择的）");

        ja("Message Review", "メッセージのレビュー");
        zh("Message Review", "消息评论");

        ja("Stop generating", "生成を停止");
        zh("Stop generating", "停止生成");

        ja("Thinking Process", "思考プロセス");
        zh("Thinking Process", "思考过程");

        ja("Close", "閉じる");
        zh("Close", "关闭");

        ja("Generating review, please wait...", "レビューを生成中です。お待ちください...");
        zh("Generating review, please wait...", "正在生成评论，请稍候……");

        ja("Error generating review: %s", "レビューの生成中にエラーが発生しました: %s");
        zh("Error generating review: %s", "生成评论时出错：%s");

        ja("Failed to start inference: %s", "推論を開始できませんでした: %s");
        zh("Failed to start inference: %s", "无法启动推理：%s");

        ja("Profile", "プロファイル");
        zh("Profile", "配置文件");

        ja("New", "新規");
        zh("New", "新建");

        ja("Rename", "名前を変更");
        zh("Rename", "重命名");

        ja("Delete", "削除");
        zh("Delete", "删除");

        ja("Reviewer Model (Default: Manuscript AI)", "レビュアーモデル（デフォルト: 原稿のAI）");
        zh("Reviewer Model (Default: Manuscript AI)", "评论者模型（默认：稿件的 AI）");

        ja("Reviewer Protocol (Default: Manuscript Protocol)", "レビュアープロトコル（デフォルト: 原稿のプロトコル）");
        zh("Reviewer Protocol (Default: Manuscript Protocol)", "评论者协议（默认：稿件的协议）");

        ja("Review Pre-Prompt (System)", "レビューの前置プロンプト（システム）");
        zh("Review Pre-Prompt (System)", "评论前置提示词（系统）");

        ja("Review Post-Prompt (User)", "レビューの後置プロンプト（ユーザー）");
        zh("Review Post-Prompt (User)", "评论后置提示词（用户）");

        ja("New Profile", "新しいプロファイル");
        zh("New Profile", "新建配置文件");

        ja("Profile Name", "プロファイル名");
        zh("Profile Name", "配置文件名称");

        ja("Create", "作成");
        zh("Create", "创建");

        ja("Profile name cannot be empty", "プロファイル名は空にできません");
        zh("Profile name cannot be empty", "配置文件名称不能为空");

        ja("Profile already exists", "プロファイルは既に存在します");
        zh("Profile already exists", "配置文件已存在");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Cannot rename %s profile", "%s プロファイルの名前は変更できません");
        zh("Cannot rename %s profile", "无法重命名 %s 配置文件");

        ja("Rename Profile", "プロファイル名を変更");
        zh("Rename Profile", "重命名配置文件");

        ja("New Profile Name", "新しいプロファイル名");
        zh("New Profile Name", "新配置文件名称");

        ja("Cannot delete %s profile", "%s プロファイルは削除できません");
        zh("Cannot delete %s profile", "无法删除 %s 配置文件");

        ja("Advanced Review Options", "レビューの詳細オプション");
        zh("Advanced Review Options", "高级评论选项");

        ja("Prompt from Profile", "プロファイルからのプロンプト");
        zh("Prompt from Profile", "来自配置文件的提示词");

        ja("Prompt Override", "プロンプトの上書き");
        zh("Prompt Override", "提示词覆盖");

        ja("Use standard prompt info", "標準のプロンプト情報を使用");
        zh("Use standard prompt info", "使用标准提示词信息");

        ja("Include Lorebook", "ロアブックを含める");
        zh("Include Lorebook", "包含世界书");

        ja("Token Limit", "トークン上限");
        zh("Token Limit", "词元上限");

        ja("Generate", "生成");
        zh("Generate", "生成");

        ja("Total tokens: %s", "合計トークン数: %s");
        zh("Total tokens: %s", "总词元数：%s");
    }
}
