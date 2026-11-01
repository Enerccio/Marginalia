package com.github.enerccio.marginalia.extensions.charactertracker;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Character Tracker in the languages besides English; the English text is the key.
 */
public class CharacterTrackerLocalization extends PluginLocalizationBundle {

    public static final CharacterTrackerLocalization T = new CharacterTrackerLocalization();

    public static final String CHARACTERS = "Characters";
    public static final String RELATIONSHIP_MAP = "Relationship map";
    public static final String THERE_ARE_NO_RELATIONSHIPS = "There are no relationships yet. Add them in the dialog of a character.";
    public static final String DOUBLE_CLICK_A_CHARACTER = "Double-click a character to arrange the map around it. Drag to move, scroll to zoom.";
    public static final String CLOSE = "Close";
    public static final String ADD = "Add";
    public static final String ADD_NEW_CHARACTER = "Add new character";
    public static final String MORE_ACTIONS = "More actions";
    public static final String IMPORT_LORE_ENTRIES_AS_CHARACTERS = "Import lore entries as characters";
    public static final String CHARACTERS_IN_THE_PROMPT = "Characters in the prompt";
    public static final String IN_PROMPT = "In prompt";
    public static final String INSERT_THE_CHARACTERS_AND_THEIR = "Insert the characters and their relationships into the prompt of every generation";
    public static final String DEPTH = "Depth";
    public static final String MESSAGES_AFTER_THE_CHARACTERS = "Messages after the characters: 0 = after the instructions, 1 = right before them";
    public static final String ROLE_OF_THE_MESSAGE = "Role of the message with the characters";
    public static final String SYSTEM = "System";
    public static final String ASSISTANT = "Assistant";
    public static final String USER = "User";
    public static final String IMPORTED_CHARACTERS = "Imported %d characters";
    public static final String SHOW_ICON_AND_NOTE = "Show icon and note";
    public static final String EDIT_CHARACTER = "Edit character";
    public static final String DELETE_CHARACTER = "Delete character";
    public static final String NO_ICON_OR_NOTE = "No icon or note";
    public static final String DELETE_THE_CHARACTER_AND_ALL = "Delete the character %s and all its relationships?";
    public static final String THIS_CHARACTER_OTHER_CHARACTER = "This character → other character";
    public static final String OTHER_CHARACTER_THIS_CHARACTER = "Other character → this character";
    public static final String BOTH_WAYS = "Both ways (↔)";
    public static final String NEW_CHARACTER = "New character";
    public static final String SAVE_THE_CHARACTER_TO_ADD = "Save the character to add its relationships.";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String NAME = "Name";
    public static final String TAGLINE = "Tagline";
    public static final String NOTE = "Note";
    public static final String ICON = "Icon";
    public static final String THE_FILE = "The file is not an image that can be read";
    public static final String UPLOAD_ICON = "Upload icon";
    public static final String THE_FILE_IS_NOT_AN_IMAGE = "The file is not an image or it is too big";
    public static final String REMOVE_ICON = "Remove icon";
    public static final String ICON_SCALED_DOWN = "Icon (scaled down to at most %1$d x %1$d pixels)";
    public static final String COLOR = "Color";
    public static final String CHARACTER = "Character";
    public static final String DELETE_RELATIONSHIP = "Delete relationship";
    public static final String RELATIONSHIPS = "Relationships";
    public static final String SEARCH_CHARACTER = "Search character";
    public static final String DIRECTION = "Direction";
    public static final String ADD_RELATIONSHIP = "Add relationship";
    public static final String CHOOSE_A_CHARACTER_AND_ENTER = "Choose a character and enter the name of the relationship";
    public static final String THERE_IS_NO_OTHER_CHARACTER = "There is no other character yet";
    public static final String THERE_ARE_UNSAVED_CHANGES_DISCARD = "There are unsaved changes. Discard them?";
    public static final String THE_NAME_IS_REQUIRED = "The name is required";
    public static final String THE_LOREBOOK_OF_THE_BOOK = "The lorebook of the book has no entries.";
    public static final String IMPORT = "Import";
    public static final String EMPTY = "(empty)";
    public static final String IMPORT_D = "Import (%d)";
    public static final String DELETE_THE_RELATIONSHIP = "Delete the relationship %s?";
    public static final String PICK_THE_POV_CHARACTER = "Pick the POV character";
    public static final String ADD_A_PRESENT_CHARACTER = "Add a present character";
    public static final String THERE_ARE_NO_CHARACTERS_YET = "There are no characters yet";

