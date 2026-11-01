package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * The settings of the plugin that books and folders inherit (see {@code InheritedSettingRegistry}): the prompts of the two kinds of
 * chat, the scene, the medium and the AI and the protocol of the chats. Their user level is the settings of the user
 * ({@link BetaReadersSettings}), their last level the built-in text (or, for the AI and the protocol, the ones of the book).
 */
public final class BetaReadersInherited {

    public static final String SINGLE_PROMPT = "betareaders.singlePrompt";
    public static final String GROUP_PROMPT = "betareaders.groupPrompt";
    public static final String SCENE = "betareaders.scene";
    public static final String MEDIUM = "betareaders.medium";
    public static final String AI = "betareaders.ai";
    public static final String PROTOCOL = "betareaders.protocol";

    private BetaReadersInherited() {
    }

    /**
     * Registers the six settings.
     *
     * @param owner     the symbolic name of the bundle
     * @param settings  the settings of the user to read the user level from; it must not write (it is asked when a setting is resolved)
     * @param ais       the AIs to choose from, the uuid of an AI is what is stored
     * @param protocols the protocols to choose from, the uuid of a protocol is what is stored
     */
    public static void register(InheritedSettingRegistry registry, String owner, Supplier<BetaReadersSettings> settings,
                                Supplier<List<Choice>> ais, Supplier<List<Choice>> protocols) {
        registry.register(owner, InheritedSetting.textArea(SINGLE_PROMPT, () -> T.localized(PROMPT_OF_A_CHAT))
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(THE_SYSTEM_PROMPT))
                .userValue(() -> settings.get().getSinglePrompt())
                .fallback(() -> BetaReadersDefaults.SINGLE_PROMPT));
        registry.register(owner, InheritedSetting.textArea(GROUP_PROMPT, () -> T.localized(PROMPT_OF_A_GROUP_CHAT))
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(THE_SYSTEM_PROMPT_OF_A_CHAT))
                .userValue(() -> settings.get().getGroupPrompt())
                .fallback(() -> BetaReadersDefaults.GROUP_PROMPT));
        registry.register(owner, InheritedSetting.textArea(SCENE, () -> T.localized(BetaReadersLocalization.SCENE))
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(WHERE_AND_HOW_THE_CONVERSATION))
                .userValue(() -> settings.get().getScene())
                .fallback(() -> BetaReadersDefaults.SCENE));
        registry.register(owner, InheritedSetting.choice(MEDIUM, () -> T.localized(BetaReadersLocalization.MEDIUM),
                        () -> settings.get().getMediums().stream().map(BetaReadersInherited::choice).toList())
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(WHAT_THE_TEXT_STANDS))
                .userValue(() -> settings.get().getMedium())
                .fallback(() -> BetaReadersDefaults.DEFAULT_MEDIUM));
        registry.register(owner, InheritedSetting.choice(AI, () -> T.localized(MODEL_OF_THE_CHATS), ais)
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(EMPTY_THE_MODEL))
                .userValue(() -> settings.get().getAi()));
        registry.register(owner, InheritedSetting.choice(PROTOCOL, () -> T.localized(PROTOCOL_OF_THE_CHATS), protocols)
                .group(() -> T.localized(BETA_READERS))
                .help(() -> T.localized(EMPTY_THE_PROTOCOL))
                .userValue(() -> settings.get().getProtocol()));
    }

    private static Choice choice(Medium medium) {
        return new Choice(medium.getId(), medium.getName());
    }
}
