package eu.tvrsier.create_logistic.content.logistic.event;

import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLDockingConnectorBlockEntity;
import net.minecraft.server.level.ServerLevel;

public class CLCommonEvents {

    public static void onServerTickEnd(final ServerLevel level) {
        CLDockingConnectorBlockEntity.MAGNET_CONTROLLER.tick(level);
    }

    public static void onPhysicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        CLDockingConnectorBlockEntity.MAGNET_CONTROLLER.physicsTick(timeStep, level);
    }
}
