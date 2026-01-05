package com.lemonclient.api.util.misc;

import com.lemonclient.api.util.chat.ChatUtil;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.chat.NotificationManager;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lemonclient.client.module.modules.hud.Notifications;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MessageBus {
   public static String watermark = ChatFormatting.GREEN + "[" + ChatFormatting.YELLOW + "Lemon" + ChatFormatting.GREEN + "] " + ChatFormatting.RESET;
   public static ChatFormatting messageFormatting = ChatFormatting.GRAY;
   protected static final Minecraft mc = Minecraft.getMinecraft();

   public static void printDebug(String text, Boolean error) {
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      sendClientPrefixMessage(
         (error ? colorMain.getDisabledColor() : colorMain.getEnabledColor()) + text, error ? Notification.Type.ERROR : Notification.Type.INFO
      );
   }

   public static void sendClientPrefixMessage(String message, Notification.Type type) {
      if (mc.world != null && mc.player != null) {
         setWatermark();
         TextComponentString string1 = new TextComponentString(watermark + messageFormatting + message);
         Notifications notifications = ModuleManager.getModule(Notifications.class);
         if (notifications.isEnabled()) {
            NotificationManager.add(new Notification(TextFormatting.GRAY + message, type));
            if (notifications.disableChat.getValue()) {
               return;
            }
         }

         mc.player.sendMessage(string1);
      }
   }

   public static void sendMessage(String message, Notification.Type type, String uniqueWord, int senderID, boolean notification) {
      if (notification) {
         sendClientDeleteMessage(message, type, uniqueWord, senderID);
      } else {
         sendDeleteMessage(message, uniqueWord, senderID);
      }
   }

   public static void sendClientDeleteMessage(String message, Notification.Type type, String uniqueWord, int senderID) {
      if (mc.world != null && mc.player != null) {
         setWatermark();
         Notifications notifications = ModuleManager.getModule(Notifications.class);
         if (notifications.isEnabled()) {
            NotificationManager.add(new Notification(TextFormatting.GRAY + message, type));
            if (notifications.disableChat.getValue()) {
               return;
            }
         }

         ChatUtil.sendDeleteMessage(watermark + messageFormatting + message, uniqueWord, senderID);
      }
   }

   public static void sendDeleteMessage(String message, String uniqueWord, int senderID) {
      if (mc.world != null && mc.player != null) {
         setWatermark();
         ChatUtil.sendDeleteMessage(watermark + messageFormatting + message, uniqueWord, senderID);
      }
   }

   public static void sendCommandMessage(String message, boolean prefix) {
      if (mc.world != null && mc.player != null) {
         setWatermark();
         String watermark1 = prefix ? watermark : "";
         ChatUtil.sendDeleteMessage(watermark1 + messageFormatting + message, "Command", 6);
      }
   }

   public static void sendMessage(String message, boolean prefix) {
      if (mc.world != null && mc.player != null) {
         setWatermark();
         String watermark1 = prefix ? watermark : "";
         TextComponentString string = new TextComponentString(watermark1 + messageFormatting + message);
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(string, getIdFromString(message));
      }
   }

   public static int getIdFromString(String name) {
      StringBuilder s = new StringBuilder();
      name = name.replace("禮", "e");
      String blacklist = "[^a-z]";

      for (int i = 0; i < name.length(); i++) {
         s.append(Integer.parseInt(String.valueOf(name.charAt(i)).replaceAll(blacklist, "e"), 36));
      }

      try {
         s = new StringBuilder(s.substring(0, 8));
      } catch (StringIndexOutOfBoundsException var4) {
         s = new StringBuilder(Integer.MAX_VALUE);
      }

      return Integer.MAX_VALUE - Integer.parseInt(s.toString().toLowerCase());
   }

   public static void sendClientRawMessage(String message) {
      if (mc.world != null && mc.player != null) {
         TextComponentString string = new TextComponentString(messageFormatting + message);
         mc.player.sendMessage(string);
      }
   }

   public static void sendServerMessage(String message) {
      if (mc.world != null && mc.player != null) {
         mc.player.connection.sendPacket(new CPacketChatMessage(message));
      }
   }

   public static void setWatermark() {
      watermark = ChatFormatting.GREEN + "[" + ChatFormatting.YELLOW + "Lemon" + ChatFormatting.GREEN + "] " + ChatFormatting.RESET;
   }
}
