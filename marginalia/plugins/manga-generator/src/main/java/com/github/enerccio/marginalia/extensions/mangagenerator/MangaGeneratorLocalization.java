package com.github.enerccio.marginalia.extensions.mangagenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.*;
import com.github.enerccio.marginalia.loc.Language;

import java.util.Map;

/**
 * Texts of the Manga Generator in the languages besides English; the English text is the key.
 */
public class MangaGeneratorLocalization extends PluginLocalizationBundle {

    public static final MangaGeneratorLocalization T = new MangaGeneratorLocalization();

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

    public static final String MANGA_GENERATOR = "Manga Generator";
    public static final String OPEN_A_BOOK_FIRST = "Open a book first";
    public static final String NEW_MANGA = "New manga";
    public static final String RENAME_THE_MANGA = "Rename the manga";
    public static final String MOVE_THE_MANGA = "Move the manga to the left";
    public static final String MOVE_THE_MANGA_TO_THE_RIGHT = "Move the manga to the right";
    public static final String DELETE_THE_MANGA = "Delete the manga";
    public static final String TOKEN_BUDGET = "Token budget";
    public static final String HOW_LONG_A_SCRIPT = "How long a script the model is asked for";
    public static final String PARTS_PER_SECTION = "Parts per section";
    public static final String HOW_MANY_PARTS = "How many parts of the story a new section adapts";
    public static final String CONTEXT_BEFORE = "Context before";
    public static final String PARTS_BEFORE_THE_ADAPTED_ONES = "Parts before the adapted ones that a new section gives the model as the background";
    public static final String CONTEXT_AFTER = "Context after";
    public static final String PARTS_AFTER_THE_ADAPTED_ONES = "Parts after the adapted ones that a new section gives the model as the background";
    public static final String SCRIPT_PROMPT = "Script prompt";
    public static final String TAGS_PROMPT = "Tags prompt";
    public static final String ADDITIONAL_RULES = "Additional rules";
    public static final String PUT_IN_PLACE_OF_ADDITIONALRULES = "Put in place of {{additionalRules}} in the script prompt";
    public static final String COPY_ALL = "Copy all";
    public static final String ADD_SECTION = "Add section";
    public static final String CONTINUOUS_MODE = "Continuous mode";
    public static final String STOP_CONTINUOUS_MODE = "Stop continuous mode";
    public static final String SCRIPTS_ONLY = "Scripts only";
    public static final String TAGS_ONLY = "Tags only";
    public static final String WHAT_THE_CONTINUOUS_MODE_GENERATES = "What the continuous mode generates: it does one kind at a time, so the model keeps the same prompt";
    public static final String GENERATE_THE_TAGS = "Generate the tags of the sections that have a script but no tags, until there are none or you stop it";
    public static final String GENERATE_THE_SCRIPT = "Generate the script of the sections that have none, and keep adding sections for the next parts of the story and generating their scripts, until the story runs out or you stop it";
    public static final String CONTINUOUS_MODE_IS_FINISHED = "Continuous mode is finished: every script has its tags";
    public static final String CONTINUOUS_MODE_IS_FINISHED_THERE = "Continuous mode is finished: there are no more parts of the story";
    public static final String CONTINUOUS_MODE_STOPPED = "Continuous mode stopped";
    public static final String CONTINUOUS_MODE_STOPPED_THE_MODEL = "Continuous mode stopped: the model wrote nothing";
    public static final String THERE_IS_NO_MANGA = "There is no manga in this book yet. Create one with the plus.";
    public static final String CLOSE = "Close";
    public static final String NO_SECTIONS_YET_ADD_ONE = "No sections yet. Add one to start with the first parts of the story.";
    public static final String MANGA_SCRIPT = "Manga Script %d";
    public static final String TITLE = "Title";
    public static final String SAVE = "Save";
    public static final String CANCEL = "Cancel";
    public static final String DELETE_THE_MANGA_THIS_CANNOT = "Delete the manga \"%s\"? This cannot be undone.";
    public static final String EMPTY_OR_UNCHANGED_THE_BUILT = "Empty or unchanged: the built-in text is used";
    public static final String THE_STORY_HAS_NO_PARTS = "The story has no parts yet";
    public static final String NO_NEW_PARTS = "No new parts of the story for another section (the story has %d parts)";
    public static final String THERE_IS_NO_SCRIPT = "There is no script or tags to copy";
    public static final String ALL_SCRIPTS_AND_TAGS_COPIED = "All scripts and tags copied";
    public static final String SECTION = "Section %d";
    public static final String SCRIPT = "Script";
    public static final String TAGS = "Tags";
    public static final String OVERRIDE = "Override";
    public static final String SCRIPT_PROMPT_OF_THIS_SECTION = "Script prompt of this section";
    public static final String TAGS_PROMPT_OF_THIS_SECTION = "Tags prompt of this section";
    public static final String ADDITIONAL_RULES_OF_THIS_SECTION = "Additional rules of this section";
    public static final String RULES_ADDENDUM = "Rules addendum";
    public static final String RULES_ADDED_TO_THE_RULES = "Rules added to the rules of the manga";
    public static final String ADAPT_FROM_PART = "Adapt from part";
    public static final String TO_PART = "to part";
    public static final String CONTEXT_FROM_PART = "Context from part";
    public static final String EDIT_THE_TEXT = "Edit the text";
    public static final String COPY = "Copy";
    public static final String SHOW_THE_REQUEST_THE_MODEL = "Show the request the model gets";
    public static final String DELETE_THIS_SECTION = "Delete this section?";
    public static final String DELETE = "Delete";
    public static final String THOUGHT = "Thought for %s s";
    public static final String THINKING = "Thinking for %s s";
    public static final String WAITING_FOR_THE_MODEL = "Waiting for the model...";
    public static final String NO_TAGS_YET_GENERATE_THEM = "No tags yet. Generate them from the script, or write them.";
    public static final String NO_SCRIPT_YET_GENERATE = "No script yet. Generate it, or write it.";
    public static final String ABORT = "Abort";
    public static final String GENERATE_TAGS = "Generate tags";
    public static final String GENERATE_SCRIPT = "Generate script";
    public static final String TAGS_COPIED = "Tags copied";
    public static final String SCRIPT_COPIED = "Script copied";
    public static final String EMPTY_OR_UNCHANGED_THE_SETTING = "Empty or unchanged: the setting of the manga is used";
    public static final String REQUEST = "Request";
    public static final String ABOUT_TOKENS = "About %d tokens";
    public static final String OF_THE_MODEL_HAS_ROOM = "of %d the model has room for (its context without the reply)";
    public static final String LORE_ENTRIES = "Lore entries: %d";
    public static final String MORE_DID_NOT_FIT = "(%d more did not fit)";
    public static final String THE_REQUEST_ABOUT_TOKENS = "The request (about %d tokens) is more than the model has room for (%d)";
    public static final String LORE_ENTRIES_WERE_LEFT_OUT = "%d lore entries were left out to fit the context";
    public static final String RESET_TO_DEFAULT = "Reset to default";
    public static final String A_NEW_MANGA_STARTS = "A new manga starts with these settings. A manga you made already keeps its own.";
    public static final String TEXT_BEFORE_EVERY_REQUEST = "Text before every request";
    public static final String PUT_INTO_THE_SYSTEM_MESSAGE = "Put into the system message, before the lore. For instructions that hold for all of your mangas.";
    public static final String CONTEXT_PARTS_BEFORE = "Context parts before";
    public static final String CONTEXT_PARTS_AFTER = "Context parts after";
    public static final String DEFAULT_SCRIPT_PROMPT = "Default script prompt";
    public static final String EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT = "Empty or unchanged: the built-in prompt is used";
    public static final String DEFAULT_TAGS_PROMPT = "Default tags prompt";
    public static final String DEFAULT_ADDITIONAL_RULES = "Default additional rules";
    public static final String THE_STORY_HAS_NO_TEXT = "The story has no text in the parts of this section";
    public static final String GENERATE_OR_WRITE_THE_SCRIPT = "Generate or write the script of the section first";
    public static final String MANGA_GENERATOR_SETTINGS = "Manga Generator Settings";
    public static final String MANGA_GENERATOR_WRITE_A_SCRIPT = "Manga Generator: write a script";
    public static final String MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS = "Manga Generator: write the image tags";

