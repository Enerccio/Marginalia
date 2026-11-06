package com.github.enerccio.marginalia.domain.templates.macros;

import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The expressions of {@code {{eval::expression}}}: a comparison, or several joined by {@code and}, {@code or}, {@code not}
 * and parentheses, that gives true or false.
 * <pre>
 *   or        a or b       a || b
 *   and       a and b      a &amp;&amp; b
 *   not       not a        !a
 *   compare   a == b   a != b   a &lt; b   a &lt;= b   a &gt; b   a &gt;= b   a ~= b   a contains b   a has b
 *   operand   "text"  'text'  12  -3.5  true  false  name  .variable  $variable  ( expression )
 * </pre>
 * Everything is text. {@code ==} and {@code !=} compare numbers by value when both sides are numbers ({@code 5 == 5.0}) and
 * otherwise the exact text (case matters); {@code ~=} is the same text ignoring case; the ordering ones compare numbers (false
 * when a side is not a number); {@code contains} looks for a text inside another one, ignoring case; {@code has} looks for an
 * item in a list separated by commas, semicolons or lines, ignoring case and the spaces around the items. An operand alone is true
 * when it is not empty, {@code 0}, {@code false}, {@code no} or {@code off}.
 * <p>
 * A {@code name} is a value the template knows ({@code povCharacter}, {@code presentCharacters}, a macro without arguments such as
 * {@code summary}), {@code .name} a local variable and {@code $name} a global one. A text is always in quotes: a name that is not
 * known is an error, not a text, so a typo cannot make an expression true by accident. All the names are checked when the expression is
 * read, also in the part of an {@code and} / {@code or} that is not evaluated.
 */
public final class ExpressionEvaluator {

    /**
     * What the names of an expression mean.
     */
    public interface Environment {

        /**
         * Whether the name is a value the template knows.
         */
        boolean knows(String name);

        /**
         * The value of a known name, empty text for none.
         */
        String value(String name);

        /**
         * The value of a variable, empty text when it is not set.
         */
        String variable(Scope scope, String name);
    }

    /**
     * The expression is not valid: a syntax error or a name that is not known.
     */
    public static final class ExpressionException extends RuntimeException {
        public ExpressionException(String message) {
            super(message);
        }
    }

    private interface Node {
        String value();
    }

    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern WORD = Pattern.compile("[A-Za-z][A-Za-z0-9_-]*");

    private final String source;
    private final Environment environment;
    private final List<String> tokens = new ArrayList<>();
    private final List<Boolean> quoted = new ArrayList<>();
    private int next;

    private ExpressionEvaluator(String source, Environment environment) {
        this.source = source;
        this.environment = environment;
    }

    /**
     * The value of the expression.
     *
     * @throws ExpressionException the expression is not valid
     */
    public static boolean evaluate(String expression, Environment environment) {
        ExpressionEvaluator parser = new ExpressionEvaluator(expression == null ? "" : expression, environment);
        parser.tokenize();
        if (parser.tokens.isEmpty()) {
            throw new ExpressionException("empty expression");
        }
        Node root = parser.parseOr();
        if (parser.next < parser.tokens.size()) {
            throw new ExpressionException("unexpected '" + parser.tokens.get(parser.next) + "'");
        }
        return TemplateData.isTruthy(root.value());
    }

    // ------------------------------------------------------------------------------------------------------------
    // tokens
    // ------------------------------------------------------------------------------------------------------------

