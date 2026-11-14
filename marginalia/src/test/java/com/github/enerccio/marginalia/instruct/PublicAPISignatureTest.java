package com.github.enerccio.marginalia.instruct;

import com.github.enerccio.marginalia.instruct.PublicAPISignatures.Field;
import com.github.enerccio.marginalia.instruct.PublicAPISignatures.Method;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every instrumented method of an {@code @Extendable} class declares the signature plugins see ({@code @PublicAPI}),
 * and the declaration is what the compiled class has. The fields plugins read are declared the same way (opt-in: the
 * annotation is on the field, and it says the type). The classes are read with ByteBuddy's type pool, so nothing is
 * loaded. A failure names the method or field and prints the annotation to put on it.
 */
class PublicAPISignatureTest {

    private static final Pattern VERSION = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    private final TypePool pool = PublicAPISignatures.pool();
    private final ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(PublicAPISignatures.class.getClassLoader());

    @Test
    void foundTheExtendableClasses() {
        // guards the scan itself: an empty list would make the other checks pass for nothing
        assertTrue(PublicAPISignatures.extendableTypes(pool).size() > 50);
    }

    @Test
    void everyInstrumentedMethodIsAnnotated() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Method method : PublicAPISignatures.methods(type, locator)) {
                if (method.declared() == null) {
                    problems.add(method.where() + " has no @PublicAPI, add:\n" + PublicAPISignatures.render("<version>", method.actual()));
                }
            }
        }
        assertNoProblems("instrumented methods without @PublicAPI", problems);
    }

    @Test
    void declaredSignaturesMatchTheCompiledClasses() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Method method : PublicAPISignatures.methods(type, locator)) {
                if (method.declared() == null) {
                    continue; // reported by everyInstrumentedMethodIsAnnotated
                }
                if (!method.actual().equals(method.declared())) {
                    problems.add(method.where() + " differs from its @PublicAPI\n  declared: " + method.declared()
                            + "\n  compiled: " + method.actual() + "\n  expected annotation:\n"
                            + PublicAPISignatures.render(method.from(), method.actual()));
                }
            }
        }
        assertNoProblems("@PublicAPI that does not describe the method", problems);
    }

    @Test
    void parametersAreNamed() {
        // the context names an argument "arg0" without the MethodParameters attribute (-parameters)
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Method method : PublicAPISignatures.methods(type, locator)) {
                if (!method.namedParameters()) {
                    problems.add(method.where() + " is compiled without parameter names");
                }
            }
        }
        assertNoProblems("methods compiled without -parameters", problems);
    }

    @Test
    void fromIsAVersion() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Method method : PublicAPISignatures.methods(type, locator)) {
                if (method.from() != null && !VERSION.matcher(method.from()).matches()) {
                    problems.add(method.where() + " has from = \"" + method.from() + "\", expected major.minor.patch");
                }
            }
        }
        assertNoProblems("@PublicAPI(from) that is not a version", problems);
    }

    @Test
    void declaredFieldTypesMatchTheCompiledClasses() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Field field : PublicAPISignatures.fields(type)) {
                if (!field.actualType().equals(field.declaredType())) {
                    problems.add(field.where() + " is " + field.actualType() + ", its @PublicAPI says " + field.declaredType()
                            + "\n  expected annotation:\n" + PublicAPISignatures.renderField(field.from(), field.actualType()));
                }
            }
        }
        assertNoProblems("@PublicAPI that does not describe the field", problems);
    }

    @Test
    void fieldFromIsAVersion() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Field field : PublicAPISignatures.fields(type)) {
                if (!VERSION.matcher(field.from()).matches()) {
                    problems.add(field.where() + " has from = \"" + field.from() + "\", expected major.minor.patch");
                }
            }
        }
        assertNoProblems("@PublicAPI(from) of a field that is not a version", problems);
    }

    @Test
    void methodsDoNotDeclareAFieldType() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.extendableTypes(pool)) {
            for (Method method : PublicAPISignatures.methods(type, locator)) {
                if (method.declaredFieldType() != null && !method.declaredFieldType().equals("void")) {
                    problems.add(method.where() + " has type = " + method.declaredFieldType() + ", that is for fields");
                }
            }
        }
        assertNoProblems("@PublicAPI on a method with a field type", problems);
    }

    @Test
    void noFieldAnnotationOnFieldsThatAreNotReachable() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.allTypes(pool)) {
            problems.addAll(PublicAPISignatures.annotatedFieldsOutOfPlace(type));
        }
        assertNoProblems("@PublicAPI on static or synthetic fields, or in classes that are not @Extendable", problems);
    }

    @Test
    void noAnnotationOnMethodsThatAreNotInstrumented() {
        List<String> problems = new ArrayList<>();
        for (TypeDescription type : PublicAPISignatures.allTypes(pool)) {
            problems.addAll(PublicAPISignatures.annotatedButNotInstrumented(type));
        }
        assertNoProblems("@PublicAPI on constructors, static, synthetic or bodiless methods, or in classes that are not @Extendable", problems);
    }

    private static void assertNoProblems(String what, List<String> problems) {
        if (!problems.isEmpty()) {
            fail(problems.size() + " " + what + ":\n" + String.join("\n", problems));
        }
    }
}
