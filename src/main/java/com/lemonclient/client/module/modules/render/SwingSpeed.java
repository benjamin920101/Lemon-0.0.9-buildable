package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;

@Module.Declaration(name = "SwingSpeed", category = Category.Render)
public class SwingSpeed extends Module {
   public static SwingSpeed INSTANCE;
   public IntegerSetting speed = this.registerInteger("Speed", 6, 1, 50);

   public SwingSpeed() {
      INSTANCE = this;
   }
}
