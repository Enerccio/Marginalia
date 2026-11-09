package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * The user level of what books and folders inherit: the language, whether a part is translated by itself, the model and the protocol of the
 * request. Kept in the attributes of the user's settings under {@link #KEY}; a book or a folder that sets the same overrides it.
 */
public class UserDefaults {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.defaults";

    private String languageId;
    private boolean auto;
    private String aiUuid;
    private String protocolUuid;
    /** The model and the protocol of the designer; empty: the ones of the translation, else the defaults of the user. */
    private String designerAiUuid;
    private String designerProtocolUuid;

    public String getLanguageId() {
        return languageId == null || languageId.isBlank() ? null : languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getAiUuid() {
        return aiUuid == null || aiUuid.isBlank() ? null : aiUuid;
    }

    public void setAiUuid(String aiUuid) {
        this.aiUuid = aiUuid;
    }

    public String getProtocolUuid() {
        return protocolUuid == null || protocolUuid.isBlank() ? null : protocolUuid;
    }

    public void setProtocolUuid(String protocolUuid) {
        this.protocolUuid = protocolUuid;
    }

    public String getDesignerAiUuid() {
        return designerAiUuid == null || designerAiUuid.isBlank() ? null : designerAiUuid;
    }

    public void setDesignerAiUuid(String designerAiUuid) {
        this.designerAiUuid = designerAiUuid;
    }

    public String getDesignerProtocolUuid() {
        return designerProtocolUuid == null || designerProtocolUuid.isBlank() ? null : designerProtocolUuid;
    }

    public void setDesignerProtocolUuid(String designerProtocolUuid) {
        this.designerProtocolUuid = designerProtocolUuid;
    }
}
