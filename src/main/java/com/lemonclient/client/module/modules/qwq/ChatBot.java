package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.ServerUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketChat;

@Module.Declaration(name = "ChatBot", category = Category.qwq)
public class ChatBot extends Module {
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Client", "Everyone"), "Everyone");
   IntegerSetting delay = this.registerInteger("Delay", 0, 0, 20, () -> this.mode.getValue().equals("Everyone"));
   String botmessage;
   boolean msg;
   int waited;
   private final Pattern CHAT_PATTERN = Pattern.compile("<.*?> ");
   private final Pattern CHAT_PATTERN2 = Pattern.compile("(.*?)");
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (!this.msg) {
            if (event.getPacket() instanceof SPacketChat) {
               String s = ((SPacketChat)event.getPacket()).getChatComponent().getUnformattedText();
               Matcher matcher = this.CHAT_PATTERN.matcher(s);
               String username = "unnamed";
               Matcher matcher2 = this.CHAT_PATTERN2.matcher(s);
               if (matcher2.find()) {
                  matcher2.group();
                  s = matcher2.replaceFirst("");
               }

               if (matcher.find()) {
                  username = matcher.group();
                  username = username.substring(1, username.length() - 2);
                  s = matcher.replaceFirst("");
               }

               if (!s.startsWith("!")) {
                  return;
               }

               s = s.substring(Math.min(s.length(), 1));
               if (s.startsWith("online")) {
                  return;
               }

               if (s.startsWith("ping")) {
                  s = s.substring(Math.min(s.length(), 5));
                  ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                  for (Entity qwq : mc.world.loadedEntityList) {
                     if (qwq instanceof EntityPlayer && s.contains(qwq.getName())) {
                        s = qwq.getName();
                     }
                  }

                  String finalS1 = s;
                  NetworkPlayerInfo profile = infoMap.stream()
                     .filter(networkPlayerInfo -> finalS1.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase()))
                     .findFirst()
                     .orElse(null);
                  if (profile != null) {
                     String message = profile.getGameProfile().getName() + "'s ping is " + profile.getResponseTime();
                     String messageSanitized = message.replaceAll("禮", "");
                     if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                     }

                     this.botmessage = messageSanitized;
                     this.msg = true;
                  }
               } else if (s.startsWith("myping")) {
                  ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());
                  String finalUsername = username;
                  NetworkPlayerInfo profile = infoMap.stream()
                     .filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(finalUsername))
                     .findFirst()
                     .orElse(null);
                  if (profile != null) {
                     String message = "Your ping is " + profile.getResponseTime();
                     String messageSanitized = message.replaceAll("禮", "");
                     if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                     }

                     this.botmessage = messageSanitized;
                     this.msg = true;
                  }
               } else if (s.startsWith("tps")) {
                  String message = "The tps is now " + ServerUtil.getTPS();
                  String messageSanitized = message.replaceAll("禮", "");
                  if (messageSanitized.length() > 255) {
                     messageSanitized = messageSanitized.substring(0, 255);
                  }

                  this.botmessage = messageSanitized;
                  this.msg = true;
               } else if (s.startsWith("help")) {
                  String uwu = "The commands are : tps, myping, ping playername";
                  String messageSanitized = uwu.replaceAll("禮", "");
                  if (messageSanitized.length() > 255) {
                     messageSanitized = messageSanitized.substring(0, 255);
                  }

                  this.botmessage = messageSanitized;
                  this.msg = true;
               } else if (s.startsWith("gay")) {
                  s = s.substring(Math.min(s.length(), 4));
                  ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                  for (Entity qwqx : mc.world.loadedEntityList) {
                     if (qwqx instanceof EntityPlayer && s.contains(qwqx.getName())) {
                        s = qwqx.getName();
                     }
                  }

                  String finalS2 = s;
                  NetworkPlayerInfo profile = infoMap.stream()
                     .filter(networkPlayerInfo -> finalS2.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase()))
                     .findFirst()
                     .orElse(null);
                  if (profile != null) {
                     String name = profile.getGameProfile().getName();
                     this.botmessage = name + " is " + String.format("%.1f", Math.random() * 100.0) + "% gay";
                     this.msg = true;
                  }
               } else if (s.startsWith("byebyebot")) {
                  this.botmessage = "!online owob";
                  this.msg = true;
               } else {
                  String uwu = "Sorry, I cant understand this command";
                  String messageSanitized = uwu.replaceAll("禮", "");
                  if (messageSanitized.length() > 255) {
                     messageSanitized = messageSanitized.substring(0, 255);
                  }

                  this.botmessage = messageSanitized;
                  this.msg = true;
               }
            }
         }
      }
   );

   @Override
   public void onUpdate() {
      if (this.msg) {
         if (this.mode.getValue().equals("Client")) {
            MessageBus.sendClientDeleteMessage(this.botmessage, Notification.Type.INFO, "ChatBot", 4);
            this.msg = false;
         } else if (this.waited++ >= this.delay.getValue()) {
            MessageBus.sendServerMessage(this.botmessage);
            this.waited = 0;
            this.msg = false;
         }
      }
   }
}
