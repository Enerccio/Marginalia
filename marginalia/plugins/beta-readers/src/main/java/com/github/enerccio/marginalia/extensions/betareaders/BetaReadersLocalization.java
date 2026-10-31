package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of Beta Readers in the languages besides English; the English text is the key.
 */
public class BetaReadersLocalization extends PluginLocalizationBundle {

    public static final BetaReadersLocalization T = new BetaReadersLocalization();

    @Override
    protected void loadKeys() {
        ja("Beta Readers", "ベータリーダー");
        zh("Beta Readers", "试读者");

        ja("Persona library", "ペルソナライブラリ");
        zh("Persona library", "读者角色库");

        ja("Personas: %d", "ペルソナ: %d");
        zh("Personas: %d", "读者角色：%d");

        ja("Mediums: %d", "メディア: %d");
        zh("Mediums: %d", "媒介：%d");

        ja("(edited)", "（編集済み）");
        zh("(edited)", "（已编辑）");

        ja("Edit personas", "ペルソナ一覧を編集");
        zh("Edit personas", "编辑读者角色列表");

        ja("Edit mediums", "メディア一覧を編集");
        zh("Edit mediums", "编辑媒介列表");

        ja("Mediums", "メディア");
        zh("Mediums", "媒介");

        ja("Medium", "メディア");
        zh("Medium", "媒介");

        ja("Model of the chats", "チャットのモデル");
        zh("Model of the chats", "聊天使用的模型");

        ja("Protocol of the chats", "チャットのプロトコル");
        zh("Protocol of the chats", "聊天使用的协议");

        ja("Prompt of a chat with one persona", "ペルソナ1人とのチャットのプロンプト");
        zh("Prompt of a chat with one persona", "与单个读者角色聊天的提示词");

        ja("Prompt of a group chat", "グループチャットのプロンプト");
        zh("Prompt of a group chat", "群聊的提示词");

        ja("Prompt", "プロンプト");
        zh("Prompt", "提示词");

        ja("The system prompt of a 1v1 chat", "1対1チャットのシステムプロンプト");
        zh("The system prompt of a 1v1 chat", "一对一聊天的系统提示词");

        ja("The system prompt of a chat with a group of personas", "ペルソナのグループとのチャットのシステムプロンプト");
        zh("The system prompt of a chat with a group of personas", "与一组读者角色聊天的系统提示词");

        ja("What the text stands for: the personas react to it as the audience of that medium",
                "テキストが何を表すか: ペルソナはそのメディアの受け手として反応します");
        zh("What the text stands for: the personas react to it as the audience of that medium",
                "文本代表什么：读者角色会以该媒介的观众身份做出反应");

        ja("What books and folders inherit: a book or a folder can set its own in its settings. The personas and the mediums are yours, a book can add personas of its own.",
                "本とフォルダーが継承する設定です。本やフォルダーは、それぞれの設定で独自の値を指定できます。ペルソナとメディアはあなた専用で、本には独自のペルソナを追加できます。");
        zh("What books and folders inherit: a book or a folder can set its own in its settings. The personas and the mediums are yours, a book can add personas of its own.",
                "书籍和文件夹继承的设置：书籍或文件夹可以在各自的设置中指定自己的值。读者角色和媒介属于你，书籍还可以添加自己的读者角色。");

        ja("Empty: the built-in prompt. {{medium}} is the text of the medium.",
                "空の場合: 組み込みのプロンプト。{{medium}} はメディアのテキストです。");
        zh("Empty: the built-in prompt. {{medium}} is the text of the medium.",
                "留空：使用内置提示词。{{medium}} 是媒介的文本。");

        ja("Empty: the built-in default", "空の場合: 組み込みの既定値");
        zh("Empty: the built-in default", "留空：使用内置默认值");

        ja("Empty: the model of the book", "空の場合: 本のモデル");
        zh("Empty: the model of the book", "留空：使用书籍的模型");

        ja("Empty: the protocol of the book", "空の場合: 本のプロトコル");
        zh("Empty: the protocol of the book", "留空：使用书籍的协议");

        ja("Add", "追加");
        zh("Add", "添加");

        ja("Edit", "編集");
        zh("Edit", "编辑");

        ja("Copy", "コピー");
        zh("Copy", "复制");

        ja("Delete", "削除");
        zh("Delete", "删除");

        ja("Delete \"%s\"?", "「%s」を削除しますか？");
        zh("Delete \"%s\"?", "删除“%s”？");

        ja("Move up", "上へ移動");
        zh("Move up", "上移");

        ja("Move down", "下へ移動");
        zh("Move down", "下移");

        ja("Save", "保存");
        zh("Save", "保存");

        ja("Cancel", "キャンセル");
        zh("Cancel", "取消");

        ja("Name", "名前");
        zh("Name", "名称");

        ja("Description", "説明");
        zh("Description", "描述");

        ja("Icon", "アイコン");
        zh("Icon", "图标");

        ja("Upload icon", "アイコンをアップロード");
        zh("Upload icon", "上传图标");

        ja("Remove icon", "アイコンを削除");
        zh("Remove icon", "移除图标");

        ja("Icon (scaled down to at most %1$d x %1$d pixels; without one, a circle with the first letters of the name is shown)",
                "アイコン（最大 %1$d x %1$d ピクセルに縮小されます。ない場合は名前の最初の文字を入れた円が表示されます）");
        zh("Icon (scaled down to at most %1$d x %1$d pixels; without one, a circle with the first letters of the name is shown)",
                "图标（缩小到最多 %1$d x %1$d 像素；没有图标时显示带有名称首字母的圆形）");

        ja("The file is not an image that can be read", "ファイルは読み取れる画像ではありません");
        zh("The file is not an image that can be read", "该文件不是可读取的图片");

        ja("The file is not an image or it is too big", "ファイルが画像ではないか、大きすぎます");
        zh("The file is not an image or it is too big", "该文件不是图片或太大");

        ja("The name is required", "名前は必須です");
        zh("The name is required", "名称为必填项");

        ja("Another persona has this name", "この名前のペルソナが既にあります");
        zh("Another persona has this name", "已有同名的读者角色");

        ja("Another medium has this name", "この名前のメディアが既にあります");
        zh("Another medium has this name", "已有同名的媒介");

        ja("New persona", "新しいペルソナ");
        zh("New persona", "新建读者角色");

        ja("Edit persona", "ペルソナを編集");
        zh("Edit persona", "编辑读者角色");

        ja("New medium", "新しいメディア");
        zh("New medium", "新建媒介");

        ja("Edit medium", "メディアを編集");
        zh("Edit medium", "编辑媒介");

        ja("How the persona reads and what it cares about; it is what the model plays",
                "ペルソナがどう読み、何を重視するか。モデルが演じる内容です");
        zh("How the persona reads and what it cares about; it is what the model plays",
                "该读者角色如何阅读、关注什么；模型将扮演它");

        ja("Goes into the prompt of the chats: what the text stands for and how the personas react to it",
                "チャットのプロンプトに入ります: テキストが何を表すか、ペルソナがそれにどう反応するか");
        zh("Goes into the prompt of the chats: what the text stands for and how the personas react to it",
                "会加入聊天的提示词：文本代表什么，以及读者角色如何对其做出反应");

        ja("Add the starter personas", "初期ペルソナを追加");
        zh("Add the starter personas", "添加预设读者角色");

        ja("Adds the personas the plugin comes with that are not in the list",
                "拡張機能に付属するペルソナのうち、リストにないものを追加します");
        zh("Adds the personas the plugin comes with that are not in the list",
                "添加扩展自带但不在列表中的读者角色");

        ja("All the starter personas are in the list", "初期ペルソナはすべてリストにあります");
        zh("All the starter personas are in the list", "所有预设读者角色都已在列表中");

        ja("Restore the built-in mediums", "組み込みのメディアを復元");
        zh("Restore the built-in mediums", "恢复内置媒介");

        ja("Adds the mediums the plugin comes with that are not in the list",
                "拡張機能に付属するメディアのうち、リストにないものを追加します");
        zh("Adds the mediums the plugin comes with that are not in the list",
                "添加扩展自带但不在列表中的媒介");

        ja("All the built-in mediums are in the list", "組み込みのメディアはすべてリストにあります");
        zh("All the built-in mediums are in the list", "所有内置媒介都已在列表中");

        ja("built-in", "組み込み");
        zh("built-in", "内置");

        ja("Title",
                "タイトル");
        zh("Title",
                "标题");

        ja("Persona",
                "ペルソナ");
        zh("Persona",
                "读者角色");

        ja("Personas",
                "ペルソナ");
        zh("Personas",
                "读者角色");

        ja("Model",
                "モデル");
        zh("Model",
                "模型");

        ja("Protocol",
                "プロトコル");
        zh("Protocol",
                "协议");

        ja("New chat",
                "新しいチャット");
        zh("New chat",
                "新建聊天");

        ja("Kind",
                "種類");
        zh("Kind",
                "类型");

        ja("One persona",
                "1人のペルソナ");
        zh("One persona",
                "单个读者角色");

        ja("A group",
                "グループ");
        zh("A group",
                "群组");

        ja("A chat with one persona stays that way. A group can start with one persona, and others can join later.",
                "ペルソナ1人とのチャットはそのままです。グループは1人のペルソナで始めることができ、後から他のペルソナが参加できます。");
        zh("A chat with one persona stays that way. A group can start with one persona, and others can join later.",
                "与单个读者角色的聊天始终如此。群组可以从一个读者角色开始，之后其他角色可以加入。");

        ja("Empty: the names of the personas",
                "空の場合: ペルソナの名前");
        zh("Empty: the names of the personas",
                "留空：使用读者角色的名称");

        ja("Reply format",
                "返信の形式");
        zh("Reply format",
                "回复格式");

        ja("Automatic asks for JSON when the model can follow a schema, free text otherwise",
                "自動: モデルがスキーマに従える場合は JSON、それ以外は自由テキストを要求します");
        zh("Automatic asks for JSON when the model can follow a schema, free text otherwise",
                "自动：模型能遵循模式时要求 JSON，否则使用自由文本");

        ja("Create",
                "作成");
        zh("Create",
                "创建");

        ja("Inherited: %s",
                "継承: %s");
        zh("Inherited: %s",
                "继承：%s");

        ja("none",
                "なし");
        zh("none",
                "无");

        ja("this book",
                "この本");
        zh("this book",
                "本书");

        ja("Automatic",
                "自動");
        zh("Automatic",
                "自动");

        ja("JSON",
                "JSON");
        zh("JSON",
                "JSON");

        ja("Free text",
                "自由テキスト");
        zh("Free text",
                "自由文本");

        ja("Choose a persona",
                "ペルソナを選択してください");
        zh("Choose a persona",
                "请选择一个读者角色");

        ja("Choose at least one persona",
                "ペルソナを1人以上選択してください");
        zh("Choose at least one persona",
                "请至少选择一个读者角色");

        ja("A chat with one persona",
                "ペルソナ1人とのチャット");
        zh("A chat with one persona",
                "与单个读者角色的聊天");

        ja("A group chat",
                "グループチャット");
        zh("A group chat",
                "群聊");

        ja("Medium: %s",
                "メディア: %s");
        zh("Medium: %s",
                "媒介：%s");

        ja("Model: %s",
                "モデル: %s");
        zh("Model: %s",
                "模型：%s");

        ja("left",
                "退出済み");
        zh("left",
                "已离开");

        ja("Chats",
                "チャット");
        zh("Chats",
                "聊天");

        ja("Rename chat",
                "チャット名を変更");
        zh("Rename chat",
                "重命名聊天");

        ja("Close chat",
                "チャットを閉じる");
        zh("Close chat",
                "关闭聊天");

        ja("Delete chat",
                "チャットを削除");
        zh("Delete chat",
                "删除聊天");

        ja("Personas of this book",
                "この本のペルソナ");
        zh("Personas of this book",
                "本书的读者角色");

        ja("Closed chats",
                "閉じたチャット");
        zh("Closed chats",
                "已关闭的聊天");

        ja("No chats yet. Start one with the + button.",
                "チャットはまだありません。+ ボタンで開始してください。");
        zh("No chats yet. Start one with the + button.",
                "还没有聊天。点击 + 按钮开始。");

        ja("There is no persona to talk to. Add some to the library in the settings, or to this book.",
                "話し相手のペルソナがありません。設定のライブラリか、この本に追加してください。");
        zh("There is no persona to talk to. Add some to the library in the settings, or to this book.",
                "没有可交谈的读者角色。请在设置的角色库或本书中添加。");

        ja("Medium, model and format",
                "メディア、モデル、形式");
        zh("Medium, model and format",
                "媒介、模型和格式");

        ja("%s joined the conversation.",
                "%s が会話に参加しました。");
        zh("%s joined the conversation.",
                "%s 加入了对话。");

        ja("%s left the conversation.",
                "%s が会話から退出しました。");
        zh("%s left the conversation.",
                "%s 离开了对话。");

        ja("You",
                "あなた");
        zh("You",
                "你");

        ja("Parts %s sent",
                "送信したパート: %s");
        zh("Parts %s sent",
                "已发送的部分：%s");

        ja("Reasoning",
                "推論");
        zh("Reasoning",
                "推理");

        ja("Narrator",
                "ナレーター");
        zh("Narrator",
                "旁白");

        ja("Personas (%d)",
                "ペルソナ (%d)");
        zh("Personas (%d)",
                "读者角色 (%d)");

        ja("Write to the beta readers...",
                "ベータリーダーに書く...");
        zh("Write to the beta readers...",
                "写给试读者……");

        ja("Check for new parts",
                "新しいパートを確認");
        zh("Check for new parts",
                "检查新的部分");

        ja("Include the latest parts",
                "最新のパートを含める");
        zh("Include the latest parts",
                "包含最新的部分");

        ja("Send",
                "送信");
        zh("Send",
                "发送");

        ja("Stop",
                "停止");
        zh("Stop",
                "停止");

        ja("There is no model to ask. Choose one for the chat, the book or in the settings.",
                "問い合わせるモデルがありません。チャット、本、または設定でモデルを選択してください。");
        zh("There is no model to ask. Choose one for the chat, the book or in the settings.",
                "没有可询问的模型。请在聊天、书籍或设置中选择一个。");

        ja("Write a message or include the latest parts.",
                "メッセージを書くか、最新のパートを含めてください。");
        zh("Write a message or include the latest parts.",
                "请写一条消息，或包含最新的部分。");

        ja("Can't ask the model: %s",
                "モデルに問い合わせできません: %s");
        zh("Can't ask the model: %s",
                "无法询问模型：%s");

        ja("The model did not answer.",
                "モデルは応答しませんでした。");
        zh("The model did not answer.",
                "模型没有回答。");

        ja("%d older messages were left out of the request to fit the context.",
                "コンテキストに収めるため、古いメッセージ %d 件をリクエストから省きました。");
        zh("%d older messages were left out of the request to fit the context.",
                "为适应上下文，请求中省略了 %d 条较早的消息。");

        ja("Include the story (%d parts)",
                "ストーリーを含める (%d パート)");
        zh("Include the story (%d parts)",
                "包含故事（%d 个部分）");

        ja("Include the latest parts (%d new)",
                "最新のパートを含める (新規 %d)");
        zh("Include the latest parts (%d new)",
                "包含最新的部分（新增 %d）");

        ja("Include the latest parts (the story changed)",
                "最新のパートを含める (ストーリーが変更されました)");
        zh("Include the latest parts (the story changed)",
                "包含最新的部分（故事已更改）");

        ja("Include the latest parts (none new)",
                "最新のパートを含める (新しいものなし)");
        zh("Include the latest parts (none new)",
                "包含最新的部分（没有新的）");

        ja("Context: %1$s of %2$s tokens",
                "コンテキスト: %1$s / %2$s トークン");
        zh("Context: %1$s of %2$s tokens",
                "上下文：%1$s / %2$s 个词元");

        ja("Context: %s tokens",
                "コンテキスト: %s トークン");
        zh("Context: %s tokens",
                "上下文：%s 个词元");
    }
}
