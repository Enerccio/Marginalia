---
label: After the part
order: 65
verified: f3fad883f
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/generation/AfterPart.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/AfterPartPhase.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/story/AutoAnnotateSteps.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/story/AutoAnnotate.java
---

# After the part

What a book does with a part once it was generated (notes, a redraft, a translation, the knowledge, audio) is the **second phase of
the generation**: when a generation of the author completed (from the page or the command bar; never for an agent, which does these
steps by its own commands), the engine goes through the **stages** of `AfterPart` on the server, with no page needed, and every step
bound to a stage runs in its turn. A plugin binds a step instead of decorating `AutoAnnotate.finished` (deprecated since 1.8.0).

```java
@Autowired
private StoryGenerationService storyGenerationService;
private Registration afterPart;

// onExtensionLoad
afterPart = storyGenerationService.addAfterPartBinding(new AfterPart.Binding(KEY + ".translate",
        () -> T.localized(TRANSLATE), AfterPart.Stage.TRANSFORM, AfterPart.Mode.SEQUENTIAL, new AfterPart.Handler() {
    @Override
    public boolean appliesTo(Manuscript book) throws Exception {
        return settings(book).isAuto();               // the automatic switch of the book
    }

    @Override
    public void run(AfterPart.Context context) throws Exception {
        RunProgress progress = new RunProgress();
        context.onStop(progress::stop);
        Outcome outcome = translate(context.getManuscript(), context.getPart(), progress);   // blocks until it is done
        if (outcome.wroteSomething()) {
            context.changed();                        // the page that generated shows the part again
        }
    }
}));

// onExtensionUnload
afterPart.unregister();
```

## Stages and modes

| Stage | For | Bound in the application |
|---|---|---|
| `REVIEW` | notes on the new text | the automatic annotation (`AutoAnnotateSteps`) |
| `REWRITE` | the text is written again; **a new generation of the book is refused while it runs** | the automatic redraft (`AutoAnnotateSteps`) |
| `TRANSFORM` | passages of the final text are replaced | Real Language and Conlang translators (sequential) |
| `EXTRACT` | data is read out of the final text | Knowledge Tracker (parallel) |
| `MEDIA` | what is made of the final text | Audiobook (parallel) |

The stages run in this order, so what changes the text comes before what reads it. Inside a stage the `SEQUENTIAL` steps run one after
another in the order they were bound, each seeing what the one before left; then the `PARALLEL` ones start together and the stage ends
when all are done. A parallel step must not change the text of the part (not enforced). On a model with one connection parallel steps
take turns anyway: every request waits for a connection of its AI.

## The handler

`run(context)` is plain blocking code on a worker thread, in the request scope of the user who generated, with the run of the phase
bound: the requests it makes to a model show in the panel of the runs, and a run of the registry it starts (`PartRuns.run`, an
`ExtractionRun`) is a part of the phase's entry (its text goes there, the phase's Stop stops it). `appliesTo(book)` says whether the
book uses the step (its switch); a step that does not apply is not shown. The context gives:

| | |
|---|---|
| `getManuscript()`, `getPart()` | the book and the part **as stored when the step starts**, with what the steps before did |
| `getRequest()`, `getAi()`, `getProtocol()`, `getInput()` | what the generation was: a new part, a swipe or a regeneration, its model, the turn |
| `getProperties()` | a copy of the generation's properties, shared by the steps of the phase (prefix your keys) |
| `onStop(action)`, `isStopped()` | stop your work when the phase is stopped (the author's Stop, a failure of another step, a regeneration of the part) |
| `await(future)` | waits for asynchronous work; throws `AfterPart.Stopped` when the phase is stopped meanwhile (the future is cancelled) |
| `changed()` | the part changed: the page that generated shows it again |
| `skip()` | nothing to do (no speech to translate): the panel does not tick the step |
| `waitForAuthor(open)` | the step waits for a decision: the panel says so and its **Review** runs `open` in the page of the click; `null` ends it |
| `stop()` | stops the phase as the Stop of the panel (the author closed the window of the step without taking its result) |
| `accessUI(action)` | runs in the page that generated, false when it is gone: for a notification there |

Throw to fail. **A failure is a Stop**: the phase ends where it is, the other steps of a parallel stage are stopped, what was applied
stays, and the author is told the step and the steps that did not run (`onWarning` of the generation's listener). There is **no
resume**: after a Stop the author does the rest by hand, so every binding needs a manual action for one part (and a command for an
agent). Do not call `Notification` from the handler: there is no page on its thread; use `accessUI`.

## The phase as a run

The phase is one run of the registry, `Runs.AFTER_PART` (*After part 14: Annotate ✓ · Redraft … · Translate*): it holds the part in
`STRUCTURE` until the end of `TRANSFORM` (no delete, no move; the translators' `WRITE` runs are allowed), then in `READ`; it
`setBlocksGeneration(true)` during `REWRITE`. Its end has the outcome (`FINISHED`, `STOPPED`, `FAILED`). A regeneration of the part
stops it. The deprecated hook `AutoAnnotate.finished` is still called, in the page that generated, when the text-changing stages are
over (the listener's `onPartFinal`), not when they were stopped or failed.

See [Generation pipeline → After the part](../generation-pipeline.md#after-the-part) and
[the runs](../services.md#the-runs-and-what-they-hold).
