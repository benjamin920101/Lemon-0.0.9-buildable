package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.dev.BedCevBreaker;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoHoleMine", category = Category.Combat)
public final class AutoHoleMine extends Module {
   public static AutoHoleMine INSTANCE;
   BooleanSetting breakTrap = this.registerBoolean("Break Trap", false);
   BooleanSetting doubleMine = this.registerBoolean("Double Mine", true);
   BooleanSetting ignore = this.registerBoolean("Ignore Bed", false);
   BooleanSetting ignorePiston = this.registerBoolean("Ignore Piston", false);
   BooleanSetting ignoreWeb = this.registerBoolean("Ignore Web", false);
   BooleanSetting fire = this.registerBoolean("Fire", false);
   BooleanSetting sand = this.registerBoolean("Falling Blocks", false);
   public boolean working;
   BlockPos[] side = new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0)};

   public AutoHoleMine() {
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
            }

            EntityPlayer target = PlayerUtil.getNearestPlayer(8.0);
            if (target != null) {
               BlockPos feet = new BlockPos(target.posX, target.posY + 0.2, target.posZ);
               double breakRange = 0.0;
               BlockPos doublePos = null;
               if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                  doublePos = PacketMine.INSTANCE.doublePos;
                  breakRange = PacketMine.INSTANCE.breakRange.getValue();
               }

               BlockPos pos = null;

               for (BlockPos side : this.side) {
                  BlockPos surroundPos = feet.add(side);
                  BlockPos crystalPos = surroundPos.add(side);
                  if (BlockUtil.isAir(surroundPos)) {
                     if (BlockUtil.isAir(surroundPos.up())) {
                        return;
                     }

                     if (BlockUtil.isAirBlock(crystalPos) && BlockUtil.isAirBlock(crystalPos.up())) {
                        if (!this.breakTrap.getValue()) {
                           return;
                        }

                        pos = surroundPos.up();
                     }
                  }
               }

               if (pos != null) {
                  this.surroundMine(pos);
               } else {
                  List<BlockPos> posList = new ArrayList<>();

                  for (BlockPos sidex : this.side) {
                     BlockPos surroundPos = feet.add(sidex);
                     BlockPos crystalPos = surroundPos.add(sidex);
                     if (BlockUtil.isAirBlock(crystalPos) && BlockUtil.isAirBlock(crystalPos.up())) {
                        if (this.checkMine(surroundPos, breakRange)) {
                           posList.add(surroundPos);
                        }
                     } else if (BlockUtil.isAir(surroundPos) && BlockUtil.isAirBlock(crystalPos.up())) {
                        if (this.checkMine(crystalPos, breakRange)) {
                           posList.add(crystalPos);
                        }
                     } else if (BlockUtil.isAir(surroundPos) && BlockUtil.isAirBlock(crystalPos) && this.checkMine(crystalPos.up(), breakRange)) {
                        posList.add(crystalPos.up());
                     }
                  }

                  if (!posList.isEmpty()) {
                     this.surroundMine(posList.stream().min(Comparator.comparing(mc.player::getDistanceSq)).orElse(null));
                  } else {
                     if (this.doubleMine.getValue()) {
                        List<AutoHoleMine.DoubleBreak> breakList = new ArrayList<>();

                        for (BlockPos sidexx : this.side) {
                           BlockPos surroundPos = feet.add(sidexx);
                           BlockPos crystalPos = surroundPos.add(sidexx);
                           if (!BlockUtil.isAir(surroundPos) && !BlockUtil.isAirBlock(crystalPos) && BlockUtil.isAirBlock(crystalPos.up())) {
                              if (this.checkMine(surroundPos, breakRange) && this.checkMine(crystalPos, breakRange)) {
                                 breakList.add(new AutoHoleMine.DoubleBreak(surroundPos, crystalPos));
                              }
                           } else if (!BlockUtil.isAir(surroundPos) && !BlockUtil.isAirBlock(crystalPos.up()) && BlockUtil.isAirBlock(crystalPos)) {
                              if (this.checkMine(surroundPos, breakRange) && this.checkMine(crystalPos.up(), breakRange)) {
                                 breakList.add(new AutoHoleMine.DoubleBreak(surroundPos, crystalPos.up()));
                              }
                           } else if (BlockUtil.isAir(surroundPos)
                              && !BlockUtil.isAirBlock(crystalPos)
                              && !BlockUtil.isAirBlock(crystalPos.up())
                              && this.checkMine(crystalPos, breakRange)
                              && this.checkMine(crystalPos.up(), breakRange)) {
                              breakList.add(new AutoHoleMine.DoubleBreak(crystalPos, crystalPos.up()));
                           }
                        }

                        if (breakList.isEmpty()) {
                           for (BlockPos sidexxx : this.side) {
                              BlockPos surroundPos = feet.add(sidexxx);
                              BlockPos crystalPos = surroundPos.add(sidexxx);
                              if (this.checkMine(surroundPos, breakRange)
                                 && this.checkMine(crystalPos, breakRange)
                                 && this.checkMine(crystalPos.up(), breakRange)) {
                                 breakList.add(new AutoHoleMine.DoubleBreak(crystalPos, crystalPos.up()));
                              }
                           }
                        }

                        if (breakList.isEmpty()) {
                           for (BlockPos sidexxxx : this.side) {
                              BlockPos surroundPos = feet.add(sidexxxx);
                              BlockPos crystalPos = surroundPos.add(sidexxxx);
                              if (!BlockUtil.isAirBlock(crystalPos)
                                 && !BlockUtil.isAirBlock(crystalPos.up())
                                 && !this.checkMine(crystalPos)
                                 && !this.checkMine(crystalPos.up())
                                 && this.checkMine(surroundPos, breakRange)
                                 && this.checkMine(surroundPos.up(), breakRange)) {
                                 breakList.add(new AutoHoleMine.DoubleBreak(surroundPos, surroundPos.up()));
                              }
                           }
                        }

                        if (!breakList.isEmpty()) {
                           AutoHoleMine.DoubleBreak doubleBreak = breakList.stream().min(Comparator.comparing(AutoHoleMine.DoubleBreak::maxRange)).orElse(null);
                           this.surroundMine(doubleBreak.doublePos);
                           if (doublePos == null) {
                              this.surroundMine(doubleBreak.packetPos);
                           }

                           return;
                        }
                     } else {
                        for (BlockPos sidexxxxx : this.side) {
                           BlockPos surroundPos = feet.add(sidexxxxx);
                           BlockPos crystalPos = surroundPos.add(sidexxxxx);
                           if (!BlockUtil.isAir(surroundPos) && this.checkMine(surroundPos, breakRange)) {
                              if (BlockUtil.isAirBlock(crystalPos) && this.checkMine(crystalPos, breakRange)
                                 || BlockUtil.isAirBlock(crystalPos.up()) && this.checkMine(crystalPos.up(), breakRange)) {
                                 posList.add(surroundPos);
                              }
                           } else if (!BlockUtil.isAirBlock(crystalPos) && this.checkMine(crystalPos, breakRange)) {
                              if (BlockUtil.isAir(surroundPos) && this.checkMine(surroundPos, breakRange)
                                 || BlockUtil.isAirBlock(crystalPos.up()) && this.checkMine(crystalPos.up(), breakRange)) {
                                 posList.add(crystalPos);
                              }
                           } else if (!BlockUtil.isAirBlock(crystalPos.up())
                              && this.checkMine(crystalPos.up(), breakRange)
                              && (
                                 BlockUtil.isAir(surroundPos) && this.checkMine(surroundPos, breakRange)
                                    || BlockUtil.isAirBlock(crystalPos) && this.checkMine(crystalPos, breakRange)
                              )) {
                              posList.add(crystalPos.up());
                           }
                        }

                        if (posList.isEmpty()) {
                           for (BlockPos sidexxxxxx : this.side) {
                              BlockPos surroundPos = feet.add(sidexxxxxx);
                              BlockPos crystalPos = surroundPos.add(sidexxxxxx);
                              if (this.checkMine(surroundPos, breakRange)
                                 && this.checkMine(crystalPos, breakRange)
                                 && this.checkMine(crystalPos.up(), breakRange)) {
                                 posList.add(crystalPos.up());
                              }
                           }
                        }

                        if (!posList.isEmpty()) {
                           this.surroundMine(posList.stream().min(Comparator.comparing(mc.player::getDistanceSq)).orElse(null));
                           return;
                        }
                     }

                     boolean hole = true;

                     for (BlockPos offset : this.side) {
                        if (BlockUtil.isAir(feet.add(offset)) && BlockUtil.isAir(feet.add(offset).up())) {
                           hole = false;
                        }
                     }

                     if (hole) {
                        for (BlockPos sidexxxxxxx : this.side) {
                           BlockPos surroundPos = feet.add(sidexxxxxxx);
                           if (this.checkMine(surroundPos, breakRange)) {
                              posList.add(surroundPos);
                           }
                        }

                        if (!posList.isEmpty()) {
                           this.surroundMine(posList.stream().min(Comparator.comparing(mc.player::getDistanceSq)).orElse(null));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean checkMine(BlockPos pos) {
      return !BlockUtil.isAir(pos) && BlockUtil.getBlock(pos).blockHardness >= 0.0F && this.can(pos);
   }

   private boolean checkMine(BlockPos pos, double range) {
      return !BlockUtil.isAir(pos) && BlockUtil.getBlock(pos).blockHardness >= 0.0F && this.can(pos) && this.getDistance(pos) <= range;
   }

   private boolean can(BlockPos pos) {
      return (!this.ignore.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.BED)
         && (!this.ignorePiston.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.PISTON_HEAD)
         && (!this.ignoreWeb.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.WEB)
         && (this.fire.getValue() || mc.world.getBlockState(pos).getBlock() != Blocks.FIRE)
         && (
            this.sand.getValue()
               || mc.world.getBlockState(pos).getBlock() != Blocks.SAND
                  && mc.world.getBlockState(pos).getBlock() != Blocks.GRAVEL
                  && mc.world.getBlockState(pos).getBlock() != Blocks.ANVIL
                  && !(mc.world.getBlockState(pos).getBlock() instanceof BlockConcretePowder)
         );
   }

   private void surroundMine(BlockPos pos) {
      if (pos != null && this.checkMine(pos)) {
         this.working = true;
         BlockPos doublePos = null;
         BlockPos instantPos = null;
         if (ModuleManager.isModuleEnabled(PacketMine.class)) {
            instantPos = PacketMine.INSTANCE.packetPos;
            doublePos = PacketMine.INSTANCE.doublePos;
         }

         if (instantPos == null || !instantPos.equals(pos)) {
            if (doublePos == null || !doublePos.equals(pos)) {
               mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
            }
         }
      }
   }

   private double getDistance(BlockPos pos) {
      return mc.player.getDistance(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
   }

   class DoubleBreak {
      BlockPos packetPos;
      BlockPos doublePos;

      public DoubleBreak(BlockPos packetPos, BlockPos doublePos) {
         this.packetPos = packetPos;
         this.doublePos = doublePos;
      }

      public double maxRange() {
         double packetRange = AutoHoleMine.this.getDistance(this.packetPos);
         double doubleRange = AutoHoleMine.this.getDistance(this.doublePos);
         return Math.max(packetRange, doubleRange);
      }
   }
}
