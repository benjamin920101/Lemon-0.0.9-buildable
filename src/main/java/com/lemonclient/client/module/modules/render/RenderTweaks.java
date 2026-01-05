package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.client.renderer.ItemRenderer;

@Module.Declaration(name = "RenderTweaks", category = Category.Render)
public class RenderTweaks extends Module {
   public BooleanSetting viewClip = this.registerBoolean("View Clip", false);
   public BooleanSetting noAnimation = this.registerBoolean("No Animation", false);
   public BooleanSetting noEat = this.registerBoolean("No Eat", false);
   BooleanSetting lowOffhand = this.registerBoolean("Low Offhand", false);
   DoubleSetting lowOffhandSlider = this.registerDouble("Offhand Height", 1.0, 0.1, 1.0, () -> this.lowOffhand.getValue());
   BooleanSetting fovChanger = this.registerBoolean("FOV", false);
   IntegerSetting fovChangerSlider = this.registerInteger("FOV Slider", 90, 70, 200, () -> this.fovChanger.getValue());
   ItemRenderer itemRenderer = mc.entityRenderer.itemRenderer;
   private float oldFOV;

   @Override
   public void onUpdate() {
      if (this.lowOffhand.getValue()) {
         this.itemRenderer.equippedProgressOffHand = this.lowOffhandSlider.getValue().floatValue();
      }

      if (this.fovChanger.getValue()) {
         mc.gameSettings.fovSetting = this.fovChangerSlider.getValue().intValue();
      }

      if (!this.fovChanger.getValue()) {
         mc.gameSettings.fovSetting = this.oldFOV;
      }
   }

   @Override
   public void onEnable() {
      this.oldFOV = mc.gameSettings.fovSetting;
   }

   @Override
   public void onDisable() {
      mc.gameSettings.fovSetting = this.oldFOV;
   }
}
