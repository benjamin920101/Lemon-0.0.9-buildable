package com.lemonclient.client.module.modules.dev;

import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.combat.AntiBurrow;
import com.lemonclient.client.module.modules.combat.AntiRegear;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lemonclient.mixin.mixins.accessor.AccessorCPacketVehicleMove;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.RayTraceResult.Type;

@Module.Declaration(name = "BedCev", category = Category.Dev)
public class BedCevBreaker extends Module {
   public static BedCevBreaker INSTANCE;
   IntegerSetting slotS = this.registerInteger("Slot", 1, 1, 9);
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 1000);
   BooleanSetting helpBlock = this.registerBoolean("Help Block", true);
   DoubleSetting maxRange = this.registerDouble("Max Range", 5.0, 0.0, 10.0, () -> this.helpBlock.getValue());
   BooleanSetting down = this.registerBoolean("Down Block", true, () -> this.helpBlock.getValue());
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting instantMine = this.registerBoolean("Instant Mine", true);
   BooleanSetting pickBypass = this.registerBoolean("Pick Bypass", false);
   BooleanSetting strict = this.registerBoolean("Strict", false);
   public boolean working;
   boolean offhand;
   boolean start;
   boolean anyBed;
   int blockSlot;
   int bedSlot;
   int pickSlot;
   long time;
   EnumFacing facing;
   Vec2f rotation;
   Timing timer = new Timing();
   BlockPos[] side = new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0)};
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(
      event -> {
         if (this.rotation != null && event.getPhase() == Phase.PRE) {
            PlayerPacket packet = new PlayerPacket(
               this, new Vec2f(this.rotation.x, PlayerPacketManager.INSTANCE.getServerSideRotation().y)
            );
            PlayerPacketManager.INSTANCE.addPacket(packet);
         }
      }
   );
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.rotation != null) {
         if (event.getPacket() instanceof Rotation) {
            ((Rotation)event.getPacket()).yaw = this.rotation.x;
         }

         if (event.getPacket() instanceof PositionRotation) {
            ((PositionRotation)event.getPacket()).yaw = this.rotation.x;
         }

         if (event.getPacket() instanceof CPacketVehicleMove) {
            ((AccessorCPacketVehicleMove)event.getPacket()).setYaw(this.rotation.x);
         }
      }
   });
   BlockPos placePos;
   int lastSlot;
   @EventHandler
   private final Listener<PacketEvent.PostSend> postSendListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         if (event.getPacket() instanceof CPacketHeldItemChange) {
            int slot = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
            if (slot != this.lastSlot) {
               this.lastSlot = slot;
               if (this.strict.getValue()) {
                  EnumFacing facing = BlockUtil.getRayTraceFacing(this.placePos, this.facing);
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, this.placePos, facing));
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.placePos, facing));
                  if (this.swing.getValue()) {
                     mc.player.swingArm(EnumHand.MAIN_HAND);
                  }

                  this.time = System.currentTimeMillis() + this.calcBreakTime();
               }
            }
         }
      }
   });

   public BedCevBreaker() {
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.working = false;
   }

   public void refill_bed() {
      if (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory) {
         int airSlot = this.isSpace();
         if (airSlot != -1) {
            for (int i = 9; i < 36; i++) {
               if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BED) {
                  mc.playerController.windowClick(0, i, airSlot, ClickType.SWAP, mc.player);
               }
            }
         }
      }
   }

   private int isSpace() {
      int slot = -1;
      int slot1 = this.slotS.getValue() - 1;
      if (mc.player.inventory.getStackInSlot(slot1).getItem() != Items.BED) {
         slot = slot1;
      }

      return slot;
   }

   @Override
   public void onEnable() {
      if (mc.objectMouseOver != null
         && mc.objectMouseOver.typeOfHit == Type.BLOCK
         && mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() != Blocks.BEDROCK) {
         this.placePos = mc.objectMouseOver.getBlockPos();
         this.start = this.offhand = false;
         this.getItem();
         this.doBreak();
         this.timer.reset();
      } else {
         this.disable();
      }
   }

   @Override
   public void fast() {
      this.working = false;
      if (mc.world == null || mc.player == null || this.placePos == null || mc.player.isDead) {
         this.disable();
      } else if (this.canPlaceBedWithoutBase() && this.space(this.placePos)) {
         this.refill_bed();
         this.getItem();
         if (!this.anyBed || this.blockSlot == -1 || this.pickSlot == -1) {
            this.disable();
         } else if (this.bedSlot != -1) {
            if (mc.world.isAirBlock(this.placePos.north())
               && mc.world.isAirBlock(this.placePos.west())
               && mc.world.isAirBlock(this.placePos.east())
               && mc.world.isAirBlock(this.placePos.south())) {
               this.helpBlock(this.placePos);
               this.rotation = null;
            } else if (!AntiRegear.INSTANCE.working && !AntiBurrow.INSTANCE.mining) {
               BlockPos instantPos = null;
               if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                  instantPos = PacketMine.INSTANCE.packetPos;
               }

               if (instantPos != null && !this.isPos2(instantPos, this.placePos)) {
                  if (instantPos.equals(new BlockPos(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ))) {
                     return;
                  }

                  if (instantPos.equals(new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ))) {
                     return;
                  }

                  if (mc.world.getBlockState(instantPos).getBlock() == Blocks.WEB) {
                     return;
                  }

                  this.doBreak();
               }

               this.working = true;
               if (!this.start && mc.world.isAirBlock(this.placePos)) {
                  this.time = System.currentTimeMillis() + (this.instantMine.getValue() ? 0 : this.calcBreakTime());
                  this.start = true;
               }

               if (this.time <= System.currentTimeMillis()) {
                  if (this.start && this.timer.passedMs(this.delay.getValue().intValue())) {
                     if (BlockUtil.isAir(this.placePos)) {
                        this.run(
                           this.blockSlot,
                           false,
                           () -> BurrowUtil.placeBlock(
                              this.placePos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                           )
                        );
                     }

                     BlockPos basePos;
                     if (this.block(this.placePos.east())) {
                        this.rotation = new Vec2f(90.0F, 90.0F);
                        basePos = this.placePos.add(1, 0, 0);
                     } else if (this.block(this.placePos.north())) {
                        this.rotation = new Vec2f(0.0F, 90.0F);
                        basePos = this.placePos.add(0, 0, -1);
                     } else if (this.block(this.placePos.west())) {
                        this.rotation = new Vec2f(-90.0F, 90.0F);
                        basePos = this.placePos.add(-1, 0, 0);
                     } else {
                        if (!this.block(this.placePos.south())) {
                           this.rotation = null;
                           return;
                        }

                        this.rotation = new Vec2f(180.0F, 90.0F);
                        basePos = this.placePos.add(0, 0, 1);
                     }

                     if (PlayerPacketManager.INSTANCE.getServerSideRotation().x != this.rotation.x) {
                        return;
                     }

                     EnumHand hand = this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                     EnumFacing opposite = EnumFacing.DOWN.getOpposite();
                     Vec3d hitVec = new Vec3d(basePos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
                     if (BlockUtil.blackList.contains(mc.world.getBlockState(basePos).getBlock()) && !ColorMain.INSTANCE.sneaking) {
                        mc.player
                           .connection
                           .sendPacket(
                              new CPacketEntityAction(mc.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SNEAKING)
                           );
                     }

                     this.run(this.bedSlot, false, () -> {
                        if (this.packet.getValue()) {
                           mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(basePos, EnumFacing.UP, hand, 0.5F, 1.0F, 0.5F));
                        } else {
                           mc.playerController.processRightClickBlock(mc.player, mc.world, basePos, EnumFacing.UP, hitVec, hand);
                        }

                        if (this.swing.getValue()) {
                           mc.player.swingArm(hand);
                        }
                     });
                     this.run(this.pickSlot, this.pickBypass.getValue(), () -> {
                        this.facing = BlockUtil.getRayTraceFacing(this.placePos, EnumFacing.UP);
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.placePos, this.facing));
                        if (!this.instantMine.getValue()) {
                           mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, this.placePos, this.facing));
                           mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.placePos, this.facing));
                           this.time = System.currentTimeMillis() + this.calcBreakTime();
                        }

                        if (this.swing.getValue()) {
                           mc.player.swingArm(EnumHand.MAIN_HAND);
                        }
                     });
                     EnumFacing side = EnumFacing.UP;
                     Vec3d vec = this.getHitVecOffset(side);
                     mc.player
                        .connection
                        .sendPacket(
                           new CPacketPlayerTryUseItemOnBlock(
                              this.placePos.up(), side, hand, (float)vec.x, (float)vec.y, (float)vec.z
                           )
                        );
                     if (this.swing.getValue()) {
                        mc.player.swingArm(hand);
                     }

                     this.timer.reset();
                  }
               }
            }
         }
      } else {
         this.disable();
      }
   }

   private Vec3d getHitVecOffset(EnumFacing face) {
      Vec3i vec = face.getDirectionVec();
      return new Vec3d(vec.x * 0.5F + 0.5F, vec.y * 0.5F + 0.5F, vec.z * 0.5F + 0.5F);
   }

   private void helpBlock(BlockPos pos) {
      List<BlockPos> blocks = NonNullList.create();

      for (BlockPos side : this.side) {
         blocks.add(pos.add(side));
      }

      if (this.down.getValue()) {
         blocks.add(pos.down());
      }

      BlockPos finalPos = blocks.stream()
         .filter(p -> mc.player.getDistanceSq(p) <= this.maxRange.getValue() * this.maxRange.getValue())
         .filter(this::canPlaceBase)
         .max(Comparator.comparing(p -> mc.player.getDistanceSq(p)))
         .orElse(null);
      this.run(
         this.blockSlot,
         false,
         () -> BurrowUtil.placeBlock(finalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue())
      );
   }

   private boolean canPlaceBase(BlockPos pos) {
      if (ColorMain.INSTANCE.breakList.contains(pos)) {
         return false;
      } else {
         return BurrowUtil.getBedFacing(pos) == null ? false : this.space(pos) && !this.intersectsWithEntity(pos);
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

   private boolean canPlaceBedWithoutBase() {
      return this.space(this.placePos)
         && (
            this.space(this.placePos.east())
               || this.space(this.placePos.north())
               || this.space(this.placePos.west())
               || this.space(this.placePos.south())
         );
   }

   private boolean block(BlockPos pos) {
      return BlockUtil.canReplace(pos) ? false : this.space(pos) && this.solid(pos);
   }

   private boolean solid(BlockPos pos) {
      return !BlockUtil.isBlockUnSolid(pos)
         && !(mc.world.getBlockState(pos).getBlock() instanceof BlockBed)
         && mc.world.getBlockState(pos).isSideSolid(mc.world, pos, EnumFacing.UP)
         && BlockUtil.getBlock(pos).fullBlock;
   }

   private boolean space(BlockPos pos) {
      return mc.world.getBlockState(pos.up()).getBlock() == Blocks.BED
         || mc.world.isAirBlock(pos.up());
   }

   private void getItem() {
      this.blockSlot = this.bedSlot = this.pickSlot = -1;
      this.anyBed = false;
      if (mc.player.getHeldItemOffhand().getItem() instanceof ItemBed) {
         this.bedSlot = 36;
         this.offhand = true;
      }

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBed) {
            this.anyBed = true;
            if (i < 9) {
               this.bedSlot = i;
            }
            break;
         }
      }

      this.blockSlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      this.pickSlot = this.findItem();
   }

   private void doBreak() {
      if (this.placePos != null
         && !mc.world.isAirBlock(this.placePos)
         && mc.world.getBlockState(this.placePos).getBlock() != Blocks.BEDROCK) {
         if (this.swing.getValue()) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
         }

         mc.playerController.onPlayerDamageBlock(this.placePos, BlockUtil.getRayTraceFacing(this.placePos, EnumFacing.UP));
      }
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   private void run(int slot, boolean bypass, Runnable runnable) {
      int oldslot = mc.player.inventory.currentItem;
      if (slot >= 0 && slot != oldslot) {
         if (!bypass && slot <= 8) {
            if (this.packetSwitch.getValue()) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            } else {
               mc.player.inventory.currentItem = slot;
            }

            runnable.run();
            if (this.packetSwitch.getValue()) {
               mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            } else {
               mc.player.inventory.currentItem = oldslot;
            }
         } else {
            if (slot < 9) {
               slot += 36;
            }

            mc.player
               .connection
               .sendPacket(
                  new CPacketClickWindow(
                     0,
                     slot,
                     mc.player.inventory.currentItem,
                     ClickType.SWAP,
                     ItemStack.EMPTY,
                     mc.player.inventoryContainer.getNextTransactionID(mc.player.inventory)
                  )
               );
            runnable.run();
            mc.player
               .connection
               .sendPacket(
                  new CPacketClickWindow(
                     0,
                     slot,
                     mc.player.inventory.currentItem,
                     ClickType.SWAP,
                     ItemStack.EMPTY,
                     mc.player.inventoryContainer.getNextTransactionID(mc.player.inventory)
                  )
               );
         }
      } else {
         runnable.run();
      }
   }

   private int calcBreakTime() {
      return this.getBreakTime() * 70;
   }

   private int getBreakTime() {
      float hardness = 50.0F;
      float breakSpeed = this.getSpeed(Blocks.OBSIDIAN.getBlockState().getBaseState());
      if (breakSpeed < 0.0F) {
         return -1;
      } else {
         float relativeDamage = this.getSpeed(Blocks.OBSIDIAN.getBlockState().getBaseState()) / hardness / 30.0F;
         return (int)Math.ceil(0.7F / relativeDamage);
      }
   }

   private int findItem() {
      int result = mc.player.inventory.currentItem;
      double speed = this.getSpeed(Blocks.OBSIDIAN.getBlockState().getBaseState(), mc.player.getHeldItemMainhand());

      for (int i = 0; i < (this.pickBypass.getValue() ? 36 : 9); i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         double stackSpeed = this.getSpeed(Blocks.OBSIDIAN.getBlockState().getBaseState(), stack);
         if (stackSpeed > speed) {
            speed = stackSpeed;
            result = i;
         }
      }

      return result;
   }

   private double getSpeed(IBlockState state, ItemStack stack) {
      double str = stack.getDestroySpeed(state);
      int effect = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
      return Math.max(str + (str > 1.0 ? effect * effect + 1.0 : 0.0), 0.0);
   }

   private float getSpeed(IBlockState blockState) {
      ItemStack itemStack = mc.player.inventory.getStackInSlot(this.pickSlot);
      float digSpeed = mc.player.inventory.getStackInSlot(this.pickSlot).getDestroySpeed(blockState);
      int efficiencyModifier;
      if (!itemStack.isEmpty() && digSpeed > 1.0 && (efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack)) > 0) {
         digSpeed += (float)(StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
      }

      if (mc.player.isPotionActive(MobEffects.HASTE)) {
         digSpeed *= 1.0F + (mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
         float fatigueScale;
         switch (mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
            case 0:
               fatigueScale = 0.3F;
               break;
            case 1:
               fatigueScale = 0.09F;
               break;
            case 2:
               fatigueScale = 0.0027F;
               break;
            default:
               fatigueScale = 8.1E-4F;
         }

         digSpeed *= fatigueScale;
      }

      if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
         digSpeed /= 5.0F;
      }

      return digSpeed;
   }
}
