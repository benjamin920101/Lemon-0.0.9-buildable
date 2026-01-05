package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.EntityRemovedEvent;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoPot", category = Category.Dev, priority = 1001)
public class AutoPot extends Module {
   ModeSetting page = this.registerMode("Page", Arrays.asList("General", "BadPot"), "General");
   BooleanSetting hp = this.registerBoolean("Health Potion", false, () -> this.page.getValue().equals("General"));
   IntegerSetting health = this.registerInteger("Health", 16, 0, 20, () -> this.hp.getValue() && this.page.getValue().equals("General"));
   BooleanSetting equal = this.registerBoolean("Equal", false, () -> this.hp.getValue() && this.page.getValue().equals("General"));
   BooleanSetting predict = this.registerBoolean("Predict", false, () -> this.hp.getValue() && this.page.getValue().equals("General"));
   DoubleSetting times = this.registerDouble(
      "Time(s)", 1.0, 0.0, 5.0, () -> this.hp.getValue() && this.predict.getValue() && this.page.getValue().equals("General")
   );
   IntegerSetting predictHpDelay = this.registerInteger(
      "Predict Health Delay", 50, 0, 1000, () -> this.hp.getValue() && this.predict.getValue() && this.page.getValue().equals("General")
   );
   IntegerSetting healthSlot = this.registerInteger("Health Slot", 1, 1, 9, () -> this.hp.getValue() && this.page.getValue().equals("General"));
   IntegerSetting hpDelay = this.registerInteger("Health Delay", 50, 0, 1000, () -> this.hp.getValue() && this.page.getValue().equals("General"));
   BooleanSetting speed = this.registerBoolean("Swiftness", false, () -> this.page.getValue().equals("General"));
   IntegerSetting time = this.registerInteger("Time Left", 5, 0, 30, () -> this.speed.getValue() && this.page.getValue().equals("General"));
   IntegerSetting swiftnessSlot = this.registerInteger("Swiftness Slot", 1, 1, 9, () -> this.speed.getValue() && this.page.getValue().equals("General"));
   IntegerSetting speedDelay = this.registerInteger("Swiftness Delay", 50, 0, 1000, () -> this.speed.getValue() && this.page.getValue().equals("General"));
   BooleanSetting only = this.registerBoolean("On GroundOnly", true, () -> this.page.getValue().equals("General"));
   BooleanSetting silentSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("General"));
   IntegerSetting delay = this.registerInteger("Delay", 10, 0, 30, () -> this.page.getValue().equals("BadPot"));
   DoubleSetting factor = this.registerDouble("Factor", 0.75, 0.0, 1.5, () -> this.page.getValue().equals("BadPot"));
   DoubleSetting range = this.registerDouble("Range", 4.0, 0.0, 10.0, () -> this.page.getValue().equals("BadPot"));
   IntegerSetting badSlot = this.registerInteger("BadPot Slot", 1, 1, 9, () -> this.page.getValue().equals("BadPot"));
   BooleanSetting weak = this.registerBoolean("Weakness", false, () -> this.page.getValue().equals("BadPot"));
   BooleanSetting jump = this.registerBoolean("JumpBoost", false, () -> this.page.getValue().equals("BadPot"));
   BooleanSetting poison = this.registerBoolean("Poison", false, () -> this.page.getValue().equals("BadPot"));
   BooleanSetting slow = this.registerBoolean("Slowness", false, () -> this.page.getValue().equals("BadPot"));
   BooleanSetting debug = this.registerBoolean("Debug", false, () -> this.page.getValue().equals("BadPot"));
   HashMap<Integer, Long> weaknessTime = new HashMap<>();
   HashMap<Integer, Long> jumpBoostTime = new HashMap<>();
   HashMap<Integer, Long> poisonTime = new HashMap<>();
   HashMap<Integer, Long> slownessTime = new HashMap<>();
   Timing hpTimer = new Timing();
   Timing hpPredictTimer = new Timing();
   Timing speedTimer = new Timing();
   Timing badPotTimer = new Timing();
   int potionSlot;
   int potSlot;
   double lastHealth = 36.0;
   boolean working = false;
   boolean preHp;
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            this.working = false;
            if (this.only.getValue() || mc.player.isInLava() || mc.player.isInWater()) {
               List<BlockPos> posList = new ArrayList<>();
               Vec3d floorPos = new Vec3d(mc.player.posX, mc.player.posY - 2.0, mc.player.posZ);
               AxisAlignedBB potBox = new AxisAlignedBB(
                  mc.player.posX - 0.125,
                  mc.player.posY - 2.0,
                  mc.player.posZ - 0.125,
                  mc.player.posX + 0.125,
                  mc.player.posY + mc.player.eyeHeight + 0.125,
                  mc.player.posZ + 0.125
               );

               for (int i = 0;
                  i < mc.player.posY + mc.player.eyeHeight + 1.125
                     && (int)(floorPos.y + i) <= (int)(mc.player.posY + mc.player.eyeHeight + 0.125);
                  i++
               ) {
                  for (Vec3d vec3d : new Vec3d[]{
                     new Vec3d(0.125, 0.0, 0.125), new Vec3d(0.125, 0.0, -0.125), new Vec3d(-0.125, 0.0, 0.125), new Vec3d(-0.125, 0.0, -0.125)
                  }) {
                     BlockPos pos = new BlockPos(
                        floorPos.x + vec3d.x, floorPos.y + i, floorPos.z + vec3d.z
                     );
                     if (!BlockUtil.isAir(pos)) {
                        posList.add(pos);
                     }
                  }
               }

               boolean can = false;

               for (BlockPos pos : posList) {
                  AxisAlignedBB box = BlockUtil.getBoundingBox(pos);
                  if (box != null && MathUtil.isIntersect(potBox, box)) {
                     can = true;
                     break;
                  }
               }

               if (!can) {
                  return;
               }
            }

            if (this.potionSlot == -1) {
               this.potionSlot = this.getPotion();
            }

            if (this.potSlot == -1) {
               this.potSlot = this.getBadPot();
            }

            if (this.potionSlot != -1 || this.potSlot != -1) {
               this.working = true;
               if (this.potionSlot > 8) {
                  if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
                     return;
                  }

                  int finalSlot = this.potionSlot == InventoryUtil.getPotion("swiftness") ? this.swiftnessSlot.getValue() : this.healthSlot.getValue();
                  mc.playerController.windowClick(0, this.potionSlot, finalSlot - 1, ClickType.SWAP, mc.player);
                  mc.playerController.updateController();
                  this.potionSlot = finalSlot - 1;
               }

               if (this.potSlot > 8) {
                  if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
                     return;
                  }

                  mc.playerController.windowClick(0, this.potSlot, this.badSlot.getValue() - 1, ClickType.SWAP, mc.player);
                  mc.playerController.updateController();
                  this.potSlot = this.badSlot.getValue() - 1;
               }

               if (event.getPhase() == Phase.PRE) {
                  PlayerPacket packet = new PlayerPacket(this, new Vec2f(PlayerPacketManager.INSTANCE.getServerSideRotation().x, 90.0F));
                  PlayerPacketManager.INSTANCE.addPacket(packet);
               }

               if (event.getPhase() == Phase.POST
                  && (
                     PlayerPacketManager.INSTANCE.getPrevServerSideRotation().y > 85.0F
                        || PlayerPacketManager.INSTANCE.getServerSideRotation().y > 85.0F
                  )) {
                  int slot = this.potionSlot == -1 ? this.potSlot : this.potionSlot;
                  InventoryUtil.run(
                     slot, this.silentSwitch.getValue(), () -> mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                  );
                  this.potionSlot = this.potSlot = -1;
               }
            }
         }
      }
   );
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.working) {
         if (event.getPacket() instanceof Rotation) {
            ((Rotation)event.getPacket()).pitch = 90.0F;
         }

         if (event.getPacket() instanceof PositionRotation) {
            ((PositionRotation)event.getPacket()).pitch = 90.0F;
         }
      }
   });
   @EventHandler
   private final Listener<PacketEvent.PostReceive> receiveListener = new Listener<>(event -> {
      if (event.getPacket() instanceof SPacketDestroyEntities) {
         Arrays.stream(((SPacketDestroyEntities)event.getPacket()).getEntityIDs()).forEach(this.weaknessTime::remove);
         Arrays.stream(((SPacketDestroyEntities)event.getPacket()).getEntityIDs()).forEach(this.jumpBoostTime::remove);
         Arrays.stream(((SPacketDestroyEntities)event.getPacket()).getEntityIDs()).forEach(this.poisonTime::remove);
         Arrays.stream(((SPacketDestroyEntities)event.getPacket()).getEntityIDs()).forEach(this.slownessTime::remove);
      }

      if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus)event.getPacket()).getOpCode() == 35) {
         this.weaknessTime.remove(((SPacketEntityStatus)event.getPacket()).getEntity(mc.world).entityId);
         this.jumpBoostTime.remove(((SPacketEntityStatus)event.getPacket()).getEntity(mc.world).entityId);
         this.poisonTime.remove(((SPacketEntityStatus)event.getPacket()).getEntity(mc.world).entityId);
         this.slownessTime.remove(((SPacketEntityStatus)event.getPacket()).getEntity(mc.world).entityId);
      }
   });
   @EventHandler
   private final Listener<EntityRemovedEvent> entityRemovedEventListener = new Listener<>(
      event -> {
         if (event.getEntity() instanceof EntityPotion) {
            List<PotionEffect> effectList = PotionUtils.getEffectsFromStack(((EntityPotion)event.getEntity()).getPotion());
            PotionEffect weakness = null;
            PotionEffect jumpBoost = null;
            PotionEffect poison = null;
            PotionEffect slowness = null;

            for (PotionEffect effect : effectList) {
               if (effect.getPotion() == MobEffects.WEAKNESS) {
                  weakness = effect;
               }

               if (effect.getPotion() == MobEffects.JUMP_BOOST) {
                  jumpBoost = effect;
               }

               if (effect.getPotion() == MobEffects.POISON) {
                  poison = effect;
               }

               if (effect.getPotion() == MobEffects.SLOWNESS) {
                  slowness = effect;
               }
            }

            AxisAlignedBB box = event.getEntity().boundingBox.grow(4.0, 2.0, 4.0);
            PotionEffect finalWeakness = weakness;
            PotionEffect finalJumpBoost = jumpBoost;
            PotionEffect finalPoison = poison;
            PotionEffect finalSlowness = slowness;
            mc.world
               .playerEntities
               .stream()
               .filter(p -> mc.player.connection.getPlayerInfo(p.getName()) != null)
               .filter(EntityUtil::isAlive)
               .filter(p -> box.intersects(p.boundingBox))
               .forEach(p -> {
                  double distanceSq = event.getEntity().getDistanceSq(p);
                  if (distanceSq < 16.0) {
                     double factor = Math.sqrt(distanceSq) * this.factor.getValue();
                     if (finalWeakness != null) {
                        double duration = factor * finalWeakness.getDuration();
                        this.weaknessTime.put(p.getEntityId(), (long)(System.currentTimeMillis() + duration * 50.0));
                     }

                     if (finalJumpBoost != null) {
                        double duration = factor * finalJumpBoost.getDuration();
                        this.jumpBoostTime.put(p.getEntityId(), (long)(System.currentTimeMillis() + duration * 50.0));
                     }

                     if (finalPoison != null) {
                        double duration = factor * finalPoison.getDuration();
                        this.poisonTime.put(p.getEntityId(), (long)(System.currentTimeMillis() + duration * 50.0));
                     }

                     if (finalSlowness != null) {
                        double duration = factor * finalSlowness.getDuration();
                        this.slownessTime.put(p.getEntityId(), (long)(System.currentTimeMillis() + duration * 50.0));
                     }
                  }
               });
         }
      }
   );

   @Override
   public void onEnable() {
      this.weaknessTime = this.jumpBoostTime = this.poisonTime = this.slownessTime = new HashMap<>();
   }

   @Override
   public void fast() {
      for (EntityPlayer player : mc.world.playerEntities) {
         int id = player.getEntityId();
         long time = System.currentTimeMillis();
         if (this.weaknessTime.containsKey(id) && this.weaknessTime.get(id) <= time) {
            this.weaknessTime.remove(id);
         }

         if (this.jumpBoostTime.containsKey(id) && this.jumpBoostTime.get(id) <= time) {
            this.jumpBoostTime.remove(id);
         }

         if (this.poisonTime.containsKey(id) && this.poisonTime.get(id) <= time) {
            this.poisonTime.remove(id);
         }

         if (this.slownessTime.containsKey(id) && this.slownessTime.get(id) <= time) {
            this.slownessTime.remove(id);
         }
      }

      if (this.debug.getValue()) {
         StringBuilder weak = new StringBuilder("Weakness");

         for (EntityPlayer player : mc.world.playerEntities) {
            if (this.weaknessTime.containsKey(player.getEntityId())) {
               weak.append(player.getName()).append(" ").append(this.weaknessTime.get(player.getEntityId()) - System.currentTimeMillis()).append(", ");
            }
         }

         if (!weak.toString().equals("Weakness")) {
            MessageBus.sendClientDeleteMessage(weak.toString(), Notification.Type.DISABLE, "Weakness", 0);
         }
      }
   }

   private int getPotion() {
      if (this.hp.getValue()) {
         if (this.healthCheck(this.health.getValue().intValue()) && this.hpTimer.passedMs(this.hpDelay.getValue().intValue())) {
            this.preHp = false;
            this.hpTimer.reset();
            int slot = InventoryUtil.getPotion("healing");
            if (slot != -1) {
               return slot;
            }
         }

         if (this.predict.getValue()) {
            this.healthPredict();
         }

         if (this.preHp && this.hpPredictTimer.passedMs(this.predictHpDelay.getValue().intValue())) {
            this.preHp = false;
            this.hpPredictTimer.reset();
            int slot = InventoryUtil.getPotion("healing");
            if (slot != -1) {
               return slot;
            }
         }
      }

      if (this.speed.getValue()
         && (
            !mc.player.isPotionActive(MobEffects.SPEED)
               || Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getDuration() <= this.time.getValue() * 20
         )
         && this.speedTimer.passedMs(this.speedDelay.getValue().intValue())) {
         this.speedTimer.reset();
         return InventoryUtil.getPotion("swiftness");
      } else {
         return -1;
      }
   }

   private int getBadPot() {
      if (this.badPotTimer.passedS(this.delay.getValue().intValue())) {
         this.badPotTimer.reset();

         for (EntityPlayer player : mc.world.playerEntities) {
            if (mc.player.connection.getPlayerInfo(player.getName()) != null
               && !EntityUtil.basicChecksEntity(player)
               && !(mc.player.getDistance(player) > this.range.getValue())) {
               if (this.weak.getValue() && !this.weaknessTime.containsKey(player.getEntityId())) {
                  int slot = InventoryUtil.getPotion("weakness");
                  if (slot != -1) {
                     return slot;
                  }
               }

               if (this.jump.getValue() && !this.jumpBoostTime.containsKey(player.getEntityId())) {
                  int slot = InventoryUtil.getPotion("leaping");
                  if (slot != -1) {
                     return slot;
                  }
               }

               if (this.poison.getValue() && !this.poisonTime.containsKey(player.getEntityId())) {
                  int slot = InventoryUtil.getPotion("poison");
                  if (slot != -1) {
                     return slot;
                  }
               }

               if (this.slow.getValue() && !this.slownessTime.containsKey(player.getEntityId())) {
                  return InventoryUtil.getPotion("slowness");
               }
            }
         }
      }

      return -1;
   }

   private boolean healthCheck(double value) {
      return mc.player.getHealth() < value || this.equal.getValue() && mc.player.getHealth() == value;
   }

   private void healthPredict() {
      double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
      if (health == 36.0) {
         this.lastHealth = 36.0;
      }

      double change = health - this.lastHealth;
      if (!(change >= 0.0)) {
         this.lastHealth = health;
         health += change * this.times.getValue();
         this.preHp = health < this.health.getValue().intValue() || this.equal.getValue() && health == this.health.getValue().intValue();
      }
   }
}
