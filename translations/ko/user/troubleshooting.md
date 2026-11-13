---
label: 문제 해결 및 FAQ
order: 900
icon: question
---

# 문제 해결 및 FAQ

<a id="where-are-the-logs"></a>

## 로그는 어디에 있나요?

대부분의 문제는 로그에 흔적을 남깁니다:

| 설치 방식 | 로그 |
|---|---|
| 데스크톱 앱 | `~/.marginalia/desktop/logs/marginalia.log` (Windows에서는 `%USERPROFILE%\.marginalia\desktop\logs`); 이전 실행 기록은 `marginalia.log.1`에 있습니다 |
| Docker | `docker compose logs -f marginalia` |

애플리케이션의 오류 대화상자(*Internal Server Error*)에도 오류가 표시되지만, 전체 세부 정보는 로그에 있습니다.

문제를 보고할 때는 페이지 왼쪽 아래 모서리의 버전 문자열을 함께 적어 주세요(예: `Marginalia 1.5.0 (93ec118)`: 빌드의 버전과 Git 커밋).

<a id="installing-and-starting"></a>

## 설치 및 시작

**macOS에서 앱이 "손상되었다"거나 "열 수 없다"고 나옵니다.**
앱에 아직 서명이 되어 있지 않습니다. 앱을 우클릭하고 *열기*를 선택하거나, `xattr -dr com.apple.quarantine /Applications/Marginalia.app` 명령을 실행하세요. [데스크톱 앱](getting-started/desktop-app.md)을 참고하세요.

**Windows SmartScreen이 앱을 차단합니다.**
*추가 정보* → *실행*을 클릭하세요.

**브라우저가 8765가 아닌 다른 포트로 열립니다.**
8765 포트가 이미 사용 중이어서 데스크톱 앱이 빈 포트를 골랐습니다. 트레이 메뉴에서 **Open Marginalia**를 사용하거나, `--port` 옵션을 주고 실행해 포트를 고정하세요.

