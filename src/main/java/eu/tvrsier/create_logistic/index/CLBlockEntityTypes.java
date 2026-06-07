package eu.tvrsier.create_logistic.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.service.SimInventoryService;
import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.client.renderer.CLDockingConnectorRenderer;
import eu.tvrsier.create_logistic.content.block.logistic_controller.CLControllerBlockEntity;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.content.block.toggle_link.ToggleLinkBlockEntity;
import eu.tvrsier.create_logistic.registrate.CreateLogisticRegistrate;

public class CLBlockEntityTypes {

    private static final CreateLogisticRegistrate REGISTRATE = CreateLogistic.getRegistrate();

    public static final BlockEntityEntry<CLControllerBlockEntity> LOGISTIC_CONTROLLER =
            REGISTRATE
                    .<CLControllerBlockEntity>blockEntity("logistic_controller", CLControllerBlockEntity::new)
                    .validBlocks(CLBlocks.LOGISTIC_CONTROLLER)
                    .register();

    public static final BlockEntityEntry<ToggleLinkBlockEntity> TOGGLE_LINK =
            REGISTRATE
                    .<ToggleLinkBlockEntity>blockEntity("toggle_link", ToggleLinkBlockEntity::new)
                    .validBlocks(CLBlocks.TOGGLE_LINK)
                    .register();

    public static final BlockEntityEntry<CLDockingConnectorBlockEntity> LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE
                    .blockEntity("logistic_docking_connector", CLDockingConnectorBlockEntity::new)
                    .onRegister(SimInventoryService.INSTANCE.registerInventory((be, dir) -> be.getLogisticInventory(dir)))
                    .onRegister(SimInventoryService.INSTANCE.registerTank((be, dir) -> be.getLogisticTank(dir)))
                    .validBlocks(CLBlocks.LOGISTIC_DOCKING_CONNECTOR)
                    .renderer(() -> CLDockingConnectorRenderer::new)
                    .register();

    public static void register() {

    }
}
