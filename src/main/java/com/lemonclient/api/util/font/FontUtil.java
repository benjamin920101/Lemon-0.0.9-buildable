package com.lemonclient.api.util.font;

import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.LemonClient;
import net.minecraft.client.Minecraft;

public class FontUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static float drawStringWithShadow(boolean customFont, String text, float x, float y, GSColor color) {
      return customFont
         ? LemonClient.INSTANCE.cFontRenderer.drawStringWithShadow(text, x, y, color)
         : mc.fontRenderer.drawStringWithShadow(text, x, y, color.getRGB());
   }

   public static float drawStringWithShadow(boolean customFont, String text, String mark, float x, float y, GSColor color) {
      mc.fontRenderer.drawStringWithShadow(mark, x, y, color.getRGB());
      return customFont
         ? LemonClient.INSTANCE.cFontRenderer.drawStringWithShadow(text, x + mc.fontRenderer.getStringWidth(mark), y, color)
         : mc.fontRenderer.drawStringWithShadow(text, x + mc.fontRenderer.getStringWidth(mark), y, color.getRGB());
   }

   public static int getStringWidth(boolean customFont, String string) {
      return customFont ? LemonClient.INSTANCE.cFontRenderer.getStringWidth(string) : mc.fontRenderer.getStringWidth(string);
   }

   public static int getFontHeight(boolean customFont) {
      return customFont ? LemonClient.INSTANCE.cFontRenderer.getHeight() : mc.fontRenderer.FONT_HEIGHT;
   }
}
