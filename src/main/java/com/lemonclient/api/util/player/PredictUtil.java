package com.lemonclient.api.util.player;

import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

public class PredictUtil {
   static final Minecraft mc = Minecraft.getMinecraft();

   public static EntityPlayer predictPlayer(EntityLivingBase entity, PredictUtil.PredictSettings settings) {
      double[] posVec = new double[]{entity.posX, entity.posY, entity.posZ};
      double motionX = entity.posX - entity.lastTickPosX;
      double motionY = entity.posY - entity.lastTickPosY;
      double motionZ = entity.posZ - entity.lastTickPosZ;
      boolean isHole = false;
      if (settings.manualOutHole && motionY > 0.2) {
         if (HoleUtil.isHole(EntityUtil.getPosition(entity), false, true, false).getType() != HoleUtil.HoleType.NONE
            && BlockUtil.getBlock(EntityUtil.getPosition(entity).add(0, 2, 0)) instanceof BlockAir) {
            isHole = true;
         } else if (settings.aboveHoleManual
            && HoleUtil.isHole(EntityUtil.getPosition(entity).add(0, -1, 0), false, true, false).getType() != HoleUtil.HoleType.NONE) {
            isHole = true;
         }

         if (isHole) {
            posVec[1]++;
         }
      }

      boolean allowPredictStair = false;
      int stairPredicted = 0;
      if (settings.stairPredict) {
         allowPredictStair = Math.hypot(motionX, motionZ) > settings.speedActivationStairs;
      }

      for (int i = 0; i < settings.tick; i++) {
         boolean predictedStair = false;
         if (settings.splitXZ) {
            double[] newPosVec = (double[])posVec.clone();
            newPosVec[0] += motionX;
            if (calculateRaytrace(posVec, newPosVec)) {
               posVec = (double[])newPosVec.clone();
            } else if (settings.stairPredict && allowPredictStair) {
               if (BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 1.0, newPosVec[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 2.0, newPosVec[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1]++;
                  predictedStair = true;
               } else if (BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 2.0, newPosVec[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 3.0, newPosVec[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1] += 2.0;
                  predictedStair = true;
               }
            }

            newPosVec = (double[])posVec.clone();
            newPosVec[2] += motionZ;
            if (calculateRaytrace(posVec, newPosVec)) {
               posVec = (double[])newPosVec.clone();
            } else if (settings.stairPredict && allowPredictStair) {
               if (BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 1.0, newPosVec[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 2.0, newPosVec[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1]++;
                  predictedStair = true;
               } else if (BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 2.0, newPosVec[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVec[0], newPosVec[1] + 3.0, newPosVec[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1]++;
                  predictedStair = true;
               }
            }
         } else {
            double[] newPosVecx = (double[])posVec.clone();
            newPosVecx[0] += motionX;
            newPosVecx[2] += motionZ;
            if (calculateRaytrace(posVec, newPosVecx)) {
               posVec = (double[])newPosVecx.clone();
            } else if (settings.stairPredict && allowPredictStair) {
               if (BlockUtil.isAirBlock(new BlockPos(newPosVecx[0], newPosVecx[1] + 1.0, newPosVecx[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVecx[0], newPosVecx[1] + 2.0, newPosVecx[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1]++;
                  predictedStair = true;
               } else if (BlockUtil.isAirBlock(new BlockPos(newPosVecx[0], newPosVecx[1] + 2.0, newPosVecx[2]))
                  && BlockUtil.isAirBlock(new BlockPos(newPosVecx[0], newPosVecx[1] + 3.0, newPosVecx[2]))
                  && stairPredicted++ < settings.nStairs) {
                  posVec[1]++;
                  predictedStair = true;
               }
            }
         }

         if (settings.calculateY && !isHole && !predictedStair) {
            double[] var24 = (double[])posVec.clone();
            double decreasePow = settings.startDecrease / Math.pow(10.0, settings.exponentStartDecrease);
            double decreasePowY = settings.decreaseY / Math.pow(10.0, settings.exponentDecreaseY);
            if (!entity.isInWater() && !entity.isInLava() && !entity.isElytraFlying()) {
               motionY += decreasePowY;
               if (Math.abs(motionY) > decreasePow) {
                  motionY = decreasePowY;
               }

               var24[1] += -1.0 * motionY;
            } else {
               decreasePowY = 0.0;
               var24[1] += motionY;
            }

            if (calculateRaytrace(posVec, var24)) {
               posVec = (double[])var24.clone();
            } else {
               motionY -= decreasePowY;
            }
         }
      }

      EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(
         mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), entity.getName())
      );
      clonedPlayer.setPosition(posVec[0], posVec[1], posVec[2]);
      if (entity instanceof EntityPlayer) {
         clonedPlayer.inventory.copyInventory(((EntityPlayer)entity).inventory);
      }

      clonedPlayer.setHealth(entity.getHealth());
      clonedPlayer.prevPosX = entity.prevPosX;
      clonedPlayer.prevPosY = entity.prevPosY;
      clonedPlayer.prevPosZ = entity.prevPosZ;

      for (PotionEffect effect : entity.getActivePotionEffects()) {
         clonedPlayer.addPotionEffect(effect);
      }

      return clonedPlayer;
   }

   public static boolean calculateRaytrace(double[] posVec, double[] newPosVec) {
      RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], newPosVec[1], newPosVec[2]));
      RayTraceResult result1 = mc.world
         .rayTraceBlocks(new Vec3d(posVec[0] + 0.3, posVec[1], posVec[2] + 0.3), new Vec3d(newPosVec[0] - 0.3, newPosVec[1], newPosVec[2] - 0.3));
      RayTraceResult result2 = mc.world
         .rayTraceBlocks(new Vec3d(posVec[0] + 0.3, posVec[1], posVec[2] - 0.3), new Vec3d(newPosVec[0] - 0.3, newPosVec[1], newPosVec[2] + 0.3));
      RayTraceResult result3 = mc.world
         .rayTraceBlocks(new Vec3d(posVec[0] - 0.3, posVec[1], posVec[2] + 0.3), new Vec3d(newPosVec[0] + 0.3, newPosVec[1], newPosVec[2] - 0.3));
      RayTraceResult result4 = mc.world
         .rayTraceBlocks(new Vec3d(posVec[0] - 0.3, posVec[1], posVec[2] - 0.3), new Vec3d(newPosVec[0] + 0.3, newPosVec[1], newPosVec[2] + 0.3));
      return result != null && result.typeOfHit != Type.ENTITY
         ? false
         : (result1 == null || result1.typeOfHit == Type.ENTITY)
            && (result2 == null || result2.typeOfHit == Type.ENTITY)
            && (result3 == null || result3.typeOfHit == Type.ENTITY)
            && (result4 == null || result4.typeOfHit == Type.ENTITY);
   }

   public static class PredictSettings {
      final int tick;
      final boolean calculateY;
      final int startDecrease;
      final int exponentStartDecrease;
      final int decreaseY;
      final int exponentDecreaseY;
      final boolean splitXZ;
      final boolean manualOutHole;
      final boolean aboveHoleManual;
      final boolean stairPredict;
      final int nStairs;
      final double speedActivationStairs;

      public PredictSettings(
         int tick,
         boolean calculateY,
         int startDecrease,
         int exponentStartDecrease,
         int decreaseY,
         int exponentDecreaseY,
         boolean splitXZ,
         boolean manualOutHole,
         boolean aboveHoleManual,
         boolean stairPredict,
         int nStairs,
         double speedActivationStairs
      ) {
         this.tick = tick;
         this.calculateY = calculateY;
         this.startDecrease = startDecrease;
         this.exponentStartDecrease = exponentStartDecrease;
         this.decreaseY = decreaseY;
         this.exponentDecreaseY = exponentDecreaseY;
         this.splitXZ = splitXZ;
         this.manualOutHole = manualOutHole;
         this.aboveHoleManual = aboveHoleManual;
         this.stairPredict = stairPredict;
         this.nStairs = nStairs;
         this.speedActivationStairs = speedActivationStairs;
      }
   }
}
