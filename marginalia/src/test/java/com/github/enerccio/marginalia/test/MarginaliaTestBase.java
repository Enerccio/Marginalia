package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.repository.PermissionRepository;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.test.llm.MockLLMScenario;
import com.github.enerccio.marginalia.test.llm.MockLLMServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base for tests running against the real Spring wiring (services, JPA, Flyway on SQLite), without OSGi and
 * {@code @Extendable} instrumentation, see {@link TestContextPostProcessor}.
 * <p>
 * The context is cached and shared by all test classes, so the database is shared too: create data with unique
 * names ({@link #uniqueName}) or annotate the class with {@code @DirtiesContext} to get a fresh database.
 * <p>
 * Each test method runs in its own mock HTTP request and session (session scoped beans such as the current
 * {@link User} start empty), and gets its own mock LLM scenario in {@link #llm}.
 */
@SpringJUnitWebConfig(locations = "classpath:META-INF/spring/test-application-config.xml")
public abstract class MarginaliaTestBase {

    public static final String DEFAULT_PASSWORD = "password";

    @Autowired
    protected UserService userService;

    @Autowired
    protected PermissionService permissionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    protected AIService aiService;

    @Autowired
    protected InferenceServices inferenceServices;

    /**
     * Session scoped current user, see {@link #loginAs}.
     */
    @Autowired
    protected User currentUser;

    /**
     * Mock LLM programmed by the test, use {@link #createAI()} for an AI pointing to it.
     */
    protected MockLLMScenario llm;

    @BeforeEach
    void setUpMockLLM() {
        llm = MockLLMServer.shared().scenario();
    }

    @AfterEach
    void tearDownMockLLM() {
        llm.close();
    }

    protected static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Creates a user; one who is not an administrator is allowed to create their own AIs ({@code CREATE_AI}), as
     * the tests need them, use {@link #setPermission} to take it away.
     */
    protected User createUser(String login, String password, boolean admin) throws Exception {
        User user = new User();
        user.setLogin(login);
        user.setFullName(login);
        user.setAdmin(admin);
        user = userService.save(user);
        user = userService.changePassword(user, password);
        if (!admin) {
            setPermission(user, PermissionType.CREATE_AI, true);
        }
        return user;
    }

    /**
     * Gives the permission to the user or takes it away. Goes around {@code PermissionService}, which is for
     * administrators and would need one (the tests that count the administrators can't have a spare).
     */
    protected void setPermission(User user, PermissionType type, boolean granted) {
        updatePermission(user, type, permission -> permission.setGranted(granted));
    }

    /**
     * Creates the permission of the user when they have none and changes it, as {@link #setPermission}.
     */
    protected void updatePermission(User user, PermissionType type, Consumer<Permission> change) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            try {
                Permission permission = permissionRepository.findByHolder(user, type);
                if (permission == null) {
                    permission = new Permission();
                    permission.setPermissionHolder(userService.find(user.getId()));
                    permission.setPermissionType(type);
                }
                change.accept(permission);
                permissionRepository.save(permission);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    protected User createUser() throws Exception {
        return createUser(uniqueName("user"), DEFAULT_PASSWORD, false);
    }

    /**
     * Makes the user the current user of the test session, like a successful login in the UI.
     */
    protected User loginAs(User user) {
        currentUser.setId(user.getId());
        currentUser.setLogin(user.getLogin());
        currentUser.setFullName(user.getFullName());
        currentUser.setAdmin(user.isAdmin());
        return user;
    }

    /**
     * Creates a user and logs them in.
     */
    protected User login() throws Exception {
        return loginAs(createUser());
    }

    /**
     * Creates an administrator and logs them in, for the services that only administrators can use.
     */
    protected User loginAdmin() throws Exception {
        return loginAs(createUser(uniqueName("admin"), DEFAULT_PASSWORD, true));
    }

    /**
     * OpenAI compatible AI owned by the current user, pointing to the test's mock LLM scenario.
     */
    protected OpenAICompatible createAI() throws Exception {
        return createAI(llm);
    }

    protected OpenAICompatible createAI(MockLLMScenario scenario) throws Exception {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setAiType(AIType.OPEN_AI_COMPATIBLE);
        ai.setName(uniqueName("mock-ai"));
        ai.setUri(scenario.baseUrl());
        ai.setApiKey("test-key");
        ai.setModel(MockLLMScenario.DEFAULT_MODEL);
        ai.setModelName(MockLLMScenario.DEFAULT_MODEL);
        ai.setNeedsJailbreak(false);
        ai.setEnabledReasoning(false);
        ai.setMaxContext(32000);
        ai.setMaxCompletionTokens(1000);
        return (OpenAICompatible) aiService.save(ai);
    }
}
