package com.github.enerccio.marginalia.extensions.audiobook.script;

/** English words for numbers: cardinals ("one hundred twenty-three"), ordinals ("twenty-first") and the way a year is read ("nineteen oh five"). */
final class NumberWords {

    private static final String[] SMALL = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
            "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] TENS = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    private static final String[] SCALES = {"", " thousand", " million", " billion", " trillion"};

    /** The largest number that is read as a number; a longer one is read digit by digit. */
    static final long MAX = 999_999_999_999_999L;

    private NumberWords() {
    }

    static String cardinal(long n) {
        if (n < 0) {
            return "minus " + cardinal(-n);
        }
        if (n < 20) {
            return SMALL[(int) n];
        }
        StringBuilder out = new StringBuilder();
        int scale = 0;
        long rest = n;
        while (rest > 0) {
            int group = (int) (rest % 1000);
            if (group != 0) {
                String words = below1000(group) + SCALES[scale];
                out.insert(0, out.isEmpty() ? words : words + " ");
            }
            rest /= 1000;
            scale++;
        }
        return out.toString();
    }

    private static String below1000(int n) {
        StringBuilder out = new StringBuilder();
        if (n >= 100) {
            out.append(SMALL[n / 100]).append(" hundred");
            n %= 100;
            if (n > 0) {
                out.append(' ');
            }
        }
        if (n >= 20) {
            out.append(TENS[n / 10]);
            if (n % 10 > 0) {
                out.append('-').append(SMALL[n % 10]);
            }
        } else if (n > 0) {
            out.append(SMALL[n]);
        }
        return out.toString();
    }

    static String ordinal(long n) {
        String words = cardinal(n);
        int cut = Math.max(words.lastIndexOf(' '), words.lastIndexOf('-')) + 1;
        String last = words.substring(cut);
        String ordinal = switch (last) {
            case "one" -> "first";
            case "two" -> "second";
            case "three" -> "third";
            case "five" -> "fifth";
            case "eight" -> "eighth";
            case "nine" -> "ninth";
            case "twelve" -> "twelfth";
            default -> last.endsWith("y") ? last.substring(0, last.length() - 1) + "ieth" : last + "th";
        };
        return words.substring(0, cut) + ordinal;
    }

    /** A year as people say it: 1905 "nineteen oh five", 1900 "nineteen hundred", 2005 "two thousand five", 2024 "twenty twenty-four". */
    static String year(int y) {
        if (y >= 2000 && y < 2010) {
            return y == 2000 ? "two thousand" : "two thousand " + SMALL[y - 2000];
        }
        int high = y / 100;
        int low = y % 100;
        if (low == 0) {
            return cardinal(high) + " hundred";
        }
        return cardinal(high) + (low < 10 ? " oh " + SMALL[low] : " " + cardinal(low));
    }

    /** The digits one by one ("zero zero seven"). */
    static String digits(String digits) {
        StringBuilder out = new StringBuilder();
        for (char c : digits.toCharArray()) {
            if (c >= '0' && c <= '9') {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(SMALL[c - '0']);
            }
        }
        return out.toString();
    }
}
