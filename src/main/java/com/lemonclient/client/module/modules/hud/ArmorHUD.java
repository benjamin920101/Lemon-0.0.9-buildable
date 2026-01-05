package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "ArmorHUD", category = Category.HUD, drawn = false)
public class ArmorHUD extends Module {
   @Override
   public void onRender() {
      GlStateManager.pushMatrix();
      GlStateManager.enableTexture2D();
      ScaledResolution resolution = new ScaledResolution(mc);
      int i = resolution.getScaledWidth() / 2;
      int iteration = 0;
      int y = resolution.getScaledHeight() - 55 - (mc.player.isInWater() ? 10 : 0);

      for (ItemStack is : mc.player.inventory.armorInventory) {
         iteration++;
         if (!is.isEmpty()) {
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 200.0F;
            mc.getRenderItem().renderItemAndEffectIntoGUI(is, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s), y + 9, new GSColor(255, 255, 255).getRGB());
            float green = ((float)is.getMaxDamage() - is.getItemDamage()) / is.getMaxDamage();
            float red = 1.0F - green;
            int dmg = 100 - (int)(red * 100.0F);
            if (green > 1.0F) {
               green = 1.0F;
            } else if (green < 0.0F) {
               green = 0.0F;
            }

            if (red > 1.0F) {
               red = 1.0F;
            }

            if (dmg < 0) {
               dmg = 0;
            }

            FontUtil.drawStringWithShadow(
               ModuleManager.getModule(ColorMain.class).customFont.getValue(),
               dmg + "",
               x + 8 - mc.fontRenderer.getStringWidth(dmg + "") / 2,
               y - 11,
               new GSColor((int)(red * 255.0F), (int)(green * 255.0F), 0)
            );
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.popMatrix();
   }
}
