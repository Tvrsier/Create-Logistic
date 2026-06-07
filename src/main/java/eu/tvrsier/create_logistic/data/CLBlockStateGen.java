package eu.tvrsier.create_logistic.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CLBlockStateGen {

    public static <T extends Block> void facingPoweredAxisBlockState(
            final DataGenContext<Block, T> ctx,
            final RegistrateBlockstateProvider prov
    ) {
        prov.directionalBlock(ctx.getEntry(),
                blockState -> prov.models().getExistingFile(
                        prov.modLoc("block/" + ctx.getName() + "/block" +
                                (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : ""))
                ));
    }
}
