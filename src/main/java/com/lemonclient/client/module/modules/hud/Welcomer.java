package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.StringSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import java.awt.Color;

@Module.Declaration(name = "Welcomer", category = Category.HUD, drawn = false)
@HUDModule.Declaration(posX = 450, posZ = 0)
public class Welcomer extends HUDModule {
   StringSetting prefix = this.registerString("Prefix", "Hi ");
   StringSetting suffix = this.registerString("Suffix", " :^)");
   ColorSetting color = this.registerColor("Color", new GSColor(255, 0, 0, 255));

   @Override
   public void populate(ITheme theme) {
      this.component = new ListComponent(new Labeled(this.getName(), null, () -> true), this.position, this.getName(), new Welcomer.WelcomerList(), 9, 1);
   }

   private class WelcomerList implements HUDList {
      private WelcomerList() {
      }

      @Override
      public int getSize() {
         return 1;
      }

      @Override
      public String getItem(int index) {
         return Welcomer.this.prefix.getText() + Welcomer.mc.player.getName() + Welcomer.this.suffix.getText();
      }

      @Override
      public Color getItemColor(int index) {
         return Welcomer.this.color.getValue();
      }

      @Override
      public boolean sortUp() {
         return false;
      }

      @Override
      public boolean sortRight() {
         return false;
      }
   }
}
