package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.ui.workspace.Workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Session scoped state of the user's session.
 */
public class SessionPoint {

    private volatile Language language;

    private volatile Workspace workspace;

    private final List<LLMChatMessage> helpChat = Collections.synchronizedList(new ArrayList<>());

    private final ProblemLog problems = new ProblemLog();

    /**
     * Language of the UI of this session, {@code null} until one is set (the default language is used then). Set from
     * the settings of the user when the workspace is opened.
     */
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * The workspace of the page the user logged in on, {@code null} before the login completes. It is the way for code
     * that has no UI component at hand (a command, a service) to reach what the user sees: the open part, the open book.
     * A session shared by several browser tabs holds the workspace of the page that logged in last; the workspace of
     * a given page is {@code ApplicationPoint.getUIs().get(ui)}. Touching its components from a thread without the UI
     * lock goes through {@code ui.access(...)}.
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * The conversation of the help assistant in this session, the user's questions and its answers in order (the
     * instructions and the manual tools are added to each request, not kept here). Synchronized: the answers are
     * added from the thread of the request. Keep the list itself (not this bean) in code that runs off the request.
     */
    public List<LLMChatMessage> getHelpChat() {
        return helpChat;
    }

    /**
     * What went wrong for the user in this session, as it was shown to them (errors, inference failures, forms that
     * could not be saved): the help assistant reads it to explain a failure. Keep the log itself (not this bean) in
     * code that runs off the request, see {@link com.github.enerccio.marginalia.utils.UiProblems}.
     */
    public ProblemLog getProblems() {
        return problems;
    }

}
