---
label: 추론 제공자
order: 980
icon: server
---

# 추론 제공자

추론 제공자는 모델에 대한 연결입니다. API 주소, API 키, 모델과 그 한도(제한값)가 여기에 포함됩니다. 모든 책은 제공자를 하나씩 사용합니다. 원하는 만큼 여러 개를 가질 수 있습니다. 예를 들어 이야기 집필에는 대형 클라우드 모델을, 실험에는 작은 로컬 모델을 쓸 수 있으며, 책마다 언제든 이들 사이를 전환할 수 있습니다.

제공자는 사용자 계정에 속합니다. 다른 사용자는 여러분의 제공자나 API 키를 볼 수 없습니다. API 키는 암호화되어 저장됩니다([데이터 폴더](administration/index.md#the-data-folder) 참조).

제공자를 추가하려면 **Create own models** 권한이 필요합니다. 이 권한은 관리자가 부여하며, 관리자는 항상 이 권한을 가지고 있습니다([권한](administration/permissions.md) 참조). 이 권한이 없으면 관리자가 여러분과 [공유](#shared-providers)한 제공자만 사용할 수 있고, **Add Inference Provider** 버튼은 표시되지 않습니다.

![추론 제공자 탭](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## 지원되는 API

Marginalia는 스트리밍을 포함한 **OpenAI Chat Completions** 인터페이스를 구현하는 모든 API와 함께 작동합니다(`/v1/chat/completions`). 대부분의 서비스와 로컬 서버가 이를 지원합니다.

| 서비스 | OpenAI URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

포트는 각 서버의 기본값입니다. 실제로 서버가 실행되는 포트를 사용하세요.

!!!warning 포트 8080
Docker 버전의 Marginalia도 포트 8080을 사용합니다. 같은 컴퓨터에서 llama.cpp를 실행한다면, 다른 포트에서 시작하세요(`llama-server --port 8081`).
!!!

!!!
[Docker 버전](getting-started/docker-server.md)에서 `localhost`은 Marginalia 컨테이너를 뜻합니다. 같은 컴퓨터에 있는 모델 서버에는 `http://host.docker.internal:<port>/v1`을 사용하세요. [같은 컴퓨터의 모델](getting-started/docker-server.md#models-on-the-same-machine)을 참조하세요.
!!!
<a id="adding-a-provider"></a>

## 공급자 추가

**Inference Providers** 탭을 열고 **Add Inference Provider**를 클릭합니다. 기존 공급자는 연필 버튼으로 편집하고 휴지통 버튼으로 삭제합니다. 복제 버튼(두 장의 종이 모양)을 누르면 확인한 이름(원래 이름에 *(Copy)*를 붙인 이름)으로 공급자의 사본이 만들어집니다. 사본은 주소, 모델, **API key**(또는 확장 기능의 설정), 추론 설정, 토큰 제한과 매개변수가 원본과 같으므로, 같은 서비스의 다른 모델을 추가하려면 이름만 정하고 클릭 한 번이면 됩니다. 사본은 공유되지 않으며 자체 한도나 사용량을 갖지 않습니다. 본인이 만든 공급자만 복제할 수 있고(관리자가 공유한 공급자는 복제할 수 없습니다. 그 키는 볼 수 없어야 하기 때문입니다), 공급자를 만들 권한이 있을 때만 복제할 수 있습니다.

대화상자 상단의 필드는 다음과 같습니다:

| Field | |
|---|---|
| **Name** | 공급자의 이름입니다. 책에서 모델을 고를 때 표시됩니다. 모델 이름을 포함하세요. 예: `OpenRouter - Claude Sonnet`. |
| **Type** | API의 종류입니다. **OpenAI Compatible**, 또는 [확장 기능](extensions/index.md)이 제공하는 공급자를 위한 **Extension**입니다. **Extension**은 해당 확장 기능이 로드된 동안에만 선택할 수 있습니다(예: [Google Gemini](extensions/google-genai.md) 또는 [Anthropic Claude](extensions/anthropic.md)). 공급자를 만든 뒤에는 유형을 바꿀 수 없습니다. |
| **Provider** | 유형이 **Extension**일 때만 나타납니다. 이 공급자가 어느 확장 기능의 공급자인지 정합니다. 그 확장 기능의 설정이 *Per Type Settings* 탭을 채웁니다. |

<a id="per-type-settings"></a>

### 유형별 설정

![모델 목록이 불러와진 상태의 Per Type Settings](../images/inference-provider.png)

이 탭은 대화상자보다 길어서 스크롤됩니다. 아래쪽 필드는 다음과 같습니다:

![시간 제한, 재시도, 추가 매개변수 및 Test Connection](../images/inference-provider-2.png)

| Field | |
|---|---|
| **OpenAI URL (ends with /v1)** | API의 기본 주소입니다. [위의 표](#supported-apis)를 참고하세요. `/chat/completions` 없이 입력합니다. |
| **Api Key** | 서비스의 키입니다. 로컬 서버는 보통 키를 확인하지 않으므로, 이 필드를 비워 두면 Marginalia가 자리표시자 `none`를 보냅니다. |
| **Model** | 사용할 모델입니다. **Refresh**를 클릭하면 API에서 모델 목록을 불러오며, 모델 ID를 직접 입력할 수도 있습니다(예: OpenRouter의 `anthropic/claude-sonnet-4.5`). |
| **Timeout (seconds)** | API가 응답할 때까지, 그리고 글쓰기를 시작한 뒤 다음 텍스트가 올 때까지 기다리는 시간입니다. 기본값은 300입니다. 추론 모델은 첫 텍스트가 나오기까지 몇 분이 걸릴 수 있습니다. 이 시간 동안 아무 응답도 없는 요청은 시간 초과 메시지와 함께 실패합니다. 모델의 생각 과정도 텍스트로 계산되므로, 계속 생각하는 모델은 중단되지 않습니다. 변경 사항은 다음 요청부터 적용되며, 작업 중인 에이전트의 요청에도 적용됩니다. |
| **Retries** | API가 속도 제한(429) 또는 서버 오류(5xx)로 응답하거나 연결할 수 없을 때 요청을 몇 번 다시 시도할지 정합니다. 재시도 간격은 점점 늘어나며, API의 `Retry-After` 헤더를 따릅니다. 기본값은 2이고, 0이면 재시도를 끕니다. 요청의 시작 부분만 다시 시도되며, 이미 도착한 텍스트가 두 번 쓰이는 일은 없습니다. |
| **Tokenizer** | Marginalia가 이 공급자의 텍스트 토큰을 세는 방식입니다. **Automatic**(기본값)은 서버의 토크나이저 엔드포인트(OpenAI, llama.cpp, LiteLLM)를 차례로 시도해 가장 먼저 응답하는 것을 사용하고, 응답하는 것이 없으면 로컬 추정치를 사용합니다. 하나를 고르면 그것만 사용합니다. **LOCAL_TOKKIT**은 서버를 전혀 호출하지 않는 로컬 추정입니다. 토큰을 세느라 서버가 다른 모델을 불러와야 하는 경우에 유용합니다. 플러그인의 토크나이저도 여기에 나타나며, 주소, 모델, 텍스트를 받지만 API 키는 받지 않고, 사용자가 직접 고를 때만 사용됩니다. **Test tokenizer**는 샘플 텍스트를 공급자가 시도할 방법들로 세어 보고, 각각이 무엇을 응답했는지 보여 줍니다. 공급자를 새로 만들면, 사용자가 직접 고르기 전까지 주소에 맞는 플러그인 토크나이저가 미리 선택됩니다. |
| **Send the thinking back** | 모델이 다음 요청에서 자신의 생각 과정을 돌려받을지 정합니다. 자세한 내용은 [Send the thinking back](#send-the-thinking-back)을 참고하세요. OpenAI 호환 공급자에만 해당합니다. Anthropic과 Google 공급자는 각 API가 필요로 하는 것을 스스로 보냅니다. |
| **Additional Parameters** | 이 공급자로 보내는 모든 요청에 추가할 필드를 담은 JSON 객체입니다. 예: 로컬 서버용 `{"top_k": 40, "min_p": 0.05}`, OpenRouter용 `{"provider": {"order": ["anthropic"]}}`. 여기에 설정한 필드는 [프로토콜](protocols.md)(예: `temperature`)이나 응답 한도(`max_completion_tokens`)의 같은 설정보다 우선합니다. 올바른 JSON 객체이거나 비어 있어야 합니다. |

기존 공급자를 편집할 때 API 키는 다시 표시되지 않습니다. **Reset Api Key**를 클릭해 새 키를 입력하지 않는 한 현재 키가 그대로 유지됩니다.

**Refresh**는 양식의 URL과 키로 API에 모델 목록을 요청합니다(`/v1/models`). *Failed to download model list*라는 메시지와 함께 실패하면 URL(`/v1`로 끝나야 합니다), 키, 서버가 실행 중인지 확인하세요. 일부 서비스는 모델 목록을 제공하지 않으므로, 이때는 모델 ID를 직접 입력하세요.

**Test Connection**은 저장하지 않고 양식의 값을 확인합니다. 재시도 없이 API에 모델 목록을 요청한 다음, 선택한 **Model**로 한 토큰짜리 응답을 요청합니다. 성공 메시지는 URL, 키, 모델이 함께 작동한다는 뜻입니다. 실패하면 원인이 표시됩니다(자세한 내용은 [Error messages](#error-messages)를 참고하세요). 일부 서비스는 모델 목록이 없으므로, 이 경우 테스트는 한 토큰짜리 응답만 확인합니다.

<a id="general-settings"></a>

### 일반 설정

![추론 공급자의 General Settings](../images/inference-provider-general.png)

| Field | |
|---|---|
| **Max Context Size** | 필수입니다. 프롬프트와 응답을 합쳐 모델이 받아들이는 전체 토큰 수입니다. |
| **Max Response Tokens** | 필수입니다. 모델이 쓸 수 있는 가장 긴 응답입니다. API에 `max_completion_tokens`로 전송되며, 이만큼의 컨텍스트가 응답을 위해 비워 둡니다. *Max Context Size*보다 작아야 합니다. |
| **Connections** | 이 모델로 동시에 진행 중일 수 있는 요청의 수입니다. 모든 사용자와 모든 종류의 요청(사용자의 생성, 부가 질의, 번역, 주석, [에이전트](agents.md#one-model-several-agents), 플러그인)을 합산합니다. 나머지 요청은 도착한 순서대로 차례를 기다립니다. 대기 중인 생성 요청에는 *Waiting for the model: N requests before this one*가 표시되며, **Stop**을 누르면 대기열에서 빠집니다. 기본값은 **1**입니다. 슬롯이 하나뿐인 로컬 서버(GPU 한 개)에 적합합니다. 한 번에 여러 요청에 응답하는 클라우드 모델은 더 큰 값을 설정해, 생성 요청과 나란히 부가 질의를 실행할 수 있습니다. |
| **GPU lock** | 관리자가 [GPU](administration/gpus.md)를 설정한 경우에만 나타납니다. 이 모델이 실행되는 GPU의 잠금입니다. 카드를 다른 모델이나 이미지·음성 서버와 공유할 때 사용합니다. 이렇게 하면 모든 요청은 GPU에서 차례를 기다리고(앞선 요청 수와 함께 계산됨), GPU에 다른 것이 올라가 있으면 그것을 내린 뒤 잠금의 단계에 따라 이 모델을 불러옵니다. 비워 두면 잠금이 없습니다. |
| **Multimodal (accepts images)** | 모델이 이미지를 이해한다는 뜻입니다. 턴에서 사용하는 [로어북 항목의 이미지](lorebooks/lorebooks.md#editing-in-full-screen)가 텍스트와 함께 전송됩니다. 기본값은 꺼짐입니다. 이미지를 받는 모델에서만 켜세요. 이미지를 지원하지 않는 모델은 요청을 거부하며, 턴이 실패합니다. 로어 블록에 있는 항목의 이미지는 시스템 프롬프트 바로 다음에 별도의 메시지로 전송됩니다(모델의 짧은 답변과 함께). 그 밖의 이미지는 사용자의 프롬프트와 함께 전송됩니다. |
| **Image placement** | (*Multimodal*을 켰을 때) 이미지를 어디에 넣을지 정합니다. *In the system message*(OpenAI 호환 공급자만): 로어 블록의 이미지를 로어 텍스트 옆에서 시스템 메시지 자체에 넣습니다. 이미지를 그곳에서 받아들이는 서버와 채팅 템플릿에서만 사용하세요. 대부분은 그렇지 않으므로 요청이 실패합니다. *Before the text of the user message*: 각 이미지를 해당 메시지의 텍스트 앞에 넣습니다. Gemma 4가 이 방식을 요구합니다. *After the text of the user message*(기본값): 텍스트 뒤에 넣습니다. 오디오와 파일은 항상 텍스트 뒤에 옵니다. |
| **Function definitions** | 모델이 호출할 수 있는 함수를 어디서 읽을지 정합니다(함수를 제공하는 [에이전트](agents.md), 부가 질의, 플러그인에 해당합니다). *In the system message*(기본값): 요청의 도구로만 전달되며, 이는 모델의 채팅 템플릿이 시스템 턴에 넣습니다. *Before* / *After the text of the user message*: 가장 최근 사용자 메시지의 앞이나 뒤에도 함수를 풀어서 적습니다. 도구는 항상 함께 전송되며, 서버가 호출을 인식하려면 이것이 필요합니다. |
| **Function call rule**, **Rule placement** | 함수를 제공하는 모든 요청에 추가되는 규칙입니다. 호출을 실행하는 대신 응답 텍스트로 쓰거나 호출을 멈추는 모델을 위한 것입니다. 예: *You act only through function calls. Never write a call as text in your reply; to use a function, call it.* 위치는 규칙을 어디에 넣을지 정합니다. 시스템 메시지, 또는 가장 최근 사용자 메시지의 앞이나 뒤입니다(기본값은 뒤입니다). Gemma 4는 사용자 메시지에 넣을 때 훨씬 잘 따릅니다. 비워 두면 규칙이 없습니다. |
| **Needs Jailbreak**, **Jailbreak Prompt** | 특정 내용의 작성을 거부하는 모델을 위한 추가 프롬프트입니다. 켜면 jailbreak 프롬프트가 프롬프트의 가장 앞에 놓입니다. |
| **Enable Reasoning**, **Reasoning Effort** | 추론("생각") 모델에 해당합니다. [추론 모델](#reasoning-models)을 참고하세요. |

Marginalia는 **Max Response Tokens**만큼의 컨텍스트를 응답을 위해 비워 두고, 나머지를 프롬프트로 채웁니다. 템플릿, [로어북](lorebooks/index.md) 항목, [요약](books/summaries.md)은 모두 토큰 계산에 포함되며, 들어가지 않는 가장 오래된 이야기 부분은 제외됩니다. 컨텍스트가 32 000 토큰이고 응답이 최대 2 000 토큰이라면, 프롬프트에는 30 000 토큰이 주어집니다.

**Max Context Size**는 서비스 문서에 나온 값을 사용하되, 조금 낮게 설정하세요. 토큰 수는 항상 정확하지 않으며, 실제 한도를 넘는 요청은 실패합니다. [프로토콜](protocols.md)은 일부 책에 대해 두 한도를 모두 낮출 수 있지만, 높이지는 못합니다.
<a id="error-messages"></a>

## 오류 메시지

API가 요청을 거부하면 Marginalia는 내부 오류를 그대로 보여 주는 대신 무엇을 고쳐야 하는지 알려 주고, 그 뒤에 API가 직접 한 말을 덧붙입니다:

![사용자에게 표시된 제공자의 오류](../images/error-dialog.png)

| 메시지 | 원인 |
|---|---|
| *제공자가 API 키를 거부했습니다.* | **Api Key**가 잘못되었거나, 만료되었거나, 비어 있습니다. |
| *이 API 키는 이 모델이나 엔드포인트를 사용할 수 없습니다.* | 키는 유효하지만 이 모델에 접근 권한이 없습니다. |
| *제공자가 설정된 모델을 알지 못합니다.* | 잘못된 **Model**입니다. **Refresh**를 사용해 API가 알고 있는 이름을 확인하세요. |
| *설정된 URL에서 아무것도 찾지 못했습니다.* | 잘못된 **OpenAI URL**입니다. 보통 `/v1`로 끝납니다. |
| *프롬프트가 모델의 컨텍스트에 들어가지 않습니다.* | **Max Context Size**가 모델이 실제로 받는 값보다 큽니다. 값을 낮추세요. |
| *제공자가 요청을 제한하고 있습니다.* | 속도 제한이거나 할당량이 소진되었습니다. 재시도 후에도 그렇습니다. |
| *제공자가 요청을 처리하지 못했습니다.* | 서비스에 문제가 있습니다(5xx). 재시도 후에도 그렇습니다. |
| *제공자가 제때 응답하지 않았습니다.* | **Timeout (seconds)**를 늘리거나 서버를 확인하세요. |
| *제공자에 연결할 수 없습니다.* | 서버가 실행 중이 아니거나 Marginalia에서 접근할 수 없거나, URL에 오타가 있습니다. |
| *제공자가 요청을 거부했습니다.* | 그 밖의 거부입니다. 대개 **Additional Parameters**에 지원되지 않는 필드가 있거나, 모델이 지원하지 않는 추론을 요청한 경우입니다. |

<a id="warning-before-sending"></a>

## 전송 전 경고

Marginalia는 컨텍스트를 **Max Context Size**에서 **Max Response Tokens**를 뺀 값까지만 채웁니다. 완성된 프롬프트가 그보다 여전히 크면(예를 들어 확장 기능이 텍스트를 추가했거나 토큰 수 계산이 틀린 경우), 요청을 보내기 전에 예상 크기를 보여 주는 경고가 나타납니다. 그래도 요청은 전송됩니다. API가 이를 거부하거나 프롬프트를 잘라 낼 수 있습니다.

![프롬프트가 들어가지 않을 수 있다는 경고](../images/context-warning.png)

<a id="reasoning-models"></a>

## 추론 모델

추론 모델은 답하기 전에 먼저 생각합니다. **Enable Reasoning**을 켜면 Marginalia는 선택한 **Reasoning Effort**(*None*, *Low*, *Medium*, *High*)를 `reasoning_effort` 값으로 API에 보냅니다. 노력을 높이면 대개 더 잘 계획된 글이 나오지만, 응답은 느려지고 비용도 더 듭니다. 모든 API가 이 매개변수를 지원하지는 않습니다. 추론을 켰을 때 요청이 실패하면 끄세요.

모델이 추론 내용을 돌려주면(서버에 따라 `reasoning_content` 또는 `reasoning`로), 그 내용은 **View Reasoning** 아래 생성된 부분 위에 표시됩니다. 추론 내용은 이야기의 일부가 아니며, 아래의 **Send the thinking back**을 설정하지 않는 한 모델에게 다시 보내지지 않습니다.

<a id="send-the-thinking-back"></a>

### 생각 되돌려 보내기

**Send the thinking back**(OpenAI 호환 제공자의 *Per Type Settings*에 있음)은 모델의 생각이 다음 요청에서 이전 메시지의 `reasoning_content`로서 모델에게 되돌아갈지를 정합니다:

| 값 | |
|---|---|
| **None** (기본값) | 절대 되돌려 보내지 않습니다. 대부분의 모델과 서버에, 그리고 알 수 없는 필드를 거부하는 API에 알맞습니다. |
| **Between tool calls** | 한 답변의 도구 호출이 계속되는 동안에만 되돌려 보냅니다. 도구를 호출한 모델은 결과와 함께 자신의 생각을 돌려받습니다. 사용자의 새 메시지는 이전 생각 없이 시작합니다. Google이 Gemma 4에 요구하는 방식이 이것입니다(*thoughts must not be removed between the function calls*, 다만 턴 사이에서는 제거되어야 함). |
| **Always** | 생각이 남아 있는 이전 답변에도, 채팅이 그 생각을 보관하는 경우에는 되돌려 보냅니다(Arc Outline의 플롯 마법사가 이렇게 합니다). Gemma 4의 서버처럼 이전 생각을 스스로 버리는 템플릿을 가진 서버는 이를 무시합니다. |

이 설정은 도구 루프(플롯 마법사, 도구를 사용하는 생성기)와 에이전트에 적용됩니다. 에이전트는 한 턴의 도구 호출이 계속되는 동안 이전 라운드의 생각을 돌려받습니다(생각은 콘솔에서 메시지와 함께 보관되며, 저장된 메시지에는 포함되지 않습니다). 서버가 이 필드를 받아들여야 합니다. llama.cpp는 받아들입니다(`reasoning`가 아니라 `reasoning_content`를 읽음). LiteLLM 뒤에 있는 경우에도 마찬가지입니다. 알 수 없는 메시지 필드를 거부하는 제공자에서 요청이 실패하면 *None*으로 두세요.

![생성된 부분의 추론](../images/reasoning-view.png)

추론도 응답에 포함됩니다. 추론 모델에는 더 큰 **Max Response Tokens**를 주세요. 그렇지 않으면 긴 추론 뒤에 글이 잘릴 수 있습니다.

<a id="token-counting"></a>

## 토큰 세기

Marginalia는 이야기의 얼마만큼이 프롬프트에 들어갈지 정하기 위해 토큰을 셉니다. 가능하면 제공자에게 묻고, 다음 방법을 순서대로 시도합니다:

1. OpenAI 토큰 세기 엔드포인트(`/v1/responses/input_tokens`),
2. llama.cpp의 `/tokenize` 엔드포인트,
3. LiteLLM의 `/tokenize` 엔드포인트,
4. LiteLLM의 `/v1/messages/count_tokens` 엔드포인트(Anthropic 모델용),
5. OpenAI 토크나이저를 이용한 로컬 추정.

작동하는 첫 번째 방법이 그때부터 계속 사용됩니다. 로컬 추정은 대부분의 모델에서 근사치에 가깝지만 정확하지는 않습니다. 이것도 **Max Context Size**를 실제 한계보다 조금 낮게 유지해야 하는 또 다른 이유입니다. *Extension* 유형의 제공자는 해당 확장 기능이 말하는 대로 셉니다. [Google Gemini](extensions/google-genai.md)와 [Anthropic Claude](extensions/anthropic.md) 제공자는 제공자에게 세어 달라고 요청합니다.

<a id="prompt-cache"></a>

## 프롬프트 캐시

Marginalia가 한 종류의 작업(새 부분, 주석, 재작성, 요약 등)을 위해 보내는 모든 요청에는 헤더 `X-Session-ID`가 실립니다. 이는 같은 사용자와 같은 종류에 대해 항상 같은 해시 값입니다. 프롬프트 캐시를 유지하는 서버나 프록시는 이것을 이용해, 같은 종류의 이전 요청에 대한 캐시를 찾을 수 있습니다. 이를 모르는 서버는 무시합니다. 디스크에 보관되는 llama.cpp 슬롯 하나에 대한 예시가 있는 [프롬프트 캐시와 세션 ID](prompt-cache.md)를 보세요.

<a id="shared-providers"></a>

## 공유 제공자

여러 사용자가 있는 설치 환경에서 관리자는 제공자를 **공유**할 수 있습니다. 제공자 대화상자에서 **Shared with all users**를 체크하세요(이 체크박스는 관리자에게만 보입니다). 그러면 모든 사용자는 자신의 책의 **Model** 목록과 **Inference Providers** 탭에서 이를 찾게 되며, *Shared* **Yes**로 표시됩니다. 사용자는 이를 사용할 수 있지만, 열거나, 주소나 API 키를 보거나, 변경하거나, 삭제할 수는 없습니다. 이는 소유자만 할 수 있습니다. 공유 제공자를 참조하는 책, 백업, 복원은 그것을 볼 수 있는 모든 사람에게 계속 작동합니다.

![일반 사용자의 Inference Providers 탭에 있는 공유 제공자](../images/provider-shared-user.png)

제공자를 공유하면 다른 사용자들이 소유자의 서비스 할당량을 사용할 수 있게 됩니다. 사용자가 자신의 제공자를 추가하지 못하게 하여 공유된 제공자만 쓰도록 하려면 [사용자와 제공자](administration/users.md#users-and-providers)를 보세요.
<a id="limits-of-shared-providers"></a>

## 공유 제공자의 한도

관리자는 각 사용자가 공유 제공자로 무엇을 할 수 있는지 제한할 수 있습니다. **Inference Providers** 탭에서 공유 제공자를 소유한 관리자는 해당 행의 **Limits** 버튼(자물쇠가 달린 사용자 아이콘)을 누를 수 있습니다. 그러면 설정된 한도가 나열된 표가 열립니다. **Add limit**을 누르면 새 한도를 만들 수 있으며, 사용자와 한도 유형을 고르면 됩니다.

![공유 제공자의 한도](../images/provider-limits.png)

| 한도 유형 | 의미 |
|---|---|
| Unlimited | 제한 없음(한도가 없는 것과 같음). |
| Token limit | 사용자는 기간마다 지정된 토큰 수까지 사용할 수 있습니다. |
| Not available | 사용자는 이 제공자를 사용할 수 없으며, 요청을 보내는 모든 작업이 실패합니다. |

**토큰 한도**에는 **reset**(매일, 월요일부터 매주, 또는 매월; 서버 시간대 기준의 달력 기간)이 있고, 세 가지 값을 각각 비워 두면 제한 없음이 됩니다. 세 가지 값은 보낸 토큰(**sent**), 생성된 토큰(**generated**), 그리고 둘을 합친 값(**in total**)입니다. 먼저 도달한 값이 있으면 다음 reset까지 사용자가 멈춥니다. 세 값을 모두 설정했다면 보낸 값과 생성된 값의 합이 전체 값보다 작아서는 안 됩니다(그렇지 않으면 전체 값에 결코 도달할 수 없습니다). 그렇지 않으면 대화상자가 저장을 거부합니다. 예를 들어 전체 100,000, 보낸 100,000, 생성 20,000은 괜찮지만, 전체 100,000, 보낸 20,000, 생성 10,000은 허용되지 않습니다.

![한도 추가](../images/provider-limit-edit.png)

타이머에 따라 초기화되는 것은 없습니다. 사용한 토큰은 사용자가 새 기간에 처음으로 제공자를 사용하거나(또는 자신의 한도를 확인하거나) 할 때 0부터 시작합니다. 요청의 크기는 전송 전에 알 수 있으므로, 프롬프트가 사용자를 **sent** 또는 **in total** 한도 너머로 밀어낼 요청은 거부됩니다(프롬프트는 Marginalia 자체의 추정치로 측정되며, 이는 서비스의 계산과 조금 다를 수 있습니다). 생성된 토큰은 미리 알 수 없습니다. 요청이 끝날 때 계산되므로, 사용자는 한도에 도달한 뒤에야 멈추며 마지막 응답이 한도를 넘을 수 있습니다. 표에 한도가 없는 사용자는 제한을 받지 않습니다.

생성된 토큰이 한도를 넘지 않도록 하려면 **Hard limit**을 선택하세요(생성된 토큰 한도가 있을 때만 사용할 수 있습니다). 요청은 허용된 가장 큰 응답 크기만큼의 공간을 예약합니다(제공자의 **Max Response Size** 또는 프로토콜의 값 중 낮은 값). Hard limit이 설정되어 있으면, 이 예약 공간이 생성 한도에서 남은 양에 들어가지 않을 때 요청이 거부됩니다. 따라서 제공자의 응답 크기보다 낮은 Hard limit은 모든 요청을 거부합니다. 표와 사용자 정보에는 이런 한도 옆에 *hard*가 표시됩니다. Hard limit은 생성된 토큰에만 적용되므로, **in total** 한도는 마지막 응답 때문에 여전히 넘을 수 있습니다.

모든 사용자는 자신의 탭에서 공유 제공자를 읽기 전용 행으로 봅니다. 공유 제공자의 **info** 버튼(*i*가 적힌 원 모양)을 누르면 자신의 한도를 볼 수 있습니다. 이 기간에 얼마나 사용했는지, 한도 값, 그리고 언제 reset되는지가 표시됩니다. 한도에 도달하면 스토리 편집기, 요약, 확장 기능은 요청을 보내는 대신 오류를 보여 줍니다.

![사용자가 보는 한도와 이 기간의 사용량](../images/provider-limit-info.png)

<a id="usage"></a>

## 사용량

**Inference Providers** 탭은 제공자별로 **Tokens sent**(프롬프트, "prefill")와 **Tokens generated**(응답, 추론 과정 포함)를 보여 줍니다. 이는 모든 책, 요약, 확장 기능에서 사용한 양을 합산한 것입니다. 공유 제공자는 사용자마다 별도의 집계가 있으므로, 자신의 몫만 볼 수 있습니다.

관리자는 탭에 있는 **Usage per user** 버튼도 사용할 수 있습니다. 이 버튼은 모든 사용자가 모든 제공자에서 사용한 양(보낸 토큰, 생성된 토큰, 요청 수, 마지막 요청 시각)을 표로 보여 줍니다.

![사용자별 사용량](../images/provider-usage.png)

이 수치는 서비스가 보고한 값입니다. 보고하지 않는 서비스나 중간에 멈춘 요청은 [Token counting](#token-counting)에 설명된 로컬 추정치로 집계되므로, 이 수치들은 대략적인 값입니다. 텍스트가 도착하기 전에 실패한 요청은 집계되지 않습니다. 관리자가 공유 제공자에 대해 [limit](#limits-of-shared-providers)을 설정하지 않았다면, 집계 수치는 참고용일 뿐입니다.

!!!
Marginalia는 서비스에 토큰 수를 보고해 달라고 요청합니다(`stream_options`와 `include_usage`). 이를 바꾸려면 제공자의 **Additional Parameters**에 직접 `stream_options`를 넣으세요. 예를 들어 `{"stream_options": {"include_usage": false}}`처럼 입력하면 됩니다. 이 값은 Marginalia가 보내는 내용을 대체하며, 이 경우 로컬 추정치가 사용됩니다.
!!!

<a id="using-a-provider-in-a-book"></a>

## 책에서 제공자 사용하기

각 책의 모델은 **About** 탭의 **Model** 필드에서 정합니다. 새 책 모두에 제공자를 사용하려면 **Settings** 탭에서 해당 제공자를 **Default Model**로 선택하세요. 책의 제공자를 바꾸면 다음에 생성되는 부분부터 적용되며, 그 전에 쓰인 부분은 그대로 남습니다.

<a id="troubleshooting"></a>

## 문제 해결

| 문제 | 확인할 사항 |
|---|---|
| *Failed to download model list* | URL이 `/v1`로 끝나는지, API 키, 서버가 실행 중이며 Marginalia에서 접근 가능한지 확인하세요. 서비스에 모델 목록이 없다면 모델 ID를 직접 입력하세요. |
| *The provider did not answer in time.* | 제공자의 **Timeout (seconds)**을 늘리세요. 추론 모델은 몇 분이 필요할 수 있습니다. |
| *The provider is limiting requests.* | 기다리거나 **Retries**를 늘리세요. |
| *Book is missing model.* | 책에 제공자가 없습니다. **About → Model**에서 하나를 선택하세요. |
| *Contextual limit not sufficient.* | 스토리 없이 프롬프트만으로도 컨텍스트에서 응답 토큰을 뺀 크기에 들어가지 않습니다. **Max Context Size**(또는 프로토콜의 **Max Context Tokens**)를 늘리거나, **Max Response Tokens**를 줄이거나, 템플릿과 로어북 항목을 줄이세요. |
| 응답 텍스트가 중간에 멈춤 | **Max Response Tokens**(또는 프로토콜의 **Max Reply Tokens**)가 너무 낮습니다. 특히 추론 모델에서 그렇습니다. |
| 추론이 켜진 상태에서 API 오류 발생 | API가 `reasoning_effort`를 지원하지 않습니다. **Enable Reasoning**을 끄세요. |
