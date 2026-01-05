package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.PredictUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoWeb", category = Category.Combat)
public class AutoWeb extends Module {
   ModeSetting page = this.registerMode("Page", Arrays.asList("Settings", "Predict"), "Settings");
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting rotate = this.registerBoolean("Rotate", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting packet = this.registerBoolean("Packet", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting swing = this.registerBoolean("Swing", true, () -> this.page.getValue().equals("Settings"));
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 2000, () -> this.page.getValue().equals("Settings"));
   IntegerSetting multiPlace = this.registerInteger("MultiPlace", 1, 1, 8, () -> this.page.getValue().equals("Settings"));
   BooleanSetting strict = this.registerBoolean("Strict", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting raytrace = this.registerBoolean("Raytrace", false, () -> this.page.getValue().equals("Settings"));
   BooleanSetting noInWeb = this.registerBoolean("NoInWeb", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting checkSelf = this.registerBoolean("CheckSelf", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting onlyGround = this.registerBoolean("SelfGround", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting down = this.registerBoolean("Down", false, () -> this.page.getValue().equals("Settings"));
   BooleanSetting face = this.registerBoolean("Face", false, () -> this.page.getValue().equals("Settings"));
   BooleanSetting feet = this.registerBoolean("Feet", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting onlyAir = this.registerBoolean("OnlyAir", true, () -> this.page.getValue().equals("Settings"));
   BooleanSetting air = this.registerBoolean("Air", true, () -> this.page.getValue().equals("Settings"));
   DoubleSetting minTargetSpeed = this.registerDouble("MinTargetSpeed", 10.0, 0.0, 50.0, () -> this.page.getValue().equals("Settings"));
   DoubleSetting range = this.registerDouble("Range", 5.0, 1.0, 6.0, () -> this.page.getValue().equals("Settings"));
   IntegerSetting tickPredict = this.registerInteger("Tick Predict", 8, 0, 30, () -> this.page.getValue().equals("Predict"));
   BooleanSetting calculateYPredict = this.registerBoolean("Calculate Y Predict", true, () -> this.page.getValue().equals("Predict"));
   IntegerSetting startDecrease = this.registerInteger(
      "Start Decrease", 39, 0, 200, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Predict")
   );
   IntegerSetting exponentStartDecrease = this.registerInteger(
      "Exponent Start", 2, 1, 5, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Predict")
   );
   IntegerSetting decreaseY = this.registerInteger("Decrease Y", 2, 1, 5, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Predict"));
   IntegerSetting exponentDecreaseY = this.registerInteger(
      "Exponent Decrease Y", 1, 1, 3, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Predict")
   );
   BooleanSetting splitXZ = this.registerBoolean("Split XZ", true, () -> this.page.getValue().equals("Predict"));
   BooleanSetting manualOutHole = this.registerBoolean("Manual Out Hole", false, () -> this.page.getValue().equals("Predict"));
   BooleanSetting aboveHoleManual = this.registerBoolean(
      "Above Hole Manual", false, () -> this.manualOutHole.getValue() && this.page.getValue().equals("Predict")
   );
   BooleanSetting stairPredict = this.registerBoolean("Stair Predict", false, () -> this.page.getValue().equals("Predict"));
   IntegerSetting nStair = this.registerInteger("N Stair", 2, 1, 4, () -> this.stairPredict.getValue() && this.page.getValue().equals("Predict"));
   DoubleSetting speedActivationStair = this.registerDouble(
      "Speed Activation Stair", 0.3, 0.0, 1.0, () -> this.stairPredict.getValue() && this.page.getValue().equals("Predict")
   );
   private final Timing timer = new Timing();
   private int progress = 0;

   @Override
   public void onTick() {
      if (this.timer.passedMs(this.delay.getValue().intValue())) {
         if (!this.onlyGround.getValue() || mc.player.onGround) {
            this.progress = 0;
            PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(
               this.tickPredict.getValue(),
               this.calculateYPredict.getValue(),
               this.startDecrease.getValue(),
               this.exponentStartDecrease.getValue(),
               this.decreaseY.getValue(),
               this.exponentDecreaseY.getValue(),
               this.splitXZ.getValue(),
               this.manualOutHole.getValue(),
               this.aboveHoleManual.getValue(),
               this.stairPredict.getValue(),
               this.nStair.getValue(),
               this.speedActivationStair.getValue()
            );

            for (EntityPlayer player : mc.world.playerEntities) {
               EntityPlayer target = PredictUtil.predictPlayer(player, settings);
               if (!EntityUtil.invalid(target, this.range.getValue() + 3.0)
                  && (!isInWeb(player) || !this.noInWeb.getValue())
                  && !(LemonClient.speedUtil.getPlayerSpeed(player) < this.minTargetSpeed.getValue())
                  && (!this.onlyAir.getValue() || !player.onGround)) {
                  if (this.down.getValue()) {
                     this.placeWeb(new BlockPos(target.posX, target.posY - 0.3, target.posZ));
                     this.placeWeb(new BlockPos(target.posX + 0.1, target.posY - 0.3, target.posZ + 0.1));
                     this.placeWeb(new BlockPos(target.posX - 0.1, target.posY - 0.3, target.posZ + 0.1));
                     this.placeWeb(new BlockPos(target.posX - 0.1, target.posY - 0.3, target.posZ - 0.1));
                     this.placeWeb(new BlockPos(target.posX + 0.1, target.posY - 0.3, target.posZ - 0.1));
                  }

                  if (this.face.getValue()) {
                     this.placeWeb(new BlockPos(target.posX + 0.2, target.posY + 1.5, target.posZ + 0.2));
                     this.placeWeb(new BlockPos(target.posX - 0.2, target.posY + 1.5, target.posZ + 0.2));
                     this.placeWeb(new BlockPos(target.posX - 0.2, target.posY + 1.5, target.posZ - 0.2));
                     this.placeWeb(new BlockPos(target.posX + 0.2, target.posY + 1.5, target.posZ - 0.2));
                  }

                  if (this.air.getValue()
                     && !player.onGround
                     && this.feet.getValue()
                     && !HoleUtil.isHoleBlock(EntityUtil.getEntityPos(target), true, false, false)) {
                     this.placeWeb(new BlockPos(target.posX + 0.2, target.posY + 0.5, target.posZ + 0.2));
                     this.placeWeb(new BlockPos(target.posX - 0.2, target.posY + 0.5, target.posZ + 0.2));
                     this.placeWeb(new BlockPos(target.posX - 0.2, target.posY + 0.5, target.posZ - 0.2));
                     this.placeWeb(new BlockPos(target.posX + 0.2, target.posY + 0.5, target.posZ - 0.2));
                  }
               }
            }
         }
      }
   }

   public static boolean isInWeb(EntityPlayer player) {
      if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 1.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 1.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 1.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 1.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY - 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY - 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY - 0.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY - 0.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 0.5, player.posZ + 0.3))) {
         return true;
      } else {
         return isWeb(new BlockPos(player.posX - 0.3, player.posY + 0.5, player.posZ - 0.3))
            ? true
            : isWeb(new BlockPos(player.posX + 0.3, player.posY + 0.5, player.posZ - 0.3));
      }
   }

   private static boolean isWeb(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() == Blocks.WEB && checkEntity(pos);
   }

   private boolean isSelf(BlockPos pos) {
      if (!this.checkSelf.getValue()) {
         return false;
      } else {
         for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity == mc.player) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean checkEntity(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer && entity != mc.player) {
            return true;
         }
      }

      return false;
   }

   private void placeWeb(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue() && !(PlayerUtil.getDistance(pos) > this.range.getValue())) {
         if (mc.world.isAirBlock(pos.up())) {
            if (this.canPlace(pos)) {
               if (!this.isSelf(pos)) {
                  if (BurrowUtil.findHotbarBlock(BlockWeb.class) != -1) {
                     InventoryUtil.run(
                        BurrowUtil.findHotbarBlock(BlockWeb.class),
                        this.packetSwitch.getValue(),
                        () -> BlockUtil.placeBlock(
                           pos, this.rotate.getValue(), this.packet.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue()
                        )
                     );
                     this.progress++;
                     this.timer.reset();
                  }
               }
            }
         }
      }
   }

   private boolean canPlace(BlockPos pos) {
      if (!BlockUtil.canBlockFacing(pos)) {
         return false;
      } else {
         return !BlockUtil.canReplace(pos) ? false : this.strictPlaceCheck(pos);
      }
   }

   private boolean strictPlaceCheck(BlockPos pos) {
      if (!this.strict.getValue() && this.raytrace.getValue()) {
         return true;
      } else {
         for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, this.raytrace.getValue())) {
            if (BlockUtil.canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }
}
