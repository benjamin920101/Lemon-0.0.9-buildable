package com.lemonclient.client.module.modules.hud;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

@Module.Declaration(name = "TextRadar", category = Category.HUD, drawn = false)
@HUDModule.Declaration(posX = 0, posZ = 50)
public class TextRadar extends HUDModule {
   ModeSetting display = this.registerMode("Display", Arrays.asList("All", "Friend", "Enemy"), "All");
   BooleanSetting sortUp = this.registerBoolean("Sort Up", false);
   BooleanSetting sortRight = this.registerBoolean("Sort Right", false);
   IntegerSetting range = this.registerInteger("Range", 100, 1, 260);
   private final TextRadar.PlayerList list = new TextRadar.PlayerList();

   @Override
   public void populate(ITheme theme) {
      this.component = new ListComponent(new Labeled(this.getName(), null, () -> true), this.position, this.getName(), this.list, 9, 1);
   }

   @Override
   public void onRender() {
      this.list.players.clear();
      mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).filter(e -> e != mc.player).forEach(e -> {
         if (!(mc.player.getDistance(e) > this.range.getValue().intValue())) {
            if (!this.display.getValue().equalsIgnoreCase("Friend") || SocialManager.isFriend(e.getName())) {
               if (!this.display.getValue().equalsIgnoreCase("Enemy") || SocialManager.isEnemy(e.getName())) {
                  this.list.players.add((EntityPlayer)e);
               }
            }
         }
      });
   }

   private class PlayerList implements HUDList {
      public List<EntityPlayer> players = new ArrayList<>();

      private PlayerList() {
      }

      @Override
      public int getSize() {
         return this.players.size();
      }

      @Override
      public String getItem(int index) {
         EntityPlayer e = this.players.get(index);
         TextFormatting friendcolor;
         if (SocialManager.isFriend(e.getName())) {
            friendcolor = ModuleManager.getModule(ColorMain.class).getFriendColor();
         } else if (SocialManager.isEnemy(e.getName())) {
            friendcolor = ModuleManager.getModule(ColorMain.class).getEnemyColor();
         } else {
            friendcolor = TextFormatting.GRAY;
         }

         float health = e.getHealth() + e.getAbsorptionAmount();
         TextFormatting healthcolor;
         if (health <= 5.0F) {
            healthcolor = TextFormatting.RED;
         } else if (health > 5.0F && health < 15.0F) {
            healthcolor = TextFormatting.YELLOW;
         } else {
            healthcolor = TextFormatting.GREEN;
         }

         float distance = TextRadar.mc.player.getDistance(e);
         TextFormatting distancecolor;
         if (distance < 20.0F) {
            distancecolor = TextFormatting.RED;
         } else if (distance >= 20.0F && distance < 50.0F) {
            distancecolor = TextFormatting.YELLOW;
         } else {
            distancecolor = TextFormatting.GREEN;
         }

         return TextFormatting.GRAY
            + "["
            + healthcolor
            + (int)health
            + TextFormatting.GRAY
            + "] "
            + friendcolor
            + e.getName()
            + TextFormatting.GRAY
            + " ["
            + distancecolor
            + (int)distance
            + TextFormatting.GRAY
            + "]";
      }

      @Override
      public Color getItemColor(int index) {
         return new Color(255, 255, 255);
      }

      @Override
      public boolean sortUp() {
         return TextRadar.this.sortUp.getValue();
      }

      @Override
      public boolean sortRight() {
         return TextRadar.this.sortRight.getValue();
      }
   }
}
