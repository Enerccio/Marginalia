package com.github.enerccio.marginalia.instruct;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What is instrumented and how, the same for the two ways the hooks get into an {@code @Extendable} class:
 * {@link BuildTimeWeaver} (the Maven build) and {@link RuntimeInstrumentationInitializer} (the agent, for classes the
 * build did not weave).
 */
public final class ExtendableWeaving {

    public static final String EXTENDABLE = "com.github.enerccio.marginalia.domain.traits.Extendable";
    public static final String WOVEN = "com.github.enerccio.marginalia.instruct.Woven";

    private static final String EXTENDABLE_DESC = "L" + EXTENDABLE.replace('.', '/') + ";";
    private static final String WOVEN_DESC = "L" + WOVEN.replace('.', '/') + ";";

    /**
     * System property that picks how ByteBuddy reads and writes class files, read once when ByteBuddy first needs it.
     */
    public static final String BYTEBUDDY_PROCESSOR = "net.bytebuddy.processor";

    private ExtendableWeaving() {
    }

    /**
     * Makes ByteBuddy read and write class files with its own ASM, never with the JDK's ClassFile API. ByteBuddy
     * switches to the ClassFile API on a JVM newer than the newest Java it knows, and the hooks woven that way fail
     * the stack map check ("branch target out of bytecode range"). With ASM such a JVM fails at once with ByteBuddy's
     * own "Java N is not supported ... update Byte Buddy", which says what to do. Call it before ByteBuddy is used; a
     * value given on the command line ({@code -Dnet.bytebuddy.processor=...}) is kept.
     */
    public static void useAsm() {
        if (System.getProperty(BYTEBUDDY_PROCESSOR) == null) {
            System.setProperty(BYTEBUDDY_PROCESSOR, "ASM_ONLY");
        }
    }

    /**
     * Classes that declare {@code @Extendable} (the annotation is not inherited) and were not woven yet.
     */
    public static ElementMatcher.Junction<TypeDescription> unwovenExtendableTypes() {
        return ElementMatchers.<TypeDescription>isAnnotatedWith(ElementMatchers.named(EXTENDABLE))
                .and(ElementMatchers.not(ElementMatchers.isAnnotatedWith(ElementMatchers.named(WOVEN))));
    }

    /**
     * Methods a decorator can be attached to: not a constructor, not static, not synthetic. Methods without a body
     * are matched too, {@link ExtendableMethodVisitor} leaves them alone.
     */
    public static ElementMatcher.Junction<MethodDescription> instrumentedMethods() {
        return ElementMatchers.not(ElementMatchers.isConstructor())
                .and(ElementMatchers.not(ElementMatchers.isSynthetic()))
                .and(ElementMatchers.not(ElementMatchers.isStatic()));
    }

    /**
     * ByteBuddy set up for adding the hooks: only the declared methods of a class are looked at. The default (the
     * methods of the whole hierarchy) is not needed and fails on the {@code @Configurable} classes that have a
     * generic supertype: AspectJ adds interfaces to the class that its generic signature does not name, ByteBuddy
     * cannot resolve them, and the agent would leave such a class without hooks and without a word.
     */
    public static ByteBuddy byteBuddy() {
        return new ByteBuddy().with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE);
    }

    /**
     * Rewrites every instrumented method with {@link ExtendableMethodVisitor}. No field or method is added, so it can
     * be applied to a class that is already loaded.
     */
    public static AsmVisitorWrapper hooks() {
        return new AsmVisitorWrapper.ForDeclaredMethods()
                .writerFlags(ClassWriter.COMPUTE_FRAMES)
                .method(
                        instrumentedMethods(),
                        (instrumentedType, instrumentedMethod, methodVisitor, implementationContext, typePool, writerFlags, readerFlags) ->
                                new ExtendableMethodVisitor(
                                        Opcodes.ASM9,
                                        methodVisitor,
                                        instrumentedMethod.getStackSize(),
                                        instrumentedType.getInternalName(),
                                        instrumentedMethod.getName(),
                                        instrumentedMethod.getDescriptor()
                                )
                );
    }

    /**
     * The {@code @Extendable} classes of a folder of class files that have no {@link Woven} mark (compiled after the
     * last run of {@link BuildTimeWeaver}, by the IDE for example). Only the headers of the class files are read,
     * nothing is loaded.
     *
     * @return their names, sorted
     */
    public static List<String> unwovenClasses(Path classes) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(classes)) {
            files = walk.filter(file -> file.toString().endsWith(".class")).sorted().collect(Collectors.toList());
        }
        List<String> unwoven = new ArrayList<>();
        for (Path file : files) {
            boolean[] found = new boolean[2];
            ClassReader reader = new ClassReader(Files.readAllBytes(file));
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    found[0] |= descriptor.equals(EXTENDABLE_DESC);
                    found[1] |= descriptor.equals(WOVEN_DESC);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            if (found[0] && !found[1]) {
                unwoven.add(reader.getClassName().replace('/', '.'));
            }
        }
        Collections.sort(unwoven);
        return unwoven;
    }
}
