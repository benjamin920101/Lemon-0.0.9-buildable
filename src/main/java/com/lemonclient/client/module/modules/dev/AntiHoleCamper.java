package com.lemonclient.client.module.modules.dev;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.lemonclient.api.event.Phase;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.PlayerPacket;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.manager.managers.PlayerPacketManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPiston;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AntiHoleCamper", category = Category.Dev, priority = 1000)
public class AntiHoleCamper extends Module {
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 20);
   BooleanSetting pause = this.registerBoolean("Pause When Move", true);
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Block", "Torch", "Both"), "Block");
   IntegerSetting range = this.registerInteger("Range", 6, 0, 10);
   BooleanSetting look = this.registerBoolean("Looking Target", false);
   BooleanSetting ground = this.registerBoolean("OnGround Check", true);
   BooleanSetting box = this.registerBoolean("Entity Box", true);
   BooleanSetting hole = this.registerBoolean("Double Hole Check", false);
   BooleanSetting pushCheck = this.registerBoolean("Push Check", false);
   BooleanSetting headCheck = this.registerBoolean("Head Check", false);
   BooleanSetting breakRedstone = this.registerBoolean("Break Redstone", false);
   BooleanSetting pushedCheck = this.registerBoolean("Pushed Check", true, () -> this.breakRedstone.getValue());
   ModeSetting breakBlock = this.registerMode("Break Block", Arrays.asList("Normal", "Packet"), "Packet", () -> this.breakRedstone.getValue());
   BooleanSetting packetPiston = this.registerBoolean("Packet Place Piston", true);
   BooleanSetting packetRedstone = this.registerBoolean("Packet Place Redstone", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting block = this.registerBoolean("Place Block", true);
   BooleanSetting packet = this.registerBoolean("Packet Place", true, () -> this.block.getValue());
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting update = this.registerBoolean("Update Controller", true);
   BooleanSetting force = this.registerBoolean("Force Rotate", true);
   BooleanSetting strict = this.registerBoolean("Strict", true);
   BooleanSetting raytrace = this.registerBoolean("RayTrace", true);
   DoubleSetting maxSpeed = this.registerDouble("Max Target Speed", 5.0, 0.0, 50.0);
   BooleanSetting debug = this.registerBoolean("Debug Msg", true);
   ModeSetting disable = this.registerMode("Disable Mode", Arrays.asList("NoDisable", "Check", "AutoDisable"), "AutoDisable");
   IntegerSetting disableDelay = this.registerInteger("Disable Delay", 0, 0, 50);
   private final Timing timer = new Timing();
   BlockPos beforePlayerPos;
   BlockPos pistonPos;
   BlockPos redstonePos;
   AntiHoleCamper.PistonPos pos = null;
   boolean useBlock;
   boolean disabling;
   int redstoneSlot;
   int pistonSlot;
   int obsiSlot;
   int waited;
   int wait;
   int[] enemyCoordsInt;
   EntityPlayer aimTarget = null;
   Vec2f rotation;
   Vec3d[] sides = new Vec3d[]{new Vec3d(0.25, 0.0, 0.25), new Vec3d(-0.25, 0.0, 0.25), new Vec3d(0.25, 0.0, -0.25), new Vec3d(-0.25, 0.0, -0.25)};
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(
      event -> {
         if (event.getPhase() == Phase.PRE && this.rotation != null) {
            PlayerPacket packet = new PlayerPacket(
               this, new Vec2f(this.rotation.x, PlayerPacketManager.INSTANCE.getServerSideRotation().y)
            );
            PlayerPacketManager.INSTANCE.addPacket(packet);
         }
      }
   );
   @EventHandler
   private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
      if (this.rotation != null && this.force.getValue()) {
         if (event.getPacket() instanceof Rotation) {
            ((Rotation)event.getPacket()).yaw = this.rotation.x;
         }

         if (event.getPacket() instanceof PositionRotation) {
            ((PositionRotation)event.getPacket()).yaw = this.rotation.x;
         }
      }
   });

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

            if (this.update.getValue()) {
               mc.playerController.updateController();
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

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private boolean airBlock(BlockPos pos) {
      return BlockUtil.canReplace(pos);
   }

   private boolean canPlacePiston(BlockPos pos, EnumFacing facing) {
      BlockPos p = pos.offset(facing);
      BlockPos push = pos.offset(facing, -1);
      double feetY = mc.player.posY;
      return (
            !this.intersectsWithEntity(p)
                  && this.airBlock(p)
                  && (!(PlayerUtil.getDistanceI(p) < 1.4 + p.getY() - feetY) || !(p.getY() > feetY + 1.0))
                  && (!(PlayerUtil.getDistanceI(p) < 2.4 + feetY - p.getY()) || !(p.getY() < feetY))
                  && BlockUtil.canPlaceWithoutBase(p, this.strict.getValue(), this.raytrace.getValue(), true)
               || this.isFacing(p, pos)
                  && (
                     mc.world.getBlockState(p).getBlock() instanceof BlockPistonBase
                        || mc.world.getBlockState(p).getBlock() == Blocks.PISTON
                        || mc.world.getBlockState(p).getBlock() == Blocks.STICKY_PISTON
                  )
         )
         && (!this.hole.getValue() || this.airBlock(push))
         && (!this.pushCheck.getValue() || this.airBlock(push.up()) && (this.airBlock(push.up(2)) || this.airBlock(push)));
   }

   public BlockPos getRedstonePos(BlockPos pistonPos) {
      BlockPos pos = this.hasRedstoneBlock(pistonPos);
      if (pos != null) {
         return pos;
      } else {
         List<BlockPos> redstone = new ArrayList<>();
         if (this.useBlock) {
            for (EnumFacing facing : EnumFacing.VALUES) {
               redstone.add(pistonPos.offset(facing));
            }
         } else {
            BlockPos[] offsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};

            for (BlockPos offs : offsets) {
               for (int i = 0; i < 2; i++) {
                  BlockPos torch = pistonPos.down(i).add(offs);
                  if (i != 1 || !BlockUtil.isBlockUnSolid(torch.up())) {
                     redstone.add(torch);
                  }
               }
            }
         }

         redstone = redstone.stream()
            .filter(
               p -> !ColorMain.INSTANCE.breakList.contains(p)
                  && !this.intersectsWithEntity(p)
                  && mc.player.getDistance(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)
                     <= this.range.getValue().intValue()
            )
            .collect(Collectors.toList());
         if (redstone.isEmpty()) {
            return null;
         } else {
            List<BlockPos> hasBase = redstone.stream()
               .filter(p -> BlockUtil.canPlaceWithoutBase(p, this.strict.getValue(), this.raytrace.getValue(), false))
               .collect(Collectors.toList());
            if (hasBase.isEmpty()) {
               hasBase.addAll(redstone);
            }

            return hasBase.stream().min(Comparator.comparing(mc.player::getDistanceSq)).orElse(null);
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.breakRedstone.getValue()
         && this.redstonePos != null
         && !this.airBlock(this.redstonePos)
         && (
            !this.pushedCheck.getValue()
               || mc.world.getBlockState(this.beforePlayerPos).getBlock() == Blocks.PISTON_HEAD
               || mc.world.getBlockState(this.beforePlayerPos.up()).getBlock() == Blocks.PISTON_HEAD
         )) {
         this.doBreak(this.redstonePos);
      }
   }

   @Override
   public void onEnable() {
      this.disabling = false;
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.rotation = null;
         this.aimTarget = null;
         if (this.breakRedstone.getValue()
            && this.redstonePos != null
            && !this.airBlock(this.redstonePos)
            && (
               !this.pushedCheck.getValue()
                  || mc.world.getBlockState(this.beforePlayerPos).getBlock() == Blocks.PISTON_HEAD
                  || mc.world.getBlockState(this.beforePlayerPos.up()).getBlock() == Blocks.PISTON_HEAD
            )) {
            this.doBreak(this.redstonePos);
         }

         if (this.disabling && !this.disable.getValue().equals("NoDisable")) {
            if (this.wait++ >= this.disableDelay.getValue()) {
               boolean placed = true;
               if (this.block.getValue()) {
                  if (this.timer.passedMs(1000L)) {
                     this.switchTo(
                        this.obsiSlot,
                        () -> BlockUtil.placeBlock(
                           this.beforePlayerPos,
                           this.rotate.getValue(),
                           this.packet.getValue(),
                           this.strict.getValue(),
                           this.raytrace.getValue(),
                           this.swing.getValue()
                        )
                     );
                     this.timer.reset();
                     if (this.disable.getValue().equals("AutoDisable")) {
                        this.disable();
                        return;
                     }
                  }

                  placed = mc.world.getBlockState(this.beforePlayerPos).getBlock() == Blocks.OBSIDIAN;
               } else if (this.disable.getValue().equals("AutoDisable")) {
                  this.disable();
               }

               if (this.disable.getValue().equals("Check") && placed) {
                  this.disable();
               }

               this.wait = 0;
               return;
            }
         } else {
            this.wait = 0;
         }

         if (this.waited++ >= this.delay.getValue() && (!MotionUtil.isMoving(mc.player) || !this.pause.getValue())) {
            this.waited = 0;
            this.redstoneSlot = this.pistonSlot = this.obsiSlot - 1;
            if (!this.ready()) {
               if (!this.disable.getValue().equals("NoDisable")) {
                  this.disable();
               }
            } else {
               if (!this.look.getValue()) {
                  this.aimTarget = PlayerUtil.getNearestPlayer(this.range.getValue().intValue() + 1.5);
               } else {
                  this.aimTarget = PlayerUtil.findLookingPlayer(this.range.getValue().intValue() + 1.5);
               }

               this.pos = null;
               if (this.aimTarget != null) {
                  if (LemonClient.speedUtil.getPlayerSpeed(this.aimTarget) > this.maxSpeed.getValue()) {
                     return;
                  }

                  if (!this.aimTarget.onGround && this.ground.getValue()) {
                     return;
                  }

                  this.beforePlayerPos = new BlockPos(this.aimTarget.posX, this.aimTarget.posY, this.aimTarget.posZ);
                  this.enemyCoordsInt = new int[]{(int)this.aimTarget.posX, (int)this.aimTarget.posY, (int)this.aimTarget.posZ};
                  if (!this.box.getValue()) {
                     this.pos = this.getPos(this.beforePlayerPos, this.beforePlayerPos);
                  } else {
                     List<AntiHoleCamper.PistonPos> list = new ArrayList<>();

                     for (Vec3d side : this.sides) {
                        Vec3d vec3d = new Vec3d(
                           this.aimTarget.posX + side.x, this.aimTarget.posY, this.aimTarget.posZ + side.z
                        );
                        BlockPos blockPos = vec3toBlockPos(vec3d);
                        AntiHoleCamper.PistonPos piston = this.getPos(blockPos, blockPos);
                        if (piston != null) {
                           list.add(piston);
                        }
                     }

                     this.pos = list.stream()
                        .filter(p -> p.getMaxRange() <= this.range.getValue().intValue())
                        .min(Comparator.comparing(AntiHoleCamper.PistonPos::getMaxRange))
                        .orElse(null);
                  }

                  if (this.pos == null) {
                     if (this.box.getValue()) {
                        List<AntiHoleCamper.PistonPos> list = new ArrayList<>();

                        for (Vec3d sidex : this.sides) {
                           Vec3d vec3d = new Vec3d(
                              this.aimTarget.posX + sidex.x,
                              this.aimTarget.posY,
                              this.aimTarget.posZ + sidex.z
                           );
                           BlockPos blockPos = vec3toBlockPos(vec3d);
                           AntiHoleCamper.PistonPos piston = this.getPos(blockPos.up(), blockPos);
                           if (piston != null) {
                              list.add(piston);
                           }
                        }

                        this.pos = list.stream()
                           .filter(p -> p.getMaxRange() <= this.range.getValue().intValue())
                           .min(Comparator.comparing(AntiHoleCamper.PistonPos::getMaxRange))
                           .orElse(null);
                     } else {
                        this.pos = this.getPos(this.beforePlayerPos.up(), this.beforePlayerPos);
                     }
                  }
               } else {
                  if (this.debug.getValue()) {
                     MessageBus.sendClientDeleteMessage("Cant find target", Notification.Type.ERROR, "AntiCamp", 7);
                  }

                  if (!this.disable.getValue().equals("NoDisable")) {
                     this.disable();
                  }
               }

               if (this.pos != null) {
                  if (this.redstonePos != null
                     && !this.useBlock
                     && mc.world.getBlockState(this.redstonePos.down()).getBlock() == Blocks.AIR) {
                     BlockPos obsiPos = new BlockPos(this.redstonePos.x, this.redstonePos.y - 1, this.redstonePos.z);
                     this.switchTo(
                        this.obsiSlot,
                        () -> BlockUtil.placeBlock(
                           obsiPos, this.rotate.getValue(), this.packet.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue()
                        )
                     );
                  }

                  this.pistonPos = this.pos.piston;
                  this.redstonePos = this.pos.redstone;
                  this.beforePlayerPos = this.pos.calcPos;
                  if (BurrowUtil.getFirstFacing(this.redstonePos) == null) {
                     this.placePiston(this.pistonPos, this.beforePlayerPos);
                     this.placeRedstone(this.redstonePos);
                  } else {
                     this.placeRedstone(this.redstonePos);
                     this.placePiston(this.pistonPos, this.beforePlayerPos);
                  }

                  this.disabling = true;
               }
            }
         }
      } else {
         this.disable();
      }
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   private void placePiston(BlockPos pistonPos, BlockPos targetPos) {
      if (BlockUtil.isAir(pistonPos)) {
         float[] angle = MathUtil.calcAngle(
            new Vec3d(pistonPos.x, 0.0, pistonPos.z), new Vec3d(targetPos.x, 0.0, targetPos.z)
         );
         this.rotation = new Vec2f(angle[0] + 180.0F, angle[1]);
         mc.player.connection.sendPacket(new Rotation(angle[0] + 180.0F, angle[1], true));
         this.switchTo(this.pistonSlot, () -> {
            BlockUtil.placeBlock(pistonPos, false, this.packetPiston.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue());
            if (this.rotate.getValue()) {
               EntityUtil.facePlacePos(pistonPos, this.strict.getValue(), this.raytrace.getValue());
            }
         });
      }
   }

   private void placeRedstone(BlockPos redstonePos) {
      this.switchTo(
         this.redstoneSlot,
         () -> BlockUtil.placeBlock(
            redstonePos, this.rotate.getValue(), this.packetRedstone.getValue(), this.strict.getValue(), this.raytrace.getValue(), this.swing.getValue()
         )
      );
   }

   private AntiHoleCamper.PistonPos getPos(BlockPos calcPos, BlockPos playerPos) {
      if (mc.world.getBlockState(calcPos).getBlock() != Blocks.BEDROCK
         && mc.world.getBlockState(calcPos).getBlock() != Blocks.OBSIDIAN) {
         List<AntiHoleCamper.PistonPos> posList = new ArrayList<>();
         if (this.headCheck.getValue() && !this.airBlock(playerPos.up(2))) {
            return null;
         } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
               if (facing != EnumFacing.UP && facing != EnumFacing.DOWN && this.canPlacePiston(calcPos, facing)) {
                  BlockPos pistonPos = calcPos.offset(facing);
                  BlockPos redstonePos = this.getRedstonePos(pistonPos);
                  if (redstonePos != null && (BlockUtil.hasNeighbour(redstonePos) || BlockUtil.hasNeighbour(pistonPos))) {
                     posList.add(new AntiHoleCamper.PistonPos(pistonPos, redstonePos, calcPos));
                  }
               }
            }

            return posList.stream()
               .filter(p -> p.getMaxRange() <= this.range.getValue().intValue())
               .min(Comparator.comparing(AntiHoleCamper.PistonPos::getMaxRange))
               .orElse(null);
         }
      } else {
         return null;
      }
   }

   public static BlockPos vec3toBlockPos(Vec3d vec3d) {
      return new BlockPos(Math.floor(vec3d.x), Math.round(vec3d.y), Math.floor(vec3d.z));
   }

   private boolean ready() {
      this.pistonSlot = findHotbarBlock(Blocks.PISTON);
      if (this.pistonSlot == -1) {
         this.pistonSlot = findHotbarBlock(Blocks.STICKY_PISTON);
      }

      this.redstoneSlot = !this.mode.getValue().equals("Torch") ? findHotbarBlock(Blocks.REDSTONE_BLOCK) : findHotbarBlock(Blocks.REDSTONE_TORCH);
      if (this.mode.getValue().equals("Both") && this.redstoneSlot == -1) {
         this.redstoneSlot = findHotbarBlock(Blocks.REDSTONE_TORCH);
      }

      this.obsiSlot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
      if (this.redstoneSlot == -1) {
         if (this.debug.getValue()) {
            MessageBus.sendClientDeleteMessage("Cant find Redstone", Notification.Type.ERROR, "AntiCamp", 7);
         }

         return false;
      } else {
         this.useBlock = this.redstoneSlot == findHotbarBlock(Blocks.REDSTONE_BLOCK);
         if ((!this.useBlock || this.block.getValue()) && this.obsiSlot == -1) {
            if (this.debug.getValue()) {
               MessageBus.sendClientDeleteMessage("Cant find Obsidian", Notification.Type.ERROR, "AntiCamp", 7);
            }

            return false;
         } else if (BurrowUtil.findHotbarBlock(ItemPiston.class) == -1) {
            if (this.debug.getValue()) {
               MessageBus.sendClientDeleteMessage("Cant find Piston", Notification.Type.ERROR, "AntiCamp", 7);
            }

            return false;
         } else {
            return true;
         }
      }
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

   private void doBreak(BlockPos pos) {
      if (this.swing.getValue()) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }

      if (this.breakBlock.getValue().equals("Packet")) {
         mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
         mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
      } else {
         mc.playerController.onPlayerDamageBlock(pos, EnumFacing.UP);
      }
   }

   public boolean isFacing(BlockPos pos, BlockPos facingPos) {
      ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
      UnmodifiableIterator var4 = properties.keySet().iterator();

      while (var4.hasNext()) {
         IProperty<?> prop = (IProperty<?>)var4.next();
         if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {
            BlockPos pushPos = pos.offset((EnumFacing)properties.get(prop));
            if (this.isPos2(facingPos, pushPos)) {
               return true;
            }
         }
      }

      return false;
   }

   public BlockPos hasRedstoneBlock(BlockPos pos) {
      List<BlockPos> redstone = new ArrayList<>();
      BlockPos[] offsets = new BlockPos[]{new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};

      for (BlockPos redstonePos : offsets) {
         redstone.add(pos.add(redstonePos));
      }

      if (this.useBlock) {
         redstone.add(pos.add(0, 1, 0));
      }

      return redstone.stream()
         .filter(p -> BlockUtil.getBlock(p) == Blocks.REDSTONE_TORCH || BlockUtil.getBlock(p) == Blocks.REDSTONE_BLOCK)
         .min(Comparator.comparing(PlayerUtil::getDistanceI))
         .orElse(null);
   }

   static class PistonPos {
      public BlockPos piston;
      public BlockPos redstone;
      public BlockPos calcPos;

      public PistonPos(BlockPos pistonPos, BlockPos redstonePos, BlockPos pos) {
         this.piston = pistonPos;
         this.redstone = redstonePos;
         this.calcPos = pos;
      }

      public double getMaxRange() {
         return this.piston != null && this.redstone != null ? Math.max(PlayerUtil.getDistance(this.piston), PlayerUtil.getDistance(this.redstone)) : 999999.0;
      }
   }
}
