package com.lemonclient.api.util.misc;

import net.minecraft.util.math.Vec3d;

public class Offsets {
   public static final Vec3d[] SURROUND = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0),
      new Vec3d(-1.0, -1.0, 0.0),
      new Vec3d(1.0, -1.0, 0.0),
      new Vec3d(0.0, -1.0, -1.0),
      new Vec3d(0.0, -1.0, 1.0),
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0)
   };
   public static final Vec3d[] SURROUND_CITY = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0),
      new Vec3d(-1.0, -1.0, 0.0),
      new Vec3d(1.0, -1.0, 0.0),
      new Vec3d(0.0, -1.0, -1.0),
      new Vec3d(0.0, -1.0, 1.0),
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0),
      new Vec3d(-2.0, 0.0, 0.0),
      new Vec3d(2.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -2.0),
      new Vec3d(0.0, 0.0, 2.0)
   };
   public static final Vec3d[] TRAP_FULL = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0),
      new Vec3d(-1.0, -1.0, 0.0),
      new Vec3d(1.0, -1.0, 0.0),
      new Vec3d(0.0, -1.0, -1.0),
      new Vec3d(0.0, -1.0, 1.0),
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0),
      new Vec3d(-1.0, 1.0, 0.0),
      new Vec3d(1.0, 1.0, 0.0),
      new Vec3d(0.0, 1.0, -1.0),
      new Vec3d(0.0, 1.0, 1.0),
      new Vec3d(1.0, 2.0, 0.0),
      new Vec3d(0.0, 2.0, 0.0)
   };
   public static final Vec3d[] TRAP_STEP = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0),
      new Vec3d(-1.0, -1.0, 0.0),
      new Vec3d(1.0, -1.0, 0.0),
      new Vec3d(0.0, -1.0, -1.0),
      new Vec3d(0.0, -1.0, 1.0),
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0),
      new Vec3d(-1.0, 1.0, 0.0),
      new Vec3d(1.0, 1.0, 0.0),
      new Vec3d(0.0, 1.0, -1.0),
      new Vec3d(0.0, 1.0, 1.0),
      new Vec3d(1.0, 2.0, 0.0),
      new Vec3d(0.0, 2.0, 0.0),
      new Vec3d(0.0, 3.0, 0.0)
   };
   public static final Vec3d[] TRAP_SIMPLE = new Vec3d[]{
      new Vec3d(0.0, -1.0, 0.0),
      new Vec3d(-1.0, -1.0, 0.0),
      new Vec3d(1.0, -1.0, 0.0),
      new Vec3d(0.0, -1.0, -1.0),
      new Vec3d(0.0, -1.0, 1.0),
      new Vec3d(-1.0, 0.0, 0.0),
      new Vec3d(1.0, 0.0, 0.0),
      new Vec3d(0.0, 0.0, -1.0),
      new Vec3d(0.0, 0.0, 1.0),
      new Vec3d(1.0, 1.0, 0.0),
      new Vec3d(1.0, 2.0, 0.0),
      new Vec3d(0.0, 2.0, 0.0)
   };
   public static final Vec3d[] BURROW = new Vec3d[]{new Vec3d(0.0, 0.0, 0.0)};
   public static final Vec3d[] BURROW_DOUBLE = new Vec3d[]{new Vec3d(0.0, 0.0, 0.0), new Vec3d(0.0, 1.0, 0.0)};
}
