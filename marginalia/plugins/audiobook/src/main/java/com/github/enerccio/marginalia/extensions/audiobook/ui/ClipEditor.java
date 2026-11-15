package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Waveform;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;

import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * A clip as a waveform to listen to and to mark a range on: drag over the waveform to select (the From and To fields follow, and the waveform
 * follows them), click to put the playhead there, zoom into the selection for a precise cut. It plays the clip, the selection, or the second
 * around the selection without it (how a cut will sound). The waveform is drawn on the server from the samples ({@link Waveform}); the playback
 * and the playhead are a small script of the plugin on the {@code audio} element.
 */
public class ClipEditor extends VerticalLayout {

    /** The columns of the drawing (it is scaled to the width). */
    private static final int BUCKETS = 1000;
    /** How much is played around a cut, in seconds. */
    private static final double AROUND = 1.5;
    /** The shortest view of a zoom, in seconds. */
    private static final double MIN_VIEW = 0.2;

    private final Element audio = new Element("audio");
    private final Div wave = new Div();
    private final Div drawing = new Div();
    private final Div selection = new Div();
    private final Div cursor = new Div();
    private final Span start = new Span();
    private final Span end = new Span();
    private final Span info = new Span();
    private final NumberField from = new NumberField(T.localized(FROM_SECONDS));
    private final NumberField to = new NumberField(T.localized(TO_SECONDS));
    private final Button play = new Button(T.localized(PLAY), Solid.PLAY.create(), e -> playFrom(-1, -1, -1, -1));
    private final Button pause = new Button(Solid.PAUSE.create(), e -> audio.executeJs("this.pause()"));
    private final Button playSelection = new Button(T.localized(PLAY_THE_SELECTION), Solid.PLAY.create(), e -> playSelection());
    private final Button playAround = new Button(T.localized(PLAY_AROUND_THE_CUT), Solid.SCISSORS.create(), e -> playAround());
    private final Button zoomIn = new Button(Solid.MAGNIFYING_GLASS_PLUS.create(), e -> zoomToSelection());
    private final Button zoomOut = new Button(Solid.MAGNIFYING_GLASS_MINUS.create(), e -> zoomOut());

    private Waveform waveform;
    private double viewFrom;
    private double viewTo;
    private double cursorAt;
    private Double downAt;
    private boolean binding;

    public ClipEditor() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        audio.setAttribute("preload", "auto");
        getElement().appendChild(audio);

