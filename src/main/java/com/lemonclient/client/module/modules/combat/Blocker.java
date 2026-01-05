package com.lemonclient.client.module.modules.combat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlacementUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.SpoofRotationUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.combat.CrystalUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "Blocker", category = Category.Combat)
public class Blocker extends Module {
   ModeSetting time = this.registerMode("Time Mode", Arrays.asList("Tick", "onUpdate", "Both", "Fast"), "Tick");
   ModeSetting breakType = this.registerMode("Type", Arrays.asList("Vanilla", "Packet"), "Vanilla");
   BooleanSetting packet = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting anvilBlocker = this.registerBoolean("Anvil", true);
   BooleanSetting fallingBlocks = this.registerBoolean("Block FallingBlocks", true);
   BooleanSetting trap = this.registerBoolean("Trap", true, () -> this.fallingBlocks.getValue());
   ModeSetting fallingMode = this.registerMode("Block Mode", Arrays.asList("Break", "Torch", "Skull"), "Break", () -> this.fallingBlocks.getValue());
   BooleanSetting pistonBlocker = this.registerBoolean("Break Piston", true);
   BooleanSetting pistonBlockerNew = this.registerBoolean("Block Piston", true);
   BooleanSetting antiFacePlace = this.registerBoolean("Shift AntiFacePlace", true);
   ModeSetting blockPlaced = this.registerMode("Block Place", Arrays.asList("Pressure", "String"), "String", () -> this.antiFacePlace.getValue());
   IntegerSetting BlocksPerTick = this.registerInteger("Blocks Per Tick", 4, 0, 10);
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 5, 0, 10);
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 10.0);
   DoubleSetting yrange = this.registerDouble("YRange", 5.0, 0.0, 10.0);
   List<BlockPos> pistonList = new ArrayList<>();
   private int delayTimeTicks = 0;
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};

   @Override
   public void onEnable() {
      this.pistonList = new ArrayList<>();
      SpoofRotationUtil.ROTATION_UTIL.onEnable();
      PlacementUtil.onEnable();
   }

   @Override
   public void onDisable() {
      SpoofRotationUtil.ROTATION_UTIL.onDisable();
      PlacementUtil.onDisable();
   }

   @Override
   public void onUpdate() {
      if (this.time.getValue().equals("onUpdate") || this.time.getValue().equals("Both")) {
         this.block();
      }
   }

   @Override
   public void onTick() {
      if (this.time.getValue().equals("Tick") || this.time.getValue().equals("Both")) {
         this.block();
      }
   }

   @Override
   public void fast() {
      if (this.time.getValue().equals("Fast")) {
         this.block();
      }
   }

   private void block() {
      if (mc.player != null && mc.world != null && !mc.player.isDead) {
         if (this.delayTimeTicks < this.tickDelay.getValue()) {
            this.delayTimeTicks++;
         } else {
            SpoofRotationUtil.ROTATION_UTIL.shouldSpoofAngles(true);
            this.delayTimeTicks = 0;
            if (this.anvilBlocker.getValue()) {
               this.blockAnvil();
            }

            if (this.fallingBlocks.getValue()) {
               this.blockFallingBlocks();
            }

            if (this.pistonBlocker.getValue()) {
               this.blockPiston();
            }

            if (this.pistonBlockerNew.getValue()) {
               this.blockPA();
            }

            if (this.antiFacePlace.getValue() && mc.gameSettings.keyBindSneak.isPressed()) {
               this.antiFacePlace();
            }
         }
      } else {
         this.pistonList.clear();
      }
   }

   private List<BlockPos> posList() {
      return EntityUtil.getSphere(PlayerUtil.getPlayerPos(), this.range.getValue(), this.yrange.getValue(), false, false, 0);
   }

   private void antiFacePlace() {
      int blocksPlaced = 0;

      for (Vec3d surround : new Vec3d[]{new Vec3d(1.0, 1.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(0.0, 1.0, -1.0)}) {
         BlockPos pos = new BlockPos(
            mc.player.posX + surround.x, mc.player.posY, mc.player.posZ + surround.z
         );
         Block temp;
         if ((temp = BlockUtil.getBlock(pos)) instanceof BlockObsidian || temp == Blocks.BEDROCK) {
            if (blocksPlaced++ == 0) {
               InventoryUtil.getHotBarPressure(this.blockPlaced.getValue());
            }

            PlacementUtil.placeItem(
               new BlockPos(pos.getX(), pos.getY() + surround.y, pos.getZ()),
               EnumHand.MAIN_HAND,
               this.rotate.getValue(),
               (Class<? extends Item>)Items.STRING.getClass()
            );
            if (blocksPlaced == this.BlocksPerTick.getValue()) {
               return;
            }
         }
      }
   }

   private void blockPA() {
      int slot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      if (slot != -1) {
         for (BlockPos pos : this.posList()) {
            if (!this.pistonList.contains(pos)
               && (
                  mc.world.getBlockState(pos).getBlock() instanceof BlockPistonBase
                     || mc.world.getBlockState(pos).getBlock() == Blocks.PISTON
                     || mc.world.getBlockState(pos).getBlock() == Blocks.STICKY_PISTON
               )) {
               this.pistonList.add(pos);
            }
         }

         this.pistonList.removeIf(blockPos -> mc.player.getDistanceSq(blockPos) > this.range.getValue() * this.range.getValue());
         if (!this.pistonList.isEmpty()) {
            InventoryUtil.run(slot, this.packetSwitch.getValue(), () -> {
               for (BlockPos posx : this.pistonList) {
                  BlockPos head = this.getHeadPos(posx);
                  if (BlockUtil.canReplace(posx) || BlockUtil.canReplace(head)) {
                     BurrowUtil.placeBlock(posx, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                     BurrowUtil.placeBlock(head, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                  }
               }
            });
         }

         this.pistonList.removeIf(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN);
      }
   }

   public BlockPos getHeadPos(BlockPos pos) {
      ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
      UnmodifiableIterator var3 = properties.keySet().iterator();

      while (var3.hasNext()) {
         IProperty<?> prop = (IProperty<?>)var3.next();
         if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {
            BlockPos pushPos = pos.offset((EnumFacing)properties.get(prop));

            for (BlockPos side : this.sides) {
               if (this.isPos2(pos.add(side), pushPos)) {
                  return pos.add(side);
               }
            }
         }
      }

      return null;
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   private void blockAnvil() {
      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityFallingBlock) {
            Block ex = ((EntityFallingBlock)t).fallTile.getBlock();
            if (ex instanceof BlockAnvil
               && (int)t.posX == (int)mc.player.posX
               && (int)t.posZ == (int)mc.player.posZ
               && BlockUtil.getBlock(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ) instanceof BlockAir) {
               this.placeBlock(new BlockPos(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ));
            }
         }
      }
   }

   private void blockFallingBlocks() {
      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityFallingBlock) {
            Block ex = ((EntityFallingBlock)t).fallTile.getBlock();
            if (!(ex instanceof BlockAnvil)
               && (int)t.posX == (int)mc.player.posX
               && (int)t.posZ == (int)mc.player.posZ
               && (int)t.posY > (int)mc.player.posY) {
               if (this.trap.getValue()) {
                  this.placeBlock(new BlockPos(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ));
               }

               int slot = -1;
               String var5 = this.fallingMode.getValue();
               switch (var5) {
                  case "Torch":
                     slot = BurrowUtil.findHotbarBlock(BlockRedstoneTorch.class);
                     break;
                  case "Skull":
                     slot = InventoryUtil.findSkullSlot();
               }

               if (slot != -1) {
                  InventoryUtil.run(
                     slot,
                     this.packetSwitch.getValue(),
                     () -> BurrowUtil.placeBlock(
                        PlayerUtil.getPlayerPos(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     )
                  );
               } else {
                  mc.playerController.onPlayerDamageBlock(PlayerUtil.getPlayerPos(), EnumFacing.UP);
               }
            }
         }
      }
   }

   private void blockPiston() {
      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityEnderCrystal
            && t.posX >= mc.player.posX - 1.5
            && t.posX <= mc.player.posX + 1.5
            && t.posZ >= mc.player.posZ - 1.5
            && t.posZ <= mc.player.posZ + 1.5) {
            for (int i = -2; i < 3; i++) {
               for (int j = -2; j < 3; j++) {
                  if ((i == 0 || j == 0) && BlockUtil.getBlock(t.posX + i, t.posY, t.posZ + j) instanceof BlockPistonBase) {
                     this.breakCrystalPiston(t);
                  }
               }
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      if (mc.world.isAirBlock(pos)) {
         int obsidianSlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         if (obsidianSlot != -1) {
            InventoryUtil.run(obsidianSlot, this.packetSwitch.getValue(), () -> {
               boolean isNull = true;
               if (BurrowUtil.getFirstFacing(pos) == null) {
                  for (BlockPos side : this.sides) {
                     BlockPos added = pos.add(side);
                     if (!this.intersectsWithEntity(added) && BurrowUtil.getFirstFacing(added) != null) {
                        BurrowUtil.placeBlock(added, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
                        isNull = false;
                        break;
                     }
                  }
               } else {
                  isNull = false;
               }

               if (!isNull) {
                  BurrowUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
               }
            });
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

   private void breakCrystalPiston(Entity crystal) {
      if (this.rotate.getValue()) {
         SpoofRotationUtil.ROTATION_UTIL.lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
      }

      if (this.breakType.getValue().equals("Vanilla")) {
         CrystalUtil.breakCrystal(crystal, this.swing.getValue());
      } else {
         CrystalUtil.breakCrystalPacket(crystal, this.swing.getValue());
      }

      if (this.rotate.getValue()) {
         SpoofRotationUtil.ROTATION_UTIL.resetRotation();
      }
   }
}
