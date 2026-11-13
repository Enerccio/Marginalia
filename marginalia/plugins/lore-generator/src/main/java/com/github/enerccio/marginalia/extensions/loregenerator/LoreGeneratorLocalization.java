package com.github.enerccio.marginalia.extensions.loregenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.loregenerator.service.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Lore Generator in the languages besides English; the English text is the key.
 */
public class LoreGeneratorLocalization extends PluginLocalizationBundle {

    public static final LoreGeneratorLocalization T = new LoreGeneratorLocalization();

    // the built-in prompts of the plugin, one object per prompt language
    private final Defaults englishDefaults = new Defaults();
    private final Map<Language, Defaults> translatedDefaults = Map.of(JA, new DefaultsJA(), ZH, new DefaultsZH(), FR, new DefaultsFR(), ES, new DefaultsES(), DE, new DefaultsDE(),
            IT, new DefaultsIT(), KO, new DefaultsKO());

    /**
     * The built-in prompts of the plugin in the prompt language of the user.
     */
    public Defaults defaults() {
        return forPromptLanguage(englishDefaults, translatedDefaults);
    }

    public static final String LORE_GENERATOR = "Lore Generator";
    public static final String GENERATE_LORE_ENTRY = "Generate lore entry";
    public static final String SAVE_PROMPT_AS = "Save prompt as";
    public static final String NAME = "Name";
    public static final String SAVE = "Save";
    public static final String NAME_CANT_BE_EMPTY = "Name can't be empty";
    public static final String CANCEL = "Cancel";
    public static final String GENERATE_LORE_ENTRY_S = "Generate lore entry: %s";
    public static final String THIS_LORE_ENTRY_ALREADY_HAS = "This lore entry already has content. Copying the result to it replaces the content and the note.";
    public static final String ALL_LORE = "All Lore";
    public static final String CURRENT_LOREBOOK = "Current Lorebook";
    public static final String ALL_ENTRIES = "All entries";
    public static final String ONLY_UP_TO_POSITION = "Only up to position (%d)";
    public static final String GENERATION_PROMPT = "Generation prompt";
    public static final String SAVE_AS = "Save as";
    public static final String DELETE = "Delete";
    public static final String PROMPT = "Prompt";
    public static final String RUN_GENERATION = "Run Generation";
    public static final String RUN_COMPACTION = "Run Compaction";
    public static final String GENERATION = "Generation";
    public static final String COMPACTION = "Compaction";
    public static final String EXIT = "Exit";
    public static final String COPY_TO_LORE_ENTRY = "Copy to Lore Entry";
    public static final String PROMPT_SAVED = "Prompt saved";
    public static final String PROMPT_ALREADY_EXISTS_OVERWRITE = "Prompt '%s' already exists, overwrite it?";
    public static final String DELETE_PROMPT = "Delete prompt '%s'?";
    public static final String THE_PROMPT_IS_EMPTY = "The prompt is empty";
    public static final String CHOOSE_A_MODEL = "Choose a model";
    public static final String COPIED_TO_THE_LORE_ENTRY = "Copied to the lore entry";
    public static final String ATTACHMENTS_IMAGES_AND_FILES = "Attachments (images and files sent with the generation)";
    public static final String ATTACHMENT = "attachment";
    public static final String UNKNOWN_TYPE = "unknown type";
    public static final String LINK = "link";
    public static final String FORMAT = "%s (%s, %s)";
    public static final String KB = "%d kB";
    public static final String DELETE_ATTACHMENT = "Delete attachment";
    public static final String THINKING_PROCESS = "Thinking Process";
    public static final String STOP = "Stop";
    public static final String COMPACTION_PROMPT = "Compaction prompt";
    public static final String GENERATING_THE_TEXT = "Generating the text of a lore entry";
    public static final String COMPACTING_THE_TEXT = "Compacting the text of a lore entry";
    public static final String LORE_GENERATOR_GENERATE_LORE = "Lore Generator: generate lore";
    public static final String LORE_GENERATOR_COMPACT_LORE = "Lore Generator: compact lore";

