package eu.tvrsier.create_logistic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.client.registry.PartialModelRegistry;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class LogisticDockingConnectorRenderer extends SafeBlockEntityRenderer<LogisticDockingConnectorBlockEntity> {

    public LogisticDockingConnectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(LogisticDockingConnectorBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource bufferSource, int light, int overlay) {
        VertexConsumer vb = bufferSource.getBuffer(RenderType.cutout());
        Direction direction = be.getBlockState().getValue(LogisticDockingConnectorBlock.FACING);
        BlockState blockState = be.getBlockState();
        float rotation = be.getFeetRotation(partialTicks) * 90;

        SuperByteBuffer piston1 = CachedBuffers.partial(PartialModelRegistry.DOCKING_CONNECTOR_MAIN_PISTON_BOTTOM, blockState);
        SuperByteBuffer piston2 = CachedBuffers.partial(PartialModelRegistry.DOCKING_CONNECTOR_MAIN_PISTON_TOP, blockState);
        ms.pushPose();
        rotateToFaceCentered(ms, direction);

        float extension = be.getExtensionDistance(partialTicks);

        piston1.translate(0, extension * 0.5, 0);
        piston2.translate(0, extension, 0);
        piston1.light(light).renderInto(ms, vb);
        piston2.light(light).renderInto(ms, vb);

        Vector2f footAnchor = new Vector2f();
        Vector2f sidePistonTopAnchor = new Vector2f();
        Vector2f sidePistonBottomAnchor = new Vector2f();
        Vector2f relativeAnchor = new Vector2f();

        footAnchor.set(-7.5f, 15.5f).div(16).add(0, extension);
        rotateVector2f(sidePistonTopAnchor.set(1.5f, -2.5f).div(16), rotation).add(footAnchor);
        sidePistonBottomAnchor.set(-6, 2).div(16).add(0, extension / 2);

        relativeAnchor.set(sidePistonTopAnchor).sub(sidePistonBottomAnchor);
        relativeAnchor.normalize();

        Matrix4f rotationMatrix = new Matrix4f(
                1, 0, 0, 0,
                0, relativeAnchor.y, relativeAnchor.x, 0,
                0, -relativeAnchor.x, relativeAnchor.y, 0,
                0, 0, 0, 1
        );

        for (int i = 0; i < 4; i++) {
            ms.pushPose();
            ms.translate(0.5, 0, 0.5);
            TransformStack.of(ms).rotateYDegrees(i * 90);

            SuperByteBuffer sidePiston1 = CachedBuffers.partial(PartialModelRegistry.DOCKING_CONNECTOR_SIDE_PISTON_BOTTOM, blockState);
            SuperByteBuffer sidePiston2 = CachedBuffers.partial(PartialModelRegistry.DOCKING_CONNECTOR_SIDE_PISTON_TOP, blockState);
            SuperByteBuffer foot = CachedBuffers.partial(PartialModelRegistry.DOCKING_CONNECTOR_FOOT, blockState);

            sidePiston1.translate(0, sidePistonBottomAnchor.y, sidePistonBottomAnchor.x);
            sidePiston2.translate(0, sidePistonTopAnchor.y, sidePistonTopAnchor.x);
            foot.translate(0, footAnchor.y, footAnchor.x);
            foot.rotateXDegrees(rotation);

            sidePiston1.mulPose(rotationMatrix);
            sidePiston2.mulPose(rotationMatrix);

            sidePiston1.light(light).renderInto(ms, vb);
            sidePiston2.light(light).renderInto(ms, vb);
            foot.light(light).renderInto(ms, vb);
            ms.popPose();
        }
        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public static void rotateToFaceCentered(PoseStack ms, Direction facing) {
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
                .uncenter();
    }

    private static Vector2f rotateVector2f(Vector2f vec, float angle) {
        angle = (float) Math.toRadians(angle);
        float s = Mth.sin(angle);
        float c = Mth.cos(angle);
        vec.set(vec.x * c + vec.y * s, vec.y * c - vec.x * s);
        return vec;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull LogisticDockingConnectorBlockEntity be) {
        BlockPos blockPos = be.getBlockPos();
        Direction facing = be.getBlockState().getValue(LogisticDockingConnectorBlock.FACING);

        return new AABB(blockPos).expandTowards(
                facing.getStepX() * 2.0D,
                facing.getStepY() * 2.0D,
                facing.getStepZ() * 2.0D
        );
    }
}
