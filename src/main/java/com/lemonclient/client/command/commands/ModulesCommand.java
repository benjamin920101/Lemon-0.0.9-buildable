package com.lemonclient.client.command.commands;

import com.lemonclient.client.command.Command;
import com.lemonclient.client.command.CommandManager;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Collection;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

@Command.Declaration(name = "Modules", syntax = "modules (click to toggle)", alias = {"modules", "module", "modulelist", "mod", "mods"})
public class ModulesCommand extends Command {
   @Override
   public void onCommand(String command, String[] message, boolean none) {
      TextComponentString msg = new TextComponentString("§7Modules: §f ");
      Collection<Module> modules = ModuleManager.getModules();
      int size = modules.size();
      int index = 0;

      for (Module module : modules) {
         msg.appendSibling(
            new TextComponentString(
                  (module.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED) + module.getName() + "§7" + (index == size - 1 ? "" : ", ")
               )
               .setStyle(
                  new Style()
                     .setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponentString(module.getCategory().name())))
                     .setClickEvent(
                        new ClickEvent(
                           net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND, CommandManager.getCommandPrefix() + "toggle " + module.getName()
                        )
                     )
               )
         );
         index++;
      }

      msg.appendSibling(new TextComponentString(ChatFormatting.GRAY + "!"));
      mc.ingameGUI.getChatGUI().printChatMessage(msg);
   }
}
