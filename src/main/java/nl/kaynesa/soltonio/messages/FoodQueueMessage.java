package nl.kaynesa.soltonio.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.kaynesa.soltonio.tracking.FoodQueue;

import java.util.function.Supplier;

public class FoodQueueMessage {
    private final CompoundNBT capabilityNBT;

    public FoodQueueMessage(FoodQueue foodQueue) {
        this.capabilityNBT = foodQueue.serializeNBT();
    }

    public FoodQueueMessage(PacketBuffer buffer) {
        this.capabilityNBT = buffer.readCompoundTag();
    }

    public void write(PacketBuffer buffer) {
        buffer.writeCompoundTag(capabilityNBT);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Handler.handle(this, context));
    }

    private static class Handler {
        static void handle(FoodQueueMessage message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                PlayerEntity player = Minecraft.getInstance().player;
                assert player != null;
                FoodQueue.get(player).deserializeNBT(message.capabilityNBT);
            });
            context.get().setPacketHandled(true);
        }
    }
}