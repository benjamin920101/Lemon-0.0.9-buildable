package com.lemonclient.api.util.player;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class MutableBlockPosHelper {
   public MutableBlockPos mutablePos = new MutableBlockPos();

   public static MutableBlockPos set(MutableBlockPos mutablePos, double x, double y, double z) {
      return mutablePos.setPos(x, y, z);
   }

   public static MutableBlockPos set(MutableBlockPos mutablePos, BlockPos pos) {
      return mutablePos.setPos(pos.getX(), pos.getY(), pos.getZ());
   }

   public static MutableBlockPos set(MutableBlockPos mutablePos, BlockPos pos, double x, double y, double z) {
      return mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
   }

   public static MutableBlockPos set(MutableBlockPos mutablePos, BlockPos pos, int x, int y, int z) {
      return mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
   }

   public static MutableBlockPos set(MutableBlockPos mutablePos, int x, int y, int z) {
      return mutablePos.setPos(x, y, z);
   }

   public static MutableBlockPos setAndAdd(MutableBlockPos mutablePos, int x, int y, int z) {
      return mutablePos.setPos(mutablePos.getX() + x, mutablePos.getY() + y, mutablePos.getZ() + z);
   }

   public static MutableBlockPos setAndAdd(MutableBlockPos mutablePos, double x, double y, double z) {
      return mutablePos.setPos(mutablePos.getX() + x, mutablePos.getY() + y, mutablePos.getZ() + z);
   }

   public static MutableBlockPos setAndAdd(MutableBlockPos mutablePos, BlockPos pos) {
      return mutablePos.setPos(
         mutablePos.getX() + pos.getX(), mutablePos.getY() + pos.getY(), mutablePos.getZ() + pos.getZ()
      );
   }

   public static MutableBlockPos setAndAdd(MutableBlockPos mutablePos, BlockPos pos, double x, double y, double z) {
      return mutablePos.setPos(
         mutablePos.getX() + pos.getX() + x,
         mutablePos.getY() + pos.getY() + y,
         mutablePos.getZ() + pos.getZ() + z
      );
   }

   public MutableBlockPos set(double x, double y, double z) {
      return this.mutablePos.setPos(x, y, z);
   }

   public MutableBlockPos set(BlockPos pos) {
      return this.mutablePos.setPos(pos.getX(), pos.getY(), pos.getZ());
   }

   public MutableBlockPos set(BlockPos pos, double x, double y, double z) {
      return this.mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
   }

   public MutableBlockPos set(BlockPos pos, int x, int y, int z) {
      return this.mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
   }

   public MutableBlockPos set(int x, int y, int z) {
      return this.mutablePos.setPos(x, y, z);
   }

   public MutableBlockPos setAndAdd(int x, int y, int z) {
      return this.mutablePos.setPos(this.mutablePos.getX() + x, this.mutablePos.getY() + y, this.mutablePos.getZ() + z);
   }

   public MutableBlockPos setAndAdd(double x, double y, double z) {
      return this.mutablePos.setPos(this.mutablePos.getX() + x, this.mutablePos.getY() + y, this.mutablePos.getZ() + z);
   }

   public MutableBlockPos setAndAdd(BlockPos pos) {
      return this.mutablePos
         .setPos(
            this.mutablePos.getX() + pos.getX(),
            this.mutablePos.getY() + pos.getY(),
            this.mutablePos.getZ() + pos.getZ()
         );
   }

   public MutableBlockPos setAndAdd(BlockPos pos, double x, double y, double z) {
      return this.mutablePos
         .setPos(
            this.mutablePos.getX() + pos.getX() + x,
            this.mutablePos.getY() + pos.getY() + y,
            this.mutablePos.getZ() + pos.getZ() + z
         );
   }
}
