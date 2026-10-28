package com.github.enerccio.marginalia.extensions.charactertracker.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.charactertracker.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configurable
public class CharacterTrackerService {

    /**
     * Icons are scaled down to fit into this many pixels (width and height).
     */
    public static final int MAX_ICON_SIZE = 200;

    /**
     * Refuse images with more pixels than this, a small file can decompress to a huge image.
     */
    private static final long MAX_SOURCE_PIXELS = 50_000_000L;

    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    /** Whether the text is a color the tracker takes, {@code #rrggbb}. */
    public static boolean isColor(String color) {
        return color != null && COLOR.matcher(color).matches();
    }
    private static final Pattern BASE64 = Pattern.compile("[A-Za-z0-9+/]*={0,2}");

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private InferenceServices inferenceServices;

    /**
     * Buffer for the headers of the inserted message, like {@code PrepareContentStep} adds for every story part.
     */
    private static final long MESSAGE_BUFFER_TOKENS = 100;

    private final Gson gson = new Gson();

    /**
     * What a generation inserts into its prompt: the text of the characters and where it goes. Taken once when the
     * generation starts, so the tokens that were reserved for it are the ones that are sent.
     */
    public record PromptInsertion(String text, int depth, LLMRole role) {
    }

    /** Who wants to know that the characters of a book were changed from outside of its view, by the uuid of the book. */
    private final Map<String, Set<Runnable>> changeListeners = new ConcurrentHashMap<>();

