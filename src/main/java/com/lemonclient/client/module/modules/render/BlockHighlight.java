package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

@Module.Declaration(name = "BlockHighlight", category = Category.Render)
public class BlockHighlight extends Module {
   ModeSetting renderLook = this.registerMode("Render", Arrays.asList("Block", "Side"), "Block");
   ModeSetting renderType = this.registerMode("Type", Arrays.asList("Outline", "Fill", "Both"), "Outline");
   IntegerSetting lineWidth = this.registerInteger("Width", 1, 1, 5);
   ColorSetting renderColor = this.registerColor("Color", new GSColor(255, 0, 0, 255));
   private int lookInt;

   @Override
   public void onWorldRender(RenderEvent event) {
      RayTraceResult rayTraceResult = mc.objectMouseOver;
      if (rayTraceResult != null) {
         EnumFacing enumFacing = mc.objectMouseOver.sideHit;
         if (enumFacing != null) {
            GSColor colorWithOpacity = new GSColor(this.renderColor.getValue(), 50);
            String var5 = this.renderLook.getValue();
            switch (var5) {
               case "Block":
                  this.lookInt = 0;
                  break;
               case "Side":
                  this.lookInt = 1;
            }

            if (rayTraceResult.typeOfHit == Type.BLOCK) {
               BlockPos blockPos = rayTraceResult.getBlockPos();
               AxisAlignedBB axisAlignedBB = mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos);
               if (mc.world.getBlockState(blockPos).getMaterial() != Material.AIR) {
                  var5 = this.renderType.getValue();
                  switch (var5) {
                     case "Outline":
                        this.renderOutline(axisAlignedBB, this.lineWidth.getValue(), this.renderColor.getValue(), enumFacing, this.lookInt);
                        break;
                     case "Fill":
                        this.renderFill(axisAlignedBB, colorWithOpacity, enumFacing, this.lookInt);
                        break;
                     case "Both":
                        this.renderOutline(axisAlignedBB, this.lineWidth.getValue(), this.renderColor.getValue(), enumFacing, this.lookInt);
                        this.renderFill(axisAlignedBB, colorWithOpacity, enumFacing, this.lookInt);
                  }
               }
            }
         }
      }
   }

   public void renderOutline(AxisAlignedBB axisAlignedBB, int width, GSColor color, EnumFacing enumFacing, int lookInt) {
      if (lookInt == 0) {
         RenderUtil.drawBoundingBox(axisAlignedBB, width, color);
      } else if (lookInt == 1) {
         RenderUtil.drawBoundingBoxWithSides(axisAlignedBB, width, color, this.findRenderingSide(enumFacing));
      }
   }

   public void renderFill(AxisAlignedBB axisAlignedBB, GSColor color, EnumFacing enumFacing, int lookInt) {
      int facing = 0;
      if (lookInt == 0) {
         facing = 63;
      } else if (lookInt == 1) {
         facing = this.findRenderingSide(enumFacing);
      }

      RenderUtil.drawBox(axisAlignedBB, true, 1.0, color, facing);
   }

   private int findRenderingSide(EnumFacing enumFacing) {
      int facing = 0;
      if (enumFacing == EnumFacing.EAST) {
         facing = 32;
      } else if (enumFacing == EnumFacing.WEST) {
         facing = 16;
      } else if (enumFacing == EnumFacing.NORTH) {
         facing = 4;
      } else if (enumFacing == EnumFacing.SOUTH) {
         facing = 8;
      } else if (enumFacing == EnumFacing.UP) {
         facing = 2;
      } else if (enumFacing == EnumFacing.DOWN) {
         facing = 1;
      }

      return facing;
   }
}
