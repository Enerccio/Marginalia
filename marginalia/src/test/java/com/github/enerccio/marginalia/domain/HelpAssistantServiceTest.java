package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.collections.LimitStrategy;
import com.github.enerccio.marginalia.domain.collections.ResetStrategy;
import com.github.enerccio.marginalia.domain.components.AILimit;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AILimitService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Settings;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.UsageService;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.manual.HelpPrompts;
import com.github.enerccio.marginalia.domain.service.manual.UiStateSource;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The help assistant: only an administrator sets it up (with an AI of their own), every user can ask it,
 * and who pays depends on the AI: with {@code applyLimits} on a shared AI the user's limit and usage count, otherwise
 * the owner of the AI pays.
 */
class HelpAssistantServiceTest extends MarginaliaTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "How do I back up?"));

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private AILimitService limitService;

    @Autowired
    private UsageService usageService;

    private User admin;
    private OpenAICompatible ai;

    /** The settings belong to the installation and outlive the test, so each test starts from the defaults. */
    /** What the permission of the users created from now on says, see {@link #enable} and {@link #allowInteractions}. */
    private boolean applyLimits;
    private boolean interactions;

    /** Everybody the help is asked by has the permission, {@code MarginaliaTestBase#setPermission} takes it away. */
    @Override
    protected User createUser() throws Exception {
        User user = super.createUser();
        updatePermission(user, PermissionType.HELPER, permission -> {
            permission.setGranted(true);
            permission.setHelperApplyLimits(applyLimits);
            permission.setHelperInteractions(interactions);
        });
        return user;
    }

    /** The users created from now on may be moved through the interface by the model. */
    private void allowInteractions() throws Exception {
        helpService.saveSettings(new Settings(ai.getUuid()));
        interactions = true;
    }

    @BeforeEach
    void resetSettings() throws Exception {
        loginAdmin();
        helpService.saveSettings(new Settings(null));
    }

    /** The administrator's AI, shared or not. */
    private void adminSetup(boolean shared) throws Exception {
        admin = loginAdmin();
        ai = createAI();
        ai.setShared(shared);
        ai = (OpenAICompatible) aiService.save(ai);
    }

    private void enable(boolean applyLimits) throws Exception {
        this.applyLimits = applyLimits;
        helpService.saveSettings(new Settings(ai.getUuid()));
    }

    private InferenceCollector ask() throws Exception {
        InferenceCollector collector = new InferenceCollector();
        helpService.stream(PAYLOAD, null, collector);
        return collector;
    }

    private Usage awaitUsage() throws Exception {
        for (int i = 0; i < 50; i++) {
            Usage usage = usageService.findForUser(ai);
            if (usage != null) {
                return usage;
            }
            Thread.sleep(100);
        }
        return null;
    }

    @Test
    void isOffUntilAnAdministratorPicksAnAi() throws Exception {
        adminSetup(false);

        assertThat(helpService.isAvailable()).isFalse();
        assertThat(helpService.getSettings()).isEqualTo(new Settings(null));

        enable(false);

        assertThat(helpService.getSettings()).isEqualTo(new Settings(ai.getUuid()));
        loginAs(createUser());
        assertThat(helpService.isAvailable()).as("for a user with the permission").isTrue();
    }

    @Test
    void needsThePermissionHelperForUsersButNotForAdministrators() throws Exception {
        adminSetup(false);
        enable(false);
        User user = loginAs(createUser());
        assertThat(helpService.isAvailable()).isTrue();

        loginAdmin();
        setPermission(user, PermissionType.HELPER, false);
        assertThat(helpService.isAvailable()).as("administrators always can").isTrue();

        loginAs(user);
        assertThat(helpService.isAvailable()).isFalse();
        assertThat(helpService.getInfo()).isNull();
        assertThatThrownBy(this::ask).isInstanceOf(InferenceException.class)
                .extracting(e -> ((InferenceException) e).getType()).isEqualTo(Type.NOT_AVAILABLE);

        loginAs(super.createUser());
        assertThat(helpService.isAvailable()).as("no permission at all").isFalse();
    }

    @Test
    void onlyAnAdministratorSetsItUp() throws Exception {
        adminSetup(false);
        enable(false);
        loginAs(createUser());

        assertThatThrownBy(() -> helpService.getSettings()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> helpService.saveSettings(new Settings(null)))
                .isInstanceOf(SecurityException.class);
        assertThat(helpService.isAvailable()).isTrue();
    }

    @Test
    void anAdministratorCanOnlyPickTheirOwnAi() throws Exception {
        adminSetup(true);
        OpenAICompatible adminAi = ai;
        loginAdmin();

        assertThatThrownBy(() -> helpService.saveSettings(new Settings(adminAi.getUuid())))
                .as("a shared AI of another administrator").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> helpService.saveSettings(new Settings("nope")))
                .as("unknown").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isNotAvailableWithoutAnAi() throws Exception {
        adminSetup(false);

        helpService.saveSettings(new Settings(null));
        assertThat(helpService.isAvailable()).isFalse();
        helpService.saveSettings(new Settings(ai.getUuid()));
        assertThat(helpService.getSettings().aiUuid()).isEqualTo(ai.getUuid());
        assertThat(helpService.isAvailable()).isTrue();
    }

    @Test
    void stopsWorkingWhenTheAiIsDeleted() throws Exception {
        adminSetup(false);
        enable(false);
        assertThat(helpService.isAvailable()).isTrue();

        aiService.delete(ai, false);

        assertThat(helpService.isAvailable()).isFalse();
        assertThatThrownBy(this::ask).isInstanceOf(InferenceException.class)
                .extracting(e -> ((InferenceException) e).getType()).isEqualTo(Type.NOT_AVAILABLE);
    }

    @Test
    void anAiThatIsNotSharedIsUsedByEveryoneAndNobodyIsCounted() throws Exception {
        adminSetup(false);
        enable(true);
        User reader = loginAs(createUser());
        llm.enqueue(MockLLMResponse.text("Use the database backups page.").usage(50, 20));

        assertThat(helpService.isMetered()).as("applyLimits means nothing for an AI that is not shared").isFalse();
        InferenceCollector collector = ask();

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        assertThat(collector.getResponse()).isEqualTo("Use the database backups page.");
        assertThat(llm.getCompletionRequests()).hasSize(1);
        Thread.sleep(300);
        assertThat(usageService.findForUser(ai)).as("nothing counted for %s", reader.getLogin()).isNull();
    }

    @Test
    void aSharedAiWithoutApplyLimitsIgnoresTheLimitOfTheUser() throws Exception {
        adminSetup(true);
        enable(false);
        User reader = createUser();
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.NOT_AVAILABLE);
        limitService.setLimit(ai, reader, limit);
        loginAs(reader);
        llm.enqueue(MockLLMResponse.text("answer").usage(10, 10));

        assertThat(helpService.isMetered()).isFalse();
        InferenceCollector collector = ask();

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        Thread.sleep(300);
        assertThat(usageService.findForUser(ai)).isNull();
    }

    @Test
    void aSharedAiWithApplyLimitsRefusesAnUnavailableUser() throws Exception {
        adminSetup(true);
        enable(true);
        User reader = createUser();
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.NOT_AVAILABLE);
        limitService.setLimit(ai, reader, limit);
        loginAs(reader);

        assertThat(helpService.isMetered()).isTrue();
        assertThatThrownBy(this::ask).isInstanceOf(InferenceException.class)
                .extracting(e -> ((InferenceException) e).getType()).isEqualTo(Type.NOT_AVAILABLE);
        assertThat(llm.getCompletionRequests()).isEmpty();

        loginAs(admin);
        assertThat(helpService.isAvailable()).as("the administrator has no limit").isTrue();
    }

    @Test
    void aSharedAiWithApplyLimitsDrainsTheUsageOfTheUser() throws Exception {
        adminSetup(true);
        enable(true);
        User reader = createUser();
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.TOKEN_LIMIT);
        limit.setResetStrategy(ResetStrategy.DAILY);
        limit.setTotalTokens(100L);
        limitService.setLimit(ai, reader, limit);
        loginAs(reader);
        llm.enqueue(MockLLMResponse.text("one").usage(80, 30));

        InferenceCollector first = ask();
        assertThat(first.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        Usage usage = awaitUsage();
        assertThat(usage).isNotNull();
        assertThat(usage.getPrefillTokens()).isEqualTo(80);
        assertThat(usage.getGeneratedTokens()).isEqualTo(30);
        assertThat(usage.getPeriodPrefillTokens() + usage.getPeriodGeneratedTokens()).isEqualTo(110);

        assertThatThrownBy(this::ask).isInstanceOf(InferenceException.class)
                .extracting(e -> ((InferenceException) e).getType()).isEqualTo(Type.LIMIT_REACHED);
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void askGivesTheModelTheManualIndexAndTheTools() throws Exception {
        adminSetup(false);
        enable(false);
        loginAs(createUser());
        llm.enqueue(MockLLMResponse.toolCall("search_manual", "{\"query\":\"database backup\"}"),
                MockLLMResponse.text("See Database backups."));

        InferenceCollector collector = new InferenceCollector();
        helpService.ask(PAYLOAD, collector);

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        assertThat(collector.getResponse()).isEqualTo("See Database backups.");
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getRawBody())
                .contains("help assistant built into Marginalia", "Pages of the manual",
                        "administration/database-backups.md", "How do I back up?", "search_manual", "read_manual_page", "get_ui_state");
        assertThat(requests.get(1).getRawBody()).as("the result of the search").contains("Page: administration/database-backups.md");
    }

    @Test
    void theConversationIsNotChangedByAsk() throws Exception {
        adminSetup(false);
        enable(false);
        llm.enqueue(MockLLMResponse.text("answer"));
        List<LLMChatMessage> history = new java.util.ArrayList<>(PAYLOAD);

        InferenceCollector collector = new InferenceCollector();
        helpService.ask(history, collector);
        collector.await(Duration.ofSeconds(10));

        assertThat(history).isEqualTo(PAYLOAD);
    }

    private String bodyOfLastRequest() {
        return llm.getCompletionRequests().getLast().getRawBody();
    }

    private void askAndWait() throws Exception {
        InferenceCollector collector = new InferenceCollector();
        helpService.ask(PAYLOAD, collector);
        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
    }

    @Test
    void administratorsAndOtherUsersGetTheirOwnDefaultPrompt() throws Exception {
        adminSetup(false);
        enable(false);
        llm.enqueue(MockLLMResponse.text("a"), MockLLMResponse.text("b"));

        askAndWait();
        assertThat(bodyOfLastRequest()).contains("administrator of this installation").doesNotContain("ordinary user");

        loginAs(createUser());
        askAndWait();
        assertThat(bodyOfLastRequest()).contains("ordinary user, not an administrator").doesNotContain("administrator of this installation");
        assertThat(bodyOfLastRequest()).as("the index is in the prompt").contains("administration/database-backups.md")
                .doesNotContain("{{manualIndex}}");
    }

    @Test
    void anAdministratorCanChangeBothPrompts() throws Exception {
        adminSetup(false);
        helpService.saveSettings(new Settings(ai.getUuid(), "Custom admin prompt.\n{{manualIndex}}", "Custom user prompt."));
        llm.enqueue(MockLLMResponse.text("a"), MockLLMResponse.text("b"));

        assertThat(helpService.getSettings().adminPrompt()).startsWith("Custom admin prompt.");
        assertThat(helpService.getSettings().userPrompt()).isEqualTo("Custom user prompt.");

        askAndWait();
        assertThat(bodyOfLastRequest()).contains("Custom admin prompt.").contains("administration/database-backups.md")
                .doesNotContain("help assistant built into Marginalia");

        loginAs(createUser());
        askAndWait();
        assertThat(bodyOfLastRequest()).contains("Custom user prompt.").doesNotContain("Custom admin prompt.")
                .as("no placeholder, no index").doesNotContain("administration/database-backups.md");
    }

    @Test
    void aPromptThatIsBlankOrTheDefaultIsStoredAsTheDefault() throws Exception {
        adminSetup(false);

        helpService.saveSettings(new Settings(ai.getUuid(), HelpPrompts.ADMIN, "   "));

        Settings stored = helpService.getSettings();
        assertThat(stored.adminPrompt()).isNull();
        assertThat(stored.userPrompt()).isNull();
    }

    @Test
    void theModelCanReadTheScreenOfTheUser() throws Exception {
        adminSetup(false);
        enable(false);
        loginAs(createUser());
        List<String> reads = new CopyOnWriteArrayList<>();
        UiStateSource screen = new UiStateSource() {
            @Override
            public void read(String path, boolean deep, boolean withInteractions, FunctionContinuation<String> done) {
                reads.add(path + "|" + deep + "|" + withInteractions);
                done.complete("## UI\nThe user has the story editor open.");
            }

            @Override
            public void interact(String path, List<String> interactions, FunctionContinuation<String> done) {
                done.fail("not expected");
            }

            @Override
            public void readProblems(int count, FunctionContinuation<String> done) {
                done.fail("not expected");
            }
        };
        llm.enqueue(MockLLMResponse.toolCall("get_ui_state", "{\"path\":\"UI/Main\",\"deep\":true}"),
                MockLLMResponse.toolCall("get_ui_state", "{}"),
                MockLLMResponse.text("You are in the story editor."));

        InferenceCollector collector = new InferenceCollector();
        helpService.ask(PAYLOAD, screen, collector);

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        assertThat(reads).containsExactly("UI/Main|true|false", "|false|false");
        assertThat(bodyOfLastRequest()).contains("The user has the story editor open.");
    }

    @Test
    void withoutAPageTheScreenIsNotAvailable() throws Exception {
        adminSetup(false);
        enable(false);
        llm.enqueue(MockLLMResponse.toolCall("get_ui_state", "{}"), MockLLMResponse.text("I can not see it."));

        askAndWait();

        assertThat(bodyOfLastRequest()).contains("The screen of the user is not available.");
    }

    /** A screen that records what the model reads and does. */
    private static final class RecordingScreen implements UiStateSource {
        final List<String> reads = new CopyOnWriteArrayList<>();
        final List<String> moves = new CopyOnWriteArrayList<>();
        final List<Integer> problemReads = new CopyOnWriteArrayList<>();

        @Override
        public void readProblems(int count, FunctionContinuation<String> done) {
            problemReads.add(count);
            done.complete("1. **INFERENCE**, 5 s ago: The provider did not accept the API key.");
        }

        @Override
        public void read(String path, boolean deep, boolean withInteractions, FunctionContinuation<String> done) {
            reads.add(path + "|" + deep + "|" + withInteractions);
            done.complete("## UI\n- `open-section:Lorebooks`");
        }

        @Override
        public void interact(String path, List<String> interactions, FunctionContinuation<String> done) {
            moves.add(path + "|" + String.join(",", interactions));
            done.complete("## UI\nThe Lorebooks section is open.");
        }
    }

    private void askWith(UiStateSource screen) throws Exception {
        InferenceCollector collector = new InferenceCollector();
        helpService.ask(PAYLOAD, screen, collector);
        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
    }

    @Test
    void interactionsAreOffUntilTheirPermissionAllowsThem() throws Exception {
        adminSetup(false);
        enable(false);
        loginAs(createUser());
        llm.enqueue(MockLLMResponse.text("a"));

        askWith(new RecordingScreen());

        assertThat(bodyOfLastRequest()).contains("get_ui_state").doesNotContain("interact_with_ui");
    }

    @Test
    void theModelCanMoveThroughTheInterfaceWhenAllowed() throws Exception {
        adminSetup(false);
        allowInteractions();
        loginAs(createUser());
        RecordingScreen screen = new RecordingScreen();
        llm.enqueue(MockLLMResponse.toolCall("get_ui_state", "{}"),
                MockLLMResponse.toolCall("interact_with_ui",
                        "{\"path\":\"UI/Main/DesktopWorkspace\",\"interactions\":[\"open-section:Lorebooks\"]}"),
                MockLLMResponse.text("You are in the lorebooks."));

        askWith(screen);

        assertThat(screen.reads).as("the elements list what they offer").containsExactly("|false|true");
        assertThat(screen.moves).containsExactly("UI/Main/DesktopWorkspace|open-section:Lorebooks");
        assertThat(bodyOfLastRequest()).contains("The Lorebooks section is open.");
        assertThat(llm.getCompletionRequests().getFirst().getRawBody()).contains("interact_with_ui")
                .contains("You can also move through the interface");
    }

    @Test
    void aSequenceOfInteractionsIsPassedOnInOrder() throws Exception {
        adminSetup(false);
        allowInteractions();
        loginAs(createUser());
        RecordingScreen screen = new RecordingScreen();
        llm.enqueue(MockLLMResponse.toolCall("interact_with_ui",
                        "{\"path\":\"UI/Main/DesktopWorkspace/AdminPart\",\"interactions\":[\"open-tab:Users\",\"open-tab:Cleanup\"]}"),
                MockLLMResponse.text("done"));

        askWith(screen);

        assertThat(screen.moves).containsExactly("UI/Main/DesktopWorkspace/AdminPart|open-tab:Users,open-tab:Cleanup");
    }

    @Test
    void anEmptySequenceIsRefused() throws Exception {
        adminSetup(false);
        allowInteractions();
        loginAs(createUser());
        RecordingScreen screen = new RecordingScreen();
        llm.enqueue(MockLLMResponse.toolCall("interact_with_ui", "{\"path\":\"UI\",\"interactions\":[]}"),
                MockLLMResponse.text("done"));

        askWith(screen);

        assertThat(screen.moves).isEmpty();
        assertThat(bodyOfLastRequest()).contains("Name at least one interaction");
    }

    @Test
    void withoutAPageThereIsNothingToInteractWith() throws Exception {
        adminSetup(false);
        allowInteractions();
        loginAs(createUser());
        llm.enqueue(MockLLMResponse.text("a"));

        askAndWait();

        assertThat(bodyOfLastRequest()).doesNotContain("interact_with_ui");
    }

    @Test
    void theModelCanReadWhatWentWrongForTheUser() throws Exception {
        adminSetup(false);
        enable(false);
        loginAs(createUser());
        RecordingScreen screen = new RecordingScreen();
        llm.enqueue(MockLLMResponse.toolCall("read_last_errors", "{\"count\":5}"),
                MockLLMResponse.toolCall("read_last_errors", "{}"),
                MockLLMResponse.text("The key is wrong."));

        askWith(screen);

        assertThat(screen.problemReads).as("what the model asked for, 0 when it did not say").containsExactly(5, 0);
        assertThat(bodyOfLastRequest()).contains("The provider did not accept the API key.");
        assertThat(llm.getCompletionRequests().getFirst().getRawBody()).contains("read_last_errors");
    }

    @Test
    void withoutAPageThereAreNoErrorsToRead() throws Exception {
        adminSetup(false);
        enable(false);
        llm.enqueue(MockLLMResponse.toolCall("read_last_errors", "{}"), MockLLMResponse.text("I can not see it."));

        askAndWait();

        assertThat(bodyOfLastRequest()).contains("The screen of the user is not available.");
    }

    @Test
    void whatToDoWhenSomethingFailsIsNotLostWithACustomPrompt() throws Exception {
        adminSetup(false);
        helpService.saveSettings(new Settings(ai.getUuid(), "Custom admin prompt.", null));
        llm.enqueue(MockLLMResponse.text("a"));

        askAndWait();

        assertThat(bodyOfLastRequest()).contains("Custom admin prompt.").contains("read_last_errors")
                .contains("Problems with the form");
    }

    @Test
    void theAdministrationDataIsOffUntilAnAdministratorAllowsIt() throws Exception {
        adminSetup(false);
        enable(false);
        assertThat(helpService.getSettings().adminData()).isFalse();
        llm.enqueue(MockLLMResponse.text("a"));

        askAndWait();

        assertThat(bodyOfLastRequest()).doesNotContain("get_admin_data").doesNotContain("read_extension_log");
    }

    @Test
    void anAdministratorCanReadTheAdministrationDataWhenAllowed() throws Exception {
        adminSetup(false);
        helpService.saveSettings(new Settings(ai.getUuid(), null, null, true));
        assertThat(helpService.getSettings().adminData()).isTrue();
        llm.enqueue(MockLLMResponse.toolCall("get_admin_data", "{\"topic\":\"users\"}"),
                MockLLMResponse.toolCall("get_admin_data", "{\"topic\":\"backups\"}"),
                MockLLMResponse.toolCall("get_admin_data", "{\"topic\":\"extensions\"}"),
                MockLLMResponse.text("done"));

        askAndWait();

        String first = llm.getCompletionRequests().getFirst().getRawBody();
        assertThat(first).contains("get_admin_data", "read_extension_log", "You can read the state of the installation");
        String last = bodyOfLastRequest();
        assertThat(last).as("the counts of the users").contains("administrators").contains("locked out");
        assertThat(last).as("the backups").contains("database backup(s)");
        assertThat(last).as("the test installation has no extension system").contains("extension system is not running");
    }

    @Test
    void anUnknownTopicOrExtensionIsRefusedWithWhatThereIs() throws Exception {
        adminSetup(false);
        helpService.saveSettings(new Settings(ai.getUuid(), null, null, true));
        llm.enqueue(MockLLMResponse.toolCall("get_admin_data", "{\"topic\":\"passwords\"}"),
                MockLLMResponse.toolCall("read_extension_log", "{\"extension\":\"sidequery\"}"),
                MockLLMResponse.text("done"));

        askAndWait();

        assertThat(bodyOfLastRequest()).contains("There is no topic").contains("passwords")
                .contains("The topics are: extensions, users, backups, cleanup, application")
                .as("no extension system in the test installation").contains("extension system is not running");
    }

    @Test
    void otherUsersNeverGetTheAdministrationDataEvenWhenAllowed() throws Exception {
        adminSetup(false);
        helpService.saveSettings(new Settings(ai.getUuid(), null, null, true));
        loginAs(createUser());
        llm.enqueue(MockLLMResponse.text("a"));

        askAndWait();

        assertThat(bodyOfLastRequest()).doesNotContain("get_admin_data").doesNotContain("read_extension_log")
                .doesNotContain("You can read the state of the installation");
    }
}
