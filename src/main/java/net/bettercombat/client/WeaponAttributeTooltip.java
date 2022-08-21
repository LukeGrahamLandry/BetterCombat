package net.bettercombat.client;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import static net.minecraft.item.ItemStack.MODIFIER_FORMAT;

public class WeaponAttributeTooltip {
    public static void initialize() {
        ItemTooltipCallback.EVENT.register((itemStack, context, lines) -> {
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if (attributes != null) {
                // Looking for last attribute line in the list
                int lastAttributeLine = 0;
                int firstHandLine = 0;
                String attributePrefix = "attribute.modifier";
                String handPrefix = "item.modifiers";
                for (int i = 0; i < lines.size(); i++) {
                    Text line = lines.get(i);
                    Text content = line;
                    // Is this a line like "+1 Something"
                    if (line instanceof TranslatableText) {
                        String key = ((TranslatableText) line).getKey();
                        if (key.startsWith(attributePrefix)) {
                            lastAttributeLine = i;
                        }
                        if (firstHandLine == 0 && key.startsWith(handPrefix)) {
                            firstHandLine = i;
                        }
                    } else {
                        for(Text part: line.getSiblings()) {
                            if (part instanceof TranslatableText) {
                                if (((TranslatableText) part).getKey().startsWith(attributePrefix)) {
                                    lastAttributeLine = i;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (BetterCombatClient.config.isTooltipAttackRangeEnabled) {
                    int operationId = EntityAttributeModifier.Operation.ADDITION.getId();
                    String rangeTranslationKey = "attribute.name.generic.attack_range";
                    double rangeValue = attributes.attackRange();
                    MutableText rangeLine = new LiteralText(" ").append(new TranslatableText("attribute.modifier.equals." + operationId, MODIFIER_FORMAT.format(rangeValue), new TranslatableText(rangeTranslationKey))).formatted(Formatting.DARK_GREEN);
                    lines.add(lastAttributeLine + 1, rangeLine);
                }

                if (attributes.isTwoHanded() && firstHandLine > 0) {
                    MutableText handLine = new TranslatableText("item.modifiers.two_handed").formatted(Formatting.GRAY);
                    lines.add(firstHandLine, handLine);
                }
            }
        });
    }
}
