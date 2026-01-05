package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class RainbowCubeShader extends FramebufferShader {
   public static final RainbowCubeShader INSTANCE = new RainbowCubeShader();
   public float time;

   public RainbowCubeShader() {
      super("rainbowCube.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("alpha");
      this.setupUniform("WAVELENGTH");
      this.setupUniform("R");
      this.setupUniform("G");
      this.setupUniform("B");
      this.setupUniform("RSTART");
      this.setupUniform("GSTART");
      this.setupUniform("BSTART");
   }

   public void updateUniforms(float duplicate, Color start, int wave, int rStart, int gStart, int bStart) {
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform1f(this.getUniform("alpha"), start.getAlpha() / 255.0F);
      GL20.glUniform1f(this.getUniform("WAVELENGTH"), wave);
      GL20.glUniform1i(this.getUniform("R"), start.getRed());
      GL20.glUniform1i(this.getUniform("G"), start.getGreen());
      GL20.glUniform1i(this.getUniform("B"), start.getBlue());
      GL20.glUniform1i(this.getUniform("RSTART"), rStart);
      GL20.glUniform1i(this.getUniform("GSTART"), gStart);
      GL20.glUniform1i(this.getUniform("BSTART"), bStart);
   }

   public void stopDraw(Color color, float radius, float quality, float duplicate, Color start, int wave, int rStart, int gStart, int bStart) {
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
      this.startShader(duplicate, start, wave, rStart, gStart, bStart);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(float duplicate, Color start, int wave, int rStart, int gStart, int bStart) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(duplicate, start, wave, rStart, gStart, bStart);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
