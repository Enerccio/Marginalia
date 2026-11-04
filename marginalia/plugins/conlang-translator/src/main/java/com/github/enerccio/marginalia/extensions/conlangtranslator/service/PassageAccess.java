package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangTransformer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Reads the translated passages of parts: the rows of the kind {@link Span#KIND} of the table of the annotations, with their data. A passage
 * that lost its text (no range) or has no data of the plugin is left out.
 */
@Configurable
public class PassageAccess implements ConlangTransformer.Passages {

    @Autowired
    private PassageService passageService;

    @Override
    public Map<Long, List<Located>> of(Collection<ChatMessage> parts) throws Exception {
        List<Long> ids = parts.stream().map(ChatMessage::getId).filter(id -> id != null).toList();
        Map<Long, List<Located>> byPart = new HashMap<>();
        for (AnchoredPassage passage : passageService.listForParts(ids, Span.KIND)) {
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
        return passageService.listForParts(List.of(part.getId()), Span.KIND);
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
