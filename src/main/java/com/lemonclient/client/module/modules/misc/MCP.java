package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import org.lwjgl.input.Mouse;

@Module.Declaration(name = "MCP", category = Category.Misc)
public class MCP extends Module {
   BooleanSetting clipRotate = this.registerBoolean("clipRotate", false);
   IntegerSetting pearlPitch = this.registerInteger("Pitch", 85, -90, 90, () -> this.clipRotate.getValue());
   BooleanSetting block = this.registerBoolean("nearBlock", true, () -> this.clipRotate.getValue());
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", false);
   BooleanSetting check = this.registerBoolean("Switch Check", false);
   @EventHandler
   private final Listener<MouseInputEvent> listener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead && mc.player.inventory != null) {
            if (Mouse.getEventButton() == 2) {
               if (mc.objectMouseOver.typeOfHit == Type.ENTITY) {
                  return;
               }

               if (this.clipRotate.getValue() && (!this.block.getValue() || mc.objectMouseOver.typeOfHit == Type.BLOCK)) {
                  mc.player
                     .connection
                     .sendPacket(new Rotation(mc.player.rotationYaw, this.pearlPitch.getValue().floatValue(), mc.player.onGround));
               }

               int pearlInvSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 35);
               int pearlHotSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 8);
               if (pearlInvSlot == -1 && pearlHotSlot == -1) {
                  return;
               }

               int oldSlot = mc.player.inventory.currentItem;
               if (pearlHotSlot == -1) {
                  ItemStack itemStack = mc.player.inventory.getStackInSlot(pearlInvSlot);
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketClickWindow(
                           0,
                           pearlInvSlot,
                           mc.player.inventory.currentItem,
                           ClickType.SWAP,
                           ItemStack.EMPTY,
                           mc.player.openContainer.getNextTransactionID(mc.player.inventory)
                        )
                     );
                  mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketClickWindow(
                           0,
                           pearlInvSlot,
                           mc.player.inventory.currentItem,
                           ClickType.SWAP,
                           itemStack,
                           mc.player.openContainer.getNextTransactionID(mc.player.inventory)
                        )
                     );
               } else {
                  this.switchTo(pearlHotSlot);
                  mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                  this.switchTo(oldSlot);
               }
            }
         }
      }
   );

   private void switchTo(int slot) {
      if (slot > -1 && slot < 9 && (!this.check.getValue() || mc.player.inventory.currentItem != slot)) {
         if (this.packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         } else {
            mc.player.inventory.currentItem = slot;
         }
      }
   }
}
