package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEntityEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.SoundCategory;

@Module.Declaration(name = "AntiLag", category = Category.Misc)
public class NoLag extends Module {
   BooleanSetting particles = this.registerBoolean("Particles", true);
   BooleanSetting effect = this.registerBoolean("Effect", true);
   BooleanSetting soundEffect = this.registerBoolean("Sound Effect", true);
   BooleanSetting skulls = this.registerBoolean("Skull", true);
   BooleanSetting tnt = this.registerBoolean("Tnt", true);
   BooleanSetting parrots = this.registerBoolean("Parrot", true);
   BooleanSetting spawn = this.registerBoolean("Spawn", true);
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (event.getPacket() instanceof SPacketParticles && this.particles.getValue()) {
         event.cancel();
      }

      if (event.getPacket() instanceof SPacketEffect && this.effect.getValue()) {
         event.cancel();
      }

      if (event.getPacket() instanceof SPacketSoundEffect && this.soundEffect.getValue()) {
         SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();
         if (packet.getCategory() == SoundCategory.PLAYERS && packet.getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
            event.cancel();
         }

         if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER) {
            event.cancel();
         }
      }

      if (event.getPacket() instanceof SPacketSpawnMob && this.spawn.getValue()) {
         SPacketSpawnMob packetx = (SPacketSpawnMob)event.getPacket();
         if (packetx.getEntityType() == 55) {
            event.cancel();
         }
      }
   });
   @EventHandler
   private final Listener<RenderEntityEvent> renderEntityEventListener = new Listener<>(event -> {
      if (this.skulls.getValue() && event.getEntity() instanceof EntityWitherSkull) {
         event.cancel();
      }

      if (this.tnt.getValue() && event.getEntity() instanceof EntityTNTPrimed) {
         event.cancel();
      }

      if (this.parrots.getValue() && event.getEntity() instanceof EntityParrot) {
         event.cancel();
      }
   });

   @Override
   public void onDisable() {
      mc.renderGlobal.loadRenderers();
      super.onDisable();
   }
}
