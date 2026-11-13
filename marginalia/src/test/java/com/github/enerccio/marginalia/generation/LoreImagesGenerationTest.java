package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The images in the content of lorebook entries in a real generation: the model reads the text without them, and gets the images
 * themselves only when its AI is set to take them, the lore block's in a message of their own after the system prompt (or in the
 * system message), the others with the user's prompt.
 */
class LoreImagesGenerationTest extends GenerationTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AIService aiService;

    private Resource image;

    @BeforeEach
    void anImage() throws Exception {
        BufferedImage picture = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", png);
        image = resourceService.uploadImage("dragon.png", png.toByteArray());
    }

    private Manuscript book(InsertionMode insertion) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Dragon");
        entry.setFiltering("dragon");
        entry.setInsertionMode(insertion);
        entry.setPayload("Dragons breathe fire.\n\n" + LoreImages.markup("red scales", image.getUuid()) + "\n\nThey hoard gold.");
        lorebookEntryService.save(entry);
        Manuscript manuscript = newManuscript();
        manuscript.setLorebook(lorebook);
        return manuscriptService.save(manuscript);
    }

    private void aiTakes(boolean images, boolean inSystem) throws Exception {
        ai.setSendLoreImages(images);
        ai.setLoreImagesInSystem(inSystem);
        aiService.save(ai);
    }

    private JsonArray sent(Manuscript manuscript) throws Exception {
        GenerationRun run = generate(manuscript, "a dragon");
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getErrors()).isEmpty();
        return llm.lastCompletionRequest().getBody().getAsJsonArray("messages");
    }

    private static List<String> roles(JsonArray messages) {
        List<String> roles = new ArrayList<>();
        messages.forEach(message -> roles.add(message.getAsJsonObject().get("role").getAsString()));
        return roles;
    }

    private static List<String> imageUrls(JsonObject message) {
        List<String> urls = new ArrayList<>();
        JsonElement content = message.get("content");
        if (content != null && content.isJsonArray()) {
            for (JsonElement part : content.getAsJsonArray()) {
                if ("image_url".equals(part.getAsJsonObject().get("type").getAsString())) {
                    urls.add(part.getAsJsonObject().getAsJsonObject("image_url").get("url").getAsString());
                }
            }
        }
        return urls;
    }

    private static String text(JsonObject message) {
        JsonElement content = message.get("content");
        if (content.isJsonPrimitive()) {
            return content.getAsString();
        }
        StringBuilder text = new StringBuilder();
        for (JsonElement part : content.getAsJsonArray()) {
            if ("text".equals(part.getAsJsonObject().get("type").getAsString())) {
                text.append(part.getAsJsonObject().get("text").getAsString());
            }
        }
        return text.toString();
    }

    @Test
    void theModelReadsTheTextWithoutTheImagesAndGetsNoneByDefault() throws Exception {
        JsonArray messages = sent(book(InsertionMode.IN_LORE_BLOCK));

        assertThat(roles(messages)).containsExactly("system", "user");
        assertThat(text(messages.get(0).getAsJsonObject())).isEqualTo("Dragons breathe fire.\n\nThey hoard gold.");
        messages.forEach(message -> assertThat(imageUrls(message.getAsJsonObject())).isEmpty());
    }

    @Test
    void theImagesOfTheLoreBlockGoIntoAnAnsweredMessageAfterTheSystemPrompt() throws Exception {
        aiTakes(true, false);

        JsonArray messages = sent(book(InsertionMode.IN_LORE_BLOCK));

        assertThat(roles(messages)).containsExactly("system", "user", "assistant", "user");
        assertThat(imageUrls(messages.get(0).getAsJsonObject())).isEmpty();
        JsonObject images = messages.get(1).getAsJsonObject();
        assertThat(imageUrls(images)).singleElement().asString().startsWith("data:image/png;base64,");
        assertThat(text(images)).contains("Dragon (red scales)");
        assertThat(text(messages.get(3).getAsJsonObject())).isEqualTo("a dragon");
        assertThat(imageUrls(messages.get(3).getAsJsonObject())).isEmpty();
    }

    @Test
    void theImagesOfTheLoreBlockCanGoIntoTheSystemMessage() throws Exception {
        aiTakes(true, true);

        JsonArray messages = sent(book(InsertionMode.IN_LORE_BLOCK));

        assertThat(roles(messages)).containsExactly("system", "user");
        JsonObject system = messages.get(0).getAsJsonObject();
        assertThat(imageUrls(system)).hasSize(1);
        assertThat(text(system)).startsWith("Dragons breathe fire.").contains("Dragon (red scales)");
    }

    @Test
    void theImagesOfAnEntryBeforeTheUserPromptGoWithThePrompt() throws Exception {
        aiTakes(true, false);

        JsonArray messages = sent(book(InsertionMode.BEFORE_USER_PROMPT));

        assertThat(roles(messages)).containsExactly("system", "user");
        JsonObject prompt = messages.get(1).getAsJsonObject();
        assertThat(imageUrls(prompt)).hasSize(1);
        assertThat(text(prompt)).startsWith("Dragons breathe fire.\n\nThey hoard gold.").contains("a dragon");
    }

    @Test
    void anImageThatIsGoneIsLeftOut() throws Exception {
        aiTakes(true, false);
        Manuscript manuscript = book(InsertionMode.IN_LORE_BLOCK);
        resourceService.softDelete(List.of(image.getUuid()));

        JsonArray messages = sent(manuscript);

        assertThat(roles(messages)).containsExactly("system", "user");
    }
}
