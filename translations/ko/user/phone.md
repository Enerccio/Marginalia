---
label: 휴대폰에서 Marginalia 사용하기
order: 985
icon: device-mobile
---

# 휴대폰에서 Marginalia 사용하기

Marginalia에는 휴대폰을 위한 레이아웃이 있습니다. 휴대폰 브라우저에서도 글을 쓰고, 부분을 편집하고 다시 생성하고, 로어북을 편집하고, 모델을 관리할 수 있으며, 관리자라면 설치 전체를 관리할 수 있습니다. 휴대폰 레이아웃은 **데스크톱 레이아웃의 복사본이 아닙니다**. 같은 기능이 한 열로 배치되고, 일부 창은 더 단순하며, 모든 확장 기능이 동작하지는 않습니다. 이 페이지에서는 그 차이를 정리합니다.

휴대폰에서 책을 읽을 때는 이를 위해 만들어진 [뷰어](books/publishing.md)를 사용할 수도 있습니다.

<a id="when-the-phone-layout-is-used"></a>

## 휴대폰 레이아웃이 사용되는 경우

Marginalia는 페이지를 불러올 **때** 브라우저 창의 너비를 확인합니다. 768픽셀보다 좁으면 휴대폰 레이아웃이, 그보다 넓으면 일반 [작업 공간](index.md#the-workspace)이 나타납니다.

- 가로 방향의 태블릿과 휴대폰은 보통 768픽셀보다 넓어서 데스크톱 레이아웃을 사용합니다.
- 작업 도중에는 레이아웃이 **바뀌지 않습니다**. 휴대폰이나 태블릿을 옆으로 돌리거나 창 크기를 바꿔도 **페이지를 다시 불러올 때까지** 레이아웃은 그대로입니다. 따라서 태블릿이나 가로로 돌린 휴대폰에서 더 넓은 데스크톱 레이아웃을 쓰려면 기기를 돌린 뒤 다시 불러오고, 휴대폰 레이아웃으로 되돌리려면 다시 돌려놓고 또 다시 불러오면 됩니다.
- 데스크톱 브라우저 창을 좁게 줄이면 이 역시 휴대폰 레이아웃이 되므로, 이를 미리 살펴보는 데 편리합니다.

데스크톱과 마찬가지로, 휴대폰 레이아웃도 브라우저 메뉴의 *Install app* / *Add to Home Screen*을 통해 앱처럼 홈 화면에 추가할 수 있습니다.

<a id="the-workspace"></a>

## 작업 공간

왼쪽의 탭들은 **서랍(drawer)**으로 대체됩니다. 상단 표시줄의 메뉴 버튼을 눌러 서랍을 열고, 탭을 누르면 해당 탭으로 전환됩니다. 현재 탭은 화면 전체를 채우며, 그 이름이 상단 표시줄에 나타납니다.

![서랍이 열린 휴대폰 작업 공간](../images/phone-workspace-drawer.png)

서랍에는 데스크톱과 같은 탭들이 있습니다. *Books*, *Lorebooks*, *Settings*, *Protocols*, *Inference Providers*, *Agents*(
[에이전트](agents.md#on-a-phone)를 사용할 수 있는 사용자에게만 표시), *Resources*, *Trash*이며, 관리자에게는 *Admin*도 표시됩니다(데스크톱에서는 버튼이던 것이 여기서는 탭입니다). 맨 아래에는 **Language** 선택기, **Change Password**, **Logout**이 있습니다. 확장 기능은 버튼을 추가할 수 있지만, 휴대폰에서 자신이 알고 있는 위치에만 추가할 수 있습니다. [확장 기능](#extensions)을 참고하세요.

*Settings* 탭에 저장되지 않은 변경 사항이 있으면, 데스크톱에서와 마찬가지로 서랍이 잠깁니다. 먼저 저장하거나 변경 사항을 취소하세요.

<a id="books"></a>

### 책

책 목록은 한 열로 표시됩니다. 목록 위에는 이름 필터, 태그 필터, 정렬 버튼(*Last Modified*, *Created*, *Name*이며, 활성화된 버튼을 다시 누르면 순서가 뒤집힘)이 있고, 그 아래에 **Add Book**, 돋보기(
[고급 검색](books/managing-books.md#advanced-search): 포함하고 제외할 태그, 날짜, 부분 개수, 텍스트), 새로 고침 버튼이 있습니다. 각 책에는 버튼이 있습니다. 별표는 즐겨찾기로 지정하고, **Move to folder**는 책을 [폴더](books/folders.md)에 넣고, 연필은 책을 열고, 휴지통은 삭제합니다([휴지통](trash.md)으로 이동).

![휴대폰의 책 목록](../images/phone-book-list.png)

목록은 데스크톱에서와 마찬가지로 한 번에 [폴더](books/folders.md) 하나만 보여 줍니다. 현재 있는 폴더의 경로는 되돌아가기 위한 버튼들로 표시되고, 그 아래에 **Include subfolders** 체크박스가 있으며, 이어서 책 위에 있는 폴더들이 나타납니다(하나를 누르면 그 안으로 들어가며, 그 아래에 있는 책의 개수가 표시됩니다). 최상위 수준에는 가상 폴더 **Favorites**도 있습니다. 이름 필터와 태그 필터는 현재 폴더를 검색하며, *Include subfolders*를 켜면 그 아래 모든 항목을 검색합니다. **Add Book**과 가져오기는 새 책을 현재 있는 폴더에 넣습니다. 현재 폴더는 기억됩니다.

![휴대폰의 폴더](../images/phone-folders.png)

![책 목록의 메뉴](../images/phone-books-menu.png)

폴더는 헤더의 **New folder** 버튼으로 만들고, 폴더 줄의 **⋮** 버튼으로 이름을 바꾸거나, 설정(설명, 태그, 언어, 그 안의 책에 적용할 모델과 프로토콜)하거나, 삭제합니다. 헤더의 **⋮** 버튼에는 여러 책에 대해 작업하는 창들이 있습니다. **Organise**(휴대폰에서는 폴더의 두 패널이 위아래로 배치되며, 키보드 안내는 없습니다), **Tag books**, **Tag folders**, 그리고 [폴더](books/folders.md)의 일괄 변경(모델과 프로토콜, 언어, 이름 변경)입니다. 책의 언어는 여기서도 폴더로부터 상속받을 수 있으며, *Prompts* 탭에서는 각 값이 어디서 왔는지 볼 수 있습니다.

<a id="lorebooks"></a>

### 로어북

휴대폰에는 데스크톱의 **로어북 편집기 전체**가 있으며, 한 열로 배치되어 있습니다. 로어북은 상자에서 고르고, 그 옆의 **⋮** 버튼을 누르면 로어북 메뉴가 열립니다. 메뉴에는 **Add Lorebook**, **Clone**, **Delete**, **Import**(로어북 파일, SillyTavern 파일, Markdown 파일을 묶은 zip 보관 파일, 다른 사용자가 공유한 로어북), **Export**(로어북 또는 SillyTavern 형식), **Tag lorebooks**, **Edit description**이 있습니다. *Enabled* 체크박스, 토큰 수, **Search and replace** 및 **activation preview** 버튼 아래에 있는 **Settings** 섹션을 열면 로어북의 이름, 하위 로어북, 태그, 스캔 깊이, *Share with other users*를 설정할 수 있습니다. [로어북](lorebooks/index.md)을 참고하세요.

그 아래에는 **Add Entry**(그리고 여러 항목의 일괄 편집과 태그를 위한 **⋮**), 검색 상자, 항목 목록이 있습니다. 항목은 활성화 체크박스와 이름이 한 줄에 있고, 열면 양식이 나타납니다. 양식에는 이름, 순서, 태그, 필터링 모드와 텍스트, 삽입 모드, 부정 태그, 활성화 맥락과 그 범위, *Also search the summaries*, *Prevent recursion*, *Only through other entries*, 내용, 메모, **Delete entry**가 있습니다. 변경한 내용은 데스크톱에서와 마찬가지로 즉시 저장됩니다. 검색 상자 아래에는 목록을 제한하는 태그 상자와 상태 상자가 있으며, **⋮** 메뉴의 **Select entries**를 켜면 모든 항목 앞에 체크박스가 나타나고, 체크한 항목에 대해 **Delete selected**가 표시됩니다. 여기에 없는 것은 *Only root lorebooks* 체크박스입니다.

이야기 서랍의 **Lorebook** 탭은 데스크톱 탭과 같이 책의 설정을 보여 줍니다. 검색 상자, 내용이 펼쳐지는 항목들, 항목의 텍스트를 편집하는 연필이 있습니다.

같은 편집기가 책의 *Lorebook* 탭에서도 사용됩니다. 책의 로어북에 하위 로어북이 있으면, 그 아래의 **Shown lorebook** 상자에서 탭이 어느 하위 로어북을 보여 줄지 바꿀 수 있고, 책의 로어북은 바뀌지 않은 채로 다시 돌아올 수 있습니다. 책을 대상으로 한 [에이전트](agents.md)가 이 로어북을 사용해 작업하는 동안에는, 로어북이 텍스트로 표시되며 그 안의 어떤 내용도 바꿀 수 없습니다.

![휴대폰의 로어북](../images/phone-lorebook.png)

![로어북의 메뉴](../images/phone-lorebook-menu.png)

![로어북의 설정](../images/phone-lorebook-settings.png)

![양식으로 열린 항목, 위에는 Lorebook VCS의 개정 기록이 있음](../images/phone-lorebook-entry.png)

*Enabled* 체크박스 옆의 버튼을 누르면 로어북 항목을 위한 **Search and replace**가 열립니다. [항목](lorebooks/entries.md#search-and-replace)을 참고하세요.

![휴대폰에서 로어북의 찾기 및 바꾸기](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### 설정, 프로토콜, 제공자, 리소스, 휴지통

- **Settings**는 데스크톱과 같지만, 좁은 열로 배치됩니다. **Import Backup as New Book**은 *Discard Changes* 위의 버튼입니다.
- **Protocols**와 **Inference Providers**는 각 항목을 텍스트로 나열하고, 그 옆(제공자의 경우 그 아래, 제공자가 더 많기 때문)에 버튼이 있습니다. 편집 대화 상자는 데스크톱과 같으므로, 휴대폰에서도 프로토콜을 [공유](protocols.md#sharing-a-protocol)할 수 있으며, 목록 위의 **Import from other user** 버튼으로 다른 사용자가 공유한 프로토콜을 가져올 수 있습니다.
- **Resources**는 파일을 사용하는 객체별로 묶어 보여 줍니다(화살표로 그룹을 펼칩니다). 각 파일은 카드로 표시되며(그림, 이름, 종류, 크기, 날짜, 다운로드·교체·삭제 버튼), *Delete selected*를 위한 체크박스가 있습니다. 그룹을 체크하면 그 안의 모든 파일이 삭제됩니다.
- **Trash**는 삭제된 각 객체를 카드로 보여 줍니다(이름, 종류, 삭제된 시점). 휴지통에 대한 설명은 *i* 버튼 뒤에 있으며, 필터 버튼들은 두 번째 줄로 넘어갑니다. 복원은 [휴지통](trash.md)에 설명된 대로 작동합니다.

![휴대폰의 리소스](../images/phone-resources.png)

![휴대폰의 추론 제공자, 공유된 제공자의 정보 버튼 포함](../images/phone-providers.png)

![휴대폰의 휴지통](../images/phone-trash.png)
<a id="the-book-window"></a>

## 책 창

책을 열면 **전체 화면**으로 표시됩니다. 닫기 버튼은 모서리의 **✕**이고, 탭 *정보*, *프롬프트*, *로어북*, *이야기*, *분기 보기*, *백업*은 한 줄에 놓여 좌우로 스크롤합니다. 데스크톱과 같은 기능을 하며([책](books/index.md) 참고), 입력 필드는 한 열로 배치됩니다.

<a id="the-story"></a>

### 이야기

이야기 탭은 파트를 한 열로 보여 줍니다. [이야기 편집기](books/story-editor.md)와의 차이는 다음과 같습니다.

![The story of a book on a phone](../images/phone-book-story.png)

- **개요**(그리고 [작가의 노트](extensions/authors-note.md)처럼 그 옆에 확장 기능이 추가하는 탭)는 **서랍**에 들어 있습니다. 왼쪽 위 버튼으로 엽니다.
- **요약 목록** 버튼과 설정 메뉴(⚙, 항목은 *텍스트 스타일 변경*, *찾기 및 바꾸기*, *가져오기*, *일괄 삭제*, *이야기 내보내기*)는 아래가 아니라 상단 막대에 있습니다.
- 새 파트를 추가하는 **+** 버튼은 아래쪽에 있습니다.
- **요약 목록** 창의 메뉴 막대(*찾기 및 바꾸기*, *Create summaries...*, *모든 요약 다시 요약*, *요약 내보내기*, *요약 가져오기*)는 별도의 줄에 있습니다. 좁은 화면에서는 항목이 **⋯** 버튼 뒤에 숨습니다. [요약 가져오기](books/summaries.md#export-and-import-of-the-summaries)는 데스크톱과 같은 창이며, 화면 너비에 맞춰 표시됩니다.

  ![Import Summaries on a phone](../images/phone-summaries-import.png)

- [주석](books/annotations.md): **주석** 탭은 개요 옆의 서랍에 있습니다. 밑줄이 그어진 문단을 누르면 그 메모가 열립니다. **파트에 주석 달기**와 **AI로 주석 달기**는 파트의 메뉴에 있고, **선택 영역에 주석 달기**는 선택한 텍스트를 길게 누른 메뉴에 있습니다.
- **찾기 및 바꾸기**는 데스크톱과 같은 창을 열며, 필드와 버튼이 위아래로 배치됩니다.

  ![Search and replace of the story on a phone](../images/phone-search-replace.png)

- 파트는 작은 줄 하나 아래에 있는 텍스트이며, 그 줄에는 아이콘 두 개가 있습니다. 첫 번째는 파트의 **메뉴**입니다(편집, 다시 생성, 스와이프, 분기, 기록, 프롬프트 보기, 이미지, 요약, 삭제). 두 번째는 **팝업**을 엽니다. 데스크톱에서 파트 오른쪽 열에 보이는 내용, 즉 ID, 모델, 프로토콜, 토큰, 단어 수와 턴 세부 정보가 여기에 나옵니다.
- 파트 메뉴의 **기록**은 [버전 기록](books/version-history.md)을 전체 화면 페이지로 엽니다. 버전은 선택한 버전 위에 카드로 놓이며, 데스크톱과 같은 동작을 합니다.

  ![The version history of a part on a phone](../images/phone-history.png)

- **통계**는 이야기의 설정 메뉴(이 책의 통계)에 있고, 책 목록 머리글의 버튼(모든 책의 통계)으로도 열 수 있습니다. 이 창은 전체 화면 페이지이며, 달력은 데스크톱의 1년 대신 최근 16주를 보여 줍니다. [글쓰기 통계](books/statistics.md)를 참고하세요.

  ![The statistics of all books on a phone](../images/phone-statistics.png)

- [요약](books/summaries.md) 개요는 고정된 너비를 유지하며 좌우로 스크롤합니다.

<a id="administration"></a>

## 관리

관리자의 서랍에는 **관리** 탭이 있습니다. [데스크톱 페이지](administration/index.md)와 같은 탭이 한 줄에 놓여 좌우로 스크롤합니다. 탭은 *사용자*, *데이터베이스 백업*, *정리*, *리소스 파일*, *도움말 어시스턴트*, *에이전트*, *업데이트*, *확장 기능*입니다. 목록(사용자, 백업, 확장 기능)은 텍스트 아래에 버튼이 있는 카드이며, 도구 모음은 두 번째 줄로 넘어갑니다. 확장 기능을 설치하는 *Load Extension*과 데이터베이스 백업을 업로드하는 기능은 휴대폰의 파일 선택기를 엽니다. 다른 사용자에게는 이 탭이 보이지 않습니다.

![The users of the Admin tab on a phone](../images/phone-admin-users.png)

![The database backups on a phone](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## 빠진 기능

휴대폰은 데스크톱이 하는 일을 모두 하지만, 다음과 같은 차이가 있습니다.

| 휴대폰에서 없거나 다른 점 | 해당 작업 위치 |
|---|---|
| *루트 로어북만* 체크 박스와, 로어북 항목을 여러 개 선택해 한꺼번에 삭제하는 기능 | 데스크톱 화면. |
| 명령 막대, 키보드 단축키, 다시 생성한 파트의 비교 보기, 여백의 메모 | 데스크톱 화면. 키보드나 넓은 화면이 필요합니다. |
| 열이 많거나 두 개의 패널로 된 창([요약](books/summaries.md) 개요, 베타 리더 대화, 만화 생성기)은 작게 표시됩니다 | 동작은 합니다. 가로 방향이 더 편합니다. |

이 목록에 없는 기능은 모두 데스크톱과 같게 동작합니다. 생성, 편집, 다시 생성, 스와이프, 파트 분기, 요약과 메타 요약, 책 프롬프트, 분기 트리, 책 백업, 내보내기, 리소스, 휴지통, 로어북, 관리 기능이 여기에 해당합니다.

<a id="extensions"></a>

## 확장 기능

휴대폰 화면은 고유한 클래스를 사용하므로, 확장 기능이 이를 명시적으로 지원해야 합니다. Marginalia에 포함된 확장 기능은 다음과 같습니다.

| 확장 기능 | 휴대폰에서 |
|---|---|
| [아크 개요](extensions/arc-outline.md) | 동작합니다. Arcs 탭은 개요 서랍에 있습니다. |
| [작가의 노트](extensions/authors-note.md) | 동작합니다. 노트는 이야기의 개요 서랍에 있습니다. |
| [베타 리더](extensions/beta-readers.md) | 동작합니다. Beta Readers 탭은 개요 서랍에 있으며, 대화 공간이 좁으니 가로 방향이 더 편합니다. |
| [책 소개 생성기](extensions/book-description-generator.md) | 동작합니다. 버튼은 정보 탭 끝에 있고, 대화상자는 옵션을 결과 위에 보여 줍니다. |
| [챕터 마커](extensions/chapter-marker.md) | 동작합니다. 챕터 제목은 개요 서랍에 있습니다. |
| [캐릭터 추적기](extensions/character-tracker.md) | 동작합니다. Characters 탭은 개요 서랍에 있습니다. |
| [스토리 노트](extensions/story-notes.md) | 동작합니다. Notes 탭은 개요 서랍에 있으며, 페이지 대화상자는 화면에 맞춰지고, 긴 페이지는 가로 방향에서 쓰기 편합니다. |
| [콘랑 번역기](extensions/conlang-translator.md) | 일부 동작합니다. *설정 → Extension Settings* 패널, 편집기, 검토, 마법사는 동작하며(어휘 격자는 좌우로 스크롤합니다), 뷰어에서 번역된 대사를 누르는 기능도 동작합니다. 이야기 쪽 기능(**Translate dialogs**, **Translate parts (constructed language)**, 이야기 안의 클릭)은 휴대폰에서 시험하지 않았습니다. [제한 사항](extensions/conlang-translator.md#limits)을 참고하세요. |
| [변환기](extensions/sillytavern-converter.md) | 동작합니다. Converter 탭은 개요 서랍에 있으며, 대화의 범위는 두 번의 탭으로 고릅니다. [휴대폰에서](extensions/sillytavern-converter.md#on-a-phone)를 참고하세요. |
| [지침 내보내기](extensions/instruction-exporter.md) | 동작합니다. **Export instructions**는 이야기의 설정 메뉴에 있습니다. |
| [만화 생성기](extensions/manga-generator.md) | 동작합니다. **Manga Generator**는 이야기의 설정 메뉴에 있으며, 창이 커서 가로 방향이 더 편합니다. |
| [리뷰어](extensions/reviewer.md) | 동작합니다. 항목은 파트의 메뉴에 있고, 설정은 *설정 → Extension Settings*에 있습니다. |
| [사이드 쿼리](extensions/side-query.md) | 동작합니다. Side Query 탭은 개요 서랍에 있습니다. |
| [로어 생성기](extensions/lore-generator.md) | 동작합니다. 마법봉 버튼은 로어북의 모든 항목 줄에 있으며, 창은 결과 위에 프롬프트를 보여 줍니다. |
| [로어북 VCS](extensions/lorebook-vcs.md) | 동작합니다. 로어북의 패널은 *사용* 줄 아래에 있고, 항목의 개정본은 항목 양식 맨 위에 있습니다. |
| [이미지 생성기](extensions/image-generator.md) | 동작합니다. **Generate image**는 파트의 메뉴에 있습니다. |

다른 출처의 확장 기능은 휴대폰에서 다르게 동작하거나 아무것도 추가하지 않을 수 있습니다. 작성자가 동작하도록 만든 곳에 나타납니다. 확장 기능이 보이지 않는다면 데스크톱 화면에서 확인하세요. 확장 기능이 휴대폰을 지원하는 방법은 [개발자 매뉴얼](../developer/plugins/extendable.md#the-phone-twins)에 설명되어 있습니다.

<a id="tips"></a>

## 팁

- 휴대폰에서 긴 글을 쓸 때는 가로 방향이 더 편합니다. 방향을 바꾼 뒤에도 페이지를 새로 고치기 전까지는 휴대폰 화면이 유지됩니다.
- 휴대폰에서 없는 기능이 필요하면 휴대폰을 옆으로 돌리고 페이지를 새로 고치거나, 더 넓은 창에서 Marginalia를 여세요. 책과 설정은 어디서나 같습니다.
- 휴대폰에서 Marginalia에 접속하려면 [서버](getting-started/docker-server.md)로 실행해야 합니다. [데스크톱 앱](getting-started/desktop-app.md)은 컴퓨터에서만 연결을 받습니다.
