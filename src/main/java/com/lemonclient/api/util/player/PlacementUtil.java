package com.lemonclient.api.util.player;

import com.lemonclient.api.util.world.BlockUtil;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacementUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static int placementConnections = 0;
   private static boolean isSneaking = false;

   public static void onEnable() {
      placementConnections++;
   }

   public static void onDisable() {
      placementConnections--;
      if (placementConnections == 0 && isSneaking) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         isSneaking = false;
      }
   }

   public static void stopSneaking() {
      if (isSneaking) {
         isSneaking = false;
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      }
   }

   public static boolean placeBlock(BlockPos blockPos, EnumHand hand, boolean rotate, Class<? extends Block> blockToPlace) {
      int oldSlot = mc.player.inventory.currentItem;
      int newSlot = InventoryUtil.findFirstBlockSlot(blockToPlace, 0, 8);
      if (newSlot == -1) {
         return false;
      } else {
         mc.player.inventory.currentItem = newSlot;
         boolean output = place(blockPos, hand, rotate);
         mc.player.inventory.currentItem = oldSlot;
         return output;
      }
   }

   public static boolean placeItem(BlockPos blockPos, EnumHand hand, boolean rotate, Class<? extends Item> itemToPlace) {
      int oldSlot = mc.player.inventory.currentItem;
      int newSlot = InventoryUtil.findFirstItemSlot(itemToPlace, 0, 8);
      if (newSlot == -1) {
         return false;
      } else {
         mc.player.inventory.currentItem = newSlot;
         boolean output = place(blockPos, hand, rotate);
         mc.player.inventory.currentItem = oldSlot;
         return output;
      }
   }

   public static boolean place(BlockPos blockPos, EnumHand hand, boolean rotate) {
      return placeBlock(blockPos, hand, rotate, true, null);
   }

   public static boolean place(BlockPos blockPos, EnumHand hand, boolean rotate, ArrayList<EnumFacing> forceSide) {
      return placeBlock(blockPos, hand, rotate, true, forceSide);
   }

   public static boolean holeFill(BlockPos blockPos, EnumHand hand, boolean rotate, boolean swing, ArrayList<EnumFacing> forceSide) {
      return holeFillBlock(blockPos, hand, rotate, swing, forceSide);
   }

   public static boolean holeFillawa(BlockPos blockPos, EnumHand hand, boolean rotate, boolean swing) {
      return holeFillBlockawa(blockPos, hand, rotate, swing);
   }

   public static boolean place(BlockPos blockPos, EnumHand hand, boolean rotate, boolean checkAction) {
      return placeBlock(blockPos, hand, rotate, checkAction, null);
   }

   public static boolean holeFill(BlockPos blockPos, EnumHand hand, boolean rotate, boolean swing) {
      return holeFillBlock(blockPos, hand, rotate, swing, null);
   }

   public static Rotation placeBlockGetRotate(BlockPos blockPos, EnumHand hand, boolean checkAction, ArrayList<EnumFacing> forceSide, boolean swingArm) {
      EntityPlayerSP player = mc.player;
      WorldClient world = mc.world;
      PlayerControllerMP playerController = mc.playerController;
      if (player == null || world == null || playerController == null) {
         return null;
      } else if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) {
         return null;
      } else {
         EnumFacing side = forceSide != null ? BlockUtil.getPlaceableSideExlude(blockPos, forceSide) : BlockUtil.getPlaceableSide(blockPos);
         if (side == null) {
            return null;
         } else {
            BlockPos neighbour = blockPos.offset(side);
            EnumFacing opposite = side.getOpposite();
            if (!BlockUtil.canBeClicked(neighbour)) {
               return null;
            } else {
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               Block neighbourBlock = world.getBlockState(neighbour).getBlock();
               if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
                  player.connection.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
                  isSneaking = true;
               }

               EnumActionResult action = playerController.processRightClickBlock(player, world, neighbour, opposite, hitVec, hand);
               if (!checkAction || action == EnumActionResult.SUCCESS) {
                  if (swingArm) {
                     player.swingArm(hand);
                     mc.rightClickDelayTimer = 4;
                  } else {
                     player.connection.sendPacket(new CPacketAnimation(hand));
                  }
               }

               return BlockUtil.getFaceVectorPacket(hitVec, true);
            }
         }
      }
   }

   public static boolean placeBlock(BlockPos blockPos, EnumHand hand, boolean rotate, boolean checkAction, ArrayList<EnumFacing> forceSide) {
      EntityPlayerSP player = mc.player;
      WorldClient world = mc.world;
      PlayerControllerMP playerController = mc.playerController;
      if (player == null || world == null || playerController == null) {
         return false;
      } else if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) {
         return false;
      } else {
         EnumFacing side = forceSide != null ? BlockUtil.getPlaceableSideExlude(blockPos, forceSide) : BlockUtil.getPlaceableSide(blockPos);
         if (side == null) {
            return false;
         } else {
            BlockPos neighbour = blockPos.offset(side);
            EnumFacing opposite = side.getOpposite();
            if (!BlockUtil.canBeClicked(neighbour)) {
               return false;
            } else {
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               Block neighbourBlock = world.getBlockState(neighbour).getBlock();
               if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
                  player.connection.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
                  isSneaking = true;
               }

               if (rotate) {
                  BlockUtil.faceVectorPacketInstant(hitVec, true);
               }

               EnumActionResult action = playerController.processRightClickBlock(player, world, neighbour, opposite, hitVec, hand);
               if (!checkAction || action == EnumActionResult.SUCCESS) {
                  player.swingArm(hand);
                  mc.rightClickDelayTimer = 4;
               }

               return action == EnumActionResult.SUCCESS;
            }
         }
      }
   }

   public static boolean holeFillBlock(BlockPos blockPos, EnumHand hand, boolean rotate, boolean swing, ArrayList<EnumFacing> forceSide) {
      EntityPlayerSP player = mc.player;
      WorldClient world = mc.world;
      PlayerControllerMP playerController = mc.playerController;
      if (player == null || world == null || playerController == null) {
         return false;
      } else if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) {
         return false;
      } else {
         EnumFacing side = forceSide != null ? BlockUtil.getPlaceableSideExlude(blockPos, forceSide) : BlockUtil.getPlaceableSide(blockPos);
         if (side == null) {
            return false;
         } else {
            BlockPos neighbour = blockPos.offset(side);
            EnumFacing opposite = side.getOpposite();
            if (!BlockUtil.canBeClicked(neighbour)) {
               return false;
            } else {
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               Block neighbourBlock = world.getBlockState(neighbour).getBlock();
               if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
                  player.connection.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
                  isSneaking = true;
               }

               if (rotate) {
                  BlockUtil.faceVectorPacketInstant(hitVec, true);
               }

               EnumActionResult action = playerController.processRightClickBlock(player, world, neighbour, opposite, hitVec, hand);
               if (swing) {
                  player.swingArm(hand);
               }

               return action == EnumActionResult.SUCCESS;
            }
         }
      }
   }

   public static boolean holeFillBlockawa(BlockPos blockPos, EnumHand hand, boolean rotate, boolean swing) {
      EntityPlayerSP player = mc.player;
      WorldClient world = mc.world;
      PlayerControllerMP playerController = mc.playerController;
      if (player == null || world == null || playerController == null) {
         return false;
      } else if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) {
         return false;
      } else {
         BlockPos neighbour;
         EnumFacing opposite;
         if (!mc.world.isAirBlock(blockPos.south())) {
            neighbour = blockPos.offset(EnumFacing.SOUTH);
            opposite = EnumFacing.SOUTH.getOpposite();
         } else if (!mc.world.isAirBlock(blockPos.north())) {
            neighbour = blockPos.offset(EnumFacing.NORTH);
            opposite = EnumFacing.NORTH.getOpposite();
         } else if (!mc.world.isAirBlock(blockPos.east())) {
            neighbour = blockPos.offset(EnumFacing.EAST);
            opposite = EnumFacing.EAST.getOpposite();
         } else {
            if (mc.world.isAirBlock(blockPos.west())) {
               return false;
            }

            neighbour = blockPos.offset(EnumFacing.WEST);
            opposite = EnumFacing.WEST.getOpposite();
         }

         if (!BlockUtil.canBeClicked(neighbour)) {
            return false;
         } else {
            Vec3d hitVec = new Vec3d(neighbour).add(new Vec3d(0.5, 0.8, 0.5)).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            Block neighbourBlock = world.getBlockState(neighbour).getBlock();
            if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
               player.connection.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
               isSneaking = true;
            }

            if (rotate) {
               BlockUtil.faceVectorPacketInstant(hitVec, true);
            }

            EnumActionResult action = playerController.processRightClickBlock(player, world, neighbour, opposite, hitVec, hand);
            if (swing) {
               player.swingArm(hand);
            }

            return action == EnumActionResult.SUCCESS;
         }
      }
   }

   public static boolean placePrecise(
      BlockPos blockPos, EnumHand hand, boolean rotate, Vec3d precise, EnumFacing forceSide, boolean onlyRotation, boolean support
   ) {
      EntityPlayerSP player = mc.player;
      WorldClient world = mc.world;
      PlayerControllerMP playerController = mc.playerController;
      if (player == null || world == null || playerController == null) {
         return false;
      } else if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) {
         return false;
      } else {
         EnumFacing side = forceSide == null ? BlockUtil.getPlaceableSide(blockPos) : forceSide;
         if (side == null) {
            return false;
         } else {
            BlockPos neighbour = blockPos.offset(side);
            EnumFacing opposite = side.getOpposite();
            if (!BlockUtil.canBeClicked(neighbour)) {
               return false;
            } else {
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               Block neighbourBlock = world.getBlockState(neighbour).getBlock();
               if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
                  player.connection.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
                  isSneaking = true;
               }

               if (rotate && !support) {
                  BlockUtil.faceVectorPacketInstant(precise == null ? hitVec : precise, true);
               }

               if (!onlyRotation) {
                  EnumActionResult action = playerController.processRightClickBlock(player, world, neighbour, opposite, precise == null ? hitVec : precise, hand);
                  if (action == EnumActionResult.SUCCESS) {
                     player.swingArm(hand);
                     mc.rightClickDelayTimer = 4;
                  }

                  return action == EnumActionResult.SUCCESS;
               } else {
                  return true;
               }
            }
         }
      }
   }
}
