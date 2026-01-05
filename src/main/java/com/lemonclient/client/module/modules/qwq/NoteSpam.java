package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "NoteSpam", category = Category.qwq)
public class NoteSpam extends Module {
   ModeSetting timeMode = this.registerMode("Time Mode", Arrays.asList("onUpdate", "Tick", "Fast"), "Fast");
   DoubleSetting range = this.registerDouble("Range", 5.5, 1.0, 10.0);
   IntegerSetting max = this.registerInteger("MaxBlocks", 30, 1, 150);

   @Override
   public void onUpdate() {
      if (this.timeMode.getValue().equalsIgnoreCase("onUpdate")) {
         this.doNoteSpam();
      }
   }

   @Override
   public void onTick() {
      if (this.timeMode.getValue().equalsIgnoreCase("Tick")) {
         this.doNoteSpam();
      }
   }

   @Override
   public void fast() {
      if (this.timeMode.getValue().equalsIgnoreCase("Fast")) {
         this.doNoteSpam();
      }
   }

   private void doNoteSpam() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         int counter = 0;

         for (BlockPos b : EntityUtil.getSphere(PlayerUtil.getPlayerPos(), this.range.getValue(), this.range.getValue(), false, true, 0)) {
            if (BlockUtil.getBlock(b) == Blocks.NOTEBLOCK) {
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, b, EnumFacing.UP));
               if (++counter > this.max.getValue()) {
                  return;
               }
            }
         }
      }
   }
}
