import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scrapes Kate Monk's Onomastikon (https://tekeli.li/onomastikon/) into the source files of the name generator: one
 * {@code {"_id", "title", "values"}} JSON per category in {@code <output dir>}, which {@code GenerateTables.java} turns
 * into the probability tables. Run on demand, not part of the normal build:
 * {@code mvn exec:exec@scrape-onomastikon} in the plugin folder, or
 * {@code java ScrapeOnomastikon.java <output dir> [delay ms]} with Gson on the classpath.
 * <p>
 * The site is a hand-made tree of pages: index pages (a table of links), and pages of names (tables of cells), often
 * several sections on one page (Male, Female, Surnames...). Rules:
 * <ul>
 * <li>Every page is read once, by its URL; the many links to one page (cross links, "up" links) change nothing.</li>
 * <li>The category is the path of the page below the root, each folder and the page named by the text of the link that
 * leads to it from the index of its parent: {@code Ancient World - Egypt - Male}. A page with several sections adds the
 * heading of the section ({@code ... - Biblical - Female}), a page with one does not.</li>
 * <li>Sections about history, sources, calendars... have no names, and so have the pages without any table of names
 * (indexes, histories): they make no category, neither does a category with fewer than {@value #MIN_NAMES} names.</li>
 * <li>A cell may hold alternatives ({@code Eanna/Enda, Ennae}), a gloss ({@code (Basque)}, {@code 'charity'}), dates,
 * descriptions: only the capitalized words made of letters (one or two words) are names.</li>
 * </ul>
 * The collection is (c) Kate Monk, "copies may be made for personal use only": keep the output out of the repository
 * ({@code src/build/resources} is git-ignored), only the probabilities go into the plugin.
 */
public class ScrapeOnomastikon {

    static final URI ROOT = URI.create("https://tekeli.li/onomastikon/");
    static final Charset PAGE_CHARSET = Charset.forName("windows-1252");
    static final int MIN_NAMES = 10;
    static final String USER_AGENT = "marginalia-name-generator (personal use, one request at a time)";

    /** Headings (lower case) of sections and pages that hold no names. */
    static final Set<String> NOT_NAMES = Set.of("history", "sources", "introduction", "pronunciation", "calendar",
            "months", "festivals", "scripts", "naming patterns", "titles", "religion", "bibliography", "books", "rulers",
            "emperors");

    /** Titles of columns and the like that are capitalized words but no names. */
    static final Set<String> NOT_A_NAME = Set.of("name", "names", "translation", "meaning", "family", "origin", "reign");

    static final Pattern LINK = Pattern.compile("<a\\s[^>]*?href\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    static final Pattern TOKEN = Pattern.compile("(<h([2-4])[^>]*>(.*?)</h\\2>|<table.*?</table>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    static final Pattern CELL = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    static final Pattern ROW = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /** A row of column titles (Given Name, Diminutives, Meaning...). */
    static final Pattern HEADER = Pattern.compile("name|nomen|diminutive|variant|form|spelling|meaning|origin|translation|reign|wives|attribute|element|description",
            Pattern.CASE_INSENSITIVE);
    /** The columns of a table with a header row that hold names. */
    static final Pattern NAME_COLUMN = Pattern.compile("name|nomen|diminutive|variant|form|spelling", Pattern.CASE_INSENSITIVE);
    static final Pattern NOT_NAME_COLUMN = Pattern.compile("meaning|origin|translation|reign|wives|attribute|element|description", Pattern.CASE_INSENSITIVE);
    static final Pattern GENDER_OR_SURNAME = Pattern.compile("\\b(male|female|surnames?)\\b", Pattern.CASE_INSENSITIVE);
    /** "A", "La-/Le", "Ja-/Jam-": the letters a long list is cut into, they belong to the heading before. */
    static final Pattern ALPHABET = Pattern.compile("[\\p{L}\\-/ ]{1,8}");
    static final Pattern ROMAN = Pattern.compile("[IVXLCDM]+");

    static final String[] LATIN1_ENTITIES = ("nbsp iexcl cent pound curren yen brvbar sect uml copy ordf laquo not shy reg macr "
            + "deg plusmn sup2 sup3 acute micro para middot cedil sup1 ordm raquo frac14 frac12 frac34 iquest Agrave Aacute "
            + "Acirc Atilde Auml Aring AElig Ccedil Egrave Eacute Ecirc Euml Igrave Iacute Icirc Iuml ETH Ntilde Ograve Oacute "
            + "Ocirc Otilde Ouml times Oslash Ugrave Uacute Ucirc Uuml Yacute THORN szlig agrave aacute acirc atilde auml aring "
            + "aelig ccedil egrave eacute ecirc euml igrave iacute icirc iuml eth ntilde ograve oacute ocirc otilde ouml divide "
            + "oslash ugrave uacute ucirc uuml yacute thorn yuml").split(" ");
    static final Map<String, Character> ENTITIES = new HashMap<>();

    static {
        for (int i = 0; i < LATIN1_ENTITIES.length; i++) {
            ENTITIES.put(LATIN1_ENTITIES[i], (char) (160 + i));
        }
        ENTITIES.put("amp", '&');
        ENTITIES.put("lt", '<');
        ENTITIES.put("gt", '>');
        ENTITIES.put("quot", '"');
    }

    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20)).build();
    private final long delayMillis;

    /** The pages that could be read, by URL. */
    private final Map<URI, String> pages = new LinkedHashMap<>();
    /** The links of a page: target URL to the text of the link. */
    private final Map<URI, Map<URI, String>> links = new HashMap<>();

    ScrapeOnomastikon(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        long delay = args.length > 1 ? Long.parseLong(args[1]) : 150;
        new ScrapeOnomastikon(delay).run(output);
    }

    void run(Path output) throws Exception {
        crawl();
        Map<String, Set<String>> categories = new LinkedHashMap<>();
        for (Map.Entry<URI, String> page : pages.entrySet()) {
            extract(page.getKey(), page.getValue(), categories);
        }
        categories.values().removeIf(names -> names.size() < MIN_NAMES);
        if (categories.isEmpty()) {
            throw new IllegalStateException("No names found, the site may have changed; " + output + " is untouched");
        }

        Files.createDirectories(output);
        try (Stream<Path> old = Files.list(output)) {
            for (Path file : old.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                Files.delete(file);
            }
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Set<String> ids = new HashSet<>();
        int names = 0;
        for (Map.Entry<String, Set<String>> category : categories.entrySet()) {
            String id = slug(category.getKey());
            for (int n = 2; !ids.add(id); n++) {
                id = slug(category.getKey()) + "-" + n;
            }
            JsonObject json = new JsonObject();
            json.addProperty("_id", id);
            json.addProperty("title", category.getKey());
            JsonArray values = new JsonArray();
            category.getValue().forEach(values::add);
            json.add("values", values);
            Files.writeString(output.resolve(id + ".json"), gson.toJson(json) + "\n");
            names += category.getValue().size();
        }
        System.out.println("onomastikon: " + pages.size() + " pages, " + categories.size() + " categories, " + names
                + " names in " + output);
    }

    // --- crawl ---

    private void crawl() throws Exception {
        Queue<URI> queue = new ArrayDeque<>();
        Set<URI> seen = new HashSet<>();
        queue.add(ROOT.resolve("index.html"));
        seen.add(queue.peek());
        while (!queue.isEmpty()) {
            URI page = queue.remove();
            String html = fetch(page);
            Thread.sleep(delayMillis);
            if (html == null) {
                continue;
            }
            pages.put(page, html);
            Map<URI, String> pageLinks = new LinkedHashMap<>();
            Matcher m = LINK.matcher(html);
            while (m.find()) {
                URI target = normalize(page, m.group(1));
                if (target == null) {
                    continue;
                }
                pageLinks.putIfAbsent(target, text(m.group(2)));
                if (seen.add(target)) {
                    queue.add(target);
                }
            }
            links.put(page, pageLinks);
        }
    }

    private String fetch(URI uri) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder(uri)
                        .header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(30)).build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    return new String(response.body(), PAGE_CHARSET);
                }
                // listed but not there
                System.out.println("onomastikon: " + response.statusCode() + " " + uri);
                return null;
            } catch (IOException e) {
                if (attempt >= 3) {
                    throw e;
                }
                Thread.sleep(1000L * attempt);
            }
        }
    }

    /** The page a link leads to, by its URL: no fragment or query, only html pages of the collection. */
    static URI normalize(URI from, String href) {
        try {
            URI target = from.resolve(href.strip().replace(" ", "%20")).normalize();
            String path = target.getPath();
            if (path == null || !"https".equals(target.getScheme()) || !ROOT.getHost().equals(target.getHost())
                    || !path.startsWith(ROOT.getPath())) {
                return null;
            }
            if (path.endsWith("/")) {
                path += "index.html";
            }
            if (!path.endsWith(".html")) {
                return null;
            }
            return new URI("https", ROOT.getHost(), path, null);
        } catch (Exception e) {
            return null;
        }
    }

    // --- categories of a page ---

    /** The labels of the page: its folders and itself, as the index pages above name them. */
    private List<String> labels(URI page) {
        String relative = page.getPath().substring(ROOT.getPath().length());
        String[] segments = relative.split("/");
        List<String> labels = new ArrayList<>();
        StringBuilder path = new StringBuilder();
        // the root index is not a category, a page named index.html is its folder
        int count = segments.length;
        for (int i = 0; i < count; i++) {
            boolean last = i == count - 1;
            if (last && segments[i].equals("index.html")) {
                break;
            }
            URI parentIndex = ROOT.resolve(path + "index.html");
            path.append(segments[i]);
            URI target = last ? ROOT.resolve(path.toString()) : ROOT.resolve(path + "/index.html");
            if (!last) {
                path.append('/');
            }
            labels.add(labelOf(target, parentIndex, segments[i]));
        }
        return labels;
    }

    /** The text of the link to the target in the index of its parent, else in any page except as an "index" link. */
    private String labelOf(URI target, URI parentIndex, String segment) {
        String text = links.getOrDefault(parentIndex, Map.of()).get(target);
        if (text == null || text.isBlank()) {
            for (Map<URI, String> pageLinks : links.values()) {
                String other = pageLinks.get(target);
                if (other != null && !other.isBlank() && !other.toLowerCase(Locale.ROOT).endsWith("index")) {
                    text = other;
                    break;
                }
            }
        }
        if (text == null || text.isBlank()) {
            text = segment.replaceFirst("\\.html$", "");
        }
        text = text.replaceAll("[\\s,:;.]+$", "").strip();
        // "Ancient-World" is a name made of one word, "Surnames A-F" is not
        return text.contains(" ") ? text : text.replace('-', ' ');
    }

    /** Puts the names of the page into the categories (title to names, without case duplicates). */
    private void extract(URI page, String html, Map<String, Set<String>> categories) {
        // sections in the order of the page; the key is the heading, null for names before any heading
        Map<String, Set<String>> sections = new LinkedHashMap<>();
        String section = null;
        boolean skip = false;
        boolean gender = false;
        Matcher m = TOKEN.matcher(html);
        while (m.find()) {
            if (m.group(3) != null) {
                // "Male<BR>", "Old Testament Male,": no punctuation at the end
                String heading = text(m.group(3)).replaceAll("[\\s,:;.]+$", "");
                if (heading.isEmpty()) {
                    continue;
                }
                if (GENDER_OR_SURNAME.matcher(heading).find()) {
                    section = heading;
                    skip = false;
                    gender = true;
                } else if (gender && ALPHABET.matcher(heading).matches()) {
                    // A, B, C of a long Male list: still Male
                    continue;
                } else {
                    section = heading;
                    skip = NOT_NAMES.contains(heading.toLowerCase(Locale.ROOT));
                    gender = false;
                }
                continue;
            }
            String table = m.group();
            if (skip || LINK.matcher(table).find()) {
                // a table of links is navigation
                continue;
            }
            for (String name : tableNames(table)) {
                sections.computeIfAbsent(section, k -> new LinkedHashSet<>()).add(name);
            }
        }
        if (sections.isEmpty()) {
            return;
        }

        List<String> labels = labels(page);
        if (labels.isEmpty() || labels.stream().anyMatch(l -> NOT_NAMES.contains(l.toLowerCase(Locale.ROOT)))) {
            return;
        }
        // "Central Africa - Central Africa": a folder and its index page are named alike
        for (int i = labels.size() - 1; i > 0; i--) {
            if (labels.get(i).equalsIgnoreCase(labels.get(i - 1))) {
                labels.remove(i);
            }
        }
        String leaf = labels.getLast().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Set<String>> entry : sections.entrySet()) {
            List<String> title = new ArrayList<>(labels);
            String heading = entry.getKey();
            // one section is the page itself; the heading is added when it tells the sections apart
            if (sections.size() > 1 && heading != null && !leaf.contains(heading.toLowerCase(Locale.ROOT))) {
                title.add(heading);
            }
            Set<String> names = categories.computeIfAbsent(String.join(" - ", title), k -> new LinkedHashSet<>());
            Set<String> have = new HashSet<>();
            names.forEach(n -> have.add(n.toLowerCase(Locale.ROOT)));
            for (String name : entry.getValue()) {
                if (have.add(name.toLowerCase(Locale.ROOT))) {
                    names.add(name);
                }
            }
        }
    }

    // --- names of a table ---

    /**
     * The names of a table: the cells of its rows, or, when its first row is a row of column titles (Given Name,
     * Diminutives, Meaning), the cells of the columns that hold names.
     */
    static List<String> tableNames(String table) {
        List<List<String>> rows = new ArrayList<>();
        Matcher rowMatcher = ROW.matcher(table);
        while (rowMatcher.find()) {
            List<String> cells = new ArrayList<>();
            Matcher cellMatcher = CELL.matcher(rowMatcher.group(1));
            while (cellMatcher.find()) {
                cells.add(cellMatcher.group(1));
            }
            rows.add(cells);
        }
        boolean[] keep = null;
        int first = 0;
        if (!rows.isEmpty() && rows.getFirst().size() > 1
                && rows.getFirst().stream().filter(c -> HEADER.matcher(text(c)).find()).count() >= 2) {
            List<String> header = rows.getFirst();
            keep = new boolean[header.size()];
            for (int i = 0; i < keep.length; i++) {
                String title = text(header.get(i));
                keep[i] = NAME_COLUMN.matcher(title).find() && !NOT_NAME_COLUMN.matcher(title).find();
            }
            first = 1;
        }
        List<String> names = new ArrayList<>();
        for (int r = first; r < rows.size(); r++) {
            List<String> cells = rows.get(r);
            for (int c = 0; c < cells.size(); c++) {
                if (keep == null || (c < keep.length && keep[c])) {
                    names.addAll(names(cells.get(c)));
                }
            }
        }
        return names;
    }

    // --- names of a cell ---

    /**
     * The names in a cell: alternatives are separated by comma, slash, semicolon or "+", glosses in parentheses or
     * quotes are dropped, what is left has to be one or two capitalized words of letters.
     */
    static List<String> names(String cellHtml) {
        String cell = text(cellHtml.replaceAll("(?i)<br\\s*/?>", ","))
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("(^|\\s)'[^']*'", " ")
                .replaceAll("\\[[^]]*]", " ");
        List<String> names = new ArrayList<>();
        for (String token : cell.split("[,/;+]")) {
            String name = token.strip().replaceAll("[.\\s]+$", "").replaceAll("\\s+", " ");
            if (isName(name)) {
                names.add(name);
            }
        }
        return names;
    }

    static boolean isName(String name) {
        if (name.length() < 3 || NOT_A_NAME.contains(name.toLowerCase(Locale.ROOT))) {
            return false;
        }
        String[] words = name.split(" ");
        if (words.length > 2) {
            return false;
        }
        for (String word : words) {
            if (word.length() < 2 || !Character.isUpperCase(word.charAt(0)) || ROMAN.matcher(word).matches()) {
                return false;
            }
            for (char c : word.toCharArray()) {
                // letters only (the site has some damaged characters, the feminine ordinal indicator among them)
                if (!(Character.isLetter(c) && c != '\u00aa' && c != '\u00ba') && c != '\'' && c != '\u2019' && c != '-') {
                    return false;
                }
            }
        }
        return true;
    }

    // --- text ---

    /** Text of an HTML fragment: tags removed, entities decoded, white space collapsed. */
    static String text(String html) {
        String s = html.replaceAll("(?s)<[^>]*>", " ");
        StringBuilder out = new StringBuilder();
        Matcher m = Pattern.compile("&(#?)(\\w+);").matcher(s);
        while (m.find()) {
            String replacement = m.group();
            if (m.group(1).isEmpty()) {
                Character c = ENTITIES.get(m.group(2));
                if (c != null) {
                    replacement = String.valueOf(c);
                }
            } else {
                try {
                    int code = m.group(2).startsWith("x") ? Integer.parseInt(m.group(2).substring(1), 16) : Integer.parseInt(m.group(2));
                    replacement = new String(Character.toChars(code));
                } catch (RuntimeException e) {
                    // left as it is
                }
            }
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString().replace('\u00a0', ' ').replaceAll("\\s+", " ").strip();
    }

    static String slug(String title) {
        String slug = title.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "-").replaceAll("^-|-$", "");
        return slug.isEmpty() ? "category" : slug;
    }

}
