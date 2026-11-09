package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.components.ModelText;
import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Where the code that gives the story to a <b>model</b> asks for the text of a part, and where an extension that keeps the story
 * in another form than the model should read it says how to turn it back.
 * <p>
 * The text of a part is {@code ChatMessage.getResponse()}; every place that sends it to a model (the prompt of a generation, the
 * summaries, the lorebook scan, annotate, redraft, the read commands of an agent, the plugins that talk about the book) takes
 * it from here instead, so an extension has one place to change what the model reads. The places that deal with the text as it
 * is stored (the editor, the history, the hash of a summary, the search and replace, the export) keep using
 * {@code getResponse()}.
 * <p>
 * Extensions register a {@link Transformer} when they are loaded and call {@link #unregisterAll} in {@code onExtensionUnload}.
 * With none registered every answer is the stored text, unchanged. A transformer that fails does not break the request: the
 * failure is logged and the part is read as it is stored.
 */
public class PartTextRegistry {

    private static final Logger log = LoggerFactory.getLogger(PartTextRegistry.class);

    /**
     * Says which passages of a part a model should read in other words.
     */
    public interface Transformer {

        /**
         * Called once with every part that is about to be read, before {@link #substitutions} is asked for each of them, so the
         * data of a whole branch can be loaded in one go (a story of hundreds of parts must not cost a query per part).
         */
        default void prepare(Collection<ChatMessage> parts) throws Exception {
        }

        /**
         * The passages to replace, as ranges of the stored text of the part, {@code part.getResponse()}.
         *
         * @return the substitutions, empty for none; ranges outside the text or overlapping an earlier one are ignored
         */
        List<Substitution> substitutions(ChatMessage part) throws Exception;
    }

    // registration order is the order of priority: the first one wins an overlap
    private final Map<Transformer, String> transformers = new LinkedHashMap<>();

    /**
     * Adds a transformer.
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    public synchronized void register(String owner, Transformer transformer) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(transformer, "transformer");
        transformers.put(transformer, owner);
    }

    public synchronized void unregister(Transformer transformer) {
        transformers.remove(transformer);
    }

    /**
     * Removes the transformers an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        transformers.values().removeIf(registered -> Objects.equals(registered, owner));
    }

    private synchronized List<Transformer> registered() {
        return new ArrayList<>(transformers.keySet());
    }

    /**
     * The part as a model reads it, with the map to the stored text.
     */
    public ModelText modelText(ChatMessage part) {
        return modelTexts(java.util.Collections.singletonList(part)).getFirst();
    }

    /**
     * The parts as a model reads them, in the order given (null parts and parts without text are read as empty). The
     * transformers prepare for all the parts at once.
     */
    public List<ModelText> modelTexts(Collection<ChatMessage> parts) {
        List<ChatMessage> list = new ArrayList<>(parts);
        List<Transformer> active = registered();
        List<ModelText> result = new ArrayList<>(list.size());
        if (active.isEmpty()) {
            for (ChatMessage part : list) {
                result.add(ModelText.unchanged(part == null ? null : part.getResponse()));
            }
            return result;
        }

        List<ChatMessage> present = list.stream().filter(Objects::nonNull).toList();
        List<Transformer> prepared = new ArrayList<>();
        for (Transformer transformer : active) {
            try {
                transformer.prepare(present);
                prepared.add(transformer);
            } catch (Exception | LinkageError e) {
                log.warn("An extension could not prepare the text of the parts for the model, they are read as stored: {}", e.getMessage(), e);
            }
        }
        for (ChatMessage part : list) {
            if (part == null || part.getResponse() == null || part.getResponse().isEmpty()) {
                result.add(ModelText.unchanged(part == null ? null : part.getResponse()));
                continue;
            }
            List<Substitution> substitutions = new ArrayList<>();
            for (Transformer transformer : prepared) {
                try {
                    List<Substitution> asked = transformer.substitutions(part);
                    if (asked != null) {
                        substitutions.addAll(asked);
                    }
                } catch (Exception | LinkageError e) {
                    log.warn("An extension could not read a part for the model, it is read as stored: {}", e.getMessage(), e);
                }
            }
            result.add(ModelText.of(part.getResponse(), substitutions));
        }
        return result;
    }

    /**
     * The text of the part a model reads, {@code getResponse()} when nothing is registered.
     */
    public String forModel(ChatMessage part) {
        return modelText(part).text();
    }

    /**
     * The text of the parts a model reads, in the order given.
     */
    public List<String> forModel(Collection<ChatMessage> parts) {
        return modelTexts(parts).stream().map(ModelText::text).toList();
    }

    /**
     * Whether any extension has registered a transformer; the places that would pay for the map can skip it when none has.
     */
    public synchronized boolean isActive() {
        return !transformers.isEmpty();
    }
}
