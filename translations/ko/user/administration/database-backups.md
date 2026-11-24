---
label: 데이터베이스 백업
order: 90
---

# 데이터베이스 백업

데이터베이스 백업은 Marginalia 데이터베이스의 완전한 사본입니다. 모든 사용자와 그들의 모든 책, 로어북, 설정, 확장 데이터가 포함됩니다. 설치 환경을 실수와 장애로부터 보호하거나 다른 컴퓨터로 옮길 때 사용하세요. 책 한 권만 백업하려면 사용자는 [책 백업](../books/backups.md)을 이용할 수 있습니다.

![Database Backups 탭](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## 백업 만들기

**Database Backups** 탭에서 **Create Backup**을 클릭하세요. Marginalia가 실행되는 동안 사본이 만들어지며, 사람들이 글을 쓰고 있어도 일관성이 유지됩니다. 백업은 **Name**, **Created** 날짜, **Type**(*Manual* 또는 *Scheduled*), **Size**와 함께 목록에 나타납니다. 도구 모음에는 데이터베이스의 현재 크기가 표시됩니다.

각 백업에는 다음 버튼이 있습니다:

| 버튼 | |
|---|---|
| **Download** | 백업 파일(`.sqlite`)을 다운로드합니다. 서버 밖에 사본을 보관하세요. |
| **Restore on restart** | 백업을 복원합니다. [아래](#restoring)를 참조하세요. |
| 휴지통 | 백업을 삭제합니다. |

**Upload Backup**은 백업 파일(`.sqlite` 또는 `.db`)을 목록에 추가합니다. 예를 들어 다른 설치 환경에서 다운로드한 파일일 수 있습니다. 파일은 먼저 검사되며, SQLite 데이터베이스가 아니거나, 손상되었거나, Marginalia 데이터베이스가 아니거나(다른 애플리케이션의 데이터베이스), 실행 중인 버전보다 새로운 Marginalia 버전에서 만들어진 경우에는 그 이유와 함께 *The database can't be restored: ...* 메시지가 표시되고 거부됩니다.

!!!warning 백업에는 비밀 정보가 들어 있습니다
데이터베이스 백업에는 모든 사용자의 비밀번호 해시와 API 키를 포함한 모든 것이 들어 있습니다. API 키는 [데이터 폴더](index.md#the-data-folder)의 `secret.key` 파일로 암호화되어 있으며, 이 파일은 백업에 포함되지 않습니다. 하지만 백업과 `secret.key` 파일을 모두 가진 사람은 API 키를 읽을 수 있습니다. 다운로드한 백업은 서버 자체만큼 안전하게 보관하세요.
!!!

파일은 데이터 폴더의 [데이터 폴더](index.md#the-data-folder) 안 `db-backups` 폴더에 저장됩니다. Marginalia가 스스로 만드는 사본, 즉 [복원](#restoring) 전의 `pre-restore-...`와 [업그레이드](#copy-before-an-upgrade) 전의 `pre-migration-...`도 목록에 포함되며 *Manual*로 표시됩니다. 그래서 예약 백업의 순환 삭제로 지워지지 않습니다. 책 백업과 확장 JAR 파일은 별도의 파일이며 데이터베이스 백업에 포함되지 않습니다.

**업로드된 파일([부분 그림](../books/story-editor.md#images))은 데이터베이스 백업에 포함되지 않습니다.** 데이터베이스는 이 파일들을 [Resources](../resources.md) 탭의 항목으로만 알고 있으며, 파일 자체는 데이터 폴더의 `data/<login>/images` 폴더에 있습니다. 이는 문제가 되지 않습니다. 복원 후에도 항목은 백업 시점의 것이며, 항목이 사용하는 그림은 폴더에 그대로 남아 있습니다. 백업 이후에 추가된 그림 파일은 아무것도 참조하지 않는 상태로 남아 디스크 공간을 차지합니다. 그림을 안전하게 보관하려면 데이터 폴더 전체를 직접 백업에 포함하거나, [책 백업](../books/backups.md#pictures-in-a-backup)에 묶어 넣으세요.

백업을 **다른 설치 환경**에서 복원할 때는 (Marginalia를 중지한 상태에서) 원래 설치 환경의 `secret.key`도 함께 복사하세요. 그렇지 않으면 저장된 API 키를 복호화할 수 없어 모든 사용자가 키를 다시 입력해야 합니다.

<a id="scheduled-backups"></a>

## 예약 백업

**Scheduled Backups** 섹션은 백업을 자동으로 만듭니다:

| 필드 | |
|---|---|
| **Create backups automatically** | 예약 백업을 켭니다. |
| **Schedule (cron)** | 백업을 만들 시점입니다. [아래](#schedule-syntax)를 참조하세요. 필드 아래에 다음 세 번의 시각이 표시됩니다. |
| **Keep scheduled backups** | 보관할 예약 백업의 수입니다. 그보다 오래된 백업은 삭제됩니다. `0`은 모두 보관합니다. 수동 백업과 업로드한 백업은 절대 삭제되지 않습니다. |

적용하려면 **Save Schedule**을 클릭하세요. 상태 줄에는 마지막 예약 백업이 만들어진 시각과 다음 백업 예정 시각이 표시됩니다.

예약된 시각에 Marginalia가 실행 중이 아니었다면 - 글을 쓸 때만 실행되는 데스크톱 앱에서 흔한 경우입니다 - 다음 시작 후 약 30초 뒤에 놓친 백업을 만듭니다. 이 30초 안에 예약을 끄거나 변경하면 그 백업은 취소됩니다.

<a id="schedule-syntax"></a>

### 예약 문법

예약은 다섯 개의 필드로 이루어진 cron 표현식입니다. 예: `minute hour day-of-month month day-of-week`

| 표현식 | 실행 시점 |
|---|---|
| `0 3 * * *` | 매일 3:00에 |
| `0 */6 * * *` | 6시간마다 |
| `30 2 * * 1` | 매주 월요일 2:30에 (요일: `0` 또는 `7`은 일요일, `1`은 월요일...) |
| `0 12 1 * *` | 매월 1일 정오에 |
| `@daily`, `@weekly`, `@monthly` | 자정에 하루에 한 번 / 일주일에 한 번 / 한 달에 한 번 (`@hourly`, `@yearly`도 사용할 수 있습니다) |

시간은 서버의 시간대를 따릅니다. Docker에서는 `TZ` 환경 변수로 설정하세요.

!!!
예약 백업을 끄려면 *Create backups automatically*의 체크를 해제하고 *Save Schedule*을 클릭하세요. 표현식은 유지되므로 나중에 백업을 다시 켤 수 있습니다. 표현식은 여전히 검사되므로 잘못된 표현식은 저장되지 않습니다. 다만 백업이 꺼져 있는 동안에는 필드가 비어 있을 수도 있습니다.
!!!

<a id="restoring"></a>

## 복원

Marginalia가 데이터베이스를 사용하는 동안에는 교체할 수 없으므로, 복원은 **다음 시작** 때 이루어집니다:

1. 백업 옆의 **Restore on restart**를 클릭하고 확인하세요.
   탭에 *Database restore is scheduled. Restart the application to apply it.*가 표시됩니다. **Cancel Restore**를 누르면 이를 취소합니다.
2. Marginalia를 다시 시작하세요. [데스크톱 앱](../getting-started/desktop-app.md)을 종료했다가 다시 실행하거나, [서버](../getting-started/docker-server.md)에서 `docker compose restart`를 실행하세요.

시작할 때 현재 데이터베이스는 `db-backups/pre-restore-<time>-marginalia.sqlite`으로 옮겨지고, 백업이 그 자리를 대신합니다. 이 파일은 목록에 나타나므로, 그 파일을 복원하면 복원을 되돌릴 수 있습니다. 이전 Marginalia 버전의 백업은 자동으로 업그레이드됩니다.

백업은 교체되기 전에 다시 검사됩니다(*Upload Backup*과 같은 검사이며, *Restore on restart*도 이 검사를 통과하지 못한 백업은 거부합니다). 검사에 실패하거나 업그레이드가 실패하면 Marginalia는 현재 데이터베이스를 유지(또는 되돌려 놓고)하고 정상적으로 시작합니다. 거부된 파일은 `db-backups/rejected-restore-<time>-marginalia.sqlite`로 옮겨지며, 로그에 그 이유가 기록됩니다.

백업 이후에 만들어진 모든 것(책, 부분, 설정)은 사라집니다. 로그인한 사용자는 계정이 바뀌었다면 다시 로그인해야 합니다.

<a id="copy-before-an-upgrade"></a>

## 업그레이드 전 복사본

새 버전의 Marginalia가 기존 데이터베이스의 구조를 바꾸려 할 때, 먼저 데이터베이스가 있던 버전을 이름에 담은 사본을 `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite` 이름으로 저장합니다. 사본은 백업 목록에 들어 있으므로, 이전 버전의 Marginalia를 설치하고 그 사본을 [복원](#restoring)하면 업그레이드를 되돌릴 수 있습니다. 최근 세 개의 사본이 보관됩니다. 새 설치에는 복사할 것이 없으며, 사본을 만들 수 없는 경우(예: 디스크가 가득 찬 경우)에는 로그에 그 사실이 기록되고 업그레이드는 계속됩니다.

중간에 실패한 마이그레이션은 자동으로 롤백되며 Marginalia는 시작하지 않습니다. 사본은 업그레이드가 완료되었지만 원하지 않는 결과가 생긴 경우를 위한 것입니다.

<a id="moving-to-another-computer"></a>

## 다른 컴퓨터로 옮기기

1. 백업을 만들고 **Download**하세요.
2. 새 컴퓨터에 Marginalia를 설치하고, 처음 시작할 때 임시 관리자를 만드세요.
3. **Upload Backup**, **Restore on restart**, 다시 시작.
4. 백업에 있던 계정으로 로그인하세요.

이전 [데이터 폴더](index.md#the-data-folder)에서 `data` 폴더(책 백업과 부분의 그림)와 `extensions` 폴더도 함께 복사하세요. 또는 Marginalia를 중지하고 `.marginalia` 폴더 전체를 복사하면 됩니다.
