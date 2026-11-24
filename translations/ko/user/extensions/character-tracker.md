---
label: 캐릭터 추적기
order: 19
---

# 캐릭터 추적기

사이드바에서 볼 수 있는 책의 등장인물 목록입니다. 각 인물에는 한 줄 설명(tagline), 메모, 아이콘, 색이 있으며, 인물 사이의 관계와 그 관계를 보여 주는 지도도 있습니다.

<a id="the-characters-tab"></a>

## Characters 탭

[스토리 편집기](../books/story-editor.md)의 왼쪽 사이드바에 *Outline* 옆으로 **Characters** 탭이 생깁니다. 목록 위에는 한 줄짜리 버튼들이 있습니다: **Add**(새 인물), 로어북 항목을 가져오는 메뉴, **Relationship map** 버튼, 그리고 프롬프트용 말풍선 버튼(이름은 마우스를 올리면 표시됨)입니다.

말풍선 버튼을 누르면 [Author's Note](authors-note.md)처럼 **In prompt**, **Depth**와 역할을 설정하는 팝오버가 열립니다. 인물이 프롬프트에 들어가 있는 동안 버튼은 초록색입니다. *In prompt*를 체크하면 모든 생성 때 인물 정보가 하나의 메시지로 전달됩니다. 각 인물마다 이름, 대괄호 안의 한 줄 설명, 메모가 한 줄씩 들어가고, 관계 목록(`Mara -> Tomas: employs`, 양방향이면 `<->`)도 함께 들어갑니다. *Depth*는 이 메시지가 끝에서부터 몇 번째 메시지 뒤에 놓일지를 정하는 값입니다(`0`은 맨 끝, 즉 지시문 뒤, `1`은 지시문 바로 앞). 역할은 *System*, *Assistant*, *User* 중 하나입니다. 이 텍스트는 스토리용 공간의 토큰을 차지합니다. 책에 인물이 하나도 없으면 아무것도 삽입되지 않습니다. 설정은 책과 함께 저장됩니다.

그 사이에 있는 마법봉 버튼에는 메뉴 항목 **Import lore entries as characters**가 있습니다. [아래](#importing-characters-from-the-lorebook)를 참고하세요.

목록은 이름순으로 정렬됩니다. 각 인물은 자신의 색을 가지며, **이름** 아래에 더 작은 글씨로 **tagline**이 표시됩니다(최대 두 줄). 인물별 버튼은 다음과 같습니다:

| 버튼 | |
|---|---|
| 꺾쇠(chevron) | 인물을 펼쳐 **icon**과 **note**를 보여 줍니다. |
| 펜 | 대화상자에서 인물을 엽니다. |
| 휴지통 | 확인 후 인물과 그 관계를 모두 삭제합니다. |

인물은 변경하는 즉시 책과 함께 저장되므로 백업에도 포함됩니다.

![Characters 탭](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## 턴에 등장인물 고르기

[새 턴](../books/story-editor.md) 양식에는 **POV character**와 **Present characters** 필드가 있습니다. 이 필드들은 여전히 자유 입력 텍스트이지만, 각각 끝에 사람 모양 아이콘이 붙은 버튼이 있어 책의 인물을 이름순으로 보여 줍니다. **POV character** 목록에서 이름을 고르면 필드의 내용이 그 이름으로 바뀝니다. **Present characters** 목록에서 고른 이름은 쉼표 뒤 맨 끝에 추가되며, 이미 들어 있으면 추가되지 않습니다. 목록은 열 때 읽히므로, 방금 추가한 인물도 목록에 나타납니다. 필드에 어떤 이름이든 직접 입력할 수 있으며, 필드의 이름이 책의 인물일 필요는 없습니다.

<a id="the-character-dialog"></a>

## 인물 대화상자

**Add**(플러스 버튼, 툴팁은 *Add new character*)와 펜 버튼은 같은 대화상자를 엽니다:

| 필드 | |
|---|---|
| **Name** | 필수입니다. |
| **Tagline** | 인물에 관한 몇 단어로, 목록과 지도에 표시됩니다. |
| **Note** | 기억해 두고 싶은 무엇이든. |
| **Icon** | PNG, JPEG 또는 GIF를 업로드합니다. 최대 200 x 200픽셀로 축소되며, 비율은 유지됩니다. |
| **Color** | 목록과 지도에서 인물의 배경색입니다. |

필드 내용은 **Save**를 눌러 적용합니다.

![관계가 있는 인물](../../images/plugin-charactertracker-dialog.png)

저장하지 않은 변경 사항이 있는 상태(아직 추가하지 않은 관계를 입력한 경우도 포함)에서 **Cancel**(또는 Esc)을 누르면 변경 사항을 버릴지 묻습니다.

![저장되지 않은 변경 사항이 있는 인물을 취소할 때 나오는 질문](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### 관계

기존 인물을 열면 대화상자에 그 인물의 관계도 나열됩니다. 인물이 가리키는 관계(`→`), 인물을 가리키는 관계(`←`), 그리고 단방향 관계(`↔`)로, 각각 이름과 한 줄 설명이 붙습니다. 휴지통 버튼은 관계를 삭제합니다.

관계를 추가하려면 상대 인물을 검색하고, **Direction**(*this character → other*, *other → this character* 또는 *both ways*)을 고른 뒤, **Name**(예: *employs*, *friends*)을 입력합니다. 필요하면 **Tagline**도 입력하고, **Add relationship** 버튼을 누릅니다(이 버튼은 입력 필드 위에 있습니다). 관계는 즉시 저장됩니다. 새로 만든 인물은 저장한 뒤에야 관계를 가질 수 있습니다.

<a id="importing-characters-from-the-lorebook"></a>

## 로어북에서 인물 가져오기

인물이 이미 로어북에 있다면, 마법봉 메뉴가 이들을 대신 만들어 줍니다. **Import lore entries as characters**를 열면 책의 로어북과 모든 하위 로어북의 항목이 *order* 순으로 정렬되어 나타납니다. 각 항목에는 이름과 체크박스가 있고, **i** 아이콘을 누르면 팝오버로 항목 내용이 나타나 결정하기 전에 빠르게 살펴볼 수 있습니다. 목록 머리글의 체크박스로는 모든 항목을 한 번에 선택하거나 해제합니다. **Import**는 체크된 항목마다 그 이름으로 인물을 만듭니다(한 줄 설명, 메모, 아이콘, 색은 비워 두므로 직접 채우면 됩니다). **Close**는 가져오지 않고 닫습니다.

항목은 이미 있는 인물과 비교되지 않습니다. 같은 항목을 두 번 가져오면 인물이 두 개 만들어집니다.

![로어북에서 인물 가져오기](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## 관계 지도

**Relationship map**은 책의 모든 관계를 그래프로 보여 줍니다. 관계가 있는 인물은 색을 배경으로 한 원으로 나타나며, 원 안에 아이콘(없으면 상자)이 들어가고 이름과 한 줄 설명이 함께 표시됩니다. 각 관계는 두 인물을 잇는 화살표이며, 이름과 한 줄 설명이 붙습니다. 화살표는 대상을 가리키고, 단방향 관계는 양쪽 끝에 화살표가 있습니다. 관계가 없는 인물은 지도에 나타나지 않습니다.

![관계 지도](../../images/plugin-charactertracker-map.png)

인물들은 한 인물을 중심으로 고리 모양으로 배치됩니다. 처음에는 관계가 가장 많은 인물이 중심이 됩니다. 중심 인물과 관계된 인물은 첫 번째 고리에, 그 인물들과 관계된 인물은 두 번째 고리에 놓이는 식입니다. **인물을 더블클릭**하면 그 인물을 중심으로 지도가 다시 배치됩니다. 인물을 끌어다 옮기거나, 지도를 이동하고, 마우스 휠로 확대·축소할 수도 있습니다.

<a id="commands"></a>

## 명령

인물은 [명령](../commands/index.md)으로도 다룰 수 있으며, 명령줄과 [에이전트](../agents.md)에서 사용할 수 있습니다. 명령은 열려 있는 책에 적용되고(에이전트는 작업 사본에 적용), 열려 있는 Characters 탭에는 변경 사항이 즉시 표시됩니다. 인물은 **이름**(대소문자 구분 없음)으로 지정하거나, 이름이 같은 인물이 둘 이상이면 `/list-characters`에서의 **번호**(이름순 번호)로 지정합니다. 관계는 `/list-character-relations`에서의 번호로 지정합니다. 인물을 변경하는 명령은 지정한 필드만 바꾸며, 빈 텍스트를 주면 한 줄 설명이나 메모가 지워집니다. 아이콘은 대화상자에서만 설정할 수 있습니다.

| 명령 | 기능 |
|---|---|
| `/list-characters` | 이름순으로 정렬된 인물: 한 줄 설명, 색, 메모 유무, 관계 수. 읽기 전용입니다. |
| `/get-character character` | 인물 한 명과 그 메모 및 관계. 읽기 전용입니다. |
| `/create-character name [tagline [note [color]]]` | 인물을 추가합니다. `color`은 `#rrggbb`입니다. |
| `/update-character character [name [tagline [note [color]]]]` | 인물을 변경합니다. |
| `/delete-character character` | 확인 후 인물을 관계와 함께 삭제합니다. |
| `/list-character-relations [character]` | 전체 관계 또는 한 인물의 관계. 읽기 전용입니다. |
| `/add-character-relation source target name [tagline [bidirectional]]` | `source`에서 `target`로 향하는 관계(*employs*)를 추가합니다. `bidirectional` `true` 옵션을 주면 양쪽이 동등한 관계(*friends*)를 추가합니다. |
| `/update-character-relation relationship [name [tagline]]` | 관계의 이름이나 한 줄 설명을 변경합니다. |
| `/delete-character-relation relationship` | 확인 후 관계를 삭제합니다. |
| `/character-tracker-map` | 프롬프트가 받는 텍스트 형태의 인물과 관계. 읽기 전용입니다. |

`character-tracker-map`은 [{%{`{{command::character-tracker-map}}`}%} 매크로](../templates/macros.md#commands)에서 쓰도록 만들어졌습니다. 이를 마스터 템플릿, 사용자 프롬프트 또는 로어북 항목에 넣으면, Characters 탭의 자동 삽입 대신(또는 그와 함께) 원하는 위치에 인물을 배치할 수 있습니다. 삽입 기능이 켜져 있든 꺼져 있든 항상 인물의 현재 상태를 보여 줍니다.

추가와 변경은 파괴적이지 않으며, 파괴적인 것은 `delete` 명령뿐입니다. 에이전트는 이 명령을 실행하기 전에 확인을 요청하도록 설정할 수 있습니다.

Arc Outline의 [플롯 마법사](arc-outline.md)는 **Characters** 도구를 선택하면 같은 명령을 사용하므로, 줄거리를 이야기하면서 등장인물을 함께 발전시킬 수 있습니다.

<a id="phone"></a>

## 휴대폰

이 탭은 [Author's Note](authors-note.md)처럼 스토리 서랍(drawer)에서 개요 옆에 있습니다. POV 및 등장 인물 버튼도 휴대폰의 새 턴 양식에 있습니다.
