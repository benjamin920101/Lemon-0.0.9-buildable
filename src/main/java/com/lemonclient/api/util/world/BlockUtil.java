package com.lemonclient.api.util.world;

import com.lemonclient.api.util.misc.Wrapper;
import com.lemonclient.api.util.world.combat.CrystalUtil;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

public class BlockUtil {
   public static final List shulkerList = Arrays.asList(
      Blocks.WHITE_SHULKER_BOX,
      Blocks.ORANGE_SHULKER_BOX,
      Blocks.MAGENTA_SHULKER_BOX,
      Blocks.LIGHT_BLUE_SHULKER_BOX,
      Blocks.YELLOW_SHULKER_BOX,
      Blocks.LIME_SHULKER_BOX,
      Blocks.PINK_SHULKER_BOX,
      Blocks.GRAY_SHULKER_BOX,
      Blocks.SILVER_SHULKER_BOX,
      Blocks.CYAN_SHULKER_BOX,
      Blocks.PURPLE_SHULKER_BOX,
      Blocks.BLUE_SHULKER_BOX,
      Blocks.BROWN_SHULKER_BOX,
      Blocks.GREEN_SHULKER_BOX,
      Blocks.RED_SHULKER_BOX,
      Blocks.BLACK_SHULKER_BOX
   );
   public static final List blackList = Arrays.asList(
      Blocks.CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.ENDER_CHEST,
      Blocks.ANVIL,
      Blocks.WOODEN_BUTTON,
      Blocks.STONE_BUTTON,
      Blocks.UNPOWERED_COMPARATOR,
      Blocks.UNPOWERED_REPEATER,
      Blocks.POWERED_REPEATER,
      Blocks.POWERED_COMPARATOR,
      Blocks.OAK_FENCE_GATE,
      Blocks.SPRUCE_FENCE_GATE,
      Blocks.BIRCH_FENCE_GATE,
      Blocks.JUNGLE_FENCE_GATE,
      Blocks.DARK_OAK_FENCE_GATE,
      Blocks.ACACIA_FENCE_GATE,
      Blocks.BREWING_STAND,
      Blocks.DISPENSER,
      Blocks.DROPPER,
      Blocks.LEVER,
      Blocks.NOTEBLOCK,
      Blocks.JUKEBOX,
      Blocks.BEACON,
      Blocks.BED,
      Blocks.FURNACE,
      Blocks.OAK_DOOR,
      Blocks.SPRUCE_DOOR,
      Blocks.BIRCH_DOOR,
      Blocks.JUNGLE_DOOR,
      Blocks.ACACIA_DOOR,
      Blocks.DARK_OAK_DOOR,
      Blocks.CAKE,
      Blocks.ENCHANTING_TABLE,
      Blocks.DRAGON_EGG,
      Blocks.HOPPER,
      Blocks.REPEATING_COMMAND_BLOCK,
      Blocks.COMMAND_BLOCK,
      Blocks.CHAIN_COMMAND_BLOCK,
      Blocks.CRAFTING_TABLE,
      Blocks.WALL_SIGN,
      Blocks.STANDING_SIGN,
      shulkerList
   );
   public static final List unSolidBlocks = Arrays.asList(
      Blocks.SNOW,
      Blocks.CARPET,
      Blocks.END_ROD,
      Blocks.SKULL,
      Blocks.FLOWER_POT,
      Blocks.TRIPWIRE,
      Blocks.TRIPWIRE_HOOK,
      Blocks.LADDER,
      Blocks.UNLIT_REDSTONE_TORCH,
      Blocks.REDSTONE_WIRE,
      Blocks.AIR,
      Blocks.PORTAL,
      Blocks.END_PORTAL,
      Blocks.WATER,
      Blocks.FLOWING_WATER,
      Blocks.LAVA,
      Blocks.FLOWING_LAVA,
      Blocks.SAPLING,
      Blocks.RED_FLOWER,
      Blocks.YELLOW_FLOWER,
      Blocks.BROWN_MUSHROOM,
      Blocks.RED_MUSHROOM,
      Blocks.WHEAT,
      Blocks.CARROTS,
      Blocks.POTATOES,
      Blocks.BEETROOTS,
      Blocks.REEDS,
      Blocks.PUMPKIN_STEM,
      Blocks.MELON_STEM,
      Blocks.WATERLILY,
      Blocks.NETHER_WART,
      Blocks.COCOA,
      Blocks.CHORUS_FLOWER,
      Blocks.CHORUS_PLANT,
      Blocks.TALLGRASS,
      Blocks.DEADBUSH,
      Blocks.VINE,
      Blocks.FIRE,
      Blocks.RAIL,
      Blocks.ACTIVATOR_RAIL,
      Blocks.DETECTOR_RAIL,
      Blocks.GOLDEN_RAIL,
      Blocks.TORCH,
      Blocks.REDSTONE_TORCH,
      Blocks.WEB,
      Blocks.PISTON_HEAD,
      Blocks.PISTON_EXTENSION,
      Blocks.PISTON,
      Blocks.STICKY_PISTON,
      Blocks.CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.ENDER_CHEST,
      Blocks.ANVIL,
      Blocks.WOODEN_BUTTON,
      Blocks.STONE_BUTTON,
      Blocks.UNPOWERED_COMPARATOR,
      Blocks.UNPOWERED_REPEATER,
      Blocks.POWERED_REPEATER,
      Blocks.POWERED_COMPARATOR,
      Blocks.OAK_FENCE_GATE,
      Blocks.SPRUCE_FENCE_GATE,
      Blocks.BIRCH_FENCE_GATE,
      Blocks.JUNGLE_FENCE_GATE,
      Blocks.DARK_OAK_FENCE_GATE,
      Blocks.ACACIA_FENCE_GATE,
      Blocks.BREWING_STAND,
      Blocks.DISPENSER,
      Blocks.DROPPER,
      Blocks.LEVER,
      Blocks.NOTEBLOCK,
      Blocks.JUKEBOX,
      Blocks.BEACON,
      Blocks.BED,
      Blocks.FURNACE,
      Blocks.OAK_DOOR,
      Blocks.SPRUCE_DOOR,
      Blocks.BIRCH_DOOR,
      Blocks.JUNGLE_DOOR,
      Blocks.ACACIA_DOOR,
      Blocks.DARK_OAK_DOOR,
      Blocks.CAKE,
      Blocks.ENCHANTING_TABLE,
      Blocks.DRAGON_EGG,
      shulkerList
   );
   public static final List airBlocks = Arrays.asList(
      Blocks.AIR,
      Blocks.MAGMA,
      Blocks.LAVA,
      Blocks.FLOWING_LAVA,
      Blocks.WATER,
      Blocks.FLOWING_WATER,
      Blocks.FIRE,
      Blocks.VINE,
      Blocks.SNOW_LAYER,
      Blocks.TALLGRASS
   );
   private static final Minecraft mc = Minecraft.getMinecraft();
   static EnumFacing[] facing = new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST};

   public static AxisAlignedBB getBoundingBox(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         AxisAlignedBB box = getState(pos).getCollisionBoundingBox(mc.world, pos);
         return box == null
            ? null
            : new AxisAlignedBB(
               pos.x + box.minX,
               pos.y + box.minY,
               pos.z + box.minZ,
               pos.x + box.maxX,
               pos.y + box.maxY,
               pos.z + box.maxZ
            );
      }
   }

   public static Vec3d[] convertVec3ds(Vec3d vec3d, Vec3d[] input) {
      Vec3d[] output = new Vec3d[input.length];

      for (int i = 0; i < input.length; i++) {
         output[i] = vec3d.add(input[i]);
      }

      return output;
   }

   public static Vec3d[] convertVec3ds(EntityPlayer entity, Vec3d[] input) {
      return convertVec3ds(entity.getPositionVector(), input);
   }

   public static NonNullList<BlockPos> getBox(float range) {
      NonNullList<BlockPos> positions = NonNullList.create();
      positions.addAll(
         EntityUtil.getSphere(
            new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)),
            (double)range,
            0.0,
            false,
            true,
            0
         )
      );
      return positions;
   }

   public static NonNullList<BlockPos> getBox(float range, BlockPos pos) {
      NonNullList<BlockPos> positions = NonNullList.create();
      positions.addAll(EntityUtil.getSphere(pos, (double)range, 0.0, false, true, 0));
      return positions;
   }

   public static boolean isBlockUnSolid(BlockPos blockPos) {
      Block block = getBlock(blockPos);
      return isBlockUnSolid(block) || !block.fullBlock;
   }

   public static boolean canOpen(BlockPos blockPos) {
      return canOpen(mc.world.getBlockState(blockPos).getBlock());
   }

   public static boolean isAir(BlockPos blockPos) {
      return isAir(mc.world.getBlockState(blockPos).getBlock());
   }

   public static boolean isAirBlock(BlockPos blockPos) {
      return isAirBlock(mc.world.getBlockState(blockPos).getBlock());
   }

   public static boolean raytraceCheck(BlockPos pos, float height) {
      return mc.world
            .rayTraceBlocks(
               new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
               new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()),
               false,
               true,
               false
            )
         == null;
   }

   public static boolean canBePlace(BlockPos pos) {
      return !checkPlayer(pos) && canReplace(pos);
   }

   public static boolean canBePlace(BlockPos pos, double distance) {
      return mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > distance
         ? false
         : !checkPlayer(pos) && canReplace(pos);
   }

   public static boolean checkPlayer(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)
            && !(entity instanceof EntityEnderCrystal)) {
            return true;
         }
      }

      return false;
   }

   public static EnumFacing getBestNeighboring(BlockPos pos, EnumFacing facing) {
      for (EnumFacing i : EnumFacing.VALUES) {
         if ((facing == null || !pos.offset(i).equals(pos.offset(facing, -1))) && i != EnumFacing.DOWN) {
            for (EnumFacing side : getPlacableFacings(pos.offset(i), true, true)) {
               if (canClick(pos.offset(i).offset(side))) {
                  return i;
               }
            }
         }
      }

      EnumFacing bestFacing = null;
      double distance = 0.0;

      for (EnumFacing ix : EnumFacing.VALUES) {
         if ((facing == null || !pos.offset(ix).equals(pos.offset(facing, -1))) && ix != EnumFacing.DOWN) {
            for (EnumFacing sidex : getPlacableFacings(pos.offset(ix), true, false)) {
               if (canClick(pos.offset(ix).offset(sidex))
                  && (bestFacing == null || mc.player.getDistanceSq(pos.offset(ix)) < distance)) {
                  bestFacing = ix;
                  distance = mc.player.getDistanceSq(pos.offset(ix));
               }
            }
         }
      }

      return null;
   }

   public static double distanceToXZ(double x, double z) {
      double dx = mc.player.posX - x;
      double dz = mc.player.posZ - z;
      return Math.sqrt(dx * dx + dz * dz);
   }

   public static void placeBlock(BlockPos pos, boolean rotate, boolean packet, boolean strict, boolean raytrace, boolean swing) {
      placeBlock(pos, EnumHand.MAIN_HAND, rotate, packet, strict, raytrace, swing);
   }

   public static void placeBlock(
      BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean attackEntity, boolean strict, boolean raytrace, boolean swing
   ) {
      if (attackEntity) {
         CrystalUtil.breakCrystal(pos, swing);
      }

      placeBlock(pos, hand, rotate, packet, strict, raytrace, swing);
   }

   public static boolean canBlockFacing(BlockPos pos) {
      boolean airCheck = false;

      for (EnumFacing side : EnumFacing.values()) {
         if (canClick(pos.offset(side))) {
            airCheck = true;
         }
      }

      return airCheck;
   }

   public static boolean canBlockFacing(BlockPos pos, BlockPos check) {
      boolean airCheck = false;

      for (EnumFacing side : EnumFacing.values()) {
         if (canClick(pos.offset(side)) && !isPos2(pos.offset(side), check)) {
            airCheck = true;
         }
      }

      return airCheck;
   }

   public static boolean strictPlaceCheck(BlockPos pos, boolean strict, boolean raytrace) {
      if (!strict) {
         return true;
      } else {
         for (EnumFacing side : getPlacableFacings(pos, true, raytrace)) {
            if (canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean strictPlaceCheck(BlockPos pos, boolean strict, boolean raytrace, BlockPos check) {
      if (!strict) {
         return true;
      } else {
         for (EnumFacing side : getPlacableFacings(pos, true, raytrace)) {
            if (canClick(pos.offset(side)) && !isPos2(pos.offset(side), check)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   public static boolean canClick(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock().canCollideCheck(mc.world.getBlockState(pos), false);
   }

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
         EntityUtil.faceVector(vec);
      }

      mc.player
         .connection
         .sendPacket(new CPacketPlayerTryUseItemOnBlock(obsPos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
      mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
   }

   public static boolean canPlaceCrystal(BlockPos pos, double distance) {
      if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > distance) {
         return false;
      } else {
         BlockPos obsPos = pos.down();
         BlockPos boost = obsPos.up();
         BlockPos boost2 = obsPos.up(2);
         return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
            && getBlock(boost) == Blocks.AIR
            && getBlock(boost2) == Blocks.AIR
            && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
            && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
      }
   }

   public static boolean canPlaceCrystal(BlockPos pos) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      BlockPos boost2 = obsPos.up(2);
      return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
         && getBlock(boost) == Blocks.AIR
         && getBlock(boost2) == Blocks.AIR
         && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
         && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
   }

   public static List<EnumFacing> getPlacableFacings(BlockPos pos, boolean strictDirection, boolean rayTrace) {
      ArrayList<EnumFacing> validFacings = new ArrayList<>();

      for (EnumFacing side : EnumFacing.values()) {
         if (!getRaytrace(pos, side)) {
            getPlaceFacing(pos, strictDirection, validFacings, side);
         }
      }

      for (EnumFacing sidex : EnumFacing.values()) {
         if (!rayTrace || !getRaytrace(pos, sidex)) {
            getPlaceFacing(pos, strictDirection, validFacings, sidex);
         }
      }

      return validFacings;
   }

   public static List<EnumFacing> getTrapPlacableFacings(BlockPos pos, boolean strictDirection, boolean rayTrace) {
      ArrayList<EnumFacing> validFacings = new ArrayList<>();

      for (EnumFacing side : facing) {
         if (!getRaytrace(pos, side)) {
            getPlaceFacing(pos, strictDirection, validFacings, side);
         }
      }

      for (EnumFacing sidex : facing) {
         if (!rayTrace || !getRaytrace(pos, sidex)) {
            getPlaceFacing(pos, strictDirection, validFacings, sidex);
         }
      }

      return validFacings;
   }

   private static boolean getRaytrace(BlockPos pos, EnumFacing side) {
      Vec3d testVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
      RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0F), testVec);
      return result != null && result.typeOfHit != Type.MISS;
   }

   private static void getPlaceFacing(BlockPos pos, boolean strictDirection, ArrayList<EnumFacing> validFacings, EnumFacing side) {
      BlockPos neighbour = pos.offset(side);
      if (strictDirection) {
         Vec3d eyePos = mc.player.getPositionEyes(1.0F);
         Vec3d blockCenter = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5);
         IBlockState blockState2 = mc.world.getBlockState(neighbour);
         boolean isFullBox = blockState2.getBlock() == Blocks.AIR || blockState2.isFullBlock();
         ArrayList<EnumFacing> validAxis = new ArrayList<>();
         validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
         validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
         validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
         if (!validAxis.contains(side.getOpposite())) {
            return;
         }
      }

      IBlockState blockState;
      if ((blockState = mc.world.getBlockState(neighbour)).getBlock().canCollideCheck(blockState, false)
         && !blockState.getMaterial().isReplaceable()) {
         validFacings.add(side);
      }
   }

   public static ArrayList<EnumFacing> checkAxis(double diff, EnumFacing negativeSide, EnumFacing positiveSide, boolean bothIfInRange) {
      ArrayList<EnumFacing> valid = new ArrayList<>();
      if (diff < -0.5) {
         valid.add(negativeSide);
      }

      if (diff > 0.5) {
         valid.add(positiveSide);
      }

      if (bothIfInRange) {
         if (!valid.contains(negativeSide)) {
            valid.add(negativeSide);
         }

         if (!valid.contains(positiveSide)) {
            valid.add(positiveSide);
         }
      }

      return valid;
   }

   public static boolean canPlaceEnum(BlockPos pos, boolean strict, boolean raytrace) {
      return !canBlockFacing(pos) ? false : strictPlaceCheck(pos, strict, raytrace);
   }

   public static boolean checkEntity(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)) {
            return true;
         }
      }

      return false;
   }

   public static boolean canPlace(BlockPos pos, double distance, boolean strict, boolean raytrace) {
      if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > distance) {
         return false;
      } else if (!canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !strictPlaceCheck(pos, strict, raytrace) ? false : !checkEntity(pos);
      }
   }

   public static boolean canPlace(BlockPos pos, boolean strict, boolean raytrace) {
      if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 6.0) {
         return false;
      } else if (!canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !strictPlaceCheck(pos, strict, raytrace) ? false : !checkEntity(pos);
      }
   }

   public static boolean canPlaceWithoutBase(BlockPos pos, boolean strict, boolean raytrace, boolean base) {
      if (!base && !canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !base && !strictPlaceCheck(pos, strict, raytrace) ? false : !checkEntity(pos);
      }
   }

   public static boolean canPlaceWithoutBase(BlockPos pos, boolean strict, boolean raytrace, boolean base, BlockPos check) {
      if (!base && !canBlockFacing(pos, check)) {
         return false;
      } else {
         return !canReplace(pos) ? false : base || strictPlaceCheck(pos, strict, raytrace, check);
      }
   }

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean strict, boolean raytrace, boolean swing) {
      EnumFacing side = getFirstFacing(pos, strict, raytrace);
      if (side != null) {
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         boolean sneaking = false;
         if (!ColorMain.INSTANCE.sneaking && blackList.contains(getBlock(neighbour))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            sneaking = true;
         }

         if (rotate) {
            faceVector(hitVec);
         }

         rightClickBlock(neighbour, hitVec, hand, opposite, packet, swing);
         if (sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }
      }
   }

   public static boolean placeBlockBoolean(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean strict, boolean raytrace, boolean swing) {
      EnumFacing side = getFirstFacing(pos, strict, raytrace);
      if (side == null) {
         return false;
      } else {
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         boolean sneaking = false;
         if (!ColorMain.INSTANCE.sneaking && blackList.contains(getBlock(neighbour))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            sneaking = true;
         }

         if (rotate) {
            faceVector(hitVec);
         }

         rightClickBlock(neighbour, hitVec, hand, opposite, packet, swing);
         if (sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }

         return true;
      }
   }

   public static void faceVector(Vec3d vec) {
      float[] rotations = EntityUtil.getLegitRotations(vec);
      EntityUtil.sendPlayerRot(rotations[0], rotations[1], mc.player.onGround);
   }

   public static boolean posHasCrystal(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityEnderCrystal && new BlockPos(entity.posX, entity.posY, entity.posZ).equals(pos)) {
            return true;
         }
      }

      return false;
   }

   public static boolean canReplace(BlockPos pos) {
      return pos == null ? false : getState(pos).getMaterial().isReplaceable() || isAir(pos);
   }

   public static boolean canReplace(Vec3d vec3d) {
      if (vec3d == null) {
         return false;
      } else {
         BlockPos pos = new BlockPos(vec3d);
         return getState(pos).getMaterial().isReplaceable() || isAir(pos);
      }
   }

   public static boolean isBlockUnSolid(Block block) {
      return unSolidBlocks.contains(block);
   }

   public static boolean canOpen(Block block) {
      return blackList.contains(block);
   }

   public static boolean isAir(Block block) {
      return airBlocks.contains(block);
   }

   public static boolean isAirBlock(Block block) {
      return block == Blocks.AIR;
   }

   public static double blockDistance2d(double blockposx, double blockposz, Entity owo) {
      double deltaX = owo.posX - blockposx;
      double deltaZ = owo.posZ - blockposz;
      return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
   }

   public static EnumFacing getRayTraceFacing(BlockPos pos) {
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5)
         );
      return result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
   }

   public static EnumFacing getRayTraceFacing(BlockPos pos, EnumFacing facing) {
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5)
         );
      return result != null && result.sideHit != null ? result.sideHit : facing;
   }

   public static IBlockState getState(BlockPos pos) {
      return mc.world.getBlockState(pos);
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

   public static Rotation getFaceVectorPacket(Vec3d vec, Boolean roundAngles) {
      float[] rotations = getNeededRotations2(vec);
      Rotation e = new Rotation(rotations[0], roundAngles ? MathHelper.normalizeAngle((int)rotations[1], 360) : rotations[1], mc.player.onGround);
      mc.player.connection.sendPacket(e);
      return e;
   }

   public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difZ = to.z - from.z;
      return new float[]{(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)};
   }

   public static BlockPos[] toBlockPos(Vec3d[] vec3ds) {
      BlockPos[] list = new BlockPos[vec3ds.length];

      for (int i = 0; i < vec3ds.length; i++) {
         list[i] = new BlockPos(vec3ds[i]);
      }

      return list;
   }

   public static boolean hasNeighbour(BlockPos blockPos) {
      boolean canPlace = false;

      for (EnumFacing side : EnumFacing.values()) {
         BlockPos neighbour = blockPos.offset(side);
         if (mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
            canPlace = true;
         }
      }

      return canPlace;
   }

   public static boolean canPlaceBlock(BlockPos pos) {
      return (getBlock(pos) == Blocks.AIR || getBlock(pos) instanceof BlockLiquid) && hasNeighbour(pos) && !blackList.contains(getBlock(pos));
   }

   public static boolean canPlaceBlockFuture(BlockPos pos) {
      return (getBlock(pos) == Blocks.AIR || getBlock(pos) instanceof BlockLiquid) && !blackList.contains(getBlock(pos));
   }

   public static void rightClickBlock(BlockPos pos, EnumFacing facing, boolean packet) {
      Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (packet) {
         rightClickBlock(pos, hitVec, EnumHand.MAIN_HAND, facing);
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND);
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void rightClickBlock(BlockPos pos, EnumFacing facing, Vec3d hVec, boolean packet) {
      Vec3d hitVec = new Vec3d(pos).add(hVec).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (packet) {
         rightClickBlock(pos, hitVec, EnumHand.MAIN_HAND, facing);
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND);
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction) {
      float f = (float)(vec.x - pos.getX());
      float f1 = (float)(vec.y - pos.getY());
      float f2 = (float)(vec.z - pos.getZ());
      mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
      mc.rightClickDelayTimer = 4;
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet, boolean swing) {
      if (packet) {
         float f = (float)(vec.x - pos.getX());
         float f1 = (float)(vec.y - pos.getY());
         float f2 = (float)(vec.z - pos.getZ());
         mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
      }

      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }

      mc.rightClickDelayTimer = 4;
   }

   public static int isPositionPlaceable(BlockPos pos, boolean rayTrace) {
      return isPositionPlaceable(pos, rayTrace, true);
   }

   public static EnumFacing getFirstFacing(BlockPos pos, boolean strict, boolean raytrace) {
      if (!strict) {
         Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
         if (iterator.hasNext()) {
            return iterator.next();
         }
      } else {
         for (EnumFacing side : getPlacableFacings(pos, true, raytrace)) {
            if (canClick(pos.offset(side))) {
               return side;
            }
         }
      }

      return null;
   }

   public static EnumFacing getTrapFirstFacing(BlockPos pos, boolean strict, boolean raytrace) {
      if (!strict) {
         Iterator<EnumFacing> iterator = getTrapPossibleSides(pos).iterator();
         if (iterator.hasNext()) {
            return iterator.next();
         }
      } else {
         for (EnumFacing side : getTrapPlacableFacings(pos, true, raytrace)) {
            if (canClick(pos.offset(side))) {
               return side;
            }
         }
      }

      return null;
   }

   public static int isPositionPlaceable(BlockPos pos, boolean rayTrace, boolean entityCheck) {
      Block block = mc.world.getBlockState(pos).getBlock();
      if (!(block instanceof BlockAir)
         && !(block instanceof BlockLiquid)
         && !(block instanceof BlockTallGrass)
         && !(block instanceof BlockFire)
         && !(block instanceof BlockDeadBush)
         && !(block instanceof BlockSnow)) {
         return 0;
      } else if (!rayTracePlaceCheck(pos, rayTrace, 0.0F)) {
         return -1;
      } else {
         if (entityCheck) {
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
               if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                  return 1;
               }
            }
         }

         for (EnumFacing side : getPossibleSides(pos)) {
            if (canBeClicked(pos.offset(side))) {
               return 3;
            }
         }

         return 2;
      }
   }

   public static List<EnumFacing> getPossibleSides(BlockPos pos) {
      List<EnumFacing> facings = new ArrayList<>();
      if (mc.world != null && pos != null) {
         for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbour = pos.offset(side);
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (blockState.getBlock().canCollideCheck(blockState, false) && !blockState.getMaterial().isReplaceable() && canBeClicked(neighbour)) {
               facings.add(side);
            }
         }

         return facings;
      } else {
         return facings;
      }
   }

   public static List<EnumFacing> getTrapPossibleSides(BlockPos pos) {
      List<EnumFacing> facings = new ArrayList<>();
      if (mc.world != null && pos != null) {
         for (EnumFacing side : facing) {
            BlockPos neighbour = pos.offset(side);
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (blockState != null && blockState.getBlock().canCollideCheck(blockState, false) && !blockState.getMaterial().isReplaceable()) {
               facings.add(side);
            }
         }

         return facings;
      } else {
         return facings;
      }
   }

   public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
      return !shouldCheck
         || mc.world
               .rayTraceBlocks(
                  new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                  new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()),
                  false,
                  true,
                  false
               )
            == null;
   }

   public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck) {
      return rayTracePlaceCheck(pos, shouldCheck, 1.0F);
   }

   public static boolean rayTracePlaceCheck(BlockPos pos) {
      return rayTracePlaceCheck(pos, true);
   }

   public static Block getBlock(BlockPos pos) {
      return getState(pos).getBlock();
   }

   public static Block getBlock(double x, double y, double z) {
      return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
   }

   public static boolean canBeClicked(BlockPos pos) {
      return getBlock(pos).canCollideCheck(getState(pos), false);
   }

   public static boolean canBeClicked(Vec3d vec3d) {
      return getBlock(new BlockPos(vec3d)).canCollideCheck(getState(new BlockPos(vec3d)), false);
   }

   public static void faceVectorPacketInstant(Vec3d vec, Boolean roundAngles) {
      float[] rotations = getNeededRotations2(vec);
      mc.player
         .connection
         .sendPacket(
            new Rotation(rotations[0], roundAngles ? MathHelper.normalizeAngle((int)rotations[1], 360) : rotations[1], mc.player.onGround)
         );
   }

   public static void faceVectorPacketInstant2(Vec3d vec) {
      float[] rotations = getLegitRotations(vec);
      Wrapper.getPlayer().connection.sendPacket(new Rotation(rotations[0], rotations[1], Wrapper.getPlayer().onGround));
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
         Wrapper.getPlayer().rotationYaw + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().rotationYaw),
         Wrapper.getPlayer().rotationPitch + MathHelper.wrapDegrees(pitch - Wrapper.getPlayer().rotationPitch)
      };
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

   public static double blockDistance(double blockposx, double blockposy, double blockposz, Entity owo) {
      double deltaX = owo.posX - blockposx;
      double deltaY = owo.posY - blockposy;
      double deltaZ = owo.posZ - blockposz;
      return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
   }

   public static List<BlockPos> getCircle(BlockPos loc, int y, float r, boolean hollow) {
      List<BlockPos> circleblocks = new ArrayList<>();
      int cx = loc.getX();
      int cz = loc.getZ();

      for (int x = cx - (int)r; x <= cx + r; x++) {
         for (int z = cz - (int)r; z <= cz + r; z++) {
            double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z);
            if (dist < r * r && (!hollow || dist >= (r - 1.0F) * (r - 1.0F))) {
               BlockPos l = new BlockPos(x, y, z);
               circleblocks.add(l);
            }
         }
      }

      return circleblocks;
   }

   public static EnumFacing getPlaceableSide(BlockPos pos) {
      for (EnumFacing side : EnumFacing.values()) {
         BlockPos neighbour = pos.offset(side);
         if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable()) {
               return side;
            }
         }
      }

      return null;
   }

   public static EnumFacing getPlaceableSideExlude(BlockPos pos, ArrayList<EnumFacing> excluding) {
      for (EnumFacing side : EnumFacing.values()) {
         if (!excluding.contains(side)) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
               IBlockState blockState = mc.world.getBlockState(neighbour);
               if (!blockState.getMaterial().isReplaceable()) {
                  return side;
               }
            }
         }
      }

      return null;
   }

   public static Vec3d getCenterOfBlock(double playerX, double playerY, double playerZ) {
      double newX = Math.floor(playerX) + 0.5;
      double newY = Math.floor(playerY);
      double newZ = Math.floor(playerZ) + 0.5;
      return new Vec3d(newX, newY, newZ);
   }
}
