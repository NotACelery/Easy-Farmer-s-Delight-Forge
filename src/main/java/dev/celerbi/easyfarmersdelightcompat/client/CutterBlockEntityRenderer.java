package dev.celerbi.easyfarmersdelightcompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.celerbi.easyfarmersdelightcompat.block.CutterBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class CutterBlockEntityRenderer implements BlockEntityRenderer<CutterBlockEntity> {
    private static final ResourceLocation CUTTING_BOARD_ID = new ResourceLocation("farmersdelight",
            "cutting_board");
    private static final TagKey<Item> FLAT = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("farmersdelight", "flat_on_cutting_board"));
    private static final float WORK_SCALE = .45F, VILLAGER_SCALE = .45F;
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;
    private final VillagerRenderer villagerRenderer;
    private final Block cuttingBoard;
    public CutterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft mc = Minecraft.getInstance();
        blockRenderer = context.getBlockRenderDispatcher();
        itemRenderer = mc.getItemRenderer();
        EntityRendererProvider.Context ec = new EntityRendererProvider.Context(
                mc.getEntityRenderDispatcher(),
                mc.getItemRenderer(),
                mc.getBlockRenderer(),
                mc.gameRenderer.itemInHandRenderer,
                mc.getResourceManager(),
                mc.getEntityModels(),
                mc.font
        );
        villagerRenderer = new VillagerRenderer(ec);
        cuttingBoard = BuiltInRegistries.BLOCK.get(CUTTING_BOARD_ID);
    }

    @Override
    public void render(CutterBlockEntity cutter, float pt, PoseStack pose, MultiBufferSource buffer, int light,
            int overlay) {
        Direction facing = cutter.getBlockState().hasProperty(CutterBlock.FACING) ? cutter.getBlockState()
                .getValue(CutterBlock.FACING) : Direction.SOUTH;
        ItemStack shown = cutter.displayInput();
        Level level = cutter.getLevel();
        int interiorLight = cutter.isItemPreview() || level == null
                ? light
                : resolveInteriorLight(level, cutter.getBlockPos(), light);

        renderVillager(cutter, facing, pose, buffer, interiorLight);
        if (cutter.isItemPreview())
            renderPreviewContents(cutter, shown, facing, pose, buffer, interiorLight, overlay);
        else
            renderWorkstation(cutter, shown, facing, pose, buffer, interiorLight, overlay);
    }

    private void renderVillager(CutterBlockEntity cutter, Direction facing, PoseStack pose, MultiBufferSource buffer,
            int light) {
        Villager v = cutter.villagerAdapter().getVillagerEntity();
        if (v == null)
            return;
        pose.pushPose();
        pose.translate(.5D, 1D / 16D, .5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0D, 0D, -4D / 16D);
        pose.scale(VILLAGER_SCALE, VILLAGER_SCALE, VILLAGER_SCALE);
        villagerRenderer.render(v, 0F, 1F, pose, buffer, light);
        pose.popPose();
    }

    private void renderWorkstation(CutterBlockEntity cutter, ItemStack shown, Direction facing, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        pose.pushPose();
        applyWorkTransform(pose, facing);
        blockRenderer.renderSingleBlock(cutter.logVariant().defaultBlockState(), pose, buffer, light, overlay);
        renderBoardAndItem(cutter, shown, pose, buffer, light, overlay, true);
        pose.popPose();
    }

    private void renderPreviewContents(CutterBlockEntity cutter, ItemStack shown, Direction facing, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        if (shown.isEmpty())
            return;
        pose.pushPose();
        applyWorkTransform(pose, facing);
        renderBoardAndItem(cutter, shown, pose, buffer, light, overlay, true);
        pose.popPose();
    }

    private void renderBoardAndItem(CutterBlockEntity cutter, ItemStack shown, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay, boolean renderBoard) {
        if (cuttingBoard == Blocks.AIR)
            return;
        pose.pushPose();
        pose.translate(0D, 1D, 0D);
        if (renderBoard)
            blockRenderer.renderSingleBlock(cuttingBoard.defaultBlockState(), pose, buffer, light, overlay);
        renderItem(cutter, shown, pose, buffer, light, overlay);
        pose.popPose();
    }

    private void renderItem(CutterBlockEntity cutter, ItemStack shown, PoseStack pose, MultiBufferSource buffer,
            int light, int overlay) {
        if (shown.isEmpty() || cutter.getLevel() == null)
            return;
        pose.pushPose();
        pose.pushPose();
        boolean block = itemRenderer.getModel(shown, cutter.getLevel(), null, 0)
                .applyTransform(ItemDisplayContext.FIXED, pose, false)
                .isGui3d();
        pose.popPose();
        if (block && !shown.is(FLAT)) {
            pose.translate(.5D, .30D, .5D);
            pose.scale(.8F, .8F, .8F);
        } else {
            pose.translate(.5D, .11D, .5D);
            pose.mulPose(Axis.XP.rotationDegrees(90F));
            pose.scale(.6F, .6F, .6F);
        }
        itemRenderer.renderStatic(shown, ItemDisplayContext.FIXED, light, overlay, pose, buffer, cutter.getLevel(),
                cutter.getBlockPos().hashCode());
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
        pose.translate(.5D, 1D / 16D, .5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0D, 0D, 2D / 16D);
        pose.translate(-.5D, 0D, -.5D);
        pose.scale(WORK_SCALE, WORK_SCALE, WORK_SCALE);
        pose.translate(.5D / WORK_SCALE - .5D, 0D, .5D / WORK_SCALE - .5D);
    }
}
