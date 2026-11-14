---
label: 가져오기 및 내보내기
order: 80
---

# 가져오기 및 내보내기

<a id="exporting"></a>

## 내보내기

편집기에서 로어북을 선택하고 **내보내기** 버튼(파일 아이콘, *Export Lorebook*)을 클릭합니다. 브라우저가 다음 내용이 담긴
`<lorebook name>.json` 파일을 다운로드합니다.

- 로어북과 **모든 하위 로어북**(모든 단계), 각각의 이름, 설명, 사용 상태와 태그;
- 모든 항목의 내용, 메모, 순서, 필터, 삽입 방식, 태그와 제외 태그.

이 기능은 사본을 보관하거나, 다른 사용자와 세계관을 공유하거나, 다른 설치 환경으로 옮길 때 사용합니다. [확장 기능](../extensions/index.md)이
로어북이나 항목에 저장한 데이터도 파일에 포함되며, 가져오기 때 복원됩니다. [Lorebook VCS](../extensions/lorebook-vcs.md) 확장 기능의 항목 수정 기록은
기존 항목에 속하므로 가져오기로 만들어진 항목에는 나타나지 않습니다. 이 기록은 확장 기능 자체의 내보내기 기능으로 따로 내보내야 합니다.

<a id="exporting-for-sillytavern"></a>

### SillyTavern용으로 내보내기

