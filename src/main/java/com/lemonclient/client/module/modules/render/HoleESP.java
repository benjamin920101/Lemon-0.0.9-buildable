package com.lemonclient.client.module.modules.render;

import com.google.common.collect.Sets;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.player.RotationUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "HoleESP", category = Category.Render)
public class HoleESP extends Module {
   public IntegerSetting range = this.registerInteger("Range", 5, 1, 20);
   IntegerSetting Yrange = this.registerInteger("Y Range", 5, 1, 20);
   BooleanSetting single = this.registerBoolean("1x1", true);
   BooleanSetting Double = this.registerBoolean("2x1", true);
   BooleanSetting fourBlocks = this.registerBoolean("2x2", true);
   BooleanSetting custom = this.registerBoolean("Custom", true);
   ModeSetting type = this.registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Air", "Ground", "Flat", "Slab", "Double"), "Air");
   BooleanSetting hideOwn = this.registerBoolean("Hide Own", false);
   BooleanSetting flatOwn = this.registerBoolean("Flat Own", false);
   BooleanSetting fov = this.registerBoolean("In Fov", false);
   DoubleSetting slabHeight = this.registerDouble("Slab Height", 0.5, 0.0, 2.0);
   DoubleSetting outslabHeight = this.registerDouble("Outline Height", 0.5, 0.0, 2.0);
   IntegerSetting width = this.registerInteger("Width", 1, 1, 10);
   ColorSetting bedrockColor = this.registerColor("Bedrock Color", new GSColor(0, 255, 0));
   ColorSetting obsidianColor = this.registerColor("Obsidian Color", new GSColor(255, 0, 0));
   ColorSetting twobedrockColor = this.registerColor("2x1 Bedrock Color", new GSColor(0, 255, 0));
   ColorSetting twoobsidianColor = this.registerColor("2x1 Obsidian Color", new GSColor(255, 0, 0));
   ColorSetting fourColor = this.registerColor("2x2 Color", new GSColor(255, 0, 0));
   ColorSetting customColor = this.registerColor("Custom Color", new GSColor(0, 0, 255));
   IntegerSetting alpha = this.registerInteger("Alpha", 50, 0, 255);
   IntegerSetting ufoAlpha = this.registerInteger("UFOAlpha", 255, 0, 255);
   private ConcurrentHashMap<AxisAlignedBB, GSColor> holes;

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.world != null) {
         if (this.holes == null) {
            this.holes = new ConcurrentHashMap<>();
         } else {
            this.holes.clear();
         }

         HashSet<BlockPos> possibleHoles = Sets.newHashSet();

         for (BlockPos pos : EntityUtil.getSphere(
            PlayerUtil.getPlayerPos(), (double)this.range.getValue().intValue(), (double)this.Yrange.getValue().intValue(), false, false, 0
         )) {
            if ((!this.fov.getValue() || RotationUtil.isInFov(pos))
               && mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)
               && !mc.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(Blocks.AIR)
               && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)
               && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
               possibleHoles.add(pos);
            }
         }

         possibleHoles.forEach(posx -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(posx, false, false, true);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
               HoleUtil.BlockSafety holeSafety = holeInfo.getSafety();
               AxisAlignedBB centreBlocks = holeInfo.getCentre();
               if (centreBlocks == null) {
                  return;
               }

               if (this.fourBlocks.getValue() && holeType == HoleUtil.HoleType.FOUR) {
                  GSColor colour = new GSColor(this.fourColor.getValue(), 255);
                  this.holes.put(centreBlocks, colour);
               } else if (this.custom.getValue() && holeType == HoleUtil.HoleType.CUSTOM) {
                  GSColor colour = new GSColor(this.customColor.getValue(), 255);
                  this.holes.put(centreBlocks, colour);
               } else if (this.Double.getValue() && holeType == HoleUtil.HoleType.DOUBLE) {
                  GSColor colour;
                  if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                     colour = new GSColor(this.twobedrockColor.getValue(), 255);
                  } else {
                     colour = new GSColor(this.twoobsidianColor.getValue(), 255);
                  }

                  this.holes.put(centreBlocks, colour);
               } else if (this.single.getValue() && holeType == HoleUtil.HoleType.SINGLE) {
                  GSColor colour;
                  if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                     colour = new GSColor(this.bedrockColor.getValue(), 255);
                  } else {
                     colour = new GSColor(this.obsidianColor.getValue(), 255);
                  }

                  this.holes.put(centreBlocks, colour);
               }
            }
         });
      }
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.player != null && mc.world != null && this.holes != null && !this.holes.isEmpty()) {
         this.holes.forEach(this::renderHoles);
      }
   }

   private void renderHoles(AxisAlignedBB hole, GSColor color) {
      String var3 = this.type.getValue();
      switch (var3) {
         case "Outline":
            this.renderOutline(hole, color);
            break;
         case "Fill":
            this.renderFill(hole, color);
            break;
         case "Both":
            this.renderOutline(hole, color);
            this.renderFill(hole, color);
      }
   }

   private void renderFill(AxisAlignedBB hole, GSColor color) {
      GSColor fillColor = new GSColor(color, this.alpha.getValue());
      int ufoAlpha = this.ufoAlpha.getValue() * 50 / 255;
      if (!this.hideOwn.getValue() || !hole.intersects(mc.player.getEntityBoundingBox())) {
         String var5 = this.mode.getValue();
         switch (var5) {
            case "Air":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBox(hole, true, 1.0, fillColor, ufoAlpha, 1);
               } else {
                  RenderUtil.drawBox(hole, true, 1.0, fillColor, ufoAlpha, 63);
               }
               break;
            case "Ground":
               RenderUtil.drawBox(hole.offset(0.0, -1.0, 0.0), true, 1.0, new GSColor(fillColor, ufoAlpha), fillColor.getAlpha(), 63);
               break;
            case "Flat":
               RenderUtil.drawBox(hole, true, 1.0, fillColor, ufoAlpha, 1);
               break;
            case "Slab":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBox(hole, true, 1.0, fillColor, ufoAlpha, 1);
               } else {
                  RenderUtil.drawBox(hole, false, this.slabHeight.getValue(), fillColor, ufoAlpha, 63);
               }
               break;
            case "Double":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBox(hole, true, 1.0, fillColor, ufoAlpha, 1);
               } else {
                  RenderUtil.drawBox(hole.setMaxY(hole.maxY + 1.0), true, 2.0, fillColor, ufoAlpha, 63);
               }
         }
      }
   }

   private void renderOutline(AxisAlignedBB hole, GSColor color) {
      GSColor outlineColor = new GSColor(color, 255);
      if (!this.hideOwn.getValue() || !hole.intersects(mc.player.getEntityBoundingBox())) {
         String var4 = this.mode.getValue();
         switch (var4) {
            case "Air":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBoundingBoxWithSides(hole, this.width.getValue(), outlineColor, this.ufoAlpha.getValue(), 1);
               } else {
                  RenderUtil.drawBoundingBox(hole, this.width.getValue().intValue(), outlineColor, this.ufoAlpha.getValue());
               }
               break;
            case "Ground":
               RenderUtil.drawBoundingBox(
                  hole.offset(0.0, -1.0, 0.0),
                  this.width.getValue().intValue(),
                  new GSColor(outlineColor, this.ufoAlpha.getValue()),
                  outlineColor.getAlpha()
               );
               break;
            case "Flat":
               RenderUtil.drawBoundingBoxWithSides(hole, this.width.getValue(), outlineColor, this.ufoAlpha.getValue(), 1);
               break;
            case "Slab":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBoundingBoxWithSides(hole, this.width.getValue(), outlineColor, this.ufoAlpha.getValue(), 1);
               } else {
                  RenderUtil.drawBoundingBox(
                     hole.setMaxY(hole.minY + this.outslabHeight.getValue()),
                     this.width.getValue().intValue(),
                     outlineColor,
                     this.ufoAlpha.getValue()
                  );
               }
               break;
            case "Double":
               if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                  RenderUtil.drawBoundingBoxWithSides(hole, this.width.getValue(), outlineColor, this.ufoAlpha.getValue(), 1);
               } else {
                  RenderUtil.drawBoundingBox(
                     hole.setMaxY(hole.maxY + 1.0), this.width.getValue().intValue(), outlineColor, this.ufoAlpha.getValue()
                  );
               }
         }
      }
   }
}
