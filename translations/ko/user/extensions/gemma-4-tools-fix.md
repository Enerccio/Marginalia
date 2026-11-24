---
label: Gemma 4 도구 호출 수정
order: 15
---

# Gemma 4 도구 호출 수정

결과를 받을 역할이 다른 API를 사용하는 Gemma 4 모델에서 함수 호출이 작동하도록 합니다.

!!!warning 최신 채팅 템플릿에서는 필요 없음
이 확장은 오래된 Gemma 4 모델에만 로드하세요. 2026년 4월부터 Gemma 4의 Google 채팅 템플릿은 Marginalia가 보내는 역할 `tool`로 결과를 읽습니다(그 이후의 모든 GGUF에 이것이 들어 있습니다). 이런 템플릿에서는 확장이 프롬프트를 오히려 나쁘게 만듭니다. 결과가 템플릿이 모르는 추가 턴으로 전달되기 때문입니다. llama.cpp도 예전 형식을 무시하므로, 그곳에서는 역할 `tool`만 작동합니다.

오래된 GGUF는 만들어질 때의 템플릿을 그대로 유지합니다. 확인하려면 llama.cpp의 `/apply-template`로 함수 호출과 그 결과를 렌더링한 뒤 모델 턴에서 `<|tool_response>`를 찾아보세요. 있다면 확장을 로드하지 마세요. 호출을 실제로 하지 않고 텍스트로 작성하는 모델은 대신 [모델 설정](../inference-providers.md)의 *Function call rule*로 도움을 받을 수 있습니다.
!!!

<a id="how-it-works"></a>

## 작동 방식

모델이 함수를 호출하면 Marginalia는 결과를 역할 `tool` 메시지로 모델에 돌려보냅니다. Gemma 4를 실행하는 일부 서버는 대신 역할 `tool_responses`를 기대하며, 그렇지 않으면 오류를 내거나 결과를 무시합니다.

확장은 이런 모델에 대해 역할을 바꿉니다. [추론 제공자](../inference-providers.md)의 *Model*에 `gemma4` 또는 `gemma-4`이 (대소문자 구분 없이) 포함되어 있으면, 해당 모델에 대한 함수 호출 결과는 `tool_responses`로 전송됩니다. 다른 모델에 대한 요청은 건드리지 않습니다. 이 확장은 SillyTavern 확장 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix)를 포팅한 것입니다.

<a id="usage"></a>

## 사용법

별도로 설정할 것은 없습니다. *Admin → Extensions*에서 확장을 로드하면 다음 요청부터 적용됩니다.
