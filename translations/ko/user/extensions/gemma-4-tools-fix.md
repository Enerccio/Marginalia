---
label: Gemma 4 도구 수정
order: 15
---

# Gemma 4 도구 수정

Gemma 4 모델처럼 함수 호출 결과에 다른 역할(role)을 기대하는 API를 가진 모델에서도 함수 호출이 작동하도록 합니다.

<a id="how-it-works"></a>

## 작동 방식

모델이 함수를 호출하면 Marginalia는 그 결과를 역할이 `tool`인 메시지로 모델에 다시 보냅니다. Gemma 4를 실행하는 일부 서버는 대신 역할 `tool_responses`를 기대하며, 그렇지 않으면 오류를 내거나 결과를 무시합니다.

이 확장 기능은 그런 모델에 대해 역할을 바꿉니다. [추론 제공자](../inference-providers.md)의 **모델**에 `gemma4` 또는 `gemma-4`가 (대소문자와 관계없이) 포함되어 있으면, 해당 모델로 가는 함수 호출 결과는 `tool_responses`로 전송됩니다. 다른 모델에 대한 요청은 변경되지 않습니다. 이 기능은 SillyTavern 확장 기능 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix)를 옮겨 만든 것입니다.

<a id="usage"></a>

## 사용법

설정할 것은 없습니다. **Admin → Extensions**에서 확장 기능을 불러오면 다음 요청부터 적용됩니다.
