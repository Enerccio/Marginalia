package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The planner of the book (the Planner tab of the story editor), as commands: {@code /get-planned-scenes},
 * {@code /add-planned-scene}, {@code /update-planned-scene}, {@code /set-planned-scene-done}, {@code /move-planned-scene} and
 * {@code /delete-planned-scene}. A scene is addressed by its number in the list (from 1); the numbers change when a scene is moved
 * or deleted, so a caller reads the list again after changing it. Updating and deleting are destructive, adding, moving and marking
 * a scene done are not.
 */
@Configurable
public class PlannerCommands extends ManuscriptReadSupport {

    static final String GET = "get-planned-scenes";
    static final String ADD = "add-planned-scene";
    static final String UPDATE = "update-planned-scene";
    static final String SET_DONE = "set-planned-scene-done";
    static final String MOVE = "move-planned-scene";
    static final String DELETE = "delete-planned-scene";


    @Autowired
    private PlannerService plannerService;

    @Command(commandName = GET, humanName = L.LABEL_COMMAND_GET_PLANNED_SCENES, readOnly = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_PLANNED_SCENES,
            llmDescription = L.DESC_COMMAND_GET_PLANNED_SCENES_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void getPlannedScenes(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        List<PlannedScene> scenes = plannerService.list(book);
        JsonObject result = new JsonObject();
        JsonArray rows = new JsonArray();
        for (int i = 0; i < scenes.size(); i++) {
            rows.add(row(i + 1, scenes.get(i)));
        }
        result.add("scenes", rows);
        result.addProperty("total", scenes.size());
        result.addProperty("done", scenes.stream().filter(PlannedScene::isDone).count());
        answer(context, result);
    }

    @Command(commandName = ADD, linkedCommand = GET, humanName = L.LABEL_COMMAND_ADD_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_ADD_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void addPlannedScene(CommandContext context) throws Exception {
        addPlannedSceneRun(context);
    }

    @Command(commandName = ADD, linkedCommand = GET, humanName = L.LABEL_COMMAND_ADD_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_ADD_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void addPlannedScene2(CommandContext context) throws Exception {
        addPlannedSceneRun(context);
    }

    @Command(commandName = ADD, linkedCommand = GET, humanName = L.LABEL_COMMAND_ADD_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_ADD_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void addPlannedScene3(CommandContext context) throws Exception {
        addPlannedSceneRun(context);
    }

    @Command(commandName = ADD, linkedCommand = GET, humanName = L.LABEL_COMMAND_ADD_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_ADD_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM),
                    @CommandParameter(name = "povCharacter", type = String.class, description = L.DESC_COMMAND_PLANNER_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void addPlannedScene4(CommandContext context) throws Exception {
        addPlannedSceneRun(context);
    }

    @Command(commandName = ADD, linkedCommand = GET, humanName = L.LABEL_COMMAND_ADD_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_ADD_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM),
                    @CommandParameter(name = "povCharacter", type = String.class, description = L.DESC_COMMAND_PLANNER_POV_PARAM),
                    @CommandParameter(name = "presentCharacters", type = String.class, description = L.DESC_COMMAND_PLANNER_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void addPlannedScene5(CommandContext context) throws Exception {
        addPlannedSceneRun(context);
    }

    private void addPlannedSceneRun(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        PlannedScene added = plannerService.add(book, new PlannedScene(context.get("title", String.class),
                context.get("sceneSetting", String.class), context.get("povCharacter", String.class),
                context.get("presentCharacters", String.class), context.get("instructions", String.class)));
        answer(context, row(plannerService.list(book).size(), added));
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanName = L.LABEL_COMMAND_UPDATE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_UPDATE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void updatePlannedScene(CommandContext context) throws Exception {
        updatePlannedSceneRun(context);
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanName = L.LABEL_COMMAND_UPDATE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_UPDATE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void updatePlannedScene3(CommandContext context) throws Exception {
        updatePlannedSceneRun(context);
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanName = L.LABEL_COMMAND_UPDATE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_UPDATE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void updatePlannedScene4(CommandContext context) throws Exception {
        updatePlannedSceneRun(context);
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanName = L.LABEL_COMMAND_UPDATE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_UPDATE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM),
                    @CommandParameter(name = "povCharacter", type = String.class, description = L.DESC_COMMAND_PLANNER_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void updatePlannedScene5(CommandContext context) throws Exception {
        updatePlannedSceneRun(context);
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanName = L.LABEL_COMMAND_UPDATE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_UPDATE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "title", type = String.class, description = L.DESC_COMMAND_PLANNER_TITLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_PLANNER_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "sceneSetting", type = String.class, description = L.DESC_COMMAND_PLANNER_SETTING_PARAM),
                    @CommandParameter(name = "povCharacter", type = String.class, description = L.DESC_COMMAND_PLANNER_POV_PARAM),
                    @CommandParameter(name = "presentCharacters", type = String.class, description = L.DESC_COMMAND_PLANNER_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void updatePlannedScene6(CommandContext context) throws Exception {
        updatePlannedSceneRun(context);
    }

    private void updatePlannedSceneRun(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        int number = context.get("scene", Integer.class);
        PlannedScene scene = at(book, number);
        scene.setTitle(given(context, "title", scene.getTitle()));
        scene.setInstructions(given(context, "instructions", scene.getInstructions()));
        scene.setSceneSetting(given(context, "sceneSetting", scene.getSceneSetting()));
        scene.setPovCharacter(given(context, "povCharacter", scene.getPovCharacter()));
        scene.setPresentCharacters(given(context, "presentCharacters", scene.getPresentCharacters()));
        plannerService.update(book, scene);
        answer(context, row(number, scene));
    }

    @Command(commandName = SET_DONE, linkedCommand = GET, humanName = L.LABEL_COMMAND_SET_PLANNED_SCENE_DONE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PLANNED_SCENE_DONE,
            llmDescription = L.DESC_COMMAND_SET_PLANNED_SCENE_DONE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void setPlannedSceneDone(CommandContext context) throws Exception {
        setPlannedSceneDoneRun(context);
    }

    @Command(commandName = SET_DONE, linkedCommand = GET, humanName = L.LABEL_COMMAND_SET_PLANNED_SCENE_DONE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PLANNED_SCENE_DONE,
            llmDescription = L.DESC_COMMAND_SET_PLANNED_SCENE_DONE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "done", type = Boolean.class, description = L.DESC_COMMAND_PLANNER_DONE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void setPlannedSceneDone2(CommandContext context) throws Exception {
        setPlannedSceneDoneRun(context);
    }

    private void setPlannedSceneDoneRun(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        int number = context.get("scene", Integer.class);
        PlannedScene scene = at(book, number);
        boolean done = !Boolean.FALSE.equals(context.get("done", Boolean.class));
        plannerService.setDone(book, scene.getUuid(), done);
        scene.setDone(done);
        answer(context, row(number, scene));
    }

    @Command(commandName = MOVE, linkedCommand = GET, humanName = L.LABEL_COMMAND_MOVE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_MOVE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_MOVE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM),
                    @CommandParameter(name = "to", type = Integer.class, description = L.DESC_COMMAND_PLANNER_TO_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void movePlannedScene(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        int number = context.get("scene", Integer.class);
        PlannedScene scene = at(book, number);
        int to = Math.max(1, context.get("to", Integer.class));
        int last = plannerService.list(book).size();
        plannerService.move(book, scene.getUuid(), Math.min(to, last) - number);
        answer(context, row(Math.min(to, last), scene));
    }

    @Command(commandName = DELETE, linkedCommand = GET, humanName = L.LABEL_COMMAND_DELETE_PLANNED_SCENE, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_DELETE_PLANNED_SCENE,
            llmDescription = L.DESC_COMMAND_DELETE_PLANNED_SCENE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = Integer.class, description = L.DESC_COMMAND_PLANNER_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PLANNER_RETURN))
    public void deletePlannedScene(CommandContext context) throws Exception {
        Manuscript book = new Book(context).manuscript;
        PlannedScene scene = at(book, context.get("scene", Integer.class));
        plannerService.delete(book, scene.getUuid());
        JsonObject result = new JsonObject();
        result.addProperty("deleted", scene.getTitle());
        answer(context, result);
    }

    // --- support ---

    /** The scene with the number (from 1), as it is stored now. */
    private PlannedScene at(Manuscript book, Integer number) throws Exception {
        List<PlannedScene> scenes = plannerService.list(book);
        if (number == null || number < 1 || number > scenes.size()) {
            throw new IllegalArgumentException("There is no scene " + number + ", the planner has " + scenes.size() + " scenes");
        }
        return scenes.get(number - 1);
    }

    /** The text of the argument when it was given, else the old one; an empty text clears the old one. */
    private static String given(CommandContext context, String name, String old) {
        String value = context.get(name, String.class);
        return value == null ? old : value;
    }

    private static JsonObject row(int number, PlannedScene scene) {
        JsonObject row = new JsonObject();
        row.addProperty("number", number);
        row.addProperty("title", scene.getTitle());
        row.addProperty("done", scene.isDone());
        row.addProperty("sceneSetting", StringUtils.defaultString(scene.getSceneSetting()));
        row.addProperty("povCharacter", StringUtils.defaultString(scene.getPovCharacter()));
        row.addProperty("presentCharacters", StringUtils.defaultString(scene.getPresentCharacters()));
        row.addProperty("instructions", StringUtils.defaultString(scene.getInstructions()));
        return row;
    }
}
