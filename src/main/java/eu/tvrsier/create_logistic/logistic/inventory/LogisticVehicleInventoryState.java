package eu.tvrsier.create_logistic.logistic.inventory;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LogisticVehicleInventoryState {

    private final Map<BlockPos, IItemHandler> inventories = new HashMap<>();

    public Collection<IItemHandler> inventories() {
        return Collections.unmodifiableCollection(inventories.values());
    }

    public void replaceAll(Map<BlockPos, IItemHandler> newInventories) {
        inventories.clear();
        inventories.putAll(newInventories);
    }

    public void add(BlockPos pos, IItemHandler inventory) {
        inventories.put(pos.immutable(), inventory);
    }

    public boolean remove(BlockPos pos) {
        var removed = inventories.remove(pos);
        return removed != null;
    }

    public int inventoryCounter() {
        return inventories.size();
    }

    public boolean isEmpty() {
        return inventories.isEmpty();
    }
}
