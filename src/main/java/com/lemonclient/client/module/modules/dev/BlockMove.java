package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;

@Module.Declaration(name = "BlockMove", category = Category.Dev, priority = 120)
public class BlockMove extends Module {
   BooleanSetting middle = this.registerBoolean("Middle", true);
   IntegerSetting delay = this.registerInteger("Delay", 250, 0, 2000);
   BooleanSetting only = this.registerBoolean("Only In Block", true);
   BooleanSetting avoid = this.registerBoolean("Avoid Out", true, () -> !this.only.getValue());
   Timing timer = new Timing();
   Vec3d[] sides = new Vec3d[]{new Vec3d(0.24, 0.0, 0.24), new Vec3d(-0.24, 0.0, 0.24), new Vec3d(0.24, 0.0, -0.24), new Vec3d(-0.24, 0.0, -0.24)};
   @EventHandler
   private final Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(
      event -> {
         Vec3d vec = mc.player.getPositionVector();
         boolean air = true;
         AxisAlignedBB playerBox = mc.player.boundingBox;

         for (Vec3d vec3d : this.sides) {
            if (!air) {
               break;
            }

            for (int i = 0; i < 2; i++) {
               BlockPos pos = new BlockPos(vec.add(vec3d).add(0.0, i, 0.0));
               if (!BlockUtil.isAir(pos)) {
                  AxisAlignedBB box = BlockUtil.getBoundingBox(pos);
                  if (box != null && playerBox.intersects(box)) {
                     air = false;
                     break;
                  }
               }
            }
         }

         if (!air) {
            if (event.getMovementInput() instanceof MovementInputFromOptions) {
               if (this.timer.passedMs(this.delay.getValue().intValue())) {
                  BlockPos pos = this.middle.getValue()
                     ? PlayerUtil.getPlayerPos()
                     : new BlockPos(Math.round(vec.x), vec.y, Math.round(vec.z));
                  EnumFacing facing = mc.player.getHorizontalFacing();
                  int x = pos.offset(facing).x - pos.x;
                  int z = pos.offset(facing).z - pos.z;
                  boolean addX = x != 0;
                  if (event.getMovementInput().forwardKeyDown) {
                     vec = this.add(pos, addX, addX ? x < 0 : z < 0);
                  } else if (event.getMovementInput().backKeyDown) {
                     vec = this.add(pos, addX, addX ? x > 0 : z > 0);
                  } else if (event.getMovementInput().leftKeyDown) {
                     vec = this.add(pos, !addX, addX ? x > 0 : z < 0);
                  } else if (event.getMovementInput().rightKeyDown) {
                     vec = this.add(pos, !addX, addX ? x < 0 : z > 0);
                  }

                  if (vec != null) {
                     mc.player.setPosition(vec.x, vec.y, vec.z);
                     this.timer.reset();
                  }
               }

               event.getMovementInput().forwardKeyDown = false;
               event.getMovementInput().backKeyDown = false;
               event.getMovementInput().leftKeyDown = false;
               event.getMovementInput().rightKeyDown = false;
               event.getMovementInput().moveForward = 0.0F;
               event.getMovementInput().moveStrafe = 0.0F;
            }
         }
      },
      200
   );

   private Vec3d add(BlockPos pos, boolean x, boolean negative) {
      Vec3d vec;
      if (negative) {
         if (x) {
            vec = this.pos(pos.add(-1, 0, 0));
         } else {
            vec = this.pos(pos.add(0, 0, -1));
         }
      } else if (x) {
         vec = this.pos(pos.add(1, 0, 0));
      } else {
         vec = this.pos(pos.add(0, 0, 1));
      }

      return vec;
   }

   private Vec3d pos(BlockPos pos) {
      if (this.middle.getValue()) {
         return new Vec3d(pos.x + 0.5, pos.y, pos.z + 0.5);
      } else {
         Vec3d vec = new Vec3d(pos.x, pos.y, pos.z);
         Vec3d lastVec = vec;
         boolean any = !mc.world.isAirBlock(pos) || !mc.world.isAirBlock(pos.up());
         vec = new Vec3d(pos.x - 1.0E-8, pos.y, pos.z);
         if (mc.world.isAirBlock(new BlockPos(vec)) && mc.world.isAirBlock(new BlockPos(vec).up())) {
            lastVec = vec;
         } else {
            any = true;
         }

         vec = new Vec3d(pos.x, pos.y, pos.z - 1.0E-8);
         if (mc.world.isAirBlock(new BlockPos(vec)) && mc.world.isAirBlock(new BlockPos(vec).up())) {
            lastVec = vec;
         } else {
            any = true;
         }

         vec = new Vec3d(pos.x - 1.0E-8, pos.y, pos.z - 1.0E-8);
         if (mc.world.isAirBlock(new BlockPos(vec)) && mc.world.isAirBlock(new BlockPos(vec).up())) {
            lastVec = vec;
         } else {
            any = true;
         }

         return !this.only.getValue() && !any && this.avoid.getValue() ? null : lastVec;
      }
   }
}
