package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.PredictUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketHeldItemChange;

@Module.Declaration(name = "Offhand", category = Category.Dev)
public class OffHand extends Module {
   public static OffHand INSTANCE;
   public boolean autoCrystal;
   ModeSetting defaultItem = this.registerMode("Default", Arrays.asList("Totem", "Crystal", "Gapple", "Plates", "Obby", "EChest", "Pot", "Exp", "Bed"), "Totem");
   ModeSetting nonDefaultItem = this.registerMode(
      "Non Default", Arrays.asList("Totem", "Crystal", "Gapple", "Obby", "EChest", "Pot", "Exp", "Plates", "String", "Skull", "Bed"), "Crystal"
   );
   ModeSetting noPlayerItem = this.registerMode(
      "No Player", Arrays.asList("Totem", "Crystal", "Gapple", "Plates", "Obby", "EChest", "Pot", "Exp", "Bed"), "Gapple"
   );
   ModeSetting swordMode = this.registerMode("Sword Switch", Arrays.asList("Gapple", "Crystal", "Pot", "None"), "Gapple");
   ModeSetting gappleMode = this.registerMode("Gap Switch", Arrays.asList("Totem", "Gapple", "Crystal", "None"), "Crystal");
   ModeSetting pickaxeMode = this.registerMode("Pick Switch", Arrays.asList("Obsidian", "EChest", "Gapple", "Crystal", "None"), "Gapple");
   ModeSetting shiftPickaxeMode = this.registerMode("Shift Pick", Arrays.asList("Obsidian", "EChest", "Gapple", "Crystal", "None"), "Gapple");
   ModeSetting potionChoose = this.registerMode("Potion", Arrays.asList("first", "strength", "swiftness"), "first");
   IntegerSetting healthSwitch = this.registerInteger("Health Switch", 14, 0, 36);
   IntegerSetting swordHealth = this.registerInteger("Sword Health", 14, 0, 36);
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 0, 0, 20);
   IntegerSetting fallDistance = this.registerInteger("Fall Distance", 12, 0, 30);
   IntegerSetting maxSwitchPerSecond = this.registerInteger("Max Switch", 6, 2, 10);
   DoubleSetting biasDamage = this.registerDouble("Bias Damage", 1.0, 0.0, 3.0);
   DoubleSetting playerDistance = this.registerDouble("Player Distance", 0.0, 0.0, 30.0);
   BooleanSetting rightGap = this.registerBoolean("Right Click Gap", false);
   BooleanSetting shiftPot = this.registerBoolean("Shift Pot", false);
   BooleanSetting swordCheck = this.registerBoolean("Only Sword", true);
   BooleanSetting crystalGap = this.registerBoolean("Crystal Gap", false);
   BooleanSetting fallDistanceBol = this.registerBoolean("Fall Distance", true);
   BooleanSetting crystalCheck = this.registerBoolean("Crystal Check", false);
   IntegerSetting predict = this.registerInteger("Predict Tick", 1, 0, 20);
   BooleanSetting noHotBar = this.registerBoolean("No HotBar", false);
   BooleanSetting onlyHotBar = this.registerBoolean("Only HotBar", false);
   BooleanSetting antiWeakness = this.registerBoolean("AntiWeakness", false);
   BooleanSetting hotBarTotem = this.registerBoolean("Switch HotBar Totem", false);
   BooleanSetting refill = this.registerBoolean("ReFill", true, () -> this.hotBarTotem.getValue());
   BooleanSetting check = this.registerBoolean("Check", true, () -> this.hotBarTotem.getValue() && this.refill.getValue());
   IntegerSetting totemSlot = this.registerInteger("Totem Slot", 1, 1, 9, () -> this.hotBarTotem.getValue() && this.refill.getValue());
   ModeSetting HudMode = this.registerMode("Hud Mode", Arrays.asList("Totem", "Offhand"), "Offhand");
   BooleanSetting debug = this.registerBoolean("Debug Msg", false);
   String ItemName;
   String itemCheck = "";
   int prevSlot;
   int tickWaited;
   int counts;
   int totems;
   boolean returnBack;
   boolean stepChanging;
   boolean firstChange;
   Item item;
   private final ArrayList<Long> switchDone = new ArrayList<>();
   Map<String, Item> allowedItemsItem = new HashMap<String, Item>() {
      {
         this.put("Totem", Items.TOTEM_OF_UNDYING);
         this.put("Crystal", Items.END_CRYSTAL);
         this.put("Gapple", Items.GOLDEN_APPLE);
         this.put("Pot", Items.POTIONITEM);
         this.put("Exp", Items.EXPERIENCE_BOTTLE);
         this.put("Bed", Items.BED);
         this.put("String", Items.STRING);
      }
   };
   Map<String, Block> allowedItemsBlock = new HashMap<String, Block>() {
      {
         this.put("Plates", Blocks.WOODEN_PRESSURE_PLATE);
         this.put("EChest", Blocks.ENDER_CHEST);
         this.put("Skull", Blocks.SKULL);
         this.put("Obby", Blocks.OBSIDIAN);
      }
   };
   int nowSlot;
   @EventHandler
   private final Listener<PacketEvent.Send> postSendListener = new Listener<>(event -> {
      if (event.getPacket() instanceof CPacketHeldItemChange) {
         this.nowSlot = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
      }
   });

   public OffHand() {
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.autoCrystal = false;
      this.firstChange = true;
      this.returnBack = false;
   }

   @Override
   public void onDisable() {
   }

   @Override
   public void onTick() {
      if (mc.world != null
         && mc.player != null
         && !mc.player.isDead
         && (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory)) {
         if (this.hotBarTotem.getValue() && this.refill.getValue()) {
            boolean hasTotem = false;

            for (int i = 0; i < 9; i++) {
               if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                  hasTotem = true;
               }
            }

            if (!hasTotem || !this.check.getValue()) {
               for (int ix = 9; ix < 36; ix++) {
                  if (mc.player.inventory.getStackInSlot(ix).getItem() == Items.TOTEM_OF_UNDYING) {
                     mc.playerController.windowClick(0, ix, this.totemSlot.getValue() - 1, ClickType.SWAP, mc.player);
                     break;
                  }
               }
            }
         }

         if (this.stepChanging) {
            if (this.tickWaited++ < this.tickDelay.getValue()) {
               return;
            }

            this.tickWaited = 0;
            this.stepChanging = false;
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
            this.switchDone.add(System.currentTimeMillis());
         }

         this.totems = mc.player
            .inventory
            .mainInventory
            .stream()
            .filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)
            .mapToInt(ItemStack::getCount)
            .sum();
         if (this.returnBack) {
            if (this.tickWaited++ < this.tickDelay.getValue()) {
               return;
            }

            this.changeBack();
         }

         this.itemCheck = this.getItem(false);
         if (this.offHandSame(this.itemCheck)) {
            if (this.hotBarTotem.getValue() && this.itemCheck.equals("Totem")) {
               this.itemCheck = this.getItem(this.switchItemTotemHot());
            }

            if (this.offHandSame(this.itemCheck)) {
               this.switchItemNormal(this.itemCheck);
            }
         }

         this.GetOffhand();
      }
   }

   private void GetOffhand() {
      if (this.HudMode.getValue().equals("Offhand")) {
         this.item = mc.player.getHeldItemOffhand().getItem();
         int items = mc.player.getHeldItemOffhand().getCount();
         this.ItemName = mc.player.getHeldItemOffhand().getDisplayName();
         this.counts = mc.player
               .inventory
               .mainInventory
               .stream()
               .filter(itemStack -> itemStack.getItem() == this.item)
               .mapToInt(ItemStack::getCount)
               .sum()
            + items;
      }
   }

   private void changeBack() {
      if (this.prevSlot == -1 || !mc.player.inventory.getStackInSlot(this.prevSlot).isEmpty()) {
         this.prevSlot = this.findEmptySlot();
      }

      if (this.prevSlot != -1) {
         mc.playerController.windowClick(0, this.prevSlot < 9 ? this.prevSlot + 36 : this.prevSlot, 0, ClickType.PICKUP, mc.player);
      } else if (this.debug.getValue()) {
         MessageBus.printDebug("Your inventory is full.", true);
      }

      this.returnBack = false;
      this.tickWaited = 0;
   }

   private boolean switchItemTotemHot() {
      int slot = InventoryUtil.findTotemSlot(0, 8);
      if (slot != -1) {
         if (this.nowSlot != slot) {
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         }

         return true;
      } else {
         return false;
      }
   }

   private void switchItemNormal(String itemCheck) {
      int t = this.getInventorySlot(itemCheck);
      if (t != -1) {
         if (itemCheck.equals("Totem") || !this.canSwitch()) {
            this.toOffHand(t);
         }
      }
   }

   private String getItem(boolean mainTotem) {
      String itemCheck = "";
      boolean normalOffHand = true;
      if (!mainTotem
         && (
            this.fallDistanceBol.getValue()
                  && mc.player.fallDistance >= this.fallDistance.getValue().intValue()
                  && mc.player.prevPosY != mc.player.posY
                  && !mc.player.isElytraFlying()
               || this.crystalCheck.getValue() && this.crystalDamage()
         )) {
         normalOffHand = false;
         itemCheck = "Totem";
      }

      Item mainHandItem = mc.player.getHeldItemMainhand().getItem();
      if (mainHandItem instanceof ItemSword) {
         boolean can = true;
         if (mc.gameSettings.keyBindUseItem.isKeyDown() && this.swordCheck.getValue()) {
            if (this.shiftPot.getValue() && mc.gameSettings.keyBindSneak.isKeyDown()) {
               can = false;
               itemCheck = "Pot";
               normalOffHand = false;
            } else if (this.rightGap.getValue() && !this.swordMode.getValue().equals("Gapple")) {
               can = false;
               itemCheck = "Gapple";
               normalOffHand = false;
            }
         }

         if (can) {
            String var6 = this.swordMode.getValue();
            switch (var6) {
               case "Gapple":
                  itemCheck = "Gapple";
                  normalOffHand = false;
                  break;
               case "Crystal":
                  itemCheck = "Crystal";
                  normalOffHand = false;
                  break;
               case "Pot":
                  itemCheck = "Pot";
                  normalOffHand = false;
            }
         }
      } else if (!this.swordCheck.getValue()) {
         if (this.shiftPot.getValue() && mc.gameSettings.keyBindSneak.isKeyDown()) {
            itemCheck = "Pot";
            normalOffHand = false;
         } else if (this.rightGap.getValue() && !this.swordMode.getValue().equals("Gapple")) {
            itemCheck = "Gapple";
            normalOffHand = false;
         }
      }

      if (mainHandItem == Items.DIAMOND_PICKAXE) {
         if (!mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) {
            String var8 = this.pickaxeMode.getValue();
            switch (var8) {
               case "Obsidian":
                  itemCheck = "Obby";
                  normalOffHand = false;
                  break;
               case "EChest":
                  itemCheck = "EChest";
                  normalOffHand = false;
                  break;
               case "Gapple":
                  itemCheck = "Gapple";
                  normalOffHand = false;
                  break;
               case "Crystal":
                  itemCheck = "Crystal";
                  normalOffHand = false;
            }
         }

         if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            String var9 = this.shiftPickaxeMode.getValue();
            switch (var9) {
               case "Obsidian":
                  itemCheck = "Obby";
                  normalOffHand = false;
                  break;
               case "EChest":
                  itemCheck = "EChest";
                  normalOffHand = false;
                  break;
               case "Gapple":
                  itemCheck = "Gapple";
                  normalOffHand = false;
                  break;
               case "Crystal":
                  itemCheck = "Crystal";
                  normalOffHand = false;
            }
         }
      }

      if (mainHandItem == Items.GOLDEN_APPLE) {
         String var10 = this.gappleMode.getValue();
         switch (var10) {
            case "Totem":
               itemCheck = "Totem";
               normalOffHand = false;
               break;
            case "Gapple":
               itemCheck = "Gapple";
               normalOffHand = false;
               break;
            case "Crystal":
               itemCheck = "Crystal";
               normalOffHand = false;
         }
      }

      if (this.crystalGap.getValue() && mainHandItem == Items.END_CRYSTAL) {
         itemCheck = "Gapple";
         normalOffHand = false;
      }

      if (normalOffHand && this.antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
         normalOffHand = false;
         itemCheck = "Crystal";
      }

      if (this.autoCrystal) {
         itemCheck = "Crystal";
         normalOffHand = false;
      }

      if (normalOffHand && !this.nearPlayer()) {
         itemCheck = this.noPlayerItem.getValue();
      }

      return this.getItemToCheck(itemCheck, mainTotem);
   }

   private boolean canSwitch() {
      long now = System.currentTimeMillis();

      for (int i = 0; i < this.switchDone.size() && now - this.switchDone.get(i) > 1000L; i++) {
         this.switchDone.remove(i);
      }

      if (this.switchDone.size() / 2 >= this.maxSwitchPerSecond.getValue()) {
         return true;
      } else {
         this.switchDone.add(now);
         return false;
      }
   }

   private boolean nearPlayer() {
      if (this.playerDistance.getValue().intValue() == 0) {
         return true;
      } else {
         for (EntityPlayer pl : mc.world.playerEntities) {
            if (pl != mc.player && mc.player.getDistance(pl) < this.playerDistance.getValue()) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean crystalDamage() {
      PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(this.predict.getValue(), true, 39, 2, 2, 1, true, true, true, true, 2, 0.15);

      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityEnderCrystal && mc.player.getDistance(t) <= 12.0F) {
            EntityPlayer player = PredictUtil.predictPlayer(mc.player, settings);
            if (DamageUtil.calculateCrystalDamage(
                        mc.player, player.getPositionVector(), player.getEntityBoundingBox(), t.posX, t.posY, t.posZ
                     )
                     * this.biasDamage.getValue()
                  >= EntityUtil.getHealth(mc.player)
               || DamageUtil.calculateCrystalDamage(
                           mc.player, player.getPositionVector(), player.getEntityBoundingBox(), t.posX, t.posY, t.posZ
                        )
                        * this.biasDamage.getValue()
                     >= EntityUtil.getHealth(mc.player)
                  && this.totems > 0) {
               return true;
            }
         }
      }

      return false;
   }

   private int findEmptySlot() {
      for (int i = 35; i > -1; i--) {
         if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   private boolean offHandSame(String itemCheck) {
      Item offHandItem = mc.player.getHeldItemOffhand().getItem();
      if (this.allowedItemsBlock.containsKey(itemCheck)) {
         Block item = this.allowedItemsBlock.get(itemCheck);
         if (offHandItem instanceof ItemBlock) {
            return ((ItemBlock)offHandItem).getBlock() != item;
         } else {
            return offHandItem instanceof ItemSkull && item == Blocks.SKULL ? true : true;
         }
      } else {
         Item item = this.allowedItemsItem.get(itemCheck);
         return item != offHandItem;
      }
   }

   private String getItemToCheck(String str, boolean mainTotem) {
      if (mainTotem) {
         return str.isEmpty() ? this.nonDefaultItem.getValue() : str;
      } else if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
         return PlayerUtil.getHealth() > this.swordHealth.getValue().intValue()
            ? (str.isEmpty() ? this.nonDefaultItem.getValue() : str)
            : this.defaultItem.getValue();
      } else {
         return PlayerUtil.getHealth() > this.healthSwitch.getValue().intValue()
            ? (str.isEmpty() ? this.nonDefaultItem.getValue() : str)
            : this.defaultItem.getValue();
      }
   }

   private int getInventorySlot(String itemName) {
      boolean blockBool = false;
      Object item;
      if (this.allowedItemsItem.containsKey(itemName)) {
         item = this.allowedItemsItem.get(itemName);
      } else {
         item = this.allowedItemsBlock.get(itemName);
         blockBool = true;
      }

      if (!this.firstChange && this.prevSlot != -1) {
         int res = this.isCorrect(this.prevSlot, blockBool, item, itemName);
         if (res != -1) {
            return res;
         }
      }

      for (int i = this.onlyHotBar.getValue() ? 8 : 35; i > (this.noHotBar.getValue() ? 9 : -1); i--) {
         int res = this.isCorrect(i, blockBool, item, itemName);
         if (res != -1) {
            return res;
         }
      }

      return -1;
   }

   private int isCorrect(int i, boolean blockBool, Object item, String itemName) {
      Item temp = mc.player.inventory.getStackInSlot(i).getItem();
      if (blockBool) {
         if (temp instanceof ItemBlock) {
            if (((ItemBlock)temp).getBlock() == item) {
               return i;
            }
         } else if (temp instanceof ItemSkull && item == Blocks.SKULL) {
            return i;
         }
      } else if (item == temp) {
         if (itemName.equals("Pot")
            && !this.potionChoose.getValue().equalsIgnoreCase("first")
            && !mc.player.inventory.getStackInSlot(i).stackTagCompound.toString().split(":")[2].contains(this.potionChoose.getValue())) {
            return -1;
         }

         return i;
      }

      return -1;
   }

   private void toOffHand(int t) {
      if (!mc.player.getHeldItemOffhand().isEmpty()) {
         if (this.firstChange) {
            this.prevSlot = t;
         }

         this.returnBack = true;
         this.firstChange = !this.firstChange;
      } else {
         this.prevSlot = -1;
      }

      mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
      if (this.tickDelay.getValue() == 0) {
         mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
         this.switchDone.add(System.currentTimeMillis());
      } else {
         this.stepChanging = true;
      }

      this.tickWaited = 0;
   }

   @Override
   public String getHudInfo() {
      if (this.HudMode.getValue().equals("Totem")) {
         this.counts = this.totems;
         if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            this.counts++;
         }

         return "[" + ChatFormatting.WHITE + "Totem " + this.counts + ChatFormatting.GRAY + "]";
      } else {
         return this.itemCheck.isEmpty()
            ? "[" + ChatFormatting.WHITE + "None" + ChatFormatting.GRAY + "]"
            : "[" + ChatFormatting.WHITE + this.itemCheck + " " + this.counts + ChatFormatting.GRAY + "]";
      }
   }
}
