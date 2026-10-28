package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.AgentRunner.LiveAnswer;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import org.apache.commons.lang3.StringUtils;

/**
 * The answer the model is writing now, in the transcript until the round is stored and the real blocks take its place: who is
 * writing, the reasoning so far (open, so it can be followed; the reader may fold it, and it stays as they left it while the
 * answer grows) and the text so far. Updated in place by every poll of the console, parts that are empty are not shown.
 */
final class AgentLiveBubble extends Div {

    private final Details reasoningBlock;
    private final Div reasoningText;
    private final Markdown answer = new Markdown();
    private String shownAnswer = "";

    AgentLiveBubble(AgentTranscriptRenderer renderer, Localization loc, String who) {
        reasoningText = renderer.text("", false);
        reasoningBlock = new Details(loc.getValue(L.LABEL_REASONING), reasoningText);
        reasoningBlock.addClassName(CssStyles.HINT_TEXT);
        reasoningBlock.setOpened(true);
        reasoningBlock.setVisible(false);
        answer.setVisible(false);
        add(renderer.text(who + " (" + loc.getValue(L.LABEL_AGENT_WORKING) + ")", true), reasoningBlock, answer);
        addClassName(CssStyles.HELP_MESSAGE);
    }

    /**
     * @return whether what is shown changed
     */
    boolean show(LiveAnswer live) {
        String thought = StringUtils.defaultString(live.reasoning());
        String text = StringUtils.defaultString(live.text());
        boolean changed = !thought.equals(reasoningText.getText()) || !text.equals(shownAnswer);
        reasoningBlock.setVisible(!thought.isEmpty());
        UIUtils.setTextIfChanged(reasoningText, thought);
        answer.setVisible(!text.isEmpty());
        if (!text.equals(shownAnswer)) {
            answer.setContent(text);
            shownAnswer = text;
        }
        return changed;
    }
}
