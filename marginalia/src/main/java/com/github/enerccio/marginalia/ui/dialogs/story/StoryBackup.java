package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The automatic backup of a book while it is written: after N generated parts or N minutes, as set on the book.
 */
@Configurable
@Extendable
public class StoryBackup {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private BackupService backupService;

    /**
     * Called after a part was generated: counts it and takes the backup when the limit of the book's strategy is reached.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "host", type = StoryHost.class)},
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "strategy", type = BackupService.BackupStrategy.class),
                    @APILocals(name = "strategyValStr", type = String.class),
                    @APILocals(name = "prefix", type = String.class),
                    @APILocals(name = "currentVal", type = String.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "ignored", type = NumberFormatException.class)})
    public void checkAndApplyBackupStrategy(StoryHost host) {
        try {
            Manuscript manuscript = host.refreshManuscript();
            if (manuscript == null) {
                return;
            }

            BackupStrategy strategy = manuscriptService.getBackupStrategy(manuscript);
            String strategyValStr = manuscriptService.getBackupStrategyValue(manuscript);

            if (strategy == null || strategy == BackupStrategy.DISABLED) {
                if (manuscript.getBackupStrategyCurrentValue() != null) {
                    manuscript.setBackupStrategyCurrentValue(null);
                    manuscriptService.save(manuscript);
                }
                return;
            }

            int limit = 1;
            if (StringUtils.isNotBlank(strategyValStr)) {
                try {
                    limit = Math.max(1, Integer.parseInt(strategyValStr.trim()));
                } catch (NumberFormatException ignored) {}
            }

            String prefix = strategy.name() + ":" + limit + ":";
            String currentVal = manuscript.getBackupStrategyCurrentValue();

            if (strategy == BackupStrategy.AFTER_N_MESSAGES) {
                int count = 0;
                if (currentVal != null && currentVal.startsWith(prefix)) {
                    try {
                        count = Integer.parseInt(currentVal.substring(prefix.length()));
                    } catch (NumberFormatException ignored) {}
                }

                count++;

                if (count >= limit) {
                    try {
                        backupService.takeBackup(manuscript);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                    count = 0;
                }

                manuscript.setBackupStrategyCurrentValue(prefix + count);
                host.save();
            } else if (strategy == BackupStrategy.AFTER_N_MINUTES) {
                long lastBackupTime = System.currentTimeMillis();
                boolean prefixValid = currentVal != null && currentVal.startsWith(prefix);

                if (prefixValid) {
                    try {
                        lastBackupTime = Long.parseLong(currentVal.substring(prefix.length()));
                    } catch (NumberFormatException ignored) {
                        prefixValid = false;
                    }
                }

                if (!prefixValid) {
                    lastBackupTime = System.currentTimeMillis();
                }

                long now = System.currentTimeMillis();
                long elapsedMinutes = (now - lastBackupTime) / (1000 * 60);

                if (elapsedMinutes >= limit) {
                    try {
                        backupService.takeBackup(manuscript);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                    lastBackupTime = now;
                }

                manuscript.setBackupStrategyCurrentValue(prefix + lastBackupTime);
                host.save();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

}
