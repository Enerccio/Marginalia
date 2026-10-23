package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeResult;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.agent.HandleTranslator;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Annotations in the work of an agent: they are in the working copy (paired with the notes of the book), the agent can write them,
 * the review offers what it did with them like the other changes, and the sync of a resume brings the notes the user wrote meanwhile.
 */
class AgentAnnotationsTest extends AgentTestBase {

    private static final String DRAGON = "there was a dragon";
    private static final String CASTLE = "and a castle";

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private AgentRunPairService pairService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private CommandScopes commandScopes;

    private User user;
    private Manuscript book;
    private Annotation existing;
    private AgentRun run;

    @BeforeEach
    void startRun() throws Exception {
        user = loginAgentUser();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setDescription("a tale");
        book = manuscriptService.save(manuscript);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse(DRAGON);
        second = chatMessageService.addChild(first, second);
        ChatMessage third = new ChatMessage();
        third.setResponse(CASTLE);
        chatMessageService.addChild(second, third);
        existing = annotationService.create(book, inBook(DRAGON), 10, 16, "which dragon?", AnnotationSource.AUTHOR, "continuity", null);
        run = agentRunService.start(agentService.save(newAgent(book)));
    }

    // --- the copy (what the agent does) and the book (what the user does)

    private Manuscript copy() throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private ChatMessage inCopy(String text) throws Exception {
        return chatMessageService.getAllMessages(copy()).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private ChatMessage inBook(String text) throws Exception {
        return chatMessageService.getAllMessages(manuscriptService.find(book.getId())).stream()
                .filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private Annotation noteInCopy() throws Exception {
        List<Annotation> notes = annotationService.listForManuscript(copy());
        assertThat(notes).as("the notes of the copy").hasSize(1);
        return notes.getFirst();
    }

    private List<Annotation> notesOfBook() throws Exception {
        return annotationService.listForManuscript(manuscriptService.find(book.getId()));
    }

    private void edit(ChatMessage message, String text) throws Exception {
        chatMessageService.storeResponse(manuscriptService.find(message.getParentScript().getId()), chatMessageService.find(message.getId()), text);
    }

    private AgentRun finish() throws Exception {
        AgentRun current = agentRunService.find(run.getId());
        run = current.getStatus() == AgentRunStatus.DONE ? current : agentRunService.transition(current, AgentRunStatus.DONE, "report");
        return run;
    }

    private MergePlan plan() throws Exception {
        finish();
        return mergeService.plan(run);
    }

    private Item item(MergePlan plan, Kind kind, Change change) {
        List<Item> found = plan.items().stream().filter(i -> i.kind() == kind && i.change() == change).toList();
        assertThat(found).as("%s %s in %s", kind, change, plan.items().stream().map(Item::id).toList()).hasSize(1);
        return found.getFirst();
    }

    private Field field(Item item, String name) {
        return item.fields().stream().filter(f -> f.name().equals(name)).findFirst().orElseThrow();
    }

    private MergeResult applyAll(MergePlan plan) throws Exception {
        return mergeService.apply(run, new MergeSelection(plan.items().stream().map(Item::id).collect(Collectors.toSet()), Map.of(), false));
    }

    // --- the copy has the notes

    @Test
    void theNotesOfTheBookAreInTheCopyAndPairedWithThem() throws Exception {
        Annotation copied = noteInCopy();

        assertThat(copied.getText()).isEqualTo("which dragon?");
        assertThat(copied.getStart()).isEqualTo(10);
        List<UuidPair> pairs = pairService.getAll(agentRunService.find(run.getId())).stream()
                .filter(pair -> pair.kind() == AgentRunPairKind.ANNOTATION).toList();
        assertThat(pairs).hasSize(1);
        assertThat(pairs.getFirst().copyUuid()).isEqualTo(copied.getUuid());
        assertThat(pairs.getFirst().originalUuid()).isEqualTo(existing.getUuid());
    }

    @Test
    void anAnnotationHasAHandleOfItsOwnKind() throws Exception {
        String handle = pairService.handleFor(agentRunService.find(run.getId()), AgentRunPairKind.ANNOTATION, noteInCopy().getUuid());

        assertThat(handle).matches("h:a\\d+");
        assertThat(pairService.findCopyUuid(agentRunService.find(run.getId()), handle)).contains(noteInCopy().getUuid());
    }

    @Test
    void aNoteTheAgentMadeGetsAHandleAndTheHandleLeadsBack() throws Exception {
        AgentRun current = agentRunService.find(run.getId());
        HandleTranslator handles = pairService.translator(current, commandScopes.forRun(current));
        Annotation made = annotationService.create(copy(), inCopy(CASTLE), 6, 12, "a castle again?", AnnotationSource.AGENT, null, null);

        String handle = handles.toHandles(made.getUuid());

        assertThat(handle).matches("h:a\\d+");
        assertThat(handles.fromHandles("annotation " + handle)).isEqualTo("annotation " + made.getUuid());
        assertThat(pairService.getAll(current).stream().filter(pair -> pair.kind() == AgentRunPairKind.ANNOTATION && pair.originalUuid() == null))
                .extracting(UuidPair::copyUuid).containsExactly(made.getUuid());
    }

    @Test
    void nothingChangedIsNothingToTakeOver() throws Exception {
        assertThat(plan().isEmpty()).isTrue();
    }

    // --- what the agent does with them

    @Test
    void aNoteTheAgentWroteIsAnAddedItemAndIsMadeInTheBookOnTheSamePassage() throws Exception {
        annotationService.create(copy(), inCopy(CASTLE), 6, 12, "a castle again?", AnnotationSource.AGENT, "style", "agent");

        MergePlan plan = plan();

        Item added = item(plan, Kind.ANNOTATION, Change.ADDED);
        assertThat(added.applicable()).isTrue();
        assertThat(field(added, "text").agent()).isEqualTo("a castle again?");
        assertThat(field(added, "quote").agent()).isEqualTo("castle");
        assertThat(added.label()).startsWith("3: ");
        applyAll(plan);

        Annotation made = notesOfBook().stream().filter(a -> a.getText().equals("a castle again?")).findFirst().orElseThrow();
        assertThat(made.getSource()).isEqualTo(AnnotationSource.AGENT);
        assertThat(made.getType()).isEqualTo("style");
        assertThat(made.getQuote()).isEqualTo("castle");
        assertThat(made.getPartId()).isEqualTo(inBook(CASTLE).getId());
        assertThat(made.getStart()).isEqualTo(6);
        assertThat(notesOfBook()).hasSize(2);
    }

    @Test
    void aNoteTheAgentResolvedIsAStatusChange() throws Exception {
        annotationService.setStatus(copy(), noteInCopy(), AnnotationStatus.RESOLVED);

        MergePlan plan = plan();

        Item changed = item(plan, Kind.ANNOTATION, Change.CHANGED);
        assertThat(changed.fields()).extracting(Field::name).containsExactly("status");
        assertThat(field(changed, "status").book()).isEqualTo("OPEN");
        assertThat(field(changed, "status").agent()).isEqualTo("RESOLVED");
        applyAll(plan);

        assertThat(notesOfBook().getFirst().getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
    }

    @Test
    void aNoteTheAgentRewroteIsAChangeAndTheUserChangingItToo() throws Exception {
        annotationService.update(copy(), noteInCopy(), "the red one?", null, null);
        finish();
        annotationService.update(manuscriptService.find(book.getId()), notesOfBook().getFirst(), "which one?", null, null);

        Item changed = item(mergeService.plan(run), Kind.ANNOTATION, Change.CHANGED);

        assertThat(changed.conflict()).isTrue();
        assertThat(field(changed, "text").agent()).isEqualTo("the red one?");
        assertThat(field(changed, "text").book()).isEqualTo("which one?");
    }

    @Test
    void whatTheUserDidToANoteAloneIsNotAChangeOfTheRun() throws Exception {
        finish();
        annotationService.update(manuscriptService.find(book.getId()), notesOfBook().getFirst(), "which one?", null, null);

        assertThat(mergeService.plan(run).isEmpty()).isTrue();
    }

    @Test
    void aNoteTheAgentDeletedIsDeletedInTheBook() throws Exception {
        annotationService.remove(copy(), noteInCopy());

        MergePlan plan = plan();

        item(plan, Kind.ANNOTATION, Change.DELETED);
        applyAll(plan);

        assertThat(notesOfBook()).isEmpty();
    }

    @Test
    void theNotesOfADeletedPartGoWithItAndAreNotItemsOfTheirOwn() throws Exception {
        ChatMessage dragon = chatMessageService.find(inCopy(DRAGON).getId());
        chatMessageService.deleteNodeAndMigrateChildren(dragon, manuscriptService.find(copy().getId()), false);

        MergePlan plan = plan();

        assertThat(plan.items()).extracting(Item::kind).containsExactly(Kind.MESSAGE);
    }

    @Test
    void aNoteOnAPartTheAgentMadeNeedsThePartAndIsLocatedInIt() throws Exception {
        ChatMessage added = new ChatMessage();
        added.setResponse("The end of the tale.");
        added = chatMessageService.addChild(chatMessageService.find(inCopy(CASTLE).getId()), added);
        annotationService.createFromQuote(copy(), added, "tale", null, null, "weak ending", AnnotationSource.AGENT, null, null);

        MergePlan plan = plan();

        Item note = item(plan, Kind.ANNOTATION, Change.ADDED);
        Item part = item(plan, Kind.MESSAGE, Change.ADDED);
        assertThat(note.dependsOn()).isEqualTo(part.id());
        assertThat(plan.items().indexOf(part)).isLessThan(plan.items().indexOf(note));

        assertThatThrownBy(() -> mergeService.apply(run, new MergeSelection(Set.of(note.id()), Map.of(), false)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Nothing that is picked can be applied");
        assertThat(notesOfBook()).hasSize(1);
    }

    @Test
    void theNoteAndThePartItIsInAreTakenTogether() throws Exception {
        ChatMessage added = new ChatMessage();
        added.setResponse("The end of the tale.");
        added = chatMessageService.addChild(chatMessageService.find(inCopy(CASTLE).getId()), added);
        annotationService.createFromQuote(copy(), added, "tale", null, null, "weak ending", AnnotationSource.AGENT, null, null);

        applyAll(plan());

        Annotation made = notesOfBook().stream().filter(a -> a.getText().equals("weak ending")).findFirst().orElseThrow();
        assertThat(made.hasRange()).isTrue();
        assertThat(made.getQuote()).isEqualTo("tale");
        assertThat(chatMessageService.find(made.getPartId()).getResponse()).isEqualTo("The end of the tale.");
    }

    @Test
    void aNoteOnAPassageTheAgentRewroteIsFoundInTheTextTheBookGets() throws Exception {
        edit(inCopy(CASTLE), "and a grand castle by the sea");
        annotationService.createFromQuote(copy(), inCopy("and a grand castle by the sea"), "castle", null, null, "which castle?",
                AnnotationSource.AGENT, null, null);

        applyAll(plan());

        Annotation made = notesOfBook().stream().filter(a -> a.getText().equals("which castle?")).findFirst().orElseThrow();
        assertThat(made.getQuote()).isEqualTo("castle");
        assertThat(inBook("and a grand castle by the sea").getResponse().substring(made.getStart(), made.getEnd())).isEqualTo("castle");
    }

    @Test
    void aNoteThePartOfWhichTheUserDeletedCantBeTaken() throws Exception {
        annotationService.create(copy(), inCopy(CASTLE), 6, 12, "a castle again?", AnnotationSource.AGENT, null, null);
        finish();
        chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(inBook(CASTLE).getId()), manuscriptService.find(book.getId()), false);

        Item note = item(mergeService.plan(run), Kind.ANNOTATION, Change.ADDED);

        assertThat(note.applicable()).isFalse();
        assertThat(note.problem()).isEqualTo(MergePlan.PROBLEM_NEEDS_PART);
    }

    // --- the sync

    @Test
    void aNoteTheUserWroteWhileTheRunWasPausedComesIntoTheCopy() throws Exception {
        run = agentRunService.pause(agentRunService.find(run.getId()));
        Annotation made = annotationService.create(manuscriptService.find(book.getId()), inBook(CASTLE), 6, 12, "which castle?",
                AnnotationSource.AUTHOR, null, null);

        run = agentRunService.resume(agentRunService.find(run.getId()));

        List<Annotation> notes = annotationService.listForManuscript(copy());
        assertThat(notes).extracting(Annotation::getText).containsExactlyInAnyOrder("which dragon?", "which castle?");
        Annotation inTheCopy = notes.stream().filter(a -> a.getText().equals("which castle?")).findFirst().orElseThrow();
        assertThat(inTheCopy.getStart()).isEqualTo(6);
        assertThat(pairService.getAll(agentRunService.find(run.getId())).stream()
                .anyMatch(pair -> pair.kind() == AgentRunPairKind.ANNOTATION && made.getUuid().equals(pair.originalUuid())
                        && inTheCopy.getUuid().equals(pair.copyUuid()))).isTrue();
        assertThat(plan().isEmpty()).as("copy and book agree").isTrue();
    }

    @Test
    void aNoteTheUserResolvedWhileTheRunWasPausedIsResolvedInTheCopy() throws Exception {
        run = agentRunService.pause(agentRunService.find(run.getId()));
        annotationService.setStatus(manuscriptService.find(book.getId()), notesOfBook().getFirst(), AnnotationStatus.DISMISSED);

        run = agentRunService.resume(agentRunService.find(run.getId()));

        assertThat(noteInCopy().getStatus()).isEqualTo(AnnotationStatus.DISMISSED);
        assertThat(plan().isEmpty()).isTrue();
    }
}
