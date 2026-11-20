---
label: xAI Grok
order: 26
---

# xAI Grok

xAI의 Grok 모델을 [추론 제공자](../inference-providers.md)로 추가합니다. xAI 자체 API를 통해 연결합니다.

Grok은 일반 *OpenAI Compatible* 제공자와 URL `https://api.x.ai/v1`로도 사용할 수 있습니다. 이 확장은 xAI API와 직접 통신하므로, 추론을 추론으로 표시하고, xAI가 보고하는 정확한 토큰 수(추론 토큰 포함)를 집계하며, 이 서비스에 맞춘 모델 목록과 연결 테스트를 제공합니다.

!!!warning 스토리 텍스트는 xAI로 전송됩니다
이 제공자를 사용하는 AI의 모든 요청(스토리가 담긴 프롬프트, 로어북 항목과 요약, 모델의 응답, 에이전트나 명령이 보내는 이미지)은 xAI 서버(`api.x.ai`, 또는 설정한 프록시)로 전송됩니다.
그 이후 데이터의 처리는 API에 대한 xAI의 약관과 개인정보 처리방침에 따릅니다. 다른 제공자는 선택한 서버로 전송하며, 그 서버는 본인이 운영하는 서버일 수도 있습니다. xAI로 보내서는 안 되는 텍스트에는 이 제공자를 사용하지 마십시오. 다른 사용자와 공유하는 서버에서, 이 제공자의 AI를 공유하는 관리자는 그 AI를 사용하는 모든 사람의 스토리 텍스트를 xAI로 보내게 됩니다.
!!!

<a id="installing"></a>

## 설치

[확장 관리](../administration/extensions.md)에서 설명한 대로 *Admin → Extensions*에서 확장을 불러옵니다. 확장을 불러오면 추론 제공자 대화상자의 **Type** 목록에 **Extension** 항목이 생기고, *Per Type Settings* 탭 상단의 **Provider** 목록에 **xAI Grok**이 나타납니다.

<a id="adding-a-grok-provider"></a>

## Grok 제공자 추가하기

1. [xAI Console](https://console.x.ai)에서 API 키를 발급받습니다.
2. **Inference Providers** 탭에서 **Add Inference Provider**를 클릭하고, **Name**을 입력한 뒤, 유형으로 **Extension**을, 제공자로 **xAI Grok**을 선택합니다.
3. *General Settings*(컨텍스트 크기, 응답 크기, [추론](#reasoning))과 *Per Type Settings* 탭의 제공자 설정을 채운 뒤 **Test Connection**을 클릭합니다.

| 필드 | |
|---|---|
| **Api Key** | xAI Console의 키입니다. 암호화되어 저장되며 다시 표시되지 않습니다. 제공자를 편집할 때 다른 키를 입력하려면 **Reset**을 클릭하십시오. |
| **Model** | 사용할 모델입니다(예: `grok-4` 또는 `grok-3-mini`). **Refresh**를 누르면 키로 사용할 수 있는 모델이 나열됩니다. 목록에 없는 이름도 직접 입력할 수 있습니다. |
| **URL** | 선택 사항입니다. 비워 두면 xAI API(`https://api.x.ai/v1`)를 사용하고, 프록시를 통해 요청을 보내려면 값을 설정하십시오. |
| **Timeout (seconds)** | xAI가 응답할 때까지, 그리고 글을 쓰기 시작한 뒤 다음 텍스트가 올 때까지 기다리는 시간입니다. 기본값은 300입니다. |
| **Retries** | xAI가 속도 제한(429)이나 서버 오류(5xx)로 응답하거나 연결할 수 없을 때 요청을 반복하는 횟수입니다. 기본값은 2이며, 0으로 두면 재시도를 끕니다. |
| **Send the reasoning effort** | *Automatic*이면 `grok-3-mini` 모델에만 추론 강도를 보냅니다. *Always*는 모든 모델에, *Never*는 어느 모델에도 보내지 않습니다. |

**Test Connection**은 모델 목록을 가져오고, 선택한 모델에 몇 개의 토큰을 요청합니다. 이때 사용되는 값은 저장하기 전에 대화상자에 입력한 값입니다.

<a id="reasoning"></a>

## 추론

추론 설정은 다른 제공자와 마찬가지로 제공자의 일반 설정에 있습니다. 추론을 켜면 모델이 보여주는 추론이 스토리 부분의 추론으로 전달됩니다. 추론 강도는 이를 지원하는 모델에만 전송됩니다(`grok-3-mini`: *low*, 또는 medium과 high에는 *high*). 다른 Grok 모델은 기본값대로 추론하며 이 매개변수를 거부합니다.

<a id="what-works"></a>

## 지원 기능

스트리밍, 중지, JSON 스키마, 모델이 호출할 수 있는 함수, 첨부 파일로서의 그림(JPEG, PNG)과 텍스트 파일, 공유 AI의 제한, 사용량 집계는 OpenAI 호환 제공자와 같이 동작합니다. 사용량은 xAI가 보고하는 값이며, 추론 토큰은 생성된 토큰으로 계산됩니다. 온도, top p, [프로토콜](../protocols.md)의 페널티는 활성화된 경우 전송됩니다. 일부 추론 모델은 페널티를 거부합니다. 지원하지 않는 기능은 xAI 서버 측 도구(실시간 검색, 코드 실행)와 이미지 또는 동영상 생성입니다.
