package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.StringSetting;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketChat;

@Module.Declaration(name = "AutoSpam", category = Category.qwq)
public class AutoSpam extends Module {
   StringSetting string = this.registerString("Message", "/msg _yonkie_ gay");
   IntegerSetting delay = this.registerInteger("Delay", 1, 0, 200);
   BooleanSetting hide = this.registerBoolean("Hide", true);
   BooleanSetting randomThing = this.registerBoolean("Random Thing", false);
   BooleanSetting letter = this.registerBoolean("Letter", true, () -> this.randomThing.getValue());
   BooleanSetting number = this.registerBoolean("Number", true, () -> this.randomThing.getValue());
   IntegerSetting character = this.registerInteger("Character", 20, 0, 256, () -> this.randomThing.getValue());
   BooleanSetting antiSpam = this.registerBoolean("AntiSpam", true);
   String sent;
   int waited;
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null && this.hide.getValue()) {
         if (event.getPacket() instanceof SPacketChat) {
            String message = ((SPacketChat)event.getPacket()).getChatComponent().getUnformattedText();
            Matcher matcher = Pattern.compile("<.*?> ").matcher(message);
            String username = "";
            if (matcher.find()) {
               username = matcher.group();
               username = username.substring(1, username.length() - 2);
            } else if (message.contains(":")) {
               int spaceIndex = message.indexOf(" ");
               if (spaceIndex != -1) {
                  username = message.substring(0, spaceIndex);
               }
            }

            username = ColorMain.cleanColor(username);
            if (message.toLowerCase().contains("to") && message.contains(":") || username.equals(mc.player.getName())) {
               event.cancel();
               MessageBus.sendDeleteMessage("Spamming", "AutoSpam", 14);
            }
         }
      }
   });

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (this.waited++ >= this.delay.getValue()) {
            this.waited = 0;
            StringBuilder phrase = new StringBuilder(this.string.getText());
            if (this.randomThing.getValue()) {
               String characters = (this.letter.getValue() ? "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" : "")
                  + (this.number.getValue() ? "0123456789" : "");
               Random random = new Random();

               for (int i = 0; i < this.character.getValue(); i++) {
                  int index = random.nextInt(characters.length());
                  phrase.append(characters.charAt(index));
               }
            }

            if (this.antiSpam.getValue()) {
               Random random = new Random();
               int nextInt = random.nextInt(16777216);
               String hex = String.format("#%06x", nextInt);
               phrase.append(" [").append(hex).append("]");
            }

            this.sent = phrase.toString();
            MessageBus.sendServerMessage(this.sent);
         }
      }
   }
}
