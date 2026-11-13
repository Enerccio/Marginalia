---
label: 챕터 마커
order: 20
---

# 챕터 마커

[스토리 편집기](../books/story-editor.md)의 **개요**를 목차로 바꿔 줍니다.

![개요의 챕터](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## 작동 방식

파트의 텍스트에 Markdown 제목(줄의 시작이 `#`인 줄)이 있으면, 해당 파트의 개요 항목은 보통의 `#n (DB ID: m)` 표시 대신 그 제목을 강조하여 보여 줍니다:

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

이 파트는 개요에서 **5. Chapter 3: The Harbour**로 표시됩니다. 숫자는 브랜치에서 파트의 위치이고, 그 뒤에 제목이 옵니다. 제목이 없는 파트는 기본 표시를 그대로 유지합니다.

- 제목은 파트의 어느 줄에나 올 수 있습니다. 여러 개가 있으면 첫 번째 제목을 사용합니다.
- 제목 수준은 어떤 것이든 가능합니다(`#`, `##`, `###`...).
- [편집](../books/story-editor.md#editing-the-text)을 저장하는 즉시 개요가 갱신됩니다. 제목을 추가하면 챕터가 시작되고, 제목을 지우면 파트가 앞 파트와 다시 합쳐집니다.

<a id="usage"></a>

## 사용법

설정할 것은 없습니다. 파트에 제목을 직접 적거나, 지시문에서 모델에게 요청하면 됩니다. 예: *Start a new chapter titled "The Harbour" with a Markdown heading.*

!!!
기본 [사용자 프롬프트](../books/prompts.md#user-prompt)는 모델에게 제목이나 챕터 제목을 쓰지 말라고 지시합니다. 모델이 제목을 쓰기를 원한다면 지시문에 그렇게 명시하거나 사용자 프롬프트의 그 규칙을 바꾸십시오.
!!!

제목은 스토리 텍스트의 일부이므로, 모델에게 전송되고 [뷰어](../books/publishing.md)와 백업에도 표시됩니다.

같은 규칙이 [내보낸 책](../books/export.md#table-of-contents)의 목차에서도 챕터를 찾는 데 사용됩니다.
