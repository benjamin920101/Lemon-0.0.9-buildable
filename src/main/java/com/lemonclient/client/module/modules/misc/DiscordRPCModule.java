package com.lemonclient.client.module.modules.misc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

@Module.Declaration(name = "DiscordRPC", category = Category.Misc)
public class DiscordRPCModule extends Module {
   private static final String applicationId = "899193061324775454";
   BooleanSetting PlayerID = this.registerBoolean("Player ID", true);
   BooleanSetting ServerIp = this.registerBoolean("Server IP", true);
   BooleanSetting coords = this.registerBoolean("Coords", true);
   private final DiscordRPC discordRPC = DiscordRPC.INSTANCE;
   DiscordEventHandlers handlers = new DiscordEventHandlers();
   DiscordRichPresence presence = new DiscordRichPresence();
   static String lastChat;
   static ServerData svr;
   @EventHandler
   private final Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> lastChat = event.getMessage().getUnformattedText());

   @Override
   public void onEnable() {
      this.init();
   }

   @Override
   public void onDisable() {
      this.discordRPC.Discord_Shutdown();
      this.discordRPC.Discord_ClearPresence();
   }

   private void init() {
      this.discordRPC.Discord_Initialize("899193061324775454", this.handlers, true, "");
      this.presence.startTimestamp = System.currentTimeMillis() / 1000L;
      this.presence.state = "Main Menu";
      if (this.PlayerID.getValue()) {
         this.presence.details = ID();
      } else {
         this.presence.details = "";
      }

      this.presence.largeImageKey = "lemonclient";
      this.presence.largeImageText = "Lemon Client v0.0.9";
      this.discordRPC.Discord_UpdatePresence(this.presence);
      new Thread(
            () -> {
               while (!Thread.currentThread().isInterrupted() && this.isEnabled()) {
                  try {
                     this.discordRPC.Discord_RunCallbacks();
                     if (this.PlayerID.getValue()) {
                        this.presence.details = ID();
                     } else {
                        this.presence.details = "";
                     }

                     this.presence.state = "";
                     if (this.coords.getValue() && mc.player != null && mc.world != null) {
                        this.presence.smallImageKey = "lazy_crocodile";
                        String dimension;
                        if (this.dimension() == -1) {
                           dimension = "Nether";
                        } else if (this.dimension() == 0) {
                           dimension = "Overworld";
                        } else {
                           dimension = "The End";
                        }

                        this.presence.smallImageText = "X:"
                           + (int)mc.player.posX
                           + " Y:"
                           + (int)mc.player.posY
                           + " Z:"
                           + (int)mc.player.posZ
                           + " ("
                           + dimension
                           + ")";
                     } else {
                        this.presence.smallImageText = "";
                     }

                     if (mc.isIntegratedServerRunning()) {
                        this.presence.state = "Single Player";
                     } else if (mc.getCurrentServerData() != null) {
                        svr = mc.getCurrentServerData();
                        if (!svr.serverIP.equals("")) {
                           if (this.ServerIp.getValue()) {
                              this.presence.state = "Multi Player (" + svr.serverIP + ")";
                              if (svr.serverIP.equals("2b2t.org")) {
                                 try {
                                    if (lastChat.contains("Position in queue: ")) {
                                       this.presence.details = this.presence.details + " (in queue" + Integer.parseInt(lastChat.substring(19)) + ")";
                                    }
                                 } catch (Throwable var3) {
                                    var3.printStackTrace();
                                 }
                              }
                           } else {
                              this.presence.state = "Multi Player";
                           }
                        }
                     } else {
                        this.presence.details = "Main Menu";
                     }

                     this.discordRPC.Discord_UpdatePresence(this.presence);
                  } catch (Exception var4) {
                     var4.printStackTrace();
                  }

                  try {
                     Thread.sleep(5000L);
                  } catch (InterruptedException var2) {
                     var2.printStackTrace();
                  }
               }
            },
            "Discord-RPC-Callback-Handler"
         )
         .start();
   }

   private int dimension() {
      return mc.player.dimension;
   }

   public static String ID() {
      return mc.player != null ? mc.player.getName() : mc.getSession().getUsername();
   }
}
