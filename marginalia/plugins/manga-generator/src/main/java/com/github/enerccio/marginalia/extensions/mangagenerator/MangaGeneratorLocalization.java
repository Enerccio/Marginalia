package com.github.enerccio.marginalia.extensions.mangagenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Manga Generator in the languages besides English; the English text is the key.
 */
public class MangaGeneratorLocalization extends PluginLocalizationBundle {

    public static final MangaGeneratorLocalization T = new MangaGeneratorLocalization();

    @Override
    protected void loadKeys() {
        ja("Manga Generator", "マンガジェネレーター");
        zh("Manga Generator", "漫画生成器");

        ja("Open a book first", "先に本を開いてください");
        zh("Open a book first", "请先打开一本书");

        ja("New manga", "新しいマンガ");
        zh("New manga", "新建漫画");

        ja("Rename the manga", "マンガの名前を変更");
        zh("Rename the manga", "重命名漫画");

        ja("Move the manga to the left", "マンガを左へ移動");
        zh("Move the manga to the left", "将漫画左移");

        ja("Move the manga to the right", "マンガを右へ移動");
        zh("Move the manga to the right", "将漫画右移");

        ja("Delete the manga", "マンガを削除");
        zh("Delete the manga", "删除漫画");

        ja("Token budget", "トークン予算");
        zh("Token budget", "令牌预算");

        ja("How long a script the model is asked for", "モデルに求めるスクリプトの長さ");
        zh("How long a script the model is asked for", "要求模型生成的脚本长度");

        ja("Parts per section", "セクションあたりのパート数");
        zh("Parts per section", "每节的段落数");

        ja("How many parts of the story a new section adapts", "新しいセクションが脚色する物語のパート数");
        zh("How many parts of the story a new section adapts", "新的一节改编的故事段落数");

        ja("Context before", "前の文脈");
        zh("Context before", "前文上下文");

        ja("Parts before the adapted ones that a new section gives the model as the background", "脚色するパートの前にある、新しいセクションが背景としてモデルに渡すパート");
        zh("Parts before the adapted ones that a new section gives the model as the background", "新的一节作为背景交给模型的、位于改编段落之前的段落");

        ja("Context after", "後の文脈");
        zh("Context after", "后文上下文");

        ja("Parts after the adapted ones that a new section gives the model as the background", "脚色するパートの後にある、新しいセクションが背景としてモデルに渡すパート");
        zh("Parts after the adapted ones that a new section gives the model as the background", "新的一节作为背景交给模型的、位于改编段落之后的段落");

        ja("Script prompt", "スクリプトのプロンプト");
        zh("Script prompt", "脚本提示词");

        ja("Tags prompt", "タグのプロンプト");
        zh("Tags prompt", "标签提示词");

        ja("Additional rules", "追加ルール");
        zh("Additional rules", "附加规则");

        ja("Put in place of {{additionalRules}} in the script prompt", "スクリプトのプロンプトの {{additionalRules}} の代わりに入ります");
        zh("Put in place of {{additionalRules}} in the script prompt", "替换脚本提示词中的 {{additionalRules}}");

        ja("Copy all", "すべてコピー");
        zh("Copy all", "全部复制");

        ja("Add section", "セクションを追加");
        zh("Add section", "添加一节");

        ja("There is no manga in this book yet. Create one with the plus.", "この本にはまだマンガがありません。プラスで作成してください。");
        zh("There is no manga in this book yet. Create one with the plus.", "这本书还没有漫画。请用加号创建一个。");

        ja("Close", "閉じる");
        zh("Close", "关闭");

        ja("No sections yet. Add one to start with the first parts of the story.", "セクションはまだありません。追加すると物語の最初のパートから始まります。");
        zh("No sections yet. Add one to start with the first parts of the story.", "还没有任何一节。添加一节，从故事的最初几段开始。");

        ja("Manga Script %d", "マンガスクリプト %d");
        zh("Manga Script %d", "漫画脚本 %d");

        ja("Title", "タイトル");
        zh("Title", "标题");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Delete the manga \"%s\"? This cannot be undone.", "マンガ「%s」を削除しますか？ 元に戻せません。");
        zh("Delete the manga \"%s\"? This cannot be undone.", "删除漫画“%s”？此操作无法撤销。");

