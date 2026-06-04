package eu.tvrsier.create_logistic.content.block.logistic_controller;

import eu.tvrsier.create_logistic.index.CLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class LogisticControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<LogisticControllerBlock> CODEC = simpleCodec(LogisticControllerBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(3, 16, 3, 13, 18, 13),
            Block.box(6, 18, 6, 10, 21, 10),
            Block.box(4.5, 17.8, 4.5, 5.3, 22, 5.3),
            Block.box(10.7, 17.8, 4.5, 11.5, 22, 5.3),
            Block.box(4.5, 17.8, 10.7, 5.3, 22, 11.5),
            Block.box(10.7, 17.8, 10.7, 11.5, 22, 11.5)
    );


    public LogisticControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LogisticControllerBlockEntity(
                CLBlockEntityTypes.LOGISTIC_CONTROLLER.get(),
                pos,
                state
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hitResult
    ) {
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof LogisticControllerBlockEntity be) {
            be.sendTestPulse();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if(level.isClientSide) return null;

        return (lvl, pos, blockStaate, blockEntity) -> {
            if(blockEntity instanceof LogisticControllerBlockEntity be) {
                be.tickServer();
            }
        };
    }
}
