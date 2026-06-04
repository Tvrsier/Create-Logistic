package eu.tvrsier.create_logistic.content.logistic.event;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleContext;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;

import java.util.Optional;

@EventBusSubscriber(modid = CreateLogistic.MODID)
public final class LogisticVehicleInventoryEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    private LogisticVehicleInventoryEvents() {}

    private record VehicleBlockEventContext(
            ServerLevel level,
            BlockPos pos,
            LogisticVehicleContext vehicle
    ) {}

    private static Optional<VehicleBlockEventContext> resolve(BlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return Optional.empty();

        BlockPos pos = event.getPos();

        SubLevel subLevel = Sable.HELPER.getContaining(level, pos);

        if (!(subLevel instanceof ServerSubLevel serverSubLevel)) return Optional.empty();

        LogisticVehicleContext vehicle = LogisticVehicleRegistry.getBySublevel(serverSubLevel);

        if (vehicle == null) return Optional.empty();

        return Optional.of(new VehicleBlockEventContext(level, pos, vehicle));
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        resolve(event).ifPresent(context -> {
            var state = context.level().getBlockState(context.pos());

            if (state.getBlock() instanceof LogisticDockingConnectorBlock block) {
                LogisticDockingConnectorBlockEntity be = block.getBlockEntity(context.level(), context.pos());

                if (be == null) {
                    LOGGER.warn(
                            "Detected Logistic Docking Connector at {} but cannot find Block Entity",
                            context.pos()
                    );
                    return;
                }

                be.setVehicleContext(context.vehicle());
                LOGGER.info(
                        "Linked Logistic Docking Connector at {} to vehicle {}",
                        context.pos(),
                        context.vehicle().vehicleId()
                );
                return;
            }

            IItemHandler handler = context.level().getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    context.pos(),
                    null
            );

            if (handler == null) return;

            context.vehicle().inventoryState().add(context.pos(), state, handler);

            LOGGER.info(
                    "Found new inventory at {} [{} slots] for vehicle {}. Total inventories: {}, total slots: {}",
                    context.pos(),
                    handler.getSlots(),
                    context.vehicle().vehicleId(),
                    context.vehicle().inventoryState().inventoryCounter(),
                    context.vehicle().inventoryState().totalSlots()
            );
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        resolve(event).ifPresent(context -> {
            var state = context.level().getBlockState(context.pos());

            if (state.getBlock() instanceof LogisticDockingConnectorBlock block) {
                LogisticDockingConnectorBlockEntity be = block.getBlockEntity(context.level(), context.pos());

                if (be != null) {
                    be.clearVehicleContext(context.vehicle());
                }

                LOGGER.info(
                        "Unlinked Logistic Docking Connector at {} from vehicle {}",
                        context.pos(),
                        context.vehicle().vehicleId()
                );
                return;
            }

            boolean removed = context.vehicle().inventoryState().remove(context.pos());

            if (removed) {
                LOGGER.info(
                        "Removed inventory at {} for vehicle {}. Remaining inventories: {}, total slots: {}",
                        context.pos(),
                        context.vehicle().vehicleId(),
                        context.vehicle().inventoryState().inventoryCounter(),
                        context.vehicle().inventoryState().totalSlots()
                );
            }
        });
    }
}
