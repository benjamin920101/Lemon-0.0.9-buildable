package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoConcrete", category = Category.Dev)
public class AutoConcrete extends Module {
   DoubleSetting range = this.registerDouble("Range", 5.5, 0.0, 10.0);
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting air = this.registerBoolean("Air Check", true);
   BooleanSetting disable = this.registerBoolean("Disable", true);
   IntegerSetting delay = this.registerInteger("Delay", 5, 0, 100, () -> !this.disable.getValue());
   DoubleSetting maxTargetSpeed = this.registerDouble("Max Target Speed", 10.0, 0.0, 50.0);
   int waited;
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};

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

   @Override
   public void onEnable() {
      this.waited = 100;
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (this.waited++ >= this.delay.getValue()) {
            this.waited = 0;
            int slot = BurrowUtil.findHotbarBlock(BlockAnvil.class);
            if (slot == -1) {
               slot = BurrowUtil.findHotbarBlock(BlockConcretePowder.class);
               if (slot == -1) {
                  return;
               }
            }

            EntityPlayer player = PlayerUtil.getNearestPlayer(this.range.getValue());
            if (!(LemonClient.speedUtil.getPlayerSpeed(player) > this.maxTargetSpeed.getValue())) {
               if (player == null) {
                  if (this.disable.getValue()) {
                     this.disable();
                  }
               } else {
                  BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                  if (!BlockUtil.airBlocks.contains(mc.world.getBlockState(pos).getBlock()) && this.air.getValue()) {
                     if (this.disable.getValue()) {
                        this.disable();
                     }
                  } else {
                     BlockPos placePos = pos.up(2);
                     if (!this.intersectsWithEntity(placePos)) {
                        if (BurrowUtil.getFirstFacing(placePos) == null) {
                           int obby = BurrowUtil.findHotbarBlock(BlockObsidian.class);
                           if (obby == -1) {
                              return;
                           }

                           boolean helped = false;

                           for (BlockPos side : this.sides) {
                              BlockPos helpingBlock = placePos.add(side);
                              if (!this.intersectsWithEntity(helpingBlock)) {
                                 if (BurrowUtil.getFirstFacing(helpingBlock) != null) {
                                    this.switchTo(
                                       obby,
                                       () -> BurrowUtil.placeBlock(
                                          helpingBlock, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                                       )
                                    );
                                    helped = true;
                                    break;
                                 }

                                 if (!this.intersectsWithEntity(helpingBlock.down())) {
                                    if (BurrowUtil.getFirstFacing(helpingBlock.down()) != null) {
                                       this.switchTo(
                                          obby,
                                          () -> {
                                             BurrowUtil.placeBlock(
                                                helpingBlock.down(),
                                                EnumHand.MAIN_HAND,
                                                this.rotate.getValue(),
                                                this.packet.getValue(),
                                                false,
                                                this.swing.getValue()
                                             );
                                             BurrowUtil.placeBlock(
                                                helpingBlock, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                                             );
                                          }
                                       );
                                       helped = true;
                                       break;
                                    }

                                    if (!this.intersectsWithEntity(helpingBlock.down(2))
                                       && BurrowUtil.getFirstFacing(helpingBlock.down(2)) != null) {
                                       this.switchTo(
                                          obby,
                                          () -> {
                                             BurrowUtil.placeBlock(
                                                helpingBlock.down(2),
                                                EnumHand.MAIN_HAND,
                                                this.rotate.getValue(),
                                                this.packet.getValue(),
                                                false,
                                                this.swing.getValue()
                                             );
                                             BurrowUtil.placeBlock(
                                                helpingBlock.down(),
                                                EnumHand.MAIN_HAND,
                                                this.rotate.getValue(),
                                                this.packet.getValue(),
                                                false,
                                                this.swing.getValue()
                                             );
                                             BurrowUtil.placeBlock(
                                                helpingBlock, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                                             );
                                          }
                                       );
                                       helped = true;
                                       break;
                                    }
                                 }
                              }
                           }

                           if (!helped) {
                              return;
                           }
                        }

                        this.switchTo(
                           slot,
                           () -> BurrowUtil.placeBlock(
                              placePos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                           )
                        );
                        if (this.disable.getValue()) {
                           this.disable();
                        }
                     }
                  }
               }
            }
         }
      } else {
         if (this.disable.getValue()) {
            this.disable();
         }
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
}
