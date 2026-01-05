package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.Pair;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

@Module.Declaration(name = "SwordSwitch", category = Category.Dev)
public class SwordSwitch extends Module {
   BooleanSetting disable = this.registerBoolean("Disable", true);

   @Override
   public void onUpdate() {
      new Pair<>(0.0F, -1);
      Pair<Float, Integer> newSlot = this.findSwordSlot();
      if ((Integer)newSlot.getValue() != -1) {
         mc.player.inventory.currentItem = (Integer)newSlot.getValue();
         if (this.disable.getValue()) {
            this.disable();
         }
      } else {
         MessageBus.sendClientPrefixMessage("Cant find sword", Notification.Type.ERROR);
         this.disable();
      }
   }

   private Pair<Float, Integer> findSwordSlot() {
      List<Integer> items = InventoryUtil.findAllItemSlots(ItemSword.class);
      List<ItemStack> inventory = mc.player.inventory.mainInventory;
      float bestModifier = 0.0F;
      int correspondingSlot = -1;

      for (Integer integer : items) {
         if (integer <= 8) {
            ItemStack stack = inventory.get(integer);
            float modifier = (EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED) + 1.0F)
               * ((ItemSword)stack.getItem()).getAttackDamage();
            if (modifier > bestModifier) {
               bestModifier = modifier;
               correspondingSlot = integer;
            }
         }
      }

      return new Pair<>(bestModifier, correspondingSlot);
   }
}
