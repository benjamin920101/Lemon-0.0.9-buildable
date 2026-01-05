package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.Arrays;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoMineBurrow", category = Category.Combat)
public class AntiBurrow extends Module {
   public static AntiBurrow INSTANCE;
   ModeSetting breakBlock = this.registerMode("Break Block", Arrays.asList("Normal", "Packet"), "Packet");
   DoubleSetting balance = this.registerDouble("Reduce", 0.24, 0.0, 0.5);
   BooleanSetting up = this.registerBoolean("Head", true);
   BooleanSetting down = this.registerBoolean("Feet", true);
   BooleanSetting first = this.registerBoolean("Head First", false, () -> this.up.getValue());
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting ignore = this.registerBoolean("Ignore Bed", false);
   BooleanSetting ignorePiston = this.registerBoolean("Ignore Piston", false);
   BooleanSetting ignoreWeb = this.registerBoolean("Ignore Web", false);
   BooleanSetting fire = this.registerBoolean("Fire", false);
   BooleanSetting sand = this.registerBoolean("Falling Blocks", false);
   BooleanSetting rail = this.registerBoolean("Rail", false);
   IntegerSetting range = this.registerInteger("Range", 5, 0, 10);
   BooleanSetting doubleMine = this.registerBoolean("Double Mine", false);
   public double yaw;
   public double pitch;
   public boolean mining;
   @EventHandler
   private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
      if (this.rotate.getValue() && this.mining) {
         if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            packet.yaw = (float)this.yaw;
            packet.pitch = (float)this.pitch;
         }
      }
   });
   public static final List<Block> airBlocks = Arrays.asList(
      Blocks.AIR, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.GRASS
   );

   public AntiBurrow() {
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.mining = false;
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.mining = false;
         if (!AntiRegear.INSTANCE.working) {
            BlockPos instantPos = null;
            if (ModuleManager.isModuleEnabled(PacketMine.class)) {
               instantPos = PacketMine.INSTANCE.packetPos;
            }

            if (instantPos != null) {
               if (instantPos.equals(new BlockPos(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ))) {
                  return;
               }

               if (instantPos.equals(new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ))) {
                  return;
               }

               if (mc.world.getBlockState(instantPos).getBlock() == Blocks.WEB) {
                  return;
               }
            }

            BlockPos pos = this.getCityPos(null);
            if (pos != null) {
               this.mining = true;
               if (this.doubleMine.getValue()) {
                  BlockPos doublePos = null;
                  if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                     doublePos = PacketMine.INSTANCE.doublePos;
                  }

                  if (doublePos == null) {
                     this.doBreak(this.getCityPos(pos));
                  }
               }

               double[] rotate = EntityUtil.calculateLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, mc.player);
               this.yaw = rotate[0];
               this.pitch = rotate[1];
               this.doBreak(pos);
            }
         }
      }
   }

   public BlockPos getCityPos(BlockPos pos) {
      EntityPlayer player = PlayerUtil.getNearestPlayer(this.range.getValue().intValue());
      if (player == null) {
         return null;
      } else {
         Vec3d[] sides = new Vec3d[]{
            new Vec3d(this.balance.getValue(), 0.0, this.balance.getValue()),
            new Vec3d(-this.balance.getValue(), 0.0, this.balance.getValue()),
            new Vec3d(this.balance.getValue(), 0.0, -this.balance.getValue()),
            new Vec3d(-this.balance.getValue(), 0.0, -this.balance.getValue())
         };
         if (this.first.getValue() && this.up.getValue()) {
            for (int x = 1; x > -1 && (this.down.getValue() || x != 0); x--) {
               for (Vec3d side : sides) {
                  BlockPos burrowPos = new BlockPos(
                     player.posX + side.x, player.posY + x, player.posZ + side.z
                  );
                  if (this.intersect(player, burrowPos) && !this.isPos2(burrowPos, pos) && this.burrow(burrowPos)) {
                     return burrowPos;
                  }
               }
            }
         } else {
            for (int x = this.down.getValue() ? 0 : 1; x < 2 && (this.up.getValue() || x != 1); x++) {
               for (Vec3d sidex : sides) {
                  BlockPos burrowPos = new BlockPos(
                     player.posX + sidex.x, player.posY + x, player.posZ + sidex.z
                  );
                  if (this.intersect(player, burrowPos) && !this.isPos2(burrowPos, pos) && this.burrow(burrowPos)) {
                     return burrowPos;
                  }
               }
            }
         }

         return null;
      }
   }

   private boolean burrow(BlockPos pos) {
      return !airBlocks.contains(mc.world.getBlockState(pos).getBlock())
         && BlockUtil.getBlock(pos).blockHardness >= 0.0F
         && (!this.ignore.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.BED)
         && (!this.ignorePiston.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.PISTON_HEAD)
         && (!this.ignoreWeb.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.WEB)
         && (this.fire.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.FIRE)
         && (
            this.rail.getValue()
               || mc.world.getBlockState(pos).getBlock() != Blocks.RAIL
                  && mc.world.getBlockState(pos).getBlock() != Blocks.ACTIVATOR_RAIL
                  && mc.world.getBlockState(pos).getBlock() != Blocks.DETECTOR_RAIL
                  && mc.world.getBlockState(pos).getBlock() != Blocks.GOLDEN_RAIL
         )
         && (
            this.sand.getValue()
               || mc.world.getBlockState(pos).getBlock() != Blocks.SAND
                  && mc.world.getBlockState(pos).getBlock() != Blocks.GRAVEL
                  && mc.world.getBlockState(pos).getBlock() != Blocks.ANVIL
                  && !(mc.world.getBlockState(pos).getBlock() instanceof BlockConcretePowder)
         );
   }

   private void doBreak(BlockPos pos) {
      if (pos == null) {
         this.mining = false;
      } else {
         BlockPos doublePos = null;
         BlockPos instantPos = null;
         if (ModuleManager.isModuleEnabled(PacketMine.class)) {
            instantPos = PacketMine.INSTANCE.packetPos;
            doublePos = PacketMine.INSTANCE.doublePos;
         }

         if (!this.isPos2(instantPos, pos) && !this.isPos2(doublePos, pos)) {
            if (this.swing.getValue()) {
               mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (this.breakBlock.getValue().equals("Packet")) {
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
            } else {
               mc.playerController.onPlayerDamageBlock(pos, EnumFacing.UP);
            }
         }
      }
   }

   private boolean intersect(EntityPlayer player, BlockPos pos) {
      return player.boundingBox.intersects(mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos));
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }
}
