package nl.kaynesa.soltonio.tracking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.kaynesa.soltonio.SOLTonio;
import nl.kaynesa.soltonio.SOLTonioConfig;

@Mod.EventBusSubscriber(modid = SOLTonio.MOD_ID)
public class FoodTracker {
    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntity();

        if (player.world.isRemote) return;
        ServerWorld world = (ServerWorld) player.world;

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        boolean isInSurvival = serverPlayer.interactionManager.getGameType() == GameType.SURVIVAL;
        if (SOLTonioConfig.limitProgressionToSurvival() && !isInSurvival) return;

        Item usedItem = event.getItem().getItem();
        if (!usedItem.isFood()) return;

        FoodQueue foodQueue = FoodQueue.get(player);
        boolean isNewRecent = foodQueue.queueFood(usedItem);

        // check this before syncing, because the sync entails an hp update
        boolean newHPSet = MaxHealthHandle.updateFoodHPModifier(player);

        CapabilityHandler.syncFoodQueue(player);
        ProgressInfo progressInfo = foodQueue.getProgressInfo();

//        String temp = String.format("queueNutrition: %f \n addedHearts: %d \n queueDistinctFoodCount: %d \n varietyModifier: %f",
//                progressInfo.getQueueNutrition(), progressInfo.getAddedHearts(), progressInfo.getQueueDistinctFoodCount(), progressInfo.getVarietyModifier()); // TODO: 31-Jan-21 Remof
//        player.sendStatusMessage(new StringTextComponent(temp),false);


    }


    /*
    private static void showChatMessage(PlayerEntity player, TextFormatting color, ITextComponent message) {
        ITextComponent component = localizedComponent("message", "chat_wrapper", message)
                .modifyStyle(style -> style.applyFormatting(color));
        player.sendStatusMessage(component, false);
    }
    */
    private FoodTracker() {}
}
