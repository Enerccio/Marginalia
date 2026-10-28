package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentActivityService;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.agent.LiveAnswerBuffer;
import com.github.enerccio.marginalia.domain.service.impl.AgentActivityServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType.RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The open consoles are told that something happened to the runs of their agent, so they do not have to ask on a timer.
 */
class AgentActivityTest extends AgentTestBase {

    @Autowired
    private AgentActivityService activity;

    @Autowired
    private AgentMessageService messageService;

    private Agent agent;
    private final AtomicInteger heard = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        agent = agent();
        activity.subscribe(agent.getId(), heard::incrementAndGet);
    }

    private void settle() throws Exception {
        ((AgentActivityServiceImpl) AopTestUtils.getUltimateTargetObject(activity)).drain();
    }

    @Test
    void aBurstOfChangesIsOneCall() throws Exception {
        for (int i = 0; i < 50; i++) {
            activity.changed(agent.getId());
        }
        settle();

        assertThat(heard.get()).isEqualTo(1);
    }

    @Test
    void aChangeAfterTheDeliveryIsHeardAgain() throws Exception {
        activity.changed(agent.getId());
        settle();
        activity.changed(agent.getId());
        settle();

        assertThat(heard.get()).isEqualTo(2);
    }

    @Test
    void theStatusOfARunAndItsTranscriptAreAnnounced() throws Exception {
        AgentRun run = agentRunService.start(agent);
        settle();
        int afterStart = heard.get();
        assertThat(afterStart).isPositive();

        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "hello")));
        settle();
        assertThat(heard.get()).isGreaterThan(afterStart);

        int afterMessage = heard.get();
        agentRunService.stop(run, false);
        settle();
        assertThat(heard.get()).isGreaterThan(afterMessage);
    }

    @Test
    void anAgentOnlyHearsItself() throws Exception {
        activity.changed(agent.getId() + 1000);
        settle();

        assertThat(heard.get()).isZero();
    }

    @Test
    void aCancelledSubscriptionHearsNothing() throws Exception {
        AtomicInteger other = new AtomicInteger();
        Runnable cancel = activity.subscribe(agent.getId(), other::incrementAndGet);
        cancel.run();

        activity.changed(agent.getId());
        settle();

        assertThat(other.get()).isZero();
        assertThat(heard.get()).isEqualTo(1);
    }

    @Test
    void theAnswerThatIsWrittenIsAnnounced() throws Exception {
        AtomicInteger changes = new AtomicInteger();
        LiveAnswerBuffer buffer = new LiveAnswerBuffer(changes::incrementAndGet);

        buffer.begin();
        buffer.append(RESPONSE, "Once ");
        buffer.append(RESPONSE, "");
        buffer.end();

        // the begin, the chunk (not the empty one) and the end
        assertThat(changes.get()).isEqualTo(3);
    }
}
