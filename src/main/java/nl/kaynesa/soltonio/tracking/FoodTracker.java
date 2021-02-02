package nl.kaynesa.soltonio.tracking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
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

        String temp = String.format("queueNutrition: %d \n addedHearts: %d \n queueDistinctFoodCount: %d \n varietyModifier: %f",
                progressInfo.getQueueNutrition(), progressInfo.getAddedHearts(), progressInfo.getQueueDistinctFoodCount(), progressInfo.getVarietyModifier()); // TODO: 31-Jan-21 Remof 
        player.sendStatusMessage(new StringTextComponent(temp),false);

        if (newHPSet) {
            if (SOLTonioConfig.shouldPlayMilestoneSounds()) {
                // passing the player makes it not play for some reason
                world.playSound(
                        null,
                        player.getPosition(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                        1.0F, 1.0F
                );
            }

            if (SOLTonioConfig.shouldSpawnMilestoneParticles()) {
                spawnParticles(world, player, ParticleTypes.HEART, 12);

                if (progressInfo.hasReachedMax()) {
                    spawnParticles(world, player, ParticleTypes.HAPPY_VILLAGER, 16);
                }
            }
            /*
            ITextComponent heartsDescription = localizedQuantityComponent("message", "hearts", SOLTonioConfig.queueNutritionPerHeart());

            if (SOLTonioConfig.shouldShowProgressAboveHotbar()) {
                String messageKey = progressInfo.hasReachedMax() ? "finished.hotbar" : "milestone_achieved";
                player.sendStatusMessage(localizedComponent("message", messageKey, heartsDescription), true);
            } else {
                showChatMessage(player, TextFormatting.DARK_AQUA, localizedComponent("message", "milestone_achieved", heartsDescription));
                if (progressInfo.hasReachedMax()) {
                    showChatMessage(player, TextFormatting.GOLD, localizedComponent("message", "finished.chat"));
                }
            }

             */
        }
    }

    private static void spawnParticles(ServerWorld world, PlayerEntity player, IParticleData type, int count) {
        // this overload sends a packet to the client
        world.spawnParticle(
                type,
                player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ(),
                count,
                0.5F, 0.5F, 0.5F,
                0.0F
        );
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
