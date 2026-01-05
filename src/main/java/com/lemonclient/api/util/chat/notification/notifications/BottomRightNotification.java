package com.lemonclient.api.util.chat.notification.notifications;

import com.lemonclient.api.util.chat.notification.Notification;
import com.lemonclient.api.util.chat.notification.NotificationType;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.awt.Color;
import net.minecraft.client.gui.Gui;

public class BottomRightNotification extends Notification {
   public BottomRightNotification(NotificationType type, String title, String message, int length) {
      super(type, title, message, length);
   }

   @Override
   public void render(int RealDisplayWidth, int RealDisplayHeight) {
      boolean customFont = ColorMain.INSTANCE.customFont.getValue();
      int width = Math.max(FontUtil.getStringWidth(customFont, this.title), FontUtil.getStringWidth(customFont, this.message)) + 20;
      int height = FontUtil.getFontHeight(customFont) * 2 + 10;
      int offset = this.getOffset(width);
      Color color = new Color(0, 0, 0, 220);
      Color color2;
      if (this.type == NotificationType.INFO) {
         color2 = new Color(25, 60, 180);
      } else if (this.type == NotificationType.WARNING) {
         color2 = new Color(204, 193, 0);
      } else if (this.type == NotificationType.WELCOME) {
         color2 = new Color(255, 255, 75);
      } else if (this.type == NotificationType.LOAD) {
         color2 = new Color(255, 255, 150);
      } else {
         color2 = new Color(204, 0, 18);
         int i = Math.max(0, Math.min(255, (int)(Math.sin(this.getTime() / 100.0) * 255.0 / 2.0 + 127.5)));
         color = new Color(i, 0, 0, 220);
      }

      Gui.drawRect(RealDisplayWidth - offset, RealDisplayHeight - 5 - height, RealDisplayWidth, RealDisplayHeight - 5, color.getRGB());
      Gui.drawRect(RealDisplayWidth - offset, RealDisplayHeight - 5 - height, RealDisplayWidth - offset + 4, RealDisplayHeight - 5, color2.getRGB());
      FontUtil.drawStringWithShadow(customFont, this.title, RealDisplayWidth - offset + 8, RealDisplayHeight - 2 - height, new GSColor(255, 255, 255));
      FontUtil.drawStringWithShadow(customFont, this.message, RealDisplayWidth - offset + 8, RealDisplayHeight - 15, new GSColor(255, 255, 255));
   }
}
