package eu.tvrsier.create_logistic.logistic.vehicle;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LogisticVehicleRegistry {

    private static final Map<UUID, LogisticVehicleContext> VEHICLES = new ConcurrentHashMap<>();
    private static final Map<ServerSubLevel, LogisticVehicleContext> SUB_LEVELS = new ConcurrentHashMap<>();

    private LogisticVehicleRegistry() {}

    public static void register(LogisticVehicleContext context) {
        VEHICLES.put(context.vehicleId(), context);
        SUB_LEVELS.put(context.status().subLevel(), context);
    }

    public static void unregister(UUID vehicleId) {
        SUB_LEVELS.remove(getByVehicleId(vehicleId).status().subLevel());
        VEHICLES.remove(vehicleId);
    }

    public static Collection<LogisticVehicleContext> getVehicles() {
        return VEHICLES.values();
    }

    public static boolean isRegistered(UUID vehicleId) {
        return VEHICLES.containsKey(vehicleId);
    }

    public static LogisticVehicleContext getByVehicleId(UUID vehicleId) {
        return VEHICLES.get(vehicleId);
    }

    public static LogisticVehicleContext getBySublevel(ServerSubLevel subLevel) {
        return SUB_LEVELS.get(subLevel);
    }
}
