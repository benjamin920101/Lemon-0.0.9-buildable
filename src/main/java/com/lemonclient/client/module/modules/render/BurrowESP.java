package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "BurrowESP", category = Category.Render)
public class BurrowESP extends Module {
   BooleanSetting self = this.registerBoolean("Self", true);
   ColorSetting selfColor = this.registerColor("Self Color", new GSColor(0, 255, 0, 50));
   BooleanSetting friend = this.registerBoolean("Friend", true);
   ColorSetting friendColor = this.registerColor("Friend Color", new GSColor(0, 0, 255, 50));
   BooleanSetting enemy = this.registerBoolean("Enemy", true);
   ColorSetting enemyColor = this.registerColor("Enemy Color", new GSColor(255, 0, 0));
   IntegerSetting ufoAlpha = this.registerInteger("Alpha", 120, 0, 255);
   IntegerSetting Alpha = this.registerInteger("Outline Alpha", 255, 0, 255);

   @Override
   public void onWorldRender(RenderEvent event) {
      for (Entity entity : mc.world.playerEntities) {
         BlockPos pos = EntityUtil.getEntityPos(entity);
         if (BlockUtil.getBlock(pos) != Blocks.AIR) {
            String name = entity.getName();
            if (entity == mc.player) {
               if (this.self.getValue()) {
                  RenderUtil.drawBox(pos, 1.0, new GSColor(this.selfColor.getValue(), this.ufoAlpha.getValue()), 63);
                  RenderUtil.drawBoundingBox(pos, 1.0, 1.0F, new GSColor(this.selfColor.getValue(), this.Alpha.getValue()));
               }
            } else if (SocialManager.isFriend(name)) {
               if (this.friend.getValue()) {
                  RenderUtil.drawBox(pos, 1.0, new GSColor(this.friendColor.getValue(), this.ufoAlpha.getValue()), 63);
                  RenderUtil.drawBoundingBox(pos, 1.0, 1.0F, new GSColor(this.friendColor.getValue(), this.Alpha.getValue()));
               }
            } else if (this.enemy.getValue()) {
               RenderUtil.drawBox(pos, 1.0, new GSColor(this.enemyColor.getValue(), this.ufoAlpha.getValue()), 63);
               RenderUtil.drawBoundingBox(pos, 1.0, 1.0F, new GSColor(this.enemyColor.getValue(), this.Alpha.getValue()));
            }
         }
      }
   }
}
