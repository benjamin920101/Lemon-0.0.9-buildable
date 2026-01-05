package com.lemonclient.api.util.player;

import com.lemonclient.api.util.world.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static EnumFacing getFacing(double rotationYaw) {
      return EnumFacing.byHorizontalIndex(MathHelper.floor(rotationYaw * 4.0 / 360.0 + 0.5) & 3);
   }

   public static Vec2f getRotationTo(AxisAlignedBB box) {
      EntityPlayerSP player = mc.player;
      if (player == null) {
         return Vec2f.ZERO;
      } else {
         Vec3d eyePos = player.getPositionEyes(1.0F);
         if (player.getEntityBoundingBox().intersects(box)) {
            return getRotationTo(eyePos, box.getCenter());
         } else {
            double x = MathHelper.clamp(eyePos.x, box.minX, box.maxX);
            double y = MathHelper.clamp(eyePos.y, box.minY, box.maxY);
            double z = MathHelper.clamp(eyePos.z, box.minZ, box.maxZ);
            return getRotationTo(eyePos, new Vec3d(x, y, z));
         }
      }
   }

   public static Vec2f getRotationTo(Vec3d posTo) {
      EntityPlayerSP player = mc.player;
      return player != null ? getRotationTo(player.getPositionEyes(1.0F), posTo) : Vec2f.ZERO;
   }

   public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
      return getRotationFromVec(posTo.subtract(posFrom));
   }

   public static Vec2f getRotationFromVec(Vec3d vec) {
      double lengthXZ = Math.hypot(vec.x, vec.z);
      double yaw = normalizeAngle(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
      double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, lengthXZ)));
      return new Vec2f((float)yaw, (float)pitch);
   }

   public static double normalizeAngle(double angle) {
      angle %= 360.0;
      if (angle >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }

   public static float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle >= 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public static boolean isInFov(BlockPos pos) {
      return pos != null && (mc.player.getDistanceSq(pos) < 4.0 || yawDist(pos) < getHalvedfov() + 2.0F);
   }

   public static boolean isInFov(Entity entity) {
      return entity != null && (mc.player.getDistanceSq(entity) < 4.0 || yawDist(entity) < getHalvedfov() + 2.0F);
   }

   public static double yawDist(BlockPos pos) {
      if (pos != null) {
         Vec3d difference = new Vec3d(pos).subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()));
         double d = Math.abs(mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0)) % 360.0;
         return d > 180.0 ? 360.0 - d : d;
      } else {
         return 0.0;
      }
   }

   public static double yawDist(Entity e) {
      if (e != null) {
         Vec3d difference = e.getPositionVector()
            .add(0.0, e.getEyeHeight() / 2.0F, 0.0)
            .subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()));
         double d = Math.abs(mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0)) % 360.0;
         return d > 180.0 ? 360.0 - d : d;
      } else {
         return 0.0;
      }
   }

   public static float transformYaw() {
      float yaw = mc.player.rotationYaw % 360.0F;
      if (mc.player.rotationYaw > 0.0F) {
         if (yaw > 180.0F) {
            yaw = -180.0F + (yaw - 180.0F);
         }
      } else if (yaw < -180.0F) {
         yaw = 180.0F + (yaw + 180.0F);
      }

      return yaw < 0.0F ? 180.0F + yaw : -180.0F + yaw;
   }

   public static boolean isInFov(Vec3d vec3d, Vec3d other) {
      if (mc.player.rotationPitch > 30.0F
         ? !(other.y > mc.player.posY)
         : !(mc.player.rotationPitch < -30.0F) || !(other.y < mc.player.posY)) {
         float angle = BlockUtil.calcAngleNoY(vec3d, other)[0] - transformYaw();
         if (angle < -270.0F) {
            return true;
         } else {
            float fov = mc.gameSettings.fovSetting / 2.0F;
            return angle < fov + 10.0F && angle > -fov - 10.0F;
         }
      } else {
         return true;
      }
   }

   public static float getFov() {
      return mc.gameSettings.fovSetting;
   }

   public static float getHalvedfov() {
      return getFov() / 2.0F;
   }
}
