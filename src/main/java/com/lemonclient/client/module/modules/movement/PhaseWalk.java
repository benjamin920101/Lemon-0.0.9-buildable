package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {
   BooleanSetting phaseCheck = this.registerBoolean("Only In Block", true);
   ModeSetting noClipMode = this.registerMode("NoClipMode", Arrays.asList("Bypass", "NoClip", "None", "Fall"), "NoClip");
   BooleanSetting fallPacket = this.registerBoolean("Fall Packet", true);
   BooleanSetting sprintPacket = this.registerBoolean("Sprint Packet", true);
   BooleanSetting instantWalk = this.registerBoolean("Instant Walk", true);
   BooleanSetting antiVoid = this.registerBoolean("Anti Void", false);
   BooleanSetting clip = this.registerBoolean("Clip", true);
   IntegerSetting antiVoidHeight = this.registerInteger("Anti Void Height", 5, 1, 100);
   DoubleSetting instantWalkSpeed = this.registerDouble("Instant Speed", 1.8, 0.1, 2.0, () -> this.instantWalk.getValue());
   DoubleSetting phaseSpeed = this.registerDouble("Phase Walk Speed", 42.4, 0.1, 70.0);
   BooleanSetting downOnShift = this.registerBoolean("Phase Down When Crouch", true);
   BooleanSetting stopMotion = this.registerBoolean("Attempt Clips", true);
   IntegerSetting stopMotionDelay = this.registerInteger("Attempt Clips Delay", 5, 0, 20, () -> this.stopMotion.getValue());
   int delay;

   @Override
   public void onDisable() {
      mc.player.noClip = false;
   }

   private boolean air(BlockPos pos) {
      Block blockState = BlockUtil.getBlock(pos);
      return !BlockUtil.airBlocks.contains(blockState) && blockState != Blocks.WEB;
   }

   @Override
   public void onUpdate() {
      this.delay++;
      double n = this.phaseSpeed.getValue() / 1000.0;
      double n2 = this.instantWalkSpeed.getValue() / 10.0;
      RayTraceResult rayTraceBlocks;
      if (this.antiVoid.getValue()
         && mc.player.posY <= this.antiVoidHeight.getValue().intValue()
         && (
            (
                     rayTraceBlocks = mc.world
                        .rayTraceBlocks(
                           mc.player.getPositionVector(),
                           new Vec3d(mc.player.posX, 0.0, mc.player.posZ),
                           false,
                           false,
                           false
                        )
                  )
                  == null
               || rayTraceBlocks.typeOfHit != Type.BLOCK
         )) {
         mc.player.setVelocity(0.0, 0.0, 0.0);
      }

      if (this.phaseCheck.getValue()) {
         if ((
               mc.gameSettings.keyBindForward.isKeyDown()
                  || mc.gameSettings.keyBindRight.isKeyDown()
                  || mc.gameSettings.keyBindLeft.isKeyDown()
                  || mc.gameSettings.keyBindBack.isKeyDown()
                  || mc.gameSettings.keyBindSneak.isKeyDown()
            )
            && (!this.eChestCheck() && this.air(PlayerUtil.getPlayerPos()) || this.air(PlayerUtil.getPlayerPos().up()))) {
            if (mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isPressed() && mc.player.isSneaking()) {
               double[] motion = this.getMotion(n);
               if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX + motion[0],
                           mc.player.posY - 0.0424,
                           mc.player.posZ + motion[1],
                           mc.player.rotationYaw,
                           mc.player.rotationPitch,
                           false
                        )
                     );
               } else {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX + motion[0],
                           mc.player.posY,
                           mc.player.posZ + motion[1],
                           mc.player.rotationYaw,
                           mc.player.rotationPitch,
                           false
                        )
                     );
               }

               if (this.noClipMode.getValue().equals("Fall")) {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX,
                           -1300.0,
                           mc.player.posZ,
                           mc.player.rotationYaw * -5.0F,
                           mc.player.rotationPitch * -5.0F,
                           true
                        )
                     );
               }

               if (this.noClipMode.getValue().equals("NoClip")) {
                  mc.player.setVelocity(0.0, 0.0, 0.0);
                  if (mc.gameSettings.keyBindForward.isKeyDown()
                     || mc.gameSettings.keyBindBack.isKeyDown()
                     || mc.gameSettings.keyBindLeft.isKeyDown()
                     || mc.gameSettings.keyBindRight.isKeyDown()) {
                     double[] directionSpeed = MathUtil.directionSpeed(0.06F);
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX + directionSpeed[0],
                              mc.player.posY,
                              mc.player.posZ + directionSpeed[1],
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY - 0.06F,
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }

                  if (mc.gameSettings.keyBindJump.isKeyDown()) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + 0.06F,
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }
               }

               if (this.noClipMode.getValue().equals("Bypass")) {
                  mc.player.noClip = true;
               }

               if (this.fallPacket.getValue()) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_RIDING_JUMP));
               }

               if (this.sprintPacket.getValue()) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
               }

               if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .setPosition(
                        mc.player.posX + motion[0], mc.player.posY - 0.0424, mc.player.posZ + motion[1]
                     );
               } else {
                  mc.player
                     .setPosition(mc.player.posX + motion[0], mc.player.posY, mc.player.posZ + motion[1]);
               }

               mc.player.motionZ = 0.0;
               mc.player.motionY = 0.0;
               mc.player.motionX = 0.0;
               mc.player.noClip = true;
            }

            if (mc.player.collidedHorizontally
               && this.clip.getValue()
               && !mc.gameSettings.keyBindForward.isKeyDown()
               && !mc.gameSettings.keyBindBack.isKeyDown()
               && !mc.gameSettings.keyBindLeft.isKeyDown()) {
               mc.gameSettings.keyBindRight.isKeyDown();
            }

            if (mc.player.collidedHorizontally && this.stopMotion.getValue() ? this.delay >= this.stopMotionDelay.getValue() : mc.player.collidedHorizontally) {
               double[] motion2 = this.getMotion(n);
               if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX + motion2[0],
                           mc.player.posY - 0.1,
                           mc.player.posZ + motion2[1],
                           mc.player.rotationYaw,
                           mc.player.rotationPitch,
                           false
                        )
                     );
               } else {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX + motion2[0],
                           mc.player.posY,
                           mc.player.posZ + motion2[1],
                           mc.player.rotationYaw,
                           mc.player.rotationPitch,
                           false
                        )
                     );
               }

               if (this.noClipMode.getValue().equals("Fall")) {
                  mc.player
                     .connection
                     .sendPacket(
                        new PositionRotation(
                           mc.player.posX,
                           -1300.0,
                           mc.player.posZ,
                           mc.player.rotationYaw * -5.0F,
                           mc.player.rotationPitch * -5.0F,
                           true
                        )
                     );
               }

               if (this.noClipMode.getValue().equals("NoClip")) {
                  mc.player.setVelocity(0.0, 0.0, 0.0);
                  if (mc.gameSettings.keyBindForward.isKeyDown()
                     || mc.gameSettings.keyBindBack.isKeyDown()
                     || mc.gameSettings.keyBindLeft.isKeyDown()
                     || mc.gameSettings.keyBindRight.isKeyDown()) {
                     double[] directionSpeed2 = MathUtil.directionSpeed(0.06F);
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX + directionSpeed2[0],
                              mc.player.posY,
                              mc.player.posZ + directionSpeed2[1],
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY - 0.06F,
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }

                  if (mc.gameSettings.keyBindJump.isKeyDown()) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + 0.06F,
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
                  }
               }

               if (this.noClipMode.getValue().equals("Bypass")) {
                  mc.player.noClip = true;
               }

               if (this.fallPacket.getValue()) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_RIDING_JUMP));
               }

               if (this.sprintPacket.getValue()) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
               }

               if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .setPosition(
                        mc.player.posX + motion2[0], mc.player.posY - 0.1, mc.player.posZ + motion2[1]
                     );
               } else {
                  mc.player
                     .setPosition(mc.player.posX + motion2[0], mc.player.posY, mc.player.posZ + motion2[1]);
               }

               mc.player.motionZ = 0.0;
               mc.player.motionY = 0.0;
               mc.player.motionX = 0.0;
               mc.player.noClip = true;
               this.delay = 0;
               return;
            }

            if (this.instantWalk.getValue()) {
               double[] directionSpeed3 = MathUtil.directionSpeed(n2);
               mc.player.motionX = directionSpeed3[0];
               mc.player.motionZ = directionSpeed3[1];
            }
         }
      } else if (mc.gameSettings.keyBindForward.isKeyDown()
         || mc.gameSettings.keyBindRight.isKeyDown()
         || mc.gameSettings.keyBindLeft.isKeyDown()
         || mc.gameSettings.keyBindBack.isKeyDown()
         || mc.gameSettings.keyBindSneak.isKeyDown()) {
         if (mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isPressed() && mc.player.isSneaking()) {
            double[] motion3 = this.getMotion(n);
            if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX + motion3[0],
                        mc.player.posY - 0.0424,
                        mc.player.posZ + motion3[1],
                        mc.player.rotationYaw,
                        mc.player.rotationPitch,
                        false
                     )
                  );
            } else {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX + motion3[0],
                        mc.player.posY,
                        mc.player.posZ + motion3[1],
                        mc.player.rotationYaw,
                        mc.player.rotationPitch,
                        false
                     )
                  );
            }

            if (this.noClipMode.getValue().equals("Fall")) {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX,
                        -1300.0,
                        mc.player.posZ,
                        mc.player.rotationYaw * -5.0F,
                        mc.player.rotationPitch * -5.0F,
                        true
                     )
                  );
            }

            if (this.noClipMode.getValue().equals("NoClip")) {
               mc.player.setVelocity(0.0, 0.0, 0.0);
               if (mc.gameSettings.keyBindForward.isKeyDown()
                  || mc.gameSettings.keyBindBack.isKeyDown()
                  || mc.gameSettings.keyBindLeft.isKeyDown()
                  || mc.gameSettings.keyBindRight.isKeyDown()) {
                  double[] directionSpeed4 = MathUtil.directionSpeed(0.06F);
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX + directionSpeed4[0],
                           mc.player.posY,
                           mc.player.posZ + directionSpeed4[1],
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }

               if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX,
                           mc.player.posY - 0.06F,
                           mc.player.posZ,
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }

               if (mc.gameSettings.keyBindJump.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX,
                           mc.player.posY + 0.06F,
                           mc.player.posZ,
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }
            }

            if (this.noClipMode.getValue().equals("Bypass")) {
               mc.player.noClip = true;
            }

            if (this.fallPacket.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_RIDING_JUMP));
            }

            if (this.sprintPacket.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
            }

            if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
               mc.player
                  .setPosition(
                     mc.player.posX + motion3[0], mc.player.posY - 0.0424, mc.player.posZ + motion3[1]
                  );
            } else {
               mc.player
                  .setPosition(mc.player.posX + motion3[0], mc.player.posY, mc.player.posZ + motion3[1]);
            }

            mc.player.motionZ = 0.0;
            mc.player.motionY = 0.0;
            mc.player.motionX = 0.0;
            mc.player.noClip = true;
         }

         if (mc.player.collidedHorizontally && this.stopMotion.getValue() ? this.delay >= this.stopMotionDelay.getValue() : mc.player.collidedHorizontally) {
            double[] motion4 = this.getMotion(n);
            if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX + motion4[0],
                        mc.player.posY - 0.1,
                        mc.player.posZ + motion4[1],
                        mc.player.rotationYaw,
                        mc.player.rotationPitch,
                        false
                     )
                  );
            } else {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX + motion4[0],
                        mc.player.posY,
                        mc.player.posZ + motion4[1],
                        mc.player.rotationYaw,
                        mc.player.rotationPitch,
                        false
                     )
                  );
            }

            if (this.noClipMode.getValue().equals("Fall")) {
               mc.player
                  .connection
                  .sendPacket(
                     new PositionRotation(
                        mc.player.posX,
                        -1300.0,
                        mc.player.posZ,
                        mc.player.rotationYaw * -5.0F,
                        mc.player.rotationPitch * -5.0F,
                        true
                     )
                  );
            }

            if (this.noClipMode.getValue().equals("NoClip")) {
               mc.player.setVelocity(0.0, 0.0, 0.0);
               if (mc.gameSettings.keyBindForward.isKeyDown()
                  || mc.gameSettings.keyBindBack.isKeyDown()
                  || mc.gameSettings.keyBindLeft.isKeyDown()
                  || mc.gameSettings.keyBindRight.isKeyDown()) {
                  double[] directionSpeed5 = MathUtil.directionSpeed(0.06F);
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX + directionSpeed5[0],
                           mc.player.posY,
                           mc.player.posZ + directionSpeed5[1],
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }

               if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX,
                           mc.player.posY - 0.06F,
                           mc.player.posZ,
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }

               if (mc.gameSettings.keyBindJump.isKeyDown()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX,
                           mc.player.posY + 0.06F,
                           mc.player.posZ,
                           mc.player.onGround
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, 0.0, mc.player.posZ, mc.player.onGround));
               }
            }

            if (this.noClipMode.getValue().equals("Bypass")) {
               mc.player.noClip = true;
            }

            if (this.fallPacket.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_RIDING_JUMP));
            }

            if (this.sprintPacket.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
            }

            if (this.downOnShift.getValue() && mc.player.collidedVertically && mc.gameSettings.keyBindSneak.isKeyDown()) {
               mc.player
                  .setPosition(mc.player.posX + motion4[0], mc.player.posY - 0.1, mc.player.posZ + motion4[1]);
            } else {
               mc.player
                  .setPosition(mc.player.posX + motion4[0], mc.player.posY, mc.player.posZ + motion4[1]);
            }

            mc.player.motionZ = 0.0;
            mc.player.motionY = 0.0;
            mc.player.motionX = 0.0;
            mc.player.noClip = true;
            this.delay = 0;
            return;
         }

         if (this.instantWalk.getValue()) {
            double[] directionSpeed6 = MathUtil.directionSpeed(n2);
            mc.player.motionX = directionSpeed6[0];
            mc.player.motionZ = directionSpeed6[1];
         }
      }
   }

   private boolean eChestCheck() {
      return String.valueOf(mc.player.posY).split("\\.")[1].equals("875")
         || String.valueOf(mc.player.posY).split("\\.")[1].equals("5");
   }

   private double[] getMotion(double n) {
      float moveForward = mc.player.movementInput.moveForward;
      float moveStrafe = mc.player.movementInput.moveStrafe;
      float n2 = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (moveForward != 0.0F) {
         if (moveStrafe > 0.0F) {
            n2 += moveForward > 0.0F ? -45 : 45;
         } else if (moveStrafe < 0.0F) {
            n2 += moveForward > 0.0F ? 45 : -45;
         }

         moveStrafe = 0.0F;
         if (moveForward > 0.0F) {
            moveForward = 1.0F;
         } else if (moveForward < 0.0F) {
            moveForward = -1.0F;
         }
      }

      return new double[]{
         moveForward * n * -Math.sin(Math.toRadians(n2)) + moveStrafe * n * Math.cos(Math.toRadians(n2)),
         moveForward * n * Math.cos(Math.toRadians(n2)) - moveStrafe * n * -Math.sin(Math.toRadians(n2))
      };
   }
}
