package eu.tvrsier.create_logistic.content.logistic.inventory;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.slf4j.Logger;

import java.util.*;

public final class LogisticVehicleInventoryState {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockPos, LogisticInventoryRef> inventories = new HashMap<>();

    public Collection<LogisticInventoryRef> inventories() {
        return Collections.unmodifiableCollection(inventories.values());
    }

    public void replaceAll(Map<BlockPos, LogisticInventoryRef> newInventories) {
        inventories.clear();

        newInventories.forEach((pos, ref) -> {
            BlockPos immutablePos = pos.immutable();
            inventories.put(
                    immutablePos,
                    new LogisticInventoryRef(immutablePos, ref.state(), ref.handler())
            );
        });
    }

    public record LogisticInventoryRef(
            BlockPos localPos,
            BlockState state,
            IItemHandler handler
    ) {}

    public void add(BlockPos pos, BlockState state, IItemHandler handler) {
        BlockPos immutablePos = pos.immutable();
        inventories.put(
                immutablePos,
                new LogisticInventoryRef(immutablePos, state, handler)
        );
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

    public Optional<IItemHandlerModifiable> modifiableAt(BlockPos pos) {
        return get(pos)
                .map(LogisticInventoryRef::handler)
                .filter(IItemHandlerModifiable.class::isInstance)
                .map(IItemHandlerModifiable.class::cast);
    }

    public Optional<LogisticInventoryRef> get(BlockPos pos) {
        return Optional.ofNullable(inventories.get(pos));
    }

    public int totalSlots() {
        return inventories.values()
                .stream()
                .mapToInt(ref -> ref.handler().getSlots())
                .sum();
    }

    public IItemHandler combinedHandler() {
        return new CombinedInvWrapper(
                inventories.values()
                        .stream()
                        .map(LogisticInventoryRef::handler)
                        .filter(IItemHandlerModifiable.class::isInstance)
                        .map(IItemHandlerModifiable.class::cast)
                        .toArray(IItemHandlerModifiable[]::new)
        );
    }

    public void debugDumpContents(UUID vehicleId) {
        LOGGER.info(
                "Inventory dump for vehicle {}: inventories={}, totalSlots={}",
                vehicleId,
                inventoryCounter(),
                totalSlots()
        );

        for (LogisticInventoryRef ref : inventories.values()) {
            IItemHandler handler = ref.handler();

            LOGGER.info(
                    "Inventory at {} block={} slots={}",
                    ref.localPos(),
                    ref.state().getBlock(),
                    handler.getSlots()
            );

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);

                if (!stack.isEmpty()) {
                    LOGGER.info(
                            " - slot {}: {} x {}",
                            slot,
                            stack.getCount(),
                            stack.getDisplayName().getString()
                    );
                }
            }
        }
    }

    public void debugDumpCombinedContents(UUID vehicleId) {
        IItemHandler combined = combinedHandler();

        LOGGER.info(
                "Combined inventory dump for vehicle {}: slots={}",
                vehicleId,
                combined.getSlots()
        );

        for (int slot = 0; slot < combined.getSlots(); slot++) {
            ItemStack stack = combined.getStackInSlot(slot);

            if (!stack.isEmpty()) {
                LOGGER.info(
                        " - combined slot {}: {} x {}",
                        slot,
                        stack.getCount(),
                        stack.getDisplayName().getString()
                );
            }
        }
    }
}
