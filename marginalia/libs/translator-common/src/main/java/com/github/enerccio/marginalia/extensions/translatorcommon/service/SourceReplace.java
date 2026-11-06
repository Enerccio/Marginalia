package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Search and replace in the English sources of the translated speeches of the active branch of a book (the passages of the plugin's kind). The
 * translated text is the text of the story and is searched and replaced by the search of the book's own window; this is for what the models read
 * in its place (a name that was spelled two ways in the English, a title the author changed).
 * <p>
 * Two steps, like the book's replace: {@link #plan} finds the matches and works out the new sources without changing anything, {@link #apply}
 * writes them. A speech whose source changed since the plan was made is left as it is and counted.
 */
@Configurable
public class SourceReplace {

    /**
     * A translated speech with matches in its source.
     *
     * @param passageUuid the passage
     * @param part        the position of the part on the branch among the parts that have text, counted from 1
     * @param number      the position of the speech in the part, counted from 1
     * @param source      the source the search saw
     * @param newSource   the source with the matches replaced
     * @param hits        the matches in the order they are in the source
     */
    public record Item(String passageUuid, int part, int number, String source, String newSource, List<TextHit> hits) {
    }

    /**
     * What a search found.
     */
    public record Plan(List<Item> items) {

        public int hitCount() {
            return items.stream().mapToInt(item -> item.hits().size()).sum();
        }
    }

    /**
     * What a replace did.
     *
     * @param hits    matches replaced
     * @param changed speeches changed
     * @param skipped speeches that were left as they are because their source changed since the search
     */
    public record Result(int hits, int changed, int skipped) {
    }

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PassageService passageService;

    private final String kindId;
    private final TranslationWriter writer;

    /**
     * @param kindId the id of the passage kind of the plugin
     */
    public SourceReplace(String kindId) {
        this.kindId = kindId;
        this.writer = new TranslationWriter(kindId);
    }

    /**
     * Looks for the search in the sources of the translated speeches of the active branch.
     *
     * @param progress reports the parts done and tells whether the user asked to stop (a stop ends the search with a {@code CancellationException})
     */
    public Plan plan(Manuscript book, TextSearch search, TaskProgress progress) throws Exception {
        Manuscript stored = manuscriptService.find(book);
        List<ChatMessage> branch = stored == null || stored.getActiveLeaf() == null ? List.of()
                : chatMessageService.getBranchFromLeaf(stored.getActiveLeaf()).stream()
                .filter(part -> part.getResponse() != null && !part.getResponse().isBlank()).toList();
        Map<Long, List<AnchoredPassage>> byPart = branch.isEmpty() ? Map.of()
                : passageService.listForParts(branch.stream().map(ChatMessage::getId).toList(), kindId).stream()
                .collect(Collectors.groupingBy(AnchoredPassage::getPartId));
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            progress.checkCancelled();
            progress.update(i, branch.size());
            int number = 0;
            for (AnchoredPassage passage : byPart.getOrDefault(branch.get(i).getId(), List.of())) {
                Span span = PassageAccess.locate(passage) == null ? null : Span.read(passage.getAttributes());
                if (span == null) {
                    continue;
                }
                number++;
                TextSearch.Result found = search.apply("source", span.getSource());
                if (found.found() && !found.newText().isEmpty()) {
                    items.add(new Item(passage.getUuid(), i + 1, number, span.getSource(), found.newText(), found.hits()));
                }
            }
        }
        progress.update(branch.size(), branch.size());
        return new Plan(items);
    }

    /**
     * Writes what the plan found.
     */
    public Result apply(Plan plan, TaskProgress progress) throws Exception {
        int hits = 0;
        int changed = 0;
        int skipped = 0;
        int done = 0;
        for (Item item : plan.items()) {
            progress.checkCancelled();
            progress.update(done++, plan.items().size());
            if (writer.replaceSource(item.passageUuid(), item.source(), item.newSource())) {
                hits += item.hits().size();
                changed++;
            } else {
                skipped++;
            }
        }
        progress.update(plan.items().size(), plan.items().size());
        return new Result(hits, changed, skipped);
    }
}
