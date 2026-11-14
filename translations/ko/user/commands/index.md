---
label: 명령
order: 945
icon: terminal
---

# 명령

명령은 메뉴를 클릭하며 찾아가는 대신 직접 입력해서 실행하는 작은 동작입니다. 이야기나 로어북에서 무언가를 찾아보거나, 검색 및 바꾸기를 하거나, 백업을 만드는 데 쓰입니다. [확장 기능](../extensions/index.md)은 자체 명령을 추가합니다. 예를 들어 로어 항목을 생성하거나 이름을 지어내는 명령이 있습니다. [에이전트](../agents.md)도 같은 명령으로 작업하므로, 각 명령은 분명한 한 가지 일을 하고 무엇을 하는지 입력하면서 바로 읽을 수 있습니다.

**명령줄**은 [스토리 편집기](../books/story-editor.md)에서 메뉴 아래, 이야기 바로 아래에 있는 입력란입니다.

<a id="running-a-command"></a>

## 명령 실행하기

1. 명령줄을 클릭하고 `/`를 입력합니다. 목록에 모든 명령이 이름과 짧은 설명과 함께 나타납니다. 글자를 더 입력하면 목록이 좁혀집니다. 필터는 명령의 이름, 제목, 설명을 모두 확인합니다.
2. 마우스로 명령을 고르거나, 화살표 키와 **Enter**로 고릅니다. 인수가 필요 없는 명령은 바로 실행됩니다. 인수가 필요한 명령이라면 `/name `가 입력란에 써지고, 이어서 인수를 입력하면 됩니다.
![슬래시를 입력해서 열린 명령 목록](../../images/command-list.png)

3. 인수를 입력하고 **Enter**를 누르거나, 입력란 옆의 재생 버튼을 클릭해서 실행합니다. 명령이 시작되면 입력란이 바로 비워집니다. 명령이 아닌 텍스트는 그대로 남으므로 고칠 수 있습니다.

`/`로 시작하지 않는 텍스트는 명령이 아니며, 아무것도 실행되지 않습니다. 입력란에서 벗어나거나 **Esc**를 눌러도 아무것도 실행되지 않으므로, 실수로 명령이 시작되는 일은 없습니다.

<a id="the-help"></a>

## 도움말

입력하는 동안 입력란 위에 작은 창이 열립니다. 이 창은 이야기의 일부를 가리지만 아무것도 움직이지 않으며, 입력란을 떠나거나 명령을 실행하면 닫힙니다. 창에는 다음이 표시됩니다.

