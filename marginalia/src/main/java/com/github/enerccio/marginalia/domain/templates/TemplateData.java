package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base of every template data object.
 * <p>
 * Subclasses declare the template specific variables as bean properties (those are listed in the UI hints). This base
 * class implements the SillyTavern compatible macros (see {@link com.github.enerccio.marginalia.domain.templates.macros.Macros})
 * on top of a {@link TemplateContext}. Templates that share a context share variables, so pass the same context (or the
 * same data object) to templates that should see each other's {@code setvar}s.
 */
public abstract class TemplateData {
    private static final Logger log = LoggerFactory.getLogger(TemplateData.class);

    /**
     * Marker emitted by {{trim}}; the template service removes it together with surrounding newlines.
     */
    public static final String TRIM_MARKER = "trim";

    private static final Set<String> FALSY = Set.of("", "false", "0", "off", "no");
    private static final Map<Class<?>, Map<String, Method>> GETTERS = new ConcurrentHashMap<>();

    private static final Pattern UTC_OFFSET = Pattern.compile("^UTC\\s*([+-])\\s*(\\d{1,2})(?::?(\\d{2}))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DICE = Pattern.compile("^(\\d*)\\s*d\\s*(\\d+)\\s*(?:([+-])\\s*(\\d+))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_ONLY = Pattern.compile("^\\d+$");

    private TemplateContext templateContext = new TemplateContext();

    /*
     * Not a bean getter on purpose, so templates can't access the context as {{templateContext}}.
     */
    public TemplateContext templateContext() {
        return templateContext;
    }

    public void setTemplateContext(TemplateContext templateContext) {
        this.templateContext = templateContext == null ? new TemplateContext() : templateContext;
    }

    protected TemplateVariables variables() {
        return templateContext.getVariables();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Property resolution
    // ------------------------------------------------------------------------------------------------------------

    private static Map<String, Method> getters(Class<?> clazz) {
        return GETTERS.computeIfAbsent(clazz, c -> {
            Map<String, Method> result = new HashMap<>();
            try {
                BeanInfo info = Introspector.getBeanInfo(c, TemplateData.class);
                for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                    if (pd.getReadMethod() != null) {
                        result.put(pd.getName(), pd.getReadMethod());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to introspect template data {}", c, e);
            }
            return result;
        });
    }

    /**
     * @return true if this data object (or the shared context) defines a property of given name
     */
    public boolean hasProperty(String name) {
        return getters(getClass()).containsKey(name) || templateContext.property(name) != null;
    }

    /**
     * Resolves property of this data object, falling back to the shared context when the property is not declared
     * or its value is null.
     */
    public Object resolveProperty(String name) {
        Method getter = getters(getClass()).get(name);
        if (getter != null) {
            try {
                Object value = getter.invoke(this);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                log.warn("Failed to read template property {}", name, e);
            }
        }
        TemplateContext.PropertyValue value = templateContext.property(name);
        return value == null ? null : value.value();
    }

    public static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Collection<?> c) {
            return !c.isEmpty();
        }
        return !FALSY.contains(value.toString().trim().toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Names & participants
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {{user}} - Marginalia has no persona, the protagonist is the POV character.
     */
    public String user() {
        return Objects.toString(templateContext.getPovCharacter(), "");
    }

    /**
     * {{char}} - the POV character.
     */
    public String character() {
        return Objects.toString(templateContext.getPovCharacter(), "");
    }

    /**
     * {{group}} - characters present in the scene (falls back to the POV character).
     */
    public String group() {
        String present = templateContext.getPresentCharacters();
        if (present == null || present.isBlank()) {
            return character();
        }
        return present;
    }

    /**
     * {{notChar}} - present characters except the POV character.
     */
    public String notCharacter() {
        String present = templateContext.getPresentCharacters();
        if (present == null || present.isBlank()) {
            return "";
        }
        String pov = character().trim();
        List<String> result = new ArrayList<>();
        for (String name : present.split("[,\\n]")) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase(pov)) {
                result.add(trimmed);
            }
        }
        return String.join(", ", result);
    }

    public String description() {
        return Objects.toString(templateContext.getManuscriptDescription(), "");
    }

    public String scenario() {
        return Objects.toString(templateContext.getSceneSetting(), "");
    }

    /**
     * {{input}} - instructions written for the current turn.
     */
    public String input() {
        return Objects.toString(templateContext.getInstructions(), "");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Story history
    // ------------------------------------------------------------------------------------------------------------

    public String lastMessage() {
        List<String> messages = templateContext.getStoryMessages();
        return messages.isEmpty() ? "" : Objects.toString(messages.getLast(), "");
    }

    public String lastMessageId() {
        List<String> messages = templateContext.getStoryMessages();
        return messages.isEmpty() ? "" : String.valueOf(messages.size() - 1);
    }

    /**
     * {{lastUserMessage}} - instructions that were used for the last story message.
     */
    public String lastUserMessage() {
        return Objects.toString(templateContext.getLastInstructions(), "");
    }

    public String summary() {
        return Objects.toString(templateContext.getLatestSummary(), "");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Time & date
    // ------------------------------------------------------------------------------------------------------------

    protected ZonedDateTime now() {
        return ZonedDateTime.now(templateContext.getClock());
    }

    public String time(String utcOffset) {
        ZonedDateTime now = now();
        if (utcOffset != null && !utcOffset.isBlank()) {
            Matcher m = UTC_OFFSET.matcher(utcOffset.trim());
            if (m.matches()) {
                int hours = Integer.parseInt(m.group(2));
                int minutes = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
                int sign = "-".equals(m.group(1)) ? -1 : 1;
                try {
                    now = now.withZoneSameInstant(ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes));
                } catch (DateTimeException e) {
                    log.debug("Invalid UTC offset {}", utcOffset);
                }
            }
        }
        return now.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
    }

    public String date() {
        return now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
    }

    public String weekday() {
        return now().format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH));
    }

