package eu.tvrsier.create_logistic.content.logistic.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class CLNeoForgeCommonEvents {

    @SubscribeEvent
    public static void postServerTick(final ServerTickEvent.Post event) {
        final MinecraftServer server = event.getServer();

        for (final ServerLevel level: server.getAllLevels()) {
            CLCommonEvents.onServerTickEnd(level);
        }
    }
}
