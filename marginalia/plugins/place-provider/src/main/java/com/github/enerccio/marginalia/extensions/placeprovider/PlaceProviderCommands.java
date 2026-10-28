package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.Gson;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The places as commands, for the command bar and for agents: {@code list-place-types}, {@code search-places},
 * {@code places-near} and {@code distance-between}. The same as the functions {@link PlaceProviderFunctions} give a model that
 * is asked directly; a wrong question (an unknown type or country) fails the command with the message of the index. An answer is a JSON object with a place on each line, so one that is too long for an agent is cut
 * between two places.
 */
public class PlaceProviderCommands {

    public static final String TYPES = "list-place-types";
    public static final String SEARCH = "search-places";
    public static final String NEAR = "places-near";
    public static final String DISTANCE = "distance-between";

    private static final Gson GSON = new Gson();

    private static final String SEARCH_TEXT = "Searches real-world places (cities, countries, seas, bays, rivers, mountains...) by name, type and "
            + "country. Reads only.";
    private static final String SEARCH_LLM = "Searches real-world places of the Earth (Natural Earth data: cities, capitals, provinces, countries, "
            + "seas, bays, gulfs, straits, fjords, lakes, rivers, islands, capes, mountains, mountain ranges, deserts, ports, airports, parks) by "
            + "name, type and country. Give * as the query to leave the name out; at least one of query, type and country has to be given. "
            + "Returns {total, places} with the best matches first: name, type, lat, lon, the countries they touch and more. It is a map of the "
            + "notable places, not every one. Use list-place-types for the types. Reads only.";
    private static final String NEAR_TEXT = "Lists the places around a place or a point, the closest first. Reads only.";
    private static final String NEAR_LLM = "Lists the real-world places within a radius of a place or a point, the closest first, with the "
            + "distance in kilometers and the compass direction from it. Returns {origin, total, places}. Reads only.";
    private static final String DISTANCE_TEXT = "The distance in a straight line between two places or points. Reads only.";
    private static final String DISTANCE_LLM = "The great-circle distance (a straight line over the surface of the Earth, not a road) in kilometers "
            + "and miles between two real-world places or points, with the direction. The answer names the places that the names were taken "
            + "for. Returns {from, to, km, miles, direction}. Reads only.";

    private static final String PARAM_QUERY = "Part of a name, * for any name";
    private static final String PARAM_TYPE = "A type from list-place-types, or what it is called ('inlets', 'mountains', 'towns')";
    private static final String PARAM_COUNTRY = "A country by name or code ('Canada', 'CA', 'CAN')";
    private static final String PARAM_LIMIT = "How many places to return, 1 to " + PlaceIndex.MAX_LIMIT + ", default " + PlaceIndex.DEFAULT_LIMIT;
    private static final String PARAM_PLACE = "A place by name ('Ottawa', 'Paris, France') or coordinates ('45.42, -75.69')";
    private static final String RESULT = "JSON object";

    private final PlaceIndex index;

    public PlaceProviderCommands(PlaceIndex index) {
        this.index = index;
    }

