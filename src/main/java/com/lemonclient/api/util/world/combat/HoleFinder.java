package com.lemonclient.api.util.world.combat;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class HoleFinder {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final Vec3i[] OFFSETS_2x2 = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 1)};
   public static final Set<Block> NO_BLAST = Sets.newHashSet(
      new Block[]{Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.ANVIL, Blocks.ENDER_CHEST}
   );
   public static final Set<Block> UNSAFE = Sets.newHashSet(new Block[]{Blocks.OBSIDIAN, Blocks.ANVIL, Blocks.ENDER_CHEST});

   public static boolean isAir(BlockPos pos) {
      return mc.world.isAirBlock(pos);
   }

   public static boolean[] isHole(BlockPos pos, boolean above) {
      boolean[] result = new boolean[]{false, true};
      return isAir(pos) && isAir(pos.up()) && (!above || isAir(pos.up(2))) ? is1x1(pos, result) : result;
   }

   public static boolean[] is1x1(BlockPos pos) {
      return is1x1(pos, new boolean[]{false, true});
   }

   public static boolean[] is1x1(BlockPos pos, boolean[] result) {
      for (EnumFacing facing : EnumFacing.values()) {
         if (facing != EnumFacing.UP) {
            BlockPos offset = pos.offset(facing);
            IBlockState state = mc.world.getBlockState(offset);
            if (state.getBlock() != Blocks.BEDROCK) {
               if (!NO_BLAST.contains(state.getBlock())) {
                  return result;
               }

               result[1] = false;
            }
         }
      }

      result[0] = true;
      return result;
   }

   public static boolean is2x1(BlockPos pos) {
      return is2x1(pos, true);
   }

   public static boolean is2x1(BlockPos pos, boolean upper) {
      if (!upper || isAir(pos) && isAir(pos.up()) && !isAir(pos.down())) {
         int airBlocks = 0;

         for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos offset = pos.offset(facing);
            if (isAir(offset)) {
               if (!isAir(offset.up())) {
                  return false;
               }

               if (isAir(offset.down())) {
                  return false;
               }

               for (EnumFacing offsetFacing : EnumFacing.HORIZONTALS) {
                  if (offsetFacing != facing.getOpposite()) {
                     IBlockState state = mc.world.getBlockState(offset.offset(offsetFacing));
                     if (!NO_BLAST.contains(state.getBlock())) {
                        return false;
                     }
                  }
               }

               airBlocks++;
            }

            if (airBlocks > 1) {
               return false;
            }
         }

         return airBlocks == 1;
      } else {
         return false;
      }
   }

   public static boolean is2x2Partial(BlockPos pos) {
      Set<BlockPos> positions = new HashSet<>();

      for (Vec3i vec : OFFSETS_2x2) {
         positions.add(pos.add(vec));
      }

      boolean airBlock = false;

      for (BlockPos holePos : positions) {
         if (!isAir(holePos) || !isAir(holePos.up()) || isAir(holePos.down())) {
            return false;
         }

         if (isAir(holePos.up(2))) {
            airBlock = true;
         }

         for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos offset = holePos.offset(facing);
            if (!positions.contains(offset)) {
               IBlockState state = mc.world.getBlockState(offset);
               if (!NO_BLAST.contains(state.getBlock())) {
                  return false;
               }
            }
         }
      }

      return airBlock;
   }

   public static boolean is2x2(BlockPos pos) {
      return is2x2(pos, true);
   }

   public static boolean is2x2(BlockPos pos, boolean upper) {
      if (upper && !isAir(pos)) {
         return false;
      } else if (is2x2Partial(pos)) {
         return true;
      } else {
         BlockPos l = pos.add(-1, 0, 0);
         boolean airL = isAir(l);
         if (airL && is2x2Partial(l)) {
            return true;
         } else {
            BlockPos r = pos.add(0, 0, -1);
            boolean airR = isAir(r);
            return airR && is2x2Partial(r) ? true : (airL || airR) && is2x2Partial(pos.add(-1, 0, -1));
         }
      }
   }

   public static boolean is2x2single(BlockPos pos, boolean upper) {
      return upper && !isAir(pos) ? false : is2x2Partial(pos);
   }
}
