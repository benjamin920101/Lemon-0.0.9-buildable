package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

@Module.Declaration(name = "KillEffect", category = Category.Misc)
public class KillEffect extends Module {
   BooleanSetting thunder = this.registerBoolean("Thunder", true);
   IntegerSetting numbersThunder = this.registerInteger("Number Thunder", 1, 1, 10);
   BooleanSetting sound = this.registerBoolean("Sound", true);
   IntegerSetting numberSound = this.registerInteger("Number Sound", 1, 1, 10);
   ArrayList<EntityPlayer> playersDead = new ArrayList<>();

   @Override
   protected void onEnable() {
      this.playersDead.clear();
   }

   @Override
   public void onUpdate() {
      if (mc.world == null) {
         this.playersDead.clear();
      } else {
         mc.world
            .playerEntities
            .forEach(
               entity -> {
                  if (this.playersDead.contains(entity)) {
                     if (entity.getHealth() > 0.0F) {
                        this.playersDead.remove(entity);
                     }
                  } else if (entity.getHealth() == 0.0F) {
                     if (this.thunder.getValue()) {
                        for (int i = 0; i < this.numbersThunder.getValue(); i++) {
                           mc.world
                              .spawnEntity(new EntityLightningBolt(mc.world, entity.posX, entity.posY, entity.posZ, true));
                        }
                     }

                     if (this.sound.getValue()) {
                        for (int i = 0; i < this.numberSound.getValue(); i++) {
                           mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5F, 1.0F);
                        }
                     }

                     this.playersDead.add(entity);
                  }
               }
            );
      }
   }
}
