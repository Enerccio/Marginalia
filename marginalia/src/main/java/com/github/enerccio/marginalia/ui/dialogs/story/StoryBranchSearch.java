package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.utils.SearchHighlighter;
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * The search of the text of the branch that is shown by a story tab (the browser's own search cannot see the parts that
 * are not loaded): a field, buttons for the previous and the next match, which match the user is at out of how many
 * and a progress bar. Every place where a word stands is a match, they are numbered in the order of the story. The
 * parts mark the matches in the text of their cards with {@link #mark}, the match the user is at in a stronger way.
 */
@Configurable(preConstruction = true)
public class StoryBranchSearch extends VerticalLayout {

    /**
     * The branch of the story as it is in the database now.
     */
    @FunctionalInterface
    public interface BranchSource {
        List<ChatMessage> get() throws Exception;
    }

    /**
     * Where the match is: the part and the number of the match inside its text.
     */
    private record Match(Long messageId, int index, int partNumber) {
    }

    @Autowired
    private Localization loc;

    private final BranchSource source;
    private final Consumer<Collection<Long>> redraw;
    private final Consumer<Long> show;

    private final TextField field = new TextField();
    private final Button previous = new Button(VaadinIcon.ARROW_UP.create());
    private final Button next = new Button(VaadinIcon.ARROW_DOWN.create());
    private final Span status = new Span();
    private final ProgressBar progress = new ProgressBar();

    private String lastQuery;
    private FulltextQuery query;
    private Pattern pattern;
    private List<Match> matches = List.of();
    private int current = -1;

    /**
     * @param source the branch to search, read when the user searches
     * @param redraw draws the responses of the cards of these parts again, called when the marks changed
     * @param show   brings the match in the part to the screen (the part may not be loaded)
     */
    public StoryBranchSearch(BranchSource source, Consumer<Collection<Long>> redraw,
                             Consumer<Long> show) {
        this.source = source;
        this.redraw = redraw;
        this.show = show;
        setPadding(false);
        setSpacing(false);
        addClassName(CssStyles.GAP_S);

        field.setPlaceholder(loc.getValue(L.LABEL_SEARCH_THIS_BRANCH));
        field.setPrefixComponent(VaadinIcon.SEARCH.create());
        field.setClearButtonVisible(true);
        field.setWidthFull();
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.setValueChangeTimeout(500);
        field.addValueChangeListener(event -> search(event.getValue()));
        field.addKeyDownListener(Key.ENTER, event -> {
            if (!Objects.equals(StringUtils.trimToNull(field.getValue()), lastQuery)) {
                search(field.getValue());
            } else {
                move(event.getModifiers().contains(KeyModifier.SHIFT) ? -1 : 1);
            }
        });

        previous.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        previous.setTooltipText(loc.getValue(L.LABEL_SEARCH_PREVIOUS_MATCH));
        previous.addClickListener(event -> move(-1));
        next.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        next.setTooltipText(loc.getValue(L.LABEL_SEARCH_NEXT_MATCH));
        next.addClickListener(event -> move(1));
        status.addClassName(CssStyles.TEXT_SECONDARY);

        HorizontalLayout buttons = new HorizontalLayout(previous, next, status);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setSpacing(false);
        buttons.setWidthFull();

        progress.setWidthFull();
        add(field, buttons, progress);
        showStatus();
    }

    /**
     * Puts the cursor into the search field.
     */
    public void focusField() {
        JavascriptLibrary.focusAfterRender(field.getElement());
    }

    /**
     * Marks the matches in the text of a part (markdown).
     *
     * @return the text with the places of the search wrapped in marks; the text itself when nothing is searched
     */
    public String mark(Long messageId, String text) {
        if (pattern == null || matches.isEmpty()) {
            return text;
        }
        int at = -1;
        if (current >= 0 && Objects.equals(matches.get(current).messageId(), messageId)) {
            at = matches.get(current).index();
        }
        return SearchHighlighter.highlight(text, pattern, CssStyles.SEARCH_MATCH, at, CssStyles.SEARCH_MATCH_CURRENT);
    }

    /**
     * The places of the search in the text of a part, as ranges of the text.
     *
     * @return the marks, none when nothing is searched or the part has no match
     */
    public List<TextMarkup.Mark> marks(Long messageId, String text) {
        List<TextMarkup.Mark> marks = new ArrayList<>();
        if (pattern == null || matches.isEmpty()) {
            return marks;
        }
        int at = -1;
        if (current >= 0 && Objects.equals(matches.get(current).messageId(), messageId)) {
            at = matches.get(current).index();
        }
        List<int[]> ranges = SearchHighlighter.ranges(text, pattern);
        for (int i = 0; i < ranges.size(); i++) {
            marks.add(new TextMarkup.Mark(ranges.get(i)[0], ranges.get(i)[1],
                    i == at ? CssStyles.SEARCH_MATCH + " " + CssStyles.SEARCH_MATCH_CURRENT : CssStyles.SEARCH_MATCH));
        }
        return marks;
    }

    /**
     * The story was drawn again with this branch: the matches are found again and the user stays at the one he was
     * at when it still exists. Nothing is shown, the cards that are made next read the marks.
     */
    public void refresh(List<ChatMessage> branch) {
        if (query == null) {
            return;
        }
        Match was = current >= 0 && current < matches.size() ? matches.get(current) : null;
        find(branch);
        current = matches.isEmpty() ? -1 : 0;
        if (was != null) {
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i).messageId().equals(was.messageId()) && matches.get(i).index() == was.index()) {
                    current = i;
                    break;
                }
            }
        }
        showStatus();
    }

    void search(String text) {
        String trimmed = StringUtils.trimToNull(text);
        if (Objects.equals(trimmed, lastQuery)) {
            return;
        }
        lastQuery = trimmed;
        Set<Long> affected = new LinkedHashSet<>();
        matches.forEach(match -> affected.add(match.messageId()));
        FulltextQuery parsed = FulltextQuery.parse(trimmed);
        query = parsed.isEmpty() ? null : parsed;
        pattern = parsed.highlightPattern();
        try {
            find(query == null ? List.of() : source.get());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            matches = List.of();
        }
        current = matches.isEmpty() ? -1 : 0;
        matches.forEach(match -> affected.add(match.messageId()));
        showStatus();
        redraw.accept(affected);
        if (current >= 0) {
            show.accept(matches.get(current).messageId());
        }
    }

    void move(int step) {
        if (matches.isEmpty()) {
            return;
        }
        Long was = matches.get(current).messageId();
        current = Math.floorMod(current + step, matches.size());
        Long now = matches.get(current).messageId();
        showStatus();
        // the card of the match the user left is drawn without the strong mark
        redraw.accept(was.equals(now) ? List.of(now) : List.of(was, now));
        show.accept(now);
    }

    private void find(List<ChatMessage> branch) {
        List<Match> found = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            ChatMessage message = branch.get(i);
            String text = message.getResponse();
            if (query == null || !query.matches(text)) {
                continue;
            }
            int count = SearchHighlighter.count(text, pattern);
            for (int index = 0; index < count; index++) {
                found.add(new Match(message.getId(), index, i + 1));
            }
        }
        matches = found;
    }

    String statusText() {
        return status.getText();
    }

    private void showStatus() {
        boolean any = !matches.isEmpty();
        previous.setEnabled(any);
        next.setEnabled(any);
        progress.setVisible(any);
        if (query == null) {
            status.setText("");
        } else if (!any) {
            status.setText(loc.getValue(L.MSG_SEARCH_NO_RESULTS));
        } else {
            status.setText(String.format(loc.getValue(L.MSG_BRANCH_SEARCH_RESULT), current + 1, matches.size(),
                    matches.get(current).partNumber()));
            progress.setMin(0);
            progress.setMax(matches.size());
            progress.setValue(current + 1);
        }
    }
}
