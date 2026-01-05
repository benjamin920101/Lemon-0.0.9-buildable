package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBed;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@Module.Declaration(name = "SelfBed", category = Category.Dev, priority = 999)
public class SelfBed extends Module {
   ModeSetting page = this.registerMode("Page", Arrays.asList("General", "Calc"), "General");
   BooleanSetting packetPlace = this.registerBoolean("Packet Place", true, () -> this.page.getValue().equals("General"));
   BooleanSetting placeSwing = this.registerBoolean("Place Swing", true, () -> this.page.getValue().equals("General"));
   BooleanSetting breakSwing = this.registerBoolean("Break Swing", true, () -> this.page.getValue().equals("General"));
   BooleanSetting packetSwing = this.registerBoolean("Packet Swing", true, () -> this.page.getValue().equals("General"));
   BooleanSetting highVersion = this.registerBoolean("1.13", true, () -> this.page.getValue().equals("General"));
   BooleanSetting autoSwitch = this.registerBoolean("Auto Switch", true, () -> this.page.getValue().equals("General"));
   BooleanSetting update = this.registerBoolean("Update", true, () -> this.page.getValue().equals("General"));
   BooleanSetting silentSwitch = this.registerBoolean("Switch Back", true, () -> this.page.getValue().equals("General") && this.autoSwitch.getValue());
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("General"));
   IntegerSetting calcDelay = this.registerInteger("Calc Delay", 0, 0, 1000, () -> this.page.getValue().equals("Calc"));
   IntegerSetting placeDelay = this.registerInteger("Place Delay", 0, 0, 1000, () -> this.page.getValue().equals("Calc"));
   IntegerSetting breakDelay = this.registerInteger("Break Delay", 0, 0, 1000, () -> this.page.getValue().equals("Calc"));
   DoubleSetting range = this.registerDouble("Place Range", 5.0, 0.0, 10.0, () -> this.page.getValue().equals("Calc"));
   DoubleSetting yRange = this.registerDouble("Y Range", 2.5, 0.0, 10.0, () -> this.page.getValue().equals("Calc"));
   ModeSetting handMode = this.registerMode("Hand", Arrays.asList("Main", "Off", "Auto"), "Auto", () -> this.page.getValue().equals("Calc"));
   DoubleSetting maxDmg = this.registerDouble("Max Self Dmg", 10.0, 0.0, 20.0, () -> this.page.getValue().equals("Calc"));
   BooleanSetting antiSuicide = this.registerBoolean("Anti Suicide", true, () -> this.page.getValue().equals("Calc"));
   BlockPos headPos;
   BlockPos basePos;
   float damage;
   float selfDamage;
   String face;
   Timing basetiming = new Timing();
   Timing calctiming = new Timing();
   Timing placetiming = new Timing();
   Timing breaktiming = new Timing();
   EnumHand hand;
   int slot;
   Vec2f rotation;
   int nowSlot;
   @EventHandler
   private final Listener<PacketEvent.Send> postSendListener = new Listener<>(event -> {
      if (event.getPacket() instanceof CPacketHeldItemChange) {
         this.nowSlot = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
      }
   });

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.world != null && !EntityUtil.isDead(mc.player) && !this.inNether()) {
         this.calc();
      } else {
         this.headPos = this.basePos = null;
         this.damage = this.selfDamage = 0.0F;
         this.rotation = null;
      }
   }

   @Override
   public void fast() {
      if (mc.player != null && mc.world != null && !EntityUtil.isDead(mc.player) && !this.inNether()) {
         if (mc.player.movementInput.moveForward != 0.0F || mc.player.movementInput.moveStrafe != 0.0F) {
            this.bedaura();
         }
      }
   }

   private void bedaura() {
      if (this.headPos != null && this.basePos != null) {
         if (this.isBed(this.headPos) || this.isBed(this.basePos)) {
            this.breakBed();
         }

         this.place();
         this.breakBed();
      }
   }

   private void calc() {
      if (this.calctiming.passedMs(this.calcDelay.getValue().intValue())) {
         this.calctiming.reset();
         this.headPos = this.basePos = null;
         this.damage = this.selfDamage = 0.0F;
         this.rotation = null;
         if (mc.player.movementInput.moveForward == 0.0F && mc.player.movementInput.moveStrafe == 0.0F) {
            return;
         }

         boolean offhand = !this.handMode.getValue().equals("Main") && mc.player.getHeldItemOffhand().getItem() == Items.BED;
         if (!offhand && !this.handMode.getValue().equals("Off")) {
            this.slot = BurrowUtil.findHotbarBlock(ItemBed.class);
            if (this.slot == -1) {
               return;
            }
         }

         this.hand = offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
         BlockPos bedPos = this.findBlocksExcluding();
         if (bedPos == null) {
            return;
         }

         this.headPos = bedPos;
         if (mc.player.getHorizontalFacing().equals(EnumFacing.SOUTH)) {
            this.face = "SOUTH";
            this.rotation = new Vec2f(0.0F, 90.0F);
            bedPos = new BlockPos(this.headPos.x, this.headPos.y, this.headPos.z - 1);
         } else if (mc.player.getHorizontalFacing().equals(EnumFacing.WEST)) {
            this.face = "WEST";
            this.rotation = new Vec2f(90.0F, 90.0F);
            bedPos = new BlockPos(this.headPos.x + 1, this.headPos.y, this.headPos.z);
         } else if (mc.player.getHorizontalFacing().equals(EnumFacing.NORTH)) {
            this.face = "NORTH";
            this.rotation = new Vec2f(180.0F, 90.0F);
            bedPos = new BlockPos(this.headPos.x, this.headPos.y, this.headPos.z + 1);
         } else {
            this.face = "EAST";
            this.rotation = new Vec2f(-90.0F, 90.0F);
            bedPos = new BlockPos(this.headPos.x - 1, this.headPos.y, this.headPos.z);
         }

         if (!this.block(bedPos, true)) {
            this.headPos = this.basePos = null;
            this.damage = this.selfDamage = 0.0F;
            this.rotation = null;
            return;
         }

         this.headPos = this.headPos.up();
         this.basePos = bedPos.up();
      }
   }

   private void place() {
      if (this.placetiming.passedMs(this.placeDelay.getValue().intValue())) {
         BlockPos neighbour = this.basePos.down();
         EnumFacing opposite = EnumFacing.DOWN.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         boolean sneak = false;
         if (BlockUtil.blackList.contains(mc.world.getBlockState(neighbour).getBlock()) && !mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            sneak = true;
         }

         this.run(() -> BurrowUtil.rightClickBlock(neighbour, hitVec, this.hand, opposite, this.packetPlace.getValue()), this.slot);
         if (this.placeSwing.getValue()) {
            this.swing(this.hand);
         }

         if (sneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }

         this.placetiming.reset();
      }
   }

   private void run(Runnable runnable, int slot) {
      if (this.hand == EnumHand.OFF_HAND) {
         runnable.run();
      } else {
         int oldSlot = mc.player.inventory.currentItem;
         if (slot != oldSlot) {
            if (this.autoSwitch.getValue()) {
               this.switchTo(slot);
               if (this.nowSlot == slot || mc.player.getHeldItemMainhand().getItem() == Items.BED) {
                  runnable.run();
               }

               if (this.silentSwitch.getValue()) {
                  this.switchTo(oldSlot);
               }
            }
         } else {
            runnable.run();
         }
      }
   }

   private void breakBed() {
      if (this.breaktiming.passedMs(this.breakDelay.getValue().intValue())) {
         EnumFacing side = EnumFacing.UP;
         if (ModuleManager.getModule(ColorMain.class).sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }

         Vec3d facing = this.getHitVecOffset(side);
         if (this.isBed(this.headPos) && !this.isBed(this.basePos)) {
            mc.player
               .connection
               .sendPacket(
                  new CPacketPlayerTryUseItemOnBlock(
                     this.headPos, side, this.hand, (float)facing.x, (float)facing.y, (float)facing.z
                  )
               );
         } else {
            mc.player
               .connection
               .sendPacket(
                  new CPacketPlayerTryUseItemOnBlock(
                     this.basePos, side, this.hand, (float)facing.x, (float)facing.y, (float)facing.z
                  )
               );
         }

         if (this.breakSwing.getValue()) {
            this.swing(this.hand);
         }

         this.breaktiming.reset();
      }
   }

   private BlockPos findBlocksExcluding() {
      double x = mc.player.prevPosX;
      double z = mc.player.prevPosZ;
      double dX = mc.player.posX - x;
      double dZ = mc.player.posZ - z;
      List<BlockPos> posList = new ArrayList<>();

      for (int y : new int[]{-3, -2, -1, 0, 1, 2}) {
         posList.addAll(
            EntityUtil.getSphere(PlayerUtil.getEyesPos(), this.range.getValue() + 1.0, 1.0, false, false, y)
               .stream()
               .filter(
                  p -> (mc.player.posX - x) * (mc.player.posX - p.x) > 0.0
                     && (mc.player.posZ - z) * (mc.player.posZ - p.z) > 0.0
               )
               .filter(this::canPlaceBed)
               .filter(p -> (x - p.x) * dX >= 0.0 && (z - p.z) * dZ >= 0.0)
               .filter(
                  p -> {
                     double dmg = DamageUtil.calculateDamage(
                        mc.player,
                        mc.player.getPositionVector(),
                        mc.player.getEntityBoundingBox(),
                        p.x + 0.5,
                        p.y + 1.5625,
                        p.z + 0.5,
                        5.0F,
                        "Bed"
                     );
                     return dmg <= this.maxDmg.getValue() && (!this.antiSuicide.getValue() || dmg <= EntityUtil.getHealth(mc.player) + 1.0F);
                  }
               )
               .collect(Collectors.toList())
         );
      }

      return posList.stream().min(Comparator.comparing(mc.player::getDistanceSq)).orElse(null);
   }

   private boolean canPlaceBed(BlockPos blockPos) {
      if (!this.block(blockPos, false)) {
         return false;
      } else {
         BlockPos pos = blockPos.offset(mc.player.getHorizontalFacing(), -1);
         return this.block(pos, true) && this.inRange(pos.up());
      }
   }

   private boolean block(BlockPos pos, boolean rangeCheck) {
      if (!this.space(pos.up())) {
         return false;
      } else if (BlockUtil.canReplace(pos)) {
         return false;
      } else {
         return !this.highVersion.getValue() && !this.solid(pos) ? false : !rangeCheck || this.inRange(pos.up());
      }
   }

   private boolean isBed(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      return block == Blocks.BED || block instanceof BlockBed;
   }

   private boolean space(BlockPos pos) {
      return mc.world.isAirBlock(pos) || mc.world.getBlockState(pos).getBlock() == Blocks.BED;
   }

   private boolean solid(BlockPos pos) {
      return !BlockUtil.isBlockUnSolid(pos)
         && !(mc.world.getBlockState(pos).getBlock() instanceof BlockBed)
         && mc.world.getBlockState(pos).isSideSolid(mc.world, pos, EnumFacing.UP);
   }

   private boolean inRange(BlockPos pos) {
      double x = pos.x - mc.player.posX;
      double z = pos.z - mc.player.posZ;
      double y = pos.y - PlayerUtil.getEyesPos().y;
      double add = Math.sqrt(y * y) / 2.0;
      return x * x + z * z <= (this.range.getValue() - add) * (this.range.getValue() - add) && y * y <= this.yRange.getValue() * this.yRange.getValue();
   }

   private Vec3d getHitVecOffset(EnumFacing face) {
      Vec3i vec = face.getDirectionVec();
      return new Vec3d(vec.x * 0.5F + 0.5F, vec.y * 0.5F + 0.5F, vec.z * 0.5F + 0.5F);
   }

   private void switchTo(int slot) {
      if (slot > -1 && slot < 9) {
         if (this.packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         } else {
            mc.player.inventory.currentItem = slot;
         }

         if (this.update.getValue()) {
            mc.playerController.updateController();
         }
      }
   }

   private void swing(EnumHand hand) {
      if (this.packetSwing.getValue()) {
         mc.player.connection.sendPacket(new CPacketAnimation(hand));
      } else {
         mc.player.swingArm(hand);
      }
   }

   private boolean inNether() {
      return mc.player.dimension == 0;
   }

   @Override
   public void onEnable() {
      this.calctiming.reset();
      this.basetiming.reset();
      this.placetiming.reset();
      this.breaktiming.reset();
   }
}
