package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.command.commands.AutoGearCommand;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "AutoGear", category = Category.Misc)
public class AutoGear extends Module {
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 0, 0, 20);
   IntegerSetting switchForTick = this.registerInteger("Switch Per Tick", 1, 1, 100);
   BooleanSetting enderChest = this.registerBoolean("EnderChest", false);
   BooleanSetting confirmSort = this.registerBoolean("Confirm Sort", true);
   BooleanSetting invasive = this.registerBoolean("Invasive", false);
   BooleanSetting closeAfter = this.registerBoolean("Close After", false);
   BooleanSetting infoMsgs = this.registerBoolean("Info Msgs", true);
   BooleanSetting debugMode = this.registerBoolean("Debug Mode", false);
   private HashMap<Integer, String> planInventory = new HashMap<>();
   private final HashMap<Integer, String> containerInv = new HashMap<>();
   private ArrayList<Integer> sortItems = new ArrayList<>();
   private int delayTimeTicks;
   private int stepNow;
   private boolean openedBefore;
   private boolean finishSort;
   private boolean doneBefore;

   @Override
   public void onEnable() {
      String curConfigName = AutoGearCommand.getCurrentSet();
      if (curConfigName.equals("")) {
         this.disable();
      } else {
         if (this.infoMsgs.getValue()) {
            MessageBus.printDebug("Config " + curConfigName + " activated", false);
         }

         String inventoryConfig = AutoGearCommand.getInventoryKit(curConfigName);
         if (inventoryConfig.equals("")) {
            this.disable();
         } else {
            String[] inventoryDivided = inventoryConfig.split(" ");
            this.planInventory = new HashMap<>();
            HashMap<String, Integer> nItems = new HashMap<>();

            for (int i = 0; i < inventoryDivided.length; i++) {
               if (!inventoryDivided[i].contains("air")) {
                  this.planInventory.put(i, inventoryDivided[i]);
                  if (nItems.containsKey(inventoryDivided[i])) {
                     nItems.put(inventoryDivided[i], nItems.get(inventoryDivided[i]) + 1);
                  } else {
                     nItems.put(inventoryDivided[i], 1);
                  }
               }
            }

            this.delayTimeTicks = 0;
            this.openedBefore = this.doneBefore = false;
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.delayTimeTicks < this.tickDelay.getValue()) {
         this.delayTimeTicks++;
      } else {
         this.delayTimeTicks = 0;
         if (this.planInventory.size() == 0) {
            this.disable();
         }

         if ((
               !(mc.player.openContainer instanceof ContainerChest)
                  || !this.enderChest.getValue()
                     && ((ContainerChest)mc.player.openContainer).getLowerChestInventory().getDisplayName().getUnformattedText().equals("Ender Chest")
            )
            && !(mc.player.openContainer instanceof ContainerShulkerBox)) {
            this.openedBefore = false;
         } else {
            this.sortInventoryAlgo();
         }
      }
   }

   private void sortInventoryAlgo() {
      if (!this.openedBefore) {
         int maxValue = mc.player.openContainer instanceof ContainerChest
            ? ((ContainerChest)mc.player.openContainer).getLowerChestInventory().getSizeInventory()
            : 27;

         for (int i = 0; i < maxValue; i++) {
            ItemStack item = (ItemStack)mc.player.openContainer.getInventory().get(i);
            this.containerInv.put(i, Objects.requireNonNull(item.getItem().getRegistryName()).toString() + item.getMetadata());
         }

         this.openedBefore = true;
         HashMap<Integer, String> inventoryCopy = this.getInventoryCopy(maxValue);
         HashMap<Integer, String> aimInventory = this.getInventoryCopy(maxValue, this.planInventory);
         this.sortItems = this.getInventorySort(inventoryCopy, aimInventory, maxValue);
         if (this.sortItems.size() == 0 && !this.doneBefore) {
            this.finishSort = false;
            if (this.closeAfter.getValue()) {
               mc.player.closeScreen();
            }
         } else {
            this.finishSort = true;
            this.stepNow = 0;
         }

         this.openedBefore = true;
      } else if (this.finishSort) {
         for (int i = 0; i < this.switchForTick.getValue(); i++) {
            if (this.sortItems.size() != 0) {
               int slotChange = this.sortItems.get(this.stepNow++);
               mc.playerController.windowClick(mc.player.openContainer.windowId, slotChange, 0, ClickType.PICKUP, mc.player);
            }

            if (this.stepNow == this.sortItems.size()) {
               if (this.confirmSort.getValue() && !this.doneBefore) {
                  this.openedBefore = false;
                  this.finishSort = false;
                  this.doneBefore = true;
                  this.checkLastItem();
                  return;
               }

               this.finishSort = false;
               if (this.infoMsgs.getValue()) {
                  MessageBus.printDebug("Inventory sorted", false);
               }

               this.checkLastItem();
               this.doneBefore = false;
               if (this.closeAfter.getValue()) {
                  mc.player.closeScreen();
               }

               return;
            }
         }
      }
   }

   private void checkLastItem() {
      if (this.sortItems.size() != 0) {
         int slotChange = this.sortItems.get(this.sortItems.size() - 1);
         if (((ItemStack)mc.player.openContainer.getInventory().get(slotChange)).isEmpty()) {
            mc.playerController.windowClick(0, slotChange, 0, ClickType.PICKUP, mc.player);
         }
      }
   }

   private ArrayList<Integer> getInventorySort(HashMap<Integer, String> copyInventory, HashMap<Integer, String> planInventoryCopy, int startValues) {
      ArrayList<Integer> planMove = new ArrayList<>();
      HashMap<String, Integer> nItemsCopy = new HashMap<>();

      for (String value : planInventoryCopy.values()) {
         if (nItemsCopy.containsKey(value)) {
            nItemsCopy.put(value, nItemsCopy.get(value) + 1);
         } else {
            nItemsCopy.put(value, 1);
         }
      }

      ArrayList<Integer> ignoreValues = new ArrayList<>();
      int[] listValue = new int[planInventoryCopy.size()];
      int id = 0;

      for (int idx : planInventoryCopy.keySet()) {
         listValue[id++] = idx;
      }

      for (int item : listValue) {
         if (copyInventory.get(item).equals(planInventoryCopy.get(item))) {
            ignoreValues.add(item);
            nItemsCopy.put(planInventoryCopy.get(item), nItemsCopy.get(planInventoryCopy.get(item)) - 1);
            if (nItemsCopy.get(planInventoryCopy.get(item)) == 0) {
               nItemsCopy.remove(planInventoryCopy.get(item));
            }

            planInventoryCopy.remove(item);
         }
      }

      String pickedItem = null;

      for (int i = startValues; i < startValues + copyInventory.size(); i++) {
         if (!ignoreValues.contains(i)) {
            String itemCheck = copyInventory.get(i);
            Optional<Entry<Integer, String>> momentAim = planInventoryCopy.entrySet().stream().filter(x -> x.getValue().equals(itemCheck)).findFirst();
            if (momentAim.isPresent()) {
               if (pickedItem == null) {
                  planMove.add(i);
               }

               int aimKey = momentAim.get().getKey();
               planMove.add(aimKey);
               if (pickedItem == null || !pickedItem.equals(itemCheck)) {
                  ignoreValues.add(aimKey);
               }

               nItemsCopy.put(itemCheck, nItemsCopy.get(itemCheck) - 1);
               if (nItemsCopy.get(itemCheck) == 0) {
                  nItemsCopy.remove(itemCheck);
               }

               copyInventory.put(i, copyInventory.get(aimKey));
               copyInventory.put(aimKey, itemCheck);
               if (!copyInventory.get(aimKey).equals("minecraft:air0")) {
                  if (i >= startValues + copyInventory.size()) {
                     continue;
                  }

                  pickedItem = copyInventory.get(i);
                  i--;
               } else {
                  pickedItem = null;
               }

               planInventoryCopy.remove(aimKey);
            } else if (pickedItem != null) {
               planMove.add(i);
               copyInventory.put(i, pickedItem);
               pickedItem = null;
            }
         }
      }

      if (planMove.size() != 0 && planMove.get(planMove.size() - 1).equals(planMove.get(planMove.size() - 2))) {
         planMove.remove(planMove.size() - 1);
      }

      Object[] keyList = this.containerInv.keySet().toArray();

      for (int values = 0; values < keyList.length; values++) {
         int itemC = (Integer)keyList[values];
         if (nItemsCopy.containsKey(this.containerInv.get(itemC))) {
            int start = planInventoryCopy.entrySet().stream().filter(x -> x.getValue().equals(this.containerInv.get(itemC))).findFirst().get().getKey();
            if (this.invasive.getValue() || ((ItemStack)mc.player.openContainer.getInventory().get(start)).isEmpty()) {
               planMove.add(start);
               planMove.add(itemC);
               planMove.add(start);
               nItemsCopy.put(planInventoryCopy.get(start), nItemsCopy.get(planInventoryCopy.get(start)) - 1);
               if (nItemsCopy.get(planInventoryCopy.get(start)) == 0) {
                  nItemsCopy.remove(planInventoryCopy.get(start));
               }

               planInventoryCopy.remove(start);
            }
         }
      }

      if (this.debugMode.getValue()) {
         for (int valuePath : planMove) {
            MessageBus.printDebug(Integer.toString(valuePath), false);
         }
      }

      return planMove;
   }

   private HashMap<Integer, String> getInventoryCopy(int startPoint) {
      HashMap<Integer, String> output = new HashMap<>();
      int sizeInventory = mc.player.inventory.mainInventory.size();

      for (int i = 0; i < sizeInventory; i++) {
         int value = i + startPoint + (i < 9 ? sizeInventory - 9 : -9);
         ItemStack item = (ItemStack)mc.player.openContainer.getInventory().get(value);
         output.put(value, Objects.requireNonNull(item.getItem().getRegistryName()).toString() + item.getMetadata());
      }

      return output;
   }

   private HashMap<Integer, String> getInventoryCopy(int startPoint, HashMap<Integer, String> inventory) {
      HashMap<Integer, String> output = new HashMap<>();
      int sizeInventory = mc.player.inventory.mainInventory.size();

      for (int val : inventory.keySet()) {
         output.put(val + startPoint + (val < 9 ? sizeInventory - 9 : -9), inventory.get(val));
      }

      return output;
   }
}
