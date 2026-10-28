package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.collections.ThemeMode;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.page.ColorScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Button that switches the UI between the dark and the light color scheme. The choice is saved to the settings of the
 * user, the workspace applies it when it is opened ({@link #apply}).
 */
@Configurable(preConstruction = true)
public class ThemeSwitch extends Button {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    private ThemeMode mode;

    public ThemeSwitch() {
        mode = load();
        refresh();
        addClickListener(event -> change());
    }

    /**
     * Sets the color scheme of the page of the current UI.
     */
    public static void apply(ThemeMode mode) {
        UI.getCurrentOrThrow().getPage().setColorScheme(mode == ThemeMode.LIGHT ? ColorScheme.Value.LIGHT : ColorScheme.Value.DARK);
    }

    private ThemeMode load() {
        try {
            return settingService.getOrCreate(UserSetting.class).getThemeMode();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return ThemeMode.DARK;
        }
    }

    private void change() {
        try {
            mode = mode.toggled();
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setThemeMode(mode);
            settingService.save(setting);
            apply(mode);
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The icon and the tooltip name what a click switches to.
     */
    private void refresh() {
        boolean dark = mode == ThemeMode.DARK;
        setIcon((dark ? Solid.SUN : Solid.MOON).create());
        setTooltipText(loc.getValue(dark ? L.LABEL_LIGHT_THEME : L.LABEL_DARK_THEME));
    }
}
