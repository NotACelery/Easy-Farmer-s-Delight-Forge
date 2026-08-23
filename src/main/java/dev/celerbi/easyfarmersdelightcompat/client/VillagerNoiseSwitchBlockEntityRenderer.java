package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.VillagerNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

/**
 * Client-personal renderer for the Noise Switch.
 *
 * The physical block has no powered state. Lever and redstone are rendered from
 * ClientPreferences so two players can look at the same block and see different
 * states without causing any world/redstone update.
 */
public final class VillagerNoiseSwitchBlockEntityRenderer implements BlockEntityRenderer<VillagerNoiseSwitchBlockEntity> {
    private static final float WORK_SCALE = 0.45F;
    private static final float VILLAGER_SCALE = 0.45F;
    private static final double FLOOR_Y = 1.0D / 16.0D + 0.002D;
    private static final double INNER_MIN = 1.0D / 16.0D;
    private static final double INNER_SIZE = 14.0D / 16.0D;
    private static final double DUST_CELL = INNER_SIZE / 3.0D;

    private final BlockRenderDispatcher blockRenderer;
    private final VillagerRenderer villagerRenderer;

    public VillagerNoiseSwitchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        blockRenderer = context.getBlockRenderDispatcher();
        EntityRendererProvider.Context entityContext = new EntityRendererProvider.Context(
                minecraft.getEntityRenderDispatcher(),
                minecraft.getItemRenderer(),
                minecraft.getBlockRenderer(),
                minecraft.gameRenderer.itemInHandRenderer,
                minecraft.getResourceManager(),
                minecraft.getEntityModels(),
                minecraft.font
        );
        villagerRenderer = new VillagerRenderer(entityContext);
    }

    @Override
    public void render(
            VillagerNoiseSwitchBlockEntity noiseSwitch,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = noiseSwitch.getBlockState().hasProperty(VillagerNoiseSwitchBlock.FACING)
                ? noiseSwitch.getBlockState().getValue(VillagerNoiseSwitchBlock.FACING)
                : Direction.SOUTH;
        boolean muted = ClientPreferences.villagersMuted();

        renderVillager(noiseSwitch, facing, pose, buffer, packedLight);
        renderRedstoneCarpet(facing, muted, pose, buffer, packedLight, packedOverlay);
        renderSwitch(facing, muted, pose, buffer, packedLight, packedOverlay);
    }

    private void renderVillager(
            VillagerNoiseSwitchBlockEntity noiseSwitch,
            Direction facing,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight
    ) {
        Villager villager = noiseSwitch.villagerAdapter().getVillagerEntity();
        if (villager == null) return;

        pose.pushPose();
        pose.translate(0.5D, 1.0D / 16.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, -4.0D / 16.0D);
        pose.scale(VILLAGER_SCALE, VILLAGER_SCALE, VILLAGER_SCALE);
        villagerRenderer.render(villager, 0.0F, 1.0F, pose, buffer, packedLight);
        pose.popPose();
    }

    private void renderRedstoneCarpet(
            Direction facing,
            boolean muted,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        BlockState dust = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.POWER, muted ? 15 : 0);

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(-0.5D, FLOOR_Y, -0.5D);

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                pose.pushPose();
                pose.translate(INNER_MIN + x * DUST_CELL, 0.0D, INNER_MIN + z * DUST_CELL);
                pose.scale((float) DUST_CELL, 1.0F, (float) DUST_CELL);
                blockRenderer.renderSingleBlock(dust, pose, buffer, packedLight, packedOverlay);
                pose.popPose();
            }
        }
        pose.popPose();
    }

    private void renderSwitch(
            Direction facing,
            boolean muted,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        pose.pushPose();
        applyWorkTransform(pose, facing);
        blockRenderer.renderSingleBlock(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffer, packedLight, packedOverlay);

        BlockState lever = Blocks.LEVER.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR)
                .setValue(LeverBlock.FACING, Direction.NORTH)
                .setValue(LeverBlock.POWERED, muted);
        pose.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(lever, pose, buffer, packedLight, packedOverlay);
        pose.popPose();
    }

    private static void applyWorkTransform(PoseStack pose, Direction facing) {
        pose.translate(0.5D, 1.0D / 16.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, 2.0D / 16.0D);
        pose.translate(-0.5D, 0.0D, -0.5D);
        pose.scale(WORK_SCALE, WORK_SCALE, WORK_SCALE);
        pose.translate(0.5D / WORK_SCALE - 0.5D, 0.0D, 0.5D / WORK_SCALE - 0.5D);
    }
}
