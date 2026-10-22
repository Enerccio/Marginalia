package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.manual.*;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HelpAssistantServiceImpl implements HelpAssistantService {

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManualService manualService;

    @Autowired
    private User currentUser;

    @Autowired
    private DatabaseBackupService databaseBackupService;

    @Autowired
    private CleanupService cleanupService;

    // not there without the extension system (the tests)
    @Autowired(required = false)
    private OsgiService osgiService;

    /** What a request uses: the AI of the settings, and whether the user's limits apply. */
    private record Target(AI ai, boolean metered) {}

    @Override
    @CommonTxReadOnly
    public Settings getSettings() throws Exception {
        adminGuard.requireAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        return new Settings(app.getHelpAiUuid(), app.getHelpPromptAdmin(), app.getHelpPromptUser(), app.getHelpAdminData());
    }

    @Override
    @CommonTx
    public void saveSettings(Settings settings) throws Exception {
        adminGuard.requireAdmin();
        String aiUuid = StringUtils.trimToNull(settings.aiUuid());
        if (aiUuid != null && !isOwnedByCurrentUser(load(aiService, aiUuid))) {
            throw new IllegalArgumentException("The help AI has to be your own AI");
        }
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        app.setHelpAiUuid(aiUuid);
        app.setHelpAdminData(settings.adminData());
        app.setHelpPromptAdmin(promptOrNull(settings.adminPrompt(), HelpPrompts.ADMIN));
        app.setHelpPromptUser(promptOrNull(settings.userPrompt(), HelpPrompts.USER));
        settingService.save(app);
    }

    @Override
    @NoTx
    public boolean isAvailable() throws Exception {
        return target().isPresent();
    }

    @Override
    @NoTx
    public Info getInfo() throws Exception {
        return target().map(t -> new Info(t.ai().getName(),
                t.ai() instanceof OpenAICompatible openAi ? openAi.getModelName() : null, t.metered())).orElse(null);
    }

    @Override
    @NoTx
    public boolean isMetered() throws Exception {
        return target().map(Target::metered).orElse(false);
    }

    @Override
    @NoTx
    public CancellationToken stream(List<LLMChatMessage> payload, FunctionSet functions, InferenceAsyncCallback callback)
            throws Exception {
        Target target = target().orElseThrow(() -> new InferenceException(Type.NOT_AVAILABLE, null, null, null));
        InferenceService service = inferenceServices.forAIOrThrow(target.ai());
        if (target.metered()) {
            return service.stream(payload, null, functions, callback);
        }
        try (UnmeteredScope _ = UnmeteredScope.open()) {
            return service.stream(payload, null, functions, callback);
        }
    }

    @Override
    @NoTx
    public CancellationToken ask(List<LLMChatMessage> history, UiStateSource uiState, InferenceAsyncCallback callback)
            throws Exception {
        // only with a screen to move through: a request that does not come from a page has nothing to interact with
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        Permission helper = helperPermission();
        boolean interactive = uiState != null && helper != null && helper.getHelperInteractions();
        // the data of the Admin page: only for an administrator, and only when an administrator allowed it
        boolean adminData = app.getHelpAdminData() && adminGuard.isAdmin();
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, instructions() + HelpPrompts.PROBLEMS
                + (interactive ? HelpPrompts.INTERACTIVE : "") + (adminData ? HelpPrompts.ADMIN_DATA : "")));
        payload.addAll(history);
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        FunctionSet tools = interactive
                ? registry.bind(InteractiveHelpTools.class, new InteractiveHelpToolsImpl(manualService, uiState))
                : registry.bind(HelpTools.class, new HelpToolsImpl(manualService, uiState));
        if (adminData) {
            AdminDataSource data = new HelpAdminData(adminGuard, osgiService, userService, databaseBackupService,
                    cleanupService, ThreadCopyRequestAttributes.create());
            tools = tools.and(registry.bind(AdminHelpTools.class, new AdminHelpToolsImpl(data)));
        }
        return stream(payload, tools, callback);
    }

    /** A blank prompt or one that is the default is not stored, so that the default can change with a new version. */
    private static String promptOrNull(String prompt, String defaultPrompt) {
        return StringUtils.isBlank(prompt) || prompt.strip().equals(defaultPrompt.strip()) ? null : prompt;
    }

    /**
     * The prompt for the current user (administrators have their own, an administrator can change both) with the
     * index of the manual in place of {@code {{manualIndex}}}.
     */
    private String instructions() throws Exception {
        boolean administrator = adminGuard.isAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        String stored = administrator ? app.getHelpPromptAdmin() : app.getHelpPromptUser();
        String prompt = StringUtils.isBlank(stored) ? HelpPrompts.defaultPrompt(administrator) : stored;
        return prompt.replace(HelpPrompts.MANUAL_INDEX, manualIndex());
    }

    private String manualIndex() {
        StringBuilder sb = new StringBuilder();
        for (ManualPage page : manualService.listPages()) {
            String description = page.description().length() > 110 ? page.description().substring(0, 110) + "..." : page.description();
            sb.append("- ").append(page.path()).append(": ").append(page.title());
            if (!description.isEmpty()) {
                sb.append(" - ").append(description);
            }
            sb.append('\n');
        }
        return sb.toString().stripTrailing();
    }

    /**
     * The permission {@code HELPER} of the current user, null when they do not have it (an administrator may ask
     * without it, then the settings of the permission have their defaults: the limits do not apply and the model does
     * not move through the interface).
     */
    private Permission helperPermission() throws Exception {
        Permission permission = permissionService.getPermission(currentUser, PermissionType.HELPER);
        return permission != null && permission.isGranted() ? permission : null;
    }

    private Optional<Target> target() throws Exception {
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        Permission helper = helperPermission();
        if (helper == null && !adminGuard.isAdmin()) {
            return Optional.empty();
        }
        AI ai = load(aiService, app.getHelpAiUuid());
        if (!ownedByAdministrator(ai)) {
            return Optional.empty();
        }
        return Optional.of(new Target(ai, ai.isShared() && helper != null && helper.getHelperApplyLimits()));
    }

    /**
     * The entity with the uuid whoever owns it, null when there is none or it is deleted.
     */
    private <T extends OwnedEntity> T load(BaseService<T, ?> service, String uuid) throws Exception {
        if (StringUtils.isBlank(uuid)) {
            return null;
        }
        Long id = service.find(uuid);
        T entity = id == null ? null : service.find(id);
        return entity == null || entity.isDeleted() ? null : entity;
    }

    private boolean isOwnedByCurrentUser(OwnedEntity entity) {
        return entity != null && entity.getOwner() != null
                && Objects.equals(entity.getOwner().getId(), currentUser.getId());
    }

    /**
     * An AI of somebody who is no longer an administrator (or of nobody) is never used: the setting must not hand
     * a private AI of an ordinary user to everybody.
     */
    private boolean ownedByAdministrator(OwnedEntity entity) throws Exception {
        if (entity == null || entity.getOwner() == null) {
            return false;
        }
        User owner = userService.find(entity.getOwner().getId());
        return owner != null && !owner.isDeleted() && owner.isAdmin();
    }
}