    @Override
    protected void loadKeys() {
        ja(MANGA_GENERATOR, "マンガジェネレーター");
        zh(MANGA_GENERATOR, "漫画生成器");
        fr(MANGA_GENERATOR, "Générateur de manga");
        es(MANGA_GENERATOR, "Generador de manga");
        de(MANGA_GENERATOR, "Manga-Generator");

        ja(OPEN_A_BOOK_FIRST, "先に本を開いてください");
        zh(OPEN_A_BOOK_FIRST, "请先打开一本书");
        fr(OPEN_A_BOOK_FIRST, "Ouvrez d'abord un livre");
        es(OPEN_A_BOOK_FIRST, "Abre primero un libro");
        de(OPEN_A_BOOK_FIRST, "Öffne zuerst ein Buch");

        ja(NEW_MANGA, "新しいマンガ");
        zh(NEW_MANGA, "新建漫画");
        fr(NEW_MANGA, "Nouveau manga");
        es(NEW_MANGA, "Nuevo manga");
        de(NEW_MANGA, "Neuer Manga");

        ja(RENAME_THE_MANGA, "マンガの名前を変更");
        zh(RENAME_THE_MANGA, "重命名漫画");
        fr(RENAME_THE_MANGA, "Renommer le manga");
        es(RENAME_THE_MANGA, "Renombrar el manga");
        de(RENAME_THE_MANGA, "Den Manga umbenennen");

        ja(MOVE_THE_MANGA, "マンガを左へ移動");
        zh(MOVE_THE_MANGA, "将漫画左移");
        fr(MOVE_THE_MANGA, "Déplacer le manga vers la gauche");
        es(MOVE_THE_MANGA, "Mover el manga a la izquierda");
        de(MOVE_THE_MANGA, "Den Manga nach links verschieben");

        ja(MOVE_THE_MANGA_TO_THE_RIGHT, "マンガを右へ移動");
        zh(MOVE_THE_MANGA_TO_THE_RIGHT, "将漫画右移");
        fr(MOVE_THE_MANGA_TO_THE_RIGHT, "Déplacer le manga vers la droite");
        es(MOVE_THE_MANGA_TO_THE_RIGHT, "Mover el manga a la derecha");
        de(MOVE_THE_MANGA_TO_THE_RIGHT, "Den Manga nach rechts verschieben");

        ja(DELETE_THE_MANGA, "マンガを削除");
        zh(DELETE_THE_MANGA, "删除漫画");
        fr(DELETE_THE_MANGA, "Supprimer le manga");
        es(DELETE_THE_MANGA, "Eliminar el manga");
        de(DELETE_THE_MANGA, "Den Manga löschen");

        ja(TOKEN_BUDGET, "トークン予算");
        zh(TOKEN_BUDGET, "令牌预算");
        fr(TOKEN_BUDGET, "Budget de jetons");
        es(TOKEN_BUDGET, "Presupuesto de tokens");
        de(TOKEN_BUDGET, "Token-Budget");

        ja(HOW_LONG_A_SCRIPT, "モデルに求めるスクリプトの長さ");
        zh(HOW_LONG_A_SCRIPT, "要求模型生成的脚本长度");
        fr(HOW_LONG_A_SCRIPT, "La longueur du script demandé au modèle");
        es(HOW_LONG_A_SCRIPT, "Qué extensión de guion se le pide al modelo");
        de(HOW_LONG_A_SCRIPT, "Wie langes Skript vom Modell verlangt wird");

        ja(PARTS_PER_SECTION, "セクションあたりのパート数");
        zh(PARTS_PER_SECTION, "每节的段落数");
        fr(PARTS_PER_SECTION, "Parties par section");
        es(PARTS_PER_SECTION, "Partes por sección");
        de(PARTS_PER_SECTION, "Abschnitte je Sektion");

        ja(HOW_MANY_PARTS, "新しいセクションが脚色する物語のパート数");
        zh(HOW_MANY_PARTS, "新的一节改编的故事段落数");
        fr(HOW_MANY_PARTS, "Combien de parties de l'histoire une nouvelle section adapte");
        es(HOW_MANY_PARTS, "Cuántas partes de la historia adapta una sección nueva");
        de(HOW_MANY_PARTS, "Wie viele Abschnitte der Geschichte eine neue Sektion adaptiert");

        ja(CONTEXT_BEFORE, "前の文脈");
        zh(CONTEXT_BEFORE, "前文上下文");
        fr(CONTEXT_BEFORE, "Contexte avant");
        es(CONTEXT_BEFORE, "Contexto anterior");
        de(CONTEXT_BEFORE, "Kontext davor");

        ja(PARTS_BEFORE_THE_ADAPTED_ONES, "脚色するパートの前にある、新しいセクションが背景としてモデルに渡すパート");
        zh(PARTS_BEFORE_THE_ADAPTED_ONES, "新的一节作为背景交给模型的、位于改编段落之前的段落");
        fr(PARTS_BEFORE_THE_ADAPTED_ONES, "Parties avant celles adaptées qu'une nouvelle section donne au modèle comme arrière-plan");
        es(PARTS_BEFORE_THE_ADAPTED_ONES, "Partes anteriores a las adaptadas que una sección nueva da al modelo como trasfondo");
        de(PARTS_BEFORE_THE_ADAPTED_ONES, "Abschnitte vor den adaptierten, die eine neue Sektion dem Modell als Hintergrund gibt");

        ja(CONTEXT_AFTER, "後の文脈");
        zh(CONTEXT_AFTER, "后文上下文");
        fr(CONTEXT_AFTER, "Contexte après");
        es(CONTEXT_AFTER, "Contexto posterior");
        de(CONTEXT_AFTER, "Kontext danach");

        ja(PARTS_AFTER_THE_ADAPTED_ONES, "脚色するパートの後にある、新しいセクションが背景としてモデルに渡すパート");
        zh(PARTS_AFTER_THE_ADAPTED_ONES, "新的一节作为背景交给模型的、位于改编段落之后的段落");
        fr(PARTS_AFTER_THE_ADAPTED_ONES, "Parties après celles adaptées qu'une nouvelle section donne au modèle comme arrière-plan");
        es(PARTS_AFTER_THE_ADAPTED_ONES, "Partes posteriores a las adaptadas que una sección nueva da al modelo como trasfondo");
        de(PARTS_AFTER_THE_ADAPTED_ONES, "Abschnitte nach den adaptierten, die eine neue Sektion dem Modell als Hintergrund gibt");

        ja(SCRIPT_PROMPT, "スクリプトのプロンプト");
        zh(SCRIPT_PROMPT, "脚本提示词");
        fr(SCRIPT_PROMPT, "Prompt du script");
        es(SCRIPT_PROMPT, "Prompt del guion");
        de(SCRIPT_PROMPT, "Skriptprompt");

        ja(TAGS_PROMPT, "タグのプロンプト");
        zh(TAGS_PROMPT, "标签提示词");
        fr(TAGS_PROMPT, "Prompt des tags");
        es(TAGS_PROMPT, "Prompt de las etiquetas");
        de(TAGS_PROMPT, "Tags-Prompt");

        ja(ADDITIONAL_RULES, "追加ルール");
        zh(ADDITIONAL_RULES, "附加规则");
        fr(ADDITIONAL_RULES, "Règles supplémentaires");
        es(ADDITIONAL_RULES, "Reglas adicionales");
        de(ADDITIONAL_RULES, "Zusätzliche Regeln");

        ja(PUT_IN_PLACE_OF_ADDITIONALRULES, "スクリプトのプロンプトの {{additionalRules}} の代わりに入ります");
        zh(PUT_IN_PLACE_OF_ADDITIONALRULES, "替换脚本提示词中的 {{additionalRules}}");
        fr(PUT_IN_PLACE_OF_ADDITIONALRULES, "Mis à la place de {{additionalRules}} dans le prompt du script");
        es(PUT_IN_PLACE_OF_ADDITIONALRULES, "Se pone en lugar de {{additionalRules}} en el prompt del guion");
        de(PUT_IN_PLACE_OF_ADDITIONALRULES, "Wird im Skriptprompt anstelle von {{additionalRules}} eingesetzt");

        ja(COPY_ALL, "すべてコピー");
        zh(COPY_ALL, "全部复制");
        fr(COPY_ALL, "Tout copier");
        es(COPY_ALL, "Copiar todo");
        de(COPY_ALL, "Alles kopieren");

        ja(ADD_SECTION, "セクションを追加");
        zh(ADD_SECTION, "添加一节");
        fr(ADD_SECTION, "Ajouter une section");
        es(ADD_SECTION, "Añadir sección");
        de(ADD_SECTION, "Sektion hinzufügen");

        ja(CONTINUOUS_MODE, "連続モード");
        zh(CONTINUOUS_MODE, "连续模式");
        fr(CONTINUOUS_MODE, "Mode continu");
        es(CONTINUOUS_MODE, "Modo continuo");
        de(CONTINUOUS_MODE, "Dauermodus");

        ja(STOP_CONTINUOUS_MODE, "連続モードを停止");
        zh(STOP_CONTINUOUS_MODE, "停止连续模式");
        fr(STOP_CONTINUOUS_MODE, "Arrêter le mode continu");
        es(STOP_CONTINUOUS_MODE, "Detener el modo continuo");
        de(STOP_CONTINUOUS_MODE, "Dauermodus beenden");

        ja(SCRIPTS_ONLY, "スクリプトのみ");
        zh(SCRIPTS_ONLY, "仅脚本");
        fr(SCRIPTS_ONLY, "Scripts seulement");
        es(SCRIPTS_ONLY, "Solo guiones");
        de(SCRIPTS_ONLY, "Nur Skripte");

        ja(TAGS_ONLY, "タグのみ");
        zh(TAGS_ONLY, "仅标签");
        fr(TAGS_ONLY, "Tags seulement");
        es(TAGS_ONLY, "Solo etiquetas");
        de(TAGS_ONLY, "Nur Tags");

        ja(WHAT_THE_CONTINUOUS_MODE_GENERATES, "連続モードが生成するもの：一度に一種類ずつ行うので、モデルは同じプロンプトを使い続けます");
        zh(WHAT_THE_CONTINUOUS_MODE_GENERATES, "连续模式生成的内容：每次只生成一种，因此模型始终使用同一个提示词");
        fr(WHAT_THE_CONTINUOUS_MODE_GENERATES, "Ce que génère le mode continu : il fait un seul genre à la fois, pour que le modèle garde le même prompt");
        es(WHAT_THE_CONTINUOUS_MODE_GENERATES, "Lo que genera el modo continuo: hace un tipo cada vez, para que el modelo conserve el mismo prompt");
        de(WHAT_THE_CONTINUOUS_MODE_GENERATES, "Was der Dauermodus generiert: Er erledigt jeweils eine Art, damit das Modell denselben Prompt behält");

        ja(GENERATE_THE_TAGS, "スクリプトがありタグのないセクションのタグを、なくなるか停止するまで生成します");
        zh(GENERATE_THE_TAGS, "为有脚本但没有标签的各节生成标签，直到没有为止或你停止");
        fr(GENERATE_THE_TAGS, "Générer les tags des sections qui ont un script mais pas de tags, jusqu'à ce qu'il n'y en ait plus ou que vous l'arrêtiez");
        es(GENERATE_THE_TAGS, "Genera las etiquetas de las secciones que tienen guion pero no etiquetas, hasta que no quede ninguna o lo detengas");
        de(GENERATE_THE_TAGS, "Die Tags der Sektionen generieren, die ein Skript, aber keine Tags haben, bis es keine mehr gibt oder du ihn stoppst");

        ja(GENERATE_THE_SCRIPT, "スクリプトのないセクションのスクリプトを生成し、物語の次のパートのセクションを追加してそのスクリプトを生成し続けます。物語のパートがなくなるか、停止するまで続きます");
        zh(GENERATE_THE_SCRIPT, "为没有脚本的各节生成脚本，并持续为故事的后续段落添加新的一节并生成脚本，直到故事用完或你停止为止");
        fr(GENERATE_THE_SCRIPT, "Générer le script des sections qui n'en ont pas, et continuer à ajouter des sections pour les parties suivantes de l'histoire et à générer leurs scripts, jusqu'à la fin de l'histoire ou jusqu'à ce que vous l'arrêtiez");
        es(GENERATE_THE_SCRIPT, "Genera el guion de las secciones que no lo tienen, y sigue añadiendo secciones para las partes siguientes de la historia y generando sus guiones, hasta que se acabe la historia o lo detengas");
        de(GENERATE_THE_SCRIPT, "Das Skript der Sektionen generieren, die keines haben, und weiter Sektionen für die nächsten Abschnitte der Geschichte hinzufügen und ihre Skripte generieren, bis die Geschichte zu Ende ist oder du ihn stoppst");

        ja(CONTINUOUS_MODE_IS_FINISHED, "連続モードが完了しました：すべてのスクリプトにタグがあります");
        zh(CONTINUOUS_MODE_IS_FINISHED, "连续模式已完成：所有脚本都已有标签");
        fr(CONTINUOUS_MODE_IS_FINISHED, "Le mode continu est terminé : chaque script a ses tags");
        es(CONTINUOUS_MODE_IS_FINISHED, "El modo continuo ha terminado: todos los guiones tienen sus etiquetas");
        de(CONTINUOUS_MODE_IS_FINISHED, "Der Dauermodus ist beendet: Jedes Skript hat seine Tags");

        ja(CONTINUOUS_MODE_IS_FINISHED_THERE, "連続モードが完了しました：物語のパートがもうありません");
        zh(CONTINUOUS_MODE_IS_FINISHED_THERE, "连续模式已完成：故事中没有更多段落了");
        fr(CONTINUOUS_MODE_IS_FINISHED_THERE, "Le mode continu est terminé : il n'y a plus de parties de l'histoire");
        es(CONTINUOUS_MODE_IS_FINISHED_THERE, "El modo continuo ha terminado: no quedan más partes de la historia");
        de(CONTINUOUS_MODE_IS_FINISHED_THERE, "Der Dauermodus ist beendet: Es gibt keine weiteren Abschnitte der Geschichte");

        ja(CONTINUOUS_MODE_STOPPED, "連続モードを停止しました");
        zh(CONTINUOUS_MODE_STOPPED, "连续模式已停止");
        fr(CONTINUOUS_MODE_STOPPED, "Mode continu arrêté");
        es(CONTINUOUS_MODE_STOPPED, "Modo continuo detenido");
        de(CONTINUOUS_MODE_STOPPED, "Dauermodus gestoppt");

        ja(CONTINUOUS_MODE_STOPPED_THE_MODEL, "連続モードを停止しました：モデルが何も書きませんでした");
        zh(CONTINUOUS_MODE_STOPPED_THE_MODEL, "连续模式已停止：模型没有写出任何内容");
        fr(CONTINUOUS_MODE_STOPPED_THE_MODEL, "Mode continu arrêté : le modèle n'a rien écrit");
        es(CONTINUOUS_MODE_STOPPED_THE_MODEL, "Modo continuo detenido: el modelo no escribió nada");
        de(CONTINUOUS_MODE_STOPPED_THE_MODEL, "Dauermodus gestoppt: Das Modell hat nichts geschrieben");

        ja(THERE_IS_NO_MANGA, "この本にはまだマンガがありません。プラスで作成してください。");
        zh(THERE_IS_NO_MANGA, "这本书还没有漫画。请用加号创建一个。");
        fr(THERE_IS_NO_MANGA, "Il n'y a pas encore de manga dans ce livre. Créez-en un avec le plus.");
        es(THERE_IS_NO_MANGA, "Todavía no hay ningún manga en este libro. Crea uno con el signo más.");
        de(THERE_IS_NO_MANGA, "In diesem Buch gibt es noch keinen Manga. Erstelle einen mit dem Plus.");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");

        ja(NO_SECTIONS_YET_ADD_ONE, "セクションはまだありません。追加すると物語の最初のパートから始まります。");
        zh(NO_SECTIONS_YET_ADD_ONE, "还没有任何一节。添加一节，从故事的最初几段开始。");
        fr(NO_SECTIONS_YET_ADD_ONE, "Pas encore de sections. Ajoutez-en une pour commencer par les premières parties de l'histoire.");
        es(NO_SECTIONS_YET_ADD_ONE, "Todavía no hay secciones. Añade una para empezar con las primeras partes de la historia.");
        de(NO_SECTIONS_YET_ADD_ONE, "Noch keine Sektionen. Füge eine hinzu, um mit den ersten Abschnitten der Geschichte zu beginnen.");

        ja(MANGA_SCRIPT, "マンガスクリプト %d");
        zh(MANGA_SCRIPT, "漫画脚本 %d");
        fr(MANGA_SCRIPT, "Script de manga %d");
        es(MANGA_SCRIPT, "Guion de manga %d");
        de(MANGA_SCRIPT, "Manga-Skript %d");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");
        fr(TITLE, "Titre");
        es(TITLE, "Título");
        de(TITLE, "Titel");

        ja(SAVE, "保存");
        zh(SAVE, "保存");
        fr(SAVE, "Enregistrer");
        es(SAVE, "Guardar");
        de(SAVE, "Speichern");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");

        ja(DELETE_THE_MANGA_THIS_CANNOT, "マンガ「%s」を削除しますか？ 元に戻せません。");
        zh(DELETE_THE_MANGA_THIS_CANNOT, "删除漫画“%s”？此操作无法撤销。");
        fr(DELETE_THE_MANGA_THIS_CANNOT, "Supprimer le manga « %s » ? C'est irréversible.");
        es(DELETE_THE_MANGA_THIS_CANNOT, "¿Eliminar el manga \"%s\"? No se puede deshacer.");
        de(DELETE_THE_MANGA_THIS_CANNOT, "Den Manga „%s“ löschen? Das kann nicht rückgängig gemacht werden.");

        ja(EMPTY_OR_UNCHANGED_THE_BUILT, "空または未変更の場合: 組み込みのテキストを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT, "留空或未修改：使用内置文本");
        fr(EMPTY_OR_UNCHANGED_THE_BUILT, "Vide ou inchangé : le texte intégré est utilisé");
        es(EMPTY_OR_UNCHANGED_THE_BUILT, "Vacío o sin cambios: se usa el texto integrado");
        de(EMPTY_OR_UNCHANGED_THE_BUILT, "Leer oder unverändert: Der eingebaute Text wird verwendet");

        ja(THE_STORY_HAS_NO_PARTS, "物語にはまだパートがありません");
        zh(THE_STORY_HAS_NO_PARTS, "故事还没有任何段落");
        fr(THE_STORY_HAS_NO_PARTS, "L'histoire n'a pas encore de parties");
        es(THE_STORY_HAS_NO_PARTS, "La historia todavía no tiene partes");
        de(THE_STORY_HAS_NO_PARTS, "Die Geschichte hat noch keine Abschnitte");

        ja(NO_NEW_PARTS, "別のセクションにできる新しい物語のパートがありません（物語は %d パート）");
        zh(NO_NEW_PARTS, "没有可再建一节的新故事段落（故事共有 %d 段）");
        fr(NO_NEW_PARTS, "Pas de nouvelles parties de l'histoire pour une autre section (l'histoire a %d parties)");
        es(NO_NEW_PARTS, "No hay partes nuevas de la historia para otra sección (la historia tiene %d partes)");
        de(NO_NEW_PARTS, "Keine neuen Abschnitte der Geschichte für eine weitere Sektion (die Geschichte hat %d Abschnitte)");

        ja(THERE_IS_NO_SCRIPT, "コピーするスクリプトもタグもありません");
        zh(THERE_IS_NO_SCRIPT, "没有可复制的脚本或标签");
        fr(THERE_IS_NO_SCRIPT, "Il n'y a ni script ni tags à copier");
        es(THERE_IS_NO_SCRIPT, "No hay guion ni etiquetas que copiar");
        de(THERE_IS_NO_SCRIPT, "Es gibt kein Skript und keine Tags zum Kopieren");

        ja(ALL_SCRIPTS_AND_TAGS_COPIED, "すべてのスクリプトとタグをコピーしました");
        zh(ALL_SCRIPTS_AND_TAGS_COPIED, "已复制全部脚本和标签");
        fr(ALL_SCRIPTS_AND_TAGS_COPIED, "Tous les scripts et tags copiés");
        es(ALL_SCRIPTS_AND_TAGS_COPIED, "Todos los guiones y etiquetas copiados");
        de(ALL_SCRIPTS_AND_TAGS_COPIED, "Alle Skripte und Tags kopiert");

        ja(SECTION, "セクション %d");
        zh(SECTION, "第 %d 节");
        fr(SECTION, "Section %d");
        es(SECTION, "Sección %d");
        de(SECTION, "Sektion %d");

        ja(SCRIPT, "スクリプト");
        zh(SCRIPT, "脚本");
        fr(SCRIPT, "Script");
        es(SCRIPT, "Guion");
        de(SCRIPT, "Skript");

        ja(TAGS, "タグ");
        zh(TAGS, "标签");
        fr(TAGS, "Tags");
        es(TAGS, "Etiquetas");
        de(TAGS, "Tags");

        ja(OVERRIDE, "上書き");
        zh(OVERRIDE, "覆盖");
        fr(OVERRIDE, "Remplacer");
        es(OVERRIDE, "Sustituir");
        de(OVERRIDE, "Überschreiben");

        ja(SCRIPT_PROMPT_OF_THIS_SECTION, "このセクションのスクリプトのプロンプト");
        zh(SCRIPT_PROMPT_OF_THIS_SECTION, "本节的脚本提示词");
        fr(SCRIPT_PROMPT_OF_THIS_SECTION, "Prompt du script de cette section");
        es(SCRIPT_PROMPT_OF_THIS_SECTION, "Prompt del guion de esta sección");
        de(SCRIPT_PROMPT_OF_THIS_SECTION, "Skriptprompt dieser Sektion");

        ja(TAGS_PROMPT_OF_THIS_SECTION, "このセクションのタグのプロンプト");
        zh(TAGS_PROMPT_OF_THIS_SECTION, "本节的标签提示词");
        fr(TAGS_PROMPT_OF_THIS_SECTION, "Prompt des tags de cette section");
        es(TAGS_PROMPT_OF_THIS_SECTION, "Prompt de las etiquetas de esta sección");
        de(TAGS_PROMPT_OF_THIS_SECTION, "Tags-Prompt dieser Sektion");

        ja(ADDITIONAL_RULES_OF_THIS_SECTION, "このセクションの追加ルール");
        zh(ADDITIONAL_RULES_OF_THIS_SECTION, "本节的附加规则");
        fr(ADDITIONAL_RULES_OF_THIS_SECTION, "Règles supplémentaires de cette section");
        es(ADDITIONAL_RULES_OF_THIS_SECTION, "Reglas adicionales de esta sección");
        de(ADDITIONAL_RULES_OF_THIS_SECTION, "Zusätzliche Regeln dieser Sektion");

        ja(RULES_ADDENDUM, "ルールの追記");
        zh(RULES_ADDENDUM, "规则补充");
        fr(RULES_ADDENDUM, "Complément aux règles");
        es(RULES_ADDENDUM, "Complemento de las reglas");
        de(RULES_ADDENDUM, "Regelergänzung");

        ja(RULES_ADDED_TO_THE_RULES, "マンガのルールに加えるルール");
        zh(RULES_ADDED_TO_THE_RULES, "加到漫画规则之后的规则");
        fr(RULES_ADDED_TO_THE_RULES, "Règles ajoutées aux règles du manga");
        es(RULES_ADDED_TO_THE_RULES, "Reglas que se añaden a las reglas del manga");
        de(RULES_ADDED_TO_THE_RULES, "Regeln, die zu den Regeln des Mangas hinzugefügt werden");

        ja(ADAPT_FROM_PART, "脚色の開始パート");
        zh(ADAPT_FROM_PART, "改编起始段");
        fr(ADAPT_FROM_PART, "Adapter depuis la partie");
        es(ADAPT_FROM_PART, "Adaptar desde la parte");
        de(ADAPT_FROM_PART, "Adaptieren ab Abschnitt");

        ja(TO_PART, "終了パート");
        zh(TO_PART, "至段");
        fr(TO_PART, "jusqu'à la partie");
        es(TO_PART, "hasta la parte");
        de(TO_PART, "bis Abschnitt");

        ja(CONTEXT_FROM_PART, "文脈の開始パート");
        zh(CONTEXT_FROM_PART, "上下文起始段");
        fr(CONTEXT_FROM_PART, "Contexte depuis la partie");
        es(CONTEXT_FROM_PART, "Contexto desde la parte");
        de(CONTEXT_FROM_PART, "Kontext ab Abschnitt");

        ja(EDIT_THE_TEXT, "テキストを編集");
        zh(EDIT_THE_TEXT, "编辑文本");
        fr(EDIT_THE_TEXT, "Modifier le texte");
        es(EDIT_THE_TEXT, "Editar el texto");
        de(EDIT_THE_TEXT, "Den Text bearbeiten");

        ja(COPY, "コピー");
        zh(COPY, "复制");
        fr(COPY, "Copier");
        es(COPY, "Copiar");
        de(COPY, "Kopieren");

        ja(SHOW_THE_REQUEST_THE_MODEL, "モデルに送るリクエストを表示");
        zh(SHOW_THE_REQUEST_THE_MODEL, "显示发给模型的请求");
        fr(SHOW_THE_REQUEST_THE_MODEL, "Afficher la requête que reçoit le modèle");
        es(SHOW_THE_REQUEST_THE_MODEL, "Mostrar la solicitud que recibe el modelo");
        de(SHOW_THE_REQUEST_THE_MODEL, "Die Anfrage anzeigen, die das Modell erhält");

        ja(DELETE_THIS_SECTION, "このセクションを削除しますか？");
        zh(DELETE_THIS_SECTION, "删除这一节？");
        fr(DELETE_THIS_SECTION, "Supprimer cette section ?");
        es(DELETE_THIS_SECTION, "¿Eliminar esta sección?");
        de(DELETE_THIS_SECTION, "Diese Sektion löschen?");

        ja(DELETE, "削除");
        zh(DELETE, "删除");
        fr(DELETE, "Supprimer");
        es(DELETE, "Eliminar");
        de(DELETE, "Löschen");

        ja(THOUGHT, "%s 秒間思考しました");
        zh(THOUGHT, "思考了 %s 秒");
        fr(THOUGHT, "A réfléchi %s s");
        es(THOUGHT, "Pensó durante %s s");
        de(THOUGHT, "%s s nachgedacht");

        ja(THINKING, "%s 秒間思考中");
        zh(THINKING, "已思考 %s 秒");
        fr(THINKING, "Réfléchit depuis %s s");
        es(THINKING, "Pensando durante %s s");
        de(THINKING, "Denkt seit %s s nach");

        ja(WAITING_FOR_THE_MODEL, "モデルを待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");
        fr(WAITING_FOR_THE_MODEL, "En attente du modèle...");
        es(WAITING_FOR_THE_MODEL, "Esperando al modelo...");
        de(WAITING_FOR_THE_MODEL, "Warte auf das Modell ...");

        ja(NO_TAGS_YET_GENERATE_THEM, "タグはまだありません。スクリプトから生成するか、自分で書いてください。");
        zh(NO_TAGS_YET_GENERATE_THEM, "还没有标签。可根据脚本生成，或自己编写。");
        fr(NO_TAGS_YET_GENERATE_THEM, "Pas encore de tags. Générez-les à partir du script, ou écrivez-les.");
        es(NO_TAGS_YET_GENERATE_THEM, "Todavía no hay etiquetas. Genéralas a partir del guion, o escríbelas.");
        de(NO_TAGS_YET_GENERATE_THEM, "Noch keine Tags. Generiere sie aus dem Skript oder schreibe sie.");

        ja(NO_SCRIPT_YET_GENERATE, "スクリプトはまだありません。生成するか、自分で書いてください。");
        zh(NO_SCRIPT_YET_GENERATE, "还没有脚本。可生成，或自己编写。");
        fr(NO_SCRIPT_YET_GENERATE, "Pas encore de script. Générez-le, ou écrivez-le.");
        es(NO_SCRIPT_YET_GENERATE, "Todavía no hay guion. Genéralo, o escríbelo.");
        de(NO_SCRIPT_YET_GENERATE, "Noch kein Skript. Generiere es oder schreibe es.");

        ja(ABORT, "中止");
        zh(ABORT, "中止");
        fr(ABORT, "Interrompre");
        es(ABORT, "Abortar");
        de(ABORT, "Abbrechen");

        ja(GENERATE_TAGS, "タグを生成");
        zh(GENERATE_TAGS, "生成标签");
        fr(GENERATE_TAGS, "Générer les tags");
        es(GENERATE_TAGS, "Generar etiquetas");
        de(GENERATE_TAGS, "Tags generieren");

        ja(GENERATE_SCRIPT, "スクリプトを生成");
        zh(GENERATE_SCRIPT, "生成脚本");
        fr(GENERATE_SCRIPT, "Générer le script");
        es(GENERATE_SCRIPT, "Generar guion");
        de(GENERATE_SCRIPT, "Skript generieren");

        ja(TAGS_COPIED, "タグをコピーしました");
        zh(TAGS_COPIED, "标签已复制");
        fr(TAGS_COPIED, "Tags copiés");
        es(TAGS_COPIED, "Etiquetas copiadas");
        de(TAGS_COPIED, "Tags kopiert");

        ja(SCRIPT_COPIED, "スクリプトをコピーしました");
        zh(SCRIPT_COPIED, "脚本已复制");
        fr(SCRIPT_COPIED, "Script copié");
        es(SCRIPT_COPIED, "Guion copiado");
        de(SCRIPT_COPIED, "Skript kopiert");

        ja(EMPTY_OR_UNCHANGED_THE_SETTING, "空または未変更の場合: マンガの設定を使います");
        zh(EMPTY_OR_UNCHANGED_THE_SETTING, "留空或未修改：使用漫画的设置");
        fr(EMPTY_OR_UNCHANGED_THE_SETTING, "Vide ou inchangé : le réglage du manga est utilisé");
        es(EMPTY_OR_UNCHANGED_THE_SETTING, "Vacío o sin cambios: se usa el ajuste del manga");
        de(EMPTY_OR_UNCHANGED_THE_SETTING, "Leer oder unverändert: Die Einstellung des Mangas wird verwendet");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");
        fr(REQUEST, "Requête");
        es(REQUEST, "Solicitud");
        de(REQUEST, "Anfrage");

        ja(ABOUT_TOKENS, "約 %d トークン");
        zh(ABOUT_TOKENS, "约 %d 个令牌");
        fr(ABOUT_TOKENS, "Environ %d jetons");
        es(ABOUT_TOKENS, "Unos %d tokens");
        de(ABOUT_TOKENS, "Etwa %d Tokens");

        ja(OF_THE_MODEL_HAS_ROOM, "（モデルの余裕: %d、返答分を除いたコンテキスト）");
        zh(OF_THE_MODEL_HAS_ROOM, "（模型可容纳 %d，即去掉回复后的上下文）");
        fr(OF_THE_MODEL_HAS_ROOM, "sur les %d que le modèle peut accueillir (son contexte sans la réponse)");
        es(OF_THE_MODEL_HAS_ROOM, "de los %d que caben en el modelo (su contexto sin la respuesta)");
        de(OF_THE_MODEL_HAS_ROOM, "von %d, für die das Modell Platz hat (sein Kontext ohne die Antwort)");

        ja(LORE_ENTRIES, "ロアエントリ: %d");
        zh(LORE_ENTRIES, "设定条目：%d");
        fr(LORE_ENTRIES, "Entrées de lore : %d");
        es(LORE_ENTRIES, "Entradas de lore: %d");
        de(LORE_ENTRIES, "Lore-Einträge: %d");

        ja(MORE_DID_NOT_FIT, "（さらに %d 件は収まりませんでした）");
        zh(MORE_DID_NOT_FIT, "（另有 %d 条放不下）");
        fr(MORE_DID_NOT_FIT, "(%d de plus ne tenaient pas)");
        es(MORE_DID_NOT_FIT, "(%d más no cabían)");
        de(MORE_DID_NOT_FIT, "(%d weitere passten nicht)");

        ja(THE_REQUEST_ABOUT_TOKENS, "リクエスト（約 %d トークン）がモデルの余裕（%d）を超えています");
        zh(THE_REQUEST_ABOUT_TOKENS, "请求（约 %d 个令牌）超出了模型可容纳的范围（%d）");
        fr(THE_REQUEST_ABOUT_TOKENS, "La requête (environ %d jetons) dépasse ce que le modèle peut accueillir (%d)");
        es(THE_REQUEST_ABOUT_TOKENS, "La solicitud (unos %d tokens) supera lo que cabe en el modelo (%d)");
        de(THE_REQUEST_ABOUT_TOKENS, "Die Anfrage (etwa %d Tokens) ist größer, als das Modell Platz hat (%d)");

        ja(LORE_ENTRIES_WERE_LEFT_OUT, "コンテキストに収めるため、ロアエントリ %d 件を除きました");
        zh(LORE_ENTRIES_WERE_LEFT_OUT, "为适应上下文，已省略 %d 条设定条目");
        fr(LORE_ENTRIES_WERE_LEFT_OUT, "%d entrées de lore ont été omises pour tenir dans le contexte");
        es(LORE_ENTRIES_WERE_LEFT_OUT, "Se omitieron %d entradas de lore para ajustarse al contexto");
        de(LORE_ENTRIES_WERE_LEFT_OUT, "%d Lore-Einträge wurden weggelassen, damit es in den Kontext passt");

        ja(RESET_TO_DEFAULT, "デフォルトに戻す");
        zh(RESET_TO_DEFAULT, "恢复默认");
        fr(RESET_TO_DEFAULT, "Rétablir la valeur par défaut");
        es(RESET_TO_DEFAULT, "Restablecer el valor predeterminado");
        de(RESET_TO_DEFAULT, "Auf Standard zurücksetzen");

        ja(A_NEW_MANGA_STARTS, "新しいマンガはこの設定で始まります。作成済みのマンガは自分の設定のままです。");
        zh(A_NEW_MANGA_STARTS, "新漫画以这些设置开始。已创建的漫画保留自己的设置。");
        fr(A_NEW_MANGA_STARTS, "Un nouveau manga commence avec ces paramètres. Un manga déjà créé garde les siens.");
        es(A_NEW_MANGA_STARTS, "Un manga nuevo empieza con estos ajustes. Un manga que ya hayas creado conserva los suyos.");
        de(A_NEW_MANGA_STARTS, "Ein neuer Manga beginnt mit diesen Einstellungen. Ein bereits erstellter Manga behält seine eigenen.");

        ja(TEXT_BEFORE_EVERY_REQUEST, "すべてのリクエストの前に置くテキスト");
        zh(TEXT_BEFORE_EVERY_REQUEST, "每个请求之前的文本");
        fr(TEXT_BEFORE_EVERY_REQUEST, "Texte avant chaque requête");
        es(TEXT_BEFORE_EVERY_REQUEST, "Texto antes de cada solicitud");
        de(TEXT_BEFORE_EVERY_REQUEST, "Text vor jeder Anfrage");

        ja(PUT_INTO_THE_SYSTEM_MESSAGE, "システムメッセージの、ロアの前に入ります。すべてのマンガに共通する指示用です。");
        zh(PUT_INTO_THE_SYSTEM_MESSAGE, "放入系统消息中、设定之前。用于适用于你所有漫画的指令。");
        fr(PUT_INTO_THE_SYSTEM_MESSAGE, "Mis dans le message système, avant le lore. Pour des instructions valables pour tous vos mangas.");
        es(PUT_INTO_THE_SYSTEM_MESSAGE, "Se pone en el mensaje del sistema, antes del lore. Para instrucciones que valen para todos tus mangas.");
        de(PUT_INTO_THE_SYSTEM_MESSAGE, "Wird in die Systemnachricht vor der Lore gesetzt. Für Anweisungen, die für alle deine Mangas gelten.");

        ja(CONTEXT_PARTS_BEFORE, "前の文脈のパート数");
        zh(CONTEXT_PARTS_BEFORE, "前文上下文段数");
        fr(CONTEXT_PARTS_BEFORE, "Parties de contexte avant");
        es(CONTEXT_PARTS_BEFORE, "Partes de contexto anteriores");
        de(CONTEXT_PARTS_BEFORE, "Kontextabschnitte davor");

        ja(CONTEXT_PARTS_AFTER, "後の文脈のパート数");
        zh(CONTEXT_PARTS_AFTER, "后文上下文段数");
        fr(CONTEXT_PARTS_AFTER, "Parties de contexte après");
        es(CONTEXT_PARTS_AFTER, "Partes de contexto posteriores");
        de(CONTEXT_PARTS_AFTER, "Kontextabschnitte danach");

        ja(DEFAULT_SCRIPT_PROMPT, "デフォルトのスクリプトのプロンプト");
        zh(DEFAULT_SCRIPT_PROMPT, "默认脚本提示词");
        fr(DEFAULT_SCRIPT_PROMPT, "Prompt du script par défaut");
        es(DEFAULT_SCRIPT_PROMPT, "Prompt del guion predeterminado");
        de(DEFAULT_SCRIPT_PROMPT, "Standard-Skriptprompt");

        ja(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "空または未変更の場合: 組み込みのプロンプトを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "留空或未修改：使用内置提示词");
        fr(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "Vide ou inchangé : le prompt intégré est utilisé");
        es(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "Vacío o sin cambios: se usa el prompt integrado");
        de(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "Leer oder unverändert: Der eingebaute Prompt wird verwendet");

        ja(DEFAULT_TAGS_PROMPT, "デフォルトのタグのプロンプト");
        zh(DEFAULT_TAGS_PROMPT, "默认标签提示词");
        fr(DEFAULT_TAGS_PROMPT, "Prompt des tags par défaut");
        es(DEFAULT_TAGS_PROMPT, "Prompt de las etiquetas predeterminado");
        de(DEFAULT_TAGS_PROMPT, "Standard-Tags-Prompt");

        ja(DEFAULT_ADDITIONAL_RULES, "デフォルトの追加ルール");
        zh(DEFAULT_ADDITIONAL_RULES, "默认附加规则");
        fr(DEFAULT_ADDITIONAL_RULES, "Règles supplémentaires par défaut");
        es(DEFAULT_ADDITIONAL_RULES, "Reglas adicionales predeterminadas");
        de(DEFAULT_ADDITIONAL_RULES, "Standardmäßige zusätzliche Regeln");

        ja(THE_STORY_HAS_NO_TEXT, "このセクションのパートに物語のテキストがありません");
        zh(THE_STORY_HAS_NO_TEXT, "本节的段落中没有故事文本");
        fr(THE_STORY_HAS_NO_TEXT, "L'histoire n'a pas de texte dans les parties de cette section");
        es(THE_STORY_HAS_NO_TEXT, "La historia no tiene texto en las partes de esta sección");
        de(THE_STORY_HAS_NO_TEXT, "Die Geschichte hat in den Abschnitten dieser Sektion keinen Text");

        ja(GENERATE_OR_WRITE_THE_SCRIPT, "先にこのセクションのスクリプトを生成するか書いてください");
        zh(GENERATE_OR_WRITE_THE_SCRIPT, "请先生成或编写本节的脚本");
        fr(GENERATE_OR_WRITE_THE_SCRIPT, "Générez ou écrivez d'abord le script de la section");
        es(GENERATE_OR_WRITE_THE_SCRIPT, "Genera o escribe primero el guion de la sección");
        de(GENERATE_OR_WRITE_THE_SCRIPT, "Generiere oder schreibe zuerst das Skript der Sektion");

        ja(MANGA_GENERATOR_SETTINGS, "マンガジェネレーター設定");
        zh(MANGA_GENERATOR_SETTINGS, "漫画生成器设置");
        fr(MANGA_GENERATOR_SETTINGS, "Paramètres du générateur de manga");
        es(MANGA_GENERATOR_SETTINGS, "Ajustes del generador de manga");
        de(MANGA_GENERATOR_SETTINGS, "Einstellungen des Manga-Generators");

        ja(MANGA_GENERATOR_WRITE_A_SCRIPT, "マンガジェネレーター: スクリプトを作成");
        zh(MANGA_GENERATOR_WRITE_A_SCRIPT, "漫画生成器：撰写脚本");
        fr(MANGA_GENERATOR_WRITE_A_SCRIPT, "Générateur de manga : écrire un script");
        es(MANGA_GENERATOR_WRITE_A_SCRIPT, "Generador de manga: escribir un guion");
        de(MANGA_GENERATOR_WRITE_A_SCRIPT, "Manga-Generator: ein Skript schreiben");

        ja(MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS, "マンガジェネレーター: 画像タグを作成");
        zh(MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS, "漫画生成器：撰写图像标签");
        fr(MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS, "Générateur de manga : écrire les tags d'image");
        es(MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS, "Generador de manga: escribir las etiquetas de imagen");
        de(MANGA_GENERATOR_WRITE_THE_IMAGE_TAGS, "Manga-Generator: die Bild-Tags schreiben");
    }
}
