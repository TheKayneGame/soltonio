package nl.kaynesa.soltonio.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import nl.kaynesa.soltonio.tracking.CapabilityHandler;
import nl.kaynesa.soltonio.tracking.FoodQueue;

public class SOLTonioAPI {
    @CapabilityInject(FoodCapability.class)
    public static Capability<FoodCapability> foodCapability;

    private SOLTonioAPI() {}

    public static FoodCapability getFoodCapability(PlayerEntity player) {
        return FoodQueue.get(player);
    }

    public static void syncFoodList(PlayerEntity player) {
        CapabilityHandler.syncFoodQueue(player);
    }

}
