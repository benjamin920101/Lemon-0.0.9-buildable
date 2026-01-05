package com.lemonclient.api.util.render.shaders;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FramebufferShader extends Shader {
   protected Framebuffer framebuffer;
   protected float red;
   protected float green;
   protected float blue;
   protected float alpha;
   protected float radius;
   protected float quality;
   protected boolean entityShadows;
   protected Minecraft mc = Minecraft.getMinecraft();

   public FramebufferShader(String fragmentShader) {
      super(fragmentShader);
      this.alpha = 1.0F;
      this.radius = 2.0F;
      this.quality = 1.0F;
   }

   public void startDraw(float partialTicks) {
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();
      (this.framebuffer = this.setupFrameBuffer(this.framebuffer)).framebufferClear();
      this.framebuffer.bindFramebuffer(true);
      this.entityShadows = this.mc.gameSettings.entityShadows;
      this.mc.gameSettings.entityShadows = false;
      this.mc.entityRenderer.setupCameraTransform(partialTicks, 0);
   }

   public void stopDraw(Color color, float radius, float quality, float duplicate) {
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
      this.startShader(duplicate);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
      if (frameBuffer == null) {
         return new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
      } else {
         if (frameBuffer.framebufferWidth != this.mc.displayWidth || frameBuffer.framebufferHeight != this.mc.displayHeight) {
            frameBuffer.deleteFramebuffer();
            frameBuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
         }

         return frameBuffer;
      }
   }

   public void drawFramebuffer(Framebuffer framebuffer) {
      ScaledResolution scaledResolution = new ScaledResolution(this.mc);
      GL11.glBindTexture(3553, framebuffer.framebufferTexture);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(0.0, scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d(scaledResolution.getScaledWidth(), 0.0);
      GL11.glEnd();
      GL20.glUseProgram(0);
   }
}
