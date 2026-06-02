package eu.tvrsier.create_logistic.client;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import eu.tvrsier.create_logistic.client.registry.PartialModelRegistry;
import eu.tvrsier.create_logistic.client.renderer.LogisticDockingConnectorRenderer;
import eu.tvrsier.create_logistic.registry.BlockEntityRegistry;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CreateLogisticClient {

    private CreateLogisticClient() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        PartialModelRegistry.init();
    }

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityRegistry.TOGGLE_LINK.get(),
                SmartBlockEntityRenderer::new
        );

        event.registerBlockEntityRenderer(
                BlockEntityRegistry.LOGISTIC_DOCKING_CONNECTOR.get(),
                LogisticDockingConnectorRenderer::new
        );
    }
}
