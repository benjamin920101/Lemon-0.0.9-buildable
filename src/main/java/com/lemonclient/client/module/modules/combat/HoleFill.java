package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.CrystalUtil;
import com.lemonclient.api.util.misc.MathUtil;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.PredictUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.api.util.world.combat.DamageUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.exploits.PacketMine;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "HoleFill", category = Category.Combat, priority = 999)
public class HoleFill extends Module {
   BooleanSetting test = this.registerBoolean("Test", false);
   ModeSetting page = this.registerMode("Page", Arrays.asList("Target", "Place", "HoleFill", "SelfFill", "Render"), "Target");
   IntegerSetting maxTarget = this.registerInteger("Max Target", 10, 1, 50, () -> this.page.getValue().equals("Target"));
   IntegerSetting tickAdd = this.registerInteger("Tick Add", 8, 1, 30, () -> this.page.getValue().equals("Target"));
   IntegerSetting maxTick = this.registerInteger("Max Tick", 8, 0, 30, () -> this.page.getValue().equals("Target"));
   BooleanSetting calculateYPredict = this.registerBoolean("Calculate Y Predict", true, () -> this.page.getValue().equals("Target"));
   IntegerSetting startDecrease = this.registerInteger(
      "Start Decrease", 39, 0, 200, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Target")
   );
   IntegerSetting exponentStartDecrease = this.registerInteger(
      "Exponent Start", 2, 1, 5, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Target")
   );
   IntegerSetting decreaseY = this.registerInteger("Decrease Y", 2, 1, 5, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Target"));
   IntegerSetting exponentDecreaseY = this.registerInteger(
      "Exponent Decrease Y", 1, 1, 3, () -> this.calculateYPredict.getValue() && this.page.getValue().equals("Target")
   );
   BooleanSetting splitXZ = this.registerBoolean("Split XZ", true, () -> this.page.getValue().equals("Target"));
   BooleanSetting manualOutHole = this.registerBoolean("Manual Out Hole", false, () -> this.page.getValue().equals("Target"));
   BooleanSetting aboveHoleManual = this.registerBoolean(
      "Above Hole Manual", false, () -> this.manualOutHole.getValue() && this.page.getValue().equals("Target")
   );
   BooleanSetting stairPredict = this.registerBoolean("Stair Predict", false, () -> this.page.getValue().equals("Target"));
   IntegerSetting nStair = this.registerInteger("N Stair", 2, 1, 4, () -> this.stairPredict.getValue() && this.page.getValue().equals("Target"));
   DoubleSetting speedActivationStair = this.registerDouble(
      "Speed Activation Stair", 0.3, 0.0, 1.0, () -> this.stairPredict.getValue() && this.page.getValue().equals("Target")
   );
   IntegerSetting delay = this.registerInteger("Calc Delay", 0, 0, 1000, () -> this.page.getValue().equals("Place"));
   BooleanSetting upPlate = this.registerBoolean("Up Slab", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting selfFill = this.registerBoolean("Self Fill", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting mine = this.registerBoolean("Mine SelfFill", true, () -> this.page.getValue().equals("Place") && this.selfFill.getValue());
   BooleanSetting selfTrap = this.registerBoolean("Self Trap", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting yCheck = this.registerBoolean("Y Check", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting web = this.registerBoolean("Web", true, () -> this.page.getValue().equals("Place") && this.yCheck.getValue());
   BooleanSetting above = this.registerBoolean("Above", true, () -> this.page.getValue().equals("Place") && this.yCheck.getValue());
   BooleanSetting raytraceCheck = this.registerBoolean("Raytrace Check", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting holeCheck = this.registerBoolean("InHole Check", true, () -> this.page.getValue().equals("Place"));
   IntegerSetting placeDelay = this.registerInteger("Place Delay", 50, 0, 1000, () -> this.page.getValue().equals("Place"));
   IntegerSetting bpc = this.registerInteger("Block pre Tick", 6, 1, 20, () -> this.page.getValue().equals("Place"));
   DoubleSetting range = this.registerDouble("Range", 6.0, 0.0, 10.0, () -> this.page.getValue().equals("Place"));
   DoubleSetting yRange = this.registerDouble("Y Range", 2.5, 0.0, 6.0, () -> this.page.getValue().equals("Place"));
   DoubleSetting fillRange = this.registerDouble("Fill Range", 3.0, 0.0, 6.0, () -> this.page.getValue().equals("Place"));
   DoubleSetting fillYRange = this.registerDouble("Fill YRange", 3.0, 0.0, 10.0, () -> this.page.getValue().equals("Place"));
   DoubleSetting safety = this.registerDouble("Safety Range", 3.0, 0.0, 6.0, () -> this.page.getValue().equals("Place"));
   BooleanSetting rotate = this.registerBoolean("Rotate", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting strict = this.registerBoolean("Strict", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting raytrace = this.registerBoolean("RayTrace", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting onGround = this.registerBoolean("OnGround", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting packet = this.registerBoolean("Packet Place", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting swing = this.registerBoolean("Swing", false, () -> this.page.getValue().equals("Place"));
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true, () -> this.page.getValue().equals("Place"));
   BooleanSetting render = this.registerBoolean("Render", false, () -> this.page.getValue().equals("Render"));
   BooleanSetting box = this.registerBoolean("Box", true, () -> this.page.getValue().equals("Render") && this.render.getValue());
   BooleanSetting outline = this.registerBoolean("Outline", true, () -> this.page.getValue().equals("Render") && this.render.getValue());
   IntegerSetting width = this.registerInteger(
      "Width", 1, 1, 5, () -> this.page.getValue().equals("Render") && this.render.getValue() && this.outline.getValue()
   );
   ColorSetting color = this.registerColor("Color", new GSColor(255, 0, 0), () -> this.page.getValue().equals("Render") && this.render.getValue());
   IntegerSetting alpha = this.registerInteger(
      "Alpha", 75, 0, 255, () -> this.page.getValue().equals("Render") && this.render.getValue() && this.box.getValue()
   );
   IntegerSetting outAlpha = this.registerInteger(
      "Outline Alpha", 125, 0, 255, () -> this.page.getValue().equals("Render") && this.render.getValue() && this.outline.getValue()
   );
   BooleanSetting animate = this.registerBoolean("Animate", true, () -> this.page.getValue().equals("Render") && this.render.getValue());
   IntegerSetting time = this.registerInteger("Life Time", 500, 0, 1000, () -> this.page.getValue().equals("Render") && this.render.getValue());
   BooleanSetting hObby = this.registerBoolean("H-Obby", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting hEChest = this.registerBoolean("H-EChest", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting hWeb = this.registerBoolean("H-Web", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting hSlab = this.registerBoolean("H-Slab", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting hSkull = this.registerBoolean("H-Skull", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting hTrap = this.registerBoolean("H-Trapdoor", true, () -> this.page.getValue().equals("HoleFill"));
   BooleanSetting sObby = this.registerBoolean("S-Obby", true, () -> this.page.getValue().equals("SelfFill"));
   BooleanSetting sEChest = this.registerBoolean("S-EChest", true, () -> this.page.getValue().equals("SelfFill"));
   BooleanSetting sWeb = this.registerBoolean("S-Web", true, () -> this.page.getValue().equals("SelfFill"));
   BooleanSetting sSlab = this.registerBoolean("S-Slab", true, () -> this.page.getValue().equals("SelfFill"));
   BooleanSetting sSkull = this.registerBoolean("S-Skull", true, () -> this.page.getValue().equals("SelfFill"));
   BooleanSetting sTrap = this.registerBoolean("S-Trapdoor", true, () -> this.page.getValue().equals("SelfFill"));
   ModeSetting jumpMode = this.registerMode("JumpMode", Arrays.asList("Normal", "Future", "Strict"), "Normal", () -> this.page.getValue().equals("SelfFill"));
   ModeSetting rubberBand = this.registerMode(
      "RubberBand",
      Arrays.asList("Cn", "Strict", "Future", "FutureStrict", "Troll", "Void", "Auto", "Test", "Custom"),
      "Cn",
      () -> this.page.getValue().equals("SelfFill")
   );
   HoleFill.managerClassRenderBlocks managerRenderBlocks = new HoleFill.managerClassRenderBlocks();
   List<BlockPos> posList = new ArrayList<>();
   Timing timer = new Timing();
   Timing placeTimer = new Timing();
   boolean trapdoor;
   boolean mined;
   boolean self;
   boolean placedSelf;
   int placed;
   int slot;
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
   Vec3d[] add = new Vec3d[]{new Vec3d(0.1, 0.0, 0.1), new Vec3d(-0.1, 0.0, 0.1), new Vec3d(-0.1, 0.0, -0.1), new Vec3d(0.1, 0.0, -0.1)};

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (this.timer.passedMs(this.delay.getValue().intValue())) {
            this.posList = this.calc();
            this.timer.reset();
         }
      }
   }

   @Override
   public void fast() {
      if (mc.world != null && mc.player != null && (mc.player.onGround || !this.onGround.getValue())) {
         if (this.placeTimer.passedMs(this.placeDelay.getValue().intValue()) && !this.posList.isEmpty()) {
            this.slot = this.findRightBlock(false);
            InventoryUtil.run(this.slot, this.packetSwitch.getValue(), () -> {
               for (BlockPos pos : this.posList) {
                  if (this.placed >= this.bpc.getValue()) {
                     break;
                  }

                  this.placeBlock(pos);
               }
            });
            this.placeTimer.reset();
         }

         if (this.mine.getValue() && !this.self && this.placedSelf) {
            boolean air = BlockUtil.isAir(PlayerUtil.getPlayerPos());
            if (this.mined) {
               if (air) {
                  if (ModuleManager.isModuleEnabled(PacketMine.class)) {
                     PacketMine.INSTANCE.lastBlock = null;
                  }

                  this.mined = false;
                  this.placedSelf = false;
               }
            } else if (!air) {
               mc.playerController.onPlayerDamageBlock(PlayerUtil.getPlayerPos(), EnumFacing.UP);
               this.mined = true;
            }
         }
      }
   }

   private List<BlockPos> calc() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.placed = 0;
         List<BlockPos> check = new ArrayList<>();
         List<HoleFill.HoleInfo> holeList = new ArrayList<>();

         for (BlockPos pos : EntityUtil.getSphere(PlayerUtil.getEyesPos(), this.range.getValue() + 1.0, this.yRange.getValue() + 1.0, false, false, 0)) {
            if (!check.contains(pos)
               && BlockUtil.canReplace(pos)
               && !DamageUtil.isResistantMine(pos.up())
               && !DamageUtil.isResistantMine(pos.up(2))) {
               HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, true, false);
               HoleUtil.HoleType holeType = holeInfo.getType();
               if (holeType != HoleUtil.HoleType.NONE) {
                  AxisAlignedBB box = holeInfo.getCentre();
                  Vec3d center = box.getCenter();
                  List<BlockPos> holePos = new ArrayList<>();

                  for (Vec3d add : this.add) {
                     BlockPos hole = new BlockPos(center.x + add.x, center.y, center.z + add.z);
                     if (!holePos.contains(hole)) {
                        holePos.add(hole);
                     }
                  }

                  check.addAll(holePos);
                  boolean recall = false;

                  for (BlockPos block : holePos) {
                     boolean selfFilling = this.isPlayer(block);
                     if (selfFilling) {
                        if (this.selfTrap.getValue()) {
                           break;
                        }

                        if (!this.selfFill.getValue() || this.findRightBlock(true) == -1) {
                           recall = true;
                           break;
                        }
                     }

                     if (ColorMain.INSTANCE.breakList.contains(block)) {
                        recall = true;
                        break;
                     }
                  }

                  if (!recall) {
                     holeList.add(new HoleFill.HoleInfo(holePos, box));
                  }
               }
            }
         }

         List<BlockPos> holePos = new ArrayList<>();
         List<EntityPlayer> targets = PlayerUtil.getNearPlayers(this.range.getValue() + this.fillRange.getValue(), this.maxTarget.getMax())
            .stream()
            .filter(player -> !this.holeCheck.getValue() || !HoleUtil.isInHole(player, false, false, false))
            .collect(Collectors.toList());
         if (this.test.getValue()) {
            targets.add(mc.player);
         }

         List<EntityPlayer> listPlayer = new ArrayList<>();

         for (EntityPlayer player : targets) {
            for (int tick = 0; tick <= this.maxTick.getValue() + this.tickAdd.getValue(); tick += this.tickAdd.getValue()) {
               if (tick >= this.maxTick.getValue()) {
                  tick = this.maxTick.getValue();
               }

               listPlayer.add(
                  PredictUtil.predictPlayer(
                     player,
                     new PredictUtil.PredictSettings(
                        tick,
                        this.calculateYPredict.getValue(),
                        this.startDecrease.getValue(),
                        this.exponentStartDecrease.getValue(),
                        this.decreaseY.getValue(),
                        this.exponentDecreaseY.getValue(),
                        this.splitXZ.getValue(),
                        this.manualOutHole.getValue(),
                        this.aboveHoleManual.getValue(),
                        this.stairPredict.getValue(),
                        this.nStair.getValue(),
                        this.speedActivationStair.getValue()
                     )
                  )
               );
               if (tick == this.maxTick.getValue()) {
                  break;
               }
            }
         }

         boolean fill = false;
         AxisAlignedBB selfBox = mc.player.getEntityBoundingBox();

         label173:
         for (HoleFill.HoleInfo hole : holeList) {
            Iterator var35 = listPlayer.iterator();

            while (true) {
               EntityPlayer target;
               while (true) {
                  if (!var35.hasNext()) {
                     continue label173;
                  }

                  target = (EntityPlayer)var35.next();
                  AxisAlignedBB targetBox = target.boundingBox;
                  if (targetBox.intersects(hole.checkBox)) {
                     if (hole.box.intersects(targetBox)) {
                        continue label173;
                     }

                     double y = hole.box.minY + 1.0;
                     if (!this.yCheck.getValue() || (int)(target.posY + 0.5) == y) {
                        break;
                     }

                     if (target.posY < y) {
                        if (!this.web.getValue() || !target.isInWeb) {
                           boolean cancel = false;

                           for (int value = (int)y - 1 - (int)target.posY; value > 0; value--) {
                              boolean recall = false;

                              for (BlockPos blockPos : hole.posList) {
                                 BlockPos posx = blockPos.down(value);
                                 if (DamageUtil.isResistantMine(posx)) {
                                    cancel = true;
                                    recall = true;
                                    break;
                                 }
                              }

                              if (recall) {
                                 break;
                              }
                           }

                           if (!cancel) {
                              break;
                           }
                        }
                     } else {
                        if (!this.above.getValue()) {
                           break;
                        }

                        boolean cancel = false;

                        for (int value = (int)target.posY - (int)y; value > 0; value--) {
                           boolean recall = false;

                           for (BlockPos blockPosx : hole.posList) {
                              BlockPos posx = blockPosx.up(value);
                              if (DamageUtil.isResistantMine(posx)) {
                                 cancel = true;
                                 recall = true;
                                 break;
                              }
                           }

                           if (recall) {
                              break;
                           }
                        }

                        if (!cancel) {
                           break;
                        }
                     }
                  }
               }

               if (!this.raytraceCheck.getValue() || CrystalUtil.calculateRaytrace(target, hole.box.getCenter())) {
                  if (!fill && selfBox.intersects(hole.box)) {
                     fill = true;
                  }

                  holePos.addAll(hole.posList);
                  break;
               }
            }
         }

         this.self = fill;
         boolean inHole = HoleUtil.isInHole(mc.player, false, true, false);
         holePos.sort(Comparator.comparing(p -> p.y));
         holePos.removeIf(
            posx -> {
               if (this.checkPlaceRange(posx) && !DamageUtil.isResistantMine(posx.up()) && !DamageUtil.isResistantMine(posx.up(2))) {
                  return !inHole && MathUtil.isIntersect(selfBox.grow(this.safety.getValue()), new AxisAlignedBB(posx))
                     ? true
                     : holePos.contains(posx.up());
               } else {
                  return true;
               }
            }
         );
         return holePos;
      } else {
         return new ArrayList<>();
      }
   }

   private boolean checkPlaceRange(BlockPos pos) {
      BlockPos playerPos = new BlockPos(
         Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)
      );
      double x = playerPos.x - (pos.x + 0.5);
      double y = playerPos.y - (pos.y + 0.5);
      double z = playerPos.z - (pos.z + 0.5);
      return x * x <= this.range.getValue() * this.range.getValue()
         && y * y <= this.yRange.getValue() * this.yRange.getValue()
         && z * z <= this.range.getValue() * this.range.getValue();
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && MathUtil.isIntersect(new AxisAlignedBB(pos), entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private boolean isPlayer(BlockPos pos) {
      for (EntityPlayer entity : mc.world.playerEntities) {
         if (entity == mc.player && MathUtil.isIntersect(new AxisAlignedBB(pos), entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   private void placeBlock(BlockPos pos) {
      if (pos != null) {
         boolean selfFilling = this.isPlayer(pos);
         if (selfFilling && this.selfTrap.getValue()) {
            int obby = BurrowUtil.findHotbarBlock(BlockObsidian.class);
            if (obby != -1) {
               InventoryUtil.run(obby, this.packetSwitch.getValue(), () -> {
                  BlockPos ori = pos.up();
                  if (BurrowUtil.getFirstFacing(pos.up(2)) == null) {
                     BlockPos e = null;
                     boolean isNull = true;

                     for (BlockPos sidexx : this.sides) {
                        BlockPos added = ori.up().add(sidexx);
                        if (!this.intersectsWithEntity(added) && BurrowUtil.getFirstFacing(added) != null) {
                           e = added;
                           isNull = false;
                           break;
                        }
                     }

                     if (isNull) {
                        for (BlockPos sidex : this.sides) {
                           BlockPos added = ori.add(sidex);
                           if (!this.intersectsWithEntity(added) && !this.intersectsWithEntity(added.up())) {
                              this.placeTrapBlock(added);
                              e = added.up();
                              break;
                           }
                        }
                     }

                     this.placeTrapBlock(e);
                  }

                  this.placeTrapBlock(pos.up(2));
               });
               return;
            }
         }

         int fillSlot = -1;
         if (selfFilling) {
            if (!this.selfFill.getValue()) {
               return;
            }

            fillSlot = this.findRightBlock(true);
            if (fillSlot == -1) {
               return;
            }
         } else if (this.intersectsWithEntity(pos)) {
            return;
         }

         this.trapdoor = fillSlot == InventoryUtil.findFirstBlockSlot(BlockTrapDoor.class, 0, 8)
            || this.upPlate.getValue() && fillSlot == BurrowUtil.findHotbarBlock(BlockSlab.class);
         boolean jump = fillSlot == BurrowUtil.findHotbarBlock(BlockEnderChest.class) || fillSlot == BurrowUtil.findHotbarBlock(BlockObsidian.class);
         EnumFacing side = this.trapdoor ? BurrowUtil.getTrapdoorFacing(pos) : BlockUtil.getFirstFacing(pos, this.strict.getValue(), this.raytrace.getValue());
         if (side != null) {
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour)
               .add(0.5, this.trapdoor ? 0.8 : 0.5, 0.5)
               .add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            if ((
                  BlockUtil.blackList.contains(mc.world.getBlockState(neighbour).getBlock())
                     || BlockUtil.shulkerList.contains(mc.world.getBlockState(neighbour).getBlock())
               )
               && !mc.player.isSneaking()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
               mc.player.setSneaking(true);
            }

            if (selfFilling) {
               this.placedSelf = true;
               if (this.trapdoor) {
                  double x = mc.player.posX;
                  double y = (int)mc.player.posY;
                  double z = mc.player.posZ;
                  if (fillSlot == InventoryUtil.findFirstBlockSlot(BlockTrapDoor.class, 0, 8)) {
                     mc.player.connection.sendPacket(new Position(x, y + 0.2F, z, mc.player.onGround));
                  } else {
                     this.jump();
                  }

                  mc.player.connection.sendPacket(new CPacketHeldItemChange(fillSlot));
                  BurrowUtil.rightClickBlock(neighbour, opposite, new Vec3d(0.5, 0.8, 0.5), true, this.swing.getValue());
                  if (fillSlot == InventoryUtil.findFirstBlockSlot(BlockTrapDoor.class, 0, 8)) {
                     mc.player.connection.sendPacket(new Position(x, y, z, mc.player.onGround));
                  } else {
                     this.rubberBand();
                  }

                  mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slot));
                  return;
               }

               if (jump) {
                  this.jump();
               }
            }

            if (this.rotate.getValue()) {
               BurrowUtil.faceVector(hitVec, true);
            }

            InventoryUtil.run(
               jump ? fillSlot : this.slot,
               this.packetSwitch.getValue(),
               () -> BurrowUtil.rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite, this.packet.getValue(), this.swing.getValue())
            );
            if (selfFilling) {
               this.rubberBand();
            }

            this.managerRenderBlocks.addRender(pos);
            this.placed++;
         }
      }
   }

   public static BlockPos getFlooredPosition(Entity entity) {
      return new BlockPos(Math.floor(entity.posX), Math.round(entity.posY), Math.floor(entity.posZ));
   }

   private void placeTrapBlock(BlockPos pos) {
      if (!ColorMain.INSTANCE.breakList.contains(pos)) {
         BurrowUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
      }
   }

   private int findRightBlock(boolean selfFill) {
      int slot = -1;
      if (selfFill) {
         if (this.sTrap.getValue()) {
            slot = InventoryUtil.findFirstBlockSlot(BlockTrapDoor.class, 0, 8);
         }

         if (this.sSkull.getValue() && slot == -1) {
            slot = InventoryUtil.findSkullSlot();
         }

         if (this.sWeb.getValue() && slot == -1) {
            slot = InventoryUtil.findFirstBlockSlot(BlockWeb.class, 0, 8);
         }

         if (this.sSlab.getValue() && slot == -1) {
            slot = BurrowUtil.findHotbarBlock(BlockSlab.class);
         }

         if (this.sEChest.getValue() && slot == -1) {
            slot = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
         }

         if (this.sObby.getValue() && slot == -1) {
            slot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         }
      } else {
         if (this.hObby.getValue()) {
            slot = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         }

         if (this.hEChest.getValue() && slot == -1) {
            slot = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
         }

         if (this.hSlab.getValue() && slot == -1) {
            slot = BurrowUtil.findHotbarBlock(BlockSlab.class);
         }

         if (this.hWeb.getValue() && slot == -1) {
            slot = InventoryUtil.findFirstBlockSlot(BlockWeb.class, 0, 8);
         }

         if (this.hSkull.getValue() && slot == -1) {
            slot = InventoryUtil.findSkullSlot();
         }

         if (this.hTrap.getValue()) {
            slot = InventoryUtil.findFirstBlockSlot(BlockTrapDoor.class, 0, 8);
         }
      }

      return slot;
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      this.managerRenderBlocks.update(this.time.getValue());
      this.managerRenderBlocks.render();
   }

   boolean sameBlockPos(BlockPos first, BlockPos second) {
      return first != null && second != null
         ? first.getX() == second.getX()
            && first.getY() == second.getY()
            && first.getZ() == second.getZ()
         : false;
   }

   public static void back() {
      for (Entity crystal : mc.world
         .loadedEntityList
         .stream()
         .filter(e -> e instanceof EntityEnderCrystal && !e.isDead)
         .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
         .collect(Collectors.toList())) {
         if (crystal instanceof EntityEnderCrystal) {
            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
         }
      }
   }

   private boolean canGoTo(BlockPos pos) {
      return isAir(pos) && isAir(pos.up());
   }

   public static boolean isAir(Vec3d vec3d) {
      return isAir(new BlockPos(vec3d));
   }

   public static boolean isAir(BlockPos pos) {
      return BlockUtil.canReplace(pos);
   }

   public static Vec3d getEyesPos() {
      return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
   }

   private void jump() {
      String var1 = this.jumpMode.getValue();
      switch (var1) {
         case "Normal":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 0.419999986886978, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 0.7531999805212015, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.001335979112147, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.166109260938214, mc.player.posZ, false)
               );
            break;
         case "Future":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 0.419997486886978, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.7500025, mc.player.posZ, false));
            mc.player
               .connection
               .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.999995, mc.player.posZ, false));
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.170005001788139, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.2426050013947485, mc.player.posZ, false)
               );
            break;
         case "Strict":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 0.419998586886978, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.7500014, mc.player.posZ, false));
            mc.player
               .connection
               .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.9999972, mc.player.posZ, false));
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.170002801788139, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.170009801788139, mc.player.posZ, false)
               );
      }
   }

   private void rubberBand() {
      String var1 = this.rubberBand.getValue();
      switch (var1) {
         case "Cn":
            double distance = 0.0;
            BlockPos bestPos = null;

            for (BlockPos pos : BlockUtil.getBox(6.0F)) {
               if (this.canGoTo(pos)
                  && !(mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 3.0)
                  && (
                     bestPos == null
                        || !(mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) >= distance)
                  )) {
                  bestPos = pos;
                  distance = mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
               }
            }

            if (bestPos != null) {
               mc.player
                  .connection
                  .sendPacket(new Position(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
            } else {
               mc.player.connection.sendPacket(new Position(mc.player.posX, -7.0, mc.player.posZ, false));
            }
            break;
         case "Future":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.242609801394749, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 2.340028003576279, mc.player.posZ, false)
               );
            break;
         case "FutureStrict":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.315205001001358, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 1.315205001001358, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 2.485225002789497, mc.player.posZ, false)
               );
            break;
         case "Troll":
            mc.player
               .connection
               .sendPacket(
                  new Position(mc.player.posX, mc.player.posY + 3.3400880035762786, mc.player.posZ, false)
               );
            mc.player
               .connection
               .sendPacket(new Position(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ, false));
            break;
         case "Strict":
            double distance = 0.0;
            BlockPos bestPos = null;

            for (int i = 0; i < 20; i++) {
               BlockPos posx = new BlockPos(mc.player.posX, mc.player.posY + 0.5 + i, mc.player.posZ);
               if (this.canGoTo(posx)
                  && mc.player.getDistance(posx.getX() + 0.5, posx.getY() + 0.5, posx.getZ() + 0.5) > 5.0
                  && (
                     bestPos == null
                        || mc.player.getDistance(posx.getX() + 0.5, posx.getY() + 0.5, posx.getZ() + 0.5) < distance
                  )) {
                  bestPos = posx;
                  distance = mc.player.getDistance(posx.getX() + 0.5, posx.getY() + 0.5, posx.getZ() + 0.5);
               }
            }

            if (bestPos != null) {
               mc.player
                  .connection
                  .sendPacket(new Position(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
            } else {
               mc.player.connection.sendPacket(new Position(mc.player.posX, -7.0, mc.player.posZ, false));
            }
            break;
         case "Void":
            mc.player.connection.sendPacket(new Position(mc.player.posX, -7.0, mc.player.posZ, false));
            break;
         case "Auto":
            for (int ix = -10; ix < 10; ix++) {
               if (ix == -1) {
                  ix = 4;
               }

               if (mc.world.getBlockState(getFlooredPosition(mc.player).add(0, ix, 0)).getBlock().equals(Blocks.AIR)
                  && mc.world
                     .getBlockState(getFlooredPosition(mc.player).add(0, ix + 1, 0))
                     .getBlock()
                     .equals(Blocks.AIR)) {
                  BlockPos posx = getFlooredPosition(mc.player).add(0, ix, 0);
                  mc.player
                     .connection
                     .sendPacket(new Position(posx.getX() + 0.3, posx.getY(), posx.getZ() + 0.3, false));
                  break;
               }
            }
      }
   }

   class HoleInfo {
      List<BlockPos> posList;
      AxisAlignedBB checkBox;
      AxisAlignedBB box;

      public HoleInfo(List<BlockPos> posList, AxisAlignedBB box) {
         this.posList = posList;
         this.box = box;
         this.checkBox = new AxisAlignedBB(
            box.minX - HoleFill.this.fillRange.getValue(),
            box.minY,
            box.minZ - HoleFill.this.fillRange.getValue(),
            box.maxX + HoleFill.this.fillRange.getValue(),
            box.maxY + HoleFill.this.fillYRange.getValue(),
            box.maxZ + HoleFill.this.fillRange.getValue()
         );
      }
   }

   class managerClassRenderBlocks {
      ArrayList<HoleFill.renderBlock> blocks = new ArrayList<>();

      void update(int time) {
         this.blocks.removeIf(e -> System.currentTimeMillis() - e.start > time);
      }

      void render() {
         this.blocks.forEach(HoleFill.renderBlock::render);
      }

      void addRender(BlockPos pos) {
         boolean render = true;

         for (HoleFill.renderBlock block : this.blocks) {
            if (HoleFill.this.sameBlockPos(block.pos, pos)) {
               render = false;
               block.resetTime();
               break;
            }
         }

         if (render) {
            this.blocks.add(HoleFill.this.new renderBlock(pos));
         }
      }
   }

   class renderBlock {
      private final BlockPos pos;
      private long start = System.currentTimeMillis();
      boolean placed;

      public renderBlock(BlockPos pos) {
         this.pos = pos;
         this.placed = false;
      }

      void resetTime() {
         this.start = System.currentTimeMillis();
      }

      void render() {
         if (!this.placed) {
            if (!DamageUtil.isResistantMine(this.pos)) {
               return;
            }

            this.resetTime();
            this.placed = true;
         }

         AxisAlignedBB alignedBB = new AxisAlignedBB(this.pos);
         if (HoleFill.this.animate.getValue()) {
            alignedBB = alignedBB.grow(this.delta() * this.delta() / 2.0 - 1.0);
         }

         if (HoleFill.this.box.getValue()) {
            RenderUtil.drawBox(alignedBB, true, 1.0, new GSColor(HoleFill.this.color.getColor(), this.returnGradient()), 63);
         }

         if (HoleFill.this.outline.getValue()) {
            RenderUtil.drawBoundingBox(
               alignedBB, HoleFill.this.width.getValue().intValue(), new GSColor(HoleFill.this.color.getColor(), this.returnOutGradient())
            );
         }
      }

      public double delta() {
         long end = this.start + HoleFill.this.time.getValue().intValue();
         double result = (double)(end - System.currentTimeMillis()) / (end - this.start);
         if (result < 0.0) {
            result = 0.0;
         }

         if (result > 1.0) {
            result = 1.0;
         }

         return 1.0 - result;
      }

      public int returnGradient() {
         return (int)(HoleFill.this.alpha.getValue().intValue() * (1.0 - this.delta()));
      }

      public int returnOutGradient() {
         return (int)(HoleFill.this.outAlpha.getValue().intValue() * (1.0 - this.delta()));
      }
   }
}
