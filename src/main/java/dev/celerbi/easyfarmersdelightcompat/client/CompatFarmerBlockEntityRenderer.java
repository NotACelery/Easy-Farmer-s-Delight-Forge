package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.data.ModelData;

/** Renders virtual villager/crop contents inside the Farmer enclosure. */
public final class CompatFarmerBlockEntityRenderer implements BlockEntityRenderer<CompatFarmerBlockEntity> {
    private static final ResourceLocation RICE_CROP_ID = new ResourceLocation("farmersdelight", "rice");
    private static final ResourceLocation RICE_PANICLES_ID = new ResourceLocation("farmersdelight", "rice_panicles");
    private static final ResourceLocation BUDDING_TOMATO_ID = new ResourceLocation("farmersdelight", "budding_tomatoes");
    private static final ResourceLocation TOMATO_CROP_ID = new ResourceLocation("farmersdelight", "tomatoes");
    private static final ResourceLocation TOMATO_ON_ROPE_ID = new ResourceLocation("farmersdelight", "tomatoes_on_rope");
    private static final ResourceLocation LEGACY_HANGING_TOMATO_ID = new ResourceLocation("farmersdelight", "hanging_tomatoes");
    private static final ResourceLocation RICH_SOIL_ID = new ResourceLocation("farmersdelight", "rich_soil");

    private static final float FARM_SCALE = 0.45F;
    private static final float PADDY_VILLAGER_SCALE = 0.90F;
    private static final float PADDY_WATERLINE_Y = 1.25F / 16.0F;
    private static final float PADDY_SURFACE_EPSILON = 1.0F / 1024.0F;
    private static final float PADDY_ISLAND_TOP_Y = PADDY_WATERLINE_Y + PADDY_SURFACE_EPSILON;
    // Paddy support islands stay inside the model's internal water footprint.
    private static final float PADDY_ISLAND_SCALE = 5.0F / 16.0F;
    private static final float PADDY_SUPPORT_LOCAL_Z = -3.0F / 16.0F;
    private static final float PADDY_SAND_LOCAL_Z = 3.0F / 16.0F;
    private static final float TOMATO_STACK_SCALE = 0.28F;
    private static final float STEM_SCALE = 0.28F;
    private static final float SUGAR_CANE_SCALE = 0.22F;
    private static final float STEM_LEFT_CENTER = -1.0F / 6.0F;
    private static final float FRUIT_RIGHT_CENTER = 1.0F / 6.0F;
    private static final float CROP_LOCAL_Z = 2.0F / 16.0F;
    private static final float VILLAGER_LOCAL_Z = -4.0F / 16.0F;

    private final Minecraft minecraft;
    private final BlockRenderDispatcher blockRenderer;
    private final VillagerRenderer villagerRenderer;

