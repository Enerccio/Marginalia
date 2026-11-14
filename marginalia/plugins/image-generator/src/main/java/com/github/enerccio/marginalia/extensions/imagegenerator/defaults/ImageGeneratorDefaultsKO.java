package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in Korean; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsKO extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "당신은 이미지 생성 모델용 프롬프트를 씁니다. 아래 장면에서 가장 시각적인 순간의 그림을 묘사하는 프롬프트를 하나 쓰세요. 누가 있는지(외모, 옷차림, 자세), 장소, 빛과 분위기를 묘사합니다. 쉼표로 구분한 짧은 구를 쓰고 문장은 쓰지 마세요. 인물의 이름은 쓰지 말고 외모로 묘사하며, 그림 안에 글자를 넣지 마세요. 장면의 언어와 관계없이 프롬프트는 영어로 쓰세요. 프롬프트만 답하세요.";
    }

    @Override
    public String sentencesInstruction() {
        return "당신은 자연어를 이해하는 이미지 생성 모델용 프롬프트를 씁니다. 아래 장면에서 가장 시각적인 순간의 그림을 위한 프롬프트를 하나 쓰세요. 세 문장에서 여섯 문장의 평이한 묘사문으로 된 한 단락입니다. 누가 있는지(나이, 외모, 옷차림, 자세, 표정)와 무엇을 하는지, 어디서 일어나는지, 그림의 구도(클로즈업, 원경, 어느 방향에서), 빛과 분위기를 쓰세요. 인물은 이름이 아니라 외모로 묘사하고, 보이는 것만 묘사하며, 그림 안에 글자를 넣지 말고, 목록, 태그, 제목을 쓰지 마세요. 장면의 언어와 관계없이 프롬프트는 영어로 쓰세요. 프롬프트만 답하세요.";
    }

    @Override
    public String sceneSetting() {
        return "장면 설정";
    }

    @Override
    public String pointOfView() {
        return "시점";
    }

    @Override
    public String charactersPresent() {
        return "등장인물";
    }

    @Override
    public String text() {
        return "본문";
    }

    @Override
    public String detailsHeader() {
        return "그림이 따라야 할 세부 사항";
    }

    @Override
    public String styleHeader() {
        return "그림의 스타일은 프롬프트에 자동으로 추가됩니다. 다른 매체나 스타일을 묘사하지 마세요";
    }
}
