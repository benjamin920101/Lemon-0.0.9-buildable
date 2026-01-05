package com.lemonclient.api.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager.Profile;
import org.lwjgl.opengl.GL11;

public class ChamsUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static void createChamsPre() {
      mc.getRenderManager().setRenderShadow(false);
      mc.getRenderManager().setRenderOutlines(false);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GL11.glEnable(32823);
      GL11.glDepthRange(0.0, 0.01);
      GlStateManager.popMatrix();
   }

   public static void createChamsPost() {
      boolean shadow = mc.getRenderManager().isRenderShadow();
      mc.getRenderManager().setRenderShadow(shadow);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GL11.glDisable(32823);
      GL11.glDepthRange(0.0, 1.0);
      GlStateManager.popMatrix();
   }

   public static void createColorPre(GSColor color, boolean isPlayer) {
      mc.getRenderManager().setRenderShadow(false);
      mc.getRenderManager().setRenderOutlines(false);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GL11.glEnable(32823);
      GL11.glDepthRange(0.0, 0.01);
      GL11.glDisable(3553);
      if (!isPlayer) {
         GlStateManager.enableBlendProfile(Profile.TRANSPARENT_MODEL);
      }

      color.glColor();
      GlStateManager.popMatrix();
   }

   public static void createColorPost(boolean isPlayer) {
      boolean shadow = mc.getRenderManager().isRenderShadow();
      mc.getRenderManager().setRenderShadow(shadow);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      if (!isPlayer) {
         GlStateManager.disableBlendProfile(Profile.TRANSPARENT_MODEL);
      }

      GL11.glDisable(32823);
      GL11.glDepthRange(0.0, 1.0);
      GL11.glEnable(3553);
      GlStateManager.popMatrix();
   }

   public static void createWirePre(GSColor color, int lineWidth, boolean isPlayer) {
      mc.getRenderManager().setRenderShadow(false);
      mc.getRenderManager().setRenderOutlines(false);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GL11.glPolygonMode(1032, 6913);
      GL11.glEnable(10754);
      GL11.glDepthRange(0.0, 0.01);
      GL11.glDisable(3553);
      GL11.glDisable(2896);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      if (!isPlayer) {
         GlStateManager.enableBlendProfile(Profile.TRANSPARENT_MODEL);
      }

      GL11.glLineWidth(lineWidth);
      color.glColor();
      GlStateManager.popMatrix();
   }

   public static void createWirePost(boolean isPlayer) {
      boolean shadow = mc.getRenderManager().isRenderShadow();
      mc.getRenderManager().setRenderShadow(shadow);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      if (!isPlayer) {
         GlStateManager.disableBlendProfile(Profile.TRANSPARENT_MODEL);
      }

      GL11.glPolygonMode(1032, 6914);
      GL11.glDisable(10754);
      GL11.glDepthRange(0.0, 1.0);
      GL11.glEnable(3553);
      GL11.glEnable(2896);
      GL11.glDisable(2848);
      GlStateManager.popMatrix();
   }
}
