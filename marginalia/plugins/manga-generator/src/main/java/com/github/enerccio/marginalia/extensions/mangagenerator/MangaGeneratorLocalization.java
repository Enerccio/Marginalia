package com.github.enerccio.marginalia.extensions.mangagenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Manga Generator in the languages besides English; the English text is the key.
 */
public class MangaGeneratorLocalization extends PluginLocalizationBundle {

    public static final MangaGeneratorLocalization T = new MangaGeneratorLocalization();

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

    @Override
    protected void loadKeys() {
        ja(MANGA_GENERATOR, "マンガジェネレーター");
        zh(MANGA_GENERATOR, "漫画生成器");

        ja(OPEN_A_BOOK_FIRST, "先に本を開いてください");
        zh(OPEN_A_BOOK_FIRST, "请先打开一本书");

        ja(NEW_MANGA, "新しいマンガ");
        zh(NEW_MANGA, "新建漫画");

        ja(RENAME_THE_MANGA, "マンガの名前を変更");
        zh(RENAME_THE_MANGA, "重命名漫画");

        ja(MOVE_THE_MANGA, "マンガを左へ移動");
        zh(MOVE_THE_MANGA, "将漫画左移");

        ja(MOVE_THE_MANGA_TO_THE_RIGHT, "マンガを右へ移動");
        zh(MOVE_THE_MANGA_TO_THE_RIGHT, "将漫画右移");

        ja(DELETE_THE_MANGA, "マンガを削除");
        zh(DELETE_THE_MANGA, "删除漫画");

        ja(TOKEN_BUDGET, "トークン予算");
        zh(TOKEN_BUDGET, "令牌预算");

        ja(HOW_LONG_A_SCRIPT, "モデルに求めるスクリプトの長さ");
        zh(HOW_LONG_A_SCRIPT, "要求模型生成的脚本长度");

        ja(PARTS_PER_SECTION, "セクションあたりのパート数");
        zh(PARTS_PER_SECTION, "每节的段落数");

        ja(HOW_MANY_PARTS, "新しいセクションが脚色する物語のパート数");
        zh(HOW_MANY_PARTS, "新的一节改编的故事段落数");

        ja(CONTEXT_BEFORE, "前の文脈");
        zh(CONTEXT_BEFORE, "前文上下文");

        ja(PARTS_BEFORE_THE_ADAPTED_ONES, "脚色するパートの前にある、新しいセクションが背景としてモデルに渡すパート");
        zh(PARTS_BEFORE_THE_ADAPTED_ONES, "新的一节作为背景交给模型的、位于改编段落之前的段落");

        ja(CONTEXT_AFTER, "後の文脈");
        zh(CONTEXT_AFTER, "后文上下文");

        ja(PARTS_AFTER_THE_ADAPTED_ONES, "脚色するパートの後にある、新しいセクションが背景としてモデルに渡すパート");
        zh(PARTS_AFTER_THE_ADAPTED_ONES, "新的一节作为背景交给模型的、位于改编段落之后的段落");

        ja(SCRIPT_PROMPT, "スクリプトのプロンプト");
        zh(SCRIPT_PROMPT, "脚本提示词");

        ja(TAGS_PROMPT, "タグのプロンプト");
        zh(TAGS_PROMPT, "标签提示词");

        ja(ADDITIONAL_RULES, "追加ルール");
        zh(ADDITIONAL_RULES, "附加规则");

        ja(PUT_IN_PLACE_OF_ADDITIONALRULES, "スクリプトのプロンプトの {{additionalRules}} の代わりに入ります");
        zh(PUT_IN_PLACE_OF_ADDITIONALRULES, "替换脚本提示词中的 {{additionalRules}}");

        ja(COPY_ALL, "すべてコピー");
        zh(COPY_ALL, "全部复制");

        ja(ADD_SECTION, "セクションを追加");
        zh(ADD_SECTION, "添加一节");

        ja(CONTINUOUS_MODE, "連続モード");
        zh(CONTINUOUS_MODE, "连续模式");

        ja(STOP_CONTINUOUS_MODE, "連続モードを停止");
        zh(STOP_CONTINUOUS_MODE, "停止连续模式");

        ja(SCRIPTS_ONLY, "スクリプトのみ");
        zh(SCRIPTS_ONLY, "仅脚本");

        ja(TAGS_ONLY, "タグのみ");
        zh(TAGS_ONLY, "仅标签");

        ja(WHAT_THE_CONTINUOUS_MODE_GENERATES, "連続モードが生成するもの：一度に一種類ずつ行うので、モデルは同じプロンプトを使い続けます");
        zh(WHAT_THE_CONTINUOUS_MODE_GENERATES, "连续模式生成的内容：每次只生成一种，因此模型始终使用同一个提示词");

        ja(GENERATE_THE_TAGS, "スクリプトがありタグのないセクションのタグを、なくなるか停止するまで生成します");
        zh(GENERATE_THE_TAGS, "为有脚本但没有标签的各节生成标签，直到没有为止或你停止");

        ja(GENERATE_THE_SCRIPT, "スクリプトのないセクションのスクリプトを生成し、物語の次のパートのセクションを追加してそのスクリプトを生成し続けます。物語のパートがなくなるか、停止するまで続きます");
        zh(GENERATE_THE_SCRIPT, "为没有脚本的各节生成脚本，并持续为故事的后续段落添加新的一节并生成脚本，直到故事用完或你停止为止");

        ja(CONTINUOUS_MODE_IS_FINISHED, "連続モードが完了しました：すべてのスクリプトにタグがあります");
        zh(CONTINUOUS_MODE_IS_FINISHED, "连续模式已完成：所有脚本都已有标签");

        ja(CONTINUOUS_MODE_IS_FINISHED_THERE, "連続モードが完了しました：物語のパートがもうありません");
        zh(CONTINUOUS_MODE_IS_FINISHED_THERE, "连续模式已完成：故事中没有更多段落了");

        ja(CONTINUOUS_MODE_STOPPED, "連続モードを停止しました");
        zh(CONTINUOUS_MODE_STOPPED, "连续模式已停止");

        ja(CONTINUOUS_MODE_STOPPED_THE_MODEL, "連続モードを停止しました：モデルが何も書きませんでした");
        zh(CONTINUOUS_MODE_STOPPED_THE_MODEL, "连续模式已停止：模型没有写出任何内容");

        ja(THERE_IS_NO_MANGA, "この本にはまだマンガがありません。プラスで作成してください。");
        zh(THERE_IS_NO_MANGA, "这本书还没有漫画。请用加号创建一个。");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(NO_SECTIONS_YET_ADD_ONE, "セクションはまだありません。追加すると物語の最初のパートから始まります。");
        zh(NO_SECTIONS_YET_ADD_ONE, "还没有任何一节。添加一节，从故事的最初几段开始。");

        ja(MANGA_SCRIPT, "マンガスクリプト %d");
        zh(MANGA_SCRIPT, "漫画脚本 %d");

        ja(TITLE, "タイトル");
        zh(TITLE, "标题");

        ja(SAVE, "保存");
        zh(SAVE, "保存");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");

        ja(DELETE_THE_MANGA_THIS_CANNOT, "マンガ「%s」を削除しますか？ 元に戻せません。");
        zh(DELETE_THE_MANGA_THIS_CANNOT, "删除漫画“%s”？此操作无法撤销。");

        ja(EMPTY_OR_UNCHANGED_THE_BUILT, "空または未変更の場合: 組み込みのテキストを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT, "留空或未修改：使用内置文本");

        ja(THE_STORY_HAS_NO_PARTS, "物語にはまだパートがありません");
        zh(THE_STORY_HAS_NO_PARTS, "故事还没有任何段落");

        ja(NO_NEW_PARTS, "別のセクションにできる新しい物語のパートがありません（物語は %d パート）");
        zh(NO_NEW_PARTS, "没有可再建一节的新故事段落（故事共有 %d 段）");

        ja(THERE_IS_NO_SCRIPT, "コピーするスクリプトもタグもありません");
        zh(THERE_IS_NO_SCRIPT, "没有可复制的脚本或标签");

        ja(ALL_SCRIPTS_AND_TAGS_COPIED, "すべてのスクリプトとタグをコピーしました");
        zh(ALL_SCRIPTS_AND_TAGS_COPIED, "已复制全部脚本和标签");

        ja(SECTION, "セクション %d");
        zh(SECTION, "第 %d 节");

        ja(SCRIPT, "スクリプト");
        zh(SCRIPT, "脚本");

        ja(TAGS, "タグ");
        zh(TAGS, "标签");

        ja(OVERRIDE, "上書き");
        zh(OVERRIDE, "覆盖");

        ja(SCRIPT_PROMPT_OF_THIS_SECTION, "このセクションのスクリプトのプロンプト");
        zh(SCRIPT_PROMPT_OF_THIS_SECTION, "本节的脚本提示词");

        ja(TAGS_PROMPT_OF_THIS_SECTION, "このセクションのタグのプロンプト");
        zh(TAGS_PROMPT_OF_THIS_SECTION, "本节的标签提示词");

        ja(ADDITIONAL_RULES_OF_THIS_SECTION, "このセクションの追加ルール");
        zh(ADDITIONAL_RULES_OF_THIS_SECTION, "本节的附加规则");

        ja(RULES_ADDENDUM, "ルールの追記");
        zh(RULES_ADDENDUM, "规则补充");

        ja(RULES_ADDED_TO_THE_RULES, "マンガのルールに加えるルール");
        zh(RULES_ADDED_TO_THE_RULES, "加到漫画规则之后的规则");

        ja(ADAPT_FROM_PART, "脚色の開始パート");
        zh(ADAPT_FROM_PART, "改编起始段");

        ja(TO_PART, "終了パート");
        zh(TO_PART, "至段");

        ja(CONTEXT_FROM_PART, "文脈の開始パート");
        zh(CONTEXT_FROM_PART, "上下文起始段");

        ja(EDIT_THE_TEXT, "テキストを編集");
        zh(EDIT_THE_TEXT, "编辑文本");

        ja(COPY, "コピー");
        zh(COPY, "复制");

        ja(SHOW_THE_REQUEST_THE_MODEL, "モデルに送るリクエストを表示");
        zh(SHOW_THE_REQUEST_THE_MODEL, "显示发给模型的请求");

        ja(DELETE_THIS_SECTION, "このセクションを削除しますか？");
        zh(DELETE_THIS_SECTION, "删除这一节？");

        ja(DELETE, "削除");
        zh(DELETE, "删除");

        ja(THOUGHT, "%s 秒間思考しました");
        zh(THOUGHT, "思考了 %s 秒");

        ja(THINKING, "%s 秒間思考中");
        zh(THINKING, "已思考 %s 秒");

        ja(WAITING_FOR_THE_MODEL, "モデルを待っています...");
        zh(WAITING_FOR_THE_MODEL, "正在等待模型...");

        ja(NO_TAGS_YET_GENERATE_THEM, "タグはまだありません。スクリプトから生成するか、自分で書いてください。");
        zh(NO_TAGS_YET_GENERATE_THEM, "还没有标签。可根据脚本生成，或自己编写。");

        ja(NO_SCRIPT_YET_GENERATE, "スクリプトはまだありません。生成するか、自分で書いてください。");
        zh(NO_SCRIPT_YET_GENERATE, "还没有脚本。可生成，或自己编写。");

        ja(ABORT, "中止");
        zh(ABORT, "中止");

        ja(GENERATE_TAGS, "タグを生成");
        zh(GENERATE_TAGS, "生成标签");

        ja(GENERATE_SCRIPT, "スクリプトを生成");
        zh(GENERATE_SCRIPT, "生成脚本");

        ja(TAGS_COPIED, "タグをコピーしました");
        zh(TAGS_COPIED, "标签已复制");

        ja(SCRIPT_COPIED, "スクリプトをコピーしました");
        zh(SCRIPT_COPIED, "脚本已复制");

        ja(EMPTY_OR_UNCHANGED_THE_SETTING, "空または未変更の場合: マンガの設定を使います");
        zh(EMPTY_OR_UNCHANGED_THE_SETTING, "留空或未修改：使用漫画的设置");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");

        ja(ABOUT_TOKENS, "約 %d トークン");
        zh(ABOUT_TOKENS, "约 %d 个令牌");

        ja(OF_THE_MODEL_HAS_ROOM, "（モデルの余裕: %d、返答分を除いたコンテキスト）");
        zh(OF_THE_MODEL_HAS_ROOM, "（模型可容纳 %d，即去掉回复后的上下文）");

        ja(LORE_ENTRIES, "ロアエントリ: %d");
        zh(LORE_ENTRIES, "设定条目：%d");

        ja(MORE_DID_NOT_FIT, "（さらに %d 件は収まりませんでした）");
        zh(MORE_DID_NOT_FIT, "（另有 %d 条放不下）");

        ja(THE_REQUEST_ABOUT_TOKENS, "リクエスト（約 %d トークン）がモデルの余裕（%d）を超えています");
        zh(THE_REQUEST_ABOUT_TOKENS, "请求（约 %d 个令牌）超出了模型可容纳的范围（%d）");

        ja(LORE_ENTRIES_WERE_LEFT_OUT, "コンテキストに収めるため、ロアエントリ %d 件を除きました");
        zh(LORE_ENTRIES_WERE_LEFT_OUT, "为适应上下文，已省略 %d 条设定条目");

        ja(RESET_TO_DEFAULT, "デフォルトに戻す");
        zh(RESET_TO_DEFAULT, "恢复默认");

        ja(A_NEW_MANGA_STARTS, "新しいマンガはこの設定で始まります。作成済みのマンガは自分の設定のままです。");
        zh(A_NEW_MANGA_STARTS, "新漫画以这些设置开始。已创建的漫画保留自己的设置。");

        ja(TEXT_BEFORE_EVERY_REQUEST, "すべてのリクエストの前に置くテキスト");
        zh(TEXT_BEFORE_EVERY_REQUEST, "每个请求之前的文本");

        ja(PUT_INTO_THE_SYSTEM_MESSAGE, "システムメッセージの、ロアの前に入ります。すべてのマンガに共通する指示用です。");
        zh(PUT_INTO_THE_SYSTEM_MESSAGE, "放入系统消息中、设定之前。用于适用于你所有漫画的指令。");

        ja(CONTEXT_PARTS_BEFORE, "前の文脈のパート数");
        zh(CONTEXT_PARTS_BEFORE, "前文上下文段数");

        ja(CONTEXT_PARTS_AFTER, "後の文脈のパート数");
        zh(CONTEXT_PARTS_AFTER, "后文上下文段数");

        ja(DEFAULT_SCRIPT_PROMPT, "デフォルトのスクリプトのプロンプト");
        zh(DEFAULT_SCRIPT_PROMPT, "默认脚本提示词");

        ja(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "空または未変更の場合: 組み込みのプロンプトを使います");
        zh(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT, "留空或未修改：使用内置提示词");

        ja(DEFAULT_TAGS_PROMPT, "デフォルトのタグのプロンプト");
        zh(DEFAULT_TAGS_PROMPT, "默认标签提示词");

        ja(DEFAULT_ADDITIONAL_RULES, "デフォルトの追加ルール");
        zh(DEFAULT_ADDITIONAL_RULES, "默认附加规则");

        ja(THE_STORY_HAS_NO_TEXT, "このセクションのパートに物語のテキストがありません");
        zh(THE_STORY_HAS_NO_TEXT, "本节的段落中没有故事文本");

        ja(GENERATE_OR_WRITE_THE_SCRIPT, "先にこのセクションのスクリプトを生成するか書いてください");
        zh(GENERATE_OR_WRITE_THE_SCRIPT, "请先生成或编写本节的脚本");

        ja(MANGA_GENERATOR_SETTINGS, "マンガジェネレーター設定");
        zh(MANGA_GENERATOR_SETTINGS, "漫画生成器设置");
    }
}
