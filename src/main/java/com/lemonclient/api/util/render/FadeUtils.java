package com.lemonclient.api.util.render;

public class FadeUtils {
   protected long start;
   protected long length;

   public FadeUtils(long ms) {
      this.length = ms;
      this.reset();
   }

   public void reset() {
      this.start = System.currentTimeMillis();
   }

   public boolean isEnd() {
      return this.getTime() >= this.length;
   }

   public FadeUtils end() {
      this.start = System.currentTimeMillis() - this.length;
      return this;
   }

   protected long getTime() {
      return System.currentTimeMillis() - this.start;
   }

   public void setLength(long length) {
      this.length = length;
   }

   public long getLength() {
      return this.length;
   }

   public double getFadeOne() {
      return this.isEnd() ? 1.0 : (double)this.getTime() / this.length;
   }

   public double toDelta() {
      double value = (double)this.toDelta(this.start) / this.length;
      if (value > 1.0) {
         value = 1.0;
      }

      if (value < 0.0) {
         value = 0.0;
      }

      return value;
   }

   public long toDelta(long start) {
      return System.currentTimeMillis() - start;
   }

   public double getFade(String fadeMode) {
      return getFade(fadeMode, this.getFadeOne());
   }

   public static double getFade(String fadeMode, double current) {
      switch (fadeMode) {
         case "FADE_IN":
            return getFadeInDefault(current);
         case "FADE_OUT":
            return getFadeOutDefault(current);
         case "FADE_EPS_IN":
            return getEpsEzFadeIn(current);
         case "FADE_EPS_OUT":
            return getEpsEzFadeOut(current);
         case "FADE_EASE_IN_QUAD":
            return easeInQuad(current);
         case "FADE_EASE_OUT_QUAD":
            return easeOutQuad(current);
         default:
            return current;
      }
   }

   public static double getFadeType(String fadeType, boolean FadeIn, double current) {
      switch (fadeType) {
         case "FADE_DEFAULT":
            return FadeIn ? getFadeInDefault(current) : getFadeOutDefault(current);
         case "FADE_EPS":
            return FadeIn ? getEpsEzFadeIn(current) : getEpsEzFadeOut(current);
         case "FADE_EASE_QUAD":
            return FadeIn ? easeInQuad(current) : easeOutQuad(current);
         default:
            return FadeIn ? current : 1.0 - current;
      }
   }

   private static double checkOne(double one) {
      return Math.max(0.0, Math.min(1.0, one));
   }

   public static double getFadeInDefault(double current) {
      return Math.tanh(checkOne(current) * 3.0);
   }

   public static double getFadeOutDefault(double current) {
      return 1.0 - getFadeInDefault(current);
   }

   public static double getEpsEzFadeIn(double current) {
      return 1.0 - getEpsEzFadeOut(current);
   }

   public static double getEpsEzFadeOut(double current) {
      return Math.cos((Math.PI / 2) * checkOne(current)) * Math.cos((Math.PI * 4.0 / 5.0) * checkOne(current));
   }

   public static double easeOutQuad(double current) {
      return 1.0 - easeInQuad(current);
   }

   public static double easeInQuad(double current) {
      return checkOne(current) * checkOne(current);
   }

   public double getEpsEzFadeInGUI() {
      return this.isEnd() ? 1.0 : Math.sin(this.getFadeOne());
   }
}
