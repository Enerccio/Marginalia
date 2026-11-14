package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/**
 * The built-in prompt texts of the Image Generator, in English; a subclass translates them and {@code ImageGeneratorLocalization.T.defaults()}
 * is the one of the prompt language of the user. Every instruction asks for the image prompt in English: image models read English best.
 */
public class ImageGeneratorDefaults {

    /** The built-in instruction of the prompt form "Tags": short comma separated phrases, for models with a CLIP text encoder. */
    public String tagsInstruction() {
        return "You write prompts for an image generation model. From the scene below, write one prompt that describes the picture of its most visual moment: who is there (looks, clothes, pose), where, the light and the mood. Use short comma separated phrases, no sentences, no names of people (describe them), no text in the picture. Write the prompt in English, whatever the language of the scene. Answer with the prompt only.";
    }

    /** The built-in instruction of the prompt form "Descriptive sentences": a paragraph, for models with a language model as the text encoder. */
    public String sentencesInstruction() {
        return "You write prompts for an image generation model that understands natural language. From the scene below, write one prompt for the picture of its most visual moment: a single paragraph of three to six plain descriptive sentences. Say who is there (age, looks, clothes, pose, expression) and what they are doing, where it happens, how the picture is framed (close up, wide shot, from which side), the light and the mood. Describe people by their looks, never by their names; describe only what can be seen; no text or letters in the picture; no lists, tags or headings. Write the prompt in English, whatever the language of the scene. Answer with the prompt only.";
    }

    /** Label of the scene setting of the part in the message to the prompt writer. */
    public String sceneSetting() {
        return "Scene setting";
    }

    /** Label of the point of view. */
    public String pointOfView() {
        return "Point of view";
    }

    /** Label of the characters present. */
    public String charactersPresent() {
        return "Characters present";
    }

    /** Label of the text of the part. */
    public String text() {
        return "Text";
    }

    /** Line before the user's details. */
    public String detailsHeader() {
        return "Details the picture must follow";
    }

    /** Line before the style, which is put in front of the prompt by itself. */
    public String styleHeader() {
        return "The style of the picture is added to your prompt by itself, do not describe another medium or style";
    }
}
