package com.github.enerccio.marginalia.extensions.lorebookvcs;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Lorebook VCS in the languages besides English; the English text is the key.
 */
public class LorebookVCSLocalization extends PluginLocalizationBundle {

    public static final LorebookVCSLocalization T = new LorebookVCSLocalization();

    public static final String LOREBOOK_REVISIONS = "Lorebook Revisions";
    public static final String EXPORT_VCS_HISTORY_JSON = "Export VCS History JSON";
    public static final String IMPORT_VCS_HISTORY_JSON = "Import VCS History JSON";
    public static final String IMPORT_MARGINALIA_VCS_JSON = "Import Marginalia VCS JSON";
    public static final String IMPORT_SILLYTAVERN_VCS_EXTENSION = "Import SillyTavern VCS Extension JSON";
    public static final String NO_ACTIVE_LOREBOOK_SELECTED = "No active lorebook selected";
    public static final String IMPORT_SILLYTAVERN_VCS_HISTORY = "Import SillyTavern VCS History into %s";
    public static final String IMPORT_LOREBOOK_VCS_HISTORY = "Import Lorebook VCS History into %s";
    public static final String INVALID_VCS_HISTORY_FORMAT = "Invalid VCS history format";
    public static final String THE_IMPORT_FILE_CONTAINS_REVISION = "The import file contains revision history of another lorebook (UUID '%s'). Its revisions will be mapped onto the entries of '%s' by entry name and order.";
    public static final String ENTRY_HISTORIES_MATCH_NO_ENTRY = "%d entry histories match no entry of this lorebook and will be skipped.";
    public static final String THE_EXISTING_REVISION_HISTORY = "The existing revision history of '%s' will be replaced.";
    public static final String IMPORTED_VCS_HISTORY_SUCCESSFULLY = "Imported VCS history successfully";
    public static final String ARE_YOU_SURE = "Are you sure?";
    public static final String CANCEL = "Cancel";
    public static final String REVISION = "Revision:";
    public static final String NONE = "None";
    public static final String DELETE_CURRENT_REVISION = "Delete current revision";
    public static final String PREVIOUS_REVISION = "Previous revision";
    public static final String NEXT_REVISION = "Next revision";
    public static final String CREATE_SNAPSHOT_REVISION = "Create snapshot revision";
    public static final String REV = "Rev %d (%s)";
    public static final String THE_LOREBOOK_IS_WORKED = "The lorebook is worked on by an agent";
    public static final String MODIFIED = "Modified: %s";
    public static final String REVISION_SNAPSHOT_CREATED = "Revision snapshot created";
    public static final String CANNOT_DELETE_THE_LAST_REMAINING = "Cannot delete the last remaining revision";

    @Override
    protected void loadKeys() {
        ja(LOREBOOK_REVISIONS, "ロアブックのリビジョン");
        zh(LOREBOOK_REVISIONS, "世界书修订");

        ja(EXPORT_VCS_HISTORY_JSON, "VCS履歴JSONをエクスポート");
        zh(EXPORT_VCS_HISTORY_JSON, "导出 VCS 历史 JSON");

        ja(IMPORT_VCS_HISTORY_JSON, "VCS履歴JSONをインポート");
        zh(IMPORT_VCS_HISTORY_JSON, "导入 VCS 历史 JSON");

        ja(IMPORT_MARGINALIA_VCS_JSON, "Marginalia VCS JSONをインポート");
        zh(IMPORT_MARGINALIA_VCS_JSON, "导入 Marginalia VCS JSON");

        ja(IMPORT_SILLYTAVERN_VCS_EXTENSION, "SillyTavern VCS拡張機能のJSONをインポート");
        zh(IMPORT_SILLYTAVERN_VCS_EXTENSION, "导入 SillyTavern VCS 扩展 JSON");

        ja(NO_ACTIVE_LOREBOOK_SELECTED, "アクティブなロアブックが選択されていません");
        zh(NO_ACTIVE_LOREBOOK_SELECTED, "未选择活动世界书");

        ja(IMPORT_SILLYTAVERN_VCS_HISTORY, "SillyTavern VCS履歴を「%s」にインポート");
        zh(IMPORT_SILLYTAVERN_VCS_HISTORY, "将 SillyTavern VCS 历史导入“%s”");

        ja(IMPORT_LOREBOOK_VCS_HISTORY, "ロアブックVCS履歴を「%s」にインポート");
        zh(IMPORT_LOREBOOK_VCS_HISTORY, "将世界书 VCS 历史导入“%s”");

        ja(INVALID_VCS_HISTORY_FORMAT, "VCS履歴の形式が正しくありません");
        zh(INVALID_VCS_HISTORY_FORMAT, "VCS 历史格式无效");

        ja(THE_IMPORT_FILE_CONTAINS_REVISION, "インポートファイルには別のロアブック（UUID '%s'）のリビジョン履歴が含まれています。そのリビジョンはエントリ名と順序に基づいて '%s' のエントリに対応付けられます。");
        zh(THE_IMPORT_FILE_CONTAINS_REVISION, "导入文件包含另一个世界书（UUID '%s'）的修订历史。其修订将按条目名称和顺序映射到“%s”的条目上。");

        ja(ENTRY_HISTORIES_MATCH_NO_ENTRY, "%d件のエントリ履歴はこのロアブックのどのエントリにも一致しないため、スキップされます。");
        zh(ENTRY_HISTORIES_MATCH_NO_ENTRY, "有 %d 条条目历史与此世界书中的任何条目都不匹配，将被跳过。");

        ja(THE_EXISTING_REVISION_HISTORY, "'%s' の既存のリビジョン履歴は置き換えられます。");
        zh(THE_EXISTING_REVISION_HISTORY, "“%s”现有的修订历史将被替换。");

        ja(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS履歴をインポートしました");
        zh(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS 历史导入成功");

        ja(ARE_YOU_SURE, "よろしいですか？");
        zh(ARE_YOU_SURE, "确定吗？");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(REVISION, "リビジョン:");
        zh(REVISION, "修订：");

        ja(NONE, "なし");
        zh(NONE, "无");

        ja(DELETE_CURRENT_REVISION, "現在のリビジョンを削除");
        zh(DELETE_CURRENT_REVISION, "删除当前修订");

        ja(PREVIOUS_REVISION, "前のリビジョン");
        zh(PREVIOUS_REVISION, "上一个修订");

        ja(NEXT_REVISION, "次のリビジョン");
        zh(NEXT_REVISION, "下一个修订");

        ja(CREATE_SNAPSHOT_REVISION, "スナップショットのリビジョンを作成");
        zh(CREATE_SNAPSHOT_REVISION, "创建快照修订");

        ja(REV, "リビジョン %d（%s）");
        zh(REV, "修订 %d（%s）");

        ja(THE_LOREBOOK_IS_WORKED, "このロアブックはエージェントが作業中です");
        zh(THE_LOREBOOK_IS_WORKED, "此世界书正在由代理处理");

        ja(MODIFIED, "更新日時: %s");
        zh(MODIFIED, "修改时间：%s");

        ja(REVISION_SNAPSHOT_CREATED, "リビジョンのスナップショットを作成しました");
        zh(REVISION_SNAPSHOT_CREATED, "已创建修订快照");

        ja(CANNOT_DELETE_THE_LAST_REMAINING, "最後に残ったリビジョンは削除できません");
        zh(CANNOT_DELETE_THE_LAST_REMAINING, "无法删除最后一个修订");
    }
}
