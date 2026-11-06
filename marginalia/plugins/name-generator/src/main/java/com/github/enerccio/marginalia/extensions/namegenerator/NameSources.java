package com.github.enerccio.marginalia.extensions.namegenerator;

import org.osgi.framework.Bundle;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * The categories other extensions registered (see {@link NameGeneratorExtension#registerWords} and {@link NameGeneratorExtension#registerGenerator}),
 * each owned by the bundle that registered it. The generator is only a proxy for them: for a <b>words</b> category it asks the owner for its words
 * and makes the names with its own Markov chain (trained again when the words changed since the last call), for a <b>generator</b> category it asks the
 * owner for the names.
 * <p>
 * A category of a bundle that is not running any more is dropped the next time the registry is looked at, so an extension that is unloaded without
 * unregistering leaves nothing behind (and nothing keeps calling into its old class loader).
 */
final class NameSources {

    /** The order of the chain trained from the words of an extension, when it does not say. */
    static final int DEFAULT_ORDER = 2;

    private static final int ALIVE = Bundle.STARTING | Bundle.ACTIVE;

    private abstract static class Source {
        final Bundle owner;
        final String title;

        Source(Bundle owner, String title) {
            this.owner = owner;
            this.title = title;
        }

        abstract List<String> generate(int count, Integer minLength, Integer maxLength, RandomGenerator random);

        boolean alive() {
            return (owner.getState() & ALIVE) != 0;
        }

        String ownerName() {
            return owner.getSymbolicName();
        }
    }

    /**
     * A chain trained on words, kept for as long as the words stay the same: the owner is asked for its words at every call and the chain is made
     * again only when they are not the ones of the last call.
     */
    private static final class Trained {
        private List<String> trained = List.of();
        private NameTables tables;

        synchronized List<String> generate(String title, Collection<String> given, int order, Integer minLength, Integer maxLength,
                                           Integer min, Integer max, RandomGenerator random, int count, String ownerName) {
            List<String> normalized = given == null ? List.of() : given.stream().map(NameTables::normalize).filter(Objects::nonNull)
                    .distinct().sorted().toList();
            if (normalized.isEmpty()) {
                throw new IllegalStateException("The category " + title + " of " + ownerName + " has no words to make names from");
            }
            if (tables == null || !normalized.equals(trained)) {
                tables = new NameTables(List.of(NameTables.train(title, normalized, order, minLength, maxLength)));
                trained = normalized;
            }
            return tables.generate(title, count, min, max, random);
        }
    }

    /** The words of the owner, the chain is made here. */
    private static final class Words extends Source {
        private final Supplier<? extends Collection<String>> words;
        private final int order;
        private final Integer minLength;
        private final Integer maxLength;
        private final Trained chain = new Trained();

        Words(Bundle owner, String title, Supplier<? extends Collection<String>> words, int order, Integer minLength, Integer maxLength) {
            super(owner, title);
            this.words = words;
            this.order = order;
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        @Override
        List<String> generate(int count, Integer min, Integer max, RandomGenerator random) {
            Collection<String> given;
            try {
                given = words.get();
            } catch (RuntimeException e) {
                throw failed(this, e);
            }
            return chain.generate(title, given, order, minLength, maxLength, min, max, random, count, ownerName());
        }
    }

    /**
     * Categories that an extension answers for at the moment they are asked about, on the thread of the caller. The provider decides what
     * exists <i>for this caller</i> (the languages of the user whose session the thread has), so one registration serves every user and nobody
     * sees the categories of another. Its chains are kept by title.
     */
    private static final class Provider {
        final Bundle owner;
        final String key;
        final Supplier<? extends Map<String, ? extends Collection<String>>> categories;
        final Map<String, Trained> chains = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Provider(Bundle owner, String key, Supplier<? extends Map<String, ? extends Collection<String>>> categories) {
            this.owner = owner;
            this.key = key;
            this.categories = categories;
        }

        boolean alive() {
            return (owner.getState() & ALIVE) != 0;
        }

        String ownerName() {
            return owner.getSymbolicName();
        }

        /** What the provider has for the caller; none when it fails (a thread with no session is such a case). */
        Map<String, Collection<String>> current() {
            Map<String, Collection<String>> found = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            try {
                Map<String, ? extends Collection<String>> given = categories.get();
                if (given != null) {
                    given.forEach((title, words) -> {
                        if (title != null && !title.isBlank() && words != null && !words.isEmpty()) {
                            found.putIfAbsent(title.strip(), List.copyOf(words));
                        }
                    });
                }
            } catch (RuntimeException e) {
                // a provider that can't answer here has nothing for this caller
            }
            return found;
        }

        List<String> generate(String title, Collection<String> words, int count, Integer min, Integer max, RandomGenerator random) {
            Trained chain;
            synchronized (chains) {
                chain = chains.computeIfAbsent(title, _ -> new Trained());
            }
            return chain.generate(title, words, DEFAULT_ORDER, null, null, min, max, random, count, ownerName());
        }
    }

    /** The owner makes the names. */
    private static final class Generated extends Source {
        private final Function<Map<String, Object>, List<String>> generator;

        Generated(Bundle owner, String title, Function<Map<String, Object>, List<String>> generator) {
            super(owner, title);
            this.generator = generator;
        }

        @Override
        List<String> generate(int count, Integer minLength, Integer maxLength, RandomGenerator random) {
            Map<String, Object> request = new HashMap<>();
            request.put(NameGeneratorExtension.REQUEST_COUNT, count);
            if (minLength != null) {
                request.put(NameGeneratorExtension.REQUEST_MIN_LENGTH, minLength);
            }
            if (maxLength != null) {
                request.put(NameGeneratorExtension.REQUEST_MAX_LENGTH, maxLength);
            }
            List<String> made;
            try {
                made = generator.apply(Collections.unmodifiableMap(request));
            } catch (RuntimeException e) {
                throw failed(this, e);
            }
            return made == null ? List.of() : made.stream().filter(Objects::nonNull).map(String::strip).filter(n -> !n.isEmpty())
                    .distinct().limit(count).toList();
        }
    }

    /**
     * Names made from words without registering anything: what an extension shows before the user keeps a selection of words.
     *
     * @throws IllegalArgumentException when no word is usable
     */
    static List<String> preview(Collection<String> words, Map<String, Object> options, int count, RandomGenerator random) {
        Map<String, Object> given = options == null ? Map.of() : options;
        int order = Math.clamp(integer(given, NameGeneratorExtension.OPTION_ORDER, DEFAULT_ORDER), 1, NameTables.Chain.MAX_ORDER);
        NameTables.Category category = NameTables.train("preview", words == null ? List.of() : words, order,
                integer(given, NameGeneratorExtension.OPTION_MIN_LENGTH, null), integer(given, NameGeneratorExtension.OPTION_MAX_LENGTH, null));
        return new NameTables(List.of(category)).generate("preview", Math.clamp(count, 1, NameGeneratorFunctionsImpl.MAX_COUNT), null, null, random);
    }

    private static IllegalStateException failed(Source source, RuntimeException e) {
        return new IllegalStateException(source.ownerName() + " could not make names of " + source.title + ": " + e.getMessage(), e);
    }

    private final Map<String, Source> sources = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /** The providers by the symbolic name of the owner and the key. */
    private final Map<String, Provider> providers = new LinkedHashMap<>();

    /**
     * @param reserved true for a title the generator has itself (a category or a group), which can't be taken
     * @throws IllegalArgumentException for a title that is blank, reserved or another extension's, or for an owner or words that are missing
     */
    synchronized void registerWords(Bundle owner, String title, Supplier<? extends Collection<String>> words, Map<String, Object> options,
                                    java.util.function.Predicate<String> reserved) {
        if (words == null) {
            throw new IllegalArgumentException("The words are missing");
        }
        String key = checked(owner, title, reserved);
        Map<String, Object> given = options == null ? Map.of() : options;
        int order = Math.clamp(integer(given, NameGeneratorExtension.OPTION_ORDER, DEFAULT_ORDER), 1, NameTables.Chain.MAX_ORDER);
        sources.put(key, new Words(owner, key, words, order, integer(given, NameGeneratorExtension.OPTION_MIN_LENGTH, null),
                integer(given, NameGeneratorExtension.OPTION_MAX_LENGTH, null)));
    }

    synchronized void registerGenerator(Bundle owner, String title, Function<Map<String, Object>, List<String>> generator,
                                        java.util.function.Predicate<String> reserved) {
        if (generator == null) {
            throw new IllegalArgumentException("The generator is missing");
        }
        String key = checked(owner, title, reserved);
        sources.put(key, new Generated(owner, key, generator));
    }

    private String checked(Bundle owner, String title, java.util.function.Predicate<String> reserved) {
        if (owner == null) {
            throw new IllegalArgumentException("The owner bundle is missing");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("The category has no name");
        }
        String key = title.strip();
        if (reserved.test(key)) {
            throw new IllegalArgumentException("'" + key + "' is a category of the name generator, choose another name");
        }
        dropDead();
        Source taken = sources.get(key);
        if (taken != null && !Objects.equals(taken.ownerName(), owner.getSymbolicName())) {
            throw new IllegalArgumentException("'" + key + "' is a category of " + taken.ownerName());
        }
        return key;
    }

    private static Integer integer(Map<String, Object> options, String key, Integer fallback) {
        Object value = options.get(key);
        return value instanceof Number number ? Integer.valueOf(number.intValue()) : fallback;
    }

    /**
     * Registers (or replaces) a provider of categories: asked for them whenever the categories are listed or generated, on the thread of the
     * caller.
     *
     * @throws IllegalArgumentException for a missing owner, key or supplier
     */
    synchronized void registerProvider(Bundle owner, String key, Supplier<? extends Map<String, ? extends Collection<String>>> categories) {
        if (owner == null || key == null || key.isBlank() || categories == null) {
            throw new IllegalArgumentException("The owner, the key and the categories are needed");
        }
        providers.put(providerId(owner, key), new Provider(owner, key.strip(), categories));
    }

    /** Removes the provider of the owner; false when it has none of that key. */
    synchronized boolean unregisterProvider(Bundle owner, String key) {
        return owner != null && key != null && providers.remove(providerId(owner, key)) != null;
    }

    private static String providerId(Bundle owner, String key) {
        return owner.getSymbolicName() + '\n' + key.strip();
    }

    /** Removes the category of the owner; false when it has none of that name. */
    synchronized boolean unregister(Bundle owner, String title) {
        if (owner == null || title == null) {
            return false;
        }
        Source source = sources.get(title.strip());
        if (source == null || !Objects.equals(source.ownerName(), owner.getSymbolicName())) {
            return false;
        }
        sources.remove(title.strip());
        return true;
    }

    /** Removes every category of the owner, returns how many there were. */
    synchronized int unregisterAll(Bundle owner) {
        if (owner == null) {
            return 0;
        }
        int before = sources.size() + providers.size();
        sources.values().removeIf(s -> Objects.equals(s.ownerName(), owner.getSymbolicName()));
        providers.values().removeIf(p -> Objects.equals(p.ownerName(), owner.getSymbolicName()));
        return before - sources.size() - providers.size();
    }

    synchronized void clear() {
        sources.clear();
        providers.clear();
    }

    /** The titles of the categories of the running bundles and of what their providers have for the caller, sorted. */
    List<String> titles() {
        Set<String> titles = new TreeSet<>();
        for (Provider provider : liveProviders()) {
            titles.addAll(provider.current().keySet());
        }
        synchronized (this) {
            dropDead();
            titles.addAll(sources.keySet());
        }
        return List.copyOf(titles);
    }

    boolean has(String title) {
        if (title == null) {
            return false;
        }
        synchronized (this) {
            dropDead();
            if (sources.containsKey(title.strip())) {
                return true;
            }
        }
        return liveProviders().stream().anyMatch(provider -> provider.current().containsKey(title.strip()));
    }

    /**
     * Up to {@code count} names of the category, made by its owner's words or by the owner; the call into the owner is not under the lock of the
     * registry.
     *
     * @throws IllegalArgumentException when there is no such category
     * @throws IllegalStateException    when the owner failed or has no words
     */
    List<String> generate(String title, int count, Integer minLength, Integer maxLength, RandomGenerator random) {
        String key = title == null ? null : title.strip();
        Source source;
        synchronized (this) {
            dropDead();
            source = key == null ? null : sources.get(key);
        }
        if (source != null) {
            return source.generate(count, minLength, maxLength, random);
        }
        if (key != null) {
            for (Provider provider : liveProviders()) {
                Collection<String> words = provider.current().get(key);
                if (words != null) {
                    return provider.generate(key, words, count, minLength, maxLength, random);
                }
            }
        }
        throw new IllegalArgumentException("Unknown category " + title);
    }

    private synchronized List<Provider> liveProviders() {
        providers.values().removeIf(p -> !p.alive());
        return List.copyOf(providers.values());
    }

    private void dropDead() {
        sources.values().removeIf(s -> !s.alive());
    }
}
