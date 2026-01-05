package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class PhobosShader extends FramebufferShader {
   public static final PhobosShader INSTANCE = new PhobosShader();
   public float time;

   public PhobosShader() {
      super("phobos.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("color");
      this.setupUniform("texelSize");
      this.setupUniform("texture");
   }

   public void updateUniforms(float duplicate, Color color, int lines, double tau) {
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1i(this.getUniform("texture"), 0);
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform4f(this.getUniform("color"), color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL20.glUniform2f(
         this.getUniform("texelSize"), 1.0F / this.mc.displayWidth * (this.radius * this.quality), 1.0F / this.mc.displayHeight * (this.radius * this.quality)
      );
   }

   public void stopDraw(Color color, float radius, float quality, float duplicate, int lines, double tau) {
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
      this.startShader(duplicate, color, lines, tau);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(float duplicate, Color color, int lines, double tau) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(duplicate, color, lines, tau);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
