package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;

@Module.Declaration(name = "AutoSwitchEChest", category = Category.Dev)
public class AutoEChest extends Module {
   IntegerSetting count = this.registerInteger("Count", 16, 1, 64);
   IntegerSetting backCount = this.registerInteger("SwitchBack Count", 121, 1, 256);
   BooleanSetting update = this.registerBoolean("UpdateController", true);
   int slot;
   int slot2;
   boolean switched = false;

   private void windowClick(int slot, int to) {
      mc.player
         .connection
         .sendPacket(
            new CPacketClickWindow(
               mc.player.inventoryContainer.windowId,
               slot,
               to,
               ClickType.SWAP,
               ItemStack.EMPTY,
               mc.player.openContainer.getNextTransactionID(mc.player.inventory)
            )
         );
      if (this.update.getValue()) {
         mc.playerController.updateController();
      }
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         int slot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         int echest = BurrowUtil.findInventoryBlock(BlockEnderChest.class);
         if (slot != -1 && echest != -1) {
            ItemStack stack = (ItemStack)mc.player.inventory.mainInventory.get(slot);
            if (stack.stackSize <= this.count.getValue()) {
               this.windowClick(echest, slot);
               this.slot = echest;
               this.slot2 = slot;
               this.switched = true;
            }

            if (this.switched) {
               int obsiCount = BurrowUtil.getCount(BlockObsidian.class);
               if (obsiCount >= this.backCount.getValue()) {
                  this.windowClick(this.slot, this.slot2);
               }
            }
         }
      }
   }
}
