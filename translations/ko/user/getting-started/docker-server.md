---
label: 서버 (Docker)
order: 90
---

# 서버 (Docker)

여러 사람이 함께 쓰거나 다른 기기에서 접속하려면 Marginalia를 서버로 실행하세요. 모든 사용자는 자신만의 책, 로어북, 추론 제공자, 프로토콜을 가집니다.

<a id="requirements"></a>

## 요구 사항

- [Docker](https://docs.docker.com/get-docker/)와 Docker Compose
- Marginalia용 약 1 GB 메모리
- 인터넷을 통한 접속: 도메인과 HTTPS를 지원하는 리버스 프록시([아래 참고](#https-and-reverse-proxy))

<a id="install"></a>

## 설치

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

`docker compose up`을 처음 실행하면 소스에서 이미지를 빌드하므로 몇 분 정도 걸립니다. 이후 Marginalia는 포트 **8080**에서 대기합니다. `http://<server>:8080`에 접속해 관리자 계정을 만드세요([첫 실행](first-start.md) 참고).

<a id="prebuilt-image"></a>

### 미리 빌드된 이미지

소스에서 빌드하는 대신, 각 릴리스와 함께 게시되는 이미지(`linux/amd64`, `linux/arm64`)를 사용할 수 있습니다. `docker-compose.yml`에서 `build:` 블록을 `image:`로 바꾸세요:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest`는 최신 릴리스, `edge`는 개발 버전이며, `1.1.0`(또는 `1.1`)은 특정 릴리스로 고정합니다. 나머지 파일 내용은 그대로 둡니다.

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
| `ports` | `"8080:8080"`은 Marginalia를 호스트의 8080 포트로 공개합니다. 같은 호스트의 리버스 프록시만 접속하게 하려면 `"127.0.0.1:8080:8080"`을 사용하세요. |
| `volumes` | `./data`에는 데이터베이스, 백업, 확장 기능이 들어 있습니다. 이 폴더를 유지하고 서버 백업에 포함하세요. 없으면 처음 시작할 때 만들어지며, 시작 시 컨테이너가 이 폴더의 소유자를 컨테이너 안에서 Marginalia가 실행되는 사용자(`jetty`)로 바꿉니다. 따라서 폴더 안의 파일은 호스트에서 그 사용자의 ID 소유가 됩니다. |
| `-Xmx1g` | 최대 메모리입니다. 사용자가 많거나 매우 큰 책을 다룬다면 `-Xmx2g` 등으로 올리세요. |

파일을 바꾼 뒤에는 `docker compose up -d`로 적용하세요.

<a id="environment-variables"></a>

### 환경 변수

Marginalia 자체 설정은 `JAVA_TOOL_OPTIONS`와 함께 서비스의 환경 변수로 지정합니다:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 변수 | 기본값 | 역할 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (이미지 안에서는 `/var/marginalia/.marginalia`) | 데이터베이스, 백업, 확장 기능, API 키를 암호화하는 키가 들어 있는 폴더입니다. 일치하는 볼륨(`./data:/your/folder`)과 함께 있을 때만 바꾸세요. 컨테이너는 시작할 때 이 폴더의 소유자를 고칩니다. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | 로그인 화면과 언어를 선택하지 않은 사용자의 언어입니다(`en` 또는 `ja`; 확장 기능의 언어는 여기서 쓸 수 없습니다). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false`로 설정하면 **로그인 유지** 항목이 숨겨집니다. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 저장된 로그인의 유효 기간(초 단위, 30일)입니다. |
| `MARGINALIA_TRUSTED_PROXIES` | 비어 있음 | `X-Forwarded-For` 헤더를 신뢰할 리버스 프록시입니다. [아래 참고](#https-and-reverse-proxy). `-DtrustedProxies`를 대체합니다. |

같은 설정을 자바 옵션 `-Dmarginalia.default-language=ja`로도 줄 수 있으며, 이 옵션이 환경 변수보다 우선합니다. 맞지 않는 값(숫자 자리에 문자가 있거나 알 수 없는 언어 등)을 주면 Marginalia가 시작되지 않으며, 이유는 로그에 나옵니다.

<a id="https-and-reverse-proxy"></a>

## HTTPS와 리버스 프록시

!!!danger 인터넷에서는 평문 HTTP를 쓰지 마세요
비밀번호와 로그인 쿠키는 요청마다 전송됩니다. 평문 HTTP에서는 중간에 있는 누구나 이를 읽을 수 있습니다.
Marginalia 앞에 HTTPS를 지원하는 리버스 프록시를 두세요.
!!!

로그인 화면의 **로그인 유지** 옵션도 HTTPS가 필요합니다. 이 쿠키는 보안(secure) 속성이 붙어 있어서, 브라우저는 HTTPS 연결에서만 보관합니다.

Marginalia는 WebSocket 연결로 페이지를 갱신하므로, 프록시는 WebSocket 업그레이드를 전달해야 합니다. `marginalia.example.com`에 대한 예시는 다음과 같습니다:

+++ Caddy
Caddy는 인증서를 받아오고 WebSocket도 자동으로 전달합니다.

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

    client_max_body_size 100m;   # 백업과 확장 기능 업로드

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # 긴 생성 작업 동안 연결 유지
    }
}
```
+++

로그인 실패는 사용자별, 그리고 클라이언트 주소별로 제한됩니다(한 주소에서 30회 실패하면, 그 주소에서의 로그인이 점점 길어지는 시간 동안, 최대 15분까지 거부됩니다). Marginalia는 지정한 프록시의 `X-Forwarded-For` 헤더만 신뢰합니다. 이 설정이 없으면 모든 사용자가 프록시 주소에서 접속한 것으로 보이고, 누구든 30회 실패하면 잠시 모두가 차단됩니다. 프록시 설정에 위 예시처럼 헤더를 유지하고, `docker-compose.yml`의 `environment`에 `MARGINALIA_TRUSTED_PROXIES=<프록시의 주소 또는 범위>`를 설정하세요. Docker 호스트에 있는 프록시는 보통 `172.16.0.0/12` Docker 네트워크를 통해 컨테이너에 접근합니다. 여러 항목은 쉼표로 구분합니다. 이 제한 정보는 메모리에 보관되므로, Marginalia를 재시작하면 초기화됩니다.

<a id="models-on-the-same-machine"></a>

## 같은 머신의 모델

컨테이너 안에서 `localhost`는 서버가 아니라 컨테이너 자신을 가리킵니다. Docker 호스트에서 실행 중인 모델 서버(llama.cpp, Ollama, LM Studio 등)를 쓰려면 [추론 제공자](../inference-providers.md)에 `http://host.docker.internal:<port>/v1` 주소를 입력하세요. Linux에서는 `docker-compose.yml`의 서비스에 다음을 추가하세요:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

모델 서버도 `127.0.0.1`뿐 아니라 컨테이너가 접근할 수 있는 주소에서 대기해야 합니다.

<a id="updating"></a>

## 업데이트

```sh
git pull
docker compose up -d --build
```

[미리 빌드된 이미지](#prebuilt-image)를 쓴다면 대신 `docker compose pull`과 `docker compose up -d`를 실행하세요.

데이터베이스는 새 버전이 시작될 때 자동으로 업그레이드됩니다. Marginalia는 구조를 바꾸기 전에 [데이터베이스 사본](../administration/database-backups.md#copy-before-an-upgrade)을 저장하지만, 그래도 먼저 직접 [데이터베이스 백업](../administration/database-backups.md)을 해 두세요.

<a id="automatic-updates"></a>

### 자동 업데이트

저장소 루트의 `rebuild.sh`(Linux, macOS)와 `rebuild.bat`(Windows)이 업데이트를 대신 수행합니다. 직접 실행하거나 cron 작업 또는 예약된 작업에서 실행하세요. 체크아웃된 브랜치(어떤 브랜치든)의 원격을 가져오며, 새로운 변경이 없으면 아무 일도 하지 않습니다(`--force`를 쓰면 `docker-compose.yml`을 바꾼 뒤처럼 변경이 없어도 다시 빌드하고 재시작합니다). 변경이 있으면 `git pull`을 하고, **Marginalia가 계속 실행되는 동안** [번들된 플러그인](#plugins)과 이미지를 빌드한 뒤, 컨테이너를 멈추고 플러그인 JAR 파일을 `data/extensions`에 복사하여(설치된 파일을 교체합니다) 새 버전을 시작합니다. 어느 단계든 실패하면 스크립트는 메시지를 남기고 멈춥니다. 실패한 곳이 pull이나 빌드라면 이전 버전이 여전히 실행 중이며, 다음 실행 때 다시 빌드합니다.

`--stop-first`는 빌드 전에 컨테이너를 멈춥니다. 애플리케이션과 빌드를 동시에 감당할 메모리가 없는 서버에 적합하며, 이 경우 빌드 내내 Marginalia가 중단됩니다.

Linux에서는 스크립트를 직접 실행할 필요도 없습니다. 업데이트 감시자(update watcher)를 설치하면 관리자가 버튼으로 서버를 업데이트할 수 있습니다. [업데이트](../administration/updates.md)를 참고하세요.

<a id="plugins"></a>

## 플러그인

Docker 이미지에는 [확장 기능](../extensions/index.md)이 들어 있지 않으며, 확장 기능은 애플리케이션과 같은 소스에서 빌드해야 합니다. 이를 위해 자바나 Maven은 필요하지 않습니다. `marginalia/plugins/docker-compose.yml`이 컨테이너 안에서 모든 확장 기능을 빌드합니다. 먼저 애플리케이션을 빌드하고, 이어서 각 플러그인을 빌드한 뒤, JAR 파일을 `marginalia/plugins/build`에 복사합니다:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

처음 빌드할 때는 몇 분이 걸립니다(애플리케이션의 의존성을 내려받기 때문입니다). 그다음 JAR 파일을 `data/extensions`에 복사하고 Marginalia를 재시작하거나, *Admin → Extensions*에서 불러오세요([확장 기능](../administration/extensions.md) 참고). `rebuild.sh` / `rebuild.bat`이 복사를 대신하므로, 이를 사용하는 서버에서는 플러그인이 항상 애플리케이션과 함께 업데이트됩니다.

<a id="health"></a>

## 상태 확인

Docker 이미지에는 Docker 상태 확인(health check)이 들어 있습니다. Marginalia가 시작되는 동안(최대 2분)에는 `docker ps`가 `(health: starting)`을 표시하고, 이후 `(healthy)`로 바뀝니다. `http://localhost:8080/health`가 `200`으로 응답하지 않게 되면 `unhealthy`가 됩니다. Docker는 비정상 상태의 컨테이너를 스스로 재시작하지 않으므로, 이 상태를 모니터링에 활용하거나 자동 복구 도구를 사용하세요.

`/health`는 로그인이 필요 없으므로 모니터링 도구나 로드 밸런서도 사용할 수 있습니다(프록시를 통해서는 `https://marginalia.example.com/health`). Marginalia가 실행 중이고 데이터베이스가 응답하면 `200`과 `{"status":"UP"}`을 돌려주며, 시작 중이거나 문제가 있으면 `503`과 `{"status":"DOWN",...}`을 돌려줍니다. 그 밖의 정보(버전, 세부 내용)는 표시하지 않으며, 이유는 로그에 있습니다.

<a id="logs"></a>

## 로그

```sh
docker compose logs -f marginalia
```
