package eu.tvrsier.create_logistic;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import eu.tvrsier.create_logistic.client.CreateLogisticClient;
import eu.tvrsier.create_logistic.command.CreateLogisticCommands;
import eu.tvrsier.create_logistic.content.logistic.event.CLCommonEvents;
import eu.tvrsier.create_logistic.content.logistic.event.CLNeoForgeCommonEvents;
import eu.tvrsier.create_logistic.index.CLBlockEntityTypes;
import eu.tvrsier.create_logistic.index.CLBlocks;
import eu.tvrsier.create_logistic.index.CLItems;
import eu.tvrsier.create_logistic.registrate.CreateLogisticRegistrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateLogistic.MODID) public class CreateLogistic {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "create_logistic";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "create_logistic" namespace
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "create_logistic"
    // namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    private static final NonNullSupplier<CreateLogisticRegistrate> REGISTRATE =
            NonNullSupplier.lazy(() ->
                    (CreateLogisticRegistrate) new CreateLogisticRegistrate(
                            asResource("main"),
                            MODID
                    ).defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            );

    // Create a dedicated creative tab for this mod so the "Create Logistic" tab appears and contains our items
    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> LOGISTIC_TAB =
            CREATIVE_MODE_TABS.register("create_logistic_tab",
                    () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.create_logistic"))
                            .icon(() -> CLBlocks.LOGISTIC_CONTROLLER.asItem().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                CreateLogisticRegistrate.TAB_ITEMS.forEach(itemSupplier -> output.accept(itemSupplier.get()));
                            })
                            .build());

    // Registered example items/blocks were removed; use dedicated ModBlocks/ModItems and a tab defined in assets lang.
    // If you want a dedicated creative tab, register it in the CREATIVE_MODE_TABS register and add items in the
    // BuildCreativeModeTabContentsEvent listener below.

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateLogistic(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        getRegistrate().registerEventListeners(modEventBus);
        CLBlocks.register();
        CLItems.register();
        CLBlockEntityTypes.register();
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(CLNeoForgeCommonEvents.class);

        SableEventPlatform.INSTANCE.onPhysicsTick(CLCommonEvents::onPhysicsTick);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with
    // @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent public static void onClientSetup(FMLClientSetupEvent event) {
            CreateLogisticClient.onClientSetup(event);
        }
        @SubscribeEvent public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            CreateLogisticClient.registerBlockEntityRenderers(event);
        }
        @SubscribeEvent public static void onRegisterCommands(RegisterCommandsEvent event) {
            CreateLogisticCommands.register(event.getDispatcher());
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static CreateLogisticRegistrate getRegistrate() {
        return REGISTRATE.get();
    }
}
