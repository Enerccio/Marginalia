package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.ExportTextRegistry;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;

import java.util.*;
import java.util.function.Function;

/**
 * Tells the core's {@link ExportTextRegistry} what the export of a plugin's translated speech changes in the text of the parts: for the choice the
 * author made in the export dialog ({@link ExportMode}), the speech replaced by its source and/or a footnote on each speech ({@link ExportText}).
 * Register it with {@code registry.register(owner, new SpeechExportContributor(passages, session -> mode))} in {@code onExtensionLoad}.
 * <p>
 * The passages of the whole export are read with one query in {@link #prepare} and kept for the thread until the next export (the registry
 * calls {@code prepare} and then {@code edits} for each part on one thread).
 */
public final class SpeechExportContributor implements ExportTextRegistry.Contributor {

    private final PassageAccess passages;
    private final Function<ExportTextRegistry.Session, ExportMode> mode;
    private final ThreadLocal<Map<Long, List<Located>>> prepared = new ThreadLocal<>();

    /**
     * @param passages the access to the passages of the plugin's kind
     * @param mode     the choice of the author for this export; null or {@link ExportMode#TRANSLATED} changes nothing (a dialog without the
     *                 plugin's field has none)
     */
    public SpeechExportContributor(PassageAccess passages, Function<ExportTextRegistry.Session, ExportMode> mode) {
        this.passages = passages;
        this.mode = mode;
    }

    @Override
    public void prepare(Collection<ChatMessage> parts, ExportTextRegistry.Session session) throws Exception {
        ExportMode chosen = mode.apply(session);
        prepared.set(chosen == null || chosen == ExportMode.TRANSLATED ? Map.of() : new HashMap<>(passages.of(parts)));
    }

    @Override
    public List<ExportTextRegistry.Edit> edits(ChatMessage part, ExportTextRegistry.Session session) {
        Map<Long, List<Located>> known = prepared.get();
        ExportMode chosen = mode.apply(session);
        List<Located> located = known != null && part.getId() != null ? known.get(part.getId()) : null;
        if (chosen == null || located == null || located.isEmpty()) {
            return List.of();
        }
        return ExportText.edits(chosen, part.getResponse(), located);
    }
}
