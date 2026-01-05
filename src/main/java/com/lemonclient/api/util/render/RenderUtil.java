package com.lemonclient.api.util.render;

import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lemonclient.client.module.modules.render.Nametags;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

public class RenderUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, GSColor color) {
      drawLine(posx, posy, posz, posx2, posy2, posz2, color, 1.0F);
   }

   public static void drawRectOutline(double x, double y, double width, double height, Color color) {
      drawGradientRectOutline(x, y, width, height, RenderUtil.GradientDirection.Normal, color, color);
   }

   public static void drawGradientRectOutline(
      double x, double y, double width, double height, RenderUtil.GradientDirection direction, Color startColor, Color endColor
   ) {
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      Color[] result = checkColorDirection(direction, startColor, endColor);
      GL11.glBegin(2);
      GL11.glColor4f(result[2].getRed() / 255.0F, result[2].getGreen() / 255.0F, result[2].getBlue() / 255.0F, result[2].getAlpha() / 255.0F);
      GL11.glVertex2d(x + width, y);
      GL11.glColor4f(result[3].getRed() / 255.0F, result[3].getGreen() / 255.0F, result[3].getBlue() / 255.0F, result[3].getAlpha() / 255.0F);
      GL11.glVertex2d(x, y);
      GL11.glColor4f(result[0].getRed() / 255.0F, result[0].getGreen() / 255.0F, result[0].getBlue() / 255.0F, result[0].getAlpha() / 255.0F);
      GL11.glVertex2d(x, y + height);
      GL11.glColor4f(result[1].getRed() / 255.0F, result[1].getGreen() / 255.0F, result[1].getBlue() / 255.0F, result[1].getAlpha() / 255.0F);
      GL11.glVertex2d(x + width, y + height);
      GL11.glEnd();
      GL11.glDisable(3042);
      GL11.glEnable(3553);
   }

   public static void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL11.glBegin(6);
      GL11.glVertex2d(x1, y1);
      GL11.glVertex2d(x2, y2);
      GL11.glVertex2d(x3, y3);
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
   }

   public static void drawRect(double x, double y, double width, double height, Color color) {
      drawGradientRect(x, y, width, height, RenderUtil.GradientDirection.Normal, color, color);
   }

   public static void setColor(Color color) {
      GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
   }

   private static Color[] checkColorDirection(RenderUtil.GradientDirection direction, Color start, Color end) {
      Color[] dir = new Color[4];
      if (direction == RenderUtil.GradientDirection.Normal) {
         for (int a = 0; a < dir.length; a++) {
            dir[a] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         }
      } else if (direction == RenderUtil.GradientDirection.DownToUp) {
         dir[0] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[1] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[2] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[3] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
      } else if (direction == RenderUtil.GradientDirection.UpToDown) {
         dir[0] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[1] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[2] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[3] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
      } else if (direction == RenderUtil.GradientDirection.RightToLeft) {
         dir[0] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[1] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[2] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[3] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
      } else if (direction == RenderUtil.GradientDirection.LeftToRight) {
         dir[0] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
         dir[1] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[2] = new Color(start.getRed(), start.getGreen(), start.getBlue(), start.getAlpha());
         dir[3] = new Color(end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha());
      } else {
         for (int a = 0; a < dir.length; a++) {
            dir[a] = new Color(255, 255, 255);
         }
      }

      return dir;
   }

   public static void drawGradientRect(
      double x, double y, double width, double height, RenderUtil.GradientDirection direction, Color startColor, Color endColor
   ) {
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      Color[] result = checkColorDirection(direction, startColor, endColor);
      GL11.glBegin(7);
      setColor(result[0]);
      GL11.glVertex2d(x + width, y);
      setColor(result[1]);
      GL11.glVertex2d(x, y);
      setColor(result[2]);
      GL11.glVertex2d(x, y + height);
      setColor(result[3]);
      GL11.glVertex2d(x + width, y + height);
      GL11.glEnd();
      GL11.glDisable(3042);
      GL11.glEnable(3553);
   }

   public static void drawRect(float x1, float y1, float x2, float y2, int color) {
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glPushMatrix();
      color(color);
      GL11.glBegin(7);
      GL11.glVertex2d(x2, y1);
      GL11.glVertex2d(x1, y1);
      GL11.glVertex2d(x1, y2);
      GL11.glVertex2d(x2, y2);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glDisable(2848);
      GL11.glPopMatrix();
      Gui.drawRect(0, 0, 0, 0, 0);
   }

   public static void drawRectSOutline(double x, double y, double x2, double y2, Color color) {
      drawGradientRectSOutline(x, y, x2, y2, RenderUtil.GradientDirection.Normal, color, color);
   }

   public static void drawGradientRectSOutline(
      double x, double y, double x2, double y2, RenderUtil.GradientDirection direction, Color startColor, Color endColor
   ) {
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      Color[] result = checkColorDirection(direction, startColor, endColor);
      GL11.glBegin(2);
      GL11.glColor4f(result[2].getRed() / 255.0F, result[2].getGreen() / 255.0F, result[2].getBlue() / 255.0F, result[2].getAlpha() / 255.0F);
      GL11.glVertex2d(x2, y);
      GL11.glColor4f(result[3].getRed() / 255.0F, result[3].getGreen() / 255.0F, result[3].getBlue() / 255.0F, result[3].getAlpha() / 255.0F);
      GL11.glVertex2d(x, y);
      GL11.glColor4f(result[0].getRed() / 255.0F, result[0].getGreen() / 255.0F, result[0].getBlue() / 255.0F, result[0].getAlpha() / 255.0F);
      GL11.glVertex2d(x, y2);
      GL11.glColor4f(result[1].getRed() / 255.0F, result[1].getGreen() / 255.0F, result[1].getBlue() / 255.0F, result[1].getAlpha() / 255.0F);
      GL11.glVertex2d(x2, y2);
      GL11.glEnd();
      GL11.glDisable(3042);
      GL11.glEnable(3553);
   }

   public static void drawRectS(double x1, double y1, float x2, float y2, int color) {
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glPushMatrix();
      color(color);
      GL11.glBegin(7);
      GL11.glVertex2d(x2, y1);
      GL11.glVertex2d(x1, y1);
      GL11.glVertex2d(x1, y2);
      GL11.glVertex2d(x2, y2);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glDisable(2848);
      GL11.glPopMatrix();
      Gui.drawRect(0, 0, 0, 0, 0);
   }

   public static void color(int color) {
      float f = (color >> 24 & 0xFF) / 255.0F;
      float f1 = (color >> 16 & 0xFF) / 255.0F;
      float f2 = (color >> 8 & 0xFF) / 255.0F;
      float f3 = (color & 0xFF) / 255.0F;
      GL11.glColor4f(f1, f2, f3, f);
   }

   public static void prepareGL() {
      GL11.glBlendFunc(770, 771);
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.glLineWidth(Float.intBitsToFloat(Float.floatToIntBits(5.0675106F) ^ 2132945164));
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.enableAlpha();
      GlStateManager.color(
         Float.intBitsToFloat(Float.floatToIntBits(11.925059F) ^ 2126433547),
         Float.intBitsToFloat(Float.floatToIntBits(18.2283F) ^ 2115097487),
         Float.intBitsToFloat(Float.floatToIntBits(9.73656F) ^ 2124138739)
      );
   }

   public static void releaseGL() {
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.enableDepth();
      GlStateManager.color(
         Float.intBitsToFloat(Float.floatToIntBits(12.552789F) ^ 2127091769),
         Float.intBitsToFloat(Float.floatToIntBits(7.122752F) ^ 2137255318),
         Float.intBitsToFloat(Float.floatToIntBits(5.4278784F) ^ 2133700910)
      );
      GL11.glColor4f(
         Float.intBitsToFloat(Float.floatToIntBits(10.5715685F) ^ 2125014309),
         Float.intBitsToFloat(Float.floatToIntBits(4.9474883F) ^ 2132693459),
         Float.intBitsToFloat(Float.floatToIntBits(4.9044757F) ^ 2132603255),
         Float.intBitsToFloat(Float.floatToIntBits(9.482457F) ^ 2123872293)
      );
   }

   public static void draw2DGradientRect(
      float left, float top, float right, float bottom, int leftBottomColor, int leftTopColor, int rightBottomColor, int rightTopColor
   ) {
      float lba = (leftBottomColor >> 24 & 0xFF) / 255.0F;
      float lbr = (leftBottomColor >> 16 & 0xFF) / 255.0F;
      float lbg = (leftBottomColor >> 8 & 0xFF) / 255.0F;
      float lbb = (leftBottomColor & 0xFF) / 255.0F;
      float rba = (rightBottomColor >> 24 & 0xFF) / 255.0F;
      float rbr = (rightBottomColor >> 16 & 0xFF) / 255.0F;
      float rbg = (rightBottomColor >> 8 & 0xFF) / 255.0F;
      float rbb = (rightBottomColor & 0xFF) / 255.0F;
      float lta = (leftTopColor >> 24 & 0xFF) / 255.0F;
      float ltr = (leftTopColor >> 16 & 0xFF) / 255.0F;
      float ltg = (leftTopColor >> 8 & 0xFF) / 255.0F;
      float ltb = (leftTopColor & 0xFF) / 255.0F;
      float rta = (rightTopColor >> 24 & 0xFF) / 255.0F;
      float rtr = (rightTopColor >> 16 & 0xFF) / 255.0F;
      float rtg = (rightTopColor >> 8 & 0xFF) / 255.0F;
      float rtb = (rightTopColor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(right, top, 0.0).color(rtr, rtg, rtb, rta).endVertex();
      bufferbuilder.pos(left, top, 0.0).color(ltr, ltg, ltb, lta).endVertex();
      bufferbuilder.pos(left, bottom, 0.0).color(lbr, lbg, lbb, lba).endVertex();
      bufferbuilder.pos(right, bottom, 0.0).color(rbr, rbg, rbb, rba).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, GSColor color, float width) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth(width);
      color.glColor();
      bufferbuilder.begin(1, DefaultVertexFormats.POSITION);
      vertex(posx, posy, posz, bufferbuilder);
      vertex(posx2, posy2, posz2, bufferbuilder);
      tessellator.draw();
   }

   public static void draw2DRect(int posX, int posY, int width, int height, int zHeight, GSColor color) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      color.glColor();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(posX, posY + height, zHeight).endVertex();
      bufferbuilder.pos(posX + width, posY + height, zHeight).endVertex();
      bufferbuilder.pos(posX + width, posY, zHeight).endVertex();
      bufferbuilder.pos(posX, posY, zHeight).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   private static void drawBorderedRect(double x, double y, double x1, GSColor inside, GSColor border) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      inside.glColor();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(x, 1.0, 0.0).endVertex();
      bufferbuilder.pos(x1, 1.0, 0.0).endVertex();
      bufferbuilder.pos(x1, y, 0.0).endVertex();
      bufferbuilder.pos(x, y, 0.0).endVertex();
      tessellator.draw();
      border.glColor();
      GlStateManager.glLineWidth(1.8F);
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(x, y, 0.0).endVertex();
      bufferbuilder.pos(x, 1.0, 0.0).endVertex();
      bufferbuilder.pos(x1, 1.0, 0.0).endVertex();
      bufferbuilder.pos(x1, y, 0.0).endVertex();
      bufferbuilder.pos(x, y, 0.0).endVertex();
      tessellator.draw();
   }

   public static void drawCircle(float x, float y, float z, Double radius, GSColor colour) {
      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      int alpha = 255 - colour.getAlpha();
      if (alpha == 0) {
         alpha = 1;
      }

      for (int i = 0; i < 361; i++) {
         bufferbuilder.pos(
               x + Math.sin(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosX,
               y - mc.getRenderManager().viewerPosY,
               z + Math.cos(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosZ
            )
            .color(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, alpha)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.enableCull();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawCircle(float x, float y, float z, Double radius, int stepCircle, int alphaVal) {
      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      int alpha = 255 - alphaVal;
      if (alpha == 0) {
         alpha = 1;
      }

      for (int i = 0; i < 361; i++) {
         GSColor colour = ColorSetting.getRainbowColor(i % 180 * stepCircle);
         bufferbuilder.pos(
               x + Math.sin(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosX,
               y - mc.getRenderManager().viewerPosY,
               z + Math.cos(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosZ
            )
            .color(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, alpha)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.enableCull();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawBox(BlockPos blockPos, double height, GSColor color, int sides) {
      drawBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0, height, 1.0, color, color.getAlpha(), sides);
   }

   public static void drawBox(AxisAlignedBB bb, boolean check, double height, GSColor color, int sides) {
      drawBox(bb, check, height, color, color.getAlpha(), sides);
   }

   public static void drawBox(AxisAlignedBB bb, boolean check, double height, GSColor color, int alpha, int sides) {
      if (check) {
         drawBox(
            bb.minX,
            bb.minY,
            bb.minZ,
            bb.maxX - bb.minX,
            bb.maxY - bb.minY,
            bb.maxZ - bb.minZ,
            color,
            alpha,
            sides
         );
      } else {
         drawBox(
            bb.minX,
            bb.minY,
            bb.minZ,
            bb.maxX - bb.minX,
            height,
            bb.maxZ - bb.minZ,
            color,
            alpha,
            sides
         );
      }
   }

   public static void drawBox(double x, double y, double z, double w, double h, double d, GSColor color, int alpha, int sides) {
      GlStateManager.disableAlpha();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      color.glColor();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      doVerticies(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, alpha, bufferbuilder, sides, false);
      tessellator.draw();
      GlStateManager.enableAlpha();
   }

   public static void drawBoxDire(AxisAlignedBB bb, double height, GSColor color, int alpha, int sides) {
      drawBoxDire(
         bb.minX,
         bb.minY,
         bb.minZ,
         bb.maxX - bb.minX,
         height,
         bb.maxZ - bb.minZ,
         color,
         alpha,
         sides
      );
      drawFixBoxDire(
         bb.minX,
         bb.minY,
         bb.minZ,
         bb.maxX - bb.minX,
         height,
         bb.maxZ - bb.minZ,
         color,
         alpha,
         sides
      );
   }

   public static void drawBoxDire(double x, double y, double z, double w, double h, double d, GSColor color, int alpha, int sides) {
      GlStateManager.disableAlpha();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      color.glColor();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      doVerticies(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, alpha, bufferbuilder, sides);
      tessellator.draw();
      GlStateManager.enableAlpha();
   }

   public static void drawFixBoxDire(double x, double y, double z, double w, double h, double d, GSColor color, int alpha, int sides) {
      GlStateManager.disableAlpha();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      color.glColor();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      doFixVerticies(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, alpha, bufferbuilder, sides);
      tessellator.draw();
      GlStateManager.enableAlpha();
   }

   public static void drawBoundingBoxDire(BlockPos pos, double height, double width, GSColor color, int alpha, int sides) {
      drawBoundingBoxDire(new AxisAlignedBB(pos), height, width, color, alpha, sides);
   }

   public static void drawBoundingBoxDire(AxisAlignedBB bb, double height, double width, GSColor color, int alpha, int sides) {
      drawBoundingBoxDire(
         bb.minX,
         bb.minY,
         bb.minZ,
         bb.maxX - bb.minX,
         height,
         bb.maxZ - bb.minZ,
         width,
         color,
         alpha,
         sides
      );
   }

   public static void drawBoundingBoxDire(double x, double y, double z, double w, double h, double d, double width, GSColor color, int alpha, int sides) {
      GlStateManager.disableAlpha();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth((float)width);
      color.glColor();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + w, y + h, z + d);
      if ((sides & 32) != 0) {
         colorVertex(bb.minX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      }

      if ((sides & 16) != 0) {
         colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 4) != 0) {
         colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 8) != 0) {
         colorVertex(bb.minX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.minY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
         colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      }

      tessellator.draw();
      GlStateManager.enableAlpha();
   }

   public static void drawBoundingBox(AxisAlignedBB bb, double width, GSColor[] otherPos) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth((float)width);
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);
      tessellator.draw();
   }

   public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, double width, GSColor[] color, boolean five, int sides) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth((float)width);
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      if ((sides & 32) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 16) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 4) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 8) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 2) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[6], color[6].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[4], color[4].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 1) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         }
      }

      tessellator.draw();
   }

   public static void drawBoundingBox(BlockPos bp, double height, float width, GSColor color) {
      drawBoundingBox(getBoundingBox(bp, height), width, color, color.getAlpha());
   }

   public static void drawBoundingBox(AxisAlignedBB bb, double width, GSColor color) {
      drawBoundingBox(bb, width, color, color.getAlpha());
   }

   public static void drawBoundingBox(AxisAlignedBB bb, double width, GSColor color, int alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth((float)width);
      color.glColor();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
      colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
      colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
      tessellator.draw();
   }

   public static void drawBoundingBoxWithSides(BlockPos blockPos, double high, int width, GSColor color, int sides) {
      drawBoundingBoxWithSides(getBoundingBox(blockPos, high), width, color, color.getAlpha(), sides);
   }

   public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, GSColor color, int sides) {
      drawBoundingBoxWithSides(getBoundingBox(blockPos, 1.0), width, color, color.getAlpha(), sides);
   }

   public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, GSColor color, int alpha, int sides) {
      drawBoundingBoxWithSides(getBoundingBox(blockPos, 1.0), width, color, alpha, sides);
   }

   public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, GSColor color, int sides) {
      drawBoundingBoxWithSides(axisAlignedBB, width, color, color.getAlpha(), sides);
   }

   public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, GSColor color, int alpha, int sides) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.glLineWidth(width);
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      doVerticies(axisAlignedBB, color, alpha, bufferbuilder, sides, true);
      tessellator.draw();
   }

   public static void drawBoxProva2(AxisAlignedBB bb, GSColor[] color, int sides) {
      drawBoxProva(
         bb.minX,
         bb.minY,
         bb.minZ,
         bb.maxX - bb.minX,
         bb.maxY - bb.minY,
         bb.maxZ - bb.minZ,
         color,
         sides
      );
   }

   public static void drawBoxProva(double x, double y, double z, double w, double h, double d, GSColor[] color, int sides) {
      GlStateManager.disableAlpha();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      doVerticiesProva(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, bufferbuilder, sides);
      tessellator.draw();
      GlStateManager.enableAlpha();
   }

   private static void doVerticiesProva(AxisAlignedBB axisAlignedBB, GSColor[] color, BufferBuilder bufferbuilder, int sides) {
      if ((sides & 32) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
      }

      if ((sides & 16) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
      }

      if ((sides & 4) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
      }

      if ((sides & 8) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
      }

      if ((sides & 2) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[6], color[6].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[4], color[4].getAlpha(), bufferbuilder);
      }

      if ((sides & 1) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
      }
   }

   public static void drawBoxWithDirection(AxisAlignedBB bb, GSColor color, float rotation, float width, int mode) {
      double xCenter = bb.minX + (bb.maxX - bb.minX) / 2.0;
      double zCenter = bb.minZ + (bb.maxZ - bb.minZ) / 2.0;
      RenderUtil.Points square = new RenderUtil.Points(bb.minY, bb.maxY, xCenter, zCenter, rotation);
      if (mode == 0) {
         square.addPoints(bb.minX, bb.minZ);
         square.addPoints(bb.minX, bb.maxZ);
         square.addPoints(bb.maxX, bb.maxZ);
         square.addPoints(bb.maxX, bb.minZ);
      }

      if (mode == 0) {
         drawDirection(square, color, width);
      }
   }

   public static void drawDirection(RenderUtil.Points square, GSColor color, float width) {
      for (int i = 0; i < 4; i++) {
         drawLine(
            square.getPoint(i)[0],
            square.yMin,
            square.getPoint(i)[1],
            square.getPoint((i + 1) % 4)[0],
            square.yMin,
            square.getPoint((i + 1) % 4)[1],
            color,
            width
         );
      }

      for (int i = 0; i < 4; i++) {
         drawLine(
            square.getPoint(i)[0],
            square.yMax,
            square.getPoint(i)[1],
            square.getPoint((i + 1) % 4)[0],
            square.yMax,
            square.getPoint((i + 1) % 4)[1],
            color,
            width
         );
      }

      for (int i = 0; i < 4; i++) {
         drawLine(square.getPoint(i)[0], square.yMin, square.getPoint(i)[1], square.getPoint(i)[0], square.yMax, square.getPoint(i)[1], color, width);
      }
   }

   public static void drawSphere(double x, double y, double z, float size, int slices, int stacks, float lineWidth, GSColor color) {
      Sphere sphere = new Sphere();
      GlStateManager.glLineWidth(lineWidth);
      color.glColor();
      sphere.setDrawStyle(100013);
      GlStateManager.pushMatrix();
      GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
      sphere.draw(size, slices, stacks);
      GlStateManager.popMatrix();
   }

   public static void drawNametag(Entity entity, String[] text, GSColor color, int type) {
      Vec3d pos = EntityUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks());
      drawNametag(pos.x, pos.y + entity.height, pos.z, text, color, type, 0.0, 0.0);
   }

   public static double getDistance(double x, double y, double z) {
      Entity viewEntity = mc.getRenderViewEntity();
      if (viewEntity == null) {
         viewEntity = mc.player;
      }

      double d0 = viewEntity.posX - x;
      double d1 = viewEntity.posY - y;
      double d2 = viewEntity.posZ - z;
      return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   public static void drawNametag(double x, double y, double z, String[] text, GSColor color, int type, double customScale, double maxSize) {
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      double dist = getDistance(x, y, z);
      double scale = 1.0;
      double offset = 0.0;
      int start = 0;
      switch (type) {
         case 0:
            scale = dist / 20.0 * Math.pow(1.2589254, 0.1 / (dist < 25.0 ? 0.5 : 2.0));
            scale = Math.min(Math.max(scale, 0.5), 5.0);
            offset = scale > 2.0 ? scale / 2.0 : scale;
            scale /= 40.0;
            start = 10;
            break;
         case 1:
            scale = customScale;
            break;
         case 2:
            scale = 0.0018 + 0.003 * dist;
            if (dist <= 8.0) {
               scale = 0.0245;
            }

            start = -8;
      }

      if (maxSize != 0.0 && scale > maxSize) {
         scale = maxSize;
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y + offset - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
      GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
      float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
      GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
      if (type == 2) {
         Nametags nametags = ModuleManager.getModule(Nametags.class);
         double width = 0.0;
         GSColor bcolor = new GSColor(0, 0, 0, 0);
         if (nametags.outline.getValue()) {
            bcolor = color;
            if (nametags.customColor.getValue()) {
               bcolor = nametags.borderColor.getValue();
            }
         }

         for (String s : text) {
            double w = FontUtil.getStringWidth(colorMain.customFont.getValue(), s) / 2.0;
            if (w > width) {
               width = w;
            }
         }

         drawBorderedRect(-width - 1.0, -mc.fontRenderer.FONT_HEIGHT, width + 2.0, new GSColor(0, 4, 0, nametags.border.getValue() ? 85 : 0), bcolor);
      }

      GlStateManager.enableTexture2D();

      for (int i = 0; i < text.length; i++) {
         FontUtil.drawStringWithShadow(
            colorMain.customFont.getValue(),
            text[i],
            -FontUtil.getStringWidth(colorMain.customFont.getValue(), text[i]) / 2,
            i * (mc.fontRenderer.FONT_HEIGHT + 1) + start,
            color
         );
      }

      GlStateManager.disableTexture2D();
      if (type != 2) {
         GlStateManager.popMatrix();
      }
   }

   private static void vertex(double x, double y, double z, BufferBuilder bufferbuilder) {
      bufferbuilder.pos(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ)
         .endVertex();
   }

   private static void colorVertex(double x, double y, double z, GSColor color, int alpha, BufferBuilder bufferbuilder) {
      bufferbuilder.pos(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ)
         .color(color.getRed(), color.getGreen(), color.getBlue(), alpha)
         .endVertex();
   }

   private static AxisAlignedBB getBoundingBox(BlockPos bp, double height) {
      double x = bp.getX();
      double y = bp.getY();
      double z = bp.getZ();
      return new AxisAlignedBB(x, y, z, x + 1.0, y + height, z + 1.0);
   }

   private static void doVerticies(AxisAlignedBB axisAlignedBB, GSColor color, int alpha, BufferBuilder bufferbuilder, int sides, boolean five) {
      if ((sides & 32) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 16) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 4) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 8) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         }
      }

      if ((sides & 2) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         }
      }

      if ((sides & 1) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         if (five) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         }
      }
   }

   public static void doVerticies(AxisAlignedBB axisAlignedBB, GSColor color, int alpha, BufferBuilder bufferbuilder, int sides) {
      if ((sides & 32) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 16) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 4) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 8) != 0) {
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
      }
   }

   public static void doFixVerticies(AxisAlignedBB axisAlignedBB, GSColor color, int alpha, BufferBuilder bufferbuilder, int sides) {
      if ((sides & 32) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
      }

      if ((sides & 16) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 4) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
      }

      if ((sides & 8) != 0) {
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
         colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
      }
   }

   public static void prepare() {
      GL11.glHint(3154, 4354);
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.shadeModel(7425);
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.enableAlpha();
      GL11.glEnable(2848);
      GL11.glEnable(34383);
   }

   public static void release() {
      GL11.glDisable(34383);
      GL11.glDisable(2848);
      GlStateManager.enableAlpha();
      GlStateManager.enableCull();
      GlStateManager.enableTexture2D();
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.glLineWidth(1.0F);
      GlStateManager.shadeModel(7424);
      GL11.glHint(3154, 4352);
   }

   public static Vec3d getInterpolatedPos(Entity entity, float partialTicks, boolean wrap) {
      Vec3d amount = new Vec3d(
         (entity.posX - entity.lastTickPosX) * partialTicks,
         (entity.posY - entity.lastTickPosY) * partialTicks,
         (entity.posZ - entity.lastTickPosZ) * partialTicks
      );
      Vec3d vec = new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(amount);
      return wrap ? vec.subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ) : vec;
   }

   public static AxisAlignedBB getAxisAlignedBB(BlockPos pos, double size) {
      AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
      Vec3d center = bb.getCenter();
      return new AxisAlignedBB(
         center.x - (bb.maxX - bb.minX) * size,
         center.y - (bb.maxY - bb.minX) * size,
         center.z - (bb.maxZ - bb.minZ) * size,
         center.x + (bb.maxX - bb.minX) * size,
         center.y + (bb.maxY - bb.minY) * size,
         center.z + (bb.maxZ - bb.minZ) * size
      );
   }

   public static AxisAlignedBB getInterpolatedAxis(AxisAlignedBB bb) {
      return new AxisAlignedBB(
         bb.minX - mc.getRenderManager().viewerPosX,
         bb.minY - mc.getRenderManager().viewerPosY,
         bb.minZ - mc.getRenderManager().viewerPosZ,
         bb.maxX - mc.getRenderManager().viewerPosX,
         bb.maxY - mc.getRenderManager().viewerPosY,
         bb.maxZ - mc.getRenderManager().viewerPosZ
      );
   }

   public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
      return interpolateEntity(entity, ticks)
         .subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
   }

   public static Vec3d interpolateEntity(Entity entity, float time) {
      return new Vec3d(
         entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
         entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
         entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time
      );
   }

   public static double getInterpolatedDouble(double pre, double current, float partialTicks) {
      return pre + (current - pre) * partialTicks;
   }

   public static float getInterpolatedFloat(float pre, float current, float partialTicks) {
      return pre + (current - pre) * partialTicks;
   }

   public static enum GradientDirection {
      LeftToRight,
      RightToLeft,
      UpToDown,
      DownToUp,
      Normal;
   }

   private static class Points {
      double[][] point = new double[10][2];
      private int count = 0;
      private final double xCenter;
      private final double zCenter;
      public final double yMin;
      public final double yMax;
      private final float rotation;

      public Points(double yMin, double yMax, double xCenter, double zCenter, float rotation) {
         this.yMin = yMin;
         this.yMax = yMax;
         this.xCenter = xCenter;
         this.zCenter = zCenter;
         this.rotation = rotation;
      }

      public void addPoints(double x, double z) {
         x -= this.xCenter;
         z -= this.zCenter;
         double rotateX = x * Math.cos(this.rotation) - z * Math.sin(this.rotation);
         double rotateZ = x * Math.sin(this.rotation) + z * Math.cos(this.rotation);
         rotateX += this.xCenter;
         rotateZ += this.zCenter;
         this.point[this.count++] = new double[]{rotateX, rotateZ};
      }

      public double[] getPoint(int index) {
         return this.point[index];
      }
   }
}
