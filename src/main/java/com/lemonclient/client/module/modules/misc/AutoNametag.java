package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;

@Module.Declaration(name = "AutoNametag", category = Category.Misc)
public class AutoNametag extends Module {
   ModeSetting modeSetting = this.registerMode("Mode", Arrays.asList("Any", "Wither"), "Wither");
   DoubleSetting range = this.registerDouble("Range", 3.5, 0.0, 10.0);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting check = this.registerBoolean("Switch Check", true);
   BooleanSetting disable = this.registerBoolean("Auto Disable", true);
   private String currentName = "";
   private int currentSlot = -1;

   @Override
   public void onUpdate() {
      this.findNameTags();
      this.useNameTag();
   }

   private void switchTo(int slot) {
      if (slot > -1 && slot < 9 && (!this.check.getValue() || mc.player.inventory.currentItem != slot)) {
         if (this.packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         } else {
            mc.player.inventory.currentItem = slot;
         }

         mc.playerController.updateController();
      }
   }

   private void useNameTag() {
      int originalSlot = mc.player.inventory.currentItem;

      for (Entity w : mc.world.getLoadedEntityList()) {
         String var4 = this.modeSetting.getValue();
         switch (var4) {
            case "Wither":
               if (w instanceof EntityWither
                  && !w.getDisplayName().getUnformattedText().equals(this.currentName)
                  && mc.player.getDistance(w) <= this.range.getValue()) {
                  int oldslot = mc.player.inventory.currentItem;
                  this.selectNameTags();
                  mc.playerController.interactWithEntity(mc.player, w, EnumHand.MAIN_HAND);
                  this.switchTo(oldslot);
               }
               break;
            case "Any":
               if ((w instanceof EntityMob || w instanceof EntityAnimal)
                  && !w.getDisplayName().getUnformattedText().equals(this.currentName)
                  && mc.player.getDistance(w) <= this.range.getValue()) {
                  int oldslot = mc.player.inventory.currentItem;
                  this.selectNameTags();
                  mc.playerController.interactWithEntity(mc.player, w, EnumHand.MAIN_HAND);
                  this.switchTo(oldslot);
               }
         }
      }

      mc.player.inventory.currentItem = originalSlot;
   }

   private void selectNameTags() {
      if (this.currentSlot != -1 && this.isNametag(this.currentSlot)) {
         this.switchTo(this.currentSlot);
      } else {
         if (this.disable.getValue()) {
            this.disable();
         }
      }
   }

   private void findNameTags() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock) && this.isNametag(i)) {
            this.currentName = stack.getDisplayName();
            this.currentSlot = i;
         }
      }
   }

   private boolean isNametag(int i) {
      ItemStack stack = mc.player.inventory.getStackInSlot(i);
      Item tag = stack.getItem();
      return tag instanceof ItemNameTag && !stack.getDisplayName().equals("Name Tag");
   }
}
