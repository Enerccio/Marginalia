package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.MarginaliaCommandTexts;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Commands that make backups. {@code /take-backup} is the button "Take backup" of the book window: it backs up the book
 * that is open and keeps the backup with the other backups of the book. It is {@code async}: a big book with many
 * messages takes a while, so the command bar shows the progress window with what is going on.
 */
@Configurable
public class BackupCommands {

    @Autowired
    private BackupService backupService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    @Command(commandName = "take-backup", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_TAKE_BACKUP,
            description = M_L.DESC_COMMAND_TAKE_BACKUP,
            returns = M_L.DESC_COMMAND_TAKE_BACKUP_RETURN)
    public void takeBackup(CommandContext context) throws Exception {
        Manuscript manuscript = CommandScope.of(context).getManuscript();
        if (manuscript == null) {
            throw new IllegalStateException(loc.getValue(M_L.ERROR_COMMAND_NO_BOOK_OPEN));
        }

        context.setStatus(String.format(loc.getValue(M_L.MSG_COMMAND_BACKUP_TAKING), manuscript.getName()));
        context.setSubStatus(loc.getValue(M_L.MSG_COMMAND_BACKUP_WRITING));
        ManuscriptBackup backup = backupService.takeBackup(manuscript);
        if (context.isTerminated()) {
            // stopped meanwhile: the backup is made already, there is nothing to undo, only nobody wants the answer
            return;
        }
        if (backup == null) {
            // the book is gone from the database
            throw new IllegalStateException(loc.getValue(M_L.ERROR_COMMAND_NO_BOOK_OPEN));
        }
        context.setReturn(String.format(loc.getValue(M_L.MSG_COMMAND_BACKUP_TAKEN), backup.getManuscriptName(),
                backup.getTotalMessagesCount(), backup.getImageCount()));
    }

}
