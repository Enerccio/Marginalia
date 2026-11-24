---
label: GPU
order: 84
---

# GPU

그래픽 카드는 대개 한 번에 모델 하나만 올려 둘 수 있습니다. 언어 모델, 이미지 모델, 음성 모델 중 하나만 올라가는 식입니다. Admin 페이지의 **GPUs** 탭에서는 한 카드에 있는 모델과 서버가 차례로 카드를 쓰도록 정할 수 있습니다. 그렇게 하면 언어 모델이 카드를 채우고 있는 동안 이미지나 오디오북 작업이 시작되지 않고, 언어 모델이 필요해지면 다시 올라옵니다.

관리자가 한 번 설정해 두면, 이후 각 사용자는 자신의 AI나 백엔드에 잠금을 골라 연결합니다.

<a id="what-it-is-made-of"></a>

## 구성 요소

| | |
|---|---|
| **GPU** | 카드 하나(또는 모델을 한 번에 하나만 올릴 수 있는 장비 하나)입니다. 이름과 **Also used outside Marginalia**가 있습니다([아래](#a-gpu-that-others-use-too) 참고). |
| **GPU lock** | GPU를 쓰는 방식 하나입니다. 즉 그 GPU에 무엇이 올라가 있는지를 뜻합니다. 예를 들어 같은 GPU에 대해 *Language model*, *ComfyUI*, *Speech* 잠금이 있을 수 있습니다. 잠금에는 순서가 있는 두 단계 목록이 있습니다. **acquire steps**는 잠금의 대상을 올리고(컨테이너 시작, 모델 적재, 먼저 내려야 할 것 언로드), **release steps**는 카드에서 그것을 내립니다(컨테이너 중지, 모델 언로드). 세 번째 목록인 **steps before every job**은 잠금의 작업마다 실행됩니다. GPU가 이 잠금을 가지고 있든 없든 실행되며, 가지고 있지 않을 때는 acquire steps보다 먼저 실행됩니다. 다른 것이 카드를 썼을 수도 있으므로 매번 확인해야 하는 내용을 넣는 곳입니다. **Jobs at once**는 이 잠금의 작업이 동시에 몇 개까지 실행될지 제한합니다(한 번에 이미지 하나만 만드는 이미지 서버라면 1, 제한이 없으면 0). |
| **GPU step** | HTTP 요청 하나입니다. 주소, 메서드, 헤더(`Name: value`, 한 줄에 하나씩 쓰며 암호화되어 저장되므로 bearer 키를 여기에 적을 수 있습니다), 본문, 시간 제한(비워 두면 60초), 응답 후 대기 시간으로 이루어집니다. 한 단계를 여러 잠금이 함께 쓸 수 있습니다. 예를 들어 이미지 잠금과 음성 잠금이 모두 먼저 실행하는 *unload the language model* 단계 하나를 둘 수 있습니다. **Test**를 누르면 요청을 즉시 보냅니다. |

![잠금의 창](../../images/gpu-lock-dialog.png)

잠금은 다음 위치에서 고릅니다.

- 언어 모델의 [AI 설정](../inference-providers.md)(일반 설정의 **GPU lock**);
- [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images)의 백엔드;
- [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech)의 백엔드.

**GPU lock** 상자는 잠금이 하나 이상 있을 때만 표시됩니다.

<a id="variables-in-the-steps"></a>

## 단계에서 쓰는 변수

단계에서 작업의 목적을 밝힐 수 있으므로, 하나의 단계가 여러 모델에 쓰일 수 있고 다른 모델을 시험해 볼 때 잠금을 고칠 필요가 없습니다. 주소, 헤더, 본문에서 쓸 수 있습니다.

| 변수 | 의미 |
|---|---|
| `{{model}}` | 작업이 쓰는 모델입니다. AI의 모델 id(AI 설정에 적힌 그대로, 예: LiteLLM을 거치는 `openai/big-32b`), 오디오북 백엔드의 모델(`higgs-audio-v3`, `chatterbox-turbo`), 음성 인식 또는 이미지 백엔드의 모델입니다. 모델이 없는 작업(클립 정리 등)에서는 비어 있습니다. |
| `{{lock}}` | 잠금의 이름입니다. |
| `{{text_model}}` | 텍스트 생성 작업에서만 쓰이며, 그 작업 AI의 모델 id입니다(`{{model}}`와 같습니다). 음성 및 이미지 작업에서는 비어 있습니다. |
| `{{last_text_model}}` | 모든 작업에서 쓰입니다. 작업이 시작하는 순간 기억해 둔, 이 잠금에서 마지막으로 실행된 텍스트 생성의 모델입니다(따라서 텍스트 작업은 자기 모델을 봅니다). 첫 텍스트 생성 전이나 Marginalia를 다시 시작한 뒤에는 비어 있습니다. |

필터로 값을 바꿀 수 있습니다. `{{model | strip:openai/}}`은 값이 해당 접두사로 시작할 때 그 접두사를 제거합니다(뒤쪽 서버가 모르는 LiteLLM 경로 이름을 떼어 낼 때 씁니다). `{{model | last}}`은 마지막 `/` 뒤에 오는 부분만 남기고, `{{model | json}}`는 JSON 문자열에 맞게 이스케이프하며, `{{model | url}}`는 주소에 맞게 인코딩합니다. `{{model | last | json}}`처럼 여러 필터를 이어 쓸 수도 있습니다. acquire steps와 steps before every job은 작업의 값을 받고, release steps는 잠금에서 마지막으로 실행된 작업의 값을 받습니다(언로드해야 할 모델이 그 값입니다). **Test**는 변수를 모두 비운 상태로 단계를 실행합니다.

<a id="how-the-turns-work"></a>

## 차례가 돌아가는 방식

- 모델에 보내는 모든 요청, 모든 이미지, 오디오북의 모든 배치나 샘플은 자신의 AI나 백엔드의 잠금을 **그 작업이 끝날 때까지** 점유합니다.
- GPU에 그 잠금이 이미 올라가 있으면 작업이 즉시 시작하며, 잠금의 **steps before every job**을 제외하고는 아무 단계도 실행되지 않습니다.
- 다른 잠금이 올라가 있으면, 작업은 그 잠금의 작업들이 끝날 때까지 기다립니다. 그다음 그 잠금의 **release steps**가 모두 실행되고(한 단계가 실패해도 전부 실행됩니다), 이어서 새 잠금의 **steps before every job**과 **acquire steps**가 순서대로 실행됩니다. 어느 한 단계가 실패하면 나머지는 중단되고, 작업은 그 단계의 메시지와 함께 실패합니다.
- release step이 실패하면 새 잠금은 아예 올라가지 않습니다(카드가 여전히 꽉 차 있을 수 있기 때문입니다). 작업은 오류와 함께 실패하고, GPU는 원래 가지고 있던 잠금을 그대로 유지합니다. 다른 잠금의 다음 작업이 다시 해제를 시도합니다.
- 작업이 끝난 뒤에도 GPU는 자신의 잠금과 함께 **계속 올라가 있습니다**. 타이머로 언로드되는 것은 없습니다. 열어 둔 채로, 접어 두거나 닫아 둔 이미지 창은 GPU를 점유하지 않지만, 이미지 모델은 언어 모델(또는 다른 잠금)이 카드를 필요로 할 때까지 올라가 있습니다.
- 작업은 들어온 순서대로 기다립니다. 한 작업이 다른 잠금을 기다리기 시작하면, 이미 올라가 있는 잠금의 새 작업들은 그 뒤에서 기다립니다. 그래서 바쁜 언어 모델이 이미지를 영원히 기다리게 만들 수 없습니다.
- 잠금이 없는 AI나 백엔드, 또는 삭제된 잠금을 가진 AI나 백엔드는 아무것도 기다리지 않습니다.

GPU에 무엇이 올라가 있는지는 애플리케이션이 실행되는 동안에만 알려집니다. 재시작 후에는 아무것도 올라가 있다고 알 수 없으므로, 첫 작업은 acquire steps만 실행합니다.

<a id="a-gpu-that-others-use-too"></a>

## 다른 곳에서도 쓰는 GPU

잠금은 Marginalia의 작업 순서만 정합니다. Marginalia가 카드에 무엇이 올라가 있다고 아는 것은 자신의 작업에서 얻은 정보입니다. Marginalia를 재시작한 뒤, 또는 다른 프로그램, 스크립트, 사람이 카드에 모델을 올린 경우(언어 모델을 깨우는 라우터에 대한 요청, ComfyUI 자체 페이지에 넣은 워크플로), Marginalia가 올라가 있다고 생각하는 것은 틀린 정보가 되며, 작업이 꽉 찬 카드에서 시작될 수도 있습니다.

- GPU의 **Also used outside Marginalia**를 켜면 Marginalia는 자신이 아는 정보를 더 이상 믿지 않습니다. 이때는 GPU가 잠금을 바꿀 때만이 아니라 **모든 작업 전에 acquire steps가 실행됩니다**(release steps는 잠금이 바뀔 때만 실행됩니다). 따라서 각 잠금의 acquire steps는 스스로 자리를 만들어야 합니다(다른 모델을 언로드하고, 다른 서버를 중지합니다). 또한 반복 실행해도 안전해야 합니다. 이미 실행 중인 서버를 다시 시작하거나, 올라가 있지 않은 모델을 언로드하거나, 이미 멈춘 컨테이너를 중지하는 요청도 성공으로 응답해야 합니다. 그렇지 않으면 모든 작업이 실패합니다. 이 방식은 작업마다 이런 요청을 추가로 보냅니다.
- **Steps before every job**은 다른 잠금 없이 한 잠금에 대해 같은 일을 합니다. 매번 확인해야 하는 단계만(다른 프로그램의 모델을 언로드하거나, 서버가 응답하는지 확인하는 단계) 작업마다 실행되고, 나머지는 잠금이 바뀔 때만 실행됩니다.
- 이를 처리하는 안전한 곳은 모든 사용자가 거치는 GPU 장비 자체입니다. 예를 들어 *start ComfyUI* 전에 언어 모델을 먼저 언로드하는 프록시나, 언어 모델을 올리기 전에 ComfyUI를 중지하는 라우터 훅을 둘 수 있습니다. 그러면 어떤 프로그램이 요청하든 누구도 카드를 두 번 채울 수 없습니다.

<a id="the-tab"></a>

## 탭

탭에는 GPU 목록이 나오며, 각 GPU에 지금 올라가 있는 잠금(아직 실행된 것이 없거나 언로드된 경우 *Unknown*)과 그 GPU에서 실행 중이거나 기다리는 작업 수가 표시됩니다. 그 아래에는 잠금과 단계가 나오며, 각각 추가, 편집, 삭제 버튼이 있습니다. **Refresh**를 누르면 GPU가 무엇을 가지고 있는지 다시 읽습니다.

![GPU, 잠금, 단계가 표시된 GPUs 탭](../../images/admin-gpus.png)

**Unload now**는 GPU를 수동으로 비웁니다. 예를 들어 Marginalia 바깥의 일에 카드를 쓰고 싶을 때 씁니다. 실행 중인 작업이 끝나기를 기다린 뒤, 올라가 있는 잠금의 release steps를 실행하고, GPU를 unknown 상태로 둡니다(단계가 실패해도 그렇게 하며, 메시지에 어느 단계인지 나타납니다). 다음 작업은 자신의 잠금을 다시 올립니다.

GPU를 삭제하면 그 GPU의 잠금도 삭제됩니다. 단계를 삭제하면 그 단계를 실행하던 잠금에서 빠집니다. 삭제된 잠금을 지정하고 있던 AI와 백엔드는 잠금 없이 실행됩니다.
<a id="an-example-one-card-several-models"></a>

## 예: 카드 하나, 여러 모델

`llama-server` 뒤에 LiteLLM을 두고, 컨테이너를 시작하고 중지하는 [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) 스타일의 프록시를 함께 쓰는 경우:

| 단계 | 요청 |
|---|---|
| 언어 모델 언로드 (Unload the language model) | `POST http://litellm:4000/v1/chat/completions`, 헤더 `X-Llama-Unload: all` 포함 (see [다른 모델을 위해 GPU 비우기](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |

Marginalia에서만 쓰일 때, 각 잠금은 자기 자신이 쓰던 카드만 비우고 그 이상은 비우지 않는다:

| 잠금 | 획득 단계 | 해제 단계 | 동시 작업 수 |
|---|---|---|---|
| Language model | (없음: 라우터가 다음 요청과 함께 모델을 불러온다) | Unload the language model | 1 |
| ComfyUI images | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech recognition | Start ComfyUI | Stop ComfyUI | 1 |
| Clip cleanup | Start ComfyUI | Stop ComfyUI | 1 |

채팅 뒤에 음성 배치를 실행하면 *Unload the language model*(언어 모델 해제)과 *Start ComfyUI*가 실행되고, 다음 채팅 메시지는 먼저 
*Stop ComfyUI*를 실행한다. ComfyUI 잠금들은 일부러 분리되어 있다. 하나에서 다른 것으로 넘어갈 때 ComfyUI를 중지했다가 다시 시작하는데, 이것이 모델이 언로드된 뒤에도 유지하는 메모리를 비우는 방법이다(fp32 Higgs는 서버 메모리의 약 16GB를 유지한다). 그래서 이미지 모델, Higgs, 인식 모델, 정리 모델이 함께 메모리에 올라가 있는 일은 없다.

**Also used outside Marginalia** 설정이면 대신 각 획득이 스스로 공간을 확보해야 한다: *Language model*은 *Stop ComfyUI*로 획득하고, 모든 ComfyUI 
잠금은 *Unload the language model, Start ComfyUI*로 획득한다.

다른 프로그램이 자체 모델(예: 음성 모델 옆의 작은 모델)을 가진 채 **같은 `llama-server` 라우터**를 사용하면, *Unload the language model*이 그 모델까지 내보내고, 그 프로그램의 언로드도 Marginalia의 모델을 내보내게 된다. `tools/gpu-control`에는 라우터의 모든 모델을 언로드하되 지정한 모델은 **제외**하는 호출이 있다. 이는 `POST http://litellm:4000/gpu/llama/unload` 단계이며 요청 본문은 `{"keep": ["{{model | strip:openai/}}"]}`다(README에 패스스루(pass-through) 설명이 있다). 이를 *Language model*의 **모든 작업 전 단계**로 설정한다. 그 잠금을 쓰는 모든 AI는 어떤 모델을 고르든 자신의 모델을 유지한다. 다른 프로그램도 각 작업 전에 자신의 모델로 같은 일을 한다. 유지된 모델만 로드되어 있으면 호출은 아무 일도 하지 않고 즉시 응답하므로, 두 프로그램 중 어느 쪽도 상대를 알 필요가 없다.

**하나의 잠금이 언어 모델과 음성 모델을 함께 제공하면**(두 모델이 카드에서 나란히 실행된다), 그 단계의 `{{model}}`은 음성 작업 중에는 음성 모델이 되는데, 이는 라우터의 어떤 모델과도 일치하지 않아서 각 응답 직후 언어 모델이 언로드되어 버린다. 대신 `{{last_text_model}}`을 유지하도록 설정한다, 값은 `{"keep": ["{{last_text_model | strip:openai/}}"]}`:  잠금의 모든 작업은 작업 종류와 관계없이 마지막 텍스트 생성의 모델을 유지한다. 첫 텍스트 생성 전에는 아무것도 유지되지 않으며, 첫 생성이 자신의 모델을 로드한다.

ComfyUI에 대해 같은 종류의 단계는 `POST http://litellm:4000/gpu/comfy/ensure`이다. ComfyUI가 응답하면 아무 일도 하지 않고, 중지되어 있으면 컨테이너를 시작하며, 실행 중이지만 ComfyUI가 응답하지 않으면(내부에서 충돌했거나 멈춘 경우) 다시 시작한 뒤 준비될 때까지 기다린다. ComfyUI 잠금의 모든 작업 전 단계로 두면, 누가 중지했든 ComfyUI가 살아 있도록 보장한다.

언어 모델의 AI는 *Language model*을, 이미지 백엔드는 *ComfyUI images*를, 음성 백엔드는 *ComfyUI speech*를, 오디오북의 [음성 인식](../extensions/audiobook.md#speech-recognition)과 [클립 정리](../extensions/audiobook.md#cleanup-of-clips)는 각자 자신의 잠금을 고른다.
