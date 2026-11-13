---
label: 프롬프트 템플릿
order: 100
---

# 프롬프트 템플릿

템플릿은 [Handlebars](https://handlebarsjs.com/guide/) 문법을 사용하며, SillyTavern [매크로](macros.md)로 확장되어 있습니다. 이 페이지에서는 각 템플릿이 받는 변수와 문법 중 Handlebars 부분을 다룹니다.

{%{

<a id="variables"></a>

## 변수

이중 중괄호로 감싼 변수는 그 값으로 바뀝니다. 예를 들어 `{{instructions}}`는 해당 파트에 대해 작성한 지시문이 됩니다. 값이 비어 있으면 아무것도 출력되지 않습니다.

<a id="variables-of-each-template"></a>

### 템플릿별 변수

| 템플릿 | 변수 |
|---|---|
| **마스터 템플릿** | `{{backgroundLore}}` 활성화된 로어북 항목 (*로어 블록 안*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` 브랜치의 모든 요약, `{{annotations}}` 모델에 전달하도록 표시한 [메모](../books/annotations.md#giving-a-note-to-the-model) (프롬프트에 포함된 파트용) |
| **User Prompt** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}` - 턴 지시문 |
| **Summary Prompt** | `{{text}}` 요약할 파트, `{{backgroundLore}}` 요약된 파트가 작성될 때 활성화되어 있던 로어 |
| **Meta Summary Prompt** | `{{summaryBlocks}}` 병합할 요약들, 가장 오래된 것부터, `{{backgroundLore}}` 메타 요약 파트가 작성될 때 활성화되어 있던 로어 |
| **Lorebook entry** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

<a id="variables-available-everywhere"></a>

### 모든 곳에서 쓸 수 있는 변수

각 템플릿의 고유 변수 외에도, 모든 템플릿에서 다음을 사용할 수 있습니다:

| 변수 | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | 작성 중인 파트의 턴 지시문 (요약의 경우: 요약 대상 파트의 지시문). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | 책의 서사 설정. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | 책의 이름과 설명. |

모든 템플릿 필드의 힌트 팝오버(ⓘ)에도 이 변수들이 나열되어 있습니다.

<a id="typos"></a>

### 오타

존재하지 않는 변수는 **`Error`**라는 단어로 출력됩니다. `{{instruction}}`(*s* 누락)은 프롬프트에 *Error*를 넣습니다. 템플릿 필드(책의 *프롬프트*, *설정*, 로어북 항목)는 이런 이름을 필드 아래에 *Unknown names (sent to the model as "Error")*로 나열합니다. 이는 경고일 뿐이며 템플릿은 그대로 저장되므로, 표시된 이름을 수정하세요. 변수와 섹션은 모든 곳에서 검사되며, 출력되지 않는 `{{#if}}` 분기 안도 포함됩니다. 조건에만 사용된 이름은 보고되지 않는데, 이는 *Error*를 출력하지 않기 때문입니다. `{{#if name}}`에서 알 수 없는 이름은 거짓이고, SillyTavern의 `{{if name}}`에서는 일반 텍스트로 취급되어 참이 됩니다.

<a id="sections"></a>

## 섹션

섹션은 변수에 값이 있을 때만 내용을 출력합니다. 안에서 `{{.}}`는 그 값입니다:

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

*장면 배경*이 비어 있으면 줄 전체가 사라집니다. 기본 템플릿은 이 방식을 모든 곳에서 사용하므로, 프롬프트에 빈 제목이 남지 않습니다.

**반전 섹션**은 변수가 비어 있을 때만 내용을 출력합니다:

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## 조건

선택적 `{{else}}`를 포함하는 `{{#if}}`:

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}`는 `{{#if x}}`의 반대입니다. SillyTavern 형식인 `{{if x}}...{{/if}}`도 사용할 수 있으며, 부정, 변수, 비교 등 더 많은 옵션이 있습니다. [조건문](macros.md#conditionals)을 참고하세요.

인자가 없는 매크로는 섹션과 조건에서 변수처럼 사용할 수 있습니다:

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## 주석과 이스케이프

| 구문 | 결과 |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | 출력에서 제거됩니다. 템플릿에 메모를 남길 때 사용하세요. |
| `\{{instructions}}` | 값 대신 `{{instructions}}`를 그대로 출력합니다. |
| `{{{instructions}}}` | `{{instructions}}`와 같습니다. Marginalia에서는 아무것도 HTML 이스케이프되지 않으므로 `<`, `&`, 따옴표는 그대로 출력됩니다. |

SillyTavern의 주석 `{{// ...}}`와 이스케이프 `\{\{...\}\}`도 사용할 수 있습니다. [유틸리티](macros.md#utility)를 참고하세요.

<a id="order-of-evaluation"></a>

## 평가 순서

생성된 파트 하나에 대해 템플릿은 다음 순서로 처리됩니다:

1. **사용자 프롬프트**;
2. 활성화된 **로어북 항목**을 그 순서대로;
3. **마스터 템플릿**.

이들은 모두 [변수](macros.md#variables) 집합을 공유합니다. 사용자 프롬프트에서 `{{setvar}}`로 설정한 값은 로어북 항목과 마스터 템플릿에서 읽을 수 있습니다. 로어북 항목에서 설정한 값은 이후의 항목과 마스터 템플릿에서 볼 수 있지만, 이미 처리된 사용자 프롬프트에서는 볼 수 없습니다.

로어북 필터는 처리된 사용자 프롬프트를 보므로, 사용자 프롬프트의 매크로가 항목을 활성화하는 텍스트를 만들어 낼 수 있습니다.

<a id="what-is-kept-between-parts"></a>

## 파트 사이에 유지되는 것

| | |
|---|---|
| **로컬 변수** (`{{setvar}}`, `{{.name}}`) | 생성된 파트와 함께 저장됩니다. 다음 파트는 활성 브랜치의 마지막 파트 값으로 시작하므로, 각 브랜치는 고유한 값을 갖습니다. *다시 생성*은 이전 파트의 값에서 다시 시작합니다. |
| **전역 변수** (`{{setglobalvar}}`, `{{$name}}`) | 책과 함께 저장되며 모든 브랜치가 공유합니다. *다시 생성*이나 파트 삭제로 되돌려지지 않습니다. |
| **요약 목록** | 변수를 읽을 수 있지만, 변수의 변경 사항은 버려집니다. |

`{{pick}}`는 같은 책에서 매번 같은 결과를 주고, `{{random}}`은 매번 새로운 결과를 줍니다.

<a id="writing-your-own-templates"></a>

## 나만의 템플릿 작성하기

기본 템플릿에서 시작하세요. 힌트 팝오버를 열고 **기본 템플릿 삽입**을 클릭한 다음 수정합니다. 유지해야 할 것들:

- 마스터 템플릿의 `{{#backgroundLore}}` 블록 - 이것이 없으면 로어북이 사용되지 않습니다(*사용자 프롬프트 앞* 항목만 모델에 전달됩니다);
- 마스터 템플릿의 `{{#summaries}}` 블록 - 이것이 없으면 요약이 사라집니다;
- 사용자 프롬프트의 `{{instructions}}` - 이것이 없으면 모델이 무엇이 일어나야 하는지 보지 못합니다;
- 요약 프롬프트의 `{{text}}` - 이것이 없으면 요약할 내용이 없습니다.

}%}
