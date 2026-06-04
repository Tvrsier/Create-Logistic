package eu.tvrsier.create_logistic.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import eu.tvrsier.create_logistic.CreateLogistic;
import eu.tvrsier.create_logistic.content.block.logistic_controller.LogisticControllerBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.LogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.logistic_docking_connector.PairedLogisticDockingConnectorBlock;
import eu.tvrsier.create_logistic.content.block.toggle_link.ToggleLinkBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CLBlocks {

    private static final CreateRegistrate REGISTRATE = CreateLogistic.getRegistrate();

    public static final BlockEntry<LogisticControllerBlock> LOGISTIC_CONTROLLER =
            REGISTRATE.block("logistic_controller", LogisticControllerBlock::new)
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

    public static final BlockEntry<LogisticDockingConnectorBlock> LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE.block("logistic_docking_connector", LogisticDockingConnectorBlock::new)
                    .properties(p -> p
                            .strength(1.3f, 6.0f)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .dynamicShape()
                    )
                    .simpleItem()
                    .register();

    public static final BlockEntry<PairedLogisticDockingConnectorBlock> PAIRED_LOGISTIC_DOCKING_CONNECTOR =
            REGISTRATE.block("paired_logistic_docking_connector", PairedLogisticDockingConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(p -> p
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK)
                            .forceSolidOff()
                    )
                    .register();

    public static void register() {

    }
}
