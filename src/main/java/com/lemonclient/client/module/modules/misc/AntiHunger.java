package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;

@Module.Declaration(name = "AntiHunger", category = Category.Misc, priority = 999)
public class AntiHunger extends Module {
   BooleanSetting cancelMove = this.registerBoolean("Cancel Spring", false);
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         if (event.getPacket() instanceof Position) {
            this.onPacket((Position)event.getPacket());
         }

         if (event.getPacket() instanceof CPacketEntityAction && this.cancelMove.getValue()) {
            CPacketEntityAction packet = (CPacketEntityAction)event.getPacket();
            if (packet.getAction() == Action.START_SPRINTING || packet.getAction() == Action.STOP_SPRINTING) {
               event.cancel();
            }
         }
      }
   });

   private void onPacket(CPacketPlayer packet) {
      packet.onGround = (mc.player.fallDistance <= 0.0F || mc.playerController.isHittingBlock) && mc.player.isElytraFlying();
   }
}
