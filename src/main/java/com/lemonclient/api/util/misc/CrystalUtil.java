package com.lemonclient.api.util.misc;

import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

public class CrystalUtil {
   public static Minecraft mc = Minecraft.getMinecraft();
   private static final List<Block> valid = Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.ENDER_CHEST, Blocks.ANVIL);

   public static void placeCrystal(BlockPos pos, boolean rotate) {
      boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
      BlockPos obsPos = pos.down();
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5)
         );
      EnumFacing facing = result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
      EnumFacing opposite = facing.getOpposite();
      Vec3d vec = new Vec3d(obsPos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()));
      if (rotate) {
         BlockUtil.faceVector(vec);
      }

      mc.player
         .connection
         .sendPacket(new CPacketPlayerTryUseItemOnBlock(obsPos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
      mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
   }

   public static boolean placeCrystal(BlockPos pos, EnumHand hand, boolean packet, boolean rotate, boolean swing) {
      EnumFacing facing = EnumFacing.UP;
      EnumFacing opposite = facing.getOpposite();
      Vec3d vec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()));
      if (rotate) {
         BlockUtil.faceVector(vec);
      }

      if (packet) {
         mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0F, 0.0F, 0.0F));
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, vec, hand);
      }

      if (swing) {
         mc.player.swingArm(hand);
      }

      return true;
   }

   public static boolean isNull(RayTraceResult result, Entity entity) {
      return result == null || result.sideHit == null || result.entityHit == entity;
   }

   public static boolean calculateRaytrace(Entity entity) {
      if (entity == null) {
         return true;
      } else {
         Vec3d vec = PlayerUtil.getEyeVec();
         Vec3d vec3d = entity.getPositionVector();
         RayTraceResult result = mc.world.rayTraceBlocks(vec, vec3d);
         if (isNull(result, entity)) {
            return true;
         } else {
            double x = entity.boundingBox.maxX - entity.boundingBox.minX;
            double y = entity.boundingBox.maxY - entity.boundingBox.minY;
            double z = entity.boundingBox.maxZ - entity.boundingBox.minZ;

            for (double addX = -x; addX <= x; addX += x) {
               for (double addY = 0.0; addY <= y; addY += y) {
                  for (double addZ = -z; addZ <= z; addZ += z) {
                     result = mc.world.rayTraceBlocks(vec, vec3d.add(addX, addY, addZ));
                     if (isNull(result, entity)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public static boolean isNull(RayTraceResult result, BlockPos pos) {
      if (result == null || result.getBlockPos() == pos) {
         return true;
      } else if (result.typeOfHit == Type.ENTITY) {
         double distance = mc.player.getDistance(result.entityHit);
         return distance <= PlayerUtil.getDistanceI(pos);
      } else {
         return false;
      }
   }

   public static boolean isNull(RayTraceResult result, Vec3d vec3d) {
      BlockPos pos = new BlockPos(vec3d);
      return isNull(result, pos);
   }

   public static boolean calculateRaytrace(BlockPos pos) {
      Vec3d vec = PlayerUtil.getEyeVec();
      Vec3d vec3d = new Vec3d(pos);
      RayTraceResult result = mc.world.rayTraceBlocks(vec, vec3d);
      if (isNull(result, pos)) {
         return true;
      } else {
         double x = 0.5;
         double y = 0.5;
         double z = 0.5;

         for (double addX = 0.0; addX <= 1.0; addX += x) {
            for (double addY = 0.0; addY <= 1.0; addY += y) {
               for (double addZ = 0.0; addZ <= 1.0; addZ += z) {
                  result = mc.world.rayTraceBlocks(vec, vec3d.add(addX, addY, addZ));
                  if (isNull(result, pos)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public static boolean calculateRaytrace(EntityPlayer player, Vec3d vec3d) {
      Vec3d vec = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
      RayTraceResult result = mc.world.rayTraceBlocks(vec, vec3d);
      if (isNull(result, vec3d)) {
         return true;
      } else {
         double x = 0.5;
         double y = 0.5;
         double z = 0.5;

         for (double addX = 0.0; addX <= 1.0; addX += x) {
            for (double addY = 0.0; addY <= 1.0; addY += y) {
               for (double addZ = 0.0; addZ <= 1.0; addZ += z) {
                  result = mc.world.rayTraceBlocks(vec, vec3d.add(addX, addY, addZ));
                  if (isNull(result, vec3d)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public static RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
      return rayTraceBlocks(start, end, false, false, false);
   }

   public static RayTraceResult rayTraceBlocks(
      Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUnCollidableBlock
   ) {
      if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
         return null;
      } else if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
         int i = MathHelper.floor(vec32.x);
         int j = MathHelper.floor(vec32.y);
         int k = MathHelper.floor(vec32.z);
         int l = MathHelper.floor(vec31.x);
         int i1;
         int j1;
         BlockPos blockpos = new BlockPos(l, i1 = MathHelper.floor(vec31.y), j1 = MathHelper.floor(vec31.z));
         IBlockState iblockstate = mc.world.getBlockState(blockpos);
         Block block = iblockstate.getBlock();
         if (!valid.contains(block)) {
            block = Blocks.AIR;
            iblockstate = Blocks.AIR.getBlockState().getBaseState();
         }

         if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(mc.world, blockpos) != Block.NULL_AABB)
            && block.canCollideCheck(iblockstate, stopOnLiquid)) {
            return iblockstate.collisionRayTrace(mc.world, blockpos, vec31, vec32);
         } else {
            RayTraceResult raytraceresult2 = null;
            int k1 = 200;

            while (k1-- >= 0) {
               if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                  return null;
               }

               if (l == i && i1 == j && j1 == k) {
                  return returnLastUnCollidableBlock ? raytraceresult2 : null;
               }

               boolean flag2 = true;
               boolean flag = true;
               boolean flag1 = true;
               double d0 = 999.0;
               double d1 = 999.0;
               double d2 = 999.0;
               if (i > l) {
                  d0 = l + 1.0;
               } else if (i < l) {
                  d0 = l + 0.0;
               } else {
                  flag2 = false;
               }

               if (j > i1) {
                  d1 = i1 + 1.0;
               } else if (j < i1) {
                  d1 = i1 + 0.0;
               } else {
                  flag = false;
               }

               if (k > j1) {
                  d2 = j1 + 1.0;
               } else if (k < j1) {
                  d2 = j1 + 0.0;
               } else {
                  flag1 = false;
               }

               double d3 = 999.0;
               double d4 = 999.0;
               double d5 = 999.0;
               double d6 = vec32.x - vec31.x;
               double d7 = vec32.y - vec31.y;
               double d8 = vec32.z - vec31.z;
               if (flag2) {
                  d3 = (d0 - vec31.x) / d6;
               }

               if (flag) {
                  d4 = (d1 - vec31.y) / d7;
               }

               if (flag1) {
                  d5 = (d2 - vec31.z) / d8;
               }

               if (d3 == -0.0) {
                  d3 = -1.0E-4;
               }

               if (d4 == -0.0) {
                  d4 = -1.0E-4;
               }

               if (d5 == -0.0) {
                  d5 = -1.0E-4;
               }

               EnumFacing enumfacing;
               if (d3 < d4 && d3 < d5) {
                  enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                  vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
               } else if (d4 < d5) {
                  enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                  vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
               } else {
                  enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                  vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
               }

               l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
               i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
               j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
               blockpos = new BlockPos(l, i1, j1);
               IBlockState iblockstate1 = mc.world.getBlockState(blockpos);
               Block block1 = iblockstate1.getBlock();
               if (!valid.contains(block1)) {
                  block1 = Blocks.AIR;
                  iblockstate1 = Blocks.AIR.getBlockState().getBaseState();
               }

               if (!ignoreBlockWithoutBoundingBox
                  || iblockstate1.getMaterial() == Material.PORTAL
                  || iblockstate1.getCollisionBoundingBox(mc.world, blockpos) != Block.NULL_AABB) {
                  if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                     return iblockstate1.collisionRayTrace(mc.world, blockpos, vec31, vec32);
                  }

                  raytraceresult2 = new RayTraceResult(Type.MISS, vec31, enumfacing, blockpos);
               }
            }

            return returnLastUnCollidableBlock ? raytraceresult2 : null;
         }
      } else {
         return null;
      }
   }

   public static boolean canPlaceCrystal(BlockPos pos) {
      return BlockUtil.getBlock(pos.add(0, 1, 0)) == Blocks.AIR && BlockUtil.getBlock(pos.add(0, 2, 0)) == Blocks.AIR;
   }

   public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      ArrayList<BlockPos> circleBlocks = new ArrayList<>();
      int cx = pos.getX();
      int cy = pos.getY();
      int cz = pos.getZ();

      for (int x = cx - (int)r; x <= cx + r; x++) {
         for (int z = cz - (int)r; z <= cz + r; z++) {
            int y = sphere ? cy - (int)r : cy;

            while (true) {
               float f = y;
               float f2 = sphere ? cy + r : cy + h;
               if (!(f < f2)) {
                  break;
               }

               double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
               if (dist < r * r && (!hollow || !(dist < (r - 1.0F) * (r - 1.0F)))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleBlocks.add(l);
               }

               y++;
            }
         }
      }

      return circleBlocks;
   }

   public static boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck, boolean onepointThirteen) {
      BlockPos boost = blockPos.add(0, 1, 0);
      BlockPos boost2 = blockPos.add(0, 2, 0);

      try {
         if (!onepointThirteen) {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK
               && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
               return false;
            }

            if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR
               || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
               return false;
            }

            if (!specialEntityCheck) {
               return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
                  && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
            }

            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
               if (!(entity instanceof EntityEnderCrystal)) {
                  return false;
               }
            }

            for (Entity entityx : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
               if (!(entityx instanceof EntityEnderCrystal)) {
                  return false;
               }
            }
         } else {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK
               && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
               return false;
            }

            if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
               return false;
            }

            if (!specialEntityCheck) {
               return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
            }

            for (Entity entityxx : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
               if (!(entityxx instanceof EntityEnderCrystal)) {
                  return false;
               }
            }
         }

         return true;
      } catch (Exception var7) {
         var7.printStackTrace();
         return false;
      }
   }

   public static void breakCrystal(BlockPos pos, boolean swing) {
      if (pos != null) {
         for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityEnderCrystal) {
               breakCrystal(entity, swing);
               break;
            }
         }
      }
   }

   public static void breakCrystalPacket(BlockPos pos, boolean swing) {
      if (pos != null) {
         for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityEnderCrystal) {
               breakCrystalPacket(entity, swing);
               break;
            }
         }
      }
   }

   public static void breakCrystal(Entity crystal, boolean swing) {
      mc.playerController.attackEntity(mc.player, crystal);
      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void breakCrystalPacket(Entity crystal, boolean swing) {
      mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void breakCrystal(
      Entity crystal, boolean packet, boolean swing, boolean packetSwitch, boolean switchBack, boolean antiWeakness, boolean weaknessBypass
   ) {
      int slot = -1;
      if (antiWeakness
         && mc.player.isPotionActive(MobEffects.WEAKNESS)
         && (
            !mc.player.isPotionActive(MobEffects.STRENGTH)
               || Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.STRENGTH)).getAmplifier() < 1
         )) {
         for (int b = 0; b < (weaknessBypass ? 36 : 9); b++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(b);
            if (stack != ItemStack.EMPTY) {
               if (stack.getItem() instanceof ItemSword) {
                  slot = b;
                  break;
               }

               if (stack.getItem() instanceof ItemTool) {
                  slot = b;
               }
            }
         }
      }

      switchTo(slot, weaknessBypass, packetSwitch, switchBack, () -> {
         if (packet) {
            breakCrystalPacket(crystal, swing);
         } else {
            breakCrystal(crystal, swing);
         }
      });
   }

   public static void windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player) {
      short short1 = player.openContainer.getNextTransactionID(player.inventory);
      ItemStack itemStack = player.openContainer.slotClick(slotId, mouseButton, type, player);
      mc.player.connection.sendPacket(new CPacketClickWindow(windowId, slotId, mouseButton, type, itemStack, short1));
      mc.playerController.updateController();
      mc.player.openContainer.detectAndSendChanges();
   }

   private static void switchTo(int slot, boolean bypass, boolean packetSwitch, boolean switchBack, Runnable runnable) {
      int oldslot = mc.player.inventory.currentItem;
      if (slot >= 0 && slot != oldslot) {
         if (bypass) {
            if (slot < 9) {
               slot += 36;
            }

            int id = mc.player.inventoryContainer.windowId;
            windowClick(id, slot, oldslot, ClickType.SWAP, mc.player);
            mc.player.openContainer.detectAndSendChanges();
            windowClick(id, slot, oldslot, ClickType.SWAP, mc.player);
         } else if (slot < 9) {
            if (!switchBack) {
               packetSwitch = false;
            }

            if (packetSwitch) {
               InventoryUtil.packetSwitch(slot);
            } else {
               InventoryUtil.switchSlot(slot);
            }

            runnable.run();
            if (switchBack) {
               if (packetSwitch) {
                  InventoryUtil.packetSwitch(oldslot);
               } else {
                  InventoryUtil.switchSlot(oldslot);
               }
            }
         }
      } else {
         runnable.run();
      }
   }
}
