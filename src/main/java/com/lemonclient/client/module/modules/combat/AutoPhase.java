package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PhaseUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.Arrays;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoPhase", category = Category.Combat)
public class AutoPhase extends Module {
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("5b", "Jp"), "5b");
   ModeSetting bound = this.registerMode("Bounds", PhaseUtil.bound, "Min", () -> this.mode.getValue().equals("5b"));
   BooleanSetting twoBeePvP = this.registerBoolean("2b2tpvp", false, () -> this.mode.getValue().equals("5b"));
   BooleanSetting update = this.registerBoolean("Update Pos", false, () -> this.mode.getValue().equals("5b"));
   BooleanSetting packet = this.registerBoolean("Packet Place", true, () -> this.mode.getValue().equals("Jp"));
   BooleanSetting swing = this.registerBoolean("Swing", true, () -> this.mode.getValue().equals("Jp"));
   BooleanSetting mine = this.registerBoolean("Mine", true, () -> this.mode.getValue().equals("Jp"));
   BooleanSetting burrow = this.registerBoolean("Try Burrow", true, () -> this.mode.getValue().equals("Jp"));
   BooleanSetting doubleBurrow = this.registerBoolean("Double", true, () -> this.mode.getValue().equals("Jp") && this.burrow.getValue());
   IntegerSetting entity = this.registerInteger("Entity Time", 5, 0, 10, () -> this.mode.getValue().equals("Jp"));
   BooleanSetting ignoreCrystal = this.registerBoolean("Ignore Crystal", true, () -> this.mode.getValue().equals("Jp"));
   IntegerSetting checkDelay = this.registerInteger("Check Time", 50, 0, 500, () -> this.mode.getValue().equals("Jp"));
   BlockPos originalPos;
   boolean down;
   Timing timing = new Timing();
   Timing timer = new Timing();
   int tpid = 0;
   List<Block> blockList = Arrays.asList(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANVIL);
   BlockPos[] sides = new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0)};
   BlockPos[] height = new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0)};
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (event.getPacket() instanceof SPacketPlayerPosLook) {
         this.tpid = ((SPacketPlayerPosLook)event.getPacket()).teleportId;
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (event.getPacket() instanceof PositionRotation || event.getPacket() instanceof Position) {
         this.tpid++;
      }
   });

   @Override
   public void onEnable() {
      if (this.mode.getValue().equals("Jp")) {
         this.down = true;
         this.originalPos = PlayerUtil.getPlayerPos();
         this.originalPos = new BlockPos(this.originalPos.x, this.originalPos.y + 0.2, this.originalPos.z);
         if (BurrowUtil.findHotbarBlock(BlockTrapDoor.class) == -1 || !mc.world.isAirBlock(this.originalPos)) {
            this.disable();
            return;
         }

         mc.player.setPosition(mc.player.posX, (int)mc.player.posY, mc.player.posZ);
         this.timing.reset();
         this.timer.reset();
         this.down = false;
      }
   }

   @Override
   public void onDisable() {
      if (this.mode.getValue().equals("Jp") && ModuleManager.isModuleEnabled(PacketMine.class)) {
         PacketMine.INSTANCE.lastBlock = null;
      }
   }

   @Override
   public void onUpdate() {
      if (this.mode.getValue().equals("Jp")) {
         this.trapdoor();
      } else {
         this.packetFly();
      }
   }

   void packetFly() {
      double[] clip = MotionUtil.forward(0.0624);
      if (mc.player.onGround) {
         this.tp(0.0, -0.0624, 0.0, false);
      } else {
         this.tp(clip[0], 0.0, clip[1], true);
      }

      this.disable();
   }

   void tp(double x, double y, double z, boolean onGround) {
      double[] dir = MotionUtil.forward(-0.0312);
      if (this.twoBeePvP.getValue()) {
         mc.player
            .connection
            .sendPacket(
               new Position(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1], onGround)
            );
      }

      mc.player
         .connection
         .sendPacket(
            new Position(
               (this.twoBeePvP.getValue() ? x / 2.0 : x) + mc.player.posX,
               y + mc.player.posY,
               (this.twoBeePvP.getValue() ? z / 2.0 : z) + mc.player.posZ,
               onGround
            )
         );
      mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.tpid - 1));
      mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.tpid));
      mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.tpid + 1));
      PhaseUtil.doBounds(this.bound.getValue(), true);
      if (this.update.getValue()) {
         mc.player.setPosition(x, y, z);
      }
   }

   private void trapdoor() {
      if (mc.world == null || mc.player == null || mc.player.isDead || this.originalPos == null) {
         this.disable();
      } else {
         if (!this.down) {
            if (BurrowUtil.findHotbarBlock(BlockTrapDoor.class) == -1) {
               this.disable();
               return;
            }

            if (this.intersectsWithEntity(this.originalPos) && this.timer.passedS(this.entity.getValue().intValue())) {
               this.disable();
               return;
            }

            EnumFacing facing = BurrowUtil.getTrapdoorFacing(this.originalPos);
            BlockPos burrowPos = null;

            for (BlockPos side : this.sides) {
               BlockPos blockPos = PlayerUtil.getPlayerPos().add(side);
               if (BlockUtil.getBlock(blockPos) == Blocks.BEDROCK || BlockUtil.getBlock(blockPos) == Blocks.OBSIDIAN) {
                  burrowPos = blockPos;
                  break;
               }
            }

            int obsidian = BurrowUtil.findHotbarBlock(BlockObsidian.class);
            if (facing == null || burrowPos == null && this.burrow.getValue()) {
               if (this.burrow.getValue()) {
                  boolean placed = false;
                  if (obsidian != -1) {
                     for (BlockPos sidex : this.sides) {
                        BlockPos blockPos = PlayerUtil.getPlayerPos().add(sidex);
                        if (!this.intersectsWithEntity(blockPos) && BlockUtil.hasNeighbour(blockPos)) {
                           mc.player.connection.sendPacket(new CPacketHeldItemChange(obsidian));
                           BurrowUtil.placeBlock(blockPos, EnumHand.MAIN_HAND, false, false, false, false);
                           if (this.doubleBurrow.getValue()) {
                              BurrowUtil.placeBlock(blockPos.up(), EnumHand.MAIN_HAND, false, false, false, false);
                           }

                           placed = true;
                           break;
                        }
                     }
                  }

                  if (!placed) {
                     this.disable();
                     return;
                  }
               } else {
                  this.disable();
               }

               return;
            }

            if (this.burrow.getValue() && this.doubleBurrow.getValue() && BlockUtil.isAir(burrowPos.up())) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(obsidian));
               BurrowUtil.placeBlock(burrowPos.up(), EnumHand.MAIN_HAND, false, false, false, false);
            }

            BlockPos neighbour = this.originalPos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            double x = mc.player.posX;
            double y = mc.player.posY;
            double z = mc.player.posZ;
            mc.player.connection.sendPacket(new Position(x, y + 0.2F, z, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketHeldItemChange(BurrowUtil.findHotbarBlock(BlockTrapDoor.class)));
            boolean sneak = false;
            if ((
                  BlockUtil.blackList.contains(mc.world.getBlockState(neighbour).getBlock())
                     || BlockUtil.shulkerList.contains(mc.world.getBlockState(neighbour).getBlock())
               )
               && !mc.player.isSneaking()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
               mc.player.setSneaking(true);
               sneak = true;
            }

            rightClickBlock(neighbour, opposite, new Vec3d(0.5, 0.8, 0.5), this.packet.getValue(), this.swing.getValue());
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            mc.player.connection.sendPacket(new Position(x, y, z, mc.player.onGround));
            if (sneak) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
               mc.player.setSneaking(false);
            }

            if (this.burrow.getValue()) {
               if (burrowPos == null) {
                  return;
               }

               mc.player.setPosition(burrowPos.x + 0.5, burrowPos.y, burrowPos.z + 0.5);
               this.disable();
            } else {
               int bedrocks = 0;
               int blocks = 0;
               double xAdd = 0.0;
               double zAdd = 0.0;

               for (BlockPos sidexx : this.sides) {
                  for (BlockPos add : this.sides) {
                     if (!this.isPos2(this.originalPos, this.originalPos.add(sidexx).add(add)) && !this.isPos2(sidexx, add)) {
                        int bedrock = 0;
                        int block = 0;
                        BlockPos sidePos = this.originalPos.add(sidexx);
                        BlockPos addPos = this.originalPos.add(add);
                        BlockPos addSide = this.originalPos.add(sidexx).add(add);

                        for (BlockPos high : this.height) {
                           Block sideState = mc.world.getBlockState(sidePos.add(high)).getBlock();
                           Block addState = mc.world.getBlockState(addPos.add(high)).getBlock();
                           Block addSideState = mc.world.getBlockState(addSide.add(high)).getBlock();
                           if (this.blockList.contains(sideState)) {
                              block += 3;
                           }

                           if (sideState == Blocks.BEDROCK) {
                              bedrock += 3;
                           }

                           if (this.blockList.contains(addState)) {
                              block += 3;
                           }

                           if (addState == Blocks.BEDROCK) {
                              bedrock += 3;
                           }

                           if (this.blockList.contains(addSideState)) {
                              block++;
                           }

                           if (addSideState == Blocks.BEDROCK) {
                              bedrock++;
                           }
                        }

                        boolean shouldSet = false;
                        if (block > blocks) {
                           shouldSet = true;
                        } else if (block == blocks && bedrock > bedrocks) {
                           shouldSet = true;
                        }

                        if (shouldSet) {
                           bedrocks = bedrock;
                           blocks = block;
                           xAdd = this.getAdd(sidexx.x + add.x);
                           zAdd = this.getAdd(sidexx.z + add.z);
                        }
                     }
                  }
               }

               mc.player
                  .setPosition(this.originalPos.getX() + xAdd, this.originalPos.getY(), this.originalPos.getZ() + zAdd);
               mc.player.motionX = 0.0;
               mc.player.motionZ = 0.0;
               if (mc.player.posX == this.originalPos.getX() + xAdd
                  && mc.player.posZ == this.originalPos.getZ() + zAdd
                  && !mc.world.isAirBlock(this.originalPos)
                  && this.timing.passedMs(this.checkDelay.getValue().intValue())) {
                  this.down = true;
               }
            }
         }

         if (this.down) {
            this.timing.reset();
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
            if (this.mine.getValue()) {
               mc.playerController.onPlayerDamageBlock(this.originalPos, EnumFacing.UP);
            } else {
               this.disable();
            }

            if (mc.world.isAirBlock(this.originalPos)) {
               this.disable();
            }
         }
      }
   }

   private double getAdd(int pos) {
      return pos == 1 ? 0.99999999 : 0.0;
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   public static void rightClickBlock(BlockPos pos, EnumFacing facing, Vec3d hVec, boolean packet, boolean swing) {
      Vec3d hitVec = new Vec3d(pos).add(hVec).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (packet) {
         rightClickBlock(pos, hitVec, EnumHand.MAIN_HAND, facing);
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND);
      }

      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction) {
      float f = (float)(vec.x - pos.getX());
      float f1 = (float)(vec.y - pos.getY());
      float f2 = (float)(vec.z - pos.getZ());
      mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem)
            && (!(entity instanceof EntityEnderCrystal) || !this.ignoreCrystal.getValue())
            && entity != mc.player
            && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }
}
