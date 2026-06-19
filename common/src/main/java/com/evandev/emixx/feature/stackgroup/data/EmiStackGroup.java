package com.evandev.emixx.feature.stackgroup.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.evandev.EmiPlusPlus;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.*;

public class EmiStackGroup extends StackGroup {
    private final Map<ResourceLocation, List<EmiIngredient>> targetMap;
    private final Set<ResourceLocation> allTargetIds;
    private final Set<ResourceLocation> excludedIds;

    public EmiStackGroup(ResourceLocation id, Set<EmiIngredient> targets, Set<ResourceLocation> excludedIds, Component name) {
        super(id, name);
        this.excludedIds = excludedIds;

        Map<ResourceLocation, List<EmiIngredient>> tempMap = new HashMap<>();
        Set<ResourceLocation> tempIds = new HashSet<>();
        for (EmiIngredient ingredient : targets) {
            for (EmiStack stack : ingredient.getEmiStacks()) {
                ResourceLocation stackId = stack.getId();
                tempMap.computeIfAbsent(stackId, k -> new ArrayList<>()).add(ingredient);
                tempIds.add(stackId);
            }
        }
        this.targetMap = tempMap;
        this.allTargetIds = tempIds;
    }

    @Override
    public Set<ResourceLocation> getOptimizedIds() {
        return allTargetIds;
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack emiStack)) return false;
        ResourceLocation stackId = emiStack.getId();
        if (excludedIds.contains(stackId)) return false;
        List<EmiIngredient> relevant = targetMap.get(stackId);
        if (relevant == null) return false;
        for (EmiIngredient target : relevant) {
            for (EmiStack ts : target.getEmiStacks()) {
                if (ts.getId().equals(stackId) && ts.getClass() == emiStack.getClass()) return true;
            }
        }
        return false;
    }

    private static JsonElement normalizeIngredientJson(JsonElement element) {
        if (!element.isJsonPrimitive()) return element;
        String str = element.getAsString();
        long colonCount = str.chars().filter(c -> c == ':').count();

        if (colonCount == 2) {
            String type = str.substring(0, str.indexOf(':'));
            String id = str.substring(str.indexOf(':') + 1);
            if (str.startsWith("#")) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", "tag");
                obj.addProperty("registry", type.substring(1));
                obj.addProperty("id", id);
                obj.addProperty("tag", id);
                return obj;
            } else {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", type);
                obj.addProperty("id", id);
                return obj;
            }
        }

        if (str.startsWith("#")) {
            String value = str.substring(1);
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "tag");
            obj.addProperty("id", value);
            obj.addProperty("tag", value);
            obj.addProperty("registry", "minecraft:item");
            return obj;
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "item");
        obj.addProperty("id", str);
        return obj;
    }

    private static EmiIngredient deserialize(JsonElement element) {
        return EmiIngredientSerializer.getDeserialized(normalizeIngredientJson(element));
    }

    public static EmiStackGroup parse(JsonElement json, ResourceLocation filenameId) {
        try {
            if (!(json instanceof JsonObject obj)) throw new IllegalArgumentException("Not a JSON object");

            ResourceLocation finalId = obj.has("id")
                    ? ResourceLocation.parse(GsonHelper.getAsString(obj, "id"))
                    : filenameId;

            String nameKey = obj.has("name") ? GsonHelper.getAsString(obj, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;

            if (!GsonHelper.isArrayNode(obj, "contents"))
                throw new IllegalArgumentException("Contents are either not present or not a list");

            Set<EmiIngredient> targets = Sets.newHashSet();
            for (JsonElement e : obj.getAsJsonArray("contents")) {
                targets.add(deserialize(e));
            }

            Set<ResourceLocation> excluded = new HashSet<>();
            if (GsonHelper.isArrayNode(obj, "exclusions")) {
                for (JsonElement e : obj.getAsJsonArray("exclusions")) {
                    for (EmiStack s : deserialize(e).getEmiStacks()) {
                        excluded.add(s.getId());
                    }
                }
            }

            return new EmiStackGroup(finalId, targets, excluded, customName);
        } catch (Exception e) {
            EmiPlusPlus.LOGGER.error("[EMI++] Failed to parse stack group {}: {}", filenameId, e.getMessage());
            return null;
        }
    }
}
