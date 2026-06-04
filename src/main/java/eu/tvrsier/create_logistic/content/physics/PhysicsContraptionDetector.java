package eu.tvrsier.create_logistic.content.physics;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.mixin_interface.assembly_preventer.PrimaryAssemblerExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public final class PhysicsContraptionDetector {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PhysicsContraptionDetector() {}

    public static PhysicsContraptionStatus detect(Level level, BlockPos pos) {
        if(!(level instanceof ServerLevel)) return PhysicsContraptionStatus.none();

        SubLevel subLevel = Sable.HELPER.getContaining(level, pos);

        if(!(subLevel instanceof ServerSubLevel serverSubLevel)) {
            return PhysicsContraptionStatus.none();
        }

        BlockPos primaryAssemblerPos = null;

        if(serverSubLevel instanceof PrimaryAssemblerExtension extension) {
            primaryAssemblerPos = extension.simulated$getPrimaryAssembler();
        }

        return PhysicsContraptionStatus.detected(serverSubLevel, primaryAssemblerPos);
    }
}
