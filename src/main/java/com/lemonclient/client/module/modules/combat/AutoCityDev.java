package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.dev.BedCevBreaker;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoCity", category = Category.Combat)
public class AutoCityDev extends Module {
   public static AutoCityDev INSTANCE;
   ModeSetting breakBlock = this.registerMode("Break Block", Arrays.asList("Normal", "Packet"), "Packet");
   IntegerSetting range = this.registerInteger("Range", 6, 0, 10);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting ignore = this.registerBoolean("Ignore Bed", false);
   public boolean working;
   float pitch;
   float yaw;
   BlockPos blockMine;
   @EventHandler
   private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
      if (this.rotate.getValue()) {
         if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            packet.yaw = this.yaw;
            packet.pitch = this.pitch;
         }
      }
   });

   public AutoCityDev() {
      INSTANCE = this;
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.working = false;
         if (!AntiBurrow.INSTANCE.mining && !AntiRegear.INSTANCE.working && !CevBreaker.INSTANCE.working && !BedCevBreaker.INSTANCE.working) {
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

               if (this.blockMine != null && !isPos2(this.blockMine, instantPos)) {
                  this.blockMine = null;
               }
            }

            EntityPlayer aimTarget = PlayerUtil.getNearestPlayer(this.range.getValue() + 2);
            if (aimTarget != null) {
               BlockPos[] offsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
               BlockPos playerPos = EntityUtil.getEntityPos(aimTarget);
               if (this.blockMine != null) {
                  if (mc.player
                        .getDistance(this.blockMine.x + 0.5, this.blockMine.y + 0.5, this.blockMine.z + 0.5)
                     > this.range.getValue().intValue()) {
                     this.blockMine = null;
                  } else {
                     boolean same = false;

                     for (BlockPos offset : offsets) {
                        if (isPos2(playerPos.add(offset), this.blockMine)) {
                           same = true;
                        }
                     }

                     if (!same) {
                        this.blockMine = null;
                     }
                  }
               }

               boolean hole = true;

               for (BlockPos offsetx : offsets) {
                  BlockPos pos = playerPos.add(offsetx);
                  IBlockState blockState = BlockUtil.getState(pos);
                  if (BlockUtil.isAir(pos) || this.ignore.getValue() && blockState == Blocks.BED) {
                     hole = false;
                  }
               }

               if (hole) {
                  if (this.blockMine != null) {
                     this.working = true;
                  } else {
                     EnumFacing facing = RotationUtil.getFacing(PlayerPacketManager.INSTANCE.getServerSideRotation().x);
                     this.blockMine = playerPos.offset(facing, -1);
                     if (mc.player
                              .getDistance(this.blockMine.x + 0.5, this.blockMine.y + 0.5, this.blockMine.z + 0.5)
                           > this.range.getValue().intValue()
                        || this.ignore.getValue() && BlockUtil.getBlock(this.blockMine) == Blocks.BED
                        || BlockUtil.getBlock(this.blockMine).blockHardness < 0.0F) {
                        List<BlockPos> posList = new ArrayList<>();

                        for (BlockPos offsetxx : offsets) {
                           BlockPos pos = playerPos.add(offsetxx);
                           if (!(mc.player.getDistanceSq(pos) > this.range.getValue() * this.range.getValue())
                              && BlockUtil.getBlock(pos) != Blocks.BEDROCK) {
                              if (this.ignore.getValue() && BlockUtil.getBlock(pos) == Blocks.BED) {
                                 return;
                              }

                              if (!(
                                 mc.player.getDistance(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
                                    > this.range.getValue().intValue()
                              )) {
                                 posList.add(pos);
                              }
                           }
                        }

                        this.blockMine = posList.stream()
                           .min(
                              Comparator.comparing(p -> mc.player.getDistance(p.x + 0.5, p.y + 0.5, p.z + 0.5))
                           )
                           .orElse(null);
                     }

                     if (this.blockMine != null) {
                        this.working = true;
                        if (this.swing.getValue()) {
                           mc.player.swingArm(EnumHand.MAIN_HAND);
                        }

                        if (this.breakBlock.getValue().equalsIgnoreCase("Packet")) {
                           mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, this.blockMine, EnumFacing.UP));
                           mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.blockMine, EnumFacing.UP));
                        } else {
                           mc.playerController.onPlayerDamageBlock(this.blockMine, EnumFacing.UP);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }
}
