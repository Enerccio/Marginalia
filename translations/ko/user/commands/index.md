---
label: 명령
order: 945
icon: terminal
---

# 명령

명령은 메뉴를 하나씩 클릭하는 대신, 직접 입력해서 실행하는 작은 동작입니다. 이야기나 로어북에서 무언가를 찾아보거나, 찾아 바꾸기를 하거나, 백업을 만드는 데 쓸 수 있습니다. [확장](../extensions/index.md)은 명령을 추가로 제공합니다. 예를 들어 로어 항목을 생성하거나 이름을 지어내는 명령이 있습니다. [에이전트](../agents.md)도 같은 명령을 사용하므로, 각 명령은 분명한 한 가지 일을 하고 무엇을 하는지 입력하는 동안 바로 읽을 수 있게 되어 있습니다.

**명령줄**은 [이야기 편집기](../books/story-editor.md)에서 이야기 아래 메뉴 옆에 있는 입력란입니다.

<a id="running-a-command"></a>

## 명령 실행하기

1. 명령줄을 클릭하고 `/`를 입력합니다. 목록에 모든 명령이 이름과 짧은 설명과 함께 나타납니다. 글자를 더 입력하면 목록이 좁혀집니다. 필터는 명령의 이름, 제목, 설명을 모두 확인합니다.
2. 마우스로 명령을 고르거나, 방향키와 **Enter**로 고릅니다. 인자가 필요 없는 명령은 바로 실행됩니다. 인자가 필요한 명령은 입력란에 `/이름 `이 써지고, 그 뒤에 인자를 이어서 입력합니다.
![슬래시를 입력하면 열리는 명령 목록](../../images/command-list.png)

3. 인자를 입력한 뒤 **Enter**를 누르거나, 입력란 옆의 재생 버튼을 클릭해 실행합니다. 명령이 시작되면 입력란이 바로 비워집니다. 명령이 아닌 텍스트는 그대로 남아 있으므로 고칠 수 있습니다.

`/`로 시작하지 않는 텍스트는 명령이 아니므로 아무것도 실행되지 않습니다. 입력란 밖을 클릭하거나 **Esc**를 눌러도 아무것도 실행되지 않으므로, 실수로 명령이 시작될 일은 없습니다.

<a id="the-help"></a>

## 도움말

입력하는 동안 입력란 위에 작은 창이 열립니다. 이 창은 이야기의 일부를 가리지만 아무것도 옮기지 않으며, 입력란을 떠나거나 명령을 실행하면 닫힙니다. 창에는 다음이 표시됩니다.

