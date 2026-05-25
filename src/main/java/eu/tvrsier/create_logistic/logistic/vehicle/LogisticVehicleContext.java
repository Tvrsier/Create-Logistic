package eu.tvrsier.create_logistic.logistic.vehicle;

import eu.tvrsier.create_logistic.logistic.inventory.LogisticVehicleInventoryState;
import eu.tvrsier.create_logistic.physics.PhysicsContraptionStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public record LogisticVehicleContext(
        UUID vehicleId,
        ServerLevel level,
        BlockPos controllerPos,
        PhysicsContraptionStatus status,
        LogisticVehicleInventoryState inventoryState
) {
    public boolean isValid() {
        return status.detected();
    }
}
