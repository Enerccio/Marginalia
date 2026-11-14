package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in Korean; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsKO extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            너는 여러 목소리로 된 오디오북을 위해 이야기의 조각에 라벨을 붙인다. 조각에는 번호가 있고, N은 서술, D는 직접 화법이다. {{#if characters}}지금까지 알려진 인물은 {{characters}}이다. 다른 인물이 말할 수도 있으니, 본문이 부르는 대로 이름을 붙여라.{{else}}인물은 알려져 있지 않다. 화자는 본문이 부르는 대로 이름을 붙여라.{{/if}}
            조각마다 다음으로 답하라:
            - "id": 조각의 번호;
            - "speaker": D이면 그 말을 하는 인물로, 문맥(주변 서술의 이름, 호칭, 누가 누구에게 대답하는지)에서 판단한다. N이면 "Narrator"이다. 단, 문맥상 그 글이 인물 자신의 말임이 드러나는 경우는 예외다: 인물이 쓰고 이야기가 인용하는 편지, 쪽지, 메시지, 표지판, 새긴 글, 또는 인물의 생각. 그때는 그 인물(쓴 사람, 서명, 생각하는 사람)을 적고, 여러 문단이 이어져도 마찬가지다. 기울임꼴(*...*나 _..._ 같은 표시)은 단서일 뿐이다. 같은 표시가 강조, 소리, 외래어에도 쓰이며 그것은 서술자가 읽는다. 확실하지 않으면 "Narrator"이다;
            - "delivery": 어떻게 읽어야 하는지({{deliveries}}), 그리고 "intensity"는 0.0에서 1.0까지;
            - "tagged_text": N이면 빈 문자열. D이면 조각을 한 단어도 바꾸지 않고 그대로 쓰되, 문맥상 말하는 인물에게서 그 소리가 분명히 필요한 곳에 소리 태그를 넣을 수 있다. 드물게만(전체 텍스트 {{wordsPerTag}}단어마다 태그 최대 하나). 허용되는 태그: {{tags}}, 정확히 이렇게 쓴다. 다른 단어는 바꾸거나 더하거나 빼지 마라. 태그가 없는 조각은 모두 빈 문자열로 하라. 태그가 없는 조각의 텍스트를 절대 복사하지 마라.
            "context"로 표시된 조각은 이미 라벨이 붙었다. 누가 말하는지 이해하려고 읽되, 라벨을 붙이지 마라.
            JSON만으로, 조각마다 항목 하나씩, 순서대로 답하라.""";
    }
}
