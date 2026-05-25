package eu.tvrsier.create_logistic.logistic.inventory;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import eu.tvrsier.create_logistic.logistic.vehicle.LogisticVehicleContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogisticInventoryScanner {

    public static final Logger LOGGER = LogUtils.getLogger();

    private LogisticInventoryScanner() {}

    public static List<IItemHandler> scan(LogisticVehicleContext context) {
        List<IItemHandler> inventories = new ArrayList<>();
        Set<BlockEntity> seen = new HashSet<>();

        ServerLevel scanLevel = context.status().subLevel().getLevel();
        var subLevel = context.status().subLevel();

        if (subLevel == null || subLevel.getPlot() == null) {
            return List.of();
        }

        LevelPlot plot = subLevel.getPlot();

        for (PlotChunkHolder chunk : plot.getLoadedChunks()) {
            var bounds = chunk.getBoundingBox();
            LOGGER.info(
                    "Chunk pos={}, minBlockX={}, minBlockZ={}, bounds={}",
                    chunk.getPos(),
                    chunk.getPos().getMinBlockX(),
                    chunk.getPos().getMinBlockZ(),
                    bounds
            );
            if (bounds == null) {
                continue;
            }

            LOGGER.info("Scanning plot chunk for vehicle {}", context.vehicleId());
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                    for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                        LOGGER.info("Chunk bounds: {}", bounds);
                        BlockPos pos = new BlockPos(
                                x + chunk.getPos().getMinBlockX(),
                                y,
                                z + chunk.getPos().getMinBlockZ()
                        );

                        if (pos.equals(context.controllerPos())) continue;

                        BlockState state = scanLevel.getBlockState(pos);

                        if (!state.isAir()) {
                            LOGGER.info("Found block at {}: {}", pos, state.getBlock());
                        }

                        BlockEntity blockEntity = scanLevel.getBlockEntity(pos);

                        if (blockEntity == null || !seen.add(blockEntity)) continue;

                        LOGGER.info("Found block entity at {}: {}", pos, blockEntity.getType());

                        IItemHandler handler = scanLevel.getCapability(
                                Capabilities.ItemHandler.BLOCK,
                                pos,
                                null
                        );

                        if (handler != null) {
                            inventories.add(handler);
                        }
                    }
                }
            }
        }
        return inventories;
    }
}
