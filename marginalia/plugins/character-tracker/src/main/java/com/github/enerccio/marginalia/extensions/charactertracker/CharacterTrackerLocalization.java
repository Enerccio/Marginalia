package com.github.enerccio.marginalia.extensions.charactertracker;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Character Tracker in the languages besides English; the English text is the key.
 */
public class CharacterTrackerLocalization extends PluginLocalizationBundle {

    public static final CharacterTrackerLocalization T = new CharacterTrackerLocalization();

    @Override
    protected void loadKeys() {
        ja("Characters", "キャラクター");
        zh("Characters", "角色");

        ja("Relationship map", "関係図");
        zh("Relationship map", "关系图");

        ja("There are no relationships yet. Add them in the dialog of a character.", "関係はまだありません。キャラクターのダイアログで追加してください。");
        zh("There are no relationships yet. Add them in the dialog of a character.", "还没有任何关系。请在角色的对话框中添加。");

        ja("Double-click a character to arrange the map around it. Drag to move, scroll to zoom.", "キャラクターをダブルクリックすると、そのキャラクターを中心にマップを配置します。ドラッグで移動、スクロールで拡大縮小できます。");
        zh("Double-click a character to arrange the map around it. Drag to move, scroll to zoom.", "双击角色可围绕该角色重新排列关系图。拖动可移动，滚动可缩放。");

        ja("Close", "閉じる");
        zh("Close", "关闭");

        ja("Add", "追加");
        zh("Add", "添加");

        ja("Add new character", "新しいキャラクターを追加");
        zh("Add new character", "添加新角色");

        ja("More actions", "その他の操作");
        zh("More actions", "更多操作");

        ja("Import lore entries as characters", "ロアエントリをキャラクターとしてインポート");
        zh("Import lore entries as characters", "将世界书条目导入为角色");

        ja("Characters in the prompt", "プロンプト内のキャラクター");
        zh("Characters in the prompt", "提示词中的角色");

        ja("In prompt", "プロンプトに含める");
        zh("In prompt", "加入提示词");

        ja("Insert the characters and their relationships into the prompt of every generation", "すべての生成のプロンプトに、キャラクターとその関係を挿入します");
        zh("Insert the characters and their relationships into the prompt of every generation", "将角色及其关系插入每次生成的提示词");

        ja("Depth", "深さ");
        zh("Depth", "深度");

        ja("Messages after the characters: 0 = after the instructions, 1 = right before them", "キャラクターの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh("Messages after the characters: 0 = after the instructions, 1 = right before them", "角色之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");

        ja("Role of the message with the characters", "キャラクターを含むメッセージのロール");
        zh("Role of the message with the characters", "包含角色的消息的角色");

        ja("System", "システム");
        zh("System", "系统");

        ja("Assistant", "アシスタント");
        zh("Assistant", "助手");

        ja("User", "ユーザー");
        zh("User", "用户");

        ja("Imported %d characters", "%d人のキャラクターをインポートしました");
        zh("Imported %d characters", "已导入 %d 个角色");

        ja("Show icon and note", "アイコンとメモを表示");
        zh("Show icon and note", "显示图标和备注");

        ja("Edit character", "キャラクターを編集");
        zh("Edit character", "编辑角色");

        ja("Delete character", "キャラクターを削除");
        zh("Delete character", "删除角色");

        ja("No icon or note", "アイコンもメモもありません");
        zh("No icon or note", "没有图标或备注");

        ja("Delete the character %s and all its relationships?", "キャラクター「%s」とそのすべての関係を削除しますか？");
        zh("Delete the character %s and all its relationships?", "要删除角色“%s”及其所有关系吗？");

        ja("This character → other character", "このキャラクター → 他のキャラクター");
        zh("This character → other character", "此角色 → 其他角色");

        ja("Other character → this character", "他のキャラクター → このキャラクター");
        zh("Other character → this character", "其他角色 → 此角色");

        ja("Both ways (↔)", "双方向（↔）");
        zh("Both ways (↔)", "双向（↔）");

        ja("New character", "新しいキャラクター");
        zh("New character", "新角色");

        ja("Save the character to add its relationships.", "関係を追加するには、先にキャラクターを保存してください。");
        zh("Save the character to add its relationships.", "请先保存角色，再添加其关系。");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Name", "名前");
        zh("Name", "名称");

        ja("Tagline", "キャッチフレーズ");
        zh("Tagline", "标语");

        ja("Note", "メモ");
        zh("Note", "备注");

        ja("Icon", "アイコン");
        zh("Icon", "图标");

        ja("The file is not an image that can be read", "ファイルは読み取れる画像ではありません");
        zh("The file is not an image that can be read", "该文件不是可读取的图片");

        ja("Upload icon", "アイコンをアップロード");
        zh("Upload icon", "上传图标");

        ja("The file is not an image or it is too big", "ファイルが画像ではないか、大きすぎます");
        zh("The file is not an image or it is too big", "该文件不是图片，或者太大");

        ja("Remove icon", "アイコンを削除");
        zh("Remove icon", "移除图标");

        ja("Icon (scaled down to at most %1$d x %1$d pixels)", "アイコン（最大 %1$d x %1$d ピクセルに縮小されます）");
        zh("Icon (scaled down to at most %1$d x %1$d pixels)", "图标（将缩小到最大 %1$d x %1$d 像素）");

        ja("Color", "色");
        zh("Color", "颜色");

        ja("Character", "キャラクター");
        zh("Character", "角色");

        ja("Delete relationship", "関係を削除");
        zh("Delete relationship", "删除关系");

        ja("Relationships", "関係");
        zh("Relationships", "关系");

        ja("Search character", "キャラクターを検索");
        zh("Search character", "搜索角色");

        ja("Direction", "方向");
        zh("Direction", "方向");

        ja("Add relationship", "関係を追加");
        zh("Add relationship", "添加关系");

        ja("Choose a character and enter the name of the relationship", "キャラクターを選択し、関係の名前を入力してください");
        zh("Choose a character and enter the name of the relationship", "请选择角色并输入关系的名称");

        ja("There is no other character yet", "他のキャラクターはまだいません");
        zh("There is no other character yet", "还没有其他角色");

        ja("There are unsaved changes. Discard them?", "保存されていない変更があります。破棄しますか？");
        zh("There are unsaved changes. Discard them?", "有未保存的更改。要放弃吗？");

        ja("The name is required", "名前は必須です");
        zh("The name is required", "名称为必填项");

        ja("The lorebook of the book has no entries.", "このブックのロアブックにはエントリがありません。");
        zh("The lorebook of the book has no entries.", "该书的世界书中没有条目。");

        ja("Import", "インポート");
        zh("Import", "导入");

        ja("(empty)", "（空）");
        zh("(empty)", "（空）");

        ja("Import (%d)", "インポート（%d）");
        zh("Import (%d)", "导入（%d）");

        ja("Delete the relationship %s?", "関係「%s」を削除しますか？");
        zh("Delete the relationship %s?", "要删除关系“%s”吗？");

        ja("Pick the POV character", "視点キャラクターを選択");
        zh("Pick the POV character", "选择视角角色");

        ja("Add a present character", "登場キャラクターを追加");
        zh("Add a present character", "添加在场角色");

        ja("There are no characters yet", "キャラクターはまだいません");
        zh("There are no characters yet", "还没有角色");
    }
}
