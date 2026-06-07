package eu.tvrsier.create_logistic.client.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import eu.tvrsier.create_logistic.CreateLogistic;

public class CLPartialModels {

    public static final PartialModel
            LOGISTIC_DOCKING_CONNECTOR_MAIN_PISTON_BOTTOM =
            block("logistic_docking_connector/main_piston_1"),
            LOGISTIC_DOCKING_CONNECTOR_MAIN_PISTON_TOP =
                    block("logistic_docking_connector/main_piston_2"),
            LOGISTIC_DOCKING_CONNECTOR_SIDE_PISTON_BOTTOM =
                    block("logistic_docking_connector/side_piston_1"),
            LOGISTIC_DOCKING_CONNECTOR_SIDE_PISTON_TOP =
                    block("logistic_docking_connector/side_piston_2"),
            LOGISTIC_DOCKING_CONNECTOR_FOOT =
                    block("logistic_docking_connector/foot");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateLogistic.asResource("block/" + path));
    }

    public static void init() {
        // init static fields
    }
}
