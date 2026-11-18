package com.github.enerccio.marginalia.domain.traits;

import java.lang.annotation.Documented;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.LocalizationKey;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation that gives the localized texts of a {@link Command} method. An annotation element holds one type,
 * so each layer has its own annotation typed with its own enum of keys ({@link CommandTexts} with the platform's
 * {@code L}, {@code MarginaliaCommandTexts} with the application's {@code M_L}), and the command service finds it by this
 * mark and reads its elements by name:
 * <ul>
 *     <li>{@code humanName}, {@code description}, {@code llmDescription}, {@code returns}: arrays of keys (an enum that
 *     implements {@link com.github.enerccio.marginalia.loc.LocalizationKey}), none or one each; the localized forms of
 *     {@link Command#humanNameText()}, {@link Command#descriptionText()}, {@link Command#llmDescriptionText()} and
 *     {@link CommandReturn#descriptionText()};</li>
 *     <li>{@code parameters}: an array of annotations with a {@code String name()} (the {@link CommandParameter#name()}
 *     of the method's signature) and a {@code description()} array of keys, the localized
 *     {@link CommandParameter#descriptionText()}.</li>
 * </ul>
 * An element the annotation leaves out is a text it does not give. A method has at most one such annotation; a text
 * given by it and by the {@code ...Text} element as well is an error when the suite is registered.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CommandTextSource {
}
