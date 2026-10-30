package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SillyTavernImportService;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Node;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class SillyTavernImportServiceImpl implements SillyTavernImportService {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Override
    @NoTx
    public ImportResult<Manuscript> importTree(Manuscript manuscript, SillyTavernChats.Tree tree, BooleanSupplier stopped,
                                               Runnable onStored) throws Exception {
        List<Node> order = tree.inStoringOrder();
        Map<Node, ChatMessage> stored = new IdentityHashMap<>();
        int imported = 0;
        boolean activeSet = false;
        ChatMessage last = null;
        for (Node node : order) {
            if (stopped.getAsBoolean()) {
                break;
            }
            ChatMessage part = new ChatMessage();
            part.setResponse(node.alternative().text());
            part.setInstructions(node.instructions());
            part.setResponseReasoning(node.alternative().reasoning());
            part.setModelUsed(node.alternative().model());
            ChatMessage saved = chatMessageService.addPart(manuscript, node.parent() == null ? null : stored.get(node.parent()), part,
                    node.alternative().model() == null ? RevisionOrigin.human(RevisionDetail.SILLYTAVERN_IMPORT, false)
                            : RevisionOrigin.model(RevisionSource.GENERATE, node.alternative().model(), false, node.alternative().reasoning()));
            stored.put(node, saved);
            last = saved;
            imported++;
            if (node == tree.activeLeaf()) {
                setActiveLeaf(manuscript, saved);
                activeSet = true;
            }
            onStored.run();
        }
        if (!activeSet && last != null) {
            // stopped before the active leaf: the book ends where the import did, so that it can be read
            setActiveLeaf(manuscript, last);
        }
        return new ImportResult<>(manuscriptService.find(manuscript), imported, order.size());
    }

    private void setActiveLeaf(Manuscript manuscript, ChatMessage leaf) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        book.setActiveLeaf(leaf);
        manuscriptService.save(book);
    }
}
