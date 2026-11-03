package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Looking up a private member of a class whose hierarchy ends in JDK classes (everything ends in {@code Object}, whose
 * methods can not be opened) must find it; it used to fail and the extension was told the method did not exist.
 */
class ReflectUtilsTest {

    @SuppressWarnings("unused")
    private static class Target {
        private String name = "x";

        private void autosave() {
        }
    }

    @Test
    void findsPrivateMethodDespiteJdkSuperclass() throws Exception {
        Method method = ReflectUtils.getMethod(Target.class, "autosave", new Class<?>[0]);
        assertThat(method).isNotNull();
        assertThat(method.getName()).isEqualTo("autosave");
    }

    @Test
    void findsPrivateField() throws Exception {
        Field field = ReflectUtils.getField(Target.class, "name");
        assertThat(field).isNotNull();
        assertThat(field.get(new Target())).isEqualTo("x");
    }

    @Test
    void unknownMethodIsNull() throws Exception {
        assertThat(ReflectUtils.getMethod(Target.class, "nope", new Class<?>[0])).isNull();
    }
}
