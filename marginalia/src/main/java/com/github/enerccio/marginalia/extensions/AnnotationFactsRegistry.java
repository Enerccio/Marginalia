package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Where an extension that <b>knows facts about the story</b> offers them to the annotation: the model that reviews a part
 * ({@link com.github.enerccio.marginalia.domain.service.AnnotateService}) gets them as the variable {@code facts} of the annotate
 * prompt and checks the part against them, so a contradiction is found as a continuity error. The Knowledge Tracker offers what each
 * character knew at the part; a timeline or an inventory plugin could offer theirs.
 * <p>
 * A provider is asked for every part that is annotated, with the part and the branch that leads to it, and answers with
 * {@link Section sections} of plain text, or nothing. It answers for the story <b>as it stands at the part</b> (what is established
 * before it, and what the part itself establishes), not for the end of the book: the model reviews an early part, and a fact that is
 * only true later would make a correct passage look wrong.
 * <p>
 * Extensions register a {@link Provider} when they are loaded and call {@link #unregisterAll} in {@code onExtensionUnload}. With none
 * registered the variable is empty and the prompt is the one it was. A provider that fails does not break the annotation: the
 * failure is logged and its facts are left out.
 */
public class AnnotationFactsRegistry {

    private static final Logger log = LoggerFactory.getLogger(AnnotationFactsRegistry.class);

    /**
     * One block of facts: a heading that says what they are and the text.
     *
     * @param title what the facts are (<i>What the characters know</i>), shown above the text; may be blank
     * @param text  the facts, one per line
     */
    public record Section(String title, String text) {
    }

    /**
     * Says what is established about the story at a part.
     */
    public interface Provider {

        /**
         * @param book   the book the part belongs to
         * @param part   the part that is reviewed
         * @param branch the parts from the start of the story to {@code part}, in order; {@code part} is the last
         * @return the facts, empty or {@code null} for none
         */
        List<Section> facts(Manuscript book, ChatMessage part, List<ChatMessage> branch) throws Exception;
    }

    // registration order is the order of the sections
    private final Map<Provider, String> providers = new LinkedHashMap<>();

    /**
     * Adds a provider.
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    public synchronized void register(String owner, Provider provider) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(provider, "provider");
        providers.put(provider, owner);
    }

    public synchronized void unregister(Provider provider) {
        providers.remove(provider);
    }

    /**
     * Removes the providers an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        providers.values().removeIf(registered -> Objects.equals(registered, owner));
    }

    /**
     * Whether any provider is registered (a caller that would load the branch only for them can skip it).
     */
    public synchronized boolean isActive() {
        return !providers.isEmpty();
    }

    private synchronized List<Provider> registered() {
        return new ArrayList<>(providers.keySet());
    }

    /**
     * The facts of all providers about a part as the text of the prompt: every section under its title, separated by an empty
     * line. Empty when there are none.
     */
    public String facts(Manuscript book, ChatMessage part, List<ChatMessage> branch) {
        List<String> blocks = new ArrayList<>();
        for (Provider provider : registered()) {
            try {
                List<Section> sections = provider.facts(book, part, branch);
                if (sections == null) {
                    continue;
                }
                for (Section section : sections) {
                    if (section == null || StringUtils.isBlank(section.text())) {
                        continue;
                    }
                    String title = StringUtils.trimToEmpty(section.title());
                    blocks.add((title.isEmpty() ? "" : title + "\n") + section.text().strip());
                }
            } catch (Exception | LinkageError e) {
                log.warn("An extension could not give its facts to the annotation, they are left out: {}", e.getMessage(), e);
            }
        }
        return String.join("\n\n", blocks);
    }
}
