package com.lemonclient.client.command.commands;

import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.command.Command;

@Command.Declaration(name = "Coords", syntax = "coords [module]", alias = {"coords", "position", "pos"})
public class CoordsCommand extends Command {
   @Override
   public void onCommand(String command, String[] message, boolean none) {
      if (mc.player != null && mc.world != null) {
         String name = message[0];
         MessageBus.sendServerMessage(
            "/msg "
               + name
               + " X:"
               + (int)mc.player.posX
               + ", Y:"
               + (int)mc.player.posY
               + ", Z:"
               + (int)mc.player.posZ
         );
      }
   }
}
