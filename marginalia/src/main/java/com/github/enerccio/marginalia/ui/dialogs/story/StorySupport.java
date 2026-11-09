package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.traits.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Small pieces the desktop and the phone story tab do the same way. Both tabs share the behaviour, so a decorator of
 * one of its methods applies to both.
 */
@Configurable
@Extendable
public class StorySupport {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SummaryService summaryService;

    /**
     * How many parts of a book the story and the reader load at a time, the setting of the user.
     *
     * @return the limit, {@code null} when the user has none and everything is loaded
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Integer.class),
            locals = {@APILocals(name = "limit", type = Integer.class),
                    @APILocals(name = "e", type = Exception.class)})
    public Integer partsLimit() {
        try {
            Integer limit = settingService.getOrCreate(UserSetting.class).getStoryPartsLimit();
            return limit != null && limit > 0 ? limit : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Stores the text the author edited: counts its tokens (with the tokenizer of the book's AI, a generic one when it
     * has none) and words and saves it.
     *
     * @return the saved message
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = ChatMessage.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "newResponse", type = String.class)})
    public ChatMessage saveEditedResponse(Manuscript manuscript, ChatMessage message, String newResponse) throws Exception {
        return chatMessageService.updateResponse(manuscript, message, newResponse).message();
    }

    /**
     * The summaries that saving the text would make outdated (they described the text of the part as it is saved now), nothing is
     * saved.
     *
     * @return the summaries, oldest first, empty when the save leaves them valid
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "newResponse", type = String.class)})
    public List<SummaryService.SummaryBlock> outdatedSummaries(Manuscript manuscript, ChatMessage message, String newResponse) throws Exception {
        return chatMessageService.outdatedByEdit(manuscript, message, newResponse);
    }

    /**
     * As {@link #saveEditedResponse(Manuscript, ChatMessage, String)}; with {@code revalidateSummaries} the summaries the edit makes
     * outdated are marked as valid again, for an author who knows that they still say the right thing.
     *
     * @return the saved message
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = ChatMessage.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "newResponse", type = String.class),
                    @APIParameter(name = "revalidateSummaries", type = boolean.class)},
            locals = {@APILocals(name = "edit", type = ChatMessageService.PartEdit.class)})
    public ChatMessage saveEditedResponse(Manuscript manuscript, ChatMessage message, String newResponse, boolean revalidateSummaries) throws Exception {
        ChatMessageService.PartEdit edit = chatMessageService.updateResponse(manuscript, message, newResponse);
        if (revalidateSummaries && !edit.outdated().isEmpty()) {
            summaryService.revalidate(manuscript, edit.outdated());
        }
        return edit.message();
    }

}
