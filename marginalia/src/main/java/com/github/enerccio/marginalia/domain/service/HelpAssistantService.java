package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.manual.UiStateSource;

import java.util.List;

/**
 * The model behind the help assistant, a chat about the manual ({@link ManualService}). An administrator picks the AI it
 * uses (application settings) and gives the permission {@link
 * com.github.enerccio.marginalia.domain.security.permissions.PermissionType#HELPER} to the users who can ask it (they
 * need no AI of their own, administrators always can). The requests have no protocol: the AI's own limits and defaults
 * apply.
 * <p>
 * The AI is one of the administrator's own, shared or not, so a user never sees or owns it: this service gets it
 * from the settings and the user's request goes through it. Who pays depends on the AI:
 * <ul>
 *   <li>a shared AI and the {@code HELPER} permission of the user with {@code helperApplyLimits}: the request is like any other to the AI, the limit the administrator
 *   set for the user is checked and the tokens are added to the user's usage;</li>
 *   <li>otherwise (an AI that is not shared, or without {@code helperApplyLimits}): the owner of the AI pays, the request
 *   is not limited and not counted for the user ({@link UnmeteredScope}).</li>
 * </ul>
 * The assistant stops working (is not {@link #isAvailable() available}) when the AI is deleted or its owner is no
 * longer an administrator, nobody else's AI is ever exposed.
 */
public interface HelpAssistantService {

    /**
     * The settings. What depends on the user (do the limits apply to them, may the model move through the interface
     * for them) is in the permission {@code HELPER} of the user, see {@link
     * com.github.enerccio.marginalia.domain.security.model.Permission#getHelperApplyLimits()}.
     *
     * @param aiUuid      uuid of the AI, null for none
     * @param adminPrompt the instructions for administrators, null for the default ({@link
     *                    com.github.enerccio.marginalia.domain.service.manual.HelpPrompts#ADMIN})
     * @param userPrompt  the instructions for the other users, null for the default
     * @param adminData   whether the model may read the data of the Admin page (extensions and their logs, backups,
     *                    cleanup, user counts) for an administrator, see {@link
     *                    com.github.enerccio.marginalia.domain.service.manual.AdminHelpTools}
     */
    record Settings(String aiUuid, String adminPrompt, String userPrompt, boolean adminData) {

        /** Settings without the data of the Admin page. */
        public Settings(String aiUuid, String adminPrompt, String userPrompt) {
            this(aiUuid, adminPrompt, userPrompt, false);
        }

        /** Settings with the default prompts. */
        public Settings(String aiUuid) {
            this(aiUuid, null, null);
        }
    }

    /**
     * What a user is told about the assistant.
     *
     * @param aiName  name of the AI that answers
     * @param model   the model of the AI, null when the AI has no model name (an extension AI)
     * @param metered whether the user's limit and usage apply, see {@link HelpAssistantService}
     */
    record Info(String aiName, String model, boolean metered) {}

    /**
     * @throws SecurityException for anybody but an administrator
     */
    Settings getSettings() throws Exception;

    /**
     * Saves the settings. The AI (when set) has to be the current administrator's own.
     * A prompt that is blank or the same as the default is stored as "default".
     *
     * @throws SecurityException        for anybody but an administrator
     * @throws IllegalArgumentException when the AI is not the administrator's
     */
    void saveSettings(Settings settings) throws Exception;

    /**
     * Whether the current user can ask the assistant: it has an AI that can be used now, and the user is an
     * administrator or has the permission {@code HELPER}.
     */
    boolean isAvailable() throws Exception;

    /**
     * Whether the current user let the assistant look at their books, see {@link
     * com.github.enerccio.marginalia.domain.service.manual.BookHelpTools}. Off until they turn it on (the panel of the
     * assistant): what it reads goes to the AI that answers.
     */
    boolean isBooksAllowed() throws Exception;

    /**
     * Saves the choice of the current user, see {@link #isBooksAllowed()}.
     */
    void setBooksAllowed(boolean allowed) throws Exception;

    /**
     * What the current user is told about the assistant: which AI answers and whether their limit applies.
     *
     * @return null when the assistant is not available
     */
    Info getInfo() throws Exception;

    /**
     * Whether a request of the current user is limited and counted: the AI is shared and the administrator chose
     * {@code helperApplyLimits}. False when the assistant is not available.
     */
    boolean isMetered() throws Exception;

    /**
     * Streams the answer of the assistant's AI (without a protocol), see
     * {@link InferenceService#stream(List, com.github.enerccio.marginalia.domain.model.impl.Protocol, FunctionSet,
     * InferenceAsyncCallback)}.
     *
     * @param functions the functions the model can call (like reading a page of the manual), null for none
     * @throws InferenceException of the type {@link InferenceException.Type#NOT_AVAILABLE} when the assistant is not
     *                            available, or the one of the limit when the request is metered and over it
     */
    CancellationToken stream(List<LLMChatMessage> payload, FunctionSet functions, InferenceAsyncCallback callback)
            throws Exception;

    /**
     * Answers the last message of the conversation as the help assistant: {@link #stream} with the assistant's
     * instructions and the index of the manual in front of the conversation and {@link
     * com.github.enerccio.marginalia.domain.service.manual.ManualTools} for the model to search and read the manual.
     * The history is not changed and not stored, that is up to the caller.
     *
     * @param history the conversation so far (user and assistant messages, the last one is the user's question)
     * @throws InferenceException as {@link #stream}
     */
    default CancellationToken ask(List<LLMChatMessage> history, InferenceAsyncCallback callback) throws Exception {
        return ask(history, null, callback);
    }

    /**
     * Like {@link #ask(List, InferenceAsyncCallback)}, and the model can also read the screen of the user (the
     * function {@code get_ui_state}) from the source; when the permission {@code HELPER} of the user has {@code helperInteractions},
     * it also sees what the elements offer to do and can apply it ({@code interact_with_ui}). The instructions are the prompt for administrators or for the
     * other users, whichever the current user is, with {@code {{manualIndex}}} replaced by the list of the manual's pages.
     *
     * @param uiState where the screen is read, null when the question does not come from a page
     */
    CancellationToken ask(List<LLMChatMessage> history, UiStateSource uiState, InferenceAsyncCallback callback)
            throws Exception;

}
