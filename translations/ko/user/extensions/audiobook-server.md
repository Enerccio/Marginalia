---
label: 음성 서버 설정
order: 28
---

# 오디오북용 음성 서버 설정

[오디오북](audiobook.md) 확장 기능은 HTTP로 접근할 수 있는 **Chatterbox-TTS-Server**([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT 라이선스)가 필요합니다. 이 페이지는 서버 쪽 설정을 다룹니다. 여기 적힌 내용은 한 대의 머신(AMD Radeon AI PRO R9700
32 GB, 모든 요청 앞에 LiteLLM을 두고, 라우터 모드의 `llama-server`)에서 시험한 것입니다. 사용자의 하드웨어와 프록시에 따라 다른 값이 필요할 수 있습니다.

<a id="which-setup-is-yours"></a>

## 어떤 구성인지 확인하기

| 사용 환경 | 해야 할 일 |
|---|---|
| 음성 서버가 **다른 머신**에서, CPU에서, 또는 언어 모델 **옆에** 들어갈 여유가 있는 카드에서 돌아가는 경우 | 서버를 실행하고 그 주소를 백엔드에 넣은 다음, **배치 전후의 요청은 비워 둡니다**. 그 밖에는 아무것도 필요하지 않습니다. |
| 음성 서버와 언어 모델이 **하나의 카드를 나눠 쓰고** 둘이 함께 들어가지 않는 경우 | 배치 전후의 요청이 필요합니다. **전**에는 카드를 비우고 음성 서버를 시작하며, **후**에는 음성 서버를 멈춥니다. 이 페이지의 나머지 내용이 이에 해당합니다. |
| 언어 모델이 클라우드 모델인 경우 | 비울 것이 없으니, 음성 서버는 원하는 곳에서 실행하고 요청은 비워 둡니다. |

요약하면, **배치 전후의 요청(pre, post)은 두 모델이 한 GPU를 번갈아 쓸 때만 필요합니다.** 이 요청들이 번갈아 쓰는 흐름을 만들어 줍니다. 플러그인은 각 파트마다가 아니라 *배치*(한 번의 클릭으로 시작한 전체 작업) 하나를 기준으로 이 요청들을 한 번씩 보냅니다.

<a id="1-the-speech-server"></a>

## 1. 음성 서버

Chatterbox-TTS-Server를 컨테이너로(또는 소스에서) 실행하여 `GET /api/model-info`에 응답하도록 합니다. 중요한 사항은 다음과 같습니다.

- **모델.** Turbo(`chatterbox-turbo`)가 확장 기능의 기본값이며 시험에서 음질이 가장 좋았습니다. 다국어 모델은 책의 언어와 다른 언어의 텍스트에만 필요합니다. 모델은 서버의 `config.yaml`(또는 `POST /save_settings`와 `POST /restart_server`)에서 설정합니다. 백엔드의 *Speech model*은 배치 전 요청 이후에 서버가 로드한 모델과 같아야 합니다.
- **일본어에는 이미지에 `pykakasi`가 필요합니다.** 없으면 서버는 *pykakasi not available - Japanese text processing skipped*를 기록하고 한자를 잘못 읽습니다. 서버의 Dockerfile에 `RUN python3 -m pip install --no-cache-dir pykakasi`를 추가하세요(`COPY . .` 앞에 둡니다). 같은 방식의 조용한 생략이 히브리어, 러시아어, 중국어 패키지에도 있습니다.
- **음성**(보이스)은 서버의 `reference_audio/` 폴더에 있는 파일입니다(유지되도록 볼륨을 마운트하세요). 확장 기능이 배치 전에 등장인물의 클립을 직접 업로드하며(`POST /upload_reference`), 이름은 클립의 해시로 만들어집니다. 서버에는 삭제 호출이 없어서 클립이 계속 쌓입니다.
- 서버에는 **인증이 없습니다.** 포트를 공개하지 마세요. 사설 네트워크에 있지 않다면 앞단에 프록시를 두세요.
- 다른 것이 필요할 때 시작하는 컨테이너에는 `restart: "no"`를 쓰세요. `restart: unless-stopped`는 부팅 시 컨테이너를 시작시켜 아무 이유 없이 카드를 점유합니다.

백엔드(*Settings → Extension Settings → Audiobook Settings*)에서 서버 주소를 입력하고, 같은 머신이나 내 네트워크에 있다면 *The server is on this machine or on my network*를 체크한 뒤 **Test connection**을 누르세요. 원하는 결과는 *the server answers, the model is loaded*입니다.
확장 기능은 `/tts`(음성, 언어, exaggeration, CFG weight, temperature, speed, seed 등 전체 매개변수)와 `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`와 통신합니다. 사용자와 서버 사이에 프록시가 있다면 이 모든 경로를 전달해야 합니다.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## 한 GPU에서 모델과 음성을 함께 쓰기

언어 모델이 올라가 있는 카드에는 음성 모델을 올릴 공간이 없습니다. 계획은 어떤 프록시에서든 같습니다.

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**순서는 협상할 수 없습니다.** 언어 모델이 카드를 점유한 상태에서 음성 컨테이너를 시작하자 시험에서 호스트 메모리가 가득 찼습니다(RAM 30 GB, 스왑, 그다음 OOM 킬러가 다른 서비스를 종료). 따라서 언로드가 **반드시 먼저** 와야 하며, 첫 번째 실패한 요청에서 목록이 중단되므로 빈 카드가 아닌 곳에서는 아무것도 시작되지 않습니다. 배치 *이후*의 요청은 그중 하나에 무슨 일이 생기든 모두 실행됩니다.

서버 안에서 모델을 언로드하는 대신 컨테이너를 *멈추는* 이유: 서버 안에서 언로드하면 모델을 여러 번 바꾼 뒤 카드의 약 1 GB가 계속 점유된 상태로 남았습니다. 컨테이너를 멈추면 모든 것이 해제됩니다.

<a id="the-requests"></a>

### 요청들

목록의 각 요청은 메서드, 주소, 헤더, 본문, 그리고 그 뒤의 대기 시간(초)으로 이루어집니다. 키를 헤더에 직접 쓰지 마세요(양식은 입력한 그대로 헤더를 보여 줍니다). 대신 **Key of the requests around a batch**에 넣으세요. 비워 두면 백엔드의 *Api key*가 전송됩니다. 자체 키가 없는 아래 요청에는 모두 `Authorization: Bearer`로 전달됩니다. 모든 것 앞에 LiteLLM을 두고 키 하나를 쓴다면 목록은 다음과 같습니다.

| | 메서드와 주소 | 헤더 | 본문 | 동작 |
|---|---|---|---|---|
| 사전 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | 언어 모델을 언로드합니다. 응답은 훅 자체가 하며 1~2초 걸립니다. |
| 사전 2 | `POST http://litellm:4000/gpu/tts/start` | | | 음성 컨테이너를 시작하고 서버가 준비되면 응답합니다(약 13초). |
| 사전 3 (서버가 모델 없이 시작하는 경우에만) | `POST http://litellm:4000/gpu/tts/load` | | | 모델을 로드합니다(8~14초, 처음 다운로드할 때는 50초). |
| 사후 1 | `POST http://litellm:4000/gpu/tts/stop` | | | 컨테이너를 중지합니다(1초 이내). |

언어 모델은 스스로 돌아옵니다. 다음 일반 요청이 오면 라우터가 모델을 로드합니다(큰 모델은 약 1분 걸리며, [프롬프트 캐시 훅](../prompt-cache.md)을 쓰면 세션의 캐시가 복원됩니다). 그래서 배치 이후에 이 모델을 위한 요청은 필요하지 않습니다.

LiteLLM 없이 쓴다면 같은 요청을 컨테이너를 시작하고 멈추는 무엇이든으로 보내면 됩니다. 지켜야 할 규칙은 위의 순서뿐입니다.

<a id="what-answers-those-addresses"></a>

### 이 주소들에 응답하는 것

- **`X-Llama-Unload`**는 LiteLLM의 훅 `gpu_unload.py`가 처리하며, 코드와 함께 [다른 모델을 위해 GPU 비우기](../prompt-cache.md#freeing-the-gpu-for-another-model)에 설명되어 있습니다(캐시 훅 없이도 동작합니다). `openai/*` 라우트를 쓴다면 본문의 모델 이름은 반드시 `openai/`로 시작해야 합니다. 다른 이름이면 모델을 언로드한 *뒤에* 400으로 응답합니다.
- **`/gpu/tts/start`, `/stop`, `/load`**는 저장소의 작은 컨트롤러 `tools/gpu-control`로 연결되는 LiteLLM **pass-through 엔드포인트**입니다. 이 컨트롤러는 이름으로 컨테이너를 시작하고 멈춥니다. README에는 **Docker 소켓 프록시**가 포함된 compose 파일(컨트롤러가 컨테이너를 시작하고 멈추는 것 외에는 아무것도 하지 못하도록 제한; 소켓 자체는 호스트에서 root 권한입니다)과 실수하기 쉬운 설정이 있습니다. 설정은 `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**입니다. LiteLLM에서는 다음과 같이 합니다.

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

  (컨테이너 이름은 `chatterbox`이고, `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info`는 컨트롤러에 무엇을 기다릴지 알려 줍니다. 컨트롤러의 토큰은 그 `.env`와 LiteLLM의 환경 변수에만 두며, Marginalia에는 LiteLLM의 키만 있습니다.)
- **모든 컨테이너와 LiteLLM은 하나의 Docker 네트워크를 공유합니다**(`docker network create ai`를 한 번 실행). 각 compose 파일에 `networks: [default, ai]`와 외부 네트워크 `ai`를 넣으세요. 직접 실행한 `docker network connect`는 컨테이너를 다시 만들 때 사라집니다.

<a id="checking-it"></a>

## 확인하기

1. `curl http://<speech server>/api/model-info`에 응답하는지 확인합니다(시작 후 `loaded`가 true).
2. 백엔드의 *Test connection*이 모델이 로드되었다고 알려 줍니다. 배치 전 요청만이 서버를 시작하는 수단이라면, 목록을 설정한 상태에서 테스트하세요. 멈췄다가 다시 시작된 서버가 아직 모델을 로드해야 할 때의 응답은 *the server answers, no model is loaded*입니다.
3. **Convert to audio**에서 짧은 파트 하나를 변환합니다. 창에 만들어지는 조각들이 표시되고, 끝나면 음성 컨테이너가 멈춰 있어야 하며, `nvidia-smi` / `nvtop` / `rocm-smi`에서 카드가 다시 비어 있어야 합니다.
4. 모델 로드 후 첫 호출이 오래 걸린다면, 이는 엔진이 예열되는 것입니다(한 번만 일어나며, 첫 실행에서는 28초가 걸렸고 이후로는 보이지 않았습니다).
<a id="troubleshooting"></a>

## 문제 해결

| 증상 | 원인 |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | 서버가 빈 상태로 시작되었습니다(모델을 한 번 언로드하는 시작 래퍼가 있는 경우). 요청 **Before 3**을 추가하세요. |
| 첫 조각은 만들어지는데, 그 뒤 서버가 *Model not loaded*라고 응답하고 로그에 `Request received for /api/unload`가 찍힘 | Marginalia가 아닌 다른 것이 모델을 언로드하고 있습니다(확장 프로그램은 `/api/unload`를 호출하지 않습니다). 보통 컨테이너 안의 시작 래퍼가 원인인데, 서버가 응답할 때까지 기다렸다가 "부팅을 가볍게 하려고" 모델을 언로드합니다. 단일 스레드 서버가 바빠서 이 대기는 첫 조각 뒤에야 끝날 수 있고, 그러면 언로드가 배치 중간에 떨어집니다(컨테이너 로그의 `urllib` 시간 초과 트레이스백은 그 래퍼가 기다리고 있다는 뜻입니다). compose 파일에서 찾을 패턴은 `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"` 입니다. 이를 `command: ["python3", "server.py"]`로 바꾸세요. 시작만 하면 Turbo가 로드된 상태가 되며, 언로드는 부팅 후 카드를 비워 두려고만 있던 것이므로, 컨트롤러가 시작하고 멈추는 컨테이너에는 필요하지 않습니다. 누가 보냈는지 알려면 확장 프로그램의 로그(*Admin → Extensions → Log*)를 읽으세요. 배치 전후의 모든 요청을 주소와 응답 시각과 함께 나열하고, 조각이 언제 시작하고 끝났는지도 보여 줍니다. 로그상 배치가 실행 중일 때 들어온 언로드나, 목록에 없는 주소에서 온 언로드는 확장 프로그램의 것이 아닙니다. |
| 배치가 중간에 멈춤(확장 프로그램의 로그에 *Cannot reach*가 나오거나, 서버 로그가 그냥 끊김). 직접 재시작하면 동작함 | 서버가 죽은 것입니다. 보통 시스템이 메모리를 전부 가져가서 강제 종료한 경우입니다(호스트에서 `dmesg \| grep -i oom` 확인: 수 GB를 쓰는 `python3` 프로세스). 그러면 확장 프로그램이 서버를 스스로 다시 시작하고(배치 뒤의 요청들, 그다음 배치 앞의 요청들), 조각을 한 번 더 만듭니다. 서버를 되살릴 수 없거나 곧바로 다시 사라지면, 배치의 나머지는 즉시 실패하고 로그에 이유가 적힙니다. 메모리가 새는 서버가 그 지경까지 가지 않게 하려면, 백엔드에서 **Restart the server after (pieces)**를 설정하세요(예: 100). 그리고 시스템이 서버만 죽이도록 컨테이너에 `mem_limit`을 거세요. |
| 프록시의 응답과 함께 변환이 즉시 실패함 | 배치 앞의 첫 요청이 실패한 것입니다(잘못된 키, 언로드 경로를 찾을 수 없음). 목록은 일부러 거기서 멈춥니다. |
| 배치가 시작될 때 호스트의 메모리가 바닥남 | 언어 모델이 아직 카드에 올라 있습니다. 언로드 요청이 빠졌거나 첫 번째가 아닙니다. |
| 일본어가 엉뚱하게 읽힘 | 서버 이미지에 `pykakasi`가 없습니다. 해당 조각에서는 백엔드도 다국어 버전이어야 합니다. |
| 프록시 뒤에서 클립 업로드가 실패함 | 프록시는 multipart 본문(`/upload_reference`)을 그대로 통과시켜야 합니다. LiteLLM pass-through로는 테스트되지 않았습니다. 동작하기 전까지는 서버의 음성 파일(`predefined:Name.wav`)을 사용하세요. |
| 다른 사용자의 배치가 내 모델을 언로드함 | 같은 서버 주소의 배치는 Marginalia 안에서 하나의 게이트를 공유하며, 다른 음성 모델용 배치는 나머지가 끝날 때까지 기다립니다. 음성 서버와 예를 들어 이미지 서버가 한 카드에 있다면, 두 백엔드 모두 **GPU (name)**에 같은 이름을 적어야 서로를 기다립니다(이미지 생성기도 마찬가지). 같은 카드에 Marginalia 설치가 두 개 있으면 게이트를 공유하지 않습니다. |
