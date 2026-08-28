package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class CompatFarmerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Minecraft minecraft;
    private final BlockRenderDispatcher blockRenderer;

    public CompatFarmerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.minecraft = Minecraft.getInstance();
        this.blockRenderer = minecraft.getBlockRenderer();
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                 || !(blockItem.getBlock() instanceof CompatFarmerBlock farmerBlock)) {
            return;
        }

        BlockState state = farmerBlock.defaultBlockState().setValue(CompatFarmerBlock.FACING, Direction.SOUTH);
        poseStack.pushPose();
        blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay);

        Level level = minecraft.level;
        if (level != null) {
            CompatFarmerBlockEntity preview = new CompatFarmerBlockEntity(BlockPos.ZERO, state);
            preview.setLevel(level);
            preview.setItemPreview(true);

            CompoundTag data = stack.getTagElement("BlockEntityTag");
            if (data != null && !data.isEmpty()) {preview.load(data);
                preview.setItemPreview(true);
            }

            BlockEntityRenderer<CompatFarmerBlockEntity> renderer =
                    minecraft.getBlockEntityRenderDispatcher().getRenderer(preview);
            if (renderer != null) {
                renderer.render(preview, 0.0F, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }
}
