package eu.tvrsier.create_logistic.compat.computercraft.wired;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum CLNoopDockingConnectorWiredElement implements CLDockingConnectorWiredElement {
    INSTANCE;

    @Override
    public void connect(CLDockingConnectorWiredElement other) {
    }

    @Override
    public void disconnect(CLDockingConnectorWiredElement other) {
    }

    @Override
    public void remove() {
    }
}
