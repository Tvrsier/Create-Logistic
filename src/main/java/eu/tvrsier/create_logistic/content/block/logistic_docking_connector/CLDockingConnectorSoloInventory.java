package eu.tvrsier.create_logistic.content.block.logistic_docking_connector;

import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;

public class CLDockingConnectorSoloInventory extends SingleSlotContainer {
    private boolean allowInsertion = false;
    public CLDockingConnectorSoloInventory() { super(64); }

    public void dock() { this.allowInsertion = true; }

    public void unDock() { this.allowInsertion = false; }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper info) { return this.allowInsertion; }
}
