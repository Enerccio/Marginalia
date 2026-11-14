package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in Japanese; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsJA extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "あなたは画像生成モデル用のプロンプトを書きます。以下の場面から、最も視覚的な瞬間を描く絵のプロンプトを一つ書いてください。誰がいるか (外見、服装、ポーズ)、場所、光、雰囲気を描写します。短いフレーズをカンマで区切って並べ、文章にしないこと。人物の名前は使わず外見で描写し、絵の中に文字を入れないこと。場面の言語にかかわらず、プロンプトは英語で書いてください。プロンプトだけを答えてください。";
    }

    @Override
    public String sentencesInstruction() {
        return "あなたは自然言語を理解する画像生成モデル用のプロンプトを書きます。以下の場面から、最も視覚的な瞬間を描く絵のプロンプトを一つ書いてください。三〜六文の平易な描写文からなる一段落にします。誰がいるか (年齢、外見、服装、ポーズ、表情) と何をしているか、どこで起きているか、構図 (クローズアップ、引きの画、どの方向から)、光と雰囲気を書きます。人物は名前ではなく外見で描写し、見えるものだけを書き、絵の中に文字を入れず、箇条書き、タグ、見出しは使わないこと。場面の言語にかかわらず、プロンプトは英語で書いてください。プロンプトだけを答えてください。";
    }

    @Override
    public String sceneSetting() {
        return "場面設定";
    }

    @Override
    public String pointOfView() {
        return "視点";
    }

    @Override
    public String charactersPresent() {
        return "登場人物";
    }

    @Override
    public String text() {
        return "本文";
    }

    @Override
    public String detailsHeader() {
        return "絵が従うべき詳細";
    }

    @Override
    public String styleHeader() {
        return "絵のスタイルはプロンプトに自動で追加されます。別の画材やスタイルを描写しないでください";
    }
}
