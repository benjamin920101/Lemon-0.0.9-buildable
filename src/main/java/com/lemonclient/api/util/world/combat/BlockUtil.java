package com.lemonclient.api.util.world.combat;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockUtil {
   static Minecraft mc = Minecraft.getMinecraft();

   public static boolean isEntitiesEmpty(BlockPos pos) {
      List entities = mc.world
         .getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))
         .stream()
         .filter(e -> !(e instanceof EntityItem))
         .filter(e -> !(e instanceof EntityXPOrb))
         .collect(Collectors.toList());
      return entities.isEmpty();
   }

   public static boolean placeBlockScaffold(BlockPos pos, boolean rotate) {
      for (EnumFacing side : EnumFacing.values()) {
         BlockPos neighbor = pos.offset(side);
         EnumFacing side2 = side.getOpposite();
         if (canBeClicked(neighbor)) {
            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
            if (rotate) {
               faceVectorPacketInstant(hitVec);
            }

            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            processRightClickBlock(neighbor, side2, hitVec);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.rightClickDelayTimer = 0;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
            return true;
         }
      }

      return false;
   }

   private static PlayerControllerMP getPlayerController() {
      return mc.playerController;
   }

   public static void processRightClickBlock(BlockPos pos, EnumFacing side, Vec3d hitVec) {
      getPlayerController().processRightClickBlock(mc.player, mc.world, pos, side, hitVec, EnumHand.MAIN_HAND);
   }

   public static IBlockState getState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   public static Block getBlock(BlockPos pos) {
      return getState(pos).getBlock();
   }

   public static boolean canBeClicked(BlockPos pos) {
      return getBlock(pos).canCollideCheck(getState(pos), false);
   }

   public static void faceVectorPacketInstant(Vec3d vec) {
      float[] rotations = getNeededRotations2(vec);
      mc.player.connection.sendPacket(new Rotation(rotations[0], rotations[1], mc.player.onGround));
   }

   private static float[] getNeededRotations2(Vec3d vec) {
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

   public static Vec3d getEyesPos() {
      return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
   }

   public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
      return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
   }

   public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
      return getInterpolatedAmount(entity, ticks, ticks, ticks);
   }

   public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
      return new Vec3d(
         (entity.posX - entity.lastTickPosX) * x,
         (entity.posY - entity.lastTickPosY) * y,
         (entity.posZ - entity.lastTickPosZ) * z
      );
   }
}
