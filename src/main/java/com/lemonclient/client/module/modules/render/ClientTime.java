package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@Module.Declaration(name = "ClientTime", category = Category.Render)
public class ClientTime extends Module {
   IntegerSetting time = this.registerInteger("Time", 1000, 0, 24000);
   @EventHandler
   private final Listener<PacketEvent.Receive> noTimeUpdates = new Listener<>(event -> {
      if (event.getPacket() instanceof SPacketTimeUpdate) {
         event.cancel();
      }
   });

   @Override
   public void onUpdate() {
      mc.world.setWorldTime(this.time.getValue().intValue());
   }
}
