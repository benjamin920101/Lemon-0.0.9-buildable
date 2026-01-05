package com.lemonclient.mixin.mixins;

import com.lemonclient.api.util.player.Locks;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeHooks.class)
public abstract class MixinForgeHooks {
   @Redirect(method = "onPickBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I", ordinal = 1))
   private static void onPickBlockHook(InventoryPlayer inventoryPlayer, int value) {
      Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> inventoryPlayer.currentItem = value);
   }
}
