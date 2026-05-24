package eu.tvrsier.create_logistic.block.entity;

import com.mojang.logging.LogUtils;
import eu.tvrsier.create_logistic.physics.PhysicsContraptionDetector;
import eu.tvrsier.create_logistic.physics.PhysicsContraptionStatus;
import eu.tvrsier.create_logistic.redstone.ControllerTransmitter;
import eu.tvrsier.create_logistic.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LogisticControllerBlockEntity extends BlockEntity {
    private final List<ControllerTransmitter> activeTransmitters = new ArrayList<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    public LogisticControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.LOGISTIC_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public void sendTestPulse() {
        if(!(level instanceof ServerLevel serverLevel)) return;

        PhysicsContraptionStatus status = PhysicsContraptionDetector.detect(serverLevel, worldPosition);

        if (status.detected()) {
            LOGGER.info("Controller is mounted on a physics contraption");
        } else {
            LOGGER.info("Controller is in normal world");
        }
        ControllerTransmitter transmitter = new ControllerTransmitter(
                serverLevel, worldPosition, new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT),
                20
        );

        activeTransmitters.add(transmitter);
        transmitter.register();
    }

    public void tickServer() {
        Iterator<ControllerTransmitter> iterator = activeTransmitters.iterator();

        while (iterator.hasNext()) {
            ControllerTransmitter transmitter = iterator.next();

            transmitter.tick();

            if(!transmitter.isAlive()) {
                iterator.remove();
            }
        }
    }
}
