package eu.tvrsier.create_logistic.redstone;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

public class ControllerTransmitter implements IRedstoneLinkable {

    private final LevelAccessor world;
    private final BlockPos pos;
    private final Couple<RedstoneLinkNetworkHandler.Frequency> networkKey;

    private int ticksRemaining;
    private boolean registered;

    public ControllerTransmitter(LevelAccessor world, BlockPos pos, ItemStack firstFrequency,
            ItemStack secondFrequency, int durationTicks) {
        this.world = world;
        this.pos = pos;
        this.networkKey = Couple.create(RedstoneLinkNetworkHandler.Frequency.of(firstFrequency),
                RedstoneLinkNetworkHandler.Frequency.of(secondFrequency));
        this.ticksRemaining = durationTicks;
    }

    public void register() {
        if (registered) return;

        registered = true;
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(world, this);
    }

    public void tick() {
        if(ticksRemaining > 0) {
            ticksRemaining--;
        }
        if(ticksRemaining <= 0) {
            unregister();
        }
    }

    public void unregister() {
        if(!registered) return;

        registered = false;
        Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(world, this);
    }

    @Override
    public int getTransmittedStrength() {
        return isAlive() ? 15 : 0;
    }

    @Override
    public void setReceivedStrength(int power) {
        return;
    }

    @Override
    public boolean isListening()  {
        return false;
    }

    @Override
    public boolean isAlive() {
        return registered && ticksRemaining > 0;
    }

    @Override
    public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
        return networkKey;
    }

    @Override
    public BlockPos getLocation() {
        return pos;
    }
}
