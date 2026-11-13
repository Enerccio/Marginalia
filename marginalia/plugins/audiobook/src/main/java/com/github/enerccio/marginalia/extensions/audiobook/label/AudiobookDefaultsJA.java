package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in Japanese; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsJA extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            あなたは、複数の声で読むオーディオブックのために、物語の断片にラベルを付けます。断片には番号があり、N は地の文、D は直接話法（台詞）です。{{#if characters}}これまでに分かっている人物: {{characters}}。ほかの人物が話すこともあります。本文での呼び方でその人物を名指ししてください。{{else}}人物は分かっていません。話し手は本文での呼び方で名指ししてください。{{/if}}
            断片ごとに次の項目で答えてください:
            - "id": 断片の番号。
            - "speaker": D の場合は、それを言う人物。文脈から判断する（周りの地の文にある名前、呼びかけ、誰が誰に答えているか）。N の場合は "Narrator"。ただし、文脈からその本文が人物自身の言葉だと分かる場合は除く: 人物が書いて物語が引用している手紙、メモ、メッセージ、看板、銘文、または人物の心の声。その場合は、その人物（書き手、署名の主、考えている人）を、何段落か続く場合も名指しする。斜体（*...* や _..._ のような印）は手がかりにすぎない。同じ書式は強調、音、外国語にも使われ、それは語り手が読む。迷ったら "Narrator"。
            - "delivery": どう読むべきか（{{deliveries}}）、そして 0.0 から 1.0 の "intensity"。
            - "tagged_text": N の場合は空文字列。D の場合は、断片を一字一句そのまま、変えずに。ただし、文脈がはっきりと話している人物のその音を求めている箇所には、音のタグを挿入してよい。ただしまれに（本文全体で {{wordsPerTag}} 語につき最大1つ）。使えるタグ: {{tags}}。書かれているとおりに正確に書くこと。ほかの言葉を変えたり、加えたり、取り除いたりしないこと。タグの付かない断片には空文字列を使うこと。タグのない断片の本文を写してはならない。
            "context" と印の付いた断片はすでにラベル付け済みです。誰が話しているかを理解するために読み、ラベルは付けないでください。
            JSON だけで、断片1つにつき1項目、順番どおりに答えてください。""";
    }
}
