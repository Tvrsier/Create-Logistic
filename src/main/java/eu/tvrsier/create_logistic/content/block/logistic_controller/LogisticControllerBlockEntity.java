package eu.tvrsier.create_logistic.content.block.logistic_controller;

import com.mojang.logging.LogUtils;
import eu.tvrsier.create_logistic.content.logistic.inventory.LogisticInventoryScanner;
import eu.tvrsier.create_logistic.content.logistic.inventory.LogisticVehicleInventoryState;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleContext;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleIdFactory;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleRegistry;
import eu.tvrsier.create_logistic.content.physics.PhysicsContraptionDetector;
import eu.tvrsier.create_logistic.content.physics.PhysicsContraptionStatus;
import eu.tvrsier.create_logistic.redstone.ControllerTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.*;

public class LogisticControllerBlockEntity extends BlockEntity {
    private final List<ControllerTransmitter> activeTransmitters = new ArrayList<>();
    private LogisticVehicleContext vehicleContext;
    private int vehicleDetectionCooldown = 0;
    private int vehicleDetectionAttempts = 0;

    private static final int MAX_VEHICLE_DETECTION_ATTEMPTS = 10;

    private static final Logger LOGGER = LogUtils.getLogger();

    public LogisticControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        tickTransmitters();
    }

    private void registerVehicleIfPresent() {
        if (vehicleContext != null) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        PhysicsContraptionStatus status =
                PhysicsContraptionDetector.detect(serverLevel, worldPosition);

        if (!status.detected()) return;

        UUID vehicleId = LogisticVehicleIdFactory.create(serverLevel, status, worldPosition);
        LOGGER.info(
                "Vehicle registration debug: controllerPos={}, assemblerPos={}, plotPos={}, subLevel={}, vehicleId={}",
                worldPosition,
                status.primaryAssemblerPos(),
                status.subLevel().getPlot().plotPos,
                status.subLevel(),
                vehicleId
        );
        if (LogisticVehicleRegistry.isRegistered(vehicleId)) {
           LOGGER.warn("Cannot register Logistic Controller at {}: vehicle {} already has a controller",
                   worldPosition, vehicleId);
           return;
        }

        vehicleContext = new LogisticVehicleContext(
                vehicleId,
                serverLevel,
                worldPosition,
                status,
                new LogisticVehicleInventoryState()
        );

        Map<BlockPos, LogisticVehicleInventoryState.LogisticInventoryRef> inventories =
                LogisticInventoryScanner.scanChunk(vehicleContext);
        vehicleContext.inventoryState().replaceAll(inventories);
        LogisticVehicleRegistry.register(vehicleContext);

        LOGGER.info(
                "Logistic Vehicle registered: id={}, controller={}, assemblerPos={} level={}, inventories={}, totalSlots={}",
                vehicleContext.vehicleId(),
                vehicleContext.controllerPos(),
                vehicleContext.status().primaryAssemblerPos(),
                vehicleContext.level().dimension().location(),
                vehicleContext.inventoryState().inventoryCounter(),
                vehicleContext.inventoryState().totalSlots()
        );
        setChanged();
    }

    public void tickTransmitters() {
        Iterator<ControllerTransmitter> iterator = activeTransmitters.iterator();

        while (iterator.hasNext()) {
            ControllerTransmitter transmitter = iterator.next();

            transmitter.tick();

            if(!transmitter.isAlive()) {
                iterator.remove();
            }
        }
    }

    public void tickVehicleRegistrationFallback() {
        if (vehicleContext != null) return;
        if (vehicleDetectionAttempts >= MAX_VEHICLE_DETECTION_ATTEMPTS) return;

        if (--vehicleDetectionCooldown > 0) return;

        vehicleDetectionCooldown = 20;

        vehicleDetectionAttempts++;
        registerVehicleIfPresent();
    }

    public LogisticVehicleContext getVehicleContext() {
        return vehicleContext;
    }

    @Override
    public void setRemoved() {
        if (vehicleContext != null) {
            LOGGER.info(
                    "Logistic Controller removed from physics contraption at: {}",
                    worldPosition
            );

            LogisticVehicleRegistry.unregister(vehicleContext.vehicleId());
            vehicleContext = null;
        }

        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (vehicleContext != null) {
            LOGGER.info(
                    "Logistic Controller chunk unloaded, unregistering vehicle: {}",
                    vehicleContext.vehicleId()
            );

            LogisticVehicleRegistry.unregister(vehicleContext.vehicleId());
            vehicleContext = null;
        }

        super.onChunkUnloaded();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && !level.isClientSide) registerVehicleIfPresent();
    }
}
