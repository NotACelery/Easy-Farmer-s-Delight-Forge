package dev.celerbi.easyfarmersdelightcompat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.FarmerVariant;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.stem.StemCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.tall.TallCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.tall.TallCropDefinitions;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmCommand {
    private static final int MAX_FARM_VOLUME = 32768;
    private static final int ATTACHED_FACES_PER_HOST = 4;

    private static final ResourceLocation VILLAGER_ITEM_ID = id("easy_villagers", "villager");
    private static final ResourceLocation RICE_ITEM_ID = id("farmersdelight", "rice");
    private static final ResourceLocation TOMATO_SEEDS_ID = id("farmersdelight", "tomato_seeds");
    private static final ResourceLocation RED_MUSHROOM_ID = id("minecraft", "red_mushroom");
    private static final ResourceLocation BROWN_MUSHROOM_ID = id("minecraft", "brown_mushroom");
    private static final ResourceLocation MELON_SEEDS_ID = id("minecraft", "melon_seeds");
    private static final ResourceLocation PUMPKIN_SEEDS_ID = id("minecraft", "pumpkin_seeds");
    private static final ResourceLocation SUGAR_CANE_ID = id("minecraft", "sugar_cane");

    private static final Map<ResourceLocation, ResourceLocation> CROP_ALIASES = Map.ofEntries(
            Map.entry(id("minecraft", "wheat"), id("minecraft", "wheat_seeds")),
            Map.entry(id("minecraft", "carrots"), id("minecraft", "carrot")),
            Map.entry(id("minecraft", "potatoes"), id("minecraft", "potato")),
            Map.entry(id("minecraft", "beetroots"), id("minecraft", "beetroot_seeds")),
            Map.entry(id("minecraft", "melon_stem"), MELON_SEEDS_ID),
            Map.entry(id("minecraft", "pumpkin_stem"), PUMPKIN_SEEDS_ID),
            Map.entry(id("farmersdelight", "budding_tomatoes"), TOMATO_SEEDS_ID),
            Map.entry(id("farmersdelight", "tomatoes"), TOMATO_SEEDS_ID),
            Map.entry(id("farmersdelight", "rice_crop"), RICE_ITEM_ID)
    );

    private FarmCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, Coordinates> fromArgument = Commands.argument(
                "from",
                BlockPosArgument.blockPos()
        );
        RequiredArgumentBuilder<CommandSourceStack, Coordinates> toArgument = Commands.argument(
                "to",
                BlockPosArgument.blockPos()
        );
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> farmArgument = Commands.argument(
                "farm",
                ResourceLocationArgument.id()
        ).suggests(FarmCommand::suggestFarmers);
        RequiredArgumentBuilder<CommandSourceStack, Boolean> villagerArgument = Commands.argument(
                "villager",
                BoolArgumentType.bool()
        ).suggests(FarmCommand::suggestVillagerFlag);
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> cropArgument = Commands.argument(
                "crop",
                ResourceLocationArgument.id()
        ).suggests(FarmCommand::suggestCrops);
        RequiredArgumentBuilder<CommandSourceStack, String> extraArgument = Commands.argument(
                "extra",
                StringArgumentType.greedyString()
        ).suggests(FarmCommand::suggestUpgrades)
                .executes(context -> execute(context, StringArgumentType.getString(context, "extra")));

        cropArgument.executes(context -> execute(context, null));
        cropArgument.then(extraArgument);
        villagerArgument.then(cropArgument);
        farmArgument.then(villagerArgument);
        toArgument.then(farmArgument);
        fromArgument.then(toArgument);

        dispatcher.register(Commands.literal("farm")
                .requires(source -> source.hasPermission(2))
                .then(fromArgument));
        verifyRegisteredBranch(dispatcher);
    }

    private static void verifyRegisteredBranch(CommandDispatcher<CommandSourceStack> dispatcher) {
        var farmRoot = dispatcher.getRoot().getChild("farm");
        var fromNode = farmRoot == null ? null : farmRoot.getChild("from");
        var toNode = fromNode == null ? null : fromNode.getChild("to");
        var farmNode = toNode == null ? null : toNode.getChild("farm");
        var villagerNode = farmNode == null ? null : farmNode.getChild("villager");
        var cropNode = villagerNode == null ? null : villagerNode.getChild("crop");

        if (cropNode == null) {
            EasyFarmersDelightCompat.LOGGER.error("Failed to register the /farm command branch.");
        }
    }

    private static int execute(CommandContext<CommandSourceStack> context, String upgradeToken) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        CompatFarmerBlock farmerBlock = resolveFarmerBlock(context);
        if (farmerBlock == null) {
            source.sendFailure(Component.translatable("command.easyfarmersdelightcompat.farm.only_farmer"));
            return 0;
        }
        BlockState requestedState = farmerBlock.defaultBlockState();

        boolean withVillager = BoolArgumentType.getBool(context, "villager");
        ResourceLocation cropId = ResourceLocationArgument.getId(context, "crop");
        PlanResult result = createPlan(level, farmerBlock, withVillager, cropId, upgradeToken);
        if (result.plan() == null) {
            source.sendFailure(result.error());
            return 0;
        }

        BlockPos from = context.getArgument("from", Coordinates.class).getBlockPos(source);
        BlockPos to = context.getArgument("to", Coordinates.class).getBlockPos(source);
        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());

        long volume = (long) (maxX - minX + 1)
                * (long) (maxY - minY + 1)
                * (long) (maxZ - minZ + 1);
        if (volume > MAX_FARM_VOLUME) {
            source.sendFailure(Component.translatable(
                    "command.easyfarmersdelightcompat.farm.too_large",
                    volume,
                    MAX_FARM_VOLUME
            ));
            return 0;
        }
        if (minY < level.getMinBuildHeight() || maxY >= level.getMaxBuildHeight()) {
            source.sendFailure(Component.translatable("command.easyfarmersdelightcompat.farm.out_of_world"));
            return 0;
        }

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (!level.hasChunkAt(pos)) {
                source.sendFailure(Component.translatable(
                        "command.easyfarmersdelightcompat.farm.unloaded",
                        pos.toShortString()
                ));
                return 0;
            }
        }

        int placed = 0;
        FarmPlan plan = result.plan();
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockPos target = pos.immutable();
            level.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
            if (!level.setBlock(target, requestedState, 3)) {
                continue;
            }
            if (!(level.getBlockEntity(target) instanceof CompatFarmerBlockEntity farmer)) {
                continue;
            }
            if (!applyPlan(level, farmer, plan)) {
                source.sendFailure(Component.translatable(
                        "command.easyfarmersdelightcompat.farm.apply_failed",
                        target.toShortString()
                ));
                return placed;
            }
            placed++;
        }

        int placedCount = placed;
        source.sendSuccess(() -> Component.translatable(
                "command.easyfarmersdelightcompat.farm.success",
                placedCount,
                plan.withVillager(),
                plan.cropLabel(),
                plan.upgradeLabel()
        ), true);
        return placedCount;
    }

    private static PlanResult createPlan(
            ServerLevel level,
            CompatFarmerBlock farmerBlock,
            boolean withVillager,
            ResourceLocation requestedCropId,
            String upgradeToken
    ) {
        boolean noCrop = isNoneCrop(requestedCropId);
        ItemStack crop = resolveCropStack(requestedCropId);
        if (!noCrop && crop.isEmpty()) {
            return PlanResult.error(Component.translatable(
                    "command.easyfarmersdelightcompat.farm.invalid_crop",
                    requestedCropId.toString()
            ));
        }

        FarmerVariant variant = farmerBlock.variant();
        CropMode mode = CropMode.NONE;
        AttachedCropDefinition attachedDefinition = null;
        int ropeCount = 0;
        int logCount = 0;
        String cropLabel = crop.isEmpty() ? "none" : BuiltInRegistries.ITEM.getKey(crop.getItem()).toString();

        if (!crop.isEmpty()) {
            ResourceLocation cropItemId = BuiltInRegistries.ITEM.getKey(crop.getItem());
            if (variant.isAquatic()) {
                if (RICE_ITEM_ID.equals(cropItemId)) {
                    mode = CropMode.RICE;
                } else if (SUGAR_CANE_ID.equals(cropItemId)) {
                    mode = CropMode.SUGAR_CANE;
                } else {
                    return unsupportedCrop(cropLabel, farmerBlock);
                }
            } else if (variant.isRich()) {
                attachedDefinition = AttachedCropDefinitions.all().stream()
                        .filter(definition -> definition.matchesPlanting(crop))
                        .findFirst()
                        .orElse(null);
                TallCropDefinition tallDefinition = TallCropDefinitions.findPlanting(crop).orElse(null);
                RegrowingCropDefinition regrowingDefinition = RegrowingCropDefinitions.findPlanting(crop).orElse(null);
                boolean customStem = StemCropDefinitions.findPlanting(crop).isPresent();

                if (attachedDefinition != null) {
                    mode = CropMode.ATTACHED;
                    Block host = attachedDefinition.canonicalHostBlock();
                    if (host == Blocks.AIR || host.asItem() == Items.AIR
                            || !attachedDefinition.matchesHost(host.defaultBlockState())) {
                        return PlanResult.error(Component.translatable(
                                "command.easyfarmersdelightcompat.farm.no_default_host",
                                attachedDefinition.id().toString()
                        ));
                    }
                } else if (tallDefinition != null) {
                    mode = CropMode.TALL;
                } else if (regrowingDefinition != null) {
                    mode = CropMode.REGROWING;
                } else if (TOMATO_SEEDS_ID.equals(cropItemId)) {
                    mode = CropMode.TOMATO;
                } else if (RED_MUSHROOM_ID.equals(cropItemId) || BROWN_MUSHROOM_ID.equals(cropItemId)) {
                    mode = CropMode.MUSHROOM;
                } else if (MELON_SEEDS_ID.equals(cropItemId) || PUMPKIN_SEEDS_ID.equals(cropItemId) || customStem) {
                    mode = CropMode.STEM;
                } else {
                    CompatFarmerBlockEntity probe = new CompatFarmerBlockEntity(
                            BlockPos.ZERO,
                            farmerBlock.defaultBlockState()
                    );
                    probe.setLevel(level);
                    if (probe.easyVillagers().isValidSeed(crop, level.registryAccess())) {
                        mode = CropMode.NORMAL;
                    } else {
                        return unsupportedCrop(cropLabel, farmerBlock);
                    }
                }
            } else {
                return unsupportedCrop(cropLabel, farmerBlock);
            }
        }

        String normalizedUpgrade = normalizeUpgrade(upgradeToken);
        switch (mode) {
            case ATTACHED -> {
                if (normalizedUpgrade.isEmpty() || "auto".equals(normalizedUpgrade)
                        || "logs".equals(normalizedUpgrade) || "logs=1".equals(normalizedUpgrade)) {
                    logCount = 1;
                } else if ("logs=2".equals(normalizedUpgrade)) {
                    logCount = 2;
                } else {
                    return invalidUpgrade(upgradeToken, "logs=1 | logs=2");
                }
            }
            case TOMATO -> {
                if (normalizedUpgrade.isEmpty() || "auto".equals(normalizedUpgrade)
                        || "none".equals(normalizedUpgrade) || "rope=0".equals(normalizedUpgrade)) {
                    ropeCount = 0;
                } else if ("rope=1".equals(normalizedUpgrade)) {
                    ropeCount = 1;
                } else if ("rope".equals(normalizedUpgrade) || "rope=2".equals(normalizedUpgrade)) {
                    ropeCount = 2;
                } else {
                    return invalidUpgrade(upgradeToken, "rope=0 | rope=1 | rope=2");
                }
            }
            case SUGAR_CANE -> {
                if (!normalizedUpgrade.isEmpty() && !"auto".equals(normalizedUpgrade)
                        && !"sand".equals(normalizedUpgrade)) {
                    return invalidUpgrade(upgradeToken, "sand");
                }
            }
            default -> {
                if (!normalizedUpgrade.isEmpty() && !"auto".equals(normalizedUpgrade)
                        && !"none".equals(normalizedUpgrade)) {
                    return invalidUpgrade(upgradeToken, "none");
                }
            }
        }

        String upgradeLabel = switch (mode) {
            case ATTACHED -> "logs=" + logCount;
            case TOMATO -> "rope=" + ropeCount;
            case SUGAR_CANE -> "sand";
            default -> "none";
        };
        return PlanResult.success(new FarmPlan(
                withVillager,
                crop.copyWithCount(1),
                mode,
                attachedDefinition,
                ropeCount,
                logCount,
                cropLabel,
                upgradeLabel
        ));
    }

    private static PlanResult unsupportedCrop(String crop, CompatFarmerBlock farmerBlock) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(farmerBlock);
        return PlanResult.error(Component.translatable(
                "command.easyfarmersdelightcompat.farm.unsupported_crop",
                crop,
                blockId.toString()
        ));
    }

    private static PlanResult invalidUpgrade(String upgrade, String expected) {
        return PlanResult.error(Component.translatable(
                "command.easyfarmersdelightcompat.farm.invalid_upgrade",
                upgrade == null ? "" : upgrade,
                expected
        ));
    }

    private static boolean applyPlan(ServerLevel level, CompatFarmerBlockEntity farmer, FarmPlan plan) {
        if (plan.withVillager()) {
            Item villagerItem = BuiltInRegistries.ITEM.get(VILLAGER_ITEM_ID);
            if (villagerItem == null || villagerItem == Items.AIR) {
                return false;
            }
            farmer.easyVillagers().insertVillager(new ItemStack(villagerItem), level.registryAccess());
            if (!farmer.easyVillagers().hasVillager(level.registryAccess())) {
                return false;
            }
        }

        ItemStack crop = plan.crop().copyWithCount(1);
        boolean configured = switch (plan.mode()) {
            case NONE -> true;
            case RICE -> {
                farmer.selectRice(level.registryAccess());
                yield true;
            }
            case SUGAR_CANE -> farmer.installPaddySand() && farmer.plantSugarCane();
            case TOMATO -> {
                farmer.selectTomato(level.registryAccess());
                boolean ropeApplied = true;
                for (int rope = 0; rope < plan.ropeCount(); rope++) {
                    ropeApplied &= farmer.addRope();
                }
                yield ropeApplied;
            }
            case MUSHROOM -> farmer.selectMushroom(crop, level.registryAccess());
            case STEM -> farmer.selectStem(crop, level.registryAccess());
            case TALL -> farmer.selectTallCrop(crop, level.registryAccess());
            case REGROWING -> farmer.selectRegrowingCrop(crop, level.registryAccess());
            case NORMAL -> {
                boolean selected = farmer.easyVillagers().setCropFromSeed(crop, level.registryAccess());
                if (selected) {
                    farmer.onNormalCropSelected();
                }
                yield selected;
            }
            case ATTACHED -> configureAttached(farmer, plan.attachedDefinition(), crop, plan.logCount());
        };

        if (configured) {
            farmer.setChanged();
        }
        return configured;
    }

    private static boolean configureAttached(
            CompatFarmerBlockEntity farmer,
            AttachedCropDefinition definition,
            ItemStack crop,
            int logCount
    ) {
        if (definition == null || logCount < 1 || logCount > 2) {
            return false;
        }
        Block host = definition.canonicalHostBlock();
        if (host == Blocks.AIR || host.asItem() == Items.AIR) {
            return false;
        }

        ItemStack hostStack = new ItemStack(host.asItem());
        for (int levelIndex = 0; levelIndex < logCount; levelIndex++) {
            if (!farmer.installAttachedHost(hostStack)) {
                return false;
            }
        }
        for (int face = 0; face < logCount * ATTACHED_FACES_PER_HOST; face++) {
            if (!farmer.plantAttachedCrop(crop)) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack resolveCropStack(ResourceLocation requested) {
        if (requested == null || isNoneCrop(requested)) {
            return ItemStack.EMPTY;
        }

        ResourceLocation itemId = CROP_ALIASES.getOrDefault(requested, requested);
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.AIR);
        if (item != Items.AIR) {
            return new ItemStack(item);
        }

        for (AttachedCropDefinition definition : AttachedCropDefinitions.all()) {
            if (definition.cropBlockId().equals(requested)) {
                ItemStack planting = canonicalPlanting(definition);
                if (!planting.isEmpty()) {
                    return planting;
                }
            }
        }
        for (TallCropDefinition definition : TallCropDefinitions.all()) {
            if (definition.cropBlockId().equals(requested)) {
                ItemStack planting = definition.canonicalPlantingStack();
                if (!planting.isEmpty()) {
                    return planting;
                }
            }
        }
        for (RegrowingCropDefinition definition : RegrowingCropDefinitions.all()) {
            if (definition.cropBlockId().equals(requested)) {
                ItemStack planting = definition.canonicalPlantingStack();
                if (!planting.isEmpty()) {
                    return planting;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack canonicalPlanting(AttachedCropDefinition definition) {
        ItemStack canonical = definition.canonicalPlantingStack();
        if (!canonical.isEmpty()) {
            return canonical;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            ItemStack candidate = new ItemStack(item);
            if (definition.matchesPlanting(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private static CompatFarmerBlock resolveFarmerBlock(CommandContext<CommandSourceStack> context) {
        ResourceLocation requested;
        try {
            requested = ResourceLocationArgument.getId(context, "farm");
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        Block block = BuiltInRegistries.BLOCK.getOptional(requested).orElse(Blocks.AIR);
        if (block instanceof CompatFarmerBlock farmer) {
            return farmer;
        }

        if ("minecraft".equals(requested.getNamespace())) {
            ResourceLocation compatId = id("easyfarmersdelightcompat", requested.getPath());
            block = BuiltInRegistries.BLOCK.getOptional(compatId).orElse(Blocks.AIR);
            if (block instanceof CompatFarmerBlock farmer) {
                return farmer;
            }
        }
        return null;
    }

    private static CompletableFuture<Suggestions> suggestFarmers(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        Set<String> values = new TreeSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof CompatFarmerBlock) {
                values.add(BuiltInRegistries.BLOCK.getKey(block).getPath());
            }
        }
        suggestMatching(builder, values);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestVillagerFlag(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        if (resolveFarmerBlock(context) != null) {
            builder.suggest("true");
            builder.suggest("false");
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCrops(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        Set<String> values = new TreeSet<>();
        values.add("none");
        values.add(RICE_ITEM_ID.toString());
        values.add(TOMATO_SEEDS_ID.toString());
        values.add(RED_MUSHROOM_ID.toString());
        values.add(BROWN_MUSHROOM_ID.toString());
        values.add(MELON_SEEDS_ID.toString());
        values.add(PUMPKIN_SEEDS_ID.toString());
        values.add(SUGAR_CANE_ID.toString());

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath();
            if (path.contains("seed") || "carrot".equals(path) || "potato".equals(path)) {
                values.add(id.toString());
            }
        }
        for (AttachedCropDefinition definition : AttachedCropDefinitions.all()) {
            ItemStack planting = canonicalPlanting(definition);
            if (!planting.isEmpty()) {
                values.add(BuiltInRegistries.ITEM.getKey(planting.getItem()).toString());
            }
        }
        for (TallCropDefinition definition : TallCropDefinitions.all()) {
            ItemStack planting = definition.canonicalPlantingStack();
            if (!planting.isEmpty()) {
                values.add(BuiltInRegistries.ITEM.getKey(planting.getItem()).toString());
            }
        }
        for (RegrowingCropDefinition definition : RegrowingCropDefinitions.all()) {
            ItemStack planting = definition.canonicalPlantingStack();
            if (!planting.isEmpty()) {
                values.add(BuiltInRegistries.ITEM.getKey(planting.getItem()).toString());
            }
        }

        suggestMatching(builder, values);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestUpgrades(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        Set<String> values = new TreeSet<>();
        values.add("none");
        try {
            CompatFarmerBlock farmerBlock = resolveFarmerBlock(context);
            ResourceLocation cropIdArgument = ResourceLocationArgument.getId(context, "crop");
            if (farmerBlock != null) {
                ItemStack crop = resolveCropStack(cropIdArgument);
                ResourceLocation cropId = crop.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(crop.getItem());
                if (!crop.isEmpty() && AttachedCropDefinitions.isPlantingItem(crop)
                        && farmerBlock.variant().isRich() && !farmerBlock.variant().isAquatic()) {
                    values.clear();
                    values.add("logs=1");
                    values.add("logs=2");
                } else if (TOMATO_SEEDS_ID.equals(cropId) && farmerBlock.variant() == FarmerVariant.RICH) {
                    values.clear();
                    values.add("rope=0");
                    values.add("rope=1");
                    values.add("rope=2");
                } else if (SUGAR_CANE_ID.equals(cropId) && farmerBlock.variant().isAquatic()) {
                    values.clear();
                    values.add("sand");
                }
            }
        } catch (IllegalArgumentException ignored) {
            values.add("sand");
            values.add("rope=0");
            values.add("rope=1");
            values.add("rope=2");
            values.add("logs=1");
            values.add("logs=2");
        }
        suggestMatching(builder, values);
        return builder.buildFuture();
    }

    private static void suggestMatching(SuggestionsBuilder builder, Iterable<String> values) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(value);
            }
        }
    }

    private static boolean isNoneCrop(ResourceLocation cropId) {
        return cropId != null
                && "minecraft".equals(cropId.getNamespace())
                && "none".equals(cropId.getPath());
    }

    private static String normalizeUpgrade(String upgrade) {
        return upgrade == null ? "" : upgrade.trim().toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    private enum CropMode {
        NONE,
        RICE,
        SUGAR_CANE,
        TOMATO,
        MUSHROOM,
        STEM,
        TALL,
        REGROWING,
        NORMAL,
        ATTACHED
    }

    private record FarmPlan(
            boolean withVillager,
            ItemStack crop,
            CropMode mode,
            AttachedCropDefinition attachedDefinition,
            int ropeCount,
            int logCount,
            String cropLabel,
            String upgradeLabel
    ) {
    }

    private record PlanResult(FarmPlan plan, Component error) {
        private static PlanResult success(FarmPlan plan) {
            return new PlanResult(plan, null);
        }

        private static PlanResult error(Component error) {
            return new PlanResult(null, error);
        }
    }
}
