---
label: 템플릿과 매크로
order: 940
icon: code
---

# 템플릿과 매크로

Marginalia가 모델로 보내는 모든 내용은 **템플릿**으로 만들어집니다. 템플릿은 생성되는 각 부분마다 채워지는 자리표시자가 있는 텍스트입니다. 프롬프트의 틀부터 로어북 항목 하나까지 모두 바꿀 수 있습니다.

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## 템플릿이 사용되는 곳

| 템플릿 | 설정 위치 | 생성되는 결과 |
|---|---|---|
| **Master Template** | 책 [Prompts](../books/prompts.md), *Settings → Templates* | 시스템 메시지: 역할, 로어, 문체, 요약 |
| **User Prompt** | 책 Prompts, *Settings → Templates* | 생성 중인 부분에 대한 요청 |
| **Summary Prompt** | 책 Prompts, *Settings → Templates* | [요약](../books/summaries.md)에 대한 요청 |
| **Meta Summary Prompt** | 책 Prompts, *Settings → Templates* | [메타 요약](../books/summaries.md#meta-summaries)에 대한 요청 |
| **Lorebook entry content** | [로어북 항목](../lorebooks/entries.md) | 프롬프트에 삽입되는 로어 |

*Point of View*, *Tense*, *Style*은 템플릿이 아니라 일반 텍스트입니다.

<a id="two-kinds-of-placeholders"></a>

## 두 가지 자리표시자

템플릿은 두 가지 문법을 함께 사용합니다.

- **Handlebars** - {%{`{{instructions}}`}%}와 같은 변수, 그리고 값이 비어 있지 않을 때만 출력되는
  {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%}와 같은 섹션. 자세한 내용은 [프롬프트 템플릿](templates.md)을 참고하세요.
- **SillyTavern 매크로** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} 및 SillyTavern 사용자에게 익숙한 나머지 매크로. SillyTavern에서 가져온 로어북과 프롬프트도 그대로 동작합니다. 자세한 내용은 [매크로 참고서](macros.md)를 참고하세요.

두 문법은 모든 템플릿에서 동작하며, 자유롭게 섞어 쓸 수 있습니다.

<a id="help-in-the-editor"></a>

## 편집기의 도움말

모든 템플릿 필드에는 힌트 팝오버가 있습니다.

- **Insert Default Template**은 기본 템플릿을 필드에 복사합니다.
- **Available Template Variables**는 해당 템플릿의 변수를, 모든 곳에서 쓸 수 있는 변수를 포함해 나열합니다.
- **Available Macros**는 짧은 설명과 함께 모든 매크로를 나열합니다.

![템플릿 필드의 힌트 팝오버](../../images/template-hints.png)

구문 오류가 있는 템플릿(예: 닫히지 않은 섹션)은 책 프롬프트와 Settings에서 저장할 수 없습니다. 로어북 항목에서는 저장되며 유효하지 않은 것으로 표시되고, 수정될 때까지 일반 텍스트로 사용됩니다.

알 수 없는 변수(보통 오타)는 저장을 막지 않습니다. 대신 해당 필드 아래에 경고로 표시됩니다. 자세한 내용은 [오타](templates.md#typos)를 참고하세요.

생성된 부분의 메뉴에 있는 **Show Prompt**는 실제로 전송된 프롬프트를 보여 줍니다. 템플릿이 무엇을 만들었는지 확인하는 가장 좋은 방법입니다.

<a id="in-this-section"></a>

## 이 섹션의 문서

- [프롬프트 템플릿](templates.md) - 각 템플릿의 변수, 섹션, 조건, 평가 순서
- [매크로 참고서](macros.md) - 모든 매크로, 변수, 조건문, SillyTavern 호환성
