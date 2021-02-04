package nl.kaynesa.soltonio.tracking;

import nl.kaynesa.soltonio.SOLTonioConfig;

public class ProgressInfo {

    private final double queueNutrition;
    private final int addedHearts;
    private final int queueDistinctFoodCount;
    private final float varietyModifier;

    ProgressInfo(FoodQueue foodQueue) {
        queueNutrition = foodQueue.getQueueNutritionValue() * 0.9 - foodQueue.getTopQueueDistinctFoodCount();// Okay hear me out, the 0.9 is something we calculated using a brute force method
        queueDistinctFoodCount = foodQueue.getQueueDistinctFoodCount();
        varietyModifier = (float) Math.min((float) (queueDistinctFoodCount - 1) / (SOLTonioConfig.getMinFoodInQueueForFullBonus() - 1), 1);
        addedHearts = (int) (queueNutrition / SOLTonioConfig.getQueueNutritionPerHeart() * varietyModifier);

    }

    public int getAddedHearts() {
        return (hasReachedMax() ? SOLTonioConfig.getMaxAddedHeartsFromFood() : addedHearts) * 2;
    }

    public boolean hasReachedMax() {
        return addedHearts >= SOLTonioConfig.getMaxAddedHeartsFromFood();
    }

    public double getQueueNutrition() {
        return queueNutrition;
    }

    public int getQueueDistinctFoodCount() {
        return queueDistinctFoodCount;
    }

    public float getVarietyModifier() {
        return varietyModifier;
    }
}
