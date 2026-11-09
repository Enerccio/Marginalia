package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechTransformer;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Reads the translated passages of parts: the rows of the passage kind of the plugin in the table of the annotations, with their data. A passage
 * that lost its text (no range) or has no data of the plugin is left out.
 */
@Configurable
public class PassageAccess implements SpeechTransformer.Passages {

    @Autowired
    private PassageService passageService;

    private final String kind;

    /**
     * @param kind the id of the passage kind the plugin registered (each plugin has its own)
     */
    public PassageAccess(String kind) {
        this.kind = kind;
    }

    @Override
    public Map<Long, List<Located>> of(Collection<ChatMessage> parts) throws Exception {
        List<Long> ids = parts.stream().map(ChatMessage::getId).filter(id -> id != null).toList();
        Map<Long, List<Located>> byPart = new HashMap<>();
        for (AnchoredPassage passage : passageService.listForParts(ids, kind)) {
            Located located = locate(passage);
            if (located != null) {
                byPart.computeIfAbsent(passage.getPartId(), id -> new ArrayList<>()).add(located);
            }
        }
        return byPart;
    }

    /**
     * The passages of one part.
     */
    public List<AnchoredPassage> passagesOf(ChatMessage part) throws Exception {
        return passageService.listForParts(List.of(part.getId()), kind);
    }

    /**
     * The passage with its range and data, null when it has none of them.
     */
    public static Located locate(AnchoredPassage passage) {
        Span span = Span.read(passage.getAttributes());
        if (span == null || !passage.hasRange() || passage.isOrphaned()) {
            return null;
        }
        return new Located(passage.getStart(), passage.getEnd(), span);
    }
}
