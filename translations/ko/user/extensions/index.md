---
label: 확장 기능
order: 930
icon: plug
---

# 확장 기능

확장 기능은 Marginalia 자체를 바꾸지 않고 기능을 더해 줍니다. Marginalia에는 서른 개의 확장 기능이 기본으로 들어 있습니다:

| 확장 기능 | 추가되는 기능 |
|---|---|
| [xAI Grok](grok.md) | xAI의 Grok 모델을 xAI 자체 API를 통해 추론 제공자로 사용합니다. 추론 과정은 답변과 따로 표시됩니다. |
| [Arc Outline](arc-outline.md) | 사이드바에 책의 아크와 그 챕터를 상태와 함께 보여 줍니다. 모델이 아크를 대신 써 줄 수 있습니다. |
| [Anthropic Claude](anthropic.md) | Anthropic의 Claude 모델을 Anthropic 자체 API를 통해 추론 제공자로 사용합니다. 사고 요약은 추론으로 표시됩니다. |
| [Audiobook](audiobook.md) | 음성 서버를 이용해 책을 오디오북으로 바꿉니다. 내레이터와 각 인물에게 목소리를 지정하고, 최종본은 스튜디오에서 만들며, 챕터 표시가 있는 mp3로 내보냅니다. [음성 서버 설정하기](audiobook-server.md). |
| [Author's Note](authors-note.md) | 책의 모든 생성 요청에 삽입되는, 모델에게 전하는 메모입니다. |
| [Beta Readers](beta-readers.md) | 직접 만든 가상의 독자(페르소나)와 책에 대해 대화합니다. 혼자 또는 여럿이 함께 대화할 수 있습니다. |
| [Book Description Generator](book-description-generator.md) | 이야기의 일부를 바탕으로 모델이 책의 제목과 소개글을 써 줍니다. About 탭의 버튼으로 실행합니다. |
| [Chapter Marker](chapter-marker.md) | 파트의 Markdown 제목에서 가져온 챕터 제목을 스토리 개요에 표시합니다. |
| [Character Tracker](character-tracker.md) | 책의 인물과 그들의 관계, 그리고 그 관계도를 보여 줍니다. |
| [Conlang Translator](conlang-translator.md) | 직접 설명한 인공 언어로 인물의 대사를 표시합니다. 모델을 위해 영어 원문은 뒤에 보존되며, 독자는 클릭하여 원문을 볼 수 있습니다. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | 도구 결과에 다른 역할을 요구하는 구형 Gemma 4 모델에서도 함수 호출이 동작하게 합니다. 최신 채팅 템플릿에서는 필요하지 않습니다. |
| [Google Gemini](google-genai.md) | Google의 Gemini 모델을 Google 자체 API를 통해 추론 제공자로 사용합니다. 사고 요약은 추론으로 표시됩니다. |
| [Image Generator](image-generator.md) | 이야기의 한 부분에 대한 이미지를 만듭니다. 모델이 프롬프트를 쓰고, ComfyUI 또는 OpenAI 방식의 이미지 API가 그림을 그리며, 다시 뽑거나 채택할 수 있습니다. |
| [Prompt History](prompt-history.md) | 세션(그리고 에이전트 실행별)마다 모델에 보낸 최근 요청 20개를 답변과 추론 과정과 함께 보관합니다. 플러그인 디버깅과 프롬프트 점검에 쓰며, 설정 → Extension Settings 또는 에이전트의 콘솔에서 엽니다. |
| [Prompt Inspector](prompt-inspector.md) | 생성 요청의 프롬프트를 보여 주고, 모델에 보내기 전에 확인을 요청합니다. 이야기의 설정 메뉴에서 켜고 끕니다. |
| [Instruction Exporter](instruction-exporter.md) | 이야기의 지침, 장면 설정, 인물을 클립보드에 복사합니다. |
| [Knowledge Tracker](knowledge-tracker.md) | 추적 대상 인물이 알고 있는 것과 모르는 것을 프롬프트에 넣어, 모델이 인물에게 그럴 수 없는 지식을 주지 않도록 합니다. |
| [Lore Generator](lore-generator.md) | 이미 있는 설정을 바탕으로 모델이 로어북 항목의 내용을 써 줍니다. |
| [Lorebook VCS](lorebook-vcs.md) | 로어북 항목의 수정 이력을 보관합니다. |
| [Manga Generator](manga-generator.md) | 이야기의 일부를 페이지와 컷으로 이루어진 만화 대본으로 바꾸고, 이미지 생성용 태그도 만듭니다. |
| [Tools for the model](tools.md) | 생성기의 **Tools** 버튼입니다. 모델이 사용할 수 있는 도구 묶음(이름, 장소)과 그 분류를 정합니다. |
| [Name generator](name-generator.md) | 모델이 이름을 지어내는 대신 쓸 이름들입니다. 마르코프 연쇄로 된 약 1270개의 이름 목록을 제공합니다. |
| [Name provider](name-provider.md) | 모델이 고를 수 있는 실제 이름입니다. 약 100개국의 이름과 성을 원래 철자대로 제공합니다. |
| [Place provider](place-provider.md) | 모델이 찾아볼 수 있는 실제 장소입니다. 도시, 바다, 만, 강, 산 등을 좌표와 국가와 함께 제공하며, 장소 주변의 것들과 두 장소 사이의 거리도 알려 줍니다. |
| [Real Language Translator](real-language-translator.md) | 실제 언어로 인물의 대사를 표시합니다(예: 일본을 배경으로 한 이야기라면 일본어). 모델을 위해 영어 원문은 뒤에 보존되며, 독자는 클릭하여 원문을 볼 수 있습니다. |
| [Reviewer](reviewer.md) | 모델이 쓴 이야기 부분에 대한 평을 제공합니다. 독자의 반응과 비평이 포함됩니다. |
| [Side Query](side-query.md) | 이야기 옆에서 책에 대해 모델과 대화하는 창입니다. |
| [Converter](sillytavern-converter.md) | 이야기 옆에 SillyTavern 채팅을 띄우고, 모델이 채팅을 바탕으로 준비한 턴 세부 사항을 보여 줍니다. 이를 이용해 채팅을 이야기로 다시 쓸 수 있습니다. 또는 이전 책의 파트(백업이나 책 자체에서)와 그 지침을 가져와 다시 고쳐 쓸 수 있습니다. |
| [State Tracker](state-tracker.md) | 추적 대상 인물의 현재 상태(옷, 지닌 물건, 손에 든 무기, 부상, 기분, 또는 직접 정한 항목)를 프롬프트에 넣어, 모델이 그 상태를 지키도록 합니다. |
| [Story Notes](story-notes.md) | 직접 만드는 작은 위키입니다. 페이지에 태그를 달고 페이지끼리 링크할 수 있으며, 사이드바에 표시됩니다. 줄거리, 아이디어, 세계관의 규칙을 적는 데 씁니다. 플롯 마법사와 에이전트는 허용한 페이지를 읽고 바꿀 수 있습니다. |
| [Read Aloud](tts-reader.md) | 뷰어에서 공개된 책을 기기의 음성으로 소리 내어 읽어 줍니다. |
| [Chinese localization](chinese-localization.md) | 사용자 인터페이스의 언어로 중국어 간체를 추가합니다. |

<a id="installing"></a>

## 설치하기

확장 기능은 **관리자**가 Marginalia 설치 전체에 대해 *Admin → Extensions*에서 설치합니다.
확장 기능은 JAR 파일이며, **Load Extension (.jar)**으로 업로드합니다. 자세한 내용은 [확장 기능 관리하기](../administration/extensions.md)를 참고하십시오.

설치가 끝나면 모든 사용자가 확장 기능을 쓸 수 있습니다. Marginalia가 실행 중일 때 불러온 확장 기능은 그 뒤에 열리는 창에 나타납니다. 책을 닫았다가 다시 열거나(또는 페이지를 새로고침하여) 확인하십시오.

!!!warning 버전을 맞추십시오
확장 기능은 특정 Marginalia 버전의 사용자 인터페이스에 연결됩니다. 다른 버전용으로 만든 확장 기능은 제대로 동작하지 않거나 애플리케이션의 일부를 망가뜨릴 수 있습니다. 사용 중인 Marginalia 버전에 포함된 확장 기능을 사용하십시오.
!!!

위의 확장 기능들은 Marginalia 소스 코드(`marginalia/plugins`)의 일부이며 따로 빌드됩니다.
자세한 내용은 [플러그인 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)를 참고하십시오.

<a id="extensions-on-a-phone"></a>

## 휴대폰에서의 확장 기능

[휴대폰 레이아웃](../phone.md)은 자체 클래스를 가지므로, 확장 기능이 이를 지원해야 합니다. Arc Outline, Author's Note, Beta Readers, Chapter Marker, Character Tracker, Instruction Exporter, Reviewer는 지원합니다. Side Query, Lore Generator, Lorebook VCS는 휴대폰에서 아무것도 추가하지 않습니다([휴대폰에서 Marginalia 사용하기](../phone.md#extensions) 참고). Name generator, Name provider, Place provider, Gemma 4 tools fix, Google Gemini에는 자체 화면이 없습니다(Google Gemini는 모델 대화상자에 제공자만 추가하며, 휴대폰에도 이 대화상자가 있습니다). Chinese localization은 휴대폰 메뉴의 언어 선택기에 있습니다. 확장 기능은 휴대폰에는 없는 Admin 페이지에서 설치합니다.

<a id="extension-settings"></a>

## 확장 기능 설정

설정이 있는 확장 기능은 **Settings → Extension Settings**(*Settings* 페이지의 세 번째 탭)에 항목을 추가합니다.
이 설정은 *Settings*의 다른 항목과 마찬가지로 계정에 속하며, 그 **Save** 버튼으로 저장됩니다.

<a id="where-extension-data-is-kept"></a>

## 확장 기능 데이터가 저장되는 곳

확장 기능은 자신이 속한 데이터와 함께 데이터를 보관합니다. 평은 이야기 부분과, 대화는 책과, 수정 이력은 로어북과 함께 저장됩니다. 그래서 이 데이터는 [데이터베이스 백업](../administration/database-backups.md)에 포함됩니다.
[책 백업](../books/backups.md)과 내보내기에 포함되는지는 각 확장 기능의 문서에 설명되어 있습니다.

확장 기능을 제거해도 그 데이터는 삭제되지 않습니다. 같은 확장 기능을 다시 설치하면 데이터가 돌아옵니다.
