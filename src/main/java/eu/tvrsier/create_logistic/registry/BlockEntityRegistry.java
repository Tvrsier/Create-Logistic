package eu.tvrsier.create_logistic.registry;

import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.block.entity.LogisticControllerBlockEntity;
import eu.tvrsier.create_logistic.block.entity.LogisticDockingConnectorBlockEntity;
import eu.tvrsier.create_logistic.block.entity.ToggleLinkBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, CreateLogistic.MODID);

    public static final Supplier<BlockEntityType<LogisticControllerBlockEntity>> LOGISTIC_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
                    "logistic_controller",
                    () -> BlockEntityType.Builder.of(
                            LogisticControllerBlockEntity::new,
                            BlockRegistry.LOGISTIC_CONTROLLER.get()
                    ).build(null)
            );

    public static final Supplier<BlockEntityType<ToggleLinkBlockEntity>> TOGGLE_LINK =
            BLOCK_ENTITIES.register(
                    "toggle_link",
                    () -> BlockEntityType.Builder.of(
                            ToggleLinkBlockEntity::new,
                            BlockRegistry.TOGGLE_LINK.get()
                    ).build(null)
            );

    public static final Supplier<BlockEntityType<LogisticDockingConnectorBlockEntity>> LOGISTIC_DOCKING_CONNECTOR =
            BLOCK_ENTITIES.register(
                    "logistic_docking_connector",
                    () -> BlockEntityType.Builder.of(
                            LogisticDockingConnectorBlockEntity::new,
                            BlockRegistry.LOGISTIC_DOCKING_CONNECTOR.get()
                    ).build(null)
            );

    private BlockEntityRegistry() {}
}
