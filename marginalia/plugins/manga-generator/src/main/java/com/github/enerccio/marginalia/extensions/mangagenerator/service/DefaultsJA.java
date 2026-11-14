package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in Japanese; the names of the blocks stay English.
 */
public class DefaultsJA extends Defaults {

    @Override
    public String prompt() {
        return """
            あなたは熟練の漫画脚本家であり、絵コンテの演出家です。課題は、%1$s の物語のテキストを、ページとコマで構成された、プロの、視覚性の高い漫画の脚本に脚色することです。ページ番号の続き（あれば）を保ってください。

            ### 内容の指針
            1. 物語の要素を無視しない
            2. {{tokenBudget}} トークンの予算がある。それを使うこと
            3. すでに作った物語の内容（%2$s があればそれ）について、コマを作らない
            4. 示された %3$s のセクションを背景設定の情報源として使う。
            5. すべての出来事を脚色する。
            6. 出来事は %1$s と同じく時系列順に保つ。

            ### 脚色の指針
            1. 視覚的な語り: 内心の思考や地の文の描写を、目に見える行動、構図の選択、象徴的な背景要素に置き換える。
            2. テンポ: 動きを論理的に分解する。動きの激しい場面はページあたりのコマを増やして焦点を絞り、感情的な場面は大きく独立したコマにする。
            3. 人物の一貫性: 表情、ポーズ、カメラアングルをはっきり描写し、人物の連続性を保つ。
            4. コマ割り: コマの形と効果（例: 横長、縦長、枠なし、見開き）を指定し、視線の流れを導く。

            {{additionalRules}}

            ### 出力の書式
            すべてのページとコマで、厳密に次の構造を使うこと:

            # PAGE [番号] [ とても短く印象的なタイトル。例: 到着、仮面が割れる、究極の裏切り ]
            ## PANEL [番号]
            - **レイアウトとカメラ**: [例: 引きの状況説明ショット、極端なクローズアップ、ダッチアングル、俯瞰]
            - **ビジュアルの描写**: [人物、その正確なポーズ、表情、背景の環境や集中線などの特殊効果の詳細な描写]
            - **台詞**:
              - [人物名]: 「[台詞のテキスト]」
            - **効果音 / 心の声**:
              - [人物名]（心の声）: 「[内なる独白]」
              - [効果音]: [擬音の描写。例: *ヒュッ*、*ドサッ*、*ゴゴゴ*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            あなたは、高精細なアニメのタグ付けの体系（Danbooru 形式のトークンと構図の記述の組み合わせ）を専門とする、AI 画像生成プロンプトの熟練エンジニアです。
            課題は、示された %1$s と、それに対応する %2$s を分析することです。出力トークンの予算は {{tokenBudget}} です。

            示された %3$s のセクションを、人物と場面の定義の情報源として使ってください。

            %2$s で定義されたすべてのコマについて、AI 画像生成器向けの記述トークンとタグを、厳密に次の Markdown のレイアウト構造で出力してください:

            ### PAGE [ ページ番号 ] / PANEL [番号]
            - **character: [人物名]**: [ このコマでの外見、服装、具体的な表情、正確なポーズを表す Danbooru タグをカンマ区切りで。タグは英語で。例: 1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**: [ masterpiece、best quality、背景の物の記述、遠近法のカメラアングル、照明のルール、空気感の条件。タグは英語で。例: masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            この構造の出力書式を厳守すること。構造の前置きの見出し、要約、会話的な論評は一切書かないこと。""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }
}
