package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.github.enerccio.marginalia.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The marks of the text a model wrote for a card of the story (the desktop and the phone one), worked out from the history of the part
 * ({@link MessageRevisionService#authorship(ChatMessage)}). The history is read once for a text, the card draws its text many times.
 */
public class CardAuthorship {

    private final MessageRevisionService revisions;
    private final Localization loc;

    private String text;
    private List<int[]> ranges = List.of();

    public CardAuthorship(MessageRevisionService revisions, Localization loc) {
        this.revisions = revisions;
        this.loc = loc;
    }

    /**
     * @param generating whether the text is being generated: all of it is the model's then, and it changes with every chunk
     * @return the marks of the text a model wrote
     */
    public List<TextMarkup.Mark> marks(ChatMessage message, String response, boolean generating) {
        List<TextMarkup.Mark> marks = new ArrayList<>();
        if (response.isEmpty()) {
            return marks;
        }
        if (generating) {
            marks.add(new TextMarkup.Mark(0, response.length(), CssStyles.GENERATED_TEXT));
            return marks;
        }
        if (!response.equals(text)) {
            try {
                ranges = revisions.authorship(message).generatedRanges();
                text = response;
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
                return marks;
            }
        }
        for (int[] range : ranges) {
            marks.add(new TextMarkup.Mark(range[0], range[1], CssStyles.GENERATED_TEXT));
        }
        return marks;
    }

    /**
     * Forgets what was worked out, the history changed.
     */
    public void invalidate() {
        text = null;
        ranges = List.of();
    }
}
