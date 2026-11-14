---
label: 확장 기능
order: 930
icon: plug
---

# 확장 기능

확장 기능은 Marginalia 자체를 바꾸지 않고 기능을 추가합니다. Marginalia에는 23개의 확장 기능이 함께 제공됩니다:

| 확장 기능 | 추가되는 것 |
|---|---|
| [Arc Outline](arc-outline.md) | 사이드바에 책의 아크와 그 챕터들, 그리고 각각의 상태가 표시됩니다. 모델이 아크를 대신 써 줄 수 있습니다. |
| [Anthropic Claude](anthropic.md) | Anthropic 자체 API를 통해 제공되는 추론 제공자로서의 Anthropic Claude 모델이며, 사고 요약이 추론으로 표시됩니다. |
| [Audiobook](audiobook.md) | 음성 서버를 이용해 책을 오디오북으로 만듭니다. 내레이터와 각 캐릭터에 목소리를 지정하고, 최종본은 스튜디오에서 작업하며, 챕터 표시가 포함된 mp3로 내보낼 수 있습니다. [음성 서버 설정하기](audiobook-server.md). |
| [Author's Note](authors-note.md) | 책의 모든 생성 과정에 삽입되는, 모델에게 전하는 메모입니다. |
| [Beta Readers](beta-readers.md) | 직접 만든 독자(페르소나)와, 책에 대해 그들과 나누는 대화(혼자 또는 여럿이 함께)입니다. |
| [Book Description Generator](book-description-generator.md) | 이야기의 일정 범위를 바탕으로 모델이 책의 제목과 설명을 작성합니다. About 탭의 버튼으로 실행합니다. |
| [Chapter Marker](chapter-marker.md) | 파트 안의 Markdown 제목을 바탕으로 스토리 개요에 챕터 제목을 붙입니다. |
| [Character Tracker](character-tracker.md) | 책의 등장인물과 그들 사이의 관계, 그리고 그 관계도입니다. |
| [Conlang Translator](conlang-translator.md) | 여러분이 설명한 인공 언어로 캐릭터들의 대사를 옮깁니다. 모델을 위해 영어 원문이 뒤에 보존되고, 독자는 클릭해서 원문을 볼 수 있습니다. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | 도구 결과에 다른 역할을 요구하는 Gemma 4 모델에서도 함수 호출이 작동하도록 합니다. |
| [Google Gemini](google-genai.md) | Google 자체 API를 통해 제공되는 추론 제공자로서의 Google Gemini 모델이며, 사고 요약이 추론으로 표시됩니다. |
| [Image Generator](image-generator.md) | 이야기의 한 부분에 맞는 이미지를 만듭니다. 모델이 프롬프트를 쓰고, ComfyUI 또는 OpenAI 방식의 이미지 API가 그림을 그리며, 여러분은 다시 생성하거나 채택할 수 있습니다. |
| [Prompt History](prompt-history.md) | 세션(그리고 에이전트 실행별로) 모델에 보낸 마지막 요청 20개를 응답 및 추론과 함께 보관합니다. 플러그인 디버깅과 프롬프트 점검에 쓰며, 설정 → 확장 기능 설정 또는 에이전트 콘솔에서 엽니다. |
| [Prompt Inspector](prompt-inspector.md) | 생성 과정의 프롬프트를 보여 주고, 모델에 보내기 전에 확인을 요청합니다. 이야기의 설정 메뉴에서 켜고 끕니다. |
| [Instruction Exporter](instruction-exporter.md) | 이야기의 지시문, 장면 설정, 등장인물을 클립보드로 복사합니다. |
| [Knowledge Tracker](knowledge-tracker.md) | 추적 대상 캐릭터가 알고 있는 것과 모르는 것을 정리해 프롬프트에 넣습니다. 모델이 캐릭터에게 가질 수 없는 지식을 주지 않도록 돕습니다. |
| [Lore Generator](lore-generator.md) | 이미 가지고 있는 설정을 바탕으로 모델이 로어북 항목의 내용을 작성합니다. |
| [Lorebook VCS](lorebook-vcs.md) | 로어북 항목의 수정 기록입니다. |
| [Manga Generator](manga-generator.md) | 이야기의 일부를 페이지와 컷으로 이루어진 만화 원고로, 그리고 이미지 생성 태그로 바꿉니다. |
| [Tools for the model](tools.md) | 생성기의 **도구** 버튼입니다. 모델이 사용할 수 있는 도구 모음(이름, 장소)과 그 범주를 정합니다. |
| [Name generator](name-generator.md) | 모델이 이름을 지어내는 대신 쓸 수 있는 이름들입니다. 마르코프 체인으로 된 약 1270개의 이름 목록을 제공합니다. |
| [Name provider](name-provider.md) | 모델이 고를 수 있는 실제 이름들입니다. 약 100개 국가의 이름과 성을 원래 철자대로 제공합니다. |
| [Place provider](place-provider.md) | 모델이 찾아볼 수 있는 실제 장소들입니다. 도시, 바다, 만, 강, 산 등을 좌표와 국가와 함께 제공하며, 어떤 장소 주변에 무엇이 있는지와 두 장소 사이의 거리도 알려 줍니다. |
| [Real Language Translator](real-language-translator.md) | 실제 언어(예: 일본을 배경으로 한 이야기라면 일본어)로 캐릭터들의 대사를 옮깁니다. 모델을 위해 영어 원문이 뒤에 보존되고, 독자는 클릭해서 원문을 볼 수 있습니다. |
| [Reviewer](reviewer.md) | 모델이 쓴 이야기 파트에 대한 검토입니다. 독자 반응과 비평을 제공합니다. |
| [Side Query](side-query.md) | 이야기 옆에서 책에 대해 모델과 나누는 대화입니다. |
| [Converter](sillytavern-converter.md) | 이야기 옆에 SillyTavern 대화를 띄우고, 모델이 그 대화로부터 준비한 턴 세부 정보를 보여 줘서 대화를 이야기로 다시 쓰게 합니다. 또는 오래된 책(백업이나 책 자체에서 가져온)의 파트들을 지시문과 함께 불러와 다시 고쳐 쓰게 합니다. |
| [Story Notes](story-notes.md) | 사이드바에 있는 작은 위키입니다. 여러분이 직접 쓰는 페이지들(태그, 페이지 간 링크 포함)로 줄거리, 아이디어, 세계관의 규칙을 정리합니다. 플롯 마법사와 에이전트는 여러분이 허용한 페이지를 읽고 바꿀 수 있습니다. |
| [Read Aloud](tts-reader.md) | 게시된 책을 뷰어에서 여러분 기기의 목소리로 소리 내어 읽어 줍니다. |
| [Chinese localization](chinese-localization.md) | 사용자 인터페이스의 언어로서의 간체 중국어입니다. |

<a id="installing"></a>

## 설치하기

확장 기능은 Marginalia 설치 전체를 위해 **관리자**가 *관리 → 확장 기능*에서 설치합니다.
확장 기능은 JAR 파일이며, **확장 기능 불러오기 (.jar)**로 업로드합니다. 자세한 내용은 [확장 기능 관리하기](../administration/extensions.md)를 참고하세요.

설치가 끝나면 확장 기능은 모든 사용자가 사용할 수 있습니다. Marginalia가 실행 중일 때 불러온 확장 기능은 그 뒤에 열리는 창에 나타납니다. 확인하려면 책을 닫았다가 다시 열거나(또는 페이지를 새로 고치세요) 됩니다.

!!!warning 버전을 맞추세요
확장 기능은 특정 Marginalia 버전의 사용자 인터페이스에 연결됩니다. 다른 버전용으로 만들어진 확장 기능은 작동을 멈추거나 애플리케이션의 일부를 망가뜨릴 수 있습니다. 사용 중인 Marginalia 버전에 함께 제공되는 확장 기능을 쓰세요.
!!!

위의 확장 기능들은 Marginalia 소스 코드(`marginalia/plugins`)의 일부이며 별도로 빌드됩니다. 자세한 내용은 [플러그인 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)를 참고하세요.

<a id="extensions-on-a-phone"></a>

## 휴대폰에서의 확장 기능

[휴대폰 레이아웃](../phone.md)은 자체 클래스를 가지고 있으므로, 확장 기능도 이를 지원해야 합니다. Arc Outline, Author's Note, Beta Readers, Chapter Marker, Character Tracker, Instruction Exporter, Reviewer는 지원합니다. Side Query, Lore Generator, Lorebook VCS는 휴대폰에서 아무것도 추가하지 않습니다([휴대폰에서 Marginalia 사용하기](../phone.md#extensions) 참고). Name generator, Name provider, Place provider, Gemma 4 tools fix, Google Gemini에는 자체 화면이 없습니다(Google Gemini는 모델 대화상자에 제공자만 추가하며, 휴대폰에도 이 대화상자가 있습니다). Chinese localization은 휴대폰 메뉴의 언어 선택기에 있습니다. 확장 기능은 휴대폰에는 없는 관리자 페이지에서 설치합니다.

<a id="extension-settings"></a>

## 확장 기능 설정

설정이 있는 확장 기능은 **설정 → 확장 기능 설정**(*설정* 페이지의 세 번째 탭)에 섹션을 추가합니다. 이 설정들은 *설정*의 나머지 항목과 마찬가지로 여러분의 계정에 속하며, **저장** 버튼으로 저장됩니다.

<a id="where-extension-data-is-kept"></a>

## 확장 기능 데이터가 보관되는 곳

확장 기능은 자신의 데이터를 그 데이터가 속한 곳과 함께 보관합니다. 검토는 이야기 파트와, 대화는 책과, 수정 기록은 로어북과 함께 저장됩니다. 따라서 이 데이터는 [데이터베이스 백업](../administration/database-backups.md)에 포함됩니다. [책 백업](../books/backups.md)과 내보내기에도 포함되는지는 각 확장 기능의 페이지에 설명되어 있습니다.

확장 기능을 제거해도 그 데이터는 삭제되지 않습니다. 다시 설치하면 데이터도 돌아옵니다.
