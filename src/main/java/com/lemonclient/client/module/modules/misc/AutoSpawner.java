package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AutoSpawner", category = Category.Misc)
public class AutoSpawner extends Module {
   ModeSetting useMode = this.registerMode("Use Mode", Arrays.asList("Single", "Spam"), "Spam");
   BooleanSetting party = this.registerBoolean("Wither Party", false);
   ModeSetting entityMode = this.registerMode("Entity Mode", Arrays.asList("Snow", "Iron", "Wither"), "Wither");
   BooleanSetting nametagWithers = this.registerBoolean("Nametag", true);
   DoubleSetting placeRange = this.registerDouble("Place Range", 3.5, 1.0, 10.0);
   IntegerSetting delay = this.registerInteger("Delay", 20, 0, 100);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting check = this.registerBoolean("Switch Check", true);
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   private static boolean isSneaking;
   private BlockPos placeTarget;
   private boolean rotationPlaceableX;
   private boolean rotationPlaceableZ;
   private int bodySlot;
   private int headSlot;
   private int buildStage;
   private int delayStep;

   private void useNameTag() {
      int originalSlot = mc.player.inventory.currentItem;

      for (Entity w : mc.world.getLoadedEntityList()) {
         if (w instanceof EntityWither && w.getDisplayName().getUnformattedText().equalsIgnoreCase("Wither")) {
            EntityWither wither = (EntityWither)w;
            if (mc.player.getDistance(wither) <= this.placeRange.getValue()) {
               this.selectNameTags();
               mc.playerController.interactWithEntity(mc.player, wither, EnumHand.MAIN_HAND);
            }
         }
      }

      this.switchTo(originalSlot);
   }

