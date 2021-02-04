package nl.kaynesa.soltonio.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.kaynesa.soltonio.SOLTonio;
import nl.kaynesa.soltonio.SOLTonioConfig;
import nl.kaynesa.soltonio.tracking.FoodQueue;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SOLTonio.MOD_ID)
public class TooltipHandle {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        System.out.println("Tooltip");
        if (!SOLTonioConfig.isFoodTooltipEnabled()) return;

        PlayerEntity player = event.getPlayer();
        if (player == null) return;

        Item food = event.getItemStack().getItem();
        if (!food.isFood()) return;

        FoodQueue foodQueue = FoodQueue.get(player);
        int recentlyEaten = foodQueue.recentlyEaten(food);
        boolean isAllowed = SOLTonioConfig.isAllowed(food);
        boolean isHearty = SOLTonioConfig.isHearty(food);

        List<ITextComponent> tooltip = event.getToolTip();

        if (!isAllowed) {
            tooltip.add(styledText("Tasty, but this ain't it chief", TextFormatting.DARK_RED)); //// TODO: 01-Feb-21 Localisation
        } else if (isHearty) {
            if (recentlyEaten < 0) {
                tooltip.add(styledText("Not eaten recently", TextFormatting.BLUE));
            } else if (recentlyEaten < 1) {
                tooltip.add(styledText("Just eaten", TextFormatting.BLUE));
            } else {
                tooltip.add(styledText(String.format("Eaten %d meals ago", recentlyEaten), TextFormatting.BLUE));
            }
        } else {
            tooltip.add(styledText("Doesn't look too nutritious", TextFormatting.RED));
        }

        //tooltip.add(styledText(String.format("Foodval: %d Saturation: %f", Objects.requireNonNull(food.getFood()).getHealing(), food.getFood().getSaturation()),TextFormatting.GRAY));//// TODO: 02-Feb-21 remof
    }

    private static ITextComponent styledText(String text, TextFormatting format) {
        return new StringTextComponent(text).modifyStyle(style -> style.applyFormatting(format));
    }

    private TooltipHandle() {
    }
}
