package com.github.enerccio.marginalia.ui.dialogs.phone;

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
 * {@link com.github.enerccio.marginalia.ui.dialogs.GlobalStatisticsDialog} for phones: fewer columns and smaller charts.
 */
@Configurable
@Extendable
public class PhoneGlobalStatisticsDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    private final Consumer<String> onOpenBook;

    /**
     * @param onOpenBook opens the book with this uuid; the dialog closes first
     */
    public PhoneGlobalStatisticsDialog(Consumer<String> onOpenBook) {
        this.onOpenBook = onOpenBook;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "view", type = StatisticsView.class),
                    @APILocals(name = "closeButton", type = Button.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_GLOBAL_STATISTICS));
        setWidth("95vw");
        setHeight("90vh");

        StatisticsView view = new StatisticsView(null, true);
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
