package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.IronFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public final class IronFarmNoiseSwitchBlockEntityRenderer implements BlockEntityRenderer<IronFarmNoiseSwitchBlockEntity> {
    private static final float WORK_SCALE = 0.30F;
    // Assembly occupies the same rear zone used by the Villager in the VNS.
    private static final float ASSEMBLY_SCALE = 0.16F;
    // Match the visual height/weight of the VNS Villager without touching the pedestal.
    private static final float GOLEM_SCALE = 0.34F;
    private static final double FLOOR_Y = 1.0D / 16.0D + 0.002D;
    private static final double INNER_MIN = 1.0D / 16.0D;
    private static final double INNER_SIZE = 14.0D / 16.0D;
    private static final double DUST_CELL = INNER_SIZE / 3.0D;

    private final BlockRenderDispatcher blockRenderer;
    private Entity cachedGolem;
    private Level cachedGolemLevel;

    public IronFarmNoiseSwitchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(IronFarmNoiseSwitchBlockEntity noiseSwitch, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = noiseSwitch.getBlockState().hasProperty(IronFarmNoiseSwitchBlock.FACING)
                ? noiseSwitch.getBlockState().getValue(IronFarmNoiseSwitchBlock.FACING)
                : Direction.SOUTH;
        boolean muted = noiseSwitch.hasGolem() && ClientPreferences.ironFarmSoundsMuted();

        if (noiseSwitch.hasGolem()) {
            renderGolem(noiseSwitch, facing, partialTick, pose, buffer, packedLight);
        } else {
            renderAssembly(noiseSwitch.assemblyStage(), facing, pose, buffer, packedLight, packedOverlay);
        }

        renderRedstoneCarpet(facing, muted, pose, buffer, packedLight, packedOverlay);
        renderSwitch(facing, muted, pose, buffer, packedLight, packedOverlay);
    }

    private void renderAssembly(int stage, Direction facing, PoseStack pose, MultiBufferSource buffer,
                                int packedLight, int packedOverlay) {
        if (stage <= 0) return;

        pose.pushPose();
        pose.translate(0.5D, 1.0D / 16.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, -4.0D / 16.0D);
        pose.scale(ASSEMBLY_SCALE, ASSEMBLY_SCALE, ASSEMBLY_SCALE);

        renderAssemblyBlock(0.0D, 0.0D, pose, buffer, packedLight, packedOverlay);
        if (stage >= 2) renderAssemblyBlock(0.0D, 1.0D, pose, buffer, packedLight, packedOverlay);
        if (stage >= 3) renderAssemblyBlock(-1.0D, 1.0D, pose, buffer, packedLight, packedOverlay);
        if (stage >= 4) renderAssemblyBlock(1.0D, 1.0D, pose, buffer, packedLight, packedOverlay);
        pose.popPose();
    }

    private void renderAssemblyBlock(double x, double y, PoseStack pose, MultiBufferSource buffer,
                                     int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(x - 0.5D, y, -0.5D);
        blockRenderer.renderSingleBlock(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffer, packedLight, packedOverlay);
        pose.popPose();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderGolem(IronFarmNoiseSwitchBlockEntity noiseSwitch, Direction facing, float partialTick,
                             PoseStack pose, MultiBufferSource buffer, int packedLight) {
        Level level = noiseSwitch.getLevel();
        if (level == null) return;

        if (cachedGolem == null || cachedGolemLevel != level) {
            cachedGolem = EntityType.IRON_GOLEM.create(level);
            cachedGolemLevel = level;
        }
        if (cachedGolem == null) return;

        pose.pushPose();
        pose.translate(0.5D, 1.0D / 16.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, -4.0D / 16.0D);
        pose.scale(GOLEM_SCALE, GOLEM_SCALE, GOLEM_SCALE);

        EntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(cachedGolem);
        renderer.render(cachedGolem, 180.0F, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }

    private void renderRedstoneCarpet(Direction facing, boolean muted, PoseStack pose, MultiBufferSource buffer,
                                      int packedLight, int packedOverlay) {
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

    private void renderSwitch(Direction facing, boolean muted, PoseStack pose, MultiBufferSource buffer,
                              int packedLight, int packedOverlay) {
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
        // Keep the compact switch pedestal in the front half, leaving the rear
        // half exclusively for the stored Villager, exactly like the original UX.
        pose.translate(0.0D, 0.0D, 4.0D / 16.0D);
        pose.scale(WORK_SCALE, WORK_SCALE, WORK_SCALE);
        pose.translate(-0.5D, 0.0D, -0.5D);
    }
}
