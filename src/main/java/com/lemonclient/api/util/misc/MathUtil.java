package com.lemonclient.api.util.misc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static Random rnd = new Random();

   public static int getRandom(int min, int max) {
      return rnd.nextInt(max - min + 1) + min;
   }

   public static float[] calcAngle(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difY = (to.y - from.y) * -1.0;
      double difZ = to.z - from.z;
      double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
      return new float[]{
         (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
      };
   }

   public static double calculateAngle(double x1, double y1, double x2, double y2) {
      double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
      return angle + Math.ceil(-angle / 360.0) * 360.0;
   }

   public static int clamp(int num, int min, int max) {
      return num < min ? min : Math.min(num, max);
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double clamp(double num, double min, double max) {
      return num < min ? min : Math.min(num, max);
   }

   public static long clamp(long num, long min, long max) {
      return num < min ? min : Math.min(num, max);
   }

   public static BigDecimal clamp(BigDecimal num, BigDecimal min, BigDecimal max) {
      return smallerThan(num, min) ? min : (biggerThan(num, max) ? max : num);
   }

   public static Vec3d roundVec(Vec3d vec3d, int places) {
      return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
   }

   public static boolean biggerThan(BigDecimal bigger, BigDecimal than) {
      return bigger.compareTo(than) > 0;
   }

   public static boolean smallerThan(BigDecimal smaller, BigDecimal than) {
      return smaller.compareTo(than) < 0;
   }

   public static double round(double value, int places) {
      return places < 0 ? value : new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
   }

   public static float round(float value, int places) {
      return places < 0 ? value : new BigDecimal((double)value).setScale(places, RoundingMode.HALF_UP).floatValue();
   }

   public static float round(float value, int places, float min, float max) {
      return MathHelper.clamp(places < 0 ? value : new BigDecimal((double)value).setScale(places, RoundingMode.HALF_UP).floatValue(), min, max);
   }

   public static Vec3d interpolateEntity(Entity entity, float time) {
      return new Vec3d(
         entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
         entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
         entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time
      );
   }

   public static float rad(float angle) {
      return (float)(angle * Math.PI / 180.0);
   }

   public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difZ = to.z - from.z;
      return new float[]{(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)};
   }

   public static Double calculateDoubleChange(double oldDouble, double newDouble, int step, int currentStep) {
      return oldDouble + (newDouble - oldDouble) * Math.max(0, Math.min(step, currentStep)) / step;
   }

   public static double square(double input) {
      return input * input;
   }

   public static double[] directionSpeed(double speed) {
      Minecraft mc = Minecraft.getMinecraft();
      float forward = mc.player.movementInput.moveForward;
      float side = mc.player.movementInput.moveStrafe;
      float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += forward > 0.0F ? -45 : 45;
         } else if (side < 0.0F) {
            yaw += forward > 0.0F ? 45 : -45;
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians(yaw + 90.0F));
      double cos = Math.cos(Math.toRadians(yaw + 90.0F));
      double posX = forward * speed * cos + side * speed * sin;
      double posZ = forward * speed * sin - side * speed * cos;
      return new double[]{posX, posZ};
   }

   public static float square(float v1) {
      return v1 * v1;
   }

   public static double square(Double v1) {
      return v1 * v1;
   }

   public static double calculateDistanceWithPartialTicks(double n, double n2, float renderPartialTicks) {
      return n2 + (n - n2) * mc.getRenderPartialTicks();
   }

   public static Vec3d interpolateEntityClose(Entity entity, float renderPartialTicks) {
      return new Vec3d(
         calculateDistanceWithPartialTicks(entity.posX, entity.lastTickPosX, renderPartialTicks) - mc.getRenderManager().renderPosX,
         calculateDistanceWithPartialTicks(entity.posY, entity.lastTickPosY, renderPartialTicks) - mc.getRenderManager().renderPosY,
         calculateDistanceWithPartialTicks(entity.posZ, entity.lastTickPosZ, renderPartialTicks) - mc.getRenderManager().renderPosZ
      );
   }

   public static double radToDeg(double rad) {
      return rad * (float) (180.0 / Math.PI);
   }

   public static double degToRad(double deg) {
      return deg * (float) (Math.PI / 180.0);
   }

   public static Vec3d direction(float yaw) {
      return new Vec3d(Math.cos(degToRad(yaw + 90.0F)), 0.0, Math.sin(degToRad(yaw + 90.0F)));
   }

   public static float wrap(float val) {
      val %= 360.0F;
      if (val >= 180.0F) {
         val -= 360.0F;
      }

      if (val < -180.0F) {
         val += 360.0F;
      }

      return val;
   }

   public static double map(double value, double a, double b, double c, double d) {
      value = (value - a) / (b - a);
      return c + value * (d - c);
   }

   public static double linear(double from, double to, double incline) {
      return from < to - incline ? from + incline : (from > to + incline ? from - incline : to);
   }

   public static double parabolic(double from, double to, double incline) {
      return from + (to - from) / incline;
   }

   public static double getDistance(Vec3d pos, double x, double y, double z) {
      double deltaX = pos.x - x;
      double deltaY = pos.y - y;
      double deltaZ = pos.z - z;
      return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
   }

   public static double[] calcIntersection(double[] line, double[] line2) {
      double a1 = line[3] - line[1];
      double b1 = line[0] - line[2];
      double c1 = a1 * line[0] + b1 * line[1];
      double a2 = line2[3] - line2[1];
      double b2 = line2[0] - line2[2];
      double c2 = a2 * line2[0] + b2 * line2[1];
      double delta = a1 * b2 - a2 * b1;
      return new double[]{(b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta};
   }

   public static boolean isIntersect(AxisAlignedBB a, AxisAlignedBB b) {
      if (a.maxX <= b.minX || a.minX >= b.maxX) {
         return false;
      } else {
         return !(a.maxY <= b.minY) && !(a.minY >= b.maxY)
            ? !(a.maxZ <= b.minZ) && !(a.minZ >= b.maxZ)
            : false;
      }
   }
}
