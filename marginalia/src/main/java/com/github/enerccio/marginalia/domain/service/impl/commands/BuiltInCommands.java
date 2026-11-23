package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read.*;
import com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.write.LoreEntryWriteCommands;
import com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.write.LorebookWriteCommands;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.*;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The commands of the application (the book, its lorebooks, backups), registered with the {@link CommandService} when the
 * application starts. The command framework itself knows none of them.
 */
public class BuiltInCommands {

    @Autowired
    private CommandService commandService;

    /** Registers the suites, the init method of the bean. */
    public void install() {
        commandService.registerCommands(new BackupCommands());
        commandService.registerCommands(new SearchReplaceChatMessageCommand());
        commandService.registerCommands(new SearchReplaceLoreEntryCommand());
        registerManuscriptReadCommands();
        registerLorebookReadCommands();
        registerManuscriptWriteCommands();
    }

    private void registerManuscriptWriteCommands() {
        commandService.registerCommands(new PartEditCommands());
        commandService.registerCommands(new PartStructureCommands());
        commandService.registerCommands(new SummaryWriteCommands());
        commandService.registerCommands(new AnnotationWriteCommands());
        commandService.registerCommands(new BookSettingsCommands());
        commandService.registerCommands(new StoryGenerationCommands());
        commandService.registerCommands(new SummaryGenerationCommands());
        commandService.registerCommands(new RedraftCommands());
        commandService.registerCommands(new ImpersonateCommands());
        commandService.registerCommands(new LoreEntryWriteCommands());
        commandService.registerCommands(new LorebookWriteCommands());
        commandService.registerCommands(new PlannerCommands());
    }

    private void registerLorebookReadCommands() {
        commandService.registerCommands(new ListLorebooksCommand());
        commandService.registerCommands(new ListLoreEntriesCommand());
        commandService.registerCommands(new GetLoreEntryCommand());
        commandService.registerCommands(new SearchLoreCommand());
        commandService.registerCommands(new PreviewLoreActivationCommand());
    }

    private void registerManuscriptReadCommands() {
        commandService.registerCommands(new GetBookInfoCommand());
        commandService.registerCommands(new GetBookStatisticsCommand());
        commandService.registerCommands(new GetStatisticsCommand());
        commandService.registerCommands(new ListPartsCommand());
        commandService.registerCommands(new GetPartCommand());
        commandService.registerCommands(new ListSwipesCommand());
        commandService.registerCommands(new ListBranchesCommand());
        commandService.registerCommands(new GetPartMetricsCommand());
        commandService.registerCommands(new ShowPromptCommand());
        commandService.registerCommands(new SearchPartsCommand());
        commandService.registerCommands(new FindOccurrencesCommand());
        commandService.registerCommands(new ComparePartsCommand());
        commandService.registerCommands(new CountTokensCommand());
        commandService.registerCommands(new ListSummariesCommand());
        commandService.registerCommands(new GetSummaryCommand());
        commandService.registerCommands(new ListAnnotationsCommand());
        commandService.registerCommands(new GetAnnotationCommand());
        commandService.registerCommands(new CheckSummariesCommand());
        commandService.registerCommands(new GetTemplateVariablesCommand());
    }
}
