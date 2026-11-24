---
label: 확장 기능 관리
order: 70
---

# 확장 기능 관리

관리자 페이지의 **Extensions** 탭에서 설치 전체에 적용되는 [확장 기능](../extensions/index.md)을 설치하고 제거합니다.

![Extensions 탭 화면](../../images/admin-extensions.png)

<a id="installing"></a>

## 설치

**Load Extension (.jar)** 버튼을 클릭하거나(또는 파일을 그 위에 끌어 놓고) 확장 기능의 JAR 파일을 선택합니다. 파일은 [데이터 폴더](index.md#the-data-folder)의 `extensions` 폴더로 복사되고 바로 시작됩니다. *Extension loaded successfully.* 메시지가 나타나면 설치가 완료된 것입니다. 목록에 확장 기능이 나타납니다:

| 열 | |
|---|---|
| **ID** | 이번 실행에서 확장 기능의 내부 번호입니다. |
| **Extension Name** | 확장 기능의 이름입니다. |
| **Version** | 버전입니다. 옆의 **#** 버튼에 마우스를 올리면 확장 기능 JAR의 SHA-256 해시가 표시됩니다. 클릭하면 해시가 복사되며, 예를 들어 작성자가 공개한 해시와 비교할 때 사용할 수 있습니다. |
| **State** | *Active*는 실행 중, *Stopped*는 직접 중지한 상태입니다([중지와 시작](#stopping-and-starting) 참고). *Installed* 또는 *Resolved*는 시작에 실패했다는 뜻입니다([확장 기능이 실패할 때](#when-an-extension-fails) 참고). |
| **Verification** | *Valid* 또는 *Invalid*입니다. 클릭하면 Marginalia가 확장 기능을 시작하기 전에 수행한 검사의 보고서를 읽을 수 있습니다. |
| **Actions** | **Stop** / **Start**, **Log**, **Unload**. |

사용자는 이후에 여는 창부터 확장 기능을 보게 됩니다. 이미 열려 있던 책은 닫았다가 다시 열거나 페이지를 새로 고쳐야 합니다.

JAR 파일을 UI에 접근할 수 없는 서버 등에서 `extensions` 폴더에 직접 복사할 수도 있습니다. 이 파일들은 다음 시작 때 로드됩니다. Docker 환경을 미리 준비할 때도 이 방법을 쓸 수 있습니다. JAR 파일을 `./data/extensions`에 넣으면 됩니다. JAR 파일은 Docker 안에서 빌드할 수도 있습니다. 서버 가이드의 [플러그인](../getting-started/docker-server.md#plugins)을 참고하세요.

!!!warning 이 버전용으로 빌드된 확장 기능을 사용하세요
확장 기능은 한 Marginalia 버전의 사용자 인터페이스 일부를 변경합니다. 응용 프로그램과 같은 소스에서 빌드된 확장 기능을 사용하세요([플러그인 README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md) 참고).
호환되지 않는 확장 기능은 로드에 실패하거나 응용 프로그램의 일부를 망가뜨릴 수 있습니다. 확장 API는 1.0에서 고정되지 않았으므로, 확장 기능도 Marginalia와 함께 업데이트해야 할 수 있습니다.
!!!

<a id="stopping-and-starting"></a>

## 중지와 시작

**Stop**은 확장 기능을 제거하지 않고 즉시 언로드합니다. 확장 기능이 추가했던 화면에서 사라지지만 JAR 파일과 데이터는 남으며, 상태는 *Stopped*로 표시됩니다. 이때 버튼은 **Start**로 바뀌며, 이를 누르면 다시 로드됩니다. 이때도 응용 프로그램 시작 때처럼 먼저 검사하며, 검사에 실패하면 중지 상태로 남고 보고서가 표시됩니다. 중지된 확장 기능은 Marginalia를 다시 시작해도 중지된 상태로 남습니다. 중지된 확장 기능의 새 버전을 업로드하면 시작됩니다.

중지 상태는 `extensions` 폴더에 있는, JAR 파일과 같은 이름에 `.STOPPED`이 덧붙은 빈 파일(`sidequery.jar.STOPPED`)로 기억됩니다. UI에 접근할 수 없는 서버에서는 이 파일을 만들어 확장 기능이 시작되지 않게 하거나, 삭제하여 다음 시작 때 확장 기능이 시작되게 할 수 있습니다.

<a id="the-extension-log"></a>

## 확장 기능 로그

**Log**는 확장 기능의 로그를 엽니다. 이는 Marginalia가 확장 기능에 제공하는 [로깅](../../developer/plugins/overview.md#logging)으로 확장 기능 자신이 기록한 내용입니다. 파일의 마지막 부분을 보여주며, **Refresh**는 파일을 다시 읽고 **Clear**는 파일을 삭제합니다. 파일은 `extensions/<name>.jar.log`이며(예: `sidequery.jar.log`), 2 MB를 넘으면 `.log.old`로 옮겨지고 새 파일이 시작됩니다. 확장 기능을 언로드하면 로그도 삭제됩니다. 아무것도 기록하지 않는 확장 기능의 로그는 비어 있습니다. 확장 기능이 기록한 내용은 응용 프로그램 로그(이름은 `extension.<name>`이며, 각 줄의 수준에 따라 기록됨)에도 남으므로 서버 로그에도 있습니다. Marginalia 자체의 문제는 응용 프로그램 로그에만 남습니다.

![확장 기능의 로그 화면](../../images/extension-log.png)

<a id="removing"></a>

## 제거

**Unload**를 누르면 확인을 요청한 뒤 확장 기능을 즉시 중지하고, `extensions` 폴더에서 JAR 파일(과 로그)을 삭제합니다.
데이터는 남습니다([확장 기능 데이터가 저장되는 곳](../extensions/index.md#where-extension-data-is-kept) 참고). 다시 로드하면 데이터도 돌아옵니다.

<a id="updating"></a>

## 업데이트

확장 기능의 새 버전을 설치하려면 새 파일로 **Load Extension (.jar)**를 사용하세요. 파일 이름은 상관없습니다. 설치된 버전은 언로드되고 JAR 파일은 삭제되며, 새 버전이 그 자리에서 시작됩니다. 새 버전을 로드하지 못하면 업로드 화면에 오류가 표시되고 기존 버전은 계속 실행됩니다.

확장 기능은 *Manage the extensions* 권한이 있는 [API 키](../account.md#api-keys)로 스크립트에서도 설치, 업데이트, 중지, 제거할 수 있습니다. 스크립트는 먼저 서버가 실행 중인 커밋과 플러그인을 빌드한 소스를 비교할 수 있습니다. 이렇게 하면 Marginalia 자체를 업데이트하지 않고도 확장 기능을 넣을 수 있는지 알 수 있습니다([개발자 매뉴얼](../../developer/api.md#plugins-without-a-full-update) 참고).

<a id="when-an-extension-fails"></a>

## 확장 기능이 실패할 때

업로드한 확장 기능을 로드할 수 없으면 업로드 화면에 *Failed to load extension: ...*가 표시되고 JAR 파일은 보관되지 않습니다.

Marginalia는 확장 기능을 시작하기 전에, 실행 중인 버전과 비교하여 검사합니다. 맞지 않는 확장 기능, 일반적으로 다른 Marginalia 버전용으로 빌드된 확장 기능은 로드되지 않습니다. 업로드한 경우에는 검사 보고서가 표시되고 파일은 보관되지 않습니다. `extensions` 폴더에 있던 확장 기능은 목록에 남지만 시작되지 않으며, *Verification* 열에 **Invalid**가 표시되고 보고서는 로그에 있습니다. Marginalia를 업데이트한 뒤에는 검사를 다시 수행하므로, 전에는 작동하던 확장 기능이 이후에 거부될 수 있습니다. 이때는 새 버전의 Marginalia용으로 빌드된 확장 기능 버전을 구해서 로드하세요. 확장 기능이 유효하지 않으면 **작업 공간 상단의 빨간 막대**가 모두에게 이를 알립니다. 관리자는 막대에 마우스를 올리면 어떤 확장 기능인지 볼 수 있고, 막대를 클릭하면 이 페이지가 열립니다. 다른 사용자에게는 관리자에게 알리라는 안내가 나타납니다(직접 중지한 확장 기능은 집계되지 않습니다). 각 확장 기능의 JAR 파일 옆에는 보고서가 담긴 작은 `.valid` 또는 `.invalid` 파일이 있습니다. 이 파일은 확장 기능과 함께 제거되며, 그대로 두어도 됩니다.

![유효하지 않은 확장 기능이 있을 때 작업 공간 위에 표시되는 빨간 막대](../../images/invalid-extensions-warning.png)

![맞지 않는 확장 기능의 검사 보고서와, 중지된 확장 기능(Start 버튼)](../../images/extension-verification.png)

`extensions` 폴더의 확장 기능이 시작에 실패하더라도(예: 다른 버전용으로 빌드된 경우) 다른 확장 기능의 로드는 막지 않습니다. 이 확장 기능은 *Active* 대신 *Installed* 또는 *Resolved* 상태로 목록에 나타나며, **Unload**로 제거할 수 있습니다. 확장 기능이 아닌 파일은 건너뛰어 목록에 나타나지 않으므로, `extensions` 폴더에서 직접 삭제하세요. 오류의 자세한 내용은 로그(`docker compose logs`, 데스크톱 앱은 `desktop/logs/marginalia.log`)에 있습니다.
