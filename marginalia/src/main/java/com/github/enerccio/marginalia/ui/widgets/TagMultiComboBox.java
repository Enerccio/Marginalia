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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Configurable
public class TagMultiComboBox extends MultiSelectComboBox<Tag> {

    private static final Logger log = LoggerFactory.getLogger(TagMultiComboBox.class);

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private Localization loc;

    private Long targetObjectId;
    private Class<?> targetClass;
    private boolean negative = false;
    private boolean loading = false;

    public TagMultiComboBox() {
        this(null);
    }

    public TagMultiComboBox(String label) {
        this(label, false);
    }

    public TagMultiComboBox(String label, boolean negative) {
        super(label);
        this.negative = negative;
        setItemLabelGenerator(Tag::getValue);
        setAllowCustomValue(true);
        setClearButtonVisible(true);

        setupSearchDataProvider();

        addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (StringUtils.isBlank(customValue)) {
                return;
            }
            try {
                String trimmed = customValue.trim();
                List<Tag> matches = tagService.searchTagsForUser(trimmed, 0, 10);
                Tag matchedTag = matches.stream()
                        .filter(t -> StringUtils.equalsIgnoreCase(t.getValue(), trimmed))
                        .findFirst()
                        .orElse(null);

                if (matchedTag == null) {
                    matchedTag = new Tag();
                    matchedTag.setValue(trimmed);
                    matchedTag = tagService.save(matchedTag);
                    refreshData();
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

    private void setupSearchDataProvider() {
        setItems(query -> {
            try {
                String filter = query.getFilter().orElse("");
                return tagService.searchTagsForUser(filter, query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                log.error("Failed to fetch tags for filter: {}", query.getFilter().orElse(""), e);
                return Stream.empty();
            }
        });
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public void refreshData() {
        getDataProvider().refreshAll();
    }

    public void setForEntity(BaseEntity entity) {
        setForEntity(entity, negative);
    }

    public void setForEntity(BaseEntity entity, boolean negative) {
        if (entity == null || entity.getId() == null) {
            setForEntity(null, null, negative);
        } else {
            setForEntity(entity.getId(), entity.getClass(), negative);
        }
    }

    public void setForEntity(Long objectId, Class<?> clazz) {
        setForEntity(objectId, clazz, negative);
    }

    public void setForEntity(Long objectId, Class<?> clazz, boolean negative) {
        this.targetObjectId = objectId;
        this.targetClass = clazz;
        this.negative = negative;

        refreshData();

        if (objectId == null || clazz == null) {
            loading = true;
            setValue(Collections.emptySet());
            loading = false;
            return;
        }

        try {
            loading = true;
            List<Tag> entityTags = tagRelationService.getTagsForObject(objectId, clazz, negative);
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
                    tagRelationService.createRelation(tag, targetObjectId, targetClass, negative);
                }
            }
            for (Tag tag : oldSet) {
                if (!newSet.contains(tag)) {
                    tagRelationService.removeRelation(tag, targetObjectId, targetClass, negative);
                }
            }
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }
}