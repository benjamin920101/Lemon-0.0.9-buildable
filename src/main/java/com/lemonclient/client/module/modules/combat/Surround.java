package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.CrystalUtil;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;

@Module.Declaration(name = "Surround", category = Category.Combat)
public class Surround extends Module {
   ModeSetting time = this.registerMode("Time Mode", Arrays.asList("Tick", "onUpdate", "Fast"), "Tick");
   BooleanSetting once = this.registerBoolean("Once", true);
   BooleanSetting echest = this.registerBoolean("Ender Chest", true);
   BooleanSetting floor = this.registerBoolean("Floor", true);
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 20);
   IntegerSetting range = this.registerInteger("Range", 5, 0, 10);
   IntegerSetting bpt = this.registerInteger("BlocksPerTick", 4, 0, 20);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting packet = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting forceBase = this.registerBoolean("Force Base", false);
   BooleanSetting breakCrystal = this.registerBoolean("Break Crystal", false);
   BooleanSetting packetBreak = this.registerBoolean("Packet Break", false, () -> this.breakCrystal.getValue());
   BooleanSetting antiWeakness = this.registerBoolean("Anti Weakness", false, () -> this.breakCrystal.getValue());
   BooleanSetting weakBypass = this.registerBoolean("Bypass Switch", false, () -> this.breakCrystal.getValue());
   BooleanSetting silent = this.registerBoolean("Silent Switch", false, () -> !this.weakBypass.getValue() && this.breakCrystal.getValue());
   List<EntityEnderCrystal> crystals = new ArrayList<>();
   List<BlockPos> surround = new ArrayList<>();
   List<BlockPos> hasEntity = new ArrayList<>();
   List<BlockPos> posList = new ArrayList<>();
   List<BlockPos> floorPos = new ArrayList<>();
   int placed;
   int waited;
   int slot;
   double y;
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};
   BlockPos[] neighbour = new BlockPos[]{
      new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(0, 1, 0)
   };
   @EventHandler
   private final Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(
      event -> {
         if (event.getMovementInput() instanceof MovementInputFromOptions) {
            if (event.getMovementInput().jump) {
               this.disable();
            }

            if (event.getMovementInput().forwardKeyDown
               || event.getMovementInput().backKeyDown
               || event.getMovementInput().leftKeyDown
               || event.getMovementInput().rightKeyDown) {
               double posY = mc.player.posY - this.y;
               if (posY * posY > 0.25) {
                  this.disable();
               }
            }
         }
      }
   );

   @Override
   public void onEnable() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.y = mc.player.posY;
      } else {
         this.disable();
      }
   }

   @Override
   public void onUpdate() {
      if (this.time.getValue().equals("onUpdate")) {
         this.doSurround();
      }
   }

   @Override
   public void onTick() {
      if (this.time.getValue().equals("Tick")) {
         this.doSurround();
      }
   }

   @Override
   public void fast() {
      if (this.time.getValue().equals("Fast")) {
         this.doSurround();
      }
   }

   private void doSurround() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.slot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         if (this.slot == -1 && this.echest.getValue()) {
            this.slot = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
         }

         if (this.slot != -1) {
            if (this.waited++ >= this.delay.getValue()) {
               this.waited = this.placed = 0;
               this.calc();
               if (this.breakCrystal.getValue() && !this.crystals.isEmpty()) {
                  Entity crystal = null;
                  Iterator pos = this.crystals.iterator();
                  if (pos.hasNext()) {
                     EntityEnderCrystal enderCrystal = (EntityEnderCrystal)pos.next();
                     crystal = enderCrystal;
                  }

                  if (crystal != null) {
                     CrystalUtil.breakCrystal(
                        crystal,
                        this.packetBreak.getValue(),
                        this.swing.getValue(),
                        this.packetSwitch.getValue(),
                        this.silent.getValue(),
                        this.antiWeakness.getValue(),
                        this.weakBypass.getValue()
                     );
                  }
               }

               if (this.floor.getValue()) {
                  for (BlockPos posx : this.floorPos) {
                     this.surround.add(posx.down());
                  }
               }

               if (!this.surround.isEmpty()) {
                  for (BlockPos posx : this.surround) {
                     if (this.placed >= this.bpt.getValue()) {
                        break;
                     }

                     if (mc.world.isAirBlock(posx)
                        || mc.world.getBlockState(posx).getBlock() == Blocks.FIRE
                        || mc.world.getBlockState(posx).getBlock() instanceof BlockLiquid) {
                        EnumFacing face = BurrowUtil.getFirstFacing(posx);
                        if (face == null || this.forceBase.getValue()) {
                           boolean canPlace = false;

                           for (BlockPos side : this.neighbour) {
                              BlockPos blockPos = posx.add(side);
                              if (!this.intersectsWithEntity(blockPos) && BlockUtil.hasNeighbour(blockPos)) {
                                 this.placeBlock(blockPos, BurrowUtil.getFirstFacing(blockPos));
                                 canPlace = true;
                                 break;
                              }
                           }

                           if (!canPlace) {
                              continue;
                           }

                           face = BurrowUtil.getFirstFacing(posx);
                        }

                        this.placeBlock(posx, face);
                     }
                  }

                  if (this.once.getValue()) {
                     this.disable();
                  }
               }
            }
         }
      } else {
         this.disable();
      }
   }

   private void placeBlock(BlockPos pos, EnumFacing side) {
      if (this.placed < this.bpt.getValue()) {
         if (!this.intersectsWithEntity(pos)) {
            if (side != null) {
               BlockPos neighbour = pos.offset(side);
               EnumFacing opposite = side.getOpposite();
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               if ((
                     BlockUtil.blackList.contains(mc.world.getBlockState(neighbour).getBlock())
                        || BlockUtil.shulkerList.contains(mc.world.getBlockState(neighbour).getBlock())
                  )
                  && !mc.player.isSneaking()) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                  mc.player.setSneaking(true);
               }

               if (this.rotate.getValue()) {
                  BurrowUtil.faceVector(hitVec, true);
               }

               InventoryUtil.run(
                  this.slot,
                  this.packetSwitch.getValue(),
                  () -> BurrowUtil.rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite, this.packet.getValue(), this.swing.getValue())
               );
               this.placed++;
            }
         }
      }
   }

   private void calc() {
      this.crystals = new ArrayList<>();
      this.surround = new ArrayList<>();
      this.hasEntity = new ArrayList<>();
      this.posList = new ArrayList<>();
      this.floorPos = new ArrayList<>();
      BlockPos playerPos = PlayerUtil.getPlayerPos();
      this.addPos(playerPos);
      if (playerPos.y != (int)mc.player.posY) {
         this.addPos(PlayerUtil.getPlayerFloorPos());
      }

      if (!this.hasEntity.isEmpty()) {
         this.entityCalc();
      }
   }

   private void entityCalc() {
      this.posList = new ArrayList<>();
      this.posList.addAll(this.hasEntity);
      this.hasEntity = new ArrayList<>();

      for (BlockPos pos : this.posList) {
         this.addPos(pos);
      }

      this.hasEntity
         .removeIf(
            blockPos -> blockPos == null
               || this.floorPos.contains(blockPos)
               || mc.player.getDistanceSq(blockPos) > this.range.getValue() * this.range.getValue()
         );
      this.surround.removeIf(blockPos -> blockPos == null || mc.player.getDistanceSq(blockPos) > this.range.getValue() * this.range.getValue());
      if (!this.hasEntity.isEmpty()) {
         this.entityCalc();
      }
   }

   private void addPos(BlockPos pos) {
      if (!this.floorPos.contains(pos)) {
         for (BlockPos side : this.sides) {
            BlockPos blockPos = pos.add(side);
            if (this.intersectsWithEntity(blockPos)) {
               this.hasEntity.add(blockPos);
            } else {
               this.surround.add(blockPos);
            }
         }

         this.floorPos.add(pos);
      }
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            if (entity instanceof EntityEnderCrystal) {
               this.crystals.add((EntityEnderCrystal)entity);
            } else if (entity instanceof EntityPlayer) {
               return true;
            }
         }
      }

      return false;
   }
}