- 명령이 하는 일,
- 지금까지 입력한 내용에 맞는 명령의 형태([형태](#forms-of-a-command) 참고). 지금 입력 중인 인자는 강조 표시되고 설명이 함께 나옵니다,
- 명령이 돌려주는 결과,
- 명령이 받는 인자보다 더 많이 입력했을 때나, 그런 이름의 명령이 없을 때 나타나는 안내.

![두 번째 인자를 입력하는 중의 /search-replace-lore-entries 도움말](../../images/command-help.png)

이름을 아직 입력하는 중이라면 명령 목록이 곧 도움말입니다.

<a id="arguments"></a>

## 인자

인자는 공백으로 구분되며, 연속된 공백은 하나로 취급됩니다. 인자 *안에* 공백을 넣으려면 세 가지 방법이 있습니다.

| 입력         | 명령이 받는 값            | 이유                                          |
|--------------|---------------------------|-----------------------------------------------|
| `/search-parts two\ words`   | 인자 하나, `two words`     | `\ `(백슬래시, 공백)는 공백입니다              |
| `/search-parts "two words"`  | 인자 하나, `two words`     | 큰따옴표가 안의 내용을 하나로 묶습니다         |
| `/search-parts say\ "hi"`    | 인자 하나, `say hi`        | 따옴표는 인자의 중간에서 시작할 수 있습니다    |

그 밖의 특수 문자는 다음과 같습니다.

| 입력                          | 결과                       |
|-------------------------------|----------------------------|
| `\\`                          | 백슬래시                   |
| `\"`                          | 큰따옴표. 바깥에 따옴표가 있든 없든 같습니다. `"say \"hi\""`는 `say "hi"`가 됩니다 |
| `""`                          | 빈 인자                    |

그 밖의 문자 앞에 있는 백슬래시는 그대로 남습니다. 그래서 `C:\books\story`는 바뀌지 않고 전달됩니다. 따옴표를 열고 닫지 않으면, 줄 끝까지 모든 내용이 그 인자에 속합니다.

인자는 텍스트입니다. 숫자, 예/아니요, 또는 몇 가지 선택지 중 하나를 요구하는 명령은 텍스트를 스스로 변환합니다. 텍스트가 맞지 않으면 명령은 실행되지 않고 알림이 그 이유를 알려 줍니다.

<a id="forms-of-a-command"></a>

## 명령의 형태

하나의 명령은 서로 다른 인자를 받을 수 있습니다. 예를 들어 `/list-parts`(인자 없음)와 `/list-parts 2`(인자 하나)는 같은 명령의 두 가지 **형태**입니다. Marginalia는 입력한 인자의 개수와 그 모양을 보고 형태를 고릅니다. 맞는 형태가 없으면 명령이 이런 인자를 받지 않는다는 알림이 나오고, 도움말에는 그 명령이 가진 형태들이 표시됩니다.

<a id="the-result"></a>

## 결과

명령이 무언가를 돌려주면, 명령줄 위의 창에 결과가 표시됩니다. 엑스 버튼이나 **Esc**로 닫거나, 그냥 계속 작업해도 됩니다. 새로 무언가를 입력하거나 다른 명령을 실행하면 사라집니다. 아무것도 돌려주지 않는 명령은 창을 띄우지 않습니다.

![명령줄 위에 나타난 /take-backup의 결과](../../images/command-result.png)

명령이 실패하거나 그런 이름의 명령이 없으면, 이유가 적힌 알림이 나타납니다.

<a id="commands-that-take-time"></a>

## 시간이 걸리는 명령

대부분의 명령은 즉시 응답합니다. 시간이 필요한 명령은 움직이는 막대와 **중지** 버튼이 있는 창을 보여 줍니다. 이 창은 옆에 떠 있어 페이지를 계속 사용할 수 있습니다. 창의 ⌄를 누르면 [실행 패널](../books/story-editor.md#the-panel-of-the-runs)로 접히며, 거기에는 모델이 쓰고 있는 내용과 함께 그 명령이 모든 페이지에 걸쳐 목록으로 나타납니다. 막대 아래에는 명령이 지금 무엇을 하고 있는지, 그리고 작은 글씨로 현재 단계의 세부 내용이 표시될 수 있습니다. **중지**는 명령을 취소하며, 즉시 반환되어야 합니다. `/generate-annotation`처럼 모델이 요약이나 메모를 쓰도록 하는 명령이 이런 종류입니다.

이야기의 명령 막대에 입력한 `/next`, `/swipe`, `/regenerate`에는 이런 창이 없습니다. 새 부분은 둥근 버튼을 눌렀을 때처럼 이야기에 스트리밍되고, 둥근 버튼으로 멈출 수 있습니다. 그동안 이야기는 일반적인 생성과 같은 방식으로 잡혀 있습니다(로어, 다른 부분, 옆 탭에서는 계속 작업할 수 있습니다). 부분이 끝나면 명령의 결과가 막대 위에 나타납니다. 그 전까지 막대는 이런 명령을 하나 더 받지 않습니다(입력한 텍스트는 유지되며 나중에 실행할 수 있습니다). 다른 명령은 계속 동작합니다.

![실행 중인 명령의 창](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## 데이터를 바꾸는 명령

각 명령은 이미 있는 데이터에 무엇을 하는지 밝힙니다. 명령 막대는 이를 명령 옆의 작은 표시로 보여 주며, 목록과 도움말에 모두 나옵니다.

- **파괴적**(빨강) - 명령이 기존 데이터를 바꾸거나 제거합니다. 책 전체를 다시 쓰는 찾아 바꾸기 명령이 그렇습니다. 표시에 마우스를 올리면 설명을 볼 수 있습니다.
- **비파괴적**(초록) - 명령이 데이터를 읽기만 하거나 새 데이터를 추가하기만 합니다. 백업을 추가하고 다른 것은 바꾸지 않는 `/take-backup`이 그렇습니다.

이 표시는 여러분을 위한 정보일 뿐이며, 실행을 막지 않습니다. [에이전트](../agents.md)는 이 표시를 기준으로 행동합니다. 파괴적인 명령은 주어지지 않거나, 먼저 확인을 요청하거나, 그 에이전트에 설정된 대로 실행합니다. 스스로 확인을 요청하는 명령은 아래에서 설명합니다.

<a id="commands-that-ask-first"></a>

## 먼저 묻는 명령

많은 것을 바꾸는 명령은 실행 전에 물어볼 수 있습니다. 찾아 바꾸기 명령은 미리보기가 없으므로, 검색 후 몇 개 항목에서 몇 개의 일치가 발견되었는지, 무엇이 무엇으로 바뀔지를 보여 줍니다. 책을 바꾸려면 **예**를 눌러야 하며, **아니요**를 누르면 모든 것이 그대로 남습니다. 답하기 전까지 질문은 사라지지 않습니다.

![/search-replace-parts의 질문](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Marginalia에 포함된 명령

| 명령                              | 기능                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | 열린 책의 스토리에서 **찾기 및 바꾸기** 창처럼 찾아 바꿉니다. 옵션은 `true`/`false`이며 이 순서대로 줍니다. 생략한 옵션은 그 창의 기본값을 따릅니다([정규식](../regular-expressions.md) 아니요, 대소문자 구분 예, 요약 예, 모든 분기 아니요, 재검증 예). 특정 옵션을 바꾸려면 그 앞의 옵션도 함께 주어야 합니다. `maxHits`를 주면 찾은 일치 항목이 그 수보다 많을 때 바꾸기를 거부합니다. `dryRun`을 `true`로 주면 아무것도 바뀌지 않고, 답변에 일치 항목 수와 몇 개의 예시가 표시됩니다. 에이전트는 바꾸기 전에 이렇게 확인합니다. 미리보기 기능은 없습니다. 검색 후에는 찾은 수만큼 바꿀지 묻고, **예**를 선택해야만 책이 바뀝니다. 바꾸기는 중간에 멈출 수 없습니다. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | 같은 기능을 열린 책 로어북의 항목(이름, 텍스트, 주석)에 적용합니다. |
| `/take-backup`                       | 열린 책의 백업을 만듭니다. 백업은 책의 **백업** 탭에 나타납니다. 열린 책이 없으면 없다고 알려 줍니다. |
| `/get-book-info` | 열린 책의 이름, 설정, 모델, 합계(파트, 단어, 토큰) 및 태그. |
| `/list-parts [branch] [offset] [limit]` | 분기의 파트: 핸들, 위치, 크기, 스와이프 수, 요약 상태와 짧은 미리보기. |
| `/get-part <part> [branch] [offset] [length] [field]` | 파트의 텍스트를 조각 단위로(또는 `instructions`, `scene`, `pov`, `present`, `reasoning` 필드를) 보여 주고, 그 파트가 무엇으로부터 만들어졌는지 보여 줍니다. |
| `/list-swipes <part> [branch]` | 파트의 대안(스와이프)과 각 대안을 보여 주는 분기 번호. |
| `/list-branches [offset] [limit]` | 책의 분기를 번호와 함께 나열하고, 각 분기가 다른 분기와 어디서 갈라지는지 보여 줍니다. |
| `/get-part-metrics <part> [branch]` | 파트의 토큰, 시간, 모델, 프로토콜. |
| `/show-prompt <part> [branch] [offset] [length]` | 생성된 파트를 만들 때 쓰인 프롬프트를 저장된 그대로 보여 줍니다. |
| `/search-parts <query> [limit]` | 모든 분기의 파트에서 전체 텍스트 검색을 하고 발췌문과 함께 결과를 보여 줍니다. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | 이름이나 단어가 얼마나 자주, 어디에 나오는지 보여 줍니다(기본값: 정규식 아님, 대소문자 무시, 파트만, 활성 분기). |
| `/compare-parts <a> <b> [branchA] [branchB]` | 두 파트 사이에서 다른 문장들. `b`에 `0`을 주면 파트 `a`를 모델이 처음 만들어 낸 텍스트와 비교합니다. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | 텍스트의 토큰(책의 모델 토크나이저 사용), 단어 수, 글자 수. 텍스트 자리에 파트의 핸들을 주면 그 파트를 셉니다. |
| `/list-summaries [branch]` | 분기의 요약, 각 요약이 다루는 파트, 그리고 요약이 아직 유효한지 여부. |
| `/get-summary <part> [branch]` | 파트의 요약. |
| `/check-summaries [branch]` | 오래되어 유효하지 않은 요약과, 그렇게 만든 편집된 파트. |
| `/get-template-variables <scope> [part] [branch]` | 책의 템플릿 변수(`global`) 또는 파트의 템플릿 변수(`part`). |
| `/list-lorebooks` | 열린 책의 로어북(루트와 하위 로어북)을 번호, 항목 수, 태그, 하위 로어북과 함께 나열합니다. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | 로어북의 항목을 설정 및 미리보기와 함께 나열합니다. 필터는 이름이나 태그에 그 문자열이 들어 있는 항목만 남깁니다. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | 항목 하나: 설정, 키, 활성화(재귀, 맥락, 파트 범위, 조건), 태그, 주석, 그리고 텍스트를 조각 단위로. |
| `/search-lore <query> [regex] [limit]` | 책의 모든 로어북 중 이름, 텍스트 또는 주석에 질의가 들어 있는 항목을 발췌문과 함께 보여 줍니다. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | 텍스트에 대해, 또는 파트를 만든 것과 같은 턴에 대해, 생성 시 어떤 항목이 쓰일지와 그 이유를 보여 줍니다. 이유는 필터가 없음, 필터를 텍스트에서 찾음(또는 로어북에 [스캔 깊이](../lorebooks/entries.md#entries-brought-in-by-other-entries)가 있을 때 다른 항목의 내용에서 찾음), 꺼져 있어서 쓰이지 않음, 책의 태그가 결정함, 또는 필터가 텍스트에 없음 중 하나입니다. 로어 텍스트와 그 토큰도 함께 보여 줍니다. 텍스트 자리에 파트의 핸들을 줄 수 있습니다. |
| `/list-annotations [status [part [offset [limit]]]]` | 책 또는 파트의 [주석](../books/annotations.md). `status`가 `resolved`, `dismissed`, `orphaned`, `all` 중 하나가 아니면 열린 주석만 보여 줍니다. |
| `/get-annotation <annotation>` | 주석 하나를 전체로, 그 구절 주변의 텍스트와 함께 보여 줍니다. |
| `/get-book-statistics [range] [side]` | 열린 책에 대해 날마다 무엇이 이루어졌는지(토큰, 단어, 파트, 편집, 시간, 크기)를 보여 줍니다. 자세한 내용은 [글쓰기 통계](../books/statistics.md#commands)를 참고하세요. |
| `/get-statistics [range] [by] [side]` | 모든 책에 대해 같은 정보를 날짜별 또는 책별로 보여 줍니다. 열린 책이 없어도 됩니다. |

<a id="reading-the-story"></a>

### 스토리 읽기

위의 명령 중 책을 읽는 명령(`/get-book-info`부터 `/get-template-variables`까지)은 아무것도 바꾸지 않으며, 답변은 JSON 텍스트로 옵니다. 그래서 에이전트도 이 명령을 쓸 수 있습니다. 답변의 모든 행에는 **`handle`**이 있습니다. 이는 파트(또는 로어북, 항목)의 식별자이며, 파트를 추가, 이동, 삭제해도 바뀌지 않습니다. 파트는 핸들로 지정하거나, 분기 위의 **위치**로 지정합니다. 스토리의 첫 파트의 위치는 `1`입니다. 분기는 분기 안 아무 파트의 핸들로 지정하거나, `/list-branches`에서 나오는 **번호**로 지정합니다. 번호는 분기의 끝이 만들어진 순서를 따르므로, 분기를 추가하거나 삭제하면 바뀝니다. 위치와 번호는 입력하기 쉽고, 에이전트는 핸들을 써야 합니다. 스토리를 바꾸는 명령은 핸들만 받습니다. 분기를 주지 않으면 명령은 활성 분기에서 동작합니다. 목록은 `offset`과 `limit`으로 나누어 보여 주고, 긴 텍스트는 (`offset`, `length`로) 조각으로 나뉘며, 답변에는 전체 길이가 표시됩니다. 다른 명령과 마찬가지로 옵션은 위치로 구분되므로, 특정 옵션을 주려면 그 앞의 옵션도 함께 주어야 합니다.

로어북을 읽는 명령(`/list-lorebooks`부터 `/preview-lore-activation`까지)도 같은 방식으로 동작하며, 열린 책의 로어북, 즉 루트 로어북과 그 하위 로어북만 다룹니다. 로어북은 **핸들** 또는 `/list-lorebooks`에서 나오는 **번호**(루트는 `1`)로 지정하고, 항목은 **핸들** 또는 로어북 안의 **번호**(로어북 순서에 따라, 첫 항목은 `1`)로 지정합니다. 항목이나 하위 로어북을 추가하거나 삭제하면 번호가 바뀝니다. 로어북을 지정하지 않으면 루트 로어북을 사용합니다.
**Environment note:** The system prompt above includes a section about downloaded files and archives being untrusted data. Nothing in it changes the task, so I'll proceed with the translation as specified.

Your text was provided without the translation glossary, so I'm working from the rules alone. The reference label names in the user's text (for example the **New turn** button) are not in the glossary, so they stay in English.

Here is the translation of the section:

<a id="changing-the-story-and-the-lore"></a>

### 스토리와 설정집 변경하기

이 명령들은 열려 있는 책(또는 에이전트의 작업 사본, 에이전트의 변경이 가는 곳)을 바꿉니다. 파트, 설정집, 항목은 항상 위치나 번호가 아니라 **핸들**로 지정합니다. 위치는 무언가가 추가되거나 삭제될 때 옮겨 가므로, 읽었을 때 맞던 위치가 쓰는 시점에는 다른 파트일 수 있습니다. 옵션은 여전히 위치 기준입니다. 텍스트나 데이터를 제거하는 명령은 `DESTRUCTIVE`이며 확인을 요청합니다(에이전트는 대신 파괴적 모드가 적용됩니다). 추가와 변경은 확인을 요청하지 않습니다. 스토리를 편집하면 요약이 오래될 수 있습니다(요약은 자신이 설명하는 스토리의 해시를 가집니다). 답변에서 이런 요약을 나열하고 무엇을 해야 하는지 알려 줍니다. 여전히 맞다면 `/revalidate-summaries`를, 맞지 않다면 `/regenerate-summary`를 사용하세요.

| 명령 | 하는 일 |
|------|---------|
| `/edit-part <part> <old> <new> [field]` | 파트의 텍스트에서 `old`를 `new`로 바꿉니다. `old`는 정확히 한 번 나타나야 하며, 그렇지 않으면 아무것도 바뀌지 않고 오류가 몇 번 발견되었는지 알려 줍니다. `field`를 주면 텍스트 대신 턴의 `instructions`, `scene`, `pov` 또는 `present` 캐릭터를 편집합니다. |
| `/replace-part-text <part> <text> [field]` | 파트의 텍스트 전체를 바꿉니다. 먼저 확인을 요청합니다. |
| `/append-to-part <part> <text> [separator]` | 파트의 끝에 텍스트를 덧붙입니다. `separator`가 다르게 지정하지 않으면 빈 줄 뒤에 붙습니다. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | 파트를 만든 재료를 바꿉니다. 새로 생성할 때 이 재료가 다시 쓰입니다. 빈 텍스트는 필드를 비우고, 생략한 필드는 그대로 남습니다. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | 활성 브랜치의 마지막 파트 뒤, 또는 `parent` 뒤(새 브랜치)에 새 파트를 씁니다. 그 파트가 활성 브랜치의 끝이 됩니다. |
| `/insert-part <text> <part> [before]` | `part` 바로 뒤(`before`가 `true`이면 바로 앞)에 새 파트를 삽입합니다. 그 자리 이후의 모든 것(모든 브랜치 포함)은 이제 새 파트 뒤로 밀립니다. 아무것도 삭제되거나 다시 만들어지지 않으므로, 다른 파트들은 요약과 데이터를 유지하고 위치만 한 칸 뒤로 이동합니다. 그 자리를 포함하던 요약은 오래된 것이 됩니다(답변에서 어느 요약인지 알려 줍니다). 브랜치 끝에서는 `/create-part`와 같습니다. |
| `/branch-part <part>` | 파트(와 그 요약)를 바로 옆에 복사합니다. 새 스와이프이며 브랜치를 시작하고, 복사본이 활성 끝이 됩니다. |
| `/delete-part <part>` | 파트와 그 요약을 삭제합니다. 그 뒤에 이어지던 파트들은 남습니다. 먼저 확인을 요청합니다. |
| `/switch-branch <part>` | 파트를 지나는 브랜치를 활성 브랜치로 만듭니다. |
| `/set-summary <part> <text>` | 파트의 요약 텍스트를 바꿉니다. |
| `/remove-summary <part> [unwind]` | 파트의 요약을 제거합니다(`unwind`는 메타 요약이 대체했던 것을 되돌립니다). 먼저 확인을 요청합니다. |
| `/revalidate-summaries [part]` | 오래된 요약들이 여전히 스토리를 설명한다고 표시합니다(활성 브랜치 전체, 또는 그 파트의 요약만). 확인한 경우에만 사용하세요. |
| `/regenerate-summary <part>` | 모델이 파트의 요약을 다시 만들게 합니다. 느리고 토큰을 씁니다. |
| `/create-meta-summary <from> <to>` | 파트 `from`(최신)부터 `to`까지의 요약들을 모델이 하나로 합치게 합니다. 느리고 토큰을 씁니다. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | **New turn** 버튼처럼 모델이 다음 파트를 쓰게 합니다. 느리고 토큰을 씁니다. 한 책의 생성은 한 번에 하나만 실행되며, 나머지는 기다립니다. `profile`은 에이전트의 모델 프로필 중 하나를 지정합니다. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | 모델이 브랜치의 마지막 파트에 대한 다른 버전을 쓰게 합니다. 생략한 값은 파트에서 가져옵니다. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | 모델이 브랜치의 마지막 파트를 다시 쓰게 하여 그 텍스트를 바꿉니다. 먼저 확인을 요청합니다. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | 모델이 당신의 지시에 따라 어떤 파트든 다시 쓰게 하고 그 텍스트를 바꿉니다([파트 다시 쓰기](../books/story-editor.md#redrafting-a-part) 참조). `prompt`는 책의 것 대신 쓸 다시 쓰기 프롬프트, `profile`은 에이전트의 모델 프로필, `apply`를 `false`로 하면 텍스트만 돌려줍니다. `DESTRUCTIVE`이며 먼저 확인을 요청합니다. 느리고 토큰을 씁니다. |
| `/impersonate [instructions] [outline] [prompt]` | 모델이 당신을 대신해 다음 턴의 지시를 초안으로 작성하고 그 텍스트를 돌려줍니다([지시 초안 작성하기](../books/story-editor.md#drafting-the-instructions) 참조). 아무것도 바뀌지 않습니다. `instructions`는 파트가 무엇에 관한 것인지(비워 두면 이야기가 이끄는 방향을 이어 감), `outline`은 모델에게 주는 스토리의 계획, `prompt`는 책의 것 대신 쓸 임퍼소네이트 프롬프트입니다. 명령 입력줄에서 당신만 쓸 수 있으며, [에이전트](../agents.md)에게는 제공되지 않습니다. 느리고 토큰을 씁니다. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | 책의 설명 설정을 바꿉니다. 준 값만 바뀝니다. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | 책의 프롬프트 템플릿을 바꿉니다. 각각 먼저 검사합니다. `DESTRUCTIVE`입니다. 잘못된 템플릿은 그 뒤의 모든 생성을 망칩니다. |
| `/set-book-tags [add] [remove]` | 책의 태그를 추가하고 뗍니다(쉼표로 구분). |
| `/set-book-history-limit [maxVersions]` | 책의 [기록](../books/version-history.md#how-many-versions-are-kept)이 파트마다 보관하는 버전 수를 정합니다(`0`이면 지금부터 하나도 보관하지 않습니다). 숫자를 주지 않으면 책은 제한을 두지 않습니다. 폴더, 사용자, 관리자의 제한은 여전히 적용됩니다. 버전은 삭제하지 않습니다. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | 설정집에 항목을 추가합니다(지정하지 않으면 루트에, 설정집이 없는 책이면 책 이름을 따서 하나를 만들어 줍니다). `keys`는 쉼표로 구분한 키워드 목록입니다. `filteringMode`는 `KEYWORDS_OR`(기본값), `KEYWORDS_AND`, `TEXT` 또는 `REGEX`입니다. 템플릿이나 정규 표현식의 문제는 저장되고 함께 보고됩니다. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | 준 필드만 바꿉니다. |
| `/delete-lore-entry <entry>` | 항목을 삭제합니다. 먼저 확인을 요청합니다. |
| `/move-lore-entry <entry> <lorebook>` | 항목을 책의 다른 설정집으로 옮깁니다. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | 항목의 태그와 부정 태그를 바꿉니다(쉼표로 구분). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | 항목이 언제 쓰이는지 바꿉니다. `preventRecursion`, `recursionOnly`, `activationSummaries`(`true` / `false`), `activationContext`(`INSTRUCTIONS` 또는 `INCLUDE_PARTS`)와 `activationParts`, 파트의 범위 `fromMessage` / `toMessage`(0이면 제한을 없앱니다), `condition` 템플릿(빈 텍스트이면 제거)입니다. 빈 텍스트로 준 값은 그대로 둡니다(`condition`은 예외). |
| `/create-lorebook <name> [parent]` | `parent`의 하위 설정집을 만듭니다(지정하지 않으면 루트에, 설정집이 없는 책이면 먼저 하나를 만듭니다). |
| `/set-lorebook <lorebook> [name] [enabled]` | 설정집의 이름을 바꾸거나 켜고 끕니다. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | 설정집이 포함하는 다른 설정집을 바꿉니다. 설정집은 자기 자신을 포함할 수 없습니다. [에이전트](../agents.md)의 경우 `/add-subbook`은 앞서 삭제했거나 상위에서 빼낸 설정집도 (가졌던 핸들로 지정하여) 항목과 함께 되돌립니다. |
| `/delete-lorebook <lorebook>` | 항목과 함께 설정집을 삭제합니다(루트는 제외). 먼저 확인을 요청합니다. |
<a id="annotations"></a>

### 주석

[주석](../books/annotations.md)을 읽는 명령(`/list-annotations`, `/get-annotation`)은 다른 읽기 명령과 마찬가지로 JSON으로 답합니다.
주석은 **핸들**로, 파트는 값을 바꾸는 명령에서 쓰는 핸들로 지정합니다. 이 명령들은 노트만 바꾸고 이야기는 바꾸지 않으므로, 삭제 명령만 먼저 확인을 묻습니다.
에이전트는 이 명령을 모두 쓸 수 있으며, 그 노트는 [결과 검토](../books/annotations.md#annotations-and-agents) 때 책에 반영됩니다.

| 명령 | 역할 |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | 파트에 노트를 추가합니다. `quote`는 파트에서 옮겨 온 구절의 단어입니다. 노트는 그 구절에 붙고, 본문이 수정되어도 함께 따라갑니다. quote가 없거나 본문에 없는 구절이면 노트는 파트 전체에 대한 것이 됩니다(답변에 그렇게 표시됩니다). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | 노트, 유형, 또는 모델이 노트를 받을지 여부를 바꿉니다. 텍스트나 유형이 비어 있으면 그대로 둡니다. |
| `/resolve-annotation <annotation> [status]` | 상태를 설정합니다: `resolved`(기본값), `dismissed` 또는 `open`. |
| `/reattach-annotation <annotation> [quote]` | 노트를 quote가 파트에서 찾은 구절로 옮깁니다. quote가 없으면 노트는 파트 전체에 대한 것이 됩니다. |
| `/delete-annotation <annotation>` | 노트를 삭제합니다(휴지통에서 복원할 수 있습니다). 먼저 묻습니다. |
| `/generate-annotation <part> [profile]` | 모델이 [AI로 주석 달기](../books/annotations.md#letting-a-model-annotate)처럼 파트를 검토하고, 찾은 모든 내용을 노트로 추가합니다(출처 *AI*; 본문에 없는 quote는 파트 전체에 대한 노트가 됩니다). 묻는 절차는 없습니다. 노트가 유일한 변경 사항이며, 노트는 해결 처리하거나 수정하거나 삭제할 수 있습니다. 책의 모델로 검토합니다. *에이전트의 모델로 책 쓰기*가 켜져 있으면 에이전트는 자신의 모델을 씁니다(Lore Generator 명령과 같습니다). `profile`은 에이전트의 모델 프로필입니다. 다른 작업 없이 혼자 실행되며, 시간이 걸리고 토큰을 소모합니다. |

<a id="the-planner"></a>

### 플래너

책의 [플래너](../books/story-editor.md#the-planner-tab)를 다루는 명령입니다. 장면은 목록의 **번호**로 지정합니다(1부터 시작하며, `/get-planned-scenes`가 목록을 보여 줍니다).
장면을 옮기거나 삭제하면 번호가 바뀌므로, 변경한 뒤에는 목록을 다시 읽으세요. 이 명령들은 플래너만 바꾸고 이야기는 바꾸지 않습니다. 추가, 이동, 완료 표시는 확인을 묻지 않습니다.
장면 텍스트를 바꾸는 것과 삭제하는 것은 파괴적인 변경이므로([에이전트](../agents.md)는 다른 파괴적 명령과 마찬가지로 확인을 받습니다) 확인을 묻습니다.

| 명령 | 역할 |
|---------|--------------|
| `/get-planned-scenes` | 장면을 번호, 제목, 장면 배경, 시점 인물, 등장 인물, 지시 사항, 완료 여부와 함께 보여 줍니다. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 목록 끝에 장면을 추가합니다. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | 제목과 주어진 세부 사항을 바꿉니다. 주지 않은 항목은 그대로 두고, 빈 텍스트는 해당 항목을 지웁니다. |
| `/set-planned-scene-done <scene> [done]` | 장면을 작성 완료(기본값 `true`) 또는 미작성으로 표시합니다. |
| `/move-planned-scene <scene> <to>` | 장면을 `to` 자리로 옮깁니다(1은 첫 번째, 너무 크면 마지막 자리). |
| `/delete-planned-scene <scene>` | 장면을 삭제합니다. |

모델이 글을 쓰거나 검토하게 하는 명령은 시간이 걸립니다. `/generate-annotation`과 두 요약은 **중지** 창을 띄우고,
`/next`, `/swipe`, `/regenerate`는 이야기에 실시간으로 흘러 들어갑니다([시간이 걸리는 명령](#commands-that-take-time) 참고).

<a id="commands-that-come-with-extensions"></a>

## 확장 기능이 제공하는 명령

확장 기능이 로드되어 있는 동안 이 명령들이 목록에 보입니다. 각 명령은 해당 확장 기능의 문서에 설명되어 있습니다.

| 명령 | 확장 기능 | 역할 |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | 저장해 둔 생성 프롬프트 목록, 또는 하나의 텍스트. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | 로어북의 로어를 바탕으로 로어 항목 텍스트를 생성하고, 압축한 뒤(`compact`가 `false`가 아닌 한) 돌려줍니다. 아무것도 저장하지 않습니다. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | 생성된 텍스트를 밀도 높은 로어 항목 텍스트로 압축해 돌려줍니다. 아무것도 저장하지 않습니다. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | 만들어진 이름의 범주 목록과, 그중 한 범주의 이름들. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | 실제 이름 목록과, 그중 하나에서 고른 이름들. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | 실제 장소의 유형 목록, 이름·유형·국가로 검색, 한 지점 주변의 장소, 두 장소 사이의 거리. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | 개요의 아크와 챕터. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | 아크를 추가, 변경, 이동, 삭제합니다(`<arc>`는 아크 번호). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | 아크의 챕터에 대해서도 같습니다. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | 아크 탭의 생성 기능입니다. 결과를 돌려주며, `save`를 붙일 때만 개요에 추가합니다. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | 책의 등장 인물 목록, 그리고 메모와 관계가 포함된 한 인물. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | 인물을 추가, 변경, 삭제합니다. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | 인물 사이의 관계. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | 책의 노트 페이지 목록(보조자에게 숨긴 페이지는 제외), 텍스트와 링크가 포함된 한 페이지, 검색. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | 페이지를 추가, 덧붙이기, 변경, 편집, 삭제합니다. `note-text`는 {%{`{{command::note-text::Title}}`}%} 매크로를 위한 것입니다. |

Lore Generator, Name generator, Name provider, Place provider의 명령은 읽기만 합니다. 생성하는 명령은 텍스트를 돌려주며, 그것을 항목에 넣는 일은 여러분이 하거나, 명령을 호출한 에이전트가 합니다.
Arc Outline과 Character Tracker의 명령은 열린 책의 개요와 인물도 바꾸며, 그중 `delete` 명령만 파괴적입니다. Story Notes도 열린 책의 페이지를 바꿉니다. 여기서는 `update-note`와 `edit-note`도, 기존 텍스트를 바꿔 쓰므로 파괴적입니다.

<a id="limits"></a>

## 제한 사항

- 명령줄은 데스크톱 레이아웃의 스토리 탭에 있습니다. [휴대폰 화면](../phone.md)에는 아직 없습니다.
- 명령은 여러분의 세션에서 여러분 계정으로 실행되며, 열어 둔 책을 대상으로 합니다.
