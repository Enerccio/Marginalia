package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.LocalizationKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the annotations that carry localized texts, which each layer declares with its own enum of keys and marks with
 * a meta-annotation ({@link CommandTextSource}, {@link TemplateDescriptionSource}): the element is read by its name.
 */
public final class LocalizedAnnotations {

    private LocalizedAnnotations() {
    }

    /**
     * The annotation of the element whose type carries the mark, null when there is none.
     *
     * @throws IllegalArgumentException when the element has more than one
     */
    public static Annotation find(AnnotatedElement element, Class<? extends Annotation> mark) {
        Annotation found = null;
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(mark)) {
                if (found != null) {
                    throw new IllegalArgumentException(element + " has two @" + mark.getSimpleName() + " annotations, "
                            + found.annotationType().getSimpleName() + " and " + annotation.annotationType().getSimpleName());
                }
                found = annotation;
            }
        }
        return found;
    }

    /** The elements a {@link CommandTextSource} annotation declares. */
    public static final List<String> COMMAND_TEXT_ELEMENTS = List.of("humanName", "description", "llmDescription", "returns", "parameters");

    /**
     * The {@link CommandTextSource} annotation of a command method, null when it has none.
     *
     * @throws IllegalArgumentException when it has two, or the type of the annotation lacks one of
     *                                  {@link #COMMAND_TEXT_ELEMENTS}
     */
    public static Annotation commandTexts(Method method) {
        Annotation texts = find(method, CommandTextSource.class);
        if (texts != null) {
            for (String element : COMMAND_TEXT_ELEMENTS) {
                try {
                    texts.annotationType().getMethod(element);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("@" + texts.annotationType().getSimpleName() + " of " + method
                            + " is a @CommandTextSource without the element " + element);
                }
            }
        }
        return texts;
    }

    /**
     * The key of a template variable: the {@code loc} of its {@link TemplateDescriptionSource} annotation, null when it
     * has none.
     */
    public static LocalizationKey templateDescription(AnnotatedElement element) {
        Annotation annotation = find(element, TemplateDescriptionSource.class);
        if (annotation == null) {
            return null;
        }
        List<LocalizationKey> keys = keys(annotation, "loc");
        return keys.isEmpty() ? null : keys.getFirst();
    }

    /**
     * The keys of an element: one for a key, the elements of an array of keys; none when the annotation is null or has
     * no such element.
     *
     * @throws IllegalArgumentException when the element is not a key or an array of them
     */
    public static List<LocalizationKey> keys(Annotation annotation, String element) {
        Object value = value(annotation, element);
        List<LocalizationKey> keys = new ArrayList<>();
        if (value == null) {
            return keys;
        }
        if (value instanceof LocalizationKey key) {
            keys.add(key);
        } else if (value instanceof Object[] array && LocalizationKey.class.isAssignableFrom(array.getClass().getComponentType())) {
            for (Object key : array) {
                keys.add((LocalizationKey) key);
            }
        } else {
            throw new IllegalArgumentException("@" + annotation.annotationType().getSimpleName() + "." + element
                    + " is not a localization key or an array of them");
        }
        return keys;
    }

    /**
     * The annotations of an element that is an array of annotations; none when the annotation is null or has no such
     * element.
     */
    public static List<Annotation> annotations(Annotation annotation, String element) {
        Object value = value(annotation, element);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof Annotation[] array)) {
            throw new IllegalArgumentException("@" + annotation.annotationType().getSimpleName() + "." + element
                    + " is not an array of annotations");
        }
        return List.of(array);
    }

    /**
     * The text of an element that is a {@code String}.
     */
    public static String string(Annotation annotation, String element) {
        Object value = value(annotation, element);
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("@" + annotation.annotationType().getSimpleName() + "." + element
                    + " is not a String");
        }
        return text;
    }

    private static Object value(Annotation annotation, String element) {
        if (annotation == null) {
            return null;
        }
        Method method;
        try {
            method = annotation.annotationType().getMethod(element);
        } catch (NoSuchMethodException e) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            throw new IllegalStateException("Cannot read @" + annotation.annotationType().getSimpleName() + "." + element, e);
        }
    }
}
