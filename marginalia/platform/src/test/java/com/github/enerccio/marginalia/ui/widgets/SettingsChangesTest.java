package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsChangesTest {

    @Test
    void aChangeDeepInsideReachesTheNearestListener() {
        Div page = new Div();
        Div form = new Div();
        Div list = new Div();
        Span row = new Span();
        page.add(form);
        form.add(list);
        list.add(row);
        AtomicInteger pageChanges = new AtomicInteger();
        AtomicInteger formChanges = new AtomicInteger();
        SettingsChanges.listen(page, pageChanges::incrementAndGet);

        SettingsChanges.changed(row);
        assertEquals(1, pageChanges.get());

        SettingsChanges.listen(form, formChanges::incrementAndGet);
        SettingsChanges.changed(row);
        assertEquals(1, pageChanges.get(), "the nearest listener takes it");
        assertEquals(1, formChanges.get());

        SettingsChanges.changed(form);
        assertEquals(2, formChanges.get(), "the source itself may listen");
    }

    @Test
    void withoutAListenerNothingHappens() {
        Div form = new Div();
        Span row = new Span();
        form.add(row);

        assertDoesNotThrow(() -> SettingsChanges.changed(row));
        assertDoesNotThrow(() -> SettingsChanges.changed(null));
    }
}
