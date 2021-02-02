package nl.kaynesa.soltonio.tracking;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import nl.kaynesa.soltonio.SOLTonio;
import nl.kaynesa.soltonio.SOLTonioConfig;
import nl.kaynesa.soltonio.api.FoodCapability;
import nl.kaynesa.soltonio.messages.FoodQueueMessage;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@EventBusSubscriber(modid = SOLTonio.MOD_ID)
public final class CapabilityHandler {
    private static final ResourceLocation FOOD = SOLTonio.resourceLocation("food");

    @EventBusSubscriber(modid = SOLTonio.MOD_ID, bus = MOD)
    private static final class Setup {
        @SubscribeEvent
        public static void setUp(FMLCommonSetupEvent event) {
            CapabilityManager.INSTANCE.register(FoodCapability.class, new FoodQueue.Storage(), FoodQueue::new);
        }
    }

    @SubscribeEvent
    public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof PlayerEntity)) return;

        event.addCapability(FOOD, new FoodQueue());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // server needs to send any loaded data to the client
        syncFoodQueue(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncFoodQueue(event.getPlayer());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && SOLTonioConfig.shouldResetOnDeath()) return;

        PlayerEntity originalPlayer = event.getOriginal();
        originalPlayer.revive(); // so we can access the capabilities; entity will get removed either way
        FoodQueue original = FoodQueue.get(originalPlayer);
        FoodQueue newInstance = FoodQueue.get(event.getPlayer());
        newInstance.deserializeNBT(original.serializeNBT());
        // can't sync yet; client hasn't attached capabilities yet
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncFoodQueue(event.getPlayer());
    }

    public static void syncFoodQueue(PlayerEntity player) {
        if (player.world.isRemote) return;

        ServerPlayerEntity target = (ServerPlayerEntity) player;
        SOLTonio.channel.sendTo(
                new FoodQueueMessage(FoodQueue.get(player)),
                target.connection.getNetworkManager(),
                NetworkDirection.PLAY_TO_CLIENT
        );

        MaxHealthHandle.updateFoodHPModifier(player);
    }
}