        ja("Empty or unchanged: the built-in text is used", "空または未変更の場合: 組み込みのテキストを使います");
        zh("Empty or unchanged: the built-in text is used", "留空或未修改：使用内置文本");

        ja("The story has no parts yet", "物語にはまだパートがありません");
        zh("The story has no parts yet", "故事还没有任何段落");

        ja("No new parts of the story for another section (the story has %d parts)", "別のセクションにできる新しい物語のパートがありません（物語は %d パート）");
        zh("No new parts of the story for another section (the story has %d parts)", "没有可再建一节的新故事段落（故事共有 %d 段）");

        ja("There is no script or tags to copy", "コピーするスクリプトもタグもありません");
        zh("There is no script or tags to copy", "没有可复制的脚本或标签");

        ja("All scripts and tags copied", "すべてのスクリプトとタグをコピーしました");
        zh("All scripts and tags copied", "已复制全部脚本和标签");

        ja("Section %d", "セクション %d");
        zh("Section %d", "第 %d 节");

        ja("Script", "スクリプト");
        zh("Script", "脚本");

        ja("Tags", "タグ");
        zh("Tags", "标签");

        ja("Override", "上書き");
        zh("Override", "覆盖");

        ja("Script prompt of this section", "このセクションのスクリプトのプロンプト");
        zh("Script prompt of this section", "本节的脚本提示词");

        ja("Tags prompt of this section", "このセクションのタグのプロンプト");
        zh("Tags prompt of this section", "本节的标签提示词");

        ja("Additional rules of this section", "このセクションの追加ルール");
        zh("Additional rules of this section", "本节的附加规则");

        ja("Rules addendum", "ルールの追記");
        zh("Rules addendum", "规则补充");

        ja("Rules added to the rules of the manga", "マンガのルールに加えるルール");
        zh("Rules added to the rules of the manga", "加到漫画规则之后的规则");

        ja("Adapt from part", "脚色の開始パート");
        zh("Adapt from part", "改编起始段");

        ja("to part", "終了パート");
        zh("to part", "至段");

        ja("Context from part", "文脈の開始パート");
        zh("Context from part", "上下文起始段");

        ja("Edit the text", "テキストを編集");
        zh("Edit the text", "编辑文本");

        ja("Copy", "コピー");
        zh("Copy", "复制");

        ja("Show the request the model gets", "モデルに送るリクエストを表示");
        zh("Show the request the model gets", "显示发给模型的请求");

        ja("Delete this section?", "このセクションを削除しますか？");
        zh("Delete this section?", "删除这一节？");

        ja("Delete", "削除");
        zh("Delete", "删除");

        ja("Thought for %s s", "%s 秒間思考しました");
        zh("Thought for %s s", "思考了 %s 秒");

        ja("Thinking for %s s", "%s 秒間思考中");
        zh("Thinking for %s s", "已思考 %s 秒");

        ja("Waiting for the model...", "モデルを待っています...");
        zh("Waiting for the model...", "正在等待模型...");

        ja("No tags yet. Generate them from the script, or write them.", "タグはまだありません。スクリプトから生成するか、自分で書いてください。");
        zh("No tags yet. Generate them from the script, or write them.", "还没有标签。可根据脚本生成，或自己编写。");

        ja("No script yet. Generate it, or write it.", "スクリプトはまだありません。生成するか、自分で書いてください。");
        zh("No script yet. Generate it, or write it.", "还没有脚本。可生成，或自己编写。");

        ja("Abort", "中止");
        zh("Abort", "中止");

        ja("Generate tags", "タグを生成");
        zh("Generate tags", "生成标签");

