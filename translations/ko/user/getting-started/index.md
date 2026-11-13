---
label: 시작하기
order: 990
icon: rocket
---

# 시작하기

Marginalia는 브라우저에서 실행됩니다. 두 가지 방법으로 설치할 수 있습니다:

| | [데스크톱 앱](desktop-app.md) | [서버 (Docker)](docker-server.md) |
|---|---|---|
| 적합한 용도 | 한 대의 컴퓨터를 한 사람이 사용할 때 | 여러 사용자가 쓰거나 다른 기기에서 접속할 때 |
| 설치 | 내려받아 실행하면 끝, 따로 설치할 것은 없음 | Docker와 Docker Compose |
| 주소 | `http://127.0.0.1:8765` (내 컴퓨터에서만) | `8080` 포트, 보통 HTTPS를 지원하는 리버스 프록시 뒤에서 사용 |
| 데이터 | `~/.marginalia` | `docker-compose.yml`과 같은 위치의 `./data` |

두 버전은 같은 기능을 제공하는 동일한 애플리케이션입니다: 책, 로어북, 사용자, 백업, 확장 기능.
휴대폰에서 글을 쓰려면 서버 버전을 실행한 뒤 휴대폰 브라우저에서 여세요: [휴대폰에서 Marginalia 사용하기](../phone.md)를 참고하세요.

Marginalia에는 자체 모델이 없습니다. 글을 쓰려면 **OpenAI 호환 API**가 필요합니다. 클라우드 서비스(OpenAI, OpenRouter 등)를 쓰거나 내 컴퓨터에서 실행하는 모델(llama.cpp, LM Studio, Ollama, vLLM 등)을 쓸 수 있습니다. API 주소와 API 키를 준비해 두세요(키가 필요 없는 로컬 서버라면 키 없이도 동작합니다. [제공자 추가](../inference-providers.md#adding-a-provider)를 참고하세요).

<a id="steps"></a>

## 단계

1. Marginalia를 설치하세요: [데스크톱 앱](desktop-app.md) 또는 [서버](docker-server.md).
2. 처음 실행할 때 [관리자 계정을 만드세요](first-start.md).
3. [빠른 시작](quick-start.md)을 따라 하세요: 모델을 연결하고, 프로토콜을 만들고, 책의 첫 부분을 쓰세요.
4. 선택 사항: [예제 책](example-book.md)을 열어 보세요. 요약, 주석, 번역이 포함된 완성된 이야기로, 긴 책이 어떤 모습인지 볼 수 있습니다.
