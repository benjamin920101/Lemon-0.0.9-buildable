package com.lemonclient.api.util.render.shaders.impl.outline;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import java.util.function.Predicate;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class CircleOutlineShader extends FramebufferShader {
   public static final CircleOutlineShader INSTANCE = new CircleOutlineShader();
   public float time = 0.0F;

   public CircleOutlineShader() {
      super("circleOutline.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("texture");
      this.setupUniform("texelSize");
      this.setupUniform("colors");
      this.setupUniform("divider");
      this.setupUniform("radius");
      this.setupUniform("maxSample");
      this.setupUniform("alpha0");
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("PI");
      this.setupUniform("rad");
   }

   public void updateUniforms(Color color, float radius, float quality, boolean gradientAlpha, int alphaOutline, float duplicate, Double PI, Double rad) {
      GL20.glUniform1i(this.getUniform("texture"), 0);
      GL20.glUniform2f(this.getUniform("texelSize"), 1.0F / this.mc.displayWidth * (radius * quality), 1.0F / this.mc.displayHeight * (radius * quality));
      GL20.glUniform3f(this.getUniform("colors"), color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
      GL20.glUniform1f(this.getUniform("divider"), 140.0F);
      GL20.glUniform1f(this.getUniform("radius"), radius);
      GL20.glUniform1f(this.getUniform("maxSample"), 10.0F);
      GL20.glUniform1f(this.getUniform("alpha0"), gradientAlpha ? -1.0F : alphaOutline / 255.0F);
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform1f(this.getUniform("PI"), PI.floatValue());
      GL20.glUniform1f(this.getUniform("rad"), rad.floatValue());
   }

   public void stopDraw(Color color, float radius, float quality, boolean gradientAlpha, int alphaOutline, float duplicate, Double PI, Double rad) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      this.startShader(color, radius, quality, gradientAlpha, alphaOutline, duplicate, PI, rad);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void stopDraw(
      Color color, float radius, float quality, boolean gradientAlpha, int alphaOutline, float duplicate, Double PI, Double rad, Predicate<Boolean> fill
   ) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      this.startShader(color, radius, quality, gradientAlpha, alphaOutline, duplicate, PI, rad);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      fill.test(false);
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(Color color, float radius, float quality, boolean gradientAlpha, int alphaOutline, float duplicate, Double PI, Double rad) {
      GL11.glPushMatrix();
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(color, radius, quality, gradientAlpha, alphaOutline, duplicate, PI, rad);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
