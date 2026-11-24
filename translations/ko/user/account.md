---
label: 계정 및 설정
order: 920
icon: person
---

# 계정 및 설정

Marginalia에서 만드는 모든 것, 즉 책, 로어북, 태그, 추론 제공자, 프로토콜, 설정은 사용자의 계정에 속합니다. 다른 사용자는 이것들을 볼 수 없고(사용자가 [공개](books/publishing.md)한 책은 예외입니다), 사용자도 다른 사용자의 것을 볼 수 없습니다.

<a id="your-account"></a>

## 내 계정

작업 공간 하단의 버튼은 다음과 같습니다.

| 버튼 | |
|---|---|
| **Language** | 사용자 인터페이스의 언어입니다. [언어](#language)를 참고하세요. |
| 해/달 버튼 | 언어 선택기 옆에 있으며, 어두운 테마와 밝은 테마를 전환합니다. [테마](#theme)를 참고하세요. |
| **Change Password** | 비밀번호, 사용자 이름, 전체 이름을 변경합니다. 아래를 참고하세요. |
| **Logout** | 이 기기에서 로그아웃합니다. |
| **Admin** | 관리자 전용입니다. [관리](administration/index.md)를 참고하세요. |

<a id="changing-the-password"></a>

### 비밀번호 변경

**Change Password**를 누르면 **Username**, **Full Name**, **Current Password**, **Password**, **Repeat password**가 있는 대화상자가 열립니다.

- 무엇을 변경하든 **현재 비밀번호**를 입력해야 합니다(계정에 비밀번호가 없다면 비워 두세요).
- 비밀번호를 바꾸려면 새 비밀번호를 두 비밀번호 필드에 모두 입력합니다.
- 이름만 바꾸고 비밀번호는 그대로 두려면 두 비밀번호 필드를 모두 비워 두세요.
- 비밀번호를 바꾸지 않고 다른 모든 기기에서 계정을 로그아웃하려면 **Log out everywhere else (other browsers and all saved logins)**를 선택하세요. 자세한 내용은 아래를 참고하세요.
- 사용자 이름은 다른 계정에서 사용 중이면 안 됩니다.

**OK**를 눌러 저장합니다. 현재 비밀번호가 틀리면 아무것도 저장되지 않습니다.

비밀번호를 바꾸면 저장된 모든 로그인(*Save login*)이 지금 사용 중인 기기를 포함해 모든 기기에서 종료되며, 새 비밀번호로 다시 로그인해야 합니다. 지금 열려 있는 다른 세션(다른 기기의 브라우저에 작업 공간이 아직 표시되어 있는 경우)도 종료되고, 지금 작업 중인 세션만 계속 유지됩니다. 기기를 분실했거나 비밀번호가 유출되었다면 비밀번호를 바꿔 해당 기기를 로그아웃시킬 수 있습니다.

**Log out everywhere else**는 새 비밀번호 없이 같은 일을 합니다. 계정에 저장된 모든 로그인이 삭제되고 다른 열린 세션이 모두 종료됩니다. 내 계정이 아닌 컴퓨터 등 어딘가에 로그인한 채로 두었을 때 사용하세요. 이 대화상자의 다른 변경과 마찬가지로 현재 비밀번호가 필요합니다. 지금 사용 중인 세션은 유지되지만, 그 세션에 저장된 로그인도 삭제되므로 이 브라우저도 다음에는 비밀번호를 묻습니다.

<a id="language"></a>

### 언어

버튼 위에 있는 **Language** 선택기로 사용자 인터페이스를 다른 언어로 바꿀 수 있습니다. 선택한 언어는 계정에 저장됩니다. 페이지가 새 언어로 다시 로드되고, 열려 있는 다른 모든 페이지(다른 기기 포함)도 마찬가지이며, 다음에 로그인할 때도 이 언어가 사용됩니다. 로그인 화면 자체는 항상 기본 언어로 표시됩니다.

목록에는 설치된 언어만 나타납니다. 영어, 일본어, 프랑스어, 스페인어, 독일어, 한국어, 이탈리아어는 Marginalia에 포함되어 있으며, 관리자는 [확장](extensions/index.md)을 불러와 언어를 더 추가할 수 있습니다. 예를 들면 [중국어 현지화](extensions/chinese-localization.md)가 있습니다. 해당 언어로 번역되지 않은 텍스트는 영어로 표시됩니다. 기본 포함 확장은 일본어, 중국어, 프랑스어, 스페인어, 독일어, 한국어, 이탈리아어로 된 자체 텍스트를 제공합니다(중국어 텍스트는 중국어 현지화를 불러왔을 때 표시됩니다). 날짜는 해당 언어의 표기 방식으로 쓰입니다. 책과 그 본문은 번역되지 않으며, 인터페이스만 번역됩니다.

![Language 선택기](../images/language-selector.png)

![일본어로 표시된 작업 공간](../images/ui-japanese.png)

<a id="theme"></a>

### 테마

**Language** 선택기 옆의 버튼으로 사용자 인터페이스를 어두운 테마(기본값)와 밝은 테마 사이에서 전환합니다. 선택한 테마는 계정에 저장되어 어느 기기에서든 다음 로그인 때 다시 사용됩니다. 다른 탭에 이미 열려 있는 페이지는 새로 고칠 때 바뀝니다. 차트와 관계도는 열릴 때 테마의 색상을 따릅니다.

![밝은 테마의 작업 공간](../images/theme-light.png)

<a id="logging-in"></a>

### 로그인

로그인 화면의 **Save login**을 선택하면 이 기기에서 30일 동안 로그인 상태가 유지됩니다(서버에서는 HTTPS가 필요합니다. [서버(Docker)](getting-started/docker-server.md#https-and-reverse-proxy)를 참고하세요). **Logout**을 누르면 로그인 상태가 종료됩니다.

계정의 비밀번호는 비워 둘 수 있으며, 이 경우 사용자 이름만으로 로그인합니다. 관리자가 잊어버린 비밀번호를 지워 주었다면 이 방식으로 로그인한 뒤 바로 **Change Password**에서 새 비밀번호를 설정하세요. 자신의 컴퓨터에서 쓰는 데스크톱 앱에서는 편리한 방법이지만, 다른 사람이 접속할 수 있는 서버에서는 절대 사용하지 마세요.

<a id="settings"></a>

## 설정

**Settings** 탭에는 모든 책에 적용되는 기본값이 있습니다.

![Settings 탭](../images/settings-defaults.png)

맨 위에는 버튼이 세 개 있습니다.

- **Import Backup as New Book** - 백업 파일로 새 책을 만듭니다. [책 백업](books/backups.md#importing-a-backup-as-a-new-book)을 참고하세요.
- **Discard Changes** - 저장하지 않은 변경 사항을 버리고 저장된 설정을 다시 불러옵니다.
- **Save** - 설정을 저장합니다.

!!!info 저장하지 않은 변경 사항
설정은 자동으로 저장되지 않습니다. 필드(확장 설정 포함)를 변경하면 곧바로 맨 위에 *Unsaved changes*가 나타나고, 다른 탭과 **Admin**, **Logout**이 잠깁니다. **Save** 또는 **Discard Changes**를 누르면 잠금이 풀립니다.
!!!

### General Settings

| 필드 | |
|---|---|
| **Default Model** | 새 책이 처음에 사용하는 [추론 제공자](inference-providers.md)입니다. |
| **Default Protocol** | 새 책이 처음에 사용하는 [프로토콜](protocols.md)입니다. |
| **Default model for annotating**, **Default protocol for annotating** | 실행할 때 다른 모델을 고르지 않았을 경우 [파트에 주석을 달](books/annotations.md#letting-a-model-annotate) 모델입니다. 비워 두면 책의 모델을 사용합니다. |
| **Backup Strategy** | 자체 전략이 없는 책에 적용되는 자동 [책 백업](books/backups.md#automatic-backups)입니다. *No Backups*, *After N Messages*, *After N Minutes* 중에서 고르며, 옆에 숫자를 입력합니다. |
| **Versions kept per part** | 폴더, 책 또는 관리자가 더 낮게 제한하지 않는 한, 모든 책에서 [기록](books/version-history.md#how-many-versions-are-kept)이 파트 본문의 이전 버전을 몇 개까지 보관할지 정합니다. 비워 두면 여기서는 제한이 없고, **0**이면 기록을 보관하지 않습니다. |
| **Parts loaded at once in the story** | [스토리](books/story-editor.md)와 [뷰어](books/publishing.md)가 책의 파트를 몇 개까지 불러온 상태로 유지할지 정합니다. 스토리는 마지막 파트부터, 뷰어는 읽던 위치(또는 처음 파트)부터 시작하며, 앞뒤의 파트는 스크롤하면서 불러오고 멀리 떨어진 파트는 다시 내려놓습니다. 비워 두면 모든 파트를 한꺼번에 불러옵니다. |
| **Daily goal: words written**, **Daily goal: minutes of editing** | [집필 통계](books/statistics.md#the-daily-goal) 달력에 쓰이는 하루 목표입니다. 모든 책을 통틀어 직접 쓴 단어 수와 편집한 시간(분)을 기준으로 합니다. 비워 두면 목표가 없습니다. |
| **Record writing statistics**, **Keep statistics for (months)** | [집필 통계](books/statistics.md#privacy-clearing-not-recording-keeping-less)를 집계할지, 그리고 몇 개월 분량을 보관할지 정합니다(비워 두거나 0이면 전부 보관). 관리자는 모든 사용자의 기록을 끄거나 더 짧은 보관 기간을 정할 수 있습니다. |

*Default Model*과 *Default Protocol*은 책을 만들 때만 사용되며, 이를 바꿔도 기존 책은 바뀌지 않습니다. [Lore Generator](extensions/lore-generator.md) 확장은 책이 없으므로 이 값으로 생성합니다. 백업 전략은 *Per manuscript override*가 없는 모든 책에 적용되며, 다음에 파트를 생성할 때부터 반영됩니다.
<a id="templates"></a>

### 템플릿

[북 프롬프트](books/prompts.md)의 기본값입니다.

| 필드 | |
|---|---|
| **Prompt language** | 내장 프롬프트의 언어입니다. 영어(기본값), 일본어, 프랑스어, 스페인어, 독일어, 이탈리아어, 한국어, 그리고 [중국어 현지화](extensions/chinese-localization.md)를 불러온 경우 중국어를 고를 수 있습니다. 아래의 [프롬프트 언어](#prompt-language)를 참고하세요. |
| **Master Template** | 프롬프트의 틀입니다. [마스터 템플릿](books/prompts.md#master-template)을 참고하세요. |
| **Point of View (POV)**, **Tense** | 서술 시점과 시제입니다. |
| **Style** | 문체와 어조에 대한 지침입니다. |
| **Default User Prompt** | 각 파트에 대한 요청입니다. [사용자 프롬프트](books/prompts.md#user-prompt)를 참고하세요. |
| **Default Summary Prompt** | [요약](books/summaries.md#the-summary-prompt)을 위한 요청입니다. |
| **Default Meta Summary Prompt** | 요약들을 하나로 합치는 [메타 요약](books/summaries.md#the-meta-summary-prompt)을 위한 요청입니다. |
| **Redraft System Prompt**, **Redraft Prompt** | 파트를 [다시 쓰기](books/story-editor.md#redrafting-a-part)할 때의 시스템 메시지와 마지막 메시지입니다. [다시 쓰기 프롬프트](books/prompts.md#redraft-prompt)를 참고하세요. |
| **Annotate System Prompt**, **Annotate Prompt** | 모델에게 파트를 [주석 달도록](books/annotations.md#letting-a-model-annotate) 요청할 때의 시스템 메시지와 마지막 메시지입니다. [주석 프롬프트](books/prompts.md#annotate-prompt)를 참고하세요. |
| **Annotate Instructions** | 모델이 주석을 달 때 무엇을 살펴볼지를 정하는 지침으로, 아무것도 설정하지 않은 북과 폴더에 적용됩니다. [주석 프롬프트](books/prompts.md#annotate-prompt)를 참고하세요. |
| **Impersonate Prompt** | 모델이 당신 대신 다음 차례의 지시문을 [작성하도록](books/story-editor.md#drafting-the-instructions) 요청하는 프롬프트입니다. [대필 프롬프트](books/prompts.md#impersonate-prompt)를 참고하세요. |
| **Compaction prompt of agents** | [에이전트](agents.md)를 사용할 수 있는 사용자에게만 표시됩니다. 에이전트 자체의 압축 프롬프트가 비어 있을 때, 에이전트를 [`/compact`](agents.md#running-a-command-yourself)할 때 에이전트에게 전달되는 요청입니다. 여기가 비어 있으면 관리자가 설정한 프롬프트를, 그것도 없으면 내장 프롬프트를 사용합니다. |
| **Instructions of agents (agents.md)** | [에이전트](agents.md)를 사용할 수 있는 사용자에게만 표시됩니다. 자체 지침이 없는 에이전트를 위한 당신의 지침으로, 매 실행마다 에이전트의 프롬프트에 추가됩니다. 비어 있으면 추가되는 지침이 없습니다. |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | [*Notifications*](administration/permissions.md) 권한이 있는 사용자에게만 표시되며([에이전트](agents.md)를 사용하는 사용자에 한정되지 않습니다), *General* 탭에 있습니다. 또한 관리자가 허용한 경우에만 사용할 수 있습니다. 애플리케이션을 닫아 둔 상태에서 에이전트가 무언가를 묻거나, 당신을 기다리거나, 작업을 마치거나, 실패했을 때 짧은 메시지를 보낼 곳을 정합니다. [자리를 비웠을 때](agents.md#when-you-are-not-there)를 참고하세요. |
| **System prompt of agents** | [에이전트](agents.md)를 사용할 수 있는 사용자에게만 표시됩니다. 에이전트 자체의 시스템 프롬프트가 비어 있을 때 에이전트에게 어떤 역할을 맡길지 알려 주는 내용입니다. 여기가 비어 있으면 관리자가 설정한 프롬프트를, 그것도 없으면 내장 프롬프트를 사용합니다. 템플릿이 아닌 일반 텍스트입니다. |

![템플릿 탭](../images/settings-templates.png)

아래쪽에는 에이전트를 사용할 수 있는 사용자를 위한 에이전트 프롬프트가 있습니다.

![템플릿 탭의 에이전트 프롬프트](../images/settings-templates-agents.png)

각 필드는 비어 있는 동안 내장 기본값을 회색으로 보여 주며, **Insert Default Template**과 사용 가능한 [변수와 매크로](templates/index.md)가 있는 힌트 팝오버를 제공합니다.

<a id="prompt-language"></a>

#### 프롬프트 언어

**Prompt language**는 내장 프롬프트의 언어를 정합니다. 즉, 비어 있는 필드가 무엇을 뜻하는지(다른 언어를 고르면 회색 글자가 바로 바뀝니다)를 정하며, 따라서 생성, 요약, 다시 쓰기, 주석, 대필에서 모델에게 전달되는 내용도 이 설정을 따릅니다. 자체 프롬프트를 가진 플러그인(Arc Outline, Beta Readers, Book Description Generator, Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook)과 에이전트의 프롬프트도 이를 따릅니다. 이 설정은 인터페이스 언어와는 별개입니다. 애플리케이션은 영어로 사용하면서 일본어 프롬프트를 보낼 수도 있습니다. 모델은 이야기의 언어로 된 지시를 가장 잘 따르므로, 북을 쓰는 언어를 고르세요. 바뀌는 것은 내장 프롬프트뿐입니다. 여기에서, 또는 북이나 폴더에서 직접 작성한 프롬프트는 그대로 유지됩니다. [에이전트](agents.md)는 이 설정이 아니라 에이전트 설정의 *Language*에 지정된 자기만의 언어로 프롬프트와 안내문을 읽습니다.

이 기본값은 **자체 필드가 비어 있는 모든 북**에 즉시 적용됩니다. 새 북에만 적용되는 것이 아닙니다. *Prompts* 탭에서 자체 값을 설정한 북은 그 값을 유지합니다. 따라서 다음과 같이 사용하면 됩니다.

- 모든 북이 공유하는 내용은 여기에서 설정하세요.
- 특정 북에만 필요한 내용은 그 북의 *Prompts* 탭에서 설정하세요.
- 북의 필드를 비우면 *Settings*의 기본값으로 되돌아갑니다.

<a id="styles"></a>

### 스타일

북의 텍스트 스타일입니다. 파트의 글이 이야기와 리더에서 어떻게 보이는지를 정합니다. 이 탭에는 애플리케이션이 제공하며 변경할 수 없는(*built in*으로 표시된) **Normal**과 **Book** 스타일, 그리고 직접 만든 스타일이 나열됩니다. 스타일을 선택하면 오른쪽에서 편집할 수 있으며, 목록 아래에는 다음 버튼이 있습니다.

- **Duplicate** - 선택한 스타일을 바탕으로 나만의 스타일을 만듭니다(내장 스타일에서도 가능하며, 이렇게 시작하면 됩니다). 이름은 *... (copy)*가 되며, 이후 이름을 바꾸고 내용을 수정할 수 있습니다.
- **Delete** - 직접 만든 스타일을 삭제합니다. 내장 스타일은 삭제할 수 없습니다.
- **Set as default** - 이야기의 설정 메뉴에서 [다른 스타일을 고르지](books/story-editor.md#text-styles) 않은 북이 사용할 스타일로 지정합니다. 기본 스타일은 목록에 *default*로 표시됩니다.
- **Export**와 **Import** - 선택한 스타일을 `.style.json` 파일로 저장하고, 그런 파일에 들어 있는 스타일을 내 스타일에 추가합니다(설정을 저장하기 전까지는 저장되지 않습니다). [스타일 공유하기](books/styling-guide.md#share-a-style-export-and-import)를 참고하세요.

[스타일 가이드](books/styling-guide.md)에서 모든 옵션을 차례로 설명하며, 내려받을 수 있는 예시 스타일도 제공합니다.

![스타일 가이드의 호러 스타일이 선택된 스타일 탭](../images/settings-styles.png)

직접 만든 스타일의 경우, 오른쪽 양식에 **Name**, **For exports (not offered for books)** 체크 상자, 그리고 두 개의 탭이 있습니다. 스타일은 북용이거나 내보내기용 중 하나입니다. 내보내기용 스타일은 [내보내기 대화 상자](books/export.md#print-styles)에 표시되며(애플리케이션이 HTML, EPUB, PDF, Word 내보내기의 모양을 담은 네 가지를 제공합니다) 북의 기본 스타일로 지정할 수 없습니다. 북용 스타일은 내보내기 대화 상자에 표시되지 않습니다. 목록에서는 내보내기용 스타일이 *(export)*로 표시됩니다.

- **Options** - 그룹별로 나뉘어 있습니다. *Text*(글꼴, 크기, 줄 높이, 글자 간격과 단어 간격, 색, 정렬, 하이픈 처리, 문단 사이의 간격, 문단의 **첫 줄 들여쓰기**, 제목 뒤의 **드롭 캡**), *Page*(글 단의 너비, 가운데 정렬, 그리고 글 뒤의 전체 영역을 채우는 **배경색**), *Emphasis, links and code*(기울임꼴과 굵은 글씨, 링크의 색, 코드 글꼴), *Quotes*, *Headings*(글꼴, 첫 번째 수준의 크기, 색, 정렬, 스몰 캡), 그리고 *Scene break*의 기호와 색(`---`)이 있습니다. 각 입력란에는 어떤 CSS 값이든(`1.1rem`, `#336699`, `var(--lumo-body-text-color)`) 넣을 수 있으며, 비워 두면 애플리케이션의 기본 모양이 그대로 유지됩니다. 글꼴 입력란은 자주 쓰는 글꼴 스택을 제안하며, 직접 만든 것도 입력할 수 있습니다.
- **Custom CSS** - 직접 작성한 CSS로, 옵션 뒤에 추가되어 스타일 안에서 적용됩니다. 따라서 작성한 규칙은 이 스타일이 적용된 텍스트에만 영향을 줍니다. `.chat-message-markdown p { text-indent: 1.5em !important; }`은 문단을 들여쓰며, 단순한 선언(`font-size: 1.2rem;`)은 텍스트 전체에 적용됩니다. 그 아래의 **Generated CSS**에는 옵션으로 만들어진 스타일시트가 표시됩니다(읽기 전용). 중괄호 `{ }`의 짝이 맞아야 하며, 맞지 않으면 스타일이 저장되지 않습니다.

![스타일의 Custom CSS 탭](../images/settings-styles-css.png)

양식 아래의 **Preview**는 해당 스타일이 적용된 샘플 텍스트를 보여 주며, 입력하는 대로 변경 사항을 따라갑니다.

다른 탭과 마찬가지로 스타일 탭도 설정의 **Save**로 저장되며, **Discard Changes**는 이 탭의 변경 사항도 함께 버립니다. 스타일을 복제하거나 편집하거나 삭제하거나 기본값으로 지정하는 작업은 작업용 사본만 바꾸며, 이 중 하나라도 하면 *Unsaved changes*(와 잠금 표시)가 나타납니다. 이름이 없는 스타일이나 중괄호의 짝이 맞지 않는 사용자 지정 CSS는 저장할 때 거부됩니다.
<a id="exports"></a>

### 내보내기

각 파일 형식을 내보낼 때 쓰는 설정입니다. 이 탭에는 설정할 항목이 있는 형식마다 구역이 하나씩 있습니다(**Plain text**, **Markdown**, **HTML**,
**Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault**. 이름을 클릭하면 열립니다).

- **Print style** (HTML, Word, PDF, EPUB처럼 글꼴과 색이 있는 형식) - 해당 형식에 대해 [내보내기 대화상자](books/export.md)가 미리 선택해 두는 스타일입니다.
  처음에는 애플리케이션이 그 형식에 기본으로 제공하는 모양(**HTML page**, **PDF book** 등)으로 되어 있으며, 목록에는 기본 제공 스타일과 직접 만든 스타일을 합친
  [내보내기용 스타일](#styles)이 들어 있습니다. 다른 스타일을 고르면 그 형식은 매번 그 스타일로 내보내집니다. 한 번만 다르게 내보내고 싶다면 대화상자에서 선택을 바꾸면 됩니다.
- **Front matter** (Markdown with front matter와 Obsidian vault) - 파일의 맨 앞에 오는 YAML 블록이며, `{{title}}`, `{{author}}`,
  `{{language}}`, `{{exportDate}}`을 쓸 수 있습니다. 비워 두면 기본 블록이 쓰입니다.
- **Title page** (다른 도구용 형식을 제외한 모든 형식. Obsidian vault는 책 노트에 포함됩니다) - 표지 페이지의 템플릿입니다. Markdown을 만들어 내는 Handlebars 템플릿이며, `{{title}}`와
  `{{author}}`를 쓸 수 있습니다(필드의 정보 아이콘에서 목록을 볼 수 있습니다). 비워 두면 기본 템플릿이 쓰입니다. 제목을 헤딩으로, 그 아래에 저자를 기울임꼴로 적는 형태입니다.
  정보 팝오버의 **Fill default**를 누르면 기본 템플릿에서 시작할 수 있습니다.
- **Authorship statement** (SillyTavern 채팅을 제외한 모든 형식) - 내보내기의 체크 박스 *Include authorship statement*가
  끝에 덧붙이는 문구입니다. `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`,
  `{{generatedPercent}}`, `{{models}}`(쉼표로 구분한 모델 목록이며, 없으면 비어 있음)을 쓸 수 있습니다. 비워 두면 기본 문구가 쓰입니다.

여기 있는 것은 내 계정의 기본 버전입니다. 책이나 폴더에 따로 설정할 수도 있습니다. [Title page template](books/export.md#title-page-template)을 참고하세요. 이 탭은 다른 탭과 마찬가지로 설정의 **Save**로 저장되고 **Discard Changes**를 누르면 변경 사항이 버려집니다. 올바르지 않은 템플릿은 저장할 때 거부됩니다. Styles 탭에서 만든 스타일은 저장한 뒤에 여기서 고를 수 있습니다.

![HTML 구역이 열린 Exports 탭](../images/settings-exports.png)

<a id="tags"></a>

### 태그

책, 로어북, 로어북 항목에 붙이는 모든 태그를 표로 보여 주며, 태그마다 연결된 개체의 수가 함께 나옵니다. 열 머리글을 클릭하면 그 열을 기준으로 정렬됩니다. 다른 탭과 달리 이 탭은 바로 적용됩니다. 저장하지 않은 변경 사항에 포함되지 않으며 저장할 것도 없습니다.

![Tags 탭](../images/settings-tags.png)

| 버튼 | |
|---|---|
| **Rename** (연필) | 태그의 새 이름을 물어봅니다. 모든 개체가 새 이름으로 그 태그를 유지합니다. |
| **Delete** (휴지통) | 확인을 요청한 뒤 태그를 삭제하고, 그 태그를 가진 모든 개체에서 제거합니다. |

새 이름이 다른 태그의 이름과 같으면(이름은 대소문자를 구분하지 않고 비교합니다) 이름을 바꾸면 두 태그가 같아지므로, 두 태그를 **병합**할지 묻습니다. 병합하면 이름을 바꾼 태그가 붙은 모든 것이 기존 태그로 옮겨지고 이름을 바꾼 태그는 삭제됩니다. 두 태그를 모두 가진 개체에는 기존 태그가 한 번만 남습니다. 취소하면 아무것도 바뀌지 않으며 태그는 예전 이름을 유지합니다.

![두 태그를 병합할지 묻는 창](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### 키보드 단축키

[스토리 편집기 단축키](books/story-editor.md#keyboard-shortcuts)의 키입니다(컴퓨터의 스토리 편집기에서 작동하며, [휴대폰 레이아웃](phone.md)에는 없습니다).
표에는 [확장 기능](extensions/index.md)이 추가하는 것을 포함해 모든 동작이 키와 함께 나열되며, 책 목록의 [Organise](books/folders.md#organise) 창에 있는 버튼의 키도 들어 있습니다. 스토리 설정 메뉴, 파트 메뉴, 파트 본문의 오른쪽 클릭 메뉴의 모든 항목이 여기서는 하나의 동작입니다(대부분은 키를 지정하기 전에는 키가 없습니다). 메뉴에는 항목 옆에 키가 표시됩니다.

| 버튼 | |
|---|---|
| **Record** | 새 키 조합을 누릅니다(수정 키를 먼저 누르거나 함께 누릅니다. 예: `Ctrl+Shift+K`). 다른 곳을 클릭하면 취소됩니다. |
| **Reset to default** | 동작에 기본 키를 되돌려 줍니다. |
| **Clear** | 동작에서 키를 없앱니다. |

빨간색 안내는 두 동작이 같이 쓰는 키, 브라우저가 자신을 위해 쓰는 키(`Ctrl+N`, `Ctrl+T`, `Ctrl+W` 등.
페이지에는 전달되지 않습니다), 그리고 글자와 함께 쓴 `Alt`(macOS처럼 일부 키보드에서는 문자가 입력됩니다)를 알려 줍니다. `Ctrl`, `Alt`, `Meta` 없이 쓰는 키는 어떤 텍스트 필드에도 포커스가 없을 때만 작동합니다. 키는 다른 설정과 마찬가지로 **Save**로 저장되며, 열려 있는 스토리에 바로 적용됩니다.

![설정의 Keyboard shortcuts](../images/settings-shortcuts.png)

<a id="notifications"></a>

### 알림

모델이 작업을 마치면 브라우저의 데스크톱 알림으로 알려 주므로, 모델이 작업하는 동안 탭을 떠나 있어도 됩니다. 이 탭에는 체크 박스 **Use desktop notifications**, 그 아래의 버튼들, 알림을 보낼 항목의 목록이 있습니다.

- **Story generation**, **Annotate with AI**, **Annotate with AI (automatic)**, **Redraft**, **Redraft (automatic)**, **Draft Instructions**,
  **Summary**, **Resummarize all summaries**, **Help assistant**, 그리고 [확장 기능](extensions/index.md)이 추가하는 항목(확장 기능의 이름이 앞에 붙습니다.
  *Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters* 등).
  *Manga Generator* 항목 중 *(automatic)*이 붙은 것은 연속 모드입니다.
- *(automatic)*이 붙은 항목은 책이 스스로 시작하는 실행입니다. 생성 뒤의 [자동 주석](books/story-editor.md)과 그 뒤에 이어지는 다시 쓰기가 여기에 해당합니다. 이런 항목은 직접 여는 대화상자와는 따로 선택합니다. **Annotate with AI**만 체크하고
  **Annotate with AI (automatic)**는 체크하지 않으면 자동 주석은 알림 없이 진행되고, 자동 항목만 체크하면 자동 주석만 알림을 보냅니다.
- 오류로 끝난 실행도 알립니다(*… failed*). 직접 중단한 실행은 알리지 않습니다.

체크 박스는 내 계정의 알림을 켜고 두 버튼을 활성화합니다. **Register this browser**는 (그 클릭을 계기로) 브라우저에 권한을 요청하며, 버튼 아래의 줄에는 이 브라우저의 상태가 표시됩니다. 허용됨, 차단됨(브라우저의 사이트 설정에서 허용해야 합니다), 아직 묻지 않음 중 하나입니다. 권한은 계정이 아니라 브라우저에 속합니다. 알림을 받으려는 브라우저나 기기마다 한 번씩 등록해야 하며, 체크 박스와 목록은 어디서나 같습니다. **Send a test notification**은 페이지가 앞에 있을 때도 알림을 바로 보여 줍니다.

**Filter** 필드는 이름으로 목록을 좁히고, **Check shown**과 **Uncheck shown**은 표시된 항목에만 적용되며 나머지는 그대로 둡니다. 알림은 페이지를 보고 있지 않을 때(다른 탭이거나 창이 앞에 없을 때)만 표시되며, 알림을 클릭하면 페이지로 돌아옵니다.

알림을 쓰려면 `https://` 주소 또는 `localhost`이 필요합니다. 내 네트워크에 있는 서버의 일반 `http://` 주소에서는 브라우저가 알림을 제공하지 않으며, 탭에 그렇게 안내됩니다. 브라우저가 잠들게 한 탭(주로 휴대폰)에서도 알림을 보여 줄 수 없습니다. 이 탭은 다른 탭과 마찬가지로 설정의 **Save**로 저장됩니다.

<a id="phrase-watch-list"></a>

### 문구 감시 목록

모든 책이 쓰는 내 [문구 감시 목록](books/annotations.md#watching-for-phrases)입니다(폴더와 책은 자기 목록을 추가합니다). 그리고 이 목록의 항목과 폴더, 책의 항목이 고르는 **주석 템플릿**도 여기서 정합니다. 문구가 발견되었을 때 주석이 어떤 내용을 말하는지, 그 유형, 모델에게 전달할지 여부를 정합니다. 다른 탭과 마찬가지로 설정의 **Save**로 저장됩니다.

![설정의 문구 감시 목록](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### API 키

HTTP API(`/api/v1`)를 통해 Marginalia를 사용하는 프로그램이나 스크립트를 위한 키입니다. 프로그램이 헤더 `Authorization: Bearer <key>`에 키를 실어 보내면, 여러분의 책에서 여러분이 할 수 있는 일을 그대로 수행합니다. 관리자와 [*API access*](administration/permissions.md) 권한이 있는 사용자만 사용할 수 있으며, 권한이 없으면 탭에 그 사실이 표시됩니다.

![API keys 탭](../images/settings-api-keys.png)

![한 번만 표시되는 새 키](../images/api-key-created.png)

**New API key**를 누르면 **Name**(어떤 프로그램이 키를 쓰는지 적어 두면 나중에 어느 키를 폐기할지 알 수 있습니다), **Access**, 그리고 **Expires**(며칠 뒤에 만료할지 입력하며, 비워 두면 만료되지 않습니다)를 입력합니다. 접근 권한은 다음 중 원하는 대로 조합할 수 있습니다.

| 접근 권한 | |
|---|---|
| **Read** | 여러분의 책, 그리고 읽기만 하는 명령. |
| **Write** | 변경하거나 삭제하는 명령을 포함한 모든 명령. *Read*를 포함합니다. |
| **Update the installation** | 관리자 전용: 설치본의 [업데이트](administration/updates.md)(상태, 로그, 시작). |
| **Manage the extensions** | 관리자 전용: [확장 기능](administration/extensions.md)의 설치, 업데이트, 중지, 제거. |

모든 키는 Marginalia의 버전을 읽을 수 있습니다. 키에는 프로그램에 필요한 권한만 주세요. 예를 들어 플러그인을 설치하는 스크립트에는 *Manage the extensions*만 있으면 되고, 그런 키로는 여러분의 책에 손댈 수 없습니다. 키의 소유자가 관리자가 아니게 되면 그 키로는 업데이트와 확장 기능을 더 이상 사용할 수 없으며, 나머지 접근 권한은 그대로 유지됩니다.

키는 만든 직후에 딱 한 번만 표시됩니다. 이때 복사해 두세요. Marginalia는 키의 지문만 보관하므로 다시 보여 줄 수 없습니다. 목록에는 각 키의 앞부분, 만든 시각, 만료 시각, 마지막으로 사용한 시각이 표시됩니다. 휴지통 버튼은 키를 **폐기**하며, 그 키를 쓰는 프로그램은 즉시 접근 권한을 잃습니다. 키는 만료되었을 때, 그리고 관리자가 권한을 회수했을 때에도 작동을 멈춥니다. 키는 만들거나 폐기하는 즉시 저장되며, 설정의 **Save**와는 상관이 없습니다.

**API 페이지**: 브라우저에서 Marginalia의 `/api/v1/`를 열면(예: `https://marginalia.example.org/api/v1/`) 모든 호출과 그 인자, 응답을 볼 수 있습니다. 맨 위의 **API key**에 키를 붙여 넣으면, 그 키로 실행할 수 있는 모든 명령도 인자와 함께 목록에 표시되고, **Try it out** → **Execute**를 누르면 그 키로 호출을 보내 응답을 보여 줍니다. 호출은 실제로 실행됩니다. 명령은 여러분의 책을 바꾸고, 업데이트는 서버를 다시 시작합니다. 페이지는 탭이 열려 있는 동안에만 키를 기억하며, 디스크에는 절대 저장하지 않습니다.

개발자를 위한 호출 설명은 [개발자 매뉴얼](../developer/api.md)에 있습니다.

### Extension Settings

설치된 [확장 기능](extensions/index.md)의 설정으로, 확장 기능마다 한 섹션씩 있습니다(예: *Reviewer Settings*, *SideQuery Settings*). 같은 **Save** 버튼으로 저장합니다. 설정이 있는 확장 기능이 설치되어 있지 않으면 탭은 비어 있습니다.

![Lore Generator 설정이 보이는 Extension Settings 탭](../images/settings-extensions.png)