        wave.addClassName(AudiobookStyles.WAVE);
        drawing.addClassName(AudiobookStyles.WAVE_DRAWING);
        selection.addClassName(AudiobookStyles.WAVE_SELECTION);
        cursor.addClassName(AudiobookStyles.WAVE_CURSOR);
        wave.add(drawing, selection, cursor);
        // where in the waveform the pointer is, as a fraction of its width
        String x = "Math.min(1, Math.max(0, (event.clientX - element.getBoundingClientRect().left) / element.clientWidth))";
        wave.getElement().addEventListener("mousedown", e -> downAt = e.getEventData().get(x).asDouble()).addEventData(x);
        wave.getElement().addEventListener("mouseup", e -> released(e.getEventData().get(x).asDouble())).addEventData(x);
        start.addClassName(CssStyles.HINT_TEXT);
        end.addClassName(CssStyles.HINT_TEXT);
        info.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout scale = new HorizontalLayout(start, info, end);
        scale.setWidthFull();
        scale.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        for (NumberField field : List.of(from, to)) {
            field.setMin(0);
            field.setStep(0.01);
            field.setStepButtonsVisible(true);
            field.setWidth("9em");
            field.addValueChangeListener(e -> {
                if (!binding) {
                    showSelection();
                }
            });
        }
        pause.setTooltipText(T.localized(PAUSE));
        zoomIn.setTooltipText(T.localized(ZOOM_TO_THE_SELECTION));
        zoomOut.setTooltipText(T.localized(SHOW_THE_WHOLE_CLIP));
        playAround.setTooltipText(T.localized(PLAY_AROUND_THE_CUT_HELP));
        for (Button b : List.of(play, pause, playSelection, playAround, zoomIn, zoomOut)) {
            b.setThemeName("tertiary small");
        }
        HorizontalLayout controls = new HorizontalLayout(play, pause, playSelection, playAround, zoomIn, zoomOut, from, to);
        controls.setWrap(true);
        controls.setAlignItems(FlexComponent.Alignment.BASELINE);
        add(wave, scale, controls);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // the selection follows a drag at once, without a round trip
        wave.getElement().executeJs("""
                const w = this, sel = $0;
                if (w._marginaliaDrag) return;
                w._marginaliaDrag = true;
                const at = e => { const r = w.getBoundingClientRect(); return Math.min(1, Math.max(0, (e.clientX - r.left) / r.width)); };
                w.addEventListener('mousedown', e => { w._down = at(e); });
                w.addEventListener('mousemove', e => {
                  if (w._down == null || !(e.buttons & 1)) return;
                  const x = at(e), a = Math.min(w._down, x), b = Math.max(w._down, x);
                  sel.style.display = 'block'; sel.style.left = (a * 100) + '%'; sel.style.width = ((b - a) * 100) + '%';
                });
                window.addEventListener('mouseup', () => { w._down = null; });
                """, selection.getElement());
    }

    /** Shows a clip (null: none). */
    public void show(Waveform waveform, StreamResource source) {
        this.waveform = waveform;
        audio.executeJs("this.pause()");
        if (source != null) {
            audio.setAttribute("src", source);
        } else {
            audio.removeAttribute("src");
        }
        binding = true;
        try {
            from.clear();
            to.clear();
        } finally {
            binding = false;
        }
        cursorAt = 0;
        viewFrom = 0;
        viewTo = waveform == null ? 0 : waveform.seconds();
        boolean drawn = waveform != null && waveform.samples().length > 0;
        wave.setVisible(drawn);
        for (Button b : List.of(zoomIn, zoomOut, playSelection, playAround)) {
            b.setEnabled(drawn);
        }
        if (drawn) {
            from.setMax(waveform.seconds());
            to.setMax(waveform.seconds());
        }
        render();
    }

    /** The start of the selection in seconds, null when nothing is selected. */
    public Double from() {
        return from.getValue();
    }

    /** The end of the selection in seconds, null when nothing is selected. */
    public Double to() {
        return to.getValue();
    }

    // --- drawing ---

    private void render() {
        if (waveform == null || waveform.samples().length == 0) {
            drawing.getElement().setProperty("innerHTML", "");
            start.setText("");
            end.setText("");
            info.setText("");
            return;
        }
        float[] peaks = waveform.peaks(viewFrom, viewTo, BUCKETS);
        StringBuilder path = new StringBuilder(BUCKETS * 24);
        for (int b = 0; b < BUCKETS; b++) {
            double top = 50 - peaks[b * 2 + 1] * 48;
            double bottom = 50 - peaks[b * 2] * 48;
            if (bottom - top < 0.5) {
                bottom = top + 0.5;
            }
            path.append('M').append(b).append(' ').append(fmt(top)).append('V').append(fmt(bottom));
        }
        drawing.getElement().setProperty("innerHTML", "<svg viewBox=\"0 0 " + BUCKETS + " 100\" preserveAspectRatio=\"none\"><path d=\"" + path
                + "\" vector-effect=\"non-scaling-stroke\"/></svg>");
        wave.getElement().setAttribute("data-from", fmt(viewFrom));
        wave.getElement().setAttribute("data-to", fmt(viewTo));
        start.setText(seconds(viewFrom));
        end.setText(seconds(viewTo));
        showSelection();
        showCursor();
    }

    private void showSelection() {
        Double a = from.getValue();
        Double b = to.getValue();
        if (a == null || b == null || b <= a || viewTo <= viewFrom || b < viewFrom || a > viewTo) {
            // a value computed at runtime, the exception of the styling rule
            selection.getStyle().set("display", "none");
        } else {
            double left = Math.max(0, (a - viewFrom) / (viewTo - viewFrom));
            double right = Math.min(1, (b - viewFrom) / (viewTo - viewFrom));
            selection.getStyle().set("display", "block");
            selection.getStyle().set("left", fmt(left * 100) + "%");
            selection.getStyle().set("width", fmt((right - left) * 100) + "%");
        }
        info.setText(a == null || b == null || b <= a ? "" : T.localized(SELECTED_SECONDS).formatted(seconds(a), seconds(b), seconds(b - a)));
    }

    private void showCursor() {
        if (viewTo <= viewFrom || cursorAt < viewFrom || cursorAt > viewTo) {
            cursor.getStyle().set("display", "none");
        } else {
            cursor.getStyle().set("display", "block");
            cursor.getStyle().set("left", fmt((cursorAt - viewFrom) / (viewTo - viewFrom) * 100) + "%");
        }
    }

    // --- the pointer ---

    private void released(double x) {
        if (waveform == null || downAt == null) {
            return;
        }
        boolean click = Math.abs(x - downAt) < 0.003;
        double a = timeAt(Math.min(downAt, x));
        double b = timeAt(Math.max(downAt, x));
        downAt = null;
        if (click || b - a < 0.01) {
            // a click: the playhead goes there
            cursorAt = a;
            audio.executeJs("this.currentTime = $0", cursorAt);
            showCursor();
            showSelection();
            return;
        }
        binding = true;
        try {
            from.setValue(round(a));
            to.setValue(round(b));
        } finally {
            binding = false;
        }
        showSelection();
    }

    private double timeAt(double fraction) {
        return viewFrom + fraction * (viewTo - viewFrom);
    }

    // --- zoom ---

    private void zoomToSelection() {
        Double a = from.getValue();
        Double b = to.getValue();
        if (waveform == null) {
            return;
        }
        double center;
        double span;
        if (a != null && b != null && b > a) {
            // the selection with a margin, so its edges can still be moved
            span = Math.max(MIN_VIEW, (b - a) * 1.5);
            center = (a + b) / 2;
        } else {
            span = Math.max(MIN_VIEW, (viewTo - viewFrom) / 4);
            center = cursorAt;
        }
        viewFrom = Math.max(0, center - span / 2);
        viewTo = Math.min(waveform.seconds(), viewFrom + span);
        viewFrom = Math.max(0, viewTo - span);
        render();
    }

    private void zoomOut() {
        if (waveform == null) {
            return;
        }
        viewFrom = 0;
        viewTo = waveform.seconds();
        render();
    }

    // --- playback ---

    private void playSelection() {
        Double a = from.getValue();
        Double b = to.getValue();
        if (a != null && b != null && b > a) {
            playFrom(a, b, -1, -1);
        }
    }

    private void playAround() {
        Double a = from.getValue();
        Double b = to.getValue();
        if (a != null && b != null && b > a) {
            playFrom(Math.max(0, a - AROUND), b + AROUND, a, b);
        }
    }

    /**
     * Plays from {@code at} (negative: where the playhead is) to {@code until} (negative: the end), jumping from {@code skipFrom} to {@code skipTo} (negative: no jump); the playhead
     * follows in the browser.
     */
    private void playFrom(double at, double until, double skipFrom, double skipTo) {
        audio.executeJs("""
                const a = this, wave = $4, cur = $5;
                if (a._raf) cancelAnimationFrame(a._raf);
                if ($0 >= 0) a.currentTime = $0;
                const end = $1, skipFrom = $2, skipTo = $3;
                const tick = () => {
                  if (skipFrom >= 0 && a.currentTime >= skipFrom && a.currentTime < skipTo) a.currentTime = skipTo;
                  if (end >= 0 && a.currentTime >= end) a.pause();
                  const vf = parseFloat(wave.dataset.from), vt = parseFloat(wave.dataset.to);
                  if (vt > vf && a.currentTime >= vf && a.currentTime <= vt) {
                    cur.style.display = 'block'; cur.style.left = ((a.currentTime - vf) / (vt - vf) * 100) + '%';
                  }
                  a._raf = a.paused ? null : requestAnimationFrame(tick);
                };
                a.play().then(() => { a._raf = requestAnimationFrame(tick); });
                """, at, until, skipFrom, skipTo, wave.getElement(), cursor.getElement());
    }

    private static double round(double seconds) {
        return Math.round(seconds * 100) / 100.0;
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String seconds(double value) {
        return String.format(Locale.ROOT, "%.2f s", value);
    }
}
