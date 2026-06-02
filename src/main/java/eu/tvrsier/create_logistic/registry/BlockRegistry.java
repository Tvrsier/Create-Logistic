package eu.tvrsier.create_logistic.registry;

import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.block.LogisticControllerBlock;
import eu.tvrsier.create_logistic.block.LogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.block.PairedLogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.block.ToggleLinkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockRegistry {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateLogistic.MODID);

    public static final DeferredBlock<Block> LOGISTIC_CONTROLLER =
            BLOCKS.registerBlock(
                    "logistic_controller",
                    LogisticControllerBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(1.3F, 6.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            );

    public static final DeferredBlock<Block> TOGGLE_LINK = BLOCKS.registerBlock("toggle_link",
            ToggleLinkBlock::new,
            BlockBehaviour.Properties.of().
                    strength(0.7F)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> LOGISTIC_DOCKING_CONNECTOR = BLOCKS.registerBlock(
            "logistic_docking_connector",
            LogisticDockingConnectorBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.3F, 6.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .dynamicShape()
    );

    public static final DeferredBlock<Block> PAIRED_LOGISTIC_DOCKING_CONNECTOR =
            BLOCKS.registerBlock(
                    "paired_logistic_docking_connector",
                    PairedLogisticDockingConnectorBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(1.3F, 6.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK)
                            .forceSolidOff()
            );

    private BlockRegistry() {
    }
}