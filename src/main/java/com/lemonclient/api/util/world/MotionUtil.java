package com.lemonclient.api.util.world;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;

public class MotionUtil {
   public static boolean isMoving(EntityLivingBase entity) {
      return entity.moveForward != 0.0F || entity.moveStrafing != 0.0F || entity.moveVertical != 0.0F || entity.motionY > -0.078;
   }

   public static boolean moving(EntityLivingBase entity) {
      return entity.moveForward != 0.0F || entity.moveStrafing != 0.0F;
   }

   public static double getMotion(EntityPlayer entity) {
      return Math.abs(entity.motionX) + Math.abs(entity.motionZ);
   }

   public static void setSpeed(EntityLivingBase entity, double speed) {
      double[] dir = forward(speed);
      entity.motionX = dir[0];
      entity.motionZ = dir[1];
   }

   public static double getBaseMoveSpeed() {
      double result = 0.2873;
      if (Minecraft.getMinecraft().player.isPotionActive(MobEffects.SPEED)) {
         result += 0.2873 * (Objects.requireNonNull(Minecraft.getMinecraft().player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1) * 0.2;
      }

      if (Minecraft.getMinecraft().player.isPotionActive(MobEffects.SLOWNESS)) {
         result -= 0.2873 * (Objects.requireNonNull(Minecraft.getMinecraft().player.getActivePotionEffect(MobEffects.SLOWNESS)).getAmplifier() + 1) * 0.15;
      }

      return result;
   }

   public static double[] forward(double speed) {
      float forward = Minecraft.getMinecraft().player.movementInput.moveForward;
      float side = Minecraft.getMinecraft().player.movementInput.moveStrafe;
      float yaw = Minecraft.getMinecraft().player.prevRotationYaw
         + (Minecraft.getMinecraft().player.rotationYaw - Minecraft.getMinecraft().player.prevRotationYaw)
            * Minecraft.getMinecraft().getRenderPartialTicks();
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

   public static double[] forward(double speed, float yaw) {
      float forward = 1.0F;
      float side = 0.0F;
      double sin = Math.sin(Math.toRadians(yaw + 90.0F));
      double cos = Math.cos(Math.toRadians(yaw + 90.0F));
      double posX = forward * speed * cos + side * speed * sin;
      double posZ = forward * speed * sin - side * speed * cos;
      return new double[]{posX, posZ};
   }

   public static double calcMoveYaw() {
      float yawIn = Minecraft.getMinecraft().player.rotationYaw;
      float moveForward = getRoundedMovementInput(Minecraft.getMinecraft().player.movementInput.moveForward);
      float moveString = getRoundedMovementInput(Minecraft.getMinecraft().player.movementInput.moveStrafe);
      float strafe = 90.0F * moveString;
      strafe *= moveForward != 0.0F ? moveForward * 0.5F : 1.0F;
      float yaw = yawIn - strafe;
      yaw -= moveForward < 0.0F ? 180.0F : 0.0F;
      return Math.toRadians(yaw);
   }

   public static float getRoundedMovementInput(float input) {
      if (input > 0.0F) {
         return 1.0F;
      } else {
         return input < 0.0F ? -1.0F : 0.0F;
      }
   }
}
