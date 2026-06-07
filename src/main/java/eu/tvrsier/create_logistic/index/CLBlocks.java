package eu.tvrsier.create_logistic.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.content.block.logistic_controller.CLControllerBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.CLPairedDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.toggle_link.ToggleLinkBlock;
import eu.tvrsier.create_logistic.data.CLBlockStateGen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CLBlocks {

    private static final CreateRegistrate REGISTRATE = CreateLogistic.getRegistrate();

    public static final BlockEntry<CLControllerBlock> LOGISTIC_CONTROLLER =
            REGISTRATE.block("logistic_controller", CLControllerBlock::new)
                    .properties(p -> p
                            .strength(1.3f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                    )
                    .simpleItem()
                    .register();

    public static final BlockEntry<ToggleLinkBlock> TOGGLE_LINK =
            REGISTRATE.block("toggle_link", ToggleLinkBlock::new)
                    .properties(p -> p
                            .strength(0.7f)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .pushReaction(PushReaction.DESTROY)
                    )
                    .simpleItem()
                    .register();

    public static final BlockEntry<CLDockingConnectorBlock> LOGISTIC_DOCKING_CONNECTOR_BLOCK =
            REGISTRATE.block("logistic_docking_connector", CLDockingConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p
                            .sound(SoundType.NETHERITE_BLOCK)
                            .isRedstoneConductor(CLBlocks::never)
                            .forceSolidOn()
                    )
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::dynamicShape)
                    .blockstate(CLBlockStateGen::facingPoweredAxisBlockState)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<CLDockingConnectorBlock> LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE.block("logistic_docking_connector", CLDockingConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p
                            .sound(SoundType.NETHERITE_BLOCK)
                            .isRedstoneConductor(CLBlocks::never)
                            .forceSolidOn()
                    )
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::dynamicShape)
                    // temporaneamente, poi possiamo sostituire con un nostro generator se serve
                    .blockstate(CLBlockStateGen::facingPoweredAxisBlockState)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<CLPairedDockingConnectorBlock> PAIRED_LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE.block("paired_logistic_docking_connector", CLPairedDockingConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .blockstate((c, p) -> p.simpleBlock(
                            c.get(),
                            p.models().getExistingFile(p.modLoc("block/logistic_docking_connector/block"))
                    ))
                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(properties -> properties
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK)
                            .forceSolidOff())
                    .register();

    private static Boolean never(BlockState state, BlockGetter getter, BlockPos pos) {
        return false;
    }

    public static void register() {

    }
}
