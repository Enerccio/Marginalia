package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.FolderService.Transfer;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The name and the folder path of a book are copied into every row of its statistics, so a book that is gone can be shown. A rename or
 * a move of the book or of a folder above it changes them in all three tables; a book in the trash or gone keeps the last ones; a
 * clone, an import and a restore of a backup are no work of the author and book nothing.
 */
class BookDataStatisticsTest extends AgentTestBase {

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private ChatMessageService chatMessageService;

    private LocalDate today;
    private LocalDate earlier;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        today = statisticService.today();
        earlier = today.minusDays(3);
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(true);
    }

    @AfterEach
    void restore() {
        ManuscriptSizeServiceImpl impl = (ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService);
        impl.flush();
        impl.setManual(false);
    }

    private Folder folder(String name, Folder parent) throws Exception {
        return folderService.create(name + "-" + UUID.randomUUID().toString().substring(0, 8), parent);
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    /** One row in each of the three tables, today and three days ago. */
    private void rows(Manuscript book) throws Exception {
        Manuscript stored = manuscriptService.find(book.getId());
        statisticService.count(stored, StatisticCounts.tokens(10, 5));
        intervalService.start(stored, ActivityKind.OPEN, false, new Date());
        Date old = Date.from(earlier.atTime(10, 0).atZone(java.time.ZoneId.systemDefault()).toInstant());
        intervalService.start(stored, ActivityKind.OPEN, false, old);
        sizeService.set(stored, today, 3, 4, 3, 4, null, false);
        sizeService.set(stored, earlier, 1, 2, 1, 2, null, false);
    }

    /** "name | path" of every row of the book in the three tables. */
    private Set<String> names(Manuscript book) throws Exception {
        Set<String> names = new TreeSet<>();
        for (ManuscriptStatistic s : statisticService.findRange(StatisticFilter.of(earlier, today, StatisticSide.ALL).forBook(book.getUuid()))) {
            names.add("statistic: " + s.getBookName() + " | " + s.getFolderPath());
        }
        for (ActivityInterval i : intervalService.findAllForUser()) {
            if (book.getUuid().equals(i.getBookUuid())) {
                names.add("interval: " + i.getBookName() + " | " + i.getFolderPath());
            }
        }
        for (ManuscriptSize s : sizeService.findRange(book.getUuid(), earlier, today)) {
            names.add("size: " + s.getBookName() + " | " + s.getFolderPath());
        }
        return names;
    }

    private static Set<String> expected(String name, String path) {
        return new TreeSet<>(Set.of("statistic: " + name + " | " + path, "interval: " + name + " | " + path, "size: " + name + " | " + path));
    }

    @Test
    void renamingTheBookRenamesAllItsRows() throws Exception {
        Folder f = folder("Sci-fi", null);
        Manuscript book = book("Old name", f);
        rows(book);
        assertThat(names(book)).isEqualTo(expected("Old name", f.getName()));

        Manuscript stored = manuscriptService.find(book.getId());
        stored.setName("New name");
        manuscriptService.save(stored);

        assertThat(names(book)).isEqualTo(expected("New name", f.getName()));
    }

    @Test
    void movingTheBookChangesThePathOfAllItsRows() throws Exception {
        Folder from = folder("From", null);
        Folder inner = folder("Inner", from);
        Folder to = folder("To", null);
        Manuscript book = book("Book", inner);
        rows(book);
        assertThat(names(book)).isEqualTo(expected("Book", from.getName() + "/" + inner.getName()));

        manuscriptService.moveToFolder(List.of(book), to);
        assertThat(names(book)).isEqualTo(expected("Book", to.getName()));

        manuscriptService.moveToFolder(List.of(book), null);
        assertThat(names(book)).isEqualTo(expected("Book", ""));
    }

    @Test
    void savingTheBookIntoAnotherFolderChangesThePathToo() throws Exception {
        Folder from = folder("From", null);
        Folder to = folder("To", null);
        Manuscript book = book("Book", from);
        rows(book);

        Manuscript stored = manuscriptService.find(book.getId());
        stored.setFolder(to);
        manuscriptService.save(stored);

        assertThat(names(book)).isEqualTo(expected("Book", to.getName()));
    }

    @Test
    void renamingAndMovingAFolderChangesTheBooksTwoLevelsDown() throws Exception {
        Folder top = folder("Top", null);
        Folder middle = folder("Middle", top);
        Folder bottom = folder("Bottom", middle);
        Manuscript deep = book("Deep", bottom);
        Manuscript shallow = book("Shallow", middle);
        Folder other = folder("Other", null);
        Manuscript elsewhere = book("Elsewhere", other);
        rows(deep);
        rows(shallow);
        rows(elsewhere);
        String elsewherePath = other.getName();

        folderService.rename(top, "Renamed-" + UUID.randomUUID().toString().substring(0, 6));
        String renamed = folderService.find(top.getId()).getName();
        assertThat(names(deep)).isEqualTo(expected("Deep", renamed + "/" + middle.getName() + "/" + bottom.getName()));
        assertThat(names(shallow)).isEqualTo(expected("Shallow", renamed + "/" + middle.getName()));
        assertThat(names(elsewhere)).isEqualTo(expected("Elsewhere", elsewherePath));

        Folder newParent = folder("NewParent", null);
        folderService.move(middle, newParent);
        assertThat(names(deep)).isEqualTo(expected("Deep", newParent.getName() + "/" + middle.getName() + "/" + bottom.getName()));
        assertThat(names(shallow)).isEqualTo(expected("Shallow", newParent.getName() + "/" + middle.getName()));
    }

    @Test
    void theDetailsDialogRenameOfAFolderChangesThePathsToo() throws Exception {
        Folder f = folder("Before", null);
        Manuscript book = book("Book", f);
        rows(book);

        String name = "After-" + UUID.randomUUID().toString().substring(0, 6);
        folderService.update(f, new FolderService.Details(name, "", null, Map.of()));

        assertThat(names(book)).isEqualTo(expected("Book", name));
    }

    @Test
    void deletingAFolderMovesItsContentUpAndTheirPathsWith() throws Exception {
        Folder top = folder("Top", null);
        Folder middle = folder("Middle", top);
        Folder bottom = folder("Bottom", middle);
        Manuscript inMiddle = book("InMiddle", middle);
        Manuscript inBottom = book("InBottom", bottom);
        rows(inMiddle);
        rows(inBottom);

        folderService.delete(middle, false);

        assertThat(names(inMiddle)).isEqualTo(expected("InMiddle", top.getName()));
        assertThat(names(inBottom)).isEqualTo(expected("InBottom", top.getName() + "/" + bottom.getName()));
    }

    @Test
    void deletingAFolderWithItsContentLeavesTheRowsAsTheyWere() throws Exception {
        Folder f = folder("Doomed", null);
        Manuscript book = book("Book", f);
        rows(book);

        folderService.deleteWithContent(f);

        assertThat(names(book)).isEqualTo(expected("Book", f.getName()));
    }

    @Test
    void aBookInTheTrashKeepsItsLastNameAndPath() throws Exception {
        Folder f = folder("Home", null);
        Manuscript book = book("Book", f);
        Manuscript alive = book("Alive", f);
        rows(book);
        rows(alive);

        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        String renamed = "Renamed-" + UUID.randomUUID().toString().substring(0, 6);
        folderService.rename(f, renamed);

        assertThat(names(book)).as("the book is in the trash").isEqualTo(expected("Book", f.getName()));
        assertThat(names(alive)).isEqualTo(expected("Alive", renamed));
    }

    @Test
    void theOrganiseDialogMovesAFolderAndItsBooks() throws Exception {
        Folder source = folder("Source", null);
        Folder target = folder("Target", null);
        Manuscript inSource = book("InSource", source);
        Manuscript loose = book("Loose", null);
        rows(inSource);
        rows(loose);

        folderService.transfer(new Transfer(List.of(loose), List.of(source), target, false, false, Map.of()), () -> {
        });

        assertThat(names(inSource)).isEqualTo(expected("InSource", target.getName() + "/" + source.getName()));
        assertThat(names(loose)).isEqualTo(expected("Loose", target.getName()));
    }

    @Test
    void aStaleCopySavedWithTheOldNameMakesTheRowsFollowTheStoredName() throws Exception {
        Manuscript book = book("First", null);
        rows(book);
        Manuscript stale = manuscriptService.find(book.getId());

        Manuscript renamed = manuscriptService.find(book.getId());
        renamed.setName("Second");
        manuscriptService.save(renamed);
        assertThat(names(book)).isEqualTo(expected("Second", ""));

        // counting for the old copy does not bring the old name back
        statisticService.count(stale, StatisticCounts.tokens(1, 1));
        assertThat(names(book).stream().filter(n -> n.startsWith("interval") || n.startsWith("size"))).containsExactlyInAnyOrder(
                "interval: Second | ", "size: Second | ");

        // saving it does: the stored name is the old one again, and the rows follow the stored name
        manuscriptService.save(stale);
        assertThat(names(book)).isEqualTo(expected("First", ""));
    }

    @Test
    void aRenameThatNobodyAnnouncedIsFoundByTheNextWriter() throws Exception {
        Manuscript book = book("Before", null);
        rows(book);
        Manuscript stored = manuscriptService.find(book.getId());

        stored.setName("After");
        // no refresh: the way a save path that was missed would do it
        manuscriptService.saveWithoutEvent(stored);
        assertThat(names(book)).contains("interval: Before | ", "size: Before | ");

        statisticService.count(manuscriptService.find(book.getId()), StatisticCounts.tokens(1, 1));

        assertThat(names(book)).isEqualTo(expected("After", ""));
    }

    @Test
    void duplicatingAndBranchingIntoANewBookBookNothingAndTheSourceKeepsItsRows() throws Exception {
        Manuscript book = book("Original", null);
        chatMessageService.createPart(manuscriptService.find(book.getId()), null, "one two three", null);
        rows(book);
        StatisticCounts before = total(book);

        Manuscript copy = backupService.cloneManuscript(manuscriptService.find(book.getId()), "Copy", false);
        Manuscript branch = backupService.cloneManuscript(manuscriptService.find(book.getId()), "Branch", true);

        assertThat(total(copy)).isEqualTo(StatisticCounts.ZERO);
        assertThat(total(branch)).isEqualTo(StatisticCounts.ZERO);
        assertThat(total(book)).isEqualTo(before);
        assertThat(intervalService.findRows(StatisticFilter.of(earlier, today, StatisticSide.ALL), null))
                .noneMatch(r -> r.bookUuid().equals(copy.getUuid()) || r.bookUuid().equals(branch.getUuid()));
    }

    @Test
    void cloningFromABackupBooksNothingAndTheBackupHasNoStatistics() throws Exception {
        Manuscript book = book("Original", null);
        chatMessageService.createPart(manuscriptService.find(book.getId()), null, "one two three", null);
        rows(book);

        JsonObject backup = manuscriptService.createBackup(manuscriptService.find(book.getId()));
        String json = backup.toString().toLowerCase();
        assertThat(json).doesNotContain("statistic").doesNotContain("activityinterval").doesNotContain("manuscriptsize")
                .doesNotContain("bookuuid");

        Manuscript clone = manuscriptService.cloneFromBackup(backup);

        assertThat(total(clone)).isEqualTo(StatisticCounts.ZERO);
    }

    @Test
    void restoringABackupCountsNothingAndLeavesTheRows() throws Exception {
        Manuscript book = book("Original", null);
        ChatMessage first = chatMessageService.createPart(manuscriptService.find(book.getId()), null, "one two three", null);
        JsonObject backup = manuscriptService.createBackup(manuscriptService.find(book.getId()));
        chatMessageService.createPart(manuscriptService.find(book.getId()), first, "four five", null);
        rows(book);
        StatisticCounts before = total(book);
        Set<String> names = names(book);

        manuscriptService.restoreBackup(manuscriptService.find(book.getId()), true, backup);

        assertThat(total(book)).isEqualTo(before);
        assertThat(names(book)).isEqualTo(names);
    }

    @Test
    void importingBooksNothing() throws Exception {
        Manuscript book = book("Imported", null);

        com.github.enerccio.marginalia.utils.MarkdownArchive.Content archive = com.github.enerccio.marginalia.utils.MarkdownArchive.read(zip());
        archiveImportService.appendParts(manuscriptService.find(book.getId()), archive, () -> false, () -> {
        });

        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).hasSize(2);
        assertThat(total(book)).isEqualTo(StatisticCounts.ZERO);
    }

    @Autowired
    private ArchiveImportService archiveImportService;

    @Autowired
    private SillyTavernImportService sillyTavernImportService;

    @Test
    void importingASillyTavernChatBooksNothing() throws Exception {
        Manuscript book = book("Imported", null);
        com.github.enerccio.marginalia.utils.SillyTavernChats.Chat chat = com.github.enerccio.marginalia.utils.SillyTavernChats.parse(
                "{\"user_name\":\"Me\",\"character_name\":\"C\",\"chat_metadata\":{}}\n"
                        + "{\"name\":\"x\",\"is_user\":true,\"is_system\":false,\"send_date\":\"2026-01-01T10:00:00.000Z\",\"mes\":\"hello there friend\",\"extra\":{}}\n"
                        + "{\"name\":\"x\",\"is_user\":false,\"is_system\":false,\"send_date\":\"2026-01-01T10:00:01.000Z\",\"mes\":\"hi\",\"extra\":{\"model\":\"m\"}}",
                "chat");

        sillyTavernImportService.importTree(manuscriptService.find(book.getId()),
                com.github.enerccio.marginalia.utils.SillyTavernChats.merge(List.of(chat), false), () -> false, () -> {
                });

        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).hasSize(2);
        assertThat(total(book)).isEqualTo(StatisticCounts.ZERO);
    }

    private static byte[] zip() throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(out)) {
            for (String file : List.of("1.md", "2.md")) {
                zip.putNextEntry(new java.util.zip.ZipEntry(file));
                zip.write(("text of " + file).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private StatisticCounts total(Manuscript book) throws Exception {
        return statisticService.total(StatisticFilter.of(earlier, today, StatisticSide.ALL).forBook(book.getUuid()));
    }
}
