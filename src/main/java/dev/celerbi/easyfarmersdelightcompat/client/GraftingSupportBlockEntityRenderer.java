package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.GraftingCanopyBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public final class GraftingSupportBlockEntityRenderer implements BlockEntityRenderer<GraftingSupportBlockEntity> {
    private static final float CANOPY_SCALE = 0.68F;
    private static final float CANOPY_BOTTOM_Y = 0.74F;
    private static final float APPLE_Y_OFFSET = 0.34F;

    private final Minecraft minecraft;
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    public GraftingSupportBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.itemRenderer = minecraft.getItemRenderer();
    }

    @Override
    public void render(
            GraftingSupportBlockEntity support,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        if (!support.getBlockState().is(ModBlocks.GRAFTING_SUPPORT.get())
                || !support.hasCanopy()
                || support.getLevel() == null) {
            return;
        }
        BlockState marker = support.getLevel().getBlockState(support.getBlockPos().above());
        if (!marker.is(ModBlocks.GRAFTING_CANOPY.get())
                || (!marker.getValue(GraftingCanopyBlock.ACTIVE) && !support.hasClientCanopyPreview())) {
            return;
        }

        BlockState canopy = support.renderState();
        if (!canopy.isAir()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, CANOPY_BOTTOM_Y, 0.5D);
            poseStack.scale(CANOPY_SCALE, CANOPY_SCALE, CANOPY_SCALE);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            renderBlockState(support, canopy, poseStack, buffer, combinedLight, combinedOverlay);
            poseStack.popPose();
            renderGraftBranch(support, poseStack, buffer, combinedLight, combinedOverlay);
        }

        if (support.renderStyle() == OrchardCropDefinition.RenderStyle.APPLE
                && support.isProductive()
                && (support.isOnRichSoil() || support.orchardAge() > 0)) {
            renderAppleGrowth(support, poseStack, buffer, combinedLight, combinedOverlay);
        }
    }

    private void renderGraftBranch(
            GraftingSupportBlockEntity support,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.98D, 0.5D);
        poseStack.scale(0.085F, 0.30F, 0.085F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        renderBlockState(
                support,
                net.minecraft.world.level.block.Blocks.STRIPPED_OAK_LOG.defaultBlockState(),
                poseStack,
                buffer,
                light,
                overlay
        );
        poseStack.popPose();
    }

    private void renderAppleGrowth(
            GraftingSupportBlockEntity support,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        int age = Math.max(0, Math.min(3, support.orchardAge()));

        // Match Croptopia's visual language: buds/flowers/fruit sit on the outside
        // faces of the canopy instead of being buried inside the leaf cube.
        if (age == 0) {
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), -0.17F, 0.77F, 0.345F, 0.070F, 0F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), 0.345F, 0.84F, 0.10F, 0.065F, 90F,
                    poseStack, buffer, light, overlay);
            return;
        }

        if (age == 1) {
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), -0.19F, 0.75F, 0.350F, 0.105F, 0F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), 0.18F, 0.84F, -0.350F, 0.100F, 180F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), 0.350F, 0.79F, 0.12F, 0.095F, 90F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.PINK_PETALS), -0.350F, 0.87F, -0.09F, 0.090F, -90F,
                    poseStack, buffer, light, overlay);
            return;
        }

        if (age == 2) {
            renderCanopyFaceItem(support, new ItemStack(Items.APPLE), -0.18F, 0.72F, 0.355F, 0.120F, 0F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.APPLE), 0.16F, 0.82F, -0.355F, 0.115F, 180F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.APPLE), 0.355F, 0.76F, 0.11F, 0.110F, 90F,
                    poseStack, buffer, light, overlay);
            renderCanopyFaceItem(support, new ItemStack(Items.APPLE), -0.355F, 0.85F, -0.11F, 0.110F, -90F,
                    poseStack, buffer, light, overlay);
            return;
        }

        renderCanopyFaceItem(support, new ItemStack(Items.APPLE), -0.19F, 0.70F, 0.360F, 0.180F, 0F,
                poseStack, buffer, light, overlay);
        renderCanopyFaceItem(support, new ItemStack(Items.APPLE), 0.18F, 0.84F, -0.360F, 0.170F, 180F,
                poseStack, buffer, light, overlay);
        renderCanopyFaceItem(support, new ItemStack(Items.APPLE), 0.360F, 0.77F, 0.12F, 0.170F, 90F,
                poseStack, buffer, light, overlay);
        renderCanopyFaceItem(support, new ItemStack(Items.APPLE), -0.360F, 0.86F, -0.12F, 0.165F, -90F,
                poseStack, buffer, light, overlay);
        renderCanopyFaceItem(support, new ItemStack(Items.APPLE), 0.16F, 0.67F, 0.360F, 0.155F, 0F,
                poseStack, buffer, light, overlay);
    }

    private void renderCanopyFaceItem(
            GraftingSupportBlockEntity support,
            ItemStack stack,
            float x,
            float y,
            float z,
            float scale,
            float yawDegrees,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (support.getLevel() == null || stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D + x, y + APPLE_Y_OFFSET, 0.5D + z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                light,
                overlay,
                poseStack,
                buffer,
                support.getLevel(),
                support.getBlockPos().hashCode() + Float.floatToIntBits(x) * 31 + Float.floatToIntBits(z)
        );
        poseStack.popPose();
    }

    private void renderBlockState(
            GraftingSupportBlockEntity support,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        int color = minecraft.getBlockColors().getColor(state, support.getLevel(), support.getBlockPos(), 0);
        float red = ((color >> 16) & 0xFF) / 255F;
        float green = ((color >> 8) & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        BakedModel model = blockRenderer.getBlockModel(state);
        boolean rendered = false;
        long seed = state.getSeed(support.getBlockPos());
        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(seed), ModelData.EMPTY)) {
            renderBlockStateLayer(
                    state, model, poseStack, buffer, combinedLight, combinedOverlay,
                    red, green, blue, renderType
            );
            rendered = true;
        }
        if (!rendered) {
            RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);
            renderBlockStateLayer(
                    state, model, poseStack, buffer, combinedLight, combinedOverlay,
                    red, green, blue, renderType
            );
        }
    }

    private void renderBlockStateLayer(
            BlockState state,
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            float red,
            float green,
            float blue,
            RenderType renderType
    ) {
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(renderType),
                state,
                model,
                red,
                green,
                blue,
                combinedLight,
                combinedOverlay,
                ModelData.EMPTY,
                renderType
        );
    }
}
