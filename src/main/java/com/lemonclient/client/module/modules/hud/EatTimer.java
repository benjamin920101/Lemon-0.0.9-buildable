package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.event.events.Render2DEvent;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import net.minecraft.item.ItemFood;

@Module.Declaration(name = "EatTimer", category = Category.HUD, drawn = false)
public class EatTimer extends Module {
   IntegerSetting timer = this.registerInteger("Timer", 32, 0, 100);
   int tick = 100;
   boolean holding = false;

   @Override
   public void onEnable() {
      this.holding = false;
      this.tick = 100;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         this.tick++;
         this.holding = mc.player.getHeldItemMainhand().getItem() instanceof ItemFood
            || mc.player.getHeldItemOffhand().getItem() instanceof ItemFood;
         if (mc.player.isHandActive() && this.holding && this.tick > this.timer.getValue()) {
            this.tick = 0;
         }
      } else {
         this.tick = 100;
      }
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (mc.world != null && mc.player != null) {
         if (this.holding) {
            if (this.tick <= this.timer.getValue()) {
               double percent = (double)this.tick / this.timer.getValue().intValue();
               String text = String.format("%.1f", percent * 100.0) + "%";
               int divider = mc.gameSettings.guiScale;
               if (divider == 0) {
                  divider = 3;
               }

               boolean font = ModuleManager.getModule(ColorMain.class).customFont.getValue();
               FontUtil.drawStringWithShadow(
                  font,
                  text,
                  mc.displayWidth / divider / 2 - FontUtil.getStringWidth(font, text) / 2,
                  mc.displayHeight / divider / 2 + 16,
                  new GSColor(255, 255, 255)
               );
            }
         } else {
            this.tick = 100;
         }
      } else {
         this.tick = 100;
      }
   }
}
