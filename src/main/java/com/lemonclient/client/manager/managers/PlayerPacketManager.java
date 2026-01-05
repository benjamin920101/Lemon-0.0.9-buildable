package com.lemonclient.client.manager.managers;

import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEntityEvent;
import com.lemonclient.api.util.misc.CollectionUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.client.manager.Manager;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.render.RotateFixer;
import java.util.ArrayList;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public enum PlayerPacketManager implements Manager {
   INSTANCE;

   private final List<PlayerPacket> packets = new ArrayList<>();
   private Vec3d prevServerSidePosition = Vec3d.ZERO;
   private Vec3d serverSidePosition = Vec3d.ZERO;
   private Vec2f prevServerSideRotation = Vec2f.ZERO;
   private Vec2f serverSideRotation = Vec2f.ZERO;
   private Vec2f clientSidePitch = Vec2f.ZERO;
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
      if (event.getPhase() == Phase.BY && !this.packets.isEmpty()) {
         PlayerPacket packet = CollectionUtil.maxOrNull(this.packets, PlayerPacket::getPriority);
         if (packet != null) {
            event.cancel();
            event.apply(packet);
         }

         this.packets.clear();
      }
   });
   @EventHandler
   private final Listener<PacketEvent.PostSend> postSendListener = new Listener<>(event -> {
      if (!event.isCancelled()) {
         Packet<?> rawPacket = event.getPacket();
         EntityPlayerSP player = this.getPlayer();
         if (player != null && rawPacket instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)rawPacket;
            if (packet.moving) {
               this.serverSidePosition = new Vec3d(packet.x, packet.y, packet.z);
            }

            if (packet.rotating) {
               this.serverSideRotation = new Vec2f(packet.yaw, packet.pitch);
               player.rotationYawHead = packet.yaw;
            }
         }
      }
   }, -200);
   @EventHandler
   private final Listener<ClientTickEvent> tickEventListener = new Listener<>(event -> {
      if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START) {
         this.prevServerSidePosition = this.serverSidePosition;
         this.prevServerSideRotation = this.serverSideRotation;
      }
   });
   @EventHandler
   private final Listener<RenderEntityEvent.Head> renderEntityEventHeadListener = new Listener<>(
      event -> {
         if (ModuleManager.getModule(RotateFixer.class).isEnabled()) {
            EntityPlayerSP player = this.getPlayer();
            if (player != null
               && !player.isRiding()
               && event.getType() == RenderEntityEvent.Type.TEXTURE
               && event.getEntity() == player
               && Minecraft.getMinecraft().currentScreen == null) {
               this.clientSidePitch = new Vec2f(player.prevRotationPitch, player.rotationPitch);
               player.prevRotationPitch = this.prevServerSideRotation.y;
               player.rotationPitch = this.serverSideRotation.y;
            }
         }
      }
   );
   @EventHandler
   private final Listener<RenderEntityEvent.Return> renderEntityEventReturnListener = new Listener<>(
      event -> {
         if (ModuleManager.getModule(RotateFixer.class).isEnabled()) {
            EntityPlayerSP player = this.getPlayer();
            if (player != null
               && !player.isRiding()
               && event.getType() == RenderEntityEvent.Type.TEXTURE
               && event.getEntity() == player
               && Minecraft.getMinecraft().currentScreen == null) {
               player.prevRotationPitch = this.clientSidePitch.x;
               player.rotationPitch = this.clientSidePitch.y;
            }
         }
      }
   );

   public void addPacket(PlayerPacket packet) {
      this.packets.add(packet);
   }

   public Vec3d getPrevServerSidePosition() {
      return this.prevServerSidePosition;
   }

   public Vec3d getServerSidePosition() {
      return this.serverSidePosition;
   }

   public Vec2f getPrevServerSideRotation() {
      return this.prevServerSideRotation;
   }

   public Vec2f getServerSideRotation() {
      return this.serverSideRotation;
   }
}
