package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AntiRegear", category = Category.Combat)
public class AntiRegear extends Module {
   public static AntiRegear INSTANCE;
   DoubleSetting reach = this.registerDouble("Range", 5.5, 0.0, 10.0);
   BooleanSetting packet = this.registerBoolean("Packet Break", false);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   List<BlockPos> selfPlaced = new ArrayList<>();
   public boolean working;
   @EventHandler
   private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
            if (mc.player.getHeldItem(packet.getHand()).getItem() instanceof ItemShulkerBox) {
               this.selfPlaced.add(packet.getPos().offset(packet.getDirection()));
            }
         }
      }
   });

   public AntiRegear() {
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.working = false;
   }

   @Override
   public void fast() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         List<BlockPos> sphere = new ArrayList<>();

         for (EntityPlayer target : PlayerUtil.getNearPlayers(16.0, 10)) {
            for (BlockPos pos : EntityUtil.getSphere(EntityUtil.getEntityPos(target), 6.5, 6.5, false, false, 0)) {
               if (!this.selfPlaced.contains(pos)
                  && mc.world.getBlockState(pos).getBlock() instanceof BlockShulkerBox
                  && mc.player.getDistance(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= this.reach.getValue()) {
                  sphere.add(pos);
               }
            }
         }

         this.working = !sphere.isEmpty();
         Iterator var6 = sphere.iterator();
         if (var6.hasNext()) {
            BlockPos posx = (BlockPos)var6.next();
            if (this.swing.getValue()) {
               mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (this.packet.getValue()) {
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, posx, EnumFacing.UP));
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, posx, EnumFacing.UP));
            } else {
               mc.playerController.onPlayerDamageBlock(posx, EnumFacing.UP);
            }
         }

         this.selfPlaced.removeIf(posxx -> !(mc.world.getBlockState(posxx).getBlock() instanceof BlockShulkerBox));
      } else {
         this.working = false;
      }
   }
}
