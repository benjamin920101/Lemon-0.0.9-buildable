package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import io.netty.util.internal.ConcurrentSet;
import java.util.Arrays;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "VoidESP", category = Category.Render)
public class VoidESP extends Module {
   IntegerSetting renderDistance = this.registerInteger("Distance", 10, 1, 40);
   IntegerSetting activeYValue = this.registerInteger("Activate Y", 20, 0, 256);
   ModeSetting renderType = this.registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
   ModeSetting renderMode = this.registerMode("Mode", Arrays.asList("Box", "Flat"), "Flat");
   IntegerSetting width = this.registerInteger("Width", 1, 1, 10);
   ColorSetting color = this.registerColor("Color", new GSColor(255, 255, 0));
   private ConcurrentSet<BlockPos> voidHoles;

   @Override
   public void onUpdate() {
      if (mc.player.dimension != 1) {
         if (mc.player.getPosition().getY() <= this.activeYValue.getValue()) {
            if (this.voidHoles == null) {
               this.voidHoles = new ConcurrentSet();
            } else {
               this.voidHoles.clear();
            }

            for (BlockPos blockPos : BlockUtil.getCircle(getPlayerPos(), 0, this.renderDistance.getValue().intValue(), false)) {
               if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.BEDROCK)
                  && !this.isAnyBedrock(blockPos, VoidESP.Offsets.center)) {
                  this.voidHoles.add(blockPos);
               }
            }
         }
      }
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.player != null && this.voidHoles != null) {
         if (mc.player.getPosition().getY() <= this.activeYValue.getValue()) {
            if (!this.voidHoles.isEmpty()) {
               this.voidHoles.forEach(blockPos -> {
                  if (this.renderMode.getValue().equalsIgnoreCase("Box")) {
                     this.drawBox(blockPos);
                  } else {
                     this.drawFlat(blockPos);
                  }

                  this.drawOutline(blockPos, this.width.getValue());
               });
            }
         }
      }
   }

   public static BlockPos getPlayerPos() {
      return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
   }

   private boolean isAnyBedrock(BlockPos origin, BlockPos[] offset) {
      for (BlockPos pos : offset) {
         if (mc.world.getBlockState(origin.add(pos)).getBlock().equals(Blocks.BEDROCK)) {
            return true;
         }
      }

      return false;
   }

   private void drawFlat(BlockPos blockPos) {
      if (this.renderType.getValue().equalsIgnoreCase("Fill") || this.renderType.getValue().equalsIgnoreCase("Both")) {
         GSColor c = new GSColor(this.color.getValue(), 50);
         if (this.renderMode.getValue().equalsIgnoreCase("Flat")) {
            RenderUtil.drawBox(blockPos, 1.0, c, 1);
         }
      }
   }

   private void drawBox(BlockPos blockPos) {
      if (this.renderType.getValue().equalsIgnoreCase("Fill") || this.renderType.getValue().equalsIgnoreCase("Both")) {
         GSColor c = new GSColor(this.color.getValue(), 50);
         RenderUtil.drawBox(blockPos, 1.0, c, 63);
      }
   }

   private void drawOutline(BlockPos blockPos, int width) {
      if (this.renderType.getValue().equalsIgnoreCase("Outline") || this.renderType.getValue().equalsIgnoreCase("Both")) {
         if (this.renderMode.getValue().equalsIgnoreCase("Box")) {
            RenderUtil.drawBoundingBox(blockPos, 1.0, width, this.color.getValue());
         }

         if (this.renderMode.getValue().equalsIgnoreCase("Flat")) {
            RenderUtil.drawBoundingBoxWithSides(blockPos, width, this.color.getValue(), 1);
         }
      }
   }

   private static class Offsets {
      static final BlockPos[] center = new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0), new BlockPos(0, 2, 0)};
   }
}
