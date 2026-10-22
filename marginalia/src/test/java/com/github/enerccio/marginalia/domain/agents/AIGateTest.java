package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AIGate.Permit;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The cap of requests of agents at one AI: the ones above it wait in turn, a canceled wait costs nobody a place.
 */
class AIGateTest extends MarginaliaTestBase {

    @Autowired
    private AIGate gate;

    private AI ai;

    @BeforeEach
    void anAI() throws Exception {
        login();
        ai = createAI();
    }

    private void cap(Integer cap) throws Exception {
        AI stored = aiService.find(ai);
        stored.setMaxConcurrentRequests(cap);
        ai = aiService.save(stored);
    }

    @Test
    void anAIWithoutACapLetsEveryoneIn() throws Exception {
        CompletableFuture<Permit> first = gate.acquire(ai);
        CompletableFuture<Permit> second = gate.acquire(ai);
        CompletableFuture<Permit> third = gate.acquire(ai);

        assertThat(first).isCompleted();
        assertThat(second).isCompleted();
        assertThat(third).isCompleted();
        assertThat(gate.active(ai)).isEqualTo(3);
        assertThat(gate.waiting(ai)).isZero();
        first.get().close();
        second.get().close();
        third.get().close();
        assertThat(gate.active(ai)).isZero();
    }

    @Test
    void theRequestsAboveTheCapWaitAndGoInTheOrderTheyCame() throws Exception {
        cap(1);

        CompletableFuture<Permit> first = gate.acquire(ai);
        CompletableFuture<Permit> second = gate.acquire(ai);
        CompletableFuture<Permit> third = gate.acquire(ai);

        assertThat(first).isCompleted();
        assertThat(second).isNotDone();
        assertThat(third).isNotDone();
        assertThat(gate.waiting(ai)).isEqualTo(2);

        first.get().close();
        assertThat(second).isCompleted();
        assertThat(third).isNotDone();
        assertThat(gate.active(ai)).isEqualTo(1);

        second.get().close();
        assertThat(third).isCompleted();
        third.get().close();
        assertThat(gate.active(ai)).isZero();
    }

    @Test
    void aCapOfTwoGivesTwoPlaces() throws Exception {
        cap(2);

        CompletableFuture<Permit> first = gate.acquire(ai);
        CompletableFuture<Permit> second = gate.acquire(ai);
        CompletableFuture<Permit> third = gate.acquire(ai);

        assertThat(first).isCompleted();
        assertThat(second).isCompleted();
        assertThat(third).isNotDone();
        second.get().close();
        assertThat(third).isCompleted();
        first.get().close();
        third.get().close();
    }

    @Test
    void closingAPermitTwiceFreesOnlyOnePlace() throws Exception {
        cap(1);
        Permit first = gate.acquire(ai).get();
        CompletableFuture<Permit> second = gate.acquire(ai);

        first.close();
        first.close();

        assertThat(second).isCompleted();
        assertThat(gate.active(ai)).isEqualTo(1);
        CompletableFuture<Permit> third = gate.acquire(ai);
        assertThat(third).as("the place of the second is not given again").isNotDone();
        second.get().close();
        third.get().close();
    }

    @Test
    void aWaitThatWasCanceledTakesNoPlaceAndNoOneBehindItWaitsForIt() throws Exception {
        cap(1);
        CompletableFuture<Permit> first = gate.acquire(ai);
        CompletableFuture<Permit> second = gate.acquire(ai);
        CompletableFuture<Permit> third = gate.acquire(ai);

        second.cancel(true);
        assertThat(gate.waiting(ai)).isEqualTo(1);

        first.get().close();
        assertThat(third).isCompleted();
        assertThat(gate.active(ai)).isEqualTo(1);
        third.get().close();
        assertThat(gate.active(ai)).isZero();
    }

    @Test
    void theCapIsReadAtEveryRequest() throws Exception {
        cap(1);
        CompletableFuture<Permit> first = gate.acquire(ai);
        CompletableFuture<Permit> second = gate.acquire(ai);
        assertThat(second).isNotDone();

        cap(3);
        CompletableFuture<Permit> third = gate.acquire(ai);
        assertThat(third).as("behind the one that waits").isNotDone();
        first.get().close();
        assertThat(second).isCompleted();
        assertThat(third).isCompleted();
        second.get().close();
        third.get().close();
    }

    @Test
    void theAIsDoNotShareTheirPlaces() throws Exception {
        cap(1);
        AI other = createAI();
        AI stored = aiService.find(other);
        stored.setMaxConcurrentRequests(1);
        other = aiService.save(stored);

        CompletableFuture<Permit> mine = gate.acquire(ai);
        CompletableFuture<Permit> theirs = gate.acquire(other);

        assertThat(mine).isCompleted();
        assertThat(theirs).isCompleted();
        mine.get().close();
        theirs.get().close();
    }
}
