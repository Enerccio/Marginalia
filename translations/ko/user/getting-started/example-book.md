---
label: 예제 책
order: 80
---

# 예제 책

*Unregistered*는 열어서 읽고 뜯어볼 수 있도록 완성된 책입니다. 하나의 이야기가 약 100개 파트(약 99,000단어)로 이루어져 있으며, Marginalia의 기능들이 남긴 흔적이 모두 들어 있습니다. 긴 책이 [스토리 편집기](../books/story-editor.md), [요약](../books/summaries.md), [주석](../books/annotations.md), [통계](../books/statistics.md)에서 어떻게 보이는지 살펴보거나, 이미 있는 자료에 확장 기능을 시험해 보기에 좋습니다.

> 체코 지하실의 와인 안내원이자 소설로 일본어를 배운 사람과, 휴가 중인 일본인 간호사가 프라하 지하실의 벽돌로 막힌 아치를 지나 Vrakht어를 쓰는 제국이 다스리는 강 계곡에서 깨어납니다.
> 두 사람 모두 그 언어를 모르고, 생활세 징수관은 그의 장부에 없는 사람 둘을 발견합니다.

<a id="what-is-in-it"></a>

## 무엇이 들어 있나요

| | |
|---|---|
| **이야기** | 본선 100개 파트와 3개 파트로 된 곁가지, 총 32개 챕터(제목은 본문에 있습니다). |
| **요약** | 모든 챕터에 대한 요약 한 개씩(길이에 상관없이, 25개 파트마다 마지막 챕터는 모델이 원문 그대로 읽도록 통째로 남겨 둡니다)과, 가장 오래된 요약들을 합치는 메타 요약. 기본 [요약 프롬프트](../books/summaries.md)의 간결한 형식을 따릅니다. |
| **주석** | 책의 *AI로 주석 달기* 기능이 제안하고 재작성에서 반영된 약 450개의 검토 메모(연속성, 문체, 설정, 시점, 전개 속도). 모든 파트에는 버전도 있으며, 전체 360개입니다([버전 기록](../books/version-history.md) 참고). |
| **로어북** | 31개 항목: 세계, 장소, 사람들, 언어. |
| **프롬프트** | 각 파트는 자신이 작성된 프롬프트와 활성화된 로어를 함께 보관합니다. 파트 메뉴의 **프롬프트 보기**, **프롬프트 비교**, **로어 사용 현황**에서 확인할 수 있습니다([책 프롬프트](../books/prompts.md)). |
| **지식 추적기** | 추적 대상 인물 7명과, 각 인물이 파트마다 무엇을 알고 무엇을 모르는지([지식 추적기](../extensions/knowledge-tracker.md)). |
| **번역된 대사** | 약 1,250개의 번역된 대사. 제국의 **Vrakht**어와 계곡의 **Aethel-Vael**어([Conlang Translator](../extensions/conlang-translator.md)), 그리고 **일본어**와 **체코어**([Real Language Translator](../extensions/real-language-translator.md)). |
| **챕터** | 본문에서 [Arc Outline](../extensions/arc-outline.md)이 만든 이야기의 개요. |
| **삽화** | 파트 31에 있는 삽화 한 점. |

네 가지 확장 기능의 데이터도 책 안에 들어 있지만, 해당 확장 기능을 불러와야만 볼 수 있습니다. 불러오지 않으면 데이터는 보존되고 무시됩니다. 이 책에는 모델이나 프로토콜이 지정되어 있지 않으므로, 가져온 뒤 책의 설정에서 직접 선택하세요.

<a id="download"></a>

## 내려받기

파일은 릴리스 [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1)에 첨부되어 있습니다.

| 파일 | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5.7 MB) | 책 본문. 삽화가 포함된 백업입니다. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | 책의 로어북만 따로 담은 파일. 책 파일에도 로어북이 들어 있으므로, 책 없이 로어북만 보고 싶을 때만 이 파일을 받으면 됩니다. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | 제국의 인공 언어. Conlang Translator용입니다. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | 계곡의 인공 언어. Conlang Translator용입니다. |

<a id="importing-it"></a>

## 가져오기

1. **언어**(Conlang Translator를 쓴다면): **설정 → 확장 설정 → Conlang Translator**에서 **가져오기**를 누르고 `vrakht.json`을 고른 뒤, 같은 방법으로 `aethel-vael.json`도 가져옵니다([Conlang Translator](../extensions/conlang-translator.md#languages)). 책의 언어 설정이 가리킬 대상이 먼저 있어야 하므로 책보다 먼저 가져오세요.
2. **책:** 책 창의 *설정* 탭에서 **새 책으로 백업 가져오기**를 누르고 이름을 입력한 뒤 `unregistered-book.zip`을 올립니다([백업을 가져오는 방법](../books/backups.md#importing-a-backup-as-a-new-book)). Marginalia가 로어북에 대해 물으면 **가져오기**를 선택하세요. 로어북은 파일 안에 들어 있습니다.
3. 새 책을 열고 설정에서 사용할 모델과 프로토콜을 고릅니다.

번역된 대사는 책 안에 저장되어 있으므로, Vrakht어, Aethel-Vael어, 일본어, 체코어 대사는 번역된 그대로 표시됩니다. Real Language Translator로 더 번역하려면 그곳에서 *Japanese*와 *Czech* 언어를 만드세요(플러그인이 사전 설정으로 제공합니다). 플러그인은 언어를 책과 함께 내보내지 않기 때문입니다.

<a id="where-it-comes-from-and-the-licence"></a>

## 출처와 라이선스

이야기, 로어북, 두 언어는 모두 독창적으로 만들어졌습니다. 개요, 로어북, 언어, 파트 계획은 Marginalia의 저자가 작성했습니다. 파트의 본문은 **AI 모델**(Claude Haiku)이 Marginalia 자체의 생성 루프를 통해 작성했습니다. 생성, 주석, 재작성, 지식 추적, 번역, 요약의 순서로 진행되었습니다. 이 책은 프로그램을 보여 주기 위한 시연용이며 출간된 소설이 아니고, 모델 문장 특유의 버릇이 그대로 남아 있습니다.

- 본문, 로어북, 두 언어는 [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)에 따라 공유 저작물로 기증되어 있으므로 어떤 용도로든 자유롭게 사용할 수 있습니다.
- 삽화는 Willem Adriaan Alexander Liernur(1887)의 *Interior of a Cottage in Scheveningen*이며, 공유 저작물로서 [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art)에서 가져왔습니다.