**내보내기** 버튼을 누르면 두 개의 항목이 있는 메뉴가 열립니다. **Export Lorebook**(위의 파일)과 **Export to SillyTavern**입니다.
후자는 로어북을 SillyTavern이 가져올 수 있는 SillyTavern 월드 정보 파일로 내려받습니다. 이것은 [가져오기](#importing-sillytavern-world-info)의 반대 과정입니다.

| Marginalia | SillyTavern |
|---|---|
| *Entry Name* | 제목 / 메모 (`comment`) |
| *Content*, *Order*, *Enabled* | 내용, 순서, 비활성화 |
| 필터 없음 | 상시 항목 (🔵) |
| *Text* 필터 | 기본 키 하나 |
| *Keywords (any)* | 키워드마다 기본 키 하나 |
| *Keywords (all)* | 첫 키워드는 기본 키, 나머지는 로직 *AND ALL*의 보조 키 |
| *Regex* | `/pattern/flags` 형식으로 쓴 키 하나 |
| *In Lore Block* | 위치 *캐릭터 정의 앞* |
| *Before User Prompt* | 위치 *깊이* 1 |
| 태그 | 캐릭터 필터(이름 기준); 항목에 태그가 없을 때만 제외 태그 사용(*exclude*) |

**하위 로어북은 하나로 병합됩니다.** SillyTavern에는 로어북 안의 로어북이라는 개념이 없으므로, 파일에는 먼저 로어북의 항목이, 그다음 하위 로어북의 항목이 들어갑니다.
SillyTavern에 대응하는 기능이 없는 것은 제외됩니다. 항목의 *Note*, 태그와 함께 쓰인 제외 태그, 확장 기능의 데이터가 이에 해당합니다. SillyTavern은 최근 채팅에서 키를 찾습니다. 반대 방향의 차이는 [달라지는 점](#what-works-differently)을 참고하십시오.

!!! warning
정규식은 Java 문법으로 작성됩니다. SillyTavern은 JavaScript를 사용하므로 단순한 식은 동작하지만, Java 전용 기능(소유 수량자, `\p{...}` 문자 클래스)은 그곳에서 일치하지 않습니다.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Marginalia 로어북 가져오기

1. **가져오기** 버튼(파일 아이콘)을 클릭하고 **Import Marginalia Lorebook**을 선택합니다.
2. Marginalia에서 내보낸 파일을 업로드합니다.

가져오기는 항상 **새** 로어북을 만듭니다. 로어북과 하위 로어북을 같은 구조로 만들며, 같은 이름의 로어북이 이미 있더라도 마찬가지입니다.
가져온 로어북은 편집기에서 선택된 상태로 열립니다. 태그는 이름으로 기존 태그와 맞춰지며, 없으면 새로 만들어집니다.

이전 버전 Marginalia의 파일(하위 로어북이 없는 단일 로어북)도 가져올 수 있습니다.

!!!
로어북은 [책 백업](../books/backups.md)에도 함께 들어 있습니다. 백업을 복원하거나 복제할 때 사본을 만드는 대신 이미 가지고 있는 로어북에 연결될 수 있습니다. [백업 복원](../books/backups.md#restoring-a-backup)을 참고하십시오.
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## 다른 사용자가 공유한 로어북 가져오기

가져오기 메뉴의 **다른 사용자에게서 가져오기**는 다른 사용자가 [공유한](lorebooks.md#sharing-a-lorebook) 로어북을 작성자 이름, 작성자와 설명과 함께 나열합니다. 행을 열어 항목을 읽고, **Import** 버튼을 눌러 개인 사본을 받을 수 있습니다. 작성자가 공유한 하위 로어북만 포함됩니다. 파일 가져오기와 달리 내보내서 보낼 필요가 없습니다. 작성자는 **다른 사용자와 공유**만 체크하면 됩니다.

<a id="importing-an-archive-of-text-files"></a>

## 텍스트 파일 아카이브 가져오기

1. **가져오기** 버튼을 클릭하고 **아카이브에서 가져오기**를 선택합니다.
2. `.md`(Markdown) 파일이 들어 있는 `.zip` 파일을 업로드합니다.
3. 표에서 **항목 이름**을 확인하고 필요하면 바꾼 뒤 **Import**를 클릭합니다.

![아카이브 업로드 창](../../images/archive-lorebook-upload.png)

![가져오기 전 항목 이름들](../../images/archive-entry-names.png)

Marginalia는 파일 이름을 따서 이름이 붙은 새 로어북을 만들고, **`.md` 파일마다 항목 하나**를 만듭니다(`.zip`은 뺀 이름). 파일은 이름 순서대로 가져오며, 이름 안의 숫자는 숫자로 비교합니다(`2.md`가 `10.md`보다 앞에 옵니다). 아카이브의 폴더 안 파일도 같은 방식입니다. 항목은 이 순서에 따라 *Order* 1, 2, 3...을 받습니다. 파일 전체가 *Content*가 됩니다. 다른 파일, 폴더, 숨김 파일(`.name`)과 Mac에서 만든 아카이브의 `__MACOSX` 폴더는 건너뜁니다.

**항목 이름**은 다음 순서로 채워집니다.

1. 아카이브 최상위의 `names.txt` 파일: 한 줄에 이름 하나씩, 첫 줄은 위 순서의 첫 번째 `.md` 파일에, 이후 줄은 차례로 적용됩니다(빈 줄이면 그 파일은 다음 규칙으로 넘어갑니다. 남는 줄은 무시됩니다). 스크립트 등으로 많은 파일의 이름을 한꺼번에 정할 때 사용합니다.
2. 파일의 첫 번째 제목(`# Chapter 1`이면 *Chapter 1*);
3. `.md`를 뺀 파일 이름.

대화 상자의 표에 결과가 보이며, 어떤 이름이든 직접 고칠 수 있습니다.

가져오기가 진행되면 진행 상황이 표시됩니다. **Stop**은 현재 저장 중인 항목까지 끝내고 가져오기를 멈춥니다. 로어북에는 이미 가져온 항목이 남으며(가져오기는 취소되지 않습니다), 몇 개의 파일을 가져왔는지 메시지로 알려줍니다.

항목은 **비활성화된 상태**로 가져옵니다. 필터가 없어서 활성화하면 모든 요청에 포함되는데, 책 한 권 분량의 장들이라면 그것은 대체로 원하지 않는 결과이기 때문입니다. 필요한 항목만 필터나 [태그](entries.md#tags)와 함께 켜십시오. 항목이 많다면 **Mass edit**(일괄 편집)을 쓰는 것이 빠릅니다.

같은 파일을 이야기에 파트로 넣고 싶다면 [책에 가져오기](../books/story-editor.md#importing-into-the-book)를 참고하십시오.

<a id="importing-sillytavern-world-info"></a>

## SillyTavern 월드 정보 가져오기

1. **가져오기** 버튼을 클릭하고 **SillyTavern에서 가져오기**를 선택합니다.
2. SillyTavern에서 내보낸 월드 정보 파일(`.json`)을 업로드합니다.

Marginalia는 월드 정보의 이름(또는 파일 이름)을 따서 새 로어북을 만들고, 각 항목을 다음과 같이 변환합니다.

| SillyTavern | Marginalia |
|---|---|
| 제목 / 메모 (`comment`) | *Entry Name* (제목이 없으면 첫 번째 키) |
| 내용 | *Content* - `{%{{{user}}}%}`, `{%{{{char}}}%}` 같은 매크로는 그대로 동작합니다. [매크로](../templates/macros.md)를 참고하십시오 |
| 순서 | *Order* |
| 비활성화 | *Enabled* 체크 해제 |
| 상시(🔵), 또는 키가 없음 | 항상 활성 (필터 없음) |
| 기본 키, 선택적 보조 키와 그 로직 | 필터, 아래 참고 |
| 대소문자 구분, 단어 단위 일치 | 필터에 포함됨 |
| 위치 *캐릭터 정의 앞/뒤*, *예시 메시지* | *In Lore Block* |
| 위치 *작가의 노트*(위/아래), *깊이* | *Before User Prompt* |
| 캐릭터 필터 | 태그(*exclude*이면 제외 태그), 캐릭터 이름과 태그 기준 |

**키는 필터가 됩니다.** 평범한 키 하나는 *Text* 필터(`dragon`)가 됩니다. 그 밖의 경우(여러 키, 보조 키(*AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*), `/regex/` 형식의 키, 대소문자 구분이나 단어 단위 일치)는 같은 일을 하는 *Regex* 필터 하나가 됩니다. 예를 들어 키 `dragon`, `wyrm`에 보조 키 `fire`(*AND ANY*)가 있으면 다음과 같이 됩니다.

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

이 식을 이해할 필요는 없습니다. 정규식을 아는 경우에만 편집하십시오.

<a id="what-works-differently"></a>

### 달라지는 점

SillyTavern 설정 중 Marginalia에 대응하는 것이 없는 항목은 해당 항목의 **Note**에 적힙니다. 예를 들어 *Imported from SillyTavern, not supported: probability 50%, inclusion group "weather".* 같은 식입니다. 이에 해당하는 것은 다음과 같습니다.

- 확률, 포함 그룹, sticky, cooldown, delay - 항목은 조건에 맞으면 항상 활성화됩니다;
- *깊이*에 삽입하는 설정 - 항목은 대신 사용자 프롬프트 앞에 들어갑니다.

가장 큰 차이는 **키를 무엇과 대조하는가**입니다. SillyTavern은 최근 채팅을 살핍니다. Marginalia는 작성 중인 파트에 대한 [요청](entries.md#filters) - 장면, 캐릭터, 지시문 - 과 대조하며, 가져온 항목은 정확히 그 범위에서 시작합니다(**Activation Context**가 *Instructions*). 그래서 SillyTavern에서는 최근 메시지에 이름이 나와서 활성화되던 항목이, Marginalia에서는 턴에서 그 이름을 언급해야만 활성화됩니다. SillyTavern과 같은 동작을 원하면 항목의 **Activation Context**를 *Instructions and parts of the story*로 설정하고, **Parts**에 살펴볼 최신 파트의 개수를 적으십시오. [이야기 살펴보기](entries.md#looking-into-the-story)를 참고하십시오. 재귀 활성화(항목이 다른 항목을 활성화하는 것)도 가져오기가 설정하지 않습니다. 로어북의 **scan depth**로 켜십시오. [다른 항목이 가져온 항목](entries.md#entries-brought-in-by-other-entries)을 참고하십시오.

가져온 뒤에는 항목을 훑어보고, 어떤 항목을 항상 활성화할지, 어떤 항목이 필터를 유지할지, 어떤 항목을 [태그](entries.md#tags)로 다루는 것이 나은지 정하십시오.
