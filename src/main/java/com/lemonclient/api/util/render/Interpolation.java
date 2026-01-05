package com.lemonclient.api.util.render;

import net.minecraft.client.Minecraft;

public class Interpolation {
   public static Minecraft mc = Minecraft.getMinecraft();

   public static double getRenderPosX() {
      return mc.getRenderManager().renderPosX;
   }

   public static double getRenderPosY() {
      return mc.getRenderManager().renderPosY;
   }

   public static double getRenderPosZ() {
      return mc.getRenderManager().renderPosZ;
   }
}
