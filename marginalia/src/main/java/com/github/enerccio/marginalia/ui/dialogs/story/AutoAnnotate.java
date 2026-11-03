package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIParameter;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The automatic annotation of a generated part, set per book in {@link AutoAnnotateDialog} (the Tools menu of the story): the annotation
 * window opens and starts at once ({@link AnnotateDialog#openAuto}); when the notes are added without asking, the redraft window follows,
 * if it is set, and starts at once to address them ({@link RedraftDialog#openAuto}). Each window closes by itself when its result is
 * taken without asking, otherwise it waits for the author. Closing a window ends the run: what would follow is dropped.
 */
@Extendable
@Configurable(preConstruction = true)
public class AutoAnnotate {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    private final Runnable onChanged;

    /**
     * @param manuscript the book, as it is stored now
     * @param onChanged  shows the story again, called after annotations were added or the part was redrafted
     */
    public AutoAnnotate(Manuscript manuscript, Runnable onChanged) {
        this.manuscript = manuscript;
        this.onChanged = onChanged;
    }

    /**
     * Whether the book annotates the parts it generates.
     */
    public static boolean isEnabled(Manuscript manuscript) {
        return manuscript != null && manuscript.getAutoAnnotate();
    }

    /**
     * Starts the run for a part that was just generated, when the book is set to annotate them and the part has a text.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "generated", type = ChatMessage.class)},
            locals = {@APILocals(name = "part", type = ChatMessage.class),
                    @APILocals(name = "auto", type = AnnotateDialog.Auto.class)})
    public void run(ChatMessage generated) throws Exception {
        if (generated == null) {
            return;
        }
        if (!isEnabled(manuscript)) {
            finished(generated);
            return;
        }
        ChatMessage part = chatMessageService.find(generated);
        if (part == null || StringUtils.isBlank(part.getResponse())) {
            finished(generated);
            return;
        }
        boolean accept = manuscript.getAutoAnnotateAccept();
        boolean redraft = accept && manuscript.getAutoAnnotateRedraft();
        AnnotateDialog.Auto auto = new AnnotateDialog.Auto(manuscript.getAutoAnnotateInstructions(),
                byUuid(aiService.findAllForUser(), manuscript.getAutoAnnotateModel()),
                byUuid(protocolService.findAllForUser(), manuscript.getAutoAnnotateProtocol()), accept);
        AnnotateDialog.openAuto(manuscript, part, auto, () -> {
            onChanged.run();
            if (redraft) {
                redraft(part);
            } else {
                finished(part);
            }
        });
    }

    /**
     * Called when nothing more will happen to a generated part by itself: right away when the book does not annotate its parts, else
     * when the notes were added and, if the book redrafts, the redraft was applied. It is not called when the author closes a window of
     * the run (the part is then in the author's hands). A hook for extensions that read the
     * finished text of the new part ({@code registerDecorator}, after the method).
     *
     * @param part the generated part, as it is stored now
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "part", type = ChatMessage.class)})
    public void finished(ChatMessage part) {
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "part", type = ChatMessage.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void redraft(ChatMessage part) {
        try {
            // the text of the part as it is now, the notes are given to the model by the dialog
            RedraftDialog.openAuto(manuscript, chatMessageService.find(part), manuscript.getAutoAnnotateAcceptRedraft(),
                    _ -> {
                        onChanged.run();
                        try {
                            finished(chatMessageService.find(part));
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                    });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The model or the protocol a book names by its uuid among the ones the user can use now; null when none is named, or when it
     * is gone (the model of the book is used then).
     */
    static <T extends OwnedEntity> T byUuid(List<T> candidates, String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return null;
        }
        return candidates.stream().filter(candidate -> uuid.equals(candidate.getUuid())).findFirst().orElse(null);
    }
}
