package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@Module.Declaration(name = "PotionEffects", category = Category.HUD, drawn = false)
@HUDModule.Declaration(posX = 0, posZ = 300)
public class PotionEffects extends HUDModule {
   BooleanSetting sortUp = this.registerBoolean("Sort Up", false);
   BooleanSetting sortRight = this.registerBoolean("Sort Right", false);
   private final PotionEffects.PotionList list = new PotionEffects.PotionList();

   @Override
   public void populate(ITheme theme) {
      this.component = new ListComponent(new Labeled(this.getName(), null, () -> true), this.position, this.getName(), this.list, 9, 1);
   }

   Color getColour(PotionEffect potion) {
      int colour = potion.getPotion().getLiquidColor();
      float r = (colour >> 16 & 0xFF) / 255.0F;
      float g = (colour >> 8 & 0xFF) / 255.0F;
      float b = (colour & 0xFF) / 255.0F;
      return new Color(r, g, b);
   }

   private class PotionList implements HUDList {
      private PotionList() {
      }

      @Override
      public int getSize() {
         return PotionEffects.mc.player.getActivePotionEffects().size();
      }

      @Override
      public String getItem(int index) {
         PotionEffect effect = (PotionEffect)PotionEffects.mc.player.getActivePotionEffects().toArray()[index];
         String name = I18n.format(effect.getPotion().getName(), new Object[0]);
         int amplifier = effect.getAmplifier() + 1;
         return name + " " + amplifier + ChatFormatting.GRAY + " " + Potion.getPotionDurationString(effect, 1.0F);
      }

      @Override
      public Color getItemColor(int i) {
         return PotionEffects.mc.player.getActivePotionEffects().toArray().length != 0
            ? PotionEffects.this.getColour((PotionEffect)PotionEffects.mc.player.getActivePotionEffects().toArray()[i])
            : null;
      }

      @Override
      public boolean sortUp() {
         return PotionEffects.this.sortUp.getValue();
      }

      @Override
      public boolean sortRight() {
         return PotionEffects.this.sortRight.getValue();
      }
   }
}
