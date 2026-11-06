package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A soak test (the one the evaluation keeps asking for): <b>two users</b> work at the same time, each on a <b>long book</b> (the book of the dataset, 100 parts, the first
 * one grown to about 400 parts), with <b>generations in flight</b> (the mock model streams slowly, so they overlap with everything else) on SQLite, the way the application runs.
 * While the generations run, each user's other thread takes backups of the whole book with its history, edits parts and reads the statistics.
 * <p>
 * It fails on any exception on any thread, on a generation that did not complete, on a part that is missing or in the wrong place after it, and on a user who sees the
 * book of the other. The times are printed, nothing but a very generous limit is asserted: the machines differ. Not covered yet: agents.
 * <p>
 * Takes about a minute (two restores of the book, 300 parts, six generations of a prompt of up to 200k characters, the backups).
 */
class LargeWorksetSoakTest extends GenerationTestBase {

    private static final int EXTRA_PARTS = 300;
    private static final int GENERATIONS_PER_USER = 3;
    private static final int BACKUPS_PER_USER = 6;

    @Autowired
    private BackupService backupService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private ManuscriptStatisticService statisticService;

    /** One user's side: the user, the AI and protocol of theirs, the book. */
    private record Side(User user, Manuscript book, int partsBefore) {
    }

    /** Runs the work as the user, in a session of its own on the thread (the session scoped current user is a proxy that resolves it by the thread). */
    private <T> T asUser(User user, Callable<T> work) throws Exception {
        RequestAttributes before = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        try {
            loginAs(user);
            return work.call();
        } finally {
            if (before == null) {
                RequestContextHolder.resetRequestAttributes();
            } else {
                RequestContextHolder.setRequestAttributes(before);
            }
        }
    }

    private Side prepare(User user, int extraParts) throws Exception {
        return asUser(user, () -> {
            OpenAICompatible ai = createAI();
            // the whole window of a big model, so the long book makes a long prompt
            ai.setMaxContext(200_000);
            ai.setMaxCompletionTokens(4_000);
            ai = (OpenAICompatible) aiService.save(ai);
            ChatCompletionProtocol p = new ChatCompletionProtocol();
            p.setName(uniqueName("soak protocol"));
            p.setProtocolType(ProtocolType.CHAT_COMPLETION);
            p.setMaxTokens(200_000);
            p.setReplyTokens(4_000);
            Protocol protocol = protocolService.save(p);

            Manuscript book = WorksetFixture.restore(backupService, lorebookService, uniqueName("Soak"), true);
            book.setAi(ai);
            book.setProtocol(protocol);
            book = manuscriptService.save(book);
            ChatMessage last = manuscriptService.find(book.getId()).getActiveLeaf();
            for (int i = 1; i <= extraParts; i++) {
                last = chatMessageService.createPart(book, last, "Part " + i + " of the soak. The reed-door stayed shut and the water kept its count. "
                        + "Tomáš wrote the figure down, Mizuki read it twice, and neither of them said what it was.", null);
            }
            Manuscript stored = manuscriptService.find(book.getId());
            return new Side(user, stored, mainLine(stored).size());
        });
    }

