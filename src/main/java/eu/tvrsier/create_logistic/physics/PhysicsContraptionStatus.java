package eu.tvrsier.create_logistic.physics;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;

public record PhysicsContraptionStatus(
        boolean detected,
        ServerSubLevel subLevel,
        BlockPos primaryAssemblerPos
) {
    public static PhysicsContraptionStatus none() {
        return new PhysicsContraptionStatus(false, null, null);
    }

    public static PhysicsContraptionStatus detected(ServerSubLevel subLevel, BlockPos primaryAssemblerPos) {
        return new PhysicsContraptionStatus(true, subLevel, primaryAssemblerPos);
    }
}