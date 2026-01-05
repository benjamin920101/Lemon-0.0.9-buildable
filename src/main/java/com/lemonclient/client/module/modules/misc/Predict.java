package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.PredictUtil;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Module.Declaration(name = "Predict", category = Category.Misc)
public class Predict extends Module {
   IntegerSetting range = this.registerInteger("Range", 10, 0, 100);
   IntegerSetting tickPredict = this.registerInteger("Tick Predict", 8, 0, 30);
   BooleanSetting calculateYPredict = this.registerBoolean("Calculate Y Predict", true);
   IntegerSetting startDecrease = this.registerInteger("Start Decrease", 39, 0, 200, () -> this.calculateYPredict.getValue());
   IntegerSetting exponentStartDecrease = this.registerInteger("Exponent Start", 2, 1, 5, () -> this.calculateYPredict.getValue());
   IntegerSetting decreaseY = this.registerInteger("Decrease Y", 2, 1, 5, () -> this.calculateYPredict.getValue());
   IntegerSetting exponentDecreaseY = this.registerInteger("Exponent Decrease Y", 1, 1, 3, () -> this.calculateYPredict.getValue());
   BooleanSetting splitXZ = this.registerBoolean("Split XZ", true);
   BooleanSetting hideSelf = this.registerBoolean("Hide Self", false);
   IntegerSetting width = this.registerInteger("Line Width", 2, 1, 5);
   BooleanSetting justOnce = this.registerBoolean("Just Once", false);
   BooleanSetting manualOutHole = this.registerBoolean("Manual Out Hole", false);
   BooleanSetting aboveHoleManual = this.registerBoolean("Above Hole Manual", false, () -> this.manualOutHole.getValue());
   BooleanSetting stairPredict = this.registerBoolean("Stair Predict", false);
   IntegerSetting nStair = this.registerInteger("N Stair", 2, 1, 4, () -> this.stairPredict.getValue());
   DoubleSetting speedActivationStair = this.registerDouble("Speed Activation Stair", 0.3, 0.0, 1.0, () -> this.stairPredict.getValue());
   ColorSetting mainColor = this.registerColor("Color");

   @Override
   public void onWorldRender(RenderEvent event) {
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
      mc.world
         .playerEntities
         .stream()
         .filter(entity -> !this.hideSelf.getValue() || entity != mc.player)
         .filter(this::rangeEntityCheck)
         .forEach(entity -> {
            EntityPlayer clonedPlayer = PredictUtil.predictPlayer(entity, settings);
            RenderUtil.drawBoundingBox(clonedPlayer.getEntityBoundingBox(), this.width.getValue().intValue(), this.mainColor.getColor());
         });
      if (this.justOnce.getValue()) {
         this.disable();
      }
   }

   private boolean rangeEntityCheck(Entity entity) {
      return entity.getDistance(mc.player) <= this.range.getValue().intValue();
   }
}
