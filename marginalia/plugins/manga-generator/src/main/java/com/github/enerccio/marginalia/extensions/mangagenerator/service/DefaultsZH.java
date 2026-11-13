package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in Chinese (simplified); the names of the blocks stay English.
 */
public class DefaultsZH extends Defaults {

    @Override
    public String prompt() {
        return """
            你是一位专业的漫画编剧兼分镜导演。你的任务是把 %1$s 中的故事文本改编成一份专业、视觉性强、按页和格组织的漫画脚本。保持页码的连续（如果有）。

            ### 内容要求
            1. 不要忽略任何故事元素
            2. 你有 {{tokenBudget}} 个令牌的预算，要用好它
            3. 不要为你之前已经创作过的故事内容（如果有 %2$s，就是其中的内容）画格
            4. 使用提供的 %3$s 部分作为背景设定的来源。
            5. 改编每一个事件。
            6. 事件保持与 %1$s 中相同的时间顺序。

            ### 改编要求
            1. 视觉化叙事：把内心想法或叙述性描写转化为可见的动作、构图选择或象征性的背景元素。
            2. 节奏：合乎逻辑地拆分动作。动态的高强度场景每页格数更多、焦点更集中；情感时刻使用更大、更独立的格。
            3. 人物一致性：清楚描述表情、姿势和镜头角度，保持人物的连贯。
            4. 格的布局：指定格的形状和效果（例如横幅、竖幅、无边框、跨页大图），引导视觉流向。

            {{additionalRules}}

            ### 输出格式
            每一页和每一格都严格使用如下结构：

            # PAGE [编号] [ 非常简短有力的标题，例如：抵达；面具破碎；终极背叛 ]
            ## PANEL [编号]
            - **布局与镜头**：[例如：远景交代镜头、大特写、荷兰角、鸟瞰]
            - **画面描述**：[详细描述人物、他们的准确姿势、面部表情，以及背景环境或速度线等特效]
            - **对白**：
              - [人物名]：“[台词]”
            - **音效 / 心声**：
              - [人物名]（心声）：“[内心独白]”
              - [音效]：[拟声词描述，例如 *嗖*、*砰*、*隆隆*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            你是一位专业的 AI 图像生成提示词工程师，专精于高保真的动漫标签体系（Danbooru 风格的标记与构图描述相结合）。
            你的任务是分析提供的 %1$s 以及对应的 %2$s。你有 {{tokenBudget}} 个输出令牌的预算。

            使用提供的 %3$s 部分作为人物和场景定义的来源。

            对 %2$s 中定义的每一格，严格按照下面的 Markdown 布局结构，输出为 AI 图像生成器定制的描述性标记和标签：

            ### PAGE [ 页码 ] / PANEL [编号]
            - **character: [人物名]**：[ 用英文 Danbooru 标签描述此格中的外貌、服装、具体表情和准确姿势，用逗号分隔。例如：1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**：[ masterpiece、best quality、背景物体描述、透视镜头角度、光照规则和氛围条件，使用英文标签。例如：masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            严格遵守这一结构化输出格式。不要写任何结构性的开头标题、总结或对话式评论。""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }
}