        ja("Generate script", "スクリプトを生成");
        zh("Generate script", "生成脚本");

        ja("Tags copied", "タグをコピーしました");
        zh("Tags copied", "标签已复制");

        ja("Script copied", "スクリプトをコピーしました");
        zh("Script copied", "脚本已复制");

        ja("Empty or unchanged: the setting of the manga is used", "空または未変更の場合: マンガの設定を使います");
        zh("Empty or unchanged: the setting of the manga is used", "留空或未修改：使用漫画的设置");

        ja("Request", "リクエスト");
        zh("Request", "请求");

        ja("About %d tokens", "約 %d トークン");
        zh("About %d tokens", "约 %d 个令牌");

        ja("of %d the model has room for (its context without the reply)", "（モデルの余裕: %d、返答分を除いたコンテキスト）");
        zh("of %d the model has room for (its context without the reply)", "（模型可容纳 %d，即去掉回复后的上下文）");

        ja("Lore entries: %d", "ロアエントリ: %d");
        zh("Lore entries: %d", "设定条目：%d");

        ja("(%d more did not fit)", "（さらに %d 件は収まりませんでした）");
        zh("(%d more did not fit)", "（另有 %d 条放不下）");

        ja("The request (about %d tokens) is more than the model has room for (%d)", "リクエスト（約 %d トークン）がモデルの余裕（%d）を超えています");
        zh("The request (about %d tokens) is more than the model has room for (%d)", "请求（约 %d 个令牌）超出了模型可容纳的范围（%d）");

        ja("%d lore entries were left out to fit the context", "コンテキストに収めるため、ロアエントリ %d 件を除きました");
        zh("%d lore entries were left out to fit the context", "为适应上下文，已省略 %d 条设定条目");

        ja("Reset to default", "デフォルトに戻す");
        zh("Reset to default", "恢复默认");

        ja("A new manga starts with these settings. A manga you made already keeps its own.", "新しいマンガはこの設定で始まります。作成済みのマンガは自分の設定のままです。");
        zh("A new manga starts with these settings. A manga you made already keeps its own.", "新漫画以这些设置开始。已创建的漫画保留自己的设置。");

        ja("Text before every request", "すべてのリクエストの前に置くテキスト");
        zh("Text before every request", "每个请求之前的文本");

        ja("Put into the system message, before the lore. For instructions that hold for all of your mangas.", "システムメッセージの、ロアの前に入ります。すべてのマンガに共通する指示用です。");
        zh("Put into the system message, before the lore. For instructions that hold for all of your mangas.", "放入系统消息中、设定之前。用于适用于你所有漫画的指令。");

        ja("Context parts before", "前の文脈のパート数");
        zh("Context parts before", "前文上下文段数");

        ja("Context parts after", "後の文脈のパート数");
        zh("Context parts after", "后文上下文段数");

        ja("Default script prompt", "デフォルトのスクリプトのプロンプト");
        zh("Default script prompt", "默认脚本提示词");

        ja("Empty or unchanged: the built-in prompt is used", "空または未変更の場合: 組み込みのプロンプトを使います");
        zh("Empty or unchanged: the built-in prompt is used", "留空或未修改：使用内置提示词");

        ja("Default tags prompt", "デフォルトのタグのプロンプト");
        zh("Default tags prompt", "默认标签提示词");

        ja("Default additional rules", "デフォルトの追加ルール");
        zh("Default additional rules", "默认附加规则");

        ja("The story has no text in the parts of this section", "このセクションのパートに物語のテキストがありません");
        zh("The story has no text in the parts of this section", "本节的段落中没有故事文本");

        ja("Generate or write the script of the section first", "先にこのセクションのスクリプトを生成するか書いてください");
        zh("Generate or write the script of the section first", "请先生成或编写本节的脚本");

        ja("Manga Generator Settings", "マンガジェネレーター設定");
        zh("Manga Generator Settings", "漫画生成器设置");
    }
}
