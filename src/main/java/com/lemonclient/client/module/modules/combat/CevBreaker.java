package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.misc.Wrapper;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.combat.CrystalUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

@Module.Declaration(name = "CevBreaker", category = Category.Combat)
public class CevBreaker extends Module {
   public static CevBreaker INSTANCE;
   ModeSetting page = this.registerMode("Page", Arrays.asList("General", "Place"), "General");
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 1000, () -> this.page.getValue().equals("General"));
   BooleanSetting helpBlock = this.registerBoolean("Help Block", true, () -> this.page.getValue().equals("General"));
   DoubleSetting maxRange = this.registerDouble("Max Range", 5.0, 0.0, 10.0, () -> this.helpBlock.getValue() && this.page.getValue().equals("General"));
   BooleanSetting down = this.registerBoolean("Down Block", true, () -> this.helpBlock.getValue() && this.page.getValue().equals("General"));
   BooleanSetting packet = this.registerBoolean("Packet Place", true, () -> this.page.getValue().equals("General"));
   BooleanSetting rotate = this.registerBoolean("Rotate", false, () -> this.page.getValue().equals("General"));
   BooleanSetting strictFacing = this.registerBoolean("Strict Facing", false, () -> this.page.getValue().equals("General"));
   BooleanSetting swing = this.registerBoolean("Swing", true, () -> this.page.getValue().equals("General"));
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("General"));
   BooleanSetting bypassSwitch = this.registerBoolean("Bypass Switch", false, () -> this.page.getValue().equals("General"));
   BooleanSetting instantMine = this.registerBoolean("Instant Mine", true, () -> this.page.getValue().equals("General"));
   BooleanSetting pickBypass = this.registerBoolean("Pick Bypass", false, () -> this.page.getValue().equals("General"));
   BooleanSetting strict = this.registerBoolean("Strict", false, () -> this.page.getValue().equals("General"));
   BooleanSetting packetCrystal = this.registerBoolean("Packet Crystal", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting crystalBypass = this.registerBoolean("Crystal Bypass", false, () -> this.page.getValue().equals("Place"));
   IntegerSetting breakDelay = this.registerInteger("Break Delay", 50, 0, 1000, () -> this.page.getValue().equals("Place"));
   ModeSetting breakCrystal = this.registerMode("Break Crystal", Arrays.asList("Vanilla", "Packet"), "Packet", () -> this.page.getValue().equals("Place"));
   BooleanSetting airCheck = this.registerBoolean("Air Check", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting antiWeakness = this.registerBoolean("AntiWeakness", true, () -> this.page.getValue().equals("Place"));
   public boolean working;
   boolean offhand;
   boolean start;
   boolean anyCrystal;
   int blockSlot;
   int crystalSlot;
   int pickSlot;
   long time;
   EnumFacing facing;
   Timing timer = new Timing();
   Timing breakTimer = new Timing();
   BlockPos[] side = new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0)};
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

   public CevBreaker() {
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.working = false;
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
      if (mc.world != null && mc.player != null && this.placePos != null && !mc.player.isDead) {
         if (mc.world.isAirBlock(this.placePos.up()) && mc.world.isAirBlock(this.placePos.up().up())) {
            this.getItem();
            if (!this.anyCrystal || this.blockSlot == -1 || this.pickSlot == -1) {
               this.disable();
            } else if (this.crystalSlot != -1) {
               if (mc.world.isAirBlock(this.placePos.down())
                  && mc.world.isAirBlock(this.placePos.north())
                  && mc.world.isAirBlock(this.placePos.west())
                  && mc.world.isAirBlock(this.placePos.east())
                  && mc.world.isAirBlock(this.placePos.south())) {
                  this.helpBlock(this.placePos);
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

                  Entity crystal = this.getCrystal();
                  if (mc.world.getBlockState(this.placePos).getBlock() instanceof BlockAir) {
                     this.breakCrystalPiston(crystal);
                     this.breakTimer.reset();
                  }

                  if (this.time <= System.currentTimeMillis()) {
                     if (this.start && this.timer.passedMs(this.delay.getValue().intValue())) {
                        this.run(
                           this.blockSlot,
                           this.bypassSwitch.getValue(),
                           false,
                           () -> BurrowUtil.placeBlock(
                              this.placePos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                           )
                        );
                        this.run(
                           this.crystalSlot,
                           this.crystalBypass.getValue(),
                           true,
                           () -> this.placeCrystal(this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND)
                        );
                        this.run(this.pickSlot, this.pickBypass.getValue(), false, () -> {
                           this.facing = EnumFacing.UP;
                           if (this.strictFacing.getValue()) {
                              this.facing = BlockUtil.getRayTraceFacing(this.placePos, this.facing);
                           }

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
                        if (!this.airCheck.getValue() || BlockUtil.isAir(this.placePos)) {
                           this.breakCrystalPiston(this.getCrystal());
                        }

                        this.timer.reset();
                     }
                  }
               }
            }
         } else {
            this.disable();
         }
      } else {
         this.disable();
      }
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
         .max(Comparator.comparing(p -> mc.player.getDistanceSq(p)))
         .orElse(null);
      this.run(
         this.blockSlot,
         this.bypassSwitch.getValue(),
         false,
         () -> BurrowUtil.placeBlock(finalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue())
      );
   }

   private void getItem() {
      this.blockSlot = this.crystalSlot = this.pickSlot = -1;
      this.anyCrystal = false;
      if (mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal) {
         this.crystalSlot = 11;
         this.offhand = true;
      }

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemEndCrystal) {
            this.anyCrystal = true;
            if (this.crystalBypass.getValue() || i < 9) {
               this.crystalSlot = i;
            }
            break;
         }
      }

      this.blockSlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      this.pickSlot = this.findItem();
   }

   private void breakCrystalPiston(Entity crystal) {
      if (crystal != null) {
         if (this.breakTimer.passedMs(this.breakDelay.getValue().intValue())) {
            this.breakTimer.reset();
            int newSlot = -1;
            if (this.antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
               for (int i = 0; i < 9; i++) {
                  ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
                  if (stack != ItemStack.EMPTY) {
                     if (stack.getItem() instanceof ItemSword) {
                        newSlot = i;
                        break;
                     }

                     if (stack.getItem() instanceof ItemTool) {
                        newSlot = i;
                     }
                  }
               }
            }

            this.run(newSlot, this.pickBypass.getValue(), false, () -> {
               if (this.breakCrystal.getValue().equalsIgnoreCase("Vanilla")) {
                  CrystalUtil.breakCrystal(crystal, this.swing.getValue());
               } else if (this.breakCrystal.getValue().equalsIgnoreCase("Packet")) {
                  CrystalUtil.breakCrystalPacket(crystal, this.swing.getValue());
               }
            });
         }
      }
   }

   private Entity getCrystal() {
      for (Entity t : mc.world.loadedEntityList) {
         if (t instanceof EntityEnderCrystal
            && t.getDistance(this.placePos.x + 0.5, this.placePos.y + 1.5, this.placePos.z + 0.5) < 3.0) {
            return t;
         }
      }

      return null;
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

   private void placeCrystal(EnumHand hand) {
      if (this.packetCrystal.getValue()) {
         mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.placePos, EnumFacing.UP, hand, 0.0F, 0.0F, 0.0F));
      } else {
         mc.playerController
            .processRightClickBlock(
               mc.player,
               mc.world,
               this.placePos,
               EnumFacing.UP,
               new Vec3d(this.placePos).add(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.UP.getDirectionVec())),
               hand
            );
      }

      if (this.swing.getValue()) {
         mc.player.swingArm(hand);
      }
   }

   private void run(int slot, boolean bypass, boolean update, Runnable runnable) {
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
            ItemStack itemStack = mc.player.inventory.getStackInSlot(slot);
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
                     update ? itemStack : ItemStack.EMPTY,
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
