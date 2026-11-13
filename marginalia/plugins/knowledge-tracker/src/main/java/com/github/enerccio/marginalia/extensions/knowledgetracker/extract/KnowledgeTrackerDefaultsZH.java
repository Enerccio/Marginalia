package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker in Chinese (simplified); the JSON and the headings of the user message are
 * the English ones.
 */
public class KnowledgeTrackerDefaultsZH extends KnowledgeTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            你负责记录故事中的人物知道什么。你会得到：世界（设定集，如果提供了的话），被追踪的人物（[Tracked characters]）以及每个人物现在知道或不知道的事情的编号条目，以及故事中新的消息（[Messages]，已编号）。对每个被追踪的人物，只报告在这些消息中发生了变化的内容。

            规则：
            1. add：人物在这些消息中“获得”的知识：亲眼看到、听到、读到、被告知或自己推断出来的。不是读者知道的，不是只有叙述者说的，也不是其他人物知道的。
            2. 不要 add 该世界的成员本来就具备的常识（对王国的臣民来说国王是谁、普通的习俗、语言、自己家乡的地理），也不要 add 世界文本所描述的人物自身就能推出的内容（其种族、身体、能力、经历、家庭、职业）。
            3. 不要 add 该人物的条目中已经写明的内容。
            4. retract：消息明确与之矛盾的条目编号（例如两人见面之后的“从未见过 X”）。只有当正文表现出矛盾时才这样做。说人物不知道 Y 的条目，只有当人物在这些消息中得知 Y 时才 retract。说了不止一件事的条目，只有当消息与其全部内容都矛盾时才 retract；只有一部分矛盾时，保留该条目并 add 新得知的内容。根据消息如何“结束”来判断条目：如果在这些消息的后面，人物又被告知、被提醒或重新推断出条目所说的内容（他问某人是谁，随后被告知），那么条目在结尾时是成立的，所以不要 retract，也不要再次 add。
            5. 绝不推测。拿不准时，什么都不报告。不在场的人物，或者没有看到、听到某个事件的人物（私下的想法、耳语、封好的信），不会从中得知任何东西。
            6. 每一项变化都需要一段从消息中照抄的证据引文（evidence）以及消息的编号。
            7. 只记录在故事后面可能重要的知识：秘密、身份、被揭示的关系和感情、计划、承诺、发现，以及其他人物不知道的事实。不要记录在场所有人物一起看到或听到的事件（故事正文本身已经包含它们）、闲聊、玩笑、情绪、天气、物品和场景细节、人物自己做过的事，也不要为多个人物记录同一个事实。每个人物最多 add 3 条，通常是 0 或 1 条。没有重要内容时，什么都不报告。

            只用 JSON 回答，不要任何其他文字：{"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}。没有变化的人物，其列表为空。""";
    }

    @Override
    public String readingNote() {
        return """
            你也可以用故事中某个较早部分的编号调用 read_part。在较早部分中得知的条目会注明这一点（"learned in part N"），消息也会注明它们属于哪个部分。如果某个条目来自传闻、谣言、计划或介绍，而这些消息似乎证实、重复或否定了它，或者仅凭条目无法判断人物在那时知道什么，就先读那个部分再做决定，而不是去猜。不要出于其他任何理由阅读部分（最多 %d 次）。你的最终回答仍然只能是 JSON。""".formatted(PartReader.MAX_READS);
    }

    @Override
    public String tidySystem() {
        return """
            你负责整理故事中的人物知道什么。你会得到：被追踪的人物以及他们知道或不知道的事情的编号条目，保持不变的条目（仅供比较），以及作为上下文的故事最后几个部分。这些条目会被发送给写作模型，以免它让某个人物拥有其不可能具备的知识；每个条目都占用空间，必须物有所值。

            规则：
            1. drop：丢弃不值得保留的条目：琐事（场景细节、闲聊、玩笑、情绪、物品）、场景中所有人物一起看到的事件（故事中已经包含）、已被后来的事件取代的内容，或者与另一个条目或保持不变的条目说的是同一件事的内容。
            2. merge：把关于同一件事的两个或更多条目合并为一个条目，并保留它们的每一个事实。不要添加条目中没有的任何内容。
            3. 绝不要 drop 或 merge 秘密、身份、关系、被揭示的感情、计划、承诺、发现，或其他人物不知道的任何事情，即使最后几个部分没有提到它：最近没有出现过并不是丢弃它的理由。
            4. 拿不准时就保留。只报告你确定的内容；列表为空是正常的。
            5. 每一次 drop 和 merge 都需要一个简短的理由（reason）。

            只用 JSON 回答，不要任何其他文字：{"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}。无事可做的人物，其列表为空。""";
    }
}
