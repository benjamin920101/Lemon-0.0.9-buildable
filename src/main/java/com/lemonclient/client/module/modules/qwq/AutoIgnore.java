package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketChat;

@Module.Declaration(name = "AutoIgnore", category = Category.qwq)
public class AutoIgnore extends Module {
   BooleanSetting filterFriend = this.registerBoolean("Filter Friend", false);
   BooleanSetting ignoreAll = this.registerBoolean("AllWhisper", false);
   BooleanSetting playerCheck = this.registerBoolean("PlayerCheck", true);
   IntegerSetting times = this.registerInteger("Times", 10, 0, 30);
   IntegerSetting life = this.registerInteger("LifeTime", 600, 0, 3000);
   HashMap<String, Integer> messageTimes = new HashMap<>();
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.player != null) {
            if (event.getPacket() instanceof SPacketChat) {
               String message = ((SPacketChat)event.getPacket()).getChatComponent().getUnformattedText();
               if (this.ignoreAll.getValue() && message.contains(":")) {
                  String username = "";
                  int spaceIndex = message.indexOf(" ");
                  if (spaceIndex != -1) {
                     username = message.substring(0, spaceIndex);
                  }

                  if (!username.isEmpty() && !SocialManager.isOnIgnoreList(username) && !SocialManager.isOnFriendList(username)
                     || !this.filterFriend.getValue()) {
                     SocialManager.addIgnore(username);
                     MessageBus.sendClientDeleteMessage(username + " has been added to ignore list", Notification.Type.INFO, "AutoIgnore", 13);
                  }
               }

               String s = message.replaceAll("\\[.*?]|<.*?>|\\d+", "");
               this.addToList(s);
               if (this.messageTimes.get(s) > this.times.getValue()) {
                  Matcher matcher = Pattern.compile("<.*?> ").matcher(message);
                  String usernamex = "";
                  if (matcher.find()) {
                     usernamex = matcher.group();
                     usernamex = usernamex.substring(1, usernamex.length() - 2);
                  } else if (message.contains(":")) {
                     int spaceIndexx = message.indexOf(" ");
                     if (spaceIndexx != -1) {
                        usernamex = message.substring(0, spaceIndexx);
                     }
                  }

                  usernamex = ColorMain.cleanColor(usernamex);
                  if (usernamex.equals(mc.player.getName())
                     || this.playerCheck.getValue() && mc.player.connection.getPlayerInfo(usernamex) == null) {
                     return;
                  }

                  if (!usernamex.isEmpty() && !SocialManager.isOnIgnoreList(usernamex) && !SocialManager.isOnFriendList(usernamex)
                     || !this.filterFriend.getValue()) {
                     SocialManager.addIgnore(usernamex);
                     MessageBus.sendClientDeleteMessage(usernamex + " has been added to ignore list", Notification.Type.INFO, "AutoIgnore", 13);
                  }

                  event.cancel();
               }
            }
         }
      }
   );

   public void addToList(final String string) {
      int time = 1;
      if (this.messageTimes.containsKey(string)) {
         time += this.messageTimes.get(string);
      }

      this.messageTimes.put(string, time);
      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            AutoIgnore.this.messageTimes.put(string, AutoIgnore.this.messageTimes.get(string) - 1);
         }
      }, this.life.getValue() * 1000);
   }
}
