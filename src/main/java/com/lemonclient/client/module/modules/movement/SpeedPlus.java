package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.MotionUpdateEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.event.events.StepEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.StringSetting;
import com.lemonclient.api.util.misc.KeyBoardClass;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.api.util.world.TimerUtils;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

@Module.Declaration(name = "Speed+", category = Category.Movement, priority = 999)
public class SpeedPlus extends Module {
   public static SpeedPlus INSTANCE = new SpeedPlus();
   BooleanSetting damageBoost = this.registerBoolean("DamageBoost", true);
   public BooleanSetting sum = this.registerBoolean("Sum", false, () -> this.damageBoost.getValue());
   BooleanSetting longJump = this.registerBoolean("TryLongJump", false);
   IntegerSetting lagCoolDown = this.registerInteger("LagCoolDown", 2200, 0, 8000, () -> this.longJump.getValue());
   IntegerSetting jumpStage = this.registerInteger("JumpStage", 6, 1, 20, () -> this.longJump.getValue());
   BooleanSetting motionJump = this.registerBoolean("MotionJump", false, () -> this.longJump.getValue());
   BooleanSetting randomBoost = this.registerBoolean("RandomBoost", false);
   BooleanSetting lavaBoost = this.registerBoolean("LavaBoost", true);
   BooleanSetting SpeedInWater = this.registerBoolean("SpeedInWater", true);
   BooleanSetting strict = this.registerBoolean("Strict", false);
   BooleanSetting strictBoost = this.registerBoolean("StrictBoost", false, () -> this.damageBoost.getValue());
   BooleanSetting useTimer = this.registerBoolean("UseTimer", true);
   BooleanSetting jump = this.registerBoolean("Jump", true);
   BooleanSetting stepCheck = this.registerBoolean("Step Check", true);
   BooleanSetting bindCheck = this.registerBoolean("Use Bind", false, () -> this.stepCheck.getValue());
   StringSetting bind = this.registerString("Step Check Bind", "", () -> this.stepCheck.getValue() && this.bindCheck.getValue());
   DoubleSetting minStepHeight = this.registerDouble("Min Step Height", 1.0, 0.0, 10.0, () -> this.stepCheck.getValue());
   DoubleSetting maxStepHeight = this.registerDouble("Max Step Height", 2.5, 0.0, 10.0, () -> this.stepCheck.getValue());
   BooleanSetting test = this.registerBoolean("Test Mode", false, () -> this.stepCheck.getValue());
   Timing lagBackCoolDown = new Timing();
   Timing rdBoostTimer = new Timing();
   boolean lagDetected;
   boolean inCoolDown;
   boolean checkCoolDown;
   boolean warn;
   boolean checkStep;
   int readyStage;
   int stage = 1;
   int level = 1;
   double boostSpeed;
   double lastDist;
   double moveSpeed;
   double stepHigh;
   float boostFactor = 6.0F;
   long detectionTime;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (event.getPacket() instanceof SPacketPlayerPosLook) {
            this.lastDist = 0.0;
            this.moveSpeed = this.applySpeedPotionEffects();
            this.stage = 2;
            this.detectionTime = System.currentTimeMillis();
            this.lagDetected = true;
            this.rdBoostTimer.reset();
            this.boostFactor = 8.0F;
            if (this.longJump.getValue()) {
               this.readyStage = 0;
               this.inCoolDown = true;
               if (!this.checkCoolDown) {
                  this.lagBackCoolDown.reset();
                  this.checkCoolDown = true;
               }
            }
         }
      }
   });
   @EventHandler
   private final Listener<MotionUpdateEvent> motionUpdateEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            try {
               if (this.lagBackCoolDown.passedMs(this.lagCoolDown.getValue().intValue())) {
                  this.checkCoolDown = false;
                  this.inCoolDown = false;
                  this.lagBackCoolDown.reset();
               }

               if (System.currentTimeMillis() - this.detectionTime > 3182L) {
                  this.lagDetected = false;
               }

               if (this.useTimer.getValue()) {
                  TimerUtils.setTickLength(45.955883F);
               }

               if (event.stage == 1) {
                  this.lastDist = Math.sqrt(
                     (mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)
                        + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)
                  );
               }
            } catch (NumberFormatException var3) {
            }
         }
      }
   );
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (mc.player.movementInput.moveForward == 0.0F && mc.player.movementInput.moveStrafe == 0.0F) {
               event.setX(0.0);
               event.setZ(0.0);
               event.setSpeed(0.0);
            } else {
               if (this.checkStep && this.test.getValue()) {
                  double yaw = this.calcMoveYaw(
                     mc.player.rotationYaw, mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe
                  );
                  double dirX = -Math.sin(yaw);
                  double dirZ = Math.cos(yaw);
                  double dist = this.calcBlockDistAhead(dirX * 6.0, dirZ * 6.0);
                  double stepHeight = this.test.getValue() ? this.calcStepHeight(dist, dirX, dirZ) : this.stepHigh;
                  double multiplier = this.applySpeedPotionEffects();
                  if (stepHeight <= this.maxStepHeight.getValue()) {
                     if (dist < 3.0 * multiplier && stepHeight > this.minStepHeight.getValue() * 2.0) {
                        return;
                     }

                     if (dist < 1.4 * multiplier && stepHeight > this.minStepHeight.getValue()) {
                        return;
                     }
                  }
               }

               if (this.SpeedInWater.getValue() || !this.shouldReturn()) {
                  if (mc.player.onGround) {
                     this.level = 2;
                  }

                  if (round(mc.player.posY - (int)mc.player.posY, 3) == round(0.138, 3) && this.jump.getValue()) {
                     mc.player.motionY -= 0.07;
                     event.setY(event.getY() - 0.08316090325960147);
                     mc.player.posY -= 0.08316090325960147;
                  }

                  if (this.level != 1) {
                     if (this.level == 2) {
                        this.level = 3;
                        if (!mc.player.isInLava() && mc.player.onGround && this.jump.getValue()) {
                           event.setY(mc.player.motionY = this.applyJumpBoostPotionEffects());
                        }

                        if (!this.strict.getValue() && !mc.player.isSneaking()) {
                           this.moveSpeed *= 1.64847275;
                        } else {
                           this.moveSpeed *= 1.433;
                        }
                     } else if (this.level == 3) {
                        this.level = 4;
                        this.moveSpeed = this.lastDist - 0.6553 * (this.lastDist - this.applySpeedPotionEffects() + 0.04);
                     } else {
                        if (mc.player.onGround
                           && (
                              !mc.world
                                    .getCollisionBoxes(mc.player, mc.player.boundingBox.offset(0.0, mc.player.motionY, 0.0))
                                    .isEmpty()
                                 || mc.player.collidedVertically
                           )) {
                           this.level = 1;
                        }

                        this.moveSpeed = this.lastDist - this.lastDist / 201.0;
                     }
                  } else {
                     this.level = 2;
                     this.moveSpeed = 1.418 * this.applySpeedPotionEffects();
                  }

                  if (this.damageBoost.getValue() && ColorMain.INSTANCE.velocityBoost != 0.0) {
                     if (this.longJump.getValue()) {
                        this.readyStage++;
                     }

                     this.boostSpeed = ColorMain.INSTANCE.velocityBoost;
                     this.moveSpeed = this.moveSpeed + this.boostSpeed;
                     if (this.strictBoost.getValue()) {
                        this.moveSpeed = Math.max((this.moveSpeed + 0.1F) / 1.5, this.applySpeedPotionEffects());
                     }

                     ColorMain.INSTANCE.velocityBoost = 0.0;
                  }

                  if (this.randomBoost.getValue()
                     && this.rdBoostTimer.passedMs(3500L)
                     && !this.lagDetected
                     && MotionUtil.moving(mc.player)
                     && mc.player.onGround) {
                     this.moveSpeed = this.moveSpeed + this.moveSpeed / this.boostFactor;
                     this.boostFactor = 6.0F;
                     this.rdBoostTimer.reset();
                  }

                  if (this.longJump.getValue() && this.readyStage >= this.jumpStage.getValue() && !this.inCoolDown) {
                     if (!this.motionJump.getValue()) {
                        this.moveSpeed = this.moveSpeed * (this.jumpStage.getValue().intValue() / 10.0F);
                     } else {
                        motionJump();
                        mc.player.motionY *= 1.02;
                        mc.player.motionY *= 1.13;
                        mc.player.motionY *= 1.27;
                        this.moveSpeed = this.moveSpeed + Math.abs(this.moveSpeed - this.boostSpeed);
                     }

                     this.readyStage = 0;
                  }

                  this.moveSpeed = Math.max(this.moveSpeed, this.applySpeedPotionEffects());
                  if (!this.shouldReturn()) {
                     event.setSpeed(this.moveSpeed);
                  } else if (this.lavaBoost.getValue() && mc.player.isInLava()) {
                     event.setX(event.getX() * 3.1);
                     event.setZ(event.getZ() * 3.1);
                     if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        event.setY(event.getY() * 3.0);
                     }
                  }
               }
            }
         }
      }
   );
   @EventHandler
   private final Listener<StepEvent> stepEventListener = new Listener<>(event -> this.stepHigh = event.getBB().minY - mc.player.posY);

   @Override
   public void onTick() {
      this.checkStep = false;
      if (this.stepCheck.getValue()) {
         if (this.bindCheck.getValue()) {
            if (this.bind.getText().isEmpty() || !Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(this.bind.getText().charAt(0)))) {
               this.checkStep = !this.checkStep;
            }
         } else {
            this.checkStep = true;
         }
      }
   }

   public static double round(double n, int n2) {
      if (n2 < 0) {
         throw new IllegalArgumentException();
      } else {
         return new BigDecimal(n).setScale(n2, RoundingMode.HALF_UP).doubleValue();
      }
   }

   @Override
   public void onEnable() {
      if (mc.player == null) {
         this.disable();
      } else {
         this.boostSpeed = 0.0;
         this.lagBackCoolDown.reset();
         this.readyStage = 0;
         this.warn = false;
         this.moveSpeed = this.applySpeedPotionEffects();
      }
   }

   public static void motionJump() {
      if (!mc.player.collidedVertically) {
         if (mc.player.motionY == -0.07190068807140403) {
            mc.player.motionY *= 0.35F;
         } else if (mc.player.motionY == -0.10306193759436909) {
            mc.player.motionY *= 0.55F;
         } else if (mc.player.motionY == -0.13395038817442878) {
            mc.player.motionY *= 0.67F;
         } else if (mc.player.motionY == -0.16635183030382) {
            mc.player.motionY *= 0.69F;
         } else if (mc.player.motionY == -0.19088711097794803) {
            mc.player.motionY *= 0.71F;
         } else if (mc.player.motionY == -0.21121925191528862) {
            mc.player.motionY *= 0.2F;
         } else if (mc.player.motionY == -0.11979897632390576) {
            mc.player.motionY *= 0.93F;
         } else if (mc.player.motionY == -0.18758479151225355) {
            mc.player.motionY *= 0.72F;
         } else if (mc.player.motionY == -0.21075983825251726) {
            mc.player.motionY *= 0.76F;
         }

         if (mc.player.motionY < -0.2 && mc.player.motionY > -0.24) {
            mc.player.motionY *= 0.7;
         }

         if (mc.player.motionY < -0.25 && mc.player.motionY > -0.32) {
            mc.player.motionY *= 0.8;
         }

         if (mc.player.motionY < -0.35 && mc.player.motionY > -0.8) {
            mc.player.motionY *= 0.98;
         }

         if (mc.player.motionY < -0.8 && mc.player.motionY > -1.6) {
            mc.player.motionY *= 0.99;
         }
      }
   }

   public boolean shouldReturn() {
      return mc.player.isInLava() || mc.player.isInWater() || mc.player.isInWeb;
   }

   @Override
   public void onDisable() {
      this.moveSpeed = 0.0;
      this.stage = 2;
      if (mc.player != null) {
         mc.player.stepHeight = 0.6F;
         TimerUtils.setTickLength(50.0F);
      }
   }

   private double calcBlockDistAhead(double offsetX, double offsetZ) {
      if (mc.player.collidedHorizontally) {
         return 0.0;
      } else {
         AxisAlignedBB box = mc.player.boundingBox;
         double x = offsetX > 0.0 ? box.maxX : box.minX;
         double z = offsetX > 0.0 ? box.maxZ : box.minZ;
         return Math.min(
            this.rayTraceDist(new Vec3d(x, box.minY + 0.6, z), offsetX, offsetZ),
            this.rayTraceDist(new Vec3d(x, box.maxY + 0.6, z), offsetX, offsetZ)
         );
      }
   }

   private double rayTraceDist(Vec3d start, double offsetX, double offsetZ) {
      RayTraceResult result = mc.world.rayTraceBlocks(start, start.add(offsetX, 0.0, offsetZ), false, true, false);
      if (result != null && result.hitVec != null) {
         double x = start.x - result.hitVec.x;
         double z = start.z - result.hitVec.z;
         return Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0));
      } else {
         return 999.0;
      }
   }

   private double calcMoveYaw(float yaw, float moveForward, float moveStrafe) {
      double moveYaw = moveForward == 0.0F && moveStrafe == 0.0F ? 0.0 : Math.toDegrees(Math.atan2(moveForward, moveStrafe)) - 90.0;
      return Math.toRadians(RotationUtil.normalizeAngle(yaw + moveYaw));
   }

   private double calcStepHeight(double dist, double motionX, double motionZ) {
      BlockPos pos = PlayerUtil.getPlayerPos();
      if (mc.world.getBlockState(pos).getCollisionBoundingBox(mc.world, pos) != null) {
         return 0.0;
      } else {
         double i = Math.max(Math.round(dist), 1L);
         double minStepHeight = Double.MAX_VALUE;
         double x = motionX * i;
         double z = motionZ * i;
         minStepHeight = this.checkBox(minStepHeight, x, 0.0);
         minStepHeight = this.checkBox(minStepHeight, 0.0, z);
         return minStepHeight == Double.MAX_VALUE ? 0.0 : minStepHeight;
      }
   }

   private double checkBox(double minStepHeight, double offsetX, double offsetZ) {
      AxisAlignedBB box = mc.player.boundingBox.offset(offsetX, 0.0, offsetZ);
      if (!mc.world.collidesWithAnyBlock(box)) {
         return minStepHeight;
      } else {
         double stepHeight = minStepHeight;

         for (double y : new double[]{0.605, 1.005, 1.505, 2.005, 2.505}) {
            if (y > minStepHeight) {
               break;
            }

            AxisAlignedBB stepBox = new AxisAlignedBB(
               box.minX, box.minY + y - 0.5, box.minZ, box.maxX, box.minY + y, box.maxZ
            );
            List<AxisAlignedBB> boxList = mc.world.getCollisionBoxes(null, stepBox);
            AxisAlignedBB maxHeight = boxList.stream().max(Comparator.comparing(bb -> bb.maxY)).orElse(null);
            if (maxHeight != null) {
               double maxStepHeight = maxHeight.maxY - mc.player.posY;
               if (!mc.world.collidesWithAnyBlock(box.offset(0.0, maxStepHeight, 0.0))) {
                  stepHeight = maxStepHeight;
                  break;
               }
            }
         }

         return stepHeight;
      }
   }

   private double applySpeedPotionEffects() {
      double result = 0.2873;
      if (mc.player.getActivePotionEffect(MobEffects.SPEED) != null) {
         result += 0.2873 * (mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1.0) * 0.2;
      }

      if (mc.player.getActivePotionEffect(MobEffects.SLOWNESS) != null) {
         result -= 0.2873 * (mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier() + 1.0) * 0.15;
      }

      return result;
   }

   private double applyJumpBoostPotionEffects() {
      double result = 0.4;
      if (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST) != null) {
         result += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
      }

      return result;
   }
}