   private void selectNameTags() {
      int tagSlot = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock)) {
            Item tag = stack.getItem();
            if (tag instanceof ItemNameTag) {
               tagSlot = i;
            }
         }
      }

      if (tagSlot != -1) {
         this.switchTo(tagSlot);
      }
   }

   private static EnumFacing getPlaceableSide(BlockPos pos) {
      for (EnumFacing side : EnumFacing.values()) {
         BlockPos neighbour = pos.offset(side);
         if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable()
               && !(blockState.getBlock() instanceof BlockTallGrass)
               && !(blockState.getBlock() instanceof BlockDeadBush)) {
               return side;
            }
         }
      }

      return null;
   }

   @Override
   protected void onEnable() {
      this.buildStage = 1;
      this.delayStep = 1;
   }

   private boolean checkBlocksInHotbar() {
      this.headSlot = -1;
      this.bodySlot = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (this.entityMode.getValue().equals("Wither")) {
               if (stack.getItem() == Items.SKULL && stack.getItemDamage() == 1) {
                  if (mc.player.inventory.getStackInSlot(i).stackSize >= 3) {
                     this.headSlot = i;
                  }
                  continue;
               }

               if (!(stack.getItem() instanceof ItemBlock)) {
                  continue;
               }

               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if (block instanceof BlockSoulSand && mc.player.inventory.getStackInSlot(i).stackSize >= 4) {
                  this.bodySlot = i;
               }
            }

            if (this.entityMode.getValue().equals("Iron")) {
               if (!(stack.getItem() instanceof ItemBlock)) {
                  continue;
               }

               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if ((block == Blocks.LIT_PUMPKIN || block == Blocks.PUMPKIN) && mc.player.inventory.getStackInSlot(i).stackSize >= 1) {
                  this.headSlot = i;
               }

               if (block == Blocks.IRON_BLOCK && mc.player.inventory.getStackInSlot(i).stackSize >= 4) {
                  this.bodySlot = i;
               }
            }

            if (this.entityMode.getValue().equals("Snow") && stack.getItem() instanceof ItemBlock) {
               Block blockx = ((ItemBlock)stack.getItem()).getBlock();
               if ((blockx == Blocks.LIT_PUMPKIN || blockx == Blocks.PUMPKIN) && mc.player.inventory.getStackInSlot(i).stackSize >= 1
                  )
                {
                  this.headSlot = i;
               }

               if (blockx == Blocks.SNOW && mc.player.inventory.getStackInSlot(i).stackSize >= 2) {
                  this.bodySlot = i;
               }
            }
         }
      }

      return this.bodySlot != -1 && this.headSlot != -1;
   }

   private boolean testStructure() {
      if (this.entityMode.getValue().equals("Wither")) {
         return this.testWitherStructure();
      } else if (this.entityMode.getValue().equals("Iron")) {
         return this.testIronGolemStructure();
      } else {
         return this.entityMode.getValue().equals("Snow") ? this.testSnowGolemStructure() : false;
      }
   }

   private boolean testWitherStructure() {
      boolean noRotationPlaceable = true;
      this.rotationPlaceableX = true;
      this.rotationPlaceableZ = true;
      boolean isShitGrass = false;
      if (mc.world.getBlockState(this.placeTarget) == null) {
         return false;
      } else {
         Block block = mc.world.getBlockState(this.placeTarget).getBlock();
         if (block instanceof BlockTallGrass || block instanceof BlockDeadBush) {
            isShitGrass = true;
         }

         if (getPlaceableSide(this.placeTarget.up()) == null) {
            return false;
         } else {
            for (BlockPos pos : AutoSpawner.BodyParts.bodyBase) {
               if (this.placingIsBlocked(this.placeTarget.add(pos))) {
                  noRotationPlaceable = false;
               }
            }

            for (BlockPos posx : AutoSpawner.BodyParts.ArmsX) {
               if (this.placingIsBlocked(this.placeTarget.add(posx)) || this.placingIsBlocked(this.placeTarget.add(posx.down()))) {
                  this.rotationPlaceableX = false;
               }
            }

            for (BlockPos posxx : AutoSpawner.BodyParts.ArmsZ) {
               if (this.placingIsBlocked(this.placeTarget.add(posxx)) || this.placingIsBlocked(this.placeTarget.add(posxx.down()))
                  )
                {
                  this.rotationPlaceableZ = false;
               }
            }

            for (BlockPos posxxx : AutoSpawner.BodyParts.headsX) {
               if (this.placingIsBlocked(this.placeTarget.add(posxxx))) {
                  this.rotationPlaceableX = false;
               }
            }

            for (BlockPos posxxxx : AutoSpawner.BodyParts.headsZ) {
               if (this.placingIsBlocked(this.placeTarget.add(posxxxx))) {
                  this.rotationPlaceableZ = false;
               }
            }

            return !isShitGrass && noRotationPlaceable && (this.rotationPlaceableX || this.rotationPlaceableZ);
         }
      }
   }

   private boolean testIronGolemStructure() {
      boolean noRotationPlaceable = true;
      this.rotationPlaceableX = true;
      this.rotationPlaceableZ = true;
      boolean isShitGrass = false;
      if (mc.world.getBlockState(this.placeTarget) == null) {
         return false;
      } else {
         Block block = mc.world.getBlockState(this.placeTarget).getBlock();
         if (block instanceof BlockTallGrass || block instanceof BlockDeadBush) {
            isShitGrass = true;
         }

         if (getPlaceableSide(this.placeTarget.up()) == null) {
            return false;
         } else {
            for (BlockPos pos : AutoSpawner.BodyParts.bodyBase) {
               if (this.placingIsBlocked(this.placeTarget.add(pos))) {
                  noRotationPlaceable = false;
               }
            }

            for (BlockPos posx : AutoSpawner.BodyParts.ArmsX) {
               if (this.placingIsBlocked(this.placeTarget.add(posx)) || this.placingIsBlocked(this.placeTarget.add(posx.down()))) {
                  this.rotationPlaceableX = false;
               }
            }

            for (BlockPos posxx : AutoSpawner.BodyParts.ArmsZ) {
               if (this.placingIsBlocked(this.placeTarget.add(posxx)) || this.placingIsBlocked(this.placeTarget.add(posxx.down()))
                  )
                {
                  this.rotationPlaceableZ = false;
               }
            }

            for (BlockPos posxxx : AutoSpawner.BodyParts.head) {
               if (this.placingIsBlocked(this.placeTarget.add(posxxx))) {
                  noRotationPlaceable = false;
               }
            }

            return !isShitGrass && noRotationPlaceable && (this.rotationPlaceableX || this.rotationPlaceableZ);
         }
      }
   }

   private boolean testSnowGolemStructure() {
      boolean noRotationPlaceable = true;
      boolean isShitGrass = false;
      if (mc.world.getBlockState(this.placeTarget) == null) {
         return false;
      } else {
         Block block = mc.world.getBlockState(this.placeTarget).getBlock();
         if (block instanceof BlockTallGrass || block instanceof BlockDeadBush) {
            isShitGrass = true;
         }

         if (getPlaceableSide(this.placeTarget.up()) == null) {
            return false;
         } else {
            for (BlockPos pos : AutoSpawner.BodyParts.bodyBase) {
               if (this.placingIsBlocked(this.placeTarget.add(pos))) {
                  noRotationPlaceable = false;
               }
            }

            for (BlockPos posx : AutoSpawner.BodyParts.head) {
               if (this.placingIsBlocked(this.placeTarget.add(posx))) {
                  noRotationPlaceable = false;
               }
            }

            return !isShitGrass && noRotationPlaceable;
         }
      }
   }

   private void switchTo(int slot) {
      if (slot > -1 && slot < 9 && (!this.check.getValue() || mc.player.inventory.currentItem != slot)) {
         if (this.packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         } else {
            mc.player.inventory.currentItem = slot;
         }

         mc.playerController.updateController();
      }
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (this.nametagWithers.getValue() && (this.party.getValue() || !this.party.getValue() && this.entityMode.getValue().equals("Wither"))) {
            this.useNameTag();
         }

         if (this.buildStage == 1) {
            isSneaking = false;
            this.rotationPlaceableX = false;
            this.rotationPlaceableZ = false;
            if (this.party.getValue()) {
               this.entityMode.setValue("Wither");
            }

            if (!this.checkBlocksInHotbar()) {
               if (this.useMode.getValue().equals("Single")) {
                  this.disable();
               }

               return;
            }

            List<BlockPos> blockPosList = EntityUtil.getSphere(
               mc.player.getPosition().down(), this.placeRange.getValue(), this.placeRange.getValue(), false, true, 0
            );
            boolean noPositionInArea = true;

            for (BlockPos pos : blockPosList) {
               this.placeTarget = pos.down();
               if (this.testStructure()) {
                  noPositionInArea = false;
                  break;
               }
            }

            if (noPositionInArea) {
               if (this.useMode.getValue().equals("Single")) {
                  this.disable();
               }

               return;
            }

            int oldslot = mc.player.inventory.currentItem;
            this.switchTo(this.bodySlot);

            for (BlockPos posx : AutoSpawner.BodyParts.bodyBase) {
               BurrowUtil.placeBlock(
                  this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
               );
            }

            if (this.entityMode.getValue().equals("Wither") || this.entityMode.getValue().equals("Iron")) {
               if (this.rotationPlaceableX) {
                  for (BlockPos posx : AutoSpawner.BodyParts.ArmsX) {
                     BurrowUtil.placeBlock(
                        this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     );
                  }
               } else if (this.rotationPlaceableZ) {
                  for (BlockPos posx : AutoSpawner.BodyParts.ArmsZ) {
                     BurrowUtil.placeBlock(
                        this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     );
                  }
               }
            }

            this.switchTo(oldslot);
            this.buildStage = 2;
         } else if (this.buildStage == 2) {
            int oldslot = mc.player.inventory.currentItem;
            this.switchTo(this.headSlot);
            if (this.entityMode.getValue().equals("Wither")) {
               if (this.rotationPlaceableX) {
                  for (BlockPos posx : AutoSpawner.BodyParts.headsX) {
                     BurrowUtil.placeBlock(
                        this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     );
                  }
               } else if (this.rotationPlaceableZ) {
                  for (BlockPos posx : AutoSpawner.BodyParts.headsZ) {
                     BurrowUtil.placeBlock(
                        this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                     );
                  }
               }
            }

            if (this.entityMode.getValue().equals("Iron") || this.entityMode.getValue().equals("Snow")) {
               for (BlockPos posx : AutoSpawner.BodyParts.head) {
                  BurrowUtil.placeBlock(
                     this.placeTarget.add(posx), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                  );
               }
            }

            if (isSneaking) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
               isSneaking = false;
            }

            if (this.useMode.getValue().equals("Single")) {
               this.disable();
            }

            this.switchTo(oldslot);
            this.buildStage = 3;
         } else if (this.buildStage == 3) {
            if (this.delayStep < this.delay.getValue()) {
               this.delayStep++;
            } else {
               this.delayStep = 1;
               this.buildStage = 1;
            }
         }
      }
   }

   private boolean placingIsBlocked(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      if (!(block instanceof BlockAir)) {
         return true;
      } else {
         for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
               return true;
            }
         }

         return false;
      }
   }

   private static class BodyParts {
      private static final BlockPos[] bodyBase = new BlockPos[]{new BlockPos(0, 1, 0), new BlockPos(0, 2, 0)};
      private static final BlockPos[] ArmsX = new BlockPos[]{new BlockPos(-1, 2, 0), new BlockPos(1, 2, 0)};
      private static final BlockPos[] ArmsZ = new BlockPos[]{new BlockPos(0, 2, -1), new BlockPos(0, 2, 1)};
      private static final BlockPos[] headsX = new BlockPos[]{new BlockPos(0, 3, 0), new BlockPos(-1, 3, 0), new BlockPos(1, 3, 0)};
      private static final BlockPos[] headsZ = new BlockPos[]{new BlockPos(0, 3, 0), new BlockPos(0, 3, -1), new BlockPos(0, 3, 1)};
      private static final BlockPos[] head = new BlockPos[]{new BlockPos(0, 3, 0)};
   }
}
