package com.github.enerccio.marginalia.domain.service;

import java.time.Instant;
import java.util.List;

/**
 * Watches the requests the application sends to the models: an extension installs one with
 * {@link InferenceSession#addListener} and sees every request (what was sent) and its end (what came back, the reasoning
 * included), whatever started it and whichever provider answered, extension AIs too.
 * <p>
 * {@link #onRequest} is called on the thread that starts the request, the one where the session of the user can still be
 * reached (the session scoped beans, the {@link InferenceSession#current() kind of work}); the end is reported on
 * whatever thread the provider finishes on, to the {@link Observation} that {@code onRequest} returned. Keep what the end
 * needs from the user's session in the observation. A listener must be quick and must not throw: what it throws is logged
 * and dropped, the request goes on.
 * <p>
 * A request that asks for functions to be called ({@code stream} and {@code infer} with a {@code FunctionSet}) is one
 * request with the calls in its {@link Response#calls() response}; a round of an agent is one request per round.
 */
public interface InferenceListener {

    /**
     * A request is about to be sent.
     *
     * @return what is told when it ends, null when this request is not of interest
     */
    Observation onRequest(Request request);

    /**
     * Whether the listener reads {@link Request#messages()}. The messages are a copy of the whole prompt, made for every request
     * when one listener wants them; a listener that only needs to know that a request was made says false and gets an empty list
     * (unless another listener wants them).
     */
    default boolean wantsMessages() {
        return true;
    }

    interface Observation {

        /**
         * A piece of the answer as it arrives, before {@link #onFinish}: reasoning or text, in the order the model wrote it.
         * Only for the requests that stream (a service that answers at once never calls it, and the text is then only in
         * the {@link Response}); on the thread of the provider, so keep it short.
         */
        default void onChunk(InferenceService.ChunkType type, String text) {
        }

        /** The request ended, exactly once: it completed, it was cancelled or it failed. */
        void onFinish(Response response);

    }

    /**
     * @param startedAt when the request was started
     * @param kind      the kind of work from the open {@link InferenceSession} ({@link InferenceSession#GENERATION}, a plugin's own
     *                  name...), null when the request was started outside of any scope
     * @param scope     the parallel run inside the kind (an agent run), null when there is none
     * @param aiName    the name the user gave the AI
     * @param model     the model id the AI asks for, null when the provider has no such setting
     * @param protocol  the name of the protocol (sampling settings) the request used, null when none
     * @param messages  the messages that were sent, in order (a copy, the caller may go on changing its list)
     */
    record Request(Instant startedAt, String kind, String scope, String aiName, String model, String protocol,
                   List<Message> messages) {
    }

    /**
     * @param role        the role of the message: SYSTEM, USER, ASSISTANT, TOOL
     * @param attachments how many attachments (images...) the message carried, they are not kept
     * @param calls       the functions the assistant called in this message, empty for other messages
     */
    record Message(String role, String content, int attachments, List<FunctionCall> calls) {
    }

    /**
     * A function the model called.
     *
     * @param result the text the model got back, null when the call was only asked for and not run (a round)
     * @param failed whether running it ended in an error
     */
    record FunctionCall(String name, String arguments, String result, boolean failed) {
    }

    enum Outcome {
        COMPLETED, CANCELLED, FAILED
    }

    /**
     * @param outcome   how the request ended
     * @param text      what the model wrote, empty when nothing arrived
     * @param reasoning what the model thought, when the provider sent it and the request reports it; null otherwise
     * @param calls     the functions called (or asked for) by the model on the way
     * @param error     the message of the error for {@link Outcome#FAILED}, null otherwise
     * @param millis    the time from the start of the request to its end
     */
    record Response(Outcome outcome, String text, String reasoning, List<FunctionCall> calls, String error, long millis) {
    }

}
