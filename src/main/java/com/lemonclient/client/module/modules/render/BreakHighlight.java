package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.DrawBlockDamageEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import java.util.HashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "BreakHighlight", category = Category.Render)
public class BreakHighlight extends Module {
   public static BreakHighlight INSTANCE;
   BooleanSetting cancelAnimation = this.registerBoolean("No Animation", true);
   IntegerSetting range = this.registerInteger("Range", 64, 0, 256);
   IntegerSetting playerRange = this.registerInteger("Player Range", 16, 0, 64);
   BooleanSetting showProgress = this.registerBoolean("Show Progress", false);
   IntegerSetting decimal = this.registerInteger("Decimal", 2, 0, 2, () -> this.showProgress.getValue());
   BooleanSetting doubleMine = this.registerBoolean("Double Mine", true);
   ColorSetting nameColor = this.registerColor("Name Color", new GSColor(255, 255, 255));
   ModeSetting renderType = this.registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
   ColorSetting color = this.registerColor("Color", new GSColor(0, 255, 0, 255));
   ColorSetting dColor = this.registerColor("Double Color", new GSColor(0, 255, 0, 255), () -> this.doubleMine.getValue());
   IntegerSetting alpha = this.registerInteger("Alpha", 100, 0, 255);
   IntegerSetting outAlpha = this.registerInteger("Outline Alpha", 255, 0, 255);
   IntegerSetting width = this.registerInteger("Width", 1, 0, 5);
   DoubleSetting scale = this.registerDouble("Text Scale", 0.025, 0.01, 0.05);
   HashMap<EntityPlayer, BreakHighlight.renderBlock> list = new HashMap<>();
   BlockPos lastBreak;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim)event.getPacket();
            BlockPos blockPos = packet.getPosition();
            if (mc.player.getDistanceSq(blockPos) > this.range.getValue() * this.range.getValue()) {
               return;
            }

            EntityPlayer entityPlayer = (EntityPlayer)mc.world.getEntityByID(packet.getBreakerId());
            if (entityPlayer == null) {
               return;
            }

            if (this.list.containsKey(entityPlayer)) {
               if (this.isPos2(this.list.get(entityPlayer).pos.pos, blockPos)) {
                  return;
               }

               this.list.get(entityPlayer).pos.updatePos(blockPos);
            } else {
               this.list.put(entityPlayer, new BreakHighlight.renderBlock(new BreakHighlight.breakPos(blockPos), entityPlayer));
            }
         }
      }
   });
   @EventHandler
   private final Listener<DrawBlockDamageEvent> drawBlockDamageEventListener = new Listener<>(event -> {
      if (this.cancelAnimation.getValue()) {
         event.cancel();
      }
   });
   @EventHandler
   private final Listener<PacketEvent.PostSend> listener = new Listener<>(event -> {
      if (event.getPacket() instanceof CPacketPlayerDigging) {
         CPacketPlayerDigging packet = (CPacketPlayerDigging)event.getPacket();
         if (packet.getAction() == Action.START_DESTROY_BLOCK) {
            this.lastBreak = packet.getPosition();
         }
      }
   });

   public BreakHighlight() {
      INSTANCE = this;
   }

   private boolean isPos2(BlockPos pos1, BlockPos pos2) {
      return pos1 != null && pos2 != null
         ? pos1.x == pos2.x && pos1.y == pos2.y && pos1.z == pos2.z
         : false;
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         for (EntityPlayer player : mc.world.playerEntities) {
            if (this.list.containsKey(player)) {
               BlockPos pos = this.list.get(player).pos.pos;
               BlockPos dPos = this.list.get(player).pos.dPos;
               if (pos != null && mc.world.getBlockState(pos).getBlockHardness(mc.world, pos) < 0.0F) {
                  this.list.get(player).pos.remove();
               }

               if (dPos != null && mc.world.getBlockState(dPos).getBlockHardness(mc.world, dPos) < 0.0F) {
                  this.list.get(player).pos.removeDouble();
               }

               if (this.isPos2(pos, dPos)) {
                  dPos = null;
               }

               if (pos != null || dPos != null) {
                  int rangeSq = this.range.getValue() * this.range.getValue();
                  int playerSq = this.playerRange.getValue() * this.playerRange.getValue();
                  if (pos == null || !(mc.player.getDistanceSq(pos) > rangeSq) || dPos == null || !(mc.player.getDistanceSq(dPos) > rangeSq)) {
                     if (pos != null && player.getDistanceSq(pos) > playerSq && dPos != null && player.getDistanceSq(dPos) > playerSq) {
                        this.list.remove(player);
                     } else {
                        this.list.get(player).update();
                     }
                  }
               }
            }
         }
      } else {
         this.list.clear();
      }
   }

   public static GSColor getRainbowColor(int damage) {
      return GSColor.fromHSB((1 + damage * 32) % 11520 / 11520.0F, 1.0F, 1.0F);
   }

   private void renderBox(BreakHighlight.breakPos pos, EntityPlayer player) {
      String[] name = new String[]{player.getName()};
      BlockPos blockPos = pos.pos;
      if (blockPos != null) {
         float mineDamage = (float)(System.currentTimeMillis() - pos.start) / (float)pos.time;
         if (mineDamage > 1.0F) {
            mineDamage = 1.0F;
         }

         AxisAlignedBB getSelectedBoundingBox = new AxisAlignedBB(blockPos);
         Vec3d getCenter = getSelectedBoundingBox.getCenter();
         float prognum = mineDamage * 100.0F;
         if (this.showProgress.getValue()) {
            String[] progress = new String[]{String.format("%.0f", prognum)};
            if (this.decimal.getValue() == 1) {
               progress = new String[]{String.format("%.1f", prognum)};
            } else if (this.decimal.getValue() == 2) {
               progress = new String[]{String.format("%.2f", prognum)};
            }

            RenderUtil.drawNametag(
               blockPos.getX() + 0.5,
               blockPos.getY() + 0.39,
               blockPos.getZ() + 0.5,
               progress,
               getRainbowColor((int)prognum),
               1,
               this.scale.getValue(),
               0.0
            );
            RenderUtil.drawNametag(
               blockPos.getX() + 0.5,
               blockPos.getY() + 0.61,
               blockPos.getZ() + 0.5,
               name,
               new GSColor(this.nameColor.getColor(), 255),
               1,
               this.scale.getValue(),
               0.0
            );
         } else {
            RenderUtil.drawNametag(
               blockPos.getX() + 0.5,
               blockPos.getY() + 0.5,
               blockPos.getZ() + 0.5,
               name,
               new GSColor(this.nameColor.getColor(), 255),
               1,
               this.scale.getValue(),
               0.0
            );
         }

         this.renderESP(
            new AxisAlignedBB(
                  getCenter.x,
                  getCenter.y,
                  getCenter.z,
                  getCenter.x,
                  getCenter.y,
                  getCenter.z
               )
               .grow(
                  (getSelectedBoundingBox.minX - getSelectedBoundingBox.maxX) * 0.5 * MathHelper.clamp(mineDamage, 0.0F, 1.0F),
                  (getSelectedBoundingBox.minY - getSelectedBoundingBox.maxY) * 0.5 * MathHelper.clamp(mineDamage, 0.0F, 1.0F),
                  (getSelectedBoundingBox.minZ - getSelectedBoundingBox.maxZ) * 0.5 * MathHelper.clamp(mineDamage, 0.0F, 1.0F)
               ),
            false
         );
      }

      if (this.doubleMine.getValue()) {
         BlockPos doubleBlockPos = pos.dPos;
         if (doubleBlockPos != null) {
            float doubleMineDamage = (float)(System.currentTimeMillis() - pos.dStart) / (float)pos.dTime;
            if (doubleMineDamage > 1.0F) {
               doubleMineDamage = 1.0F;
            }

            AxisAlignedBB getDoubleSelectedBoundingBox = new AxisAlignedBB(doubleBlockPos);
            Vec3d getDoubleCenter = getDoubleSelectedBoundingBox.getCenter();
            float doublePrognum = doubleMineDamage * 100.0F;
            if (this.showProgress.getValue()) {
               String[] progress = new String[]{String.format("%.0f", doublePrognum)};
               if (this.decimal.getValue() == 1) {
                  progress = new String[]{String.format("%.1f", doublePrognum)};
               } else if (this.decimal.getValue() == 2) {
                  progress = new String[]{String.format("%.2f", doublePrognum)};
               }

               RenderUtil.drawNametag(
                  doubleBlockPos.getX() + 0.5,
                  doubleBlockPos.getY() + 0.39,
                  doubleBlockPos.getZ() + 0.5,
                  progress,
                  getRainbowColor((int)doublePrognum),
                  1,
                  this.scale.getValue(),
                  0.0
               );
               RenderUtil.drawNametag(
                  doubleBlockPos.getX() + 0.5,
                  doubleBlockPos.getY() + 0.61,
                  doubleBlockPos.getZ() + 0.5,
                  name,
                  new GSColor(this.nameColor.getColor(), 255),
                  1,
                  this.scale.getValue(),
                  0.0
               );
            } else {
               RenderUtil.drawNametag(
                  doubleBlockPos.getX() + 0.5,
                  doubleBlockPos.getY() + 0.5,
                  doubleBlockPos.getZ() + 0.5,
                  name,
                  new GSColor(this.nameColor.getColor(), 255),
                  1,
                  this.scale.getValue(),
                  0.0
               );
            }

            this.renderESP(
               new AxisAlignedBB(
                     getDoubleCenter.x,
                     getDoubleCenter.y,
                     getDoubleCenter.z,
                     getDoubleCenter.x,
                     getDoubleCenter.y,
                     getDoubleCenter.z
                  )
                  .grow(
                     (getDoubleSelectedBoundingBox.minX - getDoubleSelectedBoundingBox.maxX)
                        * 0.5
                        * MathHelper.clamp(doubleMineDamage, 0.0F, 1.0F),
                     (getDoubleSelectedBoundingBox.minY - getDoubleSelectedBoundingBox.maxY)
                        * 0.5
                        * MathHelper.clamp(doubleMineDamage, 0.0F, 1.0F),
                     (getDoubleSelectedBoundingBox.minZ - getDoubleSelectedBoundingBox.maxZ)
                        * 0.5
                        * MathHelper.clamp(doubleMineDamage, 0.0F, 1.0F)
                  ),
               true
            );
         }
      }
   }

   private void renderESP(AxisAlignedBB axisAlignedBB, boolean dm) {
      GSColor fillColor = new GSColor(dm ? this.dColor.getValue() : this.color.getValue(), this.alpha.getValue());
      GSColor outlineColor = new GSColor(dm ? this.dColor.getValue() : this.color.getValue(), this.outAlpha.getValue());
      String var5 = this.renderType.getValue();
      switch (var5) {
         case "Fill":
            RenderUtil.drawBox(axisAlignedBB, true, 0.0, fillColor, 63);
            break;
         case "Outline":
            RenderUtil.drawBoundingBox(axisAlignedBB, this.width.getValue().intValue(), outlineColor);
            break;
         default:
            RenderUtil.drawBox(axisAlignedBB, true, 0.0, fillColor, 63);
            RenderUtil.drawBoundingBox(axisAlignedBB, this.width.getValue().intValue(), outlineColor);
      }
   }

   private int calcBreakTime(BlockPos pos) {
      if (pos == null) {
         return -1;
      } else {
         IBlockState blockState = mc.world.getBlockState(pos);
         float hardness = blockState.getBlockHardness(mc.world, pos);
         float breakSpeed = this.getBreakSpeed(pos, blockState);
         if (breakSpeed == -1.0F) {
            return -1;
         } else {
            float relativeDamage = breakSpeed / hardness / 30.0F;
            int ticks = (int)Math.ceil(0.7F / relativeDamage);
            return ticks * 50;
         }
      }
   }

   private float getBreakSpeed(BlockPos pos, IBlockState blockState) {
      float maxSpeed = 1.0F;
      int slot = this.findItem(pos);
      float speed = mc.player.inventory.getStackInSlot(slot).getDestroySpeed(blockState);
      if (speed <= 1.0F) {
         return maxSpeed;
      } else {
         int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, mc.player.inventory.getStackInSlot(slot));
         if (efficiency > 0) {
            speed += efficiency * efficiency + 1.0F;
         }

         if (speed > maxSpeed) {
            maxSpeed = speed;
         }

         return maxSpeed;
      }
   }

   public int findItem(BlockPos pos) {
      return pos == null ? mc.player.inventory.currentItem : findBestTool(pos, mc.world.getBlockState(pos));
   }

   public static int findBestTool(BlockPos pos, IBlockState state) {
      int result = mc.player.inventory.currentItem;
      if (state.getBlockHardness(mc.world, pos) > 0.0F) {
         double speed = getSpeed(state, mc.player.getHeldItemMainhand());

         for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            double stackSpeed = getSpeed(state, stack);
            if (stackSpeed > speed) {
               speed = stackSpeed;
               result = i;
            }
         }
      }

      return result;
   }

   public static double getSpeed(IBlockState state, ItemStack stack) {
      double str = stack.getDestroySpeed(state);
      int effect = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
      return Math.max(str + (str > 1.0 ? effect * effect + 1.0 : 0.0), 0.0);
   }

   public static class breakPos {
      private BlockPos pos;
      private BlockPos dPos = null;
      private long start;
      private long dStart;
      private long time;
      private long dTime;

      public breakPos(BlockPos pos) {
         this.pos = pos;
         this.start = System.currentTimeMillis();
         this.time = BreakHighlight.INSTANCE.calcBreakTime(pos);
      }

      public void updatePos(BlockPos pos) {
         if (this.dPos == null) {
            this.dPos = this.pos;
            this.dStart = this.start;
            this.dTime = (long)(this.time * 1.4);
         }

         this.pos = pos;
         this.start = System.currentTimeMillis();
         this.time = BreakHighlight.INSTANCE.calcBreakTime(pos);
      }

      public long getEnd() {
         return this.start + this.time;
      }

      public void update() {
         this.time = BreakHighlight.INSTANCE.calcBreakTime(this.pos);
         if (this.dPos != null && BlockUtil.airBlocks.contains(BreakHighlight.mc.world.getBlockState(this.dPos).getBlock())) {
            this.removeDouble();
         }
      }

      public void remove() {
         this.pos = null;
      }

      public void removeDouble() {
         this.dPos = null;
      }
   }

   class renderBlock {
      private final BreakHighlight.breakPos pos;
      private final EntityPlayer player;

      public renderBlock(BreakHighlight.breakPos pos, EntityPlayer player) {
         this.pos = pos;
         this.player = player;
      }

      void update() {
         this.pos.update();
         BreakHighlight.this.renderBox(this.pos, this.player);
      }
   }
}
