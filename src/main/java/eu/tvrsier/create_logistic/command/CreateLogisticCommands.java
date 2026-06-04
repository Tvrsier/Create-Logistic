package eu.tvrsier.create_logistic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleContext;
import eu.tvrsier.create_logistic.content.logistic.vehicle.LogisticVehicleRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class CreateLogisticCommands {

    private static final Logger LOGGER = LogUtils.getLogger();

    private CreateLogisticCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("create_logistic")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("vehicles")
                                .executes(context ->
                                        printVehicles(context.getSource())))
                        .then(Commands.literal("inventories")
                                .executes(context ->
                                        getInventoryInVehicle(context.getSource())))
        );
    }

    private static int printVehicles(CommandSourceStack source) {

        var vehicles = LogisticVehicleRegistry.getVehicles();

        if (vehicles.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("No Logistic Vehicles detected."),
                    false
            );

            return 0;
        }

        for (LogisticVehicleContext vehicle : vehicles) {
            source.sendSuccess(
                    () -> Component.literal(
                            "Vehicle id=" + vehicle.vehicleId()
                                    + ", controller=" + vehicle.controllerPos()
                                    + ", level=" + vehicle.level().dimension().location()
                    ),
                    false
            );
        }

        return vehicles.size();
    }

    private static int getInventoryInVehicle(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.literal("Player not found."));
            return 0;
        }

        SubLevel subLevel = Sable.HELPER.getTrackingOrVehicleSubLevel(player);

        if (!(subLevel instanceof ServerSubLevel serverSubLevel)) {
            source.sendFailure(Component.literal("No tracked/vehicle ServerSubLevel found."));
            return 0;
        }

        LogisticVehicleContext vehicle =
                LogisticVehicleRegistry.getBySublevel(serverSubLevel);

        if (vehicle == null) {
            source.sendFailure(Component.literal("No Logistic Vehicle found for current SubLevel."));
            return 0;
        }

        vehicle.inventoryState().debugDumpContents(vehicle.vehicleId());
        vehicle.inventoryState().debugDumpCombinedContents(vehicle.vehicleId());

        source.sendSuccess(
                () -> Component.literal(
                        "Inventory dump logged for vehicle "
                                + vehicle.vehicleId()
                                + " inventories="
                                + vehicle.inventoryState().inventoryCounter()
                                + " totalSlots="
                                + vehicle.inventoryState().totalSlots()
                ),
                false
        );

        return vehicle.inventoryState().inventoryCounter();
    }
}
