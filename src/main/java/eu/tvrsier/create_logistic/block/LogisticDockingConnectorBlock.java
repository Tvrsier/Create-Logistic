package eu.tvrsier.create_logistic.block;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import eu.tvrsier.create_logistic.block.entity.LogisticDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.index.CLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LogisticDockingConnectorBlock extends WrenchableDirectionalBlock implements
        IBE<LogisticDockingConnectorBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");

    public LogisticDockingConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(EXTENDED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockEntity(pos) instanceof LogisticDockingConnectorBlockEntity be && !be.isRetracted()
                ? Shapes.create(be.getBoundingBox(state))
                : Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(EXTENDED, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);

        if (powered != state.getValue(POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, EXTENDED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Class<LogisticDockingConnectorBlockEntity> getBlockEntityClass() {
        return LogisticDockingConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LogisticDockingConnectorBlockEntity> getBlockEntityType() {
        return CLBlockEntityTypes.LOGISTIC_DOCKING_CONNECTOR.get();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }

        return (tickerLevel, pos, tickerState, blockEntity) -> {
            if (blockEntity instanceof LogisticDockingConnectorBlockEntity connector) {
                connector.tickServer();
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            BlockPos pairedPos = pos.relative(facing);

            if (level.getBlockState(pairedPos).getBlock() instanceof PairedLogisticDockingConnectorBlock) {
                level.removeBlock(pairedPos, false);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
