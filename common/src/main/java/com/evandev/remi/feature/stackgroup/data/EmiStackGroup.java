package com.evandev.remi.feature.stackgroup.data;

import com.evandev.ReliableEmi;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class EmiStackGroup extends StackGroup {
    private final Map<ResourceLocation, List<EmiIngredient>> targetMap;
    private final Set<ResourceLocation> allTargetIds;
    private final Set<ResourceLocation> excludedIds;
    private final List<Pattern> regexes;

    public EmiStackGroup(ResourceLocation id, Set<EmiIngredient> targets, Set<ResourceLocation> excludedIds, List<Pattern> regexes, Component name) {
        super(id, name);
        this.excludedIds = excludedIds;
        this.regexes = regexes != null ? regexes : List.of();

        Map<ResourceLocation, List<EmiIngredient>> tempMap = new HashMap<>();
        Set<ResourceLocation> tempIds = new HashSet<>();
        for (EmiIngredient ingredient : targets) {
            for (EmiStack stack : getIngredientStacks(ingredient)) {
                ResourceLocation stackId = stack.getId();
                tempMap.computeIfAbsent(stackId, k -> new ArrayList<>()).add(ingredient);
                tempIds.add(stackId);
            }
        }
        this.targetMap = tempMap;
        this.allTargetIds = tempIds;
    }

    public static List<EmiStack> getIngredientStacks(EmiIngredient ingredient) {
        List<EmiStack> stacks = ingredient.getEmiStacks();
        if (stacks.isEmpty() && ingredient instanceof TagEmiIngredient tagIngredient) {
            TagKey<?> rawKey = tagIngredient.key;
            if (rawKey != null) {
                List<EmiStack> rawStacks = new ArrayList<>();
                try {
                    if (rawKey.registry().equals(BuiltInRegistries.BLOCK.key())) {
                        @SuppressWarnings("unchecked")
                        TagKey<Block> blockTagKey = (TagKey<Block>) rawKey;
                        var tagHolderList = BuiltInRegistries.BLOCK.getTag(blockTagKey);
                        if (tagHolderList.isPresent()) {
                            for (var holder : tagHolderList.get()) {
                                rawStacks.add(EmiStack.of(holder.value()));
                            }
                        }
                    } else {
                        @SuppressWarnings("unchecked")
                        TagKey<Item> itemTagKey = (TagKey<Item>) rawKey;
                        var tagHolderList = BuiltInRegistries.ITEM.getTag(itemTagKey);
                        if (tagHolderList.isPresent()) {
                            for (Holder<Item> holder : tagHolderList.get()) {
                                rawStacks.add(EmiStack.of(holder.value()));
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                if (!rawStacks.isEmpty()) {
                    return rawStacks;
                }
            }
        }
        return stacks;
    }

    private static String normalizeType(String typeStr) {
        if (typeStr == null) return "item";
        String lower = typeStr.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "jeed:effect", "jeed:effects", "jeed", "effect", "effects", "remi:effect", "emixx:effect",
                 "mob_effect", "mob_effects" -> "mob_effect";
            case "emixx:tag", "remi:tag" -> "tag";
            case "emixx:item", "remi:item", "emi:item" -> "item";
            case "emixx:fluid", "remi:fluid", "emi:fluid" -> "fluid";
            default -> typeStr;
        };
    }

    private static JsonElement normalizeIngredientJson(JsonElement element) {
        if (element == null || element.isJsonNull()) return element;

        if (!element.isJsonPrimitive()) {
            if (element instanceof JsonObject obj) {
                if (obj.has("type")) {
                    String typeStr = obj.get("type").getAsString();
                    String normType = normalizeType(typeStr);
                    if (!normType.equals(typeStr)) {
                        JsonObject copy = obj.deepCopy();
                        copy.addProperty("type", normType);
                        return copy;
                    }
                } else if (obj.has("id")) {
                    String idStr = obj.get("id").getAsString();
                    ResourceLocation resLoc = ResourceLocation.tryParse(idStr);
                    if (resLoc != null) {
                        JsonObject copy = obj.deepCopy();
                        if (BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                            copy.addProperty("type", "mob_effect");
                        } else if (BuiltInRegistries.FLUID.containsKey(resLoc)) {
                            copy.addProperty("type", "fluid");
                        } else {
                            copy.addProperty("type", "item");
                        }
                        return copy;
                    }
                }
            }
            return element;
        }

        String str = element.getAsString();
        if (str.startsWith("#")) {
            String value = str.substring(1);
            long colons = value.chars().filter(c -> c == ':').count();
            return getJsonObject(colons, value);
        }

        String[] split = str.split(":");
        if (split.length >= 4) {
            String prefix = split[0] + ":" + split[1];
            String id = split[2] + ":" + split[3];
            JsonObject obj = new JsonObject();
            obj.addProperty("type", normalizeType(prefix));
            obj.addProperty("id", id);
            return obj;
        } else if (split.length == 3) {
            String prefix = split[0];
            String id = split[1] + ":" + split[2];
            JsonObject obj = new JsonObject();
            obj.addProperty("type", normalizeType(prefix));
            obj.addProperty("id", id);
            return obj;
        } else if (split.length == 2) {
            ResourceLocation resLoc = ResourceLocation.tryParse(str);
            JsonObject obj = new JsonObject();
            obj.addProperty("id", str);
            if (resLoc != null && BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                obj.addProperty("type", "mob_effect");
            } else if (resLoc != null && BuiltInRegistries.FLUID.containsKey(resLoc)) {
                obj.addProperty("type", "fluid");
            } else {
                obj.addProperty("type", "item");
            }
            return obj;
        } else if (split.length == 1) {
            ResourceLocation resLoc = ResourceLocation.tryParse("minecraft:" + str);
            JsonObject obj = new JsonObject();
            obj.addProperty("id", "minecraft:" + str);
            if (resLoc != null && BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                obj.addProperty("type", "mob_effect");
            } else if (resLoc != null && BuiltInRegistries.FLUID.containsKey(resLoc)) {
                obj.addProperty("type", "fluid");
            } else {
                obj.addProperty("type", "item");
            }
            return obj;
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "item");
        obj.addProperty("id", str);
        return obj;
    }

    private static @NotNull JsonObject getJsonObject(long colons, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "tag");
        if (colons >= 2) {
            int firstColon = value.indexOf(':');
            String reg = value.substring(0, firstColon);
            String tagId = value.substring(firstColon + 1);
            obj.addProperty("registry", reg);
            obj.addProperty("id", tagId);
            obj.addProperty("tag", tagId);
        } else {
            obj.addProperty("id", value);
            obj.addProperty("tag", value);
            obj.addProperty("registry", "minecraft:item");
        }
        return obj;
    }

    private static EmiIngredient deserialize(JsonElement element) {
        return EmiIngredientSerializer.getDeserialized(normalizeIngredientJson(element));
    }

    private static Pattern compilePattern(String raw) {
        if (raw == null || raw.isEmpty()) return Pattern.compile("");
        String regexStr = raw;
        if (regexStr.contains("*") && !regexStr.contains(".*")) {
            regexStr = regexStr.replace("*", ".*");
        }
        try {
            return Pattern.compile(regexStr);
        } catch (Exception e) {
            try {
                return Pattern.compile(raw);
            } catch (Exception ignored) {
                return Pattern.compile("");
            }
        }
    }

    public static EmiStackGroup parse(JsonElement json, ResourceLocation filenameId) {
        try {
            if (!(json instanceof JsonObject obj)) throw new IllegalArgumentException("Not a JSON object");

            ResourceLocation finalId = obj.has("id")
                    ? ResourceLocation.parse(GsonHelper.getAsString(obj, "id"))
                    : filenameId;

            String nameKey = obj.has("name") ? GsonHelper.getAsString(obj, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;

            Set<EmiIngredient> targets = Sets.newHashSet();
            if (GsonHelper.isArrayNode(obj, "contents")) {
                for (JsonElement e : obj.getAsJsonArray("contents")) {
                    targets.add(deserialize(e));
                }
            }

            List<Pattern> regexes = new ArrayList<>();
            if (obj.has("regex")) {
                regexes.add(compilePattern(GsonHelper.getAsString(obj, "regex")));
            }
            if (GsonHelper.isArrayNode(obj, "regexes")) {
                for (JsonElement e : obj.getAsJsonArray("regexes")) {
                    regexes.add(compilePattern(e.getAsString()));
                }
            }

            if (targets.isEmpty() && regexes.isEmpty()) {
                throw new IllegalArgumentException("Contents or regex(es) must be present in group configuration.");
            }

            Set<ResourceLocation> excluded = new HashSet<>();
            if (GsonHelper.isArrayNode(obj, "exclusions")) {
                for (JsonElement e : obj.getAsJsonArray("exclusions")) {
                    for (EmiStack s : deserialize(e).getEmiStacks()) {
                        excluded.add(s.getId());
                    }
                }
            }

            return new EmiStackGroup(finalId, targets, excluded, regexes, customName);
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to parse stack group {}: {}", filenameId, e.getMessage());
            return null;
        }
    }

    private static String getStackType(EmiStack stack) {
        try {
            var serializer = dev.emi.emi.registry.EmiIngredientSerializers.BY_CLASS.get(stack.getClass());
            if (serializer != null) {
                return serializer.getType();
            }
        } catch (Exception ignored) {
        }
        String className = stack.getClass().getName();
        if (className.contains("Effect") || className.contains("Potion")) {
            return "mob_effect";
        }
        if (className.contains("Fluid")) {
            return "fluid";
        }
        if (!stack.getItemStack().isEmpty()) {
            return "item";
        }
        return null;
    }

    private static @NotNull List<String> getCandidates(String idStr, String typeStr) {
        List<String> candidates = new ArrayList<>();
        candidates.add(idStr);
        if (typeStr != null) {
            candidates.add(typeStr + ":" + idStr);
            switch (typeStr) {
                case "mob_effect" -> {
                    candidates.add("jeed:effect:" + idStr);
                    candidates.add("jeed:effects:" + idStr);
                    candidates.add("jeed:" + idStr);
                    candidates.add("effect:" + idStr);
                    candidates.add("effects:" + idStr);
                }
                case "fluid" -> {
                    candidates.add("remi:fluid:" + idStr);
                    candidates.add("emixx:fluid:" + idStr);
                }
                case "item" -> {
                    candidates.add("remi:item:" + idStr);
                    candidates.add("emixx:item:" + idStr);
                }
            }
        }
        return candidates;
    }

    @Override
    public Set<ResourceLocation> getOptimizedIds() {
        if (regexes != null && !regexes.isEmpty()) {
            return null;
        }
        return allTargetIds;
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack emiStack)) return false;
        ResourceLocation stackId = emiStack.getId();
        if (stackId == null) return false;

        if (excludedIds.contains(stackId)) return false;

        if (!regexes.isEmpty()) {
            String idStr = stackId.toString();
            String typeStr = getStackType(emiStack);

            List<String> candidates = getCandidates(idStr, typeStr);

            for (Pattern pattern : regexes) {
                for (String candidate : candidates) {
                    if (pattern.matcher(candidate).matches()) return true;
                }
            }
        }

        List<EmiIngredient> relevant = targetMap.get(stackId);
        if (relevant != null) {
            for (EmiIngredient target : relevant) {
                for (EmiStack ts : getIngredientStacks(target)) {
                    if (ts.getId().equals(stackId) && (ts.getClass() == emiStack.getClass() || ts.getClass().isInstance(emiStack) || emiStack.getClass().isInstance(ts)))
                        return true;
                }
            }
        }

        return false;
    }
}