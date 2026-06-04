package eu.tvrsier.create_logistic.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.content.block.logistic_controller.LogisticControllerBlockEntity;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.content.block.toggle_link.ToggleLinkBlockEntity;
import eu.tvrsier.create_logistic.registrate.CreateLogisticRegistrate;

public class CLBlockEntityTypes {

    private static final CreateLogisticRegistrate REGISTRATE = CreateLogistic.getRegistrate();

    public static final BlockEntityEntry<LogisticControllerBlockEntity> LOGISTIC_CONTROLLER =
            REGISTRATE
                    .<LogisticControllerBlockEntity>blockEntity("logistic_controller", LogisticControllerBlockEntity::new)
                    .validBlocks(CLBlocks.LOGISTIC_CONTROLLER)
                    .register();

    public static final BlockEntityEntry<ToggleLinkBlockEntity> TOGGLE_LINK =
            REGISTRATE
                    .<ToggleLinkBlockEntity>blockEntity("toggle_link", ToggleLinkBlockEntity::new)
                    .validBlocks(CLBlocks.TOGGLE_LINK)
                    .register();

    public static final BlockEntityEntry<LogisticDockingConnectorBlockEntity> LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE
                    .<LogisticDockingConnectorBlockEntity>blockEntity("logistic_docking_connector", LogisticDockingConnectorBlockEntity::new)
                    .validBlocks(CLBlocks.LOGISTIC_DOCKING_CONNECTOR)
                    .register();

    public static void register() {

    }
}
