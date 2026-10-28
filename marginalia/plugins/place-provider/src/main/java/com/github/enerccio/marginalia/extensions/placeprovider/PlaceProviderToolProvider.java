package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.tool.ToolProvider;
import com.github.enerccio.marginalia.domain.service.tool.ToolRequest;

import java.util.Collection;

import static com.github.enerccio.marginalia.extensions.placeprovider.PlaceProviderLocalization.T;

/**
 * The place provider as a tool suite that is on or off (it has no items). The same suite is offered as the commands
 * {@link PlaceProviderCommands}.
 */
public class PlaceProviderToolProvider implements ToolProvider {

    /** Added to the prompt when the model gets the functions; the user can replace it where they choose the tools. */
    static final String HINT = """
[ Place tools: you can call list_place_types to see the kinds of real places, search_places with a name, a type and/or a country to find real places of the Earth (cities, countries, seas, bays, rivers, mountains...) with their coordinates and the countries they are in, places_near to see what lies around a place, and distance_between to get the distance in a straight line between two places. They know the notable places of the real world, not every village or creek. When the text names a real place, look it up instead of guessing where it is, how far it is or what is around it. ]
""";

    private final PlaceIndex index;
    private final FunctionRegistry registry;

    public PlaceProviderToolProvider(PlaceIndex index, FunctionRegistry registry) {
        this.index = index;
        this.registry = registry;
    }

    @Override
    public FunctionSet functions(Collection<String> items, ToolRequest request) {
        return registry.bind(PlaceProviderFunctions.class, new PlaceProviderFunctionsImpl(index));
    }

    @Override
    public String defaultHint() {
        return HINT.strip();
    }

    @Override
    public String description() {
        return T.localized("Real places of the Earth (Natural Earth, about 18 000 notable ones): search by name, type and country, what lies around a place, the distance of two places.");
    }

    @Override
    public Object commands() {
        return new PlaceProviderCommands(index);
    }

}
