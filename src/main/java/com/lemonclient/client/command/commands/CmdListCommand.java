package com.lemonclient.client.command.commands;

import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.command.Command;
import com.lemonclient.client.command.CommandManager;

@Command.Declaration(name = "Commands", syntax = "commands", alias = {"commands", "cmd", "command", "commandlist", "help"})
public class CmdListCommand extends Command {
   @Override
   public void onCommand(String command, String[] message, boolean none) {
      for (Command command1 : CommandManager.getCommands()) {
         MessageBus.sendMessage(command1.getName() + ": \"" + command1.getSyntax() + "\"!", true);
      }
   }
}
