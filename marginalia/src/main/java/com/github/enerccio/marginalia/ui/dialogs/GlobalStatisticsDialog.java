package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.statistics.StatisticsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * What was done to all the books of the user, also the ones that are gone: the same grid by day and charts as the window of a
 * book, a list of the books and a chart of the books. A click on a book of the list opens it. The phone has
 * {@link com.github.enerccio.marginalia.ui.dialogs.phone.PhoneGlobalStatisticsDialog}.
 */
@Configurable
@Extendable
public class GlobalStatisticsDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    private final Consumer<String> onOpenBook;

    /**
     * @param onOpenBook opens the book with this uuid; the dialog closes first
     */
    public GlobalStatisticsDialog(Consumer<String> onOpenBook) {
        this.onOpenBook = onOpenBook;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "view", type = StatisticsView.class),
                    @APILocals(name = "closeButton", type = Button.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_GLOBAL_STATISTICS));
        setWidth("1250px");
        setMaxWidth("96vw");
        setHeight("90vh");
        setResizable(true);
        setDraggable(true);

        StatisticsView view = new StatisticsView(null, false);
        view.setOnOpenBook(uuid -> {
            close();
            onOpenBook.accept(uuid);
        });
        view.create();
        add(view);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);
    }
}
