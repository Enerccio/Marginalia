package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.ui.widgets.LazyContextMenu;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.internal.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The menu that is filled when it opens: nothing is added when it is created, the callback runs on every open (Flow
 * calls {@code onBeforeOpenMenu} in the round-trip before it shows the menu) and the menu still opens.
 */
class LazyContextMenuTest {

    /**
     * Gives the test access to the hook Flow calls before the menu opens.
     */
    private static final class OpenableMenu extends LazyContextMenu {
        OpenableMenu(Button target, Runnable beforeOpen) {
            super(target, beforeOpen::run);
        }

        boolean open() {
            return onBeforeOpenMenu(JacksonUtils.createObjectNode());
        }
    }

    @Test
    void itemsAreAddedOnlyWhenTheMenuOpens() {
        AtomicInteger opens = new AtomicInteger();
        OpenableMenu[] holder = new OpenableMenu[1];
        holder[0] = new OpenableMenu(new Button(), () -> {
            opens.incrementAndGet();
            if (holder[0].getItems().isEmpty()) {
                holder[0].addItem("Edit");
                holder[0].addItem("Delete");
            }
        });

        assertThat(holder[0].getItems()).isEmpty();
        assertThat(opens).hasValue(0);

        assertThat(holder[0].open()).isTrue();
        assertThat(opens).hasValue(1);
        assertThat(holder[0].getItems()).hasSize(2);

        assertThat(holder[0].open()).isTrue();
        assertThat(opens).hasValue(2);
        assertThat(holder[0].getItems()).hasSize(2);
    }
}
