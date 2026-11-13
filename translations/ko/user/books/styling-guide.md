---
label: 스타일 가이드
order: 54
---

# 스타일 가이드: 책을 책답게 보이게 하기

**텍스트 스타일**은 파트 텍스트의 모습을 결정합니다. 글꼴, 색, 본문 열의 너비, 제목, 장면 사이의 구분선이 여기에 해당합니다. 이 가이드는 첫 변경부터 완성된 모습까지 안내하며, 마지막에는 내려받을 수 있는 완성 예제를 소개합니다. 공포 소설을 위한 스타일로, 모든 옵션과 직접 작성한 CSS를 사용합니다.

스타일이 사용되는 곳은 다음과 같습니다:

| 위치 | 일어나는 일 |
|---|---|
| **스토리 편집기** | 책의 스타일(또는 기본 스타일)이 모든 파트의 모습이 됩니다. [텍스트 스타일](story-editor.md#text-styles)을 참고하세요. |
| **리더** | [출판된 책](publishing.md)은 *독자*가 자신의 스타일 중에서 고른 스타일로 읽힙니다(고르기 전에는 책 스타일). |
| **내보내기** | **내보내기용**으로 표시된 스타일은 [내보내기 대화상자](export.md#print-styles)에서 선택합니다. HTML과 EPUB은 스타일시트를, PDF와 Word는 수치를 사용합니다([각 내보내기가 사용하는 것](#what-the-exports-take) 참고). |

<a id="make-your-first-style"></a>

## 첫 스타일 만들기

1. **설정 → 스타일**을 엽니다([스타일 탭](../account.md#styles)).
2. 목록에서 **Book**을 선택하고 **복제**를 누릅니다. 기본 제공 스타일은 변경할 수 없으므로, 항상 *Book (copy)*라는 이름의 사본에서 시작하게 됩니다. 이름을 정하세요.
3. **옵션** 탭에서 몇 개의 항목을 바꿉니다. 양식 아래의 **미리 보기**에 샘플 텍스트(장 제목, 강조, 인용문, 장면 구분선, 더 작은 제목)가 표시되며, 입력할 때마다 바뀝니다.
4. 설정의 **저장**을 누릅니다. 그 전까지는 아무것도 저장되지 않으며, **변경 사항 취소**를 누르면 작업이 버려집니다.
5. 스토리 편집기에서 설정 메뉴(⚙)를 열고 → **모양** → **텍스트 스타일 변경**을 선택한 뒤 스타일에 체크합니다. 스타일은 책에 저장됩니다. 모든 책에 이 스타일을 쓰려면 목록에서 선택하고 **기본값으로 설정**을 누르세요.

![스토리 메뉴의 스타일들](../../images/story-style-menu.png)

빈 칸으로 둔 항목은 애플리케이션의 모양을 그대로 유지하므로, 스타일은 바꾸고 싶은 것만 지정하면 됩니다. 모든 칸을 비워 둔 스타일은 기본 모양입니다.

<a id="the-options"></a>

## 옵션

모든 칸은 CSS 값을 받습니다. 사용하게 될 단위는 `rem`(페이지 텍스트의 크기, 보통 1rem은 16px), `em`(현재 텍스트의 크기), `px`, `%`이며, 색은 `#336699`, `rgb(51, 102, 153)` 또는 `ivory` 같은 이름으로 씁니다.

<a id="text"></a>

### 텍스트

| 옵션 | 기능 | 예시 |
|---|---|---|
| **Font family** | 텍스트의 글꼴. 목록을 주세요. 컴퓨터에 있는 첫 번째 글꼴이 사용되며, 마지막은 일반 계열(`serif`, `sans-serif`, `monospace`)이어야 합니다. | `"Garamond", "Georgia", serif` |
| **Font size** | 텍스트의 크기. | `1.1rem` |
| **Line height** | 줄 사이의 간격. 단위 없는 숫자는 글꼴 크기의 배수입니다. | `1.7` |
| **Letter spacing**, **Word spacing** | 글자 사이, 단어 사이의 여분 간격. 값을 작게 두세요. 자간을 아주 조금 넓히면 텍스트가 여유롭게 보입니다. | `0.01em`, `0.05em` |
| **Text color** | 텍스트의 색. | `#cfc6bb` |
| **Text align** | `left`, `right`, `center` 또는 `justify`. | `justify` |
| **Hyphenation** | 줄 끝에서 긴 단어를 나눕니다. 책의 [언어](managing-books.md#the-about-tab) 사전을 사용하므로, 먼저 언어를 설정하세요. *justify*와 함께 쓰면 가장 좋습니다. | 켜짐 |
| **Paragraph spacing** | 문단 아래의 간격. 책은 보통 간격을 적게 두며, 첫 줄 들여쓰기를 쓸 때는 간격을 없애기도 합니다. | `0.3em` |
| **First line indent** | 인쇄본처럼 문단의 첫 줄을 들여씁니다. 단, 제목이나 장면 구분선 바로 뒤는 제외합니다. | `1.5em` |
| **Drop cap** | 제목 뒤 첫 문단의 첫 글자를 크게 만듭니다. | 켜짐 |

<a id="page"></a>

### 페이지

| 옵션 | 기능 | 예시 |
|---|---|---|
| **Text width** | 고유한 너비의 가운데 정렬된 열. 한 줄에 60~75자가 가장 읽기 좋으며, 대략 `34em`에서 `40em`입니다. | `38em` |
| **Background color** | 텍스트 뒤의 색. 텍스트 블록만이 아니라 전체 영역을 채웁니다. 그 위에서 잘 읽히는 텍스트 색을 고르세요. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### 강조, 링크, 코드

| 옵션 | 기능 |
|---|---|
| **Emphasis color** | *기울임꼴* 텍스트의 색. |
| **Strong color** | **굵은** 텍스트의 색. |
| **Link color** | 링크의 색. |
| **Code font family** | `code`와 코드 블록의 글꼴. |

<a id="quotes"></a>

### 인용문

| 옵션 | 기능 |
|---|---|
| **Quote color** | 인용문(`> 한 줄`)의 텍스트 색. |
| **Quote border color** | 인용문 옆의 막대 색. |

<a id="headings"></a>

### 제목

| 옵션 | 기능 |
|---|---|
| **Heading font family** | `#`, `##`, `###` 제목의 글꼴. |
| **Heading size** | 첫 단계의 크기. 낮은 단계는 비율에 따라 더 작아집니다. |
| **Heading color** | 제목의 색. |
| **Heading align** | `left`, `center` 또는 `right`. |
| **Heading small caps** | 제목을 스몰캡스로 씁니다. |

<a id="scene-break"></a>

### 장면 구분선

텍스트의 `---` 줄은 장면 사이의 구분선입니다. 기본값은 점 세 개입니다. **Scene break text**는 대신 표시될 문자(`* * *`, `~`, `† † †`)이며, **Scene break color**는 그 색입니다.

<a id="custom-css"></a>

## 사용자 지정 CSS

옵션은 흔한 것들을 다룹니다. 그 밖의 것은 **Custom CSS** 탭에 직접 작성한 CSS로 지정하세요. 이 CSS는 다른 모든 설정 뒤에 추가되며, 스타일 안에서만 적용됩니다. 작성하는 방법은 두 가지입니다:

- **단순 선언**은 텍스트를 담는 영역 전체에 적용됩니다:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **선택자가 있는 규칙**은 앞에 `&`(스타일의 영역)를 붙이거나 생략해서 씁니다. 어느 쪽이든 이 스타일이 적용된 텍스트에만 닿습니다.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

파트의 텍스트는 `chat-message-markdown` 클래스를 가진 요소 안에 있으므로, 텍스트 안의 무언가를 가리키는 선택자는 여기서 시작합니다. CSS 상자 아래의 **Generated CSS** 칸은 옵션들이 만드는 스타일시트를 보여 주며, 그 선택자를 복사하면 무엇을 덮어쓸 수 있는지 확인할 수 있습니다.

| 바꾸고 싶은 것 | 선택자(`& .chat-message-markdown` 뒤) |
|---|---|
| 문단 | `p` |
| 제목 | `h1`, `h2`, `h3` |
| 기울임꼴, 굵은 글씨 | `em`, `strong` |
| 링크 | `a`, `a:hover` |
| 인용문 | `blockquote` |
| 목록 | `ul`, `ol`, `li` |
| 코드 | `code`(인라인), `pre`(블록) |
| 장면 구분선 | `hr`, `hr::after`(기호) |
| 문단의 첫 줄 | `p::first-line` |
| 선택한 텍스트 | `::selection` |

알아 둘 점:

- **중괄호 `{ }`는 짝이 맞아야 합니다**. 그렇지 않으면 저장할 때 스타일이 거부됩니다. 주석 `/* ... */`은 괜찮습니다.
- 애플리케이션에 필요한 몇 가지 규칙은 `!important`로 되어 있습니다(텍스트가 영역보다 넓어지지 않음, 긴 단어가 줄바꿈됨, 문단이 줄바꿈을 유지함). 여러분의 CSS로는 이것들을 덮어쓸 수 없습니다. 그 밖의 모든 것은 덮어쓸 수 있는데, 여러분의 CSS가 마지막에 오기 때문입니다.
- CSS는 보이는 모습을 바꿀 수 있을 뿐, 내용을 바꿀 수는 없습니다. 텍스트 주변의 도구 모음과 탭에는 영향을 주지 않습니다.
- 사용자 지정 CSS는 스토리, 리더, HTML 내보내기에서 사용됩니다. EPUB, PDF, Word는 이를 사용하지 않습니다.

<a id="what-the-exports-take"></a>

## 각 내보내기가 사용하는 것

내보내기용 스타일은 [내보내기 대화상자](export.md#print-styles)에서 선택합니다. 스타일을 **복제**하고 **내보내기용**에 체크하거나, 애플리케이션이 제공하는 네 가지 중 하나에서 시작하세요. 애플리케이션 테마에 따라 달라지는 색(어두운 테마의 밝은 텍스트 등)은 내보낼 때 흰 페이지에 맞는 어두운 색으로 바뀝니다.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| 글꼴, 크기, 줄 높이, 간격, 정렬, 첫 줄 들여쓰기 | ✓ | ✓ | ✓ | ✓ |
| 텍스트, 강조, 굵은 글씨, 인용문, 제목, 장면 구분선의 색 | ✓ | ✓ | ✓ | ✓ |
| 배경색 | ✓ | ✓ | ✓ | ✓ (페이지의 색) |
| 텍스트 너비, 드롭 캡, 하이픈 처리, 제목의 스몰캡스 | ✓ | ✓ | | |
| 사용자 지정 CSS | ✓ | | | |

전자책 리더는 스타일시트가 무엇을 말하든 자체 글꼴, 크기, 색을 자주 사용합니다. PDF와 Word는 해당 형식이 가진 글꼴만 쓸 수 있습니다. PDF는 여러분이 작성한 목록에서 세리프, 산세리프, 고정폭 중 하나를 고르고, Word는 목록의 첫 번째 글꼴 이름을 사용하며 컴퓨터에 그 글꼴이 없으면 자체 글꼴을 보여 줍니다.
<a id="share-a-style-export-and-import"></a>

## 스타일 공유: 내보내기와 가져오기

스타일 목록 아래의 **내보내기**는 선택한 스타일을 파일(`name.style.json`)로 저장합니다. 기본 제공 스타일도 내보낼 수 있습니다. **가져오기**는 이런 파일을 읽어 들입니다. 가져온 스타일은 내 스타일에 새 스타일로 추가되며, **저장**을 누르기 전까지는 보관되지 않습니다. 이름은 그대로 유지되므로, 비슷한 스타일이 이미 있다면 이름을 바꾸세요.

파일은 JSON 형식입니다:

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

한 파일에 여러 스타일을 담을 수 있고, 직접 손으로 작성할 수도 있습니다. 옵션 이름은 대문자로 적습니다(`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...). [호러 예시](#an-example-a-horror-book)에는 모든 옵션이 들어 있습니다. 이 버전의 Marginalia가 알지 못하는 옵션은 제외되고(제외된 개수를 알려 줍니다), 더 새로운 형식 버전의 파일은 거부됩니다. 커스텀 CSS의 중괄호 짝이 맞지 않는 스타일도 거부됩니다.

<a id="an-example-a-horror-book"></a>

## 예시: 호러 책

[호러 스타일 내려받기](../../files/horror-book.style.json)(`horror-book.style.json`)를 받아 *Settings → Styles*에서 가져온 뒤 선택하면 미리 보기에서 확인할 수 있습니다. 이 스타일은 **모든 옵션**을 설정하고, 옵션으로 다룰 수 없는 부분은 커스텀 CSS로 처리합니다:

| | |
|---|---|
| **페이지** | 거의 검은 배경 `#0c0a0a`, `38em` 너비의 단, 가장자리를 조이는 어두운 비네트(커스텀 CSS). |
| **텍스트** | Garamond 서체, 크기 `1.12rem`, 줄 간격 `1.8`, 하이픈을 넣은 양끝 맞춤, `1.6em` 첫 줄 들여쓰기, 문단 사이 간격은 거의 없음. 인쇄된 소설처럼 보이도록 했습니다. 본문은 오래된 종이 같은 회색 `#cfc6bb`이며, 각 제목 뒤에 드롭 캡이 붙습니다. |
| **강조** | 먼지 낀 장밋빛 `#b99a94` 기울임꼴, 피 같은 붉은색 `#b3261e` 굵은 글씨. |
| **링크** | 병약한 초록 `#8fa07e`. 포인터를 올리면 붉은색과 물결 밑줄로 바뀝니다(커스텀 CSS). |
| **인용문** | 어두운 붉은 막대 `#5c1010`과 뒤의 희미한 붉은 얼룩이 있는 회색 텍스트(커스텀 CSS). |
| **제목** | 작은 대문자 Palatino, 가운데 정렬, 어두운 붉은색 `#8e1b17`, 희미한 광채(커스텀 CSS). |
| **장면 전환** | 어두운 붉은색 `† † †`. 목록은 점 대신 십자 표시가 붙습니다(커스텀 CSS). |
| **코드** | Courier New, 거의 검은 배경. |

![호러 스타일로 된 이야기](../../images/story-style-horror.png)

실제 텍스트에 적용해 보려면 아래 내용을 한 파트에 붙여 넣으세요(또는 미리 보기에 비슷한 샘플이 나타납니다):

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

그다음 값을 바꿔 보세요. 배경을 `#14100c`로 바꾸면 촛불 같은 갈색이 됩니다. **텍스트 너비**를 `32em`으로 설정하면 더 좁은 페이지가 됩니다. 커스텀 CSS를 지우면 옵션만으로 어떻게 보이는지 확인할 수 있습니다. 마음에 드는 모양이 나오면 **내보내기**를 눌러 파일을 책과 함께 보관하세요.
