---
label: 음성 서버 설정
order: 28
---

# Audiobook을 위한 음성 서버 설정

[Audiobook](audiobook.md) 확장 기능에는 HTTP로 접근할 수 있는 **Chatterbox-TTS-Server**([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server), FastAPI, MIT 라이선스)가 필요합니다. 이 페이지는 서버 쪽을 다룹니다. 여기 적힌 내용은 한 대의 기기에서 시험한 것입니다(AMD Radeon AI PRO R9700 32 GB, 모든 앞단에 LiteLLM, 라우터 모드의 `llama-server`). 사용자의 하드웨어와 프록시에는 다른 값이 필요할 수 있습니다.

<a id="which-setup-is-yours"></a>

## 어떤 설정이 해당하나요?

| 사용자의 상황 | 할 일 |
|---|---|
| 음성 서버가 **다른 기기**, CPU, 또는 언어 모델 **옆에** 여유가 있는 그래픽 카드에서 돌아가는 경우 | 서버를 실행하고, 그 주소를 백엔드에 입력하고, **GPU 잠금은 선택하지 않습니다**. 그 밖에는 아무것도 필요하지 않습니다. |
| 음성 서버와 언어 모델이 **같은 카드 하나를 공유**하며 함께 들어가지 않는 경우 | [GPU 잠금](../administration/gpus.md)이 필요합니다. 잠금의 **acquire** 단계는 카드를 비우고 음성 서버를 시작하며, **release** 단계는 음성 서버를 멈춥니다. 이 페이지의 나머지 내용이 이것에 관한 것입니다. |
| 언어 모델이 클라우드 모델인 경우 | 비울 것이 없습니다. 음성 서버는 원하는 곳에서 실행하고, GPU 잠금은 선택하지 마세요. |

따라서 **사전(pre) 요청과 사후(post) 요청은 두 모델이 한 GPU를 번갈아 쓸 때만 필요합니다.** 이 요청이 번갈아 쓰기를 가능하게 하며, 플러그인은 각 조각마다가 아니라 *배치*(한 번의 클릭으로 시작한 모든 작업) 단위로 한 번씩 요청을 보냅니다.

<a id="1-the-speech-server"></a>

## 1. 음성 서버

컨테이너(또는 소스)에서 Chatterbox-TTS-Server를 실행해 `GET /api/model-info`가 응답하도록 하세요. 중요한 사항은 다음과 같습니다.

- **모델.** Turbo(`chatterbox-turbo`)가 확장 기능의 기본값이며 시험에서 가장 좋은 소리를 냈습니다. 다국어 모델은 책의 언어와 다른 언어의 텍스트에만 필요합니다. 모델은 서버의 `config.yaml`(또는 `POST /save_settings`와 `POST /restart_server`)에서 설정합니다. 백엔드의 *Speech model*은 GPU 잠금의 acquire 단계 이후 서버가 로드한 모델과 같아야 합니다.
- **일본어에는 이미지에 `pykakasi`가 필요합니다.** 없으면 서버 로그에 *pykakasi not available - Japanese text processing skipped*가 남고 한자를 잘못 읽습니다. 그 Dockerfile에 `RUN python3 -m pip install --no-cache-dir pykakasi`를 추가하세요(`COPY . .` 앞에). 히브리어, 러시아어, 중국어 패키지에도 같은 조용한 건너뛰기가 있습니다.
- **음성**은 서버의 `reference_audio/` 폴더에 있는 파일입니다(유지되도록 볼륨을 마운트하세요). 확장 기능은 배치 전에 등장인물의 클립을 직접 업로드하며(`POST /upload_reference`), 이름은 클립의 해시로 만듭니다. 서버에는 삭제 호출이 없으므로 클립이 계속 쌓입니다.
- 서버에는 **인증이 없습니다**. 포트를 공개하지 마세요. 사설 네트워크에 있지 않다면 앞단에 프록시를 두세요.
- 다른 무언가가 필요할 때 시작하는 컨테이너에는 `restart: "no"`를 사용하세요. `restart: unless-stopped`는 부팅 시 서버를 시작하며 카드를 쓸데없이 계속 점유합니다.