    @Command(commandName = TYPES, linkedCommand = {SEARCH, NEAR}, humanNameText = "List place types", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Lists the types of places that search-places accepts, with how many there are of each. Reads only.",
            llmDescriptionText = "Lists the types of real-world places that search-places and places-near accept (city, capital, bay, inlet, "
                    + "river, mountain range, island...) with how many places of each there are. Returns a JSON array of {type, count}. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "types", type = String.class, descriptionText = "JSON array of {type, count}"))
    public void listTypes(CommandContext context) {
        context.setReturn(lines(index.types()));
    }

    @Command(commandName = SEARCH, linkedCommand = {TYPES, NEAR}, humanNameText = "Search places", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SEARCH_TEXT, llmDescriptionText = SEARCH_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, descriptionText = PARAM_QUERY)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void search(CommandContext context) {
        search(context, null, null, null);
    }

    @Command(commandName = SEARCH, linkedCommand = {TYPES, NEAR}, humanNameText = "Search places", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SEARCH_TEXT, llmDescriptionText = SEARCH_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, descriptionText = PARAM_QUERY),
                    @CommandParameter(name = "type", type = String.class, optional = true, descriptionText = PARAM_TYPE)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void searchType(CommandContext context) {
        search(context, context.get("type", String.class), null, null);
    }

    @Command(commandName = SEARCH, linkedCommand = {TYPES, NEAR}, humanNameText = "Search places", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SEARCH_TEXT, llmDescriptionText = SEARCH_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, descriptionText = PARAM_QUERY),
                    @CommandParameter(name = "type", type = String.class, optional = true, descriptionText = PARAM_TYPE),
                    @CommandParameter(name = "country", type = String.class, optional = true, descriptionText = PARAM_COUNTRY)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void searchCountry(CommandContext context) {
        search(context, context.get("type", String.class), context.get("country", String.class), null);
    }

    @Command(commandName = SEARCH, linkedCommand = {TYPES, NEAR}, humanNameText = "Search places", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SEARCH_TEXT, llmDescriptionText = SEARCH_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, descriptionText = PARAM_QUERY),
                    @CommandParameter(name = "type", type = String.class, optional = true, descriptionText = PARAM_TYPE),
                    @CommandParameter(name = "country", type = String.class, optional = true, descriptionText = PARAM_COUNTRY),
                    @CommandParameter(name = "limit", type = Integer.class, optional = true, descriptionText = PARAM_LIMIT,
                            min = "1", max = "" + PlaceIndex.MAX_LIMIT)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void searchLimit(CommandContext context) {
        search(context, context.get("type", String.class), context.get("country", String.class), context.get("limit", Integer.class));
    }

    private void search(CommandContext context, String type, String country, Integer limit) {
        PlaceSearch result = index.search(context.get("query", String.class), type, country, limit);
        context.setReturn("{\"total\":" + result.total() + ",\"places\":" + lines(result.places()) + "}");
    }

    @Command(commandName = NEAR, linkedCommand = {TYPES, SEARCH}, humanNameText = "Places near", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = NEAR_TEXT, llmDescriptionText = NEAR_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "place", type = String.class, descriptionText = PARAM_PLACE),
                    @CommandParameter(name = "radius_km", type = Double.class, descriptionText = "How far around it to look, in kilometers")}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void near(CommandContext context) {
        near(context, null, null);
    }

    @Command(commandName = NEAR, linkedCommand = {TYPES, SEARCH}, humanNameText = "Places near", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = NEAR_TEXT, llmDescriptionText = NEAR_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "place", type = String.class, descriptionText = PARAM_PLACE),
                    @CommandParameter(name = "radius_km", type = Double.class, descriptionText = "How far around it to look, in kilometers"),
                    @CommandParameter(name = "type", type = String.class, optional = true, descriptionText = PARAM_TYPE)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void nearType(CommandContext context) {
        near(context, context.get("type", String.class), null);
    }

    @Command(commandName = NEAR, linkedCommand = {TYPES, SEARCH}, humanNameText = "Places near", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = NEAR_TEXT, llmDescriptionText = NEAR_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "place", type = String.class, descriptionText = PARAM_PLACE),
                    @CommandParameter(name = "radius_km", type = Double.class, descriptionText = "How far around it to look, in kilometers"),
                    @CommandParameter(name = "type", type = String.class, optional = true, descriptionText = PARAM_TYPE),
                    @CommandParameter(name = "limit", type = Integer.class, optional = true, descriptionText = PARAM_LIMIT,
                            min = "1", max = "" + PlaceIndex.MAX_LIMIT)}),
            returns = @CommandReturn(name = "places", type = String.class, descriptionText = RESULT))
    public void nearLimit(CommandContext context) {
        near(context, context.get("type", String.class), context.get("limit", Integer.class));
    }

    private void near(CommandContext context, String type, Integer limit) {
        Double radius = context.get("radius_km", Double.class);
        Nearby result = index.near(context.get("place", String.class), radius == null ? 0 : radius, type, limit);
        context.setReturn("{\"origin\":" + GSON.toJson(result.origin()) + ",\"total\":" + result.total() + ",\"places\":"
                + lines(result.places()) + "}");
    }

    @Command(commandName = DISTANCE, humanNameText = "Distance between places", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = DISTANCE_TEXT, llmDescriptionText = DISTANCE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, descriptionText = PARAM_PLACE),
                    @CommandParameter(name = "to", type = String.class, descriptionText = PARAM_PLACE)}),
            returns = @CommandReturn(name = "distance", type = String.class, descriptionText = RESULT))
    public void distance(CommandContext context) {
        context.setReturn(GSON.toJson(index.distance(context.get("from", String.class), context.get("to", String.class))));
    }

    /** A JSON array with an element on each line. */
    private static String lines(List<?> elements) {
        return elements.isEmpty() ? "[]" : "[\n" + elements.stream().map(GSON::toJson).collect(Collectors.joining(",\n")) + "\n]";
    }

}
