package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /get-annotation annotation}: one annotation in full, with the text around the passage it is about.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetAnnotationCommand extends ManuscriptReadSupport {

    static final String NAME = "get-annotation";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_ANNOTATION, llmDescription = L.DESC_COMMAND_GET_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_ANNOTATION_RETURN))
    public void getAnnotation(CommandContext context) throws Exception {
        Book book = new Book(context);
        Annotation annotation = book.annotation(ref(context, "annotation"));
        JsonObject result = describe(annotation, book);
        // the whole quote and the text around it, not the short form of the lists
        if (annotation.getQuote() != null) {
            result.addProperty("quote", annotation.getQuote());
            result.addProperty("before", annotation.getContextBefore());
            result.addProperty("after", annotation.getContextAfter());
        }
        answer(context, result);
    }
}
