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
        fr(LOREBOOK_REVISIONS, "Révisions du lorebook");
        es(LOREBOOK_REVISIONS, "Revisiones del lorebook");
        de(LOREBOOK_REVISIONS, "Lorebook-Revisionen");
        ko(LOREBOOK_REVISIONS, "로어북 리비전");

        ja(EXPORT_VCS_HISTORY_JSON, "VCS履歴JSONをエクスポート");
        zh(EXPORT_VCS_HISTORY_JSON, "导出 VCS 历史 JSON");
        fr(EXPORT_VCS_HISTORY_JSON, "Exporter l'historique VCS en JSON");
        es(EXPORT_VCS_HISTORY_JSON, "Exportar el historial VCS en JSON");
        de(EXPORT_VCS_HISTORY_JSON, "VCS-Verlauf als JSON exportieren");
        ko(EXPORT_VCS_HISTORY_JSON, "VCS 기록 JSON 내보내기");

        ja(IMPORT_VCS_HISTORY_JSON, "VCS履歴JSONをインポート");
        zh(IMPORT_VCS_HISTORY_JSON, "导入 VCS 历史 JSON");
        fr(IMPORT_VCS_HISTORY_JSON, "Importer l'historique VCS en JSON");
        es(IMPORT_VCS_HISTORY_JSON, "Importar el historial VCS en JSON");
        de(IMPORT_VCS_HISTORY_JSON, "VCS-Verlauf als JSON importieren");
        ko(IMPORT_VCS_HISTORY_JSON, "VCS 기록 JSON 가져오기");

        ja(IMPORT_MARGINALIA_VCS_JSON, "Marginalia VCS JSONをインポート");
        zh(IMPORT_MARGINALIA_VCS_JSON, "导入 Marginalia VCS JSON");
        fr(IMPORT_MARGINALIA_VCS_JSON, "Importer un JSON VCS Marginalia");
        es(IMPORT_MARGINALIA_VCS_JSON, "Importar el JSON VCS de Marginalia");
        de(IMPORT_MARGINALIA_VCS_JSON, "Marginalia-VCS-JSON importieren");
        ko(IMPORT_MARGINALIA_VCS_JSON, "Marginalia VCS JSON 가져오기");

        ja(IMPORT_SILLYTAVERN_VCS_EXTENSION, "SillyTavern VCS拡張機能のJSONをインポート");
        zh(IMPORT_SILLYTAVERN_VCS_EXTENSION, "导入 SillyTavern VCS 扩展 JSON");
        fr(IMPORT_SILLYTAVERN_VCS_EXTENSION, "Importer un JSON de l'extension VCS de SillyTavern");
        es(IMPORT_SILLYTAVERN_VCS_EXTENSION, "Importar el JSON de la extensión VCS de SillyTavern");
        de(IMPORT_SILLYTAVERN_VCS_EXTENSION, "JSON der SillyTavern-VCS-Erweiterung importieren");
        ko(IMPORT_SILLYTAVERN_VCS_EXTENSION, "SillyTavern VCS 확장 JSON 가져오기");

        ja(NO_ACTIVE_LOREBOOK_SELECTED, "アクティブなロアブックが選択されていません");
        zh(NO_ACTIVE_LOREBOOK_SELECTED, "未选择活动世界书");
        fr(NO_ACTIVE_LOREBOOK_SELECTED, "Aucun lorebook actif sélectionné");
        es(NO_ACTIVE_LOREBOOK_SELECTED, "No hay ningún lorebook activo seleccionado");
        de(NO_ACTIVE_LOREBOOK_SELECTED, "Kein aktives Lorebook ausgewählt");
        ko(NO_ACTIVE_LOREBOOK_SELECTED, "선택된 활성 로어북이 없습니다");

        ja(IMPORT_SILLYTAVERN_VCS_HISTORY, "SillyTavern VCS履歴を「%s」にインポート");
        zh(IMPORT_SILLYTAVERN_VCS_HISTORY, "将 SillyTavern VCS 历史导入“%s”");
        fr(IMPORT_SILLYTAVERN_VCS_HISTORY, "Importer l'historique VCS de SillyTavern dans %s");
        es(IMPORT_SILLYTAVERN_VCS_HISTORY, "Importar el historial VCS de SillyTavern en %s");
        de(IMPORT_SILLYTAVERN_VCS_HISTORY, "SillyTavern-VCS-Verlauf in %s importieren");
        ko(IMPORT_SILLYTAVERN_VCS_HISTORY, "SillyTavern VCS 기록을 %s(으)로 가져오기");

        ja(IMPORT_LOREBOOK_VCS_HISTORY, "ロアブックVCS履歴を「%s」にインポート");
        zh(IMPORT_LOREBOOK_VCS_HISTORY, "将世界书 VCS 历史导入“%s”");
        fr(IMPORT_LOREBOOK_VCS_HISTORY, "Importer l'historique VCS du lorebook dans %s");
        es(IMPORT_LOREBOOK_VCS_HISTORY, "Importar el historial VCS del lorebook en %s");
        de(IMPORT_LOREBOOK_VCS_HISTORY, "Lorebook-VCS-Verlauf in %s importieren");
        ko(IMPORT_LOREBOOK_VCS_HISTORY, "로어북 VCS 기록을 %s(으)로 가져오기");

        ja(INVALID_VCS_HISTORY_FORMAT, "VCS履歴の形式が正しくありません");
        zh(INVALID_VCS_HISTORY_FORMAT, "VCS 历史格式无效");
        fr(INVALID_VCS_HISTORY_FORMAT, "Format d'historique VCS invalide");
        es(INVALID_VCS_HISTORY_FORMAT, "Formato de historial VCS no válido");
        de(INVALID_VCS_HISTORY_FORMAT, "Ungültiges Format des VCS-Verlaufs");
        ko(INVALID_VCS_HISTORY_FORMAT, "잘못된 VCS 기록 형식");

        ja(THE_IMPORT_FILE_CONTAINS_REVISION, "インポートファイルには別のロアブック（UUID '%s'）のリビジョン履歴が含まれています。そのリビジョンはエントリ名と順序に基づいて '%s' のエントリに対応付けられます。");
        zh(THE_IMPORT_FILE_CONTAINS_REVISION, "导入文件包含另一个世界书（UUID '%s'）的修订历史。其修订将按条目名称和顺序映射到“%s”的条目上。");
        fr(THE_IMPORT_FILE_CONTAINS_REVISION, "Le fichier importé contient l'historique des révisions d'un autre lorebook (UUID '%s'). Ses révisions seront associées aux entrées de '%s' par nom et ordre des entrées.");
        es(THE_IMPORT_FILE_CONTAINS_REVISION, "El archivo de importación contiene el historial de revisiones de otro lorebook (UUID '%s'). Sus revisiones se asignarán a las entradas de '%s' por nombre de entrada y orden.");
        de(THE_IMPORT_FILE_CONTAINS_REVISION, "Die Importdatei enthält den Revisionsverlauf eines anderen Lorebooks (UUID „%s“). Seine Revisionen werden anhand von Eintragsname und Reihenfolge auf die Einträge von „%s“ abgebildet.");
        ko(THE_IMPORT_FILE_CONTAINS_REVISION, "가져오기 파일에 다른 로어북(UUID '%s')의 리비전 기록이 들어 있습니다. 리비전은 항목 이름과 순서에 따라 '%s'의 항목에 매핑됩니다.");

        ja(ENTRY_HISTORIES_MATCH_NO_ENTRY, "%d件のエントリ履歴はこのロアブックのどのエントリにも一致しないため、スキップされます。");
        zh(ENTRY_HISTORIES_MATCH_NO_ENTRY, "有 %d 条条目历史与此世界书中的任何条目都不匹配，将被跳过。");
        fr(ENTRY_HISTORIES_MATCH_NO_ENTRY, "%d historiques d'entrée ne correspondent à aucune entrée de ce lorebook et seront ignorés.");
        es(ENTRY_HISTORIES_MATCH_NO_ENTRY, "%d historiales de entradas no coinciden con ninguna entrada de este lorebook y se omitirán.");
        de(ENTRY_HISTORIES_MATCH_NO_ENTRY, "%d Eintragsverläufe passen zu keinem Eintrag dieses Lorebooks und werden übersprungen.");
        ko(ENTRY_HISTORIES_MATCH_NO_ENTRY, "항목 기록 %d개가 이 로어북의 어떤 항목과도 일치하지 않아 건너뜁니다.");

        ja(THE_EXISTING_REVISION_HISTORY, "'%s' の既存のリビジョン履歴は置き換えられます。");
        zh(THE_EXISTING_REVISION_HISTORY, "“%s”现有的修订历史将被替换。");
        fr(THE_EXISTING_REVISION_HISTORY, "L'historique des révisions existant de '%s' sera remplacé.");
        es(THE_EXISTING_REVISION_HISTORY, "El historial de revisiones existente de '%s' se reemplazará.");
        de(THE_EXISTING_REVISION_HISTORY, "Der vorhandene Revisionsverlauf von „%s“ wird ersetzt.");
        ko(THE_EXISTING_REVISION_HISTORY, "'%s'의 기존 리비전 기록이 교체됩니다.");

        ja(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS履歴をインポートしました");
        zh(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS 历史导入成功");
        fr(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "Historique VCS importé");
        es(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "Historial VCS importado correctamente");
        de(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS-Verlauf erfolgreich importiert");
        ko(IMPORTED_VCS_HISTORY_SUCCESSFULLY, "VCS 기록을 가져왔습니다");

        ja(ARE_YOU_SURE, "よろしいですか？");
        zh(ARE_YOU_SURE, "确定吗？");
        fr(ARE_YOU_SURE, "Êtes-vous sûr ?");
        es(ARE_YOU_SURE, "¿Seguro?");
        de(ARE_YOU_SURE, "Bist du sicher?");
        ko(ARE_YOU_SURE, "계속하시겠습니까?");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

        ja(REVISION, "リビジョン:");
        zh(REVISION, "修订：");
        fr(REVISION, "Révision :");
        es(REVISION, "Revisión:");
        de(REVISION, "Revision:");
        ko(REVISION, "리비전:");

        ja(NONE, "なし");
        zh(NONE, "无");
        fr(NONE, "Aucune");
        es(NONE, "Ninguna");
        de(NONE, "Keine");
        ko(NONE, "없음");

        ja(DELETE_CURRENT_REVISION, "現在のリビジョンを削除");
        zh(DELETE_CURRENT_REVISION, "删除当前修订");
        fr(DELETE_CURRENT_REVISION, "Supprimer la révision actuelle");
        es(DELETE_CURRENT_REVISION, "Eliminar la revisión actual");
        de(DELETE_CURRENT_REVISION, "Aktuelle Revision löschen");
        ko(DELETE_CURRENT_REVISION, "현재 리비전 삭제");

        ja(PREVIOUS_REVISION, "前のリビジョン");
        zh(PREVIOUS_REVISION, "上一个修订");
        fr(PREVIOUS_REVISION, "Révision précédente");
        es(PREVIOUS_REVISION, "Revisión anterior");
        de(PREVIOUS_REVISION, "Vorherige Revision");
        ko(PREVIOUS_REVISION, "이전 리비전");

        ja(NEXT_REVISION, "次のリビジョン");
        zh(NEXT_REVISION, "下一个修订");
        fr(NEXT_REVISION, "Révision suivante");
        es(NEXT_REVISION, "Revisión siguiente");
        de(NEXT_REVISION, "Nächste Revision");
        ko(NEXT_REVISION, "다음 리비전");

        ja(CREATE_SNAPSHOT_REVISION, "スナップショットのリビジョンを作成");
        zh(CREATE_SNAPSHOT_REVISION, "创建快照修订");
        fr(CREATE_SNAPSHOT_REVISION, "Créer une révision instantanée");
        es(CREATE_SNAPSHOT_REVISION, "Crear una revisión instantánea");
        de(CREATE_SNAPSHOT_REVISION, "Snapshot-Revision erstellen");
        ko(CREATE_SNAPSHOT_REVISION, "스냅샷 리비전 만들기");

        ja(REV, "リビジョン %d（%s）");
        zh(REV, "修订 %d（%s）");
        fr(REV, "Rév. %d (%s)");
        es(REV, "Rev %d (%s)");
        de(REV, "Rev. %d (%s)");
        ko(REV, "리비전 %d (%s)");

        ja(THE_LOREBOOK_IS_WORKED, "このロアブックはエージェントが作業中です");
        zh(THE_LOREBOOK_IS_WORKED, "此世界书正在由代理处理");
        fr(THE_LOREBOOK_IS_WORKED, "Un agent travaille sur le lorebook");
        es(THE_LOREBOOK_IS_WORKED, "Un agente está trabajando en el lorebook");
        de(THE_LOREBOOK_IS_WORKED, "An dem Lorebook arbeitet ein Agent");
        ko(THE_LOREBOOK_IS_WORKED, "에이전트가 이 로어북에서 작업 중입니다");

        ja(MODIFIED, "更新日時: %s");
        zh(MODIFIED, "修改时间：%s");
        fr(MODIFIED, "Modifié : %s");
        es(MODIFIED, "Modificado: %s");
        de(MODIFIED, "Geändert: %s");
        ko(MODIFIED, "수정됨: %s");

        ja(REVISION_SNAPSHOT_CREATED, "リビジョンのスナップショットを作成しました");
        zh(REVISION_SNAPSHOT_CREATED, "已创建修订快照");
        fr(REVISION_SNAPSHOT_CREATED, "Révision instantanée créée");
        es(REVISION_SNAPSHOT_CREATED, "Revisión instantánea creada");
        de(REVISION_SNAPSHOT_CREATED, "Revisions-Snapshot erstellt");
        ko(REVISION_SNAPSHOT_CREATED, "리비전 스냅샷을 만들었습니다");

        ja(CANNOT_DELETE_THE_LAST_REMAINING, "最後に残ったリビジョンは削除できません");
        zh(CANNOT_DELETE_THE_LAST_REMAINING, "无法删除最后一个修订");
        fr(CANNOT_DELETE_THE_LAST_REMAINING, "Impossible de supprimer la dernière révision restante");
        es(CANNOT_DELETE_THE_LAST_REMAINING, "No se puede eliminar la última revisión que queda");
        de(CANNOT_DELETE_THE_LAST_REMAINING, "Die letzte verbleibende Revision kann nicht gelöscht werden");
        ko(CANNOT_DELETE_THE_LAST_REMAINING, "마지막 남은 리비전은 삭제할 수 없습니다");
    }
}
