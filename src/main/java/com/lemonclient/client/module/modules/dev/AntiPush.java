package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;

@Module.Declaration(name = "AntiPush", category = Category.Dev, priority = 1000)
public class AntiPush extends Module {
   BooleanSetting move = this.registerBoolean("Move", false);
   @EventHandler
   public final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
      MoverType moverType = event.getType();
      if (moverType != MoverType.SELF && moverType != MoverType.PLAYER) {
         event.cancel();
      }
   });

   @Override
   public void fast() {
      if (mc.world != null && mc.player != null && !mc.player.isDead && this.move.getValue()) {
         MovementInput input = mc.player.movementInput;
         if (input.moveForward == 0.0 && input.moveStrafe == 0.0) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
         }
      }
   }
}
