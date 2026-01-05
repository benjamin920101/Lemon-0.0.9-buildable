package com.lemonclient.api.util.chat.notification;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationsManager {
   public static final LinkedBlockingQueue<Notification> pendingNotifications = new LinkedBlockingQueue<>();
   private static Notification currentNotification = null;
   public static ArrayList<Notifications> notifications = new ArrayList<>();

   public static void show(Notification notification) {
      pendingNotifications.add(notification);
   }

   public static void show(Notifications notification) {
      notifications.add(notification);
   }

   public static void update() {
      if (currentNotification != null && !currentNotification.isShown()) {
         currentNotification = null;
      }

      if (currentNotification == null && !pendingNotifications.isEmpty()) {
         (currentNotification = pendingNotifications.poll()).show();
      }
   }

   public static void render() {
      try {
         int divider = Minecraft.getMinecraft().gameSettings.guiScale;
         int width = Minecraft.getMinecraft().displayWidth / divider;
         int height = Minecraft.getMinecraft().displayHeight / divider;
         update();
         if (currentNotification != null) {
            currentNotification.render(width, height);
         }
      } catch (Exception var3) {
      }
   }

   public static void drawNotifications() {
      try {
         ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
         double lastY;
         double startY = lastY = res.getScaledHeight() - 25;

         for (int i = 0; i < notifications.size(); i++) {
            Notifications not = notifications.get(i);
            if (not.shouldDelete()) {
               notifications.remove(not);

               for (int cao = 0; cao > not.width; cao--) {
                  not.animationX = cao - not.width;
               }

               startY += not.getHeight() + 3.0;
            }

            not.draw(startY, lastY);

            for (int number = 0; number < not.width; number++) {
               not.animationX = number + not.width;
            }

            startY -= not.getHeight() + 2.0;
         }
      } catch (Throwable var8) {
      }
   }
}
