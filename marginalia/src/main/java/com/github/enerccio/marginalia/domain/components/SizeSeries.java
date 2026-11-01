package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;

import java.time.LocalDate;
import java.util.*;

/**
 * The size of one book, or of all the books together, on each day of a range, made of the rows of {@code ManuscriptSize}: a day
 * without a row has the size of the last row before it, so the slope between two days is the growth. The same rules as
 * {@code ManuscriptSizeService#growth}: a book first seen as a baseline has no growth on that day, a book born inside the range
 * grows from nothing.
 */
public final class SizeSeries {

    private SizeSeries() {
    }

    /**
     * One day.
     *
     * @param present      whether any book was known by this day, the size of a day before that is zero and not a measurement
     * @param growthKnown  false on a day a book was first seen as a baseline (the growth of that book is left out of the numbers)
     * @param branchChanged some book had another active leaf than on the day before, so the branch values are not comparable
     */
    public record Point(LocalDate day, boolean present, long bookWords, long bookTokens, long branchWords, long branchTokens,
                        long growthWords, long growthTokens, long branchGrowthWords, long branchGrowthTokens,
                        boolean growthKnown, boolean branchChanged) {
    }

    /**
     * The size on each day from-to.
     *
     * @param before the latest row of each book before {@code from}
     * @param range  the rows of the days from-to
     */
    public static List<Point> build(Collection<ManuscriptSize> before, Collection<ManuscriptSize> range, LocalDate from, LocalDate to) {
        Map<String, ManuscriptSize> current = new HashMap<>();
        for (ManuscriptSize row : before) {
            current.merge(row.getBookUuid(), row, (a, b) -> b.getDay().isBefore(a.getDay()) ? a : b);
        }
        Map<LocalDate, List<ManuscriptSize>> byDay = new HashMap<>();
        for (ManuscriptSize row : range) {
            byDay.computeIfAbsent(row.getDay(), _ -> new ArrayList<>()).add(row);
        }
        List<Point> points = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            long growthWords = 0, growthTokens = 0, branchGrowthWords = 0, branchGrowthTokens = 0;
            boolean known = true, branchChanged = false;
            for (ManuscriptSize row : byDay.getOrDefault(day, List.of())) {
                ManuscriptSize previous = current.get(row.getBookUuid());
                if (previous == null && row.isBaseline()) {
                    known = false;
                    previous = row;
                }
                growthWords += row.getBookWords() - (previous == null ? 0 : previous.getBookWords());
                growthTokens += row.getBookTokens() - (previous == null ? 0 : previous.getBookTokens());
                branchGrowthWords += row.getBranchWords() - (previous == null ? 0 : previous.getBranchWords());
                branchGrowthTokens += row.getBranchTokens() - (previous == null ? 0 : previous.getBranchTokens());
                if (previous != null && !Objects.equals(previous.getLeafUuid(), row.getLeafUuid())) {
                    branchChanged = true;
                }
                current.put(row.getBookUuid(), row);
            }
            long bookWords = 0, bookTokens = 0, branchWords = 0, branchTokens = 0;
            for (ManuscriptSize row : current.values()) {
                bookWords += row.getBookWords();
                bookTokens += row.getBookTokens();
                branchWords += row.getBranchWords();
                branchTokens += row.getBranchTokens();
            }
            points.add(new Point(day, !current.isEmpty(), bookWords, bookTokens, branchWords, branchTokens, growthWords,
                    growthTokens, branchGrowthWords, branchGrowthTokens, known, branchChanged));
        }
        return points;
    }

    /**
     * How much each book of the rows grew in the range: the latest row minus the latest row before it. A book without a row before
     * the range grew from nothing, unless its first row is a baseline (the growth is then unknown, or the growth after that row).
     * Only the books that have a row in the range or before it are in the map.
     */
    public static Map<String, SizeGrowth> perBook(Collection<ManuscriptSize> before, Collection<ManuscriptSize> range) {
        Map<String, ManuscriptSize> start = new HashMap<>();
        for (ManuscriptSize row : before) {
            start.merge(row.getBookUuid(), row, (a, b) -> b.getDay().isBefore(a.getDay()) ? a : b);
        }
        Map<String, List<ManuscriptSize>> rows = new LinkedHashMap<>();
        range.stream().sorted(Comparator.comparing(ManuscriptSize::getDay)).forEach(row ->
                rows.computeIfAbsent(row.getBookUuid(), _ -> new ArrayList<>()).add(row));
        Map<String, SizeGrowth> result = new LinkedHashMap<>();
        for (String uuid : start.keySet()) {
            result.put(uuid, SizeGrowth.ZERO);
        }
        for (Map.Entry<String, List<ManuscriptSize>> entry : rows.entrySet()) {
            ManuscriptSize end = entry.getValue().getLast();
            ManuscriptSize first = start.get(entry.getKey());
            if (first == null && entry.getValue().getFirst().isBaseline()) {
                first = entry.getValue().getFirst();
            }
            if (first != null && first == end && end.isBaseline() || first == null && end.isBaseline()) {
                result.put(entry.getKey(), SizeGrowth.UNKNOWN);
                continue;
            }
            result.put(entry.getKey(), new SizeGrowth(end.getBookWords() - (first == null ? 0 : first.getBookWords()),
                    end.getBookTokens() - (first == null ? 0 : first.getBookTokens()),
                    end.getBranchWords() - (first == null ? 0 : first.getBranchWords()),
                    end.getBranchTokens() - (first == null ? 0 : first.getBranchTokens()), true));
        }
        return result;
    }

    /**
     * The growth of all the books together: the sum of those that are known.
     */
    public static SizeGrowth sum(Collection<SizeGrowth> growths) {
        long bookWords = 0, bookTokens = 0, branchWords = 0, branchTokens = 0;
        for (SizeGrowth growth : growths) {
            if (growth.known()) {
                bookWords += growth.bookWords();
                bookTokens += growth.bookTokens();
                branchWords += growth.branchWords();
                branchTokens += growth.branchTokens();
            }
        }
        return new SizeGrowth(bookWords, bookTokens, branchWords, branchTokens, true);
    }

}
