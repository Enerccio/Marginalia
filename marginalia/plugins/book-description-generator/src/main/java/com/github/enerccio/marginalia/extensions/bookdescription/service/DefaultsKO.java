package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in Korean; the JSON fields and the Markdown heading of the answer are
 * the ones the plugin reads.
 */
public class DefaultsKO extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ 너는 책의 제목과 소개 문구를 쓰는 경험 많은 편집자다. 너의 과제는 이미 쓰인 이야기를 읽고, 그 이야기가 속한 책에 이름과 소개를 붙이는 것이다. ]

### 지금의 책
이름: {{bookName}}
{{#if bookDescription}}소개: {{bookDescription}}{{/if}}

### 이야기
{{#if fromNotes}}이야기가 너무 길어 전부 줄 수 없다. 아래는 이야기를 부분별로, 읽는 순서대로 정리한 메모다.{{else}}아래는 쓰인 그대로의 이야기로, 읽는 순서대로다. 일부는 그것이 다루는 메시지를 대신하는 요약으로 주어질 수 있다.{{/if}}

{{story}}

### 지침
1. 실제로 있는 책을 소개하라. 사건, 인물, 장소를 지어내지 말고 이야기를 이어 쓰지 마라.
2. 이름은 짧고 기억하기 쉬우며 이야기의 언어로 쓴다. 결말을 드러내지 않는다. 책의 지금 이름이 정말로 가장 좋은 것이 아니라면 그대로 되풀이하지 마라.
3. 소개는 서가를 둘러보는 독자가 보는 글이다. 배경과 주요 인물을 소개하고, 중심 갈등과 걸려 있는 것을 제시하며, 계속 읽고 싶게 만든다. 짧은 문단 두세 개. 결말을 누설하지 말고, 장을 나열하지 말고, 독자를 "당신"이라고 부르지 마라.
4. 이야기의 언어로, 이야기의 어조로 써라.
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ 너는 꼼꼼한 이야기 분석가다. 너의 과제는 긴 이야기의 한 부분을 읽고, 그것을 읽지 않은 사람이 책 전체의 소개를 쓰는 데 쓸 수 있는 메모를 쓰는 것이다. ]

### 지금의 책
이름: {{bookName}}
{{#if bookDescription}}소개: {{bookDescription}}{{/if}}

### 이야기, {{parts}}부분 중 {{part}}부분
{{story}}

### 지침
1. 이 부분에서 실제로 일어나는 일을 일어나는 순서대로 촘촘히 메모하라. 아무것도 지어내지 말고 이야기를 이어 쓰지 마라.
2. 인물(이야기에서 쓰는 이름으로)과 그들이 누구이고 무엇을 원하는지, 장소, 이 부분의 중심 갈등, 이 부분이 어떻게 끝나는지를 적어라.
3. 이 부분에서 드러나는 장르, 어조, 주제를 덧붙여라.
4. 소개 문구에 필요한 것은 남기고 사소한 세부는 빼라. 짧은 문단이나 글머리표를 쓰고 머리글은 쓰지 마라.
5. 메모만으로 답하라. 도입도 설명도 없이.
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### 출력\n응답 형식이 요구하는 JSON 객체로만 답하라: 책의 \"name\"과 \"description\". 도입도 설명도 없이.";
            case JSON_NO_SCHEMA -> "### 출력\n정확히 다음 형태의 JSON 객체로만 답하라. 앞뒤에 아무것도 붙이지 마라:\n"
                    + "{\"name\": \"<책의 이름>\", \"description\": \"<책의 소개>\"}";
            case MARKDOWN -> "### 출력\nMarkdown으로만 답하라: 첫 줄은 책의 이름을 머리글로(# 책의 이름), "
                    + "그 아래는 모두 책의 소개다. 도입도 설명도 없이.";
        };
    }
}
