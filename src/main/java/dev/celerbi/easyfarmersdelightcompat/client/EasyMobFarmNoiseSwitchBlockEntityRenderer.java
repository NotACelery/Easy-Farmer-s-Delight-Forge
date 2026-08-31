package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.EasyMobFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.EasyMobFarmNoiseSwitchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public final class EasyMobFarmNoiseSwitchBlockEntityRenderer
        implements BlockEntityRenderer<EasyMobFarmNoiseSwitchBlockEntity> {
    private static final ResourceLocation ZOMBIE_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");

    private static final float ZOMBIE_SCALE = 0.42F;
    private static final float WORK_SCALE = 0.30F;
    private static final double FLOOR_BASE_Y = 0.002D;
    private static final double FLOOR_SURFACE_Y = 1.0D / 16.0D + 0.003D;
    private static final double INNER_MIN = 1.0D / 16.0D;
    private static final double INNER_SIZE = 14.0D / 16.0D;
    private static final double DUST_CELL = INNER_SIZE / 3.0D;

    private final BlockRenderDispatcher blockRenderer;
    private final ZombieModel<Zombie> zombieModel;

    public EasyMobFarmNoiseSwitchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
        zombieModel = new ZombieModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ZOMBIE));
    }

    @Override
    public void render(EasyMobFarmNoiseSwitchBlockEntity noiseSwitch, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = noiseSwitch.getBlockState().hasProperty(EasyMobFarmNoiseSwitchBlock.FACING)
                ? noiseSwitch.getBlockState().getValue(EasyMobFarmNoiseSwitchBlock.FACING)
                : Direction.SOUTH;
        boolean muted = noiseSwitch.isComplete() && ClientPreferences.easyMobFarmSoundsMuted();
        Level level = noiseSwitch.getLevel();
        int interiorLight = noiseSwitch.isItemPreview() || level == null
                ? packedLight
                : resolveInteriorLight(level, noiseSwitch.getBlockPos(), packedLight);

        renderMossyFloor(facing, pose, buffer, interiorLight, packedOverlay);
        renderRedstoneCarpet(facing, muted, pose, buffer, interiorLight, packedOverlay);
        renderZombie(noiseSwitch.assemblyStage(), facing, pose, buffer, interiorLight);
        renderSwitch(facing, muted, pose, buffer, interiorLight, packedOverlay);
    }

    private void renderMossyFloor(Direction facing, PoseStack pose, MultiBufferSource buffer,
                                  int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(-0.5D + INNER_MIN, FLOOR_BASE_Y, -0.5D + INNER_MIN);
        pose.scale((float) INNER_SIZE, 1.0F / 16.0F, (float) INNER_SIZE);
        blockRenderer.renderSingleBlock(Blocks.MOSSY_COBBLESTONE.defaultBlockState(), pose, buffer,
                packedLight, packedOverlay);
        pose.popPose();
    }

    private void renderZombie(int stage, Direction facing, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        if (stage <= 0) {
            return;
        }

        zombieModel.head.visible = stage >= 6;
        zombieModel.hat.visible = false;
        zombieModel.body.visible = stage >= 3;
        zombieModel.rightArm.visible = stage >= 4;
        zombieModel.leftArm.visible = stage >= 5;
        zombieModel.rightLeg.visible = stage >= 1;
        zombieModel.leftLeg.visible = stage >= 2;

        pose.pushPose();
        pose.translate(0.5D, FLOOR_SURFACE_Y, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, -4.0D / 16.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(-ZOMBIE_SCALE, -ZOMBIE_SCALE, ZOMBIE_SCALE);
        pose.translate(0.0D, -1.501D, 0.0D);

        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(ZOMBIE_TEXTURE));
        zombieModel.renderToBuffer(pose, vertices, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
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
        pose.translate(-0.5D, FLOOR_SURFACE_Y, -0.5D);
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
        blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), pose, buffer, packedLight,
                packedOverlay);

        BlockState lever = Blocks.LEVER.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR)
                .setValue(LeverBlock.FACING, Direction.NORTH)
                .setValue(LeverBlock.POWERED, muted);
        pose.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(lever, pose, buffer, packedLight, packedOverlay);
        pose.popPose();
    }

    private static int resolveInteriorLight(Level level, BlockPos pos, int fallback) {
        int block = LightTexture.block(fallback);
        int sky = LightTexture.sky(fallback);

        for (Direction direction : Direction.values()) {
            int sample = LevelRenderer.getLightColor(level, pos.relative(direction));
            block = Math.max(block, LightTexture.block(sample));
            sky = Math.max(sky, LightTexture.sky(sample));
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int sample = LevelRenderer.getLightColor(level, pos.relative(direction).above());
            block = Math.max(block, LightTexture.block(sample));
            sky = Math.max(sky, LightTexture.sky(sample));
        }

        return LightTexture.pack(block, sky);
    }

    private static void applyWorkTransform(PoseStack pose, Direction facing) {
        pose.translate(0.5D, FLOOR_SURFACE_Y, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0D, 0.0D, 4.0D / 16.0D);
        pose.scale(WORK_SCALE, WORK_SCALE, WORK_SCALE);
        pose.translate(-0.5D, 0.0D, -0.5D);
    }
}
