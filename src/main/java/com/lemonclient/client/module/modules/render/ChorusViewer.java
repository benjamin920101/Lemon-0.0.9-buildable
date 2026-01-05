package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "ChorusViewer", category = Category.Render)
public class ChorusViewer extends Module {
   ModeSetting render = this.registerMode("Render", Arrays.asList("None", "Rectangle", "Circle"), "None");
   IntegerSetting life = this.registerInteger("Life", 300, 0, 1000);
   DoubleSetting circleRange = this.registerDouble("Circle Range", 1.0, 0.0, 3.0);
   ColorSetting color = this.registerColor("Color", new GSColor(255, 255, 255, 150), true);
   BooleanSetting desyncCircle = this.registerBoolean("Desync Circle", false);
   IntegerSetting stepRainbowCircle = this.registerInteger("Step Rainbow Circle", 1, 1, 100);
   BooleanSetting increaseHeight = this.registerBoolean("Increase Height", true);
   DoubleSetting speedIncrease = this.registerDouble("Speed Increase", 0.01, 0.3, 0.001);
   ArrayList<ChorusViewer.renderClass> toRender = new ArrayList<>();
   @EventHandler
   private final Listener<PacketEvent.Receive> sendListener = new Listener<>(
      event -> {
         if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect soundPacket = (SPacketSoundEffect)event.getPacket();
            if (soundPacket.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) {
               this.toRender
                  .add(
                     new ChorusViewer.renderClass(
                        new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()),
                        this.life.getValue().intValue(),
                        this.render.getValue(),
                        this.color.getValue(),
                        this.circleRange.getValue(),
                        this.desyncCircle.getValue(),
                        this.stepRainbowCircle.getValue(),
                        this.circleRange.getValue(),
                        this.stepRainbowCircle.getValue(),
                        this.increaseHeight.getValue(),
                        this.speedIncrease.getValue()
                     )
                  );
            }
         }
      }
   );

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         for (int i = 0; i < this.toRender.size(); i++) {
            if (this.toRender.get(i).update()) {
               this.toRender.remove(i);
               i--;
            }
         }

         this.toRender.forEach(ChorusViewer.renderClass::render);
      }
   }

   static class renderClass {
      final Vec3d center;
      long start;
      final long life;
      final String mode;
      final double circleRange;
      final GSColor color;
      final boolean desyncCircle;
      final int stepRainbowCircle;
      final double range;
      final int desync;
      final boolean increaseHeight;
      final double speedIncrease;
      double nowHeigth = 0.0;
      boolean up = true;

      public renderClass(
         Vec3d center,
         long life,
         String mode,
         GSColor color,
         double circleRange,
         boolean desyncCircle,
         int stepRainbowCircle,
         double range,
         int desync,
         boolean increaseHeight,
         double speedIncrease
      ) {
         this.center = center;
         this.increaseHeight = increaseHeight;
         this.speedIncrease = speedIncrease;
         this.range = range;
         this.start = System.currentTimeMillis();
         this.life = life;
         this.mode = mode;
         this.desync = desync;
         this.circleRange = circleRange;
         this.color = color;
         this.desyncCircle = desyncCircle;
         this.stepRainbowCircle = stepRainbowCircle;
      }

      boolean update() {
         return System.currentTimeMillis() - this.start > this.life;
      }

      void render() {
         String var1 = this.mode;
         switch (var1) {
            case "Rectangle":
               RenderUtil.drawBox(new BlockPos(this.center.x, this.center.y, this.center.z), 1.8, this.color, 63);
               break;
            case "Circle":
               double inc = 0.0;
               if (this.increaseHeight) {
                  this.nowHeigth = this.nowHeigth + this.speedIncrease * (this.up ? 1 : -1);
                  if (this.nowHeigth > 1.8) {
                     this.up = false;
                  } else if (this.nowHeigth < 0.0) {
                     this.up = true;
                  }

                  inc = this.nowHeigth;
               }

               if (this.desyncCircle) {
                  RenderUtil.drawCircle(
                     (float)this.center.x,
                     (float)(this.center.y + inc),
                     (float)this.center.z,
                     this.range,
                     this.desync,
                     this.color.getAlpha()
                  );
               } else {
                  RenderUtil.drawCircle(
                     (float)this.center.x, (float)(this.center.y + inc), (float)this.center.z, this.range, this.color
                  );
               }
         }
      }
   }
}
