package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
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

/**
 * {@link com.github.enerccio.marginalia.ui.dialogs.StatisticsDialog} for phones: fewer columns and smaller charts.
 */
@Configurable
@Extendable
public class PhoneStatisticsDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    private final Manuscript manuscript;

    public PhoneStatisticsDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "view", type = StatisticsView.class),
                    @APILocals(name = "closeButton", type = Button.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_STATISTICS) + ": " + manuscript.getName());
        setWidth("95vw");
        setHeight("90vh");

        StatisticsView view = new StatisticsView(manuscript.getUuid(), true);
        view.create();
        add(view);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);
    }
}
