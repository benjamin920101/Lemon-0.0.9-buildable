package com.lemonclient.api.util.render.shaders.impl.fill;

import com.lemonclient.api.util.render.shaders.FramebufferShader;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FlowShader extends FramebufferShader {
   public static final FlowShader INSTANCE = new FlowShader();
   public float time;

   public FlowShader() {
      super("flow.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("color");
      this.setupUniform("iterations");
      this.setupUniform("formuparam2");
      this.setupUniform("stepsize");
      this.setupUniform("volsteps");
      this.setupUniform("zoom");
      this.setupUniform("tile");
      this.setupUniform("distfading");
      this.setupUniform("saturation");
      this.setupUniform("fadeBol");
   }

   public void updateUniforms(
      float duplicate,
      float red,
      float green,
      float blue,
      float alpha,
      int iteractions,
      float formuparam2,
      float zoom,
      float volumSteps,
      float stepSize,
      float title,
      float distfading,
      float saturation,
      float cloud,
      int fade
   ) {
      GL20.glUniform2f(
         this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform4f(this.getUniform("color"), red, green, blue, alpha);
      GL20.glUniform1i(this.getUniform("iterations"), iteractions);
      GL20.glUniform1f(this.getUniform("formuparam2"), formuparam2);
      GL20.glUniform1i(this.getUniform("volsteps"), (int)volumSteps);
      GL20.glUniform1f(this.getUniform("stepsize"), stepSize);
      GL20.glUniform1f(this.getUniform("zoom"), zoom);
      GL20.glUniform1f(this.getUniform("tile"), title);
      GL20.glUniform1f(this.getUniform("distfading"), distfading);
      GL20.glUniform1f(this.getUniform("saturation"), saturation);
      GL20.glUniform1i(this.getUniform("fadeBol"), fade);
   }

   public void stopDraw(
      Color color,
      float radius,
      float quality,
      float duplicate,
      float red,
      float green,
      float blue,
      float alpha,
      int iteractions,
      float formuparam2,
      float zoom,
      float volumSteps,
      float stepSize,
      float title,
      float distfading,
      float saturation,
      float cloud,
      int fade
   ) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.radius = radius;
      this.quality = quality;
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      GL11.glPushMatrix();
      this.startShader(duplicate, red, green, blue, alpha, iteractions, formuparam2, zoom, volumSteps, stepSize, title, distfading, saturation, cloud, fade);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public void startShader(
      float duplicate,
      float red,
      float green,
      float blue,
      float alpha,
      int iteractions,
      float formuparam2,
      float zoom,
      float volumSteps,
      float stepSize,
      float title,
      float distfading,
      float saturation,
      float cloud,
      int fade
   ) {
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(duplicate, red, green, blue, alpha, iteractions, formuparam2, zoom, volumSteps, stepSize, title, distfading, saturation, cloud, fade);
   }

   public void update(double speed) {
      this.time = (float)(this.time + speed);
   }
}
