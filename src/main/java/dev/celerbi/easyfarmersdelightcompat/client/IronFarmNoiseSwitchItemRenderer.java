package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.celerbi.easyfarmersdelightcompat.block.IronFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;

public final class IronFarmNoiseSwitchItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Minecraft minecraft;
    private final BlockRenderDispatcher blockRenderer;

    public IronFarmNoiseSwitchItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        minecraft = Minecraft.getInstance();
        blockRenderer = minecraft.getBlockRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffer, int light, int overlay) {
        BlockState state = ModBlocks.IRON_FARM_NOISE_SWITCH.get().defaultBlockState()
                .setValue(IronFarmNoiseSwitchBlock.FACING, Direction.SOUTH);

        pose.pushPose();
        blockRenderer.renderSingleBlock(state, pose, buffer, light, overlay);

        IronFarmNoiseSwitchBlockEntity preview = new IronFarmNoiseSwitchBlockEntity(BlockPos.ZERO, state);
        preview.setItemPreview(true);
        Level level = minecraft.level;
        if (level != null) {
            preview.setLevel(level);
            CompoundTag data = stack.getTagElement("BlockEntityTag");
            if (data != null && !data.isEmpty()) {
            preview.load(data);
            }
        }

        BlockEntityRenderer<IronFarmNoiseSwitchBlockEntity> renderer =
                minecraft.getBlockEntityRenderDispatcher().getRenderer(preview);
        if (renderer != null) {
            renderer.render(preview, 0.0F, pose, buffer, light, overlay);
        }
        pose.popPose();
    }
}
