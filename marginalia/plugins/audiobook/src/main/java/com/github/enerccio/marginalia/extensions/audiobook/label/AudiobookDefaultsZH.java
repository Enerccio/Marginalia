package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in Chinese (simplified); the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsZH extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            你为一部多人配音的有声书给故事的片段打标签。片段都有编号；N 是叙述，D 是直接引语（台词）。{{#if characters}}目前已知的人物有：{{characters}}。其他人也可能说话；按正文中的称呼来命名他们。{{else}}人物未知：按正文中的称呼来命名说话者。{{/if}}
            对每个片段，回答以下内容：
            - "id"：片段的编号；
            - "speaker"：对于 D，是说这句话的人物，根据上下文判断（周围叙述中的名字、称呼语、谁在回答谁）。对于 N，是 "Narrator"，除非上下文表明这段文字是某个人物自己的话：人物写下并被故事引用的信件、便条、消息、标牌或铭文，或者人物的内心想法。这时就写出那个人物（写信的人、署名者、思考的人），连续多段也是如此。斜体（如 *...* 或 _..._ 的标记）只是一个提示：同样的标记也用于强调、声音和外语词，这些由叙述者朗读。拿不准时就是 "Narrator"；
            - "delivery"：应该如何朗读（{{deliveries}}），以及 0.0 到 1.0 的 "intensity"；
            - "tagged_text"：对于 N 是空字符串。对于 D，逐字照录该片段、不做改动，只是在上下文明确要求说话人物发出某种声音的地方可以插入一个声音标签，而且要少用（整段文字中每 {{wordsPerTag}} 个词最多一个标签）。允许的标签：{{tags}}，必须原样书写。不要改动、添加或删除任何其他的词。没有标签的片段一律使用空字符串：绝不要照抄没有标签的片段的文字。
            标有 "context" 的片段已经打过标签：阅读它们来理解是谁在说话，不要给它们打标签。
            只用 JSON 回答，每个片段一项，按顺序排列。""";
    }
}
