package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoTrap", category = Category.Combat)
public class AutoTrap extends Module {
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 20);
   IntegerSetting range = this.registerInteger("Range", 5, 0, 10);
   IntegerSetting bpt = this.registerInteger("BlocksPerTick", 4, 0, 20);
   BooleanSetting top = this.registerBoolean("Top+", false);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting packet = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", false);
   BooleanSetting detect = this.registerBoolean("Detect Break", false);
   BooleanSetting self = this.registerBoolean("Self Break", false, () -> this.detect.getValue());
   BooleanSetting bed = this.registerBoolean("Bedrock", false, () -> this.detect.getValue());
   BooleanSetting pause = this.registerBoolean("BedrockHole", true);
   int ob;
   int waited;
   int placed;
   BlockPos trapPos;
   BlockPos player;
   List<BlockPos> posList = new ArrayList<>();
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};
   BlockPos[] blocks = new BlockPos[]{new BlockPos(0, 1, 0), new BlockPos(0, 2, 0)};
   BlockPos breakPos;
   private int place;
   @EventHandler
   private final Listener<PacketEvent.PostSend> listener = new Listener<>(event -> {
      if (this.player != null && this.self.getValue()) {
         if (event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = (CPacketPlayerDigging)event.getPacket();
            if (packet.getAction() == Action.START_DESTROY_BLOCK) {
               BlockPos ab = packet.getPosition();
               this.breakPos = packet.getPosition();
               if (ab.equals(this.player.add(0, 1, 0))) {
                  this.place = 17;
               }

               if (ab.equals(this.player.add(1, 1, 0))) {
                  this.place = 18;
               }

               if (ab.equals(this.player.add(-1, 1, 0))) {
                  this.place = 19;
               }

               if (ab.equals(this.player.add(0, 1, 1))) {
                  this.place = 20;
               }

               if (ab.equals(this.player.add(0, 1, -1))) {
                  this.place = 21;
               }

               if (ab.equals(this.player.add(0, 2, 0))) {
                  this.place = 22;
               }

               if (ab.equals(this.player.add(1, 0, 0))) {
                  this.place = 1;
               }

               if (ab.equals(this.player.add(-1, 0, 0))) {
                  this.place = 2;
               }

               if (ab.equals(this.player.add(0, 0, 1))) {
                  this.place = 3;
               }

               if (ab.equals(this.player.add(0, 0, -1))) {
                  this.place = 4;
               }

               if (ab.equals(this.player.add(2, 0, 0))) {
                  this.place = 5;
               }

               if (ab.equals(this.player.add(-2, 0, 0))) {
                  this.place = 6;
               }

               if (ab.equals(this.player.add(0, 0, 2))) {
                  this.place = 7;
               }

               if (ab.equals(this.player.add(0, 0, -2))) {
                  this.place = 8;
               }

               if (ab.equals(this.player.add(1, 1, 0))) {
                  this.place = 9;
               }

               if (ab.equals(this.player.add(-1, 1, 0))) {
                  this.place = 10;
               }

               if (ab.equals(this.player.add(0, 1, 1))) {
                  this.place = 11;
               }

               if (ab.equals(this.player.add(0, 1, -1))) {
                  this.place = 12;
               }

               if (ab.equals(this.player.add(1, 0, 1))) {
                  this.place = 13;
               }

               if (ab.equals(this.player.add(1, 0, -1))) {
                  this.place = 14;
               }

               if (ab.equals(this.player.add(-1, 0, 1))) {
                  this.place = 15;
               }

               if (ab.equals(this.player.add(-1, 0, -1))) {
                  this.place = 16;
               }
            }
         }
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null && this.player != null) {
         if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim)event.getPacket();
            BlockPos ab = packet.getPosition();
            this.breakPos = packet.getPosition();
            if (ab.equals(this.player.add(0, 1, 0))) {
               this.place = 17;
            }

            if (ab.equals(this.player.add(1, 0, 0))) {
               this.place = 1;
            }

            if (ab.equals(this.player.add(-1, 0, 0))) {
               this.place = 2;
            }

            if (ab.equals(this.player.add(0, 0, 1))) {
               this.place = 3;
            }

            if (ab.equals(this.player.add(0, 0, -1))) {
               this.place = 4;
            }

            if (ab.equals(this.player.add(2, 0, 0))) {
               this.place = 5;
            }

            if (ab.equals(this.player.add(-2, 0, 0))) {
               this.place = 6;
            }

            if (ab.equals(this.player.add(0, 0, 2))) {
               this.place = 7;
            }

            if (ab.equals(this.player.add(0, 0, -2))) {
               this.place = 8;
            }

            if (ab.equals(this.player.add(1, 1, 0))) {
               this.place = 9;
            }

            if (ab.equals(this.player.add(-1, 1, 0))) {
               this.place = 10;
            }

            if (ab.equals(this.player.add(0, 1, 1))) {
               this.place = 11;
            }

            if (ab.equals(this.player.add(0, 1, -1))) {
               this.place = 12;
            }

            if (ab.equals(this.player.add(1, 0, 1))) {
               this.place = 13;
            }

            if (ab.equals(this.player.add(1, 0, -1))) {
               this.place = 14;
            }

            if (ab.equals(this.player.add(-1, 0, 1))) {
               this.place = 15;
            }

            if (ab.equals(this.player.add(-1, 0, -1))) {
               this.place = 16;
            }
         }
      }
   });

   public static boolean isPlayerInHole(EntityPlayer target) {
      BlockPos blockPos = getLocalPlayerPosFloored(target);
      HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(blockPos, true, true, false);
      HoleUtil.HoleType holeType = holeInfo.getType();
      return holeType == HoleUtil.HoleType.SINGLE;
   }

   public static BlockPos getLocalPlayerPosFloored(EntityPlayer target) {
      return new BlockPos(target.getPositionVector());
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.placed = 0;
         if (this.delay.getValue() > 0) {
            if (this.waited++ < this.delay.getValue()) {
               return;
            }

            this.waited = 0;
         }

         if (BurrowUtil.findHotbarBlock(BlockObsidian.class) != -1) {
            EntityPlayer target = PlayerUtil.getNearestPlayer(this.range.getValue().intValue());
            if (target != null) {
               if (!(mc.player.getDistance(target) > this.range.getValue().intValue()) && isPlayerInHole(target)) {
                  BlockPos pos = EntityUtil.getEntityPos(target);
                  this.addBlock(pos);
               } else {
                  this.posList.clear();
               }

               this.ob = BurrowUtil.findHotbarBlock(BlockObsidian.class);
               if (this.ob != -1) {
                  this.posList.removeIf(posx -> !BlockUtil.isAir(posx) ? true : this.intersectsWithEntity(posx));
                  if (!this.posList.isEmpty()) {
                     InventoryUtil.run(this.ob, this.packetSwitch.getValue(), () -> {
                        for (BlockPos block : this.posList) {
                           if (this.placed > this.bpt.getValue()) {
                              break;
                           }

                           BurrowUtil.placeBlock(block, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                           this.placed++;
                        }
                     });
                  }

                  this.player = EntityUtil.getEntityPos(target).up();
                  this.antiCity(this.player);
               }
            }
         }
      } else {
         this.trapPos = null;
         this.posList.clear();
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

   private void addBlock(BlockPos pos) {
      if (BurrowUtil.findHotbarBlock(BlockObsidian.class) != -1) {
         List<BlockPos> blocklist = new ArrayList<>();
         blocklist.add(pos.add(0, 2, 0));
         if (this.top.getValue()) {
            blocklist.add(pos.add(0, 3, 0));
         }

         int obby = 0;

         for (BlockPos side : this.sides) {
            if (mc.world.getBlockState(pos.add(side)).getBlock() != Blocks.BEDROCK || this.bed.getValue()) {
               for (BlockPos blockPos : this.blocks) {
                  blocklist.add(pos.add(side).add(blockPos));
               }

               obby++;
            }
         }

         if (obby != 0 || this.pause.getValue()) {
            this.posList.addAll(blocklist);
         }
      }
   }

   private boolean noHard(Block block) {
      return block != Blocks.BEDROCK || this.bed.getValue();
   }

   public void antiCity(BlockPos pos) {
      int obsidian = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      if (obsidian != -1) {
         if (pos != null) {
            pos = new BlockPos(pos.x, pos.y + 0.2, pos.z);
            List<BlockPos> list = new ArrayList<>();
            if (this.breakPos != null) {
               if ((this.breakPos.equals(pos.add(1, 0, 0)) || this.breakPos.equals(pos.add(1, 1, 0)))
                  && this.isAir(pos.add(1, 0, 0))
                  && this.isAir(pos.add(1, 1, 0))) {
                  if (this.breakPos.equals(pos.add(1, 0, 0))) {
                     list.add(pos.add(1, 1, 0));
                  } else {
                     list.add(pos.add(1, 0, 0));
                  }
               }

               if ((this.breakPos.equals(pos.add(-1, 0, 0)) || this.breakPos.equals(pos.add(-1, 1, 0)))
                  && this.isAir(pos.add(-1, 0, 0))
                  && this.isAir(pos.add(-1, 1, 0))) {
                  if (this.breakPos.equals(pos.add(-1, 0, 0))) {
                     list.add(pos.add(-1, 1, 0));
                  } else {
                     list.add(pos.add(-1, 0, 0));
                  }
               }

               if ((this.breakPos.equals(pos.add(0, 0, 1)) || this.breakPos.equals(pos.add(0, 1, 1)))
                  && this.isAir(pos.add(0, 0, 1))
                  && this.isAir(pos.add(0, 1, 1))) {
                  if (this.breakPos.equals(pos.add(0, 0, 1))) {
                     list.add(pos.add(0, 1, 1));
                  } else {
                     list.add(pos.add(0, 0, 1));
                  }
               }

               if ((this.breakPos.equals(pos.add(0, 0, -1)) || this.breakPos.equals(pos.add(0, 1, -1)))
                  && this.isAir(pos.add(0, 0, -1))
                  && this.isAir(pos.add(0, 1, -1))) {
                  if (this.breakPos.equals(pos.add(0, 0, -1))) {
                     list.add(pos.add(0, 1, -1));
                  } else {
                     list.add(pos.add(0, 0, -1));
                  }
               }
            }

            if (this.noHard(this.getBlock(pos.add(1, 0, 0)).getBlock())) {
               if (this.place == 1) {
                  list.add(pos.add(2, 0, 0));
                  list.add(pos.add(1, 0, 1));
                  list.add(pos.add(1, 0, -1));
                  list.add(pos.add(1, 1, 0));
               }

               if (this.place == 5 || this.place == 9 || this.place == 13 || this.place == 14) {
                  list.add(pos.add(1, 0, 0));
               }
            }

            if (this.noHard(this.getBlock(pos.add(-1, 0, 0)).getBlock())) {
               if (this.place == 2) {
                  list.add(pos.add(-2, 0, 0));
                  list.add(pos.add(-1, 0, 1));
                  list.add(pos.add(-1, 0, -1));
                  list.add(pos.add(-1, 1, 0));
               }

               if (this.place == 6 || this.place == 10 || this.place == 15 || this.place == 16) {
                  list.add(pos.add(-1, 0, 0));
               }
            }

            if (this.noHard(this.getBlock(pos.add(0, 0, 1)).getBlock())) {
               if (this.place == 3) {
                  list.add(pos.add(0, 0, 2));
                  list.add(pos.add(1, 0, 1));
                  list.add(pos.add(-1, 0, 1));
                  list.add(pos.add(0, 1, 1));
               }

               if (this.place == 7 || this.place == 11 || this.place == 13 || this.place == 15) {
                  list.add(pos.add(0, 0, 1));
               }
            }

            if (this.noHard(this.getBlock(pos.add(0, 0, -1)).getBlock())) {
               if (this.place == 4) {
                  list.add(pos.add(0, 0, -2));
                  list.add(pos.add(1, 0, -1));
                  list.add(pos.add(-1, 0, -1));
                  list.add(pos.add(0, 1, -1));
               }

               if (this.place == 8 || this.place == 12 || this.place == 14 || this.place == 16) {
                  list.add(pos.add(0, 0, -1));
               }
            }

            if (this.noHard(this.getBlock(pos.add(0, 1, 0)).getBlock())) {
               if (this.place == 17) {
                  list.add(pos.add(0, 2, 0));
                  list.add(pos.add(0, 1, -1));
                  list.add(pos.add(0, 1, 1));
                  list.add(pos.add(1, 1, 0));
                  list.add(pos.add(-1, 1, 0));
               }

               if (this.place == 9 || this.place == 10 || this.place == 11 || this.place == 12 || this.place > 17) {
                  list.add(pos.add(0, 1, 0));
               }
            }

            this.place = 0;
            list.removeIf(p -> PlayerCheck(p) || !this.CanPlace(p));
            if (!list.isEmpty()) {
               InventoryUtil.run(obsidian, this.packetSwitch.getValue(), () -> {
                  for (BlockPos blockPos : list) {
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

   private IBlockState getBlock(BlockPos block) {
      return block == null ? null : mc.world.getBlockState(block);
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
