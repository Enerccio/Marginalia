package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tells the core which passages of a part a model reads as English: the translated ones, in their source ({@link Reverse}). Registered in the
 * {@link PartTextRegistry}, so the prompt of a generation, the summaries, the lorebook scan, annotate, redraft and the other plugins all read
 * the source and never the conlang.
 * <p>
 * The registry calls {@link #prepare} with every part it is about to read and then {@link #substitutions} for each, on one thread, so the
 * passages of the whole branch are read with one query (a story is hundreds of parts) and kept for that thread until the next call.
 */
public final class ConlangTransformer implements PartTextRegistry.Transformer {

    /**
     * Where the translated passages of parts come from.
     */
    public interface Passages {

        /**
         * The translated passages of the parts, by the id of the part; a part with none is not in the map.
         */
        Map<Long, List<Located>> of(Collection<ChatMessage> parts) throws Exception;
    }

    private final Passages passages;
    private final ThreadLocal<Map<Long, List<Located>>> prepared = new ThreadLocal<>();

    public ConlangTransformer(Passages passages) {
        this.passages = passages;
    }

    @Override
    public void prepare(Collection<ChatMessage> parts) throws Exception {
        prepared.set(new HashMap<>(passages.of(parts)));
    }

    @Override
    public List<Substitution> substitutions(ChatMessage part) throws Exception {
        Map<Long, List<Located>> known = prepared.get();
        List<Located> located = known != null && part.getId() != null ? known.get(part.getId()) : null;
        if (located == null || located.isEmpty()) {
            return List.of();
        }
        return Reverse.toModel(part.getResponse(), located).substitutions();
    }
}
