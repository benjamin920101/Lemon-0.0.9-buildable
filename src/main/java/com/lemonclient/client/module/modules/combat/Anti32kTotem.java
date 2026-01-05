package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "32kTotem", category = Category.Combat)
public class Anti32kTotem extends Module {
   IntegerSetting slot = this.registerInteger("Slot", 1, 1, 9);

   @Override
   public void fast() {
      if ((!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory)
         && mc.player.inventory.getStackInSlot(this.slot.getValue() - 1).getItem() != Items.TOTEM_OF_UNDYING) {
         for (int i = 9; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
               mc.playerController.windowClick(0, i, this.slot.getValue() - 1, ClickType.SWAP, mc.player);
               break;
            }
         }
      }
   }

   @Override
   public String getHudInfo() {
      int totems = mc.player
         .inventory
         .mainInventory
         .stream()
         .filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)
         .mapToInt(ItemStack::getCount)
         .sum();
      if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
         totems++;
      }

      return "[" + ChatFormatting.WHITE + "Totem " + totems + ChatFormatting.GRAY + "]";
   }
}
