package com.github.enerccio.marginalia.instruct;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.pool.TypePool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Puts the hooks of the extensions into the {@code @Extendable} classes of a folder of compiled classes, in place.
 * The Maven build runs it on {@code target/classes} after the AspectJ compiler ({@code process-classes}), so the
 * packaged application needs no agent. A woven class is marked with {@link Woven} and skipped the next time, so it
 * can be run again on a folder where only some classes were compiled anew.
 */
public final class BuildTimeWeaver {

    /**
     * @param woven        classes rewritten by this run
     * @param alreadyWoven {@code @Extendable} classes that were woven before
     */
    public record Result(int woven, int alreadyWoven) {
    }

    private BuildTimeWeaver() {
    }

    /**
     * {@code BuildTimeWeaver <folder of classes>}; the libraries the classes use are those of the class path.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: BuildTimeWeaver <folder of compiled classes>");
        }
        Path classes = Path.of(args[0]);
        Result result = weave(classes, BuildTimeWeaver.class.getClassLoader());
        if (result.woven() + result.alreadyWoven() == 0) {
            throw new IllegalStateException("No @Extendable class in " + classes.toAbsolutePath() + ", is it the folder of the compiled application?");
        }
        System.out.println("Wove the extension hooks into " + result.woven() + " @Extendable classes"
                + (result.alreadyWoven() > 0 ? " (" + result.alreadyWoven() + " were woven before)" : ""));
    }

    /**
     * @param classes   folder of class files, the woven ones are written back to it
     * @param libraries where the types the classes refer to are looked up (as resources, nothing is loaded)
     */
    public static Result weave(Path classes, ClassLoader libraries) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(classes)) {
            files = walk.filter(file -> file.toString().endsWith(".class")).sorted().collect(Collectors.toList());
        }
        int woven = 0;
        int alreadyWoven = 0;
        try (ClassFileLocator locator = new ClassFileLocator.Compound(
                new ClassFileLocator.ForFolder(classes.toFile()),
                ClassFileLocator.ForClassLoader.of(libraries))) {
            TypePool pool = TypePool.Default.of(locator);
            for (Path file : files) {
                String relative = classes.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
                String name = relative.substring(0, relative.length() - ".class".length()).replace('/', '.');
                if (name.equals("module-info") || name.endsWith("package-info")) {
                    continue;
                }
                TypeDescription type = pool.describe(name).resolve();
                if (!isExtendable(type)) {
                    continue;
                }
                if (isWoven(type)) {
                    alreadyWoven++;
                    continue;
                }
                Path temporary = file.resolveSibling(file.getFileName() + ".weaving");
                Files.write(temporary, weave(type, locator, pool));
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                woven++;
            }
        }
        return new Result(woven, alreadyWoven);
    }

    /**
     * The class file of the type with the hooks and the {@link Woven} mark.
     */
    public static byte[] weave(TypeDescription type, ClassFileLocator locator, TypePool pool) {
        return ExtendableWeaving.byteBuddy()
                .with(Implementation.Context.Disabled.Factory.INSTANCE)
                .decorate(type, locator)
                .visit(ExtendableWeaving.hooks())
                .annotateType(AnnotationDescription.Builder.ofType(Woven.class).build())
                .make(pool)
                .getBytes();
    }

    static boolean isExtendable(TypeDescription type) {
        return declares(type, ExtendableWeaving.EXTENDABLE);
    }

    static boolean isWoven(TypeDescription type) {
        return declares(type, ExtendableWeaving.WOVEN);
    }

    private static boolean declares(TypeDescription type, String annotation) {
        return type.getDeclaredAnnotations().stream()
                .anyMatch(declared -> declared.getAnnotationType().getName().equals(annotation));
    }
}
