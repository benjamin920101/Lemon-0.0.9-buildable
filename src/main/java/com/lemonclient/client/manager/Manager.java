package com.lemonclient.client.manager;

import me.zero.alpine.listener.Listenable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;

public interface Manager extends Listenable {
   default Minecraft getMinecraft() {
      return Minecraft.getMinecraft();
   }

   default EntityPlayerSP getPlayer() {
      return this.getMinecraft().player;
   }

   default WorldClient getWorld() {
      return this.getMinecraft().world;
   }

   default Profiler getProfiler() {
      return this.getMinecraft().profiler;
   }
}
