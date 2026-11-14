package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in Chinese; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsZH extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "你为图像生成模型撰写提示词。根据下面的场景，为其中最具画面感的时刻写一个提示词：在场的人（外貌、衣着、姿势）、地点、光线和氛围。使用逗号分隔的简短短语，不要写句子，不要使用人名（改为描述外貌），画面中不要出现文字。无论场景使用何种语言，提示词都要用英语写。只回答提示词。";
    }

    @Override
    public String sentencesInstruction() {
        return "你为能理解自然语言的图像生成模型撰写提示词。根据下面的场景，为其中最具画面感的时刻写一个提示词：一个由三到六个平实描述句组成的段落。说明在场的人（年龄、外貌、衣着、姿势、表情）以及他们在做什么、发生在哪里、画面如何取景（特写、远景、从哪个方向）、光线和氛围。用外貌而不是名字描述人物；只描述看得见的东西；画面中不要出现文字或字母；不要使用列表、标签或标题。无论场景使用何种语言，提示词都要用英语写。只回答提示词。";
    }

    @Override
    public String sceneSetting() {
        return "场景设定";
    }

    @Override
    public String pointOfView() {
        return "视角";
    }

    @Override
    public String charactersPresent() {
        return "在场角色";
    }

    @Override
    public String text() {
        return "正文";
    }

    @Override
    public String detailsHeader() {
        return "画面必须遵循的细节";
    }

    @Override
    public String styleHeader() {
        return "图片的风格会自动添加到你的提示词中，不要描述其他媒介或风格";
    }
}
