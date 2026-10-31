package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * The settings of the plugin that books and folders inherit (see {@code InheritedSettingRegistry}): the prompts of the two kinds of
 * chat, the medium and the AI and the protocol of the chats. Their user level is the settings of the user
 * ({@link BetaReadersSettings}), their last level the built-in text (or, for the AI and the protocol, the ones of the book).
 */
public final class BetaReadersInherited {

    public static final String SINGLE_PROMPT = "betareaders.singlePrompt";
    public static final String GROUP_PROMPT = "betareaders.groupPrompt";
    public static final String MEDIUM = "betareaders.medium";
    public static final String AI = "betareaders.ai";
    public static final String PROTOCOL = "betareaders.protocol";

    private BetaReadersInherited() {
    }

    /**
     * Registers the five settings.
     *
     * @param owner     the symbolic name of the bundle
     * @param settings  the settings of the user to read the user level from; it must not write (it is asked when a setting is resolved)
     * @param ais       the AIs to choose from, the uuid of an AI is what is stored
     * @param protocols the protocols to choose from, the uuid of a protocol is what is stored
     */
    public static void register(InheritedSettingRegistry registry, String owner, Supplier<BetaReadersSettings> settings,
                                Supplier<List<Choice>> ais, Supplier<List<Choice>> protocols) {
        registry.register(owner, InheritedSetting.textArea(SINGLE_PROMPT, () -> T.localized("Prompt of a chat with one persona"))
                .group(() -> T.localized("Beta Readers"))
                .help(() -> T.localized("The system prompt of a 1v1 chat"))
                .userValue(() -> settings.get().getSinglePrompt())
                .fallback(() -> BetaReadersDefaults.SINGLE_PROMPT));
        registry.register(owner, InheritedSetting.textArea(GROUP_PROMPT, () -> T.localized("Prompt of a group chat"))
                .group(() -> T.localized("Beta Readers"))
                .help(() -> T.localized("The system prompt of a chat with a group of personas"))
                .userValue(() -> settings.get().getGroupPrompt())
                .fallback(() -> BetaReadersDefaults.GROUP_PROMPT));
        registry.register(owner, InheritedSetting.choice(MEDIUM, () -> T.localized("Medium"),
                        () -> settings.get().getMediums().stream().map(BetaReadersInherited::choice).toList())
                .group(() -> T.localized("Beta Readers"))
                .help(() -> T.localized("What the text stands for: the personas react to it as the audience of that medium"))
                .userValue(() -> settings.get().getMedium())
                .fallback(() -> BetaReadersDefaults.DEFAULT_MEDIUM));
        registry.register(owner, InheritedSetting.choice(AI, () -> T.localized("Model of the chats"), ais)
                .group(() -> T.localized("Beta Readers"))
                .help(() -> T.localized("Empty: the model of the book"))
                .userValue(() -> settings.get().getAi()));
        registry.register(owner, InheritedSetting.choice(PROTOCOL, () -> T.localized("Protocol of the chats"), protocols)
                .group(() -> T.localized("Beta Readers"))
                .help(() -> T.localized("Empty: the protocol of the book"))
                .userValue(() -> settings.get().getProtocol()));
    }

    private static Choice choice(Medium medium) {
        return new Choice(medium.getId(), medium.getName());
    }
}
