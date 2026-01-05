package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

@Module.Declaration(name = "Copy IP", category = Category.Misc)
public class CopyIp extends Module {
   String server;

   @Override
   public void onEnable() {
      try {
         this.server = mc.getCurrentServerData().serverIP;
      } catch (Exception var4) {
         this.server = "Singleplayer";
      }

      String myString = this.server;
      StringSelection stringSelection = new StringSelection(myString);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
      MessageBus.sendClientPrefixMessage("Copied '" + this.server + "' to clipboard.", Notification.Type.INFO);
      this.disable();
   }
}
