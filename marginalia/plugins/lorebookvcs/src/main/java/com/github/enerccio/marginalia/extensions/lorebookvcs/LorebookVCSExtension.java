package com.github.enerccio.marginalia.extensions.lorebookvcs;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.service.LorebookVCSService;
import com.github.enerccio.marginalia.extensions.lorebookvcs.ui.LoreEntryRevisionPanel;
import com.github.enerccio.marginalia.extensions.lorebookvcs.ui.LorebookVCSGlobalPanel;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookEntryEditor;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.dialogs.LorebookEditorDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Configurable
public class LorebookVCSExtension implements MarginaliaExtension {

    private static final String GLOBAL_PANEL_KEY = LorebookVCSData.KEY + ".globalPanel";
    private static final String REVISION_PANEL_KEY = LorebookVCSData.KEY + ".revisionPanel";

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    private final LorebookVCSService vcsService = new LorebookVCSService();
    private ExtensionDecorator viewCreateDecorator;
    private ExtensionDecorator detailLayoutDecorator;
    private ExtensionDecorator editorListDecorator;
    private ExtensionDecorator entryEditorDecorator;

    // the desktop LorebookView and the PhoneLorebookView
    private final Set<VerticalLayout> activeViews = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<VerticalLayout> activeDetailLayouts = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    // the panels in the full-screen lorebook editor: the history panel on its list tab, the revision bar of its entry tabs
    private final Set<Component> activeEditorPanels = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            viewCreateDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof VerticalLayout lorebookView && isLorebookView(lorebookView)) {
                        if (ComponentUtil.getData(lorebookView, GLOBAL_PANEL_KEY) != null) return;

                        // the desktop panel goes above the header of the lorebook, the phone one under the row of the
                        // check box (the phone view has no header row)
                        boolean phone = lorebookView instanceof PhoneLorebookView;
                        HorizontalLayout anchor = context.getLocalVariable(phone ? "enabledLayout" : "lorebookHeaderLayout", HorizontalLayout.class);

                        if (anchor != null) {
                            // the panel reads the selected lorebook on every action, so it follows the lorebook combo box
                            LorebookVCSGlobalPanel globalPanel = new LorebookVCSGlobalPanel(vcsService, () -> currentLorebookOf(lorebookView), () -> {
                                try {
                                    syncAndRefreshView(lorebookView, context, currentLorebookOf(lorebookView));
                                } catch (Exception e) {
                                    UIUtils.internalServerError(loc, e);
                                }
                            });

                            int index = lorebookView.indexOf(anchor);
                            if (index >= 0) {
                                lorebookView.addComponentAtIndex(phone ? index + 1 : index, globalPanel);
                            } else {
                                lorebookView.addComponentAsFirst(globalPanel);
                            }

                            ComponentUtil.setData(lorebookView, GLOBAL_PANEL_KEY, globalPanel);
                            activeViews.add(lorebookView);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    viewCreateDecorator,
                    "com.github.enerccio.marginalia.ui.components.LorebookView",
                    "create"
            );
            extensionService.registerDecorator(
                    viewCreateDecorator,
                    "com.github.enerccio.marginalia.ui.components.PhoneLorebookView",
                    "create"
            );

            detailLayoutDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof VerticalLayout lorebookView && isLorebookView(lorebookView)) {
                        LorebookEntry entry = context.getMethodArgument("entry", LorebookEntry.class);
                        VerticalLayout detailsLayout = context.getLocalVariable("detailsLayout", VerticalLayout.class);
                        Lorebook currentLorebook = currentLorebookOf(lorebookView);

                        if (entry != null && detailsLayout != null && currentLorebook != null) {
                            if (ComponentUtil.getData(detailsLayout, REVISION_PANEL_KEY) != null) return;

                            LoreEntryRevisionPanel revPanel = new LoreEntryRevisionPanel(vcsService, currentLorebook, entry, () -> {
                                try {
                                    refreshEntryRow(lorebookView, context, entry);
                                } catch (Exception e) {
                                    UIUtils.internalServerError(loc, e);
                                }
                            });

                            detailsLayout.addComponentAsFirst(revPanel);
                            ComponentUtil.setData(detailsLayout, REVISION_PANEL_KEY, revPanel);
                            activeDetailLayouts.add(detailsLayout);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    detailLayoutDecorator,
                    "com.github.enerccio.marginalia.ui.components.LorebookView",
                    "createEntryDetailLayout"
            );
            extensionService.registerDecorator(
                    detailLayoutDecorator,
                    "com.github.enerccio.marginalia.ui.components.PhoneLorebookView",
                    "createEntryDetailLayout"
            );

            // the full-screen lorebook editor: the history of the lorebook above its list of entries
            editorListDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof LorebookEditorDialog dialog) {
                        VerticalLayout list = context.getLocalVariable("list", VerticalLayout.class);
                        if (list != null) {
                            // an import changes the entries: the list and the open tabs show them again
                            LorebookVCSGlobalPanel globalPanel = new LorebookVCSGlobalPanel(vcsService, dialog::getLorebook, dialog::refresh);
                            list.addComponentAsFirst(globalPanel);
                            activeEditorPanels.add(globalPanel);
                        }
                    }
                }
            };
            extensionService.registerDecorator(editorListDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.LorebookEditorDialog", "createList");

            // and the revision bar of an entry under the header of its tab; the tab builds its content again when it
            // reloads the entry (a revision applied), so the bar comes back with the entry as it is now
            entryEditorDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof LorebookEntryEditor editor) {
                        HorizontalLayout header = context.getLocalVariable("header", HorizontalLayout.class);
                        LorebookEntry entry = editor.getEntry();
                        Lorebook lorebook = entry == null ? null : editor.getLorebook();
                        if (header != null && lorebook != null) {
                            LoreEntryRevisionPanel revPanel = new LoreEntryRevisionPanel(vcsService, lorebook, entry, () -> {
                                LorebookEditorDialog dialog = editor.findAncestor(LorebookEditorDialog.class);
                                if (dialog != null) {
                                    dialog.reloadEntry(editor.getEntry());
                                } else {
                                    editor.reloadFromDatabase();
                                }
                            });
                            int index = editor.indexOf(header);
                            editor.addComponentAtIndex(index < 0 ? 0 : index + 1, revPanel);
                            activeEditorPanels.add(revPanel);
                        }
                    }
                }
            };
            extensionService.registerDecorator(entryEditorDecorator,
                    "com.github.enerccio.marginalia.ui.components.LorebookEntryEditor", "build");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Lorebook VCS extension", e);
        }
    }

    /**
     * Redraws the row (and the opened details) of the one entry whose revision changed, reloading the whole view
     * would close the details of all the other entries.
     */
    @SuppressWarnings("unchecked")
    private void refreshEntryRow(VerticalLayout lorebookView, ExtendableMethodContext context, LorebookEntry entry) {
        Grid<LorebookEntry> grid = context.getReflectiveFieldValue(lorebookView, "grid", Grid.class);
        if (grid == null) {
            if (lorebookView instanceof LorebookView desktop) {
                desktop.refreshEntries();
            } else if (lorebookView instanceof PhoneLorebookView phone) {
                phone.refreshEntries();
            }
            return;
        }
        grid.getDataProvider().refreshItem(entry);
    }

    private static boolean isLorebookView(VerticalLayout view) {
        return view instanceof LorebookView || view instanceof PhoneLorebookView;
    }

    private static Lorebook currentLorebookOf(VerticalLayout view) {
        return view instanceof PhoneLorebookView phone ? phone.getCurrentLorebook() : ((LorebookView) view).getCurrentLorebook();
    }

    private void syncAndRefreshView(VerticalLayout lorebookView, ExtendableMethodContext context, Lorebook currentLorebook) throws Exception {
        if (currentLorebook != null && currentLorebook.getId() != null) {
            Lorebook fresh = lorebookService.find(currentLorebook);
            if (fresh != null) {
                context.setReflectiveFieldValue(lorebookView, "currentLorebook", fresh, Lorebook.class);
            }
        }
        if (lorebookView instanceof PhoneLorebookView phone) {
            phone.refresh();
        } else {
            ((LorebookView) lorebookView).refresh();
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        if (viewCreateDecorator != null) {
            extensionService.unregisterDecorator(viewCreateDecorator);
        }
        if (detailLayoutDecorator != null) {
            extensionService.unregisterDecorator(detailLayoutDecorator);
        }
        if (editorListDecorator != null) {
            extensionService.unregisterDecorator(editorListDecorator);
        }
        if (entryEditorDecorator != null) {
            extensionService.unregisterDecorator(entryEditorDecorator);
        }

        // Remove the panels from the lorebook views of all users - change each one in its own session
        synchronized (activeViews) {
            for (VerticalLayout lorebookView : activeViews) {
                UIUtils.accessComponent(lorebookView, () -> {
                    LorebookVCSGlobalPanel globalPanel = (LorebookVCSGlobalPanel) ComponentUtil.getData(lorebookView, GLOBAL_PANEL_KEY);
                    if (globalPanel != null) {
                        lorebookView.remove(globalPanel);
                        ComponentUtil.setData(lorebookView, GLOBAL_PANEL_KEY, null);
                    }
                });
            }
            activeViews.clear();
        }

        synchronized (activeDetailLayouts) {
            for (VerticalLayout detailsLayout : activeDetailLayouts) {
                UIUtils.accessComponent(detailsLayout, () -> {
                    LoreEntryRevisionPanel revPanel = (LoreEntryRevisionPanel) ComponentUtil.getData(detailsLayout, REVISION_PANEL_KEY);
                    if (revPanel != null) {
                        detailsLayout.remove(revPanel);
                        ComponentUtil.setData(detailsLayout, REVISION_PANEL_KEY, null);
                    }
                });
            }
            activeDetailLayouts.clear();
        }

        synchronized (activeEditorPanels) {
            for (Component panel : activeEditorPanels) {
                UIUtils.accessComponent(panel, panel::removeFromParent);
            }
            activeEditorPanels.clear();
        }
    }
}