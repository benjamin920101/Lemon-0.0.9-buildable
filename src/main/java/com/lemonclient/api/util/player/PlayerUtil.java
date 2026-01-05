package com.lemonclient.api.util.player;

import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static void setPosition(double x, double y, double z) {
      mc.player.setPosition(x, y, z);
   }

   public static void setPosition(BlockPos pos) {
      mc.player.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
   }

   public static Vec3d getMotionVector() {
      return new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
   }

   public static void vClip(double d) {
      mc.player.setPosition(mc.player.posX, mc.player.posY + d, mc.player.posZ);
   }

   public static void move(double x, double y, double z) {
      mc.player.move(MoverType.SELF, x, y, z);
   }

   public static void setMotionVector(Vec3d vec) {
      mc.player.motionX = vec.x;
      mc.player.motionY = vec.y;
      mc.player.motionZ = vec.z;
   }

   public static boolean isInsideBlock() {
      try {
         AxisAlignedBB playerBoundingBox = mc.player.getEntityBoundingBox();

         for (int x = MathHelper.floor(playerBoundingBox.minX); x < MathHelper.floor(playerBoundingBox.maxX) + 1; x++) {
            for (int y = MathHelper.floor(playerBoundingBox.minY); y < MathHelper.floor(playerBoundingBox.maxY) + 1; y++) {
               for (int z = MathHelper.floor(playerBoundingBox.minZ); z < MathHelper.floor(playerBoundingBox.maxZ) + 1; z++) {
                  Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                  if (!(block instanceof BlockAir)) {
                     AxisAlignedBB boundingBox = Objects.requireNonNull(
                           block.getCollisionBoundingBox(mc.world.getBlockState(new BlockPos(x, y, z)), mc.world, new BlockPos(x, y, z))
                        )
                        .offset(x, y, z);
                     if (block instanceof BlockHopper) {
                        boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                     }

                     if (playerBoundingBox.intersects(boundingBox)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } catch (Exception var6) {
         return false;
      }
   }

   public static BlockPos getPlayerPos() {
      return new BlockPos(
         Math.floor(mc.player.posX), Math.floor(mc.player.posY + 0.5), Math.floor(mc.player.posZ)
      );
   }

   public static BlockPos getPlayerFloorPos() {
      return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
   }

   public static boolean isPlayerClipped() {
      return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox()).isEmpty();
   }

   public static void fakeJump() {
      fakeJump(5);
   }

   public static void fakeJump(int packets) {
      if (packets > 0 && packets != 5) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
      }

      if (packets > 1) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.419999986887, mc.player.posZ, true));
      }

      if (packets > 2) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.7531999805212, mc.player.posZ, true));
      }

      if (packets > 3) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.0013359791121, mc.player.posZ, true));
      }

      if (packets > 4) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.1661092609382, mc.player.posZ, true));
      }
   }

   public static double getDistance(Entity entity) {
      return mc.player.getDistance(entity);
   }

   public static double getDistance(BlockPos pos) {
      return mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
   }

   public static double getDistanceI(BlockPos pos) {
      return getEyeVec().distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
   }

   public static double getDistanceL(BlockPos pos) {
      double x = pos.x - mc.player.posX;
      double z = pos.z - mc.player.posZ;
      return Math.hypot(x, z);
   }

   public static BlockPos getEyesPos() {
      return new BlockPos(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
   }

   public static Vec3d getEyeVec() {
      return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
   }

   public static EntityPlayer getNearestPlayer(double range) {
      List<EntityPlayer> playerList = mc.world
         .playerEntities
         .stream()
         .filter(p -> mc.player.getDistance(p) <= range)
         .filter(p -> !EntityUtil.basicChecksEntity(p))
         .filter(p -> mc.player.entityId != p.entityId)
         .filter(p -> !EntityUtil.isDead(p))
         .collect(Collectors.toList());
      List<EntityPlayer> players = playerList.stream().filter(p -> SocialManager.isEnemy(p.getName())).collect(Collectors.toList());
      if (players.isEmpty()) {
         players.addAll(playerList);
      }

      return players.stream().min(Comparator.comparing(mc.player::getDistance)).orElse(null);
   }

   public static EntityPlayer findLookingPlayer(double rangeMax) {
      ArrayList<EntityPlayer> listPlayer = new ArrayList<>();

      for (EntityPlayer playerSin : mc.world.playerEntities) {
         if (!EntityUtil.basicChecksEntity(playerSin) && mc.player.getDistance(playerSin) <= rangeMax) {
            listPlayer.add(playerSin);
         }
      }

      EntityPlayer target = null;
      Vec3d positionEyes = mc.player.getPositionEyes(mc.getRenderPartialTicks());
      Vec3d rotationEyes = mc.player.getLook(mc.getRenderPartialTicks());
      int precision = 2;

      for (int i = 0; i < (int)rangeMax; i++) {
         for (int j = precision; j > 0; j--) {
            for (EntityPlayer targetTemp : listPlayer) {
               AxisAlignedBB playerBox = targetTemp.getEntityBoundingBox();
               double xArray = positionEyes.x + rotationEyes.x * i + rotationEyes.x / j;
               double yArray = positionEyes.y + rotationEyes.y * i + rotationEyes.y / j;
               double zArray = positionEyes.z + rotationEyes.z * i + rotationEyes.z / j;
               if (playerBox.maxY >= yArray
                  && playerBox.minY <= yArray
                  && playerBox.maxX >= xArray
                  && playerBox.minX <= xArray
                  && playerBox.maxZ >= zArray
                  && playerBox.minZ <= zArray) {
                  target = targetTemp;
               }
            }
         }
      }

      return target;
   }

   public static List<EntityPlayer> getNearPlayers(double range, int count) {
      List<EntityPlayer> targetList = new ArrayList<>();
      List<EntityPlayer> list = new ArrayList<>();

      for (EntityPlayer player : mc.world.playerEntities) {
         if (!(mc.player.getDistance(player) > range) && !EntityUtil.basicChecksEntity(player) && !EntityUtil.isDead(player)) {
            targetList.add(player);
         }
      }

      List<EntityPlayer> players = targetList.stream().filter(p -> SocialManager.isEnemy(p.getName())).collect(Collectors.toList());
      if (players.isEmpty()) {
         players.addAll(targetList);
      }

      players.stream().sorted(Comparator.comparing(PlayerUtil::getDistance)).forEach(list::add);
      return new ArrayList<>(list.subList(0, Math.min(count, list.size())));
   }

   public static float getHealth() {
      return mc.player.getHealth() + mc.player.getAbsorptionAmount();
   }

   public static void centerPlayer() {
      double newX = -2.0;
      double newZ = -2.0;
      int xRel = mc.player.posX < 0.0 ? -1 : 1;
      int zRel = mc.player.posZ < 0.0 ? -1 : 1;
      if (BlockUtil.getBlock(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ) instanceof BlockAir) {
         if (Math.abs(mc.player.posX % 1.0) * 100.0 <= 30.0) {
            newX = Math.round(mc.player.posX - 0.3 * xRel) + 0.5 * -xRel;
         } else if (Math.abs(mc.player.posX % 1.0) * 100.0 >= 70.0) {
            newX = Math.round(mc.player.posX + 0.3 * xRel) - 0.5 * -xRel;
         }

         if (Math.abs(mc.player.posZ % 1.0) * 100.0 <= 30.0) {
            newZ = Math.round(mc.player.posZ - 0.3 * zRel) + 0.5 * -zRel;
         } else if (Math.abs(mc.player.posZ % 1.0) * 100.0 >= 70.0) {
            newZ = Math.round(mc.player.posZ + 0.3 * zRel) - 0.5 * -zRel;
         }
      }

      if (newX == -2.0) {
         if (mc.player.posX > Math.round(mc.player.posX)) {
            newX = Math.round(mc.player.posX) + 0.5;
         } else if (mc.player.posX < Math.round(mc.player.posX)) {
            newX = Math.round(mc.player.posX) - 0.5;
         } else {
            newX = mc.player.posX;
         }
      }

      if (newZ == -2.0) {
         if (mc.player.posZ > Math.round(mc.player.posZ)) {
            newZ = Math.round(mc.player.posZ) + 0.5;
         } else if (mc.player.posZ < Math.round(mc.player.posZ)) {
            newZ = Math.round(mc.player.posZ) - 0.5;
         } else {
            newZ = mc.player.posZ;
         }
      }

      mc.player.connection.sendPacket(new Position(newX, mc.player.posY, newZ, true));
      mc.player.setPosition(newX, mc.player.posY, newZ);
   }
}
