package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.LemonClientEvent;
import com.lemonclient.api.event.events.MotionUpdateEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.PlayerJumpEvent;
import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import java.util.Arrays;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

@Module.Declaration(name = "StrafeBypass", category = Category.Movement, priority = 999)
public class StrafeBypass extends Module {
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Strict", "Normal"), "Normal");
   BooleanSetting boost = this.registerBoolean("DamageBoost", false);
   BooleanSetting randomBoost = this.registerBoolean("RandomBoost", false);
   BooleanSetting debug = this.registerBoolean("Debug", false);
   public Timing rdBoostTimer = new Timing();
   public float boostFactor = 4.0F;
   public long detectionTime;
   public boolean lagDetected;
   public double boostSpeed;
   public int stage = 1;
   private double lastDist;
   private double moveSpeed;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (event.getPacket() instanceof SPacketEntityVelocity
               && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == mc.player.getEntityId()
               && !ModuleManager.getModule(SpeedPlus.class).isEnabled()) {
               this.boostSpeed = Math.max(
                  Math.hypot(
                     ((SPacketEntityVelocity)event.getPacket()).motionX / 8000.0F, ((SPacketEntityVelocity)event.getPacket()).motionZ / 8000.0F
                  ),
                  this.boostSpeed
               );
            }

            if (event.getPacket() instanceof SPacketPlayerPosLook) {
               this.detectionTime = System.currentTimeMillis();
               this.lagDetected = true;
               this.rdBoostTimer.reset();
               this.boostFactor = 6.0F;
            }
         }
      }
   );
   @EventHandler
   private final Listener<MotionUpdateEvent> motionUpdateEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (event.getEra() == LemonClientEvent.Era.PRE) {
               if (System.currentTimeMillis() - this.detectionTime > 3182L) {
                  this.lagDetected = false;
               }

               if (event.stage == 1) {
                  this.lastDist = Math.sqrt(
                     (mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)
                        + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)
                  );
               }
            }
         }
      }
   );
   @EventHandler
   private final Listener<PlayerJumpEvent> jumpEventListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (!mc.player.isInWater() && !mc.player.isInLava()) {
            event.cancel();
         }
      }
   });
   @EventHandler
   private final Listener<PlayerMoveEvent> moveEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (!mc.player.isInWater() && !mc.player.isInLava()) {
               if (mc.player.movementInput.moveForward == 0.0 && mc.player.movementInput.moveStrafe == 0.0) {
                  event.setX(0.0);
                  event.setZ(0.0);
                  event.setSpeed(0.0);
                  return;
               }

               if (mc.player.onGround) {
                  this.stage = 2;
               }

               label118: {
                  switch (this.stage) {
                     case 0:
                        this.stage++;
                        this.lastDist = 0.0;
                        break label118;
                     case 3:
                        this.moveSpeed = this.lastDist - (this.mode.getValue().equals("Normal") ? 0.6896 : 0.795) * (this.lastDist - this.getBaseMoveSpeed());
                        break label118;
                  }

                  if ((
                        !mc.world
                              .getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0))
                              .isEmpty()
                           || mc.player.collidedVertically
                     )
                     && this.stage > 0) {
                     this.stage = mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F ? 0 : 1;
                  }

                  this.moveSpeed = this.lastDist - this.lastDist / 159.0;
               }

               if (this.boost.getValue() && this.boostSpeed != 0.0 && MotionUtil.moving(mc.player)) {
                  this.moveSpeed = this.moveSpeed + this.boostSpeed;
                  this.boostSpeed = 0.0;
               }

               if (this.randomBoost.getValue()
                  && this.rdBoostTimer.passedMs(3500L)
                  && !this.lagDetected
                  && MotionUtil.moving(mc.player)
                  && mc.player.onGround) {
                  this.moveSpeed = this.moveSpeed + this.moveSpeed / this.boostFactor;
                  if (this.debug.getValue()) {
                     MessageBus.sendClientPrefixMessage("RandomBoost", Notification.Type.INFO);
                  }

                  this.boostFactor = 4.0F;
                  this.rdBoostTimer.reset();
               }

               if (!mc.gameSettings.keyBindJump.isKeyDown() && mc.player.onGround) {
                  this.moveSpeed = this.getBaseMoveSpeed();
               } else {
                  this.moveSpeed = Math.max(this.moveSpeed, this.getBaseMoveSpeed());
               }

               if (mc.player.movementInput.moveForward != 0.0 && mc.player.movementInput.moveStrafe != 0.0) {
                  mc.player.movementInput.moveForward = mc.player.movementInput.moveForward * (float)Math.sin(Math.PI / 4);
                  mc.player.movementInput.moveStrafe = mc.player.movementInput.moveStrafe * (float)Math.cos(Math.PI / 4);
               }

               event.setX(
                  (
                        mc.player.movementInput.moveForward * this.moveSpeed * -Math.sin(Math.toRadians(mc.player.rotationYaw))
                           + mc.player.movementInput.moveStrafe * this.moveSpeed * Math.cos(Math.toRadians(mc.player.rotationYaw))
                     )
                     * (this.mode.getValue().equals("Normal") ? 0.993 : 0.99)
               );
               event.setZ(
                  (
                        mc.player.movementInput.moveForward * this.moveSpeed * Math.cos(Math.toRadians(mc.player.rotationYaw))
                           - mc.player.movementInput.moveStrafe * this.moveSpeed * -Math.sin(Math.toRadians(mc.player.rotationYaw))
                     )
                     * (this.mode.getValue().equals("Normal") ? 0.993 : 0.99)
               );
               this.stage++;
            }
         }
      }
   );

   public double getBaseMoveSpeed() {
      double result = 0.2873;
      if (mc.player.getActivePotionEffect(MobEffects.SPEED) != null) {
         result += 0.2873 * (mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1.0) * 0.2;
      }

      if (mc.player.getActivePotionEffect(MobEffects.SLOWNESS) != null) {
         result -= 0.2873 * (mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier() + 1.0) * 0.15;
      }

      return result;
   }
}
