package com.lemonclient.api.util.misc;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.client.Minecraft;

public class ServerUtil {
   static Minecraft mc = Minecraft.getMinecraft();
   private static String serverBrand;
   private static Timing timer;
   private static float[] tpsCounts;
   private static long lastUpdate;
   private static String format = "%.3f";
   private static float TPS;

   public static float getTpsFactor() {
      return 20.0F / TPS;
   }

   public static long serverRespondingTime() {
      return timer.getPassedTimeMs();
   }

   public static String getServerBrand() {
      return serverBrand;
   }

   public boolean isServerNotResponding() {
      return timer.passedMs(500L);
   }

   public ServerUtil() {
      tpsCounts = new float[10];
      format = "%.3f";
      timer = new Timing();
      TPS = 20.0F;
      lastUpdate = -1L;
      serverBrand = "";
   }

   public void update() {
      long currentTimeMillis = System.currentTimeMillis();
      if (lastUpdate == -1L) {
         lastUpdate = currentTimeMillis;
      } else {
         float n = (float)(currentTimeMillis - lastUpdate) / 20.0F;
         if (n == 0.0F) {
            n = 50.0F;
         }

         float n2;
         if ((n2 = 1000.0F / n) > 20.0F) {
            n2 = 20.0F;
         }

         System.arraycopy(ServerUtil.tpsCounts, 0, ServerUtil.tpsCounts, 1, ServerUtil.tpsCounts.length - 1);
         ServerUtil.tpsCounts[0] = n2;
         double n3 = 0.0;
         float[] tpsCounts = ServerUtil.tpsCounts;
         int length = tpsCounts.length;

         for (int i = 0; i < length; i++) {
            n3 += tpsCounts[i];
         }

         double number;
         if ((number = n3 / tpsCounts.length) > 20.0) {
            number = 20.0;
         }

         TPS = Float.parseFloat(String.format(format, number));
         lastUpdate = currentTimeMillis;
      }
   }

   public static int getPing() {
      if (mc.world != null && mc.player != null) {
         try {
            return Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.getConnection().getGameProfile().getId()).getResponseTime();
         } catch (Exception var1) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static float getTPS() {
      return TPS;
   }

   public void setServerBrand(String serverBrand) {
   }

   public static void reset() {
      Arrays.fill(tpsCounts, 20.0F);
      TPS = 20.0F;
   }

   public static void onPacketReceived() {
      timer.reset();
   }
}
