package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;

public interface ManuscriptService extends ExtendableService<Manuscript, ManuscriptRepository> {

    String getMasterTemplate(Manuscript manuscript) throws Exception;
    String getPov(Manuscript manuscript) throws Exception;
    String getTense(Manuscript manuscript) throws Exception;
    String getStyle(Manuscript manuscript) throws Exception;
    String getUserPrompt(Manuscript manuscript) throws Exception;

}
