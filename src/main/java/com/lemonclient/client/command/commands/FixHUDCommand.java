package com.lemonclient.client.command.commands;

import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.command.Command;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;

@Command.Declaration(name = "FixHUD", syntax = "fixhud", alias = {"fixhud", "hud", "resethud"})
public class FixHUDCommand extends Command {
   @Override
   public void onCommand(String command, String[] message, boolean none) {
      for (Module module : ModuleManager.getModules()) {
         if (module instanceof HUDModule) {
            ((HUDModule)module).resetPosition();
         }
      }

      MessageBus.sendCommandMessage("HUD positions reset!", true);
   }
}
