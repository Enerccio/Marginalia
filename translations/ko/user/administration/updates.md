---
label: 업데이트
order: 65
---

# 업데이트

Git 체크아웃으로 빌드된 [Docker 서버](../getting-started/docker-server.md)에서는 관리자가 서버에 로그인하지 않고도 관리자 페이지의 **Update** 탭에서 Marginalia를 업데이트할 수 있습니다. 서버는 체크아웃된 브랜치의 최신 버전을 가져오고, 애플리케이션과 [번들 플러그인](../getting-started/docker-server.md#plugins)을 빌드한 뒤 재시작합니다.

서버에는 **업데이트 감시기(update watcher)**라는 작은 프로그램이 필요하며, 이는 [한 번 설정](#setting-up-the-watcher)해야 합니다. 감시기가 없으면 이 탭에는 실행 중인 버전만 표시되고 감시기가 실행되지 않는다는 안내가 나타납니다. [데스크톱 앱](../getting-started/desktop-app.md)과 [사전 빌드된 이미지](../getting-started/docker-server.md#prebuilt-image)를 사용하는 서버도 같은 방식으로 동작합니다. 이들은 새 버전을 설치하여 업데이트합니다.

<a id="the-update-tab"></a>

## Update 탭

| | |
|---|---|
| **Running version** | 실행 중인 Marginalia의 버전과 커밋. |
| **Branch** | 서버의 체크아웃이 위치한 브랜치와 그것이 따라가는 원격 브랜치(예: `origin/dev`를 따라가는 `dev`), 그 원격 브랜치의 최신 커밋, 그리고 서버가 마지막으로 확인한 시각. 서버는 15분마다 확인합니다. |
| Status | 최신 커밋이 실행 중인 커밋과 다르면 *A new version is available*, 그렇지 않으면 *Marginalia is up to date*. 업데이트가 진행되는 동안에는 탭에 이를 표시합니다. |
| **Last update** | 여기서 시작된 마지막 업데이트가 끝난 시각과 성공 또는 실패 여부. |

서버는 그곳에 체크아웃된 브랜치가 무엇이든 그 브랜치를 따라갑니다. 설치본을 다른 브랜치로 옮기려면 서버의 체크아웃에서 `git switch`를 실행하세요. 탭은 다음 확인 후 새 브랜치를 표시합니다.

| 버튼 | |
|---|---|
| **Update and restart** | 확인 후 서버에 업데이트를 요청합니다. 새로운 변경이 없어도 빌드하고 재시작합니다(예: `docker-compose.yml`을 바꾼 뒤에 사용). |
| **Refresh** | 상태를 다시 읽습니다. 탭은 자동으로 새로 고쳐지지 않습니다. |
| **Log** | 마지막 업데이트의 출력: Git과 빌드가 기록한 내용. |

<a id="what-an-update-does"></a>

## 업데이트가 하는 일

1. 새 버전을 빌드합니다: `git pull`, 플러그인, 이미지. 이 과정은 몇 분 걸리며 그동안 **Marginalia는 계속 실행됩니다**. 탭에는 *The update is running*이 표시되고, **Log**에서 진행 상황을 볼 수 있습니다.
2. Marginalia를 중지하고, 새 플러그인을 `data/extensions`에 복사한 뒤(설치된 플러그인을 대체), 새 버전을 시작합니다. 이 과정은 1~2분 걸립니다([Health](../getting-started/docker-server.md#health) 참고). 작업 중인 모든 사용자가 중단되고, 생성 중인 내용도 모두 멈춥니다. 확인 창에는 그 순간 실행 중인 [작업](jobs.md) 수가 표시되므로 한가한 시간을 골라 진행하세요.
3. 새 버전이 시작될 때 데이터베이스가 업그레이드되며, 그 전에 [데이터베이스 사본](database-backups.md#copy-before-an-upgrade)이 저장됩니다. 그 전에 직접 [데이터베이스 백업](database-backups.md)도 만들어 두세요.

빌드가 실패하면 아무것도 중지되지 않습니다. 이전 버전이 계속 실행되고, **Last update**에는 *failed*가 표시되며, 로그에 그 이유가 나옵니다. 다음 업데이트는 새로 가져올 것이 없더라도 다시 빌드합니다.

<a id="setting-up-the-watcher"></a>

## 감시기 설정

Marginalia는 컨테이너 안에서 실행되므로 Git 체크아웃에도, Docker에도 접근할 수 없습니다. 이런 접근 권한을 주면 애플리케이션에 들어오는 모든 사람이 서버의 관리자가 되기 때문입니다. 그래서 Marginalia는 요청만 합니다. [데이터 폴더](index.md#the-data-folder)에 파일을 쓰면, 컨테이너 밖 서버에서 실행되는 감시기가 실제 작업을 수행합니다. 감시기는 체크아웃의 스크립트 `tools/update-watcher/watch.sh`이며, 실행하는 것은 항상 같은 체크아웃의 `rebuild.sh` 하나뿐입니다([자동 업데이트](../getting-started/docker-server.md#automatic-updates) 참고).

<a id="linux-with-systemd"></a>

### systemd를 쓰는 Linux

서버의 체크아웃(`docker-compose.yml`이 있는 폴더)에서:

```sh
sudo tools/update-watcher/install.sh
```

이 명령은 세 개의 유닛을 설치하고 시작합니다.

| 유닛 | |
|---|---|
| `marginalia-update.service` | 감시기를 한 번 실행합니다. |
| `marginalia-update.path` | 애플리케이션이 업데이트를 요청하는 순간 감시기를 시작합니다. |
| `marginalia-update.timer` | 부팅 2분 후에 시작하고 그 뒤 15분마다 시작하여 새 커밋을 확인합니다. |

**Admin → Update**를 열고 **Refresh**를 클릭하세요. 탭에 브랜치와 최신 커밋이 표시됩니다. 설정은 이것으로 끝입니다.

| 옵션 | |
|---|---|
| `--name NAME` | 유닛의 이름(기본값 `marginalia-update`). 한 컴퓨터에 체크아웃이 두 개(테스트 설치와 운영 설치)라면 각각 고유한 이름이 필요합니다. |
| `--user USER` | 감시기를 root 대신 이 사용자로 실행합니다. [아래](#who-the-watcher-runs-as) 참고. |
| `--uninstall` | 유닛을 중지하고 제거합니다(같은 `--name` 사용). |

`systemctl status marginalia-update.service`는 마지막 실행 결과를, `journalctl -u marginalia-update.service`는 출력(**Log**와 같은 내용, 그리고 감시기 자체가 동작하지 못했을 때의 이유)을 보여 줍니다. 유닛에는 체크아웃의 경로가 들어 있으므로, 체크아웃을 옮긴 뒤에는 `install.sh`를 다시 실행하세요.

<a id="without-systemd-cron"></a>

### systemd 없이 (cron)

감시기를 매분 실행하세요. 아무도 업데이트를 요청하지 않았고 원격을 15분 이내에 확인했다면 아무 일도 하지 않으므로 부담이 없습니다.

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

이 줄을 root의 crontab(`sudo crontab -e`)에 넣거나, 아래 설명대로 다른 사용자의 crontab에 넣으세요. 그러면 버튼을 누른 뒤 1분 안에 업데이트가 시작됩니다.

<a id="settings"></a>

### 설정

감시기는 다음 환경 변수를 읽습니다(systemd를 쓴다면 `sudo systemctl edit marginalia-update.service`를 실행하고 `[Service]` 아래에 `Environment=NAME=value` 줄을 넣으세요).

| 변수 | |
|---|---|
| `MARGINALIA_DATA_DIR` | 체크아웃 안의 `data`가 아닌 경우, 서버의 데이터 폴더(`docker-compose.yml`의 볼륨 왼쪽 값). |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh`의 인자. 기본값은 `--force`입니다. 애플리케이션과 빌드를 함께 돌릴 메모리가 없는 서버에서는 `--force --stop-first`를 사용하세요. 이 경우 빌드 전에 Marginalia를 중지하므로 빌드 내내 서비스가 중단됩니다. |
| `MARGINALIA_CHECK_MINUTES` | 요청 없이 실행될 때 원격을 다시 확인하기 전까지 상태가 얼마나 오래되어도 되는지(분). 기본값은 15입니다. systemd를 쓰면 감시기가 실행되는 빈도는 타이머가 결정합니다. |

<a id="who-the-watcher-runs-as"></a>

### 감시기를 실행하는 사용자

기본적으로 감시기는 **root**로 실행됩니다. Docker를 사용하고 컨테이너 사용자의 소유인 데이터 폴더에 써야 하기 때문입니다. Git 자체는 체크아웃의 소유자로 실행되므로 Git 파일은 계속 그 소유자의 것으로 남습니다.

감시기는 브랜치에 들어 있는 내용(`rebuild.sh`, `Dockerfile`)을 실행합니다. 직접 하는 모든 업데이트와 마찬가지입니다. 신뢰할 수 있는 저장소만 따르고, `rebuild.sh`나 `watch.sh`를 바꿀 수 있는 사람이 곧 감시기 사용자 권한으로 명령을 실행할 수 있으므로, 체크아웃은 소유자만 쓸 수 있도록 유지하세요.

`--user USER`(또는 그 사용자의 crontab)를 사용하면 다른 사용자로 실행됩니다. 그 사용자는 Docker를 사용할 수 있어야 하며(대부분의 시스템에서 root와 거의 같은 권한입니다), 체크아웃을 소유해야 하고, 보통 컨테이너 사용자의 소유인 `data/update`와 `data/extensions`에 쓸 수 있어야 합니다.

<a id="windows-and-macos"></a>

### Windows와 macOS

Docker Desktop용으로 준비된 감시기는 없습니다. `rebuild.bat` / `rebuild.sh`로 직접 업데이트하거나 예약 작업으로 실행하세요. [자동 업데이트](../getting-started/docker-server.md#automatic-updates)를 참고하세요.

<a id="troubleshooting"></a>

## 문제 해결

| | |
|---|---|
| *The update watcher does not run on the server* | 감시기가 한 시간 동안 상태를 기록하지 않았거나 한 번도 기록하지 않았습니다. `systemctl status marginalia-update.timer marginalia-update.path`와 `journalctl -u marginalia-update.service`를 확인하세요. 데이터 폴더가 체크아웃 안의 `data`가 아니라면 `MARGINALIA_DATA_DIR`를 설정하세요. |
| *Branch ... follows no remote branch* | 체크아웃이 상위(upstream)가 없는 브랜치에 있거나(또는 분리된 커밋에 있어서), 가져올 것이 없습니다. 서버에서 `git switch <branch>` 또는 `git branch --set-upstream-to=origin/<branch>`를 실행하세요. |
| *The server could not reach the Git repository* | 마지막 확인 때 `git fetch`가 실패했습니다(네트워크 없음, 체크아웃 소유자에게 자격 증명이 없는 비공개 저장소 등). 표시된 최신 커밋이 오래된 것일 수 있습니다. |
| The update was requested and nothing happens | cron을 쓰면 1분 안에 시작됩니다. 한 시간 동안 아무도 처리하지 않은 요청은 버려집니다. 감시기를 고친 뒤 뜻밖에 시작되지 않도록 하기 위함입니다. |
| *Last update: ..., failed* | **Log**를 읽으세요. `git pull`이 실패했다면 보통 체크아웃에 직접 만든 변경(수정된 파일, 로컬 커밋)이 있다는 뜻이므로 서버에서 해결하세요. 빌드가 실패하면 이전 버전이 계속 실행됩니다. |

감시기의 파일은 데이터 폴더의 `update`에 있습니다. `status.properties`(탭에 표시되는 내용), `update.log`(마지막 업데이트), 그리고 잠시 동안 존재하는 `request`입니다.