    private List<ChatMessage> mainLine(Manuscript book) throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        ChatMessage part = manuscriptService.find(book.getId()).getActiveLeaf();
        while (part != null) {
            parts.add(part);
            part = chatMessageService.getParent(part);
        }
        return parts.reversed();
    }

    /** What a user does while their generations run: backups of the whole book with its history, an edit of an old part, a look at the statistics. */
    private void otherWork(Side side, AtomicInteger done) throws Exception {
        asUser(side.user(), () -> {
            for (int round = 0; round < BACKUPS_PER_USER; round++) {
                assertThat(manuscriptService.createBackup(manuscriptService.find(side.book().getId()), true).getAsJsonArray("messages").size()).isGreaterThanOrEqualTo(side.partsBefore());
                List<ChatMessage> parts = mainLine(side.book());
                ChatMessage old = parts.get(10 + round);
                chatMessageService.updateResponse(manuscriptService.find(side.book().getId()), chatMessageService.find(old.getId()), old.getResponse() + " (edited " + round + ")");
                statisticService.findRange(com.github.enerccio.marginalia.domain.components.StatisticFilter.of(java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now(),
                        com.github.enerccio.marginalia.domain.collections.StatisticSide.ALL).forBook(side.book().getUuid()));
                done.incrementAndGet();
            }
            return null;
        });
    }

    private void generations(Side side, List<Long> millis) throws Exception {
        asUser(side.user(), () -> {
            for (int i = 0; i < GENERATIONS_PER_USER; i++) {
                long start = System.nanoTime();
                GenerationRun run = new GenerationRun().answering(true);
                storyGenerationService.generateNextTurn(manuscriptService.find(side.book().getId()), new TurnInput(null, null, null, "Continue the story, part " + (i + 1) + "."),
                        GenerationRequest.newMessage(), run);
                GenerationRun.Outcome outcome = run.await(Duration.ofSeconds(180));
                assertThat(outcome).as("generation %d of %s", i + 1, side.user().getLogin()).isEqualTo(GenerationRun.Outcome.COMPLETED);
                assertThat(run.getErrors()).isEmpty();
                assertThat(run.getSimpleErrors()).isEmpty();
                synchronized (millis) {
                    millis.add((System.nanoTime() - start) / 1_000_000);
                }
            }
            return null;
        });
    }

    @Test
    void twoUsersWorkOnLongBooksWhileGenerationsRun() throws Exception {
        User first = userService.findByName(currentUser.getLogin());
        User second = createUser(uniqueName("soak-second"), DEFAULT_PASSWORD, false);

        long start = System.nanoTime();
        Side a = prepare(first, EXTRA_PARTS);
        Side b = prepare(second, 0);
        long prepared = (System.nanoTime() - start) / 1_000_000;
        System.out.println("[soak] prepared in " + prepared + " ms: " + a.user().getLogin() + " has " + a.partsBefore() + " parts on the main line, " + b.user().getLogin() + " has " + b.partsBefore());
        assertThat(a.partsBefore()).isEqualTo(100 + EXTRA_PARTS);
        assertThat(b.partsBefore()).isEqualTo(100);

        // slow chunks: a generation takes about two seconds, the other work of both users goes on meanwhile
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            chunks.add("The ledger held its breath, word " + i + ". ");
        }
        llm.fallback(MockLLMResponse.chunks(chunks.toArray(String[]::new)).chunkDelay(Duration.ofMillis(50)));

        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Long> generationMillis = new ArrayList<>();
        AtomicInteger rounds = new AtomicInteger();
        try {
            CountDownLatch go = new CountDownLatch(1);
            List<Future<?>> work = new ArrayList<>();
            for (Side side : List.of(a, b)) {
                work.add(pool.submit(() -> {
                    go.await();
                    generations(side, generationMillis);
                    return null;
                }));
                work.add(pool.submit(() -> {
                    go.await();
                    otherWork(side, rounds);
                    return null;
                }));
            }
            long soak = System.nanoTime();
            go.countDown();
            for (Future<?> future : work) {
                future.get(5, TimeUnit.MINUTES);
            }
            System.out.println("[soak] the work of both users took " + (System.nanoTime() - soak) / 1_000_000 + " ms; generations took " + generationMillis + " ms; "
                    + rounds.get() + " rounds of backups and edits");
        } finally {
            pool.shutdownNow();
        }

        // each book got its generations, in the right place, and only those
        assertThat(rounds.get()).isEqualTo(2 * BACKUPS_PER_USER);
        for (Side side : List.of(a, b)) {
            asUser(side.user(), () -> {
                List<ChatMessage> line = mainLine(side.book());
                assertThat(line).as("main line of %s", side.user().getLogin()).hasSize(side.partsBefore() + GENERATIONS_PER_USER);
                assertThat(line.subList(line.size() - GENERATIONS_PER_USER, line.size())).allMatch(p -> p.getResponse() != null && p.getResponse().contains("The ledger held its breath"));
                // the user sees their own book and not the other's
                List<String> mine = manuscriptService.findAllForUser().stream().map(Manuscript::getUuid).toList();
                assertThat(mine).contains(side.book().getUuid());
                Side other = side == a ? b : a;
                assertThat(mine).doesNotContain(other.book().getUuid());
                return null;
            });
        }

        // the prompts were long (the book of the first user is the longest), and every request had its own user's key and book
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests).hasSize(2 * GENERATIONS_PER_USER);
        int longest = requests.stream().mapToInt(r -> r.getRawBody().length()).max().orElse(0);
        System.out.println("[soak] " + requests.size() + " completion requests, the longest " + longest + " characters");
        assertThat(longest).as("the prompt of a long book").isGreaterThan(100_000);
    }
}
