package eu.tvrsier.create_logistic.compat.computercraft.wired;

import dev.simulated_team.simulated.service.SimPlatformService;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLDockingConnectorBlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CLDockingConnectorWiredElement {

    boolean CC_LOADED = SimPlatformService.INSTANCE.isLoaded("computercraft");

    void connect(CLDockingConnectorWiredElement other);

    void disconnect(CLDockingConnectorWiredElement other);

    void remove();

    static CLDockingConnectorWiredElement create(final CLDockingConnectorBlockEntity blockEntity) {
        return CC_LOADED
                ? new CLDockingConnectorWiredElementImpl(blockEntity)
                : CLNoopDockingConnectorWiredElement.INSTANCE;
    }
}