package com.github.enerccio.marginalia.extensions.knowledgetracker.facts;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.AnnotationFactsRegistry;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * Offers what the tracked characters know to the annotation of a part ({@link AnnotationFactsRegistry}), so the model that reviews the part
 * can flag a character who acts on what they cannot know. The facts are those of the moment of the part, see {@link KnowledgeFactsBuilder}.
 * Off when the book switched it off or tracks nobody.
 */
@Configurable
public class KnowledgeFactsProvider implements AnnotationFactsRegistry.Provider {

    @Autowired
    private PartTextRegistry partTexts;

    private final KnowledgeStateService stateService;

    public KnowledgeFactsProvider(KnowledgeStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public List<AnnotationFactsRegistry.Section> facts(Manuscript book, ChatMessage part, List<ChatMessage> branch) throws Exception {
        KnowledgeState state = stateService.load(book);
        if (!state.getSettings().isFactsForAnnotation() || state.getCharacters().isEmpty() || branch == null || branch.isEmpty()) {
            return List.of();
        }
        List<Part> parts = new ArrayList<>();
        for (ChatMessage message : branch) {
            // the text as the model reads it, like the rest of the plugin
            parts.add(new Part(message.getUuid(), message.getPovCharacter(), message.getPresentCharacters(), partTexts.forModel(message),
                    message.getId()));
        }
        String facts = KnowledgeFactsBuilder.build(state, parts,
                KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators()));
        return facts.isBlank() ? List.of() : List.of(new AnnotationFactsRegistry.Section(KnowledgeFactsBuilder.HEADER, facts));
    }
}
