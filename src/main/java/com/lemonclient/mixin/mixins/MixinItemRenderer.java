package com.lemonclient.mixin.mixins;

import com.lemonclient.api.event.events.TransformSideFirstPersonEvent;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.render.NoRender;
import com.lemonclient.client.module.modules.render.RenderTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
   @Final
   @Shadow
   public Minecraft mc;

   @Inject(method = "updateEquippedItem", at = @At("RETURN"))
   public void updateEquippedItem(CallbackInfo ci) {
      RenderTweaks renderTweaks = ModuleManager.getModule(RenderTweaks.class);
      if (renderTweaks.isEnabled() && renderTweaks.noAnimation.getValue()) {
         this.mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0F;
         this.mc.entityRenderer.itemRenderer.itemStackMainHand = this.mc.player.getHeldItemMainhand();
         this.mc.entityRenderer.itemRenderer.equippedProgressOffHand = 1.0F;
         this.mc.entityRenderer.itemRenderer.itemStackOffHand = this.mc.player.getHeldItemOffhand();
      }
   }

   @Inject(method = "resetEquippedProgress", at = @At("HEAD"), cancellable = true)
   public void resetEquippedProgress(CallbackInfo ci) {
      RenderTweaks renderTweaks = ModuleManager.getModule(RenderTweaks.class);
      if (renderTweaks.isEnabled() && renderTweaks.noAnimation.getValue()) {
         ci.cancel();
      }
   }

   @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
   public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo callbackInfo) {
      TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
      LemonClient.EVENT_BUS.post(event);
   }

   @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
   public void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo callbackInfo) {
      TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
      LemonClient.EVENT_BUS.post(event);
      RenderTweaks renderTweaks = ModuleManager.getModule(RenderTweaks.class);
      if (renderTweaks.isEnabled() && renderTweaks.noEat.getValue()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "transformFirstPerson", at = @At("HEAD"))
   public void transformFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo callbackInfo) {
      TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
      LemonClient.EVENT_BUS.post(event);
   }

   @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
   public void renderOverlays(float partialTicks, CallbackInfo callbackInfo) {
      NoRender noRender = ModuleManager.getModule(NoRender.class);
      if (noRender.isEnabled() && noRender.noOverlay.getValue()) {
         callbackInfo.cancel();
      }
   }
}