    /**
     * Tells {@code listener} whenever {@link #changed} is called for the book, from the thread of the caller. For a view
     * that keeps the characters in memory and has to read them again when a command changed them.
     *
     * @return what removes the listener
     */
    public Runnable onChange(Manuscript manuscript, Runnable listener) {
        String key = manuscript.getUuid();
        changeListeners.computeIfAbsent(key, _ -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> changeListeners.computeIfPresent(key, (_, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    /**
     * Tells the views of the book that its characters were stored by someone else than the view (a command).
     */
    public void changed(Manuscript manuscript) {
        Set<Runnable> listeners = changeListeners.get(manuscript.getUuid());
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    /**
     * The characters of the book as they are stored now - the story view keeps the book it was opened with, the
     * characters may have been saved since.
     */
    public CharacterBook load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return read(current != null ? current : manuscript);
    }

    public CharacterBook read(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        CharacterTrackerData data = null;
        if (attrs != null && attrs.has(CharacterTrackerData.KEY)) {
            data = gson.fromJson(attrs.get(CharacterTrackerData.KEY), CharacterTrackerData.class);
        }
        return new CharacterBook(data);
    }

    public void save(Manuscript manuscript, CharacterBook book) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        attrs.add(CharacterTrackerData.KEY, gson.toJsonTree(book.toData()));
        manuscriptService.save(manuscript);
    }

    /**
     * The characters of the book for the prompt, null when they are not inserted (switched off, or no characters). A
     * line for each character with its tagline and note, then a line for each relationship.
     */
    public PromptInsertion promptInsertion(CharacterBook book) {
        if (!book.isPromptEnabled()) {
            return null;
        }
        String text = renderPrompt(book);
        return text.isEmpty() ? null : new PromptInsertion(text, book.getPromptDepth(), book.getPromptRole());
    }

    /**
     * Text of the characters as the model reads it, empty when there are none.
     */
    public static String renderPrompt(CharacterBook book) {
        List<StoryCharacter> characters = book.sortedCharacters().stream()
                .filter(c -> StringUtils.isNotBlank(c.getName())).toList();
        if (characters.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder("Characters of the story:");
        for (StoryCharacter character : characters) {
            text.append("\n- ").append(character.getName().trim());
            if (StringUtils.isNotBlank(character.getTagline())) {
                text.append(" (").append(character.getTagline().trim()).append(')');
            }
            if (StringUtils.isNotBlank(character.getNote())) {
                text.append(": ").append(character.getNote().trim().replaceAll("\\s*\\R\\s*", " "));
            }
        }
        StringBuilder relations = new StringBuilder();
        for (CharacterRelation relation : book.allRelations()) {
            StoryCharacter source = book.get(relation.getSourceUuid());
            StoryCharacter target = book.get(relation.getTargetUuid());
            if (source == null || target == null || StringUtils.isBlank(source.getName()) || StringUtils.isBlank(target.getName())) {
                continue;
            }
            relations.append("\n- ").append(source.getName().trim())
                    .append(relation.getDirection() == RelationDirection.UNIDIRECTIONAL ? " <-> " : " -> ")
                    .append(target.getName().trim());
            String detail = Stream.of(relation.getName(), relation.getTagline()).filter(StringUtils::isNotBlank)
                    .map(String::trim).collect(Collectors.joining(", "));
            if (!detail.isEmpty()) {
                relations.append(": ").append(detail);
            }
        }
        if (!relations.isEmpty()) {
            text.append("\n\nRelationships:").append(relations);
        }
        return text.toString();
    }

    /**
     * Tokens the insertion takes in the prompt of the model {@code ai} that generates.
     */
    public long countTokens(AI ai, PromptInsertion insertion) throws Exception {
        return inferenceServices.forAI(ai).countTokens(insertion.text()) + MESSAGE_BUFFER_TOKENS;
    }

    /**
     * Approximate size of the characters text for the model of the book - cheap enough to call when the view is built.
     */
    public long estimateTokens(Manuscript manuscript, String text) throws Exception {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        return inferenceServices.forAI(manuscript.getAi()).countTokensApprox(text);
    }

    /**
     * Returns a copy of {@code payload} with the text inserted {@link PromptInsertion#depth() depth} messages before
     * its end. It never goes before the leading system prompt.
     */
    public List<LLMChatMessage> insert(List<LLMChatMessage> payload, PromptInsertion insertion) {
        if (payload == null || insertion == null) {
            return payload;
        }
        int min = !payload.isEmpty() && payload.getFirst().getRole() == LLMRole.SYSTEM ? 1 : 0;
        int index = Math.clamp(payload.size() - Math.max(0, insertion.depth()), min, payload.size());

        List<LLMChatMessage> result = new ArrayList<>(payload);
        result.add(index, LLMChatMessage.of(insertion.role(), insertion.text()));
        return result;
    }

    /**
     * All entries of the book's lorebook and of its sub lorebooks (deleted ones are skipped), sorted by their order;
     * entries with the same order keep the order of the lorebooks (the book's own first) and of the entries in them.
     * Empty when the book has no lorebook.
     */
    public List<LorebookEntry> collectLoreEntries(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null) {
            current = manuscript;
        }
        Lorebook root = current.getLorebook() != null ? lorebookService.find(current.getLorebook()) : null;
        if (root == null) {
            return List.of();
        }

        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> processed = new HashSet<>();
        Queue<Lorebook> toProcess = new LinkedList<>();
        toProcess.add(root);
        while (!toProcess.isEmpty()) {
            Lorebook lorebook = toProcess.remove();
            if (lorebook.isDeleted() || !processed.add(lorebook.getId())) {
                continue;
            }
            lorebookEntryService.getEntriesForLorebook(lorebook).stream()
                    .filter(entry -> !entry.isDeleted()).forEach(entries::add);
            toProcess.addAll(lorebookService.getSubbooks(lorebook));
        }
        // a stable sort
        entries.sort(Comparator.comparingInt(LorebookEntry::getOrder));
        return entries;
    }

    /**
     * Scales the image down to fit into {@link #MAX_ICON_SIZE} x {@link #MAX_ICON_SIZE} (smaller images are kept as
     * they are, the proportions never change) and returns it as base64 of a PNG.
     *
     * @throws IllegalArgumentException when the data is not an image that can be read
     */
    public static String createIcon(byte[] content) {
        BufferedImage source = decode(content);
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, Math.min((double) MAX_ICON_SIZE / width, (double) MAX_ICON_SIZE / height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        // halving first keeps the result sharp when a large photo is shrunk to an icon
        BufferedImage image = toArgb(source);
        while (image.getWidth() / 2 >= targetWidth && image.getHeight() / 2 >= targetHeight) {
            image = resize(image, image.getWidth() / 2, image.getHeight() / 2);
        }
        if (image.getWidth() != targetWidth || image.getHeight() != targetHeight) {
            image = resize(image, targetWidth, targetHeight);
        }

        try {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", png)) {
                throw new IllegalArgumentException("Can't write the icon");
            }
            return Base64.getEncoder().encodeToString(png.toByteArray());
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Can't write the icon", e);
        }
    }

    private static BufferedImage decode(byte[] content) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Not an image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_SOURCE_PIXELS) {
                    throw new IllegalArgumentException("The image has too many pixels");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("The image can't be read", e);
        }
    }

    private static BufferedImage toArgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        return resize(image, image.getWidth(), image.getHeight());
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * The color if it is a valid {@code #rrggbb}, otherwise the default one. Colors end up in the style of elements,
     * so nothing else is let through.
     */
    public static String safeColor(String color) {
        return color != null && COLOR.matcher(color).matches() ? color.toLowerCase() : StoryCharacter.DEFAULT_COLOR;
    }

    /**
     * Black or white, whichever is easier to read on {@code color}.
     */
    public static String textColor(String color) {
        String c = safeColor(color);
        int r = Integer.parseInt(c.substring(1, 3), 16);
        int g = Integer.parseInt(c.substring(3, 5), 16);
        int b = Integer.parseInt(c.substring(5, 7), 16);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.55 ? "#000000" : "#ffffff";
    }

    /**
     * The {@code src} of an image element for the icon (base64 of a PNG) of a character, {@code null} if it has none.
     */
    public static String iconSource(String icon) {
        if (icon == null || icon.isBlank() || !BASE64.matcher(icon).matches()) {
            return null;
        }
        return "data:image/png;base64," + icon;
    }
}
