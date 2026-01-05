package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "FastFall", category = Category.Movement)
public class FastFall extends Module {
   DoubleSetting dist = this.registerDouble("Min Distance", 3.0, 0.0, 25.0);
   DoubleSetting speed = this.registerDouble("Multiplier", 3.0, 0.0, 10.0);

   @Override
   public void onUpdate() {
      if (mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()))
         && mc.player.onGround
         && (!mc.player.isElytraFlying() || mc.player.fallDistance < this.dist.getValue() || !mc.player.capabilities.isFlying)) {
         mc.player.motionY = mc.player.motionY - this.speed.getValue();
      }
   }
}
