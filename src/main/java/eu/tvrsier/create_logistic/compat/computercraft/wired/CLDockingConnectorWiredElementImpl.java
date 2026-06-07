package eu.tvrsier.create_logistic.compat.computercraft.wired;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredNode;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLDockingConnectorBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CLDockingConnectorWiredElementImpl implements CLDockingConnectorWiredElement, WiredElement {

    private final CLDockingConnectorBlockEntity entity;
    private final WiredNode node;

    public CLDockingConnectorWiredElementImpl(final CLDockingConnectorBlockEntity entity) {
        this.entity = entity;
        this.node = ComputerCraftAPI.createWiredNodeForElement(this);
    }

    @Override
    public WiredNode getNode() {
        return this.node;
    }

    @Override
    public String getSenderID() {
        return "logistic_docking_connector";
    }

    @Override
    public Level getLevel() {
        return this.entity.getLevel();
    }

    @Override
    public Vec3 getPosition() {
        return Vec3.atCenterOf(this.entity.getBlockPos());
    }

    @Override
    public void connect(final CLDockingConnectorWiredElement other) {
        if (other instanceof CLDockingConnectorWiredElementImpl we) {
            getNode().connectTo(we.getNode());
        }
    }

    @Override
    public void disconnect(final CLDockingConnectorWiredElement other) {
        if (other instanceof CLDockingConnectorWiredElementImpl we) {
            getNode().disconnectFrom(we.getNode());
        }
    }

    @Override
    public void remove() {
        this.getNode().remove();
    }
}
