package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.PredictUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.qwq.AutoEz;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoCreeper", category = Category.Combat)
public class AutoCreeper extends Module {
   DoubleSetting minDamage = this.registerDouble("Min Damage", 6.0, 0.0, 36.0);
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 1000);
   DoubleSetting enemyRange = this.registerDouble("Enemy Range", 10.0, 0.0, 16.0);
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 6.0);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting packet = this.registerBoolean("Packet", false);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", false);
   BooleanSetting predict = this.registerBoolean("Predict", true);
   IntegerSetting tickPredict = this.registerInteger("TickPredict", 8, 0, 30, () -> this.predict.getValue());
   BooleanSetting calculateYPredict = this.registerBoolean("CalculateYPredict", true, () -> this.predict.getValue());
   IntegerSetting startDecrease = this.registerInteger("StartDecrease", 39, 0, 200, () -> this.predict.getValue() && this.calculateYPredict.getValue());
   IntegerSetting exponentStartDecrease = this.registerInteger("ExponentStart", 2, 1, 5, () -> this.predict.getValue() && this.calculateYPredict.getValue());
   IntegerSetting decreaseY = this.registerInteger("DecreaseY", 2, 1, 5, () -> this.predict.getValue() && this.calculateYPredict.getValue());
   IntegerSetting exponentDecreaseY = this.registerInteger("ExponentDecreaseY", 1, 1, 3, () -> this.predict.getValue() && this.calculateYPredict.getValue());
   BooleanSetting splitXZ = this.registerBoolean("SplitXZ", true, () -> this.predict.getValue());
   BooleanSetting manualOutHole = this.registerBoolean("ManualOutHole", false, () -> this.predict.getValue());
   BooleanSetting aboveHoleManual = this.registerBoolean("AboveHoleManual", false, () -> this.predict.getValue() && this.manualOutHole.getValue());
   BooleanSetting stairPredict = this.registerBoolean("StairPredict", false, () -> this.predict.getValue());
   IntegerSetting nStair = this.registerInteger("NStair", 2, 1, 4, () -> this.predict.getValue() && this.stairPredict.getValue());
   DoubleSetting speedActivationStair = this.registerDouble(
      "SpeedActivationStair", 0.11, 0.0, 1.0, () -> this.predict.getValue() && this.stairPredict.getValue()
   );
   Timing timer = new Timing();
   EntityPlayer target;

   @Override
   public void onTick() {
      int slot = this.getSlot();
      if (slot != -1) {
         EntityPlayer origin = this.target = PlayerUtil.getNearestPlayer(this.enemyRange.getValue());
         if (this.target != null) {
            if (AutoEz.INSTANCE.isEnabled()) {
               AutoEz.INSTANCE.addTargetedPlayer(this.target.getName());
            }

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
            if (this.predict.getValue()) {
               this.target = PredictUtil.predictPlayer(this.target, settings);
            }

            BlockPos blockPos = null;
            double dmg = 0.0;

            for (BlockPos pos : EntityUtil.getSphere(PlayerUtil.getEyesPos(), this.range.getValue(), this.range.getValue(), false, false, 0)) {
               if (BurrowUtil.getFirstFacing(pos) != null) {
                  double damage = DamageUtil.calculateDamage(
                     origin,
                     this.target.getPositionVector(),
                     this.target.boundingBox,
                     pos.x + 0.5,
                     pos.y,
                     pos.z + 0.5,
                     3.0F,
                     "Default"
                  );
                  if (!(damage < this.minDamage.getValue()) && dmg < damage) {
                     blockPos = pos;
                     dmg = damage;
                  }
               }
            }

            if (blockPos != null) {
               if (this.timer.passedMs(this.delay.getValue().intValue())) {
                  this.timer.reset();
                  BlockPos finalBlockPos = blockPos;
                  InventoryUtil.run(
                     slot,
                     this.packetSwitch.getValue(),
                     () -> BurrowUtil.placeBlock(
                        finalBlockPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     )
                  );
               }
            }
         }
      }
   }

   public int getSlot() {
      int newSlot = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() == Items.SPAWN_EGG) {
            newSlot = i;
            break;
         }
      }

      return newSlot;
   }
}
