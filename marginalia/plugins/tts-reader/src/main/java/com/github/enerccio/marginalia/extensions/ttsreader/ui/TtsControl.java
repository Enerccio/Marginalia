package com.github.enerccio.marginalia.extensions.ttsreader.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

import static com.github.enerccio.marginalia.extensions.ttsreader.TtsReaderLocalization.*;

/**
 * The controls of the read-aloud in the viewer: play / pause, stop and a panel with the voice, the speed and the pitch.
 * The component only draws them; {@code tts-reader.js} does the reading in the browser (the speech synthesis of the
 * device) and keeps the settings in the browser's storage, so the server holds no state.
 */
public class TtsControl extends Div {

    private final Component scroller;
    private final Component content;
    private final String language;

    private final Button play = button(Solid.PLAY, READ_ALOUD, TtsReaderStyles.BUTTON_PLAY);
    private final Button pause = button(Solid.PAUSE, PAUSE_READING, TtsReaderStyles.BUTTON_PAUSE);
    private final Button stop = button(Solid.STOP, STOP_READING, TtsReaderStyles.BUTTON_STOP);
    private final Button settings = button(Solid.GEAR, READING_SETTINGS, null);
    private final Div panel = new Div();
    private final Element voice = new Element("select");
    private final Element speed = range();
    private final Element pitch = range();

    /**
     * @param scroller the scrolling panel of the reader
     * @param content  the layout whose children are the parts (the elements with {@code data-message-id})
     * @param language the code of the language of the book, {@code null} when it has none
     */
    public TtsControl(Component scroller, Component content, String language) {
        this.scroller = scroller;
        this.content = content;
        this.language = language == null ? "" : language;
        addClassName(TtsReaderStyles.BAR);
        panel.addClassName(TtsReaderStyles.PANEL);
        panel.getElement().appendChild(field(VOICE, voice), field(SPEED, speed), field(PITCH, pitch));
        add(play, pause, stop, settings, panel);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().executeJs(TtsReaderStyles.script(), getElement(), scroller.getElement(), content.getElement(),
                play.getElement(), pause.getElement(), stop.getElement(), settings.getElement(), panel.getElement(),
                voice, speed, pitch, language, T.localized(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // leaving the book silences the voice, the speech belongs to the browser, not to the page
        detachEvent.getUI().getPage().executeJs("if (window.speechSynthesis) { window.speechSynthesis.cancel(); }");
    }

    private static Button button(Solid icon, String label, String cssClass) {
        Button button = new Button(icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        button.setTooltipText(T.localized(label));
        button.getElement().setAttribute("aria-label", T.localized(label));
        if (cssClass != null) {
            button.addClassName(cssClass);
        }
        return button;
    }

    private static Element range() {
        return new Element("input").setAttribute("type", "range").setAttribute("min", "0.5")
                .setAttribute("max", "2").setAttribute("step", "0.1");
    }

    private static Element field(String label, Element input) {
        Element field = new Element("label");
        field.getClassList().add(TtsReaderStyles.FIELD);
        field.appendChild(new Element("span").setText(T.localized(label)), input);
        return field;
    }
}
