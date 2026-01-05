package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.world.TimerUtils;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Module.Declaration(name = "Speedometer", category = Category.HUD, drawn = false)
@HUDModule.Declaration(posX = 0, posZ = 70)
public class Speedometer extends HUDModule {
   private static final String MPS = "m/s";
   private static final String KMH = "km/h";
   private static final String MPH = "mph";
   ModeSetting speedUnit = this.registerMode("Unit", Arrays.asList("m/s", "km/h", "mph"), "km/h");
   BooleanSetting averageSpeed = this.registerBoolean("Average Speed", true);
   IntegerSetting averageSpeedTicks = this.registerInteger("Average Time", 20, 5, 100);
   private final ArrayDeque<Double> speedDeque = new ArrayDeque<>();
   private String speedString = "";
   @EventHandler
   private final Listener<ClientTickEvent> listener = new Listener<>(event -> {
      if (event.phase == Phase.END) {
         EntityPlayerSP player = mc.player;
         if (player != null) {
            String unit = this.speedUnit.getValue();
            double speed = this.calcSpeed(player, unit);
            double displaySpeed = speed;
            if (this.averageSpeed.getValue()) {
               if (!(speed > 0.0) && player.ticksExisted % 4 != 0) {
                  this.speedDeque.pollFirst();
               } else {
                  this.speedDeque.add(speed);
               }

               while (!this.speedDeque.isEmpty() && this.speedDeque.size() > this.averageSpeedTicks.getValue()) {
                  this.speedDeque.poll();
               }

               displaySpeed = this.average(this.speedDeque);
            }

            this.speedString = String.format("%.2f", displaySpeed) + ' ' + unit;
         }
      }
   });

   @Override
   protected void onDisable() {
      this.speedDeque.clear();
      this.speedString = "";
   }

   private double calcSpeed(EntityPlayerSP player, String unit) {
      double tps = 1000.0 / TimerUtils.getTickLength();
      double xDiff = player.posX - player.prevPosX;
      double zDiff = player.posZ - player.prevPosZ;
      double speed = Math.hypot(xDiff, zDiff) * tps;
      switch (unit) {
         case "km/h":
            speed *= 3.6;
            break;
         case "mph":
            speed *= 2.237;
      }

      return speed;
   }

   private double average(Collection<Double> collection) {
      if (collection.isEmpty()) {
         return 0.0;
      } else {
         double sum = 0.0;
         int size = 0;

         for (double element : collection) {
            sum += element;
            size++;
         }

         return sum / size;
      }
   }

   @Override
   public void populate(ITheme theme) {
      this.component = new ListComponent(new Labeled(this.getName(), null, () -> true), this.position, this.getName(), new Speedometer.SpeedLabel(), 9, 1);
   }

   private class SpeedLabel implements HUDList {
      private SpeedLabel() {
      }

      @Override
      public int getSize() {
         return 1;
      }

      @Override
      public String getItem(int index) {
         return Speedometer.this.speedString;
      }

      @Override
      public Color getItemColor(int index) {
         return new Color(255, 255, 255);
      }

      @Override
      public boolean sortUp() {
         return false;
      }

      @Override
      public boolean sortRight() {
         return false;
      }
   }
}
