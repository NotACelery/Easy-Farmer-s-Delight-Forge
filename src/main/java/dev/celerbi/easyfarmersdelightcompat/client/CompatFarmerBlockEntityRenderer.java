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

/**
 * Renders the contents of our farmer blocks using the same spatial rules as
 * Easy Villagers' 1.21.1 FarmerRenderer: villager behind the crop, both rotated
 * by the block FACING value and both kept inside the one-block enclosure.
 */
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
    // The Paddy model's water field spans 2/16..14/16. Keep both submerged
    // islands fully inside that footprint and distribute them symmetrically:
    // villager support centered at 5/16, Sugar Cane substrate at 11/16.
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
        // Paddy villagers stand on the submerged support island at the exact
        // internal waterline (plus a sub-pixel render epsilon to avoid z-fighting). They render at 90% of the normal Farmer size so
        // profession hats remain inside the glass enclosure.
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

    /**
     * Rice has two real Farmer's Delight blocks. Both are rendered inside the same
     * 0.45-scaled crop space, so even the fully mature two-stage plant stays below
     * the glass roof instead of extending into the block above.
     */
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

    /**
     * Tomato can contain base + Rope 1 + Rope 2. Three full 0.45 blocks would be
     * taller than the enclosure, so the complete trellis is rendered as one
     * 0.28-scale stack (3 * 0.28 = 0.84 blocks maximum height).
     */
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
            // Farmer's Delight 1.3+ uses tomatoes_on_rope; older supported builds
            // may expose the dedicated rope section as hanging_tomatoes instead.
            renderBlockState(withAge(tomatoOnRope.defaultBlockState(), progress), poseStack, buffer, combinedLight, combinedOverlay);
        } else if (tomato != Blocks.AIR) {
            // The declared minimum, Farmer's Delight 1.2.9, represents hanging
            // tomatoes with the legacy ropelogged state on the normal tomato block.
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
        // The support is a submerged island: its top face is exactly flush with
        // the Paddy model's water surface (Y = 1.25/16). Most of the visual block
        // therefore sits below the waterline instead of protruding above it. A tiny
        // render-only epsilon keeps its top face from z-fighting the static water plane.
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


    /**
     * Resolve light for the virtual contents of the enclosure, not for the opaque
     * terrain block that may happen to touch one of its faces.
     *
     * Easy Villagers' Farmer is logically hollow. Our crops/villager are BER
     * geometry, however, so vanilla gives them a single packed-light sample from
     * the block entity position. A roof block or a grass block beside the Farmer
     * can make that sample much darker than the light that visibly enters through
     * the remaining glass faces. Sample a small exterior halo instead: immediate
     * neighbours plus the air layer around the top edge. We deliberately do not
     * sample straight through pos.above(2), so a genuinely enclosed/dark machine
     * is still allowed to be dark.
     */
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
        // Sand is the second submerged island and uses the exact same waterline
        // as the villager support. The cane begins on that top face, not one full
        // rendered Sand block above the water.
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

        // Split the available Farmer width into thirds: stem center at 1/3,
        // fruit center at 2/3. When the virtual fruit exists, render the actual
        // vanilla attached-stem state facing EAST (toward the fruit) instead of
        // leaving a mature vertical stem that makes the fruit look spawned in.
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

    /**
     * Renders a vanilla one-block model with an explicit center, bottom Y and
     * independent XYZ scale inside the Farmer enclosure. Local X/Z are evaluated
     * after FACING rotation, so 1/3/2/3 layouts stay consistent in every direction.
     */
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

    /**
     * Uses the same block-model render pipeline as Easy Villagers instead of
     * renderSingleBlock(). This preserves the crop's own RenderType and tint and
     * avoids the black/corrupted geometry seen in the previous build.
     */
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
