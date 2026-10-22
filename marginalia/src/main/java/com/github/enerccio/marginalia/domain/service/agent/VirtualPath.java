package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A path in the virtual namespace of an agent: absolute, made of segments, with no way to name anything above the root.
 * The model supplies a string, {@link #parse} reads it once into this immutable value, and everything after that works
 * with the value, never with the string.
 * <p>
 * Rules of {@link #parse}, applied before any path is mapped to the disk:
 * <ul>
 * <li>the text is normalized (NFC) and has to start with {@code /};
 * <li>{@code ..} goes up and stops at the root (as in a chroot), so {@code /../../a} is {@code /a};
 * <li>an empty segment ({@code //}), {@code .}, a backslash, control and format characters, unpaired surrogates and
 * {@code < > : " | ? *} are refused, which also takes drive letters and UNC paths;
 * <li>a name must not start or end with a space or end with a dot, and must not be a name Windows reads as a device
 * ({@code CON}, {@code NUL}, {@code COM1}...);
 * <li>limits of the length of a name, of the whole path and of the depth.
 * </ul>
 * A single {@code /} at the end is accepted (the model writes folders that way) and dropped. Percent encoding and
 * look-alike characters (the full width dot) are ordinary letters: they are names like any other and lead nowhere.
 * Names that differ only by case are not told apart here, the file system of the agent refuses to hold two.
 */
public final class VirtualPath implements Comparable<VirtualPath> {

    public static final int MAX_INPUT_LENGTH = 2_000;
    public static final int MAX_PATH_LENGTH = 400;
    public static final int MAX_NAME_LENGTH = 100;
    // bytes of one name in UTF-8, file systems allow 255
    public static final int MAX_NAME_BYTES = 200;
    public static final int MAX_DEPTH = 12;

    public static final VirtualPath ROOT = new VirtualPath(List.of());

    private static final String FORBIDDEN_CHARACTERS = "/\\<>:\"|?*";
    private static final Pattern DEVICE_NAME = Pattern.compile(
            "^(CON|PRN|AUX|NUL|CONIN\\$|CONOUT\\$|(COM|LPT)[0-9\u00b9\u00b2\u00b3])(\\..*)?$", Pattern.CASE_INSENSITIVE);

    private final List<String> segments;

    private VirtualPath(List<String> segments) {
        this.segments = List.copyOf(segments);
    }

    /**
     * @throws VirtualFileException with the reason, for the model, when the text is not a valid path
     */
    public static VirtualPath parse(String text) {
        if (text == null || text.isEmpty()) {
            throw new VirtualFileException("The path is empty. Paths are absolute and start with /, for example /notes/plot.md");
        }
        if (text.length() > MAX_INPUT_LENGTH) {
            throw new VirtualFileException("The path is too long");
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        if (normalized.charAt(0) != '/') {
            throw new VirtualFileException("The path \"" + printable(text) + "\" is not absolute. Paths start with /, "
                    + "for example /notes/plot.md");
        }
        String rest = normalized.substring(1);
        if (rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        List<String> segments = new ArrayList<>();
        if (!rest.isEmpty()) {
            for (String segment : rest.split("/", -1)) {
                if (segment.equals("..")) {
                    if (!segments.isEmpty()) {
                        segments.remove(segments.size() - 1);
                    }
                    continue;
                }
                checkName(segment);
                segments.add(segment);
            }
        }
        if (segments.isEmpty()) {
            return ROOT;
        }
        VirtualPath path = new VirtualPath(segments);
        if (segments.size() > MAX_DEPTH) {
            throw new VirtualFileException("The path is too deep, at most " + MAX_DEPTH + " folders");
        }
        if (path.toString().length() > MAX_PATH_LENGTH) {
            throw new VirtualFileException("The path is too long, at most " + MAX_PATH_LENGTH + " characters");
        }
        return path;
    }

    /**
     * Checks a single name (a file or a folder).
     *
     * @throws VirtualFileException when it is not allowed
     */
    public static void checkName(String name) {
        if (name == null || name.isEmpty()) {
            throw new VirtualFileException("The path has an empty part (//) or ends in a name that is missing");
        }
        if (name.equals(".")) {
            throw new VirtualFileException("\".\" is not allowed in a path, write the whole path from /");
        }
        if (name.length() > MAX_NAME_LENGTH || name.getBytes(StandardCharsets.UTF_8).length > MAX_NAME_BYTES) {
            throw new VirtualFileException("A name is too long, at most " + MAX_NAME_LENGTH + " characters");
        }
        for (int i = 0; i < name.length(); ) {
            int c = name.codePointAt(i);
            i += Character.charCount(c);
            int type = Character.getType(c);
            if (Character.isISOControl(c) || type == Character.FORMAT || type == Character.LINE_SEPARATOR
                    || type == Character.PARAGRAPH_SEPARATOR || type == Character.SURROGATE
                    || type == Character.UNASSIGNED) {
                throw new VirtualFileException("The name \"" + printable(name) + "\" has a character that is not allowed "
                        + "(control, invisible or unknown)");
            }
            if (c < 128 && FORBIDDEN_CHARACTERS.indexOf(c) >= 0) {
                throw new VirtualFileException("The name \"" + printable(name) + "\" has the character " + (char) c
                        + " that is not allowed. Use / between folders; names cannot have \\ < > : \" | ? *");
            }
        }
        int first = name.codePointAt(0);
        int last = name.codePointBefore(name.length());
        if (Character.isWhitespace(first) || Character.isSpaceChar(first) || Character.isWhitespace(last)
                || Character.isSpaceChar(last)) {
            throw new VirtualFileException("The name \"" + printable(name) + "\" starts or ends with a space");
        }
        if (name.endsWith(".")) {
            throw new VirtualFileException("The name \"" + printable(name) + "\" ends with a dot");
        }
        if (DEVICE_NAME.matcher(name).matches()) {
            throw new VirtualFileException("The name \"" + name + "\" is reserved, choose another");
        }
    }

    /**
     * The text shown in an error: the model's own text, cut short, with the characters that would break a line shown
     * as spaces.
     */
    static String printable(String text) {
        String clean = text.replaceAll("\\p{Cntrl}", " ");
        return clean.length() > 80 ? clean.substring(0, 80) + "..." : clean;
    }

    public boolean isRoot() {
        return segments.isEmpty();
    }

    public int getDepth() {
        return segments.size();
    }

    public List<String> getSegments() {
        return segments;
    }

    /**
     * The first name of the path, null for the root.
     */
    public String getFirst() {
        return segments.isEmpty() ? null : segments.get(0);
    }

    /**
     * The last name of the path, null for the root.
     */
    public String getName() {
        return segments.isEmpty() ? null : segments.get(segments.size() - 1);
    }

    /**
     * The folder the path is in, null for the root.
     */
    public VirtualPath getParent() {
        return segments.isEmpty() ? null : new VirtualPath(segments.subList(0, segments.size() - 1));
    }

    /**
     * The path of a name in this folder.
     *
     * @throws VirtualFileException when the name is not allowed or the path would be too deep
     */
    public VirtualPath resolve(String name) {
        checkName(name);
        if (segments.size() >= MAX_DEPTH) {
            throw new VirtualFileException("The path is too deep, at most " + MAX_DEPTH + " folders");
        }
        List<String> longer = new ArrayList<>(segments);
        longer.add(name);
        return new VirtualPath(longer);
    }

    /**
     * Whether this path is the other one or is below it.
     */
    public boolean startsWith(VirtualPath other) {
        return segments.size() >= other.segments.size()
                && segments.subList(0, other.segments.size()).equals(other.segments);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VirtualPath other && segments.equals(other.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    @Override
    public int compareTo(VirtualPath other) {
        return toString().compareTo(other.toString());
    }

    /**
     * The path as the model writes it: {@code /} for the root, {@code /a/b} otherwise.
     */
    @Override
    public String toString() {
        return segments.isEmpty() ? "/" : "/" + String.join("/", segments);
    }

}
