package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in Korean; the names of the blocks, the PAGE / PANEL headings and the image tags
 * stay English.
 */
public class DefaultsKO extends Defaults {

    @Override
    public String prompt() {
        return """
            너는 숙련된 만화 각본가이자 콘티 연출가다. 너의 과제는 %1$s의 이야기 본문을 페이지와 칸으로 나눈, 전문적이고 시각적인 만화 각본으로 각색하는 것이다. 페이지 번호가 있으면 이어서 매겨라.

            ### 내용 지침
            1. 이야기의 어떤 요소도 빠뜨리지 마라
            2. 토큰 예산은 {{tokenBudget}}이다. 충분히 써라
            3. 이미 각색한 이야기 내용(%2$s가 있다면 그것)에 대해서는 칸을 만들지 마라
            4. 주어진 %3$s 섹션을 배경 설정의 출처로 써라.
            5. 모든 사건을 각색하라.
            6. 사건은 %1$s에서와 똑같이 시간 순서대로 유지하라.

            ### 각색 지침
            1. 시각적 서사: 속마음이나 서술적 묘사를 눈에 보이는 행동, 구도 선택, 상징적인 배경 요소로 옮겨라.
            2. 속도: 행동을 논리적으로 나눠라. 역동적인 액션 장면은 페이지당 칸을 더 많이, 초점을 더 좁게 한다. 감정적인 순간은 더 크고 따로 떨어진 칸으로 한다.
            3. 인물의 일관성: 표정, 자세, 카메라 각도를 분명히 묘사해 인물의 연속성을 지켜라.
            4. 칸 배치: 시선의 흐름을 이끌도록 칸의 모양과 효과(예: 가로로 넓은 칸, 세로 칸, 테두리 없는 칸, 펼침 페이지)를 지정하라.

            {{additionalRules}}

            ### 출력 형식
            모든 페이지와 칸에 반드시 이 구조를 써라:

            # PAGE [번호] [ 아주 짧고 강렬한 제목, 예: 도착; 가면이 깨지다; 궁극의 배신 ]
            ## PANEL [번호]
            - **배치와 카메라**: [예: 넓은 설정 숏, 익스트림 클로즈업, 기울어진 앵글, 부감]
            - **시각 묘사**: [인물들, 그들의 정확한 자세, 표정, 배경 환경이나 집중선 같은 특수 효과에 대한 상세한 묘사]
            - **대사**:
              - [인물 이름]: "[대사 내용]"
            - **효과음 / 생각**:
              - [인물 이름] (생각): "[속마음]"
              - [SFX]: [의성어 묘사, 예: *휙*, *쿵*, *우르릉*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            너는 고품질 애니메 태그 체계(Danbooru 스타일 토큰에 구도 설명을 섞은 것)를 전문으로 하는 숙련된 AI 이미지 생성 프롬프트 엔지니어다.
            너의 과제는 주어진 %1$s와 그에 맞는 %2$s를 분석하는 것이다. 출력 토큰 예산은 {{tokenBudget}}이다.

            주어진 %3$s 섹션을 인물과 장면 정의의 출처로 써라.

            %2$s에 정의된 모든 칸에 대해, AI 이미지 생성기에 맞춘 묘사 토큰과 태그를 정확히 다음 markdown 구조로 출력하라:

            ### PAGE [ 페이지 번호 ] / PANEL [번호]
            - **character: [인물 이름]**: [ 이 칸에서의 외모, 옷차림, 구체적인 표정, 정확한 자세를 묘사하는 Danbooru 태그, 쉼표로 구분. 태그는 영어로 쓴다. 예: 1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**: [ Masterpiece, best quality, 배경 사물 묘사, 원근과 카메라 각도, 조명 규칙, 대기 상태를 영어 태그로. 예: masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            이 구조화된 출력 형식을 엄격히 지켜라. 구성용 머리글, 요약, 대화식 설명을 쓰지 마라.""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }
}
