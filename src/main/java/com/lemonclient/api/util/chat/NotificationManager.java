package com.lemonclient.api.util.chat;

import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.hud.Notifications;
import java.util.ArrayList;

public class NotificationManager {
   public static ArrayList<Notification> notifications = new ArrayList<>();

   public static void add(Notification notify) {
      Notifications notification = ModuleManager.getModule(Notifications.class);
      int max = notification.max.getValue();
      if (max != 0 && notifications.size() >= max) {
         String var3 = notification.mode.getValue();
         switch (var3) {
            case "Remove":
               notifications.remove(notifications.get(0));
               break;
            case "Cancel":
               return;
         }
      }

      notify.y = notifications.size() * 25;
      notifications.add(notify);
   }

   public static void draw() {
      if (!NotificationManager.notifications.isEmpty()) {
         Notification remove = null;

         for (Notification notify : NotificationManager.notifications) {
            if (notify.x == 0.0F) {
               notify.in = !notify.in;
            }

            if (Math.abs(notify.x - notify.width) < 0.1 && !notify.in) {
               remove = notify;
            }

            Notifications notifications = ModuleManager.getModule(Notifications.class);
            if (notify.in) {
               notify.x = notify.animationUtils.animate(0.0F, notify.x, notifications.xSpeed.getValue().floatValue());
            } else {
               notify.x = (float)notify.animationUtils.animate(notify.width, (double)notify.x, (double)notifications.xSpeed.getValue().floatValue());
            }

            notify.onRender();
         }

         if (remove != null) {
            NotificationManager.notifications.remove(remove);
         }
      }
   }
}
