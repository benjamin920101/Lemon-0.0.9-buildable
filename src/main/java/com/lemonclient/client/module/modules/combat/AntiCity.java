package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AntiCity", category = Category.Combat)
public class AntiCity extends Module {
   ModeSetting time = this.registerMode("Time Mode", Arrays.asList("Tick", "onUpdate", "Fast"), "Tick");
   IntegerSetting bpt = this.registerInteger("Blocks Per Tick", 4, 0, 20);
   BooleanSetting self = this.registerBoolean("Self", false);
   BooleanSetting smart = this.registerBoolean("Smart", false);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packet = this.registerBoolean("Packet", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BlockPos breakPos;
   private int placeID;
   @EventHandler
   private final Listener<PacketEvent.PostSend> sendListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         if (this.self.getValue()) {
            if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging)event.getPacket()).getAction() == Action.START_DESTROY_BLOCK) {
               CPacketPlayerDigging packet = (CPacketPlayerDigging)event.getPacket();
               BlockPos ab = packet.getPosition();
               this.breakPos = packet.getPosition();
               BlockPos player = EntityUtil.getPlayerPos(mc.player);
               if (ab.equals(player.add(1, 0, 0))) {
                  this.placeID = 1;
               }

               if (ab.equals(player.add(-1, 0, 0))) {
                  this.placeID = 2;
               }

               if (ab.equals(player.add(0, 0, 1))) {
                  this.placeID = 3;
               }

               if (ab.equals(player.add(0, 0, -1))) {
                  this.placeID = 4;
               }

               if (ab.equals(player.add(2, 0, 0))) {
                  this.placeID = 5;
               }

               if (ab.equals(player.add(-2, 0, 0))) {
                  this.placeID = 6;
               }

               if (ab.equals(player.add(0, 0, 2))) {
                  this.placeID = 7;
               }

               if (ab.equals(player.add(0, 0, -2))) {
                  this.placeID = 8;
               }

               if (ab.equals(player.add(1, 1, 0))) {
                  this.placeID = 9;
               }

               if (ab.equals(player.add(-1, 1, 0))) {
                  this.placeID = 10;
               }

               if (ab.equals(player.add(0, 1, 1))) {
                  this.placeID = 11;
               }

               if (ab.equals(player.add(0, 1, -1))) {
                  this.placeID = 12;
               }

               if (ab.equals(player.add(1, 0, 1))) {
                  this.placeID = 13;
               }

               if (ab.equals(player.add(1, 0, -1))) {
                  this.placeID = 14;
               }

               if (ab.equals(player.add(-1, 0, 1))) {
                  this.placeID = 15;
               }

               if (ab.equals(player.add(-1, 0, -1))) {
                  this.placeID = 16;
               }
            }
         }
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim)event.getPacket();
            BlockPos ab = packet.getPosition();
            this.breakPos = packet.getPosition();
            BlockPos player = EntityUtil.getPlayerPos(mc.player);
            if (ab.equals(player.add(1, 0, 0))) {
               this.placeID = 1;
            }

            if (ab.equals(player.add(-1, 0, 0))) {
               this.placeID = 2;
            }

            if (ab.equals(player.add(0, 0, 1))) {
               this.placeID = 3;
            }

            if (ab.equals(player.add(0, 0, -1))) {
               this.placeID = 4;
            }

            if (ab.equals(player.add(2, 0, 0))) {
               this.placeID = 5;
            }

            if (ab.equals(player.add(-2, 0, 0))) {
               this.placeID = 6;
            }

            if (ab.equals(player.add(0, 0, 2))) {
               this.placeID = 7;
            }

            if (ab.equals(player.add(0, 0, -2))) {
               this.placeID = 8;
            }

            if (ab.equals(player.add(1, 1, 0))) {
               this.placeID = 9;
            }

            if (ab.equals(player.add(-1, 1, 0))) {
               this.placeID = 10;
            }

            if (ab.equals(player.add(0, 1, 1))) {
               this.placeID = 11;
            }

            if (ab.equals(player.add(0, 1, -1))) {
               this.placeID = 12;
            }

            if (ab.equals(player.add(1, 0, 1))) {
               this.placeID = 13;
            }

            if (ab.equals(player.add(1, 0, -1))) {
               this.placeID = 14;
            }

            if (ab.equals(player.add(-1, 0, 1))) {
               this.placeID = 15;
            }

            if (ab.equals(player.add(-1, 0, -1))) {
               this.placeID = 16;
            }
         }
      }
   });
   int placed;

   public static boolean noHard(Block block) {
      return block != Blocks.BEDROCK;
   }

   @Override
   public void onUpdate() {
      if (this.time.getValue().equals("onUpdate")) {
         this.antiCity();
      }

      this.placed = 0;
   }

   @Override
   public void onTick() {
      if (this.time.getValue().equals("Tick")) {
         this.antiCity();
      }
   }

   @Override
   public void fast() {
      if (this.time.getValue().equals("Fast")) {
         this.antiCity();
      }
   }

   public void antiCity() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (!(LemonClient.speedUtil.getPlayerSpeed(mc.player) >= 15.0)) {
            int obsidian = BurrowUtil.findHotbarBlock(BlockObsidian.class);
            if (obsidian != -1) {
               BlockPos pos = EntityUtil.getPlayerPos(mc.player);
               if (pos != null) {
                  pos = new BlockPos(pos.x, pos.y + 0.2, pos.z);
                  List<BlockPos> placeList = new ArrayList<>();
                  if (this.breakPos != null) {
                     if ((this.breakPos.equals(pos.add(1, 0, 0)) || this.breakPos.equals(pos.add(1, 1, 0)))
                        && this.isAir(pos.add(1, 0, 0))
                        && this.isAir(pos.add(1, 1, 0))) {
                        if (this.breakPos.equals(pos.add(1, 0, 0))) {
                           placeList.add(pos.add(1, 1, 0));
                        } else {
                           placeList.add(pos.add(1, 0, 0));
                        }
                     }

                     if ((this.breakPos.equals(pos.add(-1, 0, 0)) || this.breakPos.equals(pos.add(-1, 1, 0)))
                        && this.isAir(pos.add(-1, 0, 0))
                        && this.isAir(pos.add(-1, 1, 0))) {
                        if (this.breakPos.equals(pos.add(-1, 0, 0))) {
                           placeList.add(pos.add(-1, 1, 0));
                        } else {
                           placeList.add(pos.add(-1, 0, 0));
                        }
                     }

                     if ((this.breakPos.equals(pos.add(0, 0, 1)) || this.breakPos.equals(pos.add(0, 1, 1)))
                        && this.isAir(pos.add(0, 0, 1))
                        && this.isAir(pos.add(0, 1, 1))) {
                        if (this.breakPos.equals(pos.add(0, 0, 1))) {
                           placeList.add(pos.add(0, 1, 1));
                        } else {
                           placeList.add(pos.add(0, 0, 1));
                        }
                     }

                     if ((this.breakPos.equals(pos.add(0, 0, -1)) || this.breakPos.equals(pos.add(0, 1, -1)))
                        && this.isAir(pos.add(0, 0, -1))
                        && this.isAir(pos.add(0, 1, -1))) {
                        if (this.breakPos.equals(pos.add(0, 0, -1))) {
                           placeList.add(pos.add(0, 1, -1));
                        } else {
                           placeList.add(pos.add(0, 0, -1));
                        }
                     }
                  }

                  if (noHard(this.getBlock(pos.add(1, 0, 0)).getBlock())) {
                     if (this.placeID == 1) {
                        placeList.add(pos.add(2, 0, 0));
                        placeList.add(pos.add(1, 0, 1));
                        placeList.add(pos.add(1, 0, -1));
                        placeList.add(pos.add(1, 1, 0));
                        if (EntityCheck(pos.add(2, 0, 0))) {
                           placeList.add(pos.add(3, 0, 0));
                           placeList.add(pos.add(3, 1, 0));
                        }
                     }

                     if (this.placeID == 5) {
                        placeList.add(pos.add(1, 0, 0));
                        placeList.add(pos.add(2, 1, 0));
                        placeList.add(pos.add(3, 0, 0));
                     }

                     if (this.placeID == 9) {
                        placeList.add(pos.add(1, 0, 0));
                        placeList.add(pos.add(2, 1, 0));
                     }

                     if (this.placeID == 13 || this.placeID == 14) {
                        placeList.add(pos.add(1, 0, 0));
                     }
                  }

                  if (noHard(this.getBlock(pos.add(-1, 0, 0)).getBlock())) {
                     if (this.placeID == 2) {
                        placeList.add(pos.add(-2, 0, 0));
                        placeList.add(pos.add(-1, 0, 1));
                        placeList.add(pos.add(-1, 0, -1));
                        placeList.add(pos.add(-1, 1, 0));
                        if (EntityCheck(pos.add(-2, 0, 0))) {
                           placeList.add(pos.add(-3, 0, 0));
                           placeList.add(pos.add(-3, 1, 0));
                        }
                     }

                     if (this.placeID == 6) {
                        placeList.add(pos.add(-1, 0, 0));
                        placeList.add(pos.add(-2, 1, 0));
                        placeList.add(pos.add(-3, 0, 0));
                     }

                     if (this.placeID == 10) {
                        placeList.add(pos.add(-1, 0, 0));
                        placeList.add(pos.add(-2, 1, 0));
                     }

                     if (this.placeID == 15 || this.placeID == 16) {
                        placeList.add(pos.add(-1, 0, 0));
                     }
                  }

                  if (noHard(this.getBlock(pos.add(0, 0, 1)).getBlock())) {
                     if (this.placeID == 3) {
                        placeList.add(pos.add(0, 0, 2));
                        placeList.add(pos.add(1, 0, 1));
                        placeList.add(pos.add(-1, 0, 1));
                        placeList.add(pos.add(0, 1, 1));
                        if (EntityCheck(pos.add(0, 0, 2))) {
                           placeList.add(pos.add(0, 0, 3));
                           placeList.add(pos.add(0, 1, 3));
                        }
                     }

                     if (this.placeID == 7) {
                        placeList.add(pos.add(0, 0, 1));
                        placeList.add(pos.add(0, 1, 2));
                        placeList.add(pos.add(0, 0, 3));
                     }

                     if (this.placeID == 11) {
                        placeList.add(pos.add(0, 0, 1));
                        placeList.add(pos.add(0, 1, 2));
                     }

                     if (this.placeID == 13 || this.placeID == 15) {
                        placeList.add(pos.add(0, 0, 1));
                     }
                  }

                  if (noHard(this.getBlock(pos.add(0, 0, -1)).getBlock())) {
                     if (this.placeID == 4) {
                        placeList.add(pos.add(0, 0, -2));
                        placeList.add(pos.add(1, 0, -1));
                        placeList.add(pos.add(-1, 0, -1));
                        placeList.add(pos.add(0, 1, -1));
                        if (EntityCheck(pos.add(0, 0, -2))) {
                           placeList.add(pos.add(0, 0, -3));
                           placeList.add(pos.add(0, 1, -3));
                        }
                     }

                     if (this.placeID == 8) {
                        placeList.add(pos.add(0, 0, -1));
                        placeList.add(pos.add(0, 1, -2));
                        placeList.add(pos.add(0, 0, -3));
                     }

                     if (this.placeID == 12) {
                        placeList.add(pos.add(0, 0, -1));
                        placeList.add(pos.add(0, 1, -2));
                     }

                     if (this.placeID == 14 || this.placeID == 16) {
                        placeList.add(pos.add(0, 0, -1));
                     }
                  }

                  this.placeID = 0;
                  BlockPos instantPos;
                  if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                     instantPos = PacketMine.INSTANCE.packetPos;
                  } else {
                     instantPos = null;
                  }

                  placeList.removeIf(
                     blockPos -> PlayerCheck(blockPos)
                        || !this.CanPlace(blockPos)
                        || this.smart.getValue() && this.isPos2(blockPos, instantPos)
                        || EntityCheck(blockPos)
                  );
                  if (!placeList.isEmpty()) {
                     InventoryUtil.run(obsidian, this.packetSwitch.getValue(), () -> {
                        for (BlockPos blockPos : placeList) {
                           if (this.placed >= this.bpt.getValue()) {
                              break;
                           }

                           BurrowUtil.placeBlock(blockPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                           this.placed++;
                        }
                     });
                  }
               }
            }
         }
      }
   }

   public static boolean EntityCheck(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && entity != null) {
            return true;
         }
      }

      return false;
   }

   private IBlockState getBlock(BlockPos block) {
      return block == null ? null : mc.world.getBlockState(block);
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   public boolean CanPlace(BlockPos block) {
      for (EnumFacing face : EnumFacing.VALUES) {
         if (isReplaceable(block)
            && !BlockUtil.airBlocks.contains(this.getBlock(block.offset(face)))
            && mc.player.getDistanceSq(block) <= MathUtil.square(5.0)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isReplaceable(BlockPos pos) {
      return BlockUtil.getState(pos).getMaterial().isReplaceable();
   }

   private boolean isAir(BlockPos block) {
      return mc.world.getBlockState(block).getBlock() == Blocks.AIR;
   }

   public static boolean PlayerCheck(BlockPos pos) {
      for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer) {
            return true;
         }
      }

      return false;
   }
}
