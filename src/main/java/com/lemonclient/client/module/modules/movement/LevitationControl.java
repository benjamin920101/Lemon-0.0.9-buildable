package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Objects;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

@Module.Declaration(name = "LevitationControl", category = Category.Movement)
public class LevitationControl extends Module {
   DoubleSetting upAmplifier = this.registerDouble("Amplifier Up", 1.0, 1.0, 3.0);
   DoubleSetting downAmplifier = this.registerDouble("Amplifier Down", 1.0, 1.0, 3.0);

   @Override
   public void onUpdate() {
      if (mc.player.isPotionActive(MobEffects.LEVITATION)) {
         int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(25)))).getAmplifier();
         if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = (0.05 * (amplifier + 1) - mc.player.motionY) * 0.2 * this.upAmplifier.getValue();
         } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY = -((0.05 * (amplifier + 1) - mc.player.motionY) * 0.2 * this.downAmplifier.getValue());
         } else {
            mc.player.motionY = 0.0;
         }
      }
   }
}
