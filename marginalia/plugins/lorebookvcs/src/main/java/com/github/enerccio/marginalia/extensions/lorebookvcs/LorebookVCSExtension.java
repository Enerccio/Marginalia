package com.github.enerccio.marginalia.extensions.lorebookvcs;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.lorebookvcs.service.LorebookVCSService;
import com.github.enerccio.marginalia.extensions.lorebookvcs.ui.LoreEntryRevisionPanel;
import com.github.enerccio.marginalia.extensions.lorebookvcs.ui.LorebookVCSGlobalPanel;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LorebookVCSExtension implements MarginaliaExtension {

    private static final String GLOBAL_PANEL_KEY = "lorebookvcs_global_panel";
    private static final String REVISION_PANEL_KEY = "lorebookvcs_revision_panel";

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    private final LorebookVCSService vcsService = new LorebookVCSService();
    private ExtensionDecorator viewCreateDecorator;
    private ExtensionDecorator detailLayoutDecorator;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            viewCreateDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof LorebookView lorebookView) {
                        if (ComponentUtil.getData(lorebookView, GLOBAL_PANEL_KEY) != null) return;

                        HorizontalLayout lorebookHeaderLayout = context.getLocalVariable("lorebookHeaderLayout", HorizontalLayout.class);

                        if (lorebookHeaderLayout != null) {
                            // the panel reads the selected lorebook on every action, so it follows the lorebook combo box
                            LorebookVCSGlobalPanel globalPanel = new LorebookVCSGlobalPanel(vcsService, lorebookView::getCurrentLorebook, () -> {
                                try {
                                    syncAndRefreshView(lorebookView, context, lorebookView.getCurrentLorebook());
                                } catch (Exception e) {
                                    UIUtils.internalServerError(loc, e);
                                }
                            });

                            int index = lorebookView.indexOf(lorebookHeaderLayout);
                            if (index >= 0) {
                                lorebookView.addComponentAtIndex(index, globalPanel);
                            } else {
                                lorebookView.addComponentAsFirst(globalPanel);
                            }

                            ComponentUtil.setData(lorebookView, GLOBAL_PANEL_KEY, globalPanel);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    viewCreateDecorator,
                    "com.github.enerccio.marginalia.ui.components.LorebookView",
                    "create"
            );

            detailLayoutDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof LorebookView lorebookView) {
                        LorebookEntry entry = context.getMethodArgument("entry", LorebookEntry.class);
                        VerticalLayout detailsLayout = context.getLocalVariable("detailsLayout", VerticalLayout.class);
                        Lorebook currentLorebook = lorebookView.getCurrentLorebook();

                        if (entry != null && detailsLayout != null && currentLorebook != null) {
                            if (ComponentUtil.getData(detailsLayout, REVISION_PANEL_KEY) != null) return;

                            LoreEntryRevisionPanel revPanel = new LoreEntryRevisionPanel(vcsService, currentLorebook, entry, () -> {
                                try {
                                    syncAndRefreshView(lorebookView, context, currentLorebook);
                                } catch (Exception e) {
                                    UIUtils.internalServerError(loc, e);
                                }
                            });

                            detailsLayout.addComponentAsFirst(revPanel);
                            ComponentUtil.setData(detailsLayout, REVISION_PANEL_KEY, revPanel);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    detailLayoutDecorator,
                    "com.github.enerccio.marginalia.ui.components.LorebookView",
                    "createEntryDetailLayout"
            );

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void syncAndRefreshView(LorebookView lorebookView, ExtendableMethodContext context, Lorebook currentLorebook) throws Exception {
        if (currentLorebook != null && currentLorebook.getId() != null) {
            Lorebook fresh = lorebookService.find(currentLorebook);
            if (fresh != null) {
                context.setReflectiveFieldValue(lorebookView, "currentLorebook", fresh, Lorebook.class);
            }
        }
        lorebookView.refresh();
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (viewCreateDecorator != null) {
            extensionService.unregisterDecorator(viewCreateDecorator);
        }
        if (detailLayoutDecorator != null) {
            extensionService.unregisterDecorator(detailLayoutDecorator);
        }
    }
}