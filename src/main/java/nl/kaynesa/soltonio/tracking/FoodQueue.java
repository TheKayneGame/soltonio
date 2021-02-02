package nl.kaynesa.soltonio.tracking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import nl.kaynesa.soltonio.SOLTonioConfig;
import nl.kaynesa.soltonio.api.FoodCapability;
import nl.kaynesa.soltonio.api.SOLTonioAPI;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public final class FoodQueue implements FoodCapability {
    private static final String NBT_KEY_QUEUE_FOOD_LIST = "queueFoodList";

    public static FoodQueue get(PlayerEntity player) {
        return (FoodQueue) player.getCapability(SOLTonioAPI.foodCapability)
                .orElseThrow(FoodQueueNotFoundException::new);
    }
    private final Queue<FoodInstance> foodQueue = new LinkedList<>();

    @Nullable
    private ProgressInfo cachedProgressInfo;

    private final LazyOptional<FoodQueue> capabilityOptional = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == SOLTonioAPI.foodCapability ? capabilityOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();

        ListNBT foodQueueList = new ListNBT();
        foodQueue.stream()
                .map(FoodInstance::encode)
                .filter(Objects::nonNull)
                .map(StringNBT::valueOf)
                .forEach(foodQueueList::add);

        tag.put(NBT_KEY_QUEUE_FOOD_LIST, foodQueueList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        ListNBT foodQueueList = tag.getList(NBT_KEY_QUEUE_FOOD_LIST, Constants.NBT.TAG_STRING);

        foodQueue.clear();

        foodQueueList.stream()
                .map(nbt -> (StringNBT) nbt)
                .map(StringNBT::getString)
                .map(FoodInstance::decode)
                .filter(Objects::nonNull)
                .forEach(foodQueue::add);

        invalidateProgressInfo();
    }

    public boolean queueFood(Item food) {
        if (isQueueFull()) {
            foodQueue.remove();
        }
        boolean wasAdded = foodQueue.add(new FoodInstance(food)) && SOLTonioConfig.shouldCount(food);
        invalidateProgressInfo();
        return wasAdded;
    }

    public boolean isQueueFull() {
        return foodQueue.size() > SOLTonioConfig.getFoodQueueSize();
    }

    public int getQueueNutritionValue() {
        return foodQueue.isEmpty() ? 0 : foodQueue.stream().distinct().map(foodInstance -> Objects.requireNonNull(foodInstance.item.getFood()).getHealing()).reduce(Integer::sum).get();
    }

    public int getQueueDistinctFoodCount() {
        return foodQueue.isEmpty() ? 0 : (int) foodQueue.stream().distinct().count();
    }

    public void clearQueue() {
        foodQueue.clear();
        invalidateProgressInfo();
    }

    public int recentlyEaten(Item food) {
        if (!food.isFood()) return -2;
        final ArrayList j = new ArrayList(foodQueue);
        Collections.reverse(j);
        int i = j.indexOf(new FoodInstance(food));
        return i;
    }

    public Queue<FoodInstance> getFoodQueue() {
        return new LinkedList<FoodInstance>(foodQueue);
    }

    public ProgressInfo getProgressInfo() {
        if (cachedProgressInfo == null) {
            cachedProgressInfo = new ProgressInfo(this);
        }
        return cachedProgressInfo;
    }

    public void invalidateProgressInfo() {
        cachedProgressInfo = null;
    }

    public static final class Storage implements Capability.IStorage<FoodCapability> {
        @Override
        public INBT writeNBT(Capability<FoodCapability> capability, FoodCapability instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<FoodCapability> capability, FoodCapability instance, Direction side, INBT tag) {
            instance.deserializeNBT((CompoundNBT) tag);
        }
    }

    public static class FoodQueueNotFoundException extends RuntimeException {
        public FoodQueueNotFoundException() {
            super("Player must have food capability attached, but none was found.");
        }
    }
}
