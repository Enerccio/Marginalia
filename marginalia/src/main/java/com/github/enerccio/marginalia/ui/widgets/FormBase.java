package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.dialog.Dialog;
import io.github.enerccio.llllm.bound.SessionPoint;
import io.github.enerccio.llllm.loc.Localization;
import io.github.enerccio.llllm.model.domain.ExtendedContentEntity;
import io.github.enerccio.llllm.model.service.ExtendedContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class FormBase<T extends ExtendedContentEntity, S extends ExtendedContentService<T>> extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    protected SessionPoint sessionPoint;

    @Autowired
    protected S service;

    protected T entity;

    protected abstract void createContents() throws Exception;

    protected abstract void refresh() throws Exception;

    public void create() throws Exception {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);
        setDraggable(false);
        setResizable(false);

        createContents();
    }

    public void setModel(T entity) throws Exception {
        this.entity = entity;
        refresh();
    }

    public T getModel() {
        return this.entity;
    }

}
