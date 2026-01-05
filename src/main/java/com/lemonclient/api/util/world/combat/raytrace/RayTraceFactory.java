package com.lemonclient.api.util.world.combat.raytrace;

import com.lemonclient.api.util.world.combat.RotationUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class RayTraceFactory {
   private static final EnumFacing[] T = new EnumFacing[]{EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN};
   private static final EnumFacing[] B = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP};
   private static final EnumFacing[] S = new EnumFacing[]{EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.UP, EnumFacing.DOWN};
   public static Minecraft mc = Minecraft.getMinecraft();

   private RayTraceFactory() {
      throw new AssertionError();
   }

   public static Ray fullTrace(Entity from, World world, BlockPos pos, double resolution) {
      Ray dumbRay = null;
      double closest = Double.MAX_VALUE;

      for (EnumFacing facing : getOptimalFacings(from, pos)) {
         BlockPos offset = pos.offset(facing);
         IBlockState state = world.getBlockState(offset);
         if (!state.getMaterial().isReplaceable()) {
            Ray ray = rayTrace(from, offset, facing.getOpposite(), world, state, resolution);
            if (ray.isLegit()) {
               return ray;
            }

            double dist = from.getDistanceSq(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5);
            if (dumbRay == null || dist < closest) {
               closest = dist;
               dumbRay = ray;
            }
         }
      }

      return dumbRay;
   }

   public static Ray rayTrace(Entity from, BlockPos on, EnumFacing facing, World access, IBlockState state, double res) {
      Vec3d start = new Vec3d(from.posX, from.posY + from.getEyeHeight(), from.posZ);
      AxisAlignedBB bb = state.getBoundingBox(access, on);
      if (res >= 1.0) {
         float[] r = rots(on, facing, from, access, state);
         Vec3d look = RotationUtil.getVec3d(r[0], r[1]);
         double d = mc.playerController.getBlockReachDistance();
         Vec3d rotations = start.add(look.x * d, look.y * d, look.z * d);
         RayTraceResult result = RayTracer.trace(mc.world, access, start, rotations, false, false, true);
         return result != null && result.sideHit == facing && on.equals(result.getBlockPos())
            ? new Ray(result, r, on, facing, null).setLegit(true)
            : dumbRay(on, facing, r);
      } else {
         Vec3i dirVec = facing.getDirectionVec();
         double dirX = dirVec.getX() < 0 ? bb.minX : dirVec.getX() * bb.maxX;
         double dirY = dirVec.getY() < 0 ? bb.minY : dirVec.getY() * bb.maxY;
         double dirZ = dirVec.getZ() < 0 ? bb.minZ : dirVec.getZ() * bb.maxZ;
         double minX = on.getX() + dirX + (dirVec.getX() == 0 ? bb.minX : 0.0);
         double minY = on.getY() + dirY + (dirVec.getY() == 0 ? bb.minY : 0.0);
         double minZ = on.getZ() + dirZ + (dirVec.getZ() == 0 ? bb.minZ : 0.0);
         double maxX = on.getX() + dirX + (dirVec.getX() == 0 ? bb.maxX : 0.0);
         double maxY = on.getY() + dirY + (dirVec.getY() == 0 ? bb.maxY : 0.0);
         double maxZ = on.getZ() + dirZ + (dirVec.getZ() == 0 ? bb.maxZ : 0.0);
         boolean xEq = Double.compare(minX, maxX) == 0;
         boolean yEq = Double.compare(minY, maxY) == 0;
         boolean zEq = Double.compare(minZ, maxZ) == 0;
         if (xEq) {
            minX -= dirVec.getX() * 5.0E-4;
            maxX = minX;
         }

         if (yEq) {
            minY -= dirVec.getY() * 5.0E-4;
            maxY = minY;
         }

         if (zEq) {
            minZ -= dirVec.getZ() * 5.0E-4;
            maxZ = minZ;
         }

         double endX = Math.max(minX, maxX) - (xEq ? 0.0 : 5.0E-4);
         double endY = Math.max(minY, maxY) - (yEq ? 0.0 : 5.0E-4);
         double endZ = Math.max(minZ, maxZ) - (zEq ? 0.0 : 5.0E-4);
         if (res <= 0.0) {
            double staX = Math.min(minX, maxX) + (xEq ? 0.0 : 5.0E-4);
            double staY = Math.min(minY, maxY) + (yEq ? 0.0 : 5.0E-4);
            double staZ = Math.min(minZ, maxZ) + (zEq ? 0.0 : 5.0E-4);
            Set<Vec3d> vectors = new HashSet<>();
            vectors.add(new Vec3d(staX, staY, staZ));
            vectors.add(new Vec3d(staX, staY, endZ));
            vectors.add(new Vec3d(staX, endY, staZ));
            vectors.add(new Vec3d(staX, endY, endZ));
            vectors.add(new Vec3d(endX, staY, staZ));
            vectors.add(new Vec3d(endX, staY, endZ));
            vectors.add(new Vec3d(endX, endY, staZ));
            vectors.add(new Vec3d(endX, endY, endZ));
            double x = (endX - staX) / 2.0 + staX;
            double y = (endY - staY) / 2.0 + staY;
            double z = (endZ - staZ) / 2.0 + staZ;
            vectors.add(new Vec3d(x, y, z));

            for (Vec3d vec : vectors) {
               RayTraceResult ray = RayTracer.trace(mc.world, access, start, vec, false, false, true);
               if (ray != null && on.equals(ray.getBlockPos()) && facing == ray.sideHit) {
                  return new Ray(ray, rots(from, vec), on, facing, vec).setLegit(true);
               }
            }

            return dumbRay(on, facing, rots(on, facing, from, access, state));
         } else {
            for (double x = Math.min(minX, maxX); x <= endX; x += res) {
               for (double y = Math.min(minY, maxY); y <= endY; y += res) {
                  for (double z = Math.min(minZ, maxZ); z <= endZ; z += res) {
                     Vec3d vector = new Vec3d(x, y, z);
                     RayTraceResult ray = RayTracer.trace(mc.world, access, start, vector, false, false, true);
                     if (ray != null && facing == ray.sideHit && on.equals(ray.getBlockPos())) {
                        return new Ray(ray, rots(from, vector), on, facing, vector).setLegit(true);
                     }
                  }
               }
            }

            return dumbRay(on, facing, rots(on, facing, from, access, state));
         }
      }
   }

   public static Ray dumbRay(BlockPos on, EnumFacing offset, float[] rotations) {
      return newRay(new RayTraceResult(Type.MISS, new Vec3d(0.5, 1.0, 0.5), EnumFacing.UP, BlockPos.ORIGIN), on, offset, rotations);
   }

   public static Ray newRay(RayTraceResult result, BlockPos on, EnumFacing offset, float[] rotations) {
      return new Ray(result, rotations, on, offset, null);
   }

   static float[] rots(Entity from, Vec3d vec3d) {
      return RotationUtil.getRotations(vec3d.x, vec3d.y, vec3d.z, from);
   }

   private static float[] rots(BlockPos pos, EnumFacing facing, Entity from, World world, IBlockState state) {
      return RotationUtil.getRotations(pos, facing, from, world, state);
   }

   private static EnumFacing[] getOptimalFacings(Entity player, BlockPos pos) {
      if (pos.getY() > player.posY + 2.0) {
         return T;
      } else {
         return pos.getY() < player.posY ? B : S;
      }
   }
}
