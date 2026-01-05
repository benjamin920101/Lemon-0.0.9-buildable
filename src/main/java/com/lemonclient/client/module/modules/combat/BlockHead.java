package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.dev.PistonAura;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "BlockHead", category = Category.Combat)
public class BlockHead extends Module {
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 20);
   DoubleSetting range = this.registerDouble("Range", 5.0, 0.0, 10.0);
   IntegerSetting maxTarget = this.registerInteger("Max Target", 1, 1, 10);
   DoubleSetting maxSpeed = this.registerDouble("Max Target Speed", 10.0, 0.0, 50.0);
   IntegerSetting bpt = this.registerInteger("BlocksPerTick", 4, 0, 20);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting packet = this.registerBoolean("Packet Place", false);
   BooleanSetting swing = this.registerBoolean("Swing", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting pause = this.registerBoolean("BedrockHole", true);
   int ob;
   int waited;
   int placed;
   BlockPos[] block = new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0)};
   BlockPos[] sides = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};

   public static boolean isPlayerInHole(EntityPlayer target) {
      BlockPos blockPos = getLocalPlayerPosFloored(target);
      HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(blockPos, true, true, false);
      HoleUtil.HoleType holeType = holeInfo.getType();
      return holeType == HoleUtil.HoleType.SINGLE;
   }

   public static BlockPos getLocalPlayerPosFloored(EntityPlayer target) {
      return new BlockPos(target.getPositionVector());
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && !(entity instanceof EntityArmorStand) && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.placed = 0;
         if (this.waited++ >= this.delay.getValue()) {
            this.waited = 0;
            this.ob = BurrowUtil.findHotbarBlock(BlockObsidian.class);
            if (this.ob != -1) {
               Iterator var1 = PlayerUtil.getNearPlayers(this.range.getValue(), this.maxTarget.getValue()).iterator();

               while (true) {
                  BlockPos placePos;
                  label139:
                  while (true) {
                     if (!var1.hasNext()) {
                        return;
                     }

                     EntityPlayer target = (EntityPlayer)var1.next();
                     if (target != null
                        && !EntityUtil.isDead(target)
                        && !(LemonClient.speedUtil.getPlayerSpeed(target) > this.maxSpeed.getValue())
                        && isPlayerInHole(target)) {
                        BlockPos pos = new BlockPos(target.posX, target.posY + 0.5, target.posZ);
                        int bedrock = 0;

                        for (BlockPos side : this.sides) {
                           if (mc.world.getBlockState(pos.add(side)).getBlock() == Blocks.BEDROCK) {
                              bedrock++;
                           }
                        }

                        if (bedrock < 4 || this.pause.getValue()) {
                           placePos = pos.up(2);
                           if (BlockUtil.isAir(placePos) && !this.intersectsWithEntity(placePos)) {
                              if (BurrowUtil.getFirstFacing(pos.up(2)) != null) {
                                 break;
                              }

                              List<BlockPos> posList = new ArrayList<>();
                              List<BlockPos> list = new ArrayList<>();

                              for (BlockPos sidex : this.sides) {
                                 BlockPos crystalPos = pos.add(sidex);
                                 if (!PistonAura.INSTANCE.canPistonCrystal(crystalPos, pos)) {
                                    posList.add(crystalPos);
                                 }

                                 list.add(crystalPos);
                              }

                              if (posList.isEmpty()) {
                                 for (BlockPos sidex : this.sides) {
                                    BlockPos crystalPos = pos.add(sidex);
                                    if (!PistonAura.INSTANCE.canPistonCrystal(crystalPos.up(), pos)) {
                                       posList.add(crystalPos);
                                    }

                                    list.add(crystalPos);
                                 }
                              }

                              if (posList.isEmpty()) {
                                 posList.addAll(list);
                              }

                              BlockPos sidex = posList.stream().max(Comparator.comparing(PlayerUtil::getDistance)).orElse(null);
                              if (sidex != null) {
                                 BlockPos[] var21 = this.block;
                                 int var23 = var21.length;
                                 int var25 = 0;

                                 while (true) {
                                    if (var25 >= var23) {
                                       break label139;
                                    }

                                    BlockPos add = var21[var25];
                                    if (this.placed > this.bpt.getValue()) {
                                       return;
                                    }

                                    BlockPos obsi = sidex.up().add(add);
                                    if (!this.intersectsWithEntity(obsi) && BlockUtil.canReplace(obsi)) {
                                       InventoryUtil.run(
                                          this.ob,
                                          this.packetSwitch.getValue(),
                                          () -> BurrowUtil.placeBlock(
                                             obsi, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                                          )
                                       );
                                       this.placed++;
                                    }

                                    var25++;
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (this.placed > this.bpt.getValue()) {
                     return;
                  }

                  InventoryUtil.run(
                     this.ob,
                     this.packetSwitch.getValue(),
                     () -> BurrowUtil.placeBlock(placePos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue())
                  );
                  this.placed++;
               }
            }
         }
      }
   }
}
