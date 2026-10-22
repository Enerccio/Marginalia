package com.github.enerccio.marginalia.extensions.arcoutline.names;

import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads what was saved before the tools were chosen in the common tool dialog: the name categories of the Name Generator
 * and the Name Provider with the prefix of their source ({@code Generator: }, {@code Provider: }; none meant the
 * generator, the only extension there was) and the two hints the user wrote. They become a {@link ToolSelection} for the
 * tool suites those extensions register ({@code name-generator} and {@code name-provider}, key {@code names}).
 */
public final class LegacyNameTools {

    private static final String GENERATOR_PREFIX = "Generator: ";
    private static final String PROVIDER_PREFIX = "Provider: ";
    private static final String GENERATOR_REF = ToolSelection.ref("name-generator", "names");
    private static final String PROVIDER_REF = ToolSelection.ref("name-provider", "names");

    private LegacyNameTools() {
    }

    /**
     * @param categories    the saved categories, null when there are none
     * @param generatorHint the user's hint for the Name Generator, null or blank for the default one
     * @param providerHint  the user's hint for the Name Provider, null or blank for the default one
     * @return the selection; null when nothing was saved
     */
    public static ToolSelection migrate(Collection<String> categories, String generatorHint, String providerHint) {
        boolean hints = generatorHint != null && !generatorHint.isBlank() || providerHint != null && !providerHint.isBlank();
        if ((categories == null || categories.isEmpty()) && !hints) {
            return null;
        }
        List<String> generator = new ArrayList<>();
        List<String> provider = new ArrayList<>();
        for (String category : categories == null ? List.<String>of() : categories) {
            if (category.startsWith(PROVIDER_PREFIX)) {
                provider.add(category.substring(PROVIDER_PREFIX.length()));
            } else if (category.startsWith(GENERATOR_PREFIX)) {
                generator.add(category.substring(GENERATOR_PREFIX.length()));
            } else {
                generator.add(category);
            }
        }
        ToolSelection selection = new ToolSelection();
        selection.withItems(GENERATOR_REF, generator).get(GENERATOR_REF).setHint(generatorHint);
        selection.withItems(PROVIDER_REF, provider).get(PROVIDER_REF).setHint(providerHint);
        return selection.compact();
    }

}
