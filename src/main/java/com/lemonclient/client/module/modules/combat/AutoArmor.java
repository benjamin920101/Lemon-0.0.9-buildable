package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.InvStack;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.Locks;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;

@Module.Declaration(name = "AutoArmor", category = Category.Combat)
public class AutoArmor extends Module {
   IntegerSetting delay = this.registerInteger("Delay", 1, 1, 10);
   BooleanSetting noDesync = this.registerBoolean("No Desync", true);
   BooleanSetting illegalSync = this.registerBoolean("Illegal Sync", true);
   IntegerSetting checkDelay = this.registerInteger("Check Delay", 1, 0, 20, () -> this.noDesync.getValue());
   BooleanSetting strict = this.registerBoolean("Strict", false);
   BooleanSetting stackArmor = this.registerBoolean("Stack Armor", false);
   IntegerSetting slot = this.registerInteger("Swap Slot", 1, 1, 9, () -> this.stackArmor.getValue());
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.stackArmor.getValue());
   BooleanSetting armorSaver = this.registerBoolean("Armor Saver", false);
   BooleanSetting pauseWhenSafe = this.registerBoolean("Pause When Safe", false);
   IntegerSetting depletion = this.registerInteger("Depletion", 20, 0, 99, () -> this.armorSaver.getValue());
   BooleanSetting allowMend = this.registerBoolean("Allow Mend", false);
   IntegerSetting repair = this.registerInteger("Repair", 80, 0, 100);
   Timing rightClickTimer = new Timing();
   Timing timer = new Timing();
   private boolean sleep;
   @EventHandler
   private final Listener<RightClickItem> listener = new Listener<>(event -> {
      if (event.getEntityPlayer() == mc.player) {
         if (event.getItemStack().getItem() == Items.EXPERIENCE_BOTTLE) {
            this.rightClickTimer.reset();
         }
      }
   });

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (mc.player.ticksExisted % this.delay.getValue() == 0 && !this.checkDesync()) {
            if (!this.strict.getValue() || mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
               if (this.pauseWhenSafe.getValue()) {
                  List<Entity> proximity = mc.world
                     .loadedEntityList
                     .stream()
                     .filter(
                        e -> e instanceof EntityPlayer && !e.equals(mc.player) && mc.player.getDistance(e) <= 6.0F
                           || e instanceof EntityEnderCrystal && mc.player.getDistance(e) <= 12.0F
                     )
                     .collect(Collectors.toList());
                  if (proximity.isEmpty()) {
                     return;
                  }
               }

               boolean isMending = ModuleManager.isModuleEnabled(AutoMend.class);
               if (this.allowMend.getValue() && !this.rightClickTimer.passedMs(500L)) {
                  for (int i = 0; i < mc.player.inventory.armorInventory.size(); i++) {
                     ItemStack armorPiece = (ItemStack)mc.player.inventory.armorInventory.get(i);
                     if (armorPiece.isEmpty) {
                        return;
                     }

                     boolean mending = false;

                     for (Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(armorPiece).entrySet()) {
                        if (entry.getKey().getName().contains("mending")) {
                           mending = true;
                           break;
                        }
                     }

                     if (mending && !armorPiece.isEmpty()) {
                        long freeSlots = mc.player
                           .inventory
                           .mainInventory
                           .stream()
                           .filter(is -> is.isEmpty() || is.getItem() == Items.AIR)
                           .map(is -> mc.player.inventory.getSlotFor(is))
                           .count();
                        if (freeSlots <= 0L) {
                           return;
                        }

                        if (armorPiece.getItemDamage() != 0) {
                           this.shiftClickSpot(8 - i);
                           return;
                        }
                     }
                  }
               } else if (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory) {
                  AtomicBoolean hasSwapped = new AtomicBoolean(false);
                  if (this.sleep) {
                     this.sleep = false;
                  } else {
                     Set<InvStack> replacements = new HashSet<>();

                     for (int slot = 0; slot < 45; slot++) {
                        if (slot <= 4 || slot >= 9) {
                           InvStack invStack = new InvStack(slot, mc.player.inventoryContainer.getSlot(slot).getStack());
                           if (invStack.stack.getItem() instanceof ItemArmor || invStack.stack.getItem() instanceof ItemElytra) {
                              replacements.add(invStack);
                           }
                        }
                     }

                     List<InvStack> armors = replacements.stream()
                        .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                        .filter(
                           invStack -> !this.armorSaver.getValue()
                              || invStack.stack.getItem().getDurabilityForDisplay(invStack.stack) < this.depletion.getValue().intValue()
                        )
                        .sorted(Comparator.comparingInt(invStack -> invStack.slot))
                        .sorted(Comparator.comparingInt(invStack -> ((ItemArmor)invStack.stack.getItem()).damageReduceAmount))
                        .collect(Collectors.toList());
                     boolean wasEmpty = armors.isEmpty();
                     if (wasEmpty) {
                        armors = replacements.stream()
                           .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                           .sorted(Comparator.comparingInt(invStack -> invStack.slot))
                           .sorted(Comparator.comparingInt(invStack -> ((ItemArmor)invStack.stack.getItem()).damageReduceAmount))
                           .collect(Collectors.toList());
                     }

                     ItemStack currentHeadItem = mc.player.inventory.getStackInSlot(39);
                     ItemStack currentChestItem = mc.player.inventory.getStackInSlot(38);
                     ItemStack currentLegsItem = mc.player.inventory.getStackInSlot(37);
                     ItemStack currentFeetItem = mc.player.inventory.getStackInSlot(36);
                     boolean saveHead = !wasEmpty
                        && currentHeadItem.getCount() == 1
                        && this.armorSaver.getValue()
                        && this.getItemDamage(5) <= this.depletion.getValue();
                     boolean saveChest = !wasEmpty
                        && currentChestItem.getCount() == 1
                        && this.armorSaver.getValue()
                        && this.getItemDamage(6) <= this.depletion.getValue();
                     boolean saveLegs = !wasEmpty
                        && currentLegsItem.getCount() == 1
                        && this.armorSaver.getValue()
                        && this.getItemDamage(7) <= this.depletion.getValue();
                     boolean saveFeet = !wasEmpty
                        && currentFeetItem.getCount() == 1
                        && this.armorSaver.getValue()
                        && this.getItemDamage(8) <= this.depletion.getValue();
                     boolean replaceHead = currentHeadItem.isEmpty || saveHead || isMending && this.getItemDamage(5) >= this.repair.getValue();
                     boolean replaceChest = currentChestItem.isEmpty || saveChest || isMending && this.getItemDamage(6) >= this.repair.getValue();
                     boolean replaceLegs = currentLegsItem.isEmpty || saveLegs || isMending && this.getItemDamage(7) >= this.repair.getValue();
                     boolean replaceFeet = currentFeetItem.isEmpty || saveFeet || isMending && this.getItemDamage(8) >= this.repair.getValue();
                     if (replaceHead && !hasSwapped.get()) {
                        armors.stream()
                           .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                           .filter(invStack -> ((ItemArmor)invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.HEAD))
                           .filter(invStack -> !saveHead || this.getItemDamage(invStack.slot) > this.depletion.getValue())
                           .filter(invStack -> !isMending || this.getItemDamage(invStack.slot) <= this.repair.getValue())
                           .findFirst()
                           .ifPresent(invStack -> {
                              this.swapSlot(invStack.slot, 5);
                              hasSwapped.set(true);
                           });
                     }

                     if (replaceChest || currentChestItem.getItem() instanceof ItemElytra && !hasSwapped.get()) {
                        armors.stream()
                           .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                           .filter(invStack -> ((ItemArmor)invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.CHEST))
                           .filter(invStack -> !saveChest || this.getItemDamage(invStack.slot) > this.depletion.getValue())
                           .filter(invStack -> !isMending || this.getItemDamage(invStack.slot) <= this.repair.getValue())
                           .findFirst()
                           .ifPresent(invStack -> {
                              this.swapSlot(invStack.slot, 6);
                              hasSwapped.set(true);
                           });
                     }

                     if (replaceLegs && !hasSwapped.get()) {
                        armors.stream()
                           .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                           .filter(invStack -> ((ItemArmor)invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.LEGS))
                           .filter(invStack -> !saveLegs || this.getItemDamage(invStack.slot) > this.depletion.getValue())
                           .filter(invStack -> !isMending || this.getItemDamage(invStack.slot) <= this.repair.getValue())
                           .findFirst()
                           .ifPresent(invStack -> {
                              this.swapSlot(invStack.slot, 7);
                              hasSwapped.set(true);
                           });
                     }

                     if (replaceFeet && !hasSwapped.get()) {
                        armors.stream()
                           .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                           .filter(invStack -> ((ItemArmor)invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.FEET))
                           .filter(invStack -> !saveFeet || this.getItemDamage(invStack.slot) > this.depletion.getValue())
                           .filter(invStack -> !isMending || this.getItemDamage(invStack.slot) <= this.repair.getValue())
                           .findFirst()
                           .ifPresent(invStack -> {
                              this.swapSlot(invStack.slot, 8);
                              hasSwapped.set(true);
                           });
                     }
                  }
               }
            }
         }
      }
   }

   private int getItemDamage(int slot) {
      ItemStack itemStack = mc.player.inventoryContainer.getSlot(slot).getStack();
      float green = ((float)itemStack.getMaxDamage() - itemStack.getItemDamage()) / itemStack.getMaxDamage();
      float red = 1.0F - green;
      return 100 - (int)(red * 100.0F);
   }

   private void swapSlot(int source, int target) {
      ItemStack sourceStack = mc.player.inventoryContainer.getSlot(source).getStack();
      boolean stacked = sourceStack.getCount() > 1;
      if (stacked) {
         this.swapStack(source, target);
      } else {
         this.swap(source, target);
      }

      this.sleep = true;
   }

   private void swapStack(int slotFrom, int slotTo) {
      if (this.stackArmor.getValue()) {
         if (mc.player.inventoryContainer.getSlot(slotTo).getStack() != ItemStack.EMPTY) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotTo, 0, ClickType.QUICK_MOVE, mc.player);
         }

         int slot = this.slot.getValue() - 1;
         if (slotFrom < 36) {
            this.swapToHotbar(slotFrom);
         } else {
            slot = slotFrom - 36;
         }

         InventoryUtil.run(
            slot, this.packetSwitch.getValue(), () -> mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
         );
         if (slotFrom < 36) {
            this.swapToHotbar(slotFrom);
         }
      }
   }

   private boolean checkDesync() {
      if (this.noDesync.getValue() && !(mc.currentScreen instanceof GuiContainer)
         || mc.currentScreen instanceof GuiInventory && this.timer.passedMs(this.checkDelay.getValue() * 50)) {
         int bestSlot = -1;
         int clientValue = 0;
         boolean foundType = false;
         int armorValue = mc.player.getTotalArmorValue();

         for (int i = 5; i < 9; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack.isEmpty() && !foundType) {
               bestSlot = i;
               foundType = true;
            } else if (stack.getItem() instanceof ItemArmor) {
               ItemArmor itemArmor = (ItemArmor)stack.getItem();
               clientValue += itemArmor.damageReduceAmount;
            }
         }

         if (clientValue != armorValue && this.timer.passedMs(this.delay.getValue() * 50)) {
            if (this.illegalSync.getValue()) {
               InventoryUtil.illegalSync();
            } else if (bestSlot != -1 && getSlot(mc.player.inventory.getItemStack()) == fromSlot(bestSlot)) {
               Item ix = get(bestSlot).getItem();
               clickLocked(bestSlot, bestSlot, ix, ix);
            } else {
               Item ix = get(20).getItem();
               clickLocked(20, 20, ix, ix);
            }

            this.timer.reset();
            return true;
         }
      }

      return false;
   }

   public static void clickLocked(int slot, int to, Item inSlot, Item inTo) {
      Locks.acquire(Locks.WINDOW_CLICK_LOCK, () -> {
         if ((slot == -1 || get(slot).getItem() == inSlot) && get(to).getItem() == inTo) {
            boolean multi = slot >= 0;
            if (multi) {
               click(slot);
            }

            click(to);
         }
      });
   }

   public static void click(int slot) {
      mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
   }

   public static ItemStack get(int slot) {
      return slot == -2 ? mc.player.inventory.getItemStack() : (ItemStack)mc.player.inventoryContainer.getInventory().get(slot);
   }

   public static EntityEquipmentSlot fromSlot(int slot) {
      switch (slot) {
         case 5:
            return EntityEquipmentSlot.HEAD;
         case 6:
            return EntityEquipmentSlot.CHEST;
         case 7:
            return EntityEquipmentSlot.LEGS;
         case 8:
            return EntityEquipmentSlot.FEET;
         default:
            ItemStack stack = get(slot);
            return getSlot(stack);
      }
   }

   public static EntityEquipmentSlot getSlot(ItemStack stack) {
      if (!stack.isEmpty()) {
         if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)stack.getItem();
            return armor.getEquipmentSlot();
         }

         if (stack.getItem() instanceof ItemElytra) {
            return EntityEquipmentSlot.CHEST;
         }
      }

      return null;
   }

   private void swapToHotbar(int InvSlot) {
      mc.playerController.windowClick(0, InvSlot, this.slot.getValue() - 1, ClickType.SWAP, mc.player);
      mc.playerController.updateController();
   }

   private void swap(int slotFrom, int slotTo) {
      if (mc.player.inventoryContainer.getSlot(slotTo).getStack().isEmpty) {
         mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.QUICK_MOVE, mc.player);
      } else {
         boolean hasEmpty = false;

         for (int l_I = 0; l_I < 36; l_I++) {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            if (l_Stack.isEmpty) {
               hasEmpty = true;
               break;
            }
         }

         if (hasEmpty) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotTo, 0, ClickType.QUICK_MOVE, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.QUICK_MOVE, mc.player);
         } else {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotTo, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotFrom, 0, ClickType.PICKUP, mc.player);
         }
      }

      mc.playerController.updateController();
   }

   private void shiftClickSpot(int source) {
      mc.playerController.windowClick(mc.player.inventoryContainer.windowId, source, 0, ClickType.QUICK_MOVE, mc.player);
   }
}
