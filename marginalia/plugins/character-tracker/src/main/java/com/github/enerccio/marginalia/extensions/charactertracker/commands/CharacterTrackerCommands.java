package com.github.enerccio.marginalia.extensions.charactertracker.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterBook;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterRelation;
import com.github.enerccio.marginalia.extensions.charactertracker.model.RelationDirection;
import com.github.enerccio.marginalia.extensions.charactertracker.model.StoryCharacter;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * The characters of the book as commands, for the command bar and for agents: {@code list-characters},
 * {@code get-character}, {@code create-character}, {@code update-character}, {@code delete-character} and for the
 * relationships {@code list-character-relations}, {@code add-character-relation}, {@code update-character-relation},
 * {@code delete-character-relation}. They do what the Characters tab does (but for the icon), on the book of the scope of
 * the command (the open book, or the working copy of an agent), and the open tab shows the change at once.
 * <p>
 * A character is addressed by its <b>name</b> (the case does not matter), or by its <b>number</b> in
 * {@code list-characters} (the order of the names) when two characters have the same name or the name is a number. A
 * relationship is addressed by its number in {@code list-character-relations}. No uuid is given out or accepted. A command
 * that changes a character takes only the fields it is given ({@code null} leaves a field as it is, an empty text clears
 * a tagline or a note). Reading, adding and changing is {@code NON_DESTRUCTIVE}; deleting is {@code DESTRUCTIVE} and asks
 * the user first.
 */
public class CharacterTrackerCommands extends ManuscriptReadSupport {

    public static final String LIST = "list-characters";
    public static final String GET = "get-character";
    public static final String CREATE = "create-character";
    public static final String UPDATE = "update-character";
    public static final String DELETE = "delete-character";
    public static final String LIST_RELATIONS = "list-character-relations";
    public static final String ADD_RELATION = "add-character-relation";
    public static final String UPDATE_RELATION = "update-character-relation";
    public static final String DELETE_RELATION = "delete-character-relation";

    private static final String LIST_TEXT = "Lists the characters of the book with their tagline and the number of their relationships. Reads only.";
    private static final String LIST_LLM = "Lists the characters of the book (the cast the author keeps track of) ordered by name, with the tagline, "
            + "the color, whether there is a note and how many relationships each has, as JSON. Characters are addressed by name in the other "
            + "commands. get-character returns the note. Reads only.";
    private static final String GET_TEXT = "Returns a character with its note and its relationships. Reads only.";
    private static final String GET_LLM = "Returns one character of the book: name, tagline, the full note, color and every relationship it is in "
            + "(with whom, in which direction, what it is called). Reads only.";
    private static final String CREATE_TEXT = "Adds a character to the book.";
    private static final String CREATE_LLM = "Adds a character to the book's cast and returns it. The tagline is a few words shown under the name, "
            + "the note is free text for the author. Add the relationships with add-character-relation.";
    private static final String UPDATE_TEXT = "Changes the name, tagline, note or color of a character.";
    private static final String UPDATE_LLM = "Changes a character. Pass only the fields to change, the others stay as they are; an empty tagline or "
            + "note clears it. Returns the character.";
    private static final String DELETE_TEXT = "Deletes a character and all its relationships.";
    private static final String DELETE_LLM = "Deletes a character with all its relationships. Destructive.";
    private static final String LIST_RELATIONS_TEXT = "Lists the relationships between the characters. Reads only.";
    private static final String LIST_RELATIONS_LLM = "Lists the relationships between the characters of the book as JSON: number, the two characters, "
            + "whether it goes one way (from the source to the target, like \"employs\") or both ways (like \"friends\"), its name and tagline. "
            + "Pass a character to see only its relationships. Reads only.";
    private static final String ADD_RELATION_TEXT = "Adds a relationship between two characters.";
    private static final String ADD_RELATION_LLM = "Adds a relationship between two characters of the book and returns it. By default it goes one way, "
            + "from the source to the target (\"employs\", \"is the mother of\"); pass bidirectional true for one in which they are equal "
            + "(\"friends\", \"rivals\").";
    private static final String UPDATE_RELATION_TEXT = "Changes the name or tagline of a relationship.";
    private static final String UPDATE_RELATION_LLM = "Changes a relationship by its number in list-character-relations. Pass only the fields to "
            + "change; an empty tagline clears it. Returns the relationship.";
    private static final String DELETE_RELATION_TEXT = "Deletes a relationship.";
    private static final String DELETE_RELATION_LLM = "Deletes a relationship by its number in list-character-relations. Destructive, the relationships "
            + "after it get lower numbers.";

