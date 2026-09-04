package dev.celerbi.easyfarmersdelightcompat.integration.orchard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class OrchardCropDefinition {
    public enum RenderStyle {
        BLOCK_AGE,
        APPLE;

        static RenderStyle parse(String value) {
            try {
                return RenderStyle.valueOf(value.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unsupported orchard render style: " + value);
            }
        }
    }

    private final ResourceLocation id;
    private final ResourceLocation plantingItemId;
    private final TagKey<Item> plantingItemTag;
    private final ResourceLocation renderBlockId;
    private final String ageProperty;
    private final int minAge;
    private final int maxAge;
    private final int matureAge;
    private final int postHarvestAge;
    private final ResourceLocation harvestItemId;
    private final int minCount;
    private final int maxCount;
    private final double bonusChance;
    private final int bonusCount;
    private final boolean richSoil;
    private final RenderStyle renderStyle;

    private OrchardCropDefinition(
            ResourceLocation id,
            ResourceLocation plantingItemId,
            TagKey<Item> plantingItemTag,
            ResourceLocation renderBlockId,
            String ageProperty,
            int minAge,
            int maxAge,
            int matureAge,
            int postHarvestAge,
            ResourceLocation harvestItemId,
            int minCount,
            int maxCount,
            double bonusChance,
            int bonusCount,
            boolean richSoil,
            RenderStyle renderStyle
    ) {
        this.id = id;
        this.plantingItemId = plantingItemId;
        this.plantingItemTag = plantingItemTag;
        this.renderBlockId = renderBlockId;
        this.ageProperty = ageProperty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.matureAge = matureAge;
        this.postHarvestAge = postHarvestAge;
        this.harvestItemId = harvestItemId;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.bonusChance = bonusChance;
        this.bonusCount = bonusCount;
        this.richSoil = richSoil;
        this.renderStyle = renderStyle;
    }

    public static OrchardCropDefinition parse(ResourceLocation id, JsonObject json) {
        JsonObject planting = requireObject(json, "planting");
        ResourceLocation plantingItemId = optionalLocation(planting, "item");
        ResourceLocation plantingTagId = optionalLocation(planting, "tag");
        if ((plantingItemId == null) == (plantingTagId == null)) {
            throw new JsonParseException("planting must define exactly one of item or tag");
        }

        ResourceLocation renderBlockId = requiredLocation(json, "render_block");
        JsonObject age = requireObject(json, "age");
        String ageProperty = age.has("property") ? age.get("property").getAsString() : "";
        int minAge = requiredInt(age, "min");
        int maxAge = requiredInt(age, "max");
        int matureAge = requiredInt(age, "mature");
        int postHarvestAge = requiredInt(age, "post_harvest");
        if (minAge < 0 || maxAge < minAge || matureAge < minAge || matureAge > maxAge
                || postHarvestAge < minAge || postHarvestAge > maxAge) {
            throw new JsonParseException("invalid orchard age range");
        }

        JsonObject harvest = requireObject(json, "harvest");
        ResourceLocation harvestItemId = requiredLocation(harvest, "item");
        int minCount = harvest.has("min") ? harvest.get("min").getAsInt() : 1;
        int maxCount = harvest.has("max") ? harvest.get("max").getAsInt() : minCount;
        double bonusChance = harvest.has("bonus_chance") ? harvest.get("bonus_chance").getAsDouble() : 0.0D;
        int bonusCount = harvest.has("bonus_count") ? harvest.get("bonus_count").getAsInt() : 0;
        if (minCount < 0 || maxCount < minCount || bonusChance < 0.0D || bonusChance > 1.0D || bonusCount < 0) {
            throw new JsonParseException("invalid orchard harvest configuration");
        }

        boolean richSoil = !json.has("rich_soil") || json.get("rich_soil").getAsBoolean();
        RenderStyle renderStyle = RenderStyle.parse(json.has("render_style")
                ? json.get("render_style").getAsString()
                : "block_age");

        if (plantingItemId != null && BuiltInRegistries.ITEM.getOptional(plantingItemId)
                .filter(item -> item != Items.AIR)
                .isEmpty()) {
            return null;
        }
        Block renderBlock = BuiltInRegistries.BLOCK.getOptional(renderBlockId)
                .filter(block -> block != Blocks.AIR)
                .orElse(null);
        if (renderBlock == null) {
            return null;
        }
        if (BuiltInRegistries.ITEM.getOptional(harvestItemId).filter(item -> item != Items.AIR).isEmpty()) {
            return null;
        }

        if (!ageProperty.isBlank()) {
            BlockState defaultState = renderBlock.defaultBlockState();
            Property<?> raw = defaultState.getProperties().stream()
                    .filter(property -> property.getName().equals(ageProperty))
                    .findFirst()
                    .orElseThrow(() -> new JsonParseException(
                            "render block " + renderBlockId + " has no age property '" + ageProperty + "'"));
            if (!(raw instanceof IntegerProperty integerProperty)) {
                throw new JsonParseException("age property '" + ageProperty + "' is not integer-valued");
            }
            int actualMin = integerProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(minAge);
            int actualMax = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(maxAge);
            if (minAge < actualMin || maxAge > actualMax) {
                throw new JsonParseException("configured orchard age range " + minAge + ".." + maxAge
                        + " is outside block range " + actualMin + ".." + actualMax);
            }
        }

        TagKey<Item> plantingTag = plantingTagId == null ? null : TagKey.create(Registries.ITEM, plantingTagId);
        return new OrchardCropDefinition(
                id, plantingItemId, plantingTag, renderBlockId, ageProperty,
                minAge, maxAge, matureAge, postHarvestAge,
                harvestItemId, minCount, maxCount, bonusChance, bonusCount,
                richSoil, renderStyle
        );
    }

    public ResourceLocation id() { return id; }
    public ResourceLocation renderBlockId() { return renderBlockId; }
    public String ageProperty() { return ageProperty; }
    public int minAge() { return minAge; }
    public int maxAge() { return maxAge; }
    public int matureAge() { return matureAge; }
    public int postHarvestAge() { return postHarvestAge; }
    public boolean richSoil() { return richSoil; }
    public RenderStyle renderStyle() { return renderStyle; }
    public ResourceLocation harvestItemId() { return harvestItemId; }

    public boolean matchesPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (plantingItemId != null) {
            return plantingItemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return plantingItemTag != null && stack.is(plantingItemTag);
    }

    public ItemStack canonicalPlantingStack() {
        if (plantingItemId == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(plantingItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public BlockState renderState(int age) {
        Block block = BuiltInRegistries.BLOCK.get(renderBlockId);
        if (block == null || block == Blocks.AIR) return Blocks.AIR.defaultBlockState();
        BlockState state = block.defaultBlockState();
        if (ageProperty.isBlank()) return state;
        Property<?> raw = state.getProperties().stream()
                .filter(property -> property.getName().equals(ageProperty))
                .findFirst().orElse(null);
        if (!(raw instanceof IntegerProperty integerProperty)) return state;
        int safe = Math.max(minAge, Math.min(maxAge, age));
        return state.setValue(integerProperty, safe);
    }

    public ItemStack harvestStack(RandomSource random) {
        Item item = BuiltInRegistries.ITEM.get(harvestItemId);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        int count = minCount;
        if (maxCount > minCount) {
            count += random.nextInt(maxCount - minCount + 1);
        }
        if (bonusCount > 0 && bonusChance > 0.0D && random.nextDouble() < bonusChance) {
            count += bonusCount;
        }
        return count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    public ItemStack harvestDisplayStack() {
        Item item = BuiltInRegistries.ITEM.get(harvestItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static JsonObject requireObject(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            throw new JsonParseException("missing object: " + key);
        }
        return parent.getAsJsonObject(key);
    }

    private static int requiredInt(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonPrimitive()) {
            throw new JsonParseException("missing integer: " + key);
        }
        return parent.get(key).getAsInt();
    }

    private static ResourceLocation requiredLocation(JsonObject parent, String key) {
        ResourceLocation value = optionalLocation(parent, key);
        if (value == null) throw new JsonParseException("missing or invalid resource location: " + key);
        return value;
    }

    private static ResourceLocation optionalLocation(JsonObject parent, String key) {
        if (!parent.has(key)) return null;
        ResourceLocation value = ResourceLocation.tryParse(parent.get(key).getAsString());
        if (value == null) throw new JsonParseException("invalid resource location: " + key);
        return value;
    }
}
