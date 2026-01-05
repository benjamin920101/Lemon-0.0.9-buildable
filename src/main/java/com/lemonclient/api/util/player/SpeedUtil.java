package com.lemonclient.api.util.player;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpeedUtil {
   static Minecraft mc = Minecraft.getMinecraft();
   public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0;
   public static boolean didJumpThisTick = false;
   public static boolean isJumping = false;
   public double firstJumpSpeed = 0.0;
   public double lastJumpSpeed = 0.0;
   public double percentJumpSpeedChanged = 0.0;
   public double jumpSpeedChanged = 0.0;
   public boolean didJumpLastTick = false;
   public long jumpInfoStartTime = 0L;
   public boolean wasFirstJump = true;
   public double speedometerCurrentSpeed = 0.0;
   public HashMap<EntityPlayer, SpeedUtil.Info> playerInfo = new HashMap<>();

   public static void setDidJumpThisTick(boolean val) {
      didJumpThisTick = val;
   }

   public static void setIsJumping(boolean val) {
      isJumping = val;
   }

   public float lastJumpInfoTimeRemaining() {
      return (float)(Minecraft.getSystemTime() - this.jumpInfoStartTime) / 1000.0F;
   }

   public void update() {
      double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
      double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
      this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
      if (didJumpThisTick && (!mc.player.onGround || isJumping)) {
         if (!this.didJumpLastTick) {
            this.wasFirstJump = this.lastJumpSpeed == 0.0;
            this.percentJumpSpeedChanged = this.speedometerCurrentSpeed != 0.0 ? this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0 : -1.0;
            this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
            this.jumpInfoStartTime = Minecraft.getSystemTime();
            this.lastJumpSpeed = this.speedometerCurrentSpeed;
            this.firstJumpSpeed = this.wasFirstJump ? this.lastJumpSpeed : 0.0;
         }

         this.didJumpLastTick = didJumpThisTick;
      } else {
         this.didJumpLastTick = false;
         this.lastJumpSpeed = 0.0;
      }

      this.updatePlayers();
   }

   public void updatePlayers() {
      for (EntityPlayer player : mc.world.playerEntities) {
         int distance = 20;
         if (mc.player.getDistanceSq(player) < distance * distance) {
            Vec3d lastPos = null;
            if (this.playerInfo.get(player) != null) {
               SpeedUtil.Info info = this.playerInfo.get(player);
               lastPos = info.pos;
            }

            this.playerInfo.put(player, new SpeedUtil.Info(player, lastPos));
         }
      }
   }

   public double getPlayerSpeed(EntityPlayer player) {
      if (player == null) {
         return 0.0;
      } else {
         return this.playerInfo.get(player) == null ? 0.0 : this.turnIntoKpH(this.playerInfo.get(player).speed);
      }
   }

   public Vec3d getPlayerLastPos(EntityPlayer player) {
      if (player == null) {
         return null;
      } else {
         return this.playerInfo.get(player) == null ? null : this.playerInfo.get(player).lastPos;
      }
   }

   public double getPlayerMoveYaw(EntityPlayer player) {
      if (player == null) {
         return 0.0;
      } else {
         return this.playerInfo.get(player) == null ? 0.0 : this.playerInfo.get(player).yaw;
      }
   }

   public double turnIntoKpH(double input) {
      return MathHelper.sqrt(input) * 71.2729367892;
   }

   public double getSpeedKpH() {
      double speedometerkphdouble = this.turnIntoKpH(this.speedometerCurrentSpeed);
      return Math.round(10.0 * speedometerkphdouble) / 10.0;
   }

   public double getSpeedMpS() {
      double speedometerMpsdouble = this.turnIntoKpH(this.speedometerCurrentSpeed) / 3.6;
      return Math.round(10.0 * speedometerMpsdouble) / 10.0;
   }

   public static double calcSpeed(EntityPlayer player) {
      double distTraveledLastTickX = player.posX - player.prevPosX;
      double distTraveledLastTickZ = player.posZ - player.prevPosZ;
      return distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
   }

   public static class Info {
      double speed;
      Vec3d pos;
      Vec3d lastPos;
      double yaw;

      public Info(EntityPlayer player, Vec3d lastPos) {
         this.speed = SpeedUtil.calcSpeed(player);
         this.pos = player.getPositionVector();
         this.yaw = RotationUtil.getRotationTo(this.pos, new Vec3d(player.prevPosX, player.prevPosY, player.prevPosZ)).x;
         this.lastPos = lastPos;
      }
   }
}
