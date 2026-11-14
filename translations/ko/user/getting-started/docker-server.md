---
label: 서버 (Docker)
order: 90
---

# 서버 (Docker)

여러 사람이 함께 쓰거나 다른 기기에서 접속하려면 Marginalia를 서버로 실행하세요. 모든 사용자는 자신만의 책, 로어북, 추론 제공자, 프로토콜을 갖습니다.

<a id="requirements"></a>

## 요구 사항

- Docker Compose를 포함한 [Docker](https://docs.docker.com/get-docker/)
- Marginalia에 필요한 약 1GB 메모리
- 인터넷을 통한 접속: 도메인과 HTTPS를 지원하는 리버스 프록시 ([아래](#https-and-reverse-proxy) 참고)

<a id="install"></a>

## 설치

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

처음 `docker compose up`를 실행하면 소스에서 이미지를 빌드하며, 몇 분 정도 걸립니다. 이후 Marginalia는 **8080** 포트에서 대기합니다. `http://<server>:8080`에 접속해 관리자 계정을 만드세요 ([첫 실행](first-start.md) 참고).

<a id="prebuilt-image"></a>

### 빌드된 이미지 사용

소스에서 빌드하는 대신 각 릴리스와 함께 게시되는 이미지를 사용할 수 있습니다(`linux/amd64` 및 `linux/arm64`). `docker-compose.yml`에서 `build:` 블록을 `image:`(으)로 바꾸세요:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest`는 최신 릴리스이고, `edge`는 개발 버전입니다. `1.1.0`(또는 `1.1`)는 특정 릴리스를 고정합니다. 나머지 파일 내용은 그대로 유지됩니다.

<a id="configuration"></a>

## 설정

모든 설정은 `docker-compose.yml`에서 합니다:

```yaml
services:
  marginalia:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: marginalia-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/marginalia/.marginalia
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| 설정 | 역할 |
|---|---|
| `ports` | `"8080:8080"`은(는) Marginalia를 호스트의 8080 포트에 노출합니다. 같은 호스트의 리버스 프록시만 접근하게 하려면 `"127.0.0.1:8080:8080"`을(를) 사용하세요. |
| `volumes` | `./data`에는 데이터베이스, 백업, 확장이 들어 있습니다. 유지하고 서버 백업에 포함하세요. 없으면 처음 시작할 때 만들어지며, 시작 시 컨테이너가 이 폴더의 소유자를 Marginalia가 컨테이너 안에서 실행되는 사용자(`jetty`)로 바꾸므로, 폴더 안의 파일은 호스트에서 그 사용자의 ID 소유가 됩니다. |
| `-Xmx1g` | 최대 메모리입니다. 사용자가 많거나 아주 큰 책을 다룬다면(예: `-Xmx2g`) 값을 올리세요. |

파일을 변경한 뒤에는 `docker compose up -d`로 적용하세요.

<a id="environment-variables"></a>

### 환경 변수

Marginalia 자체의 설정은 `JAVA_TOOL_OPTIONS` 옆의 서비스 환경 변수로 지정합니다:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 변수 | 기본값 | 역할 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia`(이미지에서는 `/var/marginalia/.marginalia`) | 데이터베이스, 백업, 확장, 그리고 API 키를 암호화하는 키가 들어 있는 폴더입니다. 일치하는 볼륨(`./data:/your/folder`)과 함께일 때만 바꾸세요. 컨테이너는 시작할 때 이 폴더의 소유자를 고정합니다. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | 로그인 화면과 언어를 선택하지 않은 사용자에게 적용되는 언어입니다(`en` 또는 `ja`; 확장의 언어는 여기서 쓸 수 없습니다). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false`이면 *로그인 유지* 옵션이 숨겨집니다. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 저장된 로그인이 유지되는 기간으로, 초 단위입니다(30일). |
| `MARGINALIA_TRUSTED_PROXIES` | 비어 있음 | 헤더 `X-Forwarded-For`을(를) 신뢰할 리버스 프록시 목록입니다. [아래](#https-and-reverse-proxy) 참고. `-DtrustedProxies`을(를) 대체합니다. |

같은 설정을 Java 옵션 `-Dmarginalia.default-language=ja` 형태로도 줄 수 있으며, 이 옵션은 환경 변수보다 우선합니다. 맞지 않는 값(초 단위 숫자 자리에 문자, 알 수 없는 언어 등)이 주어지면 Marginalia는 시작하지 않습니다. 이유는 로그에 나옵니다.

<a id="https-and-reverse-proxy"></a>

## HTTPS와 리버스 프록시

!!!danger 인터넷에서는 평문 HTTP를 쓰지 마세요
비밀번호와 로그인 쿠키는 모든 요청에 실려 전송됩니다. 평문 HTTP에서는 경로상의 누구든 이를 읽을 수 있습니다.
Marginalia는 반드시 HTTPS를 지원하는 리버스 프록시 뒤에 두세요.
!!!

로그인 화면의 *로그인 유지* 옵션도 HTTPS가 필요합니다. 이 쿠키는 secure로 표시되어, 브라우저가 HTTPS 연결에서만 보관하기 때문입니다.

Marginalia는 WebSocket 연결을 통해 페이지를 갱신하므로, 프록시는 WebSocket 업그레이드를 전달해야 합니다. `marginalia.example.com`용 예시는 다음과 같습니다:

+++ Caddy
Caddy는 인증서를 발급받고 WebSocket을 자동으로 전달합니다.

```
marginalia.example.com {
    reverse_proxy localhost:8080
}
```
+++ nginx
```nginx
server {
    listen 443 ssl;
    server_name marginalia.example.com;

    ssl_certificate     /etc/letsencrypt/live/marginalia.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/marginalia.example.com/privkey.pem;

    client_max_body_size 100m;   # uploads of backups and extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # long generations keep the connection open
    }
}
```
+++

로그인 실패는 사용자별, 그리고 클라이언트 주소별로 제한됩니다(한 주소에서 30번 실패하면, 그 주소에서의 로그인은 점점 길어지는 시간 동안, 최대 15분까지 거부됩니다). Marginalia는 지정한 프록시의 `X-Forwarded-For` 헤더만 신뢰합니다. 이 설정이 없으면 모든 사용자가 프록시 주소에서 접속한 것으로 보이고, 누구든 30번 실패하면 모두가 한동안 차단됩니다. 헤더는 프록시 설정에 그대로 두고(위 예시처럼), `environment`의 `docker-compose.yml`에서 `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`을(를) 설정하세요. Docker 호스트의 프록시는 Docker 네트워크를 통해 컨테이너에 도달하며, 보통 `172.16.0.0/12`입니다. 여러 항목은 쉼표로 구분합니다. 제한 기록은 메모리에 보관되므로, Marginalia를 재시작하면 초기화됩니다.

<a id="models-on-the-same-machine"></a>

## 같은 컴퓨터의 모델

컨테이너 안에서 `localhost`은(는) 서버가 아니라 컨테이너 자신을 가리킵니다. Docker 호스트에서 실행되는 모델 서버(llama.cpp, Ollama, LM Studio 등)를 쓰려면, [추론 제공자](../inference-providers.md)에 주소 `http://host.docker.internal:<port>/v1`을(를) 입력하세요. Linux에서는 `docker-compose.yml`의 서비스에 다음을 추가하세요:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

모델 서버는 `127.0.0.1`뿐 아니라 컨테이너가 접근할 수 있는 주소에서도 대기해야 합니다.

<a id="updating"></a>

## 업데이트

```sh
git pull
docker compose up -d --build
```

[빌드된 이미지](#prebuilt-image)를 쓴다면 대신 `docker compose pull`와 `docker compose up -d`을(를) 사용하세요.

데이터베이스는 새 버전이 시작될 때 자동으로 업그레이드됩니다. Marginalia는 구조를 바꾸기 전에 [데이터베이스 사본](../administration/database-backups.md#copy-before-an-upgrade)을 저장하지만, 먼저 직접 [데이터베이스 백업](../administration/database-backups.md)도 해 두세요.

<a id="automatic-updates"></a>

### 자동 업데이트

저장소 루트의 `rebuild.sh`(Linux, macOS)와 `rebuild.bat`(Windows)가 업데이트를 대신 수행합니다. 직접 실행하거나 cron 작업 또는 예약 작업에서 실행하세요. 체크아웃이 어떤 브랜치에 있든 그 브랜치의 원격을 가져오며, 새 변경 사항이 없으면 아무 작업도 하지 않습니다(다만 `--force`은(는) 예를 들어 `docker-compose.yml`을(를) 바꾼 뒤처럼 항상 다시 빌드하고 재시작합니다). 변경 사항이 있으면 `git pull`하고, Marginalia가 계속 실행되는 **동안** [번들 플러그인](#plugins)과 이미지를 빌드합니다. 그런 다음 컨테이너를 중지하고, 플러그인 JAR을 `data/extensions`에 복사한 뒤(설치된 파일을 대체합니다) 새 버전을 시작합니다. 어느 단계든 실패하면 스크립트는 메시지와 함께 멈춥니다. 가져오기나 빌드 단계에서 실패했다면 이전 버전이 계속 실행 중이며, 다음 실행 때 다시 빌드합니다.

`--stop-first`은(는) 빌드 전에 컨테이너를 중지합니다. 애플리케이션과 빌드를 함께 감당할 메모리가 없는 서버를 위한 옵션이며, 이 경우 빌드 내내 Marginalia를 사용할 수 없습니다.

Linux에서는 업데이트 감시기를 설치해 두면 스크립트를 직접 실행할 필요가 없습니다. 관리자는 버튼 하나로 서버를 업데이트할 수 있습니다. [업데이트](../administration/updates.md) 참고.

<a id="plugins"></a>

## 플러그인

Docker 이미지에는 [확장](../extensions/index.md)이 포함되어 있지 않으며, 확장은 애플리케이션과 같은 소스에서 빌드해야 합니다. 이를 위해 Java나 Maven은 필요하지 않습니다. `marginalia/plugins/docker-compose.yml`가 컨테이너 안에서 모든 확장을 빌드합니다. 먼저 애플리케이션을 빌드한 뒤 모든 플러그인을 빌드하고, JAR을 `marginalia/plugins/build`에 복사합니다:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

처음 빌드하는 데는 몇 분이 걸립니다(애플리케이션의 의존성을 내려받기 때문입니다). 이후 JAR을 `data/extensions`에 복사하고 Marginalia를 재시작하거나, *관리 → 확장*에서 불러오세요([확장](../administration/extensions.md) 참고). `rebuild.sh` / `rebuild.bat`이(가) 복사를 대신 해 주므로, 이를 사용하는 서버에서는 플러그인이 항상 애플리케이션과 함께 업데이트됩니다.

<a id="health"></a>

## 상태 확인

이미지에는 Docker 상태 확인이 들어 있습니다. Marginalia가 시작하는 동안(최대 2분) `docker ps`에는 `(health: starting)`(으)로 표시되고, 그 뒤에는 `(healthy)`(으)로 바뀝니다. `http://localhost:8080/health`이(가) `200`(으)로 응답하지 않게 되면 `unhealthy`(으)로 바뀝니다. Docker는 비정상 컨테이너를 스스로 재시작하지 않으므로, 이 상태는 모니터링에 활용하거나 자동 복구 도구와 함께 쓰세요.

`/health`은(는) 로그인이 필요하지 않으므로, 모니터링 도구나 로드 밸런서도 사용할 수 있습니다(프록시를 거칠 때는 `https://marginalia.example.com/health`). Marginalia가 실행 중이고 데이터베이스가 응답하면 `200`과(와) `{"status":"UP"}`을(를) 반환합니다. 시작 중이거나 문제가 있으면 `503`와(과) `{"status":"DOWN",...}`을(를) 반환합니다. 그 밖의 정보(버전, 세부 내용 등)는 표시하지 않으며, 이유는 로그에 있습니다.

<a id="logs"></a>

## 로그

```sh
docker compose logs -f marginalia
```
