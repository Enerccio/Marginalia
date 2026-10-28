package com.github.enerccio.marginalia.extensions.sidequery;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Side Query in the languages besides English; the English text is the key.
 */
public class SideQueryLocalization extends PluginLocalizationBundle {

    public static final SideQueryLocalization T = new SideQueryLocalization();

    @Override
    protected void loadKeys() {
        ja("Side Query", "サイドクエリ");
        zh("Side Query", "侧边查询");

        ja("SideQuery Settings", "サイドクエリ設定");
        zh("SideQuery Settings", "侧边查询设置");

        ja("Profile", "プロファイル");
        zh("Profile", "配置文件");

        ja("New", "新規");
        zh("New", "新建");

        ja("Rename", "名前を変更");
        zh("Rename", "重命名");

        ja("Delete", "削除");
        zh("Delete", "删除");

        ja("SideQuery Model (Default: Manuscript AI)", "サイドクエリのモデル（デフォルト: 原稿のAI）");
        zh("SideQuery Model (Default: Manuscript AI)", "侧边查询模型（默认：稿件的 AI）");

        ja("SideQuery Protocol (Default: Manuscript Protocol)", "サイドクエリのプロトコル（デフォルト: 原稿のプロトコル）");
        zh("SideQuery Protocol (Default: Manuscript Protocol)", "侧边查询协议（默认：稿件的协议）");

        ja("Initial System Query", "最初のシステムクエリ");
        zh("Initial System Query", "初始系统查询");

        ja("Instructions Before User Input", "ユーザー入力の前の指示");
        zh("Instructions Before User Input", "用户输入之前的指令");

        ja("Enable AI Tab Naming", "AIによるタブ名の自動設定を有効にする");
        zh("Enable AI Tab Naming", "启用 AI 自动命名标签页");

        ja("New SideQuery Profile", "新しいサイドクエリのプロファイル");
        zh("New SideQuery Profile", "新建侧边查询配置文件");

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

        ja("Rename SideQuery Profile", "サイドクエリのプロファイル名を変更");
        zh("Rename SideQuery Profile", "重命名侧边查询配置文件");

        ja("New Profile Name", "新しいプロファイル名");
        zh("New Profile Name", "新配置文件名称");

        ja("Cannot delete %s profile", "%s プロファイルは削除できません");
        zh("Cannot delete %s profile", "无法删除 %s 配置文件");

        ja("Move message up", "メッセージを上へ移動");
        zh("Move message up", "上移消息");

        ja("Move message down", "メッセージを下へ移動");
        zh("Move message down", "下移消息");

        ja("Edit message", "メッセージを編集");
        zh("Edit message", "编辑消息");

        ja("Copied to clipboard", "クリップボードにコピーしました");
        zh("Copied to clipboard", "已复制到剪贴板");

        ja("Copy message", "メッセージをコピー");
        zh("Copy message", "复制消息");

        ja("Thinking Process", "思考プロセス");
        zh("Thinking Process", "思考过程");

        ja("User", "ユーザー");
        zh("User", "用户");

        ja("AI", "AI");
        zh("AI", "AI");

        ja("%s (Editing)", "%s（編集中）");
        zh("%s (Editing)", "%s（编辑中）");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Exclude from context", "コンテキストから除外");
        zh("Exclude from context", "从上下文中排除");

        ja("Include in context", "コンテキストに含める");
        zh("Include in context", "包含在上下文中");

        ja("Lorebook", "ロアブック");
        zh("Lorebook", "世界书");

        ja("Chat Logs", "チャットログ");
        zh("Chat Logs", "聊天记录");

        ja("Story parts from - to, numbered from 1 as in the outline", "開始〜終了のストーリーパート。アウトラインと同様に1から番号が付きます");
        zh("Story parts from - to, numbered from 1 as in the outline", "从 - 到的故事部分，编号从 1 开始，与大纲一致");

        ja("First story part (from 1)", "最初のストーリーパート（1から）");
        zh("First story part (from 1)", "第一个故事部分（从 1 开始）");

        ja("Last story part", "最後のストーリーパート");
        zh("Last story part", "最后一个故事部分");

        ja("to", "〜");
        zh("to", "至");

        ja("Tokens: 0", "トークン: 0");
        zh("Tokens: 0", "词元：0");

        ja("Tokens: ~%d", "トークン: 約%d");
        zh("Tokens: ~%d", "词元：约 %d");

        ja("-- Select Saved Query --", "-- 保存済みクエリを選択 --");
        zh("-- Select Saved Query --", "-- 选择已保存的查询 --");

        ja("Save prompt", "プロンプトを保存");
        zh("Save prompt", "保存提示词");

        ja("Save prompt as...", "プロンプトを名前を付けて保存...");
        zh("Save prompt as...", "提示词另存为...");

        ja("Delete saved prompt", "保存済みプロンプトを削除");
        zh("Delete saved prompt", "删除已保存的提示词");

        ja("Enter query...", "クエリを入力...");
        zh("Enter query...", "输入查询……");

        ja("Cannot save an empty query", "空のクエリは保存できません");
        zh("Cannot save an empty query", "无法保存空查询");

        ja("Saved query: %s", "クエリを保存しました: %s");
        zh("Saved query: %s", "已保存查询：%s");

        ja("Save Query As", "クエリを名前を付けて保存");
        zh("Save Query As", "查询另存为");

        ja("Query Name", "クエリ名");
        zh("Query Name", "查询名称");

        ja("Name cannot be empty", "名前は空にできません");
        zh("Name cannot be empty", "名称不能为空");

        ja("Select a saved query to delete", "削除する保存済みクエリを選択してください");
        zh("Select a saved query to delete", "请选择要删除的已保存查询");

        ja("Deleted query: %s", "クエリを削除しました: %s");
        zh("Deleted query: %s", "已删除查询：%s");

        ja("Generating response...", "応答を生成中...");
        zh("Generating response...", "正在生成回复……");

        ja("Error: %s", "エラー: %s");
        zh("Error: %s", "错误：%s");

        ja("Failed to start inference: %s", "推論を開始できませんでした: %s");
        zh("Failed to start inference: %s", "无法启动推理：%s");

        ja("Additional sources", "追加ソース");
        zh("Additional sources", "其他来源");

        ja("Additional sources: data of other extensions", "追加ソース: 他の拡張機能のデータ");
        zh("Additional sources: data of other extensions", "其他来源：其他扩展的数据");

        ja("No other extension offers data.", "データを提供している他の拡張機能はありません。");
        zh("No other extension offers data.", "没有其他扩展提供数据。");

        ja("About the book", "ブックについて");
        zh("About the book", "关于本书");

        ja("About story parts (added to each included part, needs Chat Logs)", "ストーリーパートについて（含まれる各パートに追加されます。チャットログが必要です）");
        zh("About story parts (added to each included part, needs Chat Logs)", "关于故事部分（添加到每个包含的部分，需要聊天记录）");

        ja("STOP", "停止");
        zh("STOP", "停止");

        ja("SEND", "送信");
        zh("SEND", "发送");

        ja("Rename Tab", "タブ名を変更");
        zh("Rename Tab", "重命名标签页");

        ja("Tab Name", "タブ名");
        zh("Tab Name", "标签页名称");

        ja("Cannot close the last tab", "最後のタブは閉じられません");
        zh("Cannot close the last tab", "无法关闭最后一个标签页");
    }
}
