package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;

@Module.Declaration(name = "NoKick", category = Category.Misc)
public class NoKick extends Module {
   public BooleanSetting noPacketKick = this.registerBoolean("Packet", true);
   BooleanSetting noSlimeCrash = this.registerBoolean("Slime", false);
   BooleanSetting noOffhandCrash = this.registerBoolean("Offhand", false);
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (this.noOffhandCrash.getValue()
            && event.getPacket() instanceof SPacketSoundEffect
            && ((SPacketSoundEffect)event.getPacket()).getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
            event.cancel();
         }
      }
   );

   @Override
   public void onUpdate() {
      if (mc.world != null && this.noSlimeCrash.getValue()) {
         mc.world.loadedEntityList.forEach(entity -> {
            if (entity instanceof EntitySlime) {
               EntitySlime slime = (EntitySlime)entity;
               if (slime.getSlimeSize() > 4) {
                  mc.world.removeEntity(entity);
               }
            }
         });
      }
   }
}
