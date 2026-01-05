package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.CrystalUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Comparator;
import net.minecraft.entity.item.EntityEnderCrystal;

@Module.Declaration(name = "CrystalHit", category = Category.Combat)
public class CrystalHit extends Module {
   IntegerSetting range = this.registerInteger("Range", 4, 0, 10);
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 40);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting packetBreak = this.registerBoolean("Packet Break", false);
   BooleanSetting antiWeakness = this.registerBoolean("Anti Weakness", false);
   BooleanSetting weakBypass = this.registerBoolean("Bypass Switch", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> !this.weakBypass.getValue());
   BooleanSetting silent = this.registerBoolean("Silent Switch", false, () -> !this.weakBypass.getValue());
   int delayTime = 0;

   @Override
   public void onUpdate() {
      EntityEnderCrystal crystal = mc.world
         .loadedEntityList
         .stream()
         .filter(entity -> entity instanceof EntityEnderCrystal)
         .map(entity -> (EntityEnderCrystal)entity)
         .min(Comparator.comparing(c -> mc.player.getDistance(c)))
         .orElse(null);
      if (crystal != null && mc.player.getDistance(crystal) <= this.range.getValue().intValue() && this.delayTime++ >= this.delay.getValue()) {
         CrystalUtil.breakCrystal(
            crystal,
            this.packetBreak.getValue(),
            this.swing.getValue(),
            this.packetSwitch.getValue(),
            this.silent.getValue(),
            this.antiWeakness.getValue(),
            this.weakBypass.getValue()
         );
      }
   }
}
