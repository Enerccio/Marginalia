package com.github.enerccio.marginalia.extensions.storynotes;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Story Notes in the languages besides English; the English text is the key.
 */
public class StoryNotesLocalization extends PluginLocalizationBundle {

    public static final StoryNotesLocalization T = new StoryNotesLocalization();

    public static final String NOTES = "Notes";
    public static final String ADD = "Add";
    public static final String ADD_NEW_NOTE = "Add a new note";
    public static final String NEW_NOTE = "New note";
    public static final String EDIT_NOTE = "Edit note";
    public static final String DELETE_NOTE = "Delete note";
    public static final String DELETE_THE_NOTE_PAGE = "Delete the note %s?";
    public static final String SEARCH_NOTES = "Search notes";
    public static final String ALL_TAGS = "All tags";
    public static final String TITLE = "Title";
    public static final String TAGS = "Tags";
    public static final String SEPARATED_BY_COMMAS = "Separated by commas";
    public static final String TEXT_MARKDOWN_LINKS = "Text (Markdown, [[Title]] links to another note)";
    public static final String WRITE = "Write";
    public static final String PREVIEW = "Preview";
    public static final String VISIBLE_TO_ASSISTANTS = "Visible to assistants";
    public static final String VISIBLE_TO_ASSISTANTS_TIP = "Commands, agents and the plot wizard can read and change this note. "
            + "Switch it off for a note that only you should see.";
    public static final String HIDDEN_FROM_ASSISTANTS = "Hidden from assistants";
    public static final String CHANGED_BY_A_COMMAND = "Changed by a command (an agent or the plot wizard), not by you in the editor";
    public static final String LINKS_FROM_THIS_NOTE = "Links from this note";
    public static final String LINKED_FROM = "Linked from";
    public static final String CREATE_THE_NOTE = "Create %s";
    public static final String NO_NOTES_YET = "No notes yet. Add a page for a plot, an idea or a rule of the world.";
    public static final String NO_NOTE_MATCHES = "No note matches";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String THERE_ARE_UNSAVED_CHANGES_DISCARD = "There are unsaved changes. Discard them?";
    public static final String CHANGED_WHILE_EDITING_OVERWRITE = "The note was changed while you were editing it. Overwrite those changes with yours?";
    public static final String ANOTHER_NOTE_HAS_THIS_TITLE = "Another note already has this title";
    public static final String THE_TITLE_IS_REQUIRED = "The title is required";
    public static final String CHARACTERS_COUNT = "%d characters";

    @Override
    protected void loadKeys() {
        ja(NOTES, "ノート");
        zh(NOTES, "笔记");
        fr(NOTES, "Notes");
        es(NOTES, "Notas");
        de(NOTES, "Notizen");
        ko(NOTES, "메모");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajouter");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");

        ja(ADD_NEW_NOTE, "新しいノートを追加");
        zh(ADD_NEW_NOTE, "添加新笔记");
        fr(ADD_NEW_NOTE, "Ajouter une nouvelle note");
        es(ADD_NEW_NOTE, "Añadir una nota nueva");
        de(ADD_NEW_NOTE, "Eine neue Notiz hinzufügen");
        ko(ADD_NEW_NOTE, "새 메모 추가");

        ja(NEW_NOTE, "新しいノート");
        zh(NEW_NOTE, "新笔记");
        fr(NEW_NOTE, "Nouvelle note");
        es(NEW_NOTE, "Nueva nota");
        de(NEW_NOTE, "Neue Notiz");
        ko(NEW_NOTE, "새 메모");

        ja(EDIT_NOTE, "ノートを編集");
        zh(EDIT_NOTE, "编辑笔记");
        fr(EDIT_NOTE, "Modifier la note");
        es(EDIT_NOTE, "Editar nota");
        de(EDIT_NOTE, "Notiz bearbeiten");
        ko(EDIT_NOTE, "메모 편집");

        ja(DELETE_NOTE, "ノートを削除");
        zh(DELETE_NOTE, "删除笔记");
        fr(DELETE_NOTE, "Supprimer la note");
        es(DELETE_NOTE, "Eliminar nota");
        de(DELETE_NOTE, "Notiz löschen");
        ko(DELETE_NOTE, "메모 삭제");

