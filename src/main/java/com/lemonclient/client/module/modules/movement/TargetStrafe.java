package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.MotionUpdateEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "TargetStrafe", category = Category.Movement)
public class TargetStrafe extends Module {
   IntegerSetting range = this.registerInteger("TargetRange", 20, 0, 256);
   BooleanSetting jump = this.registerBoolean("Jump", true);
   BooleanSetting antiStuck = this.registerBoolean("AntiStuck", true);
   DoubleSetting distanceSetting = this.registerDouble("PreferredDistance", 1.0, 0.0, 10.0);
   DoubleSetting maxDistance = this.registerDouble("MaxDistance", 10.0, 1.0, 32.0);
   DoubleSetting turnAmount = this.registerDouble("TurnAmount", 5.0, 1.0, 90.0);
   String pattern = "%.1f";
   Timing lagBackCoolDown = new Timing();
   Timing boostTimer = new Timing();
   long detectionTime;
   boolean checkCoolDown = false;
   double boostSpeed;
   double boostSpeed2;
   double lastDist;
   int level = 1;
   double moveSpeed;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
               this.lastDist = 0.0;
               this.moveSpeed = Math.min(this.getBaseMoveSpeed(), this.getBaseMoveSpeed());
               this.detectionTime = System.currentTimeMillis();
               if (!this.checkCoolDown) {
                  this.lagBackCoolDown.reset();
                  this.checkCoolDown = true;
               }
            }

            if (event.getPacket() instanceof SPacketEntityVelocity
               && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == mc.player.getEntityId()) {
               this.boostSpeed = Math.hypot(
                  ((SPacketEntityVelocity)event.getPacket()).motionX / 8000.0F, ((SPacketEntityVelocity)event.getPacket()).motionZ / 8000.0F
               );
               this.boostSpeed2 = this.boostSpeed;
            }
         }
      }
   );
   @EventHandler
   private final Listener<MotionUpdateEvent> motionUpdateEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (!ModuleManager.getModule(HoleSnap.class).isEnabled()) {
               try {
                  if (this.lagBackCoolDown.passedMs((long)Double.parseDouble(String.format(this.pattern, 1000.0)))) {
                     this.checkCoolDown = false;
                     this.lagBackCoolDown.reset();
                  }

                  if (event.stage == 1) {
                     this.lastDist = Math.sqrt(
                        (mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)
                           + (mc.player.posZ - mc.player.prevPosZ)
                              * (mc.player.posZ - mc.player.prevPosZ)
                     );
                  }
               } catch (NumberFormatException var3) {
               }
            }
         }
      }
   );
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            EntityPlayer target = PlayerUtil.getNearestPlayer(this.range.getValue().intValue());
            if (target != null) {
               if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isInWeb) {
                  return;
               }

               if (mc.player.onGround) {
                  this.level = 2;
               }

               if (round(mc.player.posY - (int)mc.player.posY, 3) == round(0.138, 3) && this.jump.getValue()) {
                  EntityPlayerSP player = mc.player;
                  player.motionY -= 0.07;
                  event.setY(event.getY() - 0.08316090325960147);
                  EntityPlayerSP player2 = mc.player;
                  player2.posY -= 0.08316090325960147;
               }

               if (this.level != 1 || mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F) {
                  if (this.level == 2) {
                     this.level = 3;
                     if (MotionUtil.moving(mc.player)) {
                        if (!mc.player.isInLava() && mc.player.onGround && this.jump.getValue()) {
                           event.setY(mc.player.motionY = 0.4);
                        }

                        this.moveSpeed *= 1.433;
                     }
                  } else if (this.level == 3) {
                     this.level = 4;
                     this.moveSpeed = this.lastDist - 0.6553 * (this.lastDist - this.getBaseMoveSpeed() + 0.04);
                  } else {
                     if (mc.player.onGround
                        && (
                           mc.world
                                    .getCollisionBoxes(mc.player, mc.player.boundingBox.offset(0.0, mc.player.motionY, 0.0))
                                    .size()
                                 > 0
                              || mc.player.collidedVertically
                        )) {
                        this.level = 1;
                     }

                     this.moveSpeed = this.lastDist - this.lastDist / 201.0;
                  }
               } else {
                  this.level = 2;
                  this.moveSpeed = 1.418 * this.getBaseMoveSpeed();
               }

               if (MotionUtil.moving(mc.player) && this.boostSpeed2 != 0.0) {
                  if (this.boostTimer.passedMs(1L)) {
                     this.moveSpeed = this.boostSpeed2;
                     this.boostTimer.reset();
                  }

                  this.boostSpeed2 = 0.0;
               }

               this.moveSpeed = Math.max(this.moveSpeed, this.getBaseMoveSpeed());
               if (mc.player.collidedHorizontally && this.antiStuck.getValue()) {
                  this.switchDirection();
               }

               this.doStrafeAtSpeed(event, RotationUtil.getRotationTo(target.getPositionVector()).x, target.getPositionVector());
            }
         }
      },
      -100
   );
   int direction = 1;

   public static double round(double n, int n2) {
      if (n2 < 0) {
         throw new IllegalArgumentException();
      } else {
         return new BigDecimal(n).setScale(n2, RoundingMode.HALF_UP).doubleValue();
      }
   }

   private void switchDirection() {
      this.direction = -this.direction;
   }

   private void doStrafeAtSpeed(PlayerMoveEvent event, float rotation, Vec3d target) {
      float rotationYaw = rotation + 90.0F * this.direction;
      double disX = mc.player.posX - target.x;
      double disZ = mc.player.posZ - target.z;
      double distance = Math.sqrt(disX * disX + disZ * disZ);
      if (distance < this.maxDistance.getValue()) {
         if (distance > this.distanceSetting.getValue()) {
            rotationYaw = (float)(rotationYaw - this.turnAmount.getValue() * this.direction);
         } else if (distance < this.distanceSetting.getValue()) {
            rotationYaw = (float)(rotationYaw + this.turnAmount.getValue() * this.direction);
         }
      } else {
         rotationYaw = rotation;
      }

      if (this.jump.getValue() && mc.player.onGround) {
         mc.player.jump();
      }

      event.setX(this.moveSpeed * Math.cos(Math.toRadians(rotationYaw + 90.0F)));
      event.setZ(this.moveSpeed * Math.sin(Math.toRadians(rotationYaw + 90.0F)));
   }

   public double getBaseMoveSpeed() {
      double n = 0.2873;
      if (mc.player.isPotionActive(MobEffects.SPEED)) {
         n *= 1.0 + 0.2 * (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1);
      }

      return n;
   }
}
