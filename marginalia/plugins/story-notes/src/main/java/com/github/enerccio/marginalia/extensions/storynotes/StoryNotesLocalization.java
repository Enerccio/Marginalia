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

        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(ADD_NEW_NOTE, "新しいノートを追加");
        zh(ADD_NEW_NOTE, "添加新笔记");

        ja(NEW_NOTE, "新しいノート");
        zh(NEW_NOTE, "新笔记");

        ja(EDIT_NOTE, "ノートを編集");
        zh(EDIT_NOTE, "编辑笔记");

        ja(DELETE_NOTE, "ノートを削除");
        zh(DELETE_NOTE, "删除笔记");

        ja(DELETE_THE_NOTE_PAGE, "ノート「%s」を削除しますか？");
        zh(DELETE_THE_NOTE_PAGE, "删除笔记“%s”吗？");

        ja(SEARCH_NOTES, "ノートを検索");
        zh(SEARCH_NOTES, "搜索笔记");

        ja(ALL_TAGS, "すべてのタグ");
        zh(ALL_TAGS, "所有标签");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");

        ja(TAGS, "タグ");
        zh(TAGS, "标签");

        ja(SEPARATED_BY_COMMAS, "カンマ区切り");
        zh(SEPARATED_BY_COMMAS, "用逗号分隔");

        ja(TEXT_MARKDOWN_LINKS, "本文（Markdown、[[タイトル]]で他のノートにリンク）");
        zh(TEXT_MARKDOWN_LINKS, "正文（Markdown，[[标题]] 链接到其他笔记）");

        ja(WRITE, "書く");
        zh(WRITE, "编写");

        ja(PREVIEW, "プレビュー");
        zh(PREVIEW, "预览");

        ja(VISIBLE_TO_ASSISTANTS, "アシスタントに公開");
        zh(VISIBLE_TO_ASSISTANTS, "对助手可见");

        ja(VISIBLE_TO_ASSISTANTS_TIP, "コマンド、エージェント、プロットウィザードがこのノートを読み書きできます。自分だけが見るノートはオフにしてください。");
        zh(VISIBLE_TO_ASSISTANTS_TIP, "命令、代理和情节向导可以读取并修改此笔记。仅供自己查看的笔记请关闭。");

        ja(HIDDEN_FROM_ASSISTANTS, "アシスタントには非公開");
        zh(HIDDEN_FROM_ASSISTANTS, "对助手隐藏");

        ja(CHANGED_BY_A_COMMAND, "コマンド（エージェントまたはプロットウィザード）による変更で、エディタでのあなたの編集ではありません");
        zh(CHANGED_BY_A_COMMAND, "由命令（代理或情节向导）修改，并非您在编辑器中修改");

        ja(LINKS_FROM_THIS_NOTE, "このノートからのリンク");
        zh(LINKS_FROM_THIS_NOTE, "此笔记的链接");

        ja(LINKED_FROM, "リンク元");
        zh(LINKED_FROM, "被以下笔记链接");

        ja(CREATE_THE_NOTE, "%s を作成");
        zh(CREATE_THE_NOTE, "创建 %s");

        ja(NO_NOTES_YET, "ノートはまだありません。プロットやアイデア、世界のルールのページを追加しましょう。");
        zh(NO_NOTES_YET, "还没有笔记。为情节、想法或世界规则添加一页吧。");

        ja(NO_NOTE_MATCHES, "一致するノートはありません");
        zh(NO_NOTE_MATCHES, "没有匹配的笔记");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(THERE_ARE_UNSAVED_CHANGES_DISCARD, "保存されていない変更があります。破棄しますか？");
        zh(THERE_ARE_UNSAVED_CHANGES_DISCARD, "有未保存的更改。要放弃吗？");

        ja(CHANGED_WHILE_EDITING_OVERWRITE, "編集中にノートが変更されました。その変更をあなたの内容で上書きしますか？");
        zh(CHANGED_WHILE_EDITING_OVERWRITE, "您编辑期间此笔记已被修改。要用您的内容覆盖这些更改吗？");

        ja(ANOTHER_NOTE_HAS_THIS_TITLE, "同じタイトルのノートがすでにあります");
        zh(ANOTHER_NOTE_HAS_THIS_TITLE, "已有另一篇笔记使用此标题");

        ja(THE_TITLE_IS_REQUIRED, "タイトルは必須です");
        zh(THE_TITLE_IS_REQUIRED, "标题为必填项");

        ja(CHARACTERS_COUNT, "%d 文字");
        zh(CHARACTERS_COUNT, "%d 个字符");
    }
}
