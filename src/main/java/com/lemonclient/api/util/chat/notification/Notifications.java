package com.lemonclient.api.util.chat.notification;

import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.render.RenderUtil;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Notifications {
   public static String ICON_NOTIFY_INFO = "ℹ";
   public static String ICON_NOTIFY_SUCCESS = "✓";
   public static String ICON_NOTIFY_WARN = "⚠";
   public static String ICON_NOTIFY_ERROR = "⚠";
   public static String ICON_NOTIFY_DISABLED = "✗";
   public Timing timer = new Timing();
   public Notifications.Type t;
   public long stayTime;
   public String message;
   public double lastY;
   public double posY;
   public double width;
   public double height;
   public double animationX;
   public int color;

   public Notifications(String message, Notifications.Type type) {
      this.message = message;
      this.timer.reset();
      this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(message) + 35;
      this.height = 20.0;
      this.animationX = this.width;
      this.stayTime = 1000L;
      this.posY = -1.0;
      this.t = type;
      if (type.equals(Notifications.Type.INFO)) {
         this.color = -14342875;
      } else if (type.equals(Notifications.Type.ERROR)) {
         this.color = new Color(36, 36, 36).getRGB();
      } else if (type.equals(Notifications.Type.SUCCESS)) {
         this.color = new Color(36, 36, 36).getRGB();
      } else if (type.equals(Notifications.Type.DISABLE)) {
         this.color = new Color(36, 36, 36).getRGB();
      } else if (type.equals(Notifications.Type.WARNING)) {
         this.color = -14342875;
      }
   }

   public static int reAlpha(int color, float alpha) {
      Color c = new Color(color);
      float r = 0.003921569F * c.getRed();
      float g = 0.003921569F * c.getGreen();
      float b = 0.003921569F * c.getBlue();
      return new Color(r, g, b, alpha).getRGB();
   }

   public void draw(double getY, double lastY) {
      this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.message) + 25;
      this.height = 22.0;
      this.lastY = lastY;
      this.animationX = this.getAnimationState(this.animationX, this.isFinished() ? this.width : 0.0, 450.0);
      if (this.posY == -1.0) {
         this.posY = getY;
      } else {
         this.posY = this.getAnimationState(this.posY, getY, 350.0);
      }

      ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
      int x1 = (int)(res.getScaledWidth() - this.width + this.animationX / 2.0);
      int x2 = (int)(res.getScaledWidth() + this.animationX / 2.0);
      int y1 = (int)this.posY - 22;
      int y2 = (int)(y1 + this.height);
      RenderUtil.drawRect(x1, y1, x2, y2, reAlpha(this.color, 0.85F));
      RenderUtil.drawRect(
         x1,
         y2 - 1,
         (float)(x1 + Math.min((x2 - x1) * (System.currentTimeMillis() - this.timer.getPassedTimeMs()) / this.stayTime, (long)(x2 - x1))),
         y2,
         reAlpha(-1, 0.85F)
      );
      switch (this.t) {
         case ERROR:
            Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_ERROR, x1 + 5, y1 + 7, -65794);
            break;
         case INFO:
            Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_INFO, x1 + 5, y1 + 7, -65794);
            break;
         case SUCCESS:
            Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_SUCCESS, x1 + 5, y1 + 7, -65794);
            break;
         case WARNING:
            Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_WARN, x1 + 5, y1 + 7, -65794);
            break;
         case DISABLE:
            Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_DISABLED, x1 + 5, y1 + 7, -65794);
      }

      y1++;
      if (this.message.contains(" Enabled")) {
         int var10002 = x1 + 19;
         int var10003 = (int)(y1 + this.height / 4.0);
         Minecraft.getMinecraft().fontRenderer.drawString(this.message, var10002, var10003, -1);
         Minecraft.getMinecraft()
            .fontRenderer
            .drawString(" Enabled", x1 + 20 + Minecraft.getMinecraft().fontRenderer.getStringWidth(this.message), (int)(y1 + this.height / 4.0), -9868951);
      } else if (this.message.contains(" Disabled")) {
         int var11 = x1 + 19;
         int var13 = (int)(y1 + this.height / 4.0);
         Minecraft.getMinecraft().fontRenderer.drawString(this.message, var11, var13, -1);
         Minecraft.getMinecraft()
            .fontRenderer
            .drawString(" Disabled", x1 + 20 + Minecraft.getMinecraft().fontRenderer.getStringWidth(this.message), (int)(y1 + this.height / 4.0), -9868951);
      } else {
         int var12 = x1 + 20;
         int var14 = (int)(y1 + this.height / 4.0);
         Minecraft.getMinecraft().fontRenderer.drawString(this.message, var12, var14, -1);
      }
   }

   public boolean shouldDelete() {
      return this.isFinished() && this.animationX >= this.width;
   }

   public boolean isFinished() {
      return this.timer.passedMs(this.stayTime) && this.posY == this.lastY;
   }

   public double getHeight() {
      return this.height;
   }

   public double getAnimationState(double animation, double finalState, double speed) {
      float add = (float)(Minecraft.getMinecraft().timer.tickLength * speed * speed);
      if (animation < finalState) {
         if (animation + add < finalState) {
            animation += add;
         } else {
            animation = finalState;
         }
      } else if (animation - add > finalState) {
         animation -= add;
      } else {
         animation = finalState;
      }

      return animation;
   }

   public static enum Type {
      SUCCESS,
      INFO,
      WARNING,
      ERROR,
      DISABLE;
   }
}
