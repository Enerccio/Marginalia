package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.service.LoreChanges;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.springframework.web.context.request.RequestAttributes;

import java.util.function.Consumer;

/**
 * Tells a component of a page of the {@link LoreChanges} made elsewhere: by another page (another window) or by a worker (an agent).
 * The changes of the page itself are left out, the page shows them already. The listener runs in the page, after the request that
 * is going on now, with the request scope of the page as it was when the listener was added (call it on the page's thread, on attach).
 */
public final class LoreChangesOnPage {

    private LoreChangesOnPage() {
    }

    /**
     * @param changed run in the page with a change of another page or a worker
     * @return removes the listener (on detach)
     */
    public static Registration listen(UI ui, Consumer<LoreChanges.Change> changed) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        return LoreChanges.addListener(change -> {
            if (change.source() != ui) {
                RunsOnPage.inPage(ui, scope, () -> changed.accept(change), true);
            }
        });
    }
}
