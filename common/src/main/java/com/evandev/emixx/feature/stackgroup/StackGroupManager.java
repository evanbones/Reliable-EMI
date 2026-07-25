package com.evandev.emixx.feature.stackgroup;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.stackgroup.data.EmiStackGroup;
import com.evandev.emixx.feature.stackgroup.data.RegexStackGroup;
import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import com.evandev.emixx.feature.stackgroup.data.groups.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class StackGroupManager {
    public static final List<StackGroup> stackGroups = new ArrayList<>();
    public static final List<EmiStack> groupedEmiStacks = new ArrayList<>();
    public static final IdentityHashMap<EmiStack, List<GroupedEmiStack<EmiStack>>> stackToGroupedStacks = new IdentityHashMap<>();
    private static final Map<String, BiFunction<ResourceLocation, JsonObject, StackGroup>> typeRegistry = new HashMap<>();
    private static final Map<ResourceLocation, List<GroupedEmiStack<EmiStack>>> itemToGroupedStacks = new HashMap<>();
    public static Map<StackGroup, EmiGroupStack> groupToGroupStacks = new HashMap<>();

    static {
        registerType("emixx:group", (id, json) -> EmiStackGroup.parse(json, id));

        registerType("emixx:tag", (id, json) -> {
            String tagName = GsonHelper.getAsString(json, "tag");
            String registryName = GsonHelper.getAsString(json, "registry", "minecraft:item");
            @SuppressWarnings("rawtypes")
            TagKey tagKey = TagKey.create(
                    ResourceKey.createRegistryKey(ResourceLocation.parse(registryName)),
                    ResourceLocation.parse(tagName));
            String nameKey = json.has("name") ? GsonHelper.getAsString(json, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;
            @SuppressWarnings("unchecked")
            EmiIngredient ingredient = EmiIngredient.of(tagKey);

            return new EmiStackGroup(id, Set.of(ingredient), Set.of(), List.of(), customName);
        });

        registerType("emixx:spawn_eggs", (id, json) -> new SpawnEggItemGroup());
        registerType("emixx:pressure_plates", (id, json) -> new PressurePlateItemGroup());
        registerType("emixx:minecarts", (id, json) -> new MinecartItemGroup());
        registerType("emixx:infested_blocks", (id, json) -> new InfestedBlockItemGroup());
        registerType("emixx:copper_blocks", (id, json) -> new CopperBlockItemGroup());
        registerType("emixx:banner_patterns", (id, json) -> new BannerPatternItemGroup());
        registerType("emixx:animal_armors", (id, json) -> new AnimalArmorItemGroup());

        registerType("emixx:regex", (id, json) -> {
            String regexString = GsonHelper.getAsString(json, "regex");
            String nameKey = json.has("name") ? GsonHelper.getAsString(json, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;
            try {
                return new RegexStackGroup(id, Pattern.compile(regexString), customName);
            } catch (Exception e) {
                EmiPlusPlus.LOGGER.error("Invalid regex in stack group {}: {}", id, regexString, e);
                return null;
            }
        });
    }

    public static void registerType(String type, BiFunction<ResourceLocation, JsonObject, StackGroup> factory) {
        typeRegistry.put(type, factory);
    }

    public static Path getGroupPath(ResourceLocation tag) {
        String name = tag.getPath().replace('/', '_');
        String filename = tag.getNamespace() + "_" + name + ".json";
        return EmiPlusPlusConfig.getConfigDir().resolve("stack_groups").resolve(filename);
    }

    public static boolean hasGroup(ResourceLocation tag) {
        for (StackGroup g : stackGroups) if (g.getId().equals(tag)) return true;
        return false;
    }

    public static boolean isGroupEnabled(ResourceLocation tag) {
        for (StackGroup g : stackGroups) {
            if (g.getId().equals(tag)) {
                return g.isEnabled;
            }
        }
        return false;
    }

    public static void toggleTagGroup(ResourceLocation tag) {
        boolean currentlyEnabled = isGroupEnabled(tag);
        String idStr = tag.toString();
        if (currentlyEnabled) {
            if (!EmiPlusPlusConfig.disabledStackGroups.contains(idStr)) {
                EmiPlusPlusConfig.disabledStackGroups.add(idStr);
            }
        } else {
            EmiPlusPlusConfig.disabledStackGroups.remove(idStr);
            if (!hasGroup(tag)) {
                saveGroupConfig(tag, true);
            } else {
                Path file = getGroupPath(tag);
                if (Files.exists(file)) {
                    saveGroupConfig(tag, true);
                }
            }
        }
        EmiPlusPlusConfig.save();
        reload();
    }

    private static void saveGroupConfig(ResourceLocation tag, boolean enabled) {
        Path file = getGroupPath(tag);
        try {
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("type", "emixx:tag");
            json.addProperty("id", tag.toString());
            json.addProperty("tag", tag.toString());
            json.addProperty("enabled", enabled);
            try (var writer = Files.newBufferedWriter(file)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            EmiPlusPlus.LOGGER.error("Failed to save stack group", e);
        }
    }

    public static void appendStacksForMatchingGroups(String query, List<EmiStack> results) {
        String lower = query.toLowerCase(Locale.ROOT);
        Set<EmiStack> existing = new HashSet<>(results);

        for (StackGroup group : stackGroups) {
            if (!group.isEnabled) continue;
            EmiGroupStack gs = groupToGroupStacks.get(group);
            if (gs == null) continue;

            boolean match = false;

            if (group.getId().toString().toLowerCase(Locale.ROOT).contains(lower)) {
                match = true;
            } else if (gs.getName().getString().toLowerCase(Locale.ROOT).contains(lower)) {
                match = true;
            }

            if (match) {
                for (var item : gs.itemsNew) {
                    if (existing.add(item.realStack)) results.add(item.realStack);
                }
            }
        }
    }

    public static void reload() {
        stackGroups.clear();
        if (!EmiPlusPlusConfig.enableStackGroups) return;

        Map<ResourceLocation, StackGroup> loaded = new LinkedHashMap<>();

        var resourceManager = Minecraft.getInstance().getResourceManager();
        try {
            var resources = resourceManager.listResources("stack_groups", loc -> loc.getPath().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                var location = entry.getKey();
                var resource = entry.getValue();
                String namespace = location.getNamespace();
                String path = location.getPath().substring("stack_groups/".length());
                path = path.substring(0, path.length() - ".json".length());
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
                try (var reader = resource.openAsReader()) {
                    loadGroup(id, JsonParser.parseReader(reader).getAsJsonObject(), loaded);
                }
            }
        } catch (Exception e) {
            EmiPlusPlus.LOGGER.error("Failed to list stack groups", e);
        }

        Path configDir = EmiPlusPlusConfig.getConfigDir().resolve("stack_groups");
        if (Files.exists(configDir)) {
            try (var stream = Files.walk(configDir)) {
                stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json")).forEach(path -> {
                    try (var reader = Files.newBufferedReader(path)) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        String idString = json.has("id") ? json.get("id").getAsString()
                                : json.has("tag") ? json.get("tag").getAsString() : null;
                        if (idString != null) loadGroup(ResourceLocation.parse(idString), json, loaded);
                    } catch (Exception e) {
                        EmiPlusPlus.LOGGER.error("Failed to load user stack group {}", path, e);
                    }
                });
            } catch (Exception e) {
                EmiPlusPlus.LOGGER.error("Failed to list user stack groups", e);
            }
        }

        List<StackGroup> sorted = new ArrayList<>(loaded.values());
        sorted.sort(Comparator.<StackGroup>comparingInt(g -> -g.priority)
                .thenComparing(g -> g.getId().toString()));
        stackGroups.addAll(sorted);
    }

    private static void loadGroup(ResourceLocation id, JsonObject json, Map<ResourceLocation, StackGroup> loaded) {
        try {
            boolean jsonEnabled = GsonHelper.getAsBoolean(json, "enabled", true);
            String type = GsonHelper.getAsString(json, "type", "emixx:group");
            BiFunction<ResourceLocation, JsonObject, StackGroup> factory = typeRegistry.get(type);

            if (factory != null) {
                StackGroup group = factory.apply(id, json);
                if (group != null) {
                    if (json.has("priority")) {
                        group.priority = GsonHelper.getAsInt(json, "priority", group.priority);
                    }
                    if (!jsonEnabled || EmiPlusPlusConfig.disabledStackGroups.contains(id.toString())) {
                        group.isEnabled = false;
                    }
                    loaded.put(id, group);
                }
            } else if (!jsonEnabled) {
                if (loaded.containsKey(id)) {
                    loaded.get(id).isEnabled = false;
                }
            } else {
                EmiPlusPlus.LOGGER.error("Unknown stack group type '{}' for {}", type, id);
            }
        } catch (Exception e) {
            EmiPlusPlus.LOGGER.error("Failed to parse stack group {}", id, e);
        }
    }

    public static List<EmiStack> buildGroupedStacks(List<EmiStack> source) {
        List<EmiStack> result = new ArrayList<>(source.size());
        Set<StackGroup> addedGroups = Collections.newSetFromMap(new IdentityHashMap<>());
        IdentityHashMap<StackGroup, List<GroupedEmiStack<EmiStack>>> groupMatches = new IdentityHashMap<>();

        for (EmiStack emiStack : source) {
            List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(emiStack);
            if (variants == null) {
                List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(emiStack.getId());
                if (idVariants == null) continue;
                variants = new ArrayList<>();
                for (var v : idVariants) {
                    if (v.realStack.isEqual(emiStack, Comparison.compareComponents())) variants.add(v);
                }
                if (variants.isEmpty()) continue;
            }
            for (var grouped : variants) {
                groupMatches.computeIfAbsent(grouped.stackGroup, k -> new ArrayList<>()).add(grouped);
            }
        }

        for (EmiStack emiStack : source) {
            List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(emiStack);
            if (variants == null) {
                List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(emiStack.getId());
                if (idVariants != null) {
                    variants = new ArrayList<>();
                    for (var v : idVariants) {
                        if (v.realStack.isEqual(emiStack, Comparison.compareComponents())) variants.add(v);
                    }
                }
            }

            if (variants == null || variants.isEmpty()) {
                result.add(emiStack);
                continue;
            }

            boolean wasGrouped = false;
            for (var grouped : variants) {
                StackGroup group = grouped.stackGroup;
                List<GroupedEmiStack<EmiStack>> matches = groupMatches.get(group);
                if (matches == null) continue;
                if (group.isEnabled && matches.size() >= 2) {
                    if (addedGroups.add(group)) {
                        EmiGroupStack cached = groupToGroupStacks.get(group);
                        if (cached != null && cached.itemsNew.size() == matches.size()) {
                            result.add(cached);
                        } else {
                            result.add(new EmiGroupStack(group, new ArrayList<>(matches)));
                        }
                    }
                    wasGrouped = true;
                    break;
                }
            }
            if (!wasGrouped) result.add(emiStack);
        }
        return result;
    }

    public static void buildGroupedEmiStacksAndStackGroupToContents(List<EmiStack> source) {
        groupedEmiStacks.clear();
        itemToGroupedStacks.clear();
        stackToGroupedStacks.clear();

        Map<StackGroup, EmiGroupStack> localGroupMap = new IdentityHashMap<>();
        for (StackGroup g : stackGroups) localGroupMap.put(g, new EmiGroupStack(g, new ArrayList<>()));

        for (EmiStack stack : source) {
            ResourceLocation stackId = stack.getId();

            for (StackGroup group : stackGroups) {
                Set<ResourceLocation> optimizedIds = group.getOptimizedIds();
                if (optimizedIds != null && !optimizedIds.isEmpty()) {
                    if (!optimizedIds.contains(stackId)) continue;
                }

                if (group.match(stack)) {
                    registerMatch(group, stack, localGroupMap);
                    break;
                }
            }
        }

        groupToGroupStacks = localGroupMap;

        for (var entry : localGroupMap.entrySet()) {
            String groupId = entry.getKey().getId().toString();
            List<String> savedOrder = EmiPlusPlusConfig.stackGroupItemOrder.get(groupId);
            if (savedOrder != null && !savedOrder.isEmpty()) {
                entry.getValue().itemsNew.sort((a, b) -> {
                    int idxA = savedOrder.indexOf(a.realStack.getId().toString());
                    int idxB = savedOrder.indexOf(b.realStack.getId().toString());
                    if (idxA == -1 && idxB == -1) return 0;
                    if (idxA == -1) return 1;
                    if (idxB == -1) return -1;
                    return Integer.compare(idxA, idxB);
                });
            }
        }
    }

    private static void registerMatch(StackGroup group, EmiStack stack, Map<StackGroup, EmiGroupStack> groupStacksMap) {
        EmiGroupStack groupStack = groupStacksMap.get(group);
        if (groupStack == null) return;
        GroupedEmiStack<EmiStack> groupedStack = new GroupedEmiStack<>(stack, group);
        boolean added = groupStack.append(groupedStack);
        if (added && group.isEnabled) {
            boolean alreadyGrouped = false;
            for (EmiStack gs : groupedEmiStacks) {
                if (gs.isEqual(stack, Comparison.compareComponents())) {
                    alreadyGrouped = true;
                    break;
                }
            }
            if (!alreadyGrouped) groupedEmiStacks.add(stack);
            itemToGroupedStacks.computeIfAbsent(stack.getId(), k -> new ArrayList<>()).add(groupedStack);
            stackToGroupedStacks.computeIfAbsent(stack, k -> new ArrayList<>()).add(groupedStack);
        }
    }
}