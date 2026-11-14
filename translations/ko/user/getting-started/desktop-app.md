---
label: 데스크톱 앱
order: 100
---

# 데스크톱 앱

데스크톱 앱은 한 대의 컴퓨터에서 쓰는 독립형 Marginalia입니다. 자체 Java 런타임과 서버를 함께 제공하며, 자신의 컴퓨터에서만 연결을 받고 기본 브라우저에서 열립니다.

<a id="download"></a>

## 다운로드

[릴리스 페이지](https://github.com/Enerccio/Marginalia/releases)에서 사용하는 시스템에 맞는 설치 프로그램이나 압축 파일을 내려받으세요. 모든 시스템에 대해 네 가지 변형이 제공됩니다:

| 파일 | 내용 |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | 설치 프로그램: Linux(Debian, Ubuntu 및 파생 배포판)에서는 `.deb`, macOS에서는 `.dmg`, Windows에서는 `.msi` |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | 내장 **[플러그인](../extensions/index.md)을 모두 포함한** 같은 설치 프로그램(아래 참고) |
| `marginalia-<version>-<os>-<arch>-app` | 앱: macOS에서는 `Marginalia.app`, Windows에서는 `Marginalia.exe`, Linux에서는 `bin/Marginalia` |
| `marginalia-<version>-<os>-<arch>` | 스크립트로 실행하는 휴대용 폴더, 명령줄 [옵션](#options) 사용 가능 |

빌드는 Windows x64, macOS(Apple Silicon 및 Intel), Linux(x64 및 arm64)용으로 제공됩니다. 파일 이름의 `<arch>`는 64비트 Intel 및 AMD 프로세서(Windows, macOS Intel, Linux)용은 `x86_64`, ARM(Apple Silicon, Linux arm64)용은 `aarch64`입니다.

<a id="install-and-start"></a>

## 설치 및 실행

+++ macOS
설치 프로그램을 사용하려면 `.dmg`를 열고 *Marginalia*를 *응용 프로그램*으로 끌어다 놓으세요. 압축 파일을 사용하려면:

1. 압축 파일을 풀고 `Marginalia.app`를 *응용 프로그램*으로 옮기세요.
2. 앱을 여세요. 앱에 아직 서명이 되어 있지 않아서, 처음에는 macOS가 실행을 거부할 수 있습니다. 앱을 우클릭하고 *열기*를 선택하거나, 다음을 실행하세요:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
설치 프로그램을 사용하려면 `.msi`를 실행하세요. 현재 사용자에게 설치되며(관리자 권한 필요 없음), 시작 메뉴와 바탕 화면에 Marginalia가 추가됩니다. 더 새로운 `.msi`는 설치된 버전을 업그레이드합니다(데이터는 `.marginalia`에 그대로 남습니다). 압축 파일을 사용하려면:

1. 압축 파일을 푸세요.
2. `Marginalia\Marginalia.exe`를 실행하세요.
3. SmartScreen이 알 수 없는 게시자에 대해 경고할 수 있습니다. *추가 정보* → *실행*을 클릭하세요.
+++ Linux
`.deb`(Debian, Ubuntu 및 파생 배포판)를 사용하여:

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

`/opt/marginalia`에 설치되고 앱 메뉴에 *Marginalia*가 추가됩니다. `sudo apt remove marginalia`로 제거할 수 있습니다(`~/.marginalia`의 데이터는 남습니다). 압축 파일을 사용하려면:

1. 압축 파일을 푸세요.
2. `Marginalia/bin/Marginalia`를 실행하세요.
+++ 휴대용 폴더
1. 압축 파일을 푸세요.
2. `bin/marginalia`(Windows에서는 `bin\marginalia.bat`)를 실행하세요.
+++

시작하는 데 몇 초가 걸립니다. 그런 다음 Marginalia가 브라우저의 `http://127.0.0.1:8765`에서 열리고 [첫 실행](first-start.md) 대화 상자를 보여 줍니다.

<a id="installer-with-the-plugins"></a>

### 플러그인이 포함된 설치 프로그램

**전체** 설치 프로그램(파일 이름에 `-full` 포함)은 같은 앱이며, Marginalia의 일부인 모든 플러그인(Anthropic 및 Google 공급자, Reviewer, Beta Readers, Side Query, Arc Outline, 생성기 등)을 함께 제공합니다. Marginalia를 처음 시작하면 플러그인이 `~/.marginalia/extensions`에 복사되고 *관리 → 확장*에 나타납니다. 기본 설치 프로그램을 사용하면 플러그인은 직접 하나씩 그곳에서 설치해야 합니다.

- 플러그인은 한 번만 설치됩니다. *관리 → 확장*에서 제거하면, 더 새로운 버전의 Marginalia로 업데이트한 뒤에도 제거된 상태로 남습니다(업데이트에는 새 플러그인이 들어 있으며, Marginalia는 이를 기존 것 옆에 두고 가장 새로운 것을 사용하며 이전 JAR은 `extensions/old-versions`에 보관합니다). 업데이트에서 새로 추가된 플러그인은 설치됩니다.
- 전체 설치 프로그램을 기본 설치 프로그램 위에 설치하거나(또는 그 반대로 설치하더라도) 데이터와 설정은 유지되며, 모든 업데이트에서도 마찬가지입니다.
- 기본 설치 프로그램은 플러그인을 제거하지 않습니다. 직접 설치한 플러그인은 절대 건드리지 않습니다.

<a id="tray-icon"></a>

## 트레이 아이콘

실행 중에는 Marginalia 아이콘(여백에 작은 분기 나무가 있는 원고 페이지)이 시스템 트레이에 표시됩니다(macOS에서는 메뉴 막대). 메뉴에는 두 개의 항목이 있습니다:

- **Open Marginalia** - 예를 들어 탭을 닫은 뒤, 브라우저에서 Marginalia를 다시 엽니다.
- **Quit Marginalia** - 서버를 중지합니다. 브라우저 탭을 닫는다고 Marginalia가 중지되지는 않습니다.

이미 실행 중인 상태에서 앱을 다시 시작하면 브라우저에서 열기만 합니다.

![트레이 아이콘과 메뉴](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## 데이터 위치

| 항목 | 위치 |
|---|---|
| 데이터베이스, 백업, 확장 | `~/.marginalia`(Windows에서는 `%USERPROFILE%\.marginalia`) |
| 로그 | `~/.marginalia/desktop/logs/marginalia.log`(이전 실행 기록은 `marginalia.log.1`로 보관됨) |

Marginalia를 다른 컴퓨터로 옮기려면 먼저 종료한 다음 `.marginalia` 폴더를 복사하세요(저장된 API 키를 복호화하는 `secret.key`도 함께 복사해야 합니다). 큰 변경을 하기 전에는 [데이터베이스 백업](../administration/database-backups.md)을 만드세요.

<a id="options"></a>

## 옵션

휴대용 실행기는 다음 옵션을 받습니다:

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning 다른 주소
`--host 0.0.0.0`(또는 다른 주소)를 사용하면 Marginalia는 일반 HTTP로 네트워크에서 접근할 수 있습니다. 다른 기기에서 접근하려면 대신 HTTPS 뒤에 있는 [서버](docker-server.md)를 사용하세요.
!!!

<a id="updating"></a>

## 업데이트

Marginalia를 종료하고, 앱(또는 휴대용 폴더)을 새 버전으로 교체한 뒤 다시 시작하세요. 데이터는 `.marginalia`에 그대로 남고 처음 시작할 때 자동으로 업그레이드됩니다. 데이터베이스 구조가 바뀌기 전에 Marginalia는 `db-backups`에 [사본](../administration/database-backups.md#copy-before-an-upgrade)을 저장합니다. 확장은 특정 Marginalia 버전용으로 빌드되므로 함께 업데이트하세요([확장 관리](../administration/extensions.md) 참고).

<a id="uninstalling"></a>

## 제거

앱이나 폴더를 삭제하세요. 데이터까지 지우려면 `.marginalia` 폴더도 삭제하세요.