        ja(DELETE_THE_NOTE_PAGE, "ノート「%s」を削除しますか？");
        zh(DELETE_THE_NOTE_PAGE, "删除笔记“%s”吗？");
        fr(DELETE_THE_NOTE_PAGE, "Supprimer la note %s ?");
        es(DELETE_THE_NOTE_PAGE, "¿Eliminar la nota %s?");
        de(DELETE_THE_NOTE_PAGE, "Die Notiz %s löschen?");
        ko(DELETE_THE_NOTE_PAGE, "메모 %s을(를) 삭제하시겠습니까?");

        ja(SEARCH_NOTES, "ノートを検索");
        zh(SEARCH_NOTES, "搜索笔记");
        fr(SEARCH_NOTES, "Rechercher dans les notes");
        es(SEARCH_NOTES, "Buscar notas");
        de(SEARCH_NOTES, "Notizen durchsuchen");
        ko(SEARCH_NOTES, "메모 검색");

        ja(ALL_TAGS, "すべてのタグ");
        zh(ALL_TAGS, "所有标签");
        fr(ALL_TAGS, "Tous les mots-clés");
        es(ALL_TAGS, "Todas las etiquetas");
        de(ALL_TAGS, "Alle Tags");
        ko(ALL_TAGS, "모든 태그");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");
        fr(TITLE, "Titre");
        es(TITLE, "Título");
        de(TITLE, "Titel");
        ko(TITLE, "제목");

        ja(TAGS, "タグ");
        zh(TAGS, "标签");
        fr(TAGS, "Mots-clés");
        es(TAGS, "Etiquetas");
        de(TAGS, "Tags");
        ko(TAGS, "태그");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");
        fr(SEPARATED_BY_COMMAS, "Séparés par des virgules");
        es(SEPARATED_BY_COMMAS, "Separadas por comas");
        de(SEPARATED_BY_COMMAS, "Durch Kommas getrennt");
        ko(SEPARATED_BY_COMMAS, "쉼표로 구분");

        ja(TEXT_MARKDOWN_LINKS, "本文（Markdown、[[タイトル]]で他のノートにリンク）");
        zh(TEXT_MARKDOWN_LINKS, "正文（Markdown，[[标题]] 链接到其他笔记）");
        fr(TEXT_MARKDOWN_LINKS, "Texte (Markdown, [[Titre]] renvoie à une autre note)");
        es(TEXT_MARKDOWN_LINKS, "Texto (Markdown, [[Título]] enlaza con otra nota)");
        de(TEXT_MARKDOWN_LINKS, "Text (Markdown, [[Titel]] verlinkt auf eine andere Notiz)");
        ko(TEXT_MARKDOWN_LINKS, "텍스트 (Markdown, [[제목]]은 다른 메모로 연결)");

        ja(WRITE, "書く");
        zh(WRITE, "编写");
        fr(WRITE, "Écrire");
        es(WRITE, "Escribir");
        de(WRITE, "Schreiben");
        ko(WRITE, "쓰기");

        ja(PREVIEW, "プレビュー");
        zh(PREVIEW, "预览");
        fr(PREVIEW, "Aperçu");
        es(PREVIEW, "Vista previa");
        de(PREVIEW, "Vorschau");
        ko(PREVIEW, "미리 보기");

        ja(VISIBLE_TO_ASSISTANTS, "アシスタントに公開");
        zh(VISIBLE_TO_ASSISTANTS, "对助手可见");
        fr(VISIBLE_TO_ASSISTANTS, "Visible par les assistants");
        es(VISIBLE_TO_ASSISTANTS, "Visible para los asistentes");
        de(VISIBLE_TO_ASSISTANTS, "Für Assistenten sichtbar");
        ko(VISIBLE_TO_ASSISTANTS, "어시스턴트에게 공개");

