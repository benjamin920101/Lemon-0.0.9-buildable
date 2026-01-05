package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "test Module", category = Category.qwq)
public class testModule extends Module {
   BooleanSetting ewe = this.registerBoolean("Don't Use or AutoCrash", true);
   BlockPos pos;
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(
      event -> {
         Packet<?> pack = event.getPacket();
         if (pack instanceof CPacketClickWindow) {
            CPacketClickWindow s = (CPacketClickWindow)pack;
            this.sendMessage(
               "CPacketClickWindow\n - Acton Number: "
                  + s.getActionNumber()
                  + "\n - Window ID: "
                  + s.getWindowId()
                  + "\n - Slot ID: "
                  + s.getSlotId()
                  + "\n - Button: "
                  + s.getUsedButton()
                  + "\n - Item Name: "
                  + s.getClickedItem().getDisplayName()
                  + "\n - Click Type Name: "
                  + s.getClickType().name()
            );
         } else if (pack instanceof CPacketConfirmTeleport) {
            CPacketConfirmTeleport s = (CPacketConfirmTeleport)pack;
            this.sendMessage("CPacketConfirmTeleport\n - Tp id: " + s.getTeleportId());
         } else if (pack instanceof CPacketConfirmTransaction) {
            CPacketConfirmTransaction s = (CPacketConfirmTransaction)pack;
            this.sendMessage("CPacketConfirmTransaction\n - Id: " + s.getUid());
         } else if (pack instanceof CPacketClientStatus) {
            CPacketClientStatus s = (CPacketClientStatus)pack;
            this.sendMessage("CPacketClientStatus\n - Status Name: " + s.getStatus().name());
         } else if (pack instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock s = (CPacketPlayerTryUseItemOnBlock)pack;
            this.sendMessage(
               "CPacketPlayerTryUseItemOnBlock\n - Pos: "
                  + s.getPos().x
                  + ", "
                  + s.getPos().y
                  + ", "
                  + s.getPos().z
                  + "\n - Side: "
                  + s.getDirection()
                  + "\n - HitVec: "
                  + s.getFacingX()
                  + ", "
                  + s.getFacingY()
                  + ", "
                  + s.getFacingZ()
            );
         } else if (pack instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem s = (CPacketPlayerTryUseItem)pack;
            this.sendMessage("CPacketPlayerTryUseItem\n - Hand: " + s.getHand().name());
         }

         if (pack instanceof CPacketHeldItemChange) {
            CPacketHeldItemChange s = (CPacketHeldItemChange)pack;
            this.sendMessage("CPacketHeldItemChange\n - Slot: " + s.getSlotId());
         } else if (pack instanceof CPacketEntityAction) {
            CPacketEntityAction s = (CPacketEntityAction)pack;
            this.sendMessage("CPacketEntityAction\n - Action: " + s.getAction().name() + "\n - Data: " + s.getAuxData());
         } else if (pack instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging s = (CPacketPlayerDigging)pack;
            this.sendMessage("CPacketPlayerDigging\n - Action: " + s.getAction().name());
         }
      }
   );

   @Override
   public void onEnable() {
   }

   @Override
   public void onUpdate() {
   }

   void sendMessage(String message) {
      MessageBus.sendClientRawMessage(message);
   }
}
