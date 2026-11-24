---
label: 업데이트
order: 65
---

# 업데이트

Git 체크아웃으로 빌드된 [Docker 서버](../getting-started/docker-server.md)에서는 관리자가 서버에 로그인하지 않고도 관리 페이지의 **Update** 탭에서 Marginalia를 업데이트할 수 있습니다. 서버가 체크아웃된 브랜치의 최신 버전을 가져오고, 애플리케이션과 [번들 플러그인](../getting-started/docker-server.md#plugins)을 빌드한 뒤 재시작합니다.

서버에는 작은 프로그램인 **업데이트 감시기(update watcher)**가 필요하며, 이는 [한 번 설정](#setting-up-the-watcher)해 두어야 합니다. 감시기가 없으면 이 탭에는 실행 중인 버전만 표시되고, 감시기가 실행되지 않는다고 나옵니다. [데스크톱 앱](../getting-started/desktop-app.md)과 [사전 빌드된 이미지](../getting-started/docker-server.md#prebuilt-image)를 사용하는 서버도 같은 방식으로 표시됩니다. 이들은 새 버전을 설치하는 방식으로 업데이트합니다.

<a id="the-update-tab"></a>

## Update 탭

| | |
|---|---|
| **Running version** | 실행 중인 Marginalia의 버전과 커밋입니다. |
| **Branch** | 서버의 체크아웃이 있는 브랜치와 따라가는 원격 브랜치(예: `dev`, `origin/dev`을 따라감), 그 원격 브랜치의 최신 커밋, 서버가 마지막으로 확인한 시각을 보여 줍니다. 서버는 15분마다 확인합니다. |
| Status | 최신 커밋이 실행 중인 커밋과 다르면 *A new version is available*, 같으면 *Marginalia is up to date*가 표시됩니다. 업데이트가 진행 중이면 탭에 그 사실이 표시됩니다. |
| **Last update** | 여기서 시작된 마지막 업데이트가 끝난 시각과 성공 또는 실패 여부입니다. |

서버는 그곳에 체크아웃된 브랜치가 무엇이든 그 브랜치를 따라갑니다. 설치본을 다른 브랜치로 옮기려면 서버의 체크아웃에서 `git switch`하세요. 탭에는 다음 확인 후에 새 브랜치가 표시됩니다.

| 버튼 | |
|---|---|
| **Update and restart** | 확인 대화상자를 거친 뒤 서버에 업데이트를 요청합니다. 새 변경이 없어도 빌드하고 재시작합니다(예: `docker-compose.yml`을 바꾼 뒤에 사용). |
| **Refresh** | 상태를 다시 읽습니다. 탭은 저절로 새로 고침되지 않습니다. |
| **Log** | 마지막 업데이트의 출력, 즉 Git과 빌드가 기록한 내용입니다. |

같은 작업은 스크립트에서도 할 수 있습니다. *Update the installation* 권한이 있는 [API 키](../account.md#api-keys)로 상태를 읽고, 업데이트를 요청하고, 로그를 읽으면 됩니다(자세한 내용은 [개발자 매뉴얼](../../developer/api.md)을 참고하세요).

<a id="what-an-update-does"></a>

## 업데이트가 하는 일

1. 새 버전을 빌드합니다: `git pull`, 플러그인, 이미지. 몇 분이 걸리며, 이 동안 **Marginalia는 계속 실행됩니다**. 탭에는 *The update is running*이 표시되고, **Log**에서 진행 상황을 볼 수 있습니다.
2. Marginalia를 중지하고, 새 플러그인을 `data/extensions`에 복사한 뒤(설치된 플러그인을 대체함) 새 버전을 시작합니다. 1~2분이 걸립니다(자세한 내용은 [상태 확인](../getting-started/docker-server.md#health)을 참고하세요). 작업 중인 모든 사용자가 중단되며, 생성 중인 내용도 모두 멈춥니다. 확인 대화상자에 그 순간 실행 중인 [작업](jobs.md) 수가 나오므로, 한가한 시간을 골라 주세요.
3. 새 버전이 시작될 때 데이터베이스가 업그레이드되며, 이에 앞서 [데이터베이스 사본](database-backups.md#copy-before-an-upgrade)이 저장됩니다. 직접 [데이터베이스 백업](database-backups.md)도 미리 만들어 두세요.

빌드가 실패하면 아무것도 중지되지 않으며, 이전 버전이 계속 실행됩니다. **Last update**에는 *failed*가 표시되고, 로그에 원인이 나옵니다. 다음 업데이트 때는 새로 가져올 것이 없더라도 다시 빌드합니다.

<a id="setting-up-the-watcher"></a>

## 감시기 설정

Marginalia는 컨테이너 안에서 실행되므로 Git 체크아웃과 Docker에 접근할 수 없습니다. 이 접근 권한을 주면 애플리케이션에 들어오는 누구나 서버의 관리자가 되기 때문입니다. 그래서 Marginalia는 요청만 합니다. 요청은 [데이터 폴더](index.md#the-data-folder)에 파일을 쓰는 방식으로 이루어지고, 컨테이너 밖에서 서버에 실행되는 감시기가 실제 작업을 수행합니다. 감시기는 체크아웃에 있는 스크립트 `tools/update-watcher/watch.sh`이며, 실행하는 것은 항상 같은 체크아웃의 `rebuild.sh`뿐입니다([자동 업데이트](../getting-started/docker-server.md#automatic-updates)를 참고하세요).

<a id="linux-with-systemd"></a>

### systemd를 쓰는 Linux

서버의 체크아웃(`docker-compose.yml`이 있는 폴더)에서 다음을 실행하세요.

```sh
sudo tools/update-watcher/install.sh
```

이 명령은 세 개의 유닛을 설치하고 시작합니다.

| 유닛 | |
|---|---|
| `marginalia-update.service` | 감시기를 한 번 실행합니다. |
| `marginalia-update.path` | 애플리케이션이 업데이트를 요청하는 즉시 시작합니다. |
| `marginalia-update.timer` | 부팅 2분 후에 시작하고, 이후 15분마다 실행되어 새 커밋을 확인합니다. |

**Admin → Update**를 열고 **Refresh**를 클릭하세요. 탭에 브랜치와 최신 커밋이 표시되면 설정이 끝난 것입니다.

| 옵션 | |
|---|---|
| `--name NAME` | 유닛의 이름입니다(기본값은 `marginalia-update`). 한 컴퓨터에 체크아웃이 둘(테스트 설치와 운영 설치) 있으면 각각 다른 이름을 써야 합니다. |
| `--user USER` | 감시기를 root 대신 이 사용자로 실행합니다. [아래](#who-the-watcher-runs-as)를 참고하세요. |
| `--uninstall` | 유닛을 중지하고 제거합니다(같은 `--name`와 함께 사용). |

`systemctl status marginalia-update.service`은 마지막 실행을 보여 주고, `journalctl -u marginalia-update.service`은 그 출력을 보여 줍니다(**Log**와 같은 내용이며, 감시기 자체가 작동하지 못했다면 그 이유도 나옵니다). 유닛에는 체크아웃의 경로가 들어 있으므로, 체크아웃을 옮긴 뒤에는 `install.sh`을 다시 실행하세요.

<a id="without-systemd-cron"></a>

### systemd 없이 쓰는 cron

감시기를 1분마다 실행하세요. 아무도 업데이트를 요청하지 않았고 마지막 원격 확인이 15분 이내였다면 아무 일도 하지 않으므로 비용이 들지 않습니다.

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

이 줄을 root의 crontab(`sudo crontab -e`)에 넣거나, 아래 설명대로 다른 사용자의 crontab에 넣으세요. 그러면 버튼을 누른 뒤 1분 안에 업데이트가 시작됩니다.

<a id="settings"></a>

### 설정

감시기는 다음 환경 변수를 읽습니다(systemd에서는 `[Service]` 아래의 `sudo systemctl edit marginalia-update.service` 줄과 `Environment=NAME=value` 줄로 설정합니다).

| 변수 | |
|---|---|
| `MARGINALIA_DATA_DIR` | 서버의 데이터 폴더입니다. 체크아웃 안의 `data`가 아닐 때 지정합니다(`docker-compose.yml` 볼륨의 왼쪽 부분). |
| `MARGINALIA_REBUILD_ARGS` | `rebuild.sh`의 인수이며, 기본값은 `--force`입니다. 애플리케이션과 빌드를 함께 돌릴 메모리가 없는 서버에서는 `--force --stop-first`을 사용하세요. 이 경우 Marginalia는 빌드 전에 중지되며, 빌드하는 동안 내내 중단됩니다. |
| `MARGINALIA_CHECK_MINUTES` | 요청 없이 실행될 때 원격을 다시 확인하기 전에 상태가 얼마나 오래되어도 되는지를 정합니다. 기본값은 15입니다(분 단위). systemd를 쓸 때는 타이머가 감시기의 실행 주기를 정합니다. |

<a id="who-the-watcher-runs-as"></a>

### 감시기를 실행하는 사용자

감시기는 기본적으로 **root**로 실행됩니다. Docker를 사용해야 하고, 컨테이너 사용자의 소유인 데이터 폴더에 써야 하기 때문입니다. Git은 체크아웃의 소유자로 실행되므로, Git이 만든 파일은 계속 그 소유자의 것으로 남습니다.

감시기는 브랜치가 담고 있는 내용(`rebuild.sh`, `Dockerfile`)을 실행합니다. 이는 직접 하는 모든 업데이트와 같습니다. 신뢰하는 저장소만 따르고, 체크아웃에는 소유자만 쓸 수 있게 하세요. 그곳에서 `rebuild.sh`나 `watch.sh`를 바꿀 수 있는 사람은 누구든 감시기 사용자의 권한으로 명령을 실행하게 되기 때문입니다.

`--user USER`(또는 그 사용자의 crontab)을 사용하면 감시기는 다른 사용자로 실행됩니다. 그 사용자는 Docker를 사용할 수 있어야 하며(대부분의 시스템에서는 이것이 root와 거의 같은 권한입니다), 체크아웃의 소유자여야 하고, 보통 컨테이너 사용자의 소유인 `data/update`와 `data/extensions`에 쓸 수 있어야 합니다.

<a id="windows-and-macos"></a>

### Windows와 macOS

Docker Desktop용으로 바로 쓸 수 있는 감시기는 없습니다. `rebuild.bat` / `rebuild.sh`로 직접 업데이트하거나 예약된 작업에서 실행하세요([자동 업데이트](../getting-started/docker-server.md#automatic-updates)를 참고하세요).

<a id="troubleshooting"></a>

## 문제 해결

| | |
|---|---|
| *The update watcher does not run on the server* | 감시기가 한 시간 동안 상태를 기록하지 않았거나 한 번도 기록하지 않았습니다. `systemctl status marginalia-update.timer marginalia-update.path`과 `journalctl -u marginalia-update.service`를 확인하세요. 데이터 폴더가 체크아웃 안의 `data`가 아니라면 `MARGINALIA_DATA_DIR`를 설정하세요. |
| *Branch ... follows no remote branch* | 체크아웃이 상위 브랜치가 없는 브랜치에 있거나(또는 분리된 커밋에 있어), 가져올 것이 없습니다. 서버에서 `git switch <branch>` 또는 `git branch --set-upstream-to=origin/<branch>`를 실행하세요. |
| *The server could not reach the Git repository* | 마지막 확인에서 `git fetch`이 실패했습니다(네트워크 없음, 또는 체크아웃 소유자에게 인증 정보가 없는 비공개 저장소). 표시된 최신 커밋이 오래된 것일 수 있습니다. |
| 업데이트를 요청했는데 아무 일도 일어나지 않습니다 | cron을 쓰면 1분 안에 시작됩니다. 한 시간 동안 아무도 처리하지 않은 요청은 버려집니다. 감시기를 고쳤을 때 예상치 못하게 시작되지 않도록 하기 위함입니다. |
| *Last update: ..., failed* | **Log**를 읽으세요. `git pull`이 실패했다면 보통 체크아웃에 자체 변경 사항(수정된 파일, 로컬 커밋)이 있다는 뜻입니다. 서버에서 이를 해결하세요. 빌드가 실패하면 이전 버전이 계속 실행됩니다. |

감시기의 파일은 데이터 폴더의 `update`에 있습니다. `status.properties`(탭에 표시되는 내용), `update.log`(마지막 업데이트), 그리고 잠시 동안 `request`가 있습니다.
