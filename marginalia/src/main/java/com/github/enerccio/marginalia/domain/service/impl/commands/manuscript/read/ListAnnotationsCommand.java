package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * {@code /list-annotations [status [part [offset [limit]]]]}: the annotations of the book or of a part, the open ones unless a
 * status is given. Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ListAnnotationsCommand extends ManuscriptReadSupport {

    static final String NAME = "list-annotations";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_ANNOTATIONS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_LIST_ANNOTATIONS, llmDescription = L.DESC_COMMAND_LIST_ANNOTATIONS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_ANNOTATIONS_RETURN))
    public void listAnnotations(CommandContext context) throws Exception {
        listAnnotations(context, null, null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_ANNOTATIONS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_LIST_ANNOTATIONS, llmDescription = L.DESC_COMMAND_LIST_ANNOTATIONS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "open", description = L.DESC_COMMAND_ANNOTATION_STATUS_FILTER_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_ANNOTATIONS_RETURN))
    public void listAnnotationsStatus(CommandContext context) throws Exception {
        listAnnotations(context, ref(context, "status"), null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_ANNOTATIONS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_LIST_ANNOTATIONS, llmDescription = L.DESC_COMMAND_LIST_ANNOTATIONS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "open", description = L.DESC_COMMAND_ANNOTATION_STATUS_FILTER_PARAM),
                    @CommandParameter(name = "part", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_FILTER_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_ANNOTATIONS_RETURN))
    public void listAnnotationsPart(CommandContext context) throws Exception {
        listAnnotations(context, ref(context, "status"), ref(context, "part"), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_ANNOTATIONS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_LIST_ANNOTATIONS, llmDescription = L.DESC_COMMAND_LIST_ANNOTATIONS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "open", description = L.DESC_COMMAND_ANNOTATION_STATUS_FILTER_PARAM),
                    @CommandParameter(name = "part", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_FILTER_PART_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0", description = L.DESC_COMMAND_OFFSET_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_ANNOTATIONS_RETURN))
    public void listAnnotationsOffset(CommandContext context) throws Exception {
        listAnnotations(context, ref(context, "status"), ref(context, "part"), context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_ANNOTATIONS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_LIST_ANNOTATIONS, llmDescription = L.DESC_COMMAND_LIST_ANNOTATIONS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "open", description = L.DESC_COMMAND_ANNOTATION_STATUS_FILTER_PARAM),
                    @CommandParameter(name = "part", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_FILTER_PART_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0", description = L.DESC_COMMAND_OFFSET_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, optional = true, defaultValue = "50", min = "1", max = "200", description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_ANNOTATIONS_RETURN))
    public void listAnnotationsLimit(CommandContext context) throws Exception {
        listAnnotations(context, ref(context, "status"), ref(context, "part"), context.get("offset", Integer.class),
                context.get("limit", Integer.class));
    }

    private void listAnnotations(CommandContext context, String status, String part, Integer offset, Integer limit) throws Exception {
        Book book = new Book(context);
        Set<AnnotationStatus> wanted = statuses(status);
        Map<Long, Integer> positions = book.positions();

        List<Annotation> found;
        if (part == null || part.isBlank()) {
            found = new ArrayList<>(annotationService.listForManuscript(book.manuscript));
            // the story first, in its order, then the parts of the other branches
            found.sort(Comparator.comparingInt((Annotation a) -> positions.getOrDefault(a.getPartId(), Integer.MAX_VALUE))
                    .thenComparing(Annotation::getPartId)
                    .thenComparingInt(a -> a.getStart() == null ? -1 : a.getStart())
                    .thenComparing(Annotation::getId));
        } else {
            found = annotationService.listForPart(book.manuscript, book.locate(part, null).message());
        }
        found.removeIf(annotation -> !wanted.contains(annotation.getStatus()));

        int from = Math.min(Math.max(offset == null ? 0 : offset, 0), found.size());
        int to = Math.min(found.size(), from + limit(limit));
        JsonArray rows = new JsonArray();
        for (Annotation annotation : found.subList(from, to)) {
            rows.add(describe(annotation, book));
        }
        JsonObject result = new JsonObject();
        result.addProperty("total", found.size());
        result.addProperty("offset", from);
        result.add("annotations", rows);
        result.addProperty("truncated", to < found.size());
        answer(context, result);
    }

    /**
     * The statuses a filter names: open when none is given, every one for {@code all}.
     */
    private Set<AnnotationStatus> statuses(String status) {
        if (status == null || status.isBlank()) {
            return EnumSet.of(AnnotationStatus.OPEN);
        }
        if (status.strip().equalsIgnoreCase("all")) {
            return EnumSet.allOf(AnnotationStatus.class);
        }
        return EnumSet.of(parseStatus(status, loc));
    }

    /**
     * The status the text names.
     *
     * @throws IllegalArgumentException when it names none
     */
    static AnnotationStatus parseStatus(String status, com.github.enerccio.marginalia.loc.Localization loc) {
        for (AnnotationStatus value : AnnotationStatus.values()) {
            if (value.name().equalsIgnoreCase(status.strip())) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_ANNOTATION_STATUS), status,
                Arrays.stream(AnnotationStatus.values()).map(value -> value.name().toLowerCase(Locale.ROOT)).toList()));
    }
}
