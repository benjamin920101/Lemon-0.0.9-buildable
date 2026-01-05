package com.lemonclient.client;

import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.misc.ShulkerBypass;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = "peek", name = "PeekBypass", version = "1", acceptedMinecraftVersions = "[1.12.2]")
public class PeekCmd {
   public static int metadataTicks = -1;
   public static int guiTicks = -1;
   public static ItemStack shulker = ItemStack.EMPTY;
   public static EntityItem drop;
   public static InventoryBasic toOpen;
   public static Minecraft mc = Minecraft.getMinecraft();

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      ClientCommandHandler.instance.registerCommand(new PeekCmd.PeekCommand());
   }

   public static NBTTagCompound getShulkerNBT(ItemStack stack) {
      if (mc.player == null) {
         return null;
      } else {
         NBTTagCompound compound = stack.getTagCompound();
         if (compound != null && compound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
            if (ModuleManager.getModule("Peek").isEnabled() && ShulkerBypass.shulkers) {
               if (tags.hasKey("Items", 9)) {
                  return tags;
               }

               MessageBus.sendMessage("Shulker is empty.", Notification.Type.INFO, "Peek", 3, ShulkerBypass.notification);
            }
         }

         return null;
      }
   }

   public static class PeekCommand extends CommandBase implements IClientCommand {
      public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
         return false;
      }

      public String getName() {
         return "peek";
      }

      public String getUsage(ICommandSender sender) {
         return null;
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
         if (PeekCmd.mc.player != null && ModuleManager.getModule("Peek").isEnabled() && ShulkerBypass.shulkers) {
            if (!PeekCmd.shulker.isEmpty()) {
               NBTTagCompound shulkerNBT = PeekCmd.getShulkerNBT(PeekCmd.shulker);
               if (shulkerNBT != null) {
                  TileEntityShulkerBox fakeShulker = new TileEntityShulkerBox();
                  fakeShulker.loadFromNbt(shulkerNBT);
                  String customName = "container.shulkerBox";
                  boolean hasCustomName = false;
                  if (shulkerNBT.hasKey("CustomName", 8)) {
                     customName = shulkerNBT.getString("CustomName");
                     hasCustomName = true;
                  }

                  InventoryBasic inv = new InventoryBasic(customName, hasCustomName, 27);

                  for (int i = 0; i < 27; i++) {
                     inv.setInventorySlotContents(i, fakeShulker.getStackInSlot(i));
                  }

                  PeekCmd.toOpen = inv;
                  PeekCmd.guiTicks = 0;
               }
            } else {
               MessageBus.sendMessage(
                  "No shulker detected! please drop and pickup your shulker.", Notification.Type.ERROR, "Peek", 3, ShulkerBypass.notification
               );
            }
         }
      }

      public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
         return true;
      }
   }
}
