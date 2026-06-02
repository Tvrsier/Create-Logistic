package eu.tvrsier.create_logistic.block;

import com.mojang.serialization.MapCodec;
import dev.simulated_team.simulated.index.SimBlocks;
import eu.tvrsier.create_logistic.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PairedLogisticDockingConnectorBlock extends DirectionalBlock {

    private static final VoxelShape[] SHAPES = {
            box(0.0, -16.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0),
            box(0.0, 0.0, -16.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 16.0, 32.0),
            box(-16.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 32.0, 16.0, 16.0)
    };

    public PairedLogisticDockingConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(PairedLogisticDockingConnectorBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final @NotNull Level level,
            final @NotNull BlockPos pos, final
            @NotNull BlockState state, final
            @NotNull Player player) {
        if (!level.isClientSide()) {
            if (player.isCreative()) {
                final BlockPos connectorPos = pos.relative(state.getValue(FACING));
                final BlockState connectorState = level.getBlockState(connectorPos);
                if (connectorState.is(BlockRegistry.LOGISTIC_DOCKING_CONNECTOR.get())) {
                    level.setBlock(connectorPos, Blocks.AIR.defaultBlockState(), 3);
                }
            } else {
                dropResources(state, level, pos, null, player, player.getMainHandItem());
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(final @NotNull Level level,
            final @NotNull Player player,
            final @NotNull BlockPos pos,
            final @NotNull BlockState state,
            @Nullable final BlockEntity blockEntity,
            final @NotNull ItemStack tool) {
        super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, tool);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState connectorBlock = level.getBlockState(pos.relative(facing));

        return connectorBlock.is(BlockRegistry.LOGISTIC_DOCKING_CONNECTOR.get())
                && connectorBlock.getValue(LogisticDockingConnectorBlock.FACING) == facing.getOpposite()
                && connectorBlock.getValue(LogisticDockingConnectorBlock.EXTENDED);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);

        if (facing != direction) return super.updateShape(state, direction, neighborState, level, pos, neighborPos);

        if (neighborState.is(BlockRegistry.LOGISTIC_DOCKING_CONNECTOR.get())
            && neighborState.getValue(LogisticDockingConnectorBlock.FACING) == facing.getOpposite()
            && neighborState.getValue(LogisticDockingConnectorBlock.EXTENDED)) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
            }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return null;
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, final Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(final BlockState state, final Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull LevelReader level, final @NotNull BlockPos pos, final @NotNull BlockState state) {
        return BlockRegistry.LOGISTIC_DOCKING_CONNECTOR.toStack();
    }
}
