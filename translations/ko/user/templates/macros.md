---
label: 매크로 참조
order: 90
---

# 매크로 참조

Marginalia는 [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/)의 매크로를 이해하므로,
SillyTavern용으로 작성된 프롬프트와 월드 인포는 수정 없이 동작합니다. 매크로는 모든 [템플릿](templates.md)에서 작동합니다:
마스터 템플릿, 사용자 프롬프트, 요약 프롬프트, 로어북 항목.

{%{

<a id="syntax"></a>

## 문법

| 형식 | 예시 |
|---|---|
| 인수 없이 | `{{user}}` |
| `::`로 구분한 인수 | `{{setvar::mood::nervous}}` |
| 공백 뒤의 첫 인수 | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| 이전 방식의 단일 `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| 중첩 매크로 | `{{getvar::{{user}}_mood}}` |
| 범위 지정(마지막 인수로 내용) | `{{setvar backstory}}Long text...{{/setvar}}` |

- 매크로 이름은 대소문자를 구분하지 않습니다: `{{User}}`와 `{{USER}}` 모두 동작합니다.
- 이름과 인수 주변의 공백은 무시됩니다: `{{ getvar :: mood }}`.
- 추가된 `::`는 마지막 인수에 포함됩니다: `{{setvar::x::a::b}}`는 `x`를 `a::b`로 설정합니다.
- 범위 지정 내용은 먼저 평가된 다음, 들여쓰기가 제거되고 앞뒤 공백이 잘립니다. 중괄호 뒤에 `#`을 붙이면(`{{#setvar name}}...{{/setvar}}`) 공백이 그대로 유지됩니다.

<a id="names-and-participants"></a>

## 이름과 참여자

Marginalia에는 페르소나나 캐릭터 카드가 없으며, 이름은 작성 중인 부분의 턴 지시문에서 가져옵니다.

| 매크로 | 결과 |
|---|---|
| `{{user}}` | 시점 캐릭터. |
| `{{char}}` | 시점 캐릭터. 별칭 `{{charIfNotGroup}}`. |
| `{{group}}` | 현재 등장하는 캐릭터들. 지정된 캐릭터가 없으면 시점 캐릭터. 별칭 `{{groupNotMuted}}`. |
| `{{notChar}}` | 현재 등장하는 캐릭터들 중 시점 캐릭터를 제외한 목록(쉼표 또는 줄바꿈으로 구분되며, `, `로 이어 붙음). |
| `{{description}}` | 책의 설명. |
| `{{scenario}}` | 장면 설정. |
| `{{input}}` | 해당 부분의 지시문. |

SillyTavern의 마커 `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>`, `<CHARIFNOTGROUP>`도 (대소문자 무관) 동작합니다.

시점 캐릭터가 *Alice*이고 현재 등장 캐릭터가 *Alice, Bob, Carol*이면:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## 이야기 기록

| 매크로 | 결과 |
|---|---|
| `{{lastMessage}}` | 활성 분기의 마지막 부분의 텍스트. 별칭 `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | 마지막 부분의 위치(0부터 셈). |
| `{{lastUserMessage}}` | 마지막 부분의 지시문. |
| `{{summary}}` | 활성 분기의 가장 최근 [요약](../books/summaries.md). |
| `{{closest-message}}`, `{{closest-message::n}}` | 작성 중인 부분에 가장 가까운 부분: 마지막 부분, 또는 거기서 거슬러 센 *n*번째 부분(`::2`는 마지막 바로 앞 부분). 해당 부분이 없으면 비어 있음. 별칭 `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | 분기의 요약에 대해서도 같음: 가장 최근 것, 또는 거슬러 센 *n*번째 것. 별칭 `{{closestSummary}}`. |
| `{{current-message}}` | *다시 생성*이나 스와이프로 대체되는 부분의 텍스트. 새 부분이면 비어 있음. 별칭 `{{currentMessage}}`. |

책의 첫 부분에서는 이 매크로들이 비어 있습니다. *다시 생성*의 경우, "마지막 부분"은 다시 생성되는 부분 바로 앞의 부분입니다.

`closest-message`와 `closest-summary`의 숫자는 매크로일 수 있으므로 변수에서 가져올 수 있습니다:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}`는 끝에서 세 번째 부분입니다. 양의 정수가 아닌 숫자는 아무것도 가져오지 않습니다.
`closest-summary`는 분기 부분들의 요약을 세며, 여기에는 [메타 요약](../books/summaries.md)과 그것이 대신하는 요약들도 포함됩니다. `{{summary}}`는 `{{closest-summary}}`와 같습니다.

<a id="date-and-time"></a>

## 날짜와 시간

| 매크로 | 예시 결과 |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - UTC 오프셋에서의 시간(`UTC-5:30`도 동작함) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js 형식 토큰](https://momentjs.com/docs/#/displaying/format/). 대괄호(`[...]`) 안의 텍스트는 그대로 출력됩니다: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - 마지막 부분이 작성된 이후의 시간. 첫 부분이면 `just now`. 별칭 `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - 두 시각의 차이. `now`, 날짜(`2026-01-08`), ISO 시각을 사용할 수 있음. 알 수 없는 값이면 아무것도 출력되지 않음. |

시각은 Marginalia가 실행되는 컴퓨터나 서버의 시간대를 따르며, 항상 영어로 표시됩니다. [Docker 버전](../getting-started/docker-server.md)에서는 `TZ` 환경 변수로 시간대를 설정하세요. 예: `TZ=Europe/Prague`.

<a id="variables"></a>

## 변수

변수는 부분들 사이에 값을 저장합니다: 카운터, 캐릭터의 기분, 이야기에서 정해진 사항 등. 변수에는 두 종류가 있습니다:

- **로컬(local)** 변수는 이야기 분기에 속합니다: 각 부분이 그 값을 기억하고, 새 부분은 바로 앞 부분의 값으로 시작합니다;
- **전역(global)** 변수는 책에 속하며, 모든 분기가 공유합니다.

자세한 내용은 [부분 사이에 유지되는 것](templates.md#what-is-kept-between-parts)을 참조하세요.

| 로컬 | 전역 | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | 값. 설정되지 않았으면 비어 있음. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | 값을 설정합니다. 아무것도 출력하지 않음. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | 숫자를 더합니다. 어느 한쪽이라도 숫자가 아니면 텍스트를 덧붙입니다. 아무것도 출력하지 않음. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | 1을 더하고 새 값을 출력합니다. 숫자가 아닌 값은 0부터 시작합니다. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | 1을 빼고 새 값을 출력합니다. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` 또는 `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | 변수를 제거합니다. 별칭 `flushvar` / `flushglobalvar`. |

숫자는 끝의 0 없이 출력됩니다: `1.50`에 `1.5`를 더하면 `3`입니다.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### 축약형

`{{.name}}`은 로컬 변수, `{{$name}}`은 전역 변수입니다. 축약형만 단독으로 쓰면 값을 출력합니다. 연산자와 함께 쓰면:

| 축약형 | 효과 | 출력 |
|---|---|---|
| `{{.hp = 20}}` | 값을 설정 | 없음 |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | 더하기 / 빼기 (`+=`는 텍스트에 텍스트를 덧붙임) | 없음 |
| `{{.turn++}}`, `{{.turn--}}` | 1을 더하기 / 빼기 | 새 값 |
| `{{.hp == 17}}`, `{{.hp != 17}}` | 비교 | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | 숫자를 비교 (텍스트이면 `false`) | `true` / `false` |

대체값과 기본값:

- `{{.name || Guest}}`는 변수가 비어 있거나 거짓 값(`0`, `false`, `no`, `off`)이면 *Guest*를 출력합니다;
- `{{.name ?? Guest}}`는 변수가 아예 설정되지 않은 경우에만 *Guest*를 출력합니다;
- `{{.title ||= Wanderer}}`는 변수가 비어 있거나 거짓 값이면 *Wanderer*로 설정한 다음 출력합니다;
- `{{.title ??= Wanderer}}`는 변수가 아예 설정되지 않은 경우에만 설정한 다음 출력합니다.

값에는 매크로를 넣을 수 있습니다: `{{.log += -> {{user}}}}`.

예를 들어, 사용자 프롬프트 맨 위에 부분 카운터를 둘 수 있습니다(`+=`는 아무것도 출력하지 않고, `{{trim}}`은 그 때문에 남는 빈 줄을 제거합니다):

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## 조건문

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

조건은 다음이 될 수 있습니다:

| 조건 | 참인 경우 |
|---|---|
| 매크로 이름 - `{{if user}}` | 매크로가 값을 내놓을 때 |
| 변수 - `{{if .mood}}`, `{{if $chapter}}` | 변수에 값이 있을 때 |
| 템플릿 변수 - `{{if sceneSetting}}` | 변수에 값이 있을 때 |
| 비교 - `{{if .hp > 20}}` | 비교가 참일 때 |
| 아무 텍스트 - `{{if yes}}` | 거짓 값을 제외하고 항상 |
| 부정 - `{{if !user}}`, `{{if !.mood}}` | 그 반대일 때 |

거짓으로 취급되는 값은 빈 값과 `0`, `false`, `no`, `off`(대소문자 무관)입니다. 그 밖의 모든 값은 참입니다.

- 짧은 형식 `{{if user::text}}`는 조건이 참일 때 *text*를 출력합니다.
- 실행되는 분기만 평가됩니다: 다른 분기의 `{{setvar}}`는 실행되지 않습니다.
- `{{if}}` 블록은 범위 지정 매크로처럼 들여쓰기가 제거되고 앞뒤 공백이 잘리므로, 가독성을 위해 들여쓸 수 있습니다. `{{#if}}`([Handlebars의 if](templates.md#conditions))는 공백을 그대로 유지합니다.
- `{{/if}}`가 없는 `{{if}}`는 텍스트로 출력됩니다.
<a id="expressions"></a>

## 표현식

`{{eval::expression}}`은 `true` 또는 `false`를 반환합니다. `{{if}}`로는 할 수 없는 비교가 필요할 때 사용하세요. 변수뿐 아니라 템플릿의 값에 대해서도 비교할 수 있으며, `and`, `or`, `not`으로 연결할 수 있습니다:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| 표현식에서 | 의미 |
|---|---|
| `"text"`, `'text'` | 텍스트. **텍스트는 항상 따옴표로 묶어야 합니다**: 따옴표 없는 단어는 이름으로 취급됩니다. |
| `12`, `-3.5`, `true`, `false` | 숫자 또는 참/거짓 값. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | `{{povCharacter}}`에서와 같은 템플릿의 값. 인수가 없는 매크로도 사용할 수 있습니다(`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | [변수](#variables)의 지역 또는 전역 값이며, 설정되지 않았다면 비어 있습니다. |
| `( )` | 묶기. |

비교:

| 연산자 | 참이 되는 경우 |
|---|---|
| `==`, `!=` | 같음, 다름. 숫자는 값으로 비교하고(`5 == 5.0`), 그 밖의 값은 정확히 비교하므로 `"Fiora" == "fiora"`는 거짓입니다. |
| `~=` | 대소문자를 무시하고 같음. |
| `<`, `<=`, `>`, `>=` | 숫자가 해당 순서에 있을 때. 한쪽이라도 숫자가 아니면 거짓입니다. |
| `contains` | 왼쪽 텍스트 안에 오른쪽 텍스트가 포함될 때(대소문자 무시). 예를 들어 `presentCharacters contains "ma"`는 *Mara*에 대해서도 참입니다. |
| `has` | 왼쪽의 목록(쉼표, 세미콜론 또는 줄바꿈으로 구분)에 오른쪽 항목이 있을 때(대소문자와 항목 주변의 공백은 무시). `presentCharacters has "Mara"`는 *Marati*에 대해서는 참이 아닙니다. |

결합: `and`(`&&`), `or`(`||`), `not`(`!`). `not`이 가장 먼저 적용되고, 다음은 `and`, 그다음은 `or`입니다. 단독으로 쓴 값은 비어 있거나, `0`, `false`, `no`, `off`가 아니면 참입니다. 텍스트 안에는 매크로를 사용할 수 있으며(`"{{user}}"`), 매크로가 먼저 계산됩니다.

올바르지 않은 표현식은 `Error`를 출력합니다. 구문 오류, 이름이 아닌 따옴표 없는 단어(`povCharacter == Fiora`), 알 수 없는 이름이 여기에 해당합니다. 모든 이름은 검사되며, 도달하지 않을 `and` / `or`의 일부에 있더라도 마찬가지이므로 오타가 숨겨지지 않습니다. [로어북 조건](../lorebooks/entries.md#a-condition-for-an-entry)에서 `Error`가 나오면 해당 항목은 사용되지 않으며, 미리보기에 이 사실이 표시됩니다.

<a id="randomness"></a>

## 무작위성

| 매크로 | 결과 |
|---|---|
| `{{random::red::green::blue}}` | 선택지 중 하나이며, 매번 다릅니다. `{{random:red,green,blue}}`와 `{{random}}red, green{{/random}}`도 사용할 수 있습니다. |
| `{{pick::sword::axe::bow}}` | 선택지 중 하나이지만, 같은 책의 같은 템플릿 위치에서는 항상 **같은 선택지**가 나옵니다. 무작위로 고른 이름처럼 일관성을 유지해야 하는 경우에 사용하세요. |
| `{{roll::2d6+3}}` | 주사위 굴림: `1d20`, `3d4-10`, `d100`. `{{roll d100}}`, `{{roll:6}}`(6면체 주사위 한 개)도 사용할 수 있습니다. 잘못된 굴림은 아무것도 출력하지 않습니다. |

<a id="generation-info"></a>

## 생성 정보

| 매크로 | 결과 |
|---|---|
| `{{model}}` | 책의 [추론 제공자](../inference-providers.md) 이름. |
| `{{maxContextTokens}}` | 토큰 단위의 컨텍스트 한도([제한](../protocols.md#limits) 참조, 프로토콜 또는 제공자 기준). 별칭은 `{{maxContext}}`입니다. |
| `{{maxResponseTokens}}` | 토큰 단위의 응답 한도. 별칭은 `{{maxResponse}}`입니다. |
| `{{maxPrompt}}` | 컨텍스트에서 응답을 뺀 값: 프롬프트에 쓸 수 있는 공간. |
| `{{lastGenerationType}}` | 새 파트는 `normal`, `regenerate`, `swipe`, 요약에서는 `quiet`. |

<a id="commands"></a>

## 명령

| 매크로 | 결과 |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | [명령](../commands/index.md)이 텍스트로 응답하는 내용. 매크로용으로 만들어진 명령만 사용할 수 있습니다(읽기만 하며, 확장 기능이 이를 명시합니다. 예: [Character Tracker](../extensions/character-tracker.md)의 `{{command::character-tracker-map}}`). 그 밖의 이름은 `Error`를 출력합니다. 실패한 명령은 아무것도 출력하지 않습니다. |

명령은 생성 과정이 매크로에 도달하면 실행됩니다(선택되지 않은 `{{if}}` 분기에 있으면 전혀 실행되지 않습니다). 실행 대상은 생성 중인 책이며, 생성의 프롬프트(사용자 프롬프트, 로어북 항목, 마스터 템플릿)에서만 실행됩니다. 그 밖의 곳에서는 매크로가 아무것도 출력하지 않습니다. 응답은 새 파트가 이어 쓰는 파트와 함께 보관됩니다. 같은 파트에서 이어서 생성하거나 같은 생성의 다른 템플릿은 명령을 다시 실행하는 대신 이 응답을 사용합니다. **다시 생성**과 **swipe**는 명령을 다시 요청하며, 새 파트는 새로운 마지막 파트에서 이어지므로 모든 새 파트도 마찬가지입니다.

<a id="utility"></a>

## 유틸리티

| 매크로 | 결과 |
|---|---|
| `{{newline}}`, `{{newline::2}}` | 한 개 이상의 줄바꿈. |
| `{{space}}`, `{{space::4}}` | 한 개 이상의 공백. |
| `{{trim}}` | 주변의 줄바꿈을 제거합니다. 변수만 설정하는 줄 뒤에 유용합니다. |
| `{{noop}}` | 없음. |
| `{{join::separator::a::b::c}}` | 첫 번째 인수 다음의 텍스트들을 개수에 관계없이 첫 번째 인수를 구분자로 하여 이어 붙입니다(매크로 포함): `{{join::-::{{user}}::{{scenario}}}}`. 빈 텍스트는 제외되므로, 선택적 매크로가 있어도 불필요한 구분자가 남지 않습니다. 직접 쓴 인수 주변의 공백은 제거되므로, 공백이 포함된 구분자는 매크로를 사용하세요: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | 뒤집힌 텍스트. |
| `{{// comment}}` | 주석이며 제거됩니다. `{{//}}` ... `{{///}}`는 그 사이의 모든 내용을 매크로를 포함해 주석 처리합니다. |
| `\{\{user\}\}` | `{{user}}`를 그대로 출력합니다. |

<a id="sillytavern-only-macros"></a>

## SillyTavern 전용 매크로

SillyTavern 채팅에서만 의미가 있는 매크로 - 페르소나와 캐릭터 카드 필드(`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), 인스트럭트 모드 시퀀스(`{{instructUserPrefix}}`...), 작성자 노트, swipe ID, `{{outlet}}` 등 - 는 인식되며 **아무것도 출력하지 않습니다**. 그래서 가져온 월드 인포가 프롬프트에 이 매크로를 노출하지 않습니다. `{{isMobile}}`과 `{{hasExtension}}`은 `false`를 출력합니다.

Marginalia가 알지 못하는 매크로는 다음과 같이 처리됩니다:

- 인수가 있는 경우(`{{fooBar::x}}`) SillyTavern과 같이 그대로 출력됩니다;
- 인수가 없는 경우(`{{fooBar}}`) 알 수 없는 [템플릿 변수](templates.md#typos)와 같이 **`Error`**로 출력됩니다.

}%}
