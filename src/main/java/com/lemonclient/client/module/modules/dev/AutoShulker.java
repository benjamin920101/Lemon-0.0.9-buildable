package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.DeathEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoShulker", category = Category.Dev)
public class AutoShulker extends Module {
   BooleanSetting once = this.registerBoolean("Once", false);
   IntegerSetting counts = this.registerInteger("EmptySlots", 6, 1, 36, () -> !this.once.getValue());
   BooleanSetting disable = this.registerBoolean("Disable After Death", true, () -> !this.once.getValue());
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 10.0);
   DoubleSetting yRange = this.registerDouble("YRange", 5.0, 0.0, 10.0);
   DoubleSetting targetRange = this.registerDouble("Target Range", 8.0, 0.0, 16.0);
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 5, 0, 10);
   IntegerSetting openDelay = this.registerInteger("Open Delay", 5, 0, 10);
   BooleanSetting inventory = this.registerBoolean("Inventory", true);
   IntegerSetting Slot = this.registerInteger("Slot", 1, 1, 9);
   BooleanSetting packetPlace = this.registerBoolean("Packet Place", true);
   BooleanSetting placeSwing = this.registerBoolean("Place Swing", true);
   BooleanSetting packetSwing = this.registerBoolean("Packet Swing", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   private int delayTimeTicks;
   BlockPos playerPos;
   AutoShulker.ShulkerPos blockAim;
   List<BlockPos> list = new ArrayList<>();
   int slot;
   boolean swapped = false;
   int tick = 0;
   @EventHandler
   private final Listener<DeathEvent> deathEventListener = new Listener<>(event -> {
      if (event.player == mc.player && this.disable.getValue()) {
         this.disable();
      }
   });

   private void switchTo(int slot, Runnable runnable) {
      int oldslot = mc.player.inventory.currentItem;
      if (slot >= 0 && slot != oldslot) {
         if (slot < 9) {
            boolean packetSwitch = this.packetSwitch.getValue();
            if (packetSwitch) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            } else {
               mc.player.inventory.currentItem = slot;
            }

            runnable.run();
            if (packetSwitch) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            } else {
               mc.player.inventory.currentItem = oldslot;
            }
         }
      } else {
         runnable.run();
      }
   }

   private int getShulkerSlot() {
      for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
         if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock
            && ((ItemBlock)mc.player.inventory.getStackInSlot(i).getItem()).getBlock() instanceof BlockShulkerBox) {
            return i;
         }
      }

      return -1;
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   private void initValues() {
      List<BlockPos> blocks = EntityUtil.getSphere(PlayerUtil.getEyesPos(), this.range.getValue() + 1.0, this.yRange.getValue() + 1.0, false, true, 0);
      blocks.removeIf(p -> ColorMain.INSTANCE.breakList.contains(p) || this.list.contains(p));
      List<AutoShulker.ShulkerPos> posList = new ArrayList<>();
      blocks.forEach(pos -> {
         EnumFacing facing = this.getFacing(pos);
         if (facing != null) {
            BlockPos neighbour = pos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            if (this.inRange(hitVec)) {
               posList.add(new AutoShulker.ShulkerPos(pos, facing, neighbour, opposite, hitVec));
            }
         }
      });
      EntityPlayer target = PlayerUtil.getNearestPlayer(12.0);
      if (target == null) {
         this.blockAim = posList.stream().min(Comparator.comparing(p -> p.getRange(mc.player))).orElse(null);
      } else {
         this.blockAim = posList.stream().max(Comparator.comparing(p -> this.getWeight(p, target))).orElse(null);
      }

      if (this.blockAim != null) {
         this.list.add(this.blockAim.pos);
      }
   }

   private double getWeight(AutoShulker.ShulkerPos pos, EntityPlayer target) {
      double range = pos.getRange(target);
      if (range >= this.targetRange.getValue()) {
         int y = 256 - pos.pos.getY();
         range += y * 100;
      }

      return range;
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private EnumFacing getFacing(BlockPos pos) {
      if (!this.intersectsWithEntity(pos) && (BlockUtil.canReplace(pos) || BlockUtil.getBlock(pos) instanceof BlockShulkerBox)) {
         for (EnumFacing facing : EnumFacing.VALUES) {
            if (BlockUtil.canBeClicked(pos.offset(facing))
               && BlockUtil.airBlocks.contains(mc.world.getBlockState(pos.offset(facing, -1)).getBlock())) {
               return facing;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean inRange(Vec3d vec) {
      double x = vec.x - mc.player.posX;
      double z = vec.z - mc.player.posZ;
      double y = vec.y - PlayerUtil.getEyesPos().y;
      double add = Math.sqrt(y * y) / 2.0;
      return x * x + z * z <= (this.range.getValue() - add) * (this.range.getValue() - add) && y * y <= this.yRange.getValue() * this.yRange.getValue();
   }

   private boolean inRange(BlockPos pos) {
      double x = pos.x + 0.5 - mc.player.posX;
      double z = pos.z + 0.5 - mc.player.posZ;
      double y = pos.y + 0.5 - PlayerUtil.getEyesPos().y;
      double add = Math.sqrt(y * y) / 2.0;
      return x * x + z * z <= (this.range.getValue() - add) * (this.range.getValue() - add) && y * y <= this.yRange.getValue() * this.yRange.getValue();
   }

   @Override
   public void onUpdate() {
      if (mc.player != null) {
         if (this.tick++ >= this.openDelay.getValue()) {
            if (this.blockAim != null && !BlockUtil.isAir(this.blockAim.pos) && !BlockUtil.canReplace(this.blockAim.pos)) {
               this.openBlock();
            }

            this.tick = 0;
         }

         if (mc.currentScreen instanceof GuiShulkerBox) {
            if (this.once.getValue()) {
               this.disable();
            }

            this.blockAim = null;
         } else if (this.delayTimeTicks++ >= this.tickDelay.getValue()) {
            this.delayTimeTicks = 0;
            if ((this.slot = this.getShulkerSlot()) != -1) {
               if (!this.once.getValue() && InventoryUtil.getEmptyCounts() < this.counts.getValue()) {
                  this.checkPos();
               } else if (this.blockAim == null) {
                  this.initValues();
               }

               if (this.blockAim == null) {
                  if (this.once.getValue()) {
                     this.disable();
                  }
               } else if (!this.inRange(this.blockAim.pos)) {
                  this.blockAim = null;
               } else {
                  if (this.slot > 8 && !this.swapped) {
                     if (!this.inventory.getValue()) {
                        return;
                     }

                     mc.playerController.windowClick(0, this.slot, this.Slot.getValue(), ClickType.SWAP, mc.player);
                     mc.playerController.updateController();
                     this.swapped = true;
                     if (this.tickDelay.getValue() != 0) {
                        return;
                     }
                  }

                  if (!BlockUtil.isAir(this.blockAim.pos) && !BlockUtil.canReplace(this.blockAim.pos)) {
                     this.openBlock();
                  } else {
                     this.switchTo(
                        this.slot,
                        () -> {
                           boolean sneak = false;
                           if (BlockUtil.blackList.contains(mc.world.getBlockState(this.blockAim.neighbour).getBlock())
                              && !mc.player.isSneaking()) {
                              mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                              sneak = true;
                           }

                           BurrowUtil.rightClickBlock(
                              this.blockAim.neighbour, this.blockAim.vec, EnumHand.MAIN_HAND, this.blockAim.opposite, this.packetPlace.getValue()
                           );
                           if (sneak) {
                              mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                           }

                           if (this.placeSwing.getValue()) {
                              this.swing();
                           }

                           this.tick = 0;
                        }
                     );
                     if (this.tickDelay.getValue() == 0) {
                        this.openBlock();
                     }
                  }
               }
            }
         }
      }
   }

   private void checkPos() {
      if (!this.isPos2(PlayerUtil.getPlayerPos(), this.playerPos)) {
         this.list = new ArrayList<>();
         this.playerPos = PlayerUtil.getPlayerPos();
      }
   }

   private void swing() {
      if (this.packetSwing.getValue()) {
         mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
      } else {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   private void openBlock() {
      EnumFacing side = EnumFacing.getDirectionFromEntityLiving(this.blockAim.pos, mc.player);
      BlockPos neighbour = this.blockAim.pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
      mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      mc.playerController.processRightClickBlock(mc.player, mc.world, this.blockAim.pos, opposite, hitVec, EnumHand.MAIN_HAND);
   }

   @Override
   public void onEnable() {
      this.blockAim = null;
      this.checkPos();
   }

   static class ShulkerPos {
      BlockPos pos;
      EnumFacing facing;
      Vec3d vec;
      BlockPos neighbour;
      EnumFacing opposite;

      public ShulkerPos(BlockPos pos, EnumFacing facing, BlockPos neighbour, EnumFacing opposite, Vec3d vec3d) {
         this.pos = pos;
         this.facing = facing;
         this.neighbour = neighbour;
         this.opposite = opposite;
         this.vec = vec3d;
      }

      public double getRange(EntityPlayer player) {
         return player.getDistance(this.pos.x + 0.5, this.pos.y + 0.5, this.pos.z + 0.5);
      }
   }
}
