package com.github.enerccio.marginalia.extensions.storytracker.llm;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Which model a tracker asks, and how much of its context a request may take.
 */
@Configurable
public class TrackerModels {

    /** Room kept free besides the answer. */
    private static final long MARGIN_TOKENS = 300;
    /** The room for the answer when the model does not say its limit. */
    private static final long DEFAULT_ANSWER_TOKENS = 4096;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ManuscriptService manuscriptService;

    /** The model and the protocol a run uses; the protocol may be {@code null}. */
    public record ModelSelection(AI ai, Protocol protocol) {
    }

    /** The models of the user, for the settings. */
    public List<AI> availableModels() throws Exception {
        return aiService.findAllForUser().stream().filter(ai -> !ai.isDeleted()).toList();
    }

    /** The protocols of the user, for the settings. */
    public List<Protocol> availableProtocols() throws Exception {
        return protocolService.findAllForUser().stream().filter(p -> !p.isDeleted()).toList();
    }

    /**
     * The model and the protocol of a tracker: the ones the tracker's settings name, else the book's, else the defaults of the
     * user.
     *
     * @param aiUuid       the model of the tracker's settings, may be {@code null}
     * @param protocolUuid the protocol of the tracker's settings, may be {@code null}
     * @return {@code null} when there is none
     */
    public ModelSelection resolve(Manuscript manuscript, String aiUuid, String protocolUuid) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        AI ai = aiUuid != null ? aiService.findForUser(aiUuid) : null;
        Protocol protocol = protocolUuid != null ? protocolService.findForUser(protocolUuid) : null;
        if (ai == null || ai.isDeleted()) {
            ai = book.getAi();
            protocol = protocol != null && !protocol.isDeleted() ? protocol : book.getProtocol();
        }
        if (ai == null || ai.isDeleted()) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            ai = user.getDefaultModel() != null ? aiService.find(user.getDefaultModel()) : null;
            protocol = user.getDefaultProtocol() != null ? protocolService.find(user.getDefaultProtocol()) : protocol;
        }
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
    }

    /** What a request may take: the context minus the room for the answer (which includes the reasoning) and a margin. */
    public static long promptBudget(AI ai) {
        long context = ai.getMaxContext() != null && ai.getMaxContext() > 0 ? ai.getMaxContext() : 8192;
        long answer = ai.getMaxCompletionTokens() != null && ai.getMaxCompletionTokens() > 0 ? ai.getMaxCompletionTokens() : DEFAULT_ANSWER_TOKENS;
        return Math.max(1000, context - answer - MARGIN_TOKENS);
    }

    /** The tokens of a request of a system and a user message, roughly. */
    public static long tokens(InferenceService service, String system, String user) throws Exception {
        return service.countTokensApprox(system + "\n" + user);
    }
}
