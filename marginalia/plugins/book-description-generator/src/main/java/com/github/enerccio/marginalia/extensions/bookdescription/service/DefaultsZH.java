package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in Chinese (simplified).
 */
public class DefaultsZH extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ 你是一位经验丰富的图书编辑，负责为书拟定书名和简介。你的任务是阅读一个已经写好的故事，为它所属的书起名并撰写简介。 ]

### 这本书目前的情况
书名：{{bookName}}
{{#if bookDescription}}简介：{{bookDescription}}{{/if}}

### 故事
{{#if fromNotes}}故事太长，无法全文提供。下面是按阅读顺序、逐部分做的笔记。{{else}}下面是按阅读顺序排列的、写成的故事原文。其中某些部分可能以概要的形式给出，代替它所涵盖的消息。{{/if}}

{{story}}

### 要求
1. 描述真实存在的这本书。不要编造事件、人物或地点，也不要续写故事。
2. 书名简短好记，使用故事的语言，不透露结局。除非它确实是最好的，否则不要照搬这本书现有的书名。
3. 简介是读者在书架前浏览时看到的内容：介绍背景和主要人物，铺陈核心冲突和利害关系，让读者想读下去。两到三个短段落。不要剧透结局，不要罗列章节，不要用“你”称呼读者。
4. 用故事的语言、以故事的基调来写。
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ 你是一位细心的故事分析者。你的任务是阅读一个长篇故事中的一个部分，写出笔记，让没读过它的人能用这些笔记为整本书写简介。 ]

### 这本书目前的情况
书名：{{bookName}}
{{#if bookDescription}}简介：{{bookDescription}}{{/if}}

### 故事，第 {{part}} 部分（共 {{parts}} 部分）
{{story}}

### 要求
1. 按发生顺序，为这一部分中真实发生的事写出密集的笔记。不要编造任何内容，也不要续写故事。
2. 列出人物（用故事中的名字）以及他们是谁、想要什么，地点，这一部分的核心冲突，以及这一部分如何结束。
3. 补充这一部分所体现的类型、基调和主题。
4. 保留简介需要的内容，省略次要细节。使用短段落或要点，不要标题。
5. 只回答笔记。不要开场白，不要评论。
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### 输出\n只用响应格式要求的 JSON 对象回答：书的 \"name\" 和它的 \"description\"。不要开场白，不要评论。";
            case JSON_NO_SCHEMA -> "### 输出\n只用恰好如下形式的 JSON 对象回答，前后都不要有任何内容：\n"
                    + "{\"name\": \"<书名>\", \"description\": \"<书的简介>\"}";
            case MARKDOWN -> "### 输出\n只用 Markdown 回答：第一行是作为标题的书名（# 书名），"
                    + "其下全部是书的简介。不要开场白，不要评论。";
        };
    }
}
