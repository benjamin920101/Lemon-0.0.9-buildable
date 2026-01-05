package com.lemonclient.mixin.mixins;

import com.lemonclient.api.util.player.Locks;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {
   @Redirect(method = "setPickedItemStack", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I", opcode = 181))
   public void setPickedItemStackHook(InventoryPlayer inventoryPlayer, int value) {
      Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> inventoryPlayer.currentItem = value);
   }

   @Redirect(method = "pickItem", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I", opcode = 181))
   public void pickItemHook(InventoryPlayer inventoryPlayer, int value) {
      Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> inventoryPlayer.currentItem = value);
   }
}