    private static final String PARAM_CHARACTER = "Name of the character (the case does not matter), or its number in list-characters";
    private static final String PARAM_NAME = "The name";
    private static final String PARAM_TAGLINE = "A few words shown under the name";
    private static final String PARAM_NOTE = "Free text for the author";
    private static final String PARAM_COLOR = "Background color of the character's card as #rrggbb";
    private static final String PARAM_RELATION = "Number of the relationship in list-character-relations, the first is 1";
    private static final String RESULT_CHARACTER = "JSON: the character as it is now";
    private static final String RESULT_RELATION = "JSON: the relationship as it is now";

    private final CharacterTrackerService service;

    public CharacterTrackerCommands(CharacterTrackerService service) {
        this.service = service;
    }

    // --- list-characters / get-character ---

    @Command(commandName = LIST, linkedCommand = GET, humanNameText = "List characters", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the characters"))
    public void list(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        JsonArray list = new JsonArray();
        int number = 1;
        for (StoryCharacter character : characters.sortedCharacters()) {
            JsonObject row = new JsonObject();
            row.addProperty("number", number++);
            row.addProperty("name", character.getName());
            row.addProperty("tagline", character.getTagline());
            row.addProperty("color", CharacterTrackerService.safeColor(character.getColor()));
            row.addProperty("hasNote", StringUtils.isNotBlank(character.getNote()));
            row.addProperty("relationships", characters.relationsOf(character).size());
            list.add(row);
        }
        JsonObject result = new JsonObject();
        result.add("characters", list);
        if (list.isEmpty()) {
            result.addProperty("hint", "The book has no characters yet, create-character adds one");
        }
        answer(context, result);
    }

    @Command(commandName = GET, linkedCommand = LIST, humanNameText = "Get character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = GET_TEXT, llmDescriptionText = GET_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void get(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        StoryCharacter character = character(characters, context.get("character", String.class));
        answer(context, characterRow(characters, character));
    }

    // --- create-character ---

    @Command(commandName = CREATE, humanNameText = "Create character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void create(CommandContext context) throws Exception {
        runCreate(context);
    }

    @Command(commandName = CREATE, humanNameText = "Create character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void createTagline(CommandContext context) throws Exception {
        runCreate(context);
    }

    @Command(commandName = CREATE, humanNameText = "Create character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void createNote(CommandContext context) throws Exception {
        runCreate(context);
    }

    @Command(commandName = CREATE, humanNameText = "Create character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "color", type = String.class, optional = true, descriptionText = PARAM_COLOR)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void createColor(CommandContext context) throws Exception {
        runCreate(context);
    }

    private void runCreate(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        StoryCharacter character = new StoryCharacter();
        character.setName(requiredName(context.get("name", String.class)));
        character.setTagline(StringUtils.trimToEmpty(context.get("tagline", String.class)));
        character.setNote(StringUtils.defaultString(context.get("note", String.class)).strip());
        String color = context.get("color", String.class);
        if (color != null) {
            character.setColor(color(color));
        }
        characters.put(character);
        store(book, characters);
        answer(context, characterRow(characters, character));
    }

    // --- update-character ---

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void update(CommandContext context) throws Exception {
        runUpdate(context);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateTagline(CommandContext context) throws Exception {
        runUpdate(context);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateNote(CommandContext context) throws Exception {
        runUpdate(context);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update character", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "color", type = String.class, optional = true, descriptionText = PARAM_COLOR)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateColor(CommandContext context) throws Exception {
        runUpdate(context);
    }

    private void runUpdate(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        StoryCharacter character = character(characters, context.get("character", String.class));
        String name = context.get("name", String.class);
        String tagline = context.get("tagline", String.class);
        String note = context.get("note", String.class);
        String color = context.get("color", String.class);
        if (name == null && tagline == null && note == null && color == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name, the tagline, the note or the color");
        }
        if (name != null) {
            character.setName(requiredName(name));
        }
        if (tagline != null) {
            character.setTagline(tagline.strip());
        }
        if (note != null) {
            character.setNote(note.strip());
        }
        if (color != null) {
            character.setColor(color(color));
        }
        characters.put(character);
        store(book, characters);
        answer(context, characterRow(characters, character));
    }

    // --- delete-character ---

    @Command(commandName = DELETE, linkedCommand = LIST, humanNameText = "Delete character", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = DELETE_TEXT, llmDescriptionText = DELETE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void delete(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        StoryCharacter character = character(characters, context.get("character", String.class));
        int relations = characters.relationsOf(character).size();
        if (!context.confirm("Delete the character " + character.getName() + " and all its relationships?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        characters.delete(character);
        store(book, characters);
        context.setReturn("Deleted the character " + character.getName()
                + (relations > 0 ? " and " + relations + " relationship(s)" : ""));
    }

    // --- relationships ---

    @Command(commandName = LIST_RELATIONS, linkedCommand = LIST, humanNameText = "List character relationships", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_RELATIONS_TEXT, llmDescriptionText = LIST_RELATIONS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the relationships"))
    public void listRelations(CommandContext context) throws Exception {
        runListRelations(context);
    }

    @Command(commandName = LIST_RELATIONS, linkedCommand = LIST, humanNameText = "List character relationships", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_RELATIONS_TEXT, llmDescriptionText = LIST_RELATIONS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, optional = true,
                            descriptionText = "Only the relationships of this character (name or number); all when omitted")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the relationships"))
    public void listRelationsOf(CommandContext context) throws Exception {
        runListRelations(context);
    }

    private void runListRelations(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        String filter = context.get("character", String.class);
        StoryCharacter only = filter == null ? null : character(characters, filter);
        JsonArray list = new JsonArray();
        int number = 0;
        for (CharacterRelation relation : characters.allRelations()) {
            number++;
            if (only == null || relation.involves(only.getUuid())) {
                list.add(relationRow(characters, number, relation));
            }
        }
        JsonObject result = new JsonObject();
        result.add("relationships", list);
        answer(context, result);
    }

    @Command(commandName = ADD_RELATION, linkedCommand = LIST, humanNameText = "Add character relationship", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_RELATION_TEXT, llmDescriptionText = ADD_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The character the relationship starts at (name or number)"),
                    @CommandParameter(name = "target", type = String.class, descriptionText = "The character it points to (name or number)"),
                    @CommandParameter(name = "name", type = String.class, descriptionText = "What the relationship is called, like \"employs\" or \"friends\"")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_RELATION))
    public void addRelation(CommandContext context) throws Exception {
        runAddRelation(context);
    }

    @Command(commandName = ADD_RELATION, linkedCommand = LIST, humanNameText = "Add character relationship", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_RELATION_TEXT, llmDescriptionText = ADD_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The character the relationship starts at (name or number)"),
                    @CommandParameter(name = "target", type = String.class, descriptionText = "The character it points to (name or number)"),
                    @CommandParameter(name = "name", type = String.class, descriptionText = "What the relationship is called, like \"employs\" or \"friends\""),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_RELATION))
    public void addRelationTagline(CommandContext context) throws Exception {
        runAddRelation(context);
    }

    @Command(commandName = ADD_RELATION, linkedCommand = LIST, humanNameText = "Add character relationship", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_RELATION_TEXT, llmDescriptionText = ADD_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The character the relationship starts at (name or number)"),
                    @CommandParameter(name = "target", type = String.class, descriptionText = "The character it points to (name or number)"),
                    @CommandParameter(name = "name", type = String.class, descriptionText = "What the relationship is called, like \"employs\" or \"friends\""),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE),
                    @CommandParameter(name = "bidirectional", type = Boolean.class, optional = true, defaultValue = "false",
                            descriptionText = "true when the two are equal in it (friends); false when it goes from the source to the target")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_RELATION))
    public void addRelationBoth(CommandContext context) throws Exception {
        runAddRelation(context);
    }

    private void runAddRelation(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        StoryCharacter source = character(characters, context.get("source", String.class));
        StoryCharacter target = character(characters, context.get("target", String.class));
        if (source == target) {
            throw new IllegalArgumentException("A character can't be in a relationship with itself");
        }
        String name = context.get("name", String.class);
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name of the relationship is required");
        }
        CharacterRelation relation = new CharacterRelation();
        relation.setSourceUuid(source.getUuid());
        relation.setTargetUuid(target.getUuid());
        relation.setName(name.strip());
        relation.setTagline(StringUtils.trimToEmpty(context.get("tagline", String.class)));
        relation.setDirection(Boolean.TRUE.equals(context.get("bidirectional", Boolean.class))
                ? RelationDirection.UNIDIRECTIONAL : RelationDirection.SOURCE_TARGET);
        characters.put(relation);
        store(book, characters);
        answer(context, relationRow(characters, characters.allRelations().indexOf(relation) + 1, relation));
    }

    @Command(commandName = UPDATE_RELATION, linkedCommand = LIST_RELATIONS, humanNameText = "Update character relationship", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_RELATION_TEXT, llmDescriptionText = UPDATE_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "relationship", type = Integer.class, descriptionText = PARAM_RELATION),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "What the relationship is called")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_RELATION))
    public void updateRelation(CommandContext context) throws Exception {
        runUpdateRelation(context);
    }

    @Command(commandName = UPDATE_RELATION, linkedCommand = LIST_RELATIONS, humanNameText = "Update character relationship", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_RELATION_TEXT, llmDescriptionText = UPDATE_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "relationship", type = Integer.class, descriptionText = PARAM_RELATION),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "What the relationship is called"),
                    @CommandParameter(name = "tagline", type = String.class, optional = true, descriptionText = PARAM_TAGLINE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_RELATION))
    public void updateRelationTagline(CommandContext context) throws Exception {
        runUpdateRelation(context);
    }

    private void runUpdateRelation(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        Integer number = context.get("relationship", Integer.class);
        CharacterRelation relation = relation(characters, number);
        String name = context.get("name", String.class);
        String tagline = context.get("tagline", String.class);
        if (name == null && tagline == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name or the tagline");
        }
        if (name != null) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("The name of the relationship is required");
            }
            relation.setName(name.strip());
        }
        if (tagline != null) {
            relation.setTagline(tagline.strip());
        }
        store(book, characters);
        answer(context, relationRow(characters, number, relation));
    }

    @Command(commandName = DELETE_RELATION, linkedCommand = LIST_RELATIONS, humanNameText = "Delete character relationship", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = DELETE_RELATION_TEXT, llmDescriptionText = DELETE_RELATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "relationship", type = Integer.class, descriptionText = PARAM_RELATION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void deleteRelation(CommandContext context) throws Exception {
        Book book = new Book(context);
        CharacterBook characters = service.read(book.manuscript);
        CharacterRelation relation = relation(characters, context.get("relationship", Integer.class));
        String described = describe(characters, relation);
        if (!context.confirm("Delete the relationship " + described + "?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        characters.delete(relation);
        store(book, characters);
        context.setReturn("Deleted the relationship " + described);
    }

    // --- helpers ---

    /** Stores the characters in the book and lets the open views of the book read them again. */
    private void store(Book book, CharacterBook characters) throws Exception {
        service.save(book.manuscript, characters);
        service.changed(book.manuscript);
    }

    /** The character with that name (the case does not matter) or, when the name is a number no character has, with that number. */
    private static StoryCharacter character(CharacterBook characters, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the name of the character");
        }
        String wanted = reference.strip();
        List<StoryCharacter> sorted = characters.sortedCharacters();
        List<StoryCharacter> named = sorted.stream().filter(c -> wanted.equalsIgnoreCase(StringUtils.strip(c.getName()))).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        if (named.size() > 1) {
            StringBuilder numbers = new StringBuilder();
            for (StoryCharacter c : named) {
                numbers.append(numbers.isEmpty() ? "" : ", ").append(sorted.indexOf(c) + 1);
            }
            throw new IllegalArgumentException("There are " + named.size() + " characters called " + wanted
                    + ", use their number instead (" + numbers + ", see list-characters)");
        }
        if (wanted.matches("\\d{1,6}")) {
            int number = Integer.parseInt(wanted);
            if (number >= 1 && number <= sorted.size()) {
                return sorted.get(number - 1);
            }
        }
        throw new IllegalArgumentException(sorted.isEmpty() ? "The book has no characters yet"
                : "There is no character " + wanted + ". Characters: " + String.join(", ", sorted.stream().map(StoryCharacter::getName).toList()));
    }

    private static CharacterRelation relation(CharacterBook characters, Integer number) {
        List<CharacterRelation> all = characters.allRelations();
        if (number == null || number < 1 || number > all.size()) {
            throw new IllegalArgumentException(all.isEmpty() ? "The book has no relationships yet"
                    : "There is no relationship " + number + ", there are " + all.size() + " (1 to " + all.size() + ")");
        }
        return all.get(number - 1);
    }

    private static String requiredName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name is required");
        }
        return name.strip();
    }

    private static String color(String color) {
        if (!CharacterTrackerService.isColor(color.strip())) {
            throw new IllegalArgumentException("The color is #rrggbb, like #e0e0e0");
        }
        return color.strip().toLowerCase();
    }

    private static JsonObject characterRow(CharacterBook characters, StoryCharacter character) {
        JsonObject row = new JsonObject();
        row.addProperty("number", characters.sortedCharacters().indexOf(character) + 1);
        row.addProperty("name", character.getName());
        row.addProperty("tagline", character.getTagline());
        row.addProperty("note", character.getNote());
        row.addProperty("color", CharacterTrackerService.safeColor(character.getColor()));
        JsonArray relations = new JsonArray();
        List<CharacterRelation> all = characters.allRelations();
        for (CharacterRelation relation : characters.relationsOf(character)) {
            JsonObject item = relationRow(characters, all.indexOf(relation) + 1, relation);
            item.addProperty("with", characters.get(relation.otherEnd(character.getUuid())).getName());
            item.addProperty("direction", relation.getDirection() == RelationDirection.UNIDIRECTIONAL ? "both"
                    : character.getUuid().equals(relation.getSourceUuid()) ? "outgoing" : "incoming");
            relations.add(item);
        }
        row.add("relationships", relations);
        return row;
    }

    private static JsonObject relationRow(CharacterBook characters, int number, CharacterRelation relation) {
        JsonObject row = new JsonObject();
        row.addProperty("number", number);
        row.addProperty("source", characters.get(relation.getSourceUuid()).getName());
        row.addProperty("target", characters.get(relation.getTargetUuid()).getName());
        row.addProperty("bidirectional", relation.getDirection() == RelationDirection.UNIDIRECTIONAL);
        row.addProperty("name", relation.getName());
        row.addProperty("tagline", relation.getTagline());
        return row;
    }

    private static String describe(CharacterBook characters, CharacterRelation relation) {
        return characters.get(relation.getSourceUuid()).getName()
                + (relation.getDirection() == RelationDirection.UNIDIRECTIONAL ? " <-> " : " -> ")
                + characters.get(relation.getTargetUuid()).getName() + " (" + relation.getName() + ")";
    }

}
