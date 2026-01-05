package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoAnvil", category = Category.Combat)
public class AutoAnvil extends Module {
   ModeSetting anvilMode = this.registerMode("Mode", Arrays.asList("Pick", "Feet", "None"), "Pick");
   ModeSetting target = this.registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", false);
   BooleanSetting packetPlace = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   DoubleSetting enemyRange = this.registerDouble("Range", 5.9, 0.0, 6.0);
   DoubleSetting decrease = this.registerDouble("Decrease", 2.0, 0.0, 6.0);
   IntegerSetting tickDelay = this.registerInteger("Tick Delay", 5, 0, 10);
   IntegerSetting blocksPerTick = this.registerInteger("Blocks Per Tick", 4, 0, 8);
   IntegerSetting hDistance = this.registerInteger("H Distance", 7, 1, 10);
   IntegerSetting minH = this.registerInteger("Min H", 3, 1, 10);
   IntegerSetting maxH = this.registerInteger("Max H", 3, 1, 10);
   private boolean noMaterials = false;
   private boolean enoughSpace = true;
   private boolean blockUp = false;
   private int[] slot_mat = new int[]{-1, -1, -1};
   private double[] enemyCoords;
   int[][] model = new int[][]{{1, 1, 1}, {-1, 1, -1}, {-1, 1, 1}, {1, 1, -1}};
   private int blocksPlaced = 0;
   private int delayTimeTicks = 0;
   private int offsetSteps = 0;
   private BlockPos base;
   private EntityPlayer aimTarget;
   private static ArrayList<Vec3d> to_place = new ArrayList<>();

   @Override
   public void onEnable() {
      this.blocksPlaced = 0;
      this.blockUp = false;
      this.slot_mat = new int[]{-1, -1, -1};
      to_place = new ArrayList<>();
      if (mc.player == null) {
         this.disable();
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         if (this.noMaterials) {
            this.setDisabledMessage("No Materials Detected... AutoAnvil turned OFF!");
         } else if (!this.enoughSpace) {
            this.setDisabledMessage("Not enough space... AutoAnvil turned OFF!");
         } else if (this.blockUp) {
            this.setDisabledMessage("Enemy head blocked.. AutoAnvil turned OFF!");
         }

         this.noMaterials = false;
      }
   }