    private void tokenize() {
        int i = 0;
        int n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '"' || c == '\'') {
                StringBuilder text = new StringBuilder();
                i++;
                boolean closed = false;
                while (i < n) {
                    char d = source.charAt(i);
                    if (d == '\\' && i + 1 < n && (source.charAt(i + 1) == c || source.charAt(i + 1) == '\\')) {
                        text.append(source.charAt(i + 1));
                        i += 2;
                    } else if (d == c) {
                        closed = true;
                        i++;
                        break;
                    } else {
                        text.append(d);
                        i++;
                    }
                }
                if (!closed) {
                    throw new ExpressionException("a text is not closed");
                }
                add(text.toString(), true);
            } else if (startsWithAny(i, "==", "!=", "<=", ">=", "~=", "&&", "||")) {
                add(source.substring(i, i + 2), false);
                i += 2;
            } else if ("<>!()".indexOf(c) >= 0) {
                add(String.valueOf(c), false);
                i++;
            } else if ((c == '.' || c == '$') && i + 1 < n && Character.isLetter(source.charAt(i + 1))) {
                int end = i + 1;
                while (end < n && (Character.isLetterOrDigit(source.charAt(end)) || source.charAt(end) == '_' || source.charAt(end) == '-')) {
                    end++;
                }
                add(source.substring(i, end), false);
                i = end;
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(source.charAt(i + 1)))) {
                int end = i + 1;
                while (end < n && (Character.isDigit(source.charAt(end)) || (source.charAt(end) == '.' && end + 1 < n && Character.isDigit(source.charAt(end + 1))))) {
                    end++;
                }
                add(source.substring(i, end), false);
                i = end;
            } else if (Character.isLetter(c)) {
                int end = i + 1;
                while (end < n && (Character.isLetterOrDigit(source.charAt(end)) || source.charAt(end) == '_' || source.charAt(end) == '-')) {
                    end++;
                }
                add(source.substring(i, end), false);
                i = end;
            } else {
                throw new ExpressionException("unexpected '" + c + "'");
            }
        }
    }

    private boolean startsWithAny(int at, String... options) {
        for (String option : options) {
            if (source.startsWith(option, at)) {
                return true;
            }
        }
        return false;
    }

    private void add(String token, boolean isQuoted) {
        tokens.add(token);
        quoted.add(isQuoted);
    }

    private boolean at(String... options) {
        if (next >= tokens.size() || quoted.get(next)) {
            return false;
        }
        String token = tokens.get(next);
        for (String option : options) {
            if (token.equalsIgnoreCase(option)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------
    // grammar
    // ------------------------------------------------------------------------------------------------------------

    private Node parseOr() {
        Node left = parseAnd();
        while (at("or", "||")) {
            next++;
            Node a = left;
            Node b = parseAnd();
            left = () -> bool(truthy(a) || truthy(b));
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseNot();
        while (at("and", "&&")) {
            next++;
            Node a = left;
            Node b = parseNot();
            left = () -> bool(truthy(a) && truthy(b));
        }
        return left;
    }

    private Node parseNot() {
        if (at("not", "!")) {
            next++;
            Node operand = parseNot();
            return () -> bool(!truthy(operand));
        }
        return parseComparison();
    }

    private Node parseComparison() {
        Node left = parseOperand();
        if (!at("==", "!=", "<", "<=", ">", ">=", "~=", "contains", "has")) {
            return left;
        }
        String operator = tokens.get(next++).toLowerCase(Locale.ROOT);
        Node right = parseOperand();
        return () -> bool(compare(operator, left.value(), right.value()));
    }

    private Node parseOperand() {
        if (next >= tokens.size()) {
            throw new ExpressionException("the expression ends too early");
        }
        String token = tokens.get(next);
        boolean isQuoted = quoted.get(next);
        next++;
        if (isQuoted) {
            return () -> token;
        }
        if (token.equals("(")) {
            Node inner = parseOr();
            if (!at(")")) {
                throw new ExpressionException("a ')' is missing");
            }
            next++;
            return inner;
        }
        if (NUMBER.matcher(token).matches()) {
            return () -> token;
        }
        if (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false")) {
            String value = token.toLowerCase(Locale.ROOT);
            return () -> value;
        }
        if (token.startsWith(".") || token.startsWith("$")) {
            Scope scope = token.startsWith("$") ? Scope.GLOBAL : Scope.LOCAL;
            String name = token.substring(1);
            return () -> Objects.toString(environment.variable(scope, name), "");
        }
        if (WORD.matcher(token).matches()) {
            if (isOperatorWord(token)) {
                throw new ExpressionException("unexpected '" + token + "'");
            }
            if (!environment.knows(token)) {
                throw new ExpressionException("unknown name '" + token + "' (a text is written in quotes)");
            }
            return () -> Objects.toString(environment.value(token), "");
        }
        throw new ExpressionException("unexpected '" + token + "'");
    }

    private static boolean isOperatorWord(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        return lower.equals("and") || lower.equals("or") || lower.equals("not") || lower.equals("contains") || lower.equals("has");
    }

    // ------------------------------------------------------------------------------------------------------------
    // values
    // ------------------------------------------------------------------------------------------------------------

    private static String bool(boolean value) {
        return value ? "true" : "false";
    }

    private static boolean truthy(Node node) {
        return TemplateData.isTruthy(node.value());
    }

    private static BigDecimal number(String text) {
        String t = text == null ? "" : text.strip();
        return NUMBER.matcher(t).matches() ? new BigDecimal(t) : null;
    }

    private static boolean compare(String operator, String left, String right) {
        BigDecimal a = number(left);
        BigDecimal b = number(right);
        return switch (operator) {
            case "==" -> a != null && b != null ? a.compareTo(b) == 0 : left.equals(right);
            case "!=" -> !compare("==", left, right);
            case "~=" -> a != null && b != null ? a.compareTo(b) == 0 : left.equalsIgnoreCase(right);
            case "<" -> a != null && b != null && a.compareTo(b) < 0;
            case "<=" -> a != null && b != null && a.compareTo(b) <= 0;
            case ">" -> a != null && b != null && a.compareTo(b) > 0;
            case ">=" -> a != null && b != null && a.compareTo(b) >= 0;
            case "contains" -> left.toLowerCase(Locale.ROOT).contains(right.toLowerCase(Locale.ROOT));
            case "has" -> hasItem(left, right);
            default -> throw new ExpressionException("unknown operator '" + operator + "'");
        };
    }

    private static boolean hasItem(String list, String item) {
        String wanted = item.strip();
        if (wanted.isEmpty()) {
            return false;
        }
        for (String part : list.split("[,;\\r\\n]")) {
            if (part.strip().equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }
}
