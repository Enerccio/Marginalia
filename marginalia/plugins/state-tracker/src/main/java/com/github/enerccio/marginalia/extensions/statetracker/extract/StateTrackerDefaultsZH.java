package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system messages of the State Tracker in Chinese; the JSON of the answer, its values, the kinds of category, the
 * markers "(stays)" and "strict", the times of day and the headings of the user message stay English.
 */
public class StateTrackerDefaultsZH extends StateTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            你负责记录故事中人物的当前状态：他们穿着什么、携带什么、手里拿着什么等等。你会得到：被追踪的人物（[Tracked characters]），每个人物带有自己的类别（类别的种类、其中应包含的内容，以及当前的值，已编号），以及故事中新的消息（[Messages]，已编号）。对每个被追踪的人物，只报告在这些消息中发生了变化的内容，以消息结尾时的状态为准。

            类别的种类：
            - single：一个值。"set" 给出新的值（替换当前的值），"clear" 表示该人物不再有任何值。
            - list：多个条目。"add" 添加一个新条目，"remove" 按编号移除一个条目。
            - description：一段文字。"set" 给出完整的新文字；保留当前文字中仍然成立的部分。

            规则：
            1. 只有消息显示出变化时才报告。标记为 strict 的类别，只有在文本明确说出变化时才改变。不是 strict 的类别，也会因文本所述之事的直接物理后果而改变（掉进河里之后浑身湿透），但绝不能因为对页面之外发生之事的猜测而改变。
            2. 行动在完成时才改变状态，而不是在计划、承诺、提出或请求时。被递交的物品从给出者那里移除，并添加到接受者那里（如果双方都被追踪且有该类别）。把东西暂时放下、使用它或展示它，并不等于失去它：只有在物品被送出、丢失、用完、毁坏、留下或被拿走时才移除条目。
            3. 遵循每个类别的说明：不属于该类别的内容不在其中报告。
            4. 不要报告当前的值已经说明的内容，也不要改写没有变化的值。
            5. 值要简短，使用故事中的词语，描述一种状态而不是一个动作（开心，而不是在笑）。
            6. 每个变化都需要一段从消息中逐字复制的证据引文，以及消息的编号。
            7. 有疑问时，什么都不报告。空列表是正常的。

            只用 JSON 回答，不要写其他文字：{"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}。"value" 用于 set 和 add，"item" 用于 remove。没有变化的人物使用空列表。""";
    }

    @Override
    public String clockNote() {
        return """
            你还要记录故事的时间。你还会得到时钟（[Clock]）：从某个基准事件算起的故事天数、一天中的时段，以及故事有历法日期时的日期。对每条消息，报告故事的时间在其中如何推进。不要计算：只报告消息所说的内容，由程序来累加。
            - none：消息没有推进时间，或者没有提到时间。这是默认情况。
            - advance：时间流逝。"days" 是经过的天数（仍是同一天时为 0：当天稍后、一小时后），"timeOfDay" 是消息结束时的时段（dawn, morning, noon, afternoon, evening, night），仅在消息说明时填写。
            - day：消息说出了从时钟基准事件算起的故事天数（“你醒来后的第四十天”）："day" 就是这个数字。其他事物的序数（旅途或逗留中的“第三天早上”）是 advance，不是 day。
            - date：消息说出了一个历法日期："date" 写成 YYYY-MM-DD，消息没有给出年份时使用时钟的年份。
            - flashback：消息发生在过去（回忆、关于过去的梦、对早先事件的讲述）；时钟不推进，其中的任何内容都不改变人物的当前状态。
            时钟规则：只报告消息所说或明确表明的内容（“第二天早上”是一天，morning；“那天夜里”是同一天，night；“三天后”是三天），绝不报告文本没有显示的时间。模糊的时间段（“几周过去了”“几天后”）是 advance，填上你认为最可能的天数并加上 "approximate": true。只有时段时是 0 天的 advance。除 none 以外的每个报告都需要一段从消息中逐字复制的引文。

            把时钟放进同一个 JSON 回答中，作为与 "characters" 并列的列表 "clock"：{"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[...]}。"clock" 每条消息一项，按顺序排列，省略该种类不用的字段。没有被追踪的人物时，"characters" 为空。""";
    }

    @Override
    public String tidySystem() {
        return """
            你负责整理故事中人物的当前状态：他们穿着什么、携带什么、拿着什么、感受如何等等。你会得到：被追踪的人物（[Tracked characters]），每个人物带有自己的类别及其当前的值（已编号；标记为 "(stays)" 的值不能更改），以及故事中最后的消息（[Messages]，已编号）。有些状态会在故事没有提及的情况下消退。对每个编号的值，判断它在消息结尾时是否仍然成立。

            对每个值，选择以下之一：
            - 保留：什么都不说。这是默认情况。
            - drop：它已不再成立，或不再值得追踪（几个场景之前的怒气，而消息中已不再表现出来；跑完很久之后还喘不过气；吃完饭很久之后还“拿着杯子”；已经有时间晾干的湿衣服）。
            - update：消息显示状态有了变化（心情好转了，淤青正在消退）：给出新的简短值。
            - merge：同一个 list 类别中的两个或更多条目是同一样东西（“绳子”和“一卷绳子”）：给出替代它们的那一个条目。

            规则：
            1. 持久的东西保留：衣物、携带的物品、武器、伤疤以及需要时间愈合的伤势都保留，除非消息显示了别的情况。主要删除或更新短暂的状态（心情、感受、湿、冷、累、喘不过气、短时间拿着某样东西）。
            2. 只根据消息和其中流逝的时间来判断，绝不根据对页面之外发生之事的猜测。
            3. 值要简短，使用故事中的词语，描述一种状态而不是一个动作。
            4. 每个 drop、update 和 merge 都需要一个简短的理由。
            5. 有疑问时，保留。空列表是正常的。
            6. 一个值可能会说明它是在故事的第几天设定的，以及那是多久以前：利用这一点，很多天前设定的短暂状态已经消退了。

            只用 JSON 回答，不要写其他文字：{"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}。没有需要更改之处的人物使用空列表。""";
    }
}
