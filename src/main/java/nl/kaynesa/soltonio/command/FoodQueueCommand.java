package nl.kaynesa.soltonio.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.kaynesa.soltonio.SOLTonio;
import nl.kaynesa.soltonio.tracking.FoodQueue;
import nl.kaynesa.soltonio.tracking.ProgressInfo;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

@Mod.EventBusSubscriber(modid = SOLTonio.MOD_ID)
public class FoodQueueCommand {
    private static final String name = "soltonio";

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal(name)
                        .then(withPlayerArgumentOrSender(literal("stats"), FoodQueueCommand::showQueueStats))
        );
    }

    @FunctionalInterface
    private interface CommandWithPlayer {
        int run(CommandContext<CommandSource> context, PlayerEntity target) throws CommandSyntaxException;
    }

    static void sendFeedback(CommandSource source, IFormattableTextComponent message) {
        source.sendFeedback(applyFeedbackStyle(message), true);
    }
    private static IFormattableTextComponent applyFeedbackStyle(IFormattableTextComponent text) {
        return text.modifyStyle(style -> style.applyFormatting(TextFormatting.DARK_AQUA));
    }

    static ArgumentBuilder<CommandSource, ?> withPlayerArgumentOrSender(ArgumentBuilder<CommandSource, ?> base, CommandWithPlayer command) {
        String target = "target";
        return base
                .executes((context) -> command.run(context, context.getSource().asPlayer()))
                .then(argument(target, EntityArgument.player())
                        .executes((context) -> command.run(context, EntityArgument.getPlayer(context, target)))
                );
    }

    static int showQueueStats(CommandContext<CommandSource> context, PlayerEntity target) {
        ProgressInfo progressInfo = FoodQueue.get(target).getProgressInfo();
        String temp = String.format("Queue Nutrition: %d\nAdded Hearts: %d\nQueue Distinct Food Count: %d\nVariety Modifier: %d%%",
                progressInfo.getQueueNutrition(), progressInfo.getAddedHearts(), progressInfo.getQueueDistinctFoodCount(), (int) progressInfo.getVarietyModifier() * 100); // TODO: 31-Jan-21 Remof
        sendFeedback(context.getSource(), new StringTextComponent(temp));
        return 0;
    }
}
