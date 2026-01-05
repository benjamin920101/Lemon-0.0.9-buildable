package com.lemonclient.mixin.mixins;

import com.lemonclient.api.event.events.BlockResetEvent;
import com.lemonclient.api.event.events.DamageBlockEvent;
import com.lemonclient.api.event.events.DestroyBlockEvent;
import com.lemonclient.api.event.events.EventPlayerOnStoppedUsingItem;
import com.lemonclient.api.event.events.ReachDistanceEvent;
import com.lemonclient.api.util.player.Locks;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketUtils;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
   @Shadow
   protected abstract void syncCurrentPlayItem();

   @Redirect(method = "updateController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
   public void syncCurrentPlayItemHook(PlayerControllerMP playerControllerMP) {
      Locks.acquire(Locks.PLACE_SWITCH_LOCK, this::syncCurrentPlayItem);
   }

   @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
   private void resetBlockWrapper(CallbackInfo callbackInfo) {
      BlockResetEvent uwu = new BlockResetEvent();
      LemonClient.EVENT_BUS.post(uwu);
      if (uwu.isCancelled()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
   public void onPlayerDestroyBlock(EntityPlayer playerIn, CallbackInfo info) {
      EventPlayerOnStoppedUsingItem event = new EventPlayerOnStoppedUsingItem();
      LemonClient.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(method = "onPlayerDamageBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", at = @At("HEAD"), cancellable = true)
   private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      DamageBlockEvent event = new DamageBlockEvent(posBlock, directionFacing);
      LemonClient.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }

   @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
   private void clickBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
      DamageBlockEvent event = new DamageBlockEvent(pos, face);
      LemonClient.EVENT_BUS.post(event);
   }

   @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
   private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
      DamageBlockEvent event = new DamageBlockEvent(pos, face);
      LemonClient.EVENT_BUS.post(event);
   }

   @Inject(
      method = "onPlayerDestroyBlock",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"),
      cancellable = true
   )
   private void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      LemonClient.EVENT_BUS.post(new DestroyBlockEvent(pos));
   }

   @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
   private void getReachDistanceHook(CallbackInfoReturnable<Float> distance) {
      ReachDistanceEvent reachDistanceEvent = new ReachDistanceEvent(distance.getReturnValue());
      LemonClient.EVENT_BUS.post(reachDistanceEvent);
      distance.setReturnValue(reachDistanceEvent.getDistance());
   }

   @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
   public void onStoppedUsingItem(EntityPlayer playerIn, CallbackInfo ci) {
      PacketUtils packetUtils = ModuleManager.getModule(PacketUtils.class);
      if (packetUtils.isEnabled()
         && packetUtils.packetUse.getValue()
         && (
            packetUtils.food.getValue() && playerIn.getHeldItem(playerIn.getActiveHand()).getItem() instanceof ItemFood
               || packetUtils.potion.getValue() && playerIn.getHeldItem(playerIn.getActiveHand()).getItem() instanceof ItemPotion
               || packetUtils.all.getValue()
         )) {
         this.syncCurrentPlayItem();
         if (packetUtils.cancel.getValue()) {
            playerIn.stopActiveHand();
         }

         ci.cancel();
      }
   }
}
