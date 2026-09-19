package dev.celerbi.easyfarmersdelightcompat.integration.tall;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Optional;
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

/**
 * Data-driven support for crops represented by a lower + upper block state.
 *
 * The block itself is never placed in the world by EFD; both halves are virtual
 * states rendered inside the Rich Farmer. This keeps compatibility independent
 * from a concrete DoublePlantBlock subclass used by another mod.
 */
public final class TallCropDefinition {
    public enum HarvestStrategy {
        RANDOM_ITEM,
        BLOCK_LOOT;

        static HarvestStrategy parse(String value) {
            try {
                return HarvestStrategy.valueOf(value.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unsupported tall-crop harvest strategy: " + value);
            }
        }
    }

    private final ResourceLocation id;
    private final ResourceLocation plantingItemId;
    private final TagKey<Item> plantingItemTag;
    private final ResourceLocation cropBlockId;
    private final String ageProperty;
    private final int minAge;
    private final int maxAge;
    private final int harvestAge;
    private final int postHarvestAge;
    private final String halfProperty;
    private final String lowerValue;
    private final String upperValue;
    private final int upperFromAge;
    private final HarvestStrategy harvestStrategy;
    private final ResourceLocation harvestItemId;
    private final ResourceLocation displayItemId;
    private final int minCount;
    private final int maxCount;
    private final int fullAgeBonus;
    private final boolean richSoil;

    private TallCropDefinition(
            ResourceLocation id,
            ResourceLocation plantingItemId,
            TagKey<Item> plantingItemTag,
            ResourceLocation cropBlockId,
            String ageProperty,
            int minAge,
            int maxAge,
            int harvestAge,
            int postHarvestAge,
            String halfProperty,
            String lowerValue,
            String upperValue,
            int upperFromAge,
            HarvestStrategy harvestStrategy,
            ResourceLocation harvestItemId,
            ResourceLocation displayItemId,
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
        this.halfProperty = halfProperty;
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
        this.upperFromAge = upperFromAge;
        this.harvestStrategy = harvestStrategy;
        this.harvestItemId = harvestItemId;
        this.displayItemId = displayItemId;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.fullAgeBonus = fullAgeBonus;
        this.richSoil = richSoil;
    }

    public static TallCropDefinition parse(ResourceLocation id, JsonObject json) {
        JsonObject planting = requireObject(json, "planting");
        ResourceLocation plantingItemId = optionalLocation(planting, "item");
        ResourceLocation plantingTagId = optionalLocation(planting, "tag");
        if ((plantingItemId == null) == (plantingTagId == null)) {
            throw new JsonParseException("planting must define exactly one of item or tag");
        }

        ResourceLocation cropBlockId = requiredLocation(json, "crop_block");
        JsonObject age = requireObject(json, "age");
        String ageProperty = age.has("property") ? age.get("property").getAsString() : "age";
        int minAge = requiredInt(age, "min");
        int maxAge = requiredInt(age, "max");
        int harvestAge = requiredInt(age, "harvest");
        int postHarvestAge = requiredInt(age, "post_harvest");
        if (minAge < 0 || maxAge < minAge || harvestAge < minAge || harvestAge > maxAge
                || postHarvestAge < minAge || postHarvestAge > maxAge) {
            throw new JsonParseException("invalid tall-crop age range");
        }

        JsonObject halves = json.has("halves") ? requireObject(json, "halves") : new JsonObject();
        String halfProperty = halves.has("property") ? requiredString(halves, "property") : "half";
        String lowerValue = halves.has("lower") ? requiredString(halves, "lower") : "lower";
        String upperValue = halves.has("upper") ? requiredString(halves, "upper") : "upper";
        int upperFromAge = halves.has("upper_from_age") ? halves.get("upper_from_age").getAsInt() : minAge;
        if (upperFromAge < minAge || upperFromAge > maxAge) {
            throw new JsonParseException("halves.upper_from_age must be inside the configured age range");
        }

        JsonObject harvest = requireObject(json, "harvest");
        HarvestStrategy harvestStrategy = HarvestStrategy.parse(requiredString(harvest, "strategy"));
        ResourceLocation harvestItemId = optionalLocation(harvest, "item");
        ResourceLocation displayItemId = optionalLocation(harvest, "display_item");
        int minCount = harvest.has("min_count") ? harvest.get("min_count").getAsInt() : 1;
        int maxCount = harvest.has("max_count") ? harvest.get("max_count").getAsInt() : minCount;
        int fullAgeBonus = harvest.has("full_age_bonus") ? harvest.get("full_age_bonus").getAsInt() : 0;
        if (harvestStrategy == HarvestStrategy.RANDOM_ITEM && harvestItemId == null) {
            throw new JsonParseException("random_item harvest requires harvest.item");
        }
        if (minCount < 0 || maxCount < minCount || fullAgeBonus < 0) {
            throw new JsonParseException("invalid tall-crop harvest counts");
        }

        boolean richSoil = !json.has("rich_soil") || json.get("rich_soil").getAsBoolean();

        if (plantingItemId != null && BuiltInRegistries.ITEM.getOptional(plantingItemId)
                .filter(item -> item != Items.AIR).isEmpty()) {
            return null;
        }
        Block cropBlock = BuiltInRegistries.BLOCK.getOptional(cropBlockId)
                .filter(block -> block != Blocks.AIR).orElse(null);
        if (cropBlock == null) {
            return null;
        }
        if (harvestItemId != null && BuiltInRegistries.ITEM.getOptional(harvestItemId)
                .filter(item -> item != Items.AIR).isEmpty()) {
            return null;
        }
        if (displayItemId != null && BuiltInRegistries.ITEM.getOptional(displayItemId)
                .filter(item -> item != Items.AIR).isEmpty()) {
            return null;
        }

        BlockState state = cropBlock.defaultBlockState();
        Property<?> ageRaw = findProperty(state, ageProperty);
        if (!(ageRaw instanceof IntegerProperty integerProperty)) {
            throw new JsonParseException("crop block " + cropBlockId + " has no integer age property '"
                    + ageProperty + "'");
        }
        int actualMin = integerProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(minAge);
        int actualMax = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(maxAge);
        if (minAge < actualMin || maxAge > actualMax) {
            throw new JsonParseException("configured age range " + minAge + ".." + maxAge
                    + " is outside block range " + actualMin + ".." + actualMax);
        }

        // Validate both serialized half values without coupling to DoubleBlockHalf at compile time.
        setSerializedProperty(state, halfProperty, lowerValue, true);
        setSerializedProperty(state, halfProperty, upperValue, true);

        TagKey<Item> plantingTag = plantingTagId == null ? null : TagKey.create(Registries.ITEM, plantingTagId);
        return new TallCropDefinition(
                id, plantingItemId, plantingTag, cropBlockId,
                ageProperty, minAge, maxAge, harvestAge, postHarvestAge,
                halfProperty, lowerValue, upperValue, upperFromAge,
                harvestStrategy, harvestItemId, displayItemId,
                minCount, maxCount, fullAgeBonus, richSoil
        );
    }

