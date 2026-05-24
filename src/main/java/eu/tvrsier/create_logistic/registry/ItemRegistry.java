package eu.tvrsier.create_logistic.registry;

import eu.tvrsier.create_logistic.CreateLogistic;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateLogistic.MODID);

    public static final DeferredItem<BlockItem> LOGISTIC_CONTROLLER = ITEMS.registerSimpleBlockItem(
            "logistic_controller", BlockRegistry.LOGISTIC_CONTROLLER
    );

    public static final DeferredItem<BlockItem> TOGGLE_LINK = ITEMS.registerSimpleBlockItem(
            "toggle_link", BlockRegistry.TOGGLE_LINK
    );

    private ItemRegistry() {
    }
}
