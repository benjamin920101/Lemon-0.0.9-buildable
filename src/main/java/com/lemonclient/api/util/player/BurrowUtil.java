package com.lemonclient.api.util.player;

import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BurrowUtil {
   public static final Minecraft mc = Minecraft.getMinecraft();
   static EnumFacing[] facing = new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST};

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking, boolean swing) {
      if (pos != null && BlockUtil.isAir(pos)) {
         EnumFacing side = getFirstFacing(pos);
         if (side != null) {
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            boolean sneak = false;
            if (!ColorMain.INSTANCE.sneaking && BlockUtil.blackList.contains(BlockUtil.getBlock(neighbour))) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
               mc.player.setSneaking(true);
               sneak = true;
            }

            if (rotate) {
               faceVector(hitVec, true);
            }

            rightClickBlock(neighbour, hitVec, hand, opposite, packet, swing);
            if (sneak) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
            }

            mc.rightClickDelayTimer = 4;
         }
      }
   }

   public static void placeBlockDown(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking, boolean swing) {
      if (pos != null && BlockUtil.isAir(pos)) {
         EnumFacing side = EnumFacing.DOWN;
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         boolean sneak = false;
         if (!ColorMain.INSTANCE.sneaking && BlockUtil.blackList.contains(BlockUtil.getBlock(neighbour))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            mc.player.setSneaking(true);
            sneak = true;
         }

         if (rotate) {
            faceVector(hitVec, true);
         }

         rightClickBlock(neighbour, hitVec, hand, opposite, packet, swing);
         if (sneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }

         mc.rightClickDelayTimer = 4;
      }
   }

   public static List<EnumFacing> getPossibleSides(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         List<EnumFacing> facings = new ArrayList<>();

         for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
               IBlockState blockState = mc.world.getBlockState(neighbour);
               if (!blockState.getMaterial().isReplaceable()) {
                  facings.add(side);
               }
            }
         }

         return facings;
      }
   }

   public static List<EnumFacing> getTrapdoorPossibleSides(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         List<EnumFacing> facings = new ArrayList<>();

         for (EnumFacing side : facing) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
               IBlockState blockState = mc.world.getBlockState(neighbour);
               if (!blockState.getMaterial().isReplaceable()) {
                  facings.add(side);
               }
            }
         }

         return facings;
      }
   }

   public static EnumFacing getFirstFacing(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         Iterator var1 = getPossibleSides(pos).iterator();
         return var1.hasNext() ? (EnumFacing)var1.next() : null;
      }
   }

   public static EnumFacing getBedFacing(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         for (EnumFacing facing : getPossibleSides(pos)) {
            if (facing != EnumFacing.UP) {
               return facing;
            }
         }

         return null;
      }
   }

   public static EnumFacing getTrapdoorFacing(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         Iterator var1 = getTrapdoorPossibleSides(pos).iterator();
         return var1.hasNext() ? (EnumFacing)var1.next() : null;
      }
   }

   public static Vec3d getEyesPos() {
      return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
   }

   public static float[] getLegitRotations(Vec3d vec) {
      Vec3d eyesPos = getEyesPos();
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{
         mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
         mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
      };
   }

   public static void faceVector(Vec3d vec, boolean normalizeAngle) {
      float[] rotations = getLegitRotations(vec);
      mc.player
         .connection
         .sendPacket(
            new Rotation(rotations[0], normalizeAngle ? MathHelper.normalizeAngle((int)rotations[1], 360) : rotations[1], mc.player.onGround)
         );
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet, boolean swing) {
      if (pos != null && vec != null && hand != null && direction != null) {
         if (packet) {
            float f = (float)(vec.x - pos.getX());
            float f1 = (float)(vec.y - pos.getY());
            float f2 = (float)(vec.z - pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
         } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
         }

         if (swing) {
            mc.player.swingArm(hand);
         }
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
      if (pos != null && vec != null && direction != null) {
         if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, 0.5F, 1.0F, 0.5F));
         } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
         }
      }
   }

   public static void rightClickBlock(BlockPos pos, EnumFacing facing, Vec3d hVec, boolean packet, boolean swing) {
      Vec3d hitVec = new Vec3d(pos).add(hVec).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (packet) {
         rightClickBlock(pos, hitVec, EnumHand.MAIN_HAND, facing);
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND);
      }

      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction) {
      float f = (float)(vec.x - pos.getX());
      float f1 = (float)(vec.y - pos.getY());
      float f2 = (float)(vec.z - pos.getZ());
      mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
   }

   public static int findBlock(Class clazz, boolean inv) {
      int slot = findHotbarBlock(clazz);
      if (slot == -1 && inv) {
         slot = findInventoryBlock(clazz);
      }

      return slot;
   }

   public static int findHotbarBlock(Class clazz) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if (clazz.isInstance(block)) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findHotbarBlock(Block blockIn) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   public static int findHotbarItem(Item input) {
      for (int i = 0; i < 9; i++) {
         Item item = mc.player.inventory.getStackInSlot(i).getItem();
         if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
            return i;
         }
      }

      return -1;
   }

   public static int findInventoryItem(Item input) {
      for (int i = 0; i < 36; i++) {
         Item item = mc.player.inventory.getStackInSlot(i).getItem();
         if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
            return i;
         }
      }

      return -1;
   }

   public static int findInventoryBlock(Class clazz) {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if (clazz.isInstance(block)) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int getCount(Class clazz) {
      int count = 0;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               count += stack.getCount();
            }

            if (stack.getItem() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if (clazz.isInstance(block)) {
                  count += stack.getCount();
               }
            }
         }
      }

      return count;
   }

   public static void switchToSlot(int slot) {
      mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
      mc.player.inventory.currentItem = slot;
      mc.playerController.updateController();
   }
}
