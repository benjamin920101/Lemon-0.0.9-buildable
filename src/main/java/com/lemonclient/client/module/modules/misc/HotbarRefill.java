package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Pair;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "HotbarRefill", category = Category.Misc)
public class HotbarRefill extends Module {
   IntegerSetting threshold = this.registerInteger("Threshold", 32, 1, 63);
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 2, 1, 10);
   private int delayStep = 0;

   @Override
   public void onUpdate() {
      if (mc.player != null) {
         if (!(mc.currentScreen instanceof GuiContainer)) {
            if (this.delayStep < this.tickDelay.getValue()) {
               this.delayStep++;
            } else {
               this.delayStep = 0;
               Pair<Integer, Integer> slots = this.findReplenishableHotbarSlot();
               if (slots != null) {
                  int inventorySlot = slots.getKey();
                  int hotbarSlot = slots.getValue();
                  mc.playerController.windowClick(0, inventorySlot, 0, ClickType.QUICK_MOVE, mc.player);
               }
            }
         }
      }
   }

   private Pair<Integer, Integer> findReplenishableHotbarSlot() {
      List<ItemStack> inventory = mc.player.inventory.mainInventory;

      for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
         ItemStack stack = inventory.get(hotbarSlot);
         if (stack.isStackable()
            && !stack.isEmpty
            && stack.getItem() != Items.AIR
            && stack.stackSize < stack.getMaxStackSize()
            && stack.stackSize <= this.threshold.getValue()) {
            int inventorySlot = this.findCompatibleInventorySlot(stack);
            if (inventorySlot != -1) {
               return new Pair<>(inventorySlot, hotbarSlot);
            }
         }
      }

      return null;
   }

   private int findCompatibleInventorySlot(ItemStack hotbarStack) {
      Item item = hotbarStack.getItem();
      List<Integer> potentialSlots;
      if (item instanceof ItemBlock) {
         potentialSlots = InventoryUtil.findAllBlockSlots((Class<? extends Block>)((ItemBlock)item).getBlock().getClass());
      } else {
         potentialSlots = InventoryUtil.findAllItemSlots((Class<? extends Item>)item.getClass());
      }

      for (int slot : potentialSlots.stream()
         .filter(integer -> integer > 8 && integer < 36)
         .sorted(Comparator.comparingInt(interger -> -interger))
         .collect(Collectors.toList())) {
         if (this.isCompatibleStacks(hotbarStack, mc.player.inventory.getStackInSlot(slot))) {
            return slot;
         }
      }

      return -1;
   }

   private boolean isCompatibleStacks(ItemStack stack1, ItemStack stack2) {
      if (!stack1.getItem().equals(stack2.getItem())) {
         return false;
      } else {
         if (stack1.getItem() instanceof ItemBlock && stack2.getItem() instanceof ItemBlock) {
            Block block1 = ((ItemBlock)stack1.getItem()).getBlock();
            Block block2 = ((ItemBlock)stack2.getItem()).getBlock();
            if (!block1.material.equals(block2.material)) {
               return false;
            }
         }

         return !stack1.getDisplayName().equals(stack2.getDisplayName()) ? false : stack1.getItemDamage() == stack2.getItemDamage();
      }
   }
}
