package com.lemonclient.api.util.player;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.LemonClient;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listenable;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

public class SpoofRotationUtil implements Listenable {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final SpoofRotationUtil ROTATION_UTIL = new SpoofRotationUtil();
   private int rotationConnections = 0;
   private boolean shouldSpoofAngles;
   private boolean isSpoofingAngles;
   private double yaw;
   private double pitch;
   @EventHandler
   private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
      Packet packet = event.getPacket();
      if (packet instanceof CPacketPlayer && this.shouldSpoofAngles && this.isSpoofingAngles) {
         ((CPacketPlayer)packet).yaw = (float)this.yaw;
         ((CPacketPlayer)packet).pitch = (float)this.pitch;
      }
   });

   private SpoofRotationUtil() {
   }

   public void onEnable() {
      this.rotationConnections++;
      if (this.rotationConnections == 1) {
         LemonClient.EVENT_BUS.subscribe(this);
      }
   }

   public void onDisable() {
      this.rotationConnections--;
      if (this.rotationConnections == 0) {
         LemonClient.EVENT_BUS.unsubscribe(this);
      }
   }

   public void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
      double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
      this.setYawAndPitch((float)v[0], (float)v[1]);
   }

   public void setYawAndPitch(float yaw1, float pitch1) {
      this.yaw = yaw1;
      this.pitch = pitch1;
      this.isSpoofingAngles = true;
   }

   public void resetRotation() {
      if (this.isSpoofingAngles) {
         this.yaw = mc.player.rotationYaw;
         this.pitch = mc.player.rotationPitch;
         this.isSpoofingAngles = false;
      }
   }

   public void shouldSpoofAngles(boolean e) {
      this.shouldSpoofAngles = e;
   }

   public boolean isSpoofingAngles() {
      return this.isSpoofingAngles;
   }
}
