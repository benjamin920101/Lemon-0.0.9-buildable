package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.EntityCollisionEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.WaterPushEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;

@Module.Declaration(name = "PlayerTweaks", category = Category.Movement)
public class PlayerTweaks extends Module {
   public BooleanSetting guiMove = this.registerBoolean("Gui Move", false);
   BooleanSetting noPush = this.registerBoolean("No Push", false);
   BooleanSetting noFall = this.registerBoolean("No Fall", false);
   public BooleanSetting noSlow = this.registerBoolean("No Slow", false);
   BooleanSetting antiKnockBack = this.registerBoolean("Velocity", false);
   @EventHandler
   private final Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {
      if (this.noSlow.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
         event.getMovementInput().moveStrafe *= 5.0F;
         event.getMovementInput().moveForward *= 5.0F;
      }
   });
   @EventHandler
   private final Listener<EntityCollisionEvent> entityCollisionEventListener = new Listener<>(event -> {
      if (this.noPush.getValue()) {
         event.cancel();
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (this.antiKnockBack.getValue()) {
            if (event.getPacket() instanceof SPacketEntityVelocity
               && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == mc.player.getEntityId()) {
               event.cancel();
            }

            if (event.getPacket() instanceof SPacketExplosion) {
               event.cancel();
            }
         }
      }
   );
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.noFall.getValue() && event.getPacket() instanceof CPacketPlayer && mc.player.fallDistance >= 3.0) {
         CPacketPlayer packet = (CPacketPlayer)event.getPacket();
         packet.onGround = true;
      }
   });
   @EventHandler
   private final Listener<WaterPushEvent> waterPushEventListener = new Listener<>(event -> {
      if (this.noPush.getValue()) {
         event.cancel();
      }
   });

   @Override
   public void onUpdate() {
      if (this.guiMove.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
         if (Keyboard.isKeyDown(200)) {
            mc.player.rotationPitch -= 5.0F;
         }

         if (Keyboard.isKeyDown(208)) {
            mc.player.rotationPitch += 5.0F;
         }

         if (Keyboard.isKeyDown(205)) {
            mc.player.rotationYaw += 5.0F;
         }

         if (Keyboard.isKeyDown(203)) {
            mc.player.rotationYaw -= 5.0F;
         }

         if (mc.player.rotationPitch > 90.0F) {
            mc.player.rotationPitch = 90.0F;
         }

         if (mc.player.rotationPitch < -90.0F) {
            mc.player.rotationPitch = -90.0F;
         }
      }
   }
}
