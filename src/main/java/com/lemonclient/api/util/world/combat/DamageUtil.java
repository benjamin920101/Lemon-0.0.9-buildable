package com.lemonclient.api.util.world.combat;

import com.lemonclient.api.util.world.combat.raytrace.RayTracer;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.combat.AutoCrystal;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;

public class DamageUtil {
   public static float calculateDamage(EntityLivingBase entity, Vec3d entityPos, AxisAlignedBB entityBox, EntityEnderCrystal crystal) {
      return calculateCrystalDamage(entity, entityPos, entityBox, crystal.posX, crystal.posY, crystal.posZ);
   }

   public static float calculateDamage(
      EntityLivingBase entity, Vec3d entityPos, AxisAlignedBB entityBox, double posX, double posY, double posZ, float size, String mode
   ) {
      MutableBlockPos mutableBlockPos = new MutableBlockPos();
      boolean isPlayer = entity instanceof EntityPlayer;
      if (isPlayer && entity.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         return 0.0F;
      } else {
         float damage = calcRawDamage(entity, entityPos, entityBox, posX, posY, posZ, size * 2.0F, mutableBlockPos, mode);
         if (isPlayer) {
            damage = calcDifficultyDamage(entity, damage);
         }

         return calcReductionDamage(entity, damage);
      }
   }

   public static float calcDamageIgnoreTerrain(
      EntityLivingBase entity, Vec3d entityPos, AxisAlignedBB entityBox, double crystalX, double crystalY, double crystalZ
   ) {
      MutableBlockPos mutableBlockPos = new MutableBlockPos();
      boolean isPlayer = entity instanceof EntityPlayer;
      if (isPlayer && entity.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         return 0.0F;
      } else {
         mutableBlockPos.setPos((int)crystalX, (int)crystalY - 1, (int)crystalZ);
         float damage;
         if (isPlayer && crystalY - entityPos.y > 1.5652173822904127 && isResistant(entity.world.getBlockState(mutableBlockPos))) {
            damage = 1.0F;
         } else {
            damage = calcRawDamage(entity, entityPos, entityBox, crystalX, crystalY, crystalZ, 12.0F, mutableBlockPos, "Crystal");
         }

         if (isPlayer) {
            damage = calcDifficultyDamage(entity, damage);
         }

         return calcReductionDamage(entity, damage);
      }
   }

   public static float calculateCrystalDamageMine(EntityLivingBase entity, Vec3d entityPos, AxisAlignedBB entityBox, double posX, double posY, double posZ) {
      MutableBlockPos mutableBlockPos = new MutableBlockPos();
      boolean isPlayer = entity instanceof EntityPlayer;
      if (isPlayer && entity.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         return 0.0F;
      } else {
         mutableBlockPos.setPos((int)posX, (int)posY - 1, (int)posZ);
         float damage;
         if (isPlayer
            && posY - entityPos.y > 1.5652173822904127
            && ((int)posX != (int)entityPos.x || (int)posZ != (int)entityPos.z)
            && isResistantMine(mutableBlockPos)) {
            damage = 1.0F;
         } else {
            float scaledDist = (float)(entityPos.distanceTo(new Vec3d(posX, posY, posZ)) / 12.0);
            if (scaledDist > 1.0F) {
               damage = 0.0F;
            } else {
               float factor = (1.0F - scaledDist) * getBlockDensity(new Vec3d(posX, posY, posZ), entityBox, entity, true, true, true, true);
               damage = Math.abs((factor * factor + factor) * 12.0F * 3.5F);
            }
         }

         if (isPlayer) {
            damage = calcDifficultyDamage(entity, damage);
         }

         return calcReductionDamage(entity, damage);
      }
   }

   public static float calculateCrystalDamage(EntityLivingBase entity, Vec3d entityPos, AxisAlignedBB entityBox, double posX, double posY, double posZ) {
      MutableBlockPos mutableBlockPos = new MutableBlockPos();
      boolean isPlayer = entity instanceof EntityPlayer;
      if (isPlayer && entity.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         return 0.0F;
      } else {
         mutableBlockPos.setPos((int)posX, (int)posY - 1, (int)posZ);
         float damage;
         if (isPlayer && posY - entityPos.y > 1.5652173822904127 && isResistant(entity.world.getBlockState(mutableBlockPos))) {
            damage = 1.0F;
         } else {
            float scaledDist = (float)(entityPos.distanceTo(new Vec3d(posX, posY, posZ)) / 12.0);
            if (scaledDist > 1.0F) {
               damage = 0.0F;
            } else {
               float factor = (1.0F - scaledDist) * getBlockDensity(new Vec3d(posX, posY, posZ), entityBox, entity, true, true, true, false);
               damage = Math.abs((factor * factor + factor) * 12.0F * 3.5F);
            }
         }

         if (isPlayer) {
            damage = calcDifficultyDamage(entity, damage);
         }

         return calcReductionDamage(entity, damage);
      }
   }

