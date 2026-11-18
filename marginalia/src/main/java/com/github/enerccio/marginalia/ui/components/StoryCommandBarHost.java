package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.GenerationView;
import com.github.enerccio.marginalia.domain.service.command.BaseCommandScope;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The {@link CommandBar} of a story: its commands run in the scope of the session that shows their generations in the story
 * ({@link CommandScopes#session(GenerationView)}), and a command that generates holds the book of the story until its generation
 * starts.
 */
@Configurable(preConstruction = true)
public class StoryCommandBarHost implements CommandBarHost {

    @Autowired
    private CommandScopes commandScopes;

    private final GenerationView view;

    public StoryCommandBarHost(GenerationView view) {
        this.view = view;
    }

    /** The story that shows the generations. */
    public GenerationView getView() {
        return view;
    }

    @Override
    public BaseCommandScope scope() {
        return commandScopes.session(view);
    }

    @Override
    public Runs.Run holdForGeneration(String humanName) {
        Manuscript book = view.getBook();
        if (book == null || book.getId() == null) {
            return null;
        }
        try {
            return Runs.run(Runs.GENERATION_COMMAND, () -> humanName).book(book).quiet().currentPage()
                    .startUnless(book, Runs.Operation.GENERATE);
        } catch (Runs.HeldException e) {
            return null;
        }
    }
}
