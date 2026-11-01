package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.icon.Icon;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The text and the icon of a group of a menu of the story editor, a sub menu that the paths of the items make
 * ({@link StoryMenuItem#path}). A group needs no registration: without one it shows the last name of its path. Registering it
 * ({@link StoryMenuRegistry#group}) gives it a translated text, an icon and a place among its siblings.
 */
public final class MenuGroup {

    private final MenuKind kind;
    private final String path;
    private final Function<Localization, String> label;
    private Supplier<Icon> icon;
    private MenuPosition position = MenuPosition.DEFAULT;
    private boolean separatorAfter;
    private boolean phone = true;

    MenuGroup(MenuKind kind, String path, Function<Localization, String> label) {
        this.kind = kind;
        this.path = path;
        this.label = label;
    }

    static MenuGroup of(MenuKind kind, String path, L label) {
        return new MenuGroup(kind, path, loc -> loc.getValue(label));
    }

    /**
     * The icon in front of the text, a new one for every menu the group is shown in.
     */
    public MenuGroup icon(Supplier<Icon> icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Where the group stands among the entries of the level it is in.
     */
    public MenuGroup position(MenuPosition position) {
        this.position = position == null ? MenuPosition.DEFAULT : position;
        return this;
    }

    /**
     * A line between the group and the entry after it.
     */
    public MenuGroup separatorAfter() {
        this.separatorAfter = true;
        return this;
    }

    /**
     * Whether the phone shows the group too (default yes).
     */
    public MenuGroup phone(boolean phone) {
        this.phone = phone;
        return this;
    }

    public MenuKind getKind() {
        return kind;
    }

    /**
     * The path of the group, which is its identity.
     */
    public String getPath() {
        return path;
    }

    String getText(Localization loc) {
        return label == null ? null : label.apply(loc);
    }

    Icon createIcon() {
        return icon == null ? null : icon.get();
    }

    MenuPosition getPosition() {
        return position;
    }

    boolean hasSeparatorAfter() {
        return separatorAfter;
    }

    boolean isPhone() {
        return phone;
    }

    /**
     * The names of the path, outermost first.
     */
    List<String> names() {
        return MenuPath.parse(path);
    }
}
