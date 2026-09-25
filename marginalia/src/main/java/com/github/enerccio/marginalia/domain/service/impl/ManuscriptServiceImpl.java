package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ManuscriptServiceImpl extends ExtendableServiceImpl<Manuscript, ManuscriptRepository> implements ManuscriptService {

    @Autowired
    private SettingService settingService;

    @Override
    @CommonTxReadOnly
    public String getMasterTemplate(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getTemplate())) {
            return manuscript.getTemplate();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getMasterTemplate())) {
            return userSetting.getMasterTemplate();
        }
        return Defaults.DEFAULT_MASTER_TEMPLATE;
    }

    @Override
    @CommonTxReadOnly
    public String getPov(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getPov())) {
            return manuscript.getPov();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultPov())) {
            return userSetting.getDefaultPov();
        }
        return Defaults.DEFAULT_POV;
    }

    @Override
    @CommonTxReadOnly
    public String getTense(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getTense())) {
            return manuscript.getTense();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultTense())) {
            return userSetting.getDefaultTense();
        }
        return Defaults.DEFAULT_TENSE;
    }

    @Override
    @CommonTxReadOnly
    public String getStyle(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getStyle())) {
            return manuscript.getStyle();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultStyle())) {
            return userSetting.getDefaultStyle();
        }
        return Defaults.DEFAULT_STYLE;
    }

    @Override
    @CommonTxReadOnly
    public String getUserPrompt(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getUserPrompt())) {
            return manuscript.getUserPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultUserPrompt())) {
            return userSetting.getDefaultUserPrompt();
        }
        return Defaults.DEFAULT_USER_PROMPT;
    }

    @Override
    public String getSummaryPrompt(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getSummaryPrompt())) {
            return manuscript.getUserPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultSummaryPrompt())) {
            return userSetting.getDefaultSummaryPrompt();
        }
        return Defaults.DEFAULT_SUMMARY_PROMPT;
    }
}
