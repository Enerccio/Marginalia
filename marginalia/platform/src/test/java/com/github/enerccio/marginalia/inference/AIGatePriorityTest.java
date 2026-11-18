package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The one exception to the plain queue of an AI: the author's generation goes before what follows the part before it.
 */
class AIGatePriorityTest extends PlatformTestBase {

    @Autowired
    private AIGate gate;

    private CompletableFuture<AIGate.Permit> acquireFor(AI ai, Runs.Run run, List<Integer> positions) {
        try (Runs.Binding _ = run == null ? null : run.bind()) {
            return gate.acquire(ai, positions == null ? null : positions::add);
        }
    }

    @Test
    void theAuthorsGenerationGoesBeforeTheStepsAfterAPartButNotBeforeOtherRequests() throws Exception {
        login();
        AI ai = createAI();
        Runs.Run phase = Runs.run(Runs.AFTER_PART, () -> "After the part").start();
        Runs.Run translation = Runs.run("reallang-translator", () -> "Translating").partOf(phase).start();
        Runs.Run other = Runs.run("summaries", () -> "Summaries").start();
        Runs.Run generation = Runs.run(Runs.GENERATION, () -> "Generating").start();

        AIGate.Permit busy = gate.acquire(ai).get(5, TimeUnit.SECONDS);
        CompletableFuture<AIGate.Permit> summary = acquireFor(ai, other, null);
        CompletableFuture<AIGate.Permit> annotation = acquireFor(ai, phase, null);
        List<Integer> translationPlace = new CopyOnWriteArrayList<>();
        CompletableFuture<AIGate.Permit> translated = acquireFor(ai, translation, translationPlace);
        List<Integer> generationPlace = new CopyOnWriteArrayList<>();
        CompletableFuture<AIGate.Permit> next = acquireFor(ai, generation, generationPlace);

        assertThat(generationPlace).as("after the summary, which came first and is not a step after the part").containsExactly(2);
        assertThat(translationPlace).as("the generation went before it").containsExactly(3, 4);

        busy.close();
        summary.get(5, TimeUnit.SECONDS).close();
        AIGate.Permit generating = next.get(5, TimeUnit.SECONDS);
        assertThat(annotation).isNotDone();
        generating.close();
        annotation.get(5, TimeUnit.SECONDS).close();
        translated.get(5, TimeUnit.SECONDS).close();
        assertThat(gate.active(ai)).isZero();
    }
}
