package com.github.enerccio.marginalia.domain.service.search.replace;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import com.github.enerccio.marginalia.utils.ReflectUtils;

import org.hibernate.Hibernate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Search and replace in the {@link Fulltextable} fields of an entity, whatever the entity is. The database finds the
 * entities by their {@code _fulltext} column (the text of those fields in one), this goes through the fields of the
 * found entity to tell where the matches are. Only fields with text ({@code String}) are looked at. The entity can be a lazy proxy, its fields are
 * read from the real object.
 */
public final class EntityTextSearch {

    private EntityTextSearch() {
    }

    /**
     * What a replace does to one field of an entity.
     *
     * @param field   the field
     * @param oldText the text of the field now
     * @param newText the text of the field with the matches replaced
     * @param hits    the matches in the field
     */
    public record FieldChange(Field field, String oldText, String newText, List<TextHit> hits) {
    }

    /**
     * @return the fields of the entity that have a match, empty when none has
     */
    public static List<FieldChange> search(ExtendableEntity entity, TextSearch search) throws IllegalAccessException {
        List<FieldChange> changes = new ArrayList<>();
        Object target = Hibernate.unproxy(entity);
        for (Field field : fields(target.getClass())) {
            if (field.get(target) instanceof String text) {
                TextSearch.Result result = search.apply(field.getName(), text);
                if (result.found()) {
                    changes.add(new FieldChange(field, text, result.newText(), result.hits()));
                }
            }
        }
        return changes;
    }

    /**
     * @return whether the fields of the entity have the texts they had when the changes were made
     */
    public static boolean hasTexts(ExtendableEntity entity, List<FieldChange> changes) throws IllegalAccessException {
        Object target = Hibernate.unproxy(entity);
        for (FieldChange change : changes) {
            if (!Objects.equals(change.field().get(target), change.oldText())) {
                return false;
            }
        }
        return true;
    }

    public static List<Field> fields(Class<?> entityClass) {
        return ReflectUtils.getAnnotatedFields(entityClass, Fulltextable.class).stream()
                .filter(field -> field.getType() == String.class)
                .toList();
    }

    /**
     * Puts the new texts into the fields; the caller saves the entity (which rebuilds {@code _fulltext}).
     */
    public static void apply(ExtendableEntity entity, List<FieldChange> changes) throws IllegalAccessException {
        Object target = Hibernate.unproxy(entity);
        for (FieldChange change : changes) {
            change.field().set(target, change.newText());
        }
    }

}
