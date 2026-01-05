package com.lemonclient.api.util.misc;

import com.lemonclient.api.setting.values.ModeSetting;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.text.TextFormatting;

public class ColorUtil {
   public static List<String> colors = Arrays.asList(
      "Black",
      "Dark Green",
      "Dark Red",
      "Gold",
      "Dark Gray",
      "Green",
      "Red",
      "Yellow",
      "Dark Blue",
      "Dark Aqua",
      "Dark Purple",
      "Gray",
      "Blue",
      "Aqua",
      "Light Purple",
      "White"
   );

   public static TextFormatting settingToTextFormatting(ModeSetting setting) {
      if (setting.getValue().equalsIgnoreCase("Black")) {
         return TextFormatting.BLACK;
      } else if (setting.getValue().equalsIgnoreCase("Dark Green")) {
         return TextFormatting.DARK_GREEN;
      } else if (setting.getValue().equalsIgnoreCase("Dark Red")) {
         return TextFormatting.DARK_RED;
      } else if (setting.getValue().equalsIgnoreCase("Gold")) {
         return TextFormatting.GOLD;
      } else if (setting.getValue().equalsIgnoreCase("Dark Gray")) {
         return TextFormatting.DARK_GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Green")) {
         return TextFormatting.GREEN;
      } else if (setting.getValue().equalsIgnoreCase("Red")) {
         return TextFormatting.RED;
      } else if (setting.getValue().equalsIgnoreCase("Yellow")) {
         return TextFormatting.YELLOW;
      } else if (setting.getValue().equalsIgnoreCase("Dark Blue")) {
         return TextFormatting.DARK_BLUE;
      } else if (setting.getValue().equalsIgnoreCase("Dark Aqua")) {
         return TextFormatting.DARK_AQUA;
      } else if (setting.getValue().equalsIgnoreCase("Dark Purple")) {
         return TextFormatting.DARK_PURPLE;
      } else if (setting.getValue().equalsIgnoreCase("Gray")) {
         return TextFormatting.GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Blue")) {
         return TextFormatting.BLUE;
      } else if (setting.getValue().equalsIgnoreCase("Light Purple")) {
         return TextFormatting.LIGHT_PURPLE;
      } else if (setting.getValue().equalsIgnoreCase("White")) {
         return TextFormatting.WHITE;
      } else {
         return setting.getValue().equalsIgnoreCase("Aqua") ? TextFormatting.AQUA : null;
      }
   }

   public static ChatFormatting textToChatFormatting(ModeSetting setting) {
      if (setting.getValue().equalsIgnoreCase("Black")) {
         return ChatFormatting.BLACK;
      } else if (setting.getValue().equalsIgnoreCase("Dark Green")) {
         return ChatFormatting.DARK_GREEN;
      } else if (setting.getValue().equalsIgnoreCase("Dark Red")) {
         return ChatFormatting.DARK_RED;
      } else if (setting.getValue().equalsIgnoreCase("Gold")) {
         return ChatFormatting.GOLD;
      } else if (setting.getValue().equalsIgnoreCase("Dark Gray")) {
         return ChatFormatting.DARK_GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Green")) {
         return ChatFormatting.GREEN;
      } else if (setting.getValue().equalsIgnoreCase("Red")) {
         return ChatFormatting.RED;
      } else if (setting.getValue().equalsIgnoreCase("Yellow")) {
         return ChatFormatting.YELLOW;
      } else if (setting.getValue().equalsIgnoreCase("Dark Blue")) {
         return ChatFormatting.DARK_BLUE;
      } else if (setting.getValue().equalsIgnoreCase("Dark Aqua")) {
         return ChatFormatting.DARK_AQUA;
      } else if (setting.getValue().equalsIgnoreCase("Dark Purple")) {
         return ChatFormatting.DARK_PURPLE;
      } else if (setting.getValue().equalsIgnoreCase("Gray")) {
         return ChatFormatting.GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Blue")) {
         return ChatFormatting.BLUE;
      } else if (setting.getValue().equalsIgnoreCase("Light Purple")) {
         return ChatFormatting.LIGHT_PURPLE;
      } else if (setting.getValue().equalsIgnoreCase("White")) {
         return ChatFormatting.WHITE;
      } else {
         return setting.getValue().equalsIgnoreCase("Aqua") ? ChatFormatting.AQUA : null;
      }
   }

   public static Color settingToColor(ModeSetting setting) {
      if (setting.getValue().equalsIgnoreCase("Black")) {
         return Color.BLACK;
      } else if (setting.getValue().equalsIgnoreCase("Dark Green")) {
         return Color.GREEN.darker();
      } else if (setting.getValue().equalsIgnoreCase("Dark Red")) {
         return Color.RED.darker();
      } else if (setting.getValue().equalsIgnoreCase("Gold")) {
         return Color.yellow.darker();
      } else if (setting.getValue().equalsIgnoreCase("Dark Gray")) {
         return Color.DARK_GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Green")) {
         return Color.green;
      } else if (setting.getValue().equalsIgnoreCase("Red")) {
         return Color.red;
      } else if (setting.getValue().equalsIgnoreCase("Yellow")) {
         return Color.yellow;
      } else if (setting.getValue().equalsIgnoreCase("Dark Blue")) {
         return Color.blue.darker();
      } else if (setting.getValue().equalsIgnoreCase("Dark Aqua")) {
         return Color.CYAN.darker();
      } else if (setting.getValue().equalsIgnoreCase("Dark Purple")) {
         return Color.MAGENTA.darker();
      } else if (setting.getValue().equalsIgnoreCase("Gray")) {
         return Color.GRAY;
      } else if (setting.getValue().equalsIgnoreCase("Blue")) {
         return Color.blue;
      } else if (setting.getValue().equalsIgnoreCase("Light Purple")) {
         return Color.magenta;
      } else if (setting.getValue().equalsIgnoreCase("White")) {
         return Color.WHITE;
      } else {
         return setting.getValue().equalsIgnoreCase("Aqua") ? Color.cyan : Color.WHITE;
      }
   }
}
