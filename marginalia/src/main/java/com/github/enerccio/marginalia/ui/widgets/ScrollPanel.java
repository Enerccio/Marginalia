package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ScrollPanel extends VerticalLayout {
    private VerticalLayout content;
    private Component header;
    private Component footer;

    public ScrollPanel(){
        super();
        preparePanel();
    }

    public ScrollPanel(Component... children){
        super();
        preparePanel();
        this.add(children);
    }

    private void preparePanel() {
        addClassName(CssStyles.OVERFLOW_AUTO);
        content = new VerticalLayout();
        content.addClassName(CssStyles.DISPLAY_BLOCK);
        content.setWidth("100%");
        super.add(content);
        setHeight("100%");
    }

    public VerticalLayout getContent(){
        return content;
    }

    /**
     * Puts the component before the content, inside the scrolled area (a "loading earlier parts" indicator).
     * {@link #removeAll()} leaves it there.
     */
    public void setHeader(Component header) {
        if (this.header != null) {
            super.remove(this.header);
        }
        this.header = header;
        if (header != null) {
            super.addComponentAsFirst(header);
        }
    }

    /**
     * Puts the component after the content, inside the scrolled area. {@link #removeAll()} leaves it there.
     */
    public void setFooter(Component footer) {
        if (this.footer != null) {
            super.remove(this.footer);
        }
        this.footer = footer;
        if (footer != null) {
            super.add(footer);
        }
    }

    @Override
    public void add(Component... components){
        content.add(components);
    }

    @Override
    public void remove(Component... components){
        content.remove(components);
    }

    @Override
    public void removeAll(){
        content.removeAll();
    }

    @Override
    public void addComponentAsFirst(Component component) {
        content.addComponentAtIndex(0, component);
    }
}
