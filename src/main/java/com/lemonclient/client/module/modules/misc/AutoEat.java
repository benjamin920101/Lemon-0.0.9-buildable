package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

@Module.Declaration(name = "AutoEat", category = Category.Misc)
public class AutoEat extends Module {
   IntegerSetting health = this.registerInteger("Health", 10, 1, 36);
   BooleanSetting equal = this.registerBoolean("Equal", false);
   boolean eating = false;

   @Override
   public void onDisable() {
      this.stopEating();
   }

   @Override
   public void onTick() {
      if (EntityUtil.isDead(mc.player)) {
         if (this.eating) {
            this.stopEating();
         }
      } else {
         if (this.shouldEat()) {
            EnumHand hand = null;
            if (this.isValid(mc.player.getHeldItemMainhand())) {
               hand = EnumHand.MAIN_HAND;
            }

            if (this.isValid(mc.player.getHeldItemOffhand())) {
               hand = EnumHand.OFF_HAND;
            }

            if (hand != null) {
               this.eat(hand);
            } else {
               int slot = this.findHotbarFood();
               if (slot != -1) {
                  mc.player.inventory.currentItem = slot;
               }
            }
         } else if (this.eating) {
            this.stopEating();
         }
      }
   }

   private int findHotbarFood() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && this.isValid(stack)) {
            return i;
         }
      }

      return -1;
   }

   private boolean shouldEat() {
      return this.equal.getValue()
         ? mc.player.getHealth() + mc.player.getAbsorptionAmount() <= this.health.getValue().intValue()
         : mc.player.getHealth() + mc.player.getAbsorptionAmount() < this.health.getValue().intValue();
   }

   private void eat(EnumHand hand) {
      if (!this.eating || !mc.player.isHandActive() || mc.player.getActiveHand() != hand) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
         mc.playerController.processRightClick(mc.player, mc.world, hand);
      }

      this.eating = true;
   }

   private void stopEating() {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
      this.eating = false;
   }

   private boolean isValid(ItemStack itemStack) {
      Item item = itemStack.item;
      return item instanceof ItemFood
         && item != Items.CHORUS_FRUIT
         && !this.isBadFood(itemStack, (ItemFood)item)
         && mc.player.canEat(item == Items.GOLDEN_APPLE);
   }

   private boolean isBadFood(ItemStack itemStack, ItemFood item) {
      return item == Items.ROTTEN_FLESH
         || item == Items.SPIDER_EYE
         || item == Items.POISONOUS_POTATO
         || item == Items.FISH && (itemStack.getMetadata() == 3 || itemStack.getMetadata() == 2);
   }
}
