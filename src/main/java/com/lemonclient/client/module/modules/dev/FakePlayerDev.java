package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.TotemPopEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.setting.values.StringSetting;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.GameType;

@Module.Declaration(name = "FakePlayerDev", category = Category.Dev)
public class FakePlayerDev extends Module {
   private final ItemStack[] armors = new ItemStack[]{
      new ItemStack(Items.DIAMOND_BOOTS), new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.DIAMOND_HELMET)
   };
   StringSetting nameFakePlayer = this.registerString("Name FakePlayer", "NotLazyOfLazys");
   BooleanSetting copyInventory = this.registerBoolean("Copy Inventory", false);
   BooleanSetting playerStacked = this.registerBoolean("Player Stacked", false);
   BooleanSetting onShift = this.registerBoolean("On Shift", false);
   BooleanSetting simulateDamage = this.registerBoolean("Simulate Damage", false);
   IntegerSetting vulnerabilityTick = this.registerInteger("Vulnerability Tick", 4, 0, 10);
   IntegerSetting resetHealth = this.registerInteger("Reset Health", 10, 0, 36);
   IntegerSetting tickRegenVal = this.registerInteger("Tick Regen", 4, 0, 30);
   IntegerSetting startHealth = this.registerInteger("Start Health", 20, 0, 30);
   ModeSetting moving = this.registerMode("Moving", Arrays.asList("None", "Line", "Circle", "Random"), "None");
   DoubleSetting speed = this.registerDouble("Speed", 0.36, 0.0, 4.0);
   DoubleSetting range = this.registerDouble("Range", 3.0, 0.0, 14.0);
   BooleanSetting followPlayer = this.registerBoolean("Follow Player", true);
   BooleanSetting resistance = this.registerBoolean("Resistance", true);
   BooleanSetting pop = this.registerBoolean("Pop", true);
   int incr;
   boolean beforePressed;
   ArrayList<FakePlayerDev.playerInfo> listPlayers = new ArrayList<>();
   FakePlayerDev.movingManager manager = new FakePlayerDev.movingManager();
   @EventHandler
   private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(
      event -> {
         if (this.simulateDamage.getValue()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof SPacketSoundEffect) {
               SPacketSoundEffect packetSoundEffect = (SPacketSoundEffect)packet;
               if (packetSoundEffect.getCategory() == SoundCategory.BLOCKS && packetSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                  for (Entity entity : new ArrayList<Entity>(mc.world.loadedEntityList)) {
                     if (entity instanceof EntityEnderCrystal
                        && entity.getDistanceSq(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()) <= 36.0
                        )
                      {
                        for (EntityPlayer entityPlayer : mc.world.playerEntities) {
                           if (entityPlayer.getName().split(this.nameFakePlayer.getText()).length == 2) {
                              Optional<FakePlayerDev.playerInfo> temp = this.listPlayers
                                 .stream()
                                 .filter(e -> e.name.equals(entityPlayer.getName()))
                                 .findAny();
                              if (temp.isPresent() && temp.get().canPop()) {
                                 float damage = DamageUtil.calculateDamage(
                                    entityPlayer,
                                    entityPlayer.getPositionVector(),
                                    entityPlayer.getEntityBoundingBox(),
                                    packetSoundEffect.getX(),
                                    packetSoundEffect.getY(),
                                    packetSoundEffect.getZ(),
                                    6.0F,
                                    "Default"
                                 );
                                 if (damage > entityPlayer.getHealth()) {
                                    entityPlayer.setHealth(this.resetHealth.getValue().intValue());
                                    if (this.pop.getValue()) {
                                       mc.effectRenderer.emitParticleAtEntity(entityPlayer, EnumParticleTypes.TOTEM, 30);
                                       mc.world
                                          .playSound(
                                             entityPlayer.posX,
                                             entityPlayer.posY,
                                             entityPlayer.posZ,
                                             SoundEvents.ITEM_TOTEM_USE,
                                             entity.getSoundCategory(),
                                             1.0F,
                                             1.0F,
                                             false
                                          );
                                    }

                                    LemonClient.EVENT_BUS.post(new TotemPopEvent(entityPlayer));
                                 } else {
                                    entityPlayer.setHealth(entityPlayer.getHealth() - damage);
                                 }

                                 temp.get().tickPop = 0;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   );

   @Override
   public void onEnable() {
      this.incr = 0;
      this.beforePressed = false;
      if (mc.player != null && !mc.player.isDead) {
         if (!this.onShift.getValue()) {
            this.spawnPlayer();
         }
      } else {
         this.disable();
      }
   }

   void spawnPlayer() {
      EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(
         mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), this.nameFakePlayer.getText())
      );
      clonedPlayer.copyLocationAndAnglesFrom(mc.player);
      clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
      clonedPlayer.rotationYaw = mc.player.rotationYaw;
      clonedPlayer.rotationPitch = mc.player.rotationPitch;
      clonedPlayer.setGameType(GameType.SURVIVAL);
      clonedPlayer.setHealth(this.startHealth.getValue().intValue());
      mc.world.addEntityToWorld(-1234 + this.incr, clonedPlayer);
      this.incr++;
      if (this.copyInventory.getValue()) {
         clonedPlayer.inventory.copyInventory(mc.player.inventory);
      } else if (this.playerStacked.getValue()) {
         for (int i = 0; i < 4; i++) {
            ItemStack item = this.armors[i];
            item.addEnchantment(i == 3 ? Enchantments.BLAST_PROTECTION : Enchantments.PROTECTION, 4);
            clonedPlayer.inventory.armorInventory.set(i, item);
         }
      }

      if (this.resistance.getValue()) {
         clonedPlayer.addPotionEffect(new PotionEffect(Potion.getPotionById(11), 123456789, 0));
      }

      clonedPlayer.onEntityUpdate();
      this.listPlayers.add(new FakePlayerDev.playerInfo(clonedPlayer.getName()));
      if (!this.moving.getValue().equals("None")) {
         this.manager
            .addPlayer(
               clonedPlayer.entityId,
               this.moving.getValue(),
               this.speed.getValue(),
               this.moving.getValue().equals("Line") ? this.getDirection() : -1,
               this.range.getValue(),
               this.followPlayer.getValue()
            );
      }
   }

   @Override
   public void onUpdate() {
      if (this.onShift.getValue() && mc.gameSettings.keyBindSneak.isPressed() && !this.beforePressed) {
         this.beforePressed = true;
         this.spawnPlayer();
      } else {
         this.beforePressed = false;
      }

      for (int i = 0; i < this.listPlayers.size(); i++) {
         if (this.listPlayers.get(i).update()) {
            int finalI = i;
            Optional<EntityPlayer> temp = mc.world
               .playerEntities
               .stream()
               .filter(e -> e.getName().equals(this.listPlayers.get(finalI).name))
               .findAny();
            if (temp.isPresent() && temp.get().getHealth() < 20.0F) {
               temp.get().setHealth(temp.get().getHealth() + 1.0F);
            }
         }
      }

      this.manager.update();
   }

   int getDirection() {
      int yaw = (int)RotationUtil.normalizeAngle(mc.player.getPitchYaw().y);
      if (yaw < 0) {
         yaw += 360;
      }

      yaw += 22;
      yaw %= 360;
      return yaw / 45;
   }

   @Override
   public void onDisable() {
      if (mc.world != null) {
         for (int i = 0; i < this.incr; i++) {
            mc.world.removeEntityFromWorld(-1234 + i);
         }
      }

      this.listPlayers.clear();
      this.manager.remove();
   }

   static class movingManager {
      private final ArrayList<FakePlayerDev.movingPlayer> players = new ArrayList<>();

      void addPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
         this.players.add(new FakePlayerDev.movingPlayer(id, type, speed, direction, range, follow));
      }

      void update() {
         this.players.forEach(FakePlayerDev.movingPlayer::move);
      }

      void remove() {
         this.players.clear();
      }
   }

   static class movingPlayer {
      private final int id;
      private final String type;
      private final double speed;
      private final int direction;
      private final double range;
      private final boolean follow;
      int rad = 0;

      public movingPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
         this.id = id;
         this.type = type;
         this.speed = speed;
         this.direction = Math.abs(direction);
         this.range = range;
         this.follow = follow;
      }

      void move() {
         Entity player = FakePlayerDev.mc.world.getEntityByID(this.id);
         if (player != null) {
            String var2 = this.type;
            switch (var2) {
               case "Line":
                  double posX = this.follow ? FakePlayerDev.mc.player.posX : player.posX;
                  double posY = this.follow ? FakePlayerDev.mc.player.posY : player.posY;
                  double posZ = this.follow ? FakePlayerDev.mc.player.posZ : player.posZ;
                  switch (this.direction) {
                     case 0:
                        posZ += this.speed;
                        break;
                     case 1:
                        posX -= this.speed / 2.0;
                        posZ += this.speed / 2.0;
                        break;
                     case 2:
                        posX -= this.speed / 2.0;
                        break;
                     case 3:
                        posZ -= this.speed / 2.0;
                        posX -= this.speed / 2.0;
                        break;
                     case 4:
                        posZ -= this.speed;
                        break;
                     case 5:
                        posX += this.speed / 2.0;
                        posZ -= this.speed / 2.0;
                        break;
                     case 6:
                        posX += this.speed;
                        break;
                     case 7:
                        posZ += this.speed / 2.0;
                        posX += this.speed / 2.0;
                  }

                  if (BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir) {
                     for (int i = 0; i < 5 && BlockUtil.getBlock(posX, posY - 1.0, posZ) instanceof BlockAir; i++) {
                        posY--;
                     }
                  } else {
                     for (int i = 0; i < 5 && !(BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir); i++) {
                        posY++;
                     }
                  }

                  player.setPositionAndUpdate(posX, posY, posZ);
                  break;
               case "Circle":
                  double posXCir = Math.cos(this.rad / 100.0) * this.range + FakePlayerDev.mc.player.posX;
                  double posZCir = Math.sin(this.rad / 100.0) * this.range + FakePlayerDev.mc.player.posZ;
                  double posYCir = FakePlayerDev.mc.player.posY;
                  if (BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir) {
                     for (int i = 0; i < 5 && BlockUtil.getBlock(posXCir, posYCir - 1.0, posZCir) instanceof BlockAir; i++) {
                        posYCir--;
                     }
                  } else {
                     for (int i = 0; i < 5 && !(BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir); i++) {
                        posYCir++;
                     }
                  }

                  player.setPositionAndUpdate(posXCir, posYCir, posZCir);
                  this.rad = (int)(this.rad + this.speed * 10.0);
               case "Random":
            }
         }
      }
   }

   class playerInfo {
      final String name;
      int tickPop = -1;
      int tickRegen = 0;

      public playerInfo(String name) {
         this.name = name;
      }

      boolean update() {
         if (this.tickPop != -1 && ++this.tickPop >= FakePlayerDev.this.vulnerabilityTick.getValue()) {
            this.tickPop = -1;
         }

         if (++this.tickRegen >= FakePlayerDev.this.tickRegenVal.getValue()) {
            this.tickRegen = 0;
            return true;
         } else {
            return false;
         }
      }

      boolean canPop() {
         return this.tickPop == -1;
      }
   }
}
