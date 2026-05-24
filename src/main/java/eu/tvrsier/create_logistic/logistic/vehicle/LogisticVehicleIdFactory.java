package eu.tvrsier.create_logistic.logistic.vehicle;

import eu.tvrsier.create_logistic.physics.PhysicsContraptionStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LogisticVehicleIdFactory {

    private LogisticVehicleIdFactory() {}

    public static UUID create(ServerLevel level, PhysicsContraptionStatus status, BlockPos fallbackPos) {
        ChunkPos plotPos = null;
        if (status.subLevel() != null && status.subLevel().getPlot() != null) {
            plotPos = status.subLevel().getPlot().plotPos;
        }

        String anchor = plotPos != null
                ? "plot:"+ plotPos
                : status.primaryAssemblerPos() != null
                ? "assembler:" + status.primaryAssemblerPos().asLong()
                : "fallback:" + fallbackPos.asLong();

        String raw = level.dimension().location() + ":" + anchor;

        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }
}
