package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.api.util.world.TimerUtils;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "LiquidSpeed", category = Category.Movement)
public class LiquidSpeed extends Module {
   DoubleSetting timerVal = this.registerDouble("Timer Speed", 1.0, 1.0, 2.0);
   DoubleSetting XZWater = this.registerDouble("XZ Water", 5.75, 0.01, 8.0);
   DoubleSetting upWater = this.registerDouble("Y+ Water", 2.69, 0.01, 8.0);
   DoubleSetting downWater = this.registerDouble("Y- Water", 0.8, 0.01, 8.0);
   DoubleSetting XZBoostWater = this.registerDouble("XZ Boost Water", 6.0, 1.0, 8.0);
   DoubleSetting yBoostWater = this.registerDouble("Y Boost Water", 2.9, 0.1, 8.0);
   DoubleSetting XZLava = this.registerDouble("XZ Lava", 3.8, 0.01, 8.0);
   DoubleSetting upLava = this.registerDouble("Y+ Lava", 2.69, 0.01, 8.0);
   DoubleSetting downLava = this.registerDouble("Y- Lava", 4.22, 0.01, 8.0);
   DoubleSetting XZBoostLava = this.registerDouble("XZ Boost Lava", 4.0, 1.0, 8.0);
   DoubleSetting yBoostLava = this.registerDouble("Y Boost Lava", 2.0, 0.1, 8.0);
   DoubleSetting jitter = this.registerDouble("Jitter", 1.0, 1.0, 20.0);
   BooleanSetting groundIgnore = this.registerBoolean("Ground Ignore", true);
   Vec3d[] sides = new Vec3d[]{new Vec3d(0.3, 0.0, 0.3), new Vec3d(0.3, 0.0, -0.3), new Vec3d(-0.3, 0.0, 0.3), new Vec3d(-0.3, 0.0, -0.3)};
   double moveSpeed = 0.0;
   double motionY = 0.0;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (event.getPacket() instanceof SPacketPlayerPosLook) {
         this.reset();
      }
   });
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
      if (mc.player != null && mc.world != null) {
         if (mc.player.isInWater() || mc.player.isInLava()) {
            if (!this.groundIgnore.getValue() && mc.player.onGround) {
               this.stopMotion(event);
               this.reset();
            } else if (mc.player.isInWater()) {
               this.waterSwim(event);
            } else if (mc.player.isInLava()) {
               this.lavaSwim(event);
            } else {
               this.reset();
            }
         }
      }
   });

   @Override
   public void onDisable() {
      this.reset();
   }

   private boolean intersect(BlockPos pos) {
      AxisAlignedBB box = BlockUtil.getBoundingBox(pos);
      return box == null ? false : mc.player.boundingBox.intersects(box);
   }

   private boolean inLiquid(Material material) {
      Vec3d vec = mc.player.getPositionVector();

      for (Vec3d side : this.sides) {
         BlockPos blockPos = new BlockPos(vec.add(side));
         if (this.intersect(blockPos)) {
            IBlockState blockState = BlockUtil.getState(blockPos);
            if (!(blockState instanceof BlockLiquid)) {
               return false;
            }

            if (((BlockLiquid)blockState).material != material) {
               return false;
            }
         }
      }

      return true;
   }

   private void lavaSwim(PlayerMoveEvent moveEvent) {
      this.ySwim(moveEvent, this.yBoostLava.getValue(), this.upLava.getValue(), this.downLava.getValue());
      boolean jump = mc.player.movementInput.jump;
      boolean sneak = mc.player.movementInput.sneak;
      if (jump && sneak || !jump && !sneak) {
         TimerUtils.setTimerSpeed(1.0F);
      } else {
         TimerUtils.setTimerSpeed(this.timerVal.getValue().floatValue());
      }

      if (mc.player.movementInput.moveForward == 0.0F && mc.player.movementInput.moveStrafe == 0.0F) {
         this.stopMotion(moveEvent);
      } else {
         double yaw = MotionUtil.calcMoveYaw();
         this.moveSpeed = Math.min(Math.max(this.moveSpeed * this.XZBoostLava.getValue(), 0.05), this.XZLava.getValue() / 20.0);
         moveEvent.setX(-Math.sin(yaw) * this.moveSpeed);
         moveEvent.setZ(Math.cos(yaw) * this.moveSpeed);
      }
   }

   private void waterSwim(PlayerMoveEvent moveEvent) {
      this.ySwim(moveEvent, this.yBoostWater.getValue(), this.upWater.getValue(), this.downWater.getValue() * 20.0);
      boolean jump = mc.player.movementInput.jump;
      boolean sneak = mc.player.movementInput.sneak;
      if (jump && sneak || !jump && !sneak) {
         TimerUtils.setTimerSpeed(1.0F);
      } else {
         TimerUtils.setTimerSpeed(this.timerVal.getValue().floatValue());
      }

      if (mc.player.movementInput.moveForward == 0.0F && mc.player.movementInput.moveStrafe == 0.0F) {
         this.stopMotion(moveEvent);
      } else {
         double yaw = MotionUtil.calcMoveYaw();
         double multiplier = this.applySpeedPotionEffects();
         this.moveSpeed = Math.min(Math.max(this.moveSpeed * this.XZBoostWater.getValue(), 0.075), this.XZWater.getValue() / 20.0);
         if (mc.player.movementInput.sneak && !mc.player.movementInput.jump) {
            double downMotion = mc.player.motionY * 0.25;
            this.moveSpeed = Math.min(this.moveSpeed, Math.max(this.moveSpeed + downMotion, 0.0));
         }

         this.moveSpeed *= multiplier;
         moveEvent.setX(-Math.sin(yaw) * this.moveSpeed);
         moveEvent.setZ(Math.cos(yaw) * this.moveSpeed);
      }
   }

   private double applySpeedPotionEffects() {
      double result = 1.0;
      if (mc.player.getActivePotionEffect(MobEffects.SPEED) != null) {
         result += (mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1.0) * 0.2;
      }

      if (mc.player.getActivePotionEffect(MobEffects.SLOWNESS) != null) {
         result -= (mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier() + 1.0) * 0.15;
      }

      return result;
   }

   private void ySwim(PlayerMoveEvent moveEvent, double vBoost, double upSpeed, double downSpeed) {
      boolean jump = mc.player.movementInput.jump;
      boolean sneak = mc.player.movementInput.sneak;
      this.motionY = Math.pow(0.1, this.jitter.getValue());
      if (!jump || !sneak) {
         if (jump) {
            this.motionY = Math.min(this.motionY + vBoost / 20.0, upSpeed / 20.0);
         }

         if (sneak) {
            this.motionY = Math.max(this.motionY - vBoost / 20.0, -downSpeed / 20.0);
         }
      }

      moveEvent.setY(this.motionY);
   }

   private void stopMotion(PlayerMoveEvent event) {
      event.setX(0.0);
      event.setZ(0.0);
      this.moveSpeed = 0.0;
   }

   private void reset() {
      this.moveSpeed = 0.0;
      this.motionY = 0.0;
   }
}
