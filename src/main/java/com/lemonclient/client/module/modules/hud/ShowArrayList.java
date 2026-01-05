package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.chat.AnimationUtil;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Comparator;

@Module.Declaration(name = "ShowArrayList", category = Category.HUD, drawn = false)
public final class ShowArrayList extends Module {
   private int count;
   IntegerSetting width = this.registerInteger("X", 0, 0, 1920);
   IntegerSetting height = this.registerInteger("Y", 0, 0, 1080);
   BooleanSetting sortUp = this.registerBoolean("Sort Up", false);
   BooleanSetting sortRight = this.registerBoolean("Sort Right", false);
   DoubleSetting animationSpeed = this.registerDouble("Animation Speed", 3.5, 0.0, 5.0);
   ColorSetting color = this.registerColor("Color", new GSColor(210, 100, 165));
   public static ShowArrayList INSTANCE;

   public ShowArrayList() {
      INSTANCE = this;
   }

   private static String getArrayList(Module module) {
      return module.getName() + ChatFormatting.GRAY + module.getHudInfo();
   }

   @Override
   public void onRender() {
      if (this.isEnabled() && mc.world != null && mc.player != null) {
         this.count = 0;
         ModuleManager.getModules()
            .stream()
            .filter(ShowArrayList::render)
            .sorted(Comparator.comparing(ShowArrayList::getWidth))
            .forEach(ShowArrayList::drawRect);
      }
   }

   private static boolean render(Module it) {
      return it.isDrawn();
   }

   private static Integer getWidth(Module it) {
      return FontUtil.getStringWidth(ModuleManager.getModule(ColorMain.class).customFont.getValue(), getArrayList(it)) * -1;
   }

   private static void drawRect(Module module) {
      boolean customFont = ModuleManager.getModule(ColorMain.class).customFont.getValue();
      if (module.isDrawn()) {
         String modText = getArrayList(module);
         float modWidth = FontUtil.getStringWidth(customFont, modText);
         float remainingAnimation = module.remainingAnimation;
         float smoothSpeed = (float)(0.01F + INSTANCE.animationSpeed.getValue() / 30.0);
         float minSpeed = 0.1F;
         if (module.isEnabled()) {
            if (module.remainingAnimation < modWidth) {
               float end = modWidth + 1.0F;
               module.remainingAnimation = AnimationUtil.moveTowards(remainingAnimation, end, smoothSpeed, minSpeed, false);
            } else if (module.remainingAnimation > modWidth) {
               float end2 = modWidth - 1.0F;
               module.remainingAnimation = AnimationUtil.moveTowards(remainingAnimation, end2, smoothSpeed, minSpeed, false);
            }
         } else {
            if (!(module.remainingAnimation > 0.0F)) {
               return;
            }

            float end3 = -modWidth;
            module.remainingAnimation = AnimationUtil.moveTowards(remainingAnimation, end3, smoothSpeed, minSpeed, false);
         }

         if (INSTANCE.sortRight.getValue()) {
            FontUtil.drawStringWithShadow(
               customFont,
               modText,
               (int)(INSTANCE.width.getValue().intValue() - module.remainingAnimation),
               INSTANCE.height.getValue() + 10 * INSTANCE.count * (INSTANCE.sortUp.getValue() ? 1 : -1),
               INSTANCE.color.getValue()
            );
         } else {
            FontUtil.drawStringWithShadow(
               customFont,
               modText,
               (int)(INSTANCE.width.getValue() - 2 - modWidth + module.remainingAnimation),
               INSTANCE.height.getValue() + 10 * INSTANCE.count * (INSTANCE.sortUp.getValue() ? 1 : -1),
               INSTANCE.color.getValue()
            );
         }

         INSTANCE.count++;
      }
   }
}
