package eu.tvrsier.create_logistic.command;

import com.mojang.brigadier.CommandDispatcher;
import eu.tvrsier.create_logistic.block.entity.LogisticControllerBlockEntity;
import eu.tvrsier.create_logistic.logistic.vehicle.LogisticVehicleContext;
import eu.tvrsier.create_logistic.logistic.vehicle.LogisticVehicleRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CreateLogisticCommands {

    private CreateLogisticCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("create_logistic")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("vehicles")
                                .executes(context ->
                                        printVehicles(context.getSource())))
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
}