- 명령이 하는 일,
- 지금까지 입력한 내용에 맞는 명령의 형식([형식](#forms-of-a-command) 참고). 지금 입력 중인 인수는 강조 표시되고 그 설명이 함께 나옵니다,
- 명령이 돌려주는 결과,
- 명령이 받는 인수보다 더 많이 입력했을 때, 또는 그런 이름의 명령이 없을 때 알려 주는 힌트.

![두 번째 인수를 입력하는 동안의 /search-replace-lore-entries 도움말](../../images/command-help.png)

이름을 아직 입력하는 중이라면 명령 목록이 곧 도움말입니다.

<a id="arguments"></a>

## 인수

인수는 공백으로 구분되며, 공백이 여러 개 연달아 있으면 하나로 칩니다. 인수 *안에* 공백을 넣으려면 세 가지 방법이 있습니다.

| 입력한 내용                    | 명령이 받는 값                 | 이유                                       |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | 인수 하나, `two words`       | `\ `(역슬래시, 공백)는 공백입니다         |
| `/search-parts "two words"`  | 인수 하나, `two words`       | 큰따옴표는 그 안의 모든 것을 한데 묶습니다 |
| `/search-parts say\ "hi"`    | 인수 하나, `say hi`          | 따옴표는 인수 중간에서 시작할 수 있습니다 |

그 밖의 특수 문자는 다음과 같습니다.

| 입력한 내용                      | 받는 값                    |
|-------------------------------|----------------------------|
| `\\`                          | 역슬래시                   |
| `\"`                          | 큰따옴표. 따옴표로 감싸든 아니든 같습니다: `"say \"hi\""`는 `say "hi"`입니다 |
| `""`                          | 빈 인수                    |

다른 것 앞에 붙은 역슬래시는 그대로 남으므로, `C:\books\story`는 변경 없이 그대로 전달됩니다. 따옴표를 열고 닫지 않으면 줄 끝까지 모든 내용이 그 인수에 속합니다.

인수는 텍스트입니다. 숫자, 예/아니요, 또는 몇 가지 선택지 중 하나를 요구하는 명령은 텍스트를 스스로 변환합니다. 텍스트가 맞지 않으면 명령은 실행되지 않고 알림이 그 사실을 알려 줍니다.

<a id="forms-of-a-command"></a>

## 명령의 형식

하나의 명령이 서로 다른 인수를 받을 수 있습니다. `/list-parts`처럼 인수가 없는 경우와 `/list-parts 2`처럼 인수가 하나인 경우는 같은 명령의 두 **형식**입니다. Marginalia는 입력한 인수의 개수와 그 모양을 보고 형식을 고릅니다. 맞는 형식이 없으면, 명령이 이 인수를 받지 않는다는 알림이 나오고, 도움말에 그 명령이 가진 형식들이 표시됩니다.

<a id="the-result"></a>

## 결과

명령이 무언가를 돌려주면, 명령줄 위의 창에 그 결과가 표시됩니다. 닫기 버튼(×), **Esc**로 닫거나, 그냥 계속 작업하면 됩니다. 새로 무언가를 입력하거나 다른 명령을 실행하면 사라집니다. 아무것도 돌려주지 않는 명령은 창을 띄우지 않습니다.

![명령줄 위에 표시된 /take-backup의 결과](../../images/command-result.png)

명령이 실패하거나 그런 명령이 없으면, 이유와 함께 알림이 나타납니다.

<a id="commands-that-take-time"></a>

## 시간이 걸리는 명령

대부분의 명령은 바로 응답합니다. 시간이 필요한 명령은 움직이는 막대와 **중지** 버튼이 있는 창을 띄웁니다. 이 창은 옆에 자리 잡고 있어 페이지는 계속 쓸 수 있습니다. 창의 ⌄를 누르면 [실행 목록 패널](../books/story-editor.md#the-panel-of-the-runs)로 접히며, 거기에는 모델이 쓰고 있는 내용과 함께 해당 명령이 모든 페이지에 걸쳐 나열됩니다. 막대 아래에서 명령은 지금 무엇을 하고 있는지 알려 줄 수 있고, 더 작은 글씨로 단계의 세부 내용도 보여 줄 수 있습니다. **중지**는 명령을 취소하며, 즉시 반환되어야 합니다. 모델에게 요약이나 메모를 쓰게 하는 명령, 예를 들어 `/generate-annotation`가 이런 종류입니다.

이야기의 명령줄에 입력한 `/next`, `/swipe`, `/regenerate`에는 이런 창이 없습니다. 새 부분은 둥근 버튼을 누를 때처럼 이야기에 스트리밍되며, 둥근 버튼으로 멈출 수 있고, 이야기는 다른 생성과 똑같이 보류됩니다(로어, 다른 부분, 옆 탭에서는 계속 작업할 수 있습니다). 부분이 끝나면 명령의 결과가 입력줄 위에 나타납니다. 그때까지 입력줄은 이런 명령을 하나 더 받지 않습니다(텍스트는 그대로 남으므로 나중에 실행할 수 있습니다). 다른 명령은 계속 작동합니다.

![실행 중인 명령의 창](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## 데이터를 바꾸는 명령

각 명령은 이미 있는 데이터에 무엇을 하는지 밝힙니다. 명령줄은 이를 명령 옆에 작은 표시로 보여 주며, 목록과 도움말 모두에 나타납니다.

- **파괴적** (빨간색) - 명령이 존재하는 데이터를 바꾸거나 제거합니다. 책 전체를 다시 쓰는 검색 및 바꾸기 명령이 그렇습니다. 표시 위에 커서를 올리면 설명을 볼 수 있습니다.
- **비파괴적** (초록색) - 명령이 데이터를 읽기만 하거나 새 데이터를 추가만 합니다. 예를 들어 `/take-backup`는 백업을 추가할 뿐 다른 것은 바꾸지 않습니다.

이 표시는 여러분을 위한 정보입니다. 명령을 실행할 때 명령을 막지는 않습니다. [에이전트](../agents.md)는 이 표시를 기준으로 판단합니다. 즉, 파괴적 명령을 받지 않거나, 먼저 묻거나, 그 에이전트에 설정된 대로 실행합니다. 스스로 확인을 요청하는 명령은 아래에 설명합니다.

<a id="commands-that-ask-first"></a>

## 먼저 묻는 명령

많이 바꾸는 명령은 실행하기 전에 물어볼 수 있습니다. 검색 및 바꾸기 명령에는 미리보기가 없으므로, 검색이 끝나면 몇 개 항목에서 몇 개의 일치를 찾았는지, 무엇을 무엇으로 바꿀지를 보여 줍니다. 이후 **예**를 눌러야만 책이 바뀌며, **아니요**를 누르면 모든 것이 그대로 남습니다. 질문은 답할 때까지 남아 있습니다.

![/search-replace-parts의 질문](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Marginalia에 포함된 명령어

| 명령어                              | 기능                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 열린 책의 이야기에서 찾기 및 바꾸기를 합니다. 이는 이야기 탭의 **찾기 및 바꾸기** 창과 같습니다. 옵션은 `true`/`false` 순서이며, 생략한 옵션은 해당 창의 기본값을 따릅니다([정규식](../regular-expressions.md) 아니요, 대소문자 구분 예, 요약 예, 모든 분기 아니요, 재검증 예). 옵션 하나를 설정하려면 그 앞의 옵션도 함께 지정해야 합니다. `maxHits`는 찾은 일치 항목이 그보다 많으면 바꾸기를 거부합니다. `dryRun` `true`와 함께 사용하면 아무것도 바뀌지 않습니다. 응답은 일치 항목 수를 세고 몇 개를 보여 주며, 에이전트가 바꾸기 전에 확인하는 방식입니다. 미리보기는 없습니다. 검색 후 찾은 일치 항목 수만큼 바꿀지 묻고, **예**를 선택해야만 책이 바뀝니다. 바꾸기는 중단할 수 없습니다. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 열린 책의 로어북 항목(이름, 텍스트, 주석)에 대해 같은 작업을 합니다. |
| `/take-backup`                       | 열린 책의 백업을 만듭니다. 백업은 책의 **백업** 탭에 나타납니다. 열린 책이 없으면 열린 책이 없다고 알려 줍니다. |
| `/get-book-info` | 열린 책의 이름, 설정, 모델, 총계(파트, 단어, 토큰)와 태그를 보여 줍니다. |
| `/list-parts [branch] [offset] [limit]` | 분기의 파트를 보여 줍니다. 핸들, 위치, 크기, 스와이프, 요약 상태와 짧은 미리보기를 포함합니다. |
| `/get-part <part> [branch] [offset] [length] [field]` | 파트의 텍스트를 여러 조각으로 나누어 보여 주며(또는 `instructions`, `scene`, `pov`, `present` 또는 `reasoning`), 그 파트가 무엇으로 만들어졌는지도 보여 줍니다. |
| `/list-swipes <part> [branch]` | 파트의 대안(스와이프)을 각각을 보여 주는 분기 번호와 함께 보여 줍니다. |
| `/list-branches [offset] [limit]` | 책의 분기를 번호와 함께 보여 주며, 각 분기가 다른 분기와 어디에서 갈라지는지도 보여 줍니다. |
| `/get-part-metrics <part> [branch]` | 파트의 토큰, 소요 시간, 모델 및 프로토콜을 보여 줍니다. |
| `/show-prompt <part> [branch] [offset] [length]` | 생성된 파트를 만들 때 사용한 프롬프트를 저장된 그대로 보여 줍니다. |
| `/search-parts <query> [limit]` | 모든 분기의 파트에서 전문 검색을 하고, 발췌문을 보여 줍니다. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 이름이나 단어가 얼마나 자주, 어디에 나타나는지 보여 줍니다(기본값: 정규식 사용 안 함, 대소문자 무시, 파트만, 활성 분기). |
| `/compare-parts <a> <b> [branchA] [branchB]` | 두 파트 사이에서 다른 문장을 보여 줍니다. `0` 명령은 `b`처럼 파트 `a`를 모델이 처음 생성한 텍스트와 비교합니다. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | 텍스트의 토큰 수(책의 모델 토크나이저 기준), 단어 수, 글자 수를 셉니다. 텍스트 대신 핸들을 주면 해당 파트를 셉니다. |
| `/list-summaries [branch]` | 분기의 요약, 요약이 다루는 파트, 그리고 요약이 아직 유효한지 여부를 보여 줍니다. |
| `/get-summary <part> [branch]` | 파트의 요약을 보여 줍니다. |
| `/check-summaries [branch]` | 오래된 요약과, 그 요약을 오래되게 만든 수정된 파트를 보여 줍니다. |
| `/get-template-variables <scope> [part] [branch]` | 책(`global`)의 또는 파트(`part`)의 템플릿 변수를 보여 줍니다. |
| `/list-lorebooks` | 열린 책의 로어북(루트와 하위 로어북)을 번호, 항목 수, 태그, 하위 로어북과 함께 보여 줍니다. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | 로어북의 항목을 설정과 미리보기와 함께 보여 줍니다. 필터는 이름이나 태그에 그 문자열이 포함된 항목만 남깁니다. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 항목 하나를 보여 줍니다. 설정, 키, 활성화(재귀, 컨텍스트, 파트 범위, 조건), 태그, 주석과 텍스트 조각을 포함합니다. |
| `/search-lore <query> [regex] [limit]` | 책의 모든 로어북 중 이름, 텍스트 또는 주석에 검색어가 포함된 항목을 발췌문과 함께 보여 줍니다. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | 텍스트에 대해(또는 파트를 만든 것과 같은 턴에 대해) 생성 시 어떤 항목이 사용될지, 그리고 그 이유를 보여 줍니다. 필터가 없거나, 텍스트에서 필터가 발견되었거나(로어북에 [스캔 깊이](../lorebooks/entries.md#entries-brought-in-by-other-entries)가 있으면 다른 항목의 내용에서 발견된 경우 포함), 꺼져 있어 사용되지 않거나, 책의 태그가 결정하거나, 필터가 텍스트에 없는 경우를 구분합니다. 로어 텍스트와 그 토큰도 함께 보여 줍니다. 텍스트 자리에 파트의 핸들을 넣을 수 있습니다. |
| `/list-annotations [status [part [offset [limit]]]]` | 책 또는 파트의 [주석](../books/annotations.md)을 보여 줍니다. `status`가 `resolved`, `dismissed`, `orphaned` 또는 `all`라고 지정하지 않으면 열려 있는 주석을 보여 줍니다. |
| `/get-annotation <annotation>` | 주석 하나를 그 구절 주변의 텍스트와 함께 전부 보여 줍니다. |
| `/get-book-statistics [range] [side]` | 열린 책에 대해 날마다 무엇을 했는지 보여 줍니다(토큰, 단어, 파트, 편집, 시간, 크기). 자세한 내용은 [글쓰기 통계](../books/statistics.md#commands)를 참고하세요. |
| `/get-statistics [range] [by] [side]` | 모든 책에 대해 같은 내용을 날마다 또는 책별로 보여 줍니다. 열린 책이 필요하지 않습니다. |

<a id="reading-the-story"></a>

### 이야기 읽기

책을 읽는 위의 명령어(`/get-book-info`부터 `/get-template-variables`까지)는 아무것도 바꾸지 않으며, JSON 텍스트로 응답하므로 에이전트도 사용할 수 있습니다. 응답의 모든 행에는 **`handle`**가 있습니다. 이는 파트(또는 로어북, 항목)의 식별자이며, 파트를 추가하거나 옮기거나 삭제해도 바뀌지 않습니다. 파트는 핸들로, 또는 분기 위의 **위치**로 지정할 수 있으며, 이야기의 첫 파트는 `1`입니다. 분기는 그 분기의 어떤 파트의 핸들로, 또는 `/list-branches`부터 시작하는 **번호**로 지정합니다(번호는 분기 끝이 만들어진 순서를 따르므로, 분기를 추가하거나 삭제하면 바뀝니다). 위치와 번호는 입력하기 쉽고, 에이전트는 핸들을 사용해야 합니다. 이야기를 바꾸는 명령어는 핸들만 받습니다. 분기를 지정하지 않으면 명령어는 활성 분기에서 동작합니다. 목록은 `offset`와 `limit`로 나누어 보여 주며, 긴 텍스트는 조각(`offset`, `length`)으로 잘리고, 응답에는 전체 길이가 표시됩니다. 다른 명령어와 마찬가지로 옵션은 위치로 구분되므로, 옵션 하나를 설정하려면 그 앞의 옵션도 함께 지정해야 합니다.

로어북을 읽는 명령어(`/list-lorebooks`부터 `/preview-lore-activation`까지)도 같은 방식으로 동작하며, 열린 책의 로어북, 즉 루트 로어북과 그 하위 로어북에만 접근합니다. 로어북은 **핸들**이나 `/list-lorebooks`부터 시작하는 **번호**로 지정합니다(루트는 `1`입니다). 항목은 **핸들**이나 해당 로어북 안의 **항목 번호**로 지정하며, 번호는 로어북의 순서를 따릅니다(첫 번째 항목은 `1`입니다). 항목이나 하위 로어북을 추가하거나 제거하면 번호가 바뀝니다. 로어북을 지정하지 않으면 루트 로어북을 사용합니다.
<a id="changing-the-story-and-the-lore"></a>

### story와 lore 변경하기

이 명령들은 열린 책(또는 에이전트의 작업 사본, 에이전트의 변경은 여기에 쌓입니다)을 바꿉니다. 파트, 로어북, 항목은 언제나 위치나 번호가 아니라 **핸들**로 지정합니다. 무언가가 추가되거나 삭제되면 위치가 바뀌기 때문입니다. 읽을 때는 맞았던 위치가 쓰는 시점에는 다른 파트를 가리킬 수도 있습니다. 옵션은 여전히 위치 기반입니다. 텍스트나 데이터를 제거하는 명령은 `DESTRUCTIVE`이며 확인을 요청합니다(에이전트는 대신 파괴적 모드가 적용됩니다). 추가와 변경은 확인을 요청하지 않습니다. 이야기를 편집하면 요약이 낡을 수 있습니다(요약에는 그것이 설명하는 이야기의 해시가 담깁니다). 답변에는 낡은 요약 목록과 그 처리 방법이 나옵니다. 여전히 맞으면 `/revalidate-summaries`, 맞지 않으면 `/regenerate-summary`를 사용하세요.

| 명령 | 하는 일 |
|------|---------|
| `/edit-part <part> <old> <new> [field]` | 파트 텍스트에서 `old`를 `new`로 바꿉니다. `old`는 정확히 한 번 발견되어야 하며, 그렇지 않으면 아무것도 바뀌지 않고 오류가 발견된 횟수를 알려 줍니다. `field`는 대신 턴의 `instructions`, `scene`, `pov` 또는 `present` 글자를 편집합니다. |
| `/replace-part-text <part> <text> [field]` | 파트의 전체 텍스트를 바꿉니다. 먼저 확인을 요청합니다. |
| `/append-to-part <part> <text> [separator]` | 파트의 끝에 텍스트를 추가합니다. `separator`가 다르게 지정하지 않으면 빈 줄 뒤에 붙습니다. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | 파트를 무엇으로 만들었는지를 바꿉니다. 새로 생성할 때 이 값이 재사용됩니다. 빈 텍스트는 필드를 비우고, 생략한 필드는 그대로 둡니다. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 활성 분기의 마지막 파트 뒤, 또는 `parent` 뒤에 새 파트를 씁니다(새 분기가 됩니다). 새 파트는 활성 분기의 끝이 됩니다. |
| `/insert-part <text> <part> [before]` | `part` 바로 뒤에 새 파트를 이야기에 삽입합니다(`before`가 `true`이면 바로 앞에 삽입합니다). 그 자리 뒤에 있던 모든 것, 모든 분기를 포함해서, 이제 새 파트 뒤로 밀려납니다. 아무것도 삭제되거나 다시 만들어지지 않으므로 다른 파트들은 요약과 데이터를 그대로 유지하고 위치가 한 칸씩 뒤로 밀립니다. 그 자리를 덮고 있던 요약은 낡게 됩니다(답변에 어떤 요약인지 나옵니다). 분기의 끝이라면 `/create-part`와 같습니다. |
| `/branch-part <part>` | 파트(와 그 요약)를 바로 옆에 복사합니다. 분기를 시작하는 새 스와이프가 되며, 복사본이 활성 끝이 됩니다. |
| `/delete-part <part>` | 파트와 그 요약을 삭제합니다. 그 뒤에 있던 파트들은 그대로 남습니다. 먼저 확인을 요청합니다. |
| `/switch-branch <part>` | 파트를 거치는 분기를 활성 분기로 만듭니다. |
| `/set-summary <part> <text>` | 파트의 요약 텍스트를 바꿉니다. |
| `/remove-summary <part> [unwind]` | 파트의 요약을 제거합니다(`unwind`는 메타 요약이 대체했던 내용을 되돌립니다). 먼저 확인을 요청합니다. |
| `/revalidate-summaries [part]` | 낡은 요약들이 여전히 이야기를 설명한다고 표시합니다(활성 분기 전체 또는 그 파트의 요약만). 직접 확인했을 때만 사용하세요. |
| `/regenerate-summary <part>` | 모델이 파트의 요약을 다시 만들게 합니다. 느리고 토큰 비용이 듭니다. |
| `/create-meta-summary <from> <to>` | 모델이 파트 `from`(가장 최근)부터 `to`까지의 요약들을 하나로 병합하게 합니다. 느리고 토큰 비용이 듭니다. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | **New turn** 버튼처럼 모델이 다음 파트를 쓰게 합니다. 느리고 토큰 비용이 듭니다. 한 책의 생성은 한 번에 하나만 실행되며 나머지는 기다립니다. `profile`는 에이전트의 모델 프로필 중 하나를 지정합니다. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | 모델이 분기의 마지막 파트의 다른 버전을 쓰게 합니다. 빠뜨린 항목은 파트에서 가져옵니다. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | 모델이 분기의 마지막 파트를 다시 쓰게 하고 그 텍스트를 교체합니다. 먼저 확인을 요청합니다. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | 모델이 여러분의 지시에 따라 임의의 파트를 다시 쓰게 하고 그 텍스트를 교체합니다(참조: [파트 다시 쓰기](../books/story-editor.md#redrafting-a-part)). `prompt`는 책의 것 대신 사용할 다시 쓰기 프롬프트이고, `profile`는 에이전트의 모델 프로필입니다. `apply` `false`는 텍스트만 돌려줍니다. `DESTRUCTIVE`; 먼저 확인을 요청합니다. 느리고 토큰 비용이 듭니다. |
| `/impersonate [instructions] [outline] [prompt]` | 모델이 여러분을 대신해 다음 턴의 지시문을 작성하고 그 텍스트를 돌려줍니다(참조: [지시문 작성하기](../books/story-editor.md#drafting-the-instructions)). 아무것도 바뀌지 않습니다. `instructions`는 파트가 무엇에 관한 것인지입니다(비우면 이야기가 이어지는 방향을 따릅니다). `outline`는 모델에게 주는 이야기 계획이고, `prompt`는 책의 것 대신 사용할 임퍼소네이트 프롬프트입니다. 명령 입력줄에서 여러분에게만 제공되며, [에이전트](../agents.md)에게는 제공되지 않습니다. 느리고 토큰 비용이 듭니다. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 책의 설명 설정을 바꿉니다. 여러분이 준 값만 바뀝니다. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 책의 프롬프트 템플릿을 바꿉니다. 각 템플릿은 먼저 검사됩니다. `DESTRUCTIVE`: 잘못된 템플릿은 이후의 모든 생성을 망칩니다. |
| `/set-book-tags [add] [remove]` | 책의 태그를 추가하고 제거합니다(쉼표로 구분). |
| `/set-book-history-limit [maxVersions]` | 책의 [기록](../books/version-history.md#how-many-versions-are-kept)이 파트마다 보관할 버전 수를 정합니다(`0`는 지금부터 아무것도 보관하지 않습니다). 숫자를 주지 않으면 책은 제한을 두지 않습니다. 폴더, 사용자, 관리자의 제한은 여전히 적용됩니다. 버전은 하나도 삭제하지 않습니다. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | 로어북에 항목을 추가합니다(지정하지 않으면 루트에 추가합니다. 로어북이 없는 책은 책 이름을 딴 로어북을 하나 받습니다). `keys`는 쉼표로 구분한 키워드 목록입니다. `filteringMode`는 `KEYWORDS_OR`(기본값), `KEYWORDS_AND`, `TEXT` 또는 `REGEX`입니다. 템플릿이나 정규 표현식의 문제는 저장된 뒤 보고됩니다. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 여러분이 준 필드를 바꿉니다. |
| `/delete-lore-entry <entry>` | 항목을 삭제합니다. 먼저 확인을 요청합니다. |
| `/move-lore-entry <entry> <lorebook>` | 항목을 책의 다른 로어북으로 옮깁니다. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | 항목의 태그와 부정 태그를 바꿉니다(쉼표로 구분). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | 항목이 언제 사용되는지를 바꿉니다: `preventRecursion`, `recursionOnly`, `activationSummaries`(`true` / `false`), `activationContext`(`INSTRUCTIONS` 또는 `INCLUDE_PARTS`)와 `activationParts`, 파트 범위 `fromMessage` / `toMessage`(0이면 제한을 없앱니다), 그리고 `condition` 템플릿(빈 텍스트이면 제거합니다). 빈 텍스트로 준 값은 그대로 둡니다(`condition`만 예외입니다). |
| `/create-lorebook <name> [parent]` | `parent`의 하위 책이 되는 로어북을 만듭니다(지정하지 않으면 루트의 하위가 됩니다. 로어북이 없는 책은 먼저 하나를 받습니다). |
| `/set-lorebook <lorebook> [name] [enabled]` | 로어북의 이름을 바꾸거나 켜고 끕니다. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | 로어북이 포함하는 로어북들을 바꿉니다. 로어북은 자기 자신을 포함할 수 없습니다. [에이전트](../agents.md)의 경우, `/add-subbook`는 이전에 삭제했거나 부모에서 빼낸 로어북을 (가졌던 핸들로 지정하여) 항목과 함께 되돌립니다. |
| `/delete-lorebook <lorebook>` | 항목과 함께 로어북을 삭제합니다(루트는 제외). 먼저 확인을 요청합니다. |
<a id="annotations"></a>

### 주석

[주석](../books/annotations.md)을 읽는 명령(`/list-annotations`, `/get-annotation`)은 다른 읽기 명령과 마찬가지로 JSON으로 답합니다. 주석은 **핸들**로 지정하고, 무언가를 바꾸는 명령에서는 부분을 그 핸들로 지정합니다. 이 명령들은 메모만 바꾸고 이야기는 바꾸지 않으므로, 삭제만 먼저 확인을 요청합니다. 에이전트는 이 명령을 모두 쓸 수 있으며, 에이전트의 메모는 [결과 검토](../books/annotations.md#annotations-and-agents)를 할 때 책에 반영됩니다.

| 명령 | 하는 일 |
|------|---------|
| `/create-annotation <part> <text> [quote [type]]` | 부분에 메모를 추가합니다. `quote`은 구절의 말이며, 부분에서 그대로 복사합니다. 메모는 그 구절에 붙어 있어서 글을 고칠 때 함께 따라갑니다. 인용이 없거나 본문에 없는 인용이면, 메모는 부분 전체에 대한 것이 됩니다(응답에 그렇다고 나옵니다). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 메모, 유형, 또는 모델이 메모를 받을지 여부를 바꿉니다. 빈 텍스트나 유형은 그대로 둡니다. |
| `/resolve-annotation <annotation> [status]` | 상태를 설정합니다: `resolved`(기본값), `dismissed` 또는 `open`. |
| `/reattach-annotation <annotation> [quote]` | 메모를 인용이 부분에서 찾는 구절로 옮깁니다. 인용이 없으면 메모는 부분 전체에 대한 것입니다. |
| `/delete-annotation <annotation>` | 메모를 삭제합니다(휴지통에서 복원할 수 있습니다). 먼저 확인을 요청합니다. |
| `/generate-annotation <part> [profile]` | 모델이 [Annotate with AI](../books/annotations.md#letting-a-model-annotate)처럼 부분을 검토하고, 찾은 것을 모두 메모로 추가합니다(출처는 *AI*이며, 본문에 없는 인용이면 부분 전체에 대한 메모가 됩니다). 확인을 요청하지 않습니다. 메모만 바뀌며, 메모는 해결하거나 고치거나 삭제할 수 있습니다. 책의 모델로 검토하며, *에이전트의 모델로 책 쓰기*가 켜져 있으면 에이전트는 자기 모델을 씁니다(로어 생성기 명령과 같습니다). `profile`는 에이전트의 모델 프로필입니다. 한 번에 실행되며, 시간이 걸리고 토큰을 씁니다. |

<a id="the-planner"></a>

### 플래너

책의 [플래너](../books/story-editor.md#the-planner-tab)를 다루는 명령입니다. 장면은 목록의 **번호**(1부터 시작, `/get-planned-scenes`이 번호를 보여 줍니다)로 지정합니다. 장면을 옮기거나 삭제하면 번호가 바뀌므로, 바꾼 뒤에는 목록을 다시 읽으세요. 이 명령들은 플래너만 바꾸고 이야기는 바꾸지 않습니다. 장면을 추가하고, 옮기고, 완료로 표시하는 것은 확인을 요청하지 않습니다. 장면의 텍스트를 바꾸거나 장면을 삭제하는 것은 파괴적입니다([에이전트](../agents.md)는 다른 파괴적 명령과 마찬가지로 확인을 요청받습니다).

| 명령 | 하는 일 |
|------|---------|
| `/get-planned-scenes` | 장면의 번호, 제목, 장면 배경, 시점 인물, 등장 인물, 지시문, 완료 여부를 보여 줍니다. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 목록 끝에 장면을 추가합니다. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 제목과 주어진 세부 사항을 바꿉니다. 빠진 것은 그대로 두고, 빈 텍스트는 지웁니다. |
| `/set-planned-scene-done <scene> [done]` | 장면을 집필 완료로(`done`의 기본값은 `true`) 또는 미집필로 표시합니다. |
| `/move-planned-scene <scene> <to>` | 장면을 `to` 위치로 옮깁니다(1은 첫 번째이고, 너무 크면 마지막 위치입니다). |
| `/delete-planned-scene <scene>` | 장면을 삭제합니다. |

모델에게 글을 쓰게 하거나 검토하게 하는 명령은 시간이 걸립니다. `/generate-annotation`과 두 요약은 **중지** 창을 보여 주고, `/next`, `/swipe`, `/regenerate`은 이야기 안으로 스트리밍됩니다([시간이 걸리는 명령](#commands-that-take-time) 참조).

<a id="commands-that-come-with-extensions"></a>

## 확장 기능과 함께 제공되는 명령

확장 기능이 로드되어 있는 동안 이 명령들이 목록에 보입니다. 각 명령은 해당 확장 기능의 문서에 설명되어 있습니다.

| 명령 | 확장 기능 | 하는 일 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 저장해 둔 생성 프롬프트, 또는 하나의 프롬프트 텍스트. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | 로어북의 로어로 로어 항목의 텍스트를 생성하고, (`compact`가 `false`이 아니면) 압축한 뒤 돌려줍니다. 아무것도 저장하지 않습니다. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 생성된 텍스트를 밀도 높은 로어 항목 텍스트로 압축해 돌려줍니다. 아무것도 저장하지 않습니다. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 지어낸 이름의 범주, 그리고 그중 한 범주의 이름. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 실제 이름 목록, 그리고 그중 한 목록에서 고른 이름. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 실제 장소의 유형, 이름·유형·국가로 하는 검색, 한 지점 주변의 장소, 두 장소 사이의 거리. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | 개요의 아크와 장. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | 아크를 추가, 변경, 이동, 삭제합니다(`<arc>`는 아크의 번호). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | 아크의 장에 대해서도 같은 기능을 합니다. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Arcs 탭의 생성기입니다. 결과를 돌려주며, `save`을 붙였을 때만 개요에 추가합니다. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 책의 등장인물, 그리고 메모와 관계가 담긴 한 인물. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 등장인물을 추가, 변경, 삭제합니다. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 등장인물 사이의 관계. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 책의 노트 페이지(어시스턴트에게 숨긴 페이지는 제외), 텍스트와 링크가 담긴 한 페이지, 그리고 검색. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | 페이지를 추가, 덧붙이기, 변경, 편집, 삭제합니다. `note-text`는 {%{`{{command::note-text::Title}}`}%} 매크로용입니다. |

로어 생성기, 이름 생성기, 이름 제공자, 장소 제공자의 명령은 읽기만 합니다. 생성하는 명령은 텍스트를 돌려주며, 그것을 항목에 넣는 일은 여러분이나 이 명령을 호출한 에이전트에게 맡겨집니다. 아크 개요와 등장인물 추적기의 명령은 열린 책의 개요와 등장인물도 바꿉니다. 이 중 `delete` 명령만 파괴적입니다. 스토리 노트도 열린 책의 페이지를 바꿉니다. 여기서는 `update-note`와 `edit-note`이 이미 있는 텍스트를 대체하므로 이것도 파괴적입니다.

<a id="limits"></a>

## 제한 사항

- 명령줄은 데스크톱 레이아웃의 이야기 탭에 있습니다. [휴대폰 보기](../phone.md)에는 아직 없습니다.
- 명령은 여러분의 세션에서 여러분으로서 실행되며, 열려 있는 책을 대상으로 작동합니다.
