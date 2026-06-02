package eu.tvrsier.create_logistic.logistic.inventory;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import eu.tvrsier.create_logistic.logistic.vehicle.LogisticVehicleContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;

import java.util.*;

public class LogisticInventoryScanner {

    public static final Logger LOGGER = LogUtils.getLogger();

    private LogisticInventoryScanner() {}

    public static Map<BlockPos, LogisticVehicleInventoryState.LogisticInventoryRef> scanChunk(LogisticVehicleContext context) {
        Map<BlockPos, LogisticVehicleInventoryState.LogisticInventoryRef> inventories = new HashMap<>();
        ServerLevel scanLevel = context.status().subLevel().getLevel();

        for (PlotChunkHolder chunkHolder : context.status().subLevel().getPlot().getLoadedChunks()) {
            LevelChunk chunk = scanLevel.getChunk(chunkHolder.getPos().x, chunkHolder.getPos().z);
            Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();

            for (Map.Entry<BlockPos, BlockEntity> entry : blockEntities.entrySet()) {
                BlockPos pos = entry.getKey();

                if (Sable.HELPER.getContaining(scanLevel, pos) != context.status().subLevel()) {
                    continue;
                }

                IItemHandler handler = scanLevel.getCapability(
                        Capabilities.ItemHandler.BLOCK,
                        pos,
                        null
                );

                if (handler != null) {
                    BlockPos immutablePos = pos.immutable();
                    BlockState state = scanLevel.getBlockState(pos);
                    inventories.put(
                            immutablePos,
                            new LogisticVehicleInventoryState.LogisticInventoryRef(
                                    immutablePos,
                                    state,
                                    handler
                            )
                    );
                }
            }
        }

        return inventories;
    }
}
