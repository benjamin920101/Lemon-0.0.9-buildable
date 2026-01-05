package com.lemonclient.api.util.chat.notification.notifications;

import com.lemonclient.api.util.chat.notification.Notification;
import com.lemonclient.api.util.chat.notification.NotificationType;
import com.lemonclient.api.util.font.CFontRenderer;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.LemonClient;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL11;

public class TopNotification extends Notification {
   public Map<Integer, Integer> offsetDirLeft = new HashMap<>();
   public Map<Integer, Integer> offsetDirRight = new HashMap<>();
   public int arrAmount = 0;
   public boolean shouldAdd = true;
   public int delaydir = 0;

   public TopNotification(NotificationType type, String title, String message, int length) {
      super(type, title, message, length);
   }

   @Override
   public void render(int RealDisplayWidth, int RealDisplayHeight) {
      CFontRenderer font = LemonClient.INSTANCE.cFontRenderer;
      this.delaydir++;
      int height = font.getHeight() * 4;
      int width = RealDisplayWidth / 4;
      int offset = this.getOffset(width);
      Color color = this.type == NotificationType.INFO ? Color.BLACK : this.getDefaultTypeColor();
      boolean shouldEffect = offset >= width - 5;
      int cx = RealDisplayWidth / 2;
      int cy = RealDisplayHeight / 8;
      int x = cx - offset;
      int dWidth = offset * 2;
      if (shouldEffect) {
         if (this.shouldAdd) {
            this.offsetDirLeft.put(this.arrAmount, -16 - 10 * this.arrAmount);
            this.offsetDirRight.put(this.arrAmount, -16 - 10 * this.arrAmount);
            this.arrAmount++;
            if (this.arrAmount >= 3) {
               this.arrAmount = 0;
               this.shouldAdd = false;
            }
         }

         GL11.glLineWidth(2.0F);

         for (Entry<Integer, Integer> offsetdir : this.offsetDirLeft.entrySet()) {
            int value = offsetdir.getValue();
            int alpha = calculateAlphaChangeColor(255, 10, 50, value);
            RenderUtil.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            this.drawC(x - value, cy, height, height);
         }

         for (Entry<Integer, Integer> offsetdir : this.offsetDirRight.entrySet()) {
            int value = offsetdir.getValue();
            int alpha = calculateAlphaChangeColor(255, 10, 50, value);
            RenderUtil.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            this.drawBC(x + dWidth + value, cy, height, height);
         }
      }

      int delay = 3;
      if (this.delaydir >= 3) {
         this.delaydir = 0;

         for (Entry<Integer, Integer> offsetdir2 : this.offsetDirLeft.entrySet()) {
            if (offsetdir2.getValue() >= 50) {
               offsetdir2.setValue(-16);
            } else {
               offsetdir2.setValue(offsetdir2.getValue() + 1);
            }
         }

         for (Entry<Integer, Integer> offsetdir2x : this.offsetDirRight.entrySet()) {
            if (offsetdir2x.getValue() >= 50) {
               offsetdir2x.setValue(-16);
            } else {
               offsetdir2x.setValue(offsetdir2x.getValue() + 1);
            }
         }
      }

      RenderUtil.drawRect(x, cy, dWidth, height, color);
      RenderUtil.drawTriangle(x, cy, x - 15 - 1, cy + height / 2.0, x, cy + height, color);
      RenderUtil.drawTriangle(x + dWidth, cy + height, x + 15 + dWidth, cy + height / 2.0, x + dWidth, cy, color);
      int fx = x + dWidth / 2;
      int alpha2 = calculateAlphaChangeColor(10, 255, width, offset);
      font.drawString(this.title, fx - font.getStringWidth(this.title) / 2.0F, cy + 3, new GSColor(255, 255, 255, alpha2));
      font.drawString(this.message, fx - font.getStringWidth(this.message) / 2.0F, cy + font.getHeight() + 8, new GSColor(255, 255, 255, alpha2));
      if (!this.shouldAdd && !shouldEffect) {
         this.offsetDirLeft.remove(this.arrAmount);
         this.offsetDirRight.remove(this.arrAmount);
         this.arrAmount++;
         if (this.arrAmount >= 3) {
            this.arrAmount = 0;
            this.shouldAdd = true;
         }
      }
   }

   public static Integer calculateAlphaChangeColor(int oldAlpha, int newAlpha, int step, int currentStep) {
      return Math.max(0, Math.min(255, oldAlpha + (newAlpha - oldAlpha) * Math.max(0, Math.min(step, currentStep)) / step));
   }

   public void drawBC(int cx, int cy, int height, int margin) {
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBegin(3);
      GL11.glVertex2d(cx, cy);
      GL11.glVertex2d(cx + margin, cy + height / 2.0);
      GL11.glVertex2d(cx, cy + height);
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
   }

   public void drawC(int cx, int cy, int height, int margin) {
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBegin(3);
      GL11.glVertex2d(cx, cy);
      GL11.glVertex2d(cx - margin, cy + height / 2.0);
      GL11.glVertex2d(cx, cy + height);
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
   }
}
