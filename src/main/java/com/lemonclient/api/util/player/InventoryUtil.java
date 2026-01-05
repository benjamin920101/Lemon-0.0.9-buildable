package com.lemonclient.api.util.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.potion.PotionUtils;

public class InventoryUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final ItemStack ILLEGAL_STACK = new ItemStack(Item.getItemFromBlock(Blocks.BEDROCK));

   public static void run(int slot, boolean packetSwitch, Runnable runnable) {
      int oldslot = mc.player.inventory.currentItem;
      if (slot >= 0 && slot != oldslot) {
         if (packetSwitch) {
            packetSwitch(slot);
         } else {
            switchSlot(slot);
         }

         runnable.run();
         if (packetSwitch) {
            packetSwitch(oldslot);
         } else {
            switchSlot(oldslot);
         }

         mc.player.openContainer.detectAndSendChanges();
      } else {
         runnable.run();
      }
   }

   public static void switchSlot(int slot) {
      mc.player.inventory.currentItem = slot;
      mc.playerController.updateController();
   }

   public static void packetSwitch(int slot) {
      mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
   }

   public static void switchToBypass(int slot) {
      Locks.acquire(Locks.WINDOW_CLICK_LOCK, () -> {
         if (mc.player.inventory.currentItem != slot && slot > -1 && slot < 9) {
            int lastSlot = mc.player.inventory.currentItem;
            int targetSlot = hotbarToInventory(slot);
            int currentSlot = hotbarToInventory(lastSlot);
            mc.playerController.windowClick(0, targetSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, currentSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, targetSlot, 0, ClickType.PICKUP, mc.player);
         }
      });
   }

   public static void switchToBypassAlt(int slot) {
      Locks.acquire(
         Locks.WINDOW_CLICK_LOCK,
         () -> {
            if (mc.player.inventory.currentItem != slot && slot > -1 && slot < 9) {
               Locks.acquire(
                  Locks.WINDOW_CLICK_LOCK,
                  () -> mc.playerController.windowClick(0, slot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player)
               );
            }
         }
      );
   }

   public static void bypassSwitch(int slot) {
      if (slot >= 0) {
         mc.playerController.pickItem(slot);
      }
   }

   public static int hotbarToInventory(int slot) {
      if (slot == -2) {
         return 45;
      } else {
         return slot > -1 && slot < 9 ? 36 + slot : slot;
      }
   }

   public static void swap(int InvSlot, int newSlot) {
      mc.playerController.windowClick(0, InvSlot, 0, ClickType.PICKUP, mc.player);
      mc.playerController.windowClick(0, newSlot, 0, ClickType.PICKUP, mc.player);
      mc.playerController.windowClick(0, InvSlot, 0, ClickType.PICKUP, mc.player);
      mc.playerController.updateController();
   }

   public static int getHotBarPressure(String mode) {
      for (int i = 0; i < 9; i++) {
         if (mode.equals("Pressure")) {
            if (isPressure(mc.player.inventory.getStackInSlot(i))) {
               return i;
            }
         } else if (isString(mc.player.inventory.getStackInSlot(i))) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isString(ItemStack stack) {
      return stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock) ? stack.getItem() == Items.STRING : false;
   }

   public static boolean isPressure(ItemStack stack) {
      return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock
         ? ((ItemBlock)stack.getItem()).getBlock() instanceof BlockPressurePlate
         : false;
   }

   public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
      HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<>();

      for (int current = 9; current <= 44; current++) {
         fullInventorySlots.put(current, (ItemStack)mc.player.inventoryContainer.getInventory().get(current));
      }

      return fullInventorySlots;
   }

   public static boolean isBlock(Item item, Class clazz) {
      if (item instanceof ItemBlock) {
         Block block = ((ItemBlock)item).getBlock();
         return clazz.isInstance(block);
      } else {
         return false;
      }
   }

   public static void click(int windowIdIn, int slotIdIn, int usedButtonIn, ClickType modeIn, ItemStack clickedItemIn, short actionNumberIn) {
      mc.player.connection.sendPacket(new CPacketClickWindow(windowIdIn, slotIdIn, usedButtonIn, modeIn, clickedItemIn, actionNumberIn));
   }

   public static int findCrystalBlockSlot() {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block.getBlockState().getBlock().blockHardness > 6.0F) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   public static void illegalSync() {
      if (mc.player != null) {
         click(0, 0, 0, ClickType.PICKUP, ILLEGAL_STACK, (short)0);
      }
   }

   public static int findObsidianSlot(boolean offHandActived, boolean activeBefore) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block instanceof BlockObsidian) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   public static int findEChestSlot(boolean offHandActived, boolean activeBefore) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block instanceof BlockEnderChest) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   public static int findSkullSlot() {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemSkull) {
            return i;
         }
      }

      return slot;
   }

   public static int findTotemSlot(int lower, int upper) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = lower; i <= upper; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && stack.getItem() == Items.TOTEM_OF_UNDYING) {
            slot = i;
            break;
         }
      }

      return slot;
   }

   public static int findFirstItemSlot(Class<? extends Item> itemToFind, int lower, int upper) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = lower; i <= upper; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && itemToFind.isInstance(stack.getItem()) && itemToFind.isInstance(stack.getItem())) {
            slot = i;
            break;
         }
      }

      return slot;
   }

   public static int findStackInventory(Item input, boolean withHotbar) {
      for (int i = withHotbar ? 0 : 9; i < 36; i++) {
         Item item = mc.player.inventory.getStackInSlot(i).getItem();
         if (Item.getIdFromItem(input) == Item.getIdFromItem(item)) {
            return i + (i < 9 ? 36 : 0);
         }
      }

      return -1;
   }

   public static int getItemSlot(Item input) {
      if (mc.player == null) {
         return 0;
      } else {
         for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); i++) {
            if (i != 0 && i != 5 && i != 6 && i != 7 && i != 8) {
               ItemStack s = (ItemStack)mc.player.inventoryContainer.getInventory().get(i);
               if (!s.isEmpty() && s.getItem() == input) {
                  return i;
               }
            }
         }

         return -1;
      }
   }

   public static int getItemInHotbar(Item p_Item) {
      for (int l_I = 0; l_I < 9; l_I++) {
         ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
         if (l_Stack != ItemStack.EMPTY && l_Stack.getItem() == p_Item) {
            return l_I;
         }
      }

      return -1;
   }

   public static int getPotion(String potion) {
      for (int l_I = 0; l_I < 36; l_I++) {
         ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
         if (l_Stack != ItemStack.EMPTY
            && l_Stack.getItem() == Items.SPLASH_POTION
            && Objects.requireNonNull(PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(l_I)).getRegistryName())
               .getPath()
               .contains(potion)) {
            return l_I;
         }
      }

      return -1;
   }

   public static int findFirstBlockSlot(Class<? extends Block> blockToFind, int lower, int upper) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = lower; i <= upper; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY
            && stack.getItem() instanceof ItemBlock
            && blockToFind.isInstance(((ItemBlock)stack.getItem()).getBlock())) {
            slot = i;
            break;
         }
      }

      return slot;
   }

   public static List<Integer> findAllItemSlots(Class<? extends Item> itemToFind) {
      List<Integer> slots = new ArrayList<>();
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY && itemToFind.isInstance(stack.getItem())) {
            slots.add(i);
         }
      }

      return slots;
   }

   public static List<Integer> findAllBlockSlots(Class<? extends Block> blockToFind) {
      List<Integer> slots = new ArrayList<>();
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mainInventory.get(i);
         if (stack != ItemStack.EMPTY
            && stack.getItem() instanceof ItemBlock
            && blockToFind.isInstance(((ItemBlock)stack.getItem()).getBlock())) {
            slots.add(i);
         }
      }

      return slots;
   }

   public static int findToolForBlockState(IBlockState iBlockState, int lower, int upper) {
      int slot = -1;
      List<ItemStack> mainInventory = mc.player.inventory.mainInventory;
      double foundMaxSpeed = 0.0;

      for (int i = lower; i <= upper; i++) {
         ItemStack itemStack = mainInventory.get(i);
         if (itemStack != ItemStack.EMPTY) {
            float breakSpeed = itemStack.getDestroySpeed(iBlockState);
            int efficiencySpeed = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack);
            if (breakSpeed > 1.0F) {
               breakSpeed = (float)(breakSpeed + (efficiencySpeed > 0 ? Math.pow(efficiencySpeed, 2.0) + 1.0 : 0.0));
               if (breakSpeed > foundMaxSpeed) {
                  foundMaxSpeed = breakSpeed;
                  slot = i;
               }
            }
         }
      }

      return slot;
   }

   public static int getEmptyCounts() {
      if (mc.player == null) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i <= 35; i++) {
            ItemStack stack = (ItemStack)mc.player.inventory.mainInventory.get(i);
            if (stack == ItemStack.EMPTY || stack.getItem() == Items.AIR) {
               count++;
            }
         }

         return count;
      }
   }
}