    public CompatFarmerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.blockRenderer = context.getBlockRenderDispatcher();
        EntityRendererProvider.Context entityContext = new EntityRendererProvider.Context(
                minecraft.getEntityRenderDispatcher(),
                minecraft.getItemRenderer(),
                minecraft.getBlockRenderer(),
                minecraft.gameRenderer.itemInHandRenderer,
                minecraft.getResourceManager(),
                minecraft.getEntityModels(),
                minecraft.font
        );
        this.villagerRenderer = new VillagerRenderer(entityContext);
    }

    @Override
    public void render(
            CompatFarmerBlockEntity farmer,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        Level level = farmer.getLevel();
        if (level == null) {
            return;
        }

        Direction direction = farmer.getBlockState().hasProperty(CompatFarmerBlock.FACING)
                ? farmer.getBlockState().getValue(CompatFarmerBlock.FACING)
                : Direction.SOUTH;
        RegistryAccess registries = level.registryAccess();
        int interiorLight = farmer.isItemPreview()
                ? combinedLight
                : resolveInteriorLight(level, farmer.getBlockPos(), combinedLight);

        if (farmer.variant().isAquatic()) {
            renderPaddyPlatform(farmer, direction, poseStack, buffer, interiorLight, combinedOverlay);
        }
        renderVillager(farmer, registries, direction, poseStack, buffer, interiorLight);
        renderCrop(farmer, registries, direction, poseStack, buffer, interiorLight, combinedOverlay);
    }

    /** Mirrors Easy Villagers FarmerRenderer villager transform exactly. */
    private void renderVillager(
            CompatFarmerBlockEntity farmer,
            RegistryAccess registries,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight
    ) {
        Villager villager = farmer.easyVillagers().getVillagerEntity(registries);
        if (villager == null) {
            return;
        }

        poseStack.pushPose();
        // Paddy villagers sit on the submerged support and render slightly smaller to fit the glass.
        double baseY = farmer.variant().isAquatic() ? PADDY_ISLAND_TOP_Y : 1D / 16D;
        poseStack.translate(0.5D, baseY, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.translate(0D, 0D, farmer.variant().isAquatic() ? PADDY_SUPPORT_LOCAL_Z : VILLAGER_LOCAL_Z);
        float villagerScale = FARM_SCALE * (farmer.variant().isAquatic() ? PADDY_VILLAGER_SCALE : 1.0F);
        poseStack.scale(villagerScale, villagerScale, villagerScale);
        villagerRenderer.render(villager, 0F, 1F, poseStack, buffer, combinedLight);
        poseStack.popPose();
    }

    private void renderCrop(
            CompatFarmerBlockEntity farmer,
            RegistryAccess registries,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        if (farmer.variant().isAquatic() && farmer.hasPaddySand()) {
            renderSugarCaneMode(farmer, direction, poseStack, buffer, combinedLight, combinedOverlay);
            return;
        }

        BlockState crop = farmer.easyVillagers().getCrop(registries);
        if (crop == null) {
            return;
        }

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
        if (farmer.variant().isAquatic() && RICE_CROP_ID.equals(cropId)) {
            renderRice(farmer, direction, poseStack, buffer, combinedLight, combinedOverlay, crop);
        } else if (isTomatoState(crop)) {
            renderTomato(farmer, direction, poseStack, buffer, combinedLight, combinedOverlay, crop);
        } else if (isStemState(crop)) {
            renderStem(farmer, direction, poseStack, buffer, combinedLight, combinedOverlay, crop);
        } else {
            poseStack.pushPose();
            applyCropTransform(poseStack, direction, FARM_SCALE);
            renderBlockState(crop, poseStack, buffer, combinedLight, combinedOverlay);
            poseStack.popPose();
        }
    }

    /** Renders both rice halves inside the Farmer's scaled crop space. */
    private void renderRice(
            CompatFarmerBlockEntity farmer,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BlockState lowerState
    ) {
        poseStack.pushPose();
        applyCropTransform(poseStack, direction, FARM_SCALE);
        renderBlockState(lowerState, poseStack, buffer, combinedLight, combinedOverlay);

        if (farmer.paddyGrowth() >= 4) {
            Block paniclesBlock = BuiltInRegistries.BLOCK.get(RICE_PANICLES_ID);
            if (paniclesBlock != Blocks.AIR) {
                poseStack.pushPose();
                poseStack.translate(0D, 1D, 0D);
                BlockState panicles = withAge(paniclesBlock.defaultBlockState(), farmer.paddyGrowth() - 4);
                renderBlockState(panicles, poseStack, buffer, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    /** Scales the full three-section Tomato trellis to fit inside one block. */
    private void renderTomato(
            CompatFarmerBlockEntity farmer,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BlockState baseCrop
    ) {
        poseStack.pushPose();
        applyCropTransform(poseStack, direction, TOMATO_STACK_SCALE);
        renderBlockState(baseCrop, poseStack, buffer, combinedLight, combinedOverlay);

        Block tomatoOnRope = BuiltInRegistries.BLOCK.get(TOMATO_ON_ROPE_ID);
        if (tomatoOnRope == Blocks.AIR) {
            tomatoOnRope = BuiltInRegistries.BLOCK.get(LEGACY_HANGING_TOMATO_ID);
        }
        Block tomato = BuiltInRegistries.BLOCK.get(TOMATO_CROP_ID);

        if (farmer.ropeCount() >= 1) {
            renderTomatoSection(1, farmer.ropeOneProgress(), tomatoOnRope, tomato, poseStack, buffer, combinedLight, combinedOverlay);
        }
        if (farmer.ropeCount() >= 2) {
            renderTomatoSection(2, farmer.ropeTwoProgress(), tomatoOnRope, tomato, poseStack, buffer, combinedLight, combinedOverlay);
        }
        poseStack.popPose();
    }

    private void renderTomatoSection(
            int section,
            int progress,
            Block tomatoOnRope,
            Block tomato,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0D, section, 0D);
        if (tomatoOnRope != Blocks.AIR) {
            // Newer FD uses tomatoes_on_rope; older builds may expose hanging_tomatoes.
            renderBlockState(withAge(tomatoOnRope.defaultBlockState(), progress), poseStack, buffer, combinedLight, combinedOverlay);
        } else if (tomato != Blocks.AIR) {
            // FD 1.2.9 falls back to the legacy ropelogged tomato state.
            BlockState legacy = withBooleanProperty(withAge(tomato.defaultBlockState(), progress), "ropelogged", true);
            renderBlockState(legacy, poseStack, buffer, combinedLight, combinedOverlay);
        }
        poseStack.popPose();
    }

    private void renderPaddyPlatform(
            CompatFarmerBlockEntity farmer,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        Block platform = farmer.variant().isRich() ? BuiltInRegistries.BLOCK.get(RICH_SOIL_ID) : Blocks.DIRT;
        if (platform == Blocks.AIR) platform = Blocks.DIRT;
        poseStack.pushPose();
        // Keep the support top flush with the Paddy water plane, with a tiny z-fight offset.
        applyScaledBlockTransform(
                poseStack,
                direction,
                0.0F,
                PADDY_SUPPORT_LOCAL_Z,
                0.0F,
                PADDY_ISLAND_SCALE,
                PADDY_ISLAND_TOP_Y,
                PADDY_ISLAND_SCALE
        );
        renderBlockState(platform.defaultBlockState(), poseStack, buffer, combinedLight, combinedOverlay);
        poseStack.popPose();
    }


    /** Samples nearby exterior light for the Farmer's virtual interior geometry. */
    private static int resolveInteriorLight(Level level, BlockPos pos, int fallback) {
        int block = LightTexture.block(fallback);
        int sky = LightTexture.sky(fallback);

        for (Direction direction : Direction.values()) {
            int sample = LevelRenderer.getLightColor(level, pos.relative(direction));
            block = Math.max(block, LightTexture.block(sample));
            sky = Math.max(sky, LightTexture.sky(sample));
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos upperEdge = pos.relative(direction).above();
            int sample = LevelRenderer.getLightColor(level, upperEdge);
            block = Math.max(block, LightTexture.block(sample));
            sky = Math.max(sky, LightTexture.sky(sample));
        }

        return LightTexture.pack(block, sky);
    }

    private void renderSugarCaneMode(
            CompatFarmerBlockEntity farmer,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        // Sugar Cane starts on the submerged sand island at the same waterline.
        poseStack.pushPose();
        applyScaledBlockTransform(
                poseStack,
                direction,
                0.0F,
                PADDY_SAND_LOCAL_Z,
                0.0F,
                PADDY_ISLAND_SCALE,
                PADDY_ISLAND_TOP_Y,
                PADDY_ISLAND_SCALE
        );
        renderBlockState(Blocks.SAND.defaultBlockState(), poseStack, buffer, combinedLight, combinedOverlay);
        poseStack.popPose();

        for (int i = 0; i < farmer.sugarCaneHeight(); i++) {
            poseStack.pushPose();
            applyScaledBlockTransform(
                    poseStack,
                    direction,
                    0.0F,
                    PADDY_SAND_LOCAL_Z,
                    PADDY_ISLAND_TOP_Y + (i * SUGAR_CANE_SCALE),
                    SUGAR_CANE_SCALE,
                    SUGAR_CANE_SCALE,
                    SUGAR_CANE_SCALE
            );
            renderBlockState(Blocks.SUGAR_CANE.defaultBlockState(), poseStack, buffer, combinedLight, combinedOverlay);
            poseStack.popPose();
        }
    }

    private void renderStem(
            CompatFarmerBlockEntity farmer,
            Direction direction,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BlockState stem
    ) {
        Block fruit = stem.is(Blocks.MELON_STEM) ? Blocks.MELON
                : stem.is(Blocks.PUMPKIN_STEM) ? Blocks.PUMPKIN : Blocks.AIR;

        // Stem and fruit use opposite thirds; ready stems render attached toward the fruit.
        BlockState renderedStem = stem;
        if (farmer.fruitReady() && fruit != Blocks.AIR) {
            Block attached = stem.is(Blocks.MELON_STEM) ? Blocks.ATTACHED_MELON_STEM : Blocks.ATTACHED_PUMPKIN_STEM;
            renderedStem = attached.defaultBlockState().setValue(AttachedStemBlock.FACING, Direction.EAST);
        }

        poseStack.pushPose();
        applyScaledBlockTransform(
                poseStack,
                direction,
                STEM_LEFT_CENTER,
                CROP_LOCAL_Z,
                1.0F / 16.0F,
                STEM_SCALE,
                STEM_SCALE,
                STEM_SCALE
        );
        renderBlockState(renderedStem, poseStack, buffer, combinedLight, combinedOverlay);
        poseStack.popPose();

        if (farmer.fruitReady() && fruit != Blocks.AIR) {
            poseStack.pushPose();
            applyScaledBlockTransform(
                    poseStack,
                    direction,
                    FRUIT_RIGHT_CENTER,
                    CROP_LOCAL_Z,
                    1.0F / 16.0F,
                    STEM_SCALE,
                    STEM_SCALE,
                    STEM_SCALE
            );
            renderBlockState(fruit.defaultBlockState(), poseStack, buffer, combinedLight, combinedOverlay);
            poseStack.popPose();
        }
    }

    /** Mirrors Easy Villagers FarmerRenderer crop positioning. */
    private static void applyCropTransform(PoseStack poseStack, Direction direction, float scale) {
        applyScaledBlockTransform(
                poseStack,
                direction,
                0.0F,
                CROP_LOCAL_Z,
                1.0F / 16.0F,
                scale,
                scale,
                scale
        );
    }

    /** Renders a block model at an explicit local center/scale after Farmer rotation. */
    private static void applyScaledBlockTransform(
            PoseStack poseStack,
            Direction direction,
            float localX,
            float localZ,
            float bottomY,
            float scaleX,
            float scaleY,
            float scaleZ
    ) {
        poseStack.translate(0.5D, bottomY, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.translate(localX, 0.0D, localZ);
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    /** Uses the block-model pipeline so crop render types and tints remain intact. */
    private void renderBlockState(
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        int color = minecraft.getBlockColors().getColor(state, null, null, 0);
        float red = ((color >> 16) & 0xFF) / 255F;
        float green = ((color >> 8) & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(renderType),
                state,
                blockRenderer.getBlockModel(state),
                red,
                green,
                blue,
                combinedLight,
                combinedOverlay,
                ModelData.EMPTY,
                renderType
        );
    }

    private static boolean isStemState(BlockState state) {
        return state.is(Blocks.MELON_STEM) || state.is(Blocks.PUMPKIN_STEM);
    }

    private static boolean isTomatoState(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return BUDDING_TOMATO_ID.equals(id) || TOMATO_CROP_ID.equals(id);
    }


    private static BlockState withBooleanProperty(BlockState state, String propertyName, boolean value) {
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals(propertyName))
                .findFirst();
        if (property.isPresent() && property.get() instanceof BooleanProperty booleanProperty) {
            return state.setValue(booleanProperty, value);
        }
        return state;
    }

    private static BlockState withAge(BlockState state, int age) {
        Optional<Property<?>> ageProperty = state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .findFirst();
        if (ageProperty.isEmpty() || !(ageProperty.get() instanceof IntegerProperty integerProperty)) {
            return state;
        }

        int max = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
        int safeAge = Math.max(0, Math.min(max, age));
        return state.setValue(integerProperty, safeAge);
    }
}
