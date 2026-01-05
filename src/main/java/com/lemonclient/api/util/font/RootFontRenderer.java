package com.lemonclient.api.util.font;

import com.lemonclient.api.util.render.FontRenderer;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class RootFontRenderer implements FontRenderer {
   private final float fontsize;
   private final net.minecraft.client.gui.FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

   public RootFontRenderer(float fontsize) {
      this.fontsize = fontsize;
   }

   @Override
   public int getFontHeight() {
      return (int)(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * this.fontsize);
   }

   @Override
   public int getStringHeight(String text) {
      return this.getFontHeight();
   }

   @Override
   public int getStringWidth(String text) {
      return (int)(this.fontRenderer.getStringWidth(text) * this.fontsize);
   }

   @Override
   public void drawString(int x, int y, String text) {
      this.drawString(x, y, 255, 255, 255, text);
   }

   @Override
   public void drawString(int x, int y, int r, int g, int b, String text) {
      this.drawString(x, y, 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF, text);
   }

   @Override
   public void drawString(int x, int y, Color color, String text) {
      this.drawString(x, y, color.getRGB(), text);
   }

   @Override
   public void drawString(int x, int y, int colour, String text) {
      this.drawString(x, y, colour, text, true);
   }

   public void drawString(int x, int y, int colour, String text, boolean shadow) {
      this.prepare(x, y);
      Minecraft.getMinecraft().fontRenderer.drawString(text, 0.0F, 0.0F, colour, shadow);
      this.pop(x, y);
   }

   @Override
   public void drawStringWithShadow(int x, int y, int r, int g, int b, String text) {
      this.drawString(x, y, 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF, text, true);
   }

   private void prepare(int x, int y) {
      GL11.glEnable(3553);
      GL11.glEnable(3042);
      GL11.glTranslatef(x, y, 0.0F);
      GL11.glScalef(this.fontsize, this.fontsize, 1.0F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void pop(int x, int y) {
      GL11.glScalef(1.0F / this.fontsize, 1.0F / this.fontsize, 1.0F);
      GL11.glTranslatef(-x, -y, 0.0F);
      GL11.glDisable(3553);
   }
}