    @Override
    protected void loadKeys() {
        ja(CHARACTERS, "キャラクター");
        zh(CHARACTERS, "角色");

        ja(RELATIONSHIP_MAP, "関係図");
        zh(RELATIONSHIP_MAP, "关系图");

        ja(THERE_ARE_NO_RELATIONSHIPS, "関係はまだありません。キャラクターのダイアログで追加してください。");
        zh(THERE_ARE_NO_RELATIONSHIPS, "还没有任何关系。请在角色的对话框中添加。");

        ja(DOUBLE_CLICK_A_CHARACTER, "キャラクターをダブルクリックすると、そのキャラクターを中心にマップを配置します。ドラッグで移動、スクロールで拡大縮小できます。");
        zh(DOUBLE_CLICK_A_CHARACTER, "双击角色可围绕该角色重新排列关系图。拖动可移动，滚动可缩放。");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(ADD, "追加");
        zh(ADD, "添加");

        ja(ADD_NEW_CHARACTER, "新しいキャラクターを追加");
        zh(ADD_NEW_CHARACTER, "添加新角色");

        ja(MORE_ACTIONS, "その他の操作");
        zh(MORE_ACTIONS, "更多操作");

        ja(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "ロアエントリをキャラクターとしてインポート");
        zh(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "将世界书条目导入为角色");

        ja(CHARACTERS_IN_THE_PROMPT, "プロンプト内のキャラクター");
        zh(CHARACTERS_IN_THE_PROMPT, "提示词中的角色");

        ja(IN_PROMPT, "プロンプトに含める");
        zh(IN_PROMPT, "加入提示词");

        ja(INSERT_THE_CHARACTERS_AND_THEIR, "すべての生成のプロンプトに、キャラクターとその関係を挿入します");
        zh(INSERT_THE_CHARACTERS_AND_THEIR, "将角色及其关系插入每次生成的提示词");

        ja(DEPTH, "深さ");
        zh(DEPTH, "深度");

        ja(MESSAGES_AFTER_THE_CHARACTERS, "キャラクターの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_CHARACTERS, "角色之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");

        ja(ROLE_OF_THE_MESSAGE, "キャラクターを含むメッセージのロール");
        zh(ROLE_OF_THE_MESSAGE, "包含角色的消息的角色");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");

        ja(USER, "ユーザー");
        zh(USER, "用户");

        ja(IMPORTED_CHARACTERS, "%d人のキャラクターをインポートしました");
        zh(IMPORTED_CHARACTERS, "已导入 %d 个角色");

        ja(SHOW_ICON_AND_NOTE, "アイコンとメモを表示");
        zh(SHOW_ICON_AND_NOTE, "显示图标和备注");

        ja(EDIT_CHARACTER, "キャラクターを編集");
        zh(EDIT_CHARACTER, "编辑角色");

        ja(DELETE_CHARACTER, "キャラクターを削除");
        zh(DELETE_CHARACTER, "删除角色");

        ja(NO_ICON_OR_NOTE, "アイコンもメモもありません");
        zh(NO_ICON_OR_NOTE, "没有图标或备注");

        ja(DELETE_THE_CHARACTER_AND_ALL, "キャラクター「%s」とそのすべての関係を削除しますか？");
        zh(DELETE_THE_CHARACTER_AND_ALL, "要删除角色“%s”及其所有关系吗？");

        ja(THIS_CHARACTER_OTHER_CHARACTER, "このキャラクター → 他のキャラクター");
        zh(THIS_CHARACTER_OTHER_CHARACTER, "此角色 → 其他角色");

        ja(OTHER_CHARACTER_THIS_CHARACTER, "他のキャラクター → このキャラクター");
        zh(OTHER_CHARACTER_THIS_CHARACTER, "其他角色 → 此角色");

        ja(BOTH_WAYS, "双方向（↔）");
        zh(BOTH_WAYS, "双向（↔）");

        ja(NEW_CHARACTER, "新しいキャラクター");
        zh(NEW_CHARACTER, "新角色");

        ja(SAVE_THE_CHARACTER_TO_ADD, "関係を追加するには、先にキャラクターを保存してください。");
        zh(SAVE_THE_CHARACTER_TO_ADD, "请先保存角色，再添加其关系。");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(NAME, "名前");
        zh(NAME, "名称");

        ja(TAGLINE, "キャッチフレーズ");
        zh(TAGLINE, "标语");

        ja(NOTE, "メモ");
        zh(NOTE, "备注");

        ja(ICON, "アイコン");
        zh(ICON, "图标");

        ja(THE_FILE, "ファイルは読み取れる画像ではありません");
        zh(THE_FILE, "该文件不是可读取的图片");

        ja(UPLOAD_ICON, "アイコンをアップロード");
        zh(UPLOAD_ICON, "上传图标");

        ja(THE_FILE_IS_NOT_AN_IMAGE, "ファイルが画像ではないか、大きすぎます");
        zh(THE_FILE_IS_NOT_AN_IMAGE, "该文件不是图片，或者太大");

        ja(REMOVE_ICON, "アイコンを削除");
        zh(REMOVE_ICON, "移除图标");

        ja(ICON_SCALED_DOWN, "アイコン（最大 %1$d x %1$d ピクセルに縮小されます）");
        zh(ICON_SCALED_DOWN, "图标（将缩小到最大 %1$d x %1$d 像素）");

        ja(COLOR, "色");
        zh(COLOR, "颜色");

        ja(CHARACTER, "キャラクター");
        zh(CHARACTER, "角色");

        ja(DELETE_RELATIONSHIP, "関係を削除");
        zh(DELETE_RELATIONSHIP, "删除关系");

        ja(RELATIONSHIPS, "関係");
        zh(RELATIONSHIPS, "关系");

        ja(SEARCH_CHARACTER, "キャラクターを検索");
        zh(SEARCH_CHARACTER, "搜索角色");

        ja(DIRECTION, "方向");
        zh(DIRECTION, "方向");

        ja(ADD_RELATIONSHIP, "関係を追加");
        zh(ADD_RELATIONSHIP, "添加关系");

        ja(CHOOSE_A_CHARACTER_AND_ENTER, "キャラクターを選択し、関係の名前を入力してください");
        zh(CHOOSE_A_CHARACTER_AND_ENTER, "请选择角色并输入关系的名称");

        ja(THERE_IS_NO_OTHER_CHARACTER, "他のキャラクターはまだいません");
        zh(THERE_IS_NO_OTHER_CHARACTER, "还没有其他角色");

        ja(THERE_ARE_UNSAVED_CHANGES_DISCARD, "保存されていない変更があります。破棄しますか？");
        zh(THERE_ARE_UNSAVED_CHANGES_DISCARD, "有未保存的更改。要放弃吗？");

        ja(THE_NAME_IS_REQUIRED, "名前は必須です");
        zh(THE_NAME_IS_REQUIRED, "名称为必填项");

        ja(THE_LOREBOOK_OF_THE_BOOK, "このブックのロアブックにはエントリがありません。");
        zh(THE_LOREBOOK_OF_THE_BOOK, "该书的世界书中没有条目。");

        ja(IMPORT, "インポート");
        zh(IMPORT, "导入");

        ja(EMPTY, "（空）");
        zh(EMPTY, "（空）");

        ja(IMPORT_D, "インポート（%d）");
        zh(IMPORT_D, "导入（%d）");

        ja(DELETE_THE_RELATIONSHIP, "関係「%s」を削除しますか？");
        zh(DELETE_THE_RELATIONSHIP, "要删除关系“%s”吗？");

        ja(PICK_THE_POV_CHARACTER, "視点キャラクターを選択");
        zh(PICK_THE_POV_CHARACTER, "选择视角角色");

        ja(ADD_A_PRESENT_CHARACTER, "登場キャラクターを追加");
        zh(ADD_A_PRESENT_CHARACTER, "添加在场角色");

        ja(THERE_ARE_NO_CHARACTERS_YET, "キャラクターはまだいません");
        zh(THERE_ARE_NO_CHARACTERS_YET, "还没有角色");
    }
}
