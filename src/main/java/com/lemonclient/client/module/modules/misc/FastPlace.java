package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.init.Items;

@Module.Declaration(name = "FastPlace", category = Category.Misc)
public class FastPlace extends Module {
   BooleanSetting exp = this.registerBoolean("Exp", false);
   BooleanSetting crystals = this.registerBoolean("Crystals", false);
   BooleanSetting offhandCrystal = this.registerBoolean("Offhand Crystal", false);
   BooleanSetting everything = this.registerBoolean("Everything", false);

   @Override
   public void onUpdate() {
      if (this.exp.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE
         || mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) {
         mc.rightClickDelayTimer = 0;
      }

      if (this.crystals.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
         mc.rightClickDelayTimer = 0;
      }

      if (this.offhandCrystal.getValue() && mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
         mc.rightClickDelayTimer = 0;
      }

      if (this.everything.getValue()) {
         mc.rightClickDelayTimer = 0;
      }

      mc.playerController.blockHitDelay = 0;
   }
}
