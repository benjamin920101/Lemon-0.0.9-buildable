package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.misc.ColorUtil;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.manager.managers.TotemPopManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

@Module.Declaration(name = "Nametags", category = Category.Render)
public class Nametags extends Module {
   IntegerSetting range = this.registerInteger("Range", 100, 10, 260);
   BooleanSetting renderSelf = this.registerBoolean("Render Self", false);
   BooleanSetting showItems = this.registerBoolean("Items", true);
   BooleanSetting showEnchantName = this.registerBoolean("Enchants", true, () -> this.showItems.getValue());
   BooleanSetting showItemName = this.registerBoolean("Item Name", false);
   BooleanSetting showDurability = this.registerBoolean("Durability", true);
   BooleanSetting showGameMode = this.registerBoolean("Gamemode", false);
   BooleanSetting showHealth = this.registerBoolean("Health", true);
   BooleanSetting showPing = this.registerBoolean("Ping", false);
   BooleanSetting showTotem = this.registerBoolean("Totem Pops", true);
   BooleanSetting showEntityID = this.registerBoolean("Entity Id", false);
   ModeSetting levelColor = this.registerMode("Level Color", ColorUtil.colors, "Green");
   public BooleanSetting border = this.registerBoolean("Border", false);
   public BooleanSetting outline = this.registerBoolean("Outline", false);
   public BooleanSetting customColor = this.registerBoolean("Custom Color", true, () -> this.outline.getValue());
   public ColorSetting borderColor = this.registerColor(
      "Border Color", new GSColor(255, 0, 0, 255), () -> this.outline.getValue() && this.customColor.getValue()
   );

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         mc.world.playerEntities.stream().filter(this::shouldRender).forEach(entityPlayer -> {
            Vec3d vec3d = this.findEntityVec3d(entityPlayer);
            this.renderNameTags(entityPlayer, vec3d.x, vec3d.y, vec3d.z);
         });
      }
   }

   private boolean shouldRender(EntityPlayer entityPlayer) {
      if (entityPlayer == mc.player && !this.renderSelf.getValue()) {
         Entity player = mc.getRenderViewEntity();
         if (player == null) {
            player = mc.player;
         }

         if (player == mc.player) {
            return false;
         }
      }

      return EntityUtil.isDead(entityPlayer) ? false : !(entityPlayer.getDistance(mc.player) > this.range.getValue().intValue());
   }

   private Vec3d findEntityVec3d(EntityPlayer entityPlayer) {
      double posX = this.balancePosition(entityPlayer.posX, entityPlayer.lastTickPosX);
      double posY = this.balancePosition(entityPlayer.posY, entityPlayer.lastTickPosY);
      double posZ = this.balancePosition(entityPlayer.posZ, entityPlayer.lastTickPosZ);
      return new Vec3d(posX, posY, posZ);
   }

   private double balancePosition(double newPosition, double oldPosition) {
      return oldPosition + (newPosition - oldPosition) * mc.timer.renderPartialTicks;
   }

   private void renderNameTags(EntityPlayer entityPlayer, double posX, double posY, double posZ) {
      double adjustedY = posY + (entityPlayer.isSneaking() ? 1.9 : 2.1);
      String[] name = new String[]{this.buildEntityNameString(entityPlayer)};
      RenderUtil.drawNametag(posX, adjustedY, posZ, name, this.findTextColor(entityPlayer), 2, 0.0, 0.0);
      this.renderItemsAndArmor(entityPlayer, 0, 0);
      GlStateManager.popMatrix();
   }

   private String buildEntityNameString(EntityPlayer entityPlayer) {
      String name = entityPlayer.getName();
      if (this.showEntityID.getValue()) {
         name = name + " ID: " + entityPlayer.getEntityId();
      }

      if (this.showGameMode.getValue()) {
         if (entityPlayer.isCreative()) {
            name = name + " [C]";
         } else if (entityPlayer.isSpectator()) {
            name = name + " [I]";
         } else {
            name = name + " [S]";
         }
      }

      if (this.showTotem.getValue()) {
         name = name + " [" + TotemPopManager.INSTANCE.getPlayerPopCount(entityPlayer.getName()) + "]";
      }

      if (this.showPing.getValue()) {
         int value = 0;
         if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()) != null) {
            value = mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()).getResponseTime();
         }

         name = name + " " + value + "ms";
      }

      if (this.showHealth.getValue()) {
         int health = (int)(entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount());
         TextFormatting textFormatting = this.findHealthColor(health);
         name = name + " " + textFormatting + health;
      }

      return name;
   }

   private TextFormatting findHealthColor(int health) {
      if (health <= 0) {
         return TextFormatting.DARK_RED;
      } else if (health <= 5) {
         return TextFormatting.RED;
      } else if (health <= 10) {
         return TextFormatting.GOLD;
      } else if (health <= 15) {
         return TextFormatting.YELLOW;
      } else {
         return health <= 20 ? TextFormatting.DARK_GREEN : TextFormatting.GREEN;
      }
   }

   private GSColor findTextColor(EntityPlayer entityPlayer) {
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      if (SocialManager.isFriend(entityPlayer.getName())) {
         return colorMain.getFriendGSColor();
      } else if (SocialManager.isEnemy(entityPlayer.getName())) {
         return colorMain.getEnemyGSColor();
      } else if (entityPlayer.isInvisible()) {
         return new GSColor(128, 128, 128);
      } else if (entityPlayer.isSneaking()) {
         return new GSColor(255, 153, 0);
      } else {
         return mc.getConnection() != null && mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()) == null
            ? new GSColor(239, 1, 71)
            : new GSColor(255, 255, 255);
      }
   }

   private void renderItemsAndArmor(EntityPlayer entityPlayer, int posX, int posY) {
      ItemStack mainHandItem = entityPlayer.getHeldItemMainhand();
      ItemStack offHandItem = entityPlayer.getHeldItemOffhand();
      int armorCount = 3;

      for (int i = 0; i <= 3; i++) {
         ItemStack itemStack = (ItemStack)entityPlayer.inventory.armorInventory.get(armorCount);
         if (!itemStack.isEmpty()) {
            posX -= 8;
            int size = EnchantmentHelper.getEnchantments(itemStack).size();
            if (this.showItems.getValue() && size > posY) {
               posY = size;
            }
         }

         armorCount--;
      }

      if (!mainHandItem.isEmpty() && (this.showItems.getValue() || this.showDurability.getValue() && offHandItem.isItemStackDamageable())) {
         posX -= 8;
         int enchantSize = EnchantmentHelper.getEnchantments(offHandItem).size();
         if (this.showItems.getValue() && enchantSize > posY) {
            posY = enchantSize;
         }
      }

      if (!mainHandItem.isEmpty()) {
         int enchantSize = EnchantmentHelper.getEnchantments(mainHandItem).size();
         if (this.showItems.getValue() && enchantSize > posY) {
            posY = enchantSize;
         }

         int armorY = this.findArmorY(posY);
         if (this.showItems.getValue() || this.showDurability.getValue() && mainHandItem.isItemStackDamageable()) {
            posX -= 8;
         }

         if (this.showItems.getValue()) {
            this.renderItem(mainHandItem, posX, armorY, posY);
            armorY -= 32;
         }

         if (this.showDurability.getValue() && mainHandItem.isItemStackDamageable()) {
            this.renderItemDurability(mainHandItem, posX, armorY);
         }

         ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
         armorY -= colorMain.customFont.getValue() ? FontUtil.getFontHeight(colorMain.customFont.getValue()) : mc.fontRenderer.FONT_HEIGHT;
         if (this.showItemName.getValue()) {
            this.renderItemName(mainHandItem, armorY);
         }

         if (this.showItems.getValue() || this.showDurability.getValue() && mainHandItem.isItemStackDamageable()) {
            posX += 16;
         }
      }

      int armorCount2 = 3;

      for (int i = 0; i <= 3; i++) {
         ItemStack itemStack = (ItemStack)entityPlayer.inventory.armorInventory.get(armorCount2);
         if (!itemStack.isEmpty()) {
            int armorYx = this.findArmorY(posY);
            if (this.showItems.getValue()) {
               this.renderItem(itemStack, posX, armorYx, posY);
               armorYx -= 32;
            }

            if (this.showDurability.getValue() && itemStack.isItemStackDamageable()) {
               this.renderItemDurability(itemStack, posX, armorYx);
            }

            posX += 16;
         }

         armorCount2--;
      }

      if (!offHandItem.isEmpty()) {
         int armorYxx = this.findArmorY(posY);
         if (this.showItems.getValue()) {
            this.renderItem(offHandItem, posX, armorYxx, posY);
            armorYxx -= 32;
         }

         if (this.showDurability.getValue() && offHandItem.isItemStackDamageable()) {
            this.renderItemDurability(offHandItem, posX, armorYxx);
         }
      }
   }

   private int findArmorY(int posY) {
      int posY2 = this.showItems.getValue() ? -26 : -27;
      if (posY > 4) {
         posY2 -= (posY - 4) * 8;
      }

      return posY2;
   }

   private void renderItemName(ItemStack itemStack, int posY) {
      GlStateManager.enableTexture2D();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 0.5);
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      FontUtil.drawStringWithShadow(
         colorMain.customFont.getValue(),
         itemStack.getDisplayName(),
         -FontUtil.getStringWidth(colorMain.customFont.getValue(), itemStack.getDisplayName()) / 2,
         posY,
         new GSColor(255, 255, 255)
      );
      GlStateManager.popMatrix();
      GlStateManager.disableTexture2D();
   }

   private void renderItemDurability(ItemStack itemStack, int posX, int posY) {
      float damagePercent = (float)(itemStack.getMaxDamage() - itemStack.getItemDamage()) / itemStack.getMaxDamage();
      float green = damagePercent;
      if (damagePercent > 1.0F) {
         green = 1.0F;
      } else if (damagePercent < 0.0F) {
         green = 0.0F;
      }

      float red = 1.0F - green;
      GlStateManager.enableTexture2D();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 0.5);
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      FontUtil.drawStringWithShadow(
         colorMain.customFont.getValue(), (int)(damagePercent * 100.0F) + "%", posX * 2, posY, new GSColor((int)(red * 255.0F), (int)(green * 255.0F), 0)
      );
      GlStateManager.popMatrix();
      GlStateManager.disableTexture2D();
   }

   private void renderItem(ItemStack itemStack, int posX, int posY, int posY2) {
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      GlStateManager.enableDepth();
      GlStateManager.disableAlpha();
      int posY3 = posY2 > 4 ? (posY2 - 4) * 8 / 2 : 0;
      mc.getRenderItem().zLevel = -150.0F;
      RenderHelper.enableStandardItemLighting();
      mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, posX, posY + posY3);
      mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, posX, posY + posY3);
      RenderHelper.disableStandardItemLighting();
      mc.getRenderItem().zLevel = 0.0F;
      RenderUtil.prepare();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 0.5);
      this.renderEnchants(itemStack, posX, posY - 24);
      GlStateManager.popMatrix();
   }

   private void renderEnchants(ItemStack itemStack, int posX, int posY) {
      GlStateManager.enableTexture2D();

      for (Enchantment enchantment : EnchantmentHelper.getEnchantments(itemStack).keySet()) {
         if (enchantment != null) {
            if (this.showEnchantName.getValue()) {
               int level = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);
               ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
               FontUtil.drawStringWithShadow(
                  colorMain.customFont.getValue(), this.findStringForEnchants(enchantment, level), posX * 2, posY, new GSColor(255, 255, 255)
               );
            }

            posY += 8;
         }
      }

      if (itemStack.getItem().equals(Items.GOLDEN_APPLE) && itemStack.hasEffect()) {
         ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
         FontUtil.drawStringWithShadow(colorMain.customFont.getValue(), "God", posX * 2, posY, new GSColor(195, 77, 65));
      }

      GlStateManager.disableTexture2D();
   }

   private String findStringForEnchants(Enchantment enchantment, int level) {
      ResourceLocation resourceLocation = (ResourceLocation)Enchantment.REGISTRY.getNameForObject(enchantment);
      String string = resourceLocation == null ? enchantment.getName() : resourceLocation.toString();
      int charCount = level > 1 ? 12 : 13;
      if (string.length() > charCount) {
         string = string.substring(10, charCount);
      }

      return string.substring(0, 1).toUpperCase() + string.substring(1) + ColorUtil.settingToTextFormatting(this.levelColor) + (level > 1 ? level : "");
   }
}
