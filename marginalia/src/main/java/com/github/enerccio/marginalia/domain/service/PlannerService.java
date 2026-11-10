package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.List;

/**
 * The planner of a book: the scenes the author plans to write, in the order they want them, each with the turn details (scene
 * setting, POV character, present characters, instructions) the new turn form takes and a flag for "written". The author fills it
 * by hand in the Planner tab of the story editor; a model can add to it too (the plot wizard of the Arc Outline plugin).
 * <p>
 * The scenes are stored with the book (extended attribute {@code plan}), so they are in its backups and copies. Scenes are
 * addressed by their uuid. Every change reads the book again and saves it, so a change made while another one is
 * in progress is not lost, and tells the listeners ({@link #onChange}).
 */
public interface PlannerService {

    /** The most scenes a book can have. */
    int MAX_SCENES = 500;

    /** The longest title. */
    int MAX_TITLE_LENGTH = 200;

    /** The longest title {@link #importBranch} makes of the instructions of a part. */
    int IMPORTED_TITLE_LENGTH = 80;

    /** The longest text of a scene setting, character list or instructions. */
    int MAX_TEXT_LENGTH = 20_000;

    /**
     * The scenes of the book as they are stored now, in order. The objects are copies: changing them changes nothing.
     */
    List<PlannedScene> list(Manuscript book) throws Exception;

    /**
     * Adds the scene at the end. It gets a new uuid and is not done, whatever the argument says.
     *
     * @return the scene as it was stored
     * @throws IllegalArgumentException when the title is blank or a text is too long
     * @throws IllegalStateException    when the book has {@link #MAX_SCENES} scenes already
     */
    PlannedScene add(Manuscript book, PlannedScene scene) throws Exception;

    /**
     * Adds the scene after the scene with the uuid {@code afterUuid}, like {@link #add}.
     *
     * @return the scene as it was stored
     * @throws IllegalArgumentException when the title is blank, a text is too long or the book has no scene {@code afterUuid}
     * @throws IllegalStateException    when the book has {@link #MAX_SCENES} scenes already
     */
    PlannedScene insert(Manuscript book, String afterUuid, PlannedScene scene) throws Exception;

    /**
     * Makes one scene of two: the scene {@code firstUuid} gets the title and the turn details of {@code merged} (it keeps its
     * uuid and its place) and the scene {@code secondUuid} is removed. The scene is done when both were.
     *
     * @return the scene as it was stored
     * @throws IllegalArgumentException when the title is blank, a text is too long, the book lacks one of the scenes or they
     *                                  are the same scene
     */
    PlannedScene merge(Manuscript book, String firstUuid, String secondUuid, PlannedScene merged) throws Exception;

    /**
     * Adds a scene at the end for every part of the branch the book is on (from the first part to the active leaf) that has
     * turn details: the scene setting, POV character, present characters and instructions of the part, with the first line of
     * its instructions as the title. Parts without turn details are left out; the scenes are not done. Nothing is added when
     * the planner would have more than {@link #MAX_SCENES} scenes.
     *
     * @return the number of scenes added
     * @throws IllegalStateException when the scenes do not fit into the planner
     */
    int importBranch(Manuscript book) throws Exception;

    /**
     * Replaces the title, the turn details and the done flag of the scene with the uuid of the argument; its place stays.
     *
     * @throws IllegalArgumentException when the title is blank, a text is too long or the book has no such scene
     */
    void update(Manuscript book, PlannedScene scene) throws Exception;

    /**
     * @throws IllegalArgumentException when the book has no such scene
     */
    void setDone(Manuscript book, String uuid, boolean done) throws Exception;

    /**
     * Removes the scene; a scene that is not there is ignored.
     */
    void delete(Manuscript book, String uuid) throws Exception;

    /**
     * Moves the scene up (negative) or down (positive) by that many places, to the first or last place at most.
     *
     * @throws IllegalArgumentException when the book has no such scene
     */
    void move(Manuscript book, String uuid, int by) throws Exception;

    /**
     * Tells {@code listener} whenever the scenes of the book are changed through this service, on the thread that changed them. For a
     * view that keeps the list on the screen.
     *
     * @return what removes the listener
     */
    Runnable onChange(Manuscript book, Runnable listener);
}
