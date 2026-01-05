package com.lemonclient.api.event.events;

import com.lemonclient.api.event.LemonClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.MoverType;

public class PlayerMoveEvent extends LemonClientEvent {
   private MoverType type;
   private double x;
   private double y;
   private double z;

   public PlayerMoveEvent(MoverType moverType, double x, double y, double z) {
      this.type = moverType;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public MoverType getType() {
      return this.type;
   }

   public void setType(MoverType type) {
      this.type = type;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public void setZ(double z) {
      this.z = z;
   }

   public void setSpeed(double speed) {
      float yaw = Minecraft.getMinecraft().player.rotationYaw;
      double forward = Minecraft.getMinecraft().player.movementInput.moveForward;
      double strafe = Minecraft.getMinecraft().player.movementInput.moveStrafe;
      if (forward == 0.0 && strafe == 0.0) {
         this.setX(0.0);
         this.setZ(0.0);
      } else {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += forward > 0.0 ? -45 : 45;
            } else if (strafe < 0.0) {
               yaw += forward > 0.0 ? 45 : -45;
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians(yaw + 90.0F));
         double sin = Math.sin(Math.toRadians(yaw + 90.0F));
         this.setX(forward * speed * cos + strafe * speed * sin);
         this.setZ(forward * speed * sin - strafe * speed * cos);
      }
   }
}
