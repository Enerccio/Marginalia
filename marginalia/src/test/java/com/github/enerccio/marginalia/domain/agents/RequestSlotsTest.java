package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.AIGate.Permit;
import com.github.enerccio.marginalia.domain.service.agent.RequestSlots;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.State;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.Sub;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The places for the requests of one run, and the registry of its sub-agents; both are plain objects.
 */
class RequestSlotsTest {

    @Test
    void withOnePlaceTheSecondRequestWaitsForTheFirst() throws Exception {
        RequestSlots slots = new RequestSlots(1);

        Permit first = slots.acquire().get();
        CompletableFuture<Permit> second = slots.acquire();

        assertThat(second).isNotDone();
        assertThat(slots.active()).isEqualTo(1);
        assertThat(slots.waiting()).isEqualTo(1);
        first.close();
        first.close();
        assertThat(second).isDone();
        assertThat(slots.active()).as("closing twice gives back once").isEqualTo(1);
        second.get().close();
        assertThat(slots.active()).isZero();
    }

    @Test
    void withTwoPlacesTwoRequestsAreInTheAirAndTheThirdWaits() throws Exception {
        RequestSlots slots = new RequestSlots(2);

        slots.acquire().get();
        slots.acquire().get();
        CompletableFuture<Permit> third = slots.acquire();

        assertThat(third).isNotDone();
        assertThat(slots.cap()).isEqualTo(2);
    }

    @Test
    void aCanceledWaitLeavesTheQueueWithoutTakingAPlace() throws Exception {
        RequestSlots slots = new RequestSlots(1);
        Permit first = slots.acquire().get();
        CompletableFuture<Permit> second = slots.acquire();
        CompletableFuture<Permit> third = slots.acquire();

        second.cancel(true);
        first.close();

        assertThat(third).isDone();
        assertThat(slots.active()).isEqualTo(1);
    }

    @Test
    void aCapBelowOneIsOne() {
        assertThat(new RequestSlots(0).cap()).isEqualTo(1);
    }

    @Test
    void subagentsAreLimitedToTheMostAndAFinishedOneMakesRoom() {
        SubAgents agents = new SubAgents(1);

        Sub first = agents.create("first");
        assertThat(first.handle()).isEqualTo("sub1");
        assertThatThrownBy(() -> agents.create("second")).isInstanceOf(IllegalStateException.class);
        AtomicInteger woken = new AtomicInteger();
        assertThat(agents.whenSlotFree(woken::incrementAndGet)).as("no place now").isFalse();

        agents.finish(first, State.DONE, "answer");
        agents.finish(first, State.FAILED, "the first end wins");

        assertThat(woken).hasValue(1);
        assertThat(first.state()).isEqualTo(State.DONE);
        assertThat(first.result()).isEqualTo("answer");
        assertThat(first.finished()).isDone();
        assertThat(agents.live()).isZero();
        assertThat(agents.create("second").handle()).isEqualTo("sub2");
        assertThat(agents.find(" SUB1 ")).isSameAs(first);
    }
}
