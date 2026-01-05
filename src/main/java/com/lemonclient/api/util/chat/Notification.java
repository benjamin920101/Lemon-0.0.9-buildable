package com.lemonclient.api.util.chat;

import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.misc.Wrapper;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lemonclient.client.module.modules.hud.Notifications;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;

public class Notification {
   public String text;
   public double width;
   public double height = 30.0;
   public float x;
   public String mark;
   Notification.Type type;
   public float y;
   public float position;
   public boolean in = true;
   AnimationUtil animationUtils = new AnimationUtil();
   AnimationUtil yAnimationUtils = new AnimationUtil();
   public static String ICON_NOTIFY_INFO = "ℹ";
   public static String ICON_NOTIFY_SUCCESS = "✓";
   public static String ICON_NOTIFY_WARN = "⚠";
   public static String ICON_NOTIFY_ERROR = "⚠";
   public static String ICON_NOTIFY_DISABLED = "✗";

   public Notification(String text, Notification.Type type) {
      String mark = "";
      this.type = type;
      if (ModuleManager.getModule(Notifications.class).mark.getValue()) {
         switch (this.type) {
            case ERROR:
               mark = TextFormatting.DARK_RED + ICON_NOTIFY_ERROR + " ";
               break;
            case INFO:
               mark = TextFormatting.YELLOW + ICON_NOTIFY_INFO + " ";
               break;
            case SUCCESS:
               mark = TextFormatting.GREEN + ICON_NOTIFY_SUCCESS + " ";
               break;
            case WARNING:
               mark = TextFormatting.RED + ICON_NOTIFY_WARN + " ";
               break;
            case DISABLE:
               mark = TextFormatting.RED + ICON_NOTIFY_DISABLED + " ";
         }
      }

      this.text = text;
      this.mark = mark;
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      this.width = FontUtil.getStringWidth(colorMain.customFont.getValue(), this.text) + 25;
      this.x = (float)this.width;
   }

   public void onRender() {
      int i = 0;

      for (Notification notification : NotificationManager.notifications) {
         if (notification == this) {
            break;
         }

         i++;
      }

      Notifications notification = ModuleManager.getModule(Notifications.class);
      this.y = this.yAnimationUtils.animate((float)(i * (this.height + 5.0)), this.y, notification.ySpeed.getValue().floatValue());
      ScaledResolution sr = new ScaledResolution(Wrapper.getMinecraft());
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      int color = this.getColor(notification.backGround.getValue());
      Color outlineColor = this.getOutColor(notification.backGround.getValue());
      switch (this.type) {
         case ERROR:
            color = this.getColor(notification.errorBackGround.getValue());
            outlineColor = this.getOutColor(notification.errorBackGround.getValue());
         case INFO:
         default:
            break;
         case SUCCESS:
            color = this.getColor(notification.successBackGround.getValue());
            outlineColor = this.getOutColor(notification.successBackGround.getValue());
            break;
         case WARNING:
            color = this.getColor(notification.warningBackGround.getValue());
            outlineColor = this.getOutColor(notification.warningBackGround.getValue());
            break;
         case DISABLE:
            color = this.getColor(notification.disableBackGround.getValue());
            outlineColor = this.getOutColor(notification.disableBackGround.getValue());
      }

      RenderUtil.drawRectS(
         sr.getScaledWidth() + this.x - this.width,
         sr.getScaledHeight() - 50 - this.y - this.height,
         sr.getScaledWidth() + this.x,
         sr.getScaledHeight() - 50 - this.y,
         color
      );
      if (notification.outline.getValue()) {
         RenderUtil.drawRectSOutline(
            sr.getScaledWidth() + this.x - this.width,
            sr.getScaledHeight() - 50 - this.y - this.height,
            sr.getScaledWidth() + this.x,
            sr.getScaledHeight() - 50 - this.y,
            outlineColor
         );
      }

      FontUtil.drawStringWithShadow(
         colorMain.customFont.getValue(),
         this.text,
         this.mark,
         (int)(sr.getScaledWidth() + this.x - this.width + 10.0),
         (int)(sr.getScaledHeight() - 50.0F - this.y - 18.0F),
         new GSColor(204, 204, 204, 232)
      );
   }

   private int getColor(GSColor color) {
      Notifications notifications = ModuleManager.getModule(Notifications.class);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), notifications.alpha.getValue()).getRGB();
   }

   private Color getOutColor(GSColor color) {
      Notifications notifications = ModuleManager.getModule(Notifications.class);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), notifications.outlineAlpha.getValue());
   }

   public static enum Type {
      SUCCESS,
      INFO,
      WARNING,
      ERROR,
      DISABLE;
   }
}
