package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ToolService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.tool.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class ToolServiceImpl implements ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolServiceImpl.class);

    @Autowired
    private CommandService commandService;

    /** The suite of commands is the one the provider gave, kept to remove exactly it. */
    private record Registered(String bundleIdentifier, String key, String name, ToolProvider provider, Object commands) {

        String ref() {
            return ToolSelection.ref(bundleIdentifier, key);
        }
    }

    /** A provider the selection turns on, with what it was asked for. */
    private record Active(Registered registered, List<String> items, ToolSelection.Entry entry) {}

    /** ref → provider, in the order of registration. */
    private final Map<String, Registered> providers = new LinkedHashMap<>();

    public void setCommandService(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public void registerProvider(String bundleIdentifier, String providerKey, String providerName, ToolProvider provider) {
        if (StringUtils.isBlank(bundleIdentifier) || StringUtils.isBlank(providerKey)) {
            throw new IllegalArgumentException("The bundle identifier and the provider key are required");
        }
        if (provider == null) {
            throw new IllegalArgumentException("The tool provider is required");
        }
        Object commands = provider.commands();
        Registered registered = new Registered(bundleIdentifier, providerKey, StringUtils.defaultIfBlank(providerName, providerKey),
                provider, commands);
        Registered previous;
        synchronized (this) {
            // putting an existing key again keeps its place
            previous = providers.put(registered.ref(), registered);
        }
        if (previous != null) {
            removeCommands(previous);
        }
        if (commands != null && commandService != null) {
            try {
                commandService.registerCommands(commands);
            } catch (RuntimeException e) {
                synchronized (this) {
                    providers.remove(registered.ref(), registered);
                }
                throw e;
            }
        }
    }

    @Override
    public void unregisterProvider(String bundleIdentifier, String providerKey) {
        Registered removed;
        synchronized (this) {
            removed = providers.remove(ToolSelection.ref(bundleIdentifier, providerKey));
        }
        if (removed != null) {
            removeCommands(removed);
        }
    }

    @Override
    public void unregisterAll(String bundleIdentifier) {
        List<Registered> removed;
        synchronized (this) {
            removed = providers.values().stream().filter(r -> r.bundleIdentifier().equals(bundleIdentifier)).toList();
            removed.forEach(r -> providers.remove(r.ref()));
        }
        removed.forEach(this::removeCommands);
    }

    private void removeCommands(Registered registered) {
        if (registered.commands() != null && commandService != null) {
            try {
                commandService.unregisterCommands(registered.commands());
            } catch (RuntimeException e) {
                log.warn("Cannot remove the commands of tool provider {}: {}", registered.ref(), e.getMessage());
            }
        }
    }

    private synchronized List<Registered> snapshot() {
        return new ArrayList<>(providers.values());
    }

    @Override
    public List<ToolProviderInfo> getProviders() {
        List<ToolProviderInfo> result = new ArrayList<>();
        for (Registered registered : snapshot()) {
            List<ToolItem> items = itemsOf(registered);
            if (items != null) {
                result.add(new ToolProviderInfo(registered.ref(), registered.name(), registered.provider().description(), items,
                        registered.provider().defaultHint(), registered.commands() != null));
            }
        }
        return result;
    }

    /** The items of a provider; null when it fails. */
    private List<ToolItem> itemsOf(Registered registered) {
        try {
            List<ToolItem> items = registered.provider().items();
            return items == null ? List.of() : List.copyOf(items);
        } catch (RuntimeException e) {
            log.warn("Tool provider {} cannot list its items: {}", registered.ref(), e.toString());
            return null;
        }
    }

    @Override
    public ToolSet resolve(ToolSelection selection, ToolRequest request) {
        if (selection == null || selection.isEmpty()) {
            return ToolSet.NONE;
        }
        FunctionSet functions = FunctionSet.empty();
        List<String> hints = new ArrayList<>();
        Map<String, String> owner = new HashMap<>();
        for (Active active : active(selection)) {
            Registered registered = active.registered();
            FunctionSet set;
            try {
                set = registered.provider().functions(active.items(), request);
            } catch (Exception e) {
                log.warn("Tool provider {} cannot give its functions: {}", registered.ref(), e.toString());
                continue;
            }
            if (set == null || set.isEmpty()) {
                continue;
            }
            for (var definition : set.getDefinitions()) {
                String other = owner.putIfAbsent(definition.getName(), registered.name());
                if (other != null) {
                    throw new IllegalStateException("The tool providers '" + other + "' and '" + registered.name()
                            + "' both have a function " + definition.getName());
                }
            }
            functions = functions.and(set);
            String hint = StringUtils.defaultIfBlank(active.entry().getHint(), registered.provider().defaultHint());
            if (StringUtils.isNotBlank(hint)) {
                hints.add(hint.strip());
            }
        }
        return functions.isEmpty() ? ToolSet.NONE : new ToolSet(functions, String.join("\n\n", hints));
    }

    /** The registered providers the selection turns on, in the order of registration, with the items that are still offered. */
    private List<Active> active(ToolSelection selection) {
        List<Active> active = new ArrayList<>();
        for (Registered registered : snapshot()) {
            ToolSelection.Entry entry = selection.get(registered.ref());
            if (entry == null) {
                continue;
            }
            List<ToolItem> items = itemsOf(registered);
            if (items == null) {
                continue;
            }
            if (items.isEmpty()) {
                if (entry.isEnabled()) {
                    active.add(new Active(registered, List.of(), entry));
                }
                continue;
            }
            Set<String> offered = items.stream().map(ToolItem::id).collect(Collectors.toSet());
            List<String> checked = entry.getItems().stream().filter(offered::contains).distinct().toList();
            if (!checked.isEmpty()) {
                active.add(new Active(registered, checked, entry));
            }
        }
        return active;
    }

}
