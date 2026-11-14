---
label: 홈
icon: home
order: 1000
---

# Marginalia

Marginalia는 대규모 언어 모델과 함께 장편 소설을 공동 집필하기 위한 자체 호스팅 글쓰기 도구입니다. 이야기의 각 부분에 대한 지시문을 작성하면 모델이 본문을 쓰고, Marginalia는 그 주변의 모든 것을 정돈된 상태로 유지합니다. 여기에는 분기되는 이야기, 세계관 설정, 모델의 컨텍스트 안에 긴 책을 담아 두는 요약, 문체를 결정하는 프롬프트 설정이 포함됩니다.

이 도구는 브라우저에서 실행됩니다. 자신의 컴퓨터에서 데스크톱 앱으로 쓰거나, 자신과 친구들을 위한 작은 서버로 운영할 수 있습니다. OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM 등 OpenAI 호환 API를 지원하는 모든 서비스와 함께 동작합니다.

![이야기 편집기](images/hero-story-editor.png)

[!button variant="primary" text="시작하기"](user/getting-started/index.md)
[!button text="다운로드"](https://github.com/Enerccio/Marginalia/releases)
[!button text="GitHub에서 소스 보기"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## 주요 기능

- **분기되는 이야기가 있는 책** – 부분을 다시 생성하거나 스와이프하고, 임의의 지점에서 분기하고, 이야기 트리를 오가며, 생성된 텍스트를 편집할 수 있습니다. [책](user/books/index.md)을 참고하세요.
- **로어북** – 세계와 인물에 대한 메모를 필요한 순간에만 프롬프트에 넣습니다. 책 태그, 텍스트 또는 정규식 필터, 중첩된 로어북으로 조건을 지정할 수 있습니다. SillyTavern의 월드 인포도 가져올 수 있습니다. [로어북](user/lorebooks/index.md)을 참고하세요.
- **템플릿과 매크로** - 프롬프트는 SillyTavern 호환 매크로를 사용하는 Handlebars 템플릿입니다. 그래서 가져온 로어도 계속 작동합니다. [템플릿과 매크로](user/templates/index.md)를 참고하세요.
- **요약** – 오래된 부분을 압축하여 긴 책도 컨텍스트 창에 들어갈 수 있게 합니다. [요약](user/books/summaries.md)을 참고하세요.
- **OpenAI 호환 모델 전부** - 추론 모델을 포함하며, 모델별 한도와 전환 가능한 생성 설정을 지원합니다. [추론 제공자](user/inference-providers.md)와 [프로토콜](user/protocols.md)을 참고하세요.
- **백업** - 책별 백업과 복원, 복사를 지원하고, 전체 데이터베이스 백업을 일정에 따라 만들 수 있습니다. [책 백업](user/books/backups.md)과 [데이터베이스 백업](user/administration/database-backups.md)을 참고하세요.
- **다중 사용자** – 사용자마다 자신의 책, 로어, 모델을 가집니다. 관리자는 사용자, 백업, 정리 작업을 관리합니다. [관리](user/administration/index.md)를 참고하세요.
- **발행된 책** – 공유할 수 있는 읽기 전용 보기입니다. [발행과 뷰어](user/books/publishing.md)를 참고하세요.
- **검색과 바꾸기, 명령** - 이야기와 로어북에서 검색하고 바꿀 수 있으며, 이야기 메뉴 옆의 명령줄에서 책을 읽고 변경하는 명령을 실행할 수 있습니다. [명령](user/commands/index.md)과 [정규 표현식](user/regular-expressions.md)을 참고하세요.
- **에이전트** - 명령을 통해 복사본 위에서 책을 혼자 작업하는 모델입니다. 작업하는 모습을 지켜보고, 콘솔에서 대화하며, 변경 사항이 책에 반영되기 전에 검토할 수 있습니다. [에이전트](user/agents.md)를 참고하세요.
- **확장 기능** - 플러그인이 실행 중에 기능을 추가합니다. 챕터 표시, 로어북 기록, AI 검토, 모델에 대한 부가 질문 등이 있습니다. [확장 기능](user/extensions/index.md)을 참고하세요.

<a id="which-guide-do-you-need"></a>

## 어떤 안내서가 필요한가요?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[사용자 안내서](user/index.md)**           | Marginalia를 설치하거나, 다른 사람들을 위해 운영하거나, 이것으로 글을 쓰고 싶다면. [시작하기](user/getting-started/index.md)부터 보세요. |
| **[개발자 안내서](developer/index.md)** | 소스에서 Marginalia를 빌드하거나, 수정하거나, 확장 기능을 작성하거나, 내부 동작을 이해하고 싶다면.                       |

<a id="quick-links"></a>

## 빠른 링크

| 하고 싶은 일                   |                                                          |
|--------------------------------|----------------------------------------------------------|
| 내 컴퓨터에 설치하기            | [데스크톱 앱](user/getting-started/desktop-app.md)       |
| 서버에서 실행하기              | [서버 (Docker)](user/getting-started/docker-server.md) |
| 모델 연결하기                  | [추론 제공자](user/inference-providers.md)       |
| 책의 첫 부분 쓰기              | [빠른 시작](user/getting-started/quick-start.md)       |
| SillyTavern 로어북 사용하기    | [가져오기와 내보내기](user/lorebooks/import-export.md)       |
| 문제 해결하기                  | [문제 해결과 FAQ](user/troubleshooting.md)         |
| 확장 기능 작성하기             | [플러그인 개발](developer/plugins/index.md)       |
| 버그를 신고하거나 기여하기     | [기여하기](developer/contributing.md)                |

<a id="about"></a>

## 정보

Marginalia는 [MIT 라이선스](https://github.com/Enerccio/Marginalia/blob/master/LICENSE)에 따른 오픈 소스입니다.
이 안내서의 스크린샷에 사용된 그림의 출처는 [크레딧](credits.md) 페이지에 표기되어 있습니다.
릴리스는 [GitHub](https://github.com/Enerccio/Marginalia/releases)에 있으며, 버그와 아이디어는 [이슈 트래커](https://github.com/Enerccio/Marginalia/issues)로 보내 주세요.
