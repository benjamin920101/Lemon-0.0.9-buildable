package com.lemonclient.api.setting.values;

import com.lemonclient.api.setting.Setting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.function.Supplier;

public class ColorSetting extends Setting<GSColor> {
   private boolean rainbow = false;
   private final boolean rainbowEnabled;
   private final boolean alphaEnabled;

   public ColorSetting(String name, Module module, boolean rainbow, GSColor value) {
      super(value, name, module);
      this.rainbow = rainbow;
      this.rainbowEnabled = true;
      this.alphaEnabled = false;
   }

   public ColorSetting(String name, Module module, boolean rainbow, GSColor value, boolean alphaEnabled) {
      super(value, name, module);
      this.rainbow = rainbow;
      this.rainbowEnabled = true;
      this.alphaEnabled = alphaEnabled;
   }

   public ColorSetting(
      String name, String configName, Module module, Supplier<Boolean> isVisible, boolean rainbow, boolean rainbowEnabled, boolean alphaEnabled, GSColor value
   ) {
      super(value, name, configName, module, isVisible);
      this.rainbow = rainbow;
      this.rainbowEnabled = rainbowEnabled;
      this.alphaEnabled = alphaEnabled;
   }

   public GSColor getValue() {
      if (this.rainbow) {
         String var1 = ColorMain.INSTANCE.rainbowMode.getValue();
         switch (var1) {
            case "Sin":
               return getRainbowSin(0, 0, 1.0, 1, 1.0, 0, false);
            case "Tan":
               return getRainbowTan(0, 0, 1.0, 1, 1.0, 0, false);
            case "Sec":
               return getRainbowSec(0, 0, 1.0, 1, 1.0, 0, false);
            case "CoTan":
               return getRainbowCoTan(0, 0, 1.0, 1, 1.0, 0, false);
            case "CoSec":
               return getRainbowCoSec(0, 0, 1.0, 1, 1.0, 0, false);
            default:
               return getRainbowColor(0, 0, 0, false);
         }
      } else {
         return (GSColor)super.getValue();
      }
   }

   public static GSColor getRainbowColor(int incr, int multiply, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(((stop ? start : System.currentTimeMillis()) + incr * multiply) % 11520L)
            / 11520.0F
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public static GSColor getRainbowColor(double incr) {
      return GSColor.fromHSB((float)(incr % 11520.0 / 11520.0) * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(), 1.0F, 1.0F);
   }

   public static GSColor getRainbowSin(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(height * multiplyHeight * Math.sin(((stop ? start : System.currentTimeMillis()) + incr / millSin * multiply) % 11520.0 / 11520.0))
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public static GSColor getRainbowTan(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(height * multiplyHeight * Math.tan(((stop ? start : System.currentTimeMillis()) + incr / millSin * multiply % 11520.0) / 11520.0))
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public static GSColor getRainbowSec(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(height * multiplyHeight * (1.0 / Math.sin(((stop ? start : System.currentTimeMillis()) + incr / millSin * multiply) % 11520.0 / 11520.0)))
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public static GSColor getRainbowCoSec(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(height * multiplyHeight * (1.0 / Math.cos(((stop ? start : System.currentTimeMillis()) + incr / millSin * multiply) % 11520.0 / 11520.0)))
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public static GSColor getRainbowCoTan(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
      return GSColor.fromHSB(
         (float)(height * multiplyHeight * Math.tan(((stop ? start : System.currentTimeMillis()) + incr / millSin * multiply) % 11520.0 / 11520.0))
            * ModuleManager.getModule(ColorMain.class).rainbowSpeed.getValue().floatValue(),
         1.0F,
         1.0F
      );
   }

   public void setValue(GSColor value) {
      super.setValue(new GSColor(value));
   }

   public GSColor getColor() {
      return (GSColor)super.getValue();
   }

   public boolean getRainbow() {
      return this.rainbow;
   }

   public void setRainbow(boolean rainbow) {
      this.rainbow = rainbow;
   }

   public boolean rainbowEnabled() {
      return this.rainbowEnabled;
   }

   public boolean alphaEnabled() {
      return this.alphaEnabled;
   }

   public long toLong() {
      long temp = this.getColor().getRGB() & 16777215;
      if (this.rainbowEnabled) {
         temp += (this.rainbow ? 1 : 0) << 24;
      }

      if (this.alphaEnabled) {
         temp += (long)this.getColor().getAlpha() << 32;
      }

      return temp;
   }

   public void fromLong(long number) {
      if (this.rainbowEnabled) {
         this.rainbow = (number & 16777216L) != 0L;
      } else {
         this.rainbow = false;
      }

      this.setValue(new GSColor((int)(number & 16777215L)));
      if (this.alphaEnabled) {
         this.setValue(new GSColor(this.getColor(), (int)((number & 1095216660480L) >> 32)));
      }
   }
}
