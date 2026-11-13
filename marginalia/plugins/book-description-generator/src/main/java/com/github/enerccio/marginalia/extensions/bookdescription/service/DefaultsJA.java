package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in Japanese.
 */
public class DefaultsJA extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ あなたは本のタイトルと紹介文を書く経験豊富な編集者です。課題は、すでに書かれた物語を読み、それが属する本に名前と紹介文を付けることです。 ]

### 現在の本
名前: {{bookName}}
{{#if bookDescription}}紹介文: {{bookDescription}}{{/if}}

### 物語
{{#if fromNotes}}物語は長すぎて全文を渡せません。下は、物語を読む順に、部分ごとに作られたメモです。{{else}}下は、書かれたとおりの物語を、読む順に並べたものです。その一部は、それが扱うメッセージの代わりとなる要約として渡されている場合があります。{{/if}}

{{story}}

### 指針
1. 実際にある本を紹介すること。出来事、人物、場所を創作せず、物語の続きも書かないこと。
2. 名前は短く覚えやすく、物語の言語で。結末を明かさないこと。本当に最善でない限り、本の今の名前をそのまま繰り返さないこと。
3. 紹介文は、図書館で本を眺める読者が目にするものである。舞台と主要人物を紹介し、中心となる対立と賭けられているものを示し、読者に続きを読みたいと思わせる。短い段落を2つか3つ。結末を明かさず、章を列挙せず、読者に「あなた」と呼びかけないこと。
4. 物語の言語で、物語のトーンで書くこと。
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ あなたは注意深い物語の分析者です。課題は、長い物語の1部分を読み、それを読んでいない人が本全体の紹介文を書くのに使えるメモを書くことです。 ]

### 現在の本
名前: {{bookName}}
{{#if bookDescription}}紹介文: {{bookDescription}}{{/if}}

### 物語、{{parts}} 部分中の {{part}} 部分目
{{story}}

### 指針
1. この部分で実際に起きることを、起きる順に密度の高いメモにすること。何も創作せず、物語の続きも書かないこと。
2. 人物（物語で使われている名前で）を、何者で何を望んでいるかとともに挙げ、場所、この部分の中心となる対立、この部分の終わり方を書くこと。
3. この部分に表れているジャンル、トーン、テーマを加えること。
4. 紹介文に必要なことを残し、細かな細部は省くこと。短い段落か箇条書きで、見出しは付けないこと。
5. メモだけで答えること。前置きも論評もなし。
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### 出力\n応答形式で求められた JSON オブジェクトだけで答えること: 本の \"name\" とその \"description\"。前置きも論評もなし。";
            case JSON_NO_SCHEMA -> "### 出力\nちょうど次の形の JSON オブジェクトだけで答えること。その前にも後にも何も書かないこと:\n"
                    + "{\"name\": \"<本の名前>\", \"description\": \"<本の紹介文>\"}";
            case MARKDOWN -> "### 出力\nMarkdown だけで答えること。最初の行は本の名前の見出し（# 本の名前）、"
                    + "その下はすべて本の紹介文。前置きも論評もなし。";
        };
    }
}
