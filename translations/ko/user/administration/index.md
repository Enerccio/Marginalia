---
label: 관리
order: 910
icon: tools
---

# 관리

관리자는 Marginalia 설치본을 관리합니다. 사용자 계정, 데이터베이스 백업, 삭제된 데이터의 정리, 업데이트, 확장 기능이 여기에 속합니다. [최초 실행](../getting-started/first-start.md) 때 만든 첫 번째 계정은 관리자이며, 다른 사용자를 관리자로 지정할 수도 있습니다.

관리자도 자신의 책을 가진 일반 사용자입니다. 다른 사용자의 책, 로어북, 설정은 볼 수 없고, 그 사용자들이 모두에게 공유한 로어북과 프로토콜만 볼 수 있습니다.

<a id="the-admin-page"></a>

## 관리 페이지

관리자에게는 작업 공간 아래쪽에 **Admin** 버튼이 있습니다. [휴대폰 레이아웃](../phone.md#administration)(너비 768픽셀 미만의 창)에서는 서랍 메뉴의 **Admin** 탭이 됩니다. 이 버튼을 누르면 다음 탭이 있는 관리 페이지가 열립니다.

| 탭 | |
|---|---|
| **Users** | 계정 만들기, 편집, 삭제와 사용자에게 [권한](permissions.md) 부여. [Users](users.md)를 참고하세요. |
| **Database Backups** | 전체 데이터베이스의 백업을 수동으로 또는 일정에 따라 만들고 복원합니다. [Database backups](database-backups.md)를 참고하세요. |
| **Cleanup** | 삭제된 데이터를 영구적으로 제거합니다. [Cleanup](cleanup.md)을 참고하세요. |
| **Resource Files** | 더 이상 사용되지 않는 리소스의 파일을 찾고, ZIP으로 내려받거나 디스크에서 삭제합니다. [Resource Files](resource-files.md)를 참고하세요. |
| **Help assistant** | 매뉴얼의 내용에 답하는 채팅입니다. 모델, 프롬프트, 그 비용을 누가 내는지 설정합니다. [Help assistant](help-assistant.md)를 참고하세요. |
| **Agents** | 에이전트와 사용자가 자신의 것을 갖고 있지 않을 때 에이전트가 따르는 시스템 프롬프트와 압축 프롬프트입니다. 비워 두면 내장 프롬프트가 쓰입니다. 정보 아이콘의 **기본 템플릿 삽입**을 누르면 내용이 채워지므로 사본을 편집할 수 있습니다. 사용자가 에이전트로 무엇을 할 수 있는지는 [권한](permissions.md)에서 정합니다. 또한 **에이전트가 사용자의 웹훅으로 메시지를 보낼 수 있음** 설정이 있습니다. 에이전트가 필요로 하고 사용자가 애플리케이션을 닫아 둔 상태이면, 서버가 사용자가 설정에 입력한 주소를 호출합니다([자리를 비운 동안](../agents.md#when-you-are-not-there)). 이것이 허용되지 않는 곳에서는 끄세요. |
| **Statistics** | [쓰기 통계](../books/statistics.md)를 기록할지 여부, 보관 기간, 사용자 한 명 또는 모든 사용자의 통계 삭제를 설정합니다. [Statistics](statistics.md)를 참고하세요. |
| **Jobs** | 외부 AI에 요청한 모든 작업의 기록(누가, 언제, 어떤 호출을, 어떻게 끝났는지), 오래된 항목을 직접 지우는 필터, 지금 실행 중인 작업을 보여 주는 `/jobs` 페이지입니다. [Jobs](jobs.md)를 참고하세요. |
| **Update** | Docker 서버에서 실행 중인 버전, 서버가 따르는 브랜치의 최신 버전, Marginalia를 업데이트하고 재시작하는 버튼을 보여 줍니다. [Updates](updates.md)를 참고하세요. |
| **Extensions** | 확장 기능을 설치하고 제거합니다. [Managing extensions](extensions.md)를 참고하세요. |

![관리 페이지의 Agents 탭](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## 데이터 폴더

Marginalia가 저장하는 모든 것은 실행하는 사용자의 홈 디렉터리에 있는 `.marginalia` 폴더에 들어 있습니다([Docker 설정](../getting-started/docker-server.md)을 쓰는 경우에는 호스트의 `./data`입니다).

| 경로 | |
|---|---|
| `marginalia.sqlite` (`-wal`, `-shm` 포함) | 데이터베이스입니다. 사용자, 책, 로어북, 설정 등 모든 것이 들어 있습니다. |
| `secret.key` | 데이터베이스에 저장된 API 키를 암호화하는 키입니다. 최초 실행 때 만들어지며, 소유자만 읽을 수 있습니다(Linux와 macOS). 이것이 없으면 저장된 API 키를 읽을 수 없습니다. 컴퓨터의 다른 사용자가 이 파일을 읽을 수 있으면 로그에 그렇다고 표시됩니다. 이때는 `chmod 600 secret.key`를 실행하세요. |
| `db-backups/` | [데이터베이스 백업](database-backups.md)입니다. |
| `data/<login>/backups/manuscripts/<book id>/` | 사용자마다의 [책 백업](../books/backups.md)입니다. |
| `extensions/` | 설치된 [확장 기능](extensions.md)의 JAR 파일입니다. |
| `update/` | Marginalia와 Docker 서버의 [업데이트 감시기](updates.md#setting-up-the-watcher)가 서로 주고받는 파일입니다. 상태, 마지막 업데이트의 로그, 요청이 들어 있습니다. |
| `agent-workspaces/<agent id>/` | [에이전트](../agents.md#the-working-copy)의 파일입니다(실행 사이에 보관하는 메모와 보고서). |
| `agent-runs/<run id>/` | 에이전트 실행이 보관하는 내용입니다. 변경 사항을 보여 주기 위한 책의 시작 상태(작업 사본과 함께 삭제됨), 체크포인트, 발견 사항을 포함합니다. 발견 사항은 정리 과정에서 실행이 지워질 때 함께 삭제됩니다. |
| `desktop/logs/` | [데스크톱 앱](../getting-started/desktop-app.md)의 로그입니다. |

설치본 전체를 백업하려면 Marginalia를 멈추고 이 폴더 전체(`secret.key` 포함)를 복사하세요. 실행 중일 때는 데이터베이스를 [데이터베이스 백업](database-backups.md)으로 백업하세요. 책 백업과 확장 기능은 일반 파일이므로 언제든 복사해도 됩니다.