백엔드(*Settings → Extension Settings → Audiobook Settings*)에서 서버 주소를 입력하고, 서버가 이 기기나 내 네트워크에 있으면 *The server is on this machine or on my network*를 체크한 뒤 **Test connection**을 누르세요. 원하는 결과는 *the server answers, the model is loaded*입니다.
확장 기능은 `/tts`(매개변수 전체: voice, language, exaggeration, CFG weight, temperature, speed, seed)와 `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`을 통해 서버와 통신합니다. 사용자와 서버 사이에 프록시가 있다면 이 모든 것을 전달해야 합니다.

<a id="higgs-audio-v3-in-comfyui"></a>

## ComfyUI의 Higgs Audio v3

음성 모델 **Higgs Audio v3 in ComfyUI**를 쓰는 백엔드는 Chatterbox-TTS-Server가 아니라, [TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI)의 노드를 갖춘 [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite)와 통신합니다. 주소는 ComfyUI의 주소입니다(예: `http://gpu-server:8188`). 이는 같은 AMD R9700에서 `yanwk/comfyui-boot:rocm` 이미지로 설정되었습니다.

1. **suite를 설치하세요**: ComfyUI의 `custom_nodes` 폴더에(`git clone https://github.com/diodiogod/TTS-Audio-Suite`) 설치합니다. 그 Python 패키지는 컨테이너 안에 들어갑니다. 컨테이너를 다시 만들 때도 유지하려면 pip에 볼륨의 폴더를 지정하세요. 컨테이너의 환경에 `PIP_USER=true`를 추가하고 `./storage/local:/root/.local`를 마운트합니다.
2. **pip이 이미지의 torch를 교체하지 않도록 하세요**(CUDA 빌드가 ROCm 빌드를 대체해 버립니다). 설치된 버전을 파일에 적고, 그것을 제약 조건(constraint)으로 삼아 설치하세요.
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   suite의 `install.py`를 통째로 실행하지 마세요. torch를 다시 설치할 수 있습니다. `librosa`는 그 `requirements.txt`에 없으며, 이것 없이는 suite가 로드되지 않습니다.
3. **모델**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`(약 8 GB)을 받거나, 처음 사용할 때 suite가 내려받도록 하세요. 그래픽 카드의 약 8 GB가 필요합니다.
4. ComfyUI와 노드가 있으면, 백엔드의 **Test connection**이 노드의 모델과 함께 *the server answers*라고 표시해야 합니다.

확장 기능이 이것으로 하는 일은 다음과 같습니다. 등장인물의 클립을 ComfyUI의 입력 폴더에 업로드하고(`POST /upload/image`, 클립에서 만든 이름으로), `GET /object_info`로 suite의 음성과 클립 목록을 가져오며, 조각마다 자체 워크플로를 `/prompt`에 보내고, `/history`에서 기다린 뒤 `/view`에서 오디오를 가져옵니다(워크플로는 또한 TTS 노드의 생성 정보를 핵심 `PreviewAny` 노드에 넘깁니다. suite는 오류가 나도 1초의 무음과 성공한 실행으로 응답하는데, 이 정보만이 조각이 실패했음을 알려 주므로, 그 조각은 보관되지 않고 이유와 함께 실패 처리됩니다). Marginalia 쪽에 ffmpeg가 있으면 flac으로 받아, 마지막 단어 이후를 잘라내고, 페이드아웃한 다음 mp3 또는 wav로 저장합니다. ffmpeg가 없으면 ComfyUI의 mp3를 그대로 쓰며, 잘라내지 않습니다(이 경우 wav 저장은 동작하지 않습니다). **조각을 하나 처리할 때마다 `{"free_memory": true}`와 함께 `POST /free`를 호출합니다**. R9700에서는 로드된 모델로 연속 세 번째 조각을 처리하자 그래픽 카드가 멈췄습니다(`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI 종료). 조각 사이에 메모리를 비우면 모델은 로드된 채로 유지되면서 이 문제를 피할 수 있으며, 조각당 약 3초가 추가됩니다. ComfyUI는 배치 전후로 음성 서버와 같은 종류의 요청([요청들](#the-requests))을 통해 시작되고 중지됩니다.

**AMD 카드에서는 fp32를 사용하세요.** 정밀도 `auto`(ROCm 7.2의 R9700에서는 bf16)로 하면 많은 조각이 횡설수설하거나, 효과음처럼 나오거나(쾅 소리에 관한 대사 한 줄이 노크 11번이 됨), 엉뚱한 목소리로 나왔습니다. 시드를 바꾸면 운만 달라졌습니다. 같은 조각을 fp32로 하면 한 단어도 틀리지 않았습니다. 백엔드의 **Precision**은 기본값이 fp32이며, 카드의 약 16 GB가 필요합니다. suite의 Granite 음성 인식도 bf16에서 같은 문제가 있어(두 번째 인식부터 횡설수설), 확장 기능은 이것도 float32로 실행합니다.

**클립 정리**([클립 정리](audiobook.md#cleanup-of-clips))는 같은 suite의 노드 두 개, `VocalRemovalNode`(분리 모델)과 `VoiceFixerNode`를 더 사용하며, ComfyUI의 `LoadAudio`과 `SaveAudio`와 함께 쓰입니다. 같은 suite의 다른 노드 팩은 필요 없습니다. suite는 처음 사용할 때 모델을 내려받습니다(MelBand Roformer 모델은 각각 약 900 MB입니다). R9700에서는 MelBand 모델(보컬, 노이즈 제거)과 음성 복원기(voice fixer)가 동작했으며, 16초 클립에 약 25초가 걸렸습니다. UVR 모델은 처음에 동작하지 않았습니다.

- `UVR-DeEcho-DeReverb`(*Remove echo*와 *Film or game clip*에서 사용)는 `module 'samplerate' has no attribute 'resample'`와 함께 실패했습니다. 컨테이너에는 `samplerate` 0.1.0이 있었는데, 이는 시스템 라이브러리 `libsamplerate`를 필요로 하는 래퍼이고 이미지에는 그것이 없었습니다. 현재 패키지는 필요한 것을 자체적으로 포함하므로, torch 고정 버전과 함께 설치하고 ComfyUI를 다시 시작하세요.
  ```sh
  PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install --user 'samplerate>=0.2.1'
  ```
  그 후 잔향 제거(de-reverb)는 16초 클립에 약 5초가 걸렸습니다.
- `UVR-DeNoise`는 내려받기에 실패했습니다(suite의 주소가 404로 응답). 사용하려면 파일을 손으로 `models/TTS/UVR/`에 넣으세요. MelBand 노이즈 제거 모델은 이런 단계가 필요 없으며 기본 정리 작업에 포함된 모델입니다.

suite의 **Fish Audio S2 Pro** 엔진은 그 카드에서 동작하지 않았습니다. 오디오가 잘못된 숫자(NaN)로 나왔습니다. Higgs Audio v3 가중치는 연구 및 비상업 라이선스에 따릅니다.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 모델과 음성을 위한 GPU 하나

언어 모델을 올린 카드에는 음성 모델을 올릴 자리가 없습니다. 관리자는 [Admin → GPUs](../administration/gpus.md)에서 한 번만 교체 설정을 합니다. 올릴 수 있는 것마다 GPU와 락(lock)을 하나씩 만들고, 언어 모델의 AI 설정과 음성 백엔드에서 해당 락을 고릅니다. 계획은 어떤 프록시에서도 같습니다:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

음성 백엔드의 배치(또는 샘플)는 GPU에 언어 모델이 올라 있을 때 음성 락의 획득(acquire) 단계를 실행한 뒤 모든 조각을 처리합니다. 그 뒤에도 GPU에는 음성 서버가 올라 있습니다. 언어 모델에 대한 다음 요청은 배치가 끝날 때까지 기다렸다가 음성 락의 해제(release) 단계(서버 중지)를 실행하고 진행합니다. 배치와 샘플이 연달아 실행되면 교체 비용은 한 번만 듭니다.

**순서는 협상할 수 없습니다.** 언어 모델이 카드를 점유한 상태에서 음성 컨테이너를 시작하자, 테스트에서 호스트 메모리가 가득 찼습니다(RAM 30 GB, 스왑, 이후 OOM 킬러가 다른 서비스를 종료함). 따라서 언로드가 **반드시 먼저** 와야 합니다. 획득 단계가 하나라도 실패하면 목록은 거기서 끝나므로, 가득 찬 카드 위에서 아무것도 시작되지 않습니다. 해제 단계가 실패하면 다음 락은 아예 로드되지 않으며(GPU가 여전히 가득 차 있을 수 있음), 작업은 오류와 함께 실패합니다. 해제 단계는 하나에 무슨 일이 생기든 모두 실행됩니다.

**Higgs, 음성 인식, 하나의 ComfyUI에서 하는 클립 정리**는 각각 고유한 락을 하나씩 받습니다. 세 개 모두 단계는 같습니다(획득: 언어 모델 언로드, ComfyUI 시작; 해제: ComfyUI 중지). ComfyUI는 언로드한 모델을 서버 메모리에 그대로 둡니다(fp32 Higgs는 약 16 GB). 그 옆에 음성 인식을 로드하자 테스트에서 서버 메모리가 부족해졌습니다. 락이 세 개이면 전환할 때마다 ComfyUI가 재시작되므로, 한 번에 하나만 로드됩니다.

서버 안에서 모델을 언로드하는 대신 컨테이너를 *중지*하는 이유: 서버 안의 언로드는 모델을 여러 번 바꾼 뒤 카드의 약 1 GB를 점유한 채로 남겼습니다. 컨테이너를 중지하면 모두 해제됩니다.

<a id="the-steps"></a>

### 단계

각 단계는 메서드, 주소, 헤더, 본문, 제한 시간(timeout), 그 뒤의 대기 시간(초)으로 이루어집니다. 헤더는 암호화되어 저장되므로 프록시의 키를 헤더에 적을 수 있습니다(`Authorization: Bearer ...`). 모든 앞단에 LiteLLM을 둔 경우, 음성 락의 단계는 다음과 같습니다(*Before 1*부터 *3*까지가 획득 단계, *After 1*이 해제 단계입니다):

| | 메서드와 주소 | 헤더 | 본문 | 역할 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 언어 모델을 언로드합니다. 훅 자체가 응답하며 1~2초 걸립니다. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 음성 컨테이너를 시작하고 서버가 준비되면 응답합니다(약 13초). |
| Before 3 (서버가 모델 없이 시작하는 경우에만) | `POST http://litellm:4000/gpu/tts/load` | | | 모델을 로드합니다(8~14초, 처음 다운로드하는 경우 50초). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 컨테이너를 중지합니다(1초 미만). |

**Higgs** 백엔드의 경우 *Before 2*는 `POST http://litellm:4000/gpu/comfy/start`입니다(ComfyUI가 올라오면 응답하며 약 15초 걸리고, 모델은 첫 번째 조각과 함께 로드됩니다). *Before 3*은 없으며, *After 1*은 `POST http://litellm:4000/gpu/comfy/stop`입니다. 두 개의 패스스루는 `tools/gpu-control`의 README에 있습니다.

언어 모델은 저절로 돌아옵니다. 다음 일반 요청이 라우터가 모델을 로드하게 합니다(큰 모델은 약 1분; [프롬프트 캐시 훅](../prompt-cache.md)을 쓰면 세션의 캐시가 복원됩니다). 그래서 배치 뒤에 모델을 위한 별도 요청은 필요하지 않습니다.

LiteLLM 없이 같은 요청은 컨테이너를 시작하고 중지하는 무엇이든 보내집니다. 지켜야 할 규칙은 위의 순서뿐입니다.

<a id="what-answers-those-addresses"></a>

### 이 주소에 응답하는 것들

- **`X-Llama-Unload`**은 LiteLLM의 훅 `gpu_unload.py`가 응답합니다. 코드와 함께 [다른 모델을 위해 GPU 비우기](../prompt-cache.md#freeing-the-gpu-for-another-model)에 설명되어 있습니다(캐시 훅 없이도 동작합니다). 라우트 `openai/*`을 쓰는 경우 본문의 모델 이름은 `openai/`로 시작해야 합니다. 다른 이름이면 모델이 이미 언로드된 *후에* 400 응답이 옵니다.
- **`/gpu/tts/start`, `/stop`, `/load`**은 저장소의 작은 컨트롤러 `tools/gpu-control`로 연결되는 LiteLLM **패스스루 엔드포인트**입니다. 이 컨트롤러는 이름으로 컨테이너를 시작하고 중지합니다. 해당 README에는 **Docker 소켓 프록시**를 포함한 compose 파일이 있습니다(컨트롤러가 컨테이너를 시작·중지만 할 수 있고 다른 일은 할 수 없도록 하기 위함이며, 소켓 자체는 호스트에서 root 권한입니다). 또한 잘못 설정하기 쉬운 항목도 있습니다: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. LiteLLM에서는:

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  (컨테이너 이름은 `chatterbox`이고, `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info`는 컨트롤러가 무엇을 기다려야 하는지 알려 줍니다. 컨트롤러의 토큰은 `.env`와 LiteLLM의 환경 변수에만 있으며, Marginalia는 LiteLLM의 키만 갖고 있습니다.)
- **모든 컨테이너와 LiteLLM은 같은 Docker 네트워크를 공유합니다**(`docker network create ai`, 한 번만). 각 compose 파일에 `networks: [default, ai]`과 외부 네트워크 `ai`을 넣으세요. 손으로 한 `docker network connect` 설정은 컨테이너를 다시 만들 때 사라집니다.

<a id="checking-it"></a>

## 확인하기

1. `curl http://<speech server>/api/model-info`이 응답합니다(시작 후 `loaded`이 true입니다).
2. 백엔드의 *Test connection*이 모델이 로드되었다고 알려 줍니다. GPU 락이 서버를 시작하는 유일한 요인이라면, 락을 선택한 상태에서 테스트하세요. *the server answers, no model is loaded*는 중지되었다가 다시 시작되어 아직 로드를 기다리는 서버의 응답입니다.
3. **Convert to audio**에서 짧은 부분 하나를 변환하세요. 창에는 만들어지는 조각들이 표시됩니다. 끝나면 음성 컨테이너가 중지되어 있어야 하며, `nvidia-smi` / `nvtop` / `rocm-smi`에서 카드가 다시 비어 있음을 확인할 수 있습니다.
4. 모델을 로드한 뒤 첫 호출이 오래 걸리면, 엔진이 예열되는 것입니다(한 번만 일어나며, 첫 실행에서는 28초였고 그 뒤로는 나타나지 않았습니다).
<a id="troubleshooting"></a>

## 문제 해결

| 증상 | 원인 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 서버가 빈 상태로 시작되었습니다(모델을 한 번 언로드하는 시작 래퍼를 사용함). 잠금의 획득 단계에 **Before 3** 단계를 추가하십시오. |
| 첫 몇 조각은 만들어지지만, 서버가 *Model not loaded*라고 응답하고 로그에 `Request received for /api/unload`가 나타남 | Marginalia가 아닌 다른 것이 모델을 언로드합니다(확장 기능은 `/api/unload`를 호출하지 않습니다). 보통 컨테이너 안의 시작 래퍼가 서버의 응답을 기다린 뒤 "부팅을 가볍게 하려고" 모델을 언로드하는 경우입니다. 단일 스레드 서버가 바빠서 이 대기는 첫 조각 이후에야 끝날 수 있고, 그러면 언로드가 배치 한가운데에 떨어집니다(컨테이너 로그의 `urllib` 시간 초과 추적 정보는 그 래퍼가 기다리는 중임을 뜻합니다). compose 파일에서 찾을 패턴은 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`입니다. 이것을 `command: ["python3", "server.py"]`로 바꾸십시오. 시작만으로도 Turbo가 로드된 상태가 되며, 언로드는 부팅 후 카드를 비워 두기 위해서만 필요했고, 컨트롤러가 시작하고 중지하는 컨테이너에는 그것이 필요 없습니다. 누가 보냈는지 알려면 확장 기능의 로그(*Admin → Extensions → Log*)를 읽으십시오. 이 로그는 배치 전후의 모든 요청을 주소와 응답된 시각과 함께, 그리고 조각이 시작되고 끝난 시점과 함께 나열합니다. GPU 잠금의 단계는 애플리케이션 로그에 있습니다. 로그에 배치가 실행 중이라고 나와 있을 때 도착한 언로드나, 목록에 없는 주소에서 온 언로드는 확장 기능의 것이 아닙니다. |
| 배치가 중간에 멈춤(확장 기능의 로그에 *Cannot reach*라고 나오거나, 서버 로그가 그냥 끝남)하고, 수동으로 재시작하면 동작함 | 서버가 죽은 것입니다. 보통 시스템이 메모리를 모두 차지했다는 이유로 강제 종료한 경우입니다(호스트에서 `dmesg \| grep -i oom`: 수 GB를 쓰는 `python3` 프로세스). 그러면 확장 기능이 직접 서버를 재시작합니다(배치 이후의 요청, GPU 잠금의 해제 및 획득 단계, 그다음 배치 이전의 요청). 그리고 조각을 한 번 더 만듭니다. 서버를 되살릴 수 없거나 곧바로 다시 사라지면, 배치의 나머지는 즉시 실패하고 로그에 그 이유가 나옵니다. 메모리가 새는 서버가 그 지경까지 가지 않게 하려면, 백엔드에서 **Restart the server after (pieces)**를 (예: 100)으로 설정하고, 시스템이 서버만 강제 종료하도록 컨테이너에 `mem_limit` 제한을 두십시오. |
| Higgs를 ComfyUI에서 사용할 때: 약 15개 조각 후 모든 조각이 *Higgs failed on the server: CUDA out of memory* (`Tried to allocate 1.45 GiB`)로 실패하며, ComfyUI를 재시작할 때까지 계속됨 | Higgs의 오디오 코덱이 절대 해제되지 않습니다. transformers 5.15.1은 로드한 모든 코덱을 캐시에 보관하며(`@lru_cache`: `HiggsAudioV2TokenizerModel`의 메서드), 모델을 다시 로드할 때마다 fp32 기준 약 770 MB의 카드 메모리를 차지합니다. ComfyUI가 모델을 언로드한 뒤에는 스위트가 조각마다 모델을 다시 로드합니다. 백엔드에서 **Restart the server after (pieces)**를 10으로 설정하거나, transformers에서 이 문제가 수정될 때까지 스위트가 모델을 해제할 때 그 캐시를 비우십시오(`HiggsAudioV2TokenizerModel._get_conv1d_layers.cache_clear()`). |
| 변환이 프록시의 응답과 함께 즉시 실패함 | GPU 잠금의 첫 번째 획득 단계가 실패했습니다(잘못된 키, 언로드 경로를 찾을 수 없음). 목록은 의도적으로 거기서 멈춥니다. |
| 배치가 시작될 때 호스트 메모리가 부족해짐 | 언어 모델이 아직 카드에 남아 있습니다. 음성 잠금에 언로드 단계가 없거나 첫 번째가 아니거나, 언어 모델의 AI에 같은 GPU의 잠금이 없습니다. |
| 일본어가 의미 없는 글자로 읽힘 | 서버 이미지에 `pykakasi`가 없습니다. 해당 조각에는 백엔드도 다국어 버전이어야 합니다. |
| 프록시 뒤에서 클립 업로드가 실패함 | 프록시는 multipart 본문(`/upload_reference`)을 변경 없이 전달해야 합니다. LiteLLM 패스스루로는 테스트하지 않았습니다. 동작할 때까지는 서버의 음성 파일(`predefined:Name.wav`)을 사용하십시오. |
| 다른 사용자의 배치가 내 아래에서 모델을 언로드함 | 같은 카드를 쓰는 모든 백엔드와 AI는 같은 [GPU](../administration/gpus.md)의 잠금 이름을 지정해야 합니다. 그래야만 순서대로 차례를 지킵니다. 잠금이 없는 백엔드는 아무도 기다리지 않습니다. 같은 카드에 Marginalia 설치가 두 개 있으면 GPU를 공유하지 않습니다. |
