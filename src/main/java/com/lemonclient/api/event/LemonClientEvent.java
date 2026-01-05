package com.lemonclient.api.event;

import me.zero.alpine.event.type.Cancellable;
import net.minecraft.client.Minecraft;

public class LemonClientEvent extends Cancellable {
   private final LemonClientEvent.Era era = LemonClientEvent.Era.PRE;
   private final float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

   public LemonClientEvent.Era getEra() {
      return this.era;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public static enum Era {
      PRE,
      PERI,
      POST;
   }
}
