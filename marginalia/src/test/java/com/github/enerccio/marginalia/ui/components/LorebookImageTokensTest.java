package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.LoreImages;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.EstimatedImageTokenizer;
import com.github.enerccio.marginalia.test.AgentTestBase;
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
 * The estimates of the lorebook editor, the lorebook view and the activation preview count an entry as the model reads it:
 * the text without the image lines, and the images by their pixels.
 */
class LorebookImageTokensTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private TokenizerService tokenizerService;

    private Lorebook lorebook;
    private Resource image;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1500, 1000, BufferedImage.TYPE_INT_RGB), "png", png);
        image = resourceService.uploadImage("map.png", png.toByteArray());
    }

    private LorebookEntry entry(String payload) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Map");
        entry.setPayload(payload);
        return entryService.save(entry);
    }

    @Test
    void anEntryCountsItsTextAndItsImages() throws Exception {
        LorebookEntry entry = entry("The realm.\n\n" + LoreImages.markup("map", image.getUuid()));

        long tokens = new LorebookTokens().of(entry);

        assertThat(tokens).isEqualTo(tokenizerService.countTokensApprox("The realm.") + EstimatedImageTokenizer.estimate(1500, 1000));
    }

    @Test
    void anImageShownTwiceCountsOnceAndAGoneOneNotAtAll() throws Exception {
        String markup = LoreImages.markup("map", image.getUuid());
        LorebookTokens tokens = new LorebookTokens();

        assertThat(tokens.estimate(markup + "\n\n" + markup)).isEqualTo(EstimatedImageTokenizer.estimate(1500, 1000));

        resourceService.softDelete(List.of(image.getUuid()));
        assertThat(new LorebookTokens().estimate(markup)).isZero();
    }

    @Test
    void theTotalWithTheSubbooksCountsTheImagesToo() throws Exception {
        entry(LoreImages.markup("map", image.getUuid()));

        assertThat(new LorebookTokens().withSubbooks(lorebookService.find(lorebook.getId())))
                .isEqualTo(EstimatedImageTokenizer.estimate(1500, 1000));
    }
}
