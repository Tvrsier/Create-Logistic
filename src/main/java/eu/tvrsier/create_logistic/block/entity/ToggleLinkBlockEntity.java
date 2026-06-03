package eu.tvrsier.create_logistic.block.entity;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkFrequencySlot;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import eu.tvrsier.create_logistic.block.ToggleLinkBlock;
import eu.tvrsier.create_logistic.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ToggleLinkBlockEntity extends SmartBlockEntity {
    private boolean receivedSignalChanged;
    private int receivedSignal;
    private int rawReceivedSignal;
    private boolean lastNetworkPowered;
    private LinkBehaviour link;

    public ToggleLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void addLinkBehaviour(List<BlockEntityBehaviour> behaviours) {
        if (link != null) {
            return;
        }
        createLink();
        behaviours.add(link);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        addLinkBehaviour(behaviours);
    }

    @Override
    public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours) {
        addLinkBehaviour(behaviours);
    }

    protected void createLink() {
        Pair<ValueBoxTransform, ValueBoxTransform> slots =
                ValueBoxTransform.Dual.makeSlots(RedstoneLinkFrequencySlot::new);
        link = LinkBehaviour.receiver(this, slots, this::setSignal);
    }

    public int getReceivedSignal() {
        return receivedSignal;
    }

    public void setSignal(int power) {
        rawReceivedSignal = power;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState blockState = getBlockState();
        boolean networkPowered = rawReceivedSignal > 0;
        if (networkPowered && !lastNetworkPowered) {
            receivedSignal = receivedSignal > 0 ? 0 : 15;
            receivedSignalChanged = true;
            boolean powered = receivedSignal > 0;
            if (powered != blockState.getValue(ToggleLinkBlock.POWERED)) {
                level.setBlockAndUpdate(
                        worldPosition,
                        blockState.setValue(ToggleLinkBlock.POWERED, powered)
                );
            }
        }
        lastNetworkPowered = networkPowered;
        if (receivedSignalChanged) {
            updateSelfAndAttached(getBlockState());
            setChanged();
        }
    }

    @Override
    public void remove() {
        super.remove();

        if (level != null && !level.isClientSide) {
            updateSelfAndAttached(getBlockState());
        }
    }

    public void updateSelfAndAttached(BlockState blockState) {
        if (level == null) {
            return;
        }
        Direction attachedFace = blockState.getValue(ToggleLinkBlock.FACING).getOpposite();
        BlockPos attachedPos = worldPosition.relative(attachedFace);
        level.blockUpdated(worldPosition, level.getBlockState(worldPosition).getBlock());
        level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
        receivedSignalChanged = false;
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putInt("Receive", receivedSignal);
        compound.putBoolean("ReceivedChanged", receivedSignalChanged);
        compound.putInt("RawReceive", rawReceivedSignal);
        compound.putBoolean("LastNetworkPowered", lastNetworkPowered);

        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        receivedSignal = compound.getInt("Receive");
        receivedSignalChanged = compound.getBoolean("ReceivedChanged");
        rawReceivedSignal = compound.getInt("RawReceive");
        lastNetworkPowered = compound.getBoolean("LastNetworkPowered");
    }
}

