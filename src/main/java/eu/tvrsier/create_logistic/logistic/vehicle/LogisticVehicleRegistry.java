package eu.tvrsier.create_logistic.logistic.vehicle;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LogisticVehicleRegistry {

    private static final Map<UUID, LogisticVehicleContext> VEHICLES = new ConcurrentHashMap<>();

    private LogisticVehicleRegistry() {}

    public static void register(LogisticVehicleContext context) {
        VEHICLES.put(context.vehicleId(), context);
    }

    public static void unregister(UUID vehicleId) {
        VEHICLES.remove(vehicleId);
    }

    public static Collection<LogisticVehicleContext> getVehicles() {
        return VEHICLES.values();
    }

    public static boolean isRegistered(UUID vehicleId) {
        return VEHICLES.containsKey(vehicleId);
    }
}
