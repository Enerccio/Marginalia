package com.github.enerccio.marginalia.domain.service;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The images in the content of lorebook entries. An image is a Markdown image whose address names a resource of the user,
 * {@code ![caption](resource:<uuid>)}: the reference stays the same wherever the entry is shown (the editor turns it into an
 * address of the page) and travels with backups and exports, which copy the resource and write its new uuid.
 * <p>
 * The model gets the text without the images ({@link #strip}); the images themselves go along as attachments of a message when the
 * model of the book is set to take them (see {@code AI.isSendLoreImages}).
 */
public final class LoreImages {

    /** The scheme of the address of an image of an entry. */
    public static final String SCHEME = "resource:";

    private static final Pattern IMAGE = Pattern.compile(
            "!\\[([^\\]\\n]*)]\\(\\s*resource:([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\s*\\)");

    private LoreImages() {
    }

    /**
     * One image in a text.
     *
     * @param caption the text in the brackets (may be empty)
     * @param uuid    the uuid of the resource
     */
    public record Reference(String caption, String uuid) {
    }

    /**
     * The Markdown of an image of the resource.
     */
    public static String markup(String caption, String uuid) {
        String clean = StringUtils.defaultString(caption).replaceAll("[\\[\\]\\n\\r]", " ").strip();
        return "![" + clean + "](" + SCHEME + uuid + ")";
    }

    /**
     * The images of the text in their order (the same image twice is listed twice).
     */
    public static List<Reference> references(String text) {
        List<Reference> found = new ArrayList<>();
        if (StringUtils.isEmpty(text)) {
            return found;
        }
        Matcher matcher = IMAGE.matcher(text);
        while (matcher.find()) {
            found.add(new Reference(matcher.group(1).strip(), matcher.group(2).toLowerCase()));
        }
        return found;
    }

    /**
     * The uuids of the resources the text shows, each once, in the order of their first image.
     */
    public static Set<String> uuids(String text) {
        Set<String> found = new LinkedHashSet<>();
        references(text).forEach(reference -> found.add(reference.uuid()));
        return found;
    }

    /**
     * The text as the model reads it: without the images (and without the blank lines an image on its own line leaves).
     */
    public static String strip(String text) {
        if (StringUtils.isEmpty(text) || !text.contains(SCHEME)) {
            return text;
        }
        String stripped = IMAGE.matcher(text).replaceAll("");
        // a line that held only images is gone, not left blank
        stripped = stripped.replaceAll("(?m)^[ \\t]+$", "");
        return stripped.replaceAll("\\n{3,}", "\n\n").strip();
    }

    /**
     * The text with other resources in its images.
     *
     * @param replacement the new uuid of a resource, the same or null to keep the image as it is
     */
    public static String replace(String text, UnaryOperator<String> replacement) {
        if (StringUtils.isEmpty(text) || !text.contains(SCHEME)) {
            return text;
        }
        Matcher matcher = IMAGE.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String uuid = matcher.group(2).toLowerCase();
            String other = replacement.apply(uuid);
            String image = other == null || other.equals(uuid) ? matcher.group() : markup(matcher.group(1), other);
            matcher.appendReplacement(out, Matcher.quoteReplacement(image));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * {@link #replace(String, UnaryOperator)} with a map from the old uuid to the new one.
     */
    public static String replace(String text, Map<String, String> replacements) {
        return replace(text, replacements::get);
    }
}
