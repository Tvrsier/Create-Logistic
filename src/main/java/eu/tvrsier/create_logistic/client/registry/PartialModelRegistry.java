package eu.tvrsier.create_logistic.client.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import eu.tvrsier.create_logistic.CreateLogistic;
import net.minecraft.resources.ResourceLocation;

public class PartialModelRegistry {

    public static final PartialModel DOCKING_CONNECTOR_MAIN_PISTON_BOTTOM =
            block("logistic_docking_connector/main_piston_1");
    public static final PartialModel DOCKING_CONNECTOR_MAIN_PISTON_TOP =
            block("logistic_docking_connector/main_piston_2");
    public static final PartialModel DOCKING_CONNECTOR_SIDE_PISTON_BOTTOM =
            block("logistic_docking_connector/side_piston_1");
    public static final PartialModel DOCKING_CONNECTOR_SIDE_PISTON_TOP =
            block("logistic_docking_connector/side_piston_2");
    public static final PartialModel DOCKING_CONNECTOR_FOOT =
            block("logistic_docking_connector/foot");

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateLogistic.MODID, "block/" + path));
    }

    public static void init() {}
}
