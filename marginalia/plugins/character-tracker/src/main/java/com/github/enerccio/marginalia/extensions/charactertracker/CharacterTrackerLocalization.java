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
        fr(CHARACTERS, "Personnages");
        es(CHARACTERS, "Personajes");
        de(CHARACTERS, "Figuren");
        ko(CHARACTERS, "캐릭터");
        it(CHARACTERS, "Personaggi");

        ja(RELATIONSHIP_MAP, "関係図");
        zh(RELATIONSHIP_MAP, "关系图");
        fr(RELATIONSHIP_MAP, "Carte des relations");
        es(RELATIONSHIP_MAP, "Mapa de relaciones");
        de(RELATIONSHIP_MAP, "Beziehungskarte");
        ko(RELATIONSHIP_MAP, "관계도");
        it(RELATIONSHIP_MAP, "Mappa delle relazioni");

        ja(THERE_ARE_NO_RELATIONSHIPS, "関係はまだありません。キャラクターのダイアログで追加してください。");
        zh(THERE_ARE_NO_RELATIONSHIPS, "还没有任何关系。请在角色的对话框中添加。");
        fr(THERE_ARE_NO_RELATIONSHIPS, "Il n'y a pas encore de relations. Ajoutez-les dans la fenêtre d'un personnage.");
        es(THERE_ARE_NO_RELATIONSHIPS, "Todavía no hay relaciones. Añádelas en el cuadro de diálogo de un personaje.");
        de(THERE_ARE_NO_RELATIONSHIPS, "Es gibt noch keine Beziehungen. Füge sie im Dialog einer Figur hinzu.");
        ko(THERE_ARE_NO_RELATIONSHIPS, "아직 관계가 없습니다. 캐릭터 대화 상자에서 추가하세요.");
        it(THERE_ARE_NO_RELATIONSHIPS, "Non ci sono ancora relazioni. Aggiungile nella finestra di un personaggio.");

        ja(DOUBLE_CLICK_A_CHARACTER, "キャラクターをダブルクリックすると、そのキャラクターを中心にマップを配置します。ドラッグで移動、スクロールで拡大縮小できます。");
        zh(DOUBLE_CLICK_A_CHARACTER, "双击角色可围绕该角色重新排列关系图。拖动可移动，滚动可缩放。");
        fr(DOUBLE_CLICK_A_CHARACTER, "Double-cliquez sur un personnage pour organiser la carte autour de lui. Faites glisser pour déplacer, la molette pour zoomer.");
        es(DOUBLE_CLICK_A_CHARACTER, "Haz doble clic en un personaje para organizar el mapa a su alrededor. Arrastra para mover, usa la rueda para ampliar.");
        de(DOUBLE_CLICK_A_CHARACTER, "Doppelklicke auf eine Figur, um die Karte um sie herum anzuordnen. Ziehen zum Verschieben, Scrollen zum Zoomen.");
        ko(DOUBLE_CLICK_A_CHARACTER, "캐릭터를 더블 클릭하면 그 캐릭터를 중심으로 관계도를 배치합니다. 드래그하여 이동하고 스크롤하여 확대/축소합니다.");
        it(DOUBLE_CLICK_A_CHARACTER, "Fai doppio clic su un personaggio per disporre la mappa attorno a lui. Trascina per spostare, scorri per ingrandire.");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(ADD, "追加");
        zh(ADD, "添加");
        fr(ADD, "Ajouter");
        es(ADD, "Añadir");
        de(ADD, "Hinzufügen");
        ko(ADD, "추가");
        it(ADD, "Aggiungi");

        ja(ADD_NEW_CHARACTER, "新しいキャラクターを追加");
        zh(ADD_NEW_CHARACTER, "添加新角色");
        fr(ADD_NEW_CHARACTER, "Ajouter un nouveau personnage");
        es(ADD_NEW_CHARACTER, "Añadir personaje nuevo");
        de(ADD_NEW_CHARACTER, "Neue Figur hinzufügen");
        ko(ADD_NEW_CHARACTER, "새 캐릭터 추가");
        it(ADD_NEW_CHARACTER, "Aggiungi un nuovo personaggio");

        ja(MORE_ACTIONS, "その他の操作");
        zh(MORE_ACTIONS, "更多操作");
        fr(MORE_ACTIONS, "Plus d'actions");
        es(MORE_ACTIONS, "Más acciones");
        de(MORE_ACTIONS, "Weitere Aktionen");
        ko(MORE_ACTIONS, "추가 작업");
        it(MORE_ACTIONS, "Altre azioni");

        ja(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "ロアエントリをキャラクターとしてインポート");
        zh(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "将世界书条目导入为角色");
        fr(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "Importer des entrées de lore comme personnages");
        es(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "Importar entradas de lore como personajes");
        de(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "Lore-Einträge als Figuren importieren");
        ko(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "로어 항목을 캐릭터로 가져오기");
        it(IMPORT_LORE_ENTRIES_AS_CHARACTERS, "Importa le voci del lore come personaggi");

        ja(CHARACTERS_IN_THE_PROMPT, "プロンプト内のキャラクター");
        zh(CHARACTERS_IN_THE_PROMPT, "提示词中的角色");
        fr(CHARACTERS_IN_THE_PROMPT, "Personnages dans le prompt");
        es(CHARACTERS_IN_THE_PROMPT, "Personajes en el prompt");
        de(CHARACTERS_IN_THE_PROMPT, "Figuren im Prompt");
        ko(CHARACTERS_IN_THE_PROMPT, "프롬프트의 캐릭터");
        it(CHARACTERS_IN_THE_PROMPT, "Personaggi nel prompt");

        ja(IN_PROMPT, "プロンプトに含める");
        zh(IN_PROMPT, "加入提示词");
        fr(IN_PROMPT, "Dans le prompt");
        es(IN_PROMPT, "En el prompt");
        de(IN_PROMPT, "Im Prompt");
        ko(IN_PROMPT, "프롬프트에 포함");
        it(IN_PROMPT, "Nel prompt");

        ja(INSERT_THE_CHARACTERS_AND_THEIR, "すべての生成のプロンプトに、キャラクターとその関係を挿入します");
        zh(INSERT_THE_CHARACTERS_AND_THEIR, "将角色及其关系插入每次生成的提示词");
        fr(INSERT_THE_CHARACTERS_AND_THEIR, "Insérer les personnages et leurs relations dans le prompt de chaque génération");
        es(INSERT_THE_CHARACTERS_AND_THEIR, "Insertar los personajes y sus relaciones en el prompt de cada generación");
        de(INSERT_THE_CHARACTERS_AND_THEIR, "Die Figuren und ihre Beziehungen in den Prompt jeder Generierung einfügen");
        ko(INSERT_THE_CHARACTERS_AND_THEIR, "모든 생성의 프롬프트에 캐릭터와 그 관계를 삽입");
        it(INSERT_THE_CHARACTERS_AND_THEIR, "Inserisci i personaggi e le loro relazioni nel prompt di ogni generazione");

        ja(DEPTH, "深さ");
        zh(DEPTH, "深度");
        fr(DEPTH, "Profondeur");
        es(DEPTH, "Profundidad");
        de(DEPTH, "Tiefe");
        ko(DEPTH, "깊이");
        it(DEPTH, "Profondità");

        ja(MESSAGES_AFTER_THE_CHARACTERS, "キャラクターの後ろのメッセージ数: 0 = 指示の後、1 = 指示の直前");
        zh(MESSAGES_AFTER_THE_CHARACTERS, "角色之后的消息数：0 = 在指令之后，1 = 紧接在指令之前");
        fr(MESSAGES_AFTER_THE_CHARACTERS, "Messages après les personnages : 0 = après les instructions, 1 = juste avant elles");
        es(MESSAGES_AFTER_THE_CHARACTERS, "Mensajes después de los personajes: 0 = después de las instrucciones, 1 = justo antes de ellas");
        de(MESSAGES_AFTER_THE_CHARACTERS, "Nachrichten nach den Figuren: 0 = nach den Anweisungen, 1 = direkt davor");
        ko(MESSAGES_AFTER_THE_CHARACTERS, "캐릭터 뒤의 메시지 수: 0 = 지시문 뒤, 1 = 지시문 바로 앞");
        it(MESSAGES_AFTER_THE_CHARACTERS, "Messaggi dopo i personaggi: 0 = dopo le istruzioni, 1 = subito prima di esse");

        ja(ROLE_OF_THE_MESSAGE, "キャラクターを含むメッセージのロール");
        zh(ROLE_OF_THE_MESSAGE, "包含角色的消息的角色");
        fr(ROLE_OF_THE_MESSAGE, "Rôle du message contenant les personnages");
        es(ROLE_OF_THE_MESSAGE, "Rol del mensaje con los personajes");
        de(ROLE_OF_THE_MESSAGE, "Rolle der Nachricht mit den Figuren");
        ko(ROLE_OF_THE_MESSAGE, "캐릭터 메시지의 역할");
        it(ROLE_OF_THE_MESSAGE, "Ruolo del messaggio con i personaggi");

        ja(SYSTEM, "システム");
        zh(SYSTEM, "系统");
        fr(SYSTEM, "Système");
        es(SYSTEM, "Sistema");
        de(SYSTEM, "System");
        ko(SYSTEM, "시스템");
        it(SYSTEM, "Sistema");

        ja(ASSISTANT, "アシスタント");
        zh(ASSISTANT, "助手");
        fr(ASSISTANT, "Assistant");
        es(ASSISTANT, "Asistente");
        de(ASSISTANT, "Assistent");
        ko(ASSISTANT, "어시스턴트");
        it(ASSISTANT, "Assistente");

        ja(USER, "ユーザー");
        zh(USER, "用户");
        fr(USER, "Utilisateur");
        es(USER, "Usuario");
        de(USER, "Benutzer");
        ko(USER, "사용자");
        it(USER, "Utente");

        ja(IMPORTED_CHARACTERS, "%d人のキャラクターをインポートしました");
        zh(IMPORTED_CHARACTERS, "已导入 %d 个角色");
        fr(IMPORTED_CHARACTERS, "%d personnages importés");
        es(IMPORTED_CHARACTERS, "Se han importado %d personajes");
        de(IMPORTED_CHARACTERS, "%d Figuren importiert");
        ko(IMPORTED_CHARACTERS, "캐릭터 %d개를 가져왔습니다");
        it(IMPORTED_CHARACTERS, "Importati %d personaggi");

        ja(SHOW_ICON_AND_NOTE, "アイコンとメモを表示");
        zh(SHOW_ICON_AND_NOTE, "显示图标和备注");
        fr(SHOW_ICON_AND_NOTE, "Afficher l'icône et la note");
        es(SHOW_ICON_AND_NOTE, "Mostrar icono y nota");
        de(SHOW_ICON_AND_NOTE, "Symbol und Notiz anzeigen");
        ko(SHOW_ICON_AND_NOTE, "아이콘과 메모 표시");
        it(SHOW_ICON_AND_NOTE, "Mostra icona e nota");

        ja(EDIT_CHARACTER, "キャラクターを編集");
        zh(EDIT_CHARACTER, "编辑角色");
        fr(EDIT_CHARACTER, "Modifier le personnage");
        es(EDIT_CHARACTER, "Editar personaje");
        de(EDIT_CHARACTER, "Figur bearbeiten");
        ko(EDIT_CHARACTER, "캐릭터 편집");
        it(EDIT_CHARACTER, "Modifica il personaggio");

        ja(DELETE_CHARACTER, "キャラクターを削除");
        zh(DELETE_CHARACTER, "删除角色");
        fr(DELETE_CHARACTER, "Supprimer le personnage");
        es(DELETE_CHARACTER, "Eliminar personaje");
        de(DELETE_CHARACTER, "Figur löschen");
        ko(DELETE_CHARACTER, "캐릭터 삭제");
        it(DELETE_CHARACTER, "Elimina il personaggio");

        ja(NO_ICON_OR_NOTE, "アイコンもメモもありません");
        zh(NO_ICON_OR_NOTE, "没有图标或备注");
        fr(NO_ICON_OR_NOTE, "Ni icône ni note");
        es(NO_ICON_OR_NOTE, "Sin icono ni nota");
        de(NO_ICON_OR_NOTE, "Kein Symbol und keine Notiz");
        ko(NO_ICON_OR_NOTE, "아이콘이나 메모 없음");
        it(NO_ICON_OR_NOTE, "Nessuna icona o nota");

        ja(DELETE_THE_CHARACTER_AND_ALL, "キャラクター「%s」とそのすべての関係を削除しますか？");
        zh(DELETE_THE_CHARACTER_AND_ALL, "要删除角色“%s”及其所有关系吗？");
        fr(DELETE_THE_CHARACTER_AND_ALL, "Supprimer le personnage %s et toutes ses relations ?");
        es(DELETE_THE_CHARACTER_AND_ALL, "¿Eliminar al personaje %s y todas sus relaciones?");
        de(DELETE_THE_CHARACTER_AND_ALL, "Die Figur %s und alle ihre Beziehungen löschen?");
        ko(DELETE_THE_CHARACTER_AND_ALL, "캐릭터 %s과(와) 모든 관계를 삭제하시겠습니까?");
        it(DELETE_THE_CHARACTER_AND_ALL, "Eliminare il personaggio %s e tutte le sue relazioni?");

        ja(THIS_CHARACTER_OTHER_CHARACTER, "このキャラクター → 他のキャラクター");
        zh(THIS_CHARACTER_OTHER_CHARACTER, "此角色 → 其他角色");
        fr(THIS_CHARACTER_OTHER_CHARACTER, "Ce personnage → autre personnage");
        es(THIS_CHARACTER_OTHER_CHARACTER, "Este personaje → otro personaje");
        de(THIS_CHARACTER_OTHER_CHARACTER, "Diese Figur → andere Figur");
        ko(THIS_CHARACTER_OTHER_CHARACTER, "이 캐릭터 → 다른 캐릭터");
        it(THIS_CHARACTER_OTHER_CHARACTER, "Questo personaggio → altro personaggio");

        ja(OTHER_CHARACTER_THIS_CHARACTER, "他のキャラクター → このキャラクター");
        zh(OTHER_CHARACTER_THIS_CHARACTER, "其他角色 → 此角色");
        fr(OTHER_CHARACTER_THIS_CHARACTER, "Autre personnage → ce personnage");
        es(OTHER_CHARACTER_THIS_CHARACTER, "Otro personaje → este personaje");
        de(OTHER_CHARACTER_THIS_CHARACTER, "Andere Figur → diese Figur");
        ko(OTHER_CHARACTER_THIS_CHARACTER, "다른 캐릭터 → 이 캐릭터");
        it(OTHER_CHARACTER_THIS_CHARACTER, "Altro personaggio → questo personaggio");

        ja(BOTH_WAYS, "双方向（↔）");
        zh(BOTH_WAYS, "双向（↔）");
        fr(BOTH_WAYS, "Dans les deux sens (↔)");
        es(BOTH_WAYS, "En ambos sentidos (↔)");
        de(BOTH_WAYS, "In beide Richtungen (↔)");
        ko(BOTH_WAYS, "양방향 (↔)");
        it(BOTH_WAYS, "In entrambi i sensi (↔)");

        ja(NEW_CHARACTER, "新しいキャラクター");
        zh(NEW_CHARACTER, "新角色");
        fr(NEW_CHARACTER, "Nouveau personnage");
        es(NEW_CHARACTER, "Personaje nuevo");
        de(NEW_CHARACTER, "Neue Figur");
        ko(NEW_CHARACTER, "새 캐릭터");
        it(NEW_CHARACTER, "Nuovo personaggio");

        ja(SAVE_THE_CHARACTER_TO_ADD, "関係を追加するには、先にキャラクターを保存してください。");
        zh(SAVE_THE_CHARACTER_TO_ADD, "请先保存角色，再添加其关系。");
        fr(SAVE_THE_CHARACTER_TO_ADD, "Enregistrez le personnage pour ajouter ses relations.");
        es(SAVE_THE_CHARACTER_TO_ADD, "Guarda el personaje para añadir sus relaciones.");
        de(SAVE_THE_CHARACTER_TO_ADD, "Speichere die Figur, um ihre Beziehungen hinzuzufügen.");
        ko(SAVE_THE_CHARACTER_TO_ADD, "관계를 추가하려면 먼저 캐릭터를 저장하세요.");
        it(SAVE_THE_CHARACTER_TO_ADD, "Salva il personaggio per aggiungere le sue relazioni.");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");
        ko(SAVE, "저장");
        it(SAVE, "Salva");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");
        it(CANCEL, "Annulla");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");

        ja(TAGLINE, "キャッチフレーズ");
        zh(TAGLINE, "标语");
        fr(TAGLINE, "Accroche");
        es(TAGLINE, "Lema");
        de(TAGLINE, "Kurzbeschreibung");
        ko(TAGLINE, "한 줄 소개");
        it(TAGLINE, "Slogan");

        ja(NOTE, "メモ");
        zh(NOTE, "备注");
        fr(NOTE, "Note");
        es(NOTE, "Nota");
        de(NOTE, "Notiz");
        ko(NOTE, "메모");
        it(NOTE, "Nota");

        ja(ICON, "アイコン");
        zh(ICON, "图标");
        fr(ICON, "Icône");
        es(ICON, "Icono");
        de(ICON, "Symbol");
        ko(ICON, "아이콘");
        it(ICON, "Icona");

        ja(THE_FILE, "ファイルは読み取れる画像ではありません");
        zh(THE_FILE, "该文件不是可读取的图片");
        fr(THE_FILE, "Le fichier n'est pas une image lisible");
        es(THE_FILE, "El archivo no es una imagen que se pueda leer");
        de(THE_FILE, "Die Datei ist kein lesbares Bild");
        ko(THE_FILE, "읽을 수 있는 이미지 파일이 아닙니다");
        it(THE_FILE, "Il file non è un'immagine leggibile");

        ja(UPLOAD_ICON, "アイコンをアップロード");
        zh(UPLOAD_ICON, "上传图标");
        fr(UPLOAD_ICON, "Téléverser une icône");
        es(UPLOAD_ICON, "Subir icono");
        de(UPLOAD_ICON, "Symbol hochladen");
        ko(UPLOAD_ICON, "아이콘 업로드");
        it(UPLOAD_ICON, "Carica icona");

        ja(THE_FILE_IS_NOT_AN_IMAGE, "ファイルが画像ではないか、大きすぎます");
        zh(THE_FILE_IS_NOT_AN_IMAGE, "该文件不是图片，或者太大");
        fr(THE_FILE_IS_NOT_AN_IMAGE, "Le fichier n'est pas une image ou il est trop gros");
        es(THE_FILE_IS_NOT_AN_IMAGE, "El archivo no es una imagen o es demasiado grande");
        de(THE_FILE_IS_NOT_AN_IMAGE, "Die Datei ist kein Bild oder sie ist zu groß");
        ko(THE_FILE_IS_NOT_AN_IMAGE, "이미지가 아니거나 너무 큽니다");
        it(THE_FILE_IS_NOT_AN_IMAGE, "Il file non è un'immagine oppure è troppo grande");

        ja(REMOVE_ICON, "アイコンを削除");
        zh(REMOVE_ICON, "移除图标");
        fr(REMOVE_ICON, "Retirer l'icône");
        es(REMOVE_ICON, "Quitar icono");
        de(REMOVE_ICON, "Symbol entfernen");
        ko(REMOVE_ICON, "아이콘 제거");
        it(REMOVE_ICON, "Rimuovi icona");

        ja(ICON_SCALED_DOWN, "アイコン（最大 %1$d x %1$d ピクセルに縮小されます）");
        zh(ICON_SCALED_DOWN, "图标（将缩小到最大 %1$d x %1$d 像素）");
        fr(ICON_SCALED_DOWN, "Icône (réduite à %1$d x %1$d pixels au plus)");
        es(ICON_SCALED_DOWN, "Icono (reducido a un máximo de %1$d x %1$d píxeles)");
        de(ICON_SCALED_DOWN, "Symbol (auf höchstens %1$d x %1$d Pixel verkleinert)");
        ko(ICON_SCALED_DOWN, "아이콘 (최대 %1$d x %1$d 픽셀로 축소됨)");
        it(ICON_SCALED_DOWN, "Icona (ridotta ad al massimo %1$d x %1$d pixel)");

        ja(COLOR, "色");
        zh(COLOR, "颜色");
        fr(COLOR, "Couleur");
        es(COLOR, "Color");
        de(COLOR, "Farbe");
        ko(COLOR, "색상");
        it(COLOR, "Colore");

        ja(CHARACTER, "キャラクター");
        zh(CHARACTER, "角色");
        fr(CHARACTER, "Personnage");
        es(CHARACTER, "Personaje");
        de(CHARACTER, "Figur");
        ko(CHARACTER, "캐릭터");
        it(CHARACTER, "Personaggio");

        ja(DELETE_RELATIONSHIP, "関係を削除");
        zh(DELETE_RELATIONSHIP, "删除关系");
        fr(DELETE_RELATIONSHIP, "Supprimer la relation");
        es(DELETE_RELATIONSHIP, "Eliminar relación");
        de(DELETE_RELATIONSHIP, "Beziehung löschen");
        ko(DELETE_RELATIONSHIP, "관계 삭제");
        it(DELETE_RELATIONSHIP, "Elimina la relazione");

        ja(RELATIONSHIPS, "関係");
        zh(RELATIONSHIPS, "关系");
        fr(RELATIONSHIPS, "Relations");
        es(RELATIONSHIPS, "Relaciones");
        de(RELATIONSHIPS, "Beziehungen");
        ko(RELATIONSHIPS, "관계");
        it(RELATIONSHIPS, "Relazioni");

        ja(SEARCH_CHARACTER, "キャラクターを検索");
        zh(SEARCH_CHARACTER, "搜索角色");
        fr(SEARCH_CHARACTER, "Rechercher un personnage");
        es(SEARCH_CHARACTER, "Buscar personaje");
        de(SEARCH_CHARACTER, "Figur suchen");
        ko(SEARCH_CHARACTER, "캐릭터 검색");
        it(SEARCH_CHARACTER, "Cerca un personaggio");

        ja(DIRECTION, "方向");
        zh(DIRECTION, "方向");
        fr(DIRECTION, "Sens");
        es(DIRECTION, "Dirección");
        de(DIRECTION, "Richtung");
        ko(DIRECTION, "방향");
        it(DIRECTION, "Direzione");

        ja(ADD_RELATIONSHIP, "関係を追加");
        zh(ADD_RELATIONSHIP, "添加关系");
        fr(ADD_RELATIONSHIP, "Ajouter une relation");
        es(ADD_RELATIONSHIP, "Añadir relación");
        de(ADD_RELATIONSHIP, "Beziehung hinzufügen");
        ko(ADD_RELATIONSHIP, "관계 추가");
        it(ADD_RELATIONSHIP, "Aggiungi una relazione");

        ja(CHOOSE_A_CHARACTER_AND_ENTER, "キャラクターを選択し、関係の名前を入力してください");
        zh(CHOOSE_A_CHARACTER_AND_ENTER, "请选择角色并输入关系的名称");
        fr(CHOOSE_A_CHARACTER_AND_ENTER, "Choisissez un personnage et saisissez le nom de la relation");
        es(CHOOSE_A_CHARACTER_AND_ENTER, "Elige un personaje e introduce el nombre de la relación");
        de(CHOOSE_A_CHARACTER_AND_ENTER, "Wähle eine Figur und gib den Namen der Beziehung ein");
        ko(CHOOSE_A_CHARACTER_AND_ENTER, "캐릭터를 선택하고 관계의 이름을 입력하세요");
        it(CHOOSE_A_CHARACTER_AND_ENTER, "Scegli un personaggio e inserisci il nome della relazione");

        ja(THERE_IS_NO_OTHER_CHARACTER, "他のキャラクターはまだいません");
        zh(THERE_IS_NO_OTHER_CHARACTER, "还没有其他角色");
        fr(THERE_IS_NO_OTHER_CHARACTER, "Il n'y a pas encore d'autre personnage");
        es(THERE_IS_NO_OTHER_CHARACTER, "Todavía no hay otro personaje");
        de(THERE_IS_NO_OTHER_CHARACTER, "Es gibt noch keine andere Figur");
        ko(THERE_IS_NO_OTHER_CHARACTER, "아직 다른 캐릭터가 없습니다");
        it(THERE_IS_NO_OTHER_CHARACTER, "Non c'è ancora un altro personaggio");

        ja(THERE_ARE_UNSAVED_CHANGES_DISCARD, "保存されていない変更があります。破棄しますか？");
        zh(THERE_ARE_UNSAVED_CHANGES_DISCARD, "有未保存的更改。要放弃吗？");
        fr(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Il y a des modifications non enregistrées. Les abandonner ?");
        es(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Hay cambios sin guardar. ¿Descartarlos?");
        de(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Es gibt nicht gespeicherte Änderungen. Verwerfen?");
        ko(THERE_ARE_UNSAVED_CHANGES_DISCARD, "저장하지 않은 변경 사항이 있습니다. 버리시겠습니까?");
        it(THERE_ARE_UNSAVED_CHANGES_DISCARD, "Ci sono modifiche non salvate. Scartarle?");

        ja(THE_NAME_IS_REQUIRED, "名前は必須です");
        zh(THE_NAME_IS_REQUIRED, "名称为必填项");
        fr(THE_NAME_IS_REQUIRED, "Le nom est obligatoire");
        es(THE_NAME_IS_REQUIRED, "El nombre es obligatorio");
        de(THE_NAME_IS_REQUIRED, "Der Name ist erforderlich");
        ko(THE_NAME_IS_REQUIRED, "이름은 필수입니다");
        it(THE_NAME_IS_REQUIRED, "Il nome è obbligatorio");

        ja(THE_LOREBOOK_OF_THE_BOOK, "このブックのロアブックにはエントリがありません。");
        zh(THE_LOREBOOK_OF_THE_BOOK, "该书的世界书中没有条目。");
        fr(THE_LOREBOOK_OF_THE_BOOK, "Le lorebook du livre n'a pas d'entrées.");
        es(THE_LOREBOOK_OF_THE_BOOK, "El lorebook del libro no tiene entradas.");
        de(THE_LOREBOOK_OF_THE_BOOK, "Das Lorebook des Buchs hat keine Einträge.");
        ko(THE_LOREBOOK_OF_THE_BOOK, "책의 로어북에 항목이 없습니다.");
        it(THE_LOREBOOK_OF_THE_BOOK, "Il lorebook del libro non ha voci.");

        ja(IMPORT, "インポート");
        zh(IMPORT, "导入");
        fr(IMPORT, "Importer");
        es(IMPORT, "Importar");
        de(IMPORT, "Importieren");
        ko(IMPORT, "가져오기");
        it(IMPORT, "Importa");

        ja(EMPTY, "（空）");
        zh(EMPTY, "（空）");
        fr(EMPTY, "(vide)");
        es(EMPTY, "(vacío)");
        de(EMPTY, "(leer)");
        ko(EMPTY, "(비어 있음)");
        it(EMPTY, "(vuoto)");

        ja(IMPORT_D, "インポート（%d）");
        zh(IMPORT_D, "导入（%d）");
        fr(IMPORT_D, "Importer (%d)");
        es(IMPORT_D, "Importar (%d)");
        de(IMPORT_D, "Importieren (%d)");
        ko(IMPORT_D, "가져오기 (%d)");
        it(IMPORT_D, "Importa (%d)");

        ja(DELETE_THE_RELATIONSHIP, "関係「%s」を削除しますか？");
        zh(DELETE_THE_RELATIONSHIP, "要删除关系“%s”吗？");
        fr(DELETE_THE_RELATIONSHIP, "Supprimer la relation %s ?");
        es(DELETE_THE_RELATIONSHIP, "¿Eliminar la relación %s?");
        de(DELETE_THE_RELATIONSHIP, "Die Beziehung %s löschen?");
        ko(DELETE_THE_RELATIONSHIP, "관계 %s을(를) 삭제하시겠습니까?");
        it(DELETE_THE_RELATIONSHIP, "Eliminare la relazione %s?");

        ja(PICK_THE_POV_CHARACTER, "視点キャラクターを選択");
        zh(PICK_THE_POV_CHARACTER, "选择视角角色");
        fr(PICK_THE_POV_CHARACTER, "Choisir le personnage du point de vue");
        es(PICK_THE_POV_CHARACTER, "Elige el personaje del punto de vista");
        de(PICK_THE_POV_CHARACTER, "Die POV-Figur wählen");
        ko(PICK_THE_POV_CHARACTER, "시점 인물 선택");
        it(PICK_THE_POV_CHARACTER, "Scegli il personaggio POV");

        ja(ADD_A_PRESENT_CHARACTER, "登場キャラクターを追加");
        zh(ADD_A_PRESENT_CHARACTER, "添加在场角色");
        fr(ADD_A_PRESENT_CHARACTER, "Ajouter un personnage présent");
        es(ADD_A_PRESENT_CHARACTER, "Añadir un personaje presente");
        de(ADD_A_PRESENT_CHARACTER, "Eine anwesende Figur hinzufügen");
        ko(ADD_A_PRESENT_CHARACTER, "등장인물 추가");
        it(ADD_A_PRESENT_CHARACTER, "Aggiungi un personaggio presente");

        ja(THERE_ARE_NO_CHARACTERS_YET, "キャラクターはまだいません");
        zh(THERE_ARE_NO_CHARACTERS_YET, "还没有角色");
        fr(THERE_ARE_NO_CHARACTERS_YET, "Il n'y a pas encore de personnages");
        es(THERE_ARE_NO_CHARACTERS_YET, "Todavía no hay personajes");
        de(THERE_ARE_NO_CHARACTERS_YET, "Es gibt noch keine Figuren");
        ko(THERE_ARE_NO_CHARACTERS_YET, "아직 캐릭터가 없습니다");
        it(THERE_ARE_NO_CHARACTERS_YET, "Non ci sono ancora personaggi");
    }
}
