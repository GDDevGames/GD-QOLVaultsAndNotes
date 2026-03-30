package dev.gdawg.qolvaultsandnotes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VaultBlockEntityRenderer
        implements BlockEntityRenderer<VaultBlockEntity, VaultBlockEntityRenderState> {

    private final BlockRenderDispatcher blockRenderDispatcher;

    public VaultBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.blockRenderDispatcher();
    }

    @Override
    public VaultBlockEntityRenderState createRenderState() {
        return new VaultBlockEntityRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(VaultBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
    }

    @Override
    public void extractRenderState(VaultBlockEntity blockEntity,
                                   VaultBlockEntityRenderState renderState,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING);
    }



    @Override
    public void submit(VaultBlockEntityRenderState renderState,
                       PoseStack pose,
                       SubmitNodeCollector collector,
                       CameraRenderState cameraState) {

        float yRot = switch (renderState.facing) {
            case SOUTH -> 0f;
            case EAST  -> 90f;
            case WEST  -> 270f;
            default    -> 180f; // NORTH
        };

        pose.pushPose();

        // Scale up to 2x2x2 blocks
        pose.scale(2.0f, 2.0f, 2.0f);

        // Rotate around center of the scaled model
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(yRot));
        pose.translate(-0.5, 0.0, -0.5);

        collector.submitBlockModel(
                pose,
                RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS),
                blockRenderDispatcher.getBlockModel(renderState.blockState),
                1.0f, 1.0f, 1.0f,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        pose.popPose();
    }
}