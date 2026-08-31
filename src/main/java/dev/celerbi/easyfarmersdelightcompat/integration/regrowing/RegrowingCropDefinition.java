package dev.celerbi.easyfarmersdelightcompat.integration.regrowing;

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

public final class RegrowingCropDefinition {
    private final ResourceLocation id;
    private final ResourceLocation plantingItemId;
    private final TagKey<Item> plantingItemTag;
    private final ResourceLocation cropBlockId;
    private final String ageProperty;
    private final int minAge;
    private final int maxAge;
    private final int harvestAge;
    private final int postHarvestAge;
    private final ResourceLocation harvestItemId;
    private final int minCount;
    private final int maxCount;
    private final int fullAgeBonus;
    private final boolean richSoil;

    private RegrowingCropDefinition(
            ResourceLocation id,
            ResourceLocation plantingItemId,
            TagKey<Item> plantingItemTag,
            ResourceLocation cropBlockId,
            String ageProperty,
            int minAge,
            int maxAge,
            int harvestAge,
            int postHarvestAge,
            ResourceLocation harvestItemId,
            int minCount,
            int maxCount,
            int fullAgeBonus,
            boolean richSoil
    ) {
        this.id = id;
        this.plantingItemId = plantingItemId;
        this.plantingItemTag = plantingItemTag;
        this.cropBlockId = cropBlockId;
        this.ageProperty = ageProperty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.harvestAge = harvestAge;
        this.postHarvestAge = postHarvestAge;
        this.harvestItemId = harvestItemId;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.fullAgeBonus = fullAgeBonus;
        this.richSoil = richSoil;
    }

