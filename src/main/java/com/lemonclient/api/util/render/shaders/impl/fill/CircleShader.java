package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class CircleShader extends FramebufferShader {
   public static final CircleShader INSTANCE = new CircleShader();
   public float time;

   public CircleShader() {
      super("circle.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("colors");
      this.setupUniform("PI");
      this.setupUniform("rad");
   }

   public void updateUniforms(float duplicate, Color color, Double PI, Double rad) {
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform1f(this.getUniform("PI"), PI.floatValue());
      GL20.glUniform1f(this.getUniform("rad"), rad.floatValue());
      GL20.glUniform4f(this.getUniform("colors"), color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
   }

   public void stopDraw(float duplicate, Color color, Double PI, Double rad) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      GL11.glPushMatrix();
      this.startShader(duplicate, color, PI, rad);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(float duplicate, Color color, Double PI, Double rad) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(duplicate, color, PI, rad);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
