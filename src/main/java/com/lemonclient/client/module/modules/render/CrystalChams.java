package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.NewRenderEntityEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.awt.Color;
import java.util.Arrays;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

@Module.Declaration(name = "CrystalChams", category = Category.Render)
public class CrystalChams extends Module {
   IntegerSetting range = this.registerInteger("Range", 32, 0, 256);
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Normal", "Gradient"), "Normal");
   BooleanSetting chams = this.registerBoolean("Chams", false);
   BooleanSetting throughWalls = this.registerBoolean("ThroughWalls", false);
   BooleanSetting wireframe = this.registerBoolean("Wireframe", false);
   BooleanSetting wireWalls = this.registerBoolean("WireThroughWalls", false);
   DoubleSetting spinSpeed = this.registerDouble("SpinSpeed", 1.0, 0.0, 4.0);
   DoubleSetting floatSpeed = this.registerDouble("FloatSpeed", 1.0, 0.0, 4.0);
   ColorSetting color = this.registerColor("Color", new GSColor(255, 255, 255, 255), true);
   ColorSetting wireFrameColor = this.registerColor("WireframeColor", new GSColor(255, 255, 255, 255), true);
   DoubleSetting lineWidth = this.registerDouble("lineWidth", 1.0, 0.0, 4.0);
   DoubleSetting lineWidthInterp = this.registerDouble("lineWidthInterp", 1.0, 0.1, 4.0);
   BooleanSetting show = this.registerBoolean("ShowEntity ;;", false);
   @EventHandler
   private final Listener<NewRenderEntityEvent> renderEntityHeadEventListener = new Listener<>(
      event -> {
         if (mc.player != null && mc.world != null && event.entityIn != null && event.entityIn.getName().length() != 0) {
            if (event.entityIn instanceof EntityEnderCrystal && !(mc.player.getDistance(event.entityIn) > this.range.getValue().intValue())) {
               if (!this.show.getValue()) {
                  event.cancel();
               }

               this.prepare();
               float spinTicks = ((EntityEnderCrystal)event.entityIn).innerRotation + Minecraft.getMinecraft().getRenderPartialTicks();
               float floatTicks = MathHelper.sin(spinTicks * 0.2F * this.floatSpeed.getValue().floatValue()) / 2.0F + 0.5F;
               float spinSpeed = this.spinSpeed.getValue().floatValue();
               float scale = 0.0625F;
               float swingAmount = spinTicks * 3.0F * spinSpeed;
               floatTicks = floatTicks * floatTicks + floatTicks;
               floatTicks *= 0.2F;
               GlStateManager.glLineWidth(
                  this.getInterpolatedLinWid(
                     mc.player.getDistance(event.entityIn) + 1.0F, this.lineWidth.getValue().floatValue(), this.lineWidthInterp.getValue().floatValue()
                  )
               );
               GL11.glDisable(3553);
               if (this.mode.getValue().equals("Gradient")) {
                  GL11.glPushAttrib(1048575);
                  GL11.glEnable(3042);
                  GL11.glDisable(2896);
                  GL11.glDisable(3553);
                  float alpha = this.color.getValue().getAlpha() / 255.0F;
                  GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
                  event.modelBase.render(event.entityIn, 0.0F, swingAmount, floatTicks, 0.0F, 0.0F, scale);
                  GL11.glEnable(3553);
                  GL11.glBlendFunc(770, 771);
                  float f = event.entityIn.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks();
                  mc.getTextureManager().bindTexture(new ResourceLocation("textures/rainbow.png"));
                  Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
                  GlStateManager.enableBlend();
                  GlStateManager.depthFunc(514);
                  GlStateManager.depthMask(false);
                  GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

                  for (int i = 0; i < 2; i++) {
                     GlStateManager.disableLighting();
                     GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
                     GlStateManager.matrixMode(5890);
                     GlStateManager.loadIdentity();
                     GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 0.5F);
                     GlStateManager.translate(0.0F, f * (0.001F + i * 0.003F) * 20.0F, 0.0F);
                     GlStateManager.matrixMode(5888);
                     event.modelBase.render(event.entityIn, 0.0F, swingAmount, floatTicks, 0.0F, 0.0F, scale);
                  }

                  GlStateManager.matrixMode(5890);
                  GlStateManager.loadIdentity();
                  GlStateManager.matrixMode(5888);
                  GlStateManager.enableLighting();
                  GlStateManager.depthMask(true);
                  GlStateManager.depthFunc(515);
                  GlStateManager.disableBlend();
                  mc.entityRenderer.setupFogColor(false);
                  GL11.glPopAttrib();
               } else {
                  if (this.wireframe.getValue()) {
                     Color wireColor = this.wireFrameColor.getValue();
                     GL11.glPushAttrib(1048575);
                     GL11.glEnable(3042);
                     GL11.glDisable(3553);
                     GL11.glDisable(2896);
                     GL11.glBlendFunc(770, 771);
                     GL11.glPolygonMode(1032, 6913);
                     if (this.wireWalls.getValue()) {
                        GL11.glDepthMask(false);
                        GL11.glDisable(2929);
                     }

                     GL11.glColor4f(wireColor.getRed() / 255.0F, wireColor.getGreen() / 255.0F, wireColor.getBlue() / 255.0F, wireColor.getAlpha() / 255.0F);
                     event.modelBase.render(event.entityIn, 0.0F, swingAmount, floatTicks, 0.0F, 0.0F, scale);
                     GL11.glPopAttrib();
                  }

                  if (this.chams.getValue()) {
                     Color chamsColor = this.color.getValue();
                     GL11.glPushAttrib(1048575);
                     GL11.glEnable(3042);
                     GL11.glDisable(3553);
                     GL11.glDisable(2896);
                     GL11.glDisable(3008);
                     GL11.glBlendFunc(770, 771);
                     GL11.glEnable(2960);
                     GL11.glEnable(10754);
                     if (this.throughWalls.getValue()) {
                        GL11.glDepthMask(false);
                        GL11.glDisable(2929);
                     }

                     GL11.glColor4f(chamsColor.getRed() / 255.0F, chamsColor.getGreen() / 255.0F, chamsColor.getBlue() / 255.0F, chamsColor.getAlpha() / 255.0F);
                     event.modelBase.render(event.entityIn, 0.0F, swingAmount, floatTicks, 0.0F, 0.0F, scale);
                     GL11.glPopAttrib();
                  }
               }

               event.limbSwing = 0.0F;
               event.limbSwingAmount = swingAmount;
               event.ageInTicks = floatTicks;
               event.netHeadYaw = 0.0F;
               event.headPitch = 0.0F;
               event.scale = scale;
               this.release();
            }
         }
      }
   );

   void prepare() {
      GlStateManager.pushMatrix();
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      GlStateManager.depthMask(false);
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
      GL11.glDisable(3553);
      GL11.glEnable(2848);
      GL11.glBlendFunc(770, 771);
   }

   void release() {
      GlStateManager.depthMask(true);
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.popMatrix();
      GL11.glEnable(3553);
      GL11.glPolygonMode(1032, 6914);
      new GSColor(255, 255, 255, 255).glColor();
   }

   float getInterpolatedLinWid(float distance, float line, float lineFactor) {
      return line * lineFactor / distance;
   }
}