    public ResourceLocation id() { return id; }
    public ResourceLocation cropBlockId() { return cropBlockId; }
    public int minAge() { return minAge; }
    public int maxAge() { return maxAge; }
    public int harvestAge() { return harvestAge; }
    public int postHarvestAge() { return postHarvestAge; }
    public int upperFromAge() { return upperFromAge; }
    public boolean richSoil() { return richSoil; }
    public boolean usesBlockLoot() { return harvestStrategy == HarvestStrategy.BLOCK_LOOT; }

    public boolean matchesPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
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
        if (block == null || block == Blocks.AIR) return Blocks.AIR.defaultBlockState();
        return lowerState(withAge(block.defaultBlockState(), minAge));
    }

    public int age(BlockState state) {
        IntegerProperty property = findAgeProperty(state);
        return property == null ? minAge : state.getValue(property);
    }

    public BlockState withAge(BlockState state, int age) {
        IntegerProperty property = findAgeProperty(state);
        if (property == null) return state;
        int safe = Math.max(minAge, Math.min(maxAge, age));
        return state.setValue(property, safe);
    }

    public BlockState lowerState(BlockState state) {
        return setSerializedProperty(state, halfProperty, lowerValue, false);
    }

    public BlockState upperState(BlockState lowerState) {
        int currentAge = age(lowerState);
        if (currentAge < upperFromAge) {
            return Blocks.AIR.defaultBlockState();
        }
        return setSerializedProperty(withAge(lowerState, currentAge), halfProperty, upperValue, false);
    }

    public ItemStack canonicalPlantingStack() {
        if (plantingItemId == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(plantingItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public ItemStack harvestDisplayStack() {
        ResourceLocation id = displayItemId != null ? displayItemId : harvestItemId;
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public ItemStack rollHarvest(RandomSource random, int age) {
        if (harvestStrategy != HarvestStrategy.RANDOM_ITEM || harvestItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(harvestItemId);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
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
        Property<?> raw = findProperty(state, ageProperty);
        return raw instanceof IntegerProperty integerProperty ? integerProperty : null;
    }

    private static Property<?> findProperty(BlockState state, String name) {
        if (state == null) return null;
        return state.getProperties().stream()
                .filter(property -> property.getName().equals(name))
                .findFirst().orElse(null);
    }

    private static BlockState setSerializedProperty(BlockState state, String name, String value, boolean strict) {
        Property<?> property = findProperty(state, name);
        if (property == null) {
            if (strict) {
                throw new JsonParseException("block " + BuiltInRegistries.BLOCK.getKey(state.getBlock())
                        + " has no property '" + name + "'");
            }
            return state;
        }
        return setSerializedProperty(state, property, value, strict);
    }

    private static <T extends Comparable<T>> BlockState setSerializedProperty(
            BlockState state, Property<T> property, String value, boolean strict
    ) {
        Optional<T> parsed = property.getValue(value);
        if (parsed.isEmpty()) {
            if (strict) {
                throw new JsonParseException("property '" + property.getName() + "' does not accept value '"
                        + value + "'");
            }
            return state;
        }
        return state.setValue(property, parsed.get());
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
        if (value.isBlank()) throw new JsonParseException("empty string: " + key);
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
