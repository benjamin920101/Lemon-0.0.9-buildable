package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.potion.PotionUtils;

@Module.Declaration(name = "Quiver", category = Category.Combat)
public class Quiver extends Module {
   IntegerSetting tickDelay = this.registerInteger("TickDelay", 3, 0, 8);

   @Override
   public void onUpdate() {
      if (mc.player != null) {
         if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow
            && mc.player.isHandActive()
            && mc.player.getItemInUseMaxCount() >= this.tickDelay.getValue()) {
            mc.player.connection.sendPacket(new Rotation(mc.player.cameraYaw, -90.0F, mc.player.onGround));
            mc.playerController.onStoppedUsingItem(mc.player);
         }

         List<Integer> arrowSlots = getItemInventory(Items.TIPPED_ARROW);
         if (arrowSlots.get(0) == -1) {
            return;
         }

         int speedSlot = -1;
         int strengthSlot = -1;

         for (Integer slot : arrowSlots) {
            if (PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot)).getRegistryName().getPath().contains("swiftness")) {
               speedSlot = slot;
            } else if (Objects.requireNonNull(PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot)).getRegistryName())
               .getPath()
               .contains("strength")) {
               strengthSlot = slot;
            }
         }
      }
   }

   public static List<Integer> getItemInventory(Item item) {
      List<Integer> ints = new ArrayList<>();

      for (int i = 9; i < 36; i++) {
         Item target = mc.player.inventory.getStackInSlot(i).getItem();
         if (item instanceof ItemBlock && ((ItemBlock)item).getBlock().equals(item)) {
            ints.add(i);
         }
      }

      if (ints.size() == 0) {
         ints.add(-1);
      }

      return ints;
   }
}