    public static RegrowingCropDefinition parse(ResourceLocation id, JsonObject json) {
        JsonObject planting = requireObject(json, "planting");
        ResourceLocation plantingItemId = optionalLocation(planting, "item");
        ResourceLocation plantingTagId = optionalLocation(planting, "tag");
        if ((plantingItemId == null) == (plantingTagId == null)) {
            throw new JsonParseException("planting must define exactly one of item or tag");
        }

        ResourceLocation cropBlockId = requiredLocation(json, "crop_block");

        JsonObject age = requireObject(json, "age");
        String ageProperty = requiredString(age, "property");
        int minAge = requiredInt(age, "min");
        int maxAge = requiredInt(age, "max");
        int harvestAge = requiredInt(age, "harvest");
        int postHarvestAge = requiredInt(age, "post_harvest");
        if (minAge < 0 || maxAge < minAge
                || harvestAge < minAge || harvestAge > maxAge
                || postHarvestAge < minAge || postHarvestAge > maxAge) {
            throw new JsonParseException("invalid age range");
        }

        JsonObject harvest = requireObject(json, "harvest");
        String strategy = requiredString(harvest, "strategy");
        if (!"random_item".equals(strategy)) {
            throw new JsonParseException("unsupported harvest strategy: " + strategy);
        }
        ResourceLocation harvestItemId = requiredLocation(harvest, "item");
        int minCount = requiredInt(harvest, "min_count");
        int maxCount = requiredInt(harvest, "max_count");
        int fullAgeBonus = harvest.has("full_age_bonus") ? requiredInt(harvest, "full_age_bonus") : 0;
        if (minCount < 0 || maxCount < minCount || fullAgeBonus < 0) {
            throw new JsonParseException("invalid harvest count range");
        }

        boolean richSoil = !json.has("rich_soil") || json.get("rich_soil").getAsBoolean();

        if (plantingItemId != null && BuiltInRegistries.ITEM.getOptional(plantingItemId)
                .filter(item -> item != Items.AIR)
                .isEmpty()) {
            return null;
        }
        if (BuiltInRegistries.ITEM.getOptional(harvestItemId)
                .filter(item -> item != Items.AIR)
                .isEmpty()) {
            return null;
        }

        Block cropBlock = BuiltInRegistries.BLOCK.getOptional(cropBlockId)
                .filter(block -> block != Blocks.AIR)
                .orElse(null);
        if (cropBlock == null) {
            return null;
        }

        BlockState defaultState = cropBlock.defaultBlockState();
        Property<?> rawAgeProperty = defaultState.getProperties().stream()
                .filter(property -> property.getName().equals(ageProperty))
                .findFirst()
                .orElseThrow(() -> new JsonParseException(
                        "crop block " + cropBlockId + " has no age property '" + ageProperty + "'"));
        if (!(rawAgeProperty instanceof IntegerProperty integerAgeProperty)) {
            throw new JsonParseException("age property '" + ageProperty + "' is not integer-valued");
        }

        int actualMin = integerAgeProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(minAge);
        int actualMax = integerAgeProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(maxAge);
        if (minAge < actualMin || maxAge > actualMax) {
            throw new JsonParseException("configured age range " + minAge + ".." + maxAge
                    + " is outside block range " + actualMin + ".." + actualMax);
        }

        TagKey<Item> plantingTag = plantingTagId == null ? null : TagKey.create(Registries.ITEM, plantingTagId);
        return new RegrowingCropDefinition(
                id,
                plantingItemId,
                plantingTag,
                cropBlockId,
                ageProperty,
                minAge,
                maxAge,
                harvestAge,
                postHarvestAge,
                harvestItemId,
                minCount,
                maxCount,
                fullAgeBonus,
                richSoil
        );
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation cropBlockId() {
        return cropBlockId;
    }

    public ResourceLocation cropBlockId() {
        return cropBlockId;
    }

    public String ageProperty() {
        return ageProperty;
    }

    public int minAge() {
        return minAge;
    }

    public int maxAge() {
        return maxAge;
    }

    public int harvestAge() {
        return harvestAge;
    }

    public int postHarvestAge() {
        return postHarvestAge;
    }

    public boolean richSoil() {
        return richSoil;
    }

    public boolean matchesPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (plantingItemId != null) {
            return plantingItemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return plantingItemTag != null && stack.is(plantingItemTag);
    }

    public boolean matchesCrop(BlockState state) {
        return state != null && cropBlockId.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public BlockState initialState() {
        Block block = BuiltInRegistries.BLOCK.get(cropBlockId);
        return withAge(block.defaultBlockState(), minAge);
    }

    public int age(BlockState state) {
        IntegerProperty property = findAgeProperty(state);
        return property == null ? minAge : state.getValue(property);
    }

    public BlockState withAge(BlockState state, int age) {
        IntegerProperty property = findAgeProperty(state);
        if (property == null) {
            return state;
        }
        int safe = Math.max(minAge, Math.min(maxAge, age));
        return state.setValue(property, safe);
    }

    public ItemStack canonicalPlantingStack() {
        if (plantingItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(plantingItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public ItemStack rollHarvest(RandomSource random, int age) {
        Item item = BuiltInRegistries.ITEM.get(harvestItemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        int count = minCount;
        if (maxCount > minCount) {
            count += random.nextInt(maxCount - minCount + 1);
        }
        if (age >= maxAge) {
            count += fullAgeBonus;
        }
        return count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private IntegerProperty findAgeProperty(BlockState state) {
        if (state == null) {
            return null;
        }
        return state.getProperties().stream()
                .filter(property -> property.getName().equals(ageProperty))
                .filter(IntegerProperty.class::isInstance)
                .map(IntegerProperty.class::cast)
                .findFirst()
                .orElse(null);
    }

    private static JsonObject requireObject(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            throw new JsonParseException("missing object: " + key);
        }
        return parent.getAsJsonObject(key);
    }

    private static String requiredString(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonPrimitive()) {
            throw new JsonParseException("missing string: " + key);
        }
        String value = parent.get(key).getAsString();
        if (value.isBlank()) {
            throw new JsonParseException("empty string: " + key);
        }
        return value;
    }

    private static int requiredInt(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonPrimitive()) {
            throw new JsonParseException("missing integer: " + key);
        }
        return parent.get(key).getAsInt();
    }

    private static ResourceLocation requiredLocation(JsonObject parent, String key) {
        ResourceLocation value = optionalLocation(parent, key);
        if (value == null) {
            throw new JsonParseException("missing or invalid resource location: " + key);
        }
        return value;
    }

    private static ResourceLocation optionalLocation(JsonObject parent, String key) {
        if (!parent.has(key)) {
            return null;
        }
        ResourceLocation value = ResourceLocation.tryParse(parent.get(key).getAsString());
        if (value == null) {
            throw new JsonParseException("invalid resource location: " + key);
        }
        return value;
    }
}
