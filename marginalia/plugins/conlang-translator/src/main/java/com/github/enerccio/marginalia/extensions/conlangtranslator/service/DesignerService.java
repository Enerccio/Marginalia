package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.Proposal;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.ProposalBuilder;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Origin;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.ModelSelection;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The designer: asks the model for what the author wants of a language (new words, a section of the rules, a review, an answer, the proposal of a
 * stage, sample translations) and turns the answer into a {@link Proposal} that the author reviews. It never writes into a language; what is
 * accepted is written by the window that asked. The UI calls this; there is no logic in a dialog.
 */
@Configurable
public class DesignerService {

    /** The most of the lorebook that goes into a request (characters). */
    public static final int MAX_LORE_CHARS = 20000;

    /** The designer cannot go on. */
    public static class DesignerException extends Exception {
        public DesignerException(String message) {
            super(message);
        }
    }

    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private LorebookEntryService lorebookEntryService;

    private final ModelClient client = new ModelClient();
    private final LanguageLibrary library = new LanguageLibrary();

    /**
     * The entries of the example lorebook of the language as text, the people and the places the language is for; empty when it has none or the
     * lorebook is gone.
     */
    public String lore(Language language) throws Exception {
        if (language.getExampleLorebook() == null) {
            return "";
        }
        Lorebook lorebook = lorebookService.findForUser(language.getExampleLorebook());
        if (lorebook == null || lorebook.isDeleted()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(lorebook)) {
            if (entry.isDeleted() || entry.getName() == null || entry.getName().isBlank()) {
                continue;
            }
            String line = entry.getName().strip() + ": " + (entry.getPayload() == null ? "" : entry.getPayload().strip().replaceAll("\\s+", " ")) + "\n";
            if (text.length() + line.length() > MAX_LORE_CHARS) {
                break;
            }
            text.append(line);
        }
        return text.toString();
    }

    /**
     * The model of the designer: the one of its own setting, else the translation's, else the default of the user.
     */
    public ModelSelection model() throws Exception {
        UserDefaults defaults = library.defaults();
        String ai = defaults.getDesignerAiUuid() != null ? defaults.getDesignerAiUuid() : defaults.getAiUuid();
        String protocol = defaults.getDesignerProtocolUuid() != null ? defaults.getDesignerProtocolUuid() : defaults.getProtocolUuid();
        ModelSelection model = client.choose(ai, protocol);
        if (model == null) {
            throw new DesignerException("There is no model for the designer: choose one in the settings of the plugin, or set a default model.");
        }
        return model;
    }

    /**
     * Asks the model and reads the answer.
     *
     * @throws DesignerException when there is no model or the answer is not the object that was asked for
     */
    public DesignerAnswer answer(Request request) throws Exception {
        ModelSelection model = model();
        String raw = client.ask(model, null, request.system(), request.user());
        DesignerAnswer answer = DesignerAnswer.parse(raw);
        if (answer == null) {
            throw new DesignerException("The model did not answer in the expected form.");
        }
        return answer;
    }

    /**
     * Asks the model and makes a proposal for the language of the answer.
     */
    public Proposal propose(Language language, Request request) throws Exception {
        ModelSelection model = model();
        String raw = client.ask(model, null, request.system(), request.user());
        DesignerAnswer answer = DesignerAnswer.parse(raw);
        if (answer == null) {
            throw new DesignerException("The model did not answer in the expected form.");
        }
        return ProposalBuilder.build(language, answer, Origin.model(null, model.ai().getName(), System.currentTimeMillis()));
    }

    /**
     * The proposal for an answer that was asked for already (the conversation reads the answer first).
     */
    public Proposal proposal(Language language, DesignerAnswer answer) {
        String name = modelName();
        return ProposalBuilder.build(language, answer, Origin.model(null, name == null ? "" : name, System.currentTimeMillis()));
    }

    /** The name of the model the designer would use, for the window to say. */
    public String modelName() {
        try {
            return model().ai().getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The words of an answer, for the window that shows samples.
     */
    public static List<DesignerAnswer.Sample> samples(DesignerAnswer answer) {
        return answer.samples();
    }
}
