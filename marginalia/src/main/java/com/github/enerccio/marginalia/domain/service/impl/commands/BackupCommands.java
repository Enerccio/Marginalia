package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.CommandUtils;
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

    @Command(commandName = "take-backup", humanName = L.LABEL_COMMAND_TAKE_BACKUP, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_TAKE_BACKUP,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = String.class,
                    description = L.DESC_COMMAND_TAKE_BACKUP_RETURN))
    public void takeBackup(CommandContext context) throws Exception {
        Manuscript open = CommandUtils.getCurrentManuscript();
        if (open == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
        }
        // the window's copy can be older than the stored book (renamed, for one)
        Manuscript manuscript = manuscriptService.findForUser(open.getUuid());
        if (manuscript == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
        }

        context.setStatus(String.format(loc.getValue(L.MSG_COMMAND_BACKUP_TAKING), manuscript.getName()));
        context.setSubStatus(loc.getValue(L.MSG_COMMAND_BACKUP_WRITING));
        ManuscriptBackup backup = backupService.takeBackup(manuscript);
        if (context.isTerminated()) {
            // stopped meanwhile: the backup is made already, there is nothing to undo, only nobody wants the answer
            return;
        }
        if (backup == null) {
            // the book is gone from the database
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
        }
        context.setReturn(String.format(loc.getValue(L.MSG_COMMAND_BACKUP_TAKEN), backup.getManuscriptName(),
                backup.getTotalMessagesCount(), backup.getImageCount()));
    }

}
