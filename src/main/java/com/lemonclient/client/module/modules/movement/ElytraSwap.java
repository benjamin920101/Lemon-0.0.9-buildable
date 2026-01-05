package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "ElytraSwap", category = Category.Movement)
public class ElytraSwap extends Module {
   @Override
   public void onEnable() {
      if (mc.player != null) {
         InventoryPlayer items = mc.player.inventory;
         ItemStack body = items.armorItemInSlot(2);
         String body2 = body.getItem().getItemStackDisplayName(body);
         if (body2.equals("Air")) {
            int t = 0;
            int c = 0;

            for (int i = 9; i < 45; i++) {
               if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA) {
                  t = i;
                  break;
               }
            }

            if (t != 0) {
               MessageBus.sendClientDeleteMessage("Equipping Elytra", Notification.Type.SUCCESS, "ElytraSwap", 1);
               mc.playerController.windowClick(0, t, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
            }

            if (t == 0) {
               for (int var13 = 9; var13 < 45; var13++) {
                  if (mc.player.inventory.getStackInSlot(var13).getItem() == Items.DIAMOND_CHESTPLATE) {
                     c = var13;
                     break;
                  }
               }

               if (c != 0) {
                  MessageBus.sendClientDeleteMessage("Equipping Chestplate", Notification.Type.SUCCESS, "ElytraSwap", 1);
                  mc.playerController.windowClick(0, c, 0, ClickType.PICKUP, mc.player);
                  mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
               }
            }

            if (c == 0 && t == 0) {
               MessageBus.sendClientDeleteMessage(
                  "You do not have an Elytra or a Chestplate in your inventory. Doing nothing", Notification.Type.ERROR, "ElytraSwap", 1
               );
            }

            this.disable();
         }

         if (body2.equals("Elytra")) {
            int t = 0;

            for (int ix = 9; ix < 45; ix++) {
               if (mc.player.inventory.getStackInSlot(ix).getItem() == Items.DIAMOND_CHESTPLATE) {
                  t = ix;
                  break;
               }
            }

            if (t != 0) {
               int l = 0;
               MessageBus.sendClientDeleteMessage("Equipping Chestplate", Notification.Type.SUCCESS, "ElytraSwap", 1);
               mc.playerController.windowClick(0, t, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);

               for (int j = 9; j < 45; j++) {
                  if (mc.player.inventory.getStackInSlot(j).getItem() == Items.AIR) {
                     l = j;
                     break;
                  }
               }

               mc.playerController.windowClick(0, l, 0, ClickType.PICKUP, mc.player);
            }

            if (t == 0) {
               MessageBus.sendClientDeleteMessage(
                  "You do not have a Chestplate in your inventory. Keeping Elytra equipped", Notification.Type.ERROR, "ElytraSwap", 1
               );
            }

            this.disable();
         }

         if (body2.equals("Diamond Chestplate")) {
            int t = 0;

            for (int ixx = 9; ixx < 45; ixx++) {
               if (mc.player.inventory.getStackInSlot(ixx).getItem() == Items.ELYTRA) {
                  t = ixx;
                  break;
               }
            }

            if (t != 0) {
               int u = 0;
               MessageBus.sendClientDeleteMessage("Equipping Elytra", Notification.Type.SUCCESS, "ElytraSwap", 1);
               mc.playerController.windowClick(0, t, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);

               for (int jx = 9; jx < 45; jx++) {
                  if (mc.player.inventory.getStackInSlot(jx).getItem() == Items.AIR) {
                     u = jx;
                     break;
                  }
               }

               mc.playerController.windowClick(0, u, 0, ClickType.PICKUP, mc.player);
            }

            if (t == 0) {
               MessageBus.sendClientDeleteMessage(
                  "You do not have a Elytra in your inventory. Keeping Chestplate equipped", Notification.Type.ERROR, "ElytraSwap", 1
               );
            }

            this.disable();
         }
      }
   }
}
