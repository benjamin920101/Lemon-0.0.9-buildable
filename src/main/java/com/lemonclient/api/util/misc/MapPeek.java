package com.lemonclient.api.util.misc;

import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.misc.ShulkerBypass;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

public class MapPeek {
   private final Minecraft mc = Minecraft.getMinecraft();
   private List<List<String>> pages = new ArrayList<>();

   public static List<List<String>> getTextInBook(ItemStack item) {
      List<String> pages = new ArrayList<>();
      NBTTagCompound nbt = item.getTagCompound();
      if (nbt != null && nbt.hasKey("pages")) {
         NBTTagList nbt2 = nbt.getTagList("pages", 8);
         nbt2.forEach(b -> pages.add(((NBTTagString)b).getString()));
      }

      List<List<String>> finalPages = new ArrayList<>();

      for (String s : pages) {
         String buffer = "";
         List<String> pageBuffer = new ArrayList<>();
         char[] chars = s.toCharArray();

         for (char c : chars) {
            if (Minecraft.getMinecraft().fontRenderer.getStringWidth(buffer) > 114 || buffer.endsWith("\n")) {
               pageBuffer.add(buffer.replace("\n", ""));
               buffer = "";
            }

            buffer = buffer + c;
         }

         pageBuffer.add(buffer);
         finalPages.add(pageBuffer);
      }

      return finalPages;
   }

   public void draw(int mouseX, int mouseY, GuiContainer screen) {
      try {
         this.pages = null;
         Slot slot = screen.getSlotUnderMouse();
         if (slot == null) {
            return;
         }

         if (ModuleManager.isModuleEnabled("Peek") && ShulkerBypass.books) {
            this.drawBookToolTip(slot, mouseX, mouseY);
         }

         if (ModuleManager.isModuleEnabled("Peek") && ShulkerBypass.maps) {
            if (slot.getStack().getItem() != Items.FILLED_MAP) {
               return;
            }

            MapData data = Items.FILLED_MAP.getMapData(slot.getStack(), this.mc.world);
            byte[] colors = Objects.requireNonNull(data).colors;
            GL11.glPushMatrix();
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslated(0.0, 0.0, 300.0);
            int x = mouseX * 2 + 30;
            int y = mouseY * 2 - 164;
            this.renderTooltipBox(x - 12, y + 12, 128, 128);

            for (int b : colors) {
               if (b / 4 != 0) {
                  GuiScreen.drawRect(x, y, x + 1, y + 1, MapColor.COLORS[(b & 0xFF) / 4].getMapColor(b & 0xFF & 3));
               }

               if (x - (mouseX * 2 + 30) == 127) {
                  x = mouseX * 2 + 30;
                  y++;
               } else {
                  x++;
               }
            }

            GL11.glScaled(2.0, 2.0, 2.0);
            GL11.glPopMatrix();
         }
      } catch (Exception var13) {
         System.out.println("oopsie poopsie");
         var13.printStackTrace();
      }
   }

   public void drawBookToolTip(Slot slot, int mX, int mY) {
      if (slot.getStack().getItem() == Items.WRITABLE_BOOK || slot.getStack().getItem() == Items.WRITTEN_BOOK) {
         if (this.pages == null) {
            this.pages = getTextInBook(slot.getStack());
         }

         if (!this.pages.isEmpty()) {
            int lenght = this.mc.fontRenderer.getStringWidth("Page: 1/" + this.pages.size());
            this.renderTooltipBox(mX + 56 - lenght / 2, mY - this.pages.get(0).size() * 10 - 19, 5, lenght);
            this.renderTooltipBox(mX, mY - this.pages.get(0).size() * 10 - 6, this.pages.get(0).size() * 10 - 2, 120);
            this.mc.fontRenderer.drawStringWithShadow("Page: 1/" + this.pages.size(), mX + 68 - lenght / 2, mY - this.pages.get(0).size() * 10 - 32, -1);
            int count = 0;

            for (String s : this.pages.get(0)) {
               this.mc.fontRenderer.drawStringWithShadow(s, mX + 12, mY - 18 - this.pages.get(0).size() * 10 + count * 10, 49344);
               count++;
            }
         }
      }
   }

   public void renderTooltipBox(int x1, int y1, int x2, int y2) {
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GlStateManager.translate(0.0F, 0.0F, 300.0F);
      int int_5 = x1 + 12;
      int int_6 = y1 - 12;
      GuiScreen.drawRect(int_5 - 3, int_6 - 4, int_5 + y2 + 3, int_6 - 3, -267386864);
      GuiScreen.drawRect(int_5 - 3, int_6 + x2 + 3, int_5 + y2 + 3, int_6 + x2 + 4, -267386864);
      GuiScreen.drawRect(int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 + x2 + 3, -267386864);
      GuiScreen.drawRect(int_5 - 4, int_6 - 3, int_5 - 3, int_6 + x2 + 3, -267386864);
      GuiScreen.drawRect(int_5 + y2 + 3, int_6 - 3, int_5 + y2 + 4, int_6 + x2 + 3, -267386864);
      GuiScreen.drawRect(int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + x2 + 3 - 1, 1347420415);
      GuiScreen.drawRect(int_5 + y2 + 2, int_6 - 3 + 1, int_5 + y2 + 3, int_6 + x2 + 3 - 1, 1347420415);
      GuiScreen.drawRect(int_5 - 3, int_6 - 3, int_5 + y2 + 3, int_6 - 3 + 1, 1347420415);
      GuiScreen.drawRect(int_5 - 3, int_6 + x2 + 2, int_5 + y2 + 3, int_6 + x2 + 3, 1344798847);
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enableRescaleNormal();
   }
}
