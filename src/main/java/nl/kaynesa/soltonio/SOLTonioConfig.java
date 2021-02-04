package nl.kaynesa.soltonio;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import nl.kaynesa.soltonio.tracking.CapabilityHandler;
import nl.kaynesa.soltonio.tracking.FoodQueue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SOLTonioConfig {
    private static String localizationPath(String path) {
        return "config." + SOLTonio.MOD_ID + "." + path;
    }

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = specPair.getLeft();
        CLIENT_SPEC = specPair.getRight();
    }

    public static void setup() {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfig.Reloading event) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;

        PlayerList players = currentServer.getPlayerList();
        for (PlayerEntity player : players.getPlayers()) {
            FoodQueue.get(player).invalidateProgressInfo();
            CapabilityHandler.syncFoodQueue(player);
        }
    }

    public static int getBaseHearts() {
        return SERVER.baseHearts.get();
    }

    public static double getQueueNutritionPerHeart() {
        return SERVER.queueNutritionPerHeart.get();
    }

    public static int getMaxAddedHeartsFromFood() {
        return SERVER.maxAddedHeartsFromFood.get();
    }

    public static int getMinFoodInQueueForFullBonus() {
        return SERVER.minFoodInQueueForFullBonus.get();
    }

    public static int getMaxDistinctThatCounts() {
        return SERVER.maxDistinctThatCounts.get();
    }

    public static List<String> getBlacklist() {
        return new ArrayList<>(SERVER.blacklist.get());
    }

    public static List<String> getWhitelist() {
        return new ArrayList<>(SERVER.whitelist.get());
    }

    public static int getMinimumFoodValue() {
        return SERVER.minimumFoodValue.get();
    }

    public static boolean shouldResetOnDeath() {
        return SERVER.shouldResetOnDeath.get();
    }

    public static boolean limitProgressionToSurvival() {
        return SERVER.limitProgressionToSurvival.get();
    }

    public static int getFoodQueueSize() {
        return SERVER.foodQueueSize.get();
    }

    public static class Server {
        public final ForgeConfigSpec.IntValue baseHearts;
        public final ForgeConfigSpec.DoubleValue queueNutritionPerHeart;
        public final ForgeConfigSpec.IntValue maxAddedHeartsFromFood;
        public final ForgeConfigSpec.IntValue minFoodInQueueForFullBonus;
        public final ForgeConfigSpec.IntValue maxDistinctThatCounts;

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist;
        public final ForgeConfigSpec.IntValue minimumFoodValue;

        public final ForgeConfigSpec.BooleanValue shouldResetOnDeath;
        public final ForgeConfigSpec.BooleanValue limitProgressionToSurvival;
        public final ForgeConfigSpec.IntValue foodQueueSize;

        Server(ForgeConfigSpec.Builder builder) {
            builder.push("Balancing");

            baseHearts = builder
                    .translation(localizationPath("base_hearts"))
                    .comment("Number of hearts you start out with.")
                    .defineInRange("baseHearts", 10, 0, 1000);

            queueNutritionPerHeart = builder
                    .translation(localizationPath("queue_nutrition_per_heart"))
                    .comment("Amount of nutrition needed per heart")
                    .defineInRange("queueNutritionPerHeart", 15d, 0, 1000);

            maxAddedHeartsFromFood = builder
                    .translation(localizationPath("max_added_hearts_from_food"))
                    .comment("Number of extra hearts you can gain from food.")
                    .defineInRange("maxAddedHealthFromFood", 10, 0,1000);

            minFoodInQueueForFullBonus = builder
                    .translation(localizationPath("min_food_in_queue_for_full_bonus"))
                    .comment("Number of distinct food required for maximum bonus potential")
                    .defineInRange("minFoodInQueueForFullBonus", 8, 0,1000);

            maxDistinctThatCounts = builder
                    .translation(localizationPath("max_distinct_that_counts"))
                    .comment("First number of distinct food that counts for the health bonus")
                    .defineInRange("maxDistinctThatCounts", 10, 0,1000);

            builder.pop();
            builder.push("filtering");

            blacklist = builder
                    .translation(localizationPath("blacklist"))
                    .comment("Foods in this list won't affect the player's health nor show up in the food book.")
                    .defineList("blacklist", Lists.newArrayList(), e -> e instanceof String);

            whitelist = builder
                    .translation(localizationPath("whitelist"))
                    .comment("When this list contains anything, the blacklist is ignored and instead only foods from here count.")
                    .defineList("whitelist", Lists.newArrayList(), e -> e instanceof String);

            minimumFoodValue = builder
                    .translation(localizationPath("minimum_food_value"))
                    .comment("The minimum hunger value foods need to provide in order to count for milestones, in half drumsticks.")
                    .defineInRange("minimumFoodValue", 1, 0, 1000);

            builder.pop();
            builder.push("miscellaneous");

            shouldResetOnDeath = builder
                    .translation(localizationPath("reset_on_death"))
                    .comment("Whether or not to reset the food list on death, effectively losing all bonus hearts.")
                    .define("resetOnDeath", false);

            limitProgressionToSurvival = builder
                    .translation(localizationPath("limit_progression_to_survival"))
                    .comment("If true, eating foods outside of survival mode (e.g. creative/adventure) is not tracked and thus does not contribute towards progression.")
                    .define("limitProgressionToSurvival", false);

            foodQueueSize = builder
                    .translation(localizationPath("food_queue_size"))
                    .comment("Set buffer size of eaten meals")
                    .defineInRange("foodQueueSize", 32, 1,100 );

            builder.pop();
        }
    }

    public static boolean isFoodTooltipEnabled() {
        return CLIENT.isFoodTooltipEnabled.get();
    }



    public static class Client {
        public final ForgeConfigSpec.BooleanValue shouldPlayMilestoneSounds;
        public final ForgeConfigSpec.BooleanValue shouldSpawnMilestoneParticles;

        public final ForgeConfigSpec.BooleanValue isFoodTooltipEnabled;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("milestone celebration");

            shouldPlayMilestoneSounds = builder
                    .translation(localizationPath("should_play_milestone_sounds"))
                    .comment("If true, reaching a new milestone plays a ding sound.")
                    .define("shouldPlayMilestoneSounds", true);

            shouldSpawnMilestoneParticles = builder
                    .translation(localizationPath("should_spawn_milestone_particles"))
                    .comment("If true, reaching a new milestone spawns particles.")
                    .define("shouldSpawnMilestoneParticles", true);

            builder.pop();
            builder.push("miscellaneous");

            isFoodTooltipEnabled = builder
                    .translation(localizationPath("is_food_tooltip_enabled"))
                    .comment("If true, foods indicate in their tooltips whether or not they have been eaten recently.")
                    .define("isFoodTooltipEnabled", true);

            builder.pop();
        }
    }

    public static boolean hasWhitelist() {
        return !SERVER.whitelist.get().isEmpty();
    }

    public static boolean isAllowed(Item food) {
        String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
        if (hasWhitelist()) {
            return matchesAnyPattern(id, SERVER.whitelist.get());
        } else {
            return !matchesAnyPattern(id, SERVER.blacklist.get());
        }
    }

    public static boolean shouldCount(Item food) {
        return isHearty(food) && isAllowed(food);
    }

    public static boolean isHearty(Item food) {
        Food foodInfo = food.getFood();
        if (foodInfo == null) return false;
        return foodInfo.getHealing() >= SERVER.minimumFoodValue.get();
    }
    private static boolean matchesAnyPattern(String query, Collection<? extends String> patterns) {
        for (String glob : patterns) {
            StringBuilder pattern = new StringBuilder(glob.length());
            for (String part : glob.split("\\*", -1)) {
                if (!part.isEmpty()) { // not necessary
                    pattern.append(Pattern.quote(part));
                }
                pattern.append(".*");
            }

            // delete extraneous trailing ".*" wildcard
            pattern.delete(pattern.length() - 2, pattern.length());

            if (Pattern.matches(pattern.toString(), query)) {
                return true;
            }
        }
        return false;
    }

}
