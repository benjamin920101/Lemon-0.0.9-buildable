package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "ArmorRegear", category = Category.Combat)
public class ArmorRegear extends Module {
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 10.0);
   IntegerSetting delay = this.registerInteger("Delay", 1, 0, 20);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting packetPlace = this.registerBoolean("Packet Place", true);
   int waited = 0;
   int actionSlot;
   int slot;
   boolean placed;
   ItemStack shulker;
   ArmorRegear.ShulkerPos pos;

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

   private int getSlot(ItemStack itemStack) {
      NonNullList<ItemStack> contentItems = NonNullList.withSize(27, ItemStack.EMPTY);
      ItemStackHelper.loadAllItems(itemStack.getTagCompound().getCompoundTag("BlockEntityTag"), contentItems);

      for (int i = 0; i < contentItems.size(); i++) {
         if (((ItemStack)contentItems.get(i)).getItem() instanceof ItemArmor && ((ItemStack)contentItems.get(i)).getCount() == 127) {
            return i;
         }
      }

      return -1;
   }

   private int getSlot(ItemStack itemStack, EntityEquipmentSlot equipmentSlot) {
      NonNullList<ItemStack> contentItems = NonNullList.withSize(27, ItemStack.EMPTY);
      ItemStackHelper.loadAllItems(itemStack.getTagCompound().getCompoundTag("BlockEntityTag"), contentItems);

      for (int i = 0; i < contentItems.size(); i++) {
         ItemStack stack = (ItemStack)contentItems.get(i);
         if (stack.getItem() instanceof ItemArmor && stack.getCount() == 127 && ((ItemArmor)stack.getItem()).armorType.equals(equipmentSlot)
            )
          {
            return i;
         }
      }

      return -1;
   }

   @Override
   public void onEnable() {
      this.actionSlot = 5;
      this.slot = -1;
      this.pos = null;
      this.placed = false;
      this.shulker = null;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack itemStack = mc.player.inventory.getStackInSlot(slot);
         if (itemStack.getItem() instanceof ItemBlock
            && ((ItemBlock)itemStack.getItem()).getBlock() instanceof BlockShulkerBox
            && this.getSlot(itemStack) != -1) {
            this.slot = slot;
            this.shulker = mc.player.inventory.getStackInSlot(slot);
            break;
         }
      }

      if (this.slot != -1) {
         this.pos = this.initValues();
      }

      this.placed = this.pos == null;
   }

   private ArmorRegear.ShulkerPos initValues() {
      List<BlockPos> blocks = EntityUtil.getSphere(PlayerUtil.getEyesPos(), this.range.getValue() + 1.0, this.range.getValue() + 1.0, false, true, 0);
      blocks.removeIf(p -> ColorMain.INSTANCE.breakList.contains(p));
      List<ArmorRegear.ShulkerPos> posList = new ArrayList<>();
      blocks.forEach(pos -> {
         EnumFacing facing = this.getFacing(pos);
         if (facing != null) {
            BlockPos neighbour = pos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            if (this.inRange(hitVec)) {
               posList.add(new ArmorRegear.ShulkerPos(pos, facing, neighbour, opposite, hitVec));
            }
         }
      });
      EntityPlayer target = PlayerUtil.getNearestPlayer(12.0);
      ArmorRegear.ShulkerPos blockAim;
      if (target == null) {
         blockAim = posList.stream().min(Comparator.comparing(p -> p.getRange(mc.player))).orElse(null);
      } else {
         blockAim = posList.stream().max(Comparator.comparing(p -> this.getWeight(p, target))).orElse(null);
      }

      return blockAim;
   }

   @Override
   public void onTick() {
      if (this.actionSlot > 8) {
         this.disable();
      } else if (this.waited++ >= this.delay.getValue()) {
         if (((Slot)mc.player.inventoryContainer.inventorySlots.get(this.actionSlot)).getStack().getCount() == 127) {
            this.actionSlot++;
         } else {
            this.waited = 0;
            if (mc.player.openContainer instanceof ContainerShulkerBox && this.slot != -1) {
               int armorSlot = this.getSlot(this.shulker, fromSlot(this.actionSlot));
               ItemStack slotStack = mc.player.inventory.getStackInSlot(this.slot);
               if (slotStack.isEmpty()
                  || !(slotStack.getItem() instanceof ItemArmor)
                  || !((ItemArmor)slotStack.getItem()).armorType.equals(fromSlot(this.actionSlot))
                  || slotStack.getCount() != 127) {
                  if (!slotStack.isEmpty()) {
                     mc.playerController.windowClick(0, this.slot, 1, ClickType.THROW, mc.player);
                  }

                  ItemStack armorStack = ((Slot)mc.player.openContainer.inventorySlots.get(armorSlot)).getStack();
                  if (!armorStack.isEmpty()) {
                     mc.playerController.windowClick(mc.player.openContainer.windowId, armorSlot, this.slot, ClickType.SWAP, mc.player);
                  } else {
                     mc.player.closeScreen();
                  }

                  return;
               }

               mc.player.closeScreen();
            }

            List<ArmorRegear.InvStack> armors = new ArrayList<>();

            for (int slot = 0; slot < 45; slot++) {
               if (slot <= 4 || slot >= 9) {
                  ArmorRegear.InvStack invStack = new ArmorRegear.InvStack(slot, mc.player.inventoryContainer.getSlot(slot).getStack());
                  if (invStack.stack.getItem() instanceof ItemArmor && invStack.stack.getCount() == 127) {
                     armors.add(invStack);
                  }
               }
            }

            ArmorRegear.InvStack stack = armors.stream()
               .filter(invStackx -> ((ItemArmor)invStackx.stack.getItem()).armorType.equals(fromSlot(this.actionSlot)))
               .min(Comparator.comparing(invStackx -> invStackx.slot))
               .orElse(null);
            if (stack == null) {
               if (!this.placed) {
                  this.switchTo(
                     this.slot,
                     () -> {
                        boolean sneak = false;
                        if (BlockUtil.blackList.contains(mc.world.getBlockState(this.pos.neighbour).getBlock())
                           && !mc.player.isSneaking()) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                           sneak = true;
                        }

                        BurrowUtil.rightClickBlock(this.pos.neighbour, this.pos.vec, EnumHand.MAIN_HAND, this.pos.opposite, this.packetPlace.getValue());
                        if (sneak) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                        }

                        this.waited = 0;
                     }
                  );
                  this.placed = true;
               } else if (this.pos != null
                  && BlockUtil.getBlock(this.pos.pos) instanceof BlockShulkerBox
                  && this.getSlot(this.shulker, fromSlot(this.actionSlot)) != -1) {
                  this.openBlock();
               } else {
                  this.disable();
               }
            } else {
               this.swapStack(stack.slot, this.actionSlot);
               this.actionSlot++;
            }
         }
      }
   }

   private void openBlock() {
      EnumFacing side = EnumFacing.getDirectionFromEntityLiving(this.pos.pos, mc.player);
      BlockPos neighbour = this.pos.pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
      mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      mc.playerController.processRightClickBlock(mc.player, mc.world, this.pos.pos, opposite, hitVec, EnumHand.MAIN_HAND);
   }

   private void swapStack(int slotFrom, int slotTo) {
      mc.playerController.windowClick(0, slotTo, 1, ClickType.THROW, mc.player);
      int slot = slotFrom - 36;
      InventoryUtil.run(slot, this.packetSwitch.getValue(), () -> mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND)));
   }

   public static ItemStack get(int slot) {
      return slot == -2 ? mc.player.inventory.getItemStack() : (ItemStack)mc.player.inventoryContainer.getInventory().get(slot);
   }

   public static EntityEquipmentSlot fromSlot(int slot) {
      switch (slot) {
         case 5:
            return EntityEquipmentSlot.HEAD;
         case 6:
            return EntityEquipmentSlot.CHEST;
         case 7:
            return EntityEquipmentSlot.LEGS;
         case 8:
            return EntityEquipmentSlot.FEET;
         default:
            return null;
      }
   }

   private double getWeight(ArmorRegear.ShulkerPos pos, EntityPlayer target) {
      double range = pos.getRange(target);
      if (range >= 4.0) {
         int y = 256 - pos.pos.getY();
         range += y * 100;
      }

      return range;
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private EnumFacing getFacing(BlockPos pos) {
      if (!this.intersectsWithEntity(pos) && (BlockUtil.canReplace(pos) || BlockUtil.getBlock(pos) instanceof BlockShulkerBox)) {
         for (EnumFacing facing : EnumFacing.VALUES) {
            if (BlockUtil.canBeClicked(pos.offset(facing))
               && BlockUtil.airBlocks.contains(mc.world.getBlockState(pos.offset(facing, -1)).getBlock())) {
               return facing;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean inRange(Vec3d vec) {
      double x = vec.x - mc.player.posX;
      double z = vec.z - mc.player.posZ;
      double y = vec.y - PlayerUtil.getEyesPos().y;
      double add = Math.sqrt(y * y) / 2.0;
      return x * x + z * z <= (this.range.getValue() - add) * (this.range.getValue() - add) && y * y <= this.range.getValue() * this.range.getValue();
   }

   public static class InvStack {
      public final int slot;
      public final ItemStack stack;

      public InvStack(int slot, ItemStack stack) {
         this.slot = slot;
         this.stack = stack;
      }
   }

   static class ShulkerPos {
      BlockPos pos;
      EnumFacing facing;
      Vec3d vec;
      BlockPos neighbour;
      EnumFacing opposite;

      public ShulkerPos(BlockPos pos, EnumFacing facing, BlockPos neighbour, EnumFacing opposite, Vec3d vec3d) {
         this.pos = pos;
         this.facing = facing;
         this.neighbour = neighbour;
         this.opposite = opposite;
         this.vec = vec3d;
      }

      public double getRange(EntityPlayer player) {
         return player.getDistance(this.pos.x + 0.5, this.pos.y + 0.5, this.pos.z + 0.5);
      }
   }
}
