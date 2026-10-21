package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
     * has none) and words, flags the part as edited and saves it.
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

}
