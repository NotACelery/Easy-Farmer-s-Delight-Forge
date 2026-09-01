package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.CutterBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CutterItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Minecraft minecraft;
    private final BlockRenderDispatcher blockRenderer;

    public CutterItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        minecraft = Minecraft.getInstance();
        blockRenderer = minecraft.getBlockRenderer();
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        Direction facing = Direction.SOUTH;
        BlockState state = ModBlocks.CUTTER.get().defaultBlockState().setValue(CutterBlock.FACING, facing);

        pose.pushPose();
        blockRenderer.renderSingleBlock(state, pose, buffer, light, overlay);

        Block variant = CutterLogVariant.fromStack(stack);
        pose.pushPose();
        applyWorkTransform(pose, facing);
        blockRenderer.renderSingleBlock(variant.defaultBlockState(), pose, buffer, light, overlay);
        pose.popPose();

        Level level = minecraft.level;
        if (level != null) {
            CutterBlockEntity preview = new CutterBlockEntity(BlockPos.ZERO, state);
            preview.setLevel(level);
            preview.setItemPreview(true);

            CompoundTag data = stack.getTagElement("BlockEntityTag");
            if (data != null && !data.isEmpty()) {
            preview.load(data);
                preview.setItemPreview(true);
            }

            BlockEntityRenderer<CutterBlockEntity> renderer =
                    minecraft.getBlockEntityRenderDispatcher().getRenderer(preview);
            if (renderer != null) {
                renderer.render(preview, 0.0F, pose, buffer, light, overlay);
            }
        }

        pose.popPose();
    }

    private static void applyWorkTransform(PoseStack pose, Direction facing) {
        final float scale = 0.45F;
        pose.translate(0.5D, 1.0D / 16.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, 2.0D / 16.0D);
        pose.translate(-0.5D, 0.0D, -0.5D);
        pose.scale(scale, scale, scale);
        pose.translate(0.5D / scale - 0.5D, 0.0D, 0.5D / scale - 0.5D);
    }
}
