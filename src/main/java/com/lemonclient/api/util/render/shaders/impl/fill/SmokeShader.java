package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class SmokeShader extends FramebufferShader {
   public static final SmokeShader INSTANCE = new SmokeShader();
   public float time;

   public SmokeShader() {
      super("smoke.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("first");
      this.setupUniform("second");
      this.setupUniform("third");
      this.setupUniform("oct");
   }

   public void updateUniforms(float duplicate, Color first, Color second, Color third, int oct) {
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform4f(
         this.getUniform("first"), first.getRed() / 255.0F * 5.0F, first.getGreen() / 255.0F * 5.0F, first.getBlue() / 255.0F * 5.0F, first.getAlpha() / 255.0F
      );
      GL20.glUniform3f(this.getUniform("second"), second.getRed() / 255.0F * 5.0F, second.getGreen() / 255.0F * 5.0F, second.getBlue() / 255.0F * 5.0F);
      GL20.glUniform3f(this.getUniform("third"), third.getRed() / 255.0F * 5.0F, third.getGreen() / 255.0F * 5.0F, third.getBlue() / 255.0F * 5.0F);
      GL20.glUniform1i(this.getUniform("oct"), oct);
   }

   public void stopDraw(Color color, float radius, float quality, float duplicate, Color first, Color second, Color third, int oct) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.red = color.getRed() / 255.0F;
      this.green = color.getGreen() / 255.0F;
      this.blue = color.getBlue() / 255.0F;
      this.alpha = color.getAlpha() / 255.0F;
      this.radius = radius;
      this.quality = quality;
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      GL11.glPushMatrix();
      this.startShader(duplicate, first, second, third, oct);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(float duplicate, Color first, Color second, Color third, int oct) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(duplicate, first, second, third, oct);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
