package com.lemonclient.client.module.modules.dev;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.CrystalUtil;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lemonclient.client.module.modules.qwq.AutoEz;
import com.lemonclient.mixin.mixins.accessor.AccessorCPacketVehicleMove;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "PistonAura", category = Category.Dev)
public class PistonAura extends Module {
   public static PistonAura INSTANCE;
   public boolean autoCrystal;
   ModeSetting page = this.registerMode("Page", Arrays.asList("Calc", "General", "Render"), "Calc");
   IntegerSetting maxTarget = this.registerInteger("Max Target", 1, 1, 10, () -> this.page.getValue().equals("Calc"));
   DoubleSetting range = this.registerDouble("Range", 6.0, 0.0, 10.0, () -> this.page.getValue().equals("Calc"));
   IntegerSetting maxY = this.registerInteger("MaxY", 3, 1, 5, () -> this.page.getValue().equals("Calc"));
   IntegerSetting delay = this.registerInteger("Delay", 20, 0, 100, () -> this.page.getValue().equals("Calc"));
   IntegerSetting baseDelay = this.registerInteger("Base Delay", 0, 0, 100, () -> this.page.getValue().equals("Calc"));
   IntegerSetting startBreakDelay = this.registerInteger("Start Break Delay", 0, 0, 100, () -> this.page.getValue().equals("Calc"));
   IntegerSetting breakDelay = this.registerInteger("Break Delay", 0, 0, 100, () -> this.page.getValue().equals("Calc"));
   BooleanSetting floor = this.registerBoolean("Floor", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting alwaysCalc = this.registerBoolean("Loop Calc", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting pistonCheck = this.registerBoolean("Piston Check", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting entityCheck = this.registerBoolean("Crystal Check", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting base = this.registerBoolean("Base", true, () -> this.page.getValue().equals("Calc"));
   BooleanSetting push = this.registerBoolean("Push To Block", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting crystal = this.registerBoolean("Crystal Detect", false, () -> this.page.getValue().equals("Calc"));
   BooleanSetting fire = this.registerBoolean("Fire", true, () -> this.page.getValue().equals("Calc"));
   BooleanSetting different = this.registerBoolean("Different Pos", false, () -> this.page.getValue().equals("Calc"));
   IntegerSetting maxPos = this.registerInteger("Max Pos", 10, 1, 25, () -> this.different.getValue() && this.page.getValue().equals("Calc"));
   ModeSetting redstone = this.registerMode("Redstone", Arrays.asList("Block", "Torch", "Both"), "Block", () -> this.page.getValue().equals("General"));
   BooleanSetting packetPlace = this.registerBoolean("Packet Place", true, () -> this.page.getValue().equals("General"));
   BooleanSetting packet = this.registerBoolean("Packet Crystal", true, () -> this.page.getValue().equals("General"));
   BooleanSetting packetBreak = this.registerBoolean("Packet Break", true, () -> this.page.getValue().equals("General"));
   BooleanSetting antiWeakness = this.registerBoolean("Anti Weakness", false, () -> this.page.getValue().equals("General"));
   BooleanSetting swingArm = this.registerBoolean("Swing Arm", true, () -> this.page.getValue().equals("General"));
   BooleanSetting silentSwitch = this.registerBoolean("Switch Back", true, () -> this.page.getValue().equals("General"));
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("General"));
   BooleanSetting crystalBypass = this.registerBoolean("Crystal Bypass", true, () -> this.page.getValue().equals("General"));
   BooleanSetting force = this.registerBoolean("Force Bypass", false, () -> this.crystalBypass.getValue() && this.page.getValue().equals("General"));
   BooleanSetting strict = this.registerBoolean("Strict", true, () -> this.page.getValue().equals("General"));
   BooleanSetting forceRotate = this.registerBoolean("Piston ForceRotate", false, () -> this.page.getValue().equals("General"));
   BooleanSetting rotate = this.registerBoolean("Rotate", true, () -> this.page.getValue().equals("General"));
   BooleanSetting pistonRotate = this.registerBoolean("Piston Rotate", true, () -> this.rotate.getValue() && this.page.getValue().equals("General"));
   BooleanSetting raytrace = this.registerBoolean("RayTrace", true, () -> this.page.getValue().equals("General"));
   BooleanSetting baseRaytrace = this.registerBoolean(
      "Base RayTrace", true, () -> this.base.getValue() && this.raytrace.getValue() && this.page.getValue().equals("General")
   );
   DoubleSetting forceRange = this.registerDouble("Force Range", 3.0, 0.0, 6.0, () -> this.raytrace.getValue() && this.page.getValue().equals("General"));
   BooleanSetting pauseEat = this.registerBoolean("Pause When Eating", true, () -> this.page.getValue().equals("General"));
   BooleanSetting pause1 = this.registerBoolean("Pause When Burrow", true, () -> this.page.getValue().equals("General"));
   DoubleSetting maxSelfSpeed = this.registerDouble("Max Self Speed", 10.0, 0.0, 50.0, () -> this.page.getValue().equals("General"));
   DoubleSetting maxTargetSpeed = this.registerDouble("Max Target Speed", 10.0, 0.0, 50.0, () -> this.page.getValue().equals("General"));
   BooleanSetting render = this.registerBoolean("Render", false, () -> this.page.getValue().equals("Render"));
   BooleanSetting fireRender = this.registerBoolean("Fire Render", false, () -> this.render.getValue() && this.page.getValue().equals("Render"));
   BooleanSetting box = this.registerBoolean("Box", false, () -> this.render.getValue() && this.page.getValue().equals("Render"));
   BooleanSetting outline = this.registerBoolean("Outline", false, () -> this.render.getValue() && this.page.getValue().equals("Render"));
   BooleanSetting hud = this.registerBoolean("HUD", false, () -> this.page.getValue().equals("Render"));
   public static EntityPlayer target = null;
   public BlockPos targetPos;
   public BlockPos pistonPos;
   public BlockPos crystalPos;
   public BlockPos redStonePos;
   public BlockPos firePos;
   public BlockPos lastTargetPos;
   public int pistonSlot;
   public int crystalSlot;
   public int redStoneSlot;
   public int obbySlot;
   public int flintSlot;
   public Timing timer = new Timing();
   public Timing baseTimer = new Timing();
   public Timing startBreakTimer = new Timing();
   public Timing breakTimer = new Timing();
   public boolean preparedSpace;
   public boolean placedPiston;
   public boolean placedCrystal;
   public boolean placedFire;
   public boolean placedRedstone;
   public boolean brokeCrystal;
   int oldSlot;
   boolean useBlock;
   boolean boom;
   boolean burrowed;
   boolean moving;
   boolean first;
   Vec2f rotation;
   BlockPos[] saveArray = new BlockPos[25];
   Vec3d[] sides = new Vec3d[]{new Vec3d(0.24, 0.0, 0.24), new Vec3d(-0.24, 0.0, 0.24), new Vec3d(0.24, 0.0, -0.24), new Vec3d(-0.24, 0.0, -0.24)};
   BlockPos[] offsets = new BlockPos[]{new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
   BlockPos[] fireList = new BlockPos[]{
      new BlockPos(0, 1, 0),
      new BlockPos(1, 1, 1),
      new BlockPos(1, 1, 0),
      new BlockPos(1, 1, -1),
      new BlockPos(0, 1, 1),
      new BlockPos(0, 1, -1),
      new BlockPos(-1, 1, 1),
      new BlockPos(-1, 1, 0),
      new BlockPos(-1, 1, -1),
      new BlockPos(1, 0, 1),
      new BlockPos(1, 0, 0),
      new BlockPos(1, 0, -1),
      new BlockPos(0, 0, 1),
      new BlockPos(0, 0, -1),
      new BlockPos(-1, 0, 1),
      new BlockPos(-1, 0, 0),
      new BlockPos(-1, 0, -1),
      new BlockPos(0, 0, 0)
   };
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.rotation != null) {
         if (event.getPacket() instanceof Rotation) {
            ((Rotation)event.getPacket()).yaw = this.rotation.x;
            ((Rotation)event.getPacket()).pitch = 0.0F;
         }

         if (event.getPacket() instanceof PositionRotation) {
            ((PositionRotation)event.getPacket()).yaw = this.rotation.x;
            ((PositionRotation)event.getPacket()).pitch = 0.0F;
         }

         if (event.getPacket() instanceof CPacketVehicleMove) {
            ((AccessorCPacketVehicleMove)event.getPacket()).setYaw(this.rotation.x);
         }
      }
   });
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
      if (this.rotation != null && event.getPhase() == Phase.PRE) {
         PlayerPacket packet = new PlayerPacket(this, new Vec2f(this.rotation.x, 0.0F));
         PlayerPacketManager.INSTANCE.addPacket(packet);
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && this.crystalPos != null) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
               SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();
               if (packet.getCategory() == SoundCategory.BLOCKS
                  && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE
                  && this.crystalPos.distanceSq(packet.getX(), packet.getY(), packet.getZ()) <= 9.0) {
                  this.boom = true;
               }
            }
         }
      }
   );
   @EventHandler
   private final Listener<PacketEvent.Receive> listener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
               SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();
               if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                  for (Entity crystal : new ArrayList<Entity>(mc.world.loadedEntityList)) {
                     if (crystal instanceof EntityEnderCrystal
                        && crystal.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= this.range.getValue() + 5.0) {
                        crystal.setDead();
                     }
                  }
               }
            }
         }
      }
   );

   public PistonAura() {
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.saveArray = new BlockPos[25];
      this.first = true;
      this.reset();
   }

   @Override
   public void onTick() {
      if (this.autoCrystal) {
         this.reset();
      } else {
         this.doPA();
      }
   }

   public void doPA() {
      this.moving = false;
      this.burrowed = false;
      BlockPos originalPos = PlayerUtil.getPlayerPos();
      Block block = BlockUtil.getBlock(originalPos);
      if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST) {
         this.burrowed = true;
      }

      if (!this.pause1.getValue() || !this.burrowed) {
         if (!this.pauseEat.getValue() || !EntityUtil.isEating()) {
            if (!(LemonClient.speedUtil.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue())) {
               this._doPA();
            }
         }
      }
   }

   public void _doPA() {
      if (!this.forceRotate.getValue()) {
         this.rotation = null;
      }

      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         try {
            if (!this.findMaterials()) {
               this.reset();
               return;
            }

            if (this.alwaysCalc.getValue() || this.boom || target == null || !EntityUtil.isAlive(target)) {
               PistonAura.PistonAuraPos pos = this.findSpace();
               if (pos == null) {
                  this.first = true;
                  target = null;
                  this.targetPos = this.pistonPos = this.redStonePos = this.firePos = this.crystalPos = null;
                  this.rotation = null;
                  return;
               }

               target = pos.target;
               this.targetPos = pos.targetPos;
               this.pistonPos = pos.piston;
               this.redStonePos = pos.redstone;
               this.crystalPos = pos.crystal;
               this.firePos = pos.fire;
            }

            if (this.targetPos == null || this.pistonPos == null || this.redStonePos == null || this.crystalPos == null) {
               if (this.breakTimer.passedDms(this.breakDelay.getValue().intValue()) && this.lastTargetPos != null) {
                  if (this.packetBreak.getValue()) {
                     CrystalUtil.breakCrystalPacket(this.lastTargetPos, this.swingArm.getValue());
                  } else {
                     CrystalUtil.breakCrystal(this.lastTargetPos, this.swingArm.getValue());
                  }

                  this.breakTimer.reset();
               }

               this.reset();
               return;
            }

            if (PlayerUtil.getDistanceI(this.pistonPos) > this.range.getValue()
               || PlayerUtil.getDistanceI(this.redStonePos) > this.range.getValue()
               || PlayerUtil.getDistanceI(this.crystalPos) > this.range.getValue()) {
               this.lastTargetPos = null;
               this.reset();
               return;
            }

            AutoEz.INSTANCE.addTargetedPlayer(target.getName());
            this.lastTargetPos = new BlockPos(this.targetPos.getX(), this.crystalPos.getY() + 2, this.targetPos.getZ());
            this.oldSlot = mc.player.inventory.currentItem;
            BlockPos offset = new BlockPos(
               this.crystalPos.getX() - this.targetPos.getX(), 0, this.crystalPos.getZ() - this.targetPos.getZ()
            );
            BlockPos headPos = this.pistonPos.add(offset.getX() * -1, 0, offset.getZ() * -1);
            Block block = BlockUtil.getBlock(headPos);
            if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || this.checkPos(headPos)) {
               this.reset();
               return;
            }

            this.placedPiston = BlockUtil.getBlock(this.pistonPos) instanceof BlockPistonBase;
            this.placedRedstone = this.hasRedstone(this.pistonPos);
            this.placedCrystal = this.getCrystal(this.crystalPos.up()) != null
               && this.getCrystal(new BlockPos(this.targetPos.getX(), this.crystalPos.getY() + 2, this.targetPos.getZ())) != null;
            this.placedFire = BlockUtil.getBlock(this.firePos) instanceof BlockFire;
            if (this.flintSlot == -1 || BlockUtil.isBlockUnSolid(this.firePos.down()) || BlockUtil.canOpen(this.firePos.down())) {
               this.placedFire = true;
            }

            if (!this.placedCrystal && (!BlockUtil.isAirBlock(this.crystalPos.up()) || !BlockUtil.isAirBlock(this.crystalPos.up(2)))) {
               this.reset();
               return;
            }

            if (!this.placedPiston && (this.intersectsWithEntity(this.pistonPos) || !BlockUtil.canReplace(this.pistonPos))) {
               this.reset();
               return;
            }

            if (!this.placedRedstone && (this.intersectsWithEntity(this.redStonePos) || !BlockUtil.canReplace(this.redStonePos))) {
               this.reset();
               return;
            }

            float[] angle = MathUtil.calcAngle(new Vec3d(this.crystalPos), new Vec3d(this.targetPos));
            this.rotation = new Vec2f(angle[0] + 180.0F, angle[1]);
            if (!this.preparedSpace) {
               this.preparedSpace = this.canPlace(this.pistonPos) || this.canPlace(this.redStonePos);
               if (!this.preparedSpace) {
                  if (!this.base.getValue()) {
                     this.preparedSpace = true;
                  } else if (this.baseTimer.passedDms(this.baseDelay.getValue().intValue())) {
                     this.baseTimer.reset();
                     this.preparedSpace = this.prepareSpace();
                  }
               }

               this.timer.reset();
            }

            if (this.preparedSpace && this.first) {
               if (!this.forceRotate.getValue()) {
                  this.timer.setMs(1000000000L);
               }

               this.first = false;
            }

            boolean finish = this.placedPiston && this.placedCrystal && this.placedRedstone;
            if (this.timer.passedDms(this.delay.getValue().intValue()) && !finish) {
               this.timer.reset();
               if (!this.placedPiston && !this.canPlace(this.pistonPos) && this.canPlace(this.redStonePos)) {
                  this.placeRedstone(this.preparedSpace && !this.placedRedstone);
               }

               this.placePiston(this.preparedSpace && !this.placedPiston);
               this.placeCrystal(this.placedPiston && !this.placedCrystal);
               if (this.placedCrystal && !this.placedFire) {
                  this.setItem(this.flintSlot);
                  if (this.placeBlock(this.firePos, this.packetPlace.getValue())) {
                     this.placedFire = true;
                  }
               }

               this.placeRedstone(this.placedFire && !this.placedRedstone);
               finish = this.placedPiston && this.placedCrystal && this.placedFire && this.placedRedstone;
            }

            if (finish) {
               Entity crystal = mc.world
                  .getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.crystalPos.up(2)))
                  .stream()
                  .filter(e -> e instanceof EntityEnderCrystal)
                  .min(Comparator.comparing(e -> this.getDistance(target, e)))
                  .orElse(null);
               if (this.startBreakTimer.passedDms(this.startBreakDelay.getValue().intValue())
                  && this.breakTimer.passedDms(this.breakDelay.getValue().intValue())
                  && crystal != null) {
                  this.breakTimer.reset();
                  if (this.antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                     int newSlot = -1;

                     for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
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

                     if (newSlot != -1) {
                        this.setItem(newSlot);
                     }
                  }

                  if (this.packetBreak.getValue()) {
                     mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                  } else {
                     mc.playerController.attackEntity(mc.player, crystal);
                  }

                  if (this.swingArm.getValue()) {
                     mc.player.swingArm(EnumHand.MAIN_HAND);
                  }
               }
            }

            this.restoreItem();
         } catch (Exception var10) {
         }
      } else {
         this.reset();
      }
   }

   private void placePiston(boolean work) {
      if (work) {
         this.setItem(this.pistonSlot);
         mc.player.connection.sendPacket(new Rotation(this.rotation.x, this.rotation.y, true));
         this.placedPiston = this.placeBlock(this.pistonPos, this.packetPlace.getValue());
         this.startBreakTimer.reset();
         this.breakTimer.reset();
      }
   }

   private void placeCrystal(boolean work) {
      if (work) {
         EnumHand hand = this.crystalSlot != 999 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
         if (this.crystalBypass.getValue() && (this.crystalSlot >= 9 || this.force.getValue()) && hand == EnumHand.MAIN_HAND) {
            int slot = this.crystalSlot;
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
            this.placedCrystal = CrystalUtil.placeCrystal(this.crystalPos, hand, this.packet.getValue(), this.rotate.getValue(), this.swingArm.getValue());
            mc.player
               .connection
               .sendPacket(
                  new CPacketClickWindow(
                     0,
                     slot,
                     mc.player.inventory.currentItem,
                     ClickType.SWAP,
                     Items.END_CRYSTAL.getDefaultInstance(),
                     mc.player.inventoryContainer.getNextTransactionID(mc.player.inventory)
                  )
               );
         } else {
            this.setItem(this.crystalSlot);
            this.placedCrystal = CrystalUtil.placeCrystal(this.crystalPos, hand, this.packet.getValue(), this.rotate.getValue(), this.swingArm.getValue());
         }

         this.startBreakTimer.reset();
         this.breakTimer.reset();
      }
   }

   private void placeRedstone(boolean work) {
      if (work) {
         this.setItem(this.redStoneSlot);
         this.placedRedstone = BlockUtil.placeBlockBoolean(
            this.redStonePos,
            EnumHand.MAIN_HAND,
            this.rotate.getValue(),
            this.packetPlace.getValue(),
            this.strict.getValue(),
            this.needRaytrace(this.redStonePos),
            this.swingArm.getValue()
         );
         this.startBreakTimer.reset();
         this.breakTimer.reset();
      }
   }

   public boolean prepareSpace() {
      BlockPos piston = this.pistonPos.add(0, -1, 0);
      if (this.isPos2(piston, this.redStonePos)) {
         piston = piston.down();
      }

      BlockPos redstone = this.redStonePos.add(0, -1, 0);
      if (!this.canPlace(this.pistonPos)) {
         if (this.intersectsWithEntity(this.pistonPos)) {
            this.reset();
         } else {
            BlockPos offset = new BlockPos(
               this.crystalPos.getX() - this.targetPos.getX(), 0, this.crystalPos.getZ() - this.targetPos.getZ()
            );
            BlockPos crystalOffset = this.crystalPos.add(offset);
            BlockPos crystalOffset1 = crystalOffset.add(offset);
            this.setItem(this.obbySlot);
            if (this.canPlace(piston) && BlockUtil.canReplace(piston) && !this.isPos2(piston, this.redStonePos)) {
               if (this.intersectsWithEntity(piston)) {
                  this.reset();
               } else {
                  BlockUtil.placeBlock(
                     piston,
                     EnumHand.MAIN_HAND,
                     this.rotate.getValue(),
                     this.packetPlace.getValue(),
                     this.strict.getValue(),
                     this.raytrace.getValue() && this.baseRaytrace.getValue(),
                     this.swingArm.getValue()
                  );
               }
            } else if (this.canPlace(crystalOffset1) && BlockUtil.canReplace(crystalOffset1) && !this.isPos2(crystalOffset1, this.redStonePos)) {
               if (this.intersectsWithEntity(crystalOffset1)) {
                  this.reset();
               } else {
                  BlockUtil.placeBlock(
                     crystalOffset1,
                     EnumHand.MAIN_HAND,
                     this.rotate.getValue(),
                     this.packetPlace.getValue(),
                     this.strict.getValue(),
                     this.raytrace.getValue() && this.baseRaytrace.getValue(),
                     this.swingArm.getValue()
                  );
               }
            } else if (!this.canPlace(crystalOffset) || !BlockUtil.canReplace(crystalOffset) || this.isPos2(crystalOffset, this.redStonePos)) {
               this.reset();
            } else if (this.intersectsWithEntity(crystalOffset)) {
               this.reset();
            } else {
               BlockUtil.placeBlock(
                  crystalOffset,
                  EnumHand.MAIN_HAND,
                  this.rotate.getValue(),
                  this.packetPlace.getValue(),
                  this.strict.getValue(),
                  this.raytrace.getValue() && this.baseRaytrace.getValue(),
                  this.swingArm.getValue()
               );
            }
         }

         return false;
      } else if ((!this.canPlace(this.redStonePos) || !this.useBlock && this.redStonePos.getY() == this.pistonPos.getY())
         && this.canPlace(redstone)
         && !this.isPos2(redstone, this.pistonPos)) {
         if (this.intersectsWithEntity(redstone)) {
            this.reset();
         } else {
            this.setItem(this.obbySlot);
            BlockUtil.placeBlock(
               redstone,
               EnumHand.MAIN_HAND,
               this.rotate.getValue(),
               this.packetPlace.getValue(),
               this.strict.getValue(),
               this.raytrace.getValue() && this.baseRaytrace.getValue(),
               this.swingArm.getValue()
            );
         }

         return false;
      } else {
         return true;
      }
   }

   public PistonAura.PistonAuraPos findSpace() {
      List<PistonAura.PistonAuraPos> list = new ArrayList<>();

      for (EntityPlayer target : PlayerUtil.getNearPlayers(this.range.getValue() + 4.0, this.maxTarget.getValue())) {
         if (!(LemonClient.speedUtil.getPlayerSpeed(target) > this.maxTargetSpeed.getValue()) && (int)(target.posY + 0.5) < 255) {
            List<PistonAura.PistonAuraPos> sideList = new ArrayList<>();

            for (Vec3d vec3d : this.sides) {
               BlockPos targetPos = new BlockPos(
                  target.posX + vec3d.x, target.posY + 0.5, target.posZ + vec3d.z
               );
               BlockPos cPos = null;

               for (Entity entity : mc.world.loadedEntityList) {
                  if (entity instanceof EntityEnderCrystal) {
                     cPos = new BlockPos(entity.posX, entity.posY - 1.0, entity.posZ);
                     int x = Math.abs(cPos.getX() - targetPos.getX());
                     int y = cPos.y - targetPos.y;
                     int z = Math.abs(cPos.getZ() - targetPos.getZ());
                     if (x <= 1 && y <= 5 && y >= 0 && z <= 1) {
                        break;
                     }

                     cPos = null;
                  }
               }

               BlockPos[] offsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
               boolean calc = false;
               List<PistonAura.PistonAuraPos> posList = new ArrayList<>();

               for (int y = this.floor.getValue() ? -1 : 0; y <= this.maxY.getValue(); y++) {
                  boolean cantPlace = false;
                  boolean block = false;

                  for (int high = y + 1; high >= 0; high--) {
                     BlockPos pos = targetPos.up(high);
                     if (isResistant(BlockUtil.getState(pos))) {
                        if (high < y + 1) {
                           cantPlace = true;
                        } else if (!this.push.getValue()) {
                           cantPlace = true;
                        } else {
                           block = true;
                        }
                     }
                  }

                  if (!cantPlace) {
                     for (BlockPos side : offsets) {
                        if (!this.crystal.getValue()) {
                           cPos = null;
                        }

                        BlockPos offset = cPos == null
                           ? side
                           : new BlockPos(cPos.getX() - targetPos.getX(), 0, cPos.getZ() - targetPos.getZ());
                        if (cPos != null && this.isPos2(new BlockPos(-offset.getX(), 0, -offset.getZ()), side)) {
                           cPos = null;
                        }

                        if (cPos == null) {
                           offset = side;
                        } else if (calc) {
                           continue;
                        }

                        BlockPos crystalPos = cPos == null ? targetPos.add(offset.getX(), y, offset.getZ()) : cPos;
                        if (crystalPos.y < 255
                           && (
                              cPos != null
                                 || (BlockUtil.getBlock(crystalPos) == Blocks.OBSIDIAN || BlockUtil.getBlock(crystalPos) == Blocks.BEDROCK)
                                    && mc.world.isAirBlock(crystalPos.up())
                                    && mc.world.isAirBlock(crystalPos.up(2))
                                    && !(PlayerUtil.getDistanceI(crystalPos) > this.range.getValue())
                                    && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(crystalPos.up())).isEmpty()
                                    && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(crystalPos.up(2))).isEmpty()
                           )) {
                           BlockPos normal = crystalPos.add(offset);
                           BlockPos oneBlock = normal.add(offset);
                           BlockPos side0 = normal.add(offset.getZ(), 0, offset.getX());
                           BlockPos side1 = normal.add(offset.getZ() * -1, 0, offset.getX() * -1);
                           BlockPos side2 = oneBlock.add(offset.getZ(), 0, offset.getX());
                           BlockPos side3 = oneBlock.add(offset.getZ() * -1, 0, offset.getX() * -1);
                           BlockPos side4 = crystalPos.add(offset.getZ(), 0, offset.getX());
                           BlockPos side5 = crystalPos.add(offset.getZ() * -1, 0, offset.getX() * -1);
                           List<BlockPos> pistons = new ArrayList<>();
                           this.add(pistons, normal);
                           this.add(pistons, oneBlock);
                           this.add(pistons, side0);
                           this.add(pistons, side1);
                           this.add(pistons, side2);
                           this.add(pistons, side3);
                           this.add(pistons, side4);
                           this.add(pistons, side5);
                           BlockPos finalOffset = offset;
                           pistons.removeIf(p -> {
                              if (this.flintSlot != -1 && !this.getFirePos(targetPos, crystalPos, finalOffset)) {
                                 return true;
                              } else if (!this.different.getValue()) {
                                 return false;
                              } else {
                                 boolean same = false;

                                 for (BlockPos savePos : this.saveArray) {
                                    if (this.isPos2(savePos, p)) {
                                       same = true;
                                       break;
                                    }
                                 }

                                 return same;
                              }
                           });
                           if (!pistons.isEmpty()) {
                              List<BlockPos> pistonList = pistons.stream()
                                 .filter(
                                    pistonPos -> {
                                       BlockPos headPos = pistonPos.add(finalOffset.getX() * -1, 0, finalOffset.getZ() * -1);
                                       Block headBlock = BlockUtil.getBlock(headPos);
                                       if (headBlock != Blocks.BEDROCK
                                          && headBlock != Blocks.OBSIDIAN
                                          && headBlock != Blocks.ENDER_CHEST
                                          && headBlock != Blocks.PISTON_HEAD
                                          && !this.checkPos(headPos)) {
                                          boolean isPiston = BlockUtil.getBlock(pistonPos) instanceof BlockPistonBase;
                                          if (!isPiston) {
                                             if (!this.canPlace(pistonPos)) {
                                                return false;
                                             }

                                             if (mc.player
                                                   .getDistance(
                                                      pistonPos.getX() + 0.5, pistonPos.getY() + 0.5, pistonPos.getZ() + 0.5
                                                   )
                                                > this.range.getValue()) {
                                                return false;
                                             }

                                             double feetY = mc.player.posY;
                                             if (PlayerUtil.getDistanceI(pistonPos) < 0.8 + pistonPos.getY() - feetY
                                                   && pistonPos.getY() > feetY + 1.0
                                                || PlayerUtil.getDistanceI(pistonPos) < 1.8 + feetY - pistonPos.getY()
                                                   && pistonPos.getY() < feetY) {
                                                return false;
                                             }
                                          } else if (this.pistonCheck.getValue() && (this.hasRedstone(pistonPos) || !this.isFacing(pistonPos, headPos))) {
                                             return false;
                                          }

                                          BlockPos redstonePos = this.getRedStonePos(crystalPos, pistonPos, finalOffset);
                                          if (redstonePos == null) {
                                             return false;
                                          } else {
                                             return this.flintSlot != -1 && this.getFirePos(targetPos, crystalPos, pistonPos, redstonePos, finalOffset) == null
                                                ? false
                                                : isPiston
                                                   || BlockUtil.canPlaceWithoutBase(
                                                      pistonPos,
                                                      this.strict.getValue(),
                                                      this.needRaytrace(pistonPos),
                                                      this.canPlace(redstonePos)
                                                         || this.obbySlot != -1 && (this.base.getValue() || this.canPlace(redstonePos.down()))
                                                   );
                                          }
                                       } else {
                                          return false;
                                       }
                                    }
                                 )
                                 .collect(Collectors.toList());
                              if (pistonList.isEmpty()) {
                                 pistonList.addAll(pistons);
                              }

                              BlockPos piston = pistonList.stream().min(Comparator.comparing(this::blockLevel)).orElse(null);
                              PistonAura.PistonAuraPos pos = new PistonAura.PistonAuraPos(
                                 crystalPos, piston, this.getRedStonePos(crystalPos, piston, offset), offset, target, targetPos, block
                              );
                              posList.add(pos);
                              if (cPos != null) {
                                 calc = true;
                              }
                           }
                        }
                     }
                  }
               }

               List<PistonAura.PistonAuraPos> paList = posList.stream().filter(p -> !p.block || p.offset.z == 1).collect(Collectors.toList());
               if (paList.isEmpty()) {
                  paList.addAll(posList);
               }

               PistonAura.PistonAuraPos best = paList.stream().min(Comparator.comparing(PistonAura.PistonAuraPos::range)).orElse(null);
               if (best != null) {
                  sideList.add(best);
               }
            }

            if (!sideList.isEmpty()) {
               list.add(sideList.stream().min(Comparator.comparing(PistonAura.PistonAuraPos::range)).orElse(null));
            }
         }
      }

      PistonAura.PistonAuraPos best = list.stream().min(Comparator.comparing(PistonAura.PistonAuraPos::range)).orElse(null);
      if (best == null) {
         this.saveArray = new BlockPos[25];
         return null;
      } else {
         return best;
      }
   }

   public boolean isFacing(BlockPos pos, BlockPos facingPos) {
      ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
      UnmodifiableIterator var4 = properties.keySet().iterator();

      while (var4.hasNext()) {
         IProperty<?> prop = (IProperty<?>)var4.next();
         if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {
            BlockPos pushPos = pos.offset((EnumFacing)properties.get(prop));
            return this.isPos2(facingPos, pushPos);
         }
      }

      return false;
   }

   public static boolean isResistant(IBlockState blockState) {
      return blockState.getMaterial() != Material.AIR
         && !BlockUtil.isAir(blockState.getBlock())
         && !(blockState instanceof BlockLiquid)
         && blockState.getBlock().blockResistance >= 19.7;
   }

   public boolean getFirePos(BlockPos targetPos, BlockPos crystalPos, BlockPos offset) {
      BlockPos origin = new BlockPos(targetPos.getX(), crystalPos.getY() + 1, targetPos.getZ());
      BlockPos pos1 = targetPos.add(-offset.x, 0, -offset.z);
      BlockPos pos2 = pos1.add(offset.z, 0, offset.x);
      BlockPos pos3 = pos1.add(-offset.z, 0, -offset.x);
      BlockPos pos4 = targetPos.add(-offset.x, 1, -offset.z);
      BlockPos pos5 = pos4.add(offset.z, 0, offset.x);
      BlockPos pos6 = pos4.add(-offset.z, 0, -offset.x);
      BlockPos[] posList = new BlockPos[]{pos1, pos2, pos3, pos4, pos5, pos6};
      boolean air = true;

      for (BlockPos pos : posList) {
         if (!BlockUtil.isAir(pos)) {
            AxisAlignedBB box = BlockUtil.getBoundingBox(pos);
            if (box != null) {
               double x = box.maxX - box.minX;
               double z = box.maxZ - box.minZ;
               if (x * x == offset.x * offset.x || z * z == offset.z * offset.z) {
                  air = false;
                  break;
               }
            }
         }
      }

      for (BlockPos firePos : this.fireList) {
         BlockPos posx = origin.add(firePos);
         if (air
            || targetPos.add(-offset.x, 0, 0).getX() != posx.getX()
               && targetPos.add(0, 0, -offset.z).getZ() != posx.getZ()) {
            if (BlockUtil.getBlock(posx) == Blocks.FIRE) {
               return true;
            }

            if (this.canPlaceFire(posx)
               && mc.world.isAirBlock(posx)
               && !BlockUtil.isBlockUnSolid(posx.down())
               && !BlockUtil.canOpen(posx.down())) {
               return true;
            }
         }
      }

      return false;
   }

   public BlockPos getFirePos(BlockPos targetPos, BlockPos crystalPos, BlockPos pistonPos, BlockPos redstonePos, BlockPos offset) {
      BlockPos origin = new BlockPos(targetPos.getX(), crystalPos.getY() + 1, targetPos.getZ());
      BlockPos pos1 = targetPos.add(-offset.x, 0, -offset.z);
      BlockPos pos2 = pos1.add(offset.z, 0, offset.x);
      BlockPos pos3 = pos1.add(-offset.z, 0, -offset.x);
      BlockPos pos4 = targetPos.add(-offset.x, 1, -offset.z);
      BlockPos pos5 = pos4.add(offset.z, 0, offset.x);
      BlockPos pos6 = pos4.add(-offset.z, 0, -offset.x);
      BlockPos[] posList = new BlockPos[]{pos1, pos2, pos3, pos4, pos5, pos6};
      boolean air = true;

      for (BlockPos pos : posList) {
         if (!BlockUtil.isAir(pos)) {
            AxisAlignedBB box = BlockUtil.getBoundingBox(pos);
            if (box != null) {
               double x = box.maxX - box.minX;
               double z = box.maxZ - box.minZ;
               if (x * x == offset.x * offset.x || z * z == offset.z * offset.z) {
                  air = false;
                  break;
               }
            }
         }
      }

      List<BlockPos> list = new ArrayList<>();

      for (BlockPos firePos : this.fireList) {
         BlockPos posx = origin.add(firePos);
         if (!this.isPos2(pistonPos, posx)
            && !this.isPos2(pistonPos.up(), posx)
            && !this.isPos2(redstonePos, posx)
            && !this.isPos2(pistonPos, posx.add(offset))
            && !this.isPos2(pistonPos.up(), posx.add(offset))
            && (
               air
                  || targetPos.add(-offset.x, 0, 0).getX() != posx.getX()
                     && targetPos.add(0, 0, -offset.z).getZ() != posx.getZ()
            )) {
            if (BlockUtil.getBlock(posx) == Blocks.FIRE) {
               return posx;
            }

            if (this.canPlaceFire(posx)
               && mc.world.isAirBlock(posx)
               && !BlockUtil.isBlockUnSolid(posx.down())
               && !BlockUtil.canOpen(posx.down())) {
               list.add(posx);
            }
         }
      }

      return list.stream().sorted(this::fireLevel).min(Comparator.comparing(PlayerUtil::getDistanceI)).orElse(null);
   }

   public BlockPos getRedStonePos(BlockPos crystalPos, BlockPos pistonPos, BlockPos offset) {
      BlockPos pos = this.hasRedstoneBlock(pistonPos);
      if (pos != null) {
         return pos;
      } else {
         List<BlockPos> redstone = new ArrayList<>();
         if (this.useBlock) {
            for (EnumFacing facing : EnumFacing.VALUES) {
               BlockPos redstonePos = pistonPos.offset(facing);
               if (BlockUtil.canReplace(redstonePos)
                  && !(
                     mc.player.getDistance(redstonePos.getX() + 0.5, redstonePos.getY() + 0.5, redstonePos.getZ() + 0.5)
                        > this.range.getValue()
                  )) {
                  boolean can = true;

                  for (BlockPos blockPos : this.fireList) {
                     if (this.isPos2(crystalPos.up().add(blockPos), redstonePos)) {
                        can = false;
                        break;
                     }
                  }

                  if (can && BlockUtil.canPlaceWithoutBase(redstonePos, this.strict.getValue(), this.needRaytrace(redstonePos), this.base.getValue())) {
                     redstone.add(redstonePos);
                  }
               }
            }
         } else {
            BlockPos pistonPush = pistonPos.add(offset.getX() * -1, 0, offset.getZ() * -1);

            for (BlockPos add : this.offsets) {
               for (int i = 0; i < 2; i++) {
                  BlockPos torch = pistonPos.down(i).add(add);
                  if (BlockUtil.canReplace(torch)
                     && (torch.getX() != crystalPos.getX() || torch.getZ() != crystalPos.getZ())
                     && (torch.getX() != pistonPush.getX() || torch.getZ() != pistonPush.getZ())
                     && (i != 1 || !BlockUtil.isBlockUnSolid(torch.up()))
                     && !(
                        mc.player.getDistance(torch.getX() + 0.5, torch.getY() + 0.5, torch.getZ() + 0.5)
                           > this.range.getValue()
                     )
                     && (BlockUtil.isAir(torch.down()) || !BlockUtil.isBlockUnSolid(torch.down()))
                     && BlockUtil.canPlaceWithoutBase(torch, this.strict.getValue(), this.needRaytrace(torch), this.base.getValue(), pistonPos)) {
                     redstone.add(torch);
                  }
               }
            }
         }

         redstone.removeIf(p -> p.y > 255);
         return redstone.stream().min(Comparator.comparing(this::blockLevel)).orElse(null);
      }
   }

   public boolean hasRedstone(BlockPos pos) {
      return this.hasRedstoneBlock(pos) != null;
   }

   public BlockPos hasRedstoneBlock(BlockPos pos) {
      if (BlockUtil.getBlock(pos.up()) == Blocks.REDSTONE_BLOCK) {
         return pos.up();
      } else {
         for (BlockPos offset : this.offsets) {
            for (int i = 0; i < 2; i++) {
               BlockPos blockPos = pos.add(offset).down(i);
               Block block = BlockUtil.getBlock(blockPos);
               if (i == 0) {
                  if (block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_BLOCK) {
                     return blockPos;
                  }
               } else if (block == Blocks.REDSTONE_TORCH && !BlockUtil.isBlockUnSolid(blockPos.up())) {
                  return blockPos;
               }
            }
         }

         return null;
      }
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   private double getDistance(EntityPlayer player, Entity entity) {
      double x = player.posX - entity.posX;
      double z = player.posZ - entity.posZ;
      return Math.sqrt(x * x + z * z);
   }

   private boolean canPlace(BlockPos pos) {
      return BlockUtil.getFirstFacing(pos, this.strict.getValue(), this.needRaytrace(pos)) != null && !this.intersectsWithEntity(pos);
   }

   private boolean canPlaceFire(BlockPos pos) {
      return BlockUtil.getFirstFacing(pos, this.strict.getValue(), this.needRaytrace(pos)) != null;
   }

   private Entity getCrystal(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (entity instanceof EntityEnderCrystal && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return entity;
         }
      }

      return null;
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)
            && (this.entityCheck.getValue() || !(entity instanceof EntityEnderCrystal))
            && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   public void add(List<BlockPos> pistons, BlockPos pos) {
      pistons.add(pos.add(0, 1, 0));
      pistons.add(pos.add(0, 2, 0));
   }

   public static int findHotbarBlock(Block blockIn) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   private int getItemHotbar() {
      for (int i = 0; i < (this.crystalBypass.getValue() ? 36 : 9); i++) {
         Item item = mc.player.inventory.getStackInSlot(i).getItem();
         if (Item.getIdFromItem(item) == Item.getIdFromItem(Items.END_CRYSTAL)) {
            return i;
         }
      }

      return -1;
   }

   public boolean findMaterials() {
      this.pistonSlot = findHotbarBlock(Blocks.PISTON);
      this.obbySlot = findHotbarBlock(Blocks.OBSIDIAN);
      this.crystalSlot = this.getItemHotbar();
      this.flintSlot = this.fire.getValue() ? BurrowUtil.findHotbarBlock(Items.FLINT_AND_STEEL.getClass()) : -1;
      if (this.pistonSlot == -1) {
         this.pistonSlot = findHotbarBlock(Blocks.STICKY_PISTON);
      }

      if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
         this.crystalSlot = 999;
      }

      int block = findHotbarBlock(Blocks.REDSTONE_BLOCK);
      int torch = findHotbarBlock(Blocks.REDSTONE_TORCH);
      if (this.redstone.getValue().equals("Block")) {
         this.redStoneSlot = block;
      }

      if (this.redstone.getValue().equals("Torch")) {
         this.redStoneSlot = torch;
      }

      if (this.redstone.getValue().equals("Both")) {
         if (block != -1) {
            this.redStoneSlot = block;
         } else {
            this.redStoneSlot = torch;
         }
      }

      this.useBlock = this.redStoneSlot == block;
      return this.pistonSlot != -1 && this.crystalSlot != -1 && this.redStoneSlot != -1;
   }

   private void reset() {
      for (int i = this.saveArray.length - 1; i > 0; i--) {
         this.saveArray[i] = this.saveArray[i - 1];
      }

      if (this.pistonPos != null) {
         this.saveArray[0] = this.pistonPos;
      }

      for (int i = 0; i < this.saveArray.length; i++) {
         if (i >= this.maxPos.getValue()) {
            this.saveArray[i] = null;
         }
      }

      if (!this.different.getValue()) {
         this.saveArray = new BlockPos[25];
      }

      target = null;
      this.targetPos = null;
      this.rotation = null;
      this.pistonPos = null;
      this.crystalPos = null;
      this.redStonePos = null;
      this.firePos = null;
      this.pistonSlot = -1;
      this.crystalSlot = -1;
      this.redStoneSlot = -1;
      this.obbySlot = -1;
      this.baseTimer = new Timing();
      this.timer = new Timing();
      this.startBreakTimer = new Timing();
      this.breakTimer = new Timing();
      this.preparedSpace = false;
      this.placedPiston = false;
      this.placedCrystal = false;
      this.placedRedstone = false;
      this.brokeCrystal = false;
      this.boom = false;
   }

   public boolean checkPos(BlockPos pos) {
      BlockPos myPos = PlayerUtil.getPlayerPos();
      return pos.getX() == myPos.getX()
         && pos.getZ() == myPos.getZ()
         && (myPos.getY() == pos.getY() || myPos.getY() + 1 == pos.getY());
   }

   public void setItem(int slot) {
      if (slot != 999) {
         this.normalSwitch(slot);
      }
   }

   private void normalSwitch(int slot) {
      if (this.packetSwitch.getValue()) {
         mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
      } else {
         mc.player.inventory.currentItem = slot;
         mc.playerController.updateController();
      }
   }

   public void restoreItem() {
      if (this.silentSwitch.getValue()) {
         if (this.packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlot));
         } else {
            mc.player.inventory.currentItem = this.oldSlot;
            mc.playerController.updateController();
         }
      }
   }

   private boolean placeBlock(BlockPos pos, boolean packet) {
      if (!BlockUtil.canReplace(pos)) {
         return false;
      } else {
         EnumFacing side = BlockUtil.getFirstFacing(pos, this.strict.getValue(), this.needRaytrace(pos));
         if (side == null) {
            return false;
         } else {
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            if (!BlockUtil.canBeClicked(neighbour)) {
               return false;
            } else {
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               boolean sneak = false;
               if (!ColorMain.INSTANCE.sneaking && BlockUtil.blackList.contains(BlockUtil.getBlock(neighbour))) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                  mc.player.setSneaking(true);
                  sneak = true;
               }

               if (packet) {
                  rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite);
               } else {
                  mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
               }

               if (this.swingArm.getValue()) {
                  mc.player.swingArm(EnumHand.MAIN_HAND);
               }

               if (this.rotate.getValue() && this.pistonRotate.getValue()) {
                  BlockUtil.faceVector(hitVec);
               }

               if (sneak) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                  mc.player.setSneaking(false);
               }

               return true;
            }
         }
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction) {
      float f = (float)(vec.x - pos.getX());
      float f1 = (float)(vec.y - pos.getY());
      float f2 = (float)(vec.z - pos.getZ());
      mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
   }

   private int fireLevel(BlockPos pos, BlockPos targetPos) {
      return pos.getX() != targetPos.getX() && pos.getZ() != targetPos.getZ() ? 0 : 1;
   }

   private int blockLevel(BlockPos pos) {
      return pos.getY() * 10000;
   }

   private boolean needRaytrace(BlockPos pos) {
      return mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > this.forceRange.getValue()
         && this.raytrace.getValue();
   }

   public boolean canPistonCrystal(BlockPos crystalPos, BlockPos targetPos) {
      BlockPos offset = new BlockPos(
         targetPos.x - crystalPos.x, crystalPos.y, targetPos.z - crystalPos.z
      );
      BlockPos normal = crystalPos.add(offset);
      BlockPos oneBlock = normal.add(offset);
      BlockPos side0 = normal.add(offset.getZ(), 0, offset.getX());
      BlockPos side1 = normal.add(offset.getZ() * -1, 0, offset.getX() * -1);
      BlockPos side2 = oneBlock.add(offset.getZ(), 0, offset.getX());
      BlockPos side3 = oneBlock.add(offset.getZ() * -1, 0, offset.getX() * -1);
      BlockPos side4 = crystalPos.add(offset.getZ(), 0, offset.getX());
      BlockPos side5 = crystalPos.add(offset.getZ() * -1, 0, offset.getX() * -1);
      List<BlockPos> pistons = new ArrayList<>();
      this.add(pistons, normal);
      this.add(pistons, oneBlock);
      this.add(pistons, side0);
      this.add(pistons, side1);
      this.add(pistons, side2);
      this.add(pistons, side3);
      this.add(pistons, side4);
      this.add(pistons, side5);
      pistons.removeIf(p -> {
         if (!this.different.getValue()) {
            return false;
         } else {
            boolean same = false;

            for (BlockPos savePos : this.saveArray) {
               if (this.isPos2(savePos, p)) {
                  same = true;
                  break;
               }
            }

            return same;
         }
      });
      if (pistons.isEmpty()) {
         return false;
      } else {
         List<BlockPos> pistonList = pistons.stream()
            .filter(
               pistonPos -> {
                  BlockPos headPos = pistonPos.add(offset.getX() * -1, 0, offset.getZ() * -1);
                  Block headBlock = BlockUtil.getBlock(headPos);
                  if (headBlock != Blocks.BEDROCK
                     && headBlock != Blocks.OBSIDIAN
                     && headBlock != Blocks.ENDER_CHEST
                     && headBlock != Blocks.PISTON_HEAD
                     && !this.checkPos(headPos)) {
                     boolean isPiston = BlockUtil.getBlock(pistonPos) instanceof BlockPistonBase;
                     if (!isPiston) {
                        if (!this.canPlace(pistonPos)) {
                           return false;
                        }

                        if (mc.player.getDistance(pistonPos.getX() + 0.5, pistonPos.getY() + 0.5, pistonPos.getZ() + 0.5)
                           > this.range.getValue()) {
                           return false;
                        }

                        double feetY = mc.player.posY;
                        if (PlayerUtil.getDistanceI(pistonPos) < 0.8 + pistonPos.getY() - feetY && pistonPos.getY() > feetY + 1.0
                           || PlayerUtil.getDistanceI(pistonPos) < 1.8 + feetY - pistonPos.getY() && pistonPos.getY() < feetY) {
                           return false;
                        }
                     } else if (this.pistonCheck.getValue() && (this.hasRedstone(pistonPos) || !this.isFacing(pistonPos, headPos))) {
                        return false;
                     }

                     BlockPos redstonePos = this.getRedStonePos(crystalPos, pistonPos, offset);
                     if (redstonePos == null) {
                        return false;
                     } else {
                        return this.flintSlot != -1 && this.getFirePos(targetPos, crystalPos, pistonPos, redstonePos, offset) == null
                           ? false
                           : isPiston
                              || BlockUtil.canPlaceWithoutBase(
                                 pistonPos,
                                 this.strict.getValue(),
                                 this.needRaytrace(pistonPos),
                                 this.canPlace(redstonePos) || this.obbySlot != -1 && (this.base.getValue() || this.canPlace(redstonePos.down()))
                              );
                     }
                  } else {
                     return false;
                  }
               }
            )
            .collect(Collectors.toList());
         return !pistonList.isEmpty();
      }
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         if (this.render.getValue()) {
            if (this.firePos != null && this.fireRender.getValue()) {
               this.drawBoxMain(this.firePos.x, this.firePos.y, this.firePos.z, 255, 160, 0);
            }

            if (this.pistonPos != null && this.crystalPos != null && this.redStonePos != null) {
               this.drawBoxMain(this.pistonPos.x, this.pistonPos.y, this.pistonPos.z, 255, 255, 150);
               this.drawBoxMain(this.crystalPos.x, this.crystalPos.y, this.crystalPos.z, 255, 255, 255);
               this.drawBoxMain(this.redStonePos.x, this.redStonePos.y, this.redStonePos.z, 225, 50, 50);
            }
         }
      }
   }

   void drawBoxMain(double x, double y, double z, int r, int g, int b) {
      AxisAlignedBB box = this.getBox(x, y, z);
      if (this.box.getValue()) {
         RenderUtil.drawBox(box, false, 1.0, new GSColor(r, g, b, 25), 63);
      }

      if (this.outline.getValue()) {
         RenderUtil.drawBoundingBox(box, 1.0, new GSColor(r, g, b, 255));
      }
   }

   AxisAlignedBB getBox(double x, double y, double z) {
      double maxX = x + 1.0;
      double maxZ = z + 1.0;
      return new AxisAlignedBB(x, y, z, maxX, y + 1.0, maxZ);
   }

   @Override
   public String getHudInfo() {
      return this.hud.getValue() && target != null ? "[" + ChatFormatting.WHITE + target.getName() + ChatFormatting.GRAY + "]" : "";
   }

   public class PistonAuraPos {
      public BlockPos targetPos;
      public BlockPos crystal;
      public BlockPos piston;
      public BlockPos redstone;
      public BlockPos fire;
      public BlockPos offset;
      EntityPlayer target;
      boolean block;

      public PistonAuraPos(BlockPos crystal, BlockPos piston, BlockPos redstone, BlockPos offset, EntityPlayer target, BlockPos targetPos, boolean block) {
         this.crystal = crystal;
         this.piston = piston;
         this.redstone = redstone;
         this.offset = offset;
         this.targetPos = targetPos;
         this.target = target;
         this.block = block;
         this.fire = PistonAura.this.getFirePos(targetPos, crystal, piston, redstone, offset);
      }

      public double range() {
         double crystalRange = PlayerUtil.getDistanceL(this.crystal);
         double pistonRange = PlayerUtil.getDistanceL(this.piston);
         return Math.max(pistonRange, crystalRange);
      }
   }
}