**"Marginalia did not start in time".**
데스크톱 앱이 서버를 시작하지 못했습니다. 로그에 원인이 나와 있습니다. 파일을 점유한 다른 프로그램이거나 망가진 확장 기능인 경우가 많습니다([아래](#extensions) 참고).

**Docker: 8080 포트가 이미 사용 중입니다.**
`docker-compose.yml`에서 포트 매핑의 왼쪽 값을 바꾸세요. 예: `"8081:8080"`, 그리고 8081 포트를 여세요.

**Marginalia가 느리거나 "OutOfMemoryError"와 함께 멈춥니다.**
메모리를 늘리세요. `docker-compose.yml`의 `JAVA_TOOL_OPTIONS`에서 `-Xmx2g`(또는 더 큰 값)를 설정합니다.

<a id="logging-in"></a>

## 로그인

**"로그인 상태 저장"을 켜도 로그인이 유지되지 않습니다.**
로그인 상태 저장은 서버에서 HTTPS를 사용해야 작동합니다. Marginalia를 HTTPS를 지원하는 리버스 프록시 뒤에 두세요. [Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)를 참고하세요.

**비밀번호를 잊어버렸습니다.**
관리자가 새 비밀번호를 설정하거나, 비밀번호를 비워서 빈 비밀번호로 로그인한 뒤 직접 새 비밀번호를 설정하도록 할 수 있습니다. *관리 → 사용자*에서 처리하세요([잊어버린 비밀번호 재설정](administration/users.md#resetting-a-forgotten-password) 참고). 이메일을 통한 비밀번호 재설정은 제공하지 않습니다.

**더 이상 관리자로 로그인할 수 없습니다.**
관리자가 없으면 데이터베이스에서 직접 비밀번호를 재설정해야 합니다:

1. Marginalia를 중지하고, [데이터 폴더](administration/index.md#the-data-folder)의 `marginalia.sqlite`를 백업으로 복사해 두세요.
2. `sqlite3` 도구로 관리자의 비밀번호를 제거하세요:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (사용자 이름은 실제 값에 맞게 바꾸세요.)
3. Marginalia를 시작하고, 해당 사용자 이름과 **빈** 비밀번호로 로그인한 뒤, 즉시 **비밀번호 변경**에서 새 비밀번호를 설정하세요.

**"서버 연결이 끊어졌습니다"라고 나오거나 페이지가 계속 다시 로드됩니다.**
리버스 프록시 뒤에서 실행한다면, 프록시가 WebSocket을 전달하고 긴 요청을 허용해야 합니다. nginx 예시는 [Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)를 참고하세요. 데스크톱 앱이라면 앱이 종료되었을 가능성이 큽니다. 다시 시작하세요.

**작업 공간을 열면 브라우저가 멈춥니다.**
Marginalia는 나갔을 때 열어 두었던 항목을 다시 엽니다. 그래서 매우 긴 책(예: 아카이브에서 가져온 책)은 무언가를 바꾸기도 전에 페이지를 멈추게 할 수 있습니다. Marginalia 주소의 `/reset`을 열어 보세요(예: `https://marginalia.example.com/reset`) 그리고 로그인하세요. 저장된 작업 공간 상태(열린 책, 선택된 탭, 필터)가 잊혀지고 빈 작업 공간으로 이동합니다. 그다음 책을 다시 열기 전에 [계정](account.md)에서 **이야기에서 한 번에 불러오는 파트 수**를 설정하세요.

<a id="models-and-generation"></a>

## 모델 및 생성

**"모델 목록을 다운로드하지 못했습니다".**
URL이 `/v1`로 끝나는지, API 키가 맞는지, 모델 서버가 실행 중인지 확인하세요. 일부 서비스는 모델 목록을 제공하지 않으므로, 그럴 때는 모델 ID를 직접 입력하세요. [추론 제공자](inference-providers.md#per-type-settings)를 참고하세요.

**Docker: 같은 컴퓨터의 모델 서버에 연결할 수 없습니다.**
컨테이너 안에서 `localhost`는 컨테이너 자신을 가리킵니다. `http://host.docker.internal:<port>/v1`을 사용하고, 모델 서버가 모든 주소에서 수신하도록 설정하세요. [같은 컴퓨터의 모델](getting-started/docker-server.md#models-on-the-same-machine)을 참고하세요.

**"Book is missing model." / "Book is missing protocol."**
책의 *정보* 탭에서 모델과 프로토콜을 선택하세요. 새 책에 자동으로 적용되도록 *설정*에서 *기본 모델*과 *기본 프로토콜*을 지정하세요.

**"Contextual limit not sufficient."**
이야기 없이도 프롬프트가 이미 응답 뒤에 남는 공간보다 큽니다. 제공자의 *최대 컨텍스트 크기*(또는 프로토콜의 *최대 컨텍스트 토큰*)를 늘리거나, *최대 응답 토큰*을 줄이거나, 템플릿, 로어북 항목, 요약을 줄이세요. [제한](protocols.md#limits)을 참고하세요.

**모델이 컨텍스트가 너무 길다고 보고합니다.**
Marginalia의 토큰 수 계산은 일부 모델에서 조금 부정확합니다. *최대 컨텍스트 크기*를 몇백 토큰 정도 낮추세요. 프롬프트가 이미 너무 커 보이면 요청 전에 추정 크기를 알려 주는 경고가 나타납니다.

**"The provider did not answer in time", "The provider is limiting requests", "The provider rejected the API key"...**
Marginalia는 모델 API의 실패 원인을 쉬운 말로 설명합니다. [오류 메시지](inference-providers.md#error-messages)를 참고하세요. 제공자 대화상자의 *연결 테스트*는 책 없이도 URL, 키, 모델을 확인합니다. 시간 제한과 재시도는 제공자별로 설정합니다.

**텍스트가 문장 중간에서 멈춥니다.**
응답 길이 제한에 도달한 것입니다. *최대 응답 토큰*(또는 프로토콜의 *최대 응답 토큰*)을 늘리세요. 추론 모델은 추론 과정도 토큰으로 계산되므로 훨씬 더 많은 값이 필요합니다.

**추론을 켜면 요청이 실패합니다.**
API가 `reasoning_effort`를 지원하지 않습니다. 제공자에서 *추론(Reasoning) 사용*을 끄세요.

**"The summaries of the messages with IDs ... are outdated ... Continue generation and delete them?"**
요약이 적용된 파트를 편집하거나 삭제했습니다. *예*를 선택하면 오래된 요약을 삭제하고 계속 생성한 뒤 새 요약을 만듭니다. *아니요*를 선택하면 중단하고 요약을 유지하는데, 요약 개요에서 표시되며 여전히 맞는 요약은 다시 검증할 수 있습니다. [요약이 오래되었을 때](books/summaries.md#when-summaries-become-outdated)를 참고하세요.

**프롬프트에 "Error"라는 단어가 들어 있습니다.**
템플릿이 존재하지 않는 변수나 매크로를 사용하고 있습니다. 대개 `instructions` 대신 `instruction`처럼 오타가 난 경우입니다. 템플릿 필드 아래에 이런 이름들이 나열됩니다. [오타](templates/templates.md#typos)를 참고하세요.

<a id="lorebooks"></a>

## 로어북

**항목이 사용되지 않습니다.** [활성화](lorebooks/entries.md#activation)의 조건을 하나씩 확인하세요:

- 항목이 켜져 있나요? 그 항목이 속한 로어북과, 그에 이르는 상위 하위 로어북이 모두 켜져 있나요?
- 항목에 태그가 있나요? 그렇다면 책에도 그중 하나의 태그가 있어야 합니다(책의 태그는 *정보* 탭에 있습니다). 부정 태그는 항목을 제외합니다.
- 로어북 자체에 **태그**가 있나요? 이 태그는 그 안의 모든 항목에 적용됩니다.
- 필터가 있나요? 필터는 **작성 중인 파트의 지시문만** 보며, 이전 이야기는 보지 않습니다. 캐릭터를 *등장인물* 또는 지시문에 언급하세요.
- 필터가 정규 표현식인가요? 잘못된 정규 표현식은 절대 일치하지 않습니다.

생성된 파트의 **프롬프트 보기**에서 어떤 로어가 전송되었는지 확인할 수 있습니다.

<a id="prompts-and-settings"></a>

## 프롬프트 및 설정

**다른 탭으로 전환할 수 없습니다.**
*설정* 탭에 저장되지 않은 변경 사항이 있습니다. **저장** 또는 **변경 사항 취소**를 클릭하세요.

**요약 프롬프트가 적용되지 않습니다.**
책 자체의 요약 프롬프트(*프롬프트* 탭)가 *설정*의 *기본 요약 프롬프트*보다 우선합니다. 기본값을 쓰려면 책의 입력란을 비우세요. 메타 요약에는 별도의 프롬프트인 *기본 메타 요약 프롬프트*가 있습니다.

**스타일, 시점 또는 시제의 매크로가 작동하지 않습니다.**
이 필드들은 일반 텍스트이며, 매크로를 처리하는 것은 템플릿뿐입니다. 매크로를 마스터 템플릿이나 사용자 프롬프트에 넣으세요.

<a id="backups"></a>

## 백업

**데이터베이스 백업을 복원했는데 아무것도 바뀌지 않았습니다.**
복원은 다음 시작 시 적용됩니다. Marginalia를 다시 시작하세요. [복원](administration/database-backups.md#restoring)을 참고하세요.

**다른 컴퓨터로 옮긴 뒤 책 백업이 보이지 않습니다.**
책 백업은 데이터베이스의 일부가 아니라 파일입니다. `data` 폴더도 함께 복사하세요. [데이터 폴더](administration/index.md#the-data-folder)를 참고하세요.

<a id="extensions"></a>

## 확장 기능

**설치한 확장 기능이 보이지 않습니다.**
책을 닫았다가 다시 열거나, 페이지를 새로 고치세요.

**확장 기능이 로드되지 않고 *관리 → 확장 기능*에도 나타나지 않습니다.**
Marginalia를 중지하고, `extensions` 폴더에서 해당 JAR 파일을 삭제한 뒤 다시 시작하세요. [확장 기능이 실패할 때](administration/extensions.md#when-an-extension-fails)를 참고하세요.

**Marginalia를 업데이트한 후 확장 기능이 거부되거나 작동을 멈췄습니다.**
확장 기능은 특정 버전의 Marginalia용으로 빌드되며, 로드할 때 검사됩니다. 보고서(*관리 → 확장 기능 → *검증**)를 열어 보고, Marginalia 버전에 맞는 확장 기능 버전을 로드하세요. [확장 기능이 실패할 때](administration/extensions.md#when-an-extension-fails)를 참고하세요.

**확장 기능을 업데이트한 뒤에도 이전 버전이 계속 실행됩니다.**
먼저 이전 버전을 언로드한 다음 새 버전을 로드하거나, Marginalia를 다시 시작하세요.
<a id="faq"></a>

## 자주 묻는 질문

**내 글이 어디로든 전송되나요?**
설정한 추론 제공자(inference provider)로만 전송됩니다. 파트별 프롬프트(및 토큰 계산 요청)는 해당 제공자의 API로 보내집니다. 내 컴퓨터에서 실행되는 모델을 쓰면 아무것도 밖으로 나가지 않습니다. Marginalia는 텔레메트리를 수집하지 않습니다.

**어떤 모델을 써야 하나요?**
글을 잘 쓰고 컨텍스트가 충분히 큰 모델이면 됩니다. 긴 책에는 32 000 토큰 이상의 컨텍스트가 여유 있습니다. 같은 책으로 여러 모델을 시험해 보세요. 제공자를 바꾸는 것은 *정보* 탭에서 한 번 클릭하면 됩니다.

**여러 사람이 한 책에 함께 작업할 수 있나요?**
아니요. 모든 책은 한 사용자에게 속합니다. 다른 사용자는 책이 [게시된](books/publishing.md) 뒤에 읽을 수 있습니다.

**Marginalia를 오프라인으로 사용할 수 있나요?**
네, 로컬에서 실행되는 모델(llama.cpp, LM Studio, Ollama 등)을 쓰면 됩니다. Marginalia 자체는 인터넷 연결이 필요하지 않습니다.

**책을 Marginalia 밖으로 꺼내려면 어떻게 하나요?**
*이야기* 탭의 설정 메뉴(⚙)에서 [이야기를 내보내기](books/export.md)를 하면 텍스트, HTML, Word, PDF, EPUB 형식으로 저장할 수 있습니다.
모든 분기를 포함해 전부 보관하려면 [책 백업을 내보내세요](books/backups.md#exporting-and-importing)(JSON).
[뷰어](books/publishing.md)에서 읽을 수도 있습니다.

**휴대폰에서도 작동하나요?**
[뷰어](books/publishing.md)는 휴대폰용으로 만들어졌고, Marginalia는 브라우저 메뉴의 *앱 설치* / *홈 화면에 추가*로 앱처럼 홈 화면에 추가할 수 있습니다. 브라우저 창이 768픽셀보다 좁으면 편집기는 [휴대폰 레이아웃](phone.md)으로 바뀝니다. 관리자 페이지가 없고, 로어북 편집기가 축소되며, 모든 확장 기능을 지원하지는 않으므로 글쓰기와 읽기에 가장 적합합니다. 설정과 관리는 더 큰 화면에서 하는 것이 더 편합니다.

**밝은 테마나 다른 언어가 있나요?**
둘 다 있습니다. 언어 선택기 옆의 해/달 버튼으로 테마를 바꿀 수 있습니다(기본은 어두운 테마). 언어 선택기에는 일본어와 확장 기능이 추가하는 언어들이 나옵니다([계정](account.md#theme) 참고).
