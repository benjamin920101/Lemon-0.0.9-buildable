package com.lemonclient.mixin.mixins;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C00Handshake.class)
public class MixinC00Handshake {
   @Shadow
   private int protocolVersion;
   @Shadow
   private String ip;
   @Shadow
   private int port;
   @Shadow
   private EnumConnectionState requestedState;

   @Inject(method = "writePacketData", at = @At("HEAD"), cancellable = true)
   public void writePacketData(PacketBuffer buf, CallbackInfo info) {
      info.cancel();
      buf.writeVarInt(this.protocolVersion);
      buf.writeString(this.ip);
      buf.writeShort(this.port);
      buf.writeVarInt(this.requestedState.getId());
   }
}
