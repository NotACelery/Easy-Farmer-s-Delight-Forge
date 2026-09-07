package dev.celerbi.easyfarmersdelightcompat.integration.stem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Data-driven description for stem crops whose fruit cannot be inferred safely from vanilla APIs.
 * This is especially useful for modded stems that can create more than one fruit block.
 */
public final class StemCropDefinition {
    private final ResourceLocation id;
    private final ResourceLocation plantingItemId;
    private final TagKey<Item> plantingItemTag;
    private final ResourceLocation stemBlockId;
    private final ResourceLocation attachedStemBlockId;
    private final List<ResourceLocation> fruitBlockIds;

    private StemCropDefinition(
            ResourceLocation id,
            ResourceLocation plantingItemId,
            TagKey<Item> plantingItemTag,
            ResourceLocation stemBlockId,
            ResourceLocation attachedStemBlockId,
            List<ResourceLocation> fruitBlockIds
    ) {
        this.id = id;
        this.plantingItemId = plantingItemId;
        this.plantingItemTag = plantingItemTag;
        this.stemBlockId = stemBlockId;
        this.attachedStemBlockId = attachedStemBlockId;
        this.fruitBlockIds = List.copyOf(fruitBlockIds);
    }

    public static StemCropDefinition parse(ResourceLocation id, JsonObject json) {
        JsonObject planting = requireObject(json, "planting");
        ResourceLocation plantingItemId = optionalLocation(planting, "item");
        ResourceLocation plantingTagId = optionalLocation(planting, "tag");
        if ((plantingItemId == null) == (plantingTagId == null)) {
            throw new JsonParseException("planting must define exactly one of item or tag");
        }

        ResourceLocation stemBlockId = requiredLocation(json, "stem_block");
        ResourceLocation attachedStemBlockId = optionalLocation(json, "attached_stem_block");
        if (!json.has("fruit_blocks") || !json.get("fruit_blocks").isJsonArray()) {
            throw new JsonParseException("missing array: fruit_blocks");
        }

        JsonArray fruits = json.getAsJsonArray("fruit_blocks");
        if (fruits.size() == 0) {
            throw new JsonParseException("fruit_blocks must contain at least one block");
        }
        List<ResourceLocation> fruitBlockIds = new ArrayList<>();
        for (JsonElement element : fruits) {
            if (!element.isJsonPrimitive()) {
                throw new JsonParseException("fruit_blocks entries must be resource locations");
            }
            ResourceLocation fruitId = ResourceLocation.tryParse(element.getAsString());
            if (fruitId == null) {
                throw new JsonParseException("invalid fruit block resource location: " + element);
            }
            fruitBlockIds.add(fruitId);
        }

        if (plantingItemId != null && BuiltInRegistries.ITEM.getOptional(plantingItemId)
                .filter(item -> item != Items.AIR)
                .isEmpty()) {
            return null;
        }

        Block stemBlock = BuiltInRegistries.BLOCK.getOptional(stemBlockId)
                .filter(block -> block != Blocks.AIR)
                .orElse(null);
        if (stemBlock == null) {
            return null;
        }
        if (!(stemBlock instanceof StemBlock)) {
            throw new JsonParseException("stem_block " + stemBlockId + " is not a StemBlock");
        }

        if (attachedStemBlockId != null && BuiltInRegistries.BLOCK.getOptional(attachedStemBlockId)
                .filter(block -> block != Blocks.AIR)
                .isEmpty()) {
            return null;
        }

        for (ResourceLocation fruitId : fruitBlockIds) {
            if (BuiltInRegistries.BLOCK.getOptional(fruitId)
                    .filter(block -> block != Blocks.AIR)
                    .isEmpty()) {
                return null;
            }
        }

        TagKey<Item> plantingTag = plantingTagId == null ? null : TagKey.create(Registries.ITEM, plantingTagId);
        return new StemCropDefinition(id, plantingItemId, plantingTag, stemBlockId, attachedStemBlockId, fruitBlockIds);
    }

    public ResourceLocation id() {
        return id;
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

    public boolean matchesStem(BlockState state) {
        return state != null && stemBlockId.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public BlockState initialState() {
        Block block = BuiltInRegistries.BLOCK.get(stemBlockId);
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = block.defaultBlockState();
        IntegerProperty age = findAgeProperty(state);
        if (age == null) {
            return state;
        }
        int min = age.getPossibleValues().stream().min(Integer::compareTo).orElse(0);
        return state.setValue(age, min);
    }

    public ItemStack canonicalPlantingStack() {
        if (plantingItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(plantingItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public Block firstFruit() {
        if (fruitBlockIds.isEmpty()) {
            return Blocks.AIR;
        }
        Block block = BuiltInRegistries.BLOCK.get(fruitBlockIds.get(0));
        return block == null ? Blocks.AIR : block;
    }

    public Block attachedStemBlock() {
        if (attachedStemBlockId == null) {
            return Blocks.AIR;
        }
        Block block = BuiltInRegistries.BLOCK.get(attachedStemBlockId);
        return block == null ? Blocks.AIR : block;
    }

    public Block randomFruit(RandomSource random) {
        if (fruitBlockIds.isEmpty()) {
            return Blocks.AIR;
        }
        ResourceLocation id = fruitBlockIds.get(random.nextInt(fruitBlockIds.size()));
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null ? Blocks.AIR : block;
    }

    private static IntegerProperty findAgeProperty(BlockState state) {
        return state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
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
