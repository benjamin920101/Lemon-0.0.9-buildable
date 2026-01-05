package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "BetterTrap", category = Category.Dev)
public class AutoTrap extends Module {
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 10.0);
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 500);
   IntegerSetting retryDelay = this.registerInteger("RetryDelay", 50, 0, 500);
   IntegerSetting blocksPerPlace = this.registerInteger("BlocksPerTick", 8, 1, 30);
   BooleanSetting chest = this.registerBoolean("EnderChest", true);
   BooleanSetting helpBlocks = this.registerBoolean("HelpBlocks", false);
   BooleanSetting only = this.registerBoolean("OnlyUntrapped", true);
   BooleanSetting strict = this.registerBoolean("Strict", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting raytrace = this.registerBoolean("Raytrace", false);
   BooleanSetting antiScaffold = this.registerBoolean("AntiScaffold", false);
   BooleanSetting antiStep = this.registerBoolean("AntiStep", false);
   BooleanSetting noGhost = this.registerBoolean("Packet", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting check = this.registerBoolean("SwitchCheck", false);
   BooleanSetting packet = this.registerBoolean("PacketSwitch", false);
   private final Timing timer = new Timing();
   private final Map<BlockPos, Integer> retries = new HashMap<>();
   private final Timing retryTimer = new Timing();
   public EntityPlayer target;
   private boolean didPlace = false;
   private int lastHotbarSlot;
   private int placements = 0;
   List<BlockPos> posList;

   @Override
   public void onEnable() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.lastHotbarSlot = mc.player.inventory.currentItem;
         this.retries.clear();
      }
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.doTrap();
      }
   }

   private void doTrap() {
      if (!this.check()) {
         this.doStaticTrap();
         if (this.didPlace) {
            this.timer.reset();
         }
      }
   }

   private void doStaticTrap() {
      int obbySlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      int eChestSlot = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
      int slot = this.chest.getValue() ? eChestSlot : (obbySlot == -1 ? eChestSlot : obbySlot);
      if (slot != -1) {
         int originalSlot = mc.player.inventory.currentItem;
         Vec3d[] sides = new Vec3d[]{new Vec3d(0.3, 0.5, 0.3), new Vec3d(-0.3, 0.5, 0.3), new Vec3d(0.3, 0.5, -0.3), new Vec3d(-0.3, 0.5, -0.3)};
         List<Vec3d> placeTargets = new ArrayList<>();

         for (Vec3d vec3d : sides) {
            placeTargets.addAll(
               EntityUtil.targets(
                  this.target.getPositionVector().add(vec3d),
                  this.antiScaffold.getValue(),
                  this.antiStep.getValue(),
                  false,
                  false,
                  false,
                  this.raytrace.getValue()
               )
            );
         }

         this.posList = this.placeList(placeTargets, this.target);
         if (!this.posList.isEmpty()) {
            this.switchTo(slot);

            for (BlockPos pos : this.posList) {
               this.placeBlock(pos);
            }

            this.switchTo(originalSlot);
         }
      }
   }

   private List<BlockPos> placeList(List<Vec3d> list, EntityPlayer target) {
      list.sort(
         (vec3d, vec3d2) -> Double.compare(
            mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z),
            mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)
         )
      );
      List<BlockPos> posList = new ArrayList<>();

      for (Vec3d vec3d3 : list) {
         BlockPos position = new BlockPos(vec3d3);
         if (!this.intersectsWithEntity(position) && BlockUtil.isAir(position)) {
            int placeability = BlockUtil.isPositionPlaceable(position, this.raytrace.getValue());
            if (placeability == 1 && (this.retries.get(position) == null || this.retries.get(position) < 4)) {
               posList.add(position);
               this.retries.put(position, this.retries.get(position) == null ? 1 : this.retries.get(position) + 1);
               this.retryTimer.reset();
            } else {
               if (placeability != 3 && this.helpBlocks.getValue() && position.getY() == Math.round(target.posY) + 1L) {
                  posList.add(position.down());
               }

               posList.add(position);
            }
         }
      }

      posList.sort(Comparator.comparingDouble(pos -> pos.y));
      return posList;
   }

   private void switchTo(int slot) {
      if (slot > -1 && slot < 9 && (!this.check.getValue() || mc.player.inventory.currentItem != slot)) {
         if (this.packet.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         } else {
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
         }
      }
   }

   private boolean check() {
      this.didPlace = false;
      this.placements = 0;
      int obbySlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      int eChestSlot = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
      int slot = this.chest.getValue() ? eChestSlot : (obbySlot == -1 ? eChestSlot : obbySlot);
      if (this.retryTimer.passedMs(this.retryDelay.getValue().intValue())) {
         this.retries.clear();
         this.retryTimer.reset();
      }

      if (slot == -1) {
         return true;
      } else {
         if (mc.player.inventory.currentItem != this.lastHotbarSlot && mc.player.inventory.currentItem != obbySlot) {
            this.lastHotbarSlot = mc.player.inventory.currentItem;
         }

         this.target = this.getTarget(this.range.getValue(), this.only.getValue());
         return this.target == null || !this.timer.passedMs(this.delay.getValue().intValue());
      }
   }

   private EntityPlayer getTarget(double range, boolean trapped) {
      EntityPlayer target = null;
      double distance = Math.pow(range, 2.0) + 1.0;

      for (EntityPlayer player : mc.world.playerEntities) {
         if (EntityUtil.isPlayerValid(player, (float)range)
            && (!trapped || !EntityUtil.isTrapped(player, this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false))
            && !(LemonClient.speedUtil.getPlayerSpeed(player) > 15.0)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (mc.player.getDistanceSq(player) < distance) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)
            && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private void placeBlock(BlockPos pos) {
      if (this.placements < this.blocksPerPlace.getValue()) {
         BlockUtil.placeBlock(
            pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.noGhost.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue()
         );
         this.didPlace = true;
         this.placements++;
      }
   }
}
