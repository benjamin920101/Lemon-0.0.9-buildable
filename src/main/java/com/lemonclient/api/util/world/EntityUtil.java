package com.lemonclient.api.util.world;

import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.misc.Wrapper;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

public class EntityUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final Vec3d[] antiDropOffsetList = new Vec3d[]{new Vec3d(0.0, -2.0, 0.0)};
   public static final Vec3d[] platformOffsetList = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0), new Vec3d(0.0, -1.0, -1.0), new Vec3d(0.0, -1.0, 1.0), new Vec3d(-1.0, -1.0, 0.0), new Vec3d(1.0, -1.0, 0.0)
   };
   public static final Vec3d[] legOffsetList = new Vec3d[]{
      new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0)
   };
   public static final Vec3d[] OffsetList = new Vec3d[]{
      new Vec3d(1.0, 1.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(0.0, 2.0, 0.0)
   };
   public static final Vec3d[] antiStepOffsetList = new Vec3d[]{
      new Vec3d(-1.0, 2.0, 0.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(0.0, 2.0, -1.0)
   };
   public static final Vec3d[] antiScaffoldOffsetList = new Vec3d[]{new Vec3d(0.0, 3.0, 0.0)};
   public static final Vec3d[] doubleLegOffsetList = new Vec3d[]{
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0),
      new Vec3d(-2.0, 0.0, 0.0),
      new Vec3d(2.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -2.0),
      new Vec3d(0.0, 0.0, 2.0)
   };

   public static void faceXYZ(double x, double y, double z) {
      faceYawAndPitch(getXYZYaw(x, y, z), getXYZPitch(x, y, z));
   }

   public static float getXYZYaw(double x, double y, double z) {
      float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(x, y, z));
      return angle[0];
   }

   public static boolean stopSneaking(boolean isSneaking) {
      if (isSneaking && mc.player != null) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      }

      return false;
   }

   public static int getDamagePercent(ItemStack stack) {
      return (int)((stack.getMaxDamage() - stack.getItemDamage()) / Math.max(0.1, (double)stack.getMaxDamage()) * 100.0);
   }

   public static void faceVector(Vec3d vec) {
      float[] rotations = getLegitRotations(vec);
      sendPlayerRot(rotations[0], rotations[1], mc.player.onGround);
   }

   public static void facePosFacing(BlockPos pos, EnumFacing side) {
      Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
      faceVector(hitVec);
   }

   public static void facePlacePos(BlockPos pos, boolean strict, boolean raytrace) {
      EnumFacing side = BlockUtil.getFirstFacing(pos, strict, raytrace);
      if (side != null) {
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         BlockUtil.faceVector(hitVec);
      }
   }

   public static float getXYZPitch(double x, double y, double z) {
      float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(x, y, z));
      return angle[1];
   }

   public static void faceYawAndPitch(float yaw, float pitch) {
      sendPlayerRot(yaw, pitch, mc.player.onGround);
   }

   public static void sendPlayerRot(float yaw, float pitch, boolean onGround) {
      mc.player.connection.sendPacket(new Rotation(yaw, pitch, onGround));
   }

   public static float[] getLegitRotations(Vec3d vec) {
      Vec3d eyesPos = BlockUtil.getEyesPos();
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

   public static Vec2f getRotations(Vec3d vec) {
      Vec3d eyesPos = BlockUtil.getEyesPos();
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new Vec2f(
         mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
         mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
      );
   }

   public static boolean isEating() {
      if (mc.world != null && mc.player != null && mc.player.ticksExisted > 20) {
         RayTraceResult result = mc.objectMouseOver;
         if (result != null && result.typeOfHit == Type.BLOCK) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();
            if (BlockUtil.blackList.contains(BlockUtil.getBlock(pos)) && !ColorMain.INSTANCE.sneaking) {
               return false;
            }
         }

         return mc.player.isHandActive()
            && (mc.player.getActiveItemStack().getItem() instanceof ItemFood || mc.player.getHeldItemMainhand().getItem() instanceof ItemFood);
      } else {
         return false;
      }
   }

   public static boolean invalid(Entity entity, double range) {
      return entity == null
         || isDead(entity)
         || entity.equals(mc.player)
         || entity instanceof EntityPlayer && SocialManager.isFriend(entity.getName())
         || mc.player.getDistanceSq(entity) > MathUtil.square(range);
   }

   public static BlockPos getEntityPos(Entity target) {
      return new BlockPos(target.posX, target.posY + 0.5, target.posZ);
   }

   public static boolean isLiving(Entity entity) {
      return entity instanceof EntityLivingBase;
   }

   public static Vec3d[] getVarOffsets(int x, int y, int z) {
      List<Vec3d> offsets = getVarOffsetList(x, y, z);
      Vec3d[] array = new Vec3d[offsets.size()];
      return offsets.toArray(array);
   }

   public static BlockPos getPlayerPos(EntityPlayer player) {
      return player == null ? null : new BlockPos(Math.floor(player.posX), Math.floor(player.posY) + 0.5, Math.floor(player.posZ));
   }

   public static List<Vec3d> getVarOffsetList(int x, int y, int z) {
      ArrayList<Vec3d> offsets = new ArrayList<>();
      offsets.add(new Vec3d(x, y, z));
      return offsets;
   }

   public static BlockPos getRoundedBlockPos(Entity entity) {
      return new BlockPos(MathUtil.roundVec(entity.lastPortalVec, 0));
   }

   public static boolean isAlive(Entity entity) {
      return isLiving(entity) && !entity.isDead && ((EntityLivingBase)entity).getHealth() > 0.0F;
   }

   public static boolean isOnLiquid() {
      double y = mc.player.posY - 0.03;

      for (int x = MathHelper.floor(mc.player.posX); x < MathHelper.ceil(mc.player.posX); x++) {
         for (int z = MathHelper.floor(mc.player.posZ); z < MathHelper.ceil(mc.player.posZ); z++) {
            BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
            if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isDead(Entity entity) {
      return !isAlive(entity);
   }

   public static float getHealth(Entity entity) {
      if (isLiving(entity)) {
         EntityLivingBase livingBase = (EntityLivingBase)entity;
         return livingBase.getHealth() + livingBase.getAbsorptionAmount();
      } else {
         return 0.0F;
      }
   }

   public static boolean isPassive(Entity e) {
      if (e instanceof EntityWolf && ((EntityWolf)e).isAngry()) {
         return false;
      } else {
         return !(e instanceof EntityAgeable) && !(e instanceof EntityAmbientCreature) && !(e instanceof EntitySquid)
            ? e instanceof EntityIronGolem && ((EntityIronGolem)e).getRevengeTarget() == null
            : true;
      }
   }

   public static Vec3d[] getOffsets(int y, boolean floor, boolean face) {
      List<Vec3d> offsets = getOffsetList(y, floor, face);
      Vec3d[] array = new Vec3d[offsets.size()];
      return offsets.toArray(array);
   }

   public static boolean isSafe(Entity entity, int height, boolean floor) {
      return getUnsafeBlocks(entity, height, floor).size() == 0;
   }

   public static Vec3d[] getUnsafeBlockArray(Entity entity, int height, boolean floor) {
      List<Vec3d> list = getUnsafeBlocks(entity, height, floor);
      Vec3d[] array = new Vec3d[list.size()];
      return list.toArray(array);
   }

   public static List<Vec3d> getUnsafeBlocks(Entity entity, int height, boolean floor) {
      return getUnsafeBlocksFromVec3d(entity.getPositionVector(), height, floor);
   }

   public static Vec3d[] getUnsafeBlockArrayFromVec3d(Vec3d pos, int height, boolean floor) {
      List<Vec3d> list = getUnsafeBlocksFromVec3d(pos, height, floor);
      Vec3d[] array = new Vec3d[list.size()];
      return list.toArray(array);
   }

   public static List<Vec3d> getUnsafeBlocksFromVec3d(Vec3d pos, int height, boolean floor) {
      ArrayList<Vec3d> vec3ds = new ArrayList<>();

      for (Vec3d vector : getOffsets(height, floor)) {
         BlockPos targetPos = new BlockPos(pos).add(vector.x, vector.y, vector.z);
         Block block = mc.world.getBlockState(targetPos).getBlock();
         if (block instanceof BlockAir
            || block instanceof BlockLiquid
            || block instanceof BlockTallGrass
            || block instanceof BlockFire
            || block instanceof BlockDeadBush
            || block instanceof BlockSnow) {
            vec3ds.add(vector);
         }
      }

      return vec3ds;
   }

   public static List<Vec3d> getOffsetList(int y, boolean floor) {
      ArrayList<Vec3d> offsets = new ArrayList<>();
      offsets.add(new Vec3d(-1.0, y, 0.0));
      offsets.add(new Vec3d(1.0, y, 0.0));
      offsets.add(new Vec3d(0.0, y, -1.0));
      offsets.add(new Vec3d(0.0, y, 1.0));
      if (floor) {
         offsets.add(new Vec3d(0.0, y - 1, 0.0));
      }

      return offsets;
   }

   public static Vec3d[] getOffsets(int y, boolean floor) {
      List<Vec3d> offsets = getOffsetList(y, floor);
      Vec3d[] array = new Vec3d[offsets.size()];
      return offsets.toArray(array);
   }

   public static List<Vec3d> getOffsetList(int y, boolean floor, boolean face) {
      ArrayList<Vec3d> offsets = new ArrayList<>();
      if (face) {
         offsets.add(new Vec3d(-1.0, y, 0.0));
         offsets.add(new Vec3d(1.0, y, 0.0));
         offsets.add(new Vec3d(0.0, y, -1.0));
         offsets.add(new Vec3d(0.0, y, 1.0));
      } else {
         offsets.add(new Vec3d(-1.0, y, 0.0));
      }

      if (floor) {
         offsets.add(new Vec3d(0.0, y - 1, 0.0));
      }

      return offsets;
   }

   public static Vec3d interpolateEntity(Entity entity, float time) {
      return new Vec3d(
         entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
         entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
         entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time
      );
   }

   public static Block isColliding(double posX, double posY, double posZ) {
      Block block = null;
      if (mc.player != null) {
         AxisAlignedBB bb = mc.player.getRidingEntity() != null
            ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0, 0.0, 0.0).offset(posX, posY, posZ)
            : mc.player.getEntityBoundingBox().contract(0.0, 0.0, 0.0).offset(posX, posY, posZ);
         int y = (int)bb.minY;

         for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
               block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
            }
         }
      }

      return block;
   }

   public static boolean isPlayerValid(EntityPlayer player, float range) {
      return player != mc.player
         && mc.player.getDistance(player) < range
         && !player.isDead
         && !SocialManager.isFriend(player.getName());
   }

   public static boolean isInLiquid() {
      if (mc.player == null) {
         return false;
      } else if (mc.player.fallDistance >= 3.0F) {
         return false;
      } else {
         boolean inLiquid = false;
         AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
         int y = (int)bb.minY;

         for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
               Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
               if (!(block instanceof BlockAir)) {
                  if (!(block instanceof BlockLiquid)) {
                     return false;
                  }

                  inLiquid = true;
               }
            }
         }

         return inLiquid;
      }
   }

   public static void setTimer(float speed) {
      TimerUtils.setTickLength(50.0F / speed);
   }

   public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
      return getInterpolatedAmount(entity, ticks, ticks, ticks);
   }

   public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
      return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
   }

   public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
      return new Vec3d(
         (entity.posX - entity.lastTickPosX) * x,
         (entity.posY - entity.lastTickPosY) * y,
         (entity.posZ - entity.lastTickPosZ) * z
      );
   }

   public static float clamp(float val, float min, float max) {
      if (val <= min) {
         val = min;
      }

      if (val >= max) {
         val = max;
      }

      return val;
   }

   public static List<BlockPos> getSphere(BlockPos loc, Double r, Double h, boolean hollow, boolean sphere, int plus_y) {
      List<BlockPos> circleBlocks = new ArrayList<>();
      double cx = loc.getX();
      double cy = loc.getY();
      double cz = loc.getZ();

      for (double x = cx - r; x <= cx + r; x++) {
         for (double z = cz - r; z <= cz + r; z++) {
            for (double y = sphere ? cy - r : cy - h; y < (sphere ? cy + r : cy + h); y++) {
               double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0.0);
               if (dist < r * r && (!hollow || !(dist < (r - 1.0) * (r - 1.0)))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleBlocks.add(l);
               }
            }
         }
      }

      return circleBlocks;
   }

   public static List<BlockPos> getFlatSphere(BlockPos loc, Double r, Double h, boolean hollow, boolean sphere, int plus_y) {
      List<BlockPos> circleBlocks = new ArrayList<>();
      double cx = loc.getX();
      double cy = loc.getY();
      double cz = loc.getZ();

      for (double y = sphere ? cy - r : cy - h; y < (sphere ? cy + r : cy + h); y++) {
         for (double x = cx - r; x <= cx + r; x++) {
            for (double z = cz - r; z <= cz + r; z++) {
               double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0.0);
               if (dist < r * r && (!hollow || !(dist < (r - 1.0) * (r - 1.0)))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleBlocks.add(l);
               }
            }
         }
      }

      return circleBlocks;
   }

   public static List<BlockPos> getSquare(BlockPos pos1, BlockPos pos2) {
      List<BlockPos> squareBlocks = new ArrayList<>();
      int x1 = pos1.getX();
      int y1 = pos1.getY();
      int z1 = pos1.getZ();
      int x2 = pos2.getX();
      int y2 = pos2.getY();
      int z2 = pos2.getZ();

      for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
         for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
               squareBlocks.add(new BlockPos(x, y, z));
            }
         }
      }

      return squareBlocks;
   }

   public static double[] calculateLookAt(double px, double py, double pz, Entity me) {
      double dirx = me.posX - px;
      double diry = me.posY - py;
      double dirz = me.posZ - pz;
      double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
      dirx /= len;
      diry /= len;
      dirz /= len;
      double pitch = Math.asin(diry);
      double yaw = Math.atan2(dirz, dirx);
      pitch = pitch * 180.0 / Math.PI;
      yaw = yaw * 180.0 / Math.PI;
      yaw += 90.0;
      return new double[]{yaw, pitch};
   }

   public static boolean basicChecksEntity(EntityPlayer pl) {
      return pl == null
         || pl.getName().equals(mc.player.getName())
         || SocialManager.isFriend(pl.getName())
         || pl.isDead
         || pl.getHealth() + pl.getAbsorptionAmount() <= 0.0F
         || pl.isCreative();
   }

   public static BlockPos getPosition(Entity pl) {
      return new BlockPos(Math.floor(pl.posX), Math.floor(pl.posY + 0.5), Math.floor(pl.posZ));
   }

   public static List<BlockPos> getBlocksIn(Entity pl) {
      List<BlockPos> blocks = new ArrayList<>();
      AxisAlignedBB bb = pl.getEntityBoundingBox();

      for (double x = Math.floor(bb.minX); x < Math.ceil(bb.maxX); x++) {
         for (double y = Math.floor(bb.minY); y < Math.ceil(bb.maxY); y++) {
            for (double z = Math.floor(bb.minZ); z < Math.ceil(bb.maxZ); z++) {
               blocks.add(new BlockPos(x, y, z));
            }
         }
      }

      return blocks;
   }

   public static boolean isMobAggressive(Entity entity) {
      if (entity instanceof EntityPigZombie) {
         if (((EntityPigZombie)entity).isArmsRaised() || ((EntityPigZombie)entity).isAngry()) {
            return true;
         }
      } else {
         if (entity instanceof EntityWolf) {
            return ((EntityWolf)entity).isAngry() && !Wrapper.getPlayer().equals(((EntityWolf)entity).getOwner());
         }

         if (entity instanceof EntityEnderman) {
            return ((EntityEnderman)entity).isScreaming();
         }
      }

      return isHostileMob(entity);
   }

   public static boolean isNeutralMob(Entity entity) {
      return entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman;
   }

   public static boolean isFriendlyMob(Entity entity) {
      return entity.isCreatureType(EnumCreatureType.CREATURE, false) && !isNeutralMob(entity)
         || entity.isCreatureType(EnumCreatureType.AMBIENT, false)
         || entity instanceof EntityVillager
         || entity instanceof EntityIronGolem
         || isNeutralMob(entity) && !isMobAggressive(entity);
   }

   public static boolean isHostileMob(Entity entity) {
      return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !isNeutralMob(entity);
   }

   public static List<Vec3d> targets(Vec3d vec3d, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace) {
      ArrayList<Vec3d> placeTargets = new ArrayList<>();
      if (antiDrop) {
         Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiDropOffsetList));
      }

      if (platform) {
         Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, platformOffsetList));
      }

      if (legs) {
         Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, legOffsetList));
      }

      Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, OffsetList));
      if (antiStep) {
         Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiStepOffsetList));
      } else {
         List<Vec3d> vec3ds = getUnsafeBlocksFromVec3d(vec3d, 2, false);
         if (vec3ds.size() == 4) {
            for (Vec3d vector : vec3ds) {
               BlockPos position = new BlockPos(vec3d).add(vector.x, vector.y, vector.z);
               switch (BlockUtil.isPositionPlaceable(position, raytrace)) {
                  case -1:
                  case 1:
                  case 2:
                     break;
                  case 3:
                     placeTargets.add(vec3d.add(vector));
                  case 0:
                  default:
                     if (antiScaffold) {
                        Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList));
                     }

                     return placeTargets;
               }
            }
         }
      }

      if (antiScaffold) {
         Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList));
      }

      return placeTargets;
   }

   public static boolean isTrapped(EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
      return getUntrappedBlocks(player, antiScaffold, antiStep, legs, platform, antiDrop).isEmpty();
   }

   public static boolean isTrappedExtended(
      int extension, EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace
   ) {
      return getUntrappedBlocksExtended(extension, player, antiScaffold, antiStep, legs, platform, antiDrop, raytrace).isEmpty();
   }

   public static List<Vec3d> getUntrappedBlocks(EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
      ArrayList<Vec3d> vec3ds = new ArrayList<>();
      if (!antiStep && getUnsafeBlocks(player, 2, false).size() == 4) {
         vec3ds.addAll(getUnsafeBlocks(player, 2, false));
      }

      for (int i = 0; i < getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop).length; i++) {
         Vec3d vector = getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop)[i];
         BlockPos targetPos = new BlockPos(player.getPositionVector()).add(vector.x, vector.y, vector.z);
         Block block = mc.world.getBlockState(targetPos).getBlock();
         if (block instanceof BlockAir
            || block instanceof BlockLiquid
            || block instanceof BlockTallGrass
            || block instanceof BlockFire
            || block instanceof BlockDeadBush
            || block instanceof BlockSnow) {
            vec3ds.add(vector);
         }
      }

      return vec3ds;
   }

   public static List<Vec3d> getBlockBlocks(Entity entity) {
      ArrayList<Vec3d> vec3ds = new ArrayList<>();
      AxisAlignedBB bb = entity.getEntityBoundingBox();
      double y = entity.posY;
      double minX = MathUtil.round(bb.minX, 0);
      double minZ = MathUtil.round(bb.minZ, 0);
      double maxX = MathUtil.round(bb.maxX, 0);
      double maxZ = MathUtil.round(bb.maxZ, 0);
      if (minX != maxX) {
         vec3ds.add(new Vec3d(minX, y, minZ));
         vec3ds.add(new Vec3d(maxX, y, minZ));
         if (minZ != maxZ) {
            vec3ds.add(new Vec3d(minX, y, maxZ));
            vec3ds.add(new Vec3d(maxX, y, maxZ));
            return vec3ds;
         }
      } else if (minZ != maxZ) {
         vec3ds.add(new Vec3d(minX, y, minZ));
         vec3ds.add(new Vec3d(minX, y, maxZ));
         return vec3ds;
      }

      vec3ds.add(entity.getPositionVector());
      return vec3ds;
   }

   public static List<Vec3d> getUntrappedBlocksExtended(
      int extension, EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace
   ) {
      ArrayList<Vec3d> placeTargets = new ArrayList<>();
      if (extension == 1) {
         placeTargets.addAll(targets(player.getPositionVector(), antiScaffold, antiStep, legs, platform, antiDrop, raytrace));
      } else {
         int extend = 1;

         for (Vec3d vec3d : getBlockBlocks(player)) {
            if (extend > extension) {
               break;
            }

            placeTargets.addAll(targets(vec3d, antiScaffold, antiStep, legs, platform, antiDrop, raytrace));
            extend++;
         }
      }

      ArrayList<Vec3d> removeList = new ArrayList<>();

      for (Vec3d vec3d : placeTargets) {
         BlockPos pos = new BlockPos(vec3d);
         if (BlockUtil.isPositionPlaceable(pos, raytrace) == -1) {
            removeList.add(vec3d);
         }
      }

      for (Vec3d vec3dx : removeList) {
         placeTargets.remove(vec3dx);
      }

      return placeTargets;
   }

   public static Vec3d[] getTrapOffsets(boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
      List<Vec3d> offsets = getTrapOffsetsList(antiScaffold, antiStep, legs, platform, antiDrop);
      Vec3d[] array = new Vec3d[offsets.size()];
      return offsets.toArray(array);
   }

   public static List<Vec3d> getTrapOffsetsList(boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
      ArrayList<Vec3d> offsets = new ArrayList<>(getOffsetList(1, false));
      offsets.add(new Vec3d(0.0, 2.0, 0.0));
      if (antiScaffold) {
         offsets.add(new Vec3d(0.0, 3.0, 0.0));
      }

      if (antiStep) {
         offsets.addAll(getOffsetList(2, false));
      }

      if (legs) {
         offsets.addAll(getOffsetList(0, false));
      }

      if (platform) {
         offsets.addAll(getOffsetList(-1, false));
         offsets.add(new Vec3d(0.0, -1.0, 0.0));
      }

      if (antiDrop) {
         offsets.add(new Vec3d(0.0, -2.0, 0.0));
      }

      return offsets;
   }
}
