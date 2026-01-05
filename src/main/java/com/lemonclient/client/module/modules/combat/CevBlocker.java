package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "CevBlocker", category = Category.Combat)
public class CevBlocker extends Module {
   ModeSetting time = this.registerMode("Time Mode", Arrays.asList("Tick", "onUpdate", "Both", "Fast"), "Tick");
   BooleanSetting high = this.registerBoolean("High Cev", true);
   BooleanSetting pa = this.registerBoolean("Ignore Bedrock", true);
   BooleanSetting bevel = this.registerBoolean("Bevel", true);
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   private List<BlockPos> cevPositions = new ArrayList<>();

   private void switchTo(int slot, Runnable runnable) {
      int oldslot = mc.player.inventory.currentItem;
      if (slot >= 0 && slot != oldslot) {
         if (slot < 9) {
            boolean packetSwitch = this.packetSwitch.getValue();
            if (packetSwitch) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            } else {
               mc.player.inventory.currentItem = slot;
               mc.playerController.updateController();
            }

            runnable.run();
            if (packetSwitch) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            } else {
               mc.player.inventory.currentItem = oldslot;
               mc.playerController.updateController();
            }
         }
      } else {
         runnable.run();
      }
   }

   @Override
   public void onUpdate() {
      if (this.time.getValue().equals("onUpdate") || this.time.getValue().equals("Both")) {
         this.doBlock();
      }
   }

   @Override
   public void onTick() {
      if (this.time.getValue().equals("Tick") || this.time.getValue().equals("Both")) {
         this.doBlock();
      }
   }

   @Override
   public void fast() {
      if (this.time.getValue().equals("Fast")) {
         this.doBlock();
      }
   }

   private void doBlock() {
      if (mc.world != null && mc.player != null) {
         BlockPos[] highpos = new BlockPos[]{
            new BlockPos(0, 3, 0), new BlockPos(0, 4, 0), new BlockPos(1, 2, 0), new BlockPos(-1, 2, 0), new BlockPos(0, 2, 1), new BlockPos(0, 2, -1)
         };
         BlockPos[] hight2 = new BlockPos[]{new BlockPos(1, 2, 1), new BlockPos(1, 2, -1), new BlockPos(-1, 2, 1), new BlockPos(-1, 2, -1)};
         BlockPos[] offsets = new BlockPos[]{
            new BlockPos(0, 2, 0), new BlockPos(1, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, -1)
         };
         BlockPos[] offsets2 = new BlockPos[]{new BlockPos(1, 1, 1), new BlockPos(1, 1, -1), new BlockPos(-1, 1, 1), new BlockPos(-1, 1, -1)};

         for (BlockPos offset : offsets) {
            this.check(offset);
         }

         if (this.high.getValue()) {
            for (BlockPos offset : highpos) {
               this.check(offset);
            }
         }

         if (this.bevel.getValue()) {
            for (BlockPos offset : offsets2) {
               this.check(offset);
            }

            if (this.high.getValue()) {
               for (BlockPos offset : hight2) {
                  this.check(offset);
               }
            }
         }

         Iterator<BlockPos> iterator = this.cevPositions.iterator();

         while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (Objects.isNull(this.getCrystal(pos))) {
               int obby = BurrowUtil.findHotbarBlock(BlockObsidian.class);
               if (obby == -1) {
                  return;
               }

               this.switchTo(
                  obby,
                  () -> {
                     if (mc.world.isAirBlock(pos)) {
                        BurrowUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                        BurrowUtil.placeBlock(
                           pos.up(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                        );
                     } else {
                        BurrowUtil.placeBlock(
                           pos.up(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                        );
                     }
                  }
               );
               iterator.remove();
            }
         }
      }
   }

   public void check(BlockPos offset) {
      BlockPos playerPos = PlayerUtil.getPlayerPos();
      BlockPos offsetPos = playerPos.add(offset);
      Entity crystal = this.getCrystal(offsetPos);
      if (!Objects.isNull(crystal)) {
         BlockPos crystalPos = EntityUtil.getEntityPos(crystal).down();
         if (!this.pa.getValue()
            || mc.world.isAirBlock(crystalPos)
            || mc.world.getBlockState(crystalPos).getBlock() == Blocks.OBSIDIAN) {
            if (!mc.world.isAirBlock(playerPos.up().up())) {
               mc.player
                  .connection
                  .sendPacket(new Position(mc.player.posX, playerPos.getY() + 0.2, mc.player.posZ, false));
            }

            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            if (!this.cevPositions.contains(crystalPos)) {
               this.cevPositions.add(crystalPos);
            }
         }
      }
   }

   private Entity getCrystal(BlockPos pos) {
      return mc.world
         .loadedEntityList
         .stream()
         .filter(e -> e instanceof EntityEnderCrystal)
         .filter(e -> EntityUtil.getEntityPos(e).down().equals(pos))
         .min(Comparator.comparing(this::getDistance))
         .orElse(null);
   }

   public double getDistance(Entity e) {
      return mc.player.getDistance(e);
   }

   @Override
   public void onDisable() {
      this.cevPositions = new ArrayList<>();
   }
}
