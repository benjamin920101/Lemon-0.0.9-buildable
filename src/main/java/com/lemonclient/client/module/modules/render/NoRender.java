package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.BossbarEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.material.Material;
import net.minecraft.init.MobEffects;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

@Module.Declaration(name = "NoRender", category = Category.Render)
public class NoRender extends Module {
   public BooleanSetting armor = this.registerBoolean("Armor", false);
   BooleanSetting fire = this.registerBoolean("Fire", false);
   BooleanSetting blind = this.registerBoolean("Blind", false);
   BooleanSetting nausea = this.registerBoolean("Nausea", false);
   public BooleanSetting hurtCam = this.registerBoolean("HurtCam", false);
   public BooleanSetting noSkylight = this.registerBoolean("Skylight", false);
   public BooleanSetting noOverlay = this.registerBoolean("No Overlay", false);
   BooleanSetting noBossBar = this.registerBoolean("No Boss Bar", false);
   public BooleanSetting nameTag = this.registerBoolean("No NameTag", false);
   public BooleanSetting noCluster = this.registerBoolean("No Cluster", false);
   IntegerSetting maxNoClusterRender = this.registerInteger("No Cluster Max", 5, 1, 25);
   public int currentClusterAmount = 0;
   @EventHandler
   public Listener<RenderBlockOverlayEvent> blockOverlayEventListener = new Listener<>(event -> {
      if (this.fire.getValue() && event.getOverlayType() == OverlayType.FIRE) {
         event.setCanceled(true);
      }

      if (this.noOverlay.getValue() && event.getOverlayType() == OverlayType.WATER) {
         event.setCanceled(true);
      }

      if (this.noOverlay.getValue() && event.getOverlayType() == OverlayType.BLOCK) {
         event.setCanceled(true);
      }
   });
   @EventHandler
   private final Listener<FogDensity> fogDensityListener = new Listener<>(
      event -> {
         if (this.noOverlay.getValue()
            && (event.getState().getMaterial().equals(Material.WATER) || event.getState().getMaterial().equals(Material.LAVA))) {
            event.setDensity(0.0F);
            event.setCanceled(true);
         }
      }
   );
   @EventHandler
   private final Listener<RenderBlockOverlayEvent> renderBlockOverlayEventListener = new Listener<>(event -> {
      if (this.noOverlay.getValue()) {
         event.setCanceled(true);
      }
   });
   @EventHandler
   private final Listener<RenderGameOverlayEvent> renderGameOverlayEventListener = new Listener<>(event -> {
      if (this.noOverlay.getValue()) {
         if (event.getType().equals(ElementType.HELMET)) {
            event.setCanceled(true);
         }

         if (event.getType().equals(ElementType.PORTAL)) {
            event.setCanceled(true);
         }
      }
   });
   @EventHandler
   private final Listener<BossbarEvent> bossbarEventListener = new Listener<>(event -> {
      if (this.noBossBar.getValue()) {
         event.cancel();
      }
   });

   @Override
   public void onUpdate() {
      if (this.blind.getValue() && mc.player.isPotionActive(MobEffects.BLINDNESS)) {
         mc.player.removePotionEffect(MobEffects.BLINDNESS);
      }

      if (this.nausea.getValue() && mc.player.isPotionActive(MobEffects.NAUSEA)) {
         mc.player.removePotionEffect(MobEffects.NAUSEA);
      }
   }

   @Override
   public void onRender() {
      this.currentClusterAmount = 0;
   }

   public boolean incrementNoClusterRender() {
      this.currentClusterAmount++;
      return this.currentClusterAmount > this.maxNoClusterRender.getValue();
   }

   public boolean getNoClusterRender() {
      return this.currentClusterAmount <= this.maxNoClusterRender.getValue();
   }
}
