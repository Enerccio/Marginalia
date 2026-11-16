package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.AnnotationTemplate;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The phrase watch list: how the lists of the user, the folders and the book add up, and the annotations a scan makes.
 */
class PhraseWatchServiceTest extends MarginaliaTestBase {

    private static final String TEXT = "Her eyes sparkled. A shiver ran down her spine, and her eyes sparkled again.";

    @Autowired
    private PhraseWatchService phraseWatchService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        part = chatMessageService.createRoot(book, message);
        book.setActiveLeaf(part);
        book = manuscriptService.save(book);
    }

    private static WatchedPhrase phrase(String text, String template) {
        return new WatchedPhrase(text, false, template);
    }

    private void userSettings(List<WatchedPhrase> phrases, List<AnnotationTemplate> templates) throws Exception {
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setWatchedPhrases(phrases);
        settings.setAnnotationTemplates(templates);
        settingService.save(settings);
    }

    private List<String> phrases(List<PhraseWatchService.Effective> entries) {
        return entries.stream().map(entry -> entry.phrase().getPhrase()).toList();
    }

    @Test
    void theListsOfTheUserTheFoldersAndTheBookAddUpAndANearerEntryReplacesTheOneAbove() throws Exception {
        AnnotationTemplate style = new AnnotationTemplate("style", "Cliché: {{match}}", "style", false);
        userSettings(List.of(phrase("eyes sparkled", null), phrase("shiver ran down", null), phrase("smirk", null)), List.of(style));
        Folder series = folderService.create(uniqueName("series"), null);
        Folder volume = folderService.create(uniqueName("volume"), series);
        // the series gives the sparkle a template, the volume switches the smirk off
        phraseWatchService.setForFolder(series, List.of(phrase("Eyes  Sparkled", style.getUuid()), phrase("orbs", null)));
        WatchedPhrase off = phrase("smirk", null);
        off.setDisabled(true);
        phraseWatchService.setForFolder(volume, List.of(off));
        book = manuscriptService.update(book, stored -> stored.setFolder(folderService.find(volume.getId())));
        phraseWatchService.setForBook(book, List.of(phrase("tapestry", null)), true);

        List<PhraseWatchService.Effective> resolved = phraseWatchService.resolve(book);

        assertThat(phrases(resolved)).containsExactly("shiver ran down", "Eyes  Sparkled", "orbs", "tapestry");
        assertThat(resolved.get(1).origin()).isEqualTo(EffectiveSetting.Origin.FOLDER);
        assertThat(resolved.get(1).folder().getId()).isEqualTo(series.getId());
        assertThat(resolved.get(1).phrase().getTemplate()).isEqualTo(style.getUuid());
        assertThat(resolved.get(0).origin()).isEqualTo(EffectiveSetting.Origin.USER);
        assertThat(resolved.get(3).origin()).isEqualTo(EffectiveSetting.Origin.BOOK);
        assertThat(phrases(phraseWatchService.resolveInherited(volume))).containsExactly("shiver ran down", "Eyes  Sparkled", "orbs");
        assertThat(phrases(phraseWatchService.resolveInherited(null))).containsExactly("eyes sparkled", "shiver ran down", "smirk");
        assertThat(manuscriptService.find(book.getId()).getPhraseWatch()).isTrue();
    }

    @Test
    void aScanAnnotatesEveryHitWithItsTemplateAndASecondScanAddsNothing() throws Exception {
        AnnotationTemplate style = new AnnotationTemplate("style", "Cliché \"{{match}}\" ({{phrase}})", "style", true);
        userSettings(List.of(), List.of(style));
        phraseWatchService.setForBook(book, List.of(phrase("eyes sparkled", style.getUuid()), phrase("shiver ran down", null)), true);

        List<Annotation> made = phraseWatchService.scan(book, part);

        assertThat(made).hasSize(3);
        Annotation first = made.getFirst();
        assertThat(first.getSource()).isEqualTo(AnnotationSource.PHRASE);
        assertThat(first.getLabel()).isEqualTo("eyes sparkled");
        assertThat(first.getText()).isEqualTo("Cliché \"eyes sparkled\" (eyes sparkled)");
        assertThat(first.getType()).isEqualTo("style");
        assertThat(first.isSendToPrompt()).isTrue();
        assertThat(TEXT.substring(first.getStart(), first.getEnd())).isEqualTo("eyes sparkled");
        Annotation builtIn = made.get(1);
        assertThat(builtIn.getLabel()).isEqualTo("shiver ran down");
        assertThat(builtIn.getText()).contains("shiver ran down");
        assertThat(builtIn.getType()).isNull();
        assertThat(builtIn.isSendToPrompt()).isFalse();

        // the author dismisses one: it stays dismissed and is not made again
        annotationService.setStatus(book, made.get(2), AnnotationStatus.DISMISSED);
        assertThat(phraseWatchService.scan(book, part)).isEmpty();
        assertThat(annotationService.listForPart(book, part)).hasSize(3);
    }

    @Test
    void theScanOfTheStoryAnnotatesThePartsOfTheActiveBranch() throws Exception {
        ChatMessage second = new ChatMessage();
        second.setResponse("The tapestry of fate. No sparkle here.");
        second = chatMessageService.addChild(part, second);
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setActiveLeaf(second);
        book = manuscriptService.save(stored);
        userSettings(List.of(new WatchedPhrase("tapestry|sparkled", true, null)), List.of());

        assertThat(phraseWatchService.scanBranch(book)).isEqualTo(3);
        assertThat(annotationService.listForPart(book, second)).extracting(Annotation::getQuote).containsExactly("tapestry");
        assertThat(phraseWatchService.scanBranch(book)).isZero();
    }

    @Test
    void anEntryThatCanNotBeUsedIsRefused() throws Exception {
        assertThatThrownBy(() -> phraseWatchService.setForBook(book, List.of(new WatchedPhrase("(open", true, null)), true))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("(open");
        // blank entries are dropped, the phrase is trimmed
        Manuscript saved = phraseWatchService.setForBook(book, List.of(phrase("  ", null), phrase("  smirk ", null)), false);
        assertThat(saved.getWatchedPhrases()).extracting(WatchedPhrase::getPhrase).containsExactly("smirk");
        assertThat(saved.getPhraseWatch()).isFalse();
    }
}
