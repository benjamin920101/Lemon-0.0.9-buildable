package com.lemonclient.api.util.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;

public class Wrapper {
   public static EntityPlayerSP getPlayer() {
      return Minecraft.getMinecraft().player;
   }

   public static Minecraft getMinecraft() {
      return Minecraft.getMinecraft();
   }

   public static World getWorld() {
      World world = Minecraft.getMinecraft().world;
      return world;
   }
}
