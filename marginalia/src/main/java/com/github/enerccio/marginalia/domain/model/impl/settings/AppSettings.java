package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AppSettings")
public class AppSettings extends Setting {

    @Transient
    @ExtendedAttribute
    private Integer dbVersion = 1;

    @Transient
    @ExtendedAttribute
    private Integer appVersion = 1;

    // scheduled database backups

    @Transient
    @ExtendedAttribute
    private Boolean backupScheduleEnabled = false;

    /**
     * Cron expression, 5 fields (minute hour day-of-month month day-of-week), 6 fields with seconds or a macro
     * like {@code @daily}.
     */
    @Transient
    @ExtendedAttribute
    private String backupSchedule = "0 3 * * *";

    /**
     * Number of scheduled backups kept, older ones are deleted. 0 keeps all.
     */
    @Transient
    @ExtendedAttribute
    private Integer backupKeep = 7;

    @Transient
    @ExtendedAttribute
    private Date lastScheduledBackup;

    /**
     * When the schedule was last changed - scheduled runs missed before this time are not caught up.
     */
    @Transient
    @ExtendedAttribute
    private Date backupScheduleChanged;

    // AIs

    /**
     * Whether users who are not administrators can create their own AIs. When off they can only use the AIs the
     * administrators share; the AIs they already have keep working. Null (never set) means allowed.
     */
    @Transient
    @ExtendedAttribute
    private Boolean userAisAllowed = true;

    // help assistant

    /**
     * Whether the help assistant (a chat about the manual) is on for everybody. Off by default.
     */
    @Transient
    @ExtendedAttribute
    private Boolean helpEnabled = false;

    /**
     * Uuid of the AI the help assistant uses, an AI of an administrator (shared or not).
     */
    @Transient
    @ExtendedAttribute
    private String helpAiUuid;

    /**
     * For a shared help AI: whether the limits of the user and their usage apply to the help questions as to any
     * other request to the AI. Without it, or for an AI that is not shared, the help is paid by the owner of the AI,
     * not limited and not counted for the user.
     */
    @Transient
    @ExtendedAttribute
    private Boolean helpApplyLimits = false;

    /**
     * Whether the help assistant may move through the interface for the user (open a section, select a tab). Only
     * moves that are harmless and can be undone; off by default.
     */
    @Transient
    @ExtendedAttribute
    private Boolean helpInteractions = false;

    /**
     * The instructions of the help assistant for administrators, null or empty for the default
     * ({@code HelpPrompts.ADMIN}).
     */
    @Transient
    @ExtendedAttribute
    private String helpPromptAdmin;

    /**
     * The instructions of the help assistant for the other users, null or empty for the default
     * ({@code HelpPrompts.USER}).
     */
    @Transient
    @ExtendedAttribute
    private String helpPromptUser;

    public Integer getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(Integer dbVersion) {
        this.dbVersion = dbVersion;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Integer appVersion) {
        this.appVersion = appVersion;
    }

    public Boolean getBackupScheduleEnabled() {
        return backupScheduleEnabled;
    }

    public void setBackupScheduleEnabled(Boolean backupScheduleEnabled) {
        this.backupScheduleEnabled = backupScheduleEnabled;
    }

    public String getBackupSchedule() {
        return backupSchedule;
    }

    public void setBackupSchedule(String backupSchedule) {
        this.backupSchedule = backupSchedule;
    }

    public Integer getBackupKeep() {
        return backupKeep;
    }

    public void setBackupKeep(Integer backupKeep) {
        this.backupKeep = backupKeep;
    }

    public Date getLastScheduledBackup() {
        return lastScheduledBackup;
    }

    public void setLastScheduledBackup(Date lastScheduledBackup) {
        this.lastScheduledBackup = lastScheduledBackup;
    }

    public Date getBackupScheduleChanged() {
        return backupScheduleChanged;
    }

    public void setBackupScheduleChanged(Date backupScheduleChanged) {
        this.backupScheduleChanged = backupScheduleChanged;
    }

    public Boolean getUserAisAllowed() {
        return userAisAllowed == null || userAisAllowed;
    }

    public void setUserAisAllowed(Boolean userAisAllowed) {
        this.userAisAllowed = userAisAllowed;
    }

    public Boolean getHelpEnabled() {
        return helpEnabled != null && helpEnabled;
    }

    public void setHelpEnabled(Boolean helpEnabled) {
        this.helpEnabled = helpEnabled;
    }

    public String getHelpAiUuid() {
        return helpAiUuid;
    }

    public void setHelpAiUuid(String helpAiUuid) {
        this.helpAiUuid = helpAiUuid;
    }

    public String getHelpPromptAdmin() {
        return helpPromptAdmin;
    }

    public void setHelpPromptAdmin(String helpPromptAdmin) {
        this.helpPromptAdmin = helpPromptAdmin;
    }

    public String getHelpPromptUser() {
        return helpPromptUser;
    }

    public void setHelpPromptUser(String helpPromptUser) {
        this.helpPromptUser = helpPromptUser;
    }

    public Boolean getHelpApplyLimits() {
        return helpApplyLimits != null && helpApplyLimits;
    }

    public void setHelpApplyLimits(Boolean helpApplyLimits) {
        this.helpApplyLimits = helpApplyLimits;
    }

    public Boolean getHelpInteractions() {
        return helpInteractions != null && helpInteractions;
    }

    public void setHelpInteractions(Boolean helpInteractions) {
        this.helpInteractions = helpInteractions;
    }
}
