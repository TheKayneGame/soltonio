package nl.kaynesa.soltonio.tracking;

import nl.kaynesa.soltonio.SOLTonioConfig;

public class ProgressInfo {

    private final int queueNutrition;
    private final int addedHearts;
    private final int queueDistinctFoodCount;
    private final float varietyModifier;

    ProgressInfo(FoodQueue foodQueue) {
        queueNutrition = foodQueue.getQueueNutritionValue();
        queueDistinctFoodCount = foodQueue.getQueueDistinctFoodCount();
        varietyModifier = (float) Math.min((float) (queueDistinctFoodCount - 1) / (SOLTonioConfig.getMinFoodInQueueForFullBonus() - 1), 1);
        addedHearts = (int) (queueNutrition / SOLTonioConfig.queueNutritionPerHeart() * varietyModifier);


    }

    public int getAddedHearts() {
        return (hasReachedMax() ? SOLTonioConfig.getMaxAddedHeartsFromFood() : addedHearts) * 2;
    }

    public boolean hasReachedMax() {
        return addedHearts >= SOLTonioConfig.getMaxAddedHeartsFromFood();
    }

    public int getQueueNutrition() {
        return queueNutrition;
    }

    public int getQueueDistinctFoodCount() {
        return queueDistinctFoodCount;
    }

    public float getVarietyModifier() {
        return varietyModifier;
    }
}
