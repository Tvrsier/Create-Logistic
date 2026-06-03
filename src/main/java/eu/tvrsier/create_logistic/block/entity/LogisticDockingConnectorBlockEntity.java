package eu.tvrsier.create_logistic.block.entity;

import eu.tvrsier.create_logistic.block.LogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.block.PairedLogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.index.CLBlocks;
import eu.tvrsier.create_logistic.logistic.docking_connector.LogisticDockingState;
import eu.tvrsier.create_logistic.logistic.vehicle.LogisticVehicleContext;
import eu.tvrsier.create_logistic.registry.BlockEntityRegistry;
import eu.tvrsier.create_logistic.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class LogisticDockingConnectorBlockEntity extends BlockEntity {

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_DOCKING_STATE = "DockingState";

    private final ItemStackHandler localInventory = new ItemStackHandler(9);

    private LogisticDockingState dockingState = LogisticDockingState.UNPOWERED;

    private LogisticVehicleContext vehicleContext;

    public LogisticDockingConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tickServer() {
        if (!(level instanceof ServerLevel)) return;

        BlockState state = getBlockState();

        if (!(state.getBlock() instanceof LogisticDockingConnectorBlock)) return;

        boolean powered = state.getValue(LogisticDockingConnectorBlock.POWERED);
        boolean extended = state.getValue(LogisticDockingConnectorBlock.EXTENDED);

        if (powered) {
            if (!extended) {
                setExtended(true);
            }

            if (dockingState == LogisticDockingState.UNPOWERED) {
                dockingState = LogisticDockingState.POWERED;
                setChanged();
            }

            return;
        }

        if(extended) {
            // TODO detach / unlock paired connector
            setExtended(false);
        }

        if (dockingState != LogisticDockingState.UNPOWERED) {
            dockingState = LogisticDockingState.UNPOWERED;
            setChanged();
        }
    }

    private void setExtended(boolean extended) {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        Direction facing = state.getValue(LogisticDockingConnectorBlock.FACING);
        BlockPos pairedPos = worldPosition.relative(facing);

        if (extended) {
            if (!canExtendTo(pairedPos)) {
                extended = false;
            } else if (level.getBlockState(pairedPos).isAir()) {
                level.setBlockAndUpdate(pairedPos,
                        CLBlocks.PAIRED_LOGISTIC_DOCKING_CONNECTOR.get().
                                defaultBlockState().
                                setValue(LogisticDockingConnectorBlock.FACING, facing.getOpposite()));
            }
        } else {
            if (level.getBlockState(pairedPos).getBlock() instanceof PairedLogisticDockingConnectorBlock) {
                level.removeBlock(pairedPos, false);
            }
        }

        level.setBlockAndUpdate(
                worldPosition,
                state.setValue(LogisticDockingConnectorBlock.EXTENDED, extended)
        );

        setChanged();
    }

    public IItemHandler getEffectiveItemHandler() {
        if (vehicleContext != null && vehicleContext.inventoryState() != null) {
            IItemHandler combinedHandler = vehicleContext.inventoryState().combinedHandler();

            if (combinedHandler.getSlots() > 0) {
                return combinedHandler;
            }
        }

        return localInventory;
    }

    public ItemStackHandler getLocalInventory() { return localInventory; }

    public LogisticDockingState getDockingState() { return dockingState; }

    public void setVehicleContext(LogisticVehicleContext vehicleContext) {
        this.vehicleContext = vehicleContext;
        setChanged();
    }

    public void clearVehicleContext(LogisticVehicleContext vehicleContext) {
        if (this.vehicleContext == vehicleContext) {
            this.vehicleContext = null;
            setChanged();
        }
    }

    public boolean hasVehicleContext() {
        return vehicleContext != null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put(TAG_INVENTORY, localInventory.serializeNBT(registries));
        tag.putString(TAG_DOCKING_STATE, dockingState.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains(TAG_INVENTORY)) localInventory.deserializeNBT(registries, tag.getCompound(TAG_INVENTORY));

        if (tag.contains(TAG_DOCKING_STATE)) {
            try {
                dockingState = LogisticDockingState.valueOf(tag.getString(TAG_DOCKING_STATE));
            } catch (IllegalArgumentException ignored) {
                dockingState = LogisticDockingState.UNPOWERED;
            }
        }
    }

    public float getExtensionDistance(float partialTicks) {
        return getBlockState().getValue(LogisticDockingConnectorBlock.EXTENDED) ? 1.0f : 0.0f;
    }

    public float getFeetRotation(float partialTicks) {
        return getBlockState().getValue(LogisticDockingConnectorBlock.EXTENDED) ? 1.0f : 0.0f;
    }

    public boolean isRetracted() {
        return !getBlockState().getValue(LogisticDockingConnectorBlock.EXTENDED);
    }

    private boolean canExtendTo(BlockPos pairedPos) {
        if (level == null) return false;

        BlockState frontState = level.getBlockState(pairedPos);

        if (frontState.isAir()) return true;

        if (frontState.getBlock() instanceof PairedLogisticDockingConnectorBlock) return true;

        return frontState.getCollisionShape(level, pairedPos).isEmpty();
    }

    public AABB getBoundingBox(BlockState state) {
        return Shulker.getProgressAabb(
                1,
                state.getValue(ShulkerBoxBlock.FACING),
                getExtensionDistance(1.0F)
        );
    }
}
