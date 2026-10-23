package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.utils.CommandUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class CommandScopesImpl implements CommandScopes {

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    @Lazy
    private LorebookService lorebookService;

    @Autowired
    @Lazy
    private LorebookEntryService lorebookEntryService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private AgentService agentService;

    @Autowired
    @Lazy
    private AgentRunPairService agentRunPairService;

    @Autowired
    @Lazy
    private AnnotationService annotationService;

    @Override
    public CommandScope session() {
        return new SessionScope();
    }

    @Override
    public CommandScope forRun(AgentRun run) {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return new RunScope(run);
    }

    /**
     * What both scopes do the same way: find the lorebooks of the book and keep to them.
     */
    private abstract class BookScope implements CommandScope {

        /** The book as it is stored now; null when there is none. */
        abstract Manuscript book() throws Exception;

        @Override
        public Manuscript getManuscript() throws Exception {
            return book();
        }

        @Override
        public Lorebook getLorebook() throws Exception {
            Manuscript book = book();
            Lorebook root = book == null || book.getLorebook() == null ? null : lorebookService.find(book.getLorebook());
            return root == null || root.isDeleted() ? null : root;
        }

        @Override
        public ChatMessage resolveMessage(String uuid) throws Exception {
            Manuscript book = book();
            Long id = uuid == null || book == null ? null : chatMessageService.find(uuid);
            ChatMessage message = id == null ? null : chatMessageService.find(id);
            if (message == null || message.isDeleted() || message.getParentScript() == null
                    || !message.getParentScript().getId().equals(book.getId())) {
                throw new IllegalArgumentException("The part does not exist");
            }
            return message;
        }

        @Override
        public Lorebook resolveLorebook(String uuid) throws Exception {
            Long id = uuid == null ? null : lorebookService.find(uuid);
            Lorebook lorebook = id == null ? null : lorebookService.find(id);
            if (lorebook == null || lorebook.isDeleted() || !tree().contains(lorebook.getId())) {
                throw new IllegalArgumentException("The lorebook does not exist");
            }
            return lorebook;
        }

        @Override
        public LorebookEntry resolveEntry(String uuid) throws Exception {
            Long id = uuid == null ? null : lorebookEntryService.find(uuid);
            LorebookEntry entry = id == null ? null : lorebookEntryService.find(id);
            if (entry == null || entry.isDeleted() || entry.getLorebook() == null || !tree().contains(entry.getLorebook().getId())) {
                throw new IllegalArgumentException("The lorebook entry does not exist");
            }
            return entry;
        }

        @Override
        public Annotation resolveAnnotation(String uuid) throws Exception {
            Manuscript book = book();
            Annotation annotation = uuid == null || book == null ? null : annotationService.findForUser(uuid);
            ChatMessage part = annotation == null || annotation.getPartId() == null ? null : chatMessageService.find(annotation.getPartId());
            if (annotation == null || annotation.isDeleted() || part == null || part.isDeleted() || part.getParentScript() == null
                    || !part.getParentScript().getId().equals(book.getId())) {
                throw new IllegalArgumentException("The annotation does not exist");
            }
            return annotation;
        }

        /**
         * The ids of the lorebooks of the book: the root and the subbooks of it, however deep.
         */
        private Set<Long> tree() throws Exception {
            Set<Long> ids = new HashSet<>();
            Deque<Lorebook> queue = new ArrayDeque<>();
            Lorebook root = getLorebook();
            if (root != null) {
                queue.add(root);
            }
            while (!queue.isEmpty()) {
                Lorebook next = queue.remove();
                if (ids.add(next.getId())) {
                    queue.addAll(lorebookService.getSubbooks(next));
                }
            }
            return ids;
        }
    }

    /**
     * The user at the command bar: the book they have open, asking them by a dialog.
     */
    private final class SessionScope extends BookScope {

        @Override
        Manuscript book() throws Exception {
            Manuscript open = CommandUtils.getCurrentManuscript();
            return open == null ? null : manuscriptService.findForUser(open.getUuid());
        }

        @Override
        public AgentRun getRun() {
            return null;
        }

        @Override
        public boolean confirm(CommandContext context, String question) throws InterruptedException {
            return CommandUtils.confirm(context, question);
        }
    }

    /**
     * An agent: the working copy of its run, whichever it is now.
     */
    private final class RunScope extends BookScope {

        private final AgentRun run;

        RunScope(AgentRun run) {
            this.run = run;
        }

        @Override
        Manuscript book() throws Exception {
            AgentRun current = agentRunService.find(run.getId());
            return current == null || current.getWorkingCopy() == null ? null : manuscriptService.find(current.getWorkingCopy());
        }

        @Override
        public AgentRun getRun() {
            return run;
        }

        @Override
        public Lorebook resolveUnusedLorebook(String uuid) throws Exception {
            Long id = uuid == null ? null : lorebookService.find(uuid);
            Lorebook lorebook = id == null ? null : lorebookService.find(id);
            AgentRun current = agentRunService.find(run.getId());
            // the cage of a lorebook that the book does not use: one of this copy (the run has a handle for it) and of this user
            if (lorebook == null || current == null || !lorebook.isAgentic() || lorebook.getOwner() == null || current.getOwner() == null
                    || !lorebook.getOwner().getId().equals(current.getOwner().getId())
                    || agentRunPairService.findHandle(current, uuid).isEmpty()) {
                throw new IllegalArgumentException("The lorebook does not exist");
            }
            return lorebook;
        }

        @Override
        public ModelChoice getModel() throws Exception {
            AgentRun current = agentRunService.find(run.getId());
            Agent agent = current == null ? null : agentService.findForUser(current.getAgent().getUuid());
            if (agent != null && agent.isUsingAgentModel()) {
                AgentService.ResolvedProfile resolved = agent.getMainProfile() == null ? null : agentService.resolveProfile(agent.getMainProfile());
                if (resolved != null) {
                    return new ModelChoice(resolved.ai(), resolved.protocol());
                }
            }
            Manuscript book = book();
            return book == null || book.getAi() == null || book.getAi().isDeleted() ? null
                    : new ModelChoice(book.getAi(), book.getProtocol() != null && !book.getProtocol().isDeleted() ? book.getProtocol() : null);
        }

        @Override
        public boolean confirm(CommandContext context, String question) {
            // what the agent may do without asking was decided when it called the command
            return !context.isTerminated();
        }
    }
}
