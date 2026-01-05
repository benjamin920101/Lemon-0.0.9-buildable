package com.lemonclient.api.util.player;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer.Position;

public class PositionUtil {
   Minecraft mc = Minecraft.getMinecraft();
   private double x;
   private double y;
   private double z;
   private boolean onground;

   public void updatePosition() {
      this.x = this.mc.player.posX;
      this.y = this.mc.player.posY;
      this.z = this.mc.player.posZ;
      this.onground = this.mc.player.onGround;
   }

   public void restorePosition() {
      this.mc.player.posX = this.x;
      this.mc.player.posY = this.y;
      this.mc.player.posZ = this.z;
      this.mc.player.onGround = this.onground;
   }

   public void setPlayerPosition(double x, double y, double z) {
      this.mc.player.posX = x;
      this.mc.player.posY = y;
      this.mc.player.posZ = z;
   }

   public void setPlayerPosition(double x, double y, double z, boolean onground) {
      this.mc.player.posX = x;
      this.mc.player.posY = y;
      this.mc.player.posZ = z;
      this.mc.player.onGround = onground;
   }

   public void setPositionPacket(double x, double y, double z, boolean onGround, boolean setPos, boolean noLagBack) {
      this.mc.player.connection.sendPacket(new Position(x, y, z, onGround));
      if (setPos) {
         this.mc.player.setPosition(x, y, z);
         if (noLagBack) {
            this.updatePosition();
         }
      }
   }

   public double getX() {
      return this.x;
   }

   public void setX(double x) {
      this.x = x;
   }

   public double getY() {
      return this.y;
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getZ() {
      return this.z;
   }

   public void setZ(double z) {
      this.z = z;
   }
}
