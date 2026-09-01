package dev.celerbi.easyfarmersdelightcompat.integration.attached;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class AttachedCropDefinition {
    public enum Tool {
        NONE,
        KNIFE,
        HOE,
        AXE;

        static Tool parse(String value) {
            try {
                return Tool.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unsupported tool requirement: " + value);
            }
        }
    }

    private final ResourceLocation id;
    private final ResourceLocation plantingItemId;
    private final TagKey<Item> plantingItemTag;
    private final ResourceLocation cropBlockId;
    private final ResourceLocation hostBlockId;
    private final TagKey<Block> hostBlockTag;
    private final String ageProperty;
    private final String facingProperty;
    private final int minAge;
    private final int maxAge;
    private final int matureAge;
    private final int postHarvestAge;
    private final String lootStrategy;
    private final boolean richSoil;
    private final Tool tool;

    private AttachedCropDefinition(
            ResourceLocation id,
            ResourceLocation plantingItemId,
            TagKey<Item> plantingItemTag,
            ResourceLocation cropBlockId,
            ResourceLocation hostBlockId,
            TagKey<Block> hostBlockTag,
            String ageProperty,
            String facingProperty,
            int minAge,
            int maxAge,
            int matureAge,
            int postHarvestAge,
            String lootStrategy,
            boolean richSoil,
            Tool tool
    ) {
        this.id = id;
        this.plantingItemId = plantingItemId;
        this.plantingItemTag = plantingItemTag;
        this.cropBlockId = cropBlockId;
        this.hostBlockId = hostBlockId;
        this.hostBlockTag = hostBlockTag;
        this.ageProperty = ageProperty;
        this.facingProperty = facingProperty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.matureAge = matureAge;
        this.postHarvestAge = postHarvestAge;
        this.lootStrategy = lootStrategy;
        this.richSoil = richSoil;
        this.tool = tool;
    }

    public static AttachedCropDefinition parse(ResourceLocation id, JsonObject json) {
        JsonObject planting = requireObject(json, "planting");
        ResourceLocation plantingItemId = optionalLocation(planting, "item");
        ResourceLocation plantingTagId = optionalLocation(planting, "tag");
        if ((plantingItemId == null) == (plantingTagId == null)) {
            throw new JsonParseException("planting must define exactly one of item or tag");
        }

        ResourceLocation cropBlockId = requiredLocation(json, "crop_block");

        JsonObject host = requireObject(json, "host");
        ResourceLocation hostBlockId = optionalLocation(host, "block");
        ResourceLocation hostTagId = optionalLocation(host, "tag");
        if ((hostBlockId == null) == (hostTagId == null)) {
            throw new JsonParseException("host must define exactly one of block or tag");
        }

        JsonObject age = requireObject(json, "age");
        String ageProperty = requiredString(age, "property");
        int minAge = requiredInt(age, "min");
        int maxAge = requiredInt(age, "max");
        int matureAge = requiredInt(age, "mature");
        int postHarvestAge = requiredInt(age, "post_harvest");
        if (minAge < 0 || maxAge < minAge || matureAge < minAge || matureAge > maxAge
                || postHarvestAge < minAge || postHarvestAge > maxAge) {
            throw new JsonParseException("invalid age range");
        }

        String facingProperty = json.has("facing_property")
                ? requiredString(json, "facing_property")
                : "facing";
        String lootStrategy = json.has("loot_strategy")
                ? requiredString(json, "loot_strategy")
                : "block_loot";
        if (!"block_loot".equals(lootStrategy)) {
            throw new JsonParseException("unsupported loot_strategy: " + lootStrategy);
        }
        boolean richSoil = !json.has("rich_soil") || json.get("rich_soil").getAsBoolean();
        Tool tool = Tool.parse(json.has("tool") ? requiredString(json, "tool") : "none");

        if (plantingItemId != null && BuiltInRegistries.ITEM.getOptional(plantingItemId)
                .filter(item -> item != net.minecraft.world.item.Items.AIR)
                .isEmpty()) {
            return null;
        }
        Block cropBlock = BuiltInRegistries.BLOCK.getOptional(cropBlockId)
                .filter(block -> block != Blocks.AIR)
                .orElse(null);
        if (cropBlock == null) {
            return null;
        }
        if (hostBlockId != null
                && BuiltInRegistries.BLOCK.getOptional(hostBlockId).filter(block -> block != Blocks.AIR).isEmpty()) {
            return null;
        }

        BlockState defaultCropState = cropBlock.defaultBlockState();
        Property<?> rawAgeProperty = defaultCropState.getProperties().stream()
                .filter(property -> property.getName().equals(ageProperty))
                .findFirst()
                .orElseThrow(() -> new JsonParseException(
                        "crop block " + cropBlockId + " has no age property '" + ageProperty + "'"));
        if (!(rawAgeProperty instanceof IntegerProperty integerAgeProperty)) {
            throw new JsonParseException("age property '" + ageProperty + "' is not integer-valued");
        }
        int actualMinAge = integerAgeProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(minAge);
        int actualMaxAge = integerAgeProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(maxAge);
        if (minAge < actualMinAge || maxAge > actualMaxAge) {
            throw new JsonParseException("configured age range " + minAge + ".." + maxAge
                    + " is outside block range " + actualMinAge + ".." + actualMaxAge);
        }
        if (!facingProperty.isBlank()) {
            Property<?> rawFacingProperty = defaultCropState.getProperties().stream()
                    .filter(property -> property.getName().equals(facingProperty))
                    .findFirst()
                    .orElseThrow(() -> new JsonParseException(
                            "crop block " + cropBlockId + " has no facing property '" + facingProperty + "'"));
            if (!(rawFacingProperty instanceof DirectionProperty)) {
                throw new JsonParseException("facing property '" + facingProperty + "' is not directional");
            }
        }

        TagKey<Item> plantingItemTag = plantingTagId == null ? null : TagKey.create(Registries.ITEM, plantingTagId);
        TagKey<Block> hostBlockTag = hostTagId == null ? null : TagKey.create(Registries.BLOCK, hostTagId);
        return new AttachedCropDefinition(
                id,
                plantingItemId,
                plantingItemTag,
                cropBlockId,
                hostBlockId,
                hostBlockTag,
                ageProperty,
                facingProperty,
                minAge,
                maxAge,
                matureAge,
                postHarvestAge,
                lootStrategy,
                richSoil,
                tool
        );
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation cropBlockId() {
        return cropBlockId;
    }

    public String ageProperty() {
        return ageProperty;
    }

    public String facingProperty() {
        return facingProperty;
    }

    public int minAge() {
        return minAge;
    }

    public int maxAge() {
        return maxAge;
    }

    public int matureAge() {
        return matureAge;
    }

    public int postHarvestAge() {
        return postHarvestAge;
    }

    public boolean richSoil() {
        return richSoil;
    }

    public Tool tool() {
        return tool;
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

    public boolean matchesHost(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (hostBlockId != null) {
            return hostBlockId.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }
        return hostBlockTag != null && state.is(hostBlockTag);
    }

    public ItemStack canonicalPlantingStack() {
        if (plantingItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(plantingItemId);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public Block canonicalHostBlock() {
        if (hostBlockId != null) {
            return BuiltInRegistries.BLOCK.get(hostBlockId);
        }
        if (hostBlockTag == null) {
            return Blocks.AIR;
        }

        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block != Blocks.AIR && block.defaultBlockState().is(hostBlockTag))
                .sorted((left, right) -> {
                    int leftRank = hostPreference(left);
                    int rightRank = hostPreference(right);
                    if (leftRank != rightRank) {
                        return Integer.compare(leftRank, rightRank);
                    }
                    ResourceLocation leftId = BuiltInRegistries.BLOCK.getKey(left);
                    ResourceLocation rightId = BuiltInRegistries.BLOCK.getKey(right);
                    return leftId.toString().compareTo(rightId.toString());
                })
                .findFirst()
                .orElse(Blocks.AIR);
    }

    private static int hostPreference(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        if (path.startsWith("stripped_")) {
            return 4;
        }
        if (path.endsWith("_log") || path.endsWith("_stem")) {
            return 0;
        }
        if (path.endsWith("_wood") || path.endsWith("_hyphae")) {
            return 2;
        }
        return 1;
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
