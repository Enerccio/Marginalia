---
label: 지시문 내보내기
order: 12
---

# 지시문 내보내기

장면 설정, 시점 인물, 등장인물을 포함한 이야기의 지시문을 클립보드에 복사합니다.

<a id="how-it-works"></a>

## 작동 방식

이 확장 기능은 [스토리 편집기](../books/story-editor.md)의 설정 메뉴(⚙)에 **Export instructions** 항목을 추가합니다.
활성 분기의 부분들을 첫 부분부터 현재 이야기의 끝까지 살펴보고, 각 부분을 생성할 때 사용된 필드를 모아 하나의 텍스트로 복사합니다. 채워진 각 필드는 이름, 콜론, 그리고 다음 줄에 내용 순서로 적힙니다. 부분들은 이야기의 순서대로 빈 줄로 구분됩니다:

![설정 메뉴의 Export instructions 항목](../../images/plugin-instructionexporter.png)

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

비어 있는 필드는 빠지며, 아무 필드도 채워지지 않은 부분도 제외됩니다. 텍스트는 어디에든 붙여넣을 수 있습니다. 책의 줄거리 개요를 보관하거나, 다른 책에서 지시문을 재사용하거나, 다른 사람에게 전달할 때 활용하세요.

<a id="usage"></a>

## 사용법

별도로 설정할 것은 없습니다. 책을 열고 설정 메뉴에서 *Export instructions*를 선택하세요. 텍스트가 클립보드에 복사되었다는 알림이 표시됩니다. 활성 분기에 이 필드들이 하나도 채워져 있지 않으면 아무것도 복사되지 않으며, 그 사실을 알려 줍니다.

활성 분기만 내보내집니다. 다른 분기를 내보내려면 먼저 그 분기로 전환하세요.

!!!
브라우저는 페이지가 `https://`로 열렸거나 `localhost`에서 열렸을 때만 클립보드에 쓰는 것을 허용합니다. 다른 주소의 일반 `http://`에서는 이 항목이 동작하지 않습니다.
!!!