    public String isoTime() {
        return now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String isoDate() {
        return now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * {{datetimeformat::format}} - format uses moment.js tokens, same as SillyTavern (e.g. YYYY-MM-DD HH:mm:ss).
     */
    public String dateTimeFormat(String format) {
        if (format == null || format.isBlank()) {
            return isoDate();
        }
        try {
            return now().format(DateTimeFormatter.ofPattern(MomentFormat.toJavaPattern(format), Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            log.debug("Invalid date time format {}", format);
            return "";
        }
    }

    /**
     * {{idleDuration}} - time since the last story message was requested.
     */
    public String idleDuration() {
        Date last = templateContext.getLastMessageTime();
        if (last == null) {
            return "just now";
        }
        return humanize(Duration.between(last.toInstant(), now().toInstant()), false);
    }

    /**
     * {{timeDiff::left::right}} - humanized difference between two times (ISO date/time, HH:mm or "now").
     */
    public String timeDiff(String left, String right) {
        ZonedDateTime l = parseTime(left);
        ZonedDateTime r = parseTime(right);
        if (l == null || r == null) {
            return "";
        }
        return humanize(Duration.between(r, l), true);
    }

    private ZonedDateTime parseTime(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        ZonedDateTime now = now();
        if (v.isEmpty() || v.equalsIgnoreCase("now")) {
            return now;
        }
        try {
            return ZonedDateTime.parse(v);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(v).toZonedDateTime();
        } catch (DateTimeParseException ignored) {
        }
        for (String pattern : List.of("yyyy-MM-dd'T'HH:mm[:ss][.SSS]", "yyyy-MM-dd HH:mm[:ss]")) {
            try {
                return LocalDateTime.parse(v, DateTimeFormatter.ofPattern(pattern)).atZone(now.getZone());
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return LocalDate.parse(v).atStartOfDay(now.getZone());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalTime.parse(v).atDate(now.toLocalDate()).atZone(now.getZone());
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    /**
     * Same thresholds as moment.js duration.humanize().
     */
    static String humanize(Duration duration, boolean withSuffix) {
        boolean future = !duration.isNegative();
        long seconds = Math.abs(duration.getSeconds());
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;
        double months = days / 30.4375;
        double years = days / 365.25;

        String text;
        if (seconds < 45) {
            text = "a few seconds";
        } else if (seconds < 90) {
            text = "a minute";
        } else if (minutes < 45) {
            text = Math.round(minutes) + " minutes";
        } else if (minutes < 90) {
            text = "an hour";
        } else if (hours < 22) {
            text = Math.round(hours) + " hours";
        } else if (hours < 36) {
            text = "a day";
        } else if (days < 26) {
            text = Math.round(days) + " days";
        } else if (days < 45) {
            text = "a month";
        } else if (days < 320) {
            text = Math.max(2, Math.round(months)) + " months";
        } else if (days < 548) {
            text = "a year";
        } else {
            text = Math.max(2, Math.round(years)) + " years";
        }

        if (!withSuffix) {
            return text;
        }
        return future ? "in " + text : text + " ago";
    }

    // ------------------------------------------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------------------------------------------

    public String getVar(Scope scope, String name) {
        return Objects.toString(variables().get(scope, name), "");
    }

    public String setVar(Scope scope, String name, String value) {
        variables().set(scope, name, value);
        return "";
    }

    /**
     * Numeric addition when both values are numbers, string concatenation otherwise.
     */
    public String addVar(Scope scope, String name, String value) {
        String current = variables().get(scope, name);
        BigDecimal currentNumber = current == null || current.isBlank() ? BigDecimal.ZERO : parseNumber(current);
        BigDecimal increment = parseNumber(value);
        if (currentNumber != null && increment != null) {
            variables().set(scope, name, formatNumber(currentNumber.add(increment)));
        } else {
            variables().set(scope, name, Objects.toString(current, "") + Objects.toString(value, ""));
        }
        return "";
    }

    public String incVar(Scope scope, String name) {
        return stepVar(scope, name, BigDecimal.ONE);
    }

    public String decVar(Scope scope, String name) {
        return stepVar(scope, name, BigDecimal.ONE.negate());
    }

    private String stepVar(Scope scope, String name, BigDecimal step) {
        BigDecimal current = parseNumber(variables().get(scope, name));
        String result = formatNumber((current == null ? BigDecimal.ZERO : current).add(step));
        variables().set(scope, name, result);
        return result;
    }

    public String hasVar(Scope scope, String name) {
        return String.valueOf(variables().has(scope, name));
    }

    public String deleteVar(Scope scope, String name) {
        variables().delete(scope, name);
        return "";
    }

    /**
     * Implements variable shorthands ({{.name op value}} and {{$name op value}}).
     *
     * @param value supplies the right hand side, evaluated only when the operator needs it
     */
    public String variableShorthand(Scope scope, String name, String operator, java.util.function.Supplier<String> value) {
        String op = operator == null ? "" : operator;
        TemplateVariables vars = variables();
        switch (op) {
            case "":
                return getVar(scope, name);
            case "=":
                return setVar(scope, name, value.get());
            case "++":
                return incVar(scope, name);
            case "--":
                return decVar(scope, name);
            case "+=":
                return addVar(scope, name, value.get());
            case "-=": {
                BigDecimal subtract = parseNumber(value.get());
                if (subtract == null) {
                    log.warn("Variable {}: value for -= is not a number", name);
                    return "";
                }
                BigDecimal current = parseNumber(vars.get(scope, name));
                vars.set(scope, name, formatNumber((current == null ? BigDecimal.ZERO : current).subtract(subtract)));
                return "";
            }
            case "||": {
                String current = vars.get(scope, name);
                return isTruthy(current) ? current : value.get();
            }
            case "??": {
                return vars.has(scope, name) ? vars.get(scope, name) : value.get();
            }
            case "||=": {
                String current = vars.get(scope, name);
                if (isTruthy(current)) {
                    return current;
                }
                String newValue = value.get();
                vars.set(scope, name, newValue);
                return newValue;
            }
            case "??=": {
                if (vars.has(scope, name)) {
                    return vars.get(scope, name);
                }
                String newValue = value.get();
                vars.set(scope, name, newValue);
                return newValue;
            }
            case "==":
                return String.valueOf(Objects.toString(vars.get(scope, name), "").equals(Objects.toString(value.get(), "")));
            case "!=":
                return String.valueOf(!Objects.toString(vars.get(scope, name), "").equals(Objects.toString(value.get(), "")));
            case ">", ">=", "<", "<=": {
                BigDecimal left = parseNumber(vars.get(scope, name));
                BigDecimal right = parseNumber(value.get());
                if (left == null || right == null) {
                    return "false";
                }
                int cmp = left.compareTo(right);
                boolean result = switch (op) {
                    case ">" -> cmp > 0;
                    case ">=" -> cmp >= 0;
                    case "<" -> cmp < 0;
                    default -> cmp <= 0;
                };
                return String.valueOf(result);
            }
            default:
                log.warn("Unknown variable operator {}", op);
                return "";
        }
    }

    static BigDecimal parseNumber(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String formatNumber(BigDecimal number) {
        BigDecimal stripped = number.stripTrailingZeros();
        if (stripped.scale() < 0) {
            stripped = stripped.setScale(0);
        }
        return stripped.toPlainString();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Randomization
    // ------------------------------------------------------------------------------------------------------------

    private static List<String> choices(List<String> args) {
        if (args.size() == 1) {
            // legacy {{random:a,b,c}} / {{random a,b,c}} form
            List<String> result = new ArrayList<>();
            for (String part : args.getFirst().split(",")) {
                result.add(part.trim());
            }
            return result;
        }
        return args;
    }

    /**
     * {{random::a::b::c}} - new random choice every time.
     */
    public String random(List<String> args) {
        List<String> choices = choices(args);
        if (choices.isEmpty()) {
            return "";
        }
        return choices.get(templateContext.getRandom().nextInt(choices.size()));
    }

    /**
     * {{pick::a::b::c}} - random choice that is stable for the manuscript and position in the template.
     */
    public String pick(List<String> args, String position) {
        List<String> choices = choices(args);
        if (choices.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((templateContext.getPickSeed() + "|" + position + "|" + String.join("::", choices))
                    .getBytes(StandardCharsets.UTF_8));
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xff);
            }
            return choices.get(new Random(seed).nextInt(choices.size()));
        } catch (Exception e) {
            return choices.getFirst();
        }
    }

    /**
     * {{roll::XdY+Z}} - dice roll, {{roll::20}} is the same as {{roll::1d20}}.
     */
    public String roll(String formula) {
        if (formula == null) {
            return "";
        }
        String f = formula.trim();
        if (NUMBER_ONLY.matcher(f).matches()) {
            f = "1d" + f;
        }
        Matcher m = DICE.matcher(f);
        if (!m.matches()) {
            log.debug("Invalid dice formula {}", formula);
            return "";
        }
        try {
            int count = m.group(1).isEmpty() ? 1 : Integer.parseInt(m.group(1));
            int sides = Integer.parseInt(m.group(2));
            if (count < 1 || count > 1000 || sides < 1 || sides > 1_000_000) {
                return "";
            }
            long total = 0;
            for (int i = 0; i < count; i++) {
                total += templateContext.getRandom().nextInt(sides) + 1;
            }
            if (m.group(3) != null) {
                long modifier = Long.parseLong(m.group(4));
                total += "-".equals(m.group(3)) ? -modifier : modifier;
            }
            return String.valueOf(total);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Runtime state
    // ------------------------------------------------------------------------------------------------------------

    public String maxContextTokens() {
        return templateContext.getMaxContextTokens() == null ? "" : String.valueOf(templateContext.getMaxContextTokens());
    }

    public String maxResponseTokens() {
        return templateContext.getMaxResponseTokens() == null ? "" : String.valueOf(templateContext.getMaxResponseTokens());
    }

    public String maxPrompt() {
        Integer context = templateContext.getMaxContextTokens();
        if (context == null) {
            return "";
        }
        Integer response = templateContext.getMaxResponseTokens();
        return String.valueOf(context - (response == null ? 0 : response));
    }

    public String model() {
        return Objects.toString(templateContext.getModelName(), "");
    }

    public String lastGenerationType() {
        return Objects.toString(templateContext.getGenerationType(), "");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------------------------------------------------

    private static int count(String value, int defaultCount) {
        if (value == null || value.isBlank()) {
            return defaultCount;
        }
        try {
            return Math.clamp(Integer.parseInt(value.trim()), 0, 1000);
        } catch (NumberFormatException e) {
            return defaultCount;
        }
    }

    public String newline(String count) {
        return "\n".repeat(count(count, 1));
    }

    public String space(String count) {
        return " ".repeat(count(count, 1));
    }

    public String noop() {
        return "";
    }

    public String trim() {
        return TRIM_MARKER;
    }

    public String reverse(String text) {
        return text == null ? "" : new StringBuilder(text).reverse().toString();
    }
}
