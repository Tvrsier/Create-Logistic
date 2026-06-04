package eu.tvrsier.create_logistic.content.block.logistic_docking_connector;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import eu.tvrsier.create_logistic.index.CLBlocks;
import eu.tvrsier.create_logistic.content.logistic.docking_connector.LogisticDockingState;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleContext;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class LogisticDockingConnectorBlockEntity extends SmartBlockEntity {

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_DOCKING_STATE = "DockingState";

    private final ItemStackHandler localInventory = new ItemStackHandler(9);

    private LogisticDockingState dockingState = LogisticDockingState.UNPOWERED;

    private LogisticVehicleContext vehicleContext;

    public LerpedFloat extension = LerpedFloat.linear().chase(0, 0.1, LerpedFloat.Chaser.LINEAR);
    public LerpedFloat feet = LerpedFloat.linear().chase(0, 0.15, LerpedFloat.Chaser.LINEAR);

    public BlockPos otherConnectorPos = null;

    public LogisticDockingConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void tickServer(boolean powered) {
        if (!(level instanceof ServerLevel)) return;

        if (powered) {
            if (dockingState == LogisticDockingState.UNPOWERED) {
                dockingState = LogisticDockingState.POWERED;
                setChanged();
            }

            return;
        }

        if (hasOtherConnector()) unDock();

        if (dockingState != LogisticDockingState.UNPOWERED) {
            dockingState = LogisticDockingState.UNPOWERED;
            setChanged();
            sendData();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) return;

        BlockState state = getBlockState();

        if (!(state.getBlock() instanceof LogisticDockingConnectorBlock)) return;

        boolean powered = state.getValue(LogisticDockingConnectorBlock.POWERED);

        extension.updateChaseTarget(powered ? 1 : 0);

        boolean blockStateExtended = state.getValue(LogisticDockingConnectorBlock.EXTENDED);

        extension.tickChaser();

        boolean shouldBeExtended = extension.getValue() == 1 && powered;
        if (!level.isClientSide && blockStateExtended != shouldBeExtended) {
            setExtended(shouldBeExtended);
        }

        if(!level.isClientSide)
            tryPairSameGrid();

        feet.updateChaseTarget(hasOtherConnector() ? 1 : 0);
        feet.tickChaser();

        if (!level.isClientSide) {
            tickServer(powered);
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
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put(TAG_INVENTORY, localInventory.serializeNBT(registries));
        tag.putString(TAG_DOCKING_STATE, dockingState.name());
        if (otherConnectorPos != null)
            tag.put("OtherConnector", NbtUtils.writeBlockPos(otherConnectorPos));

        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (tag.contains(TAG_INVENTORY)) {
            localInventory.deserializeNBT(registries, tag.getCompound(TAG_INVENTORY));
        }

        if(tag.contains(TAG_DOCKING_STATE)) {
            try {
                dockingState = LogisticDockingState.valueOf(tag.getString(TAG_DOCKING_STATE));
            } catch (IllegalArgumentException e) {
                dockingState = LogisticDockingState.UNPOWERED;
            }
        }

        if (tag.contains("OtherConnector"))
            otherConnectorPos = NbtUtils.readBlockPos(tag, "OtherConnector").orElse(null);
        else
            otherConnectorPos = null;

        super.read(tag, registries, clientPacket);
    }

    public float getExtensionDistance(float partialTicks) {
        float value = extension.getValue(partialTicks);
        return value * value * (3 - 2 * value);
    }

    public boolean isExtended() {
        return extension.getValue() == 1 && getBlockState().getValue(LogisticDockingConnectorBlock.POWERED);
    }

    public boolean isRetracted() {
        return extension.getValue() == 0;
    }

    public boolean hasOtherConnector() {
        return otherConnectorPos != null;
    }

    public LogisticDockingConnectorBlockEntity getOtherConnector() {
        if (level == null || otherConnectorPos == null)
            return null;

        return level.getBlockEntity(otherConnectorPos) instanceof LogisticDockingConnectorBlockEntity other
                ? other : null;
    }

    public void pairTo(LogisticDockingConnectorBlockEntity other) {
        if (other == null || other == this) return;

        if (other.getBlockPos().equals(otherConnectorPos)) return;

        LogisticDockingConnectorBlockEntity currentOther = getOtherConnector();
        if (currentOther != null && getBlockPos().equals(currentOther.getBlockPos())) {
            currentOther.unDock();
        }

        this.unDock();
        other.unDock();

        this.otherConnectorPos = other.getBlockPos();
        other.otherConnectorPos = getBlockPos();

        this.dockingState = LogisticDockingState.LOCKING;
        other.dockingState = LogisticDockingState.LOCKING;

        this.setChanged();
        other.setChanged();

        this.sendData();
        other.sendData();
    }

    public void unDock() {
        LogisticDockingConnectorBlockEntity other = getOtherConnector();

        otherConnectorPos = null;

        dockingState = isExtended()
                ? LogisticDockingState.POWERED
                : LogisticDockingState.UNPOWERED;

        setChanged();
        sendData();

        if (other != null && other.otherConnectorPos != null && other.otherConnectorPos.equals(getBlockPos())) {
            other.otherConnectorPos = null;
            other.dockingState = other.isExtended()
                    ? LogisticDockingState.POWERED
                    : LogisticDockingState.UNPOWERED;

            other.setChanged();
            other.sendData();
        }
    }

    private void tryPairSameGrid() {
        if (level == null || level.isClientSide) return;

        if (!isExtended() || hasOtherConnector()) return;

        Direction facing = getBlockState().getValue(LogisticDockingConnectorBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing, 3);

        if (!(level.getBlockEntity(otherPos) instanceof LogisticDockingConnectorBlockEntity other)) return;

        if (!other.isExtended()) return;

        Direction otherFacing = other.getBlockState().getValue(LogisticDockingConnectorBlock.FACING);

        if (otherFacing != facing.getOpposite()) return;

        pairTo(other);
    }

    public float getFeetRotation(float partialTicks) {
        float rotation = feet.getValue(partialTicks);

        if (feet.getChaseTarget() == 1) {
            rotation *= rotation;
        }

        return rotation;
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}