        ja(VISIBLE_TO_ASSISTANTS_TIP, "コマンド、エージェント、プロットウィザードがこのノートを読み書きできます。自分だけが見るノートはオフにしてください。");
        zh(VISIBLE_TO_ASSISTANTS_TIP, "命令、代理和情节向导可以读取并修改此笔记。仅供自己查看的笔记请关闭。");
        fr(VISIBLE_TO_ASSISTANTS_TIP, "Les commandes, les agents et l'assistant d'intrigue peuvent lire et modifier cette note. Désactivez-le pour une note que vous seul devez voir.");
        es(VISIBLE_TO_ASSISTANTS_TIP, "Los comandos, los agentes y el asistente de trama pueden leer y cambiar esta nota. Desactívalo para una nota que solo debas ver tú.");
        de(VISIBLE_TO_ASSISTANTS_TIP, "Befehle, Agenten und der Plot-Assistent können diese Notiz lesen und ändern. Schalte es für eine Notiz aus, die nur du sehen sollst.");
        ko(VISIBLE_TO_ASSISTANTS_TIP, "명령, 에이전트, 줄거리 마법사가 이 메모를 읽고 변경할 수 있습니다. 나만 봐야 하는 메모는 끄세요.");

        ja(HIDDEN_FROM_ASSISTANTS, "アシスタントには非公開");
        zh(HIDDEN_FROM_ASSISTANTS, "对助手隐藏");
        fr(HIDDEN_FROM_ASSISTANTS, "Cachée aux assistants");
        es(HIDDEN_FROM_ASSISTANTS, "Oculta para los asistentes");
        de(HIDDEN_FROM_ASSISTANTS, "Vor Assistenten verborgen");
        ko(HIDDEN_FROM_ASSISTANTS, "어시스턴트에게 숨김");

        ja(CHANGED_BY_A_COMMAND, "コマンド（エージェントまたはプロットウィザード）による変更で、エディタでのあなたの編集ではありません");
        zh(CHANGED_BY_A_COMMAND, "由命令（代理或情节向导）修改，并非您在编辑器中修改");
        fr(CHANGED_BY_A_COMMAND, "Modifiée par une commande (un agent ou l'assistant d'intrigue), pas par vous dans l'éditeur");
        es(CHANGED_BY_A_COMMAND, "Cambiada por un comando (un agente o el asistente de trama), no por ti en el editor");
        de(CHANGED_BY_A_COMMAND, "Durch einen Befehl geändert (ein Agent oder der Plot-Assistent), nicht von dir im Editor");
        ko(CHANGED_BY_A_COMMAND, "명령(에이전트나 줄거리 마법사)이 변경했으며, 편집기에서 직접 변경한 것이 아닙니다");

        ja(LINKS_FROM_THIS_NOTE, "このノートからのリンク");
        zh(LINKS_FROM_THIS_NOTE, "此笔记的链接");
        fr(LINKS_FROM_THIS_NOTE, "Liens depuis cette note");
        es(LINKS_FROM_THIS_NOTE, "Enlaces desde esta nota");
        de(LINKS_FROM_THIS_NOTE, "Links aus dieser Notiz");
        ko(LINKS_FROM_THIS_NOTE, "이 메모에서의 링크");

        ja(LINKED_FROM, "リンク元");
        zh(LINKED_FROM, "被以下笔记链接");
        fr(LINKED_FROM, "Liée depuis");
        es(LINKED_FROM, "Enlazada desde");
        de(LINKED_FROM, "Verlinkt von");
        ko(LINKED_FROM, "이 메모를 링크한 곳");

        ja(CREATE_THE_NOTE, "%s を作成");
        zh(CREATE_THE_NOTE, "创建 %s");
        fr(CREATE_THE_NOTE, "Créer %s");
        es(CREATE_THE_NOTE, "Crear %s");
        de(CREATE_THE_NOTE, "%s erstellen");
        ko(CREATE_THE_NOTE, "%s 만들기");

        ja(NO_NOTES_YET, "ノートはまだありません。プロットやアイデア、世界のルールのページを追加しましょう。");
        zh(NO_NOTES_YET, "还没有笔记。为情节、想法或世界规则添加一页吧。");
        fr(NO_NOTES_YET, "Pas encore de notes. Ajoutez une page pour une intrigue, une idée ou une règle du monde.");
        es(NO_NOTES_YET, "Todavía no hay notas. Añade una página para una trama, una idea o una regla del mundo.");
        de(NO_NOTES_YET, "Noch keine Notizen. Füge eine Seite für einen Plot, eine Idee oder eine Regel der Welt hinzu.");
        ko(NO_NOTES_YET, "아직 메모가 없습니다. 줄거리, 아이디어, 세계의 규칙을 위한 페이지를 추가하세요.");

