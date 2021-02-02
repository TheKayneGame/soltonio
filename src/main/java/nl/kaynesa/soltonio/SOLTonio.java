package nl.kaynesa.soltonio;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import nl.kaynesa.soltonio.messages.FoodQueueMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("soltonio")
@Mod.EventBusSubscriber(modid = SOLTonio.MOD_ID, bus = MOD)
public class SOLTonio {
    public static final  String MOD_ID = "soltonio";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(resourceLocation("main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static ResourceLocation resourceLocation(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public SOLTonio() {
        // Register ourselves for server and other game events we are interested in
        SOLTonioConfig.setup();
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        channel.messageBuilder(FoodQueueMessage.class, 0)
                .encoder(FoodQueueMessage::write)
                .decoder(FoodQueueMessage::new)
                .consumer(FoodQueueMessage::handle)
                .add();

    }
}
