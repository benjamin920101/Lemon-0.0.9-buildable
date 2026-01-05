package com.lemonclient.client.module.modules.dev;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AntiHolePush", category = Category.Dev)
public class AntiHolePush extends Module {
   ModeSetting timeMode = this.registerMode("Time Mode", Arrays.asList("onUpdate", "Tick", "Both", "Fast"), "Fast");
   BooleanSetting packet = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting strict = this.registerBoolean("Strict", true);
   BooleanSetting raytrace = this.registerBoolean("RayTrace", true);
   BooleanSetting trap = this.registerBoolean("Trap", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", false);
   BooleanSetting entityCheck = this.registerBoolean("Entity Check", true);
   BooleanSetting breakPiston = this.registerBoolean("Break Piston", false);

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

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onUpdate() {
      if (this.timeMode.getValue().equalsIgnoreCase("onUpdate") || this.timeMode.getValue().equalsIgnoreCase("Both")) {
         this.block();
      }
   }

   @Override
   public void onTick() {
      if (this.timeMode.getValue().equalsIgnoreCase("Tick") || this.timeMode.getValue().equalsIgnoreCase("Both")) {
         this.block();
      }
   }

   @Override
   public void fast() {
      if (this.timeMode.getValue().equalsIgnoreCase("Fast")) {
         this.block();
      }
   }

   private void block() {
      if (mc.player != null && mc.world != null) {
         BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
         int obsidian = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         if (obsidian != -1) {
            BlockPos head = pos.add(0, 2, 0);
            BlockPos pos1 = pos.add(1, 1, 0);
            BlockPos pos2 = pos.add(-1, 1, 0);
            BlockPos pos3 = pos.add(0, 1, 1);
            BlockPos pos4 = pos.add(0, 1, -1);
            if (this.airBlock(head)) {
               List<BlockPos> posList = new ArrayList<>();
               if (this.isPiston(pos1) && isFacing(pos1, EnumFacing.WEST)) {
                  BlockPos pos5 = pos.add(-1, 2, 0);
                  if (this.airBlock(pos2) && this.airBlock(pos5)) {
                     posList.add(pos2);
                  }

                  if (this.trap.getValue() && this.airBlock(head)) {
                     posList.add(pos2.up());
                     posList.add(head);
                  }

                  if (this.breakPiston.getValue()) {
                     mc.playerController.onPlayerDamageBlock(pos1, BlockUtil.getRayTraceFacing(pos3));
                  }
               }

               if (this.isPiston(pos2) && isFacing(pos2, EnumFacing.EAST)) {
                  BlockPos pos6 = pos.add(1, 2, 0);
                  if (this.airBlock(pos1) && this.airBlock(pos6)) {
                     posList.add(pos1);
                  }

                  if (this.trap.getValue() && this.airBlock(head)) {
                     posList.add(pos1.up());
                     posList.add(head);
                  }

                  if (this.breakPiston.getValue()) {
                     mc.playerController.onPlayerDamageBlock(pos2, BlockUtil.getRayTraceFacing(pos3));
                  }
               }

               if (this.isPiston(pos3) && isFacing(pos3, EnumFacing.NORTH)) {
                  BlockPos pos7 = pos.add(0, 2, -1);
                  if (this.airBlock(pos4) && this.airBlock(pos7)) {
                     posList.add(pos4);
                  }

                  if (this.trap.getValue() && this.airBlock(head)) {
                     posList.add(pos4.up());
                     posList.add(head);
                  }

                  if (this.breakPiston.getValue()) {
                     mc.playerController.onPlayerDamageBlock(pos3, BlockUtil.getRayTraceFacing(pos3));
                  }
               }

               if (this.isPiston(pos4) && isFacing(pos4, EnumFacing.SOUTH)) {
                  BlockPos pos8 = pos.add(0, 2, 1);
                  if (this.airBlock(pos3) && this.airBlock(pos8)) {
                     posList.add(pos3);
                  }

                  if (this.trap.getValue() && this.airBlock(head)) {
                     posList.add(pos3.up());
                     posList.add(head);
                  }

                  if (this.breakPiston.getValue()) {
                     mc.playerController.onPlayerDamageBlock(pos4, BlockUtil.getRayTraceFacing(pos3));
                  }
               }

               if (!posList.isEmpty()) {
                  this.switchTo(obsidian, () -> {
                     for (BlockPos placePos : posList) {
                        this.perform(placePos);
                     }
                  });
               }
            }
         }
      }
   }

   private IBlockState getBlock(BlockPos block) {
      return mc.world.getBlockState(block);
   }

   private boolean airBlock(BlockPos pos) {
      return BlockUtil.airBlocks.contains(this.getBlock(pos).getBlock());
   }

   private void perform(BlockPos pos) {
      if ((!this.entityCheck.getValue() || !this.intersectsWithEntity(pos)) && BlockUtil.canPlace(pos, this.strict.getValue(), this.raytrace.getValue())) {
         BlockUtil.placeBlock(
            pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue()
         );
      }
   }

   public static boolean isFacing(BlockPos pos, EnumFacing enumFacing) {
      ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
      UnmodifiableIterator var3 = properties.keySet().iterator();

      while (var3.hasNext()) {
         IProperty<?> prop = (IProperty<?>)var3.next();
         if (prop.getValueClass() == EnumFacing.class
            && (prop.getName().equals("facing") || prop.getName().equals("rotation"))
            && properties.get(prop) == enumFacing) {
            return true;
         }
      }

      return false;
   }

   private boolean isPiston(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() instanceof BlockPistonMoving
         || mc.world.getBlockState(pos).getBlock() instanceof BlockPistonBase
         || mc.world.getBlockState(pos).getBlock() == Blocks.PISTON
         || mc.world.getBlockState(pos).getBlock() == Blocks.STICKY_PISTON;
   }
}
