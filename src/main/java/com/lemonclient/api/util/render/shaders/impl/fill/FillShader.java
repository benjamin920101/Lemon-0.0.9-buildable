package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FillShader extends FramebufferShader {
   public static final FillShader INSTANCE = new FillShader();
   public float time;

   public FillShader() {
      super("fill.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("color");
   }

   public void updateUniforms(float red, float green, float blue, float alpha) {
      GL20.glUniform4f(this.getUniform("color"), red, green, blue, alpha);
   }

   public void stopDraw(Color color) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      GL11.glPushMatrix();
      this.startShader(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(float red, float green, float blue, float alpha) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(red, green, blue, alpha);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
