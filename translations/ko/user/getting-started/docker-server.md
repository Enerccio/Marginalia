---
label: 서버 (Docker)
order: 90
---

# 서버 (Docker)

여러 사람이 Marginalia를 함께 사용하거나 다른 기기에서 접속하고 싶을 때 Marginalia를 서버로 실행하세요. 사용자마다 자신만의 작품, 로어북, 추론 공급자, 프로토콜을 가집니다.

<a id="requirements"></a>

## 요구 사항

- [Docker](https://docs.docker.com/get-docker/)와 Docker Compose
- Marginalia에 약 1GB의 메모리
- 인터넷을 통한 접속: 도메인과 HTTPS를 지원하는 리버스 프록시([아래 참조](#https-and-reverse-proxy))

<a id="install"></a>

## 설치

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

처음 `docker compose up` 명령은 소스에서 이미지를 빌드하며, 몇 분 정도 걸립니다. 그 후 Marginalia는 **8080** 포트에서 대기합니다. `http://<server>:8080`에 접속해 관리자 계정을 만드세요([첫 실행](first-start.md) 참조).

<a id="prebuilt-image"></a>

### 빌드된 이미지 사용

소스에서 빌드하는 대신, 각 릴리스와 함께 배포되는 이미지를 사용할 수 있습니다(`linux/amd64` 및 `linux/arm64`). `docker-compose.yml`에서 `build:` 블록을 `image:`으로 바꾸세요:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest`은 최신 릴리스, `edge`은 개발 버전이며, `1.1.0`(또는 `1.1`)은 특정 릴리스를 고정합니다. 파일의 나머지 부분은 그대로 둡니다.

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
| `ports` | `"8080:8080"`은 호스트의 8080 포트로 Marginalia를 공개합니다. 같은 호스트의 리버스 프록시만 접속하게 하려면 `"127.0.0.1:8080:8080"`을 사용하세요. |
| `volumes` | `./data`은 데이터베이스, 백업, 확장을 보관합니다. 이 폴더를 유지하고 서버 백업에 포함하세요. 없으면 처음 실행할 때 만들어집니다. 시작할 때 컨테이너는 이 폴더의 소유자를 Marginalia가 컨테이너 안에서 실행되는 사용자(`jetty`)로 바꿉니다. 따라서 이 안의 파일은 호스트에서 그 사용자의 ID 소유가 됩니다. |
| `-Xmx1g` | 최대 메모리입니다. 사용자가 많거나 매우 큰 작품을 다룬다면 값을 올리세요(예: `-Xmx2g`). |

파일을 바꾼 뒤에는 `docker compose up -d`으로 적용하세요.

<a id="environment-variables"></a>

### 환경 변수

Marginalia 자체 설정은 `JAVA_TOOL_OPTIONS` 옆의 서비스 환경 변수입니다:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| 변수 | 기본값 | 역할 |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (이미지 안에서는 `/var/marginalia/.marginalia`) | 데이터베이스, 백업, 확장, 그리고 API 키를 암호화하는 키가 들어 있는 폴더입니다. 맞는 볼륨(`./data:/your/folder`)과 함께 있을 때만 바꾸세요. 컨테이너는 시작할 때 이 폴더의 소유자를 고정합니다. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | 로그인 페이지와 언어를 선택하지 않은 사용자의 언어입니다(`en` 또는 `ja`; 확장의 언어는 여기서 사용할 수 없습니다). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` 설정 시 *Save login*을 숨깁니다. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | 저장된 로그인이 유지되는 시간(초 단위, 30일)입니다. |
| `MARGINALIA_TRUSTED_PROXIES` | 비어 있음 | `X-Forwarded-For` 헤더를 신뢰할 리버스 프록시들입니다([아래 참조](#https-and-reverse-proxy)). `-DtrustedProxies`을 대체합니다. |
| `MARGINALIA_COOKIE_NAMESPACE` | 비어 있음 | *Save login* 쿠키 이름 앞에 붙는 이름입니다([한 호스트의 여러 애플리케이션](#several-applications-on-one-host) 참조). 설정하면 로그인 상태를 저장한 모든 사용자가 로그아웃됩니다. |
| `MARGINALIA_SESSION_COOKIE` | `JSESSIONID_MARGINALIA` (이미지 안에서), `JSESSIONID` (밖에서) | 세션 쿠키의 이름입니다([한 호스트의 여러 애플리케이션](#several-applications-on-one-host) 참조). |

같은 설정을 Java 옵션 `-Dmarginalia.default-language=ja`로도 줄 수 있으며, 이 옵션이 환경 변수보다 우선합니다. 맞지 않는 값(초 단위 자리에 문자, 알 수 없는 언어 등)이 있으면 Marginalia는 시작되지 않으며, 이유는 로그에 나옵니다.

<a id="https-and-reverse-proxy"></a>

## HTTPS와 리버스 프록시

!!!danger 인터넷에서 일반 HTTP를 사용하지 마세요
비밀번호와 로그인 쿠키는 모든 요청에 함께 전송됩니다. 일반 HTTP로는 전송 경로의 누구나 이를 읽을 수 있습니다.
Marginalia를 HTTPS를 지원하는 리버스 프록시 뒤에 두세요.
!!!

로그인 화면의 *Save login* 옵션도 HTTPS가 필요합니다. 이 쿠키는 secure로 표시되어, 브라우저가 HTTPS 연결에서만 보관하기 때문입니다.

Marginalia는 WebSocket 연결을 통해 페이지를 갱신하므로, 프록시는 WebSocket 업그레이드를 전달해야 합니다. `marginalia.example.com`의 예시:

+++ Caddy
Caddy는 인증서를 받아오고 WebSocket을 자동으로 전달합니다.

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

실패한 로그인은 사용자별, 그리고 클라이언트 주소별로 제한됩니다(한 주소에서 30번 실패하면, 그 주소의 로그인은 점점 길어지는 시간(최대 15분) 동안 거부됩니다). Marginalia는 지정한 프록시의 `X-Forwarded-For` 헤더만 신뢰합니다. 이 설정이 없으면 모든 사용자가 프록시 주소에서 접속한 것처럼 보이고, 누구든 30번 실패하면 잠시 모든 사용자가 차단됩니다. 프록시 설정에 이 헤더를 유지하고(위와 같이), `docker-compose.yml`의 `environment`에 `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`을 설정하세요. Docker 호스트의 프록시는 Docker 네트워크를 통해 컨테이너에 접근하며, 보통 `172.16.0.0/12`입니다. 여러 항목은 쉼표로 구분합니다. 제한 정보는 메모리에 보관되며, Marginalia를 재시작하면 지워집니다.

<a id="several-applications-on-one-host"></a>

### 한 호스트의 여러 애플리케이션

브라우저는 쿠키를 호스트 이름별로 보관하며, **포트별이 아닙니다**. `localhost:8080`의 Marginalia와 `localhost:8090`의 다른 프로그램(또는 하나의 프록시 뒤에 있는 같은 도메인의 두 프로그램)은 `localhost`의 쿠키를 공유합니다. 다른 프로그램도 Java 웹 애플리케이션이라면(Marginalia 플랫폼 위에 만든 다른 애플리케이션, 두 번째 Marginalia 등), 둘 다 세션 쿠키 `JSESSIONID`를 사용하므로 한쪽에 접속할 때마다 다른 쪽에서 로그아웃됩니다. 그래서 이미지는 세션 쿠키 이름을 `JSESSIONID_MARGINALIA`로 지정합니다. 한 호스트에서 Marginalia 컨테이너를 두 개 실행한다면, 두 번째 컨테이너에는 다른 이름을 주세요: `MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA2`과 `MARGINALIA_COOKIE_NAMESPACE=second`(*Save login* 쿠키). 각각 다른 호스트 이름(`marginalia.example.com`, `other.example.com`)을 쓰면 설정 없이도 이 문제를 피할 수 있습니다.

<a id="models-on-the-same-machine"></a>

## 같은 기기의 모델

컨테이너 안에서 `localhost`은 서버가 아니라 컨테이너 자신을 가리킵니다. Docker 호스트에서 실행되는 모델 서버(llama.cpp, Ollama, LM Studio 등)를 사용하려면, [추론 공급자](../inference-providers.md)에 주소 `http://host.docker.internal:<port>/v1`를 입력하세요. Linux에서는 `docker-compose.yml`의 서비스에 다음을 추가하세요:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

모델 서버도 컨테이너가 접근할 수 있는 주소에서 대기해야 합니다. `127.0.0.1`에서만 대기하면 안 됩니다.

<a id="updating"></a>

## 업데이트

```sh
git pull
docker compose up -d --build
```

[빌드된 이미지](#prebuilt-image)를 사용한다면 대신 `docker compose pull` 및 `docker compose up -d`을 사용하세요.

데이터베이스는 새 버전이 시작될 때 자동으로 업그레이드됩니다. Marginalia는 구조를 바꾸기 전에 [데이터베이스 사본](../administration/database-backups.md#copy-before-an-upgrade)을 저장하지만, 그 전에 직접 [데이터베이스 백업](../administration/database-backups.md)도 만들어 두세요.

<a id="automatic-updates"></a>

### 자동 업데이트

저장소 루트의 `rebuild.sh`(Linux, macOS)과 `rebuild.bat`(Windows)는 업데이트를 대신 수행합니다. 직접 실행하거나 cron 작업이나 예약 작업에서 실행하세요. 스크립트는 체크아웃된 브랜치의 원격 저장소(어떤 브랜치든)를 가져오며, 새로운 것이 없으면 아무 작업도 하지 않습니다(`--force`은 예를 들어 `docker-compose.yml`을 바꾼 뒤처럼 어쨌든 다시 빌드하고 재시작합니다). 그렇지 않으면 `git pull`을 수행하고, **Marginalia가 계속 실행되는 동안** [번들 플러그인](#plugins)과 이미지를 빌드한 다음, 컨테이너를 멈추고, 플러그인 JAR를 `data/extensions`에 복사(설치된 것을 교체)한 뒤 새 버전을 시작합니다. 어느 단계든 실패하면 스크립트는 메시지와 함께 멈춥니다. 가져오기나 빌드 단계에서 실패했다면 이전 버전이 여전히 실행 중이며, 다음 실행에서 다시 빌드합니다.

`--stop-first`은 빌드 전에 컨테이너를 멈춥니다. 애플리케이션과 빌드를 동시에 돌릴 메모리가 없는 서버를 위한 것이며, 이 경우 전체 빌드 동안 Marginalia는 중단됩니다.

Linux에서는 스크립트를 직접 실행할 필요가 전혀 없습니다. 업데이트 감시기를 설치하면 관리자가 버튼으로 서버를 업데이트할 수 있습니다([업데이트](../administration/updates.md) 참조).

<a id="plugins"></a>

## 플러그인

Docker 이미지에는 [확장](../extensions/index.md)이 들어 있지 않으며, 확장은 애플리케이션과 같은 소스에서 빌드해야 합니다. 이를 위해 Java나 Maven은 필요하지 않습니다. `marginalia/plugins/docker-compose.yml`이 컨테이너 안에서 모두 빌드합니다. 먼저 애플리케이션을 빌드하고 그다음 모든 플러그인을 빌드한 뒤, JAR를 `marginalia/plugins/build`에 복사합니다:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

처음 빌드에는 몇 분이 걸립니다(애플리케이션의 의존성을 내려받기 때문입니다). 그다음 JAR를 `data/extensions`에 복사하고 Marginalia를 재시작하거나, *Admin → Extensions*에서 불러오세요([확장](../administration/extensions.md) 참조). `rebuild.sh` / `rebuild.bat`가 복사를 대신 해 주므로, 이를 사용하는 서버에서는 플러그인이 항상 애플리케이션과 함께 업데이트됩니다.
<a id="health"></a>

## 상태 확인

이 이미지에는 Docker 상태 검사(health check)가 설정되어 있습니다. Marginalia가 시작되는 동안(최대 2분)에는 `docker ps` 상태가 `(health: starting)`으로 표시되고, 그 후에는 `(healthy)`로 바뀝니다. `http://localhost:8080/health`이(가) `200` 응답을 더 이상 보내지 않으면 상태는 `unhealthy`이 됩니다. Docker는 비정상 상태의 컨테이너를 스스로 재시작하지 않습니다. 이 상태를 모니터링에 활용하거나 자동 복구 도구와 함께 사용하세요.

`/health`는 로그인이 필요 없으므로 모니터링 도구나 로드 밸런서에서도 사용할 수 있습니다(프록시를 거칠 때: `https://marginalia.example.com/health`). Marginalia가 실행 중이고 데이터베이스가 응답하면 `200` 및 `{"status":"UP"}`로 응답합니다. 시작 중이거나 문제가 생기면 `{"status":"DOWN",...}`과 함께 `503`로 응답합니다. 그 밖의 정보(버전, 세부 내용 등)는 표시하지 않습니다. 원인은 로그에서 확인하세요.

<a id="logs"></a>

## 로그

```sh
docker compose logs -f marginalia
```
