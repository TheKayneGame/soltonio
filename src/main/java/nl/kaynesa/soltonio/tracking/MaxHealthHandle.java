package nl.kaynesa.soltonio.tracking;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.kaynesa.soltonio.SOLTonio;
import nl.kaynesa.soltonio.SOLTonioConfig;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SOLTonio.MOD_ID)
public final class MaxHealthHandle {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateFoodHPModifier(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        AttributeModifier prevModifier = getHealthModifier(event.getOriginal());
        if (prevModifier == null) return;

        updateHealthModifier(event.getPlayer(), prevModifier);
    }

    public static boolean updateFoodHPModifier(PlayerEntity player) {
        if (player.world.isRemote) return false;

        AttributeModifier prevModifier = getHealthModifier(player);

        int healthPenalty = 2 * (SOLTonioConfig.getBaseHearts() - 10);

        ProgressInfo progressInfo = FoodQueue.get(player).getProgressInfo();
        int addedHealthFromFood = progressInfo.getAddedHearts() ;
        double totalHealthModifier = healthPenalty + addedHealthFromFood;
        boolean hasChanged = prevModifier == null || prevModifier.getAmount() != totalHealthModifier;

        AttributeModifier modifier = new AttributeModifier(
                HEALTH_MODIFIER_ID,
                "Health Gained from Trying New Foods",
                totalHealthModifier,
                AttributeModifier.Operation.ADDITION
        );

        updateHealthModifier(player, modifier);

        return hasChanged;
    }

    @Nullable
    private static AttributeModifier getHealthModifier(PlayerEntity player) {
        return maxHealthAttribute(player).getModifier(HEALTH_MODIFIER_ID);
    }

    private static void updateHealthModifier(PlayerEntity player, AttributeModifier modifier) {
        float oldMax = player.getMaxHealth();

        ModifiableAttributeInstance attribute = maxHealthAttribute(player);
        attribute.removeModifier(modifier);
        attribute.applyPersistentModifier(modifier);

        float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
        // because apparently it doesn't update unless changed
        player.setHealth(1f);
        // adjust current health proportionally to increase in max health
        player.setHealth(newHealth);
    }

    private static ModifiableAttributeInstance maxHealthAttribute(PlayerEntity player) {
        return Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH));
    }

    private MaxHealthHandle() {
    }
}
