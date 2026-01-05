package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@Module.Declaration(name = "AutoDrop", category = Category.Combat)
public class AutoDrop extends Module {
   IntegerSetting delay = this.registerInteger("Drop Delay", 10, 0, 20);
   ModeSetting mode = this.registerMode("Sharpness", Arrays.asList("Sharp5", "Sharp32k", "Both"), "Both");
   private final Timing timer = new Timing();

   @Override
   public void onUpdate() {
      String var1 = this.mode.getValue();
      switch (var1) {
         case "Sharp32k":
            if (this.isSuperWeapon(mc.player.getHeldItemMainhand()) && this.timer.passedDs(this.delay.getValue().intValue())) {
               boolean holding32k = false;
               mc.player.dropItem(!holding32k);
               this.timer.reset();
               break;
            }
         case "Both":
            if (this.checkSword(mc.player.getHeldItemMainhand()) && this.timer.passedDs(this.delay.getValue().intValue())) {
               boolean holding = false;
               mc.player.dropItem(!holding);
            }
         case "Sharp5":
            if (this.checkSharpness5(mc.player.getHeldItemMainhand()) && this.timer.passedDs(this.delay.getValue().intValue())) {
               boolean holding5 = false;
               mc.player.dropItem(!holding5);
            }
      }
   }

   private boolean checkSword(ItemStack stack) {
      if (stack.getTagCompound() == null) {
         return false;
      } else if (stack.getEnchantmentTagList().getTagType() == 0) {
         return false;
      } else {
         NBTTagList enchants = (NBTTagList)stack.getTagCompound().getTag("ench");

         for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
               int lvl = enchant.getInteger("lvl");
               if (lvl > 4) {
                  return true;
               }
               break;
            }
         }

         return false;
      }
   }

   private boolean isSuperWeapon(ItemStack item) {
      if (item == null) {
         return false;
      } else if (item.getTagCompound() == null) {
         return false;
      } else if (item.getEnchantmentTagList().getTagType() == 0) {
         return false;
      } else {
         NBTTagList enchants = (NBTTagList)item.getTagCompound().getTag("ench");

         for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
               int lvl = enchant.getInteger("lvl");
               if (lvl >= 16) {
                  return true;
               }
               break;
            }
         }

         return false;
      }
   }

   private boolean checkSharpness5(ItemStack stack) {
      if (stack.getTagCompound() == null) {
         return false;
      } else if (stack.getEnchantmentTagList().getTagType() == 0) {
         return false;
      } else {
         NBTTagList enchants = (NBTTagList)stack.getTagCompound().getTag("ench");

         for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
               int lvl = enchant.getInteger("lvl");
               if (lvl == 5) {
                  return true;
               }
               break;
            }
         }

         return false;
      }
   }
}