        ja(NO_NOTE_MATCHES, "一致するノートはありません");
        zh(NO_NOTE_MATCHES, "没有匹配的笔记");
        fr(NO_NOTE_MATCHES, "Aucune note ne correspond");
        es(NO_NOTE_MATCHES, "Ninguna nota coincide");
        de(NO_NOTE_MATCHES, "Keine Notiz passt");
        ko(NO_NOTE_MATCHES, "일치하는 메모가 없습니다");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");

        ja(THERE_ARE_UNSAVED_CHANGES_DISCARD, "保存されていない変更があります。破棄しますか？");
        zh(THERE_ARE_UNSAVED_CHANGES_DISCARD, "有未保存的更改。要放弃吗？");
        fr(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Il y a des modifications non enregistrées. Les abandonner ?");
        es(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Hay cambios sin guardar. ¿Descartarlos?");
        de(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Es gibt nicht gespeicherte Änderungen. Verwerfen?");
        ko(THERE_ARE_UNSAVED_CHANGES_DISCARD, "저장하지 않은 변경 사항이 있습니다. 버리시겠습니까?");

        ja(CHANGED_WHILE_EDITING_OVERWRITE, "編集中にノートが変更されました。その変更をあなたの内容で上書きしますか？");
        zh(CHANGED_WHILE_EDITING_OVERWRITE, "您编辑期间此笔记已被修改。要用您的内容覆盖这些更改吗？");
        fr(CHANGED_WHILE_EDITING_OVERWRITE, "La note a été modifiée pendant que vous l'éditiez. Écraser ces modifications avec les vôtres ?");
        es(CHANGED_WHILE_EDITING_OVERWRITE, "La nota cambió mientras la editabas. ¿Reemplazar esos cambios con los tuyos?");
        de(CHANGED_WHILE_EDITING_OVERWRITE, "Die Notiz wurde geändert, während du sie bearbeitet hast. Diese Änderungen mit deinen überschreiben?");
        ko(CHANGED_WHILE_EDITING_OVERWRITE, "편집하는 동안 메모가 변경되었습니다. 그 변경 사항을 내 것으로 덮어쓰시겠습니까?");

        ja(ANOTHER_NOTE_HAS_THIS_TITLE, "同じタイトルのノートがすでにあります");
        zh(ANOTHER_NOTE_HAS_THIS_TITLE, "已有另一篇笔记使用此标题");
        fr(ANOTHER_NOTE_HAS_THIS_TITLE, "Une autre note a déjà ce titre");
        es(ANOTHER_NOTE_HAS_THIS_TITLE, "Otra nota ya tiene este título");
        de(ANOTHER_NOTE_HAS_THIS_TITLE, "Eine andere Notiz hat bereits diesen Titel");
        ko(ANOTHER_NOTE_HAS_THIS_TITLE, "다른 메모가 이미 이 제목을 사용합니다");

        ja(THE_TITLE_IS_REQUIRED, "タイトルは必須です");
        zh(THE_TITLE_IS_REQUIRED, "标题为必填项");
        fr(THE_TITLE_IS_REQUIRED, "Le titre est obligatoire");
        es(THE_TITLE_IS_REQUIRED, "El título es obligatorio");
        de(THE_TITLE_IS_REQUIRED, "Der Titel ist erforderlich");
        ko(THE_TITLE_IS_REQUIRED, "제목은 필수입니다");

        ja(CHARACTERS_COUNT, "%d 文字");
        zh(CHARACTERS_COUNT, "%d 个字符");
        fr(CHARACTERS_COUNT, "%d caractères");
        es(CHARACTERS_COUNT, "%d caracteres");
        de(CHARACTERS_COUNT, "%d Zeichen");
        ko(CHARACTERS_COUNT, "%d자");
    }
}