   private static float calcRawDamage(
      EntityLivingBase entity,
      Vec3d entityPos,
      AxisAlignedBB entityBox,
      double posX,
      double posY,
      double posZ,
      float doubleSize,
      MutableBlockPos mutableBlockPos,
      String mode
   ) {
      float scaledDist = (float)(entityPos.distanceTo(new Vec3d(posX, posY, posZ)) / doubleSize);
      if (scaledDist > 1.0F) {
         return 0.0F;
      } else {
         float factor = (1.0F - scaledDist) * getBlockDensity(new Vec3d(posX, posY, posZ), entityBox, entity, true, true, true, false);
         return (factor * factor + factor) * doubleSize * 3.5F + 1.0F;
      }
   }

   public static boolean getDistance(BlockPos pos, Vec3d vec) {
      if (pos != null && vec != null) {
         double x = pos.x + 0.5 - vec.x;
         double z = pos.z + 0.5 - vec.z;
         if (Math.hypot(x, z) >= 2.0) {
            return false;
         } else {
            double y = pos.y - vec.y;
            return true;
         }
      } else {
         return false;
      }
   }

   public static float ignoreTerrainDensity(Vec3d vec, AxisAlignedBB bb, EntityLivingBase entity, String mode) {
      if (mode.equals("CrystalMine")) {
         BlockPos instantPos = null;
         if (ModuleManager.isModuleEnabled(PacketMine.class)) {
            instantPos = PacketMine.INSTANCE.packetPos;
         }

         if (!getDistance(instantPos, vec)) {
            mode = "Crystal";
         }

         if (!AutoCrystal.INSTANCE.civ.getValue()
            && (
               instantPos == null
                  || instantPos.y != vec.y
                     && ((int)vec.x != (int)entity.posX || (int)vec.z != (int)entity.posZ)
            )) {
            mode = "Crystal";
         }
      }

      double d0 = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
      double d1 = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
      double d2 = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
      double d3 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
      double d4 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;
      if (d0 >= 0.0 && d1 >= 0.0 && d2 >= 0.0) {
         int j2 = 0;
         int k2 = 0;

         for (float f = 0.0F; f <= 1.0F; f = (float)(f + d0)) {
            for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)(f1 + d1)) {
               for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)(f2 + d2)) {
                  double d5 = bb.minX + (bb.maxX - bb.minX) * f;
                  double d6 = bb.minY + (bb.maxY - bb.minY) * f1;
                  double d7 = bb.minZ + (bb.maxZ - bb.minZ) * f2;
                  Vec3d newVec = new Vec3d(d5 + d3, d6, d7 + d4);
                  RayTraceResult result = entity.world.rayTraceBlocks(newVec, vec);
                  if (result == null) {
                     j2++;
                  } else {
                     IBlockState state = com.lemonclient.api.util.world.BlockUtil.getState(result.getBlockPos());
                     if (getRaytrace(entity, mode, result.getBlockPos(), state).equals("SKIP")) {
                        j2++;
                     }
                  }

                  k2++;
               }
            }
         }

         return (float)j2 / k2;
      } else {
         return 0.0F;
      }
   }

   public static boolean isResistant(IBlockState blockState) {
      return blockState.getMaterial() != Material.AIR && !(blockState instanceof BlockLiquid) && blockState.getBlock().blockResistance >= 19.7;
   }

   public static boolean isResistantMine(BlockPos pos) {
      BlockPos instantPos = null;
      if (ModuleManager.isModuleEnabled(PacketMine.class)) {
         instantPos = PacketMine.INSTANCE.packetPos;
      }

      IBlockState blockState = com.lemonclient.api.util.world.BlockUtil.getState(pos);
      return blockState.getMaterial() != Material.AIR
         && !(blockState instanceof BlockLiquid)
         && blockState.getBlock().blockResistance >= 19.7
         && (
            !isPos2(instantPos, pos)
               || com.lemonclient.api.util.world.BlockUtil.getState(pos).getBlockHardness(Minecraft.getMinecraft().world, pos) < 0.0F
         );
   }

   public static String getRaytrace(EntityLivingBase entity, String mode, BlockPos pos, IBlockState blockState) {
      switch (mode) {
         case "Crystal":
            if (isResistant(blockState)) {
               return "CALC";
            }

            return "SKIP";
         case "CrystalMine":
            if (isResistantMine(pos)) {
               return "CALC";
            }

            return "SKIP";
         case "Bed":
            Block block = blockState.getBlock();
            if (block != Blocks.AIR && block != Blocks.BED && isResistant(blockState)) {
               return "CALC";
            }

            return "SKIP";
         case "Calc":
            return "Calc";
         case "Skip":
            return "Skip";
         default:
            return blockState.getCollisionBoundingBox(entity.world, pos) != null ? "CALC" : "SKIP";
      }
   }

   public static float calcReductionDamage(EntityLivingBase entity, float damage) {
      PotionEffect potionEffect = entity.getActivePotionEffect(MobEffects.RESISTANCE);
      float resistance = potionEffect == null ? 1.0F : Math.max(1.0F - (potionEffect.getAmplifier() + 1) * 0.2F, 0.0F);
      float blastReduction = 1.0F - Math.min(calcTotalEPF(entity), 20) / 25.0F;
      return CombatRules.getDamageAfterAbsorb(damage, entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue())
         * resistance
         * blastReduction;
   }

   public static int calcTotalEPF(EntityLivingBase entity) {
      int epf = 0;

      for (ItemStack itemStack : entity.getArmorInventoryList()) {
         NBTTagList nbtTagList = itemStack.getEnchantmentTagList();

         for (int i = 0; i <= nbtTagList.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            int id = nbtTagCompound.getInteger("id");
            int level = nbtTagCompound.getShort("lvl");
            if (id == 0) {
               epf += level;
            } else if (id == 3) {
               epf += level * 2;
            }
         }
      }

      return epf;
   }

   public static float calcDifficultyDamage(EntityLivingBase entity, float damage) {
      switch (entity.world.getDifficulty()) {
         case PEACEFUL:
            return 0.0F;
         case EASY:
            return Math.min(damage * 0.5F + 1.0F, damage);
         case HARD:
            return damage * 1.5F;
         default:
            return damage;
      }
   }

   public static boolean in(double number, double floor, double ceil) {
      return number >= floor && number <= ceil;
   }

   public static boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   public static float getBlockDensity(
      Vec3d vec, AxisAlignedBB bb, EntityLivingBase entity, boolean ignoreWebs, boolean ignoreBeds, boolean terrainCalc, boolean mine
   ) {
      if (mine) {
         BlockPos instantPos = null;
         if (ModuleManager.isModuleEnabled(PacketMine.class)) {
            instantPos = PacketMine.INSTANCE.packetPos;
         }

         if (AutoCrystal.INSTANCE.rangeCheck.getValue() && !getDistance(instantPos, vec)) {
            mine = false;
         }

         if (!AutoCrystal.INSTANCE.civ.getValue()
            && (
               instantPos == null
                  || instantPos.y != vec.y
                     && ((int)vec.x != (int)entity.posX || (int)vec.z != (int)entity.posZ)
            )) {
            mine = false;
         }
      }

      double x = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
      double y = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
      double z = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
      double xFloor = (1.0 - Math.floor(1.0 / x) * x) / 2.0;
      double zFloor = (1.0 - Math.floor(1.0 / z) * z) / 2.0;
      if (x >= 0.0 && y >= 0.0 && z >= 0.0) {
         int air = 0;
         int traced = 0;

         for (float a = 0.0F; a <= 1.0F; a = (float)(a + x)) {
            for (float b = 0.0F; b <= 1.0F; b = (float)(b + y)) {
               for (float c = 0.0F; c <= 1.0F; c = (float)(c + z)) {
                  double xOff = bb.minX + (bb.maxX - bb.minX) * a;
                  double yOff = bb.minY + (bb.maxY - bb.minY) * b;
                  double zOff = bb.minZ + (bb.maxZ - bb.minZ) * c;
                  RayTraceResult result = rayTraceBlocks(
                     new Vec3d(xOff + xFloor, yOff, zOff + zFloor), vec, entity.world, false, false, false, ignoreWebs, ignoreBeds, terrainCalc, mine
                  );
                  if (result == null) {
                     air++;
                  }

                  traced++;
               }
            }
         }

         return (float)air / traced;
      } else {
         return 0.0F;
      }
   }

   public static RayTraceResult rayTraceBlocks(
      Vec3d start,
      Vec3d end,
      IBlockAccess world,
      boolean stopOnLiquid,
      boolean ignoreNoBox,
      boolean lastUncollidableBlock,
      boolean ignoreWebs,
      boolean ignoreBeds,
      boolean terrainCalc,
      boolean mine
   ) {
      BlockPos instantPos;
      if (ModuleManager.isModuleEnabled(PacketMine.class)) {
         instantPos = PacketMine.INSTANCE.packetPos;
      } else {
         instantPos = null;
      }

      return RayTracer.trace(
         Minecraft.getMinecraft().world,
         world,
         start,
         end,
         stopOnLiquid,
         ignoreNoBox,
         lastUncollidableBlock,
         (b, p) -> (
               !terrainCalc
                  || !(b.getExplosionResistance(Minecraft.getMinecraft().player) < 100.0F)
                  || !(p.distanceSq(end.x, end.y, end.z) <= 36.0)
            )
            && (
               !mine
                  || !isPos2(p, instantPos)
                  || !(com.lemonclient.api.util.world.BlockUtil.getState(p).getBlockHardness(Minecraft.getMinecraft().world, p) >= 0.0F)
            )
            && (!ignoreBeds || !(b instanceof BlockBed))
            && (!ignoreWebs || !(b instanceof BlockWeb))
      );
   }
}
