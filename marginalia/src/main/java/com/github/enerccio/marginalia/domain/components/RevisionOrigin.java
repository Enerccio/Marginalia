package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

/**
 * Who is writing a text into a part, for the code that changes the text of a part and has to say what the version it makes is:
 * the source, a short word about it, whether an agent does it, and the reasoning that came with the text (null for none, the
 * reasoning is stored as given). See {@code MessageRevisionService}. The author is the default of the methods that do not ask.
 * <p>
 * {@code counted} says whether the version goes into the writing statistics: false for work that was counted when it was done
 * (the merge of an agent's work into the book), see {@link #uncounted()}.
 */
public record RevisionOrigin(RevisionSource source, String detail, boolean agent, String reasoning, boolean counted) {

    public RevisionOrigin(RevisionSource source, String detail, boolean agent, String reasoning) {
        this(source, detail, agent, reasoning, true);
    }

    /**
     * The same origin whose versions are not counted in the writing statistics.
     */
    public RevisionOrigin uncounted() {
        return new RevisionOrigin(source, detail, agent, reasoning, false);
    }

    /**
     * The author typed it.
     */
    public static final RevisionOrigin AUTHOR = new RevisionOrigin(RevisionSource.HUMAN, null, false, null);

    /**
     * For the code that records the version itself (a restore, a roll back): the change of the text records nothing, and does not
     * look for a text that was not recorded either.
     */
    public static final RevisionOrigin UNRECORDED = new RevisionOrigin(null, null, false, null);

    /**
     * The next part was merged into the part.
     */
    public static final RevisionOrigin MERGED = new RevisionOrigin(RevisionSource.MERGE, null, false, null);

    /**
     * A mass action or a command, done by an agent when {@code agent} is set.
     */
    public static RevisionOrigin automatic(RevisionDetail detail, boolean agent) {
        return new RevisionOrigin(RevisionSource.AUTOMATIC, detail.code(), agent, null);
    }

    /**
     * A person typed it, in the way the detail says (an import, a command); {@code agent} when an agent did it for them.
     */
    public static RevisionOrigin human(RevisionDetail detail, boolean agent) {
        return new RevisionOrigin(RevisionSource.HUMAN, detail.code(), agent, null);
    }

    /**
     * A model wrote it ({@code detail} is "model · protocol"), with the reasoning it came with.
     */
    public static RevisionOrigin model(RevisionSource source, String detail, boolean agent, String reasoning) {
        return new RevisionOrigin(source, detail, agent, reasoning);
    }

    /**
     * The version for the text the part has now.
     */
    public boolean isRecorded() {
        return source != null;
    }

    public RevisionData data(ChatMessage part) {
        return new RevisionData(source, detail, agent, part.getResponse(), reasoning, null, part.getWordCount(), part.getTokenCount(), counted);
    }
}