   @Override
   public void onUpdate() {
      if (mc.player == null) {
         this.disable();
      } else {
         if (this.target.getValue().equals("Nearest")) {
            this.aimTarget = PlayerUtil.getNearestPlayer(this.enemyRange.getValue());
         } else if (this.target.getValue().equals("Looking")) {
            this.aimTarget = PlayerUtil.findLookingPlayer(this.enemyRange.getValue());
         }

         if (this.aimTarget != null && !mc.player.isDead) {
            if (this.getMaterialsSlot()) {
               this.enemyCoords = new double[]{this.aimTarget.posX, this.aimTarget.posY, this.aimTarget.posZ};
               this.enoughSpace = this.createStructure();
            } else {
               this.noMaterials = true;
            }

            if (!this.noMaterials && this.enoughSpace && !this.blockUp) {
               if (this.delayTimeTicks < this.tickDelay.getValue()) {
                  this.delayTimeTicks++;
               } else {
                  this.delayTimeTicks = 0;
                  if (!BlockUtil.isAir(new BlockPos(this.enemyCoords[0], this.enemyCoords[1] + 2.0, this.enemyCoords[2]))
                     && !(BlockUtil.getBlock(this.enemyCoords[0], this.enemyCoords[1] + 2.0, this.enemyCoords[2]) instanceof BlockAnvil)) {
                     this.blockUp = true;
                  }

                  for (this.blocksPlaced = 0; this.blocksPlaced <= this.blocksPerTick.getValue(); this.offsetSteps++) {
                     int maxSteps = to_place.size();
                     if (this.offsetSteps >= maxSteps) {
                        this.offsetSteps = 0;
                        break;
                     }

                     BlockPos offsetPos = new BlockPos(to_place.get(this.offsetSteps));
                     BlockPos targetPos = new BlockPos(this.enemyCoords[0], this.enemyCoords[1], this.enemyCoords[2])
                        .add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
                     boolean tryPlacing = true;
                     if (this.offsetSteps > 0 && this.offsetSteps < to_place.size() - 1) {
                        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(targetPos))) {
                           if (entity instanceof EntityPlayer) {
                              tryPlacing = false;
                              break;
                           }
                        }
                     }

                     if (tryPlacing && this.placeBlock(targetPos, this.offsetSteps)) {
                        this.blocksPlaced++;
                     }
                  }

                  BlockPos instantPos = null;
                  if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                     instantPos = PacketMine.INSTANCE.packetPos;
                  }

                  if (this.anvilMode.getValue().equalsIgnoreCase("Pick")
                     && (instantPos == null || !instantPos.equals(new BlockPos(this.enemyCoords[0], this.enemyCoords[1], this.enemyCoords[2])))) {
                     mc.playerController.onPlayerDamageBlock(new BlockPos(this.enemyCoords[0], this.enemyCoords[1], this.enemyCoords[2]), EnumFacing.UP);
                  }
               }
            } else {
               this.disable();
            }
         } else {
            this.disable();
         }
      }
   }

   private boolean placeBlock(BlockPos pos, int step) {
      if (this.intersectsWithEntity(pos)) {
         return false;
      } else if (!BlockUtil.canReplace(pos)) {
         return false;
      } else {
         int utilSlot = step == 0 && this.anvilMode.getValue().equalsIgnoreCase("feet") ? 2 : (step >= to_place.size() - 1 ? 1 : 0);
         if (utilSlot == 0 && BlockUtil.canBeClicked(this.base)) {
            return false;
         } else {
            int slot = this.slot_mat[utilSlot];
            int oldslot = mc.player.inventory.currentItem;
            if (mc.player.inventory.getStackInSlot(slot) != ItemStack.EMPTY) {
               if (oldslot != slot) {
                  if (this.packetSwitch.getValue()) {
                     mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                  } else {
                     mc.player.inventory.currentItem = slot;
                  }
               } else {
                  oldslot = -1;
               }

               BurrowUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packetPlace.getValue(), false, this.swing.getValue());
               if (oldslot != -1) {
                  if (this.packetSwitch.getValue()) {
                     mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
                  } else {
                     mc.player.inventory.currentItem = oldslot;
                  }
               }

               return true;
            } else {
               return false;
            }
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

   private boolean getMaterialsSlot() {
      boolean feet = this.anvilMode.getValue().equalsIgnoreCase("Feet");

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block instanceof BlockObsidian) {
               this.slot_mat[0] = i;
            } else if (block instanceof BlockAnvil) {
               this.slot_mat[1] = i;
            } else if (feet && (block instanceof BlockPressurePlate || block instanceof BlockButton)) {
               this.slot_mat[2] = i;
            }
         }
      }

      int count = 0;

      for (int val : this.slot_mat) {
         if (val != -1) {
            count++;
         }
      }

      return count - (feet ? 1 : 0) == 2;
   }

   private boolean createStructure() {
      to_place = new ArrayList<>();
      if (this.anvilMode.getValue().equalsIgnoreCase("feet")) {
         to_place.add(new Vec3d(0.0, 0.0, 0.0));
      }

      int hDistanceMod = this.hDistance.getValue();

      for (double distEnemy = mc.player.getDistance(this.aimTarget); distEnemy > this.decrease.getValue(); distEnemy -= this.decrease.getValue()) {
         hDistanceMod--;
      }

      hDistanceMod += (int)(mc.player.posY - this.aimTarget.posY);
      double min_found = Double.MAX_VALUE;
      int cor = -1;
      int i = 0;
      BlockPos[] posList = new BlockPos[]{
         new BlockPos(this.enemyCoords[0] + 1.0, this.enemyCoords[1], this.enemyCoords[2] + 1.0),
         new BlockPos(this.enemyCoords[0] - 1.0, this.enemyCoords[1], this.enemyCoords[2] - 1.0),
         new BlockPos(this.enemyCoords[0] - 1.0, this.enemyCoords[1], this.enemyCoords[2] + 1.0),
         new BlockPos(this.enemyCoords[0] + 1.0, this.enemyCoords[1], this.enemyCoords[2] - 1.0)
      };

      for (BlockPos pos : posList) {
         boolean breakOut = false;

         for (int h = 0; h <= this.minH.getValue(); h++) {
            if (BlockUtil.checkEntity(pos.up(h))) {
               breakOut = true;
               i++;
               break;
            }
         }

         if (!breakOut) {
            double distance_now = mc.player.getDistanceSq(pos);
            if (distance_now < min_found) {
               min_found = distance_now;
               cor = i;
            }

            i++;
         }
      }

      if (cor == -1) {
         return false;
      } else {
         List<Vec3d> baseList = new ArrayList<>();
         baseList.add(new Vec3d(this.model[cor][0], this.model[cor][1] - 1, this.model[cor][2]));
         baseList.add(new Vec3d(this.model[cor][0], this.model[cor][1], this.model[cor][2]));

         int incr;
         for (incr = 1;
            incr != this.maxH.getValue()
               && BlockUtil.getBlock(this.enemyCoords[0], this.enemyCoords[1] + incr, this.enemyCoords[2]) instanceof BlockAir
               && incr < hDistanceMod;
            incr++
         ) {
            baseList.add(new Vec3d(this.model[cor][0], this.model[cor][1] + incr, this.model[cor][2]));
         }

         boolean possible = incr >= this.minH.getValue() && incr <= this.maxH.getValue();
         BlockPos targetPos = new BlockPos(this.enemyCoords[0], this.enemyCoords[1], this.enemyCoords[2]);
         double x = mc.player.getDistanceSq(new BlockPos(targetPos).add(this.model[cor][0], 0, 0));
         double z = mc.player.getDistanceSq(new BlockPos(targetPos).add(0, 0, this.model[cor][2]));
         Vec3d base = new Vec3d(this.model[cor][0], this.model[cor][1] + incr - 1, 0.0);
         if (x > z) {
            base = new Vec3d(0.0, this.model[cor][1] + incr - 1, this.model[cor][2]);
         }

         this.base = targetPos.add(base.x, base.y, base.z);
         to_place.add(base);
         double yRef = base.y;
         if (BurrowUtil.getFirstFacing(targetPos.add(0.0, yRef, 0.0)) == null) {
            to_place.addAll(baseList);
         }

         to_place.add(new Vec3d(0.0, yRef, 0.0));
         return possible;
      }
   }
}
