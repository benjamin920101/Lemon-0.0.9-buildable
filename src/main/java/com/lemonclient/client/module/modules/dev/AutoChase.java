package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.event.events.StepEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.api.util.world.TimerUtils;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;

@Module.Declaration(name = "AutoChase", category = Category.Dev, priority = 120)
public class AutoChase extends Module {
   IntegerSetting targetRange = this.registerInteger("Target Range", 16, 0, 256);
   IntegerSetting fixedRange = this.registerInteger("Fixed Target Range", 16, 0, 256);
   IntegerSetting cancelRange = this.registerInteger("Cancel Range", 6, 0, 16);
   IntegerSetting downRange = this.registerInteger("Down Range", 5, 0, 8);
   IntegerSetting upRange = this.registerInteger("Up Range", 1, 0, 8);
   DoubleSetting hRange = this.registerDouble("H Range", 4.0, 1.0, 8.0);
   DoubleSetting timer = this.registerDouble("Timer", 2.0, 1.0, 50.0);
   DoubleSetting speed = this.registerDouble("Speed", 2.0, 0.0, 10.0);
   BooleanSetting step = this.registerBoolean("Step", true);
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("NCP", "Vanilla"), "NCP", () -> this.step.getValue());
   ModeSetting height = this.registerMode(
      "NCP Height", Arrays.asList("1", "1.5", "2", "2.5", "3", "4"), "2.5", () -> this.mode.getValue().equalsIgnoreCase("NCP") && this.step.getValue()
   );
   ModeSetting vHeight = this.registerMode(
      "Vanilla Height", Arrays.asList("1", "1.5", "2", "2.5", "3", "4"), "2.5", () -> this.mode.getValue().equalsIgnoreCase("Vanilla") && this.step.getValue()
   );
   BooleanSetting abnormal = this.registerBoolean("Abnormal", false, () -> !this.mode.getValue().equalsIgnoreCase("Vanilla") && this.step.getValue());
   IntegerSetting centerSpeed = this.registerInteger("Center Speed", 2, 10, 1);
   BooleanSetting only = this.registerBoolean("Only 1x1", true);
   BooleanSetting single = this.registerBoolean("Single Hole", true, () -> !this.only.getValue());
   BooleanSetting twoBlocks = this.registerBoolean("Double Hole", true, () -> !this.only.getValue());
   BooleanSetting custom = this.registerBoolean("Custom Hole", true, () -> !this.only.getValue());
   BooleanSetting four = this.registerBoolean("Four Blocks", true, () -> !this.only.getValue());
   BooleanSetting near = this.registerBoolean("Near Target", true);
   BooleanSetting disable = this.registerBoolean("Disable", true);
   BooleanSetting hud = this.registerBoolean("Hud", true);
   private int stuckTicks = 0;
   BlockPos originPos;
   BlockPos startPos;
   boolean isActive;
   boolean wasInHole;
   boolean slowDown;
   double playerSpeed;
   EntityPlayer target;
   @EventHandler
   private final Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
      if (event.getMovementInput() instanceof MovementInputFromOptions && this.isActive) {
         event.getMovementInput().jump = false;
         event.getMovementInput().sneak = false;
         event.getMovementInput().forwardKeyDown = false;
         event.getMovementInput().backKeyDown = false;
         event.getMovementInput().leftKeyDown = false;
         event.getMovementInput().rightKeyDown = false;
         event.getMovementInput().moveForward = 0.0F;
         event.getMovementInput().moveStrafe = 0.0F;
      }
   });
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveListener = new Listener<>(
      event -> {
         this.isActive = false;
         TimerUtils.setTickLength(50.0F);
         if (mc.player.isEntityAlive() && !mc.player.isElytraFlying() && !mc.player.capabilities.isFlying) {
            double currentSpeed = Math.hypot(mc.player.motionX, mc.player.motionZ);
            if (currentSpeed <= 0.05) {
               this.originPos = PlayerUtil.getPlayerPos();
            }

            this.target = this.getNearestPlayer(this.target);
            if (this.target != null) {
               double range = mc.player.getDistance(this.target);
               boolean inRange = range <= this.cancelRange.getValue().intValue();
               if (this.shouldDisable(currentSpeed, inRange)) {
                  if (this.disable.getValue()) {
                     this.disable();
                  }
               } else {
                  BlockPos hole = this.findHoles(this.target, inRange);
                  if (hole != null) {
                     double x = hole.getX() + 0.5;
                     double y = hole.getY();
                     double z = hole.getZ() + 0.5;
                     if (this.checkYRange((int)mc.player.posY, this.originPos.y)) {
                        Vec3d playerPos = mc.player.getPositionVector();
                        double yawRad = Math.toRadians(RotationUtil.getRotationTo(playerPos, new Vec3d(x, y, z)).x);
                        double dist = Math.hypot(x - playerPos.x, z - playerPos.z);
                        if (mc.player.onGround) {
                           this.playerSpeed = MotionUtil.getBaseMoveSpeed()
                              * (EntityUtil.isColliding(0.0, -0.5, 0.0) instanceof BlockLiquid && !EntityUtil.isInLiquid() ? 0.91 : this.speed.getValue());
                           this.slowDown = true;
                        }

                        double speed = Math.min(dist, this.playerSpeed);
                        mc.player.motionX = 0.0;
                        mc.player.motionZ = 0.0;
                        event.setX(-Math.sin(yawRad) * speed);
                        event.setZ(Math.cos(yawRad) * speed);
                        if (speed != 0.0 && (-Math.sin(yawRad) != 0.0 || Math.cos(yawRad) != 0.0)) {
                           TimerUtils.setTickLength((float)(50.0 / this.timer.getValue()));
                           this.isActive = true;
                        }
                     }
                  }

                  if (mc.player.collidedHorizontally && hole == null) {
                     this.stuckTicks++;
                  } else {
                     this.stuckTicks = 0;
                  }
               }
            }
         }
      }
   );
   double[] pointFiveToOne = new double[]{0.41999998688698};
   double[] one = new double[]{0.41999998688698, 0.7531999805212};
   double[] oneFive = new double[]{0.42, 0.753, 1.001, 1.084, 1.006};
   double[] oneSixTwoFive = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372};
   double[] oneEightSevenFive = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652};
   double[] two = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869};
   double[] twoFive = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
   double[] threeStep = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43, 1.78, 1.63, 1.51, 1.9, 2.21, 2.45, 2.43};
   double[] fourStep = new double[]{
      0.42, 0.75, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43, 1.78, 1.63, 1.51, 1.9, 2.21, 2.45, 2.43, 2.78, 2.63, 2.51, 2.9, 3.21, 3.45, 3.43
   };
   double[] betaShared = new double[]{0.419999986887, 0.7531999805212, 1.0013359791121, 1.1661092609382, 1.249187078744682, 1.176759275064238};
   double[] betaTwo = new double[]{1.596759261951216, 1.929959255585439};
   double[] betaTwoFive = new double[]{1.596759261951216, 1.929959255585439, 2.178095254176385, 2.3428685360024515, 2.425946353808919};
   @EventHandler
   private final Listener<StepEvent> stepEventListener = new Listener<>(event -> {
      if (this.canStep()) {
         double step = event.getBB().minY - mc.player.posY;
         if (!this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            if (this.mode.getValue().equalsIgnoreCase("NCP")) {
               if (step == 0.625 && this.abnormal.getValue()) {
                  this.sendOffsets(this.pointFiveToOne);
               } else if (step != 1.0 && (step != 0.875 && step != 1.0625 && step != 0.9375 || !this.abnormal.getValue())) {
                  if (step == 1.5) {
                     this.sendOffsets(this.oneFive);
                  } else if (step == 1.875 && this.abnormal.getValue()) {
                     this.sendOffsets(this.oneEightSevenFive);
                  } else if (step == 1.625 && this.abnormal.getValue()) {
                     this.sendOffsets(this.oneSixTwoFive);
                  } else if (step == 2.0) {
                     this.sendOffsets(this.two);
                  } else if (step == 2.5) {
                     this.sendOffsets(this.twoFive);
                  } else if (step == 3.0) {
                     this.sendOffsets(this.threeStep);
                  } else if (step == 4.0) {
                     this.sendOffsets(this.fourStep);
                  } else {
                     event.cancel();
                  }
               } else {
                  this.sendOffsets(this.one);
               }
            } else if (this.mode.getValue().equalsIgnoreCase("Beta")) {
               if (step == 1.5) {
                  this.sendOffsets(this.betaShared);
               } else if (step == 2.0) {
                  this.sendOffsets(this.betaShared);
                  this.sendOffsets(this.betaTwo);
               } else if (step == 2.5) {
                  this.sendOffsets(this.betaShared);
                  this.sendOffsets(this.betaTwoFive);
               } else if (step == 3.0) {
                  this.sendOffsets(this.betaShared);
                  this.sendOffsets(this.threeStep);
               } else if (step == 4.0) {
                  this.sendOffsets(this.betaShared);
                  this.sendOffsets(this.fourStep);
               } else {
                  event.cancel();
               }
            }
         }
      }
   });

   private EntityPlayer getNearestPlayer(EntityPlayer target) {
      return target != null && mc.player.getDistance(target) <= this.fixedRange.getValue().intValue() && !EntityUtil.basicChecksEntity(target)
         ? target
         : mc.world
            .playerEntities
            .stream()
            .filter(p -> mc.player.getDistance(p) <= this.targetRange.getValue().intValue())
            .filter(p -> mc.player.entityId != p.entityId)
            .filter(p -> !EntityUtil.basicChecksEntity(p))
            .min(Comparator.comparing(p -> mc.player.getDistance(p)))
            .orElse(null);
   }

   @Override
   public void onEnable() {
      this.wasInHole = false;
      this.startPos = this.originPos = PlayerUtil.getPlayerPos();
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead && this.startPos != null) {
         if (this.canStep()) {
            mc.player.stepHeight = this.getHeight(this.mode.getValue());
         } else {
            if (mc.player.getRidingEntity() != null) {
               mc.player.getRidingEntity().stepHeight = 1.0F;
            }

            mc.player.stepHeight = 0.6F;
         }

         if (this.target == null) {
            this.isActive = false;
         }
      } else {
         this.disable();
      }
   }

   @Override
   public void onDisable() {
      this.isActive = false;
      this.stuckTicks = 0;
      TimerUtils.setTickLength(50.0F);
      if (mc.player != null) {
         if (mc.player.getRidingEntity() != null) {
            mc.player.getRidingEntity().stepHeight = 1.0F;
         }

         mc.player.stepHeight = 0.6F;
      }
   }

   private BlockPos findHoles(EntityPlayer target, boolean inRange) {
      if (inRange && this.wasInHole) {
         return null;
      } else {
         this.wasInHole = false;
         NonNullList<BlockPos> holes = NonNullList.create();
         List<BlockPos> blockPosList = EntityUtil.getSphere(EntityUtil.getPlayerPos(target), this.hRange.getValue(), 8.0, false, true, 0);
         blockPosList.forEach(
            pos -> {
               if (this.checkYRange((int)mc.player.posY, pos.y)) {
                  if (mc.world.isAirBlock(PlayerUtil.getPlayerPos().up(2)) || (int)mc.player.posY >= pos.y) {
                     HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, this.only.getValue(), false, false);
                     HoleUtil.HoleType holeType = holeInfo.getType();
                     if (holeType != HoleUtil.HoleType.NONE) {
                        if (this.only.getValue()) {
                           if (holeType != HoleUtil.HoleType.SINGLE) {
                              return;
                           }
                        } else {
                           if (!this.single.getValue() && holeType == HoleUtil.HoleType.SINGLE) {
                              return;
                           }

                           if (!this.twoBlocks.getValue() && holeType == HoleUtil.HoleType.DOUBLE) {
                              return;
                           }

                           if (!this.custom.getValue() && holeType == HoleUtil.HoleType.CUSTOM) {
                              return;
                           }

                           if (!this.four.getValue() && holeType == HoleUtil.HoleType.FOUR) {
                              return;
                           }
                        }

                        if (mc.world.isAirBlock(pos)
                           && mc.world.isAirBlock(pos.add(0, 1, 0))
                           && mc.world.isAirBlock(pos.add(0, 2, 0))) {
                           for (int high = 0; high < mc.player.posY - pos.y; high++) {
                              if (high != 0) {
                                 if (mc.player.posY > pos.y
                                    && !mc.world.isAirBlock(new BlockPos(pos.x, pos.y + high, pos.z))) {
                                    return;
                                 }

                                 if (mc.player.posY < pos.y) {
                                    BlockPos newPos = new BlockPos(pos.x, pos.y + high, pos.z);
                                    if (mc.world.isAirBlock(newPos)
                                       && (mc.world.isAirBlock(newPos.down()) || mc.world.isAirBlock(newPos.up()))) {
                                       return;
                                    }
                                 }
                              }
                           }

                           holes.add(pos);
                        }
                     }
                  }
               }
            }
         );
         return holes.stream()
            .min(
               Comparator.comparing(
                  p -> this.near.getValue()
                     ? target.getDistance(p.x + 0.5, p.y, p.z + 0.5)
                     : mc.player.getDistance(p.x + 0.5, p.y, p.z + 0.5)
               )
            )
            .orElse(null);
      }
   }

   private boolean shouldDisable(Double currentSpeed, boolean inRange) {
      if (this.isActive) {
         return false;
      } else if (!mc.player.onGround) {
         return false;
      } else if (this.stuckTicks > 5 && currentSpeed < 0.05) {
         return true;
      } else {
         HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(
            new BlockPos(PlayerUtil.getPlayerPos().x, PlayerUtil.getPlayerPos().y + 0.5, PlayerUtil.getPlayerPos().z),
            false,
            false,
            false
         );
         HoleUtil.HoleType holeType = holeInfo.getType();
         if (holeType != HoleUtil.HoleType.NONE && inRange) {
            if (this.only.getValue()) {
               if (holeType != HoleUtil.HoleType.SINGLE) {
                  return false;
               }
            } else {
               if (!this.single.getValue() && holeType == HoleUtil.HoleType.SINGLE) {
                  return false;
               }

               if (!this.twoBlocks.getValue() && holeType == HoleUtil.HoleType.DOUBLE) {
                  return false;
               }

               if (!this.custom.getValue() && holeType == HoleUtil.HoleType.CUSTOM) {
                  return false;
               }

               if (!this.four.getValue() && holeType == HoleUtil.HoleType.FOUR) {
                  return false;
               }
            }

            Vec3d center = this.getCenter(holeInfo.getCentre());
            double XDiff = Math.abs(center.x - mc.player.posX);
            double ZDiff = Math.abs(center.z - mc.player.posZ);
            if ((!(XDiff <= 0.3) || !(ZDiff <= 0.3)) && !this.wasInHole) {
               double MotionX = center.x - mc.player.posX;
               double MotionZ = center.z - mc.player.posZ;
               mc.player.motionX = MotionX / this.centerSpeed.getValue().intValue();
               mc.player.motionZ = MotionZ / this.centerSpeed.getValue().intValue();
            }

            this.wasInHole = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public Vec3d getCenter(AxisAlignedBB box) {
      boolean air = mc.world.isAirBlock(new BlockPos(box.minX, box.minY + 1.0, box.minZ));
      return air
         ? new Vec3d(
            box.minX + (box.maxX - box.minX) / 2.0,
            box.minY,
            box.minZ + (box.maxZ - box.minZ) / 2.0
         )
         : new Vec3d(box.maxX - 0.5, box.minY, box.maxZ - 0.5);
   }

   private boolean checkYRange(int playerY, int holeY) {
      return playerY >= holeY ? playerY - holeY <= this.downRange.getValue() : holeY - playerY <= -this.upRange.getValue();
   }

   float getHeight(String mode) {
      return Float.parseFloat(mode.equals("Vanilla") ? this.vHeight.getValue() : this.height.getValue());
   }

   protected boolean canStep() {
      return !mc.player.isInWater()
         && mc.player.onGround
         && !mc.player.isOnLadder()
         && !mc.player.movementInput.jump
         && mc.player.collidedVertically
         && mc.player.fallDistance < 0.1
         && this.step.getValue()
         && this.isActive;
   }

   void sendOffsets(double[] offsets) {
      for (double i : offsets) {
         mc.player
            .connection
            .sendPacket(new Position(mc.player.posX, mc.player.posY + i + 0.0, mc.player.posZ, false));
      }
   }

   @Override
   public String getHudInfo() {
      return this.hud.getValue()
         ? "["
            + ChatFormatting.WHITE
            + (this.target == null ? "None" : this.target.getName() + ", " + (this.isActive ? "Chasing" : "Pausing"))
            + ChatFormatting.GRAY
            + "]"
         : "";
   }
}
