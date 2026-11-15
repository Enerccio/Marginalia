---
label: 음성 서버 설정
order: 28
---

# 오디오북을 위한 음성 서버 설정

[오디오북](audiobook.md) 확장 기능은 HTTP로 접근할 수 있는 **Chatterbox-TTS-Server**([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT 라이선스)가 필요합니다. 이 페이지는 서버 쪽을 다룹니다. 서버 구성은 한 대의 머신(AMD Radeon AI PRO R9700 32 GB, 모든 요청 앞에 LiteLLM, `llama-server` 라우터 모드)에서 시험되었습니다. 여러분의 하드웨어와 프록시에는 다른 값이 필요할 수 있습니다.

<a id="which-setup-is-yours"></a>

## 어떤 구성인지 확인하기

| 여러분의 상황 | 할 일 |
|---|---|
| 음성 서버가 **다른 머신**, CPU, 또는 언어 모델 **옆에 자리가 충분한** 카드에서 돌아갑니다 | 서버를 실행하고, 그 주소를 백엔드에 입력한 뒤 **GPU 잠금을 선택하지 않습니다**. 그 밖에 필요한 것은 없습니다. |
| 음성 서버와 언어 모델이 **카드 하나를 공유**하며 둘이 함께 들어가지 않습니다 | [GPU 잠금](../administration/gpus.md)이 필요합니다. 그 **획득(acquire)** 단계는 카드를 비우고 음성 서버를 시작하며, **해제(release)** 단계는 그것을 멈춥니다. 이 페이지의 나머지 내용입니다. |
| 언어 모델이 클라우드 모델입니다 | 비울 것이 없습니다. 음성 서버는 원하는 곳에서 실행하고 GPU 잠금은 선택하지 않습니다. |

정리하면, **사전(pre) 및 사후(post) 요청은 두 모델이 한 GPU를 번갈아 쓸 때만 필요합니다.** 이 요청이 번갈아 쓰기를 가능하게 하며, 플러그인은 *배치*(한 번의 클릭으로 시작한 전체 작업) 전후에 한 번씩 요청을 보냅니다. 각 부분마다 보내지는 않습니다.

<a id="1-the-speech-server"></a>

## 1. 음성 서버

Chatterbox-TTS-Server를 컨테이너(또는 소스에서)로 실행해서 `GET /api/model-info`에 응답하게 하세요. 중요한 점은 다음과 같습니다.

- **모델.** Turbo(`chatterbox-turbo`)가 확장 기능의 기본값이며 시험에서 가장 좋은 소리를 냈습니다. 다국어 모델은 책의 언어와 다른 언어의 텍스트에만 필요합니다. 모델은 서버의 `config.yaml`(또는 `POST /save_settings`와 `POST /restart_server`)에서 설정합니다. 백엔드의 *Speech model*은 GPU 잠금의 획득 단계 이후 서버가 로드한 모델과 같아야 합니다.
- **일본어에는 이미지에 `pykakasi`이 필요합니다.** 없으면 서버 로그에 *pykakasi not available - Japanese text processing skipped*가 나타나고 한자를 잘못 읽습니다. 서버의 Dockerfile에 `RUN python3 -m pip install --no-cache-dir pykakasi`을 추가하세요(`COPY . .` 앞에). 같은 조용한 건너뛰기가 히브리어, 러시아어, 중국어 패키지에도 있습니다.
- **음성**은 서버의 `reference_audio/` 폴더에 있는 파일입니다(마운트해서 재시작 후에도 유지되게 하세요). 확장 기능은 배치 전에 배우들의 클립을 스스로 업로드합니다(`POST /upload_reference`). 이름은 클립의 해시로 만들어집니다. 서버에는 삭제 호출이 없어서 클립이 계속 쌓입니다.
- 서버에는 **인증이 없습니다**. 포트를 공개하지 마세요. 사설 네트워크에 없다면 앞단에 프록시를 두세요.
- `restart: "no"`은 다른 무언가가 필요할 때 시작하는 컨테이너용입니다. `restart: unless-stopped`은 부팅 때 시작하며 카드를 대가 없이 계속 점유합니다.

백엔드(*Settings → Extension Settings → Audiobook Settings*)에 서버 주소를 입력하세요. 서버가 이 머신이나 내 네트워크에 있다면 *The server is on this machine or on my network*에 체크하고 **Test connection**을 누르세요. 원하는 응답은 *the server answers, the model is loaded*입니다.
확장 기능은 `/tts`(매개변수 전체: voice, language, exaggeration, CFG weight, temperature, speed, seed)과 `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`로 서버와 통신합니다. 여러분과 서버 사이에 프록시가 있다면 이 모든 것을 전달해야 합니다.

<a id="higgs-audio-v3-in-comfyui"></a>

## ComfyUI 안의 Higgs Audio v3

음성 모델이 **Higgs Audio v3 in ComfyUI**인 백엔드는 Chatterbox-TTS-Server가 아니라 [TTS Audio Suite](https://github.com/comfyanonymous/ComfyUI)의 노드를 갖춘 [ComfyUI](https://github.com/diodiogod/TTS-Audio-Suite)와 통신합니다. 주소는 ComfyUI의 주소입니다(예: `http://gpu-server:8188`). 이 설정은 같은 AMD R9700에서 `yanwk/comfyui-boot:rocm` 이미지로 구성되었습니다.

1. **스위트를 설치하세요.** ComfyUI의 `custom_nodes` 폴더에 설치합니다(`git clone https://github.com/diodiogod/TTS-Audio-Suite`). 스위트의 파이썬 패키지는 컨테이너 안에 설치됩니다. 컨테이너를 다시 만들어도 유지하려면 pip에 볼륨 위의 폴더를 지정하세요. 컨테이너의 환경에 `PIP_USER=true`을 추가하고 `./storage/local:/root/.local`을 마운트하세요.
2. **pip이 이미지의 torch를 바꾸지 않도록 하세요.** CUDA 빌드가 ROCm 빌드를 대체하게 됩니다. 설치된 버전을 파일에 적고, 그 파일을 제약(constraint)으로 사용해 설치하세요.
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   스위트의 `install.py`를 통째로 실행하지 마세요. torch를 다시 설치할 수 있습니다. `librosa`는 그 `requirements.txt`에 없으며, 이것이 없으면 스위트가 로드되지 않습니다.
3. **모델**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b`(약 8 GB)을 쓰거나, 처음 실행할 때 스위트가 내려받도록 두세요. 그래픽 카드의 약 8 GB가 필요합니다.
4. 백엔드에서 **Test connection**을 누르면 ComfyUI와 노드가 있을 때 노드의 모델과 함께 *the server answers*라고 표시됩니다.

확장 기능은 이것으로 다음 일을 합니다. 캐스트의 클립을 ComfyUI의 입력 폴더에 업로드하고(`POST /upload/image`, 클립에서 만든 이름으로), `GET /object_info`로 스위트의 음성과 클립을 나열하고, 조각마다 자체 워크플로를 `/prompt`에 보내고, `/history`를 기다린 뒤, `/view`에서 오디오를 가져옵니다. Marginalia 쪽에 ffmpeg가 있으면 flac으로 받아 마지막 단어 뒤를 잘라내고, 페이드 아웃한 뒤 mp3 또는 wav로 저장합니다. ffmpeg가 없으면 ComfyUI의 mp3를 자르지 않고 그대로 씁니다(이 경우 wav 저장은 작동하지 않습니다). **조각마다 끝난 뒤 `POST /free`을 `{"free_memory": true}`와 함께 호출합니다.** R9700에서는 로드된 모델로 조각을 연달아 세 번째로 처리하자 그래픽 카드가 크래시했고(`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI 종료), 조각 사이에 메모리를 비우면 모델을 로드된 채 유지하면서 크래시를 피할 수 있었습니다. 대신 조각당 약 3초가 추가됩니다. ComfyUI는 배치 전후에 음성 서버와 같은 종류의 요청([요청 내용](#the-requests))으로 시작되고 중지됩니다.

**AMD 카드에서는 fp32를 사용하세요.** 정밀도를 `auto`(ROCm 7.2의 R9700에서 bf16)로 하면 많은 조각이 의미 없는 소리로 나오거나, 효과음으로 바뀌거나(banging에 관한 한 줄이 노크 11번이 됨), 잘못된 음성으로 나왔습니다. 시드를 바꿔도 운만 달라졌습니다. 같은 조각을 fp32로 하니 단어 하나 틀리지 않았습니다. 백엔드의 **Precision**은 기본값이 fp32이며, 카드의 약 16 GB가 필요합니다. 스위트의 Granite 음성 인식도 bf16에서 같은 문제가 있으므로(두 번째 인식부터 의미 없는 출력), 확장 기능은 이것도 float32로 실행합니다.

스위트의 **Fish Audio S2 Pro** 엔진은 그 카드에서 작동하지 않았습니다. 오디오가 잘못된 숫자(NaN)로 나왔습니다. Higgs Audio v3 가중치는 연구용 및 비상업용 라이선스에 따릅니다.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## 언어 모델과 음성을 위한 GPU 하나

언어 모델을 올린 카드에는 음성 모델을 올릴 자리가 없습니다. 관리자는 [Admin → GPUs](../administration/gpus.md)에서 한 번만 교체 설정을 해 둡니다. GPU 하나와, 그 위에 올라갈 수 있는 항목마다 잠금(lock)을 하나씩 만들고, 언어 모델의 AI 설정과 음성 백엔드에서 해당 잠금을 고릅니다. 프록시가 무엇이든 계획은 같습니다:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

음성 백엔드의 배치(또는 샘플) 하나가 실행될 때 GPU에 언어 모델이 올라와 있으면, 음성 잠금의 획득(acquire) 단계를 먼저 실행한 뒤 모든 조각을 처리합니다. 그 후에도 GPU에는 음성 서버가 올라간 채로 남습니다. 언어 모델에 대한 다음 요청은 배치가 끝나기를 기다렸다가, 음성 잠금의 해제(release) 단계(서버 중지)를 실행한 다음 진행합니다. 배치와 샘플이 연달아 여러 번 실행되면 교체 비용은 한 번만 치릅니다.

**순서는 협상할 수 없습니다.** 언어 모델이 카드를 점유한 상태에서 음성 컨테이너를 시작했을 때 테스트에서 호스트 메모리가 가득 찼습니다(RAM 30 GB, 스왑, 그다음 OOM 킬러가 다른 서비스들을 종료함). 따라서 언로드가 **반드시 먼저** 와야 합니다. 첫 번째로 실패한 획득 단계에서 목록이 끝나므로, 꽉 찬 카드 위에서는 아무것도 시작되지 않습니다. 해제 단계가 실패하면 다음 잠금은 아예 로드되지 않으며(GPU가 아직 가득 차 있을 수 있음) 작업은 오류와 함께 실패합니다. 해제 단계는 그중 하나에 무슨 일이 생기든 모두 실행됩니다.

컨테이너를 *중지*하는 이유는, 서버 안에서 모델을 언로드하면 많은 모델 교체 후 카드의 약 1 GB가 점유된 채로 남았기 때문입니다. 컨테이너를 중지하면 모든 것이 해제됩니다.

<a id="the-steps"></a>

### 단계

각 단계는 메서드, 주소, 헤더, 본문, 제한 시간(timeout), 그리고 이후 대기 시간(초)으로 이루어집니다. 헤더는 암호화되어 저장되므로 프록시의 키를 그 안에 적을 수 있습니다(`Authorization: Bearer ...`). 모든 요청 앞에 LiteLLM을 둘 경우, 음성 잠금의 단계는 다음과 같습니다(*Before 1*부터 *3*까지는 획득 단계, *After 1*은 해제 단계입니다):

| | 메서드와 주소 | 헤더 | 본문 | 역할 |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 언어 모델을 언로드합니다. 훅이 직접 응답하며 1~2초 걸립니다. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | 음성 컨테이너를 시작하고 서버가 준비되면 응답합니다(약 13초). |
| Before 3 (서버가 모델 없이 시작하는 경우에만) | `POST http://litellm:4000/gpu/tts/load` | | | 모델을 로드합니다(8~14초, 처음 다운로드할 때는 50초). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 컨테이너를 중지합니다(1초 미만). |

**Higgs** 백엔드의 경우 *Before 2*는 `POST http://litellm:4000/gpu/comfy/start`입니다(ComfyUI가 올라오면 응답하며 약 15초 걸립니다. 모델은 첫 조각과 함께 로드됩니다). *Before 3*은 없고, *After 1*은 `POST http://litellm:4000/gpu/comfy/stop`입니다. 두 패스스루는 `tools/gpu-control`의 README에 있습니다.

언어 모델은 자동으로 돌아옵니다. 다음 일반 요청이 라우터가 모델을 로드하게 만듭니다(큰 모델은 약 1분 걸리며, [프롬프트 캐시 훅](../prompt-cache.md)을 사용하면 세션의 캐시가 복원됩니다). 따라서 배치 이후에 모델을 위해 별도의 요청을 보낼 필요는 없습니다.

LiteLLM 없이 사용하면 같은 요청이 컨테이너를 시작하고 중지하는 도구로 직접 전달됩니다. 지켜야 할 규칙은 위의 순서뿐입니다.

<a id="what-answers-those-addresses"></a>

### 이 주소에 응답하는 것들

- **`X-Llama-Unload`**은 LiteLLM의 훅 `gpu_unload.py`가 응답합니다. 이 훅은 [다른 모델을 위해 GPU 비우기](../prompt-cache.md#freeing-the-gpu-for-another-model)에서 코드와 함께 설명합니다(캐시 훅 없이도 동작합니다). 라우트 `openai/*`을 사용할 경우, 본문의 모델 이름은 `openai/`로 시작해야 합니다. 다른 이름이면 모델이 언로드된 *후에* 400 응답이 옵니다.
- **`/gpu/tts/start`, `/stop`, `/load`**은 저장소의 작은 컨트롤러 `tools/gpu-control`로 연결되는 LiteLLM **패스스루 엔드포인트**입니다. 이 컨트롤러는 이름으로 컨테이너를 시작하고 중지합니다. README에는 **Docker 소켓 프록시**를 포함한 compose 파일(컨트롤러가 컨테이너를 시작하고 중지하는 일만 할 수 있도록 하기 위함이며, 소켓 자체는 호스트에서 root 권한을 가집니다)과, 잘못 설정하기 쉬운 항목들이 있습니다: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. LiteLLM에서는 다음과 같이 합니다:

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

  (컨테이너 이름은 `chatterbox`이고, `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info`는 컨트롤러가 무엇을 기다려야 하는지 알려 줍니다. 컨트롤러의 토큰은 `.env`와 LiteLLM의 환경 변수에만 보관되며, Marginalia는 LiteLLM의 키만 가지고 있습니다).
- **모든 컨테이너와 LiteLLM은 같은 Docker 네트워크를 공유합니다**(`docker network create ai`, 한 번만 실행). 각 compose 파일에 `networks: [default, ai]`과 외부 네트워크 `ai`을 넣으세요. 수동으로 한 `docker network connect`는 컨테이너를 다시 만들 때 사라집니다.

<a id="checking-it"></a>

## 확인하기

1. `curl http://<speech server>/api/model-info`이 응답합니다(시작 후 `loaded`이 true입니다).
2. 백엔드의 *Test connection*이 모델이 로드되었다고 알려 줍니다. GPU 잠금이 서버를 시작하는 유일한 수단이라면, 잠금을 선택한 상태에서 테스트하세요. *서버가 응답함, 로드된 모델 없음*은 중지됐다가 다시 시작되어 아직 로드가 필요한 서버의 응답입니다.
3. **Convert to audio**에서 짧은 부분 하나를 변환하세요. 창에 조각들이 만들어지는 모습이 표시됩니다. 그 후 음성 컨테이너는 중지되어 있어야 하고, `nvidia-smi` / `nvtop` / `rocm-smi`에서 카드가 다시 비어 있는 것을 확인할 수 있어야 합니다.
4. 모델을 로드한 뒤 첫 호출이 오래 걸리면, 이는 엔진이 예열되는 것입니다(한 번만 발생하며, 첫 실행에서 28초였고 이후로는 나타나지 않습니다).
<a id="troubleshooting"></a>

## 문제 해결

| 증상 | 원인 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 서버가 빈 상태로 시작되었습니다(모델을 한 번 언로드하는 시작 래퍼가 있음). 잠금의 획득 단계에 **Before 3** 단계를 추가하십시오. |
| 조각이 몇 개 만들어진 뒤 서버가 *Model not loaded*라고 응답하고, 서버 로그에 `Request received for /api/unload`가 나타남 | Marginalia가 아닌 다른 것이 모델을 언로드하고 있습니다(익스텐션은 `/api/unload`를 호출하지 않습니다). 보통 컨테이너 안의 시작 래퍼로, 서버가 응답할 때까지 기다린 뒤 "부팅을 가볍게 유지하려고" 모델을 언로드합니다. 이 대기는 단일 스레드 서버가 바빠서 첫 조각 이후에야 끝날 수 있으며, 그러면 언로드가 일괄 처리 중간에 떨어집니다(컨테이너 로그의 `urllib` 시간 초과 트레이스백이 바로 그 래퍼가 기다리는 흔적입니다). compose 파일에서 찾을 패턴은 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`입니다. 이것을 `command: ["python3", "server.py"]`로 바꾸십시오. 시작만으로 Turbo가 로드된 상태가 되며, 언로드는 부팅 후 카드를 비워 두기 위한 용도였을 뿐이고, 컨트롤러가 시작하고 멈추는 컨테이너에는 그것이 필요하지 않습니다. 누가 보냈는지 알려면 익스텐션의 로그(*Admin → Extensions → Log*)를 읽으십시오. 이 로그에는 일괄 처리 전후의 모든 요청이 주소와 응답 시각과 함께, 조각이 시작되고 끝나는 시점과 함께 기록됩니다. GPU 잠금의 단계는 애플리케이션 로그에 있습니다. 일괄 처리 중이라고 로그에 적혀 있을 때 오는 언로드나 목록에 없는 주소에서 오는 언로드는 익스텐션의 것이 아닙니다. |
| 일괄 처리가 중간에 멈추고(익스텐션 로그에 *Cannot reach*가 나오거나 서버 로그가 그냥 끊김), 손으로 재시작하면 동작함 | 서버가 죽은 것입니다. 보통 시스템이 메모리를 모두 차지했다는 이유로 강제 종료합니다(호스트에서 `dmesg \| grep -i oom`, 수 GB를 쓰는 `python3` 프로세스). 그러면 익스텐션이 스스로 서버를 재시작하고(일괄 처리 이후의 요청, GPU 잠금의 해제 및 획득 단계, 그 다음 그 이전의 요청), 조각을 한 번 더 만듭니다. 서버를 되살릴 수 없거나 곧바로 다시 죽으면 나머지 일괄 처리는 즉시 실패하며, 로그에 그 이유가 나옵니다. 메모리가 새는 서버가 그 지경까지 가지 않게 하려면 백엔드에서 **Restart the server after (pieces)**를 설정하십시오(예: 100). 그리고 시스템이 그 서버만 종료하도록 컨테이너에 `mem_limit`를 설정하십시오. |
| 변환이 프록시의 응답과 함께 곧바로 실패함 | GPU 잠금의 첫 획득 단계가 실패했습니다(키가 잘못되었거나 언로드 경로를 찾을 수 없음). 목록이 거기서 의도적으로 멈춥니다. |
| 일괄 처리가 시작될 때 호스트의 메모리가 부족해짐 | 언어 모델이 아직 카드에 올라가 있었습니다. 음성 잠금에 언로드 단계가 없거나 첫 번째가 아니거나, 언어 모델의 AI에 같은 GPU의 잠금이 없습니다. |
| 일본어가 무의미한 글자로 읽힘 | 서버 이미지에 `pykakasi`가 없습니다. 그 조각에는 백엔드도 다국어 버전이어야 합니다. |
| 프록시 뒤에서 클립 업로드가 실패함 | 프록시는 multipart 본문(`/upload_reference`)을 변경 없이 전달해야 합니다. LiteLLM 패스스루로는 테스트하지 않았습니다. 동작할 때까지 서버의 음성 파일(`predefined:Name.wav`)을 사용하십시오. |
| 다른 사용자의 일괄 처리가 내 작업 중에 모델을 언로드함 | 한 카드를 쓰는 모든 백엔드와 AI는 같은 [GPU](../administration/gpus.md)의 잠금 이름을 지정해야 합니다. 그래야만 순서를 번갈아 사용합니다. 잠금이 없는 백엔드는 아무도 기다리지 않습니다. 같은 카드에 Marginalia 설치가 둘 있으면 GPU를 공유하지 않습니다. |
