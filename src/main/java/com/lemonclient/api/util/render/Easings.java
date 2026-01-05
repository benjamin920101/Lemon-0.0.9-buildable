package com.lemonclient.api.util.render;

public interface Easings {
   String[] easings = new String[]{"none", "cubic", "quint", "quad", "quart", "expo", "sine", "circ"};

   static double toOutEasing(String easing, double value) {
      switch (easing) {
         case "cubic":
            return cubicOut(value);
         case "quint":
            return quintOut(value);
         case "quad":
            return quadOut(value);
         case "quart":
            return quartOut(value);
         case "expo":
            return expoOut(value);
         case "sine":
            return sineOut(value);
         case "circ":
            return circOut(value);
         default:
            return value;
      }
   }

   static double toInEasing(String easing, double value) {
      switch (easing) {
         case "cubic":
            return cubicIn(value);
         case "quint":
            return quintIn(value);
         case "quad":
            return quadIn(value);
         case "quart":
            return quartIn(value);
         case "expo":
            return expoIn(value);
         case "sine":
            return sineIn(value);
         case "circ":
            return circIn(value);
         default:
            return value;
      }
   }

   static double inOutEasing(String easing, double value) {
      switch (easing) {
         case "cubic":
            return cubicInOut(value);
         case "quint":
            return quintInOut(value);
         case "quad":
            return quadInOut(value);
         case "quart":
            return quartInOut(value);
         case "expo":
            return expoInOut(value);
         case "sine":
            return sineInOut(value);
         case "circ":
            return circInOut(value);
         default:
            return value;
      }
   }

   static double cubicIn(double value) {
      return value * value * value;
   }

   static double cubicOut(double value) {
      return 1.0 - Math.pow(1.0 - value, 3.0);
   }

   static double cubicInOut(double value) {
      return value < 0.5 ? 4.0 * value * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 3.0) / 2.0;
   }

   static double quintIn(double value) {
      return value * value * value * value * value;
   }

   static double quintOut(double value) {
      return 1.0 - Math.pow(1.0 - value, 5.0);
   }

   static double quintInOut(double value) {
      return value < 0.5 ? 16.0 * value * value * value * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 5.0) / 2.0;
   }

   static double quadIn(double value) {
      return value * value;
   }

   static double quadOut(double value) {
      return 1.0 - (1.0 - value) * (1.0 - value);
   }

   static double quadInOut(double value) {
      return value < 0.5 ? 2.0 * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
   }

   static double quartIn(double value) {
      return value * value * value * value;
   }

   static double quartOut(double value) {
      return 1.0 - Math.pow(1.0 - value, 4.0);
   }

   static double quartInOut(double value) {
      return value < 0.5 ? 8.0 * value * value * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 4.0) / 2.0;
   }

   static double expoIn(double value) {
      return value == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * value - 10.0);
   }

   static double expoOut(double value) {
      return value == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * value);
   }

   static double expoInOut(double value) {
      return value == 0.0
         ? 0.0
         : (value == 1.0 ? 1.0 : (value < 0.5 ? Math.pow(2.0, 20.0 * value - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * value + 10.0)) / 2.0));
   }

   static double sineIn(double value) {
      return 1.0 - Math.cos(value * Math.PI / 2.0);
   }

   static double sineOut(double value) {
      return Math.sin(value * Math.PI / 2.0);
   }

   static double sineInOut(double value) {
      return -(Math.cos(Math.PI * value) - 1.0) / 2.0;
   }

   static double circIn(double value) {
      return 1.0 - Math.sqrt(1.0 - Math.pow(value, 2.0));
   }

   static double circOut(double value) {
      return Math.sqrt(1.0 - Math.pow(value - 1.0, 2.0));
   }

   static double circInOut(double value) {
      return value < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * value, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * value + 2.0, 2.0)) + 1.0) / 2.0;
   }
}
