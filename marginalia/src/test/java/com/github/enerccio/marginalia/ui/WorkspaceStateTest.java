package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.story.NewTurnForm;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceAutosave;
import com.github.enerccio.marginalia.ui.workspace.parts.LorebookPart;
import com.github.enerccio.marginalia.ui.workspace.parts.ManuscriptPart;
import com.github.enerccio.marginalia.ui.workspace.phone.PhoneWorkspace;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The workspace autosave: the state the components store and restore ({@link Restorable}) and how it is kept in the
 * user settings.
 */
class WorkspaceStateTest extends MarginaliaTestBase {

    @Autowired
    private SettingService settingService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagService tagService;

    @BeforeEach
    void currentUI() {
        UI.setCurrent(new UI());
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setEnabled(true);
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setEnabled(true);
        entry.setPayload("payload of " + name);
        return lorebookEntryService.save(entry);
    }

    private Manuscript manuscript() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        return manuscriptService.save(manuscript);
    }

    /**
     * Restorable with a state to hand out and a record of what it was asked to restore.
     */
    private static class Probe implements Restorable {
        JsonObject state = new JsonObject();
        JsonObject restored;

        @Override
        public JsonObject storeState() {
            return state;
        }

        @Override
        public void restoreState(JsonObject self) {
            restored = self;
        }
    }

    // ---- storage

    @Test
    void userSettingKeepsTheWorkspaceState() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        assertThat(setting.getWorkspaceState().size()).isZero();

        JsonObject state = json("{\"tab\":\"lorebooks\",\"parts\":{\"books\":{\"filters\":{\"name\":\"dragon\",\"tags\":[\"a\",\"b\"]}}}}");
        setting.setWorkspaceState(state);
        settingService.save(setting);

        assertThat(settingService.getOrCreate(UserSetting.class).getWorkspaceState()).isEqualTo(state);
    }

    @Test
    void savingOtherSettingsKeepsTheWorkspaceState() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setWorkspaceState(json("{\"tab\":\"models\"}"));
        settingService.save(setting);

        UserSetting other = settingService.getOrCreate(UserSetting.class);
        other.setDefaultPov("first person");
        settingService.save(other);

        UserSetting loaded = settingService.getOrCreate(UserSetting.class);
        assertThat(loaded.getDefaultPov()).isEqualTo("first person");
        assertThat(loaded.getWorkspaceState()).isEqualTo(json("{\"tab\":\"models\"}"));
    }

    @Test
    void autosaveWritesNothingUntilTheOldStateIsRestored() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setWorkspaceState(json("{\"tab\":\"models\"}"));
        settingService.save(setting);

        Probe probe = new Probe();
        probe.state = json("{\"tab\":\"books\"}");
        WorkspaceAutosave autosave = new WorkspaceAutosave(probe);

        autosave.save();
        assertThat(settingService.getOrCreate(UserSetting.class).getWorkspaceState()).isEqualTo(json("{\"tab\":\"models\"}"));

        autosave.restore();
        assertThat(probe.restored).isEqualTo(json("{\"tab\":\"models\"}"));

        autosave.save();
        assertThat(settingService.getOrCreate(UserSetting.class).getWorkspaceState()).isEqualTo(json("{\"tab\":\"books\"}"));
    }

    @Test
    void autosaveDoesNotSaveWhileRestoring() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setWorkspaceState(json("{\"tab\":\"models\"}"));
        settingService.save(setting);

        WorkspaceAutosave[] holder = new WorkspaceAutosave[1];
        Probe probe = new Probe() {
            @Override
            public void restoreState(JsonObject self) {
                // the components fire their change events while they restore
                state = json("{\"tab\":\"trash\"}");
                holder[0].save();
            }
        };
        holder[0] = new WorkspaceAutosave(probe);
        holder[0].restore();

        assertThat(settingService.getOrCreate(UserSetting.class).getWorkspaceState()).isEqualTo(json("{\"tab\":\"models\"}"));
    }

    @Test
    void failingRestoreStillStartsTheAutosave() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setWorkspaceState(json("{\"tab\":\"models\"}"));
        settingService.save(setting);

        Probe probe = new Probe() {
            @Override
            public void restoreState(JsonObject self) {
                throw new IllegalStateException("broken");
            }
        };
        probe.state = json("{\"tab\":\"books\"}");
        WorkspaceAutosave autosave = new WorkspaceAutosave(probe);
        autosave.restore();
        autosave.save();

        assertThat(settingService.getOrCreate(UserSetting.class).getWorkspaceState()).isEqualTo(json("{\"tab\":\"books\"}"));
    }

    // ---- components

    @Test
    void newTurnFormStoresWhatTheUserTyped() {
        NewTurnForm form = new NewTurnForm(null);
        // the fields are created by the story part; a form that was not shown keeps the values until it is
        form.restoreState(json("{\"scene\":\"A tavern\",\"pov\":\"Alice\",\"present\":\"Alice, Bob\",\"instructions\":\"Be brief\",\"edited\":true}"));

        assertThat(form.storeState())
                .isEqualTo(json("{\"scene\":\"A tavern\",\"pov\":\"Alice\",\"present\":\"Alice, Bob\",\"instructions\":\"Be brief\",\"edited\":true}"));
        assertThat(form.toTurnInput().sceneSetting()).isEqualTo("A tavern");
        assertThat(form.toTurnInput().instructions()).isEqualTo("Be brief");
    }

    @Test
    void lorebookViewRestoresTheLorebookAndTheOpenEntries() throws Exception {
        login();
        Lorebook first = lorebook("A first");
        Lorebook second = lorebook("B second");
        LorebookEntry opened = entry(second, "opened");
        LorebookEntry closed = entry(second, "closed");
        LorebookEntry other = entry(first, "other");

        LorebookView view = new LorebookView();
        view.create();
        view.refresh();
        assertThat(view.getCurrentLorebook()).isEqualTo(first);

        JsonObject state = new JsonObject();
        state.addProperty("lorebook", second.getUuid());
        state.add("entries", RestorableState.array(java.util.List.of(opened.getUuid(), other.getUuid(), "gone")));
        view.restoreState(state);

        assertThat(view.getCurrentLorebook()).isEqualTo(second);
        // the entry of the other lorebook and the entry that no longer exists are dropped
        JsonObject stored = view.storeState();
        assertThat(stored.get("lorebook").getAsString()).isEqualTo(second.getUuid());
        assertThat(RestorableState.strings(stored, "entries")).containsExactly(opened.getUuid());
        assertThat(closed).isNotNull();
    }

    @Test
    void lorebookViewSkipsALorebookThatIsGone() throws Exception {
        login();
        Lorebook kept = lorebook("A kept");
        Lorebook deleted = lorebook("B deleted");
        lorebookService.delete(deleted, false);

        LorebookView view = new LorebookView();
        view.create();
        view.refresh();
        view.restoreState(json("{\"lorebook\":\"" + deleted.getUuid() + "\"}"));
        assertThat(view.getCurrentLorebook()).isEqualTo(kept);

        view.restoreState(json("{\"lorebook\":\"not-a-uuid\",\"entries\":\"broken\"}"));
        assertThat(view.getCurrentLorebook()).isEqualTo(kept);
        assertThat(view.storeState().has("entries")).isFalse();
    }

    @Test
    void pinnedLorebookViewDoesNotStoreTheLorebook() throws Exception {
        login();
        Lorebook lorebook = lorebook("pinned");
        LorebookEntry entry = entry(lorebook, "opened");

        LorebookView view = new LorebookView(true);
        view.create();
        view.setLorebook(lorebook);
        view.restoreState(json("{\"lorebook\":\"" + lorebook("other").getUuid() + "\",\"entries\":[\"" + entry.getUuid() + "\"]}"));

        assertThat(view.getCurrentLorebook()).isEqualTo(lorebook);
        assertThat(view.storeState()).isEqualTo(json("{\"entries\":[\"" + entry.getUuid() + "\"]}"));
    }

    @Test
    void phoneLorebookViewRestoresTheLorebookAndTheOpenEntries() throws Exception {
        login();
        Lorebook first = lorebook("A first");
        Lorebook second = lorebook("B second");
        LorebookEntry opened = entry(second, "opened");

        PhoneLorebookView view = new PhoneLorebookView();
        view.create();
        view.restoreState(json("{\"lorebook\":\"" + second.getUuid() + "\",\"entries\":[\"" + opened.getUuid() + "\",\"gone\"]}"));

        assertThat(view.getCurrentLorebook()).isEqualTo(second);
        assertThat(view.storeState())
                .isEqualTo(json("{\"lorebook\":\"" + second.getUuid() + "\",\"entries\":[\"" + opened.getUuid() + "\"]}"));
        assertThat(first).isNotNull();
    }

    // ---- dialogs

    @Test
    void manuscriptDialogRestoresTheTabAndTheNewTurn() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        ManuscriptDialog dialog = new ManuscriptDialog(manuscript);
        dialog.create();

        dialog.restoreState(json("{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"lorebook\","
                + "\"parts\":{\"story\":{\"newTurn\":{\"scene\":\"A tavern\",\"pov\":\"Alice\",\"present\":\"\",\"instructions\":\"Be brief\",\"edited\":true}}}}"));

        JsonObject stored = dialog.storeState();
        assertThat(stored.get("manuscript").getAsString()).isEqualTo(manuscript.getUuid());
        assertThat(stored.get("tab").getAsString()).isEqualTo("lorebook");
        assertThat(stored.getAsJsonObject("parts").getAsJsonObject("story").getAsJsonObject("newTurn"))
                .isEqualTo(json("{\"scene\":\"A tavern\",\"pov\":\"Alice\",\"present\":\"\",\"instructions\":\"Be brief\",\"edited\":true}"));
    }

    @Test
    void manuscriptDialogReportsNavigation() throws Exception {
        login();
        ManuscriptDialog dialog = new ManuscriptDialog(manuscript());
        dialog.create();
        int[] changes = {0};
        dialog.setOnStateChange(() -> changes[0]++);

        dialog.restoreState(json("{\"tab\":\"prompt\"}"));

        assertThat(changes[0]).isPositive();
        assertThat(dialog.storeState().get("tab").getAsString()).isEqualTo("prompt");
    }

    @Test
    void phoneManuscriptDialogRestoresTheTabAndTheNewTurn() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        PhoneManuscriptDialog dialog = new PhoneManuscriptDialog(manuscript);
        dialog.create();

        dialog.restoreState(json("{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"lorebook\","
                + "\"parts\":{\"story\":{\"newTurn\":{\"scene\":\"A tavern\",\"pov\":\"Alice\",\"present\":\"\",\"instructions\":\"Be brief\",\"edited\":true}}}}"));

        JsonObject stored = dialog.storeState();
        assertThat(stored.get("tab").getAsString()).isEqualTo("lorebook");
        assertThat(stored.getAsJsonObject("parts").getAsJsonObject("story").getAsJsonObject("newTurn").get("instructions").getAsString())
                .isEqualTo("Be brief");
    }

    // ---- workspace parts

    @Test
    void manuscriptPartRestoresTheFilters() throws Exception {
        login();
        Tag tag = tagService.getOrCreateForUser(uniqueName("tag"));
        Tag deleted = tagService.getOrCreateForUser(uniqueName("tag"));
        tagService.delete(deleted, false);
        ManuscriptPart part = new ManuscriptPart(null);
        part.create();

        part.restoreState(json("{\"filters\":{\"name\":\"dragon\",\"tags\":[\"" + tag.getUuid() + "\",\"" + deleted.getUuid() + "\"]}}"));

        assertThat(part.storeState()).isEqualTo(json("{\"filters\":{\"name\":\"dragon\",\"tags\":[\"" + tag.getUuid() + "\"]}}"));
    }

    @Test
    void manuscriptPartOpensTheStoredBookOnTheStoredTab() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        ManuscriptPart part = new ManuscriptPart(null);
        part.create();

        part.restoreState(json("{\"dialog\":{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"prompt\"}}"));

        JsonObject dialog = part.storeState().getAsJsonObject("dialog");
        assertThat(dialog.get("manuscript").getAsString()).isEqualTo(manuscript.getUuid());
        assertThat(dialog.get("tab").getAsString()).isEqualTo("prompt");
    }

    @Test
    void manuscriptPartDoesNotOpenABookThatIsGone() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        manuscriptService.delete(manuscript, false);
        ManuscriptPart part = new ManuscriptPart(null);
        part.create();

        part.restoreState(json("{\"dialog\":{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"prompt\"}}"));

        assertThat(part.storeState().has("dialog")).isFalse();
    }

    @Test
    void phoneManuscriptPartOpensTheStoredBookOnTheStoredTab() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        PhoneManuscriptPart part = new PhoneManuscriptPart(null);
        part.create();

        part.restoreState(json("{\"filters\":{\"name\":\"dragon\"},\"dialog\":{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"prompt\"}}"));

        JsonObject stored = part.storeState();
        assertThat(stored.getAsJsonObject("filters").get("name").getAsString()).isEqualTo("dragon");
        assertThat(stored.getAsJsonObject("dialog").get("tab").getAsString()).isEqualTo("prompt");
    }

    @Test
    void lorebookPartPassesTheStateToItsView() throws Exception {
        login();
        Lorebook lorebook = lorebook("part");
        LorebookEntry entry = entry(lorebook, "opened");
        LorebookPart part = new LorebookPart(null);
        part.create();

        JsonObject state = json("{\"lorebook\":\"" + lorebook.getUuid() + "\",\"entries\":[\"" + entry.getUuid() + "\"]}");
        part.restoreState(state);

        assertThat(part.storeState()).isEqualTo(state);
    }

    // ---- workspaces

    private void workspaceRestoresTabAndFilters(Workspace workspace) throws Exception {
        Tag tag = tagService.getOrCreateForUser(uniqueName("tag"));
        Lorebook lorebook = lorebook("restored");
        LorebookEntry entry = entry(lorebook, "opened");
        workspace.create();
        workspace.refresh();

        JsonObject state = json("{\"tab\":\"lorebooks\",\"parts\":{"
                + "\"books\":{\"filters\":{\"name\":\"dragon\",\"tags\":[\"" + tag.getUuid() + "\"]}},"
                + "\"lorebooks\":{\"lorebook\":\"" + lorebook.getUuid() + "\",\"entries\":[\"" + entry.getUuid() + "\"]}}}");
        workspace.restoreState(state);

        assertThat(workspace.storeState()).isEqualTo(state);
    }

    @Test
    void phoneWorkspaceRestoresTabAndFilters() throws Exception {
        login();
        workspaceRestoresTabAndFilters(new PhoneWorkspace());
    }

    private void workspaceSkipsWhatIsGone(Workspace workspace) throws Exception {
        Tag tag = tagService.getOrCreateForUser(uniqueName("tag"));
        tagService.delete(tag, false);
        Lorebook lorebook = lorebook("deleted");
        lorebookService.delete(lorebook, false);
        Manuscript manuscript = manuscript();
        manuscriptService.delete(manuscript, false);
        workspace.create();
        workspace.refresh();

        workspace.restoreState(json("{\"tab\":\"models\",\"parts\":{"
                + "\"books\":{\"filters\":{\"name\":\"dragon\",\"tags\":[\"" + tag.getUuid() + "\",\"unknown\"]},"
                + "\"dialog\":{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"story\"}},"
                + "\"lorebooks\":{\"lorebook\":\"" + lorebook.getUuid() + "\"}}}"));

        assertThat(workspace.storeState()).isEqualTo(json("{\"tab\":\"models\",\"parts\":{\"books\":{\"filters\":{\"name\":\"dragon\"}}}}"));
    }

    @Test
    void phoneWorkspaceSkipsWhatIsGone() throws Exception {
        login();
        workspaceSkipsWhatIsGone(new PhoneWorkspace());
    }

    @Test
    void workspaceIgnoresAStateOfAnotherShape() throws Exception {
        login();
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();

        workspace.restoreState(json("{\"tab\":5,\"parts\":[1,2],\"unknown\":{}}"));
        workspace.restoreState(json("{\"tab\":\"nowhere\",\"parts\":{\"books\":\"x\",\"lorebooks\":{\"entries\":{}}}}"));

        assertThat(workspace.storeState().get("tab").getAsString()).isEqualTo("books");
    }

}
