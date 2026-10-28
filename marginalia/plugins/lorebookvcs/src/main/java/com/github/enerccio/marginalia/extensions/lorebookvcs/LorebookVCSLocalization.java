package com.github.enerccio.marginalia.extensions.lorebookvcs;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Lorebook VCS in the languages besides English; the English text is the key.
 */
public class LorebookVCSLocalization extends PluginLocalizationBundle {

    public static final LorebookVCSLocalization T = new LorebookVCSLocalization();

    @Override
    protected void loadKeys() {
        ja("Lorebook Revisions", "ロアブックのリビジョン");
        zh("Lorebook Revisions", "世界书修订");

        ja("Export VCS History JSON", "VCS履歴JSONをエクスポート");
        zh("Export VCS History JSON", "导出 VCS 历史 JSON");

        ja("Import VCS History JSON", "VCS履歴JSONをインポート");
        zh("Import VCS History JSON", "导入 VCS 历史 JSON");

        ja("Import Marginalia VCS JSON", "Marginalia VCS JSONをインポート");
        zh("Import Marginalia VCS JSON", "导入 Marginalia VCS JSON");

        ja("Import SillyTavern VCS Extension JSON", "SillyTavern VCS拡張機能のJSONをインポート");
        zh("Import SillyTavern VCS Extension JSON", "导入 SillyTavern VCS 扩展 JSON");

        ja("No active lorebook selected", "アクティブなロアブックが選択されていません");
        zh("No active lorebook selected", "未选择活动世界书");

        ja("Import SillyTavern VCS History into %s", "SillyTavern VCS履歴を「%s」にインポート");
        zh("Import SillyTavern VCS History into %s", "将 SillyTavern VCS 历史导入“%s”");

        ja("Import Lorebook VCS History into %s", "ロアブックVCS履歴を「%s」にインポート");
        zh("Import Lorebook VCS History into %s", "将世界书 VCS 历史导入“%s”");

        ja("Invalid VCS history format", "VCS履歴の形式が正しくありません");
        zh("Invalid VCS history format", "VCS 历史格式无效");

        ja("The import file contains revision history of another lorebook (UUID '%s'). Its revisions will be mapped onto the entries of '%s' by entry name and order.", "インポートファイルには別のロアブック（UUID '%s'）のリビジョン履歴が含まれています。そのリビジョンはエントリ名と順序に基づいて '%s' のエントリに対応付けられます。");
        zh("The import file contains revision history of another lorebook (UUID '%s'). Its revisions will be mapped onto the entries of '%s' by entry name and order.", "导入文件包含另一个世界书（UUID '%s'）的修订历史。其修订将按条目名称和顺序映射到“%s”的条目上。");

        ja("%d entry histories match no entry of this lorebook and will be skipped.", "%d件のエントリ履歴はこのロアブックのどのエントリにも一致しないため、スキップされます。");
        zh("%d entry histories match no entry of this lorebook and will be skipped.", "有 %d 条条目历史与此世界书中的任何条目都不匹配，将被跳过。");

        ja("The existing revision history of '%s' will be replaced.", "'%s' の既存のリビジョン履歴は置き換えられます。");
        zh("The existing revision history of '%s' will be replaced.", "“%s”现有的修订历史将被替换。");

        ja("Imported VCS history successfully", "VCS履歴をインポートしました");
        zh("Imported VCS history successfully", "VCS 历史导入成功");

        ja("Are you sure?", "よろしいですか？");
        zh("Are you sure?", "确定吗？");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Revision:", "リビジョン:");
        zh("Revision:", "修订：");

        ja("None", "なし");
        zh("None", "无");

        ja("Delete current revision", "現在のリビジョンを削除");
        zh("Delete current revision", "删除当前修订");

        ja("Previous revision", "前のリビジョン");
        zh("Previous revision", "上一个修订");

        ja("Next revision", "次のリビジョン");
        zh("Next revision", "下一个修订");

        ja("Create snapshot revision", "スナップショットのリビジョンを作成");
        zh("Create snapshot revision", "创建快照修订");

        ja("Rev %d (%s)", "リビジョン %d（%s）");
        zh("Rev %d (%s)", "修订 %d（%s）");

        ja("The lorebook is worked on by an agent", "このロアブックはエージェントが作業中です");
        zh("The lorebook is worked on by an agent", "此世界书正在由代理处理");

        ja("Modified: %s", "更新日時: %s");
        zh("Modified: %s", "修改时间：%s");

        ja("Revision snapshot created", "リビジョンのスナップショットを作成しました");
        zh("Revision snapshot created", "已创建修订快照");

        ja("Cannot delete the last remaining revision", "最後に残ったリビジョンは削除できません");
        zh("Cannot delete the last remaining revision", "无法删除最后一个修订");
    }
}
