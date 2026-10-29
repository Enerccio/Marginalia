package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Converter (formerly SillyTavern Converter) in the languages besides English; the English text is the key.
 */
public class STConverterLocalization extends PluginLocalizationBundle {

    public static final STConverterLocalization T = new STConverterLocalization();

    @Override
    protected void loadKeys() {
        ja("Converter", "コンバーター");
        zh("Converter", "转换器");

        ja("From", "開始");
        zh("From", "起始");

        ja("To", "終了");
        zh("To", "结束");

        ja("All", "すべて");
        zh("All", "全部");

        ja("Convert range", "範囲を変換");
        zh("Convert range", "转换范围");

        ja("Ask the model for the turn details of the messages in the range", "範囲内のメッセージのターン詳細をモデルに依頼します");
        zh("Ask the model for the turn details of the messages in the range", "让模型为范围内的消息生成回合详情");

        ja("Edit the prompt of the wand", "ワンドのプロンプトを編集");
        zh("Edit the prompt of the wand", "编辑魔杖的提示词");

        ja("Align to turns", "ターンに揃える");
        zh("Align to turns", "对齐到回合");

        ja("The range starts at the beginning of a turn and ends with a message of the character (the end of the range moves back to the last message of the character before it)", "範囲はターンの先頭から始まり、キャラクターのメッセージで終わります（範囲の終わりは、その手前にあるキャラクターの最後のメッセージまで戻ります）");
        zh("The range starts at the beginning of a turn and ends with a message of the character (the end of the range moves back to the last message of the character before it)", "范围从回合的开头开始，以角色的消息结束（范围的结尾会回退到其之前角色的最后一条消息）");

        ja("Auto mode", "自動モード");
        zh("Auto mode", "自动模式");

        ja("Convert the whole range in batches of messages, one request after another", "範囲全体をメッセージのバッチに分けて、リクエストを1件ずつ順に送って変換します");
        zh("Convert the whole range in batches of messages, one request after another", "将整个范围分成若干批消息，逐个请求依次转换");

        ja("Messages per batch", "バッチあたりのメッセージ数");
        zh("Messages per batch", "每批消息数");

        ja("Convert in batches", "バッチで変換");
        zh("Convert in batches", "分批转换");

        ja("Ask the model for the turn details of the range, one batch of messages after another", "範囲のターン詳細をモデルに、メッセージのバッチごとに順に依頼します");
        zh("Ask the model for the turn details of the range, one batch of messages after another", "依次按每批消息让模型生成该范围的回合详情");

        ja("Click a message to start a range, Shift+click to extend it.", "メッセージをクリックすると範囲を開始し、Shiftを押しながらクリックすると範囲を広げます。");
        zh("Click a message to start a range, Shift+click to extend it.", "点击一条消息开始选择范围，按住 Shift 点击可扩展范围。");

        ja("Convert to turn details", "ターン詳細に変換");
        zh("Convert to turn details", "转换为回合详情");

        ja("Chat", "チャット");
        zh("Chat", "聊天");

        ja("Turn details", "ターン詳細");
        zh("Turn details", "回合详情");

        ja("Message", "メッセージ");
        zh("Message", "消息");

        ja("Msgs", "件数");
        zh("Msgs", "消息数");

        ja("Turn", "ターン");
        zh("Turn", "回合");

        ja("Delete the turn details", "ターン詳細を削除");
        zh("Delete the turn details", "删除回合详情");

        ja("The new turn form of the story is not available", "ストーリーの新規ターンフォームは利用できません");
        zh("The new turn form of the story is not available", "故事的新回合表单不可用");

        ja("Click to put the turn details into the new turn form", "クリックしてターン詳細を新規ターンフォームに入れます");
        zh("Click to put the turn details into the new turn form", "点击将回合详情放入新回合表单");

        ja("Scene setting", "シーン設定");
        zh("Scene setting", "场景设定");

        ja("POV character", "視点キャラクター");
        zh("POV character", "视角角色");

        ja("Present characters", "登場キャラクター");
        zh("Present characters", "在场角色");

        ja("Instructions", "指示");
        zh("Instructions", "指令");

        ja("Remove the chat and the turn details made of it from the book?", "チャットと、そこから作成したターン詳細をブックから削除しますか？");
        zh("Remove the chat and the turn details made of it from the book?", "要从书籍中移除该聊天记录及据此生成的回合详情吗？");

        ja("Remove the chat", "チャットを削除");
        zh("Remove the chat", "移除聊天记录");

        ja("chat", "チャット");
        zh("chat", "聊天");

        ja("%d messages, %d checked", "%d件のメッセージ、%d件確認済み");
        zh("%d messages, %d checked", "%d 条消息，已检查 %d 条");

        ja("%d messages loaded", "%d件のメッセージを読み込みました");
        zh("%d messages loaded", "已加载 %d 条消息");

        ja("Not a SillyTavern chat", "SillyTavernのチャットではありません");
        zh("Not a SillyTavern chat", "不是 SillyTavern 聊天记录");

        ja("The chat has no messages", "チャットにメッセージがありません");
        zh("The chat has no messages", "聊天记录中没有消息");

        ja("Go to the next unchecked row", "次の未確認の行へ移動");
        zh("Go to the next unchecked row", "跳到下一个未检查的行");

        ja("All messages are checked", "すべてのメッセージを確認済みです");
        zh("All messages are checked", "所有消息均已检查");

        ja("There are no turn details", "ターン詳細がありません");
        zh("There are no turn details", "没有回合详情");

        ja("All turns are checked", "すべてのターンを確認済みです");
        zh("All turns are checked", "所有回合均已检查");

        ja("No turn details yet: choose a range and use the wand.", "ターン詳細はまだありません。範囲を選んでワンドを使ってください。");
        zh("No turn details yet: choose a range and use the wand.", "还没有回合详情：请选择范围并使用魔杖。");

        ja("%d turns, %d checked. Click one to open it in the new turn form.", "%d個のターン、%d個確認済み。クリックすると新規ターンフォームで開きます。");
        zh("%d turns, %d checked. Click one to open it in the new turn form.", "%d 个回合，已检查 %d 个。点击其中一个可在新回合表单中打开。");

        ja("Turn details (%d)", "ターン詳細（%d）");
        zh("Turn details (%d)", "回合详情（%d）");

        ja("%d messages: about %s tokens (chat %s + prompt %s)", "%d件のメッセージ: 約%sトークン（チャット %s + プロンプト %s）");
        zh("%d messages: about %s tokens (chat %s + prompt %s)", "%d 条消息：约 %s 个词元（聊天 %s + 提示词 %s）");

        ja("%d request, about %s tokens in all", "%d件のリクエスト、合計約%sトークン");
        zh("%d request, about %s tokens in all", "%d 个请求，共约 %s 个词元");

        ja("%d requests, about %s tokens in all", "%d件のリクエスト、合計約%sトークン");
        zh("%d requests, about %s tokens in all", "%d 个请求，共约 %s 个词元");

        ja("Load a chat first", "先にチャットを読み込んでください");
        zh("Load a chat first", "请先加载聊天记录");

        ja("Choose a model", "モデルを選択してください");
        zh("Choose a model", "请选择模型");

        ja("Waiting for the model...", "モデルの応答を待っています...");
        zh("Waiting for the model...", "正在等待模型...");

        ja("Can't convert: %s", "変換できません: %s");
        zh("Can't convert: %s", "无法转换：%s");

        ja("Batch %d of %d: %s", "バッチ %d/%d: %s");
        zh("Batch %d of %d: %s", "第 %d/%d 批：%s");

        ja("Batch %d of %d (try %d of %d): %s", "バッチ %d/%d（試行 %d/%d）: %s");
        zh("Batch %d of %d (try %d of %d): %s", "第 %d/%d 批（第 %d/%d 次尝试）：%s");

        ja("Writing the turn details... %d so far", "ターン詳細を書き込み中... 現在%d件");
        zh("Writing the turn details... %d so far", "正在写入回合详情……目前 %d 个");

        ja("The model is thinking...", "モデルが思考中です...");
        zh("The model is thinking...", "模型正在思考……");

        ja("Can't go on: %s", "続行できません: %s");
        zh("Can't go on: %s", "无法继续：%s");

        ja("Stopped, nothing was changed", "停止しました。何も変更されていません");
        zh("Stopped, nothing was changed", "已停止，未做任何更改");

        ja("The model did not answer.", "モデルから応答がありませんでした。");
        zh("The model did not answer.", "模型没有回答。");

        ja("%1$d turns for the messages %2$d-%3$d", "メッセージ%2$d〜%3$dに対する%1$d個のターン");
        zh("%1$d turns for the messages %2$d-%3$d", "针对第 %2$d-%3$d 条消息生成了 %1$d 个回合");

        ja(" (%d earlier ones replaced)", "（以前の%d件を置き換えました）");
        zh(" (%d earlier ones replaced)", "（替换了之前的 %d 个）");

        ja("Convert the messages %d-%d in %d requests to the model, one after another? The earlier turn details of the range that you have not ticked are replaced.", "メッセージ%d〜%dを%d件のリクエストでモデルに順番に送って変換しますか？範囲内の以前のターン詳細のうち、チェックを付けていないものは置き換えられます。");
        zh("Convert the messages %d-%d in %d requests to the model, one after another? The earlier turn details of the range that you have not ticked are replaced.", "要将第 %d-%d 条消息分 %d 个请求依次发送给模型进行转换吗？该范围内之前未勾选的回合详情将被替换。");

        ja("%1$d turns made in %2$d requests", "%2$d件のリクエストで%1$d個のターンを作成しました");
        zh("%1$d turns made in %2$d requests", "通过 %2$d 个请求生成了 %1$d 个回合");

        ja("Stopped after %d of %d requests (%d turns made)", "%d/%d件のリクエストの後で停止しました（%d個のターンを作成）");
        zh("Stopped after %d of %d requests (%d turns made)", "在 %d/%d 个请求后停止（已生成 %d 个回合）");

        ja(". The range is what is left.", "。範囲は残りの部分になっています。");
        zh(". The range is what is left.", "。范围为剩余部分。");

        ja("Prompt of the wand", "ワンドのプロンプト");
        zh("Prompt of the wand", "魔杖的提示词");

        ja("Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}", "変数: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}");
        zh("Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}, {{previousTurn}}", "变量：{{backgroundLore}}、{{chat}}、{{firstMessage}}、{{lastMessage}}、{{previousTurn}}");

        ja("Default", "デフォルト");
        zh("Default", "默认");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Stop", "停止");
        zh("Stop", "停止");

        ja("The model's answer does not match what was asked for: %s", "モデルの回答が依頼内容と一致しません: %s");
        zh("The model's answer does not match what was asked for: %s", "模型的回答与要求不符：%s");

        ja("The model's answer has no turn for the messages %d to %d", "モデルの回答に、メッセージ%d〜%dのターンがありません");
        zh("The model's answer has no turn for the messages %d to %d", "模型的回答中没有第 %d 至 %d 条消息对应的回合");

        ja("Upload a chat", "チャットをアップロード");
        zh("Upload a chat", "上传聊天记录");

        ja("Load a backup", "バックアップを読み込む");
        zh("Load a backup", "载入备份");

        ja("Pick a book", "ブックを選ぶ");
        zh("Pick a book", "选择书籍");

        ja("A SillyTavern chat (.jsonl, without branches), to rewrite as a story with the model.", "SillyTavernのチャット（.jsonl、分岐なし）。モデルとともにストーリーとして書き直します。");
        zh("A SillyTavern chat (.jsonl, without branches), to rewrite as a story with the model.", "SillyTavern 聊天记录（.jsonl，不含分支），与模型一起改写为故事。");

        ja("Or the parts of a book of yours, from a backup file or the book itself: their instructions, and the text to redraft.", "または、バックアップファイルまたはブックそのものから、あなたのブックのパート（その指示と、書き直す本文）。");
        zh("Or the parts of a book of yours, from a backup file or the book itself: their instructions, and the text to redraft.", "或者你的某本书的各个部分，来自备份文件或书籍本身：它们的指令，以及要重写的文本。");

        ja("Only the instructions and the text are kept with this book. Remove the source to load another.", "このブックには指示と本文だけが保存されます。別のものを読み込むには、まずソースを削除してください。");
        zh("Only the instructions and the text are kept with this book. Remove the source to load another.", "本书只保存指令和文本。要载入其他来源，请先移除当前来源。");

        ja("%d parts loaded", "%d個のパートを読み込みました");
        zh("%d parts loaded", "已载入 %d 个部分");

        ja("Not a backup of a book", "ブックのバックアップではありません");
        zh("Not a backup of a book", "不是书籍备份");

        ja("Book", "ブック");
        zh("Book", "书籍");

        ja("Load", "読み込む");
        zh("Load", "载入");

        ja("The parts of the branch the book is on are loaded.", "ブックが現在いるブランチのパートが読み込まれます。");
        zh("The parts of the branch the book is on are loaded.", "将载入书籍当前所在分支的各个部分。");

        ja("%d parts, %d checked", "%d個のパート、%d個チェック済み");
        zh("%d parts, %d checked", "%d 个部分，已勾选 %d 个");

        ja("Parts", "パート");
        zh("Parts", "部分");

        ja("Remove the source", "ソースを削除");
        zh("Remove the source", "移除来源");

        ja("Remove the source and what you checked of it from the book?", "ソースと、そのチェック内容をこのブックから削除しますか？");
        zh("Remove the source and what you checked of it from the book?", "要从本书中移除该来源及其勾选记录吗？");

        ja("The book has no parts to load", "読み込めるパートがありません");
        zh("The book has no parts to load", "该书没有可载入的部分");

        ja("The backup is damaged: the parts do not lead to the first part", "バックアップが壊れています: パートが最初のパートにつながっていません");
        zh("The backup is damaged: the parts do not lead to the first part", "备份已损坏：各部分无法追溯到第一个部分");

        ja("The stored source of the book can't be read", "保存されているブックのソースを読み取れません");
        zh("The stored source of the book can't be read", "无法读取已保存的书籍来源");

        ja("The stored source of the book is from a newer version of the plugin", "保存されているブックのソースは、より新しいバージョンのプラグインのものです");
        zh("The stored source of the book is from a newer version of the plugin", "已保存的书籍来源来自更新版本的插件");
    }
}