    @Override
    protected void loadKeys() {
        ja(LORE_GENERATOR, "ロアジェネレーター");
        zh(LORE_GENERATOR, "世界书生成器");
        fr(LORE_GENERATOR, "Générateur de lore");
        es(LORE_GENERATOR, "Generador de lore");
        de(LORE_GENERATOR, "Lore Generator");

        ja(GENERATE_LORE_ENTRY, "ロアエントリを生成");
        zh(GENERATE_LORE_ENTRY, "生成世界书条目");
        fr(GENERATE_LORE_ENTRY, "Générer une entrée de lore");
        es(GENERATE_LORE_ENTRY, "Generar entrada de lore");
        de(GENERATE_LORE_ENTRY, "Lore-Eintrag generieren");

        ja(SAVE_PROMPT_AS, "プロンプトを名前を付けて保存");
        zh(SAVE_PROMPT_AS, "提示词另存为");
        fr(SAVE_PROMPT_AS, "Enregistrer le prompt sous");
        es(SAVE_PROMPT_AS, "Guardar el prompt como");
        de(SAVE_PROMPT_AS, "Prompt speichern unter");

        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");

        ja(NAME_CANT_BE_EMPTY, "名前は空にできません");
        zh(NAME_CANT_BE_EMPTY, "名称不能为空");
        fr(NAME_CANT_BE_EMPTY, "Le nom ne peut pas être vide");
        es(NAME_CANT_BE_EMPTY, "El nombre no puede estar vacío");
        de(NAME_CANT_BE_EMPTY, "Der Name darf nicht leer sein");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(GENERATE_LORE_ENTRY_S, "ロアエントリを生成: %s");
        zh(GENERATE_LORE_ENTRY_S, "生成世界书条目：%s");
        fr(GENERATE_LORE_ENTRY_S, "Générer une entrée de lore : %s");
        es(GENERATE_LORE_ENTRY_S, "Generar entrada de lore: %s");
        de(GENERATE_LORE_ENTRY_S, "Lore-Eintrag generieren: %s");

        ja(THIS_LORE_ENTRY_ALREADY_HAS, "このロアエントリには既に内容があります。結果をコピーすると、内容とメモが置き換えられます。");
        zh(THIS_LORE_ENTRY_ALREADY_HAS, "此世界书条目已有内容。复制结果将替换其内容和备注。");
        fr(THIS_LORE_ENTRY_ALREADY_HAS, "Cette entrée de lore a déjà du contenu. Y copier le résultat remplace le contenu et la note.");
        es(THIS_LORE_ENTRY_ALREADY_HAS, "Esta entrada de lore ya tiene contenido. Copiar el resultado en ella reemplaza el contenido y la nota.");
        de(THIS_LORE_ENTRY_ALREADY_HAS, "Dieser Lore-Eintrag hat bereits Inhalt. Das Kopieren des Ergebnisses ersetzt den Inhalt und die Notiz.");

        ja(ALL_LORE, "すべてのロア");
        zh(ALL_LORE, "所有世界书");
        fr(ALL_LORE, "Tout le lore");
        es(ALL_LORE, "Todo el lore");
        de(ALL_LORE, "Gesamte Lore");

        ja(CURRENT_LOREBOOK, "現在のロアブック");
        zh(CURRENT_LOREBOOK, "当前世界书");
        fr(CURRENT_LOREBOOK, "Lorebook actuel");
        es(CURRENT_LOREBOOK, "Lorebook actual");
        de(CURRENT_LOREBOOK, "Aktuelles Lorebook");

        ja(ALL_ENTRIES, "すべてのエントリ");
        zh(ALL_ENTRIES, "所有条目");
        fr(ALL_ENTRIES, "Toutes les entrées");
        es(ALL_ENTRIES, "Todas las entradas");
        de(ALL_ENTRIES, "Alle Einträge");

        ja(ONLY_UP_TO_POSITION, "位置（%d）まで");
        zh(ONLY_UP_TO_POSITION, "仅到位置（%d）");
        fr(ONLY_UP_TO_POSITION, "Seulement jusqu'à la position (%d)");
        es(ONLY_UP_TO_POSITION, "Solo hasta la posición (%d)");
        de(ONLY_UP_TO_POSITION, "Nur bis Position (%d)");

        ja(GENERATION_PROMPT, "生成プロンプト");
        zh(GENERATION_PROMPT, "生成提示词");
        fr(GENERATION_PROMPT, "Prompt de génération");
        es(GENERATION_PROMPT, "Prompt de generación");
        de(GENERATION_PROMPT, "Generierungsprompt");

        ja(SAVE_AS, "名前を付けて保存");
        zh(SAVE_AS, "另存为");
        fr(SAVE_AS, "Enregistrer sous");
        es(SAVE_AS, "Guardar como");
        de(SAVE_AS, "Speichern unter");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");

        ja(PROMPT, "プロンプト");
        zh(PROMPT, "提示词");
        fr(PROMPT, "Prompt");
        es(PROMPT, "Prompt");
        de(PROMPT, "Prompt");

        ja(RUN_GENERATION, "生成を実行");
        zh(RUN_GENERATION, "运行生成");
        fr(RUN_GENERATION, "Lancer la génération");
        es(RUN_GENERATION, "Ejecutar la generación");
        de(RUN_GENERATION, "Generierung ausführen");

        ja(RUN_COMPACTION, "圧縮を実行");
        zh(RUN_COMPACTION, "运行压缩");
        fr(RUN_COMPACTION, "Lancer le compactage");
        es(RUN_COMPACTION, "Ejecutar la compactación");
        de(RUN_COMPACTION, "Verdichtung ausführen");

        ja(GENERATION, "生成");
        zh(GENERATION, "生成");
        fr(GENERATION, "Génération");
        es(GENERATION, "Generación");
        de(GENERATION, "Generierung");

        ja(COMPACTION, "圧縮");
        zh(COMPACTION, "压缩");
        fr(COMPACTION, "Compactage");
        es(COMPACTION, "Compactación");
        de(COMPACTION, "Verdichtung");

        ja(EXIT, "終了");
        zh(EXIT, "退出");
        fr(EXIT, "Quitter");
        es(EXIT, "Salir");
        de(EXIT, "Beenden");

        ja(COPY_TO_LORE_ENTRY, "ロアエントリにコピー");
        zh(COPY_TO_LORE_ENTRY, "复制到世界书条目");
        fr(COPY_TO_LORE_ENTRY, "Copier dans l'entrée de lore");
        es(COPY_TO_LORE_ENTRY, "Copiar a la entrada de lore");
        de(COPY_TO_LORE_ENTRY, "In Lore-Eintrag kopieren");

        ja(PROMPT_SAVED, "プロンプトを保存しました");
        zh(PROMPT_SAVED, "提示词已保存");
        fr(PROMPT_SAVED, "Prompt enregistré");
        es(PROMPT_SAVED, "Prompt guardado");
        de(PROMPT_SAVED, "Prompt gespeichert");

        ja(PROMPT_ALREADY_EXISTS_OVERWRITE, "プロンプト '%s' は既に存在します。上書きしますか？");
        zh(PROMPT_ALREADY_EXISTS_OVERWRITE, "提示词“%s”已存在，要覆盖吗？");
        fr(PROMPT_ALREADY_EXISTS_OVERWRITE, "Le prompt '%s' existe déjà, l'écraser ?");
        es(PROMPT_ALREADY_EXISTS_OVERWRITE, "El prompt '%s' ya existe, ¿reemplazarlo?");
        de(PROMPT_ALREADY_EXISTS_OVERWRITE, "Der Prompt „%s“ existiert bereits, überschreiben?");

        ja(DELETE_PROMPT, "プロンプト '%s' を削除しますか？");
        zh(DELETE_PROMPT, "要删除提示词“%s”吗？");
        fr(DELETE_PROMPT, "Supprimer le prompt '%s' ?");
        es(DELETE_PROMPT, "¿Eliminar el prompt '%s'?");
        de(DELETE_PROMPT, "Den Prompt „%s“ löschen?");

        ja(THE_PROMPT_IS_EMPTY, "プロンプトが空です");
        zh(THE_PROMPT_IS_EMPTY, "提示词为空");
        fr(THE_PROMPT_IS_EMPTY, "Le prompt est vide");
        es(THE_PROMPT_IS_EMPTY, "El prompt está vacío");
        de(THE_PROMPT_IS_EMPTY, "Der Prompt ist leer");

        ja(CHOOSE_A_MODEL, "モデルを選択してください");
        zh(CHOOSE_A_MODEL, "请选择模型");
        fr(CHOOSE_A_MODEL, "Choisissez un modèle");
        es(CHOOSE_A_MODEL, "Elige un modelo");
        de(CHOOSE_A_MODEL, "Wähle ein Modell");

        ja(COPIED_TO_THE_LORE_ENTRY, "ロアエントリにコピーしました");
        zh(COPIED_TO_THE_LORE_ENTRY, "已复制到世界书条目");
        fr(COPIED_TO_THE_LORE_ENTRY, "Copié dans l'entrée de lore");
        es(COPIED_TO_THE_LORE_ENTRY, "Copiado a la entrada de lore");
        de(COPIED_TO_THE_LORE_ENTRY, "In den Lore-Eintrag kopiert");

        ja(ATTACHMENTS_IMAGES_AND_FILES, "添付ファイル（生成と一緒に送信される画像とファイル）");
        zh(ATTACHMENTS_IMAGES_AND_FILES, "附件（随生成一起发送的图片和文件）");
        fr(ATTACHMENTS_IMAGES_AND_FILES, "Pièces jointes (images et fichiers envoyés avec la génération)");
        es(ATTACHMENTS_IMAGES_AND_FILES, "Adjuntos (imágenes y archivos enviados con la generación)");
        de(ATTACHMENTS_IMAGES_AND_FILES, "Anhänge (Bilder und Dateien, die mit der Generierung gesendet werden)");

        ja(ATTACHMENT, "添付ファイル");
        zh(ATTACHMENT, "附件");
        fr(ATTACHMENT, "pièce jointe");
        es(ATTACHMENT, "adjunto");
        de(ATTACHMENT, "Anhang");

        ja(UNKNOWN_TYPE, "不明な形式");
        zh(UNKNOWN_TYPE, "未知类型");
        fr(UNKNOWN_TYPE, "type inconnu");
        es(UNKNOWN_TYPE, "tipo desconocido");
        de(UNKNOWN_TYPE, "unbekannter Typ");

        ja(LINK, "リンク");
        zh(LINK, "链接");
        fr(LINK, "lien");
        es(LINK, "enlace");
        de(LINK, "Link");

        ja(FORMAT, "%s（%s、%s）");
        zh(FORMAT, "%s（%s，%s）");
        fr(FORMAT, "%s (%s, %s)");
        es(FORMAT, "%s (%s, %s)");
        de(FORMAT, "%s (%s, %s)");

        ja(KB, "%d kB");
        zh(KB, "%d kB");
        fr(KB, "%d ko");
        es(KB, "%d kB");
        de(KB, "%d kB");

        ja(DELETE_ATTACHMENT, "添付ファイルを削除");
        zh(DELETE_ATTACHMENT, "删除附件");
        fr(DELETE_ATTACHMENT, "Supprimer la pièce jointe");
        es(DELETE_ATTACHMENT, "Eliminar adjunto");
        de(DELETE_ATTACHMENT, "Anhang löschen");

        ja(THINKING_PROCESS, "思考プロセス");
        zh(THINKING_PROCESS, "思考过程");
        fr(THINKING_PROCESS, "Réflexion");
        es(THINKING_PROCESS, "Proceso de razonamiento");
        de(THINKING_PROCESS, "Denkprozess");

        ja(STOP, "停止");
        zh(STOP, "停止");
        fr(STOP, "Arrêter");
        es(STOP, "Detener");
        de(STOP, "Stopp");

        ja(COMPACTION_PROMPT, "圧縮プロンプト");
        zh(COMPACTION_PROMPT, "压缩提示词");
        fr(COMPACTION_PROMPT, "Prompt de compactage");
        es(COMPACTION_PROMPT, "Prompt de compactación");
        de(COMPACTION_PROMPT, "Verdichtungsprompt");

        ja(GENERATING_THE_TEXT, "ロアエントリのテキストを生成しています");
        zh(GENERATING_THE_TEXT, "正在生成世界书条目的文本");
        fr(GENERATING_THE_TEXT, "Génération du texte d'une entrée de lore");
        es(GENERATING_THE_TEXT, "Generando el texto de una entrada de lore");
        de(GENERATING_THE_TEXT, "Der Text eines Lore-Eintrags wird generiert");

        ja(COMPACTING_THE_TEXT, "ロアエントリのテキストを圧縮しています");
        zh(COMPACTING_THE_TEXT, "正在压缩世界书条目的文本");
        fr(COMPACTING_THE_TEXT, "Compactage du texte d'une entrée de lore");
        es(COMPACTING_THE_TEXT, "Compactando el texto de una entrada de lore");
        de(COMPACTING_THE_TEXT, "Der Text eines Lore-Eintrags wird verdichtet");

        ja(LORE_GENERATOR_GENERATE_LORE, "ロアジェネレーター: ロアを生成");
        zh(LORE_GENERATOR_GENERATE_LORE, "设定生成器：生成设定");
        fr(LORE_GENERATOR_GENERATE_LORE, "Générateur de lore : générer du lore");
        es(LORE_GENERATOR_GENERATE_LORE, "Generador de lore: generar lore");
        de(LORE_GENERATOR_GENERATE_LORE, "Lore Generator: Lore generieren");

        ja(LORE_GENERATOR_COMPACT_LORE, "ロアジェネレーター: ロアを圧縮");
        zh(LORE_GENERATOR_COMPACT_LORE, "设定生成器：压缩设定");
        fr(LORE_GENERATOR_COMPACT_LORE, "Générateur de lore : compacter du lore");
        es(LORE_GENERATOR_COMPACT_LORE, "Generador de lore: compactar lore");
        de(LORE_GENERATOR_COMPACT_LORE, "Lore Generator: Lore verdichten");
    }
}
