package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The agent that takes the lore notes of a book after an import: it reads the story and writes what it finds (characters, places,
 * things, terms) as entries of the lorebook of the book. It is an ordinary agent of the book ({@link Defaults#AGENT_LORE_NOTES_NAME},
 * the model of the book, the commands that read the story and write lorebook entries, nothing that deletes), started at once, so the
 * author follows it in its console and reviews its result as any agent's.
 */
@Configurable
public final class LoreNotesAgent {

    /**
     * The commands the agent may call: the ones that read the story and the lorebooks and the ones that write entries, none that
     * changes the story or deletes.
     */
    static final List<String> COMMANDS = List.of(
            "get-book-info", "list-parts", "get-part", "search-parts", "find-occurrences", "list-summaries", "get-summary", "count-tokens",
            "list-lorebooks", "list-lore-entries", "get-lore-entry", "search-lore",
            "create-lorebook", "set-lorebook", "create-lore-entry", "update-lore-entry", "set-lore-entry-tags");

    private static final String MAIN_ROLE = "overseer";

    /**
     * Why the agent can't be started.
     */
    public enum Problem {
        /** The user may not use agents. */
        NOT_ALLOWED,
        /** The book has no model or no protocol. */
        NO_MODEL,
        /** The book has an agent already (a book can have only one). */
        HAS_AGENT
    }

    @Autowired
    private Localization loc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentRunner agentRunner;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    /**
     * @return what stops the agent from being started for the book, null when it can be
     */
    public Problem check(Manuscript manuscript) throws Exception {
        if (!agentService.isAllowed()) {
            return Problem.NOT_ALLOWED;
        }
        if (manuscript.getAi() == null || manuscript.getProtocol() == null) {
            return Problem.NO_MODEL;
        }
        return agentService.findForManuscript(manuscript) == null ? null : Problem.HAS_AGENT;
    }

    /**
     * The name of the agent the book has, for the text that says why another can't be made.
     */
    public String existingAgentName(Manuscript manuscript) throws Exception {
        Agent agent = agentService.findForManuscript(manuscript);
        return agent == null ? "" : agent.getName();
    }

    /**
     * Makes the agent of the book (and a lorebook for the book when it has none, the agent writes its entries into the lorebook of
     * the book) and starts its run.
     *
     * @return the agent, with a run that is working
     * @throws IllegalStateException when {@link #check} says the agent can't be started
     */
    public Agent start(Manuscript manuscript) throws Exception {
        Problem problem = check(manuscript);
        if (problem != null) {
            throw new IllegalStateException("The lore notes agent can't be started: " + problem);
        }
        Manuscript book = manuscriptService.find(manuscript);
        if (book.getLorebook() == null) {
            Lorebook lorebook = new Lorebook();
            lorebook.setName(book.getName());
            lorebook.setEnabled(true);
            Lorebook made = lorebookService.save(lorebook);
            book = manuscriptService.update(book, fresh -> fresh.setLorebook(made));
        }

        Agent agent = new Agent();
        // the instructions are in the prompt language of the user, the agent reads its commands in the same one when it can
        Language promptLanguage = loc.getPromptLanguage();
        if (!Language.EN.equals(promptLanguage) && loc.getAvailableLanguages().contains(promptLanguage)) {
            agent.setLanguage(promptLanguage.code());
        }
        agent.setName(loc.getDefaults().agentLoreNotesName());
        agent.setManuscript(book);
        agent.setModelProfiles(List.of(new AgentModelProfile(MAIN_ROLE, aiService.find(book.getAi()).getUuid(), protocolService.find(book.getProtocol()).getUuid())));
        agent.setInstructions(loc.getDefaults().agentLoreNotesInstructions());
        agent.setAllowedCommands(COMMANDS);
        agent = agentService.save(agent);

        // the run is started and handed to the runner, which works it in the background
        agentRunner.run(agentRunService.start(agent));
        return agent;
    }
}
