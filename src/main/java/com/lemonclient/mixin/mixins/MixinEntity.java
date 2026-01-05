package com.lemonclient.mixin.mixins;

import com.lemonclient.api.event.events.EntityCollisionEvent;
import com.lemonclient.api.event.events.StepEvent;
import com.lemonclient.client.LemonClient;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
   @Shadow
   public double posX;
   @Shadow
   public double posY;
   @Shadow
   public double posZ;
   @Shadow
   public double motionX;
   @Shadow
   public double motionY;
   @Shadow
   public double motionZ;
   @Shadow
   public float rotationYaw;
   @Shadow
   public float rotationPitch;
   @Shadow
   public boolean onGround;
   @Shadow
   public World world;
   @Shadow
   public float stepHeight;
   @Shadow
   public boolean isDead;
   @Shadow
   public float width;
   @Shadow
   public float height;
   private Float prevHeight;

   @Shadow
   public abstract AxisAlignedBB getEntityBoundingBox();

   @Shadow
   public abstract boolean isSneaking();

   @Shadow
   @Override
   public abstract boolean equals(Object var1);

   @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
   public void velocity(Entity entityIn, CallbackInfo ci) {
      EntityCollisionEvent event = new EntityCollisionEvent();
      LemonClient.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "move", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.resetPositionToBB()V", ordinal = 1))
   private void resetPositionToBBHook(MoverType type, double x, double y, double z, CallbackInfo info) {
      if (EntityPlayerSP.class.isInstance(this) && this.prevHeight != null) {
         this.stepHeight = this.prevHeight;
         this.prevHeight = null;
      }
   }

   @Inject(method = "move", at = @At("HEAD"))
   public void move(MoverType type, double tx, double ty, double tz, CallbackInfo ci) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.getCurrentServerData() != null) {
         double x = tx;
         double y = ty;
         double z = tz;
         if (!ci.isCancelled()) {
            AxisAlignedBB bb = mc.player.getEntityBoundingBox();
            if (!mc.player.noClip) {
               if (type.equals(MoverType.PISTON)) {
                  return;
               }

               mc.world.profiler.startSection("move");
               if (mc.player.isInWeb) {
                  return;
               }

               double d2 = tx;
               double d4 = tz;
               if ((type == MoverType.SELF || type == MoverType.PLAYER) && mc.player.onGround && mc.player.isSneaking()) {
                  for (double d5 = 0.05;
                     x != 0.0 && mc.world.getCollisionBoxes(mc.player, bb.offset(x, -mc.player.stepHeight, 0.0)).isEmpty();
                     d2 = x
                  ) {
                     if (x < 0.05 && x >= -0.05) {
                        x = 0.0;
                     } else if (x > 0.0) {
                        x -= 0.05;
                     } else {
                        x += 0.05;
                     }
                  }

                  for (;
                     z != 0.0 && mc.world.getCollisionBoxes(mc.player, bb.offset(0.0, -mc.player.stepHeight, z)).isEmpty();
                     d4 = z
                  ) {
                     if (z < 0.05 && z >= -0.05) {
                        z = 0.0;
                     } else if (z > 0.0) {
                        z -= 0.05;
                     } else {
                        z += 0.05;
                     }
                  }

                  for (;
                     x != 0.0 && z != 0.0 && mc.world.getCollisionBoxes(mc.player, bb.offset(x, -mc.player.stepHeight, z)).isEmpty();
                     d4 = z
                  ) {
                     if (x < 0.05 && x >= -0.05) {
                        x = 0.0;
                     } else if (x > 0.0) {
                        x -= 0.05;
                     } else {
                        x += 0.05;
                     }

                     d2 = x;
                     if (z < 0.05 && z >= -0.05) {
                        z = 0.0;
                     } else if (z > 0.0) {
                        z -= 0.05;
                     } else {
                        z += 0.05;
                     }
                  }
               }

               List<AxisAlignedBB> list1 = mc.world.getCollisionBoxes(mc.player, bb.expand(x, ty, z));
               if (ty != 0.0) {
                  int k = 0;

                  for (int l = list1.size(); k < l; k++) {
                     y = list1.get(k).calculateYOffset(bb, y);
                  }

                  bb = bb.offset(0.0, y, 0.0);
               }

               if (x != 0.0) {
                  int j5 = 0;

                  for (int l5 = list1.size(); j5 < l5; j5++) {
                     x = list1.get(j5).calculateXOffset(bb, x);
                  }

                  if (x != 0.0) {
                     bb = bb.offset(x, 0.0, 0.0);
                  }
               }

               if (z != 0.0) {
                  int k5 = 0;

                  for (int i6 = list1.size(); k5 < i6; k5++) {
                     z = list1.get(k5).calculateZOffset(bb, z);
                  }

                  if (z != 0.0) {
                     bb = bb.offset(0.0, 0.0, z);
                  }
               }

               boolean flag = mc.player.onGround || ty != y && ty < 0.0;
               if (mc.player.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
                  y = mc.player.stepHeight;
                  List<AxisAlignedBB> list = mc.world.getCollisionBoxes(mc.player, bb.expand(d2, y, d4));
                  AxisAlignedBB axisalignedbb3 = bb.expand(d2, 0.0, d4);
                  double d8 = y;
                  int j1 = 0;

                  for (int k1 = list.size(); j1 < k1; j1++) {
                     d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
                  }

                  AxisAlignedBB axisalignedbb2 = bb.offset(0.0, d8, 0.0);
                  double d18 = d2;
                  int l1 = 0;

                  for (int i2 = list.size(); l1 < i2; l1++) {
                     d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
                  }

                  axisalignedbb2 = axisalignedbb2.offset(d18, 0.0, 0.0);
                  double d19 = d4;
                  int j2 = 0;

                  for (int k2 = list.size(); j2 < k2; j2++) {
                     d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
                  }

                  axisalignedbb2 = axisalignedbb2.offset(0.0, 0.0, d19);
                  AxisAlignedBB axisalignedbb4 = bb;
                  double d20 = y;
                  int l2 = 0;

                  for (int i3 = list.size(); l2 < i3; l2++) {
                     d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
                  }

                  axisalignedbb4 = axisalignedbb4.offset(0.0, d20, 0.0);
                  double d21 = d2;
                  int j3 = 0;

                  for (int k3 = list.size(); j3 < k3; j3++) {
                     d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
                  }

                  axisalignedbb4 = axisalignedbb4.offset(d21, 0.0, 0.0);
                  double d22 = d4;
                  int l3 = 0;

                  for (int i4 = list.size(); l3 < i4; l3++) {
                     d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
                  }

                  axisalignedbb4 = axisalignedbb4.offset(0.0, 0.0, d22);
                  double d23 = d18 * d18 + d19 * d19;
                  double d9 = d21 * d21 + d22 * d22;
                  if (d23 > d9) {
                     x = d18;
                     z = d19;
                     y = -d8;
                     bb = axisalignedbb2;
                  } else {
                     x = d21;
                     z = d22;
                     y = -d20;
                     bb = axisalignedbb4;
                  }

                  int j4 = 0;

                  for (int k4 = list.size(); j4 < k4; j4++) {
                     y = list.get(j4).calculateYOffset(bb, y);
                  }

                  bb = bb.offset(0.0, y, 0.0);
                  if (!(x * x + z * z >= x * x + z * z)) {
                     StepEvent event = new StepEvent(bb);
                     LemonClient.EVENT_BUS.post(event);
                     if (event.isCancelled()) {
                        mc.player.stepHeight = 0.5F;
                     }
                  }
               }
            }
         }
      }
   }
}
