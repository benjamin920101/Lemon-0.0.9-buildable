package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;

@Module.Declaration(name = "AutoMend", category = Category.Combat)
public class AutoMend extends Module {
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 1000);
   IntegerSetting minDamage = this.registerInteger("Min Damage", 50, 1, 100);
   IntegerSetting maxHeal = this.registerInteger("Repair To", 90, 1, 100);
   BooleanSetting takeOff = this.registerBoolean("TakeOff", true);
   IntegerSetting takeOffDelay = this.registerInteger("TakeOff Delay", 0, 0, 1000);
   BooleanSetting predict = this.registerBoolean("Predict", true);
   BooleanSetting crystal = this.registerBoolean("Crystal Check", true);
   DoubleSetting biasDamage = this.registerDouble("Bias Damage", 1.0, 0.0, 3.0);
   BooleanSetting health = this.registerBoolean("Health Check", true);
   IntegerSetting minHealth = this.registerInteger("Min Health", 16, 0, 36, () -> this.health.getValue());
   BooleanSetting player = this.registerBoolean("Enemy Check", true);
   DoubleSetting maxSpeed = this.registerDouble("Max Speed", 10.0, 0.0, 50.0, () -> this.player.getValue());
   int tookOff;
   Timing timer = new Timing();
   Timing takeOffTimer = new Timing();
   char toMend = 0;
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
      if (this.rotate.getValue()) {
         if (event.getPhase() == Phase.PRE) {
            PlayerPacket packet = new PlayerPacket(this, new Vec2f(PlayerPacketManager.INSTANCE.getServerSideRotation().x, 90.0F));
            PlayerPacketManager.INSTANCE.addPacket(packet);
         }
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.rotate.getValue()) {
         if (event.getPacket() instanceof Rotation) {
            ((Rotation)event.getPacket()).yaw = PlayerPacketManager.INSTANCE.getServerSideRotation().x;
         }

         if (event.getPacket() instanceof PositionRotation) {
            ((PositionRotation)event.getPacket()).yaw = PlayerPacketManager.INSTANCE.getServerSideRotation().x;
         }
      }
   });

   @Override
   public void onEnable() {
      this.tookOff = 0;
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && !mc.player.isDead && mc.player.ticksExisted >= 10) {
         if (this.crystal.getValue() && this.crystalDamage()) {
            this.setDisabledMessage("Lethal crystal nearby");
            this.disable();
         } else if (this.health.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() < this.minHealth.getValue().intValue()) {
            this.setDisabledMessage("Low health");
            this.disable();
         } else if (this.player.getValue() && this.checkNearbyPlayers()) {
            this.setDisabledMessage("Players nearby");
            this.disable();
         } else if (this.findXPSlot() == -1) {
            this.setDisabledMessage("No xp bottle found in hotbar");
            this.disable();
         } else if (this.checkFinished()) {
            this.setDisabledMessage("Finished mending armors");
            this.disable();
         } else if (this.timer.passedMs(this.delay.getValue().intValue())) {
            this.timer.reset();
            int sumOfDamage = 0;
            List<ItemStack> armour = mc.player.inventory.armorInventory;

            for (int i = 0; i < armour.size(); i++) {
               ItemStack itemStack = armour.get(i);
               if (!itemStack.isEmpty) {
                  float damageOnArmor = itemStack.getMaxDamage() - itemStack.getItemDamage();
                  float damagePercent = 100.0F - 100.0F * (1.0F - damageOnArmor / itemStack.getMaxDamage());
                  if (damagePercent <= this.maxHeal.getValue().intValue()) {
                     if (damagePercent <= this.minDamage.getValue().intValue()) {
                        this.toMend |= (char)(1 << i);
                     }

                     if (this.predict.getValue()) {
                        sumOfDamage += (int)(
                           itemStack.getMaxDamage() * this.maxHeal.getValue() / 100.0F - (itemStack.getMaxDamage() - itemStack.getItemDamage())
                        );
                     }
                  } else {
                     this.toMend &= (char)(~(1 << i));
                  }
               }
            }

            if (this.toMend > 0) {
               if (this.predict.getValue()) {
                  int totalXp = mc.world
                     .loadedEntityList
                     .stream()
                     .filter(entity -> entity instanceof EntityXPOrb)
                     .filter(entity -> entity.getDistanceSq(mc.player) <= 1.0)
                     .mapToInt(entity -> ((EntityXPOrb)entity).xpValue)
                     .sum();
                  if (totalXp * 2 < sumOfDamage) {
                     this.mendArmor();
                  }
               } else {
                  this.mendArmor();
               }
            }
         }
      } else {
         this.disable();
      }
   }

   private void mendArmor() {
      int newSlot = this.findXPSlot();
      if (newSlot != -1) {
         InventoryUtil.run(
            newSlot, this.packetSwitch.getValue(), () -> mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
         );
         if (this.takeOff.getValue()) {
            this.takeArmorOff();
         }
      }
   }

   private void takeArmorOff() {
      for (int slot = 5; slot <= 8; slot++) {
         ItemStack item = this.getArmor(slot);
         double max_dam = item.getMaxDamage();
         double dam_left = item.getMaxDamage() - item.getItemDamage();
         double percent = dam_left / max_dam * 100.0;
         if (percent >= this.maxHeal.getValue().intValue() && item.getItem() != Items.AIR) {
            if (!this.notInInv(Items.AIR)) {
               return;
            }

            if (!this.takeOffTimer.passedMs(this.takeOffDelay.getValue().intValue())) {
               return;
            }

            this.takeOffTimer.reset();
            boolean hasEmpty = false;

            for (int l_I = 0; l_I < 36; l_I++) {
               ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
               if (l_Stack.isEmpty) {
                  hasEmpty = true;
                  break;
               }
            }

            if (hasEmpty) {
               mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, mc.player);
            } else {
               for (int l_l = 1; l_l < 5; l_l++) {
                  if (mc.player.inventoryContainer.getSlot(l_l).getStack().isEmpty) {
                     mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                     mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_l, 0, ClickType.PICKUP, mc.player);
                  }
               }
            }
         }
      }
   }

   private ItemStack getArmor(int first) {
      return (ItemStack)mc.player.inventoryContainer.getInventory().get(first);
   }

   public Boolean notInInv(Item itemOfChoice) {
      int n = 0;
      if (itemOfChoice == mc.player.getHeldItemOffhand().getItem()) {
         return true;
      } else {
         for (int i = 35; i >= 0; i--) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == itemOfChoice) {
               return true;
            }

            n++;
         }

         return n <= 35;
      }
   }

   private int findXPSlot() {
      int slot = -1;

      for (int i = 0; i < 9; i++) {
         if (mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
            slot = i;
            break;
         }
      }

      return slot;
   }

   private boolean crystalDamage() {
      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityEnderCrystal
            && mc.player.getDistance(t) <= 12.0F
            && DamageUtil.calculateDamage(mc.player, mc.player.getPositionVector(), mc.player.getEntityBoundingBox(), (EntityEnderCrystal)t)
                  * this.biasDamage.getValue()
               >= mc.player.getHealth()) {
            return true;
         }
      }

      return false;
   }

   private boolean checkNearbyPlayers() {
      AxisAlignedBB box = new AxisAlignedBB(
         mc.player.posX - 0.5,
         mc.player.posY - 0.5,
         mc.player.posZ - 0.5,
         mc.player.posX + 0.5,
         mc.player.posY + 2.5,
         mc.player.posZ + 0.5
      );

      for (EntityPlayer entity : mc.world.playerEntities) {
         if (!EntityUtil.basicChecksEntity(entity)
            && mc.player.connection.getPlayerInfo(entity.getName()) != null
            && !(LemonClient.speedUtil.getPlayerSpeed(entity) >= this.maxSpeed.getValue())
            && box.intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private boolean checkFinished() {
      int finished = 0;

      for (int slot = 5; slot <= 8; slot++) {
         ItemStack item = this.getArmor(slot);
         if (this.getItemDamage(slot) >= this.maxHeal.getValue() || item == ItemStack.EMPTY) {
            finished++;
         }
      }

      return finished >= 4;
   }

   private int getItemDamage(int slot) {
      ItemStack itemStack = mc.player.inventoryContainer.getSlot(slot).getStack();
      float green = ((float)itemStack.getMaxDamage() - itemStack.getItemDamage()) / itemStack.getMaxDamage();
      float red = 1.0F - green;
      return 100 - (int)(red * 100.0F);
   }
}
