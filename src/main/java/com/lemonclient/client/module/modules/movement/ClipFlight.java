package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer.Position;

@Module.Declaration(name = "ClipFlight", category = Category.Exploits)
public class ClipFlight extends Module {
   ModeSetting flight = this.registerMode("Mode", Arrays.asList("Flight", "Clip"), "Clip");
   IntegerSetting packets = this.registerInteger("Packets", 80, 1, 300);
   IntegerSetting speed = this.registerInteger("XZ Speed", 7, -99, 99, () -> this.flight.getValue().equalsIgnoreCase("Flight"));
   IntegerSetting speedY = this.registerInteger("Y Speed", 7, -99, 99, () -> !this.flight.getValue().equalsIgnoreCase("Relative"));
   BooleanSetting bypass = this.registerBoolean("Bypass", false);
   IntegerSetting interval = this.registerInteger("Interval", 25, 1, 100, () -> this.flight.getValue().equalsIgnoreCase("Clip"));
   BooleanSetting update = this.registerBoolean("Update Position Client Side", false);
   int num = 0;
   double startFlat = 0.0;
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(
      event -> {
         double[] dir = MotionUtil.forward(this.speed.getValue().intValue());
         String var3 = this.flight.getValue();
         switch (var3) {
            case "Flight":
               double xPos = mc.player.posX;
               double yPos = mc.player.posY;
               double zPos = mc.player.posZ;
               if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                  yPos += this.speedY.getValue().intValue();
               } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                  yPos -= this.speedY.getValue().intValue();
               }

               xPos += dir[0];
               zPos += dir[1];
               mc.player.connection.sendPacket(new Position(xPos, yPos, zPos, false));
               if (this.update.getValue()) {
                  mc.player.setPosition(xPos, yPos, zPos);
               }

               if (this.bypass.getValue()) {
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, true));
               }
               break;
            case "Clip":
               if (mc.gameSettings.keyBindSprint.isKeyDown() || mc.player.ticksExisted % this.interval.getValue() == 0) {
                  for (int i = 0; i < this.packets.getValue(); i++) {
                     double yposition = mc.player.posY + this.speedY.getValue().intValue();
                     mc.player
                        .connection
                        .sendPacket(new Position(mc.player.posX, yposition, mc.player.posZ, false));
                     if (this.update.getValue()) {
                        mc.player.setPosition(mc.player.posX, yposition, mc.player.posZ);
                     }

                     if (this.bypass.getValue()) {
                        mc.player
                           .connection
                           .sendPacket(
                              new Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, true)
                           );
                     }
                  }
               }
         }
      }
   );

   @Override
   public void onEnable() {
      this.startFlat = mc.player.posY;
      this.num = 0;
   }
}
