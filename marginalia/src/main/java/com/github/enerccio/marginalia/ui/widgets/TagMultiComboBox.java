package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

@Configurable
public class TagMultiComboBox extends MultiSelectComboBox<Tag> {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private Localization loc;

    private Long targetObjectId;
    private Class<?> targetClass;
    private boolean loading = false;

    public TagMultiComboBox() {
        this(null);
    }

    public TagMultiComboBox(String label) {
        super(label);
        setItemLabelGenerator(Tag::getValue);
        setAllowCustomValue(true);
        setClearButtonVisible(true);

        addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (StringUtils.isBlank(customValue)) {
                return;
            }
            try {
                String trimmed = customValue.trim();
                List<Tag> existingTags = tagService.findAllForUser();
                Tag matchedTag = existingTags.stream()
                        .filter(t -> StringUtils.equalsIgnoreCase(t.getValue(), trimmed))
                        .findFirst()
                        .orElse(null);

                if (matchedTag == null) {
                    matchedTag = new Tag();
                    matchedTag.setValue(trimmed);
                    matchedTag = tagService.save(matchedTag);
                    loadAvailableTags();
                }

                Set<Tag> currentSelected = new HashSet<>(getValue());
                currentSelected.add(matchedTag);
                setValue(currentSelected);
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });

        addValueChangeListener(event -> {
            if (loading || targetObjectId == null || targetClass == null) {
                return;
            }
            syncRelations(event.getOldValue(), event.getValue());
        });
    }

    public void loadAvailableTags() {
        try {
            List<Tag> allTags = tagService.findAllForUser();
            setItems(allTags);
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    public void setForEntity(BaseEntity entity) {
        if (entity == null || entity.getId() == null) {
            setForEntity(null, null);
        } else {
            setForEntity(entity.getId(), entity.getClass());
        }
    }

    public void setForEntity(Long objectId, Class<?> clazz) {
        this.targetObjectId = objectId;
        this.targetClass = clazz;

        loadAvailableTags();

        if (objectId == null || clazz == null) {
            loading = true;
            setValue(Collections.emptySet());
            loading = false;
            return;
        }

        try {
            loading = true;
            List<Tag> entityTags = tagRelationService.getTagsForObject(objectId, clazz);
            setValue(new HashSet<>(entityTags));
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        } finally {
            loading = false;
        }
    }

    private void syncRelations(Set<Tag> oldTags, Set<Tag> newTags) {
        if (targetObjectId == null || targetClass == null) {
            return;
        }
        Set<Tag> oldSet = oldTags != null ? oldTags : Collections.emptySet();
        Set<Tag> newSet = newTags != null ? newTags : Collections.emptySet();

        try {
            for (Tag tag : newSet) {
                if (!oldSet.contains(tag)) {
                    tagRelationService.createRelation(tag, targetObjectId, targetClass);
                }
            }
            for (Tag tag : oldSet) {
                if (!newSet.contains(tag)) {
                    tagRelationService.removeRelation(tag, targetObjectId, targetClass);
                }
            }
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }
}