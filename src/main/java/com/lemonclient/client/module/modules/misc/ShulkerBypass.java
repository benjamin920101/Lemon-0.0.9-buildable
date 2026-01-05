package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.client.Minecraft;

@Module.Declaration(name = "Peek", category = Category.Misc)
public class ShulkerBypass extends Module {
   BooleanSetting notifications = this.registerBoolean("Notification", false);
   BooleanSetting shulker = this.registerBoolean("Shulker", true);
   IntegerSetting cmdDelay = this.registerInteger("Shulker Delay", 0, 0, 20);
   BooleanSetting map = this.registerBoolean("Map", true);
   BooleanSetting book = this.registerBoolean("Book", true);
   public static boolean notification;
   public static boolean shulkers;
   public static boolean maps;
   public static boolean books;
   public static int delay;

   @Override
   public void onEnable() {
      if (Minecraft.getMinecraft().player != null) {
         MessageBus.sendMessage("[ShulkerBypass] To use this throw a shulker on the ground", Notification.Type.INFO, "Peek", 3, this.notifications.getValue());
      }
   }

   @Override
   public void onUpdate() {
      notification = this.notifications.getValue();
      delay = this.cmdDelay.getValue();
      shulkers = this.shulker.getValue();
      maps = this.map.getValue();
      books = this.book.getValue();
   }
}
